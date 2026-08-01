package com.example.amls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amls.hardware.AmlsSensorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.sqrt

private fun Flow<Float>.promedioMovil(tamanoVentana: Int): Flow<Float> =
    scan(emptyList<Float>()) { ventana, valor -> (ventana + valor).takeLast(tamanoVentana) }
        .filter { it.isNotEmpty() }
        .map { it.average().toFloat() }

private fun Flow<Float>.desviacionEstandarMovil(tamanoVentana: Int): Flow<Float> =
    scan(emptyList<Float>()) { ventana, valor -> (ventana + valor).takeLast(tamanoVentana) }
        .filter { it.size >= 2 }
        .map { ventana ->
            val promedio = ventana.average()
            val varianza = ventana.map { (it - promedio) * (it - promedio) }.average()
            kotlin.math.sqrt(varianza).toFloat()
        }

private fun Flow<FloatArray>.promedioMovilVectorial(tamanoVentana: Int): Flow<FloatArray> =
    scan(emptyList<FloatArray>()) { ventana, valor -> (ventana + listOf(valor)).takeLast(tamanoVentana) }
        .filter { it.isNotEmpty() }
        .map { ventana ->
            floatArrayOf(
                ventana.map { it[0] }.average().toFloat(),
                ventana.map { it[1] }.average().toFloat(),
                ventana.map { it[2] }.average().toFloat()
            )
        }

@HiltViewModel
class AmlsViewModel @Inject constructor(
    private val sensorManager: AmlsSensorManager
) : ViewModel() {

    // 1. Mantenemos los flujos de datos crudos
    val acceleration = sensorManager.acceleration
    val lightLevel = sensorManager.lightLevel

    // 2. NUEVO: Evaluamos el contexto de iluminación
    val ambientContext = lightLevel.map { lux ->
        when {
            lux < 15f -> "Oscuro (Sugerir Modo Noche)"
            lux > 2000f -> "Mucha luz solar (Sugerir Alto Contraste)"
            else -> "Iluminación adecuada"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Calculando entorno..."
    )

    // 3. NUEVO: Evaluamos el movimiento calculando la magnitud del vector (x, y, z)
    val movementContext = acceleration.map { acc ->
        val x = acc[0]
        val y = acc[1]
        val z = acc[2]

        // La gravedad terrestre es ~9.8 m/s². Calculamos la fuerza total.
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        // Si la magnitud difiere significativamente de 9.8, hay movimiento
        if (magnitude > 11.5f || magnitude < 8.0f) {
            "En movimiento (Sugerir pausar lectura o aumentar fuente)"
        } else {
            "Dispositivo estable"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Calculando movimiento..."
    )

    // Normalización LOGARÍTMICA (no lineal): la luz varía en órdenes de
    // magnitud entre interior y sol directo. log10(lux+1)/4.0 da:
    // 15 lux -> ~0.30, 300 lux -> ~0.62, 2000 lux -> ~0.83 — alineado
    // con tus propios umbrales de ambientContext.
    val nivelLuzNormalizado = lightLevel.promedioMovil(8).map { lux ->
        (kotlin.math.log10(lux.toDouble() + 1.0) / 4.0).toFloat().coerceIn(0f, 1f)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.5f
    )

    val nivelMovimientoNormalizado = acceleration
        .map { acc -> sqrt((acc[0] * acc[0] + acc[1] * acc[1] + acc[2] * acc[2]).toDouble()).toFloat() }
        .desviacionEstandarMovil(30) // más historia, menos sensible a golpes cortos
        .map { desviacionEstandar ->
            (desviacionEstandar.coerceIn(0f, 1.3f) / 1.3f)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    fun startSensors() {
        sensorManager.startListening()
    }

    fun stopSensors() {
        sensorManager.stopListening()
    }
}
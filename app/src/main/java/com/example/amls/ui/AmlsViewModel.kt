package com.example.amls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amls.hardware.AmlsSensorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.sqrt

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

    fun startSensors() {
        sensorManager.startListening()
    }

    fun stopSensors() {
        sensorManager.stopListening()
    }
}
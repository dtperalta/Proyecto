package com.example.amls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amls.auth.TokenManager
import com.example.amls.data.PerfilAprendiz
import com.example.amls.data.RecursoEducativo
import com.example.amls.ml.DecisionAdaptacion
import com.example.amls.ml.LocalAdaptationEngine
import com.example.amls.ml.VideoCacheManager
import com.example.amls.network.AmlsApiService
import com.example.amls.network.HistorialCreateRequest
import com.example.amls.network.RecomendacionRequest
import com.example.amls.repository.SincronizacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val repository: SincronizacionRepository,
    private val apiService: AmlsApiService,
    private val localAdaptationEngine: LocalAdaptationEngine,
    @get:androidx.media3.common.util.UnstableApi val videoCacheManager: VideoCacheManager
) : ViewModel() {

    // Extraemos los datos LOCALES (Offline-First) y los convertimos en un StateFlow
    // para que la interfaz gráfica (Jetpack Compose) reaccione automáticamente si cambian.
    val recursos: StateFlow<List<RecursoEducativo>> = repository.recursosLocales
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.sincronizarRecursos()
        }
    }

    /**
     * Registra un evento de interacción del estudiante (UC-10, QA-8).
     * Es "best-effort": si no hay red, simplemente no se registra esta
     * vez (no bloquea ni afecta la experiencia de reproducción).
     *
     * Nota pendiente: para cumplir QA-8 al 100% ("sin pérdida de datos
     * ante desconexiones"), lo ideal sería encolar estos eventos en Room
     * cuando falla la red y reintentarlos después. Por ahora, en caso
     * de fallo, el evento simplemente se pierde — es una mejora futura,
     * no bloquea el resto del sistema.
     */
    fun registrarEvento(recursoId: String, tipoEvento: String) {
        viewModelScope.launch {
            try {
                apiService.registrarEvento(
                    HistorialCreateRequest(
                        recurso_id = recursoId,
                        tipo_evento = tipoEvento,
                        metadata_extra = null
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun obtenerUrlSubtitulos(recursoId: String): String? {
        return try {
            apiService.listarArchivosDeRecurso(recursoId)
                .lastOrNull { it.tipo_archivo == "subtitulos" }
                ?.url
        } catch (e: Exception) {
            null
        }
    }

    fun predecirAdaptacion(
        nivelLuz: Float,
        nivelMovimiento: Float,
        altoContrasteBase: Boolean,
        tamanoFuenteBase: Float
    ): DecisionAdaptacion {
        return localAdaptationEngine.predecir(nivelLuz, nivelMovimiento, altoContrasteBase, tamanoFuenteBase)
    }

    suspend fun obtenerNivelRecomendadoReal(perfil: PerfilAprendiz): String? {
        return try {
            val respuestaQuiz = apiService.obtenerResultadoQuiz()
            if (!respuestaQuiz.isSuccessful || respuestaQuiz.body() == null) return null
            val resultado = respuestaQuiz.body()!!

            apiService.recomendar(
                RecomendacionRequest(
                    nivel_lectura = perfil.nivelLectura,
                    porcentaje_acierto_quiz = (resultado.total_correctas.toFloat() / resultado.total_preguntas * 100),
                    cantidad_lecciones_dominadas = resultado.recursos_dominados.size
                )
            ).nivel_dificultad_recomendado
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerRecursosDominados(): List<String> {
        return try {
            val respuesta = apiService.obtenerResultadoQuiz()
            if (respuesta.isSuccessful) respuesta.body()?.recursos_dominados ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

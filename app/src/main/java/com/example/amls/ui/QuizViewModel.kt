package com.example.amls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amls.network.AmlsApiService
import com.example.amls.network.EnviarQuizRequest
import com.example.amls.network.PreguntaQuizDto
import com.example.amls.network.RespuestaQuizItemDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuizUiState {
    object Cargando : QuizUiState()
    data class Listo(val preguntas: List<PreguntaQuizDto>) : QuizUiState()
    object Enviando : QuizUiState()
    data class Enviado(val recursosDominados: List<String>, val correctas: Int, val total: Int) : QuizUiState()
    data class Error(val mensaje: String) : QuizUiState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val apiService: AmlsApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Cargando)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // pregunta_id -> índice seleccionado
    private val _respuestas = MutableStateFlow<Map<String, Int>>(emptyMap())
    val respuestas: StateFlow<Map<String, Int>> = _respuestas.asStateFlow()

    init {
        cargarQuiz()
    }

    fun cargarQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Cargando
            try {
                val preguntas = apiService.obtenerQuiz()
                _uiState.value = QuizUiState.Listo(preguntas)
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error("No se pudo cargar el quiz. Revisa tu conexión.")
            }
        }
    }

    fun reintentar() = cargarQuiz()

    fun seleccionarRespuesta(preguntaId: String, indice: Int) {
        _respuestas.value = _respuestas.value + (preguntaId to indice)
    }

    fun todasRespondidas(totalPreguntas: Int): Boolean {
        return _respuestas.value.size == totalPreguntas
    }

    fun enviarRespuestas() {
        // Bloquea reenvíos: si ya se está procesando o ya se completó,
        // ignora cualquier clic adicional que llegue mientras tanto.
        if (_uiState.value !is QuizUiState.Listo) return

        _uiState.value = QuizUiState.Enviando

        viewModelScope.launch {
            try {
                val payload = EnviarQuizRequest(
                    respuestas = _respuestas.value.map { (preguntaId, indice) ->
                        RespuestaQuizItemDto(preguntaId, indice)
                    }
                )
                val resultado = apiService.enviarQuiz(payload)
                _uiState.value = QuizUiState.Enviado(
                    resultado.recursos_dominados,
                    resultado.total_correctas,
                    resultado.total_preguntas
                )
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error("No se pudo enviar el quiz. Intenta de nuevo.")
            }
        }
    }
}

package com.example.amls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amls.auth.TokenManager
import com.example.amls.network.AmlsApiService
import com.example.amls.ui.navigation.DestinoAmls
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: AmlsApiService
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        determinarDestinoInicial()
    }

    private fun determinarDestinoInicial() {
        viewModelScope.launch {
            if (!tokenManager.haySesionActiva()) {
                _startDestination.value = DestinoAmls.Login.ruta
                return@launch
            }
            try {
                val usuario = apiService.obtenerUsuarioActual()
                if (!usuario.email_verificado) {
                    _startDestination.value = DestinoAmls.VerificarEmail.ruta
                    return@launch
                }

                val respuestaQuiz = apiService.obtenerResultadoQuiz()
                val quizCompleto = respuestaQuiz.isSuccessful && respuestaQuiz.body() != null

                _startDestination.value = if (quizCompleto) {
                    DestinoAmls.Inicio.ruta
                } else {
                    DestinoAmls.Quiz.ruta
                }
            } catch (e: Exception) {
                // Sin conexión real (no relacionado al quiz): prioriza
                // disponibilidad, igual que antes.
                _startDestination.value = DestinoAmls.Inicio.ruta
            }
        }
    }
}

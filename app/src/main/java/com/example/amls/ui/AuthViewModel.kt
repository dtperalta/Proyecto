package com.example.amls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amls.auth.TokenManager
import com.example.amls.network.AmlsApiService
import com.example.amls.network.LoginRequest
import com.example.amls.network.RegisterRequest
import com.example.amls.network.RestablecerPasswordRequest
import com.example.amls.network.SolicitarRecuperacionRequest
import com.example.amls.network.VerificarEmailRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Cargando : AuthUiState()
    data class Exito(val emailVerificado: Boolean = true) : AuthUiState()
    data class Error(val mensaje: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: AmlsApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun registrar(nombreCompleto: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                apiService.registrar(RegisterRequest(nombreCompleto, email, password))
                // Registro exitoso: iniciamos sesión automáticamente
                iniciarSesion(email, password)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapearError(e))
            }
        }
    }

    fun iniciarSesion(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                val respuesta = apiService.iniciarSesion(LoginRequest(email, password))
                tokenManager.guardarToken(respuesta.access_token)
                _uiState.value = AuthUiState.Exito(respuesta.email_verificado)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapearError(e))
            }
        }
    }

    fun verificarEmail(codigo: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                apiService.verificarEmail(VerificarEmailRequest(codigo))
                _uiState.value = AuthUiState.Exito()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapearErrorCodigo(e))
            }
        }
    }

    fun reenviarCodigoVerificacion() {
        viewModelScope.launch {
            try {
                apiService.reenviarVerificacion()
            } catch (e: Exception) {
                // Best-effort: si falla, el usuario puede intentar de nuevo
            }
        }
    }

    fun solicitarRecuperacion(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                apiService.solicitarRecuperacion(SolicitarRecuperacionRequest(email))
                _uiState.value = AuthUiState.Exito()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error de conexión. Intenta de nuevo.")
            }
        }
    }

    fun restablecerPassword(email: String, codigo: String, nuevaPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            try {
                apiService.restablecerPassword(
                    RestablecerPasswordRequest(email, codigo, nuevaPassword)
                )
                _uiState.value = AuthUiState.Exito()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapearErrorCodigo(e))
            }
        }
    }

    fun reiniciarEstado() {
        _uiState.value = AuthUiState.Idle
    }

    fun cerrarSesion() {
        tokenManager.limpiarToken()
        _uiState.value = AuthUiState.Idle
    }

    suspend fun quizCompletado(): Boolean {
        return try {
            val respuesta = apiService.obtenerResultadoQuiz()
            respuesta.isSuccessful && respuesta.body() != null
        } catch (e: Exception) {
            false
        }
    }

    private fun mapearError(e: Exception): String {
        return when {
            e.message?.contains("401") == true -> "Email o contraseña incorrectos"
            e.message?.contains("400") == true -> "Ya existe una cuenta con ese email"
            else -> "Error de conexión. Intenta de nuevo."
        }
    }

    private fun mapearErrorCodigo(e: Exception): String {
        return if (e.message?.contains("400") == true) {
            "Código inválido o expirado"
        } else {
            "Error de conexión. Intenta de nuevo."
        }
    }
}

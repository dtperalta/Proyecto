package com.example.amls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amls.repository.SincronizacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sincronizacionRepository: SincronizacionRepository
) : ViewModel() {

    fun iniciarSesion() {
        viewModelScope.launch {
            // Aquí iría tu validación de credenciales local...
            
            // Descargamos el tema 1 en segundo plano
            sincronizacionRepository.descargarTema("TEMA-1")
        }
    }
}

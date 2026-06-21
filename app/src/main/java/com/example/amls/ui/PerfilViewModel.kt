package com.example.amls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amls.data.PerfilAprendiz
import com.example.amls.data.PerfilAprendizDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val dao: PerfilAprendizDao
) : ViewModel() {

    // 1. Estado "Vivo" de la Base de Datos (Para PlaybackScreen y otras pantallas)
    val perfilReal: StateFlow<PerfilAprendiz?> = dao.obtenerPerfil()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 2. Estado "Temporal" para la pantalla de Ajustes (Para editar antes de guardar)
    private val _perfilEditable = MutableStateFlow<PerfilAprendiz?>(null)
    val perfilEditable = _perfilEditable.asStateFlow()

    init {
        prepararEdicion()
    }

    private fun prepararEdicion() {
        viewModelScope.launch {
            // Tomamos el primer valor que tenga la DB o creamos uno por defecto
            val actual = dao.obtenerPerfil().first() ?: PerfilAprendiz(
                id = 1,
                gradoPerdidaAuditiva = "Leve",
                preferenciaComunicativa = "Subtítulos",
                nivelLectura = "Básico",
                requiereAltoContraste = false,
                tamanoSubtitulos = 18
            )
            _perfilEditable.value = actual
        }
    }

    // Usado por SettingsScreen para guardar definitivamente
    fun guardarPerfil(perfil: PerfilAprendiz) {
        viewModelScope.launch {
            dao.insertarPerfil(perfil)
            // Al insertar, el Flow 'perfilReal' se actualizará automáticamente
        }
    }

    // Métodos para actualizar el estado temporal (UI)
    fun actualizarGradoPerdida(grado: String) {
        _perfilEditable.value = _perfilEditable.value?.copy(gradoPerdidaAuditiva = grado)
    }

    fun actualizarPreferencia(preferencia: String) {
        _perfilEditable.value = _perfilEditable.value?.copy(preferenciaComunicativa = preferencia)
    }

    fun actualizarNivelLectura(nivel: String) {
        _perfilEditable.value = _perfilEditable.value?.copy(nivelLectura = nivel)
    }

    fun actualizarContraste(requiere: Boolean) {
        _perfilEditable.value = _perfilEditable.value?.copy(requiereAltoContraste = requiere)
    }

    fun actualizarTamanoSubtitulos(tamano: Int) {
        _perfilEditable.value = _perfilEditable.value?.copy(tamanoSubtitulos = tamano)
    }
}

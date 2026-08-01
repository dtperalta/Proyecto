package com.example.amls.ui.theme

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferenceManager: ThemePreferenceManager
) : ViewModel() {

    private val _modoTema = MutableStateFlow(preferenceManager.obtenerModo())
    val modoTema: StateFlow<ModoTema> = _modoTema.asStateFlow()

    fun cambiarModo(modo: ModoTema) {
        preferenceManager.guardarModo(modo)
        _modoTema.value = modo
    }
}

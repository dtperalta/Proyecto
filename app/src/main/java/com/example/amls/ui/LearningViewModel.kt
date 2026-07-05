package com.example.amls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amls.data.RecursoEducativo
import com.example.amls.repository.SincronizacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    repository: SincronizacionRepository
) : ViewModel() {

    // Extraemos los datos LOCALES (Offline-First) y los convertimos en un StateFlow
    // para que la interfaz gráfica (Jetpack Compose) reaccione automáticamente si cambian.
    val recursos: StateFlow<List<RecursoEducativo>> = repository.recursosLocales
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

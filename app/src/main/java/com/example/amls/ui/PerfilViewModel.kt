package com.example.amls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amls.auth.TokenManager
import com.example.amls.data.PerfilAprendiz
import com.example.amls.data.PerfilAprendizDao
import com.example.amls.network.AmlsApiService
import com.example.amls.network.PerfilCreateRequest
import com.example.amls.network.PerfilDto
import com.example.amls.network.PerfilUpdateRequest
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
    private val dao: PerfilAprendizDao,
    private val apiService: AmlsApiService
) : ViewModel() {

    // Estado "Vivo" desde Room (para PlaybackScreen y otras pantallas,
    // sigue siendo la fuente offline-first, QA-7)
    val perfilReal: StateFlow<PerfilAprendiz?> = dao.obtenerPerfil()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Estado "Temporal" para la pantalla de Ajustes
    private val _perfilEditable = MutableStateFlow<PerfilAprendiz?>(null)
    val perfilEditable = _perfilEditable.asStateFlow()

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando = _sincronizando.asStateFlow()

    init {
        sincronizarConBackend()
    }

    /**
     * Al iniciar, intenta traer el perfil real del backend.
     * - Si existe en el backend: lo guarda en Room (se vuelve la fuente local).
     * - Si NO existe en el backend (usuario nuevo, recién registrado):
     *   crea uno con valores por defecto, tanto en el backend como en Room.
     * - Si falla la red: usa lo que ya haya en Room (offline-first, QA-7).
     */
    private fun sincronizarConBackend() {
        viewModelScope.launch {
            _sincronizando.value = true

            try {
                val perfilRemoto = apiService.obtenerPerfil()
                guardarLocal(perfilRemoto)
            } catch (e: Exception) {
                // 404 probablemente = perfil aún no existe en el backend
                try {
                    val nuevo = PerfilCreateRequest(
                        grado_perdida_auditiva = "Leve",
                        preferencia_comunicativa = "Subtítulos",
                        nivel_lectura = "Básico",
                        requiere_alto_contraste = false,
                        tamano_subtitulos = 18
                    )
                    val creado = apiService.crearPerfil(nuevo)
                    guardarLocal(creado)
                } catch (e2: Exception) {
                    // Sin conexión: usa lo que haya en Room, si algo
                    prepararEdicionSoloLocal()
                }
            }
            _sincronizando.value = false
        }
    }

    private suspend fun guardarLocal(dto: PerfilDto) {
        val perfil = PerfilAprendiz(
            id = 1,
            gradoPerdidaAuditiva = dto.grado_perdida_auditiva,
            preferenciaComunicativa = dto.preferencia_comunicativa,
            nivelLectura = dto.nivel_lectura,
            requiereAltoContraste = dto.requiere_alto_contraste,
            tamanoSubtitulos = dto.tamano_subtitulos
        )
        dao.insertarPerfil(perfil)
        _perfilEditable.value = perfil
    }

    private suspend fun prepararEdicionSoloLocal() {
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

    /**
     * Guarda el perfil: primero local (respuesta inmediata en UI),
     * luego intenta sincronizar con el backend (best-effort).
     */
    fun guardarPerfil(perfil: PerfilAprendiz) {
        viewModelScope.launch {
            dao.insertarPerfil(perfil)

            try {
                apiService.actualizarPerfil(
                    PerfilUpdateRequest(
                        grado_perdida_auditiva = perfil.gradoPerdidaAuditiva,
                        preferencia_comunicativa = perfil.preferenciaComunicativa,
                        nivel_lectura = perfil.nivelLectura,
                        requiere_alto_contraste = perfil.requiereAltoContraste,
                        tamano_subtitulos = perfil.tamanoSubtitulos
                    )
                )
            } catch (e: Exception) {
                // Sin conexión: el cambio ya quedó en Room (QA-7/QA-8).
            }
        }
    }

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

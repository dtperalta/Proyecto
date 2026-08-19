package com.example.amls.repository

import com.example.amls.data.RecursoEducativo
import com.example.amls.data.RecursoEducativoDao
import com.example.amls.network.AmlsApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SincronizacionRepository @Inject constructor(
    private val apiService: AmlsApiService,
    private val recursoDao: RecursoEducativoDao
) {
    // Exponemos los datos locales siempre (Offline-First, QA-7)
    val recursosLocales = recursoDao.obtenerRecursosLocales()

    /**
     * Descarga todos los recursos educativos disponibles en el backend
     * y los guarda en Room. Se llama al iniciar sesión (ver
     * LearningViewModel), y como es "best-effort", si falla por falta
     * de red, la UI simplemente sigue leyendo de recursosLocales.
     */
    suspend fun sincronizarRecursos() {
        try {
            val recursosDto = apiService.listarRecursos()

            val recursosEntity = recursosDto.map { dto ->
                // Descarga el contenido del .srt AHORA (con conexión
                // garantizada, ya que estamos sincronizando), para que
                // la transcripción con resaltado funcione sin depender
                // de la red al momento de reproducir (QA-7).
                val contenidoSrt = dto.url_subtitulos?.let { url ->
                    try {
                        val respuesta = apiService.descargarArchivoTexto(url)
                        if (respuesta.isSuccessful) respuesta.body()?.string() else null
                    } catch (e: Exception) {
                        null // si falla, simplemente no hay resaltado para esta lección esta vez
                    }
                }

                RecursoEducativo(
                    id = dto.id,
                    titulo = dto.titulo,
                    tipo_formato = dto.tipo_formato,
                    url_descarga = dto.url_descarga ?: "",
                    tiene_lengua_senas = dto.tiene_lengua_senas,
                    nivel_dificultad = dto.nivel_dificultad,
                    transcripcion = dto.transcripcion,
                    subtitulosUrl = dto.url_subtitulos,
                    urlLenguaSenas = dto.url_lengua_senas,
                    contenidoSrtCache = contenidoSrt
                )
            }

            recursoDao.insertarRecursos(recursosEntity)
        } catch (e: Exception) {
            // Sin conexión: la UI sigue funcionando con lo que ya
            // hay en Room (QA-7, QA-8).
            e.printStackTrace()
        }
    }
}

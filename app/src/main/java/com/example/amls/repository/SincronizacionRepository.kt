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
    // Exponemos los datos locales siempre (Offline-First)
    val recursosLocales = recursoDao.obtenerRecursosLocales()

    // Función que conecta al servidor Python y guarda en Room
    suspend fun descargarTema(temaId: String) {
        try {
            // 1. Descargamos del Cloud Simulado
            val recursosDto = apiService.obtenerRecursos(temaId)
            
            // 2. Mapeamos la respuesta de la red al modelo de Room
            val recursosEntity = recursosDto.map { dto ->
                RecursoEducativo(
                    id = dto.id,
                    titulo = dto.titulo,
                    tipo_formato = dto.tipo_formato,
                    url_descarga = dto.url_descarga,
                    tiene_lengua_senas = dto.tiene_lengua_senas,
                    nivel_dificultad = dto.nivel_dificultad
                )
            }
            
            // 3. Guardamos localmente para cumplir el escenario QA-7
            recursoDao.insertarRecursos(recursosEntity)
            
        } catch (e: Exception) {
            // Si falla (ej. no hay internet), no pasa nada, 
            // la UI seguirá leyendo de 'recursosLocales'
            e.printStackTrace()
        }
    }
}

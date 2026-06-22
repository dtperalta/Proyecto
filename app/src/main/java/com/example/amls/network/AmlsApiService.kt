package com.example.amls.network

import com.example.amls.data.PerfilAprendiz
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Modelos que mapean el JSON de Python a Kotlin
data class RecursoEducativoDto(
    val id: String,
    val titulo: String,
    val tipo_formato: String,
    val url_descarga: String,
    val tiene_lengua_senas: Boolean,
    val nivel_dificultad: String
)

data class RecomendacionResponse(
    val status: String,
    val mensaje: String,
    val recursos_recomendados: List<String>
)

// Las rutas hacia nuestra Mock API
interface AmlsApiService {
    @GET("/api/recursos/tema/{tema_id}")
    suspend fun obtenerRecursos(@Path("tema_id") temaId: String): List<RecursoEducativoDto>

    @POST("/api/recomendacion/ruta")
    suspend fun generarRutaAprendizaje(@Body perfil: PerfilAprendiz): RecomendacionResponse
}
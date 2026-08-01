package com.example.amls.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path

// ============================================
// AUTH
// ============================================
data class RegisterRequest(
    val nombre_completo: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UsuarioDto(
    val id: String,
    val nombre_completo: String,
    val email: String,
    val email_verificado: Boolean
)

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val email_verificado: Boolean
)

// ============================================
// PERFIL
// ============================================
data class PerfilCreateRequest(
    val grado_perdida_auditiva: String,
    val preferencia_comunicativa: String,
    val nivel_lectura: String,
    val requiere_alto_contraste: Boolean,
    val tamano_subtitulos: Int
)

data class PerfilUpdateRequest(
    val grado_perdida_auditiva: String,
    val preferencia_comunicativa: String,
    val nivel_lectura: String,
    val requiere_alto_contraste: Boolean,
    val tamano_subtitulos: Int
)

data class PerfilDto(
    val id: String,
    val user_id: String,
    val grado_perdida_auditiva: String,
    val preferencia_comunicativa: String,
    val nivel_lectura: String,
    val requiere_alto_contraste: Boolean,
    val tamano_subtitulos: Int
)

// ============================================
// HISTORIAL
// ============================================
data class HistorialCreateRequest(
    val recurso_id: String?,
    val tipo_evento: String,
    val metadata_extra: Map<String, Any>?
)

data class HistorialDto(
    val id: String,
    val user_id: String,
    val recurso_id: String?,
    val tipo_evento: String,
    val metadata_extra: Map<String, Any>?,
    val created_at: String
)

// ============================================
// CONTENIDO
// ============================================
data class RecursoDto(
    val id: String,
    val titulo: String,
    val tipo_formato: String,
    val url_descarga: String?,
    val tiene_lengua_senas: Boolean,
    val nivel_dificultad: String,
    val transcripcion: String?,
    val url_subtitulos: String?,
    val url_lengua_senas: String?
)

data class ArchivoRecursoDto(
    val id: String,
    val recurso_id: String,
    val tipo_archivo: String,
    val url: String
)

// ============================================
// ML RECOMMENDER
// ============================================
data class RecomendacionRequest(
    val nivel_lectura: String,
    val porcentaje_acierto_quiz: Float,
    val cantidad_lecciones_dominadas: Int
)

data class RecomendacionResponse(
    val nivel_dificultad_recomendado: String
)

// ============================================
// QUIZ
// ============================================
data class PreguntaQuizDto(
    val id: String,
    val recurso_id: String,
    val enunciado: String,
    val opciones: List<String>
)

data class RespuestaQuizItemDto(
    val pregunta_id: String,
    val indice_seleccionado: Int
)

data class EnviarQuizRequest(
    val respuestas: List<RespuestaQuizItemDto>
)

data class ResultadoQuizDto(
    val recursos_dominados: List<String>,
    val total_correctas: Int,
    val total_preguntas: Int
)

// ============================================
// VERIFICACIÓN Y RECUPERACIÓN
// ============================================
data class VerificarEmailRequest(val codigo: String)
data class SolicitarRecuperacionRequest(val email: String)
data class RestablecerPasswordRequest(
    val email: String,
    val codigo: String,
    val nueva_password: String
)
data class MensajeResponse(val mensaje: String)

// ============================================
// SERVICIO
// ============================================
interface AmlsApiService {

    // --- Auth ---
    @POST("/auth/register")
    suspend fun registrar(@Body datos: RegisterRequest): UsuarioDto

    @POST("/auth/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): TokenResponse

    @GET("/auth/me")
    suspend fun obtenerUsuarioActual(): UsuarioDto

    // --- Verificación y Recuperación ---
    @POST("/auth/verificar-email")
    suspend fun verificarEmail(@Body datos: VerificarEmailRequest): MensajeResponse

    @POST("/auth/reenviar-verificacion")
    suspend fun reenviarVerificacion(): MensajeResponse

    @POST("/auth/solicitar-recuperacion")
    suspend fun solicitarRecuperacion(@Body datos: SolicitarRecuperacionRequest): MensajeResponse

    @POST("/auth/restablecer-password")
    suspend fun restablecerPassword(@Body datos: RestablecerPasswordRequest): MensajeResponse

    // --- Perfil ---
    @POST("/profile/")
    suspend fun crearPerfil(@Body datos: PerfilCreateRequest): PerfilDto

    @GET("/profile/")
    suspend fun obtenerPerfil(): PerfilDto

    @PUT("/profile/")
    suspend fun actualizarPerfil(
        @Body datos: PerfilUpdateRequest
    ): PerfilDto

    // --- Historial ---
    @POST("/profile/historial/")
    suspend fun registrarEvento(@Body datos: HistorialCreateRequest): HistorialDto

    @GET("/profile/historial/")
    suspend fun obtenerHistorial(): List<HistorialDto>

    // --- Contenido ---
    @GET("/content/")
    suspend fun listarRecursos(): List<RecursoDto>

    @GET("/content/{recursoId}")
    suspend fun obtenerRecurso(@Path("recursoId") recursoId: String): RecursoDto

    @GET("/content/archivos/{recursoId}")
    suspend fun listarArchivosDeRecurso(@Path("recursoId") recursoId: String): List<ArchivoRecursoDto>

    // --- ML Recommender ---
    @POST("/ml/recomendar")
    suspend fun recomendar(@Body datos: RecomendacionRequest): RecomendacionResponse

    // --- Quiz ---
    @GET("/quiz/")
    suspend fun obtenerQuiz(): List<PreguntaQuizDto>

    @POST("/quiz/enviar")
    suspend fun enviarQuiz(@Body datos: EnviarQuizRequest): ResultadoQuizDto

    @GET("/quiz/resultado")
    suspend fun obtenerResultadoQuiz(): retrofit2.Response<ResultadoQuizDto>
}

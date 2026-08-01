package com.example.amls.auth

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("amls_auth", Context.MODE_PRIVATE)

    fun guardarToken(token: String) {
        prefs.edit { putString("access_token", token) }
    }

    fun obtenerToken(): String? = prefs.getString("access_token", null)

    fun limpiarToken() {
        prefs.edit { remove("access_token") }
    }

    /**
     * Extrae el user_id (campo "sub") directamente del JWT, sin
     * necesidad de un endpoint extra para consultarlo. No se verifica
     * la firma aquí porque solo se lee el token que el propio backend
     * ya emitió y validó al hacer login/registro.
     */
    fun obtenerUserId(): String? {
        val token = obtenerToken() ?: return null
        return try {
            val payload = token.split(".")[1]
            val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            JSONObject(String(decoded)).getString("sub")
        } catch (e: Exception) {
            null
        }
    }

    fun haySesionActiva(): Boolean = obtenerToken() != null
}

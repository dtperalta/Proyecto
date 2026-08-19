package com.example.amls.ml

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoProgressManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("amls_video_progress", Context.MODE_PRIVATE)

    fun guardarPosicion(recursoId: String, posicionMs: Long) {
        prefs.edit { putLong("posicion_$recursoId", posicionMs) }
    }

    fun obtenerPosicion(recursoId: String): Long {
        return prefs.getLong("posicion_$recursoId", 0L)
    }

    fun limpiarPosicion(recursoId: String) {
        prefs.edit { remove("posicion_$recursoId") }
    }

    fun guardarDecisionSenas(decisionForzar: Boolean) {
        prefs.edit { putBoolean("decision_senas_forzada", decisionForzar) }
    }

    fun yaTomoDecisionSenas(): Boolean {
        return prefs.contains("decision_senas_forzada")
    }

    fun obtenerDecisionSenas(): Boolean {
        return prefs.getBoolean("decision_senas_forzada", false)
    }

    fun limpiarDecisionSenas() {
        prefs.edit { remove("decision_senas_forzada") }
    }
}

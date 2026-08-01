package com.example.amls.ui.theme

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class ModoTema { CLARO, OSCURO, SISTEMA }

@Singleton
class ThemePreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("amls_theme", Context.MODE_PRIVATE)

    fun obtenerModo(): ModoTema {
        val valor = prefs.getString("modo_tema", ModoTema.SISTEMA.name)
        return try {
            ModoTema.valueOf(valor ?: ModoTema.SISTEMA.name)
        } catch (e: Exception) {
            ModoTema.SISTEMA
        }
    }

    fun guardarModo(modo: ModoTema) {
        prefs.edit { putString("modo_tema", modo.name) }
    }
}

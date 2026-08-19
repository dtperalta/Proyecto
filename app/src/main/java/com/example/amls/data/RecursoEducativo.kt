package com.example.amls.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recursos_educativos")
data class RecursoEducativo(
    @PrimaryKey val id: String,
    val titulo: String,
    val tipo_formato: String,
    val url_descarga: String,
    val tiene_lengua_senas: Boolean,
    val nivel_dificultad: String,
    val transcripcion: String? = null,
    val subtitulosUrl: String? = null,
    val urlLenguaSenas: String? = null,
    val contenidoSrtCache: String? = null
)
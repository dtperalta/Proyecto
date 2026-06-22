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
    val nivel_dificultad: String
)
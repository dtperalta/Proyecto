package com.example.amls.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfil_aprendiz")
data class PerfilAprendiz(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Identificador único automático

    val gradoPerdidaAuditiva: String, // Ej: "Leve", "Moderada", "Profunda"
    val preferenciaComunicativa: String, // Ej: "Lengua de Señas", "Subtítulos"
    val nivelLectura: String, // Ej: "Básico", "Avanzado"
    val requiereAltoContraste: Boolean, // true o false
    val tamanoSubtitulos: Int // Tamaño de la letra en píxeles (Ej: 16, 24)
)
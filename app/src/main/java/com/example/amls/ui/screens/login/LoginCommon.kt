package com.example.amls.ui.screens.login

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de marca — Identidad visual (fijos)
val AzulPrincipal = Color(0xFF005179)
val VerdeAzulado = Color(0xFF009383)
val FucsiaAcento = Color(0xFFEB5A67)

// Colores conscientes del tema
val GrisTexto: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

val FondoSuave: Color
    @Composable get() = MaterialTheme.colorScheme.background

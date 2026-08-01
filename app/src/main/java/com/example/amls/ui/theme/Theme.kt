package com.example.amls.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = AzulPrincipal,
    secondary = VerdeAzulado,
    tertiary = FucsiaAcento,
    background = FondoClaro,
    surface = SuperficieClara,
    onBackground = TextoPrincipalClaro,
    onSurface = TextoPrincipalClaro,
    onSurfaceVariant = TextoSecundarioClaro
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulPrincipal,
    secondary = VerdeAzulado,
    tertiary = FucsiaAcento,
    background = FondoOscuro,
    surface = SuperficieOscura,
    onBackground = TextoPrincipalOscuro,
    onSurface = TextoPrincipalOscuro,
    onSurfaceVariant = TextoSecundarioOscuro
)

@Composable
fun AmlsTheme(
    modoTema: ModoTema,
    content: @Composable () -> Unit
) {
    val esOscuro = when (modoTema) {
        ModoTema.CLARO -> false
        ModoTema.OSCURO -> true
        ModoTema.SISTEMA -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (esOscuro) EsquemaOscuro else EsquemaClaro,
        content = content
    )
}

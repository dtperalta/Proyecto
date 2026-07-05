package com.example.amls.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.amls.ui.LearningViewModel
import com.example.amls.ui.PerfilViewModel
import com.example.amls.ui.navigation.DestinoAmls

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel = hiltViewModel(),
    learningViewModel: LearningViewModel = hiltViewModel()
) {
    // Escuchamos el perfil REAL de la base de datos (reactivo)
    val perfil by perfilViewModel.perfilReal.collectAsState()
    
    // Escuchamos los recursos educativos descargados
    val recursos by learningViewModel.recursos.collectAsState()
    
    // Para este prototipo, tomaremos el primer recurso descargado (si existe)
    val leccionActual = recursos.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = leccionActual?.titulo ?: "Cargando lección...", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { navController.navigate(DestinoAmls.ConfiguracionAccesibilidad.ruta) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración de Accesibilidad")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF005179),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Reproductor Multimedia Simulado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                )

                // Componente de Subtítulos Adaptables usando datos del ViewModel
                SubtitulosAdaptables(
                    texto = "Este es un ejemplo de cómo los subtítulos cambian según tus ajustes.",
                    tamanoFuente = perfil?.tamanoSubtitulos ?: 18,
                    altoContraste = perfil?.requiereAltoContraste ?: false,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mostramos información dinámica de la base de datos
            if (leccionActual != null) {
                Text(
                    text = "Nivel de Dificultad: ${leccionActual.nivel_dificultad}",
                    color = Color(0xFF005179),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Formato sugerido: ${leccionActual.tipo_formato.replace("_", " ").uppercase()}",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "Información del Curso",
                    color = Color(0xFF005179),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "En esta sección se muestra el contenido educativo. Solo los subtítulos dentro del reproductor se verán afectados por los ajustes de accesibilidad de fuente y contraste.",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { navController.navigate(DestinoAmls.MonitorSensores.ruta) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Monitor de Entorno (Context-Awareness)", color = Color(0xFF005179))
            }
        }
    }
}

@Composable
fun SubtitulosAdaptables(
    texto: String,
    tamanoFuente: Int,
    altoContraste: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (altoContraste) Color.Yellow else Color.Black.copy(alpha = 0.6f)
    val textColor = if (altoContraste) Color.Black else Color.White

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .background(color = bgColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = texto,
            color = textColor,
            fontSize = tamanoFuente.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (altoContraste) FontWeight.Bold else FontWeight.Normal
        )
    }
}

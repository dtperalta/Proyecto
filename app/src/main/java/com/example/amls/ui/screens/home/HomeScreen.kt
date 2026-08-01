package com.example.amls.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.amls.data.RecursoEducativo
import com.example.amls.ui.LearningViewModel
import com.example.amls.ui.PerfilViewModel
import com.example.amls.ui.navigation.DestinoAmls

private val AZUL_PRINCIPAL = Color(0xFF005179)
private val FUCSIA_ACENTO = Color(0xFFEB5A67)
private val VERDE_AZULADO = Color(0xFF009383)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    learningViewModel: LearningViewModel = hiltViewModel(),
    perfilViewModel: PerfilViewModel = hiltViewModel()
) {
    val recursos by learningViewModel.recursos.collectAsState()
    val perfil by perfilViewModel.perfilReal.collectAsState()
    var nivelRecomendado by remember { mutableStateOf<String?>(null) }
    var recursosDominados by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(recursos, perfil) {
        recursosDominados = learningViewModel.obtenerRecursosDominados()
        perfil?.let {
            nivelRecomendado = learningViewModel.obtenerNivelRecomendadoReal(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Lecciones", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = { navController.navigate(DestinoAmls.ConfiguracionAccesibilidad.ruta) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AZUL_PRINCIPAL)
            )
        }
    ) { padding ->
        if (recursos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Todavía no hay lecciones disponibles.\nIntenta más tarde o revisa tu conexión.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                nivelRecomendado?.let { nivel ->
                    item {
                        BannerRecomendacion(nivel)
                    }
                }

                items(recursos) { recurso ->
                    TarjetaLeccion(
                        recurso = recurso,
                        esRecomendada = recurso.nivel_dificultad == nivelRecomendado,
                        esDominada = recurso.id in recursosDominados,
                        onClick = {
                            navController.navigate(DestinoAmls.Reproduccion.crearRuta(recurso.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerRecomendacion(nivel: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FUCSIA_ACENTO.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = FUCSIA_ACENTO)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Recomendado para ti",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FUCSIA_ACENTO
                )
                Text(
                    "Según tu perfil, te sugerimos empezar por el nivel $nivel",
                    fontSize = 13.sp,
                    color = Color(0xFF667085)
                )
            }
        }
    }
}

@Composable
private fun TarjetaLeccion(
    recurso: RecursoEducativo,
    esRecomendada: Boolean,
    esDominada: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (esDominada) {
            androidx.compose.foundation.BorderStroke(2.dp, VERDE_AZULADO)
        } else if (esRecomendada) {
            androidx.compose.foundation.BorderStroke(2.dp, FUCSIA_ACENTO)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AZUL_PRINCIPAL.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = AZUL_PRINCIPAL
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(recurso.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Nivel: ${recurso.nivel_dificultad}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            if (esDominada) {
                Box(
                    modifier = Modifier
                        .background(VERDE_AZULADO.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Ya la dominas", color = VERDE_AZULADO, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else if (esRecomendada) {
                Box(
                    modifier = Modifier
                        .background(FUCSIA_ACENTO.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Sugerido", color = FUCSIA_ACENTO, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

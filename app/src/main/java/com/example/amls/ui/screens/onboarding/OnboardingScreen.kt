package com.example.amls.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.amls.ui.PerfilViewModel
import com.example.amls.ui.navigation.DestinoAmls

private val AZUL_PRINCIPAL = Color(0xFF005179)
private const val TOTAL_PASOS = 3

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    var pasoActual by remember { mutableIntStateOf(0) }
    val perfil by viewModel.perfilEditable.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            // Indicador de progreso (estilo "banco": puntos/barra arriba)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(TOTAL_PASOS) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(
                                color = if (index <= pasoActual) AZUL_PRINCIPAL else Color.LightGray,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (perfil == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            val p = perfil!!

            Column(modifier = Modifier.weight(1f)) {
                when (pasoActual) {
                    0 -> PasoGradoPerdida(
                        seleccionado = p.gradoPerdidaAuditiva,
                        onSeleccionar = { viewModel.actualizarGradoPerdida(it) }
                    )
                    1 -> PasoPreferenciaComunicativa(
                        seleccionado = p.preferenciaComunicativa,
                        onSeleccionar = { viewModel.actualizarPreferencia(it) }
                    )
                    2 -> PasoNivelLectura(
                        seleccionado = p.nivelLectura,
                        onSeleccionar = { viewModel.actualizarNivelLectura(it) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pasoActual > 0) {
                    OutlinedButton(onClick = { pasoActual-- }) {
                        Text("Atrás")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (pasoActual < TOTAL_PASOS - 1) {
                            pasoActual++
                        } else {
                            viewModel.guardarPerfil(p)
                            navController.navigate(DestinoAmls.Quiz.ruta) {
                                popUpTo(DestinoAmls.Onboarding.ruta) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AZUL_PRINCIPAL)
                ) {
                    Text(if (pasoActual < TOTAL_PASOS - 1) "Siguiente" else "Comenzar")
                }
            }
        }
    }
}

@Composable
private fun PasoGradoPerdida(seleccionado: String, onSeleccionar: (String) -> Unit) {
    Text("¿Cuál es tu grado de pérdida auditiva?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        "Esto nos ayuda a adaptar el contenido a tus necesidades específicas.",
        color = Color.Gray,
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(32.dp))

    listOf("Leve", "Moderada", "Profunda").forEach { opcion ->
        OpcionSeleccionable(
            texto = opcion,
            seleccionado = seleccionado == opcion,
            onClick = { onSeleccionar(opcion) }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun PasoPreferenciaComunicativa(seleccionado: String, onSeleccionar: (String) -> Unit) {
    Text("¿Cómo prefieres comunicarte?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        "Elige el formato con el que te sientas más cómodo aprendiendo.",
        color = Color.Gray,
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(32.dp))

    listOf("Lengua de Señas", "Subtítulos", "Mixto").forEach { opcion ->
        OpcionSeleccionable(
            texto = opcion,
            seleccionado = seleccionado == opcion,
            onClick = { onSeleccionar(opcion) }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun PasoNivelLectura(seleccionado: String, onSeleccionar: (String) -> Unit) {
    Text("¿Cuál es tu nivel de lectura?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        "Con esto ajustamos la complejidad de los subtítulos y textos de apoyo.",
        color = Color.Gray,
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(32.dp))

    listOf("Básico", "Intermedio", "Avanzado").forEach { opcion ->
        OpcionSeleccionable(
            texto = opcion,
            seleccionado = seleccionado == opcion,
            onClick = { onSeleccionar(opcion) }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun OpcionSeleccionable(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (seleccionado) AZUL_PRINCIPAL.copy(alpha = 0.1f) else Color.White)
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = if (seleccionado) AZUL_PRINCIPAL else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionado, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = AZUL_PRINCIPAL))
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto, fontSize = 16.sp, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal)
    }
}

package com.example.amls.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.amls.ui.QuizUiState
import com.example.amls.ui.QuizViewModel
import com.example.amls.ui.navigation.DestinoAmls

private val AZUL_PRINCIPAL = androidx.compose.ui.graphics.Color(0xFF005179)
private val VERDE_AZULADO = androidx.compose.ui.graphics.Color(0xFF009383)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavController,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val respuestas by viewModel.respuestas.collectAsState()

    fun irAInicio() {
        navController.navigate(DestinoAmls.Inicio.ruta) {
            popUpTo(DestinoAmls.Onboarding.ruta) { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Diagnóstico", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AZUL_PRINCIPAL,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    actionIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { padding ->
        when (val estado = uiState) {
            is QuizUiState.Cargando -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is QuizUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(estado.mensaje, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.reintentar() }) { Text("Reintentar") }
                    }
                }
            }

            is QuizUiState.Listo -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Text(
                        "Responde estas 12 preguntas para que adaptemos tu ruta de aprendizaje a lo que ya sabes.",
                        modifier = Modifier.padding(16.dp),
                        color = androidx.compose.ui.graphics.Color.Gray,
                        fontSize = 13.sp
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(estado.preguntas) { index, pregunta ->
                            TarjetaPregunta(
                                numero = index + 1,
                                pregunta = pregunta,
                                seleccionActual = respuestas[pregunta.id],
                                onSeleccionar = { indice -> viewModel.seleccionarRespuesta(pregunta.id, indice) }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }

                    Button(
                        onClick = { viewModel.enviarRespuestas() },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AZUL_PRINCIPAL),
                        enabled = respuestas.size == estado.preguntas.size
                    ) {
                        Text("Enviar respuestas", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            QuizUiState.Enviando -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Enviando tus respuestas...")
                    }
                }
            }

            is QuizUiState.Enviado -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            "¡Listo! Acertaste ${estado.correctas} de ${estado.total}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        if (estado.recursosDominados.isNotEmpty()) {
                            Text(
                                "Ya dominas ${estado.recursosDominados.size} lección(es) — te las marcaremos en tu catálogo.",
                                color = VERDE_AZULADO,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { irAInicio() }, colors = ButtonDefaults.buttonColors(containerColor = AZUL_PRINCIPAL)) {
                            Text("Ir a mis lecciones")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPregunta(
    numero: Int,
    pregunta: com.example.amls.network.PreguntaQuizDto,
    seleccionActual: Int?,
    onSeleccionar: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$numero. ${pregunta.enunciado}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(12.dp))
            pregunta.opciones.forEachIndexed { indice, opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (seleccionActual == indice) AZUL_PRINCIPAL.copy(alpha = 0.1f)
                            else androidx.compose.ui.graphics.Color.Transparent
                        )
                        .clickable { onSeleccionar(indice) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = seleccionActual == indice,
                        onClick = { onSeleccionar(indice) },
                        colors = RadioButtonDefaults.colors(selectedColor = AZUL_PRINCIPAL)
                    )
                    Text(opcion, fontSize = 14.sp)
                }
            }
        }
    }
}

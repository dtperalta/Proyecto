package com.example.amls.ui.screens.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.amls.ui.PerfilViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val perfil by viewModel.perfilEditable.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Ajustes de Perfil y Accesibilidad", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF005179))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            perfil?.let { p ->
                // 1. Grado de Pérdida Auditiva
                Text("Grado de Pérdida Auditiva", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                DropdownSelector(
                    options = listOf("Leve", "Moderada", "Profunda"),
                    selectedOption = p.gradoPerdidaAuditiva,
                    onOptionSelected = { viewModel.actualizarGradoPerdida(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Preferencia Comunicativa
                Text("Preferencia Comunicativa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                DropdownSelector(
                    options = listOf("Lengua de Señas", "Subtítulos", "Mixto"),
                    selectedOption = p.preferenciaComunicativa,
                    onOptionSelected = { viewModel.actualizarPreferencia(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Nivel de Lectura
                Text("Nivel de Lectura", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                DropdownSelector(
                    options = listOf("Básico", "Intermedio", "Avanzado"),
                    selectedOption = p.nivelLectura,
                    onOptionSelected = { viewModel.actualizarNivelLectura(it) }
                )

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(32.dp))

                // 4. Formato Visual
                Text("Adaptación de Formato", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Tamaño de Fuente: ${p.tamanoSubtitulos} px", fontSize = 16.sp)
                Slider(
                    value = p.tamanoSubtitulos.toFloat(),
                    onValueChange = { viewModel.actualizarTamanoSubtitulos(it.toInt()) },
                    valueRange = 12f..36f,
                    steps = 24
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Habilitar Alto Contraste", fontSize = 16.sp)
                    Switch(
                        checked = p.requiereAltoContraste,
                        onCheckedChange = { viewModel.actualizarContraste(it) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Previsualización
                Text("Previsualización", style = MaterialTheme.typography.labelLarge)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (p.requiereAltoContraste) Color.Yellow else Color.LightGray)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Texto de ejemplo adaptado",
                        fontSize = p.tamanoSubtitulos.sp,
                        color = if (p.requiereAltoContraste) Color.Black else Color.DarkGray,
                        fontWeight = if (p.requiereAltoContraste) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Botón Guardar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all=16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.guardarPerfil(p)
                            scope.launch {
                                snackbarHostState.showSnackbar("Configuraciones guardadas con éxito")
                            }
                        },
                        modifier = Modifier
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005179)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                    ) {
                        Text("GUARDAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

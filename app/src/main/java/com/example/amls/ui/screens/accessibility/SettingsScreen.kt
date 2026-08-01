package com.example.amls.ui.screens.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.amls.ui.AuthViewModel
import com.example.amls.ui.PerfilViewModel
import com.example.amls.ui.navigation.DestinoAmls
import com.example.amls.ui.theme.ModoTema
import com.example.amls.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: PerfilViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(LocalContext.current as androidx.activity.ComponentActivity)
) {
    val perfil by viewModel.perfilEditable.collectAsState()
    val modoTema by themeViewModel.modoTema.collectAsState()
    val scrollState = rememberScrollState()
    var mostrarDialogoLogout by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun cerrarSesion() {
        authViewModel.cerrarSesion()
        navController.navigate(DestinoAmls.Login.ruta) {
            popUpTo(0) { inclusive = true }
        }
    }

    if (mostrarDialogoLogout) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoLogout = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres cerrar tu sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoLogout = false
                    cerrarSesion()
                }) {
                    Text("Cerrar sesión", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoLogout = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes de Perfil y Accesibilidad", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarDialogoLogout = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
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
                Text("Grado de Pérdida Auditiva", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                DropdownSelector(
                    options = listOf("Leve", "Moderada", "Profunda"),
                    selectedOption = p.gradoPerdidaAuditiva,
                    onOptionSelected = { viewModel.actualizarGradoPerdida(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Preferencia Comunicativa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                DropdownSelector(
                    options = listOf("Lengua de Señas", "Subtítulos", "Mixto"),
                    selectedOption = p.preferenciaComunicativa,
                    onOptionSelected = { viewModel.actualizarPreferencia(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                Text("Apariencia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ModoTema.entries.forEachIndexed { index, modo ->
                        SegmentedButton(
                            selected = modoTema == modo,
                            onClick = { themeViewModel.cambiarModo(modo) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ModoTema.entries.size
                            )
                        ) {
                            Text(
                                when (modo) {
                                    ModoTema.CLARO -> "Claro"
                                    ModoTema.OSCURO -> "Oscuro"
                                    ModoTema.SISTEMA -> "Sistema"
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

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

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().padding(all = 16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.guardarPerfil(p)
                            Toast.makeText(context, "Configuración guardada con éxito", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        modifier = Modifier.height(56.dp),
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

package com.example.amls.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.amls.ui.AuthUiState
import com.example.amls.ui.AuthViewModel
import com.example.amls.ui.navigation.DestinoAmls

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var paso by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var errorValidacion by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val camposHabilitados = uiState !is AuthUiState.Cargando

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Exito) {
            if (paso == 0) {
                paso = 1
                viewModel.reiniciarEstado()
            } else {
                navController.navigate(DestinoAmls.Login.ruta) {
                    popUpTo(DestinoAmls.Login.ruta) { inclusive = true }
                }
            }
        }
    }

    fun intentarRestablecer() {
        errorValidacion = when {
            codigo.length != 6 -> "El código debe tener 6 dígitos"
            nuevaPassword.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            nuevaPassword != confirmarPassword -> "Las contraseñas no coinciden"
            else -> null
        }
        if (errorValidacion == null) viewModel.restablecerPassword(email, codigo, nuevaPassword)
    }

    Column(modifier = Modifier.fillMaxSize().background(FondoSuave)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        Column(modifier = Modifier.padding(horizontal = 28.dp)) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Brush.linearGradient(listOf(VerdeAzulado, AzulPrincipal)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("AMLS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
            EtiquetaContexto(if (paso == 0) "Recuperar acceso" else "Un paso más")
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (paso == 0) "¿Olvidaste tu contraseña?" else "Restablecer contraseña",
                fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                if (paso == 0) "Ingresa tu correo y te enviaremos un código para restablecerla."
                else "Revisa tu correo e ingresa el código junto con tu nueva contraseña.",
                fontSize = 15.sp, color = GrisTexto
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (paso == 0) {
                AmlsTextField(value = email, onValueChange = { email = it }, label = "Correo electrónico")

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.solicitarRecuperacion(email) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrincipal),
                    enabled = camposHabilitados && email.isNotBlank()
                ) {
                    if (uiState is AuthUiState.Cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                    } else {
                        Text("Enviar código", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                AmlsTextField(value = codigo, onValueChange = { if (it.length <= 6) codigo = it }, label = "Código de 6 dígitos")
                Spacer(modifier = Modifier.height(14.dp))
                AmlsTextField(value = nuevaPassword, onValueChange = { nuevaPassword = it }, label = "Nueva contraseña", esPassword = true)
                Spacer(modifier = Modifier.height(14.dp))
                AmlsTextField(value = confirmarPassword, onValueChange = { confirmarPassword = it }, label = "Confirmar nueva contraseña", esPassword = true)

                val mensajeError = errorValidacion ?: (uiState as? AuthUiState.Error)?.mensaje
                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(mensajeError, color = Color(0xFFDC2626), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { intentarRestablecer() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrincipal),
                    enabled = camposHabilitados
                ) {
                    if (uiState is AuthUiState.Cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                    } else {
                        Text("Restablecer contraseña", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

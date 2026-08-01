package com.example.amls.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.amls.ui.AuthUiState
import com.example.amls.ui.AuthViewModel
import com.example.amls.ui.clickableSinIndicacion
import com.example.amls.ui.navigation.DestinoAmls

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorValidacion by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val camposHabilitados = uiState !is AuthUiState.Cargando
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Exito) {
            navController.navigate(DestinoAmls.VerificarEmail.ruta) {
                popUpTo(DestinoAmls.Login.ruta) { inclusive = true }
            }
        }
    }

    fun intentarRegistrar() {
        errorValidacion = when {
            name.isBlank() -> "Ingresa tu nombre completo"
            email.isBlank() -> "Ingresa tu correo electrónico"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            password != confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
        if (errorValidacion == null) viewModel.registrar(name, email, password)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoSuave)
            .verticalScroll(scrollState)
    ) {
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
            EtiquetaContexto("Únete a AMLS")
            Spacer(modifier = Modifier.height(16.dp))

            Text("Crear cuenta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Regístrate para empezar tu aprendizaje", fontSize = 15.sp, color = GrisTexto)

            Spacer(modifier = Modifier.height(28.dp))

            AmlsTextField(value = name, onValueChange = { name = it }, label = "Nombre completo")
            Spacer(modifier = Modifier.height(14.dp))
            AmlsTextField(value = email, onValueChange = { email = it }, label = "Correo electrónico")
            Spacer(modifier = Modifier.height(14.dp))
            AmlsTextField(value = password, onValueChange = { password = it }, label = "Contraseña", esPassword = true)
            Spacer(modifier = Modifier.height(14.dp))
            AmlsTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirmar contraseña", esPassword = true)

            val mensajeError = errorValidacion ?: (uiState as? AuthUiState.Error)?.mensaje
            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(mensajeError, color = Color(0xFFDC2626), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { intentarRegistrar() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrincipal),
                enabled = camposHabilitados
            ) {
                if (uiState is AuthUiState.Cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                } else {
                    Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("¿Ya tienes cuenta? ", color = GrisTexto, fontSize = 14.sp)
                Text(
                    "Inicia sesión",
                    color = AzulPrincipal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickableSinIndicacion { navController.popBackStack() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

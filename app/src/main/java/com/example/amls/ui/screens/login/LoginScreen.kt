package com.example.amls.ui.screens.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.amls.ui.AuthUiState
import com.example.amls.ui.AuthViewModel
import com.example.amls.ui.clickableSinIndicacion
import com.example.amls.ui.navigation.DestinoAmls

@Composable
fun EncabezadoDecorativo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = VerdeAzulado.copy(alpha = 0.12f),
                radius = 120f,
                center = Offset(size.width * 0.15f, size.height * 0.25f)
            )
            drawCircle(
                color = FucsiaAcento.copy(alpha = 0.10f),
                radius = 90f,
                center = Offset(size.width * 0.85f, size.height * 0.15f)
            )
            drawCircle(
                color = AzulPrincipal.copy(alpha = 0.08f),
                radius = 150f,
                center = Offset(size.width * 0.7f, size.height * 0.7f)
            )
        }

        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(VerdeAzulado, AzulPrincipal))),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        "AMLS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .offset(x = 24.dp, y = (-24).dp)
                    .size(16.dp)
                    .background(FucsiaAcento, CircleShape)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun AmlsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    esPassword: Boolean = false
) {
    var mostrar by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        androidx.compose.material3.Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = GrisTexto,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (esPassword && !mostrar) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (esPassword) {
                {
                    TextButton(onClick = { mostrar = !mostrar }) {
                        androidx.compose.material3.Text(if (mostrar) "Ocultar" else "Mostrar", fontSize = 12.sp, color = AzulPrincipal)
                    }
                }
            } else null,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AzulPrincipal
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EtiquetaContexto(texto: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(FucsiaAcento.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        androidx.compose.material3.Text(texto, color = FucsiaAcento, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val camposHabilitados = uiState !is AuthUiState.Cargando

    LaunchedEffect(uiState) {
        val estado = uiState
        if (estado is AuthUiState.Exito) {
            val destino = when {
                !estado.emailVerificado -> DestinoAmls.VerificarEmail.ruta
                !viewModel.quizCompletado() -> DestinoAmls.Quiz.ruta
                else -> DestinoAmls.Inicio.ruta
            }
            navController.navigate(destino) {
                popUpTo(DestinoAmls.Login.ruta) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoSuave)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 28.dp)
        ) {
            EncabezadoDecorativo()

            Spacer(modifier = Modifier.height(8.dp))

            EtiquetaContexto("Aprendizaje adaptativo")

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Text(
                "Bienvenido de nuevo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            androidx.compose.material3.Text(
                "Inicia sesión para continuar tu aprendizaje",
                fontSize = 15.sp,
                color = GrisTexto
            )

            Spacer(modifier = Modifier.height(28.dp))

            AmlsTextField(value = email, onValueChange = { email = it }, label = "Correo electrónico")
            Spacer(modifier = Modifier.height(14.dp))
            AmlsTextField(value = password, onValueChange = { password = it }, label = "Contraseña", esPassword = true)

            if (uiState is AuthUiState.Error) {
                Spacer(modifier = Modifier.height(10.dp))
                androidx.compose.material3.Text((uiState as AuthUiState.Error).mensaje, color = Color(0xFFDC2626), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { navController.navigate(DestinoAmls.OlvideContrasena.ruta) }) {
                    androidx.compose.material3.Text("¿Olvidaste tu contraseña?", color = AzulPrincipal, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.iniciarSesion(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrincipal),
                enabled = camposHabilitados && email.isNotBlank() && password.isNotBlank()
            ) {
                if (uiState is AuthUiState.Cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                } else {
                    androidx.compose.material3.Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                androidx.compose.material3.Text("¿No tienes cuenta? ", color = GrisTexto, fontSize = 14.sp)
                androidx.compose.material3.Text(
                    "Regístrate",
                    color = AzulPrincipal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickableSinIndicacion { navController.navigate(DestinoAmls.Registro.ruta) }
                )
            }
        }

        androidx.compose.material3.Text(
            "Aprendizaje Móvil Adaptativo",
            color = GrisTexto,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )
    }
}
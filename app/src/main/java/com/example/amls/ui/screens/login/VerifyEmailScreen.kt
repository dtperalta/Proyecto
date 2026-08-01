package com.example.amls.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.amls.ui.AuthUiState
import com.example.amls.ui.AuthViewModel
import com.example.amls.ui.navigation.DestinoAmls

@Composable
fun VerifyEmailScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var codigo by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val camposHabilitados = uiState !is AuthUiState.Cargando

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Exito) {
            navController.navigate(DestinoAmls.Onboarding.ruta) {
                popUpTo(DestinoAmls.VerificarEmail.ruta) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoSuave)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Brush.linearGradient(listOf(VerdeAzulado, AzulPrincipal)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("AMLS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
        EtiquetaContexto("Último paso")
        Spacer(modifier = Modifier.height(16.dp))

        Text("Verifica tu correo", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Te enviamos un código de 6 dígitos. Revisa tu bandeja de entrada (y spam).",
            fontSize = 15.sp, color = GrisTexto
        )

        Spacer(modifier = Modifier.height(32.dp))

        AmlsTextField(value = codigo, onValueChange = { if (it.length <= 6) codigo = it }, label = "Código de 6 dígitos")

        if (uiState is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(10.dp))
            Text((uiState as AuthUiState.Error).mensaje, color = Color(0xFFDC2626), fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.verificarEmail(codigo) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulPrincipal),
            enabled = camposHabilitados && codigo.length == 6
        ) {
            if (uiState is AuthUiState.Cargando) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
            } else {
                Text("Verificar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(
            onClick = {
                viewModel.reenviarCodigoVerificacion()
                Toast.makeText(context, "Código reenviado, revisa tu correo", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿No te llegó? Reenviar código", color = AzulPrincipal, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                viewModel.cerrarSesion()
                navController.navigate(DestinoAmls.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cancelar y volver más tarde", color = GrisTexto, fontSize = 13.sp)
        }
    }
}

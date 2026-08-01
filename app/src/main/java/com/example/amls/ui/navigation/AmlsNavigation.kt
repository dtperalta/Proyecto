package com.example.amls.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.amls.ui.MainViewModel
import com.example.amls.ui.screens.home.HomeScreen
import com.example.amls.ui.screens.login.ForgotPasswordScreen
import com.example.amls.ui.screens.login.LoginScreen
import com.example.amls.ui.screens.login.RegisterScreen
import com.example.amls.ui.screens.login.VerifyEmailScreen
import com.example.amls.ui.screens.onboarding.OnboardingScreen
import com.example.amls.ui.screens.learning.PlaybackScreen
import com.example.amls.ui.screens.accessibility.SettingsScreen
import com.example.amls.ui.screens.quiz.QuizScreen

sealed class DestinoAmls(val ruta: String) {
    object Login : DestinoAmls("login")
    object Registro : DestinoAmls("registro")
    object VerificarEmail : DestinoAmls("verificar-email")
    object OlvideContrasena : DestinoAmls("olvide-contrasena")
    object Onboarding : DestinoAmls("onboarding")
    object Quiz : DestinoAmls("quiz")
    object Inicio : DestinoAmls("inicio")
    object Reproduccion : DestinoAmls("reproduccion/{recursoId}") {
        fun crearRuta(recursoId: String) = "reproduccion/$recursoId"
    }
    object ConfiguracionAccesibilidad : DestinoAmls("configuracion")
}

@Composable
fun AmlsAppNavigation(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val startDestination by mainViewModel.startDestination.collectAsState()

    val destino = startDestination
    if (destino == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = navController, startDestination = destino) {

        composable(DestinoAmls.Login.ruta) {
            LoginScreen(navController = navController)
        }

        composable(DestinoAmls.Registro.ruta) {
            RegisterScreen(navController = navController)
        }

        composable(DestinoAmls.VerificarEmail.ruta) {
            VerifyEmailScreen(navController = navController)
        }

        composable(DestinoAmls.OlvideContrasena.ruta) {
            ForgotPasswordScreen(navController = navController)
        }

        composable(DestinoAmls.Onboarding.ruta) {
            OnboardingScreen(navController = navController)
        }

        composable(DestinoAmls.Quiz.ruta) {
            QuizScreen(navController = navController)
        }

        composable(DestinoAmls.Inicio.ruta) {
            HomeScreen(navController = navController)
        }

        composable(
            route = DestinoAmls.Reproduccion.ruta,
            arguments = listOf(navArgument("recursoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recursoId = backStackEntry.arguments?.getString("recursoId") ?: ""
            PlaybackScreen(navController = navController, recursoId = recursoId)
        }

        composable(DestinoAmls.ConfiguracionAccesibilidad.ruta) {
            SettingsScreen(navController = navController)
        }
    }
}

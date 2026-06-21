package com.example.amls.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.amls.data.AmlsDatabase
import com.example.amls.ui.screens.login.LoginScreen
import com.example.amls.ui.screens.login.RegisterScreen
import com.example.amls.ui.screens.sensores.SensorsScreen
import com.example.amls.ui.screens.learning.PlaybackScreen
import com.example.amls.ui.screens.accessibility.SettingsScreen

sealed class DestinoAmls(val ruta: String) {
    object Login : DestinoAmls("login")
    object Registro : DestinoAmls("registro")
    object MonitorSensores : DestinoAmls("sensores")
    object Reproduccion : DestinoAmls("reproduccion")
    object ConfiguracionAccesibilidad : DestinoAmls("configuracion")
}

@Composable
fun AmlsAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = DestinoAmls.Login.ruta) {

        composable(DestinoAmls.Login.ruta) {
            LoginScreen(navController = navController)
        }

        composable(DestinoAmls.Registro.ruta) {
            RegisterScreen(navController = navController)
        }

        composable(DestinoAmls.Reproduccion.ruta) {
            PlaybackScreen(navController = navController)
        }

        composable(DestinoAmls.ConfiguracionAccesibilidad.ruta) {
            SettingsScreen(navController = navController)
        }

        composable(DestinoAmls.MonitorSensores.ruta) {
            SensorsScreen()
        }
    }
}

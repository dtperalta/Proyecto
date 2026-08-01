package com.example.amls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.amls.ui.navigation.AmlsAppNavigation
import com.example.amls.ui.theme.AmlsTheme
import com.example.amls.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val activity = this@MainActivity
            val themeViewModel: ThemeViewModel = hiltViewModel(activity)
            val modoTema by themeViewModel.modoTema.collectAsState()

            AmlsTheme(modoTema = modoTema) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // El enrutador ahora controla toda la aplicación
                    AmlsAppNavigation()
                }
            }
        }
    }
}
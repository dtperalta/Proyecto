package com.example.amls.ui.screens.sensores

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.amls.ui.AmlsViewModel

@Composable
fun SensorsScreen(viewModel: AmlsViewModel = hiltViewModel()) {
    val acceleration by viewModel.acceleration.collectAsState()
    val lightLevel by viewModel.lightLevel.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.startSensors()
            else if (event == Lifecycle.Event.ON_PAUSE) viewModel.stopSensors()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopSensors()
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text(text = "Monitor de Entorno", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Nivel de Luz (Luxes):", style = MaterialTheme.typography.titleMedium)
        Text(text = "$lightLevel lx")
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Acelerómetro (m/s²):", style = MaterialTheme.typography.titleMedium)
        Text(text = "Eje X: ${acceleration[0]}")
        Text(text = "Eje Y: ${acceleration[1]}")
        Text(text = "Eje Z: ${acceleration[2]}")
    }
}
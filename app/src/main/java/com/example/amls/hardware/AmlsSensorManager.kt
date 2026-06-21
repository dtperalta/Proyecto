package com.example.amls.hardware

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmlsSensorManager @Inject constructor(
    private val sensorManager: SensorManager
) : SensorEventListener {

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    // Estado reactivo para el acelerómetro (ejes X, Y, Z)
    private val _acceleration = MutableStateFlow(FloatArray(3))
    val acceleration: StateFlow<FloatArray> = _acceleration.asStateFlow()

    // Estado reactivo para la luz ambiental (Luxes)
    private val _lightLevel = MutableStateFlow(0f)
    val lightLevel: StateFlow<Float> = _lightLevel.asStateFlow()

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                _acceleration.value = event.values.clone()
            }
            Sensor.TYPE_LIGHT -> {
                _lightLevel.value = event.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No requerido para este prototipo
    }
}
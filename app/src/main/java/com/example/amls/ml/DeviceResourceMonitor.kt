package com.example.amls.ml

import android.content.Context
import android.os.BatteryManager
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceResourceMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun porcentajeBateria(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun almacenamientoLibreMb(): Long {
        val stat = StatFs(context.cacheDir.path)
        return (stat.availableBytes / (1024 * 1024))
    }

    fun bateriaBaja(): Boolean = porcentajeBateria() < 15
    fun almacenamientoBajo(): Boolean = almacenamientoLibreMb() < 200
}

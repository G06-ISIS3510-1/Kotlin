package com.wheels.app.core.theme

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

// Represents ambient light sensor state: whether sensor is available and current lux reading
// Lux < 30 = dark environment (triggers dark theme), >= 30 = bright (light theme)
data class AmbientLightState(
    val isLightSensorAvailable: Boolean,
    val ambientLux: Float? = null
)

/**
 * Monitors device light sensor and emits ambient light readings for adaptive theme.
 * Unregisters listener when Flow is disposed to save battery.
 */
@Singleton
class AmbientLightMonitor @Inject constructor(
    @ApplicationContext context: Context
) {
    // Get SensorManager from Android system
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    // Get light sensor from device, or null if not available
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    // Observable Flow of ambient light readings. Emits immediately when subscribed.
    // If no sensor: emits false then closes. If sensor: emits readings and updates on changes.
    val ambientLightState: Flow<AmbientLightState> = callbackFlow {
        val sensor = lightSensor
        
        // If device doesn't have light sensor, emit once and close
        if (sensor == null) {
            trySend(AmbientLightState(isLightSensorAvailable = false))
            close()
            return@callbackFlow
        }

        // Sensor is available, emit initial state
        trySend(AmbientLightState(isLightSensorAvailable = true))

        // Create listener to receive sensor events
        val listener = object : SensorEventListener {
            // Emit new light reading when sensor value changes
            override fun onSensorChanged(event: SensorEvent) {
                val lux = event.values.firstOrNull()
                trySend(
                    AmbientLightState(
                        isLightSensorAvailable = true,
                        ambientLux = lux  // Illuminance in lux
                    )
                )
            }

            // Called when sensor accuracy changes (not used here)
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        // Register listener: Android calls onSensorChanged() when light level changes
        // SENSOR_DELAY_NORMAL = ~200ms updates (balance between responsiveness and battery)
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Cleanup when Flow is disposed: unregister listener to prevent battery drain
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    // Skip duplicate readings to reduce Flow emissions when light level doesn't change
    .distinctUntilChanged()
}

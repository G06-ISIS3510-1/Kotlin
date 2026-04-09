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

data class AmbientLightState(
    val isLightSensorAvailable: Boolean,
    val ambientLux: Float? = null
)

@Singleton
class AmbientLightMonitor @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    val ambientLightState: Flow<AmbientLightState> = callbackFlow {
        val sensor = lightSensor
        if (sensor == null) {
            trySend(AmbientLightState(isLightSensorAvailable = false))
            close()
            return@callbackFlow
        }

        trySend(AmbientLightState(isLightSensorAvailable = true))

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val lux = event.values.firstOrNull()
                trySend(
                    AmbientLightState(
                        isLightSensorAvailable = true,
                        ambientLux = lux
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.distinctUntilChanged()
}

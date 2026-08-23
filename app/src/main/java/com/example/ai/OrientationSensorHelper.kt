package com.example.ai

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.model.HorizonLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class OrientationSensorHelper(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val gravitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _horizonLevel = MutableStateFlow(HorizonLevel(0.0f, true, "Level"))
    val horizonLevel: StateFlow<HorizonLevel> = _horizonLevel.asStateFlow()

    private var smoothedAngle = 0.0f

    fun startListening() {
        gravitySensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate roll tilt in portrait mode
        val roll = Math.toDegrees(atan2(x.toDouble(), sqrt(y * y + z * z).toDouble())).toFloat()
        
        // Low-pass smoothing filter
        smoothedAngle = smoothedAngle * 0.8f + roll * 0.2f
        val angleRounded = (Math.round(smoothedAngle * 10) / 10f).coerceIn(-45f, 45f)

        val isLevel = abs(angleRounded) <= 0.8f
        val text = when {
            isLevel -> "Level"
            angleRounded > 0.8f -> "Tilt Left (${String.format("%.1f°", angleRounded)})"
            else -> "Tilt Right (${String.format("%.1f°", abs(angleRounded))})"
        }

        _horizonLevel.value = HorizonLevel(
            angleDegrees = angleRounded,
            isLevel = isLevel,
            guidanceText = text
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

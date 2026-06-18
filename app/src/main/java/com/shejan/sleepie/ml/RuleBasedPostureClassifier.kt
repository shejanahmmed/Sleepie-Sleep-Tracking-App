package com.shejan.sleepie.ml

import kotlin.math.atan2
import kotlin.math.sqrt

class RuleBasedPostureClassifier : PostureClassifier {

    override fun classify(
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        gyroX: Float,
        gyroY: Float,
        gyroZ: Float
    ): String {
        // 1. Detect movement using gyroscope magnitude
        val gyroMagnitude = sqrt((gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ).toDouble()).toFloat()
        if (gyroMagnitude > GYRO_MOVEMENT_THRESHOLD) {
            return "MOVING"
        }

        // 2. Compute Pitch and Roll angles in degrees
        // Pitch: Rotation around X-axis (tilt forward/backward)
        val pitch = atan2(accelY.toDouble(), sqrt((accelX * accelX + accelZ * accelZ).toDouble())) * (180.0 / Math.PI)
        
        // Roll: Rotation around Y-axis (tilt left/right)
        val roll = atan2(-accelX.toDouble(), accelZ.toDouble()) * (180.0 / Math.PI)

        val absPitch = Math.abs(pitch)
        val absRoll = Math.abs(roll)

        return when {
            // If the phone is tilted heavily on its side (tilted left or right)
            absRoll > SIDE_ROLL_THRESHOLD && absRoll < 135.0 -> {
                "SIDE"
            }
            // If the phone is face down (negative Z, roll near 180 or -180)
            absRoll >= 135.0 -> {
                "STOMACH"
            }
            // If the phone is propped up longitudinally (e.g., standing upright or tilted forward)
            absPitch > STOMACH_PITCH_THRESHOLD -> {
                "STOMACH"
            }
            // Otherwise, it is lying relatively flat (screen up/down)
            else -> {
                "BACK"
            }
        }
    }

    companion object {
        private const val GYRO_MOVEMENT_THRESHOLD = 0.65f // rad/s
        private const val SIDE_ROLL_THRESHOLD = 35.0 // degrees
        private const val STOMACH_PITCH_THRESHOLD = 45.0 // degrees
    }
}

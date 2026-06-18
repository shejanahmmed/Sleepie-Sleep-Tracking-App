package com.shejan.sleepie.logic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AlertEngine(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Keep track of consecutive back-snoring events
    private var consecutiveBackSnoringCount = 0

    /**
     * Process current state. Returns true if haptic alert was triggered.
     */
    fun processState(posture: String, isSnoring: Boolean, sensitivity: String): Boolean {
        if (posture == "BACK" && isSnoring) {
            consecutiveBackSnoringCount++
            // Trigger alert after 3 consecutive back-snoring intervals (approx 45 seconds)
            if (consecutiveBackSnoringCount >= TRIGGER_THRESHOLD) {
                triggerVibration(sensitivity)
                consecutiveBackSnoringCount = 0 // Reset after alerting
                return true
            }
        } else {
            // Decelerate or reset the count if they change posture or stop snoring
            consecutiveBackSnoringCount = Math.max(0, consecutiveBackSnoringCount - 1)
        }
        return false
    }

    fun reset() {
        consecutiveBackSnoringCount = 0
    }

    private fun triggerVibration(sensitivity: String) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        val timings: LongArray
        val amplitudes: IntArray

        when (sensitivity) {
            "GENTLE" -> {
                timings = longArrayOf(0, 100, 100, 100)
                amplitudes = intArrayOf(0, 60, 0, 60) // Soft taps
            }
            "STRONG" -> {
                timings = longArrayOf(0, 400, 200, 400)
                amplitudes = intArrayOf(0, 255, 0, 255) // Solid buzzes
            }
            else -> { // MEDIUM
                timings = longArrayOf(0, 250, 150, 250)
                amplitudes = intArrayOf(0, 140, 0, 140) // Standard pulses
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, -1)
        }
    }

    companion object {
        private const val TRIGGER_THRESHOLD = 3 // Trigger after 3 logs (e.g. 45 seconds)
    }
}

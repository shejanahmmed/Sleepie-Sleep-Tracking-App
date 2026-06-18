package com.shejan.sleepie.logic

import com.shejan.sleepie.data.model.PostureLog
import kotlin.math.max
import kotlin.math.min

class PostureScorer {

    fun calculateScore(logs: List<PostureLog>, durationMillis: Long): Int {
        if (logs.isEmpty()) return 0

        // 1. Duration Score (target is 8 hours = 28,800,000 ms)
        val durationHours = durationMillis.toDouble() / (1000 * 60 * 60)
        val durationScore = min(100.0, (durationHours / 8.0) * 100.0)

        // 2. Efficiency/Sleep Quality deductions
        val totalLogs = logs.size
        val movingOrAwakeLogs = logs.count { it.posture == "MOVING" || it.posture == "AWAKE" }
        val awakeRatio = movingOrAwakeLogs.toDouble() / totalLogs
        
        // Deduction: up to 30 points off for high awake ratio
        val motionDeduction = awakeRatio * 100.0 * 0.3

        // 3. Snoring deductions
        val snoringLogs = logs.count { it.isSnoring }
        val snoringRatio = snoringLogs.toDouble() / totalLogs
        
        // Deduction: up to 20 points off for heavy snoring
        val snoringDeduction = snoringRatio * 100.0 * 0.2

        val finalScore = durationScore - motionDeduction - snoringDeduction
        return max(10, min(100, finalScore.toInt()))
    }

    fun evaluateRisk(logs: List<PostureLog>): String {
        if (logs.isEmpty()) return "LOW"

        val totalLogs = logs.size
        val backLogs = logs.count { it.posture == "BACK" }
        val snoringLogs = logs.count { it.isSnoring }
        val backSnoringLogs = logs.count { it.posture == "BACK" && it.isSnoring }

        val backRatio = backLogs.toDouble() / totalLogs
        val snoringRatio = snoringLogs.toDouble() / totalLogs
        val backSnoringRatio = if (backLogs > 0) backSnoringLogs.toDouble() / backLogs else 0.0

        return when {
            // Highly correlated snoring on the back (apnea warning)
            snoringRatio > 0.15 && backRatio > 0.40 && backSnoringRatio > 0.30 -> "HIGH"
            // Moderate snoring or excessive back sleeping
            snoringRatio > 0.08 || backRatio > 0.50 -> "MEDIUM"
            else -> "LOW"
        }
    }
}

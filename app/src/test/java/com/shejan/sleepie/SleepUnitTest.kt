package com.shejan.sleepie

import com.shejan.sleepie.data.model.PostureLog
import com.shejan.sleepie.logic.PostureScorer
import com.shejan.sleepie.ml.RuleBasedPostureClassifier
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepUnitTest {

    @Test
    fun testRuleBasedPostureClassifier_Movement() {
        val classifier = RuleBasedPostureClassifier()
        // Gyroscope magnitude: sqrt(1^2 + 1^2 + 1^2) = 1.732 > 0.65 threshold
        val posture = classifier.classify(0f, 0f, 9.8f, 1f, 1f, 1f)
        assertEquals("MOVING", posture)
    }

    @Test
    fun testRuleBasedPostureClassifier_BackSleeping() {
        val classifier = RuleBasedPostureClassifier()
        // Flat, screen up (Z is approx 9.8 m/s^2, gyro is zero)
        val posture = classifier.classify(0f, 0f, 9.8f, 0f, 0f, 0f)
        assertEquals("BACK", posture)
    }

    @Test
    fun testRuleBasedPostureClassifier_SideSleeping() {
        val classifier = RuleBasedPostureClassifier()
        // Tilted on edge (X is high)
        val posture = classifier.classify(8f, 0f, 2f, 0f, 0f, 0f)
        assertEquals("SIDE", posture)
    }

    @Test
    fun testRuleBasedPostureClassifier_StomachSleeping() {
        val classifier = RuleBasedPostureClassifier()
        // Face down (negative Z)
        val posture = classifier.classify(0f, 0f, -9.8f, 0f, 0f, 0f)
        assertEquals("STOMACH", posture)
    }

    @Test
    fun testPostureScorer_ScoreCalculation() {
        val scorer = PostureScorer()
        
        // 8 hours sleep (28,800,000 ms), all optimal side sleeping, no snoring
        val optimalLogs = List(10) { PostureLog(sessionId = 1, timestamp = 0L, posture = "SIDE", isSnoring = false, snoreVolume = 0f) }
        val scoreOptimal = scorer.calculateScore(optimalLogs, 8 * 3600 * 1000L)
        assertEquals(100, scoreOptimal)

        // 4 hours sleep, frequent moving, heavy snoring
        val poorLogs = List(10) { index ->
            PostureLog(
                sessionId = 1,
                timestamp = 0L,
                posture = if (index % 2 == 0) "MOVING" else "BACK",
                isSnoring = true,
                snoreVolume = 35f
            )
        }
        val scorePoor = scorer.calculateScore(poorLogs, 4 * 3600 * 1000L)
        // Expected deductions: 50 duration score - 15 (50% moving) - 20 (100% snore) = 15 score
        assertEquals(15, scorePoor)
    }

    @Test
    fun testPostureScorer_RiskEvaluation() {
        val scorer = PostureScorer()

        // Flat, quiet sleep -> Low Risk
        val lowRiskLogs = List(10) { PostureLog(sessionId = 1, timestamp = 0L, posture = "SIDE", isSnoring = false, snoreVolume = 0f) }
        assertEquals("LOW", scorer.evaluateRisk(lowRiskLogs))

        // High ratio back sleeping with snoring -> High Risk
        val highRiskLogs = List(10) { index ->
            PostureLog(
                sessionId = 1,
                timestamp = 0L,
                posture = if (index < 6) "BACK" else "SIDE", // 60% back
                isSnoring = index < 4, // 40% snoring
                snoreVolume = 25f
            )
        }
        assertEquals("HIGH", scorer.evaluateRisk(highRiskLogs))
    }
}

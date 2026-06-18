package com.shejan.sleepie.ml

interface PostureClassifier {
    fun classify(
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        gyroX: Float,
        gyroY: Float,
        gyroZ: Float
    ): String
}

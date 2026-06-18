package com.shejan.sleepie.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long, // Epoch milliseconds
    val endTime: Long? = null, // Epoch milliseconds, null if active
    val sleepScore: Int = 0, // 0 to 100
    val riskRating: String = "LOW", // LOW, MEDIUM, HIGH
    val morningNote: String? = null // Reflection comment
)

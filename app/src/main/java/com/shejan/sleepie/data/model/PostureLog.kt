package com.shejan.sleepie.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "posture_logs",
    foreignKeys = [
        ForeignKey(
            entity = SleepSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class PostureLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long, // FK referencing sleep_sessions.id
    val timestamp: Long, // Epoch milliseconds
    val posture: String, // BACK, SIDE, STOMACH, MOVING, AWAKE
    val isSnoring: Boolean,
    val snoreVolume: Float // Volume decibels or relative level
)

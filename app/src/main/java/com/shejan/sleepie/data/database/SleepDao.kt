package com.shejan.sleepie.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shejan.sleepie.data.model.PostureLog
import com.shejan.sleepie.data.model.SleepSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SleepSession): Long

    @Update
    suspend fun updateSession(session: SleepSession)

    @Delete
    suspend fun deleteSession(session: SleepSession)

    @Query("SELECT * FROM sleep_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): SleepSession?

    @Query("SELECT * FROM sleep_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): SleepSession?

    @Query("SELECT * FROM sleep_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC")
    fun getAllCompletedSessionsFlow(): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC LIMIT 1")
    fun getLastCompletedSessionFlow(): Flow<SleepSession?>

    @Query("SELECT * FROM sleep_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC")
    suspend fun getAllCompletedSessions(): List<SleepSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostureLog(log: PostureLog): Long

    @Query("SELECT * FROM posture_logs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getPostureLogsForSessionFlow(sessionId: Long): Flow<List<PostureLog>>

    @Query("SELECT * FROM posture_logs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getPostureLogsForSession(sessionId: Long): List<PostureLog>

    @Query("DELETE FROM sleep_sessions")
    suspend fun clearAllData()
}

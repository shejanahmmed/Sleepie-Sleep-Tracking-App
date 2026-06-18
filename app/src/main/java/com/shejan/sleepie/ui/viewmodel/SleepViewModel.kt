package com.shejan.sleepie.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.sleepie.data.database.SleepDatabase
import com.shejan.sleepie.data.model.PostureLog
import com.shejan.sleepie.data.model.SleepSession
import com.shejan.sleepie.data.preferences.PreferenceHelper
import com.shejan.sleepie.service.SleepTrackerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SleepViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SleepDatabase.getDatabase(application)
    private val sleepDao = database.sleepDao()
    private val prefs = PreferenceHelper(application)

    // Dynamic Tracking Feeds from Background Service
    val isTracking: StateFlow<Boolean> = SleepTrackerService.isTracking
    val livePosture: StateFlow<String> = SleepTrackerService.livePosture
    val liveAmplitude: StateFlow<Float> = SleepTrackerService.liveAmplitude
    val elapsedSeconds: StateFlow<Long> = SleepTrackerService.elapsedSeconds

    // History and Dashboard Feeds
    val allSessions: StateFlow<List<SleepSession>> = sleepDao.getAllCompletedSessionsFlow()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastSession: StateFlow<SleepSession?> = sleepDao.getLastCompletedSessionFlow()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Preference States (for reactive Compose UI bindings)
    var isOnboardingCompleted by mutableStateOf(prefs.isOnboardingCompleted)
        private set

    var preferredPosition by mutableStateOf(prefs.preferredPosition)
        private set

    var userAge by mutableStateOf(prefs.userAge)
        private set

    var userGender by mutableStateOf(prefs.userGender)
        private set

    var userConditions by mutableStateOf(prefs.userConditions)
        private set

    var sleepGoals by mutableStateOf(prefs.sleepGoals)
        private set

    var alertSensitivity by mutableStateOf(prefs.alertSensitivity)
        private set

    var alertsEnabled by mutableStateOf(prefs.alertsEnabled)
        private set

    var snoreDetectionEnabled by mutableStateOf(prefs.snoreDetectionEnabled)
        private set

    var appLanguage by mutableStateOf(prefs.appLanguage)
        private set

    // Selected Session Details (History Details Sheet)
    private val _selectedSession = MutableStateFlow<SleepSession?>(null)
    val selectedSession: StateFlow<SleepSession?> get() = _selectedSession

    val selectedSessionLogs: StateFlow<List<PostureLog>> = _selectedSession
        .filterNotNull()
        .flatMapLatest { session ->
            sleepDao.getPostureLogsForSessionFlow(session.id)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated Streak States
    val sleepStreak: StateFlow<Int> = allSessions.map { sessions ->
        calculateStreak(sessions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setOnboardingDone(completed: Boolean) {
        prefs.isOnboardingCompleted = completed
        isOnboardingCompleted = completed
    }

    fun selectSession(session: SleepSession?) {
        _selectedSession.value = session
    }

    fun startTrackingService(context: Context) {
        val intent = Intent(context, SleepTrackerService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopTrackingService(context: Context) {
        val intent = Intent(context, SleepTrackerService::class.java).apply {
            action = SleepTrackerService.ACTION_STOP_TRACKING
        }
        context.startService(intent)
    }

    fun updateLanguage(lang: String) {
        prefs.appLanguage = lang
        appLanguage = lang
    }

    fun updateAlertSettings(enabled: Boolean, sensitivity: String) {
        prefs.alertsEnabled = enabled
        prefs.alertSensitivity = sensitivity
        alertsEnabled = enabled
        alertSensitivity = sensitivity
    }

    fun updateSnoreDetection(enabled: Boolean) {
        prefs.snoreDetectionEnabled = enabled
        snoreDetectionEnabled = enabled
    }

    fun saveProfile(preferredPos: String, age: Int, gender: String, conditions: Set<String>, goals: Set<String>) {
        prefs.preferredPosition = preferredPos
        prefs.userAge = age
        prefs.userGender = gender
        prefs.userConditions = conditions
        prefs.sleepGoals = goals

        preferredPosition = preferredPos
        userAge = age
        userGender = gender
        userConditions = conditions
        sleepGoals = goals
    }

    fun saveMorningNote(sessionId: Long, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sleepDao.getSessionById(sessionId)
            if (session != null) {
                sleepDao.updateSession(session.copy(morningNote = note.trim()))
                // Refresh active details if open
                if (_selectedSession.value?.id == sessionId) {
                    _selectedSession.value = sleepDao.getSessionById(sessionId)
                }
            }
        }
    }

    fun deleteSession(session: SleepSession) {
        viewModelScope.launch(Dispatchers.IO) {
            sleepDao.deleteSession(session)
            if (_selectedSession.value?.id == session.id) {
                _selectedSession.value = null
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            sleepDao.clearAllData()
            prefs.clear()
            
            // Reset state parameters
            isOnboardingCompleted = false
            preferredPosition = "SIDE"
            userAge = 25
            userGender = "OTHER"
            userConditions = emptySet()
            sleepGoals = emptySet()
            alertSensitivity = "MEDIUM"
            alertsEnabled = true
            snoreDetectionEnabled = true
            appLanguage = "en"
            _selectedSession.value = null
        }
    }

    private fun calculateStreak(sessions: List<SleepSession>): Int {
        if (sessions.isEmpty()) return 0
        var streak = 0
        val sortedSessions = sessions.sortedByDescending { it.startTime }
        
        // Simple day calculation (consecutive calendar entries)
        var lastTime = System.currentTimeMillis()
        for (session in sortedSessions) {
            val diffMs = lastTime - session.startTime
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            if (diffDays <= 1) {
                streak++
                lastTime = session.startTime
            } else {
                break
            }
        }
        return streak
    }
}

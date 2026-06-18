package com.shejan.sleepie.data.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var preferredPosition: String
        get() = prefs.getString(KEY_PREFERRED_POSITION, "SIDE") ?: "SIDE"
        set(value) = prefs.edit().putString(KEY_PREFERRED_POSITION, value).apply()

    var userAge: Int
        get() = prefs.getInt(KEY_USER_AGE, 25)
        set(value) = prefs.edit().putInt(KEY_USER_AGE, value).apply()

    var userGender: String
        get() = prefs.getString(KEY_USER_GENDER, "OTHER") ?: "OTHER"
        set(value) = prefs.edit().putString(KEY_USER_GENDER, value).apply()

    var userConditions: Set<String>
        get() = prefs.getStringSet(KEY_USER_CONDITIONS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_USER_CONDITIONS, value).apply()

    var sleepGoals: Set<String>
        get() = prefs.getStringSet(KEY_SLEEP_GOALS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_SLEEP_GOALS, value).apply()

    var alertSensitivity: String
        get() = prefs.getString(KEY_ALERT_SENSITIVITY, "MEDIUM") ?: "MEDIUM" // GENTLE, MEDIUM, STRONG
        set(value) = prefs.edit().putString(KEY_ALERT_SENSITIVITY, value).apply()

    var alertsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ALERTS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ALERTS_ENABLED, value).apply()

    var snoreDetectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_SNORE_DETECTION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SNORE_DETECTION_ENABLED, value).apply()

    var appLanguage: String
        get() = prefs.getString(KEY_APP_LANGUAGE, "en") ?: "en" // en or bn
        set(value) = prefs.edit().putString(KEY_APP_LANGUAGE, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "sleepie_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_PREFERRED_POSITION = "preferred_position"
        private const val KEY_USER_AGE = "user_age"
        private const val KEY_USER_GENDER = "user_gender"
        private const val KEY_USER_CONDITIONS = "user_conditions"
        private const val KEY_SLEEP_GOALS = "sleep_goals"
        private const val KEY_ALERT_SENSITIVITY = "alert_sensitivity"
        private const val KEY_ALERTS_ENABLED = "alerts_enabled"
        private const val KEY_SNORE_DETECTION_ENABLED = "snore_detection_enabled"
        private const val KEY_APP_LANGUAGE = "app_language"
    }
}

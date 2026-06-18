package com.shejan.sleepie.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.shejan.sleepie.MainActivity
import com.shejan.sleepie.R
import com.shejan.sleepie.data.database.SleepDatabase
import com.shejan.sleepie.data.model.PostureLog
import com.shejan.sleepie.data.model.SleepSession
import com.shejan.sleepie.data.preferences.PreferenceHelper
import com.shejan.sleepie.logic.AlertEngine
import com.shejan.sleepie.logic.PostureScorer
import com.shejan.sleepie.ml.RuleBasedPostureClassifier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.log10

class SleepTrackerService : Service(), SensorEventListener {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    // Raw sensor values
    private var rawAccel = FloatArray(3)
    private var rawGyro = FloatArray(3)

    private val classifier = RuleBasedPostureClassifier()
    private lateinit var alertEngine: AlertEngine
    private lateinit var prefs: PreferenceHelper
    private lateinit var database: SleepDatabase

    private var audioRecord: AudioRecord? = null
    private val isRecordingAudio = AtomicBoolean(false)
    private var micAmplitude = 0f

    private var trackingJob: Job? = null
    private var timerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        alertEngine = AlertEngine(this)
        prefs = PreferenceHelper(this)
        database = SleepDatabase.getDatabase(this)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_TRACKING) {
            stopTracking()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification("Starting tracking...", "Detecting posture..."))
        startTracking()

        return START_STICKY
    }

    private fun startTracking() {
        if (isTracking.value) return
        isTracking.value = true
        elapsedSeconds.value = 0L

        // 1. Register sensor listeners
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }

        // 2. Start snore polling
        if (prefs.snoreDetectionEnabled) {
            startAudioRecording()
        }

        // 3. Main Tracking Loop
        trackingJob = serviceScope.launch {
            // Create a new sleep session in Room
            val startTime = System.currentTimeMillis()
            val newSession = SleepSession(startTime = startTime)
            val sessionId = database.sleepDao().insertSession(newSession)
            activeSessionId.value = sessionId

            // Log entry interval loop (every 15 seconds)
            while (isActive) {
                delay(15000)

                val posture = classifier.classify(
                    rawAccel[0], rawAccel[1], rawAccel[2],
                    rawGyro[0], rawGyro[1], rawGyro[2]
                )

                // Snore detection logic: standard snoring maps to sound amplitude > threshold
                val isSnoring = micAmplitude > SNORE_AMPLITUDE_THRESHOLD
                
                // Write log to DB
                val log = PostureLog(
                    sessionId = sessionId,
                    timestamp = System.currentTimeMillis(),
                    posture = posture,
                    isSnoring = isSnoring,
                    snoreVolume = micAmplitude
                )
                database.sleepDao().insertPostureLog(log)

                // Update static flows for the UI
                livePosture.value = posture
                liveAmplitude.value = micAmplitude

                // Alert Engine: checks if back-sleeping snoring has occurred
                if (prefs.alertsEnabled) {
                    alertEngine.processState(posture, isSnoring, prefs.alertSensitivity)
                }

                // Update notification text
                val notificationText = if (prefs.appLanguage == "bn") {
                    "অবস্থান: ${translatePostureBn(posture)} | নাক ডাকা: ${if (isSnoring) "হ্যাঁ" else "না"}"
                } else {
                    "Posture: $posture | Snore: ${if (isSnoring) "Yes" else "No"}"
                }
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, createNotification("Sleepie Active Tracker", notificationText))
            }
        }

        // 4. Elapsed Time Timer Job
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                elapsedSeconds.value++
            }
        }
    }

    private fun stopTracking() {
        serviceScope.launch {
            val sessionId = activeSessionId.value
            if (sessionId != null) {
                val session = database.sleepDao().getSessionById(sessionId)
                if (session != null) {
                    val endTime = System.currentTimeMillis()
                    val logs = database.sleepDao().getPostureLogsForSession(sessionId)
                    val duration = endTime - session.startTime

                    val scorer = PostureScorer()
                    val score = scorer.calculateScore(logs, duration)
                    val risk = scorer.evaluateRisk(logs)

                    val updatedSession = session.copy(
                        endTime = endTime,
                        sleepScore = score,
                        riskRating = risk
                    )
                    database.sleepDao().updateSession(updatedSession)
                }
            }

            // Cleanup on UI thread
            withContext(Dispatchers.Main) {
                sensorManager.unregisterListener(this@SleepTrackerService)
                stopAudioRecording()
                alertEngine.reset()

                isTracking.value = false
                livePosture.value = "UNKNOWN"
                liveAmplitude.value = 0f
                activeSessionId.value = null
                elapsedSeconds.value = 0L

                stopForeground(true)
                stopSelf()
            }
        }
    }

    // Audio Polling Logic
    private fun startAudioRecording() {
        try {
            val bufferSize = AudioRecord.getMinBufferSize(
                8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (bufferSize <= 0) return

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord = null
                return
            }

            isRecordingAudio.set(true)
            audioRecord?.startRecording()

            serviceScope.launch(Dispatchers.IO) {
                val buffer = ShortArray(bufferSize)
                while (isRecordingAudio.get()) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        var maxVal = 0
                        for (i in 0 until readSize) {
                            val value = Math.abs(buffer[i].toInt())
                            if (value > maxVal) {
                                maxVal = value
                            }
                        }
                        // Normalize amplitude (0 to 100 range)
                        val normalized = (maxVal.toFloat() / 32767f) * 100f
                        micAmplitude = normalized
                    }
                    delay(500)
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted, silent ignore
        } catch (e: Exception) {
            // Other error
        }
    }

    private fun stopAudioRecording() {
        isRecordingAudio.set(false)
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioRecord = null
    }

    // Sensor Listeners
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                rawAccel[0] = event.values[0]
                rawAccel[1] = event.values[1]
                rawAccel[2] = event.values[2]
            }
            Sensor.TYPE_GYROSCOPE -> {
                rawGyro[0] = event.values[0]
                rawGyro[1] = event.values[1]
                rawGyro[2] = event.values[2]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    // Notification UI Helper
    private fun createNotification(title: String, text: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, SleepTrackerService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play) // Use system icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (prefs.appLanguage == "bn") "ট্র্যাকিং বন্ধ করুন" else "Stop Tracking",
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sleepie Tracking Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun translatePostureBn(posture: String): String {
        return when (posture) {
            "BACK" -> "চিৎ হয়ে"
            "SIDE" -> "কাত হয়ে"
            "STOMACH" -> "উপুড় হয়ে"
            "MOVING" -> "নড়াচড়া"
            "AWAKE" -> "জেগে আছেন"
            else -> "অজানা"
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopTracking()
        serviceJob.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "sleepie_tracking_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_TRACKING = "com.shejan.sleepie.ACTION_STOP_TRACKING"
        
        private const val SNORE_AMPLITUDE_THRESHOLD = 18f // Adjust based on calibration

        // Live visual Flow APIs
        private val _isTracking = MutableStateFlow(false)
        val isTracking: MutableStateFlow<Boolean> get() = _isTracking

        private val _livePosture = MutableStateFlow("UNKNOWN")
        val livePosture: MutableStateFlow<String> get() = _livePosture

        private val _liveAmplitude = MutableStateFlow(0f)
        val liveAmplitude: MutableStateFlow<Float> get() = _liveAmplitude

        private val _activeSessionId = MutableStateFlow<Long?>(null)
        val activeSessionId: MutableStateFlow<Long?> get() = _activeSessionId

        private val _elapsedSeconds = MutableStateFlow(0L)
        val elapsedSeconds: MutableStateFlow<Long> get() = _elapsedSeconds
    }
}

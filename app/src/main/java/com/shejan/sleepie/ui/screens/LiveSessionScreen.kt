package com.shejan.sleepie.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.sleepie.ui.theme.Localization
import com.shejan.sleepie.ui.viewmodel.SleepViewModel
import kotlinx.coroutines.delay

@Composable
fun LiveSessionScreen(
    viewModel: SleepViewModel,
    onStopTracking: () -> Unit
) {
    val context = LocalContext.current
    val lang = viewModel.appLanguage

    val livePosture by viewModel.livePosture.collectAsState()
    val liveAmplitude by viewModel.liveAmplitude.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()

    var showStopDialog by remember { mutableStateOf(false) }
    var alertsEnabled by remember { mutableStateOf(viewModel.alertsEnabled) }

    // Battery checking logic
    var batteryPercentage by remember { mutableStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }

    LaunchedEffect(isTracking) {
        // Register receiver for battery changes
        val batteryStatus: Intent? = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        batteryPercentage = batteryStatus?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            (level.toFloat() / scale.toFloat() * 100).toInt()
        } ?: 100

        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        isCharging = chargePlug == BatteryManager.BATTERY_PLUGGED_AC ||
                chargePlug == BatteryManager.BATTERY_PLUGGED_USB ||
                chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS
    }

    // Keep track of time spent in current posture (simulated helper)
    var currentPostureTime by remember { mutableStateOf(0) }
    var previousPosture by remember { mutableStateOf("") }
    
    LaunchedEffect(livePosture) {
        if (livePosture != previousPosture) {
            currentPostureTime = 0
            previousPosture = livePosture
        }
    }
    
    LaunchedEffect(elapsedSeconds) {
        if (isTracking) {
            currentPostureTime++
        }
    }

    // Dynamic Pulsing animation for the "active" indicator (Nothing dot red)
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Status Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30).copy(alpha = pulseAlpha)) // Pulsing red dot
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.get("live_tracking", lang).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "BATT: $batteryPercentage%${if (isCharging) " ⚡" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Central Content: Digital Clock Timer & Posture Icon Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Large Digital Elapsed Clock
                Text(
                    text = formatSecondsToClock(elapsedSeconds),
                    style = MaterialTheme.typography.displayLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 54.sp
                )
                Text(
                    text = Localization.get("live_time", lang).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Posture Icon Widget Card (Obsidian style)
                Card(
                    modifier = Modifier
                        .size(180.dp)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Large Vector Posture Illustration Icon
                        val (postureIcon, postureColor) = when (livePosture) {
                            "BACK" -> Pair(Icons.Default.AirlineSeatFlat, Color(0xFF3B82F6)) // Sleep flat
                            "SIDE" -> Pair(Icons.Default.AirlineSeatFlatAngled, Color(0xFF10B981)) // Sleep side
                            "STOMACH" -> Pair(Icons.Default.Hotel, Color(0xFFF59E0B)) // Bed icon
                            "MOVING" -> Pair(Icons.Default.DirectionsRun, Color(0xFFFF3B30)) // Moving
                            "AWAKE" -> Pair(Icons.Default.WavingHand, Color(0xFFFF3B30))
                            else -> Pair(Icons.Default.Bedtime, MaterialTheme.colorScheme.primary)
                        }

                        Icon(
                            imageVector = postureIcon,
                            contentDescription = null,
                            tint = postureColor,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Classified state text
                        Text(
                            text = translatePosture(livePosture, lang).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatSecondsToHoursMins(currentPostureTime.toLong(), lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Bottom Content: Snore Amplitude Pulse Wave & Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sound Wave Amplitude Visualizer (Compose Canvas)
                Text(
                    text = Localization.get("live_snore_indicator", lang).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Standard noise values buffer list
                val amplitudesHistory = remember { mutableStateListOf<Float>().apply { repeat(32) { add(2f) } } }

                LaunchedEffect(liveAmplitude) {
                    amplitudesHistory.add(liveAmplitude.coerceAtLeast(2f))
                    if (amplitudesHistory.size > 32) {
                        amplitudesHistory.removeAt(0)
                    }
                }

                // Capture colors before Canvas (not composable inside Canvas)
                val waveNormalColor = MaterialTheme.colorScheme.primary
                val waveSnoreColor = Color(0xFFFF3B30)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val count = amplitudesHistory.size
                    val spacing = if (count > 1) width / (count - 1) else width

                    // Draw vertical soundwave bars (Nothing OS dot-matrix layout)
                    amplitudesHistory.forEachIndexed { index, amp ->
                        val barHeight = (amp / 100f) * height
                        val clampedBarHeight = barHeight.coerceIn(4.dp.toPx(), height)
                        val x = index * spacing

                        drawLine(
                            color = if (amp > 18f) waveSnoreColor else waveNormalColor,
                            start = androidx.compose.ui.geometry.Offset(x, height / 2 - clampedBarHeight / 2),
                            end = androidx.compose.ui.geometry.Offset(x, height / 2 + clampedBarHeight / 2),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Alert toggle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.get("live_alerts_toggle", lang),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = alertsEnabled,
                        onCheckedChange = {
                            alertsEnabled = it
                            viewModel.updateAlertSettings(it, viewModel.alertSensitivity)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stop session button (Nothing OS block outline)
                OutlinedButton(
                    onClick = { showStopDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF3B30)), // Red outline warning
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF3B30))
                ) {
                    Text(
                        text = if (lang == "bn") "ট্র্যাকিং শেষ করুন" else "STOP TRACKING SESSION",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Stop Confirmation Dialog
    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = {
                Text(
                    text = Localization.get("live_dialog_title", lang),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = Localization.get("live_dialog_desc", lang))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopDialog = false
                        viewModel.stopTrackingService(context)
                        onStopTracking()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                ) {
                    Text(text = Localization.get("live_dialog_yes", lang).uppercase(), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showStopDialog = false },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(text = Localization.get("live_dialog_no", lang).uppercase(), fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
        )
    }
}

// Format seconds to digital clock HH:MM:SS
fun formatSecondsToClock(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}

fun formatSecondsToHoursMins(seconds: Long, lang: String): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (lang == "bn") {
        "${m} মিনিট ${s} সেকেন্ড"
    } else {
        "${m}m ${s}s"
    }
}

fun translatePosture(posture: String, lang: String): String {
    return when (posture) {
        "BACK" -> if (lang == "bn") "চিত হয়ে" else "Back Sleeping"
        "SIDE" -> if (lang == "bn") "কাত হয়ে" else "Side Sleeping"
        "STOMACH" -> if (lang == "bn") "উপুড় হয়ে" else "Stomach Sleeping"
        "MOVING" -> if (lang == "bn") "নড়াচড়া" else "Moving"
        "AWAKE" -> if (lang == "bn") "জেগে আছেন" else "Awake"
        else -> if (lang == "bn") "অজানা" else "Unknown"
    }
}

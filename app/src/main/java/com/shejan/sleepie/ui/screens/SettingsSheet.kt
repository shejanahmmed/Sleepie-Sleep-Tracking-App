package com.shejan.sleepie.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.sleepie.ui.theme.Localization
import com.shejan.sleepie.ui.viewmodel.SleepViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsSheet(
    viewModel: SleepViewModel,
    onClose: () -> Unit
) {
    val lang = viewModel.appLanguage
    var showClearDialog by remember { mutableStateOf(false) }

    // Recalibration State
    var showCalibration by remember { mutableStateOf(false) }
    var calibrationStep by remember { mutableStateOf(0) }
    var calibrationCountdown by remember { mutableStateOf(5) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.get("settings_title", lang).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Language Toggle (English vs. Bengali)
        Text(
            text = Localization.get("settings_lang", lang).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val languages = listOf("en" to "ENGLISH", "bn" to "বাংলা (BENGALI)")
            languages.forEach { (code, name) ->
                val selected = lang == code
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { viewModel.updateLanguage(code) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Snore detection toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (lang == "bn") "নাক ডাকা সনাক্তকরণ" else "Snore Detection Meter",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (lang == "bn") "ঘুমানোর সময় নাক ডাকার তীব্রতা পরিমাপ করুন" else "Continuously log mic volume level",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Switch(
                checked = viewModel.snoreDetectionEnabled,
                onCheckedChange = { viewModel.updateSnoreDetection(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Vibration Alert settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Localization.get("live_alerts_toggle", lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (lang == "bn") "চিত হয়ে ঘুমালে মৃদু ভাইব্রেশন দিন" else "Haptic push if snoring on back",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Switch(
                checked = viewModel.alertsEnabled,
                onCheckedChange = { viewModel.updateAlertSettings(it, viewModel.alertSensitivity) },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        if (viewModel.alertsEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = Localization.get("alert_sensitivity", lang).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val levels = listOf("GENTLE", "MEDIUM", "STRONG")
                levels.forEach { lvl ->
                    val selected = viewModel.alertSensitivity == lvl
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { viewModel.updateAlertSettings(true, lvl) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Localization.get("alert_${lvl.lowercase()}", lang).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Redo Sensor Calibration
        if (!showCalibration) {
            Button(
                onClick = {
                    showCalibration = true
                    scope.launch {
                        for (step in 1..3) {
                            calibrationStep = step
                            calibrationCountdown = 5
                            while (calibrationCountdown > 0) {
                                delay(1000)
                                calibrationCountdown--
                            }
                        }
                        calibrationStep = 4
                        delay(1500)
                        showCalibration = false
                        calibrationStep = 0
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.get("settings_redo_calib", lang).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            // Live calibration overlay inside settings
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val statusText = when (calibrationStep) {
                        1 -> Localization.get("calib_back", lang)
                        2 -> Localization.get("calib_side", lang)
                        3 -> Localization.get("calib_stomach", lang)
                        4 -> Localization.get("calib_done", lang)
                        else -> ""
                    }
                    Text(text = statusText, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    if (calibrationStep in 1..3) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$calibrationCountdown SEC", style = MaterialTheme.typography.headlineLarge, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 5. Destructive WIPE ALL DATA Button
        Button(
            onClick = { showClearDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = Localization.get("settings_clear", lang).uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = Localization.get("settings_clear", lang),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = Localization.get("settings_clear_confirm", lang))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearAllData()
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                ) {
                    Text(text = Localization.get("settings_clear_btn", lang).uppercase(), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearDialog = false },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(text = Localization.get("settings_cancel", lang).uppercase(), fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
        )
    }
}

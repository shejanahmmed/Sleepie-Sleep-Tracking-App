package com.shejan.sleepie.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.shejan.sleepie.ui.theme.Localization
import com.shejan.sleepie.ui.viewmodel.SleepViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: SleepViewModel,
    onFinished: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 5
    val lang = viewModel.appLanguage

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header: Step indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.get("app_name", lang).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$currentStep/$totalSteps",
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Step Progress Bar (Monochromatic grid dot style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (i <= currentStep) MaterialTheme.colorScheme.primary 
                                else Color.Transparent
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step content container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    1 -> WelcomeStep(lang)
                    2 -> PermissionsStep(lang)
                    3 -> CalibrationStep(lang)
                    4 -> ProfileStep(lang, viewModel)
                    5 -> GoalsStep(lang, viewModel)
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (lang == "bn") "পেছনে" else "Back",
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            viewModel.setOnboardingDone(true)
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    val btnText = when (currentStep) {
                        1 -> Localization.get("onboarding_btn_start", lang)
                        totalSteps -> Localization.get("onboarding_btn_finish", lang)
                        else -> Localization.get("onboarding_btn_next", lang)
                    }
                    Text(
                        text = btnText.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(lang: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dot-matrix inspired logo placeholder
        Box(
            modifier = Modifier
                .size(96.dp)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = Localization.get("onboarding_title", lang),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Localization.get("onboarding_welcome_desc", lang),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Privacy Promise card (thin digital border)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Text(
                text = Localization.get("onboarding_privacy", lang),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun PermissionsStep(lang: String) {
    val context = LocalContext.current
    
    // Check permission states
    var micGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var notifGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notifGranted = granted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = Localization.get("perm_title", lang).uppercase(),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = Localization.get("perm_desc", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Permission Card: Motion Sensors (Always Good)
        PermissionItemCard(
            title = Localization.get("perm_sensors", lang),
            desc = Localization.get("perm_sensors_desc", lang),
            isGranted = true,
            onGrantClick = {}
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Permission Card: Microphone
        PermissionItemCard(
            title = Localization.get("perm_mic", lang),
            desc = Localization.get("perm_mic_desc", lang),
            isGranted = micGranted,
            onGrantClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Permission Card: Notifications
        PermissionItemCard(
            title = Localization.get("perm_notif", lang),
            desc = Localization.get("perm_notif_desc", lang),
            isGranted = notifGranted,
            onGrantClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Permission Card: Battery (Info only)
        PermissionItemCard(
            title = Localization.get("perm_battery", lang),
            desc = Localization.get("perm_battery_desc", lang),
            isGranted = true,
            onGrantClick = {}
        )
    }
}

@Composable
fun PermissionItemCard(
    title: String,
    desc: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.width(16.dp))

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF10B981) // Retro alert green
            )
        } else {
            OutlinedButton(
                onClick = onGrantClick,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(text = "GRANT", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun CalibrationStep(lang: String) {
    var calibrationState by remember { mutableStateOf(0) } // 0: Idle, 1: Back, 2: Side, 3: Stomach, 4: Done
    var countdown by remember { mutableStateOf(5) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = Localization.get("calib_title", lang).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = Localization.get("calib_desc", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Dynamic Calibration Visual Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (calibrationState) {
                    0 -> {
                        Icon(imageVector = Icons.Default.SettingsPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "READY FOR CALIBRATION", style = MaterialTheme.typography.labelLarge, fontFamily = FontFamily.Monospace)
                    }
                    1 -> {
                        Text(text = Localization.get("calib_back", lang), textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$countdown SEC", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.error, fontFamily = FontFamily.Monospace)
                    }
                    2 -> {
                        Text(text = Localization.get("calib_side", lang), textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$countdown SEC", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.error, fontFamily = FontFamily.Monospace)
                    }
                    3 -> {
                        Text(text = Localization.get("calib_stomach", lang), textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$countdown SEC", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.error, fontFamily = FontFamily.Monospace)
                    }
                    4 -> {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = Localization.get("calib_done", lang).uppercase(), style = MaterialTheme.typography.labelLarge, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (calibrationState == 0 || calibrationState == 4) {
            Button(
                onClick = {
                    scope.launch {
                        calibrationState = 1
                        for (step in 1..3) {
                            calibrationState = step
                            countdown = 5
                            while (countdown > 0) {
                                delay(1000)
                                countdown--
                            }
                        }
                        calibrationState = 4
                    }
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text(
                    text = if (calibrationState == 4) "RECALIBRATE" else Localization.get("calib_btn", lang).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileStep(lang: String, viewModel: SleepViewModel) {
    var age by remember { mutableStateOf(viewModel.userAge) }
    var gender by remember { mutableStateOf(viewModel.userGender) }
    var preferredPos by remember { mutableStateOf(viewModel.preferredPosition) }
    val conditions = remember { mutableStateListOf<String>().apply { addAll(viewModel.userConditions) } }

    val conditionOptions = listOf("cond_back_pain", "cond_snoring", "cond_pregnancy", "cond_apnea")

    // Save profile to ViewModel on updates
    LaunchedEffect(preferredPos, age, gender, conditions.size) {
        viewModel.saveProfile(
            preferredPos = preferredPos,
            age = age,
            gender = gender,
            conditions = conditions.toSet(),
            goals = viewModel.sleepGoals
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = Localization.get("profile_title", lang).uppercase(),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = Localization.get("profile_desc", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Age Selection (Nothing widget style)
        Text(text = "${Localization.get("profile_age", lang)}: $age", style = MaterialTheme.typography.titleSmall)
        Slider(
            value = age.toFloat(),
            onValueChange = { age = it.toInt() },
            valueRange = 10f..100f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gender Selection (Monochrome Segmented Card)
        Text(text = Localization.get("profile_gender", lang), style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val genders = listOf("MALE", "FEMALE", "OTHER")
            genders.forEach { gen ->
                val selected = gender == gen
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
                        .clickable { gender = gen }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gen,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preferred sleeping position
        Text(text = Localization.get("profile_preferred", lang), style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val positions = listOf("BACK", "SIDE", "STOMACH")
            positions.forEach { pos ->
                val selected = preferredPos == pos
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
                        .clickable { preferredPos = pos }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Localization.get("profile_${pos.lowercase()}", lang).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Conditions list
        Text(text = Localization.get("profile_conditions", lang), style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        conditionOptions.forEach { condKey ->
            val isChecked = conditions.contains(condKey)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(8.dp))
                    .clickable {
                        if (isChecked) conditions.remove(condKey) else conditions.add(condKey)
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        if (isChecked) conditions.remove(condKey) else conditions.add(condKey)
                    },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = Localization.get(condKey, lang), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun GoalsStep(lang: String, viewModel: SleepViewModel) {
    val goalsList = listOf("goal_reduce_snore", "goal_back_pain", "goal_sleep_depth", "goal_posture_fix")
    val selectedGoals = remember { mutableStateListOf<String>().apply { addAll(viewModel.sleepGoals) } }
    var sensitivity by remember { mutableStateOf(viewModel.alertSensitivity) }
    var hapticEnabled by remember { mutableStateOf(viewModel.alertsEnabled) }

    LaunchedEffect(selectedGoals.size, hapticEnabled, sensitivity) {
        viewModel.saveProfile(
            preferredPos = viewModel.preferredPosition,
            age = viewModel.userAge,
            gender = viewModel.userGender,
            conditions = viewModel.userConditions,
            goals = selectedGoals.toSet()
        )
        viewModel.updateAlertSettings(hapticEnabled, sensitivity)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = Localization.get("goals_title", lang).uppercase(),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = Localization.get("goals_desc", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Goals List
        goalsList.forEach { goalKey ->
            val isChecked = selectedGoals.contains(goalKey)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(8.dp))
                    .clickable {
                        if (isChecked) selectedGoals.remove(goalKey) else selectedGoals.add(goalKey)
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        if (isChecked) selectedGoals.remove(goalKey) else selectedGoals.add(goalKey)
                    },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = Localization.get(goalKey, lang), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Vibration Alerts settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = Localization.get("live_alerts_toggle", lang), style = MaterialTheme.typography.titleSmall)
                Text(text = "Vibrate if snoring on back", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Switch(
                checked = hapticEnabled,
                onCheckedChange = { hapticEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        if (hapticEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = Localization.get("alert_sensitivity", lang), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val levels = listOf("GENTLE", "MEDIUM", "STRONG")
                levels.forEach { lvl ->
                    val selected = sensitivity == lvl
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
                            .clickable { sensitivity = lvl }
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
    }
}

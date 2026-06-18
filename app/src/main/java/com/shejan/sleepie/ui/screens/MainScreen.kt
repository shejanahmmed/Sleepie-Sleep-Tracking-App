package com.shejan.sleepie.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.sleepie.ui.theme.Localization
import com.shejan.sleepie.ui.viewmodel.SleepViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SleepViewModel) {
    val context = LocalContext.current
    val isOnboardingCompleted = viewModel.isOnboardingCompleted
    val isTracking by viewModel.isTracking.collectAsState()
    val lang = viewModel.appLanguage

    var activeTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: History, 2: Tips
    var showSettingsSheet by remember { mutableStateOf(false) }

    if (!isOnboardingCompleted) {
        // Overlay onboarding wizard on first launch
        OnboardingScreen(
            viewModel = viewModel,
            onFinished = {
                viewModel.setOnboardingDone(true)
            }
        )
    } else if (isTracking) {
        // Overlay active sleep tracking screen
        LiveSessionScreen(
            viewModel = viewModel,
            onStopTracking = {
                activeTab = 0 // Return to dashboard on complete
            }
        )
    } else {
        // Normal Navigation Screen with bottom tab bar
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val tabs = listOf(
                        Triple(0, Icons.Default.Bedtime, "nav_dashboard"),
                        Triple(1, Icons.Default.History, "nav_history"),
                        Triple(2, Icons.Default.Lightbulb, "nav_tips")
                    )

                    tabs.forEach { (index, icon, labelKey) ->
                        val selected = activeTab == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = { activeTab = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            },
                            label = {
                                Text(
                                    text = Localization.get(labelKey, lang).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (activeTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onStartTracking = {
                            viewModel.startTrackingService(context)
                        },
                        onOpenSettings = {
                            showSettingsSheet = true
                        }
                    )
                    1 -> HistoryScreen(viewModel = viewModel)
                    2 -> TipsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal sheet for Settings
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            SettingsSheet(
                viewModel = viewModel,
                onClose = { showSettingsSheet = false }
            )
        }
    }
}

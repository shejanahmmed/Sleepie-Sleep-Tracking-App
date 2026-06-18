package com.shejan.sleepie.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.sleepie.data.model.SleepSession
import com.shejan.sleepie.ui.theme.Localization
import com.shejan.sleepie.ui.viewmodel.SleepViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: SleepViewModel,
    onStartTracking: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val lastSession by viewModel.lastSession.collectAsState(initial = null)
    val allSessions by viewModel.allSessions.collectAsState(initial = emptyList())
    val lang = viewModel.appLanguage
    val hasSnored = lastSession?.riskRating == "HIGH" || lastSession?.riskRating == "MEDIUM"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Localization.get("dash_welcome", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = Localization.get("app_name", lang).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (lastSession == null) {
            // Empty welcome state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Localization.get("dash_no_data", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            // Last session Score Ring
            val session = lastSession!!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Gauge Ring (Compose Canvas)
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val outlineColor = MaterialTheme.colorScheme.outline
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background track
                        drawCircle(
                            color = outlineColor,
                            radius = size.minDimension / 2 - 8.dp.toPx(),
                            style = Stroke(width = 8.dp.toPx())
                        )
                        // Active sweep representing score percentage
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = (session.sleepScore / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = session.sleepScore.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                        Text(
                            text = "/100",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Score descriptions
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Localization.get("dash_last_night", lang).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getFormattedDate(session.startTime),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Text(
                            text = "${Localization.get("dash_duration", lang)}: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = formatDuration(session.endTime?.minus(session.startTime) ?: 0L, lang),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row {
                        Text(
                            text = "${Localization.get("dash_risk", lang)}: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        val ratingColor = when (session.riskRating) {
                            "HIGH" -> Color(0xFFEF4444)
                            "MEDIUM" -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        }
                        Text(
                            text = session.riskRating,
                            style = MaterialTheme.typography.bodySmall,
                            color = ratingColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row of Widgets: Snoring indicator & Tonight's Goal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Snoring status widget
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Column {
                    Icon(
                        imageVector = if (hasSnored) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (hasSnored) Color(0xFFF59E0B) else Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (hasSnored) Localization.get("dash_snore_detected", lang) else Localization.get("dash_snore_not_detected", lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expected Sleep goal widget
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = Localization.get("dash_expected_score", lang).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Localization.get("dash_expected_desc", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly Trend mini line chart widget
        if (allSessions.size >= 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.get("dash_avg_score", lang).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        val avgScore = allSessions.take(7).map { it.sleepScore }.average().toInt()
                        Text(
                            text = "$avgScore PTS",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Line chart Canvas
                    val trendScores = allSessions.take(7).reversed().map { it.sleepScore.toFloat() }
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val outlineColor = MaterialTheme.colorScheme.outline
                    
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val pointsCount = trendScores.size
                        val stepX = width / (pointsCount - 1)
                        val maxScore = 100f
                        val minScore = 30f
                        
                        val path = Path()
                        trendScores.forEachIndexed { index, score ->
                            // Scale Y: higher score = lower pixel height coordinate
                            val normalizedY = (1f - ((score - minScore) / (maxScore - minScore))) * height
                            val clampedY = normalizedY.coerceIn(0f, height)
                            val x = index * stepX
                            
                            if (index == 0) {
                                path.moveTo(x, clampedY)
                            } else {
                                path.lineTo(x, clampedY)
                            }
                            // Draw dot at each score point (Nothing style)
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, clampedY)
                            )
                        }
                        // Draw connecting path
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                        
                        // Draw grid lines
                        drawLine(
                            color = outlineColor,
                            start = androidx.compose.ui.geometry.Offset(0f, height),
                            end = androidx.compose.ui.geometry.Offset(width, height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Today's Personalized Posture Tip card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "POSTURE COACH",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val coachTip = when {
                    lastSession?.riskRating == "HIGH" -> {
                        if (lang == "bn") "কোচ: চিত হয়ে শোয়ার সময় আপনি প্রচুর নাক ডেকেছেন। শ্বাসরোধের ঝুঁকি কমাতে কাত হয়ে শোয়ার চেষ্টা করুন।"
                        else "COACH: You logged heavy snoring while sleeping on your back. We recommend sleeping on your side to open up your airways."
                    }
                    viewModel.preferredPosition == "BACK" && hasSnored -> {
                        if (lang == "bn") "কোচ: কোমর ব্যথার জন্য চিত হয়ে শোয়ার পছন্দ করেছেন, কিন্তু এর ফলে নাক ডাকা বাড়ছে। কোমরকে সোজা রাখতে হাঁটুর নিচে একটি বালিশ দিয়ে ঘুমান।"
                        else "COACH: You sleep on your back, which is causing snoring episodes. Try placing a pillow underneath your knees to align your spine."
                    }
                    else -> {
                        if (lang == "bn") "কোচ: আপনার ঘুমের ভঙ্গি চমৎকার ছিল! কাত হয়ে শোয়া মেরুদণ্ড ভালো রাখে এবং নাক ডাকা অনেকাংশে কমিয়ে দেয়।"
                        else "COACH: Great sleep posture! Sleeping on your side keeps your spine aligned and helps keep your airways clear."
                    }
                }
                Text(
                    text = coachTip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Big Glowing CTA "Start Tracking" button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onStartTracking() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.get("dash_start_btn", lang).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// Utility formatting functions
private fun getFormattedDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    return format.format(date)
}

fun formatDuration(millis: Long, lang: String): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    
    return if (lang == "bn") {
        "${hours} ঘণ্টা ${minutes} মিনিট"
    } else {
        "${hours}h ${minutes}m"
    }
}

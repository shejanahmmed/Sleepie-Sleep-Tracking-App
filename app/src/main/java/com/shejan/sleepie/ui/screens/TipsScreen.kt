package com.shejan.sleepie.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.sleepie.data.model.SleepSession
import com.shejan.sleepie.ui.theme.Localization
import com.shejan.sleepie.ui.viewmodel.SleepViewModel

@Composable
fun TipsScreen(viewModel: SleepViewModel) {
    val allSessions by viewModel.allSessions.collectAsState(initial = emptyList())
    val streakDays by viewModel.sleepStreak.collectAsState()
    val lang = viewModel.appLanguage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // App Bar Title
        Text(
            text = Localization.get("nav_tips", lang).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // 1. Personalized Coach Advice Card
        Text(
            text = Localization.get("tips_top", lang).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFF3B30)) // Accent red
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == "bn") "কোচের আজকের পরামর্শ" else "TODAY'S ANALYSIS TIP",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                val analysisTip = getPersonalizedTip(allSessions, viewModel.preferredPosition, lang)
                Text(
                    text = analysisTip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Posture Education Cards (Material3 Expansions or beautiful cards)
        Text(
            text = Localization.get("tips_edu", lang).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EduCard(
                title = if (lang == "bn") "চিত হয়ে শোয়া (Back Sleeping)" else "Back Position & Airway obstruction",
                desc = if (lang == "bn") "চিত হয়ে ঘুমালে আমাদের জিব পেছনে নেমে আসে এবং শ্বাসনালী বন্ধ হয়ে যায়, যা নাক ডাকা বাড়ায়।" 
                       else "Lying flat causes gravity to pull the tongue backwards, potentially obstructing your breathing path and increasing snoring.",
                icon = Icons.Default.AirlineSeatFlat,
                color = Color(0xFF3B82F6)
            )
            EduCard(
                title = if (lang == "bn") "কাত হয়ে শোয়া (Side Sleeping)" else "Side Sleeping: Optimal Spine Alignment",
                desc = if (lang == "bn") "কাত হয়ে ঘুমানো শ্বাসনালী উন্মুক্ত রাখে এবং কোমর ও ঘাড়ের মেরুদণ্ডকে সোজা রাখতে সাহায্য করে।" 
                       else "Side-sleeping opens up throat airways, dramatically reduces snoring frequency, and keeps your back column aligned.",
                icon = Icons.Default.AirlineSeatFlatAngled,
                color = Color(0xFF10B981)
            )
            EduCard(
                title = if (lang == "bn") "উপুড় হয়ে শোয়া (Stomach Sleeping)" else "Stomach Sleeping: Spinal warning",
                desc = if (lang == "bn") "উপুড় হয়ে ঘুমালে মেরুদণ্ডের স্বাভাবিক বাঁক বজায় থাকে না এবং ঘাড় একপাশে দীর্ঘক্ষণ বাঁকা থাকায় ব্যথা হতে পারে।" 
                       else "Sleeping on your belly causes neck straining as you twist your head. It may trigger muscular stiffness in the morning.",
                icon = Icons.Default.Hotel,
                color = Color(0xFFF59E0B)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Achievements and Badges
        Text(
            text = Localization.get("tips_badges", lang).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Grid of badges (Nothing OS widget style)
        val unlockedStreak3 = allSessions.size >= 3
        val unlockedStreak7 = allSessions.size >= 7
        val unlockedQuiet = allSessions.any { it.riskRating == "LOW" }
        val unlockedPosture = allSessions.size > 0 // Simplified mock unlock checks

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BadgeCard(
                    modifier = Modifier.weight(1f),
                    title = Localization.get("badge_streak_3", lang),
                    desc = Localization.get("badge_streak_3_desc", lang),
                    isUnlocked = unlockedStreak3,
                    icon = Icons.Default.Check
                )
                BadgeCard(
                    modifier = Modifier.weight(1f),
                    title = Localization.get("badge_streak_7", lang),
                    desc = Localization.get("badge_streak_7_desc", lang),
                    isUnlocked = unlockedStreak7,
                    icon = Icons.Default.Stars
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BadgeCard(
                    modifier = Modifier.weight(1f),
                    title = Localization.get("badge_quiet", lang),
                    desc = Localization.get("badge_quiet_desc", lang),
                    isUnlocked = unlockedQuiet,
                    icon = Icons.Default.VolumeMute
                )
                BadgeCard(
                    modifier = Modifier.weight(1f),
                    title = Localization.get("badge_posture", lang),
                    desc = Localization.get("badge_posture_desc", lang),
                    isUnlocked = unlockedPosture,
                    icon = Icons.Default.Bedtime
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EduCard(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun BadgeCard(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    isUnlocked: Boolean,
    icon: ImageVector
) {
    Box(
        modifier = modifier
            .border(
                BorderStroke(
                    1.dp,
                    if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .alpha(if (isUnlocked) 1f else 0.4f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        CircleShape
                    )
                    .background(if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

private fun getPersonalizedTip(
    sessions: List<SleepSession>,
    preferredPosition: String,
    lang: String
): String {
    if (sessions.isEmpty()) {
        return if (lang == "bn") {
            "আপনার ঘুমের ইতিহাস বিশ্লেষণ করতে আগে কমপক্ষে ১টি স্লিপ সেশন রেকর্ড করুন।"
        } else {
            "Please record at least one sleep session to generate tailored insights."
        }
    }

    val totalLogs = sessions.size
    val highRiskLogs = sessions.count { it.riskRating == "HIGH" }
    val averageScore = sessions.map { it.sleepScore }.average().toInt()

    return when {
        highRiskLogs > 0 -> {
            if (lang == "bn") {
                "বিশ্লেষণ: আপনার ব্যাক স্লিপিংয়ের সাথে নাক ডাকার শক্তিশালী সংযোগ রয়েছে। কাত হয়ে ঘুমানোর অভ্যাস করলে নাক ডাকা কমে যাওয়ার সম্ভাবনা ৭০%।"
            } else {
                "ANALYSIS: You have a high correlation between back sleeping and snoring events. Shifting your default posture to Side sleeping typically reduces snoring by up to 70%."
            }
        }
        averageScore > 85 -> {
            if (lang == "bn") {
                "বিশ্লেষণ: আপনার ঘুম অত্যন্ত স্থিতিশীল এবং ভালো। বর্তমান স্লিপ রুটিন বজায় রাখুন। শোয়ার আগে ভারী খাবার খাওয়া পরিহার করুন।"
            } else {
                "ANALYSIS: Your sleep scores are excellent. Keep up your current sleeping posture schedule, and avoid heavy meals 2 hours before bed."
            }
        }
        else -> {
            if (lang == "bn") {
                "বিশ্লেষণ: ঘুমের মান বাড়াতে তোশকের গঠন একটু শক্ত ও বালিশের উচ্চতা সঠিক রাখুন, যেন মেরুদণ্ড সোজা থাকে।"
            } else {
                "ANALYSIS: To improve overall efficiency, ensure your mattress is medium-firm and your pillow height keeps your neck neutral with your spine."
            }
        }
    }
}

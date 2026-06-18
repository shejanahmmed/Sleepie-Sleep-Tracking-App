package com.shejan.sleepie.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.shejan.sleepie.data.model.PostureLog
import com.shejan.sleepie.data.model.SleepSession
import com.shejan.sleepie.ui.theme.Localization
import com.shejan.sleepie.ui.viewmodel.SleepViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: SleepViewModel) {
    val allSessions by viewModel.allSessions.collectAsState(initial = emptyList())
    val selectedSession by viewModel.selectedSession.collectAsState()
    val selectedSessionLogs by viewModel.selectedSessionLogs.collectAsState()
    val streakDays by viewModel.sleepStreak.collectAsState()
    val lang = viewModel.appLanguage
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Weekly, 1: Monthly

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // App Bar Title
        Text(
            text = Localization.get("hist_title", lang).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Segmented Tabs: Weekly vs Monthly (Monochrome)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val tabs = listOf(Localization.get("hist_weekly", lang), Localization.get("hist_monthly", lang))
            tabs.forEachIndexed { index, title ->
                val selected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History content list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Widgets
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Streak Widget
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$streakDays ${Localization.get("hist_streak_desc", lang)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Export CSV Widget
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
                            .clickable { shareCsvExport(context, allSessions) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = Localization.get("hist_export", lang).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Past sessions list header
            item {
                Text(
                    text = Localization.get("hist_sessions_list", lang).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Historical List of Completed Sleep sessions
            items(allSessions) { session ->
                HistorySessionItem(session, lang) {
                    viewModel.selectSession(session)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Session Details sheet modal overlays
    if (selectedSession != null) {
        val session = selectedSession!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectSession(null) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            SessionDetailContent(
                session = session,
                logs = selectedSessionLogs,
                lang = lang,
                onSaveNote = { note -> viewModel.saveMorningNote(session.id, note) },
                onDelete = {
                    viewModel.deleteSession(session)
                    viewModel.selectSession(null)
                },
                onClose = { viewModel.selectSession(null) }
            )
        }
    }
}

@Composable
fun HistorySessionItem(
    session: SleepSession,
    lang: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = formatHistoryDate(session.startTime),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDuration(session.endTime?.minus(session.startTime) ?: 0L, lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${session.sleepScore} PTS",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                val ratingColor = when (session.riskRating) {
                    "HIGH" -> Color(0xFFEF4444)
                    "MEDIUM" -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }
                Text(
                    text = session.riskRating,
                    style = MaterialTheme.typography.labelSmall,
                    color = ratingColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun SessionDetailContent(
    session: SleepSession,
    logs: List<PostureLog>,
    lang: String,
    onSaveNote: (String) -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    var noteText by remember { mutableStateOf(session.morningNote ?: "") }

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
            Column {
                Text(
                    text = Localization.get("hist_detail_title", lang).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = formatHistoryDate(session.startTime),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Color-coded Horizontal Posture Timeline Bar (Compose Canvas)
        Text(
            text = Localization.get("hist_detail_timeline", lang).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (logs.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val logsCount = logs.size
                    val segmentWidth = width / logsCount

                    logs.forEachIndexed { index, log ->
                        val color = when (log.posture) {
                            "BACK" -> Color(0xFF3B82F6) // Blue
                            "SIDE" -> Color(0xFF10B981) // Green
                            "STOMACH" -> Color(0xFFF59E0B) // Amber
                            "MOVING", "AWAKE" -> Color(0xFFFF3B30) // Red
                            else -> Color(0xFF8E8E93) // Gray
                        }
                        drawRect(
                            color = color,
                            topLeft = androidx.compose.ui.geometry.Offset(index * segmentWidth, 0f),
                            size = androidx.compose.ui.geometry.Size(segmentWidth + 1f, height)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Legend Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem("Back", Color(0xFF3B82F6))
                LegendItem("Side", Color(0xFF10B981))
                LegendItem("Stomach", Color(0xFFF59E0B))
                LegendItem("Awake", Color(0xFFFF3B30))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Posture distribution values
        Text(
            text = Localization.get("hist_posture_breakdown", lang).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (logs.isNotEmpty()) {
            val total = logs.size
            val backPct = (logs.count { it.posture == "BACK" }.toFloat() / total * 100).toInt()
            val sidePct = (logs.count { it.posture == "SIDE" }.toFloat() / total * 100).toInt()
            val stomachPct = (logs.count { it.posture == "STOMACH" }.toFloat() / total * 100).toInt()
            val awakePct = 100 - backPct - sidePct - stomachPct

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DistributionRow("Back Position", "$backPct%", Color(0xFF3B82F6))
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
                    DistributionRow("Side Position", "$sidePct%", Color(0xFF10B981))
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
                    DistributionRow("Stomach Position", "$stomachPct%", Color(0xFFF59E0B))
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
                    DistributionRow("Awake / Moving", "${maxOf(0, awakePct)}%", Color(0xFFFF3B30))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Morning reflection note editor
        Text(
            text = Localization.get("hist_detail_note", lang).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            placeholder = { Text(text = Localization.get("hist_detail_note_hint", lang)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onSaveNote(noteText) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = Localization.get("hist_detail_save_note", lang).uppercase(), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Delete button
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFFFF3B30)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF3B30))
        ) {
            Text(text = "DELETE THIS SESSION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun DistributionRow(label: String, pct: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(text = pct, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

private fun formatHistoryDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}

private fun formatHistoryDate(dateStr: String): String = dateStr

// Generate and share CSV history data
private fun shareCsvExport(context: Context, sessions: List<SleepSession>) {
    if (sessions.isEmpty()) return
    
    try {
        val cacheDir = context.cacheDir
        val csvFile = File(cacheDir, "sleepie_history_export.csv")
        
        csvFile.printWriter().use { out ->
            out.println("SessionID,StartTime,EndTime,SleepScore,ObstructionRisk,MorningNote")
            sessions.forEach { s ->
                val startStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(s.startTime))
                val endStr = s.endTime?.let { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it)) } ?: ""
                out.println("${s.id},\"$startStr\",\"$endStr\",${s.sleepScore},${s.riskRating},\"${s.morningNote ?: ""}\"")
            }
        }
        
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            csvFile
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Sleepie History"))
    } catch (e: Exception) {
        // Fail silent or toast
    }
}

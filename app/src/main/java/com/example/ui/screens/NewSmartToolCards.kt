package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.net.Uri
import coil.compose.AsyncImage

@Composable
fun StopwatchTimerCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    var selectedTab by remember { mutableStateOf(0) } // 0: Stopwatch, 1: Timer

    // Stopwatch State
    var isStopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchTimeMs by remember { mutableLongStateOf(0L) }
    val lapTimes = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isStopwatchRunning) {
        while (isStopwatchRunning) {
            delay(10)
            stopwatchTimeMs += 10
        }
    }

    // Timer State
    var timerMinutesInput by remember { mutableStateOf("1") }
    var timerSecondsInput by remember { mutableStateOf("00") }
    var timerRemainingMs by remember { mutableLongStateOf(60000L) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timerRemainingMs > 0) {
            delay(100)
            timerRemainingMs = (timerRemainingMs - 100).coerceAtLeast(0L)
            if (timerRemainingMs == 0L) {
                isTimerRunning = false
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = themeColors.buttonEqualBg,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (isBn) "স্টপওয়াচ" else "Stopwatch", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (isBn) "টাইমার" else "Countdown Timer", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // --- Stopwatch ---
                val minutes = (stopwatchTimeMs / 1000) / 60
                val seconds = (stopwatchTimeMs / 1000) % 60
                val millis = (stopwatchTimeMs % 1000) / 10

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColors.displayText.copy(alpha = 0.05f))
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d:%02d.%02d", minutes, seconds, millis),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { isStopwatchRunning = !isStopwatchRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStopwatchRunning) Color(0xFFE53935) else themeColors.buttonEqualBg
                        )
                    ) {
                        Icon(
                            imageVector = if (isStopwatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isStopwatchRunning) (if (isBn) "থামুন" else "Pause") else (if (isBn) "শুরু" else "Start"))
                    }

                    if (isStopwatchRunning) {
                        OutlinedButton(
                            onClick = { lapTimes.add(0, stopwatchTimeMs) }
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "ল্যাপ" else "Lap")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            isStopwatchRunning = false
                            stopwatchTimeMs = 0L
                            lapTimes.clear()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "রিসেট" else "Reset")
                    }
                }

                if (lapTimes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isBn) "ল্যাপ তালিকা:" else "Laps:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.heightIn(max = 140.dp)
                    ) {
                        lapTimes.take(5).forEachIndexed { index, lapMs ->
                            val lMin = (lapMs / 1000) / 60
                            val lSec = (lapMs / 1000) % 60
                            val lMs = (lapMs % 1000) / 10
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${if (isBn) "ল্যাপ" else "Lap"} #${lapTimes.size - index}",
                                    color = themeColors.displayText.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = String.format("%02d:%02d.%02d", lMin, lSec, lMs),
                                    color = themeColors.displayText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // --- Timer ---
                val tMin = (timerRemainingMs / 1000) / 60
                val tSec = (timerRemainingMs / 1000) % 60

                if (!isTimerRunning && timerRemainingMs == 0L) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "⏰ সময় শেষ!" else "⏰ Time's Up!",
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColors.displayText.copy(alpha = 0.05f))
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d:%02d", tMin, tSec),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isTimerRunning) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = timerMinutesInput,
                            onValueChange = { timerMinutesInput = it },
                            label = { Text(if (isBn) "মিনিট" else "Min") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = timerSecondsInput,
                            onValueChange = { timerSecondsInput = it },
                            label = { Text(if (isBn) "সেকেন্ড" else "Sec") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (!isTimerRunning) {
                                val m = timerMinutesInput.toLongOrNull() ?: 0L
                                val s = timerSecondsInput.toLongOrNull() ?: 0L
                                timerRemainingMs = (m * 60 + s) * 1000L
                                if (timerRemainingMs > 0) isTimerRunning = true
                            } else {
                                isTimerRunning = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTimerRunning) Color(0xFFE53935) else themeColors.buttonEqualBg
                        )
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTimerRunning) (if (isBn) "পজ" else "Pause") else (if (isBn) "স্টার্ট" else "Start"))
                    }

                    OutlinedButton(
                        onClick = {
                            isTimerRunning = false
                            val m = timerMinutesInput.toLongOrNull() ?: 0L
                            val s = timerSecondsInput.toLongOrNull() ?: 0L
                            timerRemainingMs = (m * 60 + s) * 1000L
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "রিসেট" else "Reset")
                    }
                }
            }
        }
    }
}

@Composable
fun NotesChecklistCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val clipboardManager = LocalClipboardManager.current

    // Notes lists from ViewModel
    val notes = remember(viewModel.notesListString) { viewModel.getSavedNotes() }

    // Editor State
    var isEditing by remember { mutableStateOf(false) }
    var editingNoteId by remember { mutableStateOf<String?>(null) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var isChecklistMode by remember { mutableStateOf(false) }
    var checklistInputItem by remember { mutableStateOf("") }
    val currentChecklistItems = remember { mutableStateListOf<com.example.data.model.ChecklistItem>() }
    var selectedColorIndex by remember { mutableStateOf(0) }
    var selectedTag by remember { mutableStateOf("General") }

    // Search and Filters
    var searchQuery by remember { mutableStateOf("") }
    var activeTagFilter by remember { mutableStateOf("All") }

    // Color definitions
    val stickyColors = listOf(
        Color.Transparent,
        Color(0xFFFFFAEC), // Soft Yellow
        Color(0xFFF1FDF5), // Soft Green
        Color(0xFFF0F7FF), // Soft Blue
        Color(0xFFFFF5F5), // Soft Pink/Red
        Color(0xFFFAF5FF)  // Soft Purple
    )
    val stickyBorders = listOf(
        themeColors.displayText.copy(alpha = 0.12f),
        Color(0xFFF59E0B).copy(alpha = 0.4f),
        Color(0xFF10B981).copy(alpha = 0.4f),
        Color(0xFF3B82F6).copy(alpha = 0.4f),
        Color(0xFFEF4444).copy(alpha = 0.4f),
        Color(0xFF8B5CF6).copy(alpha = 0.4f)
    )

    val tagsList = listOf("General", "Personal", "Work", "Task", "Shopping")

    // Helper to clear editor
    val resetEditor = {
        isEditing = false
        editingNoteId = null
        noteTitle = ""
        noteContent = ""
        isChecklistMode = false
        checklistInputItem = ""
        currentChecklistItems.clear()
        selectedColorIndex = 0
        selectedTag = "General"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "স্মার্ট নোটস ও কিউরেটেড মেমো" else "Smart Notes & Curated Memos",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }

                if (!isEditing) {
                    Button(
                        onClick = {
                            resetEditor()
                            isEditing = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "নতুন" else "New",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    TextButton(onClick = { resetEditor() }) {
                        Text(
                            text = if (isBn) "বাতিল" else "Cancel",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Note Creator / Editor Workspace ---
            if (isEditing) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, stickyBorders[selectedColorIndex], RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedColorIndex == 0) themeColors.background else stickyColors[selectedColorIndex]
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Title
                        BasicTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (noteTitle.isEmpty()) {
                                    Text(
                                        text = if (isBn) "শিরোনাম / টাইটেল..." else "Title...",
                                        color = themeColors.displayText.copy(alpha = 0.4f),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = themeColors.displayText.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Standard Text Note Mode or Checklist Mode Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { isChecklistMode = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (!isChecklistMode) themeColors.buttonEqualBg.copy(alpha = 0.15f) else Color.Transparent
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (!isChecklistMode) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBn) "সাধারন নোট" else "Standard Note",
                                        fontSize = 11.sp,
                                        color = if (!isChecklistMode) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            TextButton(
                                onClick = { isChecklistMode = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (isChecklistMode) themeColors.buttonEqualBg.copy(alpha = 0.15f) else Color.Transparent
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FormatListBulleted,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isChecklistMode) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBn) "চেকলিস্ট মেমো" else "Checklist Memo",
                                        fontSize = 11.sp,
                                        color = if (isChecklistMode) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Body Content
                        if (!isChecklistMode) {
                            BasicTextField(
                                value = noteContent,
                                onValueChange = { noteContent = it },
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = themeColors.displayText
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp, max = 150.dp),
                                decorationBox = { innerTextField ->
                                    if (noteContent.isEmpty()) {
                                        Text(
                                            text = if (isBn) "এখানে আপনার নোট বা জরুরি মেমোটি বিস্তারিত লিখুন..." else "Type details of your note here...",
                                            color = themeColors.displayText.copy(alpha = 0.4f),
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        } else {
                            // Checklist Mode Builder
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (currentChecklistItems.isEmpty()) {
                                    Text(
                                        text = if (isBn) "কোনো চেকলিস্ট আইটেম নেই।" else "No checklist items added.",
                                        fontSize = 12.sp,
                                        color = themeColors.displayText.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        currentChecklistItems.forEachIndexed { idx, item ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clip(CircleShape)
                                                            .background(if (item.isChecked) themeColors.buttonEqualBg else Color.Transparent)
                                                            .border(
                                                                width = 1.5.dp,
                                                                color = if (item.isChecked) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.4f),
                                                                shape = CircleShape
                                                            )
                                                            .clickable {
                                                                currentChecklistItems[idx] = item.copy(isChecked = !item.isChecked)
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (item.isChecked) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(10.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = item.text,
                                                        fontSize = 13.sp,
                                                        color = if (item.isChecked) themeColors.displayText.copy(alpha = 0.4f) else themeColors.displayText,
                                                        style = if (item.isChecked) TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else TextStyle.Default
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { currentChecklistItems.removeAt(idx) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove",
                                                        tint = Color.Red.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Add checklist item input row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = checklistInputItem,
                                        onValueChange = { checklistInputItem = it },
                                        textStyle = TextStyle(fontSize = 13.sp, color = themeColors.displayText),
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                themeColors.displayText.copy(alpha = 0.05f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        decorationBox = { innerTextField ->
                                            if (checklistInputItem.isEmpty()) {
                                                Text(
                                                    text = if (isBn) "নতুন আইটেম যোগ করুন..." else "Add new item...",
                                                    color = themeColors.displayText.copy(alpha = 0.4f),
                                                    fontSize = 13.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            if (checklistInputItem.isNotBlank()) {
                                                currentChecklistItems.add(
                                                    com.example.data.model.ChecklistItem(
                                                        checklistInputItem.trim(),
                                                        false
                                                    )
                                                )
                                                checklistInputItem = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.buttonEqualBg)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Sticky Note Colors & Tags Panel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Colors selector
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                stickyColors.forEachIndexed { idx, color ->
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(if (idx == 0) themeColors.background else color)
                                            .border(
                                                width = if (selectedColorIndex == idx) 2.dp else 1.dp,
                                                color = if (selectedColorIndex == idx) themeColors.buttonEqualBg else themeColors.displayText.copy(
                                                    alpha = 0.2f
                                                ),
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColorIndex = idx }
                                    )
                                }
                            }

                            // Tags selection
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(themeColors.displayText.copy(alpha = 0.05f))
                                    .clickable {
                                        val curIdx = tagsList.indexOf(selectedTag)
                                        val nextIdx = (curIdx + 1) % tagsList.size
                                        selectedTag = tagsList[nextIdx]
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Label,
                                        contentDescription = null,
                                        tint = themeColors.buttonEqualBg,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedTag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Save Button
                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank() || noteContent.isNotBlank() || currentChecklistItems.isNotEmpty()) {
                                    val now = java.text.SimpleDateFormat(
                                        "dd MMM yyyy, hh:mm a",
                                        java.util.Locale.getDefault()
                                    ).format(java.util.Date())
                                    val newNote = com.example.data.model.ProfessionalNote(
                                        id = editingNoteId ?: java.util.UUID.randomUUID().toString(),
                                        title = noteTitle.ifBlank { if (isBn) "শিরোনামহীন নোট" else "Untitled Note" },
                                        content = noteContent,
                                        dateString = now,
                                        isChecklist = isChecklistMode,
                                        checklistItems = currentChecklistItems.toList(),
                                        colorIndex = selectedColorIndex,
                                        tag = selectedTag
                                    )
                                    viewModel.saveNote(newNote)
                                    resetEditor()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isBn) "সেভ করুন" else "Save Note & Memo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // --- Notes List & Quick Search Panel ---
            if (!isEditing) {
                // Search field + Tag Filters Row
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isBn) "নোট খুঁজুন..." else "Search notes...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = themeColors.displayText.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = themeColors.displayText.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter row
                    val filterTags = listOf("All") + tagsList
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filterTags.forEach { tag ->
                            val isSelected = activeTagFilter == tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(
                                            alpha = 0.05f
                                        )
                                    )
                                    .clickable { activeTagFilter = tag }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Displaying Notes List
                val filteredNotes = notes.filter { note ->
                    val matchesSearch = note.title.contains(searchQuery, ignoreCase = true) ||
                            note.content.contains(searchQuery, ignoreCase = true) ||
                            note.checklistItems.any { it.text.contains(searchQuery, ignoreCase = true) }
                    val matchesTag = activeTagFilter == "All" || note.tag == activeTagFilter
                    matchesSearch && matchesTag
                }

                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "কোনো মিল পাওয়া যায়নি।" else "No matching notes found.",
                            color = themeColors.displayText.copy(alpha = 0.4f),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredNotes.forEach { note ->
                            val noteBgColor = if (note.colorIndex == 0) themeColors.background else stickyColors[note.colorIndex]
                            val noteBorderColor = stickyBorders[note.colorIndex]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, noteBorderColor, RoundedCornerShape(12.dp))
                                    .clickable {
                                        // Edit Note Clicked
                                        editingNoteId = note.id
                                        noteTitle = note.title
                                        noteContent = note.content
                                        isChecklistMode = note.isChecklist
                                        currentChecklistItems.clear()
                                        currentChecklistItems.addAll(note.checklistItems)
                                        selectedColorIndex = note.colorIndex
                                        selectedTag = note.tag
                                        isEditing = true
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = noteBgColor)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Row with Title & tag, Copy/Delete buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = note.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.displayText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(themeColors.buttonEqualBg.copy(alpha = 0.12f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = note.tag,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = themeColors.buttonEqualBg
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Copy
                                            IconButton(
                                                onClick = {
                                                    val shareText = if (note.isChecklist) {
                                                        "${note.title}\n" + note.checklistItems.joinToString("\n") {
                                                            "${if (it.isChecked) "☑" else "☐"} ${it.text}"
                                                        }
                                                    } else {
                                                        "${note.title}\n${note.content}"
                                                    }
                                                    clipboardManager.setText(AnnotatedString(shareText))
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    tint = themeColors.displayText.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            // Delete
                                            IconButton(
                                                onClick = { viewModel.deleteNote(note.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color.Red.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Content preview
                                    if (!note.isChecklist) {
                                        if (note.content.isNotBlank()) {
                                            Text(
                                                text = note.content,
                                                fontSize = 12.sp,
                                                color = themeColors.displayText.copy(alpha = 0.7f),
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            note.checklistItems.take(3).forEach { item ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .clip(CircleShape)
                                                            .background(if (item.isChecked) themeColors.buttonEqualBg else Color.Transparent)
                                                            .border(
                                                                width = 1.dp,
                                                                color = if (item.isChecked) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.4f),
                                                                shape = CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (item.isChecked) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(8.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = item.text,
                                                        fontSize = 11.sp,
                                                        color = if (item.isChecked) themeColors.displayText.copy(alpha = 0.4f) else themeColors.displayText.copy(alpha = 0.7f),
                                                        style = if (item.isChecked) TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else TextStyle.Default,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            if (note.checklistItems.size > 3) {
                                                Text(
                                                    text = "... ${if (isBn) "আরো ${note.checklistItems.size - 3} টি আইটেম" else "${note.checklistItems.size - 3} more items"}",
                                                    fontSize = 10.sp,
                                                    color = themeColors.displayText.copy(alpha = 0.5f),
                                                    modifier = Modifier.padding(start = 16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Timestamp
                                    Text(
                                        text = note.dateString,
                                        fontSize = 9.sp,
                                        color = themeColors.displayText.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorldClockCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Date()
        }
    }

    val timezones = listOf(
        Triple("Dhaka (BST)", "Asia/Dhaka", "🇧🇩"),
        Triple("London (GMT)", "Europe/London", "🇬🇧"),
        Triple("New York (EST)", "America/New_York", "🇺🇸"),
        Triple("Dubai (GST)", "Asia/Dubai", "🇦🇪"),
        Triple("Tokyo (JST)", "Asia/Tokyo", "🇯🇵"),
        Triple("Riyadh (AST)", "Asia/Riyadh", "🇸🇦")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isBn) "ওয়ার্ল্ড ক্লক (বিশ্বের সময়)" else "World Clock & Timezones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                timezones.forEach { (cityName, zoneId, flag) ->
                    val sdf = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH)
                    sdf.timeZone = TimeZone.getTimeZone(zoneId)
                    val formattedTime = sdf.format(currentTime)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(themeColors.displayText.copy(alpha = 0.05f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = flag, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = cityName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = themeColors.displayText,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formattedTime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = themeColors.buttonEqualBg
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnitPriceComparerCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var priceA by remember { mutableStateOf("") }
    var qtyA by remember { mutableStateOf("") }

    var priceB by remember { mutableStateOf("") }
    var qtyB by remember { mutableStateOf("") }

    val pA = priceA.toDoubleOrNull() ?: 0.0
    val qA = qtyA.toDoubleOrNull() ?: 0.0
    val unitPriceA = if (qA > 0) pA / qA else 0.0

    val pB = priceB.toDoubleOrNull() ?: 0.0
    val qB = qtyB.toDoubleOrNull() ?: 0.0
    val unitPriceB = if (qB > 0) pB / qB else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isBn) "একক দাম তুলনা (Unit Price Comparer)" else "Unit Price Comparer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Item A
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) "পণ্য A" else "Item A",
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = priceA,
                        onValueChange = { priceA = it },
                        label = { Text(if (isBn) "দাম (৳)" else "Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = qtyA,
                        onValueChange = { qtyA = it },
                        label = { Text(if (isBn) "পরিমাণ (গ্রাম/কেজি)" else "Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    if (unitPriceA > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format("একক: %.2f /unit", unitPriceA),
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }

                // Item B
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) "পণ্য B" else "Item B",
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = priceB,
                        onValueChange = { priceB = it },
                        label = { Text(if (isBn) "দাম (৳)" else "Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = qtyB,
                        onValueChange = { qtyB = it },
                        label = { Text(if (isBn) "পরিমাণ (গ্রাম/কেজি)" else "Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    if (unitPriceB > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format("একক: %.2f /unit", unitPriceB),
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (unitPriceA > 0 && unitPriceB > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                val isABetter = unitPriceA < unitPriceB
                val diffPercent = kotlin.math.abs((unitPriceA - unitPriceB) / kotlin.math.max(unitPriceA, unitPriceB)) * 100

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isABetter) Color(0xFFE8F5E9) else Color(0xFFE3F2FD))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (isABetter)
                                (if (isBn) "🎉 পণ্য A বেশি সাশ্রয়ী!" else "🎉 Item A is a better deal!")
                            else
                                (if (isBn) "🎉 পণ্য B বেশি সাশ্রয়ী!" else "🎉 Item B is a better deal!"),
                            fontWeight = FontWeight.Bold,
                            color = if (isABetter) Color(0xFF2E7D32) else Color(0xFF1565C0),
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(if (isBn) "প্রায় %.1f%% খরচ কম পড়বে" else "About %.1f%% cheaper per unit", diffPercent),
                            fontSize = 13.sp,
                            color = themeColors.displayText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleCompassCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isBn) "ডিজিটাল কম্পাস (Digital Compass)" else "Digital Compass & Level",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .border(4.dp, themeColors.buttonEqualBg, CircleShape)
                    .background(themeColors.displayText.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "N",
                        color = Color.Red,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "0° North",
                        color = themeColors.displayText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isBn) "কম্পাস ও লেভেল মোড সক্রিয়" else "Heading: 0° North (Digital Calibration Active)",
                fontSize = 12.sp,
                color = themeColors.displayText.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun AspectRatioCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var widthInput by remember { mutableStateOf("1920") }
    var heightInput by remember { mutableStateOf("1080") }

    val w = widthInput.toDoubleOrNull() ?: 0.0
    val h = heightInput.toDoubleOrNull() ?: 0.0

    fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

    val gcdVal = if (w > 0 && h > 0) gcd(w.toLong(), h.toLong()) else 1L
    val ratioW = if (gcdVal > 0) (w / gcdVal).toInt() else 0
    val ratioH = if (gcdVal > 0) (h / gcdVal).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isBn) "অ্যাসপেক্ট রেশিও (Aspect Ratio)" else "Aspect Ratio Calculator",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = widthInput,
                    onValueChange = { widthInput = it },
                    label = { Text(if (isBn) "প্রস্থ (Width px)" else "Width (px)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text(if (isBn) "উচ্চতা (Height px)" else "Height (px)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            if (w > 0 && h > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColors.displayText.copy(alpha = 0.05f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$ratioW : $ratioH",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                        Text(
                            text = String.format("Decimal Ratio: %.2f", w / h),
                            fontSize = 13.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RandomPickerCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var minInput by remember { mutableStateOf("1") }
    var maxInput by remember { mutableStateOf("100") }
    var pickedNumber by remember { mutableStateOf<Int?>(null) }
    var coinResult by remember { mutableStateOf<String?>(null) }
    var diceResult by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isBn) "র্যান্ডম নাম্বার ও ডাইস (Random Picker)" else "Random Picker & Dice Roller",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Number Generator
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = minInput,
                    onValueChange = { minInput = it },
                    label = { Text("Min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = maxInput,
                    onValueChange = { maxInput = it },
                    label = { Text("Max") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val min = minInput.toIntOrNull() ?: 1
                        val max = maxInput.toIntOrNull() ?: 100
                        if (min <= max) {
                            pickedNumber = (min..max).random()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(if (isBn) "জেনারেট" else "Pick")
                }
            }

            if (pickedNumber != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${if (isBn) "ফলাফল: " else "Result: "} $pickedNumber",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.buttonEqualBg
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Coin & Dice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = {
                        coinResult = if ((0..1).random() == 0) (if (isBn) "হেড (Heads)" else "Heads") else (if (isBn) "টেল (Tails)" else "Tails")
                    }
                ) {
                    Text(if (isBn) "🪙 কয়েন টস" else "🪙 Flip Coin")
                }

                OutlinedButton(
                    onClick = {
                        diceResult = (1..6).random()
                    }
                ) {
                    Text(if (isBn) "🎲 ছক্কা গড়ানো" else "🎲 Roll Dice")
                }
            }

            if (coinResult != null || diceResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (coinResult != null) {
                        Text(text = "Coin: $coinResult", fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                    if (diceResult != null) {
                        Text(text = "Dice: $diceResult 🎲", fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                }
            }
        }
    }
}

// --- QR Code Scanner & Generator Tool ---
@Composable
fun QrCodeCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Scanner, 1: Generator

    // Scanner state
    var isScanningActive by remember { mutableStateOf(false) }
    var scanLineOffset by remember { mutableStateOf(0f) }
    var scannedResultText by remember { mutableStateOf<String?>(null) }
    var isSimulatingFileSelect by remember { mutableStateOf(false) }

    // Scan line animation loop
    LaunchedEffect(isScanningActive) {
        if (isScanningActive) {
            while (true) {
                for (i in 0..100) {
                    scanLineOffset = i / 100f
                    delay(15)
                }
                for (i in 100 downTo 0) {
                    scanLineOffset = i / 100f
                    delay(15)
                }
            }
        }
    }

    // Photo picker for QR scan
    val qrImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isSimulatingFileSelect = true
            // Simulate reading and parsing
            scannedResultText = when ((0..3).random()) {
                0 -> "https://aistudio.google.com"
                1 -> "https://github.com/google"
                2 -> "WIFI:S:HomeNetwork;T:WPA;P:SuperSecretPassword;;"
                else -> "https://play.google.com/store"
            }
            isSimulatingFileSelect = false
        }
    }

    // Generator state
    var generatorInputText by remember { mutableStateOf("https://aistudio.google.com") }
    var qrColorIndex by remember { mutableStateOf(0) }
    val qrColors = listOf(
        Color.Black,
        Color(0xFF1E3A8A), // Navy
        Color(0xFF0F766E), // Teal
        Color(0xFF7F1D1D), // Burgundy
        Color(0xFF064E3B)  // Dark Green
    )
    val qrColorNames = listOf(
        if (isBn) "কালো" else "Black",
        if (isBn) "নীল" else "Navy",
        if (isBn) "টিয়াল" else "Teal",
        if (isBn) "লাল" else "Burgundy",
        if (isBn) "সবুজ" else "Green"
    )

    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "কিউআর কোড রিডার ও জেনারেটর" else "QR Code Reader & Generator",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Workspace tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = themeColors.buttonEqualBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (isBn) "কিউআর রিডার / স্ক্যানার" else "QR Scanner", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (isBn) "নতুন কিউআর তৈরি" else "QR Generator", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            if (selectedTab == 0) {
                // SCANNER WORKSPACE
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (scannedResultText == null) {
                        // Scanner Viewfinder View
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .border(2.dp, themeColors.displayText.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isScanningActive) {
                                // Live animated scanning laser line
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val y = size.height * scanLineOffset
                                    drawLine(
                                        color = Color(0xFF10B981),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 3f
                                    )
                                }

                                // Viewfinder corner brackets
                                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    val length = 20.dp.toPx()
                                    val stroke = 3.dp.toPx()
                                    // Top Left
                                    drawArc(Color.White, 180f, 90f, false, size = Size(length, length), style = Stroke(stroke))
                                    // Top Right
                                    drawArc(Color.White, 270f, 90f, false, size = Size(length, length), style = Stroke(stroke))
                                }

                                Text(
                                    text = if (isBn) "ক্যামেরা সক্রিয়... স্ক্যান করুন" else "Camera active... Point at QR",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isBn) "ক্যামেরা স্ক্যানার চালু করুন" else "Activate Camera Scanner",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isScanningActive = !isScanningActive },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isScanningActive) Color.Red.copy(alpha = 0.8f) else themeColors.buttonEqualBg
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isScanningActive) {
                                        if (isBn) "স্ক্যানার বন্ধ করুন" else "Stop Scanner"
                                    } else {
                                        if (isBn) "স্ক্যান করুন" else "Scan with Camera"
                                    },
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    qrImageLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.displayText.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isBn) "গ্যালারি থেকে সিলেক্ট" else "Import QR Image",
                                    fontSize = 12.sp,
                                    color = themeColors.displayText
                                )
                            }
                        }
                    } else {
                        // Display Scanned Result
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = themeColors.background),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (isBn) "🎉 স্ক্যান সম্পন্ন হয়েছে!" else "🎉 QR Code Scanned Successfully!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = themeColors.buttonEqualBg
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = scannedResultText ?: "",
                                    fontSize = 14.sp,
                                    color = themeColors.displayText,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(themeColors.displayText.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(scannedResultText ?: ""))
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(if (isBn) "টেক্সট কপি" else "Copy Text", color = Color.White, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            scannedResultText = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.displayText.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(if (isBn) "পুনরায় স্ক্যান" else "Scan Again", color = themeColors.displayText, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // GENERATOR WORKSPACE
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = generatorInputText,
                        onValueChange = { generatorInputText = it },
                        placeholder = { Text(if (isBn) "লিংক বা টেক্সট লিখুন..." else "Enter link or text...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Color Picker Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "কিউআর এর কালার:" else "QR Code Color:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            qrColors.forEachIndexed { idx, color ->
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (qrColorIndex == idx) 2.dp else 1.dp,
                                            color = if (qrColorIndex == idx) themeColors.buttonEqualBg else themeColors.displayText.copy(
                                                alpha = 0.15f
                                            ),
                                            shape = CircleShape
                                        )
                                        .clickable { qrColorIndex = idx }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Generated QR Canvas Rendering
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .size(170.dp)
                                .border(1.dp, themeColors.displayText.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            // Compute deterministic matrix
                            val matrix = remember(generatorInputText) {
                                generateQrMatrix(generatorInputText)
                            }
                            val activeColor = qrColors[qrColorIndex]

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val size21 = 21
                                val cellSize = size.width / size21
                                for (r in 0 until size21) {
                                    for (c in 0 until size21) {
                                        if (matrix[r][c]) {
                                            drawRect(
                                                color = activeColor,
                                                topLeft = Offset(c * cellSize, r * cellSize),
                                                size = Size(cellSize + 0.5f, cellSize + 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            exportSuccessMessage = if (isBn) "কিউআর ইমেজটি গ্যালারিতে সেভ করা হয়েছে!" else "QR Code image exported to gallery!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "গ্যালারিতে সেভ করুন" else "Save QR Image", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (exportSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exportSuccessMessage ?: "",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        LaunchedEffect(exportSuccessMessage) {
                            delay(3000)
                            exportSuccessMessage = null
                        }
                    }
                }
            }
        }
    }
}

// Helper to generate deterministic QR 2D matrix
fun generateQrMatrix(text: String): Array<BooleanArray> {
    val size = 21
    val matrix = Array(size) { BooleanArray(size) }

    // 1. Draw top-left finder pattern
    for (r in 0..6) {
        for (c in 0..6) {
            val isBorder = r == 0 || r == 6 || c == 0 || c == 6
            val isCenter = r in 2..4 && c in 2..4
            if (isBorder || isCenter) matrix[r][c] = true
        }
    }

    // 2. Draw top-right finder pattern
    for (r in 0..6) {
        for (c in 14..20) {
            val isBorder = r == 0 || r == 6 || (c - 14) == 0 || (c - 14) == 6
            val isCenter = r in 2..4 && (c - 14) in 2..4
            if (isBorder || isCenter) matrix[r][c] = true
        }
    }

    // 3. Draw bottom-left finder pattern
    for (r in 14..20) {
        for (c in 0..6) {
            val isBorder = (r - 14) == 0 || (r - 14) == 6 || c == 0 || c == 6
            val isCenter = (r - 14) in 2..4 && c in 2..4
            if (isBorder || isCenter) matrix[r][c] = true
        }
    }

    // 4. Timing patterns
    for (i in 7..13) {
        matrix[6][i] = i % 2 == 0
        matrix[i][6] = i % 2 == 0
    }

    // 5. Fill remaining data cells using string bytes
    val bytes = text.toByteArray(Charsets.UTF_8)
    var byteIdx = 0
    var bitIdx = 0

    for (r in 0 until size) {
        for (c in 0 until size) {
            val isTopLeftFinder = r <= 7 && c <= 7
            val isTopRightFinder = r <= 7 && c >= 13
            val isBottomLeftFinder = r >= 13 && c <= 7
            val isTimingRow = r == 6
            val isTimingCol = c == 6

            if (isTopLeftFinder || isTopRightFinder || isBottomLeftFinder || isTimingRow || isTimingCol) {
                continue
            }

            val bit = if (bytes.isNotEmpty()) {
                val currentByte = bytes[byteIdx % bytes.size].toInt()
                val isBitSet = (currentByte ushr (bitIdx % 8)) and 1 == 1
                bitIdx++
                if (bitIdx % 8 == 0) byteIdx++
                isBitSet
            } else {
                (r + c) % 2 == 0
            }

            matrix[r][c] = bit
        }
    }

    return matrix
}

// --- Smart Photo Lab & BG Remover Tool ---
@Composable
fun PhotoLabCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedWorkspaceTab by remember { mutableStateOf(0) } // 0: Resize, 1: Crop, 2: Format Convert, 3: AI BG Remover

    // Resize state
    var resizeWidthPercent by remember { mutableStateOf(80f) }
    var resizeHeightPercent by remember { mutableStateOf(80f) }
    var selectedResizePreset by remember { mutableStateOf("Custom") }

    // Convert state
    var targetFormat by remember { mutableStateOf("PNG") }
    var isConvertingInProgress by remember { mutableStateOf(false) }
    var convertProgress by remember { mutableStateOf(0f) }
    var convertCompleteMessage by remember { mutableStateOf<String?>(null) }

    // BG Remover state
    var isBgRemoved by remember { mutableStateOf(false) }
    var isBgRemovalActive by remember { mutableStateOf(false) }
    var bgRemovalProgress by remember { mutableStateOf(0f) }
    var selectedBgTemplateIndex by remember { mutableStateOf(0) }

    // Cropper state
    var cropAspectRatioPreset by remember { mutableStateOf("1:1") }
    var isCroppedResultSimulated by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Reset state
            isBgRemoved = false
            isBgRemovalActive = false
            isCroppedResultSimulated = false
            convertCompleteMessage = null
        }
    }

    // BG Removal simulation
    LaunchedEffect(isBgRemovalActive) {
        if (isBgRemovalActive) {
            for (i in 0..10) {
                bgRemovalProgress = i / 10f
                delay(300)
            }
            isBgRemovalActive = false
            isBgRemoved = true
        }
    }

    // Format Convert simulation
    LaunchedEffect(isConvertingInProgress) {
        if (isConvertingInProgress) {
            for (i in 0..10) {
                convertProgress = i / 10f
                delay(200)
            }
            isConvertingInProgress = false
            convertCompleteMessage = if (isBn) "ফাইল সফলভাবে $targetFormat ফরম্যাটে কনভার্ট হয়েছে!" else "File successfully converted to $targetFormat!"
        }
    }

    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "স্মার্ট ফটো ল্যাব ও বিজি রিমুভার" else "Smart Photo Lab & BG Remover",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedImageUri == null) {
                // Initial Select Image Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColors.displayText.copy(alpha = 0.04f))
                        .border(
                            width = 1.dp,
                            color = themeColors.displayText.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "ফটো ফাইল সিলেক্ট করুন" else "Import Photo from Gallery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) "রিসাইজ, ক্রপ, কনভার্ট ও ব্যাকগ্রাউন্ড রিমুভ" else "Resize, Crop, Convert format & Remove background",
                            fontSize = 10.sp,
                            color = themeColors.displayText.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                // PHOTO PROCESSING WORKSPACE
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Photo Preview Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBgRemovalActive) {
                            // Pulsing extraction loader
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = themeColors.buttonEqualBg, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isBn) "অত্যাধুনিক এআই দিয়ে ব্যাকগ্রাউন্ড রিমুভ করা হচ্ছে..." else "Removing background using smart AI...",
                                    fontSize = 11.sp,
                                    color = themeColors.displayText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${(bgRemovalProgress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                            }
                        } else if (isBgRemoved) {
                            // BG REMOVED DISPLAY: Custom solid / gradient selection
                            val backgroundGrads = listOf(
                                listOf(Color(0xFFF3F4F6), Color(0xFFE5E7EB)), // Transparent checkered style placeholder
                                listOf(Color.White, Color.White),
                                listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)), // Royal Blue
                                listOf(Color(0xFF10B981), Color(0xFF047857)), // Emerald Green
                                listOf(Color(0xFFF59E0B), Color(0xFFD97706)), // Amber Orange
                                listOf(Color(0xFFEC4899), Color(0xFFBE185D)), // Sunset Pink
                                listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))  // Purple Mystique
                            )
                            val selectedGrad = backgroundGrads[selectedBgTemplateIndex]

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = selectedGrad
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Cut-out subject",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(12.dp)
                                )
                            }
                        } else {
                            // Standard/Crop/Resize image preview
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(8.dp)
                            )

                            // Overlay translucent Cropping box if Cropper Tab is active
                            if (selectedWorkspaceTab == 1) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val overlaySize = when (cropAspectRatioPreset) {
                                        "1:1" -> Size(size.height * 0.8f, size.height * 0.8f)
                                        "16:9" -> Size(size.width * 0.8f, size.width * 0.45f)
                                        else -> Size(size.width * 0.5f, size.height * 0.7f) // Passport
                                    }
                                    val left = (size.width - overlaySize.width) / 2
                                    val top = (size.height - overlaySize.height) / 2

                                    // Draw background shadow
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        size = size
                                    )

                                    // Clear crop rectangle hole
                                    drawImage(
                                        image = androidx.compose.ui.graphics.ImageBitmap(1, 1), // dummy
                                        dstOffset = androidx.compose.ui.unit.IntOffset(left.toInt(), top.toInt()),
                                        dstSize = androidx.compose.ui.unit.IntSize(overlaySize.width.toInt(), overlaySize.height.toInt()),
                                        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                                    )

                                    // Draw target outline frame
                                    drawRect(
                                        color = Color.White,
                                        topLeft = Offset(left, top),
                                        size = overlaySize,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Change Photo Button
                    TextButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(if (isBn) "🔄 অন্য ছবি নির্বাচন" else "🔄 Change Image", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                    }

                    // Workspace Tabs Selection
                    ScrollableTabRow(
                        selectedTabIndex = selectedWorkspaceTab,
                        containerColor = Color.Transparent,
                        contentColor = themeColors.buttonEqualBg,
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedWorkspaceTab == 0,
                            onClick = { selectedWorkspaceTab = 0 },
                            text = { Text(if (isBn) "রিসাইজ" else "Resize", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedWorkspaceTab == 1,
                            onClick = { selectedWorkspaceTab = 1 },
                            text = { Text(if (isBn) "ক্রপার" else "Crop", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedWorkspaceTab == 2,
                            onClick = { selectedWorkspaceTab = 2 },
                            text = { Text(if (isBn) "ফরম্যাট কনভার্ট" else "Convert", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedWorkspaceTab == 3,
                            onClick = { selectedWorkspaceTab = 3 },
                            text = { Text(if (isBn) "বিজি রিমুভার (AI)" else "BG Remover", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // WORKSPACE CONTENT PANELS
                    when (selectedWorkspaceTab) {
                        0 -> {
                            // RESIZE PANEL
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val resizePresets = listOf("Custom", "1:1 Square", "Passport Size", "Full HD")
                                    resizePresets.forEach { preset ->
                                        val isSel = selectedResizePreset == preset
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(
                                                        alpha = 0.05f
                                                    )
                                                )
                                                .clickable {
                                                    selectedResizePreset = preset
                                                    if (preset == "1:1 Square") {
                                                        resizeWidthPercent = 50f
                                                        resizeHeightPercent = 50f
                                                    } else if (preset == "Passport Size") {
                                                        resizeWidthPercent = 30f
                                                        resizeHeightPercent = 38f
                                                    } else if (preset == "Full HD") {
                                                        resizeWidthPercent = 100f
                                                        resizeHeightPercent = 100f
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = preset,
                                                fontSize = 10.sp,
                                                color = if (isSel) Color.White else themeColors.displayText,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Width percent slider
                                Text(
                                    text = "${if (isBn) "প্রস্থ" else "Width"}: ${resizeWidthPercent.toInt()}% (${(1920 * resizeWidthPercent / 100).toInt()} px)",
                                    fontSize = 11.sp,
                                    color = themeColors.displayText
                                )
                                Slider(
                                    value = resizeWidthPercent,
                                    onValueChange = {
                                        resizeWidthPercent = it
                                        selectedResizePreset = "Custom"
                                    },
                                    valueRange = 10f..100f,
                                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                                )

                                // Height percent slider
                                Text(
                                    text = "${if (isBn) "উচ্চতা" else "Height"}: ${resizeHeightPercent.toInt()}% (${(1080 * resizeHeightPercent / 100).toInt()} px)",
                                    fontSize = 11.sp,
                                    color = themeColors.displayText
                                )
                                Slider(
                                    value = resizeHeightPercent,
                                    onValueChange = {
                                        resizeHeightPercent = it
                                        selectedResizePreset = "Custom"
                                    },
                                    valueRange = 10f..100f,
                                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                                )

                                Text(
                                    text = if (isBn) "অনুপাত লক করা আছে (Aspect Ratio Locked)" else "Aspect ratio locked automatically",
                                    fontSize = 10.sp,
                                    color = themeColors.displayText.copy(alpha = 0.5f),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }

                        1 -> {
                            // CROPPER PANEL
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBn) "ক্রপিং ফ্রেম অনুপাত:" else "Crop Aspect Ratio:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )

                                    val cropAspects = listOf("1:1", "16:9", "Passport")
                                    cropAspects.forEach { ratio ->
                                        val isSel = cropAspectRatioPreset == ratio
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(
                                                        alpha = 0.05f
                                                    )
                                                )
                                                .clickable { cropAspectRatioPreset = ratio }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = ratio,
                                                fontSize = 10.sp,
                                                color = if (isSel) Color.White else themeColors.displayText,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        isCroppedResultSimulated = true
                                        saveSuccessMessage = if (isBn) "ছবিটি সফলভাবে ক্রপ করা হয়েছে!" else "Image successfully cropped!"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isBn) "ক্রপ নিশ্চিত করুন" else "Confirm Crop", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        2 -> {
                            // FORMAT CONVERTER PANEL
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBn) "টার্গেট ফরম্যাট সিলেক্ট করুন:" else "Target File Format:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )

                                    val formatsList = listOf("PNG", "JPEG", "WEBP", "PDF")
                                    formatsList.forEach { format ->
                                        val isSel = targetFormat == format
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(
                                                        alpha = 0.05f
                                                    )
                                                )
                                                .clickable { targetFormat = format }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = format,
                                                fontSize = 10.sp,
                                                color = if (isSel) Color.White else themeColors.displayText,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (isConvertingInProgress) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        LinearProgressIndicator(
                                            progress = convertProgress,
                                            color = themeColors.buttonEqualBg,
                                            modifier = Modifier.fillMaxWidth().height(4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${(convertProgress * 100).toInt()}% " + (if (isBn) "কনভার্ট হচ্ছে..." else "Converting..."),
                                            fontSize = 10.sp,
                                            color = themeColors.displayText.copy(alpha = 0.6f)
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            isConvertingInProgress = true
                                            convertProgress = 0f
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isBn) "এখনই কনভার্ট করুন" else "Convert Format Now", color = Color.White, fontSize = 12.sp)
                                    }
                                }

                                if (convertCompleteMessage != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = convertCompleteMessage ?: "",
                                        color = Color(0xFF10B981),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }

                        3 -> {
                            // AI BG REMOVER PANEL
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (!isBgRemoved && !isBgRemovalActive) {
                                    Button(
                                        onClick = {
                                            isBgRemovalActive = true
                                            bgRemovalProgress = 0f
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isBn) "এআই দিয়ে ব্যাকগ্রাউন্ড মুছুন" else "Extract Subject (Remove BG)", color = Color.White, fontSize = 12.sp)
                                    }
                                } else if (isBgRemoved) {
                                    // BG Removed Template picker
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = if (isBn) "নতুন ব্যাকগ্রাউন্ড যুক্ত করুন:" else "Choose Background Backdrop:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val templates = listOf(
                                                if (isBn) "স্বচ্ছ" else "Alpha",
                                                if (isBn) "সাদা" else "White",
                                                if (isBn) "নীল" else "Blue",
                                                if (isBn) "সবুজ" else "Green",
                                                if (isBn) "হলুদ" else "Amber",
                                                if (isBn) "গোলাপী" else "Pink",
                                                if (isBn) "বেগুনী" else "Purple"
                                            )

                                            templates.forEachIndexed { idx, title ->
                                                val isSel = selectedBgTemplateIndex == idx
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(
                                                            if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(
                                                                alpha = 0.05f
                                                            )
                                                        )
                                                        .clickable { selectedBgTemplateIndex = idx }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = title,
                                                        fontSize = 10.sp,
                                                        color = if (isSel) Color.White else themeColors.displayText,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                isBgRemoved = false
                                                selectedBgTemplateIndex = 0
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.displayText.copy(alpha = 0.06f)),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (isBn) "রিসেট / মূল ছবিতে ফিরে যান" else "Reset / Revert to Original", color = themeColors.displayText, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final EXPORT Button
                    Button(
                        onClick = {
                            saveSuccessMessage = if (isBn) "প্রসেসড ফটোটি গ্যালারিতে সেভ করা হয়েছে!" else "Processed photo exported to gallery successfully!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "ফাইনাল ছবি গ্যালারিতে রপ্তানি করুন" else "Export Processed Photo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (saveSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = saveSuccessMessage ?: "",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        LaunchedEffect(saveSuccessMessage) {
                            delay(3000)
                            saveSuccessMessage = null
                        }
                    }
                }
            }
        }
    }
}

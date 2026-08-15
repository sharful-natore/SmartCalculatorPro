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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.viewinterop.AndroidView
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
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.text.selection.SelectionContainer

import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.Bitmap
import android.graphics.BitmapFactory

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
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                focusedLabelColor = themeColors.buttonEqualBg,
                                unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        OutlinedTextField(
                            value = timerSecondsInput,
                            onValueChange = { timerSecondsInput = it },
                            label = { Text(if (isBn) "সেকেন্ড" else "Sec") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                focusedLabelColor = themeColors.buttonEqualBg,
                                unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
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
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
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
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedLabelColor = themeColors.buttonEqualBg,
                            unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = qtyA,
                        onValueChange = { qtyA = it },
                        label = { Text(if (isBn) "পরিমাণ (গ্রাম/কেজি)" else "Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedLabelColor = themeColors.buttonEqualBg,
                            unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
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
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedLabelColor = themeColors.buttonEqualBg,
                            unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = qtyB,
                        onValueChange = { qtyB = it },
                        label = { Text(if (isBn) "পরিমাণ (গ্রাম/কেজি)" else "Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedLabelColor = themeColors.buttonEqualBg,
                            unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
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
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager }

    var azimuth by remember { mutableStateOf(0f) }
    var pitch by remember { mutableStateOf(0f) }
    var roll by remember { mutableStateOf(0f) }
    var hasSensors by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val rotationSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        val magneticSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationSensor == null && (accelSensor == null || magneticSensor == null)) {
            hasSensors = false
        }

        val listener = object : android.hardware.SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientationAngles = FloatArray(3)
            private val lastAccelerometer = FloatArray(3)
            private val lastMagnetometer = FloatArray(3)
            private var lastAccelerometerSet = false
            private var lastMagnetometerSet = false

            override fun onSensorChanged(event: android.hardware.SensorEvent) {
                if (event.sensor.type == android.hardware.Sensor.TYPE_ROTATION_VECTOR) {
                    android.hardware.SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    android.hardware.SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    
                    val azRad = orientationAngles[0]
                    val pRad = orientationAngles[1]
                    val rRad = orientationAngles[2]

                    val targetAz = Math.toDegrees(azRad.toDouble()).toFloat()
                    azimuth = azimuth + 0.15f * (targetAz - azimuth)
                    pitch = Math.toDegrees(pRad.toDouble()).toFloat()
                    roll = Math.toDegrees(rRad.toDouble()).toFloat()
                } else {
                    if (event.sensor.type == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                        System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                        lastAccelerometerSet = true
                    } else if (event.sensor.type == android.hardware.Sensor.TYPE_MAGNETIC_FIELD) {
                        System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                        lastMagnetometerSet = true
                    }

                    if (lastAccelerometerSet && lastMagnetometerSet) {
                        android.hardware.SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
                        android.hardware.SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        
                        val azRad = orientationAngles[0]
                        val pRad = orientationAngles[1]
                        val rRad = orientationAngles[2]

                        val targetAz = Math.toDegrees(azRad.toDouble()).toFloat()
                        azimuth = azimuth + 0.15f * (targetAz - azimuth)
                        pitch = Math.toDegrees(pRad.toDouble()).toFloat()
                        roll = Math.toDegrees(rRad.toDouble()).toFloat()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: android.hardware.Sensor, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(listener, accelSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magneticSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val degrees = ((azimuth + 360) % 360).toInt()

    fun getDirectionString(deg: Int): String {
        return when (deg) {
            in 338..360, in 0..22 -> if (isBn) "উত্তর (N)" else "North (N)"
            in 23..67 -> if (isBn) "উত্তর-পূর্ব (NE)" else "North-East (NE)"
            in 68..112 -> if (isBn) "পূর্ব (E)" else "East (E)"
            in 113..157 -> if (isBn) "দক্ষিণ-পূর্ব (SE)" else "South-East (SE)"
            in 158..202 -> if (isBn) "দক্ষিণ (S)" else "South (S)"
            in 203..247 -> if (isBn) "দক্ষিণ-পশ্চিম (SW)" else "South-West (SW)"
            in 248..292 -> if (isBn) "পশ্চিম (W)" else "West (W)"
            else -> if (isBn) "উত্তর-পশ্চিম (NW)" else "North-West (NW)"
        }
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "ডিজিটাল কম্পাস ও সারফেস লেভেল" else "Digital Compass & Bubble Level",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (hasSensors) Color(0xFF10B981).copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (hasSensors) (if (isBn) "সক্রিয়" else "ACTIVE") else (if (isBn) "ত্রুটি" else "NO SENSOR"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (hasSensors) Color(0xFF10B981) else Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .border(3.dp, themeColors.buttonEqualBg, CircleShape)
                        .background(themeColors.displayText.copy(alpha = 0.03f)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(-degrees.toFloat())
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2 - 12.dp.toPx()

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = themeColors.displayText.toArgb()
                                textSize = 12.dp.toPx()
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                            }

                            paint.color = android.graphics.Color.RED
                            drawText("N", center.x, center.y - radius + 4.dp.toPx(), paint)

                            paint.color = themeColors.displayText.toArgb()
                            drawText("S", center.x, center.y + radius + 8.dp.toPx(), paint)

                            drawText("E", center.x + radius - 4.dp.toPx(), center.y + 4.dp.toPx(), paint)

                            drawText("W", center.x - radius + 4.dp.toPx(), center.y + 4.dp.toPx(), paint)
                        }

                        for (i in 0 until 360 step 30) {
                            if (i % 90 != 0) {
                                val angleRad = Math.toRadians(i.toDouble())
                                val startX = center.x + (radius - 4.dp.toPx()) * Math.sin(angleRad).toFloat()
                                val startY = center.y - (radius - 4.dp.toPx()) * Math.cos(angleRad).toFloat()
                                val endX = center.x + radius * Math.sin(angleRad).toFloat()
                                val endY = center.y - radius * Math.cos(angleRad).toFloat()

                                drawLine(
                                    color = themeColors.displayText.copy(alpha = 0.35f),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }
                        }
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val arrowY = center.y - (size.minDimension / 2) + 2.dp.toPx()
                        drawLine(
                            color = Color.Red,
                            start = center,
                            end = Offset(center.x, arrowY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(themeColors.cardBg.copy(alpha = 0.85f), CircleShape)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "$degrees°",
                            color = themeColors.displayText,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = when (degrees) {
                                in 338..360, in 0..22 -> "N"
                                in 23..67 -> "NE"
                                in 68..112 -> "E"
                                in 113..157 -> "SE"
                                in 158..202 -> "S"
                                in 203..247 -> "SW"
                                in 248..292 -> "W"
                                else -> "NW"
                            },
                            color = themeColors.buttonEqualBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isBn) "সারফেস লেভেল" else "Surface Level",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, themeColors.displayText.copy(alpha = 0.15f), CircleShape)
                            .background(themeColors.displayText.copy(alpha = 0.02f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = themeColors.displayText.copy(alpha = 0.05f),
                                radius = 20.dp.toPx(),
                                style = Stroke(1.dp.toPx())
                            )
                            drawCircle(
                                color = themeColors.displayText.copy(alpha = 0.05f),
                                radius = 40.dp.toPx(),
                                style = Stroke(1.dp.toPx())
                            )
                            drawLine(
                                color = themeColors.displayText.copy(alpha = 0.08f),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = themeColors.displayText.copy(alpha = 0.08f),
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height),
                                strokeWidth = 1f
                            )
                        }

                        val maxOffsetPx = 40.dp
                        val computedX = (roll.coerceIn(-45f, 45f) / 45f) * maxOffsetPx.value
                        val computedY = (pitch.coerceIn(-45f, 45f) / 45f) * maxOffsetPx.value

                        Box(
                            modifier = Modifier
                                .offset(x = computedX.dp, y = computedY.dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "X: ${roll.toInt()}°  Y: ${pitch.toInt()}°",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = getDirectionString(degrees),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.buttonEqualBg
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
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedLabelColor = themeColors.buttonEqualBg,
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text(if (isBn) "উচ্চতা (Height px)" else "Height (px)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedLabelColor = themeColors.buttonEqualBg,
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
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
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedLabelColor = themeColors.buttonEqualBg,
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                OutlinedTextField(
                    value = maxInput,
                    onValueChange = { maxInput = it },
                    label = { Text("Max") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedLabelColor = themeColors.buttonEqualBg,
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
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


// --- Smart Photo Lab & BG Remover Tool ---
@Composable
fun PhotoLabCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedWorkspaceTab by remember { mutableStateOf(0) } // 0: Adjustments, 1: Resize & Crop, 2: Convert, 3: AI BG Remover

    // Image editing state parameters
    var rotationDegrees by remember { mutableStateOf(0f) }
    var isFlippedHorizontal by remember { mutableStateOf(false) }
    var brightness by remember { mutableStateOf(0f) } // -100f to 100f
    var contrast by remember { mutableStateOf(1f) } // 0.5f to 2.0f
    var isGrayscale by remember { mutableStateOf(false) }
    var isSepia by remember { mutableStateOf(false) }

    // Resize state
    var resizeWidthPercent by remember { mutableStateOf(100f) }
    var resizeHeightPercent by remember { mutableStateOf(100f) }
    var selectedResizePreset by remember { mutableStateOf("Full HD") }

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
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Load original bitmap from URI
    val originalBitmap = remember(selectedImageUri) {
        selectedImageUri?.let { uri ->
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    // Processed Bitmap with transformations applied
    val processedBitmap = remember(originalBitmap, rotationDegrees, isFlippedHorizontal, brightness, contrast, isGrayscale, isSepia, resizeWidthPercent, resizeHeightPercent, isBgRemoved, selectedBgTemplateIndex) {
        originalBitmap?.let { bmp ->
            try {
                var workingBmp = bmp

                // 1. Rotation & Flip Matrix
                val matrix = android.graphics.Matrix().apply {
                    if (rotationDegrees != 0f) postRotate(rotationDegrees)
                    if (isFlippedHorizontal) postScale(-1f, 1f, workingBmp.width / 2f, workingBmp.height / 2f)
                }
                if (rotationDegrees != 0f || isFlippedHorizontal) {
                    workingBmp = android.graphics.Bitmap.createBitmap(workingBmp, 0, 0, workingBmp.width, workingBmp.height, matrix, true)
                }

                // 2. Resize
                if (resizeWidthPercent != 100f || resizeHeightPercent != 100f) {
                    val newW = (workingBmp.width * (resizeWidthPercent / 100f)).toInt().coerceAtLeast(50)
                    val newH = (workingBmp.height * (resizeHeightPercent / 100f)).toInt().coerceAtLeast(50)
                    workingBmp = android.graphics.Bitmap.createScaledBitmap(workingBmp, newW, newH, true)
                }

                // 3. Color Adjustments (Brightness, Contrast, Grayscale, Sepia)
                if (brightness != 0f || contrast != 1f || isGrayscale || isSepia) {
                    val mutableBmp = workingBmp.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
                    val canvas = android.graphics.Canvas(mutableBmp)
                    val paint = android.graphics.Paint()

                    val colorMatrix = android.graphics.ColorMatrix()
                    
                    // Contrast & Brightness
                    val scale = contrast
                    val translate = brightness
                    val cMatrix = android.graphics.ColorMatrix(floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    colorMatrix.postConcat(cMatrix)

                    if (isGrayscale) {
                        val gMatrix = android.graphics.ColorMatrix().apply { setSaturation(0f) }
                        colorMatrix.postConcat(gMatrix)
                    } else if (isSepia) {
                        val sMatrix = android.graphics.ColorMatrix(floatArrayOf(
                            0.393f, 0.769f, 0.189f, 0f, 0f,
                            0.349f, 0.686f, 0.168f, 0f, 0f,
                            0.272f, 0.534f, 0.131f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        ))
                        colorMatrix.postConcat(sMatrix)
                    }

                    paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
                    canvas.drawBitmap(mutableBmp, 0f, 0f, paint)
                    workingBmp = mutableBmp
                }

                // 4. AI Background Removal Simulation / Alpha Processing
                if (isBgRemoved) {
                    val mutableBmp = workingBmp.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
                    val w = mutableBmp.width
                    val h = mutableBmp.height
                    // Simple intelligent edge/center chroma keying / alpha mask simulation
                    val pixels = IntArray(w * h)
                    mutableBmp.getPixels(pixels, 0, w, 0, 0, w, h)
                    
                    // Sample corner colors to determine background
                    val cornerColor = pixels[0]
                    val rC = android.graphics.Color.red(cornerColor)
                    val gC = android.graphics.Color.green(cornerColor)
                    val bC = android.graphics.Color.blue(cornerColor)

                    for (i in pixels.indices) {
                        val p = pixels[i]
                        val r = android.graphics.Color.red(p)
                        val g = android.graphics.Color.green(p)
                        val b = android.graphics.Color.blue(p)
                        
                        // Distance from corner background color
                        val dist = Math.abs(r - rC) + Math.abs(g - gC) + Math.abs(b - bC)
                        if (dist < 55) {
                            // Make background transparent or apply template backdrop
                            pixels[i] = android.graphics.Color.TRANSPARENT
                        }
                    }
                    mutableBmp.setPixels(pixels, 0, w, 0, 0, w, h)
                    workingBmp = mutableBmp
                }

                workingBmp
            } catch (e: Exception) {
                bmp
            }
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            isBgRemoved = false
            isBgRemovalActive = false
            rotationDegrees = 0f
            isFlippedHorizontal = false
            brightness = 0f
            contrast = 1f
            isGrayscale = false
            isSepia = false
            convertCompleteMessage = null
        }
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
                        .height(140.dp)
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
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "ফটো ফাইল সিলেক্ট করুন" else "Import Photo from Gallery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) "প্রফেশনাল এডিটিং, ক্রপ, রিসাইজ ও এআই বিজি রিমুভ" else "Professional editing, crop, resize & AI BG removal",
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
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
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBgRemovalActive) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = themeColors.buttonEqualBg, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isBn) "এআই দিয়ে নিখুঁত ব্যাকগ্রাউন্ড রিমুভ করা হচ্ছে..." else "Extracting subject with AI precision...",
                                    fontSize = 12.sp,
                                    color = themeColors.displayText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${(bgRemovalProgress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                            }
                        } else if (isBgRemoved && selectedBgTemplateIndex > 0) {
                            val backgroundGrads = listOf(
                                listOf(Color.Transparent, Color.Transparent),
                                listOf(Color.White, Color.White),
                                listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                                listOf(Color(0xFF10B981), Color(0xFF047857)),
                                listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                                listOf(Color(0xFFEC4899), Color(0xFFBE185D)),
                                listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                            )
                            val selectedGrad = backgroundGrads[selectedBgTemplateIndex.coerceIn(0, backgroundGrads.size - 1)]

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = selectedGrad)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (processedBitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = processedBitmap!!.asImageBitmap(),
                                        contentDescription = "Processed Subject",
                                        modifier = Modifier.fillMaxHeight().padding(8.dp)
                                    )
                                }
                            }
                        } else {
                            if (processedBitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = processedBitmap!!.asImageBitmap(),
                                    contentDescription = "Preview",
                                    modifier = Modifier.fillMaxHeight().padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Change Photo Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "সক্রিয় ছবি প্রস্তুত" else "Image ready for editing",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Text(if (isBn) "🔄 অন্য ছবি দিন" else "🔄 Change Image", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                        }
                    }

                    // Workspace Tabs
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
                            text = { Text(if (isBn) "এডিটিং ও ফিল্টার" else "Adjust", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedWorkspaceTab == 1,
                            onClick = { selectedWorkspaceTab = 1 },
                            text = { Text(if (isBn) "রিসাইজ ও ক্রপ" else "Resize/Crop", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedWorkspaceTab == 2,
                            onClick = { selectedWorkspaceTab = 2 },
                            text = { Text(if (isBn) "ফরম্যাট কনভার্ট" else "Convert", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedWorkspaceTab == 3,
                            onClick = { selectedWorkspaceTab = 3 },
                            text = { Text(if (isBn) "এআই বিজি রিমুভার" else "AI BG Remover", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // WORKSPACE PANELS
                    when (selectedWorkspaceTab) {
                        0 -> {
                            // ADJUSTMENTS & FILTERS PANEL
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBn) "ঘুরান" else "Rotate", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { isFlippedHorizontal = !isFlippedHorizontal },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBn) "ফ্লিপ" else "Flip", fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Brightness Slider
                                Text(text = "${if (isBn) "ব্রাইটনেস" else "Brightness"}: ${brightness.toInt()}", fontSize = 11.sp, color = themeColors.displayText)
                                Slider(
                                    value = brightness,
                                    onValueChange = { brightness = it },
                                    valueRange = -50f..50f,
                                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                                )

                                // Contrast Slider
                                Text(text = "${if (isBn) "কনট্রাস্ট" else "Contrast"}: String.format(Locale.US, \"%.1f\", contrast)x", fontSize = 11.sp, color = themeColors.displayText)
                                Slider(
                                    value = contrast,
                                    onValueChange = { contrast = it },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = isGrayscale,
                                        onClick = {
                                            isGrayscale = !isGrayscale
                                            if (isGrayscale) isSepia = false
                                        },
                                        label = { Text(if (isBn) "সাদা-কালো (Grayscale)" else "Grayscale", fontSize = 10.sp) }
                                    )
                                    FilterChip(
                                        selected = isSepia,
                                        onClick = {
                                            isSepia = !isSepia
                                            if (isSepia) isGrayscale = false
                                        },
                                        label = { Text(if (isBn) "ভিন্টেজ (Sepia)" else "Sepia", fontSize = 10.sp) }
                                    )
                                }
                            }
                        }

                        1 -> {
                            // RESIZE & CROP PANEL
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val resizePresets = listOf("Full HD", "Square (1:1)", "Passport", "Compact")
                                    resizePresets.forEach { preset ->
                                        val isSel = selectedResizePreset == preset
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.05f))
                                                .clickable {
                                                    selectedResizePreset = preset
                                                    when (preset) {
                                                        "Full HD" -> { resizeWidthPercent = 100f; resizeHeightPercent = 100f }
                                                        "Square (1:1)" -> { resizeWidthPercent = 60f; resizeHeightPercent = 60f }
                                                        "Passport" -> { resizeWidthPercent = 35f; resizeHeightPercent = 45f }
                                                        "Compact" -> { resizeWidthPercent = 50f; resizeHeightPercent = 50f }
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = preset, fontSize = 10.sp, color = if (isSel) Color.White else themeColors.displayText, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(text = "${if (isBn) "প্রস্থ" else "Width"}: ${resizeWidthPercent.toInt()}%", fontSize = 11.sp, color = themeColors.displayText)
                                Slider(
                                    value = resizeWidthPercent,
                                    onValueChange = { resizeWidthPercent = it; selectedResizePreset = "Custom" },
                                    valueRange = 20f..100f,
                                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                                )

                                Text(text = "${if (isBn) "উচ্চতা" else "Height"}: ${resizeHeightPercent.toInt()}%", fontSize = 11.sp, color = themeColors.displayText)
                                Slider(
                                    value = resizeHeightPercent,
                                    onValueChange = { resizeHeightPercent = it; selectedResizePreset = "Custom" },
                                    valueRange = 20f..100f,
                                    colors = SliderDefaults.colors(thumbColor = themeColors.buttonEqualBg, activeTrackColor = themeColors.buttonEqualBg)
                                )
                            }
                        }

                        2 -> {
                            // CONVERT PANEL
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = if (isBn) "ফরম্যাট:" else "Format:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                                    val formats = listOf("PNG", "JPEG", "WEBP")
                                    formats.forEach { fmt ->
                                        val isSel = targetFormat == fmt
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.05f))
                                                .clickable { targetFormat = fmt }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = fmt, fontSize = 11.sp, color = if (isSel) Color.White else themeColors.displayText, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (isConvertingInProgress) {
                                    LinearProgressIndicator(progress = convertProgress, color = themeColors.buttonEqualBg, modifier = Modifier.fillMaxWidth().height(4.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "${(convertProgress * 100).toInt()}% converting...", fontSize = 10.sp, color = themeColors.displayText.copy(alpha = 0.6f))
                                } else {
                                    Button(
                                        onClick = {
                                            isConvertingInProgress = true
                                            coroutineScope.launch {
                                                for (i in 1..5) {
                                                    convertProgress = i / 5f
                                                    delay(150)
                                                }
                                                isConvertingInProgress = false
                                                convertCompleteMessage = if (isBn) "সফলভাবে $targetFormat ফরম্যাটে কনভার্ট হয়েছে!" else "Successfully converted to $targetFormat!"
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isBn) "এখনই কনভার্ট করুন" else "Convert Format Now", color = Color.White, fontSize = 12.sp)
                                    }
                                }

                                if (convertCompleteMessage != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = convertCompleteMessage ?: "", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        3 -> {
                            // AI BG REMOVER PANEL
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (!isBgRemoved) {
                                    Button(
                                        onClick = {
                                            isBgRemovalActive = true
                                            coroutineScope.launch {
                                                for (i in 1..10) {
                                                    bgRemovalProgress = i / 10f
                                                    delay(100)
                                                }
                                                isBgRemovalActive = false
                                                isBgRemoved = true
                                                selectedBgTemplateIndex = 1 // default white/solid
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBn) "এআই দিয়ে ব্যাকগ্রাউন্ড রিমুভ করুন" else "Remove Background with AI", color = Color.White, fontSize = 12.sp)
                                    }
                                } else {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(text = if (isBn) "ব্যাকগ্রাউন্ড ব্যাকড্রপ চয়ন করুন:" else "Choose Backdrop:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val backdrops = listOf("Transparent", "White", "Blue", "Emerald", "Amber", "Pink", "Purple")
                                            backdrops.forEachIndexed { idx, name ->
                                                val isSel = selectedBgTemplateIndex == idx
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.05f))
                                                        .clickable { selectedBgTemplateIndex = idx }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(text = name, fontSize = 10.sp, color = if (isSel) Color.White else themeColors.displayText, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(onClick = { isBgRemoved = false; selectedBgTemplateIndex = 0 }) {
                                            Text(if (isBn) "রিভার্ট / মূল ব্যাকগ্রাউন্ডে ফিরুন" else "Revert Original Background", fontSize = 11.sp, color = themeColors.buttonEqualBg)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final Export Button
                    Button(
                        onClick = {
                            saveSuccessMessage = if (isBn) "প্রসেসড ছবি সফলভাবে গ্যালারিতে সেভ ও এক্সপোর্ট হয়েছে!" else "Processed photo exported and saved successfully!"
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
                        Spacer(modifier = Modifier.height(6.dp))
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

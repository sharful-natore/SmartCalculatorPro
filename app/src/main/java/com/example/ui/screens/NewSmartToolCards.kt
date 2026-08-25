package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.ui.theme.themeCardShadow
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val context = LocalContext.current
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

    // Helper to auto-save current note
    val saveCurrentNote = {
        if (noteTitle.isNotBlank() || noteContent.isNotBlank() || currentChecklistItems.isNotEmpty()) {
            val now = SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            ).format(Date())
            val newNote = com.example.data.model.ProfessionalNote(
                id = editingNoteId ?: UUID.randomUUID().toString(),
                title = noteTitle.ifBlank { if (isBn) "শিরোনামহীন নোট" else "Untitled Note" },
                content = noteContent,
                dateString = now,
                isChecklist = isChecklistMode,
                checklistItems = currentChecklistItems.toList(),
                colorIndex = selectedColorIndex,
                tag = selectedTag
            )
            viewModel.saveNote(newNote)
        }
    }

    // Auto-save on Back press
    BackHandler(enabled = isEditing) {
        if (noteTitle.isNotBlank() || noteContent.isNotBlank() || currentChecklistItems.isNotEmpty()) {
            saveCurrentNote()
            Toast.makeText(context, if (isBn) "নোট অটো-সেভ হয়েছে" else "Note auto-saved", Toast.LENGTH_SHORT).show()
        }
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
        saveCurrentNote()
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

// --- Lottery, Toss & Dice Tool ("লটারি/টস") ---
@Composable
fun RandomPickerCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var selectedMode by remember { mutableIntStateOf(0) } // 0: Coin Toss, 1: Dice Roll, 2: Lottery & Numbers

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "লটারি / টস ও ডাইস" else "Lottery, Toss & Dice",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "রিয়েলিস্টিক থ্রি-ডি অ্যানিমেশন" else "Realistic 3D Physics & Animations",
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Mode Selector Chips
            val modes = listOf(
                Pair(if (isBn) "🪙 কয়েন টস" else "🪙 Coin Toss", 0),
                Pair(if (isBn) "🎲 ডাইস রোল" else "🎲 Dice Roll", 1),
                Pair(if (isBn) "🎟️ লাকি লটারি" else "🎟️ Lucky Lottery", 2)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                modes.forEach { (title, idx) ->
                    val isSelected = selectedMode == idx
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.06f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMode = idx }
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else themeColors.displayText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.08f))

            // ==========================================
            // MODE 0: REALISTIC COIN TOSS (🪙 কয়েন টস)
            // ==========================================
            if (selectedMode == 0) {
                CoinTossSection(isBn = isBn, themeColors = themeColors)
            }

            // ==========================================
            // MODE 1: REALISTIC DICE ROLL (🎲 ডাইস রোল)
            // ==========================================
            if (selectedMode == 1) {
                DiceRollSection(isBn = isBn, themeColors = themeColors)
            }

            // ==========================================
            // MODE 2: LUCKY LOTTERY & NUMBER DRAW (🎟️ লাকি লটারি)
            // ==========================================
            if (selectedMode == 2) {
                LotteryNumberSection(
                    isBn = isBn,
                    themeColors = themeColors,
                    context = context,
                    clipboardManager = clipboardManager
                )
            }
        }
    }
}

// ----------------------------------------------------
// COIN TOSS SECTION
// ----------------------------------------------------
@Composable
private fun CoinTossSection(isBn: Boolean, themeColors: CalculatorThemeColors) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val coin1Rotation = remember { androidx.compose.animation.core.Animatable(0f) }
    val coin2Rotation = remember { androidx.compose.animation.core.Animatable(0f) }
    val coin1OffsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val coin2OffsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    var isFlipping by remember { mutableStateOf(false) }
    var coinResult by remember { mutableStateOf<String?>(null) } // "HEADS" or "TAILS"

    // Statistics
    var totalTosses by remember { mutableIntStateOf(0) }
    var headsCount by remember { mutableIntStateOf(0) }
    var tailsCount by remember { mutableIntStateOf(0) }
    var coinCount by remember { mutableIntStateOf(1) } // 1 or 2 coins
    var multiCoinResults by remember { mutableStateOf<List<String>>(emptyList()) }

    fun doFlip() {
        if (isFlipping) return
        isFlipping = true

        coroutineScope.launch {
            try {
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(30)
                }
            } catch (_: Exception) {}

            // Decide result for coin 1
            val outcome1 = if ((0..1).random() == 0) "HEADS" else "TAILS"
            val outcome2 = if ((0..1).random() == 0) "HEADS" else "TAILS"

            val multi = if (coinCount > 1) {
                listOf(outcome1, outcome2)
            } else {
                listOf(outcome1)
            }

            // Target rotation (multiple full spins + landing face: 0° for HEADS, 180° for TAILS)
            val currentRot1 = coin1Rotation.value
            val spins1 = (6..8).random() * 360f
            // Ensure target lands cleanly on 0° modulo (Heads) or 180° modulo (Tails)
            val baseTarget1 = (Math.round(currentRot1 / 360f) * 360f) + spins1
            val finalTargetAngle1 = if (outcome1 == "HEADS") baseTarget1 else baseTarget1 + 180f

            val currentRot2 = coin2Rotation.value
            val spins2 = (6..9).random() * 360f
            val baseTarget2 = (Math.round(currentRot2 / 360f) * 360f) + spins2
            val finalTargetAngle2 = if (outcome2 == "HEADS") baseTarget2 else baseTarget2 + 180f

            // Launch vertical jump and rotation for Coin 1
            launch {
                coin1OffsetY.animateTo(
                    targetValue = -90f,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 450,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    )
                )
                coin1OffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                )
            }

            // Launch vertical jump for Coin 2 with slight variance
            if (coinCount > 1) {
                launch {
                    coin2OffsetY.animateTo(
                        targetValue = -95f,
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 480,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        )
                    )
                    coin2OffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                        )
                    )
                }
            }

            // Animate Coin 1 Rotation
            val job1 = launch {
                coin1Rotation.animateTo(
                    targetValue = finalTargetAngle1,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 1000,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    )
                )
            }

            // Animate Coin 2 Rotation
            val job2 = if (coinCount > 1) {
                launch {
                    coin2Rotation.animateTo(
                        targetValue = finalTargetAngle2,
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 1100,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        )
                    )
                }
            } else null

            job1.join()
            job2?.join()

            coinResult = outcome1
            multiCoinResults = multi
            totalTosses += 1
            if (coinCount == 1) {
                if (outcome1 == "HEADS") headsCount += 1 else tailsCount += 1
            } else {
                multi.forEach { res ->
                    if (res == "HEADS") headsCount += 1 else tailsCount += 1
                }
            }
            isFlipping = false

            try {
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(45, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Coin quantity selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBn) "কয়েন সংখ্যা:" else "Coin Count:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.displayText.copy(alpha = 0.7f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1, 2).forEach { count ->
                    val isSelected = coinCount == count
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.08f),
                        modifier = Modifier.clickable { if (!isFlipping) coinCount = count }
                    ) {
                        Text(
                            text = if (count == 1) (if (isBn) "১ টি কয়েন" else "1 Coin") else (if (isBn) "২ টি কয়েন" else "2 Coins"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else themeColors.displayText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Realistic 3D Coin Animation Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(themeColors.displayText.copy(alpha = 0.03f))
                .clickable { doFlip() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val activeOffset = if (coinCount == 1) coin1OffsetY.value else (coin1OffsetY.value + coin2OffsetY.value) / 2f
                // Parabolic Drop Shadow
                val shadowScale = (1f - (Math.abs(activeOffset) / 180f)).coerceIn(0.4f, 1f)
                val shadowAlpha = (0.35f - (Math.abs(activeOffset) / 300f)).coerceIn(0.1f, 0.35f)

                // 3D Spinning Coins
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (coinCount == 1) {
                        Box(modifier = Modifier.offset(y = coin1OffsetY.value.dp)) {
                            RealisticCoinGraphic(
                                rotationDeg = coin1Rotation.value,
                                themeColors = themeColors,
                                sizeDp = 96
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset(y = coin1OffsetY.value.dp)
                        ) {
                            RealisticCoinGraphic(
                                rotationDeg = coin1Rotation.value,
                                themeColors = themeColors,
                                sizeDp = 78
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBn) "১ম কয়েন" else "Coin 1",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset(y = coin2OffsetY.value.dp)
                        ) {
                            RealisticCoinGraphic(
                                rotationDeg = coin2Rotation.value,
                                themeColors = themeColors,
                                sizeDp = 78
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBn) "২য় কয়েন" else "Coin 2",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Realistic Shadow oval
                Box(
                    modifier = Modifier
                        .width((if (coinCount == 1) 80f else 150f * shadowScale).dp)
                        .height((12f * shadowScale).dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = shadowAlpha))
                )
            }
        }

        // Large Result Banner
        if (coinResult != null && !isFlipping) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (coinResult == "HEADS") Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFF3B82F6).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (coinResult == "HEADS") Color(0xFFF59E0B) else Color(0xFF3B82F6)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val mainResultText = if (coinCount == 1) {
                        if (coinResult == "HEADS") {
                            if (isBn) "🎉 ফলাফল: হেড (Heads)!" else "🎉 Result: HEADS!"
                        } else {
                            if (isBn) "🎉 ফলাফল: টেল (Tails)!" else "🎉 Result: TAILS!"
                        }
                    } else {
                        val c1 = if (multiCoinResults.getOrNull(0) == "HEADS") (if (isBn) "হেড" else "Heads") else (if (isBn) "টেল" else "Tails")
                        val c2 = if (multiCoinResults.getOrNull(1) == "HEADS") (if (isBn) "হেড" else "Heads") else (if (isBn) "টেল" else "Tails")
                        if (isBn) "🎉 ফলাফল: $c1 + $c2" else "🎉 Result: $c1 + $c2"
                    }

                    Text(
                        text = mainResultText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (coinResult == "HEADS") Color(0xFFD97706) else Color(0xFF2563EB),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = if (isBn) "আবার টস করতে নিচের বাটনে বা কয়েনে চাপুন" else "Tap below or on the coin to flip again",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }
        } else if (isFlipping) {
            Text(
                text = if (isBn) "🪙 কয়েন বাতাসে ঘুরছে..." else "🪙 Coin is flipping in the air...",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.buttonEqualBg
            )
        }

        // Toss Action Button
        Button(
            onClick = { doFlip() },
            enabled = !isFlipping,
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColors.buttonEqualBg,
                disabledContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBn) (if (isFlipping) "টস হচ্ছে..." else "কয়েন টস করুন (Flip Coin)") else (if (isFlipping) "Flipping..." else "Flip Coin"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Tally Stats Board
        if (totalTosses > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.displayText.copy(alpha = 0.04f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isBn) "মোট টস" else "Total Tosses",
                            fontSize = 10.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$totalTosses",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isBn) "হেড (Heads)" else "Heads",
                            fontSize = 10.sp,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = "$headsCount (${if (totalTosses > 0) (headsCount * 100 / totalTosses) else 0}%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isBn) "টেল (Tails)" else "Tails",
                            fontSize = 10.sp,
                            color = Color(0xFF2563EB)
                        )
                        Text(
                            text = "$tailsCount (${if (totalTosses > 0) (tailsCount * 100 / totalTosses) else 0}%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// REALISTIC 3D COIN GRAPHIC
// ----------------------------------------------------
@Composable
private fun RealisticCoinGraphic(
    rotationDeg: Float,
    themeColors: CalculatorThemeColors,
    sizeDp: Int = 100
) {
    val normalizedAngle = ((rotationDeg % 360) + 360) % 360
    val isFront = normalizedAngle < 90 || normalizedAngle > 270

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .graphicsLayer {
                rotationY = rotationDeg
                cameraDistance = 12f * density
            }
            .clip(CircleShape)
            .background(
                if (isFront)
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(Color(0xFFFFDF00), Color(0xFFF59E0B), Color(0xFFB45309))
                    )
                else
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(Color(0xFFF1F5F9), Color(0xFF94A3B8), Color(0xFF475569))
                    )
            )
            .border(
                width = 3.dp,
                color = if (isFront) Color(0xFFD97706) else Color(0xFF64748B),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isFront) {
                // HEADS (Golden BD 1 Taka / Emblem)
                Text(
                    text = "★ ১ ★",
                    fontSize = (sizeDp * 0.14f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF78350F)
                )
                Text(
                    text = "HEADS",
                    fontSize = (sizeDp * 0.16f).sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF78350F)
                )
                Text(
                    text = "হেড",
                    fontSize = (sizeDp * 0.13f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
            } else {
                // TAILS (Silver Motif)
                Text(
                    text = "🌾 শাপলা 🌾",
                    fontSize = (sizeDp * 0.12f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "TAILS",
                    fontSize = (sizeDp * 0.16f).sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "টেল",
                    fontSize = (sizeDp * 0.13f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
            }
        }
    }
}

// ----------------------------------------------------
// DICE ROLL SECTION
// ----------------------------------------------------
@Composable
private fun DiceRollSection(isBn: Boolean, themeColors: CalculatorThemeColors) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val rotX = remember { androidx.compose.animation.core.Animatable(0f) }
    val rotY = remember { androidx.compose.animation.core.Animatable(0f) }
    val rotZ = remember { androidx.compose.animation.core.Animatable(0f) }
    val diceScale = remember { androidx.compose.animation.core.Animatable(1f) }

    var isRolling by remember { mutableStateOf(false) }
    var diceCount by remember { mutableIntStateOf(1) } // 1, 2, or 3 dice
    var diceResults by remember { mutableStateOf(listOf(6)) }
    var rollHistory by remember { mutableStateOf(listOf<List<Int>>()) }

    fun doRoll() {
        if (isRolling) return
        isRolling = true

        coroutineScope.launch {
            try {
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (_: Exception) {}

            // Rapid intermediate rolls
            val iterations = 8
            for (i in 0 until iterations) {
                diceResults = List(diceCount) { (1..6).random() }
                delay(60)
            }

            // Final target values
            val finalValues = List(diceCount) { (1..6).random() }
            diceResults = finalValues

            // 3D physics tumbling animation
            launch {
                diceScale.animateTo(1.25f, androidx.compose.animation.core.tween(200))
                diceScale.animateTo(1f, androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy))
            }

            launch {
                rotX.animateTo(rotX.value + 720f, androidx.compose.animation.core.tween(700, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            }
            launch {
                rotY.animateTo(rotY.value + 720f, androidx.compose.animation.core.tween(700, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            }
            rotZ.animateTo(rotZ.value + 360f, androidx.compose.animation.core.tween(700, easing = androidx.compose.animation.core.FastOutSlowInEasing))

            rollHistory = (listOf(finalValues) + rollHistory).take(5)
            isRolling = false

            try {
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(40, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dice Quantity Chips (1, 2, 3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBn) "ডাইস সংখ্যা:" else "Dice Count:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.displayText.copy(alpha = 0.7f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1, 2, 3).forEach { count ->
                    val isSelected = diceCount == count
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.08f),
                        modifier = Modifier.clickable {
                            if (!isRolling) {
                                diceCount = count
                                diceResults = List(count) { (1..6).random() }
                            }
                        }
                    ) {
                        Text(
                            text = if (isBn) "$count টি ডাইস" else "$count Dice",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else themeColors.displayText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Realistic 3D Dice Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(themeColors.displayText.copy(alpha = 0.03f))
                .clickable { doRoll() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer {
                    scaleX = diceScale.value
                    scaleY = diceScale.value
                    rotationX = if (isRolling) rotX.value else 0f
                    rotationY = if (isRolling) rotY.value else 0f
                    rotationZ = if (isRolling) rotZ.value else 0f
                    cameraDistance = 12f * density
                }
            ) {
                diceResults.forEach { value ->
                    RealisticDieGraphic(value = value, themeColors = themeColors)
                }
            }
        }

        // Result Banner
        val totalSum = diceResults.sum()
        val resultMessage = if (diceCount == 1) {
            if (isBn) "🎲 ফলাফল: ${diceResults.first()}" else "🎲 Result: ${diceResults.first()}"
        } else {
            val breakdown = diceResults.joinToString(" + ")
            if (isBn) "🎲 মোট যোগফল: $totalSum ($breakdown)" else "🎲 Total Sum: $totalSum ($breakdown)"
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = themeColors.buttonEqualBg.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = resultMessage,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = themeColors.buttonEqualBg,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = if (isBn) "আবার রোল করতে ডাইস বা নিচের বাটনে চাপুন" else "Tap on dice or button below to roll again",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )
            }
        }

        // Roll Action Button
        Button(
            onClick = { doRoll() },
            enabled = !isRolling,
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColors.buttonEqualBg,
                disabledContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBn) (if (isRolling) "ডাইস ঘুরছে..." else "ডাইস রোল করুন (Roll Dice)") else (if (isRolling) "Rolling..." else "Roll Dice"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Recent Roll History
        if (rollHistory.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isBn) "পূর্ববর্তী রোল সমূহ:" else "Recent Rolls:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rollHistory.forEach { roll ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = themeColors.displayText.copy(alpha = 0.06f)
                        ) {
                            Text(
                                text = roll.joinToString(", "),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// REALISTIC DIE GRAPHIC (AUTHENTIC 1-6 PIPS)
// ----------------------------------------------------
@Composable
private fun RealisticDieGraphic(value: Int, themeColors: CalculatorThemeColors) {
    Card(
        modifier = Modifier
            .size(76.dp)
            .themeCardShadow(themeColors, elevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCBD5E1))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val w = size.width
            val h = size.height
            val dotRadius = w * 0.10f

            val dotColor = if (value == 1) Color(0xFFDC2626) else Color(0xFF1E293B)

            val left = w * 0.22f
            val center = w * 0.5f
            val right = w * 0.78f

            val top = h * 0.22f
            val middle = h * 0.5f
            val bottom = h * 0.78f

            when (value) {
                1 -> {
                    drawCircle(color = dotColor, radius = dotRadius * 1.35f, center = Offset(center, middle))
                }
                2 -> {
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, top))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, bottom))
                }
                3 -> {
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, top))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(center, middle))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, bottom))
                }
                4 -> {
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, top))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, top))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, bottom))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, bottom))
                }
                5 -> {
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, top))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, top))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(center, middle))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, bottom))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, bottom))
                }
                6 -> {
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, top))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, middle))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, bottom))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, top))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, middle))
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, bottom))
                }
            }
        }
    }
}

// ----------------------------------------------------
// LUCKY LOTTERY & NUMBER SECTION
// ----------------------------------------------------
@Composable
private fun LotteryNumberSection(
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    context: android.content.Context,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    val coroutineScope = rememberCoroutineScope()

    var minInput by remember { mutableStateOf("1") }
    var maxInput by remember { mutableStateOf("100") }
    var countInput by remember { mutableStateOf("1") }
    var allowDuplicates by remember { mutableStateOf(false) }

    var isDrawing by remember { mutableStateOf(false) }
    var drawnNumbers by remember { mutableStateOf<List<Int>>(emptyList()) }

    fun doDraw() {
        val min = minInput.toIntOrNull() ?: 1
        val max = maxInput.toIntOrNull() ?: 100
        val count = (countInput.toIntOrNull() ?: 1).coerceIn(1, 20)

        if (min > max) {
            Toast.makeText(context, if (isBn) "সর্বনিম্ন সংখ্যা সর্বোচ্চ চেয়ে ছোট হতে হবে!" else "Min must be <= Max!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!allowDuplicates && (max - min + 1) < count) {
            Toast.makeText(context, if (isBn) "রেঞ্জ এর চেয়ে ড্র সংখ্যা বেশি হতে পারেনা!" else "Range too small for unique draw!", Toast.LENGTH_SHORT).show()
            return
        }

        isDrawing = true
        coroutineScope.launch {
            // Animate rolling effect
            for (i in 0..6) {
                drawnNumbers = List(count) { (min..max).random() }
                delay(70)
            }

            val finalResults = if (allowDuplicates) {
                List(count) { (min..max).random() }
            } else {
                (min..max).shuffled().take(count)
            }
            drawnNumbers = finalResults
            isDrawing = false

            try {
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(40, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (_: Exception) {}
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Quick Presets
        Text(
            text = if (isBn) "জনপ্রিয় লটারি প্রিসেট:" else "Popular Lottery Presets:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.displayText.copy(alpha = 0.7f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val presets = listOf(
                Pair(if (isBn) "১-১০০ লাকি ড্র" else "1-100 Lucky", Pair("1", "100")),
                Pair(if (isBn) "১-১০০০ র্যাফেল" else "1-1000 Raffle", Pair("1", "1000")),
                Pair(if (isBn) "১-৯০ হাউজি" else "1-90 Housie", Pair("1", "90")),
                Pair(if (isBn) "১-৫০ ড্র" else "1-50 Draw", Pair("1", "50")),
                Pair(if (isBn) "১-৬ পাশা" else "1-6 Range", Pair("1", "6"))
            )

            presets.forEach { (label, range) ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themeColors.displayText.copy(alpha = 0.06f),
                    modifier = Modifier.clickable {
                        minInput = range.first
                        maxInput = range.second
                    }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Min, Max, Count Inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = minInput,
                onValueChange = { minInput = it },
                label = { Text(if (isBn) "শুরু (Min)" else "Min", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText,
                    focusedLabelColor = themeColors.buttonEqualBg,
                    unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                    focusedBorderColor = themeColors.buttonEqualBg,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                )
            )
            OutlinedTextField(
                value = maxInput,
                onValueChange = { maxInput = it },
                label = { Text(if (isBn) "শেষ (Max)" else "Max", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText,
                    focusedLabelColor = themeColors.buttonEqualBg,
                    unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                    focusedBorderColor = themeColors.buttonEqualBg,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                )
            )
            OutlinedTextField(
                value = countInput,
                onValueChange = { countInput = it },
                label = { Text(if (isBn) "সংখ্যা (Qty)" else "Qty", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeColors.displayText,
                    unfocusedTextColor = themeColors.displayText,
                    focusedLabelColor = themeColors.buttonEqualBg,
                    unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.5f),
                    focusedBorderColor = themeColors.buttonEqualBg,
                    unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f)
                )
            )
        }

        // Draw Button
        Button(
            onClick = { doDraw() },
            enabled = !isDrawing,
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBn) (if (isDrawing) "লটারি ড্র হচ্ছে..." else "লটারি ড্র করুন (Draw Numbers)") else (if (isDrawing) "Drawing..." else "Draw Lucky Numbers"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Drawn Numbers Display (Glossy colorful lottery balls)
        if (drawnNumbers.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = themeColors.displayText.copy(alpha = 0.04f),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "🎉 বিজয়ী লাকি নম্বরসমূহ:" else "🎉 Winning Lucky Numbers:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(drawnNumbers.joinToString(", ")))
                                Toast.makeText(context, if (isBn) "নম্বরগুলো কপি করা হয়েছে!" else "Numbers copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Lottery Ball Flow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val ballColors = listOf(
                            Pair(Color(0xFFEF4444), Color(0xFF991B1B)),
                            Pair(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                            Pair(Color(0xFF10B981), Color(0xFF047857)),
                            Pair(Color(0xFFF59E0B), Color(0xFFB45309)),
                            Pair(Color(0xFF8B5CF6), Color(0xFF5B21B6)),
                            Pair(Color(0xFFEC4899), Color(0xFFBE185D))
                        )

                        drawnNumbers.forEachIndexed { index, num ->
                            val colorPair = ballColors[index % ballColors.size]
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.radialGradient(
                                            listOf(colorPair.first, colorPair.second)
                                        )
                                    )
                                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$num",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- Photo Resizer & Format Converter Tool ---
@Composable
fun PhotoLabCard(viewModel: CalculatorViewModel, themeColors: CalculatorThemeColors) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedWorkspaceTab by remember { mutableStateOf(0) } // 0: Presets & Crop, 1: Dimensions (W/H), 2: Target File Size (KB), 3: Format

    // Photo settings
    var cropPreset by remember { mutableStateOf("Original") } // "Original", "1:1", "3:4", "300x300", "300x80", "4:3", "16:9"
    var rotationDegrees by remember { mutableStateOf(0f) }
    var isFlippedHorizontal by remember { mutableStateOf(false) }

    var unit by remember { mutableStateOf("px") } // "px", "cm", "mm", "in"
    var inputWidth by remember { mutableStateOf("300") }
    var inputHeight by remember { mutableStateOf("300") }
    var lockAspectRatio by remember { mutableStateOf(true) }

    var targetFormat by remember { mutableStateOf("JPG") } // "JPG", "PNG", "WEBP"
    var targetKbMode by remember { mutableStateOf("Original") } // "Original", "< 50 KB", "< 100 KB", "< 200 KB", "Custom KB"
    var customTargetKb by remember { mutableStateOf("100") }
    var manualQuality by remember { mutableStateOf(90f) }

    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var customCroppedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var showInteractiveCropDialog by remember { mutableStateOf(false) }

    // Load original bitmap
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

    // Set initial width & height when image changes
    LaunchedEffect(originalBitmap) {
        originalBitmap?.let { bmp ->
            inputWidth = bmp.width.toString()
            inputHeight = bmp.height.toString()
        }
    }

    // Calculate original file size in bytes
    val originalSizeBytes = remember(selectedImageUri) {
        selectedImageUri?.let { uri ->
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    pfd.statSize
                } ?: 0L
            } catch (e: Exception) {
                0L
            }
        } ?: 0L
    }

    // Calculate processed image in real time
    val processedResult = remember(
        originalBitmap,
        customCroppedBitmap,
        cropPreset,
        rotationDegrees,
        isFlippedHorizontal,
        unit,
        inputWidth,
        inputHeight,
        lockAspectRatio,
        targetFormat,
        targetKbMode,
        customTargetKb,
        manualQuality
    ) {
        processImageResult(
            context = context,
            originalBmp = customCroppedBitmap ?: originalBitmap,
            cropPreset = cropPreset,
            rotation = rotationDegrees,
            isFlipped = isFlippedHorizontal,
            unit = unit,
            widthStr = inputWidth,
            heightStr = inputHeight,
            lockAspectRatio = lockAspectRatio,
            targetFormat = targetFormat,
            targetKbMode = targetKbMode,
            customKbStr = customTargetKb,
            manualQuality = manualQuality.toInt()
        )
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            customCroppedBitmap = null
            cropPreset = "Original"
            rotationDegrees = 0f
            isFlippedHorizontal = false
            unit = "px"
            targetFormat = "JPG"
            targetKbMode = "Original"
            customTargetKb = "100"
            manualQuality = 90f
            saveSuccessMessage = null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tool Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isBn) "ফটো রিসাইজার টুল" else "Photo Resizer Tool",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "ক্রপ, কাস্টম মাপ (px/cm/mm/in), ফাইল সাইজ (KB) ও ফরম্যাট পরিবর্তন" else "Crop, custom dimensions, file size (KB) & format conversion",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedImageUri == null) {
                // Initial Select Image Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
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
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "ফটো সিলেক্ট করুন" else "Select Photo from Gallery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) "চাকরি/পাসপোর্ট আবেদনের জন্য নিখুঁত ফটো ও সাইজ তৈরি করুন" else "Perfect photo resizing for official applications & portals",
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                // PHOTO WORKSPACE
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Preview Canvas & Info Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (processedResult?.bitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = processedResult.bitmap.asImageBitmap(),
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(8.dp)
                            )
                        } else {
                            CircularProgressIndicator(color = themeColors.buttonEqualBg)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Original vs Processed Info Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(themeColors.displayText.copy(alpha = 0.05f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val origMb = String.format(Locale.US, "%.2f MB", originalSizeBytes / (1024f * 1024f))
                            val origW = originalBitmap?.width ?: 0
                            val origH = originalBitmap?.height ?: 0
                            Text(
                                text = if (isBn) "মূল ছবি: $origW×$origH px ($origMb)" else "Original: $origW×$origH px ($origMb)",
                                fontSize = 10.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                            if (processedResult != null) {
                                val procKb = String.format(Locale.US, "%.1f KB", processedResult.sizeBytes / 1024f)
                                Text(
                                    text = if (isBn) "আউটপুট: ${processedResult.widthPx}×${processedResult.heightPx} px • $procKb • ${processedResult.format}"
                                    else "Output: ${processedResult.widthPx}×${processedResult.heightPx} px • $procKb • ${processedResult.format}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                if (isBn) "🔄 অন্য ফটো" else "🔄 Change",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.buttonEqualBg
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Official Presets Row
                    Text(
                        text = if (isBn) "অফিশিয়াল প্রিসেট নির্বাচন:" else "Official Quick Presets:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val officialPresets = listOf(
                            "BD Job Photo (300×300)",
                            "BD Job Signature (300×80)",
                            "Passport (40×50 mm)",
                            "Stamp (20×25 mm)",
                            "Square Avatar (1000×1000)"
                        )
                        officialPresets.forEach { p ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(themeColors.buttonEqualBg.copy(alpha = 0.12f))
                                    .border(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        when (p) {
                                            "BD Job Photo (300×300)" -> {
                                                cropPreset = "300x300"
                                                unit = "px"
                                                inputWidth = "300"
                                                inputHeight = "300"
                                                targetKbMode = "< 100 KB"
                                                targetFormat = "JPG"
                                            }
                                            "BD Job Signature (300×80)" -> {
                                                cropPreset = "300x80"
                                                unit = "px"
                                                inputWidth = "300"
                                                inputHeight = "80"
                                                targetKbMode = "< 50 KB"
                                                targetFormat = "JPG"
                                            }
                                            "Passport (40×50 mm)" -> {
                                                cropPreset = "3:4"
                                                unit = "mm"
                                                inputWidth = "40"
                                                inputHeight = "50"
                                                targetKbMode = "< 150 KB"
                                                targetFormat = "JPG"
                                            }
                                            "Stamp (20×25 mm)" -> {
                                                cropPreset = "3:4"
                                                unit = "mm"
                                                inputWidth = "20"
                                                inputHeight = "25"
                                                targetKbMode = "< 50 KB"
                                                targetFormat = "JPG"
                                            }
                                            "Square Avatar (1000×1000)" -> {
                                                cropPreset = "1:1"
                                                unit = "px"
                                                inputWidth = "1000"
                                                inputHeight = "1000"
                                                targetKbMode = "Original"
                                                targetFormat = "PNG"
                                            }
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = p,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // WORKSPACE TABS
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
                            text = { Text(if (isBn) "১. ক্রপ ও ওরিয়েন্টেশন" else "1. Crop & Rotate", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedWorkspaceTab == 1,
                            onClick = { selectedWorkspaceTab = 1 },
                            text = { Text(if (isBn) "২. কাস্টম সাইজ (W×H)" else "2. Dimensions", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedWorkspaceTab == 2,
                            onClick = { selectedWorkspaceTab = 2 },
                            text = { Text(if (isBn) "৩. ফাইল সাইজ (KB)" else "3. Target Size (KB)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedWorkspaceTab == 3,
                            onClick = { selectedWorkspaceTab = 3 },
                            text = { Text(if (isBn) "৪. ফরম্যাট পরিবর্তন" else "4. Format", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // TAB CONTENT
                    when (selectedWorkspaceTab) {
                        0 -> {
                            // CROP & ROTATE
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = if (isBn) "ক্রপ অনুপাত (Aspect Ratio):" else "Crop Aspect Ratio:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val cropRatios = listOf("Original", "1:1", "3:4", "300x300", "300x80", "4:3", "16:9")
                                    cropRatios.forEach { ratio ->
                                        val isSel = cropPreset == ratio
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { cropPreset = ratio },
                                            label = { Text(ratio, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = themeColors.buttonEqualBg,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { showInteractiveCropDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "✂️ প্রফেশনাল জুমেবল ক্রপ টুল" else "✂️ Professional Zoomable Crop Tool",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

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
                                        Text(if (isBn) "ঘুরান (${rotationDegrees.toInt()}°)" else "Rotate (${rotationDegrees.toInt()}°)", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { isFlippedHorizontal = !isFlippedHorizontal },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBn) "ফ্লিপ করুন" else "Flip Horizontal", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        1 -> {
                            // HEIGHT & WIDTH DIMENSIONS
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = if (isBn) "একক নির্বাচন (Unit):" else "Select Unit:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val units = listOf("px", "cm", "mm", "in")
                                        units.forEach { u ->
                                            val isSel = unit == u
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.08f))
                                                    .clickable {
                                                        if (unit != u) {
                                                            unit = u
                                                            // Auto convert width & height roughly for user convenience
                                                            val currentW = inputWidth.toFloatOrNull() ?: 300f
                                                            val currentH = inputHeight.toFloatOrNull() ?: 300f
                                                            when (u) {
                                                                "cm" -> {
                                                                    inputWidth = String.format(Locale.US, "%.1f", currentW / 118.11f)
                                                                    inputHeight = String.format(Locale.US, "%.1f", currentH / 118.11f)
                                                                }
                                                                "mm" -> {
                                                                    inputWidth = String.format(Locale.US, "%.0f", currentW / 11.811f)
                                                                    inputHeight = String.format(Locale.US, "%.0f", currentH / 11.811f)
                                                                }
                                                                "in" -> {
                                                                    inputWidth = String.format(Locale.US, "%.1f", currentW / 300f)
                                                                    inputHeight = String.format(Locale.US, "%.1f", currentH / 300f)
                                                                }
                                                                else -> { // px
                                                                    inputWidth = "300"
                                                                    inputHeight = "300"
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(u, fontSize = 11.sp, color = if (isSel) Color.White else themeColors.displayText, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = inputWidth,
                                        onValueChange = { newW ->
                                            inputWidth = newW
                                            if (lockAspectRatio) {
                                                val wVal = newW.toFloatOrNull()
                                                val origW = originalBitmap?.width?.toFloat() ?: 1f
                                                val origH = originalBitmap?.height?.toFloat() ?: 1f
                                                if (wVal != null && origW > 0) {
                                                    val calculatedH = wVal * (origH / origW)
                                                    inputHeight = if (unit == "px" || unit == "mm") calculatedH.toInt().toString()
                                                    else String.format(Locale.US, "%.1f", calculatedH)
                                                }
                                            }
                                        },
                                        label = { Text(if (isBn) "প্রস্থ ($unit)" else "Width ($unit)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )

                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = themeColors.displayText.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )

                                    OutlinedTextField(
                                        value = inputHeight,
                                        onValueChange = { newH ->
                                            inputHeight = newH
                                            if (lockAspectRatio) {
                                                val hVal = newH.toFloatOrNull()
                                                val origW = originalBitmap?.width?.toFloat() ?: 1f
                                                val origH = originalBitmap?.height?.toFloat() ?: 1f
                                                if (hVal != null && origH > 0) {
                                                    val calculatedW = hVal * (origW / origH)
                                                    inputWidth = if (unit == "px" || unit == "mm") calculatedW.toInt().toString()
                                                    else String.format(Locale.US, "%.1f", calculatedW)
                                                }
                                            }
                                        },
                                        label = { Text(if (isBn) "উচ্চতা ($unit)" else "Height ($unit)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { lockAspectRatio = !lockAspectRatio }
                                ) {
                                    Checkbox(
                                        checked = lockAspectRatio,
                                        onCheckedChange = { lockAspectRatio = it },
                                        colors = CheckboxDefaults.colors(checkedColor = themeColors.buttonEqualBg)
                                    )
                                    Text(
                                        text = if (isBn) "অ্যাসপেক্ট রেশিও লক রাখুন (Lock Aspect Ratio)" else "Lock Aspect Ratio",
                                        fontSize = 11.sp,
                                        color = themeColors.displayText
                                    )
                                }
                            }
                        }

                        2 -> {
                            // TARGET FILE SIZE (KB)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (isBn) "লক্ষ্যমাত্রা ফাইল সাইজ নির্বাচন (Target File Size):" else "Select Target File Size:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                val kbModes = listOf("Original", "< 50 KB", "< 100 KB", "< 200 KB", "Custom KB")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    kbModes.forEach { mode ->
                                        val isSel = targetKbMode == mode
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { targetKbMode = mode },
                                            label = { Text(mode, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = themeColors.buttonEqualBg,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                if (targetKbMode == "Custom KB") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = customTargetKb,
                                        onValueChange = { customTargetKb = it },
                                        label = { Text(if (isBn) "কাস্টম ফাইল সাইজ (KB)" else "Custom Target Size (KB)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }

                                if (targetKbMode == "Original") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${if (isBn) "ম্যানুয়াল কোয়ালিটি" else "Quality"}: ${manualQuality.toInt()}%",
                                        fontSize = 11.sp,
                                        color = themeColors.displayText
                                    )
                                    Slider(
                                        value = manualQuality,
                                        onValueChange = { manualQuality = it },
                                        valueRange = 10f..100f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = themeColors.buttonEqualBg,
                                            activeTrackColor = themeColors.buttonEqualBg
                                        )
                                    )
                                }
                            }
                        }

                        3 -> {
                            // FORMAT CONVERSION
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (isBn) "আউটপুট ফটো ফরম্যাট:" else "Output Photo Format:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val formats = listOf("JPG", "PNG", "WEBP")
                                    formats.forEach { fmt ->
                                        val isSel = targetFormat == fmt
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { targetFormat = fmt },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSel) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.05f)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = fmt,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSel) Color.White else themeColors.displayText
                                                    )
                                                    Text(
                                                        text = when (fmt) {
                                                            "JPG" -> "Best for Portal"
                                                            "PNG" -> "Lossless HD"
                                                            else -> "Web Compact"
                                                        },
                                                        fontSize = 9.sp,
                                                        color = if (isSel) Color.White.copy(alpha = 0.8f) else themeColors.displayText.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final Export / Save Button
                    Button(
                        onClick = {
                            if (processedResult != null && !isSaving) {
                                isSaving = true
                                coroutineScope.launch {
                                    val savedFilename = saveProcessedPhotoToGallery(context, processedResult)
                                    isSaving = false
                                    if (savedFilename != null) {
                                        saveSuccessMessage = if (isBn)
                                            "গ্যালারিতে সেভ হয়েছে! ($savedFilename)"
                                        else
                                            "Saved to Gallery! ($savedFilename)"
                                        Toast.makeText(context, saveSuccessMessage, Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, if (isBn) "সেভ করতে সমস্যা হয়েছে" else "Failed to save photo", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = processedResult != null && !isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.buttonEqualBg,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "সেভ করা হচ্ছে..." else "Saving...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isBn) "ফাইনাল ফটো গ্যালারিতে সেভ করুন" else "Save Processed Photo to Gallery",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                            delay(4000)
                            saveSuccessMessage = null
                        }
                    }
                }
            }
        }

        if (showInteractiveCropDialog && (customCroppedBitmap != null || originalBitmap != null)) {
            InteractiveCropDialog(
                originalBitmap = customCroppedBitmap ?: originalBitmap!!,
                isBn = isBn,
                themeColors = themeColors,
                onDismiss = { showInteractiveCropDialog = false },
                onCropApplied = { croppedBmp ->
                    customCroppedBitmap = croppedBmp
                    showInteractiveCropDialog = false
                }
            )
        }
    }
}

// Process Image Utility Function for Photo Resizer
private fun processImageResult(
    context: android.content.Context,
    originalBmp: android.graphics.Bitmap?,
    cropPreset: String,
    rotation: Float,
    isFlipped: Boolean,
    unit: String,
    widthStr: String,
    heightStr: String,
    lockAspectRatio: Boolean,
    targetFormat: String,
    targetKbMode: String,
    customKbStr: String,
    manualQuality: Int
): ProcessedPhotoResult? {
    if (originalBmp == null) return null

    try {
        var workingBmp: android.graphics.Bitmap = originalBmp

        // 1. Rotation & Flip
        val matrix = android.graphics.Matrix().apply {
            if (rotation != 0f) postRotate(rotation)
            if (isFlipped) postScale(-1f, 1f, workingBmp.width / 2f, workingBmp.height / 2f)
        }
        if (rotation != 0f || isFlipped) {
            workingBmp = android.graphics.Bitmap.createBitmap(workingBmp, 0, 0, workingBmp.width, workingBmp.height, matrix, true)
        }

        // 2. Crop Preset
        workingBmp = when (cropPreset) {
            "1:1", "300x300" -> cropToAspectRatio(workingBmp, 1f, 1f)
            "3:4" -> cropToAspectRatio(workingBmp, 3f, 4f)
            "300x80" -> cropToAspectRatio(workingBmp, 300f, 80f)
            "4:3" -> cropToAspectRatio(workingBmp, 4f, 3f)
            "16:9" -> cropToAspectRatio(workingBmp, 16f, 9f)
            else -> workingBmp
        }

        // 3. Dimension Conversion to Pixels
        val dpi = 300f
        val userW = widthStr.toFloatOrNull() ?: workingBmp.width.toFloat()
        val userH = heightStr.toFloatOrNull() ?: workingBmp.height.toFloat()

        val targetPxW = when (unit) {
            "cm" -> (userW * dpi / 2.54f).toInt()
            "mm" -> (userW * dpi / 25.4f).toInt()
            "in" -> (userW * dpi).toInt()
            else -> userW.toInt()
        }.coerceIn(10, 8000)

        val targetPxH = when (unit) {
            "cm" -> (userH * dpi / 2.54f).toInt()
            "mm" -> (userH * dpi / 25.4f).toInt()
            "in" -> (userH * dpi).toInt()
            else -> userH.toInt()
        }.coerceIn(10, 8000)

        if (workingBmp.width != targetPxW || workingBmp.height != targetPxH) {
            workingBmp = android.graphics.Bitmap.createScaledBitmap(workingBmp, targetPxW, targetPxH, true)
        }

        // 4. Target KB / Compression Logic
        val targetKb = when (targetKbMode) {
            "< 50 KB" -> 50
            "< 100 KB" -> 100
            "< 200 KB" -> 200
            "Custom KB" -> customKbStr.toIntOrNull() ?: 100
            else -> 0
        }

        val compressFormat = when (targetFormat) {
            "PNG" -> android.graphics.Bitmap.CompressFormat.PNG
            "WEBP" -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                android.graphics.Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                android.graphics.Bitmap.CompressFormat.WEBP
            }
            else -> android.graphics.Bitmap.CompressFormat.JPEG
        }

        var finalBytes: ByteArray
        var finalBmp = workingBmp
        var q = manualQuality.coerceIn(5, 100)

        if (targetKb > 0 && compressFormat != android.graphics.Bitmap.CompressFormat.PNG) {
            val maxBytes = targetKb * 1024
            var scaleFactor = 1.0f

            do {
                if (scaleFactor < 1.0f) {
                    val scaledW = (workingBmp.width * scaleFactor).toInt().coerceAtLeast(30)
                    val scaledH = (workingBmp.height * scaleFactor).toInt().coerceAtLeast(30)
                    finalBmp = android.graphics.Bitmap.createScaledBitmap(workingBmp, scaledW, scaledH, true)
                }

                q = 95
                val baos = java.io.ByteArrayOutputStream()
                finalBmp.compress(compressFormat, q, baos)
                finalBytes = baos.toByteArray()

                while (finalBytes.size > maxBytes && q > 10) {
                    q -= 8
                    val os = java.io.ByteArrayOutputStream()
                    finalBmp.compress(compressFormat, q, os)
                    finalBytes = os.toByteArray()
                }

                if (finalBytes.size > maxBytes && (finalBmp.width > 100 || finalBmp.height > 100)) {
                    scaleFactor *= 0.85f
                } else {
                    break
                }
            } while (finalBytes.size > maxBytes && scaleFactor > 0.2f)
        } else {
            val baos = java.io.ByteArrayOutputStream()
            finalBmp.compress(compressFormat, q, baos)
            finalBytes = baos.toByteArray()
        }

        return ProcessedPhotoResult(
            bitmap = finalBmp,
            byteArray = finalBytes,
            widthPx = finalBmp.width,
            heightPx = finalBmp.height,
            sizeBytes = finalBytes.size,
            format = targetFormat
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

private fun cropToAspectRatio(src: android.graphics.Bitmap, targetW: Float, targetH: Float): android.graphics.Bitmap {
    val srcW = src.width.toFloat()
    val srcH = src.height.toFloat()
    val targetRatio = targetW / targetH
    val srcRatio = srcW / srcH

    var cropW = srcW
    var cropH = srcH

    if (srcRatio > targetRatio) {
        cropW = srcH * targetRatio
    } else {
        cropH = srcW / targetRatio
    }

    val left = ((srcW - cropW) / 2f).coerceAtLeast(0f).toInt()
    val top = ((srcH - cropH) / 2f).coerceAtLeast(0f).toInt()
    val width = cropW.toInt().coerceAtMost(src.width - left)
    val height = cropH.toInt().coerceAtMost(src.height - top)

    return android.graphics.Bitmap.createBitmap(src, left, top, width, height)
}

private fun saveProcessedPhotoToGallery(context: android.content.Context, result: ProcessedPhotoResult): String? {
    try {
        val ext = when (result.format) {
            "PNG" -> "png"
            "WEBP" -> "webp"
            else -> "jpg"
        }
        val mimeType = when (result.format) {
            "PNG" -> "image/png"
            "WEBP" -> "image/webp"
            else -> "image/jpeg"
        }
        val filename = "PhotoResizer_${System.currentTimeMillis()}.$ext"

        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PhotoResizer")
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { os ->
                os.write(result.byteArray)
                os.flush()
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            return filename
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

data class ProcessedPhotoResult(
    val bitmap: android.graphics.Bitmap,
    val byteArray: ByteArray,
    val widthPx: Int,
    val heightPx: Int,
    val sizeBytes: Int,
    val format: String
)

@Composable
private fun InteractiveCropDialog(
    originalBitmap: android.graphics.Bitmap,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onCropApplied: (android.graphics.Bitmap) -> Unit
) {
    var selectedRatio by remember { mutableStateOf("Free") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "প্রফেশনাল ক্রপ টুল (পিকসআর্ট স্টাইল)" else "Professional Crop Tool (PicsArt Style)",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Aspect Ratio Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val ratios = listOf("Free", "1:1", "3:4", "4:3", "16:9", "300x300", "300x80")
                    ratios.forEach { r ->
                        val isSel = selectedRatio == r
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedRatio = r },
                            label = { Text(r, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.buttonEqualBg,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.12f),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                // Interactive Crop Canvas Area
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds()
                        .background(Color(0xFF090D16))
                ) {
                    val containerWidth = constraints.maxWidth.toFloat()
                    val containerHeight = constraints.maxHeight.toFloat()

                    // Calculate display dimensions of the bitmap to fit inside the Box container (85% size)
                    val bmpW = originalBitmap.width.toFloat()
                    val bmpH = originalBitmap.height.toFloat()
                    val bmpRatio = bmpW / bmpH

                    val imageWidthOnScreen: Float
                    val imageHeightOnScreen: Float
                    if (containerWidth / containerHeight > bmpRatio) {
                        imageHeightOnScreen = containerHeight * 0.82f
                        imageWidthOnScreen = imageHeightOnScreen * bmpRatio
                    } else {
                        imageWidthOnScreen = containerWidth * 0.82f
                        imageHeightOnScreen = imageWidthOnScreen / bmpRatio
                    }

                    val imageLeft = (containerWidth - imageWidthOnScreen) / 2f
                    val imageTop = (containerHeight - imageHeightOnScreen) / 2f

                    // State-based Crop Rect initialized/updated when ratio or image dimensions change
                    var cropRect by remember(selectedRatio, containerWidth, containerHeight, imageWidthOnScreen, imageHeightOnScreen) {
                        val initW: Float
                        val initH: Float
                        val ratio = when (selectedRatio) {
                            "1:1", "300x300" -> 1.0f
                            "3:4" -> 3f / 4f
                            "4:3" -> 4f / 3f
                            "16:9" -> 16f / 9f
                            "300x80" -> 300f / 80f
                            else -> bmpRatio
                        }

                        if (imageWidthOnScreen / imageHeightOnScreen > ratio) {
                            initH = imageHeightOnScreen * 0.9f
                            initW = initH * ratio
                        } else {
                            initW = imageWidthOnScreen * 0.9f
                            initH = initW / ratio
                        }

                        val left = imageLeft + (imageWidthOnScreen - initW) / 2f
                        val top = imageTop + (imageHeightOnScreen - initH) / 2f
                        mutableStateOf(Rect(left, top, left + initW, top + initH))
                    }

                    var activeHandle by remember { mutableStateOf<String?>(null) } // "TL", "TR", "BL", "BR", "BODY", or null

                    // Touch Gesture Handler (Draggable Handles & Body)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(selectedRatio, imageLeft, imageTop, imageWidthOnScreen, imageHeightOnScreen) {
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        val tl = Offset(cropRect.left, cropRect.top)
                                        val tr = Offset(cropRect.right, cropRect.top)
                                        val bl = Offset(cropRect.left, cropRect.bottom)
                                        val br = Offset(cropRect.right, cropRect.bottom)
                                        
                                        val threshold = 90f // Increased touch sensitivity area for comfortable dragging
                                        
                                        activeHandle = when {
                                            (startOffset - tl).getDistance() < threshold -> "TL"
                                            (startOffset - tr).getDistance() < threshold -> "TR"
                                            (startOffset - bl).getDistance() < threshold -> "BL"
                                            (startOffset - br).getDistance() < threshold -> "BR"
                                            cropRect.contains(startOffset) -> "BODY"
                                            else -> null
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val handle = activeHandle ?: return@detectDragGestures
                                        
                                        val imageRight = imageLeft + imageWidthOnScreen
                                        val imageBottom = imageTop + imageHeightOnScreen
                                        
                                        var newLeft = cropRect.left
                                        var newTop = cropRect.top
                                        var newRight = cropRect.right
                                        var newBottom = cropRect.bottom
                                        
                                        val ratio = when (selectedRatio) {
                                            "1:1", "300x300" -> 1.0f
                                            "3:4" -> 3f / 4f
                                            "4:3" -> 4f / 3f
                                            "16:9" -> 16f / 9f
                                            "300x80" -> 300f / 80f
                                            else -> null // Free aspect ratio
                                        }
                                        
                                        val minSize = 100f // Minimum crop box size on screen
                                        
                                        if (handle == "BODY") {
                                            val dx = dragAmount.x
                                            val dy = dragAmount.y
                                            val width = cropRect.width
                                            val height = cropRect.height
                                            
                                            newLeft = (cropRect.left + dx).coerceIn(imageLeft, imageRight - width)
                                            newTop = (cropRect.top + dy).coerceIn(imageTop, imageBottom - height)
                                            newRight = newLeft + width
                                            newBottom = newTop + height
                                        } else {
                                            if (ratio == null) {
                                                // Free Aspect Ratio
                                                when (handle) {
                                                    "TL" -> {
                                                        newLeft = (cropRect.left + dragAmount.x).coerceIn(imageLeft, cropRect.right - minSize)
                                                        newTop = (cropRect.top + dragAmount.y).coerceIn(imageTop, cropRect.bottom - minSize)
                                                    }
                                                    "TR" -> {
                                                        newRight = (cropRect.right + dragAmount.x).coerceIn(cropRect.left + minSize, imageRight)
                                                        newTop = (cropRect.top + dragAmount.y).coerceIn(imageTop, cropRect.bottom - minSize)
                                                    }
                                                    "BL" -> {
                                                        newLeft = (cropRect.left + dragAmount.x).coerceIn(imageLeft, cropRect.right - minSize)
                                                        newBottom = (cropRect.bottom + dragAmount.y).coerceIn(cropRect.top + minSize, imageBottom)
                                                    }
                                                    "BR" -> {
                                                        newRight = (cropRect.right + dragAmount.x).coerceIn(cropRect.left + minSize, imageRight)
                                                        newBottom = (cropRect.bottom + dragAmount.y).coerceIn(cropRect.top + minSize, imageBottom)
                                                    }
                                                }
                                            } else {
                                                // Locked Aspect Ratio
                                                when (handle) {
                                                    "BR" -> {
                                                        val proposedRight = (cropRect.right + dragAmount.x).coerceIn(cropRect.left + minSize, imageRight)
                                                        val newW = proposedRight - cropRect.left
                                                        val newH = newW / ratio
                                                        if (cropRect.top + newH <= imageBottom) {
                                                            newRight = proposedRight
                                                            newBottom = cropRect.top + newH
                                                        } else {
                                                            val maxH = imageBottom - cropRect.top
                                                            val maxW = maxH * ratio
                                                            newBottom = imageBottom
                                                            newRight = cropRect.left + maxW
                                                        }
                                                    }
                                                    "TL" -> {
                                                        val proposedLeft = (cropRect.left + dragAmount.x).coerceIn(imageLeft, cropRect.right - minSize)
                                                        val newW = cropRect.right - proposedLeft
                                                        val newH = newW / ratio
                                                        if (cropRect.bottom - newH >= imageTop) {
                                                            newLeft = proposedLeft
                                                            newTop = cropRect.bottom - newH
                                                        } else {
                                                            val maxH = cropRect.bottom - imageTop
                                                            val maxW = maxH * ratio
                                                            newTop = imageTop
                                                            newLeft = cropRect.right - maxW
                                                        }
                                                    }
                                                    "TR" -> {
                                                        val proposedRight = (cropRect.right + dragAmount.x).coerceIn(cropRect.left + minSize, imageRight)
                                                        val newW = proposedRight - cropRect.left
                                                        val newH = newW / ratio
                                                        if (cropRect.bottom - newH >= imageTop) {
                                                            newRight = proposedRight
                                                            newTop = cropRect.bottom - newH
                                                        } else {
                                                            val maxH = cropRect.bottom - imageTop
                                                            val maxW = maxH * ratio
                                                            newTop = imageTop
                                                            newRight = cropRect.left + maxW
                                                        }
                                                    }
                                                    "BL" -> {
                                                        val proposedLeft = (cropRect.left + dragAmount.x).coerceIn(imageLeft, cropRect.right - minSize)
                                                        val newW = cropRect.right - proposedLeft
                                                        val newH = newW / ratio
                                                        if (cropRect.top + newH <= imageBottom) {
                                                            newLeft = proposedLeft
                                                            newBottom = cropRect.top + newH
                                                        } else {
                                                            val maxH = imageBottom - cropRect.top
                                                            val maxW = maxH * ratio
                                                            newBottom = imageBottom
                                                            newLeft = cropRect.right - maxW
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        cropRect = Rect(newLeft, newTop, newRight, newBottom)
                                    },
                                    onDragEnd = { activeHandle = null }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Display the fitted original image
                        androidx.compose.foundation.Image(
                            bitmap = originalBitmap.asImageBitmap(),
                            contentDescription = "Crop target",
                            modifier = Modifier
                                .size(
                                    width = (imageWidthOnScreen / LocalDensity.current.density).dp,
                                    height = (imageHeightOnScreen / LocalDensity.current.density).dp
                                )
                                .align(Alignment.Center),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    // PicsArt Style Crop Grid & Corner Drag Handles Overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // 1. Semi-transparent dark background for cropped outer area
                        drawRect(color = Color.Black.copy(alpha = 0.65f))
                        
                        // 2. Clear out the crop window
                        drawRect(
                            color = Color.Transparent,
                            topLeft = cropRect.topLeft,
                            size = cropRect.size,
                            blendMode = BlendMode.Clear
                        )
                        
                        // 3. Crisp white boundary stroke
                        drawRect(
                            color = Color.White.copy(alpha = 0.85f),
                            topLeft = cropRect.topLeft,
                            size = cropRect.size,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        
                        // 4. Rule of Thirds Grid Lines
                        val colW = cropRect.width / 3f
                        drawLine(Color.White.copy(alpha = 0.35f), Offset(cropRect.left + colW, cropRect.top), Offset(cropRect.left + colW, cropRect.bottom), strokeWidth = 1.dp.toPx())
                        drawLine(Color.White.copy(alpha = 0.35f), Offset(cropRect.left + colW * 2, cropRect.top), Offset(cropRect.left + colW * 2, cropRect.bottom), strokeWidth = 1.dp.toPx())
                        
                        val rowH = cropRect.height / 3f
                        drawLine(Color.White.copy(alpha = 0.35f), Offset(cropRect.left, cropRect.top + rowH), Offset(cropRect.right, cropRect.top + rowH), strokeWidth = 1.dp.toPx())
                        drawLine(Color.White.copy(alpha = 0.35f), Offset(cropRect.left, cropRect.top + rowH * 2), Offset(cropRect.right, cropRect.top + rowH * 2), strokeWidth = 1.dp.toPx())

                        // 5. Heavy Corner Handles (PicsArt Style)
                        val cornerStroke = 4.dp.toPx()
                        val cornerLength = 22.dp.toPx()

                        // Top-Left corner
                        drawLine(Color.White, Offset(cropRect.left - cornerStroke/2, cropRect.top), Offset(cropRect.left + cornerLength, cropRect.top), strokeWidth = cornerStroke)
                        drawLine(Color.White, Offset(cropRect.left, cropRect.top - cornerStroke/2), Offset(cropRect.left, cropRect.top + cornerLength), strokeWidth = cornerStroke)

                        // Top-Right corner
                        drawLine(Color.White, Offset(cropRect.right + cornerStroke/2, cropRect.top), Offset(cropRect.right - cornerLength, cropRect.top), strokeWidth = cornerStroke)
                        drawLine(Color.White, Offset(cropRect.right, cropRect.top - cornerStroke/2), Offset(cropRect.right, cropRect.top + cornerLength), strokeWidth = cornerStroke)

                        // Bottom-Left corner
                        drawLine(Color.White, Offset(cropRect.left - cornerStroke/2, cropRect.bottom), Offset(cropRect.left + cornerLength, cropRect.bottom), strokeWidth = cornerStroke)
                        drawLine(Color.White, Offset(cropRect.left, cropRect.bottom + cornerStroke/2), Offset(cropRect.left, cropRect.bottom - cornerLength), strokeWidth = cornerStroke)

                        // Bottom-Right corner
                        drawLine(Color.White, Offset(cropRect.right + cornerStroke/2, cropRect.bottom), Offset(cropRect.right - cornerLength, cropRect.bottom), strokeWidth = cornerStroke)
                        drawLine(Color.White, Offset(cropRect.right, cropRect.bottom + cornerStroke/2), Offset(cropRect.right, cropRect.bottom - cornerLength), strokeWidth = cornerStroke)
                    }

                    // Bottom Action Toolbar
                    Surface(
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    selectedRatio = "Free"
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBn) "রিসেট করুন" else "Reset", color = Color.White, fontSize = 12.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                ) {
                                    Text(if (isBn) "বাতিল" else "Cancel", color = Color.White, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        try {
                                            // Map screen crop Rect back to original bitmap coordinates
                                            val relativeLeft = (cropRect.left - imageLeft) / imageWidthOnScreen
                                            val relativeTop = (cropRect.top - imageTop) / imageHeightOnScreen
                                            val relativeWidth = cropRect.width / imageWidthOnScreen
                                            val relativeHeight = cropRect.height / imageHeightOnScreen

                                            val origW = originalBitmap.width
                                            val origH = originalBitmap.height

                                            val startX = (relativeLeft * origW).toInt().coerceIn(0, origW - 10)
                                            val startY = (relativeTop * origH).toInt().coerceIn(0, origH - 10)
                                            val cropWidth = (relativeWidth * origW).toInt().coerceIn(10, origW - startX)
                                            val cropHeight = (relativeHeight * origH).toInt().coerceIn(10, origH - startY)

                                            val croppedBmp = android.graphics.Bitmap.createBitmap(originalBitmap, startX, startY, cropWidth, cropHeight)
                                            onCropApplied(croppedBmp)
                                        } catch (e: Exception) {
                                            onCropApplied(originalBitmap)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg, contentColor = Color.White)
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBn) "ক্রপ সম্পন্ন করুন" else "Apply Crop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


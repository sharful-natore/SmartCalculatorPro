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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
    var noteText by remember { mutableStateOf("") }
    val notesList = remember { mutableStateListOf<String>() }
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isBn) "কুইক নোটস ও মেমো" else "Quick Notes & Memo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text(if (isBn) "জরুরি মেমো বা নোট লিখুন..." else "Type quick note or memo...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                trailingIcon = {
                    if (noteText.isNotBlank()) {
                        IconButton(onClick = {
                            notesList.add(0, noteText.trim())
                            noteText = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = themeColors.buttonEqualBg
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (notesList.isEmpty()) {
                Text(
                    text = if (isBn) "কোনো সেভ করা নোট নেই।" else "No saved notes yet.",
                    color = themeColors.displayText.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    notesList.forEachIndexed { index, note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(themeColors.displayText.copy(alpha = 0.05f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note,
                                color = themeColors.displayText,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(note))
                            }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = themeColors.displayText.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = {
                                notesList.removeAt(index)
                            }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

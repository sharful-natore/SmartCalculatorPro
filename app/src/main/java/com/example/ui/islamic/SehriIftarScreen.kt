package com.example.ui.islamic

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class RamadanDecade(val titleBn: String, val titleEn: String, val dayRange: IntRange, val accentColor: Color) {
    ALL("পূর্ণাঙ্গ ৩০ দিন", "Full 30 Days", 1..30, Color(0xFF0284C7)),
    RAHMAT("১ম দশক - রহমত", "1st 10 Days - Mercy", 1..10, Color(0xFF10B981)),
    MAGFIRAT("২য় দশক - মাগফিরাত", "2nd 10 Days - Forgiveness", 11..20, Color(0xFF0284C7)),
    NAJAT("৩য় দশক - নাজাত", "3rd 10 Days - Salvation", 21..30, Color(0xFF8B5CF6))
}

data class RamadanDayData(
    val dayNumber: Int,
    val baseSehriTime: String,
    val baseIftarTime: String,
    val weekdayBn: String,
    val weekdayEn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSehriIftarCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val offset = viewModel.selectedIslamicDistrictOffsetMinutes

    var showDistrictSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = আজকের সময়সূচি ও আমল, 1 = ৩০ দিনের রমজান ক্যালেন্ডার
    var selectedDecade by remember { mutableStateOf(RamadanDecade.ALL) }
    var showRamadanFiqhDialog by remember { mutableStateOf(false) }

    // TTS Setup
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try {
                    tts?.language = Locale("ar")
                } catch (e: Exception) {
                    tts?.language = Locale.ENGLISH
                }
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Live 1-second ticker
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Fasting Tracker Storage
    val todayDateKey = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    }
    val sharedPrefs = remember {
        context.getSharedPreferences("fasting_tracker_prefs", Context.MODE_PRIVATE)
    }

    var isFastingToday by remember {
        mutableStateOf(sharedPrefs.getBoolean("${todayDateKey}_fasting", false))
    }
    var totalFastingCount by remember {
        mutableIntStateOf(sharedPrefs.getInt("total_fasting_count", 0))
    }

    fun toggleFasting() {
        val newVal = !isFastingToday
        isFastingToday = newVal
        sharedPrefs.edit().putBoolean("${todayDateKey}_$todayDateKey", newVal).apply()
        val newCount = if (newVal) totalFastingCount + 1 else maxOf(0, totalFastingCount - 1)
        totalFastingCount = newCount
        sharedPrefs.edit().putInt("total_fasting_count", newCount).apply()

        val msg = if (isBn) {
            if (newVal) "মাশাআল্লাহ! আজকের রোজা রেকর্ড সম্পন্ন।" else "রোজার রেকর্ড বাতিল করা হয়েছে।"
        } else {
            if (newVal) "MashaAllah! Today's fast recorded." else "Fast record undone."
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    // Base Timings
    val calSehriToday = parseTimeToCal("04:46 AM", offset, 0)
    val calFajrToday = parseTimeToCal("04:52 AM", offset, 0)
    val calIftarToday = parseTimeToCal("06:18 PM", offset, 0)
    val calMaghribToday = parseTimeToCal("06:20 PM", offset, 0)
    val calSehriTomorrow = parseTimeToCal("04:46 AM", offset, 1)

    val now = currentTimeMillis

    // Fasting Phase Determination
    val (phaseTitleBn, phaseTitleEn, targetTitleBn, targetTitleEn, targetMillis, isFastingActive, isSehriActive) = when {
        now < calSehriToday.timeInMillis -> {
            PhaseData("এখন সেহরির সময়", "Sehri Time Active", "সেহরি শেষ হতে বাকি:", "Sehri Ends In:", calSehriToday.timeInMillis, false, true)
        }
        now < calIftarToday.timeInMillis -> {
            PhaseData("এখন পবিত্র রোজা পালনের সময়", "Fasting Hours Active", "ইফতার হতে বাকি সময়:", "Iftar Starts In:", calIftarToday.timeInMillis, true, false)
        }
        else -> {
            PhaseData("আজকের ইফতার সম্পন্ন হয়েছে", "Today's Iftar Completed", "আগামীকালের সেহরি সমাপ্তির বাকি:", "Tomorrow's Sehri Ends In:", calSehriTomorrow.timeInMillis, false, false)
        }
    }

    val countdownMillis = maxOf(0L, targetMillis - now)
    val totalFastingDuration = (calIftarToday.timeInMillis - calSehriToday.timeInMillis).toFloat()
    val elapsedFastingDuration = (now - calSehriToday.timeInMillis).toFloat()
    val fastingProgress = if (isFastingActive) (elapsedFastingDuration / totalFastingDuration).coerceIn(0f, 1f) else if (now >= calIftarToday.timeInMillis) 1f else 0f

    // Hero Cockpit Dynamic Gradient
    val heroGradient = when {
        isSehriActive -> listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0284C7))
        isFastingActive -> listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFF59E0B))
        else -> listOf(Color(0xFF4C1D95), Color(0xFF701A75), Color(0xFFBE185D))
    }

    // 30 Days Ramadan Timetable Model
    val ramadanDays = remember {
        val days = mutableListOf<RamadanDayData>()
        val weekdaysBn = listOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")
        val weekdaysEn = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        var curSehriMinutes = 4 * 60 + 58
        var curIftarMinutes = 18 * 60 + 8

        for (i in 1..30) {
            val sHour = curSehriMinutes / 60
            val sMin = curSehriMinutes % 60
            val iHour = curIftarMinutes / 60 - 12
            val iMin = curIftarMinutes % 60

            val sStr = String.format(Locale.ENGLISH, "%02d:%02d AM", sHour, sMin)
            val iStr = String.format(Locale.ENGLISH, "%02d:%02d PM", iHour, iMin)

            days.add(
                RamadanDayData(
                    dayNumber = i,
                    baseSehriTime = sStr,
                    baseIftarTime = iStr,
                    weekdayBn = weekdaysBn[(i - 1) % 7],
                    weekdayEn = weekdaysEn[(i - 1) % 7]
                )
            )
            // Gradual shifting over 30 days
            if (i % 3 == 0) curSehriMinutes -= 1
            if (i % 2 == 0) curIftarMinutes += 1
        }
        days
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBn) "সেহরি ও ইফতার সূচি" else "Sehri & Iftar Schedule",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showRamadanFiqhDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Fiqh Info",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = if (isBn) "লাইভ কাউন্টডাউন ও রমজান ক্যালেন্ডার" else "Live Countdown & Ramadan Calendar",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.65f)
                )
            }

            // District Switcher Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFD97706).copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.3f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showDistrictSheet = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "District",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) viewModel.selectedIslamicDistrictBn.split(" ")[0] else viewModel.selectedIslamicDistrictEn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // --- 1. HERO FASTING ORBIT COCKPIT ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(heroGradient))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFastingActive) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) phaseTitleBn else phaseTitleEn,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isBn) "লাইভ মোড" else "Live",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Countdown & Progress Ring
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) targetTitleBn else targetTitleEn,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            val (h, m, s) = formatCountdownUnits(countdownMillis, isBn)
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                DigitBox(h, isBn = isBn, unit = if (isBn) "ঘণ্টা" else "hr")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(":", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                DigitBox(m, isBn = isBn, unit = if (isBn) "মিনিট" else "min")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(":", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                DigitBox(s, isBn = isBn, unit = if (isBn) "সেকেন্ড" else "sec")
                            }
                        }

                        // Circular Progress Indicator for Fasting
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color.White.copy(alpha = 0.2f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = Color.White,
                                    startAngle = -90f,
                                    sweepAngle = fastingProgress * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(fastingProgress * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isBn) "রোজা সম্পন্ন" else "Fasted",
                                    fontSize = 8.5.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Linear Smooth Progress Bar
                    LinearProgressIndicator(
                        progress = { fastingProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
            }
        }

        // --- 2. NAVIGATION TABS: আজকের সময়সূচি বনাম ৩০ দিনের ক্যালেন্ডার ---
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = themeColors.background,
            contentColor = Color(0xFFD97706),
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = if (isBn) "আজকের সময়সূচি ও আমল" else "Today's Schedule & Deeds",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = if (isBn) "৩০ দিনের রমজান সূচি" else "30-Day Ramadan Calendar",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            )
        }

        if (selectedTab == 0) {
            // ================== TAB 1: TODAY'S DETAILED VIEW & DUAS ==================

            // Side-by-Side Timing Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sehri Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "সেহরির শেষ সময়" else "Sehri Ends",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                        Text(
                            text = adjustIslamicTimeStr("04:46 AM", offset),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.1f),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = if (isBn) "ফজর: ${adjustIslamicTimeStr("04:52 AM", offset)}" else "Fajr: ${adjustIslamicTimeStr("04:52 AM", offset)}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Iftar Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                    border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD97706).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "ইফতারের সময়" else "Iftar Time",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                        Text(
                            text = adjustIslamicTimeStr("06:18 PM", offset),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD97706).copy(alpha = 0.1f),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = if (isBn) "মাগরিব: ${adjustIslamicTimeStr("06:20 PM", offset)}" else "Maghrib: ${adjustIslamicTimeStr("06:20 PM", offset)}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Daily Fasting Tracker Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFastingToday) Color(0xFF10B981).copy(alpha = 0.12f) else themeColors.cardBg
                ),
                border = BorderStroke(1.dp, if (isFastingToday) Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { toggleFasting() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFastingToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Fasting",
                                tint = if (isFastingToday) Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.4f),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "আজকের রোজা রেখেছি" else "I am Fasting Today",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFastingToday) Color(0xFF10B981) else themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "মোট রোজা সম্পন্ন: $totalFastingCount দিন" else "Total Fasted: $totalFastingCount days",
                                fontSize = 11.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Button(
                        onClick = { toggleFasting() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFastingToday) Color(0xFF10B981) else Color(0xFFD97706)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isFastingToday) (if (isBn) "সম্পন্ন ✓" else "Completed ✓") else (if (isBn) "টিক দিন" else "Mark"),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // --- 3. MASNOON DUAS: রোজার নিয়ত ও ইফতারের দোয়া ---
            Text(
                text = if (isBn) "রোজার নিয়ত ও ইফতারের দোয়া" else "Fasting Niyyah & Iftar Duas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            // Niyyah Card
            DuaActionCard(
                title = if (isBn) "১. রোজার নিয়ত (সেহরির দোয়া)" else "1. Intention for Fasting (Niyyah)",
                arabic = "نَوَيْتُ اَنْ اُصُوْمَ غَدًا مِّنْ شَهْرِ رَمَضَانَ الْمُبَارَكِ فَرْضًا لَّكَ يَا اللهُ فَتَقَبَّلْ مِنِّى اِنَّكَ اَنْتَ السَّمِيْعُ الْعَلِيْمُ",
                transliteration = if (isBn) "উচ্চারণ: নাওয়াইতু আন আসুমা গাদাম মিন শাহরি রামাদানাল মুবারাকি ফারদাল্লাকা ইয়া আল্লাহু ফাতাকাব্বাল মিন্নি ইন্নাকা আনতাস সামিউল আলিম।" else "Pronunciation: Nawaitu an asuma ghadam min shahri ramadanal mubaraki fardallaka ya Allahu fataqabbal minni innaka antas-sami'ul 'aleem.",
                meaning = if (isBn) "অর্থ: হে আল্লাহ! আমি আগামীকাল পবিত্র রমজান মাসের তোমার নির্ধারিত ফরজ রোজা রাখার নিয়ত করলাম। অতএব তুমি আমার পক্ষ থেকে তা কবুল করো। নিশ্চয়ই তুমি সর্বশ্রোতা ও সর্বজ্ঞানী।" else "Meaning: O Allah! I intend to fast tomorrow for Your sake in the blessed month of Ramadan. So accept it from me, indeed You are the All-Hearing, All-Knowing.",
                themeColors = themeColors,
                context = context,
                tts = tts,
                isBn = isBn
            )

            // Iftar Dua Card
            DuaActionCard(
                title = if (isBn) "২. ইফতারের দোয়া" else "2. Dua for Breaking Fast (Iftar)",
                arabic = "اللَّهُمَّ لَكَ صُمْتُ وَعَلَى رِزْقِكَ أَفْطَرْتُ",
                transliteration = if (isBn) "উচ্চারণ: আল্লাহুম্মা লাকা সুমতু ওয়া 'আলা রিযক্বিকা আফতারতু।" else "Pronunciation: Allahumma laka sumtu wa 'ala rizqika aftartu.",
                meaning = if (isBn) "অর্থ: হে আল্লাহ! আমি তোমার সন্তুষ্টির জন্যই রোজা রেখেছিলাম এবং তোমারই দেওয়া রিজিক দ্বারা ইফতার করলাম।" else "Meaning: O Allah! I fasted for Your sake and I am breaking my fast with Your provision. (Abu Dawud)",
                themeColors = themeColors,
                context = context,
                tts = tts,
                isBn = isBn
            )

            // Tarabi Tasbih Card
            DuaActionCard(
                title = if (isBn) "৩. তারাবিহ নামাজের তাসবীহ" else "3. Taraweeh Prayer Tasbih",
                arabic = "سُبْحَانَ ذِى الْمُلْكِ وَالْمَلَكُوْتِ سُبْحَانَ ذِى الْعِزَّةِ وَالْعَظْمَةِ وَالْهَيْبَةِ وَالْقُدْرَةِ وَالْكِبْرِيَاءِ وَالْجَبَرُوْتِ سُبْحَانَ الْمَلِكِ الْحَىِّ الَّذِىْ لاَ يَنَامُ وَلاَ يَمُوْتُ اَبَدًا اَبَدًا سُبُّوْحٌ قُدُّوْسٌ رَبُّنَا وَرَبُّ الْمَلاَئِكَةِ وَالرُّوْحِ",
                transliteration = if (isBn) "উচ্চারণ: সুব্হানা জিল মুলকি ওয়াল মালাকূতি, সুব্হানা জিল 'ইয্যাতি ওয়াল 'আজমাতি ওয়াল হাইবাতি ওয়াল কুদরাতি ওয়াল কিবরিয়া-ই ওয়াল জাবারূত..." else "Pronunciation: Subhana dhil-mulki wal-malakoot...",
                meaning = if (isBn) "অর্থ: আল্লাহ পবিত্র, যিনি আসমান ও জমিনের রাজত্ব এবং সার্বভৌমত্বের মালিক। তিনি অতি পবিত্র, যিনি সর্বশক্তিমান ও চিরঞ্জীব।" else "Meaning: Glory be to the Owner of the Dominion and Sovereignty, the Ever-Living who never sleeps nor dies.",
                themeColors = themeColors,
                context = context,
                tts = tts,
                isBn = isBn
            )

        } else {
            // ================== TAB 2: FULL 30-DAY RAMADAN TIMETABLE ==================

            // Decade Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                RamadanDecade.entries.forEach { decade ->
                    val isSelected = selectedDecade == decade
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) decade.accentColor else themeColors.cardBg,
                        border = if (!isSelected) BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedDecade = decade }
                    ) {
                        Text(
                            text = if (isBn) decade.titleBn else decade.titleEn,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Timetable Table
            val displayedDays = ramadanDays.filter { day ->
                selectedDecade == RamadanDecade.ALL || day.dayNumber in selectedDecade.dayRange
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                displayedDays.forEach { item ->
                    val isToday = (item.dayNumber == 1) // Highlight Day 1 or current
                    val adjustedSehri = adjustIslamicTimeStr(item.baseSehriTime, offset)
                    val adjustedIftar = adjustIslamicTimeStr(item.baseIftarTime, offset)

                    val decadeTag = when (item.dayNumber) {
                        in 1..10 -> if (isBn) "রহমত" else "Mercy"
                        in 11..20 -> if (isBn) "মাগফিরাত" else "Forgiveness"
                        else -> if (isBn) "নাজাত" else "Salvation"
                    }
                    val decadeColor = when (item.dayNumber) {
                        in 1..10 -> Color(0xFF10B981)
                        in 11..20 -> Color(0xFF0284C7)
                        else -> Color(0xFF8B5CF6)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isToday) Color(0xFFD97706).copy(alpha = 0.12f) else themeColors.cardBg
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isToday) Color(0xFFD97706) else themeColors.displayText.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isToday) Color(0xFFD97706) else themeColors.displayText.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isBn) "${item.dayNumber}ম" else "${item.dayNumber}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) Color.White else themeColors.displayText
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isBn) "${item.dayNumber} রমজান (${item.weekdayBn})" else "Ramadan ${item.dayNumber} (${item.weekdayEn})",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isToday) Color(0xFFD97706) else themeColors.displayText
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = decadeColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = decadeTag,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = decadeColor,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (isBn) "সেহরি শেষ: $adjustedSehri" else "Sehri: $adjustedSehri",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0284C7),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isBn) "ইফতার: $adjustedIftar" else "Iftar: $adjustedIftar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDistrictSheet) {
        DistrictSelectionSheet(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { showDistrictSheet = false }
        )
    }

    if (showRamadanFiqhDialog) {
        AlertDialog(
            onDismissRequest = { showRamadanFiqhDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFD97706))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "রোজার জরুরি মাসআলা ও বিধান" else "Essential Ramadan Fiqh Rules",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isBn) "১. যেসব কারণে রোজা ভাঙ্গে না:" else "1. Things that DO NOT break fast:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = if (isBn) "• ভুলবশত কিছু খেলে বা পান করলে রোজা ভাঙ্গে না (মনে পড়ার সাথে সাথে খাওয়া বন্ধ করতে হবে)।\n• অনিচ্ছাকৃত বমি হলে বা নাকের ড্রপ/চোখের ড্রপ ব্যবহারে।\n• রক্ত পরীক্ষা দিলে বা ইনজেকশন/ইনসুলিন নিলে।"
                        else "• Eating or drinking out of forgetfulness.\n• Involuntary vomiting, applying eye drops.\n• Blood testing, necessary medical injections/insulin."
                    )
                    Text(
                        text = if (isBn) "২. যেসব কারণে রোজা ভেঙ্গে যায় ও কাজা করতে হয়:" else "2. Things that invalidate fast:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    Text(
                        text = if (isBn) "• ইচ্ছাকৃতভাবে পানাহার বা ধূমপান করলে।\n• মুখে কিছু দিয়ে ইচ্ছাকৃতভাবে বমি করলে।\n• সেহরির সময় শেষ হয়ে যাওয়ার পর পানাহার করলে।"
                        else "• Deliberate eating, drinking or smoking.\n• Deliberately inducing vomiting.\n• Eating after Sehri deadline has passed."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRamadanFiqhDialog = false }) {
                    Text(text = if (isBn) "ঠিক আছে" else "Close", color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun DigitBox(value: String, isBn: Boolean, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Text(
            text = unit,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun DuaActionCard(
    title: String,
    arabic: String,
    transliteration: String,
    meaning: String,
    themeColors: CalculatorThemeColors,
    context: Context,
    tts: TextToSpeech?,
    isBn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )

                Row {
                    // TTS Audio Playback
                    IconButton(
                        onClick = {
                            tts?.speak(arabic, TextToSpeech.QUEUE_FLUSH, null, "DUA_PLAY")
                            Toast.makeText(context, if (isBn) "অডিও পাঠ হচ্ছে..." else "Playing audio...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Play Dua",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Copy to Clipboard
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Dua", "$title\n\n$arabic\n\n$transliteration\n\n$meaning")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, if (isBn) "দোয়া কপি হয়েছে!" else "Dua copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Dua",
                            tint = themeColors.displayText.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "$title\n\n$arabic\n\n$transliteration\n\n$meaning\n\n- কুইক ক্যালকুলেটর ও ইসলামিক টুলস")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Dua"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = themeColors.displayText.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Text
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFD97706).copy(alpha = 0.07f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = arabic,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    lineHeight = 28.sp,
                    color = Color(0xFFB45309),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = transliteration,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = themeColors.displayText.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = meaning,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = themeColors.displayText.copy(alpha = 0.65f)
            )
        }
    }
}

private data class PhaseData(
    val phaseBn: String,
    val phaseEn: String,
    val targetBn: String,
    val targetEn: String,
    val targetTimeMillis: Long,
    val isFasting: Boolean,
    val isSehri: Boolean
)

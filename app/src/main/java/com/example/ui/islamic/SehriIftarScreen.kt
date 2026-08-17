package com.example.ui.islamic

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.namaz.NamazViewModel
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

enum class RamadanDecade(val titleBn: String, val titleEn: String, val dayRange: IntRange, val accentColor: Color) {
    ALL("সকল ৩০ দিন", "All 30 Days", 1..30, Color(0xFFD97706)),
    RAHMAT("১ম দশক - রহমত", "1st 10 Days - Mercy", 1..10, Color(0xFF10B981)),
    MAGFIRAT("২য় দশক - মাগফিরাত", "2nd 10 Days - Forgiveness", 11..20, Color(0xFF0284C7)),
    NAJAT("৩য় দশক - নাজাত", "3rd 10 Days - Salvation", 21..30, Color(0xFF8B5CF6))
}

data class RamadanDayData(
    val dayNumber: Int,
    val dateStrBn: String,
    val dateStrEn: String,
    val weekdayBn: String,
    val weekdayEn: String,
    val sehriTime: String,
    val fajrTime: String,
    val iftarTime: String,
    val maghribTime: String,
    val isToday: Boolean,
    val isFriday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSehriIftarCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var showDistrictSheet by remember { mutableStateOf(false) }
    var selectedDecade by remember { mutableStateOf(RamadanDecade.ALL) }
    var showRamadanFiqhDialog by remember { mutableStateOf(false) }

    // TTS Setup for Arabic Duas
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

    // Live 1-second dynamic ticker
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
        sharedPrefs.edit().putBoolean("${todayDateKey}_fasting", newVal).apply()
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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.autoDetectIslamicLocation(context) { success, msg ->
                if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            val msg = if (isBn) "স্বয়ংক্রিয় লোকেশন শনাক্তের জন্য লোকেশন পারমিশন দিন" else "Location permission needed for auto-detect"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val todayCal = remember(currentTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val currentDayOfYear = todayCal.get(Calendar.DAY_OF_YEAR)
    val timingsToday = remember(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, currentDayOfYear) {
        NamazTimeService.getPrayerTimesForCoordinates(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, todayCal)
    }

    val tomorrowCal = remember(currentTimeMillis) {
        Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }
    val currentDayOfYearTomorrow = tomorrowCal.get(Calendar.DAY_OF_YEAR)
    val timingsTomorrow = remember(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, currentDayOfYearTomorrow) {
        NamazTimeService.getPrayerTimesForCoordinates(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, tomorrowCal)
    }

    // Accurate Timings & Timestamps
    val calSehriToday = parseTimeToCal(timingsToday.sahri, 0, 0)
    val calFajrToday = parseTimeToCal(timingsToday.fajr, 0, 0)
    val calIftarToday = parseTimeToCal(timingsToday.maghrib, 0, 0)
    val calSehriTomorrow = parseTimeToCal(timingsTomorrow.sahri, 0, 1)

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
    val sehriCountdownMillis = maxOf(0L, if (now < calSehriToday.timeInMillis) calSehriToday.timeInMillis - now else calSehriTomorrow.timeInMillis - now)
    val iftarCountdownMillis = maxOf(0L, calIftarToday.timeInMillis - now)

    val totalFastingDurationMillis = (calIftarToday.timeInMillis - calSehriToday.timeInMillis).coerceAtLeast(1L)
    val elapsedFastingMillis = (now - calSehriToday.timeInMillis).coerceAtLeast(0L)
    val fastingProgress = if (isFastingActive) {
        (elapsedFastingMillis.toFloat() / totalFastingDurationMillis.toFloat()).coerceIn(0f, 1f)
    } else if (now >= calIftarToday.timeInMillis) 1f else 0f

    // Total Fasting Duration Calculation
    val totalFastingMinutes = remember(timingsToday) {
        val sehriMin = NamazTimeService.timeStrToMinutes(timingsToday.sahri)
        val iftarMin = NamazTimeService.timeStrToMinutes(timingsToday.maghrib)
        (iftarMin - sehriMin).coerceAtLeast(0)
    }
    val fastingHours = totalFastingMinutes / 60
    val fastingMins = totalFastingMinutes % 60
    val fastingDurationStr = if (isBn) {
        "${IslamicCalendarHelper.toBnDigits(fastingHours)} ঘণ্টা ${IslamicCalendarHelper.toBnDigits(fastingMins)} মিনিট"
    } else {
        "${fastingHours}h ${fastingMins}m"
    }

    // Hero Gradient
    val heroGradient = when {
        isSehriActive -> listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0284C7))
        isFastingActive -> listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFF59E0B))
        else -> listOf(Color(0xFF4C1D95), Color(0xFF701A75), Color(0xFFBE185D))
    }

    // 30-Day Ramadan Timetable Data (Generated offline using exact prayer calculations)
    val ramadanDays = remember(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, isBn) {
        val days = mutableListOf<RamadanDayData>()
        val banglaDayShort = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
        val englishDayShort = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val banglaMonths = listOf(
            "জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে"
        )
        val englishMonths = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )

        val cal = Calendar.getInstance()
        for (i in 1..30) {
            val timings = NamazTimeService.getPrayerTimesForCoordinates(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, cal)
            val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7
            val dNum = cal.get(Calendar.DAY_OF_MONTH)
            val mIdx = cal.get(Calendar.MONTH)

            val dateStrBn = "${IslamicCalendarHelper.toBnDigits(dNum)} ${banglaMonths[mIdx]}"
            val dateStrEn = "$dNum ${englishMonths[mIdx]}"
            val isFriday = (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)

            days.add(
                RamadanDayData(
                    dayNumber = i,
                    dateStrBn = dateStrBn,
                    dateStrEn = dateStrEn,
                    weekdayBn = banglaDayShort[dayOfWeek],
                    weekdayEn = englishDayShort[dayOfWeek],
                    sehriTime = timings.sahri,
                    fajrTime = timings.fajr,
                    iftarTime = timings.maghrib,
                    maghribTime = timings.maghrib,
                    isToday = (i == 1),
                    isFriday = isFriday
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        days
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- TOP HEADER BAR: Section Title, Fiqh Info & District Switcher ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isBn) "সেহরি ও ইফতার সূচি" else "Sehri & Iftar Schedule",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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
                        text = if (isBn) "লাইভ কাউন্টডাউন ও ৩০ দিনের সূচি" else "Live Countdown & 30-Day Timetable",
                        fontSize = 11.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.65f)
                    )
                }

                // Location Actions (GPS Auto Detect + District Switcher Pill)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick GPS Auto-Detect Button
                    Surface(
                        shape = CircleShape,
                        color = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF0284C7).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFF0284C7).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !viewModel.isDetectingIslamicLocation) {
                                if (IslamicLocationHelper.hasLocationPermission(context)) {
                                    viewModel.autoDetectIslamicLocation(context) { success, msg ->
                                        if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (viewModel.isDetectingIslamicLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 1.8.dp,
                                    color = Color(0xFF0284C7)
                                )
                            } else {
                                Icon(
                                    imageVector = if (viewModel.isIslamicLocationAutoDetected) Icons.Default.MyLocation else Icons.Default.GpsFixed,
                                    contentDescription = "GPS Auto Location",
                                    tint = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFF0284C7),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // District Switcher Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFD97706).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981).copy(alpha = 0.35f) else Color(0xFFD97706).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showDistrictSheet = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (viewModel.isIslamicLocationAutoDetected) Icons.Default.MyLocation else Icons.Default.LocationOn,
                                contentDescription = "District",
                                tint = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFFD97706),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isBn) viewModel.selectedIslamicDistrictBn.split(" ")[0] else viewModel.selectedIslamicDistrictEn,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFFD97706)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFFD97706),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // --- 1. DUAL HERO STATUS CARDS (সেহরি ও ইফতার - EQUAL HEIGHT WITH PROGRESS BARS) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // LEFT CARD: সেহরি (Sehri)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .shadow(4.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSehriActive) Color(0xFF0284C7).copy(alpha = 0.14f) else themeColors.cardBg
                    ),
                    border = BorderStroke(1.2.dp, if (isSehriActive) Color(0xFF0284C7) else themeColors.displayText.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isBn) "আজকের সেহরি" else "Today's Sehri",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isSehriActive) Color(0xFF0284C7) else themeColors.displayText.copy(alpha = 0.25f))
                                )
                            }

                            Text(
                                text = if (isBn) "সেহরি শেষ" else "Sehri Ends",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0369A1),
                                maxLines = 1
                            )

                            Text(
                                text = timingsToday.sahri,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColors.displayText
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.1f),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = if (isBn) "ফজর: ${timingsToday.fajr}" else "Fajr: ${timingsToday.fajr}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Countdown badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE0F2FE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "-${formatTimerClock(sehriCountdownMillis)}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF0284C7),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                                )
                            }

                            // Progress bar
                            val sehriProgress = remember(now, calSehriToday) {
                                if (now < calSehriToday.timeInMillis) {
                                    val start = calSehriToday.timeInMillis - (6 * 3600000L) // last 6 hours
                                    val elapsed = (now - start).coerceAtLeast(0L)
                                    (elapsed.toFloat() / (6 * 3600000f)).coerceIn(0f, 1f)
                                } else 1f
                            }
                            LinearProgressIndicator(
                                progress = { sehriProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF0284C7),
                                trackColor = Color(0xFFBAE6FD).copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // RIGHT CARD: ইফতার (Iftar)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .shadow(4.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFastingActive) Color(0xFFD97706).copy(alpha = 0.12f) else themeColors.cardBg
                    ),
                    border = BorderStroke(1.2.dp, if (isFastingActive) Color(0xFFD97706) else themeColors.displayText.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isBn) "আজকের ইফতার" else "Today's Iftar",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isFastingActive) Color(0xFFD97706) else themeColors.displayText.copy(alpha = 0.25f))
                                )
                            }

                            Text(
                                text = if (isBn) "ইফতারের সময়" else "Iftar Time",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB45309),
                                maxLines = 1
                            )

                            Text(
                                text = timingsToday.maghrib,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColors.displayText
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFD97706).copy(alpha = 0.1f),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = if (isBn) "মাগরিব: ${timingsToday.maghrib}" else "Maghrib: ${timingsToday.maghrib}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Countdown badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "-${formatTimerClock(iftarCountdownMillis)}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFD97706),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                                )
                            }

                            // Fasting Progress towards Iftar
                            LinearProgressIndicator(
                                progress = { fastingProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFD97706),
                                trackColor = Color(0xFFFDE68A).copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // --- 2. LIVE COCKPIT HERO CARD (লাইভ ফেজ, কাউন্টডাউন ডিজিট ও সার্কুলার প্রগ্রেস) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(heroGradient))
                        .padding(horizontal = 14.dp, vertical = 14.dp)
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
                                    modifier = Modifier.size(17.dp)
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
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (isBn) "লাইভ ট্র্যাকার" else "Live Tracker",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Countdown Digits & Circular Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBn) targetTitleBn else targetTitleEn,
                                    fontSize = 12.5.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                val (h, m, s) = formatCountdownUnits(countdownMillis, isBn)
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    DigitBox(h, isBn = isBn, unit = if (isBn) "ঘণ্টা" else "hr")
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(":", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 19.sp)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    DigitBox(m, isBn = isBn, unit = if (isBn) "মিনিট" else "min")
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(":", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 19.sp)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    DigitBox(s, isBn = isBn, unit = if (isBn) "সেকেন্ড" else "sec")
                                }
                            }

                            // Circular Progress Ring
                            Box(
                                modifier = Modifier.size(76.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawArc(
                                        color = Color.White.copy(alpha = 0.2f),
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 6.5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = Color.White,
                                        startAngle = -90f,
                                        sweepAngle = fastingProgress * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 6.5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(fastingProgress * 100).toInt()}%",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isBn) "রোজা সম্পন্ন" else "Fasted",
                                        fontSize = 8.5.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Linear Smooth Progress
                        LinearProgressIndicator(
                            progress = { fastingProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.25f)
                        )
                    }
                }
            }

            // --- 3. SUNRISE, SUNSET & TOTAL FASTING DURATION CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sunrise (সূর্যোদয়)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Sunrise",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (isBn) "সূর্যোদয়" else "Sunrise",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText.copy(alpha = 0.65f)
                                )
                                Text(
                                    text = timingsToday.sunrise,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                            }
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(themeColors.displayText.copy(alpha = 0.12f))
                        )

                        // Sunset (সূর্যাস্ত)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEDD5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbTwilight,
                                    contentDescription = "Sunset",
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (isBn) "সূর্যাস্ত" else "Sunset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText.copy(alpha = 0.65f)
                                )
                                Text(
                                    text = timingsToday.maghrib,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fasting Duration Badge Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFD97706).copy(alpha = 0.08f),
                        border = BorderStroke(0.8.dp, Color(0xFFD97706).copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isBn) "আজকের রোজার মোট দৈর্ঘ্য: $fastingDurationStr" else "Today's Total Fasting Duration: $fastingDurationStr",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            // --- 4. DAILY FASTING TRACKER BAR ---
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
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { toggleFasting() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFastingToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Fasting",
                                tint = if (isFastingToday) Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.4f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (isBn) "আজকের রোজা রেখেছি" else "I am Fasting Today",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFastingToday) Color(0xFF10B981) else themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "মোট রোজা সম্পন্ন: ${IslamicCalendarHelper.toBnDigits(totalFastingCount)} দিন" else "Total Fasted: $totalFastingCount days",
                                fontSize = 10.5.sp,
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
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isFastingToday) (if (isBn) "সম্পন্ন ✓" else "Completed ✓") else (if (isBn) "টিক দিন" else "Mark"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // --- 5. 30-DAY RAMADAN & NAFL TIMETABLE (৩০ দিনের পূর্ণাঙ্গ সেহরি, ফজর, ইফতার ও মাগরিব) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "৩০ দিনের সেহরি ও ইফতার সূচি" else "30-Day Ramadan Timetable",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "সেহরি, ফজর, ইফতার ও মাগরিবের পূর্ণাঙ্গ সময়সূচি" else "Complete Sehri, Fajr, Iftar & Maghrib Table",
                            fontSize = 11.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }

                // Decade Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    RamadanDecade.entries.forEach { decade ->
                        val isSelected = selectedDecade == decade
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) decade.accentColor else themeColors.cardBg,
                            border = if (!isSelected) BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedDecade = decade }
                        ) {
                            Text(
                                text = if (isBn) decade.titleBn else decade.titleEn,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.8f),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                val displayedDays = ramadanDays.filter { day ->
                    selectedDecade == RamadanDecade.ALL || day.dayNumber in selectedDecade.dayRange
                }

                // Table Container with Smooth Single-Line Horizontal Scroll
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                    border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(4.dp)
                    ) {
                        // Table Header (Fixed Widths, Non-wrapping Single-line design)
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFD97706).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SehriIftarTableCell(text = if (isBn) "রমজান" else "Ramadan", width = 56.dp, isHeader = true, themeColors = themeColors)
                            SehriIftarTableCell(text = if (isBn) "তারিখ" else "Date", width = 64.dp, isHeader = true, themeColors = themeColors)
                            SehriIftarTableCell(text = if (isBn) "বার" else "Day", width = 45.dp, isHeader = true, themeColors = themeColors)
                            SehriIftarTableCell(text = if (isBn) "সেহরি শেষ" else "Sehri Ends", width = 76.dp, isHeader = true, themeColors = themeColors)
                            SehriIftarTableCell(text = if (isBn) "ফজর" else "Fajr", width = 72.dp, isHeader = true, themeColors = themeColors)
                            SehriIftarTableCell(text = if (isBn) "ইফতার" else "Iftar", width = 72.dp, isHeader = true, themeColors = themeColors)
                            SehriIftarTableCell(text = if (isBn) "মাগরিব" else "Maghrib", width = 72.dp, isHeader = true, themeColors = themeColors)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Table Body Rows (Strictly 1 Line per cell)
                        displayedDays.forEachIndexed { index, row ->
                            val rowBg = when {
                                row.isToday -> Color(0xFFD97706).copy(alpha = 0.14f)
                                row.isFriday -> Color(0xFF0284C7).copy(alpha = 0.06f)
                                else -> Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .background(rowBg, RoundedCornerShape(6.dp))
                                    .padding(vertical = 7.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Ramadan Day
                                SehriIftarTableCell(
                                    text = if (isBn) "${IslamicCalendarHelper.toBnDigits(row.dayNumber)}ম" else "${row.dayNumber}",
                                    width = 56.dp,
                                    isHighlight = row.isToday,
                                    themeColors = themeColors
                                )
                                // Date
                                SehriIftarTableCell(
                                    text = if (isBn) row.dateStrBn else row.dateStrEn,
                                    width = 64.dp,
                                    isHighlight = row.isToday,
                                    themeColors = themeColors
                                )
                                // Day of week
                                SehriIftarTableCell(
                                    text = if (isBn) row.weekdayBn else row.weekdayEn,
                                    width = 45.dp,
                                    isFriday = row.isFriday,
                                    isHighlight = row.isToday,
                                    themeColors = themeColors
                                )
                                // Sehri End Time
                                SehriIftarTableCell(
                                    text = row.sehriTime,
                                    width = 76.dp,
                                    textColor = Color(0xFF0284C7),
                                    isBold = true,
                                    themeColors = themeColors
                                )
                                // Fajr Start Time
                                SehriIftarTableCell(
                                    text = row.fajrTime,
                                    width = 72.dp,
                                    textColor = themeColors.displayText.copy(alpha = 0.85f),
                                    themeColors = themeColors
                                )
                                // Iftar Time
                                SehriIftarTableCell(
                                    text = row.iftarTime,
                                    width = 72.dp,
                                    textColor = Color(0xFFD97706),
                                    isBold = true,
                                    themeColors = themeColors
                                )
                                // Maghrib Time
                                SehriIftarTableCell(
                                    text = row.maghribTime,
                                    width = 72.dp,
                                    textColor = themeColors.displayText.copy(alpha = 0.85f),
                                    themeColors = themeColors
                                )
                            }

                            if (index < displayedDays.size - 1) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = themeColors.displayText.copy(alpha = 0.05f)
                                )
                            }
                        }
                    }
                }
            }

            // --- 6. MASNOON DUAS: রোজার নিয়ত, ইফতারের দোয়া ও তারাবিহ তাসবীহ ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBn) "রোজার প্রয়োজনীয় দোয়া ও আমল" else "Essential Fasting Duas & Deeds",
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
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
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
                        fontSize = 16.sp,
                        color = themeColors.displayText
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
                        else "• Eating or drinking out of forgetfulness.\n• Involuntary vomiting, applying eye drops.\n• Blood testing, necessary medical injections/insulin.",
                        color = themeColors.displayText,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = if (isBn) "২. যেসব কারণে রোজা ভেঙ্গে যায় ও কাজা করতে হয়:" else "2. Things that invalidate fast:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    Text(
                        text = if (isBn) "• ইচ্ছাকৃতভাবে পানাহার বা ধূমপান করলে।\n• মুখে কিছু দিয়ে ইচ্ছাকৃতভাবে বমি করলে।\n• সেহরির সময় শেষ হয়ে যাওয়ার পর পানাহার করলে।"
                        else "• Deliberate eating, drinking or smoking.\n• Deliberately inducing vomiting.\n• Eating after Sehri deadline has passed.",
                        color = themeColors.displayText,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRamadanFiqhDialog = false }) {
                    Text(text = if (isBn) "ঠিক আছে" else "Close", color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = themeColors.cardBg,
            titleContentColor = themeColors.displayText,
            textContentColor = themeColors.displayText,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SehriIftarTableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isHighlight: Boolean = false,
    isFriday: Boolean = false,
    isBold: Boolean = false,
    textColor: Color? = null,
    themeColors: CalculatorThemeColors
) {
    Text(
        text = text,
        fontSize = 12.sp,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        fontWeight = when {
            isHeader || isBold -> FontWeight.Bold
            isHighlight -> FontWeight.ExtraBold
            isFriday -> FontWeight.Bold
            else -> FontWeight.Medium
        },
        color = textColor ?: when {
            isHeader -> Color(0xFFD97706)
            isHighlight -> Color(0xFFD97706)
            isFriday -> Color(0xFF0284C7)
            else -> themeColors.displayText
        },
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}

private fun formatTimerClock(diffMillis: Long): String {
    if (diffMillis <= 0) return "00:00:00"
    val totalSeconds = diffMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)
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
                fontSize = 19.sp,
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
    val namazViewModel: NamazViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val playingDuaId by namazViewModel.playingDuaId.collectAsStateWithLifecycle()
    val isPlaying by namazViewModel.isPlaying.collectAsStateWithLifecycle()
    val downloadProgress by namazViewModel.downloadProgress.collectAsStateWithLifecycle()
    val downloadedDuaIds by namazViewModel.downloadedDuaIds.collectAsStateWithLifecycle()

    val duaId = "ramadan_dua_" + title.hashCode().absoluteValue.toString()
    val isCurrentPlaying = playingDuaId == duaId && isPlaying
    val progress = downloadProgress[duaId]
    val isDownloaded = downloadedDuaIds.contains(duaId)

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
                    color = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Audio Playback via NamazViewModel
                    if (progress != null && progress in 1..99) {
                        CircularProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFD97706)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                namazViewModel.playOrPauseDuaAudio(duaId, null, arabic, transliteration)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isCurrentPlaying) Icons.Default.PauseCircle else Icons.Default.VolumeUp,
                                contentDescription = "Play Dua",
                                tint = if (isCurrentPlaying) Color(0xFF10B981) else Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isDownloaded || progress == 100) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Downloaded Offline",
                            tint = Color(0xFF10B981),
                            modifier = Modifier
                                .size(14.dp)
                                .padding(horizontal = 2.dp)
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

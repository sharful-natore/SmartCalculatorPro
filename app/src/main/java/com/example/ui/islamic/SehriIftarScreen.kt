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
import androidx.compose.foundation.clickable
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
import com.example.util.CalendarUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

data class MonthDaySchedule(
    val dayNumber: Int,
    val dateStrBn: String,
    val dateStrEn: String,
    val weekdayBn: String,
    val weekdayEn: String,
    val sehriTime: String,
    val fajrTime: String,
    val sunriseTime: String,
    val dhuhrTime: String,
    val asrTime: String,
    val iftarTime: String,
    val ishaTime: String,
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
    var expandedDayNumber by remember { mutableStateOf<Int?>(null) }

    // Month Navigation State
    var currentMonthOffset by remember { mutableIntStateOf(0) }

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

    // Accurate Timings for Today
    val timingsToday = remember(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, currentDayOfYear) {
        NamazTimeService.getPrayerTimesForCoordinates(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, todayCal)
    }

    // Hijri/Arabic Date for Today
    val todayHijriDateStr = remember(currentTimeMillis, isBn) {
        IslamicCalendarHelper.getHijriDateString(todayCal, isBn)
    }

    // Timestamps for Countdown & Calculations
    val calSehriToday = parseTimeToCal(timingsToday.sahri, 0, 0)
    val calIftarToday = parseTimeToCal(timingsToday.maghrib, 0, 0)

    val now = currentTimeMillis
    val iftarCountdownMillis = maxOf(0L, calIftarToday.timeInMillis - now)
    val isIftarPassed = now >= calIftarToday.timeInMillis

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
        "${fastingHours} hours ${fastingMins} mins"
    }

    // Month Data Calculation (Full Month Schedule)
    val displayMonthCal = remember(currentTimeMillis, currentMonthOffset) {
        Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            add(Calendar.MONTH, currentMonthOffset)
        }
    }

    val displayMonthName = remember(displayMonthCal, isBn) {
        val gMonth = displayMonthCal.get(Calendar.MONTH)
        val gYear = displayMonthCal.get(Calendar.YEAR)
        val bnMonths = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
        val enMonth = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(displayMonthCal.time)
        if (isBn) "${bnMonths[gMonth]} ${IslamicCalendarHelper.toBnDigits(gYear)}" else enMonth
    }

    val daysInMonth = displayMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val isCurrentViewingMonth = (currentMonthOffset == 0)
    val todayDayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH)

    val monthScheduleList = remember(displayMonthCal, viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, isBn) {
        val days = mutableListOf<MonthDaySchedule>()
        val banglaDayShort = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
        val englishDayShort = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val banglaMonths = listOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")
        val englishMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        val mIdx = displayMonthCal.get(Calendar.MONTH)
        val year = displayMonthCal.get(Calendar.YEAR)

        for (d in 1..daysInMonth) {
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, mIdx)
                set(Calendar.DAY_OF_MONTH, d)
            }
            val timings = NamazTimeService.getPrayerTimesForCoordinates(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, dayCal)
            val dayOfWeek = (dayCal.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7
            val isFriday = (dayCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
            val isToday = isCurrentViewingMonth && (d == todayDayOfMonth)

            days.add(
                MonthDaySchedule(
                    dayNumber = d,
                    dateStrBn = "${IslamicCalendarHelper.toBnDigits(d)} ${banglaMonths[mIdx]}",
                    dateStrEn = "$d ${englishMonths[mIdx]}",
                    weekdayBn = banglaDayShort[dayOfWeek],
                    weekdayEn = englishDayShort[dayOfWeek],
                    sehriTime = stripAmPm(timings.sahri, isBn),
                    fajrTime = stripAmPm(timings.fajr, isBn),
                    sunriseTime = stripAmPm(timings.sunrise, isBn),
                    dhuhrTime = stripAmPm(timings.dhuhr, isBn),
                    asrTime = stripAmPm(timings.asr, isBn),
                    iftarTime = stripAmPm(timings.maghrib, isBn),
                    ishaTime = stripAmPm(timings.isha, isBn),
                    isToday = isToday,
                    isFriday = isFriday
                )
            )
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
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- TOP BAR: Title & District / Location Pill ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "সেহরি ও ইফতারের সময়সূচি" else "Sehri & Iftar Schedule",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "নিখুঁত ইসলামিক ফাউন্ডেশন বাংলাদেশ মান" else "Accurate IFB Prayer & Fasting Timings",
                        fontSize = 11.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.65f)
                    )
                }

                // Location Pill & GPS Detection Button
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
                            .size(30.dp)
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
                                    modifier = Modifier.size(15.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF0284C7)
                                )
                            } else {
                                Icon(
                                    imageVector = if (viewModel.isIslamicLocationAutoDetected) Icons.Default.MyLocation else Icons.Default.GpsFixed,
                                    contentDescription = "GPS Auto Location",
                                    tint = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // District Switcher Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFD97706).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFFD97706).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showDistrictSheet = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (viewModel.isIslamicLocationAutoDetected) Icons.Default.MyLocation else Icons.Default.LocationOn,
                                contentDescription = "District",
                                tint = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFFD97706),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) viewModel.selectedIslamicDistrictBn.split(" ")[0] else viewModel.selectedIslamicDistrictEn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFFD97706)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // --- 1. HERO CARD (অ্যাপের থিমের সাথে মিল রেখে) ---
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B),
                                    Color(0xFF0F766E)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Top row of Hero: Arabic/Hijri Date of the Day
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "🌙 ",
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = todayHijriDateStr,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFDE68A)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (isBn) "আজকের সূচি" else "Today's Schedule",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Middle: Sehri End Time & Iftar Time Side by Side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Sehri Box
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.22f),
                                border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isBn) "সেহরির শেষ সময়" else "Sehri Ends",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFBAE6FD)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.NightlightRound,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = stripAmPm(timingsToday.sahri, isBn),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isBn) "ফজর: ${stripAmPm(timingsToday.fajr, isBn)}" else "Fajr: ${stripAmPm(timingsToday.fajr, isBn)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFBAE6FD).copy(alpha = 0.8f)
                                    )
                                }
                            }

                            // Iftar Box
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFD97706).copy(alpha = 0.25f),
                                border = BorderStroke(1.2.dp, Color(0xFFFBBF24).copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isBn) "ইফতারের সময়" else "Iftar Time",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFDE68A)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.WbTwilight,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = stripAmPm(timingsToday.maghrib, isBn),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isBn) "মাগরিব: ${stripAmPm(timingsToday.maghrib, isBn)}" else "Maghrib: ${stripAmPm(timingsToday.maghrib, isBn)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFDE68A).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Bottom Hero Section: Iftar Countdown (ইফতারের কাউন্টডাউন)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isIftarPassed) {
                                            if (isBn) "আজকের ইফতারের সময় পার হয়েছে" else "Today's Iftar completed"
                                        } else {
                                            if (isBn) "ইফতার হতে বাকি সময়:" else "Iftar Countdown:"
                                        },
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDE68A)
                                    )
                                }

                                val (h, m, s) = formatCountdownUnits(iftarCountdownMillis, isBn)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    DigitBox(h, isBn = isBn, unit = if (isBn) "ঘণ্টা" else "Hours")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(":", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    DigitBox(m, isBn = isBn, unit = if (isBn) "মিনিট" else "Mins")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(":", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    DigitBox(s, isBn = isBn, unit = if (isBn) "সেকেন্ড" else "Secs")
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // --- 2. SECOND CARD (আজকের সূর্যোদয়, সূর্যাস্তের টাইম ও রোজার মোট সময়) ---
            // =========================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                border = BorderStroke(1.2.dp, themeColors.displayText.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Sunrise",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isBn) "আজকের সূর্যোদয়" else "Today's Sunrise",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText.copy(alpha = 0.65f)
                                )
                                Text(
                                    text = stripAmPm(timingsToday.sunrise, isBn),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                            }
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEDD5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbTwilight,
                                    contentDescription = "Sunset",
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isBn) "আজকের সূর্যাস্ত" else "Today's Sunset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText.copy(alpha = 0.65f)
                                )
                                Text(
                                    text = stripAmPm(timingsToday.maghrib, isBn),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                            }
                        }
                    }

                    // Total Fasting Duration Banner (রোজার মোট সময়)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F766E).copy(alpha = 0.09f),
                        border = BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "আজকের রোজার মোট সময়: " else "Total Fasting Duration: ",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                            Text(
                                text = fastingDurationStr,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F766E)
                            )
                        }
                    }
                }
            }

            // =============================================================================================
            // --- 3. FULL MONTH SCHEDULE (সম্পূর্ণ মাসের সময়সূচি - COMPACT & EXPANDABLE WITH ALL WAQTS) ---
            // =============================================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header of the schedule section with Month Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "সম্পূর্ণ মাসের সময়সূচি" else "Full Month Schedule",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "তারিখে ট্যাপ করে সকল ওয়াক্তের সময় দেখুন" else "Tap any date to expand all prayer times",
                            fontSize = 11.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }

                    // Month Navigator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { currentMonthOffset-- },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Month",
                                tint = themeColors.displayText
                            )
                        }
                        Text(
                            text = displayMonthName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                        IconButton(
                            onClick = { currentMonthOffset++ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Month",
                                tint = themeColors.displayText
                            )
                        }
                    }
                }

                // Table Column Header (Compact)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E).copy(alpha = 0.12f)),
                    border = BorderStroke(0.8.dp, Color(0xFF0F766E).copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "তারিখ ও দিন" else "Date & Day",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E),
                            modifier = Modifier.weight(1.3f)
                        )
                        Text(
                            text = if (isBn) "সেহরি" else "Sehri",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (isBn) "ইফতার" else "Iftar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (isBn) "বিস্তারিত" else "Details",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E).copy(alpha = 0.7f),
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }

                // Month Rows List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    monthScheduleList.forEach { row ->
                        val isExpanded = expandedDayNumber == row.dayNumber
                        CompactExpandableDayCard(
                            row = row,
                            isExpanded = isExpanded,
                            isBn = isBn,
                            themeColors = themeColors,
                            onToggle = {
                                expandedDayNumber = if (isExpanded) null else row.dayNumber
                            }
                        )
                    }
                }
            }

            // ==========================================
            // --- 4. SEHRI & IFTAR DUAS (সেহরি ও ইফতারের দোয়া) ---
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBn) "সেহরি ও ইফতারের দোয়া" else "Sehri & Iftar Duas",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )

                // 1. Sehri Dua (রোজার নিয়ত)
                DuaActionCard(
                    title = if (isBn) "১. রোজার নিয়ত (সেহরির দোয়া)" else "1. Intention for Fasting (Sehri Niyyah)",
                    arabic = "نَوَيْتُ اَنْ اُصُوْمَ غَدًا مِّنْ شَهْرِ رَمَضَانَ الْمُبَارَكِ فَرْضًا لَّكَ يَا اللهُ فَتَقَبَّلْ مِنِّى اِنَّكَ اَنْتَ السَّمِيْعُ الْعَلِيْمُ",
                    transliteration = if (isBn) "উচ্চারণ: নাওয়াইতু আন আসুমা গাদাম মিন শাহরি রামাদানাল মুবারাকি ফারদাল্লাকা ইয়া আল্লাহু ফাতাকাব্বাল মিন্নি ইন্নাকা আনতাস সামিউল আলিম।" else "Pronunciation: Nawaitu an asuma ghadam min shahri ramadanal mubaraki fardallaka ya Allahu fataqabbal minni innaka antas-sami'ul 'aleem.",
                    meaning = if (isBn) "অর্থ: হে আল্লাহ! আমি আগামীকাল পবিত্র রমজান মাসের তোমার নির্ধারিত ফরজ রোজা রাখার নিয়ত করলাম। অতএব তুমি আমার পক্ষ থেকে তা কবুল করো। নিশ্চয়ই তুমি সর্বশ্রোতা ও সর্বজ্ঞানী।" else "Meaning: O Allah! I intend to fast tomorrow for Your sake in the blessed month of Ramadan. So accept it from me, indeed You are the All-Hearing, All-Knowing.",
                    themeColors = themeColors,
                    context = context,
                    tts = tts,
                    isBn = isBn
                )

                // 2. Iftar Dua (ইফতারের দোয়া)
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
}

/**
 * Compact Expandable Day Card
 * Collapsed: Date + Day + Sehri + Iftar
 * Expanded: All Waqt Times (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha, Sehri)
 */
@Composable
private fun CompactExpandableDayCard(
    row: MonthDaySchedule,
    isExpanded: Boolean,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onToggle: () -> Unit
) {
    val cardBg = when {
        row.isToday -> Color(0xFF0F766E).copy(alpha = 0.12f)
        row.isFriday -> Color(0xFF0284C7).copy(alpha = 0.05f)
        else -> themeColors.cardBg
    }
    val cardBorder = when {
        row.isToday -> Color(0xFF0F766E)
        isExpanded -> Color(0xFFD97706).copy(alpha = 0.5f)
        else -> themeColors.displayText.copy(alpha = 0.07f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(if (row.isToday || isExpanded) 1.2.dp else 0.8.dp, cardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Compact Row (No AM/PM)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Date & Day
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.3f)
                ) {
                    if (row.isToday) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF0F766E),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = if (isBn) "আজ" else "Today",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isBn) row.dateStrBn else row.dateStrEn,
                            fontSize = 13.sp,
                            fontWeight = if (row.isToday) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (row.isToday) Color(0xFF0F766E) else themeColors.displayText
                        )
                        Text(
                            text = if (isBn) row.weekdayBn else row.weekdayEn,
                            fontSize = 10.5.sp,
                            fontWeight = if (row.isFriday) FontWeight.Bold else FontWeight.Normal,
                            color = if (row.isFriday) Color(0xFF0284C7) else themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }

                // Sehri Time (No AM/PM)
                Text(
                    text = row.sehriTime,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                // Iftar Time (No AM/PM)
                Text(
                    text = row.iftarTime,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                // Expand Indicator Icon
                Box(
                    modifier = Modifier.width(36.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Prayer Times",
                        tint = if (isExpanded) Color(0xFFD97706) else themeColors.displayText.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded Section: All Waqt Times for this Day
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD97706).copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = Color(0xFFD97706).copy(alpha = 0.2f)
                    )

                    Text(
                        text = if (isBn) "আজকের সকল ওয়াক্তের নামাজের সময়:" else "All Prayer Waqt Times for this Day:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )

                    // 6-Grid Waqt Times
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WaqtMiniBadge(title = if (isBn) "ফজর" else "Fajr", time = row.fajrTime, themeColors = themeColors, modifier = Modifier.weight(1f))
                        WaqtMiniBadge(title = if (isBn) "সূর্যোদয়" else "Sunrise", time = row.sunriseTime, themeColors = themeColors, modifier = Modifier.weight(1f))
                        WaqtMiniBadge(title = if (isBn) "যোহর" else "Dhuhr", time = row.dhuhrTime, themeColors = themeColors, modifier = Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WaqtMiniBadge(title = if (isBn) "আসর" else "Asr", time = row.asrTime, themeColors = themeColors, modifier = Modifier.weight(1f))
                        WaqtMiniBadge(title = if (isBn) "মাগরিব" else "Maghrib", time = row.iftarTime, isHighlight = true, themeColors = themeColors, modifier = Modifier.weight(1f))
                        WaqtMiniBadge(title = if (isBn) "এশা" else "Isha", time = row.ishaTime, themeColors = themeColors, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WaqtMiniBadge(
    title: String,
    time: String,
    isHighlight: Boolean = false,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isHighlight) Color(0xFFD97706).copy(alpha = 0.15f) else themeColors.cardBg,
        border = BorderStroke(
            1.dp,
            if (isHighlight) Color(0xFFD97706).copy(alpha = 0.4f) else themeColors.displayText.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isHighlight) Color(0xFFD97706) else themeColors.displayText.copy(alpha = 0.65f)
            )
            Text(
                text = time,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) Color(0xFFB45309) else themeColors.displayText
            )
        }
    }
}

/**
 * Strips AM/PM from time strings and converts digits to Bengali if requested.
 */
private fun stripAmPm(timeStr: String, isBn: Boolean): String {
    val cleaned = timeStr.replace("(?i)\\s*(am|pm|পূর্বাহ্ণ|অপরাহ্ন)".toRegex(), "").trim()
    return if (isBn) IslamicCalendarHelper.toBnDigits(cleaned) else cleaned
}

@Composable
private fun DigitBox(value: String, isBn: Boolean, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
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
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Arabic Text
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFD97706).copy(alpha = 0.07f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = arabic,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    lineHeight = 26.sp,
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

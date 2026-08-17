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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.util.CalendarUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data Model for each day of the Islamic/Hijri Month
 */
data class HijriDaySchedule(
    val hijriDay: Int,
    val hijriMonthIdx: Int,
    val hijriYear: Int,
    val hijriTitleBn: String,          // e.g. "১ সফর, রবিবার"
    val hijriTitleEn: String,          // e.g. "1 Safar, Sunday"
    val gregorianSubtitleBn: String,   // e.g. "১৭ আগস্ট ২০২৬"
    val gregorianSubtitleEn: String,   // e.g. "17 Aug 2026"
    val sehriTime: String,             // e.g. "০৪:১৪" (no AM/PM)
    val fajrTime: String,
    val sunriseTime: String,
    val dhuhrTime: String,
    val asrTime: String,
    val iftarTime: String,             // e.g. "০৬:৩২"
    val ishaTime: String,
    val isToday: Boolean,
    val isFriday: Boolean,
    val forbiddenFastingReason: String? = null // Non-null if prohibited day
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

    // Hijri Month Navigation Offset (0 = Current Islamic Month, +1 = Next Islamic Month, -1 = Previous)
    var hijriMonthOffset by remember { mutableIntStateOf(0) }

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
            viewModel.autoDetectIslamicLocation(context) { _, msg ->
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
    val (todayHDay, todayHMonth, todayHYear) = remember(currentTimeMillis) {
        CalendarUtils.getHijriDateComponents(todayCal)
    }
    val todayHijriDateStr = remember(currentTimeMillis, isBn) {
        IslamicCalendarHelper.getHijriDateString(todayCal, isBn)
    }

    // Check if today is a forbidden fasting day
    val todayForbiddenReason = remember(todayHMonth, todayHDay, isBn) {
        getForbiddenFastingInfo(todayHMonth, todayHDay, isBn)
    }

    // Timestamps for Countdown & Calculations
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

    // Total Day Length Calculation (Sunrise to Sunset)
    val totalDayMinutes = remember(timingsToday) {
        val srMin = NamazTimeService.timeStrToMinutes(timingsToday.sunrise)
        val ssMin = NamazTimeService.timeStrToMinutes(timingsToday.maghrib)
        (ssMin - srMin).coerceAtLeast(0)
    }
    val dayHours = totalDayMinutes / 60
    val dayMins = totalDayMinutes % 60
    val dayLengthStr = if (isBn) {
        "${IslamicCalendarHelper.toBnDigits(dayHours)} ঘণ্টা ${IslamicCalendarHelper.toBnDigits(dayMins)} মিনিট"
    } else {
        "${dayHours} hours ${dayMins} mins"
    }

    // Hijri Month Schedule Generation
    val hijriMonthsBn = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি",
        "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )
    val hijriMonthsEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    val (activeHijriHeaderTitle, hijriScheduleList) = remember(
        currentTimeMillis,
        hijriMonthOffset,
        viewModel.selectedIslamicDistrictLat,
        viewModel.selectedIslamicDistrictLon,
        isBn,
        viewModel.hijriSyncVersion
    ) {
        generateHijriMonthSchedule(
            anchorCal = todayCal,
            monthOffset = hijriMonthOffset,
            lat = viewModel.selectedIslamicDistrictLat,
            lon = viewModel.selectedIslamicDistrictLon,
            isBn = isBn,
            hijriMonthsBn = hijriMonthsBn,
            hijriMonthsEn = hijriMonthsEn
        )
    }

    // Refresh rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_transition")
    val refreshRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refresh_rotate"
    )

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
            // --- TOP BAR: Location Selection Prompt & District / GPS Selector ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = themeColors.titleBarBg,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isBn) "আপনার লোকেশন সিলেক্ট করুন" else "Select your location",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

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
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !viewModel.isDetectingIslamicLocation) {
                                if (IslamicLocationHelper.hasLocationPermission(context)) {
                                    viewModel.autoDetectIslamicLocation(context) { _, msg ->
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
                    val districtName = if (isBn) viewModel.selectedIslamicDistrictBn.split(" ")[0] else viewModel.selectedIslamicDistrictEn
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
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .widthIn(max = 135.dp)
                        ) {
                            Icon(
                                imageVector = if (viewModel.isIslamicLocationAutoDetected) Icons.Default.MyLocation else Icons.Default.LocationOn,
                                contentDescription = "District",
                                tint = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFFD97706),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = districtName,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.isIslamicLocationAutoDetected) Color(0xFF10B981) else Color(0xFFD97706),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
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

            // =========================================================================
            // --- 1. HERO CARD (থিমের সাথে মিল রেখে মিনিমাল ডিজাইন) ---
            // =========================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                border = BorderStroke(1.2.dp, themeColors.displayText.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    // Top row of Hero: Arabic/Hijri Date of Today + Real-Time Sync/Refresh Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Arabic/Hijri Date Tag
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFD97706).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.35f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🌙 ",
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = todayHijriDateStr,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }

                        // Real-Time Moon Sighting / Hijri Date Sync Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = themeColors.titleBarBg.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, themeColors.titleBarBg.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !viewModel.isSyncingHijriDate) {
                                    viewModel.syncHijriDateOnline(context) { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync Moon Sighting Hijri Date",
                                    tint = themeColors.titleBarBg,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .then(
                                            if (viewModel.isSyncingHijriDate) Modifier.rotate(refreshRotation) else Modifier
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (viewModel.isSyncingHijriDate) {
                                        if (isBn) "সিঙ্ক হচ্ছে..." else "Syncing..."
                                    } else {
                                        if (isBn) "চাঁদ দেখা সিঙ্ক" else "Sync Hijri"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.titleBarBg
                                )
                            }
                        }
                    }

                    // Middle: Sehri End Time & Iftar Time Side by Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sehri Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isBn) "সেহরির শেষ সময়" else "Sehri Ends",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.NightlightRound,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = stripAmPm(timingsToday.sahri, isBn),
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = themeColors.displayText
                                )
                                Text(
                                    text = if (isBn) "ফজর: ${stripAmPm(timingsToday.fajr, isBn)}" else "Fajr: ${stripAmPm(timingsToday.fajr, isBn)}",
                                    fontSize = 10.5.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Iftar Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFD97706).copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isBn) "ইফতারের সময়" else "Iftar Time",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.WbTwilight,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = stripAmPm(timingsToday.maghrib, isBn),
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = themeColors.displayText
                                )
                                Text(
                                    text = if (isBn) "মাগরিব: ${stripAmPm(timingsToday.maghrib, isBn)}" else "Maghrib: ${stripAmPm(timingsToday.maghrib, isBn)}",
                                    fontSize = 10.5.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // Prohibited Day Warning Banner (If today is a forbidden day)
                    if (todayForbiddenReason != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🚫 ", fontSize = 14.sp)
                                Text(
                                    text = todayForbiddenReason,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    // Bottom Hero Section: Iftar Countdown (ইফতারের কাউন্টডাউন)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = themeColors.displayText.copy(alpha = 0.04f),
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Total Fasting Duration Banner above countdown
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFD97706).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBn) "আজকের রোজার মোট সময়: $fastingDurationStr" else "Total Fasting Time: $fastingDurationStr",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isIftarPassed) {
                                        if (isBn) "আজকের ইফতারের সময় অতিবাহিত হয়েছে" else "Today's Iftar completed"
                                    } else {
                                        if (isBn) "ইফতার হতে বাকি সময়:" else "Iftar Countdown:"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText.copy(alpha = 0.85f)
                                )
                            }

                            val (h, m, s) = formatCountdownUnits(iftarCountdownMillis, isBn)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                MinimalDigitBox(h, isBn = isBn, unit = if (isBn) "ঘণ্টা" else "Hours", themeColors = themeColors)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(":", color = themeColors.displayText.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                MinimalDigitBox(m, isBn = isBn, unit = if (isBn) "মিনিট" else "Mins", themeColors = themeColors)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(":", color = themeColors.displayText.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                MinimalDigitBox(s, isBn = isBn, unit = if (isBn) "সেকেন্ড" else "Secs", themeColors = themeColors)
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
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD97706).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Sunrise",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
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
                                    fontSize = 15.sp,
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEA580C).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbTwilight,
                                    contentDescription = "Sunset",
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(18.dp)
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
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                            }
                        }
                    }

                    // Total Day Length Banner (দিনের মোট দৈর্ঘ্য)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = themeColors.titleBarBg.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, themeColors.titleBarBg.copy(alpha = 0.22f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = themeColors.titleBarBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "দিনের মোট দৈর্ঘ্য: " else "Total Day Length: ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                            Text(
                                text = dayLengthStr,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColors.titleBarBg
                            )
                        }
                    }
                }
            }

            // =============================================================================================
            // --- 3. FULL HIJRI MONTH SCHEDULE (আরবি মাস ভিত্তিক সম্পূর্ণ ৩০ দিনের কম্প্যাক্ট সময়সূচি) ---
            // =============================================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header of the schedule section with Hijri Month Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "সম্পূর্ণ আরবি মাসের সূচি" else "Islamic Month Schedule",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "তারিখে ট্যাপ করে ওয়াক্তের সময় দেখুন" else "Tap date to expand prayer times",
                            fontSize = 11.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }

                    // Hijri Month Navigator (< Month Name >)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { hijriMonthOffset-- },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Islamic Month",
                                tint = themeColors.displayText
                            )
                        }
                        Text(
                            text = activeHijriHeaderTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD97706)
                        )
                        IconButton(
                            onClick = { hijriMonthOffset++ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Islamic Month",
                                tint = themeColors.displayText
                            )
                        }
                    }
                }

                // Table Column Header (Compact: আরবি তারিখ ও বার, সেহরি, ইফতার)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.titleBarBg.copy(alpha = 0.1f)),
                    border = BorderStroke(0.8.dp, themeColors.titleBarBg.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "আরবি তারিখ ও বার" else "Hijri Date & Day",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.titleBarBg,
                            modifier = Modifier.weight(2.0f)
                        )
                        Text(
                            text = if (isBn) "সেহরি" else "Sehri",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.7f)
                        )
                        Text(
                            text = if (isBn) "ইফতার" else "Iftar",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.7f)
                        )
                        Text(
                            text = if (isBn) "ওয়াক্ত" else "Times",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.titleBarBg.copy(alpha = 0.7f),
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(32.dp)
                        )
                    }
                }

                // Month Rows List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hijriScheduleList.forEach { row ->
                        val isExpanded = expandedDayNumber == row.hijriDay
                        CompactExpandableHijriDayCard(
                            row = row,
                            isExpanded = isExpanded,
                            isBn = isBn,
                            themeColors = themeColors,
                            onToggle = {
                                expandedDayNumber = if (isExpanded) null else row.hijriDay
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
 * Compact Expandable Hijri Day Card
 * Collapsed: Hijri Date + Day Name (Primary), English Date (Subtitle), Sehri + Iftar times
 * Highlights forbidden days of fasting (e.g. Eid-ul-Fitr, Eid-ul-Adha, Days of Tashreeq)
 * Expanded: All Waqt Times (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha, Sehri) + Prohibition reason if applicable
 */
@Composable
private fun CompactExpandableHijriDayCard(
    row: HijriDaySchedule,
    isExpanded: Boolean,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onToggle: () -> Unit
) {
    val isForbidden = row.forbiddenFastingReason != null

    val cardBg = when {
        isForbidden -> Color(0xFFEF4444).copy(alpha = 0.06f)
        row.isToday -> themeColors.titleBarBg.copy(alpha = 0.1f)
        row.isFriday -> Color(0xFF0284C7).copy(alpha = 0.04f)
        else -> themeColors.cardBg
    }
    val cardBorder = when {
        isForbidden -> Color(0xFFEF4444).copy(alpha = 0.4f)
        row.isToday -> themeColors.titleBarBg
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
        border = BorderStroke(if (row.isToday || isExpanded || isForbidden) 1.2.dp else 0.8.dp, cardBorder)
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
                // Column 1: Hijri Date + Bar (Primary) and English Date (Subtitle)
                Column(modifier = Modifier.weight(2.0f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isBn) row.hijriTitleBn else row.hijriTitleEn,
                            fontSize = 12.5.sp,
                            fontWeight = if (row.isToday) FontWeight.ExtraBold else FontWeight.Bold,
                            color = when {
                                isForbidden -> Color(0xFFDC2626)
                                row.isToday -> themeColors.titleBarBg
                                else -> themeColors.displayText
                            }
                        )
                        if (row.isToday) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = themeColors.titleBarBg
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
                    }
                    Text(
                        text = if (isBn) row.gregorianSubtitleBn else row.gregorianSubtitleEn,
                        fontSize = 10.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.55f)
                    )
                    // Forbidden badge if applicable
                    if (isForbidden) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.12f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = if (isBn) "🚫 রোজা রাখা নিষিদ্ধ" else "🚫 Fasting Prohibited",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Column 2: Sehri Time (Compact, e.g. ০৪:১৪)
                Text(
                    text = row.sehriTime,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isForbidden) themeColors.displayText.copy(alpha = 0.5f) else Color(0xFF0284C7),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.7f)
                )

                // Column 3: Iftar Time (Compact, e.g. ০৬:৩২)
                Text(
                    text = row.iftarTime,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isForbidden) themeColors.displayText.copy(alpha = 0.5f) else Color(0xFFD97706),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.7f)
                )

                // Column 4: Expand Icon
                Box(
                    modifier = Modifier.width(32.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Prayer Times",
                        tint = themeColors.displayText.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expanded Section: All 5 Waqt Start and End Times for this Day
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColors.displayText.copy(alpha = 0.03f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // If Forbidden Day, show detailed Hadith explanation
                    if (isForbidden && row.forbiddenFastingReason != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️ ", fontSize = 14.sp)
                                Text(
                                    text = row.forbiddenFastingReason,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isBn) "৫ ওয়াক্তের নামাজের শুরুর ও শেষের সময়সূচি:" else "5 Daily Prayer Start & End Times:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )

                    // 5 Waqt Start & End Times Table
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = themeColors.displayText.copy(alpha = 0.04f),
                        border = BorderStroke(0.8.dp, themeColors.displayText.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(themeColors.titleBarBg.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "ওয়াক্ত" else "Waqt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.titleBarBg,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = if (isBn) "শুরুর সময়" else "Start Time",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.titleBarBg,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (isBn) "শেষ সময়" else "End Time",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.titleBarBg,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            val prayerWaqts = listOf(
                                Triple(if (isBn) "ফজর" else "Fajr", row.fajrTime, row.sunriseTime),
                                Triple(if (isBn) "যোহর" else "Dhuhr", row.dhuhrTime, row.asrTime),
                                Triple(if (isBn) "আসর" else "Asr", row.asrTime, row.iftarTime),
                                Triple(if (isBn) "মাগরিব (ইফতার)" else "Maghrib", row.iftarTime, row.ishaTime),
                                Triple(if (isBn) "এশা" else "Isha", row.ishaTime, row.fajrTime)
                            )

                            prayerWaqts.forEach { (name, startTime, endTime) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = themeColors.displayText,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    Text(
                                        text = startTime,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = endTime,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = themeColors.displayText.copy(alpha = 0.75f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
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
private fun WaqtMiniItem(
    label: String,
    time: String,
    color: Color,
    themeColors: CalculatorThemeColors
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.25f)),
        modifier = Modifier
            .width(102.dp)
            .padding(vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = time,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = themeColors.displayText
            )
        }
    }
}

/**
 * Dua Action Card with Audio Player, Copy & Share buttons
 */
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
    var isSpeaking by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title & Action Buttons (Audio, Copy, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD97706)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Audio / TTS
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                tts?.stop()
                                isSpeaking = false
                            } else {
                                isSpeaking = true
                                tts?.speak(arabic, TextToSpeech.QUEUE_FLUSH, null, "dua_tts")
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Play Audio",
                            tint = themeColors.titleBarBg,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Copy
                    IconButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Dua", "$title\n\n$arabic\n\n$transliteration\n\n$meaning")
                            cm.setPrimaryClip(clip)
                            val msg = if (isBn) "দোয়াটি কপি করা হয়েছে" else "Dua copied to clipboard"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Dua",
                            tint = themeColors.displayText.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "$title\n\n$arabic\n\n$transliteration\n\n$meaning")
                            }
                            context.startActivity(Intent.createChooser(intent, title))
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Dua",
                            tint = themeColors.displayText.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Arabic Text
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = themeColors.titleBarBg.copy(alpha = 0.06f),
                border = BorderStroke(0.8.dp, themeColors.titleBarBg.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = arabic,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.End,
                    color = themeColors.displayText,
                    lineHeight = 28.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Transliteration
            Text(
                text = transliteration,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = themeColors.displayText.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            // Meaning
            Text(
                text = meaning,
                fontSize = 12.sp,
                color = themeColors.displayText.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Strips AM / PM from time string and converts to Bangla digits if needed
 */
private fun stripAmPm(timeStr: String, isBn: Boolean): String {
    val cleaned = timeStr
        .replace("AM", "", ignoreCase = true)
        .replace("PM", "", ignoreCase = true)
        .replace("A.M.", "", ignoreCase = true)
        .replace("P.M.", "", ignoreCase = true)
        .trim()
    return if (isBn) IslamicCalendarHelper.toBnDigits(cleaned) else cleaned
}

@Composable
private fun MinimalDigitBox(
    value: String,
    isBn: Boolean,
    unit: String,
    themeColors: CalculatorThemeColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = themeColors.displayText.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)),
            modifier = Modifier.size(width = 44.dp, height = 34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColors.displayText
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = unit,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = themeColors.displayText.copy(alpha = 0.6f)
        )
    }
}

/**
 * Checks if a specific day is a forbidden fasting day in Islam
 */
private fun getForbiddenFastingInfo(hijriMonthIdx: Int, hijriDay: Int, isBn: Boolean): String? {
    // 0=Muharram, 1=Safar, ..., 8=Ramadan, 9=Shawwal, 10=Dhu al-Qi'dah, 11=Dhu al-Hijjah
    return when {
        hijriMonthIdx == 9 && hijriDay == 1 -> {
            if (isBn) "ঈদুল ফিতর: রোজা রাখা সম্পূর্ণ হারাম (সহিহ বুখারি: ১৯৯২)" else "Eid-ul-Fitr: Fasting strictly forbidden (Bukhari: 1992)"
        }
        hijriMonthIdx == 11 && hijriDay == 10 -> {
            if (isBn) "ঈদুল আজহা: কোরবানির দিন রোজা রাখা হারাম (সহিহ মুসলিম: ১১৩৮)" else "Eid-ul-Adha: Fasting strictly forbidden (Muslim: 1138)"
        }
        hijriMonthIdx == 11 && hijriDay == 11 -> {
            if (isBn) "১ম আইয়ামে তাশরিক: রোজা রাখা হারাম (সহিহ মুসলিম: ১১৪১)" else "1st Day of Tashreeq: Fasting forbidden (Muslim: 1141)"
        }
        hijriMonthIdx == 11 && hijriDay == 12 -> {
            if (isBn) "২য় আইয়ামে তাশরিক: রোজা রাখা হারাম (সহিহ মুসলিম: ১১৪১)" else "2nd Day of Tashreeq: Fasting forbidden (Muslim: 1141)"
        }
        hijriMonthIdx == 11 && hijriDay == 13 -> {
            if (isBn) "৩য় আইয়ামে তাশরিক: রোজা রাখা হারাম (সহিহ মুসলিম: ১১৪১)" else "3rd Day of Tashreeq: Fasting forbidden (Muslim: 1141)"
        }
        else -> null
    }
}

/**
 * Generates accurate schedule for 29 or 30 days of the requested Hijri month
 */
private fun generateHijriMonthSchedule(
    anchorCal: Calendar,
    monthOffset: Int,
    lat: Double,
    lon: Double,
    isBn: Boolean,
    hijriMonthsBn: List<String>,
    hijriMonthsEn: List<String>
): Pair<String, List<HijriDaySchedule>> {
    val banglaDayNames = listOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")
    val englishDayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val banglaMonths = listOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")
    val englishMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    // Clone calendar and find 1st day of the target Hijri month
    val searchCal = (anchorCal.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, monthOffset * 29)
    }
    var (targetHDay, targetHMonth, targetHYear) = CalendarUtils.getHijriDateComponents(searchCal)

    // Backtrack to day 1 of this target Hijri month
    searchCal.add(Calendar.DAY_OF_MONTH, -(targetHDay - 1))
    var comp = CalendarUtils.getHijriDateComponents(searchCal)

    // Fine tune day-by-day
    var safety = 0
    while (comp.first > 1 && safety < 5) {
        searchCal.add(Calendar.DAY_OF_MONTH, -1)
        comp = CalendarUtils.getHijriDateComponents(searchCal)
        safety++
    }
    safety = 0
    while (comp.first < 1 && safety < 5) {
        searchCal.add(Calendar.DAY_OF_MONTH, 1)
        comp = CalendarUtils.getHijriDateComponents(searchCal)
        safety++
    }

    targetHMonth = comp.second
    targetHYear = comp.third

    val headerTitle = if (isBn) {
        val mName = hijriMonthsBn.getOrElse(targetHMonth) { "মুহাররম" }
        "$mName ${IslamicCalendarHelper.toBnDigits(targetHYear.toString())} হিজরী"
    } else {
        val mName = hijriMonthsEn.getOrElse(targetHMonth) { "Muharram" }
        "$mName $targetHYear AH"
    }

    val daysList = mutableListOf<HijriDaySchedule>()
    val todayDayOfYear = anchorCal.get(Calendar.DAY_OF_YEAR)
    val todayYear = anchorCal.get(Calendar.YEAR)

    for (dayIndex in 0..30) {
        val currentDayCal = (searchCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, dayIndex)
        }
        val currentComp = CalendarUtils.getHijriDateComponents(currentDayCal)
        val hDay = currentComp.first
        val hMonth = currentComp.second
        val hYear = currentComp.third

        // If moved to next Hijri month, stop (29 or 30 days completed)
        if (hMonth != targetHMonth && dayIndex >= 28) {
            break
        }

        val timings = NamazTimeService.getPrayerTimesForCoordinates(lat, lon, currentDayCal)
        val dayOfWeek = (currentDayCal.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7
        val isFriday = (currentDayCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
        val isToday = (currentDayCal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear && currentDayCal.get(Calendar.YEAR) == todayYear)

        val gDay = currentDayCal.get(Calendar.DAY_OF_MONTH)
        val gMonth = currentDayCal.get(Calendar.MONTH)
        val gYear = currentDayCal.get(Calendar.YEAR)

        val hMonthName = if (isBn) hijriMonthsBn.getOrElse(targetHMonth) { "" } else hijriMonthsEn.getOrElse(targetHMonth) { "" }

        val hijriTitleBn = "${IslamicCalendarHelper.toBnDigits(hDay)} $hMonthName, ${banglaDayNames[dayOfWeek]}"
        val hijriTitleEn = "$hDay $hMonthName, ${englishDayNames[dayOfWeek]}"

        val gregorianSubtitleBn = "${IslamicCalendarHelper.toBnDigits(gDay)} ${banglaMonths[gMonth]} ${IslamicCalendarHelper.toBnDigits(gYear.toString())}"
        val gregorianSubtitleEn = "$gDay ${englishMonths[gMonth]} $gYear"

        val forbiddenReason = getForbiddenFastingInfo(hMonth, hDay, isBn)

        daysList.add(
            HijriDaySchedule(
                hijriDay = hDay,
                hijriMonthIdx = hMonth,
                hijriYear = hYear,
                hijriTitleBn = hijriTitleBn,
                hijriTitleEn = hijriTitleEn,
                gregorianSubtitleBn = gregorianSubtitleBn,
                gregorianSubtitleEn = gregorianSubtitleEn,
                sehriTime = stripAmPm(timings.sahri, isBn),
                fajrTime = stripAmPm(timings.fajr, isBn),
                sunriseTime = stripAmPm(timings.sunrise, isBn),
                dhuhrTime = stripAmPm(timings.dhuhr, isBn),
                asrTime = stripAmPm(timings.asr, isBn),
                iftarTime = stripAmPm(timings.maghrib, isBn),
                ishaTime = stripAmPm(timings.isha, isBn),
                isToday = isToday,
                isFriday = isFriday,
                forbiddenFastingReason = forbiddenReason
            )
        )
    }

    return Pair(headerTitle, daysList)
}

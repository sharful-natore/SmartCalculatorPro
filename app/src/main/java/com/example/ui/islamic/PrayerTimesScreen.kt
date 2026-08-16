package com.example.ui.islamic

import android.content.Context
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

data class PrayerWaqtItem(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val timeRangeStr: String,
    val startTimeStr: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isForbidden: Boolean = false,
    val isNafl: Boolean = false,
    val noteBn: String? = null,
    val noteEn: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernPrayerTimesCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val offset = viewModel.selectedIslamicDistrictOffsetMinutes

    var showDistrictSheet by remember { mutableStateOf(false) }
    var showFiqhInfoDialog by remember { mutableStateOf(false) }

    // Live Ticker for 1-second dynamic countdown updates
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Daily Prayer Tracking State (Preserved in SharedPreferences)
    val todayDateKey = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    }
    val sharedPrefs = remember {
        context.getSharedPreferences("prayer_tracker_prefs", Context.MODE_PRIVATE)
    }

    var fajrDone by remember { mutableStateOf(sharedPrefs.getBoolean("${todayDateKey}_fajr", false)) }
    var dhuhrDone by remember { mutableStateOf(sharedPrefs.getBoolean("${todayDateKey}_dhuhr", false)) }
    var asrDone by remember { mutableStateOf(sharedPrefs.getBoolean("${todayDateKey}_asr", false)) }
    var maghribDone by remember { mutableStateOf(sharedPrefs.getBoolean("${todayDateKey}_maghrib", false)) }
    var ishaDone by remember { mutableStateOf(sharedPrefs.getBoolean("${todayDateKey}_isha", false)) }

    fun togglePrayer(key: String, currentVal: Boolean, onUpdate: (Boolean) -> Unit) {
        val newVal = !currentVal
        onUpdate(newVal)
        sharedPrefs.edit().putBoolean("${todayDateKey}_$key", newVal).apply()
        val msg = if (isBn) {
            if (newVal) "মাশাআল্লাহ! নামাজ আদায় রেকর্ড করা হয়েছে।" else "নামাজ রেকর্ড বাতিল করা হয়েছে।"
        } else {
            if (newVal) "MashaAllah! Prayer marked as completed." else "Prayer marked uncompleted."
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    val completedCount = listOf(fajrDone, dhuhrDone, asrDone, maghribDone, ishaDone).count { it }

    // Notification sound alerts toggle state
    var alertsMap by remember {
        mutableStateOf(
            mapOf(
                "fajr" to sharedPrefs.getBoolean("alert_fajr", true),
                "dhuhr" to sharedPrefs.getBoolean("alert_dhuhr", true),
                "asr" to sharedPrefs.getBoolean("alert_asr", true),
                "maghrib" to sharedPrefs.getBoolean("alert_maghrib", true),
                "isha" to sharedPrefs.getBoolean("alert_isha", true)
            )
        )
    }

    // Base Timings configured for Dhaka (Offset applies for all 64 districts)
    val calTahajjudEnd = parseTimeToCal("04:30 AM", offset, 0)
    val calFajr = parseTimeToCal("04:52 AM", offset, 0)
    val calSunrise = parseTimeToCal("06:08 AM", offset, 0)
    val calIshraq = parseTimeToCal("06:23 AM", offset, 0)
    val calDuhaEnd = parseTimeToCal("11:55 AM", offset, 0)
    val calZawaalForbidden = parseTimeToCal("12:00 PM", offset, 0)
    val calDhuhr = parseTimeToCal("12:15 PM", offset, 0)
    val calAsr = parseTimeToCal("04:35 PM", offset, 0)
    val calSunsetForbidden = parseTimeToCal("06:03 PM", offset, 0)
    val calMaghrib = parseTimeToCal("06:18 PM", offset, 0)
    val calIsha = parseTimeToCal("07:35 PM", offset, 0)

    val calFajrTomorrow = parseTimeToCal("04:52 AM", offset, 1)

    val now = currentTimeMillis

    // Active Waqt State Calculation
    val (activeWaqtId, activeWaqtTitleBn, activeWaqtTitleEn, currentEndMillis, currentStartMillis, nextWaqtTitleBn, nextWaqtTitleEn, nextStartMillis, isCurrentForbidden) = when {
        now < calFajr.timeInMillis -> {
            ActiveWaqtData("isha", "এশার ওয়াক্ত (শেষ রাত/তাহাজ্জুদ)", "Isha / Tahajjud Waqt", calFajr.timeInMillis, calIsha.timeInMillis - 86400000L, "ফজর", "Fajr", calFajr.timeInMillis, false)
        }
        now < calSunrise.timeInMillis -> {
            ActiveWaqtData("fajr", "ফজরের ওয়াক্ত", "Fajr Waqt", calSunrise.timeInMillis, calFajr.timeInMillis, "⚠️ সূর্যোদয় (নামাজ নিষেধ)", "⚠️ Sunrise (Forbidden)", calSunrise.timeInMillis, false)
        }
        now < calIshraq.timeInMillis -> {
            ActiveWaqtData("sunrise_forbidden", "⚠️ সূর্যোদয়কাল (নামাজ নিষেধ)", "⚠️ Sunrise (Forbidden Time)", calIshraq.timeInMillis, calSunrise.timeInMillis, "ইশরাক / চাশত", "Ishraq / Duha", calIshraq.timeInMillis, true)
        }
        now < calZawaalForbidden.timeInMillis -> {
            ActiveWaqtData("ishraq_duha", "ইশরাক ও চাশতের নফল ওয়াক্ত", "Ishraq & Duha Time", calZawaalForbidden.timeInMillis, calIshraq.timeInMillis, "⚠️ ঠিক দুপুর / জাওয়াল (নামাজ নিষেধ)", "⚠️ Midday / Zawaal", calZawaalForbidden.timeInMillis, false)
        }
        now < calDhuhr.timeInMillis -> {
            ActiveWaqtData("midday_forbidden", "⚠️ ঠিক দুপুর / জাওয়াল (নামাজ নিষেধ)", "⚠️ Midday / Zawaal (Forbidden)", calDhuhr.timeInMillis, calZawaalForbidden.timeInMillis, "যোহর", "Dhuhr", calDhuhr.timeInMillis, true)
        }
        now < calAsr.timeInMillis -> {
            ActiveWaqtData("dhuhr", "যোহরের ওয়াক্ত", "Dhuhr Waqt", calAsr.timeInMillis, calDhuhr.timeInMillis, "আসর", "Asr", calAsr.timeInMillis, false)
        }
        now < calSunsetForbidden.timeInMillis -> {
            ActiveWaqtData("asr", "আসরের ওয়াক্ত", "Asr Waqt", calSunsetForbidden.timeInMillis, calAsr.timeInMillis, "⚠️ সূর্যাস্তকাল (নামাজ নিষেধ)", "⚠️ Sunset (Forbidden)", calSunsetForbidden.timeInMillis, false)
        }
        now < calMaghrib.timeInMillis -> {
            ActiveWaqtData("sunset_forbidden", "⚠️ সূর্যাস্তকাল (নামাজ নিষেধ)", "⚠️ Sunset (Forbidden Time)", calMaghrib.timeInMillis, calSunsetForbidden.timeInMillis, "মাগরিব", "Maghrib", calMaghrib.timeInMillis, true)
        }
        now < calIsha.timeInMillis -> {
            ActiveWaqtData("maghrib", "মাগরিবের ওয়াক্ত", "Maghrib Waqt", calIsha.timeInMillis, calMaghrib.timeInMillis, "এশা", "Isha", calIsha.timeInMillis, false)
        }
        else -> {
            ActiveWaqtData("isha", "এশার ওয়াক্ত", "Isha Waqt", calFajrTomorrow.timeInMillis, calIsha.timeInMillis, "ফজর", "Fajr", calFajrTomorrow.timeInMillis, false)
        }
    }

    val currentRemainingMillis = maxOf(0L, currentEndMillis - now)
    val nextCountdownMillis = maxOf(0L, nextStartMillis - now)

    // Progress through active waqt
    val totalWaqtDuration = maxOf(1L, currentEndMillis - currentStartMillis).toFloat()
    val elapsedWaqtDuration = (now - currentStartMillis).toFloat()
    val waqtProgress = (elapsedWaqtDuration / totalWaqtDuration).coerceIn(0f, 1f)

    // Dynamic Futuristic Gradients according to Waqt
    val heroGradient = when (activeWaqtId) {
        "fajr" -> listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0D9488))
        "sunrise_forbidden", "midday_forbidden", "sunset_forbidden" -> listOf(Color(0xFF7F1D1D), Color(0xFF991B1B), Color(0xFFDC2626))
        "ishraq_duha" -> listOf(Color(0xFF78350F), Color(0xFFB45309), Color(0xFFD97706))
        "dhuhr" -> listOf(Color(0xFF0C4A6E), Color(0xFF0284C7), Color(0xFF38BDF8))
        "asr" -> listOf(Color(0xFF7C2D12), Color(0xFFEA580C), Color(0xFFF97316))
        "maghrib" -> listOf(Color(0xFF4C1D95), Color(0xFF701A75), Color(0xFFBE185D))
        else -> listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81))
    }

    val prayerList = listOf(
        PrayerWaqtItem("fajr", "ফজর", "Fajr", "${adjustIslamicTimeStr("04:52 AM", offset)} - ${adjustIslamicTimeStr("06:08 AM", offset)}", "04:52 AM", Icons.Default.WbTwilight, noteBn = "আউয়াল ওয়াক্তে পড়া উত্তম"),
        PrayerWaqtItem("sunrise", "সূর্যোদয় (নিষিদ্ধ)", "Sunrise (Forbidden)", "${adjustIslamicTimeStr("06:08 AM", offset)} - ${adjustIslamicTimeStr("06:23 AM", offset)}", "06:08 AM", Icons.Default.WbSunny, isForbidden = true, noteBn = "সূর্য ওঠার সময় ১৫ মিনিট সকল নামাজ হারাম"),
        PrayerWaqtItem("ishraq", "ইশরাক ও চাশত", "Ishraq & Duha", "${adjustIslamicTimeStr("06:23 AM", offset)} - ${adjustIslamicTimeStr("11:55 AM", offset)}", "06:23 AM", Icons.Default.Brightness5, isNafl = true, noteBn = "অসীম সওয়াবের নফল সালাত"),
        PrayerWaqtItem("zawaal", "ঠিক দুপুর / জাওয়াল", "Midday / Zawaal", "${adjustIslamicTimeStr("12:00 PM", offset)} - ${adjustIslamicTimeStr("12:15 PM", offset)}", "12:00 PM", Icons.Default.BrightnessMedium, isForbidden = true, noteBn = "ঠিক মাথার উপর সূর্য থাকা অবস্থায় নামাজ নিষেধ"),
        PrayerWaqtItem("dhuhr", "যোহর", "Dhuhr", "${adjustIslamicTimeStr("12:15 PM", offset)} - ${adjustIslamicTimeStr("04:35 PM", offset)}", "12:15 PM", Icons.Default.LightMode, noteBn = "৪ রাকাত সুন্নত, ৪ রাকাত ফরজ, ২ রাকাত সুন্নত"),
        PrayerWaqtItem("asr", "আসর", "Asr", "${adjustIslamicTimeStr("04:35 PM", offset)} - ${adjustIslamicTimeStr("06:03 PM", offset)}", "04:35 PM", Icons.Default.WbSunny, noteBn = "৪ রাকাত ফরজ (সালাতুল উস্তা)"),
        PrayerWaqtItem("sunset", "সূর্যাস্তকাল (নিষিদ্ধ)", "Sunset (Forbidden)", "${adjustIslamicTimeStr("06:03 PM", offset)} - ${adjustIslamicTimeStr("06:18 PM", offset)}", "06:03 PM", Icons.Default.WbTwilight, isForbidden = true, noteBn = "সূর্যাস্তের সময় নামাজ নিষেধ (ঐ দিনের আসর ছাড়া)"),
        PrayerWaqtItem("maghrib", "মাগরিব", "Maghrib", "${adjustIslamicTimeStr("06:18 PM", offset)} - ${adjustIslamicTimeStr("07:35 PM", offset)}", "06:18 PM", Icons.Default.DarkMode, noteBn = "৩ রাকাত ফরজ, ২ রাকাত সুন্নত, নফল আওয়াবিন"),
        PrayerWaqtItem("isha", "এশা ও তাহাজ্জুদ", "Isha & Tahajjud", "${adjustIslamicTimeStr("07:35 PM", offset)} - ${adjustIslamicTimeStr("04:30 AM", offset)}", "07:35 PM", Icons.Default.NightsStay, noteBn = "৪ রাকাত ফরজ, ২ সুন্নত, ৩ বিতর, তাহাজ্জুদ")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar: Title & District Selector Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBn) "নামাজের সময়সূচি" else "Prayer Times",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showFiqhInfoDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Fiqh Info",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = if (isBn) "লাইভ ওয়াক্ত ও ৫ ওয়াক্ত ট্র্যাকার" else "Live Waqt & Prayer Tracker",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.65f)
                )
            }

            // District Switcher Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0284C7).copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
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
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) viewModel.selectedIslamicDistrictBn.split(" ")[0] else viewModel.selectedIslamicDistrictEn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // --- 1. HERO FUTURISTIC COCKPIT CARD ---
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
                    // Cockpit Header Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrentForbidden) Color(0xFFEF4444) else Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCurrentForbidden) (if (isBn) "⚠️ নিষিদ্ধ সময় সক্রিয়" else "Forbidden Time Active") else (if (isBn) "বর্তমান ওয়াক্ত" else "Active Waqt"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = if (isBn) "${viewModel.selectedIslamicDistrictBn.split(" ")[0]} জোন" else "${viewModel.selectedIslamicDistrictEn} Zone",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Waqt Title & Circular Gauge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) activeWaqtTitleBn else activeWaqtTitleEn,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBn) "শেষ হতে বাকি সময়:" else "Time remaining:",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            val (h, m, s) = formatCountdownUnits(currentRemainingMillis, isBn)
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                DigitalDigitBox(h, isBn = isBn, unit = if (isBn) "ঘণ্টা" else "hr")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(":", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                DigitalDigitBox(m, isBn = isBn, unit = if (isBn) "মিনিট" else "min")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(":", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                DigitalDigitBox(s, isBn = isBn, unit = if (isBn) "সেকেন্ড" else "sec")
                            }
                        }

                        // Circular Progress Indicator
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
                                    color = if (isCurrentForbidden) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                    startAngle = -90f,
                                    sweepAngle = waqtProgress * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(waqtProgress * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isBn) "অতিবাহিত" else "Elapsed",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Next Waqt Live Strip
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "পরবর্তী: $nextWaqtTitleBn" else "Next: $nextWaqtTitleEn",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = formatIslamicCountdownFull(nextCountdownMillis, isBn),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. DAILY PRAYER TRACKER (দৈনিক ৫ ওয়াক্ত সালাত ট্র্যাকার) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "আজকের নামাজ ট্র্যাকার" else "Today's Salah Tracker",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "৫ ওয়াক্ত নামাজ আদায়ের রেকর্ড রাখুন" else "Track your 5 daily prayers",
                            fontSize = 11.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }

                    // Progress Score Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (completedCount == 5) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (completedCount == 5) Icons.Default.CheckCircle else Icons.Default.Flag,
                                contentDescription = null,
                                tint = if (completedCount == 5) Color(0xFF10B981) else Color(0xFF0284C7),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "$completedCount/৫ ওয়াক্ত" else "$completedCount/5 Done",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (completedCount == 5) Color(0xFF10B981) else Color(0xFF0284C7)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5 Waqt Checkbox Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrayerTrackerPill(
                        name = if (isBn) "ফজর" else "Fajr",
                        isDone = fajrDone,
                        modifier = Modifier.weight(1f),
                        themeColors = themeColors,
                        onClick = { togglePrayer("fajr", fajrDone) { fajrDone = it } }
                    )
                    PrayerTrackerPill(
                        name = if (isBn) "যোহর" else "Dhuhr",
                        isDone = dhuhrDone,
                        modifier = Modifier.weight(1f),
                        themeColors = themeColors,
                        onClick = { togglePrayer("dhuhr", dhuhrDone) { dhuhrDone = it } }
                    )
                    PrayerTrackerPill(
                        name = if (isBn) "আসর" else "Asr",
                        isDone = asrDone,
                        modifier = Modifier.weight(1f),
                        themeColors = themeColors,
                        onClick = { togglePrayer("asr", asrDone) { asrDone = it } }
                    )
                    PrayerTrackerPill(
                        name = if (isBn) "মাগরিব" else "Maghrib",
                        isDone = maghribDone,
                        modifier = Modifier.weight(1f),
                        themeColors = themeColors,
                        onClick = { togglePrayer("maghrib", maghribDone) { maghribDone = it } }
                    )
                    PrayerTrackerPill(
                        name = if (isBn) "এশা" else "Isha",
                        isDone = ishaDone,
                        modifier = Modifier.weight(1f),
                        themeColors = themeColors,
                        onClick = { togglePrayer("isha", ishaDone) { ishaDone = it } }
                    )
                }
            }
        }

        // --- 3. TIMETABLE SCHEDULE LIST ---
        Text(
            text = if (isBn) "দৈনিক সময়সূচি ও নিষিদ্ধ সময়" else "Daily Waqt & Forbidden Schedule",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            prayerList.forEach { item ->
                val isItemActive = when (item.id) {
                    "fajr" -> activeWaqtId == "fajr"
                    "sunrise" -> activeWaqtId == "sunrise_forbidden"
                    "ishraq" -> activeWaqtId == "ishraq_duha"
                    "zawaal" -> activeWaqtId == "midday_forbidden"
                    "dhuhr" -> activeWaqtId == "dhuhr"
                    "asr" -> activeWaqtId == "asr"
                    "sunset" -> activeWaqtId == "sunset_forbidden"
                    "maghrib" -> activeWaqtId == "maghrib"
                    "isha" -> activeWaqtId == "isha"
                    else -> false
                }

                val hasAlert = alertsMap[item.id] ?: false

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isItemActive && item.isForbidden -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            isItemActive -> Color(0xFF0284C7).copy(alpha = 0.14f)
                            item.isForbidden -> Color(0xFFDC2626).copy(alpha = 0.06f)
                            item.isNafl -> Color(0xFF10B981).copy(alpha = 0.06f)
                            else -> themeColors.cardBg
                        }
                    ),
                    border = BorderStroke(
                        width = if (isItemActive) 1.5.dp else 1.dp,
                        color = when {
                            isItemActive && item.isForbidden -> Color(0xFFEF4444)
                            isItemActive -> Color(0xFF0284C7)
                            item.isForbidden -> Color(0xFFEF4444).copy(alpha = 0.3f)
                            else -> themeColors.displayText.copy(alpha = 0.08f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            item.isForbidden -> Color(0xFFEF4444).copy(alpha = 0.18f)
                                            item.isNafl -> Color(0xFF10B981).copy(alpha = 0.18f)
                                            isItemActive -> Color(0xFF0284C7).copy(alpha = 0.2f)
                                            else -> themeColors.displayText.copy(alpha = 0.07f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = when {
                                        item.isForbidden -> Color(0xFFDC2626)
                                        item.isNafl -> Color(0xFF10B981)
                                        isItemActive -> Color(0xFF0284C7)
                                        else -> themeColors.displayText.copy(alpha = 0.65f)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isBn) item.nameBn else item.nameEn,
                                        fontSize = 14.5.sp,
                                        fontWeight = if (isItemActive || item.isForbidden) FontWeight.Bold else FontWeight.SemiBold,
                                        color = when {
                                            item.isForbidden -> Color(0xFFDC2626)
                                            isItemActive -> Color(0xFF0284C7)
                                            else -> themeColors.displayText
                                        }
                                    )
                                    if (isItemActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (item.isForbidden) Color(0xFFEF4444) else Color(0xFF0284C7)
                                        ) {
                                            Text(
                                                text = if (isBn) "সক্রিয়" else "Active",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (item.noteBn != null) {
                                    Text(
                                        text = if (isBn) item.noteBn else (item.noteEn ?: ""),
                                        fontSize = 11.sp,
                                        color = if (item.isForbidden) Color(0xFFDC2626).copy(alpha = 0.8f) else themeColors.displayText.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = item.timeRangeStr,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    item.isForbidden -> Color(0xFFDC2626)
                                    isItemActive -> Color(0xFF0284C7)
                                    else -> themeColors.displayText
                                }
                            )

                            if (!item.isForbidden && !item.isNafl) {
                                IconButton(
                                    onClick = {
                                        val newAlerts = alertsMap.toMutableMap()
                                        newAlerts[item.id] = !hasAlert
                                        alertsMap = newAlerts
                                        sharedPrefs.edit().putBoolean("alert_${item.id}", !hasAlert).apply()
                                        Toast.makeText(
                                            context,
                                            if (!hasAlert) (if (isBn) "ওয়াক্ত অ্যালার্ট অন করা হলো" else "Waqt alert enabled") else (if (isBn) "ওয়াক্ত অ্যালার্ট বন্ধ করা হলো" else "Waqt alert disabled"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.size(24.dp).padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (hasAlert) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                        contentDescription = "Alert",
                                        tint = if (hasAlert) Color(0xFF0284C7) else themeColors.displayText.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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

    if (showFiqhInfoDialog) {
        AlertDialog(
            onDismissRequest = { showFiqhInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF0284C7))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "নামাজের গুরুত্বপূর্ণ মাসআলা" else "Salah & Fiqh Guidelines",
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
                        text = if (isBn) "১. নামাজের ৩টি নিষিদ্ধ সময়:" else "1. Three Forbidden Prayer Times:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    Text(
                        text = if (isBn) "• সূর্যোদয়ের সময় (সূর্য ওঠার পর ১৫ মিনিট পর্যন্ত)।\n• ঠিক দুপুরবেলা (সূর্য ঠিক মাথার উপর থেকে পশ্চিমাকাশে ঢলে পড়ার পূর্ব পর্যন্ত প্রায় ১৫ মিনিট)।\n• সূর্যাস্তের সময় (সূর্য হলুদ বর্ণ ধারণ করা থেকে ডোবা পর্যন্ত ১৫ মিনিট, তবে ঐ দিনের আসর পড়া না থাকলে তা আদায় করা যাবে)।"
                        else "• During sunrise (approx 15 mins after rising).\n• At exact zenith/midday (until sun moves past the meridian).\n• During sunset (approx 15 mins before sunset, except same day's Asr if missed)."
                    )
                    Text(
                        text = if (isBn) "২. আউয়াল ওয়াক্তের ফজিলত:" else "2. Virtues of Praying on Time:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                    Text(
                        text = if (isBn) "রাসূলুল্লাহ (সা.) ইরশাদ করেছেন: 'আল্লাহর নিকট সর্বাধিক প্রিয় আমল হলো সময়মতো নামাজ আদায় করা।' (সহীহ বুখারী)"
                        else "Prophet Muhammad (PBUH) said: 'The dearest deed to Allah is performing prayer at its earliest appointed time.' (Sahih Bukhari)"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFiqhInfoDialog = false }) {
                    Text(text = if (isBn) "ঠিক আছে" else "Close", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun DigitalDigitBox(value: String, isBn: Boolean, unit: String) {
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
private fun PrayerTrackerPill(
    name: String,
    isDone: Boolean,
    modifier: Modifier = Modifier,
    themeColors: CalculatorThemeColors,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDone) Color(0xFF10B981).copy(alpha = 0.18f) else themeColors.background,
        border = BorderStroke(1.dp, if (isDone) Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.12f)),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isDone) Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 11.5.sp,
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium,
                color = if (isDone) Color(0xFF10B981) else themeColors.displayText
            )
        }
    }
}

private data class ActiveWaqtData(
    val activeId: String,
    val activeTitleBn: String,
    val activeTitleEn: String,
    val currentEndMillis: Long,
    val currentStartMillis: Long,
    val nextTitleBn: String,
    val nextTitleEn: String,
    val nextStartMillis: Long,
    val isForbidden: Boolean
)

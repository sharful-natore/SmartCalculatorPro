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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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
    val startTimeStr: String,
    val endTimeStr: String,
    val timeRangeStr: String,
    val icon: ImageVector,
    val isForbidden: Boolean = false,
    val isNafl: Boolean = false,
    val infoTitleBn: String? = null,
    val infoTitleEn: String? = null,
    val infoDetailBn: String? = null,
    val infoDetailEn: String? = null
)

// Helper object for Triple Calendar calculation (Gregorian, Bangla, Hijri)
object IslamicCalendarHelper {

    private val banglaDigits = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )

    fun toBnDigits(input: String): String {
        return input.map { banglaDigits[it] ?: it }.joinToString("")
    }

    fun toBnDigits(number: Int): String {
        return toBnDigits(String.format(Locale.ENGLISH, "%02d", number))
    }

    private val banglaDays = listOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")
    private val englishDays = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    private val banglaMonths = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    private val englishGregorianMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val banglaGregorianMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    private val hijriMonthsBn = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি",
        "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    private val hijriMonthsEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    fun getGregorianDateString(cal: Calendar, isBn: Boolean): String {
        val dayOfWeekIdx = (cal.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val monthIdx = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)

        return if (isBn) {
            val dayName = banglaDays[dayOfWeekIdx]
            val monthName = banglaGregorianMonths[monthIdx]
            "$dayName, ${toBnDigits(dayOfMonth)} $monthName ${toBnDigits(year.toString())}"
        } else {
            val dayName = englishDays[dayOfWeekIdx]
            val monthName = englishGregorianMonths[monthIdx]
            "$dayName, $dayOfMonth $monthName $year"
        }
    }

    /**
     * Standard Bangla Academy Calendar Converter
     */
    fun getBanglaDateString(cal: Calendar, isBn: Boolean): String {
        val gYear = cal.get(Calendar.YEAR)
        val isLeapYear = (gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0)

        val monthLengths = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, if (isLeapYear) 30 else 29, 30)
        val startDates = intArrayOf(14, 15, 15, 16, 16, 16, 17, 16, 16, 15, 14, 15)
        val gregorianMonthOrder = intArrayOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 0, 1, 2)

        val gMonth = cal.get(Calendar.MONTH)
        val gDay = cal.get(Calendar.DAY_OF_MONTH)

        var bMonthIdx = 0
        var bDay = 1
        val bYear = if (gMonth > 3 || (gMonth == 3 && gDay >= 14)) gYear - 593 else gYear - 594

        for (i in gregorianMonthOrder.indices) {
            val gm = gregorianMonthOrder[i]
            val startDay = startDates[i]

            if (gMonth == gm) {
                if (gDay >= startDay) {
                    bMonthIdx = i
                    bDay = gDay - startDay + 1
                } else {
                    val prevI = if (i == 0) 11 else i - 1
                    bMonthIdx = prevI
                    val prevStart = startDates[prevI]
                    val prevMonthLen = monthLengths[prevI]
                    bDay = prevMonthLen - (prevStart - gDay) + 1
                    if (bDay > prevMonthLen) bDay = prevMonthLen
                }
                break
            }
        }

        return if (isBn) {
            "${toBnDigits(bDay)} ${banglaMonths[bMonthIdx]} ${toBnDigits(bYear.toString())} বঙ্গাব্দ"
        } else {
            "$bDay ${banglaMonths[bMonthIdx]} $bYear BE"
        }
    }

    /**
     * Standard Astronomical Hijri Converter
     */
    fun getHijriDateString(cal: Calendar, isBn: Boolean): String {
        val (hDay, hMonth, hYear) = com.example.util.CalendarUtils.getHijriDateComponents(cal)
        return if (isBn) {
            val monthName = hijriMonthsBn.getOrElse(hMonth) { "মুহাররম" }
            "${toBnDigits(hDay)} $monthName ${toBnDigits(hYear.toString())} হিজরি"
        } else {
            val monthName = hijriMonthsEn.getOrElse(hMonth) { "Muharram" }
            "$hDay $monthName $hYear AH"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernPrayerTimesCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var showDistrictSheet by remember { mutableStateOf(false) }
    var showMonthlySheet by remember { mutableStateOf(false) }
    var showFiqhInfoDialog by remember { mutableStateOf(false) }
    var selectedForbiddenItem by remember { mutableStateOf<PrayerWaqtItem?>(null) }

    // Live Ticker for 1-second dynamic countdown updates
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Selected Date Navigation State (Allows checking past / future prayer times)
    var selectedCalendarOffsetDays by remember { mutableIntStateOf(0) }
    val displayCalendar = remember(currentTimeMillis, selectedCalendarOffsetDays) {
        Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            add(Calendar.DAY_OF_YEAR, selectedCalendarOffsetDays)
        }
    }

    val isViewingToday = (selectedCalendarOffsetDays == 0)

    // Daily Prayer Tracking State (Preserved in SharedPreferences for today's record)
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

    // High precision calculation via Adhan engine for selected calendar
    val timings = remember(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, displayCalendar.get(Calendar.DAY_OF_YEAR), displayCalendar.get(Calendar.YEAR)) {
        NamazTimeService.getPrayerTimesForCoordinates(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, displayCalendar)
    }

    val ishraqTimeStr = remember(timings) {
        NamazTimeService.minutesToTimeStr(NamazTimeService.timeStrToMinutes(timings.sunrise) + 15)
    }
    val zawaalTimeStr = remember(timings) {
        NamazTimeService.minutesToTimeStr(NamazTimeService.timeStrToMinutes(timings.dhuhr) - 15)
    }
    val sunsetForbiddenTimeStr = remember(timings) {
        NamazTimeService.minutesToTimeStr(NamazTimeService.timeStrToMinutes(timings.maghrib) - 15)
    }
    val dhuhrMinutes = remember(timings) { NamazTimeService.timeStrToMinutes(timings.dhuhr) }
    val duhaEndTimeStr = remember(dhuhrMinutes) { NamazTimeService.minutesToTimeStr(dhuhrMinutes - 20) }

    // Parse today's live milestones for "এখন" & "পরবর্তী" Cockpit
    val liveTodayTimings = remember(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, currentTimeMillis) {
        val todayCal = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        NamazTimeService.getPrayerTimesForCoordinates(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, todayCal)
    }

    // Trigger prayer notification checks
    LaunchedEffect(currentTimeMillis) {
        com.example.util.PrayerNotificationHelper.checkAndTriggerNotifications(
            context = context,
            timings = liveTodayTimings,
            alertsMap = alertsMap,
            isBn = isBn
        )
    }

    val liveIshraqStr = remember(liveTodayTimings) {
        NamazTimeService.minutesToTimeStr(NamazTimeService.timeStrToMinutes(liveTodayTimings.sunrise) + 15)
    }
    val liveZawaalStr = remember(liveTodayTimings) {
        NamazTimeService.minutesToTimeStr(NamazTimeService.timeStrToMinutes(liveTodayTimings.dhuhr) - 15)
    }
    val liveSunsetForbiddenStr = remember(liveTodayTimings) {
        NamazTimeService.minutesToTimeStr(NamazTimeService.timeStrToMinutes(liveTodayTimings.maghrib) - 15)
    }

    val calFajr = parseTimeToCal(liveTodayTimings.fajr, 0, 0)
    val calSunrise = parseTimeToCal(liveTodayTimings.sunrise, 0, 0)
    val calIshraq = parseTimeToCal(liveIshraqStr, 0, 0)
    val calZawaal = parseTimeToCal(liveZawaalStr, 0, 0)
    val calDhuhr = parseTimeToCal(liveTodayTimings.dhuhr, 0, 0)
    val calAsr = parseTimeToCal(liveTodayTimings.asr, 0, 0)
    val calSunsetForbidden = parseTimeToCal(liveSunsetForbiddenStr, 0, 0)
    val calMaghrib = parseTimeToCal(liveTodayTimings.maghrib, 0, 0)
    val calIsha = parseTimeToCal(liveTodayTimings.isha, 0, 0)
    val calFajrTomorrow = parseTimeToCal(liveTodayTimings.fajr, 0, 1)
    val calSunriseTomorrow = parseTimeToCal(liveTodayTimings.sunrise, 0, 1)

    val now = currentTimeMillis

    // Determine Currently Active Waqt & Next Waqt
    val currentWaqtData = when {
        now < calFajr.timeInMillis -> {
            ActiveWaqtData(
                activeId = "isha",
                activeTitleBn = "তাহাজ্জুদ / এশা",
                activeTitleEn = "Tahajjud / Isha",
                activeTimeStr = "${liveTodayTimings.isha} - ${liveTodayTimings.fajr}",
                currentStartMillis = calIsha.timeInMillis - 86400000L,
                currentEndMillis = calFajr.timeInMillis,
                nextTitleBn = "ফজর",
                nextTitleEn = "Fajr",
                nextTimeStr = "${liveTodayTimings.fajr} - ${liveTodayTimings.sunrise}",
                nextStartMillis = calFajr.timeInMillis,
                nextEndMillis = calSunrise.timeInMillis,
                nextDurationStrBn = formatWaqtDuration(calFajr.timeInMillis, calSunrise.timeInMillis, true),
                nextDurationStrEn = formatWaqtDuration(calFajr.timeInMillis, calSunrise.timeInMillis, false),
                isForbidden = false
            )
        }
        now < calSunrise.timeInMillis -> {
            ActiveWaqtData(
                activeId = "fajr",
                activeTitleBn = "ফজর",
                activeTitleEn = "Fajr",
                activeTimeStr = "${liveTodayTimings.fajr} - ${liveTodayTimings.sunrise}",
                currentStartMillis = calFajr.timeInMillis,
                currentEndMillis = calSunrise.timeInMillis,
                nextTitleBn = "যোহর (পরবর্তী ফরজ)",
                nextTitleEn = "Dhuhr (Next Fard)",
                nextTimeStr = "${liveTodayTimings.dhuhr} - ${liveTodayTimings.asr}",
                nextStartMillis = calDhuhr.timeInMillis,
                nextEndMillis = calAsr.timeInMillis,
                nextDurationStrBn = formatWaqtDuration(calDhuhr.timeInMillis, calAsr.timeInMillis, true),
                nextDurationStrEn = formatWaqtDuration(calDhuhr.timeInMillis, calAsr.timeInMillis, false),
                isForbidden = false
            )
        }
        now < calIshraq.timeInMillis -> {
            ActiveWaqtData(
                activeId = "sunrise_forbidden",
                activeTitleBn = "নিষিদ্ধ সময় (সূর্যোদয়)",
                activeTitleEn = "Forbidden (Sunrise)",
                activeTimeStr = "${liveTodayTimings.sunrise} - $liveIshraqStr",
                currentStartMillis = calSunrise.timeInMillis,
                currentEndMillis = calIshraq.timeInMillis,
                nextTitleBn = "ইশরাক ও চাশত",
                nextTitleEn = "Ishraq & Duha",
                nextTimeStr = "$liveIshraqStr - $liveZawaalStr",
                nextStartMillis = calIshraq.timeInMillis,
                nextEndMillis = calZawaal.timeInMillis,
                nextDurationStrBn = formatWaqtDuration(calIshraq.timeInMillis, calZawaal.timeInMillis, true),
                nextDurationStrEn = formatWaqtDuration(calIshraq.timeInMillis, calZawaal.timeInMillis, false),
                isForbidden = true
            )
        }
        now < calZawaal.timeInMillis -> {
            ActiveWaqtData(
                activeId = "ishraq_duha",
                activeTitleBn = "ইশরাক ও চাশত",
                activeTitleEn = "Ishraq & Duha",
                activeTimeStr = "$liveIshraqStr - $liveZawaalStr",
                currentStartMillis = calIshraq.timeInMillis,
                currentEndMillis = calZawaal.timeInMillis,
                nextTitleBn = "যোহর",
                nextTitleEn = "Dhuhr",
                nextTimeStr = "${liveTodayTimings.dhuhr} - ${liveTodayTimings.asr}",
                nextStartMillis = calDhuhr.timeInMillis,
                nextEndMillis = calAsr.timeInMillis,
                nextDurationStrBn = formatWaqtDuration(calDhuhr.timeInMillis, calAsr.timeInMillis, true),
                nextDurationStrEn = formatWaqtDuration(calDhuhr.timeInMillis, calAsr.timeInMillis, false),
                isForbidden = false
            )
        }
        now < calDhuhr.timeInMillis -> {
            ActiveWaqtData(
                activeId = "midday_forbidden",
                activeTitleBn = "নিষিদ্ধ সময় (দ্বিপ্রহর)",
                activeTitleEn = "Forbidden (Midday)",
                activeTimeStr = "$liveZawaalStr - ${liveTodayTimings.dhuhr}",
                currentStartMillis = calZawaal.timeInMillis,
                currentEndMillis = calDhuhr.timeInMillis,
                nextTitleBn = "যোহর",
                nextTitleEn = "Dhuhr",
                nextTimeStr = "${liveTodayTimings.dhuhr} - ${liveTodayTimings.asr}",
                nextStartMillis = calDhuhr.timeInMillis,
                nextEndMillis = calAsr.timeInMillis,
                nextDurationStrBn = formatWaqtDuration(calDhuhr.timeInMillis, calAsr.timeInMillis, true),
                nextDurationStrEn = formatWaqtDuration(calDhuhr.timeInMillis, calAsr.timeInMillis, false),
                isForbidden = true
            )
        }
        now < calAsr.timeInMillis -> {
            ActiveWaqtData(
                activeId = "dhuhr",
                activeTitleBn = "যোহর",
                activeTitleEn = "Dhuhr",
                activeTimeStr = "${liveTodayTimings.dhuhr} - ${liveTodayTimings.asr}",
                currentStartMillis = calDhuhr.timeInMillis,
                currentEndMillis = calAsr.timeInMillis,
                nextTitleBn = "আসর",
                nextTitleEn = "Asr",
                nextTimeStr = "${liveTodayTimings.asr} - $liveSunsetForbiddenStr",
                nextStartMillis = calAsr.timeInMillis,
                nextEndMillis = calSunsetForbidden.timeInMillis,
                nextDurationStrBn = formatWaqtDuration(calAsr.timeInMillis, calSunsetForbidden.timeInMillis, true),
                nextDurationStrEn = formatWaqtDuration(calAsr.timeInMillis, calSunsetForbidden.timeInMillis, false),
                isForbidden = false
            )
        }
        now < calSunsetForbidden.timeInMillis -> {
            ActiveWaqtData(
                activeId = "asr",
                activeTitleBn = "আসর",
                activeTitleEn = "Asr",
                activeTimeStr = "${liveTodayTimings.asr} - $liveSunsetForbiddenStr",
                currentStartMillis = calAsr.timeInMillis,
                currentEndMillis = calSunsetForbidden.timeInMillis,
                nextTitleBn = "মাগরিব",
                nextTitleEn = "Maghrib",
                nextTimeStr = "${liveTodayTimings.maghrib} - ${liveTodayTimings.isha}",
                nextStartMillis = calMaghrib.timeInMillis,
                nextEndMillis = calIsha.timeInMillis,
                nextDurationStrBn = formatWaqtDuration(calMaghrib.timeInMillis, calIsha.timeInMillis, true),
                nextDurationStrEn = formatWaqtDuration(calMaghrib.timeInMillis, calIsha.timeInMillis, false),
                isForbidden = false
            )
        }
        now < calMaghrib.timeInMillis -> {
            ActiveWaqtData(
                activeId = "sunset_forbidden",
                activeTitleBn = "নিষিদ্ধ সময় (সূর্যাস্ত)",
                activeTitleEn = "Forbidden (Sunset)",
                activeTimeStr = "$liveSunsetForbiddenStr - ${liveTodayTimings.maghrib}",
                currentStartMillis = calSunsetForbidden.timeInMillis,
                currentEndMillis = calMaghrib.timeInMillis,
                nextTitleBn = "মাগরিব",
                nextTitleEn = "Maghrib",
                nextTimeStr = "${liveTodayTimings.maghrib} - ${liveTodayTimings.isha}",
                nextStartMillis = calMaghrib.timeInMillis,
                nextEndMillis = calIsha.timeInMillis,
                nextDurationStrBn = formatWaqtDuration(calMaghrib.timeInMillis, calIsha.timeInMillis, true),
                nextDurationStrEn = formatWaqtDuration(calMaghrib.timeInMillis, calIsha.timeInMillis, false),
                isForbidden = true
            )
        }
        now < calIsha.timeInMillis -> {
            ActiveWaqtData(
                activeId = "maghrib",
                activeTitleBn = "মাগরিব",
                activeTitleEn = "Maghrib",
                activeTimeStr = "${liveTodayTimings.maghrib} - ${liveTodayTimings.isha}",
                currentStartMillis = calMaghrib.timeInMillis,
                currentEndMillis = calIsha.timeInMillis,
                nextTitleBn = "এশা",
                nextTitleEn = "Isha",
                nextTimeStr = "${liveTodayTimings.isha} - ${liveTodayTimings.fajr}",
                nextStartMillis = calIsha.timeInMillis,
                nextEndMillis = calFajrTomorrow.timeInMillis,
                nextDurationStrBn = formatWaqtDuration(calIsha.timeInMillis, calFajrTomorrow.timeInMillis, true),
                nextDurationStrEn = formatWaqtDuration(calIsha.timeInMillis, calFajrTomorrow.timeInMillis, false),
                isForbidden = false
            )
        }
        else -> {
            ActiveWaqtData(
                activeId = "isha",
                activeTitleBn = "এশা",
                activeTitleEn = "Isha",
                activeTimeStr = "${liveTodayTimings.isha} - ${liveTodayTimings.fajr}",
                currentStartMillis = calIsha.timeInMillis,
                currentEndMillis = calFajrTomorrow.timeInMillis,
                nextTitleBn = "ফজর",
                nextTitleEn = "Fajr",
                nextTimeStr = "${liveTodayTimings.fajr} - ${liveTodayTimings.sunrise}",
                nextStartMillis = calFajrTomorrow.timeInMillis,
                nextEndMillis = calSunriseTomorrow.timeInMillis,
                nextDurationStrBn = formatWaqtDuration(calFajrTomorrow.timeInMillis, calSunriseTomorrow.timeInMillis, true),
                nextDurationStrEn = formatWaqtDuration(calFajrTomorrow.timeInMillis, calSunriseTomorrow.timeInMillis, false),
                isForbidden = false
            )
        }
    }

    val currentRemainingMillis = maxOf(0L, currentWaqtData.currentEndMillis - now)
    val nextCountdownMillis = maxOf(0L, currentWaqtData.nextStartMillis - now)

    // Calculate day length between sunrise and sunset (maghrib)
    val dayLengthMinutes = remember(timings) {
        val sr = NamazTimeService.timeStrToMinutes(timings.sunrise)
        val ss = NamazTimeService.timeStrToMinutes(timings.maghrib)
        (ss - sr).coerceAtLeast(0)
    }
    val dayLengthHours = dayLengthMinutes / 60
    val dayLengthMins = dayLengthMinutes % 60
    val dayLengthStr = if (isBn) {
        "${IslamicCalendarHelper.toBnDigits(dayLengthHours)} ঘণ্টা ${IslamicCalendarHelper.toBnDigits(dayLengthMins)} মিনিট"
    } else {
        "${dayLengthHours}h ${dayLengthMins}m"
    }

    // Complete Prayer & Forbidden Timetable List (100% Calibrated)
    val prayerList = remember(timings, ishraqTimeStr, zawaalTimeStr, sunsetForbiddenTimeStr, duhaEndTimeStr) {
        listOf(
            PrayerWaqtItem(
                id = "fajr",
                nameBn = "ফজর",
                nameEn = "Fajr",
                startTimeStr = timings.fajr,
                endTimeStr = timings.sunrise,
                timeRangeStr = "${timings.fajr} - ${timings.sunrise}",
                icon = Icons.Default.WbTwilight
            ),
            PrayerWaqtItem(
                id = "sunrise_forbidden",
                nameBn = "নিষিদ্ধ সময়",
                nameEn = "Forbidden Time",
                startTimeStr = timings.sunrise,
                endTimeStr = ishraqTimeStr,
                timeRangeStr = "${timings.sunrise} - ${ishraqTimeStr}",
                icon = Icons.Default.WbSunny,
                isForbidden = true,
                infoTitleBn = "সূর্যোদয়কালীন নিষিদ্ধ সময়",
                infoTitleEn = "Sunrise Forbidden Period",
                infoDetailBn = "সূর্য ওঠার সময় থেকে শুরু করে প্রায় ১৫-২০ মিনিট (ইশরাকের ওয়াক্ত শুরু হওয়া পর্যন্ত) সব ধরণের নামাজ (ফরজ, নফল বা কাজা) আদায় করা সম্পূর্ণ হারাম ও নিষিদ্ধ।\n\n• দলীল: রাসূলুল্লাহ (সা.) সূর্য উদয়ের সময় নামাজ পড়তে নিষেধ করেছেন (সহীহ মুসলিম: ৮৩১)।",
                infoDetailEn = "All prayers (Fard, Nafl, Qada) are strictly prohibited during the 15-20 minutes after sunrise until Ishraq starts (Sahih Muslim: 831)."
            ),
            PrayerWaqtItem(
                id = "ishraq",
                nameBn = "ইশরাক ও চাশত",
                nameEn = "Ishraq & Duha",
                startTimeStr = ishraqTimeStr,
                endTimeStr = duhaEndTimeStr,
                timeRangeStr = "${ishraqTimeStr} - ${duhaEndTimeStr}",
                icon = Icons.Default.Brightness5,
                isNafl = true,
                infoTitleBn = "ইশরাক ও চাশতের সালাত (নফল)",
                infoTitleEn = "Ishraq & Duha Prayer",
                infoDetailBn = "সূর্যোদয়ের ১৫-২০ মিনিট পর থেকে ঠিক দুপুর (জাওয়াল)-এর পূর্ব পর্যন্ত ইশরাক ও চাশতের সালাত আদায় করার সময়।\n\n• ফজিলত: রাসূলুল্লাহ (সা.) বলেছেন, যে ব্যক্তি ফজরের পর বসে জিকির করে সূর্য ওঠার পর ২ রাকাত ইশরাক পড়বে, সে একটি পূর্ণ হজ ও ওমরার সওয়াব পাবে (তিরমিজি: ৫৮৬)।",
                infoDetailEn = "Voluntary morning prayer with tremendous rewards (Tirmidhi: 586)."
            ),
            PrayerWaqtItem(
                id = "zawaal_forbidden",
                nameBn = "নিষিদ্ধ সময়",
                nameEn = "Forbidden Time",
                startTimeStr = zawaalTimeStr,
                endTimeStr = timings.dhuhr,
                timeRangeStr = "${zawaalTimeStr} - ${timings.dhuhr}",
                icon = Icons.Default.LightMode,
                isForbidden = true,
                infoTitleBn = "দ্বিপ্রহরের নিষিদ্ধ সময় (জাওয়াল)",
                infoTitleEn = "Midday (Zawaal) Forbidden Time",
                infoDetailBn = "ঠিক দুপুরবেলা সূর্য যখন ঠিক মাথার ওপর থাকে (যোহরের ওয়াক্ত শুরু হওয়ার পূর্ববর্তী ১০-১৫ মিনিট), তখন যেকোনো প্রকার সালাত আদায় নিষিদ্ধ। সূর্য পশ্চিমাকাশে ঢলে পড়ার পর যোহরের ওয়াক্ত শুরু হয়।\n\n• দলীল: সহীহ মুসলিম: ৮৩১।",
                infoDetailEn = "Midday zenith when the sun is at the highest meridian. Prayers are strictly prohibited until the sun passes meridian (Sahih Muslim: 831)."
            ),
            PrayerWaqtItem(
                id = "dhuhr",
                nameBn = "যোহর",
                nameEn = "Dhuhr",
                startTimeStr = timings.dhuhr,
                endTimeStr = timings.asr,
                timeRangeStr = "${timings.dhuhr} - ${timings.asr}",
                icon = Icons.Default.LightMode
            ),
            PrayerWaqtItem(
                id = "asr",
                nameBn = "আসর",
                nameEn = "Asr",
                startTimeStr = timings.asr,
                endTimeStr = sunsetForbiddenTimeStr,
                timeRangeStr = "${timings.asr} - ${sunsetForbiddenTimeStr}",
                icon = Icons.Default.Brightness6
            ),
            PrayerWaqtItem(
                id = "sunset_forbidden",
                nameBn = "নিষিদ্ধ সময়",
                nameEn = "Forbidden Time",
                startTimeStr = sunsetForbiddenTimeStr,
                endTimeStr = timings.maghrib,
                timeRangeStr = "${sunsetForbiddenTimeStr} - ${timings.maghrib}",
                icon = Icons.Default.WbTwilight,
                isForbidden = true,
                infoTitleBn = "সূর্যাস্তকালীন নিষিদ্ধ সময়",
                infoTitleEn = "Sunset Forbidden Period",
                infoDetailBn = "সূর্য হলুদ বর্ণ ধারণ করা থেকে ডোবা পর্যন্ত (মাগরিবের ওয়াক্ত শুরুর পূর্ববর্তী প্রায় ১৫ মিনিট) সালাত আদায় নিষিদ্ধ।\n\n• বিশেষ নিয়ম: কোনো কারণে ঐ দিনের আসরের নামাজ আদায় করতে দেরি হয়ে থাকলে তা সূর্যাস্তের আগ মুহূর্তে হলেও দ্রুত আদায় করে নিতে হবে।",
                infoDetailEn = "Prayers prohibited during the 15 minutes before sunset, except same day's Asr if missed (Sahih Bukhari: 579)."
            ),
            PrayerWaqtItem(
                id = "maghrib",
                nameBn = "মাগরিব",
                nameEn = "Maghrib",
                startTimeStr = timings.maghrib,
                endTimeStr = timings.isha,
                timeRangeStr = "${timings.maghrib} - ${timings.isha}",
                icon = Icons.Default.DarkMode
            ),
            PrayerWaqtItem(
                id = "isha",
                nameBn = "এশা",
                nameEn = "Isha",
                startTimeStr = timings.isha,
                endTimeStr = timings.sahri,
                timeRangeStr = "${timings.isha} - ${timings.sahri}",
                icon = Icons.Default.NightsStay
            )
        )
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
            // --- TOP HEADER BAR: District Title & Actions (Auto-Location, District Selector, Monthly, Refresh Sync) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "জেলা" else "District",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColors.displayText
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // District Switcher Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = themeColors.titleBarBg.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, themeColors.titleBarBg.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .widthIn(max = 140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showDistrictSheet = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "District",
                                tint = themeColors.titleBarBg,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isBn) viewModel.selectedIslamicDistrictBn.split(" ")[0] else viewModel.selectedIslamicDistrictEn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.titleBarBg,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = themeColors.titleBarBg,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Auto Location Detect Button (Right of District Selector)
                    Surface(
                        shape = CircleShape,
                        color = themeColors.titleBarBg.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, themeColors.titleBarBg.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !viewModel.isDetectingIslamicLocation) {
                                viewModel.autoDetectIslamicLocation(context) { _, msg ->
                                    if (msg != null) {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (viewModel.isDetectingIslamicLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(15.dp),
                                    strokeWidth = 2.dp,
                                    color = themeColors.titleBarBg
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Auto Location",
                                    tint = themeColors.titleBarBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Monthly Calendar Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showMonthlySheet = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Monthly Schedule",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isBn) "মাসিক" else "Monthly",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }

                    // Global Hijri Date Moon Sync / Refresh Button
                    Surface(
                        shape = CircleShape,
                        color = themeColors.titleBarBg.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, themeColors.titleBarBg.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !viewModel.isSyncingHijriDate) {
                                viewModel.syncHijriDateOnline(context) { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (viewModel.isSyncingHijriDate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(15.dp),
                                    strokeWidth = 2.dp,
                                    color = themeColors.titleBarBg
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync Hijri Date",
                                    tint = themeColors.titleBarBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- 1. DUAL HERO STATUS CARDS (এখন & পরবর্তী - ENHANCED VISIBILITY & DISTINCT TIMINGS) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // LEFT CARD: এখন (Current Waqt - HIGH VISIBILITY)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentWaqtData.isForbidden) {
                            if (themeColors.isDark) Color(0xFF3B1010) else Color(0xFFFEF2F2)
                        } else {
                            if (themeColors.isDark) themeColors.buttonEqualBg.copy(alpha = 0.22f) else themeColors.titleBarBg.copy(alpha = 0.12f)
                        }
                    ),
                    border = BorderStroke(
                        2.dp,
                        if (currentWaqtData.isForbidden) Color(0xFFEF4444) else themeColors.titleBarBg
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (currentWaqtData.isForbidden) Color(0xFFDC2626).copy(alpha = 0.15f) else themeColors.titleBarBg.copy(alpha = 0.18f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (currentWaqtData.isForbidden) Color(0xFFDC2626) else Color(0xFF10B981))
                                        )
                                        Text(
                                            text = if (isBn) "বর্তমান ওয়াক্ত" else "Active Waqt",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (currentWaqtData.isForbidden) Color(0xFFDC2626) else themeColors.titleBarBg
                                        )
                                    }
                                }

                                if (currentWaqtData.isForbidden) {
                                    Text(
                                        text = "⚠️",
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Text(
                                text = if (isBn) currentWaqtData.activeTitleBn else currentWaqtData.activeTitleEn,
                                fontSize = 18.5.sp,
                                fontWeight = FontWeight.Black,
                                color = if (currentWaqtData.isForbidden) Color(0xFF991B1B) else themeColors.displayText,
                                maxLines = 2
                            )

                            Text(
                                text = currentWaqtData.activeTimeStr,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText.copy(alpha = 0.85f)
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Countdown remaining to current waqt end
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentWaqtData.isForbidden) Color(0xFFDC2626) else themeColors.titleBarBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val timeFormatted = formatTimerClock(currentRemainingMillis)
                                val finalDisplayTime = if (isBn) convertDigitsToBn(timeFormatted) else timeFormatted
                                val finalLabel = if (isBn) "শেষ হতে বাকি $finalDisplayTime" else "Ends in $finalDisplayTime"
                                Text(
                                    text = "⏳ $finalLabel",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                                )
                            }

                            // Elapsed progress bar with remaining percentage
                            val currentProgress = remember(currentWaqtData, currentTimeMillis) {
                                val total = (currentWaqtData.currentEndMillis - currentWaqtData.currentStartMillis).coerceAtLeast(1L)
                                val elapsed = (currentTimeMillis - currentWaqtData.currentStartMillis).coerceAtLeast(0L)
                                (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                            }
                            val currentRemainingPercent = remember(currentWaqtData, currentTimeMillis) {
                                val total = (currentWaqtData.currentEndMillis - currentWaqtData.currentStartMillis).coerceAtLeast(1L)
                                val remaining = (currentWaqtData.currentEndMillis - currentTimeMillis).coerceAtLeast(0L)
                                ((remaining.toFloat() / total.toFloat()) * 100).toInt().coerceIn(0, 100)
                            }
                            val remainingPercentStr = if (isBn) convertDigitsToBn(currentRemainingPercent.toString()) + "%" else "$currentRemainingPercent%"

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = remainingPercentStr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentWaqtData.isForbidden) Color(0xFFB91C1C) else themeColors.titleBarBg
                                )
                                LinearProgressIndicator(
                                    progress = { currentProgress },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (currentWaqtData.isForbidden) Color(0xFFDC2626) else themeColors.titleBarBg,
                                    trackColor = if (currentWaqtData.isForbidden) Color(0xFFFCA5A5).copy(alpha = 0.4f) else themeColors.titleBarBg.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }

                // RIGHT CARD: পরবর্তী (Next Waqt - DISTINCT INFORMATION & TIMINGS)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (themeColors.isDark) Color(0xFF0D2538) else Color(0xFFF0F9FF)
                    ),
                    border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0284C7).copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = if (isBn) "পরবর্তী ওয়াক্ত" else "Next Waqt",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0369A1)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (isBn) currentWaqtData.nextTitleBn else currentWaqtData.nextTitleEn,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (themeColors.isDark) Color(0xFF38BDF8) else Color(0xFF0369A1),
                                maxLines = 2
                            )

                            Text(
                                text = currentWaqtData.nextTimeStr,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText.copy(alpha = 0.8f)
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // If start of next is different from end of current (e.g. during Fajr next Fard is Dhuhr, or during forbidden periods)
                            val isDifferentFromCurrentEnd = (currentWaqtData.nextStartMillis != currentWaqtData.currentEndMillis)
                            
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.14f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isDifferentFromCurrentEnd && nextCountdownMillis > 0L) {
                                    val timeFormatted = formatTimerClock(nextCountdownMillis)
                                    val finalDisplayTime = if (isBn) convertDigitsToBn(timeFormatted) else timeFormatted
                                    val finalLabel = if (isBn) "শুরু হতে বাকি $finalDisplayTime" else "Starts in $finalDisplayTime"
                                    Text(
                                        text = "⏱️ $finalLabel",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0284C7),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                                    )
                                } else {
                                    val durationLabel = if (isBn) "ওয়াক্ত স্থায়ী: ${currentWaqtData.nextDurationStrBn}" else "Duration: ${currentWaqtData.nextDurationStrEn}"
                                    Text(
                                        text = "⏱️ $durationLabel",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0284C7),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isBn) "মোট স্থায়িত্ব:" else "Total Length:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (isBn) currentWaqtData.nextDurationStrBn else currentWaqtData.nextDurationStrEn,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7)
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. SUNRISE & SUNSET CARD (সূর্যোদয়, সূর্যাস্ত ও দিনের দৈর্ঘ্য) ---
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
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "সূর্যোদয়" else "Sunrise",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText.copy(alpha = 0.65f)
                                )
                                Text(
                                    text = timings.sunrise,
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
                                .height(34.dp)
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
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "সূর্যাস্ত" else "Sunset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText.copy(alpha = 0.65f)
                                )
                                Text(
                                    text = timings.maghrib,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day Length Badge Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = themeColors.displayText.copy(alpha = 0.04f),
                        border = BorderStroke(0.7.dp, themeColors.displayText.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = themeColors.titleBarBg,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "দিনের মোট দৈর্ঘ্য: $dayLengthStr" else "Total Day Length: $dayLengthStr",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.displayText.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // --- 3. DATE NAVIGATION BAR (TRIPLE CALENDAR SELECTOR) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Day Button <
                    IconButton(
                        onClick = { selectedCalendarOffsetDays-- },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Day",
                            tint = themeColors.titleBarBg,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Center Date Content (Gregorian, Bangla, Hijri)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedCalendarOffsetDays = 0 }
                    ) {
                        Text(
                            text = IslamicCalendarHelper.getGregorianDateString(displayCalendar, isBn),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${IslamicCalendarHelper.getBanglaDateString(displayCalendar, isBn)}, ${IslamicCalendarHelper.getHijriDateString(displayCalendar, isBn)}",
                            fontSize = 11.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                        if (!isViewingToday) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = themeColors.titleBarBg.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (isBn) "আজকের দিনে ফিরুন ↺" else "Back to Today ↺",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.titleBarBg,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Next Day Button >
                    IconButton(
                        onClick = { selectedCalendarOffsetDays++ },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Day",
                            tint = themeColors.titleBarBg,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // --- 4. PRAYER SCHEDULE TIMETABLE (LIST / TABLE) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    prayerList.forEachIndexed { index, item ->
                        val isItemActive = isViewingToday && when (item.id) {
                            "fajr" -> currentWaqtData.activeId == "fajr"
                            "sunrise" -> false
                            "sunrise_forbidden" -> currentWaqtData.activeId == "sunrise_forbidden"
                            "ishraq" -> currentWaqtData.activeId == "ishraq_duha"
                            "zawaal_forbidden" -> currentWaqtData.activeId == "midday_forbidden"
                            "dhuhr" -> currentWaqtData.activeId == "dhuhr"
                            "asr" -> currentWaqtData.activeId == "asr"
                            "sunset_forbidden" -> currentWaqtData.activeId == "sunset_forbidden"
                            "maghrib" -> currentWaqtData.activeId == "maghrib"
                            "isha" -> currentWaqtData.activeId == "isha"
                            else -> false
                        }

                        val hasAlert = alertsMap[item.id] ?: false

                        // Active row background highlight (accent for active, amber for forbidden)
                        val rowBg = when {
                            isItemActive && item.isForbidden -> Color(0xFFFEE2E2)
                            isItemActive -> themeColors.titleBarBg
                            item.isForbidden -> Color(0xFFFFF1F1)
                            else -> Color.Transparent
                        }

                        val textColor = when {
                            isItemActive && item.isForbidden -> Color(0xFFB91C1C)
                            isItemActive -> Color.White
                            item.isForbidden -> Color(0xFFC53030)
                            else -> themeColors.displayText
                        }

                        val iconColor = when {
                            isItemActive && item.isForbidden -> Color(0xFFB91C1C)
                            isItemActive -> Color.White
                            item.isForbidden -> Color(0xFFC53030)
                            else -> themeColors.titleBarBg
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .padding(horizontal = 8.dp, vertical = 9.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left: Time of Day Icon + Waqt Name (single line, no subtitle)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1.05f)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBn) item.nameBn else item.nameEn,
                                        fontSize = 14.sp,
                                        fontWeight = if (isItemActive || item.isForbidden) FontWeight.Bold else FontWeight.SemiBold,
                                        color = textColor,
                                        maxLines = 1
                                    )
                                }

                                // Center / Time Range: e.g. "04:16 AM - 05:34 AM"
                                Text(
                                    text = item.timeRangeStr,
                                    fontSize = 13.sp,
                                    fontWeight = if (isItemActive || item.isForbidden) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = textColor,
                                    maxLines = 1,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                // Far Right: Info button for forbidden/nafl/sunrise times, Notification alarm for regular wakts
                                Box(
                                    modifier = Modifier.size(28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.isForbidden || item.isNafl || item.id == "sunrise") {
                                        IconButton(
                                            onClick = { selectedForbiddenItem = item },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Details",
                                                tint = if (isItemActive) Color.White else (if (item.isForbidden) Color(0xFFD97706) else themeColors.titleBarBg),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else {
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
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (hasAlert) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                                contentDescription = "Alert",
                                                tint = if (isItemActive) Color.White else (if (hasAlert) themeColors.titleBarBg else themeColors.displayText.copy(alpha = 0.35f)),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (index < prayerList.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.6.dp,
                                color = themeColors.displayText.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }

            // --- 4. DAILY SALAH TRACKER (দৈনিক ৫ ওয়াক্ত সালাত ট্র্যাকার) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
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
                            color = if (completedCount == 5) Color(0xFF10B981).copy(alpha = 0.2f) else themeColors.titleBarBg.copy(alpha = 0.12f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = if (completedCount == 5) Icons.Default.CheckCircle else Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = if (completedCount == 5) Color(0xFF10B981) else themeColors.titleBarBg,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBn) "$completedCount/৫ ওয়াক্ত" else "$completedCount/5 Done",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (completedCount == 5) Color(0xFF10B981) else themeColors.titleBarBg
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

    if (showMonthlySheet) {
        MonthlyPrayerTimesSheet(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { showMonthlySheet = false }
        )
    }

    if (showFiqhInfoDialog) {
        AlertDialog(
            onDismissRequest = { showFiqhInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = themeColors.titleBarBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "নামাজের গুরুত্বপূর্ণ মাসআলা" else "Salah & Fiqh Guidelines",
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
                        text = if (isBn) "১. নামাজের ৩টি নিষিদ্ধ সময়:" else "1. Three Forbidden Prayer Times:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    Text(
                        text = if (isBn) "• সূর্যোদয়ের সময় (সূর্য ওঠার পর ১৫ মিনিট পর্যন্ত)।\n• ঠিক দুপুরবেলা (সূর্য ঠিক মাথার উপর থেকে পশ্চিমাকাশে ঢলে পড়ার পূর্ব পর্যন্ত প্রায় ১৫ মিনিট)।\n• সূর্যাস্তের সময় (সূর্য হলুদ বর্ণ ধারণ করা থেকে ডোবা পর্যন্ত ১৫ মিনিট, তবে ঐ দিনের আসর পড়া না থাকলে তা আদায় করা যাবে)।"
                        else "• During sunrise (approx 15 mins after rising).\n• At exact zenith/midday (until sun moves past the meridian).\n• During sunset (approx 15 mins before sunset, except same day's Asr if missed).",
                        color = themeColors.displayText,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = if (isBn) "২. আউয়াল ওয়াক্তের ফজিলত:" else "2. Virtues of Praying on Time:",
                        fontWeight = FontWeight.Bold,
                        color = themeColors.titleBarBg
                    )
                    Text(
                        text = if (isBn) "রাসূলুল্লাহ (সা.) ইরশাদ করেছেন: 'আল্লাহর নিকট সর্বাধিক প্রিয় আমল হলো সময়মতো নামাজ আদায় করা।' (সহীহ বুখারী)"
                        else "Prophet Muhammad (PBUH) said: 'The dearest deed to Allah is performing prayer at its earliest appointed time.' (Sahih Bukhari)",
                        color = themeColors.displayText.copy(alpha = 0.9f),
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFiqhInfoDialog = false }) {
                    Text(text = if (isBn) "ঠিক আছে" else "Close", color = themeColors.titleBarBg, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = themeColors.cardBg,
            titleContentColor = themeColors.displayText,
            textContentColor = themeColors.displayText,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (selectedForbiddenItem != null) {
        val item = selectedForbiddenItem!!
        AlertDialog(
            onDismissRequest = { selectedForbiddenItem = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (item.isForbidden) Color(0xFFFEF2F2) else themeColors.titleBarBg.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (item.isForbidden) Color(0xFFDC2626) else themeColors.titleBarBg,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = (if (isBn) item.infoTitleBn ?: item.nameBn else item.infoTitleEn ?: item.nameEn),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = themeColors.displayText,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (item.isForbidden) Color(0xFFFEF3C7) else themeColors.titleBarBg.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = item.timeRangeStr,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isForbidden) Color(0xFFB45309) else themeColors.titleBarBg,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = (if (isBn) item.infoDetailBn ?: "" else item.infoDetailEn ?: ""),
                        fontSize = 13.5.sp,
                        color = themeColors.displayText,
                        textAlign = TextAlign.Start,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedForbiddenItem = null }) {
                    Text(
                        text = if (isBn) "বুঝতে পেরেছি" else "Understood",
                        color = themeColors.titleBarBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = themeColors.cardBg,
            titleContentColor = themeColors.displayText,
            textContentColor = themeColors.displayText,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyPrayerTimesSheet(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var monthOffset by remember { mutableIntStateOf(0) }
    val currentCal = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthName = remember(currentCal, isBn) {
        val mIdx = currentCal.get(Calendar.MONTH)
        val y = currentCal.get(Calendar.YEAR)
        if (isBn) {
            val banglaGregorianMonths = listOf(
                "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
                "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
            )
            "${banglaGregorianMonths[mIdx]} ${IslamicCalendarHelper.toBnDigits(y.toString())}"
        } else {
            val englishGregorianMonths = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            "${englishGregorianMonths[mIdx]} $y"
        }
    }

    val todayCalendar = Calendar.getInstance()
    val isCurrentMonth = todayCalendar.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH) &&
            todayCalendar.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR)
    val todayDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH)

    val banglaDayShort = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
    val englishDayShort = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    val monthData = remember(currentCal, viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon) {
        (1..daysInMonth).map { day ->
            val dayCal = (currentCal.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, day)
            }
            val dayOfWeek = (dayCal.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7
            val timings = NamazTimeService.getPrayerTimesForCoordinates(viewModel.selectedIslamicDistrictLat, viewModel.selectedIslamicDistrictLon, dayCal)
            val isToday = isCurrentMonth && (day == todayDayOfMonth)
            val isFriday = (dayCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)

            MonthlyDayRowData(
                day = day,
                dayOfWeekName = if (isBn) banglaDayShort[dayOfWeek] else englishDayShort[dayOfWeek],
                fajr = timings.fajr,
                sunrise = timings.sunrise,
                dhuhr = timings.dhuhr,
                asr = timings.asr,
                maghrib = timings.maghrib,
                isha = timings.isha,
                isToday = isToday,
                isFriday = isFriday
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = themeColors.cardBg,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 8.dp),
                color = themeColors.displayText.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(modifier = Modifier.size(width = 38.dp, height = 4.dp))
            }
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "মাসিক নামাজের সময়সূচি" else "Monthly Prayer Timetable",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "${viewModel.selectedIslamicDistrictBn.split(" ")[0]} জেলা" else "${viewModel.selectedIslamicDistrictEn} District",
                        fontSize = 12.sp,
                        color = themeColors.titleBarBg,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Month Navigator Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = themeColors.background,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { monthOffset-- }) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = themeColors.titleBarBg
                        )
                    }

                    Text(
                        text = monthName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )

                    IconButton(onClick = { monthOffset++ }) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = themeColors.titleBarBg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table with Horizontal Scroll
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 420.dp)
            ) {
                Column(modifier = Modifier.horizontalScroll(scrollState)) {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .background(themeColors.titleBarBg.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MonthlyTableCell(text = if (isBn) "তারিখ" else "Date", width = 50.dp, isHeader = true, themeColors = themeColors)
                        MonthlyTableCell(text = if (isBn) "বার" else "Day", width = 45.dp, isHeader = true, themeColors = themeColors)
                        MonthlyTableCell(text = if (isBn) "ফজর" else "Fajr", width = 75.dp, isHeader = true, themeColors = themeColors)
                        MonthlyTableCell(text = if (isBn) "সূর্যোদয়" else "Sunrise", width = 75.dp, isHeader = true, themeColors = themeColors)
                        MonthlyTableCell(text = if (isBn) "যোহর" else "Dhuhr", width = 75.dp, isHeader = true, themeColors = themeColors)
                        MonthlyTableCell(text = if (isBn) "আসর" else "Asr", width = 75.dp, isHeader = true, themeColors = themeColors)
                        MonthlyTableCell(text = if (isBn) "মাগরিব" else "Maghrib", width = 75.dp, isHeader = true, themeColors = themeColors)
                        MonthlyTableCell(text = if (isBn) "এশা" else "Isha", width = 75.dp, isHeader = true, themeColors = themeColors)
                    }

                    // Table Body Rows
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(monthData) { row ->
                            val rowBg = when {
                                row.isToday -> Color(0xFF10B981).copy(alpha = 0.18f)
                                row.isFriday -> Color(0xFF0284C7).copy(alpha = 0.06f)
                                else -> Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .background(rowBg, RoundedCornerShape(6.dp))
                                    .padding(vertical = 6.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MonthlyTableCell(
                                    text = if (isBn) IslamicCalendarHelper.toBnDigits(row.day) else row.day.toString(),
                                    width = 50.dp,
                                    isHighlight = row.isToday,
                                    themeColors = themeColors
                                )
                                MonthlyTableCell(
                                    text = row.dayOfWeekName,
                                    width = 45.dp,
                                    isFriday = row.isFriday,
                                    isHighlight = row.isToday,
                                    themeColors = themeColors
                                )
                                MonthlyTableCell(text = row.fajr, width = 75.dp, themeColors = themeColors)
                                MonthlyTableCell(text = row.sunrise, width = 75.dp, themeColors = themeColors)
                                MonthlyTableCell(text = row.dhuhr, width = 75.dp, themeColors = themeColors)
                                MonthlyTableCell(text = row.asr, width = 75.dp, themeColors = themeColors)
                                MonthlyTableCell(text = row.maghrib, width = 75.dp, themeColors = themeColors)
                                MonthlyTableCell(text = row.isha, width = 75.dp, themeColors = themeColors)
                            }

                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = themeColors.displayText.copy(alpha = 0.04f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class MonthlyDayRowData(
    val day: Int,
    val dayOfWeekName: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val isToday: Boolean,
    val isFriday: Boolean
)

@Composable
private fun MonthlyTableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isHighlight: Boolean = false,
    isFriday: Boolean = false,
    themeColors: CalculatorThemeColors
) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = when {
            isHeader -> FontWeight.Bold
            isHighlight -> FontWeight.ExtraBold
            isFriday -> FontWeight.Bold
            else -> FontWeight.Normal
        },
        color = when {
            isHeader -> themeColors.titleBarBg
            isHighlight -> themeColors.titleBarBg
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

private fun convertDigitsToBn(str: String): String {
    val bnDigits = mapOf('0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪', '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯')
    return str.map { bnDigits[it] ?: it }.joinToString("")
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
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isDone) themeColors.titleBarBg.copy(alpha = 0.12f) else themeColors.cardBg,
        border = BorderStroke(1.dp, if (isDone) themeColors.titleBarBg else themeColors.displayText.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isDone) themeColors.titleBarBg else themeColors.displayText.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 11.5.sp,
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium,
                color = if (isDone) themeColors.titleBarBg else themeColors.displayText
            )
        }
    }
}

private fun formatWaqtDuration(startMillis: Long, endMillis: Long, isBn: Boolean): String {
    val totalMinutes = maxOf(0L, (endMillis - startMillis) / 60000L).toInt()
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return if (isBn) {
        if (hours > 0 && mins > 0) {
            "${IslamicCalendarHelper.toBnDigits(hours)} ঘণ্টা ${IslamicCalendarHelper.toBnDigits(mins)} মি."
        } else if (hours > 0) {
            "${IslamicCalendarHelper.toBnDigits(hours)} ঘণ্টা"
        } else {
            "${IslamicCalendarHelper.toBnDigits(mins)} মিনিট"
        }
    } else {
        if (hours > 0 && mins > 0) {
            "${hours}h ${mins}m"
        } else if (hours > 0) {
            "${hours}h"
        } else {
            "${mins}m"
        }
    }
}

private data class ActiveWaqtData(
    val activeId: String,
    val activeTitleBn: String,
    val activeTitleEn: String,
    val activeTimeStr: String,
    val currentStartMillis: Long,
    val currentEndMillis: Long,
    val nextTitleBn: String,
    val nextTitleEn: String,
    val nextTimeStr: String,
    val nextStartMillis: Long,
    val nextEndMillis: Long,
    val nextDurationStrBn: String,
    val nextDurationStrEn: String,
    val isForbidden: Boolean
)

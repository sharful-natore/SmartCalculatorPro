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
    val noteBn: String? = null,
    val noteEn: String? = null
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
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)

        // Julian Day Number Calculation
        var y = gYear
        var m = gMonth
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        val jd = (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + gDay + b - 1524.5

        // Approximate Islamic date offset calibration for Bangladesh
        val islamicEpoch = 1948439.5
        val daysSinceEpoch = jd - islamicEpoch
        val hijriCycle = (daysSinceEpoch / 10631.0).toInt()
        val remainingDays = daysSinceEpoch - (hijriCycle * 10631.0)
        val hijriYear = (hijriCycle * 30) + ((remainingDays - 1.0) / 354.366).toInt() + 1
        val yearStartJd = islamicEpoch + ((hijriYear - 1) * 354) + (((11 * (hijriYear - 1)) + 3) / 30)
        var dayInYear = (jd - yearStartJd).toInt()

        if (dayInYear < 0) dayInYear = 0

        var hijriMonth = (dayInYear / 29.5).toInt()
        if (hijriMonth > 11) hijriMonth = 11
        if (hijriMonth < 0) hijriMonth = 0

        val hijriDay = ((dayInYear - (hijriMonth * 29.5)).toInt() + 1).coerceIn(1, 30)

        return if (isBn) {
            val monthName = hijriMonthsBn.getOrElse(hijriMonth) { "মুহাররম" }
            "${toBnDigits(hijriDay)} $monthName ${toBnDigits(hijriYear.toString())} হিজরি"
        } else {
            val monthName = hijriMonthsEn.getOrElse(hijriMonth) { "Muharram" }
            "$hijriDay $monthName $hijriYear AH"
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
    var showFiqhInfoDialog by remember { mutableStateOf(false) }

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
    val timings = remember(viewModel.selectedIslamicDistrictEn, displayCalendar.get(Calendar.DAY_OF_YEAR), displayCalendar.get(Calendar.YEAR)) {
        NamazTimeService.getPrayerTimesForDistrict(context, viewModel.selectedIslamicDistrictEn, displayCalendar)
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
    val liveTodayTimings = remember(viewModel.selectedIslamicDistrictEn, currentTimeMillis) {
        val todayCal = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        NamazTimeService.getPrayerTimesForDistrict(context, viewModel.selectedIslamicDistrictEn, todayCal)
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

    val now = currentTimeMillis

    // Determine Currently Active Waqt & Next Waqt
    val currentWaqtData = when {
        now < calFajr.timeInMillis -> {
            ActiveWaqtData(
                activeId = "isha",
                activeTitleBn = "তাহাজ্জুদ / এশা",
                activeTitleEn = "Tahajjud / Isha",
                activeTimeStr = liveTodayTimings.isha,
                currentEndMillis = calFajr.timeInMillis,
                nextTitleBn = "ফজর",
                nextTitleEn = "Fajr",
                nextTimeStr = liveTodayTimings.fajr,
                nextStartMillis = calFajr.timeInMillis,
                isForbidden = false
            )
        }
        now < calSunrise.timeInMillis -> {
            ActiveWaqtData(
                activeId = "fajr",
                activeTitleBn = "ফজর",
                activeTitleEn = "Fajr",
                activeTimeStr = liveTodayTimings.fajr,
                currentEndMillis = calSunrise.timeInMillis,
                nextTitleBn = "সূর্যোদয় (নিষিদ্ধ)",
                nextTitleEn = "Sunrise (Forbidden)",
                nextTimeStr = liveTodayTimings.sunrise,
                nextStartMillis = calSunrise.timeInMillis,
                isForbidden = false
            )
        }
        now < calIshraq.timeInMillis -> {
            ActiveWaqtData(
                activeId = "sunrise_forbidden",
                activeTitleBn = "নিষিদ্ধ সময়",
                activeTitleEn = "Forbidden Time",
                activeTimeStr = liveTodayTimings.sunrise,
                currentEndMillis = calIshraq.timeInMillis,
                nextTitleBn = "ইশরাক ও চাশত",
                nextTitleEn = "Ishraq & Duha",
                nextTimeStr = liveIshraqStr,
                nextStartMillis = calIshraq.timeInMillis,
                isForbidden = true
            )
        }
        now < calZawaal.timeInMillis -> {
            ActiveWaqtData(
                activeId = "ishraq_duha",
                activeTitleBn = "ইশরাক ও চাশত",
                activeTitleEn = "Ishraq & Duha",
                activeTimeStr = liveIshraqStr,
                currentEndMillis = calZawaal.timeInMillis,
                nextTitleBn = "ঠিক দুপুর (নিষিদ্ধ)",
                nextTitleEn = "Midday (Forbidden)",
                nextTimeStr = liveZawaalStr,
                nextStartMillis = calZawaal.timeInMillis,
                isForbidden = false
            )
        }
        now < calDhuhr.timeInMillis -> {
            ActiveWaqtData(
                activeId = "midday_forbidden",
                activeTitleBn = "নিষিদ্ধ সময়",
                activeTitleEn = "Forbidden Time",
                activeTimeStr = liveZawaalStr,
                currentEndMillis = calDhuhr.timeInMillis,
                nextTitleBn = "যোহর",
                nextTitleEn = "Dhuhr",
                nextTimeStr = liveTodayTimings.dhuhr,
                nextStartMillis = calDhuhr.timeInMillis,
                isForbidden = true
            )
        }
        now < calAsr.timeInMillis -> {
            ActiveWaqtData(
                activeId = "dhuhr",
                activeTitleBn = "যোহর",
                activeTitleEn = "Dhuhr",
                activeTimeStr = liveTodayTimings.dhuhr,
                currentEndMillis = calAsr.timeInMillis,
                nextTitleBn = "আসর",
                nextTitleEn = "Asr",
                nextTimeStr = liveTodayTimings.asr,
                nextStartMillis = calAsr.timeInMillis,
                isForbidden = false
            )
        }
        now < calSunsetForbidden.timeInMillis -> {
            ActiveWaqtData(
                activeId = "asr",
                activeTitleBn = "আসর",
                activeTitleEn = "Asr",
                activeTimeStr = liveTodayTimings.asr,
                currentEndMillis = calSunsetForbidden.timeInMillis,
                nextTitleBn = "সূর্যাস্তকাল (নিষিদ্ধ)",
                nextTitleEn = "Sunset (Forbidden)",
                nextTimeStr = liveSunsetForbiddenStr,
                nextStartMillis = calSunsetForbidden.timeInMillis,
                isForbidden = false
            )
        }
        now < calMaghrib.timeInMillis -> {
            ActiveWaqtData(
                activeId = "sunset_forbidden",
                activeTitleBn = "নিষিদ্ধ সময়",
                activeTitleEn = "Forbidden Time",
                activeTimeStr = liveSunsetForbiddenStr,
                currentEndMillis = calMaghrib.timeInMillis,
                nextTitleBn = "মাগরিব",
                nextTitleEn = "Maghrib",
                nextTimeStr = liveTodayTimings.maghrib,
                nextStartMillis = calMaghrib.timeInMillis,
                isForbidden = true
            )
        }
        now < calIsha.timeInMillis -> {
            ActiveWaqtData(
                activeId = "maghrib",
                activeTitleBn = "মাগরিব",
                activeTitleEn = "Maghrib",
                activeTimeStr = liveTodayTimings.maghrib,
                currentEndMillis = calIsha.timeInMillis,
                nextTitleBn = "এশা",
                nextTitleEn = "Isha",
                nextTimeStr = liveTodayTimings.isha,
                nextStartMillis = calIsha.timeInMillis,
                isForbidden = false
            )
        }
        else -> {
            ActiveWaqtData(
                activeId = "isha",
                activeTitleBn = "এশা",
                activeTitleEn = "Isha",
                activeTimeStr = liveTodayTimings.isha,
                currentEndMillis = calFajrTomorrow.timeInMillis,
                nextTitleBn = "ফজর",
                nextTitleEn = "Fajr",
                nextTimeStr = liveTodayTimings.fajr,
                nextStartMillis = calFajrTomorrow.timeInMillis,
                isForbidden = false
            )
        }
    }

    val currentRemainingMillis = maxOf(0L, currentWaqtData.currentEndMillis - now)
    val nextCountdownMillis = maxOf(0L, currentWaqtData.nextStartMillis - now)

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
                icon = Icons.Default.WbTwilight,
                noteBn = "সাহরি শেষ ও ফজরের শুরু",
                noteEn = "End of Sahri & Start of Fajr"
            ),
            PrayerWaqtItem(
                id = "sunrise",
                nameBn = "সূর্যোদয়",
                nameEn = "Sunrise",
                startTimeStr = timings.sunrise,
                endTimeStr = ishraqTimeStr,
                timeRangeStr = "${timings.sunrise} - ${ishraqTimeStr}",
                icon = Icons.Default.WbSunny,
                noteBn = "সূর্যোদয়কাল",
                noteEn = "Sunrise time"
            ),
            PrayerWaqtItem(
                id = "sunrise_forbidden",
                nameBn = "নিষিদ্ধ সময়",
                nameEn = "Forbidden Time",
                startTimeStr = timings.sunrise,
                endTimeStr = ishraqTimeStr,
                timeRangeStr = "${timings.sunrise} - ${ishraqTimeStr}",
                icon = Icons.Default.WarningAmber,
                isForbidden = true,
                noteBn = "সূর্য ওঠার সময় ১৫ মিনিট সকল নামাজ হারাম",
                noteEn = "Prayers forbidden during 15m sunrise"
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
                noteBn = "অসীম সওয়াবের নফল সালাত",
                noteEn = "Voluntary morning prayers"
            ),
            PrayerWaqtItem(
                id = "zawaal_forbidden",
                nameBn = "নিষিদ্ধ সময়",
                nameEn = "Forbidden Time",
                startTimeStr = zawaalTimeStr,
                endTimeStr = timings.dhuhr,
                timeRangeStr = "${zawaalTimeStr} - ${timings.dhuhr}",
                icon = Icons.Default.WarningAmber,
                isForbidden = true,
                noteBn = "ঠিক দুপুর / জাওয়াল (মাথার উপর সূর্য)",
                noteEn = "Zenith midday forbidden period"
            ),
            PrayerWaqtItem(
                id = "dhuhr",
                nameBn = "যোহর",
                nameEn = "Dhuhr",
                startTimeStr = timings.dhuhr,
                endTimeStr = timings.asr,
                timeRangeStr = "${timings.dhuhr} - ${timings.asr}",
                icon = Icons.Default.LightMode,
                noteBn = "৪ সুন্নত, ৪ ফরজ, ২ সুন্নত",
                noteEn = "4 Sunnah, 4 Fard, 2 Sunnah"
            ),
            PrayerWaqtItem(
                id = "asr",
                nameBn = "আসর",
                nameEn = "Asr",
                startTimeStr = timings.asr,
                endTimeStr = sunsetForbiddenTimeStr,
                timeRangeStr = "${timings.asr} - ${sunsetForbiddenTimeStr}",
                icon = Icons.Default.WbSunny,
                noteBn = "৪ রাকাত ফরজ (সালাতুল উস্তা)",
                noteEn = "4 Rakat Fard"
            ),
            PrayerWaqtItem(
                id = "sunset_forbidden",
                nameBn = "নিষিদ্ধ সময়",
                nameEn = "Forbidden Time",
                startTimeStr = sunsetForbiddenTimeStr,
                endTimeStr = timings.maghrib,
                timeRangeStr = "${sunsetForbiddenTimeStr} - ${timings.maghrib}",
                icon = Icons.Default.WarningAmber,
                isForbidden = true,
                noteBn = "সূর্যাস্তের সময় নামাজ নিষেধ (ঐ দিনের আসর ছাড়া)",
                noteEn = "Sunset 15m forbidden period"
            ),
            PrayerWaqtItem(
                id = "maghrib",
                nameBn = "মাগরিব",
                nameEn = "Maghrib",
                startTimeStr = timings.maghrib,
                endTimeStr = timings.isha,
                timeRangeStr = "${timings.maghrib} - ${timings.isha}",
                icon = Icons.Default.DarkMode,
                noteBn = "ইফতার ও মাগরিব: ৩ ফরজ, ২ সুন্নত",
                noteEn = "Iftar & Maghrib: 3 Fard, 2 Sunnah"
            ),
            PrayerWaqtItem(
                id = "isha",
                nameBn = "এশা",
                nameEn = "Isha",
                startTimeStr = timings.isha,
                endTimeStr = timings.sahri,
                timeRangeStr = "${timings.isha} - ${timings.sahri}",
                icon = Icons.Default.NightsStay,
                noteBn = "৪ ফরজ, ২ সুন্নত, ৩ বিতর, তাহাজ্জুদ",
                noteEn = "4 Fard, 2 Sunnah, 3 Witr"
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
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TOP HEADER BAR: Section Title & District Pill ---
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
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isBn) "লাইভ ওয়াক্ত ও ৫ ওয়াক্ত ট্র্যাকার" else "Live Waqt & Salah Tracker",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.65f)
                    )
                }

                // District Switcher Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0D9488).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showDistrictSheet = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "District",
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) viewModel.selectedIslamicDistrictBn.split(" ")[0] else viewModel.selectedIslamicDistrictEn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D9488)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // --- 1. DUAL HERO STATUS CARDS (এখন & পরবর্তী) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // LEFT CARD: এখন (Current Waqt)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentWaqtData.isForbidden) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (currentWaqtData.isForbidden) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isBn) "এখন" else "Now",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentWaqtData.isForbidden) Color(0xFFDC2626) else Color(0xFF047857)
                            )
                            // Live Pulse Status Dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (currentWaqtData.isForbidden) Color(0xFFDC2626) else Color(0xFF10B981))
                            )
                        }

                        Text(
                            text = if (isBn) currentWaqtData.activeTitleBn else currentWaqtData.activeTitleEn,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (currentWaqtData.isForbidden) Color(0xFF991B1B) else Color(0xFF064E3B)
                        )

                        Text(
                            text = currentWaqtData.activeTimeStr,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Countdown remaining
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentWaqtData.isForbidden) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "-${formatTimerClock(currentRemainingMillis)}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = if (currentWaqtData.isForbidden) Color(0xFFB91C1C) else Color(0xFF047857),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }

                // RIGHT CARD: পরবর্তী (Next Waqt)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F9FF)
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFF0284C7).copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isBn) "পরবর্তী" else "Next",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = if (isBn) currentWaqtData.nextTitleBn else currentWaqtData.nextTitleEn,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0C4A6E)
                        )

                        Text(
                            text = currentWaqtData.nextTimeStr,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Countdown to next waqt
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "-${formatTimerClock(nextCountdownMillis)}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF0284C7),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }
            }

            // --- 2. DATE NAVIGATION BAR (TRIPLE CALENDAR SELECTOR) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
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
                            tint = Color(0xFF0D9488),
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
                                color = Color(0xFF0D9488).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isBn) "আজকের দিনে ফিরুন ↺" else "Back to Today ↺",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D9488),
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
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // --- 3. PRAYER SCHEDULE TIMETABLE (LIST / TABLE) ---
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

                        // Check if row has active highlight (forest green for active waqt like in wireframe, amber for forbidden)
                        val rowBg = when {
                            isItemActive && item.isForbidden -> Color(0xFFDC2626) // Forbidden active highlight
                            isItemActive -> Color(0xFF065F46) // Green active highlight matching user wireframe!
                            item.isForbidden -> Color(0xFFFEF3C7).copy(alpha = 0.4f)
                            else -> Color.Transparent
                        }

                        val textColor = when {
                            isItemActive -> Color.White
                            item.isForbidden -> Color(0xFFD97706)
                            else -> themeColors.displayText
                        }

                        val subTextColor = when {
                            isItemActive -> Color.White.copy(alpha = 0.8f)
                            item.isForbidden -> Color(0xFFB45309)
                            else -> themeColors.displayText.copy(alpha = 0.55f)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Waqt Name & Category / Note
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (item.isForbidden) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = "Forbidden",
                                            tint = if (isItemActive) Color.White else Color(0xFFD97706),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isBn) item.nameBn else item.nameEn,
                                                fontSize = 15.sp,
                                                fontWeight = if (isItemActive || item.isForbidden) FontWeight.Bold else FontWeight.SemiBold,
                                                color = textColor
                                            )
                                            if (isItemActive) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color.White.copy(alpha = 0.25f)
                                                ) {
                                                    Text(
                                                        text = if (isBn) "সক্রিয়" else "Active",
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                                    )
                                                }
                                            }
                                        }

                                        val note = if (isBn) item.noteBn else item.noteEn
                                        if (note != null && (item.isForbidden || item.isNafl)) {
                                            Text(
                                                text = note,
                                                fontSize = 11.sp,
                                                color = subTextColor
                                            )
                                        }
                                    }
                                }

                                // Time Range: e.g. "04:32 - 06:03" or "00:00 - 00:00"
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.timeRangeStr,
                                        fontSize = 14.sp,
                                        fontWeight = if (isItemActive || item.isForbidden) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = textColor
                                    )

                                    if (!item.isForbidden && !item.isNafl) {
                                        Spacer(modifier = Modifier.width(10.dp))
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
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (hasAlert) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                                contentDescription = "Alert",
                                                tint = if (isItemActive) Color.White else (if (hasAlert) Color(0xFF0D9488) else themeColors.displayText.copy(alpha = 0.3f)),
                                                modifier = Modifier.size(16.dp)
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
                            color = if (completedCount == 5) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF0D9488).copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = if (completedCount == 5) Icons.Default.CheckCircle else Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = if (completedCount == 5) Color(0xFF10B981) else Color(0xFF0D9488),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBn) "$completedCount/৫ ওয়াক্ত" else "$completedCount/5 Done",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (completedCount == 5) Color(0xFF10B981) else Color(0xFF0D9488)
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

    if (showFiqhInfoDialog) {
        AlertDialog(
            onDismissRequest = { showFiqhInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF0D9488))
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
                        color = Color(0xFF0D9488)
                    )
                    Text(
                        text = if (isBn) "রাসূলুল্লাহ (সা.) ইরশাদ করেছেন: 'আল্লাহর নিকট সর্বাধিক প্রিয় আমল হলো সময়মতো নামাজ আদায় করা।' (সহীহ বুখারী)"
                        else "Prophet Muhammad (PBUH) said: 'The dearest deed to Allah is performing prayer at its earliest appointed time.' (Sahih Bukhari)"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFiqhInfoDialog = false }) {
                    Text(text = if (isBn) "ঠিক আছে" else "Close", color = Color(0xFF0D9488), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
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
        color = if (isDone) Color(0xFF10B981).copy(alpha = 0.12f) else themeColors.cardBg,
        border = BorderStroke(1.dp, if (isDone) Color(0xFF10B981) else themeColors.displayText.copy(alpha = 0.08f)),
        modifier = modifier
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
    val activeTimeStr: String,
    val currentEndMillis: Long,
    val nextTitleBn: String,
    val nextTitleEn: String,
    val nextTimeStr: String,
    val nextStartMillis: Long,
    val isForbidden: Boolean
)

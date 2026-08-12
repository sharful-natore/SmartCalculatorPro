package com.example.util

import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.util.*

data class MultiDateInfo(
    val englishDate: String,
    val englishMonthYear: String,
    val englishDayName: String,
    val bengaliDate: String,
    val bengaliMonthYear: String,
    val hijriDate: String,
    val hijriMonthYear: String
)

object CalendarUtils {

    private val BENGALI_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val BENGALI_MONTHS = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )
    private val BENGALI_MONTHS_EN = listOf(
        "Boishakh", "Jyaistha", "Ashar", "Srabon", "Bhadra", "Ashwin",
        "Kartik", "Agrahayan", "Poush", "Magh", "Falgun", "Choitro"
    )
    private val HIJRI_MONTHS = listOf(
        "মহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি",
        "রজব", "শা'বান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )
    private val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )
    private val BENGALI_DAYS = listOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")

    fun toBengaliDigits(number: Int): String {
        return number.toString().map { if (it in '0'..'9') BENGALI_DIGITS[it - '0'] else it }.joinToString("")
    }

    fun getMultiDateInfo(calendar: Calendar = Calendar.getInstance(), isBn: Boolean = true): MultiDateInfo {
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)
        val gMonth = calendar.get(Calendar.MONTH) // 0-indexed
        val gYear = calendar.get(Calendar.YEAR)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

        // English Date String
        val enDayName = if (isBn) BENGALI_DAYS[dayOfWeek] else SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.time)
        val enMonthName = if (isBn) {
            val bnMonths = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
            bnMonths[gMonth]
        } else {
            SimpleDateFormat("MMMM", Locale.ENGLISH).format(calendar.time)
        }
        val enFormattedDate = if (isBn) "${toBengaliDigits(gDay)} $enMonthName ${toBengaliDigits(gYear)}" else "$gDay $enMonthName $gYear"

        // Bengali Calendar Calculation
        val (bDay, bMonthIdx, bYear) = getBengaliDateComponents(calendar)
        val bMonthName = if (isBn) BENGALI_MONTHS[bMonthIdx] else BENGALI_MONTHS_EN[bMonthIdx]
        val bnFormattedDate = if (isBn) "${toBengaliDigits(bDay)} $bMonthName ${toBengaliDigits(bYear)} বঙ্গাব্দ" else "$bDay $bMonthName $bYear Bangabda"

        // Hijri Calendar Calculation
        val (hDay, hMonthIdx, hYear) = getHijriDateComponents(calendar)
        val hMonthName = if (isBn) HIJRI_MONTHS[hMonthIdx.coerceIn(0, 11)] else HIJRI_MONTHS_EN[hMonthIdx.coerceIn(0, 11)]
        val hijriFormattedDate = if (isBn) "${toBengaliDigits(hDay)} $hMonthName ${toBengaliDigits(hYear)} হিজরী" else "$hDay $hMonthName $hYear Hijri"

        return MultiDateInfo(
            englishDate = enFormattedDate,
            englishMonthYear = "$enMonthName ${if (isBn) toBengaliDigits(gYear) else gYear}",
            englishDayName = enDayName,
            bengaliDate = bnFormattedDate,
            bengaliMonthYear = "$bMonthName ${if (isBn) toBengaliDigits(bYear) else bYear}",
            hijriDate = hijriFormattedDate,
            hijriMonthYear = "$hMonthName ${if (isBn) toBengaliDigits(hYear) else hYear}"
        )
    }

    fun getBengaliDateComponents(calendar: Calendar): Triple<Int, Int, Int> {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) // 0..11
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        val monthDays = intArrayOf(31, if (isLeapYear) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        // Day of Gregorian year (0..364)
        var dayOfYear = day
        for (i in 0 until month) {
            dayOfYear += monthDays[i]
        }

        // Bengali New Year starts on April 14 (day 104 in non-leap year, 105 in leap year)
        val pohelaBoishakhDay = if (isLeapYear) 105 else 104

        val bYear = if (dayOfYear >= pohelaBoishakhDay) year - 593 else year - 594

        var bDayOfYear = if (dayOfYear >= pohelaBoishakhDay) {
            dayOfYear - pohelaBoishakhDay + 1
        } else {
            val prevYearDays = if ((year - 1) % 4 == 0 && (year - 1) % 100 != 0 || (year - 1) % 400 == 0) 366 else 365
            prevYearDays - (if (isLeapYear) 105 else 104) + dayOfYear + 1
        }

        // Bangla month lengths: Boishakh to Bhadra (5 months) = 31 days each.
        // Ashwin to Falgun = 30 days each (Falgun 31 in leap year). Chaitra = 30 days.
        val banglaMonthLengths = intArrayOf(31, 31, 31, 31, 31, 30, 30, 30, 30, 30, if (isLeapYear) 31 else 30, 30)

        var bMonthIdx = 0
        while (bMonthIdx < 12 && bDayOfYear > banglaMonthLengths[bMonthIdx]) {
            bDayOfYear -= banglaMonthLengths[bMonthIdx]
            bMonthIdx++
        }

        val bDay = if (bDayOfYear <= 0) 1 else bDayOfYear
        return Triple(bDay, bMonthIdx.coerceIn(0, 11), bYear)
    }

    fun getHijriDateComponents(calendar: Calendar): Triple<Int, Int, Int> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val localDate = LocalDate.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                val hijrahDate = HijrahDate.from(localDate)
                val hDay = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                val hMonth = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) - 1
                val hYear = hijrahDate.get(java.time.temporal.ChronoField.YEAR)
                return Triple(hDay, hMonth, hYear)
            } catch (e: Exception) {
                // Fallback below
            }
        }

        // Fallback Islamic tabular calendar algorithm
        val julianDay = getJulianDay(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        val l = julianDay - 1948440 + 10632
        val n = ((l - 1) / 10631).toInt()
        val l1 = l - 10631 * n + 354
        val j = (((10985 - l1) / 5316)).toInt() * (((50 * l1) / 17719)).toInt() + ((l1 / 5670)).toInt() * (((43 * l1) / 15238)).toInt()
        val l2 = l1 - (((30 - j) / 15)).toInt() * (((17719 * j) / 50)).toInt() - ((j / 16)).toInt() * (((15238 * j) / 43)).toInt() + 29
        val hMonth = (((24 * l2) / 709)).toInt()
        val hDay = (l2 - (((709 * hMonth) / 24)).toInt()).toInt()
        val hYear = (30 * n + j - 30).toInt()

        return Triple(hDay, (hMonth - 1).coerceIn(0, 11), hYear)
    }

    private fun getJulianDay(year: Int, month: Int, day: Int): Long {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        return (365.25 * (y + 4716)).toLong() + (30.6001 * (m + 1)).toLong() + day + b - 1524
    }
}

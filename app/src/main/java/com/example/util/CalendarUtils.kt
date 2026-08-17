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

    fun getSpecialEvents(calendar: Calendar, isBn: Boolean): List<String> {
        val events = mutableListOf<String>()

        val gDay = calendar.get(Calendar.DAY_OF_MONTH)
        val gMonth = calendar.get(Calendar.MONTH) // 0-indexed
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 6 = Fri, 7 = Sat

        // 1. Weekend Holidays (Friday & Saturday)
        if (dayOfWeek == Calendar.FRIDAY) {
            events.add(if (isBn) "🔴 সাপ্তাহিক ছুটি (শুক্রবার)" else "🔴 Weekend Holiday (Friday)")
        } else if (dayOfWeek == Calendar.SATURDAY) {
            events.add(if (isBn) "🔴 সাপ্তাহিক ছুটি (শনিবার)" else "🔴 Weekend Holiday (Saturday)")
        }

        // 2. Fixed Gregorian Special Days / National Holidays
        when (gMonth) {
            0 -> { // January
                if (gDay == 1) events.add(if (isBn) "🎆 ইংরেজি নববর্ষ" else "🎆 New Year's Day")
                if (gDay == 10) events.add(if (isBn) "🇧🇩 বঙ্গবন্ধুর স্বদেশ প্রত্যাবর্তন দিবস" else "🇧🇩 Bangabandhu's Homecoming Day")
            }
            1 -> { // February
                if (gDay == 14) events.add(if (isBn) "💖 বিশ্ব ভালোবাসা দিবস" else "💖 Valentine's Day")
                if (gDay == 21) events.add(if (isBn) "🌺 আন্তর্জাতিক মাতৃভাষা দিবস ও শহীদ দিবস" else "🌺 International Mother Language Day")
            }
            2 -> { // March
                if (gDay == 7) events.add(if (isBn) "📜 ঐতিহাসিক ৭ই মার্চ ভাষণ দিবস" else "📜 Historic March 7th Speech Day")
                if (gDay == 8) events.add(if (isBn) "👩 আন্তর্জাতিক নারী দিবস" else "👩 International Women's Day")
                if (gDay == 17) events.add(if (isBn) "👶 জাতির পিতার জন্মবার্ষিকী ও জাতীয় শিশু দিবস" else "👶 Bangabandhu's Birthday")
                if (gDay == 26) events.add(if (isBn) "🇧🇩 মহান স্বাধীনতা ও জাতীয় দিবস" else "🇧🇩 Independence Day")
            }
            3 -> { // April
                if (gDay == 14) events.add(if (isBn) "🌾 পহেলা বৈশাখ (বাংলা নববর্ষ)" else "🌾 Pahela Baishakh (Bengali New Year)")
                if (gDay == 17) events.add(if (isBn) "📜 ঐতিহাসিক মুজিবনগর দিবস" else "📜 Historic Mujibnagar Day")
            }
            4 -> { // May
                if (gDay == 1) events.add(if (isBn) "🛠️ মে দিবস (আন্তর্জাতিক শ্রমিক দিবস)" else "🛠️ May Day (Labor Day)")
            }
            5 -> { // June
                if (gDay == 5) events.add(if (isBn) "🌍 বিশ্ব পরিবেশ দিবস" else "🌍 World Environment Day")
                if (gDay == 7) events.add(if (isBn) "📜 ঐতিহাসিক ৬ দফা দিবস" else "📜 Historic Six-Point Day")
                if (gDay == 23) events.add(if (isBn) "🏛️ আওয়ামী লীগের প্রতিষ্ঠা দিবস" else "🏛️ Awami League Founding Day")
            }
            7 -> { // August
                if (gDay == 5) events.add(if (isBn) "🎂 শেখ কামালের জন্মদিন" else "🎂 Sheikh Kamal's Birthday")
                if (gDay == 8) events.add(if (isBn) "💐 বেগম ফজিলাতুন্নেছা মুজিবের জন্মদিন" else "💐 Begum Fazilatunnesa Mujib's Birthday")
                if (gDay == 12) events.add(if (isBn) "🌟 আন্তর্জাতিক যুব দিবস" else "🌟 International Youth Day")
                if (gDay == 15) events.add(if (isBn) "🖤 জাতীয় শোক দিবস" else "🖤 National Mourning Day")
            }
            9 -> { // October
                if (gDay == 18) events.add(if (isBn) "🧒 শেখ রাসেল দিবস" else "🧒 Sheikh Russel Day")
                if (gDay == 24) events.add(if (isBn) "🌐 জাতিসংঘ দিবস" else "🌐 United Nations Day")
            }
            10 -> { // November
                if (gDay == 3) events.add(if (isBn) "🖤 জেল হত্যা দিবস" else "🖤 Jail Killing Day")
                if (gDay == 10) events.add(if (isBn) "✊ শহীদ নূর হোসেন দিবস" else "✊ Shaheed Noor Hossain Day")
                if (gDay == 21) events.add(if (isBn) "🪖 সশস্ত্র বাহিনী দিবস" else "🪖 Armed Forces Day")
            }
            11 -> { // December
                if (gDay == 14) events.add(if (isBn) "🖤 শহীদ বুদ্ধিজীবী দিবস" else "🖤 Martyred Intellectuals Day")
                if (gDay == 16) events.add(if (isBn) "🇧🇩 মহান বিজয় দিবস" else "🇧🇩 Victory Day")
                if (gDay == 25) events.add(if (isBn) "🎄 শুভ বড়দিন (ক্রিসমাস)" else "🎄 Christmas Day")
                if (gDay == 31) events.add(if (isBn) "🎆 ৩১st নাইট / বছরের শেষ দিন" else "🎆 New Year's Eve")
            }
        }

        // 3. Islamic Special Days (based on Hijri Month 0-11 & Day)
        val (hDay, hMonthIdx, _) = getHijriDateComponents(calendar)
        when (hMonthIdx) {
            0 -> { // Muharram
                if (hDay == 1) events.add(if (isBn) "🌙 পহেলা মহররম (হিজরী নববর্ষ)" else "🌙 Hijri New Year")
                if (hDay == 9) events.add(if (isBn) "🕌 পবিত্র তাসু'আ (৯ মহররম রোজা)" else "🕌 Holy Tasu'a")
                if (hDay == 10) events.add(if (isBn) "🕌 পবিত্র আশুরা" else "🕌 Holy Ashura")
            }
            1 -> { // Safar
                if (dayOfWeek == Calendar.WEDNESDAY && hDay >= 24) {
                    events.add(if (isBn) "🕌 পবিত্র আখেরি চাহার শোম্বা" else "🕌 Holy Akhari Chahar Shamba")
                }
            }
            2 -> { // Rabiul Awwal
                if (hDay == 12) events.add(if (isBn) "🕌 পবিত্র ঈদে মিলাদুন্নবী (সা.)" else "🕌 Holy Eid-e-Miladunnabi")
            }
            6 -> { // Rajab
                if (hDay == 27) events.add(if (isBn) "🕌 পবিত্র শবে মেরাজ" else "🕌 Holy Shab-e-Miraj")
            }
            7 -> { // Shaban
                if (hDay == 15) events.add(if (isBn) "🕌 পবিত্র শবে বরাত" else "🕌 Holy Shab-e-Barat")
            }
            8 -> { // Ramadan
                if (hDay == 1) events.add(if (isBn) "🌙 পবিত্র মাহে রমজান শুরু" else "🌙 First Day of Ramadan")
                if (hDay == 17) events.add(if (isBn) "⚔️ ঐতিহাসিক বদর দিবস" else "⚔️ Historic Badr Day")
                if (hDay == 27) events.add(if (isBn) "🕌 পবিত্র শবে কদর" else "🕌 Holy Shab-e-Qadr")
            }
            9 -> { // Shawwal
                if (hDay in 1..3) events.add(if (isBn) "🌙 পবিত্র ঈদুল ফিতর" else "🌙 Holy Eid-ul-Fitr")
            }
            11 -> { // Dhu al-Hijjah
                if (hDay == 9) events.add(if (isBn) "🕋 পবিত্র ইয়াওমে আরাফাহ" else "🕋 Day of Arafah")
                if (hDay in 10..12) events.add(if (isBn) "🕋 পবিত্র ঈদুল আযহা" else "🕋 Holy Eid-ul-Adha")
            }
        }

        // 4. Bengali Calendar Fixed Days
        val (bDay, bMonthIdx, _) = getBengaliDateComponents(calendar)
        if (bMonthIdx == 0 && bDay == 1 && !events.any { it.contains("পহেলা বৈশাখ") || it.contains("Baishakh") }) {
            events.add(if (isBn) "🌾 পহেলা বৈশাখ (বাংলা নববর্ষ)" else "🌾 Bengali New Year")
        }
        if (bMonthIdx == 0 && bDay == 25) {
            events.add(if (isBn) "📜 কবিগুরু রবীন্দ্রনাথ ঠাকুরের জন্মজয়ন্তী" else "📜 Rabindra Jayanti")
        }
        if (bMonthIdx == 1 && bDay == 11) {
            events.add(if (isBn) "📜 জাতীয় কবি কাজী নজরুল ইসলামের জন্মজয়ন্তী" else "📜 Nazrul Jayanti")
        }

        return events
    }

    fun hasSpecialOccasion(calendar: Calendar): Boolean {
        val events = getSpecialEvents(calendar, true)
        return events.any { !it.contains("সাপ্তাহিক ছুটি") }
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

    var hijriOffsetDays: Int = 0

    fun getHijriDateComponents(calendar: Calendar): Triple<Int, Int, Int> {
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_MONTH, -1 + hijriOffsetDays) // Adjust for Bangladesh moon sighting standard and global offset
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val localDate = LocalDate.of(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
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
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
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

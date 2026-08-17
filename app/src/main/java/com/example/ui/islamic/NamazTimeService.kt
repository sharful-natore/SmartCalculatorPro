package com.example.ui.islamic

import android.content.Context
import org.json.JSONObject
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

data class PrayerTimings(
    val sahri: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

data class NextPrayerCountdown(
    val nameBn: String,
    val nameEn: String,
    val remainingMillis: Long
)

object NamazTimeService {

    fun getPrayerTimesForDistrict(context: Context, districtNameEn: String, calendar: Calendar): PrayerTimings {
        val dist = NamazTimeManager.districts.find { it.nameEn.equals(districtNameEn, ignoreCase = true) }
            ?: NamazTimeManager.districts.first() // Fallback to Dhaka
            
        val times = NamazTimeManager.getDailyPrayerTimes(dist.lat, dist.lon, calendar.time)
        
        return PrayerTimings(
            sahri = times["sahri"] ?: "",
            fajr = times["fajr"] ?: "",
            sunrise = times["sunrise"] ?: "",
            dhuhr = times["dhuhr"] ?: "",
            asr = times["asr"] ?: "",
            maghrib = times["maghrib"] ?: "",
            isha = times["isha"] ?: ""
        )
    }

    fun getPrayerTimesForCoordinates(latitude: Double, longitude: Double, calendar: Calendar): PrayerTimings {
        val times = NamazTimeManager.getDailyPrayerTimes(latitude, longitude, calendar.time)
        return PrayerTimings(
            sahri = times["sahri"] ?: "",
            fajr = times["fajr"] ?: "",
            sunrise = times["sunrise"] ?: "",
            dhuhr = times["dhuhr"] ?: "",
            asr = times["asr"] ?: "",
            maghrib = times["maghrib"] ?: "",
            isha = times["isha"] ?: ""
        )
    }

    fun timeStrToMinutes(timeStr: String): Int = NamazTimeManager.timeStrToMinutes(timeStr)

    fun minutesToTimeStr(minutes: Int): String = NamazTimeManager.minutesToTimeStr(minutes)

    fun formatCountdown(remainingMillis: Long, isBn: Boolean): String = NamazTimeManager.formatMillisToCountdown(remainingMillis) // We can adjust this to handle Bn inside Manager if needed
}

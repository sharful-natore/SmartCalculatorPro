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
    private var cachedJson: JSONObject? = null

    // Load and cache the exact JSON asset
    @Synchronized
    private fun getJson(context: Context): JSONObject {
        if (cachedJson != null) return cachedJson!!
        return try {
            val inputStream: InputStream = context.assets.open("bd_namaz_times_exact.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charsets.UTF_8)
            val json = JSONObject(jsonString)
            cachedJson = json
            json
        } catch (e: Exception) {
            e.printStackTrace()
            JSONObject()
        }
    }

    // Get the district offset mapping
    fun getDistrictOffsetMapping(context: Context, districtNameEn: String): Map<String, Int> {
        val defaultOffsets = mapOf("s" to 0, "f" to 0, "d" to 0, "a" to 0, "i" to 0, "is" to 0)
        try {
            val json = getJson(context)
            val districtOffsets = json.optJSONObject("district_offsets") ?: return defaultOffsets
            val distKey = districtNameEn.lowercase(Locale.ENGLISH).replace(" ", "")
            
            // Try direct key or search
            var distObj = districtOffsets.optJSONObject(distKey)
            if (distObj == null) {
                val keys = districtOffsets.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    if (k.equals(districtNameEn, ignoreCase = true)) {
                        distObj = districtOffsets.optJSONObject(k)
                        break
                    }
                }
            }

            if (distObj != null) {
                val map = mutableMapOf<String, Int>()
                val keys = distObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = distObj.optInt(key, 0)
                }
                return map
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return defaultOffsets
    }

    // Direct O(1) lookup for Dhaka base timings (No interpolation)
    fun getDhakaExactBaseTimings(context: Context, month: Int, day: Int): Map<String, String> {
        val json = getJson(context)
        val dhakaMaster = json.optJSONObject("dhaka_master") ?: JSONObject()
        val key = String.format(Locale.ENGLISH, "%02d-%02d", month + 1, day)
        
        val dayObj = dhakaMaster.optJSONObject(key)
        if (dayObj != null) {
            val map = mutableMapOf<String, String>()
            val keys = dayObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = dayObj.optString(k)
            }
            return map
        }
        
        // Final fallback if date not in master (IFB defaults)
        return mapOf(
            "s" to "04:14 AM", "f" to "04:19 AM", "r" to "05:33 AM",
            "d" to "12:17 PM", "a" to "04:23 PM", "i" to "06:32 PM", "is" to "07:47 PM"
        )
    }

    fun timeStrToMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.trim().split(" ")
            val hms = parts[0].split(":")
            var hour = hms[0].toInt()
            val min = hms[1].toInt()
            val isPm = if (parts.size > 1) parts[1].equals("PM", ignoreCase = true) else false
            
            if (isPm && hour < 12) hour += 12
            if (!isPm && hour == 12) hour = 0
            hour * 60 + min
        } catch (e: Exception) {
            300
        }
    }

    fun minutesToTimeStr(minutes: Int): String {
        val wrapped = (minutes + 1440) % 1440
        val hour24 = wrapped / 60
        val min = wrapped % 60
        val isPm = hour24 >= 12
        var hour12 = hour24 % 12
        if (hour12 == 0) hour12 = 12
        val amPm = if (isPm) "PM" else "AM"
        return String.format(Locale.ENGLISH, "%02d:%02d %s", hour12, min, amPm)
    }

    fun getPrayerTimesForDistrict(context: Context, districtNameEn: String, calendar: Calendar): PrayerTimings {
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val baseDhaka = getDhakaExactBaseTimings(context, districtNameEn, month, day)
        val offsets = getDistrictOffsetMapping(context, districtNameEn)

        // Keys: s=sahri, f=fajr, r=sunrise, d=dhuhr, a=asr, i=maghrib, is=isha
        val sBase = baseDhaka["s"] ?: "04:14 AM"
        val fBase = baseDhaka["f"] ?: "04:19 AM"
        val rBase = baseDhaka["r"] ?: "05:33 AM"
        val dBase = baseDhaka["d"] ?: "12:17 PM"
        val aBase = baseDhaka["a"] ?: "04:23 PM"
        val iBase = baseDhaka["i"] ?: "06:32 PM"
        val isBase = baseDhaka["is"] ?: "07:47 PM"

        return PrayerTimings(
            sahri = minutesToTimeStr(timeStrToMinutes(sBase) + (offsets["s"] ?: 0)),
            fajr = minutesToTimeStr(timeStrToMinutes(fBase) + (offsets["f"] ?: 0)),
            sunrise = minutesToTimeStr(timeStrToMinutes(rBase) + (offsets["r"] ?: 0)),
            dhuhr = minutesToTimeStr(timeStrToMinutes(dBase) + (offsets["d"] ?: 0)),
            asr = minutesToTimeStr(timeStrToMinutes(aBase) + (offsets["a"] ?: 0)),
            maghrib = minutesToTimeStr(timeStrToMinutes(iBase) + (offsets["i"] ?: 0)),
            isha = minutesToTimeStr(timeStrToMinutes(isBase) + (offsets["is"] ?: 0))
        )
    }

    // Overload for internal convenience
    private fun getDhakaExactBaseTimings(context: Context, districtNameEn: String, month: Int, day: Int): Map<String, String> {
        return getDhakaExactBaseTimings(context, month, day)
    }

    fun formatCountdown(remainingMillis: Long, isBn: Boolean): String {
        val totalSeconds = remainingMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val hStr = String.format(Locale.ENGLISH, "%02d", hours)
        val mStr = String.format(Locale.ENGLISH, "%02d", minutes)
        val sStr = String.format(Locale.ENGLISH, "%02d", seconds)

        return if (isBn) {
            val bnDigits = mapOf('0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪', '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯')
            val hBn = hStr.map { bnDigits[it] ?: it }.joinToString("")
            val mBn = mStr.map { bnDigits[it] ?: it }.joinToString("")
            val sBn = sStr.map { bnDigits[it] ?: it }.joinToString("")
            "${hBn}:${mBn}:${sBn}"
        } else {
            "${hStr}:${mStr}:${sStr}"
        }
    }
}

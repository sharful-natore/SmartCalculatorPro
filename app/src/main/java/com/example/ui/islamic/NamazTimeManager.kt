package com.example.ui.islamic

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class DistrictCoords(val nameEn: String, val nameBn: String, val lat: Double, val lon: Double)

object NamazTimeManager {

    // Method 3: Pre-configured Map/List of 64 Bangladesh District Coordinates
    val districts = listOf(
        DistrictCoords("Dhaka", "ঢাকা", 23.8103, 90.4125),
        DistrictCoords("Chittagong", "চট্টগ্রাম", 22.3569, 91.7832),
        DistrictCoords("Rajshahi", "রাজশাহী", 24.3745, 88.6042),
        DistrictCoords("Khulna", "খুলনা", 22.8456, 89.5403),
        DistrictCoords("Sylhet", "সিলেট", 24.8949, 91.8687),
        DistrictCoords("Barisal", "বরিশাল", 22.7010, 90.3535),
        DistrictCoords("Rangpur", "রংপুর", 25.7439, 89.2752),
        DistrictCoords("Mymensingh", "ময়মনসিংহ", 24.7471, 90.4203),
        DistrictCoords("Gazipur", "গাজীপুর", 23.9999, 90.4203),
        DistrictCoords("Narayanganj", "নারায়ণগঞ্জ", 23.6238, 90.4998),
        DistrictCoords("Comilla", "কুমিল্লা", 23.4607, 91.1809),
        DistrictCoords("Bogura", "বগুড়া", 24.8481, 89.3730),
        DistrictCoords("Kushtia", "কুষ্টিয়া", 23.9013, 89.1204),
        DistrictCoords("Dinajpur", "দিনাজপুর", 25.6217, 88.6354),
        DistrictCoords("Jessore", "যশোর", 23.1664, 89.2081),
        DistrictCoords("Cox's Bazar", "কক্সবাজার", 21.4272, 92.0058),
        DistrictCoords("Noakhali", "নোয়াখালী", 22.8696, 91.0992),
        DistrictCoords("Brahmanbaria", "ব্রাহ্মণবাড়িয়া", 23.9571, 91.1109),
        DistrictCoords("Pabna", "পাবনা", 24.0063, 89.2493),
        DistrictCoords("Tangail", "টাঙ্গাইল", 24.2513, 89.9167),
        DistrictCoords("Faridpur", "ফরিদপুর", 23.6071, 89.8429),
        DistrictCoords("Gopalganj", "গোপালগঞ্জ", 23.0059, 89.8266),
        DistrictCoords("Madaripur", "মাদারীপুর", 23.1641, 90.1896),
        DistrictCoords("Rajbari", "রাজবাড়ী", 23.7574, 89.6444),
        DistrictCoords("Shariatpur", "শরীয়তপুর", 23.2423, 90.3412),
        DistrictCoords("Bandarban", "বান্দরবান", 22.1953, 92.2184),
        DistrictCoords("Chandpur", "চাঁদপুর", 23.2333, 90.6500),
        DistrictCoords("Feni", "ফেনী", 23.0159, 91.3976),
        DistrictCoords("Khagrachari", "খাগড়াছড়ি", 23.1192, 91.9841),
        DistrictCoords("Lakshmipur", "লক্ষ্মীপুর", 22.9429, 90.8417),
        DistrictCoords("Rangamati", "রাঙ্গামাটি", 22.6574, 92.1733),
        DistrictCoords("Bagerhat", "বাগেরহাট", 22.6516, 89.7859),
        DistrictCoords("Chuadanga", "চুয়াডাঙ্গা", 23.6401, 88.8504),
        DistrictCoords("Jhenaidah", "ঝিনাইদহ", 23.5450, 89.1726),
        DistrictCoords("Magura", "মাগুরা", 23.4873, 89.4199),
        DistrictCoords("Meherpur", "মেহেরপুর", 23.7622, 88.6318),
        DistrictCoords("Narail", "নড়াইল", 23.1725, 89.5126),
        DistrictCoords("Satkhira", "সাতক্ষীরা", 22.7185, 89.0705),
        DistrictCoords("Joypurhat", "জয়পুরহাট", 25.0947, 89.0209),
        DistrictCoords("Naogaon", "নওগাঁ", 24.7936, 88.9318),
        DistrictCoords("Natore", "নাটোর", 24.4102, 88.9595),
        DistrictCoords("Chapainawabganj", "চাঁপাইনবাবগঞ্জ", 24.5965, 88.2753),
        DistrictCoords("Sirajganj", "সিরাজগঞ্জ", 24.4534, 89.7084),
        DistrictCoords("Barguna", "বরগুনা", 22.1591, 90.1245),
        DistrictCoords("Bhola", "ভোলা", 22.6851, 90.6440),
        DistrictCoords("Jhalokati", "ঝালকাঠি", 22.6395, 90.1987),
        DistrictCoords("Patuakhali", "পটুয়াখালী", 22.3596, 90.3297),
        DistrictCoords("Pirojpur", "পিরোজপুর", 22.5781, 89.9699),
        DistrictCoords("Habiganj", "হবিগঞ্জ", 24.3749, 91.4133),
        DistrictCoords("Moulvibazar", "মৌলভীবাজার", 24.4829, 91.7476),
        DistrictCoords("Sunamganj", "সুনামগঞ্জ", 25.0658, 91.3950),
        DistrictCoords("Gaibandha", "গাইবান্ধা", 25.3288, 89.5280),
        DistrictCoords("Kurigram", "কুড়িগ্রাম", 25.8054, 89.6361),
        DistrictCoords("Lalmonirhat", "লালমনিরহাট", 25.9125, 89.4426),
        DistrictCoords("Nilphamari", "নীলফামারী", 25.9317, 88.8560),
        DistrictCoords("Panchagarh", "পঞ্চগড়", 26.3411, 88.5542),
        DistrictCoords("Thakurgaon", "ঠাকুরগাঁও", 26.0337, 88.4617),
        DistrictCoords("Jamalpur", "জামালপুর", 24.9197, 89.9481),
        DistrictCoords("Netrokona", "নেত্রকোণা", 24.8701, 90.7275),
        DistrictCoords("Sherpur", "শেরপুর", 25.0188, 90.0175),
        DistrictCoords("Manikganj", "মানিকগঞ্জ", 23.8644, 89.9967),
        DistrictCoords("Munshiganj", "মুন্সীগঞ্জ", 23.5433, 90.5354),
        DistrictCoords("Narsingdi", "নরসিংদী", 23.9229, 90.7177),
        DistrictCoords("Kishoreganj", "কিশোরগঞ্জ", 24.4449, 90.7766)
    )

    private val dhakaTimeZone = TimeZone.getTimeZone("Asia/Dhaka")

    /**
     * Method 1: getDailyPrayerTimes(latitude, longitude, date)
     * Strictly calibrated to Islamic Foundation Bangladesh (IFB) standards.
     * Fajr Angle: 18°, Isha Angle: 18° (KARACHI method), Madhab: HANAFI.
     */
    fun getDailyPrayerTimes(latitude: Double, longitude: Double, date: Date): Map<String, String> {
        val coordinates = Coordinates(latitude, longitude)
        val dateComponents = DateComponents.from(date)
        
        val params = CalculationMethod.KARACHI.parameters
        params.madhab = Madhab.HANAFI
        
        // IFB Standard Calibration Adjustments
        params.adjustments.fajr = 1
        params.adjustments.dhuhr = 1
        params.adjustments.asr = 0
        params.adjustments.maghrib = 2
        params.adjustments.isha = 1
        
        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)
        
        val formatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        formatter.timeZone = dhakaTimeZone
        
        return mapOf(
            "fajr" to formatter.format(prayerTimes.fajr),
            "sahri" to formatter.format(prayerTimes.fajr),
            "sunrise" to formatter.format(prayerTimes.sunrise),
            "dhuhr" to formatter.format(prayerTimes.dhuhr),
            "asr" to formatter.format(prayerTimes.asr),
            "maghrib" to formatter.format(prayerTimes.maghrib),
            "iftar" to formatter.format(prayerTimes.maghrib),
            "isha" to formatter.format(prayerTimes.isha)
        )
    }

    /**
     * Method 2: getNextWaqtCountdown(latitude, longitude)
     */
    fun getNextWaqtCountdown(latitude: Double, longitude: Double): Pair<String, Long> {
        val now = Date()
        val coordinates = Coordinates(latitude, longitude)
        
        val params = CalculationMethod.KARACHI.parameters
        params.madhab = Madhab.HANAFI
        params.adjustments.fajr = 1
        params.adjustments.dhuhr = 1
        params.adjustments.asr = 0
        params.adjustments.maghrib = 2
        params.adjustments.isha = 1
        
        val dateComponents = DateComponents.from(now)
        var prayerTimes = PrayerTimes(coordinates, dateComponents, params)
        
        var nextPrayer = prayerTimes.nextPrayer()
        var nextTime = prayerTimes.timeForPrayer(nextPrayer)
        
        if (nextTime == null || nextTime.before(now)) {
            val tomorrow = Calendar.getInstance(dhakaTimeZone).apply {
                time = now
                add(Calendar.DAY_OF_YEAR, 1)
            }
            prayerTimes = PrayerTimes(coordinates, DateComponents.from(tomorrow.time), params)
            nextPrayer = com.batoulapps.adhan.Prayer.FAJR
            nextTime = prayerTimes.fajr
        }

        val remainingMillis = nextTime!!.time - now.time
        val prayerName = when(nextPrayer) {
            com.batoulapps.adhan.Prayer.FAJR -> "Fajr / Sahri End"
            com.batoulapps.adhan.Prayer.SUNRISE -> "Sunrise"
            com.batoulapps.adhan.Prayer.DHUHR -> "Dhuhr"
            com.batoulapps.adhan.Prayer.ASR -> "Asr"
            com.batoulapps.adhan.Prayer.MAGHRIB -> "Maghrib / Iftar"
            com.batoulapps.adhan.Prayer.ISHA -> "Isha"
            else -> "Fajr"
        }
        
        return Pair(prayerName, remainingMillis)
    }

    fun formatMillisToCountdown(millis: Long): String {
        val h = TimeUnit.MILLISECONDS.toHours(millis)
        val m = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val s = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(Locale.ENGLISH, "%02dh %02dm %02ds", h, m, s)
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
}

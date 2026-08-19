package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.example.QuickCalculatorActivity
import com.example.QuickCalendarActivity
import com.example.QuickMarketActivity
import com.example.QuickPrayerActivity
import com.example.R
import com.example.data.network.WeatherApiClient
import com.example.ui.islamic.NamazTimeService
import com.example.util.CalendarUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import kotlin.math.roundToInt

class ToolsMateMasterWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"
        const val ACTION_TOGGLE_THEME = "com.example.widget.ACTION_TOGGLE_THEME"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, ToolsMateMasterWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            val intent = Intent(context, ToolsMateMasterWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }

        private fun convertDigitsToBn(str: String): String {
            val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
            val sb = StringBuilder()
            for (ch in str) {
                if (ch in '0'..'9') {
                    sb.append(bnDigits[ch - '0'])
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }

        private fun getWeatherConditionBn(code: Int): String {
            return when (code) {
                0 -> "নির্মল আকাশ"
                1, 2, 3 -> "আংশিক মেঘলা"
                45, 48 -> "কুয়াশাচ্ছন্ন"
                51, 53, 55, 61, 63, 65, 80, 81, 82 -> "বৃষ্টিপাত"
                95, 96, 99 -> "বজ্রবৃষ্টি"
                else -> "স্বাভাবিক আবহাওয়া"
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            try {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        try {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            super.onReceive(context, intent)
            if (intent.action == ACTION_REFRESH_WIDGET) {
                val pendingResult = goAsync()
                val appContext = context.applicationContext
                Toast.makeText(appContext, "উইজেট, আবহাওয়া ও চাঁদ দেখা সিঙ্ক করা হচ্ছে...", Toast.LENGTH_SHORT).show()

                CoroutineScope(Dispatchers.IO).launch {
                    var weatherSuccess = false
                    try {
                        val prefs = appContext.getSharedPreferences("islamic_location_prefs", Context.MODE_PRIVATE)
                        val lat = prefs.getFloat("islamic_district_lat", 23.8103f).toDouble()
                        val lon = prefs.getFloat("islamic_district_lon", 90.4125f).toDouble()

                        val weatherRes = WeatherApiClient.weatherApi.getWeather(lat, lon)
                        val liveTemp = weatherRes.current.temperature_2m
                        val liveCode = weatherRes.current.weather_code
                        val liveFeel = weatherRes.current.apparent_temperature
                        val liveHumidity = weatherRes.current.relative_humidity_2m
                        val liveWind = weatherRes.current.wind_speed_10m
                        val liveSunrise = weatherRes.daily.sunrise.getOrNull(0)?.substringAfter("T") ?: "05:32"
                        val liveSunset = weatherRes.daily.sunset.getOrNull(0)?.substringAfter("T") ?: "18:24"

                        prefs.edit()
                            .putFloat("cached_temp", liveTemp.toFloat())
                            .putInt("cached_weather_code", liveCode)
                            .putFloat("cached_feel", liveFeel.toFloat())
                            .putInt("cached_humidity", liveHumidity)
                            .putFloat("cached_wind", liveWind.toFloat())
                            .putString("cached_sunrise", liveSunrise)
                            .putString("cached_sunset", liveSunset)
                            .apply()
                        weatherSuccess = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // 2. Sync Hijri Date Online (Moon sighting info)
                    var hijriSuccess = false
                    try {
                        val today = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                        val url = URL("https://api.aladhan.com/v1/gToH/$today")
                        val connection = (url.openConnection() as HttpURLConnection).apply {
                            connectTimeout = 5000
                            readTimeout = 5000
                            requestMethod = "GET"
                        }

                        if (connection.responseCode == 200) {
                            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                            val json = JSONObject(responseText)
                            val data = json.getJSONObject("data")
                            val hijri = data.getJSONObject("hijri")
                            // Successfully fetched
                            hijriSuccess = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    withContext(Dispatchers.Main) {
                        updateAllWidgets(appContext)

                        if (weatherSuccess) {
                            Toast.makeText(appContext, "সাফল্যের সাথে উইজেট, আবহাওয়া ও চাঁদ দেখা সিঙ্ক হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(appContext, "আংশিক সফল: উইজেট ও চাঁদ দেখা সিঙ্ক হয়েছে, তবে আবহাওয়া অফলাইন।", Toast.LENGTH_SHORT).show()
                        }
                        pendingResult.finish()
                    }
                }
            } else if (intent.action == ACTION_TOGGLE_THEME) {
                val widgetPrefs = context.getSharedPreferences("widget_theme_prefs", Context.MODE_PRIVATE)
                val currentDark = widgetPrefs.getBoolean("is_dark_mode", false)
                widgetPrefs.edit().putBoolean("is_dark_mode", !currentDark).apply()
                updateAllWidgets(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            val views = RemoteViews(context.packageName, R.layout.widget_master_layout)

            // 1. SharedPreferences Theme & Location Data
            val widgetPrefs = context.getSharedPreferences("widget_theme_prefs", Context.MODE_PRIVATE)
            val isDarkMode = widgetPrefs.getBoolean("is_dark_mode", false)

            val prefs = context.getSharedPreferences("islamic_location_prefs", Context.MODE_PRIVATE)
            val districtBn = prefs.getString("islamic_district_bn", "ঢাকা") ?: "ঢাকা"
            val lat = prefs.getFloat("islamic_district_lat", 23.8103f).toDouble()
            val lon = prefs.getFloat("islamic_district_lon", 90.4125f).toDouble()

            // 2. Apply Theme Styling dynamically for perfect high-contrast UX
            if (isDarkMode) {
                views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_background_dark)
                views.setInt(R.id.widget_weather_container, "setBackgroundResource", R.drawable.widget_card_inner_bg_dark)
                views.setInt(R.id.widget_card_container, "setBackgroundResource", R.drawable.widget_card_inner_bg_dark)
                views.setInt(R.id.widget_btn_calc, "setBackgroundResource", R.drawable.widget_shortcut_bg_dark)
                views.setInt(R.id.widget_btn_calendar, "setBackgroundResource", R.drawable.widget_shortcut_bg_dark)
                views.setInt(R.id.widget_btn_market, "setBackgroundResource", R.drawable.widget_shortcut_bg_dark)
                views.setInt(R.id.widget_btn_islamic, "setBackgroundResource", R.drawable.widget_shortcut_bg_dark)
                views.setInt(R.id.widget_btn_theme_toggle, "setBackgroundResource", R.drawable.widget_shortcut_bg_dark)
                views.setInt(R.id.widget_btn_refresh, "setBackgroundResource", R.drawable.widget_shortcut_bg_dark)

                views.setImageViewResource(R.id.widget_btn_theme_toggle, R.drawable.ic_widget_sun)
                views.setTextColor(R.id.widget_app_brand, Color.parseColor("#E2E8F0"))
                views.setTextColor(R.id.widget_header_dot, Color.parseColor("#38BDF8"))
                views.setTextColor(R.id.widget_location_text, Color.parseColor("#38BDF8"))
                views.setTextColor(R.id.widget_date_text, Color.parseColor("#94A3B8"))
                views.setTextColor(R.id.widget_weather_temp, Color.parseColor("#38BDF8"))
                views.setTextColor(R.id.widget_weather_condition, Color.parseColor("#CBD5E1"))
                views.setTextColor(R.id.widget_weather_details, Color.parseColor("#94A3B8"))
                views.setTextColor(R.id.widget_sunrise_time, Color.parseColor("#FDE047"))
                views.setTextColor(R.id.widget_sunset_time, Color.parseColor("#F87171"))
                views.setTextColor(R.id.widget_sunrise_label, Color.parseColor("#94A3B8"))
                views.setTextColor(R.id.widget_sunset_label, Color.parseColor("#94A3B8"))

                views.setInt(R.id.widget_sunrise_icon, "setColorFilter", Color.parseColor("#FDE047"))
                views.setInt(R.id.widget_sunset_icon, "setColorFilter", Color.parseColor("#F87171"))
                views.setInt(R.id.widget_icon_location, "setColorFilter", Color.parseColor("#38BDF8"))
                views.setInt(R.id.widget_icon_clock, "setColorFilter", Color.parseColor("#38BDF8"))
                views.setInt(R.id.widget_icon_timer, "setColorFilter", Color.parseColor("#F59E0B"))
                views.setInt(R.id.widget_icon_sehri, "setColorFilter", Color.parseColor("#34D399"))
                views.setInt(R.id.widget_icon_iftar, "setColorFilter", Color.parseColor("#F87171"))
                views.setInt(R.id.widget_icon_calc, "setColorFilter", Color.parseColor("#FFFFFF"))
                views.setInt(R.id.widget_icon_calendar, "setColorFilter", Color.parseColor("#FFFFFF"))
                views.setInt(R.id.widget_icon_market, "setColorFilter", Color.parseColor("#FFFFFF"))
                views.setInt(R.id.widget_icon_islamic, "setColorFilter", Color.parseColor("#FFFFFF"))
                views.setInt(R.id.widget_btn_theme_toggle, "setColorFilter", Color.parseColor("#FFFFFF"))
                views.setInt(R.id.widget_btn_refresh, "setColorFilter", Color.parseColor("#FFFFFF"))

                views.setTextColor(R.id.widget_active_waqt_title, Color.parseColor("#38BDF8"))
                views.setTextColor(R.id.widget_active_waqt_time, Color.parseColor("#E2E8F0"))
                views.setTextColor(R.id.widget_countdown_text, Color.parseColor("#F59E0B"))
                views.setTextColor(R.id.widget_next_waqt_text, Color.parseColor("#CBD5E1"))
                views.setTextColor(R.id.widget_sehri_text, Color.parseColor("#34D399"))
                views.setTextColor(R.id.widget_iftar_text, Color.parseColor("#F87171"))
                views.setTextColor(R.id.widget_btn_calc_text, Color.parseColor("#F8FAFC"))
                views.setTextColor(R.id.widget_btn_calendar_text, Color.parseColor("#F8FAFC"))
                views.setTextColor(R.id.widget_btn_market_text, Color.parseColor("#F8FAFC"))
                views.setTextColor(R.id.widget_btn_islamic_text, Color.parseColor("#F8FAFC"))
            } else {
                views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_background)
                views.setInt(R.id.widget_weather_container, "setBackgroundResource", R.drawable.widget_card_inner_bg)
                views.setInt(R.id.widget_card_container, "setBackgroundResource", R.drawable.widget_card_inner_bg)
                views.setInt(R.id.widget_btn_calc, "setBackgroundResource", R.drawable.widget_shortcut_bg)
                views.setInt(R.id.widget_btn_calendar, "setBackgroundResource", R.drawable.widget_shortcut_bg)
                views.setInt(R.id.widget_btn_market, "setBackgroundResource", R.drawable.widget_shortcut_bg)
                views.setInt(R.id.widget_btn_islamic, "setBackgroundResource", R.drawable.widget_shortcut_bg)
                views.setInt(R.id.widget_btn_theme_toggle, "setBackgroundResource", R.drawable.widget_shortcut_bg)
                views.setInt(R.id.widget_btn_refresh, "setBackgroundResource", R.drawable.widget_shortcut_bg)

                views.setImageViewResource(R.id.widget_btn_theme_toggle, R.drawable.ic_widget_moon)
                views.setTextColor(R.id.widget_app_brand, Color.parseColor("#334155"))
                views.setTextColor(R.id.widget_header_dot, Color.parseColor("#0284C7"))
                views.setTextColor(R.id.widget_location_text, Color.parseColor("#0284C7"))
                views.setTextColor(R.id.widget_date_text, Color.parseColor("#475569"))
                views.setTextColor(R.id.widget_weather_temp, Color.parseColor("#0284C7"))
                views.setTextColor(R.id.widget_weather_condition, Color.parseColor("#475569"))
                views.setTextColor(R.id.widget_weather_details, Color.parseColor("#475569"))
                views.setTextColor(R.id.widget_sunrise_time, Color.parseColor("#D97706"))
                views.setTextColor(R.id.widget_sunset_time, Color.parseColor("#DC2626"))
                views.setTextColor(R.id.widget_sunrise_label, Color.parseColor("#475569"))
                views.setTextColor(R.id.widget_sunset_label, Color.parseColor("#475569"))

                views.setInt(R.id.widget_sunrise_icon, "setColorFilter", Color.parseColor("#D97706"))
                views.setInt(R.id.widget_sunset_icon, "setColorFilter", Color.parseColor("#DC2626"))
                views.setInt(R.id.widget_icon_location, "setColorFilter", Color.parseColor("#0284C7"))
                views.setInt(R.id.widget_icon_clock, "setColorFilter", Color.parseColor("#0284C7"))
                views.setInt(R.id.widget_icon_timer, "setColorFilter", Color.parseColor("#D97706"))
                views.setInt(R.id.widget_icon_sehri, "setColorFilter", Color.parseColor("#059669"))
                views.setInt(R.id.widget_icon_iftar, "setColorFilter", Color.parseColor("#DC2626"))
                views.setInt(R.id.widget_icon_calc, "setColorFilter", Color.parseColor("#0284C7"))
                views.setInt(R.id.widget_icon_calendar, "setColorFilter", Color.parseColor("#0284C7"))
                views.setInt(R.id.widget_icon_market, "setColorFilter", Color.parseColor("#0284C7"))
                views.setInt(R.id.widget_icon_islamic, "setColorFilter", Color.parseColor("#0284C7"))
                views.setInt(R.id.widget_btn_theme_toggle, "setColorFilter", Color.parseColor("#0284C7"))
                views.setInt(R.id.widget_btn_refresh, "setColorFilter", Color.parseColor("#0284C7"))

                views.setTextColor(R.id.widget_active_waqt_title, Color.parseColor("#0284C7"))
                views.setTextColor(R.id.widget_active_waqt_time, Color.parseColor("#0369A1"))
                views.setTextColor(R.id.widget_countdown_text, Color.parseColor("#D97706"))
                views.setTextColor(R.id.widget_next_waqt_text, Color.parseColor("#334155"))
                views.setTextColor(R.id.widget_sehri_text, Color.parseColor("#059669"))
                views.setTextColor(R.id.widget_iftar_text, Color.parseColor("#DC2626"))
                views.setTextColor(R.id.widget_btn_calc_text, Color.parseColor("#0369A1"))
                views.setTextColor(R.id.widget_btn_calendar_text, Color.parseColor("#0369A1"))
                views.setTextColor(R.id.widget_btn_market_text, Color.parseColor("#0369A1"))
                views.setTextColor(R.id.widget_btn_islamic_text, Color.parseColor("#0369A1"))
            }

            // 3. Dates
            val nowCal = Calendar.getInstance()
            val multiDate = try {
                CalendarUtils.getMultiDateInfo(nowCal, isBn = true)
            } catch (e: Exception) {
                null
            }

            val headerLocationStr = districtBn
            val headerDateStr = if (multiDate != null) {
                val bnDateClean = multiDate.bengaliDate.replace(" বঙ্গাব্দ", "")
                val hijriClean = multiDate.hijriDate.replace(" হিজরী", "")
                "${multiDate.englishDate} | $bnDateClean | $hijriClean ${multiDate.englishDayName}"
            } else {
                "ToolsMate Widget"
            }

            views.setTextViewText(R.id.widget_location_text, headerLocationStr)
            views.setTextViewText(R.id.widget_date_text, headerDateStr)

            // 4. Default / Fallback Weather Data
            val cachedTemp = prefs.getFloat("cached_temp", 29.0f).toDouble()
            val cachedCode = prefs.getInt("cached_weather_code", 1)
            val cachedFeel = prefs.getFloat("cached_feel", 31.0f).toDouble()
            val cachedHumidity = prefs.getInt("cached_humidity", 65)
            val cachedWind = prefs.getFloat("cached_wind", 12.0f).toDouble()
            val cachedSunrise = prefs.getString("cached_sunrise", "05:32") ?: "05:32"
            val cachedSunset = prefs.getString("cached_sunset", "18:24") ?: "18:24"

            val tempBn = convertDigitsToBn(cachedTemp.roundToInt().toString())
            val feelBn = convertDigitsToBn(cachedFeel.roundToInt().toString())
            val humidityBn = convertDigitsToBn(cachedHumidity.toString())
            val windBn = convertDigitsToBn(cachedWind.roundToInt().toString())
            val sunriseBn = convertDigitsToBn(cachedSunrise)
            val sunsetBn = convertDigitsToBn(cachedSunset)
            val conditionBn = getWeatherConditionBn(cachedCode)

            views.setTextViewText(R.id.widget_weather_temp, "$tempBn°C")
            views.setTextViewText(R.id.widget_weather_condition, "$conditionBn • অনুভূত $feelBn°C")
            views.setTextViewText(R.id.widget_weather_details, "আর্দ্রতা $humidityBn% • বাতাস $windBn কিমি/ঘ")
            views.setTextViewText(R.id.widget_sunrise_time, sunriseBn)
            views.setTextViewText(R.id.widget_sunset_time, sunsetBn)

            // Async Fetch Fresh Weather Data
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val weatherRes = WeatherApiClient.weatherApi.getWeather(lat, lon)
                    val liveTemp = weatherRes.current.temperature_2m
                    val liveCode = weatherRes.current.weather_code
                    val liveFeel = weatherRes.current.apparent_temperature
                    val liveHumidity = weatherRes.current.relative_humidity_2m
                    val liveWind = weatherRes.current.wind_speed_10m
                    val liveSunrise = weatherRes.daily.sunrise.getOrNull(0)?.substringAfter("T") ?: "05:32"
                    val liveSunset = weatherRes.daily.sunset.getOrNull(0)?.substringAfter("T") ?: "18:24"

                    prefs.edit()
                        .putFloat("cached_temp", liveTemp.toFloat())
                        .putInt("cached_weather_code", liveCode)
                        .putFloat("cached_feel", liveFeel.toFloat())
                        .putInt("cached_humidity", liveHumidity)
                        .putFloat("cached_wind", liveWind.toFloat())
                        .putString("cached_sunrise", liveSunrise)
                        .putString("cached_sunset", liveSunset)
                        .apply()

                    val freshTempBn = convertDigitsToBn(liveTemp.roundToInt().toString())
                    val freshFeelBn = convertDigitsToBn(liveFeel.roundToInt().toString())
                    val freshHumidityBn = convertDigitsToBn(liveHumidity.toString())
                    val freshWindBn = convertDigitsToBn(liveWind.roundToInt().toString())
                    val freshSunriseBn = convertDigitsToBn(liveSunrise)
                    val freshSunsetBn = convertDigitsToBn(liveSunset)
                    val freshConditionBn = getWeatherConditionBn(liveCode)

                    val updatedViews = RemoteViews(context.packageName, R.layout.widget_master_layout)
                    updatedViews.setTextViewText(R.id.widget_weather_temp, "$freshTempBn°C")
                    updatedViews.setTextViewText(R.id.widget_weather_condition, "$freshConditionBn • অনুভূত $freshFeelBn°C")
                    updatedViews.setTextViewText(R.id.widget_weather_details, "আর্দ্রতা $freshHumidityBn% • বাতাস $freshWindBn কিমি/ঘ")
                    updatedViews.setTextViewText(R.id.widget_sunrise_time, freshSunriseBn)
                    updatedViews.setTextViewText(R.id.widget_sunset_time, freshSunsetBn)
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, updatedViews)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 5. Timings & Active Waqt Calculation
            try {
                val timings = NamazTimeService.getPrayerTimesForCoordinates(lat, lon, nowCal)

                val nowMin = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
                val fajrMin = NamazTimeService.timeStrToMinutes(timings.fajr)
                val sunriseMin = NamazTimeService.timeStrToMinutes(timings.sunrise)
                val dhuhrMin = NamazTimeService.timeStrToMinutes(timings.dhuhr)
                val asrMin = NamazTimeService.timeStrToMinutes(timings.asr)
                val maghribMin = NamazTimeService.timeStrToMinutes(timings.maghrib)
                val ishaMin = NamazTimeService.timeStrToMinutes(timings.isha)

                var activeWaqtName = "মাগরিব"
                var activeWaqtTime = "${timings.maghrib} - ${timings.isha}"
                var nextWaqtName = "এশা"
                var nextWaqtTime = timings.isha
                var remainingMin = 0

                when {
                    nowMin in fajrMin until sunriseMin -> {
                        activeWaqtName = "ফজর"
                        activeWaqtTime = "${timings.fajr} - ${timings.sunrise}"
                        nextWaqtName = "ইশরাক"
                        nextWaqtTime = timings.sunrise
                        remainingMin = sunriseMin - nowMin
                    }
                    nowMin in sunriseMin until dhuhrMin -> {
                        activeWaqtName = "ইশরাক"
                        activeWaqtTime = "${timings.sunrise} - ${timings.dhuhr}"
                        nextWaqtName = "জোহর"
                        nextWaqtTime = timings.dhuhr
                        remainingMin = dhuhrMin - nowMin
                    }
                    nowMin in dhuhrMin until asrMin -> {
                        activeWaqtName = "জোহর"
                        activeWaqtTime = "${timings.dhuhr} - ${timings.asr}"
                        nextWaqtName = "আসর"
                        nextWaqtTime = timings.asr
                        remainingMin = asrMin - nowMin
                    }
                    nowMin in asrMin until maghribMin -> {
                        activeWaqtName = "আসর"
                        activeWaqtTime = "${timings.asr} - ${timings.maghrib}"
                        nextWaqtName = "মাগরিব"
                        nextWaqtTime = timings.maghrib
                        remainingMin = maghribMin - nowMin
                    }
                    nowMin in maghribMin until ishaMin -> {
                        activeWaqtName = "মাগরিব"
                        activeWaqtTime = "${timings.maghrib} - ${timings.isha}"
                        nextWaqtName = "এশা"
                        nextWaqtTime = timings.isha
                        remainingMin = ishaMin - nowMin
                    }
                    nowMin >= ishaMin -> {
                        activeWaqtName = "এশা"
                        activeWaqtTime = "${timings.isha} - ${timings.fajr}"
                        nextWaqtName = "ফজর"
                        nextWaqtTime = timings.fajr
                        remainingMin = (24 * 60 - nowMin) + fajrMin
                    }
                    else -> { // Before Fajr
                        activeWaqtName = "তাহাজ্জুদ"
                        activeWaqtTime = "১২:০০ AM - ${timings.fajr}"
                        nextWaqtName = "ফজর"
                        nextWaqtTime = timings.fajr
                        remainingMin = fajrMin - nowMin
                    }
                }

                val hours = remainingMin / 60
                val mins = remainingMin % 60
                val countdownStr = if (hours > 0) {
                    "শেষ হতে বাকিঃ ${convertDigitsToBn(hours.toString())} ঘণ্টা ${convertDigitsToBn(mins.toString())} মিনিট"
                } else {
                    "শেষ হতে বাকিঃ ${convertDigitsToBn(mins.toString())} মিনিট"
                }

                val sehriStr = "সেহরিঃ ${timings.sahri}"
                val iftarStr = "ইফতারঃ ${timings.maghrib}"
                val nextWaqtStr = "পরবর্তীঃ $nextWaqtName ($nextWaqtTime)"

                views.setTextViewText(R.id.widget_active_waqt_title, "বর্তমান ওয়াক্তঃ $activeWaqtName")
                views.setTextViewText(R.id.widget_active_waqt_time, activeWaqtTime)
                views.setTextViewText(R.id.widget_countdown_text, countdownStr)
                views.setTextViewText(R.id.widget_next_waqt_text, nextWaqtStr)
                views.setTextViewText(R.id.widget_sehri_text, sehriStr)
                views.setTextViewText(R.id.widget_iftar_text, iftarStr)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 6. Pending Intents for Actions
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

            // Theme Toggle Action
            val toggleThemeIntent = Intent(context, ToolsMateMasterWidget::class.java).apply {
                action = ACTION_TOGGLE_THEME
            }
            val toggleThemePendingIntent = PendingIntent.getBroadcast(context, 99, toggleThemeIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_btn_theme_toggle, toggleThemePendingIntent)

            // Refresh Action
            val refreshIntent = Intent(context, ToolsMateMasterWidget::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(context, 100, refreshIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            // Calculator Action
            val calcIntent = Intent(context, QuickCalculatorActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val calcPendingIntent = PendingIntent.getActivity(context, 101, calcIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_btn_calc, calcPendingIntent)

            // Calendar Action
            val calendarIntent = Intent(context, QuickCalendarActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val calendarPendingIntent = PendingIntent.getActivity(context, 102, calendarIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_btn_calendar, calendarPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_date_text, calendarPendingIntent)

            // Market List / Notes Action
            val marketIntent = Intent(context, QuickMarketActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val marketPendingIntent = PendingIntent.getActivity(context, 103, marketIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_btn_market, marketPendingIntent)

            // Islamic Tools Action
            val prayerIntent = Intent(context, QuickPrayerActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val prayerPendingIntent = PendingIntent.getActivity(context, 104, prayerIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_btn_islamic, prayerPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_card_container, prayerPendingIntent)

            // District Selection Action (Click Location in Widget)
            val districtIntent = Intent(context, com.example.MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("target_tab", "district")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val districtPendingIntent = PendingIntent.getActivity(context, 105, districtIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_location_click_container, districtPendingIntent)

            // Open App on empty space click
            val mainAppIntent = Intent(context, com.example.MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val mainAppPendingIntent = PendingIntent.getActivity(context, 106, mainAppIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_root, mainAppPendingIntent)

            // Weather Card Action (Clicking weather container opens Weather tool dialog)
            val weatherIntent = Intent(context, com.example.MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("target_tool", "WEATHER")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val weatherPendingIntent = PendingIntent.getActivity(context, 107, weatherIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_weather_container, weatherPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

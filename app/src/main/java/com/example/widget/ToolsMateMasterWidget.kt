package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.QuickCalculatorActivity
import com.example.QuickMarketActivity
import com.example.QuickPrayerActivity
import com.example.QuickQuranActivity
import com.example.R
import com.example.data.model.ToolType
import com.example.ui.islamic.NamazTimeService
import com.example.util.CalendarUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ToolsMateMasterWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"

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
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, ToolsMateMasterWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_master_layout)

        // 1. SharedPreferences Location Data
        val prefs = context.getSharedPreferences("islamic_district_prefs", Context.MODE_PRIVATE)
        val districtBn = prefs.getString("islamic_district_bn", "ঢাকা") ?: "ঢাকা"
        val lat = prefs.getFloat("islamic_district_lat", 23.8103f).toDouble()
        val lon = prefs.getFloat("islamic_district_lon", 90.4125f).toDouble()

        // 2. Dates
        val nowCal = Calendar.getInstance()
        val multiDate = CalendarUtils.getMultiDateInfo(nowCal, isBn = true)

        val headerLocationStr = "📍 $districtBn • ToolsMate"
        val headerDateStr = "${multiDate.englishDate} | ${multiDate.hijriDate.replace(" হিজরী", "")}"

        views.setTextViewText(R.id.widget_location_text, headerLocationStr)
        views.setTextViewText(R.id.widget_date_text, headerDateStr)

        // 3. Timings & Active Waqt Calculation
        try {
            val timings = NamazTimeService.getPrayerTimesForCoordinates(lat, lon, nowCal)

            val nowMin = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
            val fajrMin = NamazTimeService.timeStrToMinutes(timings.fajr)
            val sunriseMin = NamazTimeService.timeStrToMinutes(timings.sunrise)
            val dhuhrMin = NamazTimeService.timeStrToMinutes(timings.dhuhr)
            val asrMin = NamazTimeService.timeStrToMinutes(timings.asr)
            val maghribMin = NamazTimeService.timeStrToMinutes(timings.maghrib)
            val ishaMin = NamazTimeService.timeStrToMinutes(timings.isha)

            var activeWaqtName = "তাহাজ্জুদ / এশা"
            var activeWaqtTime = "${timings.isha} - ${timings.fajr}"
            var remainingMin = 0

            when {
                nowMin in fajrMin until sunriseMin -> {
                    activeWaqtName = "ফজর"
                    activeWaqtTime = "${timings.fajr} - ${timings.sunrise}"
                    remainingMin = sunriseMin - nowMin
                }
                nowMin in sunriseMin until dhuhrMin -> {
                    activeWaqtName = "সূর্যোদয় / ইশরাক"
                    activeWaqtTime = "${timings.sunrise} - ${timings.dhuhr}"
                    remainingMin = dhuhrMin - nowMin
                }
                nowMin in dhuhrMin until asrMin -> {
                    activeWaqtName = "জোহর"
                    activeWaqtTime = "${timings.dhuhr} - ${timings.asr}"
                    remainingMin = asrMin - nowMin
                }
                nowMin in asrMin until maghribMin -> {
                    activeWaqtName = "আসর"
                    activeWaqtTime = "${timings.asr} - ${timings.maghrib}"
                    remainingMin = maghribMin - nowMin
                }
                nowMin in maghribMin until ishaMin -> {
                    activeWaqtName = "মাগরিব"
                    activeWaqtTime = "${timings.maghrib} - ${timings.isha}"
                    remainingMin = ishaMin - nowMin
                }
                nowMin >= ishaMin -> {
                    activeWaqtName = "এশা"
                    activeWaqtTime = "${timings.isha} - ${timings.fajr}"
                    remainingMin = (24 * 60 - nowMin) + fajrMin
                }
                else -> { // Before Fajr
                    activeWaqtName = "তাহাজ্জুদ"
                    activeWaqtTime = "১২:০০ AM - ${timings.fajr}"
                    remainingMin = fajrMin - nowMin
                }
            }

            val hours = remainingMin / 60
            val mins = remainingMin % 60
            val countdownStr = "⏳ শেষ হতে বাকি: ${convertDigitsToBn(String.format(Locale.ENGLISH, "%02d", hours))}ঘণ্টা ${convertDigitsToBn(String.format(Locale.ENGLISH, "%02d", mins))}মি"
            val sehriIftarStr = "🌅 সেহরি: ${timings.sahri}  |  🌇 ইফতার: ${timings.maghrib}"

            views.setTextViewText(R.id.widget_active_waqt_title, "🕌 বর্তমান ওয়াক্ত: $activeWaqtName")
            views.setTextViewText(R.id.widget_active_waqt_time, activeWaqtTime)
            views.setTextViewText(R.id.widget_countdown_text, countdownStr)
            views.setTextViewText(R.id.widget_sehri_iftar_text, sehriIftarStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Pending Intents for Quick Actions
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

        // Refresh Action
        val refreshIntent = Intent(context, ToolsMateMasterWidget::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(context, 100, refreshIntent, flags)
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        // Prayer Times Action
        val prayerIntent = Intent(context, QuickPrayerActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val prayerPendingIntent = PendingIntent.getActivity(context, 101, prayerIntent, flags)
        views.setOnClickPendingIntent(R.id.widget_btn_prayer, prayerPendingIntent)

        // Quran Action
        val quranIntent = Intent(context, QuickQuranActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val quranPendingIntent = PendingIntent.getActivity(context, 102, quranIntent, flags)
        views.setOnClickPendingIntent(R.id.widget_btn_quran, quranPendingIntent)

        // Digital Tasbih Action
        val tasbihIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("target_tool", ToolType.DIGITAL_TASBIH.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val tasbihPendingIntent = PendingIntent.getActivity(context, 103, tasbihIntent, flags)
        views.setOnClickPendingIntent(R.id.widget_btn_tasbih, tasbihPendingIntent)

        // Notes Action
        val notesIntent = Intent(context, QuickMarketActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val notesPendingIntent = PendingIntent.getActivity(context, 104, notesIntent, flags)
        views.setOnClickPendingIntent(R.id.widget_btn_notes, notesPendingIntent)

        // Calculator Action
        val calcIntent = Intent(context, QuickCalculatorActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val calcPendingIntent = PendingIntent.getActivity(context, 105, calcIntent, flags)
        views.setOnClickPendingIntent(R.id.widget_btn_calc, calcPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

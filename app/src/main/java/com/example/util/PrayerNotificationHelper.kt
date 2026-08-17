package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ui.islamic.NamazTimeService
import com.example.ui.islamic.PrayerTimings
import java.text.SimpleDateFormat
import java.util.*

object PrayerNotificationHelper {

    private const val CHANNEL_ID = "islamic_tools_alerts"
    private const val CHANNEL_NAME = "Islamic Tools Alerts"
    private const val CHANNEL_DESC = "Notifications for Prayer, Fasting, and Forbidden times"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun postNotification(context: Context, id: Int, title: String, message: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            id,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            } else {
                android.app.PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }

    fun checkAndTriggerNotifications(
        context: Context,
        timings: PrayerTimings,
        alertsMap: Map<String, Boolean>,
        isBn: Boolean
    ) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentMinuteOfDay = currentHour * 60 + currentMinute

        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
        val sharedPrefs = context.getSharedPreferences("prayer_notification_prefs", Context.MODE_PRIVATE)
        val lastNotifiedMin = sharedPrefs.getInt("${todayKey}_last_notified_min", -1)

        // Only trigger once per minute
        if (lastNotifiedMin == currentMinuteOfDay) return

        // Calculate key timestamps in minutes of the day
        val fajrMin = NamazTimeService.timeStrToMinutes(timings.fajr)
        val sunriseMin = NamazTimeService.timeStrToMinutes(timings.sunrise)
        val ishraqMin = sunriseMin + 15
        val zawaalMin = NamazTimeService.timeStrToMinutes(timings.dhuhr) - 15
        val dhuhrMin = NamazTimeService.timeStrToMinutes(timings.dhuhr)
        val asrMin = NamazTimeService.timeStrToMinutes(timings.asr)
        val sunsetForbiddenMin = NamazTimeService.timeStrToMinutes(timings.maghrib) - 15
        val maghribMin = NamazTimeService.timeStrToMinutes(timings.maghrib)
        val ishaMin = NamazTimeService.timeStrToMinutes(timings.isha)
        val sehriMin = NamazTimeService.timeStrToMinutes(timings.sahri)
        val sehriWarningMin = sehriMin - 10

        var title = ""
        var msg = ""
        var notificationId = 1000

        when (currentMinuteOfDay) {
            // 1. Five Daily Prayers (Fajr, Dhuhr, Asr, Maghrib, Isha)
            fajrMin -> {
                if (alertsMap["fajr"] != false) {
                    title = if (isBn) "ফজরের নামাজের সময়" else "Fajr Prayer Time"
                    msg = if (isBn) "ফজরের ওয়াক্ত শুরু হয়েছে। নামাজ আদায়ের প্রস্তুতি নিন।" else "Fajr time has started. Please prepare for prayers."
                    notificationId = 1001
                }
            }
            dhuhrMin -> {
                if (alertsMap["dhuhr"] != false) {
                    title = if (isBn) "যোহরের নামাজের সময়" else "Dhuhr Prayer Time"
                    msg = if (isBn) "যোহরের ওয়াক্ত শুরু হয়েছে। জামায়াতে নামাজ আদায়ের প্রস্তুতি নিন।" else "Dhuhr time has started. Please prepare for congregation."
                    notificationId = 1002
                }
            }
            asrMin -> {
                if (alertsMap["asr"] != false) {
                    title = if (isBn) "আছরের নামাজের সময়" else "Asr Prayer Time"
                    msg = if (isBn) "আছরের ওয়াক্ত শুরু হয়েছে। নামাজ আদায়ের প্রস্তুতি নিন।" else "Asr time has started. Please prepare for prayers."
                    notificationId = 1003
                }
            }
            maghribMin -> {
                if (alertsMap["maghrib"] != false) {
                    title = if (isBn) "মাগরিবের নামাজের সময় (ইফতার)" else "Maghrib Prayer Time (Iftar)"
                    msg = if (isBn) "মাগরিবের ওয়াক্ত ও ইফতারের সময় হয়েছে। নামাজ ও ইফতারের প্রস্তুতি নিন।" else "Maghrib time & Iftar time has started. Prepare for breaking fast."
                    notificationId = 1004
                }
            }
            ishaMin -> {
                if (alertsMap["isha"] != false) {
                    title = if (isBn) "এশার নামাজের সময়" else "Isha Prayer Time"
                    msg = if (isBn) "এশার ওয়াক্ত শুরু হয়েছে। নামাজ আদায়ের প্রস্তুতি নিন।" else "Isha time has started. Please prepare for prayers."
                    notificationId = 1005
                }
            }

            // 2. Salat Forbidden Times
            sunriseMin -> {
                title = if (isBn) "নামাজের নিষিদ্ধ সময় শুরু" else "Forbidden Prayer Time Started"
                msg = if (isBn) "সূর্যোদয় হচ্ছে। এখন সব ধরনের নামাজ আদায় করা নিষিদ্ধ।" else "Sunrise is in progress. Any prayers are strictly forbidden now."
                notificationId = 1010
            }
            ishraqMin -> {
                title = if (isBn) "নামাজের নিষিদ্ধ সময় শেষ" else "Forbidden Prayer Time Ended"
                msg = if (isBn) "সূর্যোদয় শেষ হয়েছে। এখন নফল বা ইশরাকের নামাজ আদায় করতে পারেন।" else "Sunrise has ended. You can perform Ishraq or Nafl prayers now."
                notificationId = 1011
            }
            zawaalMin -> {
                title = if (isBn) "নামাজের নিষিদ্ধ সময় (জাওয়াল)" else "Forbidden Prayer Time (Zawaal)"
                msg = if (isBn) "সূর্য ঠিক মাথার উপর রয়েছে। এখন নামাজ আদায় করা নিষিদ্ধ।" else "The sun is directly overhead. Prayers are strictly forbidden now."
                notificationId = 1012
            }
            sunsetForbiddenMin -> {
                title = if (isBn) "নামাজের নিষিদ্ধ সময় শুরু (সূর্যাস্ত)" else "Forbidden Prayer Time (Sunset)"
                msg = if (isBn) "সূর্যাস্ত হতে যাচ্ছে। মাগরিবের ফরয ছাড়া অন্য সকল নামাজ এখন নিষিদ্ধ।" else "Sunset is approaching. All prayers except Maghrib Fard are forbidden."
                notificationId = 1013
            }

            // 3. Sehri warning and ending
            sehriWarningMin -> {
                title = if (isBn) "সেহরি শেষ হতে ১০ মিনিট বাকি" else "10 Minutes Left for Sehri"
                msg = if (isBn) "সেহরির শেষ সময় ঘনিয়ে এসেছে। দ্রুত শেষ করুন।" else "Sehri is ending in 10 minutes. Please finish your meal."
                notificationId = 1020
            }
            sehriMin -> {
                title = if (isBn) "সেহরির সময় শেষ" else "Sehri Time Ended"
                msg = if (isBn) "সেহরির শেষ সময় অতিবাহিত হয়েছে। সুবহে সাদিক শুরু হয়েছে।" else "Sehri time has ended. Fasting time begins."
                notificationId = 1021
            }
        }

        if (title.isNotEmpty() && msg.isNotEmpty()) {
            postNotification(context, notificationId, title, msg)
            sharedPrefs.edit().putInt("${todayKey}_last_notified_min", currentMinuteOfDay).apply()
        }
    }
}

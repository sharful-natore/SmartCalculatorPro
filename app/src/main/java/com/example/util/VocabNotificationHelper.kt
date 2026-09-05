package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ui.screens.tools.VocabWord
import com.example.ui.screens.tools.VocabularyDataProvider
import com.example.ui.screens.tools.VocabularyDataPacks
import java.util.Calendar

object VocabNotificationHelper {

    private const val CHANNEL_ID = "vocab_word_of_the_day"
    private const val CHANNEL_NAME = "Word of the Day"
    private const val CHANNEL_DESC = "Daily Vocabulary Building Notifications"
    private const val NOTIFICATION_ID = 8801

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerDailyWordNotificationIfNeeded(context: Context, word: VocabWord) {
        val prefs = context.getSharedPreferences("vocab_prefs", Context.MODE_PRIVATE)
        val todayCalendar = Calendar.getInstance()
        val currentDayOfYear = todayCalendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = todayCalendar.get(Calendar.YEAR)
        val todayKey = "$currentYear-$currentDayOfYear"

        val lastSentDay = prefs.getString("last_word_of_day_key", "")
        if (lastSentDay != todayKey) {
            postWordNotification(context, word)
            prefs.edit().putString("last_word_of_day_key", todayKey).apply()
        }

        scheduleDailyAlarm(context)
    }

    fun postWordNotification(context: Context, word: VocabWord) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val synsText = if (word.synonyms.isNotEmpty()) " • Synonyms: ${word.synonyms.take(2).joinToString(", ")}" else ""
        val title = "📌 Word of the Day: ${word.word} (${word.partOfSpeech})"
        val message = "${word.meaningBn}$synsText"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun scheduleDailyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, VocabDailyReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            8802,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class VocabDailyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val words = VocabularyDataProvider.getWordsForPacks(context, setOf("starter", "master_dictionary"))
            val randomWord = words.randomOrNull() ?: VocabularyDataPacks.starterWords.random()
            VocabNotificationHelper.postWordNotification(context, randomWord)
        }
    }
}

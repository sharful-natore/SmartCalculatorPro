package com.example.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.QuickCalculatorActivity
import com.example.QuickCalendarActivity
import com.example.QuickMarketActivity
import com.example.QuickPrayerActivity
import com.example.QuickQuranActivity
import com.example.R
import com.example.data.model.ConverterType
import com.example.data.model.ToolType

object ShortcutUtils {

    fun pinToolShortcut(context: Context, toolType: ToolType, isBn: Boolean) {
        val title = toolType.getTitle(if (isBn) AppLanguage.BENGALI else AppLanguage.ENGLISH)
        val targetActivityClass = when (toolType) {
            ToolType.MULTI_CALENDAR -> QuickCalendarActivity::class.java
            ToolType.NOTES_CHECKLIST -> QuickMarketActivity::class.java
            ToolType.SEHRI_IFTAR, ToolType.NAMAZ_EDUCATION, ToolType.PRAYER_TIMES -> QuickPrayerActivity::class.java
            ToolType.HOLY_QURAN -> QuickQuranActivity::class.java
            else -> MainActivity::class.java
        }
        val intent = Intent(context, targetActivityClass).apply {
            action = Intent.ACTION_VIEW
            if (targetActivityClass == MainActivity::class.java) {
                putExtra("target_tool", toolType.name)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val iconRes = when (toolType) {
            ToolType.MULTI_CALENDAR -> R.drawable.ic_shortcut_calendar
            ToolType.NOTES_CHECKLIST -> R.drawable.ic_shortcut_market
            ToolType.SEHRI_IFTAR, ToolType.NAMAZ_EDUCATION, ToolType.PRAYER_TIMES -> R.drawable.ic_shortcut_prayer
            ToolType.HOLY_QURAN, ToolType.ISLAMIC_DUAS -> R.drawable.ic_shortcut_converter
            else -> R.drawable.ic_shortcut_dashboard
        }
        val icon = getBitmapIcon(context, iconRes)
        createPinnedShortcut(context, "tool_${toolType.name}", title, intent, icon, isBn)
    }

    fun pinConverterShortcut(context: Context, converterType: ConverterType, isBn: Boolean) {
        val title = converterType.getTitle(if (isBn) AppLanguage.BENGALI else AppLanguage.ENGLISH)
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("target_converter", converterType.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val icon = getBitmapIcon(context, R.drawable.ic_shortcut_converter)
        createPinnedShortcut(context, "conv_${converterType.name}", title, intent, icon, isBn)
    }

    private fun getBitmapIcon(context: Context, @DrawableRes resId: Int): Icon {
        val drawable = ContextCompat.getDrawable(context, resId) ?: ContextCompat.getDrawable(context, R.drawable.app_logo)!!
        val bitmap = Bitmap.createBitmap(
            192, 192,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return Icon.createWithBitmap(bitmap)
    }

    private fun createPinnedShortcut(
        context: Context,
        id: String,
        title: String,
        intent: Intent,
        icon: Icon,
        isBn: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                val pinShortcutInfo = ShortcutInfo.Builder(context, id)
                    .setShortLabel(title)
                    .setLongLabel(title)
                    .setIcon(icon)
                    .setIntent(intent)
                    .build()

                val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)
                val successPendingIntent = PendingIntent.getBroadcast(
                    context, 0, pinnedShortcutCallbackIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                shortcutManager.requestPinShortcut(pinShortcutInfo, successPendingIntent.intentSender)
                Toast.makeText(
                    context,
                    if (isBn) "'$title' হোমস্ক্রিনে শর্টকাট হিসেবে যোগ করার অনুরোধ পাঠানো হয়েছে" else "Request sent to add '$title' shortcut to Home Screen",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    if (isBn) "আপনার লঞ্চার পিন শর্টকাট সাপোর্ট করে না" else "Pin shortcuts are not supported by your launcher",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                if (isBn) "হোমস্ক্রিন শর্টকাট ফিচারটি অ্যান্ড্রয়েড ৮.০ বা তার পরের ভার্সনে উপলব্ধ" else "Home screen shortcuts require Android 8.0+",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun updateDynamicShortcuts(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
            val isBn = context.resources.configuration.locales[0].language == "bn"

            val shortcuts = listOf(
                ShortcutInfo.Builder(context, "shortcut_calculator")
                    .setShortLabel("Calculator")
                    .setLongLabel("Calculator")
                    .setIcon(getBitmapIcon(context, R.drawable.ic_shortcut_calculator))
                    .setIntent(Intent(context, QuickCalculatorActivity::class.java).apply { action = Intent.ACTION_VIEW })
                    .build(),
                ShortcutInfo.Builder(context, "shortcut_calendar")
                    .setShortLabel("Calendar")
                    .setLongLabel("Calendar")
                    .setIcon(getBitmapIcon(context, R.drawable.ic_shortcut_calendar))
                    .setIntent(Intent(context, QuickCalendarActivity::class.java).apply { action = Intent.ACTION_VIEW })
                    .build(),
                ShortcutInfo.Builder(context, "shortcut_market")
                    .setShortLabel("Shopping List")
                    .setLongLabel("Shopping List")
                    .setIcon(getBitmapIcon(context, R.drawable.ic_shortcut_market))
                    .setIntent(Intent(context, QuickMarketActivity::class.java).apply { action = Intent.ACTION_VIEW })
                    .build(),
                ShortcutInfo.Builder(context, "shortcut_prayer")
                    .setShortLabel("Islamic Tools")
                    .setLongLabel("Islamic Tools")
                    .setIcon(getBitmapIcon(context, R.drawable.ic_shortcut_prayer))
                    .setIntent(Intent(context, QuickPrayerActivity::class.java).apply { action = Intent.ACTION_VIEW })
                    .build()
            )

            try {
                shortcutManager.dynamicShortcuts = shortcuts
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

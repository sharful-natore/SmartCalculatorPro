package com.example.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import com.example.data.model.ConverterType
import com.example.data.model.ToolType

object ShortcutUtils {

    fun pinToolShortcut(context: Context, toolType: ToolType, isBn: Boolean) {
        val title = toolType.getTitle(if (isBn) AppLanguage.BENGALI else AppLanguage.ENGLISH)
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("target_tool", toolType.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val iconRes = R.drawable.app_logo
        val icon = Icon.createWithResource(context, iconRes)
        createPinnedShortcut(context, "tool_${toolType.name}", title, intent, icon, isBn)
    }

    fun pinConverterShortcut(context: Context, converterType: ConverterType, isBn: Boolean) {
        val title = converterType.getTitle(if (isBn) AppLanguage.BENGALI else AppLanguage.ENGLISH)
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("target_converter", converterType.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val icon = Icon.createWithResource(context, R.drawable.ic_shortcut_converter)
        createPinnedShortcut(context, "conv_${converterType.name}", title, intent, icon, isBn)
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
}

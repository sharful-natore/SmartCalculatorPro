package com.example.util

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CrashReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long,
    val formattedTime: String,
    val exceptionType: String,
    val errorMessage: String,
    val stackTrace: String,
    val deviceManufacturer: String,
    val deviceModel: String,
    val androidVersion: String,
    val sdkInt: Int,
    val appVersionName: String,
    val appVersionCode: Long,
    val screenContext: String = ""
) {
    fun toFormattedReport(): String {
        return buildString {
            appendLine("=== CALCULATOR & SMART TOOLS CRASH REPORT ===")
            appendLine("Date/Time: $formattedTime")
            appendLine("App Version: $appVersionName ($appVersionCode)")
            appendLine("Device: $deviceManufacturer $deviceModel (API $sdkInt, Android $androidVersion)")
            if (screenContext.isNotBlank()) {
                appendLine("Active Screen/Tool: $screenContext")
            }
            appendLine("Exception: $exceptionType")
            appendLine("Message: $errorMessage")
            appendLine()
            appendLine("--- STACK TRACE ---")
            appendLine(stackTrace)
            appendLine("=============================================")
        }
    }
}

object CrashReporter {
    private const val PREFS_NAME = "app_crash_reporter_prefs"
    private const val KEY_HAS_PENDING = "has_pending_crash"
    private const val KEY_TIMESTAMP = "crash_timestamp"
    private const val KEY_TYPE = "crash_type"
    private const val KEY_MESSAGE = "crash_message"
    private const val KEY_STACKTRACE = "crash_stacktrace"
    private const val KEY_SCREEN = "crash_screen"
    private const val KEY_ALL_LOGS = "crash_recent_history"

    const val DEVELOPER_EMAIL = "shorifbd24@gmail.com"

    @Volatile
    var currentActiveScreen: String = "Dashboard"

    fun install(application: Application) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                recordCrash(application, throwable, currentActiveScreen)
            } catch (e: Throwable) {
                Log.e("CrashReporter", "Failed to record crash", e)
            }

            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(10)
            }
        }
    }

    fun recordCrash(context: Context, throwable: Throwable, screen: String = currentActiveScreen) {
        try {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            val stackTrace = sw.toString()

            val now = System.currentTimeMillis()
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(now))

            val pInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
            } catch (_: Exception) {
                null
            }

            val versionName = pInfo?.versionName ?: "1.0.0"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo?.longVersionCode ?: 1L
            } else {
                @Suppress("DEPRECATION")
                (pInfo?.versionCode?.toLong() ?: 1L)
            }

            val report = CrashReport(
                timestamp = now,
                formattedTime = timeStr,
                exceptionType = throwable.javaClass.name,
                errorMessage = throwable.localizedMessage ?: (throwable.message ?: "No error message"),
                stackTrace = stackTrace,
                deviceManufacturer = Build.MANUFACTURER,
                deviceModel = Build.MODEL,
                androidVersion = Build.VERSION.RELEASE ?: "Unknown",
                sdkInt = Build.VERSION.SDK_INT,
                appVersionName = versionName,
                appVersionCode = versionCode,
                screenContext = screen
            )

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_HAS_PENDING, true)
                .putLong(KEY_TIMESTAMP, now)
                .putString(KEY_TYPE, report.exceptionType)
                .putString(KEY_MESSAGE, report.errorMessage)
                .putString(KEY_STACKTRACE, report.stackTrace)
                .putString(KEY_SCREEN, report.screenContext)
                .commit()

            // Also append to history
            val existingHistory = prefs.getString(KEY_ALL_LOGS, "") ?: ""
            val newEntry = "[$timeStr | $screen] ${report.exceptionType}: ${report.errorMessage}"
            val updatedHistory = (newEntry + "\n" + existingHistory).take(4000)
            prefs.edit().putString(KEY_ALL_LOGS, updatedHistory).apply()

        } catch (e: Exception) {
            Log.e("CrashReporter", "Error in recordCrash", e)
        }
    }

    fun logHandledException(context: Context, tag: String, throwable: Throwable) {
        try {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            val stackTrace = sw.toString()
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existingHistory = prefs.getString(KEY_ALL_LOGS, "") ?: ""
            val newEntry = "[$timeStr | Handled-$tag] ${throwable.javaClass.simpleName}: ${throwable.localizedMessage}\n${stackTrace.take(300)}"
            val updatedHistory = (newEntry + "\n---\n" + existingHistory).take(4000)
            prefs.edit().putString(KEY_ALL_LOGS, updatedHistory).apply()
            Log.w("CrashReporter", "Handled exception logged: $tag", throwable)
        } catch (_: Exception) {}
    }

    fun getPendingCrashReport(context: Context): CrashReport? {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val hasPending = prefs.getBoolean(KEY_HAS_PENDING, false)
            if (!hasPending) return null

            val timestamp = prefs.getLong(KEY_TIMESTAMP, System.currentTimeMillis())
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(timestamp))
            val exType = prefs.getString(KEY_TYPE, "UnknownException") ?: "UnknownException"
            val exMsg = prefs.getString(KEY_MESSAGE, "Unexpected crash occurred") ?: "Unexpected crash occurred"
            val stack = prefs.getString(KEY_STACKTRACE, "No stacktrace recorded") ?: "No stacktrace recorded"
            val screen = prefs.getString(KEY_SCREEN, "") ?: ""

            val pInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
            } catch (_: Exception) {
                null
            }

            return CrashReport(
                timestamp = timestamp,
                formattedTime = timeStr,
                exceptionType = exType,
                errorMessage = exMsg,
                stackTrace = stack,
                deviceManufacturer = Build.MANUFACTURER,
                deviceModel = Build.MODEL,
                androidVersion = Build.VERSION.RELEASE ?: "Unknown",
                sdkInt = Build.VERSION.SDK_INT,
                appVersionName = pInfo?.versionName ?: "1.0.0",
                appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo?.longVersionCode ?: 1L else @Suppress("DEPRECATION") (pInfo?.versionCode?.toLong() ?: 1L),
                screenContext = screen
            )
        } catch (e: Exception) {
            Log.e("CrashReporter", "Error reading crash report", e)
            return null
        }
    }

    fun clearPendingCrashReport(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_HAS_PENDING, false).apply()
        } catch (_: Exception) {}
    }

    fun getAllErrorLogs(context: Context): String {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_ALL_LOGS, "") ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    fun clearAllLogs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_ALL_LOGS).putBoolean(KEY_HAS_PENDING, false).apply()
        } catch (_: Exception) {}
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Crash Report") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "লগ ক্লিপবোর্ডে কপি করা হয়েছে / Log copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "কপি করতে ব্যর্থ / Copy failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmailReport(context: Context, report: CrashReport, userNote: String = "") {
        try {
            val fullBody = buildString {
                if (userNote.isNotBlank()) {
                    appendLine("USER NOTE / ব্যবহারকারীর মন্তব্য:")
                    appendLine(userNote)
                    appendLine()
                }
                append(report.toFormattedReport())
            }

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(DEVELOPER_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, "[App Crash Report] ${report.exceptionType} on ${report.deviceModel}")
                putExtra(Intent.EXTRA_TEXT, fullBody)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback to general share intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(DEVELOPER_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, "[App Crash Report] ${report.exceptionType}")
                    putExtra(Intent.EXTRA_TEXT, fullBody)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Send Crash Report via..."))
            }
        } catch (e: Exception) {
            copyToClipboard(context, report.toFormattedReport())
            Toast.makeText(context, "ইমেইল অ্যাপ খোলা সম্ভব হয়নি। লগ কপি করা হয়েছে।", Toast.LENGTH_LONG).show()
        }
    }
}

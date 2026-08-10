package com.example

import android.app.Application
import android.content.Context
import android.util.Log

class CalculatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val prefs = getSharedPreferences("app_error_prefs", Context.MODE_PRIVATE)
                val sw = java.io.StringWriter()
                val pw = java.io.PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()
                prefs.edit()
                    .putString("last_error", throwable.localizedMessage ?: throwable.javaClass.simpleName)
                    .putString("last_stacktrace", stackTrace)
                    .putLong("error_time", System.currentTimeMillis())
                    .apply()
                Log.e("CalculatorApp", "Uncaught exception caught: $stackTrace", throwable)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(1)
            }
        }
    }
}

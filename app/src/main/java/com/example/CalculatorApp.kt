package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.util.CrashReporter
import com.example.util.UpdateManager

class CalculatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Global Crash Reporting System
        CrashReporter.install(this)
        
        // Initialize Firebase
        UpdateManager.initFirebase(this)
    }
}

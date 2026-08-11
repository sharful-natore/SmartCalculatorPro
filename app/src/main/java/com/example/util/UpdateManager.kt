package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object UpdateManager {
    private const val TAG = "UpdateManager"
    
    // Remote Config Key Constants
    const val KEY_APP_VERSION = "App_version"
    const val KEY_CHANGE_NOTE = "Change_note"
    const val KEY_UPDATE_URL = "Update_url"

    fun initFirebase(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val apiKey = BuildConfig.FIREBASE_API_KEY
                val appId = BuildConfig.FIREBASE_APP_ID
                val projectId = BuildConfig.FIREBASE_PROJECT_ID
                
                if (apiKey.isNotEmpty() && appId.isNotEmpty() && projectId.isNotEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setApplicationId(appId)
                        .setProjectId(projectId)
                        .build()
                    FirebaseApp.initializeApp(context.applicationContext, options)
                    Log.d(TAG, "Firebase initialized successfully")
                } else {
                    Log.w(TAG, "Firebase configuration missing or empty in BuildConfig")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase", e)
        }
    }

    fun checkForUpdates(
        context: Context,
        onUpdateAvailable: (latestVersion: String, changeLog: String, downloadUrl: String) -> Unit,
        onNoUpdate: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        initFirebase(context)
        
        if (FirebaseApp.getApps(context).isEmpty()) {
            onError(Exception("Firebase is not initialized. Please set up FIREBASE_API_KEY, FIREBASE_APP_ID, and FIREBASE_PROJECT_ID in your environment/Secrets."))
            return
        }
        
        try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0) // Fetch immediately
                .build()
            remoteConfig.setConfigSettingsAsync(configSettings)
            
            // Set defaults
            val defaults = mapOf(
                KEY_APP_VERSION to "1",
                KEY_CHANGE_NOTE to "N/A",
                KEY_UPDATE_URL to "N/A"
            )
            remoteConfig.setDefaultsAsync(defaults)

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val latestVersionStr = remoteConfig.getString(KEY_APP_VERSION)
                        val changeLog = remoteConfig.getString(KEY_CHANGE_NOTE)
                        val downloadUrl = remoteConfig.getString(KEY_UPDATE_URL)

                        Log.d(TAG, "Fetched values: version=$latestVersionStr, note=$changeLog, url=$downloadUrl")

                        val currentVersionCode = try {
                            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                pInfo.longVersionCode
                            } else {
                                @Suppress("DEPRECATION")
                                pInfo.versionCode.toLong()
                            }
                        } catch (e: Exception) {
                            1L
                        }

                        // Parse latest version code
                        val latestVersionCode = latestVersionStr.toLongOrNull() ?: 1L

                        if (latestVersionCode > currentVersionCode && downloadUrl.isNotEmpty() && downloadUrl != "N/A") {
                            onUpdateAvailable(latestVersionStr, changeLog, downloadUrl)
                        } else {
                            onNoUpdate()
                        }
                    } else {
                        val exception = task.exception ?: Exception("Failed to fetch Remote Config")
                        Log.e(TAG, "Fetch failed", exception)
                        onError(exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkForUpdates", e)
            onError(e)
        }
    }

    suspend fun downloadApk(
        context: Context,
        url: String,
        onProgress: (Int) -> Unit,
        onSuccess: (File) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IOException("HTTP error code: ${response.code}")
                }
                val body = response.body ?: throw IOException("Empty response body")
                val contentLength = body.contentLength()
                val apkFile = File(context.cacheDir, "app_update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                body.byteStream().use { inputStream ->
                    FileOutputStream(apkFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                val progress = ((totalBytesRead * 100) / contentLength).toInt()
                                onProgress(progress)
                            }
                        }
                    }
                }
                onSuccess(apkFile)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        try {
            // Check unknown sources permission on Oreo+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return false
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting install intent", e)
            return false
        }
    }
}

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
    
    // Remote Config Key Constants (Default + Fallbacks)
    const val KEY_APP_VERSION = "App_version"
    const val KEY_CHANGE_NOTE = "Change_note"
    const val KEY_UPDATE_URL = "Update_url"
    const val KEY_DEV_PHOTO = "Dev_photo"

    fun fetchDevPhotoUrl(context: Context, onResult: (String) -> Unit) {
        initFirebase(context)
        if (FirebaseApp.getApps(context).isEmpty()) {
            onResult("")
            return
        }
        try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val devPhoto = getRemoteString(remoteConfig, KEY_DEV_PHOTO, "dev_photo", "developer_photo", "DevPhoto")
                    if (devPhoto.isNotEmpty()) {
                        context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
                            .edit().putString("remote_dev_photo_url", devPhoto).apply()
                        onResult(devPhoto)
                    } else {
                        onResult("")
                    }
                } else {
                    onResult("")
                }
            }
        } catch (e: Exception) {
            onResult("")
        }
    }

    private fun getRemoteString(remoteConfig: FirebaseRemoteConfig, vararg keys: String): String {
        for (key in keys) {
            val v = remoteConfig.getString(key)
            if (v.isNotEmpty() && v != "N/A") {
                return v.trim()
            }
        }
        return ""
    }

    /**
     * Compares two version strings (e.g., "1.3" vs "1.2.0", or "3" vs "2").
     * Returns true if remoteVersion is strictly newer than currentVersion.
     */
    private fun isNewerVersion(remoteVersion: String, currentVersionName: String, currentVersionCode: Long): Boolean {
        // 1. Direct integer version code check (e.g. "3" > 2)
        val remoteCode = remoteVersion.toLongOrNull()
        if (remoteCode != null) {
            return remoteCode > currentVersionCode
        }

        // 2. Semantic version comparison (e.g. "1.3" vs "1.2.0")
        try {
            val cleanRemote = remoteVersion.replace(Regex("[^0-9.]"), "")
            val cleanCurrent = currentVersionName.replace(Regex("[^0-9.]"), "")

            val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
        } catch (_: Exception) {}

        return false
    }

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
                KEY_APP_VERSION to "1.0",
                "app_version" to "1.0",
                KEY_CHANGE_NOTE to "N/A",
                "change_note" to "N/A",
                KEY_UPDATE_URL to "N/A",
                "update_url" to "N/A"
            )
            remoteConfig.setDefaultsAsync(defaults)

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Support case-insensitive & varied naming in Firebase Remote Config
                        val latestVersionStr = getRemoteString(
                            remoteConfig,
                            KEY_APP_VERSION,
                            "app_version",
                            "latest_version",
                            "version",
                            "version_code",
                            "latest_version_code"
                        ).ifEmpty { "1.0" }

                        val changeLog = getRemoteString(
                            remoteConfig,
                            KEY_CHANGE_NOTE,
                            "change_note",
                            "changelog",
                            "whats_new",
                            "update_description"
                        ).ifEmpty { "New improvements and bug fixes." }

                        val downloadUrl = getRemoteString(
                            remoteConfig,
                            KEY_UPDATE_URL,
                            "update_url",
                            "apk_url",
                            "download_url",
                            "app_url"
                        )

                        Log.d(TAG, "Fetched values: version=$latestVersionStr, note=$changeLog, url=$downloadUrl")

                        val pInfo = try {
                            context.packageManager.getPackageInfo(context.packageName, 0)
                        } catch (_: Exception) {
                            null
                        }

                        val currentVersionCode = try {
                            if (pInfo != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    pInfo.longVersionCode
                                } else {
                                    @Suppress("DEPRECATION")
                                    pInfo.versionCode.toLong()
                                }
                            } else 1L
                        } catch (_: Exception) {
                            1L
                        }

                        val currentVersionName = pInfo?.versionName ?: BuildConfig.VERSION_NAME ?: "1.0"

                        val isUpdateNeeded = isNewerVersion(latestVersionStr, currentVersionName, currentVersionCode)

                        if (isUpdateNeeded && downloadUrl.isNotEmpty() && downloadUrl != "N/A") {
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

package com.example.util

import android.content.Context
import android.content.SharedPreferences

object TtsSettingsManager {
    private const val PREFS_NAME = "tts_settings_prefs"
    private const val KEY_VOICE_TYPE = "voice_type"
    private const val KEY_SPEECH_RATE = "speech_rate"
    private const val KEY_PITCH = "pitch"
    private const val KEY_ARABIC_VOICE_NAME = "arabic_voice_name"
    private const val KEY_BANGLA_VOICE_NAME = "bangla_voice_name"

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getVoiceType(context: Context): String {
        return getPrefs(context).getString(KEY_VOICE_TYPE, "MALE") ?: "MALE"
    }

    fun setVoiceType(context: Context, value: String) {
        getPrefs(context).edit().putString(KEY_VOICE_TYPE, value).apply()
    }

    fun getArabicVoiceName(context: Context): String {
        return getPrefs(context).getString(KEY_ARABIC_VOICE_NAME, "") ?: ""
    }

    fun setArabicVoiceName(context: Context, value: String) {
        getPrefs(context).edit().putString(KEY_ARABIC_VOICE_NAME, value).apply()
    }

    fun getBanglaVoiceName(context: Context): String {
        return getPrefs(context).getString(KEY_BANGLA_VOICE_NAME, "") ?: ""
    }

    fun setBanglaVoiceName(context: Context, value: String) {
        getPrefs(context).edit().putString(KEY_BANGLA_VOICE_NAME, value).apply()
    }

    fun getSpeechRate(context: Context): Float {
        // Reduced default speech rate from 0.95f / 1.0f to 0.85f as requested
        return getPrefs(context).getFloat(KEY_SPEECH_RATE, 0.85f)
    }

    fun setSpeechRate(context: Context, value: Float) {
        getPrefs(context).edit().putFloat(KEY_SPEECH_RATE, value).apply()
    }

    fun getPitch(context: Context): Float {
        return getPrefs(context).getFloat(KEY_PITCH, 1.0f)
    }

    fun setPitch(context: Context, value: Float) {
        getPrefs(context).edit().putFloat(KEY_PITCH, value).apply()
    }
}

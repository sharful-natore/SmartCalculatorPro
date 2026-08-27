package com.example.data.islamic

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class IslamicMaleTtsPlayer private constructor(context: Context) : TextToSpeech.OnInitListener {

    companion object {
        @Volatile
        private var INSTANCE: IslamicMaleTtsPlayer? = null

        fun getInstance(context: Context): IslamicMaleTtsPlayer {
            return INSTANCE ?: synchronized(this) {
                val instance = IslamicMaleTtsPlayer(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    @Volatile private var isInitialized = false
    private var cachedMaleVoice: Voice? = null
    @Volatile private var pendingAction: (() -> Unit)? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activeAudioId = MutableStateFlow<String?>(null)
    val activeAudioId: StateFlow<String?> = _activeAudioId.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupArabicMaleVoice()
            warmupEngine()
            pendingAction?.invoke()
            pendingAction = null
        }
    }

    private fun warmupEngine() {
        val ttsObj = tts ?: return
        try {
            configureMaleVoiceParams()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsObj.speak(" ", TextToSpeech.QUEUE_FLUSH, null, "warmup")
            }
        } catch (_: Exception) {}
    }

    private fun setupArabicMaleVoice() {
        val ttsObj = tts ?: return
        val arLocale = Locale("ar")
        try {
            ttsObj.language = arLocale
            val availableVoices = ttsObj.voices
            if (availableVoices != null) {
                // EXCLUDE female voices strictly: ar-x-a, ar-x-b, ar-xa-a, ar-xa-b, female, woman, fem, girl, sfb, sfa
                // INCLUDE male voices: ar-x-c, ar-x-d, ar-x-e, ar-xa-c, ar-xa-d, male, man
                val explicitMaleVoice = availableVoices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    val lang = voice.locale.language.lowercase()
                    (lang == "ar" || name.contains("ar")) &&
                    (name.contains("male") ||
                     name.contains("man") ||
                     name.contains("ar-x-c") ||
                     name.contains("ar-x-d") ||
                     name.contains("ar-x-e") ||
                     name.contains("ar-xa-c") ||
                     name.contains("ar-xa-d") ||
                     name.contains("#male")) &&
                    !name.contains("female") &&
                    !name.contains("woman") &&
                    !name.contains("fem") &&
                    !name.contains("girl") &&
                    !name.contains("ar-x-a") &&
                    !name.contains("ar-x-b") &&
                    !name.contains("ar-xa-a") &&
                    !name.contains("ar-xa-b") &&
                    !name.contains("sfb") &&
                    !name.contains("sfa")
                } ?: availableVoices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    val lang = voice.locale.language.lowercase()
                    (lang == "ar" || name.contains("ar")) &&
                    !name.contains("female") &&
                    !name.contains("woman") &&
                    !name.contains("fem") &&
                    !name.contains("girl") &&
                    !name.contains("ar-x-a") &&
                    !name.contains("ar-x-b") &&
                    !name.contains("ar-xa-a") &&
                    !name.contains("ar-xa-b") &&
                    !name.contains("sfb") &&
                    !name.contains("sfa")
                }
                if (explicitMaleVoice != null) {
                    cachedMaleVoice = explicitMaleVoice
                    ttsObj.voice = explicitMaleVoice
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        configureMaleVoiceParams()
    }

    private fun configureMaleVoiceParams() {
        val ttsObj = tts ?: return
        try {
            ttsObj.language = Locale("ar")
            cachedMaleVoice?.let { ttsObj.voice = it }
            // Set natural, resonant male pitch (0.78f) and natural speech rate (0.95f)
            ttsObj.setPitch(0.78f)
            ttsObj.setSpeechRate(0.95f)
        } catch (_: Exception) {}
    }

    fun speakFastArabic(id: String, arabicText: String) {
        val ttsObj = tts ?: return
        if (!isInitialized) {
            pendingAction = { speakFastArabic(id, arabicText) }
            return
        }

        try {
            ttsObj.stop()
        } catch (_: Exception) {}

        _activeAudioId.value = id
        _isSpeaking.value = true

        val cleanAr = arabicText.replace("\n", " ").trim()
        configureMaleVoiceParams()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsObj.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _activeAudioId.value = null
                }
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _activeAudioId.value = null
                }
            })
            ttsObj.speak(cleanAr, TextToSpeech.QUEUE_FLUSH, null, id)
        } else {
            @Suppress("DEPRECATION")
            val params = HashMap<String, String>()
            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = id
            ttsObj.speak(cleanAr, TextToSpeech.QUEUE_FLUSH, params)
        }
    }

    fun speakOrStop(id: String, arabicText: String, banglaText: String? = null) {
        if (!isInitialized) {
            pendingAction = { speakOrStop(id, arabicText, banglaText) }
            return
        }

        if (_isSpeaking.value && _activeAudioId.value == id) {
            stop()
        } else {
            stop()
            speakFastArabic(id, arabicText)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isSpeaking.value = false
        _activeAudioId.value = null
    }
}


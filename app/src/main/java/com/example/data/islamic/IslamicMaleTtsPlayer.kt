package com.example.data.islamic

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
    @Volatile private var isBnAvailable = false
    private var cachedMaleVoice: android.speech.tts.Voice? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activeAudioId = MutableStateFlow<String?>(null)
    val activeAudioId: StateFlow<String?> = _activeAudioId.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupArabicMaleVoice()
            checkBanglaSupport()
        }
    }

    private fun checkBanglaSupport() {
        val ttsObj = tts ?: return
        try {
            val bnLocale = Locale("bn", "BD")
            isBnAvailable = ttsObj.isLanguageAvailable(bnLocale) >= TextToSpeech.LANG_AVAILABLE
        } catch (e: Exception) {
            isBnAvailable = false
        }
    }

    private fun setupArabicMaleVoice() {
        val ttsObj = tts ?: return
        val arLocale = Locale("ar")
        try {
            ttsObj.language = arLocale
            val availableVoices = ttsObj.voices
            if (availableVoices != null) {
                // Strictly exclude female voices and look for male voices
                val maleVoice = availableVoices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    voice.locale.language == "ar" &&
                    (name.contains("male") ||
                     name.contains("man") ||
                     name.contains("ar-x-a") ||
                     name.contains("ar-x-c") ||
                     name.contains("#male")) &&
                    !name.contains("female") &&
                    !name.contains("ar-x-b") &&
                    !name.contains("ar-x-d") &&
                    !name.contains("sfb") &&
                    !name.contains("fem")
                } ?: availableVoices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    voice.locale.language == "ar" &&
                    !name.contains("female") &&
                    !name.contains("ar-x-b") &&
                    !name.contains("ar-x-d") &&
                    !name.contains("sfb") &&
                    !name.contains("fem")
                }
                if (maleVoice != null) {
                    cachedMaleVoice = maleVoice
                    ttsObj.voice = maleVoice
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Set deep, dignified male pitch (0.68f) and natural speech rate (0.95f)
        ttsObj.setPitch(0.68f)
        ttsObj.setSpeechRate(0.95f)
    }

    fun speakFastArabic(id: String, arabicText: String) {
        val ttsObj = tts ?: return
        if (!isInitialized) return

        try {
            ttsObj.stop()
        } catch (_: Exception) {}

        _activeAudioId.value = id
        _isSpeaking.value = true

        val cleanAr = arabicText.replace("\n", " ").trim()
        try {
            ttsObj.language = Locale("ar")
            cachedMaleVoice?.let { ttsObj.voice = it }
            ttsObj.setPitch(0.68f)
            ttsObj.setSpeechRate(0.95f)
        } catch (_: Exception) {}

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
        if (!isInitialized) return
        val ttsObj = tts ?: return

        if (_isSpeaking.value && _activeAudioId.value == id) {
            stop()
        } else {
            stop()
            setupArabicMaleVoice()
            _activeAudioId.value = id
            _isSpeaking.value = true

            val cleanAr = arabicText.replace("\n", " ").trim()
            val cleanBn = banglaText?.replace("\n", " ")?.trim() ?: ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsObj.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "${id}_ar" && cleanBn.isNotEmpty()) {
                            try {
                                if (isBnAvailable) {
                                    ttsObj.language = Locale("bn", "BD")
                                } else {
                                    ttsObj.language = Locale.getDefault()
                                }
                                ttsObj.setPitch(0.70f)
                                ttsObj.setSpeechRate(1.0f)
                                ttsObj.speak(cleanBn, TextToSpeech.QUEUE_FLUSH, null, id)
                            } catch (e: Exception) {
                                _isSpeaking.value = false
                                _activeAudioId.value = null
                            }
                        } else if (utteranceId == id || utteranceId == "${id}_ar") {
                            _isSpeaking.value = false
                            _activeAudioId.value = null
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _activeAudioId.value = null
                    }
                })
                if (cleanBn.isNotEmpty()) {
                    ttsObj.speak(cleanAr, TextToSpeech.QUEUE_FLUSH, null, "${id}_ar")
                } else {
                    ttsObj.speak(cleanAr, TextToSpeech.QUEUE_FLUSH, null, id)
                }
            } else {
                @Suppress("DEPRECATION")
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = id
                ttsObj.speak(cleanAr, TextToSpeech.QUEUE_FLUSH, params)
            }
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


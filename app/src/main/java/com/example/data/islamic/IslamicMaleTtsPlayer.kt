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
    private var isInitialized = false
    private var isBnAvailable = false

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
                val maleVoice = availableVoices.firstOrNull { voice ->
                    voice.locale.language == "ar" &&
                    (voice.name.lowercase().contains("male") ||
                     voice.name.lowercase().contains("man") ||
                     voice.name.lowercase().contains("ar-x-a") ||
                     voice.name.lowercase().contains("ar-x-d") ||
                     !voice.name.lowercase().contains("female"))
                }
                if (maleVoice != null) {
                    ttsObj.voice = maleVoice
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Set dignified male pitch (0.76f) and natural speech rate (0.98f)
        ttsObj.setPitch(0.76f)
        ttsObj.setSpeechRate(0.98f)
    }

    fun speakFastArabic(id: String, arabicText: String) {
        if (!isInitialized) return
        val ttsObj = tts ?: return

        try {
            ttsObj.stop()
        } catch (_: Exception) {}

        _activeAudioId.value = id
        _isSpeaking.value = true

        val cleanAr = arabicText.replace("\n", " ").trim()
        setupArabicMaleVoice()

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
                                ttsObj.setPitch(0.95f)
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


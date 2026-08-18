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

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activeAudioId = MutableStateFlow<String?>(null)
    val activeAudioId: StateFlow<String?> = _activeAudioId.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupArabicMaleVoice()
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

        // Set male pitch and speech rate for dignified Arabic recitation
        ttsObj.setPitch(0.82f)
        ttsObj.setSpeechRate(0.85f)
    }

    fun speakOrStop(id: String, arabicText: String) {
        if (!isInitialized) return
        val ttsObj = tts ?: return

        if (_isSpeaking.value && _activeAudioId.value == id) {
            stop()
        } else {
            stop()
            setupArabicMaleVoice()
            _activeAudioId.value = id
            _isSpeaking.value = true

            val cleanText = arabicText.replace("\n", " ").trim()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsObj.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == id) {
                            _isSpeaking.value = false
                            _activeAudioId.value = null
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _activeAudioId.value = null
                    }
                })
                ttsObj.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, id)
            } else {
                @Suppress("DEPRECATION")
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = id
                ttsObj.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params)
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

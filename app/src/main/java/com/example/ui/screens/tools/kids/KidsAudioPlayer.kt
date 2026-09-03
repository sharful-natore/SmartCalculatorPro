package com.example.ui.screens.tools.kids

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class KidsAudioPlayer(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    var isInitialized by mutableStateOf(false)
        private set
    var isMuted by mutableStateOf(false)

    private var toneGen: ToneGenerator? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            // Graceful fallback if device restricts audio
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setSpeechRate(0.85f) // Slower, clearer speech for young children
            tts?.setPitch(1.15f)     // Cheerful, child-friendly pitch
        }
    }

    fun playClickSound() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun playSuccessChime() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun playCelebrationSound() {
        if (isMuted) return
        scope.launch {
            try {
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                delay(120)
                toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 180)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun speak(text: String, isBn: Boolean = true) {
        if (isMuted || !isInitialized) return
        val engine = tts ?: return

        try {
            val spokenText = if (isBn) {
                text.replace("ঐরাবত", "ওইরা বত")
            } else {
                text
            }

            val targetLocale = if (isBn) {
                Locale("bn", "BD")
            } else {
                Locale.US
            }

            if (engine.isLanguageAvailable(targetLocale) >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = targetLocale
            } else {
                engine.language = Locale.getDefault()
            }

            engine.setSpeechRate(0.85f)
            engine.setPitch(1.15f)

            val utteranceId = "kids_speak_${System.currentTimeMillis()}"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                engine.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } else {
                @Suppress("DEPRECATION")
                engine.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null)
            }
        } catch (e: Exception) {
            // Graceful ignore
        }
    }

    /**
     * Spell out letters one-by-one with step-by-step visual indicator callback,
     * then speak the complete assembled word!
     */
    fun spellWordStepByStep(
        letterParts: List<String>,
        fullWord: String,
        isBn: Boolean,
        onStepHighlight: (Int) -> Unit,
        onComplete: () -> Unit = {}
    ) {
        if (isMuted) {
            onComplete()
            return
        }

        scope.launch {
            for (i in letterParts.indices) {
                onStepHighlight(i)
                val part = letterParts[i]
                speak(part, isBn)
                delay(800) // Allow each letter to be heard clearly
            }
            onStepHighlight(-1) // Clear highlight
            delay(250)
            playSuccessChime()
            delay(150)
            speak(fullWord, isBn)
            delay(900)
            onComplete()
        }
    }

    /**
     * Pronounce Phonics letter sound with clear, child-friendly delivery
     */
    fun speakPhonicsLetter(letter: String, soundBn: String, exampleWord: String) {
        if (isMuted) return
        stop()
        if (exampleWord.isNotEmpty()) {
            speak("$letter. $exampleWord.", isBn = false)
        } else {
            speak(letter, isBn = false)
        }
    }

    fun speakPhonicsLetter(item: PhonicsLetterSound) {
        val example = item.exampleWords.firstOrNull()?.word ?: ""
        speakPhonicsLetter(item.letter, item.soundBn, example)
    }

    /**
     * Read out a phonics rule cleanly
     */
    fun speakPhonicsRule(ruleTitle: String, explanationBn: String, exampleWords: List<String>) {
        if (isMuted) return
        stop()
        scope.launch {
            speak(ruleTitle, isBn = false)
            delay(700)
            speak(explanationBn, isBn = true)
        }
    }

    fun speakPhonicsRule(item: PhonicsRuleItem) {
        speakPhonicsRule(item.ruleTitle, item.explanationBn, item.examples.map { it.word })
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            toneGen?.release()
            toneGen = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}

package com.example.ui.namaz

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class NamazViewModel(application: Application) : AndroidViewModel(application) {

    // Tab State: 0=Wudu, 1=Daily Prayers, 2=Special Prayers, 3=All Duas, 4=Visual Illustrator
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Gender Mode: false = Male (পুরুষ), true = Female (নারী)
    private val _isFemaleMode = MutableStateFlow(false)
    val isFemaleMode: StateFlow<Boolean> = _isFemaleMode.asStateFlow()

    // Search Query for Duas & Niyyat
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected Waqt ID in Daily Prayers tab
    private val _selectedWaqtId = MutableStateFlow("fajr")
    val selectedWaqtId: StateFlow<String> = _selectedWaqtId.asStateFlow()

    // Expanded Cards State
    private val _expandedRuleIds = MutableStateFlow<Set<String>>(setOf("janazah", "eid"))
    val expandedRuleIds: StateFlow<Set<String>> = _expandedRuleIds.asStateFlow()

    // Audio Playback State
    private val _playingDuaId = MutableStateFlow<String?>(null)
    val playingDuaId: StateFlow<String?> = _playingDuaId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        initExoPlayer()
        initTts()
    }

    private fun initExoPlayer() {
        try {
            exoPlayer = ExoPlayer.Builder(getApplication()).build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                        if (!isPlaying && playbackState == Player.STATE_ENDED) {
                            _playingDuaId.value = null
                        }
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(getApplication()) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsInitialized = true
                    textToSpeech?.language = Locale("ar")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun toggleGenderMode() {
        _isFemaleMode.update { !it }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedWaqtId(waqtId: String) {
        _selectedWaqtId.value = waqtId
    }

    fun toggleRuleExpanded(ruleId: String) {
        _expandedRuleIds.update { set ->
            if (set.contains(ruleId)) set - ruleId else set + ruleId
        }
    }

    fun playOrPauseDuaAudio(duaId: String, audioUrl: String?, arabicText: String, banglaPronunciation: String) {
        if (_playingDuaId.value == duaId && _isPlaying.value) {
            pauseAudio()
            return
        }

        stopAudio()
        _playingDuaId.value = duaId

        if (audioUrl != null && audioUrl.isNotEmpty()) {
            try {
                exoPlayer?.let { player ->
                    player.setMediaItem(MediaItem.fromUri(audioUrl))
                    player.prepare()
                    player.play()
                    _isPlaying.value = true
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to TTS recitation if URL is empty or fails
        if (isTtsInitialized && textToSpeech != null) {
            textToSpeech?.setLanguage(Locale("ar"))
            textToSpeech?.speak(arabicText, TextToSpeech.QUEUE_FLUSH, null, duaId)
            _isPlaying.value = true
        }
    }

    fun pauseAudio() {
        exoPlayer?.pause()
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        _isPlaying.value = false
    }

    fun stopAudio() {
        exoPlayer?.stop()
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        _playingDuaId.value = null
        _isPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        try {
            exoPlayer?.release()
            exoPlayer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()

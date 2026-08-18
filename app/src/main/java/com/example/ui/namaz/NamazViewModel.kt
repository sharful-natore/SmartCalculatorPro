package com.example.ui.namaz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.islamic.IslamicAudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NamazViewModel(application: Application) : AndroidViewModel(application) {

    val audioPlayer: IslamicAudioPlayer = IslamicAudioPlayer.getInstance(application)
    val maleTtsPlayer: com.example.data.islamic.IslamicMaleTtsPlayer = com.example.data.islamic.IslamicMaleTtsPlayer.getInstance(application)

    // Tab State: 0=Wudu & Taharat, 1=5 Daily Waqts, 2=Special Prayers, 3=Duas & Surahs, 4=Ahkam & Sahw, 5=Visual Guide
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Gender Mode: false = Male (পুরুষ), true = Female (নারী)
    private val _isFemaleMode = MutableStateFlow(false)
    val isFemaleMode: StateFlow<Boolean> = _isFemaleMode.asStateFlow()

    // Search Query for Duas & Niyyat & Surahs
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected Waqt ID in Daily Prayers tab ("fajr", "dhuhr", "asr", "maghrib", "isha")
    private val _selectedWaqtId = MutableStateFlow("fajr")
    val selectedWaqtId: StateFlow<String> = _selectedWaqtId.asStateFlow()

    // Selected Sub-Rakat View in Daily Waqts ("farz", "sunnat", "witr")
    private val _selectedRakatType = MutableStateFlow("farz")
    val selectedRakatType: StateFlow<String> = _selectedRakatType.asStateFlow()

    // Expanded Cards State in Special Prayers & Fiqh
    private val _expandedRuleIds = MutableStateFlow<Set<String>>(setOf("jumuah", "janazah", "eid", "ahkam_arkan", "sahw_procedure"))
    val expandedRuleIds: StateFlow<Set<String>> = _expandedRuleIds.asStateFlow()

    // Audio Playback State delegated to IslamicMaleTtsPlayer
    val playingDuaId: StateFlow<String?> = maleTtsPlayer.activeAudioId
    val isPlaying: StateFlow<Boolean> = maleTtsPlayer.isSpeaking

    // Downloaded Duas Tracking
    val downloadedDuaIds: StateFlow<Set<String>> = audioPlayer.downloadedAudioIds

    // Download Progress Tracking: Maps duaId -> progress percentage (1..100)
    val downloadProgress: StateFlow<Map<String, Int>> = audioPlayer.downloadProgress

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun toggleGenderMode() {
        _isFemaleMode.update { !it }
    }

    fun setGenderMode(female: Boolean) {
        _isFemaleMode.value = female
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedWaqtId(waqtId: String) {
        _selectedWaqtId.value = waqtId
        _selectedRakatType.value = "farz"
    }

    fun setSelectedRakatType(type: String) {
        _selectedRakatType.value = type
    }

    fun toggleRuleExpanded(ruleId: String) {
        _expandedRuleIds.update { set ->
            if (set.contains(ruleId)) set - ruleId else set + ruleId
        }
    }

    fun downloadDuaAudio(duaId: String, arabicText: String = "", explicitUrl: String? = null) {
        // CDN download disabled for non-Quran items
    }

    fun playOrPauseDuaAudio(
        duaId: String,
        audioUrl: String? = null,
        arabicText: String = "",
        banglaPronunciation: String = "",
        title: String = "",
        category: String = "ISLAMIC"
    ) {
        maleTtsPlayer.speakOrStop(id = duaId, arabicText = arabicText)
    }

    fun pauseAudio() {
        maleTtsPlayer.stop()
    }

    fun stopAudio() {
        maleTtsPlayer.stop()
    }

    override fun onCleared() {
        super.onCleared()
    }
}

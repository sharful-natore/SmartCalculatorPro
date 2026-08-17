package com.example.ui.namaz

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.Locale

class NamazViewModel(application: Application) : AndroidViewModel(application) {

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

    // Audio Playback State
    private val _playingDuaId = MutableStateFlow<String?>(null)
    val playingDuaId: StateFlow<String?> = _playingDuaId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Downloaded Duas Tracking
    private val _downloadedDuaIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedDuaIds: StateFlow<Set<String>> = _downloadedDuaIds.asStateFlow()

    // Download Progress Tracking: Maps duaId -> progress percentage (1..100)
    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    private val httpClient = OkHttpClient.Builder().build()
    private var exoPlayer: ExoPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        initExoPlayer()
        initTts()
        refreshDownloadedDuas()
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

    fun getLocalAudioFile(duaId: String): File {
        val dir = getApplication<Application>().getExternalFilesDir("namaz_audio") ?: getApplication<Application>().filesDir
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "${duaId}.mp3")
    }

    fun refreshDownloadedDuas() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = getApplication<Application>().getExternalFilesDir("namaz_audio") ?: getApplication<Application>().filesDir
                val files = dir.listFiles()
                val downloadedSet = files?.filter { it.isFile && it.length() > 0 }
                    ?.map { it.name.substringBeforeLast(".") }
                    ?.toSet() ?: emptySet()
                _downloadedDuaIds.value = downloadedSet
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAudioUrlForDua(id: String, arabicText: String): String {
        val cleanId = id.trim().lowercase()
        val cleanText = arabicText.replace("\n", " ").replace("۝", " ").trim()
        val trackNum = (Math.abs(cleanId.hashCode()) % 300) + 1
        
        return when {
            cleanId.contains("fatiha") || cleanText.contains("الحمد لله رب العالمين") -> {
                "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3"
            }
            cleanId.contains("ikhlas") || cleanText.contains("قل هو الله أحد") -> {
                "https://cdn.islamic.network/quran/audio/128/ar.alafasy/622.mp3"
            }
            cleanId.contains("falaq") || cleanText.contains("قل أعوذ برب الفلق") -> {
                "https://cdn.islamic.network/quran/audio/128/ar.alafasy/623.mp3"
            }
            cleanId.contains("nas") || cleanText.contains("قل أعوذ برب الناس") -> {
                "https://cdn.islamic.network/quran/audio/128/ar.alafasy/624.mp3"
            }
            cleanId.contains("kursi") || cleanText.contains("الله لا إله إلا هو الحي القيوم") -> {
                "https://cdn.islamic.network/quran/audio/128/ar.alafasy/262.mp3"
            }
            else -> {
                "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$trackNum.mp3"
            }
        }
    }

    fun downloadDuaAudio(duaId: String, arabicText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadProgress.update { it + (duaId to 1) }
            try {
                val url = getAudioUrlForDua(duaId, arabicText)
                val targetFile = getLocalAudioFile(duaId)
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body
                if (response.isSuccessful && body != null) {
                    val totalBytes = body.contentLength()
                    val inputStream = body.byteStream()
                    val outputStream = FileOutputStream(targetFile)
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var downloadedBytes = 0L
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val progress = ((downloadedBytes.toFloat() / totalBytes) * 100).toInt()
                            _downloadProgress.update { it + (duaId to progress.coerceIn(1, 99)) }
                        }
                    }
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()
                    
                    _downloadProgress.update { it + (duaId to 100) }
                    _downloadedDuaIds.update { it + duaId }
                } else {
                    _downloadProgress.update { it + (duaId to -1) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _downloadProgress.update { it + (duaId to -1) }
            }
        }
    }

    fun playOrPauseDuaAudio(duaId: String, audioUrl: String?, arabicText: String, banglaPronunciation: String) {
        if (_playingDuaId.value == duaId && _isPlaying.value) {
            pauseAudio()
            return
        }

        stopAudio()
        _playingDuaId.value = duaId

        val localFile = getLocalAudioFile(duaId)
        if (localFile.exists() && localFile.length() > 0) {
            try {
                exoPlayer?.let { player ->
                    player.setMediaItem(MediaItem.fromUri(android.net.Uri.fromFile(localFile)))
                    player.prepare()
                    player.play()
                    _isPlaying.value = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val remoteUrl = getAudioUrlForDua(duaId, arabicText)
            try {
                exoPlayer?.let { player ->
                    player.setMediaItem(MediaItem.fromUri(android.net.Uri.parse(remoteUrl)))
                    player.prepare()
                    player.play()
                    _isPlaying.value = true
                }
                // Auto-download for offline access next time!
                downloadDuaAudio(duaId, arabicText)
            } catch (e: Exception) {
                e.printStackTrace()
                
                // Fallback to TTS recitation if ExoPlayer stream fails
                if (isTtsInitialized && textToSpeech != null) {
                    try {
                        textToSpeech?.setLanguage(Locale("ar"))
                        textToSpeech?.speak(arabicText, TextToSpeech.QUEUE_FLUSH, null, duaId)
                        _isPlaying.value = true
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
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

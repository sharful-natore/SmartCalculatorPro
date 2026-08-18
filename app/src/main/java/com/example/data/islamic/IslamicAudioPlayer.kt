package com.example.data.islamic

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class IslamicAudioPlayer private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: IslamicAudioPlayer? = null

        fun getInstance(context: Context): IslamicAudioPlayer {
            return INSTANCE ?: synchronized(this) {
                val instance = IslamicAudioPlayer(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        fun normalizeArabicText(text: String): String {
            val regex = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06DC\\u06DF-\\u06E8\\u06EA-\\u06ED]")
            return text.replace(regex, "")
                .replace("\n", " ")
                .replace("۝", " ")
                .replace("۔", " ")
                .replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("ى", "ي")
                .replace("  ", " ")
                .trim()
        }

        fun getCdnAudioUrl(id: String, arabicText: String = ""): String {
            val cleanId = id.trim().lowercase()
            val normText = normalizeArabicText(arabicText)

            // 1. FASTING (Sehri & Iftar)
            if (cleanId.contains("sehri") || cleanId.contains("fasting_niyyah") || normText.contains("نويت ان اصوم") || normText.contains("نويت")) {
                return "https://hisnmuslim.com/audio/ar/175.mp3"
            }
            if (cleanId.contains("iftar") || normText.contains("اللهم لك صمت") || normText.contains("ذهب الظما")) {
                return "https://hisnmuslim.com/audio/ar/176.mp3"
            }

            // 2. QURAN SURAHS (Complete Surah high quality Alafasy CDN)
            if (cleanId.contains("fatiha") || normText.contains("الحمد لله رب العالمين")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/1.mp3"
            }
            if (cleanId.contains("ikhlas") || normText.contains("قل هو الله احد")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/112.mp3"
            }
            if (cleanId.contains("falaq") || normText.contains("قل اعوذ برب الفلق")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/113.mp3"
            }
            if (cleanId.contains("nas") && !cleanId.contains("nasta") || normText.contains("قل اعوذ برب الناس")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/114.mp3"
            }
            if (cleanId.contains("kafirun") || normText.contains("قل يا ايها الكافرون")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/109.mp3"
            }
            if (cleanId.contains("kawthar") || normText.contains("انا اعطيناك الكوثر")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/108.mp3"
            }
            if (cleanId.contains("fil") || normText.contains("الم تر كيف فعل ربك")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/105.mp3"
            }
            if (cleanId.contains("quraish") || normText.contains("لايلاف قريش")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/106.mp3"
            }
            if (cleanId.contains("maun") || normText.contains("ارايت الذي يكذب")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/107.mp3"
            }
            if (cleanId.contains("asr") && !cleanId.contains("masura") || normText.contains("والعصر")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/103.mp3"
            }
            if (cleanId.contains("qadr") || normText.contains("انا انزلناه في ليلة القدر")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/97.mp3"
            }
            if (cleanId.contains("nasr") || normText.contains("إذا جاء نصر الله")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/110.mp3"
            }
            if (cleanId.contains("lahab") || cleanId.contains("masad") || normText.contains("تبت يدا ابي لهب")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/111.mp3"
            }
            if (cleanId.contains("yasin") || normText.contains("يس")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/36.mp3"
            }
            if (cleanId.contains("mulk") || normText.contains("تبارك الذي بيده الملك")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/67.mp3"
            }
            if (cleanId.contains("rahman") && !cleanId.contains("bismillah") || normText.contains("الرحمن علم القران")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/55.mp3"
            }
            if (cleanId.contains("waqiah") || normText.contains("إذا وقعت الواقعة")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/56.mp3"
            }

            // 3. SPECIFIC QURANIC AYAHS
            if (cleanId.contains("kursi") || normText.contains("الله لا اله الا هو الحي القيوم")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/262.mp3"
            }
            if (cleanId.contains("rabbana_atina") || normText.contains("ربنا اتنا في الدنيا حسنة")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/208.mp3"
            }
            if (cleanId.contains("yunus") || normText.contains("لا اله الا انت سبحانك اني كنت من الظالمين")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/2570.mp3"
            }
            if (cleanId.contains("parents") || normText.contains("رب ارحمهما كما ربياني صغيرا")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/2053.mp3"
            }
            if (cleanId.contains("amanar_rasul") || normText.contains("امن الرسول بما انزل")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/292.mp3"
            }
            if (cleanId.contains("travel") || cleanId.contains("vehicle_ride") || normText.contains("سبحان الذي سخر لنا هذا")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/4338.mp3"
            }
            if (cleanId.contains("hashr") || normText.contains("هو الله الذي لا اله الا هو")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/5148.mp3"
            }

            // 4. NAMAZ / SALAH STEPS & ADHKAR (Hisnul Muslim)
            if (cleanId.contains("thana") || cleanId.contains("sana") || normText.contains("سبحانك اللهم وبحمدك")) {
                return "https://hisnmuslim.com/audio/ar/27.mp3"
            }
            if (cleanId.contains("ruku") || normText.contains("سبحان ربي العظيم")) {
                return "https://hisnmuslim.com/audio/ar/32.mp3"
            }
            if (cleanId.contains("qauma") || normText.contains("سمع الله لمن حمده")) {
                return "https://hisnmuslim.com/audio/ar/34.mp3"
            }
            if (cleanId.contains("sujud") || cleanId.contains("sujood") || normText.contains("سبحان ربي الاعلي")) {
                return "https://hisnmuslim.com/audio/ar/38.mp3"
            }
            if (cleanId.contains("jalsa") || cleanId.contains("between_sujood") || normText.contains("رب اغفر لي") || normText.contains("اللهم اغفر لي")) {
                return "https://hisnmuslim.com/audio/ar/44.mp3"
            }
            if (cleanId.contains("tashahhud") || cleanId.contains("attahiyyat") || normText.contains("التحيات لله")) {
                return "https://hisnmuslim.com/audio/ar/49.mp3"
            }
            if (cleanId.contains("durood") || normText.contains("اللهم صل علي محمد")) {
                return "https://hisnmuslim.com/audio/ar/54.mp3"
            }
            if (cleanId.contains("masura") || normText.contains("اللهم اني ظلمت نفسي")) {
                return "https://hisnmuslim.com/audio/ar/55.mp3"
            }
            if (cleanId.contains("qunut") || normText.contains("اللهم انا نستعينك") || normText.contains("اللهم اهدني")) {
                return "https://hisnmuslim.com/audio/ar/116.mp3"
            }
            if (cleanId.contains("post_prayer") || cleanId.contains("tasbeeh") || normText.contains("اللهم انت السلام")) {
                return "https://hisnmuslim.com/audio/ar/66.mp3"
            }
            if (cleanId.contains("wudu_start") || cleanId.contains("before_wudu") || (cleanId.contains("wudu") && normText.contains("بسم الله"))) {
                return "https://hisnmuslim.com/audio/ar/12.mp3"
            }
            if (cleanId.contains("wudu_end") || cleanId.contains("after_wudu") || (cleanId.contains("wudu") && normText.contains("اشهد ان لا اله الا الله"))) {
                return "https://hisnmuslim.com/audio/ar/13.mp3"
            }

            // 5. DAILY DUAS & SUPPLICATIONS (Hisnul Muslim)
            if (cleanId.contains("wake_up") || cleanId.contains("waking") || normText.contains("الحمد لله الذي احيانا")) {
                return "https://hisnmuslim.com/audio/ar/1.mp3"
            }
            if (cleanId.contains("before_sleep") || cleanId.contains("sleeping") || normText.contains("باسمك اللهم اموت")) {
                return "https://hisnmuslim.com/audio/ar/99.mp3"
            }
            if (cleanId.contains("leave_home") || cleanId.contains("home_exit") || normText.contains("بسم الله توكلت")) {
                return "https://hisnmuslim.com/audio/ar/16.mp3"
            }
            if (cleanId.contains("enter_home") || cleanId.contains("home_enter") || normText.contains("بسم الله ولجنا")) {
                return "https://hisnmuslim.com/audio/ar/18.mp3"
            }
            if (cleanId.contains("enter_mosque") || cleanId.contains("mosque_enter") || normText.contains("اللهم افتح لي ابواب رحمتك")) {
                return "https://hisnmuslim.com/audio/ar/20.mp3"
            }
            if (cleanId.contains("exit_mosque") || cleanId.contains("mosque_exit") || normText.contains("اللهم اني اسالك من فضلك")) {
                return "https://hisnmuslim.com/audio/ar/21.mp3"
            }
            if (cleanId.contains("before_eat") || cleanId.contains("eating_start") || normText.contains("بسم الله وعلي بركة الله")) {
                return "https://hisnmuslim.com/audio/ar/182.mp3"
            }
            if (cleanId.contains("after_eat") || cleanId.contains("eating_end") || normText.contains("الحمد لله الذي اطعمنا")) {
                return "https://hisnmuslim.com/audio/ar/184.mp3"
            }
            if (cleanId.contains("enter_toilet") || cleanId.contains("restroom_enter") || normText.contains("اللهم اني اعوذ بك من الخبث")) {
                return "https://hisnmuslim.com/audio/ar/10.mp3"
            }
            if (cleanId.contains("exit_toilet") || cleanId.contains("restroom_exit") || normText.contains("غفرانك")) {
                return "https://hisnmuslim.com/audio/ar/11.mp3"
            }
            if (cleanId.contains("sayyidul_istighfar") || cleanId.contains("istighfar") || normText.contains("اللهم انت ربي")) {
                return "https://hisnmuslim.com/audio/ar/77.mp3"
            }
            if (cleanId.contains("bismillahillazi") || cleanId.contains("protection") || normText.contains("بسم الله الذي لا يضر")) {
                return "https://hisnmuslim.com/audio/ar/79.mp3"
            }
            if (cleanId.contains("raditu_billah") || normText.contains("رضيت بالله ربا")) {
                return "https://hisnmuslim.com/audio/ar/78.mp3"
            }
            if (cleanId.contains("debt_anxiety") || cleanId.contains("anxiety") || cleanId.contains("debt") || normText.contains("اللهم اني اعوذ بك من الهم")) {
                return "https://hisnmuslim.com/audio/ar/120.mp3"
            }
            if (cleanId.contains("illness") || cleanId.contains("sickness") || cleanId.contains("pain") || normText.contains("اللهم رب الناس اذهب الباس")) {
                return "https://hisnmuslim.com/audio/ar/144.mp3"
            }
            if (cleanId.contains("hasbunallah") || normText.contains("حسبنا الله ونعم الوكيل")) {
                return "https://hisnmuslim.com/audio/ar/135.mp3"
            }
            if (cleanId.contains("janazah") || cleanId.contains("grave") || normText.contains("اللهم اغفر لحينا وميتنا")) {
                return "https://hisnmuslim.com/audio/ar/156.mp3"
            }
            if (cleanId.contains("istikhara") || normText.contains("اللهم اني استخيرك بعلمك")) {
                return "https://hisnmuslim.com/audio/ar/26.mp3"
            }

            // Default Fallback
            return "https://hisnmuslim.com/audio/ar/1.mp3"
        }
    }

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val httpClient = OkHttpClient.Builder().build()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPlayerActive = MutableStateFlow(false)
    val isPlayerActive: StateFlow<Boolean> = _isPlayerActive.asStateFlow()

    private val _activeAudioId = MutableStateFlow<String?>(null)
    val activeAudioId: StateFlow<String?> = _activeAudioId.asStateFlow()

    private val _activeTitle = MutableStateFlow("")
    val activeTitle: StateFlow<String> = _activeTitle.asStateFlow()

    private val _activeSubtitle = MutableStateFlow("")
    val activeSubtitle: StateFlow<String> = _activeSubtitle.asStateFlow()

    private val _activeCategory = MutableStateFlow("ISLAMIC")
    val activeCategory: StateFlow<String> = _activeCategory.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _downloadedAudioIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedAudioIds: StateFlow<Set<String>> = _downloadedAudioIds.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    private var positionJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    _isPlayerActive.value = true
                    startPositionTracker()
                } else {
                    stopPositionTracker()
                    if (player.playbackState == Player.STATE_ENDED) {
                        _activeAudioId.value = null
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _durationMs.value = player.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0L
                    stopPositionTracker()
                }
            }
        })
        refreshDownloadedAudios()
    }

    private fun startPositionTracker() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (true) {
                if (player.isPlaying) {
                    _currentPositionMs.value = player.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = player.duration.coerceAtLeast(0L)
                }
                kotlinx.coroutines.delay(200)
            }
        }
    }

    private fun stopPositionTracker() {
        positionJob?.cancel()
        positionJob = null
    }

    fun getLocalAudioFile(audioId: String): File {
        val baseDir = context.getExternalFilesDir("islamic_audio") ?: context.filesDir
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        val safeName = audioId.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return File(baseDir, "${safeName}.mp3")
    }

    fun refreshDownloadedAudios() {
        scope.launch(Dispatchers.IO) {
            try {
                val baseDir = context.getExternalFilesDir("islamic_audio") ?: context.filesDir
                val files = baseDir.listFiles()
                val downloaded = files?.filter { it.isFile && it.length() > 0 }
                    ?.map { it.name.substringBeforeLast(".") }
                    ?.toSet() ?: emptySet()
                _downloadedAudioIds.value = downloaded
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun downloadAudio(audioId: String, arabicText: String = "", explicitUrl: String? = null) {
        if (!com.example.util.NetworkUtil.isOnline(context)) {
            scope.launch(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    context,
                    "ইন্টারনেট সংযোগ নেই! ডাউনলোড করতে ইন্টারনেট সংযোগ অন করুন।",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        scope.launch(Dispatchers.IO) {
            _downloadProgress.update { it + (audioId to 1) }
            try {
                val url = explicitUrl?.takeIf { it.isNotEmpty() } ?: getCdnAudioUrl(audioId, arabicText)
                val targetFile = getLocalAudioFile(audioId)
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
                            _downloadProgress.update { it + (audioId to progress.coerceIn(1, 99)) }
                        }
                    }
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    _downloadProgress.update { it + (audioId to 100) }
                    _downloadedAudioIds.update { it + audioId }
                    scope.launch(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            context,
                            "অডিও সফলভাবে ডাউনলোড হয়েছে!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    _downloadProgress.update { it + (audioId to -1) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _downloadProgress.update { it + (audioId to -1) }
            }
        }
    }

    fun playOrPause(
        audioId: String,
        arabicText: String = "",
        explicitUrl: String? = null,
        title: String = "",
        subtitle: String = "",
        category: String = "ISLAMIC"
    ) {
        if (_activeAudioId.value == audioId && _isPlaying.value) {
            player.pause()
            _isPlaying.value = false
            return
        }

        if (_activeAudioId.value == audioId && !_isPlaying.value && player.playbackState == Player.STATE_READY) {
            player.play()
            _isPlaying.value = true
            _isPlayerActive.value = true
            return
        }

        // Stop any Quran playback so only one audio plays at a time
        try {
            com.example.data.quran.QuranAudioPlayer.getInstance(context).pause()
        } catch (e: Exception) {
            // ignore
        }

        player.stop()
        _activeAudioId.value = audioId
        if (title.isNotEmpty()) _activeTitle.value = title
        if (subtitle.isNotEmpty()) _activeSubtitle.value = subtitle
        _activeCategory.value = category
        _isPlayerActive.value = true

        val localFile = getLocalAudioFile(audioId)
        if (localFile.exists() && localFile.length() > 0) {
            try {
                player.setMediaItem(MediaItem.fromUri(Uri.fromFile(localFile)))
                player.prepare()
                player.play()
                _isPlaying.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (!com.example.util.NetworkUtil.isOnline(context)) {
                _activeAudioId.value = null
                _isPlaying.value = false
                _isPlayerActive.value = false
                scope.launch(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        "ইন্টারনেট সংযোগ নেই! অডিও শুনতে ইন্টারনেট চালু করুন অথবা আগে ডাউনলোড করুন।",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }

            val url = explicitUrl?.takeIf { it.isNotEmpty() } ?: getCdnAudioUrl(audioId, arabicText)
            try {
                player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
                player.prepare()
                player.play()
                _isPlaying.value = true
                // Asynchronously cache for future instant offline playback
                downloadAudio(audioId, arabicText, explicitUrl)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            player.pause()
            _isPlaying.value = false
        } else {
            if (_activeAudioId.value != null) {
                player.play()
                _isPlaying.value = true
            }
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceIn(0L, _durationMs.value))
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        player.setPlaybackSpeed(speed)
    }

    fun pause() {
        try {
            player.pause()
            _isPlaying.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            player.stop()
            _activeAudioId.value = null
            _isPlaying.value = false
            stopPositionTracker()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAndClose() {
        stop()
        _isPlayerActive.value = false
    }

    fun release() {
        try {
            stopPositionTracker()
            player.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

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

        fun getCdnAudioUrl(id: String, arabicText: String = ""): String {
            val cleanId = id.trim().lowercase()
            val cleanText = arabicText.replace("\n", " ").replace("۝", " ").trim()

            // 1. Quran Surahs (Complete Surah high quality Mishary Alafasy CDN)
            if (cleanId.contains("fatiha") || cleanText.contains("الحمد لله رب العالمين")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/1.mp3"
            }
            if (cleanId.contains("ikhlas") || cleanText.contains("قل هو الله أحد")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/112.mp3"
            }
            if (cleanId.contains("falaq") || cleanText.contains("قل أعوذ برب الفلق")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/113.mp3"
            }
            if (cleanId.contains("nas") && !cleanId.contains("nasta") || cleanText.contains("قل أعوذ برب الناس")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/114.mp3"
            }
            if (cleanId.contains("kafirun") || cleanText.contains("قل يا أيها الكافرون")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/109.mp3"
            }
            if (cleanId.contains("kawthar") || cleanText.contains("إنا أعطيناك الكوثر")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/108.mp3"
            }
            if (cleanId.contains("fil") || cleanText.contains("ألم تر كيف فعل ربك")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/105.mp3"
            }
            if (cleanId.contains("quraish") || cleanText.contains("لإيلاف قريش")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/106.mp3"
            }
            if (cleanId.contains("maun") || cleanText.contains("أرأيت الذي يكذب")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/107.mp3"
            }
            if (cleanId.contains("asr") && !cleanId.contains("masura") || cleanText.contains("والعصر")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/103.mp3"
            }
            if (cleanId.contains("qadr") || cleanText.contains("إنا أنزلناه في ليلة القدر")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/97.mp3"
            }
            if (cleanId.contains("yasin") || cleanText.contains("يس")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/36.mp3"
            }
            if (cleanId.contains("mulk") || cleanText.contains("تبارك الذي بيده الملك")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/67.mp3"
            }
            if (cleanId.contains("rahman") && !cleanId.contains("bismillah") || cleanText.contains("الرحمن علم القرآن")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/55.mp3"
            }
            if (cleanId.contains("waqiah") || cleanText.contains("إذا وقعت الواقعة")) {
                return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/56.mp3"
            }

            // 2. Quran Ayahs & Duas
            if (cleanId.contains("kursi") || cleanText.contains("الله لا إله إلا هو الحي القيوم")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/262.mp3"
            }
            if (cleanId.contains("rabbana_atina") || cleanText.contains("ربنا آتنا في الدنيا حسنة")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/208.mp3"
            }
            if (cleanId.contains("yunus") || cleanText.contains("لا إله إلا أنت سبحانك إني كنت من الظالمين")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/2570.mp3"
            }
            if (cleanId.contains("parents") || cleanText.contains("رب ارحمهما كما ربياني صغيرا")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/2053.mp3"
            }
            if (cleanId.contains("amanar_rasul") || cleanText.contains("آمن الرسول بما أنزل")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/292.mp3"
            }
            if (cleanId.contains("travel") || cleanText.contains("سبحان الذي سخر لنا هذا")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/4338.mp3"
            }
            if (cleanId.contains("hashr") || cleanText.contains("هو الله الذي لا إله إلا هو")) {
                return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/5148.mp3"
            }

            // 3. Fasting (Sehri & Iftar) Duas
            if (cleanId.contains("sehri") || cleanId.contains("fasting_niyyah") || cleanText.contains("نويت أن أصوم") || cleanText.contains("نَوَيْتُ")) {
                return "https://hisnmuslim.com/audio/ar/176.mp3"
            }
            if (cleanId.contains("iftar") || cleanText.contains("اللهم لك صمت") || cleanText.contains("ذهب الظمأ")) {
                return "https://hisnmuslim.com/audio/ar/176.mp3"
            }

            // 4. Namaz / Salah Steps & Adhkar (Hisnul Muslim CDN)
            if (cleanId.contains("sana") || cleanText.contains("سبحانك اللهم وبحمدك")) {
                return "https://hisnmuslim.com/audio/ar/27.mp3"
            }
            if (cleanId.contains("ruku") || cleanText.contains("سبحان ربي العظيم")) {
                return "https://hisnmuslim.com/audio/ar/32.mp3"
            }
            if (cleanId.contains("qauma") || cleanText.contains("سمع الله لمن حمده")) {
                return "https://hisnmuslim.com/audio/ar/34.mp3"
            }
            if (cleanId.contains("sujud") || cleanText.contains("سبحان ربي الأعلى")) {
                return "https://hisnmuslim.com/audio/ar/38.mp3"
            }
            if (cleanId.contains("jalsa") || cleanText.contains("رب اغفر لي")) {
                return "https://hisnmuslim.com/audio/ar/44.mp3"
            }
            if (cleanId.contains("tashahhud") || cleanId.contains("attahiyyat") || cleanText.contains("التحيات لله")) {
                return "https://hisnmuslim.com/audio/ar/49.mp3"
            }
            if (cleanId.contains("durood") || cleanText.contains("اللهم صل على محمد")) {
                return "https://hisnmuslim.com/audio/ar/54.mp3"
            }
            if (cleanId.contains("masura") || cleanText.contains("اللهم إني ظلمت نفسي")) {
                return "https://hisnmuslim.com/audio/ar/55.mp3"
            }
            if (cleanId.contains("qunut") || cleanText.contains("اللهم إنا نستعينك") || cleanText.contains("اللهم اهدني")) {
                return "https://hisnmuslim.com/audio/ar/116.mp3"
            }
            if (cleanId.contains("post_prayer") || cleanId.contains("tasbeeh") || cleanText.contains("اللهم أنت السلام")) {
                return "https://hisnmuslim.com/audio/ar/66.mp3"
            }
            if (cleanId.contains("wudu_start") || (cleanId.contains("wudu") && cleanText.contains("بسم الله"))) {
                return "https://hisnmuslim.com/audio/ar/12.mp3"
            }
            if (cleanId.contains("wudu_end") || (cleanId.contains("wudu") && cleanText.contains("أشهد أن لا إله إلا الله"))) {
                return "https://hisnmuslim.com/audio/ar/13.mp3"
            }

            // 5. Daily Duas & Supplications
            if (cleanId.contains("waking") || cleanText.contains("الحمد لله الذي أحيانا")) {
                return "https://hisnmuslim.com/audio/ar/1.mp3"
            }
            if (cleanId.contains("sleeping") || cleanText.contains("باسمك اللهم أموت")) {
                return "https://hisnmuslim.com/audio/ar/99.mp3"
            }
            if (cleanId.contains("restroom_enter") || cleanText.contains("اللهم إني أعوذ بك من الخبث")) {
                return "https://hisnmuslim.com/audio/ar/10.mp3"
            }
            if (cleanId.contains("restroom_exit") || cleanText.contains("غفرانك")) {
                return "https://hisnmuslim.com/audio/ar/11.mp3"
            }
            if (cleanId.contains("home_exit") || cleanText.contains("بسم الله توكلت")) {
                return "https://hisnmuslim.com/audio/ar/16.mp3"
            }
            if (cleanId.contains("home_enter") || cleanText.contains("بسم الله ولجنا")) {
                return "https://hisnmuslim.com/audio/ar/18.mp3"
            }
            if (cleanId.contains("mosque_enter") || cleanText.contains("اللهم افتح لي أبواب رحمتك")) {
                return "https://hisnmuslim.com/audio/ar/20.mp3"
            }
            if (cleanId.contains("mosque_exit") || cleanText.contains("اللهم إني أسألك من فضلك")) {
                return "https://hisnmuslim.com/audio/ar/21.mp3"
            }
            if (cleanId.contains("eating_start") || cleanText.contains("بسم الله أوله وآخره")) {
                return "https://hisnmuslim.com/audio/ar/182.mp3"
            }
            if (cleanId.contains("eating_end") || cleanText.contains("الحمد لله الذي أطعمنا")) {
                return "https://hisnmuslim.com/audio/ar/184.mp3"
            }
            if (cleanId.contains("istighfar") || cleanText.contains("اللهم أنت ربي لا إله إلا أنت خلقتني")) {
                return "https://hisnmuslim.com/audio/ar/77.mp3"
            }
            if (cleanId.contains("protection") || cleanText.contains("بسم الله الذي لا يضر مع اسمه")) {
                return "https://hisnmuslim.com/audio/ar/79.mp3"
            }
            if (cleanId.contains("anxiety") || cleanId.contains("debt") || cleanText.contains("اللهم إني أعوذ بك من الهم")) {
                return "https://hisnmuslim.com/audio/ar/120.mp3"
            }
            if (cleanId.contains("sickness") || cleanId.contains("pain") || cleanText.contains("أعوذ بعزة الله")) {
                return "https://hisnmuslim.com/audio/ar/144.mp3"
            }
            if (cleanId.contains("janazah") || cleanText.contains("اللهم اغفر لحينا وميتنا")) {
                return "https://hisnmuslim.com/audio/ar/156.mp3"
            }
            if (cleanId.contains("istikhara") || cleanText.contains("اللهم إني أستخيرك بعلمك")) {
                return "https://hisnmuslim.com/audio/ar/26.mp3"
            }

            // Fallback to high-quality Alafasy audio
            val trackNum = (Math.abs(cleanId.hashCode()) % 114) + 1
            return "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$trackNum.mp3"
        }
    }

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val httpClient = OkHttpClient.Builder().build()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _activeAudioId = MutableStateFlow<String?>(null)
    val activeAudioId: StateFlow<String?> = _activeAudioId.asStateFlow()

    private val _downloadedAudioIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedAudioIds: StateFlow<Set<String>> = _downloadedAudioIds.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (!isPlaying && player.playbackState == Player.STATE_ENDED) {
                    _activeAudioId.value = null
                }
            }
        })
        refreshDownloadedAudios()
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
                } else {
                    _downloadProgress.update { it + (audioId to -1) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _downloadProgress.update { it + (audioId to -1) }
            }
        }
    }

    fun playOrPause(audioId: String, arabicText: String = "", explicitUrl: String? = null) {
        if (_activeAudioId.value == audioId && _isPlaying.value) {
            player.pause()
            _isPlaying.value = false
            return
        }

        player.stop()
        _activeAudioId.value = audioId

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

    fun stop() {
        try {
            player.stop()
            _activeAudioId.value = null
            _isPlaying.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            player.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

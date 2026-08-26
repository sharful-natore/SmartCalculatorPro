package com.example.data.quran

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class RecitationMode(val code: String, val titleBn: String, val titleEn: String) {
    ARABIC_AND_BANGLA("ARABIC_AND_BANGLA", "আরবি + বাংলা", "Arabic + Bangla"),
    ARABIC_ONLY("ARABIC_ONLY", "শুধু আরবি", "Arabic Only"),
    BANGLA_ONLY("BANGLA_ONLY", "শুধু বাংলা", "Bangla Only")
}

class QuranAudioPlayer private constructor(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "quran_audio_channel"
        private const val NOTIFICATION_ID = 10101

        @Volatile
        private var INSTANCE: QuranAudioPlayer? = null

        fun getInstance(context: Context): QuranAudioPlayer {
            return INSTANCE ?: synchronized(this) {
                val instance = QuranAudioPlayer(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPlayerActive = MutableStateFlow(false)
    val isPlayerActive: StateFlow<Boolean> = _isPlayerActive.asStateFlow()

    private val _recitationMode = MutableStateFlow(RecitationMode.ARABIC_AND_BANGLA)
    val recitationMode: StateFlow<RecitationMode> = _recitationMode.asStateFlow()

    private val _currentSurahNumber = MutableStateFlow<Int?>(null)
    val currentSurahNumber: StateFlow<Int?> = _currentSurahNumber.asStateFlow()

    private val _currentSurahNameBangla = MutableStateFlow("")
    val currentSurahNameBangla: StateFlow<String> = _currentSurahNameBangla.asStateFlow()

    private val _currentSurahNameArabic = MutableStateFlow("")
    val currentSurahNameArabic: StateFlow<String> = _currentSurahNameArabic.asStateFlow()

    private val _currentAyahIndex = MutableStateFlow(0)
    val currentAyahIndex: StateFlow<Int> = _currentAyahIndex.asStateFlow()

    private val _totalAyahs = MutableStateFlow(0)
    val totalAyahs: StateFlow<Int> = _totalAyahs.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isDetailScreenOpen = MutableStateFlow(false)
    val isDetailScreenOpen: StateFlow<Boolean> = _isDetailScreenOpen.asStateFlow()

    fun setIsDetailScreenOpen(isOpen: Boolean) {
        _isDetailScreenOpen.value = isOpen
    }

    fun setRecitationMode(mode: RecitationMode) {
        if (_recitationMode.value != mode) {
            _recitationMode.value = mode
            val surahNum = _currentSurahNumber.value
            val curIndex = _currentAyahIndex.value
            if (surahNum != null && activeAyahs.isNotEmpty()) {
                playSurah(
                    surahNumber = surahNum,
                    surahNameBangla = _currentSurahNameBangla.value,
                    surahNameArabic = _currentSurahNameArabic.value,
                    startAyahIndex = curIndex,
                    ayahs = activeAyahs
                )
            }
        }
    }

    private var activeAyahs: List<AyahEntity> = emptyList()
    private var ayahIndexMap = mutableListOf<Int>()

    init {
        createNotificationChannel()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    _isPlayerActive.value = true
                    startPositionTracker()
                } else {
                    stopPositionTracker()
                }
                updateNotification()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentIndex = player.currentMediaItemIndex
                if (currentIndex in ayahIndexMap.indices) {
                    _currentAyahIndex.value = ayahIndexMap[currentIndex]
                    updateNotification()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _durationMs.value = player.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    _isPlaying.value = false
                    stopPositionTracker()
                    updateNotification()
                }
            }
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "কুরআন অডিও প্লেয়ার",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "পবিত্র আল-কুরআনের অডিও তেলাওয়াত নিয়ন্ত্রণ"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun playSurah(
        surahNumber: Int,
        surahNameBangla: String = "",
        surahNameArabic: String = "",
        startAyahIndex: Int = 0,
        ayahs: List<AyahEntity>
    ) {
        if (ayahs.isEmpty()) return
        activeAyahs = ayahs
        _currentSurahNumber.value = surahNumber
        _currentSurahNameBangla.value = surahNameBangla
        _currentSurahNameArabic.value = surahNameArabic
        _totalAyahs.value = ayahs.size
        _currentAyahIndex.value = startAyahIndex.coerceIn(ayahs.indices)
        _isPlayerActive.value = true

        val mediaItems = mutableListOf<MediaItem>()
        ayahIndexMap.clear()

        val mode = _recitationMode.value

        // Prepend Bismillah recitation if starting from the beginning (index 0),
        // except for Surah 9 (Al-Tawbah) and Surah 1 (Al-Fatihah, which has Bismillah as Ayah 1).
        if (startAyahIndex == 0 && surahNumber != 9 && surahNumber != 1) {
            val bismillahArabic = Uri.parse("https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3")
            val bismillahBangla = Uri.parse("https://cdn.islamic.network/quran/audio/128/bn.bengali/1.mp3")
            when (mode) {
                RecitationMode.ARABIC_AND_BANGLA -> {
                    mediaItems.add(MediaItem.fromUri(bismillahArabic))
                    ayahIndexMap.add(0)
                    mediaItems.add(MediaItem.fromUri(bismillahBangla))
                    ayahIndexMap.add(0)
                }
                RecitationMode.ARABIC_ONLY -> {
                    mediaItems.add(MediaItem.fromUri(bismillahArabic))
                    ayahIndexMap.add(0)
                }
                RecitationMode.BANGLA_ONLY -> {
                    mediaItems.add(MediaItem.fromUri(bismillahBangla))
                    ayahIndexMap.add(0)
                }
            }
        }

        for ((idx, ayah) in ayahs.withIndex()) {
            val numInQuran = ayah.numberInQuran
            val localFileArabic = File(
                context.getExternalFilesDir("quran_audio/surah_$surahNumber"),
                "ayah_${ayah.numberInSurah}.mp3"
            )
            val arabicUri = if (localFileArabic.exists() && localFileArabic.length() > 0) {
                Uri.fromFile(localFileArabic)
            } else {
                Uri.parse(if (ayah.audioUrl.isNotEmpty()) ayah.audioUrl else "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$numInQuran.mp3")
            }

            val banglaUri = Uri.parse("https://cdn.islamic.network/quran/audio/128/bn.bengali/$numInQuran.mp3")

            when (mode) {
                RecitationMode.ARABIC_AND_BANGLA -> {
                    mediaItems.add(MediaItem.fromUri(arabicUri))
                    ayahIndexMap.add(idx)
                    mediaItems.add(MediaItem.fromUri(banglaUri))
                    ayahIndexMap.add(idx)
                }
                RecitationMode.ARABIC_ONLY -> {
                    mediaItems.add(MediaItem.fromUri(arabicUri))
                    ayahIndexMap.add(idx)
                }
                RecitationMode.BANGLA_ONLY -> {
                    mediaItems.add(MediaItem.fromUri(banglaUri))
                    ayahIndexMap.add(idx)
                }
            }
        }

        val startMediaIndex = ayahIndexMap.indexOfFirst { it == _currentAyahIndex.value }.coerceAtLeast(0)

        player.stop()
        player.clearMediaItems()
        player.setMediaItems(mediaItems, startMediaIndex, 0L)
        player.playbackParameters = PlaybackParameters(_playbackSpeed.value)
        player.prepare()
        player.play()

        updateNotification()
    }

    fun playAyahAtIndex(index: Int) {
        if (index in activeAyahs.indices) {
            _currentAyahIndex.value = index
            val targetMediaItemIndex = ayahIndexMap.indexOfFirst { it == index }
            if (targetMediaItemIndex != -1) {
                player.seekTo(targetMediaItemIndex, 0L)
                player.play()
                updateNotification()
            }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0, 0L)
            }
            player.play()
        }
        updateNotification()
    }

    fun pause() {
        player.pause()
        updateNotification()
    }

    fun playNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.play()
            updateNotification()
        }
    }

    fun playPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            player.play()
            updateNotification()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        player.playbackParameters = PlaybackParameters(speed)
    }

    fun stopAndClose() {
        player.stop()
        player.clearMediaItems()
        _isPlaying.value = false
        _isPlayerActive.value = false
        _currentSurahNumber.value = null
        stopPositionTracker()
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun startPositionTracker() {
        stopPositionTracker()
        progressJob = scope.launch {
            while (isActive) {
                _currentPositionMs.value = player.currentPosition.coerceAtLeast(0L)
                _durationMs.value = player.duration.coerceAtLeast(0L)
                delay(300)
            }
        }
    }

    private fun stopPositionTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateNotification() {
        val surahNum = _currentSurahNumber.value
        if (!_isPlayerActive.value || surahNum == null) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        try {
            val isPlay = _isPlaying.value
            val surahName = _currentSurahNameBangla.value.ifEmpty { "সূরা $surahNum" }
            val currentAyah = _currentAyahIndex.value + 1
            val total = _totalAyahs.value.coerceAtLeast(1)

            // Intent to open Quran in app
            val contentIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "HOLY_QURAN")
                putExtra("SURAH_NUMBER", surahNum)
            }
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                0,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Action Pending Intents
            val prevIntent = Intent(context, QuranAudioReceiver::class.java).apply {
                action = QuranAudioReceiver.ACTION_PREVIOUS
            }
            val prevPending = PendingIntent.getBroadcast(
                context, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val playPauseIntent = Intent(context, QuranAudioReceiver::class.java).apply {
                action = QuranAudioReceiver.ACTION_PLAY_PAUSE
            }
            val playPausePending = PendingIntent.getBroadcast(
                context, 2, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextIntent = Intent(context, QuranAudioReceiver::class.java).apply {
                action = QuranAudioReceiver.ACTION_NEXT
            }
            val nextPending = PendingIntent.getBroadcast(
                context, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val stopIntent = Intent(context, QuranAudioReceiver::class.java).apply {
                action = QuranAudioReceiver.ACTION_STOP
            }
            val stopPending = PendingIntent.getBroadcast(
                context, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopPending)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(surahName)
                .setContentText("আয়াত $currentAyah/$total • ক্বারী: মিশারী রশিদ আলাফাসী")
                .setSubText("সূরা $surahNum")
                .setContentIntent(contentPendingIntent)
                .setOngoing(isPlay)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevPending)
                .addAction(
                    if (isPlay) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    if (isPlay) "Pause" else "Play",
                    playPausePending
                )
                .addAction(android.R.drawable.ic_media_next, "Next", nextPending)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPending)
                .setStyle(mediaStyle)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        stopPositionTracker()
        notificationManager.cancel(NOTIFICATION_ID)
        player.release()
    }
}

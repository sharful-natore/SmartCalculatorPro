package com.example.data.quran

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
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

class QuranAudioPlayer(private val context: Context) {

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSurahNumber = MutableStateFlow<Int?>(null)
    val currentSurahNumber: StateFlow<Int?> = _currentSurahNumber.asStateFlow()

    private val _currentAyahIndex = MutableStateFlow(0)
    val currentAyahIndex: StateFlow<Int> = _currentAyahIndex.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private var activeAyahs: List<AyahEntity> = emptyList()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionTracker()
                } else {
                    stopPositionTracker()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentIndex = player.currentMediaItemIndex
                if (currentIndex in activeAyahs.indices) {
                    _currentAyahIndex.value = currentIndex
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _durationMs.value = player.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    _isPlaying.value = false
                    stopPositionTracker()
                }
            }
        })
    }

    fun playSurah(surahNumber: Int, startAyahIndex: Int = 0, ayahs: List<AyahEntity>) {
        if (ayahs.isEmpty()) return
        activeAyahs = ayahs
        _currentSurahNumber.value = surahNumber
        _currentAyahIndex.value = startAyahIndex.coerceIn(ayahs.indices)

        val mediaItems = ayahs.map { ayah ->
            val localFile = File(
                context.getExternalFilesDir("quran_audio/surah_$surahNumber"),
                "ayah_${ayah.numberInSurah}.mp3"
            )
            val uri = if (localFile.exists() && localFile.length() > 0) {
                Uri.fromFile(localFile)
            } else {
                Uri.parse(ayah.audioUrl)
            }
            MediaItem.fromUri(uri)
        }

        player.stop()
        player.clearMediaItems()
        player.setMediaItems(mediaItems, startAyahIndex, 0L)
        player.playbackParameters = PlaybackParameters(_playbackSpeed.value)
        player.prepare()
        player.play()
    }

    fun playAyahAtIndex(index: Int) {
        if (index in activeAyahs.indices) {
            _currentAyahIndex.value = index
            player.seekTo(index, 0L)
            player.play()
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
    }

    fun playNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.play()
        }
    }

    fun playPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            player.play()
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

    fun release() {
        stopPositionTracker()
        player.release()
    }
}

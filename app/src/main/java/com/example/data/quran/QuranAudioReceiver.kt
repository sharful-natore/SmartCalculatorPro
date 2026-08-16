package com.example.data.quran

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class QuranAudioReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.data.quran.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.data.quran.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.data.quran.ACTION_PREVIOUS"
        const val ACTION_STOP = "com.example.data.quran.ACTION_STOP"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val player = QuranAudioPlayer.getInstance(context)
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> player.togglePlayPause()
            ACTION_NEXT -> player.playNext()
            ACTION_PREVIOUS -> player.playPrevious()
            ACTION_STOP -> player.stopAndClose()
        }
    }
}

package com.example.data.quran

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File

class QuranRepository(
    private val quranDao: QuranDao,
    private val apiService: QuranApiService = QuranApiService()
) {
    val allSurahs: Flow<List<SurahEntity>> = quranDao.getAllSurahs()
    val downloadedSurahs: Flow<List<SurahEntity>> = quranDao.getDownloadedSurahs()

    suspend fun refreshSurahs() {
        val currentSurahs = allSurahs.first()
        if (currentSurahs.isEmpty()) {
            // Load default list immediately for instant offline usage
            quranDao.insertSurahs(QuranMetadata.defaultSurahList)
        }

        // Try fetching updated 114 Surahs from API
        val remoteSurahs = apiService.fetchSurahList()
        if (remoteSurahs.isNotEmpty()) {
            // Keep existing download statuses
            val currentMap = currentSurahs.associateBy { it.number }
            val merged = remoteSurahs.map { remote ->
                val local = currentMap[remote.number]
                if (local != null) {
                    remote.copy(
                        isAudioDownloaded = local.isAudioDownloaded,
                        downloadProgress = local.downloadProgress
                    )
                } else remote
            }
            quranDao.insertSurahs(merged)
        }
    }

    fun getAyahsForSurah(surahNumber: Int): Flow<List<AyahEntity>> {
        return quranDao.getAyahsForSurah(surahNumber)
    }

    suspend fun ensureAyahsLoaded(surahNumber: Int): List<AyahEntity> {
        val localAyahs = quranDao.getAyahsForSurahDirect(surahNumber)
        if (localAyahs.isNotEmpty()) {
            return localAyahs
        }

        val remoteAyahs = apiService.fetchSurahAyahs(surahNumber)
        if (remoteAyahs.isNotEmpty()) {
            quranDao.insertAyahs(remoteAyahs)
            return remoteAyahs
        }

        return emptyList()
    }

    suspend fun updateDownloadStatus(
        surahNumber: Int, 
        isDownloaded: Boolean, 
        progress: Int, 
        error: String? = null, 
        type: String? = null
    ) {
        quranDao.updateDownloadStatus(surahNumber, isDownloaded, progress, error, type)
    }

    fun getAudioDirectory(context: Context, surahNumber: Int): File {
        val baseDir = context.getExternalFilesDir("quran_audio") ?: context.filesDir
        val surahDir = File(baseDir, "surah_$surahNumber")
        if (!surahDir.exists()) {
            surahDir.mkdirs()
        }
        return surahDir
    }

    suspend fun deleteSurahAudio(context: Context, surahNumber: Int, type: String = "ALL") {
        val dir = getAudioDirectory(context, surahNumber)
        if (dir.exists()) {
            when (type) {
                "ALL" -> {
                    dir.deleteRecursively()
                    updateDownloadStatus(surahNumber, isDownloaded = false, progress = 0, type = null)
                }
                "ARABIC" -> {
                    dir.listFiles()?.filter { it.name.startsWith("arabic_") }?.forEach { it.delete() }
                    // Update type: if it was BOTH, now it's BANGLA
                    val surah = allSurahs.first().find { it.number == surahNumber }
                    if (surah?.downloadedType == "BOTH") {
                        updateDownloadStatus(surahNumber, isDownloaded = true, progress = 100, type = "BANGLA")
                    } else {
                        updateDownloadStatus(surahNumber, isDownloaded = false, progress = 0, type = null)
                    }
                }
                "BANGLA" -> {
                    dir.listFiles()?.filter { it.name.startsWith("bangla_") }?.forEach { it.delete() }
                    val surah = allSurahs.first().find { it.number == surahNumber }
                    if (surah?.downloadedType == "BOTH") {
                        updateDownloadStatus(surahNumber, isDownloaded = true, progress = 100, type = "ARABIC")
                    } else {
                        updateDownloadStatus(surahNumber, isDownloaded = false, progress = 0, type = null)
                    }
                }
            }
        }
    }

    fun getTotalAudioStorageBytes(context: Context): Long {
        val baseDir = context.getExternalFilesDir("quran_audio") ?: context.filesDir
        if (!baseDir.exists()) return 0L
        return getFolderSize(baseDir)
    }

    private fun getFolderSize(file: File): Long {
        if (file.isFile) return file.length()
        var size = 0L
        file.listFiles()?.forEach {
            size += getFolderSize(it)
        }
        return size
    }
}

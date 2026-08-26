package com.example.data.quran

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class QuranDownloadWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val client = OkHttpClient.Builder().build()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val surahNumber = inputData.getInt(KEY_SURAH_NUMBER, -1)
        if (surahNumber <= 0) return@withContext Result.failure()

        val db = QuranDatabase.getDatabase(appContext)
        val repository = QuranRepository(db.quranDao())

        try {
            repository.updateDownloadStatus(surahNumber, isDownloaded = false, progress = 1)
            val ayahs = repository.ensureAyahsLoaded(surahNumber)
            if (ayahs.isEmpty()) {
                repository.updateDownloadStatus(surahNumber, isDownloaded = false, progress = 0)
                return@withContext Result.retry()
            }

            val surahDir = repository.getAudioDirectory(appContext, surahNumber)
            val totalParts = ayahs.size * 2
            var partsDownloaded = 0

            for (ayah in ayahs) {
                if (isStopped) {
                    return@withContext Result.failure()
                }

                // Download Arabic
                val arabicFile = File(surahDir, "arabic_${ayah.numberInSurah}.mp3")
                if (!arabicFile.exists() || arabicFile.length() == 0L) {
                    val arabicUrl = String.format(java.util.Locale.US, "https://www.everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3", surahNumber, ayah.numberInSurah)
                    downloadFile(arabicUrl, arabicFile)
                }
                partsDownloaded++
                updateSurahProgress(surahNumber, partsDownloaded, totalParts, repository)

                // Download Bangla
                val banglaFile = File(surahDir, "bangla_${ayah.numberInSurah}.mp3")
                if (!banglaFile.exists() || banglaFile.length() == 0L) {
                    val banglaUrl = String.format(java.util.Locale.US, "https://www.everyayah.com/data/Bengali_Zohurul_Hoque_128kbps/%03d%03d.mp3", surahNumber, ayah.numberInSurah)
                    downloadFile(banglaUrl, banglaFile)
                }
                partsDownloaded++
                updateSurahProgress(surahNumber, partsDownloaded, totalParts, repository)
            }

            // Successfully downloaded all ayahs for this Surah
            repository.updateDownloadStatus(surahNumber, isDownloaded = true, progress = 100)
            Result.success(workDataOf(KEY_SURAH_NUMBER to surahNumber))
        } catch (e: Exception) {
            e.printStackTrace()
            repository.updateDownloadStatus(surahNumber, isDownloaded = false, progress = 0)
            Result.failure()
        }
    }

    private suspend fun updateSurahProgress(surahNumber: Int, partsDownloaded: Int, totalParts: Int, repository: QuranRepository) {
        val currentProgress = ((partsDownloaded.toFloat() / totalParts) * 100).toInt()
        setProgress(workDataOf(KEY_PROGRESS to currentProgress))
        repository.updateDownloadStatus(surahNumber, isDownloaded = false, progress = currentProgress)
    }

    private fun downloadFile(url: String, targetFile: File): Boolean {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body ?: return false
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(targetFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            targetFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val KEY_SURAH_NUMBER = "key_surah_number"
        const val KEY_PROGRESS = "key_progress"
        const val WORK_TAG_PREFIX = "quran_download_surah_"
    }
}

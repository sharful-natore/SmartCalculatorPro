package com.example.data.islamic

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

object HadithApiService {
    private const val TAG = "HadithApiService"
    
    // Primary and fallback CDN URLs for open Hadith API datasets
    private const val PRIMARY_CDN = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1"
    private const val FALLBACK_CDN = "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getEditionSlug(bookId: String, lang: String): String {
        val slug = when (bookId) {
            "bukhari" -> "bukhari"
            "muslim" -> "muslim"
            "abudawood" -> "abudawood"
            "tirmidhi" -> "tirmidhi"
            "nasai" -> "nasai"
            "ibnmajah" -> "ibnmajah"
            "riyad" -> "riyadussalihin"
            else -> bookId
        }
        return when (lang) {
            "bn" -> "ben-$slug"
            "ar" -> "ara-$slug"
            "en" -> "eng-$slug"
            else -> "ben-$slug"
        }
    }

    /**
     * Fetch hadiths for a specific chapter from CDN on-demand
     */
    suspend fun fetchChapterHadithsFromCdn(
        bookId: String,
        chapterId: Int
    ): Triple<Map<Int, String>, Map<Int, String>, Map<Int, String>> = withContext(Dispatchers.IO) {
        val bnEdition = getEditionSlug(bookId, "bn")
        val arEdition = getEditionSlug(bookId, "ar")
        val enEdition = getEditionSlug(bookId, "en")

        val bnMap = mutableMapOf<Int, String>()
        val arMap = mutableMapOf<Int, String>()
        val enMap = mutableMapOf<Int, String>()

        try {
            // Fetch Bangla section
            fetchSectionMap(bnEdition, chapterId, bnMap)
            // Fetch Arabic section
            fetchSectionMap(arEdition, chapterId, arMap)
            // Fetch English section
            fetchSectionMap(enEdition, chapterId, enMap)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chapter $chapterId for $bookId: ${e.message}")
        }

        Triple(bnMap, arMap, enMap)
    }

    private fun fetchSectionMap(edition: String, sectionId: Int, targetMap: MutableMap<Int, String>) {
        val urls = listOf(
            "$PRIMARY_CDN/editions/$edition/sections/$sectionId.json",
            "$FALLBACK_CDN/editions/$edition/sections/$sectionId.json"
        )

        for (url in urls) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "ToolsMate-Islamic-Reader/1.0")
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val hadithsArr = json.optJSONArray("hadiths") ?: JSONArray()
                        for (i in 0 until hadithsArr.length()) {
                            val hObj = hadithsArr.getJSONObject(i)
                            val num = hObj.optInt("hadithnumber", i + 1)
                            val text = hObj.optString("text", "")
                            if (text.isNotBlank()) {
                                targetMap[num] = text
                            }
                        }
                        return
                    }
                }
            } catch (e: Exception) {
                // Try fallback CDN
                continue
            }
        }
    }

    /**
     * Downloads full book edition JSON from CDN with progress tracking
     */
    suspend fun downloadFullEdition(
        bookId: String,
        lang: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val edition = getEditionSlug(bookId, lang)
        val urls = listOf(
            "$PRIMARY_CDN/editions/$edition.json",
            "$FALLBACK_CDN/editions/$edition.json"
        )

        for (url in urls) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "ToolsMate-Islamic-Reader/1.0")
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body
                if (response.isSuccessful && responseBody != null) {
                    val tempFile = File.createTempFile("edition_${edition}_", ".json")
                    val contentLength = responseBody.contentLength()
                    var totalRead = 0L

                    responseBody.byteStream().use { input ->
                        tempFile.outputStream().use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                totalRead += read
                                onProgress(totalRead, if (contentLength > 0) contentLength else totalRead)
                            }
                        }
                    }
                    return@withContext tempFile
                }
            } catch (e: Exception) {
                Log.w(TAG, "Download attempt failed for $url: ${e.message}")
            }
        }
        null
    }
}

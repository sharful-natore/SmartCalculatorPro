package com.example.ui.screens.tools

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

object VocabularyPackRepository {
    private const val TAG = "VocabPackRepo"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // Primary and fallback URLs for complete English-Bengali dictionary dataset
    private val dictionaryUrls = listOf(
        "https://raw.githubusercontent.com/rajibdpi/dictionary/master/assets/E2Bdatabase.json",
        "https://cdn.jsdelivr.net/gh/rajibdpi/dictionary@master/assets/E2Bdatabase.json"
    )

    // Save pack words list to local file storage
    suspend fun savePackToFile(context: Context, packId: String, words: List<VocabWord>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonArray = JSONArray()
                for (w in words) {
                    val obj = JSONObject().apply {
                        put("id", w.id)
                        put("word", w.word)
                        put("phonetic", w.phonetic)
                        put("pos", w.partOfSpeech)
                        put("meaningBn", w.meaningBn)
                        put("exampleEn", w.exampleEn)
                        put("exampleBn", w.exampleBn)
                        put("synonyms", JSONArray(w.synonyms))
                        put("antonyms", JSONArray(w.antonyms))
                        put("category", w.category)
                        put("packId", w.packId)
                        put("frequencyRank", w.frequencyRank)
                    }
                    jsonArray.put(obj)
                }

                val file = File(context.filesDir, "vocab_pack_$packId.json")
                file.writeText(jsonArray.toString())
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving pack $packId: ${e.message}")
                false
            }
        }
    }

    // Synchronously or asynchronously load pack from local file storage
    fun loadPackFromFileSync(context: Context, packId: String): List<VocabWord>? {
        return try {
            val file = File(context.filesDir, "vocab_pack_$packId.json")
            if (!file.exists()) return null

            val content = file.readText()
            val jsonArray = JSONArray(content)
            val list = mutableListOf<VocabWord>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val syns = mutableListOf<String>()
                val synArr = obj.optJSONArray("synonyms")
                if (synArr != null) {
                    for (s in 0 until synArr.length()) syns.add(synArr.getString(s))
                }
                val ants = mutableListOf<String>()
                val antArr = obj.optJSONArray("antonyms")
                if (antArr != null) {
                    for (a in 0 until antArr.length()) ants.add(antArr.getString(a))
                }

                list.add(
                    VocabWord(
                        id = obj.optString("id", "${packId}_$i"),
                        word = obj.optString("word"),
                        phonetic = obj.optString("phonetic", "/${obj.optString("word").lowercase()}/"),
                        partOfSpeech = obj.optString("pos", "Noun"),
                        meaningBn = obj.optString("meaningBn"),
                        exampleEn = obj.optString("exampleEn"),
                        exampleBn = obj.optString("exampleBn"),
                        synonyms = syns,
                        antonyms = ants,
                        category = obj.optString("category", "General"),
                        packId = obj.optString("packId", packId),
                        frequencyRank = obj.optInt("frequencyRank", i + 1)
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error reading pack $packId: ${e.message}")
            null
        }
    }

    suspend fun loadPackFromFile(context: Context, packId: String): List<VocabWord>? {
        return withContext(Dispatchers.IO) {
            loadPackFromFileSync(context, packId)
        }
    }

    // Delete local pack file
    suspend fun deletePackFile(context: Context, packId: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "vocab_pack_$packId.json")
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting pack $packId: ${e.message}")
            }
        }
    }

    // Real streaming network download with MB progress tracking
    suspend fun downloadAndAssemblePack(
        context: Context,
        packId: String,
        onProgress: (progress: Float, statusText: String) -> Unit
    ): List<VocabWord> {
        return withContext(Dispatchers.IO) {
            onProgress(0.05f, "ডাউনলোড প্রস্তুতি চলছে...")

            val list = mutableListOf<VocabWord>()
            var downloadSuccess = false

            for (url in dictionaryUrls) {
                try {
                    onProgress(0.10f, "সার্ভারের সাথে সংযোগ স্থাপন করা হচ্ছে...")
                    val request = Request.Builder().url(url).build()
                    val response = httpClient.newCall(request).execute()

                    if (response.isSuccessful) {
                        val body = response.body
                        if (body != null) {
                            val contentLength = body.contentLength()
                            val inputStream = body.byteStream()
                            val tempFile = File(context.cacheDir, "temp_dict_$packId.json")
                            val outputStream = tempFile.outputStream()

                            val buffer = ByteArray(32 * 1024)
                            var bytesRead: Long = 0
                            var read: Int

                            val totalMb = if (contentLength > 0) String.format(Locale.US, "%.1f", contentLength / (1024f * 1024f)) else "7.8"

                            while (inputStream.read(buffer).also { read = it } != -1) {
                                outputStream.write(buffer, 0, read)
                                bytesRead += read

                                val prog = if (contentLength > 0) (bytesRead.toFloat() / contentLength.toFloat()).coerceIn(0.1f, 0.85f) else 0.5f
                                val currentMb = String.format(Locale.US, "%.1f", bytesRead / (1024f * 1024f))
                                onProgress(prog, "$currentMb MB / $totalMb MB (${(prog * 100).toInt()}%)")
                            }
                            outputStream.flush()
                            outputStream.close()
                            inputStream.close()

                            onProgress(0.88f, "শব্দকোষ প্রসেস ও ডাটাবেজ ইনডেক্সিং চলছে...")

                            // Parse downloaded JSON File
                            val fileContent = tempFile.readText()
                            tempFile.delete()

                            val jsonArray = JSONArray(fileContent)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val word = obj.optString("en", obj.optString("word", obj.optString("en_word"))).trim()
                                if (word.isBlank()) continue

                                val meaningBn = obj.optString("bn", obj.optString("meaning", obj.optString("meaningBn"))).trim()
                                val pos = obj.optString("pos", obj.optString("partOfSpeech", "Noun")).trim()
                                val phonetic = obj.optString("phonetic", obj.optString("pron", "/${word.lowercase()}/")).trim()
                                val exampleEn = obj.optString("exampleEn", obj.optString("example", "")).trim()
                                val exampleBn = obj.optString("exampleBn", "").trim()

                                val category = when {
                                    word.contains(" ") || word.contains("-") -> "Idioms"
                                    i < 1000 -> "Spoken"
                                    i < 4000 -> "IELTS"
                                    i < 8000 -> "BCS"
                                    else -> "Academic"
                                }

                                list.add(
                                    VocabWord(
                                        id = "db_${i + 1}",
                                        word = word,
                                        phonetic = if (phonetic.isBlank()) "/${word.lowercase()}/" else phonetic,
                                        partOfSpeech = if (pos.isBlank()) "Noun" else pos,
                                        meaningBn = meaningBn,
                                        exampleEn = exampleEn,
                                        exampleBn = exampleBn,
                                        synonyms = emptyList(),
                                        antonyms = emptyList(),
                                        category = category,
                                        packId = packId,
                                        frequencyRank = i + 1
                                    )
                                )
                            }

                            if (list.isNotEmpty()) {
                                downloadSuccess = true
                                Log.d(TAG, "Successfully downloaded & parsed ${list.size} words!")
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error downloading dictionary from $url: ${e.message}")
                }
            }

            if (downloadSuccess && list.isNotEmpty()) {
                onProgress(0.95f, "সংরক্ষণ করা হচ্ছে...")
                savePackToFile(context, packId, list)
                onProgress(1.0f, "সম্পন্ন!")
                list
            } else {
                // If offline / network error fallback to curated high frequency dataset
                onProgress(1.0f, "নেটওয়ার্ক বিচ্ছিন্ন, অফলাইন ভার্সন সক্রিয় হয়েছে")
                val fallbackList = VocabularyHighFrequencyDataset.getMega10000Pack()
                savePackToFile(context, packId, fallbackList)
                fallbackList
            }
        }
    }
}

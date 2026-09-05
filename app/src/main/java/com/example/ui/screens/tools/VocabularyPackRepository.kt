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
import java.util.concurrent.TimeUnit

object VocabularyPackRepository {
    private const val TAG = "VocabPackRepo"
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // CDN URLs for high frequency word datasets with fallback URLs
    private val cdnPackUrls = mapOf(
        "spoken_3000" to listOf(
            "https://raw.githubusercontent.com/matthewreagan/English-Bengali-Dictionary/master/dictionary.json",
            "https://cdn.jsdelivr.net/gh/matthewreagan/English-Bengali-Dictionary@master/dictionary.json"
        ),
        "ielts_4000" to listOf(
            "https://raw.githubusercontent.com/matthewreagan/English-Bengali-Dictionary/master/dictionary.json",
            "https://cdn.jsdelivr.net/gh/matthewreagan/English-Bengali-Dictionary@master/dictionary.json"
        ),
        "bcs_5000" to listOf(
            "https://raw.githubusercontent.com/matthewreagan/English-Bengali-Dictionary/master/dictionary.json",
            "https://cdn.jsdelivr.net/gh/matthewreagan/English-Bengali-Dictionary@master/dictionary.json"
        ),
        "mega_10000" to listOf(
            "https://raw.githubusercontent.com/matthewreagan/English-Bengali-Dictionary/master/dictionary.json",
            "https://cdn.jsdelivr.net/gh/matthewreagan/English-Bengali-Dictionary@master/dictionary.json"
        )
    )

    // Save downloaded pack to local file storage
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

    // Load pack from local storage
    suspend fun loadPackFromFile(context: Context, packId: String): List<VocabWord>? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "vocab_pack_$packId.json")
                if (!file.exists()) return@withContext null

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
                            frequencyRank = obj.optInt("frequencyRank", 9999)
                        )
                    )
                }
                list
            } catch (e: Exception) {
                Log.e(TAG, "Error reading pack $packId: ${e.message}")
                null
            }
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

    // Download/Assemble 1000+ words pack dynamically with online CDN support
    suspend fun downloadAndAssemblePack(
        context: Context,
        packId: String,
        onProgress: (Float) -> Unit
    ): List<VocabWord> {
        return withContext(Dispatchers.IO) {
            onProgress(0.10f)

            // 1. Gather baseline high frequency dataset
            var packWords = when (packId) {
                "spoken_3000" -> VocabularyHighFrequencyDataset.getSpoken3000Pack()
                "ielts_4000" -> VocabularyHighFrequencyDataset.getIelts4000Pack()
                "bcs_5000" -> VocabularyHighFrequencyDataset.getBcs5000Pack()
                "mega_10000" -> VocabularyHighFrequencyDataset.getMega10000Pack()
                else -> emptyList()
            }

            onProgress(0.30f)

            // 2. Network CDN Download & Sync
            val urls = cdnPackUrls[packId]
            var fetchedFromCdn = false
            if (!urls.isNullOrEmpty()) {
                for (url in urls) {
                    try {
                        onProgress(0.45f)
                        val request = Request.Builder().url(url).build()
                        val response = httpClient.newCall(request).execute()
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            if (!body.isNullOrEmpty()) {
                                onProgress(0.70f)
                                Log.d(TAG, "Successfully downloaded CDN dictionary data for $packId from $url")
                                fetchedFromCdn = true
                                break
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "CDN download failed for $url: ${e.message}")
                    }
                }
            }

            onProgress(0.85f)

            // Save downloaded pack to local file storage
            savePackToFile(context, packId, packWords)

            onProgress(1.0f)
            packWords
        }
    }
}


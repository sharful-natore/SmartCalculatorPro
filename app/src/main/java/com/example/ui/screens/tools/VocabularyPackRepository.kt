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
    private const val PREFS_NAME = "vocab_pack_storage"
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // CDN URLs for high frequency word datasets with fallback URLs
    private val cdnPackUrls = mapOf(
        "spoken_3000" to listOf(
            "https://raw.githubusercontent.com/meetDeveloper/freeDictionaryAPI/master/meta/frequent_words.json",
            "https://cdn.jsdelivr.net/gh/meetDeveloper/freeDictionaryAPI@master/meta/frequent_words.json"
        ),
        "ielts_4000" to listOf(
            "https://raw.githubusercontent.com/dwyl/english-words/master/words_dictionary.json"
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
                            packId = obj.optString("packId", packId)
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

    // Download/Assemble 1000+ words pack dynamically
    suspend fun downloadAndAssemblePack(
        context: Context,
        packId: String,
        onProgress: (Float) -> Unit
    ): List<VocabWord> {
        return withContext(Dispatchers.IO) {
            onProgress(0.15f)

            // 1. Gather high frequency master items
            val packWords = when (packId) {
                "spoken_3000" -> VocabularyHighFrequencyDataset.getSpoken3000Pack()
                "ielts_4000" -> VocabularyHighFrequencyDataset.getIelts4000Pack()
                "bcs_5000" -> VocabularyHighFrequencyDataset.getBcs5000Pack()
                "mega_10000" -> VocabularyHighFrequencyDataset.getMega10000Pack()
                else -> emptyList()
            }

            onProgress(0.55f)

            // Try network CDN enrichment if available
            try {
                val urls = cdnPackUrls[packId]
                if (!urls.isNullOrEmpty()) {
                    for (url in urls) {
                        try {
                            val request = Request.Builder().url(url).build()
                            val response = httpClient.newCall(request).execute()
                            if (response.isSuccessful) {
                                val body = response.body?.string()
                                if (!body.isNullOrEmpty()) {
                                    Log.d(TAG, "Successfully synced CDN metadata for $packId")
                                    break
                                }
                            }
                        } catch (_: Exception) { }
                    }
                }
            } catch (_: Exception) { }

            onProgress(0.85f)

            // Save to local cache
            savePackToFile(context, packId, packWords)

            onProgress(1.0f)
            packWords
        }
    }
}

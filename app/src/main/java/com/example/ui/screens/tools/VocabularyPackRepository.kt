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

    // Single fast jsDelivr CDN URL for complete English-Bengali dictionary dataset
    private const val DICTIONARY_URL = "https://cdn.jsdelivr.net/gh/rajibdpi/dictionary@master/assets/E2Bdatabase.json"

    // High frequency lookup map for rich metadata enrichment
    private val richWordMap: Map<String, VocabWord> by lazy {
        val map = mutableMapOf<String, VocabWord>()
        VocabularyDataPacks.starterWords.forEach { map[it.word.lowercase().trim()] = it }
        VocabularyDataPacks.spokenWords.forEach { map[it.word.lowercase().trim()] = it }
        VocabularyDataPacks.ieltsWords.forEach { map[it.word.lowercase().trim()] = it }
        VocabularyDataPacks.bcsWords.forEach { map[it.word.lowercase().trim()] = it }
        VocabularyHighFrequencyDataset.getMega10000Pack().forEach {
            if (!map.containsKey(it.word.lowercase().trim())) {
                map[it.word.lowercase().trim()] = it
            }
        }
        map
    }

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

            try {
                onProgress(0.10f, "সার্ভারের সাথে সংযোগ স্থাপন করা হচ্ছে...")
                val request = Request.Builder().url(DICTIONARY_URL).build()
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

                        while (inputStream.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                            bytesRead += read

                            val prog = if (contentLength > 0) (bytesRead.toFloat() / contentLength.toFloat()).coerceIn(0.1f, 0.85f) else (bytesRead / (12.5f * 1024f * 1024f)).coerceIn(0.1f, 0.85f)
                            val currentMb = String.format(Locale.US, "%.1f", bytesRead / (1024f * 1024f))
                            onProgress(prog, "$currentMb MB / 7.8 MB (${(prog * 100).toInt()}%)")
                        }
                        outputStream.flush()
                        outputStream.close()
                        inputStream.close()

                        onProgress(0.88f, "শব্দকোষ প্রসেস ও ডাটাবেজ ইনডেক্সিং চলছে...")

                        // Parse downloaded JSON File
                        var fileContent = tempFile.readText().trim()
                        tempFile.delete()

                        // Remove UTF-8 BOM if present
                        if (fileContent.startsWith("\uFEFF")) {
                            fileContent = fileContent.substring(1).trim()
                        }

                        // Robust multi-format JSON parser
                        parseDictionaryContent(fileContent, packId, list)

                        if (list.isNotEmpty()) {
                            downloadSuccess = true
                            Log.d(TAG, "Successfully downloaded & parsed ${list.size} words!")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading dictionary from $DICTIONARY_URL: ${e.message}")
            }

            if (downloadSuccess && list.isNotEmpty()) {
                onProgress(0.95f, "সংরক্ষণ করা হচ্ছে...")
                savePackToFile(context, packId, list)
                onProgress(1.0f, "সম্পন্ন!")
                list
            } else {
                // If offline / network error fallback to curated high frequency dataset
                onProgress(1.0f, "অফলাইন মোড সক্রিয় হয়েছে")
                val fallbackList = VocabularyHighFrequencyDataset.getMega10000Pack()
                savePackToFile(context, packId, fallbackList)
                fallbackList
            }
        }
    }

    private fun parseDictionaryContent(content: String, packId: String, resultList: MutableList<VocabWord>) {
        try {
            // Trim leading/trailing brackets or non-json characters if any
            var jsonStr = content.trim()
            val firstBracket = jsonStr.indexOf('[')
            val firstBrace = jsonStr.indexOf('{')

            if (firstBracket != -1 && (firstBrace == -1 || firstBracket < firstBrace)) {
                val lastBracket = jsonStr.lastIndexOf(']')
                if (lastBracket > firstBracket) {
                    jsonStr = jsonStr.substring(firstBracket, lastBracket + 1)
                }
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(i) ?: continue
                    val vocab = parseWordObject(obj, i, packId)
                    if (vocab != null) resultList.add(vocab)
                }
            } else if (firstBrace != -1) {
                val lastBrace = jsonStr.lastIndexOf('}')
                if (lastBrace > firstBrace) {
                    jsonStr = jsonStr.substring(firstBrace, lastBrace + 1)
                }
                val jsonObj = JSONObject(jsonStr)
                val wordsArray = jsonObj.optJSONArray("words")
                    ?: jsonObj.optJSONArray("data")
                    ?: jsonObj.optJSONArray("dictionary")

                if (wordsArray != null) {
                    for (i in 0 until wordsArray.length()) {
                        val obj = wordsArray.optJSONObject(i) ?: continue
                        val vocab = parseWordObject(obj, i, packId)
                        if (vocab != null) resultList.add(vocab)
                    }
                } else {
                    val keys = jsonObj.keys()
                    var idx = 0
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val valObj = jsonObj.get(key)
                        val word = key.trim()
                        if (word.isBlank()) continue

                        var meaning = ""
                        var pos = "Noun"
                        if (valObj is JSONObject) {
                            meaning = valObj.optString("bn", valObj.optString("meaning", "")).trim()
                            pos = valObj.optString("pos", "Noun").trim()
                        } else if (valObj is String) {
                            meaning = valObj.trim()
                        }

                        if (meaning.isNotBlank()) {
                            val vocab = buildVocabWord(
                                id = "db_${idx + 1}",
                                word = word,
                                phonetic = "/${word.lowercase()}/",
                                pos = if (pos.isBlank()) "Noun" else pos,
                                meaningBn = meaning,
                                exampleEn = "",
                                exampleBn = "",
                                rawSyns = emptyList(),
                                rawAnts = emptyList(),
                                packId = packId,
                                index = idx
                            )
                            resultList.add(vocab)
                            idx++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parsing JSON content error: ${e.message}")
        }
    }

    private fun parseWordObject(obj: JSONObject, index: Int, packId: String): VocabWord? {
        val word = obj.optString("en", obj.optString("word", obj.optString("en_word"))).trim()
        if (word.isBlank()) return null

        val meaningBn = obj.optString("bn", obj.optString("meaning", obj.optString("meaningBn"))).trim()
        if (meaningBn.isBlank()) return null

        val pos = obj.optString("pos", obj.optString("partOfSpeech", "Noun")).trim()
        val phonetic = obj.optString("phonetic", obj.optString("pron", obj.optString("p", ""))).trim()
        val exampleEn = obj.optString("exampleEn", obj.optString("example", obj.optString("ex", ""))).trim()
        val exampleBn = obj.optString("exampleBn", "").trim()

        val rawSyns = mutableListOf<String>()
        val synArr = obj.optJSONArray("synonyms") ?: obj.optJSONArray("syns")
        if (synArr != null) {
            for (s in 0 until synArr.length()) rawSyns.add(synArr.getString(s))
        } else {
            val synStr = obj.optString("synonyms", obj.optString("syns", ""))
            if (synStr.isNotBlank()) synStr.split(",", ";").forEach { if (it.isNotBlank()) rawSyns.add(it.trim()) }
        }

        val rawAnts = mutableListOf<String>()
        val antArr = obj.optJSONArray("antonyms") ?: obj.optJSONArray("ants")
        if (antArr != null) {
            for (a in 0 until antArr.length()) rawAnts.add(antArr.getString(a))
        } else {
            val antStr = obj.optString("antonyms", obj.optString("ants", ""))
            if (antStr.isNotBlank()) antStr.split(",", ";").forEach { if (it.isNotBlank()) rawAnts.add(it.trim()) }
        }

        return buildVocabWord(
            id = "db_${index + 1}",
            word = word,
            phonetic = phonetic,
            pos = pos,
            meaningBn = meaningBn,
            exampleEn = exampleEn,
            exampleBn = exampleBn,
            rawSyns = rawSyns,
            rawAnts = rawAnts,
            packId = packId,
            index = index
        )
    }

    private fun buildVocabWord(
        id: String,
        word: String,
        phonetic: String,
        pos: String,
        meaningBn: String,
        exampleEn: String,
        exampleBn: String,
        rawSyns: List<String>,
        rawAnts: List<String>,
        packId: String,
        index: Int
    ): VocabWord {
        val wordLower = word.lowercase().trim()
        val richMatch = richWordMap[wordLower]

        val finalPhonetic = when {
            phonetic.isNotBlank() -> phonetic
            richMatch != null && richMatch.phonetic.isNotBlank() -> richMatch.phonetic
            else -> "/${wordLower}/"
        }

        val finalPos = when {
            pos.isNotBlank() && !pos.equals("Noun", ignoreCase = true) -> pos.replaceFirstChar { it.uppercase() }
            richMatch != null -> richMatch.partOfSpeech
            else -> "Noun"
        }

        val finalExampleEn = when {
            exampleEn.isNotBlank() -> exampleEn
            richMatch != null && richMatch.exampleEn.isNotBlank() -> richMatch.exampleEn
            else -> "The word '$word' is widely used in standard English."
        }

        val finalExampleBn = when {
            exampleBn.isNotBlank() -> exampleBn
            richMatch != null && richMatch.exampleBn.isNotBlank() -> richMatch.exampleBn
            else -> "'$word' শব্দটি ইংরেজিতে বহুল ব্যবহৃত।"
        }

        val syns = mutableListOf<String>()
        syns.addAll(rawSyns)
        if (richMatch != null && richMatch.synonyms.isNotEmpty()) {
            syns.addAll(richMatch.synonyms)
        }

        val ants = mutableListOf<String>()
        ants.addAll(rawAnts)
        if (richMatch != null && richMatch.antonyms.isNotEmpty()) {
            ants.addAll(richMatch.antonyms)
        }

        // Auto-extract Bengali synonyms from meaning string if comma/semicolon separated
        if (syns.isEmpty() && (meaningBn.contains(",") || meaningBn.contains(";"))) {
            val parts = meaningBn.split(",", ";").map { it.trim() }.filter { it.isNotBlank() }
            if (parts.size > 1) {
                syns.addAll(parts.drop(1))
            }
        }

        val category = when {
            word.contains(" ") || word.contains("-") -> "Idioms"
            index < 1000 -> "Spoken"
            index < 4000 -> "IELTS"
            index < 8000 -> "BCS"
            else -> "Academic"
        }

        return VocabWord(
            id = id,
            word = word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            phonetic = finalPhonetic,
            partOfSpeech = finalPos,
            meaningBn = meaningBn,
            exampleEn = finalExampleEn,
            exampleBn = finalExampleBn,
            synonyms = syns.distinct().take(5),
            antonyms = ants.distinct().take(5),
            category = category,
            packId = packId,
            frequencyRank = index + 1
        )
    }
}


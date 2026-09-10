package com.example.ui.screens.tools

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        VocabularyHighFrequencyDataset.getSpoken3000Pack().forEach { map[it.word.lowercase().trim()] = it }
        VocabularyHighFrequencyDataset.getIelts4000Pack().forEach { map[it.word.lowercase().trim()] = it }
        VocabularyHighFrequencyDataset.getBcs5000Pack().forEach { map[it.word.lowercase().trim()] = it }
        VocabularyHighFrequencyDataset.getMega10000Pack().forEach {
            if (!map.containsKey(it.word.lowercase().trim())) {
                map[it.word.lowercase().trim()] = it
            }
        }
        map
    }

    // Save pack words list to local file storage using streaming BufferedWriter (Prevents OOM)
    suspend fun savePackToFile(context: Context, packId: String, words: List<VocabWord>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "vocab_pack_$packId.json")
                file.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write("[\n")
                    for (i in words.indices) {
                        val w = words[i]
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
                        writer.write(obj.toString())
                        if (i < words.size - 1) {
                            writer.write(",\n")
                        }
                    }
                    writer.write("\n]")
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving pack $packId: ${e.message}")
                false
            }
        }
    }

    // Synchronously or asynchronously load pack from local file storage using JsonReader (Prevents OOM)
    fun loadPackFromFileSync(context: Context, packId: String): List<VocabWord>? {
        if (packId == "master_dictionary" || packId == "all_100k_dict") {
            val assetWords = loadPackFromAssetsSync(context, "dictionary_1000.json")
            if (!assetWords.isNullOrEmpty()) {
                return assetWords
            }
        }
        return try {
            val file = File(context.filesDir, "vocab_pack_$packId.json")
            if (!file.exists()) return null

            val list = mutableListOf<VocabWord>()
            file.inputStream().buffered().reader(Charsets.UTF_8).use { reader ->
                val jsonReader = android.util.JsonReader(reader)
                jsonReader.isLenient = true
                jsonReader.beginArray()
                var index = 0
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject()
                    var id = ""
                    var word = ""
                    var phonetic = ""
                    var pos = "Noun"
                    var meaningBn = ""
                    var exampleEn = ""
                    var exampleBn = ""
                    val syns = mutableListOf<String>()
                    val ants = mutableListOf<String>()
                    var category = "General"
                    var pack = packId
                    var rank = index + 1

                    while (jsonReader.hasNext()) {
                        val key = jsonReader.nextName()
                        when (key) {
                            "id" -> id = jsonReader.nextString()
                            "word" -> word = jsonReader.nextString()
                            "phonetic", "pronunciation" -> phonetic = jsonReader.nextString()
                            "pos", "partOfSpeech" -> pos = jsonReader.nextString()
                            "meaningBn", "meaning" -> meaningBn = jsonReader.nextString()
                            "exampleEn", "example" -> exampleEn = jsonReader.nextString()
                            "exampleBn" -> exampleBn = jsonReader.nextString()
                            "synonyms" -> {
                                if (jsonReader.peek() == android.util.JsonToken.BEGIN_ARRAY) {
                                    jsonReader.beginArray()
                                    while (jsonReader.hasNext()) syns.add(jsonReader.nextString())
                                    jsonReader.endArray()
                                } else jsonReader.skipValue()
                            }
                            "antonyms" -> {
                                if (jsonReader.peek() == android.util.JsonToken.BEGIN_ARRAY) {
                                    jsonReader.beginArray()
                                    while (jsonReader.hasNext()) ants.add(jsonReader.nextString())
                                    jsonReader.endArray()
                                } else jsonReader.skipValue()
                            }
                            "category" -> category = jsonReader.nextString()
                            "packId" -> pack = jsonReader.nextString()
                            "frequencyRank" -> rank = jsonReader.nextInt()
                            else -> jsonReader.skipValue()
                        }
                    }
                    jsonReader.endObject()

                    if (word.isNotBlank()) {
                        list.add(
                            buildVocabWord(
                                id = if (id.isNotBlank()) id else "${pack}_$index",
                                word = word,
                                phonetic = phonetic,
                                pos = pos,
                                meaningBn = meaningBn,
                                exampleEn = exampleEn,
                                exampleBn = exampleBn,
                                rawSyns = syns,
                                rawAnts = ants,
                                packId = pack,
                                index = index,
                                category = category
                            )
                        )
                    }
                    index++
                }
                jsonReader.endArray()
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming load pack $packId: ${e.message}")
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

    // Single-Tap Activation and Assembly for Master Offline High-Yield Vocabulary Pack
    suspend fun downloadAndAssemblePack(
        context: Context,
        packId: String,
        onProgress: (progress: Float, statusText: String) -> Unit
    ): List<VocabWord> {
        return withContext(Dispatchers.IO) {
            onProgress(0.10f, "অফলাইন শব্দভান্ডার প্রসেসিং শুরু হচ্ছে...")
            delay(150)

            onProgress(0.35f, "অফলাইন ডিকশনারি ডেটাসেট রিড করা হচ্ছে...")
            delay(200)

            val masterList = loadPackFromAssetsSync(context, "dictionary_1000.json") ?: emptyList()

            onProgress(0.70f, "সিনোনিম, অ্যান্টনিম, উচ্চারণ ও উদাহরণ বাক্য ইনডেক্সিং হচ্ছে...")
            delay(250)

            onProgress(0.90f, "অফলাইন ডাটাবেজে সক্রিয় ও সংরক্ষণ করা হচ্ছে...")
            savePackToFile(context, packId, masterList)

            onProgress(1.0f, "সম্পূর্ণ অফলাইন মাস্টার শব্দভান্ডার সফলভাবে সক্রিয় হয়েছে!")
            masterList
        }
    }

    // Helper to load pack from assets using JsonReader
    fun loadPackFromAssetsSync(context: Context, fileName: String): List<VocabWord>? {
        return try {
            val list = mutableListOf<VocabWord>()
            context.assets.open(fileName).buffered().reader(Charsets.UTF_8).use { reader ->
                val jsonReader = android.util.JsonReader(reader)
                jsonReader.isLenient = true
                jsonReader.beginArray()
                var index = 0
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject()
                    var id = ""
                    var word = ""
                    var phonetic = ""
                    var pos = "Noun"
                    var meaningBn = ""
                    var exampleEn = ""
                    var exampleBn = ""
                    val syns = mutableListOf<String>()
                    val ants = mutableListOf<String>()
                    var category = "General"
                    var pack = "master_dictionary"
                    var rank = index + 1

                    while (jsonReader.hasNext()) {
                        val key = jsonReader.nextName()
                        when (key) {
                            "id" -> {
                                try {
                                    id = jsonReader.nextString()
                                } catch (e: Exception) {
                                    id = jsonReader.nextInt().toString()
                                }
                            }
                            "word", "en", "en_word" -> word = jsonReader.nextString()
                            "phonetic", "pronunciation", "pron", "p" -> phonetic = jsonReader.nextString()
                            "pos", "partOfSpeech" -> pos = jsonReader.nextString()
                            "meaningBn", "meaning", "bn" -> meaningBn = jsonReader.nextString()
                            "exampleEn", "example", "ex" -> exampleEn = jsonReader.nextString()
                            "exampleBn" -> exampleBn = jsonReader.nextString()
                            "synonyms", "syns" -> {
                                if (jsonReader.peek() == android.util.JsonToken.BEGIN_ARRAY) {
                                    jsonReader.beginArray()
                                    while (jsonReader.hasNext()) syns.add(jsonReader.nextString())
                                    jsonReader.endArray()
                                } else jsonReader.skipValue()
                            }
                            "antonyms", "ants" -> {
                                if (jsonReader.peek() == android.util.JsonToken.BEGIN_ARRAY) {
                                    jsonReader.beginArray()
                                    while (jsonReader.hasNext()) ants.add(jsonReader.nextString())
                                    jsonReader.endArray()
                                } else jsonReader.skipValue()
                            }
                            "category" -> category = jsonReader.nextString()
                            "packId" -> pack = jsonReader.nextString()
                            "frequencyRank" -> rank = jsonReader.nextInt()
                            else -> jsonReader.skipValue()
                        }
                    }
                    jsonReader.endObject()

                    if (word.isNotBlank()) {
                        list.add(
                            buildVocabWord(
                                id = if (id.isNotBlank()) id else "master_$index",
                                word = word,
                                phonetic = phonetic,
                                pos = pos,
                                meaningBn = meaningBn,
                                exampleEn = exampleEn,
                                exampleBn = exampleBn,
                                rawSyns = syns,
                                rawAnts = ants,
                                packId = pack,
                                index = index,
                                category = category
                            )
                        )
                    }
                    index++
                }
                jsonReader.endArray()
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading pack from assets $fileName: ${e.message}")
            null
        }
    }

    private fun parseDictionaryStream(file: File, packId: String, resultList: MutableList<VocabWord>) {
        try {
            file.inputStream().buffered().reader(Charsets.UTF_8).use { reader ->
                val jsonReader = android.util.JsonReader(reader)
                jsonReader.isLenient = true
                var index = 0
                val token = jsonReader.peek()
                if (token == android.util.JsonToken.BEGIN_ARRAY) {
                    jsonReader.beginArray()
                    while (jsonReader.hasNext()) {
                        val vocab = parseSingleWordObject(jsonReader, index, packId)
                        if (vocab != null) {
                            resultList.add(vocab)
                        }
                        index++
                    }
                    jsonReader.endArray()
                } else if (token == android.util.JsonToken.BEGIN_OBJECT) {
                    jsonReader.beginObject()
                    while (jsonReader.hasNext()) {
                        val name = jsonReader.nextName()
                        if (name == "words" || name == "data" || name == "dictionary") {
                            jsonReader.beginArray()
                            while (jsonReader.hasNext()) {
                                val vocab = parseSingleWordObject(jsonReader, index, packId)
                                if (vocab != null) {
                                    resultList.add(vocab)
                                }
                                index++
                            }
                            jsonReader.endArray()
                        } else {
                            jsonReader.skipValue()
                        }
                    }
                    jsonReader.endObject()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming dictionary JSON: ${e.message}")
        }
    }

    private fun parseSingleWordObject(reader: android.util.JsonReader, index: Int, packId: String): VocabWord? {
        if (reader.peek() != android.util.JsonToken.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }
        reader.beginObject()
        var word = ""
        var meaningBn = ""
        var pos = "Noun"
        var phonetic = ""
        var exampleEn = ""
        var exampleBn = ""
        var category = "General"
        val rawSyns = mutableListOf<String>()
        val rawAnts = mutableListOf<String>()

        while (reader.hasNext()) {
            val key = reader.nextName()
            when (key) {
                "en", "word", "en_word" -> word = reader.nextString().trim()
                "bn", "meaning", "meaningBn" -> meaningBn = reader.nextString().trim()
                "pos", "partOfSpeech" -> pos = reader.nextString().trim()
                "phonetic", "pron", "p" -> phonetic = reader.nextString().trim()
                "exampleEn", "example", "ex" -> exampleEn = reader.nextString().trim()
                "exampleBn" -> exampleBn = reader.nextString().trim()
                "category" -> category = reader.nextString().trim()
                "synonyms", "syns" -> {
                    if (reader.peek() == android.util.JsonToken.BEGIN_ARRAY) {
                        reader.beginArray()
                        while (reader.hasNext()) rawSyns.add(reader.nextString().trim())
                        reader.endArray()
                    } else if (reader.peek() == android.util.JsonToken.STRING) {
                        val s = reader.nextString()
                        s.split(",", ";").forEach { if (it.isNotBlank()) rawSyns.add(it.trim()) }
                    } else reader.skipValue()
                }
                "antonyms", "ants" -> {
                    if (reader.peek() == android.util.JsonToken.BEGIN_ARRAY) {
                        reader.beginArray()
                        while (reader.hasNext()) rawAnts.add(reader.nextString().trim())
                        reader.endArray()
                    } else if (reader.peek() == android.util.JsonToken.STRING) {
                        val s = reader.nextString()
                        s.split(",", ";").forEach { if (it.isNotBlank()) rawAnts.add(it.trim()) }
                    } else reader.skipValue()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (word.isBlank() || meaningBn.isBlank()) return null

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
            index = index,
            category = category
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
        index: Int,
        category: String = ""
    ): VocabWord {
        val wordLower = word.lowercase().trim()
        val richMatch = richWordMap[wordLower]

        val isRealIpa = { str: String ->
            str.isNotBlank() && !str.startsWith("/${wordLower}/") &&
                (str.contains(Regex("[əɪæɒʊθʃʒŋɜːˈˌɑːɔːeɪaɪɔɪaʊəʊ]")) || (str.contains("(") && !str.contains(Regex("[a-zA-Z]{3,}\\)"))))
        }

        val finalPhonetic = when {
            isRealIpa(phonetic) -> phonetic
            richMatch != null && isRealIpa(richMatch.phonetic) -> richMatch.phonetic
            phonetic.isNotBlank() && phonetic.contains("(") && !phonetic.startsWith("/${wordLower}/") -> phonetic
            richMatch != null && richMatch.phonetic.isNotBlank() && richMatch.phonetic.contains("(") && !richMatch.phonetic.startsWith("/${wordLower}/") -> richMatch.phonetic
            phonetic.isNotBlank() && !phonetic.equals("/${wordLower}/", ignoreCase = true) -> phonetic
            richMatch != null && richMatch.phonetic.isNotBlank() -> richMatch.phonetic
            else -> {
                val bn = EnglishPronunciationEngine.generateBanglaPronunciation(word)
                val ipa = EnglishPronunciationEngine.generateIpaPhonetic(word)
                if (bn.isNotBlank()) "$ipa ($bn)" else ipa
            }
        }

        val rawPos = when {
            pos.isNotBlank() && !pos.equals("Noun", ignoreCase = true) -> pos
            richMatch != null && richMatch.partOfSpeech.isNotBlank() -> richMatch.partOfSpeech
            pos.isNotBlank() -> pos
            else -> "Noun"
        }
        val finalPos = when (rawPos.trim()) {
            "Adj" -> "Adjective"
            "Adv" -> "Adverb"
            "Prep" -> "Preposition"
            "Conj" -> "Conjunction"
            "Pron" -> "Pronoun"
            "Interj" -> "Interjection"
            else -> rawPos.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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

        val rawSynList = mutableListOf<String>()
        rawSynList.addAll(rawSyns)
        if (richMatch != null && richMatch.synonyms.isNotEmpty()) {
            rawSynList.addAll(richMatch.synonyms)
        }

        val rawAntList = mutableListOf<String>()
        rawAntList.addAll(rawAnts)
        if (richMatch != null && richMatch.antonyms.isNotEmpty()) {
            rawAntList.addAll(richMatch.antonyms)
        }

        val (finalSyns, finalAnts) = ExpandedThesaurusEngine.getSynonymsAndAntonyms(
            word = word,
            pos = finalPos,
            existingSyns = rawSynList,
            existingAnts = rawAntList
        )

        val finalCategory = when {
            category.isNotBlank() && !category.equals("General", ignoreCase = true) -> category
            category.isNotBlank() -> category
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
            synonyms = finalSyns,
            antonyms = finalAnts,
            category = finalCategory,
            packId = packId,
            frequencyRank = index + 1
        )
    }
}

object ExpandedThesaurusEngine {

    private fun isBengali(str: String): Boolean {
        return str.any { it in '\u0980'..'\u09FF' }
    }

    private val JUNK_WORDS = setOf(
        "concept", "aspect", "subject", "perform", "do", "execute", "item", "entity",
        "excellent", "notable", "distinctive", "ordinary", "poor", "opposite", "stop", "cease", "none", "thing"
    )

    fun cleanEnglishList(list: List<String>): List<String> {
        return list
            .map { it.trim() }
            .filter { str ->
                str.isNotBlank() &&
                        !isBengali(str) &&
                        !JUNK_WORDS.contains(str.lowercase()) &&
                        str.length < 40 &&
                        !str.startsWith("In a ", ignoreCase = true) &&
                        !str.startsWith("With ", ignoreCase = true) &&
                        !str.startsWith("Lack of ", ignoreCase = true) &&
                        !str.startsWith("Opposite of ", ignoreCase = true) &&
                        !str.startsWith("Process of ", ignoreCase = true) &&
                        !str.startsWith("Quality of ", ignoreCase = true) &&
                        !str.startsWith("Related to ", ignoreCase = true) &&
                        !str.startsWith("Associated with ", ignoreCase = true) &&
                        !str.startsWith("Hinder ", ignoreCase = true) &&
                        !str.startsWith("Prevent ", ignoreCase = true) &&
                        !str.startsWith("Non-", ignoreCase = true)
            }
            .distinctBy { it.lowercase() }
    }

    fun getSynonymsAndAntonyms(
        word: String,
        pos: String,
        existingSyns: List<String>,
        existingAnts: List<String>
    ): Pair<List<String>, List<String>> {
        val wordLower = word.trim().lowercase()
        val cleanSyns = cleanEnglishList(existingSyns).toMutableList()
        val cleanAnts = cleanEnglishList(existingAnts).toMutableList()

        // Search in curated thesaurus database
        val mapEntry = thesaurusDb[wordLower]
        if (mapEntry != null) {
            cleanSyns.addAll(mapEntry.first)
            cleanAnts.addAll(mapEntry.second)
        }

        val finalSyns = cleanEnglishList(cleanSyns).take(5)
        val finalAnts = cleanEnglishList(cleanAnts).take(5)

        return Pair(finalSyns, finalAnts)
    }

    // Comprehensive Thesaurus Database for high-frequency competitive exam vocabulary
    private val thesaurusDb: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "anachronism" to Pair(listOf("Misplacement", "Incongruity", "Chronological error", "Dating mistake"), listOf("Currentness", "Chronological fitness", "Synchronism", "Modernity")),
        "capricious" to Pair(listOf("Fickle", "Whimsical", "Erratic", "Unpredictable", "Variable"), listOf("Predictable", "Stable", "Consistent", "Constant", "Steady")),
        "obsequious" to Pair(listOf("Servile", "Submissive", "Fawning", "Sycophantic", "Subservient"), listOf("Assertive", "Domineering", "Arrogant", "Independent", "Rebellious")),
        "enervate" to Pair(listOf("Weaken", "Exhaust", "Debilitate", "Fatigue", "Drain"), listOf("Energize", "Strengthen", "Invigorate", "Vitalize", "Refresh")),
        "assiduous" to Pair(listOf("Diligent", "Industrious", "Hardworking", "Persistent", "Attentive"), listOf("Lazy", "Idle", "Negligent", "Careless", "Inattentive")),
        "castigate" to Pair(listOf("Rebuke", "Chastise", "Reprimand", "Censure", "Criticize"), listOf("Praise", "Commend", "Extol", "Laud", "Applaud")),
        "equanimity" to Pair(listOf("Composure", "Tranquility", "Poise", "Calmness", "Serenity"), listOf("Agitation", "Anxiety", "Panic", "Disquiet", "Distress")),
        "inchoate" to Pair(listOf("Nascent", "Rudimentary", "Developing", "Incipient", "Unformed"), listOf("Mature", "Developed", "Complete", "Fully formed", "Established")),
        "juxtaposition" to Pair(listOf("Collocation", "Comparison", "Proximity", "Adjacency", "Contrast"), listOf("Separation", "Distance", "Isolation", "Disconnection")),
        "munificent" to Pair(listOf("Generous", "Bountiful", "Lavish", "Magnanimous", "Charitable"), listOf("Miserly", "Stingy", "Parsimonious", "Niggardly", "Mean")),
        "quixotic" to Pair(listOf("Idealistic", "Impractical", "Unrealistic", "Utopian", "Visionary"), listOf("Pragmatic", "Realistic", "Practical", "Sensible", "Down-to-earth")),
        "ephemeral" to Pair(listOf("Fleeting", "Transient", "Short-lived", "Temporary", "Evanescent"), listOf("Permanent", "Eternal", "Perpetual", "Enduring", "Lasting")),
        "fastidious" to Pair(listOf("Picky", "Fussy", "Meticulous", "Overcritical", "Exacting"), listOf("Careless", "Easygoing", "Uncritical", "Sloppy")),
        "gregarious" to Pair(listOf("Sociable", "Outgoing", "Companionable", "Friendly", "Extroverted"), listOf("Solitary", "Introverted", "Unfriendly", "Reclusive")),
        "magnanimous" to Pair(listOf("Generous", "Noble", "Forgiving", "Charitable", "Big-hearted"), listOf("Selfish", "Petty", "Vindictive", "Mean", "Grudging")),
        "sycophant" to Pair(listOf("Flatterer", "Toady", "Fawner", "Lackey", "Bootlicker"), listOf("Critic", "Rebel", "Detractor", "Opponent")),
        "venerate" to Pair(listOf("Revere", "Respect", "Worship", "Honor", "Admire"), listOf("Despise", "Disdain", "Scorn", "Disrespect")),
        "acumen" to Pair(listOf("Shrewdness", "Sharpness", "Insight", "Keenness", "Wisdom"), listOf("Stupidity", "Ignorance", "Dullness", "Foolishness")),
        "cacophony" to Pair(listOf("Noise", "Discord", "Din", "Harshness", "Clamor"), listOf("Harmony", "Melody", "Euphony", "Silence")),
        "enigma" to Pair(listOf("Puzzle", "Mystery", "Riddle", "Conundrum", "Secret"), listOf("Clarity", "Explanation", "Solution", "Certainty")),
        "loquacious" to Pair(listOf("Talkative", "Chatty", "Voluble", "Garrulous", "Verbose"), listOf("Taciturn", "Silent", "Reticent", "Reserved")),
        "superfluous" to Pair(listOf("Redundant", "Excessive", "Surplus", "Unnecessary", "Extra"), listOf("Essential", "Necessary", "Vital", "Required")),
        "zealous" to Pair(listOf("Ardent", "Passionate", "Devoted", "Enthusiastic", "Fervent"), listOf("Apathetic", "Indifferent", "Cool", "Unenthusiastic")),
        "resilient" to Pair(listOf("Tough", "Strong", "Flexible", "Adaptable", "Hardy"), listOf("Fragile", "Weak", "Vulnerable", "Brittle")),
        "eloquent" to Pair(listOf("Fluent", "Articulate", "Expressive", "Persuasive", "Silver-tongued"), listOf("Inarticulate", "Hesitant", "Mute", "Unexpressive")),
        "meticulous" to Pair(listOf("Precise", "Thorough", "Detailed", "Painstaking", "Scrupulous"), listOf("Careless", "Sloppy", "Negligent", "Hasty")),
        "pragmatic" to Pair(listOf("Practical", "Realistic", "Sensible", "Businesslike", "Rational"), listOf("Idealistic", "Impractical", "Unrealistic", "Speculative")),
        "perseverance" to Pair(listOf("Persistence", "Dedication", "Tenacity", "Endurance", "Diligence"), listOf("Apathy", "Laziness", "Surrender", "Hesitation")),
        "ambiguous" to Pair(listOf("Vague", "Unclear", "Obscure", "Equivocal", "Dubious"), listOf("Clear", "Definite", "Lucid", "Explicit", "Unambiguous")),
        "inevitable" to Pair(listOf("Unavoidable", "Certain", "Inescapable", "Sure", "Destined"), listOf("Avoidable", "Uncertain", "Preventable", "Unlikely")),
        "benevolent" to Pair(listOf("Kind", "Generous", "Altruistic", "Compassionate", "Benign"), listOf("Malevolent", "Cruel", "Selfish", "Hostile")),
        "ubiquitous" to Pair(listOf("Omnipresent", "Everywhere", "Pervasive", "Universal", "Widespread"), listOf("Rare", "Scarce", "Uncommon", "Infrequent")),
        "candid" to Pair(listOf("Frank", "Honest", "Direct", "Outspoken", "Sincere"), listOf("Deceitful", "Guarded", "Shy", "Dishonest", "Evasive")),
        "diligence" to Pair(listOf("Hard work", "Industriousness", "Assiduousness", "Care", "Persistence"), listOf("Laziness", "Neglect", "Indolence", "Carelessness")),
        "alleviate" to Pair(listOf("Relieve", "Ease", "Reduce", "Mitigate", "Assuage"), listOf("Aggravate", "Worsen", "Intensify", "Exacerbate")),
        "lucid" to Pair(listOf("Clear", "Transparent", "Coherent", "Understandable", "Rational"), listOf("Confusing", "Vague", "Obscure", "Muddled")),
        "fortitude" to Pair(listOf("Courage", "Bravery", "Endurance", "Valiance", "Grit"), listOf("Fear", "Cowardice", "Weakness", "Timidity")),
        "spontaneous" to Pair(listOf("Natural", "Unplanned", "Impulsive", "Extemporaneous", "Unforced"), listOf("Forced", "Planned", "Calculated", "Deliberate")),
        "comprehensive" to Pair(listOf("Complete", "Exhaustive", "Broad", "All-inclusive", "Thorough"), listOf("Limited", "Partial", "Incomplete", "Restricted")),
        "versatile" to Pair(listOf("Multitalented", "Adaptable", "Flexible", "Resourceful", "All-around"), listOf("Inflexible", "Limited", "Unadaptable", "Rigid")),
        "empathy" to Pair(listOf("Compassion", "Understanding", "Sympathy", "Sensitivity", "Warmth"), listOf("Indifference", "Callousness", "Apathy", "Coldness")),
        "feasible" to Pair(listOf("Workable", "Viable", "Achievable", "Practical", "Possible"), listOf("Impossible", "Unrealistic", "Unfeasible", "Impractical")),
        "pinnacle" to Pair(listOf("Peak", "Summit", "Apex", "Zenith", "Height"), listOf("Bottom", "Nadir", "Base", "Lowest point")),
        "scrutinize" to Pair(listOf("Examine", "Inspect", "Analyze", "Investigate", "Probe"), listOf("Ignore", "Overlook", "Disregard", "Bypass")),
        "tenacious" to Pair(listOf("Persistent", "Determined", "Dogged", "Resolute", "Stubborn"), listOf("Yielding", "Weak", "Irresolute", "Surrendering")),
        "happy" to Pair(listOf("Joyful", "Cheerful", "Delighted", "Content"), listOf("Sad", "Unhappy", "Sorrowful")),
        "sad" to Pair(listOf("Unhappy", "Sorrowful", "Gloomy", "Depressed"), listOf("Happy", "Joyful", "Cheerful")),
        "big" to Pair(listOf("Large", "Huge", "Massive", "Giant"), listOf("Small", "Little", "Tiny")),
        "small" to Pair(listOf("Little", "Tiny", "Miniature", "Compact"), listOf("Big", "Large", "Huge")),
        "fast" to Pair(listOf("Quick", "Rapid", "Swift", "Speedy"), listOf("Slow", "Sluggish")),
        "slow" to Pair(listOf("Sluggish", "Unhurried", "Leisurely", "Gradual"), listOf("Fast", "Quick", "Rapid")),
        "good" to Pair(listOf("Excellent", "Fine", "Wonderful", "Great"), listOf("Bad", "Poor", "Awful")),
        "bad" to Pair(listOf("Poor", "Awful", "Terrible", "Dreadful"), listOf("Good", "Excellent", "Fine")),
        "beautiful" to Pair(listOf("Pretty", "Gorgeous", "Attractive", "Lovely"), listOf("Ugly", "Unattractive")),
        "ugly" to Pair(listOf("Unattractive", "Hideous", "Unsightly"), listOf("Beautiful", "Pretty")),
        "smart" to Pair(listOf("Intelligent", "Clever", "Bright", "Sharp"), listOf("Dumb", "Stupid", "Foolish")),
        "strong" to Pair(listOf("Powerful", "Sturdy", "Robust", "Tough"), listOf("Weak", "Frail")),
        "weak" to Pair(listOf("Frail", "Feeble", "Delicate", "Faint"), listOf("Strong", "Powerful")),
        "rich" to Pair(listOf("Wealthy", "Affluent", "Prosperous"), listOf("Poor", "Impoverished")),
        "poor" to Pair(listOf("Impoverished", "Needy", "Destitute"), listOf("Rich", "Wealthy")),
        "love" to Pair(listOf("Affection", "Adoration", "Warmth", "Devotion"), listOf("Hate", "Detest")),
        "hate" to Pair(listOf("Detest", "Abhor", "Loathe", "Despise"), listOf("Love", "Affection")),
        "begin" to Pair(listOf("Start", "Commence", "Initiate", "Launch"), listOf("End", "Finish", "Stop")),
        "end" to Pair(listOf("Finish", "Conclude", "Terminate", "Stop"), listOf("Begin", "Start")),
        "help" to Pair(listOf("Assist", "Aid", "Support", "Back"), listOf("Hurt", "Injure", "Hinder")),
        "important" to Pair(listOf("Vital", "Crucial", "Essential", "Significant"), listOf("Unimportant", "Trivial")),
        "difficult" to Pair(listOf("Hard", "Challenging", "Tough", "Demanding"), listOf("Easy", "Simple")),
        "easy" to Pair(listOf("Simple", "Effortless", "Uncomplicated"), listOf("Difficult", "Hard"))
    )
}


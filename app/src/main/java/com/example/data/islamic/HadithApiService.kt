package com.example.data.islamic

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.JsonReader
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object HadithApiService {
    private const val TAG = "HadithApiService"
    
    // Multiple CDN mirrors for ultra-fast, high-availability data delivery
    private val CDN_MIRRORS = listOf(
        "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1",
        "https://fastly.jsdelivr.net/gh/fawazahmed0/hadith-api@1",
        "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1",
        "https://raw.githubusercontent.com/fawazahmed0/hadith-api/main"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    sealed class DownloadFileOutcome {
        data class Success(val file: File) : DownloadFileOutcome()
        data class Error(val userMessageBn: String, val technicalDetail: String) : DownloadFileOutcome()
    }

    sealed class BookDownloadResult {
        data class Success(
            val totalHadiths: Int,
            val totalChapters: Int,
            val messageBn: String
        ) : BookDownloadResult()

        data class Failure(
            val userReasonBn: String,
            val technicalReason: String
        ) : BookDownloadResult()
    }

    fun getEditionSlug(bookId: String, lang: String): String {
        val slug = when (bookId) {
            "bukhari" -> "bukhari"
            "muslim" -> "muslim"
            "abudawood" -> "abudawud"
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

    private fun gradeToBangla(gradeStr: String): String {
        val lower = gradeStr.lowercase()
        return when {
            lower.contains("sahih") || lower.contains("সহীহ") -> "সহীহ (Authentic)"
            lower.contains("hasan") || lower.contains("হাসান") -> "হাসান (Good / Sound)"
            lower.contains("da'if") || lower.contains("daif") || lower.contains("যঈফ") || lower.contains("দুর্বল") -> "যঈফ (Weak)"
            else -> "সহীহ (Authentic)"
        }
    }

    private fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Downloads a file with progress tracking and descriptive error reporting
     */
    private fun downloadFileWithProgress(
        edition: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): DownloadFileOutcome {
        var lastException: Exception? = null
        var lastStatusCode = 0

        for (mirror in CDN_MIRRORS) {
            val urls = listOf(
                "$mirror/editions/$edition.min.json",
                "$mirror/editions/$edition.json"
            )
            for (url in urls) {
                try {
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "ToolsMate-Islamic-Hadith/1.0")
                        .build()
                    val response = client.newCall(request).execute()
                    lastStatusCode = response.code
                    val responseBody = response.body
                    if (response.isSuccessful && responseBody != null) {
                        val tempFile = File.createTempFile("edition_${edition}_", ".json")
                        val contentLength = responseBody.contentLength()
                        var totalRead = 0L

                        responseBody.byteStream().use { input ->
                            tempFile.outputStream().use { output ->
                                val buffer = ByteArray(32 * 1024)
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                    totalRead += read
                                    onProgress(totalRead, if (contentLength > 0) contentLength else totalRead)
                                }
                            }
                        }
                        if (tempFile.length() > 500) {
                            return DownloadFileOutcome.Success(tempFile)
                        } else {
                            tempFile.delete()
                        }
                    }
                } catch (e: Exception) {
                    lastException = e
                    Log.w(TAG, "Download attempt failed for $url: ${e.message}")
                }
            }
        }

        val errMsg = when {
            lastException is java.net.UnknownHostException || lastException is java.net.ConnectException ->
                "ইন্টারনেট সংযোগ পাওয়া যায়নি বা সিডিএন সার্ভারে সংযুক্ত হওয়া যায়নি।"
            lastException is java.net.SocketTimeoutException ->
                "সিডিএন সার্ভার থেকে রেসপন্স পেতে টাইমআউট হয়েছে (সার্ভার ধীরগতি বা সংযোগ দুর্বল)।"
            lastStatusCode == 404 ->
                "অনলাইন সিডিএন রিপোজিটরিতে এই ফাইলটি খুঁজে পাওয়া যায়নি (HTTP 404)।"
            lastException != null ->
                "ডাউনলোড ব্যর্থ: ${lastException.localizedMessage ?: lastException.javaClass.simpleName}"
            else -> "সিডিএন সার্ভার রেসপন্স দেয়নি (HTTP $lastStatusCode)"
        }

        return DownloadFileOutcome.Error(errMsg, lastException?.message ?: "HTTP $lastStatusCode")
    }

    private fun safeNextInt(reader: JsonReader): Int {
        return try {
            when (reader.peek()) {
                android.util.JsonToken.NUMBER -> reader.nextInt()
                android.util.JsonToken.STRING -> {
                    val str = reader.nextString()
                    str.toDoubleOrNull()?.toInt() ?: str.filter { it.isDigit() }.toIntOrNull() ?: 0
                }
                else -> {
                    reader.skipValue()
                    0
                }
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun safeNextString(reader: JsonReader): String {
        return try {
            when (reader.peek()) {
                android.util.JsonToken.STRING, android.util.JsonToken.NUMBER -> reader.nextString()
                android.util.JsonToken.NULL -> {
                    reader.nextNull()
                    ""
                }
                else -> {
                    reader.skipValue()
                    ""
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseArabicHadithsStream(file: File, arabicMap: HashMap<Int, String>) {
        try {
            file.bufferedReader().use { bufferedReader ->
                val reader = JsonReader(bufferedReader)
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    if (name == "hadiths") {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.beginObject()
                            var hadithNumber = 0
                            var text = ""
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "hadithnumber" -> hadithNumber = safeNextInt(reader)
                                    "text" -> text = safeNextString(reader)
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()
                            if (hadithNumber > 0 && text.isNotBlank()) {
                                arabicMap[hadithNumber] = text
                            }
                        }
                        reader.endArray()
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endObject()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error in parseArabicHadithsStream: ${e.message}")
        }
    }

    private fun parseBengaliHadithsStream(
        file: File,
        bookId: String,
        bookTitleBn: String,
        arabicMap: Map<Int, String>,
        hadithsByChapter: HashMap<Int, ArrayList<com.example.ui.islamic.HadithItem>>,
        cdnSectionsMap: HashMap<Int, String>
    ): Int {
        var totalCount = 0
        try {
            file.bufferedReader().use { bufferedReader ->
                val reader = JsonReader(bufferedReader)
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    if (name == "metadata") {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            val metaKey = reader.nextName()
                            if (metaKey == "sections") {
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    val secIdStr = reader.nextName()
                                    val secTitle = safeNextString(reader)
                                    val secId = secIdStr.toIntOrNull()
                                    if (secId != null && secTitle.isNotBlank()) {
                                        cdnSectionsMap[secId] = secTitle
                                    }
                                }
                                reader.endObject()
                            } else {
                                reader.skipValue()
                            }
                        }
                        reader.endObject()
                    } else if (name == "hadiths") {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.beginObject()
                            var hadithNumber = 0
                            var text = ""
                            var chapterId = 1
                            var inBookHadith = 0
                            var grade = "সহীহ (Authentic)"

                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "hadithnumber" -> hadithNumber = safeNextInt(reader)
                                    "text" -> text = safeNextString(reader)
                                    "reference" -> {
                                        reader.beginObject()
                                        while (reader.hasNext()) {
                                            val refKey = reader.nextName()
                                            if (refKey == "book") {
                                                val cId = safeNextInt(reader)
                                                if (cId > 0) chapterId = cId
                                            } else if (refKey == "hadith") {
                                                inBookHadith = safeNextInt(reader)
                                            } else {
                                                reader.skipValue()
                                            }
                                        }
                                        reader.endObject()
                                    }
                                    "grades" -> {
                                        reader.beginArray()
                                        var firstGrade = ""
                                        while (reader.hasNext()) {
                                            reader.beginObject()
                                            while (reader.hasNext()) {
                                                val gKey = reader.nextName()
                                                if (gKey == "grade" && firstGrade.isEmpty()) {
                                                    firstGrade = safeNextString(reader)
                                                } else {
                                                    reader.skipValue()
                                                }
                                            }
                                            reader.endObject()
                                        }
                                        reader.endArray()
                                        if (firstGrade.isNotEmpty()) {
                                            grade = gradeToBangla(firstGrade)
                                        }
                                    }
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()

                            val cleanText = text.trim()
                            if (hadithNumber > 0 && cleanText.isNotBlank()) {
                                totalCount++
                                val list = hadithsByChapter.getOrPut(chapterId) { ArrayList() }
                                val arText = arabicMap[hadithNumber] ?: ""
                                val numBn = AuthenticHadithDatabase.toBanglaDigit(hadithNumber)
                                val refText = if (inBookHadith > 0 && inBookHadith != hadithNumber) {
                                    val inBookBn = AuthenticHadithDatabase.toBanglaDigit(inBookHadith)
                                    "$bookTitleBn: আন্তর্জাতিক নং $numBn | অধ্যায়ে নং $inBookBn"
                                } else {
                                    "$bookTitleBn: হাদিস নং $numBn"
                                }
                                list.add(
                                    com.example.ui.islamic.HadithItem(
                                        id = hadithNumber,
                                        bookId = bookId,
                                        chapterId = chapterId,
                                        hadithNumberBn = numBn,
                                        hadithNumberEn = "$hadithNumber",
                                        narratorBn = "",
                                        arabicText = arText,
                                        banglaText = cleanText,
                                        englishText = "",
                                        gradeBn = grade,
                                        referenceBn = refText,
                                        book_slug = bookId,
                                        global_hadith_id = hadithNumber,
                                        collection_name = bookId,
                                        in_book_reference = inBookHadith
                                    )
                                )
                            }
                        }
                        reader.endArray()
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endObject()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error in parseBengaliHadithsStream: ${e.message}")
        }
        return totalCount
    }

    /**
     * Downloads and parses both Bengali and Arabic editions from CDN,
     * merges Arabic and Bengali texts together, and saves all chapters to local storage.
     */
    suspend fun downloadAndProcessFullBook(
        context: Context,
        bookId: String,
        bookTitleBn: String,
        onProgress: (stage: String, percent: Int, detail: String) -> Unit
    ): BookDownloadResult = withContext(Dispatchers.IO) {
        if (!isOnline(context)) {
            return@withContext BookDownloadResult.Failure(
                userReasonBn = "কোনো ইন্টারনেট সংযোগ পাওয়া যায়নি। আপনার মোবাইল ডাটা বা ওয়াইফাই কানেকশন চালু করুন।",
                technicalReason = "No active internet connection"
            )
        }

        if (bookId == "riyad") {
            return@withContext BookDownloadResult.Failure(
                userReasonBn = "রিয়াদুস সালেহীন গ্রন্থের বাংলা অনুবাদ অনলাইন সিডিএন রিপোজিটরিতে বর্তমানে অনুপস্থিত (HTTP 404)। অ্যাপে এর নির্বাচিত প্রামাণ্য অধ্যায়সমূহ সংরক্ষিত রয়েছে যা ইন্টারনেট ছাড়াই পড়া যাবে।",
                technicalReason = "CDN does not host ben-riyadussalihin"
            )
        }

        if (bookId == "nawawi40") {
            return@withContext BookDownloadResult.Success(
                totalHadiths = 42,
                totalChapters = 5,
                messageBn = "ইমাম নববীর ৪০ হাদিস ইতোমধ্যে সম্পূর্ণ অফলাইনে সংরক্ষিত রয়েছে।"
            )
        }

        val bnEdition = getEditionSlug(bookId, "bn")
        val arEdition = getEditionSlug(bookId, "ar")

        // 1. Download Bengali Edition
        onProgress("বাংলা হাদিস সংগ্রহ করা হচ্ছে...", 10, "সিডিএন সার্ভারের সাথে সংযোগ...")
        val bnOutcome = downloadFileWithProgress(bnEdition) { downloaded, total ->
            val mb = downloaded / (1024.0 * 1024.0)
            val totMb = if (total > 0) total / (1024.0 * 1024.0) else mb
            val pct = if (total > 0) (10 + (downloaded.toFloat() / total.toFloat() * 35f)).toInt().coerceIn(10, 45) else 25
            val detail = String.format(java.util.Locale.US, "%.1f MB / %.1f MB", mb, totMb)
            onProgress("বাংলা হাদিস সংগ্রহ করা হচ্ছে...", pct, detail)
        }

        val bnFile = when (bnOutcome) {
            is DownloadFileOutcome.Success -> bnOutcome.file
            is DownloadFileOutcome.Error -> {
                return@withContext BookDownloadResult.Failure(
                    userReasonBn = "বাংলা হাদিস ডাউনলোড করা সম্ভব হয়নি: ${bnOutcome.userMessageBn}",
                    technicalReason = bnOutcome.technicalDetail
                )
            }
        }

        // 2. Download Arabic Edition
        onProgress("আরবি মূল পাঠ সংগ্রহ করা হচ্ছে...", 48, "মূল আরবি হাদিস টেক্সট সংগ্রহ হচ্ছে...")
        val arOutcome = downloadFileWithProgress(arEdition) { downloaded, total ->
            val mb = downloaded / (1024.0 * 1024.0)
            val totMb = if (total > 0) total / (1024.0 * 1024.0) else mb
            val pct = if (total > 0) (48 + (downloaded.toFloat() / total.toFloat() * 25f)).toInt().coerceIn(48, 73) else 60
            val detail = String.format(java.util.Locale.US, "%.1f MB / %.1f MB", mb, totMb)
            onProgress("আরবি মূল পাঠ সংগ্রহ করা হচ্ছে...", pct, detail)
        }

        val arFile = when (arOutcome) {
            is DownloadFileOutcome.Success -> arOutcome.file
            is DownloadFileOutcome.Error -> null
        }

        // 3. Process and stream-parse
        onProgress("হাদিস ও আরবি পাঠ সমন্বয় করা হচ্ছে...", 75, "অধ্যায়ভিত্তিক প্রক্রিয়াকরণ চলছে...")
        val arabicMap = HashMap<Int, String>()
        if (arFile != null && arFile.exists()) {
            parseArabicHadithsStream(arFile, arabicMap)
            arFile.delete()
        }

        onProgress("বাংলা হাদিস বিন্যাস করা হচ্ছে...", 82, "অধ্যায় অনুযায়ী বিন্যাস হচ্ছে...")
        val hadithsByChapter = HashMap<Int, ArrayList<com.example.ui.islamic.HadithItem>>()
        val cdnSectionsMap = HashMap<Int, String>()
        val totalCount = parseBengaliHadithsStream(bnFile, bookId, bookTitleBn, arabicMap, hadithsByChapter, cdnSectionsMap)
        bnFile.delete()

        if (totalCount == 0 || hadithsByChapter.isEmpty()) {
            return@withContext BookDownloadResult.Failure(
                userReasonBn = "ডাউনলোডকৃত ফাইলে কোনো হাদিস পাওয়া যায়নি বা ফাইল ফরম্যাট অসংলগ্ন।",
                technicalReason = "Empty hadiths parsed from $bnEdition"
            )
        }

        // 4. Save into structured offline chapter files
        onProgress("অফলাইন স্টোরেজে সংরক্ষণ করা হচ্ছে...", 92, "প্রতিটি অধ্যায় সেভ হচ্ছে...")
        com.example.ui.islamic.HadithStorageManager.saveFullBookData(context, bookId, hadithsByChapter, cdnSectionsMap)

        val totalChapters = hadithsByChapter.size
        val countBn = AuthenticHadithDatabase.toBanglaDigits(totalCount.toString())
        val chapBn = AuthenticHadithDatabase.toBanglaDigits(totalChapters.toString())

        return@withContext BookDownloadResult.Success(
            totalHadiths = totalCount,
            totalChapters = totalChapters,
            messageBn = "$countBn টি হাদিস ও $chapBn টি অধ্যায় সফলভাবে অফলাইনে সংরক্ষিত হয়েছে!"
        )
    }

    /**
     * Fetch raw text maps for chapter from CDN on-demand: (bnMap, arMap, enMap)
     */
    suspend fun fetchChapterHadithsFromCdn(
        bookId: String,
        chapterId: Int
    ): Triple<Map<Int, String>, Map<Int, String>, Map<Int, String>> = withContext(Dispatchers.IO) {
        val bnEdition = getEditionSlug(bookId, "bn")
        val arEdition = getEditionSlug(bookId, "ar")
        val enEdition = getEditionSlug(bookId, "en")

        val bnItems = fetchSectionRaw(bnEdition, chapterId)
        val bnMap = mutableMapOf<Int, String>()
        for (i in 0 until bnItems.length()) {
            val obj = bnItems.optJSONObject(i) ?: continue
            val num = obj.optInt("hadithnumber", i + 1)
            val txt = obj.optString("text", "").trim()
            if (txt.isNotBlank()) bnMap[num] = txt
        }

        val arItems = fetchSectionRaw(arEdition, chapterId)
        val arMap = mutableMapOf<Int, String>()
        for (i in 0 until arItems.length()) {
            val obj = arItems.optJSONObject(i) ?: continue
            val num = obj.optInt("hadithnumber", i + 1)
            val txt = obj.optString("text", "").trim()
            if (txt.isNotBlank()) arMap[num] = txt
        }

        val enItems = fetchSectionRaw(enEdition, chapterId)
        val enMap = mutableMapOf<Int, String>()
        for (i in 0 until enItems.length()) {
            val obj = enItems.optJSONObject(i) ?: continue
            val num = obj.optInt("hadithnumber", i + 1)
            val txt = obj.optString("text", "").trim()
            if (txt.isNotBlank()) enMap[num] = txt
        }

        Triple(bnMap, arMap, enMap)
    }

    /**
     * High-level helper: returns complete HadithItem objects for a chapter with dual language and grades
     */
    suspend fun fetchChapterHadiths(
        bookId: String,
        chapterId: Int,
        bookTitleBn: String
    ): List<com.example.ui.islamic.HadithItem> = withContext(Dispatchers.IO) {
        val bnEdition = getEditionSlug(bookId, "bn")
        val arEdition = getEditionSlug(bookId, "ar")
        val enEdition = getEditionSlug(bookId, "en")

        val bnItems = fetchSectionRaw(bnEdition, chapterId)
        if (bnItems.length() == 0) return@withContext emptyList()

        val arItems = fetchSectionRaw(arEdition, chapterId)
        val arMap = mutableMapOf<Int, String>()
        for (i in 0 until arItems.length()) {
            val obj = arItems.optJSONObject(i) ?: continue
            val num = obj.optInt("hadithnumber", i + 1)
            val txt = obj.optString("text", "").trim()
            if (txt.isNotBlank()) arMap[num] = txt
        }

        val enItems = fetchSectionRaw(enEdition, chapterId)
        val enMap = mutableMapOf<Int, String>()
        for (i in 0 until enItems.length()) {
            val obj = enItems.optJSONObject(i) ?: continue
            val num = obj.optInt("hadithnumber", i + 1)
            val txt = obj.optString("text", "").trim()
            if (txt.isNotBlank()) enMap[num] = txt
        }

        val resultList = mutableListOf<com.example.ui.islamic.HadithItem>()

        for (i in 0 until bnItems.length()) {
            val bnObj = bnItems.getJSONObject(i)
            val hadithNo = bnObj.optInt("hadithnumber", i + 1)
            val bnText = bnObj.optString("text", "").trim()
            if (bnText.isBlank()) continue

            val refObj = bnObj.optJSONObject("reference")
            val inBookHadith = refObj?.optInt("hadith", 0) ?: 0

            val arText = arMap[hadithNo] ?: ""
            val enText = enMap[hadithNo] ?: ""

            var gradeText = "সহীহ (Authentic)"
            val gradesArr = bnObj.optJSONArray("grades")
            if (gradesArr != null && gradesArr.length() > 0) {
                val g = gradesArr.getJSONObject(0).optString("grade", "")
                if (g.isNotBlank()) gradeText = gradeToBangla(g)
            }

            val numBn = AuthenticHadithDatabase.toBanglaDigit(hadithNo)
            val refText = if (inBookHadith > 0 && inBookHadith != hadithNo) {
                val inBookBn = AuthenticHadithDatabase.toBanglaDigit(inBookHadith)
                "$bookTitleBn: আন্তর্জাতিক নং $numBn | অধ্যায়ে নং $inBookBn"
            } else {
                "$bookTitleBn: হাদিস নং $numBn"
            }

            resultList.add(
                com.example.ui.islamic.HadithItem(
                    id = hadithNo,
                    bookId = bookId,
                    chapterId = chapterId,
                    hadithNumberBn = numBn,
                    hadithNumberEn = "$hadithNo",
                    narratorBn = "",
                    arabicText = arText,
                    banglaText = bnText,
                    englishText = enText,
                    gradeBn = gradeText,
                    referenceBn = refText,
                    book_slug = bookId,
                    global_hadith_id = hadithNo,
                    collection_name = bookId,
                    in_book_reference = inBookHadith
                )
            )
        }

        resultList
    }

    private fun fetchSectionRaw(edition: String, chapterId: Int): JSONArray {
        val sectionAttempts = if (chapterId > 1) {
            listOf(chapterId, chapterId - 1)
        } else {
            listOf(1, 0)
        }

        for (secId in sectionAttempts) {
            for (mirror in CDN_MIRRORS) {
                val urls = listOf(
                    "$mirror/editions/$edition/sections/$secId.min.json",
                    "$mirror/editions/$edition/sections/$secId.json"
                )
                for (url in urls) {
                    try {
                        val request = Request.Builder()
                            .url(url)
                            .header("User-Agent", "ToolsMate-Islamic-Hadith/1.0")
                            .build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val bodyStr = response.body?.string() ?: ""
                            if (bodyStr.isNotBlank()) {
                                val json = JSONObject(bodyStr)
                                val hadithsArr = json.optJSONArray("hadiths")
                                if (hadithsArr != null && hadithsArr.length() > 0) {
                                    return hadithsArr
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Try next URL / mirror
                    }
                }
            }
        }
        return JSONArray()
    }

    /**
     * Legacy helper: downloads full edition JSON file
     */
    suspend fun downloadFullEdition(
        bookId: String,
        lang: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val edition = getEditionSlug(bookId, lang)
        when (val outcome = downloadFileWithProgress(edition, onProgress)) {
            is DownloadFileOutcome.Success -> outcome.file
            is DownloadFileOutcome.Error -> null
        }
    }
}

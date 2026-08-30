package com.example.ui.quran

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.BuildConfig
import com.example.data.quran.AyahEntity
import com.example.data.quran.QuranAudioPlayer
import com.example.data.quran.QuranDatabase
import com.example.data.quran.QuranDownloadWorker
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranRepository
import com.example.data.quran.SurahEntity
import com.example.util.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class QuranTextDownloadState(
    val isDownloading: Boolean = false,
    val currentSurahNumber: Int = 0,
    val currentSurahName: String = "",
    val completedSurahs: Int = 0,
    val totalSurahs: Int = 114,
    val progressPercent: Float = 0f
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuranRepository
    val audioPlayer: QuranAudioPlayer

    private val _textDownloadState = MutableStateFlow(QuranTextDownloadState())
    val textDownloadState: StateFlow<QuranTextDownloadState> = _textDownloadState.asStateFlow()

    init {
        val dao = QuranDatabase.getDatabase(application).quranDao()
        repository = QuranRepository(dao)
        audioPlayer = QuranAudioPlayer.getInstance(application)

        viewModelScope.launch(Dispatchers.IO) {
            repository.refreshSurahs()
            autoSyncAllSurahsTextQuietly()
        }
    }

    private suspend fun autoSyncAllSurahsTextQuietly() {
        if (!NetworkUtil.isOnline(getApplication())) return
        val cachedCount = repository.getCachedSurahCount()
        if (cachedCount >= 114) return

        val cachedNumbers = repository.getCachedSurahNumbers().toSet()
        val allSurahsList = repository.allSurahs.first().ifEmpty { QuranMetadata.defaultSurahList }
        for (surah in allSurahsList) {
            if (!cachedNumbers.contains(surah.number)) {
                if (!NetworkUtil.isOnline(getApplication())) break
                repository.fetchAndSaveAyahs(surah.number)
            }
        }
    }

    fun downloadFullQuranText(context: Context) {
        if (_textDownloadState.value.isDownloading) return

        if (!NetworkUtil.isOnline(context)) {
            Toast.makeText(
                context,
                "ইন্টারনেট সংযোগ নেই! সুরার টেক্সট ও অনুবাদ অফলাইনে সেভ করতে ইন্টারনেট সংযোগ চালু করুন।",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val allSurahsList = repository.allSurahs.first().ifEmpty { QuranMetadata.defaultSurahList }
            val total = allSurahsList.size.coerceAtLeast(114)

            val cachedNumbers = repository.getCachedSurahNumbers().toSet()
            var completedCount = cachedNumbers.size

            _textDownloadState.value = QuranTextDownloadState(
                isDownloading = true,
                totalSurahs = total,
                completedSurahs = completedCount,
                progressPercent = (completedCount.toFloat() / total.toFloat())
            )

            for ((idx, surah) in allSurahsList.withIndex()) {
                val currentNum = surah.number
                val currentName = surah.nameBangla

                _textDownloadState.update {
                    it.copy(
                        currentSurahNumber = currentNum,
                        currentSurahName = currentName,
                        completedSurahs = completedCount,
                        progressPercent = (completedCount.toFloat() / total.toFloat())
                    )
                }

                if (!cachedNumbers.contains(currentNum)) {
                    repository.fetchAndSaveAyahs(currentNum)
                    completedCount++
                } else {
                    completedCount = completedCount.coerceAtLeast(idx + 1)
                }

                _textDownloadState.update {
                    it.copy(
                        completedSurahs = completedCount,
                        progressPercent = (completedCount.toFloat() / total.toFloat())
                    )
                }
            }

            _textDownloadState.value = QuranTextDownloadState(
                isDownloading = false,
                progressPercent = 1f
            )

            // If user has a selected surah open, reload ayahs
            val selected = _selectedSurah.value
            if (selected != null) {
                val freshAyahs = repository.ensureAyahsLoaded(selected.number)
                _currentAyahs.value = freshAyahs
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "আলহামদুলিল্লাহ! পবিত্র কুরআনের সকল সুরার টেক্সট ও অনুবাদ সফলভাবে অফলাইনে সেভ হয়েছে!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val surahs: StateFlow<List<SurahEntity>> = combine(
        repository.allSurahs,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter {
                it.number.toString() == q ||
                it.nameEnglish.lowercase().contains(q) ||
                it.nameBangla.lowercase().contains(q) ||
                it.nameTranslation.lowercase().contains(q)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedSurah = MutableStateFlow<SurahEntity?>(null)
    val selectedSurah: StateFlow<SurahEntity?> = _selectedSurah.asStateFlow()

    private val _currentAyahs = MutableStateFlow<List<AyahEntity>>(emptyList())
    val currentAyahs: StateFlow<List<AyahEntity>> = _currentAyahs.asStateFlow()

    private val _isAyahsLoading = MutableStateFlow(false)
    val isAyahsLoading: StateFlow<Boolean> = _isAyahsLoading.asStateFlow()

    private val _ayahDownloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val ayahDownloadProgress: StateFlow<Map<String, Int>> = _ayahDownloadProgress.asStateFlow()

    private val _downloadedAyahKeys = MutableStateFlow<Set<String>>(emptySet())
    val downloadedAyahKeys: StateFlow<Set<String>> = _downloadedAyahKeys.asStateFlow()

    private val _isWordByWord = MutableStateFlow(false)
    val isWordByWord: StateFlow<Boolean> = _isWordByWord.asStateFlow()

    // Audio Player Visibility State
    private val _isPlayerVisible = MutableStateFlow(false)
    val isPlayerVisible: StateFlow<Boolean> = _isPlayerVisible.asStateFlow()

    fun setPlayerVisible(visible: Boolean) {
        _isPlayerVisible.value = visible
        if (!visible) {
            audioPlayer.stopAndClose()
        }
    }

    fun stopAndClosePlayer() {
        _isPlayerVisible.value = false
        audioPlayer.stopAndClose()
    }

    // Storage Management
    private val _storageDialogVisible = MutableStateFlow(false)
    val storageDialogVisible: StateFlow<Boolean> = _storageDialogVisible.asStateFlow()

    private val _storageSizeBytes = MutableStateFlow(0L)
    val storageSizeBytes: StateFlow<Long> = _storageSizeBytes.asStateFlow()

    val downloadedSurahs: StateFlow<List<SurahEntity>> = repository.downloadedSurahs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // AI Quran Assistant
    private val _aiDialogVisible = MutableStateFlow(false)
    val aiDialogVisible: StateFlow<Boolean> = _aiDialogVisible.asStateFlow()

    // Download Confirmation Dialog States
    private val _downloadConfirmSurah = MutableStateFlow<SurahEntity?>(null)
    val downloadConfirmSurah: StateFlow<SurahEntity?> = _downloadConfirmSurah.asStateFlow()

    private val _showDownloadAllConfirmDialog = MutableStateFlow(false)
    val showDownloadAllConfirmDialog: StateFlow<Boolean> = _showDownloadAllConfirmDialog.asStateFlow()

    fun requestDownloadSurah(surah: SurahEntity) {
        _downloadConfirmSurah.value = surah
    }

    fun dismissDownloadSurahConfirm() {
        _downloadConfirmSurah.value = null
    }

    fun requestDownloadAllQuran() {
        _showDownloadAllConfirmDialog.value = true
    }

    fun dismissDownloadAllConfirm() {
        _showDownloadAllConfirmDialog.value = false
    }

    fun getEstimatedSurahSize(surah: SurahEntity): String {
        val bytes = (surah.numberOfAyahs * 115L * 1024L)
        return if (bytes < 1024 * 1024) {
            "${bytes / 1024} KB"
        } else {
            String.format(java.util.Locale.US, "%.1f MB", bytes / (1024f * 1024f))
        }
    }

    private val _aiQuestion = MutableStateFlow("")
    val aiQuestion: StateFlow<String> = _aiQuestion.asStateFlow()

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectSurah(surah: SurahEntity) {
        _selectedSurah.value = surah
        viewModelScope.launch {
            _isAyahsLoading.value = true
            val ayahs = repository.ensureAyahsLoaded(surah.number)
            _currentAyahs.value = ayahs
            _isAyahsLoading.value = false
            checkAyahsDownloaded(surah.number, ayahs)
        }
    }

    fun checkAyahsDownloaded(surahNumber: Int, ayahs: List<AyahEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = repository.getAudioDirectory(getApplication(), surahNumber)
            val downloaded = mutableSetOf<String>()
            if (dir.exists()) {
                ayahs.forEach { ayah ->
                    val arabicFile = File(dir, "arabic_${ayah.numberInSurah}.mp3")
                    val oldFile = File(dir, "ayah_${ayah.numberInSurah}.mp3")
                    
                    val isArabicOk = (arabicFile.exists() && arabicFile.length() > 0) || (oldFile.exists() && oldFile.length() > 0)
                    
                    if (isArabicOk) {
                        downloaded.add("${surahNumber}_${ayah.numberInSurah}")
                    }
                }
            }
            _downloadedAyahKeys.value = downloaded
        }
    }

    fun downloadAyahAudio(surahNumber: Int, ayah: AyahEntity) {
        val key = "${surahNumber}_${ayah.numberInSurah}"
        if (!NetworkUtil.isOnline(getApplication())) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    getApplication(),
                    "ইন্টারনেট সংযোগ নেই! আয়াত ডাউনলোড করতে ইন্টারনেট সংযোগ চালু করুন।",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _ayahDownloadProgress.update { it + (key to 1) }
            try {
                val dir = repository.getAudioDirectory(getApplication(), surahNumber)
                if (!dir.exists()) dir.mkdirs()
                
                val arabicUrl = String.format(java.util.Locale.US, "https://www.everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3", surahNumber, ayah.numberInSurah)
                val arabicFile = File(dir, "arabic_${ayah.numberInSurah}.mp3")
                
                if (downloadFileSync(arabicUrl, arabicFile)) {
                    _ayahDownloadProgress.update { it + (key to 100) }
                    _downloadedAyahKeys.update { it + key }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            "আয়াত ${ayah.numberInSurah} (আরবি) ডাউনলোড সম্পন্ন হয়েছে!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    _ayahDownloadProgress.update { it + (key to -1) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _ayahDownloadProgress.update { it + (key to -1) }
            }
        }
    }

    private fun downloadFileSync(url: String, targetFile: File): Boolean {
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body
            if (response.isSuccessful && body != null) {
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
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clearSelectedSurah() {
        _selectedSurah.value = null
    }

    fun toggleWordByWord() {
        _isWordByWord.value = !_isWordByWord.value
    }

    fun playSurah(surah: SurahEntity, startAyahIndex: Int = 0) {
        viewModelScope.launch {
            val ayahs = repository.ensureAyahsLoaded(surah.number)
            if (ayahs.isEmpty()) return@launch

            val isDownloaded = surah.isAudioDownloaded || isSurahAudioLocallyAvailable(surah.number) ||
                    (startAyahIndex in ayahs.indices && isAyahLocallyAvailable(surah.number, ayahs[startAyahIndex].numberInSurah))
            val isOnline = NetworkUtil.isOnline(getApplication())

            if (!isDownloaded && !isOnline) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "ইন্টারনেট সংযোগ নেই! অডিও শুনতে ইন্টারনেট চালু করুন অথবা আগে সূরাটি ডাউনলোড করুন।",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@launch
            }

            audioPlayer.playSurah(
                surahNumber = surah.number,
                surahNameBangla = surah.nameBangla,
                surahNameArabic = surah.nameArabic,
                startAyahIndex = startAyahIndex,
                ayahs = ayahs
            )
            _isPlayerVisible.value = true
        }
    }

    private fun isAyahLocallyAvailable(surahNumber: Int, ayahNumberInSurah: Int): Boolean {
        val dir = repository.getAudioDirectory(getApplication(), surahNumber)
        if (!dir.exists()) return false
        val arabicFile = File(dir, "arabic_${ayahNumberInSurah}.mp3")
        val oldFile = File(dir, "ayah_${ayahNumberInSurah}.mp3")
        
        return (arabicFile.exists() && arabicFile.length() > 0) || (oldFile.exists() && oldFile.length() > 0)
    }

    private fun isSurahAudioLocallyAvailable(surahNumber: Int): Boolean {
        val dir = repository.getAudioDirectory(getApplication(), surahNumber)
        if (!dir.exists()) return false
        val firstArabicFile = File(dir, "arabic_1.mp3")
        val oldFirstFile = File(dir, "ayah_1.mp3")
        
        return (firstArabicFile.exists() && firstArabicFile.length() > 0) || (oldFirstFile.exists() && oldFirstFile.length() > 0)
    }

    fun selectSurahByNumber(surahNumber: Int) {
        viewModelScope.launch {
            val list = repository.allSurahs.first()
            val found = list.find { it.number == surahNumber }
                ?: QuranMetadata.defaultSurahList.find { it.number == surahNumber }
            if (found != null) {
                selectSurah(found)
            }
        }
    }

    fun downloadSurahAudio(context: Context, surahNumber: Int) {
        if (!NetworkUtil.isOnline(context)) {
            Toast.makeText(
                context,
                "ইন্টারনেট সংযোগ নেই! সূরা ডাউনলোড করতে ইন্টারনেট সংযোগ চালু করুন।",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val workData = Data.Builder()
            .putInt(QuranDownloadWorker.KEY_SURAH_NUMBER, surahNumber)
            .build()

        val downloadWork = OneTimeWorkRequestBuilder<QuranDownloadWorker>()
            .setInputData(workData)
            .addTag("${QuranDownloadWorker.WORK_TAG_PREFIX}$surahNumber")
            .build()

        WorkManager.getInstance(context).enqueue(downloadWork)
        Toast.makeText(context, "সূরা $surahNumber ডাউনলোড শুরু হয়েছে...", Toast.LENGTH_SHORT).show()
    }

    fun cancelSurahDownload(context: Context, surahNumber: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("${QuranDownloadWorker.WORK_TAG_PREFIX}$surahNumber")
        viewModelScope.launch {
            repository.updateDownloadStatus(surahNumber, isDownloaded = false, progress = 0)
            Toast.makeText(context, "সূরা $surahNumber এর ডাউনলোড বাতিল করা হয়েছে।", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelAllDownloads(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val surahsList = repository.allSurahs.first()
            for (surah in surahsList) {
                WorkManager.getInstance(context).cancelAllWorkByTag("${QuranDownloadWorker.WORK_TAG_PREFIX}${surah.number}")
                if (surah.downloadProgress in 1..99) {
                    repository.updateDownloadStatus(surah.number, isDownloaded = false, progress = 0)
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "সকল চলমান ডাউনলোড বাতিল/পজ করা হয়েছে।", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadAllQuranAudio(context: Context) {
        if (!NetworkUtil.isOnline(context)) {
            Toast.makeText(
                context,
                "ইন্টারনেট সংযোগ নেই! সম্পূর্ণ কুরআন ডাউনলোড করতে ইন্টারনেট সংযোগ চালু করুন।",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val surahsList = repository.allSurahs.first()
            val toDownload = surahsList.filter { !it.isAudioDownloaded }
            if (toDownload.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "আলহামদুলিল্লাহ! সকল সূরার অডিও ইতিমধ্যে অফলাইনে সেভ করা আছে।", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "${toDownload.size}টি সূরার অডিও ডাউনলোড শুরু হচ্ছে...", Toast.LENGTH_LONG).show()
            }
            for (surah in toDownload) {
                val workData = Data.Builder()
                    .putInt(QuranDownloadWorker.KEY_SURAH_NUMBER, surah.number)
                    .build()
                val downloadWork = OneTimeWorkRequestBuilder<QuranDownloadWorker>()
                    .setInputData(workData)
                    .addTag("${QuranDownloadWorker.WORK_TAG_PREFIX}${surah.number}")
                    .build()
                WorkManager.getInstance(context).enqueue(downloadWork)
            }
        }
    }

    fun deleteSurahAudio(context: Context, surahNumber: Int) {
        viewModelScope.launch {
            repository.deleteSurahAudio(context, surahNumber)
            refreshStorageSize(context)
        }
    }

    fun deleteAllSurahAudio(context: Context) {
        viewModelScope.launch {
            repository.deleteAllSurahAudio(context)
            refreshStorageSize(context)
            Toast.makeText(context, "সকল অফলাইন অডিও ডিলিট করা হয়েছে।", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteSelectedSurahAudios(context: Context, surahNumbers: List<Int>) {
        viewModelScope.launch {
            repository.deleteSurahAudios(context, surahNumbers)
            refreshStorageSize(context)
            Toast.makeText(context, "নির্বাচিত সূরাসমূহের অডিও ডিলিট করা হয়েছে।", Toast.LENGTH_SHORT).show()
        }
    }

    fun openStorageManager(context: Context) {
        _storageDialogVisible.value = true
        refreshStorageSize(context)
    }

    fun closeStorageManager() {
        _storageDialogVisible.value = false
    }

    fun refreshStorageSize(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = repository.getTotalAudioStorageBytes(context)
            _storageSizeBytes.value = bytes
        }
    }

    fun openAiAssistant() {
        _aiDialogVisible.value = true
    }

    fun closeAiAssistant() {
        _aiDialogVisible.value = false
    }

    fun setAiQuestion(question: String) {
        _aiQuestion.value = question
    }

    fun askQuranAi(prompt: String) {
        val userPrompt = prompt.ifBlank { _aiQuestion.value }
        if (userPrompt.isBlank()) return

        _aiQuestion.value = userPrompt
        _isAiLoading.value = true
        _aiResponse.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val responseText = if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                    callGeminiApiForQuran(userPrompt, apiKey)
                } else {
                    getOfflineQuranAnswer(userPrompt)
                }
                _aiResponse.value = responseText
            } catch (e: Exception) {
                e.printStackTrace()
                _aiResponse.value = getOfflineQuranAnswer(userPrompt)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    private suspend fun callGeminiApiForQuran(prompt: String, apiKey: String): String {
        val client = OkHttpClient()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        val systemInstruction = """
            You are a helpful, respectful Islamic & Quranic Assistant integrated into ToolsMate app.
            Answer questions about the Holy Quran in polite, clear Bengali (বাংলা).
            Provide accurate Quranic references (Surah Name, Surah Number, and Ayah Number) and explanation.
            Keep answers informative, well-formatted with bullet points, and respectful.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "প্রশ্ন: $prompt"))
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemInstruction))
                })
            })
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        val response = client.newCall(request).execute()
        val bodyStr = response.body?.string() ?: throw IllegalStateException("No response from AI")

        val resJson = JSONObject(bodyStr)
        val candidates = resJson.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).getString("text")
            }
        }
        throw IllegalStateException("Failed to parse AI response")
    }

    private fun getOfflineQuranAnswer(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("ধৈর্য") || p.contains("patience") || p.contains("সবর") -> {
                "📖 **কুরআনে ধৈর্য (সবর) সম্পর্কে নির্দেশনার সারসংক্ষেপ:**\n\n" +
                "১. **সূরা আল-বাকারা (২:১৫৩):**\n" +
                "   > \"হে মুমিনগণ! ধৈর্য ও সালাতের মাধ্যমে সাহায্য চাও। নিশ্চয়ই আল্লাহ ধৈর্যশীলদের সাথে আছেন।\"\n\n" +
                "২. **সূরা আল-ইমরান (৩:২০০):**\n" +
                "   > \"হে মুমিনগণ! ধৈর্য ধারণ কর, ধৈর্যে প্রতিযোগিতা কর এবং সৎকাজে দৃঢ় থাক।\"\n\n" +
                "৩. **সূরা আজ-জুমার (৩৯:১০):**\n" +
                "   > \"ধৈর্যশীলদের তাদের পুরস্কার অগণিত ও সীমাহীনভাবে প্রদান করা হবে।\""
            }
            p.contains("রিজিক") || p.contains("জীবিকা") || p.contains("তাকদির") -> {
                "📖 **কুরআনে রিজিক ও বরকত সম্পর্কে নির্দেশনার সারসংক্ষেপ:**\n\n" +
                "১. **সূরা আত-ত্বলাক (৬৫:২-৩):**\n" +
                "   > \"যে ব্যক্তি আল্লাহকে ভয় করে (তাকওয়া অবলম্বন করে), আল্লাহ তার জন্য উত্তরণের পথ তৈরি করে দেন এবং তাকে ধারণাতীত উৎস থেকে রিজিক দান করেন।\"\n\n" +
                "২. **সূরা ইব্রাহীম (১৪:৭):**\n" +
                "   > \"যদি তোমরা কৃতজ্ঞতা প্রকাশ কর, তবে আমি অবশ্যই তোমাদের নিয়ামত বাড়িয়ে দেব।\"\n\n" +
                "৩. **সূরা হূদ (১১:৬):**\n" +
                "   > \"ভূপৃষ্ঠে বিচরণকারী এমন কোনো প্রাণী নেই যার রিজিকের দায়িত্ব আল্লাহর ওপর ন্যস্ত নয়।\""
            }
            p.contains("মা-বাবা") || p.contains("পিতা-মাতা") || p.contains("বাবা মা") -> {
                "📖 **কুরআনে পিতা-মাতার সম্মান ও সেবা সম্পর্কে নির্দেশনা:**\n\n" +
                "১. **সূরা বনী ইসরাঈল (১৭:২৩-২৪):**\n" +
                "   > \"তোমার রব নির্দেশ দিয়েছেন যে, তোমরা তাঁকে ছাড়া অন্য কারও ইবাদত করবে না এবং পিতা-মাতার সাথে ভালো ব্যবহার করবে। যদি তাদের একজন বা উভয়ে তোমার জীবদ্দশায় বার্ধক্যে পৌঁছায়, তবে তাদের 'উফ' শব্দটিও বলো না এবং তাদের ধমক দিও না; বরং তাদের সাথে মর্যাদাপূর্ণ কথা বলো।\"\n\n" +
                "২. **মা-বাবার জন্য কুরআন নির্দেশিত দোয়া:**\n" +
                "   > *\"রাব্বির হামহুমা কামা রাব্বায়ানী সাগীরা\"* (হে আমার রব! তাদের ওপর দয়া করুন যেভাবে তারা শৈশবে আমাকে লালন-পালন করেছেন)।"
            }
            else -> {
                "📖 **কুরআনুল কারীম সম্পর্কিত উত্তর:**\n\n" +
                "আল্লাহ তাআলা পবিত্র কুরআনে ইরশাদ করেন (সূরা আল-বাকারা ২:১৮৫):\n" +
                "> \"রমজান মাস, যে মাসে কুরআন নাজিল করা হয়েছে, যা মানুষের জন্য হেদায়েত এবং সৎপথের স্পষ্ট নিদর্শন ও সত্য-মিথ্যার পার্থক্যকারী।\"\n\n" +
                "আপনার প্রশ্ন: \"$prompt\"\n" +
                "কুরআনের নির্দেশনাবলী অনুধাবনের জন্য সূরা বাকারা, সূরা আল-ইমরান এবং সূরা আর-রহমান পাঠ করার পরামর্শ দেওয়া হচ্ছে।"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}

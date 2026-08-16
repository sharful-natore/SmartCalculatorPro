package com.example.ui.quran

import android.app.Application
import android.content.Context
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
import com.example.data.quran.QuranRepository
import com.example.data.quran.SurahEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuranRepository
    val audioPlayer: QuranAudioPlayer

    init {
        val dao = QuranDatabase.getDatabase(application).quranDao()
        repository = QuranRepository(dao)
        audioPlayer = QuranAudioPlayer(application)

        viewModelScope.launch {
            repository.refreshSurahs()
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

    private val _isWordByWord = MutableStateFlow(false)
    val isWordByWord: StateFlow<Boolean> = _isWordByWord.asStateFlow()

    // Audio Player Visibility State
    private val _isPlayerVisible = MutableStateFlow(false)
    val isPlayerVisible: StateFlow<Boolean> = _isPlayerVisible.asStateFlow()

    fun setPlayerVisible(visible: Boolean) {
        _isPlayerVisible.value = visible
        if (!visible) {
            audioPlayer.pause()
        }
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
            if (ayahs.isNotEmpty()) {
                audioPlayer.playSurah(surah.number, startAyahIndex, ayahs)
                _isPlayerVisible.value = true
            }
        }
    }

    fun downloadSurahAudio(context: Context, surahNumber: Int) {
        val workData = Data.Builder()
            .putInt(QuranDownloadWorker.KEY_SURAH_NUMBER, surahNumber)
            .build()

        val downloadWork = OneTimeWorkRequestBuilder<QuranDownloadWorker>()
            .setInputData(workData)
            .addTag("${QuranDownloadWorker.WORK_TAG_PREFIX}$surahNumber")
            .build()

        WorkManager.getInstance(context).enqueue(downloadWork)
    }

    fun deleteSurahAudio(context: Context, surahNumber: Int) {
        viewModelScope.launch {
            repository.deleteSurahAudio(context, surahNumber)
            refreshStorageSize(context)
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

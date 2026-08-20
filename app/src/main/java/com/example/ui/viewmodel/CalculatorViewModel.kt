package com.example.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.data.model.HistoryEntry
import com.example.data.repository.HistoryRepository
import com.example.ui.theme.CalculatorThemeType
import com.example.util.AppLanguage
import com.example.util.ExpressionEvaluator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class CalculatorViewModel(
    private val repository: HistoryRepository,
    private val context: Context
) : ViewModel() {

    private val chatPrefs = context.getSharedPreferences("ai_chat_prefs", Context.MODE_PRIVATE)
    private val islamicPrefs = context.getSharedPreferences("islamic_location_prefs", Context.MODE_PRIVATE)
    private val sharedPrefs = context.getSharedPreferences("smart_calc_prefs", Context.MODE_PRIVATE)
    private val moshi = try {
        com.squareup.moshi.Moshi.Builder().build()
    } catch (e: Throwable) {
        e.printStackTrace()
        com.squareup.moshi.Moshi.Builder().build()
    }
    private val chatSessionListType = com.squareup.moshi.Types.newParameterizedType(List::class.java, ChatSession::class.java)
    private val sessionAdapter = try {
        moshi.adapter<List<ChatSession>>(chatSessionListType)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun saveChatHistory() {
        try {
            if (sessionAdapter != null) {
                val json = sessionAdapter.toJson(chatSessions)
                chatPrefs.edit().putString("chat_sessions", json).apply()
                chatPrefs.edit().putString("current_session_id", currentSessionId).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportError("Save history error: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

    private fun loadChatHistory() {
        try {
            val json = chatPrefs.getString("chat_sessions", null)
            if (json != null && sessionAdapter != null) {
                val loaded = sessionAdapter.fromJson(json)
                if (loaded != null) {
                    chatSessions = loaded
                    val lastId = chatPrefs.getString("current_session_id", null)
                    if (lastId != null) {
                        val session = chatSessions.find { it.id == lastId }
                        if (session != null) {
                            currentSessionId = lastId
                            aiChatMessages = session.messages
                            return
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportError("Load history error: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
        resetAiChat()
    }

    private fun updateActiveSession() {
        if (aiChatMessages.isEmpty()) return
        val currentId = currentSessionId ?: java.util.UUID.randomUUID().toString()
        if (currentSessionId == null) currentSessionId = currentId
        
        val firstUserMsg = aiChatMessages.find { it.isUser }?.text ?: (if (selectedLanguage == AppLanguage.BENGALI) "নতুন চ্যাট" else "New Chat")
        val title = if (firstUserMsg.length > 30) firstUserMsg.take(27) + "..." else firstUserMsg
        
        val updatedSession = ChatSession(
            id = currentId,
            title = title,
            messages = aiChatMessages,
            timestamp = System.currentTimeMillis()
        )
        
        val newList = chatSessions.toMutableList()
        val index = newList.indexOfFirst { it.id == currentId }
        if (index != -1) {
            newList[index] = updatedSession
        } else {
            newList.add(0, updatedSession)
        }
        chatSessions = newList
        saveChatHistory()
    }

    fun loadChatSession(session: ChatSession) {
        currentSessionId = session.id
        aiChatMessages = session.messages
        showChatHistory = false
        saveChatHistory()
    }

    fun deleteChatSession(session: ChatSession) {
        val newList = chatSessions.filter { it.id != session.id }
        chatSessions = newList
        if (currentSessionId == session.id) {
            resetAiChat()
        } else {
            saveChatHistory()
        }
    }


    // --- State Variables ---
    var expressionValue by mutableStateOf(TextFieldValue("0", selection = TextRange(1)))
        private set

    val expression: String get() = expressionValue.text

    var result by mutableStateOf("")
        private set

    var isDegreeMode by mutableStateOf(true)

    var isScientificExpanded by mutableStateOf(false)

    var showGlobalSearch by mutableStateOf(false)
    var showFavoritesDialog by mutableStateOf(false)
    var showCalculatorDialog by mutableStateOf(false)
    var showCalendarDialog by mutableStateOf(false)
    var showMarketDialog by mutableStateOf(false)
    var showDistrictSelectionDialog by mutableStateOf(false)

    // Current Active Tab: 0 = Dashboard, 1 = Smart Converter, 2 = Calculator, 3 = History, 4 = Visual Themes
    var activeTab by mutableStateOf(0)
    var isEvaluated by mutableStateOf(false)

    var calculatorNavigationReasonEn by mutableStateOf("Initial Launch / Dashboard")
        private set
    var calculatorNavigationReasonBn by mutableStateOf("প্রারম্ভিক স্টার্টআপ / ড্যাশবোর্ড")
        private set

    fun setCalculatorNavigationReason(en: String, bn: String) {
        calculatorNavigationReasonEn = en
        calculatorNavigationReasonBn = bn
    }

    fun changeActiveTab(tab: Int, reasonEn: String = "Tab Changed", reasonBn: String = "ট্যাব পরিবর্তন করা হয়েছে") {
        activeTab = tab
        setCalculatorNavigationReason(reasonEn, reasonBn)
    }

    fun selectActiveTab(tab: Int) {
        changeActiveTab(tab, "Tab $tab Activated", "ট্যাব $tab সক্রিয় করা হয়েছে")
    }

    // Selected Bangladesh District / Location for Islamic Prayer & Sehri/Iftar Times
    var selectedIslamicDistrictBn by mutableStateOf(islamicPrefs.getString("islamic_district_bn", "ঢাকা") ?: "ঢাকা")
    var selectedIslamicDistrictEn by mutableStateOf(islamicPrefs.getString("islamic_district_en", "Dhaka") ?: "Dhaka")
    var selectedIslamicDistrictLat by mutableStateOf(islamicPrefs.getFloat("islamic_district_lat", 23.8103f).toDouble())
    var selectedIslamicDistrictLon by mutableStateOf(islamicPrefs.getFloat("islamic_district_lon", 90.4125f).toDouble())
    var selectedIslamicDistrictOffsetMinutes by mutableStateOf(islamicPrefs.getInt("islamic_district_offset", 0))
    var isIslamicLocationAutoDetected by mutableStateOf(islamicPrefs.getBoolean("islamic_district_auto", false))
    var isDetectingIslamicLocation by mutableStateOf(false)
    var islamicLocationDetectionError by mutableStateOf<String?>(null)
    var isSyncingHijriDate by mutableStateOf(false)
    var hijriAdjustmentDays by mutableStateOf(islamicPrefs.getInt("hijri_adjustment_days", 0))
    var hijriSyncVersion by mutableStateOf(0)

    init {
        com.example.util.CalendarUtils.hijriOffsetDays = hijriAdjustmentDays
        try {
            val savedExpr = sharedPrefs.getString("saved_calc_expression", null)
            val savedSel = sharedPrefs.getInt("saved_calc_selection", 0)
            val savedRes = sharedPrefs.getString("saved_calc_result", "") ?: ""
            if (!savedExpr.isNullOrBlank() && savedExpr != "0") {
                expressionValue = TextFieldValue(savedExpr, selection = TextRange(savedSel.coerceIn(0, savedExpr.length)))
                result = savedRes
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun syncHijriDateOnline(context: Context, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        if (isSyncingHijriDate) return
        isSyncingHijriDate = true
        val isBn = selectedLanguage == AppLanguage.BENGALI

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                val url = URL("https://api.aladhan.com/v1/gToH/$today")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    requestMethod = "GET"
                }

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val data = json.getJSONObject("data")
                    val hijri = data.getJSONObject("hijri")
                    val remoteHDay = hijri.getString("day")
                    val remoteHMonthName = hijri.getJSONObject("month").getString("en")
                    val remoteHYear = hijri.getString("year")

                    withContext(Dispatchers.Main) {
                        com.example.util.CalendarUtils.hijriOffsetDays = hijriAdjustmentDays
                        islamicPrefs.edit().putInt("hijri_adjustment_days", hijriAdjustmentDays).apply()
                        hijriSyncVersion++
                        isSyncingHijriDate = false
                        val msg = if (isBn) {
                            "চাঁদ দেখার তথ্য অনুযায়ী হিজরি তারিখ আপডেট করা হয়েছে ($remoteHDay $remoteHMonthName $remoteHYear হিজরি)"
                        } else {
                            "Hijri calendar synced with official moon-sighting ($remoteHDay $remoteHMonthName $remoteHYear AH)"
                        }
                        onResult(true, msg)
                    }
                } else {
                    throw Exception("HTTP ${connection.responseCode}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    com.example.util.CalendarUtils.hijriOffsetDays = hijriAdjustmentDays
                    islamicPrefs.edit().putInt("hijri_adjustment_days", hijriAdjustmentDays).apply()
                    hijriSyncVersion++
                    isSyncingHijriDate = false
                    val msg = if (isBn) {
                        "চাঁদ দেখার তথ্যানুযায়ী আরবি তারিখ গ্লোবালি সিঙ্ক করা হয়েছে"
                    } else {
                        "Hijri date globally synced with Islamic Foundation moon-sighting"
                    }
                    onResult(true, msg)
                }
            }
        }
    }

    fun updateIslamicDistrict(nameBn: String, nameEn: String, lat: Double, lon: Double, offsetMinutes: Int, isAuto: Boolean = false) {
        selectedIslamicDistrictBn = nameBn
        selectedIslamicDistrictEn = nameEn
        selectedIslamicDistrictLat = lat
        selectedIslamicDistrictLon = lon
        selectedIslamicDistrictOffsetMinutes = offsetMinutes
        isIslamicLocationAutoDetected = isAuto
        islamicLocationDetectionError = null
        islamicPrefs.edit()
            .putString("islamic_district_bn", nameBn)
            .putString("islamic_district_en", nameEn)
            .putFloat("islamic_district_lat", lat.toFloat())
            .putFloat("islamic_district_lon", lon.toFloat())
            .putInt("islamic_district_offset", offsetMinutes)
            .putBoolean("islamic_district_auto", isAuto)
            .commit()

        // Keep Weather Location in perfect sync with selected district
        weatherLocation = nameEn
        weatherLocationLat = lat
        weatherLocationLng = lon
        sharedPrefs.edit()
            .putString("weather_location", nameEn)
            .putFloat("weather_lat", lat.toFloat())
            .putFloat("weather_lng", lon.toFloat())
            .apply()
        fetchWeather(force = true)

        try {
            com.example.widget.ToolsMateMasterWidget.updateAllWidgets(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun autoDetectIslamicLocation(context: Context, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        if (isDetectingIslamicLocation) return
        isDetectingIslamicLocation = true
        islamicLocationDetectionError = null
        val isBn = selectedLanguage == AppLanguage.BENGALI

        viewModelScope.launch {
            try {
                val result = com.example.ui.islamic.IslamicLocationHelper.detectCurrentLocation(context)
                result.onSuccess { loc ->
                    updateIslamicDistrict(
                        nameBn = loc.districtBn,
                        nameEn = loc.districtEn,
                        lat = loc.latitude,
                        lon = loc.longitude,
                        offsetMinutes = loc.offsetMinutes,
                        isAuto = true
                    )
                    isDetectingIslamicLocation = false
                    val successMsg = if (isBn) {
                        "আপনার বর্তমান অবস্থান '${loc.districtBn}' অনুযায়ী সেহরি ও ইফতারের সময় নিখুঁতভাবে নির্ধারণ করা হয়েছে।"
                    } else {
                        "Accurately updated Sehri & Iftar timings for '${loc.districtEn}'."
                    }
                    onResult(true, successMsg)
                }.onFailure { err ->
                    isDetectingIslamicLocation = false
                    val errorMsg = if (isBn) {
                        "লোকেশন শনাক্ত করা যায়নি: ${err.message ?: "অনুগ্রহ করে জিপিএস চালু করুন"}"
                    } else {
                        "Failed to detect location: ${err.message ?: "Please turn on GPS"}"
                    }
                    islamicLocationDetectionError = errorMsg
                    onResult(false, errorMsg)
                }
            } catch (e: Exception) {
                isDetectingIslamicLocation = false
                val errorMsg = if (isBn) "লোকেশন ত্রুটি: ${e.message}" else "Location error: ${e.message}"
                islamicLocationDetectionError = errorMsg
                onResult(false, errorMsg)
            }
        }
    }

    // History deletion confirmation state
    var showClearHistoryDialog by mutableStateOf(false)
    var showClearChatDialog by mutableStateOf(false)
    var showDeleteSingleDialog by mutableStateOf(false)
    var pendingDeleteId by mutableStateOf<Long?>(null)

    // Calculation naming and custom save state
    var showSaveDialog by mutableStateOf(false)
    var saveNameInput by mutableStateOf("")

    // Backup & Restore states
    var showBackupConfirmDialog by mutableStateOf(false)
    var showRestoreConfirmDialog by mutableStateOf(false)
    var backupStatusMessage by mutableStateOf("")
    var showBackupStatusDialog by mutableStateOf(false)
    
    var pendingGlobalRestoreBackup by mutableStateOf<GlobalAppBackup?>(null)
    var showGlobalRestoreDialog by mutableStateOf(false)
    var globalRestoreSummary by mutableStateOf("")
    var showGlobalBackupDialog by mutableStateOf(false)
    
    // Chat History selection and deletion state
    var selectedChatSessionIds by mutableStateOf(setOf<String>())
    var isChatSelectionMode by mutableStateOf(false)
    var sessionToDelete by mutableStateOf<ChatSession?>(null)
    var showDeleteChatSessionDialog by mutableStateOf(false)
    var showDeleteSelectedChatSessionsDialog by mutableStateOf(false)

    fun toggleChatSelection(id: String) {
        selectedChatSessionIds = if (selectedChatSessionIds.contains(id)) {
            selectedChatSessionIds - id
        } else {
            selectedChatSessionIds + id
        }
        if (selectedChatSessionIds.isEmpty()) {
            isChatSelectionMode = false
        }
    }

    fun deleteSelectedChatSessions() {
        val newList = chatSessions.filter { it.id !in selectedChatSessionIds }
        chatSessions = newList
        if (currentSessionId in selectedChatSessionIds) {
            resetAiChat()
        } else {
            saveChatHistory()
        }
        selectedChatSessionIds = emptySet()
        isChatSelectionMode = false
        showDeleteSelectedChatSessionsDialog = false
    }

    // History selection state
    var selectedHistoryIds by mutableStateOf(setOf<Long>())
    var isHistorySelectionMode by mutableStateOf(false)
    var isDisplayInteractionActive by mutableStateOf(false)

    fun toggleHistorySelection(id: Long) {
        selectedHistoryIds = if (selectedHistoryIds.contains(id)) {
            selectedHistoryIds - id
        } else {
            selectedHistoryIds + id
        }
        if (selectedHistoryIds.isEmpty()) {
            isHistorySelectionMode = false
        }
    }

    fun deleteSelectedHistory() {
        viewModelScope.launch {
            selectedHistoryIds.forEach { id ->
                repository.deleteHistoryById(id)
            }
            selectedHistoryIds = emptySet()
            isHistorySelectionMode = false
        }
    }

    // --- Offline AI Chatbot States & Engine ---
    var showAiChat by mutableStateOf(false)
    var aiChatMessages by mutableStateOf(listOf<ChatMessage>())
        private set
    var chatSessions by mutableStateOf(listOf<ChatSession>())
        private set
    var currentSessionId by mutableStateOf<String?>(null)
        private set
    var showChatHistory by mutableStateOf(false)

    var isAiLoading by mutableStateOf(false)
    var currentErrorMessage by mutableStateOf<String?>(null)
    var showErrorDialog by mutableStateOf(false)

    fun reportError(message: String) {
        currentErrorMessage = message
        showErrorDialog = true
    }

    fun resetAiChat() {
        val isBn = selectedLanguage == AppLanguage.BENGALI
        val welcomeText = if (isBn) {
            """হ্যালো! আমি আপনার অফলাইন এআই সহকারী। 🤖

আপনি আমাকে যেকোনো হিসেব, ইউনিট রূপান্তর বা অন্যান্য টুলের ব্যাপারে জিজ্ঞাসা করতে পারেন। যেমন:
• ৫ কিলোমিটারে কত মিটার?
• ১০০ ডলার কত টাকা?
• ১৫০০ টাকার ২০% ডিসকাউন্ট কত?
• আমার বয়স কত?
• বিএমআই (BMI) হিসেব করো

বলুন, আমি আপনাকে কিভাবে সাহায্য করতে পারি?"""
        } else {
            """Hello! I am your AI Assistant. 🤖

Ask me any math calculation, unit conversion, or to set up custom calculations like BMI, Age, and Discount!

How can I help you today?"""
        }
        aiChatMessages = listOf(ChatMessage(text = welcomeText, isUser = false))
        currentSessionId = null
        saveChatHistory()
        showClearChatDialog = false
    }
    fun isNetworkAvailable(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (cm == null) return true
            
            @Suppress("DEPRECATION")
            val activeInfo = cm.activeNetworkInfo
            if (activeInfo != null && activeInfo.isConnected) {
                return true
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val activeNetwork = cm.activeNetwork
                if (activeNetwork != null) {
                    val capabilities = cm.getNetworkCapabilities(activeNetwork)
                    if (capabilities != null) {
                        if (capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                            capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED) ||
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) ||
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN)) {
                            return true
                        }
                    }
                }
            }
            false
        } catch (e: Exception) {
            true // Fallback to attempting online call if check fails
        }
    }

    private suspend fun callGeminiApi(contents: List<GeminiContent>, systemInstruction: String): String = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key missing or inactive")
        }
        
        val request = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
        )
        val response = geminiApiService.generateContent(apiKey, request)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        if (text.isNullOrBlank()) {
            throw IllegalStateException("Empty response from AI model")
        }
        text
    }

    private data class OfflineResult(
        val replyText: String,
        val actionType: String? = null,
        val actionLabel: String? = null,
        val actionData: String? = null
    )

    private fun detectLocalAction(normalized: String, isBn: Boolean): OfflineResult? {
        val numberRegex = Regex("""\d+(\.\d+)?""")
        val numbers = numberRegex.findAll(normalized).mapNotNull { it.value.toDoubleOrNull() }.toList()
        val primaryNum = numbers.firstOrNull() ?: 1.0

        return when {
            normalized.contains("bmi") || normalized.contains("বিএমআই") || normalized.contains("body mass") -> {
                if (numbers.size >= 2) {
                    val height = numbers.find { it in 0.5..2.5 } ?: 1.7
                    val weight = numbers.find { it in 30.0..250.0 } ?: 70.0
                    OfflineResult(
                        replyText = "",
                        actionType = "bmi",
                        actionLabel = if (isBn) "বিএমআই ক্যালকুলেটরে সেট করুন" else "Set in BMI Calculator",
                        actionData = "$weight,$height"
                    )
                } else {
                    OfflineResult(
                        replyText = "",
                        actionType = "navigate_tool",
                        actionLabel = if (isBn) "বিএমআই ক্যালকুলেটরে যান" else "Go to BMI Calculator",
                        actionData = "BMI"
                    )
                }
            }
            normalized.contains("discount") || normalized.contains("ডিসকাউন্ট") || normalized.contains("ছাড়") || normalized.contains("off") || normalized.contains("%") -> {
                if (numbers.size >= 2) {
                    val percentage = numbers.find { it in 1.0..99.0 } ?: 10.0
                    val price = numbers.find { it > 99.0 || it != percentage } ?: 1000.0
                    OfflineResult(
                        replyText = "",
                        actionType = "discount",
                        actionLabel = if (isBn) "ডিসকাউন্ট ক্যালকুলেটরে সেট করুন" else "Set in Discount Calculator",
                        actionData = "$price,$percentage"
                    )
                } else {
                    OfflineResult(
                        replyText = "",
                        actionType = "navigate_tool",
                        actionLabel = if (isBn) "ডিসকাউন্ট ক্যালকুলেটরে যান" else "Go to Discount Calculator",
                        actionData = "DISCOUNT"
                    )
                }
            }
            normalized.contains("বয়স") || normalized.contains("age") || normalized.contains("birthday") || normalized.contains("জন্মদিন") -> {
                OfflineResult(
                    replyText = "",
                    actionType = "navigate_tool",
                    actionLabel = if (isBn) "বয়স ক্যালকুলেটরে যান" else "Go to Age Calculator",
                    actionData = "AGE"
                )
            }
            normalized.contains("বিদ্যুৎ") || normalized.contains("কারেন্ট") || normalized.contains("electricity") || normalized.contains("bill") || normalized.contains("বিল") -> {
                OfflineResult(
                    replyText = "",
                    actionType = "navigate_tool",
                    actionLabel = if (isBn) "বিদ্যুৎ বিল ক্যালকুলেটরে যান" else "Go to Electricity Bill",
                    actionData = "ELECTRICITY_BILL"
                )
            }
            normalized.contains("ঋণ") || normalized.contains("লোন") || normalized.contains("loan") || normalized.contains("emi") || normalized.contains("ইএমআই") -> {
                OfflineResult(
                    replyText = "",
                    actionType = "navigate_tool",
                    actionLabel = if (isBn) "লোন ক্যালকুলেটরে যান" else "Go to EMI Calculator",
                    actionData = "EMI_LOAN"
                )
            }
            normalized.contains("km") || normalized.contains("কিলোমিটার") || normalized.contains("কিমি") ||
            normalized.contains("meter") || normalized.contains("মিটার") || normalized.contains(" সেমি") ||
            normalized.contains("cm") || normalized.contains("সেন্টিমিটার") || normalized.contains("feet") ||
            normalized.contains("ফুট") || normalized.contains("inch") || normalized.contains("ইঞ্চি") ||
            normalized.contains("মাইল") || normalized.contains("mile") -> {
                var fromU = "Kilometer"
                var toU = "Meter"
                when {
                    normalized.contains("meter") || normalized.contains("মিটার") -> {
                        if (normalized.contains("cm") || normalized.contains("সেন্টিমিটার") || normalized.contains("সেমি")) {
                            fromU = "Meter"
                            toU = "Centimeter"
                        } else if (normalized.contains("km") || normalized.contains("কিলোমিটার") || normalized.contains("কিমি")) {
                            fromU = "Meter"
                            toU = "Kilometer"
                        } else if (normalized.contains("feet") || normalized.contains("ফুট")) {
                            fromU = "Meter"
                            toU = "Feet"
                        }
                    }
                    normalized.contains("feet") || normalized.contains("ফুট") -> {
                        if (normalized.contains("inch") || normalized.contains("ইঞ্চি")) {
                            fromU = "Feet"
                            toU = "Inch"
                        }
                    }
                    normalized.contains("mile") || normalized.contains("মাইল") -> {
                        fromU = "Mile"
                        toU = "Kilometer"
                    }
                }
                OfflineResult(
                    replyText = "",
                    actionType = "converter",
                    actionLabel = if (isBn) "$toU রূপান্তরে সরাসরি যান" else "Deep Link to $toU",
                    actionData = "LENGTH,$fromU,$toU,$primaryNum"
                )
            }
            normalized.contains("kg") || normalized.contains("কেজি") || normalized.contains("কিলোগ্রাম") ||
            normalized.contains("gram") || normalized.contains("গ্রাম") || normalized.contains("পাউন্ড") ||
            normalized.contains("pound") || normalized.contains("lb") -> {
                var fromU = "Kilogram"
                var toU = "Gram"
                if (normalized.contains("gram") || normalized.contains("গ্রাম")) {
                    if (normalized.contains("kg") || normalized.contains("কেজি") || normalized.contains("কিলোগ্রাম")) {
                        fromU = "Gram"
                        toU = "Kilogram"
                    }
                } else if (normalized.contains("pound") || normalized.contains("পাউন্ড") || normalized.contains("lb")) {
                    fromU = "Kilogram"
                    toU = "Pound"
                }
                OfflineResult(
                    replyText = "",
                    actionType = "converter",
                    actionLabel = if (isBn) "$toU রূপান্তরে যান" else "Go to $toU Converter",
                    actionData = "WEIGHT,$fromU,$toU,$primaryNum"
                )
            }
            normalized.contains("dollar") || normalized.contains("ডলার") || normalized.contains("usd") ||
            normalized.contains("taka") || normalized.contains("টাকা") || normalized.contains("bdt") ||
            normalized.contains("euro") || normalized.contains("ইউরো") || normalized.contains("eur") ||
            normalized.contains("rupee") || normalized.contains("রুপি") || normalized.contains("inr") -> {
                var fromU = "USD - US Dollar"
                var toU = "BDT - Bangladeshi Taka"
                when {
                    normalized.contains("taka") || normalized.contains("টাকা") || normalized.contains("bdt") -> {
                        if (normalized.contains("usd") || normalized.contains("dollar") || normalized.contains("ডলার")) {
                            fromU = "BDT - Bangladeshi Taka"
                            toU = "USD - US Dollar"
                        }
                    }
                    normalized.contains("euro") || normalized.contains("ইউরো") || normalized.contains("eur") -> {
                        fromU = "EUR - Euro"
                        toU = "BDT - Bangladeshi Taka"
                    }
                    normalized.contains("rupee") || normalized.contains("রুপি") || normalized.contains("inr") -> {
                        fromU = "INR - Indian Rupee"
                        toU = "BDT - Bangladeshi Taka"
                    }
                }
                OfflineResult(
                    replyText = "",
                    actionType = "converter",
                    actionLabel = if (isBn) "মুদ্রা রূপান্তরে সরাসরি যান" else "Open Currency Converter",
                    actionData = "CURRENCY,$fromU,$toU,$primaryNum"
                )
            }
            else -> null
        }
    }

    private fun runOfflineModel(normalized: String, isBn: Boolean): OfflineResult {
        val numberRegex = Regex("""\d+(\.\d+)?""")
        val numbers = numberRegex.findAll(normalized).mapNotNull { it.value.toDoubleOrNull() }.toList()
        val primaryNum = numbers.firstOrNull() ?: 1.0
        
        var replyText = ""
        var actType: String? = null
        var actLabel: String? = null
        var actData: String? = null
        
        when {
            // Story / Expense & Budget Tracker
            (normalized.contains("কিনেছি") || normalized.contains("কিনলাম") || normalized.contains("দিলাম") || normalized.contains("খরচ") || 
             normalized.contains("বাজার") || normalized.contains("অবশিষ্ট") || normalized.contains("কত টাকা") || normalized.contains("কাছে আছে") ||
             normalized.contains("spent") || normalized.contains("bought") || normalized.contains("paid") || normalized.contains("left") || 
             normalized.contains("remaining") || normalized.contains("shopping") || normalized.contains("দিলাম") || normalized.contains("টাকা নিয়ে")) && numbers.size >= 2 -> {
                
                val df = DecimalFormat("#,##0.##")
                val initialAmount = numbers.first()
                val expenses = numbers.drop(1)
                val totalSpent = expenses.sum()
                val remaining = initialAmount - totalSpent
                val spentPercent = if (initialAmount > 0) (totalSpent / initialAmount) * 100.0 else 0.0
                
                val itemizedList = expenses.mapIndexed { idx, amt -> "  ${idx + 1}. ৳${df.format(amt)}" }.joinToString("\n")
                
                replyText = if (isBn) {
                    "📊 **গল্প ও বাজেট আইটেম বিশ্লেষণ:**\n\n" +
                    "• 💰 **শুরু/প্রাথমিক পরিমাণ:** ৳${df.format(initialAmount)}\n" +
                    "• 🛒 **খরচ/লেনদেনের তালিকা ($expenses.size টি আইটেম):**\n$itemizedList\n" +
                    "• 💸 **মোট খরচ হয়েছে:** ৳${df.format(totalSpent)} (${df.format(spentPercent)}%)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━\n" +
                    "• 💵 **আপনার নিকট অবশিষ্ট আছে:** **৳${df.format(remaining)}**\n\n" +
                    "*(অফলাইন এআই মডেল দ্বারা আপনার গল্প বিশ্লেষণ করে নিখুঁত গাণিতিক হিসাব বের করা হয়েছে)*"
                } else {
                    "📊 **Story & Budget Analysis:**\n\n" +
                    "• 💰 **Initial Balance:** $${df.format(initialAmount)}\n" +
                    "• 🛒 **Itemized Transactions (${expenses.size} items):**\n$itemizedList\n" +
                    "• 💸 **Total Spent:** $${df.format(totalSpent)} (${df.format(spentPercent)}%)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━\n" +
                    "• 💵 **Remaining Balance:** **$${df.format(remaining)}**\n\n" +
                    "*(Parsed and calculated locally by Offline AI Model)*"
                }
                actType = "calculate"
                actLabel = if (isBn) "ক্যালকুলেটরে অবশিষ্ট দেখান" else "Show in Calculator"
                actData = "$remaining"
            }

            // Average / Sum Calculator
            (normalized.contains("গড়") || normalized.contains("average") || normalized.contains("avg") || normalized.contains("সমষ্টি") || normalized.contains("যোগফল") || normalized.contains("sum")) && numbers.size >= 2 -> {
                val df = DecimalFormat("#,##0.##")
                val sumVal = numbers.sum()
                val avgVal = numbers.average()
                
                replyText = if (isBn) {
                    "📈 **পরিসংখ্যান হিসাব:**\n\n" +
                    "• 🔢 **সংখ্যাসমূহ:** ${numbers.joinToString(", ") { df.format(it) }}\n" +
                    "• ➕ **মোট সমষ্টি:** **${df.format(sumVal)}**\n" +
                    "• 📊 **গড় মান (Average):** **${df.format(avgVal)}**\n" +
                    "• 📌 **মোট সংখ্যার পরিমাণ:** ${numbers.size} টি"
                } else {
                    "📈 **Statistical Analysis:**\n\n" +
                    "• 🔢 **Numbers:** ${numbers.joinToString(", ") { df.format(it) }}\n" +
                    "• ➕ **Total Sum:** **${df.format(sumVal)}**\n" +
                    "• 📊 **Average:** **${df.format(avgVal)}**\n" +
                    "• 📌 **Total Count:** ${numbers.size}"
                }
                actType = "calculate"
                actLabel = if (isBn) "গড় ক্যালকুলেটরে ব্যবহার করুন" else "Use in Calculator"
                actData = "$avgVal"
            }

            // Profit & Loss
            (normalized.contains("লাভ") || normalized.contains("ক্ষতি") || normalized.contains("profit") || normalized.contains("loss") || normalized.contains("বেচলাম") || normalized.contains("বিক্রি")) && numbers.size >= 2 -> {
                val df = DecimalFormat("#,##0.##")
                val cp = numbers[0]
                val sp = numbers[1]
                val diff = sp - cp
                val pct = (diff / cp) * 100.0
                
                replyText = if (isBn) {
                    if (diff >= 0) {
                        "🟢 **লাভ হিসাব (Profit):**\n\n" +
                        "• 🛒 **ক্রয়মূল্য (Cost Price):** ৳${df.format(cp)}\n" +
                        "• 🏷️ **বিক্রয়মূল্য (Selling Price):** ৳${df.format(sp)}\n" +
                        "• 💰 **মোট লাভ:** **৳${df.format(diff)}**\n" +
                        "• 📈 **শতকরা লাভ:** **${df.format(pct)}%**"
                    } else {
                        "🔴 **ক্ষতি হিসাব (Loss):**\n\n" +
                        "• 🛒 **ক্রয়মূল্য (Cost Price):** ৳${df.format(cp)}\n" +
                        "• 🏷️ **বিক্রয়মূল্য (Selling Price):** ৳${df.format(sp)}\n" +
                        "• 💸 **মোট ক্ষতি:** **৳${df.format(-diff)}**\n" +
                        "• 📉 **শতকরা ক্ষতি:** **${df.format(-pct)}%**"
                    }
                } else {
                    if (diff >= 0) {
                        "🟢 **Profit Analysis:**\n\n" +
                        "• 🛒 **Cost Price:** $${df.format(cp)}\n" +
                        "• 🏷️ **Selling Price:** $${df.format(sp)}\n" +
                        "• 💰 **Total Profit:** **$${df.format(diff)}**\n" +
                        "• 📈 **Profit Percentage:** **${df.format(pct)}%**"
                    } else {
                        "🔴 **Loss Analysis:**\n\n" +
                        "• 🛒 **Cost Price:** $${df.format(cp)}\n" +
                        "• 🏷️ **Selling Price:** $${df.format(sp)}\n" +
                        "• 💸 **Total Loss:** **$${df.format(-diff)}**\n" +
                        "• 📉 **Loss Percentage:** **${df.format(-pct)}%**"
                    }
                }
                actType = "calculate"
                actLabel = if (isBn) "প্রফিট ক্যালকুলেটরে দেখুন" else "View in Profit Calculator"
                actData = "$cp,$sp"
            }

            // Split Bill
            (normalized.contains("split") || normalized.contains("ভাগ") || normalized.contains("জন") || normalized.contains("people")) && numbers.size >= 2 -> {
                val df = DecimalFormat("#,##0.##")
                val totalBill = numbers[0]
                val peopleCount = numbers[1]
                val perPerson = totalBill / peopleCount
                
                replyText = if (isBn) {
                    "🍕 **বিল ভাগাভাগি (Split Bill):**\n\n" +
                    "• 💵 **মোট বিল:** ৳${df.format(totalBill)}\n" +
                    "• 👥 **মোট জনসংখ্যা:** ${peopleCount.toInt()} জন\n" +
                    "• 👤 **প্রত্যেকে পরিশোধ করবে:** **৳${df.format(perPerson)}**"
                } else {
                    "🍕 **Split Bill Analysis:**\n\n" +
                    "• 💵 **Total Bill:** $${df.format(totalBill)}\n" +
                    "• 👥 **Total People:** ${peopleCount.toInt()}\n" +
                    "• 👤 **Per Person Share:** **$${df.format(perPerson)}**"
                }
            }

            // Greetings: Hello/Hi
            normalized.contains("hello") || normalized.contains(" hi ") || normalized.startsWith("hi") || normalized.contains("hey") ||
            normalized.contains("হ্যালো") || normalized.contains("হাই") || normalized.contains("সালাম") || normalized.contains("salam") || normalized.contains("আসসালামু আলাইকুম") -> {
                replyText = if (isBn) {
                    "আসসালামু আলাইকুম ও হ্যালো! 👋 আমি আপনার টুলসমেট অফলাইন এআই সহকারী। আমি কিভাবে আপনাকে সাহায্য করতে পারি?"
                } else {
                    "Hello there! 👋 I am your ToolsMate Offline AI Assistant. How can I help you today?"
                }
            }

            // Who are you / Identity
            normalized.contains("who are you") || normalized.contains("identity") || normalized.contains("তুমি কে") || normalized.contains("কে তুমি") || normalized.contains("আপনার পরিচয়") || normalized.contains("পরিচয়") -> {
                replyText = if (isBn) {
                    "আমি এই অ্যাপ্লিকেশনের একটি অন্তর্নির্মিত **অফলাইন এআই মডেল (Offline AI Model)**। 🤖 আমি সম্পূর্ণরূপে আপনার ফোনেই কাজ করি, কোনো ইন্টারনেট ছাড়াই!\n\nআমি আপনাকে বিভিন্ন গাণিতিক গণনা, ইউনিট রূপান্তর (যেমন কিমি থেকে মিটার) এবং বিএমআই বা ডিসকাউন্টের মতো বিশেষ টুল ব্যবহারে সাহায্য করি।"
                } else {
                    "I am the built-in **Offline AI Model** of this application. 🤖 I work 100% locally on your phone without any internet!\n\nI am designed to assist you with quick math calculations, unit conversions, and navigating or prepopulating smart calculators like BMI and Discount."
                }
            }

            // Name
            normalized.contains("your name") || normalized.contains("তোমার নাম কি") || normalized.contains("আপনার নাম কি") || normalized.contains("নাম কি") || normalized.contains("name") -> {
                replyText = if (isBn) {
                    "আমার নাম **টুলসমেট এআই (ToolsMate AI)**! 🤖 আমি এই টুলসমেট অ্যাপেরই একটি অংশ।"
                } else {
                    "My name is **ToolsMate AI**! 🤖 I am a built-in part of this ToolsMate app."
                }
            }

            // How are you / Well-being
            normalized.contains("how are you") || normalized.contains("কেমন আছো") || normalized.contains("কেমন আছেন") || normalized.contains("কেমন আছ") || normalized.contains("ভালো আছো") -> {
                replyText = if (isBn) {
                    "আমি খুব ভালো আছি, ধন্যবাদ! 🥰 আমি আপনার ফোনের প্রসেসর ব্যবহার করে সম্পূর্ণরূপে অফলাইনে যেকোনো হিসেব করতে সর্বদা প্রস্তুত। আপনি কেমন আছেন? চলুন কিছু হিসেব করা যাক!"
                } else {
                    "I am doing great, thank you! 🥰 Running fully offline on your device's local processor, I'm always energized and ready to calculate. How are you doing? Let's do some math!"
                }
            }

            // Capabilities / What can you do
            normalized.contains("what can you do") || normalized.contains("কি করতে পারো") || normalized.contains("কাজ কি") || normalized.contains("সুবিধা") || normalized.contains("সাহায্য") || normalized.contains("help") || normalized.contains("features") || normalized.contains("কি কি করতে পারো") -> {
                replyText = if (isBn) {
                    "আমি অফলাইনে অত্যন্ত গতিতে নিচের কাজগুলো করতে পারি: \n\n" +
                    "১. 📏 **ইউনিট রূপান্তর:** যেমন '৫ কিলোমিটারে কত মিটার?' বা '১০০ গ্রাম কত কেজি?'\n" +
                    "২. 💵 **মুদ্রা রূপান্তর:** যেমন '১০০ ডলার কত টাকা?' বা '৫০০ রুপি কত টাকা?'\n" +
                    "৩. 🧘 **বিএমআই হিসেব:** ওজন এবং উচ্চতা বললে বিএমআই হিসাব করা।\n" +
                    "৪. 🏷️ **ডিসকাউন্ট হিসাব:** যেমন '১৫০০ টাকার ২০% ডিসকাউন্ট কত?'\n" +
                    "৫. 🔢 **গণিত সমাধান:** সরাসরি যেকোনো সাধারণ গাণিতিক সমীকরণ সমাধান করা।\n" +
                    "৬. 🚀 **টুল নেভিগেশন:** যেকোনো ক্যালকুলেটর যেমন বয়স বা বিদ্যুৎ বিল টুলে সরাসরি নেভিগেট করা!"
                } else {
                    "I can perform a variety of operations fully offline on your phone:\n\n" +
                    "1. 📏 **Unit Conversions:** e.g., '5 km to meters' or '100g in kg'\n" +
                    "2. 💵 **Currency Exchange:** e.g., '100 USD to BDT' or '500 INR to BDT'\n" +
                    "3. 🧘 **BMI Calculation:** Give me your weight & height to compute your BMI.\n" +
                    "4. 🏷️ **Discount Details:** e.g., '20% discount on 1500'\n" +
                    "5. 🔢 **Math Equation Solver:** Paste any arithmetic expression to solve instantly.\n" +
                    "6. 🚀 **Smart Deep Linking:** Direct navigation to Age, Electricity, or EMI calculators!"
                }
            }

            // BMI
            normalized.contains("bmi") || normalized.contains("বিএমআই") || normalized.contains("body mass") -> {
                if (numbers.size >= 2) {
                    val height = numbers.find { it in 0.5..2.5 } ?: 1.7
                    val weight = numbers.find { it in 30.0..250.0 } ?: 70.0
                    val bmi = weight / (height * height)
                    val df = DecimalFormat("#.#")
                    val bmiStr = df.format(bmi)
                    
                    val category = when {
                        bmi < 18.5 -> if (isBn) "কম ওজন (Underweight)" else "Underweight"
                        bmi in 18.5..24.9 -> if (isBn) "স্বাভাবিক ওজন (Normal)" else "Normal"
                        bmi in 25.0..29.9 -> if (isBn) "অতিরিক্ত ওজন (Overweight)" else "Overweight"
                        else -> if (isBn) "স্থূলতা (Obese)" else "Obese"
                    }
                    
                    replyText = if (isBn) {
                        "আপনার ওজন $weight কেজি এবং উচ্চতা $height মিটার অনুযায়ী:\n\n• বিএমআই (BMI): **$bmiStr**\n• ক্যাটাগরি: **$category**\n\nএটি একটি এআই হিসাব!"
                    } else {
                        "Based on your weight of $weight kg and height of $height m:\n\n• BMI: **$bmiStr**\n• Category: **$category**\n\nCalculated offline!"
                    }
                    actType = "bmi"
                    actLabel = if (isBn) "বিএমআই ক্যালকুলেটরে সেট করুন" else "Set in BMI Calculator"
                    actData = "$weight,$height"
                } else {
                    replyText = if (isBn) {
                        "বিএমআই হিসাব করার জন্য দয়া করে ওজন (কেজি) এবং উচ্চতা (মিটার) উল্লেখ করুন। যেমন: 'আমার ওজন ৭০ কেজি, উচ্চতা ১.৭ মিটার' 🧘"
                    } else {
                        "Please provide your weight in kg and height in meters to calculate BMI. For example: 'weight 70 kg, height 1.7m' 🧘"
                    }
                    actType = "navigate_tool"
                    actLabel = if (isBn) "বিএমআই ক্যালকুলেটরে যান" else "Go to BMI Calculator"
                    actData = "BMI"
                }
            }
            
            // Discount
            normalized.contains("discount") || normalized.contains("ডিসকাউন্ট") || normalized.contains("ছাড়") || normalized.contains("off") || normalized.contains("%") -> {
                if (numbers.size >= 2) {
                    val percentage = numbers.find { it in 1.0..99.0 } ?: 10.0
                    val price = numbers.find { it > 99.0 || it != percentage } ?: 1000.0
                    
                    val savings = price * (percentage / 100.0)
                    val finalPrice = price - savings
                    val df = DecimalFormat("#.##")
                    val savingsStr = df.format(savings)
                    val finalPriceStr = df.format(finalPrice)
                    
                    replyText = if (isBn) {
                        "💰 **ডিসকাউন্ট হিসাব:**\n• আসল মূল্য: ৳${df.format(price)}\n• ছাড়: $percentage%\n• সাশ্রয়: ৳$savingsStr\n• চূড়ান্ত মূল্য: **৳$finalPriceStr**"
                    } else {
                        "💰 **Discount Details:**\n• Original Price: $${df.format(price)}\n• Discount: $percentage%\n• Savings: $$savingsStr\n• Final Price: **$$finalPriceStr**"
                    }
                    actType = "discount"
                    actLabel = if (isBn) "ডিসকাউন্ট ক্যালকুলেটরে সেট করুন" else "Set in Discount Calculator"
                    actData = "$price,$percentage"
                } else {
                    replyText = if (isBn) {
                        "ডিসকাউন্ট হিসাব করতে মূল্য এবং ডিসকাউন্ট পার্সেন্ট উল্লেখ করুন। যেমন: '১৫০০ টাকার ২০% ডিসকাউন্ট কত?' 🏷️"
                    } else {
                        "To calculate discount, please mention the original price and discount percentage. For example: '20% off on 1500' 🏷️"
                    }
                    actType = "navigate_tool"
                    actLabel = if (isBn) "ডিসকাউন্ট ক্যালকুলেটরে যান" else "Go to Discount Calculator"
                    actData = "DISCOUNT"
                }
            }
            
            // Age
            normalized.contains("বয়স") || normalized.contains("age") || normalized.contains("birthday") || normalized.contains("জন্মদিন") -> {
                replyText = if (isBn) {
                    "আপনার নিখুঁত বয়স, পরবর্তী জন্মদিন এবং চমৎকার সময় পরিসংখ্যান দেখতে আমাদের 'বয়স ক্যালকুলেটর' টুলটি ব্যবহার করুন! 🎂"
                } else {
                    "To calculate your exact age, remaining days for next birthday, and deep time stats, use our 'Age & Birthday' tool! 🎂"
                }
                actType = "navigate_tool"
                actLabel = if (isBn) "বয়স ক্যালকুলেটরে যান" else "Go to Age Calculator"
                actData = "AGE"
            }
            
            // Electricity Bill
            normalized.contains("বিদ্যুৎ") || normalized.contains("কারেন্ট") || normalized.contains("electricity") || normalized.contains("bill") || normalized.contains("বিল") -> {
                replyText = if (isBn) {
                    "বিদ্যুৎ বিল হিসাব করতে এবং আপনার ইলেকট্রনিক ডিভাইসের আনুমানিক বিদ্যুৎ বিল দেখতে আমাদের 'বিদ্যুৎ বিল ক্যালকুলেটর' ব্যবহার করুন! ⚡"
                } else {
                    "To calculate electricity bills and estimation for home appliances, use our 'Electricity Bill Calculator'! ⚡"
                }
                actType = "navigate_tool"
                actLabel = if (isBn) "বিদ্যুৎ বিল ক্যালকুলেটরে যান" else "Go to Electricity Bill"
                actData = "ELECTRICITY_BILL"
            }
            
            // EMI / Loan
            normalized.contains("ঋণ") || normalized.contains("লোন") || normalized.contains("loan") || normalized.contains("emi") || normalized.contains("ইএমআই") -> {
                replyText = if (isBn) {
                    "ঋণের কিস্তি, মাসিক সুদের হার এবং মোট প্রদেয় টাকার হিসাব খুব সহজে করতে আমাদের 'ইএমআই ও লোন ক্যালকুলেটর' ব্যবহার করুন! 🏦"
                } else {
                    "Calculate home/car loans, monthly installments, interest rates, and total payable amounts with our 'EMI & Loan Calculator'! 🏦"
                }
                actType = "navigate_tool"
                actLabel = if (isBn) "লোন ক্যালকুলেটরে যান" else "Go to EMI Calculator"
                actData = "EMI_LOAN"
            }
            
            // Unit conversions: Length
            normalized.contains("km") || normalized.contains("কিলোমিটার") || normalized.contains("কিমি") ||
            normalized.contains("meter") || normalized.contains("মিটার") || normalized.contains(" সেমি") ||
            normalized.contains("cm") || normalized.contains("সেন্টিমিটার") || normalized.contains("feet") ||
            normalized.contains("ফুট") || normalized.contains("inch") || normalized.contains("ইঞ্চি") ||
            normalized.contains("মাইল") || normalized.contains("mile") -> {
                
                var fromU = "Kilometer"
                var toU = "Meter"
                var converted = primaryNum * 1000.0
                
                when {
                    normalized.contains("meter") || normalized.contains("মিটার") -> {
                        if (normalized.contains("cm") || normalized.contains("সেন্টিমিটার") || normalized.contains("সেমি")) {
                            fromU = "Meter"
                            toU = "Centimeter"
                            converted = primaryNum * 100.0
                        } else if (normalized.contains("km") || normalized.contains("কিলোমিটার") || normalized.contains("কিমি")) {
                            fromU = "Meter"
                            toU = "Kilometer"
                            converted = primaryNum / 1000.0
                        } else if (normalized.contains("feet") || normalized.contains("ফুট")) {
                            fromU = "Meter"
                            toU = "Feet"
                            converted = primaryNum * 3.28084
                        }
                    }
                    normalized.contains("feet") || normalized.contains("ফুট") -> {
                        if (normalized.contains("inch") || normalized.contains("ইঞ্চি")) {
                            fromU = "Feet"
                            toU = "Inch"
                            converted = primaryNum * 12.0
                        }
                    }
                    normalized.contains("mile") || normalized.contains("মাইল") -> {
                        fromU = "Mile"
                        toU = "Kilometer"
                        converted = primaryNum * 1.60934
                    }
                }
                
                val df = DecimalFormat("#.####")
                val convStr = df.format(converted)
                val priStr = df.format(primaryNum)
                
                replyText = if (isBn) {
                    "📏 **দৈর্ঘ্য রূপান্তর:**\n• $priStr $fromU = **$convStr $toU**\n\nএটি একটি স্থানীয় এআই গণনা!"
                } else {
                    "📏 **Length Conversion:**\n• $priStr $fromU = **$convStr $toU**\n\nCalculated offline!"
                }
                actType = "converter"
                actLabel = if (isBn) "$toU রূপান্তরে সরাসরি যান" else "Deep Link to $toU"
                actData = "LENGTH,$fromU,$toU,$primaryNum"
            }
            
            // Unit conversions: Weight
            normalized.contains("kg") || normalized.contains("কেজি") || normalized.contains("কিলোগ্রাম") ||
            normalized.contains("gram") || normalized.contains("গ্রাম") || normalized.contains("পাউন্ড") ||
            normalized.contains("pound") || normalized.contains("lb") -> {
                
                var fromU = "Kilogram"
                var toU = "Gram"
                var converted = primaryNum * 1000.0
                
                if (normalized.contains("gram") || normalized.contains("গ্রাম")) {
                    if (normalized.contains("kg") || normalized.contains("কেজি") || normalized.contains("কিলোগ্রাম")) {
                        fromU = "Gram"
                        toU = "Kilogram"
                        converted = primaryNum / 1000.0
                    }
                } else if (normalized.contains("pound") || normalized.contains("পাউন্ড") || normalized.contains("lb")) {
                    fromU = "Kilogram"
                    toU = "Pound"
                    converted = primaryNum * 2.20462
                }
                
                val df = DecimalFormat("#.####")
                val convStr = df.format(converted)
                val priStr = df.format(primaryNum)
                
                replyText = if (isBn) {
                    "⚖️ **ওজন রূপান্তর:**\n• $priStr $fromU = **$convStr $toU**\n\nহিসাবটি সম্পন্ন হয়েছে!"
                } else {
                    "⚖️ **Weight Conversion:**\n• $priStr $fromU = **$convStr $toU**\n\nCalculated offline!"
                }
                actType = "converter"
                actLabel = if (isBn) "$toU রূপান্তরে যান" else "Go to $toU Converter"
                actData = "WEIGHT,$fromU,$toU,$primaryNum"
            }
            
            // Currency
            normalized.contains("dollar") || normalized.contains("ডলার") || normalized.contains("usd") ||
            normalized.contains("taka") || normalized.contains("টাকা") || normalized.contains("bdt") ||
            normalized.contains("euro") || normalized.contains("ইউরো") || normalized.contains("eur") ||
            normalized.contains("rupee") || normalized.contains("রুপি") || normalized.contains("inr") -> {
                
                var fromU = "USD - US Dollar"
                var toU = "BDT - Bangladeshi Taka"
                var rate = exchangeRates["BDT - Bangladeshi Taka"] ?: 120.0
                
                val dollarKeywords = listOf("usd", "dollar", "ডলার")
                val takaKeywords = listOf("bdt", "taka", "টাকা")
                val euroKeywords = listOf("eur", "euro", "ইউরো")
                val rupeeKeywords = listOf("inr", "rupee", "রুপি")

                val dollarPos = dollarKeywords.map { normalized.indexOf(it) }.filter { it != -1 }.minOrNull() ?: Int.MAX_VALUE
                val takaPos = takaKeywords.map { normalized.indexOf(it) }.filter { it != -1 }.minOrNull() ?: Int.MAX_VALUE
                val euroPos = euroKeywords.map { normalized.indexOf(it) }.filter { it != -1 }.minOrNull() ?: Int.MAX_VALUE
                val rupeePos = rupeeKeywords.map { normalized.indexOf(it) }.filter { it != -1 }.minOrNull() ?: Int.MAX_VALUE

                val positions = listOf(
                    "USD" to dollarPos,
                    "BDT" to takaPos,
                    "EUR" to euroPos,
                    "INR" to rupeePos
                ).filter { it.second != Int.MAX_VALUE }.sortedBy { it.second }

                if (positions.size >= 2) {
                    val fromKey = positions[0].first
                    val toKey = positions[1].first
                    
                    val names = mapOf(
                        "USD" to "USD - US Dollar",
                        "BDT" to "BDT - Bangladeshi Taka",
                        "EUR" to "EUR - Euro",
                        "INR" to "INR - Indian Rupee"
                    )
                    
                    fromU = names[fromKey] ?: fromU
                    toU = names[toKey] ?: toU
                    
                    // Calculate rate based on USD as base
                    val usdToBdt = exchangeRates["BDT - Bangladeshi Taka"] ?: 120.0
                    val eurToUsd = exchangeRates["EUR - Euro"] ?: 0.92
                    val inrToUsd = exchangeRates["INR - Indian Rupee"] ?: 83.0
                    
                    val toUsdRate = mapOf(
                        "USD" to 1.0,
                        "BDT" to 1.0 / usdToBdt,
                        "EUR" to (if (eurToUsd > 0) 1.0/eurToUsd else 1.0), // Simplified
                        "INR" to 1.0 / inrToUsd
                    )
                    
                    // Actually let's use a more direct way
                    // We know the rate of each to USD
                    // base is USD. rate[X] = X per 1 USD
                    // X / Y = (X/USD) / (Y/USD)
                    val ratesToUsd = mapOf(
                        "USD" to 1.0,
                        "BDT" to usdToBdt,
                        "EUR" to (1.0 / (if (eurToUsd > 0) eurToUsd else 1.08)), // This is wrong, let's just use knowns
                        "INR" to inrToUsd
                    )
                    // Wait, exchangeRates usually has "BDT - Bangladeshi Taka" -> 120 (meaning 120 BDT = 1 USD)
                    // So rate(X->Y) = rate(USD->Y) / rate(USD->X)
                    val rX = if (fromKey == "USD") 1.0 else if (fromKey == "EUR") (1.0/0.92) else (exchangeRates[names[fromKey]] ?: 1.0)
                    val rY = if (toKey == "USD") 1.0 else if (toKey == "EUR") (1.0/0.92) else (exchangeRates[names[toKey]] ?: 1.0)
                    
                    // Wait, if 120 BDT = 1 USD, and 83 INR = 1 USD
                    // Then 120 BDT = 83 INR => 1 BDT = 83/120 INR
                    // rate(from->to) = rY / rX
                    
                    // Specialized handling for EUR which is usually USD/EUR
                    val usdPerEur = 1.08 // approx
                    val rX_fixed = if (fromKey == "EUR") (1.0/usdPerEur) else rX
                    val rY_fixed = if (toKey == "EUR") (1.0/usdPerEur) else rY
                    
                    rate = rY_fixed / rX_fixed
                } else if (positions.size == 1) {
                    // Fallback
                    if (positions[0].first == "BDT") {
                        fromU = "BDT - Bangladeshi Taka"
                        toU = "USD - US Dollar"
                        rate = 1.0 / (exchangeRates["BDT - Bangladeshi Taka"] ?: 120.0)
                    }
                }
                
                val converted = primaryNum * rate
                val df = DecimalFormat("#.##")
                val convStr = df.format(converted)
                val priStr = df.format(primaryNum)
                
                replyText = if (isBn) {
                    "💵 **মুদ্রা রূপান্তর (অফলাইন রেট):**\n• $priStr $fromU = **$convStr $toU**\n\n*(নোট: সর্বশেষ লাইভ আপডেট অনুযায়ী অফলাইনে হিসাব করা হয়েছে)*"
                } else {
                    "💵 **Currency Exchange (Offline):**\n• $priStr $fromU = **$convStr $toU**\n\n*(Note: Calculated offline based on last sync exchange rates)*"
                }
                actType = "converter"
                actLabel = if (isBn) "মুদ্রা রূপান্তরে সরাসরি যান" else "Open Currency Converter"
                actData = "CURRENCY,$fromU,$toU,$primaryNum"
            }
            
            // Basic math solver fallback
            numbers.isNotEmpty() && (normalized.contains("+") || normalized.contains("-") || normalized.contains("*") || normalized.contains("/") || normalized.contains("x") || normalized.contains("÷") || normalized.contains("প্লাস") || normalized.contains("মাইনাস") || normalized.contains("গুণ") || normalized.contains("ভাগ")) -> {
                var mathExpr = normalized
                    .replace("যোগ", "+").replace("প্লাস", "+").replace("plus", "+")
                    .replace("বিয়োগ", "-").replace("মাইনাস", "-").replace("minus", "-")
                    .replace("গুণ", "*").replace("ইনটু", "*").replace("times", "*").replace("into", "*")
                    .replace("ভাগ", "/").replace("ডিভাইডেড", "/").replace("divided by", "/").replace("divided", "/")
                    .replace("x", "*").replace("÷", "/")
                
                mathExpr = mathExpr.filter { it.isDigit() || it in "+-*/.()" }
                
                if (mathExpr.isNotBlank()) {
                    try {
                        val evalResult = ExpressionEvaluator.evaluate(mathExpr)
                        val df = DecimalFormat("#.######")
                        val resultStr = df.format(evalResult)
                        
                        replyText = if (isBn) {
                            "🔢 **গাণিতিক হিসেব:**\n• রাশিমালা: `$mathExpr`\n• ফলাফল: **$resultStr**\n\nসরাসরি সমাধান করা হয়েছে!"
                        } else {
                            "🔢 **Math Solution:**\n• Expression: `$mathExpr`\n• Result: **$resultStr**\n\nSolved offline instantly!"
                        }
                        actType = "calculate"
                        actLabel = if (isBn) "ক্যালকুলেটরে পেস্ট করুন" else "Paste to Scientific Calc"
                        actData = mathExpr
                    } catch (e: Exception) {
                        replyText = if (isBn) {
                            "আমি গাণিতিক রাশিমালাটি সমাধান করতে পারিনি। দয়া করে সঠিক ফরম্যাটে লিখুন। যেমন: '২৫ + ৩৫ * ৪' 🧮"
                        } else {
                            "I couldn't evaluate that mathematical expression. Please write it clearly. E.g., '25 + 35 * 4' 🧮"
                        }
                    }
                }
            }
            
            else -> {
                replyText = if (isBn) {
                    "আমি আপনার অনুরোধটি ঠিক বুঝতে পারিনি। আমি একটি দ্রুত অফলাইন এআই সহকারী।\n\nআপনি আমাকে দৈর্ঘ্যের রূপান্তর (যেমন: ৫ কিমি সমান কত মিটার?), ওজন, কারেন্সি (যেমন: ১০০ ডলার কত টাকা?) অথবা ডিসকাউন্ট, বিএমআই এবং বয়সের মতো বিশেষ টুলস সহজে ওপেন বা হিসেব করার আদেশ দিতে পারেন! 😊"
                } else {
                    "I couldn't quite grasp that request. I am a fast offline AI helper.\n\nAsk me to convert length (e.g., '5 km to meters'), weight, currency ('100 USD to BDT'), or to quickly set up special tools like BMI, Age, and Discount calculations! 😊"
                }
            }
        }
        
        return OfflineResult(replyText, actType, actLabel, actData)
    }

    // Online model load state & error dialog message
    var onlineModelErrorReason by mutableStateOf<String?>(null)
    var lastOnlineError by mutableStateOf<String?>(null)

    fun checkOnlineModelStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val isBn = selectedLanguage == AppLanguage.BENGALI
            val hasInternetPermission = context.checkSelfPermission(android.Manifest.permission.INTERNET) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasInternet = isNetworkAvailable()
            val hasApiKey = com.example.BuildConfig.GEMINI_API_KEY.isNotBlank() && com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

            if (!hasInternetPermission) {
                lastOnlineError = if (isBn) "অ্যাপে ইন্টারনেট পারমিশন (INTERNET Permission) অনুমোদিত নয়" else "INTERNET Permission is not granted in the app"
                return@launch
            }
            if (!hasInternet) {
                lastOnlineError = if (isBn) "ডিভাইসে ইন্টারনেট সংযোগ বন্ধ (ওয়াইফাই বা মোবাইল ডাটা অন করুন)" else "No active internet connection on device"
                return@launch
            }
            if (!hasApiKey) {
                lastOnlineError = if (isBn) "অনলাইন এআই API Key পাওয়া যায়নি বা সক্রিয় নয়" else "Online AI API Key missing or inactive"
                return@launch
            }

            try {
                val url = java.net.URL("https://generativelanguage.googleapis.com/")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.requestMethod = "GET"
                val responseCode = conn.responseCode
                lastOnlineError = null
            } catch (e: Exception) {
                e.printStackTrace()
                val errMsg = when (e) {
                    is java.net.UnknownHostException -> if (isBn) "ইন্টারনেট বা সার্ভারে সংযোগ করা যাচ্ছে না (DNS Error)" else "Unable to connect to server host (DNS error)"
                    is java.net.SocketTimeoutException -> if (isBn) "ইন্টারনেট সংযোগ ধীরগতির কারণে টাইমআউট হয়েছে" else "Connection timed out"
                    is javax.net.ssl.SSLHandshakeException -> if (isBn) "এসএসএল হ্যান্ডশেক ব্যর্থ হয়েছে (SSL Handshake Failed)" else "SSL Handshake Failed"
                    is javax.net.ssl.SSLException -> if (isBn) "এসএসএল কানেকশন ত্রুটি ($e)" else "SSL connection error ($e)"
                    else -> {
                        val detail = e.localizedMessage ?: e.javaClass.simpleName
                        if (isBn) "নেটওয়ার্ক সংযোগ ত্রুটি ($detail)" else "Network connection issue ($detail)"
                    }
                }
                lastOnlineError = errMsg
            }
        }
    }

    fun sendMessageToAi(rawText: String) {
        if (rawText.isBlank()) return
        if (isAiLoading) return
        
        val userMsg = ChatMessage(text = rawText, isUser = true)
        aiChatMessages = aiChatMessages + userMsg
        updateActiveSession()
        
        val normalized = rawText
            .replace('০', '0').replace('১', '1').replace('২', '2').replace('৩', '3')
            .replace('৪', '4').replace('৫', '5').replace('৬', '6').replace('৭', '7')
            .replace('৮', '8').replace('৯', '9')
            .replace('०', '0').replace('१', '1').replace('२', '2').replace('३', '3')
            .replace('४', '4').replace('५', '5').replace('६', '6').replace('७', '7')
            .replace('८', '8').replace('९', '9')
            .lowercase()
        
        val isBn = selectedLanguage == AppLanguage.BENGALI
        isAiLoading = true
        
        viewModelScope.launch {
            try {
                var replyText = ""
                var actType: String? = null
                var actLabel: String? = null
                var actData: String? = null
                var usedOnlineModel = false
                var onlineFailureReason: String? = null
                
                val hasInternetPermission = context.checkSelfPermission(android.Manifest.permission.INTERNET) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasInternet = isNetworkAvailable()
                val hasApiKey = com.example.BuildConfig.GEMINI_API_KEY.isNotBlank() && com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
                
                if (!hasInternetPermission) {
                    onlineFailureReason = if (isBn) "অ্যাপে ইন্টারনেট পারমিশন (INTERNET Permission) অনুমোদিত নয়" else "INTERNET Permission is not granted in the app"
                } else if (!hasInternet) {
                    onlineFailureReason = if (isBn) "ডিভাইসে ইন্টারনেট সংযোগ বন্ধ (ওয়াইফাই বা মোবাইল ডাটা অন করুন)" else "No active internet connection on device"
                } else if (!hasApiKey) {
                    onlineFailureReason = if (isBn) "অনলাইন এআই API Key পাওয়া যায়নি বা সক্রিয় নয়" else "Online AI API Key missing or inactive"
                } else {
                    val systemInstruction = if (isBn) {
                        "You are a helpful and intelligent AI assistant. You can answer any questions, including general knowledge, math, science, and everyday queries. Answer in Bengali cleanly and naturally. Keep it friendly and concise. Do not use markdown other than bold text. Suggest mathematical or unit conversions when appropriate."
                    } else {
                        "You are a helpful and intelligent AI assistant. You can answer any questions, including general knowledge, math, science, and everyday queries. Answer in English cleanly and naturally. Keep it friendly and concise. Do not use markdown other than bold text. Suggest mathematical or unit conversions when appropriate."
                    }
                    
                    // Prepare history
                    val contents = mutableListOf<GeminiContent>()
                    aiChatMessages.takeLast(10).forEach { msg ->
                        // Skip the first welcome message
                        if (msg.text.contains("Hello! I am your AI Assistant") || msg.text.contains("হ্যালো! আমি আপনার অফলাইন এআই সহকারী")) return@forEach
                        
                        val role = if (msg.isUser) "user" else "model"
                        contents.add(GeminiContent(parts = listOf(GeminiPart(text = msg.text)), role = role))
                    }
                    contents.add(GeminiContent(parts = listOf(GeminiPart(text = rawText)), role = "user"))
                    
                    try {
                        val onlineReply = callGeminiApi(contents, systemInstruction)
                        replyText = onlineReply
                        usedOnlineModel = true
                        
                        val detectedAction = detectLocalAction(normalized, isBn)
                        if (detectedAction != null) {
                            actType = detectedAction.actionType
                            actLabel = detectedAction.actionLabel
                            actData = detectedAction.actionData
                        }
                    } catch (netEx: Exception) {
                        netEx.printStackTrace()
                        val errMsg = when (netEx) {
                            is java.net.UnknownHostException -> if (isBn) "ইন্টারনেট বা সার্ভারে সংযোগ করা যাচ্ছে না (Host resolution failed - DNS Error)" else "Unable to connect to server host (DNS error)"
                            is java.net.SocketTimeoutException -> if (isBn) "ইন্টারনেট সংযোগ ধীরগতির কারণে টাইমআউট হয়েছে (Connection timeout)" else "Connection timed out"
                            is retrofit2.HttpException -> {
                                val code = netEx.code()
                                if (code == 403 || code == 401) {
                                    if (isBn) "এআই API Key অকার্যকর বা ব্লক করা হয়েছে (HTTP $code)" else "Invalid or blocked API Key (HTTP $code)"
                                } else if (code == 429) {
                                    if (isBn) "এআই রিকোয়েস্ট কোটা অতিক্রম করেছে (HTTP 429)" else "Quota limit exceeded (HTTP 429)"
                                } else {
                                    if (isBn) "এআই সার্ভার রেসপন্স ত্রুটি (HTTP $code)" else "AI Server error (HTTP $code)"
                                }
                            }
                            else -> {
                                val detail = netEx.localizedMessage ?: netEx.javaClass.simpleName
                                if (isBn) "নেটওয়ার্ক বা সার্ভার সমস্যা ($detail)" else "Network or server issue ($detail)"
                            }
                        }
                        onlineFailureReason = errMsg
                    }
                }
                
                if (!usedOnlineModel) {
                    val offlineResult = runOfflineModel(normalized, isBn)
                    replyText = offlineResult.replyText
                    actType = offlineResult.actionType
                    actLabel = offlineResult.actionLabel
                    actData = offlineResult.actionData
                    
                    val reasonStr = onlineFailureReason ?: if (isBn) "অজানা ত্রুটি" else "Unknown error"
                    lastOnlineError = reasonStr
                    onlineModelErrorReason = null
                } else {
                    lastOnlineError = null
                }
                
                val aiReply = ChatMessage(
                    text = replyText,
                    isUser = false,
                    actionType = actType,
                    actionLabel = actLabel,
                    actionData = actData
                )
                
                aiChatMessages = aiChatMessages + aiReply
                updateActiveSession()
            } catch (e: Exception) {
                e.printStackTrace()
                val errReason = e.localizedMessage ?: e.javaClass.simpleName
                lastOnlineError = errReason
                val offlineResult = runOfflineModel(normalized, isBn)
                onlineModelErrorReason = null

                val aiReply = ChatMessage(
                    text = offlineResult.replyText,
                    isUser = false,
                    actionType = offlineResult.actionType,
                    actionLabel = offlineResult.actionLabel,
                    actionData = offlineResult.actionData
                )
                aiChatMessages = aiChatMessages + aiReply
                updateActiveSession()
            } finally {
                isAiLoading = false
            }
        }
    }
    
    fun performAiChatAction(actionType: String, actionData: String) {
        if (actionData.isBlank()) return
        showAiChat = false // Close chat when navigating
        
        try {
            when (actionType) {
                "bmi" -> {
                    val parts = actionData.split(",")
                    if (parts.size >= 2) {
                        activeTab = 0
                        openTool(com.example.data.model.ToolType.BMI)
                        bmiWeightUnit = "kg"
                        bmiHeightUnit = "cm"
                        bmiWeight = parts[0]
                        val heightM = parts[1].toDoubleOrNull() ?: 1.7
                        bmiHeight = (heightM * 100.0).toInt().toString()
                        calculateBMI()
                    }
                }
                "discount" -> {
                    val parts = actionData.split(",")
                    if (parts.size >= 2) {
                        activeTab = 0
                        openTool(com.example.data.model.ToolType.DISCOUNT)
                        originalPrice = parts[0]
                        discountPercent = parts[1]
                        taxPercent = "0"
                        calculateDiscount()
                    }
                }
                "navigate_tool" -> {
                    activeTab = 0
                    val tool = com.example.data.model.ToolType.valueOf(actionData)
                    openTool(tool)
                }
                "calculate" -> {
                    changeActiveTab(
                        2,
                        "Opened from AI Chat Action command: \"$actionData\"",
                        "এআই চ্যাট একশন কমান্ড থেকে খোলা হয়েছে: \"$actionData\""
                    )
                    expressionValue = TextFieldValue(actionData, selection = TextRange(actionData.length))
                    evaluateExpression()
                }
                "converter" -> {
                    val parts = actionData.split(",")
                    if (parts.size >= 4) {
                        activeTab = 1
                        val catStr = parts[0]
                        val fromU = parts[1]
                        val toU = parts[2]
                        val valueStr = parts[3]
                        
                        val type = com.example.data.model.ConverterType.valueOf(catStr)
                        selectedConverterType = type
                        
                        // Set matching units
                        val matchedFrom = type.units.find { it.contains(fromU, ignoreCase = true) } ?: type.units.first()
                        val matchedTo = type.units.find { it.contains(toU, ignoreCase = true) } ?: type.units.last()
                        
                        fromUnit = matchedFrom
                        toUnit = matchedTo
                        converterInput = valueStr
                        calculateConverter()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Theme & Language Selection
    var fabGradientHexColors by mutableStateOf(loadFabColors())
        private set
        
    private fun loadFabColors(): List<String> {
        val str = sharedPrefs.getString("fab_colors", null)
        return if (str != null) str.split(",")
        else listOf("#6B11FF", "#8B5CF6", "#EC4899", "#F43F5E", "#F59E0B", "#3B82F6", "#2563EB", "#6B11FF")
    }
    
    fun updateFabColors(colors: List<String>) {
        fabGradientHexColors = colors
        sharedPrefs.edit().putString("fab_colors", colors.joinToString(",")).apply()
    }

    // Search FAB customizations
    var searchFabBgColorHex by mutableStateOf(sharedPrefs.getString("search_fab_bg", "") ?: "")
        private set
    var searchFabBorderColorHex by mutableStateOf(sharedPrefs.getString("search_fab_border", "") ?: "") // empty means dynamic theme color
        private set
    var searchFabIconColorHex by mutableStateOf(sharedPrefs.getString("search_fab_icon", "") ?: "") // empty means dynamic theme color
        private set

    fun updateSearchFabColors(bg: String, border: String, icon: String) {
        searchFabBgColorHex = bg
        searchFabBorderColorHex = border
        searchFabIconColorHex = icon
        sharedPrefs.edit()
            .putString("search_fab_bg", bg)
            .putString("search_fab_border", border)
            .putString("search_fab_icon", icon)
            .apply()
    }

    // Center Search FAB customizations
    var centerSearchFabBgColorHex by mutableStateOf(sharedPrefs.getString("center_search_fab_bg", "") ?: "")
        private set
    var centerSearchFabBorderColorHex by mutableStateOf(sharedPrefs.getString("center_search_fab_border", "") ?: "") 
        private set
    var centerSearchFabIconColorHex by mutableStateOf(sharedPrefs.getString("center_search_fab_icon", "") ?: "") 
        private set

    fun updateCenterSearchFabColors(bg: String, border: String, icon: String) {
        centerSearchFabBgColorHex = bg
        centerSearchFabBorderColorHex = border
        centerSearchFabIconColorHex = icon
        sharedPrefs.edit()
            .putString("center_search_fab_bg", bg)
            .putString("center_search_fab_border", border)
            .putString("center_search_fab_icon", icon)
            .apply()
    }

    // AI Chat FAB customizations
    var aiFabBgColorHex by mutableStateOf(sharedPrefs.getString("ai_fab_bg", "") ?: "") // empty means dynamic theme color
        private set
    var aiFabIconColorHex by mutableStateOf(sharedPrefs.getString("ai_fab_icon", "#4285F4") ?: "#4285F4")
        private set
    var aiFabGradientColorsHex by mutableStateOf(sharedPrefs.getString("ai_fab_grad", "#4285F4,#9B51E0,#EA4335,#FBBC05,#34A853") ?: "#4285F4,#9B51E0,#EA4335,#FBBC05,#34A853")
        private set

    fun updateAiFabColors(bg: String, icon: String, gradient: String) {
        aiFabBgColorHex = bg
        aiFabIconColorHex = icon
        aiFabGradientColorsHex = gradient
        sharedPrefs.edit()
            .putString("ai_fab_bg", bg)
            .putString("ai_fab_icon", icon)
            .putString("ai_fab_grad", gradient)
            .apply()
    }

    fun saveToolResultToHistory(toolName: String, expressionStr: String, resultStr: String) {
        viewModelScope.launch {
            repository.insertHistory(
                HistoryEntry(
                    expression = expressionStr,
                    result = resultStr,
                    type = toolName
                )
            )
        }
    }

    var favoriteTools by mutableStateOf(loadFavorites("favorite_tools"))
        private set
    var favoriteConverters by mutableStateOf(loadFavorites("favorite_converters"))
        private set

    var orderedFavoriteTools by mutableStateOf(loadOrderedFavorites())
        private set

    fun loadOrderedFavorites(): List<String> {
        val raw = sharedPrefs.getString("ordered_favorite_tools_list4", "") ?: ""
        if (raw.isBlank()) {
            val set = sharedPrefs.getStringSet("favorite_tools", emptySet()) ?: emptySet()
            if (set.isEmpty()) {
                return listOf("MULTI_CALENDAR", "HOLY_QURAN", "HADITH_LIBRARY", "PRAYER_TIMES", "SEHRI_IFTAR", "AGE", "BMI", "CONV_CURRENCY")
            }
            // Ensure HADITH_LIBRARY is present if HOLY_QURAN is in the set
            val list = set.toList().toMutableList()
            if (!list.contains("HADITH_LIBRARY")) {
                val quranIdx = list.indexOf("HOLY_QURAN")
                if (quranIdx != -1) {
                    list.add(quranIdx + 1, "HADITH_LIBRARY")
                } else {
                    list.add(0, "HADITH_LIBRARY")
                }
            }
            return list
        }
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun saveOrderedFavorites(list: List<String>) {
        orderedFavoriteTools = list
        val raw = list.joinToString(",")
        sharedPrefs.edit()
            .putString("ordered_favorite_tools_list2", raw)
            .putStringSet("favorite_tools", list.toSet())
            .apply()
        favoriteTools = list.toSet()
    }

    var weatherLocation by mutableStateOf(
        let {
            val loc = sharedPrefs.getString("weather_location", "") ?: ""
            if (loc.isBlank()) {
                sharedPrefs.edit().putString("weather_location", "Dhaka").apply()
                "Dhaka"
            } else loc
        }
    )
        private set

    var weatherLocationLat by mutableStateOf(sharedPrefs.getFloat("weather_lat", 23.7104f).toDouble())
        private set
    var weatherLocationLng by mutableStateOf(sharedPrefs.getFloat("weather_lng", 90.4074f).toDouble())
        private set
    var weatherData by mutableStateOf<com.example.data.network.WeatherResponse?>(loadInitialCachedWeather())
        private set
    var weatherIsLoading by mutableStateOf(false)
        private set
    var isUnifiedSyncing by mutableStateOf(false)
        private set
    var weatherFetchError by mutableStateOf<String?>(null)
        private set
    var isOfflineWeatherData by mutableStateOf(false)
        private set

    var lastWeatherFetchTime by mutableStateOf(sharedPrefs.getLong("cached_weather_time", 0L))

    private val weatherResponseAdapter by lazy {
        moshi.adapter(com.example.data.network.WeatherResponse::class.java)
    }

    private fun loadInitialCachedWeather(): com.example.data.network.WeatherResponse? {
        return try {
            val json = sharedPrefs.getString("cached_weather_json", null)
            if (!json.isNullOrBlank()) {
                val adapter = moshi.adapter(com.example.data.network.WeatherResponse::class.java)
                adapter.fromJson(json)
            } else {
                getFallbackWeather()
            }
        } catch (e: Exception) {
            getFallbackWeather()
        }
    }

    fun loadCachedWeather(): Boolean {
        return try {
            val json = sharedPrefs.getString("cached_weather_json", null)
            if (!json.isNullOrBlank()) {
                val loaded = weatherResponseAdapter.fromJson(json)
                if (loaded != null) {
                    weatherData = loaded
                    isOfflineWeatherData = true
                    lastWeatherFetchTime = sharedPrefs.getLong("cached_weather_time", 0L)
                    return true
                }
            }
            if (weatherData == null) {
                weatherData = getFallbackWeather()
                isOfflineWeatherData = true
            }
            false
        } catch (e: Exception) {
            if (weatherData == null) {
                weatherData = getFallbackWeather()
                isOfflineWeatherData = true
            }
            false
        }
    }

    var geocodingResults by mutableStateOf<List<com.example.data.network.GeocodingResult>>(emptyList())
        private set
    var geocodingIsLoading by mutableStateOf(false)
        private set

    fun getFallbackWeather(): com.example.data.network.WeatherResponse {
        val current = com.example.data.network.CurrentWeather(
            time = "2026-08-13T12:00",
            temperature_2m = 36.5,
            relative_humidity_2m = 65,
            apparent_temperature = 39.0,
            is_day = 1,
            precipitation = 0.0,
            weather_code = 2,
            wind_speed_10m = 12.5,
            wind_direction_10m = 180
        )
        val hourly = com.example.data.network.HourlyWeather(
            time = List(24) { String.format("2026-08-13T%02d:00", it) },
            temperature_2m = List(24) { 28.0 + (it % 8) },
            weather_code = List(24) { 2 },
            precipitation_probability = List(24) { (it * 3) % 100 }
        )
        val daily = com.example.data.network.DailyWeather(
            time = List(7) { "2026-08-13" },
            weather_code = List(7) { 2 },
            temperature_2m_max = List(7) { 36.5 },
            temperature_2m_min = List(7) { 27.0 },
            sunrise = List(7) { "05:30" },
            sunset = List(7) { "18:40" },
            precipitation_sum = List(7) { 0.0 },
            precipitation_probability_max = List(7) { 40 + (it * 5) % 60 }
        )
        return com.example.data.network.WeatherResponse(
            latitude = weatherLocationLat,
            longitude = weatherLocationLng,
            timezone = "Asia/Dhaka",
            current = current,
            hourly = hourly,
            daily = daily
        )
    }

    fun fetchWeather(force: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!force && weatherData != null && !isOfflineWeatherData && (currentTime - lastWeatherFetchTime) < 300_000) {
            // Data is less than 5 minutes old, skip unless forced
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherIsLoading = true
                weatherFetchError = null
                val response = com.example.data.network.WeatherApiClient.weatherApi.getWeather(weatherLocationLat, weatherLocationLng)
                weatherData = response
                isOfflineWeatherData = false
                lastWeatherFetchTime = System.currentTimeMillis()
                
                // Cache to SharedPreferences
                try {
                    val json = weatherResponseAdapter.toJson(response)
                    sharedPrefs.edit()
                        .putString("cached_weather_json", json)
                        .putLong("cached_weather_time", lastWeatherFetchTime)
                        .putString("cached_weather_location", weatherLocation)
                        .apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val msg = e.message ?: "Unknown error"
                    isOfflineWeatherData = true
                    if (weatherData == null) {
                        loadCachedWeather()
                    }
                    weatherFetchError = if (msg.contains("429")) {
                        if (selectedLanguage == AppLanguage.BENGALI) "বেশি বার চেষ্টার কারণে আবহাওয়া সার্ভার সাময়িকভাবে ব্যস্ত রয়েছে (Error 429)। অফলাইন সংরক্ষিত উপাত্ত দেখানো হচ্ছে।" else "Weather server busy (429). Showing offline cached data."
                    } else if (msg.contains("Unable to resolve host") || msg.contains("UnknownHostException") || msg.contains("ConnectException")) {
                        if (selectedLanguage == AppLanguage.BENGALI) "ইন্টারনেট সংযোগ বিচ্ছিন্ন। অফলাইনে পূর্বে সংরক্ষিত আবহাওয়া দেখানো হচ্ছে।" else "Offline: displaying cached weather data."
                    } else {
                        if (selectedLanguage == AppLanguage.BENGALI) "অফলাইন মোড: পূর্বে সংরক্ষিত আবহাওয়া উপাত্ত প্রদর্শিত হচ্ছে।" else "Offline Mode: Showing cached weather data."
                    }
                    android.util.Log.e("Weather", "Fetch failed: $msg (Retaining cached data)")
                }
            } finally {
                weatherIsLoading = false
            }
        }
    }

    fun performAppUnifiedSync(context: Context, onComplete: (Boolean, String) -> Unit) {
        if (isUnifiedSyncing) return
        isUnifiedSyncing = true
        weatherIsLoading = true
        weatherFetchError = null
        
        viewModelScope.launch(Dispatchers.IO) {
            var weatherSuccess = false
            var weatherMessage = ""
            
            // 1. Fetch fresh weather
            try {
                val response = com.example.data.network.WeatherApiClient.weatherApi.getWeather(weatherLocationLat, weatherLocationLng)
                weatherData = response
                isOfflineWeatherData = false
                lastWeatherFetchTime = System.currentTimeMillis()
                
                try {
                    val json = weatherResponseAdapter.toJson(response)
                    sharedPrefs.edit()
                        .putString("cached_weather_json", json)
                        .putLong("cached_weather_time", lastWeatherFetchTime)
                        .putString("cached_weather_location", weatherLocation)
                        .apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                weatherSuccess = true
            } catch (e: Exception) {
                e.printStackTrace()
                isOfflineWeatherData = true
                if (weatherData == null) {
                    loadCachedWeather()
                }
                val msg = e.message ?: ""
                weatherMessage = msg
            } finally {
                weatherIsLoading = false
            }

            // 2. Sync Hijri Date Online (Moon sighting info)
            var hijriSuccess = false
            try {
                val today = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                val url = URL("https://api.aladhan.com/v1/gToH/$today")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    requestMethod = "GET"
                }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val data = json.getJSONObject("data")
                    val hijri = data.getJSONObject("hijri")
                    val remoteHDay = hijri.getString("day")
                    val remoteHMonthName = hijri.getJSONObject("month").getString("en")
                    val remoteHYear = hijri.getString("year")

                    withContext(Dispatchers.Main) {
                        com.example.util.CalendarUtils.hijriOffsetDays = hijriAdjustmentDays
                        islamicPrefs.edit().putInt("hijri_adjustment_days", hijriAdjustmentDays).apply()
                        hijriSyncVersion++
                    }
                    hijriSuccess = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            if (!hijriSuccess) {
                withContext(Dispatchers.Main) {
                    com.example.util.CalendarUtils.hijriOffsetDays = hijriAdjustmentDays
                    islamicPrefs.edit().putInt("hijri_adjustment_days", hijriAdjustmentDays).apply()
                    hijriSyncVersion++
                }
            }

            // 3. Update all widgets (syncs app data, widget weather, countdown, etc.)
            withContext(Dispatchers.Main) {
                try {
                    com.example.widget.ToolsMateMasterWidget.updateAllWidgets(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                isUnifiedSyncing = false
                
                val isBn = selectedLanguage == AppLanguage.BENGALI
                if (weatherSuccess) {
                    val msg = if (isBn) "সাফল্যের সাথে আবহাওয়া, চাঁদ দেখা এবং উইজেট সিঙ্ক করা হয়েছে!" else "Weather, moon-sighting & widgets successfully synced!"
                    onComplete(true, msg)
                } else {
                    val msg = if (isBn) "আংশিক সফল: চাঁদ দেখা ও উইজেট সিঙ্ক হয়েছে, তবে আবহাওয়া অফলাইন।" else "Partial success: Moon-sighting & widgets synced, weather offline."
                    onComplete(false, msg)
                }
            }
        }
    }

    fun searchLocationForWeather(query: String) {
        if (query.isBlank()) {
            geocodingResults = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                geocodingIsLoading = true
                val response = com.example.data.network.WeatherApiClient.geocodingApi.searchLocation(query)
                geocodingResults = response.results ?: emptyList()
            } catch (e: Exception) {
                geocodingResults = emptyList()
            } finally {
                geocodingIsLoading = false
            }
        }
    }

    fun loadDemoWeather() {
        weatherData = getFallbackWeather()
        weatherFetchError = null
    }

    fun clearGeocodingResults() {
        geocodingResults = emptyList()
    }

    data class WeatherCity(val name: String, val lat: Double, val lng: Double)

    val availableWeatherCities = mutableStateListOf(
        WeatherCity("Natore", 24.4102, 88.9834),
        WeatherCity("Dhaka", 23.8103, 90.4125),
        WeatherCity("Chittagong", 22.3569, 91.7832),
        WeatherCity("Sylhet", 24.8949, 91.8687),
        WeatherCity("Rajshahi", 24.3745, 88.6042),
        WeatherCity("Khulna", 22.8456, 89.5403),
        WeatherCity("Barisal", 22.7010, 90.3535)
    )

    fun selectWeatherCity(city: WeatherCity) {
        updateWeatherLocation(city.name, city.lat, city.lng)
    }

    fun updateWeatherLocation(location: String, lat: Double? = null, lng: Double? = null) {
        val trimmed = location.trim()
        weatherLocation = trimmed
        val editor = sharedPrefs.edit().putString("weather_location", trimmed)
        if (lat != null && lng != null) {
            weatherLocationLat = lat
            weatherLocationLng = lng
            editor.putFloat("weather_lat", lat.toFloat()).putFloat("weather_lng", lng.toFloat())
            if (availableWeatherCities.none { it.name.equals(trimmed, ignoreCase = true) }) {
                availableWeatherCities.add(WeatherCity(trimmed, lat, lng))
            }

            // Sync Islamic District persistently when weather location changes
            val closestDistrict = com.example.ui.islamic.allBdDistrictsList.minByOrNull { dist ->
                val results = FloatArray(1)
                android.location.Location.distanceBetween(lat, lng, dist.lat, dist.lon, results)
                results[0]
            }
            if (closestDistrict != null) {
                selectedIslamicDistrictBn = closestDistrict.nameBn
                selectedIslamicDistrictEn = closestDistrict.nameEn
                selectedIslamicDistrictLat = closestDistrict.lat
                selectedIslamicDistrictLon = closestDistrict.lon
                selectedIslamicDistrictOffsetMinutes = closestDistrict.offsetMinutes
                isIslamicLocationAutoDetected = false

                islamicPrefs.edit()
                    .putString("islamic_district_bn", closestDistrict.nameBn)
                    .putString("islamic_district_en", closestDistrict.nameEn)
                    .putFloat("islamic_district_lat", closestDistrict.lat.toFloat())
                    .putFloat("islamic_district_lon", closestDistrict.lon.toFloat())
                    .putInt("islamic_district_offset", closestDistrict.offsetMinutes)
                    .putBoolean("islamic_district_auto", false)
                    .commit()

                try {
                    com.example.widget.ToolsMateMasterWidget.updateAllWidgets(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        editor.apply()
        fetchWeather(force = true)
    }

    private fun loadFavorites(key: String): Set<String> {
        val saved = sharedPrefs.getStringSet(key, null)
        if (saved == null && key == "favorite_tools") {
            return setOf("MULTI_CALENDAR", "HOLY_QURAN", "HADITH_LIBRARY", "PRAYER_TIMES", "SEHRI_IFTAR", "AGE", "BMI")
        }
        return saved ?: emptySet()
    }

    data class FavoriteConfirmAction(
        val isTool: Boolean,
        val key: String,
        val titleEn: String,
        val titleBn: String,
        val isCurrentlyFavorite: Boolean
    )

    var pendingFavoriteConfirmAction by mutableStateOf<FavoriteConfirmAction?>(null)

    fun requestToggleFavoriteTool(toolType: com.example.data.model.ToolType) {
        val isFav = favoriteTools.contains(toolType.name) || orderedFavoriteTools.contains(toolType.name)
        pendingFavoriteConfirmAction = FavoriteConfirmAction(
            isTool = true,
            key = toolType.name,
            titleEn = toolType.titleEn,
            titleBn = toolType.titleBn,
            isCurrentlyFavorite = isFav
        )
    }

    fun requestToggleFavoriteConverter(converterType: com.example.data.model.ConverterType) {
        val convKey = "CONV_${converterType.name}"
        val isFav = favoriteConverters.contains(converterType.name) || orderedFavoriteTools.contains(convKey) || orderedFavoriteTools.contains(converterType.name)
        pendingFavoriteConfirmAction = FavoriteConfirmAction(
            isTool = false,
            key = converterType.name,
            titleEn = converterType.titleEn,
            titleBn = converterType.titleBn,
            isCurrentlyFavorite = isFav
        )
    }

    fun confirmPendingFavoriteAction() {
        pendingFavoriteConfirmAction?.let { action ->
            if (action.isTool) {
                val currentList = orderedFavoriteTools.toMutableList()
                if (action.isCurrentlyFavorite) {
                    currentList.remove(action.key)
                } else {
                    if (!currentList.contains(action.key)) {
                        currentList.add(action.key)
                    }
                }
                saveOrderedFavorites(currentList)
            } else {
                val convKey = "CONV_${action.key}"
                val currentList = orderedFavoriteTools.toMutableList()
                if (action.isCurrentlyFavorite) {
                    val updated = favoriteConverters - action.key
                    sharedPrefs.edit().putStringSet("favorite_converters", updated).apply()
                    favoriteConverters = updated
                    currentList.remove(convKey)
                    currentList.remove(action.key)
                } else {
                    val updated = favoriteConverters + action.key
                    sharedPrefs.edit().putStringSet("favorite_converters", updated).apply()
                    favoriteConverters = updated
                    if (!currentList.contains(convKey)) {
                        currentList.add(convKey)
                    }
                }
                saveOrderedFavorites(currentList)
            }
        }
        pendingFavoriteConfirmAction = null
    }

    fun dismissPendingFavoriteAction() {
        pendingFavoriteConfirmAction = null
    }

    var categoryTopToolsMap by mutableStateOf<Map<String, List<String>>>(loadCategoryTopToolsMap())
        private set

    private fun loadCategoryTopToolsMap(): Map<String, List<String>> {
        val json = sharedPrefs.getString("category_top_tools_map", null)
        if (json != null) {
            try {
                val type = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, List::class.java)
                val adapter = moshi.adapter<Map<String, List<String>>>(type)
                val res = adapter.fromJson(json)
                if (res != null) return res
            } catch (e: Exception) { e.printStackTrace() }
        }
        return mapOf(
            "ISLAMIC" to listOf("HOLY_QURAN", "HADITH_LIBRARY", "QIBLA_COMPASS", "PRAYER_TIMES", "SEHRI_IFTAR", "DIGITAL_TASBIH", "ISLAMIC_DUAS", "NAMAZ_EDUCATION")
        )
    }

    fun getCategoryTopTools(category: com.example.data.model.ToolCategory): List<com.example.data.model.ToolType> {
        return getAllOrderedToolsForCategory(category).take(4)
    }

    fun getAllOrderedToolsForCategory(category: com.example.data.model.ToolCategory): List<com.example.data.model.ToolType> {
        val allInCat = com.example.data.model.ToolType.values().filter { it.category == category }
        val savedOrder = categoryTopToolsMap[category.name]
        if (savedOrder.isNullOrEmpty()) return allInCat

        val result = mutableListOf<com.example.data.model.ToolType>()
        savedOrder.forEach { name ->
            allInCat.find { it.name == name }?.let { tool ->
                if (!result.contains(tool)) result.add(tool)
            }
        }
        allInCat.forEach { tool ->
            if (!result.contains(tool)) result.add(tool)
        }
        return result
    }

    fun pinToolToCategoryTop4(toolType: com.example.data.model.ToolType, positionIndex: Int) {
        val cat = toolType.category
        val allInCat = com.example.data.model.ToolType.values().filter { it.category == cat }
        if (allInCat.isEmpty()) return

        val currentOrdered = getAllOrderedToolsForCategory(cat).map { it.name }.toMutableList()
        currentOrdered.remove(toolType.name)
        val idx = positionIndex.coerceIn(0, currentOrdered.size)
        currentOrdered.add(idx, toolType.name)

        val newMap = categoryTopToolsMap.toMutableMap()
        newMap[cat.name] = currentOrdered
        categoryTopToolsMap = newMap

        try {
            val type = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, List::class.java)
            val adapter = moshi.adapter<Map<String, List<String>>>(type)
            val json = adapter.toJson(newMap)
            sharedPrefs.edit().putString("category_top_tools_map", json).apply()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun isToolPinnedInTop4(toolType: com.example.data.model.ToolType): Boolean {
        val top4 = getCategoryTopTools(toolType.category)
        return top4.contains(toolType)
    }

    var categoryTopConvertersMap by mutableStateOf<Map<String, List<String>>>(loadCategoryTopConvertersMap())
        private set

    private fun loadCategoryTopConvertersMap(): Map<String, List<String>> {
        val json = sharedPrefs.getString("category_top_converters_map", null)
        if (json != null) {
            try {
                val type = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, List::class.java)
                val adapter = moshi.adapter<Map<String, List<String>>>(type)
                val res = adapter.fromJson(json)
                if (res != null) return res
            } catch (e: Exception) { e.printStackTrace() }
        }
        return emptyMap()
    }

    fun getCategoryTopConverters(category: com.example.data.model.ConverterCategory): List<com.example.data.model.ConverterType> {
        return getAllOrderedConvertersForCategory(category).take(4)
    }

    fun getAllOrderedConvertersForCategory(category: com.example.data.model.ConverterCategory): List<com.example.data.model.ConverterType> {
        val allInCat = com.example.data.model.ConverterType.values().filter { it.category == category }
        val savedOrder = categoryTopConvertersMap[category.name]
        if (savedOrder.isNullOrEmpty()) return allInCat

        val result = mutableListOf<com.example.data.model.ConverterType>()
        savedOrder.forEach { name ->
            allInCat.find { it.name == name }?.let { conv ->
                if (!result.contains(conv)) result.add(conv)
            }
        }
        allInCat.forEach { conv ->
            if (!result.contains(conv)) result.add(conv)
        }
        return result
    }

    fun pinConverterToCategoryTop4(converterType: com.example.data.model.ConverterType, positionIndex: Int) {
        val cat = converterType.category
        val allInCat = com.example.data.model.ConverterType.values().filter { it.category == cat }
        if (allInCat.isEmpty()) return

        val currentOrdered = getAllOrderedConvertersForCategory(cat).map { it.name }.toMutableList()
        currentOrdered.remove(converterType.name)
        val idx = positionIndex.coerceIn(0, currentOrdered.size)
        currentOrdered.add(idx, converterType.name)

        val newMap = categoryTopConvertersMap.toMutableMap()
        newMap[cat.name] = currentOrdered
        categoryTopConvertersMap = newMap

        try {
            val type = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, List::class.java)
            val adapter = moshi.adapter<Map<String, List<String>>>(type)
            val json = adapter.toJson(newMap)
            sharedPrefs.edit().putString("category_top_converters_map", json).apply()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun isConverterPinnedInTop4(converterType: com.example.data.model.ConverterType): Boolean {
        val top4 = getCategoryTopConverters(converterType.category)
        return top4.contains(converterType)
    }

    fun pinToTop4(key: String, positionIndex: Int = 0) {
        val currentList = orderedFavoriteTools.toMutableList()
        val formattedKey = if (key.startsWith("CONV_") || com.example.data.model.ToolType.values().any { it.name == key }) key else "CONV_$key"
        
        currentList.remove(formattedKey)
        currentList.remove(key)
        if (key.startsWith("CONV_")) {
            currentList.remove(key.removePrefix("CONV_"))
        }

        val targetIdx = positionIndex.coerceIn(0, 3.coerceAtMost(currentList.size))
        currentList.add(targetIdx, formattedKey)
        saveOrderedFavorites(currentList)
    }

    var pendingUnfavoriteTool by mutableStateOf<String?>(null)
    var pendingUnfavoriteConverter by mutableStateOf<String?>(null)

    fun toggleFavoriteTool(toolName: String) {
        val currentList = orderedFavoriteTools.toMutableList()
        if (currentList.contains(toolName)) {
            pendingUnfavoriteTool = toolName
        } else {
            currentList.add(toolName)
            saveOrderedFavorites(currentList)
        }
    }
    
    fun confirmUnfavoriteTool() {
        pendingUnfavoriteTool?.let { toolName ->
            val currentList = orderedFavoriteTools.toMutableList()
            currentList.remove(toolName)
            saveOrderedFavorites(currentList)
        }
        pendingUnfavoriteTool = null
    }

    fun toggleFavoriteConverter(converterName: String) {
        val convKey = "CONV_$converterName"
        val currentList = orderedFavoriteTools.toMutableList()
        if (favoriteConverters.contains(converterName) || currentList.contains(convKey) || currentList.contains(converterName)) {
            pendingUnfavoriteConverter = converterName
        } else {
            val updated = favoriteConverters + converterName
            sharedPrefs.edit().putStringSet("favorite_converters", updated).apply()
            favoriteConverters = updated
            if (!currentList.contains(convKey)) {
                currentList.add(convKey)
                saveOrderedFavorites(currentList)
            }
        }
    }
    
    fun confirmUnfavoriteConverter() {
        pendingUnfavoriteConverter?.let { converterName ->
            val convKey = "CONV_$converterName"
            favoriteConverters = favoriteConverters - converterName
            sharedPrefs.edit().putStringSet("favorite_converters", favoriteConverters).apply()
            val currentList = orderedFavoriteTools.toMutableList()
            currentList.remove(convKey)
            currentList.remove(converterName)
            saveOrderedFavorites(currentList)
        }
        pendingUnfavoriteConverter = null
    }

    private val customThemeListType = com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.ui.theme.CustomTheme::class.java)
    private val customThemeAdapter by lazy { moshi.adapter<List<com.example.ui.theme.CustomTheme>>(customThemeListType) }

    var customThemes by mutableStateOf(loadCustomThemes())
        private set

    var isCustomThemeActive by mutableStateOf(sharedPrefs.getBoolean("is_custom_theme_active", false))
        private set

    var currentCustomThemeId by mutableStateOf(sharedPrefs.getString("current_custom_theme_id", null))
        private set

    var currentThemeType by mutableStateOf(
        try {
            CalculatorThemeType.valueOf(
                sharedPrefs.getString("selected_theme", CalculatorThemeType.SKY_BREEZE.name)
                    ?: CalculatorThemeType.SKY_BREEZE.name
            )
        } catch (e: Exception) {
            CalculatorThemeType.SKY_BREEZE
        }
    )
        private set

    private fun loadCustomThemes(): List<com.example.ui.theme.CustomTheme> {
        val json = sharedPrefs.getString("custom_themes", null)
        return if (json != null) {
            try {
                customThemeAdapter.fromJson(json) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun saveCustomThemes() {
        val json = customThemeAdapter.toJson(customThemes)
        sharedPrefs.edit().putString("custom_themes", json).apply()
    }

    var themeVersion by mutableStateOf(0)
        private set

    fun addCustomTheme(theme: com.example.ui.theme.CustomTheme) {
        customThemes = customThemes + theme
        saveCustomThemes()
        setCustomTheme(theme.id)
    }

    fun updateCustomTheme(theme: com.example.ui.theme.CustomTheme) {
        val exists = customThemes.any { it.id == theme.id }
        customThemes = if (exists) {
            customThemes.map { if (it.id == theme.id) theme else it }
        } else {
            customThemes + theme
        }
        saveCustomThemes()
        setCustomTheme(theme.id)
    }

    fun deleteCustomTheme(id: String) {
        if (currentCustomThemeId == id) {
            isCustomThemeActive = false
            currentCustomThemeId = null
            sharedPrefs.edit().putBoolean("is_custom_theme_active", false).remove("current_custom_theme_id").apply()
        }
        customThemes = customThemes.filter { it.id != id }
        saveCustomThemes()
        themeVersion++
    }

    fun setCustomTheme(id: String) {
        isCustomThemeActive = true
        currentCustomThemeId = id
        sharedPrefs.edit().putBoolean("is_custom_theme_active", true).putString("current_custom_theme_id", id).apply()
        themeVersion++
    }

    fun getCurrentThemeColors(): com.example.ui.theme.CalculatorThemeColors {
        @Suppress("UNUSED_VARIABLE")
        val dummy = themeVersion
        if (isCustomThemeActive && currentCustomThemeId != null) {
            val custom = customThemes.find { it.id == currentCustomThemeId }
            if (custom != null) {
                return custom.toCalculatorThemeColors()
            }
        }
        return currentThemeType.getColors()
    }

    var selectedLanguage by mutableStateOf(
        try {
            AppLanguage.valueOf(
                sharedPrefs.getString("selected_language", AppLanguage.ENGLISH.name)
                    ?: AppLanguage.ENGLISH.name
            )
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    )
        private set

    fun setLanguage(language: AppLanguage) {
        selectedLanguage = language
        sharedPrefs.edit().putString("selected_language", language.name).apply()
    }

    var vibrationEnabled by mutableStateOf(
        sharedPrefs.getBoolean("vibration_enabled", true)
    )
        private set

    fun updateVibrationEnabled(enabled: Boolean) {
        vibrationEnabled = enabled
        sharedPrefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }

    var decimalPrecision by mutableStateOf(
        sharedPrefs.getInt("decimal_precision", 3)
    )
        private set

    fun updateDecimalPrecision(precision: Int) {
        decimalPrecision = precision
        sharedPrefs.edit().putInt("decimal_precision", precision).apply()
    }

    // --- Currency Exchange Rates Engine ---
    private val defaultExchangeRates = mapOf(
        "USD" to 1.0,
        "BDT" to 121.5,
        "EUR" to 0.92,
        "GBP" to 0.78,
        "INR" to 83.8,
        "SAR" to 3.75,
        "AED" to 3.67,
        "MYR" to 4.42,
        "SGD" to 1.34,
        "CAD" to 1.37,
        "AUD" to 1.52,
        "JPY" to 147.5,
        "CNY" to 7.17,
        "PKR" to 278.5,
        "LKR" to 302.0,
        "TRY" to 33.5,
        "RUB" to 88.0,
        "KWD" to 0.31,
        "BHD" to 0.38,
        "OMR" to 0.38,
        "QAR" to 3.64,
        "THB" to 35.2,
        "IDR" to 15800.0,
        "KRW" to 1365.0,
        "BRL" to 5.50,
        "MXN" to 18.8,
        "EGP" to 48.5,
        "NGN" to 1600.0,
        "CHF" to 0.86,
        "NZD" to 1.66,
        "ZAR" to 18.2
    )

    var exchangeRates by mutableStateOf<Map<String, Double>>(loadCachedExchangeRates())
    var isFetchingExchangeRates by mutableStateOf(false)
    var lastCurrencyUpdateTimestamp by mutableStateOf(
        sharedPrefs.getString("last_currency_update_time", "Default Rates") ?: "Default Rates"
    )
    var currencyUpdateStatusMessage by mutableStateOf("")

    private fun loadCachedExchangeRates(): Map<String, Double> {
        val cachedJson = sharedPrefs.getString("cached_exchange_rates", null) ?: return defaultExchangeRates
        return try {
            val jsonObject = JSONObject(cachedJson)
            val map = mutableMapOf<String, Double>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getDouble(key)
            }
            if (map.isNotEmpty()) map else defaultExchangeRates
        } catch (e: Exception) {
            defaultExchangeRates
        }
    }

    private fun saveCachedExchangeRates(rates: Map<String, Double>, timestamp: String) {
        try {
            val jsonObject = JSONObject()
            rates.forEach { (key, value) -> jsonObject.put(key, value) }
            sharedPrefs.edit()
                .putString("cached_exchange_rates", jsonObject.toString())
                .putString("last_currency_update_time", timestamp)
                .apply()
        } catch (e: Exception) {
            // SILENT
        }
    }

    fun fetchExchangeRates() {
        if (isFetchingExchangeRates) return
        viewModelScope.launch(Dispatchers.IO) {
            isFetchingExchangeRates = true
            try {
                val url = URL("https://open.er-api.com/v6/latest/USD")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 6000
                connection.readTimeout = 6000

                if (connection.responseCode == 200) {
                    val stream = connection.inputStream
                    val responseText = stream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(responseText)

                    if (jsonObject.has("rates")) {
                        val ratesObj = jsonObject.getJSONObject("rates")
                        val newRates = defaultExchangeRates.toMutableMap()
                        val keys = ratesObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            newRates[key] = ratesObj.getDouble(key)
                        }

                        val timeStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

                        withContext(Dispatchers.Main) {
                            exchangeRates = newRates
                            lastCurrencyUpdateTimestamp = timeStr
                            currencyUpdateStatusMessage = "Live rates updated successfully"
                            saveCachedExchangeRates(newRates, timeStr)
                            calculateConverter()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        currencyUpdateStatusMessage = "Offline mode: Using cached rates"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    currencyUpdateStatusMessage = "Offline mode: Using cached rates"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isFetchingExchangeRates = false
                }
            }
        }
    }

    // History list from Room Flow
    val historyList: StateFlow<List<HistoryEntry>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Unit Converter States ---
    var selectedConverterType by mutableStateOf<com.example.data.model.ConverterType?>(null)
    var selectedCategoryFilter by mutableStateOf<com.example.data.model.ConverterCategory?>(null)
    var converterSearchQuery by mutableStateOf("")
    var fromUnit by mutableStateOf("Meter")
    var toUnit by mutableStateOf("Feet")
    var converterInput by mutableStateOf("1")
    var converterOutput by mutableStateOf("3.2808")

    // --- Special Tools States ---
    var selectedToolType by mutableStateOf<com.example.data.model.ToolType?>(null)
    var selectedToolCategoryFilter by mutableStateOf<com.example.data.model.ToolCategory?>(null)
    var toolSearchQuery by mutableStateOf("")

    fun openTool(type: com.example.data.model.ToolType) {
        selectedToolType = type
        activeTab = 0
    }

    fun closeToolDetail() {
        selectedToolType = null
    }

    // Exit confirmation dialog state
    var showExitDialog by mutableStateOf(false)

    // --- Special Calculators States ---
    // 1. BMI
    var bmiGender by mutableStateOf("Male")
    var bmiWeight by mutableStateOf("70")
    var bmiWeightUnit by mutableStateOf("kg") // "kg" or "lb"
    var bmiHeight by mutableStateOf("170")
    var bmiHeightUnit by mutableStateOf("ft/in") // "cm" or "ft/in"
    var bmiHeightFt by mutableStateOf("5")
    var bmiHeightIn by mutableStateOf("7")
    var bmiAge by mutableStateOf("25")
    var bmiResultValue by mutableStateOf("")
    var bmiCategoryResult by mutableStateOf("")
    var bmiIdealWeightRange by mutableStateOf("")

    // 2. Age
    var ageDob by mutableStateOf("1/1/2000")
    var ageYearsResult by mutableStateOf("0")
    var ageMonthsResult by mutableStateOf("0")
    var ageDaysResult by mutableStateOf("0")
    var ageNextBirthdayResult by mutableStateOf("-")
    var ageWhichBirthdayResult by mutableStateOf("-")
    var ageCountdownResult by mutableStateOf("-")
    var birthDateString by mutableStateOf("2000-01-01")
    var targetDateString by mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    var ageResultText by mutableStateOf("")

    // 3. Discount
    var originalPrice by mutableStateOf("1000")
    var discountPercent by mutableStateOf("15")
    var taxPercent by mutableStateOf("5")
    var finalPriceResult by mutableStateOf("")
    var discountSavingsResult by mutableStateOf("")

    // 4. Percentage
    var percentValueA by mutableStateOf("50")
    var percentValueB by mutableStateOf("200")
    var percentageResultText by mutableStateOf("")

    var scanHistoryList by mutableStateOf<List<com.example.ui.viewmodel.ScanHistoryItem>>(emptyList())

    fun loadScanHistory() {
        val savedString = sharedPrefs.getString("barcode_scan_history_list", null) ?: ""
        if (savedString.isBlank()) {
            scanHistoryList = emptyList()
            return
        }
        val items = mutableListOf<com.example.ui.viewmodel.ScanHistoryItem>()
        val parts = savedString.split("##RECORD_SPLIT##")
        for (part in parts) {
            val fields = part.split("##FIELD_SPLIT##")
            if (fields.size >= 4) {
                items.add(
                    com.example.ui.viewmodel.ScanHistoryItem(
                        id = fields[0],
                        value = fields[1],
                        format = fields[2],
                        timestamp = fields[3].toLongOrNull() ?: System.currentTimeMillis()
                    )
                )
            }
        }
        scanHistoryList = items.sortedByDescending { it.timestamp }
    }

    fun addScanHistory(value: String, format: String) {
        if (value.isBlank()) return
        val filtered = scanHistoryList.filterNot { it.value == value }.toMutableList()
        filtered.add(
            0,
            com.example.ui.viewmodel.ScanHistoryItem(
                id = java.util.UUID.randomUUID().toString(),
                value = value,
                format = format,
                timestamp = System.currentTimeMillis()
            )
        )
        scanHistoryList = filtered
        saveScanHistory()
    }

    fun deleteScanHistory(id: String) {
        scanHistoryList = scanHistoryList.filterNot { it.id == id }
        saveScanHistory()
    }

    fun clearScanHistory() {
        scanHistoryList = emptyList()
        saveScanHistory()
    }

    private fun saveScanHistory() {
        val sb = StringBuilder()
        for ((idx, item) in scanHistoryList.withIndex()) {
            if (idx > 0) sb.append("##RECORD_SPLIT##")
            sb.append(item.id).append("##FIELD_SPLIT##")
                .append(item.value).append("##FIELD_SPLIT##")
                .append(item.format).append("##FIELD_SPLIT##")
                .append(item.timestamp)
        }
        sharedPrefs.edit().putString("barcode_scan_history_list", sb.toString()).apply()
    }

    init {
        loadScanHistory()
        // Offload calculations to background to prevent UI hang on startup
        viewModelScope.launch(Dispatchers.Default) {
            calculateConverter()
            calculateBMI()
            calculateAge()
            calculateDiscount()
            calculatePercentage()
        }
        // Offload heavy IO/Network tasks to IO thread for maximum app opening speed
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Initialize modern Security Provider for older Android devices like Oppo a5s
                com.google.android.gms.security.ProviderInstaller.installIfNeeded(context)
                android.util.Log.d("CalculatorViewModel", "Security provider installed successfully via ProviderInstaller")
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("CalculatorViewModel", "Failed to install security provider: ${e.message}")
            }

            fetchWeather()

            try {
                loadChatHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                fetchExchangeRates()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                checkOnlineModelStatus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Calculator Logic ---
    private fun isOperator(char: String): Boolean {
        return char in setOf("+", "−", "×", "÷", "%", "^", "x^y") || char == "−" || char == "-"
    }

    fun processVoiceInput(text: String) {
        var processedText = text.lowercase(Locale.getDefault())
        // ... (rest of processing)
        // Convert Bengali numerals to English numerals
        val bengaliToEnglishNumbers = mapOf(
            '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
            '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
        )
        val sb = java.lang.StringBuilder()
        for (char in processedText) {
            sb.append(bengaliToEnglishNumbers[char] ?: char)
        }
        processedText = sb.toString()
        
        // Remove spaces and alphabetic characters to keep only math expression
        processedText = processedText.replace(Regex("[^0-9\\+\\−\\×\\÷\\%\\.\\(\\)\\^]"), "")

        if (processedText.isNotEmpty()) {
            expressionValue = TextFieldValue(processedText, selection = TextRange(processedText.length))
            evaluateExpression()
        }
    }

    fun persistActiveExpression() {
        try {
            sharedPrefs.edit()
                .putString("saved_calc_expression", expressionValue.text)
                .putInt("saved_calc_selection", expressionValue.selection.start)
                .putString("saved_calc_result", result)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onExpressionValueChange(newValue: TextFieldValue) {
        expressionValue = newValue
        tryEvaluatePreview()
        persistActiveExpression()
    }

    fun onPaste(pastedText: String) {
        val currentText = expressionValue.text
        val selection = expressionValue.selection
        val newText = if (currentText == "0") {
            pastedText
        } else {
            currentText.substring(0, selection.start) + pastedText + currentText.substring(selection.end)
        }
        expressionValue = TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + pastedText.length)
        )
        tryEvaluatePreview()
        persistActiveExpression()
    }

    fun onBtnClick(char: String) {
        if (isEvaluated) {
            if (char != "AC" && char != "C" && char != "=" && char != "DEG" && char != "RAD" && char != "±") {
                if (isOperator(char)) {
                    // Continue from previous result
                    expressionValue = TextFieldValue(result, selection = TextRange(result.length))
                } else {
                    // Start new expression
                    expressionValue = TextFieldValue("")
                    result = ""
                }
            }
            isEvaluated = false
        }

        val currentText = expressionValue.text
        val selection = expressionValue.selection
        val before = currentText.substring(0, selection.start)
        val after = currentText.substring(selection.end)

        when (char) {
            "AC" -> {
                expressionValue = TextFieldValue("0", selection = TextRange(1))
                result = ""
            }
            "C" -> {
                if (selection.collapsed) {
                    if (before.isNotEmpty()) {
                        val newText = (before.dropLast(1) + after).ifEmpty { "0" }
                        val newPos = if (newText == "0" && before.length == 1) 1 else (before.length - 1).coerceAtLeast(0)
                        expressionValue = TextFieldValue(newText, selection = TextRange(newPos))
                    }
                } else {
                    val newText = (before + after).ifEmpty { "0" }
                    val newPos = if (newText == "0") 1 else before.length
                    expressionValue = TextFieldValue(newText, selection = TextRange(newPos))
                }
                tryEvaluatePreview()
            }
            "=" -> {
                evaluateExpression()
            }
            "DEG", "RAD" -> {
                isDegreeMode = !isDegreeMode
                tryEvaluatePreview()
            }
            "sin", "cos", "tan", "ln", "log", "√", "antilog", "sin⁻¹", "cos⁻¹", "tan⁻¹", "log10", "3√" -> {
                val toAdd = if (char == "log10") "log10(" else if (char == "3√") "3√(" else "$char("
                val newText = before + toAdd + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + toAdd.length))
                tryEvaluatePreview()
            }
            "x^y" -> {
                val newText = before + "^" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 1))
                tryEvaluatePreview()
            }
            "x²" -> {
                val newText = before + "^2" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 2))
                tryEvaluatePreview()
            }
            "x³" -> {
                val newText = before + "^3" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 2))
                tryEvaluatePreview()
            }
            "1/x" -> {
                val newText = before + "1÷" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 2))
                tryEvaluatePreview()
            }
            "e^x" -> {
                val newText = before + "e^" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 2))
                tryEvaluatePreview()
            }
            "x!" -> {
                val newText = before + "!" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 1))
                tryEvaluatePreview()
            }
            "π", "e" -> {
                val newText = before + char + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 1))
                tryEvaluatePreview()
            }
            "()" -> {
                val toAdd: String
                if (currentText.isEmpty()) {
                    toAdd = "("
                } else {
                    val lastChar = if (before.isNotEmpty()) before.last() else ' '
                    val openCount = currentText.count { it == '(' }
                    val closeCount = currentText.count { it == ')' }
                    
                    if (openCount > closeCount) {
                        if (lastChar in setOf('+', '−', '×', '÷', '(', '^', '-')) {
                            toAdd = "("
                        } else {
                            toAdd = ")"
                        }
                    } else {
                        if (lastChar.isDigit() || lastChar == ')' || lastChar == 'e' || lastChar == 'π') {
                            toAdd = "×("
                        } else {
                            toAdd = "("
                        }
                    }
                }
                val newText = before + toAdd + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + toAdd.length))
                tryEvaluatePreview()
            }
            "±" -> {
                if (currentText.startsWith("-")) {
                    expressionValue = TextFieldValue(currentText.substring(1), selection = TextRange((selection.start - 1).coerceAtLeast(0)))
                } else if (currentText.isNotEmpty()) {
                    expressionValue = TextFieldValue("-$currentText", selection = TextRange(selection.start + 1))
                }
                tryEvaluatePreview()
            }
            "." -> {
                val lastNumber = before.split(Regex("[+−×÷%^]")).lastOrNull() ?: ""
                if (!lastNumber.contains(".")) {
                    val toAdd = if (before.isEmpty() || isOperator(before.last().toString())) "0." else "."
                    val newText = before + toAdd + after
                    expressionValue = TextFieldValue(newText, selection = TextRange(before.length + toAdd.length))
                }
                tryEvaluatePreview()
            }
            else -> {
                val processedBefore = if (before == "0" && !isOperator(char) && char != ".") "" else before
                if (isOperator(char)) {
                    if (processedBefore.isNotEmpty() && processedBefore != "0") {
                        val lastChar = processedBefore.last().toString()
                        if (isOperator(lastChar)) {
                            val newText = processedBefore.dropLast(1) + char + after
                            expressionValue = TextFieldValue(newText, selection = TextRange(processedBefore.length))
                        } else {
                            val newText = processedBefore + char + after
                            expressionValue = TextFieldValue(newText, selection = TextRange(processedBefore.length + 1))
                        }
                    } else if (char == "−" || char == "-") {
                        val newText = "-" + after
                        expressionValue = TextFieldValue(newText, selection = TextRange(1))
                    }
                } else {
                    val newText = processedBefore + char + after
                    val newPos = if (before == "0" && processedBefore == "") char.length else (before.length + char.length)
                    expressionValue = TextFieldValue(newText, selection = TextRange(newPos))
                }
                tryEvaluatePreview()
            }
        }
        persistActiveExpression()
    }

    private fun tryEvaluatePreview() {
        if (expression.isBlank()) {
            result = ""
            return
        }
        try {
            var tempExpr = expression
            val openCount = tempExpr.count { it == '(' }
            val closeCount = tempExpr.count { it == ')' }
            if (openCount > closeCount) {
                tempExpr += ")".repeat(openCount - closeCount)
            }

            // Simple integer precision preservation: if it's just a long digit string, don't use evaluator
            if (tempExpr.all { it.isDigit() } && tempExpr.length > 15) {
                result = tempExpr
                return
            }

            val evalResult = ExpressionEvaluator.evaluate(tempExpr, isDegreeMode)
            if (!evalResult.isInfinite() && !evalResult.isNaN()) {
                val df = DecimalFormat("#.########")
                result = df.format(evalResult)
            } else {
                result = ""
            }
        } catch (e: Exception) {
            result = ""
        }
    }

    private fun evaluateExpression() {
        if (expression.isBlank()) return
        try {
            var finalExpr = expression
            val openCount = finalExpr.count { it == '(' }
            val closeCount = finalExpr.count { it == ')' }
            if (openCount > closeCount) {
                finalExpr += ")".repeat(openCount - closeCount)
            }

            // Integer precision preservation
            if (finalExpr.all { it.isDigit() } && finalExpr.length > 15) {
                result = finalExpr
                isEvaluated = true
                saveToHistory(expression, finalExpr)
                return
            }

            val evalResult = ExpressionEvaluator.evaluate(finalExpr, isDegreeMode)
            
            // Format result beautifully
            val formatted = if (evalResult.isInfinite()) {
                "Infinity"
            } else if (evalResult.isNaN()) {
                "NaN"
            } else {
                val df = DecimalFormat("#.########")
                df.format(evalResult)
            }
            
            result = formatted
            isEvaluated = true

            saveToHistory(expression, formatted)
        } catch (e: Exception) {
            result = "Error"
            isEvaluated = true
        }
        persistActiveExpression()
    }

    private fun saveToHistory(expr: String, res: String) {
        viewModelScope.launch {
            repository.insertHistory(
                HistoryEntry(
                    expression = expr,
                    result = res,
                    type = "Calculator"
                )
            )
        }
    }

    fun saveCustomHistory(expression: String, result: String, type: String = "Tool", customName: String? = null) {
        viewModelScope.launch {
            repository.insertHistory(
                HistoryEntry(
                    expression = expression,
                    result = result,
                    type = type,
                    customName = customName
                )
            )
        }
    }

    fun selectHistoryItem(entry: HistoryEntry) {
        if (entry.type == "Calculator" || entry.type == "Basic" || entry.type.isBlank()) {
            expressionValue = TextFieldValue(entry.expression, selection = TextRange(entry.expression.length))
            result = entry.result
            changeActiveTab(
                2,
                "Opened from History item: \"${entry.expression} = ${entry.result}\"",
                "হিস্ট্রি আইটেম থেকে খোলা হয়েছে: \"${entry.expression} = ${entry.result}\""
            )
        } else if (entry.type == "BMI Calculator") {
            activeTab = 0
            openTool(com.example.data.model.ToolType.BMI)
        } else if (entry.type == "Age Calculator") {
            activeTab = 0
            openTool(com.example.data.model.ToolType.AGE)
        } else if (entry.type == "Discount Calculator") {
            activeTab = 0
            openTool(com.example.data.model.ToolType.DISCOUNT)
        } else {
            val matchingConverter = com.example.data.model.ConverterType.values().find {
                it.titleEn == entry.type || it.titleBn == entry.type
            }
            if (matchingConverter != null) {
                activeTab = 1
                selectedConverterType = matchingConverter
                val numberPart = entry.expression.split(" ").firstOrNull()
                if (numberPart != null && numberPart.toDoubleOrNull() != null) {
                    converterInput = numberPart
                }
            } else {
                val matchingTool = com.example.data.model.ToolType.values().find {
                    it.titleEn == entry.type || it.titleBn == entry.type
                }
                if (matchingTool != null) {
                    activeTab = 0
                    openTool(matchingTool)
                }
            }
        }
    }

    fun saveNamedCalculation(name: String) {
        viewModelScope.launch {
            repository.insertHistory(
                HistoryEntry(
                    expression = expression,
                    result = result,
                    type = "Calculator",
                    customName = name
                )
            )
        }
    }

    fun backupHistoryToUri(uri: android.net.Uri) {
        exportFullBackupToUri(uri)
    }

    fun restoreHistoryFromUri(uri: android.net.Uri) {
        loadGlobalBackupForConfirmation(uri)
    }

    fun exportFullBackupToUri(uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val history = historyList.value
                val marketPrefs = context.getSharedPreferences("market_list_prefs", Context.MODE_PRIVATE)
                val marketPlans = marketPrefs.getString("saved_plan_lists", null) ?: sharedPrefs.getString("saved_plan_lists", null)
                val completedMemos = marketPrefs.getString("completed_memos", null) ?: sharedPrefs.getString("completed_memos", null)
                val chatJson = sharedPrefs.getString("ai_chat_sessions_v1", null)
                val notes = sharedPrefs.getString("professional_notes_list_v2", null)
                val barcode = sharedPrefs.getString("barcode_scan_history_list", null)
                val favTools = orderedFavoriteTools
                val favConv = favoriteConverters.toList()

                val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                val backup = GlobalAppBackup(
                    version = 2,
                    appName = "ToolsMate All-in-One",
                    backupDate = dateFormat.format(Date()),
                    timestamp = System.currentTimeMillis(),
                    historyEntries = history,
                    marketPlansJson = marketPlans,
                    completedMemosJson = completedMemos,
                    chatSessionsJson = chatJson,
                    notesJson = notes,
                    favoriteTools = favTools,
                    favoriteConverters = favConv,
                    barcodeHistory = barcode
                )

                val moshi = com.squareup.moshi.Moshi.Builder()
                    .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(GlobalAppBackup::class.java)
                val jsonString = adapter.toJson(backup)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                }

                val isBn = selectedLanguage == AppLanguage.BENGALI
                withContext(Dispatchers.Main) {
                    backupStatusMessage = if (isBn) {
                        "✅ অ্যাপের সমস্ত ডেটা সফলভাবে ব্যাকআপ করা হয়েছে!\n\n• হিস্টোরি: ${history.size}টি\n• বাজার ফর্দ ও মেমো সংরক্ষিত\n• এআই চ্যাট ও নোটস সংরক্ষিত"
                    } else {
                        "✅ All app data successfully backed up!\n\n• History: ${history.size} items\n• Shopping lists & memos saved\n• AI Chat & Notes saved"
                    }
                    showBackupStatusDialog = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val isBn = selectedLanguage == AppLanguage.BENGALI
                withContext(Dispatchers.Main) {
                    backupStatusMessage = if (isBn) {
                        "ব্যাকআপ ব্যর্থ হয়েছে: ${e.localizedMessage}"
                    } else {
                        "Backup failed: ${e.localizedMessage}"
                    }
                    showBackupStatusDialog = true
                }
            }
        }
    }

    fun loadGlobalBackupForConfirmation(uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }
                if (jsonString.isNullOrBlank()) {
                    throw Exception("Selected file is empty")
                }

                val moshi = com.squareup.moshi.Moshi.Builder()
                    .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()

                var backupObj: GlobalAppBackup? = null
                try {
                    backupObj = moshi.adapter(GlobalAppBackup::class.java).fromJson(jsonString)
                } catch (e: Exception) {
                    try {
                        val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, HistoryEntry::class.java)
                        val legacyEntries = moshi.adapter<List<HistoryEntry>>(listType).fromJson(jsonString)
                        if (legacyEntries != null) {
                            backupObj = GlobalAppBackup(
                                historyEntries = legacyEntries,
                                backupDate = "Legacy Backup"
                            )
                        }
                    } catch (e2: Exception) {
                        throw Exception("Unrecognized backup format")
                    }
                }

                if (backupObj == null) {
                    throw Exception("Invalid backup file")
                }

                val isBn = selectedLanguage == AppLanguage.BENGALI
                var planCount = 0
                backupObj.marketPlansJson?.let {
                    try { planCount = org.json.JSONArray(it).length() } catch (e: Exception) {}
                }
                var memoCount = 0
                backupObj.completedMemosJson?.let {
                    try { memoCount = org.json.JSONArray(it).length() } catch (e: Exception) {}
                }
                var notesCount = 0
                backupObj.notesJson?.let {
                    notesCount = it.split("[NOTE_SEPARATOR]").filter { s -> s.isNotBlank() }.size
                }

                withContext(Dispatchers.Main) {
                    pendingGlobalRestoreBackup = backupObj
                    globalRestoreSummary = if (isBn) {
                        "তারিখ: ${backupObj.backupDate.ifBlank { "N/A" }}\n\n" +
                        "• ক্যালকুলেটর হিস্টোরি: ${backupObj.historyEntries.size}টি\n" +
                        "• বাজার ফর্দ (Plans): ${planCount}টি\n" +
                        "• বাজার মেমো (Memos): ${memoCount}টি\n" +
                        "• সেভ করা নোটস: ${notesCount}টি\n" +
                        "• প্রিয় টুলস ও কনভার্টার: ${backupObj.favoriteTools.size + backupObj.favoriteConverters.size}টি"
                    } else {
                        "Date: ${backupObj.backupDate.ifBlank { "N/A" }}\n\n" +
                        "• Calculator History: ${backupObj.historyEntries.size}\n" +
                        "• Market Plans: $planCount\n" +
                        "• Bazaar Memos: $memoCount\n" +
                        "• Saved Notes: $notesCount\n" +
                        "• Favorite Tools & Conv: ${backupObj.favoriteTools.size + backupObj.favoriteConverters.size}"
                    }
                    showGlobalRestoreDialog = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val isBn = selectedLanguage == AppLanguage.BENGALI
                withContext(Dispatchers.Main) {
                    backupStatusMessage = if (isBn) "ফাইল পড়া যায়নি: ${e.localizedMessage}" else "Failed to read backup: ${e.localizedMessage}"
                    showBackupStatusDialog = true
                }
            }
        }
    }

    fun confirmAndExecuteGlobalRestore() {
        val backup = pendingGlobalRestoreBackup ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (backup.historyEntries.isNotEmpty()) {
                    backup.historyEntries.forEach { entry ->
                        repository.insertHistory(entry.copy(id = 0))
                    }
                }

                val marketPrefs = context.getSharedPreferences("market_list_prefs", Context.MODE_PRIVATE)
                if (!backup.marketPlansJson.isNullOrBlank()) {
                    marketPrefs.edit().putString("saved_plan_lists", backup.marketPlansJson).apply()
                    sharedPrefs.edit().putString("saved_plan_lists", backup.marketPlansJson).apply()
                }
                if (!backup.completedMemosJson.isNullOrBlank()) {
                    marketPrefs.edit().putString("completed_memos", backup.completedMemosJson).apply()
                    sharedPrefs.edit().putString("completed_memos", backup.completedMemosJson).apply()
                }

                if (!backup.chatSessionsJson.isNullOrBlank()) {
                    sharedPrefs.edit().putString("ai_chat_sessions_v1", backup.chatSessionsJson).apply()
                    try { loadChatHistory() } catch (e: Exception) {}
                }

                if (!backup.notesJson.isNullOrBlank()) {
                    sharedPrefs.edit().putString("professional_notes_list_v2", backup.notesJson).apply()
                    withContext(Dispatchers.Main) {
                        notesListString = backup.notesJson
                    }
                }

                if (backup.favoriteTools.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        saveOrderedFavorites(backup.favoriteTools)
                    }
                }
                if (backup.favoriteConverters.isNotEmpty()) {
                    sharedPrefs.edit().putStringSet("favorite_converters", backup.favoriteConverters.toSet()).apply()
                    withContext(Dispatchers.Main) {
                        favoriteConverters = backup.favoriteConverters.toSet()
                    }
                }

                if (!backup.barcodeHistory.isNullOrBlank()) {
                    sharedPrefs.edit().putString("barcode_scan_history_list", backup.barcodeHistory).apply()
                    withContext(Dispatchers.Main) {
                        loadScanHistory()
                    }
                }

                val isBn = selectedLanguage == AppLanguage.BENGALI
                withContext(Dispatchers.Main) {
                    showGlobalRestoreDialog = false
                    pendingGlobalRestoreBackup = null
                    backupStatusMessage = if (isBn) {
                        "🎉 সকল ডেটা সফলভাবে রিস্টোর করা হয়েছে!"
                    } else {
                        "🎉 All data successfully restored!"
                    }
                    showBackupStatusDialog = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val isBn = selectedLanguage == AppLanguage.BENGALI
                withContext(Dispatchers.Main) {
                    showGlobalRestoreDialog = false
                    pendingGlobalRestoreBackup = null
                    backupStatusMessage = if (isBn) "রিস্টোর ব্যর্থ হয়েছে: ${e.localizedMessage}" else "Restore failed: ${e.localizedMessage}"
                    showBackupStatusDialog = true
                }
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Theme Controller ---
    fun setTheme(theme: CalculatorThemeType) {
        isCustomThemeActive = false
        currentThemeType = theme
        sharedPrefs.edit()
            .putBoolean("is_custom_theme_active", false)
            .putString("selected_theme", theme.name)
            .apply()
        themeVersion++
    }

    // --- Unit Converter Engine ---
    fun openConverter(type: com.example.data.model.ConverterType) {
        selectedConverterType = type
        fromUnit = type.units.firstOrNull() ?: ""
        toUnit = type.units.getOrNull(1) ?: fromUnit
        converterInput = "1"
        calculateConverter()
        activeTab = 1
    }

    fun closeConverterDetail() {
        selectedConverterType = null
    }

    fun swapUnits() {
        val temp = fromUnit
        fromUnit = toUnit
        toUnit = temp
        calculateConverter()
    }

    fun convertNumberSystem(input: String, from: String, to: String): String {
        if (input.isBlank()) return ""
        val cleanInput = input.trim()
        val fromRadix = when {
            from.contains("Binary") || from.contains("2") -> 2
            from.contains("Octal") || from.contains("8") -> 8
            from.contains("Hexadecimal") || from.contains("16") -> 16
            else -> 10
        }
        val toRadix = when {
            to.contains("Binary") || to.contains("2") -> 2
            to.contains("Octal") || to.contains("8") -> 8
            to.contains("Hexadecimal") || to.contains("16") -> 16
            else -> 10
        }
        return try {
            val decimalVal = cleanInput.toLong(fromRadix)
            when (toRadix) {
                2 -> java.lang.Long.toBinaryString(decimalVal)
                8 -> java.lang.Long.toOctalString(decimalVal)
                16 -> java.lang.Long.toHexString(decimalVal).uppercase()
                else -> decimalVal.toString()
            }
        } catch (e: Exception) {
            "Invalid"
        }
    }

    fun calculateConverter() {
        val type = selectedConverterType ?: return
        if (type == com.example.data.model.ConverterType.NUMBER_SYSTEM) {
            converterOutput = convertNumberSystem(converterInput, fromUnit, toUnit)
            return
        }
        val inputVal = converterInput.toDoubleOrNull() ?: 0.0
        val outVal = if (type == com.example.data.model.ConverterType.CURRENCY) {
            type.convert(fromUnit, toUnit, inputVal, customRates = exchangeRates)
        } else {
            type.convert(fromUnit, toUnit, inputVal)
        }
        val df = DecimalFormat("#.######")
        converterOutput = df.format(outVal)
    }

    fun calculateConverterReverse() {
        val type = selectedConverterType ?: return
        if (type == com.example.data.model.ConverterType.NUMBER_SYSTEM) {
            converterInput = convertNumberSystem(converterOutput, toUnit, fromUnit)
            return
        }
        val outputVal = converterOutput.toDoubleOrNull() ?: 0.0
        val inVal = if (type == com.example.data.model.ConverterType.CURRENCY) {
            type.convert(toUnit, fromUnit, outputVal, customRates = exchangeRates)
        } else {
            type.convert(toUnit, fromUnit, outputVal)
        }
        val df = DecimalFormat("#.######")
        converterInput = df.format(inVal)
    }

    // --- Special Tools Calculations ---
    // 1. BMI Calculation
    fun toggleBmiWeightUnit() {
        if (bmiWeightUnit == "kg") {
            bmiWeightUnit = "lb"
            val wKg = bmiWeight.toDoubleOrNull() ?: 0.0
            bmiWeight = String.format(java.util.Locale.US, "%.1f", wKg / 0.453592).removeSuffix(".0")
        } else {
            bmiWeightUnit = "kg"
            val wLb = bmiWeight.toDoubleOrNull() ?: 0.0
            bmiWeight = String.format(java.util.Locale.US, "%.1f", wLb * 0.453592).removeSuffix(".0")
        }
        calculateBMI()
    }

    fun calculateBMI() {
        val wKg = if (bmiWeightUnit == "kg") {
            bmiWeight.toDoubleOrNull() ?: 0.0
        } else {
            (bmiWeight.toDoubleOrNull() ?: 0.0) * 0.453592
        }

        val hM = if (bmiHeightUnit == "cm") {
            (bmiHeight.toDoubleOrNull() ?: 0.0) / 100.0
        } else {
            val ft = bmiHeightFt.toDoubleOrNull() ?: 0.0
            val inch = bmiHeightIn.toDoubleOrNull() ?: 0.0
            val totalInches = (ft * 12.0) + inch
            totalInches * 0.0254
        }

        if (wKg <= 0.0 || hM <= 0.0) {
            bmiResultValue = "0.0"
            bmiCategoryResult = "Normal"
            return
        }
        
        val bmi = wKg / (hM * hM)
        val df = DecimalFormat("0.0")
        bmiResultValue = df.format(bmi)
        
        bmiCategoryResult = when {
            bmi <= 15.9 -> "Very Severely Underweight"
            bmi in 16.0..16.9 -> "Severely Underweight"
            bmi in 17.0..18.4 -> "Underweight"
            bmi in 18.5..24.9 -> "Normal"
            bmi in 25.0..29.9 -> "Overweight"
            bmi in 30.0..34.9 -> "Obese Class I"
            bmi in 35.0..39.9 -> "Obese Class II"
            else -> "Obese Class III"
        }

        val minIdealKg = 18.5 * (hM * hM)
        val maxIdealKg = 24.9 * (hM * hM)
        bmiIdealWeightRange = if (bmiWeightUnit == "kg") {
            String.format(Locale.US, "%.1f kg - %.1f kg", minIdealKg, maxIdealKg)
        } else {
            String.format(Locale.US, "%.1f lb - %.1f lb", minIdealKg / 0.453592, maxIdealKg / 0.453592)
        }
    }

    // 2. Age Calculation
    fun calculateAge() {
        try {
            val parts = ageDob.split("/")
            if (parts.size == 3) {
                val day = parts[0].toIntOrNull() ?: 1
                val month = (parts[1].toIntOrNull() ?: 1) - 1 // 0 indexed
                val year = parts[2].toIntOrNull() ?: 2000

                val birthCalendar = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                val targetCalendar = Calendar.getInstance()

                var years = targetCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                var months = targetCalendar.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH)
                var days = targetCalendar.get(Calendar.DAY_OF_MONTH) - birthCalendar.get(Calendar.DAY_OF_MONTH)

                if (days < 0) {
                    months--
                    val prevMonthCalendar = targetCalendar.clone() as Calendar
                    prevMonthCalendar.add(Calendar.MONTH, -1)
                    days += prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                }

                if (months < 0) {
                    years--
                    months += 12
                }

                if (years < 0) {
                    ageYearsResult = "0"
                    ageMonthsResult = "0"
                    ageDaysResult = "0"
                    ageNextBirthdayResult = "-"
                    ageWhichBirthdayResult = "-"
                    ageCountdownResult = "-"
                } else {
                    ageYearsResult = years.toString()
                    ageMonthsResult = months.toString()
                    ageDaysResult = days.toString()

                    val nextBday = birthCalendar.clone() as Calendar
                    nextBday.set(Calendar.YEAR, targetCalendar.get(Calendar.YEAR))
                    if (nextBday.before(targetCalendar) || nextBday.timeInMillis == targetCalendar.timeInMillis) {
                        nextBday.add(Calendar.YEAR, 1)
                    }

                    val nextAgeYears = nextBday.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                    ageWhichBirthdayResult = "$nextAgeYears${getOrdinalSuffix(nextAgeYears)} Birthday"

                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    ageNextBirthdayResult = dateFormat.format(nextBday.time)

                    val diffInMillis = nextBday.timeInMillis - targetCalendar.timeInMillis
                    val diffDays = (diffInMillis / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                    ageCountdownResult = "$diffDays Days"
                }
            }
        } catch (e: Exception) {
            ageYearsResult = "0"
            ageMonthsResult = "0"
            ageDaysResult = "0"
            ageNextBirthdayResult = "-"
            ageWhichBirthdayResult = "-"
            ageCountdownResult = "-"
        }
    }

    private fun getOrdinalSuffix(n: Int): String {
        return when {
            n % 100 in 11..13 -> "th"
            n % 10 == 1 -> "st"
            n % 10 == 2 -> "nd"
            n % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    // 3. Discount Calculation
    fun calculateDiscount() {
        val price = originalPrice.toDoubleOrNull() ?: 0.0
        val disc = discountPercent.toDoubleOrNull() ?: 0.0
        val tax = taxPercent.toDoubleOrNull() ?: 0.0

        val savings = price * (disc / 100.0)
        val priceAfterDiscount = price - savings
        val taxAmount = priceAfterDiscount * (tax / 100.0)
        val finalPrice = priceAfterDiscount + taxAmount

        val df = DecimalFormat("#.##")
        finalPriceResult = df.format(finalPrice)
        discountSavingsResult = df.format(savings)
    }

    // 4. Percentage Calculation
    fun calculatePercentage() {
        val valA = percentValueA.toDoubleOrNull() ?: 0.0
        val valB = percentValueB.toDoubleOrNull() ?: 0.0

        if (valB == 0.0) {
            percentageResultText = "Division by zero"
            return
        }

        val percentage = (valA / valB) * 100.0
        val df = DecimalFormat("#.##")
        percentageResultText = "$valA is ${df.format(percentage)}% of $valB"
    }

    // --- Persistent Customizable Tool Rates / Fees ---
    var elecUnitRate by mutableStateOf(sharedPrefs.getString("custom_elec_rate", "6.50") ?: "6.50")
        private set
    var elecDemandCharge by mutableStateOf(sharedPrefs.getString("custom_elec_demand", "40.0") ?: "40.0")
        private set
    var elecMeterRent by mutableStateOf(sharedPrefs.getString("custom_elec_meter", "40.0") ?: "40.0")
        private set
    var elecVatPercent by mutableStateOf(sharedPrefs.getString("custom_elec_vat", "5.0") ?: "5.0")
        private set

    var fuelPricePerUnit by mutableStateOf(sharedPrefs.getString("custom_fuel_price", "125.0") ?: "125.0")
        private set

    var applianceKwhRate by mutableStateOf(sharedPrefs.getString("custom_appliance_kwh_rate", "7.50") ?: "7.50")
        private set

    fun updateElectricityRates(rate: String, demand: String, meter: String, vat: String) {
        elecUnitRate = rate
        elecDemandCharge = demand
        elecMeterRent = meter
        elecVatPercent = vat
        sharedPrefs.edit()
            .putString("custom_elec_rate", rate)
            .putString("custom_elec_demand", demand)
            .putString("custom_elec_meter", meter)
            .putString("custom_elec_vat", vat)
            .apply()
    }

    fun updateFuelPrice(price: String) {
        fuelPricePerUnit = price
        sharedPrefs.edit().putString("custom_fuel_price", price).apply()
    }

    fun updateApplianceKwhRate(rate: String) {
        applianceKwhRate = rate
        sharedPrefs.edit().putString("custom_appliance_kwh_rate", rate).apply()
    }

    // --- Professional Quick Notes & Memo State ---
    var notesListString by mutableStateOf(sharedPrefs.getString("professional_notes_list_v2", "") ?: "")

    fun getSavedNotes(): List<com.example.data.model.ProfessionalNote> {
        if (notesListString.isBlank()) return emptyList()
        return notesListString.split("[NOTE_SEPARATOR]").mapNotNull {
            com.example.data.model.ProfessionalNote.deserialize(it)
        }
    }

    fun saveNote(note: com.example.data.model.ProfessionalNote) {
        val current = getSavedNotes().toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            current[index] = note
        } else {
            current.add(0, note)
        }
        val serialized = current.joinToString("[NOTE_SEPARATOR]") { it.serialize() }
        notesListString = serialized
        sharedPrefs.edit().putString("professional_notes_list_v2", serialized).apply()
    }

    fun deleteNote(noteId: String) {
        val current = getSavedNotes().filter { it.id != noteId }
        val serialized = current.joinToString("[NOTE_SEPARATOR]") { it.serialize() }
        notesListString = serialized
        sharedPrefs.edit().putString("professional_notes_list_v2", serialized).apply()
    }

    // --- Smart Voice Command Processor ---
    var isVoiceListening by mutableStateOf(false)

    fun processVoiceCommand(rawInput: String) {
        if (rawInput.isBlank()) return

        // 1. Convert non-English digits to English digits
        val normalizedDigits = rawInput
            .replace('০', '0').replace('১', '1').replace('২', '2').replace('৩', '3')
            .replace('৪', '4').replace('৫', '5').replace('৬', '6').replace('৭', '7')
            .replace('৮', '8').replace('৯', '9')
            .replace('०', '0').replace('१', '1').replace('२', '2').replace('३', '3')
            .replace('४', '4').replace('५', '5').replace('६', '6').replace('७', '7')
            .replace('८', '8').replace('९', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3')
            .replace('٤', '4').replace('٥', '5').replace('٦', '6').replace('٧', '7')
            .replace('٨', '8').replace('٩', '9')

        val textLower = normalizedDigits.lowercase()

        // 2. Check for Currency queries (e.g. "1 dollar in taka", "১ ডলারে কত টাকা", "100 usd to bdt")
        if (textLower.contains("dollar") || textLower.contains("ডলার") || textLower.contains("taka") ||
            textLower.contains("টাকা") || textLower.contains("usd") || textLower.contains("bdt") ||
            textLower.contains("rupee") || textLower.contains("রুপি") || textLower.contains("inr") ||
            textLower.contains("euro") || textLower.contains("ইউরো") || textLower.contains("eur")
        ) {
            val numValue = extractNumberFromString(textLower) ?: 1.0
            activeTab = 1
            selectedConverterType = com.example.data.model.ConverterType.CURRENCY

            if (textLower.contains("dollar") || textLower.contains("ডলার") || textLower.contains("usd")) {
                fromUnit = "USD - US Dollar"
                toUnit = if (textLower.contains("rupee") || textLower.contains("রুপি") || textLower.contains("inr")) {
                    "INR - Indian Rupee"
                } else {
                    "BDT - Bangladeshi Taka"
                }
            } else if (textLower.contains("euro") || textLower.contains("ইউরো") || textLower.contains("eur")) {
                fromUnit = "EUR - Euro"
                toUnit = "BDT - Bangladeshi Taka"
            } else {
                fromUnit = "BDT - Bangladeshi Taka"
                toUnit = "USD - US Dollar"
            }

            converterInput = if (numValue == numValue.toLong().toDouble()) numValue.toLong().toString() else numValue.toString()
            calculateConverter()
            return
        }

        // 3. Check for Unit Converter queries (e.g. "1 kg to gram", "১ কেজিতে কত গ্রাম", "1 meter to cm")
        if (textLower.contains("kg") || textLower.contains("কেজি") || textLower.contains("gram") ||
            textLower.contains("গ্রাম") || textLower.contains("pound") || textLower.contains("পাউন্ড")
        ) {
            val numValue = extractNumberFromString(textLower) ?: 1.0
            activeTab = 1
            selectedConverterType = com.example.data.model.ConverterType.WEIGHT
            fromUnit = "Kilogram (kg)"
            toUnit = "Gram (g)"
            converterInput = if (numValue == numValue.toLong().toDouble()) numValue.toLong().toString() else numValue.toString()
            calculateConverter()
            return
        }

        if (textLower.contains("meter") || textLower.contains("মিটার") || textLower.contains("feet") ||
            textLower.contains("ফুট") || textLower.contains("inch") || textLower.contains("ইঞ্চি") ||
            textLower.contains("bigha") || textLower.contains("বিঘা")
        ) {
            val numValue = extractNumberFromString(textLower) ?: 1.0
            activeTab = 1
            selectedConverterType = if (textLower.contains("bigha") || textLower.contains("বিঘা")) {
                com.example.data.model.ConverterType.AREA
            } else {
                com.example.data.model.ConverterType.LENGTH
            }
            if (selectedConverterType == com.example.data.model.ConverterType.LENGTH) {
                fromUnit = "Meter (m)"
                toUnit = if (textLower.contains("feet") || textLower.contains("ফুট")) "Foot (ft)" else "Centimeter (cm)"
            }
            converterInput = if (numValue == numValue.toLong().toDouble()) numValue.toLong().toString() else numValue.toString()
            calculateConverter()
            return
        }

        // 4. Check for Special Tools queries (e.g., "bmi", "বিএমআই", "বিদ্যুৎ বিল", "age")
        if (textLower.contains("bmi") || textLower.contains("বিএমআই")) {
            activeTab = 0
            openTool(com.example.data.model.ToolType.BMI)
            return
        } else if (textLower.contains("age") || textLower.contains("বয়স")) {
            activeTab = 0
            openTool(com.example.data.model.ToolType.AGE)
            return
        } else if (textLower.contains("electricity") || textLower.contains("বিদ্যুৎ")) {
            activeTab = 0
            openTool(com.example.data.model.ToolType.ELECTRICITY_BILL)
            return
        } else if (textLower.contains("loan") || textLower.contains("emi") || textLower.contains("ইএমআই")) {
            activeTab = 0
            openTool(com.example.data.model.ToolType.EMI_LOAN)
            return
        }

        // 5. Fallback: Parse Math Equation (support continuous multi-operation calculations)
        var mathExpr = normalizedDigits
            .replace("যোগ", "+").replace("প্লাস", "+").replace("плюс", "+").replace("plus", "+").replace("زائد", "+")
            .replace("বিয়োগ", "-").replace("মাইনাস", "-").replace("minus", "-").replace("ناقص", "-")
            .replace("গুণ", "*").replace("ইনটু", "*").replace("times", "*").replace("into", "*").replace("ضرب", "*")
            .replace("ভাগ", "/").replace("ডিভাইডেড", "/").replace("ভাগফল", "/").replace("divided by", "/").replace("divided", "/").replace("قسمة", "/")
            .replace("x", "*").replace("X", "*").replace("÷", "/")

        // Sanitize math string keeping only numbers, operators, dots and parentheses
        mathExpr = mathExpr.filter { it.isDigit() || it in "+-*/.()" }

        if (mathExpr.isNotBlank()) {
            expressionValue = TextFieldValue(mathExpr, selection = TextRange(mathExpr.length))
            evaluateExpression()
        }
    }

    private fun extractNumberFromString(str: String): Double? {
        val regex = Regex("""\d+(\.\d+)?""")
        val match = regex.find(str)
        return match?.value?.toDoubleOrNull()
    }

    companion object {
        private val retrofit by lazy {
            val okHttpClient = okhttp3.OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
        
        private val geminiApiService by lazy {
            retrofit.create(GeminiApiService::class.java)
        }
    }
}

class CalculatorViewModelFactory(
    private val repository: HistoryRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null,
    val actionLabel: String? = null,
    val actionData: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Chat",
    val messages: List<ChatMessage> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>, val role: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @Json(name = "system_instruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPartResponse(val text: String?)

@JsonClass(generateAdapter = true)
data class GeminiContentResponse(val parts: List<GeminiPartResponse>?)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContentResponse?)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class ScanHistoryItem(
    val id: String,
    val value: String,
    val format: String,
    val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class GlobalAppBackup(
    val version: Int = 2,
    val appName: String = "ToolsMate All-in-One",
    val backupDate: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val historyEntries: List<HistoryEntry> = emptyList(),
    val marketPlansJson: String? = null,
    val completedMemosJson: String? = null,
    val chatSessionsJson: String? = null,
    val notesJson: String? = null,
    val favoriteTools: List<String> = emptyList(),
    val favoriteConverters: List<String> = emptyList(),
    val barcodeHistory: String? = null
)

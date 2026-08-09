package com.example.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
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
    private val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
    private val chatSessionListType = com.squareup.moshi.Types.newParameterizedType(List::class.java, ChatSession::class.java)
    private val sessionAdapter = moshi.adapter<List<ChatSession>>(chatSessionListType)

    private fun saveChatHistory() {
        val json = sessionAdapter.toJson(chatSessions)
        chatPrefs.edit().putString("chat_sessions", json).apply()
        chatPrefs.edit().putString("current_session_id", currentSessionId).apply()
    }

    private fun loadChatHistory() {
        val json = chatPrefs.getString("chat_sessions", null)
        if (json != null) {
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    // Current Active Tab: 0 = Calculator, 1 = Converter, 2 = Special, 3 = History, 4 = Themes
    var activeTab by mutableStateOf(0)
    var isEvaluated by mutableStateOf(false)

    // History deletion confirmation state
    var showClearHistoryDialog by mutableStateOf(false)
    var showClearChatDialog by mutableStateOf(false)
    var showDeleteSingleDialog by mutableStateOf(false)
    var pendingDeleteId by mutableStateOf<Long?>(null)
    
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
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun callGeminiApi(contents: List<GeminiContent>, systemInstruction: String): String? = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext null
        
        try {
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
            )
            val response = geminiApiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
            // Greetings: Hello/Hi
            normalized.contains("hello") || normalized.contains(" hi ") || normalized.startsWith("hi") || normalized.contains("hey") ||
            normalized.contains("হ্যালো") || normalized.contains("হাই") || normalized.contains("সালাম") || normalized.contains("salam") || normalized.contains("আসসালামু আলাইকুম") -> {
                replyText = if (isBn) {
                    "আসসালামু আলাইকুম ও হ্যালো! 👋 আমি আপনার স্মার্ট ক্যালকুলেটর অফলাইন এআই সহকারী। আমি কিভাবে আপনাকে সাহায্য করতে পারি?"
                } else {
                    "Hello there! 👋 I am your Smart Calculator Offline AI Assistant. How can I help you today?"
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
                    "আমার নাম **স্মার্ট এআই সহকারী (Smart AI Assistant)**! 🤖 আমি এই স্মার্ট ক্যালকুলেটর অ্যাপেরই একটি অংশ।"
                } else {
                    "My name is **Smart AI Assistant**! 🤖 I am a built-in part of this Smart Calculator app."
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
            var replyText = ""
            var actType: String? = null
            var actLabel: String? = null
            var actData: String? = null
            var usedOnlineModel = false
            
            if (isNetworkAvailable() && com.example.BuildConfig.GEMINI_API_KEY.isNotBlank() && com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
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
                
                val onlineReply = callGeminiApi(contents, systemInstruction)
                if (onlineReply != null) {
                    replyText = onlineReply
                    usedOnlineModel = true
                    
                    val detectedAction = detectLocalAction(normalized, isBn)
                    if (detectedAction != null) {
                        actType = detectedAction.actionType
                        actLabel = detectedAction.actionLabel
                        actData = detectedAction.actionData
                    }
                }
            }
            
            if (!usedOnlineModel) {
                val offlineResult = runOfflineModel(normalized, isBn)
                replyText = offlineResult.replyText
                actType = offlineResult.actionType
                actLabel = offlineResult.actionLabel
                actData = offlineResult.actionData
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
            isAiLoading = false
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
                        activeTab = 2
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
                        activeTab = 2
                        openTool(com.example.data.model.ToolType.DISCOUNT)
                        originalPrice = parts[0]
                        discountPercent = parts[1]
                        taxPercent = "0"
                        calculateDiscount()
                    }
                }
                "navigate_tool" -> {
                    activeTab = 2
                    val tool = com.example.data.model.ToolType.valueOf(actionData)
                    openTool(tool)
                }
                "calculate" -> {
                    activeTab = 0
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
    private val sharedPrefs = context.getSharedPreferences("smart_calc_prefs", Context.MODE_PRIVATE)

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

    fun addCustomTheme(theme: com.example.ui.theme.CustomTheme) {
        customThemes = customThemes + theme
        saveCustomThemes()
    }

    fun updateCustomTheme(theme: com.example.ui.theme.CustomTheme) {
        customThemes = customThemes.map { if (it.id == theme.id) theme else it }
        saveCustomThemes()
    }

    fun deleteCustomTheme(id: String) {
        if (currentCustomThemeId == id) {
            isCustomThemeActive = false
            currentCustomThemeId = null
            sharedPrefs.edit().putBoolean("is_custom_theme_active", false).remove("current_custom_theme_id").apply()
        }
        customThemes = customThemes.filter { it.id != id }
        saveCustomThemes()
    }

    fun setCustomTheme(id: String) {
        isCustomThemeActive = true
        currentCustomThemeId = id
        sharedPrefs.edit().putBoolean("is_custom_theme_active", true).putString("current_custom_theme_id", id).apply()
    }

    fun getCurrentThemeColors(): com.example.ui.theme.CalculatorThemeColors {
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

    init {
        // Auto-fetch exchange rates when connected
        fetchExchangeRates()

        // Trigger initial calculations
        calculateConverter()
        calculateBMI()
        calculateAge()
        calculateDiscount()
        calculatePercentage()
        loadChatHistory()
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

    fun onExpressionValueChange(newValue: TextFieldValue) {
        expressionValue = newValue
        tryEvaluatePreview()
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
    }

    private fun tryEvaluatePreview() {
        if (expression.isBlank()) {
            result = ""
            return
        }
        try {
            // Check if there are unclosed parentheses and temporarily close them for preview
            var tempExpr = expression
            val openCount = tempExpr.count { it == '(' }
            val closeCount = tempExpr.count { it == ')' }
            if (openCount > closeCount) {
                tempExpr += ")".repeat(openCount - closeCount)
            }

            val evalResult = ExpressionEvaluator.evaluate(tempExpr, isDegreeMode)
            if (!evalResult.isInfinite() && !evalResult.isNaN()) {
                val df = DecimalFormat("#.########")
                result = df.format(evalResult)
            }
        } catch (e: Exception) {
            // Silently keep previous or clear preview if invalid
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

            // Save to database
            viewModelScope.launch {
                repository.insertHistory(
                    HistoryEntry(
                        expression = expression,
                        result = formatted,
                        type = "Calculator"
                    )
                )
            }
        } catch (e: Exception) {
            result = "Error"
            isEvaluated = true
        }
    }

    fun selectHistoryItem(entry: HistoryEntry) {
        expressionValue = TextFieldValue(entry.expression, selection = TextRange(entry.expression.length))
        result = entry.result
        activeTab = 0 // Switch to calculator
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
    }

    // --- Unit Converter Engine ---
    fun openConverter(type: com.example.data.model.ConverterType) {
        selectedConverterType = type
        fromUnit = type.units.firstOrNull() ?: ""
        toUnit = type.units.getOrNull(1) ?: fromUnit
        converterInput = "1"
        calculateConverter()
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

    fun calculateConverter() {
        val type = selectedConverterType ?: return
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
            activeTab = 2
            openTool(com.example.data.model.ToolType.BMI)
            return
        } else if (textLower.contains("age") || textLower.contains("বয়স")) {
            activeTab = 2
            openTool(com.example.data.model.ToolType.AGE)
            return
        } else if (textLower.contains("electricity") || textLower.contains("বিদ্যুৎ")) {
            activeTab = 2
            openTool(com.example.data.model.ToolType.ELECTRICITY_BILL)
            return
        } else if (textLower.contains("loan") || textLower.contains("emi") || textLower.contains("ইএমআই")) {
            activeTab = 2
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
            activeTab = 0
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
            Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
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

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null,
    val actionLabel: String? = null,
    val actionData: String? = null
)

data class ChatSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val messages: List<ChatMessage>,
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

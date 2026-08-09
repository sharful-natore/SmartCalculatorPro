import re

with open("app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt", "r") as f:
    content = f.read()

# Add moshi and prefs logic
moshi_setup = """    private val chatPrefs = context.getSharedPreferences("ai_chat_prefs", Context.MODE_PRIVATE)
    private val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
    private val chatMessageListType = com.squareup.moshi.Types.newParameterizedType(List::class.java, ChatMessage::class.java)
    private val chatAdapter = moshi.adapter<List<ChatMessage>>(chatMessageListType)

    private fun saveChatHistory() {
        val json = chatAdapter.toJson(aiChatMessages)
        chatPrefs.edit().putString("chat_history", json).apply()
    }

    private fun loadChatHistory() {
        val json = chatPrefs.getString("chat_history", null)
        if (json != null) {
            try {
                val loaded = chatAdapter.fromJson(json)
                if (loaded != null && loaded.isNotEmpty()) {
                    aiChatMessages = loaded
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        resetAiChat()
    }
"""

if "chatPrefs" not in content:
    content = content.replace("class CalculatorViewModel(\n    private val repository: HistoryRepository,\n    private val context: Context\n) : ViewModel() {", "class CalculatorViewModel(\n    private val repository: HistoryRepository,\n    private val context: Context\n) : ViewModel() {\n\n" + moshi_setup)

content = content.replace("aiChatMessages = listOf(ChatMessage(text = welcomeText, isUser = false))", "aiChatMessages = listOf(ChatMessage(text = welcomeText, isUser = false))\n        saveChatHistory()")
content = content.replace("aiChatMessages = aiChatMessages + userMsg", "aiChatMessages = aiChatMessages + userMsg\n            saveChatHistory()")
content = content.replace("aiChatMessages = aiChatMessages + aiReply", "aiChatMessages = aiChatMessages + aiReply\n            saveChatHistory()")

content = content.replace("resetAiChat()\n    }", "loadChatHistory()\n    }")

with open("app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt", "w") as f:
    f.write(content)

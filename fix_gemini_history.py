import re

with open("app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt", "r") as f:
    content = f.read()

# 1. Update callGeminiApi to take contents instead of prompt String
target_func = """    private suspend fun callGeminiApi(prompt: String, systemInstruction: String): String? = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext null
        
        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
            )"""

replacement_func = """    private suspend fun callGeminiApi(contents: List<GeminiContent>, systemInstruction: String): String? = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext null
        
        try {
            val request = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
            )"""

if target_func in content:
    content = content.replace(target_func, replacement_func)
    print("Fixed callGeminiApi signature.")

# 2. Update the caller
target_caller = """                val onlineReply = callGeminiApi(rawText, systemInstruction)"""

replacement_caller = """                // Prepare history
                val contents = mutableListOf<GeminiContent>()
                aiChatMessages.takeLast(10).forEach { msg ->
                    // Skip the first welcome message
                    if (msg.text.contains("Hello! I am your AI Assistant") || msg.text.contains("হ্যালো! আমি আপনার অফলাইন এআই সহকারী")) return@forEach
                    
                    val role = if (msg.isUser) "user" else "model"
                    contents.add(GeminiContent(parts = listOf(GeminiPart(text = msg.text)), role = role))
                }
                contents.add(GeminiContent(parts = listOf(GeminiPart(text = rawText)), role = "user"))
                
                val onlineReply = callGeminiApi(contents, systemInstruction)"""

if target_caller in content:
    content = content.replace(target_caller, replacement_caller)
    print("Fixed caller.")

# 3. Add 'role' to GeminiContent if not present
target_content_class = """data class GeminiContent(val parts: List<GeminiPart>)"""
replacement_content_class = """data class GeminiContent(val parts: List<GeminiPart>, val role: String? = null)"""
if target_content_class in content:
    content = content.replace(target_content_class, replacement_content_class)
    print("Fixed GeminiContent.")

with open("app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt", "w") as f:
    f.write(content)


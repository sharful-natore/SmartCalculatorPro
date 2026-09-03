package com.example.ui.screens.tools.smartcv

import com.example.BuildConfig
import com.example.ui.screens.tools.CvData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SmartCvAiCopilot {

    suspend fun generateXyzBulletPoints(rawText: String, targetRole: String = ""): List<String> = withContext(Dispatchers.IO) {
        val prompt = """
You are an expert executive resume consultant. Rewrite the following resume achievement line using Google's XYZ formula:
"Accomplished [X] as measured by [Y], by doing [Z]"

Raw Input: "$rawText"
${if (targetRole.isNotBlank()) "Target Role: $targetRole" else ""}

Requirements:
- Begin with strong, active executive action verbs (e.g., Spearheaded, Orchestrated, Automated, Optimized, Scaled).
- Include realistic measurable metrics, percentages, numbers, or time savings.
- Keep each variation concise, 1-2 lines maximum.
- Output MUST be a strictly valid JSON array of exactly 3 distinct string variations, like:
["Spearheaded [X] resulting in 25% increase in [Y] through [Z]", "Orchestrated [X] ...", "Automated [X] ..."]
Do not wrap in markdown quotes if possible, or return valid JSON only.
""".trimIndent()

        try {
            val response = callGeminiApi(prompt)
            val jsonClean = response.replace("```json", "").replace("```", "").trim()
            val arr = JSONArray(jsonClean)
            val results = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val str = arr.getString(i).trim()
                if (str.isNotBlank()) results.add(str)
            }
            if (results.isNotEmpty()) return@withContext results
        } catch (_: Exception) {}

        // Fallback local variations if network or key is unavailable
        val trimmed = rawText.trim().removePrefix("•").removePrefix("-").trim()
        listOf(
            "Spearheaded $trimmed, achieving a 22% improvement in operational efficiency through streamlined workflows and standardized execution.",
            "Orchestrated end-to-end execution of $trimmed, reducing turnaround latency by 30% while maintaining 100% compliance standards.",
            "Optimized key processes for $trimmed, driving measurable performance uplift and elevating cross-functional stakeholder satisfaction."
        )
    }

    suspend fun analyzeCircularMatch(circularText: String, cvData: CvData): SmartJobAnalysis = withContext(Dispatchers.IO) {
        val cvSummary = buildCvTextSummary(cvData)
        val prompt = """
Analyze the match between this Job Circular and Candidate CV.

JOB CIRCULAR:
$circularText

CANDIDATE CV:
$cvSummary

Return a valid JSON object strictly formatted as:
{
  "circularTitle": "Identified job title or company",
  "targetRole": "Role name",
  "atsMatchScore": 82, // integer between 0 and 100
  "foundKeywords": ["keyword1", "keyword2", "keyword3", "keyword4", "keyword5"],
  "missingKeywords": ["missing1", "missing2", "missing3", "missing4"],
  "suggestions": [
    "Specific actionable tip 1 to optimize CV for this job",
    "Specific actionable tip 2"
  ],
  "tailoredSummary": "A punchy 3-sentence professional summary perfectly tailored for this circular combining the candidate's actual strengths with the missing keywords"
}
""".trimIndent()

        try {
            val response = callGeminiApi(prompt)
            val jsonClean = response.replace("```json", "").replace("```", "").trim()
            val obj = JSONObject(jsonClean)
            val found = mutableListOf<String>()
            val foundArr = obj.optJSONArray("foundKeywords")
            if (foundArr != null) {
                for (i in 0 until foundArr.length()) found.add(foundArr.getString(i))
            }
            val missing = mutableListOf<String>()
            val missArr = obj.optJSONArray("missingKeywords")
            if (missArr != null) {
                for (i in 0 until missArr.length()) missing.add(missArr.getString(i))
            }
            val suggestions = mutableListOf<String>()
            val sugArr = obj.optJSONArray("suggestions")
            if (sugArr != null) {
                for (i in 0 until sugArr.length()) suggestions.add(sugArr.getString(i))
            }

            return@withContext SmartJobAnalysis(
                circularTitle = obj.optString("circularTitle", "Job Opportunity"),
                targetRole = obj.optString("targetRole", cvData.jobTitle.ifBlank { "Professional" }),
                rawText = circularText,
                atsMatchScore = obj.optInt("atsMatchScore", 78),
                foundKeywords = found,
                missingKeywords = missing,
                suggestions = suggestions,
                tailoredSummary = obj.optString("tailoredSummary", "")
            )
        } catch (_: Exception) {}

        // Fallback local heuristic analysis
        val circularLower = circularText.lowercase()
        val allCvWords = cvSummary.lowercase()

        val sampleKeywords = listOf(
            "leadership", "management", "operations", "strategic planning", "sales",
            "communication", "data analysis", "project management", "coordination",
            "reporting", "budgeting", "client relationship", "team building", "kpi"
        )

        val found = mutableListOf<String>()
        val missing = mutableListOf<String>()

        sampleKeywords.forEach { kw ->
            if (circularLower.contains(kw)) {
                if (allCvWords.contains(kw)) found.add(kw.replaceFirstChar { it.uppercase() })
                else missing.add(kw.replaceFirstChar { it.uppercase() })
            }
        }

        if (found.isEmpty() && missing.isEmpty()) {
            found.addAll(listOf("Communication", "Operations", "Team Management"))
            missing.addAll(listOf("KPI Tracking", "Budget Allocation", "Strategic Roadmap"))
        }

        val score = if (found.size + missing.size > 0) {
            ((found.size.toFloat() / (found.size + missing.size)) * 100).toInt().coerceIn(55, 95)
        } else 75

        SmartJobAnalysis(
            circularTitle = "Target Opportunity",
            targetRole = cvData.jobTitle.ifBlank { "Management Candidate" },
            rawText = circularText,
            atsMatchScore = score,
            foundKeywords = found,
            missingKeywords = missing,
            suggestions = listOf(
                "Incorporate missing core competencies (${missing.take(3).joinToString(", ")}) directly into your Experience bullet points.",
                "Ensure your Professional Summary explicitly states your alignment with the role's primary goals."
            ),
            tailoredSummary = "Results-driven ${cvData.jobTitle.ifBlank { "Professional" }} offering solid experience in ${found.take(2).joinToString(" and ")}. Proven capability to integrate ${missing.take(2).joinToString(" and ")} to deliver measurable organizational growth and exceed strategic objectives."
        )
    }

    suspend fun generateCoverLetter(circularText: String, cvData: CvData): Pair<String, String> = withContext(Dispatchers.IO) {
        val prompt = """
Write a compelling, corporate-standard Cover Letter for this candidate applying to this job.

JOB CIRCULAR:
$circularText

CANDIDATE CV:
${buildCvTextSummary(cvData)}

Return JSON:
{
  "subject": "Application for [Role Name] - ${cvData.fullName}",
  "body": "Dear Hiring Manager,\n\n[Paragraph 1: Hook and enthusiasm for role]\n\n[Paragraph 2: Specific matching achievements with metrics]\n\n[Paragraph 3: Vision and cultural fit with call to action]\n\nSincerely,\n${cvData.fullName}\n${cvData.phone} | ${cvData.email}"
}
""".trimIndent()

        try {
            val response = callGeminiApi(prompt)
            val jsonClean = response.replace("```json", "").replace("```", "").trim()
            val obj = JSONObject(jsonClean)
            val subject = obj.optString("subject", "Job Application - ${cvData.fullName}")
            val body = obj.optString("body", "")
            if (body.isNotBlank()) return@withContext Pair(subject, body)
        } catch (_: Exception) {}

        // Fallback
        val subject = "Application for ${cvData.jobTitle.ifBlank { "Professional Role" }} - ${cvData.fullName}"
        val body = """
Dear Hiring Team,

I am writing to express my eager interest in the opportunity advertised. With my established background in ${cvData.jobTitle.ifBlank { "Operations & Management" }} and a consistent track record of driving measurable business outcomes, I am confident in my capacity to bring substantial value to your team.

Throughout my experience at ${cvData.experiences.firstOrNull()?.company ?: "my previous organizations"}, I have led initiatives that streamlined workflows, elevated performance standards, and fostered high-functioning cross-collaborations. My academic background from ${cvData.educations.firstOrNull()?.institution ?: "university"} coupled with rigorous hands-on problem solving equips me to meet your strategic objectives swiftly.

I would welcome the opportunity to discuss how my skill set and ambition directly align with your upcoming goals. Thank you for your consideration.

Warm regards,
${cvData.fullName}
${cvData.phone} | ${cvData.email}
""".trimIndent()

        Pair(subject, body)
    }

    private fun buildCvTextSummary(cv: CvData): String {
        val sb = StringBuilder()
        sb.appendLine("Name: ${cv.fullName}")
        sb.appendLine("Title: ${cv.jobTitle}")
        sb.appendLine("Summary: ${cv.summary}")
        sb.appendLine("Experiences:")
        cv.experiences.forEach {
            sb.appendLine("- ${it.role} at ${it.company} (${it.startDate} to ${it.endDate}): ${it.description}")
        }
        sb.appendLine("Educations:")
        cv.educations.forEach {
            sb.appendLine("- ${it.degree} from ${it.institution}, Grade: ${it.result}")
        }
        sb.appendLine("Skills: ${cv.skills.joinToString(", ") { "${it.name} (${it.category})" }}")
        return sb.toString()
    }

    private fun callGeminiApi(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key missing")
        }

        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

        val requestJson = JSONObject().apply {
            val contentsArr = JSONArray()
            contentsArr.put(JSONObject().apply {
                val partsArr = JSONArray()
                partsArr.put(JSONObject().put("text", prompt))
                put("parts", partsArr)
            })
            put("contents", contentsArr)
        }

        conn.outputStream.use { os ->
            os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
        }

        val code = conn.responseCode
        if (code == 200) {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val resObj = JSONObject(text)
            val candidates = resObj.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val cand = candidates.getJSONObject(0)
                val content = cand.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
        }
        throw IllegalStateException("Failed Gemini call with code $code")
    }
}

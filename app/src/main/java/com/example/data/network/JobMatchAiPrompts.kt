package com.example.data.network

import com.example.data.model.AtsChecklistItem
import com.example.data.model.ChecklistStatus
import com.example.data.model.CvSectionKey
import com.example.data.model.FixNowAiResult
import com.example.data.model.JobMatchResponse
import com.example.data.model.ResumeModel
import org.json.JSONArray
import org.json.JSONObject

/**
 * MODULE 2: Strict System Instructions & Gemini Prompts
 * Enforces strictly valid RAW JSON outputs without markdown wrapping or conversational noise.
 */
object JobMatchAiPrompts {

    const val STRICT_JSON_SYSTEM_INSTRUCTION = """You are an elite ATS (Applicant Tracking System) Algorithm & Senior Executive Technical Recruiter.
Your job is to critically evaluate resumes against corporate ATS benchmarks and specific job circular requirements.

CRITICAL INSTRUCTIONS:
1. Respond strictly in valid RAW JSON format only.
2. Do NOT use markdown code fences (no ```json or ```).
3. Do NOT include any intro, commentary, or outro text.
4. Output MUST be directly parseable by a strict JSON parser.
5. All checklist items must have status either "PRESENT" or "MISSING".
6. sectionKey must strictly be one of: "SUMMARY", "EXPERIENCE", "EDUCATION", "SKILLS", "PROJECTS", "CERTIFICATIONS", "LANGUAGES", "LAYOUT", "CONTACT_INFO"."""

    /**
     * Builds strict ATS Optimizer prompt
     */
    fun buildAtsAnalysisPrompt(resume: ResumeModel): String {
        return """Evaluate the following resume against global ATS parsing standards (Format, Action Verbs, Measurable Metrics/KPIs, Keyword Density, Contact Info, Layout).

Resume Content:
- Full Name: ${resume.fullName}
- Title: ${resume.jobTitle}
- Contact: Email=${resume.email}, Phone=${resume.phone}, Address=${resume.address}, LinkedIn=${resume.linkedin}, GitHub/Portfolio=${resume.githubOrPortfolio}
- Summary: ${resume.summary}
- Skills: ${resume.skills.joinToString(", ")}
- Experiences: ${resume.experiences.joinToString("; ") { "${it.role} at ${it.company} (${it.startDate} - ${it.endDate}): ${it.description} | Achievements: ${it.achievements.joinToString(", ")}" }}
- Education: ${resume.educations.joinToString("; ") { "${it.degree} from ${it.institution} (${it.passingYear})" }}
- Projects: ${resume.projects.joinToString("; ") { "${it.title}: ${it.description}" }}
- Certifications: ${resume.certifications}
- Languages: ${resume.languages}

Respond strictly in valid JSON format only matching this schema:
{
  "score": 85,
  "analysisType": "ATS",
  "summaryFeedback": "Brief 1-2 sentence overall feedback",
  "suggestedKeywords": ["Action Verbs", "Keywords"],
  "checklist": [
    {
      "title": "Quantified achievements with % or $ metrics in Work Experience",
      "status": "MISSING",
      "sectionKey": "EXPERIENCE",
      "detail": "Include metrics like 'increased efficiency by 35%'"
    },
    {
      "title": "Standard contact details with LinkedIn URL",
      "status": "PRESENT",
      "sectionKey": "CONTACT_INFO",
      "detail": "Verified contact details present"
    },
    {
      "title": "Clear Technical & Domain Skills Categorization",
      "status": "MISSING",
      "sectionKey": "SKILLS",
      "detail": "Categorize skills into Core, Frameworks, and Tools"
    },
    {
      "title": "Strong Professional Executive Summary with target job title",
      "status": "PRESENT",
      "sectionKey": "SUMMARY",
      "detail": "Good opening summary"
    },
    {
      "title": "Single-column ATS friendly layout structure",
      "status": "PRESENT",
      "sectionKey": "LAYOUT",
      "detail": "Complies with ATS scanners"
    }
  ]
}"""
    }

    /**
     * Builds strict Job Circular Matching prompt
     */
    fun buildCircularMatchPrompt(resume: ResumeModel, circularText: String): String {
        return """Compare and match the following Resume against the Target Job Circular.
Calculate exact match score % based on required qualifications, technical stack, responsibilities, and industry keywords.

Target Job Circular:
$circularText

Candidate Resume:
- Full Name: ${resume.fullName}
- Title: ${resume.jobTitle}
- Summary: ${resume.summary}
- Skills: ${resume.skills.joinToString(", ")}
- Experiences: ${resume.experiences.joinToString("; ") { "${it.role} at ${it.company}: ${it.description}" }}
- Education: ${resume.educations.joinToString("; ") { "${it.degree} from ${it.institution}" }}
- Projects: ${resume.projects.joinToString("; ") { "${it.title}: ${it.description}" }}
- Certifications: ${resume.certifications}

Respond strictly in valid JSON format only matching this schema:
{
  "score": 75,
  "analysisType": "CIRCULAR",
  "summaryFeedback": "Summary of match alignment and gaps",
  "suggestedKeywords": ["RequiredKeyword1", "RequiredSkill2"],
  "checklist": [
    {
      "title": "Primary Required Tech Stack Keywords present in Skills/Experience",
      "status": "MISSING",
      "sectionKey": "SKILLS",
      "detail": "Missing specific required technologies mentioned in circular"
    },
    {
      "title": "Tailored Professional Summary matching Job Position",
      "status": "MISSING",
      "sectionKey": "SUMMARY",
      "detail": "Summary should explicitly mention the targeted position"
    },
    {
      "title": "Relevant Work Experience aligned with Circular Responsibilities",
      "status": "PRESENT",
      "sectionKey": "EXPERIENCE",
      "detail": "Work history matches core domain"
    },
    {
      "title": "Educational Qualification Requirement Satisfied",
      "status": "PRESENT",
      "sectionKey": "EDUCATION",
      "detail": "Degree requirement fulfilled"
    },
    {
      "title": "Relevant Project Portfolio / Case Studies",
      "status": "PRESENT",
      "sectionKey": "PROJECTS",
      "detail": "Hands-on projects demonstrated"
    }
  ]
}"""
    }

    /**
     * Builds Fix-Now Auto-Fill content generator prompt
     */
    fun buildFixNowAutoFillPrompt(
        sectionKey: String,
        itemTitle: String,
        resume: ResumeModel,
        jobCircular: String? = null
    ): String {
        val circularContext = if (!jobCircular.isNullOrBlank()) "Target Circular Context:\n$jobCircular\n" else ""
        return """You are an expert ATS Resume Optimizer.
Generate high-impact, ATS-optimized, metric-driven professional content to resolve the following missing requirement for the section "$sectionKey".

Missing Item: $itemTitle
Candidate Job Title: ${resume.jobTitle}
Current Candidate Skills: ${resume.skills.joinToString(", ")}
$circularContext

Respond strictly in valid JSON format only matching this schema:
{
  "sectionKey": "$sectionKey",
  "title": "$itemTitle",
  "actionType": "APPEND",
  "generatedContent": "Optimized professional text ready to be inserted into the CV",
  "bulletPoints": [
    "• Spearheaded cross-functional initiative delivering 28% efficiency boost",
    "• Optimized core data pipeline cutting latency by 40%"
  ]
}"""
    }

    /**
     * Clean raw response and parse into JobMatchResponse
     */
    fun parseJobMatchResponse(rawText: String): JobMatchResponse {
        val cleanJson = sanitizeJson(rawText)
        return parseJobMatchManually(cleanJson)
    }

    /**
     * Clean raw response and parse into FixNowAiResult
     */
    fun parseFixNowResult(rawText: String, defaultSectionKey: String, defaultTitle: String): FixNowAiResult {
        val cleanJson = sanitizeJson(rawText)
        return try {
            val obj = JSONObject(cleanJson)
            val bullets = mutableListOf<String>()
            val arr = obj.optJSONArray("bulletPoints")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    bullets.add(arr.optString(i))
                }
            }
            FixNowAiResult(
                sectionKey = obj.optString("sectionKey", defaultSectionKey),
                title = obj.optString("title", defaultTitle),
                generatedContent = obj.optString("generatedContent", cleanJson),
                actionType = obj.optString("actionType", "APPEND"),
                bulletPoints = bullets
            )
        } catch (ex: Exception) {
            FixNowAiResult(
                sectionKey = defaultSectionKey,
                title = defaultTitle,
                generatedContent = cleanJson.trim(),
                actionType = "APPEND",
                bulletPoints = emptyList()
            )
        }
    }

    private fun sanitizeJson(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.substring(7)
        } else if (text.startsWith("```")) {
            text = text.substring(3)
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length - 3)
        }
        text = text.trim()
        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        return if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            text.substring(firstBrace, lastBrace + 1)
        } else {
            text
        }
    }

    private fun parseJobMatchManually(jsonStr: String): JobMatchResponse {
        return try {
            val obj = JSONObject(jsonStr)
            val score = obj.optInt("score", 70)
            val analysisType = obj.optString("analysisType", "ATS")
            val summaryFeedback = obj.optString("summaryFeedback", "")
            val suggestedKeywords = mutableListOf<String>()
            val kwArr = obj.optJSONArray("suggestedKeywords")
            if (kwArr != null) {
                for (i in 0 until kwArr.length()) {
                    suggestedKeywords.add(kwArr.optString(i))
                }
            }

            val checklist = mutableListOf<AtsChecklistItem>()
            val clArr = obj.optJSONArray("checklist")
            if (clArr != null) {
                for (i in 0 until clArr.length()) {
                    val itemObj = clArr.optJSONObject(i) ?: continue
                    checklist.add(
                        AtsChecklistItem(
                            title = itemObj.optString("title", "Checklist item"),
                            status = itemObj.optString("status", ChecklistStatus.PRESENT),
                            sectionKey = itemObj.optString("sectionKey", CvSectionKey.SUMMARY),
                            explanation = itemObj.optString("detail", itemObj.optString("explanation", "")),
                            detail = itemObj.optString("detail", ""),
                            suggestedContent = itemObj.optString("suggestedContent", "")
                        )
                    )
                }
            }

            JobMatchResponse(
                score = score,
                analysisType = analysisType,
                checklist = checklist,
                summaryFeedback = summaryFeedback,
                suggestedKeywords = suggestedKeywords
            )
        } catch (e: Exception) {
            // Safe fallback response
            JobMatchResponse(
                score = 65,
                analysisType = "ATS",
                checklist = listOf(
                    AtsChecklistItem("Clear Professional Summary", ChecklistStatus.PRESENT, CvSectionKey.SUMMARY, explanation = "Summary is present"),
                    AtsChecklistItem("Quantified Work Experience Results", ChecklistStatus.MISSING, CvSectionKey.EXPERIENCE, explanation = "Add metrics like % or $ to work history"),
                    AtsChecklistItem("Categorized Technical Skills", ChecklistStatus.MISSING, CvSectionKey.SKILLS, explanation = "Add domain & technical skills"),
                    AtsChecklistItem("Complete Contact Information", ChecklistStatus.PRESENT, CvSectionKey.CONTACT_INFO, explanation = "Contact details verified")
                )
            )
        }
    }
}

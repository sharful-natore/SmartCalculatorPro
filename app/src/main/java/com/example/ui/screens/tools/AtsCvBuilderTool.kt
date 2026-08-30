package com.example.ui.screens.tools

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

// ================= DATA MODELS FOR ATS CV =================

enum class CvTemplateStyle(
    val titleEn: String,
    val titleBn: String,
    val description: String,
    val primaryColorHex: Int,
    val isTwoColumn: Boolean = false
) {
    CLASSIC_CORPORATE(
        "Classic Corporate",
        "ক্লাসিক কর্পোরেট (ATS সেরা)",
        "ব্যাংক, করপোরেট ও সরকারি চাকরির জন্য ১০০% টেক্সট-ফ্রেন্ডলি ঐতিহ্যবাহী লেআউট",
        AndroidColor.parseColor("#1A365D")
    ),
    MODERN_MINIMALIST(
        "Modern Minimalist",
        "মডার্ন মিনিমালিস্ট",
        "আইটি, সফটওয়্যার ও আধুনিক কোম্পানির জন্য পরিচ্ছন্ন ও নজরকাড়া ডিজাইন",
        AndroidColor.parseColor("#0D9488")
    ),
    FRESHER_ENTRY(
        "Fresher & Academic",
        "ফ্রেশার ও একাডেমিক",
        "যাদের অভিজ্ঞতা কম, তাদের প্রজেক্ট, স্কিল ও শিক্ষা প্রদর্শনকারী ডিজাইন",
        AndroidColor.parseColor("#4F46E5")
    ),
    EXECUTIVE_TWO_COLUMN(
        "Executive Two-Column",
        "এক্সিকিউティブ ২-কলাম",
        "অভিজ্ঞ পেশাজীবীদের জন্য সাইডবারসহ স্টাইলিশ ও তথ্যবহুল লেআউট",
        AndroidColor.parseColor("#1E293B"),
        isTwoColumn = true
    ),
    CREATIVE_MARKETING(
        "Creative Marketing",
        "ক্রিয়েটিভ মার্কেটিং",
        "মার্কেটিং, ব্র্যান্ডিং ও ক্রিয়েটিভ রোলের জন্য আকর্ষণীয় লাল ও ডার্ক অ্যাকসেন্ট",
        AndroidColor.parseColor("#B91C1C")
    ),
    BUSINESS_ANALYST_MBA(
        "MBA Strategic Analyst",
        "এমবিএ স্ট্র্যাটেজিক অ্যানালিস্ট",
        "ব্যবস্থাপনা, অ্যানালিটিক্স ও এক্সিকিউটিভ রোলের জন্য রয়্যাল ব্লু অ্যাকসেন্ট",
        AndroidColor.parseColor("#0369A1")
    ),
    CLEAN_TECH_STARTUP(
        "Clean Tech Startup",
        "ক্লিন টেক স্টার্টআপ",
        "আধুনিক স্টার্টআপের জন্য গাঢ় সবুজ রঙ ও জ্যামিতিক নকশার প্রফেশনাল মিক্স",
        AndroidColor.parseColor("#0F766E")
    )
}

val HexagonShape = object : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            val w = size.width
            val h = size.height
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.75f)
            lineTo(w * 0.5f, h)
            lineTo(0f, h * 0.75f)
            lineTo(0f, h * 0.25f)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

data class CvExperienceItem(
    val id: String = UUID.randomUUID().toString(),
    val company: String = "",
    val role: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrent: Boolean = false,
    val description: String = ""
)

data class CvEducationItem(
    val id: String = UUID.randomUUID().toString(),
    val degree: String = "",
    val institution: String = "",
    val passingYear: String = "",
    val result: String = ""
)

data class CvSkillItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val level: String = "Proficient"
)

data class CvProjectItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val link: String = ""
)

data class CvData(
    val id: String = UUID.randomUUID().toString(),
    val profileLabel: String = "MBA Analyst Profile",
    val fullName: String = "Md. Shariful Islam",
    val jobTitle: String = "Management Graduate & Business Analyst",
    val email: String = "shariful.mba@example.com",
    val phone: String = "+880 1711-223344",
    val address: String = "Dhaka, Bangladesh",
    val linkedin: String = "linkedin.com/in/shariful-mba",
    val githubOrPortfolio: String = "portfolio.shariful.com",
    val summary: String = "A highly analytical and result-oriented Management MBA Graduate with a strong foundation in business strategy, market research, and financial analysis. Proven expertise in leveraging data-driven insights to optimize business operations, increase project efficiency, and drive marketing campaigns. Excellent communication, team leadership, and strategic planning skills,",
    val photoBase64: String = "",
    val photoShape: String = "Circle", // Circle, Rounded, Square, Hexagon
    val photoScale: Float = 1.0f,
    val photoOffsetX: Float = 0f,
    val photoOffsetY: Float = 0f,
    val experiences: List<CvExperienceItem> = listOf(
        CvExperienceItem(
            company = "Apex Business Solutions",
            role = "Associate Business Analyst",
            startDate = "Jan 2024",
            endDate = "Present",
            isCurrent = true,
            description = "• Conducted market research and competitor analysis to assist in strategic decision-making.\n• Developed interactive business performance dashboards, increasing analytical clarity by 25%.\n• Managed cross-functional project coordination across marketing and product design teams."
        ),
        CvExperienceItem(
            company = "Strategic Marketing Group",
            role = "Management Trainee (Intern)",
            startDate = "Jun 2023",
            endDate = "Dec 2023",
            isCurrent = false,
            description = "• Collaborated on brand optimization and promotional campaigns for FMCG sector clients.\n• Assisted in preparation of financial models and feasibility reports for new product launches."
        )
    ),
    val educations: List<CvEducationItem> = listOf(
        CvEducationItem(
            degree = "Master of Business Administration (MBA)",
            institution = "Institute of Business Administration (IBA), University of Dhaka",
            passingYear = "2023",
            result = "CGPA: 3.82 / 4.00"
        ),
        CvEducationItem(
            degree = "Bachelor of Business Administration (BBA) in Management",
            institution = "University of Dhaka",
            passingYear = "2021",
            result = "CGPA: 3.78 / 4.00"
        )
    ),
    val skills: List<CvSkillItem> = listOf(
        CvSkillItem(name = "Business Strategy & Planning", level = "Expert"),
        CvSkillItem(name = "Market Analysis", level = "Expert"),
        CvSkillItem(name = "Financial Modeling", level = "Proficient"),
        CvSkillItem(name = "Agile Project Management", level = "Proficient"),
        CvSkillItem(name = "Data Visualization & PowerBI", level = "Proficient"),
        CvSkillItem(name = "ATS Resume Optimization", level = "Expert"),
        CvSkillItem(name = "Team Leadership & Collaboration", level = "Expert")
    ),
    val projects: List<CvProjectItem> = listOf(
        CvProjectItem(
            title = "E-Commerce Market Feasibility study",
            description = "Conducted an in-depth financial feasibility and customer acquisition study for a logistics startup.\nDesigned a forecasting model with 92% planning accuracy.",
            link = "portfolio.shariful.com/projects/feasibility"
        )
    ),
    val languages: String = "English (Professional), Bengali (Native)",
    val templateStyle: CvTemplateStyle = CvTemplateStyle.CLASSIC_CORPORATE,
    val targetJobCircular: String = ""
)

// SharedPreferences Multi-Profile Helpers
private const val CV_PREFS_NAME = "ats_cv_builder_multi_prefs_v2"
private const val CV_PROFILES_LIST_KEY = "saved_cv_profiles_json_list"
private const val ACTIVE_PROFILE_ID_KEY = "active_cv_profile_uuid"

private fun getSeedProfilesList(): List<CvData> {
    val defaultMba = CvData(
        id = "profile_mba_shariful",
        profileLabel = "Md. Shariful Islam (Primary MBA)",
        fullName = "Md. Shariful Islam",
        jobTitle = "Management Graduate & Business Analyst"
    )
    val marketingPersona = CvData(
        id = "profile_marketing_specialist",
        profileLabel = "Marketing Specialist Persona",
        fullName = "Md. Shariful Islam",
        jobTitle = "Brand & Strategic Marketing Manager",
        email = "shariful.marketing@example.com",
        summary = "Dynamic MBA Marketing Graduate with a passion for brand equity, tactical campaign planning, and consumer market intelligence. Experienced in drafting digital marketing roadmaps that successfully accelerated customer engagement metrics by 30%.",
        experiences = listOf(
            CvExperienceItem(
                company = "Global Consumer Brands Ltd",
                role = "Strategic Brand Associate",
                startDate = "Oct 2023",
                endDate = "Present",
                isCurrent = true,
                description = "• Guided tactical market research to redefine target consumer demographics.\n• Coordinated digital performance campaigns across social media channels, scaling reach by 45%.\n• Managed advertising agency partnerships to align with brand aesthetic standards."
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Brand Identity & Positioning", level = "Expert"),
            CvSkillItem(name = "Digital Campaign Planning", level = "Expert"),
            CvSkillItem(name = "Market Research & Analysis", level = "Expert"),
            CvSkillItem(name = "Content Strategy", level = "Proficient")
        ),
        templateStyle = CvTemplateStyle.CREATIVE_MARKETING
    )
    val techProjectPersona = CvData(
        id = "profile_tech_pm",
        profileLabel = "Strategic Project Manager Persona",
        fullName = "Md. Shariful Islam",
        jobTitle = "Strategic Project Manager (MBA)",
        email = "shariful.pm@example.com",
        summary = "Analytical MBA Graduate specialized in Operations and Tech Management. Proven capability to streamline product life-cycles, implement agile frameworks, and structure quantitative feasibility dashboards.",
        experiences = listOf(
            CvExperienceItem(
                company = "NextGen Tech Solutions",
                role = "Agile Project Coordinator",
                startDate = "Nov 2023",
                endDate = "Present",
                isCurrent = true,
                description = "• Formulated agile project delivery frameworks, boosting operational sprint efficiency by 18%.\n• Designed and synchronized dynamic client progress dashboards using Jira and PowerBI."
            )
        ),
        skills = listOf(
            CvSkillItem(name = "Agile / Scrum Methodologies", level = "Expert"),
            CvSkillItem(name = "Operations Planning", level = "Expert"),
            CvSkillItem(name = "Jira & PowerBI", level = "Expert"),
            CvSkillItem(name = "Risk Mitigation", level = "Proficient")
        ),
        templateStyle = CvTemplateStyle.CLEAN_TECH_STARTUP
    )
    return listOf(defaultMba, marketingPersona, techProjectPersona)
}

private fun saveAllCvProfiles(context: Context, profiles: List<CvData>) {
    try {
        val prefs = context.getSharedPreferences(CV_PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        profiles.forEach { profile ->
            val obj = JSONObject().apply {
                put("id", profile.id)
                put("profileLabel", profile.profileLabel)
                put("fullName", profile.fullName)
                put("jobTitle", profile.jobTitle)
                put("email", profile.email)
                put("phone", profile.phone)
                put("address", profile.address)
                put("linkedin", profile.linkedin)
                put("githubOrPortfolio", profile.githubOrPortfolio)
                put("summary", profile.summary)
                put("languages", profile.languages)
                put("templateStyle", profile.templateStyle.name)
                put("targetJobCircular", profile.targetJobCircular)
                put("photoBase64", profile.photoBase64)
                put("photoShape", profile.photoShape)
                put("photoScale", profile.photoScale.toDouble())
                put("photoOffsetX", profile.photoOffsetX.toDouble())
                put("photoOffsetY", profile.photoOffsetY.toDouble())

                val expArr = JSONArray()
                profile.experiences.forEach { exp ->
                    expArr.put(JSONObject().apply {
                        put("id", exp.id)
                        put("company", exp.company)
                        put("role", exp.role)
                        put("startDate", exp.startDate)
                        put("endDate", exp.endDate)
                        put("isCurrent", exp.isCurrent)
                        put("description", exp.description)
                    })
                }
                put("experiences", expArr)

                val eduArr = JSONArray()
                profile.educations.forEach { edu ->
                    eduArr.put(JSONObject().apply {
                        put("id", edu.id)
                        put("degree", edu.degree)
                        put("institution", edu.institution)
                        put("passingYear", edu.passingYear)
                        put("result", edu.result)
                    })
                }
                put("educations", eduArr)

                val skillArr = JSONArray()
                profile.skills.forEach { sk ->
                    skillArr.put(JSONObject().apply {
                        put("id", sk.id)
                        put("name", sk.name)
                        put("level", sk.level)
                    })
                }
                put("skills", skillArr)

                val projArr = JSONArray()
                profile.projects.forEach { pr ->
                    projArr.put(JSONObject().apply {
                        put("id", pr.id)
                        put("title", pr.title)
                        put("description", pr.description)
                        put("link", pr.link)
                    })
                }
                put("projects", projArr)
            }
            arr.put(obj)
        }
        prefs.edit().putString(CV_PROFILES_LIST_KEY, arr.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun loadAllCvProfiles(context: Context): List<CvData> {
    try {
        val prefs = context.getSharedPreferences(CV_PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(CV_PROFILES_LIST_KEY, null)
        if (raw.isNullOrBlank()) {
            val defaults = getSeedProfilesList()
            saveAllCvProfiles(context, defaults)
            return defaults
        }
        val arr = JSONArray(raw)
        val list = mutableListOf<CvData>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val expList = mutableListOf<CvExperienceItem>()
            obj.optJSONArray("experiences")?.let { expArr ->
                for (j in 0 until expArr.length()) {
                    val expObj = expArr.getJSONObject(j)
                    expList.add(
                        CvExperienceItem(
                            id = expObj.optString("id", UUID.randomUUID().toString()),
                            company = expObj.optString("company"),
                            role = expObj.optString("role"),
                            startDate = expObj.optString("startDate"),
                            endDate = expObj.optString("endDate"),
                            isCurrent = expObj.optBoolean("isCurrent"),
                            description = expObj.optString("description")
                        )
                    )
                }
            }

            val eduList = mutableListOf<CvEducationItem>()
            obj.optJSONArray("educations")?.let { eduArr ->
                for (j in 0 until eduArr.length()) {
                    val eduObj = eduArr.getJSONObject(j)
                    eduList.add(
                        CvEducationItem(
                            id = eduObj.optString("id", UUID.randomUUID().toString()),
                            degree = eduObj.optString("degree"),
                            institution = eduObj.optString("institution"),
                            passingYear = eduObj.optString("passingYear"),
                            result = eduObj.optString("result")
                        )
                    )
                }
            }

            val skillList = mutableListOf<CvSkillItem>()
            obj.optJSONArray("skills")?.let { skArr ->
                for (j in 0 until skArr.length()) {
                    val skObj = skArr.getJSONObject(j)
                    skillList.add(
                        CvSkillItem(
                            id = skObj.optString("id", UUID.randomUUID().toString()),
                            name = skObj.optString("name"),
                            level = skObj.optString("level", "Proficient")
                        )
                    )
                }
            }

            val projList = mutableListOf<CvProjectItem>()
            obj.optJSONArray("projects")?.let { prArr ->
                for (j in 0 until prArr.length()) {
                    val prObj = prArr.getJSONObject(j)
                    projList.add(
                        CvProjectItem(
                            id = prObj.optString("id", UUID.randomUUID().toString()),
                            title = prObj.optString("title"),
                            description = prObj.optString("description"),
                            link = prObj.optString("link")
                        )
                    )
                }
            }

            val styleStr = obj.optString("templateStyle", CvTemplateStyle.CLASSIC_CORPORATE.name)
            val style = try { CvTemplateStyle.valueOf(styleStr) } catch (_: Exception) { CvTemplateStyle.CLASSIC_CORPORATE }

            list.add(
                CvData(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    profileLabel = obj.optString("profileLabel", "Unnamed Resume"),
                    fullName = obj.optString("fullName", "Md. Shariful Islam"),
                    jobTitle = obj.optString("jobTitle", "Management Graduate & Business Analyst"),
                    email = obj.optString("email", "shariful.mba@example.com"),
                    phone = obj.optString("phone", "+880 1711-223344"),
                    address = obj.optString("address", "Dhaka, Bangladesh"),
                    linkedin = obj.optString("linkedin", "linkedin.com/in/shariful-mba"),
                    githubOrPortfolio = obj.optString("githubOrPortfolio", "portfolio.shariful.com"),
                    summary = obj.optString("summary", ""),
                    experiences = expList,
                    educations = eduList,
                    skills = skillList,
                    projects = projList,
                    languages = obj.optString("languages", "English (Fluent), Bengali (Native)"),
                    templateStyle = style,
                    targetJobCircular = obj.optString("targetJobCircular", ""),
                    photoBase64 = obj.optString("photoBase64", ""),
                    photoShape = obj.optString("photoShape", "Circle"),
                    photoScale = obj.optDouble("photoScale", 1.0).toFloat(),
                    photoOffsetX = obj.optDouble("photoOffsetX", 0.0).toFloat(),
                    photoOffsetY = obj.optDouble("photoOffsetY", 0.0).toFloat()
                )
            )
        }
        return list
    } catch (e: Exception) {
        e.printStackTrace()
        return getSeedProfilesList()
    }
}

private fun loadActiveProfileId(context: Context): String {
    val prefs = context.getSharedPreferences(CV_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(ACTIVE_PROFILE_ID_KEY, "") ?: ""
}

private fun saveActiveProfileId(context: Context, id: String) {
    val prefs = context.getSharedPreferences(CV_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(ACTIVE_PROFILE_ID_KEY, id).apply()
}

// ================= GEMINI AI MULTI-MODAL CALL =================

private suspend fun callGeminiAiMultiModal(
    prompt: String,
    systemInstruction: String,
    imageBytes: ByteArray? = null,
    mimeType: String = "image/jpeg"
): String = withContext(Dispatchers.IO) {
    val apiKey = com.example.BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
        throw IllegalStateException("API Key is missing or not configured in Secrets panel.")
    }

    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
    conn.doOutput = true
    conn.connectTimeout = 20000
    conn.readTimeout = 25000

    val requestJson = JSONObject().apply {
        val contentsArr = JSONArray()
        contentsArr.put(JSONObject().apply {
            val partsArr = JSONArray()
            partsArr.put(JSONObject().put("text", prompt))

            if (imageBytes != null) {
                val base64Str = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                partsArr.put(JSONObject().apply {
                    put("inline_data", JSONObject().apply {
                        put("mime_type", mimeType)
                        put("data", base64Str)
                    })
                })
            }
            put("parts", partsArr)
        })
        put("contents", contentsArr)

        if (systemInstruction.isNotBlank()) {
            put("system_instruction", JSONObject().apply {
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemInstruction))
                put("parts", sysParts)
            })
        }
    }

    conn.outputStream.use { os ->
        os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
    }

    val responseCode = conn.responseCode
    if (responseCode == 200) {
        val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
        val resObj = JSONObject(responseStr)
        val candidates = resObj.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val cand = candidates.getJSONObject(0)
            val content = cand.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return@withContext parts.getJSONObject(0).optString("text", "")
            }
        }
        throw IllegalStateException("No contents generated from AI model.")
    } else {
        val errStr = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        throw IllegalStateException("Gemini AI API Error ($responseCode): $errStr")
    }
}

// ================= DYNAMIC HIGH-QUALITY PDF GENERATOR =================

private fun generateCvPdfFile(context: Context, data: CvData): File {
    val pdfDocument = PdfDocument()

    // Standard A4: 595 x 842 pt
    val pageWidth = 595
    val pageHeight = 842

    var currentPageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas

    val margin = 36f
    val contentWidth = pageWidth - (margin * 2)
    var currentY = margin

    val pdfStyle = data.templateStyle
    val primaryColor = pdfStyle.primaryColorHex
    val textColor = AndroidColor.parseColor("#1E293B")
    val subTextColor = AndroidColor.parseColor("#475569")
    val mutedLineColor = AndroidColor.parseColor("#E2E8F0")

    val titlePaint = TextPaint().apply {
        isAntiAlias = true
        color = if (pdfStyle == CvTemplateStyle.CREATIVE_MARKETING) AndroidColor.WHITE else primaryColor
        textSize = 21f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val subtitlePaint = TextPaint().apply {
        isAntiAlias = true
        color = if (pdfStyle == CvTemplateStyle.CREATIVE_MARKETING) AndroidColor.WHITE else primaryColor
        textSize = 12.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val contactPaint = TextPaint().apply {
        isAntiAlias = true
        color = if (pdfStyle == CvTemplateStyle.CREATIVE_MARKETING) AndroidColor.WHITE else subTextColor
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    val sectionHeaderPaint = TextPaint().apply {
        isAntiAlias = true
        color = primaryColor
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val bodyPaint = TextPaint().apply {
        isAntiAlias = true
        color = textColor
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    val bodyBoldPaint = TextPaint().apply {
        isAntiAlias = true
        color = textColor
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val linePaint = Paint().apply {
        color = primaryColor
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    fun checkAndAddNewPage(neededHeight: Float) {
        if (currentY + neededHeight > pageHeight - margin) {
            pdfDocument.finishPage(page)
            currentPageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            currentY = margin
        }
    }

    // --- Template Specific Drawing Helper ---
    if (pdfStyle == CvTemplateStyle.CREATIVE_MARKETING) {
        // Draw Solid Banner Background
        val bannerPaint = Paint().apply {
            color = AndroidColor.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 135f, bannerPaint)

        val accentBarPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 135f, pageWidth.toFloat(), 140f, accentBarPaint)

        currentY = 24f
    }

    // Profile Photo Decode
    val photoBitmap = if (data.photoBase64.isNotBlank()) {
        try {
            val decodedBytes = Base64.decode(data.photoBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }
    val hasPhoto = photoBitmap != null
    val photoSize = 65f
    val photoMargin = 12f
    val textWidth = if (hasPhoto) (contentWidth - photoSize - photoMargin) else contentWidth

    // Draw Profile Photo on PDF if it exists
    if (photoBitmap != null) {
        val px = pageWidth - margin - photoSize
        val py = if (pdfStyle == CvTemplateStyle.CREATIVE_MARKETING) 25f else currentY

        canvas.save()
        val path = android.graphics.Path()
        when (data.photoShape) {
            "Circle" -> {
                path.addCircle(px + photoSize / 2f, py + photoSize / 2f, photoSize / 2f, android.graphics.Path.Direction.CW)
                canvas.clipPath(path)
            }
            "Rounded" -> {
                val rect = android.graphics.RectF(px, py, px + photoSize, py + photoSize)
                path.addRoundRect(rect, 8f, 8f, android.graphics.Path.Direction.CW)
                canvas.clipPath(path)
            }
            "Hexagon" -> {
                path.moveTo(px + photoSize * 0.5f, py)
                path.lineTo(px + photoSize, py + photoSize * 0.25f)
                path.lineTo(px + photoSize, py + photoSize * 0.75f)
                path.lineTo(px + photoSize * 0.5f, py + photoSize)
                path.lineTo(px, py + photoSize * 0.75f)
                path.lineTo(px, py + photoSize * 0.25f)
                path.close()
                canvas.clipPath(path)
            }
            else -> {
                val rect = android.graphics.RectF(px, py, px + photoSize, py + photoSize)
                path.addRect(rect, android.graphics.Path.Direction.CW)
                canvas.clipPath(path)
            }
        }

        canvas.save()
        // Clip to square bounding box
        val clipBox = android.graphics.Path()
        clipBox.addRect(android.graphics.RectF(px, py, px + photoSize, py + photoSize), android.graphics.Path.Direction.CW)
        canvas.clipPath(clipBox)

        // Apply interactive scale and drag offsets
        val cx = px + photoSize / 2f
        val cy = py + photoSize / 2f
        canvas.translate(cx, cy)
        canvas.scale(data.photoScale, data.photoScale)
        // Convert screen scale offsets to PDF points
        canvas.translate(data.photoOffsetX / 3.2f, data.photoOffsetY / 3.2f)
        canvas.translate(-cx, -cy)

        val dstRect = android.graphics.RectF(px, py, px + photoSize, py + photoSize)
        canvas.drawBitmap(photoBitmap, null, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))
        canvas.restore() // restore transform
        canvas.restore() // restore shape clipping

        // Draw elegant thin border around the shape
        val borderPaint = Paint().apply {
            color = if (pdfStyle == CvTemplateStyle.CREATIVE_MARKETING) AndroidColor.WHITE else primaryColor
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        when (data.photoShape) {
            "Circle" -> canvas.drawCircle(px + photoSize / 2f, py + photoSize / 2f, photoSize / 2f, borderPaint)
            "Rounded" -> canvas.drawRoundRect(android.graphics.RectF(px, py, px + photoSize, py + photoSize), 8f, 8f, borderPaint)
            "Hexagon" -> {
                val bPath = android.graphics.Path().apply {
                    moveTo(px + photoSize * 0.5f, py)
                    lineTo(px + photoSize, py + photoSize * 0.25f)
                    lineTo(px + photoSize, py + photoSize * 0.75f)
                    lineTo(px + photoSize * 0.5f, py + photoSize)
                    lineTo(px, py + photoSize * 0.75f)
                    lineTo(px, py + photoSize * 0.25f)
                    close()
                }
                canvas.drawPath(bPath, borderPaint)
            }
            else -> canvas.drawRect(android.graphics.RectF(px, py, px + photoSize, py + photoSize), borderPaint)
        }
    }

    // Name
    if (data.fullName.isNotBlank()) {
        val nameLayout = StaticLayout.Builder.obtain(data.fullName, 0, data.fullName.length, titlePaint, textWidth.toInt()).build()
        checkAndAddNewPage(nameLayout.height.toFloat() + 5f)
        canvas.save()
        canvas.translate(margin, currentY)
        nameLayout.draw(canvas)
        canvas.restore()
        currentY += nameLayout.height + 4
    }

    // Designation
    if (data.jobTitle.isNotBlank()) {
        val designationLayout = StaticLayout.Builder.obtain(data.jobTitle, 0, data.jobTitle.length, subtitlePaint, textWidth.toInt()).build()
        checkAndAddNewPage(designationLayout.height.toFloat() + 5f)
        canvas.save()
        canvas.translate(margin, currentY)
        designationLayout.draw(canvas)
        canvas.restore()
        currentY += designationLayout.height + 6
    }

    // Contacts block
    val contacts = mutableListOf<String>()
    if (data.phone.isNotBlank()) contacts.add("📞 ${data.phone}")
    if (data.email.isNotBlank()) contacts.add("✉ ${data.email}")
    if (data.address.isNotBlank()) contacts.add("📍 ${data.address}")
    if (data.linkedin.isNotBlank()) contacts.add("🔗 ${data.linkedin}")
    if (data.githubOrPortfolio.isNotBlank()) contacts.add("🌐 ${data.githubOrPortfolio}")

    if (contacts.isNotEmpty()) {
        val contactStr = contacts.joinToString("  |  ")
        val contactLayout = StaticLayout.Builder.obtain(contactStr, 0, contactStr.length, contactPaint, textWidth.toInt()).build()
        checkAndAddNewPage(contactLayout.height.toFloat() + 6f)
        canvas.save()
        canvas.translate(margin, currentY)
        contactLayout.draw(canvas)
        canvas.restore()
        currentY += contactLayout.height + 10
    }

    if (pdfStyle == CvTemplateStyle.CREATIVE_MARKETING) {
        currentY = 155f // Advance past the banner setup
    } else {
        // Simple elegant top border
        checkAndAddNewPage(10f)
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, linePaint)
        currentY += 12f
    }

    fun drawSectionHeader(title: String) {
        checkAndAddNewPage(32f)
        canvas.drawText(title.uppercase(), margin, currentY + 12f, sectionHeaderPaint)
        currentY += 16f

        // Customizable line under headers
        val headerBarPaint = Paint().apply {
            color = if (pdfStyle == CvTemplateStyle.CLASSIC_CORPORATE || pdfStyle == CvTemplateStyle.BUSINESS_ANALYST_MBA) primaryColor else mutedLineColor
            strokeWidth = if (pdfStyle == CvTemplateStyle.BUSINESS_ANALYST_MBA) 2.5f else 1f
        }
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, headerBarPaint)

        // MBA double line effect
        if (pdfStyle == CvTemplateStyle.BUSINESS_ANALYST_MBA) {
            canvas.drawLine(margin, currentY + 3f, pageWidth - margin, currentY + 3f, Paint().apply {
                color = AndroidColor.parseColor("#93C5FD")
                strokeWidth = 1f
            })
            currentY += 8f
        } else {
            currentY += 8f
        }
    }

    // Professional Summary
    if (data.summary.isNotBlank()) {
        drawSectionHeader("PROFESSIONAL SUMMARY")
        val summaryLayout = StaticLayout.Builder.obtain(data.summary, 0, data.summary.length, bodyPaint, contentWidth.toInt()).build()
        checkAndAddNewPage(summaryLayout.height.toFloat() + 10)
        canvas.save()
        canvas.translate(margin, currentY)
        summaryLayout.draw(canvas)
        canvas.restore()
        currentY += summaryLayout.height + 14
    }

    // Work Experience
    if (data.experiences.isNotEmpty()) {
        drawSectionHeader("WORK EXPERIENCE")
        data.experiences.forEach { exp ->
            if (exp.role.isNotBlank() || exp.company.isNotBlank()) {
                val titleLine = "${exp.role}${if (exp.role.isNotBlank() && exp.company.isNotBlank()) " — " else ""}${exp.company}"
                val dateLine = "${exp.startDate}${if (exp.startDate.isNotBlank() && exp.endDate.isNotBlank()) " – " else ""}${if (exp.isCurrent) "Present" else exp.endDate}"

                val titleLayout = StaticLayout.Builder.obtain(titleLine, 0, titleLine.length, bodyBoldPaint, (contentWidth * 0.7f).toInt()).build()
                val dateLayout = StaticLayout.Builder.obtain(dateLine, 0, dateLine.length, contactPaint, (contentWidth * 0.3f).toInt()).setAlignment(Layout.Alignment.ALIGN_OPPOSITE).build()

                val itemHeight = maxOf(titleLayout.height, dateLayout.height).toFloat() + 4f
                checkAndAddNewPage(itemHeight)

                canvas.save()
                canvas.translate(margin, currentY)
                titleLayout.draw(canvas)
                canvas.restore()

                canvas.save()
                canvas.translate(margin + contentWidth * 0.7f, currentY)
                dateLayout.draw(canvas)
                canvas.restore()

                currentY += itemHeight

                if (exp.description.isNotBlank()) {
                    val descLayout = StaticLayout.Builder.obtain(exp.description, 0, exp.description.length, bodyPaint, contentWidth.toInt()).build()
                    checkAndAddNewPage(descLayout.height.toFloat() + 6)
                    canvas.save()
                    canvas.translate(margin, currentY)
                    descLayout.draw(canvas)
                    canvas.restore()
                    currentY += descLayout.height + 10
                } else {
                    currentY += 6
                }
            }
        }
    }

    // Education
    if (data.educations.isNotEmpty()) {
        drawSectionHeader("EDUCATION")
        data.educations.forEach { edu ->
            if (edu.degree.isNotBlank() || edu.institution.isNotBlank()) {
                val titleLine = "${edu.degree}${if (edu.degree.isNotBlank() && edu.institution.isNotBlank()) ", " else ""}${edu.institution}"
                val detailsLine = "${edu.passingYear}${if (edu.passingYear.isNotBlank() && edu.result.isNotBlank()) " | " else ""}${edu.result}"

                val titleLayout = StaticLayout.Builder.obtain(titleLine, 0, titleLine.length, bodyBoldPaint, contentWidth.toInt()).build()
                checkAndAddNewPage(titleLayout.height.toFloat() + 2)
                canvas.save()
                canvas.translate(margin, currentY)
                titleLayout.draw(canvas)
                canvas.restore()
                currentY += titleLayout.height + 2

                if (detailsLine.isNotBlank()) {
                    val detailsLayout = StaticLayout.Builder.obtain(detailsLine, 0, detailsLine.length, contactPaint, contentWidth.toInt()).build()
                    checkAndAddNewPage(detailsLayout.height.toFloat() + 6)
                    canvas.save()
                    canvas.translate(margin, currentY)
                    detailsLayout.draw(canvas)
                    canvas.restore()
                    currentY += detailsLayout.height + 8
                }
            }
        }
    }

    // Skills
    if (data.skills.isNotEmpty()) {
        drawSectionHeader("CORE SKILLS & TECHNOLOGIES")
        val skillsStr = data.skills.joinToString("  •  ") { "${it.name}${if (it.level.isNotBlank()) " (${it.level})" else ""}" }
        val skillsLayout = StaticLayout.Builder.obtain(skillsStr, 0, skillsStr.length, bodyPaint, contentWidth.toInt()).build()
        checkAndAddNewPage(skillsLayout.height.toFloat() + 10)
        canvas.save()
        canvas.translate(margin, currentY)
        skillsLayout.draw(canvas)
        canvas.restore()
        currentY += skillsLayout.height + 12
    }

    // Projects
    if (data.projects.isNotEmpty()) {
        drawSectionHeader("KEY PROJECTS")
        data.projects.forEach { proj ->
            if (proj.title.isNotBlank()) {
                val projHeader = "${proj.title}${if (proj.link.isNotBlank()) " — (${proj.link})" else ""}"
                val titleLayout = StaticLayout.Builder.obtain(projHeader, 0, projHeader.length, bodyBoldPaint, contentWidth.toInt()).build()
                checkAndAddNewPage(titleLayout.height.toFloat() + 2)
                canvas.save()
                canvas.translate(margin, currentY)
                titleLayout.draw(canvas)
                canvas.restore()
                currentY += titleLayout.height + 2

                if (proj.description.isNotBlank()) {
                    val descLayout = StaticLayout.Builder.obtain(proj.description, 0, proj.description.length, bodyPaint, contentWidth.toInt()).build()
                    checkAndAddNewPage(descLayout.height.toFloat() + 6)
                    canvas.save()
                    canvas.translate(margin, currentY)
                    descLayout.draw(canvas)
                    canvas.restore()
                    currentY += descLayout.height + 8
                }
            }
        }
    }

    // Languages
    if (data.languages.isNotBlank()) {
        drawSectionHeader("LANGUAGES")
        val langLayout = StaticLayout.Builder.obtain(data.languages, 0, data.languages.length, bodyPaint, contentWidth.toInt()).build()
        checkAndAddNewPage(langLayout.height.toFloat() + 10)
        canvas.save()
        canvas.translate(margin, currentY)
        langLayout.draw(canvas)
        canvas.restore()
        currentY += langLayout.height + 10
    }

    pdfDocument.finishPage(page)

    val file = File(context.cacheDir, "ATS_CV_${System.currentTimeMillis()}.pdf")
    val fos = FileOutputStream(file)
    pdfDocument.writeTo(fos)
    pdfDocument.close()
    fos.close()

    return file
}

private fun renderPdfPageToBitmap(pdfFile: File, pageIndex: Int = 0): Bitmap? {
    return try {
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        if (renderer.pageCount > pageIndex) {
            val page = renderer.openPage(pageIndex)
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(AndroidColor.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            pfd.close()
            bitmap
        } else {
            renderer.close()
            pfd.close()
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ================= MAIN TOOL COMPOSABLE =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtsCvBuilderTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    // Load multi profiles
    var profilesList by remember { mutableStateOf(loadAllCvProfiles(context)) }
    var activeProfileId by remember { mutableStateOf(loadActiveProfileId(context)) }

    // Resolve active CvData
    if (activeProfileId.isBlank() || profilesList.none { it.id == activeProfileId }) {
        activeProfileId = profilesList.firstOrNull()?.id ?: ""
    }

    val activeCvDataIndex = profilesList.indexOfFirst { it.id == activeProfileId }.let { if (it == -1) 0 else it }
    var cvData by remember(activeProfileId, profilesList) {
        mutableStateOf(profilesList.getOrNull(activeCvDataIndex) ?: CvData())
    }

    // States for screens
    var selectedTab by remember { mutableStateOf(0) } // 0: Profile/Personas, 1: Experience, 2: Education, 3: Job Match, 4: Preview

    var isAiLoading by remember { mutableStateOf(false) }
    var aiLoadingMessage by remember { mutableStateOf("") }

    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfPreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Save changes and update cache
    fun updateCvDataState(updated: CvData) {
        cvData = updated
        val updatedList = profilesList.toMutableList()
        val idx = updatedList.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            updatedList[idx] = updated
        } else {
            updatedList.add(updated)
        }
        profilesList = updatedList
        saveAllCvProfiles(context, updatedList)
    }

    // Auto-render live PDF vector preview on changes
    LaunchedEffect(cvData) {
        withContext(Dispatchers.IO) {
            val file = generateCvPdfFile(context, cvData)
            val bitmap = renderPdfPageToBitmap(file, 0)
            withContext(Dispatchers.Main) {
                generatedPdfFile = file
                pdfPreviewBitmap = bitmap
            }
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Beautiful tab controls (Removal of double clipart/emojis, using proper Icons)
            val tabs = if (isBn) {
                listOf("প্রোফাইল", "অভিজ্ঞতা", "শিক্ষা ও স্কিল", "জব ম্যাচ", "প্রিভিউ")
            } else {
                listOf("Profile", "Experience", "Education", "Job Match", "Preview")
            }

            val tabIcons = listOf(
                Icons.Default.Person,
                Icons.Default.Work,
                Icons.Default.School,
                Icons.Default.AutoAwesome,
                Icons.Default.Visibility
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = themeColors.cardBg,
                contentColor = themeColors.displayText,
                edgePadding = 12.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = null,
                                tint = if (selectedTab == index) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.5f)
                            )
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.7f)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // RENDER ACTIVE SCREEN
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> ProfileAndPersonasTab(
                        cvData = cvData,
                        profilesList = profilesList,
                        onCvDataChange = { updateCvDataState(it) },
                        onActiveProfileSelected = { id ->
                            activeProfileId = id
                            saveActiveProfileId(context, id)
                        },
                        onAddNewProfile = { name ->
                            val label = if (name.isNotBlank()) name else "New MBA Draft ${profilesList.size + 1}"
                            val newProfile = CvData(
                                id = UUID.randomUUID().toString(),
                                profileLabel = label,
                                fullName = "Md. Shariful Islam",
                                jobTitle = "Management Graduate & Business Analyst"
                            )
                            val updatedList = profilesList.toMutableList()
                            updatedList.add(newProfile)
                            profilesList = updatedList
                            saveAllCvProfiles(context, updatedList)
                            activeProfileId = newProfile.id
                            saveActiveProfileId(context, newProfile.id)
                            showToast(if (isBn) "নতুন ড্রাফট তৈরি হয়েছে!" else "New CV draft profile created!")
                        },
                        onDeleteProfile = { idToDelete ->
                            if (profilesList.size <= 1) {
                                showToast(if (isBn) "অন্তত একটি ড্রাফট প্রোফাইল থাকতে হবে!" else "Must keep at least one profile draft!")
                            } else {
                                val updatedList = profilesList.filter { it.id != idToDelete }
                                profilesList = updatedList
                                saveAllCvProfiles(context, updatedList)
                                if (activeProfileId == idToDelete) {
                                    activeProfileId = updatedList.first().id
                                    saveActiveProfileId(context, activeProfileId)
                                }
                                showToast(if (isBn) "ড্রাফটটি মুছে ফেলা হয়েছে!" else "CV draft deleted successfully!")
                            }
                        },
                        themeColors = themeColors,
                        isBn = isBn,
                        onOpenPdfInViewer = { profileToOpen ->
                            scope.launch {
                                val file = generateCvPdfFile(context, profileToOpen)
                                withContext(Dispatchers.Main) {
                                    viewModel.pdfReaderInitialUri = Uri.fromFile(file)
                                    viewModel.pdfReaderInitialName = "CV_${profileToOpen.fullName.replace(" ", "_")}.pdf"
                                    viewModel.selectedToolType = com.example.data.model.ToolType.PDF_READER
                                }
                            }
                        },
                        onGenerateSummaryAi = {
                            if (cvData.jobTitle.isBlank()) {
                                showToast(if (isBn) "অনুগ্রহ করে পদবীটি টাইপ করুন" else "Please fill target designation first")
                                return@ProfileAndPersonasTab
                            }
                            isAiLoading = true
                            aiLoadingMessage = if (isBn) "✨ জেমিনি এআই আপনার ক্যারিয়ারের সারসংক্ষেপ ও অর্জনের ওপর ভিত্তি করে কন্টেন্ট রি-রাইট করছে..." else "✨ Gemini AI is designing an MBA specialized summary..."
                            scope.launch {
                                try {
                                    // Take into account already entered information or brief description
                                    val currentNotes = cvData.summary
                                    val currentSkills = cvData.skills.joinToString { it.name }
                                    val currentExperiences = cvData.experiences.joinToString { "${it.role} at ${it.company}" }

                                    val prompt = "The candidate is Md. Shariful Islam, a Management/MBA Graduate. Target job title: '${cvData.jobTitle}'. " +
                                            "Current partial profile summary is: '$currentNotes'. " +
                                            "Key skills: '$currentSkills'. Work experiences: '$currentExperiences'. " +
                                            "Task: Write a highly specialized, modern, professional, ATS-optimized executive summary of exactly 3 sentences. Enhance and expand any notes they have provided."
                                    val sys = "You are an executive CV writer. Return ONLY the written professional summary text, with no preamble, quotes or markdown."

                                    val aiResult = callGeminiAiMultiModal(prompt, sys)
                                    updateCvDataState(cvData.copy(summary = aiResult.trim()))
                                    showToast(if (isBn) "সফলভাবে কাস্টমাইজড সামারি জেনারেট হয়েছে!" else "Aesthetic summary tailored successfully!")
                                } catch (e: Exception) {
                                    showToast("AI Error: ${e.message}")
                                } finally {
                                    isAiLoading = false
                                }
                            }
                        }
                    )

                    1 -> ExperienceTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        themeColors = themeColors,
                        isBn = isBn,
                        onEnhanceBulletAi = { idx ->
                            val exp = cvData.experiences.getOrNull(idx) ?: return@ExperienceTab
                            if (exp.role.isBlank()) {
                                showToast(if (isBn) "অনুগ্রহ করে পদবী উল্লেখ করুন" else "Please enter role title first")
                                return@ExperienceTab
                            }
                            isAiLoading = true
                            aiLoadingMessage = if (isBn) "✨ জেমিনি এআই আপনার কাজের বিবরণীটিকে আধুনিক ATS ও এক্সিকিউটিভ শব্দ দিয়ে সাজাচ্ছে..." else "✨ Gemini is phrasing ATS-driven professional action bullet points..."
                            scope.launch {
                                try {
                                    val prompt = "Role: '${exp.role}' at '${exp.company}'. Current raw description/bullet points: '${exp.description}'. Rewrite into exactly 3 robust, results-focused executive action-verb bullets. Use metrics/percentages simulation if appropriate."
                                    val sys = "You are an expert recruiter. Return ONLY 3 bullet lines starting with bullet symbol (•), no other conversation."
                                    val aiResult = callGeminiAiMultiModal(prompt, sys)
                                    val updatedExpList = cvData.experiences.toMutableList()
                                    updatedExpList[idx] = exp.copy(description = aiResult.trim())
                                    updateCvDataState(cvData.copy(experiences = updatedExpList))
                                    showToast(if (isBn) "বুলেট পয়েন্ট নিখুঁতভাবে টিউন হয়েছে!" else "Bullet points tailored successfully!")
                                } catch (e: Exception) {
                                    showToast("AI Error: ${e.message}")
                                } finally {
                                    isAiLoading = false
                                }
                            }
                        }
                    )

                    2 -> EducationAndSkillsTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        themeColors = themeColors,
                        isBn = isBn
                    )

                    3 -> AiJobCircularMatchTab(
                        cvData = cvData,
                        onCvDataChange = { updateCvDataState(it) },
                        themeColors = themeColors,
                        isBn = isBn,
                        onMatchCircularAi = { circularText, imageBytes, imageMime ->
                            if (circularText.isBlank() && imageBytes == null) {
                                showToast(if (isBn) "অনুগ্রহ করে সার্কুলার টেক্সট দিন অথবা ছবি আপলোড করুন!" else "Please provide circular text or pick an image!")
                                return@AiJobCircularMatchTab
                            }
                            isAiLoading = true
                            aiLoadingMessage = if (isBn) "✨ জেমিনি এআই সার্কুলারের টেক্সট ও ছবি বিশ্লেষণ করছে এবং আপনার সিভি অটো-টিউন করছে..." else "✨ Gemini AI is analyzing the circular image and text to tailor your resume..."
                            scope.launch {
                                try {
                                    val promptBuilder = StringBuilder()
                                    promptBuilder.append("Target job circular context:\n")
                                    if (circularText.isNotBlank()) {
                                        promptBuilder.append("Circular Text: $circularText\n")
                                    }
                                    if (imageBytes != null) {
                                        promptBuilder.append("[An image of the job circular is also attached below for you to read details from]\n")
                                    }

                                    promptBuilder.append("\nCandidate MBA Profile Summary: ${cvData.summary}\n")
                                    promptBuilder.append("Skills: ${cvData.skills.joinToString { it.name }}\n")
                                    promptBuilder.append("Current Experiences: ${cvData.experiences.joinToString { "${it.role} at ${it.company}" }}\n")
                                    promptBuilder.append("\nTask:\n")
                                    promptBuilder.append("1. Rewrite their summary to perfectly align with the circular, emphasizing skills they have that match the circular.\n")
                                    promptBuilder.append("2. Suggest 5 additional key skills derived from the circular that match an MBA/Management candidate.\n")
                                    promptBuilder.append("Return ONLY a JSON formatted block inside bracket structures, with keys 'tailoredSummary' (string) and 'newSkills' (array of strings).")

                                    val sys = "You are a senior ATS Match consultant. Return ONLY valid JSON: {\"tailoredSummary\": \"...\", \"newSkills\": [\"Skill1\", \"Skill2\"]}"

                                    val rawResult = callGeminiAiMultiModal(
                                        prompt = promptBuilder.toString(),
                                        systemInstruction = sys,
                                        imageBytes = imageBytes,
                                        mimeType = imageMime
                                    )

                                    val cleanJsonStr = rawResult.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
                                    val jsonObj = JSONObject(cleanJsonStr)
                                    val tailoredSummary = jsonObj.optString("tailoredSummary", cvData.summary)

                                    val updatedSkills = cvData.skills.toMutableList()
                                    jsonObj.optJSONArray("newSkills")?.let { arr ->
                                        for (i in 0 until arr.length()) {
                                            val skName = arr.getString(i)
                                            if (updatedSkills.none { it.name.equals(skName, ignoreCase = true) }) {
                                                updatedSkills.add(CvSkillItem(name = skName, level = "Proficient"))
                                            }
                                        }
                                    }

                                    updateCvDataState(cvData.copy(
                                        summary = tailoredSummary,
                                        skills = updatedSkills,
                                        targetJobCircular = circularText
                                    ))
                                    showToast(if (isBn) "অভিনন্দন! সার্কুলার অনুযায়ী আপনার সিভি টিউন সম্পন্ন হয়েছে।" else "CV tailored perfectly to circular!")
                                    selectedTab = 4 // switch to preview
                                } catch (e: Exception) {
                                    showToast("AI Match Error: ${e.message}")
                                } finally {
                                    isAiLoading = false
                                }
                            }
                        }
                    )

                    4 -> PreviewAndExportTab(
                        cvData = cvData,
                        pdfFile = generatedPdfFile,
                        pdfBitmap = pdfPreviewBitmap,
                        themeColors = themeColors,
                        isBn = isBn,
                        onTemplateChange = { newStyle ->
                            updateCvDataState(cvData.copy(templateStyle = newStyle))
                        },
                        onDownloadPdf = {
                            val file = generatedPdfFile ?: return@PreviewAndExportTab
                            try {
                                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                val destFile = File(downloadsDir, "CV_${cvData.fullName.replace(" ", "_")}_ATS.pdf")
                                file.copyTo(destFile, overwrite = true)
                                showToast(if (isBn) "পিডিএফ ডাউনলোড ফোল্ডারে সেভ হয়েছে!" else "PDF saved to Downloads folder!")
                            } catch (e: Exception) {
                                showToast("Saved to App Storage: ${file.name}")
                            }
                        },
                        onSharePdf = {
                            val file = generatedPdfFile ?: return@PreviewAndExportTab
                            try {
                                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, if (isBn) "সিভি পিডিএফ শেয়ার করুন" else "Share CV PDF"))
                            } catch (e: Exception) {
                                showToast("Share error: ${e.message}")
                            }
                        },
                        onOpenPdfInAppViewer = {
                            val file = generatedPdfFile ?: return@PreviewAndExportTab
                            viewModel.pdfReaderInitialUri = Uri.fromFile(file)
                            viewModel.pdfReaderInitialName = "CV_${cvData.fullName.replace(" ", "_")}.pdf"
                            viewModel.selectedToolType = com.example.data.model.ToolType.PDF_READER
                        }
                    )
                }
            }
        }

        // Elegant AI Loading Overlay Window
        if (isAiLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.56f))
                    .pointerInput(Unit) { detectTapGestures { } },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = themeColors.cardBg,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = themeColors.buttonEqualBg, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = aiLoadingMessage,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ================= TAB 0: PROFILE AND PERSONA DETAILS =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileAndPersonasTab(
    cvData: CvData,
    profilesList: List<CvData>,
    onCvDataChange: (CvData) -> Unit,
    onActiveProfileSelected: (String) -> Unit,
    onAddNewProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onOpenPdfInViewer: (CvData) -> Unit,
    onGenerateSummaryAi: () -> Unit
) {
    val scrollState = rememberScrollState()
    var newProfileName by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // --- MULTI-PROFILE MANAGER HEADER ---
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionCardHeader(
                        title = if (isBn) "সিভি ড্রাফট ও প্রোফাইল সমূহ" else "CV Draft Profiles & Personas",
                        icon = Icons.Default.FolderOpen,
                        themeColors = themeColors
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isBn) "নতুন ড্রাফট" else "New Draft", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Profiles Horizontal selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(profilesList) { profile ->
                        val isActive = profile.id == cvData.id
                        Surface(
                            onClick = { onActiveProfileSelected(profile.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isActive) themeColors.buttonEqualBg.copy(alpha = 0.15f) else themeColors.background,
                            border = BorderStroke(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.width(220.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = profile.profileLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = themeColors.displayText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (profilesList.size > 1) {
                                        IconButton(
                                            onClick = { onDeleteProfile(profile.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = profile.jobTitle,
                                    fontSize = 10.sp,
                                    color = themeColors.displayText.copy(alpha = 0.65f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    // Quick launch native PDF Viewer button
                                    IconButton(
                                        onClick = { onOpenPdfInViewer(profile) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(themeColors.buttonEqualBg.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "Quick view", tint = themeColors.buttonEqualBg, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- ACTIVE PROFILE FIELDS ---
        SectionCardHeader(
            title = if (isBn) "ব্যক্তিগত ও যোগাযোগ তথ্য" else "Personal & Contact Details",
            icon = Icons.Default.Person,
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- PROFILE PICTURE CUSTOMIZER ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isBn) "সিভি প্রোফাইল ছবি এবং ক্রপ সেটিংস" else "Profile Picture & Interactive Crop",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = themeColors.displayText
                )
                Text(
                    text = if (isBn) "সিভির জন্য ছবি আপলোড করে ইচ্ছামত গোল, গোল-কোণা, বা ষড়ভুজ শেইপ সিলেক্ট করুন এবং ড্র্যাগ বা জুম করে পজিশন ঠিক করুন" else "Upload a photo for your CV, pick your favorite shape, and interactively zoom and drag to adjust the crop position",
                    fontSize = 10.5.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                val context = LocalContext.current
                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()

                            if (originalBitmap != null) {
                                // Crop to center square default to keep scale handling simple
                                val minDim = minOf(originalBitmap.width, originalBitmap.height)
                                val cx = (originalBitmap.width - minDim) / 2
                                val cy = (originalBitmap.height - minDim) / 2
                                val squareBmp = Bitmap.createBitmap(originalBitmap, cx, cy, minDim, minDim)
                                val scaledBmp = Bitmap.createScaledBitmap(squareBmp, 300, 300, true)

                                val baos = java.io.ByteArrayOutputStream()
                                scaledBmp.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                                val base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                                onCvDataChange(cvData.copy(
                                    photoBase64 = base64,
                                    photoScale = 1.0f,
                                    photoOffsetX = 0f,
                                    photoOffsetY = 0f
                                ))
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error loading image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo Preview with Drag Gesture Crop
                    val decodedBmp = remember(cvData.photoBase64) {
                        if (cvData.photoBase64.isNotBlank()) {
                            try {
                                val bytes = Base64.decode(cvData.photoBase64, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (_: Exception) {
                                null
                            }
                        } else {
                            null
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Gray.copy(alpha = 0.1f), shape = when (cvData.photoShape) {
                                "Circle" -> CircleShape
                                "Rounded" -> RoundedCornerShape(12.dp)
                                "Hexagon" -> HexagonShape
                                else -> androidx.compose.ui.graphics.RectangleShape
                            })
                            .border(1.5.dp, themeColors.buttonEqualBg, shape = when (cvData.photoShape) {
                                "Circle" -> CircleShape
                                "Rounded" -> RoundedCornerShape(12.dp)
                                "Hexagon" -> HexagonShape
                                else -> androidx.compose.ui.graphics.RectangleShape
                            })
                            .clip(shape = when (cvData.photoShape) {
                                "Circle" -> CircleShape
                                "Rounded" -> RoundedCornerShape(12.dp)
                                "Hexagon" -> HexagonShape
                                else -> androidx.compose.ui.graphics.RectangleShape
                            })
                            .pointerInput(cvData.photoScale) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    // Adjust drag offsets based on zoom scale
                                    val bound = 150f * cvData.photoScale
                                    val newX = (cvData.photoOffsetX + dragAmount.x).coerceIn(-bound, bound)
                                    val newY = (cvData.photoOffsetY + dragAmount.y).coerceIn(-bound, bound)
                                    onCvDataChange(cvData.copy(photoOffsetX = newX, photoOffsetY = newY))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (decodedBmp != null) {
                            Image(
                                bitmap = decodedBmp.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = cvData.photoScale,
                                        scaleY = cvData.photoScale,
                                        translationX = cvData.photoOffsetX,
                                        translationY = cvData.photoOffsetY
                                    )
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = themeColors.displayText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isBn) "ছবি দিন" else "No Photo",
                                    fontSize = 10.sp,
                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Action buttons & configurations
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isBn) "গ্যালারি" else "Gallery", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (cvData.photoBase64.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        onCvDataChange(cvData.copy(
                                            photoBase64 = "",
                                            photoScale = 1.0f,
                                            photoOffsetX = 0f,
                                            photoOffsetY = 0f
                                        ))
                                    },
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.6f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isBn) "বাদ দিন" else "Remove", color = Color.Red.copy(alpha = 0.7f), fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Shape Chips Selection
                        Text(
                            text = if (isBn) "ছবির শেইপ নির্ধারণ করুন:" else "Select Photo Shape:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            val shapes = listOf("Circle", "Rounded", "Hexagon", "Square")
                            shapes.forEach { shapeName ->
                                val isSelected = cvData.photoShape == shapeName
                                val label = when (shapeName) {
                                    "Circle" -> if (isBn) "বৃত্তাকার" else "Circle"
                                    "Rounded" -> if (isBn) "কোণ গোল" else "Rounded"
                                    "Hexagon" -> if (isBn) "ষড়ভুজ" else "Hexagon"
                                    else -> if (isBn) "চারকোণা" else "Square"
                                }
                                Surface(
                                    onClick = { onCvDataChange(cvData.copy(photoShape = shapeName)) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) themeColors.buttonEqualBg else themeColors.background,
                                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else themeColors.displayText.copy(alpha = 0.15f)),
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 9.5.sp,
                                        color = if (isSelected) Color.White else themeColors.displayText,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (cvData.photoBase64.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Zoom Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = themeColors.displayText.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "জুম সাইজ (স্লাইডার):" else "Interactive Zoom Scale:",
                            fontSize = 10.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = cvData.photoScale,
                            onValueChange = { onCvDataChange(cvData.copy(photoScale = it)) },
                            valueRange = 1.0f..3.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = themeColors.buttonEqualBg,
                                activeTrackColor = themeColors.buttonEqualBg,
                                inactiveTrackColor = themeColors.displayText.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${"%.1f".format(cvData.photoScale)}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }

                    // Reset positioning trigger
                    if (cvData.photoOffsetX != 0f || cvData.photoOffsetY != 0f || cvData.photoScale != 1.0f) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    onCvDataChange(cvData.copy(
                                        photoScale = 1.0f,
                                        photoOffsetX = 0f,
                                        photoOffsetY = 0f
                                    ))
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(
                                    text = if (isBn) "পজিশন রিসেট" else "Reset Crop & Zoom",
                                    fontSize = 10.sp,
                                    color = themeColors.buttonEqualBg,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        CvCustomTextField(
            label = if (isBn) "ড্রাফট লেবেল / নাম (Draft Label)" else "Draft Profile Label",
            value = cvData.profileLabel,
            onValueChange = { onCvDataChange(cvData.copy(profileLabel = it)) },
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        CvCustomTextField(
            label = if (isBn) "পূর্ণ নাম (Full Name)" else "Full Name",
            value = cvData.fullName,
            onValueChange = { onCvDataChange(cvData.copy(fullName = it)) },
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        CvCustomTextField(
            label = if (isBn) "পদবী (Target Designation / Job Title)" else "Job Title / Target Designation",
            value = cvData.jobTitle,
            onValueChange = { onCvDataChange(cvData.copy(jobTitle = it)) },
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "ইমেইল (Email)" else "Email Address",
                    value = cvData.email,
                    onValueChange = { onCvDataChange(cvData.copy(email = it)) },
                    themeColors = themeColors
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "ফোন নম্বর (Phone)" else "Phone Number",
                    value = cvData.phone,
                    onValueChange = { onCvDataChange(cvData.copy(phone = it)) },
                    themeColors = themeColors
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CvCustomTextField(
            label = if (isBn) "ঠিকানা (Address)" else "City & Country / Address",
            value = cvData.address,
            onValueChange = { onCvDataChange(cvData.copy(address = it)) },
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "লিঙ্কডইন প্রোফাইল" else "LinkedIn Profile URL",
                    value = cvData.linkedin,
                    onValueChange = { onCvDataChange(cvData.copy(linkedin = it)) },
                    themeColors = themeColors
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                CvCustomTextField(
                    label = if (isBn) "গিথাব / পোর্টফোলিও" else "GitHub / Portfolio URL",
                    value = cvData.githubOrPortfolio,
                    onValueChange = { onCvDataChange(cvData.copy(githubOrPortfolio = it)) },
                    themeColors = themeColors
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SUMMARY SECTION WITH ATS AUTO-GENERATE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCardHeader(
                title = if (isBn) "প্রফেশনাল সামারি (Professional Summary)" else "Professional Summary",
                icon = Icons.Default.Description,
                themeColors = themeColors
            )
            Button(
                onClick = onGenerateSummaryAi,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                // Explicit high contrast White color applied to resolve the black button issue
                Text(text = if (isBn) "AI দিয়ে লিখুন" else "Generate with AI", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = cvData.summary,
            onValueChange = { onCvDataChange(cvData.copy(summary = it)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text(text = if (isBn) "আপনার ক্যারিয়ারের সামারি লিখুন অথবা এআই দিয়ে রি-রাইট করুন..." else "Write notes here & use Gemini AI to weave them into a professional corporate summary...", fontSize = 12.5.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg,
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            )
        )
    }

    // Add Profile Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = if (isBn) "নতুন সিভি ড্রাফট তৈরি করুন" else "Create New CV Draft Profile") },
            text = {
                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = { newProfileName = it },
                    label = { Text(text = if (isBn) "ড্রাফটের নাম" else "Profile / Resume Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddNewProfile(newProfileName)
                        newProfileName = ""
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(text = if (isBn) "তৈরি করুন" else "Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.buttonEqualBg)
                }
            }
        )
    }
}

// ================= TAB 1: WORK EXPERIENCES =================

@Composable
private fun ExperienceTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onEnhanceBulletAi: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCardHeader(
                title = if (isBn) "কাজের অভিজ্ঞতা (${cvData.experiences.size})" else "Work Experience (${cvData.experiences.size})",
                icon = Icons.Default.Work,
                themeColors = themeColors
            )
            OutlinedButton(
                onClick = {
                    val newList = cvData.experiences.toMutableList()
                    newList.add(CvExperienceItem())
                    onCvDataChange(cvData.copy(experiences = newList))
                },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "নতুন যোগ করুন" else "Add Position", color = themeColors.buttonEqualBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        cvData.experiences.forEachIndexed { index, exp ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "অভিজ্ঞতা #${index + 1}" else "Position #${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = themeColors.buttonEqualBg
                        )
                        IconButton(
                            onClick = {
                                val newList = cvData.experiences.toMutableList()
                                newList.removeAt(index)
                                onCvDataChange(cvData.copy(experiences = newList))
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            CvCustomTextField(
                                label = if (isBn) "পদবী (Role Title)" else "Role / Designation",
                                value = exp.role,
                                onValueChange = { r ->
                                    val newList = cvData.experiences.toMutableList()
                                    newList[index] = exp.copy(role = r)
                                    onCvDataChange(cvData.copy(experiences = newList))
                                },
                                themeColors = themeColors
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            CvCustomTextField(
                                label = if (isBn) "কোম্পানির নাম" else "Company Name",
                                value = exp.company,
                                onValueChange = { c ->
                                    val newList = cvData.experiences.toMutableList()
                                    newList[index] = exp.copy(company = c)
                                    onCvDataChange(cvData.copy(experiences = newList))
                                },
                                themeColors = themeColors
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            CvCustomTextField(
                                label = if (isBn) "শুরুর তারিখ" else "Start Date (e.g. Jan 2023)",
                                value = exp.startDate,
                                onValueChange = { s ->
                                    val newList = cvData.experiences.toMutableList()
                                    newList[index] = exp.copy(startDate = s)
                                    onCvDataChange(cvData.copy(experiences = newList))
                                },
                                themeColors = themeColors
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            CvCustomTextField(
                                label = if (isBn) "শেষের তারিখ" else "End Date (or Present)",
                                value = exp.endDate,
                                onValueChange = { e ->
                                    val newList = cvData.experiences.toMutableList()
                                    newList[index] = exp.copy(endDate = e)
                                    onCvDataChange(cvData.copy(experiences = newList))
                                },
                                themeColors = themeColors
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "দায়িত্ব ও অর্জনসমূহ (Bullet points)" else "Responsibilities & Key Achievements",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )
                        TextButton(
                            onClick = { onEnhanceBulletAi(index) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isBn) "AI রি-রাইট করুন" else "AI Enhance Bullet", color = themeColors.buttonEqualBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = exp.description,
                        onValueChange = { d ->
                            val newList = cvData.experiences.toMutableList()
                            newList[index] = exp.copy(description = d)
                            onCvDataChange(cvData.copy(experiences = newList))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text(text = "• Conducted business evaluations...\n• Tailored campaign KPIs...\n• Coordinated teams...", fontSize = 11.5.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                            focusedContainerColor = themeColors.cardBg,
                            unfocusedContainerColor = themeColors.cardBg,
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText
                        )
                    )
                }
            }
        }
    }
}

// ================= TAB 2: EDUCATION AND SKILLS =================

@Composable
private fun EducationAndSkillsTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // EDUCATION CARD BLOCK
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCardHeader(
                title = if (isBn) "শিক্ষাগত যোগ্যতা" else "Education Details",
                icon = Icons.Default.School,
                themeColors = themeColors
            )
            OutlinedButton(
                onClick = {
                    val newList = cvData.educations.toMutableList()
                    newList.add(CvEducationItem())
                    onCvDataChange(cvData.copy(educations = newList))
                },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "ডিগ্রি যোগ করুন" else "Add Degree", color = themeColors.buttonEqualBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        cvData.educations.forEachIndexed { index, edu ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Education #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = themeColors.buttonEqualBg)
                        IconButton(
                            onClick = {
                                val newList = cvData.educations.toMutableList()
                                newList.removeAt(index)
                                onCvDataChange(cvData.copy(educations = newList))
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    CvCustomTextField(
                        label = if (isBn) "ডিগ্রির নাম (Degree / Certificate)" else "Degree Title (e.g., MBA in Management)",
                        value = edu.degree,
                        onValueChange = { d ->
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(degree = d)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    CvCustomTextField(
                        label = if (isBn) "শিক্ষা প্রতিষ্ঠান (Institution / University)" else "University / Institution Name",
                        value = edu.institution,
                        onValueChange = { i ->
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(institution = i)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            CvCustomTextField(
                                label = if (isBn) "পাসের বছর" else "Passing Year",
                                value = edu.passingYear,
                                onValueChange = { y ->
                                    val newList = cvData.educations.toMutableList()
                                    newList[index] = edu.copy(passingYear = y)
                                    onCvDataChange(cvData.copy(educations = newList))
                                },
                                themeColors = themeColors
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            CvCustomTextField(
                                label = if (isBn) "সিজিপিএ (CGPA / Result)" else "CGPA / Academic Result",
                                value = edu.result,
                                onValueChange = { r ->
                                    val newList = cvData.educations.toMutableList()
                                    newList[index] = edu.copy(result = r)
                                    onCvDataChange(cvData.copy(educations = newList))
                                },
                                themeColors = themeColors
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SKILLS SECTION BLOCK
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCardHeader(
                title = if (isBn) "প্রফেশনাল স্কিলস (${cvData.skills.size})" else "Skills & Business Tools (${cvData.skills.size})",
                icon = Icons.Default.Code,
                themeColors = themeColors
            )
            OutlinedButton(
                onClick = {
                    val newList = cvData.skills.toMutableList()
                    newList.add(CvSkillItem())
                    onCvDataChange(cvData.copy(skills = newList))
                },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "স্কিল যোগ করুন" else "Add Skill", color = themeColors.buttonEqualBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        cvData.skills.forEachIndexed { index, sk ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CvCustomTextField(
                        label = if (isBn) "স্কিলের নাম" else "Skill / Domain (e.g. Strategic Planning)",
                        value = sk.name,
                        onValueChange = { sn ->
                            val newList = cvData.skills.toMutableList()
                            newList[index] = sk.copy(name = sn)
                            onCvDataChange(cvData.copy(skills = newList))
                        },
                        themeColors = themeColors
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val newList = cvData.skills.toMutableList()
                        newList.removeAt(index)
                        onCvDataChange(cvData.copy(skills = newList))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CvCustomTextField(
            label = if (isBn) "ভাষাগত দক্ষতা (Languages)" else "Language Fluency",
            value = cvData.languages,
            onValueChange = { onCvDataChange(cvData.copy(languages = it)) },
            themeColors = themeColors
        )
    }
}

// ================= TAB 3: AI JOB MATCH WITH IMAGE UPLOAD =================

@Composable
private fun AiJobCircularMatchTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onMatchCircularAi: (String, ByteArray?, String) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var circularInputText by remember { mutableStateOf(cvData.targetJobCircular) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                context.contentResolver.openInputStream(uri).use { stream ->
                    selectedImageBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        SectionCardHeader(
            title = if (isBn) "জব সার্কুলার অনুযায়ী এআই কাস্টমাইজেশন" else "AI Job Circular Match & Tayloring",
            icon = Icons.Default.AutoAwesome,
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isBn)
                "যে চাকরির জন্য আবেদন করতে চান তার সার্কুলার টেক্সট পেস্ট করুন অথবা ক্যামেরা/গ্যালারি থেকে সরাসরি সার্কুলার ইমেজের একটি ছবি দিন! জেমিনি এআই সার্কুলারটি বিশ্লেষণ করে আপনার সিভির সামারি ও কি-ওয়ার্ড কাস্টমাইজ করবে।"
            else
                "Paste the job circular text OR select an image of the circular from your gallery. Gemini Multi-modal AI will analyze the requirements from both text/image to optimize your summary and key skills.",
            fontSize = 12.sp,
            color = themeColors.displayText.copy(alpha = 0.75f),
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Text input area
        OutlinedTextField(
            value = circularInputText,
            onValueChange = { circularInputText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            placeholder = { Text(text = if (isBn) "জব ডেসক্রিপশন পেস্ট করুন..." else "Paste Job circular / description requirements text here...", fontSize = 12.5.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg,
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Image input options
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isBn) "সার্কুলার ছবি ইনপুট করুন" else "Upload Circular Image (Camera/Gallery)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isBn) "গ্যালারি থেকে ছবি নিন" else "Choose Image", color = themeColors.buttonEqualBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (selectedImageUri != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                selectedImageUri = null
                                selectedImageBitmap = null
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Clear image", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }

                if (selectedImageBitmap != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(
                        bitmap = selectedImageBitmap!!.asImageBitmap(),
                        contentDescription = "Circular thumbnail",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                var imageBytes: ByteArray? = null
                var mimeType = "image/jpeg"
                if (selectedImageUri != null) {
                    try {
                        context.contentResolver.openInputStream(selectedImageUri!!).use { stream ->
                            imageBytes = stream?.readBytes()
                        }
                        mimeType = context.contentResolver.getType(selectedImageUri!!) ?: "image/jpeg"
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                onMatchCircularAi(circularInputText, imageBytes, mimeType)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            // Explicit White text color to ensure contrast
            Text(
                text = if (isBn) "✨ জেমিনি এআই দিয়ে সিভি টিউন করুন" else "✨ Tailor Resume with Gemini AI",
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ================= TAB 4: PREVIEW & EXPORT =================

@Composable
private fun PreviewAndExportTab(
    cvData: CvData,
    pdfFile: File?,
    pdfBitmap: Bitmap?,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onTemplateChange: (CvTemplateStyle) -> Unit,
    onDownloadPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onOpenPdfInAppViewer: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // Template Selector Grid (At least 5-7 templates)
        SectionCardHeader(
            title = if (isBn) "সিভি টেমপ্লেট নির্বাচন" else "Select ATS Resume Template",
            icon = Icons.Default.Style,
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal elegant picker for 7 templates
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CvTemplateStyle.values()) { style ->
                val isSelected = cvData.templateStyle == style
                Surface(
                    onClick = { onTemplateChange(style) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) themeColors.buttonEqualBg.copy(alpha = 0.15f) else themeColors.cardBg,
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.width(190.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onTemplateChange(style) },
                                colors = RadioButtonDefaults.colors(selectedColor = themeColors.buttonEqualBg)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) style.titleBn else style.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = themeColors.displayText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = style.description,
                            fontSize = 9.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f),
                            lineHeight = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- NEW ACTION: OPEN IN NATIVE APP PDF READER ---
        Button(
            onClick = onOpenPdfInAppViewer,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
        ) {
            Icon(imageVector = Icons.Default.ChromeReaderMode, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            // High contrast white text resolving black issue
            Text(
                text = if (isBn) "পিডিএফ রিডার দিয়ে প্রিভিউ করুন" else "Preview in Native PDF Viewer App",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // HD Export Action Buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onDownloadPdf,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                // High contrast white text resolving black issue
                Text(text = if (isBn) "HD PDF ডাউনলোড" else "Download PDF", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onSharePdf,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, themeColors.buttonEqualBg)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (isBn) "পিডিএফ শেয়ার" else "Share PDF", color = themeColors.buttonEqualBg, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Vector PDF Live Canvas Screen
        SectionCardHeader(
            title = if (isBn) "লাইভ ভেক্টর প্রিভিউ (A4 Page 1)" else "Live Vector Preview (A4 Page 1)",
            icon = Icons.Default.Visibility,
            themeColors = themeColors
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            if (pdfBitmap != null) {
                Image(
                    bitmap = pdfBitmap.asImageBitmap(),
                    contentDescription = "A4 Page Vector Preview",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = themeColors.buttonEqualBg)
                }
            }
        }
    }
}

// ================= SHARABLE REUSABLE HELPER UI =================

@Composable
private fun SectionCardHeader(
    title: String,
    icon: ImageVector,
    themeColors: CalculatorThemeColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = themeColors.buttonEqualBg,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )
    }
}

@Composable
private fun CvCustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    themeColors: CalculatorThemeColors
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = themeColors.displayText.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedContainerColor = themeColors.cardBg,
                unfocusedContainerColor = themeColors.cardBg,
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            )
        )
    }
}

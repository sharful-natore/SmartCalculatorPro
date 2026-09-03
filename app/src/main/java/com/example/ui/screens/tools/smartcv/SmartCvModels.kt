package com.example.ui.screens.tools.smartcv

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.screens.tools.CvData
import com.example.ui.screens.tools.CvEducationItem
import com.example.ui.screens.tools.CvExperienceItem
import com.example.ui.screens.tools.CvProjectItem
import com.example.ui.screens.tools.CvSkillItem
import com.example.ui.screens.tools.CvCustomSectionItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

enum class SmartCvStudioTab(
    val titleEn: String,
    val titleBn: String,
    val icon: ImageVector
) {
    CANVAS("Canvas Preview", "ক্যানভাস প্রিভিউ", Icons.Default.Visibility),
    STRUCTURE("Structure & Blocks", "মডুলার স্ট্রাকচার", Icons.Default.ViewStream),
    STYLE("Style & Themes", "স্টাইল ও থিম", Icons.Default.Palette),
    AI_COPILOT("AI Co-pilot", "এআই কো-পাইলট", Icons.Default.AutoAwesome)
}

enum class SmartCvTemplate(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val taglineEn: String,
    val taglineBn: String,
    val atsScoreBadge: String
) {
    HARVARD_CLASSIC(
        id = "HARVARD_CLASSIC",
        nameEn = "Harvard Ivy ATS",
        nameBn = "হার্ভার্ড ক্লাসিক ATS",
        taglineEn = "100% text-first, pristine typography, top-tier corporate standard",
        taglineBn = "১০০% টেক্সট-ফার্স্ট, ট্র্যাডিশনাল ক্লিন সেরিফ, সর্বোচ্চ ATS স্কোর",
        atsScoreBadge = "99% ATS Pass"
    ),
    MODERN_TECH(
        id = "MODERN_TECH",
        nameEn = "Modern Tech Minimalist",
        nameBn = "মডার্ন টেক মিনিমালিস্ট",
        taglineEn = "Clean accent borders, contemporary badges, software & startup ready",
        taglineBn = "সফট অ্যাকসেন্ট বার, স্লিক সেকশন ডিভাইডার ও টেক-ফ্রেন্ডলি ফন্ট",
        atsScoreBadge = "95% ATS Pass"
    ),
    EXECUTIVE_SPLIT(
        id = "EXECUTIVE_SPLIT",
        nameEn = "Executive Two-Column",
        nameBn = "এক্সিকিউটিভ স্প্লিট (২ কলাম)",
        taglineEn = "Left tinted sidebar for contact & skills, right column for experience",
        taglineBn = "লেফট সাইডবার প্যানেল ও রাইট কলাম এক্সপেরিয়েন্স ফোকাস",
        atsScoreBadge = "92% ATS Pass"
    ),
    COMPACT_ENTRY(
        id = "COMPACT_ENTRY",
        nameEn = "Compact Fresher Frame",
        nameBn = "কমপ্যাক্ট ফ্রেশার ও স্টুডেন্ট",
        taglineEn = "Boxed header card, space-optimized for early career & graduates",
        taglineBn = "বর্ডারড হেডার কার্ড, স্পেস অপটিমাইজড ফ্রেশার ও স্টুডেন্ট লেআউট",
        atsScoreBadge = "94% ATS Pass"
    )
}

enum class SmartAccentColor(
    val labelEn: String,
    val labelBn: String,
    val hex: String,
    val composeColor: Color
) {
    CLASSIC_NAVY("Classic Navy", "ক্লাসিক নেভি", "#1E3A8A", Color(0xFF1E3A8A)),
    EXECUTIVE_SLATE("Executive Slate", "স্লেট চারকোল", "#1E293B", Color(0xFF1E293B)),
    EMERALD_FOREST("Emerald Forest", "এমারেল্ড গ্রিন", "#065F46", Color(0xFF065F46)),
    DEEP_BURGUNDY("Deep Burgundy", "বার্গেন্ডি", "#831843", Color(0xFF831843)),
    TECH_TEAL("Tech Teal", "মডার্ন টিল", "#0F766E", Color(0xFF0F766E)),
    INDIGO_ROYAL("Indigo Royal", "রয়্যাল ইন্ডিগো", "#4338CA", Color(0xFF4338CA)),
    PURE_CHARCOAL("Pure Monochromatic", "বিশুদ্ধ মনোক্রোম", "#000000", Color(0xFF000000))
}

data class SmartPageBudget(
    val totalPages: Int,
    val estimatedLastPageFillPercent: Int,
    val statusTextEn: String,
    val statusTextBn: String,
    val isOverflowWarning: Boolean
)

data class SmartKeywordChip(
    val keyword: String,
    val isMatched: Boolean,
    val category: String = "Core Competency"
)

data class SmartJobAnalysis(
    val circularTitle: String = "",
    val targetRole: String = "",
    val rawText: String = "",
    val atsMatchScore: Int = 0,
    val foundKeywords: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val tailoredSummary: String = "",
    val generatedCoverLetter: String = "",
    val generatedEmailSubject: String = "",
    val generatedEmailBody: String = ""
)

data class SmartCvHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val candidateName: String,
    val profileLabel: String,
    val templateName: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Storage Constants
private const val SMART_CV_PREFS = "smart_cv_studio_prefs_v1"
private const val SMART_CV_PROFILES_KEY = "smart_cv_profiles_json_list"
private const val SMART_CV_ACTIVE_ID_KEY = "smart_cv_active_profile_uuid"
private const val SMART_CV_HISTORY_KEY = "smart_cv_export_history_list"

// Old ATS CV prefs for seamless import
private const val OLD_ATS_PREFS = "ats_cv_builder_multi_prefs_v6"
private const val OLD_ATS_PROFILES_KEY = "saved_cv_profiles_json_list"

object SmartCvStorage {

    fun loadProfiles(context: Context): List<CvData> {
        val prefs = context.getSharedPreferences(SMART_CV_PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(SMART_CV_PROFILES_KEY, null)
        if (!json.isNullOrBlank()) {
            val parsed = parseProfilesJson(json)
            if (parsed.isNotEmpty()) return parsed
        }

        // Try importing from older ATS CV Builder prefs if available
        val oldPrefs = context.getSharedPreferences(OLD_ATS_PREFS, Context.MODE_PRIVATE)
        val oldJson = oldPrefs.getString(OLD_ATS_PROFILES_KEY, null)
        if (!oldJson.isNullOrBlank()) {
            val oldProfiles = parseProfilesJson(oldJson)
            if (oldProfiles.isNotEmpty()) {
                saveProfiles(context, oldProfiles)
                return oldProfiles
            }
        }

        // Fallback to default starter profile
        val defaultProfile = createStarterProfile()
        saveProfiles(context, listOf(defaultProfile))
        return listOf(defaultProfile)
    }

    fun saveProfiles(context: Context, profiles: List<CvData>) {
        val prefs = context.getSharedPreferences(SMART_CV_PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray()
        profiles.forEach { p ->
            arr.put(serializeProfile(p))
        }
        prefs.edit().putString(SMART_CV_PROFILES_KEY, arr.toString()).apply()
    }

    fun loadActiveProfileId(context: Context): String {
        val prefs = context.getSharedPreferences(SMART_CV_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(SMART_CV_ACTIVE_ID_KEY, "") ?: ""
    }

    fun saveActiveProfileId(context: Context, id: String) {
        val prefs = context.getSharedPreferences(SMART_CV_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(SMART_CV_ACTIVE_ID_KEY, id).apply()
    }

    fun loadExportHistory(context: Context): List<SmartCvHistoryItem> {
        val prefs = context.getSharedPreferences(SMART_CV_PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(SMART_CV_HISTORY_KEY, null) ?: return emptyList()
        val list = mutableListOf<SmartCvHistoryItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val file = File(obj.optString("filePath", ""))
                if (file.exists() && file.length() > 0) {
                    list.add(
                        SmartCvHistoryItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            fileName = obj.optString("fileName", file.name),
                            filePath = file.absolutePath,
                            candidateName = obj.optString("candidateName", "Candidate"),
                            profileLabel = obj.optString("profileLabel", "Smart Profile"),
                            templateName = obj.optString("templateName", "Harvard Ivy ATS"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return list
    }

    fun addExportHistory(context: Context, item: SmartCvHistoryItem) {
        val current = loadExportHistory(context).toMutableList()
        current.removeAll { it.filePath == item.filePath }
        current.add(0, item)
        val trimmed = if (current.size > 30) current.take(30) else current
        val prefs = context.getSharedPreferences(SMART_CV_PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray()
        trimmed.forEach { h ->
            arr.put(JSONObject().apply {
                put("id", h.id)
                put("fileName", h.fileName)
                put("filePath", h.filePath)
                put("candidateName", h.candidateName)
                put("profileLabel", h.profileLabel)
                put("templateName", h.templateName)
                put("timestamp", h.timestamp)
            })
        }
        prefs.edit().putString(SMART_CV_HISTORY_KEY, arr.toString()).apply()
    }

    fun deleteExportHistory(context: Context, item: SmartCvHistoryItem) {
        try {
            val file = File(item.filePath)
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
        val current = loadExportHistory(context).filter { it.id != item.id }
        val prefs = context.getSharedPreferences(SMART_CV_PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray()
        current.forEach { h ->
            arr.put(JSONObject().apply {
                put("id", h.id)
                put("fileName", h.fileName)
                put("filePath", h.filePath)
                put("candidateName", h.candidateName)
                put("profileLabel", h.profileLabel)
                put("templateName", h.templateName)
                put("timestamp", h.timestamp)
            })
        }
        prefs.edit().putString(SMART_CV_HISTORY_KEY, arr.toString()).apply()
    }

    private fun serializeProfile(p: CvData): JSONObject {
        return JSONObject().apply {
            put("id", p.id)
            put("profileLabel", p.profileLabel)
            put("fullName", p.fullName)
            put("jobTitle", p.jobTitle)
            put("email", p.email)
            put("phone", p.phone)
            put("address", p.address)
            put("linkedin", p.linkedin)
            put("githubOrPortfolio", p.githubOrPortfolio)
            put("summary", p.summary)
            put("photoBase64", p.photoBase64)
            put("photoShape", p.photoShape)
            put("photoScale", p.photoScale.toDouble())
            put("photoOffsetX", p.photoOffsetX.toDouble())
            put("photoOffsetY", p.photoOffsetY.toDouble())
            put("photoWidth", p.photoWidth)
            put("photoHeight", p.photoHeight)
            put("photoBorderWidth", p.photoBorderWidth.toDouble())
            put("photoCornerRadius", p.photoCornerRadius)
            put("fatherName", p.fatherName)
            put("motherName", p.motherName)
            put("religion", p.religion)
            put("bloodGroup", p.bloodGroup)
            put("permanentAddress", p.permanentAddress)
            put("presentAddress", p.presentAddress)
            put("certifications", p.certifications)
            put("references", p.references)
            put("languages", p.languages)
            put("templateStyle", p.templateStyle.name)
            put("targetJobCircular", p.targetJobCircular)
            put("isFresher", p.isFresher)
            put("showSignatureLine", p.showSignatureLine)
            put("showContactIcons", p.showContactIcons)
            put("showSectionIcons", p.showSectionIcons)
            put("fontScale", p.fontScale)
            put("bulletStyle", p.bulletStyle)
            put("primaryColorHexOverride", p.primaryColorHexOverride)
            put("sectionTitleSize", p.sectionTitleSize.toDouble())
            put("bodyFontSize", p.bodyFontSize.toDouble())
            put("customMargin", p.customMargin.toDouble())
            put("sectionSpacing", p.sectionSpacing.toDouble())
            put("itemSpacing", p.itemSpacing.toDouble())
            put("customLineSpacing", p.customLineSpacing.toDouble())
            put("skillDisplayStyle", p.skillDisplayStyle)
            put("showSkillDescriptions", p.showSkillDescriptions)

            // Experiences
            val expArr = JSONArray()
            p.experiences.forEach { exp ->
                expArr.put(JSONObject().apply {
                    put("id", exp.id)
                    put("company", exp.company)
                    put("role", exp.role)
                    put("startDate", exp.startDate)
                    put("endDate", exp.endDate)
                    put("isCurrent", exp.isCurrent)
                    put("description", exp.description)
                    put("location", exp.location)
                })
            }
            put("experiences", expArr)

            // Educations
            val eduArr = JSONArray()
            p.educations.forEach { edu ->
                eduArr.put(JSONObject().apply {
                    put("id", edu.id)
                    put("degree", edu.degree)
                    put("institution", edu.institution)
                    put("passingYear", edu.passingYear)
                    put("result", edu.result)
                    put("examLevel", edu.examLevel)
                    put("subjectMajor", edu.subjectMajor)
                    put("resultType", edu.resultType)
                })
            }
            put("educations", eduArr)

            // Skills
            val skillArr = JSONArray()
            p.skills.forEach { sk ->
                skillArr.put(JSONObject().apply {
                    put("id", sk.id)
                    put("name", sk.name)
                    put("level", sk.level)
                    put("category", sk.category)
                    put("description", sk.description)
                })
            }
            put("skills", skillArr)

            // Projects
            val projArr = JSONArray()
            p.projects.forEach { pr ->
                projArr.put(JSONObject().apply {
                    put("id", pr.id)
                    put("title", pr.title)
                    put("description", pr.description)
                    put("link", pr.link)
                })
            }
            put("projects", projArr)

            // Custom Sections
            val custArr = JSONArray()
            p.customSections.forEach { cs ->
                custArr.put(JSONObject().apply {
                    put("id", cs.id)
                    put("title", cs.title)
                    put("content", cs.content)
                    put("sectionType", cs.sectionType)
                })
            }
            put("customSections", custArr)

            // Section Order & Hidden
            val orderArr = JSONArray()
            p.sectionOrder.forEach { orderArr.put(it) }
            put("sectionOrder", orderArr)

            val hiddenArr = JSONArray()
            p.hiddenSections.forEach { hiddenArr.put(it) }
            put("hiddenSections", hiddenArr)
        }
    }

    private fun parseProfilesJson(raw: String): List<CvData> {
        val list = mutableListOf<CvData>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val experiences = mutableListOf<CvExperienceItem>()
                val expArr = obj.optJSONArray("experiences")
                if (expArr != null) {
                    for (j in 0 until expArr.length()) {
                        val eo = expArr.getJSONObject(j)
                        experiences.add(
                            CvExperienceItem(
                                id = eo.optString("id", UUID.randomUUID().toString()),
                                company = eo.optString("company", ""),
                                role = eo.optString("role", ""),
                                startDate = eo.optString("startDate", ""),
                                endDate = eo.optString("endDate", ""),
                                isCurrent = eo.optBoolean("isCurrent", false),
                                description = eo.optString("description", ""),
                                location = eo.optString("location", "")
                            )
                        )
                    }
                }

                val educations = mutableListOf<CvEducationItem>()
                val eduArr = obj.optJSONArray("educations")
                if (eduArr != null) {
                    for (j in 0 until eduArr.length()) {
                        val ed = eduArr.getJSONObject(j)
                        educations.add(
                            CvEducationItem(
                                id = ed.optString("id", UUID.randomUUID().toString()),
                                degree = ed.optString("degree", ""),
                                institution = ed.optString("institution", ""),
                                passingYear = ed.optString("passingYear", ""),
                                result = ed.optString("result", ""),
                                examLevel = ed.optString("examLevel", ""),
                                subjectMajor = ed.optString("subjectMajor", ""),
                                resultType = ed.optString("resultType", "")
                            )
                        )
                    }
                }

                val skills = mutableListOf<CvSkillItem>()
                val skillArr = obj.optJSONArray("skills")
                if (skillArr != null) {
                    for (j in 0 until skillArr.length()) {
                        val so = skillArr.getJSONObject(j)
                        skills.add(
                            CvSkillItem(
                                id = so.optString("id", UUID.randomUUID().toString()),
                                name = so.optString("name", ""),
                                level = so.optString("level", "Proficient"),
                                category = so.optString("category", ""),
                                description = so.optString("description", "")
                            )
                        )
                    }
                }

                val projects = mutableListOf<CvProjectItem>()
                val projArr = obj.optJSONArray("projects")
                if (projArr != null) {
                    for (j in 0 until projArr.length()) {
                        val po = projArr.getJSONObject(j)
                        projects.add(
                            CvProjectItem(
                                id = po.optString("id", UUID.randomUUID().toString()),
                                title = po.optString("title", ""),
                                description = po.optString("description", ""),
                                link = po.optString("link", "")
                            )
                        )
                    }
                }

                val customSections = mutableListOf<CvCustomSectionItem>()
                val custArr = obj.optJSONArray("customSections")
                if (custArr != null) {
                    for (j in 0 until custArr.length()) {
                        val co = custArr.getJSONObject(j)
                        customSections.add(
                            CvCustomSectionItem(
                                id = co.optString("id", UUID.randomUUID().toString()),
                                title = co.optString("title", ""),
                                content = co.optString("content", ""),
                                sectionType = co.optString("sectionType", "EXPERIENCE")
                            )
                        )
                    }
                }

                val sectionOrder = mutableListOf<String>()
                val orderArr = obj.optJSONArray("sectionOrder")
                if (orderArr != null) {
                    for (k in 0 until orderArr.length()) {
                        sectionOrder.add(orderArr.getString(k))
                    }
                } else {
                    sectionOrder.addAll(listOf("SUMMARY", "EXPERIENCE", "EDUCATION", "SKILLS", "PROJECTS", "CERTIFICATIONS", "LANGUAGES", "CUSTOM_SECTIONS", "PERSONAL_INFO", "REFERENCES"))
                }

                val hiddenSections = mutableListOf<String>()
                val hiddenArr = obj.optJSONArray("hiddenSections")
                if (hiddenArr != null) {
                    for (k in 0 until hiddenArr.length()) {
                        hiddenSections.add(hiddenArr.getString(k))
                    }
                }

                list.add(
                    CvData(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        profileLabel = obj.optString("profileLabel", "Smart Profile"),
                        fullName = obj.optString("fullName", ""),
                        jobTitle = obj.optString("jobTitle", ""),
                        email = obj.optString("email", ""),
                        phone = obj.optString("phone", ""),
                        address = obj.optString("address", ""),
                        linkedin = obj.optString("linkedin", ""),
                        githubOrPortfolio = obj.optString("githubOrPortfolio", ""),
                        summary = obj.optString("summary", ""),
                        photoBase64 = obj.optString("photoBase64", ""),
                        photoShape = obj.optString("photoShape", "Circle"),
                        photoScale = obj.optDouble("photoScale", 1.0).toFloat(),
                        photoOffsetX = obj.optDouble("photoOffsetX", 0.0).toFloat(),
                        photoOffsetY = obj.optDouble("photoOffsetY", 0.0).toFloat(),
                        photoWidth = obj.optInt("photoWidth", 80),
                        photoHeight = obj.optInt("photoHeight", 80),
                        photoBorderWidth = obj.optDouble("photoBorderWidth", 1.5).toFloat(),
                        photoCornerRadius = obj.optInt("photoCornerRadius", 10),
                        fatherName = obj.optString("fatherName", ""),
                        motherName = obj.optString("motherName", ""),
                        religion = obj.optString("religion", ""),
                        bloodGroup = obj.optString("bloodGroup", ""),
                        permanentAddress = obj.optString("permanentAddress", ""),
                        presentAddress = obj.optString("presentAddress", ""),
                        certifications = obj.optString("certifications", ""),
                        references = obj.optString("references", ""),
                        experiences = experiences,
                        educations = educations,
                        skills = skills,
                        projects = projects,
                        languages = obj.optString("languages", "English (Fluent), Bengali (Native)"),
                        customSections = customSections,
                        sectionOrder = sectionOrder,
                        hiddenSections = hiddenSections,
                        showContactIcons = obj.optBoolean("showContactIcons", true),
                        showSectionIcons = obj.optBoolean("showSectionIcons", false),
                        fontScale = obj.optString("fontScale", "STANDARD"),
                        bulletStyle = obj.optString("bulletStyle", "BULLET"),
                        primaryColorHexOverride = obj.optString("primaryColorHexOverride", "#1E3A8A"),
                        sectionTitleSize = obj.optDouble("sectionTitleSize", 11.5).toFloat(),
                        bodyFontSize = obj.optDouble("bodyFontSize", 9.5).toFloat(),
                        customMargin = obj.optDouble("customMargin", 36.0).toFloat(),
                        sectionSpacing = obj.optDouble("sectionSpacing", 8.0).toFloat(),
                        itemSpacing = obj.optDouble("itemSpacing", 4.0).toFloat(),
                        customLineSpacing = obj.optDouble("customLineSpacing", 1.15).toFloat(),
                        skillDisplayStyle = obj.optString("skillDisplayStyle", "GROUPED_COMMA"),
                        showSkillDescriptions = obj.optBoolean("showSkillDescriptions", true)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun createStarterProfile(): CvData {
        return CvData(
            id = UUID.randomUUID().toString(),
            profileLabel = "Executive Profile",
            fullName = "Md. Shariful Islam",
            jobTitle = "Operations & Management Specialist",
            email = "connect.shariful@gmail.com",
            phone = "+8801768899599",
            address = "Dhaka, Bangladesh",
            linkedin = "linkedin.com/in/shariful-islam",
            githubOrPortfolio = "portfolio.shariful.me",
            summary = "Results-driven Management Specialist with proven expertise in Strategic Operations, Cross-functional Team Leadership, and Market Expansion. Adept at transforming business targets into high-performing workflows and measurable revenue growth.",
            languages = "English (Professional), Bengali (Native)",
            primaryColorHexOverride = "#1E3A8A",
            bulletStyle = "BULLET",
            experiences = listOf(
                CvExperienceItem(
                    company = "Apex Enterprises Ltd.",
                    role = "Assistant Manager - Operations",
                    startDate = "Jan 2023",
                    endDate = "Present",
                    isCurrent = true,
                    location = "Dhaka, Bangladesh",
                    description = "• Spearheaded end-to-end operational workflows, boosting turnaround speed by 28% across 4 regional branches.\n• Orchestrated supply chain optimization and inventory forecasting, cutting warehousing overheads by 15%.\n• Mentored a high-caliber team of 14 executives, maintaining 98% quarterly target compliance."
                ),
                CvExperienceItem(
                    company = "BBS (Population & Housing Census)",
                    role = "Enumerator / Data Collector",
                    startDate = "Jun 2022",
                    endDate = "Jul 2022",
                    isCurrent = false,
                    location = "Natore, Bangladesh",
                    description = "• Handled high-volume demographic census records with 100% data fidelity and strict confidentiality.\n• Leveraged digital enumeration software to sync daily field telemetry under rigorous deadlines."
                )
            ),
            educations = listOf(
                CvEducationItem(
                    degree = "Master of Business Administration (MBA)",
                    institution = "Govt. Edward College, Pabna",
                    passingYear = "2022",
                    result = "3.42 out of 4.00",
                    subjectMajor = "Management"
                ),
                CvEducationItem(
                    degree = "Bachelor of Business Administration (BBA)",
                    institution = "Baraigram Govt. College, Natore",
                    passingYear = "2020",
                    result = "3.28 out of 4.00",
                    subjectMajor = "Management"
                )
            ),
            skills = listOf(
                CvSkillItem(name = "Strategic Operations Planning", category = "Management & Strategy"),
                CvSkillItem(name = "Cross-Functional Leadership", category = "Management & Strategy"),
                CvSkillItem(name = "Market Research & Analysis", category = "Business & Analytics"),
                CvSkillItem(name = "Advanced MS Excel (Data Modeling)", category = "Technical & Tools"),
                CvSkillItem(name = "ERP & Inventory Management", category = "Technical & Tools"),
                CvSkillItem(name = "Territory & Distributor Relations", category = "Marketing & Sales"),
                CvSkillItem(name = "Sales Pipeline Optimization", category = "Marketing & Sales")
            ),
            projects = listOf(
                CvProjectItem(
                    title = "Regional Supply Chain Optimization Study",
                    description = "Formulated an empirical logistics cost reduction framework adopting lean inventory principles for FMCG distributors in North Bengal.",
                    link = "https://github.com/shariful/supply-chain-framework"
                )
            ),
            certifications = "• Certified Lean Six Sigma Green Belt (2023)\n• Advanced Excel for Business Analytics - Coursera (2022)"
        )
    }
}

package com.example.data.model

/**
 * MODULE 1: JSON Schema & Data Models for AI Job Matching & ATS Optimizer
 */

object ChecklistStatus {
    const val PRESENT = "PRESENT"
    const val MISSING = "MISSING"
    const val NEEDS_REVIEW = "NEEDS_REVIEW"
}

object CvSectionKey {
    const val SUMMARY = "SUMMARY"
    const val EXPERIENCE = "EXPERIENCE"
    const val EDUCATION = "EDUCATION"
    const val SKILLS = "SKILLS"
    const val PROJECTS = "PROJECTS"
    const val CERTIFICATIONS = "CERTIFICATIONS"
    const val LANGUAGES = "LANGUAGES"
    const val LAYOUT = "LAYOUT"
    const val CONTACT_INFO = "CONTACT_INFO"
    const val CUSTOM_SECTIONS = "CUSTOM_SECTIONS"
}

data class AtsChecklistItem(
    val title: String,
    val status: String = ChecklistStatus.MISSING, // "PRESENT" or "MISSING"
    val sectionKey: String = CvSectionKey.SUMMARY, // "SKILLS", "EXPERIENCE", "SUMMARY", "LAYOUT", etc.
    val explanation: String = "",
    val detail: String? = null,
    val suggestedContent: String? = null
)

data class JobMatchResponse(
    val score: Int = 0,
    val analysisType: String = "ATS", // "ATS" or "CIRCULAR"
    val checklist: List<AtsChecklistItem> = emptyList(),
    val summaryFeedback: String? = null,
    val suggestedKeywords: List<String> = emptyList(),
    val strengths: List<String> = emptyList()
)

data class FixNowAiResult(
    val sectionKey: String,
    val title: String,
    val generatedContent: String,
    val actionType: String = "APPEND", // "APPEND", "REPLACE", "UPDATE"
    val bulletPoints: List<String> = emptyList()
)

/**
 * Generalized Resume Data Model
 */
data class ResumeModel(
    val fullName: String = "",
    val jobTitle: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val linkedin: String = "",
    val githubOrPortfolio: String = "",
    val summary: String = "",
    val skills: List<String> = emptyList(),
    val experiences: List<ResumeExperience> = emptyList(),
    val educations: List<ResumeEducation> = emptyList(),
    val projects: List<ResumeProject> = emptyList(),
    val certifications: String = "",
    val languages: String = "",
    val customSections: Map<String, String> = emptyMap(),
    val targetJobCircular: String = ""
)

data class ResumeExperience(
    val company: String = "",
    val role: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrent: Boolean = false,
    val location: String = "",
    val description: String = "",
    val achievements: List<String> = emptyList()
)

data class ResumeEducation(
    val institution: String = "",
    val degree: String = "",
    val passingYear: String = "",
    val result: String = ""
)

data class ResumeProject(
    val title: String = "",
    val description: String = "",
    val link: String = ""
)

package com.example.util

import com.example.data.model.CvSectionKey
import com.example.data.model.FixNowAiResult
import com.example.data.model.ResumeEducation
import com.example.data.model.ResumeExperience
import com.example.data.model.ResumeModel
import com.example.data.model.ResumeProject
import com.example.ui.screens.tools.CvCustomSectionItem
import com.example.ui.screens.tools.CvData
import com.example.ui.screens.tools.CvExperienceItem
import com.example.ui.screens.tools.CvProjectItem
import com.example.ui.screens.tools.CvSkillItem

/**
 * MODULE 4: Resume & CV Data Merge Engine
 * Dynamically merges AI generated ATS content, bullet points, and keywords into resume models.
 */
object ResumeMergeHelper {

    /**
     * Converts UI CvData to generalized ResumeModel
     */
    fun cvDataToResumeModel(cv: CvData): ResumeModel {
        return ResumeModel(
            fullName = cv.fullName,
            jobTitle = cv.jobTitle,
            email = cv.email,
            phone = cv.phone,
            address = cv.address,
            linkedin = cv.linkedin,
            githubOrPortfolio = cv.githubOrPortfolio,
            summary = cv.summary,
            skills = cv.skills.map { it.name },
            experiences = cv.experiences.map {
                ResumeExperience(
                    company = it.company,
                    role = it.role,
                    startDate = it.startDate,
                    endDate = it.endDate,
                    isCurrent = it.isCurrent,
                    location = it.location,
                    description = it.description
                )
            },
            educations = cv.educations.map {
                ResumeEducation(
                    institution = it.institution,
                    degree = it.degree,
                    passingYear = it.passingYear,
                    result = it.result
                )
            },
            projects = cv.projects.map {
                ResumeProject(
                    title = it.title,
                    description = it.description,
                    link = it.link
                )
            },
            certifications = cv.certifications,
            languages = cv.languages,
            targetJobCircular = cv.targetJobCircular
        )
    }

    /**
     * Merges AI generated content directly into CvData
     */
    fun mergeAiIntoCvData(
        current: CvData,
        sectionKey: String,
        generatedContent: String,
        bulletPoints: List<String> = emptyList()
    ): CvData {
        val cleanContent = generatedContent.trim()
        val normalizedKey = sectionKey.uppercase().trim()

        return when (normalizedKey) {
            CvSectionKey.SUMMARY -> {
                current.copy(summary = cleanContent)
            }

            CvSectionKey.SKILLS -> {
                val existingSkills = current.skills.map { it.name.trim().lowercase() }.toSet()
                val newSkillsList = current.skills.toMutableList()

                // Parse comma-separated or newline-separated skills
                val rawSkills = cleanContent.split(",", "\n", "•", "-").map { it.trim() }.filter { it.isNotBlank() }
                for (s in rawSkills) {
                    val skillName = s.replace(Regex("^[^a-zA-Z0-9]+"), "").trim()
                    if (skillName.isNotBlank() && skillName.lowercase() !in existingSkills) {
                        newSkillsList.add(CvSkillItem(name = skillName, level = "Expert"))
                    }
                }
                current.copy(skills = newSkillsList)
            }

            CvSectionKey.EXPERIENCE -> {
                if (current.experiences.isNotEmpty()) {
                    val updatedList = current.experiences.toMutableList()
                    val firstExp = updatedList[0]
                    val mergedDesc = if (bulletPoints.isNotEmpty()) {
                        val newBullets = bulletPoints.joinToString("\n") { if (it.startsWith("•")) it else "• $it" }
                        if (firstExp.description.isNotBlank()) "${firstExp.description.trim()}\n$newBullets" else newBullets
                    } else {
                        if (firstExp.description.isNotBlank()) "${firstExp.description.trim()}\n$cleanContent" else cleanContent
                    }
                    updatedList[0] = firstExp.copy(description = mergedDesc)
                    current.copy(experiences = updatedList)
                } else {
                    // Create an initial experience item
                    val newExp = CvExperienceItem(
                        company = "Professional Experience",
                        role = current.jobTitle.ifBlank { "Specialist" },
                        startDate = "2023",
                        endDate = "Present",
                        isCurrent = true,
                        description = if (bulletPoints.isNotEmpty()) bulletPoints.joinToString("\n") { "• $it" } else cleanContent
                    )
                    current.copy(experiences = listOf(newExp))
                }
            }

            CvSectionKey.PROJECTS -> {
                val newProj = CvProjectItem(
                    title = "Key Featured Project",
                    description = cleanContent
                )
                current.copy(projects = current.projects + newProj)
            }

            CvSectionKey.CERTIFICATIONS -> {
                val merged = if (current.certifications.isNotBlank()) {
                    "${current.certifications.trim()}\n$cleanContent"
                } else {
                    cleanContent
                }
                current.copy(certifications = merged)
            }

            CvSectionKey.LANGUAGES -> {
                val merged = if (current.languages.isNotBlank()) {
                    "${current.languages.trim()}, $cleanContent"
                } else {
                    cleanContent
                }
                current.copy(languages = merged)
            }

            CvSectionKey.CUSTOM_SECTIONS -> {
                val newSec = CvCustomSectionItem(
                    title = "Key Accomplishments",
                    content = cleanContent
                )
                current.copy(customSections = current.customSections + newSec)
            }

            else -> {
                // Default fallback: append to summary or custom section
                if (current.summary.isBlank()) {
                    current.copy(summary = cleanContent)
                } else {
                    val newSec = CvCustomSectionItem(
                        title = "Additional Highlights",
                        content = cleanContent
                    )
                    current.copy(customSections = current.customSections + newSec)
                }
            }
        }
    }
}

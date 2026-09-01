package com.example.ui.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AtsChecklistItem
import com.example.data.model.CvSectionKey
import com.example.data.model.FixNowAiResult
import com.example.data.model.ResumeModel
import com.example.data.network.JobMatchAiPrompts
import com.example.ui.screens.tools.CvData
import com.example.ui.theme.CalculatorThemeColors
import com.example.util.ResumeMergeHelper
import kotlinx.coroutines.launch

/**
 * MODULE 4: Fix Now BottomSheet Dialog
 * Handles:
 *  - Option A (Manual): Deep links / Navigates to the respective CV section tab.
 *  - Option B (AI Auto-Fill): Triggers Gemini API to generate the missing content and auto-merges it.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixNowBottomSheet(
    item: AtsChecklistItem,
    cvData: CvData,
    themeColors: CalculatorThemeColors,
    isBn: Boolean = false,
    onDismiss: () -> Unit,
    onNavigateToSection: (sectionKey: String, targetTabIndex: Int) -> Unit,
    onApplyAiFix: (updatedCv: CvData) -> Unit,
    callGeminiAiApi: suspend (prompt: String, sysPrompt: String) -> String
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf<FixNowAiResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val targetTabIndex = when (item.sectionKey.uppercase()) {
        CvSectionKey.CONTACT_INFO, CvSectionKey.SUMMARY -> 0 // Profile
        CvSectionKey.EXPERIENCE -> 1 // Experience
        CvSectionKey.EDUCATION, CvSectionKey.SKILLS, CvSectionKey.CERTIFICATIONS, CvSectionKey.LANGUAGES -> 2 // Education & Skills
        CvSectionKey.CUSTOM_SECTIONS, CvSectionKey.LAYOUT -> 3 // Customization
        CvSectionKey.PROJECTS -> 1 // Experience or projects
        else -> 0
    }

    val sectionNameLabel = when (item.sectionKey.uppercase()) {
        CvSectionKey.SUMMARY -> if (isBn) "প্রোফাইল সামারি" else "Executive Summary"
        CvSectionKey.EXPERIENCE -> if (isBn) "কাজের অভিজ্ঞতা" else "Work Experience"
        CvSectionKey.SKILLS -> if (isBn) "স্কিল ও দক্ষতা" else "Skills & Competencies"
        CvSectionKey.EDUCATION -> if (isBn) "শিক্ষাগত যোগ্যতা" else "Education"
        CvSectionKey.PROJECTS -> if (isBn) "প্রজেক্টস" else "Projects & Portfolio"
        CvSectionKey.CERTIFICATIONS -> if (isBn) "সার্টিফিকেশন" else "Certifications"
        CvSectionKey.CONTACT_INFO -> if (isBn) "যোগাযোগ তথ্য" else "Contact Information"
        CvSectionKey.LAYOUT -> if (isBn) "লেআউট ও ফরম্যাটিং" else "Layout & Formatting"
        else -> item.sectionKey
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = themeColors.cardBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "এটিএস অপ্টিমাইজার সমাধান" else "Fix ATS Requirement",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = sectionNameLabel,
                            fontSize = 11.5.sp,
                            color = themeColors.buttonEqualBg,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Missing Requirement Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✕ " + item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                    if (!item.detail.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.detail,
                            fontSize = 11.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PREVIEW AI RESULT (IF GENERATED)
            if (generatedResult != null) {
                val res = generatedResult!!
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "জেমিনি এআই জেনারেটেড কন্টেন্ট প্রস্তুত:" else "AI Generated Content Ready:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (res.bulletPoints.isNotEmpty()) {
                            res.bulletPoints.forEach { pt ->
                                Text(
                                    text = if (pt.startsWith("•")) pt else "• $pt",
                                    fontSize = 11.5.sp,
                                    color = themeColors.displayText,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        } else {
                            Text(
                                text = res.generatedContent,
                                fontSize = 11.5.sp,
                                color = themeColors.displayText,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val updated = ResumeMergeHelper.mergeAiIntoCvData(
                                    current = cvData,
                                    sectionKey = item.sectionKey,
                                    generatedContent = res.generatedContent,
                                    bulletPoints = res.bulletPoints
                                )
                                onApplyAiFix(updated)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Text(
                                text = if (isBn) "সিভিতে প্রয়োগ ও অটো-মার্জ করুন" else "Apply & Auto-Merge to Resume",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error display
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    fontSize = 11.sp,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // ACTION OPTIONS
            Text(
                text = if (isBn) "কীভাবে সমাধান করতে চান?" else "How would you like to fix this?",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.displayText.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // OPTION B: AI AUTO-FILL BUTTON
            Button(
                onClick = {
                    if (isGenerating) return@Button
                    isGenerating = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val resume = ResumeMergeHelper.cvDataToResumeModel(cvData)
                            val prompt = JobMatchAiPrompts.buildFixNowAutoFillPrompt(
                                sectionKey = item.sectionKey,
                                itemTitle = item.title,
                                resume = resume,
                                jobCircular = cvData.targetJobCircular
                            )
                            val rawJson = callGeminiAiApi(prompt, JobMatchAiPrompts.STRICT_JSON_SYSTEM_INSTRUCTION)
                            val result = JobMatchAiPrompts.parseFixNowResult(rawJson, item.sectionKey, item.title)
                            generatedResult = result
                        } catch (e: Exception) {
                            errorMessage = "AI generation failed: ${e.localizedMessage ?: "Unknown error"}"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "জেমিনি এআই কন্টেন্ট লিখছে..." else "Gemini AI Generating Fix...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "অপশন ১: এআই অটো-ফিল দিয়ে লিখুন" else "Option B: AI Auto-Generate & Merge",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // OPTION A: MANUAL EDIT NAVIGATION
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onNavigateToSection(item.sectionKey, targetTabIndex)
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.25f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.displayText),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = themeColors.displayText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "অপশন ২: নিজে হাতে এডিট করুন ($sectionNameLabel)" else "Option A: Manual Edit ($sectionNameLabel)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

package com.example.ui.screens.tools.smartcv

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.screens.tools.CvData
import com.example.ui.screens.tools.CvEducationItem
import com.example.ui.screens.tools.CvExperienceItem
import com.example.ui.screens.tools.CvProjectItem
import com.example.ui.screens.tools.CvSkillItem
import com.example.ui.screens.tools.CvCustomSectionItem
import com.example.ui.theme.CalculatorThemeColors
import kotlinx.coroutines.launch
import java.util.UUID

// =============================================================================
// MODULAR BLOCK MANAGER & SECTION REORDER
// =============================================================================

data class SmartCvSectionMeta(
    val key: String,
    val titleEn: String,
    val titleBn: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCvStructureTab(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onOpenXyzDialog: (String, (String) -> Unit) -> Unit
) {
    var activeEditSection by remember { mutableStateOf<String?>(null) }

    val sectionDefinitions = remember {
        listOf(
            SmartCvSectionMeta("HEADER", "Personal Info & Contact", "ব্যক্তিগত তথ্য ও যোগাযোগ", Icons.Default.Person),
            SmartCvSectionMeta("SUMMARY", "Professional Summary", "প্রফেশনাল সামারি ও অবজেক্টিভ", Icons.Default.ShortText),
            SmartCvSectionMeta("EXPERIENCE", "Work Experience", "কাজের অভিজ্ঞতা", Icons.Default.Work),
            SmartCvSectionMeta("EDUCATION", "Education & Academics", "শিক্ষাগত যোগ্যতা", Icons.Default.School),
            SmartCvSectionMeta("SKILLS", "Skills & Competencies", "দক্ষতা ও স্কিলস", Icons.Default.Bolt),
            SmartCvSectionMeta("PROJECTS", "Key Projects & Portfolio", "প্রজেক্ট ও পোর্টফোলিও", Icons.Default.RocketLaunch),
            SmartCvSectionMeta("CERTIFICATIONS", "Certifications & Courses", "সার্টিফিকেশন ও কোর্স", Icons.Default.WorkspacePremium),
            SmartCvSectionMeta("LANGUAGES", "Languages & Fluency", "ভাষা ও দক্ষতা", Icons.Default.Translate),
            SmartCvSectionMeta("CUSTOM", "Custom Sections", "কাস্টম সেকশন", Icons.Default.DashboardCustomize)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Header hint banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonEqualBg.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "মডুলার ব্লক ম্যানেজার ও রি-অর্ডার" else "Modular Block Manager",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "তীর চিহ্নে চাপ দিয়ে সেকশনের ক্রম পরিবর্তন করুন। চোখের আইকনে চাপ দিয়ে হাইড/শো করুন।" 
                            else "Use arrows to reorder sections. Use the eye icon to hide/show on CV without deleting data.",
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        itemsIndexed(sectionDefinitions) { index, sec ->
            val key = sec.key
            val icon = sec.icon
            val isHidden = cvData.hiddenSections.contains(key)
            val title = if (isBn) sec.titleBn else sec.titleEn

            val subtitleText = when (key) {
                "HEADER" -> cvData.fullName.ifBlank { "Name & contact info" }
                "SUMMARY" -> if (cvData.summary.isNotBlank()) cvData.summary.take(45) + "..." else "Add your executive pitch"
                "EXPERIENCE" -> "${cvData.experiences.size} ${if (isBn) "টি অভিজ্ঞতা যোগ করা" else "roles added"}"
                "EDUCATION" -> "${cvData.educations.size} ${if (isBn) "টি ডিগ্রি যোগ করা" else "degrees added"}"
                "SKILLS" -> "${cvData.skills.size} ${if (isBn) "টি স্কিল যোগ করা" else "skills categorized"}"
                "PROJECTS" -> "${cvData.projects.size} ${if (isBn) "টি প্রজেক্ট" else "projects listed"}"
                "CERTIFICATIONS" -> if (cvData.certifications.isNotBlank()) "Included" else "None"
                "LANGUAGES" -> cvData.languages.ifBlank { "English, Bengali" }
                else -> "${cvData.customSections.size} custom blocks"
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isHidden) themeColors.background else themeColors.buttonFunctionBg.copy(alpha = 0.35f),
                border = BorderStroke(
                    1.dp,
                    if (isHidden) themeColors.displayText.copy(alpha = 0.1f) else themeColors.displayText.copy(alpha = 0.18f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeEditSection = key }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isHidden) Color.Gray.copy(alpha = 0.2f) else themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isHidden) Color.Gray else themeColors.buttonEqualBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = if (isHidden) themeColors.displayText.copy(alpha = 0.45f) else themeColors.displayText
                            )
                            if (isHidden) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color.Gray.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isBn) "লুকানো" else "Hidden",
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        Text(
                            text = subtitleText,
                            fontSize = 11.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.55f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Eye visibility toggle
                    IconButton(
                        onClick = {
                            val newHidden = cvData.hiddenSections.toMutableList()
                            if (newHidden.contains(key)) newHidden.remove(key) else newHidden.add(key)
                            onCvDataChange(cvData.copy(hiddenSections = newHidden))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Visibility",
                            tint = if (isHidden) Color.Gray.copy(alpha = 0.6f) else themeColors.buttonEqualBg,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Edit button
                    IconButton(
                        onClick = { activeEditSection = key },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Section",
                            tint = themeColors.displayText.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet / Dialog based on activeEditSection
    activeEditSection?.let { sectionKey ->
        SmartSectionEditDialog(
            sectionKey = sectionKey,
            cvData = cvData,
            onCvDataChange = onCvDataChange,
            onDismiss = { activeEditSection = null },
            themeColors = themeColors,
            isBn = isBn,
            onOpenXyzDialog = onOpenXyzDialog
        )
    }
}

// =============================================================================
// SECTION EDIT DIALOG ROUTER
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSectionEditDialog(
    sectionKey: String,
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    onDismiss: () -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onOpenXyzDialog: (String, (String) -> Unit) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = themeColors.background,
            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (sectionKey) {
                            "HEADER" -> if (isBn) "ব্যক্তিগত ও যোগাযোগ তথ্য" else "Personal & Contact"
                            "SUMMARY" -> if (isBn) "প্রফেশনাল সামারি" else "Professional Summary"
                            "EXPERIENCE" -> if (isBn) "কাজের অভিজ্ঞতা" else "Work Experience"
                            "EDUCATION" -> if (isBn) "শিক্ষাগত যোগ্যতা" else "Education"
                            "SKILLS" -> if (isBn) "স্কিলস ও দক্ষতা" else "Skills & Competencies"
                            "PROJECTS" -> if (isBn) "প্রজেক্ট ও পোর্টফোলিও" else "Key Projects"
                            "CERTIFICATIONS" -> if (isBn) "সার্টিফিকেশন" else "Certifications"
                            "LANGUAGES" -> if (isBn) "ভাষা ও দক্ষতা" else "Languages"
                            else -> if (isBn) "কাস্টম সেকশন" else "Custom Sections"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = themeColors.displayText
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = themeColors.displayText.copy(alpha = 0.15f)
                )

                // Sub-forms
                Box(modifier = Modifier.weight(1f)) {
                    when (sectionKey) {
                        "HEADER" -> EditHeaderForm(cvData, onCvDataChange, themeColors, isBn)
                        "SUMMARY" -> EditSummaryForm(cvData, onCvDataChange, themeColors, isBn, onOpenXyzDialog)
                        "EXPERIENCE" -> EditExperienceForm(cvData, onCvDataChange, themeColors, isBn, onOpenXyzDialog)
                        "EDUCATION" -> EditEducationForm(cvData, onCvDataChange, themeColors, isBn)
                        "SKILLS" -> EditSkillsForm(cvData, onCvDataChange, themeColors, isBn)
                        "PROJECTS" -> EditProjectsForm(cvData, onCvDataChange, themeColors, isBn)
                        "CERTIFICATIONS" -> EditCertificationsForm(cvData, onCvDataChange, themeColors, isBn)
                        "LANGUAGES" -> EditLanguagesForm(cvData, onCvDataChange, themeColors, isBn)
                        else -> EditCustomSectionsForm(cvData, onCvDataChange, themeColors, isBn)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBn) "সম্পন্ন করুন" else "Save & Close",
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualText
                    )
                }
            }
        }
    }
}

// =============================================================================
// SUB EDIT FORMS
// =============================================================================

@Composable
fun EditHeaderForm(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SmartTextField(
            value = cvData.fullName,
            onValueChange = { onCvDataChange(cvData.copy(fullName = it)) },
            label = if (isBn) "পুরো নাম" else "Full Name",
            themeColors = themeColors
        )
        SmartTextField(
            value = cvData.jobTitle,
            onValueChange = { onCvDataChange(cvData.copy(jobTitle = it)) },
            label = if (isBn) "পদবি / প্রফেশনাল টাইটেল" else "Professional Title / Target Role",
            themeColors = themeColors
        )
        SmartTextField(
            value = cvData.email,
            onValueChange = { onCvDataChange(cvData.copy(email = it)) },
            label = if (isBn) "ইমেইল অ্যাড্রেস" else "Email Address",
            themeColors = themeColors
        )
        SmartTextField(
            value = cvData.phone,
            onValueChange = { onCvDataChange(cvData.copy(phone = it)) },
            label = if (isBn) "ফোন নম্বর" else "Phone Number",
            themeColors = themeColors
        )
        SmartTextField(
            value = cvData.address,
            onValueChange = { onCvDataChange(cvData.copy(address = it)) },
            label = if (isBn) "ঠিকানা / শহর" else "Location (City, Country)",
            themeColors = themeColors
        )
        SmartTextField(
            value = cvData.linkedin,
            onValueChange = { onCvDataChange(cvData.copy(linkedin = it)) },
            label = if (isBn) "লিঙ্কডইন প্রোফাইল লিঙ্ক" else "LinkedIn Profile URL",
            themeColors = themeColors
        )
        SmartTextField(
            value = cvData.githubOrPortfolio,
            onValueChange = { onCvDataChange(cvData.copy(githubOrPortfolio = it)) },
            label = if (isBn) "পোর্টফোলিও / গিটহাব" else "Portfolio / GitHub URL",
            themeColors = themeColors
        )
    }
}

@Composable
fun EditSummaryForm(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onOpenXyzDialog: (String, (String) -> Unit) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBn) "ক্যারিয়ার অবজেক্টিভ বা সামারি" else "Executive Summary / Objective",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = themeColors.displayText.copy(alpha = 0.7f)
            )
            FilledTonalButton(
                onClick = {
                    onOpenXyzDialog(cvData.summary.ifBlank { "Experienced professional in ${cvData.jobTitle.ifBlank { "Management" }}" }) { newText ->
                        onCvDataChange(cvData.copy(summary = newText))
                    }
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f))
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (isBn) "এআই রাইটার ✨" else "AI Enhance ✨", fontSize = 11.sp, color = themeColors.buttonEqualBg)
            }
        }

        OutlinedTextField(
            value = cvData.summary,
            onValueChange = { onCvDataChange(cvData.copy(summary = it)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            ),
            placeholder = {
                Text(
                    text = if (isBn) "আপনার ক্যারিয়ারের মূল অর্জন ও লক্ষ্য সংক্ষেপে লিখুন..." 
                    else "Write a concise, high-impact summary highlighting your core strengths and measurable impact...",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.4f)
                )
            }
        )
    }
}

@Composable
fun EditExperienceForm(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onOpenXyzDialog: (String, (String) -> Unit) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBn) "কাজের অভিজ্ঞতাসমূহ" else "Career History",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = themeColors.displayText
            )
            Button(
                onClick = {
                    val list = cvData.experiences.toMutableList()
                    list.add(0, CvExperienceItem(role = "Executive", company = "New Company", startDate = "2023", endDate = "Present", isCurrent = true))
                    onCvDataChange(cvData.copy(experiences = list))
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "+ অভিজ্ঞতা যোগ" else "+ Add Role", fontSize = 11.sp)
            }
        }

        cvData.experiences.forEachIndexed { index, exp ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonFunctionBg.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "#${index + 1} ${exp.role.ifBlank { "Role" }}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColors.displayText)
                        IconButton(
                            onClick = {
                                val list = cvData.experiences.toMutableList()
                                list.removeAt(index)
                                onCvDataChange(cvData.copy(experiences = list))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }

                    SmartTextField(
                        value = exp.role,
                        onValueChange = { updated ->
                            val list = cvData.experiences.toMutableList()
                            list[index] = exp.copy(role = updated)
                            onCvDataChange(cvData.copy(experiences = list))
                        },
                        label = if (isBn) "পদবি (Role)" else "Job Role / Title",
                        themeColors = themeColors
                    )

                    SmartTextField(
                        value = exp.company,
                        onValueChange = { updated ->
                            val list = cvData.experiences.toMutableList()
                            list[index] = exp.copy(company = updated)
                            onCvDataChange(cvData.copy(experiences = list))
                        },
                        label = if (isBn) "কোম্পানির নাম" else "Company / Organization",
                        themeColors = themeColors
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            SmartTextField(
                                value = exp.startDate,
                                onValueChange = { updated ->
                                    val list = cvData.experiences.toMutableList()
                                    list[index] = exp.copy(startDate = updated)
                                    onCvDataChange(cvData.copy(experiences = list))
                                },
                                label = if (isBn) "শুরু" else "Start Date",
                                themeColors = themeColors
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SmartTextField(
                                value = if (exp.isCurrent) "Present" else exp.endDate,
                                onValueChange = { updated ->
                                    val list = cvData.experiences.toMutableList()
                                    list[index] = exp.copy(endDate = updated, isCurrent = updated.equals("Present", ignoreCase = true))
                                    onCvDataChange(cvData.copy(experiences = list))
                                },
                                label = if (isBn) "শেষ / বর্তমান" else "End Date / Present",
                                themeColors = themeColors
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "কাজের বিবরণ ও বুলেট পয়েন্ট" else "Key Achievements & Bullets",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText.copy(alpha = 0.65f)
                        )
                        FilledTonalButton(
                            onClick = {
                                onOpenXyzDialog(exp.description.ifBlank { "Responsible for ${exp.role} duties and operations at ${exp.company}" }) { newText ->
                                    val list = cvData.experiences.toMutableList()
                                    list[index] = exp.copy(description = newText)
                                    onCvDataChange(cvData.copy(experiences = list))
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f))
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isBn) "Google XYZ এআই ✨" else "Google XYZ AI ✨", fontSize = 10.sp, color = themeColors.buttonEqualBg)
                        }
                    }

                    OutlinedTextField(
                        value = exp.description,
                        onValueChange = { updated ->
                            val list = cvData.experiences.toMutableList()
                            list[index] = exp.copy(description = updated)
                            onCvDataChange(cvData.copy(experiences = list))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EditEducationForm(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (isBn) "ডিগ্রি ও শিক্ষাগত অর্জন" else "Education List", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColors.displayText)
            Button(
                onClick = {
                    val list = cvData.educations.toMutableList()
                    list.add(CvEducationItem(degree = "Bachelor of Science", institution = "University", passingYear = "2022", result = "3.50"))
                    onCvDataChange(cvData.copy(educations = list))
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "+ ডিগ্রি যোগ" else "+ Add Degree", fontSize = 11.sp)
            }
        }

        cvData.educations.forEachIndexed { index, edu ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonFunctionBg.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "#${index + 1} ${edu.degree.ifBlank { "Degree" }}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColors.displayText)
                        IconButton(
                            onClick = {
                                val list = cvData.educations.toMutableList()
                                list.removeAt(index)
                                onCvDataChange(cvData.copy(educations = list))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }

                    SmartTextField(
                        value = edu.degree,
                        onValueChange = { updated ->
                            val list = cvData.educations.toMutableList()
                            list[index] = edu.copy(degree = updated)
                            onCvDataChange(cvData.copy(educations = list))
                        },
                        label = if (isBn) "ডিগ্রির নাম (যেমন: MBA / BBA / B.Sc)" else "Degree Title",
                        themeColors = themeColors
                    )
                    SmartTextField(
                        value = edu.institution,
                        onValueChange = { updated ->
                            val list = cvData.educations.toMutableList()
                            list[index] = edu.copy(institution = updated)
                            onCvDataChange(cvData.copy(educations = list))
                        },
                        label = if (isBn) "শিক্ষা প্রতিষ্ঠান / বিশ্ববিদ্যালয়" else "Institution / University",
                        themeColors = themeColors
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            SmartTextField(
                                value = edu.passingYear,
                                onValueChange = { updated ->
                                    val list = cvData.educations.toMutableList()
                                    list[index] = edu.copy(passingYear = updated)
                                    onCvDataChange(cvData.copy(educations = list))
                                },
                                label = if (isBn) "পাসের সন" else "Passing Year",
                                themeColors = themeColors
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SmartTextField(
                                value = edu.result,
                                onValueChange = { updated ->
                                    val list = cvData.educations.toMutableList()
                                    list[index] = edu.copy(result = updated)
                                    onCvDataChange(cvData.copy(educations = list))
                                },
                                label = if (isBn) "ফলাফল (CGPA / Grade)" else "Result / CGPA",
                                themeColors = themeColors
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditSkillsForm(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    var newSkillName by remember { mutableStateOf("") }
    var newSkillCategory by remember { mutableStateOf("Core Competency") }

    val quickSuggestions = remember {
        listOf(
            "Strategic Planning", "Project Management", "Operations Management",
            "Advanced MS Excel", "Data Analysis", "ERP Software",
            "Sales Pipeline", "B2B Sales", "Territory Management",
            "Cross-functional Leadership", "Customer Relations", "Digital Marketing"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isBn) "দ্রুত স্কিল যোগ করুন (সাজেশন)" else "Quick Skill Suggestions",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.displayText.copy(alpha = 0.7f)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickSuggestions.forEach { suggestion ->
                val alreadyAdded = cvData.skills.any { it.name.equals(suggestion, ignoreCase = true) }
                FilterChip(
                    selected = alreadyAdded,
                    onClick = {
                        val list = cvData.skills.toMutableList()
                        if (alreadyAdded) {
                            list.removeAll { it.name.equals(suggestion, ignoreCase = true) }
                        } else {
                            list.add(CvSkillItem(name = suggestion, category = "Core Competencies"))
                        }
                        onCvDataChange(cvData.copy(skills = list))
                    },
                    label = { Text(text = suggestion, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.2f),
                        selectedLabelColor = themeColors.buttonEqualBg
                    )
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = themeColors.displayText.copy(alpha = 0.15f))

        Text(text = if (isBn) "কাস্টম স্কিল ইনপুট" else "Add Custom Skill", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = themeColors.displayText)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                SmartTextField(
                    value = newSkillName,
                    onValueChange = { newSkillName = it },
                    label = if (isBn) "স্কিলের নাম" else "Skill Name",
                    themeColors = themeColors
                )
            }
            Button(
                onClick = {
                    if (newSkillName.isNotBlank()) {
                        val list = cvData.skills.toMutableList()
                        list.add(CvSkillItem(name = newSkillName.trim(), category = newSkillCategory))
                        onCvDataChange(cvData.copy(skills = list))
                        newSkillName = ""
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Text(text = if (isBn) "যোগ" else "Add")
            }
        }

        Text(text = if (isBn) "বর্তমান স্কিল তালিকা (${cvData.skills.size})" else "Active Skills (${cvData.skills.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColors.displayText)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            cvData.skills.forEachIndexed { index, sk ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themeColors.buttonFunctionBg.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = sk.name, fontSize = 11.5.sp, color = themeColors.displayText)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    val list = cvData.skills.toMutableList()
                                    list.removeAt(index)
                                    onCvDataChange(cvData.copy(skills = list))
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditProjectsForm(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (isBn) "প্রজেক্ট তালিকা" else "Projects List", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColors.displayText)
            Button(
                onClick = {
                    val list = cvData.projects.toMutableList()
                    list.add(CvProjectItem(title = "Key Project", description = "Designed and delivered solution...", link = "https://example.com"))
                    onCvDataChange(cvData.copy(projects = list))
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Text(text = if (isBn) "+ প্রজেক্ট" else "+ Add", fontSize = 11.sp)
            }
        }

        cvData.projects.forEachIndexed { index, pr ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonFunctionBg.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = pr.title.ifBlank { "Project #${index + 1}" }, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = themeColors.displayText)
                        IconButton(
                            onClick = {
                                val list = cvData.projects.toMutableList()
                                list.removeAt(index)
                                onCvDataChange(cvData.copy(projects = list))
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }

                    SmartTextField(
                        value = pr.title,
                        onValueChange = { updated ->
                            val list = cvData.projects.toMutableList()
                            list[index] = pr.copy(title = updated)
                            onCvDataChange(cvData.copy(projects = list))
                        },
                        label = if (isBn) "প্রজেক্ট শিরোনাম" else "Project Title",
                        themeColors = themeColors
                    )
                    SmartTextField(
                        value = pr.link,
                        onValueChange = { updated ->
                            val list = cvData.projects.toMutableList()
                            list[index] = pr.copy(link = updated)
                            onCvDataChange(cvData.copy(projects = list))
                        },
                        label = if (isBn) "লিঙ্ক / ইউআরএল" else "Project URL / Link",
                        themeColors = themeColors
                    )
                    SmartTextField(
                        value = pr.description,
                        onValueChange = { updated ->
                            val list = cvData.projects.toMutableList()
                            list[index] = pr.copy(description = updated)
                            onCvDataChange(cvData.copy(projects = list))
                        },
                        label = if (isBn) "বিবরণ ও ফলাফল" else "Description & Impact",
                        themeColors = themeColors
                    )
                }
            }
        }
    }
}

@Composable
fun EditCertificationsForm(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isBn) "সার্টিফিকেশন ও প্রশিক্ষণ কোর্স (প্রতি লাইনে একটি)" 
            else "Certifications, Accreditations & Licenses (One per line)",
            fontSize = 12.sp,
            color = themeColors.displayText.copy(alpha = 0.7f)
        )
        OutlinedTextField(
            value = cvData.certifications,
            onValueChange = { onCvDataChange(cvData.copy(certifications = it)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(10.dp),
            placeholder = {
                Text(
                    text = "• Certified Lean Six Sigma Green Belt (2023)\n• Project Management Professional (PMP)\n• Advanced Excel for Business - Coursera",
                    fontSize = 11.5.sp,
                    color = themeColors.displayText.copy(alpha = 0.4f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.buttonEqualBg,
                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                focusedTextColor = themeColors.displayText,
                unfocusedTextColor = themeColors.displayText
            )
        )
    }
}

@Composable
fun EditLanguagesForm(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isBn) "ভাষাগত দক্ষতা" else "Language Fluency",
            fontSize = 12.sp,
            color = themeColors.displayText.copy(alpha = 0.7f)
        )
        SmartTextField(
            value = cvData.languages,
            onValueChange = { onCvDataChange(cvData.copy(languages = it)) },
            label = if (isBn) "ভাষা ও পর্যায় (যেমন: English - Fluent, Bengali - Native)" else "Languages (e.g., English - Professional, Bengali - Native)",
            themeColors = themeColors
        )
    }
}

@Composable
fun EditCustomSectionsForm(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (isBn) "কাস্টম সেকশন তালিকা" else "Custom Sections", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColors.displayText)
            Button(
                onClick = {
                    val list = cvData.customSections.toMutableList()
                    list.add(CvCustomSectionItem(title = "Volunteer Work", content = "Led blood donation campaign..."))
                    onCvDataChange(cvData.copy(customSections = list))
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Text(text = if (isBn) "+ কাস্টম ব্লক" else "+ Add Block", fontSize = 11.sp)
            }
        }

        cvData.customSections.forEachIndexed { index, cs ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonFunctionBg.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = cs.title.ifBlank { "Custom #${index + 1}" }, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = themeColors.displayText)
                        IconButton(
                            onClick = {
                                val list = cvData.customSections.toMutableList()
                                list.removeAt(index)
                                onCvDataChange(cvData.copy(customSections = list))
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }

                    SmartTextField(
                        value = cs.title,
                        onValueChange = { updated ->
                            val list = cvData.customSections.toMutableList()
                            list[index] = cs.copy(title = updated)
                            onCvDataChange(cvData.copy(customSections = list))
                        },
                        label = if (isBn) "সেকশনের শিরোনাম" else "Section Title",
                        themeColors = themeColors
                    )
                    SmartTextField(
                        value = cs.content,
                        onValueChange = { updated ->
                            val list = cvData.customSections.toMutableList()
                            list[index] = cs.copy(content = updated)
                            onCvDataChange(cvData.copy(customSections = list))
                        },
                        label = if (isBn) "কন্টেন্ট বা বিবরণ" else "Content",
                        themeColors = themeColors
                    )
                }
            }
        }
    }
}

// =============================================================================
// GOOGLE XYZ AI BULLET REWRITER MODAL
// =============================================================================

@Composable
fun SmartGoogleXyzModal(
    rawText: String,
    targetRole: String,
    onApply: (String) -> Unit,
    onDismiss: () -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    var isGenerating by remember { mutableStateOf(false) }
    var variations by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentRaw by remember { mutableStateOf(rawText) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (currentRaw.isNotBlank()) {
            isGenerating = true
            variations = SmartCvAiCopilot.generateXyzBulletPoints(currentRaw, targetRole)
            isGenerating = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = themeColors.background,
            border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = themeColors.buttonEqualBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "গুগল XYZ এআই রাইটার" else "Google XYZ AI Writer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = themeColors.displayText
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText)
                    }
                }

                Text(
                    text = if (isBn) "গুগলের ফর্মুলা: Accomplished [X] as measured by [Y], by doing [Z]"
                    else "Google Formula: Accomplished [X] as measured by [Y], by doing [Z]",
                    fontSize = 11.sp,
                    color = themeColors.buttonEqualBg,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                OutlinedTextField(
                    value = currentRaw,
                    onValueChange = { currentRaw = it },
                    label = { Text(if (isBn) "আপনার সাধারণ বাক্য" else "Raw Achievement Line", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isGenerating = true
                            variations = SmartCvAiCopilot.generateXyzBulletPoints(currentRaw, targetRole)
                            isGenerating = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    enabled = !isGenerating && currentRaw.isNotBlank()
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isBn) "তৈরি হচ্ছে..." else "Generating XYZ Bullets...", fontSize = 12.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isBn) "৩টি ভ্যারিয়েশন তৈরি করুন" else "Generate 3 High-Impact Variations", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isBn) "যেকোনো একটিতে ট্যাপ করে সিভিতে যুক্ত করুন:" else "Tap any variation to apply directly:",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(variations) { variation ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = themeColors.buttonFunctionBg.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onApply(variation)
                                    onDismiss()
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = variation,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = themeColors.displayText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = if (isBn) "ট্যাপ করে সিলেক্ট করুন ➔" else "Tap to Apply ➔",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.buttonEqualBg
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper generic text field
@Composable
fun SmartTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    themeColors: CalculatorThemeColors
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = themeColors.buttonEqualBg,
            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
            focusedTextColor = themeColors.displayText,
            unfocusedTextColor = themeColors.displayText
        ),
        singleLine = true
    )
}

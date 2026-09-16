package com.example.ui.screens.tools

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun CvEducationSection(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    onRequestAiPrompt: (title: String, defaultPrompt: String, targetField: String, expIdx: Int) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    isLiveEdit: Boolean = false
) {
    var deleteConfirmDialogState by remember { mutableStateOf<DeleteConfirmState?>(null) }

    if (deleteConfirmDialogState != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmDialogState = null },
            title = {
                Text(
                    text = deleteConfirmDialogState!!.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = themeColors.displayText
                )
            },
            text = {
                Text(
                    text = deleteConfirmDialogState!!.message,
                    fontSize = 14.sp,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteConfirmDialogState!!.onConfirm()
                        deleteConfirmDialogState = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                ) {
                    Text(text = if (isBn) "হ্যাঁ, ডিলিট করুন" else "Yes, Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteConfirmDialogState = null }
                ) {
                    Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText.copy(alpha = 0.6f))
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // EDUCATION CARD BLOCK
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionCardHeader(
            title = if (isBn) "শিক্ষাগত যোগ্যতা" else "Education Details",
            icon = Icons.Default.School,
            themeColors = themeColors,
            modifier = Modifier.weight(1f, fill = false)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                onClick = {
                    onRequestAiPrompt(
                        if (isBn) "শিক্ষাগত যোগ্যতা এআই নির্দেশনা" else "Education Details AI Prompt",
                        "Generate my educational background details for ${cvData.jobTitle.ifBlank { "Professional" }} profile...",
                        "EDUCATION",
                        -1
                    )
                },
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonEqualBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "এআই জেনারেট" else "AI Generate", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Surface(
                onClick = {
                    val newList = cvData.educations.toMutableList()
                    newList.add(CvEducationItem(
                        examLevel = "B.Sc.",
                        degree = "B.Sc. in Computer Science & Engineering",
                        subjectMajor = "Computer Science & Engineering (CSE)",
                        institution = "University of Dhaka (DU)",
                        passingYear = "2022",
                        resultType = "CGPA (Out of 4.0)"
                    ))
                    onCvDataChange(cvData.copy(educations = newList))
                },
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonEqualBg.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = if (isBn) "যুক্ত করুন" else "Add", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    cvData.educations.forEachIndexed { index, edu ->
        var currentExam by remember(edu.examLevel, edu.degree) {
            mutableStateOf(
                if (CV_EXAM_OPTIONS.contains(edu.examLevel)) edu.examLevel
                else if (edu.degree.isNotBlank() && CV_EXAM_OPTIONS.contains(edu.degree)) edu.degree
                else if (edu.examLevel == "Others" || edu.degree.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                else "B.Sc."
            )
        }
        var currentSubject by remember(edu.subjectMajor) {
            mutableStateOf(
                if (CV_SUBJECT_OPTIONS.contains(edu.subjectMajor)) edu.subjectMajor
                else if (edu.subjectMajor.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                else "General"
            )
        }
        var currentInst by remember(edu.institution) {
            mutableStateOf(
                if (CV_INST_OPTIONS.contains(edu.institution)) edu.institution
                else if (edu.institution.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                else "Others (ম্যানুয়াল ইনপুট)"
            )
        }
        var currentYear by remember(edu.passingYear) {
            mutableStateOf(
                if (CV_YEAR_OPTIONS.contains(edu.passingYear)) edu.passingYear
                else if (edu.passingYear.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                else "2021"
            )
        }
        var currentResultSys by remember(edu.resultType) {
            mutableStateOf(
                if (CV_RESULT_SYS_OPTIONS.contains(edu.resultType)) edu.resultType
                else if (edu.resultType.isNotBlank()) "Others (ম্যানুয়াল ইনপুট)"
                else "CGPA (Out of 4.0)"
            )
        }

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
                        text = "Education Entry #${index + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = themeColors.buttonEqualBg,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = {
                            deleteConfirmDialogState = DeleteConfirmState(
                                title = if (isBn) "শিক্ষাগত যোগ্যতা ডিলিট করার নিশ্চয়তা" else "Confirm Education Deletion",
                                message = if (isBn) "আপনি কি নিশ্চিত যে এই শিক্ষাগত যোগ্যতাটি ডিলিট করতে চান?" else "Are you sure you want to delete this educational qualification?",
                                onConfirm = {
                                    val newList = cvData.educations.toMutableList()
                                    newList.removeAt(index)
                                    onCvDataChange(cvData.copy(educations = newList))
                                }
                            )
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    CvCustomDropdown(
                        label = if (isBn) "পরীক্ষা/ডিগ্রি লেভেল" else "Exam / Degree Level",
                        selectedValue = currentExam,
                        options = CV_EXAM_OPTIONS,
                        onValueChange = { selected ->
                            currentExam = selected
                            val newExamVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") "Others" else selected
                            val isSecondaryLevel = selected in listOf("S.S.C", "H.S.C", "Dakhil", "Alim", "O-Level", "A-Level", "S.S.C (Vocational)", "H.S.C (Vocational)", "S.S.C (Business Management)", "H.S.C (BM / BMT)")
                            val computedDegree = if (selected == "Others (ম্যানুয়াল ইনপুট)") edu.degree
                                                 else if (isSecondaryLevel) selected
                                                 else if (currentSubject != "General" && currentSubject != "Others (ম্যানুয়াল ইনপুট)") "$selected in $currentSubject"
                                                 else selected
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(examLevel = newExamVal, degree = computedDegree)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CvCustomDropdown(
                        label = if (isBn) "বিষয়/গ্রুপ/ডিপার্টমেন্ট" else "Subject / Department",
                        selectedValue = currentSubject,
                        options = CV_SUBJECT_OPTIONS,
                        onValueChange = { selected ->
                            currentSubject = selected
                            val newSubVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") "Others" else selected
                            val isSecondaryLevel = currentExam in listOf("S.S.C", "H.S.C", "Dakhil", "Alim", "O-Level", "A-Level", "S.S.C (Vocational)", "H.S.C (Vocational)", "S.S.C (Business Management)", "H.S.C (BM / BMT)")
                            val computedDegree = if (currentExam != "Others (ম্যানুয়াল ইনপুট)" && selected != "General" && selected != "Others (ম্যানুয়াল ইনপুট)") {
                                if (isSecondaryLevel) currentExam else "$currentExam in $selected"
                            } else edu.degree
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(subjectMajor = newSubVal, degree = computedDegree)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (currentExam == "Others (ম্যানুয়াল ইনপুট)") {
                    Spacer(modifier = Modifier.height(6.dp))
                    CvCustomTextField(
                        label = if (isBn) "ম্যানুয়াল ডিগ্রির নাম লিখুন" else "Enter Manual Degree Title",
                        value = edu.degree,
                        onValueChange = { d ->
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(degree = d)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                    )
                }

                if (currentSubject == "Others (ম্যানুয়াল ইনপুট)") {
                    Spacer(modifier = Modifier.height(6.dp))
                    CvCustomTextField(
                        label = if (isBn) "ম্যানুয়াল বিষয়ের নাম লিখুন" else "Enter Manual Subject Name",
                        value = if (edu.subjectMajor == "Others") "" else edu.subjectMajor,
                        onValueChange = { sub ->
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(subjectMajor = sub)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                CvCustomDropdown(
                    label = if (isBn) "বোর্ড / বিশ্ববিদ্যালয় / প্রতিষ্ঠান" else "Board / University / Institution",
                    selectedValue = currentInst,
                    options = CV_INST_OPTIONS,
                    onValueChange = { selected ->
                        currentInst = selected
                        val instVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") edu.institution else selected
                        val newList = cvData.educations.toMutableList()
                        newList[index] = edu.copy(institution = instVal)
                        onCvDataChange(cvData.copy(educations = newList))
                    },
                    themeColors = themeColors
                )

                if (currentInst == "Others (ম্যানুয়াল ইনপুট)") {
                    Spacer(modifier = Modifier.height(6.dp))
                    CvCustomTextField(
                        label = if (isBn) "ম্যানুয়াল প্রতিষ্ঠানের নাম লিখুন" else "Enter Manual Institution Name",
                        value = edu.institution,
                        onValueChange = { inst ->
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(institution = inst)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    CvCustomDropdown(
                        label = if (isBn) "পাসের বছর" else "Passing Year",
                        selectedValue = currentYear,
                        options = CV_YEAR_OPTIONS,
                        onValueChange = { selected ->
                            currentYear = selected
                            val yrVal = if (selected == "Others (ম্যানুয়াল ইনপুট)") edu.passingYear else selected
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(passingYear = yrVal)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CvCustomDropdown(
                        label = if (isBn) "ফলাফল পদ্ধতি" else "Result Type",
                        selectedValue = currentResultSys,
                        options = CV_RESULT_SYS_OPTIONS,
                        onValueChange = { selected ->
                            currentResultSys = selected
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(resultType = selected)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (currentYear == "Others (ম্যানুয়াল ইনপুট)") {
                    Spacer(modifier = Modifier.height(6.dp))
                    CvCustomTextField(
                        label = if (isBn) "ম্যানুয়াল বছর/স্ট্যাটাস লিখুন" else "Enter Manual Passing Year/Status",
                        value = edu.passingYear,
                        onValueChange = { yr ->
                            val newList = cvData.educations.toMutableList()
                            newList[index] = edu.copy(passingYear = yr)
                            onCvDataChange(cvData.copy(educations = newList))
                        },
                        themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                CvCustomTextField(
                    label = if (isBn) "প্রাপ্ত পয়েন্ট / ডিভিশন (যেমন: 3.85)" else "GPA / Score / Division (e.g., 3.85)",
                    value = edu.result,
                    onValueChange = { r ->
                        val newList = cvData.educations.toMutableList()
                        newList[index] = edu.copy(result = r)
                        onCvDataChange(cvData.copy(educations = newList))
                    },
                    themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
                    placeholderText = if (isBn) "যেমন: 3.80" else "e.g., 3.80"
                )
            }
        }
    }
}

@Composable
fun CvSkillsSection(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    onRequestAiPrompt: (title: String, defaultPrompt: String, targetField: String, expIdx: Int) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    isLiveEdit: Boolean = false
) {
    val context = LocalContext.current
    var deleteConfirmDialogState by remember { mutableStateOf<DeleteConfirmState?>(null) }
    var localAddedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var showAddCategoryMenu by remember { mutableStateOf(false) }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }
    var showBulkSkillDialog by remember { mutableStateOf(false) }

    if (deleteConfirmDialogState != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmDialogState = null },
            title = {
                Text(
                    text = deleteConfirmDialogState!!.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = themeColors.displayText
                )
            },
            text = {
                Text(
                    text = deleteConfirmDialogState!!.message,
                    fontSize = 14.sp,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteConfirmDialogState!!.onConfirm()
                        deleteConfirmDialogState = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                ) {
                    Text(text = if (isBn) "হ্যাঁ, ডিলিট করুন" else "Yes, Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteConfirmDialogState = null }
                ) {
                    Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText.copy(alpha = 0.6f))
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(14.dp)
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCardHeader(
                title = if (isBn) "কী স্কিল এবং পারদর্শিতা (${cvData.skills.size})" else "Key Skill & Competence (${cvData.skills.size})",
                icon = Icons.Default.Code,
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )

            // Add Category button
            Box {
                Surface(
                    onClick = { showAddCategoryMenu = true },
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.buttonEqualBg.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = if (isBn) "ক্যাটাগরি" else "Category", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                    }
                }

                DropdownMenu(
                    expanded = showAddCategoryMenu,
                    onDismissRequest = { showAddCategoryMenu = false },
                    modifier = Modifier.background(themeColors.cardBg)
                ) {
                    val availableCategories = SKILL_CATEGORY_LIBRARY.keys.toList() + "Custom / অন্যান্য"
                    availableCategories.forEach { categoryName ->
                        DropdownMenuItem(
                            text = { Text(text = categoryName, fontSize = 12.sp, color = themeColors.displayText) },
                            onClick = {
                                showAddCategoryMenu = false
                                if (categoryName == "Custom / অন্যান্য") {
                                    showCustomCategoryDialog = true
                                } else {
                                    if (!localAddedCategories.contains(categoryName)) {
                                        localAddedCategories = localAddedCategories + categoryName
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Show/Hide Skill Description switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(themeColors.displayText.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isBn) "বিবরণ" else "Desc",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = cvData.showSkillDescriptions,
                    onCheckedChange = { onCvDataChange(cvData.copy(showSkillDescriptions = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = themeColors.buttonEqualBg,
                        checkedTrackColor = themeColors.buttonEqualBg.copy(alpha = 0.4f),
                        uncheckedThumbColor = themeColors.displayText.copy(alpha = 0.5f),
                        uncheckedTrackColor = themeColors.displayText.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.scale(0.7f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                onClick = { showBulkSkillDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "স্মার্ট পেস্ট" else "Smart Paste", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }

            Surface(
                onClick = {
                    onRequestAiPrompt(
                        if (isBn) "কী স্কিল এআই দিয়ে জেনারেট করুন" else "Generate Key Skills with AI",
                        "Generate 6 professional key skills with short descriptions (format: Title: Description) for a ${cvData.jobTitle.ifBlank { "Professional" }} candidate.",
                        "SKILLS",
                        -1
                    )
                },
                shape = RoundedCornerShape(12.dp),
                color = themeColors.buttonEqualBg,
                border = BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "এআই জেনারেট" else "AI Generate", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    if (showCustomCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCustomCategoryDialog = false },
            title = { Text(text = if (isBn) "নতুন কাস্টম ক্যাটাগরি" else "New Custom Category", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText) },
            text = {
                OutlinedTextField(
                    value = customCategoryInput,
                    onValueChange = { customCategoryInput = it },
                    placeholder = { Text(text = if (isBn) "যেমন: Soft Skills & Leadership" else "e.g., Soft Skills & Leadership") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customCategoryInput.isNotBlank()) {
                            val catName = customCategoryInput.trim()
                            if (!localAddedCategories.contains(catName)) {
                                localAddedCategories = localAddedCategories + catName
                            }
                            customCategoryInput = ""
                            showCustomCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(text = if (isBn) "যোগ করুন" else "Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomCategoryDialog = false }) {
                    Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg
        )
    }

    if (showBulkSkillDialog) {
        CvSkillBulkPasteDialog(
            isBn = isBn,
            themeColors = themeColors,
            currentSkillCount = cvData.skills.size,
            onDismiss = { showBulkSkillDialog = false },
            onApplySkills = { newSkills, replaceAll ->
                val updated = if (replaceAll) newSkills else (cvData.skills + newSkills)
                val hasAnyDesc = updated.any { it.description.isNotBlank() }
                onCvDataChange(
                    cvData.copy(
                        skills = updated,
                        showSkillDescriptions = if (hasAnyDesc) true else cvData.showSkillDescriptions,
                        skillDisplayStyle = if (hasAnyDesc && cvData.skillDisplayStyle == "GROUPED_COMMA") "BULLET_WITH_DESC" else cvData.skillDisplayStyle
                    )
                )
                Toast.makeText(context, if (isBn) "${newSkills.size}টি স্কিল সিভিতে যুক্ত হয়েছে!" else "Added ${newSkills.size} skills to CV!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = if (isBn) "টিপস: প্রতিটি ফিল্ডে 'টাইটেল: বিবরণ' লিখলে সিভিতে টাইটেলটি অটোমেটিক বোল্ড হয়ে যাবে।" else "Tip: Use 'Title: Description' format in any field to automatically bold the title in CV output.",
        fontSize = 10.5.sp,
        color = themeColors.displayText.copy(alpha = 0.65f),
        modifier = Modifier.padding(bottom = 8.dp)
    )

    val activeCategories = (cvData.skills.map {
        val resolvedCat = it.category.ifBlank { findBestCategoryForSkill(it.name) }
        normalizeCategoryName(resolvedCat)
    } + localAddedCategories).distinct()

    if (activeCategories.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = themeColors.displayText.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isBn) "কোনো স্কিল ক্যাটাগরি যোগ করা হয়নি।" else "No skill categories added yet.",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = themeColors.displayText.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = { showBulkSkillDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBn) "জেমিনি বা টেক্সট থেকে পেস্ট করুন" else "Paste from Gemini / Text", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        activeCategories.forEach { cat ->
            val skillsInCat = cvData.skills.filter {
                val resolvedCat = it.category.ifBlank { findBestCategoryForSkill(it.name) }
                val finalCat = normalizeCategoryName(resolvedCat)
                finalCat == cat
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f)),
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
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Arrow DropDown to change category
                            var showChangeCategoryMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { showChangeCategoryMenu = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Change Category",
                                        tint = themeColors.displayText.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showChangeCategoryMenu,
                                    onDismissRequest = { showChangeCategoryMenu = false },
                                    modifier = Modifier.background(themeColors.cardBg)
                                ) {
                                    val availableCategories = SKILL_CATEGORY_LIBRARY.keys.toList()
                                    availableCategories.forEach { targetCat ->
                                        if (targetCat != cat) {
                                            DropdownMenuItem(
                                                text = { Text(text = targetCat, fontSize = 11.sp, color = themeColors.displayText) },
                                                onClick = {
                                                    showChangeCategoryMenu = false
                                                    val updatedSkills = cvData.skills.map { sk ->
                                                        val currentResolvedCat = sk.category.ifBlank { findBestCategoryForSkill(sk.name) }
                                                        val finalResolvedCat = normalizeCategoryName(currentResolvedCat)
                                                        if (finalResolvedCat == cat) {
                                                            sk.copy(category = targetCat)
                                                        } else {
                                                            sk
                                                        }
                                                    }
                                                    onCvDataChange(cvData.copy(skills = updatedSkills))
                                                    Toast.makeText(context, if (isBn) "ক্যাটাগরি পরিবর্তন করা হয়েছে" else "Category updated to $targetCat", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    localAddedCategories = localAddedCategories.filter { it != cat }
                                    val remainingSkills = cvData.skills.filter { sk ->
                                        val resolvedCat = sk.category.ifBlank { findBestCategoryForSkill(sk.name) }
                                        val finalCat = normalizeCategoryName(resolvedCat)
                                        finalCat != cat
                                    }
                                    onCvDataChange(cvData.copy(skills = remainingSkills))
                                    Toast.makeText(context, if (isBn) "'$cat' ক্যাটাগরি মুছে ফেলা হয়েছে" else "Removed category '$cat'", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Category",
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (skillsInCat.isEmpty()) {
                        Text(
                            text = if (isBn) "এই ক্যাটাগরিতে কোনো স্কিল নেই। নিচের বাটনে ক্লিক করে যোগ করুন।" else "No skills in this category. Click the button below to add.",
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        skillsInCat.forEach { sk ->
                            val originalIdx = cvData.skills.indexOfFirst { it.id == sk.id }
                            if (originalIdx != -1) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .background(
                                            color = themeColors.displayText.copy(alpha = 0.02f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 0.5.dp,
                                            color = themeColors.displayText.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))

                                            if (sk.name.isBlank()) {
                                                OutlinedTextField(
                                                    value = sk.name,
                                                    onValueChange = { text ->
                                                        val newList = cvData.skills.toMutableList()
                                                        newList[originalIdx] = sk.copy(name = text)
                                                        onCvDataChange(cvData.copy(skills = newList))
                                                    },
                                                    placeholder = { Text(if (isBn) "স্কিল টাইটেল লিখুন" else "Enter Skill Title", fontSize = 11.5.sp) },
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = themeColors.buttonEqualBg,
                                                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                                                        focusedTextColor = themeColors.displayText,
                                                        unfocusedTextColor = themeColors.displayText
                                                    ),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(42.dp),
                                                    textStyle = TextStyle(fontSize = 11.5.sp)
                                                )
                                            } else {
                                                Text(
                                                    text = sk.name,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = themeColors.displayText,
                                                    modifier = Modifier.clickable {
                                                        val newList = cvData.skills.toMutableList()
                                                        newList[originalIdx] = sk.copy(name = "")
                                                        onCvDataChange(cvData.copy(skills = newList))
                                                    }
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                deleteConfirmDialogState = DeleteConfirmState(
                                                    title = if (isBn) "স্কিল মুছে ফেলার নিশ্চয়তা" else "Confirm Skill Deletion",
                                                    message = if (isBn) "আপনি কি নিশ্চিত যে '${sk.name}' স্কিলটি ডিলিট করতে চান?" else "Are you sure you want to delete the skill '${sk.name}'?",
                                                    onConfirm = {
                                                        val newList = cvData.skills.toMutableList()
                                                        newList.removeAt(originalIdx)
                                                        onCvDataChange(cvData.copy(skills = newList))
                                                    }
                                                )
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Skill",
                                                tint = Color.Red.copy(alpha = 0.6f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    if (cvData.showSkillDescriptions) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        CvCustomTextField(
                                            label = if (isBn) "দক্ষতার বিবরণ (ঐচ্ছিক)" else "Competency Description (Optional)",
                                            value = sk.description,
                                            onValueChange = { desc ->
                                                val newList = cvData.skills.toMutableList()
                                                newList[originalIdx] = sk.copy(description = desc)
                                                onCvDataChange(cvData.copy(skills = newList))
                                            },
                                            themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn,
                                            placeholderText = if (isBn) "যেমন: ১+ বছরের অভিজ্ঞতা এবং প্রজেক্টে সফল ব্যবহার।" else "e.g., 1+ years hands-on experience and successful production use.",
                                            onAiPrompt = { onRequestAiPrompt("Skill Suggestion", "Suggest a professional resume competency bullet description for the skill '${sk.name}' under the category '${sk.category}' for a ${cvData.jobTitle} candidate...", "SKILLS_SINGLE", originalIdx) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    var showSkillSelectorMenu by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.align(Alignment.End)) {
                        Surface(
                            onClick = { showSkillSelectorMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            color = themeColors.buttonEqualBg.copy(alpha = 0.08f),
                            border = BorderStroke(0.5.dp, themeColors.buttonEqualBg.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isBn) "স্কিল যোগ করুন" else "Add Skill",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSkillSelectorMenu,
                            onDismissRequest = { showSkillSelectorMenu = false },
                            modifier = Modifier.background(themeColors.cardBg)
                        ) {
                            val predefinedSkills = SKILL_CATEGORY_LIBRARY[cat] ?: emptyList()
                            val options = predefinedSkills + "Others (ম্যানুয়াল ইনপুট)"
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(text = option, fontSize = 12.sp, color = themeColors.displayText) },
                                    onClick = {
                                        showSkillSelectorMenu = false
                                        val newList = cvData.skills.toMutableList()
                                        if (option == "Others (ম্যানুয়াল ইনপুট)") {
                                            newList.add(CvSkillItem(name = "", category = cat))
                                        } else {
                                            newList.add(CvSkillItem(name = option, category = cat))
                                        }
                                        onCvDataChange(cvData.copy(skills = newList))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CvCertificationsAndReferencesSection(
    cvData: CvData,
    onCvDataChange: (CvData) -> Unit,
    onRequestAiPrompt: (title: String, defaultPrompt: String, targetField: String, expIdx: Int) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    isLiveEdit: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        SectionCardHeader(
            title = if (isBn) "ট্রেনিং ও সার্টিফিকেট" else "Training & Certifications",
            icon = Icons.Default.Verified,
            themeColors = themeColors,
            modifier = Modifier.weight(1f, fill = false)
        )
        Surface(
            onClick = {
                onRequestAiPrompt(
                    if (isBn) "ট্রেনিং ও সার্টিফিকেট এআই নির্দেশনা" else "Training & Certifications AI Prompt",
                    "Generate a list of 3 relevant professional certifications and training for a ${cvData.jobTitle.ifBlank { "Professional" }} candidate...",
                    "CERTIFICATIONS",
                    -1
                )
            },
            shape = RoundedCornerShape(12.dp),
            color = themeColors.buttonEqualBg
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "এআই জেনারেট" else "AI Generate", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
        value = cvData.certifications,
        onValueChange = { onCvDataChange(cvData.copy(certifications = it)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 15,
        placeholder = { Text(text = "• Project Management Professional (PMP) - PMI, 2024\n• Business Intelligence - Coursera, 2023", fontSize = 11.5.sp) },
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

    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        SectionCardHeader(
            title = if (isBn) "রেফারেন্স (References)" else "References",
            icon = Icons.Default.SupervisorAccount,
            themeColors = themeColors,
            modifier = Modifier.weight(1f, fill = false)
        )
        Surface(
            onClick = {
                onRequestAiPrompt(
                    if (isBn) "রেফারেন্স এআই নির্দেশনা" else "References AI Prompt",
                    "Suggest 2 professional reference placeholders or typical reference format for a ${cvData.jobTitle.ifBlank { "Professional" }} candidate...",
                    "REFERENCES",
                    -1
                )
            },
            shape = RoundedCornerShape(12.dp),
            color = themeColors.buttonEqualBg
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBn) "এআই জেনারেট" else "AI Generate", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
        value = cvData.references,
        onValueChange = { onCvDataChange(cvData.copy(references = it)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 10,
        placeholder = { Text(text = "Available upon request.", fontSize = 11.5.sp) },
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

    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (isBn) "ভাষাগত দক্ষতা (Languages)" else "Language Fluency",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = themeColors.displayText.copy(alpha = 0.8f)
        )
        Button(
            onClick = {
                onRequestAiPrompt(
                    if (isBn) "ভাষা দক্ষতা এআই নির্দেশনা" else "Language Fluency AI Prompt",
                    "Suggest language fluency levels (e.g., Bengali: Native, English: Professional) for a candidate...",
                    "LANGUAGES",
                    -1
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (isBn) "AI সাজেশন" else "AI Suggest", fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
    CvCustomTextField(
        label = "",
        value = cvData.languages,
        onValueChange = { onCvDataChange(cvData.copy(languages = it)) },
        themeColors = themeColors, isLiveEdit = isLiveEdit, isBn = isBn
    )
}

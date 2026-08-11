package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.LanguageManager
import java.text.DecimalFormat

// --- GPA Calculator (জিপিএ ক্যালকুলেটর) ---
data class GpaSubject(
    val id: Int,
    var name: String,
    var gradePoint: String,
    var credits: String
)

@Composable
fun GpaCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var scale by remember { mutableStateOf("5.0") } // "5.0" or "4.0"
    
    var subjects by remember {
        mutableStateOf(
            listOf(
                GpaSubject(1, "Subject 1", "5.0", "1.0"),
                GpaSubject(2, "Subject 2", "4.0", "1.0"),
                GpaSubject(3, "Subject 3", "3.5", "1.0")
            )
        )
    }

    // Calculation
    var totalPoints = 0.0
    var totalCredits = 0.0
    subjects.forEach { sub ->
        val gp = sub.gradePoint.toDoubleOrNull() ?: 0.0
        val cr = sub.credits.toDoubleOrNull() ?: 1.0
        totalPoints += (gp * cr)
        totalCredits += cr
    }
    val gpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0
    val df = DecimalFormat("0.00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "জিপিএ ক্যালকুলেটর (GPA Calculator)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Scale Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColors.background, RoundedCornerShape(8.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("5.0", "4.0").forEach { sc ->
                val isSelected = scale == sc
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) themeColors.buttonEqualBg else Color.Transparent)
                        .clickable { 
                            scale = sc 
                            // Update grade points based on scale reset
                            val defaultGp = if (sc == "5.0") "5.0" else "4.0"
                            subjects = subjects.map { it.copy(gradePoint = defaultGp) }
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (sc == "5.0") "৫.০ স্কেল (স্কুল/কলেজ)" else "৪.০ স্কেল (বিশ্ববিদ্যালয়)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subjects List
        subjects.forEachIndexed { index, sub ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Name Field (Compact)
                CustomOutlinedTextField(
                    value = sub.name,
                    onValueChange = { newValue ->
                        subjects = subjects.map { if (it.id == sub.id) it.copy(name = newValue) else it }
                    },
                    label = "বিষয় ${index + 1}",
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Text
                )

                // Grade Point
                CustomOutlinedTextField(
                    value = sub.gradePoint,
                    onValueChange = { newValue ->
                        subjects = subjects.map { if (it.id == sub.id) it.copy(gradePoint = newValue) else it }
                    },
                    label = "পয়েন্ট (GP)",
                    themeColors = themeColors,
                    modifier = Modifier.weight(0.8f)
                )

                // Credits
                CustomOutlinedTextField(
                    value = sub.credits,
                    onValueChange = { newValue ->
                        subjects = subjects.map { if (it.id == sub.id) it.copy(credits = newValue) else it }
                    },
                    label = "ক্রেডিট",
                    themeColors = themeColors,
                    modifier = Modifier.weight(0.7f)
                )

                // Remove Button
                IconButton(
                    onClick = {
                        if (subjects.size > 1) {
                            subjects = subjects.filter { it.id != sub.id }
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Subject",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Add Subject Button
        Button(
            onClick = {
                val nextId = (subjects.maxOfOrNull { it.id } ?: 0) + 1
                val defaultGp = if (scale == "5.0") "5.0" else "4.0"
                subjects = subjects + GpaSubject(nextId, "Subject $nextId", defaultGp, "1.0")
            },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.background),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Subject",
                tint = themeColors.displayText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "নতুন বিষয় যোগ করুন",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = themeColors.displayText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GPA Results Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "আপনার জিপিএ (Semester GPA):",
                            fontSize = 12.sp,
                            color = themeColors.displayText
                        )
                        Text(
                            text = "${df.format(gpa)} / $scale",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    gpa >= (scale.toDouble() - 0.01) -> Color(0xFF10B981)
                                    gpa >= 3.0 -> Color(0xFF3B82F6)
                                    gpa >= 2.0 -> Color(0xFFF59E0B)
                                    else -> Color.Red
                                }.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when {
                                gpa >= 5.0 -> "অসাধারণ (A+)"
                                gpa >= 4.0 -> if (scale == "4.0") "অসাধারণ (A/A+)" else "খুব ভালো (A)"
                                gpa >= 3.5 -> "ভালো (A-)"
                                gpa >= 3.0 -> "মাঝারি (B)"
                                gpa >= 2.0 -> "উত্তীর্ণ (Pass)"
                                gpa > 0.0 -> "অনুত্তীর্ণ (F)"
                                else -> "কোনো ডেটা নেই"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                gpa >= (scale.toDouble() - 0.01) -> Color(0xFF10B981)
                                gpa >= 3.0 -> Color(0xFF3B82F6)
                                gpa >= 2.0 -> Color(0xFFF59E0B)
                                else -> Color.Red
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "মোট বিষয়: ${subjects.size} টি",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "মোট ক্রেডিট/ওয়েট: $totalCredits",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// --- CGPA Calculator (সিজিপিএ ক্যালকুলেটর) ---
data class CgpaSemester(
    val id: Int,
    var name: String,
    var sgpa: String,
    var credits: String
)

@Composable
fun CgpaCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var semesters by remember {
        mutableStateOf(
            listOf(
                CgpaSemester(1, "Semester 1", "3.80", "15.0"),
                CgpaSemester(2, "Semester 2", "3.90", "15.0")
            )
        )
    }

    // Calculations
    var totalPoints = 0.0
    var totalCredits = 0.0
    semesters.forEach { sem ->
        val sg = sem.sgpa.toDoubleOrNull() ?: 0.0
        val cr = sem.credits.toDoubleOrNull() ?: 0.0
        totalPoints += (sg * cr)
        totalCredits += cr
    }
    val cgpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0
    val df = DecimalFormat("0.00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "সিজিপিএ ক্যালকুলেটর (CGPA Calculator)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Semester list
        semesters.forEachIndexed { index, sem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomOutlinedTextField(
                    value = sem.name,
                    onValueChange = { newValue ->
                        semesters = semesters.map { if (it.id == sem.id) it.copy(name = newValue) else it }
                    },
                    label = "সেমিস্টার ${index + 1}",
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Text
                )

                CustomOutlinedTextField(
                    value = sem.sgpa,
                    onValueChange = { newValue ->
                        semesters = semesters.map { if (it.id == sem.id) it.copy(sgpa = newValue) else it }
                    },
                    label = "এসজিপিএ (SGPA)",
                    themeColors = themeColors,
                    modifier = Modifier.weight(0.9f)
                )

                CustomOutlinedTextField(
                    value = sem.credits,
                    onValueChange = { newValue ->
                        semesters = semesters.map { if (it.id == sem.id) it.copy(credits = newValue) else it }
                    },
                    label = "মোট ক্রেডিট",
                    themeColors = themeColors,
                    modifier = Modifier.weight(0.8f)
                )

                IconButton(
                    onClick = {
                        if (semesters.size > 1) {
                            semesters = semesters.filter { it.id != sem.id }
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Semester",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Add Semester Button
        Button(
            onClick = {
                val nextId = (semesters.maxOfOrNull { it.id } ?: 0) + 1
                semesters = semesters + CgpaSemester(nextId, "Semester $nextId", "3.75", "15.0")
            },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.background),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Semester",
                tint = themeColors.displayText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "নতুন সেমিস্টার যোগ করুন",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = themeColors.displayText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CGPA Results Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "মোট সিজিপিএ (Cumulative CGPA):",
                            fontSize = 12.sp,
                            color = themeColors.displayText
                        )
                        Text(
                            text = "${df.format(cgpa)} / 4.00",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when {
                                cgpa >= 3.85 -> "চমৎকার (First Class)"
                                cgpa >= 3.5 -> "অত্যন্ত ভালো"
                                cgpa >= 3.0 -> "খুব ভালো"
                                cgpa >= 2.2 -> "উত্তীর্ণ"
                                else -> "সাধারণ"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "মোট সেমিস্টার: ${semesters.size} টি",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "মোট ক্রেডিট আওয়ার: $totalCredits",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// --- Tuition Fees Calculator (টিউশন ফিস ক্যালকুলেটর) ---
@Composable
fun TuitionFeesCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var costPerCredit by remember { mutableStateOf("4500") }
    var totalCredits by remember { mutableStateOf("12") }
    var waiverPercent by remember { mutableStateOf("20") }
    var otherFees by remember { mutableStateOf("5000") }
    var installments by remember { mutableStateOf("3") }

    val cpc = costPerCredit.toDoubleOrNull() ?: 0.0
    val tc = totalCredits.toDoubleOrNull() ?: 0.0
    val wp = waiverPercent.toDoubleOrNull() ?: 0.0
    val of = otherFees.toDoubleOrNull() ?: 0.0
    val inst = installments.toIntOrNull() ?: 1

    val grossTuition = cpc * tc
    val waiverAmount = grossTuition * (wp / 100.0)
    val netTuition = grossTuition - waiverAmount
    val totalFees = netTuition + of
    val perInstallment = if (inst > 0) totalFees / inst else totalFees

    val df = DecimalFormat("#,##0")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "টিউশন ফিস ক্যালকুলেটর (Tuition Fees)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = costPerCredit,
                onValueChange = { costPerCredit = it },
                label = "প্রতি ক্রেডিট ফি (৳)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = totalCredits,
                onValueChange = { totalCredits = it },
                label = "মোট ক্রেডিট",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = waiverPercent,
                onValueChange = { waiverPercent = it },
                label = "ওয়েভার / স্কলারশিপ (%)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = otherFees,
                onValueChange = { otherFees = it },
                label = "অন্যান্য সেমিস্টার ফি (৳)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CustomOutlinedTextField(
            value = installments,
            onValueChange = { installments = it },
            label = "কিস্তির সংখ্যা (Installments)",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Calculations Results Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("মোট টিউশন ফি (পূর্বের):", fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(grossTuition)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ওয়েভারের পরিমাণ ($wp%):", fontSize = 12.sp, color = Color(0xFF10B981))
                    Text("- ৳ ${df.format(waiverAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("অন্যান্য আনুষঙ্গিক ফি:", fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(of)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = themeColors.displayText.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("মোট প্রদেয় ফি (সবসহ):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    Text("৳ ${df.format(totalFees)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                }

                if (inst > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("প্রতি কিস্তিতে প্রদেয় ($inst টি কিস্তি):", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = themeColors.displayText.copy(alpha = 0.8f))
                        Text("৳ ${df.format(perInstallment)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                    }
                }
            }
        }
    }
}

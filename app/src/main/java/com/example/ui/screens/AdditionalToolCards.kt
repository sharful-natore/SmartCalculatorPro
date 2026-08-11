package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.components.CustomOutlinedTextField
import java.text.DecimalFormat

// --- 1. Calorie / BMR Calculator ---
@Composable
fun BMRCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("25") }
    
    var heightUnit by remember { mutableStateOf("cm") } // "cm" or "ft/in"
    var weightUnit by remember { mutableStateOf("kg") } // "kg" or "lbs"
    var heightCm by remember { mutableStateOf("170") }
    var heightFt by remember { mutableStateOf("5") }
    var heightIn by remember { mutableStateOf("7") }
    var weightInput by remember { mutableStateOf("70") }
    
    var activityMultiplier by remember { mutableStateOf(1.375) } // Lightly active default

    val ageVal = age.toDoubleOrNull() ?: 0.0
    
    val weightValKg = if (weightUnit == "kg") {
        weightInput.toDoubleOrNull() ?: 0.0
    } else {
        (weightInput.toDoubleOrNull() ?: 0.0) * 0.453592
    }

    val heightValCm = if (heightUnit == "cm") {
        heightCm.toDoubleOrNull() ?: 0.0
    } else {
        val ft = heightFt.toDoubleOrNull() ?: 0.0
        val inch = heightIn.toDoubleOrNull() ?: 0.0
        ((ft * 12) + inch) * 2.54
    }

    val bmr = if (gender == "Male") {
        (10 * weightValKg) + (6.25 * heightValCm) - (5 * ageVal) + 5
    } else {
        (10 * weightValKg) + (6.25 * heightValCm) - (5 * ageVal) - 161
    }

    val tdee = bmr * activityMultiplier
    val df = DecimalFormat("#,##0")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("bmr_calc_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        // Gender row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = gender == "Male",
                onClick = { gender = "Male" },
                label = { Text(LanguageManager.getString("male", lang), color = if (gender == "Male") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = gender == "Female",
                onClick = { gender = "Female" },
                label = { Text(LanguageManager.getString("female", lang), color = if (gender == "Female") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Unit Toggles row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Height unit toggle
            Row(
                modifier = Modifier.weight(1f).background(themeColors.background, RoundedCornerShape(8.dp)).padding(2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("cm", "ft/in").forEach { unit ->
                    val isSelected = heightUnit == unit
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) themeColors.buttonEqualBg else Color.Transparent)
                            .clickable { heightUnit = unit }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unit == "cm") "সেমি" else "ফুট/ইঞ্চি",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Weight unit toggle
            Row(
                modifier = Modifier.weight(1f).background(themeColors.background, RoundedCornerShape(8.dp)).padding(2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("kg", "lbs").forEach { unit ->
                    val isSelected = weightUnit == unit
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) themeColors.buttonEqualBg else Color.Transparent)
                            .clickable { weightUnit = unit }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unit == "kg") "কেজি" else "পাউন্ড",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Fields Row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CustomOutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = LanguageManager.getString("age_years", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            
            if (heightUnit == "cm") {
                CustomOutlinedTextField(
                    value = heightCm,
                    onValueChange = { heightCm = it },
                    label = "উচ্চতা (সেমি)",
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Row(modifier = Modifier.weight(1.2f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CustomOutlinedTextField(
                        value = heightFt,
                        onValueChange = { heightFt = it },
                        label = "ফুট",
                        themeColors = themeColors,
                        modifier = Modifier.weight(1f)
                    )
                    CustomOutlinedTextField(
                        value = heightIn,
                        onValueChange = { heightIn = it },
                        label = "ইঞ্চি",
                        themeColors = themeColors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            CustomOutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = if (weightUnit == "kg") "ওজন (কেজি)" else "ওজন (পাউন্ড)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(LanguageManager.getString("activity_level", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(6.dp))

        val activities = listOf(
            LanguageManager.getString("activity_sedentary", lang) to 1.2,
            LanguageManager.getString("activity_light", lang) to 1.375,
            LanguageManager.getString("activity_moderate", lang) to 1.55,
            LanguageManager.getString("activity_heavy", lang) to 1.725
        )

        activities.forEach { (label, value) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activityMultiplier = value }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = activityMultiplier == value,
                    onClick = { activityMultiplier = value },
                    colors = RadioButtonDefaults.colors(selectedColor = themeColors.buttonEqualBg)
                )
                Text(label, fontSize = 12.sp, color = themeColors.displayText)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                    Text(LanguageManager.getString("bmr_result", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("${df.format(bmr.coerceAtLeast(0.0))} kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(LanguageManager.getString("tdee_maintain", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("${df.format(tdee.coerceAtLeast(0.0))} kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(LanguageManager.getString("weight_loss_goal", lang), fontSize = 12.sp, color = Color(0xFF10B981))
                    Text("${df.format((tdee - 500).coerceAtLeast(0.0))} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(LanguageManager.getString("weight_gain_goal", lang), fontSize = 12.sp, color = Color(0xFFF59E0B))
                    Text("${df.format((tdee + 500).coerceAtLeast(0.0))} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
            }
        }
    }
}

// --- 2. Ideal Weight Calculator ---
@Composable
fun IdealWeightCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var gender by remember { mutableStateOf("Male") }
    var heightUnit by remember { mutableStateOf("ft/in") } // "ft/in" or "cm"
    var heightCm by remember { mutableStateOf("170") }
    var heightFt by remember { mutableStateOf("5") }
    var heightIn by remember { mutableStateOf("7") }

    val heightValCm = if (heightUnit == "cm") {
        heightCm.toDoubleOrNull() ?: 170.0
    } else {
        val ft = heightFt.toDoubleOrNull() ?: 0.0
        val inch = heightIn.toDoubleOrNull() ?: 0.0
        ((ft * 12) + inch) * 2.54
    }

    val totalInches = heightValCm / 2.54
    val totalMeters = heightValCm / 100.0

    // Devine formula
    val devineKg = if (totalInches >= 60) {
        if (gender == "Male") 50.0 + 2.3 * (totalInches - 60)
        else 45.5 + 2.3 * (totalInches - 60)
    } else 0.0

    // Healthy BMI 18.5 - 24.9 range
    val minBmiKg = 18.5 * totalMeters * totalMeters
    val maxBmiKg = 24.9 * totalMeters * totalMeters

    val df = DecimalFormat("#.#")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("ideal_weight_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = gender == "Male",
                onClick = { gender = "Male" },
                label = { Text(LanguageManager.getString("male", lang), color = if (gender == "Male") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = gender == "Female",
                onClick = { gender = "Female" },
                label = { Text(LanguageManager.getString("female", lang), color = if (gender == "Female") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Height unit toggle
        Row(
            modifier = Modifier.fillMaxWidth().background(themeColors.background, RoundedCornerShape(8.dp)).padding(2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("cm", "ft/in").forEach { unit ->
                val isSelected = heightUnit == unit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) themeColors.buttonEqualBg else Color.Transparent)
                        .clickable { heightUnit = unit }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unit == "cm") "সেমি" else "ফুট/ইঞ্চি",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (heightUnit == "cm") {
            CustomOutlinedTextField(
                value = heightCm,
                onValueChange = { heightCm = it },
                label = "উচ্চতা (সেমি)",
                themeColors = themeColors,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CustomOutlinedTextField(
                    value = heightFt,
                    onValueChange = { heightFt = it },
                    label = LanguageManager.getString("height_ft", lang),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
                CustomOutlinedTextField(
                    value = heightIn,
                    onValueChange = { heightIn = it },
                    label = LanguageManager.getString("height_in", lang),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("কাঙ্ক্ষিত আদর্শ ওজন (Devine Formula):", fontSize = 13.sp, color = themeColors.displayText)
                Text("${df.format(devineKg.coerceAtLeast(0.0))} কেজি (${df.format(devineKg * 2.20462)} পাউন্ড)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))

                Spacer(modifier = Modifier.height(8.dp))

                Text("স্বাস্থ্যকর ওজনের সীমা (BMI 18.5 - 24.9):", fontSize = 12.sp, color = themeColors.displayText)
                Text("${df.format(minBmiKg.coerceAtLeast(0.0))} - ${df.format(maxBmiKg.coerceAtLeast(0.0))} কেজি", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
            }
        }
    }
}

// --- 3. Water Intake Tracker ---
@Composable
fun WaterIntakeTrackerCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var weightUnit by remember { mutableStateOf("kg") } // "kg" or "lbs"
    var weightInput by remember { mutableStateOf("65") }
    var exerciseMins by remember { mutableStateOf("30") }
    var glassesDrunk by remember { mutableStateOf(3) }

    val weightVal = weightInput.toDoubleOrNull() ?: 0.0
    val weightKgVal = if (weightUnit == "kg") weightVal else weightVal * 0.453592
    val exVal = exerciseMins.toDoubleOrNull() ?: 0.0

    val baseLiters = weightKgVal * 0.033
    val exLiters = (exVal / 30.0) * 0.35
    val totalLiters = baseLiters + exLiters
    val totalGlasses = (totalLiters * 1000 / 250).toInt().coerceAtLeast(1)

    val df = DecimalFormat("#.#")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("water_intake_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        // Weight unit toggle
        Row(
            modifier = Modifier.fillMaxWidth().background(themeColors.background, RoundedCornerShape(8.dp)).padding(2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("kg", "lbs").forEach { unit ->
                val isSelected = weightUnit == unit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) themeColors.buttonEqualBg else Color.Transparent)
                        .clickable { weightUnit = unit }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unit == "kg") "কেজি" else "পাউন্ড",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else themeColors.displayText.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = if (weightUnit == "kg") "ওজন (কেজি)" else "ওজন (পাউন্ড)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = exerciseMins,
                onValueChange = { exerciseMins = it },
                label = LanguageManager.getString("daily_exercise_mins", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                        Text(LanguageManager.getString("daily_water_req", lang), fontSize = 12.sp, color = themeColors.displayText)
                        Text("${df.format(totalLiters.coerceAtLeast(0.0))} L ($totalGlasses ${LanguageManager.getString("glasses", lang)})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                    }

                    // Interactive glass counter
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (glassesDrunk > 0) glassesDrunk-- },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(themeColors.background)
                        ) {
                            Text("-", fontWeight = FontWeight.Bold, color = themeColors.displayText)
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$glassesDrunk / $totalGlasses", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = themeColors.displayText)
                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { glassesDrunk++ },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(themeColors.buttonEqualBg)
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (glassesDrunk.toFloat() / totalGlasses.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = themeColors.buttonEqualBg,
                    trackColor = themeColors.background
                )
            }
        }
    }
}

// --- 4. EMI & Loan Calculator ---
@Composable
fun EmiLoanCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var loanAmount by remember { mutableStateOf("500000") }
    var interestRate by remember { mutableStateOf("9.5") }
    var tenureYears by remember { mutableStateOf("5") }

    val p = loanAmount.toDoubleOrNull() ?: 0.0
    val annualRate = interestRate.toDoubleOrNull() ?: 0.0
    val years = tenureYears.toDoubleOrNull() ?: 0.0

    val r = annualRate / (12 * 100)
    val n = years * 12

    val emi = if (p > 0 && r > 0 && n > 0) {
        (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1)
    } else 0.0

    val totalPayment = emi * n
    val totalInterest = totalPayment - p

    val df = DecimalFormat("#,##0")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("emi_loan_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        CustomOutlinedTextField(
            value = loanAmount,
            onValueChange = { loanAmount = it },
            label = LanguageManager.getString("loan_amount", lang),
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = interestRate,
                onValueChange = { interestRate = it },
                label = LanguageManager.getString("annual_interest_rate", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = tenureYears,
                onValueChange = { tenureYears = it },
                label = LanguageManager.getString("tenure_years", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("monthly_emi", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(emi.coerceAtLeast(0.0))}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("total_interest_payable", lang), fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalInterest.coerceAtLeast(0.0))}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("total_payment", lang), fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalPayment.coerceAtLeast(0.0))}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
            }
        }
    }
}

// --- 5. Profit & Loss Margin ---
@Composable
fun ProfitLossMarginCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var costPrice by remember { mutableStateOf("1000") }
    var sellingPrice by remember { mutableStateOf("1250") }
    var quantity by remember { mutableStateOf("1") }

    val cp = costPrice.toDoubleOrNull() ?: 0.0
    val sp = sellingPrice.toDoubleOrNull() ?: 0.0
    val qty = quantity.toDoubleOrNull() ?: 1.0

    val totalCost = cp * qty
    val totalRevenue = sp * qty
    val profitLoss = totalRevenue - totalCost
    val profitLossPercent = if (totalCost > 0) (profitLoss / totalCost) * 100 else 0.0
    val marginPercent = if (totalRevenue > 0) (profitLoss / totalRevenue) * 100 else 0.0

    val df = DecimalFormat("#,##0.00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("profit_loss_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = costPrice,
                onValueChange = { costPrice = it },
                label = LanguageManager.getString("cost_price", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = sellingPrice,
                onValueChange = { sellingPrice = it },
                label = LanguageManager.getString("selling_price", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CustomOutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = LanguageManager.getString("quantity", lang),
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        val isProfit = profitLoss >= 0
        val resultColor = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)

        Card(
            colors = CardDefaults.cardColors(containerColor = resultColor.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isProfit) LanguageManager.getString("total_profit", lang) else LanguageManager.getString("total_loss", lang),
                    fontSize = 13.sp,
                    color = themeColors.displayText
                )
                Text(
                    text = "৳ ${df.format(Math.abs(profitLoss))}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = resultColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("profit_loss_percent", lang), fontSize = 12.sp, color = themeColors.displayText)
                    Text("${df.format(profitLossPercent)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = resultColor)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("profit_margin_percent", lang), fontSize = 12.sp, color = themeColors.displayText)
                    Text("${df.format(marginPercent)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
            }
        }
    }
}

// --- 6. VAT & Tax Calculator ---
@Composable
fun VatTaxCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var amount by remember { mutableStateOf("1000") }
    var vatRate by remember { mutableStateOf("15") }
    var mode by remember { mutableStateOf("ADD") } // "ADD" or "REMOVE"

    val amt = amount.toDoubleOrNull() ?: 0.0
    val rate = vatRate.toDoubleOrNull() ?: 0.0

    val (baseAmount, vatAmount, totalAmount) = if (mode == "ADD") {
        val v = amt * (rate / 100.0)
        Triple(amt, v, amt + v)
    } else {
        val b = amt / (1 + (rate / 100.0))
        val v = amt - b
        Triple(b, v, amt)
    }

    val df = DecimalFormat("#,##0.00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("vat_tax_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = mode == "ADD",
                onClick = { mode = "ADD" },
                label = { Text(LanguageManager.getString("add_vat", lang), color = if (mode == "ADD") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = mode == "REMOVE",
                onClick = { mode = "REMOVE" },
                label = { Text(LanguageManager.getString("remove_vat", lang), color = if (mode == "REMOVE") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = LanguageManager.getString("amount_tk", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(2f)
            )
            CustomOutlinedTextField(
                value = vatRate,
                onValueChange = { vatRate = it },
                label = LanguageManager.getString("vat_rate_pct", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("net_amount", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(baseAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("vat_amount", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(vatAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("gross_total", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}

// --- 7. Simple & Compound Interest ---
@Composable
fun InterestCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var principal by remember { mutableStateOf("100000") }
    var rate by remember { mutableStateOf("8.5") }
    var timeYears by remember { mutableStateOf("3") }
    var isCompound by remember { mutableStateOf(true) }

    val p = principal.toDoubleOrNull() ?: 0.0
    val r = rate.toDoubleOrNull() ?: 0.0
    val t = timeYears.toDoubleOrNull() ?: 0.0

    val (totalInterest, finalAmount) = if (!isCompound) {
        val interest = (p * r * t) / 100.0
        Pair(interest, p + interest)
    } else {
        val amount = p * Math.pow(1 + (r / 100.0), t)
        Pair(amount - p, amount)
    }

    val df = DecimalFormat("#,##0.00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("interest_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = isCompound,
                onClick = { isCompound = true },
                label = { Text(LanguageManager.getString("interest_compound", lang), color = if (isCompound) Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !isCompound,
                onClick = { isCompound = false },
                label = { Text(LanguageManager.getString("interest_simple", lang), color = if (!isCompound) Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        CustomOutlinedTextField(
            value = principal,
            onValueChange = { principal = it },
            label = LanguageManager.getString("principal_amount", lang),
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = LanguageManager.getString("interest_rate_pct", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = timeYears,
                onValueChange = { timeYears = it },
                label = LanguageManager.getString("time_years", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("total_interest_earned", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalInterest.coerceAtLeast(0.0))}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("maturity_amount", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(finalAmount.coerceAtLeast(0.0))}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}

// --- 8. Date Difference ---
@Composable
fun DateDifferenceCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var startDay by remember { mutableStateOf("1") }
    var startMonth by remember { mutableStateOf("1") }
    var startYear by remember { mutableStateOf("2025") }

    var endDay by remember { mutableStateOf("9") }
    var endMonth by remember { mutableStateOf("8") }
    var endYear by remember { mutableStateOf("2026") }

    val sd = startDay.toIntOrNull() ?: 1
    val sm = startMonth.toIntOrNull() ?: 1
    val sy = startYear.toIntOrNull() ?: 2025

    val ed = endDay.toIntOrNull() ?: 9
    val em = endMonth.toIntOrNull() ?: 8
    val ey = endYear.toIntOrNull() ?: 2026

    var diffYears = ey - sy
    var diffMonths = em - sm
    var diffDays = ed - sd

    if (diffDays < 0) {
        diffMonths -= 1
        diffDays += 30
    }
    if (diffMonths < 0) {
        diffYears -= 1
        diffMonths += 12
    }

    val totalEstDays = (diffYears * 365) + (diffMonths * 30) + diffDays

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("date_diff_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Text(LanguageManager.getString("start_date", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(value = startDay, onValueChange = { startDay = it }, label = LanguageManager.getString("day", lang), themeColors = themeColors, modifier = Modifier.weight(1f))
            CustomOutlinedTextField(value = startMonth, onValueChange = { startMonth = it }, label = LanguageManager.getString("month", lang), themeColors = themeColors, modifier = Modifier.weight(1f))
            CustomOutlinedTextField(value = startYear, onValueChange = { startYear = it }, label = LanguageManager.getString("year", lang), themeColors = themeColors, modifier = Modifier.weight(1.2f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(LanguageManager.getString("end_date", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(value = endDay, onValueChange = { endDay = it }, label = LanguageManager.getString("day", lang), themeColors = themeColors, modifier = Modifier.weight(1f))
            CustomOutlinedTextField(value = endMonth, onValueChange = { endMonth = it }, label = LanguageManager.getString("month", lang), themeColors = themeColors, modifier = Modifier.weight(1f))
            CustomOutlinedTextField(value = endYear, onValueChange = { endYear = it }, label = LanguageManager.getString("year", lang), themeColors = themeColors, modifier = Modifier.weight(1.2f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(LanguageManager.getString("date_diff_result", lang), fontSize = 13.sp, color = themeColors.displayText)
                Text("${diffYears.coerceAtLeast(0)} ${LanguageManager.getString("year", lang)}, ${diffMonths.coerceAtLeast(0)} ${LanguageManager.getString("month", lang)}, ${diffDays.coerceAtLeast(0)} ${LanguageManager.getString("day", lang)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)

                Spacer(modifier = Modifier.height(6.dp))
                Text("${LanguageManager.getString("total_est_days", lang)}: ${totalEstDays.coerceAtLeast(0)}", fontSize = 13.sp, color = themeColors.displayText)
            }
        }
    }
}

// --- 9. Tip Calculator ---
@Composable
fun TipCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var billAmount by remember { mutableStateOf("1500") }
    var tipPercent by remember { mutableStateOf("10") }
    var peopleCount by remember { mutableStateOf("3") }

    val bill = billAmount.toDoubleOrNull() ?: 0.0
    val tipPct = tipPercent.toDoubleOrNull() ?: 0.0
    val people = (peopleCount.toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0)

    val tipAmount = bill * (tipPct / 100.0)
    val totalBill = bill + tipAmount
    val perPersonTotal = totalBill / people
    val perPersonTip = tipAmount / people

    val df = DecimalFormat("#,##0.00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("tip_split_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        CustomOutlinedTextField(
            value = billAmount,
            onValueChange = { billAmount = it },
            label = LanguageManager.getString("bill_amount", lang),
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = tipPercent,
                onValueChange = { tipPercent = it },
                label = LanguageManager.getString("tip_percent", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = peopleCount,
                onValueChange = { peopleCount = it },
                label = LanguageManager.getString("split_people", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("bill_per_person", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(perPersonTotal)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("total_tip_amount", lang), fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(tipAmount)} (${df.format(perPersonTip)}/p)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("grand_total_bill", lang), fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalBill)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
            }
        }
    }
}

// --- 10. Electricity Bill Calculator ---
@Composable
fun ElectricityBillCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var units by remember { mutableStateOf("250") }
    var connectionType by remember { mutableStateOf("Residential") } // Residential / Commercial
    var isCustomizing by remember { mutableStateOf(false) }

    var customRate by remember { mutableStateOf(viewModel.elecUnitRate) }
    var customDemand by remember { mutableStateOf(viewModel.elecDemandCharge) }
    var customMeter by remember { mutableStateOf(viewModel.elecMeterRent) }
    var customVat by remember { mutableStateOf(viewModel.elecVatPercent) }
    var useFlatRate by remember { mutableStateOf(false) }

    val u = units.toDoubleOrNull() ?: 0.0
    val cRate = customRate.toDoubleOrNull() ?: 6.50
    val cDemand = customDemand.toDoubleOrNull() ?: 40.0
    val cMeter = customMeter.toDoubleOrNull() ?: 40.0
    val cVatPct = customVat.toDoubleOrNull() ?: 5.0

    val energyCost = if (useFlatRate) {
        u * cRate
    } else {
        when {
            u <= 50 -> u * 4.63
            u <= 75 -> (50 * 4.63) + ((u - 50) * 5.26)
            u <= 200 -> (50 * 4.63) + (25 * 5.26) + ((u - 75) * 7.20)
            u <= 300 -> (50 * 4.63) + (25 * 5.26) + (125 * 7.20) + ((u - 200) * 7.59)
            u <= 400 -> (50 * 4.63) + (25 * 5.26) + (125 * 7.20) + (100 * 7.59) + ((u - 300) * 8.02)
            u <= 600 -> (50 * 4.63) + (25 * 5.26) + (125 * 7.20) + (100 * 7.59) + (100 * 8.02) + ((u - 400) * 12.67)
            else -> (50 * 4.63) + (25 * 5.26) + (125 * 7.20) + (100 * 7.59) + (100 * 8.02) + (200 * 12.67) + ((u - 600) * 14.61)
        }
    }

    val multiplier = if (connectionType == "Commercial") 1.35 else 1.0
    val baseEnergy = energyCost * multiplier
    val demandAndMeterFee = (cDemand + cMeter) * (if (connectionType == "Commercial") 1.5 else 1.0)
    val vat = (baseEnergy + demandAndMeterFee) * (cVatPct / 100.0)
    val totalBill = baseEnergy + demandAndMeterFee + vat

    val df = DecimalFormat("#,##0.00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LanguageManager.getString("electricity_title", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            TextButton(
                onClick = { isCustomizing = !isCustomizing },
                colors = ButtonDefaults.textButtonColors(contentColor = themeColors.buttonEqualBg)
            ) {
                Icon(
                    imageVector = if (isCustomizing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = "Edit Rates",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCustomizing) LanguageManager.getString("close", lang) else LanguageManager.getString("edit_rates", lang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isCustomizing) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = themeColors.background)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = LanguageManager.getString("custom_tariff_settings", lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = useFlatRate,
                            onCheckedChange = { useFlatRate = it },
                            colors = CheckboxDefaults.colors(checkedColor = themeColors.buttonEqualBg)
                        )
                        Text(
                            text = LanguageManager.getString("use_flat_rate", lang),
                            fontSize = 12.sp,
                            color = themeColors.displayText
                        )
                    }

                    if (useFlatRate) {
                        CustomOutlinedTextField(
                            value = customRate,
                            onValueChange = { customRate = it },
                            label = LanguageManager.getString("unit_flat_charge", lang),
                            themeColors = themeColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CustomOutlinedTextField(
                            value = customDemand,
                            onValueChange = { customDemand = it },
                            label = LanguageManager.getString("demand_charge", lang),
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                        CustomOutlinedTextField(
                            value = customMeter,
                            onValueChange = { customMeter = it },
                            label = LanguageManager.getString("meter_rent", lang),
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    CustomOutlinedTextField(
                        value = customVat,
                        onValueChange = { customVat = it },
                        label = LanguageManager.getString("vat_rate_pct", lang),
                        themeColors = themeColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.updateElectricityRates(customRate, customDemand, customMeter, customVat)
                            isCustomizing = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LanguageManager.getString("save_settings", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = connectionType == "Residential",
                onClick = { connectionType = "Residential" },
                label = { Text(LanguageManager.getString("residential", lang), color = if (connectionType == "Residential") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = connectionType == "Commercial",
                onClick = { connectionType = "Commercial" },
                label = { Text(LanguageManager.getString("commercial", lang), color = if (connectionType == "Commercial") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        CustomOutlinedTextField(
            value = units,
            onValueChange = { units = it },
            label = LanguageManager.getString("monthly_units_kwh", lang),
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("energy_charge", lang), fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(baseEnergy)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("demand_meter_charge", lang), fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(demandAndMeterFee)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("VAT (${customVat}%):", fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(vat)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("estimated_total_bill", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalBill)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                }
            }
        }
    }
}

// --- 11. Appliance Energy Cost ---
@Composable
fun ApplianceEnergyCostCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var wattage by remember { mutableStateOf("75") }
    var hoursPerDay by remember { mutableStateOf("8") }
    var unitRate by remember { mutableStateOf(7.50) }

    val watts = wattage.toDoubleOrNull() ?: 0.0
    val hours = hoursPerDay.toDoubleOrNull() ?: 0.0

    val dailyKwh = (watts * hours) / 1000.0
    val monthlyKwh = dailyKwh * 30.0
    val dailyCost = dailyKwh * unitRate
    val monthlyCost = monthlyKwh * unitRate

    val df = DecimalFormat("#,##0.00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("appliance_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(10.dp))

        // Presets row
        Text(LanguageManager.getString("quick_select", lang), fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            AssistChip(onClick = { wattage = "75" }, label = { Text("Fan (75W)") })
            AssistChip(onClick = { wattage = "1500" }, label = { Text("AC (1500W)") })
            AssistChip(onClick = { wattage = "200" }, label = { Text("Fridge (200W)") })
            AssistChip(onClick = { wattage = "100" }, label = { Text("TV (100W)") })
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = wattage,
                onValueChange = { wattage = it },
                label = LanguageManager.getString("wattage_watt", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = hoursPerDay,
                onValueChange = { hoursPerDay = it },
                label = LanguageManager.getString("daily_hours_used", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("daily_energy_cost", lang), fontSize = 12.sp, color = themeColors.displayText)
                    Text("${df.format(dailyKwh)} kWh (৳ ${df.format(dailyCost)})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("monthly_energy_cost", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("${df.format(monthlyKwh)} kWh", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("monthly_bill_amount", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(monthlyCost)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}

// --- 12. Battery / Power Bank Backup ---
@Composable
fun BatteryBackupCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var loadWatts by remember { mutableStateOf("150") }
    var batteryAh by remember { mutableStateOf("150") }
    var batteryVolts by remember { mutableStateOf("12") }
    var efficiencyPct by remember { mutableStateOf("85") }

    val watts = loadWatts.toDoubleOrNull() ?: 0.0
    val ah = batteryAh.toDoubleOrNull() ?: 0.0
    val volts = batteryVolts.toDoubleOrNull() ?: 12.0
    val eff = (efficiencyPct.toDoubleOrNull() ?: 85.0) / 100.0

    val storedWh = volts * ah * eff
    val backupHoursTotal = if (watts > 0) storedWh / watts else 0.0

    val hours = backupHoursTotal.toInt()
    val minutes = ((backupHoursTotal - hours) * 60).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("battery_backup_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = loadWatts,
                onValueChange = { loadWatts = it },
                label = LanguageManager.getString("load_watt", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = batteryAh,
                onValueChange = { batteryAh = it },
                label = LanguageManager.getString("battery_ah", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = batteryVolts,
                onValueChange = { batteryVolts = it },
                label = LanguageManager.getString("battery_volts", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = efficiencyPct,
                onValueChange = { efficiencyPct = it },
                label = LanguageManager.getString("inverter_efficiency", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(LanguageManager.getString("est_backup_time", lang), fontSize = 13.sp, color = themeColors.displayText)
                Text("${hours.coerceAtLeast(0)} ${LanguageManager.getString("hours", lang)} ${minutes.coerceAtLeast(0)} ${LanguageManager.getString("mins", lang)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)

                Spacer(modifier = Modifier.height(6.dp))
                Text("${LanguageManager.getString("total_usable_wh", lang)}: ${storedWh.toInt()} Wh", fontSize = 12.sp, color = themeColors.displayText)
            }
        }
    }
}

// --- 13. Fuel Cost Calculator ---
@Composable
fun FuelCostCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var distance by remember { mutableStateOf("120") }
    var mileage by remember { mutableStateOf("15") } // Km per Liter
    var fuelPrice by remember { mutableStateOf("130") } // Tk per Liter

    val d = distance.toDoubleOrNull() ?: 0.0
    val m = mileage.toDoubleOrNull() ?: 1.0
    val price = fuelPrice.toDoubleOrNull() ?: 0.0

    val fuelNeeded = if (m > 0) d / m else 0.0
    val totalCost = fuelNeeded * price
    val costPerKm = if (d > 0) totalCost / d else 0.0

    val df = DecimalFormat("#,##0.00")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("fuel_cost_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        CustomOutlinedTextField(
            value = distance,
            onValueChange = { distance = it },
            label = LanguageManager.getString("total_distance_km", lang),
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = mileage,
                onValueChange = { mileage = it },
                label = LanguageManager.getString("mileage_km_l", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = fuelPrice,
                onValueChange = { fuelPrice = it },
                label = LanguageManager.getString("fuel_price_tk_l", lang),
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("fuel_needed", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("${df.format(fuelNeeded)} L", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("total_fuel_cost", lang), fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalCost)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageManager.getString("cost_per_km", lang), fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(costPerKm)} / km", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
            }
        }
    }
}

// --- 14. Speed, Distance & Time ---
@Composable
fun SpeedDistanceTimeCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var calcTarget by remember { mutableStateOf("Speed") } // Speed, Distance, Time
    var val1 by remember { mutableStateOf("60") } // Distance or Speed
    var val2 by remember { mutableStateOf("2") }  // Time or Distance

    val v1 = val1.toDoubleOrNull() ?: 0.0
    val v2 = val2.toDoubleOrNull() ?: 0.0

    val (resultText, resultValueStr) = when (calcTarget) {
        "Speed" -> { // Speed = Distance / Time
            val speed = if (v2 > 0) v1 / v2 else 0.0
            val ms = speed / 3.6
            val mph = speed * 0.621371
            Pair(LanguageManager.getString("speed_calc_res", lang), "${DecimalFormat("#,##0.00").format(speed)} km/h (${DecimalFormat("#,##0.00").format(ms)} m/s, ${DecimalFormat("#,##0.00").format(mph)} mph)")
        }
        "Distance" -> { // Distance = Speed * Time
            val distance = v1 * v2
            val miles = distance * 0.621371
            Pair(LanguageManager.getString("distance_calc_res", lang), "${DecimalFormat("#,##0.00").format(distance)} km (${DecimalFormat("#,##0.00").format(miles)} miles)")
        }
        else -> { // Time = Distance / Speed
            val hoursVal = if (v2 > 0) v1 / v2 else 0.0
            val h = hoursVal.toInt()
            val m = ((hoursVal - h) * 60).toInt()
            Pair(LanguageManager.getString("time_calc_res", lang), "$h ${LanguageManager.getString("hours", lang)} $m ${LanguageManager.getString("mins", lang)} (${DecimalFormat("#,##0.00").format(hoursVal)} hrs)")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(LanguageManager.getString("speed_dist_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = calcTarget == "Speed",
                onClick = { calcTarget = "Speed"; val1 = "120"; val2 = "2" },
                label = { Text(LanguageManager.getString("calc_speed", lang), color = if (calcTarget == "Speed") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = calcTarget == "Distance",
                onClick = { calcTarget = "Distance"; val1 = "60"; val2 = "2" },
                label = { Text(LanguageManager.getString("calc_distance", lang), color = if (calcTarget == "Distance") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = calcTarget == "Time",
                onClick = { calcTarget = "Time"; val1 = "120"; val2 = "60" },
                label = { Text(LanguageManager.getString("calc_time", lang), color = if (calcTarget == "Time") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themeColors.buttonEqualBg,
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (calcTarget) {
            "Speed" -> {
                CustomOutlinedTextField(value = val1, onValueChange = { val1 = it }, label = LanguageManager.getString("distance_km", lang), themeColors = themeColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                CustomOutlinedTextField(value = val2, onValueChange = { val2 = it }, label = LanguageManager.getString("time_hours", lang), themeColors = themeColors, modifier = Modifier.fillMaxWidth())
            }
            "Distance" -> {
                CustomOutlinedTextField(value = val1, onValueChange = { val1 = it }, label = LanguageManager.getString("speed_kmh", lang), themeColors = themeColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                CustomOutlinedTextField(value = val2, onValueChange = { val2 = it }, label = LanguageManager.getString("time_hours", lang), themeColors = themeColors, modifier = Modifier.fillMaxWidth())
            }
            "Time" -> {
                CustomOutlinedTextField(value = val1, onValueChange = { val1 = it }, label = LanguageManager.getString("distance_km", lang), themeColors = themeColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                CustomOutlinedTextField(value = val2, onValueChange = { val2 = it }, label = LanguageManager.getString("speed_kmh", lang), themeColors = themeColors, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(resultText, fontSize = 13.sp, color = themeColors.displayText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(resultValueStr, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
            }
        }
    }
}

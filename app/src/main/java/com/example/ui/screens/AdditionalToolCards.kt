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
import java.text.DecimalFormat

// Custom OutlinedTextField with explicit high contrast theme colors for pristine text visibility
@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = themeColors.displayText.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = themeColors.cardBg,
            unfocusedContainerColor = themeColors.cardBg,
            focusedTextColor = themeColors.displayText,
            unfocusedTextColor = themeColors.displayText,
            focusedBorderColor = Color(0xFF6366F1),
            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.35f),
            focusedLabelColor = Color(0xFF6366F1),
            unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.8f),
            cursorColor = Color(0xFF6366F1)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        singleLine = singleLine,
        trailingIcon = trailingIcon
    )
}

// --- 1. Calorie / BMR Calculator ---
@Composable
fun BMRCalculatorCard(
    themeColors: CalculatorThemeColors
) {
    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("25") }
    var height by remember { mutableStateOf("170") } // cm
    var weight by remember { mutableStateOf("70") } // kg
    var activityMultiplier by remember { mutableStateOf(1.375) } // Lightly active default

    val ageVal = age.toDoubleOrNull() ?: 0.0
    val heightVal = height.toDoubleOrNull() ?: 0.0
    val weightVal = weight.toDoubleOrNull() ?: 0.0

    val bmr = if (gender == "Male") {
        (10 * weightVal) + (6.25 * heightVal) - (5 * ageVal) + 5
    } else {
        (10 * weightVal) + (6.25 * heightVal) - (5 * ageVal) - 161
    }

    val tdee = bmr * activityMultiplier
    val df = DecimalFormat("#,##0")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("ক্যালোরি ও BMR হিসাব", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        // Gender row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = gender == "Male",
                onClick = { gender = "Male" },
                label = { Text("পুরুষ (Male)", color = if (gender == "Male") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF6366F1),
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = gender == "Female",
                onClick = { gender = "Female" },
                label = { Text("নারী (Female)", color = if (gender == "Female") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF6366F1),
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = "বয়স (বছর)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = "উচ্চতা (সেমি)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = "ওজন (কেজি)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("দৈনিক কায়িক পরিশ্রমের মাত্রা:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(6.dp))

        val activities = listOf(
            "কম / বসে কাজ (1.2)" to 1.2,
            "হালকা ব্যায়াম ১-৩ দিন (1.375)" to 1.375,
            "মাঝারি ব্যায়াম ৩-৫ দিন (1.55)" to 1.55,
            "কঠোর পরিশ্রম / ব্যায়াম (1.725)" to 1.725
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
                    onClick = { activityMultiplier = value }
                )
                Text(label, fontSize = 12.sp, color = themeColors.displayText)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("BMR (বেসাল মেটাবলিক রেট):", fontSize = 13.sp, color = themeColors.displayText)
                    Text("${df.format(bmr.coerceAtLeast(0.0))} kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ওজন বজায় রাখতে প্রয়োজনীয় ক্যালোরি:", fontSize = 13.sp, color = themeColors.displayText)
                    Text("${df.format(tdee.coerceAtLeast(0.0))} kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ওজন কমাতে (-500 kcal/দিন):", fontSize = 12.sp, color = Color(0xFF10B981))
                    Text("${df.format((tdee - 500).coerceAtLeast(0.0))} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ওজন বাড়াতে (+500 kcal/দিন):", fontSize = 12.sp, color = Color(0xFFF59E0B))
                    Text("${df.format((tdee + 500).coerceAtLeast(0.0))} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
            }
        }
    }
}

// --- 2. Ideal Weight Calculator ---
@Composable
fun IdealWeightCalculatorCard(
    themeColors: CalculatorThemeColors
) {
    var gender by remember { mutableStateOf("Male") }
    var heightFt by remember { mutableStateOf("5") }
    var heightIn by remember { mutableStateOf("7") }

    val ft = heightFt.toDoubleOrNull() ?: 0.0
    val inch = heightIn.toDoubleOrNull() ?: 0.0
    val totalInches = (ft * 12) + inch
    val totalMeters = totalInches * 0.0254

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
        Text("আদর্শ ওজন ক্যালকুলেটর", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = gender == "Male",
                onClick = { gender = "Male" },
                label = { Text("পুরুষ (Male)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = gender == "Female",
                onClick = { gender = "Female" },
                label = { Text("নারী (Female)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = heightFt,
                onValueChange = { heightFt = it },
                label = { Text("উচ্চতা (ফুট)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = heightIn,
                onValueChange = { heightIn = it },
                label = { Text("ইঞ্চি") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
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
    themeColors: CalculatorThemeColors
) {
    var weight by remember { mutableStateOf("65") }
    var exerciseMins by remember { mutableStateOf("30") }
    var glassesDrunk by remember { mutableStateOf(3) }

    val weightVal = weight.toDoubleOrNull() ?: 0.0
    val exVal = exerciseMins.toDoubleOrNull() ?: 0.0

    val baseLiters = weightVal * 0.033
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
        Text("পানি পান ট্র্যাকার", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("ওজন (কেজি)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = exerciseMins,
                onValueChange = { exerciseMins = it },
                label = { Text("দৈনিক ব্যায়াম (মিনিট)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0EA5E9).copy(alpha = 0.1f)),
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
                        Text("দৈনিক প্রয়োজনীয় পানি:", fontSize = 12.sp, color = themeColors.displayText)
                        Text("${df.format(totalLiters.coerceAtLeast(0.0))} লিটার ($totalGlasses গ্লাস)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0EA5E9))
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
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF0EA5E9))
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (glassesDrunk.toFloat() / totalGlasses.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF0EA5E9),
                    trackColor = themeColors.background
                )
            }
        }
    }
}

// --- 4. EMI & Loan Calculator ---
@Composable
fun EmiLoanCalculatorCard(
    themeColors: CalculatorThemeColors
) {
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
        Text("ইএমআই ও ঋণ ক্যালকুলেটর", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = loanAmount,
            onValueChange = { loanAmount = it },
            label = { Text("ঋণের পরিমাণ (৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = interestRate,
                onValueChange = { interestRate = it },
                label = { Text("বার্ষিক সুদের হার (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = tenureYears,
                onValueChange = { tenureYears = it },
                label = { Text("মেয়াদ (বছর)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("মাসিক কিস্তি (EMI):", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(emi.coerceAtLeast(0.0))}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("মোট প্রদেয় সুদ:", fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalInterest.coerceAtLeast(0.0))}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("সর্বমোট পরিশোধযোগ্য অর্থ:", fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalPayment.coerceAtLeast(0.0))}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
            }
        }
    }
}

// --- 5. Profit & Loss Margin ---
@Composable
fun ProfitLossMarginCard(
    themeColors: CalculatorThemeColors
) {
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
        Text("লাভ ও ক্ষতি মার্জিন", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = costPrice,
                onValueChange = { costPrice = it },
                label = { Text("ক্রয়মূল্য (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = sellingPrice,
                onValueChange = { sellingPrice = it },
                label = { Text("বিক্রয়মূল্য (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("পরিমাণ (সংখ্যা)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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
                    text = if (isProfit) "মোট লাভ (Profit):" else "মোট ক্ষতি (Loss):",
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
                    Text("লাভ/ক্ষতির শতাংশ:", fontSize = 12.sp, color = themeColors.displayText)
                    Text("${df.format(profitLossPercent)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = resultColor)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("প্রফিট মার্জিন (Margin):", fontSize = 12.sp, color = themeColors.displayText)
                    Text("${df.format(marginPercent)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
            }
        }
    }
}

// --- 6. VAT & Tax Calculator ---
@Composable
fun VatTaxCalculatorCard(
    themeColors: CalculatorThemeColors
) {
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
        Text("ভ্যাট ও ট্যাক্স ক্যালকুলেটর", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = mode == "ADD",
                onClick = { mode = "ADD" },
                label = { Text("ভ্যাট যোগ করুন (+VAT)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = mode == "REMOVE",
                onClick = { mode = "REMOVE" },
                label = { Text("ভ্যাট অন্তর্ভুক্ত (-VAT)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("টাকার পরিমাণ (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(2f),
                singleLine = true
            )
            OutlinedTextField(
                value = vatRate,
                onValueChange = { vatRate = it },
                label = { Text("ভ্যাট হার (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("মূল টাকা (Net Amount):", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(baseAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ভ্যাটের পরিমাণ (VAT Amount):", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(vatAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("সর্বমোট টাকা (Gross Total):", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}

// --- 7. Simple & Compound Interest ---
@Composable
fun InterestCalculatorCard(
    themeColors: CalculatorThemeColors
) {
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
        Text("সুদ হিসাব (সরল ও চক্রবৃদ্ধি)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = isCompound,
                onClick = { isCompound = true },
                label = { Text("চক্রবৃদ্ধি (Compound)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !isCompound,
                onClick = { isCompound = false },
                label = { Text("সরল (Simple)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = principal,
            onValueChange = { principal = it },
            label = { Text("মূলধন / আমানত (৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("সুদের হার (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = timeYears,
                onValueChange = { timeYears = it },
                label = { Text("সময় (বছর)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("মোট অর্জিত সুদ:", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalInterest.coerceAtLeast(0.0))}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("মেয়াদোত্তীর্ণ সর্বমোট অর্থ:", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(finalAmount.coerceAtLeast(0.0))}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}

// --- 8. Date Difference ---
@Composable
fun DateDifferenceCard(
    themeColors: CalculatorThemeColors
) {
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
        Text("তারিখের ব্যবধান", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Text("প্রথম তারিখ (Start Date):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = startDay, onValueChange = { startDay = it }, label = { Text("দিন") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = startMonth, onValueChange = { startMonth = it }, label = { Text("মাস") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = startYear, onValueChange = { startYear = it }, label = { Text("বছর") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1.2f), singleLine = true)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("দ্বিতীয় তারিখ (End Date):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = endDay, onValueChange = { endDay = it }, label = { Text("দিন") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = endMonth, onValueChange = { endMonth = it }, label = { Text("মাস") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = endYear, onValueChange = { endYear = it }, label = { Text("বছর") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1.2f), singleLine = true)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0EA5E9).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("তারিখ দুটির মধ্যকার ব্যবধান:", fontSize = 13.sp, color = themeColors.displayText)
                Text("${diffYears.coerceAtLeast(0)} বছর, ${diffMonths.coerceAtLeast(0)} মাস, ${diffDays.coerceAtLeast(0)} দিন", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0EA5E9))

                Spacer(modifier = Modifier.height(6.dp))
                Text("মোট আনুমানিক দিন: ${totalEstDays.coerceAtLeast(0)} দিন", fontSize = 13.sp, color = themeColors.displayText)
            }
        }
    }
}

// --- 9. Tip Calculator ---
@Composable
fun TipCalculatorCard(
    themeColors: CalculatorThemeColors
) {
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
        Text("টিপ ও বিল ভাগ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = billAmount,
            onValueChange = { billAmount = it },
            label = { Text("মোট বিলের পরিমাণ (৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = tipPercent,
                onValueChange = { tipPercent = it },
                label = { Text("টিপ শতাংশ (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = peopleCount,
                onValueChange = { peopleCount = it },
                label = { Text("মানুষের সংখ্যা") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("প্রতিজনের মোট বিল:", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(perPersonTotal)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("মোট টিপের পরিমাণ:", fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(tipAmount)} (প্রতিজনে ৳ ${df.format(perPersonTip)})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("সর্বমোট বিল (টিপসহ):", fontSize = 12.sp, color = themeColors.displayText)
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
                text = LanguageManager.getString("electricity_title", viewModel.selectedLanguage).ifEmpty { "বিদ্যুৎ বিল ক্যালকুলেটর" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            TextButton(
                onClick = { isCustomizing = !isCustomizing },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6366F1))
            ) {
                Icon(
                    imageVector = if (isCustomizing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = "Edit Rates",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCustomizing) "বন্ধ করুন" else "রেট এডিট করুন",
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
                        text = "কাস্টম চার্জ ও ট্যারিফ সেটিংস (সেভ থাকবে)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = useFlatRate,
                            onCheckedChange = { useFlatRate = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF6366F1))
                        )
                        Text(
                            text = "ফ্ল্যাট রেট ব্যবহার করুন (স্ল্যাব রেটের বদলে)",
                            fontSize = 12.sp,
                            color = themeColors.displayText
                        )
                    }

                    if (useFlatRate) {
                        CustomOutlinedTextField(
                            value = customRate,
                            onValueChange = { customRate = it },
                            label = "ইউনিট প্রতি ফ্ল্যাট চার্জ (৳)",
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
                            label = "ডিমান্ড চার্জ (৳)",
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                        CustomOutlinedTextField(
                            value = customMeter,
                            onValueChange = { customMeter = it },
                            label = "মিটার ভাড়া (৳)",
                            themeColors = themeColors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    CustomOutlinedTextField(
                        value = customVat,
                        onValueChange = { customVat = it },
                        label = "ভ্যাট হার (%)",
                        themeColors = themeColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.updateElectricityRates(customRate, customDemand, customMeter, customVat)
                            isCustomizing = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সেটিংস সেভ করুন (Save Settings)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = connectionType == "Residential",
                onClick = { connectionType = "Residential" },
                label = { Text("আবাসিক (Residential)", color = if (connectionType == "Residential") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF6366F1),
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = connectionType == "Commercial",
                onClick = { connectionType = "Commercial" },
                label = { Text("বাণিজ্যিক (Commercial)", color = if (connectionType == "Commercial") Color.White else themeColors.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF6366F1),
                    containerColor = themeColors.cardBg
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        CustomOutlinedTextField(
            value = units,
            onValueChange = { units = it },
            label = "মাসিক ব্যবহৃত ইউনিট (kWh)",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("বিদ্যুৎ শক্তি চার্জ (Energy Charge):", fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(baseEnergy)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ডিমান্ড ও মিটার চার্জ:", fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(demandAndMeterFee)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ভ্যাট (${customVat}% VAT):", fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(vat)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("সর্বমোট আনুমানিক বিল:", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalBill)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
            }
        }
    }
}

// --- 11. Appliance Energy Cost ---
@Composable
fun ApplianceEnergyCostCard(
    themeColors: CalculatorThemeColors
) {
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
        Text("সরঞ্জামের বিদ্যুৎ খরচ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(10.dp))

        // Presets row
        Text("দ্রুত নির্বাচন:", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            AssistChip(onClick = { wattage = "75" }, label = { Text("ফ্যান (75W)") })
            AssistChip(onClick = { wattage = "1500" }, label = { Text("এসি (1500W)") })
            AssistChip(onClick = { wattage = "200" }, label = { Text("ফ্রিজ (200W)") })
            AssistChip(onClick = { wattage = "100" }, label = { Text("টিভি (100W)") })
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = wattage,
                onValueChange = { wattage = it },
                label = { Text("ওয়াট (Watt)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = hoursPerDay,
                onValueChange = { hoursPerDay = it },
                label = { Text("দৈনিক ব্যবহার (ঘণ্টা)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("দৈনিক বিদ্যুৎ খরচ:", fontSize = 12.sp, color = themeColors.displayText)
                    Text("${df.format(dailyKwh)} kWh (৳ ${df.format(dailyCost)})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("মাসিক বিদ্যুৎ খরচ (৩০ দিন):", fontSize = 13.sp, color = themeColors.displayText)
                    Text("${df.format(monthlyKwh)} kWh", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("মাসিক বিলের পরিমাণ:", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(monthlyCost)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}

// --- 12. Battery / Power Bank Backup ---
@Composable
fun BatteryBackupCard(
    themeColors: CalculatorThemeColors
) {
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
        Text("ব্যাটারি / আইপিএস ব্যাকআপ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = loadWatts,
                onValueChange = { loadWatts = it },
                label = { Text("লোড (Watt)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = batteryAh,
                onValueChange = { batteryAh = it },
                label = { Text("ব্যাটারি Capacity (Ah)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = batteryVolts,
                onValueChange = { batteryVolts = it },
                label = { Text("ভোল্টেজ (V, e.g. 12V)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = efficiencyPct,
                onValueChange = { efficiencyPct = it },
                label = { Text("ইনভার্টার দক্ষতা (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("আনুমানিক ব্যাকআপ সময়:", fontSize = 13.sp, color = themeColors.displayText)
                Text("${hours.coerceAtLeast(0)} ঘণ্টা ${minutes.coerceAtLeast(0)} মিনিট", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))

                Spacer(modifier = Modifier.height(6.dp))
                Text("মোট কার্যকরী শক্তি: ${storedWh.toInt()} Wh", fontSize = 12.sp, color = themeColors.displayText)
            }
        }
    }
}

// --- 13. Fuel Cost Calculator ---
@Composable
fun FuelCostCalculatorCard(
    themeColors: CalculatorThemeColors
) {
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
        Text("জ্বালানি খরচ ক্যালকুলেটর", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = distance,
            onValueChange = { distance = it },
            label = { Text("মোট দূরত্ব (কিলোমিটার)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = mileage,
                onValueChange = { mileage = it },
                label = { Text("মাইলেজ (কিমি/লিটার)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = fuelPrice,
                onValueChange = { fuelPrice = it },
                label = { Text("তৈলের দাম (৳/লিটার)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("প্রয়োজনীয় জ্বালানি:", fontSize = 13.sp, color = themeColors.displayText)
                    Text("${df.format(fuelNeeded)} লিটার", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("সর্বমোট জ্বালানি খরচ:", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(totalCost)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("প্রতি কিলোমিটার খরচ:", fontSize = 12.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(costPerKm)} / কিমি", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
            }
        }
    }
}

// --- 14. Speed, Distance & Time ---
@Composable
fun SpeedDistanceTimeCard(
    themeColors: CalculatorThemeColors
) {
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
            Pair("গতিবেগ (Speed):", "${DecimalFormat("#,##0.00").format(speed)} km/h (${DecimalFormat("#,##0.00").format(ms)} m/s, ${DecimalFormat("#,##0.00").format(mph)} mph)")
        }
        "Distance" -> { // Distance = Speed * Time
            val distance = v1 * v2
            val miles = distance * 0.621371
            Pair("দূরত্ব (Distance):", "${DecimalFormat("#,##0.00").format(distance)} km (${DecimalFormat("#,##0.00").format(miles)} miles)")
        }
        else -> { // Time = Distance / Speed
            val hoursVal = if (v2 > 0) v1 / v2 else 0.0
            val h = hoursVal.toInt()
            val m = ((hoursVal - h) * 60).toInt()
            Pair("ভ্রমণের সময় (Time):", "$h ঘণ্টা $m মিনিট (${DecimalFormat("#,##0.00").format(hoursVal)} hours)")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("গতি, দূরত্ব ও সময়", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = calcTarget == "Speed",
                onClick = { calcTarget = "Speed"; val1 = "120"; val2 = "2" },
                label = { Text("গতি নির্ণয়") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = calcTarget == "Distance",
                onClick = { calcTarget = "Distance"; val1 = "60"; val2 = "2" },
                label = { Text("দূরত্ব নির্ণয়") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = calcTarget == "Time",
                onClick = { calcTarget = "Time"; val1 = "120"; val2 = "60" },
                label = { Text("সময় নির্ণয়") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (calcTarget) {
            "Speed" -> {
                OutlinedTextField(value = val1, onValueChange = { val1 = it }, label = { Text("দূরত্ব (কিমি)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = val2, onValueChange = { val2 = it }, label = { Text("সময় (ঘণ্টা)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            "Distance" -> {
                OutlinedTextField(value = val1, onValueChange = { val1 = it }, label = { Text("গতিবেগ (কিমি/ঘণ্টা)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = val2, onValueChange = { val2 = it }, label = { Text("সময় (ঘণ্টা)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            "Time" -> {
                OutlinedTextField(value = val1, onValueChange = { val1 = it }, label = { Text("দূরত্ব (কিমি)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = val2, onValueChange = { val2 = it }, label = { Text("গতিবেগ (কিমি/ঘণ্টা)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(resultText, fontSize = 13.sp, color = themeColors.displayText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(resultValueStr, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
            }
        }
    }
}

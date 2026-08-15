package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.LanguageManager
import com.example.util.AppLanguage
import com.example.ui.components.CustomOutlinedTextField
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

// Removed local CustomOutlinedTextField

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

// --- 1. Zakat Calculator ---
@Composable
fun ZakatCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    val isBn = lang == AppLanguage.BENGALI

    var selectedGoldUnit by remember { mutableStateOf(if (isBn) "ভরি" else "Vori") }
    var selectedGoldRateUnit by remember { mutableStateOf(if (isBn) "প্রতি ভরি" else "Per Vori") }
    var goldVori by remember { mutableStateOf("0") }
    var goldAnna by remember { mutableStateOf("0") }
    var goldRatti by remember { mutableStateOf("0") }
    var goldPrice by remember { mutableStateOf("125000") } // Default per Vori in BDT

    var selectedSilverRateUnit by remember { mutableStateOf(if (isBn) "প্রতি ভরি" else "Per Vori") }
    var silverVori by remember { mutableStateOf("0") }
    var silverAnna by remember { mutableStateOf("0") }
    var silverRatti by remember { mutableStateOf("0") }
    var silverPrice by remember { mutableStateOf("1800") } // Default per Vori in BDT

    var cashAmount by remember { mutableStateOf("") }
    var investmentAmount by remember { mutableStateOf("") }
    var debtsAmount by remember { mutableStateOf("") }

    val rateUnits = if (isBn) listOf("প্রতি ভরি", "প্রতি গ্রাম") else listOf("Per Vori", "Per Gram")

    val gVoriVal = goldVori.toDoubleOrNull() ?: 0.0
    val gAnnaVal = goldAnna.toDoubleOrNull() ?: 0.0
    val gRattiVal = goldRatti.toDoubleOrNull() ?: 0.0
    val goldPriceVal = goldPrice.toDoubleOrNull() ?: 0.0

    val sVoriVal = silverVori.toDoubleOrNull() ?: 0.0
    val sAnnaVal = silverAnna.toDoubleOrNull() ?: 0.0
    val sRattiVal = silverRatti.toDoubleOrNull() ?: 0.0
    val silverPriceVal = silverPrice.toDoubleOrNull() ?: 0.0

    val cashVal = cashAmount.toDoubleOrNull() ?: 0.0
    val investVal = investmentAmount.toDoubleOrNull() ?: 0.0
    val debtVal = debtsAmount.toDoubleOrNull() ?: 0.0

    // Gold total Vori and value calculation
    val totalGoldVoriEquivalent = gVoriVal + (gAnnaVal / 16.0) + (gRattiVal / 96.0)
    val goldPricePerGram = if (selectedGoldRateUnit == "প্রতি ভরি" || selectedGoldRateUnit == "Per Vori") {
        goldPriceVal / 11.664
    } else {
        goldPriceVal
    }
    val goldGrams = totalGoldVoriEquivalent * 11.664
    val goldValue = goldGrams * goldPricePerGram

    // Silver total Vori and value calculation
    val totalSilverVoriEquivalent = sVoriVal + (sAnnaVal / 16.0) + (sRattiVal / 96.0)
    val silverPricePerGram = if (selectedSilverRateUnit == "প্রতি ভরি" || selectedSilverRateUnit == "Per Vori") {
        silverPriceVal / 11.664
    } else {
        silverPriceVal
    }
    val silverGrams = totalSilverVoriEquivalent * 11.664
    val silverValue = silverGrams * silverPricePerGram

    val totalAssets = goldValue + silverValue + cashVal + investVal
    val netWealth = totalAssets - debtVal
    val zakatPayable = if (netWealth > 0) netWealth * 0.025 else 0.0

    val df = DecimalFormat("#,##0.##")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = LanguageManager.getString("zakat_calc_title", lang),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )
        Spacer(modifier = Modifier.height(12.dp))

        // --- 1. Gold Section (সোনার পরিমাণ ও বাজারমূল্য) ---
        Text(
            text = if (isBn) "১. সোনা (Gold)" else "1. Gold",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.buttonEqualBg
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CustomOutlinedTextField(
                value = goldVori,
                onValueChange = { goldVori = it },
                label = if (isBn) "ভরি" else "Vori",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = goldAnna,
                onValueChange = { goldAnna = it },
                label = if (isBn) "আনা" else "Anna",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = goldRatti,
                onValueChange = { goldRatti = it },
                label = if (isBn) "রতি" else "Ratti",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomOutlinedTextField(
                value = goldPrice,
                onValueChange = { goldPrice = it },
                label = if (isBn) "সোনার বাজারমূল্য" else "Gold Price",
                themeColors = themeColors,
                modifier = Modifier.weight(1.3f)
            )
            Row(modifier = Modifier.weight(1.7f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rateUnits.forEach { unit ->
                    val selected = selectedGoldRateUnit == unit
                    FilterChip(
                        selected = selected,
                        onClick = { selectedGoldRateUnit = unit },
                        label = { Text(unit, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.buttonEqualBg,
                            selectedLabelColor = Color.White,
                            containerColor = themeColors.background,
                            labelColor = themeColors.displayText
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. Silver Section (রুপার পরিমাণ ও বাজারমূল্য) ---
        Text(
            text = if (isBn) "২. রুপা (Silver)" else "2. Silver",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.buttonEqualBg
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CustomOutlinedTextField(
                value = silverVori,
                onValueChange = { silverVori = it },
                label = if (isBn) "ভরি" else "Vori",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = silverAnna,
                onValueChange = { silverAnna = it },
                label = if (isBn) "আনা" else "Anna",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = silverRatti,
                onValueChange = { silverRatti = it },
                label = if (isBn) "রতি" else "Ratti",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomOutlinedTextField(
                value = silverPrice,
                onValueChange = { silverPrice = it },
                label = if (isBn) "রুপার বাজারমূল্য" else "Silver Price",
                themeColors = themeColors,
                modifier = Modifier.weight(1.3f)
            )
            Row(modifier = Modifier.weight(1.7f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rateUnits.forEach { unit ->
                    val selected = selectedSilverRateUnit == unit
                    FilterChip(
                        selected = selected,
                        onClick = { selectedSilverRateUnit = unit },
                        label = { Text(unit, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.buttonEqualBg,
                            selectedLabelColor = Color.White,
                            containerColor = themeColors.background,
                            labelColor = themeColors.displayText
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. Other Financial Fields ---
        Text(
            text = if (isBn) "৩. অন্যান্য সম্পদ ও ঋণ" else "3. Cash, Investments & Debts",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.buttonEqualBg
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomOutlinedTextField(
                value = cashAmount,
                onValueChange = { cashAmount = it },
                label = if (isBn) "নগদ টাকা" else "Cash & Savings",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = investmentAmount,
                onValueChange = { investmentAmount = it },
                label = if (isBn) "বিনিয়োগ / শেয়ার" else "Investments",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CustomOutlinedTextField(
            value = debtsAmount,
            onValueChange = { debtsAmount = it },
            label = if (isBn) "ঋণ / দেনা" else "Debts & Liabilities",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                    Text(
                        text = if (isBn) "মোট যাকাতযোগ্য সম্পদ:" else "Total Zakatably Wealth:",
                        fontSize = 13.sp,
                        color = themeColors.displayText
                    )
                    Text(
                        text = "${if (isBn) "৳ " else "$ "}${df.format(netWealth.coerceAtLeast(0.0))}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "প্রদেয় যাকাত (২.৫%):" else "Payable Zakat (2.5%):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = "${if (isBn) "৳ " else "$ "}${df.format(zakatPayable)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                }
            }
        }
    }
}

// --- 2. Savings Target Calculator ---
@Composable
fun SavingsTargetCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var targetAmount by remember { mutableStateOf("1000000") }
    var currentSavings by remember { mutableStateOf("100000") }
    var yearsToSave by remember { mutableStateOf("5") }
    var annualInterest by remember { mutableStateOf("7") }

    val target = targetAmount.toDoubleOrNull() ?: 0.0
    val current = currentSavings.toDoubleOrNull() ?: 0.0
    val years = yearsToSave.toDoubleOrNull() ?: 1.0
    val rate = (annualInterest.toDoubleOrNull() ?: 0.0) / 100.0 / 12.0

    val months = years * 12
    val monthlySavings = if (rate > 0) {
        val numerator = target - (current * Math.pow(1 + rate, months))
        val denominator = (Math.pow(1 + rate, months) - 1) / rate
        if (denominator > 0) numerator / denominator else 0.0
    } else {
        (target - current) / months
    }

    val df = DecimalFormat("#,##0")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("সেভিংস টার্গেট", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        CustomOutlinedTextField(
            value = targetAmount,
            onValueChange = { targetAmount = it },
            label = "লক্ষ্যমাত্রা (৳)",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomOutlinedTextField(
                value = currentSavings,
                onValueChange = { currentSavings = it },
                label = "বর্তমান সঞ্চয় (৳)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = yearsToSave,
                onValueChange = { yearsToSave = it },
                label = "সময় (বছর)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CustomOutlinedTextField(
            value = annualInterest,
            onValueChange = { annualInterest = it },
            label = "বার্ষিক মুনাফার হার (%)",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("মাসিক সঞ্চয় প্রয়োজন:", fontSize = 13.sp, color = themeColors.displayText)
                Text("৳ ${df.format(monthlySavings.coerceAtLeast(0.0))}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Spacer(modifier = Modifier.height(4.dp))
                Text("পরবর্তী $months মাস ধরে সঞ্চয় করতে হবে", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
            }
        }
    }
}

// --- 3. Pregnancy Due Date Calculator ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyDueDateCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var lastPeriodDate by remember { mutableStateOf(Calendar.getInstance()) }
    val showDatePicker = remember { mutableStateOf(false) }

    val dueBy = (lastPeriodDate.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, 280) // 40 weeks
    }

    val today = Calendar.getInstance()
    val diffMillis = today.timeInMillis - lastPeriodDate.timeInMillis
    val weeks = (diffMillis / (1000 * 60 * 60 * 24 * 7)).toInt()
    val daysRemaining = (diffMillis / (1000 * 60 * 60 * 24)).toInt() % 7

    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("প্রেগনেন্সি ডিউ ডেট", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Text("শেষ পিরিয়ডের তারিখ নির্বাচন করুন:", fontSize = 12.sp, color = themeColors.displayText.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))
        
        Button(
            onClick = { showDatePicker.value = true },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.background),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(sdf.format(lastPeriodDate.time), color = themeColors.displayText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("সম্ভাব্য প্রসবের তারিখ (EDD):", fontSize = 13.sp, color = themeColors.displayText)
                Text(sdf.format(dueBy.time), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text("বর্তমান গর্ভাবস্থা:", fontSize = 13.sp, color = themeColors.displayText)
                Text("$weeks সপ্তাহ $daysRemaining দিন", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
            }
        }
    }

    if (showDatePicker.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = lastPeriodDate.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = it
                        lastPeriodDate = cal
                    }
                    showDatePicker.value = false
                }) { Text("ঠিক আছে", color = themeColors.buttonEqualBg) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// --- 4. Blood Donation Tracker ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodDonationTrackerCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var lastDonationDate by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.MONTH, -2) }) }
    val showDatePicker = remember { mutableStateOf(false) }

    val nextEligible = (lastDonationDate.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, 90) // 3 months wait
    }

    val today = Calendar.getInstance()
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val isEligible = today.after(nextEligible)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("রক্তদান ট্র্যাকার", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Text("সর্বশেষ রক্তদানের তারিখ:", fontSize = 12.sp, color = themeColors.displayText.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))
        
        Button(
            onClick = { showDatePicker.value = true },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.background),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(sdf.format(lastDonationDate.time), color = themeColors.displayText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isEligible) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("পরবর্তী রক্তদানের সম্ভাব্য তারিখ:", fontSize = 13.sp, color = themeColors.displayText)
                Text(sdf.format(nextEligible.time), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isEligible) Color(0xFF4CAF50) else Color(0xFFF44336))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isEligible) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (isEligible) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEligible) "আপনি এখন রক্তদান করতে পারবেন" else "এখনো সময় হয়নি, অপেক্ষা করুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.displayText
                    )
                }
            }
        }
    }

    if (showDatePicker.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = lastDonationDate.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = it
                        lastDonationDate = cal
                    }
                    showDatePicker.value = false
                }) { Text("ঠিক আছে", color = themeColors.buttonEqualBg) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// --- 5. Resistor Color Code Calculator ---
@Composable
fun ResistorColorCodeCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val colors = listOf(
        "Black" to Color.Black,
        "Brown" to Color(0xFF8B4513),
        "Red" to Color.Red,
        "Orange" to Color(0xFFFFA500),
        "Yellow" to Color.Yellow,
        "Green" to Color.Green,
        "Blue" to Color.Blue,
        "Violet" to Color(0xFFEE82EE),
        "Grey" to Color.Gray,
        "White" to Color.White
    )

    val multipliers = colors + listOf(
        "Gold" to Color(0xFFFFD700),
        "Silver" to Color(0xFFC0C0C0)
    )

    var band1 by remember { mutableIntStateOf(1) } // Brown
    var band2 by remember { mutableIntStateOf(0) } // Black
    var multiplierBand by remember { mutableIntStateOf(2) } // Red (x100)
    
    val resistance = (band1 * 10 + band2) * Math.pow(10.0, multiplierBand.toDouble())
    
    val formattedResistance = when {
        resistance >= 1_000_000 -> "${resistance / 1_000_000.0} MΩ"
        resistance >= 1_000 -> "${resistance / 1_000.0} kΩ"
        else -> "$resistance Ω"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("রেজিস্টর কালার কোড", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        // Visual Resistor Representation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color(0xFFDEB887), RoundedCornerShape(30.dp))
                .padding(horizontal = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(Modifier.width(12.dp).fillMaxHeight().background(colors[band1].second))
                Box(Modifier.width(12.dp).fillMaxHeight().background(colors[band2].second))
                Box(Modifier.width(12.dp).fillMaxHeight().background(multipliers[multiplierBand].second))
                Box(Modifier.width(12.dp).fillMaxHeight().background(Color(0xFFFFD700))) // Tolerance Gold fixed
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color Selectors
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("ব্যান্ড ১", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                ColorDropdown(colors, band1) { band1 = it }
            }
            Column(Modifier.weight(1f)) {
                Text("ব্যান্ড ২", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                ColorDropdown(colors, band2) { band2 = it }
            }
            Column(Modifier.weight(1f)) {
                Text("মাল্টিপ্লায়ার", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                ColorDropdown(multipliers, multiplierBand) { multiplierBand = it }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("মোট রেজিস্ট্যান্স মান:", fontSize = 13.sp, color = themeColors.displayText)
                Text(formattedResistance, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
            }
        }
    }
}

@Composable
fun ColorDropdown(colors: List<Pair<String, Color>>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
                .clickable { expanded = true }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(16.dp).clip(CircleShape).background(colors[selectedIndex].second).border(0.5.dp, Color.Gray, CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            colors.forEachIndexed { index, pair ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(14.dp).clip(CircleShape).background(pair.second).border(0.5.dp, Color.Gray, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(pair.first, fontSize = 12.sp)
                        }
                    },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

// --- 6. Color Code Converter ---
@Composable
fun ColorConverterCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val cardTitle = if (isBn) "কালার কোড কনভার্টার" else "Color Code Converter"

    var hex by remember { mutableStateOf("FF5722") }
    var r by remember { mutableStateOf("255") }
    var g by remember { mutableStateOf("87") }
    var b by remember { mutableStateOf("34") }

    fun updateFromHex(newHex: String) {
        var cleanHex = newHex.replace("#", "").trim()
        if (cleanHex.length > 6) {
            cleanHex = cleanHex.take(6)
        }
        hex = cleanHex
        try {
            if (cleanHex.length == 6) {
                val color = Color(android.graphics.Color.parseColor("#$cleanHex"))
                r = (color.red * 255).toInt().toString()
                g = (color.green * 255).toInt().toString()
                b = (color.blue * 255).toInt().toString()
            }
        } catch (e: Exception) {}
    }

    fun updateFromRgb() {
        try {
            val ri = r.toIntOrNull()?.coerceIn(0, 255) ?: 0
            val gi = g.toIntOrNull()?.coerceIn(0, 255) ?: 0
            val bi = b.toIntOrNull()?.coerceIn(0, 255) ?: 0
            hex = String.format("%02X%02X%02X", ri, gi, bi)
        } catch (e: Exception) {}
    }

    val currentColor = try { Color(android.graphics.Color.parseColor("#$hex")) } catch(e: Exception) { Color.Gray }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(cardTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(currentColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$hex",
                color = if (currentColor.toArgb() == Color.White.toArgb()) Color.Black else Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Interactive Color Wheel ---
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(180.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val touch = change.position
                        val dx = touch.x - center.x
                        val dy = touch.y - center.y
                        val radius = size.width / 2f
                        if (radius > 0) {
                            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                            val angleRad = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
                            val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                            val hueVal = (angleDeg + 360f) % 360f
                            val satVal = (dist / radius).coerceIn(0f, 1f)
                            
                            val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hueVal, satVal, 1f))
                            val red = android.graphics.Color.red(colorInt)
                            val green = android.graphics.Color.green(colorInt)
                            val blue = android.graphics.Color.blue(colorInt)
                            
                            hex = String.format("%02X%02X%02X", red, green, blue)
                            r = red.toString()
                            g = green.toString()
                            b = blue.toString()
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { touch ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = touch.x - center.x
                        val dy = touch.y - center.y
                        val radius = size.width / 2f
                        if (radius > 0) {
                            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                            val angleRad = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
                            val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                            val hueVal = (angleDeg + 360f) % 360f
                            val satVal = (dist / radius).coerceIn(0f, 1f)
                            
                            val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hueVal, satVal, 1f))
                            val red = android.graphics.Color.red(colorInt)
                            val green = android.graphics.Color.green(colorInt)
                            val blue = android.graphics.Color.blue(colorInt)
                            
                            hex = String.format("%02X%02X%02X", red, green, blue)
                            r = red.toString()
                            g = green.toString()
                            b = blue.toString()
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                
                val sweepColors = listOf(
                    Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                )
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.sweepGradient(sweepColors, center),
                    radius = radius
                )
                
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(Color.White, Color.Transparent),
                        center = center,
                        radius = radius
                    ),
                    radius = radius
                )
                
                val hsv = FloatArray(3)
                try {
                    val ri = r.toIntOrNull() ?: 255
                    val gi = g.toIntOrNull() ?: 87
                    val bi = b.toIntOrNull() ?: 34
                    android.graphics.Color.RGBToHSV(ri, gi, bi, hsv)
                } catch (e: Exception) {}
                
                val currentHue = hsv[0]
                val currentSat = hsv[1]
                
                val angleRad = Math.toRadians(currentHue.toDouble()).toFloat()
                val dist = currentSat * radius
                val thumbX = center.x + dist * Math.cos(angleRad.toDouble()).toFloat()
                val thumbY = center.y + dist * Math.sin(angleRad.toDouble()).toFloat()
                
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = 10.dp.toPx(),
                    center = Offset(thumbX, thumbY + 1.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = Offset(thumbX, thumbY)
                )
                drawCircle(
                    color = currentColor,
                    radius = 6.dp.toPx(),
                    center = Offset(thumbX, thumbY)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        CustomOutlinedTextField(
            value = hex,
            onValueChange = { updateFromHex(it) },
            label = if (isBn) "হেক্স কোড (HEX)" else "HEX Code",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Ascii
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomOutlinedTextField(
                value = r,
                onValueChange = { r = it; updateFromRgb() },
                label = if (isBn) "লাল (Red)" else "Red (R)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = g,
                onValueChange = { g = it; updateFromRgb() },
                label = if (isBn) "সবুজ (Green)" else "Green (G)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = b,
                onValueChange = { b = it; updateFromRgb() },
                label = if (isBn) "নীল (Blue)" else "Blue (B)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// --- 7. Roman Numerals Converter ---
@Composable
fun RomanNumeralsCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    var integerInput by remember { mutableStateOf("10") }
    var romanInput by remember { mutableStateOf("X") }

    fun intToRoman(num: Int): String {
        var n = num
        val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val romanLetters = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        val roman = StringBuilder()
        for (i in values.indices) {
            while (n >= values[i]) {
                n -= values[i]
                roman.append(romanLetters[i])
            }
        }
        return roman.toString()
    }

    fun romanToInt(s: String): Int {
        val map = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
        var res = 0
        var i = 0
        while (i < s.length) {
            val s1 = map[s[i]] ?: 0
            if (i + 1 < s.length) {
                val s2 = map[s[i + 1]] ?: 0
                if (s1 >= s2) {
                    res += s1
                    i++
                } else {
                    res += s2 - s1
                    i += 2
                }
            } else {
                res += s1
                i++
            }
        }
        return res
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("রোমান সংখ্যা কনভার্টার", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        CustomOutlinedTextField(
            value = integerInput,
            onValueChange = {
                integerInput = it
                val intVal = it.toIntOrNull() ?: 0
                if (intVal in 1..3999) {
                    romanInput = intToRoman(intVal)
                }
            },
            label = "পূর্ণ সংখ্যা (১-৩৯৯৯)",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.SwapVert, contentDescription = null, tint = themeColors.buttonEqualBg)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        CustomOutlinedTextField(
            value = romanInput,
            onValueChange = {
                val input = it.uppercase()
                romanInput = input
                if (input.isNotEmpty()) {
                    integerInput = romanToInt(input).toString()
                }
            },
            label = "রোমান সংখ্যা",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Ascii
        )
    }
}

// --- 8. Time Zone Converter ---
@Composable
fun TimeZoneCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val zones = listOf(
        "UTC" to "UTC",
        "Dhaka" to "Asia/Dhaka",
        "New York" to "America/New_York",
        "London" to "Europe/London",
        "India" to "Asia/Kolkata",
        "Tokyo" to "Asia/Tokyo",
        "Dubai" to "Asia/Dubai",
        "Sydney" to "Australia/Sydney"
    )

    var fromZoneIndex by remember { mutableIntStateOf(1) } // Dhaka
    var toZoneIndex by remember { mutableIntStateOf(2) } // NY
    
    var hour by remember { mutableStateOf("12") }
    var minute by remember { mutableStateOf("00") }
    var isAm by remember { mutableStateOf(true) }

    fun convertTime(): String {
        try {
            val fromZoneId = TimeZone.getTimeZone(zones[fromZoneIndex].second)
            val toZoneId = TimeZone.getTimeZone(zones[toZoneIndex].second)
            
            val calendar = Calendar.getInstance(fromZoneId)
            var h = hour.toIntOrNull() ?: 12
            if (h == 12) h = 0
            if (!isAm) h += 12
            
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, minute.toIntOrNull() ?: 0)
            
            val sdf = SimpleDateFormat("hh:mm a (EEEE)", Locale.getDefault())
            sdf.timeZone = toZoneId
            return sdf.format(calendar.time)
        } catch (e: Exception) {
            return "Error"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("টাইম জোন কনভার্টার", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = hour,
                onValueChange = { if (it.length <= 2) hour = it },
                label = "ঘণ্টা",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = minute,
                onValueChange = { if (it.length <= 2) minute = it },
                label = "মিনিট",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            Column(Modifier.weight(1f)) {
                Button(
                    onClick = { isAm = !isAm },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.background),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(if (isAm) "AM" else "PM", color = themeColors.buttonEqualBg, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("থেকে", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                TimeZoneDropdown(zones, fromZoneIndex) { fromZoneIndex = it }
            }
            Column(Modifier.weight(1f)) {
                Text("পর্যন্ত", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                TimeZoneDropdown(zones, toZoneIndex) { toZoneIndex = it }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${zones[toZoneIndex].first} এর সময়:", fontSize = 13.sp, color = themeColors.displayText)
                Text(convertTime(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
            }
        }
    }
}

@Composable
fun TimeZoneDropdown(zones: List<Pair<String, String>>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
                .clickable { expanded = true }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(zones[selectedIndex].first, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            zones.forEachIndexed { index, pair ->
                DropdownMenuItem(
                    text = { Text(pair.first, fontSize = 13.sp) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothMeasurementCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    val isBn = lang == AppLanguage.BENGALI
    val df = remember { DecimalFormat("#.####") }

    // Mixed calculator state
    var mixedGaj by remember { mutableStateOf("2") }
    var mixedGira by remember { mutableStateOf("4") }

    // Direct unit converter states (synchronous/reactive updates)
    var gajVal by remember { mutableStateOf("1") }
    var giraVal by remember { mutableStateOf("16") }
    var haatVal by remember { mutableStateOf("2") }
    var inchVal by remember { mutableStateOf("36") }
    var meterVal by remember { mutableStateOf("0.9144") }

    fun updateAllFromInches(inches: Double, sourceUnit: String) {
        val calculatedGaj = inches / 36.0
        val calculatedGira = inches / 2.25
        val calculatedHaat = inches / 18.0
        val calculatedMeter = inches * 0.0254

        if (sourceUnit != "gaj") gajVal = if (inches == 0.0) "" else df.format(calculatedGaj)
        if (sourceUnit != "gira") giraVal = if (inches == 0.0) "" else df.format(calculatedGira)
        if (sourceUnit != "haat") haatVal = if (inches == 0.0) "" else df.format(calculatedHaat)
        if (sourceUnit != "inch") inchVal = if (inches == 0.0) "" else df.format(inches)
        if (sourceUnit != "meter") meterVal = if (inches == 0.0) "" else df.format(calculatedMeter)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Section 1: Mixed Gaj & Gira
        Text(
            text = if (isBn) "গজ ও গিরা মিশ্রিত হিসাবকারী" else "Gaj & Gira Mixed Calculator",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = mixedGaj,
                onValueChange = {
                    mixedGaj = it
                },
                label = if (isBn) "গজ" else "Gaj",
                themeColors = themeColors,
                modifier = Modifier.weight(1f).testTag("cloth_mixed_gaj")
            )
            CustomOutlinedTextField(
                value = mixedGira,
                onValueChange = {
                    mixedGira = it
                },
                label = if (isBn) "গিরা" else "Gira",
                themeColors = themeColors,
                modifier = Modifier.weight(1f).testTag("cloth_mixed_gira")
            )
        }

        val mGaj = mixedGaj.toDoubleOrNull() ?: 0.0
        val mGira = mixedGira.toDoubleOrNull() ?: 0.0
        val totalInches = (mGaj * 36.0) + (mGira * 2.25)
        val totalHaat = totalInches / 18.0
        val totalMeters = totalInches * 0.0254
        val decimalGaj = totalInches / 36.0

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isBn) "মোট দৈর্ঘ্য রূপান্তর ফলাফল:" else "Total Length Conversions:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if (isBn) "ইঞ্চি (Inches):" else "Inches:", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                        Text("${df.format(totalInches)} Inch", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                    Column {
                        Text(if (isBn) "হাত (Haat):" else "Haat:", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                        Text("${df.format(totalHaat)} Haat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if (isBn) "মিটার (Meters):" else "Meters:", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                        Text("${df.format(totalMeters)} m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                    Column {
                        Text(if (isBn) "দশমিক গজ (Decimal Gaj):" else "Decimal Gaj:", fontSize = 11.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                        Text("${df.format(decimalGaj)} Gaj", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = themeColors.displayText.copy(alpha = 0.15f))
        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Real-time Unit Grid Converter
        Text(
            text = if (isBn) "যেকোনো একক থেকে সরাসরি রূপান্তর" else "Real-time Direct Converter",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomOutlinedTextField(
            value = gajVal,
            onValueChange = {
                gajVal = it
                val d = it.toDoubleOrNull() ?: 0.0
                updateAllFromInches(d * 36.0, "gaj")
            },
            label = if (isBn) "গজ (Gaj)" else "Gaj",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("cloth_gaj_input")
        )

        CustomOutlinedTextField(
            value = giraVal,
            onValueChange = {
                giraVal = it
                val d = it.toDoubleOrNull() ?: 0.0
                updateAllFromInches(d * 2.25, "gira")
            },
            label = if (isBn) "গিরা (Gira)" else "Gira",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("cloth_gira_input")
        )

        CustomOutlinedTextField(
            value = haatVal,
            onValueChange = {
                haatVal = it
                val d = it.toDoubleOrNull() ?: 0.0
                updateAllFromInches(d * 18.0, "haat")
            },
            label = if (isBn) "হাত (Haat)" else "Haat",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("cloth_haat_input")
        )

        CustomOutlinedTextField(
            value = inchVal,
            onValueChange = {
                inchVal = it
                val d = it.toDoubleOrNull() ?: 0.0
                updateAllFromInches(d, "inch")
            },
            label = if (isBn) "ইঞ্চি (Inch)" else "Inch",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("cloth_inch_input")
        )

        CustomOutlinedTextField(
            value = meterVal,
            onValueChange = {
                meterVal = it
                val d = it.toDoubleOrNull() ?: 0.0
                updateAllFromInches(d / 0.0254, "meter")
            },
            label = if (isBn) "মিটার (Meter)" else "Meter",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("cloth_meter_input")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    val isBn = lang == AppLanguage.BENGALI
    val df = remember { DecimalFormat("#.##") }
    val dfFour = remember { DecimalFormat("#.####") }

    var selectedMetalTab by remember { mutableStateOf("gold") } // "gold" or "silver"

    var voriStr by remember { mutableStateOf("1") }
    var annaStr by remember { mutableStateOf("0") }
    var rattiStr by remember { mutableStateOf("0") }
    var pointStr by remember { mutableStateOf("0") }

    var selectedCarat by remember { mutableStateOf("22K") }
    var goldPricePerVori by remember { mutableStateOf("125000") } // Default Gold Price per Vori BDT
    var silverPricePerVori by remember { mutableStateOf("2100") } // Default Silver Price per Vori BDT

    var goldMakingChargeStr by remember { mutableStateOf("5000") } // Flat making charge in BDT for Gold
    var silverMakingChargeStr by remember { mutableStateOf("300") } // Flat making charge in BDT for Silver
    var vatPercentStr by remember { mutableStateOf("5") } // 5% standard VAT

    val carats = listOf("22K", "21K", "18K", "Sanatan")

    val isGold = selectedMetalTab == "gold"
    val currentPricePerVoriStr = if (isGold) goldPricePerVori else silverPricePerVori
    val currentMakingChargeStr = if (isGold) goldMakingChargeStr else silverMakingChargeStr

    // Automatic rate suggest based on Carat relative to 22K rate
    val rawBaseRate = currentPricePerVoriStr.toDoubleOrNull() ?: 0.0
    val derivedRate = when (selectedCarat) {
        "22K" -> rawBaseRate
        "21K" -> rawBaseRate * (21.0 / 22.0)
        "18K" -> rawBaseRate * (18.0 / 22.0)
        else -> rawBaseRate * 0.65 // Sanatan has ~65% pure metal rate
    }

    // Weight Calculation
    val vori = voriStr.toDoubleOrNull() ?: 0.0
    val anna = annaStr.toDoubleOrNull() ?: 0.0
    val ratti = rattiStr.toDoubleOrNull() ?: 0.0
    val point = pointStr.toDoubleOrNull() ?: 0.0

    // 1 Vori = 16 Anna = 96 Ratti = 960 Point
    val totalVori = vori + (anna / 16.0) + (ratti / 96.0) + (point / 960.0)
    val totalGrams = totalVori * 11.664

    // Price Calculations
    val metalOnlyPrice = totalVori * derivedRate
    val makingCharge = currentMakingChargeStr.toDoubleOrNull() ?: 0.0
    val vatPercent = vatPercentStr.toDoubleOrNull() ?: 0.0

    val baseWithMaking = metalOnlyPrice + makingCharge
    val vatAmount = baseWithMaking * (vatPercent / 100.0)
    val totalPrice = baseWithMaking + vatAmount

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = if (isBn) "স্বর্ণ ও রৌপ্য মূল্য ক্যালকুলেটর" else "Gold & Silver Price Calculator",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.displayText
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Metal Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { selectedMetalTab = "gold" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGold) themeColors.buttonEqualBg else themeColors.buttonNormalBg,
                    contentColor = if (isGold) themeColors.buttonEqualText else themeColors.buttonNormalText
                ),
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isBn) "🏆 স্বর্ণ (Gold)" else "🏆 Gold",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { selectedMetalTab = "silver" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isGold) themeColors.buttonEqualBg else themeColors.buttonNormalBg,
                    contentColor = if (!isGold) themeColors.buttonEqualText else themeColors.buttonNormalText
                ),
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isBn) "🥈 রৌপ্য (Silver)" else "🥈 Silver",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Weight Inputs (Vori, Anna, Ratti, Point)
        Text(
            text = if (isGold) {
                if (isBn) "স্বর্ণের ওজন:" else "Gold Weight:"
            } else {
                if (isBn) "রৌপ্যের ওজন:" else "Silver Weight:"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = themeColors.displayText.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = voriStr,
                onValueChange = { voriStr = it },
                label = if (isBn) "ভরি" else "Vori",
                themeColors = themeColors,
                modifier = Modifier.weight(1.0f).testTag("gold_vori_input")
            )
            CustomOutlinedTextField(
                value = annaStr,
                onValueChange = { annaStr = it },
                label = if (isBn) "আনা" else "Anna",
                themeColors = themeColors,
                modifier = Modifier.weight(1.0f).testTag("gold_anna_input")
            )
            CustomOutlinedTextField(
                value = rattiStr,
                onValueChange = { rattiStr = it },
                label = if (isBn) "রতি" else "Ratti",
                themeColors = themeColors,
                modifier = Modifier.weight(1.0f).testTag("gold_ratti_input")
            )
            CustomOutlinedTextField(
                value = pointStr,
                onValueChange = { pointStr = it },
                label = if (isBn) "পয়েন্ট" else "Point",
                themeColors = themeColors,
                modifier = Modifier.weight(1.0f).testTag("gold_point_input")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Carat selection Row
        Text(
            text = if (isGold) {
                if (isBn) "স্বর্ণের মান (ক্যারেট):" else "Gold Quality (Carat):"
            } else {
                if (isBn) "রৌপ্যের মান (ক্যারেট/হলমার্ক):" else "Silver Quality (Carat):"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = themeColors.displayText.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            carats.forEach { carat ->
                val isSelected = selectedCarat == carat
                val caratLabel = if (!isGold && carat == "Sanatan") (if (isBn) "সনাতন" else "Sanatan") else carat
                Button(
                    onClick = { selectedCarat = carat },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) themeColors.buttonEqualBg else themeColors.buttonNormalBg,
                        contentColor = if (isSelected) themeColors.buttonEqualText else themeColors.buttonNormalText
                    ),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = caratLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Price Input
        CustomOutlinedTextField(
            value = if (isGold) goldPricePerVori else silverPricePerVori,
            onValueChange = {
                if (isGold) goldPricePerVori = it else silverPricePerVori = it
            },
            label = if (isGold) {
                if (isBn) "২২ ক্যারেট স্বর্ণের প্রতি ভরি মূল্য (৳)" else "22K Gold Price Per Vori (৳)"
            } else {
                if (isBn) "২২ ক্যারেট রৌপ্যের প্রতি ভরি মূল্য (৳)" else "22K Silver Price Per Vori (৳)"
            },
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth().testTag("gold_price_per_vori_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CustomOutlinedTextField(
                value = if (isGold) goldMakingChargeStr else silverMakingChargeStr,
                onValueChange = {
                    if (isGold) goldMakingChargeStr = it else silverMakingChargeStr = it
                },
                label = if (isBn) "মজুরি (৳)" else "Making Charge (৳)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f).testTag("gold_making_charge_input")
            )
            CustomOutlinedTextField(
                value = vatPercentStr,
                onValueChange = { vatPercentStr = it },
                label = if (isBn) "ভ্যাট (%)" else "VAT (%)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f).testTag("gold_vat_input")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isBn) {
                        if (isGold) "স্বর্ণের হিসাব বিবরণী" else "রৌপ্যের হিসাব বিবরণী"
                    } else {
                        if (isGold) "Gold Calculated Details" else "Silver Calculated Details"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBn) "মোট ওজন (ভরি):" else "Total Weight (Vori):", fontSize = 12.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                    Text("${dfFour.format(totalVori)} ভরি / Vori", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBn) "মোট ওজন (গ্রাম):" else "Total Weight (Grams):", fontSize = 12.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                    Text("${dfFour.format(totalGrams)} গ্রাম / Grams", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBn) "ধার্যকৃত প্রতি ভরি দর:" else "Effective Rate/Vori:", fontSize = 12.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                    Text("৳ ${df.format(derivedRate)} ($selectedCarat)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        if (isBn) {
                            if (isGold) "স্বর্ণের প্রকৃত মূল্য:" else "রৌপ্যের প্রকৃত মূল্য:"
                        } else {
                            if (isGold) "Gold Value Only:" else "Silver Value Only:"
                        },
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                    Text("৳ ${df.format(metalOnlyPrice)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBn) "ভ্যাট পরিমাণ:" else "VAT Amount:", fontSize = 12.sp, color = themeColors.displayText.copy(alpha = 0.7f))
                    Text("৳ ${df.format(vatAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = themeColors.displayText.copy(alpha = 0.15f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBn) "সর্বমোট প্রদেয় মূল্য:" else "Total Payable Price:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    Text("৳ ${df.format(totalPrice)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
                }
            }
        }
    }
}

@Composable
fun CollapsibleInfoSection(
    title: String,
    infoItems: List<Pair<String, String>>,
    themeColors: CalculatorThemeColors
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Divider(color = themeColors.displayText.copy(alpha = 0.12f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Collapse/Expand",
                tint = themeColors.displayText.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                infoItems.forEach { (subtitle, content) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeColors.background.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, themeColors.displayText.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = content,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

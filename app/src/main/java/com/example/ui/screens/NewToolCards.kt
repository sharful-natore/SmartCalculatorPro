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
import com.example.ui.components.CustomOutlinedTextField
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

// Removed local CustomOutlinedTextField

// --- 1. Zakat Calculator ---
@Composable
fun ZakatCalculatorCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val lang = viewModel.selectedLanguage
    var goldAmount by remember { mutableStateOf("") }
    var silverAmount by remember { mutableStateOf("") }
    var cashAmount by remember { mutableStateOf("") }
    var investmentAmount by remember { mutableStateOf("") }
    var debtsAmount by remember { mutableStateOf("") }
    var goldPrice by remember { mutableStateOf("7000") } // Per gram (example)

    val goldVal = goldAmount.toDoubleOrNull() ?: 0.0
    val silverVal = silverAmount.toDoubleOrNull() ?: 0.0
    val cashVal = cashAmount.toDoubleOrNull() ?: 0.0
    val investVal = investmentAmount.toDoubleOrNull() ?: 0.0
    val debtVal = debtsAmount.toDoubleOrNull() ?: 0.0
    val priceVal = goldPrice.toDoubleOrNull() ?: 0.0

    val totalAssets = (goldVal * priceVal) + cashVal + investVal
    val netWealth = totalAssets - debtVal
    val zakatPayable = if (netWealth >= (87.48 * priceVal)) netWealth * 0.025 else 0.0

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

        CustomOutlinedTextField(
            value = goldPrice,
            onValueChange = { goldPrice = it },
            label = "স্বর্ণের বাজারমূল্য (প্রতি গ্রাম)",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomOutlinedTextField(
                value = goldAmount,
                onValueChange = { goldAmount = it },
                label = "স্বর্ণ (গ্রাম)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = cashAmount,
                onValueChange = { cashAmount = it },
                label = "নগদ টাকা",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomOutlinedTextField(
                value = investmentAmount,
                onValueChange = { investmentAmount = it },
                label = "বিনিয়োগ/শেয়ার",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = debtsAmount,
                onValueChange = { debtsAmount = it },
                label = "ঋণ/দেনা",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
        }

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
                    Text("মোট যাকাতযোগ্য সম্পদ:", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(netWealth.coerceAtLeast(0.0))}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("প্রদেয় যাকাত (২.৫%):", fontSize = 13.sp, color = themeColors.displayText)
                    Text("৳ ${df.format(zakatPayable)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
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
    var hex by remember { mutableStateOf("FF5722") }
    var r by remember { mutableStateOf("255") }
    var g by remember { mutableStateOf("87") }
    var b by remember { mutableStateOf("34") }

    fun updateFromHex(newHex: String) {
        hex = newHex
        try {
            val color = Color(android.graphics.Color.parseColor("#$newHex"))
            r = (color.red * 255).toInt().toString()
            g = (color.green * 255).toInt().toString()
            b = (color.blue * 255).toInt().toString()
        } catch (e: Exception) {}
    }

    fun updateFromRgb() {
        try {
            val ri = r.toInt().coerceIn(0, 255)
            val gi = g.toInt().coerceIn(0, 255)
            val bi = b.toInt().coerceIn(0, 255)
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
        Text("কালার কোড কনভার্টার", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(currentColor),
            contentAlignment = Alignment.Center
        ) {
            Text("#$hex", color = if (currentColor.toArgb() == Color.White.toArgb()) Color.Black else Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomOutlinedTextField(
            value = hex,
            onValueChange = { updateFromHex(it) },
            label = "HEX Code",
            themeColors = themeColors,
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Ascii
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomOutlinedTextField(
                value = r,
                onValueChange = { r = it; updateFromRgb() },
                label = "Red (R)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = g,
                onValueChange = { g = it; updateFromRgb() },
                label = "Green (G)",
                themeColors = themeColors,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = b,
                onValueChange = { b = it; updateFromRgb() },
                label = "Blue (B)",
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

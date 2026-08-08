package com.example.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.HistoryEntry
import com.example.data.repository.HistoryRepository
import com.example.ui.theme.CalculatorThemeType
import com.example.util.ExpressionEvaluator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class CalculatorViewModel(
    private val repository: HistoryRepository,
    private val context: Context
) : ViewModel() {

    // --- State Variables ---
    var expression by mutableStateOf("")
        private set

    var result by mutableStateOf("")
        private set

    var isDegreeMode by mutableStateOf(true)

    var isScientificExpanded by mutableStateOf(false)

    // Current Active Tab: 0 = Calculator, 1 = Converter, 2 = Special, 3 = History, 4 = Themes
    var activeTab by mutableStateOf(0)
    var isEvaluated by mutableStateOf(false)

    // History deletion confirmation state
    var showClearHistoryDialog by mutableStateOf(false)
    var showDeleteSingleDialog by mutableStateOf(false)
    var pendingDeleteId by mutableStateOf<Long?>(null)

    // Theme Selection
    private val sharedPrefs = context.getSharedPreferences("smart_calc_prefs", Context.MODE_PRIVATE)
    var currentThemeType by mutableStateOf(
        CalculatorThemeType.valueOf(
            sharedPrefs.getString("selected_theme", CalculatorThemeType.SLEEK_INTERFACE.name) 
                ?: CalculatorThemeType.SLEEK_INTERFACE.name
        )
    )
        private set

    // History list from Room Flow
    val historyList: StateFlow<List<HistoryEntry>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Unit Converter States ---
    var unitCategory by mutableStateOf(UnitCategory.LENGTH)
    var fromUnit by mutableStateOf("Meter")
    var toUnit by mutableStateOf("Foot")
    var converterInput by mutableStateOf("1")
    var converterOutput by mutableStateOf("3.2808")

    // --- Special Calculators States ---
    // 1. BMI
    var bmiGender by mutableStateOf("Male")
    var bmiWeight by mutableStateOf("70")
    var bmiWeightUnit by mutableStateOf("kg") // "kg" or "lb"
    var bmiHeight by mutableStateOf("170")
    var bmiHeightUnit by mutableStateOf("ft/in") // "cm" or "ft/in"
    var bmiHeightFt by mutableStateOf("5")
    var bmiHeightIn by mutableStateOf("7")
    var bmiAge by mutableStateOf("25")
    var bmiResultValue by mutableStateOf("")
    var bmiCategoryResult by mutableStateOf("")

    // 2. Age
    var ageDob by mutableStateOf("1/1/2000")
    var ageYearsResult by mutableStateOf("0")
    var ageMonthsResult by mutableStateOf("0")
    var ageDaysResult by mutableStateOf("0")
    var birthDateString by mutableStateOf("2000-01-01")
    var targetDateString by mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    var ageResultText by mutableStateOf("")

    // 3. Discount
    var originalPrice by mutableStateOf("1000")
    var discountPercent by mutableStateOf("15")
    var taxPercent by mutableStateOf("5")
    var finalPriceResult by mutableStateOf("")
    var discountSavingsResult by mutableStateOf("")

    // 4. Percentage
    var percentValueA by mutableStateOf("50")
    var percentValueB by mutableStateOf("200")
    var percentageResultText by mutableStateOf("")

    init {
        // Trigger initial calculations
        calculateConverter()
        calculateBMI()
        calculateAge()
        calculateDiscount()
        calculatePercentage()
    }

    // --- Calculator Logic ---
    private fun isOperator(char: String): Boolean {
        return char in setOf("+", "−", "×", "÷", "%", "^", "x^y") || char == "−" || char == "-"
    }

    fun processVoiceInput(text: String) {
        var processedText = text.lowercase(Locale.getDefault())
        // Replace Bengali words with operators and English numbers
        processedText = processedText.replace("যোগ", "+")
            .replace("বিয়োগ", "−")
            .replace("বিয়োগ", "−")
            .replace("গুণ", "×")
            .replace("গুন", "×")
            .replace("ভাগ", "÷")
            .replace("দশমিক", ".")
            .replace("শতকরা", "%")
            .replace("পয়েন্ট", ".")
            .replace("পয়েন্ট", ".")

        // Convert Bengali numerals to English numerals
        val bengaliToEnglishNumbers = mapOf(
            '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
            '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
        )
        val sb = java.lang.StringBuilder()
        for (char in processedText) {
            sb.append(bengaliToEnglishNumbers[char] ?: char)
        }
        processedText = sb.toString()
        
        // Remove spaces and alphabetic characters to keep only math expression
        processedText = processedText.replace(Regex("[^0-9\\+\\−\\×\\÷\\%\\.\\(\\)\\^]"), "")

        if (processedText.isNotEmpty()) {
            expression = processedText
            evaluateExpression()
        }
    }

    fun onBtnClick(char: String) {
        if (isEvaluated) {
            if (char != "AC" && char != "C" && char != "=" && char != "DEG" && char != "RAD" && char != "±") {
                if (isOperator(char)) {
                    // Continue from previous result
                    expression = result
                } else {
                    // Start new expression
                    expression = ""
                    result = ""
                }
            }
            isEvaluated = false
        }

        when (char) {
            "AC" -> {
                expression = ""
                result = ""
            }
            "C" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
                tryEvaluatePreview()
            }
            "=" -> {
                evaluateExpression()
            }
            "DEG", "RAD" -> {
                isDegreeMode = !isDegreeMode
                tryEvaluatePreview()
            }
            "sin", "cos", "tan", "ln", "log", "√", "antilog" -> {
                expression += "$char("
                tryEvaluatePreview()
            }
            "sin⁻¹", "cos⁻¹", "tan⁻¹" -> {
                expression += "$char("
                tryEvaluatePreview()
            }
            "log^10", "log10" -> {
                expression += "log10("
                tryEvaluatePreview()
            }
            "3√" -> {
                expression += "3√("
                tryEvaluatePreview()
            }
            "x^y" -> {
                expression += "^"
                tryEvaluatePreview()
            }
            "x²" -> {
                expression += "^2"
                tryEvaluatePreview()
            }
            "x³" -> {
                expression += "^3"
                tryEvaluatePreview()
            }
            "1/x" -> {
                expression += "1÷"
                tryEvaluatePreview()
            }
            "e^x" -> {
                expression += "e^"
                tryEvaluatePreview()
            }
            "x!" -> {
                expression += "!"
                tryEvaluatePreview()
            }
            "π", "e" -> {
                expression += char
                tryEvaluatePreview()
            }
            "()" -> {
                val tempExpr = expression
                if (tempExpr.isEmpty()) {
                    expression += "("
                } else {
                    val lastChar = tempExpr.last()
                    val openCount = tempExpr.count { it == '(' }
                    val closeCount = tempExpr.count { it == ')' }
                    
                    if (openCount > closeCount) {
                        if (lastChar in setOf('+', '−', '×', '÷', '(', '^', '-')) {
                            expression += "("
                        } else {
                            expression += ")"
                        }
                    } else {
                        if (lastChar.isDigit() || lastChar == ')' || lastChar == 'e' || lastChar == 'π') {
                            expression += "×("
                        } else {
                            expression += "("
                        }
                    }
                }
                tryEvaluatePreview()
            }
            "±" -> {
                if (expression.startsWith("-")) {
                    expression = expression.substring(1)
                } else if (expression.isNotEmpty()) {
                    expression = "-$expression"
                }
                tryEvaluatePreview()
            }
            else -> {
                expression += char
                tryEvaluatePreview()
            }
        }
    }

    private fun tryEvaluatePreview() {
        if (expression.isBlank()) {
            result = ""
            return
        }
        try {
            // Check if there are unclosed parentheses and temporarily close them for preview
            var tempExpr = expression
            val openCount = tempExpr.count { it == '(' }
            val closeCount = tempExpr.count { it == ')' }
            if (openCount > closeCount) {
                tempExpr += ")".repeat(openCount - closeCount)
            }

            val evalResult = ExpressionEvaluator.evaluate(tempExpr, isDegreeMode)
            if (!evalResult.isInfinite() && !evalResult.isNaN()) {
                val df = DecimalFormat("#.########")
                result = df.format(evalResult)
            }
        } catch (e: Exception) {
            // Silently keep previous or clear preview if invalid
        }
    }

    private fun evaluateExpression() {
        if (expression.isBlank()) return
        try {
            var finalExpr = expression
            val openCount = finalExpr.count { it == '(' }
            val closeCount = finalExpr.count { it == ')' }
            if (openCount > closeCount) {
                finalExpr += ")".repeat(openCount - closeCount)
            }

            val evalResult = ExpressionEvaluator.evaluate(finalExpr, isDegreeMode)
            
            // Format result beautifully
            val formatted = if (evalResult.isInfinite()) {
                "Infinity"
            } else if (evalResult.isNaN()) {
                "NaN"
            } else {
                val df = DecimalFormat("#.########")
                df.format(evalResult)
            }
            
            result = formatted
            isEvaluated = true

            // Save to database
            viewModelScope.launch {
                repository.insertHistory(
                    HistoryEntry(
                        expression = expression,
                        result = formatted,
                        type = "Calculator"
                    )
                )
            }
        } catch (e: Exception) {
            result = "Error"
            isEvaluated = true
        }
    }

    fun selectHistoryItem(entry: HistoryEntry) {
        expression = entry.expression
        result = entry.result
        activeTab = 0 // Switch to calculator
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Theme Controller ---
    fun setTheme(theme: CalculatorThemeType) {
        currentThemeType = theme
        sharedPrefs.edit().putString("selected_theme", theme.name).apply()
    }

    // --- Unit Converter Engine ---
    enum class UnitCategory(val label: String, val labelBn: String) {
        LENGTH("Length", "দৈর্ঘ্য"),
        WEIGHT("Weight", "ওজন"),
        AREA("Area", "ক্ষেত্রফল"),
        VOLUME("Volume", "আয়তন"),
        TEMPERATURE("Temperature", "তাপমাত্রা")
    }

    fun onUnitCategoryChange(category: UnitCategory) {
        unitCategory = category
        val units = getUnitsForCategory(category)
        fromUnit = units.firstOrNull() ?: ""
        toUnit = units.getOrNull(1) ?: fromUnit
        calculateConverter()
    }

    fun getUnitsForCategory(category: UnitCategory): List<String> {
        return when (category) {
            UnitCategory.LENGTH -> listOf("Meter", "Kilometer", "Centimeter", "Millimeter", "Mile", "Yard", "Foot", "Inch")
            UnitCategory.WEIGHT -> listOf("Kilogram", "Gram", "Milligram", "Pound", "Ounce")
            UnitCategory.AREA -> listOf("Square Meter", "Square Kilometer", "Acre", "Hectare", "Square Feet")
            UnitCategory.VOLUME -> listOf("Liter", "Milliliter", "Gallon", "Quart", "Cup")
            UnitCategory.TEMPERATURE -> listOf("Celsius", "Fahrenheit", "Kelvin")
        }
    }

    fun swapUnits() {
        val temp = fromUnit
        fromUnit = toUnit
        toUnit = temp
        calculateConverter()
    }

    fun calculateConverter() {
        val inputVal = converterInput.toDoubleOrNull() ?: 0.0
        val outVal = convertUnits(unitCategory, fromUnit, toUnit, inputVal)
        val df = DecimalFormat("#.######")
        converterOutput = df.format(outVal)
    }

    private fun convertUnits(category: UnitCategory, from: String, to: String, value: Double): Double {
        if (from == to) return value
        return when (category) {
            UnitCategory.LENGTH -> {
                // Base unit: Meter
                val inMeters = when (from) {
                    "Meter" -> value
                    "Kilometer" -> value * 1000.0
                    "Centimeter" -> value / 100.0
                    "Millimeter" -> value / 1000.0
                    "Mile" -> value * 1609.344
                    "Yard" -> value * 0.9144
                    "Foot" -> value * 0.3048
                    "Inch" -> value * 0.0254
                    else -> value
                }
                when (to) {
                    "Meter" -> inMeters
                    "Kilometer" -> inMeters / 1000.0
                    "Centimeter" -> inMeters * 100.0
                    "Millimeter" -> inMeters * 1000.0
                    "Mile" -> inMeters / 1609.344
                    "Yard" -> inMeters / 0.9144
                    "Foot" -> inMeters / 0.3048
                    "Inch" -> inMeters / 0.0254
                    else -> inMeters
                }
            }
            UnitCategory.WEIGHT -> {
                // Base unit: Gram
                val inGrams = when (from) {
                    "Gram" -> value
                    "Kilogram" -> value * 1000.0
                    "Milligram" -> value / 1000.0
                    "Pound" -> value * 453.59237
                    "Ounce" -> value * 28.34952
                    else -> value
                }
                when (to) {
                    "Gram" -> inGrams
                    "Kilogram" -> inGrams / 1000.0
                    "Milligram" -> inGrams * 1000.0
                    "Pound" -> inGrams / 453.59237
                    "Ounce" -> inGrams / 28.34952
                    else -> inGrams
                }
            }
            UnitCategory.AREA -> {
                // Base unit: Square Meter
                val inSqM = when (from) {
                    "Square Meter" -> value
                    "Square Kilometer" -> value * 1_000_000.0
                    "Acre" -> value * 4046.856
                    "Hectare" -> value * 10000.0
                    "Square Feet" -> value * 0.092903
                    else -> value
                }
                when (to) {
                    "Square Meter" -> inSqM
                    "Square Kilometer" -> inSqM / 1_000_000.0
                    "Acre" -> inSqM / 4046.856
                    "Hectare" -> inSqM / 10000.0
                    "Square Feet" -> inSqM / 0.092903
                    else -> inSqM
                }
            }
            UnitCategory.VOLUME -> {
                // Base unit: Liter
                val inLiters = when (from) {
                    "Liter" -> value
                    "Milliliter" -> value / 1000.0
                    "Gallon" -> value * 3.78541
                    "Quart" -> value * 0.946353
                    "Cup" -> value * 0.236588
                    else -> value
                }
                when (to) {
                    "Liter" -> inLiters
                    "Milliliter" -> inLiters * 1000.0
                    "Gallon" -> inLiters / 3.78541
                    "Quart" -> inLiters / 0.946353
                    "Cup" -> inLiters / 0.236588
                    else -> inLiters
                }
            }
            UnitCategory.TEMPERATURE -> {
                when (from) {
                    "Celsius" -> {
                        when (to) {
                            "Fahrenheit" -> (value * 9/5) + 32
                            "Kelvin" -> value + 273.15
                            else -> value
                        }
                    }
                    "Fahrenheit" -> {
                        when (to) {
                            "Celsius" -> (value - 32) * 5/9
                            "Kelvin" -> ((value - 32) * 5/9) + 273.15
                            else -> value
                        }
                    }
                    "Kelvin" -> {
                        when (to) {
                            "Celsius" -> value - 273.15
                            "Fahrenheit" -> ((value - 273.15) * 9/5) + 32
                            else -> value
                        }
                    }
                    else -> value
                }
            }
        }
    }

    // --- Special Tools Calculations ---
    // 1. BMI Calculation
    fun toggleBmiWeightUnit() {
        if (bmiWeightUnit == "kg") {
            bmiWeightUnit = "lb"
            val wKg = bmiWeight.toDoubleOrNull() ?: 0.0
            bmiWeight = String.format(java.util.Locale.US, "%.1f", wKg / 0.453592).removeSuffix(".0")
        } else {
            bmiWeightUnit = "kg"
            val wLb = bmiWeight.toDoubleOrNull() ?: 0.0
            bmiWeight = String.format(java.util.Locale.US, "%.1f", wLb * 0.453592).removeSuffix(".0")
        }
        calculateBMI()
    }

    fun calculateBMI() {
        val wKg = if (bmiWeightUnit == "kg") {
            bmiWeight.toDoubleOrNull() ?: 0.0
        } else {
            (bmiWeight.toDoubleOrNull() ?: 0.0) * 0.453592
        }

        val hM = if (bmiHeightUnit == "cm") {
            (bmiHeight.toDoubleOrNull() ?: 0.0) / 100.0
        } else {
            val ft = bmiHeightFt.toDoubleOrNull() ?: 0.0
            val inch = bmiHeightIn.toDoubleOrNull() ?: 0.0
            val totalInches = (ft * 12.0) + inch
            totalInches * 0.0254
        }

        if (wKg <= 0.0 || hM <= 0.0) {
            bmiResultValue = "0.0"
            bmiCategoryResult = "Normal"
            return
        }
        
        val bmi = wKg / (hM * hM)
        val df = DecimalFormat("0.0")
        bmiResultValue = df.format(bmi)
        
        bmiCategoryResult = when {
            bmi <= 15.9 -> "Very Severely Underweight"
            bmi in 16.0..16.9 -> "Severely Underweight"
            bmi in 17.0..18.4 -> "Underweight"
            bmi in 18.5..24.9 -> "Normal"
            bmi in 25.0..29.9 -> "Overweight"
            bmi in 30.0..34.9 -> "Obese Class I"
            bmi in 35.0..39.9 -> "Obese Class II"
            else -> "Obese Class III"
        }
    }

    // 2. Age Calculation
    fun calculateAge() {
        try {
            val parts = ageDob.split("/")
            if (parts.size == 3) {
                val day = parts[0].toIntOrNull() ?: 1
                val month = (parts[1].toIntOrNull() ?: 1) - 1 // 0 indexed
                val year = parts[2].toIntOrNull() ?: 2000

                val birthCalendar = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                val targetCalendar = Calendar.getInstance()

                var years = targetCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                var months = targetCalendar.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH)
                var days = targetCalendar.get(Calendar.DAY_OF_MONTH) - birthCalendar.get(Calendar.DAY_OF_MONTH)

                if (days < 0) {
                    months--
                    val prevMonthCalendar = targetCalendar.clone() as Calendar
                    prevMonthCalendar.add(Calendar.MONTH, -1)
                    days += prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                }

                if (months < 0) {
                    years--
                    months += 12
                }

                if (years < 0) {
                    ageYearsResult = "0"
                    ageMonthsResult = "0"
                    ageDaysResult = "0"
                } else {
                    ageYearsResult = years.toString()
                    ageMonthsResult = months.toString()
                    ageDaysResult = days.toString()
                }
            }
        } catch (e: Exception) {
            ageYearsResult = "0"
            ageMonthsResult = "0"
            ageDaysResult = "0"
        }
    }

    // 3. Discount Calculation
    fun calculateDiscount() {
        val price = originalPrice.toDoubleOrNull() ?: 0.0
        val disc = discountPercent.toDoubleOrNull() ?: 0.0
        val tax = taxPercent.toDoubleOrNull() ?: 0.0

        val savings = price * (disc / 100.0)
        val priceAfterDiscount = price - savings
        val taxAmount = priceAfterDiscount * (tax / 100.0)
        val finalPrice = priceAfterDiscount + taxAmount

        val df = DecimalFormat("#.##")
        finalPriceResult = df.format(finalPrice)
        discountSavingsResult = df.format(savings)
    }

    // 4. Percentage Calculation
    fun calculatePercentage() {
        val valA = percentValueA.toDoubleOrNull() ?: 0.0
        val valB = percentValueB.toDoubleOrNull() ?: 0.0

        if (valB == 0.0) {
            percentageResultText = "Division by zero"
            return
        }

        val percentage = (valA / valB) * 100.0
        val df = DecimalFormat("#.##")
        percentageResultText = "$valA is ${df.format(percentage)}% of $valB\n" +
                "($valA হলো $valB এর ${df.format(percentage)}%)"
    }
}

class CalculatorViewModelFactory(
    private val repository: HistoryRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

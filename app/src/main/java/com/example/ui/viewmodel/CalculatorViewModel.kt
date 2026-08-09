package com.example.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
    var expressionValue by mutableStateOf(TextFieldValue("0", selection = TextRange(1)))
        private set

    val expression: String get() = expressionValue.text

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

    // History selection state
    var selectedHistoryIds by mutableStateOf(setOf<Long>())
    var isHistorySelectionMode by mutableStateOf(false)

    fun toggleHistorySelection(id: Long) {
        selectedHistoryIds = if (selectedHistoryIds.contains(id)) {
            selectedHistoryIds - id
        } else {
            selectedHistoryIds + id
        }
        if (selectedHistoryIds.isEmpty()) {
            isHistorySelectionMode = false
        }
    }

    fun deleteSelectedHistory() {
        viewModelScope.launch {
            selectedHistoryIds.forEach { id ->
                repository.deleteHistoryById(id)
            }
            selectedHistoryIds = emptySet()
            isHistorySelectionMode = false
        }
    }

    // Theme Selection
    private val sharedPrefs = context.getSharedPreferences("smart_calc_prefs", Context.MODE_PRIVATE)
    var currentThemeType by mutableStateOf(
        try {
            CalculatorThemeType.valueOf(
                sharedPrefs.getString("selected_theme", CalculatorThemeType.INDIGO_ESSENCE.name)
                    ?: CalculatorThemeType.INDIGO_ESSENCE.name
            )
        } catch (e: Exception) {
            CalculatorThemeType.INDIGO_ESSENCE
        }
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
    var bmiIdealWeightRange by mutableStateOf("")

    // 2. Age
    var ageDob by mutableStateOf("1/1/2000")
    var ageYearsResult by mutableStateOf("0")
    var ageMonthsResult by mutableStateOf("0")
    var ageDaysResult by mutableStateOf("0")
    var ageNextBirthdayResult by mutableStateOf("-")
    var ageWhichBirthdayResult by mutableStateOf("-")
    var ageCountdownResult by mutableStateOf("-")
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
        // ... (rest of processing)
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
            expressionValue = TextFieldValue(processedText, selection = TextRange(processedText.length))
            evaluateExpression()
        }
    }

    fun onExpressionValueChange(newValue: TextFieldValue) {
        expressionValue = newValue
        tryEvaluatePreview()
    }

    fun onPaste(pastedText: String) {
        val currentText = expressionValue.text
        val selection = expressionValue.selection
        val newText = if (currentText == "0") {
            pastedText
        } else {
            currentText.substring(0, selection.start) + pastedText + currentText.substring(selection.end)
        }
        expressionValue = TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + pastedText.length)
        )
        tryEvaluatePreview()
    }

    fun onBtnClick(char: String) {
        if (isEvaluated) {
            if (char != "AC" && char != "C" && char != "=" && char != "DEG" && char != "RAD" && char != "±") {
                if (isOperator(char)) {
                    // Continue from previous result
                    expressionValue = TextFieldValue(result, selection = TextRange(result.length))
                } else {
                    // Start new expression
                    expressionValue = TextFieldValue("")
                    result = ""
                }
            }
            isEvaluated = false
        }

        val currentText = expressionValue.text
        val selection = expressionValue.selection
        val before = currentText.substring(0, selection.start)
        val after = currentText.substring(selection.end)

        when (char) {
            "AC" -> {
                expressionValue = TextFieldValue("0", selection = TextRange(1))
                result = ""
            }
            "C" -> {
                if (selection.collapsed) {
                    if (before.isNotEmpty()) {
                        val newText = (before.dropLast(1) + after).ifEmpty { "0" }
                        val newPos = if (newText == "0" && before.length == 1) 1 else (before.length - 1).coerceAtLeast(0)
                        expressionValue = TextFieldValue(newText, selection = TextRange(newPos))
                    }
                } else {
                    val newText = (before + after).ifEmpty { "0" }
                    val newPos = if (newText == "0") 1 else before.length
                    expressionValue = TextFieldValue(newText, selection = TextRange(newPos))
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
            "sin", "cos", "tan", "ln", "log", "√", "antilog", "sin⁻¹", "cos⁻¹", "tan⁻¹", "log10", "3√" -> {
                val toAdd = if (char == "log10") "log10(" else if (char == "3√") "3√(" else "$char("
                val newText = before + toAdd + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + toAdd.length))
                tryEvaluatePreview()
            }
            "x^y" -> {
                val newText = before + "^" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 1))
                tryEvaluatePreview()
            }
            "x²" -> {
                val newText = before + "^2" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 2))
                tryEvaluatePreview()
            }
            "x³" -> {
                val newText = before + "^3" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 2))
                tryEvaluatePreview()
            }
            "1/x" -> {
                val newText = before + "1÷" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 2))
                tryEvaluatePreview()
            }
            "e^x" -> {
                val newText = before + "e^" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 2))
                tryEvaluatePreview()
            }
            "x!" -> {
                val newText = before + "!" + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 1))
                tryEvaluatePreview()
            }
            "π", "e" -> {
                val newText = before + char + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + 1))
                tryEvaluatePreview()
            }
            "()" -> {
                val toAdd: String
                if (currentText.isEmpty()) {
                    toAdd = "("
                } else {
                    val lastChar = if (before.isNotEmpty()) before.last() else ' '
                    val openCount = currentText.count { it == '(' }
                    val closeCount = currentText.count { it == ')' }
                    
                    if (openCount > closeCount) {
                        if (lastChar in setOf('+', '−', '×', '÷', '(', '^', '-')) {
                            toAdd = "("
                        } else {
                            toAdd = ")"
                        }
                    } else {
                        if (lastChar.isDigit() || lastChar == ')' || lastChar == 'e' || lastChar == 'π') {
                            toAdd = "×("
                        } else {
                            toAdd = "("
                        }
                    }
                }
                val newText = before + toAdd + after
                expressionValue = TextFieldValue(newText, selection = TextRange(before.length + toAdd.length))
                tryEvaluatePreview()
            }
            "±" -> {
                if (currentText.startsWith("-")) {
                    expressionValue = TextFieldValue(currentText.substring(1), selection = TextRange((selection.start - 1).coerceAtLeast(0)))
                } else if (currentText.isNotEmpty()) {
                    expressionValue = TextFieldValue("-$currentText", selection = TextRange(selection.start + 1))
                }
                tryEvaluatePreview()
            }
            "." -> {
                val lastNumber = before.split(Regex("[+−×÷%^]")).lastOrNull() ?: ""
                if (!lastNumber.contains(".")) {
                    val toAdd = if (before.isEmpty() || isOperator(before.last().toString())) "0." else "."
                    val newText = before + toAdd + after
                    expressionValue = TextFieldValue(newText, selection = TextRange(before.length + toAdd.length))
                }
                tryEvaluatePreview()
            }
            else -> {
                val processedBefore = if (before == "0" && !isOperator(char) && char != ".") "" else before
                if (isOperator(char)) {
                    if (processedBefore.isNotEmpty() && processedBefore != "0") {
                        val lastChar = processedBefore.last().toString()
                        if (isOperator(lastChar)) {
                            val newText = processedBefore.dropLast(1) + char + after
                            expressionValue = TextFieldValue(newText, selection = TextRange(processedBefore.length))
                        } else {
                            val newText = processedBefore + char + after
                            expressionValue = TextFieldValue(newText, selection = TextRange(processedBefore.length + 1))
                        }
                    } else if (char == "−" || char == "-") {
                        val newText = "-" + after
                        expressionValue = TextFieldValue(newText, selection = TextRange(1))
                    }
                } else {
                    val newText = processedBefore + char + after
                    val newPos = if (before == "0" && processedBefore == "") char.length else (before.length + char.length)
                    expressionValue = TextFieldValue(newText, selection = TextRange(newPos))
                }
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
        expressionValue = TextFieldValue(entry.expression, selection = TextRange(entry.expression.length))
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

    fun calculateConverterReverse() {
        val outputVal = converterOutput.toDoubleOrNull() ?: 0.0
        val inVal = convertUnits(unitCategory, toUnit, fromUnit, outputVal)
        val df = DecimalFormat("#.######")
        converterInput = df.format(inVal)
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

        val minIdealKg = 18.5 * (hM * hM)
        val maxIdealKg = 24.9 * (hM * hM)
        bmiIdealWeightRange = if (bmiWeightUnit == "kg") {
            String.format(Locale.US, "%.1f kg - %.1f kg", minIdealKg, maxIdealKg)
        } else {
            String.format(Locale.US, "%.1f lb - %.1f lb", minIdealKg / 0.453592, maxIdealKg / 0.453592)
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
                    ageNextBirthdayResult = "-"
                    ageWhichBirthdayResult = "-"
                    ageCountdownResult = "-"
                } else {
                    ageYearsResult = years.toString()
                    ageMonthsResult = months.toString()
                    ageDaysResult = days.toString()

                    val nextBday = birthCalendar.clone() as Calendar
                    nextBday.set(Calendar.YEAR, targetCalendar.get(Calendar.YEAR))
                    if (nextBday.before(targetCalendar) || nextBday.timeInMillis == targetCalendar.timeInMillis) {
                        nextBday.add(Calendar.YEAR, 1)
                    }

                    val nextAgeYears = nextBday.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                    ageWhichBirthdayResult = "$nextAgeYears${getOrdinalSuffix(nextAgeYears)} Birthday"

                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    ageNextBirthdayResult = dateFormat.format(nextBday.time)

                    val diffInMillis = nextBday.timeInMillis - targetCalendar.timeInMillis
                    val diffDays = (diffInMillis / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                    ageCountdownResult = "$diffDays Days"
                }
            }
        } catch (e: Exception) {
            ageYearsResult = "0"
            ageMonthsResult = "0"
            ageDaysResult = "0"
            ageNextBirthdayResult = "-"
            ageWhichBirthdayResult = "-"
            ageCountdownResult = "-"
        }
    }

    private fun getOrdinalSuffix(n: Int): String {
        return when {
            n % 100 in 11..13 -> "th"
            n % 10 == 1 -> "st"
            n % 10 == 2 -> "nd"
            n % 10 == 3 -> "rd"
            else -> "th"
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
        percentageResultText = "$valA is ${df.format(percentage)}% of $valB"
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

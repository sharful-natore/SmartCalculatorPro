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
import com.example.util.AppLanguage
import com.example.util.ExpressionEvaluator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
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

    // Theme & Language Selection
    private val sharedPrefs = context.getSharedPreferences("smart_calc_prefs", Context.MODE_PRIVATE)
    var currentThemeType by mutableStateOf(
        try {
            CalculatorThemeType.valueOf(
                sharedPrefs.getString("selected_theme", CalculatorThemeType.SKY_BREEZE.name)
                    ?: CalculatorThemeType.SKY_BREEZE.name
            )
        } catch (e: Exception) {
            CalculatorThemeType.SKY_BREEZE
        }
    )
        private set

    var selectedLanguage by mutableStateOf(
        try {
            AppLanguage.valueOf(
                sharedPrefs.getString("selected_language", AppLanguage.ENGLISH.name)
                    ?: AppLanguage.ENGLISH.name
            )
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    )
        private set

    fun setLanguage(language: AppLanguage) {
        selectedLanguage = language
        sharedPrefs.edit().putString("selected_language", language.name).apply()
    }

    // --- Currency Exchange Rates Engine ---
    private val defaultExchangeRates = mapOf(
        "USD" to 1.0,
        "BDT" to 121.5,
        "EUR" to 0.92,
        "GBP" to 0.78,
        "INR" to 83.8,
        "SAR" to 3.75,
        "AED" to 3.67,
        "MYR" to 4.42,
        "SGD" to 1.34,
        "CAD" to 1.37,
        "AUD" to 1.52,
        "JPY" to 147.5,
        "CNY" to 7.17,
        "PKR" to 278.5,
        "LKR" to 302.0,
        "TRY" to 33.5,
        "RUB" to 88.0,
        "KWD" to 0.31,
        "BHD" to 0.38,
        "OMR" to 0.38,
        "QAR" to 3.64,
        "THB" to 35.2,
        "IDR" to 15800.0,
        "KRW" to 1365.0,
        "BRL" to 5.50,
        "MXN" to 18.8,
        "EGP" to 48.5,
        "NGN" to 1600.0,
        "CHF" to 0.86,
        "NZD" to 1.66,
        "ZAR" to 18.2
    )

    var exchangeRates by mutableStateOf<Map<String, Double>>(loadCachedExchangeRates())
    var isFetchingExchangeRates by mutableStateOf(false)
    var lastCurrencyUpdateTimestamp by mutableStateOf(
        sharedPrefs.getString("last_currency_update_time", "Default Rates") ?: "Default Rates"
    )
    var currencyUpdateStatusMessage by mutableStateOf("")

    private fun loadCachedExchangeRates(): Map<String, Double> {
        val cachedJson = sharedPrefs.getString("cached_exchange_rates", null) ?: return defaultExchangeRates
        return try {
            val jsonObject = JSONObject(cachedJson)
            val map = mutableMapOf<String, Double>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getDouble(key)
            }
            if (map.isNotEmpty()) map else defaultExchangeRates
        } catch (e: Exception) {
            defaultExchangeRates
        }
    }

    private fun saveCachedExchangeRates(rates: Map<String, Double>, timestamp: String) {
        try {
            val jsonObject = JSONObject()
            rates.forEach { (key, value) -> jsonObject.put(key, value) }
            sharedPrefs.edit()
                .putString("cached_exchange_rates", jsonObject.toString())
                .putString("last_currency_update_time", timestamp)
                .apply()
        } catch (e: Exception) {
            // SILENT
        }
    }

    fun fetchExchangeRates() {
        if (isFetchingExchangeRates) return
        viewModelScope.launch(Dispatchers.IO) {
            isFetchingExchangeRates = true
            try {
                val url = URL("https://open.er-api.com/v6/latest/USD")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 6000
                connection.readTimeout = 6000

                if (connection.responseCode == 200) {
                    val stream = connection.inputStream
                    val responseText = stream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(responseText)

                    if (jsonObject.has("rates")) {
                        val ratesObj = jsonObject.getJSONObject("rates")
                        val newRates = defaultExchangeRates.toMutableMap()
                        val keys = ratesObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            newRates[key] = ratesObj.getDouble(key)
                        }

                        val timeStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

                        withContext(Dispatchers.Main) {
                            exchangeRates = newRates
                            lastCurrencyUpdateTimestamp = timeStr
                            currencyUpdateStatusMessage = "Live rates updated successfully"
                            saveCachedExchangeRates(newRates, timeStr)
                            calculateConverter()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        currencyUpdateStatusMessage = "Offline mode: Using cached rates"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    currencyUpdateStatusMessage = "Offline mode: Using cached rates"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isFetchingExchangeRates = false
                }
            }
        }
    }

    // History list from Room Flow
    val historyList: StateFlow<List<HistoryEntry>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Unit Converter States ---
    var selectedConverterType by mutableStateOf<com.example.data.model.ConverterType?>(null)
    var selectedCategoryFilter by mutableStateOf<com.example.data.model.ConverterCategory?>(null)
    var converterSearchQuery by mutableStateOf("")
    var fromUnit by mutableStateOf("Meter")
    var toUnit by mutableStateOf("Feet")
    var converterInput by mutableStateOf("1")
    var converterOutput by mutableStateOf("3.2808")

    // --- Special Tools States ---
    var selectedToolType by mutableStateOf<com.example.data.model.ToolType?>(null)
    var selectedToolCategoryFilter by mutableStateOf<com.example.data.model.ToolCategory?>(null)
    var toolSearchQuery by mutableStateOf("")

    fun openTool(type: com.example.data.model.ToolType) {
        selectedToolType = type
    }

    fun closeToolDetail() {
        selectedToolType = null
    }

    // Exit confirmation dialog state
    var showExitDialog by mutableStateOf(false)

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
        // Auto-fetch exchange rates when connected
        fetchExchangeRates()

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
    fun openConverter(type: com.example.data.model.ConverterType) {
        selectedConverterType = type
        fromUnit = type.units.firstOrNull() ?: ""
        toUnit = type.units.getOrNull(1) ?: fromUnit
        converterInput = "1"
        calculateConverter()
    }

    fun closeConverterDetail() {
        selectedConverterType = null
    }

    fun swapUnits() {
        val temp = fromUnit
        fromUnit = toUnit
        toUnit = temp
        calculateConverter()
    }

    fun calculateConverter() {
        val type = selectedConverterType ?: return
        val inputVal = converterInput.toDoubleOrNull() ?: 0.0
        val outVal = if (type == com.example.data.model.ConverterType.CURRENCY) {
            type.convert(fromUnit, toUnit, inputVal, customRates = exchangeRates)
        } else {
            type.convert(fromUnit, toUnit, inputVal)
        }
        val df = DecimalFormat("#.######")
        converterOutput = df.format(outVal)
    }

    fun calculateConverterReverse() {
        val type = selectedConverterType ?: return
        val outputVal = converterOutput.toDoubleOrNull() ?: 0.0
        val inVal = if (type == com.example.data.model.ConverterType.CURRENCY) {
            type.convert(toUnit, fromUnit, outputVal, customRates = exchangeRates)
        } else {
            type.convert(toUnit, fromUnit, outputVal)
        }
        val df = DecimalFormat("#.######")
        converterInput = df.format(inVal)
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

    // --- Persistent Customizable Tool Rates / Fees ---
    var elecUnitRate by mutableStateOf(sharedPrefs.getString("custom_elec_rate", "6.50") ?: "6.50")
        private set
    var elecDemandCharge by mutableStateOf(sharedPrefs.getString("custom_elec_demand", "40.0") ?: "40.0")
        private set
    var elecMeterRent by mutableStateOf(sharedPrefs.getString("custom_elec_meter", "40.0") ?: "40.0")
        private set
    var elecVatPercent by mutableStateOf(sharedPrefs.getString("custom_elec_vat", "5.0") ?: "5.0")
        private set

    var fuelPricePerUnit by mutableStateOf(sharedPrefs.getString("custom_fuel_price", "125.0") ?: "125.0")
        private set

    var applianceKwhRate by mutableStateOf(sharedPrefs.getString("custom_appliance_kwh_rate", "7.50") ?: "7.50")
        private set

    fun updateElectricityRates(rate: String, demand: String, meter: String, vat: String) {
        elecUnitRate = rate
        elecDemandCharge = demand
        elecMeterRent = meter
        elecVatPercent = vat
        sharedPrefs.edit()
            .putString("custom_elec_rate", rate)
            .putString("custom_elec_demand", demand)
            .putString("custom_elec_meter", meter)
            .putString("custom_elec_vat", vat)
            .apply()
    }

    fun updateFuelPrice(price: String) {
        fuelPricePerUnit = price
        sharedPrefs.edit().putString("custom_fuel_price", price).apply()
    }

    fun updateApplianceKwhRate(rate: String) {
        applianceKwhRate = rate
        sharedPrefs.edit().putString("custom_appliance_kwh_rate", rate).apply()
    }

    // --- Smart Voice Command Processor ---
    var isVoiceListening by mutableStateOf(false)

    fun processVoiceCommand(rawInput: String) {
        if (rawInput.isBlank()) return

        // 1. Convert non-English digits to English digits
        val normalizedDigits = rawInput
            .replace('০', '0').replace('১', '1').replace('২', '2').replace('৩', '3')
            .replace('৪', '4').replace('৫', '5').replace('৬', '6').replace('৭', '7')
            .replace('৮', '8').replace('৯', '9')
            .replace('०', '0').replace('१', '1').replace('२', '2').replace('३', '3')
            .replace('४', '4').replace('५', '5').replace('६', '6').replace('७', '7')
            .replace('८', '8').replace('९', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3')
            .replace('٤', '4').replace('٥', '5').replace('٦', '6').replace('٧', '7')
            .replace('٨', '8').replace('٩', '9')

        val textLower = normalizedDigits.lowercase()

        // 2. Check for Currency queries (e.g. "1 dollar in taka", "১ ডলারে কত টাকা", "100 usd to bdt")
        if (textLower.contains("dollar") || textLower.contains("ডলার") || textLower.contains("taka") ||
            textLower.contains("টাকা") || textLower.contains("usd") || textLower.contains("bdt") ||
            textLower.contains("rupee") || textLower.contains("রুপি") || textLower.contains("inr") ||
            textLower.contains("euro") || textLower.contains("ইউরো") || textLower.contains("eur")
        ) {
            val numValue = extractNumberFromString(textLower) ?: 1.0
            activeTab = 1
            selectedConverterType = com.example.data.model.ConverterType.CURRENCY

            if (textLower.contains("dollar") || textLower.contains("ডলার") || textLower.contains("usd")) {
                fromUnit = "USD - US Dollar"
                toUnit = if (textLower.contains("rupee") || textLower.contains("রুপি") || textLower.contains("inr")) {
                    "INR - Indian Rupee"
                } else {
                    "BDT - Bangladeshi Taka"
                }
            } else if (textLower.contains("euro") || textLower.contains("ইউরো") || textLower.contains("eur")) {
                fromUnit = "EUR - Euro"
                toUnit = "BDT - Bangladeshi Taka"
            } else {
                fromUnit = "BDT - Bangladeshi Taka"
                toUnit = "USD - US Dollar"
            }

            converterInput = if (numValue == numValue.toLong().toDouble()) numValue.toLong().toString() else numValue.toString()
            calculateConverter()
            return
        }

        // 3. Check for Unit Converter queries (e.g. "1 kg to gram", "১ কেজিতে কত গ্রাম", "1 meter to cm")
        if (textLower.contains("kg") || textLower.contains("কেজি") || textLower.contains("gram") ||
            textLower.contains("গ্রাম") || textLower.contains("pound") || textLower.contains("পাউন্ড")
        ) {
            val numValue = extractNumberFromString(textLower) ?: 1.0
            activeTab = 1
            selectedConverterType = com.example.data.model.ConverterType.WEIGHT
            fromUnit = "Kilogram (kg)"
            toUnit = "Gram (g)"
            converterInput = if (numValue == numValue.toLong().toDouble()) numValue.toLong().toString() else numValue.toString()
            calculateConverter()
            return
        }

        if (textLower.contains("meter") || textLower.contains("মিটার") || textLower.contains("feet") ||
            textLower.contains("ফুট") || textLower.contains("inch") || textLower.contains("ইঞ্চি") ||
            textLower.contains("bigha") || textLower.contains("বিঘা")
        ) {
            val numValue = extractNumberFromString(textLower) ?: 1.0
            activeTab = 1
            selectedConverterType = if (textLower.contains("bigha") || textLower.contains("বিঘা")) {
                com.example.data.model.ConverterType.AREA
            } else {
                com.example.data.model.ConverterType.LENGTH
            }
            if (selectedConverterType == com.example.data.model.ConverterType.LENGTH) {
                fromUnit = "Meter (m)"
                toUnit = if (textLower.contains("feet") || textLower.contains("ফুট")) "Foot (ft)" else "Centimeter (cm)"
            }
            converterInput = if (numValue == numValue.toLong().toDouble()) numValue.toLong().toString() else numValue.toString()
            calculateConverter()
            return
        }

        // 4. Check for Special Tools queries (e.g., "bmi", "বিএমআই", "বিদ্যুৎ বিল", "age")
        if (textLower.contains("bmi") || textLower.contains("বিএমআই")) {
            activeTab = 2
            openTool(com.example.data.model.ToolType.BMI)
            return
        } else if (textLower.contains("age") || textLower.contains("বয়স")) {
            activeTab = 2
            openTool(com.example.data.model.ToolType.AGE)
            return
        } else if (textLower.contains("electricity") || textLower.contains("বিদ্যুৎ")) {
            activeTab = 2
            openTool(com.example.data.model.ToolType.ELECTRICITY_BILL)
            return
        } else if (textLower.contains("loan") || textLower.contains("emi") || textLower.contains("ইএমআই")) {
            activeTab = 2
            openTool(com.example.data.model.ToolType.EMI_LOAN)
            return
        }

        // 5. Fallback: Parse Math Equation (support continuous multi-operation calculations)
        var mathExpr = normalizedDigits
            .replace("যোগ", "+").replace("প্লাস", "+").replace("плюс", "+").replace("plus", "+").replace("زائد", "+")
            .replace("বিয়োগ", "-").replace("মাইনাস", "-").replace("minus", "-").replace("ناقص", "-")
            .replace("গুণ", "*").replace("ইনটু", "*").replace("times", "*").replace("into", "*").replace("ضرب", "*")
            .replace("ভাগ", "/").replace("ডিভাইডেড", "/").replace("ভাগফল", "/").replace("divided by", "/").replace("divided", "/").replace("قسمة", "/")
            .replace("x", "*").replace("X", "*").replace("÷", "/")

        // Sanitize math string keeping only numbers, operators, dots and parentheses
        mathExpr = mathExpr.filter { it.isDigit() || it in "+-*/.()" }

        if (mathExpr.isNotBlank()) {
            activeTab = 0
            expressionValue = TextFieldValue(mathExpr, selection = TextRange(mathExpr.length))
            evaluateExpression()
        }
    }

    private fun extractNumberFromString(str: String): Double? {
        val regex = Regex("""\d+(\.\d+)?""")
        val match = regex.find(str)
        return match?.value?.toDoubleOrNull()
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

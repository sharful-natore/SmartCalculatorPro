package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinanceTransaction
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.util.bounceOverscroll
import com.example.util.horizontalBounceOverscroll
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

// Contact Person Model for Debt and Loan tracking
data class ContactPerson(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val address: String,
    val colorIndex: Int = 0
) {
    fun toSerializedString(): String {
        val escapedName = name.replace("|~|", " ").replace(";;;", " ")
        val escapedPhone = phone.replace("|~|", " ").replace(";;;", " ")
        val escapedAddress = address.replace("|~|", " ").replace(";;;", " ")
        return "$id|~|$escapedName|~|$escapedPhone|~|$escapedAddress|~|$colorIndex"
    }

    companion object {
        fun fromSerializedString(str: String): ContactPerson? {
            val parts = str.split("|~|")
            if (parts.size >= 5) {
                return ContactPerson(
                    id = parts[0],
                    name = parts[1],
                    phone = parts[2],
                    address = parts[3],
                    colorIndex = parts[4].toIntOrNull() ?: 0
                )
            }
            return null
        }
    }
}

// Helper methods for local persistence of contacts and savings target
fun saveContacts(context: Context, contacts: List<ContactPerson>) {
    val prefs = context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)
    val serialized = contacts.joinToString(";;;") { it.toSerializedString() }
    prefs.edit().putString("contact_persons", serialized).apply()
}

fun getContacts(context: Context): List<ContactPerson> {
    val prefs = context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)
    val serialized = prefs.getString("contact_persons", "") ?: ""
    if (serialized.isEmpty()) return emptyList()
    return serialized.split(";;;").mapNotNull { ContactPerson.fromSerializedString(it) }
}

fun saveSavingsTarget(context: Context, target: Double) {
    val prefs = context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)
    prefs.edit().putFloat("savings_target", target.toFloat()).apply()
}

fun getSavingsTarget(context: Context): Double {
    val prefs = context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)
    return prefs.getFloat("savings_target", 50000f).toDouble()
}

val avatarGradients = listOf(
    Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))), // Blue
    Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D))), // Pink
    Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857))), // Green
    Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309))), // Amber
    Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))), // Purple
    Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0891B2))), // Cyan
    Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))  // Red
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FinanceScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current
    val transactions by viewModel.financeTransactions.collectAsState()

    // Preferences & Persistence
    var contactsList by remember { mutableStateOf(getContacts(context)) }
    var savingsTarget by remember { mutableStateOf(getSavingsTarget(context)) }

    // State Variables
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE, DEBT, SAVINGS
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }

    // Sorting state variables
    var sortBy by remember { mutableStateOf("TIME") } // "TIME", "NAME", "AMOUNT"
    var sortOrder by remember { mutableStateOf("DESC") } // "ASC", "DESC"
    var showSortDialog by remember { mutableStateOf(false) }

    // Date Filtering sorting state
    var dateFilterType by remember { mutableStateOf("ALL") } // ALL, MONTH_FILTER, DATE_RANGE
    val currentCal = remember { Calendar.getInstance() }
    val curMonth = currentCal.get(Calendar.MONTH)
    val curYear = currentCal.get(Calendar.YEAR)
    var specificMonth by remember { mutableStateOf(curMonth) }
    var specificYear by remember { mutableStateOf(curYear) }
    var rangeStartDate by remember { mutableStateOf<Long?>(null) }
    var rangeEndDate by remember { mutableStateOf<Long?>(null) }
    var showDateRangePickerDialog by remember { mutableStateOf(false) }

    // Dialog flags
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<FinanceTransaction?>(null) }
    var deleteContactTarget by remember { mutableStateOf<ContactPerson?>(null) }
    var showMultiDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var selectedTransactionDetails by remember { mutableStateOf<FinanceTransaction?>(null) }
    var showTargetEditDialog by remember { mutableStateOf(false) }
    var showContactsManagerDialog by remember { mutableStateOf(false) }
    var showMonthYearPickerDialog by remember { mutableStateOf(false) }

    // Summary calculations
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val totalSavings = remember(transactions) {
        transactions.filter { it.type == "SAVINGS" }.sumOf {
            if (it.subType == "WITHDRAWAL") -it.amount else it.amount
        }
    }
    val debtTaken = remember(transactions) { // দেনা (নিয়েছি)
        transactions.filter { it.type == "DEBT" && it.subType == "TAKEN" && !it.isSettled }.sumOf { it.amount }
    }
    val loanGiven = remember(transactions) { // পাওনা (দিয়েছি)
        transactions.filter { it.type == "DEBT" && it.subType == "GIVEN" && !it.isSettled }.sumOf { it.amount }
    }
    val netBalance = remember(totalIncome, totalExpense, totalSavings, debtTaken, loanGiven) {
        (totalIncome + debtTaken) - (totalExpense + loanGiven)
    }

    val currencySymbol = "৳"
    val df = remember { DecimalFormat("#,##0.00") }

    fun formatAmount(amount: Double): String {
        val formatted = df.format(amount)
        if (!isBn) return "$currencySymbol $formatted"
        val benNumbers = mapOf(
            '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
            '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
        )
        val bnStr = formatted.map { benNumbers[it] ?: it }.joinToString("")
        return "$currencySymbol $bnStr"
    }

    val monthsBn = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val monthsEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun getMonthLabel(m: Int, y: Int): String {
        val mName = if (isBn) monthsBn[m] else monthsEn[m]
        val yStr = if (isBn) {
            val benNumbers = mapOf(
                '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
                '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
            )
            y.toString().map { benNumbers[it] ?: it }.joinToString("")
        } else y.toString()
        return "$mName $yStr"
    }

    val filteredList = remember(transactions, searchQuery, selectedFilterTab, dateFilterType, specificMonth, specificYear, rangeStartDate, rangeEndDate, sortBy, sortOrder) {
        val list = transactions.filter { item ->
            val matchesFilter = when (selectedFilterTab) {
                "INCOME" -> item.type == "INCOME"
                "EXPENSE" -> item.type == "EXPENSE"
                "DEBT_TAKEN" -> item.type == "DEBT" && item.subType == "TAKEN"
                "DEBT_GIVEN" -> item.type == "DEBT" && item.subType == "GIVEN"
                "SAVINGS" -> item.type == "SAVINGS"
                else -> true
            }
            val matchesDate = when (dateFilterType) {
                "MONTH_FILTER" -> {
                    val itemCal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
                    itemCal.get(Calendar.MONTH) == specificMonth && itemCal.get(Calendar.YEAR) == specificYear
                }
                "DATE_RANGE" -> {
                    val start = rangeStartDate
                    val end = rangeEndDate
                    if (start != null && end != null) {
                        val startCal = Calendar.getInstance().apply {
                            timeInMillis = start
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val endCal = Calendar.getInstance().apply {
                            timeInMillis = end
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        item.timestamp in startCal.timeInMillis..endCal.timeInMillis
                    } else {
                        true
                    }
                }
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.title.contains(searchQuery, ignoreCase = true) ||
                        item.category.contains(searchQuery, ignoreCase = true) ||
                        item.note.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesDate && matchesSearch
        }

        when (sortBy) {
            "NAME" -> {
                if (sortOrder == "ASC") list.sortedBy { it.title.lowercase() }
                else list.sortedByDescending { it.title.lowercase() }
            }
            "AMOUNT" -> {
                if (sortOrder == "ASC") list.sortedBy { it.amount }
                else list.sortedByDescending { it.amount }
            }
            else -> { // TIME
                if (sortOrder == "ASC") list.sortedBy { it.timestamp }
                else list.sortedByDescending { it.timestamp }
            }
        }
    }

    // Dynamic counts for category filter badges matching current date filter and search query
    val listForBadges = remember(transactions, dateFilterType, specificMonth, specificYear, rangeStartDate, rangeEndDate, searchQuery) {
        transactions.filter { item ->
            val matchesDate = when (dateFilterType) {
                "MONTH_FILTER" -> {
                    val itemCal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
                    itemCal.get(Calendar.MONTH) == specificMonth && itemCal.get(Calendar.YEAR) == specificYear
                }
                "DATE_RANGE" -> {
                    val start = rangeStartDate
                    val end = rangeEndDate
                    if (start != null && end != null) {
                        val startCal = Calendar.getInstance().apply {
                            timeInMillis = start
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val endCal = Calendar.getInstance().apply {
                            timeInMillis = end
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        item.timestamp in startCal.timeInMillis..endCal.timeInMillis
                    } else {
                        true
                    }
                }
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.title.contains(searchQuery, ignoreCase = true) ||
                        item.category.contains(searchQuery, ignoreCase = true) ||
                        item.note.contains(searchQuery, ignoreCase = true)
            }
            matchesDate && matchesSearch
        }
    }

    val countAll = listForBadges.size
    val countIncome = listForBadges.count { it.type == "INCOME" }
    val countExpense = listForBadges.count { it.type == "EXPENSE" }
    val countDebtTaken = listForBadges.count { it.type == "DEBT" && it.subType == "TAKEN" }
    val countDebtGiven = listForBadges.count { it.type == "DEBT" && it.subType == "GIVEN" }

    // Soft Blurred Gradients
    val incExpBg = if (themeColors.isDark) {
        Brush.linearGradient(listOf(Color(0xFF1B5E20).copy(alpha = 0.15f), Color(0xFFB71C1C).copy(alpha = 0.15f)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFE8F5E9).copy(alpha = 0.90f), Color(0xFFFFEBEE).copy(alpha = 0.90f)))
    }

    val debtLoanBg = if (themeColors.isDark) {
        Brush.linearGradient(listOf(Color(0xFFE65100).copy(alpha = 0.15f), Color(0xFF4A148C).copy(alpha = 0.15f)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFF3E0).copy(alpha = 0.90f), Color(0xFFF3E5F5).copy(alpha = 0.90f)))
    }

    val savingsBg = if (themeColors.isDark) {
        Brush.linearGradient(listOf(Color(0xFF0D47A1).copy(alpha = 0.15f), Color(0xFF006064).copy(alpha = 0.15f)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFE3F2FD).copy(alpha = 0.90f), Color(0xFFE0F7FA).copy(alpha = 0.90f)))
    }

    // Dynamic primary theme color matching active app theme
    val primaryThemeColor = themeColors.buttonEqualBg

    // Theme harmonized text color for unselected chips and transaction titles
    val themeHarmonizedTextColor = remember(primaryThemeColor, themeColors.isDark) {
        if (themeColors.isDark) {
            Color(0xFFCBD5E1)
        } else {
            Color(
                red = (primaryThemeColor.red * 0.35f + 0.12f * 0.65f).coerceIn(0f, 1f),
                green = (primaryThemeColor.green * 0.35f + 0.18f * 0.65f).coerceIn(0f, 1f),
                blue = (primaryThemeColor.blue * 0.35f + 0.24f * 0.65f).coerceIn(0f, 1f),
                alpha = 1f
            )
        }
    }

    val screenBg = if (themeColors.isDark) themeColors.background else Color(0xFFF8FAFC)

    Scaffold(
        containerColor = screenBg,
        topBar = {
            if (selectedIds.isNotEmpty()) {
                Surface(
                    color = primaryThemeColor,
                    contentColor = Color.White,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedIds = emptySet() }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val countStr = if (isBn) {
                                val benNumbers = mapOf(
                                    '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
                                    '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
                                )
                                "${selectedIds.size}টি নির্বাচিত".map { benNumbers[it] ?: it }.joinToString("")
                            } else {
                                "${selectedIds.size} selected"
                            }
                            Text(
                                text = countStr,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Select All Button
                            val allIds = filteredList.map { it.id }.toSet()
                            val isAllSelected = selectedIds.containsAll(allIds) && allIds.isNotEmpty()
                            IconButton(
                                onClick = {
                                    selectedIds = if (isAllSelected) emptySet() else allIds
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SelectAll,
                                    contentDescription = "Select All",
                                    tint = Color.White
                                )
                            }
                            
                            // Delete Button
                            IconButton(
                                onClick = {
                                    if (selectedIds.isNotEmpty()) {
                                        showMultiDeleteConfirmDialog = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTransaction = null
                    showAddDialog = true
                },
                containerColor = primaryThemeColor,
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isBn) "লেনদেন যোগ করুন" else "Add Transaction",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { padding ->
        val groupedTransactions = remember(filteredList, isBn) {
            filteredList.groupBy { item ->
                val cal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val monthIndex = cal.get(Calendar.MONTH)
                val year = cal.get(Calendar.YEAR)
                
                if (isBn) {
                    val monthsBn = listOf(
                        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
                        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
                    )
                    fun toBnNum(num: Int): String {
                        val benNumbers = mapOf(
                            '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
                            '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
                        )
                        return num.toString().map { benNumbers[it] ?: it }.joinToString("")
                    }
                    "${toBnNum(day)} ${monthsBn[monthIndex]}, ${toBnNum(year)}"
                } else {
                    val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                    sdf.format(Date(item.timestamp))
                }
            }
        }

        val rangeLabel = remember(rangeStartDate, rangeEndDate, isBn) {
            val start = rangeStartDate
            val end = rangeEndDate
            if (start != null && end != null) {
                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                val startStr = sdf.format(Date(start))
                val endStr = sdf.format(Date(end))
                val res = "$startStr - $endStr"
                if (!isBn) res
                else {
                    val benNumbers = mapOf(
                        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
                        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
                    )
                    res.map { benNumbers[it] ?: it }.joinToString("")
                }
            } else {
                if (isBn) "তারিখ অনুযায়ী" else "By Date"
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp)
                .bounceOverscroll(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
        ) {
            // 1. Two Grid-like Summary Cards Side-by-Side (Vibrant Solid Theme Cards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left Card: Income & Expense (আয় ও ব্যয়)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(98.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(primaryThemeColor)
                                .padding(horizontal = 13.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Row 1: Income (আয়)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                        Text(
                                            text = if (isBn) "আয়ঃ" else "Income:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.85f),
                                            lineHeight = 13.sp
                                        )
                                        Text(
                                            text = formatAmount(totalIncome),
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // Row 2: Expense (ব্যয়)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                        Text(
                                            text = if (isBn) "ব্যয়ঃ" else "Expense:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.85f),
                                            lineHeight = 13.sp
                                        )
                                        Text(
                                            text = formatAmount(totalExpense),
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Right Card: Debt & Loan (দেনা ও পাওনা)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(98.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(primaryThemeColor)
                                .padding(horizontal = 13.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Row 1: Debt (দেনা)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                        Text(
                                            text = if (isBn) "দেনাঃ" else "Debt:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.85f),
                                            lineHeight = 13.sp
                                        )
                                        Text(
                                            text = formatAmount(debtTaken),
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // Row 2: Loan (পাওনা)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                        Text(
                                            text = if (isBn) "পাওনাঃ" else "Loan:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.85f),
                                            lineHeight = 13.sp
                                        )
                                        Text(
                                            text = formatAmount(loanGiven),
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Balance & Savings Card (ব্যালেন্স ও সঞ্চয়)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(primaryThemeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Half: Balance (ব্যালেন্স)
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                    Text(
                                        text = if (isBn) "ব্যালেন্সঃ" else "Balance:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 12.sp
                                    )
                                    Text(
                                        text = formatAmount(netBalance),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            // Vertical divider in the middle
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight(0.5f)
                                    .background(Color.White.copy(alpha = 0.3f))
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            // Right Half: Savings (সঞ্চয়) with Bank Icon
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                    Text(
                                        text = if (isBn) "সঞ্চয়ঃ" else "Savings:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 12.sp
                                    )
                                    Text(
                                        text = formatAmount(totalSavings),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Precise Rounded Search Bar & Profile Contacts Button Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text(
                                text = if (isBn) "সার্চ করুন" else "Search here...", 
                                fontSize = 14.sp, 
                                color = primaryThemeColor.copy(alpha = 0.6f)
                            ) 
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = themeColors.displayText),
                        singleLine = true,
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Search, 
                                contentDescription = null, 
                                modifier = Modifier.size(22.dp), 
                                tint = primaryThemeColor
                            ) 
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close, 
                                        contentDescription = null, 
                                        tint = primaryThemeColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (themeColors.isDark) themeColors.cardBg else Color.White,
                            unfocusedContainerColor = if (themeColors.isDark) themeColors.cardBg else Color.White,
                            focusedBorderColor = primaryThemeColor,
                            unfocusedBorderColor = if (themeColors.isDark) Color(0xFF334155) else primaryThemeColor.copy(alpha = 0.5f),
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText
                        )
                    )

                    // Contacts Button matching search border style exactly
                    IconButton(
                        onClick = { showContactsManagerDialog = true },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (themeColors.isDark) themeColors.cardBg else Color.White)
                            .border(1.5.dp, primaryThemeColor, RoundedCornerShape(18.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = primaryThemeColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // 4. Date Filter Scrollable Pill Button Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalBounceOverscroll()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFilters = listOf(
                        "ALL" to (if (isBn) "সব সময়ের" else "All Time"),
                        "MONTH_FILTER" to (if (isBn) "${getMonthLabel(specificMonth, specificYear)} মাসের" else "${monthsEn[specificMonth]} ${specificYear}"),
                        "DATE_RANGE" to rangeLabel
                    )

                    dateFilters.forEach { (key, label) ->
                        val isSelected = dateFilterType == key
                        val bgColor = if (isSelected) primaryThemeColor.copy(alpha = 0.12f) else if (themeColors.isDark) themeColors.cardBg else Color.White
                        val borderCol = if (isSelected) primaryThemeColor else if (themeColors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        val borderSize = if (isSelected) 1.5.dp else 1.dp
                        val contentColor = if (isSelected) primaryThemeColor else themeHarmonizedTextColor

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(bgColor)
                                .border(borderSize, borderCol, RoundedCornerShape(16.dp))
                                .clickable {
                                    if (key == "MONTH_FILTER") {
                                        dateFilterType = "MONTH_FILTER"
                                        showMonthYearPickerDialog = true
                                    } else if (key == "DATE_RANGE") {
                                        showDateRangePickerDialog = true
                                    } else {
                                        dateFilterType = "ALL"
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor
                            )
                        }
                    }
                }
            }

            // 5. Category Filter Row with Dynamic Badges & Overlapping Counts (Sticky Header)
            stickyHeader {
                Surface(
                    color = screenBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalBounceOverscroll()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            "ALL" to (if (isBn) "সব" else "All"),
                            "INCOME" to (if (isBn) "আয়" else "Income"),
                            "EXPENSE" to (if (isBn) "ব্যয়" else "Expense"),
                            "DEBT_TAKEN" to (if (isBn) "দেনা" else "Debt"),
                            "DEBT_GIVEN" to (if (isBn) "পাওনা" else "Loan")
                        )

                        fun toBnNum(num: Int): String {
                            val benNumbers = mapOf(
                                '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
                                '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
                            )
                            return num.toString().map { benNumbers[it] ?: it }.joinToString("")
                        }

                        tabs.forEach { (key, label) ->
                            val isSelected = selectedFilterTab == key
                            val count = when (key) {
                                "ALL" -> countAll
                                "INCOME" -> countIncome
                                "EXPENSE" -> countExpense
                                "DEBT_TAKEN" -> countDebtTaken
                                "DEBT_GIVEN" -> countDebtGiven
                                else -> 0
                            }

                            Box(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(top = 4.dp, end = 4.dp), // Space for top-right badge overlap
                                contentAlignment = Alignment.TopEnd
                            ) {
                                val chipBg = if (isSelected) primaryThemeColor else if (themeColors.isDark) themeColors.cardBg else Color.White
                                val textColor = if (isSelected) Color.White else themeHarmonizedTextColor
                                val borderColor = if (isSelected) primaryThemeColor else if (themeColors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                val borderSize = if (isSelected) 1.5.dp else 1.dp

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(chipBg)
                                        .border(borderSize, borderColor, RoundedCornerShape(16.dp))
                                        .clickable { selectedFilterTab = key }
                                        .padding(horizontal = 14.dp, vertical = 8.5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }

                                // Dynamic pill/circle badge with count (compact height)
                                if (count > 0) {
                                    val badgeColor = if (key == "ALL") Color(0xFFEF4444) else primaryThemeColor
                                    val countStr = if (isBn) toBnNum(count) else count.toString()

                                    Box(
                                        modifier = Modifier
                                            .offset(x = 4.dp, y = (-3).dp)
                                            .defaultMinSize(minWidth = 14.dp, minHeight = 13.dp)
                                            .background(badgeColor, RoundedCornerShape(7.dp))
                                            .padding(horizontal = 3.dp, vertical = 0.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = countStr,
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 9.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Sort Button on the far right
                        IconButton(
                            onClick = { showSortDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (themeColors.isDark) themeColors.cardBg else Color.White)
                                .border(1.dp, if (themeColors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Sort/Filter",
                                tint = primaryThemeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 6. Section Title Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thick vertical indicator
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(primaryThemeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Small card with calendar icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (themeColors.isDark) themeColors.cardBg else Color.White)
                            .border(1.dp, if (themeColors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = primaryThemeColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    // Title Text
                    Text(
                        text = if (isBn) "চলতি মাসের লেনদেন" else "Current Month's Transactions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Count Badge
                    fun toBnNum(num: Int): String {
                        val benNumbers = mapOf(
                            '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
                            '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
                        )
                        return num.toString().map { benNumbers[it] ?: it }.joinToString("")
                    }
                    val totalCountLabel = if (isBn) "${toBnNum(filteredList.size)}টি" else "${filteredList.size} Items"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(primaryThemeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = totalCountLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryThemeColor
                        )
                    }
                }
            }

            // 7. Grouped list of transaction cards matching the picture
            groupedTransactions.forEach { (dateHeader, itemsInGroup) ->
                item {
                    Text(
                        text = dateHeader,
                        fontSize = 13.sp,
                        color = Color(0xFF718096),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                
                items(itemsInGroup) { item ->
                    val isSelected = selectedIds.contains(item.id)
                    val isSelectionMode = selectedIds.isNotEmpty()
                    TransactionCard(
                        item = item,
                        isBn = isBn,
                        themeColors = themeColors,
                        formatAmount = ::formatAmount,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onToggleSelect = {
                            selectedIds = if (isSelected) selectedIds - item.id else selectedIds + item.id
                        },
                        onToggleSettled = {
                            viewModel.updateFinanceTransaction(item.copy(isSettled = !item.isSettled))
                        },
                        onClick = {
                            if (isSelectionMode) {
                                selectedIds = if (isSelected) selectedIds - item.id else selectedIds + item.id
                            } else {
                                selectedTransactionDetails = item
                            }
                        },
                        onDelete = {
                            if (!isSelectionMode) {
                                selectedIds = setOf(item.id)
                            }
                        }
                    )
                }
            }
        }
    }

    // Savings Target Edit Dialog
    if (showTargetEditDialog) {
        var tempTarget by remember { mutableStateOf(savingsTarget.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showTargetEditDialog = false },
            title = {
                Text(
                    text = if (isBn) "সঞ্চয় লক্ষ্যমাত্রা পরিবর্তন" else "Edit Savings Target",
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            },
            text = {
                OutlinedTextField(
                    value = tempTarget,
                    onValueChange = { tempTarget = it },
                    label = { Text(if (isBn) "লক্ষ্যমাত্রা পরিমাণ (৳)" else "Target Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = themeColors.background,
                        unfocusedContainerColor = themeColors.background,
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                        focusedLabelColor = themeColors.buttonEqualBg,
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = tempTarget.toDoubleOrNull() ?: 50000.0
                        saveSavingsTarget(context, amt)
                        savingsTarget = amt
                        showTargetEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBn) "ঠিক আছে" else "Set Target", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetEditDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Date Range Picker Dialog
    if (showDateRangePickerDialog) {
        val showDatePicker = { isStart: Boolean ->
            val currentSelected = if (isStart) rangeStartDate else rangeEndDate
            val cal = Calendar.getInstance().apply {
                if (currentSelected != null) {
                    timeInMillis = currentSelected
                }
            }
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }
                    if (isStart) {
                        rangeStartDate = selectedCal.timeInMillis
                    } else {
                        rangeEndDate = selectedCal.timeInMillis
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog(
            onDismissRequest = { showDateRangePickerDialog = false },
            title = {
                Text(
                    text = if (isBn) "তারিখের ব্যবধান নির্বাচন করুন" else "Select Date Range",
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isBn) "শুরুর তারিখ ও শেষের তারিখ নির্বাচন করুন:" else "Choose start and end dates:",
                        fontSize = 13.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                    
                    // Start Date Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(themeColors.background)
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .clickable { showDatePicker(true) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "শুরুর তারিখ" else "Start Date",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.5f)
                            )
                            val startValStr = if (rangeStartDate != null) {
                                SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(rangeStartDate!!))
                            } else {
                                if (isBn) "নির্বাচন করুন" else "Select"
                            }
                            Text(
                                text = startValStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                        }
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
                    }

                    // End Date Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(themeColors.background)
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .clickable { showDatePicker(false) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "শেষের তারিখ" else "End Date",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.5f)
                            )
                            val endValStr = if (rangeEndDate != null) {
                                SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(rangeEndDate!!))
                            } else {
                                if (isBn) "নির্বাচন করুন" else "Select"
                            }
                            Text(
                                text = endValStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                        }
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rangeStartDate == null) rangeStartDate = System.currentTimeMillis()
                        if (rangeEndDate == null) rangeEndDate = System.currentTimeMillis()
                        dateFilterType = "DATE_RANGE"
                        showDateRangePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isBn) "ঠিক আছে" else "Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePickerDialog = false }) {
                    Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Specific Month Selection Dialog
    if (showMonthYearPickerDialog) {
        AlertDialog(
            onDismissRequest = { showMonthYearPickerDialog = false },
            title = {
                Text(
                    text = if (isBn) "নির্দিষ্ট মাস সিলেক্ট করুন" else "Select Specific Month",
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Year Select Scrollable Row or selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBn) "বছর:" else "Year:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { specificYear-- }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = themeColors.buttonEqualBg)
                            }
                            Text(
                                text = if (isBn) {
                                    val benNumbers = mapOf(
                                        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
                                        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
                                    )
                                    specificYear.toString().map { benNumbers[it] ?: it }.joinToString("")
                                } else specificYear.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            IconButton(onClick = { specificYear++ }) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = themeColors.buttonEqualBg)
                            }
                        }
                    }

                    Divider(color = themeColors.displayText.copy(alpha = 0.08f))

                    // Month Grids
                    Text(if (isBn) "মাস:" else "Month:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                    Box(modifier = Modifier.height(200.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0..3) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (col in 0..2) {
                                        val mIdx = row * 3 + col
                                        val isMSelected = specificMonth == mIdx
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isMSelected) themeColors.buttonEqualBg else themeColors.background)
                                                .clickable { specificMonth = mIdx }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isBn) monthsBn[mIdx] else monthsEn[mIdx].take(3),
                                                fontSize = 12.sp,
                                                fontWeight = if (isMSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isMSelected) Color.White else themeColors.displayText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        dateFilterType = "MONTH_FILTER"
                        showMonthYearPickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBn) "নিশ্চিত করুন" else "Select", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMonthYearPickerDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Contacts Manager Dialog (ব্যক্তিদের তালিকা)
    if (showContactsManagerDialog) {
        var showAddContactForm by remember { mutableStateOf(false) }
        var contactSearchQuery by remember { mutableStateOf("") }
        var cName by remember { mutableStateOf("") }
        var cPhone by remember { mutableStateOf("") }
        var cAddress by remember { mutableStateOf("") }

        val filteredContacts = remember(contactsList, contactSearchQuery) {
            contactsList.filter {
                it.name.contains(contactSearchQuery, ignoreCase = true) ||
                it.phone.contains(contactSearchQuery, ignoreCase = true) ||
                it.address.contains(contactSearchQuery, ignoreCase = true)
            }
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showContactsManagerDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = themeColors.cardBg,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header with solid teal background and rounded top corners
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = themeColors.buttonEqualBg,
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "ব্যক্তিদের তালিকা" else "Contact Directory",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            IconButton(
                                onClick = { showAddContactForm = !showAddContactForm },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (showAddContactForm) Icons.Default.Close else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedVisibility(
                            visible = showAddContactForm,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = themeColors.background,
                                border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isBn) "নতুন ব্যক্তি যোগ করুন" else "Create New Contact",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.buttonEqualBg
                                    )
                                    OutlinedTextField(
                                        value = cName,
                                        onValueChange = { cName = it },
                                        label = { Text(if (isBn) "পূর্ণ নাম" else "Full Name", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = themeColors.cardBg,
                                            unfocusedContainerColor = themeColors.cardBg,
                                            focusedBorderColor = themeColors.buttonEqualBg,
                                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.12f),
                                            focusedTextColor = themeColors.displayText,
                                            unfocusedTextColor = themeColors.displayText
                                        )
                                    )
                                    OutlinedTextField(
                                        value = cPhone,
                                        onValueChange = { cPhone = it },
                                        label = { Text(if (isBn) "মোবাইল নম্বর" else "Phone Number", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = themeColors.cardBg,
                                            unfocusedContainerColor = themeColors.cardBg,
                                            focusedBorderColor = themeColors.buttonEqualBg,
                                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.12f),
                                            focusedTextColor = themeColors.displayText,
                                            unfocusedTextColor = themeColors.displayText
                                        )
                                    )
                                    OutlinedTextField(
                                        value = cAddress,
                                        onValueChange = { cAddress = it },
                                        label = { Text(if (isBn) "ঠিকানা" else "Address", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = themeColors.cardBg,
                                            unfocusedContainerColor = themeColors.cardBg,
                                            focusedBorderColor = themeColors.buttonEqualBg,
                                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.12f),
                                            focusedTextColor = themeColors.displayText,
                                            unfocusedTextColor = themeColors.displayText
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            if (cName.isBlank()) {
                                                Toast.makeText(context, if (isBn) "নাম অবশ্যই দিতে হবে" else "Name is required", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            val newC = ContactPerson(
                                                name = cName.trim(),
                                                phone = cPhone.trim(),
                                                address = cAddress.trim(),
                                                colorIndex = (0..6).random()
                                            )
                                            val newList = contactsList + newC
                                            saveContacts(context, newList)
                                            contactsList = newList
                                            cName = ""
                                            cPhone = ""
                                            cAddress = ""
                                            showAddContactForm = false
                                            Toast.makeText(context, if (isBn) "ব্যক্তি সংরক্ষিত হয়েছে" else "Contact added successfully", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (isBn) "যোগ করুন" else "Save Contact", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Contacts List with search
                        OutlinedTextField(
                            value = contactSearchQuery,
                            onValueChange = { contactSearchQuery = it },
                            placeholder = { Text(if (isBn) "ব্যক্তি খুঁজুন..." else "Search contact...", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = themeColors.background,
                                unfocusedContainerColor = themeColors.background,
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.1f),
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText
                            )
                        )

                        Box(modifier = Modifier.height(200.dp)) {
                            if (filteredContacts.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isBn) "কোনো ব্যক্তি পাওয়া যায়নি" else "No contacts found",
                                        fontSize = 12.sp,
                                        color = themeColors.displayText.copy(alpha = 0.5f)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(filteredContacts) { contact ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(themeColors.background)
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(CircleShape)
                                                        .background(avatarGradients[contact.colorIndex % avatarGradients.size]),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = contact.name.take(1).uppercase(),
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = contact.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColors.displayText
                                                    )
                                                    if (contact.phone.isNotEmpty()) {
                                                        Text(
                                                            text = contact.phone,
                                                            fontSize = 11.sp,
                                                            color = themeColors.displayText.copy(alpha = 0.6f)
                                                        )
                                                    }
                                                    if (contact.address.isNotEmpty()) {
                                                        Text(
                                                            text = contact.address,
                                                            fontSize = 10.sp,
                                                            color = themeColors.displayText.copy(alpha = 0.5f),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    deleteContactTarget = contact
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = Color.Red.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showContactsManagerDialog = false },
                                colors = ButtonDefaults.textButtonColors(contentColor = themeColors.buttonEqualBg)
                            ) {
                                Text(if (isBn) "বাতিল" else "Cancel", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Transaction Dialog
    if (showAddDialog) {
        AddEditTransactionDialog(
            initialTransaction = editingTransaction,
            isBn = isBn,
            themeColors = themeColors,
            contactsList = contactsList,
            onDismiss = { showAddDialog = false },
            onSave = { transaction ->
                if (editingTransaction != null) {
                    viewModel.updateFinanceTransaction(transaction)
                } else {
                    viewModel.addFinanceTransaction(transaction)
                }
                showAddDialog = false
            },
            onAddQuickContact = { newContact ->
                val newList = contactsList + newContact
                saveContacts(context, newList)
                contactsList = newList
            }
        )
    }

    // Details, Share, Edit, and Delete Dialog
    if (selectedTransactionDetails != null) {
        val item = selectedTransactionDetails!!
        val (typeLabel, badgeBg, badgeTextColor, typeIcon) = remember(item) {
            when (item.type) {
                "INCOME" -> Quadruple(if (isBn) "আয়" else "Income", Color(0xFF22C55E).copy(alpha = 0.15f), Color(0xFF16A34A), Icons.AutoMirrored.Filled.TrendingUp)
                "EXPENSE" -> Quadruple(if (isBn) "ব্যয়" else "Expense", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFDC2626), Icons.AutoMirrored.Filled.TrendingDown)
                "SAVINGS" -> Quadruple(if (isBn) "সঞ্চয়" else "Savings", Color(0xFF3B82F6).copy(alpha = 0.15f), Color(0xFF2563EB), Icons.Default.AccountBalance) // Bank icon
                else -> { // DEBT
                    if (item.subType == "TAKEN") {
                        Quadruple(if (isBn) "দেনা (দেবেন)" else "Debt", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFD97706), Icons.Default.CallReceived)
                    } else {
                        Quadruple(if (isBn) "পাওনা (পাবেন)" else "Loan", Color(0xFF8B5CF6).copy(alpha = 0.15f), Color(0xFF7C3AED), Icons.Default.CallMade)
                    }
                }
            }
        }
        val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
        val formattedTime = sdf.format(Date(item.timestamp))

        AlertDialog(
            onDismissRequest = { selectedTransactionDetails = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = badgeTextColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isBn) "লেনদেন বিবরণী" else "Transaction Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = typeLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = (if (item.type == "EXPENSE" || (item.type == "DEBT" && item.subType == "GIVEN")) "-" else "+") + formatAmount(item.amount),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeTextColor
                            )
                        }
                    }

                    DetailItemRow(
                        label = if (isBn) "বিবরণ:" else "Description:",
                        value = item.title,
                        themeColors = themeColors
                    )

                    if (item.category.isNotEmpty()) {
                        DetailItemRow(
                            label = if (isBn) "ক্যাটাগরি:" else "Category:",
                            value = item.category,
                            themeColors = themeColors
                        )
                    }

                    DetailItemRow(
                        label = if (isBn) "তারিখ ও সময়:" else "Date & Time:",
                        value = formattedTime,
                        themeColors = themeColors
                    )

                    if (item.note.isNotBlank()) {
                        DetailItemRow(
                            label = if (isBn) "অতিরিক্ত নোট:" else "Note:",
                            value = item.note,
                            themeColors = themeColors
                        )
                    }

                    if (item.type == "DEBT") {
                        DetailItemRow(
                            label = if (isBn) "অবস্থা:" else "Status:",
                            value = if (item.isSettled) (if (isBn) "পরিশোধিত" else "Settled") else (if (isBn) "বকেয়া (ক্লিক করে মেটান)" else "Pending"),
                            themeColors = themeColors,
                            valueColor = if (item.isSettled) Color(0xFF16A34A) else Color(0xFFD97706)
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Share button
                    Button(
                        onClick = {
                            try {
                                val shareText = if (isBn) {
                                    "📊 লেনদেন বিবরণী:\n📌 শিরোনাম: ${item.title}\n📈 ধরণ: $typeLabel\n🏷️ ক্যাটাগরি: ${item.category}\n💰 পরিমাণ: ${formatAmount(item.amount)}\n🗓️ সময়: $formattedTime\n📝 নোট: ${item.note}"
                                } else {
                                    "📊 Transaction Details:\n📌 Title: ${item.title}\n📈 Type: $typeLabel\n🏷️ Category: ${item.category}\n💰 Amount: ${formatAmount(item.amount)}\n🗓️ Time: $formattedTime\n📝 Note: ${item.note}"
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, if (isBn) "শেয়ার করুন" else "Share via"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "শেয়ার" else "Share", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Edit button
                    Button(
                        onClick = {
                            editingTransaction = item
                            showAddDialog = true
                            selectedTransactionDetails = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "এডিট" else "Edit", color = themeColors.buttonEqualBg, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Delete button
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = item
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "মুছুন" else "Delete", color = Color(0xFFDC2626), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTransactionDetails = null }) {
                    Text(if (isBn) "বন্ধ করুন" else "Close", color = themeColors.displayText.copy(alpha = 0.7f))
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // 1. Delete single transaction with Numeric Captcha Confirmation
    if (showDeleteConfirmDialog != null) {
        val target = showDeleteConfirmDialog!!
        NumericCaptchaDeleteDialog(
            title = if (isBn) "লেনদেন মুছে ফেলবেন?" else "Delete Transaction?",
            message = if (isBn) "\"${target.title}\" (${formatAmount(target.amount)}) লেনদেনটি স্থায়ীভাবে মুছে ফেলতে নিচের সংখ্যা ক্যাপচা কোডটি পূরণ করুন।" else "Enter the numeric security captcha below to delete transaction \"${target.title}\" (${formatAmount(target.amount)}).",
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { showDeleteConfirmDialog = null },
            onConfirmDelete = {
                viewModel.deleteFinanceTransaction(target.id)
                if (selectedTransactionDetails?.id == target.id) {
                    selectedTransactionDetails = null
                }
                showDeleteConfirmDialog = null
                Toast.makeText(context, if (isBn) "লেনদেনটি সফলভাবে মুছে ফেলা হয়েছে" else "Transaction deleted successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 2. Delete contact person with Numeric Captcha Confirmation
    if (deleteContactTarget != null) {
        val target = deleteContactTarget!!
        NumericCaptchaDeleteDialog(
            title = if (isBn) "ব্যক্তি মুছে ফেলবেন?" else "Delete Contact Person?",
            message = if (isBn) "\"${target.name}\" কে তালিকা থেকে স্থায়ীভাবে মুছে ফেলতে নিচের সংখ্যা ক্যাপচা কোডটি পূরণ করুন।" else "Enter the numeric security captcha below to delete contact \"${target.name}\".",
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { deleteContactTarget = null },
            onConfirmDelete = {
                val newList = contactsList.filter { it.id != target.id }
                saveContacts(context, newList)
                contactsList = newList
                deleteContactTarget = null
                Toast.makeText(context, if (isBn) "ব্যক্তি সফলভাবে মুছে ফেলা হয়েছে" else "Contact deleted successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 3. Multi-select Delete with Numeric Captcha Confirmation
    if (showMultiDeleteConfirmDialog) {
        NumericCaptchaDeleteDialog(
            title = if (isBn) "নির্বাচিত লেনদেন মুছে ফেলবেন?" else "Delete Selected Transactions?",
            message = if (isBn) "নির্বাচিত ${if (isBn) toBanglaDigits(selectedIds.size.toString()) else selectedIds.size.toString()} টি লেনদেন মুছে ফেলতে নিচের সংখ্যা ক্যাপচা কোডটি পূরণ করুন।" else "Enter the numeric security captcha below to delete ${selectedIds.size} selected transactions.",
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { showMultiDeleteConfirmDialog = false },
            onConfirmDelete = {
                selectedIds.forEach { id ->
                    viewModel.deleteFinanceTransaction(id)
                }
                if (selectedTransactionDetails != null && selectedIds.contains(selectedTransactionDetails!!.id)) {
                    selectedTransactionDetails = null
                }
                val count = selectedIds.size
                selectedIds = emptySet()
                showMultiDeleteConfirmDialog = false
                Toast.makeText(context, if (isBn) "$count টি লেনদেন সফলভাবে মুছে ফেলা হয়েছে" else "Selected transactions deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 4. Clear All Confirmation Dialog with Numeric Captcha
    if (showClearAllConfirm) {
        NumericCaptchaDeleteDialog(
            title = if (isBn) "সব লেনদেন মুছে ফেলবেন?" else "Clear All Transactions?",
            message = if (isBn) "আপনার সমস্ত আয়, ব্যয়, দেনা ও পাওনার তথ্য মুছে ফেলার জন্য নিচের সংখ্যা ক্যাপচা কোডটি পূরণ করুন।" else "Enter the numeric security captcha below to permanently wipe all finance records.",
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { showClearAllConfirm = false },
            onConfirmDelete = {
                viewModel.clearAllFinanceTransactions()
                selectedTransactionDetails = null
                showClearAllConfirm = false
                Toast.makeText(context, if (isBn) "সমস্ত লেনদেন সফলভাবে মুছে ফেলা হয়েছে" else "All records cleared", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Sort Options Dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = {
                Text(
                    text = if (isBn) "লেনদেন সাজান" else "Sort Transactions",
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = if (isBn) "সাজানোর ভিত্তি (Sort By):" else "Sort By:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                    
                    // Sort Options: TIME, NAME, AMOUNT
                    val sortOptions = listOf(
                        "TIME" to (if (isBn) "সময় (Time)" else "Time"),
                        "NAME" to (if (isBn) "নাম (Name/Title)" else "Name/Title"),
                        "AMOUNT" to (if (isBn) "পরিমাণ (Amount)" else "Amount")
                    )
                    
                    sortOptions.forEach { (option, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sortBy = option }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortBy == option,
                                onClick = { sortBy = option },
                                colors = RadioButtonDefaults.colors(selectedColor = themeColors.buttonEqualBg)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, color = themeColors.displayText, fontSize = 14.sp)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(themeColors.displayText.copy(alpha = 0.12f))
                    )
                    
                    Text(
                        text = if (isBn) "ক্রম (Order):" else "Order:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.8f)
                    )
                    
                    // Order Options: ASC, DESC
                    val orderOptions = listOf(
                        "DESC" to (if (isBn) "নতুন/বড় আগে (Descending)" else "Newest/Highest First"),
                        "ASC" to (if (isBn) "পুরোনো/ছোট আগে (Ascending)" else "Oldest/Lowest First")
                    )
                    
                    orderOptions.forEach { (order, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sortOrder = order }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortOrder == order,
                                onClick = { sortOrder = order },
                                colors = RadioButtonDefaults.colors(selectedColor = themeColors.buttonEqualBg)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, color = themeColors.displayText, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSortDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isBn) "ঠিক আছে" else "Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun DetailItemRow(
    label: String,
    value: String,
    themeColors: CalculatorThemeColors,
    valueColor: Color = themeColors.displayText
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = themeColors.displayText.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.5.sp,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(5.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(themeColors.displayText.copy(alpha = 0.05f)))
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TransactionCard(
    item: FinanceTransaction,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    formatAmount: (Double) -> String,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelect: () -> Unit,
    onToggleSettled: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (typeLabel, icon, color, isMinus) = remember(item) {
        when (item.type) {
            "INCOME" -> Quadruple(if (isBn) "আয়" else "Income", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF16A34A), false)
            "EXPENSE" -> Quadruple(if (isBn) "ব্যয়" else "Expense", Icons.AutoMirrored.Filled.TrendingDown, Color(0xFFDC2626), true)
            "SAVINGS" -> Quadruple(if (isBn) "সঞ্চয়" else "Savings", Icons.Default.AccountBalance, Color(0xFF2563EB), item.subType == "WITHDRAWAL")
            else -> { // DEBT
                if (item.subType == "TAKEN") {
                    Quadruple(if (isBn) "দেনা" else "Debt", Icons.Default.CallReceived, Color(0xFFD97706), false)
                } else {
                    Quadruple(if (isBn) "পাওনা" else "Loan", Icons.Default.CallMade, Color(0xFF7C3AED), true)
                }
            }
        }
    }

    val (typeBadgeText, typeBadgeBg, typeBadgeTextCol) = when (item.type) {
        "INCOME" -> Triple(if (isBn) "আয়" else "Income", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "EXPENSE" -> Triple(if (isBn) "ব্যয়" else "Expense", Color(0xFFFFEBEE), Color(0xFFC62828))
        "SAVINGS" -> Triple(if (isBn) "সঞ্চয়" else "Savings", Color(0xFFE3F2FD), Color(0xFF1565C0))
        else -> { // DEBT
            if (item.subType == "TAKEN") {
                Triple(if (isBn) "দেনা" else "Debt", Color(0xFFFFF8E1), Color(0xFFF57F17))
            } else {
                Triple(if (isBn) "পাওনা" else "Loan", Color(0xFFE8EAF6), Color(0xFF283593))
            }
        }
    }

    val categoryBg = themeColors.buttonEqualBg.copy(alpha = 0.1f)
    val categoryTextCol = themeColors.buttonEqualBg

    val sdf = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val timeString = sdf.format(Date(item.timestamp))

    val titleTextColor = if (themeColors.isDark) {
        Color(0xFFF1F5F9)
    } else {
        Color(
            red = (themeColors.buttonEqualBg.red * 0.35f + 0.12f * 0.65f).coerceIn(0f, 1f),
            green = (themeColors.buttonEqualBg.green * 0.35f + 0.18f * 0.65f).coerceIn(0f, 1f),
            blue = (themeColors.buttonEqualBg.blue * 0.35f + 0.24f * 0.65f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onDelete() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                themeColors.buttonEqualBg.copy(alpha = 0.08f)
            } else if (themeColors.isDark) {
                themeColors.cardBg
            } else {
                Color.White
            }
        ),
        border = BorderStroke(
            if (isSelected) 1.dp else 0.8.dp,
            if (isSelected) themeColors.buttonEqualBg.copy(alpha = 0.7f)
            else if (themeColors.isDark) Color(0xFF334155)
            else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox for Selection Mode
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = themeColors.buttonEqualBg,
                        uncheckedColor = Color(0xFF94A3B8),
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 6.dp)
                )
            }

            // Left side circular Icon indicator
            val iconBg = when (item.type) {
                "INCOME" -> Color(0xFFE8F5E9)
                "EXPENSE" -> Color(0xFFFFEBEE)
                "SAVINGS" -> Color(0xFFE3F2FD)
                else -> { // DEBT
                    if (item.subType == "TAKEN") Color(0xFFFFF8E1) else Color(0xFFE8EAF6)
                }
            }
            val iconTint = when (item.type) {
                "INCOME" -> Color(0xFF2E7D32)
                "EXPENSE" -> Color(0xFFC62828)
                "SAVINGS" -> Color(0xFF1565C0)
                else -> { // DEBT
                    if (item.subType == "TAKEN") Color(0xFFF57F17) else Color(0xFF283593)
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text and Info Column extending all the way to the right
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.5.dp)
            ) {
                // Line 1: Title & Category / Subcategory Badges (extends full width to the right margin)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Type Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeBadgeBg)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeBadgeText,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeBadgeTextCol
                        )
                    }

                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryBg)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryTextCol
                        )
                    }

                    // Debt status badge
                    if (item.type == "DEBT") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (item.isSettled) Color(0xFFE8F5E9)
                                    else Color(0xFFFFF8E1)
                                )
                                .clickable { onToggleSettled() }
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (item.isSettled) (if (isBn) "পরিশোধিত" else "Settled") else (if (isBn) "বকেয়া" else "Pending"),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isSettled) Color(0xFF2E7D32) else Color(0xFFF57F17)
                            )
                        }
                    }
                }

                // Line 2: Timestamp on left and Amount on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Timestamp
                    Text(
                        text = timeString,
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.55f)
                    )

                    // Amount
                    Text(
                        text = (if (isMinus) "-" else "+") + formatAmount(item.amount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

// Dialog for Adding or Editing transaction
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: FinanceTransaction?,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    contactsList: List<ContactPerson>,
    onDismiss: () -> Unit,
    onSave: (FinanceTransaction) -> Unit,
    onAddQuickContact: (ContactPerson) -> Unit
) {
    var type by remember { mutableStateOf(initialTransaction?.type ?: "EXPENSE") }
    var subType by remember { mutableStateOf(initialTransaction?.subType ?: (if (type == "DEBT") "TAKEN" else "")) }
    var title by remember { mutableStateOf(initialTransaction?.title ?: "") }
    var amountText by remember { mutableStateOf(initialTransaction?.amount?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var category by remember { mutableStateOf(initialTransaction?.category ?: (if (type == "INCOME") "বেতন" else "খাবার")) }
    var note by remember { mutableStateOf(initialTransaction?.note ?: "") }

    // Contact integration states
    var selectedPerson by remember { mutableStateOf<ContactPerson?>(null) }
    var contactSearchQuery by remember { mutableStateOf("") }
    var showAddContactQuickForm by remember { mutableStateOf(false) }
    var quickContactName by remember { mutableStateOf("") }
    var quickContactPhone by remember { mutableStateOf("") }
    var quickContactAddress by remember { mutableStateOf("") }

    val categories = remember(type) {
        when (type) {
            "INCOME" -> listOf("বেতন", "ব্যবসা", "ফ্রিল্যান্সিং", "বোনাস", "উপহার", "অন্যান্য")
            "EXPENSE" -> listOf("খাবার", "বাজার", "বাসা ভাড়া", "বিল", "কেনাকাটা", "চিকিৎসা", "শিক্ষা", "যাতায়াত", "অন্যান্য")
            "DEBT" -> listOf("ব্যক্তিগত ঋণ", "বন্ধুর ধার", "দোকানের বাকি", "ব্যাংক ঋণ", "অন্যান্য")
            else -> listOf("ডিপোজিট", "ডিপিএস", "সঞ্চয়পত্র", "জরুরি ফান্ড", "অন্যান্য")
        }
    }

    LaunchedEffect(type) {
        if (!categories.contains(category)) {
            category = categories.first()
        }
    }

    // Try matching existing contact
    LaunchedEffect(title) {
        if (type == "DEBT" && selectedPerson == null) {
            val matched = contactsList.firstOrNull { it.name.equals(title.trim(), ignoreCase = true) }
            if (matched != null) {
                selectedPerson = matched
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTransaction == null) (if (isBn) "নতুন লেনদেন যুক্ত করুন" else "Add New Transaction") else (if (isBn) "লেনদেন এডিট করুন" else "Edit Transaction"),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = themeColors.displayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Segment Control Capsule Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColors.background, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val types = listOf(
                        "INCOME" to (if (isBn) "আয়" else "Income"),
                        "EXPENSE" to (if (isBn) "ব্যয়" else "Expense"),
                        "DEBT" to (if (isBn) "দেনা/পাওনা" else "Loans"),
                        "SAVINGS" to (if (isBn) "সঞ্চয়" else "Savings")
                    )
                    types.forEach { (tKey, label) ->
                        val selected = type == tKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) themeColors.buttonEqualBg else Color.Transparent)
                                .clickable {
                                    type = tKey
                                    if (tKey == "DEBT") {
                                        subType = "TAKEN"
                                    } else if (tKey == "SAVINGS") {
                                        subType = "DEPOSIT"
                                    } else {
                                        subType = ""
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else themeColors.displayText.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Subtypes
                if (type == "DEBT") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = { subType = "TAKEN" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (subType == "TAKEN") Color(0xFFF59E0B) else themeColors.background,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isBn) "দেনা (নিয়েছি)" else "Debt (Taken)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (subType == "TAKEN") Color.White else themeColors.displayText.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                        Surface(
                            onClick = { subType = "GIVEN" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (subType == "GIVEN") Color(0xFF8B5CF6) else themeColors.background,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isBn) "পাওনা (দিয়েছি)" else "Loan (Given)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (subType == "GIVEN") Color.White else themeColors.displayText.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }

                if (type == "SAVINGS") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = { subType = "DEPOSIT" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (subType == "DEPOSIT") Color(0xFF2563EB) else themeColors.background,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isBn) "জমা করুন" else "Deposit",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (subType == "DEPOSIT") Color.White else themeColors.displayText.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                        Surface(
                            onClick = { subType = "WITHDRAWAL" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (subType == "WITHDRAWAL") Color(0xFFEF4444) else themeColors.background,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isBn) "উত্তোলন করুন" else "Withdrawal",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (subType == "WITHDRAWAL") Color.White else themeColors.displayText.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }

                // Contact Person Selector for Debt and Loans
                if (type == "DEBT") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.background),
                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "ব্যক্তি নির্বাচন করুন" else "Select Person",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                                TextButton(
                                    onClick = { showAddContactQuickForm = !showAddContactQuickForm },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBn) "নতুন ব্যক্তি" else "Add Person", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Quick Contact Creation Row
                            AnimatedVisibility(visible = showAddContactQuickForm) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(themeColors.cardBg, RoundedCornerShape(10.dp))
                                        .padding(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = quickContactName,
                                        onValueChange = { quickContactName = it },
                                        placeholder = { Text(if (isBn) "ব্যক্তির নাম" else "Name", color = themeColors.displayText.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = themeColors.buttonEqualBg,
                                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.12f),
                                            focusedTextColor = themeColors.displayText,
                                            unfocusedTextColor = themeColors.displayText
                                        )
                                    )
                                    OutlinedTextField(
                                        value = quickContactPhone,
                                        onValueChange = { quickContactPhone = it },
                                        placeholder = { Text(if (isBn) "ফোন নম্বর" else "Phone", color = themeColors.displayText.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = themeColors.buttonEqualBg,
                                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.12f),
                                            focusedTextColor = themeColors.displayText,
                                            unfocusedTextColor = themeColors.displayText
                                        )
                                    )
                                    OutlinedTextField(
                                        value = quickContactAddress,
                                        onValueChange = { quickContactAddress = it },
                                        placeholder = { Text(if (isBn) "ঠিকানা" else "Address", color = themeColors.displayText.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = themeColors.buttonEqualBg,
                                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.12f),
                                            focusedTextColor = themeColors.displayText,
                                            unfocusedTextColor = themeColors.displayText
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            if (quickContactName.isBlank()) return@Button
                                            val c = ContactPerson(
                                                name = quickContactName.trim(),
                                                phone = quickContactPhone.trim(),
                                                address = quickContactAddress.trim(),
                                                colorIndex = (0..6).random()
                                            )
                                            onAddQuickContact(c)
                                            selectedPerson = c
                                            title = c.name
                                            note = "ফোন: ${c.phone}, ঠিকানা: ${c.address}"
                                            quickContactName = ""
                                            quickContactPhone = ""
                                            quickContactAddress = ""
                                            showAddContactQuickForm = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isBn) "সংরক্ষণ করুন" else "Save Contact", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }

                            if (selectedPerson != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(themeColors.buttonEqualBg.copy(alpha = 0.15f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(avatarGradients[selectedPerson!!.colorIndex % avatarGradients.size]),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                selectedPerson!!.name.take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(selectedPerson!!.name, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                                            if (selectedPerson!!.phone.isNotEmpty()) {
                                                Text(selectedPerson!!.phone, fontSize = 10.sp, color = themeColors.displayText.copy(alpha = 0.6f))
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { selectedPerson = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = themeColors.displayText.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                    }
                                }
                            } else {
                                // Contact search and selector list
                                OutlinedTextField(
                                    value = contactSearchQuery,
                                    onValueChange = { contactSearchQuery = it },
                                    placeholder = { Text(if (isBn) "ব্যক্তির নাম লিখে খুঁজুন..." else "Search person...", fontSize = 11.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = themeColors.cardBg,
                                        unfocusedContainerColor = themeColors.cardBg,
                                        focusedBorderColor = themeColors.buttonEqualBg,
                                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.1f),
                                        focusedTextColor = themeColors.displayText,
                                        unfocusedTextColor = themeColors.displayText
                                    )
                                )

                                val matchedPersons = contactsList.filter {
                                    it.name.contains(contactSearchQuery, ignoreCase = true) ||
                                    it.phone.contains(contactSearchQuery, ignoreCase = true)
                                }

                                if (matchedPersons.isNotEmpty()) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(matchedPersons) { p ->
                                            Surface(
                                                onClick = {
                                                    selectedPerson = p
                                                    title = p.name
                                                    note = "ফোন: ${p.phone}, ঠিকানা: ${p.address}"
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                color = themeColors.cardBg,
                                                border = BorderStroke(0.5.dp, themeColors.displayText.copy(alpha = 0.12f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(22.dp)
                                                            .clip(CircleShape)
                                                            .background(avatarGradients[p.colorIndex % avatarGradients.size]),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(p.name.take(1).uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(p.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Title Input (if not selected person or we can edit name)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBn) "বিবরণ / শিরোনাম" else "Title / Description") },
                    placeholder = { Text(if (isBn) "যেমন: মাসিক বেতন, বাজার খরচ..." else "e.g. Salary, Grocery...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = themeColors.background,
                        unfocusedContainerColor = themeColors.background,
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                        focusedLabelColor = themeColors.buttonEqualBg,
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    )
                )

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBn) "টাকার পরিমাণ (৳)" else "Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = themeColors.background,
                        unfocusedContainerColor = themeColors.background,
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                        focusedLabelColor = themeColors.buttonEqualBg,
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    )
                )

                // Category Selection Chips
                Text(
                    text = if (isBn) "ক্যাটাগরি" else "Category",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        val selected = category == cat
                        FilterChip(
                            selected = selected,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.buttonEqualBg,
                                selectedLabelColor = Color.White,
                                containerColor = themeColors.background,
                                labelColor = themeColors.displayText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = themeColors.displayText.copy(alpha = 0.12f),
                                selectedBorderColor = themeColors.buttonEqualBg,
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBn) "অতিরিক্ত নোট (ঐচ্ছিক)" else "Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = themeColors.background,
                        unfocusedContainerColor = themeColors.background,
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                        focusedLabelColor = themeColors.buttonEqualBg,
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isBlank() || amt <= 0.0) return@Button
                    val t = FinanceTransaction(
                        id = initialTransaction?.id ?: 0L,
                        title = title.trim(),
                        amount = amt,
                        type = type,
                        subType = subType,
                        category = category,
                        note = note.trim(),
                        timestamp = initialTransaction?.timestamp ?: System.currentTimeMillis(),
                        isSettled = initialTransaction?.isSettled ?: false
                    )
                    onSave(t)
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isBn) "সংরক্ষণ করুন" else "Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
            }
        },
        containerColor = themeColors.cardBg,
        shape = RoundedCornerShape(24.dp)
    )
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

fun toBanglaDigits(str: String): String {
    return str.map { ch ->
        when (ch) {
            '0' -> '০'
            '1' -> '১'
            '2' -> '২'
            '3' -> '৩'
            '4' -> '৪'
            '5' -> '৫'
            '6' -> '৬'
            '7' -> '৭'
            '8' -> '৮'
            '9' -> '৯'
            else -> ch
        }
    }.joinToString("")
}

@Composable
fun NumericCaptchaDeleteDialog(
    title: String,
    message: String,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    var captchaCode by remember { mutableStateOf((1000..9999).random().toString()) }
    var inputCode by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    val normalizedInput = remember(inputCode) {
        inputCode.map { ch ->
            when (ch) {
                '০' -> '0'; '১' -> '1'; '২' -> '2'; '৩' -> '3'; '৪' -> '4'
                '৫' -> '5'; '৬' -> '6'; '৭' -> '7'; '৮' -> '8'; '৯' -> '9'
                else -> ch
            }
        }.joinToString("").trim()
    }
    val isMatched = normalizedInput == captchaCode

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = themeColors.displayText.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 18.sp
                )

                // Captcha display banner
                Surface(
                    color = themeColors.displayText.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isBn) "নিরাপত্তা কোড (ক্যাপচা):" else "Security Captcha Code:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBn) toBanglaDigits(captchaCode) else captchaCode,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 5.sp,
                                color = Color(0xFFDC2626)
                            )
                        }

                        IconButton(
                            onClick = {
                                captchaCode = (1000..9999).random().toString()
                                inputCode = ""
                                hasError = false
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(themeColors.buttonEqualBg.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Captcha",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Captcha Input Field
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = {
                        inputCode = it.filter { char -> char.isDigit() || (isBn && "০১২৩৪৫৬৭৮৯".contains(char)) }.take(6)
                        hasError = false
                    },
                    label = {
                        Text(
                            text = if (isBn) "ক্যাপচা কোড লিখুন" else "Enter captcha code",
                            fontSize = 12.sp
                        )
                    },
                    placeholder = {
                        Text(
                            text = if (isBn) toBanglaDigits(captchaCode) else captchaCode,
                            fontSize = 13.sp,
                            color = themeColors.displayText.copy(alpha = 0.3f)
                        )
                    },
                    singleLine = true,
                    isError = hasError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isMatched) Color(0xFF16A34A) else Color(0xFFDC2626),
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        errorBorderColor = Color(0xFFDC2626),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    )
                )

                if (hasError) {
                    Text(
                        text = if (isBn) "❌ ক্যাপচা কোড মিলেনি! সঠিক কোডটি দিন।" else "❌ Captcha code does not match!",
                        color = Color(0xFFDC2626),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isMatched) {
                        onConfirmDelete()
                    } else {
                        hasError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMatched) Color(0xFFDC2626) else Color(0xFFDC2626).copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBn) "মুছে ফেলুন" else "Delete",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isBn) "বাতিল" else "Cancel",
                    color = themeColors.displayText.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = themeColors.cardBg,
        shape = RoundedCornerShape(24.dp)
    )
}

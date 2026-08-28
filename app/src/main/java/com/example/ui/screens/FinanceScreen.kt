package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI
    val context = LocalContext.current
    val transactions by viewModel.financeTransactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE, DEBT, SAVINGS
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<FinanceTransaction?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

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
    val debtTaken = remember(transactions) { // দেনা (কাউকে দেব)
        transactions.filter { it.type == "DEBT" && it.subType == "TAKEN" && !it.isSettled }.sumOf { it.amount }
    }
    val loanGiven = remember(transactions) { // পাওনা (কারো থেকে পাব)
        transactions.filter { it.type == "DEBT" && it.subType == "GIVEN" && !it.isSettled }.sumOf { it.amount }
    }
    val netBalance = remember(totalIncome, totalExpense, totalSavings, debtTaken, loanGiven) {
        (totalIncome + debtTaken) - (totalExpense + loanGiven)
    }

    val currencySymbol = if (isBn) "৳" else "৳"
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

    val filteredList = remember(transactions, searchQuery, selectedFilterTab) {
        transactions.filter { item ->
            val matchesFilter = when (selectedFilterTab) {
                "INCOME" -> item.type == "INCOME"
                "EXPENSE" -> item.type == "EXPENSE"
                "DEBT" -> item.type == "DEBT"
                "SAVINGS" -> item.type == "SAVINGS"
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.title.contains(searchQuery, ignoreCase = true) ||
                        item.category.contains(searchQuery, ignoreCase = true) ||
                        item.note.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }.sortedByDescending { it.timestamp }
    }

    Scaffold(
        containerColor = themeColors.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingTransaction = null
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                text = {
                    Text(
                        text = if (isBn) "লেনদেন যোগ করুন" else "Add Transaction",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                containerColor = themeColors.buttonEqualBg,
                shape = RoundedCornerShape(24.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "ব্যক্তিগত ফিন্যান্স ম্যানেজার" else "Personal Finance Tracker",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Text(
                            text = if (isBn) "আয়, ব্যয়, দেনা, পাওনা ও সঞ্চয়ের হিসাব" else "Income, Expense, Debt & Savings",
                            fontSize = 11.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.65f)
                        )
                    }
                }

                if (transactions.isNotEmpty()) {
                    IconButton(onClick = { showClearAllConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All",
                            tint = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Net Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    themeColors.buttonEqualBg,
                                    themeColors.buttonEqualBg.copy(alpha = 0.85f),
                                    themeColors.cardBg
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "মোট নেট স্থিতি (Net Balance)" else "Total Net Balance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.20f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isBn) "${transactions.size} টি লেনদেন" else "${transactions.size} Transactions",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatAmount(netBalance),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Financial Summary Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Income Pill
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(if (isBn) "আয়" else "Income", fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f))
                                    Text(formatAmount(totalIncome), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Expense Pill
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(if (isBn) "ব্যয়" else "Expense", fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f))
                                    Text(formatAmount(totalExpense), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Savings Pill
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(if (isBn) "সঞ্চয়" else "Savings", fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f))
                                    Text(formatAmount(totalSavings), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Detail Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricMiniCard(
                    title = if (isBn) "আয়" else "Income",
                    amountStr = formatAmount(totalIncome),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    accentColor = Color(0xFF22C55E),
                    bgColor = themeColors.cardBg,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFilterTab = "INCOME" }
                )
                MetricMiniCard(
                    title = if (isBn) "ব্যয়" else "Expense",
                    amountStr = formatAmount(totalExpense),
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    accentColor = Color(0xFFEF4444),
                    bgColor = themeColors.cardBg,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFilterTab = "EXPENSE" }
                )
                MetricMiniCard(
                    title = if (isBn) "দেনা (দেবেন)" else "Debt (Payable)",
                    amountStr = formatAmount(debtTaken),
                    icon = Icons.Default.CallReceived,
                    accentColor = Color(0xFFF59E0B),
                    bgColor = themeColors.cardBg,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFilterTab = "DEBT" }
                )
                MetricMiniCard(
                    title = if (isBn) "পাওনা (পাবেন)" else "Loan (Receivable)",
                    amountStr = formatAmount(loanGiven),
                    icon = Icons.Default.CallMade,
                    accentColor = Color(0xFF8B5CF6),
                    bgColor = themeColors.cardBg,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFilterTab = "DEBT" }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs & Search Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isBn) "খুঁজুন (টাইটেল, ক্যাটাগরি)..." else "Search...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.5.sp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = themeColors.cardBg,
                        unfocusedContainerColor = themeColors.cardBg,
                        focusedIndicatorColor = themeColors.buttonEqualBg,
                        unfocusedIndicatorColor = themeColors.displayText.copy(alpha = 0.25f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf(
                    "ALL" to (if (isBn) "সব (${transactions.size})" else "All (${transactions.size})"),
                    "INCOME" to (if (isBn) "আয় 📈" else "Income 📈"),
                    "EXPENSE" to (if (isBn) "ব্যয় 📉" else "Expense 📉"),
                    "DEBT" to (if (isBn) "দেনা/পাওনা 🤝" else "Loans 🤝"),
                    "SAVINGS" to (if (isBn) "সঞ্চয় 💰" else "Savings 💰")
                )
                items(filters) { (key, label) ->
                    val isSelected = selectedFilterTab == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilterTab = key },
                        label = { Text(label, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.buttonEqualBg,
                            selectedLabelColor = Color.White,
                            containerColor = themeColors.cardBg,
                            labelColor = themeColors.displayText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.12f),
                            selectedBorderColor = themeColors.buttonEqualBg,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transactions List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = themeColors.displayText.copy(alpha = 0.35f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isBn) "কোনো লেনদেনের হিসাব পাওয়া যায়নি" else "No financial transactions found",
                            fontSize = 14.sp,
                            color = themeColors.displayText.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) "নিচের + বাটন চেপে নতুন আয়, ব্যয় বা দেনা-পাওনা যোগ করুন" else "Tap + button below to record income, expense, or loans",
                            fontSize = 11.5.sp,
                            color = themeColors.displayText.copy(alpha = 0.45f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        TransactionCard(
                            item = item,
                            isBn = isBn,
                            themeColors = themeColors,
                            formatAmount = { formatAmount(it) },
                            onToggleSettled = {
                                viewModel.updateFinanceTransaction(item.copy(isSettled = !item.isSettled))
                            },
                            onEdit = {
                                editingTransaction = item
                                showAddDialog = true
                            },
                            onDelete = {
                                showDeleteConfirmDialog = item
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Transaction Dialog Sheet
    if (showAddDialog) {
        AddEditTransactionDialog(
            initialTransaction = editingTransaction,
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { showAddDialog = false },
            onSave = { transaction ->
                if (editingTransaction != null) {
                    viewModel.updateFinanceTransaction(transaction)
                } else {
                    viewModel.addFinanceTransaction(transaction)
                }
                showAddDialog = false
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog != null) {
        val target = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text(if (isBn) "লেনদেন মুছে ফেলবেন?" else "Delete Transaction?", color = themeColors.displayText) },
            text = { Text(if (isBn) "\"${target.title}\" লেনদেনটি তালিকা থেকে স্থায়ীভাবে মুছে ফেলা হবে।" else "Are you sure you want to delete \"${target.title}\"?", color = themeColors.displayText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFinanceTransaction(target.id)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (isBn) "মুছে ফেলুন" else "Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text(if (isBn) "সব লেনদেন মুছে ফেলবেন?" else "Clear All Transactions?", color = themeColors.displayText) },
            text = { Text(if (isBn) "আপনার সমস্ত ফিন্যান্সিয়াল লেনদেনের তথ্য ডিলিট হয়ে যাবে।" else "This will delete all saved income, expense, and loan records.", color = themeColors.displayText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllFinanceTransactions()
                        showClearAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (isBn) "সব মুছুন" else "Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg
        )
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    amountStr: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amountStr,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TransactionCard(
    item: FinanceTransaction,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    formatAmount: (Double) -> String,
    onToggleSettled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedTime = sdf.format(Date(item.timestamp))

    val (typeLabel, badgeBg, badgeTextColor, typeIcon) = remember(item) {
        when (item.type) {
            "INCOME" -> Quadruple(if (isBn) "আয়" else "Income", Color(0xFF22C55E).copy(alpha = 0.15f), Color(0xFF16A34A), Icons.AutoMirrored.Filled.TrendingUp)
            "EXPENSE" -> Quadruple(if (isBn) "ব্যয়" else "Expense", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFDC2626), Icons.AutoMirrored.Filled.TrendingDown)
            "SAVINGS" -> Quadruple(if (isBn) "সঞ্চয়" else "Savings", Color(0xFF3B82F6).copy(alpha = 0.15f), Color(0xFF2563EB), Icons.Default.Savings)
            else -> { // DEBT
                if (item.subType == "TAKEN") {
                    Quadruple(if (isBn) "দেনা (দেবেন)" else "Debt", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFD97706), Icons.Default.CallReceived)
                } else {
                    Quadruple(if (isBn) "পাওনা (পাবেন)" else "Loan", Color(0xFF8B5CF6).copy(alpha = 0.15f), Color(0xFF7C3AED), Icons.Default.CallMade)
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = typeIcon, contentDescription = null, tint = badgeTextColor, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(color = badgeBg, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = typeLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (item.category.isNotEmpty()) {
                            Surface(color = themeColors.displayText.copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = item.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.displayText.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.note.isNotBlank() || formattedTime.isNotEmpty()) {
                        Text(
                            text = if (item.note.isNotBlank()) "${item.note} • $formattedTime" else formattedTime,
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.60f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (item.type == "DEBT") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            onClick = onToggleSettled,
                            color = if (item.isSettled) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.isSettled) Icons.Default.CheckCircle else Icons.Default.Pending,
                                    contentDescription = null,
                                    tint = if (item.isSettled) Color(0xFF16A34A) else Color(0xFFD97706),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (item.isSettled) (if (isBn) "পরিশোধিত" else "Settled") else (if (isBn) "বকেয়া (ক্লিক করে পরিশোধিত করুন)" else "Pending"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isSettled) Color(0xFF16A34A) else Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (item.type == "EXPENSE" || (item.type == "DEBT" && item.subType == "GIVEN")) "-" else "+") + formatAmount(item.amount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeTextColor
                )

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = themeColors.displayText.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444).copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: FinanceTransaction?,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onSave: (FinanceTransaction) -> Unit
) {
    var type by remember { mutableStateOf(initialTransaction?.type ?: "EXPENSE") }
    var subType by remember { mutableStateOf(initialTransaction?.subType ?: (if (type == "DEBT") "TAKEN" else "")) }
    var title by remember { mutableStateOf(initialTransaction?.title ?: "") }
    var amountText by remember { mutableStateOf(initialTransaction?.amount?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var category by remember { mutableStateOf(initialTransaction?.category ?: (if (type == "INCOME") "বেতন" else "খাবার")) }
    var note by remember { mutableStateOf(initialTransaction?.note ?: "") }

    val categories = remember(type) {
        when (type) {
            "INCOME" -> listOf("বেতন", "ব্যবসা", "ফ্রিল্যান্সিং", "বোনাস", "উপহার", "অন্যান্য")
            "EXPENSE" -> listOf("খাবার", "বাজার", "বাসা ভাড়া", "বিল", "কেনাকাটা", "চিকিৎসা", "শিক্ষা", "যাতায়াত", "অন্যান্য")
            "DEBT" -> listOf("ব্যক্তিগত ঋণ", "বন্ধুর ধার", "দোকানের বাকি", "ব্যাংক ঋণ", "অন্যান্য")
            else -> listOf("ডিপোজিট", "ডিপিএস", "সঞ্চয়পত্র", "জরুরি ফান্ড", "অন্যান্য")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTransaction == null) (if (isBn) "নতুন লেনদেন যুক্ত করুন" else "Add New Transaction") else (if (isBn) "লেনদেন এডিট করুন" else "Edit Transaction"),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = themeColors.displayText
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val types = listOf(
                        "INCOME" to (if (isBn) "আয়" else "Income"),
                        "EXPENSE" to (if (isBn) "ব্যয়" else "Expense"),
                        "DEBT" to (if (isBn) "দেনা/পাওনা" else "Debt"),
                        "SAVINGS" to (if (isBn) "সঞ্চয়" else "Savings")
                    )
                    types.forEach { (tKey, label) ->
                        val selected = type == tKey
                        Surface(
                            onClick = {
                                type = tKey
                                category = categories.first()
                                if (tKey == "DEBT") subType = "TAKEN"
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) themeColors.buttonEqualBg else themeColors.background,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else themeColors.displayText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                if (type == "DEBT") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = { subType = "TAKEN" },
                            shape = RoundedCornerShape(8.dp),
                            color = if (subType == "TAKEN") Color(0xFFF59E0B) else themeColors.background,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isBn) "দেনা (কারো থেকে নিয়েছি)" else "Debt (Taken)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (subType == "TAKEN") Color.White else themeColors.displayText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                        Surface(
                            onClick = { subType = "GIVEN" },
                            shape = RoundedCornerShape(8.dp),
                            color = if (subType == "GIVEN") Color(0xFF8B5CF6) else themeColors.background,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isBn) "পাওনা (কাউকে দিয়েছি)" else "Loan (Given)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (subType == "GIVEN") Color.White else themeColors.displayText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBn) "বিবরণ / শিরোনাম" else "Title / Description") },
                    placeholder = { Text(if (isBn) "যেমন: মাসিক বেতন, বাজার খরচ..." else "e.g. Salary, Grocery...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = themeColors.cardBg,
                        unfocusedContainerColor = themeColors.cardBg
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
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = themeColors.cardBg,
                        unfocusedContainerColor = themeColors.cardBg
                    )
                )

                // Category Selection Chips
                Text(if (isBn) "ক্যাটাগরি:" else "Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(5).forEach { cat ->
                        val selected = category == cat
                        FilterChip(
                            selected = selected,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.buttonEqualBg,
                                selectedLabelColor = Color.White
                            )
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
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = themeColors.cardBg,
                        unfocusedContainerColor = themeColors.cardBg
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
                        id = initialTransaction?.id ?: 0,
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
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
            ) {
                Text(if (isBn) "সংরক্ষণ করুন" else "Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
            }
        },
        containerColor = themeColors.cardBg
    )
}

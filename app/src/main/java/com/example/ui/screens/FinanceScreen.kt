package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.graphics.Brush
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
    var selectedTransactionDetails by remember { mutableStateOf<FinanceTransaction?>(null) }

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

    // Soft Blurred Gradients - delicate pastel colors
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp)
                .bounceOverscroll(), // Bouncing overscroll behavior
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
        ) {
            // 1. Beautiful Modern Title Header Bar
            item {
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
                                .size(42.dp)
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
                                text = if (isBn) "আর্থিক বিবরণী" else "Financial Statement",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "মোট নেট স্থিতি: ${formatAmount(netBalance)}" else "Net Balance: ${formatAmount(netBalance)}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.displayText.copy(alpha = 0.65f)
                            )
                        }
                    }

                    if (transactions.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearAllConfirm = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(themeColors.displayText.copy(alpha = 0.05f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All",
                                tint = themeColors.displayText.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. Newly Designed 3 Cards with Soft Blurred Gradients
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Card 1: Income & Expense (Full Width)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(incExpBg)
                                .border(
                                    width = 1.dp,
                                    color = themeColors.displayText.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isBn) "আয় ও ব্যয় বিবরণী" else "Income & Expense Ledger",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Income Column
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF22C55E).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                                contentDescription = null,
                                                tint = Color(0xFF16A34A),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (isBn) "মোট আয়" else "Total Income",
                                                fontSize = 10.5.sp,
                                                color = themeColors.displayText.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = formatAmount(totalIncome),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF16A34A)
                                            )
                                        }
                                    }

                                    // Divider Line
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(30.dp)
                                            .background(themeColors.displayText.copy(alpha = 0.12f))
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Expense Column
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                                contentDescription = null,
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (isBn) "মোট ব্যয়" else "Total Expense",
                                                fontSize = 10.5.sp,
                                                color = themeColors.displayText.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = formatAmount(totalExpense),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFDC2626)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card 2 & 3: Side-by-Side (Debt & Loan, Savings Balance)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 2: Debt & Loan
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(debtLoanBg)
                                    .border(
                                        width = 1.dp,
                                        color = themeColors.displayText.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (isBn) "দেনা ও পাওনা হিসাব" else "Debt & Loan Status",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Debt Rows
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CallReceived,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = if (isBn) "দেনা (দেবেন)" else "Debt",
                                                    fontSize = 9.sp,
                                                    color = themeColors.displayText.copy(alpha = 0.55f),
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = formatAmount(debtTaken),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFD97706),
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CallMade,
                                                    contentDescription = null,
                                                    tint = Color(0xFF7C3AED),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = if (isBn) "পাওনা (পাবেন)" else "Loan",
                                                    fontSize = 9.sp,
                                                    color = themeColors.displayText.copy(alpha = 0.55f),
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = formatAmount(loanGiven),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF7C3AED),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Card 3: Savings Balance with Bank Icon
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(savingsBg)
                                    .border(
                                        width = 1.dp,
                                        color = themeColors.displayText.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (isBn) "মোট সঞ্চয়" else "Total Savings",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Replaced Piggy icon with Bank icon
                                            Icon(
                                                imageVector = Icons.Default.AccountBalance,
                                                contentDescription = null,
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (isBn) "সঞ্চয় ব্যালেন্স" else "Savings Balance",
                                                fontSize = 9.sp,
                                                color = themeColors.displayText.copy(alpha = 0.55f),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = formatAmount(totalSavings),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF2563EB),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isBn) "লেনদেন খুঁজুন (টাইটেল, ক্যাটাগরি, নোট)..." else "Search transactions...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.5.sp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = themeColors.cardBg,
                        unfocusedContainerColor = themeColors.cardBg,
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.15f),
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText
                    )
                )
            }

            // 4. Horizontal Filter Chips
            item {
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
            }

            // 5. Scrollable list of Transactions
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 50.dp),
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
                                text = if (isBn) "নিচের + বাটন চেপে নতুন আদান-প্রদান যোগ করুন" else "Tap the + button below to record income, expense, or loans",
                                fontSize = 11.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.45f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    TransactionCard(
                        item = item,
                        isBn = isBn,
                        themeColors = themeColors,
                        formatAmount = { formatAmount(it) },
                        onToggleSettled = {
                            viewModel.updateFinanceTransaction(item.copy(isSettled = !item.isSettled))
                        },
                        onClick = {
                            selectedTransactionDetails = item
                        },
                        onDelete = {
                            showDeleteConfirmDialog = item
                        }
                    )
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

    // Modern Detail Dialog with details, share, edit, delete
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
                    // Large Amount display
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

                    // Details Rows
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
                    // Share Button
                    Button(
                        onClick = {
                            try {
                                val shareText = if (isBn) {
                                    "📊 লেনদেন বিবরণী:\n📌 শিরোনাম: ${item.title}\n📈 ধরণ: $typeLabel\n🏷️ ক্যাটাগরি: ${item.category}\n💰 পরিমাণ: ${formatAmount(item.amount)}\n🗓️ সময়: $formattedTime\n📝 নোট: ${item.note}"
                                } else {
                                    "📊 Transaction Details:\n📌 Title: ${item.title}\n📈 Type: $typeLabel\n🏷️ Category: ${item.category}\n💰 Amount: ${formatAmount(item.amount)}\n🗓️ Time: $formattedTime\n📝 Note: ${item.note}"
                                }
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, if (isBn) "শেয়ার করুন" else "Share via"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "শেয়ার" else "Share", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Edit Button
                    Button(
                        onClick = {
                            editingTransaction = item
                            showAddDialog = true
                            selectedTransactionDetails = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "এডিট" else "Edit", color = themeColors.buttonEqualBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog != null) {
        val target = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text(if (isBn) "লেনদেন মুছে ফেলবেন?" else "Delete Transaction?", color = themeColors.displayText, fontWeight = FontWeight.Bold) },
            text = { Text(if (isBn) "\"${target.title}\" লেনদেনটি তালিকা থেকে স্থায়ীভাবে মুছে ফেলা হবে।" else "Are you sure you want to delete \"${target.title}\"?", color = themeColors.displayText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFinanceTransaction(target.id)
                        if (selectedTransactionDetails?.id == target.id) {
                            selectedTransactionDetails = null
                        }
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBn) "মুছে ফেলুন" else "Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Clear All Confirmation Dialog
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text(if (isBn) "সব লেনদেন মুছে ফেলবেন?" else "Clear All Transactions?", color = themeColors.displayText, fontWeight = FontWeight.Bold) },
            text = { Text(if (isBn) "আপনার সমস্ত ফিন্যান্সিয়াল লেনদেনের তথ্য ডিলিট হয়ে যাবে।" else "This will delete all saved income, expense, and loan records.", color = themeColors.displayText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllFinanceTransactions()
                        selectedTransactionDetails = null
                        showClearAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBn) "সব মুছুন" else "Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
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

@Composable
fun TransactionCard(
    item: FinanceTransaction,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    formatAmount: (Double) -> String,
    onToggleSettled: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedTime = sdf.format(Date(item.timestamp))

    val (typeLabel, badgeBg, badgeTextColor, typeIcon) = remember(item) {
        when (item.type) {
            "INCOME" -> Quadruple(if (isBn) "আয়" else "Income", Color(0xFF22C55E).copy(alpha = 0.15f), Color(0xFF16A34A), Icons.AutoMirrored.Filled.TrendingUp)
            "EXPENSE" -> Quadruple(if (isBn) "ব্যয়" else "Expense", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFDC2626), Icons.AutoMirrored.Filled.TrendingDown)
            "SAVINGS" -> Quadruple(if (isBn) "সঞ্চয়" else "Savings", Color(0xFF3B82F6).copy(alpha = 0.15f), Color(0xFF2563EB), Icons.Default.AccountBalance) // Bank icon
            else -> { // DEBT
                if (item.subType == "TAKEN") {
                    Quadruple(if (isBn) "দেনা" else "Debt", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFD97706), Icons.Default.CallReceived)
                } else {
                    Quadruple(if (isBn) "পাওনা" else "Loan", Color(0xFF8B5CF6).copy(alpha = 0.15f), Color(0xFF7C3AED), Icons.Default.CallMade)
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
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
                            onClick = { onToggleSettled() },
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

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
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

    // Adjust category selection if type changes and current category is not in list
    LaunchedEffect(type) {
        if (!categories.contains(category)) {
            category = categories.first()
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
                        "DEBT" to (if (isBn) "দেনা/পাওনা" else "Debt"),
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
                                fontSize = 11.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else themeColors.displayText.copy(alpha = 0.8f)
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

                // Title Input
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
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f)
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
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f)
                    )
                )

                // Category Selection Chips Scrollable LazyRow
                Text(
                    text = if (isBn) "ক্যাটাগরি" else "Category",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                androidx.compose.foundation.lazy.LazyRow(
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
                        unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f)
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

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.HistoryEntry
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class MarketItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var quantity: Double = 1.0,
    var unit: String = "কেজি",
    var unitPrice: Double = 0.0,
    var isChecked: Boolean = false
) {
    val total: Double get() = quantity * unitPrice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketListCard(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier
) {
    MarketListScreen(viewModel, themeColors, modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketListScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var listTitle by remember { mutableStateOf(if (isBn) "আজকের বাজার ফর্দ" else "Market Expense List") }
    val items = remember { mutableStateListOf<MarketItem>() }

    // Dialog state
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MarketItem?>(null) }
    var showSaveMemoDialog by remember { mutableStateOf(false) }

    // Form fields for Add/Edit
    var itemName by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }
    var itemUnitPrice by remember { mutableStateOf("") }
    var itemUnit by remember { mutableStateOf(if (isBn) "কেজি" else "kg") }

    val unitsList = if (isBn) {
        listOf("কেজি", "গ্রাম", "লিটার", "পিস", "প্যাকেট", "বক্স", "ডজন")
    } else {
        listOf("kg", "gm", "liter", "pcs", "pkt", "box", "doz")
    }

    val grandTotal = remember(items.toList()) { items.sumOf { it.total } }
    val checkedTotal = remember(items.toList()) { items.filter { it.isChecked }.sumOf { it.total } }

    fun openAddDialog(itemToEdit: MarketItem? = null) {
        editingItem = itemToEdit
        if (itemToEdit != null) {
            itemName = itemToEdit.name
            itemQty = if (itemToEdit.quantity % 1.0 == 0.0) itemToEdit.quantity.toInt().toString() else itemToEdit.quantity.toString()
            itemUnitPrice = if (itemToEdit.unitPrice % 1.0 == 0.0) itemToEdit.unitPrice.toInt().toString() else itemToEdit.unitPrice.toString()
            itemUnit = itemToEdit.unit
        } else {
            itemName = ""
            itemQty = "1"
            itemUnitPrice = ""
            itemUnit = if (isBn) "কেজি" else "kg"
        }
        showAddItemDialog = true
    }

    fun saveItem() {
        if (itemName.isBlank()) return
        val qty = itemQty.toDoubleOrNull() ?: 1.0
        val price = itemUnitPrice.toDoubleOrNull() ?: 0.0

        if (editingItem != null) {
            val index = items.indexOfFirst { it.id == editingItem!!.id }
            if (index != -1) {
                items[index] = items[index].copy(
                    name = itemName.trim(),
                    quantity = qty,
                    unit = itemUnit,
                    unitPrice = price
                )
            }
        } else {
            items.add(
                MarketItem(
                    name = itemName.trim(),
                    quantity = qty,
                    unit = itemUnit,
                    unitPrice = price
                )
            )
        }
        showAddItemDialog = false
    }

    fun exportToPdf(context: Context, title: String, itemList: List<MarketItem>) {
        if (itemList.isEmpty()) {
            Toast.makeText(context, if (isBn) "তালিকা খালি!" else "List is empty!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val textPaint = Paint().apply { isAntiAlias = true }

            // Title
            paint.color = AndroidColor.parseColor("#1E293B")
            paint.textSize = 22f
            paint.isFakeBoldText = true
            canvas.drawText(title, 40f, 60f, paint)

            // Date
            textPaint.color = AndroidColor.parseColor("#64748B")
            textPaint.textSize = 12f
            val dateStr = SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale.getDefault()).format(Date())
            canvas.drawText("তারিখ / Date: $dateStr", 40f, 82f, textPaint)

            // Table Header Background
            paint.color = AndroidColor.parseColor("#F1F5F9")
            canvas.drawRect(40f, 105f, 555f, 135f, paint)

            // Table Header Text
            paint.color = AndroidColor.parseColor("#0F172A")
            paint.textSize = 12f
            paint.isFakeBoldText = true

            canvas.drawText("নং", 50f, 125f, paint)
            canvas.drawText("পণ্যের নাম / Item", 90f, 125f, paint)
            canvas.drawText("পরিমাণ / Qty", 290f, 125f, paint)
            canvas.drawText("দর / Rate", 390f, 125f, paint)
            canvas.drawText("মোট / Total", 470f, 125f, paint)

            // Line
            paint.strokeWidth = 1f
            paint.color = AndroidColor.parseColor("#CBD5E1")
            canvas.drawLine(40f, 135f, 555f, 135f, paint)

            // Table Rows
            var yPos = 160f
            var totalCost = 0.0

            textPaint.color = AndroidColor.parseColor("#334155")
            textPaint.textSize = 11f

            itemList.forEachIndexed { idx, item ->
                if (yPos > 780f) return@forEachIndexed // Simple single page cutoff for demo
                totalCost += item.total

                canvas.drawText("${idx + 1}", 50f, yPos, textPaint)

                val displayName = if (item.name.length > 22) item.name.take(20) + ".." else item.name
                canvas.drawText(displayName, 90f, yPos, textPaint)

                val qtyStr = if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()} ${item.unit}" else "${item.quantity} ${item.unit}"
                canvas.drawText(qtyStr, 290f, yPos, textPaint)

                canvas.drawText("৳${String.format("%.1f", item.unitPrice)}", 390f, yPos, textPaint)
                canvas.drawText("৳${String.format("%.1f", item.total)}", 470f, yPos, textPaint)

                yPos += 26f
                canvas.drawLine(40f, yPos - 10f, 555f, yPos - 10f, paint)
            }

            // Summary Total
            yPos += 15f
            paint.color = AndroidColor.parseColor("#2563EB")
            paint.textSize = 15f
            paint.isFakeBoldText = true
            canvas.drawText("সর্বমোট হিসাব / Grand Total: ৳${String.format("%.2f", totalCost)}", 220f, yPos, paint)

            pdfDocument.finishPage(page)

            // Save file
            val pdfDir = File(context.cacheDir, "pdf_exports").apply { mkdirs() }
            val file = File(pdfDir, "Market_List_${System.currentTimeMillis()}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            // Share / Open PDF
            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, if (isBn) "বাজার ফর্দ PDF শেয়ার করুন" else "Share Market List PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF তৈরি ব্যর্থ হয়েছে: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = themeColors.background,
        topBar = {
            Surface(
                color = themeColors.cardBg,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(themeColors.buttonEqualBg.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "বাজার ফর্দ ও হিসাব" else "Market List & Tracker",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                                Text(
                                    text = if (isBn) "পণ্য যোগ করুন, হিসাব করুন ও PDF সেভ করুন" else "Add items, calculate total & export PDF",
                                    fontSize = 11.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { exportToPdf(context, listTitle, items) }) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "Export PDF",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                            IconButton(onClick = { showSaveMemoDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Save Memo",
                                    tint = themeColors.buttonEqualBg
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title Text Field
                    OutlinedTextField(
                        value = listTitle,
                        onValueChange = { listTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            fontSize = 15.sp
                        ),
                        placeholder = { Text(if (isBn) "ফর্দ এর নাম (যেমন: সাপ্তাহিক বাজার)" else "List Title", color = themeColors.displayText.copy(alpha = 0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddDialog(null) },
                containerColor = themeColors.buttonEqualBg,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp)
        ) {
            // Total Summary Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isBn) "সর্বমোট হিসাব" else "Grand Total",
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "৳ ${String.format("%.2f", grandTotal)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isBn) "কেনাকাটা সম্পূর্ণ (${items.count { it.isChecked }}/${items.size})" else "Checked Items (${items.count { it.isChecked }}/${items.size})",
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "৳ ${String.format("%.2f", checkedTotal)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Items List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            tint = themeColors.displayText.copy(alpha = 0.3f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "আপনার ফর্দে কোন পণ্য নেই!\n+ বাটনে ক্লিক করে পণ্য যোগ করুন।" else "No items in your market list!\nClick + button to add products.",
                            fontSize = 14.sp,
                            color = themeColors.displayText.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isChecked) themeColors.cardBg.copy(alpha = 0.5f) else themeColors.cardBg
                            ),
                            elevation = CardDefaults.cardElevation(if (item.isChecked) 0.dp else 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { isChecked ->
                                        items[index] = item.copy(isChecked = isChecked)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = themeColors.buttonEqualBg)
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { openAddDialog(item) }
                                ) {
                                    Text(
                                        text = item.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.isChecked) themeColors.displayText.copy(alpha = 0.5f) else themeColors.displayText,
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${if (item.quantity % 1.0 == 0.0) item.quantity.toInt() else item.quantity} ${item.unit}  ×  ৳${item.unitPrice}",
                                        fontSize = 12.sp,
                                        color = themeColors.displayText.copy(alpha = 0.6f)
                                    )
                                }

                                Text(
                                    text = "৳ ${String.format("%.1f", item.total)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isChecked) themeColors.displayText.copy(alpha = 0.5f) else themeColors.buttonEqualBg
                                )

                                IconButton(
                                    onClick = { items.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Item Dialog
    if (showAddItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = {
                Text(
                    text = if (editingItem != null) (if (isBn) "পণ্য সংশোধন করুন" else "Edit Item") else (if (isBn) "নতুন পণ্য যোগ করুন" else "Add New Item"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.displayText
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text(if (isBn) "পণ্যের নাম" else "Item Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColors.buttonEqualBg)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = itemQty,
                            onValueChange = { itemQty = it },
                            label = { Text(if (isBn) "পরিমাণ" else "Quantity") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColors.buttonEqualBg)
                        )

                        // Unit Selector
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = itemUnit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isBn) "একক" else "Unit") },
                                trailingIcon = {
                                    IconButton(onClick = { dropdownExpanded = true }) {
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColors.buttonEqualBg)
                            )
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                unitsList.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = {
                                            itemUnit = unit
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = itemUnitPrice,
                        onValueChange = { itemUnitPrice = it },
                        label = { Text(if (isBn) "প্রতি এককের দাম (৳)" else "Unit Price (৳)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColors.buttonEqualBg)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { saveItem() },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(if (isBn) "সংরক্ষণ করুন" else "Save Item", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Save Memo Dialog
    if (showSaveMemoDialog) {
        AlertDialog(
            onDismissRequest = { showSaveMemoDialog = false },
            title = {
                Text(
                    text = if (isBn) "হিস্টোরিতে মেমো সেভ করুন" else "Save Memo to History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.displayText
                )
            },
            text = {
                Text(
                    text = if (isBn) "আপনি কি এই বাজার ফর্দটি (মোট ৳${String.format("%.2f", grandTotal)}) অ্যাপ হিস্টোরিতে সেভ করতে চান?"
                    else "Do you want to save this market list (Total ৳${String.format("%.2f", grandTotal)}) to app history?",
                    fontSize = 14.sp,
                    color = themeColors.displayText.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val formattedSummary = items.joinToString("\n") {
                            "${it.name}: ${if (it.quantity % 1.0 == 0.0) it.quantity.toInt() else it.quantity} ${it.unit} × ৳${it.unitPrice} = ৳${it.total}"
                        }
                        viewModel.saveCustomHistory(
                            expression = listTitle,
                            result = "৳ ${String.format("%.2f", grandTotal)}",
                            type = "Market List",
                            customName = "$listTitle (${items.size} items)"
                        )
                        showSaveMemoDialog = false
                        Toast.makeText(context, if (isBn) "মেমো সেভ করা হয়েছে!" else "Memo saved to history!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(if (isBn) "হ্যাঁ, সেভ করুন" else "Save Memo", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveMemoDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

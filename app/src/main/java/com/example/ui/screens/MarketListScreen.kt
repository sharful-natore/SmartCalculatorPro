package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import com.example.util.bounceOverscroll
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

// --- Data Models ---

data class MarketItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var unitPrice: Double = 0.0,
    var quantity: Double = 1.0,
    var unit: String = "কেজি",
    var isChecked: Boolean = false
) {
    val total: Double get() = quantity * unitPrice

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("unitPrice", unitPrice)
            put("quantity", quantity)
            put("unit", unit)
            put("isChecked", isChecked)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): MarketItem {
            return MarketItem(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", ""),
                unitPrice = json.optDouble("unitPrice", 0.0),
                quantity = json.optDouble("quantity", 1.0),
                unit = json.optString("unit", "কেজি"),
                isChecked = json.optBoolean("isChecked", false)
            )
        }
    }
}

data class MarketPlanList(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<MarketItem> = emptyList(),
    val isCompleted: Boolean = false
) {
    val grandTotal: Double get() = items.sumOf { it.total }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("timestamp", timestamp)
        json.put("isCompleted", isCompleted)
        val arr = JSONArray()
        items.forEach { arr.put(it.toJson()) }
        json.put("items", arr)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): MarketPlanList {
            val itemsList = mutableListOf<MarketItem>()
            val arr = json.optJSONArray("items")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    itemsList.add(MarketItem.fromJson(arr.getJSONObject(i)))
                }
            }
            return MarketPlanList(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "বাজার ফর্দ"),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                items = itemsList,
                isCompleted = json.optBoolean("isCompleted", false)
            )
        }
    }
}

data class CompletedBazaarMemo(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<MarketItem> = emptyList(),
    val totalCost: Double = 0.0,
    val note: String = ""
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("timestamp", timestamp)
        json.put("totalCost", totalCost)
        json.put("note", note)
        val arr = JSONArray()
        items.forEach { arr.put(it.toJson()) }
        json.put("items", arr)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): CompletedBazaarMemo {
            val itemsList = mutableListOf<MarketItem>()
            val arr = json.optJSONArray("items")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    itemsList.add(MarketItem.fromJson(arr.getJSONObject(i)))
                }
            }
            return CompletedBazaarMemo(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "সম্পন্ন বাজার মেমো"),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                items = itemsList,
                totalCost = json.optDouble("totalCost", 0.0),
                note = json.optString("note", "")
            )
        }
    }
}

// Local Storage Manager for Market Lists
object MarketStorageManager {
    private const val PREF_NAME = "market_list_prefs"
    private const val KEY_PLAN_LISTS = "saved_plan_lists"
    private const val KEY_COMPLETED_MEMOS = "completed_memos"

    fun loadPlanLists(context: Context): List<MarketPlanList> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_PLAN_LISTS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<MarketPlanList>()
            for (i in 0 until arr.length()) {
                list.add(MarketPlanList.fromJson(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePlanLists(context: Context, lists: List<MarketPlanList>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        lists.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_PLAN_LISTS, arr.toString()).apply()
    }

    fun loadCompletedMemos(context: Context): List<CompletedBazaarMemo> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_COMPLETED_MEMOS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<CompletedBazaarMemo>()
            for (i in 0 until arr.length()) {
                list.add(CompletedBazaarMemo.fromJson(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCompletedMemos(context: Context, memos: List<CompletedBazaarMemo>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        memos.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_COMPLETED_MEMOS, arr.toString()).apply()
    }

    fun saveDraftPlan(context: Context, plan: MarketPlanList?) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (plan == null || (plan.items.isEmpty() && plan.title.isBlank())) {
            prefs.edit().remove("draft_plan").apply()
        } else {
            prefs.edit().putString("draft_plan", plan.toJson().toString()).apply()
        }
    }

    fun loadDraftPlan(context: Context): MarketPlanList? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("draft_plan", null) ?: return null
        return try {
            val plan = MarketPlanList.fromJson(JSONObject(raw))
            if (plan.items.any { it.name.isNotBlank() } || plan.title.isNotBlank()) plan else null
        } catch (e: Exception) {
            null
        }
    }

    fun clearDraftPlan(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("draft_plan").apply()
    }
}

// Top-level Composable Wrappers
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

    // Main Tab State: 0 -> ফর্দ (Shopping Lists), 1 -> হিস্টোরি (History)
    var selectedMainTab by remember { mutableStateOf(0) }

    // Persistent Lists
    val planLists = remember { mutableStateListOf<MarketPlanList>() }
    val completedMemos = remember { mutableStateListOf<CompletedBazaarMemo>() }

    LaunchedEffect(Unit) {
        planLists.clear()
        planLists.addAll(MarketStorageManager.loadPlanLists(context))
        completedMemos.clear()
        completedMemos.addAll(MarketStorageManager.loadCompletedMemos(context))
    }

    fun persistPlans() {
        MarketStorageManager.savePlanLists(context, planLists.toList())
    }

    fun persistMemos() {
        MarketStorageManager.saveCompletedMemos(context, completedMemos.toList())
    }

    // Modal / Dialog States
    var showCreateOrBazaarDialog by remember { mutableStateOf(false) }
    var initialDialogTab by remember { mutableStateOf(0) } // 0: ফর্দ তৈরি, 1: বাজার সম্পন্ন

    // Interactive Shopping Execution Dialog for a selected Plan List
    var activeExecutingPlan by remember { mutableStateOf<MarketPlanList?>(null) }
    var editingPlan by remember { mutableStateOf<MarketPlanList?>(null) }

    // Viewing or Editing a specific Completed Memo
    var viewingMemo by remember { mutableStateOf<CompletedBazaarMemo?>(null) }
    var editingMemo by remember { mutableStateOf<CompletedBazaarMemo?>(null) }

    // Deletion confirmation state for Plan or Memo
    var itemToDeletePrompt by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    // PDF Export helper with SAF launcher
    var pendingMemoToExport by remember { mutableStateOf<CompletedBazaarMemo?>(null) }

    var isHeaderVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): Offset {
                if (available.y < -12f && isHeaderVisible) {
                    isHeaderVisible = false
                } else if (available.y > 12f && !isHeaderVisible) {
                    isHeaderVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null && pendingMemoToExport != null) {
            exportMemoPdfToUri(context, uri, pendingMemoToExport!!, isBn)
            pendingMemoToExport = null
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        containerColor = themeColors.background,
        topBar = {
            Surface(
                color = themeColors.cardBg,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 8.dp)
                ) {
                    // Top 2 Chips: ফর্দ (Shopping Lists) & হিস্টোরি (History)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Chip 1: ফর্দ
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedMainTab = 0 },
                            color = if (selectedMainTab == 0) themeColors.buttonEqualBg else themeColors.background,
                            shape = RoundedCornerShape(12.dp),
                            border = if (selectedMainTab == 0) null else androidx.compose.foundation.BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatListNumbered,
                                    contentDescription = null,
                                    tint = if (selectedMainTab == 0) Color.White else themeColors.displayText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "ফর্দ (${planLists.size})" else "Lists (${planLists.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (selectedMainTab == 0) Color.White else themeColors.displayText
                                )
                            }
                        }

                        // Chip 2: হিস্টোরি
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedMainTab = 1 },
                            color = if (selectedMainTab == 1) themeColors.buttonEqualBg else themeColors.background,
                            shape = RoundedCornerShape(12.dp),
                            border = if (selectedMainTab == 1) null else androidx.compose.foundation.BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = if (selectedMainTab == 1) Color.White else themeColors.displayText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "হিস্টোরি (${completedMemos.size})" else "History (${completedMemos.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (selectedMainTab == 1) Color.White else themeColors.displayText
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
                    initialDialogTab = if (selectedMainTab == 0) 0 else 1
                    showCreateOrBazaarDialog = true
                },
                containerColor = themeColors.buttonEqualBg,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Market List or Complete Bazaar",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedMainTab,
                label = "MainTabsAnimation"
            ) { tab ->
                when (tab) {
                    0 -> {
                        // TAB 1: ফর্দ লিস্ট (Active Shopping Lists)
                        if (planLists.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Default.PlaylistAddCheck,
                                title = if (isBn) "কোন বাজার ফর্দ নেই" else "No Shopping Lists Found",
                                subtitle = if (isBn) "নিচের + বাটনে ক্লিক করে নতুন বাজার ফর্দ তৈরি করুন অথবা সরাসরি বাজার সম্পন্ন করুন।"
                                else "Tap the + button below to create a shopping list or do instant shopping.",
                                themeColors = themeColors
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(planLists, key = { it.id }) { plan ->
                                    MarketPlanCardItem(
                                        plan = plan,
                                        isBn = isBn,
                                        themeColors = themeColors,
                                        onClick = {
                                            activeExecutingPlan = plan
                                        },
                                        onEdit = {
                                            editingPlan = plan
                                        },
                                        onDelete = {
                                            itemToDeletePrompt = (if (isBn) "আপনি কি \"${plan.title}\" ফর্দটি মুছে ফেলতে চান?" else "Do you want to delete \"${plan.title}\"?") to {
                                                planLists.remove(plan)
                                                persistPlans()
                                                Toast.makeText(context, if (isBn) "ফর্দ মুছে ফেলা হয়েছে" else "List deleted", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(72.dp)) }
                            }
                        }
                    }
                    1 -> {
                        // TAB 2: হিস্টোরি (Completed Bazaars / Memos)
                        if (completedMemos.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Default.Receipt,
                                title = if (isBn) "কোন বাজার মেমো নেই" else "No Memo History Found",
                                subtitle = if (isBn) "বাজার সম্পন্ন করার পর এখানে প্রতিটি বাজারের পূর্ণাঙ্গ মেমো ও হিসাব সংরক্ষিত থাকবে।"
                                else "Completed bazaars will appear here as itemized shopping memos.",
                                themeColors = themeColors
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .bounceOverscroll(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(completedMemos, key = { it.id }) { memo ->
                                    CompletedMemoCardItem(
                                        memo = memo,
                                        isBn = isBn,
                                        themeColors = themeColors,
                                        onClick = {
                                            viewingMemo = memo
                                        },
                                        onEdit = {
                                            editingMemo = memo
                                        },
                                        onExportPdf = {
                                            pendingMemoToExport = memo
                                            val defaultName = getSanitizedMemoPdfFileName(memo)
                                            createDocumentLauncher.launch(defaultName)
                                        },
                                        onSharePdf = {
                                            shareMemoPdfDirect(context, memo, isBn)
                                        },
                                        onDelete = {
                                            itemToDeletePrompt = (if (isBn) "আপনি কি এই বাজার মেমোটি মুছে ফেলতে চান?" else "Do you want to delete this shopping memo?") to {
                                                completedMemos.remove(memo)
                                                persistMemos()
                                                Toast.makeText(context, if (isBn) "মেমো মুছে ফেলা হয়েছে" else "Memo deleted", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(72.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG 1: "+" Button Dialog with 2 Chips (ফর্দ তৈরি vs বাজার সম্পন্ন)
    if (showCreateOrBazaarDialog || editingPlan != null) {
        CreateOrBazaarModalDialog(
            initialTab = if (editingPlan != null) 0 else initialDialogTab,
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = {
                showCreateOrBazaarDialog = false
                editingPlan = null
            },
            onSavePlan = { newPlan ->
                MarketStorageManager.clearDraftPlan(context)
                if (editingPlan != null) {
                    val idx = planLists.indexOfFirst { it.id == editingPlan!!.id }
                    if (idx != -1) {
                        planLists[idx] = newPlan
                    } else {
                        planLists.add(0, newPlan)
                    }
                } else {
                    planLists.add(0, newPlan)
                }
                persistPlans()
                showCreateOrBazaarDialog = false
                editingPlan = null
                selectedMainTab = 0
                Toast.makeText(context, if (isBn) "ফর্দ সফলভাবে সেভ হয়েছে!" else "Shopping list saved!", Toast.LENGTH_SHORT).show()
            },
            onCompleteDirectBazaar = { newMemo ->
                MarketStorageManager.clearDraftPlan(context)
                completedMemos.add(0, newMemo)
                persistMemos()
                // Sync with general calculation history
                viewModel.saveCustomHistory(
                    expression = newMemo.title,
                    result = "৳ ${String.format(Locale.US, "%.2f", newMemo.totalCost)}",
                    type = "Market Memo",
                    customName = "${newMemo.title} (${newMemo.items.size} items)"
                )
                showCreateOrBazaarDialog = false
                editingPlan = null
                selectedMainTab = 1
                viewingMemo = newMemo
                Toast.makeText(context, if (isBn) "বাজার সম্পন্ন ও মেমো সংরক্ষিত হয়েছে!" else "Bazaar completed and memo saved!", Toast.LENGTH_SHORT).show()
            },
            editingPlan = editingPlan
        )
    }

    // DIALOG 2: Interactive Shopping / Bazaar Completion Dialog from a Selected Plan
    if (activeExecutingPlan != null) {
        ExecuteBazaarFromPlanDialog(
            plan = activeExecutingPlan!!,
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { activeExecutingPlan = null },
            onCompleteBazaar = { completedMemo, deleteOriginalPlan ->
                MarketStorageManager.clearDraftPlan(context)
                completedMemos.add(0, completedMemo)
                persistMemos()
                if (deleteOriginalPlan) {
                    planLists.removeAll { it.id == activeExecutingPlan!!.id }
                    persistPlans()
                }
                viewModel.saveCustomHistory(
                    expression = completedMemo.title,
                    result = "৳ ${String.format(Locale.US, "%.2f", completedMemo.totalCost)}",
                    type = "Market Memo",
                    customName = "${completedMemo.title} (${completedMemo.items.size} items)"
                )
                activeExecutingPlan = null
                selectedMainTab = 1
                viewingMemo = completedMemo
                Toast.makeText(context, if (isBn) "বাজার সম্পন্ন ও মেমো তৈরি হয়েছে!" else "Bazaar completed & memo generated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // DIALOG 3: View Memo Receipt Dialog
    if (viewingMemo != null) {
        MemoDetailDialog(
            memo = viewingMemo!!,
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { viewingMemo = null },
            onEditMemo = { memoToEdit ->
                viewingMemo = null
                editingMemo = memoToEdit
            },
            onExportSafPdf = { memo ->
                pendingMemoToExport = memo
                val defaultName = getSanitizedMemoPdfFileName(memo)
                createDocumentLauncher.launch(defaultName)
            },
            onSharePdf = { memo ->
                shareMemoPdfDirect(context, memo, isBn)
            }
        )
    }

    // DIALOG 4: Edit Completed Bazaar Memo Dialog
    if (editingMemo != null) {
        EditCompletedMemoDialog(
            memo = editingMemo!!,
            isBn = isBn,
            themeColors = themeColors,
            onDismiss = { editingMemo = null },
            onSaveEditedMemo = { updatedMemo ->
                val idx = completedMemos.indexOfFirst { it.id == updatedMemo.id }
                if (idx != -1) {
                    completedMemos[idx] = updatedMemo
                } else {
                    completedMemos.add(0, updatedMemo)
                }
                persistMemos()
                viewModel.saveCustomHistory(
                    expression = updatedMemo.title,
                    result = "৳ ${String.format(Locale.US, "%.2f", updatedMemo.totalCost)}",
                    type = "Market Memo",
                    customName = "${updatedMemo.title} (${updatedMemo.items.size} items)"
                )
                editingMemo = null
                viewingMemo = updatedMemo
                Toast.makeText(context, if (isBn) "মেমো সফলভাবে আপডেট হয়েছে!" else "Memo updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Confirmation Dialog for item deletion
    if (itemToDeletePrompt != null) {
        AlertDialog(
            onDismissRequest = { itemToDeletePrompt = null },
            title = {
                Text(
                    text = if (isBn) "ডিলিট নিশ্চিতকরণ" else "Confirm Deletion",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.displayText
                )
            },
            text = {
                Text(
                    text = itemToDeletePrompt!!.first,
                    fontSize = 14.sp,
                    color = themeColors.displayText.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val action = itemToDeletePrompt!!.second
                        itemToDeletePrompt = null
                        action()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(text = if (isBn) "ডিলিট করুন" else "Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDeletePrompt = null }) {
                    Text(text = if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// --- Card Item for Plan Lists ---
@Composable
fun MarketPlanCardItem(
    plan: MarketPlanList,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, yyyy - hh:mm a", if (isBn) Locale("bn") else Locale.ENGLISH).format(Date(plan.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = plan.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = dateStr,
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.55f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Plan",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Plan",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.08f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Items preview chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = themeColors.background,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isBn) "${plan.items.size} টি পণ্য" else "${plan.items.size} Items",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isBn) "আনুমানিক মোট" else "Estimated Total",
                        fontSize = 10.sp,
                        color = themeColors.displayText.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "৳ ${String.format(Locale.US, "%.2f", plan.grandTotal)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action button to start shopping
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg.copy(alpha = 0.12f)),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCartCheckout,
                    contentDescription = null,
                    tint = themeColors.buttonEqualBg,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBn) "বাজার সম্পন্ন করুন (চেকলিস্ট)" else "Complete Shopping (Checklist)",
                    color = themeColors.buttonEqualBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// --- Card Item for Completed History Memos ---
@Composable
fun CompletedMemoCardItem(
    memo: CompletedBazaarMemo,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onExportPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, yyyy - hh:mm a", if (isBn) Locale("bn") else Locale.ENGLISH).format(Date(memo.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = memo.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = dateStr,
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.55f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Memo",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Memo",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.08f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "কেনা হয়েছে" else "Purchased",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (isBn) "${memo.items.size} টি পণ্য" else "${memo.items.size} Items",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.displayText
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isBn) "সর্বমোট খরচ" else "Total Spent",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "৳ ${String.format(Locale.US, "%.2f", memo.totalCost)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (View Memo, Edit Memo, Save to Folder, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "মেমো" else "View", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "এডিট" else "Edit", color = themeColors.buttonEqualBg, fontSize = 12.sp)
                }

                Button(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "PDF সেভ" else "Save PDF", color = Color.White, fontSize = 12.sp)
                }

                IconButton(
                    onClick = onSharePdf,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = themeColors.buttonEqualBg)
                }
            }
        }
    }
}

// --- DIALOG 1: Create Plan List OR Direct Shopping Modal ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrBazaarModalDialog(
    initialTab: Int,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onSavePlan: (MarketPlanList) -> Unit,
    onCompleteDirectBazaar: (CompletedBazaarMemo) -> Unit,
    editingPlan: MarketPlanList? = null
) {
    val context = LocalContext.current
    var hasDraft by remember { mutableStateOf(if (editingPlan == null) MarketStorageManager.loadDraftPlan(context) != null else false) }
    val savedDraft = remember { if (editingPlan == null) MarketStorageManager.loadDraftPlan(context) else null }
    // 0 -> ফর্দ তৈরি, 1 -> বাজার সম্পন্ন
    var dialogTab by remember { mutableStateOf(if (editingPlan != null) 0 else initialTab) }
    var listTitle by remember {
        mutableStateOf(
            editingPlan?.title ?: savedDraft?.title ?: (
                if (isBn) "বাজারের ফর্দ - " + SimpleDateFormat("dd MMM", Locale("bn")).format(Date())
                else "Market List - " + SimpleDateFormat("dd MMM", Locale.US).format(Date())
            )
        )
    }

    // Editable Items List
    val items = remember {
        val list = mutableStateListOf<MarketItem>()
        if (editingPlan != null) {
            list.addAll(editingPlan.items.map { it.copy() })
        } else if (savedDraft != null && savedDraft.items.isNotEmpty()) {
            list.addAll(savedDraft.items.map { it.copy() })
        } else {
            list.add(MarketItem(name = "", unitPrice = 0.0, quantity = 1.0, unit = if (isBn) "কেজি" else "kg", isChecked = true))
        }
        list
    }

    var isSubmitted by remember { mutableStateOf(false) }

    // Auto-save draft on change unless submitted
    LaunchedEffect(listTitle, items.map { "${it.name}_${it.unitPrice}_${it.quantity}_${it.unit}_${it.isChecked}" }) {
        if (editingPlan == null && !isSubmitted) {
            val valid = items.any { it.name.isNotBlank() || it.unitPrice > 0 }
            if (valid || listTitle.isNotBlank()) {
                MarketStorageManager.saveDraftPlan(context, MarketPlanList(title = listTitle, items = items.toList()))
                hasDraft = true
            }
        }
    }

    // Auto-save and close on Back Press
    BackHandler {
        if (editingPlan == null && !isSubmitted) {
            val valid = items.filter { it.name.isNotBlank() }
            if (valid.isNotEmpty()) {
                MarketStorageManager.saveDraftPlan(context, MarketPlanList(title = listTitle, items = items.toList()))
            }
        }
        onDismiss()
    }

    // Voice recognition targeting
    var voiceTargetItemIndex by remember { mutableStateOf<Int?>(null) }
    var isVoiceTargetTitle by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                if (isVoiceTargetTitle) {
                    listTitle = spokenText.trim()
                } else {
                    val idx = voiceTargetItemIndex
                    if (idx != null && idx in items.indices) {
                        val current = items[idx]
                        val updated = parseBazaarVoiceText(spokenText, current, isBn)
                        items[idx] = updated
                    }
                }
            }
        }
    }

    val launchVoiceFor: (Boolean, Int?) -> Unit = { isTitle, idx ->
        try {
            isVoiceTargetTitle = isTitle
            voiceTargetItemIndex = idx
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBn) "bn-BD" else "en-US")
                putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    if (isTitle) (if (isBn) "ফর্দের নাম বলুন..." else "Speak List Name...")
                    else (if (isBn) "পণ্যের নাম বা বিবরণ বলুন (যেমন: আলু ৫০ টাকা ২ কেজি)..." else "Speak Item Name / Price (e.g. Potato 50 tk 2 kg)...")
                )
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, if (isBn) "ভয়েস ইনপুট সমর্থিত নয়" else "Voice input not supported", Toast.LENGTH_SHORT).show()
        }
    }

    // Item row deletion confirmation
    var itemIndexToDelete by remember { mutableStateOf<Int?>(null) }
    var validationErrorMsg by remember { mutableStateOf<String?>(null) }

    val unitsList = if (isBn) {
        listOf("কেজি", "গ্রাম", "লিটার", "পিস", "প্যাকেট", "বক্স", "ডজন", "হালি", "বস্তা")
    } else {
        listOf("kg", "gm", "liter", "pcs", "pkt", "box", "doz", "bag")
    }

    // Calculation for Total
    val calculatedGrandTotal by remember {
        derivedStateOf {
            if (dialogTab == 0) {
                items.sumOf { it.total }
            } else {
                items.filter { it.isChecked }.sumOf { it.total }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.96f),
                shape = RoundedCornerShape(20.dp),
                color = themeColors.background,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Title and Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (dialogTab == 0) (if (isBn) "নতুন ফর্দ তৈরি" else "Create Shopping List")
                                else (if (isBn) "সরাসরি বাজার সম্পন্ন" else "Instant Bazaar Shopping"),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            if (hasDraft && editingPlan == null) {
                                Text(
                                    text = if (isBn) "অসমাপ্ত খসড়া লোড করা হয়েছে" else "Saved draft loaded",
                                    fontSize = 10.sp,
                                    color = themeColors.buttonEqualBg
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (hasDraft && editingPlan == null) {
                                TextButton(
                                    onClick = {
                                        MarketStorageManager.clearDraftPlan(context)
                                        hasDraft = false
                                        items.clear()
                                        items.add(MarketItem(name = "", unitPrice = 0.0, quantity = 1.0, unit = if (isBn) "কেজি" else "kg", isChecked = (dialogTab == 1)))
                                        listTitle = if (isBn) "বাজারের ফর্দ - " + SimpleDateFormat("dd MMM", Locale("bn")).format(Date())
                                        else "Market List - " + SimpleDateFormat("dd MMM", Locale.US).format(Date())
                                        Toast.makeText(context, if (isBn) "খসড়া মুছে ফেলা হয়েছে" else "Draft reset", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isBn) "খসড়া রিসেট" else "Reset Draft",
                                        fontSize = 11.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Top 2 Dialog Chips: "ফর্দ তৈরি" and "বাজার সম্পন্ন"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    dialogTab = 0
                                    validationErrorMsg = null
                                },
                            color = if (dialogTab == 0) themeColors.buttonEqualBg else themeColors.cardBg,
                            border = if (dialogTab == 0) null else androidx.compose.foundation.BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistAdd,
                                    contentDescription = null,
                                    tint = if (dialogTab == 0) Color.White else themeColors.displayText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "ফর্দ তৈরি" else "Create List",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (dialogTab == 0) Color.White else themeColors.displayText
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    dialogTab = 1
                                    validationErrorMsg = null
                                },
                            color = if (dialogTab == 1) Color(0xFF10B981) else themeColors.cardBg,
                            border = if (dialogTab == 1) null else androidx.compose.foundation.BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = if (dialogTab == 1) Color.White else themeColors.displayText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "বাজার সম্পন্ন" else "Finish Bazaar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (dialogTab == 1) Color.White else themeColors.displayText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // List / Memo Title Field with Voice Input Button
                    OutlinedTextField(
                        value = listTitle,
                        onValueChange = { listTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(if (isBn) "ফর্দ / মেমো নাম" else "List / Memo Title", fontSize = 11.sp) },
                        trailingIcon = {
                            IconButton(onClick = { launchVoiceFor(true, null) }) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input for Title",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedLabelColor = themeColors.buttonEqualBg,
                            unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                            focusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (validationErrorMsg != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = validationErrorMsg!!,
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Scrollable Item Entry Rows
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .bounceOverscroll(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(items, key = { index, item -> item.id }) { index, item ->
                            ItemInputRow(
                                index = index,
                                item = item,
                                isBazaarMode = (dialogTab == 1),
                                isBn = isBn,
                                unitsList = unitsList,
                                themeColors = themeColors,
                                onItemChange = { updated ->
                                    items[index] = updated
                                    validationErrorMsg = null
                                },
                                onDeleteClick = {
                                    itemIndexToDelete = index
                                },
                                onVoiceClick = {
                                    launchVoiceFor(false, index)
                                }
                            )
                        }

                        item {
                            // Button: Add New Item
                            Button(
                                onClick = {
                                    items.add(
                                        MarketItem(
                                            name = "",
                                            unitPrice = 0.0,
                                            quantity = 1.0,
                                            unit = if (isBn) "কেজি" else "kg",
                                            isChecked = (dialogTab == 1)
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.cardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "+ নতুন আইটেম যোগ করুন" else "+ Add New Item",
                                    color = themeColors.buttonEqualBg,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom Total and Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (dialogTab == 0) (if (isBn) "মোট আনুমানিক হিসাব" else "Estimated Total")
                                else (if (isBn) "সম্পন্ন মোট হিসাব" else "Total Purchased"),
                                fontSize = 10.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "৳ ${String.format(Locale.US, "%.2f", calculatedGrandTotal)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dialogTab == 0) themeColors.buttonEqualBg else Color(0xFF10B981)
                            )
                        }

                        Button(
                            onClick = {
                                val validItems = items.filter { it.name.isNotBlank() }
                                if (validItems.isEmpty()) {
                                    validationErrorMsg = if (isBn) "অনুগ্রহ করে অন্তত একটি পণ্যের নাম লিখুন!" else "Please enter at least one item name!"
                                    return@Button
                                }

                                isSubmitted = true
                                if (dialogTab == 0) {
                                    // Plan Mode Save
                                    val newPlan = MarketPlanList(
                                        id = editingPlan?.id ?: java.util.UUID.randomUUID().toString(),
                                        title = listTitle.ifBlank { if (isBn) "বাজারের ফর্দ" else "Shopping List" },
                                        items = validItems,
                                        timestamp = editingPlan?.timestamp ?: System.currentTimeMillis()
                                    )
                                    onSavePlan(newPlan)
                                } else {
                                    // Direct Bazaar Mode Validation
                                    val checkedItems = items.filter { it.isChecked && it.name.isNotBlank() }
                                    if (checkedItems.isEmpty()) {
                                        validationErrorMsg = if (isBn) "অনুগ্রহ করে কেনাকাটা করা পণ্যগুলো টিক দিন!" else "Please check the items you bought!"
                                        return@Button
                                    }

                                    val hasMissingInputs = checkedItems.any { it.unitPrice <= 0.0 || it.quantity <= 0.0 }
                                    if (hasMissingInputs) {
                                        validationErrorMsg = if (isBn) "বাজার সম্পন্ন করতে টিক দেওয়া প্রতিটি পণ্যের দর ও পরিমাণ ইনপুট দিতে হবে!"
                                        else "Price and quantity must be provided for all checked items!"
                                        return@Button
                                    }

                                    val memo = CompletedBazaarMemo(
                                        title = listTitle.ifBlank { if (isBn) "সম্পন্ন বাজার মেমো" else "Completed Bazaar" },
                                        items = checkedItems,
                                        totalCost = checkedItems.sumOf { it.total }
                                    )
                                    onCompleteDirectBazaar(memo)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dialogTab == 0) themeColors.buttonEqualBg else Color(0xFF10B981)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = if (dialogTab == 0) Icons.Default.Save else Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dialogTab == 0) (if (isBn) "ফর্দ সেভ করুন" else "Save List")
                                else (if (isBn) "বাজার সম্পন্ন করুন" else "Complete Bazaar"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Delete Row Confirmation Dialog
        if (itemIndexToDelete != null) {
            val idx = itemIndexToDelete!!
            val itemName = if (idx in items.indices && items[idx].name.isNotBlank()) items[idx].name else if (isBn) "এই আইটেমটি" else "this item"

            AlertDialog(
                onDismissRequest = { itemIndexToDelete = null },
                title = {
                    Text(
                        text = if (isBn) "আইটেম ডিলিট" else "Delete Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Text(
                        text = if (isBn) "আপনি কি \"$itemName\" ডিলিট করতে চান?" else "Do you want to delete \"$itemName\"?",
                        fontSize = 14.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (idx in items.indices) {
                                items.removeAt(idx)
                                if (items.isEmpty()) {
                                    items.add(MarketItem(name = "", unitPrice = 0.0, quantity = 1.0, isChecked = (dialogTab == 1)))
                                }
                            }
                            itemIndexToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(if (isBn) "ডিলিট" else "Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemIndexToDelete = null }) {
                        Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// --- Helper: Smart Voice Parser for Bazaar items ---
fun parseBazaarVoiceText(spoken: String, currentItem: MarketItem, isBn: Boolean): MarketItem {
    var raw = spoken.trim()
    if (raw.isBlank()) return currentItem

    // Normalize Bengali digits
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    var normalized = raw
    for (i in bnDigits.indices) {
        normalized = normalized.replace(bnDigits[i], ('0' + i))
    }

    val unitKeywords = listOf(
        "কেজি" to "কেজি", "কেজিতে" to "কেজি", "kg" to "kg",
        "গ্রাম" to "গ্রাম", "gm" to "gm", "gram" to "gm",
        "লিটার" to "লিটার", "liter" to "liter", "ltr" to "liter",
        "পিস" to "পিস", "টা" to "পিস", "pcs" to "pcs", "piece" to "pcs",
        "প্যাকেট" to "প্যাকেট", "pkt" to "pkt", "packet" to "pkt",
        "বক্স" to "বক্স", "box" to "box",
        "ডজন" to "ডজন", "doz" to "doz", "dozen" to "doz",
        "হালি" to "হালি",
        "বস্তা" to "বস্তা", "bag" to "bag"
    )

    var detectedUnit = currentItem.unit
    for ((key, u) in unitKeywords) {
        if (raw.contains(key, ignoreCase = true)) {
            detectedUnit = if (isBn) {
                when (u) {
                    "kg" -> "কেজি"
                    "gm" -> "গ্রাম"
                    "liter" -> "লিটার"
                    "pcs" -> "পিস"
                    "pkt" -> "প্যাকেট"
                    "box" -> "বক্স"
                    "doz" -> "ডজন"
                    "bag" -> "বস্তা"
                    else -> u
                }
            } else {
                when (u) {
                    "কেজি" -> "kg"
                    "গ্রাম" -> "gm"
                    "লিটার" -> "liter"
                    "পিস" -> "pcs"
                    "প্যাকেট" -> "pkt"
                    "বক্স" -> "box"
                    "ডজন" -> "doz"
                    "হালি" -> "pcs"
                    "বস্তা" -> "bag"
                    else -> u
                }
            }
            break
        }
    }

    val numbers = Regex("""\d+(\.\d+)?""").findAll(normalized).mapNotNull { it.value.toDoubleOrNull() }.toList()

    var newPrice = currentItem.unitPrice
    var newQty = currentItem.quantity

    if (numbers.size >= 2) {
        newPrice = numbers[0]
        newQty = numbers[1]
    } else if (numbers.size == 1) {
        if (raw.contains("টাকা") || raw.contains("tk") || raw.contains("rate") || raw.contains("দর")) {
            newPrice = numbers[0]
        } else if (raw.contains("কেজি") || raw.contains("লিটার") || raw.contains("গ্রাম") || raw.contains("পিস") || raw.contains("ডজন") || raw.contains("kg") || raw.contains("pcs")) {
            newQty = numbers[0]
        } else {
            newPrice = numbers[0]
        }
    }

    var cleanedName = raw
    for ((key, _) in unitKeywords) {
        cleanedName = cleanedName.replace(key, "", ignoreCase = true)
    }
    cleanedName = cleanedName.replace("টাকা", "", ignoreCase = true)
        .replace("টাকার", "", ignoreCase = true)
        .replace("tk", "", ignoreCase = true)
        .replace("taka", "", ignoreCase = true)
        .replace("দর", "", ignoreCase = true)
        .replace("রেট", "", ignoreCase = true)
        .replace("দাম", "", ignoreCase = true)
        .replace(Regex("""[0-9০-৯\.]+"""), "")
        .trim()

    val finalName = if (cleanedName.isNotBlank()) cleanedName else raw

    return currentItem.copy(
        name = finalName,
        unitPrice = if (newPrice > 0) newPrice else currentItem.unitPrice,
        quantity = if (newQty > 0) newQty else currentItem.quantity,
        unit = detectedUnit
    )
}

// --- Item Input Row Composable (Compact & Professional) ---
@Composable
fun ItemInputRow(
    index: Int,
    item: MarketItem,
    isBazaarMode: Boolean,
    isBn: Boolean,
    unitsList: List<String>,
    themeColors: CalculatorThemeColors,
    onItemChange: (MarketItem) -> Unit,
    onDeleteClick: () -> Unit,
    onVoiceClick: (() -> Unit)? = null
) {
    var nameText by remember(item.id, item.name) { mutableStateOf(item.name) }
    var rateText by remember(item.id, item.unitPrice) { mutableStateOf(if (item.unitPrice > 0) (if (item.unitPrice % 1.0 == 0.0) item.unitPrice.toInt().toString() else item.unitPrice.toString()) else "") }
    var qtyText by remember(item.id, item.quantity) { mutableStateOf(if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()) }
    var unitText by remember(item.id, item.unit) { mutableStateOf(item.unit) }
    var unitMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Row 1: Checkbox (if Bazaar Mode) + Name Field + Voice Button + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isBazaarMode) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { isChecked ->
                            onItemChange(item.copy(isChecked = isChecked))
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981)),
                        modifier = Modifier.size(24.dp).padding(end = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Compact Name Input Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeColors.background)
                        .border(1.dp, themeColors.displayText.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = nameText,
                            onValueChange = {
                                nameText = it
                                onItemChange(item.copy(name = it))
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = themeColors.displayText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(themeColors.buttonEqualBg),
                            decorationBox = { innerTextField ->
                                if (nameText.isEmpty()) {
                                    Text(
                                        text = if (isBn) "পণ্যের নাম (আলু, চাল)" else "Item name (e.g. Rice)",
                                        fontSize = 12.sp,
                                        color = themeColors.displayText.copy(alpha = 0.4f)
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (onVoiceClick != null) {
                            IconButton(
                                onClick = onVoiceClick,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Delete Button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Row",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: দর (Rate) + পরিমাণ (Qty) + একক (Unit) + মোট (Total)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // दर (Rate)
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeColors.background)
                        .border(1.dp, themeColors.displayText.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = rateText,
                        onValueChange = {
                            rateText = it
                            val p = it.toDoubleOrNull() ?: 0.0
                            onItemChange(item.copy(unitPrice = p))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = themeColors.displayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(themeColors.buttonEqualBg),
                        decorationBox = { innerTextField ->
                            if (rateText.isEmpty()) {
                                Text(
                                    text = if (isBn) "দর ৳" else "Rate ৳",
                                    fontSize = 11.sp,
                                    color = themeColors.displayText.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // পরিমাণ (Qty)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeColors.background)
                        .border(1.dp, themeColors.displayText.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = qtyText,
                        onValueChange = {
                            qtyText = it
                            val q = it.toDoubleOrNull() ?: 1.0
                            onItemChange(item.copy(quantity = q))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = themeColors.displayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(themeColors.buttonEqualBg),
                        decorationBox = { innerTextField ->
                            if (qtyText.isEmpty()) {
                                Text(
                                    text = if (isBn) "পরিমাণ" else "Qty",
                                    fontSize = 11.sp,
                                    color = themeColors.displayText.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // একক Selector Dropdown
                Box(modifier = Modifier.weight(0.9f)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, themeColors.displayText.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .clickable { unitMenuExpanded = true },
                        color = themeColors.background
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = unitText,
                                fontSize = 11.sp,
                                color = themeColors.displayText,
                                maxLines = 1
                            )
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = themeColors.displayText)
                        }
                    }

                    DropdownMenu(
                        expanded = unitMenuExpanded,
                        onDismissRequest = { unitMenuExpanded = false }
                    ) {
                        unitsList.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit, fontSize = 12.sp) },
                                onClick = {
                                    unitText = unit
                                    onItemChange(item.copy(unit = unit))
                                    unitMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Calculated Item Total
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .padding(start = 2.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isBn) "মোট" else "Total",
                            fontSize = 9.sp,
                            color = themeColors.displayText.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "৳ ${String.format(Locale.US, "%.1f", item.total)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBazaarMode && !item.isChecked) themeColors.displayText.copy(alpha = 0.4f) else themeColors.buttonEqualBg
                        )
                    }
                }
            }
        }
    }
}

// --- DIALOG 2: Execute Bazaar From Saved Plan (Interactive Checklist & Completion) ---
@Composable
fun ExecuteBazaarFromPlanDialog(
    plan: MarketPlanList,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onCompleteBazaar: (CompletedBazaarMemo, Boolean) -> Unit
) {
    val context = LocalContext.current
    var memoTitle by remember { mutableStateOf(plan.title) }
    val executionItems = remember {
        mutableStateListOf<MarketItem>().apply {
            addAll(plan.items.map { it.copy(isChecked = false) })
        }
    }

    var deleteOriginalPlanAfterCompletion by remember { mutableStateOf(true) }
    var itemIndexToDelete by remember { mutableStateOf<Int?>(null) }
    var validationErrorMsg by remember { mutableStateOf<String?>(null) }

    val syncChangesToPlan = {
        val currentLists = MarketStorageManager.loadPlanLists(context).toMutableList()
        val idx = currentLists.indexOfFirst { it.id == plan.id }
        if (idx != -1) {
            currentLists[idx] = plan.copy(title = memoTitle, items = executionItems.toList())
            MarketStorageManager.savePlanLists(context, currentLists)
        }
    }

    BackHandler {
        syncChangesToPlan()
        onDismiss()
    }

    var voiceTargetItemIndex by remember { mutableStateOf<Int?>(null) }
    var isVoiceTargetTitle by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                if (isVoiceTargetTitle) {
                    memoTitle = spokenText.trim()
                } else {
                    val idx = voiceTargetItemIndex
                    if (idx != null && idx in executionItems.indices) {
                        val current = executionItems[idx]
                        val updated = parseBazaarVoiceText(spokenText, current, isBn)
                        executionItems[idx] = updated
                    }
                }
            }
        }
    }

    val launchVoiceFor: (Boolean, Int?) -> Unit = { isTitle, idx ->
        try {
            isVoiceTargetTitle = isTitle
            voiceTargetItemIndex = idx
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBn) "bn-BD" else "en-US")
                putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    if (isTitle) (if (isBn) "মেমোর নাম বলুন..." else "Speak Memo Title...")
                    else (if (isBn) "পণ্যের নাম বা বিবরণ বলুন (যেমন: আলু ৫০ টাকা ২ কেজি)..." else "Speak Item Details...")
                )
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, if (isBn) "ভয়েস ইনপুট সমর্থিত নয়" else "Voice input not supported", Toast.LENGTH_SHORT).show()
        }
    }

    val unitsList = if (isBn) {
        listOf("কেজি", "গ্রাম", "লিটার", "পিস", "প্যাকেট", "বক্স", "ডজন", "হালি", "বস্তা")
    } else {
        listOf("kg", "gm", "liter", "pcs", "pkt", "box", "doz", "bag")
    }

    val livePurchasedTotal by remember {
        derivedStateOf {
            executionItems.filter { it.isChecked }.sumOf { it.total }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.96f),
                shape = RoundedCornerShape(20.dp),
                color = themeColors.background,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "বাজার সম্পন্নকরণ (লাইভ চেকলিস্ট)" else "Bazaar Shopping Checklist",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "পণ্য কেনার পর টিক দিন ও প্রকৃত দর/পরিমাণ দিন" else "Check items bought & verify actual rate/qty",
                                fontSize = 10.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title Input with Voice
                    OutlinedTextField(
                        value = memoTitle,
                        onValueChange = { memoTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(if (isBn) "মেমোর নাম" else "Memo Title", fontSize = 11.sp) },
                        trailingIcon = {
                            IconButton(onClick = { launchVoiceFor(true, null) }) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedLabelColor = themeColors.buttonEqualBg,
                            unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                            focusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (validationErrorMsg != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = validationErrorMsg!!,
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Scrollable Checklist Items
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .bounceOverscroll(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(executionItems, key = { index, item -> item.id }) { index, item ->
                            ItemInputRow(
                                index = index,
                                item = item,
                                isBazaarMode = true,
                                isBn = isBn,
                                unitsList = unitsList,
                                themeColors = themeColors,
                                onItemChange = { updated ->
                                    executionItems[index] = updated
                                    validationErrorMsg = null
                                },
                                onDeleteClick = {
                                    itemIndexToDelete = index
                                },
                                onVoiceClick = {
                                    launchVoiceFor(false, index)
                                }
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    executionItems.add(
                                        MarketItem(
                                            name = "",
                                            unitPrice = 0.0,
                                            quantity = 1.0,
                                            unit = if (isBn) "কেজি" else "kg",
                                            isChecked = true
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.cardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.4f))
                            ) {
                                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "+ বাজারে অতিরিক্ত পণ্য যোগ করুন" else "+ Add Additional Item",
                                    color = themeColors.buttonEqualBg,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = deleteOriginalPlanAfterCompletion,
                            onCheckedChange = { deleteOriginalPlanAfterCompletion = it },
                            colors = CheckboxDefaults.colors(checkedColor = themeColors.buttonEqualBg),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "বাজার সম্পন্ন হলে ফর্দ তালিকা থেকে এটি সরিয়ে ফেলুন" else "Remove from active lists after completion",
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom Total and Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "কেনা হয়েছে (${executionItems.count { it.isChecked }}/${executionItems.size})" else "Checked (${executionItems.count { it.isChecked }}/${executionItems.size})",
                                fontSize = 10.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "৳ ${String.format(Locale.US, "%.2f", livePurchasedTotal)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        Button(
                            onClick = {
                                val checkedItems = executionItems.filter { it.isChecked && it.name.isNotBlank() }
                                if (checkedItems.isEmpty()) {
                                    validationErrorMsg = if (isBn) "অনুগ্রহ করে কেনা পণ্যগুলোতে টিক দিন!" else "Please check the items you bought!"
                                    return@Button
                                }

                                val hasMissingInputs = checkedItems.any { it.unitPrice <= 0.0 || it.quantity <= 0.0 }
                                if (hasMissingInputs) {
                                    validationErrorMsg = if (isBn) "বাজার সম্পন্ন করতে টিক দেওয়া প্রতিটি পণ্যের দর ও পরিমাণ ইনপুট দিতে হবে!"
                                    else "Unit price and quantity must be entered for all checked items!"
                                    return@Button
                                }

                                val completedMemo = CompletedBazaarMemo(
                                    title = memoTitle.ifBlank { plan.title },
                                    items = checkedItems,
                                    totalCost = checkedItems.sumOf { it.total }
                                )

                                onCompleteBazaar(completedMemo, deleteOriginalPlanAfterCompletion)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "বাজার সম্পন্ন করুন" else "Complete Bazaar",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation inside Execution dialog
        if (itemIndexToDelete != null) {
            val idx = itemIndexToDelete!!
            val itemName = if (idx in executionItems.indices && executionItems[idx].name.isNotBlank()) executionItems[idx].name else if (isBn) "এই আইটেমটি" else "this item"

            AlertDialog(
                onDismissRequest = { itemIndexToDelete = null },
                title = {
                    Text(
                        text = if (isBn) "আইটেম ডিলিট" else "Delete Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Text(
                        text = if (isBn) "আপনি কি \"$itemName\" ডিলিট করতে চান?" else "Do you want to delete \"$itemName\"?",
                        fontSize = 14.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (idx in executionItems.indices) {
                                executionItems.removeAt(idx)
                            }
                            itemIndexToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(if (isBn) "ডিলিট" else "Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemIndexToDelete = null }) {
                        Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// --- DIALOG 3: Edit Completed Memo Dialog ---
@Composable
fun EditCompletedMemoDialog(
    memo: CompletedBazaarMemo,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onSaveEditedMemo: (CompletedBazaarMemo) -> Unit
) {
    val context = LocalContext.current
    var memoTitle by remember { mutableStateOf(memo.title) }
    val editableItems = remember {
        mutableStateListOf<MarketItem>().apply {
            addAll(memo.items.map { it.copy(isChecked = true) })
        }
    }

    var itemIndexToDelete by remember { mutableStateOf<Int?>(null) }
    var validationErrorMsg by remember { mutableStateOf<String?>(null) }

    var voiceTargetItemIndex by remember { mutableStateOf<Int?>(null) }
    var isVoiceTargetTitle by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                if (isVoiceTargetTitle) {
                    memoTitle = spokenText.trim()
                } else {
                    val idx = voiceTargetItemIndex
                    if (idx != null && idx in editableItems.indices) {
                        val current = editableItems[idx]
                        val updated = parseBazaarVoiceText(spokenText, current, isBn)
                        editableItems[idx] = updated
                    }
                }
            }
        }
    }

    val launchVoiceFor: (Boolean, Int?) -> Unit = { isTitle, idx ->
        try {
            isVoiceTargetTitle = isTitle
            voiceTargetItemIndex = idx
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBn) "bn-BD" else "en-US")
                putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    if (isTitle) (if (isBn) "মেমোর নাম বলুন..." else "Speak Memo Title...")
                    else (if (isBn) "পণ্যের নাম বা বিবরণ বলুন..." else "Speak Item Details...")
                )
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, if (isBn) "ভয়েস ইনপুট সমর্থিত নয়" else "Voice input not supported", Toast.LENGTH_SHORT).show()
        }
    }

    val unitsList = if (isBn) {
        listOf("কেজি", "গ্রাম", "লিটার", "পিস", "প্যাকেট", "বক্স", "ডজন", "হালি", "বস্তা")
    } else {
        listOf("kg", "gm", "liter", "pcs", "pkt", "box", "doz", "bag")
    }

    val totalCost by remember {
        derivedStateOf {
            editableItems.sumOf { it.total }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.96f),
                shape = RoundedCornerShape(20.dp),
                color = themeColors.background,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "মেমো এডিট করুন" else "Edit Memo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = if (isBn) "মেমোর পণ্য, দর বা পরিমাণ পরিবর্তন করুন" else "Update memo items, rate or quantity",
                                fontSize = 10.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = memoTitle,
                        onValueChange = { memoTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(if (isBn) "মেমোর নাম" else "Memo Title", fontSize = 11.sp) },
                        trailingIcon = {
                            IconButton(onClick = { launchVoiceFor(true, null) }) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.displayText,
                            unfocusedTextColor = themeColors.displayText,
                            focusedLabelColor = themeColors.buttonEqualBg,
                            unfocusedLabelColor = themeColors.displayText.copy(alpha = 0.6f),
                            focusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = themeColors.displayText.copy(alpha = 0.5f),
                            focusedBorderColor = themeColors.buttonEqualBg,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (validationErrorMsg != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = validationErrorMsg!!,
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .bounceOverscroll(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(editableItems, key = { index, item -> item.id }) { index, item ->
                            ItemInputRow(
                                index = index,
                                item = item,
                                isBazaarMode = false,
                                isBn = isBn,
                                unitsList = unitsList,
                                themeColors = themeColors,
                                onItemChange = { updated ->
                                    editableItems[index] = updated
                                    validationErrorMsg = null
                                },
                                onDeleteClick = {
                                    itemIndexToDelete = index
                                },
                                onVoiceClick = {
                                    launchVoiceFor(false, index)
                                }
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    editableItems.add(
                                        MarketItem(
                                            name = "",
                                            unitPrice = 0.0,
                                            quantity = 1.0,
                                            unit = if (isBn) "কেজি" else "kg",
                                            isChecked = true
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.cardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.buttonEqualBg.copy(alpha = 0.4f))
                            ) {
                                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "+ নতুন আইটেম যোগ করুন" else "+ Add New Item",
                                    color = themeColors.buttonEqualBg,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "আপডেটকৃত মোট খরচ" else "Updated Total",
                                fontSize = 10.sp,
                                color = themeColors.displayText.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "৳ ${String.format(Locale.US, "%.2f", totalCost)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        Button(
                            onClick = {
                                val validItems = editableItems.filter { it.name.isNotBlank() }
                                if (validItems.isEmpty()) {
                                    validationErrorMsg = if (isBn) "অনুগ্রহ করে অন্তত একটি পণ্য রাখুন!" else "Please keep at least one item!"
                                    return@Button
                                }

                                val hasMissing = validItems.any { it.unitPrice <= 0.0 || it.quantity <= 0.0 }
                                if (hasMissing) {
                                    validationErrorMsg = if (isBn) "প্রতিটি পণ্যের দর ও পরিমাণ ইনপুট দিতে হবে!" else "Rate and quantity are required!"
                                    return@Button
                                }

                                val updatedMemo = memo.copy(
                                    title = memoTitle.ifBlank { memo.title },
                                    items = validItems,
                                    totalCost = validItems.sumOf { it.total }
                                )
                                onSaveEditedMemo(updatedMemo)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "মেমো সেভ করুন" else "Save Memo",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        if (itemIndexToDelete != null) {
            val idx = itemIndexToDelete!!
            val itemName = if (idx in editableItems.indices && editableItems[idx].name.isNotBlank()) editableItems[idx].name else if (isBn) "এই আইটেমটি" else "this item"

            AlertDialog(
                onDismissRequest = { itemIndexToDelete = null },
                title = {
                    Text(
                        text = if (isBn) "আইটেম ডিলিট" else "Delete Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = themeColors.displayText
                    )
                },
                text = {
                    Text(
                        text = if (isBn) "আপনি কি \"$itemName\" ডিলিট করতে চান?" else "Do you want to delete \"$itemName\"?",
                        fontSize = 14.sp,
                        color = themeColors.displayText.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (idx in editableItems.indices) {
                                editableItems.removeAt(idx)
                            }
                            itemIndexToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(if (isBn) "ডিলিট" else "Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemIndexToDelete = null }) {
                        Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                    }
                },
                containerColor = themeColors.cardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// --- DIALOG 4: Memo Detail / Voucher View with Export PDF ---
@Composable
fun MemoDetailDialog(
    memo: CompletedBazaarMemo,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onDismiss: () -> Unit,
    onExportSafPdf: (CompletedBazaarMemo) -> Unit,
    onSharePdf: (CompletedBazaarMemo) -> Unit,
    onEditMemo: ((CompletedBazaarMemo) -> Unit)? = null
) {
    val dateStr = SimpleDateFormat("dd MMMM, yyyy - hh:mm a", if (isBn) Locale("bn") else Locale.ENGLISH).format(Date(memo.timestamp))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.96f),
                shape = RoundedCornerShape(20.dp),
                color = themeColors.background,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "বাজার ভাউচার মেমো" else "Shopping Memo Receipt",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onEditMemo != null) {
                                IconButton(
                                    onClick = { onEditMemo(memo) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Memo", tint = themeColors.buttonEqualBg, modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = themeColors.displayText)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cash Memo Container Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Memo Voucher Top Header
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = memo.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateStr,
                                    fontSize = 11.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = if (isBn) "✓ কেনাকাটা সম্পন্ন ভাউচার" else "✓ Completed Shopping Memo",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.12f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(themeColors.background, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = if (isBn) "নং" else "#", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText.copy(alpha = 0.7f), modifier = Modifier.width(24.dp))
                                Text(text = if (isBn) "পণ্যের নাম" else "Item", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText.copy(alpha = 0.7f), modifier = Modifier.weight(1.5f))
                                Text(text = if (isBn) "পরিমাণ" else "Qty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText.copy(alpha = 0.7f), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(text = if (isBn) "দর" else "Rate", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText.copy(alpha = 0.7f), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                Text(text = if (isBn) "মোট" else "Total", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText.copy(alpha = 0.7f), modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Itemized Rows
                            memo.items.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        color = themeColors.displayText.copy(alpha = 0.6f),
                                        modifier = Modifier.width(24.dp)
                                    )
                                    Text(
                                        text = item.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = themeColors.displayText,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                    Text(
                                        text = "${if (item.quantity % 1.0 == 0.0) item.quantity.toInt() else item.quantity} ${item.unit}",
                                        fontSize = 11.sp,
                                        color = themeColors.displayText.copy(alpha = 0.8f),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "৳${String.format(Locale.US, "%.1f", item.unitPrice)}",
                                        fontSize = 11.sp,
                                        color = themeColors.displayText.copy(alpha = 0.8f),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = "৳${String.format(Locale.US, "%.1f", item.total)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText,
                                        modifier = Modifier.weight(1.2f),
                                        textAlign = TextAlign.End
                                    )
                                }
                                HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.05f), thickness = 0.5.dp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.15f), thickness = 1.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Grand Total Calculation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "সর্বমোট বাজার খরচ:" else "Grand Total Spent:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                                Text(
                                    text = "৳ ${String.format(Locale.US, "%.2f", memo.totalCost)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Export & Share Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onExportSafPdf(memo) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "PDF সেভ" else "Save PDF",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { onSharePdf(memo) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = themeColors.buttonEqualBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isBn) "শেয়ার করুন" else "Share PDF", color = themeColors.buttonEqualBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- Empty State View ---
@Composable
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    themeColors: CalculatorThemeColors
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(themeColors.cardBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = themeColors.displayText.copy(alpha = 0.35f),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = themeColors.displayText.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

// --- PDF Generation and Export Helpers ---

fun getSanitizedMemoPdfFileName(memo: CompletedBazaarMemo): String {
    val cleanTitle = memo.title.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_").take(30).trim('_')
    val timeFormatted = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(memo.timestamp))
    return if (cleanTitle.isNotBlank()) "${cleanTitle}_$timeFormatted.pdf" else "Market_Memo_$timeFormatted.pdf"
}

fun generateMarketPdfDocument(memo: CompletedBazaarMemo, isBn: Boolean): PdfDocument {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Standard
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas

    val paint = Paint().apply { isAntiAlias = true }
    val textPaint = Paint().apply { isAntiAlias = true }

    // Top Header Banner
    paint.color = AndroidColor.parseColor("#0D9488")
    canvas.drawRect(0f, 0f, 595f, 85f, paint)

    // Header Title
    paint.color = AndroidColor.WHITE
    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText(if (isBn) "বাজারের খরচ ও ভাউচার মেমো" else "Market Expense & Voucher Memo", 40f, 48f, paint)

    // Subtitle without "Smart Calculator"
    paint.textSize = 11f
    paint.isFakeBoldText = false
    canvas.drawText("ToolsMate Market Expense Tracker", 40f, 68f, paint)

    // Memo Details Box
    textPaint.color = AndroidColor.parseColor("#1E293B")
    textPaint.textSize = 14f
    textPaint.isFakeBoldText = true
    canvas.drawText(memo.title, 40f, 115f, textPaint)

    textPaint.color = AndroidColor.parseColor("#64748B")
    textPaint.textSize = 10f
    textPaint.isFakeBoldText = false
    val dateStr = SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale.getDefault()).format(Date(memo.timestamp))
    canvas.drawText("তারিখ / Date: $dateStr", 40f, 132f, textPaint)

    // Table Header Background
    paint.color = AndroidColor.parseColor("#E2E8F0")
    canvas.drawRect(40f, 150f, 555f, 178f, paint)

    // Table Header Texts
    paint.color = AndroidColor.parseColor("#0F172A")
    paint.textSize = 10f
    paint.isFakeBoldText = true
    canvas.drawText("নং", 50f, 168f, paint)
    canvas.drawText("পণ্যের নাম (Item)", 85f, 168f, paint)
    canvas.drawText("পরিমাণ (Qty)", 290f, 168f, paint)
    canvas.drawText("দর (Rate)", 390f, 168f, paint)
    canvas.drawText("মোট (Total)", 480f, 168f, paint)

    // Header separator line
    paint.color = AndroidColor.parseColor("#94A3B8")
    paint.strokeWidth = 1f
    canvas.drawLine(40f, 178f, 555f, 178f, paint)

    // Table Rows
    var yPos = 200f
    textPaint.textSize = 10f

    val linePaint = Paint().apply {
        isAntiAlias = true
        color = AndroidColor.parseColor("#E2E8F0")
        strokeWidth = 0.75f
    }
    val altRowPaint = Paint().apply {
        isAntiAlias = true
        color = AndroidColor.parseColor("#F8FAFC")
    }

    memo.items.forEachIndexed { idx, item ->
        if (yPos > 740f) return@forEachIndexed

        if (idx % 2 == 1) {
            canvas.drawRect(40f, yPos - 14f, 555f, yPos + 10f, altRowPaint)
        }

        textPaint.color = AndroidColor.parseColor("#475569")
        canvas.drawText("${idx + 1}", 50f, yPos, textPaint)

        textPaint.color = AndroidColor.parseColor("#0F172A")
        val displayName = if (item.name.length > 28) item.name.take(26) + ".." else item.name
        canvas.drawText(displayName, 85f, yPos, textPaint)

        textPaint.color = AndroidColor.parseColor("#334155")
        val qtyStr = if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()} ${item.unit}" else "${item.quantity} ${item.unit}"
        canvas.drawText(qtyStr, 290f, yPos, textPaint)

        canvas.drawText("৳${String.format(Locale.US, "%.1f", item.unitPrice)}", 390f, yPos, textPaint)

        textPaint.color = AndroidColor.parseColor("#0F172A")
        textPaint.isFakeBoldText = true
        canvas.drawText("৳${String.format(Locale.US, "%.1f", item.total)}", 480f, yPos, textPaint)
        textPaint.isFakeBoldText = false

        canvas.drawLine(40f, yPos + 10f, 555f, yPos + 10f, linePaint)
        yPos += 24f
    }

    // Grand Total Summary Box
    yPos += 14f
    paint.color = AndroidColor.parseColor("#F0FDF4")
    canvas.drawRoundRect(40f, yPos - 12f, 555f, yPos + 32f, 8f, 8f, paint)

    paint.color = AndroidColor.parseColor("#10B981")
    paint.strokeWidth = 1.5f
    paint.style = Paint.Style.STROKE
    canvas.drawRoundRect(40f, yPos - 12f, 555f, yPos + 32f, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    paint.color = AndroidColor.parseColor("#065F46")
    paint.textSize = 12f
    paint.isFakeBoldText = true
    canvas.drawText(if (isBn) "মোট পণ্য: ${memo.items.size} টি" else "Total Items: ${memo.items.size}", 56f, yPos + 12f, paint)

    paint.color = AndroidColor.parseColor("#047857")
    paint.textSize = 14f
    paint.isFakeBoldText = true
    val totalLabel = if (isBn) "সর্বমোট বাজার খরচ:" else "Grand Total:"
    canvas.drawText("$totalLabel ৳${String.format(Locale.US, "%.2f", memo.totalCost)}", 320f, yPos + 12f, paint)

    pdfDocument.finishPage(page)
    return pdfDocument
}

fun exportMemoPdfToUri(context: Context, targetUri: Uri, memo: CompletedBazaarMemo, isBn: Boolean) {
    try {
        val pdfDocument = generateMarketPdfDocument(memo, isBn)
        context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()
        Toast.makeText(context, if (isBn) "PDF মেমরিতে সফলভাবে সংরক্ষিত হয়েছে!" else "PDF saved successfully!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "PDF সংরক্ষণ ব্যর্থ: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun shareMemoPdfDirect(context: Context, memo: CompletedBazaarMemo, isBn: Boolean) {
    try {
        val pdfDocument = generateMarketPdfDocument(memo, isBn)
        val pdfDir = File(context.cacheDir, "pdf_exports").apply { mkdirs() }
        val fileName = getSanitizedMemoPdfFileName(memo)
        val file = File(pdfDir, fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, if (isBn) "বাজার মেমো PDF শেয়ার করুন" else "Share Market Memo PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "PDF শেয়ার ব্যর্থ হয়েছে: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

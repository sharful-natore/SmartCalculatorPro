package com.example.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Mic
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Download
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.util.scaleOnPress
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Close
import com.example.util.AppLanguage


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val historyItems by viewModel.historyList.collectAsState()

    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isAscending by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf("Calculator") }

    val context = LocalContext.current
    val historySpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                searchQuery = spokenText
                isSearchActive = true
            }
        }
    }
    fun startHistoryVoiceSearch() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (isBn) "কথা বলুন..." else "Speak now...")
            }
            historySpeechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice search unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val bounceAnimatable = remember { Animatable(0f) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.backupHistoryToUri(it)
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.restoreHistoryFromUri(it)
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val currentValue = bounceAnimatable.value
                if (currentValue != 0f) {
                    if ((currentValue < 0f && available.y > 0f) || (currentValue > 0f && available.y < 0f)) {
                        val newDelta = available.y * 0.35f
                        val newValue = if (currentValue < 0f) {
                            (currentValue + newDelta).coerceAtMost(0f)
                        } else {
                            (currentValue + newDelta).coerceAtLeast(0f)
                        }
                        coroutineScope.launch {
                            bounceAnimatable.snapTo(newValue)
                        }
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y != 0f) {
                    coroutineScope.launch {
                        bounceAnimatable.snapTo((bounceAnimatable.value + available.y * 0.35f).coerceIn(-140f, 140f))
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                bounceAnimatable.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
                return super.onPostFling(consumed, available)
            }
        }
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress && bounceAnimatable.value != 0f) {
            coroutineScope.launch {
                bounceAnimatable.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        val screenHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = screenHeight)
                .nestedScroll(nestedScrollConnection)
                .offset { IntOffset(0, bounceAnimatable.value.roundToInt()) }
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isBn) "সার্চ করুন..." else "Search...") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = themeColors.buttonEqualBg,
                        unfocusedIndicatorColor = themeColors.displayText.copy(alpha = 0.5f),
                        cursorColor = themeColors.buttonEqualBg
                    ),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Search", modifier = Modifier.size(20.dp))
                                }
                            }
                            IconButton(onClick = { startHistoryVoiceSearch() }) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = themeColors.buttonEqualBg, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { isSearchActive = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Search", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                )
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (viewModel.isHistorySelectionMode) "${viewModel.selectedHistoryIds.size} Selected" else if (isBn) "হিস্টোরি" else "History",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }
            }

            if (viewModel.isHistorySelectionMode) {
                IconButton(
                    onClick = { viewModel.deleteSelectedHistory() },
                    modifier = Modifier.testTag("delete_selected_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Selected",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isSearchActive) {
                        IconButton(
                            onClick = { isSearchActive = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = themeColors.buttonEqualBg,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(themeColors.cardBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (isBn) "নতুন আগে" else "Newest First", color = themeColors.displayText) },
                                    onClick = {
                                        isAscending = false
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isBn) "পুরোনো আগে" else "Oldest First", color = themeColors.displayText) },
                                    onClick = {
                                        isAscending = true
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                    // Backup Button
                    IconButton(
                        onClick = { viewModel.showBackupConfirmDialog = true },
                        modifier = Modifier.testTag("backup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Backup History",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Restore Button
                    IconButton(
                        onClick = { viewModel.showRestoreConfirmDialog = true },
                        modifier = Modifier.testTag("restore_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Restore History",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Clear All Button (Only show if there are items)
                    if (historyItems.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.showClearHistoryDialog = true },
                            modifier = Modifier.testTag("clear_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All History",
                                tint = themeColors.buttonEqualBg,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Tool Type Filter Chips Row
        val availableTypes = remember(historyItems) {
            val types = historyItems.map { if (it.type == "Basic") "Calculator" else it.type }.filter { it.isNotEmpty() }.distinct()
            val list = mutableListOf<String>()
            if (types.contains("Calculator") || historyItems.isEmpty()) {
                list.add("Calculator")
            }
            list.addAll(types.filter { it != "Calculator" })
            if (!types.contains("Calculator") && historyItems.isNotEmpty()) {
                list.add(0, "Calculator")
            }
            list
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableTypes.forEach { type ->
                val isSelected = selectedTypeFilter == type
                val labelText = if (isBn) {
                    when (type) {
                        "Calculator" -> "ক্যালকুলেটর"
                        "BMI Calculator" -> "বিএমআই"
                        "Age Calculator" -> "বয়স"
                        "Discount Calculator" -> "ডিসকাউন্ট"
                        else -> type
                    }
                } else {
                    type
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTypeFilter = type },
                    label = { Text(labelText) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themeColors.buttonEqualBg,
                        selectedLabelColor = Color.White,
                        containerColor = themeColors.cardBg,
                        labelColor = themeColors.displayText
                    ),
                    modifier = Modifier.testTag("history_chip_$type")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "No History",
                        tint = themeColors.displayExpressionText.copy(alpha = 0.4f),
                        modifier = Modifier.size(84.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No History Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Perform some calculations in the calculator screen, and they will appear here automatically as items you can re-use.",
                        fontSize = 13.sp,
                        color = themeColors.displayExpressionText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val filteredItems = historyItems.filter { 
                    // Filter by selected category chip
                    val matchesType = if (selectedTypeFilter == "Calculator") {
                        it.type == "Calculator" || it.type == "Basic" || it.type.isBlank()
                    } else {
                        it.type == selectedTypeFilter
                    }
                    if (!matchesType) return@filter false

                    val eng = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
                    val ben = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
                    var nExpr = it.expression
                    var nRes = it.result
                    var nTag = it.customName ?: ""
                    var nQuery = searchQuery
                    for (i in 0..9) {
                        nExpr = nExpr.replace(ben[i], eng[i])
                        nRes = nRes.replace(ben[i], eng[i])
                        nTag = nTag.replace(ben[i], eng[i])
                        nQuery = nQuery.replace(ben[i], eng[i])
                    }
                    nExpr.contains(nQuery, ignoreCase = true) || 
                    nRes.contains(nQuery, ignoreCase = true) || 
                    nTag.contains(nQuery, ignoreCase = true)
                }.let { 
                    if (isAscending) it.sortedBy { item -> item.timestamp } else it.sortedByDescending { item -> item.timestamp }
                }
                
                @Composable
                fun HighlightedText(text: String, query: String, color: Color, modifier: Modifier = Modifier, fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight? = null, fontFamily: FontFamily? = null) {
                    if (query.isEmpty()) {
                        Text(text = text, color = color, modifier = modifier, fontSize = fontSize, fontWeight = fontWeight, fontFamily = fontFamily, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        return
                    }
                    val eng = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
                    val ben = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
                    var normalizedText = text
                    var normalizedQuery = query
                    for (i in 0..9) {
                        normalizedText = normalizedText.replace(ben[i], eng[i])
                        normalizedQuery = normalizedQuery.replace(ben[i], eng[i])
                    }

                    val startIndex = normalizedText.indexOf(normalizedQuery, ignoreCase = true)
                    if (startIndex >= 0) {
                        val annotated = buildAnnotatedString {
                            append(text.substring(0, startIndex))
                            withStyle(style = SpanStyle(background = Color.Yellow.copy(alpha = 0.6f), color = Color.Black)) {
                                // Important: We need to append the EXACT MATCH from the original text
                                // because the query might be English but original text might be Bengali
                                append(text.substring(startIndex, startIndex + normalizedQuery.length))
                            }
                            append(text.substring(startIndex + normalizedQuery.length))
                        }
                        Text(text = annotated, color = color, modifier = modifier, fontSize = fontSize, fontWeight = fontWeight, fontFamily = fontFamily, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    } else {
                        Text(text = text, color = color, modifier = modifier, fontSize = fontSize, fontWeight = fontWeight, fontFamily = fontFamily, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }

                filteredItems.forEach { entry ->
                    val sdf = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
                    val formattedTime = sdf.format(Date(entry.timestamp))
                    val isSelected = viewModel.selectedHistoryIds.contains(entry.id)

                    val interactionSource = remember { MutableInteractionSource() }
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_item_${entry.id}")
                            .clip(RoundedCornerShape(16.dp))
                            .scaleOnPress(interactionSource)
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = androidx.compose.foundation.LocalIndication.current,
                                onClick = {
                                    if (viewModel.isHistorySelectionMode) {
                                        viewModel.toggleHistorySelection(entry.id)
                                    } else {
                                        viewModel.selectHistoryItem(entry)
                                    }
                                },
                                onLongClick = {
                                    viewModel.isHistorySelectionMode = true
                                    viewModel.toggleHistorySelection(entry.id)
                                }
                            ),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isSelected) themeColors.buttonEqualBg.copy(alpha = 0.12f) else themeColors.cardBg
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isSelected) 0.dp else 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = formattedTime,
                                        fontSize = 11.sp,
                                        color = themeColors.displayExpressionText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (!entry.customName.isNullOrBlank()) {
                                        Surface(
                                            color = themeColors.buttonEqualBg.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            HighlightedText(
                                                text = entry.customName,
                                                query = searchQuery,
                                                fontSize = 11.sp,
                                                color = themeColors.buttonEqualBg,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                HighlightedText(
                                    text = entry.expression,
                                    query = searchQuery,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = themeColors.displayText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val displayResult = try {
                                    val cleaned = entry.result.replace(",", "")
                                    val doubleVal = cleaned.toDouble()
                                    val precision = viewModel.decimalPrecision
                                    val pattern = if (precision <= 0) "#" else "#." + "#".repeat(precision)
                                    java.text.DecimalFormat(pattern).format(doubleVal)
                                } catch (e: Exception) {
                                    entry.result
                                }
                                Text(
                                    text = "= $displayResult",
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg, // Higher contrast color
                                    maxLines = 1
                                )
                            }
                            
                            if (viewModel.isHistorySelectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleHistorySelection(entry.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = themeColors.buttonEqualBg)
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        viewModel.pendingDeleteId = entry.id
                                        viewModel.showDeleteSingleDialog = true
                                    },
                                    modifier = Modifier.testTag("delete_item_${entry.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Item",
                                        tint = themeColors.displayText.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (viewModel.showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showClearHistoryDialog = false },
            title = { Text("Clear All History", color = themeColors.displayText) },
            text = { Text("Are you sure you want to delete your entire calculation history?", color = themeColors.displayText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        viewModel.showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.showClearHistoryDialog = false }
                ) {
                    Text("Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg
        )
    }

    if (viewModel.showDeleteSingleDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.showDeleteSingleDialog = false
                viewModel.pendingDeleteId = null
            },
            title = { Text("Delete History Item", color = themeColors.displayText) },
            text = { Text("Are you sure you want to delete this calculation from your history?", color = themeColors.displayText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.pendingDeleteId?.let { id ->
                            viewModel.deleteHistoryItem(id)
                        }
                        viewModel.showDeleteSingleDialog = false
                        viewModel.pendingDeleteId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.showDeleteSingleDialog = false
                        viewModel.pendingDeleteId = null
                    }
                ) {
                    Text("Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg
        )
    }

    val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI

    if (viewModel.showBackupConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showBackupConfirmDialog = false },
            title = { Text(if (isBn) "হিস্টোরি ব্যাকআপ করুন" else "Backup History", color = themeColors.displayText) },
            text = { Text(if (isBn) "আপনি কি সম্পূর্ণ ক্যালকুলেশন হিস্টোরি একটি JSON ফাইল হিসেবে সংরক্ষণ করতে চান?" else "Do you want to export your complete calculation history to a JSON file?", color = themeColors.displayText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showBackupConfirmDialog = false
                        backupLauncher.launch("calculator_history_backup.json")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(if (isBn) "ব্যাকআপ করুন" else "Backup", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.showBackupConfirmDialog = false }
                ) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg
        )
    }

    if (viewModel.showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showRestoreConfirmDialog = false },
            title = { Text(if (isBn) "হিস্টোরি রিস্টোর করুন" else "Restore History", color = themeColors.displayText) },
            text = { Text(if (isBn) "আপনি কি একটি JSON ফাইল থেকে পূর্বের হিস্টোরি রিস্টোর করতে চান? এটি বর্তমান তালিকায় নতুন এন্ট্রিগুলো যুক্ত করবে।" else "Do you want to restore calculations from a JSON file? This will import and append them to your current list.", color = themeColors.displayText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showRestoreConfirmDialog = false
                        restoreLauncher.launch(arrayOf("application/json", "application/octet-stream"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(if (isBn) "রিস্টোর করুন" else "Restore", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.showRestoreConfirmDialog = false }
                ) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = themeColors.displayText)
                }
            },
            containerColor = themeColors.cardBg
        )
    }

    if (viewModel.showBackupStatusDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showBackupStatusDialog = false },
            title = { Text(if (isBn) "অবস্থা" else "Status", color = themeColors.displayText) },
            text = { Text(viewModel.backupStatusMessage, color = themeColors.displayText) },
            confirmButton = {
                Button(
                    onClick = { viewModel.showBackupStatusDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg)
                ) {
                    Text(if (isBn) "ঠিক আছে" else "OK", color = Color.White)
                }
            },
            containerColor = themeColors.cardBg
        )
    }
    }
}

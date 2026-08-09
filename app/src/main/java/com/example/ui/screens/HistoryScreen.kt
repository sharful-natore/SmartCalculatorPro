package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    val historyItems by viewModel.historyList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (viewModel.isHistorySelectionMode) "${viewModel.selectedHistoryIds.size} Selected" else "Calculation History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
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
            } else if (historyItems.isNotEmpty()) {
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

        Spacer(modifier = Modifier.height(16.dp))

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
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyItems) { entry ->
                    val sdf = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
                    val formattedTime = sdf.format(Date(entry.timestamp))
                    val isSelected = viewModel.selectedHistoryIds.contains(entry.id)

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_item_${entry.id}")
                            .clip(RoundedCornerShape(16.dp))
                            .combinedClickable(
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
                                Text(
                                    text = formattedTime,
                                    fontSize = 11.sp,
                                    color = themeColors.displayExpressionText,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = entry.expression,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = themeColors.displayText,
                                    maxLines = 1
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
}

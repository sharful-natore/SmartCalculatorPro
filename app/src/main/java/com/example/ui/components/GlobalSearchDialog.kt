package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConverterType
import com.example.data.model.ToolType
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage

sealed class SearchResult(
    val title: String,
    val subTitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
) {
    class Converter(val type: ConverterType, language: AppLanguage, val onAction: () -> Unit) : SearchResult(
        title = if (language == AppLanguage.BENGALI) type.titleBn else type.titleEn,
        subTitle = if (language == AppLanguage.BENGALI) "কনভার্টার" else "Converter",
        icon = type.icon,
        onClick = onAction
    )
    class Tool(val type: ToolType, language: AppLanguage, val onAction: () -> Unit) : SearchResult(
        title = if (language == AppLanguage.BENGALI) type.titleBn else type.titleEn,
        subTitle = if (language == AppLanguage.BENGALI) "টুল" else "Tool",
        icon = type.icon,
        onClick = onAction
    )
    class History(val expression: String, val result: String, val onAction: () -> Unit) : SearchResult(
        title = expression,
        subTitle = result,
        icon = Icons.Default.History,
        onClick = onAction
    )
}

@Composable
fun GlobalSearchDialog(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors
) {
    if (!viewModel.showGlobalSearch) return

    androidx.activity.compose.BackHandler {
        viewModel.showGlobalSearch = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = themeColors.background
    ) {
        val language = viewModel.selectedLanguage
        var searchQuery by remember { mutableStateOf("") }
        val historyItems by viewModel.historyList.collectAsState()

        val searchResults = remember(searchQuery, historyItems) {
            if (searchQuery.isBlank()) emptyList<SearchResult>()
            else {
                val query = searchQuery.trim()
                val results = mutableListOf<SearchResult>()

                val eng = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
                val ben = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
                var normalizedQuery = query
                for (i in 0..9) {
                    normalizedQuery = normalizedQuery.replace(ben[i], eng[i])
                }

                // Search Converters
                ConverterType.values().forEach { type ->
                    if (type.titleEn.contains(query, ignoreCase = true) || type.titleBn.contains(query, ignoreCase = true)) {
                        results.add(SearchResult.Converter(type, language) {
                            viewModel.selectedConverterType = type
                            viewModel.activeTab = 1
                            viewModel.showGlobalSearch = false
                        })
                    }
                }

                // Search Tools
                ToolType.values().forEach { type ->
                    if (type.titleEn.contains(query, ignoreCase = true) || type.titleBn.contains(query, ignoreCase = true)) {
                        results.add(SearchResult.Tool(type, language) {
                            viewModel.selectedToolCategoryFilter = null
                            viewModel.selectedToolType = type
                            viewModel.activeTab = 2
                            viewModel.showGlobalSearch = false
                        })
                    }
                }

                // Search History
                historyItems.forEach { item ->
                    var nExpr = item.expression
                    var nRes = item.result
                    for (i in 0..9) {
                        nExpr = nExpr.replace(ben[i], eng[i])
                        nRes = nRes.replace(ben[i], eng[i])
                    }
                    
                    if (nExpr.contains(normalizedQuery, ignoreCase = true) || nRes.contains(normalizedQuery, ignoreCase = true)) {
                        results.add(SearchResult.History(item.expression, item.result) {
                            viewModel.selectHistoryItem(item)
                            viewModel.activeTab = 0
                            viewModel.showGlobalSearch = false
                        })
                    }
                }

                results
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Search Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.showGlobalSearch = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = themeColors.displayText)
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            if (language == AppLanguage.BENGALI) "সার্চ করুন..." else "Search everything...",
                            color = themeColors.displayText.copy(alpha = 0.5f)
                        ) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedBorderColor = themeColors.buttonEqualBg,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.3f),
                        cursorColor = themeColors.buttonEqualBg
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = themeColors.displayText)
                            }
                        }
                    }
                )
            }

            // Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (searchQuery.isBlank()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                            Text(
                                if (language == AppLanguage.BENGALI) "আপনার প্রয়োজনীয় কিছু খুঁজুন" else "Find what you need",
                                color = themeColors.displayText.copy(alpha = 0.5f),
                                fontSize = 18.sp
                            )
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                            Text(
                                if (language == AppLanguage.BENGALI) "কোনো ফলাফল পাওয়া যায়নি" else "No results found",
                                color = themeColors.displayText.copy(alpha = 0.5f),
                                fontSize = 18.sp
                            )
                        }
                    }
                } else {
                    items(searchResults) { result ->
                        SearchResultItem(result, searchQuery, themeColors)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    result: SearchResult,
    query: String,
    themeColors: CalculatorThemeColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { result.onClick() },
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(themeColors.buttonEqualBg.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(result.icon, contentDescription = null, tint = themeColors.buttonEqualBg, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = buildAnnotatedString {
                        appendWithHighlight(result.title, query, themeColors.buttonEqualBg.copy(alpha = 0.3f))
                    },
                    color = themeColors.displayText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = buildAnnotatedString {
                        appendWithHighlight(result.subTitle, query, themeColors.buttonEqualBg.copy(alpha = 0.3f))
                    },
                    color = themeColors.displayText.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun AnnotatedString.Builder.appendWithHighlight(text: String, query: String, highlightColor: Color) {
    if (query.isEmpty()) {
        append(text)
        return
    }

    val eng = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    val ben = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
    var normalizedText = text.lowercase()
    var normalizedQuery = query.lowercase()
    for (i in 0..9) {
        normalizedText = normalizedText.replace(ben[i], eng[i])
        normalizedQuery = normalizedQuery.replace(ben[i], eng[i])
    }
    
    var lastIndex = 0
    var index = normalizedText.indexOf(normalizedQuery, lastIndex)
    
    while (index != -1) {
        append(text.substring(lastIndex, index))
        withStyle(style = SpanStyle(background = highlightColor)) {
            append(text.substring(index, index + normalizedQuery.length))
        }
        lastIndex = index + normalizedQuery.length
        index = normalizedText.indexOf(normalizedQuery, lastIndex)
    }
    append(text.substring(lastIndex))
}

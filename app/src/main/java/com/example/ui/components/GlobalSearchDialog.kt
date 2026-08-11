package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
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

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val dialogWidth = (screenWidth * 0.92f).coerceAtMost(500.dp)
    val dialogHeight = (configuration.screenHeightDp.dp * 0.85f)

    Dialog(
        onDismissRequest = { viewModel.showGlobalSearch = false },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        androidx.activity.compose.BackHandler {
            viewModel.showGlobalSearch = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { viewModel.showGlobalSearch = false },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(dialogWidth)
                    .height(dialogHeight)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(32.dp),
                color = Color.White, // As per image design
                tonalElevation = 8.dp
            ) {
                val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header with Title and Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.BENGALI) "স্মার্ট অনুসন্ধান" else "Smart Search",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2D3142),
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == AppLanguage.BENGALI) "সব তথ্য এক জায়গায় খুঁজুন" else "Find everything in one place",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F3F9))
                                .clickable { viewModel.showGlobalSearch = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF4A4E69),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Search Input Field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .border(2.dp, Color(0xFF7A86E1).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF7A86E1),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    color = Color(0xFF2D3142),
                                    fontWeight = FontWeight.Medium
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = if (language == AppLanguage.BENGALI) "টুল, কনভার্টার বা হিস্ট্রি খুঁজুন..." else "Search tools, converters or history...",
                                            fontSize = 18.sp,
                                            color = Color.Gray.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Content Area (Results or Empty State)
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isBlank()) {
                            // Initial Empty State
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AsyncImage(
                                    model = R.drawable.ic_search_empty,
                                    contentDescription = null,
                                    modifier = Modifier.size(140.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = if (language == AppLanguage.BENGALI) "খুঁজতে টাইপ করুন..." else "Type to search...",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (language == AppLanguage.BENGALI) 
                                        "যেমন: হিস্ট্রি, বিএমআই, বা তাপমাত্রা" 
                                        else "Example: History, BMI, or Temperature",
                                    fontSize = 14.sp,
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (searchResults.isEmpty()) {
                            // No Results State
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.Gray.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (language == AppLanguage.BENGALI) "কোনো ফলাফল পাওয়া যায়নি" else "No results found",
                                    color = Color.Gray,
                                    fontSize = 18.sp
                                )
                            }
                        } else {
                            // Results List
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(searchResults) { result ->
                                    SearchResultItem(result, searchQuery, themeColors)
                                }
                            }
                        }
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

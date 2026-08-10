import re

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """
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
import androidx.compose.material.icons.filled.Close
import com.example.util.AppLanguage
"""
content = content.replace('import java.util.*', 'import java.util.*\n' + imports)

# Update HistoryScreen signature to include variables
state_vars = """
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isAscending by remember { mutableStateOf(false) }
"""
content = re.sub(r'val historyItems by viewModel.historyList.collectAsState\(\)', 
                 r'val historyItems by viewModel.historyList.collectAsState()\n' + state_vars, content)


header_old = """
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
"""

header_new = """
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
                        IconButton(onClick = { 
                            if (searchQuery.isNotEmpty()) {
                                searchQuery = ""
                            } else {
                                isSearchActive = false 
                            }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Search", modifier = Modifier.size(20.dp))
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
"""
content = content.replace(header_old, header_new)

icons_old = """
                    // Backup Button
"""
icons_new = """
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
                        IconButton(
                            onClick = { isAscending = !isAscending }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = if (isAscending) themeColors.buttonEqualBg else themeColors.buttonEqualBg.copy(alpha=0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    // Backup Button
"""
content = content.replace(icons_old, icons_new)

# Update mapping and sorting
# We need to find: `historyItems.forEach { entry ->`
loop_old = """
                historyItems.forEach { entry ->
"""
loop_new = """
                val filteredItems = historyItems.filter { 
                    it.expression.contains(searchQuery, ignoreCase = true) || 
                    it.result.contains(searchQuery, ignoreCase = true) || 
                    (it.tag?.contains(searchQuery, ignoreCase = true) == true)
                }.let { 
                    if (isAscending) it.sortedBy { item -> item.timestamp } else it.sortedByDescending { item -> item.timestamp }
                }
                
                @Composable
                fun HighlightedText(text: String, query: String, color: Color, modifier: Modifier = Modifier, fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight? = null) {
                    if (query.isEmpty()) {
                        Text(text = text, color = color, modifier = modifier, fontSize = fontSize, fontWeight = fontWeight, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        return
                    }
                    val startIndex = text.indexOf(query, ignoreCase = true)
                    if (startIndex >= 0) {
                        val annotated = buildAnnotatedString {
                            append(text.substring(0, startIndex))
                            withStyle(style = SpanStyle(background = Color.Yellow.copy(alpha = 0.5f))) {
                                append(text.substring(startIndex, startIndex + query.length))
                            }
                            append(text.substring(startIndex + query.length))
                        }
                        Text(text = annotated, color = color, modifier = modifier, fontSize = fontSize, fontWeight = fontWeight, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    } else {
                        Text(text = text, color = color, modifier = modifier, fontSize = fontSize, fontWeight = fontWeight, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }

                filteredItems.forEach { entry ->
"""
content = content.replace(loop_old, loop_new)

# Replace normal text with HighlightedText
content = content.replace(
    '''Text(
                                    text = entry.expression,
                                    fontSize = 16.sp,
                                    color = themeColors.displayExpressionText,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )''',
    '''HighlightedText(
                                    text = entry.expression,
                                    query = searchQuery,
                                    color = themeColors.displayExpressionText,
                                    fontSize = 16.sp
                                )'''
)
content = content.replace(
    '''Text(
                                    text = entry.result,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )''',
    '''HighlightedText(
                                    text = entry.result,
                                    query = searchQuery,
                                    color = themeColors.displayText,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )'''
)
content = content.replace(
    '''Text(
                                    text = entry.tag!!,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.buttonEqualBg, // Higher contrast color
                                    maxLines = 1
                                )''',
    '''HighlightedText(
                                    text = entry.tag!!,
                                    query = searchQuery,
                                    color = themeColors.buttonEqualBg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )'''
)


with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'w') as f:
    f.write(content)

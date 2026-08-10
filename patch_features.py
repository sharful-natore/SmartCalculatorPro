import re

# 1. Update BasicScientificScreen.kt
with open('app/src/main/java/com/example/ui/screens/BasicScientificScreen.kt', 'r') as f:
    sci_content = f.read()

old_drag_handle = """        // Drag Handle / Visual cue for swiping
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .then(dragModifier)
                .clickable {
                    coroutineScope.launch {
                        if (viewModel.isScientificExpanded) {
                            expansionAnimatable.animateTo(0f)
                            viewModel.isScientificExpanded = false
                        } else {
                            expansionAnimatable.animateTo(1f)
                            viewModel.isScientificExpanded = true
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = buttonPadding)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(themeColors.displayText.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Hide Scientific Mode" else "Show Scientific Mode",
                    tint = themeColors.displayText.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }"""

new_drag_handle = """        // Drag Handle / Visual cue for swiping
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .then(dragModifier)
                .clickable {
                    coroutineScope.launch {
                        if (viewModel.isScientificExpanded) {
                            expansionAnimatable.animateTo(0f)
                            viewModel.isScientificExpanded = false
                        } else {
                            expansionAnimatable.animateTo(1f)
                            viewModel.isScientificExpanded = true
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = buttonPadding)
                    .clip(RoundedCornerShape(8.dp))
                    .background(androidx.compose.ui.graphics.Color.Transparent)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Hide Scientific Mode" else "Show Scientific Mode",
                    tint = themeColors.displayText.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }"""

if old_drag_handle in sci_content:
    sci_content = sci_content.replace(old_drag_handle, new_drag_handle)
    with open('app/src/main/java/com/example/ui/screens/BasicScientificScreen.kt', 'w') as f:
        f.write(sci_content)
    print("BasicScientificScreen.kt updated successfully.")
else:
    print("Could not find Drag Handle in BasicScientificScreen.kt")

# 2. Update HistoryScreen.kt
with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'r') as f:
    hist_content = f.read()

old_filter_code = """                val filteredItems = historyItems.filter { 
                    it.expression.contains(searchQuery, ignoreCase = true) || 
                    it.result.contains(searchQuery, ignoreCase = true) || 
                    (it.customName?.contains(searchQuery, ignoreCase = true) == true)
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
                }"""

new_filter_code = """                val filteredItems = historyItems.filter { 
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
                fun HighlightedText(text: String, query: String, color: Color, modifier: Modifier = Modifier, fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight? = null) {
                    if (query.isEmpty()) {
                        Text(text = text, color = color, modifier = modifier, fontSize = fontSize, fontWeight = fontWeight, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
                            withStyle(style = SpanStyle(background = Color.Yellow.copy(alpha = 0.5f), color = Color.Black)) {
                                append(text.substring(startIndex, startIndex + query.length))
                            }
                            append(text.substring(startIndex + query.length))
                        }
                        Text(text = annotated, color = color, modifier = modifier, fontSize = fontSize, fontWeight = fontWeight, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    } else {
                        Text(text = text, color = color, modifier = modifier, fontSize = fontSize, fontWeight = fontWeight, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }"""

if old_filter_code in hist_content:
    hist_content = hist_content.replace(old_filter_code, new_filter_code)
    with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'w') as f:
        f.write(hist_content)
    print("HistoryScreen.kt updated successfully.")
else:
    print("Could not find Filter Code in HistoryScreen.kt")


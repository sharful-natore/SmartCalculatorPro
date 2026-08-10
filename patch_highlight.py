import re

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'r') as f:
    content = f.read()

old_highlight = """                @Composable
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

new_highlight = """                @Composable
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
                }"""

content = content.replace(old_highlight, new_highlight)

# Update Text to HighlightedText for customName
old_custom_name = """                                            Text(
                                                text = entry.customName,
                                                fontSize = 11.sp,
                                                color = themeColors.buttonEqualBg,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )"""
new_custom_name = """                                            HighlightedText(
                                                text = entry.customName,
                                                query = searchQuery,
                                                fontSize = 11.sp,
                                                color = themeColors.buttonEqualBg,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )"""
content = content.replace(old_custom_name, new_custom_name)

# Update Text to HighlightedText for expression
old_expression = """                                Text(
                                    text = entry.expression,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = themeColors.displayText,
                                    maxLines = 1
                                )"""
new_expression = """                                HighlightedText(
                                    text = entry.expression,
                                    query = searchQuery,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = themeColors.displayText
                                )"""
content = content.replace(old_expression, new_expression)

# Update Text to HighlightedText for result
old_result = """                                Text(
                                    text = "= $displayResult",
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = themeColors.displayText,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )"""
new_result = """                                HighlightedText(
                                    text = "= $displayResult",
                                    query = searchQuery,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = themeColors.displayText,
                                    fontWeight = FontWeight.Bold
                                )"""
content = content.replace(old_result, new_result)

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'w') as f:
    f.write(content)
print("Updated History HighlightedText")

import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

target = """                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) "এআইএস সিভি বিল্ডার" else "ATS CV Builder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = cvData.profileLabel.ifBlank { if (isBn) "স্মার্ট এআই এবং প্রফেশনাল টেমপ্লেট" else "Smart AI & Professional Templates" },
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
                // Undo Button
                IconButton(
                    onClick = { performUndo() },
                    enabled = undoStack.isNotEmpty(),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (undoStack.isNotEmpty()) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.3f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                // Redo Button
                IconButton(
                    onClick = { performRedo() },
                    enabled = redoStack.isNotEmpty(),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (redoStack.isNotEmpty()) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.3f),
                        modifier = Modifier.size(22.dp)
                    )
                }"""

replacement = """                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) "এটিএস সিভি বিল্ডার" else "ATS CV Builder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = cvData.profileLabel.ifBlank { "Default" },
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
                // Undo Button
                IconButton(
                    onClick = { performUndo() },
                    enabled = undoStack.isNotEmpty(),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (undoStack.isNotEmpty()) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.3f),
                        modifier = Modifier.size(22.dp)
                    )
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success fixing header")
else:
    print("Target not found for header")

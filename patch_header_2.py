import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

content = content.replace('if (isBn) "এআইএস সিভি বিল্ডার" else "ATS CV Builder"', 'if (isBn) "এটিএস সিভি বিল্ডার" else "ATS CV Builder"')

target_redo = """                // Redo Button
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

if target_redo in content:
    content = content.replace(target_redo, "")
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success fixing header 2")
else:
    print("Target not found for header 2")

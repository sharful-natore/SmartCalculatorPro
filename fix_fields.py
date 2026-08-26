import re
with open("app/src/main/java/com/example/ui/screens/tools/PdfTools.kt", "r") as f:
    content = f.read()

# Replace the broken colors blocks
bad_pattern = r'colors = OutlinedTextFieldDefaults\.colors\(\),\s*focusedTextColor =.*?\n\s*\),'
good_colors = """colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                cursorColor = themeColors.buttonEqualBg,
                                focusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                unfocusedContainerColor = themeColors.buttonEqualBg.copy(alpha = 0.04f),
                                focusedBorderColor = themeColors.buttonEqualBg,
                                unfocusedBorderColor = themeColors.buttonEqualBg.copy(alpha = 0.3f)
                            ),"""

content = re.sub(bad_pattern, good_colors, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/tools/PdfTools.kt", "w") as f:
    f.write(content)

import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

target = """                            .border(1.5.dp, themeColors.buttonEqualBg, shape = when (cvData.photoShape) {"""

replacement = """                            .border(3.5.dp, Color(android.graphics.Color.parseColor(cvData.templateStyle.primaryColorHex)), shape = when (cvData.photoShape) {"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success UI border")
else:
    print("Target not found for UI border")

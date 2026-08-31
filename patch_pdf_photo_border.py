import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

target = """        val borderPaint = Paint().apply {
            color = AndroidColor.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }"""

replacement = """        val borderPaint = Paint().apply {
            color = AndroidColor.parseColor(data.templateStyle.primaryColorHex)
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
            isAntiAlias = true
        }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success PDF border")
else:
    print("Target not found for PDF border")

import sys
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

target = '    fun drawSectionHeader(title: String, defaultIconKey: String = "") {'
replacement = """    var isFirstSection = true
    fun drawSectionHeader(title: String, defaultIconKey: String = "") {
        if (!isFirstSection) currentY += sectionGap
        isFirstSection = false"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Patched section gap")
else:
    print("Could not find drawSectionHeader")

import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

pattern = r'onSaveProfile: \(\) -> Unit = \{\}\)\s*\{\s*val scrollState = rememberScrollState\(\)\s*Column\('
replacement = r'''onSaveProfile: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    var previewMode by remember { mutableStateOf("PREVIEW") } // "PREVIEW" or "LIVE_EDIT"
    Column('''

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(content)
    print("Success injecting target 1")
else:
    print("Target 1 regex not found")

import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

new_content = content.replace('IconButton(\n                    onClick = { showHistoryDialog = true },\n                    modifier = Modifier.size(38.dp)\n                ) {\n                    Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {',
'IconButton(\n                    onClick = { showHistoryDialog = true },\n                    modifier = Modifier.size(46.dp)\n                ) {\n                    Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {')

if new_content != content:
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(new_content)
    print("Fixed history badge bounds")
else:
    print("Could not find history badge bounds")

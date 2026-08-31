file_path = 'app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('.padding(start = 28.dp, vertical = 2.dp)', '.padding(start = 28.dp, top = 2.dp, bottom = 2.dp)')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)

print("Fixed padding!")

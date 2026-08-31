import re

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

# Pattern for StaticLayout.Builder.obtain(...).build()
# We want to replace it with StaticLayout.Builder.obtain(...).setLineSpacing(0f, data.customLineSpacing).build()
content = re.sub(
    r'(StaticLayout\.Builder\.obtain\([^)]+\))(\.build\(\))',
    r'\1.setLineSpacing(0f, data.customLineSpacing)\2',
    content
)

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
    f.write(content)

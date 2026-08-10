import re

with open('app/src/main/java/com/example/ui/screens/BasicScientificScreen.kt', 'r') as f:
    content = f.read()

# Fix the import
content = content.replace('import androidx.compose.ui.Modifier\n', 'import androidx.compose.ui.Modifier\nimport androidx.compose.ui.layout.layout\n')

# Fix the usage
old_usage = ".androidx.compose.ui.layout.layout { measurable, constraints ->"
new_usage = ".layout { measurable, constraints ->"
content = content.replace(old_usage, new_usage)

with open('app/src/main/java/com/example/ui/screens/BasicScientificScreen.kt', 'w') as f:
    f.write(content)

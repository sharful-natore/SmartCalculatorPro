import re

with open('app/src/main/java/com/example/ui/MainApp.kt', 'r') as f:
    content = f.read()

# I will just import graphicsLayer and scale
imports_to_add = "\nimport androidx.compose.ui.graphics.graphicsLayer\nimport androidx.compose.ui.draw.scale\n"
if "import androidx.compose.ui.graphics.graphicsLayer" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier" + imports_to_add)

with open('app/src/main/java/com/example/ui/MainApp.kt', 'w') as f:
    f.write(content)
print("Added imports")

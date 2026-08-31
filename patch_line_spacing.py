import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

# Pattern to find `.build()` after `StaticLayout.Builder.obtain` and insert `.setLineSpacing(0f, data.customLineSpacing)` before it
# However, there might be other calls like `.setAlignment(...)` in between.
# Let's just find `.build()` and if it's on a line with `StaticLayout.Builder.obtain`, we replace it.
lines = content.split('\n')
for i, line in enumerate(lines):
    if 'StaticLayout.Builder.obtain' in line and '.build()' in line:
        lines[i] = line.replace('.build()', '.setLineSpacing(0f, data.customLineSpacing).build()')

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
    f.write('\n'.join(lines))
print("Patched line spacing")

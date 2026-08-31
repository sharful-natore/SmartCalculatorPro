import re
with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

# We need to find the Box that has contentAlignment = Alignment.Center and contains Icons.Default.AccountCircle
# And the Box inside it for the badge, which has .offset(x = 2.dp, y = (-2).dp)
# We will change the parent Box to not clip, or change the IconButton size.
# Actually, the parent IconButton has `modifier = Modifier.size(38.dp)`.
# The Box inside it has `modifier = Modifier.size(38.dp)`.
# The badge Box has `.offset(x = 2.dp, y = (-2).dp)` which might push it outside the 38.dp bounds.
# We can change the IconButton to `Modifier.size(42.dp)` and the inner Box to `Modifier.size(42.dp)`.

new_content = content.replace('IconButton(\n                    onClick = { showProfileManagerDialog = true },\n                    modifier = Modifier.size(38.dp)\n                ) {\n                    Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {',
'IconButton(\n                    onClick = { showProfileManagerDialog = true },\n                    modifier = Modifier.size(46.dp)\n                ) {\n                    Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {')

if new_content != content:
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
        f.write(new_content)
    print("Fixed profile badge bounds")
else:
    print("Could not find profile badge bounds")

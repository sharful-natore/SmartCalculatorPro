import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# Replace the start
content = content.replace(
    "// Time-based Greeting & Multi-Date Header Banner (Hidden during search)\n        if (searchQuery.isBlank()) {\n",
    "// Time-based Greeting & Multi-Date Header Banner\n"
)

# We need to find the matching closing brace. It has this comment:
# } // Closes if (searchQuery.isBlank())
content = content.replace(
    "} // Closes if (searchQuery.isBlank())\n",
    "\n"
)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)


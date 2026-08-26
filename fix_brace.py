import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

bad_string = """            listOf(
                "1. Custom PDF Creator" to "Create professional A4 PDF documents from notes, text, titles, and photos to save or share effortlessly."
            )
        ToolType.QR_BARCODE"""

good_string = """            listOf(
                "1. Custom PDF Creator" to "Create professional A4 PDF documents from notes, text, titles, and photos to save or share effortlessly."
            )
        }
        ToolType.QR_BARCODE"""

content = content.replace(bad_string, good_string)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)

import re

with open("app/src/main/java/com/example/data/model/ToolModels.kt", "r") as f:
    content = f.read()

bad_string = """                QR_BARCODE(
        "QR & Barcode Scanner/Creator", "কিউআর ও বারকোড টুল",
        "যেকোনো বারকোড ও কিউআর কোড ফাস্ট স্ক্যান করা এবং নতুন কিউআর তৈরি করার টুল",
        ToolCategory.UTILITY, Icons.Default.QrCodeScanner
    ),
    PDF_READER"""

good_string = """                QR_BARCODE -> "Ultra-fast QR and Barcode scanner and creator with zero app size impact"
                PDF_READER"""

content = content.replace(bad_string, good_string)

with open("app/src/main/java/com/example/data/model/ToolModels.kt", "w") as f:
    f.write(content)

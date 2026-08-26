import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

qr_info = """        ToolType.QR_BARCODE -> if (isBn) {
            listOf(
                "১. কিউআর স্ক্যানার" to "যেকোনো কিউআর বা বারকোড দ্রুত স্ক্যান করুন এবং ফ্ল্যাশলাইট বা গ্যালারি থেকে ছবি নিয়ে স্ক্যান করার সুবিধা।",
                "২. কিউআর ক্রিয়েটর" to "টেক্সট, ওয়েবসাইট লিঙ্ক, ফোন নাম্বার বা ওয়াইফাই ইনফো দিয়ে নিজের কাস্টম কিউআর কোড তৈরি করুন।"
            )
        } else {
            listOf(
                "1. QR Scanner" to "Quickly scan any QR or Barcode, with flashlight support and ability to scan from gallery images.",
                "2. QR Creator" to "Create custom QR codes using text, website links, phone numbers, or WiFi information."
            )
        }
    }
}"""

content = re.sub(r'        \}\n    \}\n\}\n', qr_info + "\n", content)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)

import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

info_block = """        ToolType.PDF_MAKER -> if (isBn) {
            listOf(
                "১. কাস্টম পিডিএফ মেকার" to "নোটস, শিরোনাম, টেক্সট বা ছবি যুক্ত করে এ৪ (A4) সাইজের প্রফেশনাল পিডিএফ নথি তৈরি, সেভ ও শেয়ার করার সুবিধা।"
            )
        } else {
            listOf(
                "1. Custom PDF Maker" to "Create, save and share professional A4 size PDF documents by adding notes, titles, text or images."
            )
        }
        ToolType.QR_BARCODE -> if (isBn) {
            listOf(
                "১. কিউআর স্ক্যানার" to "যেকোনো কিউআর বা বারকোড দ্রুত স্ক্যান করুন এবং ফ্ল্যাশলাইট বা গ্যালারি থেকে ছবি নিয়ে স্ক্যান করার সুবিধা।",
                "২. কিউআর ক্রিয়েটর" to "টেক্সট, ওয়েবসাইট লিঙ্ক, ফোন নাম্বার বা ওয়াইফাই ইনফো দিয়ে নিজের কাস্টম কিউআর কোড তৈরি করুন।"
            )
        } else {
            listOf(
                "1. QR Scanner" to "Quickly scan any QR or Barcode, with flashlight support and ability to scan from gallery images.",
                "2. QR Creator" to "Create custom QR codes using text, website links, phone numbers, or WiFi information."
            )
        }"""
        
content = content.replace("""        ToolType.PDF_MAKER -> if (isBn) {
            listOf(
                "১. কাস্টম পিডিএফ মেকার" to "নোটস, শিরোনাম, টেক্সট বা ছবি যুক্ত করে এ৪ (A4) সাইজের প্রফেশনাল পিডিএফ নথি তৈরি, সেভ ও শেয়ার করার সুবিধা।"
            )
        } else {
            listOf(
                "1. Custom PDF Maker" to "Create, save and share professional A4 size PDF documents by adding notes, titles, text or images."
            )
        }""", info_block)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)

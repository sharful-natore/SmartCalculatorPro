import re

with open('app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Let's find the exact corrupted section
start_marker = "// Profit & Loss"
end_marker = "// Greetings: Hello/Hi"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx != -1 and end_idx != -1:
    good_content = """// Profit & Loss
            (normalized.contains("লাভ") || normalized.contains("ক্ষতি") || normalized.contains("profit") || normalized.contains("loss") || normalized.contains("বেচলাম") || normalized.contains("বিক্রি")) && numbers.size >= 2 -> {
                val df = DecimalFormat("#,##0.##")
                val cp = numbers[0]
                val sp = numbers[1]
                val diff = sp - cp
                val pct = (diff / cp) * 100.0
                
                replyText = if (isBn) {
                    if (diff >= 0) {
                        "🟢 **লাভ হিসাব (Profit):**\\n\\n" +
                        "• 🛒 **ক্রয়মূল্য (Cost Price):** ৳${df.format(cp)}\\n" +
                        "• 🏷️ **বিক্রয়মূল্য (Selling Price):** ৳${df.format(sp)}\\n" +
                        "• 💰 **মোট লাভ:** **৳${df.format(diff)}**\\n" +
                        "• 📈 **শতকরা লাভ:** **${df.format(pct)}%**"
                    } else {
                        "🔴 **ক্ষতি হিসাব (Loss):**\\n\\n" +
                        "• 🛒 **ক্রয়মূল্য (Cost Price):** ৳${df.format(cp)}\\n" +
                        "• 🏷️ **বিক্রয়মূল্য (Selling Price):** ৳${df.format(sp)}\\n" +
                        "• 💸 **মোট ক্ষতি:** **৳${df.format(-diff)}**\\n" +
                        "• 📉 **শতকরা ক্ষতি:** **${df.format(-pct)}%**"
                    }
                } else {
                    if (diff >= 0) {
                        "🟢 **Profit Analysis:**\\n\\n" +
                        "• 🛒 **Cost Price:** $${df.format(cp)}\\n" +
                        "• 🏷️ **Selling Price:** $${df.format(sp)}\\n" +
                        "• 💰 **Total Profit:** **$${df.format(diff)}**\\n" +
                        "• 📈 **Profit Percentage:** **${df.format(pct)}%**"
                    } else {
                        "🔴 **Loss Analysis:**\\n\\n" +
                        "• 🛒 **Cost Price:** $${df.format(cp)}\\n" +
                        "• 🏷️ **Selling Price:** $${df.format(sp)}\\n" +
                        "• 💸 **Total Loss:** **$${df.format(-diff)}**\\n" +
                        "• 📉 **Loss Percentage:** **${df.format(-pct)}%**"
                    }
                }
                actType = "calculate"
                actLabel = if (isBn) "প্রফিট ক্যালকুলেটরে দেখুন" else "View in Profit Calculator"
                actData = "$cp,$sp"
            }

            // Split Bill
            (normalized.contains("split") || normalized.contains("ভাগ") || normalized.contains("জন") || normalized.contains("people")) && numbers.size >= 2 -> {
                val df = DecimalFormat("#,##0.##")
                val totalBill = numbers[0]
                val peopleCount = numbers[1]
                val perPerson = totalBill / peopleCount
                
                replyText = if (isBn) {
                    "🍕 **বিল ভাগাভাগি (Split Bill):**\\n\\n" +
                    "• 💵 **মোট বিল:** ৳${df.format(totalBill)}\\n" +
                    "• 👥 **মোট জনসংখ্যা:** ${peopleCount.toInt()} জন\\n" +
                    "• 👤 **প্রত্যেকে পরিশোধ করবে:** **৳${df.format(perPerson)}**"
                } else {
                    "🍕 **Split Bill Analysis:**\\n\\n" +
                    "• 💵 **Total Bill:** $${df.format(totalBill)}\\n" +
                    "• 👥 **Total People:** ${peopleCount.toInt()}\\n" +
                    "• 👤 **Per Person Share:** **$${df.format(perPerson)}**"
                }
            }

            """
    
    new_content = content[:start_idx] + good_content + content[end_idx:]
    with open('app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt', 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("Fixed CalculatorViewModel.kt")
else:
    print("Markers not found")

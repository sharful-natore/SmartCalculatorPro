import re

with open('app/src/main/java/com/example/data/model/ToolModels.kt', 'r') as f:
    content = f.read()

# Add to Enum
old_tip = """    TIP(
        "Tip Calculator", "টিপ ও বিল ভাগ",
        "রেস্তোরাঁয় বিল ভাগ ও টিপ দেওয়ার হিসেব",
        ToolCategory.UTILITY, Icons.Default.Restaurant
    ),"""

new_tip = """    TIP(
        "Tip Calculator", "টিপ ও বিল ভাগ",
        "রেস্তোরাঁয় বিল ভাগ ও টিপ দেওয়ার হিসেব",
        ToolCategory.UTILITY, Icons.Default.Restaurant
    ),
    TEXT_COUNTER(
        "Word & Text Counter", "শব্দ ও অক্ষর গণনা",
        "যেকোনো টেক্সট বা প্যারাগ্রাফের শব্দ, অক্ষর ও লাইন গণনা",
        ToolCategory.UTILITY, Icons.Default.FormatQuote
    ),
    PASSWORD_GENERATOR(
        "Password Generator", "পাসওয়ার্ড জেনারেটর",
        "কাস্টমাইজড এবং সুরক্ষিত স্ট্রং পাসওয়ার্ড তৈরি করুন",
        ToolCategory.UTILITY, Icons.Default.Password
    ),"""

content = content.replace(old_tip, new_tip)

# Add to getDescription
old_desc = """                TIP -> "Calculate bill split amount and tip per person"
                ELECTRICITY_BILL -> "Calculate monthly electricity bill from kWh units and tariff\""""

new_desc = """                TIP -> "Calculate bill split amount and tip per person"
                TEXT_COUNTER -> "Count words, characters, and spaces in any text"
                PASSWORD_GENERATOR -> "Generate secure customized passwords"
                ELECTRICITY_BILL -> "Calculate monthly electricity bill from kWh units and tariff\""""

content = content.replace(old_desc, new_desc)

with open('app/src/main/java/com/example/data/model/ToolModels.kt', 'w') as f:
    f.write(content)
print("Updated ToolModels.kt")

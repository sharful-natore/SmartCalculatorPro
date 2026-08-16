import re

with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Replace Qaza Rule
old_qaza = """        PrayerRule(
            id = "qaza",
            titleBn = "কাজা নামাজ (Qaza)",
            titleEn = "Missed Prayer",
            category = "Special",
            introductionBn = "অনিচ্ছাকৃতভাবে নামাজ ছুটে গেলে তা দ্রুত কাজা আদায় করা আবশ্যক।",
            rakatsCountBn = "শুধুমাত্র ফরজ ও বিতর",
            steps = listOf(
                PrayerStep(
                    1, "কাজা আদায়ের নিয়ম", "Qaza Procedure",
                    "শুধুমাত্র ফরজ ও বিতর নামাজের কাজা করতে হয়, সুন্নতের কাজা নেই। কয়েক ওয়াক্তের কাজা থাকলে ধারাবাহিকভাবে আদায় করুন। বেশি দিনের ছুটে যাওয়া কাজা থাকলে 'উমরী কাজা' হিসেবে ক্রমান্বয়ে আদায় করতে থাকুন।",
                    postureType = PostureType.NIYYAT
                )
            )
        )"""

new_qaza = """        PrayerRule(
            id = "qaza",
            titleBn = "কাজা নামাজ (Qaza)",
            titleEn = "Missed Prayer",
            category = "Special",
            introductionBn = "অনিচ্ছাকৃত বা ইচ্ছাকৃতভাবে নামাজ ছুটে গেলে তার কাজা আদায় করা ফরজ। তবে সুন্নতের কোনো কাজা নেই।",
            rakatsCountBn = "শুধুমাত্র ফরজ ও বিতর",
            extraNotesBn = "দীর্ঘদিনের নামাজ ছুটে গেলে 'উমরী কাজা' হিসেবে ক্রমান্বয়ে আদায় করতে থাকুন।",
            steps = listOf(
                PrayerStep(
                    1, "কাজার নিয়ত", "Qaza Niyyat",
                    "নিয়ত করুন: 'আমার জীবনের ছুটে যাওয়া প্রথম/সর্বশেষ ফজরের (বা অন্য ওয়াক্তের) কাজা নামাজ আদায় করছি।'",
                    postureType = PostureType.NIYYAT
                ),
                PrayerStep(
                    2, "ধারাবাহিকতা", "Sequence",
                    "যদি ৬ ওয়াক্তের কম নামাজ কাজা থাকে, তবে বর্তমান ওয়াক্তের নামাজ পড়ার আগে কাজা নামাজ পড়ে নেওয়া ওয়াজিব (তবে বর্তমান ওয়াক্তের সময় কম থাকলে আগে বর্তমান নামাজ পড়তে হবে)।",
                    postureType = PostureType.QIYAM
                ),
                PrayerStep(
                    3, "উমরী কাজা", "Umari Qaza",
                    "জীবনে অনেক নামাজ ছুটে গিয়ে থাকলে প্রতিদিন বর্তমান নামাজের সাথে সাথে একটি করে কাজা নামাজ আদায় করার চেষ্টা করুন। শুধুমাত্র ফরজ এবং বিতর নামাজ পড়তে হবে।",
                    postureType = PostureType.DUA_GENERIC
                )
            )
        )"""
content = content.replace(old_qaza, new_qaza)

with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "w", encoding="utf-8") as f:
    f.write(content)


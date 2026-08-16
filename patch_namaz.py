import re

with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Add to WaqtInfo
old_waqt = """data class WaqtInfo(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val timeDescriptionBn: String,
    val totalRakats: Int,
    val extraNotesBn: String? = null,
    val steps2Rakat: List<PrayerStep>? = null,
    val steps3Rakat: List<PrayerStep>? = null,
    val steps4Rakat: List<PrayerStep>? = null
)"""

new_waqt = """data class WaqtInfo(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val timeDescriptionBn: String,
    val totalRakat: Int,
    val farz: Int,
    val sunnatMuakkadah: Int,
    val sunnatGairMuakkadah: Int = 0,
    val witr: Int = 0,
    val nafl: Int = 0,
    val arabicName: String = "",
    val descriptionBn: String = "",
    val extraNotesBn: String? = null,
    val steps2Rakat: List<PrayerStep>? = null,
    val steps3Rakat: List<PrayerStep>? = null,
    val steps4Rakat: List<PrayerStep>? = null
)"""
content = content.replace(old_waqt, new_waqt)

# Add to PrayerRule
old_rule = """data class PrayerRule(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val category: String,
    val introductionBn: String,
    val rakatsCountBn: String,
    val specialTakbeerCountBn: String? = null,
    val extraNotesBn: String? = null,
    val steps: List<PrayerStep>
)"""
new_rule = """data class PrayerRule(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val category: String,
    val introductionBn: String,
    val rakatsCountBn: String,
    val specialTakbeerCountBn: String? = null,
    val extraNotesBn: String? = null,
    val khutbahNoteBn: String? = null,
    val steps: List<PrayerStep>
)"""
content = content.replace(old_rule, new_rule)

# Add wuduBreakersList
wudu_breakers = """    val wuduBreakersList = listOf(
        "১. মল-মূত্র বা বায়ু নির্গত হওয়া।",
        "২. শরীর থেকে রক্ত, পুঁজ বা পানি বের হয়ে গড়িয়ে পড়া।",
        "৩. মুখ ভরে বমি হওয়া।",
        "৪. থুথুর সাথে রক্তের ভাগ সমান বা বেশি হওয়া।",
        "৫. কোনো কিছুর সাথে ঠেস দিয়ে বা শুয়ে ঘুমিয়ে পড়া।",
        "৬. অজ্ঞান হওয়া বা পাগল হয়ে যাওয়া।"
    )
"""
content = content.replace("val ghuslFarzList", wudu_breakers + "\n    val ghuslFarzList")

# Replace Waqt initializations
waqts = """val dailyWaqts = listOf(
        WaqtInfo(
            "fajr", "ফজর", "Fajr", "ভোরবেলা (সুবহে সাদিক থেকে সূর্যোদয়ের পূর্ব পর্যন্ত)", 4,
            "২ রাকাত সুন্নতে মুয়াক্কাদাহ এবং ২ রাকাত ফরজ।",
            steps2Rakat = get2RakatSteps("ফজর")
        ),
        WaqtInfo(
            "dhuhr", "জোহর", "Dhuhr", "দুপুরবেলা (সূর্য পশ্চিম দিকে হেলে পড়ার পর থেকে আসরের ওয়াক্তের পূর্ব পর্যন্ত)", 10,
            "৪ রাকাত সুন্নতে মুয়াক্কাদাহ, ৪ রাকাত ফরজ এবং শেষে ২ রাকাত সুন্নতে মুয়াক্কাদাহ।",
            steps4Rakat = get4RakatSteps("জোহর"),
            steps2Rakat = get2RakatSteps("জোহর সুন্নত")
        ),
        WaqtInfo(
            "asr", "আসর", "Asr", "বিকালবেলা (জোহরের ওয়াক্ত শেষ হওয়ার পর থেকে সূর্যাস্তের পূর্ব পর্যন্ত)", 4,
            "৪ রাকাত ফরজ। (এর আগে ৪ রাকাত গায়রে মুয়াক্কাদাহ সুন্নত পড়া যায়)।",
            steps4Rakat = get4RakatSteps("আসর")
        ),
        WaqtInfo(
            "maghrib", "মাগরিব", "Maghrib", "সন্ধ্যাবেলা (সূর্যাস্তের পর থেকে গোধূলি মিলিয়ে যাওয়া পর্যন্ত)", 5,
            "৩ রাকাত ফরজ এবং এরপর ২ রাকাত সুন্নতে মুয়াক্কাদাহ।",
            steps3Rakat = get3RakatSteps("মাগরিব"),
            steps2Rakat = get2RakatSteps("মাগরিব সুন্নত")
        ),
        WaqtInfo(
            "isha", "এশা", "Isha", "রাত্রিবেলা (মাগরিবের ওয়াক্ত শেষ হওয়ার পর থেকে সুবহে সাদিকের পূর্ব পর্যন্ত)", 9,
            "৪ রাকাত ফরজ, ২ রাকাত সুন্নতে মুয়াক্কাদাহ এবং শেষে ৩ রাকাত বিতর ওয়াজিব।",
            steps4Rakat = get4RakatSteps("এশা"),
            steps2Rakat = get2RakatSteps("এশা সুন্নত"),
            steps3Rakat = getWitr3RakatSteps()
        )
    )"""

new_waqts = """val dailyWaqts = listOf(
        WaqtInfo(
            "fajr", "ফজর", "Fajr", "ভোরবেলা (সুবহে সাদিক থেকে সূর্যোদয়ের পূর্ব পর্যন্ত)", 4, 2, 2, 0, 0, 0, "صلاة الفجر", "ফজর নামাজ মোট ৪ রাকাত। প্রথমে ২ রাকাত সুন্নতে মুয়াক্কাদাহ, এরপর ২ রাকাত ফরজ।",
            "২ রাকাত সুন্নতে মুয়াক্কাদাহ এবং ২ রাকাত ফরজ।",
            steps2Rakat = get2RakatSteps("ফজর")
        ),
        WaqtInfo(
            "dhuhr", "জোহর", "Dhuhr", "দুপুরবেলা (সূর্য পশ্চিম দিকে হেলে পড়ার পর থেকে আসরের ওয়াক্তের পূর্ব পর্যন্ত)", 10, 4, 6, 0, 0, 2, "صلاة الظهر", "জোহর নামাজ মোট ১০ রাকাত (নফলসহ ১২ রাকাত)। প্রথমে ৪ রাকাত সুন্নতে মুয়াক্কাদাহ, তারপর ৪ রাকাত ফরজ, শেষে ২ রাকাত সুন্নতে মুয়াক্কাদাহ।",
            "৪ রাকাত সুন্নতে মুয়াক্কাদাহ, ৪ রাকাত ফরজ এবং শেষে ২ রাকাত সুন্নতে মুয়াক্কাদাহ।",
            steps4Rakat = get4RakatSteps("জোহর"),
            steps2Rakat = get2RakatSteps("জোহর সুন্নত")
        ),
        WaqtInfo(
            "asr", "আসর", "Asr", "বিকালবেলা (জোহরের ওয়াক্ত শেষ হওয়ার পর থেকে সূর্যাস্তের পূর্ব পর্যন্ত)", 4, 4, 0, 4, 0, 0, "صلاة العصر", "আসর নামাজ মোট ৪ রাকাত ফরজ। (তবে ফরজের আগে ৪ রাকাত গায়রে মুয়াক্কাদাহ সুন্নত পড়া উত্তম)।",
            "৪ রাকাত ফরজ। (এর আগে ৪ রাকাত গায়রে মুয়াক্কাদাহ সুন্নত পড়া যায়)।",
            steps4Rakat = get4RakatSteps("আসর")
        ),
        WaqtInfo(
            "maghrib", "মাগরিব", "Maghrib", "সন্ধ্যাবেলা (সূর্যাস্তের পর থেকে গোধূলি মিলিয়ে যাওয়া পর্যন্ত)", 5, 3, 2, 0, 0, 2, "صلاة المغرب", "মাগরিব নামাজ মোট ৫ রাকাত (নফলসহ ৭ রাকাত)। প্রথমে ৩ রাকাত ফরজ, এরপর ২ রাকাত সুন্নতে মুয়াক্কাদাহ।",
            "৩ রাকাত ফরজ এবং এরপর ২ রাকাত সুন্নতে মুয়াক্কাদাহ।",
            steps3Rakat = get3RakatSteps("মাগরিব"),
            steps2Rakat = get2RakatSteps("মাগরিব সুন্নত")
        ),
        WaqtInfo(
            "isha", "এশা", "Isha", "রাত্রিবেলা (মাগরিবের ওয়াক্ত শেষ হওয়ার পর থেকে সুবহে সাদিকের পূর্ব পর্যন্ত)", 9, 4, 2, 4, 3, 2, "صلاة العشاء", "এশা নামাজ মোট ১৫ রাকাত। প্রথমে ৪ রাকাত সুন্নত, এরপর ৪ রাকাত ফরজ, ২ রাকাত সুন্নতে মুয়াক্কাদাহ, ২ রাকাত নফল এবং শেষে ৩ রাকাত বিতর ওয়াজিব।",
            "৪ রাকাত ফরজ, ২ রাকাত সুন্নতে মুয়াক্কাদাহ এবং শেষে ৩ রাকাত বিতর ওয়াজিব।",
            steps4Rakat = get4RakatSteps("এশা"),
            steps2Rakat = get2RakatSteps("এশা সুন্নত"),
            steps3Rakat = getWitr3RakatSteps()
        )
    )"""
content = content.replace(waqts, new_waqts)


with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "w", encoding="utf-8") as f:
    f.write(content)


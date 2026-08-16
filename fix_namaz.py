import sys

with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "r", encoding="utf-8", errors="ignore") as f:
    lines = f.readlines()

# 1. Insert dailyWaqts right before val allDuasAndNiyyat (index 692)
daily_waqts_code = """    val dailyWaqts = listOf(
        WaqtInfo(
            id = "fajr",
            nameBn = "ফজর",
            nameEn = "Fajr",
            arabicName = "صَلَاةُ الفَجْر",
            totalRakat = 4,
            farz = 2,
            sunnatMuakkadah = 2,
            sunnatGairMuakkadah = 0,
            nafl = 0,
            witr = 0,
            descriptionBn = "ফজর নামাজের সময় সুবহে সাদিক থেকে শুরু হয়ে সূর্যোদয়ের পূর্ব পর্যন্ত থাকে। প্রথমে ২ রাকাত সুন্নতে মুয়াক্কাদাহ এবং পরে ২ রাকাত ফরজ নামাজ আদায় করতে হয়।",
            steps2Rakat = get2RakatSteps("Fajr")
        ),
        WaqtInfo(
            id = "dhuhr",
            nameBn = "যোহর",
            nameEn = "Dhuhr",
            arabicName = "صَلَاةُ الظُّهْر",
            totalRakat = 10,
            farz = 4,
            sunnatMuakkadah = 6, // 4 before, 2 after
            sunnatGairMuakkadah = 0,
            nafl = 0,
            witr = 0,
            descriptionBn = "দুপুরের সূর্য পশ্চিম আকাশে ঢলে পড়ার পর থেকে যোহর নামাজের ওয়াক্ত শুরু হয় এবং কোনো বস্তুর ছায়া তার দ্বিগুণ হওয়া পর্যন্ত থাকে। যোহরে প্রথমে ৪ রাকাত সুন্নত, তারপর ৪ রাকাত ফরজ ও শেষে ২ রাকাত সুন্নত আদায় করতে হয়।",
            steps2Rakat = get2RakatSteps("Dhuhr"),
            steps4Rakat = get4RakatSteps("Dhuhr")
        ),
        WaqtInfo(
            id = "asr",
            nameBn = "আসর",
            nameEn = "Asr",
            arabicName = "صَلَاةُ العَصْر",
            totalRakat = 8,
            farz = 4,
            sunnatMuakkadah = 0,
            sunnatGairMuakkadah = 4,
            nafl = 0,
            witr = 0,
            descriptionBn = "যোহরের ওয়াক্ত শেষ হওয়ার পর থেকে আসরের ওয়াক্ত শুরু হয় এবং সূর্যাস্তের পূর্ব পর্যন্ত থাকে। আসরের ৪ রাকাত ফরজ নামাজ আদায় করা ফরজ। ফরজের পূর্বে ৪ রাকাত সুন্নতে গায়রে মুয়াক্কাদাহ পড়া উত্তম।",
            steps2Rakat = get2RakatSteps("Asr"),
            steps4Rakat = get4RakatSteps("Asr")
        ),
        WaqtInfo(
            id = "maghrib",
            nameBn = "মাগরিব",
            nameEn = "Maghrib",
            arabicName = "صَلَاةُ المَغْرِب",
            totalRakat = 5,
            farz = 3,
            sunnatMuakkadah = 2,
            sunnatGairMuakkadah = 0,
            nafl = 0,
            witr = 0,
            descriptionBn = "সূর্যাস্তের পর থেকে মাগরিবের ওয়াক্ত শুরু হয় এবং পশ্চিম আকাশে লাল আভা (শফক) বিলীন হওয়া পর্যন্ত থাকে। মাগরিবে প্রথমে ৩ রাকাত ফরজ এবং পরে ২ রাকাত সুন্নতে মুয়াক্কাদাহ নামাজ আদায় করতে হয়।",
            steps2Rakat = get2RakatSteps("Maghrib"),
            steps3Rakat = get3RakatSteps("Maghrib")
        ),
        WaqtInfo(
            id = "isha",
            nameBn = "এশা",
            nameEn = "Isha",
            arabicName = "صَلَاةُ العِشَاء",
            totalRakat = 9,
            farz = 4,
            sunnatMuakkadah = 2,
            sunnatGairMuakkadah = 0,
            nafl = 3,
            witr = 0,
            descriptionBn = "মাগরিবের ওয়াক্ত শেষ হওয়ার পর থেকে সুবহে সাদিকের পূর্ব পর্যন্ত এশার নামাজের ওয়াক্ত থাকে। এশা নামাজে প্রথমে ৪ রাকাত ফরজ ও পরে ২ রাকাত সুন্নতে মুয়াক্কাদাহ আদায় করতে হয়। এরপর ৩ রাকাত বিতর ও ২ রাকাত নফল আদায় করা হয়।",
            steps2Rakat = get2RakatSteps("Isha"),
            steps4Rakat = get4RakatSteps("Isha")
        ),
        WaqtInfo(
            id = "witr",
            nameBn = "বিতর",
            nameEn = "Witr",
            arabicName = "صَلَاةُ الوِتْر",
            totalRakat = 3,
            farz = 0,
            sunnatMuakkadah = 0,
            sunnatGairMuakkadah = 0,
            nafl = 0,
            witr = 3,
            descriptionBn = "বিতর নামাজ ৩ রাকাত পড়া ওয়াজিব। এটি সাধারণত এশার ফরজ ও সুন্নতের পর আদায় করা হয়। এর ৩য় রাকাতে সূরা ফাতেহা ও অন্য সূরা পড়ার পর অতিরিক্ত তাকবীর বলে দুআ কুনুত পাঠ করা ওয়াজিব।",
            steps2Rakat = get2RakatSteps("Witr"),
            steps3Rakat = getWitr3RakatSteps()
        )
    )

"""

lines.insert(692, daily_waqts_code)

# 2. Replace index range 454 to 562 (inclusive) with "        ),\n"
# Note that inserting at index 692 shifted things, but if we do it in reverse order, indices are unaffected!
# So let's re-read and apply replacements in REVERSE order.
# Re-reading to ensure clean state
with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "r", encoding="utf-8", errors="ignore") as f:
    lines = f.readlines()

# Modifying in reverse order to keep indices stable:

# Insertion at 692
lines.insert(692, daily_waqts_code)

# Replacement of index 454 to 562 (inclusive)
# This removes lines from 454 to 562 and replaces them with a single line
lines[454:563] = ["        ),\n"]

# Replacement of index 190 to 191 (inclusive) with repaired tayammumSteps and ghusl lists
tayammum_and_ghusl_code = """        PrayerStep(
            2, "মাটিতে হাত মারা ও মুখ মাসাহ", "Strike Dust & Wipe Face",
            "পবিত্র মাটি বা ধূলিমুক্ত পাথরে দুই হাত হালকা আঘাত করুন এবং অতিরিক্ত ধুলো থাকলে ফুঁ দিয়ে তা পরিষ্কার করে পুরো মুখমণ্ডল একবার মাসাহ করুন।",
            postureType = PostureType.DUA_GENERIC
        ),
        PrayerStep(
            3, "হাত ও কনুই মাসাহ", "Strike Dust & Wipe Arms",
            "পুনরায় দুই হাত মাটিতে আলতো করে আঘাত করুন এবং ডান হাতের সাহায্যে বাম হাত এবং বাম হাতের সাহায্যে ডান হাত কনুইসহ মাসাহ করুন।",
            postureType = PostureType.DUA_GENERIC
        )
    )

    val ghuslFarzList = listOf(
        "১. গড়গড়ার সহিত কুলি করা (রোজা না থাকলে)।",
        "২. নাকে পানি দিয়ে নরম অংশ পর্যন্ত পৌঁছানো ও নাক পরিষ্কার করা।",
        "৩. সমস্ত শরীরে এমনভাবে পানি পৌঁছানো যেন একটি চুলের গোড়াও শুকনা না থাকে।"
    )

    val ghuslSunnahSteps = listOf(
        "১. প্রথমে উভয় হাত কবজি পর্যন্ত ৩ বার ধোয়া।",
        "২. শরীরে কোথাও কোনো অপবিত্রতা বা ময়লা লেগে থাকলে তা পরিষ্কার করা।",
        "৩. নামাজের অজুর মতো পূর্ণাঙ্গ অজু করা (তবে পা ধোয়া শেষে করা যেতে পারে)।",
        "৪. মাথায় ৩ বার পানি ঢেলে ভালোভাবে ধোয়া যেন চুলের গোড়া ভিজে যায়।",
        "৫. প্রথমে ডান কাঁধে এবং পরে বাম কাঁধে পানি ঢেলে সারা শরীর ভালোভাবে ধুয়ে নেওয়া।",
        "৬. এরপর সম্পূর্ণ শরীরে এমনভাবে পানি ঢালা ও ডলা যাতে কোনো অঙ্গই শুকনো না থাকে।"
    )
"""

lines[190:192] = [tayammum_and_ghusl_code]

with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "w", encoding="utf-8") as f:
    f.writelines(lines)

print("Replacement successful!")

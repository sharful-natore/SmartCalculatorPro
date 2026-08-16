package com.example.ui.namaz

import androidx.compose.ui.graphics.vector.ImageVector

enum class PostureType {
    NIYYAT,
    TAKBEER,
    QIYAM,
    RUKU,
    QAUMA,
    SUJUD,
    JALSA,
    TASHAHHUD,
    SALAM,
    WUDU_GENERIC,
    DUA_GENERIC
}

data class PrayerStep(
    val stepNumber: Int,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String = "",
    val postureType: PostureType,
    val arabicText: String? = null,
    val banglaPronunciation: String? = null,
    val banglaMeaning: String? = null,
    val maleNoteBn: String? = null,
    val femaleNoteBn: String? = null,
    val audioUrl: String? = null
)

data class DuaItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val category: String, // e.g., "Niyyat", "Prayer Dua", "Janazah Dua", "Wudu Dua"
    val arabicText: String,
    val banglaPronunciation: String,
    val banglaMeaning: String,
    val englishMeaning: String = "",
    val audioUrl: String? = null,
    val contextOrVirtueBn: String? = null
)

data class WaqtInfo(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val arabicName: String,
    val totalRakat: Int,
    val farz: Int,
    val sunnatMuakkadah: Int,
    val sunnatGairMuakkadah: Int,
    val nafl: Int,
    val witr: Int,
    val descriptionBn: String,
    val steps2Rakat: List<PrayerStep>,
    val steps3Rakat: List<PrayerStep>? = null,
    val steps4Rakat: List<PrayerStep>? = null
)

data class PrayerRule(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val category: String, // "Wudu", "Taharat", "Special", "Janazah", "Eid", "Nofl"
    val introductionBn: String,
    val steps: List<PrayerStep>,
    val rakatsCountBn: String = "",
    val specialTakbeerCountBn: String? = null,
    val khutbahNoteBn: String? = null,
    val extraNotesBn: String? = null
)

data class WuduGuideItem(
    val stepNumber: Int,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val isFarz: Boolean = false,
    val isSunnah: Boolean = false,
    val arabicDua: String? = null,
    val banglaDuaPronunciation: String? = null,
    val banglaDuaMeaning: String? = null
)

// --- STATIC DATA REPOSITORIES ---

object NamazDataRepository {

    // 1. WUDU & TAHARAT
    val wuduFarzList = listOf(
        "১. সমস্ত মুখমণ্ডল ধোয়া (কপালের চুলের গোড়া থেকে থুতনির নিচ এবং এক কানের লতি থেকে অপর কানের লতি পর্যন্ত)।",
        "২. দুই হাত কনুইসহ ধোয়া।",
        "৩. মাথার চারভাগের একভাগ মাসাহ করা।",
        "৪. দুই পা টাখনুসহ (পায়ের গিট) ধোয়া।"
    )

    val wuduSunnahList = listOf(
        "১. মনে মনে অজুর নিয়ত করা ও 'বিসমিল্লাহ' বলে শুরু করা।",
        "২. দুই হাত কবজি পর্যন্ত ৩ বার ধোয়া।",
        "৩. মিসওয়াক করা (অথবা আঙুল দিয়ে দাঁত পরিষ্কার করা)।",
        "৪. ৩ বার কুলি করা।",
        "৫. ৩ বার নাকে পানি দিয়ে ডান হাতে নাক পরিষ্কার করা।",
        "৬. দাড়ি খিলাল করা (ঘন দাড়ি থাকলে)।",
        "৭. হাত ও পায়ের আঙুলসমূহ খিলাল করা।",
        "৮. সমস্ত মুখমণ্ডল ৩ বার ধোয়া।",
        "৯. দুই হাত কনুইসহ ৩ বার ধোয়া।",
        "১০. সম্পূর্ণ মাথা একবার মাসাহ করা।",
        "১১. ভিজা আঙুল দিয়ে কান ও গর্দান মাসাহ করা।",
        "১২. সমস্ত অঙ্গ ধারাবাহিকভাবে পরপর ধোয়া (তারতিব)।",
        "১৩. ডান দিক থেকে অঙ্গ ধোয়া শুরু করা।"
    )

    val wuduBreakersList = listOf(
        "১. পায়খানা বা প্রস্রাবের রাস্তা দিয়ে কোনো কিছু বের হওয়া।",
        "২. মুখ ভরে বমি করা।",
        "৩. শরীরের কোনো স্থান থেকে রক্ত, পুঁজ বা পানি বের হয়ে গড়িয়ে পড়া।",
        "৪. থুথুর সাথে রক্তের ভাগ সমান বা বেশি হওয়া।",
        "৫. কাত হয়ে, হেলে বা হেলান দিয়ে ঘুমিয়ে পড়া।",
        "৬. পাগল, মাতাল বা অচেতন হওয়া।",
        "৭. নামাজে উচ্চস্বরে হেসে ওঠা (রুকু-সিজদাবিশিষ্ট নামাজে)।"
    )

    val wuduSteps = listOf(
        WuduGuideItem(
            1, "নিয়ত ও বিসমিল্লাহ", "Intention & Bismillah",
            "পবিত্রতার উদ্দেশ্যে মনে মনে অজুর নিয়ত করুন এবং 'বিসমিল্লাহির রহমানির রহিম' পাঠ করে হাত ধোয়া শুরু করুন।",
            isSunnah = true,
            arabicDua = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            banglaDuaPronunciation = "বিসমিল্লাহির রাহমানির রাহিম",
            banglaDuaMeaning = "পরম করুণাময় অসীম দয়ালু আল্লাহর নামে শুরু করছি।"
        ),
        WuduGuideItem(
            2, "হাত ধোয়া (কবজি পর্যন্ত)", "Washing Hands",
            "দুই হাতের কবজি পর্যন্ত ভালোভাবে ৩ বার পানি দিয়ে ধুয়ে নিন এবং আঙুলসমূহ খিলাল করুন।",
            isSunnah = true
        ),
        WuduGuideItem(
            3, "কুলি করা (মাজমাজা)", "Rinsing Mouth",
            "ডান হাতে পানি নিয়ে ৩ বার ভালোভাবে কুলি করুন। রোজা না থাকলে ভালোভাবে গড়গড়া করা সুন্নত।",
            isSunnah = true
        ),
        WuduGuideItem(
            4, "নাকে পানি দেওয়া (ইস্তিনশাক)", "Cleaning Nose",
            "ডান হাতে নাকে ৩ বার পানি দিন এবং বাম হাত দিয়ে নাকে জমে থাকা পানি বা ময়লা পরিষ্কার করুন।",
            isSunnah = true
        ),
        WuduGuideItem(
            5, "মুখমণ্ডল ধোয়া (ফরজ)", "Washing Face (Farz)",
            "কপালের চুল গজানোর স্থান থেকে থুতনির নিচ এবং এক কান থেকে অন্য কান পর্যন্ত পুরো মুখমণ্ডল ৩ বার পানি দিয়ে ধুয়ে নিন।",
            isFarz = true
        ),
        WuduGuideItem(
            6, "দুই হাত কনুইসহ ধোয়া (ফরজ)", "Washing Arms to Elbows (Farz)",
            "প্রথমে ডান হাত কনুইসহ ৩ বার এবং পরে বাম হাত কনুইসহ ৩ বার পানি দিয়ে ভালোভাবে ধুয়ে নিন।",
            isFarz = true
        ),
        WuduGuideItem(
            7, "মাথা মাসাহ করা (ফরজ)", "Wiping Head (Farz)",
            "হাত ভিজিয়ে কপাল থেকে পেছনের ঘাড় পর্যন্ত এবং ঘাড় থেকে আবার সামনে এনে পুরো মাথা মাসাহ করুন (কমপক্ষে ৪ ভাগের ১ ভাগ মাসাহ করা ফরজ)।",
            isFarz = true
        ),
        WuduGuideItem(
            8, "কান ও ঘাড় মাসাহ", "Wiping Ears & Neck",
            "শাহাদাত আঙুল দিয়ে কানের ভেতরের অংশ এবং বৃদ্ধা আঙুল দিয়ে কানের পেছনের অংশ মাসাহ করুন। হাতের উল্টো পিঠ দিয়ে ঘাড় মাসাহ করুন।",
            isSunnah = true
        ),
        WuduGuideItem(
            9, "দুই পা টাখনুসহ ধোয়া (ফরজ)", "Washing Feet to Ankles (Farz)",
            "প্রথমে ডান পা এবং পরে বাম পা টাখনু (গিট) সহ ৩ বার ধুয়ে নিন। বাম হাতের কনিষ্ঠ আঙুল দিয়ে পায়ের আঙুলের ফাঁকসমূহ খিলাল করুন।",
            isFarz = true,
            arabicDua = "أَشْهَدُ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            banglaDuaPronunciation = "আশহাদু আল-লা ইলাহা ইল্লাল্লাহু ওয়াহ্দাহু লা শারীকা লাহু ওয়্যাশহাদু আন্না মুহাম্মাদান আবদুহু ওয়া রাসূলুহু।",
            banglaDuaMeaning = "আমি সাক্ষ্য দিচ্ছি যে, আল্লাহ ছাড়া কোনো ইলাহ নেই, তিনি একক, তাঁর কোনো শরিক নেই। আমি আরো সাক্ষ্য দিচ্ছি যে, মুহাম্মদ (সা.) তাঁর বান্দা ও রাসূল।"
        )
    )

    val tayammumSteps = listOf(
        PrayerStep(
            1, "নিয়ত করা", "Niyyat",
            "পবিত্রতার নিয়তে মনে মনে নিয়ত করা: 'আমি পবিত্রতা অর্জনের জন্য তায়াম্মুমের নিয়ত করছি।'",
            postureType = PostureType.NIYYAT,
            arabicText = "نَوَيْتُ أَنْ أَتَيَمَّمَ لِرَفْعِ الْحَدَثِ وَلاِسْتِبَاحَةِ الصَّلاَةِ",
            banglaPronunciation = "নাওয়াইতু আন আতায়াম্মামা লিরাফইল হাদাসি ওয়ালিসতিবাহাতিস সালাতি",
            banglaMeaning = "আমি অপবিত্রতা দূরীকরণ ও নামাজ আদায়ের উদ্দেশ্যে তায়াম্মুমের নিয়ত করছি।"
        ),
        PrayerStep(
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

    // 2. 5 DAILY PRAYERS (WAQT)
    val dailyWaqts = listOf(
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
            sunnatMuakkadah = 6,
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
            totalRakat = 15,
            farz = 4,
            sunnatMuakkadah = 4,
            sunnatGairMuakkadah = 4,
            nafl = 0,
            witr = 3,
            descriptionBn = "মাগরিবের ওয়াক্ত শেষ হওয়ার পর থেকে সুবহে সাদিকের পূর্ব পর্যন্ত এশার নামাজের ওয়াক্ত থাকে। এশা নামাজে প্রথমে ৪ রাকাত ফরজ ও পরে ২ রাকাত সুন্নতে মুয়াক্কাদাহ আদায় করতে হয়। এরপর ৩ রাকাত বিতর আদায় করা হয়।",
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

    // 3. SPECIAL & OCCASIONAL PRAYERS
    val specialPrayers = listOf(
        PrayerRule(
            id = "janazah",
            titleBn = "জানাজার নামাজ (Janazah Prayer)",
            titleEn = "Janazah Funeral Prayer",
            category = "Janazah",
            introductionBn = "জানাজার নামাজ ফরজ এ কেফায়া। এতে কোনো রুকু বা সিজদা নেই, পুরো নামাজ ৪টি তাকবীরের মাধ্যমে দাঁড়িয়ে আদায় করতে হয়।",
            rakatsCountBn = "দাঁড়িয়ে ৪ তাকবীর (রুকু-সিজদা নেই)",
            specialTakbeerCountBn = "৪ তাকবীর",
            extraNotesBn = "ইমামের পেছনে মুক্তাদিগণ নিয়ত করে হাত বেঁধে ৪ তাকবীর বলবেন।",
            steps = listOf(
                PrayerStep(
                    1, "১ম তাকবীর ও ছানা", "1st Takbeer & Sana",
                    "কিবলামুখী হয়ে নিয়ত করুন। 'আল্লাহু আকবার' বলে হাত বেঁধে ছানা পাঠ করুন (সুবহানাকাল্লাহুম্মা... এর সাথে 'ওয়া জাল্লা সানাউকা' যুক্ত করুন)।",
                    postureType = PostureType.TAKBEER,
                    arabicText = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَىٰ جَدُّكَ وَجَلَّ ثَنَاؤُكَ وَلَا إِلَٰهَ غَيْرُكَ",
                    banglaPronunciation = "সুবহানাকা আল্লাহুম্মা ওয়া বিহামদিকা ওয়া তাবারাকাসমুকা ওয়া তাআলা জদ্দুকা ওয়া জাল্লা সানাউকা ওয়া লা ইলাহা গাইরুক।",
                    banglaMeaning = "হে আল্লাহ! তোমার প্রশংসা সহকারে পবিত্রতা বর্ণনা করছি, তোমার নাম বরকতময়, তোমার মর্যাদা সুউচ্চ, তোমার প্রশংসা সুমহান এবং তুমি ছাড়া কোনো মাবুদ নেই।"
                ),
                PrayerStep(
                    2, "২য় তাকবীর ও দরুদ শরিফ", "2nd Takbeer & Durood",
                    "হাত না উঠিয়ে ২য় তাকবীর 'আল্লাহু আকবার' বলুন এবং দরুদে ইব্রাহিম পাঠ করুন।",
                    postureType = PostureType.TAKBEER,
                    arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّদٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ মজিদ। আল্লাহুম্মা বারিক আলা মুহাম্মাদিওঁ ওয়া আলা আলি মুহাম্মাদিন কামা বারাকতা আলা ইব্রাহিমা ওয়া আলা আলি ইব্রাহিমা ইন্নাকা হামিদুম মাজিদ।",
                    banglaPronunciation = "আল্লাহুম্মা সাল্লি আলা মুহাম্মাদিওঁ ওয়া আলা আলি মুহাম্মাদিন কামা সাল্লাইতা আলা ইব্রাহিমা ওয়া আলা আলি ইব্রাহিমা ইন্নাকা হামিদুম মাজিদ। আল্লাহুম্মা বারিক আলা মুহাম্মাদিওঁ ওয়া আলা আলি মুহাম্মাদিন কামা বারাকতা আলা ইব্রাহিমা ওয়া আলা আলি ইব্রাহিমা ইন্নাকা হামিদুম মাজিদ।",
                    banglaMeaning = "হে আল্লাহ! মুহাম্মদ (সা.) এবং তাঁর বংশধরের ওপর রহমত নাজিল করো, যেমন ইব্রাহিম (আ.) ও তাঁর বংশধরের ওপর রহমত নাজিল করেছিলে। নিশ্চয়ই তুমি প্রশংসিত ও মহিমান্বিত। হে আল্লাহ! মুহাম্মদ (সা.) এবং তাঁর বংশধরদের ওপর বরকত নাজিল করো, যেমন ইব্রাহিম (আ.) ও তাঁর বংশধরের ওপর বরকত নাজিল করেছিলে। নিশ্চয়ই তুমি প্রশংসিত ও মহিমান্বিত।"
                ),
                PrayerStep(
                    3, "৩য় তাকবীর ও জানাজার দোয়া (প্রাপ্তবয়স্ক)", "3rd Takbeer & Adult Funeral Dua",
                    "হাত না উঠিয়ে ৩য় তাকবীর 'আল্লাহু আকবার' বলুন এবং মেয়্যেতের জন্য খাস দোয়া পাঠ করুন।",
                    postureType = PostureType.TAKBEER,
                    arabicText = "اللَّهُمَّ اغْفِرْ لِحَيِّنَا وَمَيِّتِنَا وَشَاهِدِنَا وَغَائِبِنَا وَصَغِيرِنَا وَكَبِيرِنَا وَذَكَرِنَا وَأُنْثَانَا...",
                    banglaPronunciation = "আল্লাহুম্মাগফির লিহাইয়্যিনা ওয়া মায়্যিতিনা ওয়া শাহেদিনা...",
                    banglaMeaning = "হে আল্লাহ! আমাদের জীবিত ও মৃত, উপস্থিত ও অনুপস্থিত, ছোট ও বড়, পুরুষ ও নারী সকলকে ক্ষমা করে দাও। হে আল্লাহ! আমাদের মধ্যে যাকে তুমি জীবিত রাখবে তাকে ইসলামের ওপর জীবিত রাখো এবং যাকে মৃত্যু দান করবে তাকে ঈমানের সাথে মৃত্যু দান করো।"
                ),
                PrayerStep(
                    4, "৪র্থ তাকবীর ও সালাম", "4th Takbeer & Salam",
                    "৪র্থ তাকবীর 'আল্লাহু আকবার' বলে হাত না উঠিয়েই ডান ও বাম দিকে সালাম ফেরান।",
                    postureType = PostureType.SALAM,
                    arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                    banglaPronunciation = "আসসালামু আলাইকুম ওয়া রহমাতুল্লাহ",
                    banglaMeaning = "আপনার ওপর আল্লাহর শান্তি ও রহমত বর্ষিত হোক।"
                )
            )
        ),
        PrayerRule(
            id = "eid",
            titleBn = "ঈদের নামাজ (Eid-ul-Fitr & Eid-ul-Adha)",
            titleEn = "Eid Prayer Rules",
            category = "Eid",
            introductionBn = "ঈদুল ফিতর ও ঈদুল আজহার নামাজ ২ রাকাত ওয়াজিব। এতে অতিরিক্ত ৬টি তাকবীর বলতে হয়।",
            rakatsCountBn = "২ রাকাত ওয়াজিব",
            specialTakbeerCountBn = "অতিরিক্ত ৬ তাকবীর",
            steps = listOf(
                PrayerStep(1, "নিয়ত ও তাকবীর", "Niyyat", "নিয়ত ও তাকবীর", postureType = PostureType.TAKBEER)
            )
        ),
        PrayerRule(
            id = "jumuah",
            titleBn = "জুমুআর নামাজ (Jumu'ah)",
            titleEn = "Jumu'ah Friday Prayer",
            category = "Special",
            introductionBn = "জুমুআর নামাজ সপ্তাহের শ্রেষ্ঠ নামাজ।",
            rakatsCountBn = "২ রাকাত ফরজ",
            steps = listOf(
                PrayerStep(1, "খুতবা ও জামাত", "Khutbah", "খুতবা ও জামাত", postureType = PostureType.TAKBEER)
            )
        ),
        PrayerRule(
            id = "tarawih",
            titleBn = "তারাবিহ নামাজ (Tarawih)",
            titleEn = "Tarawih Prayer",
            category = "Special",
            introductionBn = "রমজান মাসে ২০ রাকাত তারাবিহ নামাজ পড়া সুন্নতে মুয়াক্কাদাহ।",
            rakatsCountBn = "২০ রাকাত",
            steps = listOf(
                PrayerStep(1, "তারাবিহ আদায়ের নিয়ম", "Tarawih", "নিয়ম", postureType = PostureType.QIYAM)
            )
        ),
        PrayerRule(
            id = "tahajjud",
            titleBn = "তাহাজ্জুদ নামাজ (Tahajjud)",
            titleEn = "Tahajjud Prayer",
            category = "Nofl",
            introductionBn = "তাহাজ্জুদ নামাজ রাতের শেষ তৃতীয়াংশে পড়া শ্রেষ্ঠ ইবাদত।",
            rakatsCountBn = "২ থেকে ১২ রাকাত",
            steps = listOf(
                PrayerStep(1, "তাহাজ্জুদ আদায়ের নিয়ম", "Tahajjud", "নিয়ম", postureType = PostureType.QIYAM)
            )
        ),
        PrayerRule(
            id = "qaza",
            titleBn = "কাজা নামাজ (Qaza)",
            titleEn = "Missed Prayer",
            category = "Special",
            introductionBn = "ছুটে যাওয়া নামাজের নিয়ম।",
            steps = listOf(
                PrayerStep(1, "কাজা আদায়ের নিয়ম", "Qaza", "নিয়ম", postureType = PostureType.NIYYAT)
            )
        )
    )

    // Helper step generators
    private fun get2RakatSteps(waqtName: String): List<PrayerStep> {
        return listOf(
            PrayerStep(
                1, "নিয়ত ও তাকবীরে তাহরিমা", "Takbeer & Intention",
                "কিবলামুখী হয়ে দাঁড়ান, মনে মনে নিয়ত করুন এবং 'আল্লাহু আকবার' বলে হাত উঠিয়ে নাভির নিচে (নারীরা বুকে) বাঁধুন।",
                postureType = PostureType.TAKBEER,
                arabicText = "اللَّهُ أَكْبَرُ",
                banglaPronunciation = "আল্লাহু আকবার",
                banglaMeaning = "আল্লাহ সবচেয়ে মহান।"
            ),
            PrayerStep(
                2, "ছানা ও ক্বিরাআত", "Qiyam",
                "হাত বেঁধে প্রথমে ছানা পাঠ করুন। এরপর সূরা ফাতেহা ও অন্য সূরা পাঠ করুন।",
                postureType = PostureType.QIYAM
            ),
            PrayerStep(
                3, "রুকু", "Ruku",
                "'আল্লাহু আকবার' বলে রুকুতে যান।",
                postureType = PostureType.RUKU
            ),
            PrayerStep(
                4, "কওমা", "Qauma",
                "রুকু থেকে সোজা হয়ে দাঁড়ান।",
                postureType = PostureType.QAUMA
            ),
            PrayerStep(
                5, "সিজদা", "Sujud",
                "'আল্লাহু আকবার' বলে সিজদায় যান।",
                postureType = PostureType.SUJUD
            ),
            PrayerStep(
                6, "২য় রাকাত ও শেষ বৈঠক", "2nd Rakat",
                "২য় রাকাত সম্পন্ন করে শেষ বৈঠকে বসুন।",
                postureType = PostureType.TASHAHHUD
            ),
            PrayerStep(
                7, "সালাম", "Salam",
                "সালাম ফেরান।",
                postureType = PostureType.SALAM
            )
        )
    }

    private fun get3RakatSteps(waqtName: String): List<PrayerStep> {
        val base = get2RakatSteps(waqtName).toMutableList()
        if (base.size >= 2) {
            base.removeAt(base.size - 1)
            base.removeAt(base.size - 1)
        }
        base.add(
            PrayerStep(6, "১ম বৈঠক", "1st Session", "আত্তাহিয়্যাতু পড়ুন", postureType = PostureType.TASHAHHUD)
        )
        base.add(
            PrayerStep(7, "৩য় রাকাত ও সালাম", "3rd Rakat", "৩য় রাকাত শেষে সালাম ফেরান", postureType = PostureType.SALAM)
        )
        return base
    }

    private fun get4RakatSteps(waqtName: String): List<PrayerStep> {
        val base = get3RakatSteps(waqtName).toMutableList()
        base.removeAt(base.size - 1)
        base.add(
            PrayerStep(7, "৩য় রাকাত", "3rd Rakat", "৩য় রাকাত সম্পন্ন করুন", postureType = PostureType.QIYAM)
        )
        base.add(
            PrayerStep(8, "৪র্থ রাকাত ও সালাম", "4th Rakat", "৪র্থ রাকাত শেষে সালাম ফেরান", postureType = PostureType.SALAM)
        )
        return base
    }

    private fun getWitr3RakatSteps(): List<PrayerStep> {
        return listOf(
            PrayerStep(1, "বিতর ১ম ২ রাকাত", "First 2 Rakats", "আদয় করুন", postureType = PostureType.QIYAM),
            PrayerStep(2, "৩য় রাকাত ও দুআ কুনুত", "3rd Rakat & Qunut", "দুআ কুনুত পড়ুন", postureType = PostureType.TAKBEER),
            PrayerStep(3, "সালাম", "Salam", "সালাম ফেরান", postureType = PostureType.SALAM)
        )
    }
    // 4. ALL NIYYAT & DUAS
    val allDuasAndNiyyat = listOf(
        DuaItem(
            id = "sana",
            titleBn = "ছানা (Sana)",
            titleEn = "Sana",
            category = "Prayer Dua",
            arabicText = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَىٰ جَدُّكَ وَلَا إِلَٰهَ غَيْرُكَ",
            banglaPronunciation = "সুবহানাকা আল্লাহুম্মা ওয়া বিহামদিকা ওয়া তাবারাকাসমুকা ওয়া তাআলা জদ্দুকা ওয়া লা ইলাহা গাইরুক।",
            banglaMeaning = "হে আল্লাহ! তোমার প্রশংসা সহকারে তোমার পবিত্রতা ঘোষণা করছি, তোমার নাম বরকতময়, তোমার মর্যাদা সুউচ্চ এবং তুমি ছাড়া কোনো সত্যিকারের উপাস্য নেই।",
            englishMeaning = "Glory be to You, O Allah, and praise be to You, and blessed is Your name, and exalted is Your majesty, and there is no deity besides You."
        ),
        DuaItem(
            id = "tashahhud",
            titleBn = "তাশাহহুদ / আত্তাহিয়্যাতু",
            titleEn = "Tashahhud / Attahiyyata",
            category = "Prayer Dua",
            arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَىٰ عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            banglaPronunciation = "আত্তাহিয়্যাতু লিল্লাহি ওয়াস সালাওয়াতু ওয়াত তাইয়্যিবাতু, আসসালামু আলাইকা আইয়্যুহান নাবিয়্যু ওয়া রহমাতুল্লাহি ওয়া বারাকাতুহু, আসসালামু আলাইনা ওয়া আলা ইবাদিল্লাহিস সালিহীন, আশহাদু আল-লা ইলাহা ইল্লাল্লাহু ওয়া আশহাদু আন্না মুহাম্মাদান আবদুহু ওয়া রাসুলুহু।",
            banglaMeaning = "সকল কায়িক, বাচনিক ও আর্থিক এবাদত একমাত্র আল্লাহর জন্য। হে নবী! আপনার প্রতি সালাম, আল্লাহর রহমত ও বরকত বর্ষিত হোক। আমাদের প্রতি এবং আল্লাহর নেক বান্দাদের প্রতি সালাম। আমি সাক্ষ্য দিচ্ছি আল্লাহ ছাড়া কোনো মাবুদ নেই এবং মুহাম্মদ (সা.) তাঁর বান্দা ও রাসূল।",
            englishMeaning = "All compliments, prayers and pure actions are for Allah. Peace be upon you, O Prophet, and the mercy of Allah and His blessings."
        ),
        DuaItem(
            id = "durood",
            titleBn = "দরুদে ইব্রাহিম (Durood Sharif)",
            titleEn = "Durood Ibrahim",
            category = "Prayer Dua",
            arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
            banglaPronunciation = "আল্লাহুম্মা সাল্লি আলা মুহাম্মাদিওঁ ওয়া আলা আলি মুহাম্মাদিন কামা সাল্লাইতা আলা ইব্রাহিমা ওয়া আলা আলি ইব্রাহিমা ইন্নাকা হামিদুম মাজিদ। আল্লাহুম্মা বারিক আলা মুহাম্মাদিওঁ ওয়া আলা আলি মুহাম্মাদিন কামা বারাকতা আলা ইব্রাহিমা ওয়া আলা আলি ইব্রাহিমা ইন্নাকা হামিদুম মাজিদ।",
            banglaMeaning = "হে আল্লাহ! মুহাম্মদ (সা.) এবং তাঁর বংশধরের ওপর রহমত নাজিল করো, যেমন ইব্রাহিম (আ.) ও তাঁর বংশধরের ওপর রহমত নাজিল করেছিলে। নিশ্চয়ই তুমি প্রশংসিত ও মহিমান্বিত...",
            englishMeaning = "O Allah, send peace upon Muhammad and the family of Muhammad, as You sent peace upon Ibrahim and the family of Ibrahim..."
        ),
        DuaItem(
            id = "dua_masura",
            titleBn = "দোয়া মাসুরা (Dua Masura)",
            titleEn = "Dua Masura",
            category = "Prayer Dua",
            arabicText = "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ وَارْحَمْنِي إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ",
            banglaPronunciation = "আল্লাহুম্মা ইন্নি জালামতু নাফসি জুলমান কাসিরাওঁ ওয়া লা ইয়াগফিরুজ জুনুবা ইল্লা আনতা ফাগফিরলি মাগফিরাতাম মিন ইনদিকা ওয়ারহামনি ইন্নাকা আনতাল গাফুরুর রাহিম।",
            banglaMeaning = "হে আল্লাহ! আমি আমার নিজের ওপর অনেক জুলুম করেছি। তুমি ছাড়া আর কেউ পাপ ক্ষমা করতে পারে না। অতএব তোমার পক্ষ থেকে আমাকে ক্ষমা করে দাও এবং আমার ওপর দয়া করো। নিশ্চয়ই তুমি পরম ক্ষমাশীল, পরম দয়ালু।",
            englishMeaning = "O Allah, I have greatly wronged myself, and no one forgives sins except You. So grant me forgiveness from You..."
        ),
        DuaItem(
            id = "dua_qunut",
            titleBn = "দুআ কুনুত (Dua Qunut)",
            titleEn = "Dua Qunut for Witr",
            category = "Prayer Dua",
            arabicText = "اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنُؤْمِنُ بِكَ وَنَتَوَكَّلُ عَلَيْكَ وَنُثْنِي عَلَيْكَ الْخَيْرَ وَنَشْكُرُكَ وَلَا نَكْفُرُكَ وَنَخْلَعُ وَنَتْرُكُ مَنْ يَفْجُرُكَ، اللَّهُمَّ إِيَّاكَ نَعْبُدُ وَلَكَ نُصَلِّي وَنَسْجُدُ وَإِلَيْكَ نَسْعَىٰ وَنَحْفِدُ وَنَرْجُو رَحْمَتَكَ وَنَخْشَىٰ عَذَابَكَ إِنَّ عَذَابَكَ بِالْكُفَّارِ مُلْحَقٌ",
            banglaPronunciation = "আল্লাহুম্মা ইন্না নাস্তাইনুকা ওয়া নাস্তাগফিরুকা ওয়া নু'মিনু বিকা ওয়া নাতাওয়াক্কালু আলাইকা ওয়া নুছনি আলাইকাল খাইরা ওয়া নাশকুরুকা ওয়া লা নাকফুরুকা ওয়া নাখলাউ ওয়া নাতরুকু মাই ইয়্যাফজুরুক। আল্লাহুম্মা ইয়্যাকা নাবুদু ওয়া লাকা নুসাল্লি ওয়া নাসজুদু ওয়া ইলাইকা নাসআ ওয়া নাহফিদু ওয়া নারজু রহমাতাকা ওয়া নাখশা আজাবাকা ইন্না আজাবাকা বিল কুফফারি মুলহাক।",
            banglaMeaning = "হে আল্লাহ! আমরা তোমারই সাহায্য চাচ্ছি, তোমার নিকট ক্ষমা প্রার্থনা করছি, তোমার ওপর ঈমান আনছি, তোমার ওপর ভরসা করছি এবং তোমার উত্তম প্রশংসা করছি। আমরা তোমার শুকরিয়া আদায় করছি, অকৃতজ্ঞ হচ্ছি না..."
        ),
        DuaItem(
            id = "niyyat_fajr",
            titleBn = "ফজরের নামাজের নিয়ত",
            titleEn = "Fajr Intention",
            category = "Niyyat",
            arabicText = "نَوَيْتُ أَنْ أُصَلِّيَ لِلَّهِ تَعَالَى رَكْعَتَيْ صَلَاةِ الْفَجْرِ فَرْضُ اللَّهِ تَعَالَى مُتَوَجِّهًا إِلَى كَعْبَةِ الشَّرِيفَةِ",
            banglaPronunciation = "নাওয়াইতু আন উসাল্লিয়া লিল্লাহি তাআলা রাকআতাই সালাতিল ফজরি ফারজুল্লাহি তাআলা মুতাওয়াজ্জিহান ইলা কাবাতিশ শারিফাহ।",
            banglaMeaning = "আমি কাবাশরিফের দিকে মুখ করে আল্লাহর ওয়াস্তে ফজরের ২ রাকাত ফরজ নামাজ আদায়ের নিয়ত করছি।"
        ),
        DuaItem(
            id = "niyyat_dhuhr",
            titleBn = "জোহরের নামাজের নিয়ত",
            titleEn = "Dhuhr Intention",
            category = "Niyyat",
            arabicText = "نَوَيْتُ أَنْ أُصَلِّيَ لِلَّهِ تَعَالَى أَرْبَعَ رَكَعَاتٍ صَلَاةِ الظُّهْرِ فَرْضُ اللَّهِ تَعَالَى مُتَوَجِّهًا إِلَى كَعْبَةِ الشَّرِيفَةِ",
            banglaPronunciation = "নাওয়াইতু আন উসাল্লিয়া লিল্লাহি তাআলা আরবাআ রাকআতি সালাতিজ জুহরি ফারজুল্লাহি তাআলা মুতাওয়াজ্জিহান ইলা কাবাতিশ শারিফাহ।",
            banglaMeaning = "আমি কেবলামুখী হয়ে আল্লাহর উদ্দেশ্যে জোহরের ৪ রাকাত ফরজ নামাজ আদায়ের নিয়ত করছি।"
        ),
        DuaItem(
            id = "niyyat_wudu",
            titleBn = "অজুর নিয়ত",
            titleEn = "Wudu Intention",
            category = "Niyyat",
            arabicText = "نَوَيْتُ أَنْ أَتَوَضَّأَ لِرَفْعِ الْحَدَثِ وَاسْتِبَاحَةً لِلصَّلَاةِ",
            banglaPronunciation = "নাওয়াইতু আন আতাওয়াজ্জাআ লিরাফইল হাদাসি ওয়াস্থিবাহাতাল লিসসালাতি।",
            banglaMeaning = "আমি অপবিত্রতা দূর করতে এবং নামাজ আদায় হালাল করার জন্য অজুর নিয়ত করছি।"
        ),
        DuaItem(
            id = "dua_wudu_after",
            titleBn = "অজু শেষের দোয়া",
            titleEn = "Dua After Wudu",
            category = "Wudu Dua",
            arabicText = "أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، اللَّهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ الْمُتَطَهِّرِينَ",
            banglaPronunciation = "আশহাদু আল-লা ইলাহা ইল্লাল্লাহু ওয়াহদাহু লা শারিকা লাহু ওয়া আশহাদু আন্না মুহাম্মাদান আবদুহু ওয়া রাসুলুহু। আল্লাহুম্মাজ আলনি মিনাত্তাওয়াবিনা ওয়াজ আলনি মিনাল মুতাতাহহিরিন।",
            banglaMeaning = "আমি সাক্ষ্য দিচ্ছি যে আল্লাহ ছাড়া কোনো উপাস্য নেই, তিনি একক, তাঁর কোনো শরিক নেই। এবং মুহাম্মদ (সা.) তাঁর বান্দা ও রাসূল। হে আল্লাহ! আমাকে তওবাকারীদের অন্তর্ভুক্ত করো এবং পবিত্রতা অর্জনকারীদের অন্তর্ভুক্ত করো।"
        )
    )

    // 5. VISUAL STEP ILLUSTRATOR POSITIONS
    val visualPositions = listOf(
        PrayerStep(
            1, "১. দণ্ডায়মান অবস্থা (Qiyam)", "Standing Position (Qiyam)",
            "কিবলামুখী হয়ে সোজা হয়ে দাঁড়ান। দৃষ্টি থাকবে সিজদার স্থানে। হস্তদ্বয় বাঁধুন।",
            postureType = PostureType.QIYAM,
            maleNoteBn = "পুরুষরা দু'পায়ের মাঝে ৪ আঙুল পরিমাণ ফাঁকা রেখে দাঁড়াবেন এবং হাত নাভির নিচে বাঁধবেন।",
            femaleNoteBn = "নারীরা দু'পা মিলিয়ে দাঁড়াবেন এবং দুই হাত বুকের ওপর রাখবেন।"
        ),
        PrayerStep(
            2, "২. রুকু অবস্থা (Ruku)", "Bowing Position (Ruku)",
            "মাথা ও পিঠ সমান্তরাল রেখে নত হন। দুই হাত দিয়ে হাঁটু শক্ত করে ধরুন।",
            postureType = PostureType.RUKU,
            maleNoteBn = "পুরুষরা কনুই পাঁজর থেকে পৃথক রাখবেন এবং পিঠ টেবিলের মতো সোজা রাখবেন।",
            femaleNoteBn = "নারীরা অল্প নত হয়ে হাত হাঁটুতে রাখবেন, আঙুলসমূহ মিলিয়ে রাখবেন এবং শরীর জড়ো করে রাখবেন।"
        ),
        PrayerStep(
            3, "৩. সোজা হয়ে দাঁড়ানো (Qauma)", "Standing Straight (Qauma)",
            "রুকু থেকে সোজা হয়ে দাঁড়ান। হাত দুটি স্বাভাবিকভাবে দু'পাশে ঝুলিয়ে রাখুন।",
            postureType = PostureType.QAUMA
        ),
        PrayerStep(
            4, "৪. সিজদা অবস্থা (Sujud)", "Prostration Position (Sujud)",
            "কপাল, নাক, দু'হাত, হাঁটু ও পায়ের আঙুল মাটিতে রেখে সিজদা সম্পন্ন করুন।",
            postureType = PostureType.SUJUD,
            maleNoteBn = "পুরুষরা পেট উরু থেকে এবং বাহু পাঁজর থেকে দূরে রাখবেন। পায়ের আঙুল খাড়া রাখবেন।",
            femaleNoteBn = "নারীরা মাটির সাথে শরীর বিছিয়ে পেটের সাথে উরু মিলিয়ে হাত কনুইসহ মাটিতে রাখবেন।"
        ),
        PrayerStep(
            5, "৫. বৈঠক ও সালাম (Jalsa & Salam)", "Sitting & Ending Salam",
            "দুই সিজদার মাঝে বা তাশাহহুদের জন্য হাঁটু গেড়ে বসুন। শেষে ডানে ও বামে সালাম ফেরান।",
            postureType = PostureType.TASHAHHUD,
            maleNoteBn = "পুরুষরা বাম পা বিছিয়ে তার ওপর বসবেন এবং ডান পা খাড়া রাখবেন।",
            femaleNoteBn = "নারীরা দুই পা ডান দিকে বের করে নিতম্বের ওপর বসবেন (তাজাওরুক)।"
        )
    )
}

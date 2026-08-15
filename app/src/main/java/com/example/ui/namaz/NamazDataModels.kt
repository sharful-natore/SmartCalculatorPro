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
            "পবিত্র মাটি বা ধূলিমুক্ত পাথরে দু'হাত মেড়ে অতিরিক্ত ধূলা ঝেড়ে ফেলুন এবং পুরো মুখমণ্ডল একবার মাসাহ করুন।",
            postureType = PostureType.WUDU_GENERIC
        ),
        PrayerStep(
            3, "পুনরায় মাটিতে হাত মারা ও হাত মাসাহ", "Strike Dust & Wipe Arms",
            "পুনরায় মাটিতে দু'হাত মেড়ে প্রথমে বাম হাত দিয়ে ডান হাত কনুই পর্যন্ত এবং পরে ডান হাত দিয়ে বাম হাত কনুই পর্যন্ত মাসাহ করুন।",
            postureType = PostureType.WUDU_GENERIC
        )
    )

    val ghuslFarzList = listOf(
        "১. ভালোভাবে কুলি করা (গড়গড়াসহ)।",
        "২. নাকে পানি দিয়ে নাকের ভেতর পরিষ্কার করা।",
        "৩. সমস্ত শরীরে এমনভাবে পানি পৌঁছানো যেন একটি চুল পরিমাণ স্থানও শুকনো না থাকে।"
    )

    val ghuslSunnahSteps = listOf(
        "১. গোসলের নিয়ত করা এবং বিসমিল্লাহ বলা।",
        "২. দুই হাত কবজি পর্যন্ত ধোয়া।",
        "৩. শরীরে কোনো অপবিত্রতা থাকলে তা ধুয়ে পরিষ্কার করা।",
        "৪. সাধারণ অজুর ন্যায় অজু করা।",
        "৫. মাথায় ৩ বার পানি ঢালা যেন চুলের গোড়া ভিজে যায়।",
        "৬. প্রথমে ডান কাঁধে ও পরে বাম কাঁধে ৩ বার পানি ঢেলে সারা শরীর ভালোভাবে ধোয়া।"
    )

    // 2. DAILY WAQT PRAYERS
    val dailyWaqts = listOf(
        WaqtInfo(
            id = "fajr",
            nameBn = "ফজর",
            nameEn = "Fajr",
            arabicName = "صَلَاةُ الْفَجْرِ",
            totalRakat = 4,
            farz = 2,
            sunnatMuakkadah = 2,
            sunnatGairMuakkadah = 0,
            nafl = 0,
            witr = 0,
            descriptionBn = "সুবেহ সাদিক থেকে সূর্যোদয়ের পূর্ব পর্যন্ত ফজরের সময়। সুন্নতে মুয়াক্কাদাহ ২ রাকাত আগে এবং ফরজ ২ রাকাত পরে।",
            steps2Rakat = get2RakatSteps("ফজর")
        ),
        WaqtInfo(
            id = "dhuhr",
            nameBn = "জোহর",
            nameEn = "Dhuhr",
            arabicName = "صَلَاةُ الظُّهْرِ",
            totalRakat = 12,
            farz = 4,
            sunnatMuakkadah = 6, // 4 before farz, 2 after farz
            sunnatGairMuakkadah = 0,
            nafl = 2,
            witr = 0,
            descriptionBn = "সূর্য পশ্চিমাকাশে ঢলে পড়ার পর থেকে বস্তুর ছায়া দ্বিগুণ হওয়া পর্যন্ত জোহরের সময়। ৪ রাকাত সুন্নত + ৪ রাকাত ফরজ + ২ রাকাত সুন্নত + ২ রাকাত নফল।",
            steps2Rakat = get2RakatSteps("জোহর"),
            steps4Rakat = get4RakatSteps("জোহর")
        ),
        WaqtInfo(
            id = "asr",
            nameBn = "আসর",
            nameEn = "Asr",
            arabicName = "صَلَاةُ الْعَصْرِ",
            totalRakat = 8,
            farz = 4,
            sunnatMuakkadah = 0,
            sunnatGairMuakkadah = 4,
            nafl = 0,
            witr = 0,
            descriptionBn = "বস্তুর ছায়া দ্বিগুণ হওয়ার পর থেকে সূর্যাস্তের পূর্ব পর্যন্ত আসরের সময়। ৪ রাকাত গায়রে মুয়াক্কাদাহ সুন্নত ও ৪ রাকাত ফরজ।",
            steps2Rakat = get2RakatSteps("আসর"),
            steps4Rakat = get4RakatSteps("আসর")
        ),
        WaqtInfo(
            id = "maghrib",
            nameBn = "মাগরিব",
            nameEn = "Maghrib",
            arabicName = "صَلَاةُ الْمَغْرِبِ",
            totalRakat = 7,
            farz = 3,
            sunnatMuakkadah = 2,
            sunnatGairMuakkadah = 0,
            nafl = 2,
            witr = 0,
            descriptionBn = "সূর্যাস্তের পর থেকে পশ্চিমাকাশের লাল আভা অদৃশ্য হওয়া পর্যন্ত মাগরিবের সময়। ৩ রাকাত ফরজ + ২ রাকাত সুন্নত + ২ রাকাত নফল।",
            steps2Rakat = get2RakatSteps("মাগরিব"),
            steps3Rakat = get3RakatSteps("মাগরিব")
        ),
        WaqtInfo(
            id = "isha",
            nameBn = "এশা",
            nameEn = "Isha",
            arabicName = "صَلَاةُ الْعِشَاءِ",
            totalRakat = 15,
            farz = 4,
            sunnatMuakkadah = 2,
            sunnatGairMuakkadah = 4,
            nafl = 2,
            witr = 3,
            descriptionBn = "পশ্চিমাকাশের লাল আভা চলে যাওয়ার পর থেকে সুবেহ সাদিকের পূর্ব পর্যন্ত এশার সময়। ৪ গায়রে মুয়াক্কাদাহ + ৪ ফরজ + ২ সুন্নত + ২ নফল + ৩ বিতর।",
            steps2Rakat = get2RakatSteps("এশা"),
            steps3Rakat = getWitr3RakatSteps(),
            steps4Rakat = get4RakatSteps("এশা")
        ),
        WaqtInfo(
            id = "witr",
            nameBn = "বিতর",
            nameEn = "Witr",
            arabicName = "صَلَاةُ الْوِتْرِ",
            totalRakat = 3,
            farz = 0,
            sunnatMuakkadah = 0,
            sunnatGairMuakkadah = 0,
            nafl = 0,
            witr = 3,
            descriptionBn = "এশার নামাজের পর ওয়াজিব ৩ রাকাত বিতরের নামাজ। ৩য় রাকাতে সূরা ফাতেহা ও কিরাতের পর অতিরিক্ত তকবীর বলে দুআ কুনুত পাঠ করতে হয়।",
            steps2Rakat = emptyList(),
            steps3Rakat = getWitr3RakatSteps()
        )
    )

    // Helper step generators
    private fun get2RakatSteps(waqtName: String): List<PrayerStep> {
        return listOf(
            PrayerStep(
                1, "নিয়ত ও তাকবীরে তাহরিমা", "Takbeer & Intention",
                "কিবলামুখী হয়ে দাঁড়ান, নিয়ত করুন এবং 'আল্লাহু আকবার' বলে হাত উঠিয়ে নাভির নিচে (নারীরা বুকে) বাঁধুন।",
                postureType = PostureType.TAKBEER,
                arabicText = "اللَّهُ أَكْبَرُ",
                banglaPronunciation = "আল্লাহু আকবার",
                banglaMeaning = "আল্লাহ সবচেয়ে মহান।",
                maleNoteBn = "পুরুষরা দুই হাত কানের লতি পর্যন্ত তুলবেন এবং নাভির নিচে হাত বাঁধুন।",
                femaleNoteBn = "নারীরা দুই হাত কাঁধ/বুক পর্যন্ত তুলবেন এবং বুকের ওপর হাত বাঁধুন।"
            ),
            PrayerStep(
                2, "ছানা, সূরা ফাতেহা ও সূরা মেলানো (১ম রাকাত)", "Qiyam & Recitation",
                "হাত বেঁধে ছানা পাঠ করুন। এরপর আউযুবিল্লাহ-বিসমিল্লাহসহ সূরা ফাতেহা এবং যেকোনো একটি অন্য সূরা পাঠ করুন।",
                postureType = PostureType.QIYAM,
                arabicText = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَىٰ جَدُّكَ وَلَا إِلَٰهَ غَيْرُكَ",
                banglaPronunciation = "সুবহানাকা আল্লাহুম্মা ওয়া বিহামদিকা ওয়া তাবারাকাসমুকা ওয়া তাআলা জদ্দুকা ওয়া লা ইলাহা গাইরুক।",
                banglaMeaning = "হে আল্লাহ! তোমার প্রশংসা সহকারে তোমার পবিত্রতা ঘোষণা করছি, তোমার নাম বরকতময়, তোমার মর্যাদা সুউচ্চ এবং তুমি ছাড়া কোনো উপাস্য নেই।"
            ),
            PrayerStep(
                3, "রুকু (Ruku)", "Bow Down",
                "’আল্লাহু আকবার’ বলে রুকুতে যান। পিঠ সোজা রাখুন এবং দুই হাত দিয়ে হাঁটু শক্তভাবে ধরুন। ৩, ৫ বা ৭ বার তাসবিহ পাঠ করুন।",
                postureType = PostureType.RUKU,
                arabicText = "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                banglaPronunciation = "সুবহানা রাব্বিয়াল আজীম (৩ বার)",
                banglaMeaning = "আমার মহান প্রতিপালকের পবিত্রতা বর্ণনা করছি।",
                maleNoteBn = "পুরুষরা কনুই শরীর থেকে ফাঁকা রাখবেন এবং পিঠ সমান্তরাল রাখবেন।",
                femaleNoteBn = "নারীরা শরীর সংকুচিত রেখে রুকু করবেন এবং হাত হাঁটুতে হালকাভাবে রাখবেন।"
            ),
            PrayerStep(
                4, "কওমা (রুকু থেকে সোজা হয়ে দাঁড়ানো)", "Stand Straight (Qauma)",
                "'সামিয়াল্লাহু লিমান হামিদাহ' বলে সোজা হয়ে দাঁড়ান এবং বলুন 'রাব্বানা লাকাল হামদ'।",
                postureType = PostureType.QAUMA,
                arabicText = "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ - رَبَّنَا لَكَ الْحَمْدُ",
                banglaPronunciation = "সামিয়াল্লাহু লিমান হামিদাহ - রাব্বানা লাকাল হামদ",
                banglaMeaning = "আল্লাহ তাঁর প্রশংসাকারীর কথা শুনেছেন - হে আমাদের রব! সকল প্রশংসা তোমারই জন্য।"
            ),
            PrayerStep(
                5, "সিজদা (১ম ও ২য় সিজদা)", "Prostration (Sujud)",
                "'আল্লাহু আকবার' বলে সিজদায় যান। কপাল, নাক, দু'হাত, দু'হাঁটু ও দু'পায়ের আঙুল মাটিতে রাখুন। ৩ বার তাসবিহ বলুন। দুই সিজদার মাঝে জলসায় বসুন।",
                postureType = PostureType.SUJUD,
                arabicText = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                banglaPronunciation = "সুবহানা রাব্বিয়াল আলা (৩ বার)",
                banglaMeaning = "আমার সুউচ্চ প্রতিপালকের পবিত্রতা ঘোষণা করছি।",
                maleNoteBn = "পুরুষরা পেটের ওপর থেকে উরু এবং বাহু থেকে পাঁজর আলাদা রাখবেন। পায়ের আঙুল কিবলামুখী রাখবেন।",
                femaleNoteBn = "নারীরা পেটের সাথে উরু এবং বাহুর সাথে পাঁজর মিলিয়ে সংকুচিত হয়ে সিজদা করবেন।"
            ),
            PrayerStep(
                6, "২য় রাকাত ও বৈঠক (তশাহহুদ, দরুদ ও দোয়া মাসুরা)", "2nd Rakat & Final Tashahhud",
                "২য় রাকাত একই নিয়মে আদায় করে শেষ সিজদা শেষে বৈঠকে বসুন। আত্তাহিয়্যাতু, দরুদ শরিফ ও দোয়া মাসুরা পাঠ করুন।",
                postureType = PostureType.TASHAHHUD,
                arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ...",
                banglaPronunciation = "আত্তাহিয়্যাতু লিল্লাহি ওয়াস সালাওয়াতু ওয়াত তাইয়্যিবাতু...",
                banglaMeaning = "সকল মৌখিক, শারীরিক ও আর্থিক ইবাদত আল্লাহর জন্য..."
            ),
            PrayerStep(
                7, "সালাম ফিরানো (Salam)", "Ending Salam",
                "প্রথমে ডান দিকে মুখ ফিরিয়ে বলুন 'আসসালামু আলাইকুম ওয়া রহমাতুল্লাহ', এরপর বাম দিকে মুখ ফিরিয়ে সালাম দিন।",
                postureType = PostureType.SALAM,
                arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                banglaPronunciation = "আসসালামু আলাইকুম ওয়া রহমাতুল্লাহ",
                banglaMeaning = "আপনার ওপর আল্লাহর শান্তি ও রহমত বর্ষিত হোক।"
            )
        )
    }

    private fun get3RakatSteps(waqtName: String): List<PrayerStep> {
        val base = get2RakatSteps(waqtName).toMutableList()
        base[5] = PrayerStep(
            6, "প্রথম বৈঠক (তশাহহুদ পাঠ)", "First Tashahhud",
            "২য় রাকাতের সিজদা শেষে বসবেন এবং শুধুমাত্র 'আত্তাহিয়্যাতু' পাঠ করবেন। এরপর দরুদ না পড়ে 'আল্লাহু আকবার' বলে ৩য় রাকাতের জন্য উঠবেন।",
            postureType = PostureType.TASHAHHUD,
            arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ...",
            banglaPronunciation = "আত্তাহিয়্যাতু লিল্লাহি ওয়াস সালাওয়াতু...",
            banglaMeaning = "সকল মৌখিক, শারীরিক ইবাদত আল্লাহর জন্য।"
        )
        base.add(
            PrayerStep(
                7, "৩য় রাকাত ও শেষ বৈঠক", "3rd Rakat & Final Tashahhud",
                "৩য় রাকাতে শুধুমাত্র সূরা ফাতেহা (ফরজ হলে) পাঠ করে রুকু-সিজদা সম্পন্ন করুন এবং শেষ বৈঠকে বসে আত্তাহিয়্যাতু, দরুদ ও দোয়া মাসুরা পড়ে সালাম ফেরান।",
                postureType = PostureType.TASHAHHUD
            )
        )
        return base
    }

    private fun get4RakatSteps(waqtName: String): List<PrayerStep> {
        val base = get3RakatSteps(waqtName).toMutableList()
        base.add(
            PrayerStep(
                8, "৪র্থ রাকাত ও শেষ বৈঠক", "4th Rakat & Final Tashahhud",
                "৪র্থ রাকাতে সূরা ফাতেহা পড়ে রুকু-সিজদা সম্পন্ন করে শেষ বৈঠকে বসবেন। তাশাহহুদ, দরুদ ও দোয়া মাসুরা পড়ে দু'দিকে সালাম ফেরাবেন।",
                postureType = PostureType.SALAM
            )
        )
        return base
    }

    private fun getWitr3RakatSteps(): List<PrayerStep> {
        return listOf(
            PrayerStep(
                1, "বিতর নিয়ত ও প্রথম ২ রাকাত", "Witr First 2 Rakats",
                "বিতরের নিয়ত করে প্রথম ২ রাকাত সাধারণ নিয়মে আদায় করুন এবং ২য় রাকাত শেষে প্রথম বৈঠকে শুধুমাত্র আত্তাহিয়্যাতু পড়ে ৩য় রাকাতের জন্য দাঁড়ান।",
                postureType = PostureType.QIYAM
            ),
            PrayerStep(
                2, "৩য় রাকাতে সূরা ফাতেহা ও ক্বিরাআত", "3rd Rakat Recitation",
                "৩য় রাকাতে সূরা ফাতেহার পর অন্য একটি সূরা বা অতিরিক্ত আয়াত পাঠ করুন।",
                postureType = PostureType.QIYAM
            ),
            PrayerStep(
                3, "অতিরিক্ত তাকবীর ও দুআ কুনুত", "Extra Takbeer & Dua Qunut",
                "ক্বিরাআত শেষে রুকুতে না গিয়ে 'আল্লাহু আকবার' বলে হাত কান পর্যন্ত (নারীরা বুক পর্যন্ত) উঠিয়ে হাত বাঁধুন এবং দুআ কুনুত পাঠ করুন।",
                postureType = PostureType.TAKBEER,
                arabicText = "اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنُؤْمِنُ بِكَ وَنَتَوَكَّلُ عَلَيْكَ...",
                banglaPronunciation = "আল্লাহুম্মা ইন্না নাস্তাইনুকা ওয়া নাস্তাগফিরুকা ওয়া নু'মিনু বিকা ওয়া নাতাওয়াক্কালু আলাইকা...",
                banglaMeaning = "হে আল্লাহ! আমরা তোমারই সাহায্য প্রার্থনা করছি, তোমারই নিকট ক্ষমা চাচ্ছি এবং তোমার ওপর ভরসা করছি..."
            ),
            PrayerStep(
                4, "রুকু, সিজদা ও শেষ বৈঠক", "Ruku, Sujud & Salam",
                "দুআ কুনুত পড়া শেষে ’আল্লাহু আকবার’ বলে রুকু ও ২টি সিজদা সম্পন্ন করুন। শেষ বৈঠকে তাশাহহুদ, দরুদ ও দোয়া মাসুরা পড়ে সালাম ফেরান।",
                postureType = PostureType.SALAM
            )
        )
    }

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
                    arabicText = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَى جَدُّكَ وَجَلَّ ثَنَاؤُكَ وَلاَ إِلاَهَ غَيْرُكَ",
                    banglaPronunciation = "সুবহানাকা আল্লাহুম্মা ওয়া বিহামদিকা ওয়া তাবারাকাসমুকা ওয়া তাআলা জদ্দুকা ওয়া জাল্লা সানাউকা ওয়া লা ইলাহা গাইরুক।",
                    banglaMeaning = "হে আল্লাহ! তোমার প্রশংসা সহকারে পবিত্রতা বর্ণনা করছি, তোমার নাম বরকতময়, তোমার মর্যাদা সুউচ্চ, তোমার প্রশংসা সুমহান এবং তুমি ছাড়া কোনো মাবুদ নেই।"
                ),
                PrayerStep(
                    2, "২য় তাকবীর ও দরুদ শরিফ", "2nd Takbeer & Durood",
                    "হাত না উঠিয়ে ২য় তাকবীর 'আল্লাহু আকবার' বলুন এবং দরুদে ইব্রাহিম পাঠ করুন।",
                    postureType = PostureType.TAKBEER,
                    arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ...",
                    banglaPronunciation = "আল্লাহুম্মা সাল্লি আলা মুহাম্মাদিওঁ ওয়া আলা আলি মুহাম্মাদ...",
                    banglaMeaning = "হে আল্লাহ! মুহাম্মদ (সা.) এবং তাঁর বংশধরদের ওপর রহমত বর্ষণ করো..."
                ),
                PrayerStep(
                    3, "৩য় তাকবীর ও জানাজার দোয়া (প্রাপ্তবয়স্ক)", "3rd Takbeer & Adult Funeral Dua",
                    "হাত না উঠিয়ে ৩য় তাকবীর 'আল্লাহু আকবার' বলুন এবং মেয়্যেতের জন্য খাস দোয়া পাঠ করুন।",
                    postureType = PostureType.TAKBEER,
                    arabicText = "اللَّهُمَّ اغْفِرْ لِحَيِّنَا وَمَيِّتِنَا وَشَاهِدِنَا وَغَائِبِنَا وَصَغِيرِنَا وَكَبِيرِنَا وَذَكَرِنَا وَأُنْثَانَا...",
                    banglaPronunciation = "আল্লাহুম্মাগফির লিহাইয়্যিনা ওয়া মায়্যিতিনা ওয়া শাহেদিনা ওয়া গাইবিনা ওয়া সাগিরিনা ওয়া কাবিরিনা ওয়া জাকারিনা ওয়া উনছানা...",
                    banglaMeaning = "হে আল্লাহ! আমাদের জীবিত ও মৃত, উপস্থিত ও অনুপস্থিত, ছোট ও বড়, পুরুষ ও নারী সকলকে ক্ষমা করে দাও..."
                ),
                PrayerStep(
                    4, "অপ্রাপ্তবয়স্ক (শিশু) ছেলে ও মেয়ের জানাজার দোয়া", "Child Janazah Dua",
                    "নাবালেগ ছেলে শিশুর দোয়া: 'আল্লাহুম্মাজআলহু লানা ফারতাওঁ...', নাবালেগ মেয়ে শিশুর দোয়া: 'আল্লাহুম্মাজআলহা লানা ফারতাওঁ...'",
                    postureType = PostureType.DUA_GENERIC,
                    arabicText = "اللَّهُمَّ اجْعَلْهُ لَنَا فَرَطًا وَاجْعَلْهُ لَنَا أَجْرًا وَذُخْرًا وَاجْعَلْهُ لَنَا شَافِعًا وَمُشَفَّعًا",
                    banglaPronunciation = "আল্লাহুম্মাজআলহু লানা ফারতাওঁ ওয়াজআলহু লানা আজরাওঁ ওয়া জুখরাওঁ ওয়াজআলহু লানা শাফিআওঁ ওয়া মুশাফ্ফাআ।",
                    banglaMeaning = "হে আল্লাহ! এই শিশুকে আমাদের জন্য অগ্রগামী সুসংবাদ বানাও, তাকে আমাদের পরকালের প্রতিদান ও গচ্ছিত সম্পদ বানাও এবং তাকে আমাদের জন্য সুপারিশকারী ও গ্রহণযোগ্য সুপারিশকারী বানাও।"
                ),
                PrayerStep(
                    5, "৪র্থ তাকবীর ও সালাম", "4th Takbeer & Salam",
                    "৪র্থ তাকবীর 'আল্লাহু আকবার' বলে হাত না উঠিয়েই ডান ও বাম দিকে সালাম ফেরান।",
                    postureType = PostureType.SALAM,
                    arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                    banglaPronunciation = "আসসালামু আলাইকুম ওয়া রহমাতুল্লাহ"
                )
            )
        ),
        PrayerRule(
            id = "eid",
            titleBn = "ঈদের নামাজ (Eid-ul-Fitr & Eid-ul-Adha)",
            titleEn = "Eid Prayer Rules",
            category = "Eid",
            introductionBn = "ঈদুল ফিতর ও ঈদুল আজহার নামাজ ২ রাকাত ওয়াজিব। এতে অতিরিক্ত ৬টি তাকবীর বলতে হয়। জামাতে পড়া আবশ্যক এবং নামাজের পর খুতবা শোনা সুন্নত।",
            rakatsCountBn = "২ রাকাত ওয়াজিব",
            specialTakbeerCountBn = "অতিরিক্ত ৬ তাকবীর",
            khutbahNoteBn = "ঈদের নামাজের পর ২টি খুতবা প্রদান করা ও মনোযোগ দিয়ে শোনা সুন্নত।",
            steps = listOf(
                PrayerStep(
                    1, "নিয়ত ও ১ম রাকাতের ৩ অতিরিক্ত তাকবীর", "1st Rakat 3 Extra Takbeers",
                    "ঈদের নামাজের নিয়ত করে 'আল্লাহু আকবার' বলে হাত বাঁধুন এবং ছানা পড়ুন। এরপর অতিরিক্ত ৩ বার 'আল্লাহু আকবার' বলুন। ১ম ও ২য় তাকবীরে হাত কানের লতি পর্যন্ত উঠিয়ে ছেড়ে দিন, ৩য় তাকবীরে হাত উঠিয়ে বাঁধুন।",
                    postureType = PostureType.TAKBEER
                ),
                PrayerStep(
                    2, "ক্বিরাআত ও রুকু-সিজদা", "Recitation & Ruku",
                    "হাত বেঁধে ইমাম সাহেব সূরা ফাতেহা ও অন্য সূরা বড় আওয়াজে পড়বেন। এরপর সাধারণ নিয়মে রুকু ও সিজদা সম্পন্ন করে ২য় রাকাতের জন্য উঠবেন।",
                    postureType = PostureType.QIYAM
                ),
                PrayerStep(
                    3, "২য় রাকাতের ক্বিরাআত ও ৩ অতিরিক্ত তাকবীর", "2nd Rakat 3 Extra Takbeers",
                    "২য় রাকাতে দাঁড়াইয়া প্রথমে ইমাম সাহেব সূরা ফাতেহা ও অন্য সূরা পড়বেন। ক্বিরাআত শেষে রুকুতে যাওয়ার আগে অতিরিক্ত ৩ বার 'আল্লাহু আকবার' বলে হাত উঠিয়ে ছেড়ে দেবেন।",
                    postureType = PostureType.TAKBEER
                ),
                PrayerStep(
                    4, "রুকুর তাকবীর ও শেষ বৈঠক", "Ruku Takbeer & Salam",
                    "৪র্থ বার 'আল্লাহু আকবার' বলে হাত না উঠিয়ে সোজা রুকুতে চলে যাবেন। এরপর সিজদা ও শেষ বৈঠক সম্পন্ন করে সালাম ফেরাবেন।",
                    postureType = PostureType.SALAM
                )
            )
        ),
        PrayerRule(
            id = "jumuah",
            titleBn = "জুমুআর নামাজ (Jumu'ah)",
            titleEn = "Jumu'ah Friday Prayer",
            category = "Special",
            introductionBn = "শুক্রবার জোহরের ওয়াক্তে জুমুআর ২ রাকাত ফরজ নামাজ জামাতে আদায় করা মুসলিম পুরুষের ওপর ফরজ।",
            rakatsCountBn = "২ রাকাত ফরজ (জামাতে)",
            khutbahNoteBn = "ইমামের খুতবা চলাকালে চুপ থাকা ও মনোযোগ সহকারে খুতবা শোনা ওয়াজিব।",
            steps = listOf(
                PrayerStep(
                    1, "কাবলাল জুমুআ ৪ রাকাত", "4 Rakat Kablal Jumuah",
                    "খুতবার পূর্বে ৪ রাকাত সুন্নতে মুয়াক্কাদাহ কাবলাল জুমুআ একা একা আদায় করুন।",
                    postureType = PostureType.QIYAM
                ),
                PrayerStep(
                    2, "খুতবা শ্রবণ ও ২ রাকাত ফরজ", "Khutbah & 2 Rakat Farz",
                    "খুতবা শেষে ইমামের পেছনে ২ রাকাত জুমুআর ফরজ নামাজ জামাতে আদায় করুন।",
                    postureType = PostureType.QIYAM
                ),
                PrayerStep(
                    3, "বাদাল জুমুআ ৪ রাকাত", "4 Rakat Ba'dal Jumuah",
                    "ফরজ শেষে ৪ রাকাত সুন্নতে মুয়াক্কাদাহ বাদাল জুমুআ আদায় করুন।",
                    postureType = PostureType.SALAM
                )
            )
        ),
        PrayerRule(
            id = "tahajjud",
            titleBn = "তাহাজ্জুদ নামাজ (Tahajjud)",
            titleEn = "Tahajjud Night Prayer",
            category = "Nofl",
            introductionBn = "এশার নামাজের পর শেষ রাতে ঘুমানোর পর জেগে উঠে তাহাজ্জুদ নামাজ পড়া অত্যন্ত ফজিলতপূর্ণ নফল ইবাদত। নূন্যতম ২ রাকাত থেকে ১২ রাকাত পর্যন্ত পড়া যায়।",
            rakatsCountBn = "২, ৪, ৮ বা ১২ রাকাত (২ রাকাত করে)",
            extraNotesBn = "রাসূলুল্লাহ (সা.) সাধারণত ৮ রাকাত তাহাজ্জুদ পড়তেন।",
            steps = listOf(
                PrayerStep(
                    1, "তাহাজ্জুদের নিয়ত ও সালাত", "Tahajjud Flow",
                    "মনে মনে তাহাজ্জুদের নিয়ত করে ২ রাকাত করে যত রাকাত সম্ভব সুন্দর ও দীর্ঘ ক্বিরাআতে আদায় করুন।",
                    postureType = PostureType.QIYAM
                )
            )
        ),
        PrayerRule(
            id = "tasbih",
            titleBn = "সালাতুল তাসবিহ (Salatul Tasbih)",
            titleEn = "Salatul Tasbih Prayer",
            category = "Nofl",
            introductionBn = "সালাতুল তাসবিহ ৪ রাকাতের নফল নামাজ। এই নামাজে মোট ৩০০ বার বিশেষ তাসবিহ পাঠ করতে হয়। জীবনে অন্তত একবার হলেও এই নামাজ পড়া উচিত।",
            rakatsCountBn = "৪ রাকাত (মোট ৩০০ বার তাসবিহ)",
            extraNotesBn = "তাসবিহ: 'সুবহানাল্লাহি ওয়াল হামদুলিল্লাহি ওয়া লা ইলাহা ইল্লাল্লাহু والله اكبر'",
            steps = listOf(
                PrayerStep(
                    1, "বিশেষ তাসবিহ নিয়ম", "Tasbih Recitation Pattern",
                    "প্রতি রাকাতে ৭৫ বার করে ৪ রাকাতে মোট ৩০০ বার তাসবিহ পাঠ করতে হয়:\n• ছানার পর: ১৫ বার\n• ক্বিরাআত শেষে: ১০ বার\n• রুকুতে: ১০ বার\n• রুকু থেকে উঠে (কওমা): ১০ বার\n• ১ম সিজদায়: ১০ বার\n• দুই সিজদার মাঝে (জলসা): ১০ বার\n• ২য় সিজদায়: ১০ বার (মোট ৭৫ বার)।",
                    postureType = PostureType.QIYAM,
                    arabicText = "سُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ وَلَا إِلَٰهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ",
                    banglaPronunciation = "সুবহানাল্লাহি ওয়াল হামদুলিল্লাহি ওয়া লা ইলাহা ইল্লাল্লাহু ওয়াল্লাহু আকবার"
                )
            )
        ),
        PrayerRule(
            id = "tarabi",
            titleBn = "তারাবীহ নামাজ (Tarabi)",
            titleEn = "Taraweeh Prayer Rules",
            category = "Special",
            introductionBn = "রমজান মাসে এশার ফরজ ও সুন্নতের পর ২০ রাকাত তারাবীহ নামাজ সুন্নতে মুয়াক্কাদাহ। ২ রাকাত করে ১০ সালামে আদায় করা হয়।",
            rakatsCountBn = "২০ রাকাত (২ রাকাত করে)",
            extraNotesBn = "প্রতি ৪ রাকাত পর পর একটু বসে তারাবীহের দোয়া বা তাসবিহ পাঠ করা উত্তম।",
            steps = listOf(
                PrayerStep(
                    1, "তারাবীহের মুনাজাত ও তাসবিহ", "Tarabi Dua",
                    "৪ রাকাত পরপর পঠিত দোয়া: 'সুবহানা জিল মুলকি ওয়াল মালাকুতি...'",
                    postureType = PostureType.DUA_GENERIC,
                    arabicText = "سُبْحَانَ ذِي الْمُلْكِ وَالْمَلَكُوتِ، سُبْحَانَ ذِي الْعِزَّةِ وَالْعَظَمَةِ وَالْهَيْبَةِ وَالْقُدْرَةِ وَالْكِبْرِيَاءِ وَالْجَبَرُوتِ...",
                    banglaPronunciation = "সুবহানা জিল মুলকি ওয়াল মালাকুতি সুবহানা জিল ইযযাতি ওয়াল আমাঝাতি ওয়াল হাইবাতি ওয়াল কুদরাতি ওয়াল কিবরিয়ায়ি ওয়াল জাবারুত..."
                )
            )
        ),
        PrayerRule(
            id = "qaza",
            titleBn = "কাজা নামাজের হিসাব ও নিয়ম (Qaza Prayer)",
            titleEn = "Missed Prayer Guidance",
            category = "Special",
            introductionBn = "অনিচ্ছাকৃতভাবে কোনো ওয়াক্তের নামাজ ছুটে গেলে তা দ্রুত কাজা আদায় করা আবশ্যক। ফরজ ও বিতর নামাজের কাজা করতে হয়, সুন্নতের কাজা নেই।",
            rakatsCountBn = "ফজর (২), জোহর (৪), আসর (৪), মাগরিব (৩), এশা (৪) ও বিতর (৩)",
            steps = listOf(
                PrayerStep(
                    1, "কাজা আদায়ের তরতিব", "Qaza Order",
                    "যদি কয়েক ওয়াক্তের কাজা থাকে তবে ধারাবাহিকভাবে আদায় করুন। বেশি দিনের ছুটে যাওয়া কাজা থাকলে 'উমরী কাজা' হিসেবে ক্রমান্বয়ে আদায় করতে থাকুন।",
                    postureType = PostureType.NIYYAT
                )
            )
        )
    )

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

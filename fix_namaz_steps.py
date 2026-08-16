import re

with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "r", encoding="utf-8") as f:
    content = f.read()

replacement = """    private fun get2RakatSteps(waqtName: String): List<PrayerStep> {
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
                2, "ছানা ও ক্বিরাআত (সূরা পাঠ)", "Qiyam",
                "হাত বেঁধে প্রথমে ছানা পাঠ করুন। এরপর সূরা ফাতেহা ও অন্য একটি সূরা (যেমন: সূরা ইখলাস) পাঠ করুন।",
                postureType = PostureType.QIYAM,
                arabicText = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَىٰ جَدُّكَ وَلَا إِلَٰهَ غَيْرُكَ\nبِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nالْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ...",
                banglaPronunciation = "সুবহানাকা আল্লাহুম্মা ওয়া বিহামদিকা ওয়া তাবারাকাসমুকা ওয়া তাআলা জদ্দুকা ওয়া লা ইলাহা গাইরুক।\nবিসমিল্লাহির রাহমানির রাহিম...\nআলহামদু লিল্লাহি রাব্বিল আলামিন...",
                banglaMeaning = "হে আল্লাহ! তোমার প্রশংসা সহকারে পবিত্রতা ঘোষণা করছি... সমস্ত প্রশংসা বিশ্বজগতের প্রতিপালক আল্লাহর জন্য..."
            ),
            PrayerStep(
                3, "রুকু", "Ruku",
                "'আল্লাহু আকবার' বলে রুকুতে যান এবং রুকুর তাসবিহ অন্তত ৩ বার পাঠ করুন।",
                postureType = PostureType.RUKU,
                arabicText = "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                banglaPronunciation = "সুবহানা রাব্বিয়াল আযীম",
                banglaMeaning = "আমার মহান রবের পবিত্রতা বর্ণনা করছি।"
            ),
            PrayerStep(
                4, "কওমা (রুকু থেকে ওঠা)", "Qauma",
                "রুকু থেকে সোজা হয়ে দাঁড়ানোর সময় তাসমি' এবং সোজা হয়ে দাঁড়িয়ে তাহমিদ পাঠ করুন।",
                postureType = PostureType.QAUMA,
                arabicText = "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ\nرَبَّنَا لَكَ الْحَمْدُ",
                banglaPronunciation = "সামিআল্লাহু লিমান হামিদাহ\nরাব্বানা লাকাল হামদ",
                banglaMeaning = "যে আল্লাহর প্রশংসা করে, আল্লাহ তার কথা শোনেন।\nহে আমাদের রব! সমস্ত প্রশংসা আপনারই।"
            ),
            PrayerStep(
                5, "সিজদা", "Sujud",
                "'আল্লাহু আকবার' বলে সিজদায় যান এবং সিজদার তাসবিহ অন্তত ৩ বার পাঠ করুন। এরপর বসে আবার ২য় সিজদা করুন।",
                postureType = PostureType.SUJUD,
                arabicText = "سُبْحَانَ رَبِّيَ الْأَعْلَىٰ",
                banglaPronunciation = "সুবহানা রাব্বিয়াল আ'লা",
                banglaMeaning = "আমার সর্বোচ্চ রবের পবিত্রতা বর্ণনা করছি।"
            ),
            PrayerStep(
                6, "২য় রাকাত ও শেষ বৈঠক", "2nd Rakat & Tashahhud",
                "২য় রাকাত একইভাবে সম্পন্ন করে শেষ বৈঠকে বসুন। বৈঠকে আত্তাহিয়্যাতু, দরুদ শরিফ ও দোয়া মাসুরা পাঠ করুন।",
                postureType = PostureType.TASHAHHUD,
                arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ...\nاللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ...\nاللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي...",
                banglaPronunciation = "আত্তাহিয়্যাতু লিল্লাহি ওয়াস সালাওয়াতু... আল্লাহুম্মা সাল্লি আলা মুহাম্মাদ... আল্লাহুম্মা ইন্নি যালামতু নাফসি...",
                banglaMeaning = "সকল ইবাদত আল্লাহর জন্য... হে আল্লাহ! মুহাম্মাদ (সা.) এর উপর রহমত বর্ষণ করুন... হে আল্লাহ! আমি নিজের উপর অনেক জুলুম করেছি..."
            ),
            PrayerStep(
                7, "সালাম", "Salam",
                "প্রথমে ডান দিকে এবং পরে বাম দিকে মুখ ফিরিয়ে সালাম ফেরানোর মাধ্যমে নামাজ শেষ করুন।",
                postureType = PostureType.SALAM,
                arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                banglaPronunciation = "আসসালামু আলাইকুম ওয়া রহমাতুল্লাহ",
                banglaMeaning = "আপনাদের ওপর আল্লাহর শান্তি ও রহমত বর্ষিত হোক।"
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
            PrayerStep(6, "১ম বৈঠক", "1st Session", "২য় রাকাত শেষে বসে শুধুমাত্র আত্তাহিয়্যাতু পড়ুন।", postureType = PostureType.TASHAHHUD,
                arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ...",
                banglaPronunciation = "আত্তাহিয়্যাতু লিল্লাহি ওয়াস সালাওয়াতু...",
                banglaMeaning = "সকল কায়িক, বাচনিক ও আর্থিক ইবাদত একমাত্র আল্লাহর জন্য..."
            )
        )
        base.add(
            PrayerStep(7, "৩য় রাকাত ও সালাম", "3rd Rakat & Salam", "৩য় রাকাত (শুধু সূরা ফাতেহা) সম্পন্ন করে শেষ বৈঠকে বসুন। আত্তাহিয়্যাতু, দরুদ, দোয়া মাসুরা পড়ে সালাম ফেরান।", postureType = PostureType.SALAM,
                arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                banglaPronunciation = "আসসালামু আলাইকুম ওয়া রহমাতুল্লাহ",
                banglaMeaning = "আপনাদের ওপর আল্লাহর শান্তি ও রহমত বর্ষিত হোক।"
            )
        )
        return base
    }

    private fun get4RakatSteps(waqtName: String): List<PrayerStep> {
        val base = get3RakatSteps(waqtName).toMutableList()
        base.removeAt(base.size - 1)
        base.add(
            PrayerStep(7, "৩য় রাকাত", "3rd Rakat", "৩য় রাকাত (ফরজ হলে শুধু সূরা ফাতেহা, সুন্নত হলে সাথে অন্য সূরা) পড়ে সিজদা করুন।", postureType = PostureType.QIYAM,
                arabicText = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ...",
                banglaPronunciation = "আলহামদু লিল্লাহি রাব্বিল আলামিন...",
                banglaMeaning = "সমস্ত প্রশংসা বিশ্বজগতের প্রতিপালক আল্লাহর জন্য..."
            )
        )
        base.add(
            PrayerStep(8, "৪র্থ রাকাত ও সালাম", "4th Rakat & Salam", "৪র্থ রাকাত সম্পন্ন করে শেষ বৈঠকে বসুন। আত্তাহিয়্যাতু, দরুদ, দোয়া মাসুরা পড়ে সালাম ফেরান।", postureType = PostureType.SALAM,
                arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                banglaPronunciation = "আসসালামু আলাইকুম ওয়া রহমাতুল্লাহ",
                banglaMeaning = "আপনাদের ওপর আল্লাহর শান্তি ও রহমত বর্ষিত হোক।"
            )
        )
        return base
    }

    private fun getWitr3RakatSteps(): List<PrayerStep> {
        return listOf(
            PrayerStep(1, "বিতর ১ম ২ রাকাত", "First 2 Rakats", "সাধারণ ২ রাকাত নামাজের মত আদায় করে ১ম বৈঠকে বসে আত্তাহিয়্যাতু পাঠ করুন এবং ৩য় রাকাতের জন্য দাঁড়ান।", postureType = PostureType.QIYAM),
            PrayerStep(2, "৩য় রাকাত ও দুআ কুনুত", "3rd Rakat & Qunut", "৩য় রাকাতে সূরা ফাতেহা ও অন্য সূরা পড়ার পর 'আল্লাহু আকবার' বলে হাত কান পর্যন্ত উঠিয়ে আবার বাঁধুন এবং দুআ কুনুত পড়ুন।", postureType = PostureType.TAKBEER,
                arabicText = "اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنُؤْمِنُ بِكَ وَنَتَوَكَّلُ عَلَيْكَ وَنُثْنِي عَلَيْكَ الْخَيْرَ...",
                banglaPronunciation = "আল্লাহুম্মা ইন্না নাসতাঈনুকা ওয়া নাসতাগফিরুকা ওয়া নু'মিনু বিকা ওয়া নাতাওয়াক্কালু আলাইকা...",
                banglaMeaning = "হে আল্লাহ! আমরা আপনারই সাহায্য চাই, আপনারই কাছে ক্ষমা চাই, আপনার প্রতি ঈমান রাখি, আপনারই ওপর ভরসা করি এবং আপনার উত্তম প্রশংসা করি..."
            ),
            PrayerStep(3, "রুকু, সিজদা ও সালাম", "Ruku, Sujud & Salam", "এরপর রুকু, সিজদা ও শেষ বৈঠক করে সালাম ফেরান।", postureType = PostureType.SALAM)
        )
    }"""

start_idx = content.find("    private fun get2RakatSteps(waqtName: String): List<PrayerStep> {")
end_idx = content.find("    // 4. ALL NIYYAT & DUAS")

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + replacement + "\n" + content[end_idx:]

with open("app/src/main/java/com/example/ui/namaz/NamazDataModels.kt", "w", encoding="utf-8") as f:
    f.write(content)


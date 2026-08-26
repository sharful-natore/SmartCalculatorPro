package com.example.data.quran

import java.util.Locale

object QuranBanglaPronunciation {

    // Pre-mapped well-known Ayah pronunciations for highest accuracy
    private val wellKnownAyahs: Map<Pair<Int, Int>, String> = mapOf(
        // Surah 1: Al-Fatiha
        Pair(1, 1) to "বিসমিল্লাহির রাহমানির রাহিম",
        Pair(1, 2) to "আলহামদুলিল্লাহি রাব্বিল আলামিন",
        Pair(1, 3) to "আর-রাহমানির রাহিম",
        Pair(1, 4) to "মালিকি ইয়াওমিদ্দিন",
        Pair(1, 5) to "ইয়্যাকা নাবুদু ওয়া ইয়্যাকা নাস্তাঈন",
        Pair(1, 6) to "ইহদিনাস সিরাতাল মুস্তাকিম",
        Pair(1, 7) to "সিরাতাল্লাযিনা আনআমতা আলাইহিম গাইরিল মাগদুবি আলাইহিম ওয়ালাদ্দাল্লিন",

        // Surah 112: Al-Ikhlas
        Pair(112, 1) to "কুল হুয়াল্লাহু আহাদ",
        Pair(112, 2) to "আল্লাহুস সামাদ",
        Pair(112, 3) to "লাম ইয়ালিদ ওয়া লাম ইউলাদ",
        Pair(112, 4) to "ওয়া লাম ইয়াকুল্লাহু কুফুওয়ান আহাদ",

        // Surah 113: Al-Falaq
        Pair(113, 1) to "কুল আউযু বিরাব্বিল ফালাক",
        Pair(113, 2) to "মিন শাররি মা খালাক",
        Pair(113, 3) to "ওয়া মিন শাররি গাসিকিন ইযা ওয়াকাব",
        Pair(113, 4) to "ওয়া মিন শাররিন নাফ্ফাসাতি ফিল উকাদ",
        Pair(113, 5) to "ওয়া মিন শাররি হাসিদিন ইযা হাসাদ",

        // Surah 114: An-Nas
        Pair(114, 1) to "কুল আউযু বিরাব্বিন নাস",
        Pair(114, 2) to "মালিকিন নাস",
        Pair(114, 3) to "ইলাহিন নাস",
        Pair(114, 4) to "মিন শাররিল ওয়াসওয়াসিল খান্নাস",
        Pair(114, 5) to "আল্লাযী ইউওয়াসউইসু ফী সুদূরিন নাস",
        Pair(114, 6) to "মিনাল জিন্নাতি ওয়ান নাস",

        // Surah 108: Al-Kawthar
        Pair(108, 1) to "ইন্না আ'তাইনা কাল কাওসার",
        Pair(108, 2) to "ফাসাল্লি লিরাব্বিকা ওয়ানহার",
        Pair(108, 3) to "ইন্না শানিয়াকা হুয়াল আবতার",

        // Surah 109: Al-Kafirun
        Pair(109, 1) to "কুল ইয়া আইয়্যুহাল কাফিরূন",
        Pair(109, 2) to "লা আ'বুদু মা তা'বুদূন",
        Pair(109, 3) to "ওয়ালা আনতুম আবিদূনা মা আ'বুদ",
        Pair(109, 4) to "ওয়ালা আনা আবিদুম মা আবাত্তুম",
        Pair(109, 5) to "ওয়ালা আনতুম আবিদূনা মা আ'বুদ",
        Pair(109, 6) to "লাকুম দীনুকুম ওয়ালিয়া দীন",

        // Surah 110: An-Nasr
        Pair(110, 1) to "ইযা জাআ নাসরুল্লাহি ওয়াল ফাতহ",
        Pair(110, 2) to "ওয়া রাআইতান নাসা ইয়াদখুলূনা ফী দীনিল্লাহি আফওয়াজা",
        Pair(110, 3) to "ফাসাব্বিহ বিহামদি রাব্বিকা ওয়াসতাগফিরহু ইন্নাহু কানা তাওওয়াবা",

        // Surah 111: Al-Masad / Al-Lahab
        Pair(111, 1) to "তাব্বাত ইয়াদা আবী লাহাবিওঁ ওয়াতাব্ব",
        Pair(111, 2) to "মা আগনা আনহু মালুহু ওয়ামা কাসাব",
        Pair(111, 3) to "সাইয়াসলা নারান যাতা লাহাব",
        Pair(111, 4) to "ওয়ামরাআতুহু হাম্মালাতাল হাতাব",
        Pair(111, 5) to "ফী জীদিহা হাবলুম মিম মাসাদ",

        // Ayat Al-Kursi (2:255)
        Pair(2, 255) to "আল্লাহু লা ইলাহা ইল্লা হুয়াল হাইয়্যুল কাইয়্যুম, লা তা'খুযুহু সিনাতুঁও ওয়ালা নাওম, লাহু মা ফিস সামাওয়াতি ওয়ামা ফিল আরদ, মান যাল্লাযী ইয়্যাশফা'উ ইন্দাহু ইল্লা বিইযনিহ, ইয়া'লামু মা বাইনা আইদীহিম ওয়ামা খালফাহুম, ওয়ালা ইয়ুহীতূনা বিশাইইম মিন ইলমিহী ইল্লা বিমা শাআ, ওয়াসিআ কুরসিয়্যুহুস সামাওয়াতি ওয়াল আরদ, ওয়ালা ইয়াঊদুহু হিফযুহুমা ওয়া হুয়াল আলিয়্যুল আযীম"
    )

    // Common Arabic vocabulary mapping for fluent transliteration
    private val wordMap: Map<String, String> = mapOf(
        "bismi" to "বিসমি",
        "allahi" to "আল্লাহি",
        "allahu" to "আল্লাহু",
        "allaha" to "আল্লাহ",
        "ar-rahmani" to "আর-রাহমানি",
        "ar-rahmani" to "আর-রাহমানি",
        "ar-rahim" to "আর-রাহিম",
        "ar-rahimi" to "আর-রাহিমি",
        "al-hamdu" to "আল-হামদু",
        "lillahi" to "লিল্লাহি",
        "rabbi" to "রাব্বি",
        "rabbil" to "রাব্বিল",
        "al-alamin" to "আল-আলামিন",
        "al-alamina" to "আল-আলামিন",
        "maliki" to "মালিকি",
        "yawmi" to "ইয়াওমি",
        "ad-din" to "আদ-দ্বীন",
        "ad-dini" to "আদ-দ্বীন",
        "iyyaka" to "ইয়্যাকা",
        "na'budu" to "নাবুদু",
        "wa-iyyaka" to "ওয়া-ইয়্যাকা",
        "nasta'in" to "নাস্তাঈন",
        "nasta'inu" to "নাস্তাঈনু",
        "ihdina" to "ইহদিনা",
        "as-sirata" to "আস-সিরাতা",
        "al-mustaqim" to "আল-মুস্তাকিম",
        "al-mustaqima" to "আল-মুস্তাকিমা",
        "sirata" to "সিরাতা",
        "alladhina" to "আল্লাযিনা",
        "an'amta" to "আনআমতা",
        "alayhim" to "আলাইহিম",
        "ghayri" to "গাইরি",
        "al-maghdubi" to "আল-মাগদূবি",
        "wala" to "ওয়ালা",
        "ad-dallin" to "আদ-দাল্লিন",
        "qul" to "কুল",
        "huwa" to "হুয়া",
        "ahad" to "আহাদ",
        "ahadun" to "আহাদ",
        "as-samad" to "আস-সামাদ",
        "lam" to "লাম",
        "yalid" to "ইয়ালিদ",
        "wa-lam" to "ওয়া লাম",
        "yulad" to "ইউলাদ",
        "kufuwan" to "কুফুওয়ান",
        "inna" to "ইন্না",
        "a'tayna" to "আ'তায়না",
        "al-kawthar" to "আল-কাউসার",
        "fasalli" to "ফাসাল্লি",
        "lirabbika" to "লিরাব্বিকা",
        "wanhar" to "ওয়ানহার",
        "shani'aka" to "শানিয়াকা",
        "al-abtar" to "আল-আবতার",
        "min" to "মিন",
        "sharri" to "শাররি",
        "ma" to "মা",
        "khalaq" to "খালাক",
        "wa-min" to "ওয়া মিন",
        "gasikin" to "গাসিকিন",
        "iza" to "ইযা",
        "wakab" to "ওয়াকাব",
        "naffasati" to "নাফফাসাতি",
        "fil" to "ফিল",
        "uqad" to "উকাদ",
        "hasidin" to "হাসিদিন",
        "hasad" to "হাসাদ",
        "nas" to "নাস",
        "malikin" to "মালিকিন",
        "ilahin" to "ইলাহিন",
        "waswasil" to "ওয়াসওয়াসিল",
        "khannas" to "খান্নাস",
        "allazi" to "আল্লাযী",
        "yuwaswisu" to "ইউওয়াসউইসু",
        "fi" to "ফী",
        "sudurin" to "সুদূরিন",
        "minal" to "মিনাল",
        "jinnati" to "জিন্নাতি",
        "wannas" to "ওয়ান নাস"
    )

    fun getPronunciation(surahNumber: Int, ayahNumberInSurah: Int, englishTransliteration: String): String {
        // 1. Check exact Surah + Ayah cache
        val exact = wellKnownAyahs[Pair(surahNumber, ayahNumberInSurah)]
        if (exact != null) return exact

        if (englishTransliteration.isBlank()) return ""

        // 2. Perform intelligent English/Arabic transliteration to Bengali
        val rawBangla = convertTransliterationToBangla(englishTransliteration)
        
        // 3. Sanitize and clean up the final output
        return sanitizeBangla(rawBangla)
    }

    private fun sanitizeBangla(input: String): String {
        var text = input
            // Remove hyphens completely as they are seen as "machine-generated" artifacts
            .replace("-", "")
            
            // Remove duplicate vowel signs (kar-chinha)
            .replace("াা", "া")
            .replace("িি", "ি")
            .replace("ীী", "ী")
            .replace("ুু", "ু")
            .replace("ূূ", "ূ")
            .replace("েে", "ে")
            .replace("ৈৈ", "ৈ")
            .replace("োো", "ো")
            .replace("ৌৌ", "ৌ")
            
            // Clean up apostrophes and weird symbols that make reading hard
            .replace("’", "")
            .replace("'", "")
            .replace("ঃ ", " ") // Remove unnecessary visarga at word ends
            .replace("ঃ", "")
            .replace("`", "")
            
            // Fix spacing issues commonly found in transliteration
            .replace("\\s+".toRegex(), " ")
            .trim()

        return text
    }

    private fun convertTransliterationToBangla(input: String): String {
        var text = input
            .replace("ā", "aa")
            .replace("ī", "ee")
            .replace("ū", "oo")
            .replace("ḍ", "d")
            .replace("ḥ", "h")
            .replace("ṣ", "s")
            .replace("ṭ", "t")
            .replace("ẓ", "z")
            .replace("ġ", "gh")
            .replace("ḫ", "kh")
            .replace("š", "sh")
            .replace("ḏ", "dh")
            .replace("ʿ", "'")
            .replace("’", "'")
            .replace("ʾ", "")

        val words = text.split("\\s+".toRegex())
        val convertedWords = words.map { rawWord ->
            val cleanWord = rawWord.lowercase(Locale.ROOT).replace(Regex("[^a-z'-]"), "")
            val punctuation = rawWord.filter { it in ".,;:!?" }

            val mapped = wordMap[cleanWord]
            if (mapped != null) {
                mapped + punctuation
            } else {
                transliterateWord(cleanWord) + punctuation
            }
        }

        return convertedWords.joinToString(" ")
    }

    private fun transliterateWord(word: String): String {
        if (word.isEmpty()) return ""

        var w = word.lowercase(Locale.ROOT)
        // Normalize prefixes - NO HYPHENS as per user requirement
        w = w.replace("al-", "আল")
        w = w.replace("wa-", "ওয়া")
        w = w.replace("fa-", "ফা")
        w = w.replace("bi-", "বি")
        w = w.replace("la-", "লা")

        val sb = StringBuilder()
        var i = 0
        while (i < w.length) {
            // Match multi-character patterns
            when {
                w.startsWith("allah", i) -> { sb.append("আল্লাহ"); i += 5 }
                w.startsWith("sh", i) -> { sb.append("শ"); i += 2 }
                w.startsWith("kh", i) -> { sb.append("খ"); i += 2 }
                w.startsWith("gh", i) -> { sb.append("গ"); i += 2 }
                w.startsWith("th", i) -> { sb.append("ছ"); i += 2 }
                w.startsWith("dh", i) -> { sb.append("য"); i += 2 }
                w.startsWith("zh", i) -> { sb.append("য"); i += 2 }
                w.startsWith("ph", i) -> { sb.append("ফ"); i += 2 }
                w.startsWith("aa", i) || w.startsWith("ā", i) -> {
                    if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("আ") 
                    else if (sb.last().toString() != "া") sb.append("া")
                    i += if (w.startsWith("aa", i)) 2 else 1
                }
                w.startsWith("ee", i) || w.startsWith("ī", i) -> {
                    if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("ঈ") 
                    else if (sb.last().toString() != "ী") sb.append("ী")
                    i += if (w.startsWith("ee", i)) 2 else 1
                }
                w.startsWith("oo", i) || w.startsWith("ū", i) -> {
                    if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("ঊ") 
                    else if (sb.last().toString() != "ূ") sb.append("ূ")
                    i += if (w.startsWith("oo", i)) 2 else 1
                }
                w.startsWith("ai", i) -> {
                    if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("আই") 
                    else if (sb.last().toString() != "াই") sb.append("াই")
                    i += 2
                }
                w.startsWith("au", i) -> {
                    if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("আউ") 
                    else if (sb.last().toString() != "াউ") sb.append("াউ")
                    i += 2
                }
                w.startsWith("ou", i) -> {
                    if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("উ") 
                    else if (sb.last().toString() != "ু") sb.append("ু")
                    i += 2
                }
                else -> {
                    val ch = w[i]
                    when (ch) {
                        'a' -> {
                            if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("আ") 
                            else if (sb.last().toString() != "া") sb.append("া")
                        }
                        'i' -> {
                            if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("ই") 
                            else if (sb.last().toString() != "ি") sb.append("ি")
                        }
                        'u' -> {
                            if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("উ") 
                            else if (sb.last().toString() != "ু") sb.append("ু")
                        }
                        'e' -> {
                            if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("এ") 
                            else if (sb.last().toString() != "ে") sb.append("ে")
                        }
                        'o' -> {
                            if (sb.isEmpty() || sb.last().isWhitespace()) sb.append("ও") 
                            else if (sb.last().toString() != "ো") sb.append("ো")
                        }
                        'b' -> sb.append("ব")
                        'c' -> sb.append("ক")
                        'd' -> sb.append("দ")
                        'f' -> sb.append("ফ")
                        'g' -> sb.append("গ")
                        'h' -> sb.append("হ")
                        'j' -> sb.append("জ")
                        'k' -> sb.append("ক")
                        'l' -> sb.append("ল")
                        'm' -> sb.append("ম")
                        'n' -> sb.append("ন")
                        'p' -> sb.append("প")
                        'q' -> sb.append("ক্ব")
                        'r' -> sb.append("র")
                        's' -> sb.append("স")
                        't' -> sb.append("ত")
                        'v' -> sb.append("ভ")
                        'w' -> sb.append("ওয়া")
                        'y' -> sb.append("ইয়া")
                        'z' -> sb.append("য")
                        '\'' -> { /* Ignore apostrophes in conversion as per rule */ }
                        '-' -> { /* Ignore hyphens */ }
                        else -> sb.append(ch)
                    }
                    i++
                }
            }
        }

        return sb.toString()
    }
}

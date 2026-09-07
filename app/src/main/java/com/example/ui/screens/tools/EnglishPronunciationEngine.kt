package com.example.ui.screens.tools

/**
 * Intelligent English-to-Bangla Phonetic Pronunciation Engine
 * Converts English vocabulary into natural Bengali syllable sounds and conjuncts.
 */
object EnglishPronunciationEngine {

    fun generateBanglaPronunciation(word: String): String {
        val w = word.lowercase().trim()
        if (w.isEmpty()) return ""

        // Curated special words & irregulars
        val special = SPECIAL_WORDS[w]
        if (special != null) return special

        var s = w
        // Multi-letter replacements (Prefixes / Suffixes / Blends)
        s = s.replace("ph", "ফ")
            .replace("th", "থ")
            .replace("ch", "চ")
            .replace("sh", "শ")
            .replace("qu", "কুই")
            .replace("ck", "ক")
            .replace("str", "স্ট্র")
            .replace("spr", "স্প্র")
            .replace("scr", "স্ক্র")
            .replace("dr", "ড্র")
            .replace("tr", "ট্র")
            .replace("br", "ব্র")
            .replace("pr", "প্র")
            .replace("kr", "ক্র")
            .replace("gr", "গ্র")
            .replace("fr", "ফ্র")
            .replace("st", "স্ট")
            .replace("sk", "স্ক")
            .replace("sp", "স্প")
            .replace("sl", "স্ল")
            .replace("sm", "স্ম")
            .replace("sn", "স্ন")
            .replace("sw", "স্ব")
            .replace("tion", "শন")
            .replace("sion", "শন")
            .replace("cian", "শিয়ান")
            .replace("tian", "শিয়ান")
            .replace("able", "েবল")
            .replace("ible", "িবল")
            .replace("ment", "মেন্ট")
            .replace("ness", "নেস")
            .replace("ist", "িস্ট")
            .replace("ism", "িজম")
            .replace("ing", "িং")
            .replace("ous", "াস")
            .replace("ious", "িয়াস")
            .replace("ee", "ী")
            .replace("oo", "ু")
            .replace("ea", "ী")
            .replace("ai", "েই")
            .replace("ay", "ে")
            .replace("oa", "ো")
            .replace("ou", "াউ")
            .replace("b", "ব")
            .replace("d", "ড")
            .replace("f", "ফ")
            .replace("g", "গ")
            .replace("h", "হ")
            .replace("j", "জ")
            .replace("k", "ক")
            .replace("l", "ল")
            .replace("m", "ম")
            .replace("n", "ন")
            .replace("p", "প")
            .replace("r", "র")
            .replace("s", "স")
            .replace("t", "ট")
            .replace("v", "ভ")
            .replace("w", "উ")
            .replace("x", "ক্স")
            .replace("y", "ই")
            .replace("z", "জ")
            .replace("a", "া")
            .replace("e", "ে")
            .replace("i", "ি")
            .replace("o", "ো")
            .replace("u", "ু")

        // Clean initial vowel kar signs into independent vowels
        var res = s
        if (res.startsWith("া")) res = "আ" + res.substring(1)
        else if (res.startsWith("ে")) res = "এ" + res.substring(1)
        else if (res.startsWith("ি")) res = "ই" + res.substring(1)
        else if (res.startsWith("ো")) res = "ও" + res.substring(1)
        else if (res.startsWith("ু")) res = "উ" + res.substring(1)

        return res
    }

    private val SPECIAL_WORDS = mapOf(
        "draconian" to "ড্রাকোনিয়ান",
        "draft" to "ড্রাফট",
        "dramatic" to "ড্রামাটিক",
        "drastic" to "ড্রাস্টিক",
        "schedule" to "শিডিউল",
        "psychology" to "সাইকোলজি",
        "knowledge" to "নলেজ",
        "knight" to "নাইট",
        "debt" to "ডেট",
        "doubt" to "ডাউট",
        "subtle" to "সাট্‌ল",
        "receipt" to "রিসীট",
        "island" to "আইল্যান্ড",
        "colonel" to "কার্নেল",
        "choir" to "কোয়ার",
        "chaos" to "কেয়াস",
        "queue" to "কিউ",
        "rhythm" to "রিদম",
        "gauge" to "গেইজ",
        "lieutenant" to "লেফটেন্যান্ট",
        "bureaucracy" to "ব্যুরোক্রেসি",
        "entrepreneur" to "আঁত্রাপ্রেনার",
        "renaissance" to "রেনেসাঁস",
        "pneumonia" to "নিউমোনিয়া"
    )
}

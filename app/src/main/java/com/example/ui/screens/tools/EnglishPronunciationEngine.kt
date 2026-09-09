package com.example.ui.screens.tools

/**
 * Intelligent English-to-Bangla Phonetic Pronunciation Engine
 * Converts English vocabulary into natural Bengali syllable sounds and authentic phonetic forms.
 */
object EnglishPronunciationEngine {

    fun generateBanglaPronunciation(word: String): String {
        val w = word.lowercase().trim()
        if (w.isEmpty()) return ""

        // Curated special words & irregular pronunciations
        val special = SPECIAL_WORDS[w]
        if (special != null) return special

        var s = w

        // Syllable / Prefix mappings at the start of word
        if (s.startsWith("dis")) s = "ডিস" + s.substring(3)
        else if (s.startsWith("mis")) s = "মিস" + s.substring(3)
        else if (s.startsWith("un")) s = "আন" + s.substring(2)
        else if (s.startsWith("in")) s = "ইন" + s.substring(2)
        else if (s.startsWith("im")) s = "ইম" + s.substring(2)
        else if (s.startsWith("en")) s = "এন" + s.substring(2)
        else if (s.startsWith("em")) s = "এম" + s.substring(2)
        else if (s.startsWith("re")) s = "রি" + s.substring(2)
        else if (s.startsWith("pre")) s = "প্রি" + s.substring(3)
        else if (s.startsWith("pro")) s = "প্রো" + s.substring(3)
        else if (s.startsWith("sub")) s = "সাব" + s.substring(3)
        else if (s.startsWith("con")) s = "কন" + s.substring(3)
        else if (s.startsWith("com")) s = "কম" + s.substring(3)
        else if (s.startsWith("ex")) s = "এক্স" + s.substring(2)
        else if (s.startsWith("inter")) s = "ইন্টার" + s.substring(5)
        else if (s.startsWith("super")) s = "সুপার" + s.substring(5)
        else if (s.startsWith("trans")) s = "ট্রান্স" + s.substring(5)
        else if (s.startsWith("over")) s = "ওভার" + s.substring(4)
        else if (s.startsWith("under")) s = "আন্ডার" + s.substring(5)
        else if (s.startsWith("auto")) s = "অটো" + s.substring(4)
        else if (s.startsWith("anti")) s = "অ্যান্টি" + s.substring(4)

        // Suffix transformations
        if (s.endsWith("tion")) s = s.removeSuffix("tion") + "শন"
        else if (s.endsWith("sion")) s = s.removeSuffix("sion") + "শন"
        else if (s.endsWith("ssion")) s = s.removeSuffix("ssion") + "শন"
        else if (s.endsWith("cian")) s = s.removeSuffix("cian") + "শিয়ান"
        else if (s.endsWith("ture")) s = s.removeSuffix("ture") + "চার"
        else if (s.endsWith("sure")) s = s.removeSuffix("sure") + "শার"
        else if (s.endsWith("ment")) s = s.removeSuffix("ment") + "মেন্ট"
        else if (s.endsWith("ness")) s = s.removeSuffix("ness") + "নেস"
        else if (s.endsWith("able")) s = s.removeSuffix("able") + "েবল"
        else if (s.endsWith("ible")) s = s.removeSuffix("ible") + "িবল"
        else if (s.endsWith("ful")) s = s.removeSuffix("ful") + "ফুল"
        else if (s.endsWith("less")) s = s.removeSuffix("less") + "লেস"
        else if (s.endsWith("ship")) s = s.removeSuffix("ship") + "শিপ"
        else if (s.endsWith("hood")) s = s.removeSuffix("hood") + "হুড"
        else if (s.endsWith("ward")) s = s.removeSuffix("ward") + "ওয়ার্ড"
        else if (s.endsWith("ity")) s = s.removeSuffix("ity") + "ইটি"
        else if (s.endsWith("ous")) s = s.removeSuffix("ous") + "াস"
        else if (s.endsWith("ious")) s = s.removeSuffix("ious") + "িয়াস"
        else if (s.endsWith("ize")) s = s.removeSuffix("ize") + "াইজ"
        else if (s.endsWith("ise")) s = s.removeSuffix("ise") + "াইজ"
        else if (s.endsWith("ing")) s = s.removeSuffix("ing") + "িং"
        else if (s.endsWith("ist")) s = s.removeSuffix("ist") + "িস্ট"
        else if (s.endsWith("ism")) s = s.removeSuffix("ism") + "িজম"
        else if (s.endsWith("er")) s = s.removeSuffix("er") + "ার"
        else if (s.endsWith("or")) s = s.removeSuffix("or") + "র"
        else if (s.endsWith("ar")) s = s.removeSuffix("ar") + "ার"
        else if (s.endsWith("al")) s = s.removeSuffix("al") + "াল"
        else if (s.endsWith("ic")) s = s.removeSuffix("ic") + "িক"
        else if (s.endsWith("ly")) s = s.removeSuffix("ly") + "লি"

        // Multi-letter replacements & clusters
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

        // Intelligent 'c' handling (Soft c vs Hard c vs sc)
        s = s.replace(Regex("cc[eiy]"), "ক্স")
            .replace("cc", "ক")
            .replace(Regex("sc[eiy]"), "স")
            .replace("sc", "স্ক")
            .replace(Regex("c[eiy]"), "স")
            .replace("c", "ক")

        // Vowel digraphs & long vowels
        s = s.replace("ee", "ী")
            .replace("oo", "ু")
            .replace("ea", "ী")
            .replace("ai", "েই")
            .replace("ay", "ে")
            .replace("oa", "ো")
            .replace("ou", "াউ")
            .replace("igh", "আই")

        // Single consonants
        s = s.replace("b", "ব")
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

        // Vowels
        s = s.replace("a", "া")
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

        // ABSOLUTE SANITY CHECK: Strip any lingering Latin/ASCII characters
        res = res.replace(Regex("[a-zA-Z]"), "").trim()
        return if (res.isNotBlank()) res else word
    }

    fun generateIpaPhonetic(word: String): String {
        val w = word.lowercase().trim()
        if (w.isEmpty()) return ""

        val special = SPECIAL_IPA[w]
        if (special != null) return special

        var ipa = w
        // Prefix substitutions
        ipa = ipa.replace(Regex("^dis"), "dɪs")
            .replace(Regex("^mis"), "mɪs")
            .replace(Regex("^un"), "ʌn")
            .replace(Regex("^in"), "ɪn")
            .replace(Regex("^im"), "ɪm")
            .replace(Regex("^re"), "riː")
            .replace(Regex("^pre"), "prɪ")
            .replace(Regex("^pro"), "prəʊ")
            .replace(Regex("^sub"), "sʌb")
            .replace(Regex("^con"), "kən")
            .replace(Regex("^com"), "kəm")
            .replace(Regex("^ex"), "ɛks")
            .replace(Regex("^inter"), "ˌɪn.tə")
            .replace(Regex("^super"), "ˈsuː.pə")
            .replace(Regex("^trans"), "trænz")

        // Suffix substitutions
        ipa = ipa.replace(Regex("tion$"), "ʃən")
            .replace(Regex("sion$"), "ʃən")
            .replace(Regex("ture$"), "tʃə")
            .replace(Regex("sure$"), "ʃə")
            .replace(Regex("ment$"), "mənt")
            .replace(Regex("ness$"), "nəs")
            .replace(Regex("able$"), "əbl")
            .replace(Regex("ible$"), "əbl")
            .replace(Regex("ful$"), "fʊl")
            .replace(Regex("less$"), "ləs")
            .replace(Regex("ity$"), "ɪti")
            .replace(Regex("ous$"), "əs")
            .replace(Regex("ize$"), "aɪz")
            .replace(Regex("er$"), "ə")
            .replace(Regex("or$"), "ə")
            .replace(Regex("ar$"), "ə")
            .replace(Regex("al$"), "əl")
            .replace(Regex("ic$"), "ɪk")
            .replace(Regex("ly$"), "li")

        ipa = ipa.replace("ph", "f")
            .replace("th", "θ")
            .replace("ch", "tʃ")
            .replace("sh", "ʃ")
            .replace("qu", "kw")
            .replace("ck", "k")
            .replace(Regex("c[eiy]"), "s")
            .replace("c", "k")
            .replace("ee", "iː")
            .replace("oo", "uː")
            .replace("ea", "iː")
            .replace("ai", "eɪ")
            .replace("ay", "eɪ")
            .replace("oa", "əʊ")
            .replace("ou", "aʊ")

        return "/ˈ${ipa}/"
    }

    private val SPECIAL_IPA = mapOf(
        "discover" to "/dɪskˈʌvɐ/",
        "discuss" to "/dɪskˈʌs/",
        "disease" to "/dɪzˈiːz/",
        "accept" to "/əkˈsept/",
        "accurate" to "/ˈæk.jə.rət/",
        "ancient" to "/ˈeɪn.ʃənt/",
        "schedule" to "/ˈʃedʒ.uːl/",
        "psychology" to "/saɪˈkɒl.ə.dʒi/",
        "knowledge" to "/ˈnɒl.ɪdʒ/",
        "environment" to "/ɪnˈvaɪ.rən.mənt/",
        "ensure" to "/ɪnˈʃʊə/",
        "essential" to "/ɪˈsen.ʃəl/"
    )

    private val SPECIAL_WORDS = mapOf(
        "discover" to "ডিসকাভার",
        "discuss" to "ডিসকাস",
        "disease" to "ডিজিজ",
        "accept" to "অ্যাকসেপ্ট",
        "access" to "অ্যাকসেস",
        "accurate" to "অ্যাকিউরেট",
        "achieve" to "অ্যাচিভ",
        "activity" to "অ্যাক্টিভিটি",
        "ancient" to "এনশেন্ট",
        "circle" to "সার্কেল",
        "circus" to "সার্কাস",
        "city" to "সিটি",
        "cycle" to "সাইকেল",
        "science" to "সায়েন্স",
        "scientific" to "সায়েন্টিফিক",
        "social" to "সোশাল",
        "special" to "স্পেশাল",
        "official" to "অফিশিয়াল",
        "financial" to "ফাইন্যানশিয়াল",
        "commercial" to "কমার্শিয়াল",
        "racial" to "রেসিয়াল",
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
        "pneumonia" to "নিউমোনিয়া",
        "ensure" to "ইনশিওর",
        "entertain" to "এন্টারটেইন",
        "enthusiasm" to "এনথুজিয়াজম",
        "environment" to "এনভায়রনমেন্ট",
        "essential" to "এসেনশিয়াল",
        "establish" to "এস্টাবলিশ",
        "estimate" to "এস্টিমেট",
        "evaluate" to "ইভ্যালুয়েট",
        "excellent" to "এক্সেলেন্ট",
        "business" to "বিজনেস",
        "comfortable" to "কমফোর্টেবল",
        "vegetable" to "ভেজিটেবল"
    )
}

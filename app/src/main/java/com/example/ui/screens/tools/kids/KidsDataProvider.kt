package com.example.ui.screens.tools.kids

import androidx.compose.ui.graphics.Color

object KidsDataProvider {

    /**
     * Pronunciation text helper according to requirements:
     * - Bengali Swaraborno: "স্বরে অ, স্বরে অ তে অজগর", "স্বরে আ, স্বরে আ তে আম", etc.
     * - Bengali Byanjonborno: "ক, ক তে কলম", "খ, খ তে খরগোশ", etc.
     * - English Alphabet: "A, A for Apple", "B, B for Ball", etc.
     */
    fun getAlphabetSpeechText(item: LetterItem, isEnglish: Boolean, isDetailed: Boolean = false): String {
        if (isEnglish) {
            val rawLetter = item.letter.trim()
            val letterChar = rawLetter.split(" ").firstOrNull()?.uppercase() ?: rawLetter.take(1).uppercase()
            val word = item.wordBn.trim()
            val baseSpeech = "$letterChar, $letterChar for $word"
            return if (isDetailed && item.exampleSentence.isNotEmpty()) {
                "$baseSpeech. ${item.exampleSentence}"
            } else {
                baseSpeech
            }
        } else {
            val letter = item.letter.trim()
            val word = item.wordBn.trim()
            val vowelName = when (letter) {
                "অ" -> "স্বরে অ"
                "আ" -> "স্বরে আ"
                "ই" -> "হ্রস্ব ই"
                "ঈ" -> "দীর্ঘ ঈ"
                "উ" -> "হ্রস্ব উ"
                "ঊ" -> "দীর্ঘ ঊ"
                "ঋ" -> "ঋ"
                "এ" -> "এ"
                "ঐ" -> "ঐ"
                "ও" -> "ও"
                "ঔ" -> "ঔ"
                else -> null
            }

            val spokenWord = if (word.contains("ঐরাবত")) "ওইরা বত" else word
            val baseSpeech = if (letter == "ঐ") {
                "ঐ, ঐ তে ওইরা বত"
            } else if (vowelName != null) {
                "$vowelName, $vowelName তে $spokenWord"
            } else if (item.pronunciation.isNotEmpty() && item.pronunciation != letter && !item.pronunciation.contains("কার")) {
                "${item.pronunciation}, $letter তে $spokenWord"
            } else if (item.pronunciation.contains("কার")) {
                "${item.pronunciation}, $spokenWord"
            } else {
                "$letter, $letter তে $spokenWord"
            }

            return if (isDetailed && item.exampleSentence.isNotEmpty()) {
                val sentence = item.exampleSentence.replace("ঐরাবত", "ওইরা বত")
                "$baseSpeech। $sentence"
            } else {
                baseSpeech
            }
        }
    }

    // -------------------------------------------------------------
    // 1. BANGLA SWAROBORNO (১১টি স্বরবর্ণ)
    // -------------------------------------------------------------
    val banglaVowels = listOf(
        LetterItem("অ", "অ", "অজগর", "Python", "🐍", "অ-তে অজগর আসছে তেরে", Color(0xFFFF6F00)),
        LetterItem("আ", "আ", "আম", "Mango", "🥭", "আ-তে আমটি আমি খাব পেড়ে", Color(0xFFE65100)),
        LetterItem("ই", "হ্রস্ব ই", "ইলিশ", "Hilsa Fish", "🐟", "ই-তে ইলিশ ভাজা খেতে মজা", Color(0xFF0288D1)),
        LetterItem("ঈ", "দীর্ঘ ঈ", "ঈগল", "Eagle", "🦅", "ঈ-তে ঈগল পাখি পাখা মেলে", Color(0xFF388E3C)),
        LetterItem("উ", "হ্রস্ব উ", "উট", "Camel", "🐪", "উ-তে উট চলেছে মরু দেশে", Color(0xFFF57C00)),
        LetterItem("ঊ", "দীর্ঘ ঊ", "ঊষা", "Dawn", "🌅", "ঊ-তে ঊষার আলো পুব আকাশে", Color(0xFFD32F2F)),
        LetterItem("ঋ", "ঋ", "ঋষি", "Sage", "🧘", "ঋ-তে ঋষি মশাই বসেন ধ্যানে", Color(0xFF7B1FA2)),
        LetterItem("এ", "এ", "একতারা", "Ektara", "🪕", "এ-তে একতারাটি বাজে বেশ", Color(0xFF00796B)),
        LetterItem("ঐ", "ঐ", "ঐরাবত", "Elephant", "🐘", "ঐ-তে ঐরাবত ঐ চলছে ধীরে", Color(0xFF5D4037)),
        LetterItem("ও", "ও", "ওলকপি", "Kohlrabi", "🥬", "ও-তে ওলকপি খেলে বাড়ে বল", Color(0xFF689F38)),
        LetterItem("ঔ", "ঔ", "ঔষধ", "Medicine", "💊", "ঔ-তে ঔষধ খেলে অসুখ সারে", Color(0xFFC2185B))
    )

    // -------------------------------------------------------------
    // BANGLA BYANJONBORNO (৩৯টি ব্যঞ্জনবর্ণ)
    // -------------------------------------------------------------
    val banglaConsonants = listOf(
        LetterItem("ক", "ক", "কলম", "Pen", "🖊️", "ক-তে কলম দিয়ে লিখি আমি", Color(0xFF1976D2)),
        LetterItem("খ", "খ", "খরগোশ", "Rabbit", "🐇", "খ-তে খরগোশ ছানা লাফিয়ে চলে", Color(0xFFFF5722)),
        LetterItem("গ", "গ", "গরু", "Cow", "🐄", "গ-তে গরু আমাদের দুধ দেয়", Color(0xFF4CAF50)),
        LetterItem("ঘ", "ঘ", "ঘুড়ি", "Kite", "🪁", "ঘ-তে ঘুড়ি ওড়ে দূর আকাশে", Color(0xFF9C27B0)),
        LetterItem("ঙ", "ঙ", "ব্যাঙ", "Frog", "🐸", "ঙ-তে ব্যাঙ ডাকে গ্যাং গ্যাং", Color(0xFF009688)),
        LetterItem("চ", "চ", "চাঁদ", "Moon", "🌙", "চ-তে চাঁদ মামা দেয় জোছনা", Color(0xFFFFB300)),
        LetterItem("ছ", "ছ", "ছাতা", "Umbrella", "☂️", "ছ-তে ছাতা মাথায় বৃষ্টিতে চলি", Color(0xFFE91E63)),
        LetterItem("জ", "জ", "জাহাজ", "Ship", "🚢", "জ-তে জাহাজ ভাসে নীল সাগরে", Color(0xFF2196F3)),
        LetterItem("ঝ", "ঝ", "ঝুড়ি", "Basket", "🧺", "ঝ-তে ঝুড়ি ভরা ফল আনো", Color(0xFF795548)),
        LetterItem("ঞ", "ঞ", "মিঞা", "Miah", "😺", "ঞ-তে মিঞা ভাই হাসে বেশ", Color(0xFF607D8B)),
        LetterItem("ট", "ট", "টিয়া", "Parrot", "🦜", "ট-তে টিয়া পাখির লাল ঠোঁট", Color(0xFF4CAF50)),
        LetterItem("ঠ", "ঠ", "ঠাকুমা", "Grandma", "👵", "ঠ-তে ঠাকুমা শোনায় মজার গল্প", Color(0xFFFF9800)),
        LetterItem("ড", "ড", "ডালিম", "Pomegranate", "🍎", "ড-তে ডালিম ফলে মিষ্টি রস", Color(0xFFE53935)),
        LetterItem("ঢ", "ঢ", "ঢাক", "Drum", "🥁", "ঢ-তে ঢাক বাজে ঢং ঢং", Color(0xFF8E24AA)),
        LetterItem("ণ", "মূর্ধন্য ণ", "হরিণ", "Deer", "🦌", "ণ-তে হরিণ ছানা বনে ঘোরে", Color(0xFF00897B)),
        LetterItem("ত", "ত", "তরমুজ", "Watermelon", "🍉", "ত-তে তরমুজ খেতে ভারি মজা", Color(0xFF43A047)),
        LetterItem("থ", "থ", "থালা", "Plate", "🍽️", "থ-তে থালা ভরা গরম ভাত", Color(0xFF3949AB)),
        LetterItem("দ", "দ", "দোয়েল", "Magpie Robin", "🐦", "দ-তে দোয়েল পাখি জাতীয় পাখি", Color(0xFF039BE5)),
        LetterItem("ধ", "ধ", "ধান", "Paddy", "🌾", "ধ-তে ধানের শীষে সোনা দোলে", Color(0xFFFDD835)),
        LetterItem("ন", "দন্ত্য ন", "নদী", "River", "🌊", "ন-তে নদী চলে আঁকাবাঁকা", Color(0xFF00ACC1)),
        LetterItem("প", "প", "পাখি", "Bird", "🕊️", "প-তে পাখির গানে ঘুম ভাঙে", Color(0xFF7CB342)),
        LetterItem("ফ", "ফ", "ফুল", "Flower", "🌺", "ফ-তে ফুল ফোটে কত সুন্দর", Color(0xFFE91E63)),
        LetterItem("ব", "ব", "বই", "Book", "📚", "ব-তে বই পড়ে জ্ঞান বাড়ে", Color(0xFF1E88E5)),
        LetterItem("ভ", "ভ", "ভালুক", "Bear", "🐻", "ভ-তে ভালুক নাচে হেলেদুলে", Color(0xFF6D4C41)),
        LetterItem("ম", "ম", "মাছ", "Fish", "🐠", "ম-তে মাছ থাকে গভীর জলে", Color(0xFF00897B)),
        LetterItem("য", "অন্তঃস্থ য", "যাঁতা", "Grindstone", "⚙️", "য-তে যাঁতা ঘোরে আপন মনে", Color(0xFF546E7A)),
        LetterItem("র", "র", "রথ", "Chariot", "🎪", "র-তে রথের মেলায় যাব আজ", Color(0xFFFF5722)),
        LetterItem("ল", "ল", "লাটিম", "Spinning Top", "🪀", "ল-তে লাটিম ঘোরে বনবন", Color(0xFF8E24AA)),
        LetterItem("শ", "তালব্য শ", "শাপলা", "Water Lily", "🪷", "শ-তে শাপলা আমাদের জাতীয় ফুল", Color(0xFFD81B60)),
        LetterItem("ষ", "মূর্ধন্য ষ", "ষাঁড়", "Bull", "🐂", "ষ-তে ষাঁড় ছোটে মাঠের মাঝে", Color(0xFFE53935)),
        LetterItem("স", "দন্ত্য স", "সিংহ", "Lion", "🦁", "স-তে সিংহ বনের রাজা", Color(0xFFFB8C00)),
        LetterItem("হ", "হ", "হাতি", "Elephant", "🐘", "হ-তে হাতির পিঠে মজার চড়া", Color(0xFF3949AB)),
        LetterItem("ড়", "ড-এ বিন্দু ড়", "গাড়ি", "Car", "🚗", "ড়-তে গাড়ি চড়ে যাব বাড়ি", Color(0xFF1E88E5)),
        LetterItem("ঢ়", "ঢ-এ বিন্দু ঢ়", "আষাঢ়", "Asharh Month", "🌧️", "ঢ়-তে আষাঢ় মাসে নামে বৃষ্টি", Color(0xFF00ACC1)),
        LetterItem("য়", "অন্তঃস্থ য়", "পায়রা", "Pigeon", "🕊️", "য়-তে পায়রা বাক বাকুম ডাকে", Color(0xFF7CB342)),
        LetterItem("ৎ", "খণ্ড ত", "মৎস্য", "Fish", "🐟", "ৎ-তে মৎস্য শিকার করি সুখে", Color(0xFF00897B)),
        LetterItem("ং", "অনুস্বার", "রংধনু", "Rainbow", "🌈", "ং-তে রংধনু ওঠে সাত রঙে", Color(0xFF8E24AA)),
        LetterItem("ঃ", "বিসর্গ", "দুঃখী", "Poor / Sad", "🤲", "ঃ-তে দুঃখীদের সাহায্য করো", Color(0xFF546E7A)),
        LetterItem("ঁ", "চন্দ্রবিন্দু", "চাঁদ", "Crescent", "🌙", "ঁ-তে চাঁদ মামা জাগে রাতে", Color(0xFFFFB300))
    )

    // -------------------------------------------------------------
    // BANGLA KAR CHINHO (১০টি কারচিহ্ন)
    // -------------------------------------------------------------
    val banglaKarMarks = listOf(
        LetterItem("া", "আ-কার", "মা", "Mother", "❤️", "ম + া = মা", Color(0xFFE53935)),
        LetterItem("ি", "হ্রস্ব ই-কার", "দিন", "Day", "☀️", "দ + ি + ন = দিন", Color(0xFF1E88E5)),
        LetterItem("ী", "দীর্ঘ ঈ-কার", "নদী", "River", "🌊", "ন + দ + ী = নদী", Color(0xFF00897B)),
        LetterItem("ু", "হ্রস্ব উ-কার", "ফুল", "Flower", "🌸", "ফ + ু + ল = ফুল", Color(0xFFE91E63)),
        LetterItem("ূ", "দীর্ঘ ঊ-কার", "সূর্য", "Sun", "🌞", "স + ূ + র + ্য = সূর্য", Color(0xFFFB8C00)),
        LetterItem("ৃ", "ঋ-কার", "গৃহ", "Home", "🏡", "গ + ৃ + হ = গৃহ", Color(0xFF43A047)),
        LetterItem("ে", "এ-কার", "মেঘ", "Cloud", "☁️", "ম + ে + ঘ = মেঘ", Color(0xFF3949AB)),
        LetterItem("ৈ", "ঐ-কার", "সৈনিক", "Soldier", "💂", "স + ৈ + ন + ি + ক = সৈনিক", Color(0xFF8E24AA)),
        LetterItem("ো", "ও-কার", "গোল", "Round", "⚽", "গ + ো + ল = গোল", Color(0xFFF57C00)),
        LetterItem("ৌ", "ঔ-কার", "নৌকা", "Boat", "⛵", "ন + ৌ + ক + া = নৌকা", Color(0xFF00ACC1))
    )

    // -------------------------------------------------------------
    // ENGLISH ALPHABET (A to Z)
    // -------------------------------------------------------------
    val englishAlphabet = listOf(
        LetterItem("A a", "অ্যা / /æ/", "Apple", "আপেল", "🍎", "A is for Apple, sweet and red", Color(0xFFE53935)),
        LetterItem("B b", "ব / /b/", "Ball", "বল", "⚽", "B is for Ball, bounce it high", Color(0xFF1E88E5)),
        LetterItem("C c", "ক / /k/", "Cat", "বিড়াল", "🐱", "C is for Cat, meow meow", Color(0xFFFF9800)),
        LetterItem("D d", "ড / /d/", "Dog", "কুকুর", "🐶", "D is for Dog, woof woof", Color(0xFF43A047)),
        LetterItem("E e", "এ / /e/", "Elephant", "হাতি", "🐘", "E is for Elephant, big and strong", Color(0xFF3949AB)),
        LetterItem("F f", "ফ / /f/", "Fish", "মাছ", "🐟", "F is for Fish, swimming in water", Color(0xFF00897B)),
        LetterItem("G g", "গ / /g/", "Giraffe", "জিরাফ", "🦒", "G is for Giraffe with tall neck", Color(0xFFF57C00)),
        LetterItem("H h", "হ / /h/", "Hat", "টুপি", "🎩", "H is for Hat on my head", Color(0xFF8E24AA)),
        LetterItem("I i", "ই / /ɪ/", "Ice Cream", "আইসক্রিম", "🍦", "I is for Ice Cream, cold and yummy", Color(0xFFE91E63)),
        LetterItem("J j", "জ / /dʒ/", "Jug", "জগ", "🫖", "J is for Jug full of water", Color(0xFF00ACC1)),
        LetterItem("K k", "ক / /k/", "Kite", "ঘুড়ি", "🪁", "K is for Kite flying in sky", Color(0xFFFF5722)),
        LetterItem("L l", "ল / /l/", "Lion", "সিংহ", "🦁", "L is for Lion, king of jungle", Color(0xFFFB8C00)),
        LetterItem("M m", "ম / /m/", "Mango", "আম", "🥭", "M is for Mango, king of fruits", Color(0xFFFDD835)),
        LetterItem("N n", "ন / /n/", "Nest", "পাখির বাসা", "🪺", "N is for Nest with cute eggs", Color(0xFF6D4C41)),
        LetterItem("O o", "অ / /ɒ/", "Orange", "কমলা", "🍊", "O is for Orange, juicy and round", Color(0xFFFF6F00)),
        LetterItem("P p", "প / /p/", "Parrot", "টিয়া পাখি", "🦜", "P is for Parrot, colorful green", Color(0xFF4CAF50)),
        LetterItem("Q q", "ক্ব / /kw/", "Queen", "রানি", "👑", "Q is for Queen with shiny crown", Color(0xFF9C27B0)),
        LetterItem("R r", "র / /r/", "Rabbit", "খরগোশ", "🐇", "R is for Rabbit hopping around", Color(0xFF00897B)),
        LetterItem("S s", "স / /s/", "Sun", "সূর্য", "☀️", "S is for Sun shining bright", Color(0xFFFBC02D)),
        LetterItem("T t", "ট / /t/", "Tiger", "বাঘ", "🐅", "T is for Tiger with sharp stripes", Color(0xFFFF5722)),
        LetterItem("U u", "আ / /ʌ/", "Umbrella", "ছাতা", "☂️", "U is for Umbrella in the rain", Color(0xFF3949AB)),
        LetterItem("V v", "ভ / /v/", "Van", "ভ্যান গাড়ি", "🚐", "V is for Van driving on road", Color(0xFF009688)),
        LetterItem("W w", "ও / /w/", "Watch", "ঘড়ি", "⌚", "W is for Watch, tick tick tock", Color(0xFF546E7A)),
        LetterItem("X x", "ক্স / /ks/", "Xylophone", "জাইলোফোন", "🎼", "X is for Xylophone with sweet notes", Color(0xFFE91E63)),
        LetterItem("Y y", "ইয় / /j/", "Yo-yo", "ইয়ো-ইয়ো খেলনা", "🪀", "Y is for Yo-yo spinning down and up", Color(0xFF8E24AA)),
        LetterItem("Z z", "জ / /z/", "Zebra", "জেব্রা", "🦓", "Z is for Zebra with black & white stripes", Color(0xFF37474F))
    )

    // -------------------------------------------------------------
    // 2. SPELLING & PHONICS DATA (বানান করে পড়া শেখা)
    // -------------------------------------------------------------
    val spellingWords = listOf(
        // BANGLA 2-LETTER
        SpellingWordItem("বই", "ব - ই", "পুস্তক (Book)", listOf("ব", "ই"), "ব + ই = বই", "📚", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF1E88E5)),
        SpellingWordItem("আম", "আ - ম", "রসালো ফল (Mango)", listOf("আ", "ম"), "আ + ম = আম", "🥭", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFFFF6F00)),
        SpellingWordItem("জল", "জ - ল", "পানি (Water)", listOf("জ", "ল"), "জ + ল = জল", "💧", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF00ACC1)),
        SpellingWordItem("ফল", "ফ - ল", "ফলমূল (Fruit)", listOf("ফ", "ল"), "ফ + ল = ফল", "🍎", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFFE53935)),
        SpellingWordItem("ঘর", "ঘ - র", "বাসা (House)", listOf("ঘ", "র"), "ঘ + র = ঘর", "🏡", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF43A047)),
        SpellingWordItem("রথ", "র - থ", "রথ গাড়ি (Chariot)", listOf("র", "থ"), "র + থ = রথ", "🎪", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF8E24AA)),
        SpellingWordItem("নল", "ন - ল", "নলকূপের নল (Pipe)", listOf("ন", "ল"), "ন + ল = নল", "🚰", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF3949AB)),
        SpellingWordItem("পথ", "প - থ", "রাস্তা (Road)", listOf("প", "থ"), "প + থ = পথ", "🛣️", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF546E7A)),
        SpellingWordItem("বল", "ব - ল", "খেলার বল (Ball)", listOf("ব", "ল"), "ব + ল = বল", "⚽", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF1E88E5)),
        SpellingWordItem("মল", "ম - ল", "মলিনতা / ময়লা", listOf("ম", "ল"), "ম + ল = মল", "🧼", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF795548)),
        SpellingWordItem("ঘট", "ঘ - ট", "মাটির পাত্র (Pot)", listOf("ঘ", "ট"), "ঘ + ট = ঘট", "🏺", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFFD84315)),
        SpellingWordItem("চক", "চ - ক", "লেখার চক (Chalk)", listOf("চ", "ক"), "চ + ক = চক", "🖍️", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF6A1B9A)),
        SpellingWordItem("বন", "ব - ন", "জঙ্গল (Forest)", listOf("ব", "ন"), "ব + ন = বন", "🌲", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF2E7D32)),
        SpellingWordItem("জগ", "জ - গ", "পানির জগ (Jug)", listOf("জ", "গ"), "জ + গ = জগ", "🫖", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF00838F)),
        SpellingWordItem("টব", "ট - ব", "গাছের টব (Flower Tub)", listOf("ট", "ব"), "ট + ব = টব", "🪴", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFF558B2F)),
        SpellingWordItem("দশ", "দ - শ", "সংখ্যা ১০ (Ten)", listOf("দ", "শ"), "দ + শ = দশ", "🔟", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFFC2185B)),
        SpellingWordItem("রস", "র - স", "মিষ্টি রস (Juice)", listOf("র", "স"), "র + স = রস", "🧃", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFFE65100)),
        SpellingWordItem("উল", "উ - ল", "পশমি উল (Wool)", listOf("উ", "ল"), "উ + ল = উল", "🧶", SpellingCategory.BANGLA_TWO_LETTER, Color(0xFFAD1457)),

        // BANGLA KAR-CHINHO WORDS
        SpellingWordItem("মা", "ম - া", "আম্মু / মাতা", listOf("ম", "া"), "ম + া = মা", "❤️", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFFE91E63)),
        SpellingWordItem("বাবা", "ব - া - ব - া", "আব্বু / পিতা", listOf("ব", "া", "ব", "া"), "ব + া = বা, ব + া = বা -> বাবা", "👨‍👧", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF1E88E5)),
        SpellingWordItem("কাক", "ক - া - ক", "কালো পাখি (Crow)", listOf("ক", "া", "ক"), "ক + া = কা, কা + ক = কাক", "🐦‍⬛", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF37474F)),
        SpellingWordItem("গাছ", "গ - া - ছ", "বৃক্ষ (Tree)", listOf("গ", "া", "ছ"), "গ + া = গা, গা + ছ = গাছ", "🌳", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF43A047)),
        SpellingWordItem("মাছ", "ম - া - ছ", "মৎস্য (Fish)", listOf("ম", "া", "ছ"), "ম + া = মা, মা + ছ = মাছ", "🐟", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF00897B)),
        SpellingWordItem("ফুল", "ফ - ু - ল", "পুষ্প (Flower)", listOf("ফ", "ু", "ল"), "ফ + ু = ফু, ফু + ল = ফুল", "🌸", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFFE91E63)),
        SpellingWordItem("দুধ", "দ - ু - ধ", "দুগ্ধ (Milk)", listOf("দ", "ু", "ধ"), "দ + ু = দু, দু + ধ = দুধ", "🥛", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF0288D1)),
        SpellingWordItem("নদী", "ন - দ - ী", "গাঙ / তটিনী (River)", listOf("ন", "দ", "ী"), "ন, দ + ী = দী -> নদী", "🌊", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF00ACC1)),
        SpellingWordItem("চাঁদ", "চ - ঁ - া - দ", "শশী (Moon)", listOf("চ", "ঁ", "া", "দ"), "চ + ঁ + া = চাঁ, চাঁ + দ = চাঁদ", "🌙", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFFFFB300)),
        SpellingWordItem("পাখি", "প - া - খ - ি", "পক্ষী (Bird)", listOf("প", "া", "খ", "ি"), "প + া = পা, খ + ি = খি -> পাখি", "🦜", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF7CB342)),
        SpellingWordItem("খাতা", "খ - া - ত - া", "নোটবুক (Notebook)", listOf("খ", "া", "ত", "া"), "খ + া = খা, ত + া = তা -> খাতা", "📒", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFFFF9800)),
        SpellingWordItem("ছাতা", "ছ - া - ত - া", "ছত্র (Umbrella)", listOf("ছ", "া", "ত", "া"), "ছ + া = ছা, ত + া = তা -> ছাতা", "☂️", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF9C27B0)),
        SpellingWordItem("মামা", "ম - া - ম - া", "মাতুল (Uncle)", listOf("ম", "া", "ম", "া"), "ম + া = মা, ম + া = মা -> মামা", "🧔", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF3F51B5)),
        SpellingWordItem("দাদা", "দ - া - দ - া", "পিতামহ (Grandpa)", listOf("দ", "া", "দ", "া"), "দ + া = দা, দ + া = দা -> দাদা", "👴", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF795548)),
        SpellingWordItem("রাজা", "র - া - জ - া", "নৃপতি (King)", listOf("র", "া", "জ", "া"), "র + া = রা, জ + া = জা -> রাজা", "👑", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFFFFB300)),
        SpellingWordItem("রানি", "র - া - ন - ি", "রাজ্ঞী (Queen)", listOf("র", "া", "ন", "ি"), "র + া = রা, ন + ি = নি -> রানি", "👸", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFFE91E63)),
        SpellingWordItem("জামা", "জ - া - ম - া", "পোশাক (Shirt)", listOf("জ", "া", "ম", "া"), "জ + া = জা, ম + া = মা -> জামা", "👕", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF00BCD4)),
        SpellingWordItem("হাতি", "হ - া - ত - ি", "গজ / ঐরাবত (Elephant)", listOf("হ", "া", "ত", "ি"), "হ + া = হা, ত + ি = তি -> হাতি", "🐘", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF607D8B)),
        SpellingWordItem("গাড়ি", "গ - া - ড় - ি", "যানবাহন (Car)", listOf("গ", "া", "ড়", "ি"), "গ + া = গা, ড় + ি = ড়ি -> গাড়ি", "🚗", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFFE53935)),
        SpellingWordItem("বাঘ", "ব - া - ঘ", "শার্দূল (Tiger)", listOf("ব", "া", "ঘ"), "ব + া = বা, বা + ঘ = বাঘ", "🐅", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFFFF5722)),
        SpellingWordItem("মেঘ", "ম - ে - ঘ", "জলদ (Cloud)", listOf("ম", "ে", "ঘ"), "ম + ে = মে, মে + ঘ = মেঘ", "☁️", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF1976D2)),
        SpellingWordItem("চোখ", "চ - ো - খ", "নয়ন (Eye)", listOf("চ", "ো", "খ"), "চ + ো = চো, চো + খ = চোখ", "👁️", SpellingCategory.BANGLA_KAR_WORDS, Color(0xFF009688)),

        // BANGLA 3-LETTER
        SpellingWordItem("কলম", "ক - ল - ম", "লেখনী (Pen)", listOf("ক", "ল", "ম"), "ক + ল + ম = কলম", "🖊️", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF1E88E5)),
        SpellingWordItem("কদম", "ক - দ - ম", "হলুদ ফুল (Kadamba)", listOf("ক", "দ", "ম"), "ক + দ + ম = কদম", "🌼", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFFFBC02D)),
        SpellingWordItem("সরল", "স - র - ল", "সহজ / সোজা (Simple)", listOf("স", "র", "ল"), "স + র + ল = সরল", "📐", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF43A047)),
        SpellingWordItem("শপথ", "শ - প - থ", "প্রতিজ্ঞা (Oath)", listOf("শ", "প", "থ"), "শ + প + থ = শপথ", "🤝", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF8E24AA)),
        SpellingWordItem("বাতাস", "ব - া - ত - া - স", "বায়ু / হাওয়া (Wind)", listOf("ব", "া", "ত", "া", "স"), "ব + া = বা, ত + া = তা + স = বাতাস", "🌬️", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF00ACC1)),
        SpellingWordItem("আকাশ", "আ - ক - া - শ", "গগন (Sky)", listOf("আ", "ক", "া", "শ"), "আ, ক + া = কা + শ = আকাশ", "☁️", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF0288D1)),
        SpellingWordItem("বাগান", "ব - া - গ - া - ন", "কুঞ্জ (Garden)", listOf("ব", "া", "গ", "া", "ন"), "ব + া = বা, গ + া = গা + ন = বাগান", "🏡", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF388E3C)),
        SpellingWordItem("নরম", "ন - র - ম", "কোমল (Soft)", listOf("ন", "র", "ম"), "ন + র + ম = নরম", "🧸", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF8D6E63)),
        SpellingWordItem("গরম", "গ - র - ম", "উষ্ণ (Hot)", listOf("গ", "র", "ম"), "গ + র + ম = গরম", "🔥", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFFE64A19)),
        SpellingWordItem("চরণ", "চ - র - ণ", "পা / পদযুগল (Feet)", listOf("চ", "র", "ণ"), "চ + র + ণ = চরণ", "🦶", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF5C6BC0)),
        SpellingWordItem("নয়ন", "ন - য় - ন", "চোখ / আঁখি (Eyes)", listOf("ন", "য়", "ন"), "ন + য় + ন = নয়ন", "👀", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF26A69A)),
        SpellingWordItem("শহর", "শ - হ - র", "নগর (City)", listOf("শ", "হ", "র"), "শ + হ + র = শহর", "🏙️", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF546E7A)),
        SpellingWordItem("ফসল", "ফ - স - ল", "শস্য (Crops)", listOf("ফ", "স", "ল"), "ফ + স + ল = ফসল", "🌾", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFFF9A825)),
        SpellingWordItem("ডালিম", "ড - া - ল - ি - ম", "বেদানা ফল (Pomegranate)", listOf("ড", "া", "ল", "ি", "ম"), "ড + া = ডা, ল + ি = লি + ম = ডালিম", "🍎", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFFC2185B)),
        SpellingWordItem("বিড়াল", "ব - ি - ড় - া - ল", "মার্জার (Cat)", listOf("ব", "ি", "ড়", "া", "ল"), "ব + ি = বি, ড় + া = ড়া + ল = বিড়াল", "🐱", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFFFF9800)),
        SpellingWordItem("হরিণ", "হ - র - ি - ণ", "মৃগ (Deer)", listOf("হ", "র", "ি", "ণ"), "হ, র + ি = রি + ণ = হরিণ", "🦌", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF689F38)),
        SpellingWordItem("গোলাপ", "গ - ো - ল - া - প", "সুন্দর ফুল (Rose)", listOf("গ", "ো", "ল", "া", "প"), "গ + ো = গো, ল + া = লা + প = গোলাপ", "🌹", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFFD81B60)),
        SpellingWordItem("পুতুল", "প - ু - ত - ু - ল", "খেলনা (Doll)", listOf("প", "ু", "ত", "ু", "ল"), "প + ু = পু, ত + ু = তু + ল = পুতুল", "🪆", SpellingCategory.BANGLA_THREE_LETTER, Color(0xFF7E57C2)),

        // BANGLA ADVANCED (৪-৫ বর্ণ)
        SpellingWordItem("প্রজাপতি", "প - ্র - জ - া - প - ত - ি", "রঙিন পতঙ্গ (Butterfly)", listOf("প", "্র", "জ", "া", "প", "ত", "ি"), "প + ্র = প্র, জ + া = জা, প, ত + ি = তি -> প্রজাপতি", "🦋", SpellingCategory.BANGLA_ADVANCED, Color(0xFF8E24AA)),
        SpellingWordItem("রংধনু", "র - ং - ধ - ন - ু", "সাত রঙের রামধনু (Rainbow)", listOf("র", "ং", "ধ", "ন", "ু"), "র + ং = রং, ধ, ন + ু = নু -> রংধনু", "🌈", SpellingCategory.BANGLA_ADVANCED, Color(0xFF00ACC1)),
        SpellingWordItem("বিদ্যালয়", "ব - ি - দ - ্য - া - ল - য়", "পাঠশালা / স্কুল (School)", listOf("ব", "ি", "দ", "্য", "া", "ল", "য়"), "ব + ি = বি, দ + ্য + া = দ্যা, ল, য় -> বিদ্যালয়", "🏫", SpellingCategory.BANGLA_ADVANCED, Color(0xFF1E88E5)),
        SpellingWordItem("বাংলাদেশ", "ব - া - ং - ল - া - দ - ে - শ", "আমাদের মাতৃভূমি", listOf("ব", "া", "ং", "ল", "া", "দ", "ে", "শ"), "বাং-লা-দে-শ -> বাংলাদেশ", "🇧🇩", SpellingCategory.BANGLA_ADVANCED, Color(0xFF2E7D32)),
        SpellingWordItem("তরমুজ", "ত - র - ম - ু - জ", "গ্রীষ্মকালীন রসালো ফল", listOf("ত", "র", "ম", "ু", "জ"), "ত + র = তর, ম + ু = মু + জ = তরমুজ", "🍉", SpellingCategory.BANGLA_ADVANCED, Color(0xFFE53935)),
        SpellingWordItem("সাইকেল", "স - া - ই - ক - ে - ল", "দ্বিচক্র যান (Bicycle)", listOf("স", "া", "ই", "ক", "ে", "ল"), "স + া = সা + ই = সাই, ক + ে = কে + ল = সাইকেল", "🚲", SpellingCategory.BANGLA_ADVANCED, Color(0xFFFF9800)),
        SpellingWordItem("কম্পিউটার", "ক - ম - প - ি - উ - ট - া - র", "গণকযন্ত্র (Computer)", listOf("ক", "ম", "প", "ি", "উ", "ট", "া", "র"), "কম-পি-উ-টার -> কম্পিউটার", "💻", SpellingCategory.BANGLA_ADVANCED, Color(0xFF3949AB)),
        SpellingWordItem("সূর্যমুখী", "স - ূ - র - ্য - ম - ু - খ - ী", "হলুদ সূর্যমুখী ফুল", listOf("স", "ূ", "র", "্য", "ম", "ু", "খ", "ী"), "সূর্য-মুখী -> সূর্যমুখী", "🌻", SpellingCategory.BANGLA_ADVANCED, Color(0xFFFBC02D)),

        // ENGLISH 3-LETTER CVC
        SpellingWordItem("CAT", "C - A - T", "বিড়াল (Meow)", listOf("C", "A", "T"), "C (/k/) + A (/æ/) + T (/t/) = CAT", "🐱", SpellingCategory.ENGLISH_CVC, Color(0xFFFF9800)),
        SpellingWordItem("DOG", "D - O - G", "কুকুর (Woof)", listOf("D", "O", "G"), "D (/d/) + O (/ɒ/) + G (/g/) = DOG", "🐶", SpellingCategory.ENGLISH_CVC, Color(0xFF4CAF50)),
        SpellingWordItem("SUN", "S - U - N", "সূর্য (Sun)", listOf("S", "U", "N"), "S (/s/) + U (/ʌ/) + N (/n/) = SUN", "☀️", SpellingCategory.ENGLISH_CVC, Color(0xFFFBC02D)),
        SpellingWordItem("BAT", "B - A - T", "খেলার ব্যাট / বাদুড়", listOf("B", "A", "T"), "B (/b/) + A (/æ/) + T (/t/) = BAT", "🦇", SpellingCategory.ENGLISH_CVC, Color(0xFF5D4037)),
        SpellingWordItem("PEN", "P - E - N", "কলম (Pen)", listOf("P", "E", "N"), "P (/p/) + E (/e/) + N (/n/) = PEN", "🖊️", SpellingCategory.ENGLISH_CVC, Color(0xFF1E88E5)),
        SpellingWordItem("CUP", "C - U - P", "চায়ের কাপ", listOf("C", "U", "P"), "C (/k/) + U (/ʌ/) + P (/p/) = CUP", "☕", SpellingCategory.ENGLISH_CVC, Color(0xFF8E24AA)),
        SpellingWordItem("FAN", "F - A - N", "পাখা (Fan)", listOf("F", "A", "N"), "F (/f/) + A (/æ/) + N (/n/) = FAN", "🪭", SpellingCategory.ENGLISH_CVC, Color(0xFF00ACC1)),
        SpellingWordItem("BUS", "B - U - S", "বাস গাড়ি (Bus)", listOf("B", "U", "S"), "B (/b/) + U (/ʌ/) + S (/s/) = BUS", "🚌", SpellingCategory.ENGLISH_CVC, Color(0xFFFF5722)),
        SpellingWordItem("HAT", "H - A - T", "মাথার টুপি (Hat)", listOf("H", "A", "T"), "H (/h/) + A (/æ/) + T (/t/) = HAT", "🎩", SpellingCategory.ENGLISH_CVC, Color(0xFF3949AB)),
        SpellingWordItem("FOX", "F - O - X", "শেয়াল (Clever Fox)", listOf("F", "O", "X"), "F (/f/) + O (/ɒ/) + X (/ks/) = FOX", "🦊", SpellingCategory.ENGLISH_CVC, Color(0xFFE65100)),
        SpellingWordItem("PIG", "P - I - G", "শূকর ছানা (Piglet)", listOf("P", "I", "G"), "P (/p/) + I (/ɪ/) + G (/g/) = PIG", "🐷", SpellingCategory.ENGLISH_CVC, Color(0xFFE91E63)),
        SpellingWordItem("BED", "B - E - D", "ঘুমানোর বিছানা", listOf("B", "E", "D"), "B (/b/) + E (/e/) + D (/d/) = BED", "🛏️", SpellingCategory.ENGLISH_CVC, Color(0xFF5C6BC0)),
        SpellingWordItem("BOX", "B - O - X", "বাক্স (Carton Box)", listOf("B", "O", "X"), "B (/b/) + O (/ɒ/) + X (/ks/) = BOX", "📦", SpellingCategory.ENGLISH_CVC, Color(0xFF8D6E63)),
        SpellingWordItem("VAN", "V - A - N", "ভ্যান গাড়ি", listOf("V", "A", "N"), "V (/v/) + A (/æ/) + N (/n/) = VAN", "🚐", SpellingCategory.ENGLISH_CVC, Color(0xFF00897B)),
        SpellingWordItem("HEN", "H - E - N", "মুরগি (Lays Eggs)", listOf("H", "E", "N"), "H (/h/) + E (/e/) + N (/n/) = HEN", "🐔", SpellingCategory.ENGLISH_CVC, Color(0xFFD81B60)),
        SpellingWordItem("TOP", "T - O - P", "লাটিম খেলনা", listOf("T", "O", "P"), "T (/t/) + O (/ɒ/) + P (/p/) = TOP", "🪀", SpellingCategory.ENGLISH_CVC, Color(0xFF0288D1)),
        SpellingWordItem("NET", "N - E - T", "মাছ ধরার জাল", listOf("N", "E", "T"), "N (/n/) + E (/e/) + T (/t/) = NET", "🕸️", SpellingCategory.ENGLISH_CVC, Color(0xFF00796B)),
        SpellingWordItem("PIN", "P - I - N", "ছোট আলপিন", listOf("P", "I", "N"), "P (/p/) + I (/ɪ/) + N (/n/) = PIN", "📌", SpellingCategory.ENGLISH_CVC, Color(0xFFC2185B)),
        SpellingWordItem("MAP", "M - A - P", "মানচিত্র (Map)", listOf("M", "A", "P"), "M (/m/) + A (/æ/) + P (/p/) = MAP", "🗺️", SpellingCategory.ENGLISH_CVC, Color(0xFF43A047)),
        SpellingWordItem("BAG", "B - A - G", "স্কুল ব্যাগ / থলে", listOf("B", "A", "G"), "B (/b/) + A (/æ/) + G (/g/) = BAG", "🎒", SpellingCategory.ENGLISH_CVC, Color(0xFF1976D2)),
        SpellingWordItem("LEG", "L - E - G", "শরীরের পা", listOf("L", "E", "G"), "L (/l/) + E (/e/) + G (/g/) = LEG", "🦵", SpellingCategory.ENGLISH_CVC, Color(0xFF8D6E63)),
        SpellingWordItem("LIP", "L - I - P", "মুখের ঠোঁট", listOf("L", "I", "P"), "L (/l/) + I (/ɪ/) + P (/p/) = LIP", "👄", SpellingCategory.ENGLISH_CVC, Color(0xFFE91E63)),
        SpellingWordItem("SUN", "S - U - N", "উজ্জ্বল সূর্য", listOf("S", "U", "N"), "S (/s/) + U (/ʌ/) + N (/n/) = SUN", "☀️", SpellingCategory.ENGLISH_CVC, Color(0xFFFFB300)),
        SpellingWordItem("MOP", "M - O - P", "মেঝের মোছা", listOf("M", "O", "P"), "M (/m/) + O (/ɒ/) + P (/p/) = MOP", "🧹", SpellingCategory.ENGLISH_CVC, Color(0xFF00ACC1)),

        // ENGLISH WORDS (4-5 LETTERS & DIGRAPHS)
        SpellingWordItem("BOOK", "B - O - O - K", "বই (Reading)", listOf("B", "O", "O", "K"), "B - O - O - K = BOOK", "📚", SpellingCategory.ENGLISH_WORDS, Color(0xFF1E88E5)),
        SpellingWordItem("TREE", "T - R - E - E", "গাছ (Green Tree)", listOf("T", "R", "E", "E"), "T - R - E - E = TREE", "🌳", SpellingCategory.ENGLISH_WORDS, Color(0xFF43A047)),
        SpellingWordItem("FISH", "F - I - S - H", "মাছ (Swims)", listOf("F", "I", "S", "H"), "F - I - S - H = FISH", "🐟", SpellingCategory.ENGLISH_WORDS, Color(0xFF00897B)),
        SpellingWordItem("MILK", "M - I - L - K", "দুধ (Healthy Drink)", listOf("M", "I", "L", "K"), "M - I - L - K = MILK", "🥛", SpellingCategory.ENGLISH_WORDS, Color(0xFF0288D1)),
        SpellingWordItem("BIRD", "B - I - R - D", "পাখি (Flies)", listOf("B", "I", "R", "D"), "B - I - R - D = BIRD", "🐦", SpellingCategory.ENGLISH_WORDS, Color(0xFF7CB342)),
        SpellingWordItem("STAR", "S - T - A - R", "তারা / নক্ষত্র", listOf("S", "T", "A", "R"), "S - T - A - R = STAR", "⭐", SpellingCategory.ENGLISH_WORDS, Color(0xFFFFB300)),
        SpellingWordItem("BALL", "B - A - L - L", "খেলার বল", listOf("B", "A", "L", "L"), "B - A - L - L = BALL", "⚽", SpellingCategory.ENGLISH_WORDS, Color(0xFFE53935)),
        SpellingWordItem("ROSE", "R - O - S - E", "গোলাপ ফুল", listOf("R", "O", "S", "E"), "R - O - S - E = ROSE", "🌹", SpellingCategory.ENGLISH_WORDS, Color(0xFFE91E63)),
        SpellingWordItem("SHIP", "S - H - I - P", "বড় জাহাজ (Sea Ship)", listOf("S", "H", "I", "P"), "SH (/ʃ/) + I + P = SHIP", "🚢", SpellingCategory.ENGLISH_WORDS, Color(0xFF1565C0)),
        SpellingWordItem("DUCK", "D - U - C - K", "পাতিহাঁস (Quack)", listOf("D", "U", "C", "K"), "D + U + CK (/k/) = DUCK", "🦆", SpellingCategory.ENGLISH_WORDS, Color(0xFF2E7D32)),
        SpellingWordItem("FROG", "F - R - O - G", "ব্যাঙ (Croaks)", listOf("F", "R", "O", "G"), "FR + O + G = FROG", "🐸", SpellingCategory.ENGLISH_WORDS, Color(0xFF388E3C)),
        SpellingWordItem("MOON", "M - O - O - N", "চাঁদ মামা", listOf("M", "O", "O", "N"), "M + OO (/uː/) + N = MOON", "🌙", SpellingCategory.ENGLISH_WORDS, Color(0xFFFFB300)),
        SpellingWordItem("LION", "L - I - O - N", "সিংহ (King)", listOf("L", "I", "O", "N"), "L - I - O - N = LION", "🦁", SpellingCategory.ENGLISH_WORDS, Color(0xFFFF9800)),
        SpellingWordItem("KING", "K - I - N - G", "রাজা (Crown)", listOf("K", "I", "N", "G"), "K + I + NG (/ŋ/) = KING", "👑", SpellingCategory.ENGLISH_WORDS, Color(0xFFFBC02D)),
        SpellingWordItem("RING", "R - I - N - G", "হাতের আংটি", listOf("R", "I", "N", "G"), "R + I + NG (/ŋ/) = RING", "💍", SpellingCategory.ENGLISH_WORDS, Color(0xFF00BCD4)),
        SpellingWordItem("CAKE", "C - A - K - E", "মিষ্টি কেক", listOf("C", "A", "K", "E"), "C + A + K + Silent E = CAKE", "🎂", SpellingCategory.ENGLISH_WORDS, Color(0xFFEC407A)),
        SpellingWordItem("KITE", "K - I - T - E", "উড়ন্ত ঘুড়ি", listOf("K", "I", "T", "E"), "K + I + T + Silent E = KITE", "🪁", SpellingCategory.ENGLISH_WORDS, Color(0xFF9C27B0)),
        SpellingWordItem("NOSE", "N - O - S - E", "নাক (Smell)", listOf("N", "O", "S", "E"), "N + O + S + Silent E = NOSE", "👃", SpellingCategory.ENGLISH_WORDS, Color(0xFFFF7043)),
        SpellingWordItem("BELL", "B - E - L - L", "স্কুল ঘণ্টা (Ding Dong)", listOf("B", "E", "L", "L"), "B - E - L - L = BELL", "🔔", SpellingCategory.ENGLISH_WORDS, Color(0xFFFFB300)),
        SpellingWordItem("BABY", "B - A - B - Y", "ছোট শিশু (Infant)", listOf("B", "A", "B", "Y"), "B - A - B - Y = BABY", "👶", SpellingCategory.ENGLISH_WORDS, Color(0xFF26C6DA)),
        SpellingWordItem("BOAT", "B - O - A - T", "নৌকা (Rowing)", listOf("B", "O", "A", "T"), "B + OA (/oʊ/) + T = BOAT", "⛵", SpellingCategory.ENGLISH_WORDS, Color(0xFF0288D1)),
        SpellingWordItem("DOLL", "D - O - L - L", "খেলার পুতুল", listOf("D", "O", "L", "L"), "D - O - L - L = DOLL", "🪆", SpellingCategory.ENGLISH_WORDS, Color(0xFFAB47BC)),
        SpellingWordItem("DEER", "D - E - E - R", "মায়াবী হরিণ", listOf("D", "E", "E", "R"), "D + EE (/iː/) + R = DEER", "🦌", SpellingCategory.ENGLISH_WORDS, Color(0xFF8D6E63)),
        SpellingWordItem("APPLE", "A - P - P - L - E", "মিষ্টি আপেল", listOf("A", "P", "P", "L", "E"), "A - P - P - L - E = APPLE", "🍎", SpellingCategory.ENGLISH_WORDS, Color(0xFFE53935))
    )

    // -------------------------------------------------------------
    // ENGLISH PHONEMIC CHART & PRONUNCIATION RULES DATA
    // -------------------------------------------------------------
    val phonicsLetterSounds = listOf(
        PhonicsLetterSound(
            letter = "A a",
            soundBn = "অ্যা / আ / এই",
            ipa = "/æ/ or /eɪ/",
            ruleExplanation = "CVC শব্দে শর্ট সাউন্ড 'অ্যা' (যেমন Cat), কিন্তু শব্দের শেষে Silent 'e' থাকলে লং সাউন্ড 'এই' হয় (যেমন Cake, Name)।",
            exampleWords = listOf(
                PhonicsWordExample("Apple", "অ্যাপল", "আপেল", "🍎"),
                PhonicsWordExample("Ant", "অ্যান্ট", "পিঁপড়া", "🐜"),
                PhonicsWordExample("Cake", "কেইক", "কেক", "🎂"),
                PhonicsWordExample("Baby", "বেইবি", "শিশু", "👶")
            ),
            accentColor = Color(0xFFE53935)
        ),
        PhonicsLetterSound(
            letter = "B b",
            soundBn = "ব",
            ipa = "/b/",
            ruleExplanation = "বাংলা 'ব' অক্ষরের মতো উচ্চারিত হয়। দুই ঠোঁট একসাথে চেপে ছেড়ে দিয়ে 'ব' উচ্চারণ করতে হয়।",
            exampleWords = listOf(
                PhonicsWordExample("Ball", "বল", "খেলার বল", "⚽"),
                PhonicsWordExample("Bat", "ব্যাট", "ব্যাট / বাদুড়", "🦇"),
                PhonicsWordExample("Book", "বুক", "বই", "📚"),
                PhonicsWordExample("Bus", "বাস", "বাস গাড়ি", "🚌")
            ),
            accentColor = Color(0xFF1E88E5)
        ),
        PhonicsLetterSound(
            letter = "C c",
            soundBn = "ক (Hard) / স (Soft)",
            ipa = "/k/ or /s/",
            ruleExplanation = "C-এর পরে e, i, y থাকলে 'স' উচ্চারিত হয় (City, Ice, Circle)। বাকি সব ক্ষেত্রে 'ক' উচ্চারিত হয় (Cat, Cup, Car)।",
            exampleWords = listOf(
                PhonicsWordExample("Cat", "ক্যাট", "বিড়াল (Hard C)", "🐱"),
                PhonicsWordExample("Cup", "কাপ", "চায়ের পেয়ালা", "☕"),
                PhonicsWordExample("City", "সিটি", "শহর (Soft C)", "🏙️"),
                PhonicsWordExample("Ice", "আইস", "বরফ (Soft C)", "🧊")
            ),
            accentColor = Color(0xFFFF9800)
        ),
        PhonicsLetterSound(
            letter = "D d",
            soundBn = "ড",
            ipa = "/d/",
            ruleExplanation = "বাংলা 'ড' ধ্বনির মতো স্পষ্ট উচ্চারিত হয়। জিহ্বার ডগা ওপরের মাড়িতে স্পর্শ করে।",
            exampleWords = listOf(
                PhonicsWordExample("Dog", "ডগ", "কুকুর", "🐶"),
                PhonicsWordExample("Duck", "ডাক", "পাতিহাঁস", "🦆"),
                PhonicsWordExample("Doll", "ডল", "পুতুল", "🪆"),
                PhonicsWordExample("Door", "ডোর", "দরজা", "🚪")
            ),
            accentColor = Color(0xFF43A047)
        ),
        PhonicsLetterSound(
            letter = "E e",
            soundBn = "এ / ঈ",
            ipa = "/e/ or /iː/",
            ruleExplanation = "শর্ট সাউন্ড 'এ' (যেমন Bed, Pen, Egg), লং সাউন্ড বা ডাবল 'ee' থাকলে 'ঈ' (যেমন Tree, Bee, Eagle)।",
            exampleWords = listOf(
                PhonicsWordExample("Egg", "এগ", "ডিম", "🥚"),
                PhonicsWordExample("Elephant", "এলিফ্যান্ট", "হাতি", "🐘"),
                PhonicsWordExample("Tree", "ট্রী", "গাছ (Long E)", "🌳"),
                PhonicsWordExample("Eagle", "ঈগল", "ঈগল পাখি", "🦅")
            ),
            accentColor = Color(0xFF3949AB)
        ),
        PhonicsLetterSound(
            letter = "F f",
            soundBn = "ফ",
            ipa = "/f/",
            ruleExplanation = "ওপরের দাঁত নিচের ঠোঁটের ওপর হালকা ছুঁয়ে বাতাস ছেড়ে 'ফ' উচ্চারণ করতে হয়।",
            exampleWords = listOf(
                PhonicsWordExample("Fish", "ফিশ", "মাছ", "🐟"),
                PhonicsWordExample("Fan", "ফ্যান", "পাখা", "🪭"),
                PhonicsWordExample("Frog", "ফ্রগ", "ব্যাঙ", "🐸"),
                PhonicsWordExample("Fox", "ফক্স", "শেয়াল", "🦊")
            ),
            accentColor = Color(0xFF00897B)
        ),
        PhonicsLetterSound(
            letter = "G g",
            soundBn = "গ (Hard) / জ (Soft)",
            ipa = "/ɡ/ or /dʒ/",
            ruleExplanation = "G-এর পরে e, i, y থাকলে অধিকাংশ ক্ষেত্রে 'জ' হয় (Giraffe, Gem)। অন্যথায় 'গ' হয় (Gun, Girl, Goat)।",
            exampleWords = listOf(
                PhonicsWordExample("Goat", "গোট", "ছাগল (Hard G)", "🐐"),
                PhonicsWordExample("Gun", "গান", "বন্দুক", "🔫"),
                PhonicsWordExample("Giraffe", "জিরাফ", "জিরাফ (Soft G)", "🦒"),
                PhonicsWordExample("Gem", "জেম", "রত্নপাথর", "💎")
            ),
            accentColor = Color(0xFFF57C00)
        ),
        PhonicsLetterSound(
            letter = "H h",
            soundBn = "হ",
            ipa = "/h/",
            ruleExplanation = "গলা থেকে নিঃশ্বাস ছেড়ে বাংলা 'হ' এর মতো মৃদু উচ্চারিত হয়। যেমন Hat, Hen।",
            exampleWords = listOf(
                PhonicsWordExample("Hat", "হ্যাট", "টুপি", "🎩"),
                PhonicsWordExample("Hen", "হেন", "মুরগি", "🐔"),
                PhonicsWordExample("Horse", "হর্স", "ঘোড়া", "🐴"),
                PhonicsWordExample("Hand", "হ্যান্ড", "হাত", "✋")
            ),
            accentColor = Color(0xFF8E24AA)
        ),
        PhonicsLetterSound(
            letter = "I i",
            soundBn = "ই / আই",
            ipa = "/ɪ/ or /aɪ/",
            ruleExplanation = "শর্ট সাউন্ড 'ই' (Sit, Pin, Igloo)। শব্দের শেষে Silent 'e' থাকলে লং সাউন্ড 'আই' হয় (Kite, Ice, Fire)।",
            exampleWords = listOf(
                PhonicsWordExample("Ink", "ইঙ্ক", "কালি (Short I)", "🖋️"),
                PhonicsWordExample("Igloo", "ইগলু", "বরফের ঘর", "🛖"),
                PhonicsWordExample("Kite", "কাইট", "ঘুড়ি (Long I)", "🪁"),
                PhonicsWordExample("Ice", "আইস", "বরফ (Long I)", "🧊")
            ),
            accentColor = Color(0xFFE91E63)
        ),
        PhonicsLetterSound(
            letter = "J j",
            soundBn = "জ",
            ipa = "/dʒ/",
            ruleExplanation = "বাংলা 'জ' অক্ষরের মতো স্পষ্ট উচ্চারিত হয়। যেমন Jam, Jug, Jump।",
            exampleWords = listOf(
                PhonicsWordExample("Jug", "জগ", "পানির জগ", "🫖"),
                PhonicsWordExample("Jam", "জ্যাম", "মিষ্টি জ্যাম", "🍓"),
                PhonicsWordExample("Jelly", "জেলি", "খাবার জেলি", "🍮"),
                PhonicsWordExample("Jeep", "জিপ", "জিপ গাড়ি", "🚙")
            ),
            accentColor = Color(0xFF00ACC1)
        ),
        PhonicsLetterSound(
            letter = "K k",
            soundBn = "ক",
            ipa = "/k/",
            ruleExplanation = "বাংলা 'ক' অক্ষরের মতো শক্ত উচ্চারণ। কিন্তু N-এর আগে বসলে K অনুচ্চারিত (Silent) থাকে (যেমন Knife, Knee)।",
            exampleWords = listOf(
                PhonicsWordExample("Kite", "কাইট", "ঘুড়ি", "🪁"),
                PhonicsWordExample("King", "কিং", "রাজা", "👑"),
                PhonicsWordExample("Kangaroo", "ক্যাঙ্গারু", "ক্যাঙ্গারু", "🦘"),
                PhonicsWordExample("Key", "কী", "চাবি", "🔑")
            ),
            accentColor = Color(0xFFFF5722)
        ),
        PhonicsLetterSound(
            letter = "L l",
            soundBn = "ল",
            ipa = "/l/",
            ruleExplanation = "বাংলা 'ল' অক্ষরের মতো। জিহ্বা ওপরের দাঁতের পেছনের মাড়িতে ছুঁয়ে থাকে।",
            exampleWords = listOf(
                PhonicsWordExample("Lion", "লায়ন", "সিংহ", "🦁"),
                PhonicsWordExample("Leaf", "লিফ", "গাছের পাতা", "🍃"),
                PhonicsWordExample("Lamp", "ল্যাম্প", "বাতি", "💡"),
                PhonicsWordExample("Lemon", "লেমন", "লেবু", "🍋")
            ),
            accentColor = Color(0xFFFB8C00)
        ),
        PhonicsLetterSound(
            letter = "M m",
            soundBn = "ম",
            ipa = "/m/",
            ruleExplanation = "দুই ঠোঁট বন্ধ রেখে নাক দিয়ে গুনগুন শব্দ বের করে 'ম' উচ্চারিত হয়। যেমন Moon, Mango।",
            exampleWords = listOf(
                PhonicsWordExample("Mango", "ম্যাঙ্গো", "রসালো আম", "🥭"),
                PhonicsWordExample("Moon", "মুন", "চাঁদ", "🌙"),
                PhonicsWordExample("Monkey", "মাঙ্কি", "বানর", "🐒"),
                PhonicsWordExample("Milk", "মিল্ক", "দুধ", "🥛")
            ),
            accentColor = Color(0xFFFDD835)
        ),
        PhonicsLetterSound(
            letter = "N n",
            soundBn = "ন",
            ipa = "/n/",
            ruleExplanation = "বাংলা 'ন' ধ্বনির মতো। জিহ্বা ওপরের তালুতে ঠেকিয়ে নাসিক্য ধ্বনি দিতে হয়।",
            exampleWords = listOf(
                PhonicsWordExample("Nest", "নেস্ট", "পাখির বাসা", "🪺"),
                PhonicsWordExample("Net", "নেট", "জাল", "🕸️"),
                PhonicsWordExample("Nose", "নোজ", "নাক", "👃"),
                PhonicsWordExample("Nut", "নাট", "বাদাম", "🥜")
            ),
            accentColor = Color(0xFF6D4C41)
        ),
        PhonicsLetterSound(
            letter = "O o",
            soundBn = "অ / ও",
            ipa = "/ɒ/ or /oʊ/",
            ruleExplanation = "শর্ট সাউন্ড মুখ গোল করে 'অ' (Dog, Ox, Box)। লং সাউন্ড বা শেষে 'e' থাকলে 'ও' (Rose, Hope, Boat)।",
            exampleWords = listOf(
                PhonicsWordExample("Orange", "অরেঞ্জ", "কমলা লেবু", "🍊"),
                PhonicsWordExample("Ox", "অক্স", "ষাঁড়", "🐂"),
                PhonicsWordExample("Rose", "রোজ", "গোলাপ (Long O)", "🌹"),
                PhonicsWordExample("Boat", "বোট", "নৌকা (Long O)", "⛵")
            ),
            accentColor = Color(0xFFFF6F00)
        ),
        PhonicsLetterSound(
            letter = "P p",
            soundBn = "প",
            ipa = "/p/",
            ruleExplanation = "দুই ঠোঁট চেপে এক দমকে বাতাস ছেড়ে হালকা পপিং শব্দে 'প' উচ্চারিত হয়।",
            exampleWords = listOf(
                PhonicsWordExample("Pen", "পেন", "কলম", "🖊️"),
                PhonicsWordExample("Parrot", "প্যারট", "টিয়া পাখি", "🦜"),
                PhonicsWordExample("Pig", "পিগ", "শূকর", "🐷"),
                PhonicsWordExample("Pan", "প্যান", "রান্নার কড়াই", "🍳")
            ),
            accentColor = Color(0xFF4CAF50)
        ),
        PhonicsLetterSound(
            letter = "Q q (Qu)",
            soundBn = "কোয়া / ক্ব",
            ipa = "/kw/",
            ruleExplanation = "ইংরেজিতে Q প্রায় সবসময় u-এর সাথে বসে (Qu) এবং 'কোয়া' বা /kw/ ধ্বনি তৈরি করে।",
            exampleWords = listOf(
                PhonicsWordExample("Queen", "কুইন", "রানি", "👑"),
                PhonicsWordExample("Quick", "কুইক", "দ্রুত", "⚡"),
                PhonicsWordExample("Quiet", "কোয়ায়েট", "শান্ত / নীরব", "🤫"),
                PhonicsWordExample("Quilt", "কুইল্ট", "লেপ / কাঁথা", "🛏️")
            ),
            accentColor = Color(0xFF9C27B0)
        ),
        PhonicsLetterSound(
            letter = "R r",
            soundBn = "র",
            ipa = "/r/",
            ruleExplanation = "জিহ্বা পেছনের দিকে বাঁকা করে তালু স্পর্শ না করিয়ে 'র' ধ্বনি বের করতে হয়।",
            exampleWords = listOf(
                PhonicsWordExample("Rabbit", "র‍্যাবিট", "খরগোশ", "🐇"),
                PhonicsWordExample("Ring", "রিং", "আংটি", "💍"),
                PhonicsWordExample("Rain", "রেইন", "বৃষ্টি", "🌧️"),
                PhonicsWordExample("Rocket", "রকেট", "মহাকাশযান", "🚀")
            ),
            accentColor = Color(0xFF00897B)
        ),
        PhonicsLetterSound(
            letter = "S s",
            soundBn = "স / জ",
            ipa = "/s/ or /z/",
            ruleExplanation = "দাঁত বন্ধ করে হিসহিস শব্দে 'স' (Sun, Star)। দুটি ভাওয়েলের মাঝে বসলে কখনো 'জ' হয় (Rose, Is)।",
            exampleWords = listOf(
                PhonicsWordExample("Sun", "সান", "সূর্য", "☀️"),
                PhonicsWordExample("Star", "স্টার", "তারা", "⭐"),
                PhonicsWordExample("Snake", "স্নেক", "সাপ", "🐍"),
                PhonicsWordExample("Spoon", "স্পুন", "চামচ", "🥄")
            ),
            accentColor = Color(0xFFFBC02D)
        ),
        PhonicsLetterSound(
            letter = "T t",
            soundBn = "ট",
            ipa = "/t/",
            ruleExplanation = "বাংলা 'ট' ধ্বনির মতো। জিহ্বার ডগা ওপরের দাঁতের গোড়ায় ঠেকিয়ে বাতাস ছেড়ে উচ্চারণ করতে হয়।",
            exampleWords = listOf(
                PhonicsWordExample("Tiger", "টাইগার", "বাঘ", "🐅"),
                PhonicsWordExample("Tree", "ট্রী", "গাছ", "🌳"),
                PhonicsWordExample("Top", "টপ", "লাটিম", "🪀"),
                PhonicsWordExample("Train", "ট্রেন", "রেলগাড়ি", "🚂")
            ),
            accentColor = Color(0xFFFF5722)
        ),
        PhonicsLetterSound(
            letter = "U u",
            soundBn = "আ / ইউ",
            ipa = "/ʌ/ or /juː/",
            ruleExplanation = "শর্ট সাউন্ড গলা থেকে 'আ' বা 'উ' (Sun, Cup, Up)। লং সাউন্ড নিজের নাম 'ইউ' (Uniform, Tube, Cute)।",
            exampleWords = listOf(
                PhonicsWordExample("Umbrella", "আমব্রেলা", "ছাতা (Short U)", "☂️"),
                PhonicsWordExample("Up", "আপ", "উপরে", "⬆️"),
                PhonicsWordExample("Uniform", "ইউনিফর্ম", "পোশাক (Long U)", "👔"),
                PhonicsWordExample("Tube", "টিউব", "নল (Magic E)", "🧪")
            ),
            accentColor = Color(0xFF3949AB)
        ),
        PhonicsLetterSound(
            letter = "V v",
            soundBn = "ভ",
            ipa = "/v/",
            ruleExplanation = "ওপরের দাঁত নিচের ঠোঁটে হালকা ছুঁয়ে ঠোঁটে মৃদু কম্পন (ভিব্রেশন) সৃষ্টি করে 'ভ' উচ্চারিত হয়।",
            exampleWords = listOf(
                PhonicsWordExample("Van", "ভ্যান", "ভ্যান গাড়ি", "🚐"),
                PhonicsWordExample("Vase", "ভেস", "ফুলদানি", "🏺"),
                PhonicsWordExample("Violin", "ভায়োলিন", "বেহালা", "🎻"),
                PhonicsWordExample("Vegetable", "ভেজিটেবল", "শাকসবজি", "🥦")
            ),
            accentColor = Color(0xFF009688)
        ),
        PhonicsLetterSound(
            letter = "W w",
            soundBn = "ওয় / ও",
            ipa = "/w/",
            ruleExplanation = "ঠোঁট গোল করে শিস দেওয়ার মতো ভঙ্গি করে 'ওয়' ধ্বনি বের করতে হয়। যেমন Watch, Water।",
            exampleWords = listOf(
                PhonicsWordExample("Watch", "ওয়াচ", "হাতঘড়ি", "⌚"),
                PhonicsWordExample("Water", "ওয়াটার", "পানি", "💧"),
                PhonicsWordExample("Wind", "উইন্ড", "বাতাস", "🌬️"),
                PhonicsWordExample("Wall", "ওয়াল", "দেওয়াল", "🧱")
            ),
            accentColor = Color(0xFF546E7A)
        ),
        PhonicsLetterSound(
            letter = "X x",
            soundBn = "ক্স",
            ipa = "/ks/",
            ruleExplanation = "সাধারণত 'ক' ও 'স' এর যুক্তধ্বনি 'ক্স' (/ks/) তৈরি করে (Box, Fox, Six)।",
            exampleWords = listOf(
                PhonicsWordExample("Box", "বক্স", "বাক্স", "📦"),
                PhonicsWordExample("Fox", "ফক্স", "শেয়াল", "🦊"),
                PhonicsWordExample("Six", "সিক্স", "সংখ্যা ৬", "6️⃣"),
                PhonicsWordExample("Xylophone", "জাইলোফোন", "বাদ্যযন্ত্র", "🎼")
            ),
            accentColor = Color(0xFFE91E63)
        ),
        PhonicsLetterSound(
            letter = "Y y",
            soundBn = "ইয় / আই",
            ipa = "/j/ or /aɪ/",
            ruleExplanation = "শব্দের শুরুতে কনসোনেন্ট হিসেবে 'ইয়' (Yak, Yellow)। শব্দের শেষে সেমি-ভাওয়েল হিসেবে 'আই' বা 'ই' (Cry, Sky, Happy)।",
            exampleWords = listOf(
                PhonicsWordExample("Yak", "ইয়াক", "চমরী গাই", "🐂"),
                PhonicsWordExample("Yellow", "ইয়েলো", "হলুদ রং", "🟡"),
                PhonicsWordExample("Cry", "ক্রাই", "কান্না করা", "😢"),
                PhonicsWordExample("Baby", "বেইবি", "শিশু", "👶")
            ),
            accentColor = Color(0xFF8E24AA)
        ),
        PhonicsLetterSound(
            letter = "Z z",
            soundBn = "জ / য",
            ipa = "/z/",
            ruleExplanation = "দাঁত বন্ধ রেখে মৌমাছির গুনগুনের মতো তীব্র কম্পন তুলে 'য/জ' উচ্চারণ করতে হয়। যেমন Zoo, Zebra।",
            exampleWords = listOf(
                PhonicsWordExample("Zebra", "জেব্রা", "জেব্রা", "🦓"),
                PhonicsWordExample("Zoo", "জু", "চিড়িয়াখানা", "🦁"),
                PhonicsWordExample("Zip", "জিপ", "চেইন", "🤐"),
                PhonicsWordExample("Zero", "জিরো", "শূন্য", "0️⃣")
            ),
            accentColor = Color(0xFF37474F)
        )
    )

    // VOWEL SHORT VS LONG RULES (ম্যাজিক 'E' সহ)
    val vowelRules = listOf(
        PhonicsRuleItem(
            ruleTitle = "Short 'A' vs Long 'A' (Magic E)",
            formula = "C + A + P (ক্যাপ) ➡️ C + A + P + E (কেইপ)",
            explanationBn = "শব্দের শেষে অনুচ্চারিত 'E' যুক্ত হলে আগের 'A' ভাওয়েলটি 'অ্যা' থেকে দীর্ঘ 'এই' ধ্বনিতে পরিণত হয়।",
            examples = listOf(
                PhonicsWordExample("Cap", "ক্যাপ", "টুপি (Short A)", "🧢"),
                PhonicsWordExample("Cape", "কেইপ", "আলখেল্লা (Long A)", "🦸"),
                PhonicsWordExample("Tap", "ট্যাপ", "পানির কল", "🚰"),
                PhonicsWordExample("Tape", "টেইপ", "আঠার ফিতা", "📼"),
                PhonicsWordExample("Hat", "হ্যাট", "টুপি", "🎩"),
                PhonicsWordExample("Hate", "হেইট", "ঘৃণা করা", "😠")
            ),
            accentColor = Color(0xFFE53935)
        ),
        PhonicsRuleItem(
            ruleTitle = "Short 'I' vs Long 'I' (Magic E)",
            formula = "K + I + T (কিট) ➡️ K + I + T + E (কাইট)",
            explanationBn = "সাধারণত 'I' এর উচ্চারণ 'ই' (Short I)। কিন্তু শেষে 'E' বসলে 'I' এর উচ্চারণ পূর্ণ 'আই' (Long I) হয়ে যায়।",
            examples = listOf(
                PhonicsWordExample("Kit", "কিট", "সরঞ্জাম বক্স", "🧰"),
                PhonicsWordExample("Kite", "কাইট", "ঘুড়ি (Magic E)", "🪁"),
                PhonicsWordExample("Pin", "পিন", "আলপিন", "📌"),
                PhonicsWordExample("Pine", "পাইন", "পাইন গাছ", "🌲"),
                PhonicsWordExample("Bite", "বাইট", "কামড় দেওয়া", "🦷"),
                PhonicsWordExample("Ride", "রাইড", "চড়া / চালানো", "🚲")
            ),
            accentColor = Color(0xFF1E88E5)
        ),
        PhonicsRuleItem(
            ruleTitle = "Short 'O' vs Long 'O' (Magic E)",
            formula = "H + O + P (হপ) ➡️ H + O + P + E (হোপ)",
            explanationBn = "'O' শর্ট অবস্থায় গোল মুখ করে 'অ' (Hop, Not, Rob)। কিন্তু Magic E থাকলে স্পষ্ট 'ও' (Hope, Note, Robe)।",
            examples = listOf(
                PhonicsWordExample("Hop", "হপ", "এক পায়ে লাফ", "🦘"),
                PhonicsWordExample("Hope", "হোপ", "আশা করা", "🌟"),
                PhonicsWordExample("Not", "নট", "না বোধক", "❌"),
                PhonicsWordExample("Note", "নোট", "লেখা নোট", "📝"),
                PhonicsWordExample("Rob", "রব", "ডাকাতি", "🦹"),
                PhonicsWordExample("Robe", "রোব", "পোশাক", "🥋")
            ),
            accentColor = Color(0xFFFF9800)
        ),
        PhonicsRuleItem(
            ruleTitle = "Short 'U' vs Long 'U' (Magic E)",
            formula = "T + U + B (টাব) ➡️ T + U + B + E (টিউব)",
            explanationBn = "'U' সাধারণ CVC শব্দে 'আ' (Tub, Cut, Hug)। শেষে E যুক্ত হলে দীর্ঘ 'ইউ' উচ্চারিত হয় (Tube, Cute, Huge)।",
            examples = listOf(
                PhonicsWordExample("Tub", "টাব", "গোসলের গামলা", "🛁"),
                PhonicsWordExample("Tube", "টিউব", "নল (Magic E)", "🧪"),
                PhonicsWordExample("Cut", "কাট", "কাটা", "✂️"),
                PhonicsWordExample("Cute", "কিউট", "মিষ্টি / কিউট", "🥰"),
                PhonicsWordExample("Hug", "হাগ", "জড়িয়ে ধরা", "🤗"),
                PhonicsWordExample("Huge", "হিউজ", "বিশাল বড়", "🐘")
            ),
            accentColor = Color(0xFF43A047)
        )
    )

    // CONSONANT DIGRAPHS (দুটি বর্ণ মিলে এক ধ্বনি)
    val digraphRules = listOf(
        PhonicsRuleItem(
            ruleTitle = "SH Digraph = /ʃ/ ('শ' ধ্বনি)",
            formula = "S + H = 'শ' (যেমন: Ship, Fish)",
            explanationBn = "S এবং H একসাথে বসলে সবসময় বাংলা তালব্য 'শ' বা চুপ করানোর 'শহ্' ধ্বনি সৃষ্টি করে।",
            examples = listOf(
                PhonicsWordExample("Ship", "শিপ", "বড় জাহাজ", "🚢"),
                PhonicsWordExample("Shop", "শপ", "দোকান", "🏪"),
                PhonicsWordExample("Fish", "ফিশ", "মাছ", "🐟"),
                PhonicsWordExample("Dish", "ডিশ", "খাবারের থালা", "🍽️"),
                PhonicsWordExample("Shirt", "শার্ট", "জামা", "👔"),
                PhonicsWordExample("Shoes", "শুজ", "জুতো জোড়া", "👟")
            ),
            accentColor = Color(0xFF1565C0)
        ),
        PhonicsRuleItem(
            ruleTitle = "CH Digraph = /tʃ/ ('চ' ধ্বনি)",
            formula = "C + H = 'চ' (যেমন: Chair, Rich)",
            explanationBn = "C এবং H একসাথে মিললে বাংলা 'চ' ধ্বনি উচ্চারণ করে (যেমন ট্রেন ছাড়ে 'চুক চুক')।",
            examples = listOf(
                PhonicsWordExample("Chair", "চেয়ার", "বসার কেদারা", "🪑"),
                PhonicsWordExample("Chin", "চিন", "মুখের চিবুক", "🧔"),
                PhonicsWordExample("Rich", "রিচ", "ধনী ব্যক্তি", "💰"),
                PhonicsWordExample("Much", "মাচ", "অনেক বেশি", "📈"),
                PhonicsWordExample("Cheese", "চিজ", "পনির", "🧀"),
                PhonicsWordExample("Child", "চাইল্ড", "ছোট শিশু", "🧒")
            ),
            accentColor = Color(0xFFE91E63)
        ),
        PhonicsRuleItem(
            ruleTitle = "TH (Soft / Unvoiced) = /θ/ ('থ' ধ্বনি)",
            formula = "T + H = 'থ' (যেমন: Think, Three)",
            explanationBn = "জিহ্বার ডগা দুই দাঁতের ফাঁকে মৃদু স্পর্শ করিয়ে বাতাস বের করে বাংলা 'থ' এর মতো নরম ধ্বনি বের করতে হয়।",
            examples = listOf(
                PhonicsWordExample("Think", "থিঙ্ক", "চিন্তা করা", "💭"),
                PhonicsWordExample("Three", "থ্রী", "সংখ্যা ৩", "3️⃣"),
                PhonicsWordExample("Bath", "বাথ", "গোসল", "🛁"),
                PhonicsWordExample("Teeth", "টিথ", "দাঁতগুলো", "🦷"),
                PhonicsWordExample("Thumb", "থাম", "বৃদ্ধাঙ্গুল", "👍")
            ),
            accentColor = Color(0xFF00897B)
        ),
        PhonicsRuleItem(
            ruleTitle = "TH (Hard / Voiced) = /ð/ ('দ' ধ্বনি)",
            formula = "T + H = 'দ' (যেমন: This, That)",
            explanationBn = "জিহ্বা দাঁতে চেপে গলায় মৃদু কম্পন তুলে বাংলা 'দ' অক্ষরের মতো শক্ত উচ্চারণ করতে হয়।",
            examples = listOf(
                PhonicsWordExample("This", "দিস", "এই জিনিসটি", "👉"),
                PhonicsWordExample("That", "দ্যাট", "ঐ জিনিসটি", "👈"),
                PhonicsWordExample("Mother", "মাদার", "মা", "👩"),
                PhonicsWordExample("Father", "ফাদার", "বাবা", "👨"),
                PhonicsWordExample("They", "দে", "তারা সকলে", "👥")
            ),
            accentColor = Color(0xFF6D4C41)
        ),
        PhonicsRuleItem(
            ruleTitle = "PH Digraph = /f/ ('ফ' ধ্বনি)",
            formula = "P + H = 'ফ' (যেমন: Phone, Dolphin)",
            explanationBn = "P এবং H পাশাপাশি বসলে সম্পূর্ণ 'ফ' বা /f/ এর মতো উচ্চারিত হয়, কখনোই 'প-হ' নয়।",
            examples = listOf(
                PhonicsWordExample("Phone", "ফোন", "টেলিফোন", "📱"),
                PhonicsWordExample("Photo", "ফটো", "ছবি", "📷"),
                PhonicsWordExample("Dolphin", "ডলফিন", "ডলফিন মাছ", "🐬"),
                PhonicsWordExample("Elephant", "এলিফ্যান্ট", "বড় হাতি", "🐘")
            ),
            accentColor = Color(0xFF3949AB)
        ),
        PhonicsRuleItem(
            ruleTitle = "WH Digraph = /w/ ('ওয়' ধ্বনি)",
            formula = "W + H = 'ওয়' (যেমন: What, White)",
            explanationBn = "প্রশ্নবোধক শব্দের শুরুতে WH বসলে মৃদু বাতাস সহ 'ওয়' ধ্বনি দেয় (হোয়াট / ওয়াট)।",
            examples = listOf(
                PhonicsWordExample("What", "হোয়াট", "কী?", "❓"),
                PhonicsWordExample("When", "হোয়েন", "কখন?", "⏰"),
                PhonicsWordExample("Where", "হোয়ার", "কোথায়?", "📍"),
                PhonicsWordExample("White", "হোয়াইট", "সাদা রং", "⚪"),
                PhonicsWordExample("Wheel", "হুইল", "গাড়ির চাকা", "🛞")
            ),
            accentColor = Color(0xFF00ACC1)
        ),
        PhonicsRuleItem(
            ruleTitle = "CK Digraph = /k/ ('ক' ধ্বনি)",
            formula = "C + K = 'ক' (যেমন: Duck, Clock)",
            explanationBn = "শর্ট ভাওয়েল শব্দের শেষে শক্ত 'ক' ধ্বনি দিতে C এবং K একসাথে মিলে /k/ ধ্বনি সৃষ্টি করে।",
            examples = listOf(
                PhonicsWordExample("Duck", "ডাক", "পাতিহাঁস", "🦆"),
                PhonicsWordExample("Lock", "লক", "তালা", "🔒"),
                PhonicsWordExample("Clock", "ক্লক", "দেওয়াল ঘড়ি", "🕒"),
                PhonicsWordExample("Rock", "রক", "শক্ত পাথর", "🪨")
            ),
            accentColor = Color(0xFF43A047)
        ),
        PhonicsRuleItem(
            ruleTitle = "NG Digraph = /ŋ/ ('ঙ / ং' ধ্বনি)",
            formula = "N + G = 'ঙ / ং' (যেমন: King, Ring)",
            explanationBn = "শব্দের শেষে N এবং G মিলে বাংলা অনুস্বার 'ং' বা 'ঙ' এর মতো মিষ্টি অনুনাসিক ধ্বনি তৈরি করে।",
            examples = listOf(
                PhonicsWordExample("King", "কিং", "মহারাজা", "👑"),
                PhonicsWordExample("Ring", "রিং", "আংটি", "💍"),
                PhonicsWordExample("Sing", "সিং", "গান গাওয়া", "🎤"),
                PhonicsWordExample("Song", "সং", "সুন্দর গান", "🎵"),
                PhonicsWordExample("Long", "লং", "লম্বা", "📏")
            ),
            accentColor = Color(0xFF8E24AA)
        )
    )

    // CONSONANT BLENDS (যুক্তধ্বনি)
    val consonantBlendRules = listOf(
        PhonicsRuleItem(
            ruleTitle = "L-Blends (BL, CL, FL, PL, SL)",
            formula = "Consonant + L (যেমন: Blue, Clap, Play)",
            explanationBn = "L-যুক্ত ধ্বনিতে প্রথম বর্ণের পর দ্রুত 'ল' মিশে যায় এবং দুটি বর্ণই স্পষ্ট শোনা যায়।",
            examples = listOf(
                PhonicsWordExample("Blue", "ব্লু", "নীল রং", "🔵"),
                PhonicsWordExample("Clap", "ক্ল্যাপ", "হাততালি", "👏"),
                PhonicsWordExample("Flag", "ফ্ল্যাগ", "জাতীয় পতাকা", "🚩"),
                PhonicsWordExample("Play", "প্লে", "খেলাধুলা", "⚽"),
                PhonicsWordExample("Slow", "স্লো", "ধীরগতি", "🐢")
            ),
            accentColor = Color(0xFF1E88E5)
        ),
        PhonicsRuleItem(
            ruleTitle = "R-Blends (BR, CR, DR, FR, TR)",
            formula = "Consonant + R (যেমন: Frog, Tree, Brown)",
            explanationBn = "R-যুক্ত ধ্বনিতে বর্ণের সাথে 'র-ফলা' যুক্ত হয়ে জোরালো ধ্বনি সৃষ্টি হয়।",
            examples = listOf(
                PhonicsWordExample("Frog", "ফ্রগ", "সবুজ ব্যাঙ", "🐸"),
                PhonicsWordExample("Tree", "ট্রী", "সবুজ গাছ", "🌳"),
                PhonicsWordExample("Brown", "ব্রাউন", "বাদামি রং", "🟤"),
                PhonicsWordExample("Crab", "ক্র্যাব", "নদীর কাঁকড়া", "🦀"),
                PhonicsWordExample("Drum", "ড্রাম", "বাজানোর ঢাক", "🥁")
            ),
            accentColor = Color(0xFF43A047)
        ),
        PhonicsRuleItem(
            ruleTitle = "S-Blends (ST, SP, SM, SN, SW)",
            formula = "S + Consonant (যেমন: Star, Spoon, Smile)",
            explanationBn = "S দিয়ে শুরু হয়ে পরের ব্যঞ্জনের সাথে যুক্ত হয়ে শিসযুক্ত যুক্তধ্বনি তৈরি করে।",
            examples = listOf(
                PhonicsWordExample("Star", "স্টার", "ঝলমলে তারা", "⭐"),
                PhonicsWordExample("Spoon", "স্পুন", "খাবার চামচ", "🥄"),
                PhonicsWordExample("Smile", "স্মাইল", "মিষ্টি হাসি", "😊"),
                PhonicsWordExample("Snow", "স্নো", "তুষারপাত", "❄️"),
                PhonicsWordExample("Swim", "সুইম", "সাঁতার কাটা", "🏊")
            ),
            accentColor = Color(0xFFFFB300)
        )
    )

    // SILENT LETTERS RULES (অনুচ্চারিত বর্ণ ও বানানের নিয়ম)
    val silentLetterRules = listOf(
        PhonicsRuleItem(
            ruleTitle = "Silent 'K' Rule (K অনুচ্চারিত)",
            formula = "K + N ➡️ K চুপ থাকে (Knife = নাইফ)",
            explanationBn = "শব্দের শুরুতে যদি K-এর ঠিক পরপরই N থাকে, তবে K কখনোই উচ্চারিত হয় না, শুধু N থেকে উচ্চারণ শুরু হয়।",
            examples = listOf(
                PhonicsWordExample("Knife", "নাইফ", "ধারালো ছুরি (ক-নাইফ নয়)", "🔪"),
                PhonicsWordExample("Knee", "নী", "হাঁটু (ক-নী নয়)", "🦵"),
                PhonicsWordExample("Knot", "নট", "দড়ির গিঁট (ক-নট নয়)", "🪢"),
                PhonicsWordExample("Know", "নো", "জানা / চেনা (ক-নো নয়)", "🧠"),
                PhonicsWordExample("Knight", "নাইট", "বীর যোদ্ধা", "🛡️")
            ),
            accentColor = Color(0xFFD32F2F)
        ),
        PhonicsRuleItem(
            ruleTitle = "Silent 'W' Rule (W অনুচ্চারিত)",
            formula = "W + R ➡️ W চুপ থাকে (Write = রাইট)",
            explanationBn = "শব্দের শুরুতে W-এর পর R থাকলে W অনুচ্চারিত থাকে, উচ্চারণ শুরু হয় R থেকে।",
            examples = listOf(
                PhonicsWordExample("Write", "রাইট", "খাতায় লেখা", "✍️"),
                PhonicsWordExample("Wrong", "রং", "ভুল উত্তর", "❌"),
                PhonicsWordExample("Wrist", "রিস্ট", "হাতের কব্জি", "⌚"),
                PhonicsWordExample("Wrap", "র‍্যাপ", "উপহার মোড়ানো", "🎁")
            ),
            accentColor = Color(0xFFE65100)
        ),
        PhonicsRuleItem(
            ruleTitle = "Silent 'B' Rule (B অনুচ্চারিত)",
            formula = "M + B (শেষে) ➡️ B চুপ থাকে (Comb = কোম)",
            explanationBn = "শব্দের শেষে M-এর পরে B থাকলে B উচ্চারিত হয় না। যেমন চিরুনি 'কম্ব' নয়, 'কোম'।",
            examples = listOf(
                PhonicsWordExample("Comb", "কোম", "মাথা আঁচড়ানোর চিরুনি", "🪮"),
                PhonicsWordExample("Lamb", "ল্যাম", "ভেড়ার ছানা (ল্যাম্ব নয়)", "🐑"),
                PhonicsWordExample("Thumb", "থাম", "বৃদ্ধাঙ্গুল (থাম্ব নয়)", "👍"),
                PhonicsWordExample("Climb", "ক্লাইম", "গাছে ওঠা (ক্লাইম্ব নয়)", "🧗")
            ),
            accentColor = Color(0xFF7B1FA2)
        ),
        PhonicsRuleItem(
            ruleTitle = "Silent 'L' Rule (L অনুচ্চারিত)",
            formula = "L + K / M / F ➡️ L চুপ থাকে (Walk = ওয়াক)",
            explanationBn = "A বা O-এর পর L এবং তারপর K, M, F থাকলে L অনুচ্চারিত থেকে যায়। যেমন Walk (ওয়াক), Talk (টক)।",
            examples = listOf(
                PhonicsWordExample("Walk", "ওয়াক", "হেঁটে চলা (ওয়াক্)", "🚶"),
                PhonicsWordExample("Talk", "টক", "কথা বলা (টল্ক নয়)", "🗣️"),
                PhonicsWordExample("Half", "হাফ", "অর্ধেক (হাল্ফ নয়)", "🌗"),
                PhonicsWordExample("Calm", "কাম", "শান্ত থাকা (কাল্ম নয়)", "🧘"),
                PhonicsWordExample("Could", "কুড", "পারতো (কোল্ড নয়)", "💭")
            ),
            accentColor = Color(0xFF00796B)
        ),
        PhonicsRuleItem(
            ruleTitle = "Silent 'G' & 'GH' Rule (G/GH অনুচ্চারিত)",
            formula = "G + N বা I + GH ➡️ অনুচ্চারিত (Sign = সাইন, Light = লাইট)",
            explanationBn = "N-এর আগে G থাকলে G অনুচ্চারিত থাকে (Sign)। শব্দের ভেতরে 'igh'-এ GH অনুচ্চারিত থেকে শুধু 'আই' উচ্চারিত হয়।",
            examples = listOf(
                PhonicsWordExample("Sign", "সাইন", "চিহ্ন বা সই", "✍️"),
                PhonicsWordExample("Light", "লাইট", "আলো", "💡"),
                PhonicsWordExample("Night", "নাইট", "অন্ধকার রাত", "🌃"),
                PhonicsWordExample("High", "হাই", "উঁচু পাহাড়", "⛰️"),
                PhonicsWordExample("Fight", "ফাইট", "যুদ্ধ করা", "🥊")
            ),
            accentColor = Color(0xFF37474F)
        )
    )

    // WORD FAMILIES & BLENDING LESSONS (বর্ণ জোড়া লেগে শব্দ তৈরির নিয়ম)
    val wordFamilyRules = listOf(
        PhonicsRuleItem(
            ruleTitle = "-AT Family (অ্যাট পরিবার)",
            formula = "Consonant + AT = ক্যাট, ব্যাট, র্যাট...",
            explanationBn = "যে কোনো অক্ষরের সাথে 'at' (অ্যাট) জোড়া লাগালে সহজে শব্দ তৈরি হয়: C + at = Cat, B + at = Bat, H + at = Hat, M + at = Mat, R + at = Rat।",
            examples = listOf(
                PhonicsWordExample("Cat", "ক্যাট", "বিড়াল (C + at)", "🐱"),
                PhonicsWordExample("Bat", "ব্যাট", "খেলার ব্যাট (B + at)", "🦇"),
                PhonicsWordExample("Hat", "হ্যাট", "টুপি (H + at)", "🎩"),
                PhonicsWordExample("Rat", "র‍্যাট", "ইঁদুর (R + at)", "🐀"),
                PhonicsWordExample("Mat", "ম্যাট", "মাদুর (M + at)", "🧶")
            ),
            accentColor = Color(0xFFE53935)
        ),
        PhonicsRuleItem(
            ruleTitle = "-AN Family (অ্যান পরিবার)",
            formula = "Consonant + AN = ফ্যান, প্যান, ভ্যান...",
            explanationBn = "অক্ষরের সাথে 'an' (অ্যান) জুড়ে দিলে: F + an = Fan, P + an = Pan, V + an = Van, M + an = Man, C + an = Can।",
            examples = listOf(
                PhonicsWordExample("Fan", "ফ্যান", "পাখা (F + an)", "🪭"),
                PhonicsWordExample("Pan", "প্যান", "কড়াই (P + an)", "🍳"),
                PhonicsWordExample("Van", "ভ্যান", "ভ্যান গাড়ি (V + an)", "🚐"),
                PhonicsWordExample("Man", "ম্যান", "পুরুষ মানুষ (M + an)", "👨"),
                PhonicsWordExample("Can", "ক্যান", "টিনের কৌটো (C + an)", "🥫")
            ),
            accentColor = Color(0xFF1E88E5)
        ),
        PhonicsRuleItem(
            ruleTitle = "-EN & -ET Families (এন ও এট পরিবার)",
            formula = "Consonant + EN / ET = পেন, হেন, নেট, পেট...",
            explanationBn = "E-এর শর্ট সাউন্ড 'এ'। P + en = Pen, H + en = Hen, T + en = Ten, N + et = Net, P + et = Pet, W + et = Wet।",
            examples = listOf(
                PhonicsWordExample("Pen", "পেন", "কলম (P + en)", "🖊️"),
                PhonicsWordExample("Hen", "হেন", "মুরগি (H + en)", "🐔"),
                PhonicsWordExample("Ten", "টেন", "সংখ্যা ১০ (T + en)", "🔟"),
                PhonicsWordExample("Net", "নেট", "জাল (N + et)", "🕸️"),
                PhonicsWordExample("Pet", "পেট", "পোষা প্রাণী (P + et)", "🐶")
            ),
            accentColor = Color(0xFF3949AB)
        ),
        PhonicsRuleItem(
            ruleTitle = "-IN & -IG Families (ইন ও ইগ পরিবার)",
            formula = "Consonant + IN / IG = পিন, উইন, পিগ, বিগ...",
            explanationBn = "I-এর শর্ট সাউন্ড 'ই'। P + in = Pin, W + in = Win, B + in = Bin, P + ig = Pig, B + ig = Big, D + ig = Dig।",
            examples = listOf(
                PhonicsWordExample("Pin", "পিন", "আলপিন (P + in)", "📌"),
                PhonicsWordExample("Win", "উইন", "জয়লাভ করা (W + in)", "🏆"),
                PhonicsWordExample("Bin", "বিন", "ময়লার ঝুড়ি (B + in)", "🗑️"),
                PhonicsWordExample("Pig", "পিগ", "শূকর ছানা (P + ig)", "🐷"),
                PhonicsWordExample("Big", "বিগ", "বিশাল বড় (B + ig)", "🐘")
            ),
            accentColor = Color(0xFF00897B)
        ),
        PhonicsRuleItem(
            ruleTitle = "-OG & -OP Families (অগ ও অপ পরিবার)",
            formula = "Consonant + OG / OP = ডগ, লগ, টপ, মপ...",
            explanationBn = "O-এর শর্ট সাউন্ড 'অ'। D + og = Dog, L + og = Log, F + og = Fog, T + op = Top, M + op = Mop, H + op = Hop।",
            examples = listOf(
                PhonicsWordExample("Dog", "ডগ", "কুকুর (D + og)", "🐶"),
                PhonicsWordExample("Log", "লগ", "গাছের গুঁড়ি (L + og)", "🪵"),
                PhonicsWordExample("Top", "টপ", "লাটিম খেলনা (T + op)", "🪀"),
                PhonicsWordExample("Mop", "মপ", "মেঝের মোছা (M + op)", "🧹"),
                PhonicsWordExample("Hop", "হপ", "লাফানো (H + op)", "🦘")
            ),
            accentColor = Color(0xFFFF9800)
        ),
        PhonicsRuleItem(
            ruleTitle = "-UG & -UN Families (আগ ও আন পরিবার)",
            formula = "Consonant + UG / UN = সান, রান, বাগ, মাগ...",
            explanationBn = "U-এর শর্ট সাউন্ড 'আ'। S + un = Sun, R + un = Run, B + un = Bun, B + ug = Bug, M + ug = Mug, H + ug = Hug।",
            examples = listOf(
                PhonicsWordExample("Sun", "সান", "সূর্য (S + un)", "☀️"),
                PhonicsWordExample("Run", "রান", "দৌড়ানো (R + un)", "🏃"),
                PhonicsWordExample("Bug", "বাগ", "ছোট পোকা (B + ug)", "🐞"),
                PhonicsWordExample("Mug", "মাগ", "পানির মগ (M + ug)", "🍺"),
                PhonicsWordExample("Hug", "হাগ", "জড়িয়ে ধরা (H + ug)", "🤗")
            ),
            accentColor = Color(0xFF4CAF50)
        ),
        PhonicsRuleItem(
            ruleTitle = "Double Vowels (EE, OO, EA, AI, OA)",
            formula = "দুটি Vowel মিলে দীর্ঘ সুর (Tree, Moon, Boat, Rain)",
            explanationBn = "যখন দুটি Vowel পাশাপাশি আসে, তখন প্রথম Vowel-এর নাম দীর্ঘ হয়ে উচ্চারিত হয়: EE/EA = 'ঈ' (Tree, Leaf), OO = 'উ/ঊ' (Moon, Book), OA = 'ও' (Boat, Goat), AI/AY = 'এই' (Rain, Day)।",
            examples = listOf(
                PhonicsWordExample("Tree", "ট্রী", "গাছ (EE = ঈ)", "🌳"),
                PhonicsWordExample("Moon", "মুন", "চাঁদ (OO = ঊ)", "🌙"),
                PhonicsWordExample("Boat", "বোট", "নৌকা (OA = ও)", "⛵"),
                PhonicsWordExample("Rain", "রেইন", "বৃষ্টি (AI = এই)", "🌧️"),
                PhonicsWordExample("Leaf", "লিফ", "পাতা (EA = ঈ)", "🍃")
            ),
            accentColor = Color(0xFF8E24AA)
        ),
        PhonicsRuleItem(
            ruleTitle = "-ALL & -AR Rules (অল ও আর পরিবার)",
            formula = "A + LL = 'অল' (Ball), A + R = 'আর' (Car)",
            explanationBn = "A-এর পর LL থাকলে মুখ গোল করে 'অল' উচ্চারিত হয় (Ball, Wall, Call)। A-এর পর R থাকলে খোলা গলায় 'আর' উচ্চারিত হয় (Car, Star, Park)।",
            examples = listOf(
                PhonicsWordExample("Ball", "বল", "খেলার বল (B + all)", "⚽"),
                PhonicsWordExample("Wall", "ওয়াল", "দেওয়াল (W + all)", "🧱"),
                PhonicsWordExample("Car", "কার", "গাড়ি (C + ar)", "🚗"),
                PhonicsWordExample("Star", "স্টার", "আকাশের তারা (St + ar)", "⭐"),
                PhonicsWordExample("Park", "পার্ক", "খেলার পার্ক (P + ar + k)", "🏞️")
            ),
            accentColor = Color(0xFF00ACC1)
        )
    )

    // -------------------------------------------------------------
    // 3. NUMBERS 1 TO 20 (সংখ্যা ও গণনা)
    // -------------------------------------------------------------
    val numberItems = listOf(
        NumberItem("১", "1", "এক", "One", "🍎", 1, Color(0xFFE53935)),
        NumberItem("২", "2", "দুই", "Two", "🎈", 2, Color(0xFF1E88E5)),
        NumberItem("৩", "3", "তিন", "Three", "⭐", 3, Color(0xFFFFB300)),
        NumberItem("৪", "4", "চার", "Four", "🚗", 4, Color(0xFF43A047)),
        NumberItem("৫", "5", "পাঁচ", "Five", "🦋", 5, Color(0xFF8E24AA)),
        NumberItem("৬", "6", "ছয়", "Six", "🍓", 6, Color(0xFFE91E63)),
        NumberItem("৭", "7", "সাত", "Seven", "🦆", 7, Color(0xFF00ACC1)),
        NumberItem("৮", "8", "আট", "Eight", "⚽", 8, Color(0xFFFF5722)),
        NumberItem("৯", "9", "নয়", "Nine", "🌸", 9, Color(0xFF00897B)),
        NumberItem("১০", "10", "দশ", "Ten", "🍦", 10, Color(0xFFF57C00)),
        NumberItem("১১", "11", "এগারো", "Eleven", "🍭", 11, Color(0xFF3949AB)),
        NumberItem("১২", "12", "বারো", "Twelve", "🐢", 12, Color(0xFF2E7D32)),
        NumberItem("১৩", "13", "তেরো", "Thirteen", "🚀", 13, Color(0xFFD81B60)),
        NumberItem("১৪", "14", "চৌদ্দ", "Fourteen", "🧁", 14, Color(0xFFFBC02D)),
        NumberItem("১৫", "15", "পনেরো", "Fifteen", "🐬", 15, Color(0xFF0288D1)),
        NumberItem("১৬", "16", "ষোলো", "Sixteen", "🌺", 16, Color(0xFF8E24AA)),
        NumberItem("১৭", "17", "সতেরো", "Seventeen", "⛵", 17, Color(0xFF009688)),
        NumberItem("১৮", "18", "আঠারো", "Eighteen", "💎", 18, Color(0xFF1E88E5)),
        NumberItem("১৯", "19", "উনিশ", "Nineteen", "🍉", 19, Color(0xFFE53935)),
        NumberItem("২০", "20", "বিশ", "Twenty", "👑", 20, Color(0xFFFF9800))
    )

    // MULTIPLICATION TABLES 1 TO 10 (নামতা)
    fun getMultiplicationTable(n: Int): MultiplicationTable {
        val bnDigits = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
        fun toBn(num: Int): String = num.toString().map { bnDigits[it - '0'] }.joinToString("")
        val multiplierBnWords = listOf("", "একে", "দুগুণে", "তিনে", "চারে", "পাঁচে", "ছয়ে", "সাতে", "আটে", "নয়ে", "দশে")

        val items = (1..10).map { i ->
            val prod = n * i
            val textBn = "${toBn(n)} × ${toBn(i)} = ${toBn(prod)}"
            val speechBn = when {
                n == 1 -> "${toBn(i)} একে ${toBn(i)}"
                n == 2 -> when (i) {
                    1 -> "২ একে ২"
                    2 -> "২ দুগুণে ৪"
                    3 -> "৩ দুগুণে ৬"
                    4 -> "৪ দুগুণে ৮"
                    5 -> "৫ দুগুণে ১০"
                    6 -> "৬ দুগুণে ১২"
                    7 -> "৭ দুগুণে ১৪"
                    8 -> "৮ দুগুণে ১৬"
                    9 -> "৯ দুগুণে ১৮"
                    10 -> "২ দশে ২০"
                    else -> "২ ${multiplierBnWords[i]} ${toBn(prod)}"
                }
                else -> "${toBn(n)} ${multiplierBnWords[i]} ${toBn(prod)}"
            }
            val textEn = "$n × $i = $prod"
            val speechEn = "$n times $i is $prod"
            MultiplicationItem(n, i, prod, textBn, speechBn, textEn, speechEn)
        }
        return MultiplicationTable(n, toBn(n), items)
    }

    // -------------------------------------------------------------
    // 4. RHYMES & POEMS (ছড়া ও কবিতা)
    // -------------------------------------------------------------
    val rhymesList = listOf(
        KidsRhyme(
            id = "rhyme_chand",
            titleBn = "চাঁদ উঠেছে ফুল ফুটেছে",
            titleEn = "Chand Uthechhe Ful Futeychhe",
            authorBn = "প্রচলিত লোকছড়া",
            authorEn = "Traditional Folk",
            emoji = "🌙",
            linesBn = listOf(
                "চাঁদ উঠেছে ফুল ফুটেছে",
                "কদম তলায় কে?",
                "হাতি নাচছে ঘোড়া নাচছে",
                "সোনামণির বে!"
            ),
            linesEn = listOf(
                "The moon has risen, flowers bloom,",
                "Who is under the Kadamba tree?",
                "The elephant dances, horse prances,",
                "It is our sweet baby's wedding jubilee!"
            ),
            themeColor = Color(0xFFFF9800),
            funFact = "বাংলা শিশুদের অন্যতম জনপ্রিয় এবং মিষ্টি ঘুমপাড়ানি ছড়া।"
        ),
        KidsRhyme(
            id = "rhyme_hattima",
            titleBn = "হাট্টিমা টিম টিম",
            titleEn = "Hattima Tim Tim",
            authorBn = "রোকনুজ্জামান খান",
            authorEn = "Roknuzzaman Khan",
            emoji = "🥚",
            linesBn = listOf(
                "হাট্টিমা টিম টিম",
                "তারা মাঠে পাড়ে ডিম,",
                "তাদের খাঁড়া দুটো শিং,",
                "তারা হাট্টিমা টিম টিম!"
            ),
            linesEn = listOf(
                "Hattima tim tim,",
                "They lay eggs in the green field,",
                "They have two straight upright horns,",
                "They are Hattima tim tim!"
            ),
            themeColor = Color(0xFF4CAF50),
            funFact = "হাট্টিমা টিম টিম ছড়াটি শুনলে শিশুরা আনন্দে হেসে ওঠে।"
        ),
        KidsRhyme(
            id = "rhyme_ata_gach",
            titleBn = "আতা গাছে তোতা পাখি",
            titleEn = "Ata Gachhe Tota Pakhi",
            authorBn = "প্রচলিত শিশুতোষ ছড়া",
            authorEn = "Traditional Folk",
            emoji = "🦜",
            linesBn = listOf(
                "আতা গাছে তোতা পাখি",
                "ডালিম গাছে মৌ,",
                "এত ডাকি তবু কথা",
                "কও না কেন বউ?"
            ),
            linesEn = listOf(
                "Parrot sits on custard-apple tree,",
                "Honeybee on pomegranate bough,",
                "I call you so much sweet girl,",
                "Why won't you talk right now?"
            ),
            themeColor = Color(0xFF009688),
            funFact = "পাখি ও গাছের অপরূপ বর্ণনা রয়েছে এই চিরচেনা ছড়ায়।"
        ),
        KidsRhyme(
            id = "rhyme_probhati",
            titleBn = "ভোর হলো দোর খোলো",
            titleEn = "Provati - Open the Door",
            authorBn = "কাজী নজরুল ইসলাম",
            authorEn = "Kazi Nazrul Islam",
            emoji = "🌅",
            linesBn = listOf(
                "ভোর হলো দোর খোলো",
                "খুকুমণি ওঠো রে!",
                "ঐ ডাকে জুঁই-শাখে",
                "ফুল-খুকী ছোট রে!"
            ),
            linesEn = listOf(
                "Dawn has arrived, open your door,",
                "Little baby girl wake up and play!",
                "The jasmine blossoms softly call,",
                "Run outside to greet the day!"
            ),
            themeColor = Color(0xFFE91E63),
            funFact = "আমাদের জাতীয় কবি কাজী নজরুল ইসলামের অমর শিশুতোষ ছড়া।"
        ),
        KidsRhyme(
            id = "rhyme_twinkle",
            titleBn = "টুইঙ্কল টুইঙ্কল লিটল স্টার",
            titleEn = "Twinkle, Twinkle, Little Star",
            authorBn = "জেন টেলর (Jane Taylor)",
            authorEn = "Jane Taylor",
            emoji = "⭐",
            linesBn = listOf(
                "টুইঙ্কল, টুইঙ্কল, লিটল স্টার,",
                "হাউ আই ওয়ান্ডার হোয়াট ইউ আর!",
                "আপ অ্যাভাব দ্য ওয়ার্ল্ড সো হাই,",
                "লাইক আ ডায়মন্ড ইন দ্য স্কাই!"
            ),
            linesEn = listOf(
                "Twinkle, twinkle, little star,",
                "How I wonder what you are!",
                "Up above the world so high,",
                "Like a diamond in the sky!"
            ),
            themeColor = Color(0xFF1E88E5),
            funFact = "The world's most famous English nursery rhyme loved by millions of kids."
        ),
        KidsRhyme(
            id = "rhyme_baabaa",
            titleBn = "বা বা ব্ল্যাক শিপ",
            titleEn = "Baa, Baa, Black Sheep",
            authorBn = "মাদার গুজ",
            authorEn = "Mother Goose",
            emoji = "🐑",
            linesBn = listOf(
                "বা বা ব্ল্যাক শিপ, হ্যাভ ইউ এনি উল?",
                "ইয়েস স্যার, ইয়েস স্যার, থ্রি ব্যাগস ফুল!",
                "ওয়ান ফর দ্য মাস্টার, ওয়ান ফর দ্য ডেম,",
                "অ্যান্ড ওয়ান ফর দ্য লিটল বয় হু লিভস ডাউন দ্য লেন!"
            ),
            linesEn = listOf(
                "Baa, baa, black sheep, have you any wool?",
                "Yes sir, yes sir, three bags full!",
                "One for the master, and one for the dame,",
                "And one for the little boy who lives down the lane!"
            ),
            themeColor = Color(0xFF7E57C2),
            funFact = "A lively rhyme teaching children sharing, colors, and rhythm."
        )
    )

    // -------------------------------------------------------------
    // 5. ANIMALS & NATURE (পশুপাখি ও চারপাশ)
    // -------------------------------------------------------------
    val natureItems = listOf(
        // ANIMALS & BIRDS
        NatureItem("বাঘ", "Tiger", "টাইগার", "হালুম!", "🐅", NatureCategory.ANIMALS, "রয়্যাল বেঙ্গল টাইগার বাংলাদেশের জাতীয় পশু।", Color(0xFFFF6F00)),
        NatureItem("সিংহ", "Lion", "লায়ন", "গর্জন!", "🦁", NatureCategory.ANIMALS, "সিংহকে বনের রাজা বলা হয়।", Color(0xFFFFA000)),
        NatureItem("হাতি", "Elephant", "এলিফ্যান্ট", "চিঁহিঁ!", "🐘", NatureCategory.ANIMALS, "হাতি হলো ডাঙার সবচেয়ে বড় স্তন্যপায়ী প্রাণী।", Color(0xFF546E7A)),
        NatureItem("হরিণ", "Deer", "ডিয়ার", "মায়াবী হরিণ", "🦌", NatureCategory.ANIMALS, "হরিণের চোখ খুব সুন্দর এবং সে দ্রুত দৌড়ায়।", Color(0xFF8D6E63)),
        NatureItem("বিড়াল", "Cat", "ক্যাট", "মিউ মিউ!", "🐱", NatureCategory.ANIMALS, "বিড়াল মাছ খেতে ও খেলতে খুব ভালোবাসে।", Color(0xFFFFB300)),
        NatureItem("কুকুর", "Dog", "ডগ", "ঘেউ ঘেউ!", "🐶", NatureCategory.ANIMALS, "কুকুর মানুষের সবচেয়ে বিশ্বস্ত ও প্রভুভক্ত বন্ধু।", Color(0xFF8D6E63)),
        NatureItem("গরু", "Cow", "কাউ", "হাম্বা!", "🐄", NatureCategory.ANIMALS, "গরু আমাদের পুষ্টিকর মিষ্টি দুধ দেয়।", Color(0xFF43A047)),
        NatureItem("ঘোড়া", "Horse", "হর্স", "চিঁহিঁ!", "🐎", NatureCategory.ANIMALS, "ঘোড়া খুব দ্রুত দৌড়াতে পারে।", Color(0xFF6D4C41)),
        NatureItem("ছাগল", "Goat", "গোট", "ম্যা ম্যা!", "🐐", NatureCategory.ANIMALS, "ছাগল ঘাস ও গাছের সবুজ পাতা খায়।", Color(0xFF795548)),
        NatureItem("খরগোশ", "Rabbit", "র‍্যাবিট", "লাফিয়ে চলে", "🐇", NatureCategory.ANIMALS, "খরগোশ গাজর খেতে খুব পছন্দ করে।", Color(0xFFE91E63)),
        NatureItem("বানর", "Monkey", "মাঙ্কি", "কিকি কিকি!", "🐒", NatureCategory.ANIMALS, "বানর গাছে গাছে লাফিয়ে খেলা করে।", Color(0xFFF57C00)),
        NatureItem("ভালুক", "Bear", "বেয়ার", "বনের ভালুক", "🐻", NatureCategory.ANIMALS, "ভালুক মিষ্টি মধু ও মাছ খেতে ভালোবাসে।", Color(0xFF5D4037)),
        NatureItem("জিরাফ", "Giraffe", "জিরাফ", "লম্বা গলা", "🦒", NatureCategory.ANIMALS, "জিরাফ পৃথিবীর সবচেয়ে লম্বা প্রাণী।", Color(0xFFFFB300)),
        NatureItem("জেব্রা", "Zebra", "জেব্রা", "কালো-সাদা দাগ", "🦓", NatureCategory.ANIMALS, "জেব্রার শরীরে সাদা ও কালো ডোরাকাটা দাগ থাকে।", Color(0xFF37474F)),
        NatureItem("ক্যাঙ্গারু", "Kangaroo", "ক্যাঙ্গারু", "লাফিয়ে ছোটে", "🦘", NatureCategory.ANIMALS, "ক্যাঙ্গারু তার থলিতে বাচ্চা রাখে।", Color(0xFFE65100)),
        NatureItem("উট", "Camel", "ক্যামেল", "মরুর জাহাজ", "🐪", NatureCategory.ANIMALS, "উট মরুভূমিতে অনেক দিন পানি না খেয়ে চলতে পারে।", Color(0xFFD84315)),
        NatureItem("ডলফিন", "Dolphin", "ডলফিন", "বন্ধুবৎসল", "🐬", NatureCategory.ANIMALS, "ডলফিন পানিতে চমৎকার খেলা দেখায়।", Color(0xFF0288D1)),
        NatureItem("তিমি", "Whale", "হোয়েল", "সাগরের বিশালাকায়", "🐋", NatureCategory.ANIMALS, "নীল তিমি পৃথিবীর সবচেয়ে বড় প্রাণী।", Color(0xFF1565C0)),
        NatureItem("কচ্ছপ", "Turtle", "টার্টল", "ধীরে হাঁটে", "🐢", NatureCategory.ANIMALS, "কচ্ছপের পিঠের শক্ত খোলস তাকে রক্ষা করে।", Color(0xFF2E7D32)),
        NatureItem("ব্যাঙ", "Frog", "ফ্রগ", "ঘ্যাঙর ঘ্যাঙ!", "🐸", NatureCategory.ANIMALS, "বর্ষাকালে ব্যাঙের ডাক বেশি শোনা যায়।", Color(0xFF388E3C)),
        NatureItem("দোয়েল", "Magpie Robin", "ম্যাগপাই", "শিস শিস!", "🐦", NatureCategory.ANIMALS, "দোয়েল বাংলাদেশের জাতীয় পাখি।", Color(0xFF0288D1)),
        NatureItem("টিয়া", "Parrot", "প্যারট", "কথা বলে", "🦜", NatureCategory.ANIMALS, "টিয়া পাখির সবুজ ডানা ও লাল টুকটুকে ঠোঁট।", Color(0xFF4CAF50)),
        NatureItem("ময়ূর", "Peacock", "পিকক", "পেখম মেলে নাচে", "🦚", NatureCategory.ANIMALS, "ময়ূর পেখম মেলে অপরূপ নাচ দেখায়।", Color(0xFF00897B)),
        NatureItem("কোকিল", "Cuckoo", "কুকু", "কুহু কুহু!", "🐦‍⬛", NatureCategory.ANIMALS, "বসন্তকালে কোকিল মিষ্টি সুরে গান গায়।", Color(0xFF37474F)),
        NatureItem("কাক", "Crow", "ক্রো", "কা কা!", "🐦‍⬛", NatureCategory.ANIMALS, "কাক খুব চতুর ও পরিচিত পাখি।", Color(0xFF455A64)),
        NatureItem("চড়ুই", "Sparrow", "স্প্যারো", "চিঁ চিঁ!", "🐤", NatureCategory.ANIMALS, "চড়ুই আমাদের ঘরের কোণে বাসা বাঁধে।", Color(0xFFFFB300)),
        NatureItem("পেঁচা", "Owl", "আউল", "হু হু!", "🦉", NatureCategory.ANIMALS, "পেঁচা রাতে জেগে থাকে ও স্পষ্ট দেখে।", Color(0xFF795548)),
        NatureItem("হাঁস", "Duck", "ডাক", "প্যাক প্যাক!", "🦆", NatureCategory.ANIMALS, "হাঁস পানিতে মনের সুখে সাঁতার কাটে।", Color(0xFF00897B)),
        NatureItem("রাজহাঁস", "Swan", "সোয়ান", "ধবধবে সাদা", "🦢", NatureCategory.ANIMALS, "রাজহাঁস দেখতে রাজকীয় ও অত্যন্ত সুন্দর।", Color(0xFF00ACC1)),
        NatureItem("বক", "Heron", "হেরন", "মাছ শিকারি", "🦩", NatureCategory.ANIMALS, "বক নদীর ধারে এক পায়ে দাঁড়িয়ে মাছ ধরে।", Color(0xFF009688)),
        NatureItem("ঈগল", "Eagle", "ঈগল", "আকাশের রাজা", "🦅", NatureCategory.ANIMALS, "ঈগল পাখি উঁচুতে উড়ে তীক্ষ্ণ চোখে শিকার খোঁজে।", Color(0xFF4E342E)),
        NatureItem("কবুতর", "Pigeon", "পিজিয়ন", "বাক বাকুম!", "🕊️", NatureCategory.ANIMALS, "কবুতর শান্তির প্রতীক হিসেবে পরিচিত।", Color(0xFF78909C)),
        NatureItem("মাছরাঙা", "Kingfisher", "কিংফিশার", "ঝাঁপ দিয়ে মাছ ধরে", "🐦", NatureCategory.ANIMALS, "মাছরাঙা রঙিন পালকের চতুর মাছ শিকারি পাখি।", Color(0xFF039BE5)),

        // FLOWERS
        NatureItem("শাপলা", "Water Lily", "ওয়াটার লিলি", "জাতীয় ফুল", "🪷", NatureCategory.FLOWERS, "সাদা শাপলা বাংলাদেশের জাতীয় ফুল।", Color(0xFF00ACC1)),
        NatureItem("গোলাপ", "Rose", "রোজ", "ফুলের রানি", "🌹", NatureCategory.FLOWERS, "গোলাপের মিষ্টি সুবাস মন জুড়িয়ে দেয়।", Color(0xFFE91E63)),
        NatureItem("সূর্যমুখী", "Sunflower", "সানফ্লাওয়ার", "সূর্যমুখী ফুল", "🌻", NatureCategory.FLOWERS, "সূর্যমুখী ফুল সূর্যের দিকে মুখ করে হাসে।", Color(0xFFFFB300)),
        NatureItem("জবা", "Hibiscus", "হিবিস্কাস", "রক্তজবা", "🌺", NatureCategory.FLOWERS, "লাল টুকটুকে জবা ফুল বাগান সুন্দর করে।", Color(0xFFC2185B)),
        NatureItem("বেলি", "Jasmine", "জেসমিন", "সুগন্ধি ফুল", "🌼", NatureCategory.FLOWERS, "বেলি ফুলের মালা পরতে মেয়েরা ভালোবাসে।", Color(0xFFFFF176)),
        NatureItem("রজনীগন্ধা", "Tuberose", "টিউবরোজ", "মনমাতানো গন্ধ", "🌸", NatureCategory.FLOWERS, "রজনীগন্ধা বিয়ে ও উৎসবে ব্যবহার করা হয়।", Color(0xFF81C784)),
        NatureItem("গাঁদা", "Marigold", "মেরিগোল্ড", "হলুদ গাঁদা", "🏵️", NatureCategory.FLOWERS, "শীতকালে গাঢ় হলুদ ও কমলা গাঁদা ফুল ফোটে।", Color(0xFFFF9800)),
        NatureItem("কদম", "Burflower", "কাদাম্বা", "বর্ষার ফুল", "🏵️", NatureCategory.FLOWERS, "বর্ষাকালে গাছের ডালে গোল গোল কদম ফুল ফোটে।", Color(0xFFFBC02D)),
        NatureItem("পদ্ম", "Lotus", "লোটাস", "পবিত্র পদ্ম", "🪷", NatureCategory.FLOWERS, "পদ্ম ফুল দিঘির জলে অপরূপ সৌন্দর্য ছড়ায়।", Color(0xFFEC407A)),
        NatureItem("পলাশ", "Flame of Forest", "পলাশ", "বসন্তের লাল", "🌺", NatureCategory.FLOWERS, "বসন্তে লাল পলাশ ফুলে গাছ রঙিন হয়ে ওঠে।", Color(0xFFD84315)),
        NatureItem("শিউলি", "Night Jasmine", "শিউলি", "শরতের ফুল", "💮", NatureCategory.FLOWERS, "শরতের ভোরে সুবাসিত শিউলি ফুল ঝরে পড়ে।", Color(0xFFFF7043)),
        NatureItem("চন্দ্রমল্লিকা", "Chrysanthemum", "ক্রিসেন্থিমাম", "শীতের বাহার", "💮", NatureCategory.FLOWERS, "চন্দ্রমল্লিকা নানা রঙের অপূর্ব শোভা দেয়।", Color(0xFF9C27B0)),
        NatureItem("ডালিয়া", "Dahlia", "ডালিয়া", "বড় পাপড়ি", "🌸", NatureCategory.FLOWERS, "শীতের বাগানে বড় ডালিয়া ফুল সবার নজর কাড়ে।", Color(0xFFAD1457)),

        // FRUITS
        NatureItem("আম", "Mango", "ম্যাঙ্গো", "ফলের রাজা", "🥭", NatureCategory.FRUITS, "আম হলো মিষ্টি রসালো ফলের রাজা।", Color(0xFFFF8F00)),
        NatureItem("কাঁঠাল", "Jackfruit", "জ্যাকফ্রুট", "জাতীয় ফল", "🍈", NatureCategory.FRUITS, "কাঁঠাল বাংলাদেশের জাতীয় ফল।", Color(0xFF7CB342)),
        NatureItem("কলা", "Banana", "ব্যানানা", "পুষ্টিকর ফল", "🍌", NatureCategory.FRUITS, "কলা খেলে শরীরে প্রচুর ভিটামিন ও শক্তি পাওয়া যায়।", Color(0xFFFBC02D)),
        NatureItem("আপেল", "Apple", "অ্যাপল", "লাল টুকটুকে", "🍎", NatureCategory.FRUITS, "প্রতিদিন একটি আপেল রোগবালাই দূরে রাখে।", Color(0xFFE53935)),
        NatureItem("কমলা", "Orange", "অরেঞ্জ", "মিষ্টি-টক ফল", "🍊", NatureCategory.FRUITS, "কমলালেবুতে প্রচুর ভিটামিন সি থাকে।", Color(0xFFFF6F00)),
        NatureItem("তরমুজ", "Watermelon", "ওয়াটারমেলন", "রসালো লাল", "🍉", NatureCategory.FRUITS, "গরমে ঠান্ডা তরমুজ খেলে শরীর জুড়িয়ে যায়।", Color(0xFFD81B60)),
        NatureItem("পেয়ারা", "Guava", "গুয়াভা", "সবুজ পেয়ারা", "🍐", NatureCategory.FRUITS, "পেয়ারাতে লেবুর চেয়েও বেশি ভিটামিন সি আছে।", Color(0xFF689F38)),
        NatureItem("আনারস", "Pineapple", "পাইনঅ্যাপল", "রসালো মিষ্টি", "🍍", NatureCategory.FRUITS, "আনারস একটি সুস্বাদু রসালো গ্রীষ্মকালীন ফল।", Color(0xFFF57C00)),
        NatureItem("লিচু", "Litchi", "লিচি", "মিষ্টি কোয়া", "🍒", NatureCategory.FRUITS, "লিচু রসালো ও অত্যন্ত মিষ্টি ফল।", Color(0xFFE91E63)),
        NatureItem("পেঁপে", "Papaya", "পাপায়া", "হলুদ পাকা পেঁপে", "🍈", NatureCategory.FRUITS, "পাকা পেঁপে মিষ্টি ও পেটের জন্য খুব উপকারী।", Color(0xFFFFA000)),
        NatureItem("আঙুর", "Grape", "গ্রেপস", "রসের থোকা", "🍇", NatureCategory.FRUITS, "আঙুর থোকায় থোকায় গাছে ধরে।", Color(0xFF7B1FA2)),
        NatureItem("ডালিম", "Pomegranate", "পোমেগ্রানেট", "দানা ভরা ফল", "🍎", NatureCategory.FRUITS, "ডালিমের লাল দানাগুলো দেখতে মুক্তার মতো।", Color(0xFFC2185B)),
        NatureItem("স্ট্রবেরি", "Strawberry", "স্ট্রবেরি", "লাল মিষ্টি", "🍓", NatureCategory.FRUITS, "স্ট্রবেরি খুব আকর্ষণীয় ও সুস্বাদু ফল।", Color(0xFFD81B60)),
        NatureItem("নারকেল", "Coconut", "কোকোনাট", "মিষ্টি পানি", "🥥", NatureCategory.FRUITS, "ডাবের পানি শরীর ঠান্ডা ও সতেজ রাখে।", Color(0xFF6D4C41)),
        NatureItem("লেবু", "Lemon", "লেমন", "টক লেবু", "🍋", NatureCategory.FRUITS, "লেবুর শরবত ক্লান্তি দূর করে।", Color(0xFFFDD835)),

        // VEGETABLES
        NatureItem("আলু", "Potato", "পটেটো", "সবজির রাজা", "🥔", NatureCategory.VEGETABLES, "আলু দিয়ে সব ধরনের তরকারি ও চিপস তৈরি হয়।", Color(0xFF8D6E63)),
        NatureItem("টমেটো", "Tomato", "টমেটো", "লাল সালাদ", "🍅", NatureCategory.VEGETABLES, "টমেটো কাঁচা ও রান্না উভয়ভাবেই খাওয়া যায়।", Color(0xFFE53935)),
        NatureItem("গাজর", "Carrot", "ক্যারট", "মিষ্টি গাজর", "🥕", NatureCategory.VEGETABLES, "গাজর খেলে চোখের দৃষ্টিশক্তি ভালো থাকে।", Color(0xFFFF6F00)),
        NatureItem("বেগুন", "Eggplant", "এগপ্ল্যান্ট", "বেগুনী সবজি", "🍆", NatureCategory.VEGETABLES, "মচমচে বেগুনি ভাজা খেতে খুবই চমৎকার।", Color(0xFF7B1FA2)),
        NatureItem("ফুলকপি", "Cauliflower", "কলিফ্লাওয়ার", "শীতের সবজি", "🥦", NatureCategory.VEGETABLES, "ফুলকপির রোস্ট ও তরকারি অত্যন্ত সুস্বাদু।", Color(0xFF43A047)),
        NatureItem("বাঁধাকপি", "Cabbage", "ক্যাবাজ", "পাতার বাঁধাকপি", "🥬", NatureCategory.VEGETABLES, "বাঁধাকপির সালাদ স্বাস্থ্যকর ও পুষ্টিকর।", Color(0xFF388E3C)),
        NatureItem("শসা", "Cucumber", "কিউকাম্বার", "ঠান্ডা সালাদ", "🥒", NatureCategory.VEGETABLES, "শসা শরীরে পানির ঘাটতি পূরণ করে।", Color(0xFF2E7D32)),
        NatureItem("মুলা", "Radish", "র‍্যাডিশ", "সাদা মুলা", "🌱", NatureCategory.VEGETABLES, "মুলা শীতে সালাদ ও তরকারি হিসেবে খাওয়া হয়।", Color(0xFF9E9E9E)),
        NatureItem("লাউ", "Bottle Gourd", "বটল গার্ড", "মিষ্টি লাউ", "🥒", NatureCategory.VEGETABLES, "লাউ শরীর ঠান্ডা রাখে ও খুব পুষ্টিকর।", Color(0xFF7CB342)),
        NatureItem("মিষ্টি কুমড়া", "Pumpkin", "পাম্পকিন", "হলুদ কুমড়া", "🎃", NatureCategory.VEGETABLES, "মিষ্টি কুমড়াতে প্রচুর ভিটামিন এ আছে।", Color(0xFFFF9800)),
        NatureItem("মটরশুঁটি", "Peas", "পিস", "সবুজ দানা", "🫛", NatureCategory.VEGETABLES, "পোলাও ও সালাদে মটরশুঁটি দিলে দারুণ লাগে।", Color(0xFF4CAF50)),
        NatureItem("ঢ্যাঁড়শ", "Okra", "ওকরা", "সবুজ ঢ্যাঁড়শ", "🌿", NatureCategory.VEGETABLES, "ঢ্যাঁড়শ ভাজি আমাদের সবার পছন্দের।", Color(0xFF00897B)),

        // VEHICLES
        NatureItem("গাড়ি", "Car", "কার", "পিপ পিপ!", "🚗", NatureCategory.VEHICLES, "গাড়ি চড়ে আমরা শহরের রাস্তায় চলাচল করি।", Color(0xFF1E88E5)),
        NatureItem("বাস", "Bus", "বাস", "পোঁ পোঁ!", "🚌", NatureCategory.VEHICLES, "বাসে করে অনেক মানুষ একসাথে ভ্রমণ করে।", Color(0xFFFF9800)),
        NatureItem("ট্রাক", "Truck", "ট্রাক", "ভারী মালপত্র", "🚚", NatureCategory.VEHICLES, "ট্রাক দিয়ে এক শহর থেকে অন্য শহরে পণ্য নেওয়া হয়।", Color(0xFF546E7A)),
        NatureItem("ট্রেন", "Train", "ট্রেন", "ঝিক ঝিক ঝিক!", "🚂", NatureCategory.VEHICLES, "ট্রেন রেললাইনের ওপর দিয়ে দূরদেশে যায়।", Color(0xFF37474F)),
        NatureItem("উড়োজাহাজ", "Airplane", "এয়ারপ্লেন", "আকাশে ওড়ে", "✈️", NatureCategory.VEHICLES, "উড়োজাহাজে চড়ে এক দেশ থেকে অন্য দেশে যাওয়া যায়।", Color(0xFF0288D1)),
        NatureItem("হেলিকপ্টার", "Helicopter", "হেলিকপ্টার", "পাখা ঘোরে", "🚁", NatureCategory.VEHICLES, "হেলিকপ্টার সোজা আকাশে উঠতে ও নামতে পারে।", Color(0xFFD81B60)),
        NatureItem("নৌকা", "Boat", "বোট", "নদীর বাহন", "⛵", NatureCategory.VEHICLES, "নদীতে বৈঠা বেয়ে বা পাল তুলে নৌকা চলে।", Color(0xFF009688)),
        NatureItem("লঞ্চ", "Launch", "লঞ্চ", "নদীর যাত্রীযান", "🚢", NatureCategory.VEHICLES, "লঞ্চে করে মানুষ নদী পারাপার হয়।", Color(0xFF00838F)),
        NatureItem("জাহাজ", "Ship", "শিপ", "সাগরের জাহাজ", "🛳️", NatureCategory.VEHICLES, "বিশাল জাহাজ মহাসাগরের বুক চিরে চলে।", Color(0xFF1565C0)),
        NatureItem("সাইকেল", "Bicycle", "বাইসাইকেল", "প্যাডেল দিয়ে চলে", "🚲", NatureCategory.VEHICLES, "সাইকেল চালানো স্বাস্থ্যের জন্য খুব ভালো।", Color(0xFF43A047)),
        NatureItem("রিকশা", "Rickshaw", "রিকশা", "টুং টাং বেল", "🛺", NatureCategory.VEHICLES, "রিকশা বাংলাদেশের ঐতিহ্যবাহী জনপ্রিয় বাহন।", Color(0xFFE91E63)),
        NatureItem("মোটরসাইকেল", "Motorcycle", "মোটরবাইক", "দ্রুত গতি", "🏍️", NatureCategory.VEHICLES, "মোটরসাইকেল চালানোর সময় হেলমেট পরতে হয়।", Color(0xFFD32F2F)),
        NatureItem("অ্যাম্বুলেন্স", "Ambulance", "অ্যাম্বুলেন্স", "সাইরেন বাজে!", "🚑", NatureCategory.VEHICLES, "অ্যাম্বুলেন্স জরুরি সময়ে রোগীকে হাসপাতালে নেয়।", Color(0xFFC62828)),
        NatureItem("ফায়ার ট্রাক", "Fire Truck", "ফায়ার ইঞ্জিন", "আগুন নেভায়", "🚒", NatureCategory.VEHICLES, "দমকল কর্মীরা ফায়ার ট্রাক দিয়ে আগুন নেভান।", Color(0xFFE53935)),
        NatureItem("রকেট", "Rocket", "রকেট", "মহাকাশে যায়", "🚀", NatureCategory.VEHICLES, "রকেট পৃথিবীর বাইরে মহাকাশে নভোচারী নিয়ে যায়।", Color(0xFF6A1B9A)),

        // COLORS & SHAPES
        NatureItem("লাল", "Red", "রেড", "রং", "🔴", NatureCategory.COLORS_SHAPES, "লাল রক্তের রং ও আমাদের জাতীয় পতাকার সূর্যের রং।", Color(0xFFE53935)),
        NatureItem("সবুজ", "Green", "গ্রিন", "রং", "🟢", NatureCategory.COLORS_SHAPES, "সবুজ গাছপালা ও আমাদের পতাকার জমিনের রং।", Color(0xFF43A047)),
        NatureItem("নীল", "Blue", "ব্লু", "রং", "🔵", NatureCategory.COLORS_SHAPES, "নীল আকাশ ও গভীর সমুদ্রের চমৎকার রং।", Color(0xFF1E88E5)),
        NatureItem("হলুদ", "Yellow", "ইয়েলো", "রং", "🟡", NatureCategory.COLORS_SHAPES, "হলুদ পাকা আম, কলা ও রোদঝিলমিল সূর্য।", Color(0xFFFBC02D)),
        NatureItem("কমলা", "Orange", "অরেঞ্জ", "রং", "🟠", NatureCategory.COLORS_SHAPES, "কমলা লেবু ও গোধূলির আকাশের রং।", Color(0xFFFF6F00)),
        NatureItem("বেগুনি", "Purple", "পার্পল", "রং", "🟣", NatureCategory.COLORS_SHAPES, "বেগুন ও সুন্দর অপরাজিতা ফুলের রং।", Color(0xFF7B1FA2)),
        NatureItem("গোলাপি", "Pink", "পিঙ্ক", "রং", "💖", NatureCategory.COLORS_SHAPES, "গোলাপ ও পদ্ম ফুলের মিষ্টি রং।", Color(0xFFE91E63)),
        NatureItem("কালো", "Black", "ব্ল্যাক", "রং", "⚫", NatureCategory.COLORS_SHAPES, "কালো কুচকুচে রাত ও মাথার চুলের রং।", Color(0xFF212121)),
        NatureItem("সাদা", "White", "হোয়াইট", "রং", "⚪", NatureCategory.COLORS_SHAPES, "সাদা শাপলা, দুধ ও শান্তির প্রতীক।", Color(0xFF78909C)),
        NatureItem("বৃত্ত", "Circle", "সার্কেল", "আকার", "⭕", NatureCategory.COLORS_SHAPES, "থালা, চাকা ও ফুটবলের গোল রূপকে বৃত্ত বলে।", Color(0xFFFF9800)),
        NatureItem("ত্রিভুজ", "Triangle", "ট্রায়াঙ্গল", "আকার", "🔺", NatureCategory.COLORS_SHAPES, "তিনটি সরলরেখা দিয়ে ঘেরা রূপকে ত্রিভুজ বলে।", Color(0xFF8E24AA)),
        NatureItem("বর্গক্ষেত্র", "Square", "স্কয়ার", "আকার", "⏹️", NatureCategory.COLORS_SHAPES, "চারটি সমান বাহু বিশিষ্ট রূপকে বর্গ বা চারকোনা বলে।", Color(0xFF00ACC1)),
        NatureItem("তারা", "Star", "স্টার", "আকার", "⭐", NatureCategory.COLORS_SHAPES, "পাঁচ কোনাবিশিষ্ট আকাশের ঝলমলে তারা।", Color(0xFFFFB300)),
        NatureItem("হার্ট", "Heart", "হার্ট", "আকার", "❤️", NatureCategory.COLORS_SHAPES, "ভালোবাসার মিষ্টি হৃদয়ের আকার।", Color(0xFFD81B60))
    )

    // -------------------------------------------------------------
    // 6. QUIZ & MEMORY MATCH DATA
    // -------------------------------------------------------------
    val quizQuestions = listOf(
        QuizQuestion(
            type = QuizGameType.PICTURE_TO_WORD,
            questionBn = "ছবিটি দেখে বলো এটি কিসের ফল?",
            questionEn = "Look at the picture, which fruit is this?",
            visualPrompt = "🥭",
            options = listOf("আপেল", "আম", "কলা", "কমলা"),
            correctIndex = 1,
            rewardPoints = 10,
            hintBn = "এটি মিষ্টি রসালো ফলের রাজা!"
        ),
        QuizQuestion(
            type = QuizGameType.MISSING_LETTER,
            questionBn = "সঠিক বর্ণটি বসিয়ে শব্দ বানাও: ব + [ ? ] = বই",
            questionEn = "Find the missing letter: B + [ ? ] = BOOK",
            visualPrompt = "ব + [ ? ] 📚",
            options = listOf("ক", "ই", "ম", "ল"),
            correctIndex = 1,
            rewardPoints = 10,
            hintBn = "ব এর পর হ্রস্ব ই বসলে হয় বই!"
        ),
        QuizQuestion(
            type = QuizGameType.ANIMAL_SOUND,
            questionBn = "কোন প্রাণী 'মিউ মিউ' করে ডাকে?",
            questionEn = "Which animal says 'Meow'?",
            visualPrompt = "🐱",
            options = listOf("কুকুর", "গরু", "বিড়াল", "ঘোড়া"),
            correctIndex = 2,
            rewardPoints = 10,
            hintBn = "এটি দুধ-মাছ খেতে ভালোবাসে!"
        ),
        QuizQuestion(
            type = QuizGameType.COUNTING_PUZZLE,
            questionBn = "এখানে কয়টি তারা আছে গুনে বলো?",
            questionEn = "Count how many stars are here?",
            visualPrompt = "⭐ ⭐ ⭐ ⭐ ⭐",
            options = listOf("৩টি", "৪টি", "৫টি", "৬টি"),
            correctIndex = 2,
            rewardPoints = 10,
            hintBn = "আঙুল দিয়ে এক এক করে গুনে দেখো!"
        ),
        QuizQuestion(
            type = QuizGameType.MISSING_LETTER,
            questionBn = "Fill in the missing letter: C - [ ? ] - T = CAT 🐱",
            questionEn = "Complete the CVC word:",
            visualPrompt = "C _ T",
            options = listOf("O", "A", "U", "E"),
            correctIndex = 1,
            rewardPoints = 10,
            hintBn = "The vowel /æ/ like Apple!"
        ),
        QuizQuestion(
            type = QuizGameType.PICTURE_TO_WORD,
            questionBn = "বাংলাদেশের জাতীয় পাখি কোনটি?",
            questionEn = "What is the national bird of Bangladesh?",
            visualPrompt = "🐦",
            options = listOf("টিয়া", "ময়ূর", "দোয়েল", "কোকিল"),
            correctIndex = 2,
            rewardPoints = 10,
            hintBn = "কালো-সাদা রঙের মিষ্টি গানের দোয়েল!"
        ),
        QuizQuestion(
            type = QuizGameType.PICTURE_TO_WORD,
            questionBn = "কোন ফুলটি আমাদের জাতীয় ফুল?",
            questionEn = "What is the national flower of Bangladesh?",
            visualPrompt = "🪷",
            options = listOf("গোলাপ", "সূর্যমুখী", "শাপলা", "গাঁদা"),
            correctIndex = 2,
            rewardPoints = 10,
            hintBn = "জলে ফোটা সাদা শাপলা ফুল!"
        ),
        QuizQuestion(
            type = QuizGameType.PICTURE_TO_WORD,
            questionBn = "বনের রাজা বলা হয় কোন প্রাণীকে?",
            questionEn = "Who is the king of the jungle?",
            visualPrompt = "🦁",
            options = listOf("হাতি", "সিংহ", "বাঘ", "ভালুক"),
            correctIndex = 1,
            rewardPoints = 10,
            hintBn = "কেশরী সিংহের মাথায় যেন মুকুট!"
        ),
        QuizQuestion(
            type = QuizGameType.COUNTING_PUZZLE,
            questionBn = "এখানে কয়টি আপেল আছে গুনে বলো?",
            questionEn = "Count how many apples are here?",
            visualPrompt = "🍎 🍎 🍎",
            options = listOf("১টি", "২টি", "৩টি", "৪টি"),
            correctIndex = 2,
            rewardPoints = 10,
            hintBn = "এক, দুই, তিন!"
        ),
        QuizQuestion(
            type = QuizGameType.ANIMAL_SOUND,
            questionBn = "গরু কীভাবে ডাকে?",
            questionEn = "What sound does a cow make?",
            visualPrompt = "🐄",
            options = listOf("ঘেউ ঘেউ", "হাম্বা হাম্বা", "প্যাক প্যাক", "চিঁহিঁ"),
            correctIndex = 1,
            rewardPoints = 10,
            hintBn = "গরু ডাকে হাম্বা হাম্বা!"
        ),
        QuizQuestion(
            type = QuizGameType.MISSING_LETTER,
            questionBn = "Complete the word: S - U - [ ? ] = SUN ☀️",
            questionEn = "Fill the last letter:",
            visualPrompt = "S U _",
            options = listOf("T", "N", "P", "G"),
            correctIndex = 1,
            rewardPoints = 10,
            hintBn = "S-U-N is SUN!"
        ),
        QuizQuestion(
            type = QuizGameType.PICTURE_TO_WORD,
            questionBn = "কোন বাহনটি আকাশে ওড়ে?",
            questionEn = "Which vehicle flies in the sky?",
            visualPrompt = "✈️",
            options = listOf("গাড়ি", "ট্রেন", "উড়োজাহাজ", "নৌকা"),
            correctIndex = 2,
            rewardPoints = 10,
            hintBn = "ডানা মেলে মেঘের ওপরে উড়ে চলে!"
        ),
        QuizQuestion(
            type = QuizGameType.PICTURE_TO_WORD,
            questionBn = "বাংলাদেশের জাতীয় ফল কোনটি?",
            questionEn = "What is the national fruit of Bangladesh?",
            visualPrompt = "🍈",
            options = listOf("আম", "কাঁঠাল", "কলা", "লিচু"),
            correctIndex = 1,
            rewardPoints = 10,
            hintBn = "গায়ে কাঁটা কিন্তু ভেতরে মিষ্টি কোয়া!"
        ),
        QuizQuestion(
            type = QuizGameType.MISSING_LETTER,
            questionBn = "সঠিক বর্ণ বসাও: ক + [ ? ] = কলম 🖊️",
            questionEn = "Find missing letters: K + [ ? ] = KALAM",
            visualPrompt = "ক + [ ? ] = কলম",
            options = listOf("রম", "লম", "দম", "গম"),
            correctIndex = 1,
            rewardPoints = 10,
            hintBn = "ক + ল + ম = কলম!"
        ),
        QuizQuestion(
            type = QuizGameType.PICTURE_TO_WORD,
            questionBn = "গোল আকারের আকৃতিকে কী বলা হয়?",
            questionEn = "What is the name of round shape?",
            visualPrompt = "⭕",
            options = listOf("ত্রিভুজ", "বর্গ", "বৃত্ত", "তারা"),
            correctIndex = 2,
            rewardPoints = 10,
            hintBn = "ফুটবলের মতো গোলাকার বৃত্ত!"
        )
    )

    // Pairs for Memory Game
    val memoryPairs = listOf(
        Pair("🦁", "সিংহ (Lion)"),
        Pair("🍎", "আপেল (Apple)"),
        Pair("🚗", "গাড়ি (Car)"),
        Pair("🌸", "ফুল (Flower)"),
        Pair("⭐", "তারা (Star)"),
        Pair("🐟", "মাছ (Fish)"),
        Pair("🐘", "হাতি (Elephant)"),
        Pair("🥭", "আম (Mango)")
    )
}

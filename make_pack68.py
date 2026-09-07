# -*- coding: utf-8 -*-
import json

with open('app/src/main/assets/dictionary_1000.json') as f:
    existing_words = set(w['word'].strip().lower() for w in json.load(f))

with open('pack67.json') as f:
    p67_words = set(w['word'].strip().lower() for w in json.load(f))

words_p68_raw = [
    # 1. Linguistics, Phonetics & Writing Systems
    {
        "word": "Diacritic",
        "pos": "Noun",
        "phonetic": "/ˌdaɪ.əˈkrɪt.ɪk/ (ডায়াক্রিটিক)",
        "meaningBn": "উচ্চারণ বা অর্থ পরিবর্তনের জন্য বর্ণের উপরে বা নিচে যুক্ত বিশেষ চিহ্ন (যেমন আকার, একার, উমলাউট বা একসেন্ট)",
        "synonyms": ["Diacritical mark", "Accent mark", "Phonetic glyph"],
        "antonyms": ["Base letter", "Unmarked glyph"],
        "exampleEn": "The French word 'café' uses an acute accent as a diacritic over the letter e.",
        "exampleBn": "ফরাসি শব্দ 'café'-তে e বর্ণের ওপর ডায়াক্রিটিক চিহ্ন হিসেবে একটি অ্যাকুট অ্যাকসেন্ট ব্যবহৃত হয়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Umlaut",
        "pos": "Noun",
        "phonetic": "/ˈʊm.laʊt/ (উমলাউট)",
        "meaningBn": "জার্মান বা নর্ডিক বর্ণমালায় স্বরবর্ণের মাথার উপর স্থাপিত দুটি বিন্দু যা স্বরধ্বনির পরিবর্তন নির্দেশ করে (যেমন ä, ö, ü)",
        "synonyms": ["Diaeresis mark", "Vowel mutation sign"],
        "antonyms": ["Uninflected vowel"],
        "exampleEn": "Adding an umlaut in German often converts a singular noun into its plural form.",
        "exampleBn": "জার্মান ভাষায় উমলাউট চিহ্ন যোগ করলে প্রায়শই একবচন বিশেষ্য বহুবচনে রূপান্তরিত হয়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Cedilla",
        "pos": "Noun",
        "phonetic": "/sɪˈdɪl.ə/ (সেডিলা)",
        "meaningBn": "ফরাসি বা পর্তুগিজ ভাষায় c বর্ণের নিচে যুক্ত হুকের মতো চিহ্ন যা 'ক' ধ্বনিকে 'স' ধ্বনিতে রূপান্তর করে (ç)",
        "synonyms": ["Subscript tail", "Softening hook"],
        "antonyms": ["Hard consonant"],
        "exampleEn": "The word 'façade' preserves the French cedilla beneath the c to indicate a soft 's' sound.",
        "exampleBn": "'façade' শব্দটিতে নরম 'স' উচ্চারণ নির্দেশ করতে c অক্ষরের নিচে ফরাসি সেডিলা চিহ্নটি সংরক্ষিত রয়েছে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Circumflex",
        "pos": "Noun",
        "phonetic": "/ˈsɜːr.kəm.flɛks/ (সার্কামফ্লেক্স)",
        "meaningBn": "স্বরবর্ণের ওপরে স্থাপিত উল্টানো V-আকৃতির চিহ্ন (^)",
        "synonyms": ["Caret accent", "Roof mark", "Hat accent"],
        "antonyms": ["Macron"],
        "exampleEn": "In French orthography, a circumflex often indicates a historical loss of an 's' sound.",
        "exampleBn": "ফরাসি বানানে একটি সার্কামফ্লেক্স চিহ্ন প্রায়শই কোনো প্রাচীন বিলুপ্ত 's' ধ্বনির নির্দেশক হিসেবে কাজ করে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Macron",
        "pos": "Noun",
        "phonetic": "/ˈmækrɒn/ (ম্যাক্রন)",
        "meaningBn": "স্বরবর্ণের দীর্ঘ উচ্চারণ নির্দেশক অনুভূমিক সরলরেখা চিহ্ন (¯)",
        "synonyms": ["Long vowel bar", "Horizontal accent"],
        "antonyms": ["Breve (short vowel mark)"],
        "exampleEn": "Dictionaries employ a macron over vowels to signify elongated pronunciation.",
        "exampleBn": "অভিধানে স্বরবর্ণের দীর্ঘ টান বা উচ্চারণ নির্দেশ করতে অক্ষরের ওপর ম্যাক্রন চিহ্ন ব্যবহার করা হয়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Breve",
        "pos": "Noun",
        "phonetic": "/briːv/ (ব্রেভ)",
        "meaningBn": "স্বরবর্ণের হ্রস্ব বা খাটো উচ্চারণ নির্দেশক ছোট অর্ধচন্দ্রাকৃতি বাঁকা চিহ্ন (˘)",
        "synonyms": ["Short vowel sign", "Curved diacritic"],
        "antonyms": ["Macron"],
        "exampleEn": "A breve placed above a vowel in pronunciation guides indicates a short vowel sound.",
        "exampleBn": "উচ্চারণ নির্দেশিকায় স্বরবর্ণের ওপর ব্রেভ চিহ্ন বসিয়ে সেটির হ্রস্ব স্বরধ্বনি নির্দেশ করা হয়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Digraph",
        "pos": "Noun",
        "phonetic": "/ˈdaɪ.ɡræf/ (ডাইগ্রাফ / যুগ্মবর্ণ)",
        "meaningBn": "পাশাপাশি দুটি বর্ণ মিলে যখন একটি একক মৌলিক ধ্বনি প্রকাশ করে (যেমন 'sh' বা 'ph')",
        "synonyms": ["Two-letter phonogram", "Compound letter"],
        "antonyms": ["Monograph", "Single letter"],
        "exampleEn": "The letters 'th' in 'thin' form a digraph producing a single unvoiced dental sound.",
        "exampleBn": "'thin' শব্দের 'th' বর্ণ দুটি মিলে একটি ডাইগ্রাফ তৈরি করে যা একটি একক দন্ত্য ধ্বনি সৃষ্টি করে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Diphthong",
        "pos": "Noun",
        "phonetic": "/ˈdɪf.θɒŋ/ (ডিপথং / যৌগিক স্বরধ্বনি)",
        "meaningBn": "একই ঝোঁকে উচ্চারিত দুটি স্বরধ্বনির মিশ্রণে সৃষ্ট অবিচ্ছিন্ন যৌগিক স্বরধ্বনি (যেমন বাংলায় 'ঐ' বা 'ঔ')",
        "synonyms": ["Gliding vowel", "Compound vowel sound"],
        "antonyms": ["Monophthong (pure vowel)"],
        "exampleEn": "The vowel sound in the word 'coin' is a diphthong gliding from 'o' to 'i'.",
        "exampleBn": "'coin' শব্দের স্বরধ্বনিটি একটি যৌগিক স্বরধ্বনি বা ডিপথং যা 'o' থেকে 'i'-তে মসৃণভাবে পরিবর্তিত হয়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Trigraph",
        "pos": "Noun",
        "phonetic": "/ˈtraɪ.ɡræf/ (ট্রাইগ্রাফ)",
        "meaningBn": "পরপর তিনটি বর্ণ মিলে যখন একটি একক ধ্বনি তৈরি করে (যেমন 'sch' বা 'eau')",
        "synonyms": ["Three-letter phonogram", "Triple letter combination"],
        "antonyms": ["Single letter", "Digraph"],
        "exampleEn": "In German, the trigraph 'sch' represents the single sound equivalent to English 'sh'.",
        "exampleBn": "জার্মান ভাষায় 'sch' ট্রাইগ্রাফটি ইংরেজির 'sh'-এর সমতুল্য একটি একক ধ্বনি প্রকাশ করে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Ligature",
        "pos": "Noun",
        "phonetic": "/ˈlɪɡ.ə.tʃər/ (লিগেচার / যুক্তাক্ষর)",
        "meaningBn": "মুদ্রণ ও মুদ্রাক্ষরবিদ্যায় দুটি বা ততোধিক বর্ণকে একত্রিত করে গঠিত একটি সংযুক্ত রূপ (যেমন æ বা œ)",
        "synonyms": ["Typographic glyph union", "Joint letterform", "Connected character"],
        "antonyms": ["Separate letters", "Unlinked glyphs"],
        "exampleEn": "Classic typography often connects 'f' and 'i' into an elegant single ligature.",
        "exampleBn": "ধ্রুপদী মুদ্রণবিদ্যায় সৌন্দর্য বৃদ্ধির জন্য প্রায়ই 'f' ও 'i'-কে একটি মার্জিত লিগেচারে যুক্ত করা হয়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Syllabary",
        "pos": "Noun",
        "phonetic": "/ˈsɪl.əˌbɛr.i/ (সিলেবারি)",
        "meaningBn": "এমন লিখনপদ্ধতি যেখানে প্রতিটি চিহ্ন একক ধ্বনির বদলে একটি পূর্ণ দল বা সিলেবলকে প্রকাশ করে (যেমন জাপানি হিরাগানা)",
        "synonyms": ["Syllabic writing system", "Syllabic alphabet"],
        "antonyms": ["Phonemic alphabet", "Logographic system"],
        "exampleEn": "Japanese hiragana and katakana are syllabaries where each symbol represents a consonant-vowel pair.",
        "exampleBn": "জাপানি হিরাগানা ও কাতাকানা হলো সিলেবারি পদ্ধতি যেখানে প্রতিটি চিহ্ন ব্যঞ্জন ও স্বরের যুগল দল প্রকাশ করে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Ideogram",
        "pos": "Noun",
        "phonetic": "/ˈaɪ.di.əˌɡræm/ (আইডিওগ্রাম / ভাবলিপি)",
        "meaningBn": "এমন লিখিত প্রতীক বা চিহ্ন যা কোনো নির্দিষ্ট ধ্বনি নয় বরং সরাসরি একটি ধারণা বা অর্থ প্রকাশ করে (যেমন চীনা হরফ বা 💡)",
        "synonyms": ["Idea symbol", "Concept glyph", "Pictographic character"],
        "antonyms": ["Phonogram", "Phonetic letter"],
        "exampleEn": "Road signs showing a pedestrian crossed out serve as universal ideograms forbidding foot traffic.",
        "exampleBn": "ক্রস চিহ্নযুক্ত পথচারী আঁকা রাস্তার সাইনবোর্ডগুলো পথচারী চলাচল নিষিদ্ধের সর্বজনীন ভাবলিপি হিসেবে কাজ করে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Pictogram",
        "pos": "Noun",
        "phonetic": "/ˈpɪk.təˌɡræm/ (পিক্টোগ্রাম / চিত্রলিপি)",
        "meaningBn": "কোনো বস্তু বা ধারণাকে সরাসরি চিত্রাঙ্কনের মাধ্যমে উপস্থাপনকারী প্রতীক বা ছবি",
        "synonyms": ["Picture symbol", "Iconic sign", "Graphic emblem"],
        "antonyms": ["Abstract alphabet"],
        "exampleEn": "Ancient Sumerian cuneiform originally evolved from simple clay pictograms.",
        "exampleBn": "প্রাচীন সুমেরীয় কিউনিফর্ম লিপি মূলত মাটির ওপর আঁকা সাধারণ চিত্রলিপি বা পিক্টোগ্রাম থেকেই বিবর্তিত হয়েছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Logogram",
        "pos": "Noun",
        "phonetic": "/ˈlɒɡ.əˌɡræm/ (লোগোগ্রাম / শব্দপ্রতীক)",
        "meaningBn": "এমন একক লিখিত চিহ্ন যা একটি সম্পূর্ণ শব্দ বা রূপমূল প্রকাশ করে (যেমন $, &, বা %)",
        "synonyms": ["Word symbol", "Logograph", "Word glyph"],
        "antonyms": ["Letter", "Phoneme character"],
        "exampleEn": "The ampersand sign '&' is a logogram standing directly for the word 'and'.",
        "exampleBn": "অ্যাম্পারস্যান্ড চিহ্ন '&' হলো একটি লোগোগ্রাম যা সরাসরি 'and' শব্দের বিকল্প হিসেবে ব্যবহৃত হয়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Phoneme",
        "pos": "Noun",
        "phonetic": "/ˈfoʊ.niːm/ (ফোনিম / ধ্বনিমূল)",
        "meaningBn": "কোনো ভাষার ক্ষুদ্রতম অর্থপৃথককারী মৌলিক উচ্চারিত ধ্বনি একক (যেমন 'প' ও 'ব')",
        "synonyms": ["Distinctive sound unit", "Fundamental vocal speech sound"],
        "antonyms": ["Allophone variant"],
        "exampleEn": "Switching the phoneme /p/ with /b/ changes the English word 'pat' to 'bat'.",
        "exampleBn": "ফোনিম /p/-এর জায়গায় /b/ ধ্বনিমূল বসালে ইংরেজি শব্দ 'pat' বদলে গিয়ে 'bat' হয়ে যায়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Morpheme",
        "pos": "Noun",
        "phonetic": "/ˈmɔːr.fiːm/ (মরফিম / রূপমূল)",
        "meaningBn": "কোনো ভাষার ক্ষুদ্রতম অর্থবোধক বা ব্যাকরণিক একক যাকে আর বিভাজিত করা যায় না",
        "synonyms": ["Minimal meaningful linguistic unit", "Linguistic base component"],
        "antonyms": ["Sentence", "Clause"],
        "exampleEn": "The word 'unbreakable' contains three morphemes: the prefix 'un-', root 'break', and suffix '-able'.",
        "exampleBn": "'unbreakable' শব্দটিতে তিনটি রূপমূল রয়েছে: উপসর্গ 'un-', মূল 'break', এবং প্রত্যয় '-able'।",
        "category": "Literature & Arts"
    },
    {
        "word": "Allophone",
        "pos": "Noun",
        "phonetic": "/ˈæl.əˌfoʊn/ (অ্যালোফোন / সহধ্বনি)",
        "meaningBn": "একই ধ্বনিমূলের প্রেক্ষাপটভেদে ভিন্ন ভিন্ন কিন্তু অর্থের পরিবর্তন না ঘটানো উচ্চারণ বৈচিত্র্য",
        "synonyms": ["Phonetic variant", "Positional sound variant"],
        "antonyms": ["Phoneme"],
        "exampleEn": "The aspirated 'p' in 'pin' and unaspirated 'p' in 'spin' are allophones of the same phoneme.",
        "exampleBn": "'pin' শব্দের মহাপ্রাণ 'p' এবং 'spin' শব্দের অল্পপ্রাণ 'p' হলো একই ধ্বনিমূলের দুটি সহধ্বনি।",
        "category": "Literature & Arts"
    },
    {
        "word": "Allomorph",
        "pos": "Noun",
        "phonetic": "/ˈæl.əˌmɔːrf/ (অ্যালোমর্ফ / সহরূপ)",
        "meaningBn": "একই রূপমূলের ভিন্ন ভিন্ন উচ্চারণ বা রূপগত বৈচিত্র্য (যেমন বহুবচনে -s, -es, -en)",
        "synonyms": ["Morphemic variant", "Contextual morpheme variant"],
        "antonyms": ["Morpheme"],
        "exampleEn": "The English plural suffixes in 'cats' /s/, 'dogs' /z/, and 'horses' /ɪz/ are allomorphs.",
        "exampleBn": "ইংরেজি 'cats', 'dogs' এবং 'horses'-এর বহুবচন প্রত্যয়গুলো একই রূপমূলের বিভিন্ন সহরূপ।",
        "category": "Literature & Arts"
    },
    {
        "word": "Grapheme",
        "pos": "Noun",
        "phonetic": "/ˈɡræf.iːm/ (গ্রাফিম / বর্ণমূল)",
        "meaningBn": "কোনো লিখনপদ্ধতির ক্ষুদ্রতম অর্থপূর্ণ একক বা লিখিত বর্ণ",
        "synonyms": ["Written letter unit", "Orthographic symbol"],
        "antonyms": ["Spoken phoneme"],
        "exampleEn": "A single phoneme can be represented by multiple graphemes in English spelling.",
        "exampleBn": "ইংরেজি বানানে একটি একক ধ্বনিমূল একাধিক বর্ণমূল বা গ্রাফিম দ্বারা লিখিত হতে পারে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Lexeme",
        "pos": "Noun",
        "phonetic": "/ˈlɛk.siːm/ (লেক্সিম / শব্দমূল)",
        "meaningBn": "অভিধানের মূল শব্দ যার আওতায় তার বিভিন্ন ব্যাকরণিক রূপ অন্তর্ভুক্ত থাকে (যেমন run, runs, ran, running)",
        "synonyms": ["Fundamental lexical unit", "Dictionary headword entry"],
        "antonyms": ["Inflected token"],
        "exampleEn": "The words 'write', 'wrote', and 'written' are all inflectional forms of the single lexeme WRITE.",
        "exampleBn": "'write', 'wrote', এবং 'written' শব্দগুলো WRITE নামক একটিমাত্র লেক্সিমের ব্যাকরণিক রূপভেদ।",
        "category": "Literature & Arts"
    },
    {
        "word": "Calque",
        "pos": "Noun",
        "phonetic": "/kælk/ (ক্যাল্ক / অনুবাদ ঋণ)",
        "meaningBn": "অন্য ভাষা থেকে শব্দ বা বাক্যাংশকে আক্ষরিকভাবে হুবহু অনুবাদ করে নিজের ভাষায় গ্রহণ করা (যেমন skyscraper -> আকাশচুম্বী)",
        "synonyms": ["Loan translation", "Direct semantic borrowing"],
        "antonyms": ["Phonetic loanword"],
        "exampleEn": "The English term 'flea market' is a direct calque of the French 'marché aux puces'.",
        "exampleBn": "ইংরেজি 'flea market' শব্দটি ফরাসি 'marché aux puces'-এর একটি প্রত্যক্ষ আক্ষরিক অনুবাদ ঋণ।",
        "category": "Literature & Arts"
    },
    {
        "word": "Portmanteau",
        "pos": "Noun",
        "phonetic": "/pɔːrtˈmæn.toʊ/ (পোর্টম্যান্টো / জোড়কলম শব্দ)",
        "meaningBn": "দুটি ভিন্ন শব্দের অংশ জুড়ে দিয়ে গঠিত নতুন সংকর শব্দ (যেমন smoke + fog = smog; brunch)",
        "synonyms": ["Blend word", "Telescopic compound", "Coinage combination"],
        "antonyms": ["Compound word without blending", "Root word"],
        "exampleEn": "The word 'podcast' is a famous portmanteau blending 'iPod' and 'broadcast'.",
        "exampleBn": "'podcast' শব্দটি 'iPod' এবং 'broadcast' শব্দ দুটিকে জোড়া লাগিয়ে তৈরি এক বিখ্যাত পোর্টম্যান্টো বা জোড়কলম শব্দ।",
        "category": "Everyday & Social"
    },

    # 2. Poetic Metrics, Stanzas & Prosody
    {
        "word": "Caesura",
        "pos": "Noun",
        "phonetic": "/sɪˈzjʊər.ə/ (সিজুরা / জ্যোতিচ্ছেদ)",
        "meaningBn": "কবিতার চরণের মধ্যভাগে ছন্দ রক্ষার জন্য ব্যাকরণিক বা ভাবগত স্বাভাবিক বিরতি",
        "synonyms": ["Metrical pause", "Mid-line break", "Rhythmic pause"],
        "antonyms": ["Continuous enjambment", "Unbroken meter"],
        "exampleEn": "Alexander Pope masterfully employed caesuras to give balance to his rhyming couplets.",
        "exampleBn": "আলেকজান্ডার পোপ তাঁর মিত্রাক্ষর কবিতার চরণে ছন্দময় ভারসাম্য আনতে অত্যন্ত দক্ষতার সাথে সিজুরা বিরতি ব্যবহার করতেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Enjambment",
        "pos": "Noun",
        "phonetic": "/ɪnˈdʒæm.mənt/ (এনজ্যাম্বমেন্ট / প্রবহমান ছন্দ)",
        "meaningBn": "কবিতার এক চরণের ভাব বা বাক্য বিরতিহীনভাবে পরবর্তী চরণে প্রবাহিত হওয়ার কৌশল",
        "synonyms": ["Run-on line", "Syntactic spillover"],
        "antonyms": ["End-stopped line"],
        "exampleEn": "Modern free-verse poets frequently use enjambment to create momentum and unexpected tension.",
        "exampleBn": "আধুনিক মুক্তছন্দের কবিরা গতি সঞ্চার এবং অপ্রত্যাশিত নাটকীয় টান সৃষ্টির জন্য প্রায়শই প্রবহমান ছন্দ ব্যবহার করেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Spondee",
        "pos": "Noun",
        "phonetic": "/ˈspɒn.diː/ (স্পন্ডি)",
        "meaningBn": "কবিতার ছন্দে পরপর দুটি তীব্র বা দীর্ঘ শ্বাসাঘাতযুক্ত দল বা মাত্রার সমন্বয়ে গঠিত দলবৃত্ত চরণখণ্ড (--)",
        "synonyms": ["Double-accented foot", "Heavy poetic foot"],
        "antonyms": ["Pyrrhic (two unstressed syllables)"],
        "exampleEn": "The phrase 'heartbreak' is a textbook spondee with two equally stressed syllables.",
        "exampleBn": "'heartbreak' শব্দটি একটি আদর্শ স্পন্ডি যার উভয় অক্ষরের ওপরই সমান তীব্র শ্বাসাঘাত বা জোর পড়ে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Pyrrhic",
        "pos": "Noun",
        "phonetic": "/ˈpɪr.ɪk/ (পিরিক)",
        "meaningBn": "কবিতার ছন্দে পরপর দুটি শ্বাসাঘাতহীন বা দুর্বল দলের সমন্বয়ে গঠিত চরণখণ্ড (˘˘)",
        "synonyms": ["Unstressed metrical foot", "Dibrach"],
        "antonyms": ["Spondee"],
        "exampleEn": "Poets sometimes insert a pyrrhic foot to speed up the rhythmic tempo of an iambic line.",
        "exampleBn": "কবিরা মাঝে মাঝে চরণের ছন্দময় গতি দ্রুততর করার জন্য একটি শ্বাসাঘাতহীন পিরিক পর্ব যোগ করেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Dactyl",
        "pos": "Noun",
        "phonetic": "/ˈdæk.tɪl/ (ড্যাকটাইল)",
        "meaningBn": "একটি শ্বাসাঘাতযুক্ত দলের পর দুটি শ্বাসাঘাতহীন দলের সমন্বয়ে গঠিত ছন্দ পর্ব (- ˘ ˘)",
        "synonyms": ["Triple-syllable foot", "Falling meter foot"],
        "antonyms": ["Anapest (˘ ˘ -)"],
        "exampleEn": "Words like 'carefully' and 'poetry' follow the descending rhythmic pattern of a dactyl.",
        "exampleBn": "'carefully' এবং 'poetry' শব্দগুলো ড্যাকটাইলের ছন্দোবদ্ধ অবরোহী মাত্রা প্যাটার্ন অনুসরণ করে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Anapest",
        "pos": "Noun",
        "phonetic": "/ˈæn.əˌpɛst/ (অ্যানাপেস্ট)",
        "meaningBn": "দুটি দুর্বল বা শ্বাসাঘাতহীন দলের পর একটি তীব্র শ্বাসাঘাতযুক্ত দলের ছন্দ পর্ব (˘ ˘ -)",
        "synonyms": ["Rising metrical foot", "Anti-dactyl"],
        "antonyms": ["Dactyl"],
        "exampleEn": "Lord Byron's poem 'The Destruction of Sennacherib' gallops along in driving anapests.",
        "exampleBn": "লর্ড বায়রনের বিখ্যাত কবিতাটি দ্রুতগামী অ্যানাপেস্ট ছন্দের ঘোড়দৌড়ের মতো চাঞ্চল্যকর গতিতে এগিয়ে চলে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Trochee",
        "pos": "Noun",
        "phonetic": "/ˈtroʊ.kiː/ (ট্রোকি)",
        "meaningBn": "প্রথমে একটি শ্বাসাঘাতযুক্ত দল এবং পরে একটি শ্বাসাঘাতহীন দলের ছন্দ পর্ব (- ˘)",
        "synonyms": ["Choree", "Falling two-syllable foot"],
        "antonyms": ["Iamb (˘ -)"],
        "exampleEn": "Shakespeare's witches chant 'Double, double toil and trouble' in ominous trochees.",
        "exampleBn": "শেক্সপিয়রের জাদুকরীরা রহস্যময় ও ভয়ংকর ট্রোকি ছন্দে মন্ত্র উচ্চারণ করে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Scansion",
        "pos": "Noun",
        "phonetic": "/ˈskæn.ʃən/ (স্ক্যানশন / ছন্দোবিশ্লেষণ)",
        "meaningBn": "কবিতার চরণের মাত্রা, পর্ব ও শ্বাসাঘাত চিহ্নিত করে ছন্দ পরীক্ষা ও বিশ্লেষণ করার পদ্ধতি",
        "synonyms": ["Metrical analysis", "Verse rhythm parsing"],
        "antonyms": ["Prose reading"],
        "exampleEn": "Rigorous scansion revealed that Milton frequently varied his pentameter lines with inverted feet.",
        "exampleBn": "নিখুঁত ছন্দোবিশ্লেষণ বা স্ক্যানশনের মাধ্যমে দেখা যায় যে মিল্টন প্রায়ই তাঁর চরণে মাত্রার বৈচিত্র্য আনতেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Hemistich",
        "pos": "Noun",
        "phonetic": "/ˈhɛm.ɪ.stɪk/ (হেমেস্টিক / অর্ধচরণ)",
        "meaningBn": "বিরতি বা সিজুরা দ্বারা দ্বিখণ্ডিত কোনো পঙক্তি বা কাব্যচরণের ঠিক অর্ধাংশ",
        "synonyms": ["Half-line of verse", "Split verse segment"],
        "antonyms": ["Complete stanza"],
        "exampleEn": "Old English alliterative poetry structured each line as two balanced hemistichs joined by alliteration.",
        "exampleBn": "প্রাচীন ইংরেজি কবিতায় প্রতিটি চরণ অনুপ্রাস দ্বারা যুক্ত দুটি ভারসাম্যপূর্ণ অর্ধচরণে গঠিত হতো।",
        "category": "Literature & Arts"
    },
    {
        "word": "Villanelle",
        "pos": "Noun",
        "phonetic": "/ˌvɪl.əˈnɛl/ (ভিলানেল)",
        "meaningBn": "উনিশ চরণের নির্দিষ্ট রীতির ফরাসি কবিতা যাতে পাঁচটি তিন-চরণের স্তবক ও একটি চার-চরণের স্তবক থাকে",
        "synonyms": ["Nineteen-line fixed form", "Refrain poem"],
        "antonyms": ["Free verse poem"],
        "exampleEn": "Dylan Thomas's 'Do not go gentle into that good night' is the English language's most celebrated villanelle.",
        "exampleBn": "ডিলান থমাসের 'Do not go gentle into that good night' হলো ইংরেজি সাহিত্যের সবচেয়ে বিখ্যাত ভিলানেল কবিতা।",
        "category": "Literature & Arts"
    },
    {
        "word": "Rondeau",
        "pos": "Noun",
        "phonetic": "/ˈrɒn.doʊ/ (রন্দো)",
        "meaningBn": "তেরো বা পনেরো চরণের ফরাসি গীতিকবিতার রূপ যাতে মাত্র দুটি অন্ত্যমিল এবং একটি নির্দিষ্ট ধুয়াপদ থাকে",
        "synonyms": ["Fixed-form lyric poem", "Refrain-based French verse"],
        "antonyms": ["Blank verse epic"],
        "exampleEn": "The World War I memorial poem 'In Flanders Fields' is written in the traditional rondeau structure.",
        "exampleBn": "প্রথম বিশ্বযুদ্ধের স্মৃতিবিজড়িত 'In Flanders Fields' কবিতাটি ঐতিহ্যবাহী রন্দো কাঠামোতে রচিত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Triolet",
        "pos": "Noun",
        "phonetic": "/ˈtriː.əˌlɛt/ (ট্রিওলেট)",
        "meaningBn": "আট চরণের একটি ক্ষুদ্র কবিতা রূপ যেখানে প্রথম চরণটি চতুর্থ ও সপ্তম চরণে এবং দ্বিতীয় চরণটি অষ্টম চরণে পুনরাবৃত্ত হয়",
        "synonyms": ["Eight-line refrain verse", "French miniature stanza"],
        "antonyms": ["Epic canto"],
        "exampleEn": "Thomas Hardy crafted poignant triolets that expressed bittersweet nostalgia in tight musical rhymes.",
        "exampleBn": "টমাস হার্ডি আট চরণের আঁটসাঁট সুরেলা ট্রিওলেট কবিতার মাধ্যমে মর্মস্পর্শী স্মৃতিবেদনা ফুটিয়ে তুলেছিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Ballade",
        "pos": "Noun",
        "phonetic": "/bəˈlɑːd/ (ব্যালাড)",
        "meaningBn": "আট চরণের তিনটি স্তবক এবং শেষে চার চরণের একটি সমাপনী স্তবকযুক্ত (এনভয়) শাস্ত্রীয় ফরাসি কাব্যরূপ",
        "synonyms": ["Classical lyric form", "Chaucerian fixed verse"],
        "antonyms": ["Prose poem"],
        "exampleEn": "François Villon won enduring fame through masterly ballades dedicated to medieval rogues and fallen beauties.",
        "exampleBn": "ফ্রাঁসোয়া ভিলন মধ্যযুগীয় ভবঘুরে ও সুদর্শনাদের নিয়ে রচিত অনবদ্য ব্যালাড কাব্যের মাধ্যমে অমর খ্যাতি অর্জন করেছিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Alexandrine",
        "pos": "Noun",
        "phonetic": "/ˌæl.ɪɡˈzæn.draɪn/ (আলেকজান্দ্রাইন)",
        "meaningBn": "বারো মাত্রার বিশিষ্ট দ্বিপদী ছন্দ চরণ (প্রধানত ফরাসি ধ্রুপদী নাটকে বহুল ব্যবহৃত)",
        "synonyms": ["Twelve-syllable iambic line", "Hexameter line"],
        "antonyms": ["Trimeter line"],
        "exampleEn": "Molière composed Tartuffe in rhyming alexandrines, establishing a benchmark for French comedic drama.",
        "exampleBn": "মলিয়ের মিত্রাক্ষরযুক্ত বারো মাত্রার আলেকজান্দ্রাইন ছন্দে তাঁর বিখ্যাত নাটক তার্তুফ রচনা করেছিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Canto",
        "pos": "Noun",
        "phonetic": "/ˈkæn.toʊ/ (ক্যান্টো / সর্গ)",
        "meaningBn": "মহাকাব্য বা দীর্ঘ বর্ণনামূলক কবিতার অন্যতম প্রধান সর্গ বা অধ্যায়",
        "synonyms": ["Epic chapter", "Poetic division", "Book section"],
        "antonyms": ["Single line", "Stanza"],
        "exampleEn": "Dante's Divine Comedy is divided systematically into one hundred distinct cantos.",
        "exampleBn": "দান্তের 'ডিভাইন কমেডি' মহাকাব্যটি সুবিন্যস্তভাবে একশতটি স্বতন্ত্র সর্গ বা ক্যান্টোতে বিভক্ত।",
        "category": "Literature & Arts"
    },

    # 3. Classical Drama & Literary Theory
    {
        "word": "Catharsis",
        "pos": "Noun",
        "phonetic": "/kəˈθɑːr.sɪs/ (ক্যাথারসিস / ভাবমোচন)",
        "meaningBn": "করুণ নাটক দেখে দর্শক হৃদয়ে ভয় ও অনুকম্পার উদ্রেক ঘটিয়ে মনের যাবতীয় মালিন্য ও আবেগ মুক্তির পরিশুদ্ধি",
        "synonyms": ["Emotional purgation", "Spiritual cleansing", "Emotional release"],
        "antonyms": ["Emotional repression", "Inner turmoil"],
        "exampleEn": "Watching King Lear's tragic downfall brought an overwhelming sense of emotional catharsis to the audience.",
        "exampleBn": "কিং লিয়রের মর্মান্তিক পতন দেখে দর্শকদের হৃদয়ে এক তীব্র মানসিক প্রশান্তি ও ভাবমোচন বা ক্যাথারসিস জেগেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Hubris",
        "pos": "Noun",
        "phonetic": "/ˈhjuː.brɪs/ (হিউব্রিস / উদ্ধত অহংকার)",
        "meaningBn": "দেবতাদের উপেক্ষা করা বা ভাগ্যের বিরুদ্ধে অন্ধ অহংকার যা ট্র্যাজেডির নায়কের পতনের কারণ হয়",
        "synonyms": ["Overweening pride", "Fatal arrogance", "Presumption"],
        "antonyms": ["Humility", "Modesty"],
        "exampleEn": "In Greek tragedy, hubris inevitably invites divine retribution from the gods.",
        "exampleBn": "গ্রিক ট্র্যাজেডিতে অন্ধ ও উদ্ধত অহংকার বা হিউব্রিস অনিবার্যভাবেই দেবতাদের শাস্তি ডেকে আনে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Hamartia",
        "pos": "Noun",
        "phonetic": "/ˌhɑː.mɑːrˈtiː.ə/ (হ্যামারশিয়া / ট্র্যাজিক ভুল)",
        "meaningBn": "নায়কের চারিত্রিক কোনো প্রধান দুর্বলতা বা মারাত্মক বিচারিক ভুল যা তার চূড়ান্ত পতন ডেকে আনে",
        "synonyms": ["Tragic flaw", "Fatal error in judgment", "Critical blindspot"],
        "antonyms": ["Flawless wisdom", "Perfection"],
        "exampleEn": "Othello's crippling jealousy was the tragic hamartia that Iago exploited to ruin him.",
        "exampleBn": "ওথেলোর মারাত্মক অন্ধ সন্দেহপরায়ণতাই ছিল সেই আত্মঘাতী ভুল বা হ্যামারশিয়া যাকে কাজে লাগিয়ে ইয়াগো তাকে ধ্বংস করেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Peripeteia",
        "pos": "Noun",
        "phonetic": "/ˌpɛr.ɪ.pɪˈtiː.ə/ (পেরি পেতেয়া / ভাগ্যের অপ্রত্যাশিত মোড়)",
        "meaningBn": "নাটকের প্লটে পরিস্থিতির আকস্মিক ও সম্পূর্ণ বিপরীতমুখী পরিবর্তন (সৌভাগ্য থেকে দুর্ভাগ্যে রূপান্তর)",
        "synonyms": ["Reversal of fortune", "Sudden dramatic turning point"],
        "antonyms": ["Stagnation", "Predictable progression"],
        "exampleEn": "The peripeteia in Oedipus Rex occurs when the messenger arrives intending to ease Oedipus's fears but confirms his doom.",
        "exampleBn": "ইডিপাস নাটকে ভাগ্যের নাটকীয় মোড় আসে যখন দূত ভয় দূর করতে এসে উল্টো তার সর্বনাশের সত্য উন্মোচন করে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Anagnorisis",
        "pos": "Noun",
        "phonetic": "/ˌæn.əɡˈnɒr.ɪ.sɪs/ (অ্যানাগনরিসিস / সত্যের হঠাৎ উপলব্ধি)",
        "meaningBn": "নায়কের নিজের আসল পরিচয়, সত্য বা পরিস্থিতির ভয়াবহতা সম্পর্কে আকস্মিক ও চমকপ্রদ সত্য উদঘাটন",
        "synonyms": ["Dramatic recognition", "Moment of revelation", "Discovery of truth"],
        "antonyms": ["Blind ignorance", "Self-deception"],
        "exampleEn": "Oedipus experiences tragic anagnorisis when he finally realizes he has fulfilled the prophecy.",
        "exampleBn": "ইডিপাস চরম সত্যের মর্মঘাতী উপলব্ধি লাভ করে যখন সে বুঝতে পারে সে নিজেই নিজের দুর্ভাগ্য ডেকে এনেছে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Soliloquy",
        "pos": "Noun",
        "phonetic": "/səˈlɪl.ə.kwi/ (সলিলকি / স্বগতোক্তি)",
        "meaningBn": "মঞ্চে একা থাকা অবস্থায় চরিত্রের নিজের মনের গোপন ভাব ও চিন্তা উচ্চৈঃস্বরে প্রকাশ করার দীর্ঘ নাটকীয় বক্তব্য",
        "synonyms": ["Dramatic monologue to oneself", "Interior thoughts spoken aloud"],
        "antonyms": ["Dialogue", "Colloquy"],
        "exampleEn": "Hamlet's famous 'To be, or not to be' soliloquy reveals his profound existential struggle.",
        "exampleBn": "হ্যামলেটের বিখ্যাত 'To be, or not to be' স্বগতোক্তিটি তার গভীর আত্মিক ও অস্তিত্বের টানাপোড়েন তুলে ধরে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Strophe",
        "pos": "Noun",
        "phonetic": "/ˈstroʊ.fi/ (স্ট্রোফি)",
        "meaningBn": "প্রাচীন গ্রিক কোরাস দলের মঞ্চের এক পাশ থেকে অন্য পাশে নাচতে নাচতে গাওয়া গীতিকবিতার প্রথম স্তবক",
        "synonyms": ["Choral movement stanza", "First stanza of ode"],
        "antonyms": ["Antistrophe"],
        "exampleEn": "During the strophe, the Greek chorus chanted while turning gracefully from right to left.",
        "exampleBn": "স্ট্রোফি পরিবেশনার সময় গ্রিক কোরাস দল ডান থেকে বামে সুশৃঙ্খল ছন্দে আবর্তিত হয়ে গান গাইত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Antistrophe",
        "pos": "Noun",
        "phonetic": "/ænˈtɪs.trə.fi/ (অ্যান্টিস্ট্রোফি)",
        "meaningBn": "কোরাস দলের পূর্ববর্তী গতিপথের উল্টো দিকে ফিরে এসে গাওয়া প্রতি-স্তবক",
        "synonyms": ["Counter-movement stanza", "Response stanza of ode"],
        "antonyms": ["Strophe"],
        "exampleEn": "The antistrophe answered the philosophical claims made in the preceding strophe.",
        "exampleBn": "অ্যান্টিস্ট্রোফি অংশে পূর্ববর্তী স্তবকে উত্থাপিত দার্শনিক প্রশ্নের কাব্যিক প্রতিউত্তর দেওয়া হতো।",
        "category": "Literature & Arts"
    },
    {
        "word": "Epode",
        "pos": "Noun",
        "phonetic": "/ˈɛp.oʊd/ (ইপোড)",
        "meaningBn": "গ্রিক ওড বা স্তোত্রকাব্যে স্ট্রোফি ও অ্যান্টিস্ট্রোফির পর কোরাস দলের স্থির দাঁড়িয়ে গাওয়া তৃতীয় ও সমাপনী স্তবক",
        "synonyms": ["Concluding choral stanza", "After-song"],
        "antonyms": ["Opening strophe"],
        "exampleEn": "The chorus stood motionless at center stage while chanting the solemn closing epode.",
        "exampleBn": "মঞ্চের কেন্দ্রে কোরাস দল স্থির হয়ে দাঁড়িয়ে তাদের গম্ভীর সমাপনী ইপোড স্তবকটি আবৃত্তি করত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Bildungsroman",
        "pos": "Noun",
        "phonetic": "/ˈbɪl.dʊŋz.roʊˌmɑːn/ (বিল্ডুংসরোমান)",
        "meaningBn": "নায়ক বা নায়িকার শৈশব থেকে পূর্ণাঙ্গ মানসিক ও আত্মিক প্রাপ্তবয়স্ক হওয়ার ক্রমবিকাশ বর্ণনাকারী উপন্যাস",
        "synonyms": ["Coming-of-age novel", "Apprenticeship novel", "Formative narrative"],
        "antonyms": ["Static adventure tale"],
        "exampleEn": "Charlotte Brontë's Jane Eyre is a masterclass in the psychological realism of the Victorian bildungsroman.",
        "exampleBn": "শার্লট ব্রন্টের 'জেন আয়ার' ভিক্টোরীয় বিকাশধর্মী বিল্ডুংসরোমান উপন্যাসের এক অনন্য মাইলফলক।",
        "category": "Literature & Arts"
    },
    {
        "word": "Epistolary",
        "pos": "Adjective",
        "phonetic": "/ɪˈpɪs.təˌlɛr.i/ (এপিস্টোলারি / পত্রোপন্যাসসংক্রান্ত)",
        "meaningBn": "চিঠিপত্র, দিনলিপি বা ডায়েরির পাতার আকারে রচিত সাহিত্যকর্ম সংক্রান্ত",
        "synonyms": ["Letter-based narrative", "Diary-structured"],
        "antonyms": ["Third-person chronicle"],
        "exampleEn": "Bram Stoker's Dracula is famously told through an epistolary format of journals, letters, and telegraphs.",
        "exampleBn": "ব্রাম স্টোকারের 'ড্রাকুলা' দিনলিপি, চিঠি ও টেলিগ্রাফ সংবলিত বিখ্যাত পত্রোপন্যাস বা এপিস্টোলারি শৈলীতে বর্ণিত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Picaresque",
        "pos": "Adjective",
        "phonetic": "/ˌpɪk.əˈrɛsk/ (পিকারেস্ক)",
        "meaningBn": "কোনো ধূর্ত, চতুর কিন্তু আকর্ষণীয় ভবঘুরে চরিত্রের রোমাঞ্চকর ও বিশৃঙ্খল অভিযাত্রা বর্ণনাকারী উপন্যাস",
        "synonyms": ["Roguish narrative", "Episodic rogue tale", "Vagabond adventure"],
        "antonyms": ["Epic romance", "Moral treatise"],
        "exampleEn": "Cervantes's Don Quixote borrows heavily from early Spanish picaresque traditions.",
        "exampleBn": "সারভান্তেসের 'ডন কুইক্সোট' আদি স্প্যানিশ পিকারেস্ক বা ভবঘুরে অভিযাত্রার ঐতিহ্য থেকে ব্যাপকভাবে উপাদান গ্রহণ করেছে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Verisimilitude",
        "pos": "Noun",
        "phonetic": "/ˌvɛr.ɪ.sɪˈmɪl.ɪ.tjuːd/ (ভ্যারিসিমিলিটিউড / সত্যের প্রতিভাস)",
        "meaningBn": "কাল্পনিক বা নাটকীয় রচনায় বাস্তব জীবনের মতো জীবন্ত ও বিশ্বাসযোগ্য মনে হওয়ার গুণ",
        "synonyms": ["Likeness to truth", "Plausibility", "Realism", "Authenticity"],
        "antonyms": ["Implausibility", "Fancifulness"],
        "exampleEn": "Historical fiction relies on accurate period details to maintain convincing verisimilitude.",
        "exampleBn": "ঐতিহাসিক উপন্যাসকে পাঠকের চোখে বিশ্বাসযোগ্য ও জীবন্ত করে তুলতে নির্ভুল বাস্তবতার প্রতিভাস আবশ্যক।",
        "category": "Literature & Arts"
    }
]

print(f"Pack 68 raw items: {len(words_p68_raw)}")

# Check against existing and p67
clean_p68 = []
seen_p68 = set()
for item in words_p68_raw:
    w = item['word'].strip().lower()
    if w not in existing_words and w not in p67_words and w not in seen_p68:
        clean_p68.append(item)
        seen_p68.add(w)

print(f"Verified clean in Pack 68 base: {len(clean_p68)}")

# Write temporary pack68_base.json
with open('pack68_base.json', 'w', encoding='utf-8') as f:
    json.dump(clean_p68, f, ensure_ascii=False, indent=2)

# -*- coding: utf-8 -*-
import json

batch1_sub2 = [
    {
        "word": "Graphomaniac",
        "pos": "Noun",
        "phonetic": "/ˌɡræf.oʊˈmeɪ.ni.æk/ (গ্রাফোমেনিয়াক)",
        "meaningBn": "অদম্য ও বাতিকগ্রস্ত লেখার নেশায় আক্রান্ত ব্যক্তি",
        "synonyms": ["Compulsive writer", "Obsessive scribbler"],
        "antonyms": ["Reluctant writer", "Illiterate"],
        "exampleEn": "The eccentric author was a true graphomaniac who filled reams of paper every morning.",
        "exampleBn": "উদ্ভট লেখকটি ছিলেন বাতিকগ্রস্ত লেখার নেশায় আক্রান্ত যিনি রোজ সকালে স্তূপের পর স্তূপ কাগজ ভরিয়ে ফেলতেন।",
        "category": "One Word Substitution"
    },
    {
        "word": "Bibliomaniac",
        "pos": "Noun",
        "phonetic": "/ˌbɪb.li.oʊˈmeɪ.ni.æk/ (বিবলিওমেনিয়াক)",
        "meaningBn": "বই সংগ্রহের তীব্র বাতিকগ্রস্ত ব্যক্তি",
        "synonyms": ["Obsessive book collector", "Book hoarder"],
        "antonyms": ["Bibliophobe"],
        "exampleEn": "As a passionate bibliomaniac, his apartment was packed with towering stacks of rare first editions.",
        "exampleBn": "বই সংগ্রহের তীব্র বাতিকে আক্রান্ত হওয়ায় তাঁর অ্যাপার্টমেন্ট বিরল প্রথম সংস্করণের বইয়ের স্তূপে ভরা ছিল।",
        "category": "One Word Substitution"
    },
    {
        "word": "Ergophile",
        "pos": "Noun",
        "phonetic": "/ˈɜːr.ɡəˌfaɪl/ (আর্গোফাইল)",
        "meaningBn": "কঠোর পরিশ্রম ও কাজ ভালোবাসে এমন ব্যক্তি",
        "synonyms": ["Work lover", "Workaholic", "Industrious person"],
        "antonyms": ["Ergophobe", "Sluggard", "Idler"],
        "exampleEn": "An unyielding ergophile, he arrived first at the laboratory and left last every evening.",
        "exampleBn": "কাজের প্রতি গভীর অনুরক্ত ব্যক্তি হওয়ায় সে প্রতিদিন সকালে সবার আগে ল্যাবে আসত এবং রাতে সবার শেষে বের হতো।",
        "category": "One Word Substitution"
    },
    {
        "word": "Ergophobe",
        "pos": "Noun",
        "phonetic": "/ˈɜːr.ɡəˌfoʊb/ (আর্গোফোব)",
        "meaningBn": "কাজ ও শারীরিক পরিশ্রমকে ভয় বা চরম অপছন্দকারী অলস ব্যক্তি",
        "synonyms": ["Work avoider", "Shirker", "Sloth"],
        "antonyms": ["Ergophile", "Hard worker"],
        "exampleEn": "His family worried that his refusal to seek employment stemmed from being an incurable ergophobe.",
        "exampleBn": "তার পরিবার শঙ্কিত ছিল যে চাকরি খোঁজার অনিহাটি আসলে তার শ্রমবিমুখ অলস মানসিকতার ফসল।",
        "category": "One Word Substitution"
    },
    {
        "word": "Neophile",
        "pos": "Noun",
        "phonetic": "/ˈniː.oʊˌfaɪl/ (নিওফাইল)",
        "meaningBn": "নতুনত্ব, আধুনিক গ্যাজেট বা সাম্প্রতিক পরিবর্তন ভালোবাসে এমন ব্যক্তি",
        "synonyms": ["Novelty lover", "Early adopter", "Innovator"],
        "antonyms": ["Neophobe", "Traditionalist", "Luddite"],
        "exampleEn": "Tech companies market their most experimental products directly to enthusiastic neophiles.",
        "exampleBn": "প্রযুক্তি প্রতিষ্ঠানগুলো তাদের নিত্যনতুন নিরীক্ষামূলক পণ্যগুলো নতুনত্বপ্রেমী গ্রাহকদের উদ্দেশ্যেই বাজারে ছাড়ে।",
        "category": "One Word Substitution"
    },
    {
        "word": "Neophobe",
        "pos": "Noun",
        "phonetic": "/ˈniː.oʊˌfoʊb/ (নিওফোব)",
        "meaningBn": "নতুন বিষয়, পরিবর্তন বা উদ্ভাবনকে ভয় ও অবিশ্বাস করে এমন রক্ষণশীল ব্যক্তি",
        "synonyms": ["Resister of change", "Traditionalist", "Misoneist"],
        "antonyms": ["Neophile", "Progressive"],
        "exampleEn": "The old clerk was a staunch neophobe who vehemently resisted switching to computer software.",
        "exampleBn": "বয়োজ্যেষ্ঠ কেরানিটি ছিলেন চরম পরিবর্তনবিমুখ যিনি কম্পিউটার সফটওয়্যার ব্যবহারে তীব্র আপত্তি জানিয়েছিলেন।",
        "category": "One Word Substitution"
    },
    {
        "word": "Xenophile",
        "pos": "Noun",
        "phonetic": "/ˈzɛn.əˌfaɪl/ (জেনোফাইল)",
        "meaningBn": "বিদেশি সংস্কৃতি, রীতিনীতি বা বিদেশি মানুষ ভালোবাসে এমন ব্যক্তি",
        "synonyms": ["Foreign-culture lover", "Cosmopolitan"],
        "antonyms": ["Xenophobe"],
        "exampleEn": "A lifelong xenophile, she mastered four Asian languages and collected folk art from around the globe.",
        "exampleBn": "আজীবন ভিনদেশি সংস্কৃতিপ্রেমী হওয়ায় সে চারটি এশীয় ভাষা রপ্ত করেছিল এবং বিশ্বজুড়ে লোকশিল্প সংগ্রহ করেছিল।",
        "category": "One Word Substitution"
    },
    {
        "word": "Claustrophobe",
        "pos": "Noun",
        "phonetic": "/ˈklɒs.trəˌfoʊb/ (ক্লস্ট্রোফোব)",
        "meaningBn": "আবদ্ধ বা সংকীর্ণ স্থানে অবস্থানের ভয় বা আতঙ্কগ্রস্ত ব্যক্তি",
        "synonyms": ["Confined-space phobic"],
        "antonyms": ["Agoraphobe"],
        "exampleEn": "Being a claustrophobe, she chose to climb ten flights of stairs rather than enter the elevator.",
        "exampleBn": "আবদ্ধ স্থানে আতঙ্কগ্রস্ত হওয়ার কারণে সে লিফটে না চড়ে দশ তলার সিঁড়ি ভেঙে ওপরে উঠল।",
        "category": "One Word Substitution"
    },
    {
        "word": "Agoraphobe",
        "pos": "Noun",
        "phonetic": "/ˌæɡ.ər.əˈfoʊb/ (অ্যাগোরাফোব)",
        "meaningBn": "উন্মুক্ত প্রান্তর বা জনাকীর্ণ জনসমাগমস্থলে যেতে আতঙ্কগ্রস্ত ব্যক্তি",
        "synonyms": ["Crowd-phobic", "Open-space phobic"],
        "antonyms": ["Claustrophobe"],
        "exampleEn": "The agoraphobe felt safest inside his quiet home and rarely stepped out into public plazas.",
        "exampleBn": "উন্মুক্ত স্থানের ভীতিতে ভোগা ব্যক্তিটি নিজের শান্ত ঘরেই নিরাপদ বোধ করত এবং জনসমাগমের স্থানে কদাচিৎ যেত।",
        "category": "One Word Substitution"
    },
    {
        "word": "Monophobe",
        "pos": "Noun",
        "phonetic": "/ˈmɒn.əˌfoʊb/ (মনোফোব)",
        "meaningBn": "একাকিত্ব বা একা থাকাকে চরম ভয় পায় এমন ব্যক্তি",
        "synonyms": ["Solitude-fearing person", "Autophobe"],
        "antonyms": ["Hermit", "Recluse", "Solitary"],
        "exampleEn": "As a monophobe, he kept the television running constantly just to hear background chatter.",
        "exampleBn": "একাকিত্বের আতঙ্কে আক্রান্ত হওয়ায় সামান্য মানুষের গলার আওয়াজ পাওয়ার জন্য সে সারাক্ষণ টেলিভিশন চালিয়ে রাখত।",
        "category": "One Word Substitution"
    },
    {
        "word": "Peccadillo",
        "pos": "Noun",
        "phonetic": "/ˌpɛk.əˈdɪl.oʊ/ (পেক্যাডিলো)",
        "meaningBn": "সামান্য বা ক্ষমার যোগ্য লঘু পাপ বা স্খলন",
        "synonyms": ["Minor offense", "Petty fault", "Trifling slip"],
        "antonyms": ["Heinous crime", "Mortal sin", "Felony"],
        "exampleEn": "Arriving five minutes late was treated as a harmless peccadillo rather than gross misconduct.",
        "exampleBn": "পাঁচ মিনিট দেরিতে আসাকে গুরুতর অসদাচরণ না ভেবে সামান্য ক্ষমার যোগ্য ত্রুটি হিসেবে গণ্য করা হয়েছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Foible",
        "pos": "Noun",
        "phonetic": "/ˈfɔɪ.bəl/ (ফয়বল)",
        "meaningBn": "ব্যক্তিত্বের সামান্য চারিত্রিক দুর্বলতা বা খামখেয়ালিপনা",
        "synonyms": ["Quirk", "Minor weakness", "Failing"],
        "antonyms": ["Forte", "Great strength", "Virtue"],
        "exampleEn": "Her friends affectionately tolerated her foible of obsessively alphabetizing every spice jar.",
        "exampleBn": "প্রতিটি মশলার কৌটা বর্ণানুক্রম অনুযায়ী সাজিয়ে রাখার তার খামখেয়ালিপনা বন্ধুরা স্নেহের চোখে মেনে নিত।",
        "category": "BCS & Bank"
    },
    {
        "word": "Crotchet",
        "pos": "Noun",
        "phonetic": "/ˈkrɒtʃ.ɪt/ (ক্রোচেট)",
        "meaningBn": "উদ্ভট বা খাপছাড়া ব্যক্তিগত খেয়াল; জেদ",
        "synonyms": ["Whim", "Caprice", "Eccentric notion"],
        "antonyms": ["Reasoned plan", "Sound judgment"],
        "exampleEn": "He had a bizarre crotchet that meetings must strictly begin at seven minutes past the hour.",
        "exampleBn": "তার এক অদ্ভুত জেদ ছিল যে মিটিং ঠিক সাত মিনিট দেরিতে শুরু হতে হবে।",
        "category": "BCS & Bank"
    },
    {
        "word": "Vagary",
        "pos": "Noun",
        "phonetic": "/vəˈɡɛər.i/ (ভেগারি)",
        "meaningBn": "অপ্রত্যাশিত ও খামখেয়ালি পরিবর্তন; অদৃষ্টের অনিশ্চিত খেয়াল",
        "synonyms": ["Caprice", "Whim", "Erratic shift", "Fluctuation"],
        "antonyms": ["Predictability", "Steadfastness", "Constancy"],
        "exampleEn": "Farmers must constantly adapt to the seasonal vagaries of unpredictable weather patterns.",
        "exampleBn": "আবহাওয়ার অনিশ্চিত খামখেয়ালিপনার সাথে কৃষকদের প্রতিনিয়ত মানিয়ে নিতে হয়।",
        "category": "BCS & Bank"
    },
    {
        "word": "Caprice",
        "pos": "Noun",
        "phonetic": "/kəˈpriːs/ (ক্যাপ্রিস)",
        "meaningBn": "অযৌক্তিক হঠাৎ খেয়াল; মর্জিমাফিক আচরণ",
        "synonyms": ["Whim", "Impulse", "Fickleness", "Vagary"],
        "antonyms": ["Steadfastness", "Deliberation", "Constancy"],
        "exampleEn": "The tyrant ruled by personal caprice rather than through established statutory law.",
        "exampleBn": "স্বৈরশাসক বিধিবদ্ধ আইনের পরিবর্তে নিজের মর্জিমাফিক খেয়ালখুশি দিয়ে দেশ শাসন করত।",
        "category": "BCS & Bank"
    },
    {
        "word": "Ataraxia",
        "pos": "Noun",
        "phonetic": "/ˌæt.əˈræk.si.ə/ (অ্যাটারেক্সিয়া)",
        "meaningBn": "মানসিক প্রশান্তি; উদ্বেগহীন অবিচল স্থৈর্য",
        "synonyms": ["Equanimity", "Serenity", "Imperturbability"],
        "antonyms": ["Agitation", "Turbulence", "Anxiety"],
        "exampleEn": "Epicurean philosophers sought ataraxia through moderating desire and contemplating nature.",
        "exampleBn": "এপিকিউরিয়ান দার্শনিকরা কামনা নিয়ন্ত্রণ ও প্রকৃতির ধ্যানের মাধ্যমে মানসিক পরম প্রশান্তি খুঁজতেন।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Apatheia",
        "pos": "Noun",
        "phonetic": "/ˌæp.əˈθaɪ.ə/ (অ্যাপাথিয়া)",
        "meaningBn": "স্টোয়িক দর্শনে মোহ ও সংবেদনশীল আবেগমুক্ত মানসিক ভারসাম্য",
        "synonyms": ["Stoic calm", "Freedom from suffering", "Detachment"],
        "antonyms": ["Passion", "Emotional turmoil"],
        "exampleEn": "In Stoic doctrine, apatheia does not mean indifference, but mastery over disruptive passions.",
        "exampleBn": "স্টোয়িক নীতিতে আবেগমুক্ততা মানে নিস্পৃহতা নয়, বরং বিধ্বংসী আবেগের ওপর পূর্ণ আত্মনিয়ন্ত্রণ।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Eudaimonia",
        "pos": "Noun",
        "phonetic": "/juː.daɪˈmoʊ.ni.ə/ (ইউডাইমোনিয়া)",
        "meaningBn": "নৈতিক গুণ ও সার্বিক উৎকর্ষময় পরম মানবকল্যাণ ও আত্মতুষ্টি",
        "synonyms": ["Human flourishing", "Highest good", "Supreme well-being"],
        "antonyms": ["Misery", "Depravity", "Despair"],
        "exampleEn": "Aristotle posited that eudaimonia is achieved through virtuous living and rational action.",
        "exampleBn": "অ্যারিস্টটল দাবি করেছিলেন যে সদ্গুণময় জীবন ও যুক্তিবাদী কাজের মাধ্যমেই পরম মানবকল্যাণ ও প্রস্ফুটন সম্ভব।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Anomie",
        "pos": "Noun",
        "phonetic": "/ˈæn.ə.mi/ (অ্যানোমি)",
        "meaningBn": "সামাজিক নীতিহীনতা; নৈতিক মানদণ্ড ও শৃঙ্খলার অবক্ষয়",
        "synonyms": ["Normlessness", "Social breakdown", "Alienation"],
        "antonyms": ["Social cohesion", "Moral order", "Solidarity"],
        "exampleEn": "Rapid industrialization without institutional support threw the rural populace into acute anomie.",
        "exampleBn": "প্রাতিষ্ঠানিক সহযোগিতা ছাড়া দ্রুত শিল্পায়ন গ্রামীণ জনগোষ্ঠীকে তীব্র সামাজিক নীতিহীনতা ও বিচ্ছিন্নতায় ফেলে দিয়েছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Weltschmerz",
        "pos": "Noun",
        "phonetic": "/ˈvɛlt.ʃmɛərts/ (ভেল্টশমার্জ)",
        "meaningBn": "জগতের বাস্তবতা ও আদর্শের মধ্যকার পার্থক্যে সৃষ্ট বৈশ্বিক বিষাদ",
        "synonyms": ["World-weariness", "Philosophical sadness"],
        "antonyms": ["Optimism", "Exuberance", "Zest"],
        "exampleEn": "The poet's early verses reflected deep Weltschmerz following the devastation of the war.",
        "exampleBn": "যুদ্ধের ধ্বংসযজ্ঞের পর কবির প্রথম দিকের কবিতাগুলোতে গভীর বৈশ্বিক বিষাদের ছায়া প্রতিফলিত হয়েছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Sehnsucht",
        "pos": "Noun",
        "phonetic": "/ˈzeɪnˌzʊxt/ (জেনজুক্ট)",
        "meaningBn": "অধরা বা অপূর্ণ কোনো কিছুর প্রতি অবর্ণনীয় আকুল হাহাকার",
        "synonyms": ["Inconsolable yearning", "Yearning", "Pining"],
        "antonyms": ["Contentment", "Fulfillment"],
        "exampleEn": "Listening to the distant violin melody stirred a mysterious Sehnsucht in her chest.",
        "exampleBn": "দূর থেকে ভেসে আসা বেহালার সুর তার বুকে এক অপার অপূর্ণ আকাঙ্ক্ষার হাহাকার জাগিয়ে তুলল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Saudade",
        "pos": "Noun",
        "phonetic": "/saʊˈdɑː.də/ (সাউডাদ)",
        "meaningBn": "হারিয়ে যাওয়া মানুষ বা অতীতের প্রতি গভীর নস্টালজিক বিষাদ ও ভালোবাসা",
        "synonyms": ["Bittersweet longing", "Nostalgic melancholy"],
        "antonyms": ["Indifference", "Emotional numbness"],
        "exampleEn": "The melancholy folk songs of Lisbon capture the quintessence of saudade.",
        "exampleBn": "লিসবনের বিষাদময় লোকগানগুলোতে হারিয়ে যাওয়া স্মৃতির প্রতি গভীর আবেগময় বেদনার পূর্ণ প্রতিফলন মেলে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Akrasia",
        "pos": "Noun",
        "phonetic": "/əˈkreɪ.zi.ə/ (অ্যাক্রেসিয়া)",
        "meaningBn": "সঠিক জ্ঞান থাকা সত্ত্বেও দুর্বল ইচ্ছাশক্তির কারণে ভুল কাজ করা",
        "synonyms": ["Weakness of will", "Lack of self-control", "Incontinence"],
        "antonyms": ["Self-mastery", "Iron willpower", "Temperance"],
        "exampleEn": "Knowing he had an exam at dawn, checking social media until 3 AM was pure akrasia.",
        "exampleBn": "সকালে পরীক্ষা জানা সত্ত্বেও রাত তিনটা পর্যন্ত ফোনে চোখ রাখা ছিল ইচ্ছাশক্তির দুর্বলতা বা অ্যাক্রেসিয়ার ফল।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Anagnorisis",
        "pos": "Noun",
        "phonetic": "/ˌæn.əɡˈnɒr.ɪ.sɪs/ (অ্যানাগনরিসিস)",
        "meaningBn": "নাটক বা সাহিত্যে চরিত্রের আত্মোপলব্ধি বা সত্য উন্মোচনের মুহূর্ত",
        "synonyms": ["Moment of recognition", "Critical discovery", "Revelation"],
        "antonyms": ["Blind ignorance", "Unawareness"],
        "exampleEn": "Oedipus experiences devastating anagnorisis when he finally discovers the truth of his birth.",
        "exampleBn": "নিজের জন্মের আসল সত্য জানার পর ইডিপাস এক মর্মন্তুদ আত্মোপলব্ধির মুখোমুখি হয়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Peripeteia",
        "pos": "Noun",
        "phonetic": "/ˌpɛr.ɪ.pɪˈtiː.ə/ (প্যারিপেতিয়া)",
        "meaningBn": "নাটকে বা জীবনে ভাগ্যের আকস্মিক ও নাটকীয় বিপর্যয়",
        "synonyms": ["Sudden reversal", "Turning point", "Cataclysmic pivot"],
        "antonyms": ["Stable continuum", "Predictable progression"],
        "exampleEn": "The bankruptcy of the conglomerate marked an abrupt peripeteia in the CEO's lavish life.",
        "exampleBn": "ব্যবসা প্রতিষ্ঠানের দেউলিয়াত্ব প্রধান নির্বাহীর বিলাসবহুল জীবনে এক আকস্মিক ভাগ্য বিপর্যয় ডেকে এনেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Quietism",
        "pos": "Noun",
        "phonetic": "/ˈkwaɪ.ə.tɪz.əm/ (কোয়ায়েটিজম)",
        "meaningBn": "লৌকিক কর্মপ্রচেষ্টা বর্জন করে নিষ্ক্রিয় আধ্যাত্মিক আত্মসমর্পণ",
        "synonyms": ["Passive contemplation", "Mystical withdrawal", "Inaction"],
        "antonyms": ["Activism", "Militancy", "Proactive engagement"],
        "exampleEn": "Critics complained that spiritual quietism discouraged social reform and practical charity.",
        "exampleBn": "সমালোচকরা বলতেন যে নিষ্ক্রিয় আধ্যাত্মিকতাবাদ সামাজিক সংস্কার ও কার্যকর সেবামূলক কাজে প্রতিবন্ধকতা তৈরি করে।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Fideism",
        "pos": "Noun",
        "phonetic": "/ˈfaɪ.diː.ɪz.əm/ (ফিডিইজম)",
        "meaningBn": "বুদ্ধি বা বৈজ্ঞানিক প্রমাণের চেয়ে অন্ধ বিশ্বাসের প্রাধান্য দেওয়ার মতবাদ",
        "synonyms": ["Faith-based belief", "Reliance on revelation"],
        "antonyms": ["Rationalism", "Empiricism", "Skepticism"],
        "exampleEn": "Fideism holds that religious truths transcend human rationality and can only be embraced by faith.",
        "exampleBn": "ফিডিইজম বিশ্বাস করে যে ধর্মীয় সত্য মানুষের বুদ্ধির অতীত এবং তা কেবল অবিচল বিশ্বাসেই ধারণ সম্ভব।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Nominalism",
        "pos": "Noun",
        "phonetic": "/ˈnɒm.ɪ.nə.lɪz.əm/ (নমিনালিজম)",
        "meaningBn": "দার্শনিক মতবাদ যাতে সার্বিক ধারণা কেবল নামমাত্র, বাস্তব বস্তু নয়",
        "synonyms": ["Concept anti-realism", "Particularism"],
        "antonyms": ["Philosophical realism", "Platonism"],
        "exampleEn": "Medieval nominalism denied that universal essences existed independently of individual objects.",
        "exampleBn": "মধ্যযুগীয় নামবাদ মতবাদটি অস্বীকার করেছিল যে স্বতন্ত্র বস্তুর বাইরে সার্বিক ভাবমূর্তির কোনো বাস্তব অস্তিত্ব আছে।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Eristic",
        "pos": "Adjective",
        "phonetic": "/ɪˈrɪs.tɪk/ (ইরিসটিক)",
        "meaningBn": "সত্য অনুসন্ধানের চেয়ে কেবল তর্কে জেতার জন্য চালানো কুতর্কসংক্রান্ত",
        "synonyms": ["Disputatious", "Contentious", "Combative"],
        "antonyms": ["Irenic", "Collaborative", "Truth-seeking"],
        "exampleEn": "The debate devolved into an eristic shouting match devoid of meaningful substance.",
        "exampleBn": "বিতর্কটি অর্থপূর্ণ আলোচনার বদলে কেবল জেতার লক্ষ্যে কুতর্কপূর্ণ চিৎকার-চেঁচামেচিতে পর্যবসিত হলো।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Paralogism",
        "pos": "Noun",
        "phonetic": "/pəˈræl.əˌdʒɪz.əm/ (প্যারালজিজম)",
        "meaningBn": "অজ্ঞাতসারে ভুল যুক্তির অবতারণা; অসচেতন অনুপপত্তি",
        "synonyms": ["Unintentional fallacy", "Faulty deduction"],
        "antonyms": ["Sound argument", "Valid syllogism"],
        "exampleEn": "Kant analyzed paralogisms to expose the natural fallacies of speculative metaphysics.",
        "exampleBn": "কান্ট অধিবিদ্যার স্বাভাবিক অসচেতন যুক্তিভ্রান্তিগুলো ফাঁস করতে এই অনুপপত্তির বিশ্লেষণ করেছিলেন।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Enthymeme",
        "pos": "Noun",
        "phonetic": "/ˈɛn.θɪˌmiːm/ (এনথিমিম)",
        "meaningBn": "এমন এক ধরনের যুক্তি যেখানে একটি আশ্রয়বাক্য অনুক্ত বা উহ্য রাখা হয়",
        "synonyms": ["Truncated syllogism", "Implicit argument"],
        "antonyms": ["Explicit syllogism"],
        "exampleEn": "'He is mortal because he is human' is an enthymeme omitting 'All humans are mortal'.",
        "exampleBn": "'সে মানুষ তাই সে মরণশীল' হলো উহ্য আশ্রয়বাক্যবিশিষ্ট যুক্তি যেখানে 'সব মানুষ মরণশীল' উহ্য রয়েছে।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Sorites",
        "pos": "Noun",
        "phonetic": "/soʊˈraɪ.tiːz/ (সোরাইটিজ)",
        "meaningBn": "ধারাবাহিক একাধিক আশ্রয়বাক্যের শৃঙ্খলযুক্ত জটিল অনুমান বা হেত্বাভাস",
        "synonyms": ["Chain argument", "Polysyllogism", "Heap paradox"],
        "antonyms": ["Simple inference"],
        "exampleEn": "The sorites paradox demonstrates how vague predicates like 'heap' challenge binary logic.",
        "exampleBn": "সোরাইটিজ হেত্বাভাস প্রমাণ করে যে 'স্তূপ'-এর মতো অস্পষ্ট পদ কীভাবে দ্বৈত যৌক্তিকতাকে চ্যালেঞ্জ করে।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Antinomy",
        "pos": "Noun",
        "phonetic": "/ænˈtɪn.ə.mi/ (অ্যান্টিনোমি)",
        "meaningBn": "দুটি সমান যুক্তিগ্রাহ্য আইনের বা অনুমানের মধ্যকার মৌলিক বিরোধ",
        "synonyms": ["Paradox", "Contradiction between laws", "Incompatibility"],
        "antonyms": ["Harmonious agreement", "Consistency"],
        "exampleEn": "Kant identified four antinomies of pure reason where thesis and antithesis seemed equally valid.",
        "exampleBn": "কান্ট বিশুদ্ধ বুদ্ধির চারটি এমন মৌলিক বিরোধ চিহ্নিত করেন যেখানে পক্ষ ও বিপক্ষ উভয় যুক্তিই সমান কার্যকর দেখায়।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Disquisition",
        "pos": "Noun",
        "phonetic": "/ˌdɪs.kwɪˈzɪʃ.ən/ (ডিসকুইজিশন)",
        "meaningBn": "কোনো বিষয়ের ওপর পুঙ্খানুপুঙ্খ ও পাণ্ডিত্যপূর্ণ দীর্ঘ সন্দর্ভ",
        "synonyms": ["Treatise", "Exposition", "Dissertation", "Monograph"],
        "antonyms": ["Cursory sketch", "Brief note"],
        "exampleEn": "The professor published a lengthy disquisition on international maritime law.",
        "exampleBn": "অধ্যাপক আন্তর্জাতিক সমুদ্র আইনের ওপর একটি বিশদ ও পাণ্ডিত্যপূর্ণ সন্দর্ভ প্রকাশ করেন।",
        "category": "Academic & Scholarly"
    },
    {
        "word": "Tractate",
        "pos": "Noun",
        "phonetic": "/ˈtræk.teɪt/ (ট্র্যাকটেট)",
        "meaningBn": "বিশেষ বিষয়ের ওপর প্রণীত নিয়মতান্ত্রিক ছোট প্রবন্ধ বা পুস্তিকা",
        "synonyms": ["Treatise", "Tract", "Monograph", "Essay"],
        "antonyms": ["Anthology", "Omnibus"],
        "exampleEn": "Spinoza penned a profound theological-political tractate defending freedom of thought.",
        "exampleBn": "স্পিনোজা চিন্তার স্বাধীনতা রক্ষা করে একটি গভীর ধর্মতাত্ত্বিক-রাজনৈতিক পুস্তিকা রচনা করেছিলেন।",
        "category": "Academic & Scholarly"
    },
    {
        "word": "Papyrology",
        "pos": "Noun",
        "phonetic": "/ˌpæp.ɪˈrɒl.ə.dʒi/ (প্যাপাইরোলজি)",
        "meaningBn": "প্রাচীন প্যাপিরাস নথিপত্র পাঠ ও গবেষণাসংক্রান্ত শাস্ত্র",
        "synonyms": ["Study of ancient papyri", "Manuscript analysis"],
        "antonyms": ["Modern printing studies"],
        "exampleEn": "Discoveries in papyrology have revealed lost Greek tragedies preserved in Egyptian sands.",
        "exampleBn": "প্যাপিরাস শাস্ত্রের আবিষ্কার মিসরের বালুর নিচে সংরক্ষিত অনেক হারিয়ে যাওয়া গ্রিক ট্র্যাজেডি উন্মোচন করেছে।",
        "category": "Academic & Scholarly"
    },
    {
        "word": "Codicology",
        "pos": "Noun",
        "phonetic": "/ˌkoʊ.dɪˈkɒl.ə.dʒi/ (কোডিকোলজি)",
        "meaningBn": "হস্তলিখিত প্রাচীন পাণ্ডুলিপি ও পুথি প্রস্তুতের ইতিহাসের বিজ্ঞান",
        "synonyms": ["Manuscript archaeology", "Book material history"],
        "antonyms": ["Digital bibliography"],
        "exampleEn": "Through codicology, historians identified the animal skins used to bind the ancient gospel.",
        "exampleBn": "পাণ্ডুলিপিবিদ্যার মাধ্যমে ইতিহাসবিদরা প্রাচীন ধর্মগ্রন্থ বাঁধানোর কাজে ব্যবহৃত পশুর চামড়া শনাক্ত করেছিলেন।",
        "category": "Academic & Scholarly"
    },
    {
        "word": "Sigillography",
        "pos": "Noun",
        "phonetic": "/ˌsɪdʒ.ɪˈlɒɡ.rə.fi/ (সিজিলোগ্রাফি)",
        "meaningBn": "প্রাচীন রাজকীয় ও দাপ্তরিক সিলমোহরসংক্রান্ত বিদ্যা",
        "synonyms": ["Sphragistics", "Study of seals"],
        "antonyms": ["Digital signature"],
        "exampleEn": "The royal charter was authenticated through forensic sigillography of the wax seal.",
        "exampleBn": "মোমের তৈরি সিলমোহরের বৈজ্ঞানিক পরীক্ষার মাধ্যমে রাজকীয় সনদটির সত্যতা নিশ্চিত করা হয়েছিল।",
        "category": "Academic & Scholarly"
    },
    {
        "word": "Chorography",
        "pos": "Noun",
        "phonetic": "/kəˈrɒɡ.rə.fi/ (কোরোগ্রাফি)",
        "meaningBn": "কোনো নির্দিষ্ট অঞ্চল বা স্থানের পুঙ্খানুপুঙ্খ বিবরণ ও মানচিত্রাঙ্কন",
        "synonyms": ["Regional description", "Local topography"],
        "antonyms": ["Cosmography", "General geography"],
        "exampleEn": "Renaissance scholars produced exquisite chorographies depicting every village in the county.",
        "exampleBn": "রেনেসাঁর পণ্ডিতরা কাউন্টির প্রতিটি গ্রাম চিত্রিত করে চমৎকার আঞ্চলিক বিবরণ ও মানচিত্র তৈরি করতেন।",
        "category": "Academic & Scholarly"
    },
    {
        "word": "Dendrochronology",
        "pos": "Noun",
        "phonetic": "/ˌdɛn.droʊ.krəˈnɒl.ə.dʒi/ (ডেনড্রোক্রোনোলজি)",
        "meaningBn": "গাছের গুঁড়ির বার্ষিক বলয় গুনে বয়স নির্ধারণের বিজ্ঞান",
        "synonyms": ["Tree-ring dating", "Botanical chronometry"],
        "antonyms": ["Radiocarbon dating (generic)"],
        "exampleEn": "Using dendrochronology, archaeologists determined the exact year the ancient wooden longhouse was built.",
        "exampleBn": "গাছের গুঁড়ির বলয় পরিমাপ করে প্রত্নতাত্ত্বিকরা কাঠের প্রাচীন গৃহটি নির্মাণের সঠিক বছর নির্ধারণ করেছিলেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Stratigraphy",
        "pos": "Noun",
        "phonetic": "/strəˈtɪɡ.rə.fi/ (স্ট্র্যাটিগ্রাফি)",
        "meaningBn": "ভূগর্ভস্থ শিলা ও মাটির স্তরীভবন বিদ্যা",
        "synonyms": ["Geological layering", "Rock-strata study"],
        "antonyms": ["Surface geology"],
        "exampleEn": "Archaeological stratigraphy proves that the Roman ruins lie beneath the medieval marketplace.",
        "exampleBn": "মাটির স্তরীভবন বিদ্যা প্রমাণ করে যে রোমান ধ্বংসাবশেষ মধ্যযুগীয় বাজারের নিচে অবস্থিত।",
        "category": "Science & Environment"
    },
    {
        "word": "Speleology",
        "pos": "Noun",
        "phonetic": "/ˌspiː.liˈɒl.ə.dʒi/ (স্পিলিওলজি)",
        "meaningBn": "প্রাকৃতিক গুহা অন্বেষণ ও বৈজ্ঞানিক গবেষণা",
        "synonyms": ["Caving science", "Cave exploration"],
        "antonyms": ["Mountaineering"],
        "exampleEn": "Advances in speleology led to the discovery of subterranean rivers deep beneath the limestone plateau.",
        "exampleBn": "গুহাবিদ্যার অগ্রগতির ফলে চুনাপাথর মালভূমির গভীরে ভূগর্ভস্থ নদীর সন্ধান পাওয়া গেছে।",
        "category": "Science & Environment"
    },
    {
        "word": "Limnology",
        "pos": "Noun",
        "phonetic": "/lɪmˈnɒl.ə.dʒi/ (লিম্নোলজি)",
        "meaningBn": "হ্রদ, পুকুর ও মিঠা পানির জলাশয়সংক্রান্ত জীব ও ভৌত বিজ্ঞান",
        "synonyms": ["Freshwater ecology", "Inland waters study"],
        "antonyms": ["Oceanography", "Marine biology"],
        "exampleEn": "The university opened a new limnology lab to monitor pollution levels in regional lakes.",
        "exampleBn": "আঞ্চলিক হ্রদগুলোতে দূষণের মাত্রা পর্যবেক্ষণ করতে বিশ্ববিদ্যালয় একটি নতুন মিঠা পানি গবেষণা ল্যাব চালু করেছে।",
        "category": "Science & Environment"
    },
    {
        "word": "Glaciology",
        "pos": "Noun",
        "phonetic": "/ˌɡlæs.iˈɒl.ə.dʒi/ (গ্ল্যাসিওলজি)",
        "meaningBn": "হিমবাহ ও বরফের প্রকৃতিসংক্রান্ত বিজ্ঞান",
        "synonyms": ["Study of glaciers", "Ice-sheet science"],
        "antonyms": ["Volcanology"],
        "exampleEn": "Experts in glaciology warned of accelerating melt rates in the Himalayan ice caps.",
        "exampleBn": "হিমবাহ বিশেষজ্ঞরা হিমালয়ের বরফের চাদর গলে যাওয়ার ক্রমবর্ধমান হার নিয়ে সতর্কবার্তা দিয়েছেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Pteridology",
        "pos": "Noun",
        "phonetic": "/ˌtɛr.ɪˈdɒl.ə.dʒi/ (টেরিডোলজি)",
        "meaningBn": "ফার্ন ও ফার্নজাতীয় উদ্ভিদের বৈজ্ঞানিক পাঠ",
        "synonyms": ["Study of ferns", "Pteridophyte botany"],
        "antonyms": ["Dendrology"],
        "exampleEn": "Her groundbreaking research in pteridology identified three endangered species of rainforest ferns.",
        "exampleBn": "ফার্নবিদ্যায় তাঁর যুগান্তকারী গবেষণা রেইনফরেস্টের তিনটি বিপন্ন প্রজাতির ফার্ন শনাক্ত করেছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Dendrology",
        "pos": "Noun",
        "phonetic": "/dɛnˈdrɒl.ə.dʒi/ (ডেনড্রোলজি)",
        "meaningBn": "বৃক্ষ ও কাষ্ঠল উদ্ভিদের বৈজ্ঞানিক পাঠ",
        "synonyms": ["Study of trees", "Arboriculture science"],
        "antonyms": ["Agrostology"],
        "exampleEn": "A degree in dendrology equips foresters with the expertise to identify diverse timber species.",
        "exampleBn": "বৃক্ষবিদ্যার ডিগ্রি বন কর্মকর্তাদের বিভিন্ন কাষ্ঠল প্রজাতির গাছ শনাক্ত করার দক্ষতা দেয়।",
        "category": "Science & Environment"
    },
    {
        "word": "Pomology",
        "pos": "Noun",
        "phonetic": "/poʊˈmɒl.ə.dʒi/ (পোমোলজি)",
        "meaningBn": "ফল চাষ ও ফলবিজ্ঞান",
        "synonyms": ["Fruit breeding", "Study of fruit growing"],
        "antonyms": ["Floriculture"],
        "exampleEn": "Specialists in pomology developed disease-resistant apple cultivars for commercial orchards.",
        "exampleBn": "ফলবিজ্ঞান বিশেষজ্ঞরা বাণিজ্যিক বাগানের জন্য রোগপ্রতিরোধী আপেলের জাত উদ্ভাবন করেছেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Agrostology",
        "pos": "Noun",
        "phonetic": "/ˌæɡ.rɒsˈtɒl.ə.dʒi/ (অ্যাগ্রোস্টোলজি)",
        "meaningBn": "ঘাস ও তৃণজাতীয় উদ্ভিদসংক্রান্ত বিজ্ঞান",
        "synonyms": ["Graminology", "Study of grasses"],
        "antonyms": ["Dendrology"],
        "exampleEn": "Agrostology is vital for improving forage crops in arid grazing pastures.",
        "exampleBn": "শুষ্ক চারণভূমিতে পশুখাদ্য হিসেবে ঘাসের ফলন বাড়ানোর জন্য তৃণবিজ্ঞান অত্যন্ত জরুরি।",
        "category": "Science & Environment"
    },
    {
        "word": "Herpetology",
        "pos": "Noun",
        "phonetic": "/ˌhɜː.pɪˈtɒl.ə.dʒi/ (হার্পেটোলজি)",
        "meaningBn": "উভচর ও সরীসৃপ প্রাণীসংক্রান্ত বিজ্ঞান",
        "synonyms": ["Study of reptiles and amphibians"],
        "antonyms": ["Ornithology", "Mammalogy"],
        "exampleEn": "Her enthusiasm for herpetology led her to explore the swamp in search of venomous vipers.",
        "exampleBn": "সরীসৃপবিজ্ঞানের প্রতি অনুরাগের কারণে সে বিষধর ভাইপারের সন্ধানে জলাভূমিতে অনুসন্ধান চালিয়েছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Malacology",
        "pos": "Noun",
        "phonetic": "/ˌmæl.əˈkɒl.ə.dʒi/ (ম্যালাকোলজি)",
        "meaningBn": "শামুক, ঝিনুক ও কম্বোজ (mollusk) প্রাণীসংক্রান্ত বিজ্ঞান",
        "synonyms": ["Mollusk zoology", "Invertebrate study"],
        "antonyms": ["Ichthyology"],
        "exampleEn": "Marine malacology examines the calcification processes of deep-sea clams.",
        "exampleBn": "সামুদ্রিক কম্বোজবিজ্ঞান গভীর সমুদ্রের ঝিনুকের খোলস গঠনের রাসায়নিক প্রক্রিয়া পর্যালোচনা করে।",
        "category": "Science & Environment"
    }
]

with open('next_part1_sub2.json', 'w', encoding='utf-8') as f:
    json.dump(batch1_sub2, f, ensure_ascii=False, indent=2)
print("Part 1 sub 2 written:", len(batch1_sub2))

# Combine sub1 and sub2 into next_part1.json with packId 64 and IDs 6301 to 6400
with open('next_part1_sub1.json') as f1, open('next_part1_sub2.json') as f2:
    w1 = json.load(f1)
    w2 = json.load(f2)

full1 = w1 + w2
for i, item in enumerate(full1):
    item['id'] = 6301 + i
    item['packId'] = 64

with open('next_part1.json', 'w', encoding='utf-8') as fout:
    json.dump(full1, fout, ensure_ascii=False, indent=2)
print("Complete next_part1.json ready with entries:", len(full1))

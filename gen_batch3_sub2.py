# -*- coding: utf-8 -*-
import json

batch3_sub2 = [
    {
        "word": "Sublimation",
        "pos": "Noun",
        "phonetic": "/ˌsʌb.lɪˈmeɪ.ʃən/ (সাবলিমেশন)",
        "meaningBn": "অগ্রহণযোগ্য জৈবিক কামনা বা ক্ষোভকে উচ্চতর সৃষ্টিশীল সামাজিক কাজে রূপান্তর",
        "synonyms": ["Constructive redirection", "Psychological refinement"],
        "antonyms": ["Degradation", "Destructive acting-out"],
        "exampleEn": "Freud considered artistic creation to be the highest form of drive sublimation.",
        "exampleBn": "ফ্রয়েড শৈল্পিক সৃষ্টিকে মানুষের অন্তর্নিহিত কামনার সর্বোচ্চ ইতিবাচক রূপান্তর বলে মনে করতেন।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Introjection",
        "pos": "Noun",
        "phonetic": "/ˌɪn.troʊˈdʒɛk.ʃən/ (ইনট্রোজেকশন)",
        "meaningBn": "বাইরের পরিবেশ বা অন্যের আদর্শ ও মূল্যবোধকে অবচেতনভাবে নিজের মধ্যে আত্মস্থ করা",
        "synonyms": ["Internalization", "Unconscious assimilation"],
        "antonyms": ["Projection", "Externalization"],
        "exampleEn": "A child forms their moral conscience through the gradual introjection of parental values.",
        "exampleBn": "একটি শিশু পিতা-মাতার মূল্যবোধ অবচেতনভাবে নিজের ভেতর আত্মস্থ করার মাধ্যমে নীতিবোধ গড়ে তোলে।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Abductive",
        "pos": "Adjective",
        "phonetic": "/æbˈdʌk.tɪভ/ (অ্যাবডাক্টিভ)",
        "meaningBn": "প্রাপ্ত প্রমাণের ভিত্তিতে সম্ভাব্য সেরা যৌক্তিক অনুমান সংক্রান্ত (যুক্তিবিদ্যায়)",
        "synonyms": ["Inference to best explanation", "Diagnostic reasoning"],
        "antonyms": ["Deductive", "Inductive"],
        "exampleEn": "Medical diagnosis is fundamentally an abductive reasoning process from observed symptoms.",
        "exampleBn": "চিকিৎসায় রোগ নির্ণয় হলো পর্যবেক্ষণকৃত উপসর্গের ভিত্তিতে সম্ভাব্য সেরা অনুমানের যৌক্তিক প্রক্রিয়া।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Petitio principii",
        "pos": "Noun",
        "phonetic": "/pəˈtɪʃ.i.oʊ prɪnˈsɪp.i.aɪ/ (পেটিশিয়ো প্রিন্সিপিয়াই)",
        "meaningBn": "চক্রক যুক্তি বা যা প্রমাণ করতে হবে তাকেই স্বতঃসিদ্ধ ধরে নেওয়ার কুযুক্তি (Begging the question)",
        "synonyms": ["Begging the question", "Circular argument", "Vicious circle"],
        "antonyms": ["Sound demonstration", "Independent proof"],
        "exampleEn": "Assuming the book is divine because the author claims so is a classic petitio principii.",
        "exampleBn": "লেখক দাবি করেছেন বলেই বইটি ঐশ্বরিক—এমন ধরে নেওয়া হলো একটি চিরায়ত চক্রক কুযুক্তি।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Tu quoque",
        "pos": "Noun",
        "phonetic": "/ˌtuː ˈkwoʊ.kwi/ (তু কোকওয়ে)",
        "meaningBn": "যুক্তির জবাব না দিয়ে অভিযোগকারীর দিকেই পাল্টা অভিযোগ ছোড়ার ভ্রান্ত যুক্তি",
        "synonyms": ["You too fallacy", "Counter-accusation", "Whataboutism"],
        "antonyms": ["Direct refutation", "Valid counterargument"],
        "exampleEn": "Responding to corruption allegations by accusing opponents of greed is a cheap tu quoque deflection.",
        "exampleBn": "দুর্নীতির অভিযোগের উত্তরে বিরোধীদের লোভী বলে দোষারোপ করা হলো নিছক পাল্টা দোষ চাপানোর কুযুক্তি।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Reductio ad absurdum",
        "pos": "Noun",
        "phonetic": "/rɪˈdʌk.ti.oʊ æd əbˈsɜːr.dəm/ (রিডাকশিও অ্যাড অ্যাবসার্ডাম)",
        "meaningBn": "কোনো প্রস্তাবনাকে সত্য ধরলে যে চরম অযৌক্তিক ফলাফল আসে তা দেখিয়ে তা ভুল প্রমাণ করার পদ্ধতি",
        "synonyms": ["Proof by contradiction", "Reduction to absurdity"],
        "antonyms": ["Direct affirmation"],
        "exampleEn": "The mathematician used reductio ad absurdum to demonstrate that the square root of two is irrational.",
        "exampleBn": "গণিতবিদ চরম অযৌক্তিক ফলাফল প্রদর্শনের পদ্ধতি ব্যবহার করে প্রমাণ করেছিলেন রুট ২ একটি অমূলদ সংখ্যা।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Epiphenomenon",
        "pos": "Noun",
        "phonetic": "/ˌɛp.ɪ.fɪˈnɒm.ɪ.nɒn/ (এপিফেনোমেনন)",
        "meaningBn": "একটি মূল প্রক্রিয়ার সাথে আনুষঙ্গিকভাবে সৃষ্ট গৌণ উপজাত যা মূল ঘটনাকে প্রভাবিত করে না",
        "synonyms": ["By-product", "Secondary manifestation", "Side effect"],
        "antonyms": ["Root cause", "Primary driver"],
        "exampleEn": "Materialist philosophers argue that human consciousness is merely an epiphenomenon of neural activity.",
        "exampleBn": "বস্তুবাদী দার্শনিকরা দাবি করেন মানুষের চেতনা কেবল মস্তিষ্কের স্নায়বিক ক্রিয়ার একটি গৌণ উপজাত।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Qualia",
        "pos": "Noun",
        "phonetic": "/ˈkwɑː.li.ə/ (কুয়ালিয়া)",
        "meaningBn": "অনুভূতির নিজস্ব ব্যক্তিনিষ্ঠ গুণ বা স্বাদ (যেমন লাল রঙের নিজস্ব অনুভূতি বা ব্যথার তীব্রতা)",
        "synonyms": ["Subjective conscious experience", "Phenomenal sensations"],
        "antonyms": ["Objective metrics", "Physical wavelengths"],
        "exampleEn": "Philosophers debate whether an artificial intelligence could ever truly experience internal qualia.",
        "exampleBn": "দার্শনিকরা বিতর্ক করেন কৃত্রিম বুদ্ধিমত্তা কখনো সত্যিকারের অভ্যন্তরীণ ব্যক্তিনিষ্ঠ অনুভূতি অনুভব করতে পারবে কি না।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Panpsychism",
        "pos": "Noun",
        "phonetic": "/pænˈsaɪ.kɪz.əm/ (প্যানসাইকিজম)",
        "meaningBn": "মহাবিশ্বের প্রতিটি পদার্থের মধ্যে কোনো না কোনো মাত্রার চেতনা বিদ্যমান—এই দার্শনিক মতবাদ",
        "synonyms": ["Universal consciousness theory", "All-pervading mind"],
        "antonyms": ["Physicalism", "Mechanism"],
        "exampleEn": "Contemporary physics has sparked a renewed philosophical interest in panpsychism.",
        "exampleBn": "সমসাময়িক পদার্থবিদ্যা বিশ্বজনীন সর্বচেতনাবাদের দর্শনে নতুন করে আগ্রহের জন্ম দিয়েছে।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Compatibilism",
        "pos": "Noun",
        "phonetic": "/kəmˈpæt.ə.bəl.ɪz.əm/ (কমপ্যাটিবিলিজম)",
        "meaningBn": "মানুষের স্বাধীন ইচ্ছা ও নিয়তিবাদ বা কার্যকারণবাদের মধ্যে কোনো বিরোধ নেই—এই মতবাদ",
        "synonyms": ["Soft determinism", "Free will reconciling theory"],
        "antonyms": ["Hard determinism", "Incompatibilism"],
        "exampleEn": "Compatibilism maintains that moral responsibility can coexist with deterministic physical laws.",
        "exampleBn": "সঙ্গতিবাদ মতবাদ অনুসারে মানুষের নৈতিক দায়িত্ব নির্ধারিত প্রাকৃতিক নিয়মের সাথে একসাথে টিকে থাকতে পারে।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Incompatibilism",
        "pos": "Noun",
        "phonetic": "/ˌɪn.kəmˈpæt.ə.bəl.ɪz.əm/ (ইনকমপ্যাটিবিলিজম)",
        "meaningBn": "স্বাধীন ইচ্ছা ও কার্যকারণবাদ বা নিয়তিবাদ কখনোই একসাথে সম্ভব নয়—এই মতবাদ",
        "synonyms": ["Hard determinism or libertarianism", "Irreconcilability of free will"],
        "antonyms": ["Compatibilism", "Soft determinism"],
        "exampleEn": "Incompatibilism argues that if physical determinism is true, genuine free choice is an illusion.",
        "exampleBn": "অসঙ্গতিবাদ দাবি করে যদি প্রাকৃতিক কার্যকারণবাদ সত্য হয় তবে মানুষের স্বাধীন পছন্দ কেবল একটি দৃষ্টিভ্রম।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Choler",
        "pos": "Noun",
        "phonetic": "/ˈkɒl.ər/ (কলার)",
        "meaningBn": "তীব্র ক্রোধ, উগ্র মেজাজ বা খিটখিটে স্বভাব",
        "synonyms": ["Anger", "Bile", "Irascibility", "Fury"],
        "antonyms": ["Serenity", "Placidity", "Calmness"],
        "exampleEn": "The commander struggled to restrain his sudden choler when the scout reported a tactical retreat.",
        "exampleBn": "গুপ্তচর যখন কৌশলগত পিছু হটার সংবাদ দিল তখন সেনাপতি তাঁর তীব্র উগ্র ক্রোধ সংবরণ করতে হিমশিম খাচ্ছিলেন।",
        "category": "BCS & Bank"
    },
    {
        "word": "Phlegmatic",
        "pos": "Adjective",
        "phonetic": "/flɛɡˈmæt.ɪk/ (ফ্লেগমেটিক)",
        "meaningBn": "ধীরস্থির, শান্ত ও সহজে বিচলিত হয় না এমন আবেগহীন ব্যক্তিত্ব",
        "synonyms": ["Unflappable", "Calm", "Impassive", "Stolid"],
        "antonyms": ["Excitable", "Volatile", "Hot-headed"],
        "exampleEn": "Even during the terrifying turbulence, the phlegmatic pilot reassured passengers with absolute poise.",
        "exampleBn": "ভীষণ বিমান ঝাঁকুনির মধ্যেও ধীরস্থির ও শান্ত পাইলট অটল আত্মবিশ্বাসে যাত্রীদের আশ্বস্ত করেছিলেন।",
        "category": "BCS & Bank"
    },
    {
        "word": "Sanguineous",
        "pos": "Adjective",
        "phonetic": "/sæŋˈɡwɪn.i.əs/ (স্যাঙ্গুইনিয়াস)",
        "meaningBn": "রক্তবর্ণ বা রক্তসংশ্লিষ্ট; রক্তে রঞ্জিত",
        "synonyms": ["Blood-red", "Bloody", "Sanguine"],
        "antonyms": ["Pale", "Colorless", "Bloodless"],
        "exampleEn": "The battlefield was soaked in a sanguineous mist as evening descended.",
        "exampleBn": "সন্ধ্যা নেমে আসার সাথে সাথে যুদ্ধক্ষেত্রটি রক্তবর্ণ কুয়াশায় ঢেকে গিয়েছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Bilious",
        "pos": "Adjective",
        "phonetic": "/ˈbɪl.i.əs/ (বিলিয়াস)",
        "meaningBn": "বমি বমি ভাবযুক্ত অথবা বদমেজাজি ও তীব্র তিক্ত মেজাজের",
        "synonyms": ["Peevish", "Queasy", "Irascible", "Splenetic"],
        "antonyms": ["Good-humored", "Congenial", "Affable"],
        "exampleEn": "A bilious review dismissed the young author's debut novel as unreadable trash.",
        "exampleBn": "এক বদমেজাজি ও তিক্ত সমালোচনায় তরুণ লেখকের প্রথম উপন্যাসটিকে অপাঠ্য আবর্জনা বলে উড়িয়ে দেওয়া হয়।",
        "category": "BCS & Bank"
    },
    {
        "word": "Atrabilious",
        "pos": "Adjective",
        "phonetic": "/ˌæt.rəˈbɪl.i.əs/ (অ্যাট্রাবিলিয়াস)",
        "meaningBn": "বিষাদগ্রস্ত, বিষণ্ণ ও খিটখিটে মেজাজের",
        "synonyms": ["Melancholic", "Gloomy", "Morose", "Sullen"],
        "antonyms": ["Cheerful", "Sanguine", "Buoyant"],
        "exampleEn": "His atrabilious outlook on humanity made him avoid social gatherings entirely.",
        "exampleBn": "মানবজাতির প্রতি তাঁর বিষণ্ণ ও তিক্ত দৃষ্টিভঙ্গি তাঁকে সামাজিক মেলামেশা থেকে পুরোপুরি দূরে রেখেছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Splenetic",
        "pos": "Adjective",
        "phonetic": "/splɪˈnɛt.ɪk/ (স্প্লেনেটিক)",
        "meaningBn": "তীব্র খিটখিটে, সহজে ক্ষুব্ধ ও তিক্ত স্বভাবের",
        "synonyms": ["Bad-tempered", "Spiteful", "Peevish", "Cholerous"],
        "antonyms": ["Good-natured", "Placid", "Mild"],
        "exampleEn": "The editor's splenetic outburst intimidated the young cub reporters in the newsroom.",
        "exampleBn": "নিউজরুমে সম্পাদকের তীব্র খিটখিটে রাগ ও ধমক নতুন শিক্ষানবিশ সাংবাদিকদের ভীত করে তুলেছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Saturnine",
        "pos": "Adjective",
        "phonetic": "/ˈsæt.ər.naɪn/ (স্যাটারনাইন)",
        "meaningBn": "গম্ভীর, বিষাদময়, অন্ধকারাচ্ছন্ন ও মৃদু হাসিশূন্য স্বভাবের",
        "synonyms": ["Gloomy", "Somber", "Glum", "Taciturn"],
        "antonyms": ["Jovial", "Sunny", "Cheerful"],
        "exampleEn": "Behind his dark clothes and saturnine expression lay a profoundly compassionate heart.",
        "exampleBn": "তাঁর কালো পোশাক ও গম্ভীর বিষাদময় চেহারার পেছনে লুকিয়ে ছিল এক গভীর সহানুভূতিশীল হৃদয়।",
        "category": "BCS & Bank"
    },
    {
        "word": "Mercurial",
        "pos": "Adjective",
        "phonetic": "/mɜːrˈkjʊər.i.əl/ (মারকিউরিয়াল)",
        "meaningBn": "চঞ্চল ও ক্ষণে ক্ষণে মেজাজ পরিবর্তনকারী; পরিবর্তনশীল",
        "synonyms": ["Volatile", "Capricious", "Erratic", "Fickle"],
        "antonyms": ["Steadfast", "Constant", "Reliable"],
        "exampleEn": "Working with a boss of such mercurial temper meant walking on eggshells every day.",
        "exampleBn": "এমন ক্ষণে ক্ষণে মেজাজ পরিবর্তনকারী বসের সাথে কাজ করা মানে প্রতিদিনই আতঙ্কের মধ্যে থাকা।",
        "category": "BCS & Bank"
    },
    {
        "word": "Jovial",
        "pos": "Adjective",
        "phonetic": "/ˈdʒoʊ.vi.əl/ (জোভিয়াল)",
        "meaningBn": "হাসিখুশি, প্রফুল্ল, আমুদে ও বন্ধুবৎসল",
        "synonyms": ["Cheerful", "Merry", "Genial", "Convivial"],
        "antonyms": ["Saturnine", "Morose", "Gloomy"],
        "exampleEn": "The jovial host ensured that every guest had a warm drink and a hearty laugh.",
        "exampleBn": "হাসিখুশি ও আমুদে গৃহকর্তা নিশ্চিত করেছিলেন যেন প্রতিটি অতিথি উষ্ণ পানীয় এবং প্রাণখোলা আনন্দ উপভোগ করে।",
        "category": "BCS & Bank"
    },
    {
        "word": "Martial",
        "pos": "Adjective",
        "phonetic": "/ˈmɑːr.ʃəl/ (মার্শাল)",
        "meaningBn": "যুদ্ধসংক্রান্ত; সামরিক বা বীরত্বব্যঞ্জক",
        "synonyms": ["Warlike", "Military", "Soldierly", "Bellicose"],
        "antonyms": ["Peaceful", "Civilian", "Pacific"],
        "exampleEn": "The brass band played inspiring martial music as the regiments paraded past.",
        "exampleBn": "রেজিমেন্টের কুচকাওয়াজের সময় ব্রাস ব্যান্ডটি অনুপ্রেরণাদায়ী সামরিক সুর বাজাচ্ছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Apollonian",
        "pos": "Adjective",
        "phonetic": "/ˌæp.əˈloʊ.ni.ən/ (অ্যাপোলোনিয়ান)",
        "meaningBn": "শৃঙ্খলাবদ্ধ, যুক্তিনিষ্ঠ, ভারসাম্যপূর্ণ ও আত্মসংযমী শিল্পরীতি বা মনোভাব",
        "synonyms": ["Rational", "Harmonious", "Serene", "Measured"],
        "antonyms": ["Dionysian", "Chaotic", "Frenzied"],
        "exampleEn": "Classical Greek architecture exemplifies the Apollonian ideal of geometric symmetry.",
        "exampleBn": "ধ্রুপদী গ্রিক স্থাপত্যকলা জ্যামিতিক সামঞ্জস্যের সুশৃঙ্খল অ্যাপোলোনিয়ান আদর্শকে মূর্ত করে তোলে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Dionysian",
        "pos": "Adjective",
        "phonetic": "/ˌdaɪ.əˈnɪs.i.ən/ (ডায়োনিসিয়ান)",
        "meaningBn": "আবেগতাড়িত, উন্মাতাল, অসংযত ও বুনো উল্লাসপূর্ণ শিল্পরীতি বা স্বভাব",
        "synonyms": ["Ecstatic", "Frenzied", "Wild", "Bacchic"],
        "antonyms": ["Apollonian", "Disciplined", "Measured"],
        "exampleEn": "The festival culminated in a Dionysian night of wild drumming and unrestrained dancing.",
        "exampleBn": "উৎসবটি বুনো ড্রামের আওয়াজ এবং অসংযত উন্মাতাল নৃত্যের এক আবেগপূর্ণ রাতে রূপ নিয়েছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Promethean",
        "pos": "Adjective",
        "phonetic": "/prəˈmiː.θi.ən/ (প্রোমিথিয়ান)",
        "meaningBn": "মানবকল্যাণে প্রচলিত বিধান বা কর্তৃত্বকে সাহসের সাথে চ্যালেঞ্জকারী এবং সাহসী উদ্ভাবনী",
        "synonyms": ["Daringly innovative", "Rebellious for good", "Heroically creative"],
        "antonyms": ["Timid", "Submissive", "Conformist"],
        "exampleEn": "The scientists mounted a Promethean effort to crack the genetic code and cure hereditary illnesses.",
        "exampleBn": "বিজ্ঞানীরা বংশগত রোগ নিরাময়ের জন্য জেনেটিক কোড ভাঙার এক সাহসী প্রোমিথিয়ান প্রয়াস চালিয়েছিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Sisyphean",
        "pos": "Adjective",
        "phonetic": "/ˌsɪs.ɪˈfiː.ən/ (সিসিফিয়ান)",
        "meaningBn": "অবিরাম অথচ চরম নিষ্ফল ও কখনোই শেষ না হওয়া একঘেয়ে কঠিন পরিশ্রম",
        "synonyms": ["Endless and futile", "Laborious", "Unending toil"],
        "antonyms": ["Productive", "Fulfilling", "Fruitful"],
        "exampleEn": "Manual sorting of millions of unlabelled documents proved to be a soul-crushing, Sisyphean task.",
        "exampleBn": "লাখ লাখ নামহীন নথি হাতে বাছাই করা এক প্রাণান্তকর ও অন্তহীন নিষ্ফল পরিশ্রমে পরিণত হয়েছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Tantalizing",
        "pos": "Adjective",
        "phonetic": "/ˈtæn.tə.laɪ.zɪŋ/ (ট্যান্টালাইজিং)",
        "meaningBn": "চোখের সামনে লোভনীয় কিছু রেখে নাগালের বাইরে রেখে প্রলুব্ধ বা উত্যক্তকারী",
        "synonyms": ["Teasing", "Alluring", "Enticing", "Tormenting"],
        "antonyms": ["Repulsive", "Satisfying", "Attainable"],
        "exampleEn": "A tantalizing aroma of baking bread drifted from the kitchen window, teasing our hunger.",
        "exampleBn": "রান্নাঘরের জানালা দিয়ে তাজা পাউরুটি সেঁকার লোভনীয় সুবাস ভেসে এসে আমাদের ক্ষুধাকে উসকে দিচ্ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Procrustean",
        "pos": "Adjective",
        "phonetic": "/prəˈkrʌs.ti.ən/ (প্রোক্রাস্টিয়ান)",
        "meaningBn": "ব্যক্তিগত পার্থক্য অগ্রাহ্য করে সকলকে জোরপূর্বক একটিমাত্র নির্দিষ্ট কাঠামোর মধ্যে মাপার নির্মম রীতি",
        "synonyms": ["Arbitrarily rigid", "Enforcing uniform conformity", "Inflexible"],
        "antonyms": ["Flexible", "Accommodating", "Customized"],
        "exampleEn": "The school's Procrustean curriculum stifled children who did not fit the rigid standardized mold.",
        "exampleBn": "বিদ্যালয়ের চাপিয়ে দেওয়া নির্মম অভিন্ন পাঠ্যক্রম সেই শিশুদের বিকাশ রুদ্ধ করেছিল যারা বাঁধা নিয়মে আঁটত না।",
        "category": "Literature & Arts"
    },
    {
        "word": "Protean",
        "pos": "Adjective",
        "phonetic": "/ˈproʊ.ti.ən/ (প্রোটিয়ান)",
        "meaningBn": "সহজেই রূপ বা ভূমিকা পরিবর্তন করতে সক্ষম; বহুরূপী",
        "synonyms": ["Versatile", "Ever-changing", "Variable", "Chameleonic"],
        "antonyms": ["Rigid", "Static", "Monolithic"],
        "exampleEn": "The actor was celebrated for his protean genius, playing comedy, tragedy, and farce with equal flair.",
        "exampleBn": "অভিনেতাটি তাঁর বহুরূপী প্রতিভার জন্য নন্দিত ছিলেন, যিনি সমান দক্ষতায় হাস্যরস ও ট্র্যাজেডি ফুটিয়ে তুলতেন।",
        "category": "BCS & Bank"
    },
    {
        "word": "Gordian",
        "pos": "Adjective",
        "phonetic": "/ˈɡɔːr.di.ən/ (গর্ডিয়ান)",
        "meaningBn": "অত্যন্ত জটিল বা দুর্বোধ্য (যেমন Gordian knot = জটিল সমস্যা)",
        "synonyms": ["Intricate", "Extremely complex", "Knotted"],
        "antonyms": ["Simple", "Straightforward"],
        "exampleEn": "Resolving the maritime border dispute required cutting through a Gordian knot of historic rivalries.",
        "exampleBn": "সামুদ্রিক সীমান্ত বিরোধের মীমাংসা করতে ঐতিহাসিক বৈরিতার জটিল গ্রন্থি সাহসের সাথে ছিন্ন করতে হয়েছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Pyrrhic",
        "pos": "Adjective",
        "phonetic": "/ˈpɪr.ɪk/ (পিরিক)",
        "meaningBn": "এমন বিজয় যা অর্জনে এত বিপুল ক্ষয়ক্ষতি হয় যে তা কার্যত পরাজয়ের সমান",
        "synonyms": ["Costly victory", "Ruinous gain", "Hollow win"],
        "antonyms": ["Decisive victory", "Resounding triumph"],
        "exampleEn": "Winning the defamation lawsuit at the expense of his life savings proved to be a Pyrrhic victory.",
        "exampleBn": "জীবনের সঞ্চিত সব অর্থ খুইয়ে মানহানির মামলায় জয়ী হওয়া কার্যত এক পরাজয়তুল্য বিজয় প্রমাণ হয়েছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Draconian",
        "pos": "Adjective",
        "phonetic": "/drəˈkoʊ.ni.ən/ (ড্রাকোনিয়ান)",
        "meaningBn": "চরম কঠোর, নির্মম ও নিষ্ঠুর (আইন বা শাস্তির ক্ষেত্রে)",
        "synonyms": ["Harsh", "Severe", "Strict", "Ruthless"],
        "antonyms": ["Lenient", "Merciful", "Mild"],
        "exampleEn": "Human rights advocates condemned the government's draconian curfew penalties.",
        "exampleBn": "মানবাধিকার কর্মীরা সরকারের সান্ধ্য আইনের চরম কঠোর ও নির্মম শাস্তির নিন্দা জানিয়েছিলেন।",
        "category": "BCS & Bank"
    },
    {
        "word": "Spartan",
        "pos": "Adjective",
        "phonetic": "/ˈspɑːr.tən/ (স্পার্টান)",
        "meaningBn": "চরম সাধারণ, জাঁকজমকহীন, কঠিন কৃচ্ছ্রসাধন ও শৃঙ্খলাপরায়ণ",
        "synonyms": ["Austere", "Ascetic", "Frugal", "Self-disciplined"],
        "antonyms": ["Luxurious", "Opulent", "Sybaritic"],
        "exampleEn": "Monks live in spartan cells equipped with nothing more than a wooden cot and desk.",
        "exampleBn": "সন্ন্যাসীরা চরম জাঁকজমকহীন কক্ষে বাস করেন যেখানে কাঠের খাট আর পড়ার টেবিল ছাড়া কিছুই নেই।",
        "category": "BCS & Bank"
    },
    {
        "word": "Utopian",
        "pos": "Adjective",
        "phonetic": "/juːˈtoʊ.pi.ən/ (ইউটোপিয়ান)",
        "meaningBn": "বাস্তবায়নযোগ্য নয় এমন কাল্পনিক নিখুঁত আদর্শ সমাজ বা স্বপ্নসংক্রান্ত",
        "synonyms": ["Idealistic", "Visionary", "Chimerical", "Quixotic"],
        "antonyms": ["Dystopian", "Pragmatic", "Realistic"],
        "exampleEn": "Critics dismissed his manifesto as an unrealistic, utopian fantasy detached from human nature.",
        "exampleBn": "সমালোচকরা তাঁর ইশতেহারকে মানবপ্রকৃতি বিবর্জিত এক কাল্পনিক অবাস্তব ইউটোপিয়ান স্বপ্ন বলে বাতিল করে দেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Dystopian",
        "pos": "Adjective",
        "phonetic": "/dɪsˈtoʊ.pi.ən/ (ডিস্টোপিয়ান)",
        "meaningBn": "ভয়াবহ স্বৈরতন্ত্র, চরম দুর্দশা ও নিপীড়িত ভবিষ্যৎ সমাজসংক্রান্ত",
        "synonyms": ["Nightmarish", "Oppressive", "Totalitarian future"],
        "antonyms": ["Utopian", "Idyllic"],
        "exampleEn": "Orwell's novel painted a chilling, dystopian vision of perpetual government surveillance.",
        "exampleBn": "অরওয়েলের উপন্যাসটি সার্বক্ষণিক সরকারি নজরদারির এক গা ছমছমে অন্ধকার স্বৈরতান্ত্রিক সমাজের চিত্র এঁকেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Machiavellian",
        "pos": "Adjective",
        "phonetic": "/ˌmæk.i.əˈvɛl.i.ən/ (ম্যাকিয়াভেলিয়ান)",
        "meaningBn": "ক্ষমতা টিকিয়ে রাখতে কূটকৌশলী, চতুর, নীতিহীন ও সুবিধাবাদী",
        "synonyms": ["Cunning", "Scheming", "Devious", "Unscrupulous"],
        "antonyms": ["Ingenuous", "Scrupulous", "Noble"],
        "exampleEn": "The corporate executive used Machiavellian tactics to oust his rivals from the boardroom.",
        "exampleBn": "করপোরেট নির্বাহী বোর্ডরুম থেকে প্রতিদ্বন্দ্বীদের সরাতে চরম কূটকৌশলী ও নীতিহীন পথ অবলম্বন করেছিলেন।",
        "category": "BCS & Bank"
    },
    {
        "word": "Orwellian",
        "pos": "Adjective",
        "phonetic": "/ɔːrˈwɛl.i.ən/ (অরওয়েলিয়ান)",
        "meaningBn": "নাগরিকের ব্যক্তিগত স্বাধীনতা হরণকারী ও সর্বগ্রাসী রাষ্ট্রীয় নজরদারিসম্পর্কিত",
        "synonyms": ["Totalitarian", "Surveillance-heavy", "Oppressive state"],
        "antonyms": ["Democratic", "Egalitarian", "Free"],
        "exampleEn": "The mandatory facial recognition policy felt uncomfortably Orwellian to privacy campaigners.",
        "exampleBn": "বাধ্যতামূলক মুখমণ্ডল শনাক্তকরণ নীতিটি ব্যক্তিস্বাধীনতা কর্মীদের কাছে অস্বস্তিকরভাবে অরওয়েলিয়ান স্বৈরতান্ত্রিক মনে হয়েছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Kafkaesque",
        "pos": "Adjective",
        "phonetic": "/ˌkæf.kəˈɛsk/ (কাফকায়েস্ক)",
        "meaningBn": "অযৌক্তিক, জটিল আমলাতান্ত্রিক বেড়াজাল এবং দুঃস্বপ্নময় অদ্ভুত পরিস্থিতি",
        "synonyms": ["Surreal and nightmarish", "Bureaucratically absurd", "Bizarre"],
        "antonyms": ["Sensible", "Transparent", "Streamlined"],
        "exampleEn": "Trying to correct an error on his tax report turned into a maddening, Kafkaesque ordeal.",
        "exampleBn": "ট্যাক্স ফাইলের একটি ভুল সংশোধন করতে যাওয়া এক উন্মাদনাময় ও দুঃস্বপ্নতুল্য কাফকায়েস্ক ভোগান্তিতে রূপ নেয়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Byronic",
        "pos": "Adjective",
        "phonetic": "/baɪˈrɒn.ɪk/ (বায়রনিক)",
        "meaningBn": "উদ্বেল, অভিমানী, আবেগপ্রবণ অথচ বিষণ্ণ ও প্রথাবিরোধী ব্যক্তিত্বসংক্রান্ত",
        "synonyms": ["Brooding", "Romantic rebel", "Melancholy and defiant"],
        "antonyms": ["Conventional", "Cheery", "Conforming"],
        "exampleEn": "With his brooding gaze and troubled past, the novel's protagonist is the archetype of a Byronic hero.",
        "exampleBn": "বিষাদময় চাউনি ও বিক্ষুব্ধ অতীতের কারণে উপন্যাসের নায়কটি এক খাঁটি বায়রনিক প্রথাবিরোধী চরিত্রের প্রতীক।",
        "category": "Literature & Arts"
    },
    {
        "word": "Faustian",
        "pos": "Adjective",
        "phonetic": "/ˈfaʊ.sti.ən/ (ফাউস্টিয়ান)",
        "meaningBn": "সাময়িক স্বার্থ, ক্ষমতা বা আনন্দের লোভে নিজের আত্মা বা নৈতিকতা বিসর্জন দেওয়ার মতো চুক্তি",
        "synonyms": ["Soul-selling", "Compromising integrity for power"],
        "antonyms": ["Principled", "Uncompromising"],
        "exampleEn": "Acquiring monopoly power by colluding with criminals was a tragic Faustian bargain.",
        "exampleBn": "অপরাধীদের সাথে যোগসাজশ করে একচেটিয়া আধিপত্য অর্জন করা ছিল এক আত্মঘাতী ফাউস্টিয়ান নৈতিকতার বিসর্জন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Pickwickian",
        "pos": "Adjective",
        "phonetic": "/pɪkˈwɪk.i.ən/ (পিকউইকিয়ান)",
        "meaningBn": "আক্ষরিক নয় বরং নিজস্ব ও বিশেষ অর্থে ব্যবহৃত শব্দ বা সাদাসিধে সরল রসাত্মক চরিত্র",
        "synonyms": ["Used in an esoteric sense", "Jovial and naive"],
        "antonyms": ["Literal", "Cynical"],
        "exampleEn": "He claimed he used the harsh word solely in a Pickwickian sense, without intending any offense.",
        "exampleBn": "তিনি দাবি করেছিলেন যে তিনি কড়া শব্দটি কেবল একটি বিশেষ হালকা অর্থে ব্যবহার করেছেন, কাউকে আঘাত করতে নয়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Micawberish",
        "pos": "Adjective",
        "phonetic": "/mɪˈkɔː.bər.ɪʃ/ (মিকবারিশ)",
        "meaningBn": "নিষ্ক্রিয় বসে থেকে অন্ধ বিশ্বাস রাখা যে ভবিষ্যতে কিছু একটা ইতিবাচক মিটে যাবে",
        "synonyms": ["Blindly optimistic in poverty", "Passive hopefulness"],
        "antonyms": ["Proactive", "Realistic", "Resourceful"],
        "exampleEn": "Relying on a Micawberish hope that debts will magically disappear is financial suicide.",
        "exampleBn": "ঋণ নিজে থেকেই অলৌকিকভাবে গায়েব হয়ে যাবে—এমন অন্ধ নিষ্ক্রিয় আশায় থাকা হলো অর্থনৈতিক আত্মহত্যার শামিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Pecksniffian",
        "pos": "Adjective",
        "phonetic": "/pɛkˈsnɪf.i.ən/ (পেকসনিফিয়ান)",
        "meaningBn": "বকধার্মিক; উচ্চ নৈতিকতার ভানকারী চরম কপট ও ভণ্ড ব্যক্তিত্ব",
        "synonyms": ["Hypocritically moralizing", "Sanctimonious", "Pharisaical"],
        "antonyms": ["Genuinely honest", "Humble", "Sincere"],
        "exampleEn": "The politician's Pecksniffian speeches on honesty contrasted sharply with his hidden bribes.",
        "exampleBn": "সততার বিষয়ে রাজনীতিবিদের লোকদেখানো নীতিবাগীশ বক্তৃতা তাঁর গোপন ঘুস নেওয়ার সাথে চরম বৈপরীত্য তৈরি করত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Babbittry",
        "pos": "Noun",
        "phonetic": "/ˈbæb.ɪ.tri/ (ব্যাবিট্রি)",
        "meaningBn": "মধ্যবিত্ত সমাজের অন্ধ প্রথানুগত্য ও বস্তুবাদী স্থূল মানসিকতা",
        "synonyms": ["Smug bourgeois conformity", "Narrow philistinism"],
        "antonyms": ["Bohemianism", "Intellectual nonconformity"],
        "exampleEn": "The satirist mocked the dull Babbittry of suburban business clubs.",
        "exampleBn": "ব্যঙ্গসাহিত্যিক শহরতলির ব্যবসায়ী ক্লাবের স্থূল বস্তুবাদী প্রথানুগত্য ও অন্ধ মানসিকতাকে উপহাস করেছিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Bowdlerism",
        "pos": "Noun",
        "phonetic": "/ˈbaʊd.lə.rɪz.əm/ (বাউডলারিজম)",
        "meaningBn": "অশালীন মনে করে বই বা নাটক থেকে মূল অংশ বা বাক্য ছাঁটাই করার শুচিবাই",
        "synonyms": ["Prudish expurgation", "Moral censorship"],
        "antonyms": ["Unabridged publishing", "Fidelity to original"],
        "exampleEn": "Victorian bowdlerism stripped classic Shakespearean tragedies of their raw sexual humor.",
        "exampleBn": "ভিক্টোরিয়ান শুচিবাই শেক্সপিয়ারের ক্লাসিক ট্র্যাজেডিগুলো থেকে তাদের আদি রসাত্মক হাস্যরস ছেঁটে ফেলেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Comstockery",
        "pos": "Noun",
        "phonetic": "/ˈkɒm.stɒk.ər.i/ (কমস্টকারিজম)",
        "meaningBn": "অতিমাত্রায় রক্ষনশীল নৈতিক শুচিবাই ও শিল্পের ওপর গোঁড়া সেন্সরশিপ আরোপ",
        "synonyms": ["Aggressive prudish censorship", "Overzealous moral policing"],
        "antonyms": ["Artistic freedom", "Open-mindedness"],
        "exampleEn": "Nineteenth-century comstockery led to the banning of great medical literature on anatomy.",
        "exampleBn": "উনবিংশ শতাব্দীর অন্ধ নৈতিক গোঁড়ামি অ্যানাটমির মতো বিখ্যাত চিকিৎসাশাস্ত্রের বইও নিষিদ্ধ করতে বাধ্য করেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Grundyism",
        "pos": "Noun",
        "phonetic": "/ˈɡrʌn.di.ɪz.əm/ (গ্রানডিইজম)",
        "meaningBn": "সমাজের লোক কী বলবে—এই ভয়ে চালিত গোঁড়া শুচিবাই ও আচারনিষ্ঠা",
        "synonyms": ["Narrow moral conventionalism", "Prudery", "Neighbourhood gossip fear"],
        "antonyms": ["Free thinking", "Individualism"],
        "exampleEn": "The young artist fled the small town to escape the stifling Grundyism of the village elders.",
        "exampleBn": "তরুণ শিল্পী গ্রামের মুরুব্বিদের লোকলজ্জার ভয়ে চালিত গোঁড়া মানসিকতা এড়াতে শহর ছেড়ে পালিয়ে গিয়েছিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Philistinism",
        "pos": "Noun",
        "phonetic": "/ˈfɪl.ɪ.stɪ.nɪz.əm/ (ফিলিস্টিনিজম)",
        "meaningBn": "শিল্প, সাহিত্য ও সংস্কৃতির প্রতি উদাসীনতা, অশ্রদ্ধা ও স্থূল বস্তুবাদী মনোভাব",
        "synonyms": ["Anti-intellectualism", "Cultural ignorance", "Boorishness"],
        "antonyms": ["Cultured sophistication", "Connoisseurship"],
        "exampleEn": "The decision to demolish the heritage theater exposed the crass philistinism of the city council.",
        "exampleBn": "ঐতিহ্যবাহী প্রেক্ষাগৃহ ভেঙে ফেলার সিদ্ধান্তটি নগর পরিষদের স্থূল সংস্কৃতিহীন মানসিকতা উন্মোচিত করেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Iconoclasm",
        "pos": "Noun",
        "phonetic": "/aɪˈkɒn.əˌklæz.əm/ (আইকনোক্লাজম)",
        "meaningBn": "মূর্তিভাঙ্গা অথবা সমাজে প্রতিষ্ঠিত চিরাচরিত ধ্যানধারণা ও বিশ্বাসকে আক্রমণ করার মনোভাব",
        "synonyms": ["Image-breaking", "Challenging orthodoxy", "Radical heresy"],
        "antonyms": ["Traditionalism", "Orthodoxy", "Dogma reverence"],
        "exampleEn": "Modern art movements embraced bold iconoclasm, shattering centuries of academic conventions.",
        "exampleBn": "আধুনিক শিল্প আন্দোলনগুলো শত শত বছরের প্রাতিষ্ঠানিক রীতি চূর্ণ করে সাহসী প্রথাভাঙ্গা চেতনাকে বরণ করেছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Sciolism",
        "pos": "Noun",
        "phonetic": "/ˈsaɪ.ə.lɪz.əm/ (সায়োলিজম)",
        "meaningBn": "অল্পবিদ্যা জাহির করা; অগভীর জ্ঞান নিয়ে পণ্ডিতির ভান",
        "synonyms": ["Superficial knowledge", "Shallow pretense of learning"],
        "antonyms": ["Profound erudition", "Deep scholarship"],
        "exampleEn": "The debate was undermined by the loud sciolism of participants who had only skimmed headlines.",
        "exampleBn": "বিতর্কটি কেবল শিরোনাম দেখে জ্ঞান জাহির করা আলোচকদের অগভীর পণ্ডিতির কারণে মাটি হয়ে গিয়েছিল।",
        "category": "Academic & Scholarly"
    },
    {
        "word": "Gargoyle",
        "pos": "Noun",
        "phonetic": "/ˈɡɑːr.ɡɔɪl/ (গারগয়েল)",
        "meaningBn": "প্রাচীন ভবনের ছাদে স্থাপিত পানি নিষ্কাশনের বিকটাকার অদ্ভুত মূর্তিযুক্ত মুখ",
        "synonyms": ["Waterspout carving", "Grotesque stone sculpture"],
        "antonyms": ["Smooth drainage pipe"],
        "exampleEn": "Stone gargoyles peered menacingly from the eaves of Notre-Dame, funneling rainwater safely away.",
        "exampleBn": "নটর ড্যামের ছাদের কিনারা থেকে পাথরের তৈরি গারগয়েলগুলো বৃষ্টির পানি নিরাপদে দূরে বের করে দিত।",
        "category": "Literature & Arts"
    }
]

with open('next_part3_sub2.json', 'w', encoding='utf-8') as f:
    json.dump(batch3_sub2, f, ensure_ascii=False, indent=2)
print("Part 3 sub 2 written:", len(batch3_sub2))

with open('next_part3_sub1.json') as f1, open('next_part3_sub2.json') as f2:
    w1 = json.load(f1)
    w2 = json.load(f2)

full3 = w1 + w2
for i, item in enumerate(full3):
    item['id'] = 6501 + i
    item['packId'] = 66

with open('next_part3.json', 'w', encoding='utf-8') as fout:
    json.dump(full3, fout, ensure_ascii=False, indent=2)
print("Complete next_part3.json ready with entries:", len(full3))

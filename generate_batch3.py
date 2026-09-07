import json

with open('app/src/main/assets/dictionary_1000.json', 'r', encoding='utf-8') as f:
    existing_data = json.load(f)

existing_words = set(x['word'].strip().lower() for x in existing_data)
print(f"Existing word count: {len(existing_data)}")

# High-frequency exam words batch (350 words)
# Each entry is a dict with authentic exam vocabulary
candidates = [
    # 1 - 20
    {
        "word": "Aberration",
        "pos": "Noun",
        "phonetic": "/ˌæb.əˈreɪ.ʃən/ (অ্যাবারেশন)",
        "meaningBn": "স্বাভাবিক অবস্থা থেকে বিচ্যুতি বা অস্বাভাবিকতা",
        "synonyms": ["Anomaly", "Deviation", "Divergence", "Irregularity"],
        "antonyms": ["Normality", "Regularity", "Conformity"],
        "exampleEn": "The sudden drop in temperature was an environmental aberration for this time of year.",
        "exampleBn": "বছরের এই সময়ে তাপমাত্রার হঠাৎ পতন একটি পরিবেশগত অস্বাভাবিকতা ছিল।",
        "category": "GRE / BCS High-Frequency"
    },
    {
        "word": "Abscond",
        "pos": "Verb",
        "phonetic": "/əbˈskɒnd/ (আবসকন্ড)",
        "meaningBn": "আত্মগোপন করা বা গোপনে পালিয়ে যাওয়া",
        "synonyms": ["Flee", "Escape", "Decamp", "Bolts"],
        "antonyms": ["Remain", "Stay", "Appear"],
        "exampleEn": "The cashier tried to abscond with the bank's daily earnings.",
        "exampleBn": "ক্যাশিয়ার ব্যাংকের দৈনিক উপার্জন নিয়ে গোপনে পালিয়ে যাওয়ার চেষ্টা করেছিল।",
        "category": "Bank & Admission"
    },
    {
        "word": "Abstemious",
        "pos": "Adjective",
        "phonetic": "/æbˈstiː.mi.əs/ (অ্যাবস্টিমিয়াস)",
        "meaningBn": "আহার ও পানীয় গ্রহণের ক্ষেত্রে সংযমী বা পরিমিতচারী",
        "synonyms": ["Temperate", "Moderate", "Austere", "Ascetic"],
        "antonyms": ["Gluttonous", "Self-indulgent", "Intemperate"],
        "exampleEn": "Despite the lavish banquet, the monk remained abstemious in his eating.",
        "exampleBn": "স্বাদুকর ভোজ থাকা সত্ত্বেও সন্ন্যাসী তাঁর খাবারে পরিমিতচারী ছিলেন।",
        "category": "GRE / BCS High-Frequency"
    },
    {
        "word": "Acumen",
        "pos": "Noun",
        "phonetic": "/ˈæk.jə.mən/ (অ্যাকিউমেন)",
        "meaningBn": "সূক্ষ্ম বিচারবুদ্ধি বা ব্যবসায়িক ও বুদ্ধিবৃত্তিক বিচক্ষণতা",
        "synonyms": ["Astuteness", "Shrewdness", "Insight", "Acuteness"],
        "antonyms": ["Ignorance", "Stupidity", "Obtuseness"],
        "exampleEn": "Her financial acumen helped the startup become profitable in six months.",
        "exampleBn": "তাঁর আর্থিক বিচক্ষণতা নতুন কোম্পানিটিকে ছয় মাসের মধ্যে লাভজনক হতে সাহায্য করেছিল।",
        "category": "Bank & Corporate"
    },
    {
        "word": "Admonish",
        "pos": "Verb",
        "phonetic": "/ədˈmɒn.ɪʃ/ (এডমনিশ)",
        "meaningBn": "মৃদু তিরস্কার করা বা সতর্ক করে দেওয়া",
        "synonyms": ["Reprimand", "Chide", "Warn", "Advise"],
        "antonyms": ["Praise", "Applaud", "Commend"],
        "exampleEn": "The teacher had to admonish the students for making noise during lectures.",
        "exampleBn": "লেকচারের সময় শব্দ করার জন্য শিক্ষককে শিক্ষার্থীদের মৃদু তিরস্কার করতে হয়েছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Aggrandize",
        "pos": "Verb",
        "phonetic": "/əˈɡræn.daɪz/ (অ্যাগ্র্যান্ডাইজ)",
        "meaningBn": "ক্ষমতা, সম্মান বা মর্যাদা বৃদ্ধি করা",
        "synonyms": ["Exalt", "Elevate", "Dignify", "Enlarge"],
        "antonyms": ["Belittle", "Degrade", "Diminish"],
        "exampleEn": "The politician sought to aggrandize himself by taking credit for others' work.",
        "exampleBn": "রাজনীতিবিদ অন্যদের কাজের কৃতিত্ব নিয়ে নিজের মর্যাদা বাড়াতে চেয়েছিলেন।",
        "category": "GRE / BCS High-Frequency"
    },
    {
        "word": "Alacrity",
        "pos": "Noun",
        "phonetic": "/əˈlæk.rə.ti/ (অ্যালাক্রিটি)",
        "meaningBn": "উৎসুক্যপূর্ণ তৎপরতা বা চটপটে আগ্রহ",
        "synonyms": ["Eagerness", "Promptness", "Enthusiasm", "Readiness"],
        "antonyms": ["Apathy", "Reluctance", "Sluggishness"],
        "exampleEn": "She accepted the job offer with impressive alacrity.",
        "exampleBn": "তিনি চিত্তাকর্ষক আগ্রহ ও তৎপরতার সাথে চাকরির অফারটি গ্রহণ করেছিলেন।",
        "category": "IELTS & GRE"
    },
    {
        "word": "Anachronism",
        "pos": "Noun",
        "phonetic": "/əˈnæk.rə.nɪz.əm/ (অ্যানাক্রোনিজম)",
        "meaningBn": "কালবৈষম্য বা যুগের সাথে অপ্রাসঙ্গিক কোনো বস্তু/ধারণা",
        "synonyms": ["Misplacement", "Solecism", "Incongruity"],
        "antonyms": ["Synchronism", "Timeliness"],
        "exampleEn": "Using a typewriter in a modem digital agency is an absolute anachronism.",
        "exampleBn": "একটি আধুনিক ডিজিটাল সংস্থায় টাইপরাইটার ব্যবহার করা সম্পূর্ণ কালবৈষম্য।",
        "category": "GRE & Admission"
    },
    {
        "word": "Antipathy",
        "pos": "Noun",
        "phonetic": "/ænˈtɪp.ə.θi/ (অ্যান্টিপ্যাথি)",
        "meaningBn": "তীব্র ঘৃণা বা বিদ্বেষপূর্ণ মনোভাব",
        "synonyms": ["Hostility", "Aversion", "Animosity", "Hatred"],
        "antonyms": ["Sympathy", "Affinity", "Liking"],
        "exampleEn": "There was a mutual antipathy between the two competing executives.",
        "exampleBn": "প্রতিদ্বন্দ্বী দুই নির্বাহীর মধ্যে পারস্পরিক তীব্র বিদ্বেষ ছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Apathy",
        "pos": "Noun",
        "phonetic": "/ˈæp.ə.θi/ (অ্যাপাথি)",
        "meaningBn": "উদাসীনতা বা কোনো বিষয়ে আগ্রহের অভাব",
        "synonyms": ["Indifference", "Unconcern", "Passivity", "Lethargy"],
        "antonyms": ["Passionate interest", "Enthusiasm", "Concern"],
        "exampleEn": "Voter apathy is a growing concern for democratic institutions.",
        "exampleBn": "ভোটারদের উদাসীনতা গণতান্ত্রিক প্রতিষ্ঠানগুলোর জন্য একটি ক্রমবর্ধমান উদ্বেগের বিষয়।",
        "category": "BCS & Bank"
    },
    {
        "word": "Arduous",
        "pos": "Adjective",
        "phonetic": "/ˈɑː.dʒu.əs/ (আর্ডুয়াস)",
        "meaningBn": "কঠিন, পরিশ্রমসাধ্য ও কষ্টকর",
        "synonyms": ["Onerous", "Strenuous", "Grueling", "Laborious"],
        "antonyms": ["Easy", "Effortless", "Simple"],
        "exampleEn": "Climbing the steep Himalayan trail was an arduous journey.",
        "exampleBn": "হিমালয়ের খাড়া পথ বেয়ে ওঠা একটি অত্যন্ত কষ্টকর যাত্রা ছিল।",
        "category": "IELTS & GRE"
    },
    {
        "word": "Ascetic",
        "pos": "Adjective",
        "phonetic": "/əˈset.ɪk/ (অ্যাসেটিক)",
        "meaningBn": "কঠোর আত্মসংযমী বা তপস্বীসুলভ জীবনযাপনকারী",
        "synonyms": ["Austere", "Abstemious", "Puritanical", "Monastic"],
        "antonyms": ["Hedonistic", "Sybaritic", "Self-indulgent"],
        "exampleEn": "The hermit led an ascetic life in a small mountain cabin.",
        "exampleBn": "সন্ন্যাসী পাহাড়ের একটি ছোট কুঁড়েঘরে কঠোর তপস্বীসুলভ জীবন যাপন করতেন।",
        "category": "GRE & Admission"
    },
    {
        "word": "Assiduous",
        "pos": "Adjective",
        "phonetic": "/əˈsɪd.ju.əs/ (অ্যাসিডুয়াস)",
        "meaningBn": "অধ্যবসায়ী বা অত্যন্ত পরিশ্রমী ও মনোযোগী",
        "synonyms": ["Diligent", "Meticulous", "Painstaking", "Sedulous"],
        "antonyms": ["Lazy", "Negligent", "Careless"],
        "exampleEn": "Through assiduous research, the scientist discovered a new compound.",
        "exampleBn": "অধ্যবসায়ী গবেষণার মাধ্যমে বিজ্ঞানী একটি নতুন যৌগ আবিষ্কার করেছিলেন।",
        "category": "BCS & GRE"
    },
    {
        "word": "Audacious",
        "pos": "Adjective",
        "phonetic": "/ɔːˈdeɪ.ʃəs/ (অডেশাস)",
        "meaningBn": "অত্যন্ত সাহসী বা স্পর্ধাপূর্ণ",
        "synonyms": ["Bold", "Daring", "Intrepid", "Reckless"],
        "antonyms": ["Timid", "Cowardly", "Cautious"],
        "exampleEn": "The young entrepreneur launched an audacious campaign against established rivals.",
        "exampleBn": "তরুণ উদ্যোক্তা প্রতিষ্ঠিত প্রতিদ্বন্দ্বীদের বিরুদ্ধে এক স্পর্ধাপূর্ণ অভিযান শুরু করেছিলেন।",
        "category": "BCS & GRE"
    },
    {
        "word": "Austere",
        "pos": "Adjective",
        "phonetic": "/ɔːˈstɪər/ (অস্টিয়ার)",
        "meaningBn": "কঠোর, অনালঙ্কার বা কৃচ্ছ্রসাধনকারী",
        "synonyms": ["Severe", "Stern", "Unadorned", "Ascetic"],
        "antonyms": ["Lavish", "Ornate", "Gentle"],
        "exampleEn": "The room was furnished in an austere style with only a bed and table.",
        "exampleBn": "ঘরটি অনালঙ্কার শৈলীতে কেবল একটি বিছানা ও টেবিল দিয়ে সজ্জিত ছিল।",
        "category": "IELTS & BCS"
    },
    {
        "word": "Banal",
        "pos": "Adjective",
        "phonetic": "/bəˈnɑːl/ (ব্যানাল)",
        "meaningBn": "তুচ্ছ, বস্তাপচা বা গতানুগতিক",
        "synonyms": ["Trite", "Hackneyed", "Commonplace", "Clichéd"],
        "antonyms": ["Original", "Fresh", "Novel"],
        "exampleEn": "The film had a banal storyline that bored most of the audience.",
        "exampleBn": "মুভিটির গল্পটি ছিল বস্তাপচা, যা বেশিরভাগ দর্শককে বিরক্ত করেছিল।",
        "category": "GRE & BCS"
    },
    {
        "word": "Belligerent",
        "pos": "Adjective",
        "phonetic": "/bəˈlɪdʒ.ər.ənt/ (বেলিজারেন্ট)",
        "meaningBn": "যুদ্ধংদেহী বা ঝগড়াটে ও আক্রমণাত্মক",
        "synonyms": ["Aggressive", "Combative", "Pugnacious", "Hostile"],
        "antonyms": ["Peaceful", "Friendly", "Conciliatory"],
        "exampleEn": "His belligerent tone angered everyone present at the meeting.",
        "exampleBn": "তাঁর যুদ্ধংদেহী কণ্ঠস্বর সভায় উপস্থিত সবাইকে ক্ষুব্ধ করেছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Benevolent",
        "pos": "Adjective",
        "phonetic": "/bəˈnev.əl.ənt/ (বেনেভোলেন্ট)",
        "meaningBn": "পোপকারী, দয়ালু বা হিতৈষী",
        "synonyms": ["Charitable", "Altruistic", "Philanthropic", "Kind"],
        "antonyms": ["Malevolent", "Spiteful", "Hostile"],
        "exampleEn": "A benevolent donor funded the construction of the public library.",
        "exampleBn": "একজন পরোপকারী দাতা পাবলিক লাইব্রেরি নির্মাণের অর্থায়ন করেছিলেন।",
        "category": "Admission & Bank"
    },
    {
        "word": "Bolster",
        "pos": "Verb",
        "phonetic": "/ˈbəʊl.stər/ (বোলস্টার)",
        "meaningBn": "শক্তিশালী করা, সমর্থন দেওয়া বা চাঙ্গা করা",
        "synonyms": ["Strengthen", "Reinforce", "Fortify", "Support"],
        "antonyms": ["Undermine", "Weaken", "Impair"],
        "exampleEn": "The new evidence helped bolster the defense attorney's argument.",
        "exampleBn": "নতুন প্রমাণটি আসামিপক্ষের উকিলের যুক্তিকে শক্তিশালী করতে সাহায্য করেছিল।",
        "category": "GRE & IELTS"
    },
    {
        "word": "Bombastic",
        "pos": "Adjective",
        "phonetic": "/bɒmˈbæs.tɪk/ (বম্বাস্টিক)",
        "meaningBn": "আড়ম্বরপূর্ণ শব্দবহুল কিন্তু মূল ভাববর্জিত",
        "synonyms": ["Pompous", "Grandiloquent", "High-flown", "Pretentious"],
        "antonyms": ["Unpretentious", "Restrained", "Simple"],
        "exampleEn": "The candidate's bombastic speech failed to convince thoughtful voters.",
        "exampleBn": "প্রার্থীর আড়ম্বরপূর্ণ ফাঁপা বক্তব্য চিন্তাশীল ভোটারদের প্রমুগ্ধ করতে ব্যর্থ হয়েছিল।",
        "category": "GRE & Admission"
    }
]

print(f"Candidates prepared: {len(candidates)}")

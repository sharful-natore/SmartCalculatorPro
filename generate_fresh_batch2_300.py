import json, re

# Load existing words
existing_words = set()

with open('app/src/main/assets/dictionary_1000.json', 'r', encoding='utf-8') as f:
    data = json.load(f)
    for w in data:
        if 'word' in w and isinstance(w['word'], str):
            existing_words.add(w['word'].strip().lower())

with open('app/src/main/java/com/example/ui/screens/tools/VocabularyDataPacks.kt', 'r', encoding='utf-8') as f:
    content = f.read()
    for m in re.findall(r'VocabWord\s*\(\s*\"[^\"]+\"\s*,\s*\"([^\"]+)\"', content):
        existing_words.add(m.strip().lower())

with open('app/src/main/java/com/example/ui/screens/tools/VocabularyHighFrequencyDataset.kt', 'r', encoding='utf-8') as f:
    content = f.read()
    for t in re.findall(r'Triple\s*\(\s*\"([^\"]+)\"', content):
        existing_words.add(t.strip().lower())

print(f"Total existing words in DB: {len(existing_words)}")

# Candidate generator list 1
candidates_b2 = [
    # A
    ("Abate", "/əˈbeɪt/", "Verb", "হ্রাস পাওয়া, প্রাবল্য কমানো", "The storm showed no sign of abating.", "ঝড়টির হ্রাস পাওয়ার কোনো লক্ষণ ছিল না।", ["Lessen", "Subside", "Diminish"], ["Increase", "Intensify"], "IELTS"),
    ("Aberrant", "/əˈber.ənt/", "Adj", "পথভ্রষ্ট, নীতিবহির্ভূত বা অস্বাভাবিক", "His aberrant behavior worried his parents.", "তার অস্বাভাবিক আচরণ তার মা-বাবাকে চিন্তিত করেছিল।", ["Abnormal", "Deviant", "Atypical"], ["Normal", "Typical"], "GRE"),
    ("Abeyance", "/əˈbeɪ.əns/", "Noun", "স্থগিতাবস্থা, সাময়িক নিষ্ক্রিয়তা", "The project was held in abeyance.", "প্রকল্পটি স্থগিতাবস্থায় রাখা হয়েছিল।", ["Suspension", "Dormancy", "Delay"], ["Continuation", "Action"], "GRE"),
    ("Abjure", "/əbˈdʒʊər/", "Verb", "শপথপূর্বক পরিত্যাগ করা", "He abjured his allegiance to the king.", "সে রাজার প্রতি তার আনুগত্য শপথপূর্বক ত্যাগ করল।", ["Renounce", "Recant", "Relinquish"], ["Affirm", "Claim"], "GRE"),
    ("Abrade", "/əˈbreɪd/", "Verb", "ঘষে ক্ষয় করা, ছাল তোলা", "Ropes abraded his wrists.", "দড়ি তার কব্জি ঘষে ছাল তুলে দিয়েছিল।", ["Scrape", "Erode", "Wear down"], ["Smooth", "Protect"], "Medical"),
    ("Abscond", "/æbˈskɒnd/", "Verb", "গোপনে চম্পট দেওয়া, পালিয়ে যাওয়া", "The cashier absconded with company funds.", "ক্যাশিয়ার কোম্পানির তহবিল নিয়ে চম্পট দিল।", ["Flee", "Escape", "Bolt"], ["Remain", "Stay"], "Legal"),
    ("Abstemious", "/æbˈstiː.mi.əs/", "Adj", "সংযমী, পরিমিতাহারী", "He led an abstemious lifestyle.", "সে এক সংযমী পরিমিত জীবনযাপন করত।", ["Moderate", "Temperate", "Sober"], ["Indulgent", "Gluttonous"], "GRE"),
    ("Abstruse", "/æbˈstruːs/", "Adj", "গূঢ়, জটিল ও দুর্বোধ্য", "An abstruse philosophical treatise.", "এক গূঢ় জটিল দার্শনিক আলোচনা।", ["Obscure", "Arcane", "Esoteric"], ["Clear", "Simple"], "Academic"),
    ("Accretion", "/əˈkriː.ʃən/", "Noun", "ক্রমবৃদ্ধি, পরিবর্ধন", "An accretion of ice on the wings.", "ডানার ওপর বরফের ক্রমবৃদ্ধি।", ["Accumulation", "Growth"], ["Decrease", "Erosion"], "Science"),
    ("Acerbic", "/əˈsɜː.bɪk/", "Adj", "কটূভাষী, উগ্র বা তিক্ত", "He had an acerbic sense of humor.", "তার এক কটূভাষী তিক্ত রসিকতার অভ্যাস ছিল।", ["Caustic", "Sharp", "Bitacting"], ["Mild", "Sweet"], "GRE"),

    # B
    ("Balk", "/bɔːk/", "Verb", "বাধা দেওয়া, অনিচ্ছা প্রকাশ করা", "He balked at the high cost.", "অতিরিক্ত খরচে সে অনিচ্ছা প্রকাশ করল।", ["Hesitate", "Refuse", "Flinch"], ["Accept", "Proceed"], "Spoken"),
    ("Banal", "/bəˈnɑːl/", "Adj", "গতানুগতিক, চর্বিতচর্বণ বা তুচ্ছ", "A banal conversation about the weather.", "আবহাওয়া নিয়ে এক গতানুগতিক তুচ্ছ আলাপ।", ["Trite", "Hackneyed", "Cliché"], ["Original", "Fresh"], "GRE"),
    ("Bane", "/beɪn/", "Noun", "সর্বনাশের কারণ, অভিশাপ", "Stress is the bane of modern life.", "মানসিক চাপ আধুনিক জীবনের এক অভিশাপ।", ["Curse", "Blight", "Ruination"], ["Blessing", "Boon"], "Literature"),
    ("Bedlam", "/ˈbed.ləm/", "Noun", "শোরগোল, হট্টগোল বা বিশৃঙ্খলা", "Bedlam broke out when news spread.", "সংবাদটি ছড়িয়ে পড়লে হট্টগোল শুরু হয়ে গেল।", ["Chaos", "Pandemonium", "Uproar"], ["Order", "Calm"], "General"),
    ("Belie", "/bɪˈlaɪ/", "Verb", "মিথ্যা প্রতিপন্ন করা, ভ্রান্ত ধারণা দেওয়া", "Her energetic demeanor belied her age.", "তার প্রাণবন্ত রূপ তার বয়সের ভ্রান্ত ধারণা দিল।", ["Contradict", "Misrepresent"], ["Prove", "Reveal"], "GRE"),
    ("Bemoan", "/bɪˈməʊn/", "Verb", "বিলাপ করা, আফসোস করা", "They bemoaned the lack of progress.", "অগ্রগতির অভাবে তারা আফসোস করল।", ["Lament", "Mourn", "Deplore"], ["Celebrate", "Rejoice"], "Literature"),
    ("Beneficent", "/bəˈnef.ɪ.sənt/", "Adj", "পরোপকারী, কল্যাণময়", "A beneficent king ruled the realm.", "এক পরোপকারী রাজা রাজ্য শাসন করতেন।", ["Kind", "Generous", "Altruistic"], ["Malevolent", "Cruel"], "BCS"),
    ("Bereft", "/bɪˈreft/", "Adj", "শূন্য, বঞ্চিত বা নিঃস্ব", "He was left bereft of hope.", "সে আশা থেকে সম্পূর্ণ বর্জিত হলো।", ["Deprived", "Devoid", "Lacking"], ["Full", "Abounding"], "Literature"),
    ("Blandishment", "/ˈblæn.dɪʃ.mənt/", "Noun", "তোষামোদ, প্রলুব্ধকর মিষ্টি কথা", "She resisted all his blandishments.", "সে তার সকল তোষামোদি প্রলুব্ধকর কথা উপেক্ষা করল।", ["Flattery", "Coaxing", "Wheedling"], [], "GRE"),
    ("Bolster", "/ˈbəʊl.stər/", "Verb", "শক্তি জোগানো, চাঙ্গা করা", "More evidence is needed to bolster the claim.", "দাবিটি চাঙ্গা করতে আরও প্রমাণ প্রয়োজন।", ["Support", "Strengthen", "Reinforce"], ["Undermine", "Weaken"], "IELTS"),

    # C
    ("Cacophonous", "/kəˈkɒf.ə.nəs/", "Adj", "বেসুরো, বিকট শব্দময়", "A cacophonous noise came from construction.", "নির্মাণকাজ থেকে এক বিকট শব্দ আসছিল।", ["Harsh", "Discordant", "Raucous"], ["Harmonious", "Euphonious"], "GRE"),
    ("Calumny", "/ˈkæl.əm.ni/", "Noun", "অপবাদ, মিথ্যা কলঙ্ক", "She was victim of malicious calumny.", "সে হিংসাত্মক অপবাদের শিকার হয়েছিল।", ["Slander", "Defamation", "Libel"], ["Praise", "Compliment"], "Legal"),
    ("Canard", "/kəˈnɑːd/", "Noun", "গুজব, ঝুটা খবর", "The rumor was a complete canard.", "গুজবটি ছিল এক ঝুটা খবর।", ["False rumor", "Hoax"], [], "News"),
    ("Capricious", "/kəˈprɪʃ.əs/", "Adj", "খামখেয়ালী, ক্ষণে ক্ষণে বদলানো", "Island weather is capricious.", "দ্বীপের আবহাওয়া খামখেয়ালী।", ["Whimsical", "Fickle", "Erratic"], ["Predictable", "Stable"], "IELTS"),
    ("Castigate", "/ˈkæs.tɪ.ɡeɪt/", "Verb", "তীব্র ভর্ৎসনা করা", "The manager castigated staff for delay.", "ম্যানেজার দেরির জন্য কর্মীদের তীব্র ভর্ৎসনা করলেন।", ["Rebuke", "Reprimand", "Chastise"], ["Praise", "Commend"], "GRE"),
    ("Caustic", "/ˈkɔː.stɪk/", "Adj", "দাহ্য, কটূভাষী বা তীব্র তীক্ষ্ণ", "He made a caustic remark.", "সে এক তীক্ষ্ণ কটূ মন্তব্য করল।", ["Sarcastic", "Acerbic", "Biting"], ["Gentle", "Kind"], "GRE"),
    ("Censure", "/ˈsen.ʃər/", "Verb", "নিন্দা করা, তিরস্কার করা", "The council voted to censure the mayor.", "কাউন্সিল মেয়রকে তিরস্কার করতে ভোট দিল।", ["Condemn", "Criticize", "Reprimand"], ["Praise", "Applaud"], "Politics"),
    ("Chauvinism", "/ˈʃəʊ.vɪ.nɪ.zəm/", "Noun", "অন্ধ স্বদেশপ্রীতি, উগ্র জাত্যাভিমান", "He showed narrow chauvinism.", "সে এক সংকীর্ণ উগ্র জাত্যাভিমান দেখাল।", ["Fanaticism", "Jingoism"], [], "Sociology"),
    ("Chicanery", "/ʃɪˈkeɪ.nər.i/", "Noun", "প্রতারণা, আইনি বা রাজনৈতিক প্যাঁচ", "Financial chicanery ruined the firm.", "আর্থিক প্রতারণা ফার্মটিকে ধ্বংস করল।", ["Deception", "Trickery", "Subterfuge"], ["Honesty", "Candor"], "Legal"),
    ("Clamor", "/ˈklæm.ər/", "Noun", "কোলাহল, তীব্র হাঁকডাক", "A clamor arose for tax reform.", "কর সংস্কারের জন্য এক তীব্র দাবি উঠল।", ["Uproar", "Outcry", "Din"], ["Silence", "Quiet"], "Politics"),

    # D - F
    ("Dally", "/ˈdæl.i/", "Verb", "সময় নষ্ট করা, ঢিলেমি করা", "Don't dally on the way home.", "বাড়ির পথে ঢিলেমি কোরো না।", ["Loiter", "Delay", "Dawdle"], ["Hurry", "Hasten"], "Spoken"),
    ("Daunt", "/dɔːnt/", "Verb", "ভীত করা, নিরুৎসাহিত করা", "Dangerous roads did not daunt him.", "বিপজ্জনক রাস্তা তাকে ভীত করতে পারেনি।", ["Intimidate", "Discourage", "Deter"], ["Encourage", "Inspirit"], "IELTS"),
    ("Dearth", "/dɜːθ/", "Noun", "আকাল, চরম অভাব", "There is a dearth of skilled labor.", "দক্ষ শ্রমিকের চরম আকাল রয়েছে।", ["Scarcity", "Shortage", "Paucity"], ["Abundance", "Surplus"], "Academic"),
    ("Debacle", "/dɪˈbɑː.kəl/", "Noun", "পতন, মহাবিপর্যয় বা পরাজয়", "The product launch was a total debacle.", "পণ্যটির উদ্বোধন ছিল এক মহাবিপর্যয়।", ["Disaster", "Fiasco", "Collapse"], ["Success", "Triumph"], "Business"),
    ("Decorous", "/ˈdek.ər.əs/", "Adj", "শালীন, শিষ্ট ও মার্জিত", "They behaved in a decorous manner.", "তারা শালীন মার্জিত আচরণ করল।", ["Proper", "Polite", "Seemly"], ["Improper", "Rude"], "Literature"),
    ("Defame", "/dɪˈfeɪm/", "Verb", "মানহানি করা, কলঙ্ক ছড়ানো", "The article sought to defame him.", "প্রবন্ধটি তার মানহানি করার চেষ্টা করল।", ["Slander", "Malign", "Libel"], ["Praise", "Honor"], "Legal"),
    ("Deft", "/deft/", "Adj", "দক্ষ, চটপটে ও নিপুণ", "With a deft motion, she caught ball.", "এক চটপটে কৌশলে সে বলটি লুফে নিল।", ["Skillful", "Nimble", "Adept"], ["Clumsy", "Awkward"], "Sports"),
    ("Delineation", "/dɪˌlɪn.iˈeɪ.ʃən/", "Noun", "সীমানা নির্ধারণ, সঠিক বর্ণনা", "Clear delineation of roles is vital.", "দায়িত্বের সঠিক রূপরেখা অত্যন্ত গুরুত্বপূর্ণ।", ["Description", "Outline", "Depiction"], [], "Management"),
    ("Delude", "/dɪˈluːd/", "Verb", "প্রতারিত করা, ভুল ধারণায় রাখা", "Don't delude yourself into thinking it's easy.", "সহজ ভেবে নিজেকে প্রতারিত কোরো না।", ["Deceive", "Mislead", "Trick"], ["Enlighten"], "Spoken"),
    ("Demur", "/dɪˈmɜːr/", "Verb", "দ্বিধা করা, আপত্তি জানানো", "She demurred at the suggestion.", "প্রস্তাবটিতে সে আপত্তি জানাল।", ["Object", "Protest", "Hesitate"], ["Agree", "Consent"], "GRE")
]

print("Batch 2 candidates prepared count:", len(candidates_b2))


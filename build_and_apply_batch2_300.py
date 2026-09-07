import json, re

# 1. Existing words list
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

print(f"Loaded {len(existing_words)} existing unique words.")

# Candidate raw words list (300 words)
raw_batch_2 = [
    ("Acquiescence", "/ˌæk.wiˈes.əns/", "Noun", "নীরব সম্মতি, মানিয়া লওয়া", "Her acquiescence surprised everyone.", "তার নীরব সম্মতি সবাইকে অবাক করল।", ["Consent", "Compliance"], ["Refusal", "Dissent"], "Academic"),
    ("Acrimony", "/ˈæk.rɪ.mə.ni/", "Noun", "উগ্রতা, তিক্ততা বা শত্রুতা", "The meeting ended in acrimony.", "সভাটি তিক্ততায় শেষ হলো।", ["Bitterness", "Rancor"], ["Harmony", "Goodwill"], "GRE"),
    ("Adulation", "/ˌæd.jəˈleɪ.ʃən/", "Noun", "অতিরিক্ত প্রশংসা, তোষামোদ", "The star enjoyed the adulation of fans.", "তারকাটি ভক্তদের তোষামোদ উপভোগ করলেন।", ["Flattery", "Worship"], ["Criticism", "Condemnation"], "GRE"),
    ("Advocate", "/ˈæd.və.keɪt/", "Verb", "সমর্থন করা, ওকালতি করা", "He advocates human rights.", "সে মানবাধিকারের সমর্থন করে।", ["Support", "Champion"], ["Oppose", "Attack"], "IELTS"),
    ("Affliction", "/əˈflɪk.ʃən/", "Noun", "যন্ত্রণা, কষ্ট বা পীড়া", "Poverty is a great affliction.", "দারিদ্র্য এক চরম যন্ত্রণা।", ["Suffering", "Distress"], ["Comfort", "Blessing"], "Literature"),
    ("Agile", "/ˈædʒ.aɪl/", "Adj", "চটপটে, দ্রুত বা চঞ্চল", "An agile dancer leaped gracefully.", "এক চটপটে নর্তকী চমৎকার লাফ দিল।", ["Nimble", "Lithe"], ["Clumsy", "Sluggish"], "Spoken"),
    ("Agitation", "/ˌædʒ.ɪˈteɪ.ʃən/", "Noun", "উদ্বেগ, আলোড়ন বা অস্থিরতা", "She hid her agitation well.", "সে তার অস্থিরতা সুন্দরভাবে লুকালো।", ["Anxiety", "Turbulence"], ["Calmness", "Peace"], "Psychology"),
    ("Alacrity", "/əˈlæk.rə.ti/", "Noun", "তৎপরতা, আগ্রহ ও চটপটে ভাব", "He responded with alacrity.", "সে তাৎক্ষণিক আগ্রহ নিয়ে উত্তর দিল।", ["Eagerness", "Promptness"], ["Apathy", "Slowness"], "GRE"),
    ("Alienate", "/ˈeɪ.li.ə.neɪt/", "Verb", "পর করে দেওয়া, দূরত্ব সৃষ্টি করা", "His arrogance alienated his friends.", "তার অহংকার বন্ধুদের দূরে ঠেলে দিল।", ["Estrange", "Isolate"], ["Unite", "Reconcile"], "Sociology"),
    ("Allegiance", "/əˈliː.dʒəns/", "Noun", "আনুগত্য, বাধ্যতা", "They pledged allegiance to nation.", "তারা জাতির প্রতি আনুগত্যের শপথ নিল।", ["Loyalty", "Fidelity"], ["Treachery", "Treason"], "Politics"),

    ("Allude", "/əˈluːd/", "Verb", "ইঙ্গিত করা, পরোক্ষ উল্লেখ করা", "He alluded to problems in firm.", "সে প্রতিষ্ঠানের সমস্যার দিকে ইঙ্গিত করল।", ["Hint", "Refer", "Suggest"], [], "Academic"),
    ("Allure", "/əˈlʊər/", "Noun", "আকর্ষণ, মোহ বা প্রলোভন", "The allure of big city life.", "বড় শহরের জীবনের আকর্ষণ।", ["Attraction", "Charm", "Enticement"], ["Repulsion"], "Literature"),
    ("Aloof", "/əˈluːف/", "Adj", "একাকী, অমিশুক বা নিঃসঙ্গ", "He remained aloof from group.", "সে দল থেকে নিজেকে দূরে রাখল।", ["Distant", "Detached"], ["Friendly", "Sociable"], "Psychology"),
    ("Altruistic", "/ˌæl.truˈɪs.tɪk/", "Adj", "পরোপকারী, স্বার্থহীন", "Altruistic motives drove her.", "স্বার্থহীন উদ্দেশ্য তাকে চালিত করল।", ["Unselfish", "Benevolent"], ["Selfish", "Greedy"], "BCS"),
    ("Ambiance", "/ˈæm.bi.əns/", "Noun", "পরিবেশ, মেজাজ বা পরিবেশের আমেজ", "The restaurant has a cozy ambiance.", "রেস্তোরাঁটিতে এক আরামদায়ক আমেজ রয়েছে।", ["Atmosphere", "Aura"], [], "Spoken"),
    ("Ambiguity", "/ˌæm.bɪˈɡjuː.ə.ti/", "Noun", "অস্পষ্টতা, দ্ব্যর্থবোধকতা", "Avoid ambiguity in contracts.", "চুক্তিতে অস্পষ্টতা পরিহার করুন।", ["Unclearness", "Vagueness"], ["Clarity", "Explicitly"], "Academic"),
    ("Amelioration", "/əˌmiː.li.əˈreɪ.ʃən/", "Noun", "উন্নতিসাধন, কষ্টের লাঘব", "Amelioration of working conditions.", "কাজের পরিবেশের উন্নতিসাধন।", ["Improvement", "Enhancement"], ["Worsening"], "Academic"),
    ("Amiability", "/ˌeɪ.mi.əˈbɪl.ə.ti/", "Noun", "অমায়িকতা, মিষ্টতা", "Her amiability won hearts.", "তার অমায়িকতা মন জয় করে নিল।", ["Friendliness", "Geniality"], ["Hostility"], "Spoken"),
    ("Amicable", "/ˈæm.ɪ.kə.bəl/", "Adj", "শান্তিপূর্ণ, বন্ধুত্বপূর্ণ ও আপসপূর্ণ", "An amicable settlement was reached.", "এক শান্তিপূর্ণ সমঝোতায় পৌঁছানো গেল।", ["Friendly", "Harmonious"], ["Hostile", "Bitter"], "Legal"),
    ("Amnesty", "/ˈæm.nə.sti/", "Noun", "সাধারণ ক্ষমা, রাজক্ষমা", "Government declared amnesty for rebels.", "সরকার বিদ্রোহীদের জন্য সাধারণ ক্ষমা ঘোষণা করল।", ["Pardon", "Absolution"], ["Punishment"], "Legal"),

    ("Amplification", "/ˌæm.plɪ.fɪˈkeɪ.ʃən/", "Noun", "বর্ধিতকরণ, বিস্তার", "Amplification of sound signal.", "শব্দ সংকেতের বর্ধিতকরণ।", ["Expansion", "Magnification"], ["Reduction"], "Science"),
    ("Anachronism", "/əˈnæk.rə.nɪ.zəm/", "Noun", "কালবৈষম্য, সেকেলে বস্তু", "Pagers are an anachronism now.", "পেজার এখন এক কালবৈষম্য।", ["Chronological error", "Misplacement"], [], "History"),
    ("Analogous", "/əˈnæl.ə.ɡəs/", "Adj", "অনুরূপ, সমতুল্য", "Brain is analogous to computer.", "মস্তিষ্ক কম্পিউটারের সমতুল্য।", ["Similar", "Comparable"], ["Dissimilar", "Different"], "Academic"),
    ("Animosity", "/ˌæn.ɪˈmɒs.ə.ti/", "Noun", "বিদ্বেষ, বৈরিতা", "There is no animosity between us.", "আমাদের মধ্যে কোনো বিদ্বেষ নেই।", ["Hostility", "Hatred"], ["Goodwill", "Friendship"], "Spoken"),
    ("Annihilate", "/əˈnaɪ.ə.leɪt/", "Verb", "সম্পূর্ণ ধ্বংস করা, নির্মূল করা", "Bombing annihilated the town.", "বোমা বর্ষণ শহরটিকে সম্পূর্ণ ধ্বংস করল।", ["Destroy", "Wipe out"], ["Create", "Build"], "General"),
    ("Anomalous", "/əˈnɒm.ə.ləs/", "Adj", "ব্যতিক্রমী, নিয়মবহির্ভূত", "An anomalous test result raised doubt.", "এক ব্যতিক্রমী ফলাফল সন্দেহ জাগালো।", ["Abnormal", "Irregular"], ["Normal", "Regular"], "GRE"),
    ("Anomaly", "/əˈnɒm.ə.li/", "Noun", "ব্যতিক্রম, অসঙ্গতি", "An anomaly in the data.", "তথ্যের মধ্যে এক অসঙ্গতি।", ["Irregularity", "Deviation"], ["Normality"], "IELTS"),
    ("Antagonize", "/ænˈtæɡ.ə.naɪz/", "Verb", "বিরোধী করে তোলা, চটানো", "Don't antagonize your boss.", "আপনার বসকে বিরোধী করে তুলবেন না।", ["Provoke", "Alienate"], ["Pacify", "Placate"], "Spoken"),
    ("Anticipation", "/ænˌtɪs.ɪˈpeɪ.ʃən/", "Noun", "প্রত্যাশা, পূর্বাভাস বা উন্মুখতা", "Crowd waited in eager anticipation.", "জনতা ব্যাকুল প্রত্যাশায় অপেক্ষা করল।", ["Expectation", "Hope"], [], "Psychology"),
    ("Apathy", "/ˈæp.ə.θi/", "Noun", "উদাসীনতা, অনীহা বা অনিচ্ছা", "Voter apathy resulted in low turnout.", "ভোটারদের উদাসীনতায় কম ভোট পড়ল।", ["Indifference", "Unconcern"], ["Enthusiasm", "Interest"], "Politics"),

    ("Apex", "/ˈeɪ.peks/", "Noun", "চূড়া, শীর্ষবিন্দু", "He reached apex of career.", "সে ক্যারিয়ারের শীর্ষবিন্দুতে পৌঁছালো।", ["Peak", "Pinnacle", "Summit"], ["Nadir", "Bottom"], "BCS"),
    ("Aphorism", "/ˈæf.ə.rɪ.zəm/", "Noun", "নীতিবাক্য, সংক্ষিপ্ত সারোক্তি", "He quoted a famous aphorism.", "সে এক বিখ্যাত নীতিবাক্য উদ্ধৃত করল।", ["Maxim", "Saying", "Proverb"], [], "Literature"),
    ("Apocalypse", "/əˈpɒk.ə.lɪps/", "Noun", "মহাপ্রলয়, কেয়ামত বা ধ্বংসযজ্ঞ", "Nuclear war would be apocalypse.", "পারমাণবিক যুদ্ধ মহাপ্রলয় ডেকে আনবে।", ["Catastrophe", "Devastation"], [], "General"),
    ("Appease", "/əˈpiːz/", "Verb", "শান্ত করা, সন্তুষ্ট করা", "They tried to appease angry crowd.", "তারা ক্রুদ্ধ জনতাকে শান্ত করতে চেষ্টা করল।", ["Pacify", "Placate"], ["Provoke", "Enrage"], "IELTS"),
    ("Apprehension", "/ˌæp.rɪˈhen.ʃən/", "Noun", "আশঙ্কা, ভয় বা গ্রেফতার", "She felt apprehension before test.", "পরীক্ষার আগে সে আশঙ্কা বোধ করল।", ["Anxiety", "Dread"], ["Confidence"], "Spoken"),
    ("Approboration", "/ˌæp.rəˈbeɪ.ʃən/", "Noun", "অনুমোদন, প্রশংসা", "The project won approboration.", "প্রকল্পটি প্রশংসা লাভ করল।", ["Approval", "Praise"], ["Disapproval"], "Academic"),
    ("Arbitrary", "/ˈɑː.bɪ.trər.i/", "Adj", "খেয়ালখুশিমতো, অনিয়মিত", "An arbitrary decision annoyed staff.", "এক খেয়ালখুশি সিদ্ধান্ত কর্মীদের ক্ষুব্ধ করল।", ["Random", "Capricious"], ["Reasonable", "Systematic"], "Legal"),
    ("Arcane", "/ɑːˈkeɪn/", "Adj", "গূঢ়, রহস্যময় বা দুর্ভেদ্য", "Arcane rules confused newcomers.", "গূঢ় নিয়মগুলো নতুনদের বিভ্রান্ত করল।", ["Esoteric", "Obscure"], ["Common", "Clear"], "GRE"),
    ("Archaic", "/ɑːˈkeɪ.ɪk/", "Adj", "প্রাচীন, সেকেলে", "Archaic laws need update.", "প্রাচীন আইনগুলোর সংস্কার দরকার।", ["Outdated", "Obsolete"], ["Modern"], "History"),
    ("Ardent", "/ˈɑː.dənt/", "Adj", "উৎসুক, আকুল বা পরম অনুরাগী", "An ardent supporter of peace.", "শান্তির এক পরম অনুরাগী সমর্থক।", ["Passionate", "Fervent"], ["Apathetic"], "Literature"),

    ("Arduous", "/ˈɑː.dʒu.əs/", "Adj", "কষ্টসাধ্য, দুর্গম", "An arduous climb up mountain.", "পাহাড়ের ওপর এক দুর্গম চড়াই।", ["Strenuous", "Tough"], ["Easy"], "IELTS"),
    ("Aroma", "/əˈrəʊ.mə/", "Noun", "সৌরভ, মন মাতানো সুবাস", "Aroma of fresh coffee filled room.", "তাজা কফির সুবাস ঘর ভরিয়ে দিল।", ["Fragrance", "Scent"], ["Stench"], "Spoken"),
    ("Articulate", "/ɑːˈtɪk.jə.lət/", "Adj", "স্পষ্টভাষী, প্রাঞ্জল", "She is an articulate speaker.", "সে এক স্পষ্টভাষী বক্তা।", ["Fluent", "Expressive"], ["Inarticulate"], "Spoken"),
    ("Ascendance", "/əˈsen.dəns/", "Noun", "প্রভাব বৃদ্ধি, আধিপত্য", "Party gained ascendance in region.", "দলটি অঞ্চলে আধিপত্য লাভ করল।", ["Dominance", "Supremacy"], ["Decline"], "Politics"),
    ("Ascetic", "/əˈset.ɪk/", "Noun", "সন্ন্যাসী, তপস্বী", "He lived as an ascetic in cave.", "সে গুহায় এক তপস্বী হিসেবে বাস করত।", ["Hermit", "Monk"], ["Hedonist"], "Religion"),
    ("Asperity", "/æˈsper.ə.ti/", "Noun", "কঠোরতা, রুক্ষতা", "He answered with asperity.", "সে রুক্ষতার সাথে উত্তর দিল।", ["Harshness", "Roughness"], ["Mildness"], "GRE"),
    ("Aspersion", "/əˈspɜː.ʃən/", "Noun", "কলঙ্ক, অপবাদ বা কুৎসা", "Casting aspersions on character.", "চরিত্রে অপবাদ লেপন করা।", ["Slander", "Defamation"], ["Praise"], "Legal"),
    ("Assail", "/əˈseɪl/", "Verb", "আক্রমণ করা, আঘাত হানা", "Doubts assailed his mind.", "সন্দেহ তার মনে আঘাত হানল।", ["Attack", "Assault"], ["Defend"], "Literature"),
    ("Assiduous", "/əˈsɪdʒ.u.əs/", "Adj", "পরিশ্রমী, নাছোড়বান্দা", "Assiduous research yielded results.", "পরিশ্রমী গবেষণা ফল এনে দিল।", ["Diligent", "Sedulous"], ["Lazy"], "Academic"),
    ("Assuage", "/əˈsweɪdʒ/", "Verb", "প্রশমিত করা, উপশম করা", "Medicine assuaged pain.", "ওষুধটি ব্যথা উপশম করল।", ["Relieve", "Soothe"], ["Aggravate"], "GRE"),

    ("Astute", "/əˈstʃuːt/", "Adj", "বিচক্ষণ, সূক্ষ্মদর্শী", "An astute businessman made profit.", "এক বিচক্ষণ ব্যবসায়ী লাভ করল।", ["Shrewd", "Perceptive"], ["Foolish"], "Business"),
    ("Audacious", "/ɔːˈdeɪ.ʃəs/", "Adj", "দুঃসাহসী, বেপরোয়া", "An audacious rescue plan.", "এক দুঃসাহসী উদ্ধার পরিকল্পনা।", ["Bold", "Daring"], ["Timid"], "IELTS"),
    ("Augment", "/ɔːɡˈment/", "Verb", "বৃদ্ধি করা, বাড়ানো", "He took second job to augment income.", "আয় বাড়াতে সে দ্বিতীয় চাকরি নিল।", ["Increase", "Expand"], ["Decrease"], "Academic"),
    ("Auspicious", "/ɔːˈspɪʃ.əs/", "Adj", "শুভ, মঙ্গলজনক", "An auspicious start to event.", "অনুষ্ঠানের এক শুভ সূচনা।", ["Favorable", "Promising"], ["Inauspicious"], "Culture"),
    ("Austere", "/ɒsˈtɪər/", "Adj", "কঠোর, অনাড়ম্বর", "An austere lifestyle in desert.", "মরুভূমিতে এক অনাড়ম্বর জীবন।", ["Severe", "Strict"], ["Lavish"], "Literature"),
    ("Authenticity", "/ˌɔː.θenˈtɪs.ə.ti/", "Noun", "সত্যতা, খাঁটিত্ব", "Experts verified authenticity of painting.", "বিশেষজ্ঞরা ছবির খাঁটিত্ব যাচাই করলেন।", ["Genuineness", "Validity"], ["Falsehood"], "Art"),
    ("Autocratic", "/ˌɔː.təˈkræt.ɪk/", "Adj", "একনায়কতান্ত্রিক, স্বৈরাচারী", "An autocratic leader ruled.", "এক স্বৈরাচারী নেতা শাসন করল।", ["Despotic", "Tyrannical"], ["Democratic"], "Politics"),
    ("Autonomy", "/ɔːˈtɒn.ə.mi/", "Noun", "স্বায়ত্তশাসন, স্বাধীনতা", "Region demanded autonomy.", "অঞ্চলটি স্বায়ত্তশাসন দাবি করল।", ["Independence", "Self-rule"], ["Dependence"], "Politics"),
    ("Avarice", "/ˈæv.ər.ɪs/", "Noun", "লোভ, অর্থলালসা", "Avarice ruined his career.", "অর্থলালসা তার ক্যারিয়ার ধ্বংস করল।", ["Greed", "Cupidity"], ["Generosity"], "GRE"),
    ("Aversion", "/əˈvɜː.ʃən/", "Noun", "বিমুখতা, প্রবল অপছন্দ", "He has aversion to risk.", "ঝুঁকির প্রতি তার বিমুখতা রয়েছে।", ["Dislike", "Antipathy"], ["Liking"], "Psychology")
]

# Generate additional candidate batches automatically to ensure exactly 300 words
vocab_words_pool = [
    ("Beguiling", "/bɪˈɡaɪ.lɪŋ/", "Adj", "মোহনীয়, ছলনাকারী", "She gave a beguiling smile.", "সে এক মোহনীয় হাসি দিল।", ["Charming", "Enchanting"], ["Repulsive"], "Literature"),
    ("Belated", "/bɪˈleɪ.tɪd/", "Adj", "বিলম্বিত, দেরিতে আসা", "Belated birthday wishes.", "বিলম্বিত জন্মদিনের শুভেচ্ছা।", ["Delayed", "Late"], ["Early", "Prompt"], "Spoken"),
    ("Belittle", "/bɪˈlɪt.əl/", "Verb", "হেয় করা, ছোট করা", "Don't belittle his efforts.", "তার চেষ্টাকে হেয় করবেন না।", ["Disparage", "Depreciate"], ["Praise", "Extol"], "Spoken"),
    ("Bellicose", "/ˈbel.ɪ.kəʊs/", "Adj", "যুদ্ধবাজ, মারমুখী", "A bellicose speech angered nation.", "এক মারমুখী ভাষণ জাতিকে ক্রুদ্ধ করল।", ["Hostile", "Aggressive"], ["Peaceful"], "GRE"),
    ("Benefactor", "/ˈben.ɪ.fæk.tər/", "Noun", "হিতৈষী, দানবীর", "A generous benefactor funded hospital.", "এক দানবীর হাসপাতালে অর্থায়ন করলেন।", ["Patron", "Donor"], [], "Social"),
    ("Benevolence", "/bəˈnev.əl.əns/", "Noun", "দানশীলতা, উদারতা", "His benevolence touched lives.", "তার দানশীলতা জীবন স্পর্শ করল।", ["Kindness", "Altruism"], ["Malevolence"], "BCS"),
    ("Benign", "/bɪˈnaɪn/", "Adj", "সদয়, নিরপরাধ বা অতিকারক", "A benign tumor was removed.", "এক অতিকারক টিউমার অপসারণ করা হলো।", ["Harmless", "Gentle"], ["Malignant"], "Medical"),
    ("Bequeath", "/bɪˈkwiːð/", "Verb", "উত্তরাধিকারসূত্রে দেওয়া", "He bequeathed estate to son.", "সে ছেলের নামে সম্পত্তি দান করল।", ["Leave", "Will", "Hand down"], [], "Legal"),
    ("Berate", "/bɪˈreɪt/", "Verb", "কটু কথা বলা, তিরস্কার করা", "Parent berated child for grade.", "অভিভাবক সন্তানকে তিরস্কার করলেন।", ["Scold", "Chide"], ["Praise"], "Spoken"),
    ("Bereavement", "/bɪˈriːv.mənt/", "Noun", "স্বজনহারানো শোক", "Family suffered bereavement.", "পরিবারটি স্বজনহারানোর শোকে ভুগল।", ["Grief", "Loss"], [], "Literature"),
    ("Beseech", "/bɪˈsiːtʃ/", "Verb", "সাদরে অনুনয় করা, আকুতি করা", "I beseech you to reconsider.", "আমি আপনাকে পুনরায় ভাবার আকুতি জানাচ্ছি।", ["Implore", "Beg"], [], "Literature"),
    ("Bower", "/ˈbaʊ.ər/", "Noun", "কুঞ্জ, লতাগৃহ", "They sat in a shadowy bower.", "তারা লতাগৃহে বসল।", ["Arbor", "Shady place"], [], "Poetry"),
    ("Braggart", "/ˈbræɡ.ət/", "Noun", "অহংকারী, বড়াইকারী লোক", "Nobody liked the loud braggart.", "কেউ অহংকারী বড়াইকারীকে পছন্দ করত না।", ["Boaster"], ["Humble person"], "Literature"),
    ("Brevity", "/ˈbrev.ə.ti/", "Noun", "সংক্ষিপ্ততা, সংক্ষেপ", "Brevity is soul of wit.", "সংক্ষিপ্ততাই বুদ্ধির সার।", ["Conciseness", "Terseness"], ["Lengthiness"], "Spoken"),
    ("Bungling", "/ˈbʌŋ.ɡlɪŋ/", "Adj", "আনাড়ি, ভুলভাল ভরা", "Bungling attempt ruined plan.", "আনাড়ি চেষ্টা পরিকল্পনা পণ্ড করল।", ["Clumsy", "Incompetent"], ["Skillful"], "Spoken"),
    ("Buoyant", "/ˈbɔɪ.ənt/", "Adj", "ভাসমান, প্রফুল্ল ও আশাবাদী", "She was in a buoyant mood.", "সে এক প্রফুল্ল মেজাজে ছিল।", ["Cheerful", "Upbeat"], ["Depressed"], "Spoken"),
    ("Burgeon", "/ˈbɜː.dʒən/", "Verb", "দ্রুত বৃদ্ধি পাওয়া, বিকশিত হওয়া", "Town burgeoned into city.", "শহরটি দ্রুত বড় নগরে বিকশিত হলো।", ["Flourish", "Expand"], ["Shrink"], "Academic"),
    ("Buttress", "/ˈbʌt.rəs/", "Verb", "জোরদার করা, প্রঠেক দেওয়া", "Data buttressed argument.", "তথ্য যুক্তিকে জোরদার করল।", ["Support", "Reinforce"], ["Weaken"], "GRE"),
    ("Cajole", "/kəˈdʒəʊl/", "Verb", "মিষ্টি কথায় প্রলুব্ধ করা", "She cajoled him into agreeing.", "সে মিষ্টি কথায় তাকে রাজি করাল।", ["Coax", "Wheedle"], ["Bully"], "Spoken"),
    ("Calamity", "/kəˈlæm.ə.ti/", "Noun", "মহাবিপর্যয়, দুর্ভোগ", "Flood was a severe calamity.", "বন্যা ছিল চরম এক মহাবিপর্যয়।", ["Disaster", "Catastrophe"], ["Blessing"], "General"),
    ("Callousness", "/ˈkæl.əs.nəs/", "Noun", "অনভূতিহীনতা, পাষাণ ভাব", "His callousness shocked friends.", "তার অনুভূতিহীনতা বন্ধুদের স্তব্ধ করল।", ["Insensitivity", "Heartlessness"], ["Compassion"], "Psychology"),
    ("Calmness", "/ˈkɑːm.nəs/", "Noun", "শান্তি, স্থিরতা", "She faced crisis with calmness.", "সে স্থিরতার সাথে সংকটের মুখোমুখি হলো।", ["Tranquility", "Serenity"], ["Agitation"], "Spoken"),
    ("Camouflage", "/ˈkæm.ə.flɑːʒ/", "Verb", "ছদ্মবেশ ধরা, গোপন করা", "Animals camouflage in nature.", "প্রাণীরা প্রকৃতিতে ছদ্মবেশ ধরে।", ["Disguise", "Conceal"], ["Expose"], "Science"),
    ("Candid", "/ˈkæn.dɪd/", "Adj", "অকপট, স্পষ্টভাষী", "She gave candid advice.", "সে অকপট পরামর্শ দিল।", ["Frank", "Honest"], ["Deceitful"], "Spoken"),
    ("Canon", "/ˈkæn.ən/", "Noun", "নীতিমালা, ধর্মীয় বা সাহিত্যিক সংহিতা", "Literary canon of nation.", "জাতির সাহিত্যিক সংহিতা।", ["Rule", "Standard"], [], "Literature"),
    ("Cantankerous", "/kænˈtæŋ.kər.əs/", "Adj", "ঝগড়াটে, বদমেজাজী", "A cantankerous old man.", "এক ঝগড়াটে বৃদ্ধ লোক।", ["Bad-tempered", "Irascible"], ["Good-natured"], "GRE"),
    ("Captivate", "/ˈkæp.tɪ.veɪt/", "Verb", "মুগ্ধ করা, মোহিত করা", "Her performance captivated audience.", "তার পারফরম্যান্স দর্শকদের মোহিত করল।", ["Enchant", "Fascinate"], ["Repel"], "Spoken"),
    ("Cardiologist", "/ˌkɑː.diˈɒl.ə.dʒɪst/", "Noun", "হৃদরোগ বিশেষজ্ঞ", "He consulted a cardiologist.", "সে হৃদরোগ বিশেষজ্ঞের পরামর্শ নিল।", ["Heart specialist"], [], "Medical"),
    ("Cathartic", "/kəˈθɑː.tɪk/", "Adj", "মানসিক ভারমুক্তকারী", "Crying was a cathartic experience.", "কান্না ছিল এক মানসিক ভারমুক্তকারী অভিজ্ঞতা।", ["Purifying", "Releasing"], [], "Psychology"),
    ("Causation", "/kɔːˈzeɪ.ʃən/", "Noun", "কার্যকারণ সম্পর্ক", "Establishing causation in study.", "গবেষণায় কার্যকারণ সম্পর্ক স্থাপন।", ["Cause-and-effect"], [], "Science")
]

# Merge lists and ensure unique 300 entries
all_candidates = raw_batch_2 + vocab_words_pool

# Keep generating words dynamically until exact 300 unique entries reached
extra_word_stems = [
    ("Chronicle", "/ˈkrɒn.ɪ.kəl/", "Noun", "ইতিবৃত্ত, ধারাবাহিক বিবরণ", "A chronicle of war events.", "যুদ্ধের ঘটনার ধারাবাহিক ইতিবৃত্ত।", ["History", "Record"], [], "History"),
    ("Chivalry", "/ˈʃɪv.əl.ri/", "Noun", "বীরত্ব ও সৌজন্য", "Code of chivalry in medieval times.", "মধ্যযুগে বীরত্ব ও সৌজন্যের নিয়ম।", ["Gallantry", "Courtesy"], [], "History"),
    ("Cipher", "/ˈsaɪ.fər/", "Noun", "গোপন সঙ্কেত, শূন্য", "A secret cipher code.", "এক গোপন সংকেত কোড।", ["Code", "Secret message"], [], "Technology"),
    ("Circumlocution", "/ˌsɜː.kəm.ləˈkjuː.ʃən/", "Noun", "ঘুরিয়ে পেঁচিয়ে কথা বলা", "Avoid circumlocution in essay.", "প্রবন্ধে ঘুরিয়ে কথা বলা এড়ান।", ["Indirectness", "Evasiveness"], ["Directness"], "GRE"),
    ("Clairvoyant", "/kleəˈvɔɪ.ənt/", "Adj", "দূরদর্শী, অলৌকিক দৃষ্টিসম্পন্ন", "A clairvoyant prediction came true.", "এক অলৌকিক দূরদর্শী ভবিষ্যৎবাণী সত্য হলো।", ["Perceptive", "Psychic"], [], "Literature"),
    ("Clemency", "/ˈklem.ən.si/", "Noun", "দয়া, ক্ষমা বা সহানুভূতি", "Judge showed clemency to youth.", "বিচারক যুবকের প্রতি দয়া দেখালেন।", ["Mercy", "Leniency"], ["Ruthlessness"], "Legal"),
    ("Coalesce", "/ˌkəʊ.əˈles/", "Verb", "একত্রিত হওয়া, জোড়া লাগা", "Views coalesced into strategy.", "মতামতগুলো এক কৌশলে রূপ নিল।", ["Merge", "Unite"], ["Separate"], "Academic"),
    ("Cognitive", "/ˈkɒɡ.nə.tɪv/", "Adj", "জ্ঞানীয়, মানসিক চিন্তাসংক্রান্ত", "Cognitive skills improve with age.", "বয়সের সাথে জ্ঞানীয় দক্ষতা বাড়ে।", ["Mental", "Intellectual"], [], "Psychology"),
    ("Coherence", "/kəʊˈhɪə.rəns/", "Noun", "সামঞ্জস্য, সুস্পষ্ট সংহতি", "Lack of coherence in speech.", "ভাষণে সামঞ্জস্যের অভাব।", ["Consistency", "Clarity"], ["Incoherence"], "Academic"),
    ("Colloquial", "/kəˈləʊ.kwi.əl/", "Adj", "কথ্য, ইনফরমাল ভাষা", "Colloquial expression in English.", "ইংরেজি কথ্য ভাষার প্রকাশ।", ["Informal", "Conversational"], ["Formal"], "Spoken"),
    ("Collusion", "/kəˈluː.ʒən/", "Noun", "চক্রান্ত, গোপন সলাপররামর্শ", "Collusion between firms was illegal.", "ফার্মগুলোর মধ্যকার চক্রান্ত বেআইনি ছিল।", ["Conspiracy", "Secret agreement"], [], "Legal"),
    ("Commendation", "/ˌkɒm.enˈdeɪ.ʃən/", "Noun", "প্রশংসা, স্বীকৃতি", "She received commendation for bravery.", "সাহসিকতার জন্য সে প্রশংসা পেল।", ["Praise", "Award"], ["Censure"], "General"),
    ("Commiserate", "/kəˈmɪz.ə.reɪt/", "Verb", "সহানুভূতি প্রকাশ করা", "Friends commiserated with victim.", "বন্ধুরা ভুক্তভোগীর সাথে সহানুভূতি প্রকাশ করল।", ["Sympathize", "Console"], [], "Spoken"),
    ("Composure", "/kəmˈpəʊ.ʒər/", "Noun", "মনের স্থিরতা, প্রশান্তি", "He maintained composure under stress.", "চাপের মুখে সে মনের স্থিরতা বজায় রাখল।", ["Calmness", "Poise"], ["Agitation"], "Psychology"),
    ("Compulsion", "/kəmˈpʌl.ʃən/", "Noun", "অদম্য ইচ্ছা, বাধ্যবাধকতা", "Felt a compulsion to check phone.", "ফোন চেক করার এক অদম্য ইচ্ছা অনুভব করল।", ["Urge", "Necessity"], [], "Psychology"),
    ("Compunction", "/kəmˈpʌŋk.ʃən/", "Noun", "অনুতাপ, অনুশোচনা", "Showed no compunction for lie.", "মিথ্যার জন্য কোনো অনুশোচনা দেখালো না।", ["Remorse", "Regret"], [], "GRE"),
    ("Concatenation", "/kənˌkæt.əˈneɪ.ʃən/", "Noun", "শৃঙ্খলিত সংযোগ, পর পর ঘটা ঘটনা", "A concatenation of unfortunate events.", "দুর্ভাগ্যজনক ঘটনার শৃঙ্খলিত সংযোগ।", ["Series", "Chain"], [], "Academic"),
    ("Concentric", "/kənˈsen.trɪk/", "Adj", "এককেন্দ্রিক", "Concentric circles on target.", "লক্ষ্যবস্তুর ওপর এককে কেন্দ্র করা বৃত্তসমূহ।", ["Aligned"], [], "Science"),
    ("Concurrence", "/kənˈkʌr.əns/", "Noun", "একমত হওয়া, একই সাথে ঘটা", "General concurrence on policy.", "নীতিতে সাধারণ একমত হওয়া।", ["Agreement", "Coincidence"], ["Disagreement"], "Legal"),
    ("Condescend", "/ˌkɒn.dɪˈsend/", "Verb", "মাথা নোয়ানো, ছোট চোখে দেখা", "Don't condescend to subordinates.", "অধীনস্থদের ছোট চোখে দেখবেন না।", ["Patronize", "Deign"], [], "Spoken"),
    ("Condolence", "/kənˈdəʊ.ləns/", "Noun", "সমবেদনা, শোক প্রকাশ", "Sent heartfelt condolence to family.", "পরিবারে আন্তরিক সমবেদনা পাঠালেন।", ["Sympathy", "Solace"], [], "Spoken"),
    ("Confluence", "/ˈkɒn.flu.əns/", "Noun", "মিলনস্থল, সংযোগ", "Confluence of two rivers.", "দুটি নদীর মিলনস্থল।", ["Junction", "Convergence"], [], "Geography"),
    ("Conformity", "/kənˈfɔː.mə.ti/", "Noun", "আনুগত্য, প্রথানুগত্য", "Conformity to social norms.", "সামাজিক নিয়মের আনুগত্য।", ["Compliance", "Obedience"], ["Nonconformity"], "Sociology"),
    ("Conglomerate", "/kənˈɡlɒm.ər.ət/", "Noun", "বহুমুখী বিশাল শিল্পগোষ্ঠী", "A media conglomerate bought company.", "এক মিডিয়া শিল্পগোষ্ঠী কোম্পানিটি কিনল।", ["Corporation", "Group"], [], "Business"),
    ("Conjecture", "/kənˈdʒek.tʃər/", "Verb", "অনুমান করা", "We can only conjecture cause.", "আমরা কেবল কারণটি অনুমান করতে পারি।", ["Guess", "Speculate"], ["Prove"], "Academic"),
    ("Conscientious", "/ˌkɒn.ʃiˈen.ʃəs/", "Adj", "বিবেকবান, নিষ্ঠাবান", "A conscientious student studied daily.", "এক নিষ্ঠাবান ছাত্র প্রতিদিন পড়ত।", ["Meticulous", "Dutiful"], ["Careless"], "Academic"),
    ("Conscript", "/kənˈskrɪpt/", "Verb", "বাধ্যতামূলক সেনাবাহিনীতে ভর্তি করা", "Youths were conscripted into army.", "যুবকদের সেনাবাহিনীতে ভর্তি করা হলো।", ["Draft", "Enlist"], [], "Military"),
    ("Consecrate", "/ˈkɒn.sɪ.kreɪt/", "Verb", "পবিত্র ঘোষণা করা, উৎসর্গ করা", "Church was consecrated in 1800.", "গীর্জাটি ১৮০০ সালে উৎসর্গ করা হয়েছিল।", ["Sanctify", "Dedicate"], [], "Religion"),
    ("Consensus", "/kənˈsen.səs/", "Noun", "সর্বসম্মত সিদ্ধান্ত, ঐকমত্য", "Board reached a consensus.", "বোর্ড এক ঐকমত্যে পৌঁছালো।", ["Agreement", "Unanimity"], ["Disagreement"], "Business"),
    ("Consolation", "/ˌkɒn.səˈleɪ.ʃən/", "Noun", "সান্ত্বনা, আশ্বস্তকরণ", "A letter brought consolation to mother.", "চিঠিটি মায়ের জন্য সান্ত্বনা আনল।", ["Comfort", "Solace"], [], "Literature")
]

all_candidates.extend(extra_word_stems)

# Additional generated vocabulary to reach full 300
more_entries = [
    ("Consolidate", "/kənˈsɒl.ɪ.deɪt/", "Verb", "দৃঢ় করা, একীভূত করা", "Company consolidated its position.", "কোম্পানিটি তার অবস্থান একীভূত করল।", ["Strengthen", "Merge"], ["Weaken"], "Business"),
    ("Consternation", "/ˌkɒn.stəˈneɪ.ʃən/", "Noun", "চরম বিস্ময়, আতঙ্ক", "News caused consternation in city.", "সংবাদটি শহরে আতঙ্ক ছড়িয়ে দিল।", ["Dismay", "Alarm"], ["Calmness"], "General"),
    ("Constrain", "/kənˈstreɪn/", "Verb", "সীমাবদ্ধ করা, বাধ্য করা", "Budget constrained research.", "বাজেট গবেষণাকে সীমাবদ্ধ করল।", ["Restrict", "Limit"], ["Free"], "Academic"),
    ("Contagious", "/kənˈteɪ.dʒəs/", "Adj", "সংক্রামক, দ্রুত ছড়ানো", "Laughter is highly contagious.", "হাসি অত্যন্ত সংক্রামক।", ["Infectious", "Catching"], [], "Medical"),
    ("Contemplative", "/kənˈtem.plə.tɪv/", "Adj", "চিন্তাশীল, ধ্যানমগ্ন", "A contemplative walk in forest.", "বনে এক চিন্তাশীল হাঁটা।", ["Thoughtful", "Meditative"], [], "Philosophy"),
    ("Contemporaneous", "/kənˌtem.pəˈreɪ.ni.əs/", "Adj", "সমসাময়িক", "Contemporaneous accounts of event.", "ঘটনার সমসাময়িক বিবরণী।", ["Simultaneous", "Coexisting"], [], "History"),
    ("Contingency", "/kənˈtɪn.dʒən.si/", "Noun", "জরুরি সম্ভাবনা, আকস্মিক অবস্থা", "Prepared for any contingency.", "যেকোনো জরুরি সম্ভাবনার জন্য প্রস্তুত।", ["Emergency", "Possibility"], [], "Management"),
    ("Contraband", "/ˈkɒn.trə.bænd/", "Noun", "চোরাচালানিকৃত পণ্য, নিষিদ্ধ সামগ্রী", "Seized contraband at border.", "সীমান্তে নিষিদ্ধ সামগ্রী বাজেয়াপ্ত করা হলো।", ["Smuggled goods"], [], "Legal"),
    ("Controvert", "/ˌkɒn.trəˈvɜːt/", "Verb", "খণ্ডন করা, অস্বীকার করা", "Evidence controverted theory.", "সাক্ষ্যপ্রমাণ তত্ত্বটিকে খণ্ডন করল।", ["Refute", "Deny"], ["Confirm"], "GRE"),
    ("Conundrum", "/kəˈnʌn.drəm/", "Noun", "জটিল ধাঁধা, কঠিন সংকট", "Facing an ethical conundrum.", "এক নৈতিক সংকটের মুখোমুখি হওয়া।", ["Puzzle", "Enigma"], [], "IELTS"),
    ("Convalesce", "/ˌkɒn.vəˈles/", "Verb", "অসুখ থেকে সুস্থ হয়ে ওঠা", "He is convalescing at home.", "সে বাড়িতে সুস্থ হয়ে উঠছে।", ["Recover", "Recuperate"], [], "Medical"),
    ("Convergence", "/kənˈvɜː.dʒəns/", "Noun", "একত্রীকরণ, এক বিন্দুতে মিলন", "Convergence of technology and art.", "প্রযুক্তি ও শিল্পের একত্রীকরণ।", ["Merging", "Junction"], ["Divergence"], "Technology"),
    ("Convivial", "/kənˈvɪv.i.əl/", "Adj", "উৎসুক, সানন্দ ও উৎসবমুখর", "A convivial dinner with friends.", "বন্ধুদের সাথে এক উৎসবমুখর রাতের খাবার।", ["Festive", "Friendly"], ["Somber"], "Spoken"),
    ("Convolution", "/ˌkɒn.vəˈluː.ʃən/", "Noun", "প্যাঁচ, জটিলতা", "Convolutions of legal argument.", "আইনি যুক্তির প্যাঁচসমূহ।", ["Complexity", "Twist"], [], "Legal"),
    ("Convulsion", "/kənˈvʌl.ʃən/", "Noun", "খিঁচুনি, তীব্র আলোড়ন", "Economic convulsion shook nation.", "অর্থনৈতিক আলোড়ন জাতিকে কাঁপিয়ে দিল।", ["Spasm", "Upheaval"], [], "Medical"),
    ("Copious", "/ˈkəʊ.pi.əs/", "Adj", "বিপুল, অঢেল", "Poured copious praise on winner.", "বিজয়ীকে অঢেল প্রশংসা দেওয়া হলো।", ["Abundant", "Plentiful"], ["Meager"], "Academic"),
    ("Coquette", "/kɒˈket/", "Noun", "নাকউঁচু ছলনাকারী নারী", "A charming coquette flirts.", "এক মোহনীয় নারী ফ্লার্ট করে।", ["Flirt"], [], "Literature"),
    ("Cordially", "/ˈkɔː.di.ə.li/", "Adv", "আন্তরিকতার সাথে", "You are cordially invited.", "আপনাকে আন্তরিকতার সাথে আমন্ত্রণ জানানো হচ্ছে।", ["Warmly", "Heartily"], [], "Spoken"),
    ("Copia", "/ˈkəʊ.pi.ə/", "Noun", "প্রাচুর্য, সাবলীলতা", "Copia of ideas in essay.", "প্রবন্ধে ধারণার প্রাচুর্য।", ["Abundance", "Profusion"], [], "Literature"),
    ("Cornucopia", "/ˌkɔː.njəˈkəʊ.pi.ə/", "Noun", "প্রাচুর্যের আধার, অঢেল উৎস", "A cornucopia of fruits.", "ফলের এক প্রাচুর্যের আধার।", ["Bounty", "Abundance"], [], "Culture"),
    ("Corot", "/ˈkɒr.əʊ/", "Noun", "চিত্রশিল্পী প্রথার শৈলী", "Classic landscape painting.", "ধ্রুপদী প্রাকৃতিক ল্যান্ডস্কেপ।", ["Style"], [], "Art"),
    ("Corporeal", "/kɔːˈpɔː.ri.əl/", "Adj", "শারীরিক, পার্থিব বা দৃশ্যমান", "Corporeal existence on earth.", "পৃথিবীতে শারীরিক অস্তিত্ব।", ["Bodily", "Physical"], ["Spiritual"], "Philosophy"),
    ("Correlation", "/ˌkɒr.əˈleɪ.ʃən/", "Noun", "সহসম্বন্ধ, পারস্পরিক যোগসূত্র", "A strong correlation between study and marks.", "পড়ালেখা ও নম্বরের মধ্যে এক গভীর সহসম্বন্ধ।", ["Connection", "Link"], [], "Science"),
    ("Corroboration", "/kəˌrɒb.əˈreɪ.ʃən/", "Noun", "সত্যতা নিশ্চিতকরণ", "Need corroboration for claim.", "দাবির সত্যতা নিশ্চিতকরণ প্রয়োজন।", ["Confirmation", "Verification"], [], "Legal"),
    ("Corrosive", "/kəˈrəʊ.sɪv/", "Adj", "ক্ষয়কারী, ধ্বংসাত্মক", "Acid has a corrosive effect.", "এসিডের এক ক্ষয়কারী প্রভাব রয়েছে।", ["Erosive", "Harmful"], [], "Science"),
    ("Counteract", "/ˌkaʊn.tərˈækt/", "Verb", "প্রতিরোধ করা, প্রশমিত করা", "Antidote counteracted poison.", "প্রতিষেধক বিষ প্রতিরোধ করল।", ["Neutralize", "Offset"], [], "Medical"),
    ("Countermand", "/ˌkaʊn.tərˈmɑːnd/", "Verb", "আদেশ বাতিল করা", "General countermanded attack order.", "জেনারেল আক্রমণের আদেশ বাতিল করলেন।", ["Cancel", "Revoke"], [], "Military"),
    ("Countenance", "/ˈkaʊn.tən.əns/", "Noun", "মুখাবয়ব, প্রশ্রয় বা অনুমতি", "Gave no countenance to crime.", "অপরাধকে কোনো প্রশ্রয় দেওয়া হলো না।", ["Expression", "Support"], [], "Literature"),
    ("Courteous", "/ˈkɜː.ti.əs/", "Adj", "ভদ্র, শিষ্ট ও মার্জিত", "A courteous clerk assisted guest.", "এক ভদ্র ক্লার্ক অতিথিকে সাহায্য করল।", ["Polite", "Civil"], ["Rude"], "Spoken"),
    ("Covertly", "/ˈkʌv.ət.li/", "Adv", "গোপনে, আড়ালে", "He acted covertly in mission.", "সে মিশনে গোপনে কাজ করল।", ["Secretly", "Stealthily"], ["Openly"], "General"),
    ("Covetous", "/ˈkʌv.ɪ.təs/", "Adj", "লোভী, পরশ্রীকাতর", "Covetous of neighbour's success.", "প্রতিবেশীর সাফল্যের প্রতি পরশ্রীকাতর।", ["Greedy", "Envious"], ["Generous"], "GRE"),
    ("Cower", "/ˈkaʊ.ər/", "Verb", "ভয়ে কাঁপানো, গুটিয়ে যাওয়া", "Dog cowered in corner.", "কুকুরটি কোণায় ভয়ে গুটিয়ে গেল।", ["Cringe", "Shrink"], [], "Literature"),
    ("Coyness", "/ˈkɔɪ.nəs/", "Noun", "লাজুকতা, লজ্জা পাওয়ার ভান", "She smiled with charming coyness.", "সে এক চমৎকার লাজুকতায় হাসল।", ["Shyness", "Modesty"], [], "Literature"),
    ("Crass", "/kræs/", "Adj", "স্থূল, অনুভূতিহীন বা অভদ্র", "A crass remark offended all.", "এক স্থূল মন্তব্য সবাইকে ক্ষুব্ধ করল।", ["Insensitive", "Rude"], ["Refined"], "GRE"),
    ("Craven", "/ˈkreɪ.vən/", "Adj", "কা পুরুষ, কাপুরুষোচিত", "A craven surrender disappointed nation.", "এক কাপুরুষোচিত আত্মসমর্পণ জাতিকে হতাশ করল।", ["Cowardly", "Timid"], ["Brave"], "GRE"),
    ("Credence", "/ˈkriː.dəns/", "Noun", "বিশ্বাস, সত্যতা প্রদান", "Gave no credence to rumor.", "গুজবে কোনো বিশ্বাস দিল না।", ["Belief", "Acceptance"], ["Disbelief"], "Academic"),
    ("Credulity", "/krɪˈdʒuː.lə.ti/", "Noun", "সহজবিশ্বাস, অতি সরলতা", "Exploited credulity of public.", "জনগণের অতি সরলতার সুযোগ নিল।", ["Naivety", "Gullibility"], [], "GRE"),
    ("Crestfallen", "/ˈkrest.fɔː.lən/", "Adj", "হতাশ, ম্লান বদন", "Felt crestfallen after loss.", "পরাজয়ের পর ম্লান বদন অনুভব করল।", ["Dejected", "Disappointed"], ["Elated"], "Literature"),
    ("Cryptic", "/ˈkrɪp.tɪk/", "Adj", "গুপ্ত, রহস্যময় সংকেতভরা", "A cryptic note left on table.", "টেবিলে ফেলে যাওয়া এক গোপন রহস্যময় নোট।", ["Mysterious", "Enigmatic"], ["Clear"], "General"),
    ("Culmination", "/ˌkʌl.mɪˈneɪ.ʃən/", "Noun", "চূড়ান্ত পরিণতি, শিখর", "Culmination of years of effort.", "বছরের পর বছর চেষ্টার চূড়ান্ত পরিণতি।", ["Climax", "Peak"], [], "Academic"),
    ("Culpability", "/ˌkʌl.pəˈbɪl.ə.ti/", "Noun", "অপরাধিতা, দায়বদ্ধতা", "Denied any culpability in scam.", "স্ক্যামে যেকোনো অপরাধিতা অস্বীকার করল।", ["Guilt", "Blame"], ["Innocence"], "Legal"),
    ("Cultivate", "/ˈkʌl.tɪ.veɪt/", "Verb", "চাষ করা, বিকাশ ঘটানো", "Cultivate good habits early.", "ছোটবেলা থেকেই ভালো অভ্যাসের বিকাশ ঘটান।", ["Develop", "Foster"], [], "Spoken"),
    ("Cumbersome", "/ˈkʌm.bə.səm/", "Adj", "ভারী, আনাড়িপূর্ণ ও ঝামেলার", "A cumbersome process took hours.", "এক ঝামেলার প্রক্রিয়া ঘণ্টার পর ঘণ্টা সময় নিল।", ["Unwieldy", "Awkward"], ["Convenient"], "Spoken"),
    ("Cumulative", "/ˈkjuː.mjə.lə.tɪv/", "Adj", "সঞ্চিত, ক্রমবর্ধমান", "Cumulative effect of pollution.", "দূষণের ক্রমবর্ধমান সঞ্চিত প্রভাব।", ["Increasing", "Accumulated"], [], "Science"),
    ("Cupidity", "/kjuːˈpɪd.ə.ti/", "Noun", "অর্থলালসা, চরম লোভ", "Motivated by sheer cupidity.", "স্রেফ চরম অর্থলালসায় প্ররোচিত।", ["Greed", "Avarice"], [], "GRE"),
    ("Curator", "/kjʊəˈreɪ.tər/", "Noun", "জাদুঘর বা গ্যালারির তত্ত্বাবধায়ক", "Curator presented artifact.", "তত্ত্বাবধায়ক প্রত্নবস্তু প্রদর্শন করলেন।", ["Keeper", "Custodian"], [], "Art"),
    ("Curtness", "/ˈkɜːt.nəs/", "Noun", "রুক্ষ সংক্ষিপ্ততা", "Apologized for curtness.", "রুক্ষ সংক্ষিপ্ততার জন্য ক্ষমা চাইল।", ["Terseness", "Abruptness"], [], "Spoken"),
    ("Cynicism", "/ˈsɪn.ɪ.sɪ.zəm/", "Noun", "সন্দেহপ্রবণতা, কুটিল দৃষ্টিভঙ্গি", "Public cynicism toward promises.", "প্রতিশ্রুতির প্রতি জনগণের কুটিল দৃষ্টিভঙ্গি।", ["Skeptisism", "Doubt"], ["Optimism"], "Politics"),
    ("Dainty", "/ˈdeɪn.ti/", "Adj", "সূক্ষ্ম, সুদৃশ্য ও নরম", "A dainty porcelain teacup.", "এক সুদৃশ্য সূক্ষ্ম চীনামাটির চায়ের কাপ।", ["Delicate", "Refined"], ["Coarse"], "Literature"),
    ("Dauntless", "/ˈdɔːnt.ləs/", "Adj", "অকুতোভয়, নির্ভীক", "Dauntless courage in battle.", "যুদ্ধে অকুতোভয় সাহস।", ["Fearless", "Intrepid"], ["Timid"], "Literature"),
    ("Dawdle", "/ˈdɔː.dəl/", "Verb", "অহেতুক অলসতা করা, সময় কাটানো", "Don't dawdle or we'll miss bus.", "অহেতুক অলসতা করবেন না নয়তো বাস মিস হবে।", ["Loiter", "Delay"], ["Hurry"], "Spoken"),
    ("Deadlock", "/ˈded.lɒk/", "Noun", "অচল অবস্থা, অচলাবস্থা", "Talks reached a deadlock.", "আলোচনা এক অচলাবস্থায় পৌঁছালো।", ["Stalemate", "Impasse"], [], "Politics"),
    ("Dearth", "/dɜːθ/", "Noun", "চরম অপ্রতুলতা, অনটন", "Dearth of water in drought.", "খরায় পানির চরম অনটন।", ["Scarcity", "Shortage"], ["Abundance"], "IELTS"),
    ("Debilitating", "/dɪˈbɪl.ɪ.teɪ.tɪŋ/", "Adj", "দুর্বলকারী, শারীরিক ভাঙন ধরায় এমন", "A debilitating illness kept him in bed.", "এক দুর্বলকারী অসুস্থতা তাকে বিছানায় রাখল।", ["Weakening", "Enfeebling"], [], "Medical"),
    ("Decadence", "/ˈdek.ə.dəns/", "Noun", "ক্ষয়, নৈতিক পতন", "Era of cultural decadence.", "সাংস্কৃতিক পতনের যুগ।", ["Decline", "Deterioration"], [], "History"),
    ("Decipher", "/dɪˈsaɪ.fər/", "Verb", "উদ্ধার করা, পাঠোদ্ধার করা", "Decipher ancient code.", "প্রাচীন সংকেতের পাঠোদ্ধার করা।", ["Decode", "Interpret"], [], "Science"),
    ("Declamatory", "/dɪˈklæm.ə.tər.i/", "Adj", "উত্তেজক আড়ম্বরপূর্ণ বক্তৃতাময়", "A declamatory tone of voice.", "কণ্ঠস্বরের এক আড়ম্বরপূর্ণ সুর।", ["Bombastic", "Pompous"], [], "Literature"),
    ("Decomposing", "/ˌdiː.kəmˈpəʊ.zɪŋ/", "Adj", "পচনশীল, গলিত", "Decomposing leaves fertilize soil.", "পচনশীল পাতা মাটিকে উর্বর করে।", ["Decaying", "Rotting"], [], "Science"),
    ("Decorum", "/dɪˈkɔː.rəm/", "Noun", "শালীনতা, শিষ্টাচার", "Maintain decorum in court.", "আদালতে শিষ্টাচার বজায় রাখুন।", ["Propriety", "Etiquette"], ["Impropriety"], "Legal"),
    ("Decrepit", "/dɪˈkrep.ɪt/", "Adj", "জরাজীর্ণ, জীর্ণশীর্ণ", "A decrepit wooden building.", "এক জরাজীর্ণ কাঠের ভবন।", ["Dilapidated", "Feebled"], [], "Literature"),
    ("Dedudction", "/dɪˈdʌk.ʃən/", "Noun", "সিদ্ধান্ত নিগমন, কর্তন", "Tax deduction from salary.", "বেতন থেকে কর কর্তন।", ["Subtraction", "Inference"], [], "Academic"),
    ("Defalcation", "/ˌdiː.fælˈkeɪ.ʃən/", "Noun", "অর্থ তসরুপ, তছরুপ", "Discovered defalcation in bank.", "ব্যাংকে অর্থ তসরুপ ধরা পড়ল।", ["Embezzlement", "Misappropriation"], [], "Legal"),
    ("Defensible", "/dɪˈfen.sə.bəl/", "Adj", "রক্ষণযোগ্য, যুক্তিযুক্ত", "A defensible strategy.", "এক যুক্তিযুক্ত রক্ষণযোগ্য কৌশল।", ["Justifiable", "Valid"], [], "Academic"),
    ("Deferential", "/ˌdef.ərˈen.ʃəl/", "Adj", "শ্রদ্ধাশীল, সম্মানপ্রদর্শনকারী", "Deferential attitude to elders.", "গুরুজনদের প্রতি শ্রদ্ধাশীল মনোভাব।", ["Respectful", "Obedient"], ["Disrespectful"], "Spoken"),
    ("Defiance", "/dɪˈfaɪ.əns/", "Noun", "অমান্য, স্পর্ধা বা চ্যালেঞ্জ", "Act of open defiance.", "খোলামেলা অমান্য করার কাজ।", ["Resistance", "Rebellion"], ["Obedience"], "Politics"),
    ("Definitive", "/dɪˈfɪn.ə.tɪv/", "Adj", "চূড়ান্ত, সুনির্দিষ্ট ও খাঁটি", "The definitive guide to subject.", "বিষয়টির ওপর এক চূড়ান্ত নির্দেশিকা।", ["Conclusive", "Final"], [], "Academic"),
    ("Deflect", "/dɪˈflekt/", "Verb", "দিক পরিবর্তন করানো, অন্যমুখী করা", "Deflect criticism smoothly.", "সমালোচনাকে চতুরতার সাথে অন্যমুখী করা।", ["Divert", "Turn aside"], [], "Spoken"),
    ("Deftness", "/ˈdeft.nəs/", "Noun", "চটপটে দক্ষতা, পারদর্শিতা", "Handled situation with deftness.", "পরিস্থিতিটি পারদর্শিতার সাথে সামলালো।", ["Skill", "Adroitness"], [], "General"),
    ("Degenerate", "/dɪˈdʒen.ər.eɪt/", "Verb", "অবনতি ঘটা, নিকৃষ্ট হওয়া", "Discussion degenerated into fight.", "আলোচনা মারামারিতে রূপ নিয়ে অবনতি ঘটালো।", ["Deteriorate", "Decline"], ["Improve"], "Social"),
    ("Deification", "/ˌdiː.ɪ.fɪˈkeɪ.ʃən/", "Noun", "দেবতাজ্ঞানকরণ, পূজনীয় করা", "Deification of national heroes.", "জাতীয় বীরদের দেবতাজ্ঞান করা।", ["Worship", "Exaltation"], [], "Culture"),
    ("Deign", "/deɪn/", "Verb", "দয়া করে রাজি হওয়া, মাথা নোয়ানো", "He didn't deign to reply.", "সে উত্তর দিতেও দয়া করল না।", ["Condescend", "Stoop"], [], "Literature"),
    ("Delectable", "/dɪˈlek.tə.bəl/", "Adj", "সুস্বাদু, মনোহর", "A delectable chocolate cake.", "এক সুস্বাদু চকলেট কেক।", ["Delicious", "Tasty"], [], "Spoken"),
    ("Deliberate", "/dɪˈlɪb.ər.eɪt/", "Verb", "গভীরভাবে আলোচনা বা চিন্তা করা", "Jury deliberated for hours.", "জুরি ঘণ্টার পর ঘণ্টা গভীর আলোচনা করল।", ["Ponder", "Consider"], [], "Legal"),
    ("Delineate", "/dɪˈlɪn.i.eɪt/", "Verb", "আঁকা, সঠিকভাবে বর্ণনা করা", "Delineate boundaries on map.", "মানচিত্রে সীমানা সঠিকভাবে নির্দেশ করা।", ["Outline", "Define"], [], "Academic"),
    ("Delirium", "/dɪˈlɪr.i.əm/", "Noun", "প্রলাপ, তীব্র মানসিক বিভ্রান্তি", "Fever caused delirium.", "জ্বর তীব্র প্রলাপের সৃষ্টি করল।", ["Frenzy", "Confusion"], [], "Medical"),
    ("Deluge", "/ˈdel.juːdʒ/", "Noun", "প্লাবন, অতিবৃষ্টি বা প্রশ্নের সয়লাব", "A deluge of complaints received.", "অভিযোগের সয়লাব পাওয়া গেল।", ["Flood", "Torrent"], [], "General"),
    ("Delusion", "/dɪˈluː.ʒən/", "Noun", "ভ্রান্তি, মরীচিকা বা ভুল ধারণা", "Suffering from delusions of grandeur.", "মহত্ত্বের ভ্রান্তিতে ভোগা।", ["Illusion", "Fantasy"], [], "Psychology"),
    ("Demagogue", "/ˈdem.ə.ɡɒɡ/", "Noun", "উত্তেজক রাজনৈতিক বক্তা", "Demagogue misled citizens.", "উত্তেজক বক্তা নাগরিকদের বিভ্রান্ত করল।", ["Agitator"], [], "Politics"),
    ("Demeanor", "/dɪˈmiː.nər/", "Noun", "আচরণ, ভাবভঙ্গি", "A calm demeanor reassured all.", "এক শান্ত ভাবভঙ্গি সবাইকে আশ্বস্ত করল।", ["Behavior", "Manner"], [], "Spoken"),
    ("Demented", "/dɪˈmen.tɪd/", "Adj", "উন্মাদ, ক্ষেপাটে", "Demented laughter echoed.", "উন্মাদ হাসি প্রতিধ্বনিত হলো।", ["Insane", "Crazy"], [], "Literature"),
    ("Demise", "/dɪˈmaɪz/", "Noun", "মৃত্যু, পতন বা অবসান", "Demise of ancient empire.", "প্রাচীন সাম্রাজ্যের অবসান।", ["Death", "End"], [], "History"),
    ("Democracy", "/dɪˈmɒk.rə.si/", "Noun", "গণতন্ত্র, জনগণের শাসন", "Principles of democracy.", "গণতন্ত্রের মূলনীতিসমূহ।", ["Self-rule"], [], "Politics"),
    ("Demolish", "/dɪˈmɒl.ɪʃ/", "Verb", "ভেঙে ফেলা, গুঁড়িয়ে দেওয়া", "Demolish old structure.", "পুরনো কাঠামো গুঁড়িয়ে দেওয়া।", ["Destroy", "Raze"], ["Build"], "General"),
    ("Demoralize", "/dɪˈmɒr.ə.laɪz/", "Verb", "মনোবল ভেঙে দেওয়া", "Defeat demoralized team.", "পরাজয় দলটির মনোবল ভেঙে দিল।", ["Dishearten", "Discourage"], [], "Sports"),
    ("Demure", "/dɪˈmjʊər/", "Adj", "শান্ত, ধীর ও লাজুক", "A demure young woman.", "এক শান্ত ধীর লাজুক যুবতী।", ["Modest", "Shy"], [], "Literature"),
    ("Denigrate", "/ˈden.ɪ.ɡreɪt/", "Verb", "কলঙ্কিত করা, হেয় করা", "Don't denigrate reputation.", "সুনাম হেয় করবেন না।", ["Belittle", "Malign"], [], "GRE"),
    ("Denouncement", "/dɪˈnaʊns.mənt/", "Noun", "প্রকাশ্য প্রতিবাদ বা নিন্দা", "Denouncement of violence.", "সহিংসতার প্রকাশ্য প্রতিবাদ।", ["Condemnation"], [], "Politics"),
    ("Denouement", "/deɪˈnuː.mɒ̃/", "Noun", "নাটকের শেষ পরিণতি, গ্রন্থিমোচন", "Surprising denouement of novel.", "উপন্যাসের বিস্ময়কর গ্রন্থিমোচন।", ["Resolution", "Outcome"], [], "Literature"),
    ("Denunciate", "/dɪˈnʌn.si.eɪt/", "Verb", "নিন্দা করা, অভিযুক্ত করা", "Denunciate corruption openly.", "দুর্নীতি প্রকাশ্যে নিন্দা করা।", ["Condemn", "Accuse"], [], "Politics"),
    ("Depict", "/dɪˈpɪkt/", "Verb", "ফুটিয়ে তোলা, অঙ্কন করা", "Painting depicts rural scene.", "ছবিটি গ্রামীণ দৃশ্য ফুটিয়ে তোলে।", ["Portray", "Illustrate"], [], "Art"),
    ("Depletion", "/dɪˈpliː.ʃən/", "Noun", "ক্ষয়, নিঃশেষ হওয়া", "Depletion of natural resources.", "প্রাকৃতিক সম্পদের নিঃশেষ হওয়া।", ["Exhaustion", "Reduction"], [], "Science"),
    ("Deplorable", "/dɪˈplɔː.rə.bəl/", "Adj", "শোচনীয়, চরম নিন্দনীয়", "Deplorable living conditions.", "শোচনীয় জীবনযাত্রার মান।", ["Disgraceful", "Lamentable"], [], "Social"),
    ("Deportment", "/dɪˈpɔːt.mənt/", "Noun", "চালচলন, শিষ্টাচারযুক্ত আচরণ", "Excellent deportment of student.", "শিক্ষার্থীর চমৎকার চালচলন।", ["Bearing", "Conduct"], [], "Academic"),
    ("Deposition", "/ˌdep.əˈzɪʃ.ən/", "Noun", "হলফনামা, জবানবন্দী বা পদচ্যুতি", "Witness gave deposition.", "সাক্ষী জবানবন্দী দিল।", ["Testimony", "Statement"], [], "Legal"),
    ("Depravity", "/dɪˈpræv.ə.ti/", "Noun", "নৈতিক অধঃপতন, ভ্রষ্টতা", "Act of extreme depravity.", "চরম নৈতিক অধঃপতনের কাজ।", ["Wickedness", "Corruption"], [], "GRE"),
    ("Deprecate", "/ˈdep.rə.keɪt/", "Verb", "অপ্রীতি প্রকাশ করা, অপছন্দ করা", "Deprecate violence in media.", "মিডিয়ায় সহিংসতা অপ্রীতি প্রকাশ করা।", ["Disapprove", "Deplore"], [], "GRE"),
    ("Depredation", "/ˌdep.rəˈdeɪ.ʃən/", "Noun", "লুণ্ঠন, ধ্বংসযজ্ঞ", "Depredations of war.", "যুদ্ধের ধ্বংসযজ্ঞ।", ["Plunder", "Ravage"], [], "History"),
    ("Derelict", "/ˈder.ə.lɪkt/", "Adj", "পরিত্যক্ত, জরাজীর্ণ", "A derelict factory site.", "এক পরিত্যক্ত কারখানা এলাকা।", ["Abandoned", "Dilapidated"], [], "General"),
    ("Derision", "/dɪˈrɪʒ.ən/", "Noun", "উপহাস, বিদ্রূপ", "Met with derision by peers.", "সহকর্মীদের দ্বারা উপহাসের মুখোমুখি হলো।", ["Mockery", "Ridicule"], [], "Literature"),
    ("Derivation", "/ˌder.ɪˈveɪ.ʃən/", "Noun", "উৎপত্তি, ব্যুৎপত্তি", "Derivation of word from Latin.", "ল্যাটিন থেকে শব্দের ব্যুৎপত্তি।", ["Origin", "Source"], [], "Academic"),
    ("Derogatory", "/dɪˈrɒɡ.ə.tər.i/", "Adj", "মানহানিকর, অপমানজনক", "Derogatory comments removed.", "মানহানিকর মন্তব্য সরিয়ে ফেলা হলো।", ["Disparaging", "Offensive"], [], "Spoken"),
    ("Desecrate", "/ˈdes.ɪ.kreɪt/", "Verb", "অপবিত্র করা, কলঙ্কিত করা", "Vandals desecrated tomb.", "বর্বরেরা সমাধি অপবিত্র করল।", ["Profane", "Violate"], [], "Religion"),
    ("Desolation", "/ˌdes.əˈleɪ.ʃən/", "Noun", "নির্জনতা, হাহাকার ও উজার অবস্থা", "A scene of utter desolation.", "চরম হাহাকারের এক দৃশ্য।", ["Devastation", "Bleakness"], [], "Literature"),
    ("Despicable", "/dɪˈspɪk.ə.bəl/", "Adj", "ঘৃণ্য, নরাধম ও জঘন্য", "A despicable act of betrayal.", "বিশ্বাসঘাতকতার এক ঘৃণ্য কাজ।", ["Contemptible", "Vile"], [], "Spoken"),
    ("Despondency", "/dɪˈspɒn.dən.si/", "Noun", "হতাশা, গভীর বিষাদ", "Fell into despondency.", "গভীর হতাশায় ডুবে গেল।", ["Dejection", "Gloom"], [], "Psychology"),
    ("Despotism", "/ˈdes.pə.tɪ.zəm/", "Noun", "স্বৈরাচার, অত্যাচারী শাসন", "Fought against despotism.", "স্বৈরাচারের বিরুদ্ধে লড়াই করল।", ["Tyranny", "Autocracy"], [], "History"),
    ("Desultory", "/ˈdes.əl.tər.i/", "Adj", "উদ্বাষ্ট, বিশৃঙ্খল বা লক্ষ্যহীন", "A desultory conversation.", "এক লক্ষ্যহীন বিশৃঙ্খল আলাপ।", ["Random", "Aimless"], [], "GRE"),
    ("Detachment", "/dɪˈtætʃ.mənt/", "Noun", "নিরপেক্ষতা, বিচ্ছিন্নতা", "Viewed situation with detachment.", "নিরপেক্ষতার সাথে পরিস্থিতি অবলোকন করল।", ["Impartiality", "Objectivity"], [], "Psychology"),
    ("Deterrence", "/dɪˈter.əns/", "Noun", "প্রতিরোধ, ভয় দেখিয়ে থামানো", "Nuclear deterrence policy.", "পারমাণবিক প্রতিরোধ নীতি।", ["Prevention", "Deterrent"], [], "Politics"),
    ("Detestation", "/ˌdiː.tesˈteɪ.ʃən/", "Noun", "চরম ঘৃণা, অপছন্দ", "Felt detestation for crime.", "অপরাধের জন্য চরম ঘৃণা অনুভব করল।", ["Hatred", "Loathing"], [], "GRE"),
    ("Detraction", "/dɪˈtræk.ʃən/", "Noun", "মানহানি, প্রশংসা কমানো", "Free from detraction.", "মানহানি থেকে মুক্ত।", ["Slander", "Belittling"], [], "Literature"),
    ("Deviance", "/ˈdiː.vi.əns/", "Noun", "পথভ্রষ্টতা, নিয়মলঙ্ঘন", "Social deviance studied.", "সামাজিক পথভ্রষ্টতা অধ্যায়ন করা হলো।", ["Divergence", "Abnormality"], [], "Sociology"),
    ("Deviousness", "/ˈdiː.vi.əs.নাস/", "Noun", "কুটিলতা, ছলনা", "Known for deviousness.", "কুটিলতার জন্য পরিচিত।", ["Deceitfulness", "Cunning"], [], "GRE"),
    ("Devoid", "/dɪˈvɔɪd/", "Adj", "শূন্য, বর্জিত", "Devoid of any sense.", "যেকোনো বোধ থেকে শূন্য।", ["Lacking", "Empty"], [], "Spoken"),
    ("Devolution", "/ˌdiː.vəˈluː.ʃən/", "Noun", "ক্ষমতার বিকেন্দ্রীকরণ", "Devolution of power to region.", "অঞ্চলে ক্ষমতার বিকেন্দ্রীকরণ।", ["Decentralization"], [], "Politics"),
    ("Devoutness", "/dɪˈvaʊt.নাস/", "Noun", "ধার্মিকতা, পরম ভক্তি", "Respected for devoutness.", "ধার্মিকতার জন্য শ্রদ্ধেয়।", ["Piety", "Religiousness"], [], "Religion"),
    ("Dexterity", "/dekˈster.ə.ti/", "Noun", "হাতে-কলমে চটপটে দক্ষতা", "Manual dexterity required.", "হাতে-কলমে চটপটে দক্ষতা প্রয়োজন।", ["Agility", "Skill"], [], "Sports"),
    ("Diabolical", "/ˌdaɪ.əˈbɒl.ɪ.kəl/", "Adj", "পৈশাচিক, শয়তানিভরা", "A diabolical plot uncovered.", "এক পৈশাচিক চক্রান্ত উন্মোচিত হলো।", ["Fiendish", "Wicked"], [], "Literature"),
    ("Diagnosis", "/ˌdaɪ.əɡˈnəʊ.sɪs/", "Noun", "রোগ নির্ণয়, নিদান", "Early diagnosis saves lives.", "প্রাথমিক রোগ নির্ণয় জীবন বাঁচায়।", ["Identification"], [], "Medical"),
    ("Dialectic", "/ˌdaɪ.əˈlek.tɪk/", "Noun", "যুক্তিবিদ্যা, দ্বান্দ্বিক তর্কপদ্ধতি", "Hegelian dialectic method.", "হেগেলীয় দ্বান্দ্বিক পদ্ধতি।", ["Logic", "Argumentation"], [], "Philosophy"),
    ("Diaphanous", "/daɪˈæf.ən.əs/", "Adj", "স্বচ্ছ, পাতলা ও সুক্ষ্ম", "Diaphanous silk fabric.", "সুক্ষ্ম পাতলা রেশমি কাপড়।", ["Sheer", "Translucent"], [], "Fashion"),
    ("Dichotomy", "/daɪˈkɒt.ə.mi/", "Noun", "দ্বিখণ্ডতা, বৈপরীত্য", "Dichotomy between theory and reality.", "তত্ত্ব ও বাস্তবতার দ্বিখণ্ডতা।", ["Split", "Division"], [], "Academic"),
    ("Dictatorial", "/ˌdɪk.təˈtɔː.ri.əl/", "Adj", "একনায়কসুলভ, হুকুমদারি", "A dictatorial management style.", "এক একনায়কসুলভ ব্যবস্থাপনা শৈলী।", ["Authoritarian", "Domineering"], [], "Politics"),
    ("Didacticism", "/dɪˈdæk.tɪ.sɪ.zəm/", "Noun", "উপদেশধর্মিতা", "Avoid overt didacticism.", "প্রকট উপদেশধর্মিতা পরিহার করুন।", ["Moralizing"], [], "Literature"),
    ("Differing", "/ˈdɪf.ər.ɪŋ/", "Adj", "ভিন্নমত পোষণকারী", "Differing views expressed.", "ভিন্নমত প্রকাশ করা হলো।", ["Varying", "Divergent"], [], "Spoken"),
    ("Diffidence", "/ˈdɪf.ɪ.dəns/", "Noun", "আত্মবিশ্বাসহীনতা, লাজুকতা", "Overcame diffidence to speak.", "কথা বলতে লাজুকতা জয় করল।", ["Shyness", "Modesty"], [], "Psychology"),
    ("Diffuse", "/dɪˈfjuːz/", "Adj", "ছড়ানো, অসংহত বা শব্দবহুল", "A diffuse explanation.", "এক অসংহত শব্দবহুল ব্যাখ্যা।", ["Wordy", "Scattered"], [], "GRE"),
    ("Digression", "/daɪˈɡreʃ.ən/", "Noun", "মূল আলোচনা থেকে বিচ্যুতি", "Pardon the brief digression.", "সংক্ষিপ্ত বিচ্যুতির জন্য ক্ষমা করবেন।", ["Deviation", "Detour"], [], "Academic"),
    ("Dilapidated", "/dɪˈlæp.ɪ.deɪ.tɪd/", "Adj", "জরাজীর্ণ, ভগ্নদশাপ্রাপ্ত", "A dilapidated old house.", "এক জরাজীর্ণ পুরনো বাড়ি।", ["Ruinous", "Broken-down"], [], "General"),
    ("Dilatory", "/ˈdɪl.ə.tər.i/", "Adj", "দীর্ঘসূত্রী, গড়িমসি করা", "Dilatory tactics delayed trial.", "দীর্ঘসূত্রী কৌশল বিচার বিলম্বিত করল।", ["Slow", "Tardy"], [], "GRE"),
    ("Dilettante", "/ˌdɪl.əˈtæn.ti/", "Noun", "অগভীর চর্চাকারী, রসিক শখিন ব্যক্তি", "Art dilettante visited museum.", "শিল্পের শখিন ব্যক্তি জাদুঘর পরিদর্শনে এলেন।", ["Amateur", "Dabbler"], [], "Art"),
    ("Diligence", "/ˈdɪl.ɪ.dʒəns/", "Noun", "পরিশ্রম, নিষ্ঠা", "Rewarded for diligence.", "নিষ্ঠার জন্য পুরস্কৃত।", ["Industry", "Assiduity"], [], "BCS"),
    ("Diminution", "/ˌdɪm.ɪˈnjuː.ʃən/", "Noun", "হ্রাস, ক্ষয় পাওয়া", "Diminution of power.", "ক্ষমতার হ্রাস পাওয়া।", ["Reduction", "Lessen"], [], "Academic"),
    ("Diplomacy", "/dɪˈpləʊ.mə.si/", "Noun", "কূটনীতি, চতুর আচরণ", "Resolved via diplomacy.", "কূটনীতির মাধ্যমে সমাধান।", ["Statesmanship", "Tact"], [], "Politics"),
    ("Disapprobation", "/ˌdɪs.æp.rəˈbeɪ.ʃən/", "Noun", "অননুমোদন, নিন্দা", "Expressed disapprobation.", "অননুমোদন প্রকাশ করল।", ["Disapproval", "Censure"], [], "GRE"),
    ("Disarming", "/dɪsˈɑː.mɪŋ/", "Adj", "নির্দোষ, বিদ্বেষ দূরকারী", "A disarming smile.", "এক নির্দোষ অমায়িক হাসি।", ["Charming", "Winning"], [], "Spoken"),
    ("Discernment", "/dɪˈsɜːn.mənt/", "Noun", "সূক্ষ্ম বিচারবুদ্ধি, দেখা ও বোঝার ক্ষমতা", "Showed great discernment.", "চমৎকার বিচারবুদ্ধি দেখালো।", ["Insight", "Perception"], [], "Academic"),
    ("Disclaim", "/dɪsˈkleɪm/", "Verb", "দাবি অস্বীকার করা, দায়মুক্ত হওয়া", "Disclaimed all liability.", "সকল দায়বদ্ধতা অস্বীকার করল।", ["Deny", "Renounce"], [], "Legal"),
    ("Discomfiture", "/dɪsˈkʌm.fɪ.tʃər/", "Noun", "অপ্রস্তুত অবস্থা, অস্বস্তি", "Noticed his discomfiture.", "তার অস্বস্তি লক্ষ্য করল।", ["Embarrassment", "Confusion"], [], "GRE"),
    ("Disconsolate", "/dɪsˈkɒn.səl.ət/", "Adj", "সান্ত্বনাহীন, বিষণ্ণ", "A disconsolate widow.", "এক সান্ত্বনাহীন বিষণ্ণ বিধবা।", ["Inconsolable", "Sad"], [], "Literature"),
    ("Discordance", "/dɪsˈkɔː.dəns/", "Noun", "অমিল, অসঙ্গতি", "Discordance between reports.", "প্রতিবেদনের মধ্যে অসঙ্গতি।", ["Conflict", "Inconsistency"], [], "Academic"),
    ("Discreditable", "/dɪsˈkred.ɪ.tə.bəl/", "Adj", "অসম্মানজনক, মানহানিকর", "Discreditable behavior.", "অসম্মানজনক আচরণ।", ["Shameful", "Disgraceful"], [], "Spoken"),
    ("Discrepancy", "/dɪˈskrep.ən.si/", "Noun", "অসংগতি, অমিল", "A major discrepancy in accounts.", "হিসাবে এক বড় অসংগতি।", ["Inconsistency", "Mismatch"], [], "IELTS"),
    ("Discrete", "/dɪˈskriːt/", "Adj", "স্বতন্ত্র, আলাদা আলাদা", "Divided into discrete units.", "আলাদা আলাদা ইউনিটে বিভক্ত।", ["Separate", "Distinct"], [], "Science"),
    ("Discretionary", "/dɪˈskreʃ.ən.ər.i/", "Adj", "স্বাচ্ছন্দ্যমূলক, স্বেচ্ছাধীন", "Discretionary funds available.", "স্বেচ্ছাধীন তহবিল বিদ্যমান।", ["Optional", "Voluntary"], [], "Business"),
    ("Discriminating", "/dɪˈskrɪm.ɪ.neɪ.tɪŋ/", "Adj", "বিচক্ষণ, সূক্ষ্ম পারদর্শী", "A discriminating buyer.", "এক বিচক্ষণ ক্রেতা।", ["Perceptive", "Select"], [], "Academic"),
    ("Discursive", "/dɪˈskɜː.sɪv/", "Adj", "এলোমেলো, বিষয়ান্তরাশ্রয়ী", "A discursive essay.", "এক এলোমেলো প্রবন্ধ।", ["Rambling", "Digressive"], [], "GRE"),
    ("Disdainful", "/dɪsˈdeɪn.fəl/", "Adj", "অবজ্ঞাসূচক, ঘৃণাপূর্ণ", "A disdainful look.", "এক ঘৃণাপূর্ণ দৃষ্টি।", ["Scornful", "Contemptuous"], [], "Literature"),
    ("Disillusionment", "/ˌdɪs.ɪˈluː.ʒən.mənt/", "Noun", "মোহভঙ্গ, ভুল ধারণা ভাঙা", "Felt deep disillusionment.", "গভীর মোহভঙ্গ অনুভব করল।", ["Disenchantment"], [], "Psychology"),
    ("Disingenuous", "/ˌdɪs.ɪnˈdʒen.ju.əs/", "Adj", "কপট, অসাধু বা ছদ্মবেশধারী", "A disingenuous remark.", "এক কপট অসাধু মন্তব্য।", ["Insincere", "Deceitful"], [], "GRE"),
    ("Disinterested", "/dɪsˈɪn.tre.stɪd/", "Adj", "পক্ষপাতহীন, নিষ্কাম", "A disinterested judge.", "এক পক্ষপাতহীন বিচারক।", ["Impartial", "Unbiased"], [], "Academic"),
    ("Disjointed", "/dɪsˈdʒɔɪn.tɪd/", "Adj", "বিচ্ছিন্ন, সংহতিহীন", "A disjointed narrative.", "এক বিচ্ছিন্ন বিবরণী।", ["Disconnected", "Incoherent"], [], "Academic"),
    ("Dismantling", "/dɪsˈmæn.tlɪŋ/", "Noun", "খুলে ফেলা, ধ্বংসসাধন", "Dismantling of machinery.", "যন্ত্রপাতি খুলে ফেলা।", ["Disassembling"], [], "Technology"),
    ("Disparagement", "/dɪsˈpær.ɪdʒ.mənt/", "Noun", "অবমাননা, হেয়করণ", "Resented disparagement.", "অবমাননায় ক্ষুব্ধ হলো।", ["Belittling", "Defamation"], [], "GRE"),
    ("Dispassion", "/dɪsˈpæʃ.ən/", "Noun", "নিরপেক্ষতা, আবেগহীনতা", "Judged with dispassion.", "নিরপেক্ষতার সাথে বিচার করল।", ["Objectivity", "Calmness"], [], "Academic"),
    ("Dispatch", "/dɪˈspætʃ/", "Verb", "দ্রুত পাঠানো, শেষ করা", "Dispatched troops to border.", "সীমান্তে দ্রুত সৈন্য পাঠালো।", ["Send", "Expedite"], [], "Military"),
    ("Dispel", "/dɪˈspel/", "Verb", "দূর করা, দূর করে দেওয়া", "Dispel fear with truth.", "সত্য দিয়ে ভয় দূর করুন।", ["Dismiss", "Banish"], [], "Spoken"),
    ("Dispensable", "/dɪˈspen.sə.bəl/", "Adj", "অপ্রয়োজনীয়, বর্জনীয়", "Considered dispensable item.", "বর্জনীয় বস্তু হিসেবে গণ্য।", ["Unnecessary", "Superfluous"], [], "Spoken"),
    ("Dispersal", "/dɪˈspɜː.səl/", "Noun", "ছত্রভঙ্গ, ইতস্তত ছড়ানো", "Dispersal of crowd.", "ভিড়ের ছত্রভঙ্গ হওয়া।", ["Scattering", "Dissolution"], [], "General"),
    ("Disposition", "/ˌdɪs.pəˈzɪʃ.ən/", "Noun", "স্বভাব, মেজাজ বা বিন্যাস", "A sunny disposition.", "এক হাস্যোজ্জ্বল প্রফুল্ল স্বভাব।", ["Temperament", "Nature"], [], "Psychology"),
    ("Disreputable", "/dɪsˈrep.jə.tə.bəl/", "Adj", "কুখ্যাত, বদনামযুক্ত", "A disreputable character.", "এক কুখ্যাত বদনামযুক্ত চরিত্র।", ["Notorious", "Infamous"], [], "Spoken"),
    ("Disrepute", "/ˌdɪs.rɪˈpjuːt/", "Noun", "বদনাম, দুর্নাম", "Fell into disrepute.", "দুর্নামে পতিত হলো।", ["Disgrace", "Dishonor"], [], "General"),
    ("Dissect", "/daɪˈsekt/", "Verb", "ব্যবচ্ছেদ করা, সূক্ষ্ম বিশ্লেষণ করা", "Dissect argument in detail.", "যুক্তি সূক্ষ্মভাবে বিশ্লেষণ করা।", ["Analyze", "Examine"], [], "Science"),
    ("Dissemble", "/dɪˈsem.bəl/", "Verb", "ভান করা, সত্য লুকানো", "Dissemble true feelings.", "আসল অনুভূতি লুকানো।", ["Disguise", "Feign"], [], "GRE"),
    ("Disseminate", "/dɪˈsem.ɪ.neɪt/", "Verb", "প্রচার করা, ছড়িয়ে দেওয়া", "Disseminate information widely.", "তথ্য ব্যাপকভাবে প্রচার করা।", ["Broadcast", "Circulate"], [], "IELTS"),
    ("Dissension", "/dɪˈsen.ʃən/", "Noun", "মতবিরোধ, কোন্দল", "Dissension among members.", "সদস্যদের মধ্যে মতবিরোধ।", ["Discord", "Disagreement"], [], "Politics"),
    ("Dissenting", "/dɪˈsen.tɪŋ/", "Adj", "ভিন্নমত পোষণকারী", "Dissenting voice in council.", "কাউন্সিলে ভিন্নমত পোষণকারী কণ্ঠ।", ["Opposing", "Disagreeing"], [], "Politics"),
    ("Disservice", "/dɪsˈsɜː.vɪs/", "Noun", "ক্ষতি, অপকার", "Did a disservice to team.", "দলের অপকার করল।", ["Harm", "Injury"], [], "Spoken"),
    ("Dissident", "/ˈdɪs.ɪ.dənt/", "Adj", "বিদ্রোহী, রাজদ্রোহী", "Dissident groups united.", "বিদ্রোহী দলগুলো এক হলো।", ["Rebellious", "Dissenter"], [], "Politics"),
    ("Dissimilarity", "/dɪˌsɪm.ɪˈlær.ə.ti/", "Noun", "বৈসাদৃশ্য, অমিল", "Noticeable dissimilarity.", "লক্ষণীয় বৈসাদৃশ্য।", ["Difference", "Unlikeness"], [], "Academic"),
    ("Dissimulation", "/dɪˌsɪm.jəˈleɪ.ʃən/", "Noun", "ছলনা, ভণ্ডামি", "Practiced dissimulation.", "ছলনার আশ্রয় নিল।", ["Hypocrisy", "Deceit"], [], "GRE"),
    ("Dissipate", "/ˈdɪs.ɪ.peɪt/", "Verb", "উবে যাওয়া, অপচয় করা", "Clouds dissipated quickly.", "মেঘ দ্রুত উবে গেল।", ["Disperse", "Squander"], [], "Science"),
    ("Dissolute", "/ˈdɪs.ə.luːt/", "Adj", "উচ্ছৃঙ্খল, চরিত্রহীন", "Led a dissolute life.", "এক চরিত্রহীন জীবন যাপন করল।", ["Licentious", "Debauched"], [], "GRE"),
    ("Dissolution", "/ˌdɪs.əˈluː.ʃən/", "Noun", "বিলুপ্তি, অবসান", "Dissolution of parliament.", "পার্লামেন্টের বিলুপ্তি।", ["Termination", "Disintegration"], [], "Politics"),
    ("Dissonant", "/ˈdɪs.ə.nənt/", "Adj", "বেসুরো, খটকা লাগা", "Dissonant sounds echoed.", "বেসুরো শব্দ প্রতিধ্বনিত হলো।", ["Inharmonious", "Harsh"], [], "Music"),
    ("Dissuade", "/dɪˈsweɪd/", "Verb", "নিবারণ করা, মত ফেরানো", "Dissuaded him from leaving.", "তাকে যাওয়া থেকে নিবারণ করল।", ["Deter", "Discourage"], [], "Spoken"),
    ("Distend", "/dɪˈstend/", "Verb", "স্ফীত করা, ফুলে ওঠা", "Stomach distended after meal.", "খাবারের পর পেট ফুলে উঠল।", ["Swell", "Bloat"], [], "Medical"),
    ("Distillation", "/ˌdɪs.tɪˈleɪ.ʃən/", "Noun", "পাতন, নির্যাস বের করা", "Distillation of wisdom.", "জ্ঞানের নির্যাস বের করা।", ["Purification", "Extraction"], [], "Science"),
    ("Distinctive", "/dɪˈstɪŋk.tɪv/", "Adj", "স্বতন্ত্র, অনন্য বৈশিষ্ট্যমণ্ডিত", "A distinctive smell.", "এক অনন্য বিশিষ্ট গন্ধ।", ["Unique", "Characteristic"], [], "Spoken"),
    ("Distortion", "/dɪˈstɔː.ʃən/", "Noun", "বিকৃতি, বিকৃত রূপ", "Distortion of truth.", "সত্যের বিকৃতি।", ["Misrepresentation", "Deformity"], [], "Academic"),
    ("Distraught", "/dɪˈstrɔːt/", "Adj", "উদ্বিগ্ন, চরম ব্যাকুল", "Distraught mother searched for child.", "উদ্বিগ্ন মা সন্তানকে খুঁজছিলেন।", ["Agitated", "Upset"], [], "Literature"),
    ("Diurnal", "/daɪˈɜː.nəl/", "Adj", "দিবাচর, দিনকালীন", "Diurnal animals active in day.", "দিবাচর প্রাণীরা দিনে সক্রিয়।", ["Daily", "Daytime"], [], "Biology"),
    ("Divergence", "/daɪˈvɜː.dʒəns/", "Noun", "বিচ্যুতি, ভিন্নমুখী প্রসার", "Divergence of opinions.", "মতামতের ভিন্নমুখী প্রসার।", ["Deviation", "Difference"], [], "Academic"),
    ("Diversion", "/daɪˈvɜː.ʃən/", "Noun", "মনোযোগ সরানো, পথ পরিবর্তন", "Traffic diversion created queue.", "যানবাহনের পথ পরিবর্তন জট সৃষ্টি করল।", ["Detour", "Distraction"], [], "General"),
    ("Divest", "/daɪˈvest/", "Verb", "বঞ্চিত করা, সরিয়ে নেওয়া", "Divested of authority.", "কর্তৃত্ব থেকে বঞ্চিত।", ["Strip", "Deprive"], [], "Business"),
    ("Divination", "/ˌdɪv.ɪˈneɪ.ʃən/", "Noun", "ভবিষ্যদ্বাণী, গণকবিদ্যা", "Ancient art of divination.", "ভবিষ্যদ্বাণীর প্রাচীন বিদ্যা।", ["Prophecy", "Foretelling"], [], "Culture"),
    ("Divulsive", "/daɪˈvʌl.sɪv/", "Adj", "বিচ্ছিন্নকারী, উৎপাটনকারী", "Divulsive forces split state.", "বিচ্ছিন্নকারী শক্তি রাজ্য ভাগ করল।", ["Splitting", "Tearing"], [], "History"),
    ("Docility", "/dəʊˈsɪl.ə.ti/", "Noun", "সহজবশ্যতা, শান্ত অনুগত স্বভাব", "Admired for docility.", "শান্ত অনুগত স্বভাবের জন্য প্রশংসিত।", ["Obedience", "Submissiveness"], [], "General"),
    ("Doctrinaire", "/ˌdɒk.trɪˈneər/", "Adj", "গোঁড়া মতবাদী, অনমনীয় কট্টর", "Doctrinaire approach failed.", "গোঁড়া মতবাদী পন্থা ব্যর্থ হলো।", ["Dogmatic", "Rigid"], [], "Politics"),
    ("Doggedly", "/ˈdɒɡ.ɪd.li/", "Adv", "অদম্যভাবে, নাছোড়বান্দার মতো", "Pursued goal doggedly.", "লক্ষ্য অদম্যভাবে তাড়া করল।", ["Persistently", "Tenaciously"], [], "Spoken"),
    ("Doggerel", "/ˈdɒɡ.ər.əl/", "Noun", "তুচ্ছ হালকা চটুল কবিতা", "Wrote humorous doggerel.", "কৌতুকপূর্ণ চটুল কবিতা লিখল।", ["Trite verse"], [], "Literature"),
    ("Dogmatism", "/ˈdɒɡ.mə.tɪ.zəm/", "Noun", "গোঁড়ামি, মতান্ধতা", "Avoid political dogmatism.", "রাজনৈতিক গোঁড়ামি পরিহার করুন।", ["Fanaticism", "Bigotry"], [], "Politics"),
    ("Doldrums", "/ˈdɒl.drəmz/", "Noun", "হতাশাগ্রস্ত নিস্তেজ অবস্থা", "Economy in doldrums.", "অর্থনীতি নিস্তেজ অবস্থায়।", ["Inactivity", "Stagnation"], [], "Economics"),
    ("Dolefully", "/ˈdəʊl.fə.li/", "Adv", "করুণভাবে, বিষাদগ্রস্ত অবস্থায়", "Looked dolefully at rain.", "বৃষ্টির দিকে করুণভাবে তাকালো।", ["Mournfully", "Sorrowfully"], [], "Literature"),
    ("Doltish", "/ˈdəʊl.tɪʃ/", "Adj", "বোকা, নির্বোধ প্রকৃতির", "Doltish mistake lost game.", "নির্বোধ ভুলে ম্যাচ হারল।", ["Stupid", "Foolish"], [], "Spoken"),
    ("Domain", "/dəˈmeɪn/", "Noun", "সাম্রাজ্য, ক্ষেত্র বা এলাকা", "Domain of science.", "বিজ্ঞানের ক্ষেত্র।", ["Realm", "Field"], [], "Academic"),
    ("Domicile", "/ˈdɒm.ɪ.saɪl/", "Noun", "স্থায়ী বাসস্থান, ঠিকানা", "Proof of domicile required.", "স্থায়ী বাসস্থানের প্রমাণ প্রয়োজন।", ["Residence", "Home"], [], "Legal"),
    ("Domineering", "/ˌdɒm.ɪˈnɪə.rɪŋ/", "Adj", "প্রভুত্বব্যঞ্জক, হুকুমদার", "A domineering personality.", "এক প্রভুত্বব্যঞ্জক ব্যক্তিত্ব।", ["Imperious", "Arrogant"], [], "Spoken"),
    ("Donation", "/dəʊˈneɪ.ʃən/", "Noun", "অনুদান, দান", "Generous donation received.", "উদার অনুদান পাওয়া গেল।", ["Contribution", "Gift"], [], "Spoken"),
    ("Dormancy", "/ˈdɔː.mən.si/", "Noun", "সুপ্তাবস্থা, নিষ্ক্রিয়তা", "Seeds in dormancy.", "বীজ সুপ্তাবস্থায়।", ["Inactivity", "Latency"], [], "Biology"),
    ("Dormant", "/ˈdɔː.mənt/", "Adj", "সুপ্ত, ঘুমন্ত বা নিষ্ক্রিয়", "Dormant volcano erupted.", "সুপ্ত আগ্নেয়গিরি জেগে উঠল।", ["Inactive", "Sleeping"], [], "Science"),
    ("Dorsal", "/ˈdɔː.səl/", "Adj", "পৃষ্ঠদেশীয়, পিঠের দিকের", "Dorsal fin of shark.", "হাঙরের পিঠের ফিন।", ["Back"], [], "Biology"),
    ("Dotage", "/ˈdəʊ.tɪdʒ/", "Noun", "বার্ধক্যজনিত স্মৃতিভ্রম", "Cared for in dotage.", "বার্ধক্যজনিত স্মৃতিভ্রমে সেবা করা হলো।", ["Old age", "Senility"], [], "Literature"),
    ("Doughty", "/ˈdaʊ.ti/", "Adj", "সাহসী, বীরত্বপূর্ণ", "A doughty warrior fought.", "এক বীরত্বপূর্ণ যোদ্ধা লড়ল।", ["Brave", "Valiant"], [], "Literature"),
    ("Dourness", "/ˈdaʊə.nəs/", "Noun", "কঠোরতা, বিষণ্ণ মুখভাব", "Known for dourness.", "কঠোর বিষণ্ণ ভাবের জন্য পরিচিত।", ["Sternness", "Gloomy"], [], "Literature"),
    ("Douse", "/daʊs/", "Verb", "নেভানো, তরলে ডোবানো", "Douse fire with water.", "পানি দিয়ে আগুন নেভান।", ["Extinguish", "Soak"], [], "General"),
    ("DOWDY", "/ˈdaʊ.di/", "Adj", "অনাকর্ষক, অমাজিত পোষাক পরিহিত", "A dowdy dress.", "এক অমাজিত অনাকর্ষক পোশাক।", ["Unfashionable", "Plain"], [], "Fashion"),
    ("Downfall", "/ˈdaʊn.fɔːl/", "Noun", "পতন, ধ্বংস", "Arrogance caused downfall.", "অহংকার পতনের কারণ হলো।", ["Ruin", "Collapse"], [], "History"),
    ("Draconian", "/drəˈkəʊ.ni.ən/", "Adj", "চরম কঠোর, নির্দয় ও নিষ্ঠুর", "Draconian laws imposed.", "চরম কঠোর আইন চাপানো হলো।", ["Harsh", "Severe"], [], "Legal"),
    ("Drastic", "/ˈdræs.tɪk/", "Adj", "চরম, আশু জোরালো পদক্ষেপ", "Drastic measures needed.", "চরম জোরালো পদক্ষেপ প্রয়োজন।", ["Extreme", "Severe"], [], "Spoken"),
    ("Drawback", "/ˈdrɔː.bæk/", "Noun", "ত্রুটি, সীমাবদ্ধতা", "Major drawback of plan.", "পরিকল্পনার মূল ত্রুটি।", ["Disadvantage", "Snag"], [], "Spoken"),
    ("Drollery", "/ˈdrəʊ.lər.i/", "Noun", "কৌতুকপ্রদতা, হাসির কাণ্ড", "Amused by drollery.", "কৌতুকপ্রদতায় আনন্দিত হলো।", ["Wag", "Jesting"], [], "Literature"),
    ("Drudgery", "/ˈdrʌdʒ.ər.i/", "Noun", "একঘেয়ে খাটাখাটুনি", "Relieved from drudgery.", "একঘেয়ে খাটাখাটুনি থেকে মুক্তি।", ["Hard work", "Toil"], [], "Spoken"),
    ("Dualism", "/ˈdʒuː.ə.lɪ.zəm/", "Noun", "দ্বৈতবাদ", "Dualism of mind and body.", "মন ও শরীরের দ্বৈতবাদ।", ["Duality"], [], "Philosophy"),
    ("Dubiety", "/djuːˈbaɪ.ə.ti/", "Noun", "সন্দেহ, অনিশ্চয়তা", "Expressed dubiety about claims.", "দাবিতে সন্দেহ প্রকাশ করল।", ["Doubt", "Uncertainty"], [], "GRE"),
    ("Dulcimer", "/ˈdʌl.sɪ.mər/", "Noun", "একপ্রকার বাদ্যযন্ত্র", "Played a sweet dulcimer tune.", "এক মিষ্টি সুরের বাদ্যযন্ত্র বাজালো।", ["Musical instrument"], [], "Music"),
    ("Dupe", "/dʒuːp/", "Verb", "প্রতারিত করা, বোকা বানানো", "Duped by scammer.", "প্রতারকের দ্বারা প্রতারিত হলো।", ["Deceive", "Trick"], [], "Spoken"),
    ("Duplication", "/ˌdʒuː.plɪˈkeɪ.ʃən/", "Noun", "অনুলিপি, দ্বিত্বকরণ", "Avoid duplication of effort.", "চেষ্টার দ্বিত্বকরণ এড়ান।", ["Repetition", "Copying"], [], "Academic"),
    ("Duress", "/dʒuːˈres/", "Noun", "বলপ্রয়োগ, ভয় দেখিয়ে বাধ্যকরণ", "Signed contract under duress.", "ভয় দেখিয়ে বাধ্য করে চুক্তিতে সই নিল।", ["Coercion", "Compulsion"], [], "Legal"),
    ("Duty-bound", "/ˈdʒuː.ti.baʊnd/", "Adj", "দায়বদ্ধ, কর্তব্যপরায়ণ", "Felt duty-bound to report.", "প্রতিবেদন দিতে নিজেকে দায়বদ্ধ মনে করল।", ["Obligated", "Bound"], [], "Legal"),
    ("Dwindle", "/ˈdwɪn.dəl/", "Verb", "কমে যাওয়া, হ্রাস পাওয়া", "Resources dwindled fast.", "সম্পদ দ্রুত কমে গেল।", ["Shrink", "Decrease"], [], "General"),
    ("Dynamic", "/daɪˈnæm.ɪk/", "Adj", "গতিশীল, প্রাণবন্ত", "A dynamic leader inspiring all.", "এক গতিশীল নেতা সবাইকে অনুপ্রাণিত করছে।", ["Energetic", "Vibrant"], [], "Spoken")
]

all_candidates.extend(more_entries)

# Filter unique entries against existing database
selected_300 = []
seen = set()

for item in all_candidates:
    w = item[0].strip()
    k = w.lower()
    if k not in existing_words and k not in seen:
        seen.add(k)
        selected_300.append(item)
        if len(selected_300) == 300:
            break

print(f"Filter completed. Selected {len(selected_300)} unique words for batch 2!")

# Append to app/src/main/assets/dictionary_1000.json
dict_path = 'app/src/main/assets/dictionary_1000.json'
with open(dict_path, 'r', encoding='utf-8') as f:
    current_dict = json.load(f)

max_id = max([item.get('id', 0) for item in current_dict if isinstance(item.get('id'), int)] or [0])

new_json_items = []
for idx, item in enumerate(selected_300, start=max_id+1):
    w, ph, pos, mean, exE, exB, syns, ants, cat = item
    entry = {
        "id": idx,
        "word": w,
        "pos": pos,
        "phonetic": ph,
        "meaningBn": mean,
        "exampleEn": exE,
        "exampleBn": exB,
        "synonyms": syns,
        "antonyms": ants,
        "category": cat,
        "packId": "extra_300_batch2"
    }
    new_json_items.append(entry)

current_dict.extend(new_json_items)

with open(dict_path, 'w', encoding='utf-8') as f:
    json.dump(current_dict, f, ensure_ascii=False, indent=2)

print(f"Successfully added {len(new_json_items)} new words to {dict_path}!")

with open('new_batch2_300_words.json', 'w', encoding='utf-8') as f:
    json.dump(new_json_items, f, ensure_ascii=False, indent=2)


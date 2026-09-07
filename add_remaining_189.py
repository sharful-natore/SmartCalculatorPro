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

print(f"Total existing unique words before remaining generation: {len(existing_words)}")

# Generative candidate pool for letters E through Z
pool_remaining = [
    # E
    ("Ebullience", "/ɪˈbʌl.i.əns/", "Noun", "উচ্ছ্বাস, প্রফুল্লতা", "Her ebullience cheered everyone up.", "তার উচ্ছ্বাস সবাইকে আনন্দিত করল।", ["Exuberance", "High spirits"], ["Depression"], "Psychology"),
    ("Eccentricity", "/ˌek.senˈtrɪs.ə.ti/", "Noun", "অদ্ভুত স্বভাব, খামখেয়ালীপনা", "Known for harmless eccentricity.", "ক্ষতিহীন অদ্ভুত স্বভাবের জন্য পরিচিত।", ["Quirk", "Peculiarity"], ["Normalcy"], "Spoken"),
    ("Echolalia", "/ˌek.əʊˈleɪ.li.ə/", "Noun", "কথার অবিকল পুনরাবৃত্তি", "Echolalia observed in child.", "শিশুর মধ্যে কথার অবিকল পুনরাবৃত্তি দেখা গেল।", ["Repetition"], [], "Medical"),
    ("Ecstatic", "/ɪkˈstæt.ɪk/", "Adj", "পরমানন্দিত, মহা আনন্দিত", "Ecstatic crowd cheered team.", "পরমানন্দিত জনতা দলকে হাততালি দিল।", ["Elated", "Overjoyed"], ["Despondent"], "Spoken"),
    ("Edifying", "/ˈed.ɪ.faɪ.ɪŋ/", "Adj", "উপদেশমূলক, চরিত্র গঠনকারী", "An edifying lecture on ethics.", "নৈতিকতার ওপর এক উপদেশমূলক বক্তৃতা।", ["Instructive", "Enlightening"], [], "Academic"),
    ("Effervescent", "/ˌef.əˈves.ənt/", "Adj", "বুদ্বুদময়, প্রাণবন্ত ও প্রফুল্ল", "An effervescent personality.", "এক প্রাণবন্ত প্রফুল্ল ব্যক্তিত্ব।", ["Bubbly", "Lively"], ["Subdued"], "Spoken"),
    ("Efficacy", "/ˈef.ɪ.kə.si/", "Noun", "কার্যকারিতা, ফলপ্রসূতা", "Proven efficacy of drug.", "ওষুধের প্রমাণিত কার্যকারিতা।", ["Effectiveness", "Potency"], [], "Medical"),
    ("Efflorescence", "/ˌef.lɔːˈres.əns/", "Noun", "বিকাশ, ফুল ফোটার সময়", "Cultural efflorescence in Renaissance.", "রেনেসাঁ যুগে সাংস্কৃতিক বিকাশ।", ["Blossoming", "Flourishing"], [], "History"),
    ("Effrontery", "/ɪˈfrʌn.tər.i/", "Noun", "ধৃষ্টতা, আস্পর্ধা", "Had effrontery to interrupt.", "কথা কাটার ধৃষ্টতা দেখালো।", ["Audacity", "Impudence"], [], "GRE"),
    ("Effulgence", "/ɪˈfʌl.dʒəns/", "Noun", "উজ্জ্বল আলো, দীপ্তি", "Effulgence of full moon.", "পূর্ণিমার চাঁদের উজ্জ্বল দীপ্তি।", ["Radiance", "Brilliance"], [], "Literature"),
    ("Egalitarianism", "/ɪˌɡæl.ɪˈteər.i.ə.nɪ.zəm/", "Noun", "সাম্যবাদ, সমাধিকারবাদ", "Promote egalitarianism in society.", "সমাজে সমাধিকারবাদ প্রচার করা।", ["Equality"], [], "Politics"),
    ("Egregious", "/ɪˈɡriː.dʒəs/", "Adj", "মারাত্মক, জঘন্য অপকর্ম", "An egregious error in calculation.", "হিসাবে এক মারাত্মক ভুল।", ["Flagrant", "Glaring"], [], "BCS"),
    ("Egress", "/ˈiː.ɡres/", "Noun", "প্রস্থান, বাইরে যাওয়ার পথ", "Egress blocked by fire.", "আগুনে প্রস্থান পথ অবরুদ্ধ হলো।", ["Exit", "Departure"], ["Ingress"], "General"),
    ("Elaboration", "/ɪˌlæb.əˈreɪ.ʃən/", "Noun", "বিস্তারিত বিবরণ", "Needs further elaboration.", "আরও বিস্তারিত বিবরণ প্রয়োজন।", ["Expansion", "Detailing"], [], "Academic"),
    ("Elated", "/ɪˈleɪ.tɪd/", "Adj", "মহা আনন্দিত, উল্লাসিত", "Elated by victory.", "জয়ে মহা আনন্দিত।", ["Ecstatic", "Thrilled"], ["Dejected"], "Spoken"),
    ("Electorate", "/iˈlek.tər.ət/", "Noun", "ভোটারমণ্ডলী, নির্বাচনী জনগণ", "Electorate voted wisely.", "ভোটারমণ্ডলী বিজ্ঞতার সাথে ভোট দিল।", ["Voters"], [], "Politics"),

    # F
    ("Fabrication", "/ˌfæb.rɪˈkeɪ.ʃən/", "Noun", "বানানো গল্প, মিথ্যা রচনা", "Story was pure fabrication.", "গল্পটি ছিল খাঁটি বানানো মিথ্যা।", ["Lie", "Forgery"], [], "Spoken"),
    ("Facetious", "/fəˈsiː.ʃəs/", "Adj", "তামাশাপূর্ণ, ফাজিল কৌতুকভরা", "A facetious remark during speech.", "বক্তৃতার মাঝে এক তামাশাপূর্ণ মন্তব্য।", ["Flippant", "Joking"], ["Serious"], "GRE"),
    ("Facilitation", "/fəˌsɪl.ɪˈteɪ.ʃən/", "Noun", "সহজীকরণ, সহায়তা", "Facilitation of trade agreement.", "বাণিজ্য চুক্তির সহজীকরণ।", ["Assistance", "Easing"], [], "Business"),
    ("Factionalism", "/ˈfæk.ʃən.əl.ɪ.zəm/", "Noun", "দলবাজি, উপদলীয় কোন্দল", "Factionalism weakened party.", "দলাদলি দলটিকে দুর্বল করল।", ["In-fighting", "Division"], [], "Politics"),
    ("Fallaciousness", "/fəˈleɪ.ʃəs.nəs/", "Noun", "যুক্তিহীনতা, ভ্রান্তি", "Exposed fallaciousness of logic.", "যুক্তির ভ্রান্তি উন্মোচিত করল।", ["Falsehood", "Invalidity"], [], "Academic"),
    ("Fallibility", "/ˌfæl.əˈbɪl.ə.ti/", "Noun", "ভুল করার সম্ভাবনা", "Human fallibility acknowledged.", "মানুষের ভুল করার সম্ভাবনা স্বীকৃত।", ["Imperfection"], ["Infallibility"], "Psychology"),
    ("Falsification", "/ˌfɔːl.sɪ.fɪˈkeɪ.ʃən/", "Noun", "জালিয়াতি, বিকৃতকরণ", "Falsification of records.", "নথিপত্রের জালিয়াতি।", ["Forgery", "Distortion"], [], "Legal"),
    ("Fanaticism", "/fəˈnæt.ɪ.sɪ.zəm/", "Noun", "অন্ধ ধর্মান্ধতা, উগ্রতা", "Religious fanaticism caused conflict.", "ধর্মীয় উগ্রতা সংঘাত সৃষ্টি করল।", ["Extremism", "Bigotry"], [], "Sociology"),

    # G
    ("Galvanize", "/ˈɡæl.və.naɪz/", "Verb", "তৎপর করা, উদ্দীপিত করা", "Speech galvanized public.", "ভাষণ জনগণকে উদ্দীপিত করল।", ["Rouse", "Spur"], ["Dampen"], "IELTS"),
    ("Garrulity", "/ɡæˈruː.lə.ti/", "Noun", "বাচালতা, অহেতুক অতিরিক্ত কথা", "Annoyed by his garrulity.", "তার বাচালতায় বিরক্ত হলো।", ["Talkativeness", "Loquacity"], [], "GRE"),
    ("Gaudiness", "/ˈɡɔː.di.nəs/", "Noun", "চটকদার রঙচঙে ভাব", "Gaudiness of decorations.", "সজ্জার চটকদার রঙচঙে ভাব।", ["Flashiness", "Tastelessness"], [], "Fashion"),
    ("Genealogy", "/ˌdʒiː.niˈæl.ə.dʒi/", "Noun", "বংশতালিকা, কুলজী", "Researched family genealogy.", "পরিবারের বংশতালিকা গবেষণা করল।", ["Lineage", "Pedigree"], [], "History"),
    ("Generosity", "/ˌdʒen.əˈrɒs.ə.ti/", "Noun", "উদারতা, দানশীলতা", "Praised for generosity.", "উদারতার জন্য প্রশংসিত।", ["Magnanimity", "Bounty"], [], "Spoken"),

    # H
    ("Hackneyed", "/ˈhæk.niːd/", "Adj", "ঘিসাপিটা, গতানুগতিক", "Hackneyed slogans avoided.", "গতানুগতিক ঘিসাপিটা স্লোগান এড়ানো হলো।", ["Trite", "Cliché"], ["Original"], "GRE"),
    ("Haggle", "/ˈhæɡ.əl/", "Verb", "দরদাম করা", "Haggled over price of fish.", "মাছের দাম নিয়ে দরদাম করল।", ["Bargain"], [], "Spoken"),
    ("Hallowed", "/ˈhæl.əʊd/", "Adj", "পবিত্র, পূজনীয়", "Hallowed halls of university.", "বিশ্ববিদ্যালয়ের পবিত্র প্রাঙ্গণ।", ["Sacred", "Holy"], [], "Academic"),
    ("Haphazard", "/hæpˈhæz.əd/", "Adj", "আকাঙ্ক্ষাহীন, বিশৃঙ্খল ও এলোমেলো", "Haphazard arrangement of books.", "বইয়ের এলোমেলো বিশৃঙ্খল বিন্যাস।", ["Random", "Disorganized"], ["Orderly"], "General"),

    # I
    ("Iconoclastic", "/aɪˌkɒn.əˈklæs.tɪk/", "Adj", "প্রথাভাঙা, ঐতিহ্যবিরোধী", "An iconoclastic artist.", "এক প্রথাভাঙা শিল্পী।", ["Rebellious", "Subversive"], [], "Art"),
    ("Idiosyncratic", "/ˌɪd.i.ə.sɪŋˈkræt.ɪk/", "Adj", "ব্যক্তিগত বিশেষ স্বভাবমণ্ডিত", "An idiosyncratic writing style.", "এক ব্যক্তিগত বিশেষ স্বভাবমণ্ডিত লিখনশৈলী।", ["Peculiar", "Quirky"], [], "Literature"),
    ("Illuminating", "/ɪˈluː.mɪ.neɪ.tɪŋ/", "Adj", "আলোকপাতকারী, স্পষ্টকারী", "An illuminating insight.", "এক আলোকপাতকারী সূক্ষ্ম পর্যবেক্ষণ।", ["Enlightening", "Revealing"], [], "Academic"),
    ("Illusive", "/ɪˈluː.sɪv/", "Adj", "মায়াবী, বিভ্রান্তিকর", "Illusive promise of easy wealth.", "সহজ সম্পদের মায়াবী প্রতিশ্রুতি।", ["Deceptive", "Illusory"], [], "Literature"),

    # J
    ("Jovial", "/ˈdʒəʊ.vi.əl/", "Adj", "উল্লাসিত, হাসিখুশি ও প্রাণবন্ত", "A jovial host greeted guests.", "এক হাসিখুশি প্রফুল্ল মেজবান অতিথিদের বরণ করল।", ["Cheerful", "Merry"], ["Gloomy"], "Spoken"),
    ("Jubilant", "/ˈdʒuː.bɪ.lənt/", "Adj", "উচ্ছ্বসিত, বিজয়ানন্দিত", "Jubilant fans celebrated win.", "উচ্ছ্বসিত ভক্তরা জয় উদযাপন করল।", ["Ecstatic", "Triumphant"], [], "Sports"),
    ("Judicious", "/dʒuːˈdɪʃ.əs/", "Adj", "বিচক্ষণ, সুবিবেচক", "Judicious use of resources.", "সম্পদের সুবিবেচক ব্যবহার।", ["Prudent", "Wise"], ["Foolish"], "IELTS"),

    # L
    ("Labyrinthine", "/ˌlæb.əˈrɪn.θaɪn/", "Adj", "জটিল, ধাঁধাময় গলি বা পথ", "Labyrinthine passages of old castle.", "পুরনো দুর্গের জটিল ধাঁধাময় গলি।", ["Complex", "Intricate"], [], "Literature"),
    ("Lachrymose", "/ˈlæk.rɪ.məʊs/", "Adj", "অশ্রুসজল, কাঁদানে", "A lachrymose drama movie.", "এক অশ্রুসজল কাঁদানে ড্রামা সিনেমা।", ["Tearful", "Mournful"], [], "Literature"),
    ("Lamentable", "/ˈlæm.ən.tə.bəl/", "Adj", "শোচনীয়, দুঃখজনক", "Lamentable state of roads.", "রাস্তার শোচনীয় অবস্থা।", ["Regrettable", "Deplorable"], [], "General"),

    # M
    ("Magnanimity", "/ˌmæɡ.nəˈnɪm.ə.ti/", "Noun", "মহানুভবতা, উদারতা", "Showed magnanimity to enemy.", "শত্রুর প্রতি মহানুভবতা দেখালো।", ["Generosity", "Nobility"], [], "BCS"),
    ("Malevolence", "/məˈlev.əl.əns/", "Noun", "বিদ্বেষ, হিংসা", "Gared with malevolence.", "হিংসার সাথে তাকালো।", ["Malice", "Hostility"], [], "Literature"),
    ("Malleability", "/ˌmæl.i.əˈbɪl.ə.ti/", "Noun", "নমনীয়তা, রূপান্তরযোগ্যতা", "Malleability of silver metal.", "রূপা ধাতুর নমনীয়তা।", ["Flexibility", "Pliant"], [], "Science"),

    # N
    ("Noxiousness", "/ˈnɒk.ʃəs.nəs/", "Noun", "বিষাক্ততা, অনিষ্টকারিতা", "Noxiousness of industrial fumes.", "শিল্পকারখানার ধোঁয়ার বিষাক্ততা।", ["Toxicity", "Harmfulness"], [], "Science"),
    ("Nullification", "/ˌnʌl.ɪ.fɪˈkeɪ.ʃən/", "Noun", "বাতিলকরণ, রদ", "Nullification of contract.", "চুক্তির বাতিলকরণ।", ["Cancellation", "Invalidation"], [], "Legal"),

    # O
    ("Obfuscation", "/ˌɒb.fʌsˈkeɪ.ʃən/", "Noun", "অস্পষ্টকরণ, বিভ্রান্তি তৈরি", "Deliberate obfuscation of truth.", "সত্যের ইচ্ছাকৃত অস্পষ্টকরণ।", ["Confusion", "Obscurity"], [], "Academic"),
    ("Obliteration", "/əˌblɪt.əˈreɪ.ʃən/", "Noun", "সম্পূর্ণ চিহ্ন মুছে ফেলা", "Obliteration of evidence.", "প্রমাণের চিহ্ন সম্পূর্ণ মুছে ফেলা।", ["Destruction", "Erasing"], [], "Legal"),

    # P
    ("Pacific", "/pəˈsɪf.ɪk/", "Adj", "শান্তিকামী, শান্ত বা অমায়িক", "Pacific nature of negotiation.", "আলোচনার শান্তিকামী ধরণ।", ["Peaceful", "Calm"], [], "Politics"),
    ("Panegyric", "/ˌpæn.əˈdʒɪr.ɪk/", "Noun", "প্রশংসাগীতি, অতিস্তুতিভরা ভাষণ", "Delivered a panegyric for hero.", "বীরদের জন্য প্রশংসাগীতি পেশ করল।", ["Eulogy", "Tribute"], [], "Literature"),
    ("Paramount", "/ˈpær.ə.maʊnt/", "Adj", "সর্বোচ্চ, সর্বাধিক গুরুত্বপূর্ণ", "Safety is of paramount importance.", "নিরাপত্তা সর্বাধিক গুরুত্বপূর্ণ।", ["Supreme", "Chief"], [], "Academic"),
    ("Perseverance", "/ˌpɜː.sɪˈvɪə.rəns/", "Noun", "অধ্যবসায়, একনিষ্ঠ চেষ্টা", "Perseverance leads to success.", "অধ্যবসায় সাফল্য এনে দেয়।", ["Persistence", "Diligence"], [], "Spoken"),
    ("Placating", "/pləˈkeɪ.tɪŋ/", "Adj", "শান্ত করার প্রয়াসী", "A placating gesture to crowd.", "জনতাকে শান্ত করার এক প্রয়াস।", ["Conciliatory", "Soothing"], [], "Politics"),
    ("Polysyllabic", "/ˌpɒl.i.sɪˈlæb.ɪk/", "Adj", "বহু-অক্ষর বিশিষ্ট, জটিল শব্দভরা", "Polysyllabic words in text.", "পাঠ্যে বহু-অক্ষর বিশিষ্ট জটিল শব্দসমূহ।", ["Complex"], [], "Linguistics"),
    ("Ponderous", "/ˈpɒn.dər.əs/", "Adj", "ভারী, ধীরগতিসম্পন্ন ও একঘেয়ে", "A ponderous style of speech.", "ভাষণের এক ভারী একঘেয়ে শৈলী।", ["Heavy", "Lumbering"], [], "Literature"),
    ("Preeminence", "/priːˈem.ɪ.nəns/", "Noun", "শ্রেষ্ঠত্ব, প্রাধান্য", "Achieved preeminence in field.", "ক্ষেত্রের শ্রেষ্ঠত্ব অর্জন করল।", ["Superiority", "Dominance"], [], "Academic"),
    ("Premeditation", "/ˌpriː.med.ɪˈteɪ.ʃən/", "Noun", "পূর্বপরিকল্পনা", "Crime committed with premeditation.", "পূর্বপরিকল্পনার মাধ্যমে অপরাধ করা হয়েছিল।", ["Planning", "Forethought"], [], "Legal"),

    # Q - Z
    ("Quaintness", "/ˈkweɪnt.nəs/", "Noun", "মনোরম প্রাচীন বৈচিত্র্য", "Quaintness of old village.", "পুরনো গ্রামের মনোরম বৈচিত্র্য।", ["Charming oddity"], [], "Literature"),
    ("Quiescence", "/kwiˈes.əns/", "Noun", "স্থগিততা, নীরব সুপ্তাবস্থা", "Period of quiescence before eruption.", "উৎপাতের আগে নীরব সুপ্তাবস্থা।", ["Dormancy", "Quietness"], [], "Science"),
    ("Ramification", "/ˌræm.ɪ.fɪˈkeɪ.ʃən/", "Noun", "সুদূরপ্রসারী শাখা-প্রশাখা, ফলাফল", "Serious ramifications of policy.", "নীতির গুরুতর সুদূরপ্রসারী ফলাফল।", ["Consequence", "Outcome"], [], "Business"),
    ("Recalcitrance", "/rɪˈkæl.sɪ.trəns/", "Noun", "অবাধ্যতা, জেদ", "Punished for recalcitrance.", "অবাধ্যতার জন্য শাস্তি দিল।", ["Stubbornness", "Defiance"], [], "GRE"),
    ("Reconciliation", "/ˌrek.ənˌsɪl.iˈeɪ.ʃən/", "Noun", "পুনর্মিলন, আপস", "Reconciliation between nations.", "জাতিসমূহের মধ্যে পুনর্মিলন।", ["Harmony", "Settlement"], [], "Politics"),
    ("Rectitude", "/ˈrek.tɪ.tʃuːd/", "Noun", "সততা, সঠিক নৈতিকতা", "A man of moral rectitude.", "নৈতিক সততার এক পুরুষ।", ["Integrity", "Honesty"], [], "BCS"),
    ("Recuperation", "/rɪˌkjuː.pərˈeɪ.ʃən/", "Noun", "সুস্থ হয়ে ওঠা, আরোগ্য", "Fast recuperation after surgery.", "সার্জারির পর দ্রুত আরোগ্যলাভ।", ["Recovery", "Healing"], [], "Medical"),
    ("Refreshed", "/rɪˈfreʃt/", "Adj", "সতেজ, নতুন উদ্যমে চাঙ্গা", "Felt refreshed after sleep.", "ঘুমের পর সতেজ অনুভব করল।", ["Invigorated", "Restored"], [], "Spoken"),
    ("Regenerative", "/rɪˈdʒen.ər.ə.tɪv/", "Adj", "পুনরুজ্জীবনকারী, পুনর্গঠনমূলক", "Regenerative medicine advances.", "পুনরুজ্জীবনকারী চিকিৎসার অগ্রগতি।", ["Restorative"], [], "Science"),
    ("Remuneration", "/rɪˌmjuː.nərˈeɪ.ʃən/", "Noun", "পারিশ্রমিক, মেহনতানা", "Fair remuneration for work.", "কাজের ন্যায্য পারিশ্রমিক।", ["Payment", "Salary"], [], "Business"),
    ("Repudiation", "/rɪˌpjuː.diˈeɪ.ʃən/", "Noun", "অস্বীকার, প্রত্যাখ্যান", "Repudiation of false claim.", "ভুয়া দাবির প্রত্যাখ্যান।", ["Rejection", "Denial"], [], "Legal"),
    ("Resplendence", "/rɪˈsplen.dəns/", "Noun", "জাঁকজমক, মহোজ্জ্বল শোভা", "Resplendence of royal palace.", "রাজপ্রাসাদের মহোজ্জ্বল শোভা।", ["Brilliance", "Splendor"], [], "Literature"),
    ("Rhetorical", "/rɪˈtɒr.ɪ.kəl/", "Adj", "অলঙ্কারপূর্ণ, আড়ম্বরময় বাকছল", "A rhetorical question asked.", "এক অলঙ্কারপূর্ণ প্রশ্ন করা হলো।", ["Stylistic", "Oratorical"], [], "Literature"),
    ("Sanctification", "/ˌsæŋk.tɪ.fɪˈkeɪ.ʃən/", "Noun", "পবিত্রকরণ, পূতকরণ", "Sanctification of temple.", "মন্দিরের পবিত্রকরণ।", ["Blessing", "Consecration"], [], "Religion"),
    ("Scintillating", "/ˈsɪn.tɪ.leɪ.tɪŋ/", "Adj", "ঝলমলে, বুদ্ধিদীপ্ত ও আকর্ষণীয়", "A scintillating performance.", "এক বুদ্ধিদীপ্ত ঝলমলে পারফরম্যান্স।", ["Sparkling", "Brilliant"], [], "Spoken"),
    ("Subsume", "/səbˈsjuːm/", "Verb", "অন্তর্ভুক্ত করা, অন্তর্ভুক্ত হওয়া", "Category subsumes all cases.", "বিভাগটি সকল ঘটনা অন্তর্ভুক্ত করে।", ["Include", "Incorporate"], [], "Academic"),
    ("Surreptitious", "/ˌsʌr.əpˈtɪʃ.əs/", "Adj", "গুপ্ত, অলক্ষ্যে সংঘটিত", "A surreptitious glance at phone.", "ফোনে এক অলক্ষ্যে গোপন দৃষ্টিপাত।", ["Secret", "Covert"], [], "GRE"),
    ("Tantamount", "/ˈtæn.tə.maʊnt/", "Adj", "সমপরিমাণ, সমতুল্য", "Silence was tantamount to admission.", "নীরবতা স্বীকারের সমতুল্য ছিল।", ["Equivalent", "Equal"], [], "Academic"),
    ("Transcendence", "/trænˈsen.dəns/", "Noun", "সীমা অতিক্রম, মহীয়সী উৎকর্ষ", "Spiritual transcendence felt.", "আধ্যাত্মিক মহীয়সী উৎকর্ষ অনুভূত হলো।", ["Excellence", "Supremacy"], [], "Philosophy"),
    ("Ubiquity", "/juːˈbɪk.wə.ti/", "Noun", "সর্বব্যাপী উপস্থিতি", "Ubiquity of smartphones today.", "আজ স্মার্টফোনের সর্বব্যাপী উপস্থিতি।", ["Omnipresence"], [], "Technology"),
    ("Unassailable", "/ˌʌn.əˈseɪ.lə.bəl/", "Adj", "অখণ্ডনীয়, আক্রমণাতীত", "Unassailable evidence produced.", "অখণ্ডনীয় সাক্ষ্যপ্রমাণ পেশ করা হলো।", ["Invulnerable", "Irrefutable"], [], "Legal"),
    ("Veneration", "/ˌven.ərˈeɪ.ʃən/", "Noun", "গভীর শ্রদ্ধা, ভক্তি", "Held in high veneration.", "গভীর শ্রদ্ধায় রাখা হলো।", ["Reverence", "Respect"], [], "Culture"),
    ("Vindicate", "/ˈvɪn.dɪ.keɪt/", "Verb", "সত্যতা প্রমাণ করা, নির্দোষ প্রমাণ করা", "Court decision vindicated him.", "আদালতের রায় তাকে নির্দোষ প্রমাণ করল।", ["Exonerate", "Acquit"], [], "Legal"),
    ("Volitility", "/ˌvɒl.əˈtɪl.ə.ti/", "Noun", "অস্থিরতা, ক্ষণে ক্ষণে মেজাজ বদল", "Market volatility worried investors.", "বাজারের অস্থিরতা বিনোয়োগকারীদের চিন্তিত করল।", ["Instability", "Fickleness"], [], "Economics"),
    ("Vulnerability", "/ˌvʌl.nər.əˈbɪl.ə.ti/", "Noun", "সুরক্ষাহীনতা, স্পর্শকাতরতা", "Security vulnerability fixed.", "নিরাপত্তার সুরক্ষাহীনতা ঠিক করা হলো।", ["Weakness", "Exposedness"], [], "Technology"),
    ("Wholehearted", "/ˌhəʊlˈhɑː.tɪd/", "Adj", "একান্ত, সর্বান্তঃকরণে", "Gave wholehearted support.", "সর্বান্তঃকরণে সমর্থন দিল।", ["Sincere", "Enthusiastic"], [], "Spoken"),
    ("Witticism", "/ˈwɪt.ɪ.sɪ.zəm/", "Noun", "বুদ্ধিদীপ্ত রসিকতা, চতুর মন্তব্য", "Audience laughed at witticism.", "বুদ্ধিদীপ্ত রসিকতায় দর্শকরা হাসল।", ["Joke", "Quip"], [], "Literature"),
    ("Zenith", "/ˈzen.ɪθ/", "Noun", "শীর্ষবিন্দু, চরম উৎকর্ষ", "Reached zenith of glory.", "মহিমার শীর্ষবিন্দুতে পৌঁছালো।", ["Peak", "Pinnacle"], ["Nadir"], "BCS")
]

# Get count needed to reach 300 total for batch 2
dict_path = 'app/src/main/assets/dictionary_1000.json'
with open(dict_path, 'r', encoding='utf-8') as f:
    current_dict = json.load(f)

existing_batch2_count = sum(1 for item in current_dict if item.get('packId') == 'extra_300_batch2')
needed = 300 - existing_batch2_count
print(f"Current batch2 items count in json: {existing_batch2_count}. Needed to complete 300: {needed}")

# Collect filtered unique entries
selected_remaining = []
seen = set()

for item in pool_remaining:
    w = item[0].strip()
    k = w.lower()
    if k not in existing_words and k not in seen:
        seen.add(k)
        selected_remaining.append(item)
        if len(selected_remaining) == needed:
            break

print(f"Filtered {len(selected_remaining)} additional unique items.")

max_id = max([item.get('id', 0) for item in current_dict if isinstance(item.get('id'), int)] or [0])

new_entries = []
for idx, item in enumerate(selected_remaining, start=max_id+1):
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
    new_entries.append(entry)

current_dict.extend(new_entries)

with open(dict_path, 'w', encoding='utf-8') as f:
    json.dump(current_dict, f, ensure_ascii=False, indent=2)

print(f"Added {len(new_entries)} items to {dict_path}. Total dict count is now {len(current_dict)}.")


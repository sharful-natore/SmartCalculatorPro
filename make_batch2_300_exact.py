import json, re

dict_path = 'app/src/main/assets/dictionary_1000.json'

# Load existing json data
with open(dict_path, 'r', encoding='utf-8') as f:
    dict_data = json.load(f)

# Keep non-batch2 items intact
non_b2_items = [item for item in dict_data if item.get('packId') != 'extra_300_batch2']
b2_items = [item for item in dict_data if item.get('packId') == 'extra_300_batch2']

# Build existing words set from non-b2 items and Kotlin files
existing_words = set()
for item in non_b2_items:
    if 'word' in item and isinstance(item['word'], str):
        existing_words.add(item['word'].strip().lower())

with open('app/src/main/java/com/example/ui/screens/tools/VocabularyDataPacks.kt', 'r', encoding='utf-8') as f:
    content = f.read()
    for m in re.findall(r'VocabWord\s*\(\s*\"[^\"]+\"\s*,\s*\"([^\"]+)\"', content):
        existing_words.add(m.strip().lower())

with open('app/src/main/java/com/example/ui/screens/tools/VocabularyHighFrequencyDataset.kt', 'r', encoding='utf-8') as f:
    content = f.read()
    for t in re.findall(r'Triple\s*\(\s*\"([^\"]+)\"', content):
        existing_words.add(t.strip().lower())

print(f"Base existing unique words count: {len(existing_words)}")

# Keep existing valid b2 items that don't collide with existing_words
valid_b2 = []
b2_seen = set()
for item in b2_items:
    w = item.get('word', '').strip().lower()
    if w and w not in existing_words and w not in b2_seen:
        b2_seen.add(w)
        valid_b2.append(item)

print(f"Valid existing batch2 items: {len(valid_b2)}")

needed = 300 - len(valid_b2)
print(f"Still need {needed} unique words for batch 2.")

# High quality pool of English vocabulary with Bengali translations
vocabulary_pool = [
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
    ("Fabrication", "/ˌfæb.rɪˈkeɪ.ʃən/", "Noun", "বানানো গল্প, মিথ্যা রচনা", "Story was pure fabrication.", "গল্পটি ছিল খাঁটি বানানো মিথ্যা।", ["Lie", "Forgery"], [], "Spoken"),
    ("Facetious", "/fəˈsiː.ʃəs/", "Adj", "তামাশাপূর্ণ, ফাজিল কৌতুকভরা", "A facetious remark during speech.", "বক্তৃতার মাঝে এক তামাশাপূর্ণ মন্তব্য।", ["Flippant", "Joking"], ["Serious"], "GRE"),
    ("Facilitation", "/fəˌsɪl.ɪˈteɪ.ʃən/", "Noun", "সহজীকরণ, সহায়তা", "Facilitation of trade agreement.", "বাণিজ্য চুক্তির সহজীকরণ।", ["Assistance", "Easing"], [], "Business"),
    ("Factionalism", "/ˈfæk.ʃən.əl.ɪ.zəm/", "Noun", "দলবাজি, উপদলীয় কোন্দল", "Factionalism weakened party.", "দলাদলি দলটিকে দুর্বল করল।", ["In-fighting", "Division"], [], "Politics"),
    ("Fallaciousness", "/fəˈleɪ.ʃəs.nəs/", "Noun", "যুক্তিহীনতা, ভ্রান্তি", "Exposed fallaciousness of logic.", "যুক্তির ভ্রান্তি উন্মোচিত করল।", ["Falsehood", "Invalidity"], [], "Academic"),
    ("Fallibility", "/ˌfæl.əˈbɪl.ə.ti/", "Noun", "ভুল করার সম্ভাবনা", "Human fallibility acknowledged.", "মানুষের ভুল করার সম্ভাবনা স্বীকৃত।", ["Imperfection"], ["Infallibility"], "Psychology"),
    ("Falsification", "/ˌfɔːl.sɪ.fɪˈkeɪ.ʃən/", "Noun", "জালিয়াতি, বিকৃতকরণ", "Falsification of records.", "নথিপত্রের জালিয়াতি।", ["Forgery", "Distortion"], [], "Legal"),
    ("Fanaticism", "/fəˈnæt.ɪ.sɪ.zəm/", "Noun", "অন্ধ ধর্মান্ধতা, উগ্রতা", "Religious fanaticism caused conflict.", "ধর্মীয় উগ্রতা সংঘাত সৃষ্টি করল।", ["Extremism", "Bigotry"], [], "Sociology"),
    ("Galvanize", "/ˈɡæl.və.naɪz/", "Verb", "তৎপর করা, উদ্দীপিত করা", "Speech galvanized public.", "ভাষণ জনগণকে উদ্দীপিত করল।", ["Rouse", "Spur"], ["Dampen"], "IELTS"),
    ("Garrulity", "/ɡæˈruː.lə.ti/", "Noun", "বাচালতা, অহেতুক অতিরিক্ত কথা", "Annoyed by his garrulity.", "তার বাচালতায় বিরক্ত হলো।", ["Talkativeness", "Loquacity"], [], "GRE"),
    ("Gaudiness", "/ˈɡɔː.di.nəs/", "Noun", "চটকদার রঙচঙে ভাব", "Gaudiness of decorations.", "সজ্জার চটকদার রঙচঙে ভাব।", ["Flashiness", "Tastelessness"], [], "Fashion"),
    ("Genealogy", "/ˌdʒiː.niˈæl.ə.dʒi/", "Noun", "বংশতালিকা, কুলজী", "Researched family genealogy.", "পরিবারের বংশতালিকা গবেষণা করল।", ["Lineage", "Pedigree"], [], "History"),
    ("Generosity", "/ˌdʒen.əˈrɒs.ə.ti/", "Noun", "উদারতা, দানশীলতা", "Praised for generosity.", "উদারতার জন্য প্রশংসিত।", ["Magnanimity", "Bounty"], [], "Spoken"),
    ("Hackneyed", "/ˈhæk.niːd/", "Adj", "ঘিসাপিটা, গতানুগতিক", "Hackneyed slogans avoided.", "গতানুগতিক ঘিসাপিটা স্লোগান এড়ানো হলো।", ["Trite", "Cliché"], ["Original"], "GRE"),
    ("Haggle", "/ˈhæɡ.əl/", "Verb", "দরদাম করা", "Haggled over price of fish.", "মাছের দাম নিয়ে দরদাম করল।", ["Bargain"], [], "Spoken"),
    ("Hallowed", "/ˈhæl.əʊd/", "Adj", "পবিত্র, পূজনীয়", "Hallowed halls of university.", "বিশ্ববিদ্যালয়ের পবিত্র প্রাঙ্গণ।", ["Sacred", "Holy"], [], "Academic"),
    ("Haphazard", "/hæpˈhæz.əd/", "Adj", "আকাঙ্ক্ষাহীন, বিশৃঙ্খল ও এলোমেলো", "Haphazard arrangement of books.", "বইয়ের এলোমেলো বিশৃঙ্খল বিন্যাস।", ["Random", "Disorganized"], ["Orderly"], "General"),
    ("Iconoclastic", "/aɪˌkɒn.əˈklæs.tɪk/", "Adj", "প্রথাভাঙা, ঐতিহ্যবিরোধী", "An iconoclastic artist.", "এক প্রথাভাঙা শিল্পী।", ["Rebellious", "Subversive"], [], "Art"),
    ("Idiosyncratic", "/ˌɪd.i.ə.sɪŋˈkræt.ɪk/", "Adj", "ব্যক্তিগত বিশেষ স্বভাবমণ্ডিত", "An idiosyncratic writing style.", "এক ব্যক্তিগত বিশেষ স্বভাবমণ্ডিত লিখনশৈলী।", ["Peculiar", "Quirky"], [], "Literature"),
    ("Illuminating", "/ɪˈluː.mɪ.neɪ.tɪŋ/", "Adj", "আলোকপাতকারী, স্পষ্টকারী", "An illuminating insight.", "এক আলোকপাতকারী সূক্ষ্ম পর্যবেক্ষণ।", ["Enlightening", "Revealing"], [], "Academic"),
    ("Illusive", "/ɪˈluː.sɪv/", "Adj", "মায়াবী, বিভ্রান্তিকর", "Illusive promise of easy wealth.", "সহজ সম্পদের মায়াবী প্রতিশ্রুতি।", ["Deceptive", "Illusory"], [], "Literature"),
    ("Jovial", "/ˈdʒəʊ.vi.əl/", "Adj", "উল্লাসিত, হাসিখুশি ও প্রাণবন্ত", "A jovial host greeted guests.", "এক হাসিখুশি প্রফুল্ল মেজবান অতিথিদের বরণ করল।", ["Cheerful", "Merry"], ["Gloomy"], "Spoken"),
    ("Jubilant", "/ˈdʒuː.bɪ.lənt/", "Adj", "উচ্ছ্বসিত, বিজয়ানন্দিত", "Jubilant fans celebrated win.", "উচ্ছ্বসিত ভক্তরা জয় উদযাপন করল।", ["Ecstatic", "Triumphant"], [], "Sports"),
    ("Judicious", "/dʒuːˈdɪʃ.əs/", "Adj", "বিচক্ষণ, সুবিবেচক", "Judicious use of resources.", "সম্পদের সুবিবেচক ব্যবহার।", ["Prudent", "Wise"], ["Foolish"], "IELTS"),
    ("Labyrinthine", "/ˌlæb.əˈrɪn.θaɪn/", "Adj", "জটিল, ধাঁধাময় গলি বা পথ", "Labyrinthine passages of old castle.", "পুরনো দুর্গের জটিল ধাঁধাময় গলি।", ["Complex", "Intricate"], [], "Literature"),
    ("Lachrymose", "/ˈlæk.rɪ.məʊs/", "Adj", "অশ্রুসজল, কাঁদানে", "A lachrymose drama movie.", "এক অশ্রুসজল কাঁদানে ড্রামা সিনেমা।", ["Tearful", "Mournful"], [], "Literature"),
    ("Lamentable", "/ˈlæm.ən.tə.bəl/", "Adj", "শোচনীয়, দুঃখজনক", "Lamentable state of roads.", "রাস্তার শোচনীয় অবস্থা।", ["Regrettable", "Deplorable"], [], "General"),
    ("Magnanimity", "/ˌmæɡ.nəˈnɪm.ə.ti/", "Noun", "মহানুভবতা, উদারতা", "Showed magnanimity to enemy.", "শত্রুর প্রতি মহানুভবতা দেখালো।", ["Generosity", "Nobility"], [], "BCS"),
    ("Malevolence", "/məˈlev.əl.əns/", "Noun", "বিদ্বেষ, হিংসা", "Gared with malevolence.", "হিংসার সাথে তাকালো।", ["Malice", "Hostility"], [], "Literature"),
    ("Malleability", "/ˌmæl.i.əˈbɪl.ə.ti/", "Noun", "নমনীয়তা, রূপান্তরযোগ্যতা", "Malleability of silver metal.", "রূপা ধাতুর নমনীয়তা।", ["Flexibility", "Pliant"], [], "Science"),
    ("Noxiousness", "/ˈnɒk.ʃəs.nəs/", "Noun", "বিষাক্ততা, অনিষ্টকারিতা", "Noxiousness of industrial fumes.", "শিল্পকারখানার ধোঁয়ার বিষাক্ততা।", ["Toxicity", "Harmfulness"], [], "Science"),
    ("Nullification", "/ˌnʌl.ɪ.fɪˈkeɪ.ʃən/", "Noun", "বাতিলকরণ, রদ", "Nullification of contract.", "চুক্তির বাতিলকরণ।", ["Cancellation", "Invalidation"], [], "Legal"),
    ("Obfuscation", "/ˌɒb.fʌsˈkeɪ.ʃən/", "Noun", "অস্পষ্টকরণ, বিভ্রান্তি তৈরি", "Deliberate obfuscation of truth.", "সত্যের ইচ্ছাকৃত অস্পষ্টকরণ।", ["Confusion", "Obscurity"], [], "Academic"),
    ("Obliteration", "/əˌblɪt.əˈreɪ.ʃən/", "Noun", "সম্পূর্ণ চিহ্ন মুছে ফেলা", "Obliteration of evidence.", "প্রমাণের চিহ্ন সম্পূর্ণ মুছে ফেলা।", ["Destruction", "Erasing"], [], "Legal"),
    ("Pacific", "/pəˈsɪf.ɪk/", "Adj", "শান্তিকামী, শান্ত বা অমায়িক", "Pacific nature of negotiation.", "আলোচনার শান্তিকামী ধরণ।", ["Peaceful", "Calm"], [], "Politics"),
    ("Panegyric", "/ˌpæn.əˈdʒɪr.ɪk/", "Noun", "প্রশংসাগীতি, অতিস্তুতিভরা ভাষণ", "Delivered a panegyric for hero.", "বীরদের জন্য প্রশংসাগীতি পেশ করল।", ["Eulogy", "Tribute"], [], "Literature"),
    ("Paramount", "/ˈpær.ə.maʊnt/", "Adj", "সর্বোচ্চ, সর্বাধিক গুরুত্বপূর্ণ", "Safety is of paramount importance.", "নিরাপত্তা সর্বাধিক গুরুত্বপূর্ণ।", ["Supreme", "Chief"], [], "Academic"),
    ("Perseverance", "/ˌpɜː.sɪˈvɪə.rəns/", "Noun", "অধ্যবসায়, একনিষ্ঠ চেষ্টা", "Perseverance leads to success.", "অধ্যবসায় সাফল্য এনে দেয়।", ["Persistence", "Diligence"], [], "Spoken"),
    ("Placating", "/pləˈkeɪ.tɪŋ/", "Adj", "শান্ত করার প্রয়াসী", "A placating gesture to crowd.", "জনতাকে শান্ত করার এক প্রয়াস।", ["Conciliatory", "Soothing"], [], "Politics"),
    ("Polysyllabic", "/ˌpɒl.i.sɪˈlæb.ɪk/", "Adj", "বহু-অক্ষর বিশিষ্ট, জটিল শব্দভরা", "Polysyllabic words in text.", "পাঠ্যে বহু-অক্ষর বিশিষ্ট জটিল শব্দসমূহ।", ["Complex"], [], "Linguistics"),
    ("Ponderous", "/ˈpɒn.dər.əs/", "Adj", "ভারী, ধীরগতিসম্পন্ন ও একঘেয়ে", "A ponderous style of speech.", "ভাষণের এক ভারী একঘেয়ে শৈলী।", ["Heavy", "Lumbering"], [], "Literature"),
    ("Preeminence", "/priːˈem.ɪ.nəns/", "Noun", "শ্রেষ্ঠত্ব, প্রাধান্য", "Achieved preeminence in field.", "ক্ষেত্রের শ্রেষ্ঠত্ব অর্জন করল।", ["Superiority", "Dominance"], [], "Academic"),
    ("Premeditation", "/ˌpriː.med.ɪˈteɪ.ʃən/", "Noun", "পূর্বপরিকল্পনা", "Crime committed with premeditation.", "পূর্বপরিকল্পনার মাধ্যমে অপরাধ করা হয়েছিল।", ["Planning", "Forethought"], [], "Legal"),
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
    ("Zenith", "/ˈzen.ɪθ/", "Noun", "শীর্ষবিন্দু, চরম উৎকর্ষ", "Reached zenith of glory.", "মহিমার শীর্ষবিন্দুতে পৌঁছালো।", ["Peak", "Pinnacle"], ["Nadir"], "BCS"),
    ("Abnegation", "/ˌæb.nɪˈɡeɪ.ʃən/", "Noun", "আত্মত্যাগ, বর্জন", "Abnegation of personal desires.", "ব্যক্তিগত ইচ্ছার আত্মত্যাগ।", ["Self-denial"], [], "GRE"),
    ("Abridgement", "/əˈbrɪdʒ.mənt/", "Noun", "সংক্ষেপণ, পরিমার্জন", "Abridgement of long text.", "দীর্ঘ পাঠ্যের সংক্ষেপণ।", ["Shortening"], [], "Academic"),
    ("Absolutism", "/ˈæb.sə.luː.tɪ.zəm/", "Noun", "একচ্ছত্রবাদ, নিরঙ্কুশ শাসন", "Absolutism in medieval times.", "মধ্যযুগে নিরঙ্কুশ শাসন।", ["Autocracy"], [], "History"),
    ("Abstemiousness", "/æbˈstiː.mi.əs.nəs/", "Noun", "পরিমিতাহার, সংযম", "Praised for abstemiousness.", "সংযমের জন্য প্রশংসিত।", ["Temperance"], [], "BCS"),
    ("Acquiescent", "/ˌæk.wiˈes.ənt/", "Adj", "অনুমোদনকারী, নিশব্দে সম্মত", "An acquiescent agreement.", "এক নিশব্দ অনুমোদনকারী সম্মতি।", ["Compliant"], [], "Spoken"),
    ("Acrophobic", "/ˌæk.rəˈfəʊ.bɪk/", "Adj", "উচ্চতাভীতিসম্পন্ন", "Acrophobic people avoid balconies.", "উচ্চতাভীতিসম্পন্ন মানুষ বারান্দা এড়ায়।", ["Fearful of heights"], [], "Psychology"),
    ("Actuality", "/ˌæk.tʃuˈæl.ə.ti/", "Noun", "বাস্তবতা, পরম সত্য", "In actuality, it was different.", "বাস্তবে এটি ভিন্ন ছিল।", ["Reality"], [], "Spoken"),
    ("Acquisitiveness", "/əˈkwɪz.ɪ.tɪv.nəs/", "Noun", "আহরণেচ্ছা, পাওয়ার আগ্রহ", "Acquisitiveness of wealth.", "সম্পদ আহরণের প্রবল আগ্রহ।", ["Greed"], [], "Psychology"),
    ("Adumbrate", "/ˈæd.əm.breɪt/", "Verb", "আভাস দেওয়া, পূর্বসূচনা দেওয়া", "Adumbrated future policies.", "ভবিষ্যৎ নীতির আভাস দিল।", ["Foreshadow"], [], "GRE"),
    ("Adventitious", "/ˌæd.venˈtɪʃ.əs/", "Adj", "আকস্মিক, অনাকাঙ্ক্ষিতভাবে অর্জিত", "Adventitious gains in trade.", "বাণিজ্যে আকস্মিক অর্জন।", ["Accidental"], [], "Business"),
    ("Aforestation", "/æf.ɒr.ɪˈsteɪ.ʃən/", "Noun", "বনসৃজন, নতুন বনায়ন", "Aforestation drive in coastal area.", "উপকূলীয় এলাকায় বনসৃজন অভিযান।", ["Afforestation"], [], "Environment"),
    ("Aggrandizement", "/əˈɡræn.dɪz.mənt/", "Noun", "ক্ষমতা বৃদ্ধি, আত্মউন্নতি", "Self aggrandizement through fame.", "খ্যাতির মাধ্যমে ক্ষমতা বৃদ্ধি।", ["Exaltation"], [], "Politics"),
    ("Agglomeration", "/əˌɡlɒm.əˈreɪ.ʃən/", "Noun", "স্তূপীকরণ, একত্রীকরণ", "Agglomeration of small towns.", "ছোট শহরের স্তূপীকরণ।", ["Cluster"], [], "Geography"),
    ("Bacteriology", "/bækˌtɪə.riˈɒl.ə.dʒi/", "Noun", "জীবাণুবিদ্যা, ব্যাকটেরিয়াতত্ত্ব", "Studied bacteriology at medical college.", "মেডিকেল কলেজে জীবাণুবিদ্যা অধ্যয়ন করেছে।", ["Microbiology"], [], "Science"),
    ("Beguilement", "/bɪˈɡaɪl.mənt/", "Noun", "প্রতারণা, মোহমুগ্ধকরণ", "Beguilement by false promises.", "মিথ্যা প্রতিশ্রুতির মোহমুগ্ধকরণ।", ["Charm"], [], "Literature"),
    ("Belligerency", "/bəˈlɪdʒ.ər.ən.si/", "Noun", "যুদ্ধংদেহি ভাব, আগ্রাসন", "Showed belligerency in speech.", "বক্তৃতায় যুদ্ধংদেহি ভাব দেখালো।", ["Hostility"], [], "Politics"),
    ("Beneficence", "/bəˈnef.ɪ.səns/", "Noun", "দানশীলতা, কল্যাণকামিতা", "Known for social beneficence.", "সামাজিক কল্যাণকামিতার জন্য পরিচিত।", ["Charity"], [], "BCS"),
    ("Benevolence", "/bəˈnev.əl.əns/", "Noun", "উদারতা, সদয়ভাব", "Act of benevolence to poor.", "দরিদ্রদের প্রতি উদারতার কাজ।", ["Altruism"], [], "Spoken"),
    ("Bifurcation", "/ˌbaɪ.fəˈkeɪ.ʃən/", "Noun", "দ্বিখণ্ডন, দুই শাখায় বিভাজন", "Bifurcation of road.", "রাস্তার দ্বিখণ্ডন।", ["Division"], [], "Geography"),
    ("Biodiversity", "/ˌbaɪ.əʊ.daɪˈvɜː.sə.ti/", "Noun", "জীববৈচিত্র্য", "Protect biodiversity of rainforest.", "বৃষ্টিঅঞ্চলের জীববৈচিত্র্য রক্ষা করুন।", ["Ecological variety"], [], "Environment"),
    ("Bountifulness", "/ˈbaʊn.tɪ.fəl.nəs/", "Noun", "প্রচুরতা, প্রাচুর্য", "Bountifulness of harvest.", "ফসলের প্রাচুর্য।", ["Abundance"], [], "General"),
    ("Callousness", "/ˈkæl.əs.nəs/", "Noun", "নিষ্ঠুরতা, অনুভূতিহীনতা", "Shocked by callousness of crowd.", "জনতার অনুভূতিহীনতায় হতবাক।", ["Heartlessness"], [], "Psychology"),
    ("Camaraderie", "/ˌkæm.əˈrɑː.də.ri/", "Noun", "সৌহার্দ্য, সখ্যতা", "Camaraderie among teammates.", "সহকর্মীদের মধ্যে সৌহার্দ্য।", ["Comradeship"], [], "Spoken"),
    ("Canonization", "/ˌkæn.ən.aɪˈzeɪ.ʃən/", "Noun", "সাধু ঘোষণা, সর্বোচ্চ মর্যাদা দান", "Canonization of Saint.", "সাধুর মর্যাদা দান।", ["Glorification"], [], "History"),
    ("Capriciousness", "/kəˈprɪʃ.əs.nəs/", "Noun", "খামখেয়ালীপনা, অস্থির মনোভাব", "Capriciousness of weather.", "আবহাওয়া খামখেয়ালীপনা।", ["Whimsicality"], [], "Literature"),
    ("Categorization", "/ˌkæt.ə.ɡər.aɪˈzeɪ.ʃən/", "Noun", "শ্রেণীবদ্ধকরণ", "Categorization of data.", "তথ্যের শ্রেণীবদ্ধকরণ।", ["Classification"], [], "Academic"),
    ("Circumlocution", "/ˌsɜː.kəm.ləˈkjuː.ʃən/", "Noun", "ঘুরিয়ে কথা বলা, পরোক্ষ উক্তি", "Avoid circumlocution in essay.", "প্রবন্ধে ঘুরে পেঁচিয়ে কথা এড়িয়ে চলুন।", ["Verbosity"], [], "GRE"),
    ("Circumnavigation", "/ˌsɜː.kəmˌnæv.ɪˈɡeɪ.ʃən/", "Noun", "জলপথে পৃথিবী প্রদক্ষিণ", "Circumnavigation of globe.", "জলপথে পৃথিবী প্রদক্ষিণ।", ["Global sailing"], [], "History"),
    ("Coagulation", "/kəʊˌæɡ.jəˈleɪ.ʃən/", "Noun", "জমাট বাঁধা, রক্ত তঞ্চন", "Coagulation of blood sample.", "রক্তের নমুনার জমাট বাঁধা।", ["Clotting"], [], "Medical"),
    ("Coalescence", "/ˌkəʊ.əˈles.əns/", "Noun", "একত্রীকরণ, সংমিশ্রণ", "Coalescence of two groups.", "দুই দলের একত্রীকরণ।", ["Union"], [], "Academic"),
    ("Codification", "/ˌkɒd.ɪ.fɪˈkeɪ.ʃən/", "Noun", "আইন বা নীতির সংহিতা তৈরি", "Codification of labor laws.", "শ্রম আইনের সংহিতা তৈরি।", ["Systematization"], [], "Legal"),
    ("Decrepitude", "/dɪˈkrep.ɪ.tʃuːd/", "Noun", "জরাজীর্ণতা, জীর্ণ দশা", "Building fell into decrepitude.", "ভবনটি জরাজীর্ণতায় পরিণত হলো।", ["Dilapidation"], [], "Literature"),
    ("Defalcation", "/ˌdiː.fælˈkeɪ.ʃən/", "Noun", "তসরুফ, গচ্ছিত টাকা আত্মসাৎ", "Investigation into defalcation.", "টাকা আত্মসাতের তদন্ত।", ["Embezzlement"], [], "Legal"),
    ("Deferential", "/ˌdef.ərˈen.ʃəl/", "Adj", "শ্রদ্ধাশীল, অনুগত", "A deferential tone to elders.", "গুরুজনদের প্রতি শ্রদ্ধাশীল সুর।", ["Respectful"], [], "Spoken"),
    ("Deliberateness", "/dɪˈlɪb.ər.ət.nəs/", "Noun", "সচেতন চিন্তাভাবনা, উদ্দেশ্যমূলকতা", "Acted with calm deliberateness.", "শান্ত উদ্দেশ্যমূলকতার সাথে কাজ করল।", ["Intentionality"], [], "Spoken"),
    ("Demonstrable", "/dɪˈmɒn.strə.bəl/", "Adj", "প্রমাণযোগ্য, সুষ্পষ্ট", "Demonstrable improvement in test.", "পরীক্ষায় প্রমাণযোগ্য উন্নতি।", ["Provable"], [], "Academic"),
    ("Educational", "/ˌedʒ.ʊˈkeɪ.ʃən.əl/", "Adj", "শিক্ষণীয়, শিক্ষামূলক", "An educational tour to museum.", "জাদুঘরে শিক্ষামূলক সফর।", ["Instructive"], [], "Spoken"),
    ("Emotionality", "/ɪˌməʊ.ʃənˈæl.ə.ti/", "Noun", "আবেগপ্রবণতা", "High emotionality in drama.", "নাটকে উচ্চ আবেগপ্রবণতা।", ["Sensibility"], [], "Psychology"),
    ("Empiricalness", "/ɪmˈpɪr.ɪ.kəl.nəs/", "Noun", "অভিজ্ঞতালব্ধতা, বস্তুনিষ্ঠতা", "Empiricalness of scientific data.", "বিজ্ঞানভিত্তিক তথ্যের বস্তুনিষ্ঠতা।", ["Objectivity"], [], "Science"),
    ("Fictionalization", "/ˌfɪk.ʃən.əl.aɪˈzeɪ.ʃən/", "Noun", "কাল্পনিক রূপদান, গল্পে রূপান্তর", "Fictionalization of history.", "ইতিহাসের কাল্পনিক রূপদান।", ["Dramatization"], [], "Literature"),
    ("Fluorescence", "/flɔːˈres.əns/", "Noun", "প্রতিপ্রভা, আলোক বিচ্ছুরণ", "Fluorescence under UV light.", "অতিবেগুনী আলোতে প্রতিপ্রভা।", ["Luminescence"], [], "Science"),
    ("Generalization", "/ˌdʒen.ər.əl.aɪˈzeɪ.ʃən/", "Noun", "সাধারণীকরণ, পাইকারি সিদ্ধান্ত", "Avoid broad generalization.", "অহেতুক সাধারণীকরণ এড়িয়ে চলুন।", ["Broad statement"], [], "Academic"),
    ("Gravitation", "/ˌɡræv.ɪˈteɪ.ʃən/", "Noun", "মহাকর্ষ, আকর্ষণ বল", "Force of gravitation.", "মহাকর্ষ বল।", ["Attraction"], [], "Science"),
    ("Harmonization", "/ˌhɑː.mə.naɪˈzeɪ.ʃən/", "Noun", "সামঞ্জস্যবিধান, মেলবন্ধন", "Harmonization of standards.", "মানদণ্ডের সামঞ্জস্যবিধান।", ["Alignment"], [], "Business"),
    ("Histrionics", "/ˌhɪs.triˈɒn.ɪks/", "Noun", "নাটকীয়তা, অতিরঞ্জিত আচরণ", "Tired of her histrionics.", "তার নাটকীয়তায় ক্লান্ত।", ["Theatrics"], [], "Literature"),
    ("Idealization", "/aɪˌdɪə.laɪˈzeɪ.ʃən/", "Noun", "আদর্শ রূপদান, অতি-উচ্চ ধারণা", "Idealization of romance.", "রোমান্সের অতি-উচ্চ ধারণা।", ["Romanticization"], [], "Psychology"),
    ("Ignominious", "/ˌɪɡ.nəˈmɪn.i.əs/", "Adj", "অপমানজনক, লজ্জাকর", "An ignominious defeat.", "এক অপমানজনক পরাজয়।", ["Humiliating"], [], "GRE"),
    ("Illumination", "/ɪˌluː.mɪˈneɪ.ʃən/", "Noun", "আলোকসজ্জা, আলোকিতকরণ", "Festive illumination in streets.", "রাস্তায় উৎসবের আলোকসজ্জা।", ["Lighting"], [], "General"),
    ("Juxtaposition", "/ˌdʒʌk.stə.pəˈzɪʃ.ən/", "Noun", "পাশাপাশি স্থাপন, বৈসাদৃশ্যপ্রদর্শন", "Juxtaposition of light and dark.", "আলো ও অন্ধকারের পাশাপাশি স্থাপন।", ["Comparison"], [], "Art"),
    ("Lamentation", "/ˌlæm.ənˈteɪ.ʃən/", "Noun", "বিলাপ, শោក প্রকাশ", "Lamentation for lost hero.", "হারানো বীরের জন্য বিলাপ।", ["Mourning"], [], "Literature"),
    ("Liberalization", "/ˌlɪb.ər.əl.aɪˈzeɪ.ʃən/", "Noun", "উদারীকরণ, শিথিলকরণ", "Economic liberalization in 1990s.", "নব্বইয়ের দশকে অর্থনৈতিক উদারীকরণ।", ["Relaxation"], [], "Economics"),
    ("Magnification", "/ˌmæɡ.nɪ.fɪˈkeɪ.ʃən/", "Noun", "বিবর্ধন, বড় করে দেখা", "Microscope magnification power.", "অণুবীক্ষণ যন্ত্রের বিবর্ধন ক্ষমতা।", ["Enlargement"], [], "Science"),
    ("Materialization", "/məˌtɪə.ri.əl.aɪˈzeɪ.ʃən/", "Noun", "বাস্তবায়ন, আত্মপ্রকাশ", "Materialization of plans.", "পরিকল্পনার বাস্তবায়ন।", ["Fulfillment"], [], "Business"),
    ("Nationalization", "/ˌnæʃ.ən.əl.aɪˈzeɪ.ʃən/", "Noun", "জাতীয়করণ", "Nationalization of banks.", "ব্যাংকসমূহের জাতীয়করণ।", ["State ownership"], [], "Economics"),
    ("Operationalization", "/ˌɒp.ər.eɪ.ʃən.əl.aɪˈzeɪ.ʃən/", "Noun", "কার্যকরীকরণ, সংজ্ঞায়িতকরণ", "Operationalization of research variables.", "গবেষণার পরিবর্তনশীল চলকের কার্যকরীকরণ।", ["Implementation"], [], "Research"),
    ("Personalization", "/ˌpɜː.sən.əl.aɪˈzeɪ.ʃən/", "Noun", "ব্যক্তিনিলয়তা, নিজস্বকরণ", "Personalization of settings.", "সেটিংসের নিজস্বকরণ।", ["Customization"], [], "Technology"),
    ("Philosophical", "/ˌfɪl.əˈsɒf.ɪ.kəl/", "Adj", "দার্শনিক, জ্ঞানভিত্তিক", "A philosophical discussion on life.", "জীবনের ওপর এক দার্শনিক আলোচনা।", ["Thoughtful"], [], "Academic"),
    ("Politicization", "/pəˌlɪt.ɪ.saɪˈzeɪ.ʃən/", "Noun", "রাজনীতিকীকরণ", "Politicization of university administration.", "বিশ্ববিদ্যালয় প্রশাসনের রাজনীতিকীকরণ।", ["Political involvement"], [], "Politics"),
    ("Quantification", "/ˌkwɒn.tɪ.fɪˈkeɪ.ʃən/", "Noun", "পরিমাণ নির্ধারণ", "Quantification of risks.", "ঝুঁকির পরিমাণ নির্ধারণ।", ["Measurement"], [], "Business"),
    ("Rationalization", "/ˌræʃ.ən.əl.aɪˈzeɪ.ʃən/", "Noun", "যৌক্তিকীকরণ, অজুহাত গঠন", "Rationalization of bad behavior.", "খারাপ আচরণের যৌক্তিকীকরণ।", ["Justification"], [], "Psychology"),
    ("Reconceptualization", "/ˌriː.kən.sep.tʃu.əl.aɪˈzeɪ.ʃən/", "Noun", "পুনঃধারণা গঠন", "Reconceptualization of strategy.", "কৌশলের পুনঃধারণা গঠন।", ["Reimagining"], [], "Academic"),
    ("Rehabilitation", "/ˌriː.həˌbɪl.ɪˈteɪ.ʃən/", "Noun", "পুনর্বাসন, আরোগ্যলাভ", "Rehabilitation center for patients.", "রোগীদের জন্য পুনর্বাসন কেন্দ্র।", ["Recovery"], [], "Medical"),
    ("Standardization", "/ˌstæn.də.daɪˈzeɪ.ʃən/", "Noun", "মানকীকরণ, সমতাবিধান", "Standardization of products.", "পণ্যসমূহের মানকীকরণ।", ["Normalization"], [], "Business"),
    ("Transformation", "/ˌtræns.fəˈmeɪ.ʃən/", "Noun", "রূপান্তর, আমূল পরিবর্তন", "Transformation of digital economy.", "ডিজিটাল অর্থনীতির রূপান্তর।", ["Change"], [], "Spoken"),
    ("Absolution", "/ˌæb.səˈluː.ʃən/", "Noun", "পাপমোচন, মুক্তি", "Sought absolution at church.", "গির্জায় পাপমোচন চাইল।", ["Forgiveness"], [], "Religion"),
    ("Accretion", "/əˈkriː.ʃən/", "Noun", "সংযোজন, বৃদ্ধি", "Accretion of sediment.", "পলিমাটির বৃদ্ধি।", ["Accumulation"], [], "Science"),
    ("Acquiesce", "/ˌæk.wiˈes/", "Verb", "মেনে নেওয়া, সম্মতি দেওয়া", "Acquiesced to father demands.", "বাবার দাবিতে সম্মতি দিল।", ["Consent"], [], "BCS"),
    ("Admonition", "/ˌæd.məˈnɪʃ.ən/", "Noun", "সতর্কবার্তা, মৃদু তিরস্কার", "Listened to teacher admonition.", "শিক্ষকের সতর্কবার্তা শুনল।", ["Warning"], [], "Academic"),
    ("Aestheticism", "/esˈθet.ɪ.sɪ.zəm/", "Noun", "সৌন্দর্যবাদ, কলারসিকতা", "Devoted to aestheticism.", "সৌন্দর্যবাদে নিবেদিত।", ["Love of beauty"], [], "Art"),
    ("Affiliation", "/əˌfɪl.iˈeɪ.ʃən/", "Noun", "সংযুক্তি, অন্তর্ভুক্তি", "Political affiliation revealed.", "রাজনৈতিক সংযুক্তি প্রকাশিত হলো।", ["Association"], [], "Politics"),
    ("Aggregate", "/ˈæɡ.rɪ.ɡət/", "Noun", "সমষ্টি, মোট পরিমাণ", "Aggregate score in exam.", "পরীক্ষায় মোট অর্জিত নম্বর।", ["Total"], [], "Academic"),
    ("Alienation", "/ˌeɪ.li.əˈneɪ.ʃən/", "Noun", "বিচ্ছিন্নতা, পরকীয়তা", "Sense of social alienation.", "সামাজিক বিচ্ছিন্নতার অনুভূতি।", ["Estrangement"], [], "Sociology"),
    ("Allegation", "/ˌæl.ɪˈɡeɪ.ʃən/", "Noun", "অভিযোগ, দাবি", "Denied corruption allegation.", "দুর্নীতির অভিযোগ অস্বীকার করল।", ["Accusation"], [], "Legal"),
    ("Alliteration", "/əˌlɪt.əˈreɪ.ʃən/", "Noun", "অনুপ্রাস, একই ধ্বনির পুনরাবৃত্তি", "Poet used alliteration in verse.", "কবি কবিতায় অনুপ্রাস ব্যবহার করেছেন।", ["Repetition of sound"], [], "Literature"),
    ("Altruistic", "/ˌæl.truˈɪs.tɪk/", "Adj", "পরোপকারী, জনহিতৈষী", "Altruistic deed cheered village.", "পরোপকারী কাজ গ্রামবাসীকে আনন্দিত করল।", ["Charitable"], [], "Spoken"),
    ("Ambiguity", "/ˌæm.bɪˈɡjuː.ə.ti/", "Noun", "দ্ব্যর্থকতা, অস্পষ্টতা", "Resolved ambiguity in law.", "আইনের অস্পষ্টতা দূর করল।", ["Uncertainty"], [], "Academic"),
    ("Amelioration", "/əˌmiː.li.əˈreɪ.ʃən/", "Noun", "উন্নতিসাধন, সংস্কার", "Amelioration of living condition.", "জীবনযাত্রার মানের উন্নতিসাধন।", ["Improvement"], [], "Economics"),
    ("Anachronism", "/əˈnæk.rə.nɪ.zəm/", "Noun", "কালবৈষম্য, ভুল সময়ের উপাদান", "Sword in modern film was anachronism.", "আধুনিক চলচ্চিত্রে তরবারি ছিল কালবৈষম্য।", ["Misplacement in time"], [], "History"),
    ("Analogy", "/əˈnæl.ə.dʒi/", "Noun", "সাদৃশ্য, রূপক তুলনা", "Explained logic using analogy.", "সাদৃশ্য ব্যবহার করে যুক্তি বুঝিয়ে দিল।", ["Comparison"], [], "Academic"),
    ("Animosity", "/ˌæn.ɪˈmɒs.ə.ti/", "Noun", "শত্রুতা, বিদ্বেষ", "No personal animosity.", "কোনো ব্যক্তিগত শত্রুতা নেই।", ["Hostility"], [], "Spoken"),
    ("Anomalous", "/əˈnɒm.ə.ləs/", "Adj", "ব্যতিক্রমী, নিয়মবহির্ভূত", "Anomalous result in lab.", "পরীক্ষাগারে ব্যতিক্রমী ফলাফল।", ["Abnormal"], [], "Science"),
    ("Antagonism", "/ænˈtæɡ.ən.ɪ.zəm/", "Noun", "বিরোধিতা, বৈরিতা", "Felt antagonism from rivals.", "প্রতিদ্বন্দ্বীদের থেকে বৈরিতা অনুভব করল।", ["Opposition"], [], "Politics"),
    ("Antecedent", "/ˌæn.tɪˈsiː.dənt/", "Noun", "পূর্ববর্তী ঘটনা, পূর্বসূরি", "Historic antecedent of war.", "যুদ্ধের ঐতিহাসিক পূর্ববর্তী ঘটনা।", ["Predecessor"], [], "History"),
    ("Anticipation", "/ænˌtɪs.ɪˈpeɪ.ʃən/", "Noun", "প্রত্যাশা, আগাম আনন্দ", "Waited in eager anticipation.", "অধীর প্রত্যাশায় অপেক্ষা করল।", ["Expectation"], [], "Spoken"),
    ("Antithesis", "/ænˈtɪθ.ə.sɪs/", "Noun", "পরম বিপরীত, প্রতিপক্ষ", "Love is antithesis of hate.", "ভালোবাসা হলো ঘৃণার পরম বিপরীত।", ["Opposite"], [], "GRE"),
    ("Apex", "/ˈeɪ.peks/", "Noun", "শীর্ষ, সর্বোচ্চ স্থান", "At apex of career.", "ক্যারিয়ারের শীর্ষে।", ["Peak"], [], "Spoken"),
    ("Aphorism", "/ˈæf.ər.ɪ.zəm/", "Noun", "প্রবাদ, প্রবচন", "Famous aphorism on honesty.", "সততার ওপর বিখ্যাত প্রবাদ।", ["Maxi"], [], "Literature"),
    ("Apostasy", "/əˈpɒs.tə.si/", "Noun", "স্বধর্মত্যাগ, আদর্শচ্যুতি", "Charged with apostasy.", "স্বধর্মত্যাগের দায়ে অভিযুক্ত।", ["Renunciation"], [], "Religion"),
    ("Apparition", "/ˌæp.əˈrɪʃ.ən/", "Noun", "ভূতুড়ে দৃশ্য, অলৌকিক অবয়ব", "Scared by sudden apparition.", "আকস্মিক ভূতুড়ে দৃশ্যে ভীত হলো।", ["Ghost"], [], "Literature"),
    ("Appease", "/əˈpiːz/", "Verb", "শান্ত করা, তোষণ করা", "Appeased angry customers.", "ক্রুদ্ধ গ্রাহকদের শান্ত করল।", ["Pacify"], [], "Spoken"),
    ("Approbation", "/ˌæp.rəˈbeɪ.ʃən/", "Noun", "অনুমোদন, প্রশংসা", "Gained boss approbation.", "বসের অনুমোদন পেল।", ["Approval"], [], "BCS"),
    ("Archaic", "/ɑːˈkeɪ.ɪk/", "Adj", "প্রাচীন, সেকেলে", "Archaic language in manuscript.", "পাণ্ডুলিপিতে সেকেলে ভাষা।", ["Obsolete"], [], "Academic"),
    ("Architectural", "/ˌɑː.kɪˈtek.tʃər.əl/", "Adj", "স্থাপত্যবিষয়ক", "Architectural marvel of building.", "ভবনটির স্থাপত্যবিষয়ক বিস্ময়।", ["Structural"], [], "Art"),
    ("Articulate", "/ɑːˈtɪk.jə.lət/", "Adj", "স্পষ্টভাষী, বাকপটু", "An articulate speaker.", "এক বাকপটু বক্তা।", ["Eloquent"], [], "Spoken"),
    ("Asceticism", "/əˈset.ɪ.sɪ.zəm/", "Noun", "কঠোর কৃচ্ছ্রসাধন, তপস্যা", "Practiced lifelong asceticism.", "আজীবন কঠোর কৃচ্ছ্রসাধন করল।", ["Self-denial"], [], "Philosophy"),
    ("Asperity", "/æˈsper.ə.ti/", "Noun", "কর্কশতা, রূঢ় আচরণ", "Spoke with unexpected asperity.", "প্রত্যাশিত কর্কশতার সাথে কথা বলল।", ["Harshness"], [], "Literature"),
    ("Aspiration", "/ˌæs.pɪˈreɪ.ʃən/", "Noun", "উচ্চাকাঙ্ক্ষা, বড় স্বপ্ন", "High career aspiration.", "উচ্চ সামাজিক ও কর্মজীবনের আকাঙ্ক্ষা।", ["Ambition"], [], "Spoken"),
    ("Assertion", "/əˈsɜː.ʃən/", "Noun", "দৃঢ় দাবি, ঘোষণা", "Strong assertion of truth.", "সত্যের দৃঢ় ঘোষণা।", ["Declaration"], [], "Academic"),
    ("Assiduity", "/ˌæs.ɪˈdʒuː.ə.ti/", "Noun", "অধ্যবসায়, নিরলস চেষ্টা", "Work done with assiduity.", "নিরলস চেষ্টার সাথে সম্পন্ন কাজ।", ["Diligence"], [], "BCS"),
    ("Assimilation", "/əˌsɪm.ɪˈleɪ.ʃən/", "Noun", "আত্মীকরণ, হজম", "Assimilation of new ideas.", "নতুন চিন্তাধারার আত্মীকরণ।", ["Absorption"], [], "Psychology"),
    ("Assuage", "/əˈsweɪdʒ/", "Verb", "প্রশমিত করা, উপশম করা", "Assuaged her guilt.", "তার অপরাধবোধ প্রশমিত করল।", ["Relieve"], [], "GRE"),
    ("Astral", "/ˈæs.trəl/", "Adj", "নক্ষত্রসংক্রান্ত, মহাজাগতিক", "Astral body concept.", "নক্ষত্রসংক্রান্ত ধারণার অবয়ব।", ["Stellar"], [], "Science"),
    ("Astringent", "/əˈstrɪn.dʒənt/", "Adj", "কষাটে, কঠোর ও কড়া", "Astringent review of book.", "বইয়ের কঠোর ও কড়া সমালোচনা।", ["Harsh"], [], "Literature"),
    ("Asymmetrical", "/ˌeɪ.sɪˈmet.rɪ.kəl/", "Adj", "অসমমিতিক, অসমান আকৃতির", "Asymmetrical design of vase.", "ফুলদানির অসমান অসমমিতিক নকশা।", ["Unequal"], [], "Art"),
    ("Atonement", "/əˈtəʊn.mənt/", "Noun", "পাপস্খলন, প্রাশ্চিত্ত", "Act of atonement for sin.", "পাপের প্রাশ্চিত্তের কাজ।", ["Repentance"], [], "Religion"),
    ("Atrocity", "/əˈtrɒs.ə.ti/", "Noun", "নৃশংসতা, জঘন্য অত্যাচার", "War atrocities condemned.", "যুদ্ধের নৃশংসতার তীব্র নিন্দা করা হলো।", ["Cruelty"], [], "Politics"),
    ("Attenuation", "/əˌten.juˈeɪ.ʃən/", "Noun", "ক্ষীণতা, হ্রাস পাওয়া", "Attenuation of radio signal.", "রেডিও সংকেতের ক্ষীণতা।", ["Weakening"], [], "Science"),
    ("Audacity", "/ɔːˈdæs.ə.ti/", "Noun", "স্পর্ধা, দুঃসাহস", "Had audacity to argue.", "তর্ক করার স্পর্ধা ছিল।", ["Boldness"], [], "Spoken"),
    ("Augmentation", "/ˌɔːɡ.menˈteɪ.ʃən/", "Noun", "বৃদ্ধি, পরিবর্ধন", "Augmentation of income.", "আয়ের পরিবর্ধন।", ["Increase"], [], "Business"),
    ("Auspicious", "/ɔːˈspɪʃ.əs/", "Adj", "শুভ, মঙ্গলজনক", "An auspicious beginning.", "এক শুভ সূচনা।", ["Promising"], [], "Spoken"),
    ("Austere", "/ɒsˈtɪər/", "Adj", "কঠোর, অনাড়ম্বর", "An austere lifestyle.", "এক অনাড়ম্বর কঠোর জীবনযাত্রা।", ["Stern"], [], "BCS"),
    ("Authenticity", "/ˌɔː.θenˈtɪs.ə.ti/", "Noun", "প্রামাণ্যতা, খাঁটিত্ব", "Verified authenticity of art.", "শিল্পকর্মের প্রামাণ্যতা যাচাই করল।", ["Genuineness"], [], "Academic"),
    ("Autocratic", "/ˌɔː.təˈkræt.ɪk/", "Adj", "স্বৈরাচারী, একনায়কতান্ত্রিক", "An autocratic ruler.", "এক স্বৈরাচারী শাসক।", ["Despotic"], [], "Politics"),
    ("Autonomy", "/ɔːˈtɒn.ə.mi/", "Noun", "স্বায়ত্তশাসন, স্বাধীনতা", "Demanded regional autonomy.", "আঞ্চলিক স্বায়ত্তশাসন দাবি করল।", ["Independence"], [], "Politics"),
    ("Avaricious", "/ˌæv.əˈrɪʃ.əs/", "Adj", "অর্থলোভী, অতিলালসী", "An avaricious merchant.", "এক অর্থলোভী ব্যবসায়ী।", ["Greedy"], [], "GRE"),
    ("Axiomatic", "/ˌæk.si.əˈmæt.ɪk/", "Adj", "স্বতঃসিদ্ধ, স্বতঃপ্রমাণিত", "Axiomatic truth in math.", "গণিতের স্বতঃসিদ্ধ সত্য।", ["Self-evident"], [], "Academic")
]

selected_additional = []
for item in vocabulary_pool:
    w = item[0].strip()
    k = w.lower()
    if k not in existing_words and k not in b2_seen:
        b2_seen.add(k)
        selected_additional.append(item)
        if len(selected_additional) == needed:
            break

print(f"Selected {len(selected_additional)} new items from vocabulary pool.")

# Combine valid existing b2 items + selected additional items
max_id = max([item.get('id', 0) for item in non_b2_items if isinstance(item.get('id'), int)] or [0])

final_b2_list = list(valid_b2)
for idx, item in enumerate(selected_additional, start=max_id+len(valid_b2)+1):
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
    final_b2_list.append(entry)

print(f"Final batch2 items count: {len(final_b2_list)}")

# Save updated json
all_final = non_b2_items + final_b2_list
with open(dict_path, 'w', encoding='utf-8') as f:
    json.dump(all_final, f, ensure_ascii=False, indent=2)

print(f"Successfully saved {dict_path}. Total dict length is {len(all_final)}.")


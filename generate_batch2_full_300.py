import json, re

dict_path = 'app/src/main/assets/dictionary_1000.json'

with open(dict_path, 'r', encoding='utf-8') as f:
    dict_data = json.load(f)

non_b2_items = [item for item in dict_data if item.get('packId') != 'extra_300_batch2']
b2_items = [item for item in dict_data if item.get('packId') == 'extra_300_batch2']

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

print(f"Existing unique words count: {len(existing_words)}")

valid_b2 = []
b2_seen = set()
for item in b2_items:
    w = item.get('word', '').strip().lower()
    if w and w not in existing_words and w not in b2_seen:
        b2_seen.add(w)
        valid_b2.append(item)

needed = 300 - len(valid_b2)
print(f"Existing valid b2 items: {len(valid_b2)}. Still needed: {needed}")

# Generate pool of real words with full fields
words_data = [
    # B
    ("Bafflement", "/ˈbæf.əl.mənt/", "Noun", "হতাশা, বিভ্রান্তি", "Felt complete bafflement at the result.", "ফলাফলে সম্পূর্ণ বিভ্রান্তি অনুভব করল।", ["Confusion", "Perplexity"], [], "Psychology"),
    ("Balkiness", "/ˈbɔː.ki.nəs/", "Noun", "একগুঁয়েমি, জেদ", "Balkiness of the mule.", "খচ্চরের একগুঁয়েমি।", ["Stubbornness"], [], "General"),
    ("Balminess", "/ˈbɑː.mi.nəs/", "Noun", "মনোরম আবহাওয়া, স্নিগ্ধতা", "Balminess of the spring evening.", "বসন্তের সন্ধ্যার স্নিগ্ধতা।", ["Mildness", "Gentleness"], [], "Nature"),
    ("Banality", "/bəˈnɑː.lə.ti/", "Noun", "তুচ্ছতা, সাধারণতা", "Banality of daily routine.", "দৈনন্দিন কাজের তুচ্ছতা।", ["Triviality", "Triteness"], [], "Literature"),
    ("Banishment", "/ˈbæn.ɪʃ.mənt/", "Noun", "নির্বাসন, তাড়িয়ে দেওয়া", "Banishment from the kingdom.", "রাজ্য থেকে নির্বাসন।", ["Exile", "Expulsion"], [], "History"),
    ("Bantering", "/ˈbæn.tər.ɪŋ/", "Noun", "কৌতুকপূর্ণ আলাপ, ঠাট্টা", "Light bantering between friends.", "বন্ধুদের মধ্যে হালকা ঠাট্টা।", ["Raillery", "Joking"], [], "Spoken"),
    ("Barbarism", "/ˈbɑː.bər.ɪ.zəm/", "Noun", "বর্বরতা, অসভ্যতা", "Act of sheer barbarism.", "ঘোর বর্বরতার কাজ।", ["Savagery", "Crudity"], [], "Sociology"),
    ("Barrenness", "/ˈbær.ən.nəs/", "Noun", "বন্ধ্যাত্ব, অনুর্বরতা", "Barrenness of the desert soil.", "মরুভূমির মাটির অনুর্বরতা।", ["Infertility", "Aridity"], [], "Nature"),
    ("Bashing", "/ˈbæʃ.ɪŋ/", "Noun", "কঠোর সমালোচনা, প্রহার", "Media bashing of the policy.", "নীতির ওপর মিডিয়ার কঠোর সমালোচনা।", ["Criticism", "Assault"], [], "Media"),
    ("Befuddlement", "/bɪˈfʌd.əl.mənt/", "Noun", "বিভ্রান্তি, বুদ্ধিভ্রম", "In a state of befuddlement.", "বুদ্ধিভ্রমের অবস্থায়।", ["Confusion", "Daze"], [], "Psychology"),
    ("Beguilement", "/bɪˈɡaɪl.mənt/", "Noun", "প্রতারণা, মোহিতকরণ", "Beguilement by charming words.", "মনোমুগ্ধকর কথায় মোহিতকরণ।", ["Enchantment", "Deception"], [], "Literature"),
    ("Belatedness", "/bɪˈleɪ.tɪd.nəs/", "Noun", "বিলম্ব, দেরিতে হওয়া", "Belatedness of the response.", "উত্তরের বিলম্ব।", ["Lateness", "Delay"], [], "Spoken"),
    ("Belittlement", "/bɪˈlɪt.əl.mənt/", "Noun", "অবমূল্যায়ন, তুচ্ছকরণ", "Tired of constant belittlement.", "ক্রমাগত অবমূল্যায়নে ক্লান্ত।", ["Disparagement", "Depreciation"], [], "Psychology"),
    ("Belligerency", "/bəˈlɪdʒ.ər.ən.si/", "Noun", "যুদ্ধংদেহি মনোভাব", "Showed belligerency in dispute.", "বিতর্কে যুদ্ধংদেহি মনোভাব দেখালো।", ["Aggression", "Hostility"], [], "Politics"),
    ("Benefaction", "/ˌben.ɪˈfæk.ʃən/", "Noun", "দানশীলতা, অনুদান", "Generous benefaction to hospital.", "হাসপাতালে উদার অনুদান।", ["Donation", "Gift"], [], "Sociology"),
    ("Benevolence", "/bəˈnev.əl.əns/", "Noun", "উদারতা, মহত্ত্ব", "Act of pure benevolence.", "খাঁটি উদারতার কাজ।", ["Kindness", "Altruism"], [], "BCS"),

    # C
    ("Candidness", "/ˈkæn.dɪd.nəs/", "Noun", "কপটতাহীনতা, স্পষ্টবাদিতা", "Appreciated her candidness.", "তার স্পষ্টবাদিতার প্রশংসা করল।", ["Frankness", "Honesty"], [], "Spoken"),
    ("Canonization", "/ˌkæn.ən.aɪˈzeɪ.ʃən/", "Noun", "সাধু ঘোষণা, মর্যাদা প্রদান", "Canonization of the saint.", "সাধুর মর্যাদা প্রদান।", ["Glorification"], [], "History"),
    ("Capriciousness", "/kəˈprɪʃ.əs.nəs/", "Noun", "খামখেয়ালীপনা, পরিবর্তনশীলতা", "Capriciousness of the market.", "বাজারের খামখেয়ালীপনা।", ["Fickleness", "Whim"], [], "Economics"),
    ("Captivation", "/ˌkæp.tɪˈveɪ.ʃən/", "Noun", "মুগ্ধতা, আকর্ষণ", "Captivation by the music.", "সঙ্গীতে মুগ্ধতা।", ["Fascination", "Charm"], [], "Art"),
    ("Carelessness", "/ˈkeə.ləs.nəs/", "Noun", "অসাবধানতা, অবহেলা", "Accident caused by carelessness.", "অসাবধানতার কারণে দুর্ঘটনা।", ["Negligence", "Heedlessness"], [], "Spoken"),
    ("Castigation", "/ˌkæs.tɪˈɡeɪ.ʃən/", "Noun", "কঠোর তিরস্কার, শাস্তি", "Severe castigation by judge.", "বিচারক কর্তৃক কঠোর তিরস্কার।", ["Rebuking", "Chastisement"], [], "Legal"),
    ("Cataclysmic", "/ˌkæt.əˈklɪz.mɪk/", "Adj", "মহাপ্রলয়ঙ্করী, প্রলয়ঙ্করি", "Cataclysmic flood destroyed city.", "মহাপ্রলয়ঙ্করী বন্যা শহর ধ্বংস করল।", ["Devastating", "Disastrous"], [], "Nature"),
    ("Categorization", "/ˌkæt.ə.ɡər.aɪˈzeɪ.ʃən/", "Noun", "শ্রেণীবদ্ধকরণ", "Categorization of books.", "বইয়ের শ্রেণীবদ্ধকরণ।", ["Classification", "Grouping"], [], "Academic"),
    ("Cautiousness", "/ˈkɔː.ʃəs.nəs/", "Noun", "সতর্কতা, সাবধানতা", "Proceeded with cautionness.", "সতর্কতার সাথে অগ্রসর হলো।", ["Prudence", "Care"], [], "Spoken"),
    ("Celebratory", "/ˈsel.ə.brə.tər.i/", "Adj", "উৎসবমুখর, উদযাপনী", "Celebratory dinner after win.", "জয়ের পর উৎসবমুখর নৈশভোজ।", ["Festive", "Joyful"], [], "Culture"),
    ("Censorious", "/senˈsɔː.ri.əs/", "Adj", "ছিদ্রান্বেষী, সমালোচনামুখর", "Censorious remarks on art.", "শিল্পের ওপর সমালোচনামুখর মন্তব্য।", ["Critical", "Fault-finding"], [], "GRE"),
    ("Centralization", "/ˌsen.trə.laɪˈzeɪ.ʃən/", "Noun", "কেন্দ্রীকরণ", "Centralization of authority.", "ক্ষমতার কেন্দ্রীকরণ।", ["Concentration"], [], "Politics"),
    ("Chagrin", "/ˈʃæɡ.rɪ কথাও/", "Noun", "ক্ষোভ, হতাশা ও বিরক্তি", "To his chagrin, he failed.", "তার হতাশার বিষয়, সে ব্যর্থ হলো।", ["Vexation", "Mortification"], [], "Literature"),
    ("Characteristic", "/ˌkær.ək.təˈrɪs.tɪk/", "Noun", "বৈশিষ্ট্য, লক্ষণ", "Key characteristic of species.", "প্রজাতির প্রধান বৈশিষ্ট্য।", ["Feature", "Trait"], [], "Science"),
    ("Charlatanry", "/ˈʃɑː.lə.tən.ri/", "Noun", "ভণ্ডামি, প্রবঞ্চনা", "Exposed his charlatanry.", "তার ভণ্ডামি উন্মোচিত করল।", ["Quackery", "Imposture"], [], "Literature"),
    ("Cheerfulness", "/ˈtʃɪə.fəl.nəs/", "Noun", "প্রফুল্লতা, আনন্দের মেজাজ", "Brought cheerfulness to home.", "ঘরে প্রফুল্লতা নিয়ে এলো।", ["Happiness", "Joy"], [], "Spoken"),
    ("Chronological", "/ˌkrɒn.əˈlɒdʒ.ɪ.kəl/", "Adj", "কালানুক্রমিক, সময়ানুক্রমিক", "Chronological order of events.", "ঘটনার কালানুক্রমিক ধারাবাহিকতা।", ["Sequential"], [], "History"),

    # D
    ("Dauntlessness", "/ˈdɔːnt.ləs.nəs/", "Noun", "নির্ভীকতা, অদম্য সাহস", "Dauntlessness in battle.", "যুদ্ধে অদম্য সাহস।", ["Fearlessness", "Bravery"], [], "History"),
    ("Debasement", "/dɪˈbeɪs.mənt/", "Noun", "অবক্ষয়, মানহানি", "Debasement of moral values.", "নৈতিক মূল্যের অবক্ষয়।", ["Degradation", "Devaluation"], [], "Sociology"),
    ("Deceitfulness", "/dɪˈsiːt.fəl.nəs/", "Noun", "প্রতারণাপূর্ণ স্বভাব, শঠতা", "Punished for deceitfulness.", "শঠতার জন্য শাস্তি পেল।", ["Dishonesty", "Treachery"], [], "Psychology"),
    ("Decisiveness", "/dɪˈsaɪ.sɪv.nəs/", "Noun", "দৃঢ়সংকল্পতা, সিদ্ধান্ত গ্রহণ ক্ষমতা", "Showed decisiveness in crisis.", "সংকটে দৃঢ়সংকল্পতা দেখালো।", ["Determination", "Resolution"], [], "Business"),
    ("Decolonial", "/ˌdiː.kəˈləʊ.ni.əl/", "Adj", "উপনিবেশ-উত্তর, ঔপনিবেশিকতামুক্ত", "Decolonial literature study.", "ঔপনিবেশিকতামুক্ত সাহিত্য চর্চা।", ["Post-colonial"], [], "Academic"),
    ("Deconstructive", "/ˌdiː.kənˈstrʌk.tɪv/", "Adj", "বিশ্লেষণমূলক, গঠন ভাঙার কৌশল", "Deconstructive criticism of essay.", "প্রবন্ধের বিশ্লেষণমূলক মূল্যায়ন।", ["Analytical"], [], "Literature"),
    ("Deference", "/ˈdef.ər.əns/", "Noun", "শ্রদ্ধা, মান্যতা", "Showed deference to elders.", "গুরুজনদের প্রতি মান্যতা দেখালো।", ["Respect", "Submission"], [], "Spoken"),
    ("Defiantness", "/dɪˈfaɪ.ənt.nəs/", "Noun", "স্পর্ধিত অবাধ্যতা", "Defiantness against unjust law.", "অন্যায্য আইনের বিরুদ্ধে অবাধ্যতা।", ["Rebelliousness", "Resistance"], [], "Politics"),
    ("Definitiveness", "/dɪˈfɪn.ɪ.tɪv.nəs/", "Noun", "চূড়ান্ততা, সুনির্দিষ্টতা", "Definitiveness of the answer.", "উত্তরের চূড়ান্ততা।", ["Finality", "Certainty"], [], "Academic"),
    ("Delectable", "/dɪˈlek.tə.bəl/", "Adj", "সুস্বাদু, চমৎকার", "A delectable feast served.", "এক সুস্বাদু ভোজ পরিবেশন করা হলো।", ["Delicious", "Tasty"], [], "Spoken"),

    # E
    ("Eagerness", "/ˈiː.ɡə.nəs/", "Noun", "আগ্রহ, ব্যাকুলতা", "Learned with great eagerness.", "প্রবল আগ্রহ নিয়ে শিখল।", ["Enthusiasm", "Keenness"], [], "Spoken"),
    ("Earnestness", "/ˈɜː.nɪst.nəs/", "Noun", "আন্তরিকতা, একাগ্রতা", "Spoke with deep earnestness.", "গভীর আন্তরিকতার সাথে বলল।", ["Sincerity", "Seriousness"], [], "Spoken"),
    ("Eccentricity", "/ˌek.senˈtrɪs.ə.ti/", "Noun", "খামখেয়ালীপনা, অদ্ভূত স্বভাব", "Known for eccentricity.", "অদ্ভূত স্বভাবের জন্য পরিচিত।", ["Peculiarity"], [], "Spoken"),
    ("Ecclesiastical", "/ɪˌkliː.ziˈæs.tɪ.kəl/", "Adj", "ধর্মীয়, গির্জা সংক্রান্ত", "Ecclesiastical law studies.", "ধর্মীয় আইন অধ্যয়ন।", ["Churchly", "Religious"], [], "History"),
    ("Effectiveness", "/ɪˈfek.tɪv.nəs/", "Noun", "কার্যকারিতা", "Effectiveness of new strategy.", "নতুন কৌশলের কার্যকারিতা।", ["Efficacy", "Efficiency"], [], "Business"),

    # F
    ("Fabulousness", "/ˈfæb.jə.ləs.nəs/", "Noun", "চমৎকারিত্ব, চমৎকার ভাব", "Fabulousness of the dress.", "পোশাকের চমৎকার ভাব।", ["Splendor", "Marvelousness"], [], "Fashion"),
    ("Facetiousness", "/fəˈsiː.ʃəs.nəs/", "Noun", "তামাশাপূর্ণতা, চটুলতা", "Annoyed by facetiousness.", "তামাশাপূর্ণতায় বিরক্ত।", ["Flippancy", "Humor"], [], "Spoken"),
    ("Faithfulness", "/ˈfeɪθ.fəl.nəs/", "Noun", "বিশ্বস্ততা, আনুগত্য", "Rewarded for faithfulness.", "বিশ্বস্ততার জন্য পুরস্কৃত।", ["Loyalty", "Fidelity"], [], "Spoken"),
    ("Falsification", "/ˌfɔːl.sɪ.fɪˈkeɪ.ʃən/", "Noun", "জালিয়াতি, পরিবর্তন", "Falsification of evidence.", "প্রমাণের জালিয়াতি।", ["Forgery", "Distortion"], [], "Legal"),
    ("Fascinating", "/ˈfæs.ən.eɪ.tɪŋ/", "Adj", "মনোমুগ্ধকর, দারুণ আকর্ষণীয়", "A fascinating history book.", "এক দারুণ আকর্ষণীয় ইতিহাস বই।", ["Captivating", "Charming"], [], "Spoken"),

    # G
    ("Gallantry", "/ˈɡæl.ən.tri/", "Noun", "বীরত্ব, শৌর্য", "Awarded medal for gallantry.", "বীরত্বের জন্য পদক পেল।", ["Bravery", "Valor"], [], "History"),
    ("Garrulousness", "/ˈɡær.əl.əs.nəs/", "Noun", "বাচালতা, অহেতুক কথা বলা", "Garrulousness annoyed colleagues.", "বাচালতা সহকর্মীদের বিরক্ত করল।", ["Talkativeness", "Loquacity"], [], "Spoken"),
    ("Gaudiness", "/ˈɡɔː.di.nəs/", "Noun", "ভোজবাজিসুলভ রঙচঙে ভাব", "Gaudiness of the costume.", "পোশাকের রঙচঙে ভাব।", ["Flashiness", "Tastelessness"], [], "Fashion"),
    ("Generalization", "/ˌdʒen.ər.əl.aɪˈzeɪ.ʃən/", "Noun", "সাধারণীকরণ", "Avoid sweeping generalization.", "পাইকারি সাধারণীকরণ এড়িয়ে চলুন।", ["Broad statement"], [], "Academic"),
    ("Generosity", "/ˌdʒen.əˈrɒs.ə.ti/", "Noun", "উদারতা, মহত্ত্ব", "Praised for generosity.", "উদারতার জন্য প্রশংসিত।", ["Magnanimity", "Open-handedness"], [], "Spoken"),

    # H
    ("Harmoniousness", "/hɑːˈməʊ.ni.əs.nəs/", "Noun", "সামঞ্জস্য, মেলবন্ধন", "Harmoniousness of the melody.", "সুরের মেলবন্ধন।", ["Concord", "Harmony"], [], "Music"),
    ("Haughtiness", "/ˈhɔː.ti.nəs/", "Noun", "অহংকার, ঔদ্ধত্য", "Haughtiness made him unpopular.", "ঔদ্ধত্য তাকে অপ্রিয় করল।", ["Arrogance", "Pride"], [], "Psychology"),
    ("Hazardousness", "/ˈhæz.ə.dəs.nəs/", "Noun", "ঝুঁকিপূর্ণতা, বিপজ্জনকতা", "Hazardousness of chemical job.", "রাসায়নিক কাজের বিপজ্জনকতা।", ["Danger", "Peril"], [], "Safety"),
    ("Heartlessness", "/ˈhɑːt.ləs.nəs/", "Noun", "নিষ্ঠুরতা, হীনতা", "Heartlessness toward animals.", "প্রাণীদের প্রতি নিষ্ঠুরতা।", ["Cruelty", "Callousness"], [], "Psychology"),
    ("Heroism", "/ˈher.əʊ.ɪ.zəm/", "Noun", "বীরত্ব, সাহসিকতা", "Act of immense heroism.", "অসীম সাহসিকতার কাজ।", ["Valour", "Courage"], [], "History"),

    # I
    ("Idealization", "/aɪˌdɪə.laɪˈzeɪ.ʃən/", "Noun", "আদর্শরূপ দান", "Idealization of childhood.", "শৈশবের আদর্শরূপ দান।", ["Romanticization"], [], "Psychology"),
    ("Identifiable", "/aɪˌden.tɪˈfaɪ.ə.bəl/", "Adj", "শনাক্তকরণযোগ্য, চেনা যায় এমন", "Identifiable footprint on sand.", "বালুতে শনাক্তকরণযোগ্য পায়ের ছাপ।", ["Recognizable"], [], "General"),
    ("Ideological", "/ˌaɪ.di.əˈlɒdʒ.ɪ.kəl/", "Adj", "মতাদর্শগত, আদর্শিক", "Ideological differences in party.", "দলে মতাদর্শগত পার্থক্য।", ["Doctrinal"], [], "Politics"),
    ("Ignominious", "/ˌɪɡ.nəˈmɪn.i.əs/", "Adj", "অপমানজনক, লজ্জাকর", "Ignominious defeat in battle.", "যুদ্ধে অপমানজনক পরাজয়।", ["Humiliating", "Disgraceful"], [], "GRE"),
    ("Illuminating", "/ɪˈluː.mɪ.neɪ.tɪŋ/", "Adj", "আলোকপাতকারী, স্পষ্টকারী", "An illuminating lecture.", "এক স্পষ্টকারী চমৎকার বক্তৃতা।", ["Enlightening", "Revealing"], [], "Academic"),

    # J - L
    ("Joviality", "/ˌdʒəʊ.viˈæl.ə.ti/", "Noun", "হাসিখুশি মেজাজ, প্রফুল্লতা", "Full of warm joviality.", "উষ্ণ প্রফুল্লতায় ভরা।", ["Cheerfulness", "Merriment"], [], "Spoken"),
    ("Jubilance", "/ˈdʒuː.bɪ.ləns/", "Noun", "উল্লাস, বিজয়ানন্দ", "Jubilance after the match.", "ম্যাচের পর বিজয়ানন্দ।", ["Exultation", "Triumph"], [], "Sports"),
    ("Judiciousness", "/dʒuːˈdɪʃ.əs.nəs/", "Noun", "বিচক্ষণতা, সুবিবেচনা", "Acted with judiciousness.", "সুবিবেচনার সাথে কাজ করল।", ["Prudence", "Wisdom"], [], "IELTS"),
    ("Lamentation", "/ˌlæm.ənˈteɪ.ʃən/", "Noun", "বিলাপ, দুঃখ প্রকাশ", "Lamentation for the dead.", "মৃতের জন্য বিলাপ।", ["Mourning", "Weeping"], [], "Literature"),
    ("Liberation", "/ˌlɪb.ərˈeɪ.ʃən/", "Noun", "মুক্তি, স্বাধীনতা", "Liberation war of Bangladesh.", "বাংলাদেশের মুক্তিযুদ্ধ।", ["Freedom", "Emancipation"], [], "History"),

    # M - N
    ("Magnificence", "/mæɡˈnɪf.ɪ.səns/", "Noun", "জাঁকজমক, চমৎকারিত্ব", "Magnificence of palace.", "প্রাসাদের জাঁকজমক।", ["Splendor", "Grandeur"], [], "Architecture"),
    ("Maliciousness", "/məˈlɪʃ.əs.nəs/", "Noun", "দুষ্টুমি, হিংসাপরায়ণতা", "Maliciousness of the rumor.", "গুজবের হিংসাপরায়ণতা।", ["Malevolence", "Spite"], [], "Psychology"),
    ("Malleability", "/ˌmæl.i.əˈbɪl.ə.ti/", "Noun", "নমনীয়তা, গঠনযোগ্যতা", "Malleability of gold metal.", "স্বর্ণের নমনীয়তা।", ["Flexibility", "Plasticity"], [], "Science"),
    ("Manifestation", "/ˌmæn.ɪ.fesˈteɪ.ʃən/", "Noun", "প্রকাশ, আত্মপ্রকাশ", "Manifestation of disease.", "রোগের আত্মপ্রকাশ।", ["Expression", "Indication"], [], "Medical"),
    ("Melancholic", "/ˌmel.ənˈkɒl.ɪk/", "Adj", "বিষাদগ্রস্ত, বিষণ্ণ", "A melancholic melody played.", "এক বিষণ্ণ সুর বাজানো হলো।", ["Sorrowful", "Sad"], [], "Music"),
    ("Meticulousness", "/məˈtɪk.jə.ləs.nəs/", "Noun", "সূক্ষ্মতা, নিখুঁত কাজ", "Meticulousness in design.", "নকশায় নিখুঁত কাজ।", ["Thoroughness", "Care"], [], "Design"),
    ("Mystification", "/ˌmɪs.tɪ.fɪˈkeɪ.ʃən/", "Noun", "রহস্য সৃষ্টি, ধোঁকা", "Mystification of simple facts.", "সহজ ঘটনার রহস্য সৃষ্টি।", ["Puzzlement", "Bafflement"], [], "Literature"),
    ("Nationalism", "/ˈnæʃ.ən.əl.ɪ.zəm/", "Noun", "জাতীয়তাবাদ", "Rise of democratic nationalism.", "গণতান্ত্রিক জাতীয়তাবাদের উত্থান।", ["Patriotism"], [], "Politics"),
    ("Noxiousness", "/ˈnɒk.ʃəs.nəs/", "Noun", "বিষাক্ততা, অনিষ্টকারিতা", "Noxiousness of toxic gas.", "বিষাক্ত গ্যাসের অনিষ্টকারিতা।", ["Toxicity", "Harmfulness"], [], "Science"),

    # O - P
    ("Obdurateness", "/ˈɒb.djʊ.rət.nəs/", "Noun", "একগুঁয়েমি, অবাধ্যতা", "Obdurateness in refusal.", "প্রত্যাখ্যানে একগুঁয়েমি।", ["Stubbornness", "Obstinacy"], [], "GRE"),
    ("Obliteration", "/əˌblɪt.əˈreɪ.ʃən/", "Noun", "সম্পূর্ণ চিহ্ন মুছে ফেলা", "Obliteration of the town.", "শহরের চিহ্ন সম্পূর্ণ মুছে ফেলা।", ["Destruction", "Erasing"], [], "History"),
    ("Obscuritism", "/əbˈskjʊər.ɪ.tɪ.zəm/", "Noun", "অস্পষ্টতা রাখা, গোঁড়ামি", "Fought against obscuritism.", "গোঁড়ামির বিরুদ্ধে লড়াই করল।", ["Dogmatism"], [], "Academic"),
    ("Plausibility", "/ˌplɔː.zəˈbɪl.ə.ti/", "Noun", "বিশ্বাসযোগ্যতা", "Plausibility of the explanation.", "ব্যাখ্যার বিশ্বাসযোগ্যতা।", ["Credibility", "Likelihood"], [], "Academic"),
    ("Ponderousness", "/ˈpɒn.dər.əs.nəs/", "Noun", "ভারী ভাব, ধীর গতি", "Ponderousness of style.", "শৈলীর ভারী ভাব।", ["Heaviness", "Clumsiness"], [], "Literature"),
    ("Preeminence", "/priːˈem.ɪ.nəns/", "Noun", "শ্রেষ্ঠত্ব, প্রাধান্য", "Achieved preeminence.", "শ্রেষ্ঠত্ব অর্জন করল।", ["Superiority", "Excellence"], [], "Academic"),
    ("Preservation", "/ˌprez.əˈveɪ.ʃən/", "Noun", "সংরক্ষণ, হেফাযত", "Preservation of heritage.", "ঐতিহ্যের সংরক্ষণ।", ["Conservation", "Protection"], [], "Culture"),
    ("Profoundness", "/prəˈfaʊnd.nəs/", "Noun", "গভীরতা, প্রজ্ঞা", "Profoundness of thought.", "চিন্তার গভীরতা।", ["Depth", "Wisdom"], [], "Philosophy"),

    # Q - Z
    ("Quantification", "/ˌkwɒn.tɪ.fɪˈkeɪ.ʃən/", "Noun", "পরিমাণ নির্ধারণ", "Quantification of results.", "ফলাফলের পরিমাণ নির্ধারণ।", ["Measurement"], [], "Science"),
    ("Radiance", "/ˈreɪ.di.əns/", "Noun", "দীপ্তি, উজ্জ্বলতা", "Radiance of the sun.", "সূর্যের উজ্জ্বল দীপ্তি।", ["Brightness", "Glow"], [], "Nature"),
    ("Recalcitrance", "/rɪˈkæl.sɪ.trəns/", "Noun", "অধ্যবসায় বা অবাধ্যতা", "Punished for recalcitrance.", "অবাধ্যতার জন্য শাস্তি পেল।", ["Defiance", "Obstinacy"], [], "GRE"),
    ("Reconciliation", "/ˌrek.ənˌsɪl.iˈeɪ.ʃən/", "Noun", "পুনর্মিলন, আপস", "Reconciliation between parties.", "দলগুলির মধ্যে পুনর্মিলন।", ["Harmony", "Settlement"], [], "Politics"),
    ("Regeneration", "/rɪˌdʒen.əˈreɪ.ʃən/", "Noun", "পুনরুজ্জীবন, পুনর্গঠন", "Regeneration of urban area.", "নগর এলাকার পুনর্গঠন।", ["Renewal", "Revival"], [], "Geography"),
    ("Reliability", "/rɪˌlaɪ.əˈbɪl.ə.ti/", "Noun", "নির্ভরযোগ্যতা", "Reliability of the test.", "পরীক্ষার নির্ভরযোগ্যতা।", ["Dependability", "Trustworthiness"], [], "Spoken"),
    ("Remuneration", "/rɪˌmjuː.nərˈeɪ.ʃən/", "Noun", "পারিশ্রমিক, মেহনতানা", "Received fair remuneration.", "ন্যায্য পারিশ্রমিক পেল।", ["Payment", "Salary"], [], "Business"),
    ("Resplendence", "/rɪˈsplen.dəns/", "Noun", "জাঁকজমক, মহোজ্জ্বল শোভা", "Resplendence of her dress.", "তার পোশাকের মহোজ্জ্বল শোভা।", ["Splendor", "Brilliance"], [], "Fashion"),
    ("Scintillating", "/ˈsɪn.tɪ.leɪ.tɪŋ/", "Adj", "ঝলমলে, বুদ্ধিদীপ্ত", "A scintillating conversation.", "এক বুদ্ধিদীপ্ত ঝলমলে আলাপ।", ["Sparkling", "Brilliant"], [], "Spoken"),
    ("Sophistication", "/səˌfɪs.tɪˈkeɪ.ʃən/", "Noun", "জটিলতা, আধুনিক রুচিশীলতা", "Sophistication of system.", "সিস্টেমের আধুনিক রুচিশীলতা।", ["Refinement", "Elegance"], [], "Technology"),
    ("Subsumption", "/səbˈsʌmp.ʃən/", "Noun", "অন্তর্ভুক্তি", "Subsumption of minor laws.", "ছোট আইনের অন্তর্ভুক্তি।", ["Inclusion"], [], "Legal"),
    ("Surreptitious", "/ˌsʌr.əpˈtɪʃ.əs/", "Adj", "গোপন, অলক্ষ্যে সংঘটিত", "A surreptitious glance.", "এক অলক্ষ্যে গোপন দৃষ্টি।", ["Secret", "Furtive"], [], "GRE"),
    ("Transcendence", "/trænˈsen.dəns/", "Noun", "মহীয়সী উৎকর্ষ, অতিক্রম", "Spiritual transcendence.", "আধ্যাত্মিক মহীয়সী উৎকর্ষ।", ["Excellence", "Supremacy"], [], "Philosophy"),
    ("Unassailable", "/ˌʌn.əˈseɪ.lə.bəl/", "Adj", "অখণ্ডনীয়, আক্রমণাতীত", "Unassailable argument.", "অখণ্ডনীয় যুক্তি।", ["Indisputable"], [], "Legal"),
    ("Veneration", "/ˌven.ərˈeɪ.ʃən/", "Noun", "গভীর শ্রদ্ধা, ভক্তি", "Held in veneration.", "গভীর শ্রদ্ধায় রাখা।", ["Reverence", "Respect"], [], "Culture"),
    ("Vindication", "/ˌvɪn.dɪˈkeɪ.ʃən/", "Noun", "সত্যতা প্রমাণ, নির্দোষ প্রমাণ", "Vindication of his rights.", "তার অধিকারের সত্যতা প্রমাণ।", ["Exoneration", "Proof"], [], "Legal"),
    ("Vulnerability", "/ˌvʌl.nər.əˈbɪl.ə.ti/", "Noun", "সুরক্ষাহীনতা, স্পর্শকাতরতা", "Security vulnerability fixed.", "নিরাপত্তার সুরক্ষাহীনতা ঠিক করা হলো।", ["Weakness"], [], "Technology"),
    ("Wholehearted", "/ˌhəʊlˈhɑː.tɪd/", "Adj", "একান্ত, সর্বান্তঃকরণে", "Gave wholehearted support.", "সর্বান্তঃকরণে সমর্থন দিল।", ["Sincere", "Enthusiastic"], [], "Spoken")
]

# Additional 220 items generator using valid base words to guarantee unique additions
letters = "BCDEFGHIJKLMNOPQRSTUVWXYZ"
bases = [
    ("Acclamation", "/ˌæk.ləˈmeɪ.ʃən/", "Noun", "করতালিসহ সাধুবাদ", "Acclamation from the audience.", "দর্শকদের করতালিসহ সাধুবাদ।", ["Applause"], [], "General"),
    ("Accreditation", "/əˌkred.ɪˈteɪ.ʃən/", "Noun", "অনুমোদন, স্বীকৃতি", "Hospital gained accreditation.", "হাসপাতাল স্বীকৃতি অর্জন করল।", ["Recognition"], [], "Academic"),
    ("Acquisitive", "/əˈkwɪz.ɪ.tɪv/", "Adj", "সংগ্রহপ্রিয়, লালসী", "An acquisitive mindset.", "এক সংগ্রহপ্রিয় মনোভাব।", ["Greedy"], [], "Psychology"),
    ("Adaptability", "/əˌdæp.təˈbɪl.ə.ti/", "Noun", "অভিযোজনযোগ্যতা", "Adaptability in new environment.", "নতুন পরিবেশে অভিযোজনযোগ্যতা।", ["Flexibility"], [], "General"),
    ("Advisability", "/ədˌvaɪ.zəˈbɪl.ə.ti/", "Noun", "পরামর্শযোগ্যতা, যুক্তিযুক্ততা", "Discussed advisability of plan.", "পরিকল্পনার যুক্তিযুক্ততা আলোচনা করল।", ["Prudence"], [], "Business"),
    ("Affirmation", "/ˌæf.əˈmeɪ.ʃən/", "Noun", "দৃঢ় সমর্থন, স্বীকৃতি", "Received positive affirmation.", "ইতিবাচক সমর্থন পেল।", ["Confirmation"], [], "Psychology"),
    ("Affliction", "/əˈflɪk.ʃən/", "Noun", "যাতনা, কষ্ট", "Comforted him in affliction.", "কষ্টের দিনে তাকে সান্ত্বনা দিল।", ["Suffering"], [], "Spoken"),
    ("Aggravation", "/ˌæɡ.rəˈveɪ.ʃən/", "Noun", "অবনতি, রাগিয়ে দেওয়া", "Cause of constant aggravation.", "ক্রমাগত রাগিয়ে দেওয়ার কারণ।", ["Annoyance"], [], "Spoken"),
    ("Alienated", "/ˈeɪ.li.ə.neɪ.tɪd/", "Adj", "বিচ্ছিন্ন, পর হয়ে যাওয়া", "Felt alienated from family.", "পরিবার থেকে বিচ্ছিন্ন অনুভব করল।", ["Estranged"], [], "Sociology"),
    ("Allegorical", "/ˌæl.ɪˈɡɒr.ɪ.kəl/", "Adj", "রূপক, প্রচ্ছন্ন অর্থবহ", "An allegorical novel.", "এক রূপক উপন্যাস।", ["Symbolic"], [], "Literature"),
    ("Alleviation", "/əˌliː.viˈeɪ.ʃən/", "Noun", "উপশম, লাঘব", "Alleviation of poverty.", "দারিদ্র্যের উপশম।", ["Relief"], [], "Economics"),
    ("Alteration", "/ˌɔːl.təˈreɪ.ʃən/", "Noun", "পরিবর্তন, সংশোধন", "Made slight alteration to coat.", "কোটটিতে সামান্য পরিবর্তন করল।", ["Change"], [], "Spoken"),
    ("Amalgamation", "/əˌmæl.ɡəˈmeɪ.ʃən/", "Noun", "একত্রীকরণ, সংমিশ্রণ", "Amalgamation of two firms.", "দুই প্রতিষ্ঠানের সংমিশ্রণ।", ["Merger"], [], "Business"),
    ("Ameliorative", "/əˈmiː.li.ə.rə.tɪv/", "Adj", "উন্নতিসাধনকারী", "Ameliorative measures taken.", "উন্নতিসাধনকারী পদক্ষেপ নেওয়া হলো।", ["Improving"], [], "Sociology"),
    ("Amicability", "/ˌæm.ɪ.kəˈbɪl.ə.ti/", "Noun", "বন্ধুভাবাপন্নতা, সৌহার্দ্য", "Resolved with amicability.", "সৌহার্দ্যের সাথে সমাধান করল।", ["Friendliness"], [], "Spoken"),
    ("Amplification", "/ˌæm.plɪ.fɪˈkeɪ.ʃən/", "Noun", "বিবর্ধন, প্রসার", "Amplification of sound signal.", "শব্দ সংকেতের বিবর্ধন।", ["Expansion"], [], "Science"),
    ("Anatomical", "/ˌæn.əˈtɒm.ɪ.kəl/", "Adj", "শারীরস্থানবিষয়ক", "Anatomical chart in lab.", "পরীক্ষাগারে শারীরস্থানবিষয়ক চার্ট।", ["Structural"], [], "Medical"),
    ("Ancestral", "/ænˈses.trəl/", "Adj", "পৈতৃক, বংশানুক্রমিক", "Ancestral home in village.", "গ্রামে পৈতৃক ভিটা।", ["Hereditary"], [], "History"),
    ("Annihilate", "/əˈnaɪ.ə.leɪt/", "Verb", "সম্পূর্ণ ধ্বংস করা", "Enemy force annihilated.", "শত্রুবাহিনী সম্পূর্ণ ধ্বংস করা হলো।", ["Destroy"], [], "Military"),
    ("Annotation", "/ˌæn.əˈteɪ.ʃən/", "Noun", "টীকা, ব্যাখ্যাসংকেত", "Book with detailed annotation.", "বিস্তারিত টীকাযুক্ত বই।", ["Note"], [], "Academic"),
    ("Anomalousness", "/əˈnɒm.ə.ləs.nəs/", "Noun", "ব্যতিক্রমী ভাব, অনিয়ম", "Anomalousness of reaction.", "প্রতিক্রিয়ার ব্যতিক্রমী ভাব।", ["Irregularity"], [], "Science"),
    ("Anonymousness", "/əˈnɒn.ɪ.məs.nəs/", "Noun", "বেনামি ভাব, অজ্ঞাতপরিচয়", "Desired complete anonymousness.", "সম্পূর্ণ অজ্ঞাতপরিচয় কামনা করল।", ["Anonymity"], [], "General"),
    ("Antagonistic", "/ænˌtæɡ.ənˈɪs.tɪk/", "Adj", "বৈরী, বিরোধিতাপূর্ণ", "Antagonistic attitude to rivals.", "প্রতিদ্বন্দ্বীদের প্রতি বৈরী মনোভাব।", ["Hostile"], [], "Spoken"),
    ("Anticipatory", "/ænˌtɪs.ɪˈpeɪ.tər.i/", "Adj", "প্রত্যাশামূলক, অগ্রিম", "Anticipated joy felt.", "অগ্রিম আনন্দ অনুভূত হলো।", ["Expectant"], [], "Psychology"),
    ("Antiseptic", "/ˌæn.tiˈsep.tɪk/", "Adj", "জীবাণুমুক্তকারী", "Applied antiseptic cream.", "জীবাণুমুক্তকারী ক্রিম লাগালো।", ["Sterile"], [], "Medical"),
    ("Apathetic", "/ˌæp.əˈθet.ɪk/", "Adj", "উদাসীন, আগ্রহহীন", "Apathetic about election.", "নির্বাচন সম্পর্কে উদাসীন।", ["Indifferent"], [], "Spoken"),
    ("Apparel", "/əˈpær.əl/", "Noun", "পোশাক-পরিচ্ছদ", "Garment industry apparel.", "পোশাক শিল্পের পরিচ্ছদ।", ["Clothing"], [], "Fashion"),
    ("Applicability", "/ˌæp.lɪ.kəˈbɪl.ə.ti/", "Noun", "প্রযোজ্যতা, প্রয়োগযোগ্যতা", "Applicability of rule.", "নিয়মের প্রযোজ্যতা।", ["Relevance"], [], "Academic"),
    ("Apportionment", "/əˈpɔː.ʃən.mənt/", "Noun", "বণ্টন, ভাগকরণ", "Fair apportionment of budget.", "বাজেটের ন্যায্য বণ্টন।", ["Distribution"], [], "Economics"),
    ("Apprehension", "/ˌæp.rɪˈhen.ʃən/", "Noun", "আশঙ্কা, ভয়", "Felt deep apprehension.", "গভীর আশঙ্কা অনুভব করল।", ["Anxiety"], [], "Spoken"),
    ("Approximation", "/əˌprɒk.sɪˈmeɪ.ʃən/", "Noun", "আনুমানিক হিসাব, আসন্নতা", "Rough approximation of total.", "মোটে আনুমানিক হিসাব।", ["Estimate"], [], "Math"),
    ("Arduousness", "/ˈɑː.dʒu.əs.nəs/", "Noun", "কঠোর পরিশ্রমসাধ্যতা", "Arduousness of climb.", "পাহাড় চড়ার কঠোর পরিশ্রমসাধ্যতা।", ["Difficulty"], [], "General"),
    ("Argumentative", "/ˌɑːɡ.jəˈmen.tə.tɪv/", "Adj", "তর্কপ্রিয়, যুক্তিপূর্ণ", "An argumentative nature.", "এক তর্কপ্রিয় স্বভাব।", ["Quarrelsome"], [], "Spoken"),
    ("Arrogance", "/ˈær.ə.ɡəns/", "Noun", "অহংকার, ঔদ্ধত্য", "Hated his arrogance.", "তার অহংকার ঘৃণা করল।", ["Haughtiness"], [], "Psychology"),
    ("Articulation", "/ɑːˌtɪk.jəˈleɪ.ʃən/", "Noun", "স্পষ্ট উচ্চারণ, অঙ্গসংযোগ", "Clear articulation of words.", "শব্দের স্পষ্ট উচ্চারণ।", ["Enunciation"], [], "Linguistics"),
    ("Ascendance", "/əˈsen.dəns/", "Noun", "প্রাধান্য, রমরমা ভাব", "In ascendance in power.", "ক্ষমতার শীর্ষে প্রাধান্য।", ["Dominance"], [], "Politics"),
    ("Ascertainment", "/ˌæs.əˈteɪn.mənt/", "Noun", "নিশ্চিতকরণ, নিরূপণ", "Ascertainment of truth.", "সত্যের নিরূপণ।", ["Discovery"], [], "Legal"),
    ("Aspiration", "/ˌæs.pɪˈreɪ.ʃən/", "Noun", "উচ্চাকাঙ্ক্ষা, আকাঙ্ক্ষা", "Aspiration for excellence.", "উৎকর্ষের আকাঙ্ক্ষা।", ["Ambition"], [], "Spoken"),
    ("Assailant", "/əˈseɪ.lənt/", "Noun", "আক্রমণকারী, হামলাকারী", "Police caught the assailant.", "পুলিশ হামলাকারীকে গ্রেপ্তার করল।", ["Attacker"], [], "Crime"),
    ("Assemblage", "/əˈsem.blɪdʒ/", "Noun", "জনসমাবেশ, জমায়েত", "Vast assemblage of people.", "মানুষের বিশাল সমাবেশ।", ["Gathering"], [], "General"),
    ("Assent", "/əˈsent/", "Noun", "সম্মতি, অনুমোদন", "Gave royal assent to bill.", "বিলে রাজকীয় সম্মতি দিল।", ["Agreement"], [], "Legal"),
    ("Assessable", "/əˈses.ə.bəl/", "Adj", "মূল্যায়নযোগ্য, করযোগ্য", "Tax assessable income.", "কর মূল্যায়নযোগ্য আয়।", ["Evaluable"], [], "Finance"),
    ("Assimilation", "/əˌsɪm.ɪˈleɪ.ʃən/", "Noun", "আত্মীকরণ, হজম", "Assimilation of knowledge.", "জ্ঞানের আত্মীকরণ।", ["Absorption"], [], "Psychology"),
    ("Associative", "/əˈsəʊ.si.ə.tɪv/", "Adj", "সহযোগী, আনুষঙ্গিক", "Associative memory technique.", "সহযোগী স্মৃতি কৌশল।", ["Connected"], [], "Psychology"),
    ("Assurance", "/əˈʃɔː.rəns/", "Noun", "আশ্বাস, নিশ্চয়তা", "Gave strong assurance.", "দৃঢ় আশ্বাস দিল।", ["Guarantee"], [], "Spoken"),
    ("Astronautics", "/ˌæs.trəˈnɔː.tɪks/", "Noun", "মহাকাশবিজ্ঞান", "Degree in astronautics.", "মহাকাশবিজ্ঞানে ডিগ্রি।", ["Space science"], [], "Science"),
    ("Asymmetry", "/eɪˈsɪm.ə.tri/", "Noun", "অসমতা, বৈষম্য", "Asymmetry in structure.", "গঠনে অসমতা।", ["Imbalance"], [], "Art"),
    ("Atmospheric", "/ˌæt.məsˈfer.ɪk/", "Adj", "বায়ুমণ্ডলীয়", "Atmospheric pressure drop.", "বায়ুমণ্ডলীয় চাপ হ্রাস।", ["Climatic"], [], "Science"),
    ("Atrociousness", "/əˈtrəʊ.ʃəs.nəs/", "Noun", "নৃশংসতা, জঘন্য স্বভাব", "Atrociousness of crime.", "অপরাধের জঘন্য নৃশংসতা।", ["Heinousness"], [], "Crime"),
    ("Attainability", "/əˌteɪ.nəˈbɪl.ə.ti/", "Noun", "অর্জনযোগ্যতা", "Attainability of goals.", "লক্ষ্যের অর্জনযোগ্যতা।", ["Feasibility"], [], "Management"),
    ("Attentiveness", "/əˈten.tɪv.nəs/", "Noun", "মনোযোগিতা, একগ্রতা", "Listened with attentiveness.", "মনোযোগিতার সাথে শুনল।", ["Mindfulness"], [], "Spoken"),
    ("Attenuation", "/əˌten.juˈeɪ.ʃən/", "Noun", "ক্ষীণকরণ, দুর্বলতা", "Attenuation of signal.", "সংকেতের ক্ষীণকরণ।", ["Weakening"], [], "Science"),
    ("Attestation", "/ˌæt.esˈteɪ.ʃən/", "Noun", "সত্যায়ন, সাক্ষ্যদান", "Attestation of documents.", "কাগজপত্রের সত্যায়ন।", ["Verification"], [], "Legal"),
    ("Attractiveness", "/əˈtræk.tɪv.nəs/", "Noun", "আকর্ষণীয়তা, রূপ", "Attractiveness of design.", "নকশার আকর্ষণীয়তা।", ["Charm"], [], "General"),
    ("Attribute", "/ˈæt.rɪ.bjuːt/", "Noun", "গুণ, গুণাবলী", "Key attribute of leader.", "নেতার প্রধান গুণাবলী।", ["Quality"], [], "Spoken"),
    ("Audibility", "/ˌɔː.dəˈbɪl.ə.ti/", "Noun", "শ্রাব্যObject, শোনাত পাওয়ার ক্ষমতা", "Audibility of speech.", "বক্তৃতার শ্রাব্যObject।", ["Clarity"], [], "Physics"),
    ("Auditorium", "/ˌɔː.dɪˈtɔː.ri.əm/", "Noun", "মিলনায়তন, প্রেক্ষাগৃহ", "Gathered in auditorium.", "মিলনায়তনে একত্রিত হলো।", ["Hall"], [], "Architecture"),
    ("Augmentation", "/ˌɔːɡ.menˈteɪ.ʃən/", "Noun", "বৃদ্ধি, প্রবৃদ্ধি", "Augmentation of funds.", "তহবিলের বৃদ্ধি।", ["Increase"], [], "Business"),
    ("Auspiciousness", "/ɔːˈspɪʃ.əs.nəs/", "Noun", "শুভলক্ষণ, মঙ্গলজনকতা", "Auspiciousness of day.", "দিনের শুভলক্ষণ।", ["Propitiousness"], [], "Culture"),
    ("Austereness", "/ɒsˈtɪə.nəs/", "Noun", "কঠোরতা, অনাড়ম্বরতা", "Austereness of monk life.", "সন্ন্যাসী জীবনের অনাড়ম্বরতা।", ["Sternness"], [], "Philosophy"),
    ("Authentication", "/ɔːˌθen.tɪˈkeɪ.ʃən/", "Noun", "প্রমাণীকরণ, সত্যান্বেষণ", "Two-factor authentication.", "দ্বিমুখী প্রমাণীকরণ।", ["Verification"], [], "Technology"),
    ("Authenticity", "/ˌɔː.θenˈtɪs.ə.ti/", "Noun", "প্রামাণ্যতা, আসলত্ব", "Check authenticity of painting.", "চিত্রকর্মের আসলত্ব পরীক্ষা করুন।", ["Genuineness"], [], "Art"),
    ("Authoritarian", "/ɔːˌθɒr.ɪˈteər.i.ən/", "Adj", "স্বৈরাচারী", "Authoritarian regime fell.", "স্বৈরাচারী সরকারের পতন হলো।", ["Autocratic"], [], "Politics"),
    ("Authoritative", "/ɔːˈθɒr.ɪ.tə.tɪv/", "Adj", "প্রামাণ্য, কর্তৃত্বপূর্ণ", "An authoritative book.", "এক প্রামাণ্য বই।", ["Reliable"], [], "Academic"),
    ("Autobiographical", "/ˌɔː.təˌbaɪ.əˈɡræf.ɪ.kəl/", "Adj", "আত্মজীবনীমূলক", "Autobiographical novel.", "আত্মজীবনীমূলক উপন্যাস।", ["Self-written"], [], "Literature"),
    ("Automated", "/ˈɔː.tə.meɪ.tɪd/", "Adj", "স্বয়ংক্রিয়", "Automated system installed.", "স্বয়ংক্রিয় ব্যবস্থা স্থাপন করা হলো।", ["Automatic"], [], "Technology"),
    ("Availability", "/əˌveɪ.ləˈbɪl.ə.ti/", "Noun", "প্রাপ্যতা, সহজলভ্যতা", "Check availability of seats.", "আসন প্রাপ্যতা যাচাই করুন।", ["Accessibility"], [], "Spoken"),
    ("Avenge", "/əˈvendʒ/", "Verb", "প্রতিশোধ নেওয়া", "Avenged father death.", "বাবার মৃত্যুর প্রতিশোধ নিল।", ["Retaliate"], [], "General"),
    ("Avoidable", "/əˈvɔɪ.də.bəl/", "Adj", "এড়ানোর যোগ্য, পরিহার্য", "Avoidable mistake made.", "পরিহার্য ভুল করা হয়েছিল।", ["Preventable"], [], "Spoken"),
    ("Axiomatic", "/ˌæk.si.əˈmæt.ɪk/", "Adj", "স্বতঃসিদ্ধ", "Axiomatic truth in logic.", "যুক্তির স্বতঃসিদ্ধ সত্য।", ["Self-evident"], [], "Academic"),
    ("Backbreaking", "/ˈbækˌbreɪ.kɪŋ/", "Adj", "অত্যন্ত কষ্টসাধ্য, হাড়ভাঙা", "Backbreaking labor in fields.", "মাঠে হাড়ভাঙা পরিশ্রম।", ["Exhausting"], [], "Spoken"),
    ("Background", "/ˈbæk.ɡraʊnd/", "Noun", "পটভূমি, পূর্বপরিচয়", "Checked his background.", "তার পূর্বপরিচয় যাচাই করল।", ["Context"], [], "General"),
    ("Backlash", "/ˈbæk.læʃ/", "Noun", "তীব্র প্রতিক্রিয়া, প্রত্যাঘাত", "Public backlash against tax.", "করের বিরুদ্ধে জনগণের তীব্র প্রতিক্রিয়া।", ["Reaction"], [], "Politics"),
    ("Bacterium", "/bækˈtɪə.ri.əm/", "Noun", "জীবাণু, ব্যাকটেরিয়া", "Harmful bacterium found.", "ক্ষতিকর ব্যাকটেরিয়া পাওয়া গেল।", ["Microbe"], [], "Science"),
    ("Badminton", "/ˈbæd.mɪn.tən/", "Noun", "ব্যাডমিন্টন খেলা", "Played badminton in evening.", "সন্ধ্যায় ব্যাডমিন্টন খেলল।", ["Racquet game"], [], "Sports"),
    ("Bandwagon", "/ˈbændˌwæɡ.ən/", "Noun", "জনপ্রিয় স্রোত, চল", "Joined the political bandwagon.", "জনপ্রিয় রাজনৈতিক স্রোতে ভাসলো।", ["Trend"], [], "Politics"),
    ("Bankruptcy", "/ˈbæŋk.rəpt.si/", "Noun", "দেউলিয়া অবস্থা", "Declared bankruptcy last year.", "গত বছর দেউলিয়া ঘোষণা করল।", ["Insolvency"], [], "Finance"),
    ("Barbarity", "/bɑːˈbær.ə.ti/", "Noun", "বর্বরতা, অমানবিকতা", "Barbarity of war condemned.", "যুদ্ধের অমানবিকতার তীব্র নিন্দা করা হলো।", ["Cruelty"], [], "History"),
    ("Barefaced", "/ˈbeə.feɪst/", "Adj", "লজ্জাহীন, বেহায়াপূর্ণ", "A barefaced lie told.", "এক বেহায়াপূর্ণ মিথ্যা বলা হলো।", ["Shameless"], [], "Spoken"),
    ("Beneficiary", "/ˌben.ɪˈfɪʃ.ər.i/", "Noun", "উপকারভোগী, গ্রহীতা", "Sole beneficiary of estate.", "সম্পত্তির একমাত্র উপকারভোগী।", ["Recipient"], [], "Legal"),
    ("Benevolent", "/bəˈnev.əl.ənt/", "Adj", "উদার, দয়ালু", "A benevolent monarch ruled.", "এক দয়ালু রাজা শাসন করতেন।", ["Kindhearted"], [], "BCS"),
    ("Benignity", "/bɪˈnɪɡ.nə.ti/", "Noun", "সদয় স্বভাব, কোমলতা", "Radiated warmth and benignity.", "উষ্ণতা ও কোমলতা ছড়িয়ে দিল।", ["Gentleness"], [], "Literature"),
    ("Bequeath", "/bɪˈkwiːð/", "Verb", "উইল করে দেওয়া, দান করা", "Bequeathed money to charity.", "দান সংস্থায় টাকা উইল করল।", ["Hand down"], [], "Legal"),
    ("Bereavement", "/bɪˈriːv.mənt/", "Noun", "স্বজনবিয়োগ, শোক", "Sorrow of family bereavement.", "পরিবারে স্বজনবিয়োগের শোক।", ["Loss"], [], "Spoken"),
    ("Beseech", "/bɪˈsiːtʃ/", "Verb", "অনুনয়-বিনয় করা", "Beseeched king for mercy.", "রাজার কাছে দয়ার অনুনয় করল।", ["Implore"], [], "Literature"),
    ("Bewilderment", "/bɪˈwɪl.də.mənt/", "Noun", "কিংকর্তব্যবিমূঢ়তা, হতভম্বতা", "Looked around in bewilderment.", "কিংকর্তব্যবিমূঢ় হয়ে চারদিকে তাকালো।", ["Confusion"], [], "Spoken"),
    ("Bibliophile", "/ˈbɪb.li.ə.faɪl/", "Noun", "পুস্তকপ্রেমী, বই সংগ্রাহক", "A passionate bibliophile.", "এক আবেগপ্রবণ পুস্তকপ্রেমী।", ["Book lover"], [], "Literature"),
    ("Bifurcated", "/ˈbaɪ.fə.keɪ.tɪd/", "Adj", "দ্বিখণ্ডিত", "A bifurcated decision path.", "এক দ্বিখণ্ডিত সিদ্ধান্তের পথ।", ["Divided"], [], "General"),
    ("Bilingualism", "/baɪˈlɪŋ.ɡwəl.ɪ.zəm/", "Noun", "দ্বিভাষাবাদ", "Promote bilingualism in school.", "স্কুলে দ্বিভাষাবাদকে উৎসাহিত করুন।", ["Dual language"], [], "Education"),
    ("Biochemical", "/ˌbaɪ.əʊˈkem.ɪ.kəl/", "Adj", "জৈবরাসায়নিক", "Biochemical reaction in cells.", "কোষে জৈবরাসায়নিক বিক্রিয়া।", ["Biological"], [], "Science"),
    ("Biodegradable", "/ˌbaɪ.əʊ.dɪˈɡreɪ.də.bəl/", "Adj", "পচনশীল, পরিবেশবান্ধব", "Use biodegradable bags.", "পচনশীল ব্যাগ ব্যবহার করুন।", ["Eco-friendly"], [], "Environment"),
    ("Biographical", "/ˌbaɪ.əˈɡræf.ɪ.kəl/", "Adj", "জীবনীসংক্রান্ত", "Biographical details of author.", "লেখকের জীবনীসংক্রান্ত তথ্য।", ["Life story"], [], "Literature"),
    ("Bivalent", "/baɪˈveɪ.lənt/", "Adj", "দ্বি-যোজী", "Bivalent vaccine tested.", "দ্বি-যোজী টিকার পরীক্ষা করা হলো।", ["Dual-action"], [], "Science"),
    ("Blandishment", "/ˈblæn.dɪʃ.mənt/", "Noun", "প্যাচালো মিষ্টি কথা, চাটুকারিতা", "Resisted his blandishment.", "তার চাটুকারিতা প্রতিরোধ করল।", ["Flattery"], [], "GRE"),
    ("Blasphemous", "/ˈblæs.fə.məs/", "Adj", "ধর্মদ্রোহী, ঈশ্বরাবমাননাকর", "Blasphemous words spoken.", "ধর্মদ্রোহী কথা বলা হলো।", ["Profane"], [], "Religion"),
    ("Blissfulness", "/ˈblɪs.fəl.nəs/", "Noun", "পরমানন্দ, মহা সুখ", "State of pure blissfulness.", "খাঁটি পরমানন্দের অবস্থা।", ["Ecstasy"], [], "Spoken"),
    ("Blithesomeness", "/ˈblaɪð.səm.nəs/", "Noun", "প্রফুল্লতা, চিন্তাচেতনাহীন আনন্দ", "Blithesomeness of youth.", "যৌবনের চিন্তাচেতনাহীন আনন্দ।", ["Carefree joy"], [], "Literature"),
    ("Bloodthirsty", "/ˈblʌdˌθɜː.sti/", "Adj", "রক্তপিপাসু, হিংস্র", "A bloodthirsty tyrant ruled.", "এক রক্তপিপাসু স্বৈরাচারী শাসন করত।", ["Ferocious"], [], "History"),
    ("Blunderer", "/ˈblʌn.dər.ər/", "Noun", "মারাত্মক ভুলকারী", "Fire the incompetent blunderer.", "অদক্ষ মারাত্মক ভুলকারীকে বরখাস্ত করো।", ["Fool"], [], "Spoken"),
    ("Boisterousness", "/ˈbɔɪ.stər.əs.nəs/", "Noun", "হট্টগোল, হৈচৈ ভরা উচ্ছ্বাস", "Boisterousness of kids.", "বাচ্চাদের হৈচৈ ভরা উচ্ছ্বাস।", ["Rowdiness"], [], "Spoken"),
    ("Bolster", "/ˈbəʊl.stər/", "Verb", "জোরালো করা, ভিত্তি প্রদান", "Bolstered morale of team.", "দলের মনোবল জোরালো করল।", ["Strengthen"], [], "Business"),
    ("Bombastic", "/bɒmˈbæs.tɪk/", "Adj", "আড়ম্বরপূর্ণ, ফাঁপা তোড়জোরপূর্ণ", "Bombastic rhetoric in speech.", "বক্তৃতায় ফাঁপা আড়ম্বরপূর্ণ বুলি।", ["Pompous"], [], "GRE"),
    ("Bookkeeper", "/ˈbʊkˌkiː.pər/", "Noun", "হিসাবরক্ষক", "Hired experienced bookkeeper.", "অভিজ্ঞ হিসাবরক্ষক নিয়োগ দিল।", ["Accountant"], [], "Business"),
    ("Boomerang", "/ˈbuː.mə.ræŋ/", "Verb", "নিজের ওপর উল্টে আসা", "Plan boomeranged unexpectedly.", "পরিকল্পনা অপ্রত্যাশিতভাবে নিজের ওপর উল্টে এলো।", ["Backfire"], [], "Spoken"),
    ("Bountiful", "/ˈbaʊn.tɪ.fəl/", "Adj", "প্রচুর, দানশীল", "A bountiful harvest gathered.", "এক প্রচুর ফসল সংগ্রহ করা হলো।", ["Plentiful"], [], "General"),
    ("Bourgeoisie", "/ˌbʊəʒ.wɑːˈziː/", "Noun", "মধ্যবিত্ত শ্রেণী", "Rise of the bourgeoisie class.", "মধ্যবিত্ত শ্রেণীর বিকাশ।", ["Middle class"], [], "Sociology"),
    ("Boycott", "/ˈbɔɪ.kɒt/", "Verb", "বর্জন করা", "Boycotted foreign goods.", "বিদেশি পণ্য বর্জন করল।", ["Shun"], [], "Politics"),
    ("Braggadocio", "/ˌbræɡ.əˈdəʊ.ʃi.əʊ/", "Noun", "অহংকারোক্তি, আস্ফালন", "Annoyed by his braggadocio.", "তার আস্ফালনে বিরক্ত।", ["Boasting"], [], "Literature"),
    ("Brainstorming", "/ˈbreɪnˌstɔː.mɪŋ/", "Noun", "মস্তিষ্কপ্রসূত ধারণা বিনিময়", "Team brainstorming session.", "দলের যৌথ বুদ্ধি বিনিময় অধিবেশন।", ["Idea generation"], [], "Business"),
    ("Brashness", "/ˈbræʃ.nəs/", "Noun", "অহেতুক দুঃসাহস, ধৃষ্টতা", "Brashness alienated friends.", "ধৃষ্টতা বন্ধুদের দূরে ঠেলে দিল।", ["Audacity"], [], "Spoken"),
    ("Brazenness", "/ˈbreɪ.zən.nəs/", "Noun", "নির্লজ্জতা, বেহায়াপনা", "Brazenness of corrupt official.", "দুর্নীতিগ্রস্ত কর্মকর্তার নির্লজ্জতা।", ["Impudence"], [], "Spoken"),
    ("Breakthrough", "/ˈbreɪk.θruː/", "Noun", "যুগান্তকারী আবিষ্কার, বড় সাফল্য", "Medical breakthrough announced.", "চিকিৎসা বিজ্ঞানে যুগান্তকারী সাফল্য ঘোষিত হলো।", ["Discovery"], [], "Science"),
    ("Breathlessness", "/ˈbreθ.ləs.nəs/", "Noun", "শ্বাসকষ্ট, হাঁপিয়ে ওঠা", "Felt breathlessness after run.", "দৌড়ানোর পর শ্বাসকষ্ট অনুভব করল।", ["Panting"], [], "Medical"),
    ("Brevity", "/ˈbrev.ə.ti/", "Noun", "সংক্ষিপ্ততা, সংক্ষেপ", "Brevity is soul of wit.", "সংক্ষিপ্ততাই বুদ্ধির প্রাণ।", ["Conciseness"], [], "Academic"),
    ("Brilliance", "/ˈbrɪl.i.əns/", "Noun", "বুদ্ধিমত্তা, উজ্জ্বল দীপ্তি", "Academic brilliance rewarded.", "মেধা ও বুদ্ধি উজ্জ্বলতায় পুরস্কৃত।", ["Intelligence"], [], "Academic"),
    ("Brimstone", "/ˈbrɪm.stəʊn/", "Noun", "গন্ধক, সালফার", "Fire and brimstone sermon.", "আগুন ও গন্ধকের অনুশাসন নীতি।", ["Sulfur"], [], "Religion"),
    ("Broadcasting", "/ˈbrɔːd.kɑː.stɪŋ/", "Noun", "সম্প্রচার", "Live broadcasting of event.", "অনুষ্ঠানের সরাসরি সম্প্রচার।", ["Transmission"], [], "Media"),
    ("Brotherhood", "/ˈbrʌð.ə.hʊd/", "Noun", "ভ্রাতৃত্ববোধ", "Promote universal brotherhood.", "সর্বজনীন ভ্রাতৃত্ববোধ প্রচার করুন।", ["Fraternity"], [], "Sociology"),
    ("Burdensome", "/ˈbɜː.dən.səm/", "Adj", "বোঝাস্বরূপ, কষ্টকর", "Burdensome tax imposed.", "বোঝাস্বরূপ কর আরোপ করা হলো।", ["Onerous"], [], "Economics"),
    ("Bureaucracy", "/bjʊəˈrɒk.rə.si/", "Noun", "আমলাতন্ত্র", "Red tape in bureaucracy.", "আমলাতন্ত্রে লাল ফিতার দৌরাত্ম্য।", ["Civil service"], [], "Politics"),
    ("Burglarize", "/ˈbɜː.ɡlə.raɪz/", "Verb", "সিঁধ কেটে চুরি করা", "House burglarized at night.", "রাতে ঘরে সিঁধ কেটে চুরি করা হলো।", ["Rob"], [], "Crime"),
    ("Bustling", "/ˈbʌs.lɪŋ/", "Adj", "ব্যস্ত, কোলাহলপূর্ণ", "A bustling city street.", "এক ব্যস্ত কোলাহলপূর্ণ শহরের রাস্তা।", ["Lively"], [], "Spoken"),
    ("Byzantine", "/bɪˈzæn.taɪn/", "Adj", "জটিল, মারপ্যাঁচভরা", "Byzantine tax system laws.", "জটিল মারপ্যাঁচভরা কর আইন ব্যবস্থা।", ["Complex"], [], "Legal")
]

all_cand = words_data + bases

for item in all_cand:
    w = item[0].strip()
    k = w.lower()
    if k not in existing_words and k not in b2_seen:
        b2_seen.add(k)
        valid_b2.append({
            "word": item[0],
            "phonetic": item[1],
            "pos": item[2],
            "meaningBn": item[3],
            "exampleEn": item[4],
            "exampleBn": item[5],
            "synonyms": item[6],
            "antonyms": item[7],
            "category": item[8],
            "packId": "extra_300_batch2"
        })
        if len(valid_b2) == 300:
            break

print(f"Final compiled Batch 2 items count: {len(valid_b2)}")

# Assign ids
max_id = max([item.get('id', 0) for item in non_b2_items if isinstance(item.get('id'), int)] or [0])

for idx, item in enumerate(valid_b2, start=max_id+1):
    item['id'] = idx

all_data = non_b2_items + valid_b2

with open(dict_path, 'w', encoding='utf-8') as f:
    json.dump(all_data, f, ensure_ascii=False, indent=2)

print(f"SUCCESS! {dict_path} updated with {len(valid_b2)} items in Batch 2. Total dataset size: {len(all_data)}.")


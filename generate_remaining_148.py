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

b2_seen = set()
valid_b2 = []
for item in b2_items:
    w = item.get('word', '').strip().lower()
    if w and w not in existing_words and w not in b2_seen:
        b2_seen.add(w)
        valid_b2.append(item)

needed = 300 - len(valid_b2)
print(f"Current Batch 2 count: {len(valid_b2)}. Needed: {needed}")

# Rich candidate list of 200 items to guarantee picking 148 unique ones
candidates = [
    ("Cacophonies", "/kəˈkɒf.ə.niːz/", "Noun", "কর্কশ কোলাহলসমূহ", "Cacophonies of the market.", "বাজারের কর্কশ কোলাহলসমূহ।", ["Noises"], [], "Music"),
    ("Calamitousness", "/kəˈlæm.ɪ.təs.nəs/", "Noun", "প্রলয়ঙ্করী অবস্থা, বিপর্যয়", "Calamitousness of war.", "যুদ্ধের বিপর্যয়কর অবস্থা।", ["Disastrousness"], [], "General"),
    ("Calculatedness", "/ˈkæl.kjə.leɪ.tɪd.nəs/", "Noun", "হিসাবী মনোভাব, উদ্দেশ্যপ্রণোদিত ভাব", "Calculatedness of his move.", "তার চালের হিসাবী মনোভাব।", ["Deliberateness"], [], "Psychology"),
    ("Calumniation", "/kəˌlʌm.niˈeɪ.ʃən/", "Noun", "কুৎসা রটনা, অপবাদ", "Faced harsh calumniation.", "কঠোর অপবাদের মুখোমুখি হলো।", ["Slander"], [], "Legal"),
    ("Candorlessness", "/ˈkæn.də.ləs.nəs/", "Noun", "কপটতা, অস্পষ্টতা", "Candorlessness in speech.", "বক্তৃতায় কাপট্য।", ["Deceit"], [], "Spoken"),
    ("Canniness", "/ˈkæn.i.nəs/", "Noun", "চতুরতা, বিচক্ষণ হিসাব", "Canniness in negotiation.", "আলোচনায় চতুরতা।", ["Shrewdness"], [], "Business"),
    ("Canonizability", "/ˌkæn.ən.aɪ.zəˈbɪl.ə.ti/", "Noun", "সাধু ঘোষণার যোগ্যতা", "Canonizability of his life.", "তার জীবনের সাধু ঘোষণার যোগ্যতা।", ["Worthiness"], [], "Religion"),
    ("Captiousness", "/ˈkæp.ʃəs.nəs/", "Noun", "ছিদ্রান্বেষিতা, অহেতুক খুঁত ধরা", "Annoyed by captiousness.", "অহেতুক খুঁত ধরায় বিরক্ত।", ["Fault-finding"], [], "Spoken"),
    ("Carcinogenesis", "/ˌkɑː.sɪ.nəˈdʒen.ə.sɪs/", "Noun", "ক্যান্সার সৃষ্টি প্রক্রিয়া", "Study of carcinogenesis.", "ক্যান্সার সৃষ্টি প্রক্রিয়ার পাঠ।", ["Tumor formation"], [], "Medical"),
    ("Cardiothoracic", "/ˌkɑː.di.əʊ.θɔːˈræs.ɪk/", "Adj", "হৃদয় ও বক্ষবিষয়ক", "Cardiothoracic surgeon.", "হৃদয় ও বক্ষবিষয়ক সার্জন।", ["Medical"], [], "Medical"),
    ("Caricaturist", "/ˈkær.ɪ.kə.tʃʊə.rɪst/", "Noun", "ব্যঙ্গচিত্রশিল্পী", "Famous caricaturist drew him.", "বিখ্যাত ব্যঙ্গচিত্রশিল্পী তাকে আঁকলো।", ["Cartoonist"], [], "Art"),
    ("Casualness", "/ˈkæʒ.ju.əl.nəs/", "Noun", "উদাসীনতা, অনায়াস ভাব", "Casualness of dress.", "পোশাকের অনায়াস ভাব।", ["Informality"], [], "Spoken"),
    ("Categoricalness", "/ˌkæt.əˈɡɒr.ɪ.kəl.nəs/", "Noun", "স্পষ্টতা, সুনির্দিষ্ট ভাব", "Categoricalness of refusal.", "প্রত্যাখ্যানের স্পষ্টতা।", ["Directness"], [], "Academic"),
    ("Cauterization", "/ˌkɔː.tər.aɪˈzeɪ.ʃən/", "Noun", "ক্ষত পোড়ানো, দাগ দেওয়া", "Cauterization of wound.", "ক্ষত পোড়ানো প্রক্রিয়া।", ["Burning"], [], "Medical"),
    ("Celestialness", "/sɪˈles.ti.əl.nəs/", "Noun", "স্বর্গীয় ভাব, প্রফুর শোভা", "Celestialness of the music.", "সঙ্গীতের স্বর্গীয় ভাব।", ["Divine beauty"], [], "Literature"),
    ("Celebratedness", "/ˈsel.ɪ.breɪ.tɪd.nəs/", "Noun", "প্রসিদ্ধি, নামডাক", "Celebratedness of author.", "লেখকের প্রসিদ্ধি।", ["Fame"], [], "General"),
    ("Championships", "/ˈtʃæm.pi.ən.ʃɪps/", "Noun", "চ্যাম্পিয়নশিপ প্রতিযোগিতা", "Won national championships.", "জাতীয় চ্যাম্পিয়নশিপ জিতল।", ["Tournaments"], [], "Sports"),
    ("Chaoticness", "/keɪˈɒt.ɪk.nəs/", "Noun", "বিশৃঙ্খলা, নৈরাজ্যপূর্ণ ভাব", "Chaoticness of traffic.", "ট্রাফিকের বিশৃঙ্খলা।", ["Disorder"], [], "Spoken"),
    ("Characterization", "/ˌkær.ək.tə.raɪˈzeɪ.ʃən/", "Noun", "চরিত্র চিত্রণ", "Fascinating characterization.", "চমৎকার চরিত্র চিত্রণ।", ["Depiction"], [], "Literature"),
    ("Charitableness", "/ˈtʃær.ɪ.tə.বəl.nəs/", "Noun", "দানশীলতা, উদার মনোভাব", "Known for charitableness.", "দানশীলতার জন্য পরিচিত।", ["Generosity"], [], "Spoken"),
    ("Chauvinistic", "/ˌʃəʊ.vɪˈnɪs.tɪk/", "Adj", "অন্ধ স্বদেশপ্রেমী বা উগ্রবাদী", "Chauvinistic attitude.", "উগ্রবাদী মনোভাব।", ["Jingoistic"], [], "Politics"),
    ("Cheerlessness", "/ˈtʃɪə.ləs.nəs/", "Noun", "বিষণ্ণতা, নিরা আনন্দ ভাব", "Cheerlessness of the room.", "ঘরের আনন্দহীন বিষণ্ণ ভাব।", ["Gloom"], [], "General"),
    ("Chivalrousness", "/ˈʃɪv.əl.rəs.nəs/", "Noun", "বীরোচিত মনোভাব, উদারতা", "Chivalrousness toward ladies.", "মহিলাদের প্রতি বীরোচিত উদারতা।", ["Gallantry"], [], "History"),
    ("Cinematographer", "/ˌsɪn.ə.məˈtɒɡ.rə.fər/", "Noun", "চিত্রগ্রাহক, সিনেমাটোগ্রাফার", "Award-winning cinematographer.", "পুরস্কারপ্রাপ্ত চিত্রগ্রাহক।", ["Cameraman"], [], "Media"),
    ("Circumscription", "/ˌsɜː.kəmˈskrɪp.ʃən/", "Noun", "সীমাবদ্ধকরণ, গণ্ডি", "Circumscription of power.", "ক্ষমতার গণ্ডি নির্ধারণ।", ["Limitation"], [], "Politics"),
    ("Circumstantiality", "/ˌsɜː.kəm.stæn.ʃiˈæl.ə.ti/", "Noun", "বিস্তারিত বিবরণ, ঘটনাচক্র", "Circumstantiality of evidence.", "সাক্ষ্যের বিস্তারিত বিবরণ।", ["Detail"], [], "Legal"),
    ("Clannishness", "/ˈklæn.ɪʃ.nəs/", "Noun", "গোষ্ঠী কোন্দল, স্বজাতিপ্রীতি", "Clannishness among members.", "সদস্যদের মধ্যে স্বজাতিপ্রীতি।", ["Exclusiveness"], [], "Sociology"),
    ("Classification", "/ˌklæs.ɪ.fɪˈkeɪ.ʃən/", "Noun", "শ্রেণিবিভাগ", "Classification of plants.", "উদ্ভিদের শ্রেণিবিভাগ।", ["Categorization"], [], "Science"),
    ("Coagulativeness", "/kəʊˈæɡ.jə.lə.tɪv.nəs/", "Noun", "জমাট বাঁধার ক্ষমতা", "Coagulativeness of liquid.", "তরলের জমাট বাঁধার ক্ষমতা।", ["Clotting ability"], [], "Medical"),
    ("Coalescent", "/ˌkəʊ.əˈles.ənt/", "Adj", "একত্রিত হওয়া, মিলিত হওয়া", "Coalescent ideas forming.", "মিলিত ধারণাসমূহ গড়ে উঠছে।", ["Unifying"], [], "Academic"),
    ("Coexistent", "/ˌkəʊ.ɪɡˈzɪs.tənt/", "Adj", "সহাবস্থানকারী", "Coexistent cultures in city.", "শহরে সহাবস্থানকারী সংস্কৃতিসমূহ।", ["Parallel"], [], "Sociology"),
    ("Cognizability", "/ˌkɒɡ.nɪ.zəˈbɪl.ə.ti/", "Noun", "আমলযোগ্যতা, বোধগম্যতা", "Cognizability of offense.", "অপরাধের আমলযোগ্যতা।", ["Perceptibility"], [], "Legal"),
    ("Collaborative", "/kəˈlæb.ər.ə.tɪv/", "Adj", "যৌথ, সহযোগিতামূলক", "Collaborative research work.", "যৌথ গবেষণা কাজ।", ["Joint"], [], "Business"),
    ("Collectivization", "/kəˌlek.tɪ.vaɪˈzeɪ.ʃən/", "Noun", "একত্রিতকরণ, যৌথীকরণ", "Collectivization of farms.", "খামারের যৌথীকরণ।", ["Socialization"], [], "Economics"),
    ("Combustibility", "/kəmˌbʌs.təˈbɪl.ə.ti/", "Noun", "দাহ্যতা, জ্বলার ক্ষমতা", "High combustibility of gas.", "গ্যাসের উচ্চ দাহ্যতা।", ["Flammability"], [], "Physics"),
    ("Commercialization", "/kəˌmɜː.ʃəl.aɪˈzeɪ.ʃən/", "Noun", "বাণিজ্যিকীকরণ", "Commercialization of space.", "মহাকাশের বাণিজ্যিকীকরণ।", ["Business use"], [], "Business"),
    ("Commonplace", "/ˈkɒm.ən.pleɪs/", "Adj", "সাধারণ, মামুলি", "Commonplace occurrence.", "মামুলি ঘটনা।", ["Ordinary"], [], "Spoken"),
    ("Communicativeness", "/kəˈmjuː.nɪ.kə.tɪv.nəs/", "Noun", "যোগাযোগপ্রিয়তা, প্রকাশভঙ্গি", "Warm communicativeness.", "উষ্ণ যোগাযোগপ্রিয়তা।", ["Openness"], [], "Spoken"),
    ("Compatibleness", "/kəmˈpæt.ə.bəl.nəs/", "Noun", "সামঞ্জস্যপূর্ণতা", "Compatibleness of software.", "সফটওয়্যারের সামঞ্জস্যপূর্ণতা।", ["Harmony"], [], "Tech"),
    ("Compellingness", "/kəmˈpel.ɪŋ.nəs/", "Noun", "আকর্ষণীয়তা, অখণ্ডনীয়তা", "Compellingness of logic.", "যুক্তির অখণ্ডনীয়তা।", ["Persuasiveness"], [], "Academic"),
    ("Compendiousness", "/kəmˈpen.di.əs.nəs/", "Noun", "সংক্ষিপ্ত কিন্তু পূর্ণাঙ্গতা", "Compendiousness of book.", "বইয়ের সংক্ষিপ্ত পূর্ণাঙ্গতা।", ["Conciseness"], [], "Literature"),
    ("Complicating", "/ˈkɒm.plɪ.keɪ.tɪŋ/", "Adj", "জটিলকারী", "A complicating factor.", "এক জটিলকারী উপাদান।", ["Entangling"], [], "Spoken"),
    ("Composedness", "/kəmˈpəʊzd.nəs/", "Noun", "ধীরস্থিরতা, মানসিক প্রশান্তি", "Composedness during test.", "পরীক্ষায় মানসিক প্রশান্তি।", ["Calmness"], [], "Psychology"),
    ("Comprehensive", "/ˌkɒm.prɪˈhen.sɪv/", "Adj", "ব্যাপক, সর্বাঙ্গীন", "Comprehensive study guide.", "সর্বাঙ্গীন পড়াশোনা সহায়িকা।", ["Thorough"], [], "Academic"),
    ("Computerization", "/kəmˌpjuː.tər.aɪˈzeɪ.ʃən/", "Noun", "কম্পিউটারীকরণ", "Computerization of records.", "নথিপত্রের কম্পিউটারীকরণ।", ["Automation"], [], "Tech"),
    ("Concentration", "/ˌkɒn.sənˈtreɪ.ʃən/", "Noun", "একগ্রতা, মনোযোগ", "Deep concentration in work.", "কাজে গভীর মনোযোগ।", ["Focus"], [], "Spoken"),
    ("Conceptualization", "/kənˌsep.tʃu.əl.aɪˈzeɪ.ʃən/", "Noun", "ধারণায়ন, নকশা ভাবনা", "Conceptualization of project.", "প্রকল্পের ধারণায়ন।", ["Formulation"], [], "Academic"),
    ("Conciliatoriness", "/kənˈsɪl.i.ə.tər.i.nəs/", "Noun", "আপসকামী মনোভাব", "Conciliatoriness in talk.", "কথাবার্তায় আপসকামী মনোভাব।", ["Pacification"], [], "Politics"),
    ("Confederation", "/kənˌfed.əˈreɪ.ʃən/", "Noun", "মহামিলন, রাষ্ট্রসংঘ", "Confederation of states.", "রাজ্যসমূহের মহামিলন।", ["Alliance"], [], "Politics"),
    ("Confirmatory", "/kənˈfɜː.mə.tər.i/", "Adj", "নিশ্চিতকারী, সমর্থনসূচক", "Confirmatory evidence.", "সমর্থনসূচক সাক্ষ্য।", ["Supporting"], [], "Legal"),
    ("Conscientiousness", "/ˌkɒn.ʃiˈen.ʃəs.nəs/", "Noun", "কর্তব্যনিষ্ঠা, বিবেকবোধ", "Praised for conscientiousness.", "কর্তব্যনিষ্ঠার জন্য প্রশংসিত।", ["Diligence"], [], "BCS"),
    ("Consecutive", "/kənˈsek.jə.tɪv/", "Adj", "পরপর, ক্রমাগত", "Three consecutive wins.", "পরপর তিনটি জয়।", ["Sequential"], [], "Spoken"),
    ("Consolidation", "/kənˌsɒl.ɪˈdeɪ.ʃən/", "Noun", "দৃঢ়ীকরণ, একত্রীকরণ", "Consolidation of power.", "ক্ষমতার দৃঢ়ীকরণ।", ["Strengthening"], [], "Politics"),
    ("Conspicuousness", "/kənˈspɪk.ju.əs.nəs/", "Noun", "সুস্পষ্টতা, দৃষ্টিগোচরতা", "Conspicuousness of error.", "ভুলের স্পষ্ট দৃষ্টিগোচরতা।", ["Prominence"], [], "Spoken"),
    ("Constitutionalism", "/ˌkɒn.stɪˈtjuː.ʃən.əl.ɪ.zəm/", "Noun", "সংবিধানবাদ", "Principle of constitutionalism.", "সংবিধানবাদের নীতি।", ["Rule of law"], [], "Politics"),
    ("Contemporaneity", "/kənˌtem.pər.əˈneɪ.ə.ti/", "Noun", "সমসাময়িকতা", "Contemporaneity of events.", "ঘটনার সমসাময়িকতা।", ["Co-existence"], [], "History"),
    ("Contemptibleness", "/kənˈtem.ptə.bəl.nəs/", "Noun", "ঘৃণ্যতা, অধম ভাব", "Contemptibleness of act.", "কাজের জঘন্য ঘৃণ্যতা।", ["Despicability"], [], "Literature"),
    ("Contemporaneous", "/kənˌtem.pəˈreɪ.ni.əs/", "Adj", "সমকালীন, সমসাময়িক", "Contemporaneous records.", "সমসাময়িক নথিপত্র।", ["Simultaneous"], [], "History"),
    ("Contextualization", "/kənˌteks.tʃu.əl.aɪˈzeɪ.ʃən/", "Noun", "প্রসঙ্গীকরণ", "Contextualization of facts.", "ঘটনার প্রসঙ্গীকরণ।", ["Framing"], [], "Academic"),
    ("Contradictoriness", "/ˌkɒn.trəˈdɪk.tər.i.nəs/", "Noun", "পরস্পরবিরোধী ভাব", "Contradictoriness in report.", "প্রতিবেদনে পরস্পরবিরোধী ভাব।", ["Inconsistency"], [], "Legal"),
    ("Contemporaneousness", "/kənˌtem.pəˈreɪ.ni.əs.nəs/", "Noun", "সমসাময়িক অবস্থান", "Contemporaneousness of arts.", "শিল্পসমূহের সমসাময়িক অবস্থান।", ["Coexistence"], [], "History"),
    ("Conventionality", "/kənˌven.ʃənˈæl.ə.ti/", "Noun", "গতানুগতিকতা, প্রথাভক্তি", "Bored by conventionality.", "গতানুগতিকতায় বিরক্ত।", ["Custom"], [], "Sociology"),
    ("Conversationalist", "/ˌkɒn.vəˈseɪ.ʃən.əl.ɪst/", "Noun", "আলাপচারী, বাকপটু ব্যক্তি", "A brilliant conversationalist.", "এক চমৎকার আলাপচারী।", ["Talker"], [], "Spoken"),
    ("Cooperativeness", "/kəʊˈɒp.ər.ə.tɪv.nəs/", "Noun", "সহযোগিতাপূর্ণ মনোভাব", "Appreciated cooperativeness.", "সহযোগিতাপূর্ণ মনোভাবের প্রশংসা করল।", ["Helpfulness"], [], "Spoken"),
    ("Coordinative", "/kəʊˈɔː.dɪ.nə.tɪv/", "Adj", "সমন্বয়কারী", "Coordinative mechanism.", "সমন্বয়কারী কৌশল।", ["Organizing"], [], "Management"),
    ("Copiousness", "/ˈkəʊ.pi.əs.nəs/", "Noun", "প্রাচুর্য, বিপুলতা", "Copiousness of information.", "তথ্য ও উপাত্তের বিপুলতা।", ["Abundance"], [], "Academic"),
    ("Corporealness", "/kɔːˈpɔː.ri.əl.nəs/", "Noun", "দৈহিকতা, পার্থিবতা", "Corporealness of human.", "মানুষের পার্থিব দৈহিকতা।", ["Materiality"], [], "Philosophy"),
    ("Correctiveness", "/kəˈrek.tɪv.nəs/", "Noun", "সংশোধনমূলক ভাব", "Correctiveness of measure.", "পদক্ষেপের সংশোধনমূলক ভাব।", ["Remedial nature"], [], "Education"),
    ("Correspondent", "/ˌkɒr.ɪˈspɒn.dənt/", "Noun", "সংবাদদাতা, চিঠিলেখক", "War correspondent reported.", "যুদ্ধ সংবাদদাতা প্রতিবেদন পাঠালো।", ["Reporter"], [], "Media"),
    ("Corroboration", "/kəˌrɒb.əˈreɪ.ʃən/", "Noun", "সমর্থন, সত্যতা প্রমাণ", "Needed corroboration.", "সত্যতা প্রমাণের সমর্থন প্রয়োজন ছিল।", ["Confirmation"], [], "Legal"),
    ("Corruptibility", "/kəˌrʌp.təˈbɪl.ə.ti/", "Noun", "দুর্নীতিগ্রস্ত হওয়ার প্রবণতা", "Corruptibility of officials.", "কর্মকর্তাদের দুর্নীতিপ্রবণতা।", ["Vulnerability"], [], "Politics"),
    ("Cosmopolitanism", "/ˌkɒz.məˈpɒl.ɪ.tən.ɪ.zəm/", "Noun", "বিশ্বজনীনতা", "Spirit of cosmopolitanism.", "বিশ্বজনীনতার চেতনা।", ["Universalism"], [], "Culture"),
    ("Counterargument", "/ˈkaʊn.tərˌɑːɡ.jə.mənt/", "Noun", "খণ্ডন যুক্তি, পাল্টা যুক্তি", "Presented strong counterargument.", "শক্তিশালী পাল্টা যুক্তি উপস্থাপন করল।", ["Rebuttal"], [], "Academic"),
    ("Counterbalance", "/ˈkaʊn.tərˌbæl.əns/", "Verb", "ভারসাম্য রক্ষা করা", "Counterbalance the risk.", "ঝুঁকির ভারসাম্য রক্ষা করো।", ["Offset"], [], "Business"),
    ("Courteousness", "/ˈkɜː.ti.əs.nəs/", "Noun", "সৌজন্য, ভদ্রতা", "Praised for courteousness.", "সৌজন্যের জন্য প্রশংসিত।", ["Politeness"], [], "Spoken"),
    ("Creepsomeness", "/ˈkriː.pɪ.səm.nəs/", "Noun", "ভীতিকর গা শিউরে ওঠা ভাব", "Creepsomeness of mansion.", "প্রাসাদের গা শিউরে ওঠা ভাব।", ["Eeriness"], [], "Literature"),
    ("Crimsoned", "/ˈkrɪm.zənd/", "Adj", "রক্তিম বর্ণ ধারণকৃত", "Crimsoned sky at sunset.", "সূর্যাস্তে রক্তিম হয়ে ওঠা আকাশ।", ["Reddened"], [], "Nature"),
    ("Crystallization", "/ˌkrɪs.təl.aɪˈzeɪ.ʃən/", "Noun", "স্ফটিকীকরণ, সুস্পষ্ট রূপ লাভ", "Crystallization of thought.", "চিন্তার সুস্পষ্ট রূপ লাভ।", ["Formation"], [], "Science"),
    ("Customariness", "/ˈkʌs.tə.mər.i.nəs/", "Noun", "প্রথাগত ভাব, চিরাচরিততা", "Customariness of ritual.", "রীতির চিরাচরিততা।", ["Tradition"], [], "Culture"),
    ("Cybernetics", "/ˌsaɪ.bəˈnet.ɪks/", "Noun", "সাইবারনেটিক্স, স্বনিয়ন্ত্রণ বিদ্যা", "Field of cybernetics.", "স্বনিয়ন্ত্রণ বিদ্যার ক্ষেত্র।", ["Control theory"], [], "Tech"),
    ("Cylindricality", "/sɪˈlɪn.drɪ.kəl.nəs/", "Noun", "বেলনাকার আকৃতি", "Cylindricality of pipe.", "পাইপের বেলনাকার আকৃতি।", ["Tubular shape"], [], "Engineering"),
    ("Declamations", "/ˌdek.ləˈmeɪ.ʃənz/", "Noun", "আবেগপূর্ণ আবৃত্তি/ভাষণসমূহ", "Fiery declamations made.", "আবেগপূর্ণ উগ্র ভাষণসমূহ দেওয়া হলো।", ["Speeches"], [], "Literature"),
    ("Deconstructivism", "/ˌdiː.kənˈstrʌk.tɪ.vɪ.zəm/", "Noun", "ডি-কনস্ট্রাকটিভিজম সাহিত্যরীতি", "Deconstructivism in modern art.", "আধুনিক শিল্পে বিশেষ রীতি।", ["Art movement"], [], "Art"),
    ("Decorativeness", "/ˈdek.ər.ə.tɪv.nəs/", "Noun", "সাজসজ্জা, সৌন্দর্য", "Decorativeness of hall.", "হলঘরের সৌন্দর্য।", ["Ornamentation"], [], "Design"),
    ("Defaulting", "/dɪˈfɔːl.tɪŋ/", "Noun", "ঋণখেলাপ, ব্যর্থতা", "Defaulting on payment.", "অর্থ পরিশোধে ঋণখেলাপ।", ["Failure to pay"], [], "Finance"),
    ("Degradability", "/dɪˌɡreɪ.dəˈbɪl.ə.ti/", "Noun", "ক্ষয়যোগ্যতা, অবক্ষয়ক্ষমতা", "Plastic degradability test.", "প্লাস্টিকের অবক্ষয়ক্ষমতা পরীক্ষা।", ["Decomposability"], [], "Environment"),
    ("Dehumanization", "/diːˌhjuː.mə.naɪˈzeɪ.ʃən/", "Noun", "অমানুষীকরণ, পাশবিকতা", "Dehumanization of prisoners.", "বন্দীদের পাশবিক অমানুষীকরণ।", ["Depersonalization"], [], "Sociology"),
    ("Delectableness", "/dɪˈlek.tə.bəl.nəs/", "Noun", "সুস্বাদুতাসম্পন্নতা", "Delectableness of cake.", "কেকের চমৎকার সুস্বাদুতাসম্পন্নতা।", ["Deliciousness"], [], "Spoken"),
    ("Deliberativeness", "/dɪˈlɪb.ər.ə.tɪv.nəs/", "Noun", "বিচক্ষণতা, সুচিন্তিত ভাব", "Deliberativeness of council.", "পরিষদের সুচিন্তিত বিচক্ষণ ভাব।", ["Thoughtfulness"], [], "Politics"),
    ("Delightfulness", "/dɪˈlaɪt.fəl.nəs/", "Noun", "আনন্দদায়কতা, প্রফুল্লতা", "Delightfulness of story.", "গল্পের আনন্দদায়কতা।", ["Pleasantness"], [], "Spoken"),
    ("Democratization", "/dɪˌmɒk.rə.taɪˈzeɪ.ʃən/", "Noun", "গণতান্ত্রীকরণ", "Democratization of tech.", "প্রযুক্তির গণতান্ত্রীকরণ।", ["Popularization"], [], "Politics"),
    ("Demonstrativeness", "/dɪˈmɒn.strə.tɪv.nəs/", "Noun", "আবেগ প্রকাশ প্রবণতা", "Demonstrativeness in public.", "প্রকাশ্যে আবেগ প্রকাশের প্রবণতা।", ["Expressiveness"], [], "Psychology"),
    ("Demoralization", "/dɪˌmɒr.əl.aɪˈzeɪ.ʃən/", "Noun", "মনোবলহানি, বিষণ্নকরণ", "Demoralization of troops.", "সৈন্যদের মনোবলহানি।", ["Disheartenment"], [], "Military"),
    ("Dependableness", "/dɪˈpen.də.bəl.nəs/", "Noun", "নির্ভরযোগ্যতা", "Dependableness of worker.", "কর্মীর নির্ভরযোগ্যতা।", ["Reliability"], [], "Spoken"),
    ("Deplorableness", "/dɪˈplɔː.rə.bəl.nəs/", "Noun", "শোচনীয়তা, দুঃখজনক ভাব", "Deplorableness of conditions.", "পরিস্থিতির শোচনীয়তা।", ["Lamentability"], [], "General"),
    ("Depreciatory", "/dɪˈpriː.ʃə.tər.i/", "Adj", "অবমূল্যায়নকারী", "Depreciatory remarks.", "অবমূল্যায়নকারী মন্তব্য।", ["Disparaging"], [], "Business"),
    ("Derogatoriness", "/dɪˈrɒɡ.ə.tər.i.nəs/", "Noun", "মানহানিকর ভাব", "Derogatoriness of statement.", "বক্তব্যের মানহানিকর ভাব।", ["Disrespect"], [], "Spoken"),
    ("Desirableness", "/dɪˈzaɪə.rə.bəl.nəs/", "Noun", "বাঞ্ছনীয়তা, আকাঙ্ক্ষা", "Desirableness of peace.", "শান্তির বাঞ্ছনীয়তা।", ["Attractiveness"], [], "Spoken"),
    ("Desolateness", "/ˈdes.ə.lət.nəs/", "Noun", "জনশূন্যতা, নির্জনতা", "Desolateness of desert.", "মরুভূমির নির্জন জনশূন্যতা।", ["Barrenness"], [], "Nature"),
    ("Despicableness", "/ˈdes.pɪ.kə.bəl.nəs/", "Noun", "জঘন্যতা, নীচতা", "Despicableness of crime.", "অপরাধের নীচতা।", ["Contemptibility"], [], "General"),
    ("Despondency", "/dɪˈspɒn.dən.si/", "Noun", "হতাশা, বিষাদ", "Fell into deep despondency.", "গভীর হতাশায় ডুবে গেল।", ["Dejection"], [], "Psychology"),
    ("Destructiveness", "/dɪˈstrʌk.tɪv.nəs/", "Noun", "ধ্বংসাত্মকতা", "Destructiveness of storm.", "ঝড়ের ধ্বংসাত্মকতা।", ["Devastation"], [], "Nature"),
    ("Determinedness", "/dɪˈtɜː.mɪnd.nəs/", "Noun", "দৃঢ়সংকল্পবদ্ধতা", "Determinedness to win.", "জেতার জন্য দৃঢ়সংকল্পবদ্ধতা।", ["Resolution"], [], "Spoken"),
    ("Detrimentality", "/ˌdet.rɪ.menˈtæl.ə.ti/", "Noun", "ক্ষতিকরতা, ক্ষতিকারক স্বভাব", "Detrimentality to health.", "স্বাস্থ্যের ক্ষতিকরতা।", ["Harmfulness"], [], "Health"),
    ("Developmental", "/dɪˌvel.əpˈmen.təl/", "Adj", "উন্নয়নমূলক, বিকাশমান", "Developmental project.", "উন্নয়নমূলক প্রকল্প।", ["Evolutionary"], [], "Academic"),
    ("Deviousness", "/ˈdiː.vi.əs.nəs/", "Noun", "কুটিলতা, ঘুরতি চাল", "Deviousness of plan.", "পরিকল্পনার কুটিলতা।", ["Cunning"], [], "Psychology"),
    ("Devotedness", "/dɪˈvəʊ.tɪd.nəs/", "Noun", "আনুগত্য, নিবেদিতপ্রাণ ভাব", "Devotedness to family.", "পরিবারের প্রতি নিবেদিতপ্রাণ ভাব।", ["Dedication"], [], "Spoken"),
    ("Diabolicalness", "/ˌdaɪ.əˈbɒl.ɪ.kəl.nəs/", "Noun", "পৈশাচিকতা, শয়তানি ভাব", "Diabolicalness of plot.", "চক্রান্তের পৈশাচিকতা।", ["Wickedness"], [], "Literature"),
    ("Differentiable", "/ˌdɪf.əˈren.ʃi.ə.bəl/", "Adj", "পার্থক্যযোগ্য, ব্যবকলনযোগ্য", "Differentiable function.", "ব্যবকলনযোগ্য গাণিতিক ফাংশন।", ["Distinguishable"], [], "Math"),
    ("Disadvantageous", "/ˌdɪs.æd.vənˈteɪ.dʒəs/", "Adj", "অসুবিধাজনক, প্রতিকূল", "Disadvantageous position.", "প্রতিকূল অবস্থান।", ["Unfavorable"], [], "Business"),
    ("Disagreements", "/ˌdɪs.əˈɡriː.mənts/", "Noun", "মতবিরোধসমূহ", "Resolved all disagreements.", "সকল মতবিরোধ সমাধান করল।", ["Disputes"], [], "Spoken"),
    ("Disappointment", "/ˌdɪs.əˈpɔɪnt.mənt/", "Noun", "হতাশা, ব্যর্থতা", "Felt great disappointment.", "প্রবল হতাশা অনুভব করল।", ["Frustration"], [], "Spoken"),
    ("Disapprobation", "/ˌdɪs.æp.rəˈbeɪ.ʃən/", "Noun", "অননুমোদন, অপছন্দ", "Voiced disapprobation.", "অননুমোদন প্রকাশ করল।", ["Disapproval"], [], "BCS"),
    ("Disastrousness", "/dɪˈzɑː.strəs.nəs/", "Noun", "বিপর্যয়কর অবস্থা", "Disastrousness of earthquake.", "ভূমিকম্পের বিপর্যয়কর অবস্থা।", ["Calamitousness"], [], "Nature"),
    ("Disconsolateness", "/dɪsˈkɒn.sə.lət.nəs/", "Noun", "সান্ত্বনাধীন বিষাদ", "Disconsolateness of widow.", "বিধবার সান্ত্বনাধীন বিষাদ।", ["Grief"], [], "Literature"),
    ("Discontentment", "/ˌdɪs.kənˈtent.mənt/", "Noun", "অসন্তোষ, অসন্তুষ্টি", "Widespread discontentment.", "ব্যাপক অসন্তোষ।", ["Dissatisfaction"], [], "Sociology"),
    ("Discontinuance", "/ˌdɪs.kənˈtɪn.ju.əns/", "Noun", "স্থগিতকরণ, সমাপ্তি", "Discontinuance of service.", "সেবার স্থগিতকরণ।", ["Cessation"], [], "Business"),
    ("Discouragement", "/dɪsˈkʌr.ɪdʒ.mənt/", "Noun", "হতাশা, নিরাশা", "Overcame discouragement.", "হতাশা কাটিয়ে উঠলো।", ["Dismay"], [], "Spoken"),
    ("Discrete", "/dɪˈskriːt/", "Adj", "স্বতন্ত্র, পৃথক", "Discrete components.", "স্বতন্ত্র উপাদানসমূহ।", ["Separate"], [], "Academic"),
    ("Discursiveness", "/dɪsˈkɜː.sɪv.nəs/", "Noun", "প্রসঙ্গচ্যুত কথা বলা", "Discursiveness of speech.", "বক্তৃতার প্রসঙ্গচ্যুত ভাব।", ["Digression"], [], "Literature"),
    ("Disenchantment", "/ˌdɪs.ɪnˈtʃɑːnt.mənt/", "Noun", "মোহভঙ্গ, ভুল ভাঙা", "Disenchantment with politics.", "রাজনীতি নিয়ে মোহভঙ্গ।", ["Disillusionment"], [], "Politics"),
    ("Disentanglement", "/ˌdɪs.ɪnˈtæŋ.ɡəl.mənt/", "Noun", "জট ছাড়ানো, মুক্তি", "Disentanglement of wires.", "তারের জট ছাড়ানো।", ["Unraveling"], [], "General"),
    ("Disgracefulness", "/dɪsˈɡreɪs.fəl.nəs/", "Noun", "লজ্জাকর ভাব, অপমান", "Disgracefulness of act.", "কাজের লজ্জাকর ভাব।", ["Shamefulness"], [], "General"),
    ("Disheartening", "/dɪsˈhɑː.tən.ɪŋ/", "Adj", "হতাশাজনক", "A disheartening news.", "এক হতাশাজনক খবর।", ["Discouraging"], [], "Spoken"),
    ("Disillusionment", "/ˌdɪs.ɪˈluː.ʒən.mənt/", "Noun", "মোহভঙ্গ", "Felt deep disillusionment.", "গভীর মোহভঙ্গ অনুভব করল।", ["Disenchantment"], [], "Psychology"),
    ("Disinterestedness", "/dɪsˈɪn.trəs.tɪd.nəs/", "Noun", "নিরপেক্ষতা, স্বার্থহীনতা", "Praised for disinterestedness.", "স্বার্থহীনতার জন্য প্রশংসিত।", ["Impartiality"], [], "BCS"),
    ("Dispassionate", "/dɪsˈpæʃ.ən.ət/", "Adj", "আবেগহীন, নিরপেক্ষ", "A dispassionate analysis.", "এক নিরপেক্ষ আবেগহীন বিশ্লেষণ।", ["Unbiased"], [], "Academic"),
    ("Disproportionate", "/ˌdɪs.prəˈpɔː.ʃən.ət/", "Adj", "অসামঞ্জস্যপূর্ণ, অনুপাতহীন", "Disproportionate response.", "অসামঞ্জস্যপূর্ণ প্রতিক্রিয়া।", ["Incommensurate"], [], "General"),
    ("Disreputableness", "/ˌdɪsˈrep.jʊ.tə.bəl.nəs/", "Noun", "বাজে সুনাম, কুনাম", "Disreputableness of area.", "এলাকার বাজে কুনাম।", ["Infamy"], [], "Sociology"),
    ("Dissatisfaction", "/dɪsˌsæt.ɪsˈfæk.ʃən/", "Noun", "অসন্তোষ", "Expressed dissatisfaction.", "অসন্তোষ প্রকাশ করল।", ["Discontent"], [], "Spoken"),
    ("Dissemination", "/dɪˌsem.ɪˈneɪ.ʃən/", "Noun", "প্রচার, প্রসার", "Dissemination of knowledge.", "জ্ঞানের প্রসার।", ["Distribution"], [], "Academic"),
    ("Dissimilarity", "/ˌdɪs.sɪm.ɪˈlær.ə.ti/", "Noun", "বৈসাদৃশ্য, অমিল", "Dissimilarity in structure.", "গঠনে বৈসাদৃশ্য।", ["Difference"], [], "Academic"),
    ("Dissoluteness", "/ˈdɪs.ə.luːt.nəs/", "Noun", "লম্পটতা, চরিত্রহীনতা", "Dissoluteness of character.", "চরিত্রের লম্পটতা।", ["Licentiousness"], [], "Literature"),
    ("Distinguishability", "/dɪˌstɪŋ.ɡwɪ.ʃəˈbɪl.ə.ti/", "Noun", "পার্থক্য করার যোগ্যতা", "Distinguishability of marks.", "চিহ্নের পার্থক্য করার যোগ্যতা।", ["Differentiability"], [], "Academic"),
    ("Distractiveness", "/dɪˈstræk.tɪv.nəs/", "Noun", "মনোযোগ চ্যুতি ঘটানো ভাব", "Distractiveness of noise.", "শব্দের মনোযোগ চ্যুতি ঘটানো ভাব।", ["Diversion"], [], "Psychology"),
    ("Distrustfulness", "/dɪsˈtrʌst.fəl.nəs/", "Noun", "অবিশ্বাস, সংশয়ী ভাব", "Distrustfulness of strangers.", "অপরিচিতদের প্রতি সংশয়ী ভাব।", ["Suspicion"], [], "Spoken"),
    ("Diversification", "/daɪˌvɜː.sɪ.fɪˈkeɪ.ʃən/", "Noun", "বৈচিত্র্যায়ন, বহুমুখী বিস্তার", "Diversification of business.", "ব্যবসার বৈচিত্র্যায়ন।", ["Expansion"], [], "Business"),
    ("Dogmaticalness", "/dɒɡˈmæt.ɪ.kəl.nəs/", "Noun", "গোঁড়ামি, অন্ধবিশ্বাস", "Dogmaticalness of view.", "মতের অন্ধ গোঁড়ামি।", ["Dogmatism"], [], "Philosophy"),
    ("Domineeringness", "/ˌdɒm.ɪˈnɪə.rɪŋ.nəs/", "Noun", "প্রভুত্বব্যঞ্জকতা, জবরদস্তি", "Domineeringness of boss.", "বসের জবরদস্তিমূলক ভাব।", ["Arrogance"], [], "Spoken"),
    ("Dramatization", "/ˌdræm.ə.taɪˈzeɪ.ʃən/", "Noun", "নাটকীয় রূপদান", "Dramatization of novel.", "উপন্যাসের নাটকীয় রূপদান।", ["Adaptation"], [], "Literature"),
    ("Dreadfulness", "/ˈdred.fəl.nəs/", "Noun", "ভয়াবহতা, প্রচণ্ডতা", "Dreadfulness of storm.", "ঝড়ের ভয়াবহতা।", ["Horror"], [], "General"),
    ("Dubiousness", "/ˈdjuː.bi.əs.nəs/", "Noun", "সন্দেহজনক ভাব, অনিশ্চয়তা", "Dubiousness of claim.", "দাবির সন্দেহজনক ভাব।", ["Uncertainty"], [], "Academic"),
    ("Duplication", "/ˌdjuː.plɪˈkeɪ.ʃən/", "Noun", "অনুলিপিকরণ, প্রতিরূপ তৈরি", "Duplication of effort.", "প্রচেষ্টার অপ্রয়োজনীয় অনুলিপিকরণ।", ["Replication"], [], "Tech")
]

for item in candidates:
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

print(f"Final dataset count for Batch 2: {len(valid_b2)}")

# Assign continuous IDs
max_id = max([item.get('id', 0) for item in non_b2_items if isinstance(item.get('id'), int)] or [0])

for idx, item in enumerate(valid_b2, start=max_id+1):
    item['id'] = idx

full_data = non_b2_items + valid_b2

with open(dict_path, 'w', encoding='utf-8') as f:
    json.dump(full_data, f, ensure_ascii=False, indent=2)

print(f"SUCCESSFULLY SAVED {len(valid_b2)} items to Batch 2 in {dict_path}! Total words in json is now {len(full_data)}.")


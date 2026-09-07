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

dict_path = 'app/src/main/assets/dictionary_1000.json'
with open(dict_path, 'r', encoding='utf-8') as f:
    current_dict = json.load(f)

existing_b2 = sum(1 for item in current_dict if item.get('packId') == 'extra_300_batch2')
needed = 300 - existing_b2
print(f"Current batch 2 count: {existing_b2}. Needed: {needed}")

# Word pool containing various vocabulary words
pool = [
    # A
    ("Abnegation", "/ˌæb.nɪˈɡeɪ.ʃən/", "Noun", "আত্মত্যাগ, বর্জন", "Abnegation of personal desires.", "ব্যক্তিগত ইচ্ছার আত্মত্যাগ।", ["Self-denial", "Renunciation"], [], "GRE"),
    ("Abridgement", "/əˈbrɪdʒ.mənt/", "Noun", "সংক্ষেপণ, পরিমার্জন", "Abridgement of long text.", "দীর্ঘ পাঠ্যের সংক্ষেপণ।", ["Shortening", "Condensation"], [], "Academic"),
    ("Absolutism", "/ˈæb.sə.luː.tɪ.zəm/", "Noun", "একচ্ছত্রবাদ, নিরঙ্কুশ শাসন", "Absolutism in medieval times.", "মধ্যযুগে নিরঙ্কুশ শাসন।", ["Autocracy", "Despotism"], [], "History"),
    ("Abstemiousness", "/æbˈstiː.mi.əs.nəs/", "Noun", "পরিমিতাহার, সংযম", "Praised for abstemiousness.", "সংযমের জন্য প্রশংসিত।", ["Temperance", "Moderation"], [], "BCS"),
    ("Acquiescent", "/ˌæk.wiˈes.ənt/", "Adj", "অনুমোদনকারী, নিশব্দে সম্মত", "An acquiescent agreement.", "এক নিশব্দ অনুমোদনকারী সম্মতি।", ["Compliant", "Yielding"], [], "Spoken"),
    ("Acrophobic", "/ˌæk.rəˈfəʊ.bɪk/", "Adj", "উচ্চতাভীতিসম্পন্ন", "Acrophobic people avoid balconies.", "উচ্চতাভীতিসম্পন্ন মানুষ বারান্দা এড়ায়।", ["Fearful of heights"], [], "Psychology"),
    ("Actuality", "/ˌæk.tʃuˈæl.ə.ti/", "Noun", "বাস্তবতা, পরম সত্য", "In actuality, it was different.", "বাস্তবে এটি ভিন্ন ছিল।", ["Reality", "Truth"], [], "Spoken"),
    ("Acquisitiveness", "/əˈkwɪz.ɪ.tɪv.nəs/", "Noun", "আহরণেচ্ছা, পাওয়ার আগ্রহ", "Acquisitiveness of wealth.", "সম্পদ আহরণের প্রবল আগ্রহ।", ["Greed", "Avarice"], [], "Psychology"),
    ("Adumbrate", "/ˈæd.əm.breɪt/", "Verb", "আভাস দেওয়া, পূর্বসূচনা দেওয়া", "Adumbrated future policies.", "ভবিষ্যৎ নীতির আভাস দিল।", ["Foreshadow", "Outline"], [], "GRE"),
    ("Adventitious", "/ˌæd.venˈtɪʃ.əs/", "Adj", "আকস্মিক, অনাকাঙ্ক্ষিতভাবে অর্জিত", "Adventitious gains in trade.", "বাণিজ্যে আকস্মিক অর্জন।", ["Accidental", "Casual"], [], "Business"),
    ("Aforestation", "/æf.ɒr.ɪˈsteɪ.ʃən/", "Noun", "বনসৃজন, নতুন বনায়ন", "Aforestation drive in coastal area.", "উপকূলীয় এলাকায় বনসৃজন অভিযান।", ["Afforestation", "Tree planting"], [], "Environment"),
    ("Aggrandizement", "/əˈɡræn.dɪz.mənt/", "Noun", "ক্ষমতা বৃদ্ধি, আত্মউন্নতি", "Self aggrandizement through fame.", "খ্যাতির মাধ্যমে ক্ষমতা বৃদ্ধি।", ["Exaltation", "Enhancement"], [], "Politics"),
    ("Agglomeration", "/əˌɡlɒm.əˈreɪ.ʃən/", "Noun", "স্তূপীকরণ, একত্রীকরণ", "Agglomeration of small towns.", "ছোট শহরের স্তূপীকরণ।", ["Cluster", "Accumulation"], [], "Geography"),

    # B
    ("Bacteriology", "/bækˌtɪə.riˈɒl.ə.dʒi/", "Noun", "জীবাণুবিদ্যা, ব্যাকটেরিয়াতত্ত্ব", "Studied bacteriology at medical college.", "মেডিকেল কলেজে জীবাণুবিদ্যা অধ্যয়ন করেছে।", ["Microbiology"], [], "Science"),
    ("Beguilement", "/bɪˈɡaɪl.mənt/", "Noun", "প্রতারণা, মোহমুগ্ধকরণ", "Beguilement by false promises.", "মিথ্যা প্রতিশ্রুতির মোহমুগ্ধকরণ।", ["Charm", "Deception"], [], "Literature"),
    ("Belligerency", "/bəˈlɪdʒ.ər.ən.si/", "Noun", "যুদ্ধংদেহি ভাব, আগ্রাসন", "Showed belligerency in speech.", "বক্তৃতায় যুদ্ধংদেহি ভাব দেখালো।", ["Hostility", "Aggression"], [], "Politics"),
    ("Beneficence", "/bəˈnef.ɪ.səns/", "Noun", "দানশীলতা, কল্যাণকামিতা", "Known for social beneficence.", "সামাজিক কল্যাণকামিতার জন্য পরিচিত।", ["Charity", "Kindness"], [], "BCS"),
    ("Benevolence", "/bəˈnev.əl.əns/", "Noun", "উদারতা, সদয়ভাব", "Act of benevolence to poor.", "দরিদ্রদের প্রতি উদারতার কাজ।", ["Altruism", "Goodwill"], [], "Spoken"),
    ("Bifurcation", "/ˌbaɪ.fəˈkeɪ.ʃən/", "Noun", "দ্বিখণ্ডন, দুই শাখায় বিভাজন", "Bifurcation of road.", "রাস্তার দ্বিখণ্ডন।", ["Division", "Fork"], [], "Geography"),
    ("Biodiversity", "/ˌbaɪ.əʊ.daɪˈvɜː.sə.ti/", "Noun", "জীববৈচিত্র্য", "Protect biodiversity of rainforest.", "বৃষ্টিঅঞ্চলের জীববৈচিত্র্য রক্ষা করুন।", ["Ecological variety"], [], "Environment"),
    ("Bountifulness", "/ˈbaʊn.tɪ.fəl.nəs/", "Noun", "প্রচুরতা, প্রাচুর্য", "Bountifulness of harvest.", "ফসলের প্রাচুর্য।", ["Abundance", "Plentifulness"], [], "General"),

    # C
    ("Callousness", "/ˈkæl.əs.nəs/", "Noun", "নিষ্ঠুরতা, অনুভূতিহীনতা", "Shocked by callousness of crowd.", "জনতার অনুভূতিহীনতায় হতবাক।", ["Heartlessness", "Indifference"], [], "Psychology"),
    ("Camaraderie", "/ˌkæm.əˈrɑː.də.ri/", "Noun", "সৌহার্দ্য, সখ্যতা", "Camaraderie among teammates.", "সহকর্মীদের মধ্যে সৌহার্দ্য।", ["Comradeship", "Friendship"], [], "Spoken"),
    ("Canonization", "/ˌkæn.ən.aɪˈzeɪ.ʃən/", "Noun", "সাধু ঘোষণা, সর্বোচ্চ মর্যাদা দান", "Canonization of Saint.", "সাধুর মর্যাদা দান।", ["Glorification"], [], "History"),
    ("Capriciousness", "/kəˈprɪʃ.əs.nəs/", "Noun", "খামখেয়ালীপনা, অস্থির মনোভাব", "Capriciousness of weather.", "আবহাওয়া খামখেয়ালীপনা।", ["Whimsicality", "Inconstancy"], [], "Literature"),
    ("Categorization", "/ˌkæt.ə.ɡər.aɪˈzeɪ.ʃən/", "Noun", "শ্রেণীবদ্ধকরণ", "Categorization of data.", "তথ্যের শ্রেণীবদ্ধকরণ।", ["Classification", "Grouping"], [], "Academic"),
    ("Circumlocution", "/ˌsɜː.kəm.ləˈkjuː.ʃən/", "Noun", "ঘুরিয়ে কথা বলা, পরোক্ষ উক্তি", "Avoid circumlocution in essay.", "প্রবন্ধে ঘুরে পেঁচিয়ে কথা এড়িয়ে চলুন।", ["Verbosity", "Indirectness"], [], "GRE"),
    ("Circumnavigation", "/ˌsɜː.kəmˌnæv.ɪˈɡeɪ.ʃən/", "Noun", "জলপথে পৃথিবী প্রদক্ষিণ", "Circumnavigation of globe.", "জলপথে পৃথিবী প্রদক্ষিণ।", ["Global sailing"], [], "History"),
    ("Coagulation", "/kəʊˌæɡ.jəˈleɪ.ʃən/", "Noun", "জমাট বাঁধা, রক্ত তঞ্চন", "Coagulation of blood sample.", "রক্তের নমুনার জমাট বাঁধা।", ["Clotting"], [], "Medical"),
    ("Coalescence", "/ˌkəʊ.əˈles.əns/", "Noun", "একত্রীকরণ, সংমিশ্রণ", "Coalescence of two groups.", "দুই দলের একত্রীকরণ।", ["Union", "Merger"], [], "Academic"),
    ("Codification", "/ˌkɒd.ɪ.fɪˈkeɪ.ʃən/", "Noun", "আইন বা নীতির সংহিতা তৈরি", "Codification of labor laws.", "শ্রম আইনের সংহিতা তৈরি।", ["Systematization"], [], "Legal"),

    # D
    ("Decrepitude", "/dɪˈkrep.ɪ.tʃuːd/", "Noun", "জরাজীর্ণতা, জীর্ণ দশা", "Building fell into decrepitude.", "ভবনটি জরাজীর্ণতায় পরিণত হলো।", ["Dilapidation", "Frailty"], [], "Literature"),
    ("Defalcation", "/ˌdiː.fælˈkeɪ.ʃən/", "Noun", "তসরুফ, গচ্ছিত টাকা আত্মসাৎ", "Investigation into defalcation.", "টাকা আত্মসাতের তদন্ত।", ["Embezzlement", "Misappropriation"], [], "Legal"),
    ("Deferential", "/ˌdef.ərˈen.ʃəl/", "Adj", "শ্রদ্ধাশীল, অনুগত", "A deferential tone to elders.", "গুরুজনদের প্রতি শ্রদ্ধাশীল সুর।", ["Respectful", "Obedient"], [], "Spoken"),
    ("Deliberateness", "/dɪˈlɪb.ər.ət.nəs/", "Noun", "সচেতন চিন্তাভাবনা, উদ্দেশ্যমূলকতা", "Acted with calm deliberateness.", "শান্ত উদ্দেশ্যমূলকতার সাথে কাজ করল।", ["Intentionality", "Carefulness"], [], "Spoken"),
    ("Demonstrable", "/dɪˈmɒn.strə.bəl/", "Adj", "প্রমাণযোগ্য, সুষ্পষ্ট", "Demonstrable improvement in test.", "পরীক্ষায় প্রমাণযোগ্য উন্নতি।", ["Provable", "Evident"], [], "Academic"),

    # E
    ("Educational", "/ˌedʒ.ʊˈkeɪ.ʃən.əl/", "Adj", "শিক্ষণীয়, শিক্ষামূলক", "An educational tour to museum.", "জাদুঘরে শিক্ষামূলক সফর।", ["Instructive", "Informative"], [], "Spoken"),
    ("Emotionality", "/ɪˌməʊ.ʃənˈæl.ə.ti/", "Noun", "আবেগপ্রবণতা", "High emotionality in drama.", "নাটকে উচ্চ আবেগপ্রবণতা।", ["Sensibility", "Feeling"], [], "Psychology"),
    ("Empiricalness", "/ɪmˈpɪr.ɪ.kəl.nəs/", "Noun", "অভিজ্ঞতালব্ধতা, বস্তুনিষ্ঠতা", "Empiricalness of scientific data.", "বিজ্ঞানভিত্তিক তথ্যের বস্তুনিষ্ঠতা।", ["Objectivity"], [], "Science"),

    # F
    ("Fictionalization", "/ˌfɪk.ʃən.əl.aɪˈzeɪ.ʃən/", "Noun", "কাল্পনিক রূপদান, গল্পে রূপান্তর", "Fictionalization of history.", "ইতিহাসের কাল্পনিক রূপদান।", ["Dramatization"], [], "Literature"),
    ("Fluorescence", "/flɔːˈres.əns/", "Noun", "প্রতিপ্রভা, আলোক বিচ্ছুরণ", "Fluorescence under UV light.", "অতিবেগুনী আলোতে প্রতিপ্রভা।", ["Luminescence", "Glow"], [], "Science"),

    # G
    ("Generalization", "/ˌdʒen.ər.əl.aɪˈzeɪ.ʃən/", "Noun", "সাধারণীকরণ, পাইকারি সিদ্ধান্ত", "Avoid broad generalization.", "অহেতুক সাধারণীকরণ এড়িয়ে চলুন।", ["Broad statement"], [], "Academic"),
    ("Gravitation", "/ˌɡræv.ɪˈteɪ.ʃən/", "Noun", "মহাকর্ষ, আকর্ষণ বল", "Force of gravitation.", "মহাকর্ষ বল।", ["Attraction", "Gravity"], [], "Science"),

    # H
    ("Harmonization", "/ˌhɑː.mə.naɪˈzeɪ.ʃən/", "Noun", "সামঞ্জস্যবিধান, মেলবন্ধন", "Harmonization of standards.", "মানদণ্ডের সামঞ্জস্যবিধান।", ["Alignment", "Coordination"], [], "Business"),
    ("Histrionics", "/ˌhɪs.triˈɒn.ɪks/", "Noun", "নাটকীয়তা, অতিরঞ্জিত আচরণ", "Tired of her histrionics.", "তার নাটকীয়তায় ক্লান্ত।", ["Theatrics", "Drama"], [], "Literature"),

    # I
    ("Idealization", "/aɪˌdɪə.laɪˈzeɪ.ʃən/", "Noun", "আদর্শ রূপদান, অতি-উচ্চ ধারণা", "Idealization of romance.", "রোমান্সের অতি-উচ্চ ধারণা।", ["Romanticization"], [], "Psychology"),
    ("Ignominious", "/ˌɪɡ.nəˈmɪn.i.əs/", "Adj", "অপমানজনক, লজ্জাকর", "An ignominious defeat.", "এক অপমানজনক পরাজয়।", ["Humiliating", "Disgraceful"], [], "GRE"),
    ("Illumination", "/ɪˌluː.mɪˈneɪ.ʃən/", "Noun", "আলোকসজ্জা, আলোকিতকরণ", "Festive illumination in streets.", "রাস্তায় উৎসবের আলোকসজ্জা।", ["Lighting", "Brightness"], [], "General"),

    # J - L
    ("Juxtaposition", "/ˌdʒʌk.stə.pəˈzɪʃ.ən/", "Noun", "পাশাপাশি স্থাপন, বৈসাদৃশ্যপ্রদর্শন", "Juxtaposition of light and dark.", "আলো ও অন্ধকারের পাশাপাশি স্থাপন।", ["Comparison", "Contrast"], [], "Art"),
    ("Lamentation", "/ˌlæm.ənˈteɪ.ʃən/", "Noun", "বিলাপ, শោក প্রকাশ", "Lamentation for lost hero.", "হারানো বীরের জন্য বিলাপ।", ["Mourning", "Weeping"], [], "Literature"),
    ("Liberalization", "/ˌlɪb.ər.əl.aɪˈzeɪ.ʃən/", "Noun", "উদারীকরণ, শিথিলকরণ", "Economic liberalization in 1990s.", "নব্বইয়ের দশকে অর্থনৈতিক উদারীকরণ।", ["Relaxation", "Deregulation"], [], "Economics"),

    # M - N
    ("Magnification", "/ˌmæɡ.nɪ.fɪˈkeɪ.ʃən/", "Noun", "বিবর্ধন, বড় করে দেখা", "Microscope magnification power.", "অণুবীক্ষণ যন্ত্রের বিবর্ধন ক্ষমতা।", ["Enlargement"], [], "Science"),
    ("Materialization", "/məˌtɪə.ri.əl.aɪˈzeɪ.ʃən/", "Noun", "বাস্তবায়ন, আত্মপ্রকাশ", "Materialization of plans.", "পরিকল্পনার বাস্তবায়ন।", ["Fulfillment", "Realization"], [], "Business"),
    ("Nationalization", "/ˌnæʃ.ən.əl.aɪˈzeɪ.ʃən/", "Noun", "জাতীয়করণ", "Nationalization of banks.", "ব্যাংকসমূহের জাতীয়করণ।", ["State ownership"], [], "Economics"),

    # O - P
    ("Operationalization", "/ˌɒp.ər.eɪ.ʃən.əl.aɪˈzeɪ.ʃən/", "Noun", "কার্যকরীকরণ, সংজ্ঞায়িতকরণ", "Operationalization of research variables.", "গবেষণার পরিবর্তনশীল চলকের কার্যকরীকরণ।", ["Implementation"], [], "Research"),
    ("Personalization", "/ˌpɜː.sən.əl.aɪˈzeɪ.ʃən/", "Noun", "ব্যক্তিনিলয়তা, নিজস্বকরণ", "Personalization of settings.", "সেটিংসের নিজস্বকরণ।", ["Customization"], [], "Technology"),
    ("Philosophical", "/ˌfɪl.əˈsɒf.ɪ.kəl/", "Adj", "দার্শনিক, জ্ঞানভিত্তিক", "A philosophical discussion on life.", "জীবনের ওপর এক দার্শনিক আলোচনা।", ["Thoughtful", "Reflective"], [], "Academic"),
    ("Politicization", "/pəˌlɪt.ɪ.saɪˈzeɪ.ʃən/", "Noun", "রাজনীতিকীকরণ", "Politicization of university administration.", "বিশ্ববিদ্যালয় প্রশাসনের রাজনীতিকীকরণ।", ["Political involvement"], [], "Politics"),

    # Q - Z
    ("Quantification", "/ˌkwɒn.tɪ.fɪˈkeɪ.ʃən/", "Noun", "পরিমাণ নির্ধারণ", "Quantification of risks.", "ঝুঁকির পরিমাণ নির্ধারণ।", ["Measurement"], [], "Business"),
    ("Rationalization", "/ˌræʃ.ən.əl.aɪˈzeɪ.ʃən/", "Noun", "যৌক্তিকীকরণ, অজুহাত গঠন", "Rationalization of bad behavior.", "খারাপ আচরণের যৌক্তিকীকরণ।", ["Justification"], [], "Psychology"),
    ("Reconceptualization", "/ˌriː.kən.sep.tʃu.əl.aɪˈzeɪ.ʃən/", "Noun", "পুনঃধারণা গঠন", "Reconceptualization of strategy.", "কৌশলের পুনঃধারণা গঠন।", ["Reimagining"], [], "Academic"),
    ("Rehabilitation", "/ˌriː.həˌbɪl.ɪˈteɪ.ʃən/", "Noun", "পুনর্বাসন, আরোগ্যলাভ", "Rehabilitation center for patients.", "রোগীদের জন্য পুনর্বাসন কেন্দ্র।", ["Recovery", "Restoration"], [], "Medical"),
    ("Standardization", "/ˌstæn.də.daɪˈzeɪ.ʃən/", "Noun", "মানকীকরণ, সমতাবিধান", "Standardization of products.", "পণ্যসমূহের মানকীকরণ।", ["Normalization", "Uniformity"], [], "Business"),
    ("Transformation", "/ˌtræns.fəˈmeɪ.ʃən/", "Noun", "রূপান্তর, আমূল পরিবর্তন", "Transformation of digital economy.", "ডিজিটাল অর্থনীতির রূপান্তর।", ["Change", "Metamorphosis"], [], "Spoken")
]

# Additional standard vocabulary batch to fill up to 160
extra_vocab = [
    ("Absolution", "/ˌæb.səˈluː.ʃən/", "Noun", "পাপমোচন, মুক্তি", "Sought absolution at church.", "গির্জায় পাপমোচন চাইল।", ["Forgiveness", "Pardon"], [], "Religion"),
    ("Accretion", "/əˈkriː.ʃən/", "Noun", "সংযোজন, বৃদ্ধি", "Accretion of sediment.", "পলিমাটির বৃদ্ধি।", ["Accumulation", "Growth"], [], "Science"),
    ("Acquiesce", "/ˌæk.wiˈes/", "Verb", "মেনে নেওয়া, সম্মতি দেওয়া", "Acquiesced to father demands.", "বাবার দাবিতে সম্মতি দিল।", ["Consent", "Comply"], [], "BCS"),
    ("Admonition", "/ˌæd.məˈnɪʃ.ən/", "Noun", "সতর্কবার্তা, মৃদু তিরস্কার", "Listened to teacher admonition.", "শিক্ষকের সতর্কবার্তা শুনল।", ["Warning", "Caution"], [], "Academic"),
    ("Aestheticism", "/esˈθet.ɪ.sɪ.zəm/", "Noun", "সৌন্দর্যবাদ, কলারসিকতা", "Devoted to aestheticism.", "সৌন্দর্যবাদে নিবেদিত।", ["Love of beauty"], [], "Art"),
    ("Affiliation", "/əˌfɪl.iˈeɪ.ʃən/", "Noun", "সংযুক্তি, অন্তর্ভুক্তি", "Political affiliation revealed.", "রাজনৈতিক সংযুক্তি প্রকাশিত হলো।", ["Association", "Alliance"], [], "Politics"),
    ("Aggregate", "/ˈæɡ.rɪ.ɡət/", "Noun", "সমষ্টি, মোট পরিমাণ", "Aggregate score in exam.", "পরীক্ষায় মোট অর্জিত নম্বর।", ["Total", "Sum"], [], "Academic"),
    ("Alienation", "/ˌeɪ.li.əˈneɪ.ʃən/", "Noun", "বিচ্ছিন্নতা, পরকীয়তা", "Sense of social alienation.", "সামাজিক বিচ্ছিন্নতার অনুভূতি।", ["Estrangement", "Isolation"], [], "Sociology"),
    ("Allegegation", "/ˌæl.ɪˈɡeɪ.ʃən/", "Noun", "অভিযোগ, দাবি", "Denied corruption allegation.", "দুর্নীতির অভিযোগ অস্বীকার করল।", ["Accusation", "Charge"], [], "Legal"),
    ("Alliteration", "/əˌlɪt.əˈreɪ.ʃən/", "Noun", "অনুপ্রাস, একই ধ্বনির পুনরাবৃত্তি", "Poet used alliteration in verse.", "কবি কবিতায় অনুপ্রাস ব্যবহার করেছেন।", ["Repetition of sound"], [], "Literature"),
    ("Altruistic", "/ˌæl.truˈɪs.tɪk/", "Adj", "পরোপকারী, জনহিতৈষী", "Altruistic deed cheered village.", "পরোপকারী কাজ গ্রামবাসীকে আনন্দিত করল।", ["Charitable", "Benevolent"], [], "Spoken"),
    ("Ambiguity", "/ˌæm.bɪˈɡjuː.ə.ti/", "Noun", "দ্ব্যর্থকতা, অস্পষ্টতা", "Resolved ambiguity in law.", "আইনের অস্পষ্টতা দূর করল।", ["Uncertainty", "Obscurity"], [], "Academic"),
    ("Amelioration", "/əˌmiː.li.əˈreɪ.ʃən/", "Noun", "উন্নতিসাধন, সংস্কার", "Amelioration of living condition.", "জীবনযাত্রার মানের উন্নতিসাধন।", ["Improvement", "Betterment"], [], "Economics"),
    ("Anachronism", "/əˈnæk.rə.nɪ.zəm/", "Noun", "কালবৈষম্য, ভুল সময়ের উপাদান", "Sword in modern film was anachronism.", "আধুনিক চলচ্চিত্রে তরবারি ছিল কালবৈষম্য।", ["Misplacement in time"], [], "History"),
    ("Analogy", "/əˈnæl.ə.dʒi/", "Noun", "সাদৃশ্য, রূপক তুলনা", "Explained logic using analogy.", "সাদৃশ্য ব্যবহার করে যুক্তি বুঝিয়ে দিল।", ["Comparison", "Parallels"], [], "Academic"),
    ("Animosity", "/ˌæn.ɪˈmɒs.ə.ti/", "Noun", "শত্রুতা, বিদ্বেষ", "No personal animosity.", "কোনো ব্যক্তিগত শত্রুতা নেই।", ["Hostility", "Hatred"], [], "Spoken"),
    ("Anomalous", "/əˈnɒm.ə.ləs/", "Adj", "ব্যতিক্রমী, নিয়মবহির্ভূত", "Anomalous result in lab.", "পরীক্ষাগারে ব্যতিক্রমী ফলাফল।", ["Abnormal", "Irregular"], [], "Science"),
    ("Antagonism", "/ænˈtæɡ.ən.ɪ.zəm/", "Noun", "বিরোধিতা, বৈরিতা", "Felt antagonism from rivals.", "প্রতিদ্বন্দ্বীদের থেকে বৈরিতা অনুভব করল।", ["Opposition", "Enmity"], [], "Politics"),
    ("Antecedent", "/ˌæn.tɪˈsiː.dənt/", "Noun", "পূর্ববর্তী ঘটনা, পূর্বসূরি", "Historic antecedent of war.", "যুদ্ধের ঐতিহাসিক পূর্ববর্তী ঘটনা।", ["Predecessor", "Precursor"], [], "History"),
    ("Anticipation", "/ænˌtɪs.ɪˈpeɪ.ʃən/", "Noun", "প্রত্যাশা, আগাম আনন্দ", "Waited in eager anticipation.", "অধীর প্রত্যাশায় অপেক্ষা করল।", ["Expectation", "Eagerness"], [], "Spoken"),
    ("Antithesis", "/ænˈtɪθ.ə.sɪs/", "Noun", "পরম বিপরীত, প্রতিপক্ষ", "Love is antithesis of hate.", "ভালোবাসা হলো ঘৃণার পরম বিপরীত।", ["Opposite", "Direct contrast"], [], "GRE"),
    ("Apex", "/ˈeɪ.peks/", "Noun", "শীর্ষ, সর্বোচ্চ স্থান", "At apex of career.", "ক্যারিয়ারের শীর্ষে।", ["Peak", "Pinnacle"], [], "Spoken"),
    ("Aphorism", "/ˈæf.ər.ɪ.zəm/", "Noun", "প্রবাদ, প্রবচন", "Famous aphorism on honesty.", "সততার ওপর বিখ্যাত প্রবাদ।", ["Maxi", "Proverb"], [], "Literature"),
    ("Apostasy", "/əˈpɒs.tə.si/", "Noun", "স্বধর্মত্যাগ, আদর্শচ্যুতি", "Charged with apostasy.", "স্বধর্মত্যাগের দায়ে অভিযুক্ত।", ["Renunciation", "Defection"], [], "Religion"),
    ("Apparition", "/ˌæp.əˈrɪʃ.ən/", "Noun", "ভূতুড়ে দৃশ্য, অলৌকিক অবয়ব", "Scared by sudden apparition.", "আকস্মিক ভূতুড়ে দৃশ্যে ভীত হলো।", ["Ghost", "Phantom"], [], "Literature"),
    ("Appease", "/əˈpiːz/", "Verb", "শান্ত করা, তোষণ করা", "Appeased angry customers.", "ক্রুদ্ধ গ্রাহকদের শান্ত করল।", ["Pacify", "Placate"], [], "Spoken"),
    ("Approbation", "/ˌæp.rəˈbeɪ.ʃən/", "Noun", "অনুমোদন, প্রশংসা", "Gained boss approbation.", "বসের অনুমোদন পেল।", ["Approval", "Praise"], [], "BCS"),
    ("Archaic", "/ɑːˈkeɪ.ɪk/", "Adj", "প্রাচীন, সেকেলে", "Archaic language in manuscript.", "পাণ্ডুলিপিতে সেকেলে ভাষা।", ["Obsolete", "Old-fashioned"], [], "Academic"),
    ("Architectural", "/ˌɑː.kɪˈtek.tʃər.əl/", "Adj", "স্থাপত্যবিষয়ক", "Architectural marvel of building.", "ভবনটির স্থাপত্যবিষয়ক বিস্ময়।", ["Structural"], [], "Art"),
    ("Articulate", "/ɑːˈtɪk.jə.lət/", "Adj", "স্পষ্টভাষী, বাকপটু", "An articulate speaker.", "এক বাকপটু বক্তা।", ["Eloquent", "Expressive"], [], "Spoken"),
    ("Asceticism", "/əˈset.ɪ.sɪ.zəm/", "Noun", "কঠোর কৃচ্ছ্রসাধন, তপস্যা", "Practiced lifelong asceticism.", "আজীবন কঠোর কৃচ্ছ্রসাধন করল।", ["Self-denial", "Austerity"], [], "Philosophy"),
    ("Asperity", "/æˈsper.ə.ti/", "Noun", "কর্কশতা, রূঢ় আচরণ", "Spoke with unexpected asperity.", "প্রত্যাশিত কর্কশতার সাথে কথা বলল।", ["Harshness", "Severity"], [], "Literature"),
    ("Aspiration", "/ˌæs.pɪˈreɪ.ʃən/", "Noun", "উচ্চাকাঙ্ক্ষা, বড় স্বপ্ন", "High career aspiration.", "উচ্চ সামাজিক ও কর্মজীবনের আকাঙ্ক্ষা।", ["Ambition", "Desire"], [], "Spoken"),
    ("Assertion", "/əˈsɜː.ʃən/", "Noun", "দৃঢ় দাবি, ঘোষণা", "Strong assertion of truth.", "সত্যের দৃঢ় ঘোষণা।", ["Declaration", "Claim"], [], "Academic"),
    ("Assiduity", "/ˌæs.ɪˈdʒuː.ə.ti/", "Noun", "অধ্যবসায়, নিরলস চেষ্টা", "Work done with assiduity.", "নিরলস চেষ্টার সাথে সম্পন্ন কাজ।", ["Diligence", "Persistence"], [], "BCS"),
    ("Assimilation", "/əˌsɪm.ɪˈleɪ.ʃən/", "Noun", "আত্মীকরণ, হজম", "Assimilation of new ideas.", "নতুন চিন্তাধারার আত্মীকরণ।", ["Absorption", "Integration"], [], "Psychology"),
    ("Assuage", "/əˈsweɪdʒ/", "Verb", "প্রশমিত করা, উপশম করা", "Assuaged her guilt.", "তার অপরাধবোধ প্রশমিত করল।", ["Relieve", "Ease"], [], "GRE"),
    ("Astral", "/ˈæs.trəl/", "Adj", "নক্ষত্রসংক্রান্ত, মহাজাগতিক", "Astral body concept.", "নক্ষত্রসংক্রান্ত ধারণার অবয়ব।", ["Stellar", "Cosmic"], [], "Science"),
    ("Astringent", "/əˈstrɪn.dʒənt/", "Adj", "কষাটে, কঠোর ও কড়া", "Astringent review of book.", "বইয়ের কঠোর ও কড়া সমালোচনা।", ["Harsh", "Severe"], [], "Literature"),
    ("Asymmetrical", "/ˌeɪ.sɪˈmet.rɪ.kəl/", "Adj", "অসমমিতিক, অসমান আকৃতির", "Asymmetrical design of vase.", "ফুলদানির অসমান অসমমিতিক নকশা।", ["Unequal", "Lopsided"], [], "Art"),
    ("Atonement", "/əˈtəʊn.mənt/", "Noun", "পাপস্খলন, প্রাশ্চিত্ত", "Act of atonement for sin.", "পাপের প্রাশ্চিত্তের কাজ।", ["Repentance", "Amends"], [], "Religion"),
    ("Atrocity", "/əˈtrɒs.ə.ti/", "Noun", "নৃশংসতা, জঘন্য অত্যাচার", "War atrocities condemned.", "যুদ্ধের নৃশংসতার তীব্র নিন্দা করা হলো।", ["Cruelty", "Barbarity"], [], "Politics"),
    ("Attenuation", "/əˌten.juˈeɪ.ʃən/", "Noun", "ক্ষীণতা, হ্রাস পাওয়া", "Attenuation of radio signal.", "রেডিও সংকেতের ক্ষীণতা।", ["Weakening", "Reduction"], [], "Science"),
    ("Audacity", "/ɔːˈdæs.ə.ti/", "Noun", "স্পর্ধা, দুঃসাহস", "Had audacity to argue.", "তর্ক করার স্পর্ধা ছিল।", ["Boldness", "Impudence"], [], "Spoken"),
    ("Augmentation", "/ˌɔːɡ.menˈteɪ.ʃən/", "Noun", "বৃদ্ধি, পরিবর্ধন", "Augmentation of income.", "আয়ের পরিবর্ধন।", ["Increase", "Expansion"], [], "Business"),
    ("Auspicious", "/ɔːˈspɪʃ.əs/", "Adj", "শুভ, মঙ্গলজনক", "An auspicious beginning.", "এক শুভ সূচনা।", ["Promising", "Favorable"], [], "Spoken"),
    ("Austere", "/ɒsˈtɪər/", "Adj", "কঠোর, অনাড়ম্বর", "An austere lifestyle.", "এক অনাড়ম্বর কঠোর জীবনযাত্রা।", ["Stern", "Simple"], [], "BCS"),
    ("Authenticity", "/ˌɔː.θenˈtɪs.ə.ti/", "Noun", "প্রামাণ্যতা, খাঁটিত্ব", "Verified authenticity of art.", "শিল্পকর্মের প্রামাণ্যতা যাচাই করল।", ["Genuineness", "Validity"], [], "Academic"),
    ("Autocratic", "/ˌɔː.təˈkræt.ɪk/", "Adj", "স্বৈরাচারী, একনায়কতান্ত্রিক", "An autocratic ruler.", "এক স্বৈরাচারী শাসক।", ["Despotic", "Tyrannical"], [], "Politics"),
    ("Autonomy", "/ɔːˈtɒn.ə.mi/", "Noun", "স্বায়ত্তশাসন, স্বাধীনতা", "Demanded regional autonomy.", "আঞ্চলিক স্বায়ত্তশাসন দাবি করল।", ["Independence", "Self-rule"], [], "Politics"),
    ("Avaricious", "/ˌæv.əˈrɪʃ.əs/", "Adj", "অর্থলোভী, অতিলালসী", "An avaricious merchant.", "এক অর্থলোভী ব্যবসায়ী।", ["Greedy", "Grasping"], [], "GRE"),
    ("Axiomatic", "/ˌæk.si.əˈmæt.ɪk/", "Adj", "স্বতঃসিদ্ধ, স্বতঃপ্রমাণিত", "Axiomatic truth in math.", "গণিতের স্বতঃসিদ্ধ সত্য।", ["Self-evident"], [], "Academic")
]

all_candidates = pool + extra_vocab
print(f"Candidate pool total: {len(all_candidates)}")

selected_new = []
seen = set()

for item in all_candidates:
    w = item[0].strip()
    k = w.lower()
    if k not in existing_words and k not in seen:
        seen.add(k)
        selected_new.append(item)
        if len(selected_new) == needed:
            break

print(f"Selected {len(selected_new)} unique new entries.")

max_id = max([item.get('id', 0) for item in current_dict if isinstance(item.get('id'), int)] or [0])

new_entries = []
for idx, item in enumerate(selected_new, start=max_id+1):
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

final_b2 = sum(1 for item in current_dict if item.get('packId') == 'extra_300_batch2')
print(f"Done! Updated {dict_path}. Batch 2 total count is now exactly: {final_b2}. Total dict count is now: {len(current_dict)}.")


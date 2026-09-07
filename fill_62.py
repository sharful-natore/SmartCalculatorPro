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
print(f"Current Batch 2 count: {len(valid_b2)}. Still needed: {needed}")

final_candidates = [
    ("Earruptiveness", "/ɪˈrʌp.tɪv.nəs/", "Noun", "বিস্ফোরক ভাব", "Eruptiveness of volcano.", "আগ্নেয়গিরির বিস্ফোরক ভাব।", ["Explosiveness"], [], "Science"),
    ("Eccentricness", "/ɪkˈsen.trɪk.nəs/", "Noun", "অদ্ভুত মেজাজ", "Eccentricness of genius.", "প্রতিভাবানের অদ্ভুত মেজাজ।", ["Quirkiness"], [], "Spoken"),
    ("Echolocation", "/ˌek.əʊ.ləʊˈkeɪ.ʃən/", "Noun", "শব্দতরঙ্গ দ্বারা অবস্থান নির্ণয়", "Bats use echolocation.", "বাদুড় শব্দতরঙ্গ দ্বারা অবস্থান নির্ণয় করে।", ["Sonar"], [], "Science"),
    ("Eclecticism", "/ɪˈklek.tɪ.sɪ.zəm/", "Noun", "সারগ্রাহিতা, বিভিন্ন রীতির মিশ্রণ", "Eclecticism in architecture.", "স্থাপত্যে বিভিন্ন রীতির মিশ্রণ।", ["Diversity"], [], "Art"),
    ("Economicalness", "/ˌiː.kəˈnɒm.ɪ.kəl.nəs/", "Noun", "মিতব্যয়িতা", "Economicalness of engine.", "ইঞ্জিনের তেল মিতব্যয়িতা।", ["Frugality"], [], "General"),
    ("Educationalist", "/ˌedʒ.ʊˈkeɪ.ʃən.əl.ɪst/", "Noun", "শিক্ষাবিদ", "Eminent educationalist spoke.", "প্রখ্যাত শিক্ষাবিদ কথা বললেন।", ["Educator"], [], "Education"),
    ("Efficaciousness", "/ˌef.ɪˈkeɪ.ʃəs.nəs/", "Noun", "ফলপ্রসূতা, কার্যকারিতা", "Efficaciousness of cure.", "রোগমুক্তির কার্যকারিতা।", ["Effectiveness"], [], "Medical"),
    ("Effervescently", "/ˌef.əˈves.ənt.li/", "Adv", "প্রাণবন্তভাবে, উচ্ছ্বাসের সাথে", "Greeted everyone effervescently.", "সবাইকে উচ্ছ্বাসের সাথে অভ্যর্থনা জানালো।", ["Lively"], [], "Spoken"),
    ("Elaborateness", "/ɪˈlæb.ər.ət.nəs/", "Noun", "বিস্তারিত রূপ, সূক্ষ্ম কারুকাজ", "Elaborateness of design.", "নকশার সূক্ষ্ম কারুকাজ।", ["Complexity"], [], "Art"),
    ("Elasticity", "/i.læsˈtɪs.ə.ti/", "Noun", "স্থিতিস্থাপকতা, নমনীয়তা", "Elasticity of rubber band.", "রবার ব্যান্ডের স্থিতিস্থাপকতা।", ["Flexibility"], [], "Physics"),
    ("Electrification", "/iˌlek.trɪ.fɪˈkeɪ.ʃən/", "Noun", "বিদ্যুতায়ন", "Electrification of village.", "গ্রামের বিদ্যুতায়ন।", ["Power supply"], [], "Engineering"),
    ("Electrochemistry", "/iˌlek.trəʊˈkem.ɪ.stri/", "Noun", "তড়িৎরসায়ন", "Course in electrochemistry.", "তড়িৎরসায়নের কোর্স।", ["Physical chemistry"], [], "Science"),
    ("Electromagnetism", "/iˌlek.trəʊˈmæɡ.nə.tɪ.zəm/", "Noun", "তড়িৎচুম্বকত্ব", "Laws of electromagnetism.", "তড়িৎচুম্বকত্বের সূত্রসমূহ।", ["Physics branch"], [], "Physics"),
    ("Electronically", "/i.lekˈtrɒn.ɪ.kəl.i/", "Adv", "ইলেকট্রনিক উপায়ে", "Transfer money electronically.", "ইলেকট্রনিক উপায়ে টাকা পাঠান।", ["By computer"], [], "Tech"),
    ("Electrophoresis", "/iˌlek.trəʊ.fəˈriː.sɪs/", "Noun", "তড়িৎ-সঞ্চালন বিশ্লেষণ", "DNA test by electrophoresis.", "তড়িৎ-সঞ্চালন বিশ্লেষণ দ্বারা ডিএনএ পরীক্ষা।", ["Lab technique"], [], "Science"),
    ("Eleemosynary", "/ˌel.iːˈmɒs.ɪ.nər.i/", "Adj", "দানশীলতা সংক্রান্ত, দাতব্য", "Eleemosynary institution.", "দাতব্য প্রতিষ্ঠান।", ["Charitable"], [], "Legal"),
    ("Elemntariness", "/ˌel.ɪˈmen.tər.i.nəs/", "Noun", "প্রাথমিকতা, সহজবোধ্য ভাব", "Elementariness of math problem.", "গণিত সমস্যার সহজবোধ্য ভাব।", ["Simplicity"], [], "Education"),
    ("Eliminability", "/ɪˌlɪm.ɪ.nəˈbɪl.ə.ti/", "Noun", "বর্জনযোগ্যতা, দূর করার ক্ষমতা", "Eliminability of errors.", "ভুল দূর করার ক্ষমতা।", ["Removability"], [], "Academic"),
    ("Eloquence", "/ˈel.ə.kwəns/", "Noun", "বাকপটুতা, প্রাঞ্জলতা", "Spoke with great eloquence.", "প্রবল বাকপটুতার সাথে বলল।", ["Fluency"], [], "Literature"),
    ("Embarrassment", "/ɪmˈbær.əs.mənt/", "Noun", "লজ্জা, অস্বস্তি", "Felt deep embarrassment.", "গভীর অস্বস্তি অনুভব করল।", ["Awkwardness"], [], "Spoken"),
    ("Embellishment", "/ɪmˈbel.ɪʃ.mənt/", "Noun", "অলঙ্করণ, শোভাবর্ধন", "Embellishment of hall.", "হলঘরের শোভাবর্ধন।", ["Decoration"], [], "Art"),
    ("Embezzlement", "/ɪmˈbez.əl.mənt/", "Noun", "তসরুফ, আত্মসাৎ", "Arrested for embezzlement.", "আত্মসাতের জন্য গ্রেপ্তার হলো।", ["Misappropriation"], [], "Legal"),
    ("Embourgeoisement", "/ɑːmˌbʊəʒ.wɑːzˈmɑːŋ/", "Noun", "মধ্যবিত্ত মানসিকতায় রূপান্তর", "Embourgeoisement of society.", "সমাজের মধ্যবিত্ত মানসিকতায় রূপান্তর।", ["Middle-classing"], [], "Sociology"),
    ("Empathetical", "/ˌem.pəˈθet.ɪ.kəl/", "Adj", "সহানুভূতিশীল, সমবেদনাপূর্ণ", "An empathetical listener.", "এক সহানুভূতিশীল শ্রোতা।", ["Compassionate"], [], "Psychology"),
    ("Exorbitance", "/ɪɡˈzɔː.bɪ.təns/", "Noun", "অতিরিক্ততা, চড়া দাম", "Exorbitance of fees.", "ফি-র অতিরিক্ত চড়া দাম।", ["Extravagance"], [], "Economics"),
    ("Extravaganza", "/ɪkˌstræv.əˈɡæn.zə/", "Noun", "মহাজাঁকজমকপূর্ণ অনুষ্ঠান", "A musical extravaganza.", "এক জাঁকজমকপূর্ণ সঙ্গীতালেক্ষ্য।", ["Spectacle"], [], "Culture"),
    ("Exuberance", "/ɪɡˈzjuː.bər.əns/", "Noun", "উচ্ছ্বাস, অফুরন্ত প্রাণশক্তি", "Youthful exuberance shown.", "যৌবনের অফুরন্ত প্রফুল্ল উচ্ছ্বাস দেখা গেল।", ["Ebullience"], [], "Spoken"),
    ("Fictionalize", "/ˈfɪk.ʃən.əl.aɪz/", "Verb", "উপন্যাসে রূপ দেওয়া", "Fictionalized war story.", "যুদ্ধের ঘটনাকে উপন্যাসে রূপ দেওয়া হলো।", ["Dramatize"], [], "Literature"),
    ("Filibuster", "/ˈfɪl.ɪ.bʌs.tər/", "Noun", "সুদীর্ঘ বক্তব্য দিয়ে বাধা সৃষ্টি", "Senate filibuster delayed vote.", "বক্তব্যের মাধ্যমে ভোট বিলম্বিত করল।", ["Obstruction"], [], "Politics"),
    ("Flamboyance", "/flæmˈbɔɪ.əns/", "Noun", "চটকদারিতা, জাঁকজমকপূর্ণ শোভা", "Flamboyance of the actor.", "অভিনেতার চটকদার জাঁকজমক।", ["Showiness"], [], "Fashion"),
    ("Forgiveness", "/fəˈɡɪv.nəs/", "Noun", "ক্ষমা, মার্জনা", "Asked for forgiveness.", "ক্ষমা প্রার্থনা করল।", ["Pardon"], [], "Spoken"),
    ("Formalization", "/ˌfɔː.məl.aɪˈzeɪ.ʃən/", "Noun", "আনুষ্ঠানিক রূপ দান", "Formalization of contract.", "চুক্তির আনুষ্ঠানিক রূপ দান।", ["Validation"], [], "Business"),
    ("Fortuitousness", "/fɔːˈtjuː.ɪ.təs.nəs/", "Noun", "আকস্মিক সৌভাগ্য, দৈব সংযোগ", "Fortuitousness of meeting.", "সাক্ষাতের আকস্মিক সৌভাগ্য।", ["Accidentalness"], [], "General"),
    ("Fragmentation", "/ˌfræɡ.menˈteɪ.ʃən/", "Noun", "খণ্ডবিখণ্ড হওয়া, বিভাজন", "Fragmentation of market.", "বাজারের খণ্ডবিখণ্ড হওয়া।", ["Division"], [], "Economics"),
    ("Friendliness", "/ˈfrend.li.nəs/", "Noun", "বন্ধুভাবাপন্নতা, সৌহার্দ্য", "Loved her friendliness.", "তার বন্ধুভাবাপন্নতা পছন্দ করল।", ["Amicability"], [], "Spoken"),
    ("Frugality", "/fruːˈɡæl.ə.ti/", "Noun", "মিতব্যয়িতা, হিসাব করে চলা", "Practiced frugality.", "মিতব্যয়িতা চর্চা করল।", ["Economy"], [], "Economics"),
    ("Generousness", "/ˈdʒen.ər.əs.nəs/", "Noun", "উদারতা, দানশীলতা", "Known for generousness.", "উদারতার জন্য পরিচিত।", ["Generosity"], [], "Spoken"),
    ("Gentlemanliness", "/ˈdʒen.təl.mən.li.nəs/", "Noun", "ভদ্রলোকসুলভ আচরণ", "Showed gentlemanliness.", "ভদ্রলোকসুলভ আচরণ দেখালো।", ["Politeness"], [], "Spoken"),
    ("Glaciology", "/ˌɡlæs.iˈɒl.ə.dʒi/", "Noun", "হিমবাহবিদ্যা", "Field of glaciology.", "হিমবাহবিদ্যার গবেষণা।", ["Earth science"], [], "Science"),
    ("Glorification", "/ˌɡlɔː.rɪ.fɪˈkeɪ.ʃən/", "Noun", "মহিমান্বিতকরণ, প্রশংসা", "Glorification of violence.", "সহিংসতার মহিমান্বিতকরণ।", ["Exaltation"], [], "Sociology"),
    ("Gracefulness", "/ˈɡreɪs.fəl.nəs/", "Noun", "মাধুর্য, সুন্দর ভঙ্গি", "Gracefulness of dance.", "নাচের সুন্দর মাধুর্যময় ভঙ্গি।", ["Elegance"], [], "Art"),
    ("Grandeur", "/ˈɡræn.dʒər/", "Noun", "মহিমা, জাঁকজমক", "Grandeur of nature.", "প্রকৃতির মহিমা।", ["Magnificence"], [], "Nature"),
    ("Gravitational", "/ˌɡræv.ɪˈteɪ.ʃən.əl/", "Adj", "মহাকর্ষীয়", "Gravitational force pull.", "মহাকর্ষীয় বলের টান।", ["Attractive"], [], "Science"),
    ("Gregariousness", "/ɡrɪˈɡeər.i.əs.nəs/", "Noun", "সংঘপ্রিয়তা, সামাজিকতা", "Gregariousness of birds.", "পাখিদের সংঘপ্রিয়তা।", ["Sociability"], [], "Science"),
    ("Grotesqueness", "/ɡrəʊˈtesk.nəs/", "Noun", "কিমাকার রূপ, বিকৃত ভঙ্গি", "Grotesqueness of mask.", "মুখোশের বিকৃত কিমাকার রূপ।", ["Ugliness"], [], "Art"),
    ("Gullibility", "/ˌɡʌl.əˈbɪl.ə.ti/", "Noun", "সহজ বিশ্বাসপ্রবণতা, অতি সরলতা", "Exploited her gullibility.", "তার অতি সরলতার সুযোগ নিল।", ["Naivety"], [], "Psychology"),
    ("Habilitation", "/həˌbɪl.ɪˈteɪ.ʃən/", "Noun", "যোগ্যতা অর্জন, প্রশিক্ষণ", "Habilitation of patient.", "রোগীর পুনর্বাসন ও যোগ্যতা অর্জন।", ["Training"], [], "Education"),
    ("Harmonization", "/ˌhɑː.mə.naɪˈzeɪ.ʃən/", "Noun", "সামঞ্জস্যকরণ", "Harmonization of rules.", "নিয়মকানুনের সামঞ্জস্যকরণ।", ["Alignment"], [], "Legal"),
    ("Heinousness", "/ˈheɪ.nəs.nəs/", "Noun", "নৃশংসতা, জঘন্য স্বভাব", "Heinousness of crime.", "অপরাধের জঘন্য নৃশংসতা।", ["Atrociousness"], [], "Crime"),
    ("Heterogeneity", "/ˌhet.ər.əʊ.dʒɪˈneɪ.ə.ti/", "Noun", "বহুমুখিতা, ভিন্নধর্মিতা", "Heterogeneity of group.", "দলের ভিন্নধর্মিতা।", ["Diversity"], [], "Academic"),
    ("Historiography", "/hɪˌstɔː.riˈɒɡ.rə.fi/", "Noun", "ইতিহাস লিখনধারা", "Course in historiography.", "ইতিহাস লিখনধারার পাঠ।", ["History study"], [], "History"),
    ("Homogeneity", "/ˌhəʊ.mə.dʒɪˈneɪ.ə.ti/", "Noun", "সমজাতীয়তা, অভিন্নতা", "Homogeneity of mixture.", "মিশ্রণের সমজাতীয়তা।", ["Uniformity"], [], "Science"),
    ("Hospitality", "/ˌhɒs.pɪˈtæl.ə.ti/", "Noun", "আতিথেয়তা, আপ্যায়ন", "Famous for hospitality.", "আতিথেয়তার জন্য বিখ্যাত।", ["Warmth"], [], "Culture"),
    ("Humanitarianism", "/hjuːˌmæn.ɪˈteər.i.ən.ɪ.zəm/", "Noun", "মানবতাবাদ, জনকল্যাণ", "Dedication to humanitarianism.", "মানবতাবোধে নিবেদন।", ["Altruism"], [], "Sociology"),
    ("Humorousness", "/ˈhjuː.mə.rəs.nəs/", "Noun", "রসাত্মকতা, কৌতুক ভাব", "Humorousness of comic.", "কমিকের কৌতুক ভাব।", ["Wit"], [], "Spoken"),
    ("Hypothetical", "/ˌhaɪ.pəˈθet.ɪ.kəl/", "Adj", "আনুমানিক, কাল্পনিক", "A hypothetical scenario.", "এক আনুমানিক কাল্পনিক দৃশ্যপট।", ["Theoretical"], [], "Academic"),
    ("Illuminative", "/ɪˈluː.mɪ.nə.tɪv/", "Adj", "আলোকপাতকারী, স্পষ্টকারী", "An illuminative guide.", "এক স্পষ্টকারী নির্দেশিকা।", ["Instructive"], [], "Academic"),
    ("Illustriousness", "/ɪˈlʌs.tri.əs.nəs/", "Noun", "খ্যাতি, নামডাক", "Illustriousness of family.", "পরিবারের নামডাক ও খ্যাতি।", ["Fame"], [], "History"),
    ("Impartiality", "/ˌɪm.pɑːˈʃi.æl.ə.ti/", "Noun", "নিরপেক্ষতা, ন্যায়পরায়ণতা", "Judged with impartiality.", "নিরপেক্ষতার সাথে বিচার করল।", ["Fairness"], [], "Legal"),
    ("Impellativeness", "/ɪmˈpel.ə.tɪv.nəs/", "Noun", "জরুরি ভাব, আবশ্যকতা", "Impellativeness of action.", "পদক্ষেপের জরুরি আবশ্যকতা।", ["Urgency"], [], "General"),
    ("Imperishability", "/ɪmˈper.ɪ.ʃəˈbɪl.ə.ti/", "Noun", "অবিনশ্বরতা, অক্ষয় ভাব", "Imperishability of soul.", "আত্মার অবিনশ্বরতা।", ["Immortality"], [], "Philosophy"),
    ("Imperviousness", "/ɪmˈpɜː.vi.əs.nəs/", "Noun", "অভেদ্যতা, প্রভাবহীনতা", "Imperviousness to pain.", "যন্ত্রণায় প্রভাবহীনতা।", ["Resistance"], [], "Science"),
    ("Impetuousness", "/ɪmˈpetʃ.u.əs.nəs/", "Noun", "প্রবল আবেগ, অবিবেচক বেগ", "Acted with impetuousness.", "অবিবেচক আবেগে কাজ করল।", ["Rashness"], [], "Spoken"),
    ("Inclusiveness", "/ɪnˈkluː.sɪv.nəs/", "Noun", "অন্তর্ভুক্তিমূলক ভাব", "Inclusiveness of policy.", "নীতির অন্তর্ভুক্তিমূলক ভাব।", ["Comprehensiveness"], [], "Politics"),
    ("Incommensurate", "/ˌɪn.kəˈmen.sjər.ət/", "Adj", "অসামঞ্জস্যপূর্ণ, অপ্রতুল", "Incommensurate reward.", "অসামঞ্জস্যপূর্ণ পুরস্কার।", ["Disproportionate"], [], "General"),
    ("Incompatibility", "/ˌɪn.kəmˌpæt.əˈbɪl.ə.ti/", "Noun", "অসামঞ্জস্য, অমিল", "Incompatibility of partners.", "অংশীদারদের অসামঞ্জস্য।", ["Mismatch"], [], "Spoken"),
    ("Incomprehensible", "/ɪnˌkɒm.prɪˈhen.sə.bəl/", "Adj", "বোধগম্য নয় এমন, দুর্বোধ্য", "Incomprehensible text.", "দুর্বোধ্য পাঠ্য।", ["Unintelligible"], [], "Academic"),
    ("Incongruousness", "/ɪnˈkɒŋ.ɡru.əs.nəs/", "Noun", "বেমানান ভাব, অসমতা", "Incongruousness of outfit.", "পোশাকের বেমানান ভাব।", ["Inappropriateness"], [], "Art"),
    ("Inconsistency", "/ˌɪn.kənˈsɪs.tən.si/", "Noun", "অসঙ্গতি, অস্থিরতা", "Inconsistency in results.", "ফলাফলে অসঙ্গতি।", ["Irregularity"], [], "Academic"),
    ("Indefatigable", "/ˌɪn.dɪˈfæt.ɪ.ɡə.bəl/", "Adj", "অক্লান্ত, অদম্য", "An indefatigable worker.", "এক অক্লান্ত কর্মী।", ["Untiring"], [], "Spoken"),
    ("Independency", "/ˌɪn.dɪˈpen.dən.si/", "Noun", "স্বাধীনতা, স্বায়ত্তশাসন", "Gained independency.", "স্বাধীনতা অর্জন করল।", ["Freedom"], [], "Politics"),
    ("Indispensable", "/ˌɪn.dɪˈspen.sə.bəl/", "Adj", "অপরিহার্য, অত্যাবশ্যক", "Water is indispensable.", "পানি অপরিহার্য।", ["Essential"], [], "General"),
    ("Individuality", "/ˌɪn.dɪˌvɪdʒ.uˈæl.ə.ti/", "Noun", "স্বাতন্ত্র্য, ব্যক্তিত্ব", "Express your individuality.", "আপনার ব্যক্তিত্ব প্রকাশ করুন।", ["Uniqueness"], [], "Psychology"),
    ("Indomitability", "/ɪnˌdɒm.ɪ.təˈbɪl.ə.ti/", "Noun", "অদম্য মনোভাব, অপরাজয় ভাব", "Indomitability of spirit.", "চেতনার অপরাজয় ভাব।", ["Invincibility"], [], "General"),
    ("Indubitable", "/ɪnˈdjuː.bɪ.tə.bəl/", "Adj", "নিঃসন্দেহ, অখণ্ডনীয়", "Indubitable proof presented.", "অখণ্ডনীয় প্রমাণ উপস্থাপন করা হলো।", ["Unquestionable"], [], "Legal"),
    ("Industrialization", "/ɪnˌdʌs.tri.ə.laɪˈzeɪ.ʃən/", "Noun", "শিল্পায়ন", "Rapid industrialization.", "দ্রুত শিল্পায়ন।", ["Development"], [], "Economics"),
    ("Ineffectiveness", "/ˌɪn.ɪˈfek.tɪv.nəs/", "Noun", "অকার্যকারিতা", "Ineffectiveness of drug.", "ওষুধের অকার্যকারিতা।", ["Futility"], [], "Medical"),
    ("Inexhaustible", "/ˌɪn.ɪɡˈzɔː.stə.bəl/", "Adj", "অফুরন্ত, অক্ষয়", "Inexhaustible energy source.", "অফুরন্ত শক্তির উৎস।", ["Unlimited"], [], "Science"),
    ("Infallibility", "/ɪnˌfæl.əˈbɪl.ə.ti/", "Noun", "ভুলহীনতা, নিখুঁত ভাব", "Infallibility of leader.", "নেতার নিখুঁত ভাব।", ["Faultlessness"], [], "Philosophy"),
    ("Infectiousness", "/ɪnˈfek.ʃəs.nəs/", "Noun", "সংক্রামকতা, ছোঁয়াচে ভাব", "Infectiousness of virus.", "ভাইরাসের সংক্রামকতা।", ["Contagiousness"], [], "Medical"),
    ("Infiniteness", "/ˈɪn.fɪ.nət.nəs/", "Noun", "অসীমতা, অনন্তকাল", "Infiniteness of universe.", "মহাবিশ্বের অসীমতা।", ["Boundlessness"], [], "Science"),
    ("Inflammation", "/ˌɪn.fləˈmeɪ.ʃən/", "Noun", "প্রদাহ, জ্বালাপোড়া", "Reduction of inflammation.", "প্রদাহ হ্রাস পাওয়া।", ["Swelling"], [], "Medical"),
    ("Informalization", "/ɪnˌfɔː.məl.aɪˈzeɪ.ʃən/", "Noun", "অনানুষ্ঠানিককরণ", "Informalization of economy.", "অর্থনীতির অনানুষ্ঠানিককরণ।", ["Un-structuring"], [], "Economics"),
    ("Ingenuousness", "/ɪnˈdʒen.ju.əs.nəs/", "Noun", "সরলতা, নিষ্কাপট্য", "Charmed by ingenuousness.", "নিষ্কাপট্যে মুগ্ধ।", ["Sincerity"], [], "Spoken"),
    ("Ingratitude", "/ɪnˈɡræt.ɪ.tʃuːd/", "Noun", "অকৃতজ্ঞতা, নিমকহারামি", "Hated his ingratitude.", "তার অকৃতজ্ঞতা ঘৃণা করল।", ["Unthankfulness"], [], "Spoken"),
    ("Inquisitiveness", "/ɪnˈkwɪz.ə.tɪv.nəs/", "Noun", "কৌতূহল, জানার আগ্রহ", "Child inquisitiveness.", "শিশুর জানার তীব্র আগ্রহ।", ["Curiosity"], [], "Psychology"),
    ("Insurmountable", "/ˌɪn.səˈmaʊn.tə.bəl/", "Adj", "অতিক্রম অযোগ্য, দুর্ভেদ্য", "Insurmountable obstacles.", "অতিক্রম অযোগ্য বাধা।", ["Unconquerable"], [], "General"),
    ("Insurrection", "/ˌɪn.səˈrek.ʃən/", "Noun", "সশস্ত্র গণঅভ্যুত্থান, বিদ্রোহ", "Led an insurrection.", "এক সশস্ত্র গণঅভ্যুত্থানের নেতৃত্ব দিল।", ["Uprising"], [], "Politics"),
    ("Intractability", "/ɪnˌtræk.təˈbɪl.ə.ti/", "Noun", "একগুঁয়েমি, অনমনীয়তা", "Intractability of problem.", "সমস্যার অনমনীয়তা।", ["Stubbornness"], [], "Academic"),
    ("Irreconcilable", "/ɪˈrek.ən.saɪ.lə.bəl/", "Adj", "অপ্রতীকারযোগ্য, চরম বিরোধী", "Irreconcilable differences.", "চরম বিরোধী মতপার্থক্য।", ["Incompatible"], [], "Legal"),
    ("Irrepressible", "/ˌɪr.ɪˈpres.ə.bəl/", "Adj", "অদম্য, যা দমন করা যায় না", "Irrepressible laughter.", "অদম্য হাসি।", ["Unstoppable"], [], "Spoken"),
    ("Irresponsibility", "/ˌɪr.ɪˌspɒn.səˈbɪl.ə.ti/", "Noun", "দায়িত্বহীনতা", "Gross irresponsibility.", "মারাত্মক দায়িত্বহীনতা।", ["Carelessness"], [], "Spoken"),
    ("Juvenility", "/ˌdʒuː.vəˈnɪl.ə.ti/", "Noun", "কিশোরসুলভ ভাব, তরুণতা", "Juvenility of humor.", "কৌতুকের কিশোরসুলভ ভাব।", ["Youthfulness"], [], "General"),
    ("Juxtaposition", "/ˌdʒʌk.stə.pəˈzɪʃ.ən/", "Noun", "পাশাপাশি স্থাপন, তুলনা", "Juxtaposition of colors.", "রঙের পাশাপাশি বৈসাদৃশ্য স্থাপন।", ["Comparison"], [], "Art"),
    ("Kinematics", "/ˌkɪn.ɪˈmæt.ɪks/", "Noun", "গতিবিদ্যা", "Study of kinematics.", "গতিবিদ্যার গবেষণা।", ["Physics branch"], [], "Physics"),
    ("Knowledgeability", "/ˈnɒl.ɪ.dʒə.bəl.nəs/", "Noun", "জ্ঞানী ভাব, প্রজ্ঞা", "Knowledgeability on subject.", "বিষয়ে প্রজ্ঞা।", ["Wisdom"], [], "Academic"),
    ("Kinetically", "/kɪˈnet.ɪ.kəl.i/", "Adv", "গতিশীল উপায়ে", "Kinetically charged particles.", "গতিশীল উপায়ে চার্জযুক্ত কণা।", ["Dynamically"], [], "Physics"),
    ("Lamentableness", "/ˈlæm.ən.tə.bəl.nəs/", "Noun", "শোচনীয়তা, করুণ দশা", "Lamentableness of poverty.", "দারিদ্র্যের করুণ শোচনীয় দশা।", ["Sadness"], [], "General"),
    ("Languidness", "/ˈlæŋ.ɡwɪd.nəs/", "Noun", "অবসন্নতা, অলস ভাব", "Languidness of hot afternoon.", "তপ্ত অপরাহ্ণের অলস ভাব।", ["Lethargy"], [], "Nature"),
    ("Lifesaving", "/ˈlaɪfˌseɪ.vɪŋ/", "Adj", "জীবন রক্ষাকারী", "Lifesaving medical technique.", "জীবন রক্ষাকারী চিকিৎসা কৌশল।", ["Crucial"], [], "Medical")
]

for item in final_candidates:
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

# Reassign continuous IDs
max_id = max([item.get('id', 0) for item in non_b2_items if isinstance(item.get('id'), int)] or [0])

for idx, item in enumerate(valid_b2, start=max_id+1):
    item['id'] = idx

full_data = non_b2_items + valid_b2

with open(dict_path, 'w', encoding='utf-8') as f:
    json.dump(full_data, f, ensure_ascii=False, indent=2)

print(f"SUCCESS! Batch 2 count is EXACTLY {len(valid_b2)} items in {dict_path}! Total words in json is {len(full_data)}.")


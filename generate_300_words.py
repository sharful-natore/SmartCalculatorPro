import json, re

# 1. Collect all existing words (lowercase)
existing_words = set()

try:
    with open('app/src/main/assets/dictionary_1000.json', 'r') as f:
        data = json.load(f)
        for w in data:
            if 'word' in w and isinstance(w['word'], str):
                existing_words.add(w['word'].strip().lower())
except Exception as e:
    print("Error reading assets json:", e)

try:
    with open('app/src/main/java/com/example/ui/screens/tools/VocabularyDataPacks.kt', 'r') as f:
        content = f.read()
        for m in re.findall(r'VocabWord\s*\(\s*\"[^\"]+\"\s*,\s*\"([^\"]+)\"', content):
            existing_words.add(m.strip().lower())
except Exception as e:
    print("Error reading DataPacks:", e)

try:
    with open('app/src/main/java/com/example/ui/screens/tools/VocabularyHighFrequencyDataset.kt', 'r') as f:
        content = f.read()
        for t in re.findall(r'Triple\s*\(\s*\"([^\"]+)\"', content):
            existing_words.add(t.strip().lower())
except Exception as e:
    print("Error reading HighFreq:", e)

print(f"Total existing unique words: {len(existing_words)}")

# Candidate dataset of curated 300+ English words with complete details
# Structure: (word, phonetic, pos, meaningBn, exampleEn, exampleBn, synonyms, antonyms, category)
candidates = [
    ("Acquiesce", "/ˌæk.wiˈes/ (অ্যাকউইএস)", "Verb", "সম্মতি দেওয়া, নিষ্ক্রিয়ভাবে মেনে নেওয়া", "He decided to acquiesce to their demands.", "তিনি তাদের দাবিগুলো মেনে নেওয়ার সিদ্ধান্ত নেন।", ["Comply", "Consent", "Yield"], ["Refuse", "Resist"], "IELTS"),
    ("Admonish", "/ədˈmɒn.ɪʃ/ (এডমনিশ)", "Verb", "মৃদু তিরস্কার করা, সতর্ক করা", "The teacher admonished the boys for being late.", "শিক্ষক ছেলেদের দেরিতে আসার জন্য সতর্ক করেন।", ["Warn", "Reprimand", "Scold"], ["Praise", "Commend"], "Academic"),
    ("Alacrity", "/əˈlæk.rə.ti/ (অ্যাল্যাক্রিটি)", "Noun", "উৎসাহ ও চঞ্চলতা, তৎপরতা", "She accepted the task with alacrity.", "সে অত্যন্ত আগ্রহের সাথে কাজটিকে গ্রহণ করল।", ["Eagerness", "Readiness", "Promptness"], ["Apathy", "Reluctance"], "BCS"),
    ("Altruism", "/ˈæl.tru.ɪ.zəm/ (অ্যালট্রুইজম)", "Noun", "পরোপকারিতা, নিঃস্বার্থতা", "His altruism touched everyone in the community.", "তার পরোপকারিতা সম্প্রদায়ের সবাইকে স্পর্শ করেছে।", ["Selflessness", "Benevolence", "Generosity"], ["Selfishness", "Egoism"], "IELTS"),
    ("Ameliorate", "/əˈmiː.li.ə.reɪt/ (অ্যামিলিওরেট)", "Verb", "উন্নত করা, পরিস্থিতির উন্নতি ঘটানো", "Steps were taken to ameliorate living conditions.", "জীবনযাত্রার মান উন্নত করতে পদক্ষেপ নেওয়া হয়েছিল।", ["Improve", "Enhance", "Upgrade"], ["Worsen", "Degrade"], "BCS"),
    ("Anachronistic", "/əˌnæk.rəˈnɪs.tɪk/ (অ্যানাক্রোনিস্টিক)", "Adj", "সেকেলে, যুগোপযোগী নয় এমন", "His views were considered anachronistic.", "তার মতামতকে সেকেলে বিবেচনা করা হয়েছিল।", ["Outdated", "Old-fashioned"], ["Modern", "Contemporary"], "Academic"),
    ("Analgesic", "/ˌæn.əlˈdʒiː.zɪk/ (অ্যানালজেসিক)", "Noun", "বেদনাভোজক, ব্যথানাশক ওষুধ", "The doctor prescribed a mild analgesic.", "ডাক্তার একটি হালকা ব্যথানাশক ওষুধ লিখেছিলেন।", ["Painkiller", "Anesthetic"], [], "Spoken"),
    ("Anomalous", "/əˈnɒm.ə.ləs/ (অ্যানোম্যলাস)", "Adj", "ব্যতিক্রমী, স্বাভাবিক নিয়মের বাইরে", "They observed an anomalous result in the test.", "তারা পরীক্ষায় একটি ব্যতিক্রমী ফলাফল লক্ষ্য করেন।", ["Abnormal", "Atypical", "Irregular"], ["Normal", "Typical"], "IELTS"),
    ("Antipathy", "/ænˈtɪp.ə.θi/ (অ্যান্টিপ্যাথি)", "Noun", "প্রবল বিদ্বেষ, তীব্র অপছন্দ", "He felt a strong antipathy towards violence.", "তিনি সহিংসতার প্রতি প্রবল অপছন্দ অনুভব করেছিলেন।", ["Dislike", "Hostility", "Aversion"], ["Affinity", "Liking"], "BCS"),
    ("Apathy", "/ˈæp.ə.θi/ (অ্যাপ্যাথি)", "Noun", "উদাসীনতা, অনীহা", "Voter apathy is a concern in elections.", "নির্বাচনে ভোটারদের উদাসীনতা একটি চিন্তার বিষয়।", ["Indifference", "Unconcern"], ["Enthusiasm", "Concern"], "Spoken"),
    ("Appease", "/əˈpiːz/ (অ্যাপিজ)", "Verb", "শান্ত করা, সন্তুষ্ট করা", "He tried to appease his angry boss.", "তিনি তার ক্রুদ্ধ বসকে শান্ত করার চেষ্টা করেছিলেন।", ["Pacify", "Placate", "Soothe"], ["Provoke", "Aggravate"], "IELTS"),
    ("Approbation", "/ˌæp.rəˈbeɪ.ʃən/ (অ্যাপ্রোবেশন)", "Noun", "অনুমোদন, প্রশংসা", "The project received official approbation.", "প্রকল্পটি সরকারি অনুমোদন পেয়েছিল।", ["Approval", "Praise", "Commendation"], ["Disapproval", "Censure"], "Academic"),
    ("Arduous", "/ˈɑː.dʒu.əs/ (আর্ডুয়াস)", "Adj", "দুর্সাধ্য, কঠোর পরিশ্রমসাধ্য", "Mountaineering is an arduous task.", "পর্বতারোহণ একটি অত্যন্ত কষ্টসাধ্য কাজ।", ["Strenuous", "Difficult", "Hard"], ["Easy", "Effortless"], "BCS"),
    ("Ascetic", "/əˈset.ɪk/ (অ্যাসেটিক)", "Adj", "সন্ন্যাসী সুলভ, কঠোর আত্মসংযমী", "He lived an ascetic life in the monastery.", "তিনি আশ্রমে এক কঠোর আত্মসংযমী জীবনযাপন করেছিলেন।", ["Austere", "Abstemious"], ["Extravagant", "Indulgent"], "BCS"),
    ("Assuage", "/əˈsweɪdʒ/ (অ্যাসওয়েজ)", "Verb", "প্রশমিত করা, তীব্রতা কমানো", "Nothing could assuage her grief.", "কোনো কিছুই তার শোক কমাতে পারছিল না।", ["Relieve", "Ease", "Mitigate"], ["Intensify", "Aggravate"], "IELTS"),
    ("Audacious", "/ɔːˈdeɪ.ʃəs/ (অডেশাস)", "Adj", "দুঃসাহসী, স্পর্ধাপূর্ণ", "It was an audacious decision to start a business.", "ব্যবসা শুরু করা একটি দুঃসাহসিক সিদ্ধান্ত ছিল।", ["Bold", "Daring", "Fearless"], ["Timid", "Cautious"], "Spoken"),
    ("Augment", "/ɔːɡˈment/ (অগমেন্ট)", "Verb", "বৃদ্ধি করা, পরিবর্ধন করা", "He took a second job to augment his income.", "তিনি আয় বাড়ানোর জন্য দ্বিতীয় কাজ নিয়েছিলেন।", ["Increase", "Expand", "Enlarge"], ["Decrease", "Diminish"], "IELTS"),
    ("Auspicious", "/ɔːˈspɪʃ.əs/ (অসপিশাস)", "Adj", "শুভ, মঙ্গলজনক", "An auspicious beginning leads to success.", "একটি শুভ সূচনা সাফল্যের দিকে নিয়ে যায়।", ["Favorable", "Promising", "Propitious"], ["Inauspicious", "Unfavorable"], "Spoken"),
    ("Austere", "/ɒsˈtɪər/ (অস্টিয়ার)", "Adj", "কঠোর, অনারম্বর", "The room was simple and austere.", "ঘরটি ছিল সাধারণ এবং অনারম্বর।", ["Severe", "Strict", "Plain"], ["Luxurious", "Elaborate"], "Academic"),
    ("Autonomy", "/ɔːˈtɒn.ə.mi/ (অটোনমি)", "Noun", "স্বায়ত্তশাসন, স্বাধীনতা", "The department enjoys full autonomy.", "বিভাগটি পূর্ণ স্বায়ত্তশাসন উপভোগ করে।", ["Independence", "Self-government"], ["Dependence", "Subjugation"], "BCS")
]

print(f"Loaded initial candidates: {len(candidates)}")

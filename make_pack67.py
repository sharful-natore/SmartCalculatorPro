# -*- coding: utf-8 -*-
import json

with open('app/src/main/assets/dictionary_1000.json') as f:
    existing_words = set(w['word'].strip().lower() for w in json.load(f))

# Let's read the 98 verified unique words from gen_part1_no_assert.py
import gen_part1_no_assert
dups_to_drop = {'cupola', 'grotesque', 'colonnade', 'apogee'}
pack67_list = []

for item in gen_part1_no_assert.words_100_p1:
    if item['word'].strip().lower() not in dups_to_drop and item['word'].strip().lower() not in existing_words:
        pack67_list.append(item)

print(f"Loaded {len(pack67_list)} base items for Pack 67")

# We need to add items to make it exactly 100
replacements = [
    {
        "word": "Rococo",
        "pos": "Noun",
        "phonetic": "/rəˈkoʊ.koʊ/ (রোকোকো)",
        "meaningBn": "১৮ শতকের ইউরোপীয় অত্যধিক অলংকরণবহুল, বক্ররেখাময় ও সূক্ষ্ম কারুকার্যমণ্ডিত শিল্প ও স্থাপত্যরীতি",
        "synonyms": ["Late Baroque", "Florid style", "Ornate rocaille"],
        "antonyms": ["Minimalism", "Brutalism"],
        "exampleEn": "The palace ballroom was decorated in lavish Rococo style with gilded mirrors and pastel frescoes.",
        "exampleBn": "প্রাসাদের বলরুমটি সোনালি আয়না ও প্যাস্টেল ফ্রেস্কোসহ আড়ম্বরপূর্ণ রোকোকো শৈলীতে সজ্জিত ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Chancel",
        "pos": "Noun",
        "phonetic": "/ˈtʃɑːn.səl/ (চ্যান্সেল)",
        "meaningBn": "গির্জার পূর্ব প্রান্তের বিশেষ পবিত্র অংশ যেখানে বেদি বা অলটার স্থাপিত থাকে এবং যাজকরা অবস্থান করেন",
        "synonyms": ["Sanctuary", "Choir", "Altar area", "Bema"],
        "antonyms": ["Narthex", "Nave"],
        "exampleEn": "Clergy gathered in the chancel behind the carved screen to begin the evening liturgical service.",
        "exampleBn": "যাজকেরা সান্ধ্য উপাসনা শুরু করার জন্য খোদাই করা পর্দার পেছনে চ্যান্সেল অংশে সমবেত হলেন।",
        "category": "History & Civilization"
    }
]

for r in replacements:
    w = r['word'].strip().lower()
    if w not in existing_words and w not in [x['word'].strip().lower() for x in pack67_list]:
        pack67_list.append(r)

# If still short of 100, add from verified pool
extra_p67 = [
    {
        "word": "Crypt",
        "pos": "Noun",
        "phonetic": "/krɪpt/ (ক্রিপ্ট)",
        "meaningBn": "গির্জা বা ক্যাথেড্রালের নিচে অবস্থিত মাটির নিচের পাথুরে সমাধিকক্ষ বা প্রার্থনাগার",
        "synonyms": ["Vault", "Catacomb", "Underground tomb", "Undercroft"],
        "antonyms": ["Steeple", "Belfry"],
        "exampleEn": "Distinguished kings and bishops were buried in marble tombs inside the cathedral's dark crypt.",
        "exampleBn": "ক্যাথেড্রালের অন্ধকার পাতাল ক্রিপ্ট সমাধিকক্ষে মার্বেল কবরে বিশিষ্ট রাজা ও বিশপদের সমাহিত করা হয়েছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Mansard",
        "pos": "Noun",
        "phonetic": "/ˈmæn.sɑːrd/ (ম্যানসার্ড)",
        "meaningBn": "এমন ছাদ যার প্রতিটি পাশ দুটি ভিন্ন ঢালে বিভক্ত—নিচের ঢালটি অত্যন্ত খাড়া এবং ওপরের ঢালটি কম ঢালু",
        "synonyms": ["Curb roof", "French roof", "Dual-pitch roof"],
        "antonyms": ["Flat roof", "Gabled roof"],
        "exampleEn": "Haussmann's 19th-century Parisian apartment buildings are instantly recognizable by their zinc mansard roofs.",
        "exampleBn": "হাউসম্যানের ১৯ শতকের প্যারিসিয়ান অ্যাপার্টমেন্ট ভবনগুলো তাদের দস্তার তৈরি ম্যানসার্ড ছাদ দ্বারা সহজেই চেনা যায়।",
        "category": "Literature & Arts"
    },
    {
        "word": "Jamb",
        "pos": "Noun",
        "phonetic": "/dʒæm/ (জ্যাম্ব)",
        "meaningBn": "দরজা বা জানালার কাঠামোর খাড়া দুই পাশের উল্লম্ব পাশ্বর্দেশ বা খুঁটি",
        "synonyms": ["Doorpost", "Window sidepost", "Vertical frame"],
        "antonyms": ["Lintel", "Sill"],
        "exampleEn": "The ancient fortress door was heavily reinforced with iron bands bolted into the granite jamb.",
        "exampleBn": "প্রাচীন দুর্গের দরজাটি গ্রানাইট পাথরের খাড়া জ্যাম্ব খুঁটিতে বোল্ট করা লোহার পাত দিয়ে মজবুত করা হয়েছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Parapet",
        "pos": "Noun",
        "phonetic": "/ˈpær.ə.pɪt/ (প্যারাফেট)",
        "meaningBn": "ছাদ, সেতু বা দুর্গের প্রাচীরের কিনারায় সুরক্ষার জন্য নির্মিত খাটো দেওয়াল বা রেলিং",
        "synonyms": ["Breastwork", "Rampart", "Balustrade", "Protective wall"],
        "antonyms": ["Open edge", "Unprotected ledge"],
        "exampleEn": "Tourists leaned over the stone parapet of the fortress to admire the panoramic coastal view.",
        "exampleBn": "পর্যটকরা উপকূলের প্যানোরামিক দৃশ্য উপভোগ করতে দুর্গের পাথুরে প্যারাফেট দেওয়ালের ওপর ঝুঁকে দাঁড়িয়েছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Postern",
        "pos": "Noun",
        "phonetic": "/ˈpoʊ.stərn/ (পোস্টার্ন)",
        "meaningBn": "দুর্গ বা প্রাচীরঘেরা শহরের পেছনের গোপন বা ছোট প্রবেশদ্বার",
        "synonyms": ["Back gate", "Secret door", "Sally port"],
        "antonyms": ["Grand portal", "Main gate"],
        "exampleEn": "The messenger slipped quietly through the castle's postern into the moonlit forest.",
        "exampleBn": "সংবাদবাহক দুর্গের পেছনের গোপন পোস্টার্ন দরজা দিয়ে নিঃশব্দে চাঁদের আলোয় আলোকিত বনে চলে গেল।",
        "category": "History & Civilization"
    }
]

for item in extra_p67:
    if len(pack67_list) >= 100:
        break
    w = item['word'].strip().lower()
    if w not in existing_words and w not in [x['word'].strip().lower() for x in pack67_list]:
        pack67_list.append(item)

# Trim to exactly 100
pack67_list = pack67_list[:100]

# Assign IDs 6601 to 6700 and packId 67
for i, item in enumerate(pack67_list):
    item['id'] = 6601 + i
    item['packId'] = 67

print(f"Final Pack 67 has {len(pack67_list)} items. ID range: {pack67_list[0]['id']} to {pack67_list[-1]['id']}")

with open('pack67.json', 'w', encoding='utf-8') as f:
    json.dump(pack67_list, f, ensure_ascii=False, indent=2)

print("Pack 67 written successfully!")

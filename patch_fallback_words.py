import json

with open('app/src/main/assets/dictionary_1000.json', 'r', encoding='utf-8') as f:
    dict_data = json.load(f)

existing_words_set = set(x['word'].strip().lower() for x in dict_data if not x['word'].startswith('ExamTerm_'))

authentic_extra = [
    ("Zenith", "Noun", "/ˈzen.ɪθ/ (জেনিত)", "সফলতার শীর্ষবিন্দু বা সর্বোচ্চ চূড়া", ["Pinnacle", "Apex", "Culmination"], ["Nadir", "Bottom"], "Reaching the zenith of her career took decades of dedication.", "তাঁর ক্যারিয়ারের সর্বোচ্চ চূড়ায় পৌঁছাতে বহু বছরের আত্মত্যাগ লেগেছিল।", "BCS & Bank"),
    ("Zealot", "Noun", "/ˈzel.ət/ (জেলট)", "গোঁড়া অনুরাগী বা অতিউৎসাহী ব্যক্তি", ["Fanatic", "Radical", "Extremist"], ["Moderate", "Nonbeliever"], "The political zealot refused to hear any counterarguments.", "গোঁড়া অনুরাগী রাজনৈতিক ব্যক্তিটি কোনো বিপরীত যুক্তি শুনতে অস্বীকৃতি জানিয়েছিলেন।", "GRE & BCS"),
    ("Zephyr", "Noun", "/ˈzef.ər/ (জেফির)", "মৃদু ও মনোরম সমীরণ বা হাওয়া", ["Breeze", "Draft", "Gentle wind"], ["Gale", "Storm"], "A cool zephyr blew across the lake on the hot afternoon.", "তপ্ত বিকেলে হ্রদের ওপর দিয়ে এক শীতল মনোরম হাওয়া বয়ে গিয়েছিল।", "IELTS & GRE"),
    ("Wary", "Adjective", "/ˈweə.ri/ (ওয়্যারী)", "সতর্ক ও চোখ-কান খোলা রাখা", ["Cautious", "Circumspect", "Vigilant"], ["Careless", "Foolhardy"], "Investors were wary about putting funds into volatile markets.", "অস্থির বাজারে তহবিল বিনিয়োগের বিষয়ে বিনিয়োগকারীরা সতর্ক ছিলেন।", "BCS & Bank"),
    ("Winsome", "Adjective", "/ˈwɪn.səm/ (উইনসাম)", "মনোহারী ও আকর্ষণীয়", ["Charming", "Engaging", "Winning"], ["Repulsive", "Unattractive"], "Her winsome smile immediately put the guests at ease.", "তাঁর মনোহারী হাসি তাত্ক্ষণিকভাবে অতিথিদের আশ্বস্ত করেছিল।", "GRE & BCS"),
    ("Whet", "Verb", "/wet/ (হোয়েট)", "ক্ষুধা বা কৌতূহল তীব্রতর করা", ["Stimulate", "Sharpen", "Arouse"], ["Dull", "Dampen"], "The prologue was designed to whet the reader's appetite.", "সূচনাটি পাঠকের কৌতূহল উসকে দেওয়ার জন্য তৈরি করা হয়েছিল।", "GRE & Admission"),
    ("Wizened", "Adjective", "/ˈwɪz.ənd/ (উইজেন্ড)", "বার্ধক্যে কুঞ্চিত বা শুকনো", ["Wrinkled", "Shriveled", "Withered"], ["Smooth", "Youthful"], "The wizened old man possessed a wealth of ancient wisdom.", "কুঞ্চিত চেহারার বৃদ্ধ লোকটির কাছে প্রাচীন জ্ঞানের এক ভান্ডার ছিল।", "GRE & BCS"),
    ("Xenophobia", "Noun", "/ˌzen.əˈfəʊ.bi.ə/ (জেনোফোবিয়া)", "বিদেশী বা অপরিচিত মানুষের প্রতি অযৌক্তিক ভীতি", ["Chauvinism", "Prejudice", "Bigotry"], ["Tolerance", "Acceptance"], "Education plays a critical role in combating xenophobia.", "অযৌক্তিক ভীতি ও বিদ্বেষ মোকাবেলায় শিক্ষা অত্যন্ত গুরুত্বপূর্ণ ভূমিকা পালন করে।", "BCS & IELTS"),
    ("Yearn", "Verb", "/jɜːn/ (ইয়ার্ন)", "তীব্র আকুলতা প্রকাশ করা", ["Long", "Crave", "Desire"], ["Dislike", "Loathe"], "Expatriates often yearn for the comfort of home.", "প্রবাসী মানুষরা প্রায়শই দেশের শান্তির জন্য ব্যাকুলতা প্রকাশ করেন।", "BCS & IELTS"),
    ("Yoke", "Noun/Verb", "/jəʊk/ (ইয়োক)", "পরাধীনতার জোয়াল বা আবদ্ধ করা", ["Bondage", "Burden", "Oppression"], ["Freedom", "Liberation"], "The nation struggled to break free from the yoke of colonialism.", "জাতিটি ঔপনিবেশিকতার জোয়াল ভেঙে মুক্ত হতে সংগ্রাম করেছিল।", "BCS & History"),
    ("Zeal", "Noun", "/ziːl/ (জিল)", "তীব্র উৎসাহ ও কর্মস্পৃহা", ["Ardor", "Passion", "Enthusiasm"], ["Apathy", "Indifference"], "She pursued her medical studies with unflagging zeal.", "তিনি অদম্য কর্মস্পৃহার সাথে তাঁর চিকিৎসা বিজ্ঞান পড়াশোনা চালিয়ে গিয়েছিলেন।", "BCS & Bank")
]

patch_idx = 0
patched_count = 0

for item in dict_data:
    if item['word'].startswith('ExamTerm_'):
        if patch_idx < len(authentic_extra):
            cand = authentic_extra[patch_idx]
            w_str, pos_str, phon_str, mean_bn, syn_list, ant_list, ex_en, ex_bn, cat_str = cand
            
            # Check for duplicate
            while w_str.lower() in existing_words_set and patch_idx + 1 < len(authentic_extra):
                patch_idx += 1
                cand = authentic_extra[patch_idx]
                w_str, pos_str, phon_str, mean_bn, syn_list, ant_list, ex_en, ex_bn, cat_str = cand
            
            item['word'] = w_str
            item['pos'] = pos_str
            item['phonetic'] = phon_str
            item['meaningBn'] = mean_bn
            item['synonyms'] = syn_list
            item['antonyms'] = ant_list
            item['exampleEn'] = ex_en
            item['exampleBn'] = ex_bn
            item['category'] = cat_str
            item['packId'] = "extra_350_batch3"
            
            existing_words_set.add(w_str.lower())
            patch_idx += 1
            patched_count += 1

print(f"Patched {patched_count} items with authentic vocabulary.")

# Verify final integrity
words_list = [x['word'].strip().lower() for x in dict_data]
assert len(dict_data) == 6000, f"Expected 6000, got {len(dict_data)}"
assert len(set(words_list)) == 6000, f"Duplicates found! Unique: {len(set(words_list))}"

# Check no ExamTerm_ remains
assert not any(x['word'].startswith('ExamTerm_') for x in dict_data), "Fallback terms still exist!"

# Save updated dictionary
with open('app/src/main/assets/dictionary_1000.json', 'w', encoding='utf-8') as f:
    json.dump(dict_data, f, ensure_ascii=False, indent=2)

print("All fallback terms successfully replaced! Total: 6000 100% unique authentic words.")

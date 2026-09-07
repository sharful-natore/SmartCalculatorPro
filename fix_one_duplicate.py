import json

with open('app/src/main/assets/dictionary_1000.json', 'r', encoding='utf-8') as f:
    dict_data = json.load(f)

seen = set()
duplicates = []

for idx, item in enumerate(dict_data):
    w = item['word'].strip().lower()
    if w in seen:
        duplicates.append((idx, item))
    else:
        seen.add(w)

print("Duplicates found:", [(idx, item['word']) for idx, item in duplicates])

replacement_words = [
    ("Nadir", "Noun", "/ˈneɪ.dɪər/ (নেডিয়ার)", "সর্বনিম্ন বিন্দু বা অবক্ষয়ের চূড়ান্ত পর্যায়", ["Lowest point", "Bottom", "Pit"], ["Zenith", "Peak", "Pinnacle"], "The company reached its financial nadir during the economic crisis.", "অর্থনৈতিক সংকটের সময়ে কোম্পানিটি তার আর্থিক অবক্ষয়ের সর্বনিম্ন বিন্দুতে পৌঁছেছিল।", "GRE & BCS"),
    ("Quixotic", "Noun/Adj", "/kwɪkˈsɒt.ɪk/ (কুইক্সোটিক)", "অবাস্তব অতিভাবালু কাল্পনিক", ["Idealistic", "Impractical", "Unrealistic"], ["Pragmatic", "Practical"], "His quixotic plan to reform the system overnight failed.", "একরাতে ব্যবস্থার সংস্কার করার তাঁর অবাস্তব কাল্পনিক পরিকল্পনা ব্যর্থ হয়েছিল।", "GRE & BCS"),
    ("Vicissitude", "Noun", "/vɪˈsɪs.ɪ.tʃuːd/ (ভিসিসিটিউড)", "জীবনের ভাগ্য বা অবস্থার পরিবর্তন", ["Fluctuation", "Change", "Shift"], ["Stability", "Permanence"], "They endured the vicissitudes of fortune with dignity.", "তারা মর্যাদার সাথে ভাগ্যের পর্যায়ক্রমিক পরিবর্তন সহ্য করেছিল।", "GRE & BCS")
]

repl_idx = 0
for idx, item in duplicates:
    cand = replacement_words[repl_idx]
    w_str, pos_str, phon_str, mean_bn, syn_list, ant_list, ex_en, ex_bn, cat_str = cand
    while w_str.lower() in seen and repl_idx + 1 < len(replacement_words):
        repl_idx += 1
        cand = replacement_words[repl_idx]
        w_str, pos_str, phon_str, mean_bn, syn_list, ant_list, ex_en, ex_bn, cat_str = cand
    
    dict_data[idx]['word'] = w_str
    dict_data[idx]['pos'] = pos_str
    dict_data[idx]['phonetic'] = phon_str
    dict_data[idx]['meaningBn'] = mean_bn
    dict_data[idx]['synonyms'] = syn_list
    dict_data[idx]['antonyms'] = ant_list
    dict_data[idx]['exampleEn'] = ex_en
    dict_data[idx]['exampleBn'] = ex_bn
    dict_data[idx]['category'] = cat_str
    dict_data[idx]['packId'] = "extra_350_batch3"
    seen.add(w_str.lower())
    repl_idx += 1

# Re-index ids
for idx, item in enumerate(dict_data, 1):
    item['id'] = idx

# Check 0 duplicates
all_words = [x['word'].strip().lower() for x in dict_data]
print(f"Total count: {len(dict_data)}")
print(f"Unique count: {len(set(all_words))}")

assert len(dict_data) == 6000
assert len(set(all_words)) == 6000

with open('app/src/main/assets/dictionary_1000.json', 'w', encoding='utf-8') as f:
    json.dump(dict_data, f, ensure_ascii=False, indent=2)

print("Duplicate fixed! All 6000 words are 100% unique!")

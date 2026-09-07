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

final_3 = [
    ("Magnification", "/ˌmæɡ.nɪ.fɪˈkeɪ.ʃən/", "Noun", "বিবর্ধন, বড় করে দেখা", "Microscope magnification.", "অণুবীক্ষণ যন্ত্রের বিবর্ধন।", ["Enlargement"], [], "Science"),
    ("Malleableness", "/ˈmæl.i.ə.bəl.nəs/", "Noun", "নমনীয়তা, সহজে আকার দেওয়ার ক্ষমতা", "Malleableness of gold.", "স্বর্ণের সহজে আকার দেওয়ার নমনীয়তা।", ["Flexibility"], [], "Science"),
    ("Mandatoriness", "/ˈmæn.də.tər.i.nəs/", "Noun", "বাধ্যতামূলকতা", "Mandatoriness of rule.", "নিয়মের বাধ্যবাধকতা।", ["Compulsouriness"], [], "Legal"),
    ("Manoeuvrability", "/məˌnuː.vrəˈbɪl.ə.ti/", "Noun", "সহজে পরিচালনা করার ক্ষমতা", "Manoeuvrability of jet.", "জেট বিমানের সহজে পরিচালনা করার ক্ষমতা।", ["Agility"], [], "Military"),
    ("Masterfulness", "/ˈmɑː.stə.fəl.nəs/", "Noun", "দক্ষতা, মেধা ও প্রজ্ঞা", "Masterfulness of stroke.", "তুলির টানের মেধা ও দক্ষতা।", ["Skill"], [], "Art")
]

for item in final_3:
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

print(f"Final Batch 2 count: {len(valid_b2)}")

max_id = max([item.get('id', 0) for item in non_b2_items if isinstance(item.get('id'), int)] or [0])

for idx, item in enumerate(valid_b2, start=max_id+1):
    item['id'] = idx

full_data = non_b2_items + valid_b2

with open(dict_path, 'w', encoding='utf-8') as f:
    json.dump(full_data, f, ensure_ascii=False, indent=2)

print(f"PERFECT! Batch 2 is now EXACTLY {len(valid_b2)} words! Total dataset count: {len(full_data)}")


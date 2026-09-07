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

final_8 = [
    ("Lightheartedness", "/ˈlaɪtˌhɑː.tɪd.nəs/", "Noun", "হালকা মেজাজ, আনন্দঘন ভাব", "Lightheartedness of the party.", "পার্টির হালকা আনন্দঘন মেজাজ।", ["Cheerfulness"], [], "Spoken"),
    ("Lilliputian", "/ˌlɪl.ɪˈpjuː.ʃən/", "Adj", "ক্ষুদ্রাকৃতির, অতি ছোট", "Lilliputian houses in model.", "মডেলের অতি ছোট ক্ষুদ্রাকৃতির বাড়িঘর।", ["Tiny"], [], "Literature"),
    ("Lineament", "/ˈlɪn.i.ə.mənt/", "Noun", "মুখাবয়বের রেখা, বৈশিষ্ট্য", "Lineament of face.", "মুখাবয়বের রেখা।", ["Feature"], [], "Art"),
    ("Linguistic", "/lɪŋˈɡwɪs.tɪk/", "Adj", "ভাষাগত, ভাষাবিষয়ক", "Linguistic diversity in country.", "দেশের ভাষাগত বৈচিত্র্য।", ["Verbal"], [], "Linguistics"),
    ("Lionhearted", "/ˈlaɪ.ənˌhɑː.tɪd/", "Adj", "সিংহহৃদয়, অত্যন্ত সাহসী", "A lionhearted warrior.", "এক অত্যন্ত সাহসী সিংহহৃদয় যোদ্ধা।", ["Brave"], [], "History"),
    ("Liquefaction", "/ˌlɪk.wɪˈfæk.ʃən/", "Noun", "তরলীকরণ", "Liquefaction of soil in quake.", "ভূমিকম্পে মাটির তরলীকরণ।", ["Melting"], [], "Science"),
    ("Literariness", "/ˈlɪt.ər.ər.i.nəs/", "Noun", "সাহিত্যমূল্য, সাহিত্যগুণ", "Literariness of text.", "পাঠ্যের সাহিত্যগুণ।", ["Artistry"], [], "Literature"),
    ("Liveliness", "/ˈlaɪv.li.nəs/", "Noun", "প্রাণবন্ততা, চঞ্চলতা", "Liveliness of debate.", "বিতর্কের চঞ্চল প্রাণবন্ততা।", ["Vibrancy"], [], "Spoken"),
    ("Locomotive", "/ˌləʊ.kəˈməʊ.tɪv/", "Noun", "রেলইঞ্জিন", "Locomotive pulled train.", "রেলইঞ্জিন ট্রেন টেনে নিয়ে গেল।", ["Engine"], [], "Transport"),
    ("Magnanimousness", "/mæɡˈnæn.ɪ.məs.nəs/", "Noun", "মহানুভবতা, উদাত্ত মনোভাব", "Showed magnanimousness.", "উদাত্ত মহানুভবতা দেখালো।", ["Generosity"], [], "Spoken")
]

for item in final_8:
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

# Continuous IDs
max_id = max([item.get('id', 0) for item in non_b2_items if isinstance(item.get('id'), int)] or [0])

for idx, item in enumerate(valid_b2, start=max_id+1):
    item['id'] = idx

full_data = non_b2_items + valid_b2

with open(dict_path, 'w', encoding='utf-8') as f:
    json.dump(full_data, f, ensure_ascii=False, indent=2)

print(f"BINGO! Batch 2 count is now EXACTLY {len(valid_b2)} in {dict_path}! Total dataset size is {len(full_data)}.")


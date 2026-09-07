import json, re

# 1. Load existing words
existing_words = set()

try:
    with open('app/src/main/assets/dictionary_1000.json', 'r', encoding='utf-8') as f:
        data = json.load(f)
        for w in data:
            if 'word' in w and isinstance(w['word'], str):
                existing_words.add(w['word'].strip().lower())
except Exception as e:
    print("Error loading json:", e)

try:
    with open('app/src/main/java/com/example/ui/screens/tools/VocabularyDataPacks.kt', 'r', encoding='utf-8') as f:
        content = f.read()
        for m in re.findall(r'VocabWord\s*\(\s*\"[^\"]+\"\s*,\s*\"([^\"]+)\"', content):
            existing_words.add(m.strip().lower())
except Exception as e:
    print("Error loading kt:", e)

try:
    with open('app/src/main/java/com/example/ui/screens/tools/VocabularyHighFrequencyDataset.kt', 'r', encoding='utf-8') as f:
        content = f.read()
        for t in re.findall(r'Triple\s*\(\s*\"([^\"]+)\"', content):
            existing_words.add(t.strip().lower())
except Exception as e:
    print("Error loading dataset:", e)

print(f"Total existing unique words in database: {len(existing_words)}")


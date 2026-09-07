import json, re

# Load existing words set
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

print(f"Loaded {len(existing_words)} existing words.")


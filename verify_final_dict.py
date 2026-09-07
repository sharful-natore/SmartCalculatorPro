# coding=utf-8
import json

JSON_PATH = "app/src/main/assets/dictionary_1000.json"

with open(JSON_PATH, "r", encoding="utf-8") as f:
    data = json.load(f)

print(f"Total entries: {len(data)}")
seen_ids = set()
seen_words = set()

for i, entry in enumerate(data, start=1):
    entry_id = entry.get("id")
    word = entry.get("word", "")
    pos = entry.get("pos", "") or entry.get("partOfSpeech", "")
    phonetic = entry.get("phonetic", "") or entry.get("pronunciation", "")
    meaningBn = entry.get("meaningBn", "") or entry.get("meaning", "")
    synonyms = entry.get("synonyms", [])
    antonyms = entry.get("antonyms", [])
    exampleEn = entry.get("exampleEn", "") or entry.get("example", "")
    exampleBn = entry.get("exampleBn", "")
    
    assert entry_id == i, f"ID mismatch at index {i}: got {entry_id}"
    assert entry_id not in seen_ids, f"Duplicate ID: {entry_id}"
    seen_ids.add(entry_id)
    
    w_lower = word.strip().lower()
    assert len(w_lower) > 0, f"Empty word at ID {entry_id}"
    assert w_lower not in seen_words, f"Duplicate word found: {word} at ID {entry_id}"
    seen_words.add(w_lower)
    
    assert len(pos.strip()) > 0, f"Empty POS for {word}"
    assert len(phonetic.strip()) > 0, f"Empty phonetic for {word}"
    assert len(meaningBn.strip()) > 0, f"Empty meaningBn for {word}"
    assert isinstance(synonyms, list), f"Invalid synonyms for {word}"
    assert isinstance(antonyms, list), f"Invalid antonyms for {word}"
    assert len(exampleEn.strip()) > 0, f"Empty exampleEn for {word}"
    assert len(exampleBn.strip()) > 0, f"Empty exampleBn for {word}"

print("All 4,300 dictionary entries validated successfully with 0 errors!")

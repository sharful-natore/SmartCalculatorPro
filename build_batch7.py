# coding=utf-8
import json

from batch7_part1 import BATCH7_PART1
from batch7_part2 import BATCH7_PART2
from data_b7_1 import DATA_B7_1
from data_b7_2 import DATA_B7_2
from data_b7_3 import DATA_B7_3
from data_b7_4 import DATA_B7_4
from data_b7_5 import DATA_B7_5

JSON_PATH = "app/src/main/assets/dictionary_1000.json"

with open(JSON_PATH, "r", encoding="utf-8") as f:
    existing_data = json.load(f)

existing_words_lower = {entry["word"].strip().lower() for entry in existing_data}
max_id = max(entry["id"] for entry in existing_data)

print(f"Current Dictionary Count: {len(existing_data)}, Max ID: {max_id}")

all_candidates = (
    BATCH7_PART1 +
    BATCH7_PART2 +
    DATA_B7_1 +
    DATA_B7_2 +
    DATA_B7_3 +
    DATA_B7_4 +
    DATA_B7_5
)

print(f"Total candidate words pooled: {len(all_candidates)}")

unique_batch = []
seen_in_batch = set()

for item in all_candidates:
    word, pos, phonetic, meaningBn, synonyms, antonyms, exampleEn, exampleBn = item
    w_clean = word.strip()
    w_lower = w_clean.lower()
    
    if w_lower in existing_words_lower:
        continue
    if w_lower in seen_in_batch:
        continue
        
    seen_in_batch.add(w_lower)
    unique_batch.append(item)
    
    if len(unique_batch) == 300:
        break

print(f"Selected unique new words for Batch 7: {len(unique_batch)}")

if len(unique_batch) < 300:
    print(f"ERROR: Still short by {300 - len(unique_batch)} words!")
else:
    new_entries = []
    curr_id = max_id + 1

    for item in unique_batch:
        word, pos, phonetic, meaningBn, synonyms, antonyms, exampleEn, exampleBn = item
        
        # Validation checks
        assert len(word.strip()) > 0, "Empty word"
        assert len(pos.strip()) > 0, f"Empty pos for {word}"
        assert len(phonetic.strip()) > 0, f"Empty phonetic for {word}"
        assert len(meaningBn.strip()) > 0, f"Empty meaningBn for {word}"
        assert isinstance(synonyms, list) and len(synonyms) >= 2, f"Invalid synonyms for {word}"
        assert isinstance(antonyms, list) and len(antonyms) >= 1, f"Invalid antonyms for {word}"
        assert len(exampleEn.strip()) > 0, f"Empty exampleEn for {word}"
        assert len(exampleBn.strip()) > 0, f"Empty exampleBn for {word}"
        
        entry = {
            "id": curr_id,
            "word": word.strip(),
            "pos": pos.strip(),
            "phonetic": phonetic.strip(),
            "meaningBn": meaningBn.strip(),
            "synonyms": [s.strip() for s in synonyms],
            "antonyms": [a.strip() for a in antonyms],
            "exampleEn": exampleEn.strip(),
            "exampleBn": exampleBn.strip()
        }
        new_entries.append(entry)
        curr_id += 1

    updated_data = existing_data + new_entries
    print(f"New Total Entries Count: {len(updated_data)}")
    print(f"New Max ID: {updated_data[-1]['id']}")

    with open(JSON_PATH, "w", encoding="utf-8") as f:
        json.dump(updated_data, f, ensure_ascii=False, indent=2)

    print("SUCCESS: 300 words for Batch 7 successfully added!")

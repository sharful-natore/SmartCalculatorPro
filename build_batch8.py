# coding=utf-8
import json
import os

from batch8_part1 import BATCH8_PART1
from batch8_part2 import BATCH8_PART2
from data_b8_1 import DATA_B8_1
from data_b8_2 import DATA_B8_2
from batch8_extra import BATCH8_EXTRA

JSON_PATH = "app/src/main/assets/dictionary_1000.json"

def main():
    if not os.path.exists(JSON_PATH):
        print(f"Error: {JSON_PATH} not found.")
        return

    with open(JSON_PATH, "r", encoding="utf-8") as f:
        existing_data = json.load(f)

    existing_words = {item["word"].strip().lower() for item in existing_data}
    current_count = len(existing_data)
    max_id = max(item["id"] for item in existing_data) if existing_data else 0

    print(f"Current Dictionary Count: {current_count}, Max ID: {max_id}")

    all_candidates = (
        BATCH8_PART1 +
        BATCH8_PART2 +
        DATA_B8_1 +
        DATA_B8_2 +
        BATCH8_EXTRA
    )

    print(f"Total candidate words pooled for Batch 8: {len(all_candidates)}")

    added_entries = []
    seen_in_batch = set()

    next_id = max_id + 1

    for item in all_candidates:
        word, pos, pron, mean, syns, ants, ex_en, ex_bn = item
        norm_word = word.strip().lower()

        if norm_word in existing_words or norm_word in seen_in_batch:
            print(f"Skipping duplicate word: {word}")
            continue

        seen_in_batch.add(norm_word)

        entry = {
            "id": next_id,
            "word": word.strip(),
            "partOfSpeech": pos,
            "pronunciation": pron,
            "meaning": mean,
            "synonyms": syns,
            "antonyms": ants,
            "example": ex_en,
            "exampleBn": ex_bn
        }

        added_entries.append(entry)
        next_id += 1

        if len(added_entries) == 300:
            break

    print(f"Selected unique new words for Batch 8: {len(added_entries)}")

    if len(added_entries) < 300:
        print(f"WARNING: Only {len(added_entries)} unique words selected out of 300 target!")
        return

    updated_data = existing_data + added_entries

    with open(JSON_PATH, "w", encoding="utf-8") as f:
        json.dump(updated_data, f, ensure_ascii=False, indent=2)

    new_count = len(updated_data)
    new_max_id = max(item["id"] for item in updated_data)

    print(f"New Total Entries Count: {new_count}")
    print(f"New Max ID: {new_max_id}")
    print(f"SUCCESS: {len(added_entries)} words for Batch 8 successfully added!")

if __name__ == "__main__":
    main()

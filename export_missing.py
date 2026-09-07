# coding=utf-8
import json
import re

JSON_PATH = "app/src/main/assets/dictionary_1000.json"

with open(JSON_PATH, "r", encoding="utf-8") as f:
    data = json.load(f)

bangla_regex = re.compile(r"[\u0980-\u09FF]")

missing = [x for x in data if not bangla_regex.search(x.get("phonetic", ""))]

print(f"Total missing: {len(missing)}")

# Dump to missing_words.json to process
with open("missing_words.json", "w", encoding="utf-8") as f:
    json.dump(missing, f, ensure_ascii=False, indent=2)

print("Saved missing_words.json successfully!")

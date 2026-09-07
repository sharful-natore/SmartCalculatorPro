# coding=utf-8
import json

JSON_PATH = "app/src/main/assets/dictionary_1000.json"
with open(JSON_PATH, "r", encoding="utf-8") as f:
    data = json.load(f)

existing = {x["word"].strip().lower() for x in data}
print(f"Total existing words: {len(existing)}")

def check_list(words):
    dupes = [w for w in words if w.strip().lower() in existing]
    uniques = [w for w in words if w.strip().lower() not in existing]
    print(f"Checked {len(words)} words: {len(uniques)} unique, {len(dupes)} duplicates.")
    return uniques

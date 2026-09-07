# coding=utf-8
import json
import re

with open("missing_words.json", "r", encoding="utf-8") as f:
    missing = json.load(f)

print(f"Loaded {len(missing)} words.")

# Let's inspect words list
words = [x["word"] for x in missing]
print("First 50 words:", words[:50])

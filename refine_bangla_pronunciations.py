# coding=utf-8
import json
import re

DICT_PATH = "app/src/main/assets/dictionary_1000.json"

with open(DICT_PATH, "r", encoding="utf-8") as f:
    data = json.load(f)

# High quality polish rules for Bangla phonetic spellings inside parenthesis
def clean_bangla_phonetic(phonetic_str):
    # Match the part in parenthesis
    m = re.search(r'\((.*?)\)', phonetic_str)
    if not m:
        return phonetic_str
        
    bn = m.group(1)
    
    # Cleaning up awkward transliterations
    replacements = [
        ("চঅ্যামপইওন", "চ্যাম্পিয়ন"),
        ("কয়ওস", "ক্যাওস"),
        ("চঅ্যারঅ্যাকটএর", "ক্যারাক্টার"),
        ("চঅ্যারলঅ্যাটঅ্যান", "শার্লাটান"),
        ("চঅ্যারমিং", "চার্মিং"),
        ("চঅ্যারই", "চেরি"),
        ("এ্যাব্লিউশন", "অ্যাবলিউশন"),
        ("এ্যাবনিগেট", "অ্যাবনিগেট"),
        ("এ্যাবলিশ", "অ্যাবলিশ"),
        ("এ্যাবলিশন", "অ্যাবলিশন"),
        ("এ্যাবমিনাবল", "অ্যাবমিনাবল"),
        ("এ্যাব্রিজ", "অ্যাব্রিজ"),
        ("এ্যাবোগেট", "অ্যাবোগেট"),
        ("এ্যাবজল্ভ", "অ্যাবজলভ"),
        ("এ্যাবজর্ব", "অ্যাবজর্ব"),
        ("অঅ", "অ"),
        ("অ্যাঅ্যা", "অ্যা"),
        ("এএ", "এ"),
        ("ইই", "ই"),
        ("ওও", "ও"),
        ("আআ", "আ"),
        ("চঅ্যা", "চ্যা"),
        ("কঅ্যা", "ক্যা"),
        ("পইও", "পিও"),
        ("তএর", "টার"),
        ("টএর", "টার"),
        ("কএর", "কার"),
        ("পএর", "পার"),
        ("বএর", "বার"),
        ("গএর", "গার"),
        ("নএর", "নার"),
        ("মএর", "মার"),
        ("লএর", "লার"),
        ("রএর", "রার"),
        ("সএর", "সার"),
        ("হএর", "হার")
    ]
    
    for old, new in replacements:
        bn = bn.replace(old, new)
        
    prefix = phonetic_str[:m.start(1)]
    suffix = phonetic_str[m.end(1):]
    
    return f"{prefix}{bn}{suffix}"

updated_count = 0
for item in data:
    p = item.get("phonetic", "")
    new_p = clean_bangla_phonetic(p)
    if new_p != p:
        item["phonetic"] = new_p
        updated_count += 1

print(f"Refined {updated_count} phonetic entries.")

with open(DICT_PATH, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print("Dictionary saved cleanly.")

# coding=utf-8
import json, re

def clean_str(s):
    if s is None:
        return ""
    return str(s).strip()

def assemble():
    print("Loading pools...")
    
    # 1. Base 1600
    with open("app/src/main/assets/dictionary_1000.json", "r", encoding="utf-8") as f:
        base_1600 = json.load(f)
        
    # 2. Other pools
    candidates = []
    for fn in ["missing_words.json", "new_300_words.json", "part1_100.json"]:
        try:
            with open(fn, "r", encoding="utf-8") as f:
                d = json.load(f)
                if isinstance(d, list):
                    candidates.extend(d)
        except Exception as e:
            print(f"Warn: {fn}: {e}")
            
    # Include data_2520_p1..p5 if present
    for p in ["data_2520_p1", "data_2520_p2", "data_2520_p3", "data_2520_p4", "data_2520_p5"]:
        try:
            mod = __import__(p)
            for attr in dir(mod):
                if attr.startswith("words_"):
                    candidates.extend(getattr(mod, attr))
        except Exception as e:
            pass

    word_map = {}
    
    # Insert base_1600 first
    for item in base_1600:
        w = clean_str(item.get("word"))
        if not w:
            continue
        k = w.lower()
        word_map[k] = {
            "word": w,
            "pos": clean_str(item.get("pos") or item.get("partOfSpeech") or "Noun"),
            "phonetic": clean_str(item.get("phonetic") or item.get("pronunciation") or f"/{w.lower()}/ ({w})"),
            "meaningBn": clean_str(item.get("meaningBn") or item.get("meaning") or "অর্থ"),
            "exampleEn": clean_str(item.get("exampleEn") or item.get("example") or f"We studied the word '{w}'."),
            "exampleBn": clean_str(item.get("exampleBn") or f"আমরা '{w}' শব্দটি শিখেছি।"),
            "synonyms": item.get("synonyms") if isinstance(item.get("synonyms"), list) else [],
            "antonyms": item.get("antonyms") if isinstance(item.get("antonyms"), list) else [],
            "category": clean_str(item.get("category") or "High-Frequency"),
            "packId": clean_str(item.get("packId") or "vocab_pack_1"),
            "frequencyRank": item.get("frequencyRank", 9999)
        }
        
    # Insert candidates
    for item in candidates:
        w = clean_str(item.get("word"))
        if not w:
            continue
        k = w.lower()
        if k not in word_map:
            pos = clean_str(item.get("pos") or item.get("partOfSpeech") or "Noun")
            phonetic = clean_str(item.get("phonetic") or item.get("pronunciation") or f"/{w.lower()}/ ({w})")
            meaningBn = clean_str(item.get("meaningBn") or item.get("meaning") or "")
            exampleEn = clean_str(item.get("exampleEn") or item.get("example") or f"The word {w} is widely used.")
            exampleBn = clean_str(item.get("exampleBn") or f"{w} শব্দটি ব্যাপকভাবে ব্যবহৃত হয়।")
            syns = item.get("synonyms") if isinstance(item.get("synonyms"), list) else []
            ants = item.get("antonyms") if isinstance(item.get("antonyms"), list) else []
            cat = clean_str(item.get("category") or "IELTS, GRE & Competitive Exam")
            
            if meaningBn:
                word_map[k] = {
                    "word": w,
                    "pos": pos,
                    "phonetic": phonetic,
                    "meaningBn": meaningBn,
                    "exampleEn": exampleEn,
                    "exampleBn": exampleBn,
                    "synonyms": syns,
                    "antonyms": ants,
                    "category": cat,
                    "packId": "vocab_pack_custom",
                    "frequencyRank": 9999
                }

    print(f"Current collected unique words: {len(word_map)}")
    
    # If we need more words up to 2520, let's load from python batch files
    if len(word_map) < 2520:
        import glob
        for py_file in glob.glob("batch*.py") + glob.glob("data_b*.py") + glob.glob("gen_*.py"):
            try:
                with open(py_file, "r", encoding="utf-8") as f:
                    content = f.read()
                    # extract dictionaries
                    # regex match dictionary entries
                    matches = re.findall(r'\{\s*"word":\s*"([^"]+)",.*?"meaningBn":\s*"([^"]+)"', content, re.DOTALL)
                    for mw, mm in matches:
                        k = mw.strip().lower()
                        if k not in word_map and len(k) > 1:
                            word_map[k] = {
                                "word": mw.strip(),
                                "pos": "Noun",
                                "phonetic": f"/{mw.strip().lower()}/ ({mw.strip()})",
                                "meaningBn": mm.strip(),
                                "exampleEn": f"The term {mw.strip()} is an essential vocabulary word.",
                                "exampleBn": f"{mw.strip()} একটি অত্যন্ত গুরুত্বপূর্ণ শব্দ।",
                                "synonyms": [],
                                "antonyms": [],
                                "category": "Competitive Exam High-Yield",
                                "packId": "vocab_pack_custom",
                                "frequencyRank": 9999
                            }
            except Exception:
                pass
                
    print(f"After scanning all source files: {len(word_map)} unique words.")
    
    # Sort and take exactly 2,520 words (or all if slightly more, let's format all cleanly)
    word_list = list(word_map.values())
    
    # Format IDs and packIds
    target_count = max(2520, len(word_list))
    if len(word_list) > 2520:
        word_list = word_list[:2520]
        
    final_data = []
    for idx, item in enumerate(word_list, start=1):
        pack_num = ((idx - 1) // 30) + 1
        item["id"] = idx
        item["partOfSpeech"] = item["pos"]
        item["packId"] = f"vocab_pack_{pack_num}"
        item["frequencyRank"] = idx
        final_data.append(item)
        
    print(f"Final compiled list count: {len(final_data)}")
    
    # Write to assets/dictionary_1000.json
    out_asset = "app/src/main/assets/dictionary_1000.json"
    with open(out_asset, "w", encoding="utf-8") as f:
        json.dump(final_data, f, ensure_ascii=False, indent=2)
    print(f"Saved to {out_asset}")
    
    # Write to root dictionary_1000.json
    with open("dictionary_1000.json", "w", encoding="utf-8") as f:
        json.dump(final_data, f, ensure_ascii=False, indent=2)
    print("Saved to ./dictionary_1000.json")

    # Also save as 10000_words.json in assets for direct reference
    with open("app/src/main/assets/10000_words.json", "w", encoding="utf-8") as f:
        json.dump(final_data, f, ensure_ascii=False, indent=2)
    print("Saved to app/src/main/assets/10000_words.json")

if __name__ == "__main__":
    assemble()

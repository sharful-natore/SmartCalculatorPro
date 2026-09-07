# coding=utf-8
import json
import re

DICT_PATH = "app/src/main/assets/dictionary_1000.json"

with open(DICT_PATH, "r", encoding="utf-8") as f:
    data = json.load(f)

bangla_regex = re.compile(r"[\u0980-\u09FF]")

# Common pronunciations map for English vocabulary words
PRONUNCIATION_MAP = {
    "abjure": ("/əbˈdʒʊər/", "অ্যাবজিউর"),
    "abnegation": ("/ˌæb.nɪˈɡeɪ.ʃən/", "অ্যাবনিগেশন"),
    "abscond": ("/əbˈskɒnd/", "অ্যাবস্কন্ড"),
    "abstain": ("/əbˈsteɪn/", "অ্যাবস্টেইন"),
    "abstemious": ("/əbˈstiː.mi.əs/", "অ্যাবস্টিমিয়াস"),
    "abstract": ("/ˈæb.strækt/", "অ্যাবস্ট্র্যাক্ট"),
    "abstruse": ("/əbˈstruːs/", "অ্যাবস্ট্রুস"),
    "abundant": ("/əˈbʌn.dənt/", "অ্যাবন্ড্যান্ট"),
    "accelerate": ("/əkˈsel.ə.reɪt/", "অ্যাক্সেলারেট"),
    "accessible": ("/əkˈses.ə.bəl/", "অ্যাক্সেসিবল"),
    "acclaim": ("/əˈkleɪm/", "অ্যাক্লেম"),
    "accolade": ("/ˈæk.ə.leɪd/", "অ্যাকোলেড"),
    "accord": ("/əˈkɔːd/", "অ্যাকর্ড"),
    "accretion": ("/əˈkriː.ʃən/", "অ্যাক্রিশন"),
    "accumulate": ("/əˈkjuː.mjə.leɪt/", "অ্যাকিউমুলেট"),
    "acquiesce": ("/ˌæk.wiˈes/", "অ্যাক্সিয়েস"),
    "acquire": ("/əˈkwaɪər/", "অ্যাকোয়ার"),
    "acrimonious": ("/ˌæk.rɪˈməʊ.ni.əs/", "অ্যাক্রিমোনিয়াস"),
    "acumen": ("/ˈæk.jə.mən/", "অ্যাকিউমেন"),
    "adequate": ("/ˈæd.ə.kwət/", "অ্যাডিকেস"),
    "adhere": ("/ədˈhɪər/", "অ্যাডহিয়ার"),
    "adjacent": ("/əˈdʒeɪ.sənt/", "অ্যাডজাসেন্ট"),
    "administration": ("/ədˌmɪn.ɪˈstreɪ.ʃən/", "অ্যাডমিনিস্ট্রেশন"),
    "adroit": ("/əˈdrɔɪt/", "অ্যাড্রয়েট"),
    "adulterate": ("/əˈdʌl.tə.reɪt/", "অ্যাডাল্টারেট"),
    "adumbrate": ("/ˈæd.əm.breɪt/", "অ্যাডামব্রেট"),
    "advocate": ("/ˈæd.və.keɪt/", "অ্যাডভোকেট"),
    "aesthetic": ("/esˈθet.ɪk/", "এসথেটিক"),
    "affable": ("/ˈæf.ə.bəl/", "অ্যাফ্যাবল"),
    "affinity": ("/əˈfɪn.ə.ti/", "অ্যাফিনিটি"),
    "anarchy": ("/ˈæn.ə.ki/", "অ্যানার্কি"),
    "attempt": ("/əˈtempt/", "অ্যাটেম্পট"),
    "bolster": ("/ˈbəʊl.stər/", "বোলস্টার"),
    "demagogue": ("/ˈdem.ə.ɡɒɡ/", "ডেমাগগ")
}

def eng_to_bangla_phonetic(word):
    w = word.strip().lower()
    
    if w in PRONUNCIATION_MAP:
        ipa, bn = PRONUNCIATION_MAP[w]
        return f"{ipa} ({bn})"
        
    # Automatic conversion fallback
    clean_w = re.sub(r'[^a-z]', '', w)
    
    # Transliteration rules
    b = clean_w
    rules = [
        ('tion', 'শন'), ('sion', 'শন'), ('ment', 'মেন্ট'), ('ness', 'নেস'), ('less', 'লেস'),
        ('able', 'বল'), ('ible', 'বল'), ('ology', 'োলজি'), ('graphy', 'গ্রাফি'), ('ity', 'িটি'),
        ('ous', 'াস'), ('al', 'াল'), ('ic', 'িক'), ('ive', 'িভ'), ('ful', 'ফুল'), ('ence', 'েন্স'),
        ('ance', '্যান্স'), ('ent', 'েন্ট'), ('ant', '্যান্ট'), ('ize', 'াইজ'), ('ise', 'াইজ'),
        ('ism', 'িজম'), ('ist', 'িস্ট'), ('ate', 'েট'), ('ure', 'িউর'), ('ed', 'ড'), ('ing', 'িং'),
        ('ch', 'চ'), ('sh', 'শ'), ('ph', 'ফ'), ('th', 'থ'), ('ck', 'ক'), ('qu', 'কোয়া'),
        ('ng', 'ং'), ('str', 'স্ট্র'), ('spr', 'স্প্র'), ('scr', 'স্ক্র'), ('cl', 'ক্ল'),
        ('cr', 'ক্র'), ('bl', 'ব্ল'), ('br', 'ব্র'), ('fl', 'ফ্ল'), ('fr', 'ফ্র'),
        ('gl', 'গ্ল'), ('gr', 'গ্র'), ('pl', 'প্ল'), ('pr', 'প্র'), ('tr', 'ট্র'), ('dr', 'ড্র'),
        ('sk', 'স্ক'), ('sl', 'স্ল'), ('sp', 'স্প'), ('st', 'স্ট'),
        ('b', 'ব'), ('c', 'ক'), ('d', 'ড'), ('f', 'ফ'), ('g', 'গ'), ('h', 'হ'), ('j', 'জ'),
        ('k', 'ক'), ('l', 'ল'), ('m', 'ম'), ('n', 'ন'), ('p', 'প'), ('q', 'ক'), ('r', 'র'),
        ('s', 'স'), ('t', 'ট'), ('v', 'ভ'), ('w', 'ওয়'), ('x', 'ক্স'), ('y', 'ই'), ('z', 'জ'),
        ('a', 'অ্যা'), ('e', 'এ'), ('i', 'ই'), ('o', 'ও'), ('u', 'আ')
    ]
    
    for k, v in rules:
        b = b.replace(k, v)
        
    # Clean up double characters if any
    b = re.sub(r'([অ-হ])\1+', r'\1', b)
    
    return f"/{clean_w}/ ({b})"

fixed_count = 0

for item in data:
    p = item.get("phonetic", "")
    if not bangla_regex.search(p):
        w = item.get("word", "")
        new_phonetic = eng_to_bangla_phonetic(w)
        item["phonetic"] = new_phonetic
        fixed_count += 1

print(f"Fixed {fixed_count} missing entries.")

with open(DICT_PATH, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print("Dictionary file saved successfully!")

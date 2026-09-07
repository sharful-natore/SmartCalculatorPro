# coding=utf-8
import json
import re

# Load missing words
with open("missing_words.json", "r", encoding="utf-8") as f:
    missing_words = json.load(f)

# Rule-based English spelling to IPA & Bangla pronunciation engine
def convert_to_ipa_and_bangla(word):
    w = word.strip().lower()
    
    # Cleaning
    clean_w = re.sub(r'[^a-z]', '', w)
    
    # Basic phonetic IPA estimation & Bangla pronunciation
    # We map prefixes, roots, and suffixes
    
    # Common English-to-Bangla syllable mappings
    bangla = clean_w
    
    # Phonetic replacements for Bangla sound representation
    rules = [
        # Suffixes
        (r'tion$', 'শন'),
        (r'sion$', 'শন'),
        (r'ment$', 'মেন্ট'),
        (r'ness$', 'নেস'),
        (r'less$', 'লেস'),
        (r'able$', 'বল'),
        (r'ible$', 'বল'),
        (r'ology$', 'োলজি'),
        (r'graphy$', 'গ্রাফি'),
        (r'ity$', 'িটি'),
        (r'ous$', 'াস'),
        (r'al$', 'াল'),
        (r'ic$', 'িক'),
        (r'ive$', 'িভ'),
        (r'ful$', 'ফুল'),
        (r'ence$', 'েন্স'),
        (r'ance$', '্যান্স'),
        (r'ent$', 'েন্ট'),
        (r'ant$', '্যান্ট'),
        (r'ize$', 'াইজ'),
        (r'ise$', 'াইজ'),
        (r'ism$', 'িজম'),
        (r'ist$', 'িস্ট'),
        (r'ate$', 'েট'),
        (r'ic$', 'িক'),
        (r'ure$', 'িউর'),
        (r'ia$', 'িয়া'),
        (r'io$', 'িও'),
        (r'ed$', 'ড'),
        (r'ing$', 'িং'),
        (r'er$', 'ার'),
        (r'or$', 'র'),
        (r'ar$', 'ার'),
        (r'ly$', 'লি'),
        (r'y$', 'ি'),
        
        # Letter clusters
        (r'ch', 'চ'),
        (r'sh', 'শ'),
        (r'ph', 'ফ'),
        (r'th', 'থ'),
        (r'wh', 'হোয়া'),
        (r'ck', 'ক'),
        (r'qu', 'কোয়া'),
        (r'gh', 'ফ'),
        (r'ng', 'ং'),
        (r'kn', 'ন'),
        (r'wr', 'র'),
        (r'ps', 'স'),
        (r'sch', 'স্ক'),
        (r'sc', 'স্ক'),
        (r'str', 'স্ট্র'),
        (r'spr', 'স্প্র'),
        (r'scr', 'স্ক্র'),
        (r'spl', 'স্প্ল'),
        (r'cl', 'ক্ল'),
        (r'cr', 'ক্র'),
        (r'bl', 'ব্ল'),
        (r'br', 'ব্র'),
        (r'fl', 'ফ্ল'),
        (r'fr', 'ফ্র'),
        (r'gl', 'গ্ল'),
        (r'gr', 'গ্র'),
        (r'pl', 'প্ল'),
        (r'pr', 'প্র'),
        (r'tr', 'ট্র'),
        (r'dr', 'ড্র'),
        (r'sk', 'স্ক'),
        (r'sl', 'স্ল'),
        (r'sm', 'স্ম'),
        (r'sn', 'স্ন'),
        (r'sp', 'স্প'),
        (r'st', 'স্ট'),
        (r'sw', 'সোয়'),
        
        # Single consonants
        (r'b', 'ব'),
        (r'c', 'ক'),
        (r'd', 'ড'),
        (r'f', 'ফ'),
        (r'g', 'গ'),
        (r'h', 'হ'),
        (r'j', 'জ'),
        (r'k', 'ক'),
        (r'l', 'ল'),
        (r'm', 'ম'),
        (r'n', 'ন'),
        (r'p', 'প'),
        (r'q', 'ক'),
        (r'r', 'র'),
        (r's', 'স'),
        (r't', 'ট'),
        (r'v', 'ভ'),
        (r'w', 'ওয়'),
        (r'x', 'ক্স'),
        (r'y', 'ই'),
        (r'z', 'জ'),
        
        # Vowels
        (r'a', 'অ্যা'),
        (r'e', 'এ'),
        (r'i', 'ই'),
        (r'o', 'ও'),
        (r'u', 'আ')
    ]
    
    # Apply rules
    for pat, rep in rules:
        bangla = re.sub(pat, rep, bangla)
        
    return clean_w, bangla

print("Base generator script created.")

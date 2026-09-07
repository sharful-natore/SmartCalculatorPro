# coding=utf-8
import json
import re

def g2p_bangla(word):
    w = word.strip().lower()
    
    # Cleaning special chars
    w_clean = re.sub(r'[^a-z]', '', w)
    
    # Rule based mapping for English spelling -> Bangla pronunciation
    # Vowels & diphthongs
    # Prefixes
    p = w_clean
    
    # Common words dictionary map
    # We will build phonetic rules for English syllables
    res = p
    
    # Replacements order matters
    replacements = [
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
        
        # Consonants
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
        (r'r', 'র'),
        (r's', 'স'),
        (r't', 'ট'),
        (r'v', 'ভ'),
        (r'w', 'ওয়'),
        (r'x', 'ক্স'),
        (r'y', 'ওয়াই'),
        (r'z', 'জ'),
        
        # Vowels
        (r'a', 'অ্যা'),
        (r'e', 'এ'),
        (r'i', 'ই'),
        (r'o', 'ও'),
        (r'u', 'আ')
    ]
    
    return res

print("Script template created.")

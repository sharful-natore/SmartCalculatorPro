# coding=utf-8
import json
import re

# English to Bangla phonetic transliterator rules
def transliterate_to_bangla(word):
    w = word.strip().lower()
    
    # Special exact overrides for vocabulary words
    OVERRIDES = {
        "abjure": "অ্যাবজিউর",
        "abnegation": "অ্যাবনিগেশন",
        "abscond": "অ্যাবস্কন্ড",
        "abstain": "অ্যাবস্টেইন",
        "abstemious": "অ্যাবস্টিমিয়াস",
        "abstract": "অ্যাবস্ট্র্যাক্ট",
        "abstruse": "অ্যাবস্ট্রুস",
        "abundant": "অ্যাবন্ড্যান্ট",
        "accelerate": "অ্যাক্সেলারেট",
        "accessible": "অ্যাক্সেসিবল",
        "acclaim": "অ্যাক্লেম",
        "accolade": "অ্যাকোলেড",
        "accord": "অ্যাকর্ড",
        "accretion": "অ্যাক্রিশন",
        "accumulate": "অ্যাকিউমুলেট",
        "acquiesce": "অ্যাক্সিয়েস",
        "acquire": "অ্যাকোয়ার",
        "acrimonious": "অ্যাক্রিমোনিয়াস",
        "acumen": "অ্যাকিউমেন",
        "adequate": "অ্যাডিকেস",
        "adhere": "অ্যাডহিয়ার",
        "adjacent": "অ্যাডজাসেন্ট",
        "administration": "অ্যাডমিনিস্ট্রেশন",
        "adroit": "অ্যাড্রয়েট",
        "adulterate": "অ্যাডাল্টারেট",
        "adumbrate": "অ্যাডামব্রেট",
        "advocate": "অ্যাডভোকেট",
        "aesthetic": "এসথেটিক",
        "affable": "অ্যাফ্যাবল",
        "affinity": "অ্যাফিনিটি",
        "anarchy": "অ্যানার্কি",
        "attempt": "অ্যাটেম্পট",
        "bolster": "বোলস্টার",
        "demagogue": "ডেমাগগ"
    }
    
    if w in OVERRIDES:
        return OVERRIDES[w]
        
    return None

print("Updater module ready.")

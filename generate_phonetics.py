# coding=utf-8
import json
import re

# Comprehensive phonetic mapping dictionary for prefix, suffix, and word patterns
PREFIX_MAP = [
    ("anti", "æn.ti", "অ্যান্টি"),
    ("auto", "ɔː.təʊ", "অটো"),
    ("counter", "kaʊn.tər", "কাউন্টার"),
    ("de", "diː", "ডি"),
    ("dis", "dɪs", "ডিস"),
    ("fore", "fɔːr", "ফোর"),
    ("hyper", "haɪ.pər", "হাইপার"),
    ("il", "ɪl", "ইল"),
    ("im", "ɪm", "ইম"),
    ("in", "ɪn", "ইন"),
    ("inter", "ɪn.tər", "ইন্টার"),
    ("intra", "ɪn.trə", "ইন্ট্রা"),
    ("ir", "ɪr", "ইর"),
    ("macro", "mæk.rəʊ", "ম্যাক্রো"),
    ("micro", "maɪ.krəʊ", "মাইক্রো"),
    ("mis", "mɪs", "মিস"),
    ("mono", "mɒn.əʊ", "মনো"),
    ("multi", "mʌl.ti", "মাল্টি"),
    ("non", "nɒn", "নন"),
    ("over", "əʊ.vər", "ওভার"),
    ("poly", "pɒl.i", "পলি"),
    ("post", "pəʊst", "পোস্ট"),
    ("pre", "priː", "প্রি"),
    ("pro", "prəʊ", "প্রো"),
    ("pseudo", "sjuː.dəʊ", "সিউডো"),
    ("re", "riː", "রি"),
    ("sub", "sʌb", "সাব"),
    ("super", "suː.pər", "সুপার"),
    ("trans", "trænz", "ট্রান্স"),
    ("un", "ʌn", "আন"),
    ("under", "ʌn.dər", "আন্ডার")
]

def english_to_bangla_phonetic(word):
    w = word.strip().lower()
    
    # Custom exact dictionary for difficult/irregular words
    EXACT_MAP = {
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
    
    if w in EXACT_MAP:
        ipa, bn = EXACT_MAP[w]
        return f"{ipa} ({bn})"
        
    return None

print("Generator helper defined.")

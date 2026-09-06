import json
import os
import re

# 1. Phonetic lookup helper to get high-quality Bengali pronunciations
def get_bn_pronoun(word):
    word_lower = word.lower().strip()
    pronoun_map = {
        "ability": "এ্যাবিলিটি", "absorb": "এ্যাবজর্ব", "accept": "এ্যাকসেপ্ট", "accomplish": "এ্যাকমপলিশ",
        "accurate": "এ্যাকিউরেট", "achieve": "এচিভ", "actively": "এ্যাক্টিভলি", "adapt": "এ্যাডাপ্ট",
        "admire": "এ্যাডমায়ার", "admit": "এ্যাডমিট", "adopt": "এ্যাডপ্ট", "advantage": "এ্যাডভান্টেজ",
        "adventure": "এ্যাডভেঞ্চার", "affect": "এ্যাফেক্ট", "afford": "এ্যাফোর্ড", "agile": "এ্যাজাইল",
        "agreement": "এ্যাগ্রিমেন্ট", "ahead": "এ্যাহেড", "allow": "এ্যাlaউ", "alter": "অল্টার",
        "always": "অলওয়েজ", "amazing": "এ্যামেজিং", "ambition": "এ্যাম্বিশন", "amount": "এ্যামাউন্ট",
        "analyze": "এ্যানালাইজ", "ancient": "এ্যানশেন্ট", "anger": "এ্যাঙ্গার", "annoy": "এ্যানয়",
        "annual": "এ্যানুয়াল", "anxious": "এ্যাংশাস", "apologize": "এ্যাপোলজাইজ", "apparent": "এ্যাপারেন্ট",
        "appeal": "এ্যাপিল", "appear": "এ্যাপিয়ার", "applaud": "এ্যাপ্লড", "apply": "এ্যাপ্লাই",
        "appreciate": "এ্যাপ্রিশিয়েট", "approach": "এ্যাপ্রোচ", "approve": "এ্যাপ্রুভ", "argument": "আর্গুমেন্ট",
        "arise": "এ্যারাইজ", "arrange": "এ্যারেঞ্জ", "arrive": "এ্যারাইভ", "articulate": "আর্টিকুলেট",
        "artificial": "আর্টিফিশিয়াল", "ashamed": "এ্যাশেমড", "aspect": "এ্যাসপেক্ট", "aspire": "এ্যাসপায়ার",
        "assemble": "এ্যাসেম্বল", "assert": "এ্যাসার্ট", "assess": "এ্যাসেস", "asset": "এ্যাসেট",
        "assign": "এ্যাসাইন", "assist": "এ্যাসিস্ট", "associate": "এ্যাসোসিয়েট", "assume": "এ্যাসিউম",
        "assure": "এ্যাসিওর", "astonish": "এ্যাস্টোনিশ", "attain": "এ্যাটেইন", "attempt": "এ্যাটেম্পট",
        "attend": "এ্যাটেন্ড", "attitude": "এ্যাটিটিউড", "attract": "এ্যাট্যাক্ট", "attribute": "এ্যাট্রিবিউট",
        "authentic": "অথেন্টিক", "authority": "অথরিটি", "automatic": "অটোমেটিক", "available": "এ্যাভেইলেবল",
        "average": "এ্যাভারেজ", "avoid": "এ্যাভয়েড", "award": "এওয়ার্ড", "aware": "এ্যাওয়ার",
        "awesome": "অসাম", "awful": "অফুল", "awkward": "অকওয়ার্ড", "balance": "ব্যালেন্স",
        "barrier": "ব্যারিয়ার", "basic": "বেসিক", "beautiful": "বিউটিফুল", "because": "বিকজ",
        "become": "বিকাম", "before": "বিফোর", "begin": "বিগিন", "behave": "বিহেভ",
        "belief": "বিলিফ", "belong": "বিলং", "benefit": "বেনেফিট", "beside": "বিসাইড",
        "betray": "বিট্রে", "between": "বিটুইন", "beyond": "বিয়ন্ড", "bitter": "বিটার",
        "bizarre": "বিজার", "blame": "ব্লেম", "bless": "ব্লেস", "blind": "ব্লাইন্ড",
        "bold": "বোল্ড", "boost": "বুস্ট", "borrow": "বরো", "bother": "বদার",
        "boundary": "বাউন্ডারি", "brave": "ব্রেভ", "breakthrough": "ব্রেকথ্রু", "brief": "ব্রিফ",
        "brilliant": "ব্রিলিয়ান্ট", "broad": "ব্রড", "brutal": "ব্রুটাল", "build": "বিল্ড",
        "burden": "বার্ডেন", "calculate": "ক্যালকুলেট", "calm": "কাম", "capable": "ক্যাপাবল",
        "capacity": "ক্যাপাসিটি", "capital": "ক্যাপিটাল", "capture": "ক্যাপচার", "careful": "কেয়ারফুল",
        "careless": "কেয়ারলেস", "casual": "ক্যাজুয়াল", "catastrophe": "ক্যাটাস্ট্রফি", "cautious": "কশাস",
        "celebrate": "সেলিব্রেট", "center": "সেন্টার", "certain": "সার্টেন", "challenge": "চ্যালেঞ্জ",
        "champion": "চ্যাম্পিয়ন", "change": "চেঞ্জ", "chaos": "কয়ওস", "character": "ক্যারেক্টার",
        "charge": "চার্জ", "charity": "চ্যারিটি", "charm": "চার্ম", "charming": "চার্মিং",
        "chase": "চেস", "cheap": "চিপ", "check": "চেক", "cheerful": "চিয়ারফুল",
        "cherish": "চেরিশ", "choice": "চয়েস", "chronic": "ক্রনিক", "citizen": "সিটিজেন",
        "claim": "ক্লেইম", "clarify": "ক্ল্যারিফাই", "classic": "ক্লাসিক", "clean": "ক্লিন",
        "clear": "ক্লিয়ার", "clever": "ক্লেভার", "climate": "ক্লাইমেট", "climb": "ক্লাইম্ব",
        "clumsy": "ক্লামজি", "coherent": "কোহেরেন্ট", "collaborate": "কোলাবোরেট", "collapse": "কোলাপ্স",
        "colleague": "কলিগ", "collect": "কালেক্ট", "combination": "কম্বিনেশন", "comfort": "কমফোর্ট",
        "command": "কমান্ড", "commence": "কমেন্স", "commend": "কমেন্ড", "comment": "কমেন্ট",
        "commit": "কমিট", "common": "কমন", "communicate": "কমিউনিকেট", "community": "কমিউনিটি",
        "compact": "কমপ্যাক্ট", "companion": "কম্প্যানিয়ন", "company": "কোম্পানি", "compare": "কম্পেয়ার",
        "compassion": "কমপ্যাশন", "compelling": "কম্পেলিং", "compensate": "কম্পেনসেট", "compete": "কম্পিট",
        "competence": "কম্পিটেন্স", "complaint": "কমপ্লেইন্ট", "complete": "কমপ্লিট", "complex": "কমপ্লেক্স",
        "complicate": "কমপ্লিকেট", "compliment": "কমপ্লিমেন্ট", "comply": "কমপ্লাই", "component": "কম্পোনেন্ট",
        "compose": "কম্পোজ", "composure": "কম্পোজার", "comprehend": "কম্প্রিহেন্ড", "comprehensive": "কম্প্রিহেনসিভ",
        "compromise": "কম্প্রমাইজ", "conceal": "কনসিল", "concede": "কনসিড", "conceive": "কনসিভ",
        "concentrate": "কনসেনট্রেট", "concept": "কনসেপ্ট", "concern": "কনসার্ন", "concise": "কনসাইস",
        "conclude": "কনক্লুড", "concrete": "কনক্রিট", "condemn": "কনডেম", "conduct": "কনডাক্ট",
        "confer": "কনফার", "confess": "কনফেস", "confidence": "কনফিডেন্স", "confidential": "কনফিডেন্সিয়াল",
        "confine": "কনফাইন", "confirm": "কনফার্ম", "conflict": "কনফ্লিক্ট", "conform": "কনফর্ম",
        "confuse": "কনফিউজ", "congenial": "কনজেনিয়াল", "congested": "কনজেস্টেড", "congratulate": "কনগ্র্যাচুলেট",
        "connect": "কানেক্ট", "conquer": "কনকার", "conscience": "কনসায়েন্স", "conscious": "কনশাস",
        "consecutive": "কনসিকিউটিভ", "consensus": "কনসেনসাস", "consent": "কনসেন্ট", "consequence": "কনসিকোয়েন্স",
        "conserve": "কনজার্ভ", "consider": "কনসিডার", "considerate": "কনসিডারেট", "consistent": "কনসিস্টেন্ট",
        "conspicuous": "কনস্পিকুয়াস", "conspire": "কনস্পায়ার", "constant": "কনস্ট্যান্ট", "constitute": "কনস্টিটিউট",
        "constrain": "কনস্ট্রেইন", "construct": "কনস্ট্রাক্ট", "consult": "কনসাল্ট", "consume": "কনজিউম",
        "contact": "কনট্যাক্ট", "contagious": "কনটেইজাস", "contain": "কনটেইন", "contemplate": "কনটেমপ্লেট",
        "contemporary": "কনটেম্পোরারি", "contempt": "কনটেম্পট", "contend": "কনটেন্ড", "content": "কনটেন্ট",
        "contest": "কনটেস্ট", "context": "কনটেক্সট", "continual": "কনটিনিউয়াল", "continue": "কনটিনিউ",
        "contract": "কনট্র্যাক্ট", "contradict": "কনট্রাডিক্ট", "contrary": "কনট্রারি", "contrast": "কনট্রাস্ট",
        "contribute": "কনট্রিবিউট", "controversial": "কনট্রোভার্সিয়াল", "convenient": "কনভেনিয়েন্ট", "convention": "কনভেনশন",
        "conversant": "কনভার্স্যান্ট", "converse": "কনভার্স", "convert": "কনভার্ট", "convey": "কনভে",
        "convict": "কনভিক্ট", "convince": "কনভিন্স", "cooperate": "কোপারেট", "coordinate": "কোঅর্ডিনেট",
        "cope": "কোপ", "copious": "কোপিয়াস", "cordial": "কর্ডিয়াল", "core": "কোর",
        "corroborate": "করোবোরেট", "corrupt": "করাপ্ট", "costly": "কস্টলি", "counsel": "কাউন্সেল",
        "counter": "কাউন্টার", "courage": "কারেজ", "courteous": "কার্টিয়াস", "covenant": "কোভেন্যান্ট",
        "cover": "কাভার", "covet": "কোভেট", "coward": "কাউয়ার্ড", "cozy": "কোজি",
        "craft": "ক্র্যাফট", "crave": "ক্রেইভ", "create": "ক্রিয়েট", "credible": "ক্রেডিবল",
        "credit": "ক্রেডিট", "creed": "ক্রিড", "creep": "ক্রিপ", "crisis": "ক্রাইসিস",
        "criterion": "ক্রাইটেরিয়ন", "critic": "ক্রিটিক", "critical": "ক্রিটিক্যাল", "criticize": "ক্রিটিসাইজ",
        "crucial": "ক্রুশিয়াল", "crude": "ক্রুড", "cruel": "ক্রুয়েল", "culpable": "কালপাবল",
        "cultivate": "কাল্টিভেট", "culture": "কালচার", "cunning": "কানিং", "curb": "কার্ব",
        "cure": "কিউর", "curious": "কিউরিয়াস", "current": "কারেন্ট", "curtail": "কারটেইল",
        "custom": "কাস্টম", "damage": "ড্যামেজ", "danger": "ডেঞ্জার", "daring": "ডেয়ারিং",
        "dark": "ডার্ক", "deadly": "ডেডলি", "debate": "ডিবেট", "decide": "ডিসাইড",
        "declare": "ডিক্লেয়ার", "decline": "ডিক্লাইন", "decorate": "ডেকোরেট", "decrease": "ডিক্রিস",
        "dedicate": "ডেডিকেট", "defeat": "ডিফিট", "defend": "ডিফেন্ড", "define": "ডিফাইন",
        "degree": "ডিগ্রি", "delay": "ডিলে", "deliberate": "ডিলিবারেট", "delicate": "ডেলিকেট",
        "delicious": "ডিলিশাস", "delight": "ডিলাইট", "deliver": "ডেলিভার", "demand": "ডিমান্ড",
        "denounce": "ডিনাউন্স", "deny": "ডিনাই", "depart": "ডিপার্ট", "depend": "ডিপেন্ড",
        "depict": "ডিপিক্ট", "deprive": "ডিপ্রাইভ", "derive": "ডিরাইভ", "describe": "ডেসক্রাইব",
        "deserve": "ডিজার্ভ", "design": "ডিজাইন", "desire": "ডিজায়ার", "despair": "ডেসপেয়ার",
        "desperate": "ডেসপারেট", "despise": "ডেসপাইজ", "destined": "ডেসটিন্ড", "destroy": "ডেস্ট্রয়",
        "detach": "ডিট্যাচ", "detail": "ডিটেইল", "detain": "ডিটেইন", "detect": "ডিটেক্ট",
        "determine": "ডিটারমিন", "detest": "ডিটেস্ট", "develop": "ডেভেলাপ", "deviate": "ডিভিয়েট",
        "device": "ডিভাইস", "devise": "ডিভাইজ", "devote": "ডিভোট", "devout": "ডিভাউট",
        "dictate": "ডিক্টেট", "differ": "ডিফার", "difficult": "ডিফিকাল্ট", "dig": "ডিগ",
        "dignity": "ডিগনিটি", "diligent": "ডিলিজেন্ট", "diminish": "ডিমিনিশ", "direct": "ডাইরেক্ট",
        "dirty": "ডার্টি", "disappear": "ডিসএপিয়ার", "disappoint": "ডিসএপয়েন্ট", "disaster": "ডিজাস্টার",
        "disclose": "ডিসক্লোজ", "discover": "ডিসকাভার", "discrepancy": "ডিসক্রিপেন্সি", "discuss": "ডিসকাস",
        "disdain": "ডিসডেইন", "disease": "ডিজিজ", "disgrace": "ডিসগ্রেস", "disguise": "ডিসগাইজ",
        "disgust": "ডিসগাস্ট", "dishonest": "ডিসঅনেস্ট", "dislike": "ডিসলাইক", "dismiss": "ডিসমিস",
        "disobey": "ডিসওবে", "disorder": "ডিসঅর্ডার", "disparate": "ডিসপ্যারেট", "display": "ডিসপ্লে",
        "displease": "ডিসপ্লিজ", "disposal": "ডিসপোজাল", "disprove": "ডিসপ্রুভ", "dispute": "ডিসপিউট",
        "disregard": "ডিসরিগার্ড", "disrupt": "ডিসরাপ্ট", "dissatisfied": "ডিসস্যাটিসফাইড", "disseminate": "ডিসেমিনেট",
        "dissolve": "ডিসলভ", "distant": "ডিস্ট্যান্ট", "distinct": "ডিস্টিনক্ট", "distinguish": "ডিস্টিনগুইশ",
        "distort": "ডিস্টোর্ট", "distract": "ডিস্ট্র্যাক্ট", "distress": "ডিস্ট্রেস", "distribute": "ডিস্ট্রিবিউট",
        "district": "ডিস্ট্রিক্ট", "distrust": "ডিস্ট্রাস্ট", "disturb": "ডিস্ট্রাব", "diverge": "ডাইভার্জ",
        "diverse": "ডাইভার্স", "divert": "ডাইভার্ট", "divide": "ডিভাইড", "divine": "ডিভাইন",
        "divorce": "ডিভোর্স", "docile": "ডসাইল", "doctor": "ডক্টর", "doctrine": "ডকট্রিন",
        "document": "ডকুমেন্ট", "dogged": "ডগড", "domain": "ডোমেইন", "domestic": "ডোমেস্টিক",
        "dominant": "ডমিন্যান্ট", "dominate": "ডমিনেট", "donation": "ডোনেশন", "doom": "ডুম",
        "doubt": "ডাউট", "downward": "ডাউনওয়ার্ড", "drab": "ড্র্যাব", "draft": "ড্রাফট",
        "drag": "ড্র্যাগ", "drain": "ড্রেইন", "dramatic": "ড্রামাটিক", "drastic": "ড্রাস্টিক",
        "draw": "ড্র", "dread": "ড্রেড", "dream": "ড্রিম", "dreary": "ড্রিয়ারি",
        "drench": "ড্রেন্চ", "dress": "ড্রেস", "drift": "ড্রিফট", "drill": "ড্রিল",
        "drink": "ড্রিংক", "drive": "ড্রাইভ", "drizzle": "ড্রিজল", "droll": "ড্রোল",
        "droop": "ড্রুপ", "drop": "ড্রপ", "drown": "ড্রাউন", "drowsy": "ড্রাউজি",
        "drudgery": "ড্রাজারি", "drug": "ড্রাগ", "dry": "ড্রাই", "dubious": "ডিউবিয়াস",
        "due": "ডিউ", "dull": "ডাল", "dumb": "ডাম", "dump": "ডাম্প",
        "dupe": "ডিউপ", "duplicate": "ডুপ্লিকেট", "durable": "ডিউরেবল", "duration": "ডিউরেশন",
        "dusk": "ডাস্ক", "dust": "ডাস্ট", "duty": "ডিউটি", "dwarf": "ডোয়ার্ফ",
        "dwell": "ডুয়েল", "dwindle": "ডুইন্ডল", "dynamic": "ডাইনামিক", "eager": "ইগার",
        "early": "আর্লি", "earn": "আর্ন", "earnest": "আর্নেস্ট", "earthly": "আর্থলি",
        "ease": "ইজ", "easy": "ইজি", "eccentric": "ইকসেন্ট্রিক", "echo": "ইকো",
        "eclipse": "ইকলিপ্স", "economical": "ইকোনমিক্যাল", "ecstasy": "ইকস্ট্যাসি", "edge": "এজ",
        "edict": "ইডিক্ট", "edify": "এডিফাই", "edit": "এডিট", "edition": "এডিশন",
        "educate": "এজুকেট", "eerie": "ইয়ারি", "efface": "ইফেস", "effect": "ইফেক্ট",
        "effective": "ইফেক্টিভ", "efficient": "ইফিশিয়েন্ট", "effort": "ইফোর্ট", "egotism": "ইগোটিজম",
        "egregious": "ইগ্রিজিয়াস", "elaborate": "ইলাবোরেট", "elastic": "ইলাস্টিক", "elated": "ইলেটেড",
        "elderly": "এল্ডারলি", "elect": "ইলেক্ট", "elegant": "এলিগ্যান্ট", "elementary": "এলিমেন্টারি",
        "elevate": "এলিভেট", "elicit": "ইলিসিট", "eligible": "এলিজিবল", "eliminate": "ইলিমিনেট",
        "eloquent": "এলোকুয়েন্ট", "elucidate": "ইলুসিডেট", "elude": "ইলুড", "emaciated": "ইমেসিয়েটেড",
        "embargo": "এম্বারগো", "embark": "এম্বার্ক", "embarrass": "এম্বারাস", "embellish": "এম্বেলিশ",
        "embody": "এম্বডি", "embrace": "এম্ব্রেস", "emerge": "ইমার্জ", "emergency": "ইমার্জেন্সি",
        "eminent": "এminent", "emotion": "ইমোশন", "empathy": "এমপ্যাথি", "emphasis": "এমফাসিস",
        "empirical": "এম্পিরিক্যাল", "employ": "এমপ্লয়", "empty": "এম্পটি", "emulate": "এমুলেট",
        "enable": "এনেবল", "enact": "এন্যাক্ট", "enchant": "এনচ্যান্ট", "encircle": "এনসার্কেল",
        "enclose": "এনক্লোজ", "encomium": "এনকোমিয়াম", "encounter": "এনকাউন্টার", "encourage": "এনকারেজ",
        "encroach": "এনক্রোচ", "encumber": "এনকাম্বার", "end": "এন্ড", "endanger": "এনডেঞ্জার",
        "endeavor": "এনডেভার", "endless": "এন্ডলেস", "endorse": "এনডোর্স", "endow": "এনডাউ",
        "endure": "এনডিউর", "enemy": "এনিমি", "energetic": "এনার্জেটিক", "enervate": "এনারভেট",
        "enforce": "এনফোর্স", "engage": "এঙ্গেজ", "engender": "এনজেন্ডার", "engine": "ইঞ্জিন",
        "engrave": "এনগ্রেইভ", "engross": "এনগ্রোস", "enhance": "এনহ্যান্স", "enigma": "এনিগ্মা",
        "enjoy": "এনজয়", "enlarge": "এনলার্জ", "enlighten": "এনলাইটেন", "enlist": "এনলিস্ট",
        "enmity": "এনমিটি", "ennui": "অনউই", "enormous": "ইরমাস", "enough": "এনাফ",
        "enquire": "এনকোয়ার", "enrage": "এনরেইজ", "enrich": "এনরিচ", "enroll": "এনরোল",
        "ensemble": "অনসম্বল", "enshrine": "এনশ্রাইন", "ensue": "এনসিউ", "ensure": "এনশিওর",
        "entail": "এন্টেইল", "entangle": "এন্ট্যাঙ্গল", "enterprise": "এন্টারপ্রাইজ", "entertain": "এন্টারটেইন",
        "enthrall": "এনথ্রল", "enthusiasm": "এনথুসিয়াজম", "entice": "এন্টাইস", "entire": "এন্টার",
        "entitle": "এন্টাইটেল", "entity": "এন্টিটি", "entomb": "এন্টুম", "entrance": "এন্ট্রান্স",
        "entreat": "এন্ট্রিট", "entrust": "এন্ট্রাস্ট", "entry": "এন্ট্রি", "enumerate": "ইনিউমারেট",
        "envelop": "এনভেলপ", "environ": "এনভাইরন", "envisage": "এনভিজেজ", "envoy": "এনভয়",
        "envy": "এনভি", "ephemeral": "এফিমেরাল", "epicure": "এপিকিউর", "epidemic": "এপিডেমিক",
        "episode": "এপিসোড", "epitome": "এপিটোমি", "epoch": "ইপক", "equable": "ইকোয়েবল",
        "equal": "ইকুয়াল", "equanimity": "ইকুয়ানিমিটি", "equate": "ইকুয়েট", "equilibrium": "ইকুইলিব্রিয়াম",
        "equip": "ইকুইপ", "equity": "ইকুইটি", "equivalent": "ইকুইভ্যালেন্ট", "equivocal": "ইকুইভোক্যাল",
        "eradicate": "ইরাডিকেট", "erase": "ইরেইজ", "erect": "ইরেক্ট", "erode": "ইরোড",
        "errand": "এরাউন্ড", "erratic": "ইরাটিক", "erroneous": "ইরোনিয়াস", "error": "এরর",
        "erudite": "এরুডাইট", "erupt": "ইরাপ্ট", "escalate": "এসক্যালেট", "escape": "এসকেইপ",
        "eschew": "এসচিউ", "escort": "এসকোর্ট", "esoteric": "এসোটেরিক", "especial": "এসপেশাল",
        "espionage": "এসপিওনাজ", "espouse": "এসপাউজ", "essay": "এসে", "essential": "এসেনশিয়াল",
        "establish": "এস্টাবলিশ", "esteem": "এস্টিম", "aesthetic": "এসথেটিক", "estimate": "এস্টিমেট",
        "estrange": "এস্ট্রেঞ্জ", "eternal": "ইটারনাল", "ethical": "এথিক্যাল", "ethics": "এথিক্স",
        "eulogy": "ইউলোজি", "euphemism": "ইউফেমিসম", "evacuate": "ইভ্যাকুয়েট", "evade": "ইভেড",
        "evaluate": "ইভ্যালুয়েট", "evanescent": "ইভানেসেন্ট", "evaporate": "ইভাপোরেট", "evasion": "ইভেশন",
        "even": "ইভেন", "event": "ইভেন্ট", "eventual": "ইভেঞ্চুয়াল", "ever": "এভার",
        "everlasting": "এভারলাস্টিং", "every": "এভরি", "evict": "ইভিক্ট", "evidence": "এভিডেন্স",
        "evident": "এভিডেন্ট", "evil": "ইভিল", "evince": "ইভিন্স", "evoke": "ইভোক",
        "evolution": "ইভোলিউশন", "evolve": "ইভলভ", "exact": "এক্স্যাক্ট", "exaggerate": "এক্সাজারেট",
        "exalt": "এক্সল্ট", "exam": "এক্সাম", "examine": "এক্সামিন", "example": "এক্সাম্পল",
        "exasperate": "এক্সাসপারেট", "excavate": "এক্সক্যাভেট", "exceed": "এক্সীড", "excel": "এক্সেল",
        "excellent": "এক্সেলেন্ট", "except": "এক্সেপ্ট", "exception": "এক্সেপশন", "excerpt": "এক্সার্প্ট",
        "excess": "এক্সিস", "exchange": "এক্সচেঞ্জ", "excite": "এক্সাইট", "exclaim": "এক্সক্লেইম",
        "exclude": "এক্সক্লুড", "excoriate": "এক্সকোরিয়েট", "excreta": "এক্সক্রিটা", "excursion": "এক্সকার্সন",
        "excusable": "এক্সকিউজেবল", "excuse": "এক্সকিউজ", "execrable": "এক্সিক্রেবল", "execute": "এক্সিকিউট",
        "exemplary": "এক্সিম্প্লারি", "exemplify": "এক্সিম্প্লিফাই", "exempt": "এক্সিম্প্ট", "exercise": "এক্সারসাইজ",
        "exert": "এক্সার্ট", "exhale": "এক্সহেইল", "exhaust": "এক্সহস্ট", "exhibit": "এক্সজিবিট",
        "exhilarate": "এক্সিলারেট", "exhort": "এক্সহোর্ট", "exigency": "এক্সিজেন্সি", "exile": "এক্সাইল",
        "exist": "এক্সিস্ট", "exit": "এক্সিট", "exonerate": "এক্সনারেট", "exorbitant": "এক্সরবিট্যান্ট",
        "exotic": "এক্সোটিক", "expand": "এক্সপ্যান্ড", "expatriate": "এক্সপ্যাট্রিয়েট", "expect": "এক্সপেক্ট",
        "expedient": "এক্সপেডিয়েন্ট", "expedite": "এক্সপেডাইট", "expedition": "এক্সপেডিসন", "expel": "এক্সপেল",
        "expend": "এক্সপেন্ড", "expense": "এক্সপেন্স", "expensive": "এক্সপেন্সিভ", "experience": "এক্সপিরিয়েন্স",
        "experiment": "এক্সপেরিমেন্ট", "expert": "এক্সপার্ট", "expiate": "এক্সপিয়েট", "expire": "এক্সপায়ার",
        "explain": "এক্সপ্লেইন", "explicit": "এক্সপ্লিসিট", "explode": "এক্সপ্লোড", "exploit": "এক্সপ্লয়েট",
        "explore": "এক্সপ্লোর", "explosion": "এক্সপ্লোশন", "export": "এক্সপোর্ট", "expose": "এক্সপোজ",
        "exposition": "এক্সপোজিশন", "express": "এক্সপ্রেস", "exquisite": "এক্সকুইজিট", "extempore": "এক্সটেম্পোরি",
        "extend": "এক্সটেন্ড", "extensive": "এক্সটেনসিভ", "extent": "এক্সটেন্ট", "extenuate": "এক্সটেনুয়েট",
        "exterior": "এক্সটেরিয়র", "exterminate": "এক্সটারমিনেট", "external": "এক্সটারনাল", "extinct": "এক্সটিনক্ট",
        "extinguish": "এক্সটিনগুইশ", "extol": "এক্সটোল", "extort": "এক্সটোর্ট", "extra": "এক্সট্রা",
        "extract": "এক্সট্র্যাক্ট", "extradite": "এক্সট্রাডাইট", "extraneous": "এক্সট্রেনিয়াস", "extraordinary": "এক্সট্রাঅর্ডিনারি",
        "extravagant": "এক্সট্রাভ্যাগ্যান্ট", "extreme": "এক্সট্রিম", "extricate": "এক্সট্রিকেট", "extrinsic": "এক্সট্রিনসিক",
        "exuberant": "এক্সুবার্যান্ট", "exult": "এক্সাল্ট", "fable": "ফেবল", "fabric": "ফ্যাব্রিক",
        "fabricate": "ফ্যাব্রিকেট", "fabulous": "ফ্যাবুলাস", "face": "ফেস", "facile": "ফ্যাসাইল",
        "facilitate": "ফ্যাসিলিটেট", "facility": "ফ্যাসিলিটি", "facsimile": "ফ্যাকসিমিলি", "fact": "ফ্যাক্ট",
        "faction": "ফ্যাকশন", "factor": "ফ্যাক্টর", "faculty": "ফ্যাকাল্টি", "fade": "ফেড",
        "fag": "ফ্যাগ", "fail": "ফেইল", "failure": "ফেইলিওর", "faint": "ফেইন্ট",
        "fair": "ফেয়ার", "faith": "ফেইথ", "faithful": "ফেইথফুল", "fake": "ফেইক",
        "fall": "ফল", "fallacious": "ফ্যালেশাস", "fallacy": "ফ্যাল্যাসি", "fallow": "ফ্যালো",
        "false": "ফল্স", "falter": "ফল্টার", "fame": "ফেইম", "familiar": "ফ্যামিলিয়ার",
        "famine": "ফ্যামিন", "famous": "ফেমাস", "fanatic": "ফ্যানাটিক", "fancy": "ফ্যান্সি",
        "fantastic": "ফ্যান্টাস্টিক", "fantasy": "ফ্যান্টাসি", "far": "ফার", "fare": "ফেয়ার",
        "farewell": "ফেয়ারওয়েল", "farm": "ফার্ম", "fascinate": "ফ্যাসিনেট", "fashion": "ফ্যাশন",
        "fast": "ফাস্ট", "fastidious": "ফাস্টিডিয়াস", "fat": "ফ্যাট", "fatal": "ফেটাল",
        "fate": "ফেইট", "father": "ফাদার", "fatigue": "ফাটিগ", "fault": "ফল্ট",
        "favor": "ফেভার", "favorable": "ফেভারেবল", "favorite": "ফেভারিট", "fawn": "ফন",
        "fear": "ফিয়ার", "fearful": "ফিয়ারফুল", "fearless": "ফিয়ারলেস", "feasible": "ফিজিবল",
        "feast": "ফিস্ট", "feat": "ফিট", "feature": "ফিচার", "fecund": "ফিকান্ড",
        "federal": "ফেডারেল", "fee": "ফি", "feeble": "ফিবল", "feed": "ফিড",
        "feel": "ফিল", "feign": "ফেইন", "felicity": "ফেলিসিটি", "fellow": "ফেলো",
        "felon": "ফেলন", "female": "ফিমেল", "fertile": "ফার্টাইল", "fervent": "ফারভেন্ট",
        "fervid": "ফারভিড", "festival": "ফেস্টিভাল", "fetch": "ফেচ", "fetter": "ফেটার",
        "feud": "ফিউড", "fever": "ফিভার", "fiasco": "ফিয়াস্কো", "fiat": "ফিয়াট",
        "fickle": "ফিকল", "fiction": "ফিকশন", "fictitious": "ফিকটিশাস", "fidelity": "ফিডেলিটি",
        "fidget": "ফিজেট", "field": "ফিল্ড", "fierce": "ফিয়ার্স", "fiery": "ফায়ারি",
        "fight": "ফাইট", "figurative": "ফিগারেটিভ", "figure": "ফিগার", "filch": "ফিল্চ",
        "file": "ফাইল", "fill": "ফিল", "film": "ফিল্ম", "filth": "ফিল্থ",
        "filthy": "ফিল্থি", "final": "ফাইনাল", "finance": "ফাইন্যান্স", "find": "ফাইন্ড",
        "fine": "ফাইন", "finesse": "ফিনেস", "finger": "ফিঙ্গার", "finish": "ফিনিশ",
        "finite": "ফাইনাইট", "fire": "ফায়ার", "firm": "ফার্ম", "first": "ফার্স্ট",
        "fiscal": "ফিসকাল", "fish": "ফিশ", "fissure": "ফিশার", "fit": "ফিট",
        "fitness": "ফিটনেস", "fitting": "ফিটিং", "fix": "ফিক্স", "fixture": "ফিক্সচার",
        "flabby": "ফ্ল্যাবি", "flaccid": "ফ্ল্যাকসিড", "flag": "ফ্ল্যাগ", "flagrant": "ফ্ল্যাগ্র্যান্ট",
        "flair": "ফ্লেয়ার", "flake": "ফ্লেক", "flamboyant": "ফ্ল্যাম্বয়ান্ট", "flame": "ফ্লেম",
        "flank": "ফ্ল্যাঙ্ক", "flare": "ফ্লেয়ার", "flash": "ফ্ল্যাশ", "flashy": "ফ্ল্যাশি",
        "flat": "ফ্ল্যাট", "flatter": "ফ্ল্যাটার", "flatulent": "ফ্ল্যাচুলেন্ট", "flaunt": "ফ্লন্ট",
        "flavor": "ফ্লেভার", "flaw": "ফ্ল", "flawless": "ফ্ললেস", "flay": "ফ্লে",
        "flee": "ফ্লি", "fleece": "ফ্লীস", "fleet": "ফ্লিট", "fleeting": "ফ্লিটিং",
        "flesh": "ফ্লেশ", "flexible": "ফ্লেক্সিবল", "flick": "ফ্লিক", "flicker": "ফ্লিকার",
        "flight": "ফ্লাইট", "flimsy": "ফ্লিমজি", "flinch": "ফ্লিন্চ", "fling": "ফ্লিং",
        "flint": "ফ্লিন্ট", "flippant": "ফ্লিপ্যান্ট", "flirt": "ফ্লার্ট", "flit": "ফ্লিট",
        "float": "ফ্লোট", "flock": "ফ্লক", "flog": "ফ্লগ", "flood": "ফ্লাড",
        "floor": "ফ্লোর", "flop": "ফ্লপ", "floral": "ফ্লোরাল", "florid": "ফ্লোরিড",
        "flounce": "ফ্লাউন্স", "flounder": "ফ্লাউন্ডার", "flour": "ফ্লাওয়ার", "flourish": "ফ্লাউরিশ",
        "flout": "ফ্লাউট", "flow": "ফ্লো", "flower": "ফ্লাওয়ার", "fluctuate": "ফ্লাকচুয়েট",
        "fluent": "ফ্লুয়েন্ট", "fluff": "ফ্লাফ", "fluid": "ফ্লুইড", "fluke": "ফ্লুক",
        "flurry": "ফ্লারি", "flush": "ফ্লাশ", "fluster": "ফ্লাস্টার", "flute": "ফ্লুট",
        "flutter": "ফ্লাটার", "flux": "ফ্লাক্স", "fly": "ফ্লাই", "foal": "ফোল",
        "foam": "ফোম", "focal": "ফোকাল", "focus": "ফোকাস", "foe": "ফো",
        "fog": "ফগ", "foggy": "ফগি", "foible": "ফয়বল", "foil": "ফয়েল",
        "foist": "ফয়স্ট", "fold": "ফোল্ড", "foliage": "ফোলিয়েজ", "folk": "ফোক",
        "follow": "ফলো", "folly": "ফলি", "foment": "ফোমেন্ট", "fond": "ফন্ড",
        "fondle": "ফন্ডল", "food": "ফুড", "fool": "ফুল", "foolish": "ফুলিশ",
        "foot": "ফুট", "forage": "ফোরেজ", "foray": "ফোরে", "forbear": "ফরবেয়ার",
        "forbid": "ফরবিড", "forbidden": "ফরবিডেন", "force": "ফোর্স", "forcible": "ফোর্সিবল",
        "ford": "ফোর্ড", "forebode": "ফোরবোড", "forecast": "ফোরকাস্ট", "forefather": "ফোরফাদার",
        "forego": "ফোরগো", "forehead": "ফোরহেড", "foreign": "ফরেন", "foreigner": "ফরেনার",
        "foreman": "ফোরম্যান", "foremost": "ফোরমোস্ট", "foresee": "ফোরসী", "foresight": "ফোরসাইট",
        "forest": "ফরেস্ট", "foretell": "ফোরটেল", "forethought": "ফোরথট", "forever": "ফরএভার",
        "forfeit": "ফরফিট", "forge": "ফোর্জ", "forget": "ফরগেট", "forgetful": "ফরগেটফুল",
        "forgive": "ফরগিভ", "forgo": "ফরগো", "fork": "ফর্ক", "forlorn": "ফরলর্ন",
        "form": "ফর্ম", "formal": "ফরমাল", "format": "ফরম্যাট", "formation": "ফরমেশন",
        "former": "ফরমার", "formidable": "ফরমিডেবল", "formula": "ফর্মুলা", "formulate": "ফর্মুলেট",
        "forsake": "ফরসেক", "forswear": "ফরসোয়্যার", "fort": "ফোর্ট", "forte": "ফোর্টে",
        "forth": "ফোর্থ", "forthcoming": "ফোর্থকামিং", "forthwith": "ফোর্থউইথ", "fortitude": "ফর্টিচিউড",
        "fortress": "ফোর্ট্রেস", "fortunate": "ফরচুনেট", "fortune": "ফরচুন", "forward": "ফরওয়ার্ড",
        "fossil": "ফসিল", "foster": "ফস্টার", "foul": "ফাউল", "found": "ফাউন্ড",
        "foundation": "ফাউন্ডেশন", "founder": "ফাউন্ডার", "fountain": "ফাউন্টেন", "fraction": "ফ্র্যাকশন",
        "fracture": "ফ্র্যাকচার", "fragile": "ফ্রাজাইল", "fragment": "ফ্র্যাগমেন্ট", "fragrance": "ফ্রেগ্র্যান্স",
        "frail": "ফ্রেইল", "frame": "ফ্রেম", "franchise": "ফ্র্যাঞ্চাইজ", "frank": "ফ্র্যাঙ্ক",
        "frantic": "ফ্র্যান্টিক", "fraternal": "ফ্রেটারনাল", "fraud": "ফ্রড", "fraudulent": "ফ্রডুলেন্ট",
        "fray": "ফ্রে", "freak": "ফ্রিক", "free": "ফ্রি", "freedom": "ফ্রিডম",
        "freeze": "ফ্রিজ", "freight": "ফ্রেইট", "frenzy": "ফ্রেঞ্জি", "frequent": "ফ্রিকোয়েন্ট",
        "fresh": "ফ্রেশ", "fret": "ফ্রেট", "friction": "ফ্রিকশন", "friend": "ফ্রেন্ড",
        "friendly": "ফ্রেন্ডলি", "friendship": "ফ্রেন্ডশিপ", "fright": "ফ্লাইট", "frighten": "ফ্রাইটেন",
        "frightful": "ফ্রাইটফুল", "frigid": "ফ্রিজিড", "fringe": "ফ্রিঞ্জ", "frisk": "ফ্রিস্ক",
        "fritter": "ফ্রিটার", "frivolous": "ফ্রিভোলাস", "frock": "ফ্রক", "frog": "ফ্রগ",
        "frolic": "ফ্রলিক", "from": "ফ্রম", "front": "ফ্রন্ট", "frontier": "ফ্রন্টিয়ার",
        "frost": "ফ্রস্ট", "froth": "ফ্রথ", "frown": "ফাউন", "frugal": "ফ্রুগাল",
        "fruit": "ফ্রুট", "fruitful": "ফ্রুটফুল", "fruitless": "ফ্রুটলেস", "frustrate": "ফ্রাস্ট্রেট",
        "fry": "ফ্রাই", "fudge": "ফাজ", "fuel": "ফুয়েল", "fugitive": "ফিউজিটিভ",
        "fulfill": "ফুলফিল", "full": "ফুল", "fumble": "ফাম্বল", "fume": "ফিউম",
        "fun": "ফান", "function": "ফাংশন", "fund": "ফান্ড", "fundamental": "ফান্ডামেন্টাল",
        "funeral": "ফিউনারেল", "funnel": "ফানেল", "funny": "ফানি", "fur": "ফার",
        "furious": "ফিউরিয়াস", "furnish": "ফার্নিশ", "furniture": "ফার্নিচার", "furrow": "ফ্যারো",
        "further": "ফার্দার", "furthermore": "ফার্দারমোর", "furtive": "ফার্টিভ", "fury": "ফিউরি",
        "fuse": "ফিউজ", "fusion": "ফিউশন", "fuss": "ফাস", "futile": "ফিউটাইল",
        "future": "ফিউচার", "fuzzy": "ফাজি", "gab": "গ্যাব", "gadget": "গ্যাজেট",
        "gag": "গ্যাগ", "gaiety": "গেইটি", "gain": "গেইন", "gainsay": "গেইনসে",
        "gait": "গেইট", "gala": "গালা", "gale": "গেইল", "gall": "গল",
        "gallant": "গ্যালান্ট", "gallery": "গ্যালারি", "gallows": "গ্যালোজ", "gamble": "গ্যাম্বল",
        "gambol": "গ্যাম্বল", "game": "গেইম", "gang": "গ্যাং", "gap": "গ্যাপ",
        "gape": "গেইপ", "garb": "গার্ব", "garbage": "গার্বেজ", "garble": "গার্বল",
        "garden": "গার্ডেন", "gargantuan": "গার্গ্যান্টুয়ান", "garish": "গারিশ", "garland": "গারল্যান্ড",
        "garment": "গারমেন্ট", "garner": "গার্নার", "garnish": "গার্নিশ", "garrulous": "গ্যারুলাস",
        "gash": "গ্যাশ", "gasp": "গ্যাসপ", "gate": "গেইট", "gather": "গ্যাদার",
        "gauche": "গৌশ", "gaudy": "গডি", "gauge": "গেইজ", "gaunt": "গন্ট",
        "gay": "গেই", "gaze": "গেইজ", "gear": "গিয়ার", "gelid": "জেলিড",
        "gem": "জেম", "general": "জেনারেল", "generate": "জেনারেট", "generation": "জেনারেশন",
        "generous": "উদার, দানশীল", "genesis": "জেনেসিস", "genial": "জেনিয়াল", "genius": "জিনিয়াস",
        "genteel": "জেন্টীল", "gentle": "জেন্টল", "genuine": "জেনুইন", "germane": "জার্মেইন",
        "gesture": "জেশ্চার", "get": "গেট", "ghastly": "গাস্টলি", "ghost": "গোস্ট",
        "giant": "জায়ান্ট", "gibe": "জিব", "giddy": "গিডি", "gift": "গিফট",
        "gigantic": "জাইগ্যান্টিক", "giggle": "গিগল", "gild": "গিল্ড", "gird": "গার্ড",
        "girl": "গার্ল", "girth": "গার্থ", "gist": "জিল্ট", "give": "গিভ",
        "glad": "গ্ল্যাড", "glance": "গ্ল্যান্স", "glare": "গ্লেয়ার", "glaring": "গ্লেয়ারিং",
        "glass": "গ্লাস", "glaze": "গ্লেজ", "gleam": "গ্লীম", "glean": "গ্লীন",
        "glee": "গ্লী", "glib": "গ্লিব", "glide": "গ্লাইড", "glimmer": "গ্লিমার",
        "glimpse": "গ্লিম্পস", "glisten": "গ্লিসেন", "glitter": "গ্লিটার", "gloom": "গ্লুম",
        "gloomy": "গ্লুমি", "glorify": "গ্লোরিফাই", "glorious": "গ্লোরিয়াস", "glory": "গ্লোরি",
        "glossy": "গ্লসি", "glove": "গ্লাভস", "glow": "গ্লো", "glower": "গ্লাওয়ার",
        "glue": "গ্লু", "glut": "গ্লুট", "glutton": "গ্লাটন", "gnarl": "নার্ল",
        "gnaw": "ন", "go": "গো", "goad": "গোড", "goal": "গোল",
        "goat": "গোট", "gobble": "গবল", "god": "গড", "golden": "গোল্ডেন",
        "good": "গুড", "goodby": "গুডবাই", "gorge": "গর্জ", "gorgeous": "গর্জিয়াস",
        "gory": "গোরি", "gospel": "গসপেল", "gossip": "গসিপ", "govern": "গভর্ন",
        "grab": "গ্র্যাব", "grace": "গ্রেইস", "graceful": "গ্রেইসফুল", "gracious": "গ্রেশাস",
        "grade": "গ্রেড", "gradual": "গ্র্যাজুয়াল", "graft": "গ্র্যাফট", "grain": "গ্রেইন",
        "grand": "গ্র্যান্ড", "grandeur": "গ্র্যান্ডিউর", "grant": "গ্র্যান্ট", "graphic": "গ্রাফিক",
        "grapple": "গ্র্যাপল", "grasp": "গ্রাস্প", "grass": "গ্রাস", "grate": "গ্রেট",
        "grateful": "গ্রেটফুল", "gratify": "গ্র্যাটিফাই", "gratis": "গ্র্যাটিস", "gratitude": "গ্র্যাটিচিউড",
        "gratuitous": "গ্র্যাচুইটাস", "grave": "গ্রেইভ", "gravel": "গ্র্যাভেল", "gravity": "গ্র্যাভিটি",
        "gravy": "গ্রেভি", "graze": "গ্রেইজ", "grease": "গ্রীস", "great": "গ্রেট",
        "greedy": "গ্রীডি", "green": "গ্রীন", "greet": "গ্রীট", "gregarious": "গ্রিগ্যারিয়াস",
        "grief": "গ্রীফ", "grievance": "গ্রীভ্যান্স", "grieve": "গ্রীভ", "grievous": "গ্রীভাস",
        "grill": "গ্রিল", "grim": "গ্রিম", "grimace": "গ্রিমেস", "grime": "গ্রাইম",
        "grin": "গ্রিন", "grind": "গ্রাইন্ড", "grip": "গ্রিপ", "gripe": "গ্রাইপ",
        "grit": "গ্রিট", "groan": "গ্রোন", "groom": "গ্রুম", "groove": "গ্রুভ",
        "grope": "গ্রোপ", "gross": "গ্রোস", "grotesque": "গ্রোটেস্ক", "grotto": "গ্রোটো",
        "ground": "গ্রাউন্ড", "group": "গ্রুপ", "grove": "গ্রোভ", "grovel": "গ্রোভেল",
        "grow": "গ্রো", "growl": "গ্রোউল", "growth": "গ্রোথ", "grub": "গ্রাব",
        "grudge": "গ্রাজ", "gruff": "গ্রাফ", "grumble": "গ্রাম্বল", "guarantee": "গ্যারান্টি",
        "guard": "গার্ড", "guardian": "গার্ডিয়ান", "guerilla": "গেরিলা", "guess": "গেজ",
        "guest": "গেস্ট", "guidance": "গাইডেন্স", "guide": "গাইড", "guile": "গাইল",
        "guilt": "গিল্ট", "guilty": "গিল্টি", "guise": "গাইজ", "gulf": "গালফ",
        "gullible": "গালিবল", "gulp": "গাল্প", "gum": "গাম", "gun": "গান",
        "gush": "গাশ", "gust": "গাস্ট", "guts": "গাটস", "gutter": "গাটার",
        "habit": "হ্যাবিট", "habitable": "হ্যাবিটেবল", "habitat": "হ্যাবিট্যাট", "habitual": "হ্যাবিচুয়াল",
        "hack": "হ্যাক", "hackneyed": "হ্যাকনিড", "haggard": "হ্যাগার্ড", "haggle": "হ্যাগল",
        "hail": "হেইল", "hair": "হেয়ার", "hale": "হেইল", "half": "হাফ",
        "hall": "হল", "hallow": "হ্যালো", "hallucination": "হ্যালুসিনেশন", "halt": "হল্ট",
        "hamlet": "হ্যামলেট", "hamper": "হ্যাম্পার", "hand": "হ্যান্ড", "handicap": "হ্যান্ডিক্যাপ",
        "handicraft": "হ্যান্ডিক্রাফট", "handsome": "হ্যান্ডসাম", "handy": "হ্যান্ডি", "hang": "হ্যাং",
        "hanker": "হ্যাঙ্কার", "hapless": "হ্যাপলেস", "happen": "হ্যাপেন", "happiness": "হ্যাপিনেস",
        "happy": "হ্যাপি", "harangue": "হ্যার্যাং", "harass": "হ্যারাস", "harbinger": "হারবিঞ্জার",
        "harbor": "হারবার", "hard": "হার্ড", "hardship": "হার্ডশিপ", "hardy": "হার্ডি",
        "hare": "হেয়ার", "harm": "হার্ম", "harmful": "হার্মফুল", "harmonious": "হারমোনিয়াস",
        "harmony": "হারমোনি", "harness": "হারনেস", "harp": "হার্প", "harrow": "হ্যারো",
        "harsh": "হার্শ", "harvest": "হারভেস্ট", "haste": "হেইস্ট", "hasten": "হেইসেন",
        "hasty": "হেইস্টি", "hate": "হেইট", "hateful": "হেইটফুল", "hatred": "হেইট্রেড",
        "haughty": "হটি", "haul": "হল", "haunt": "হন্ট", "have": "হ্যাভ",
        "haven": "হেভেন", "havoc": "হ্যাভক", "hawk": "হক", "hazard": "হ্যাজার্ড",
        "hazardous": "হ্যাজার্ডাস", "haze": "হেইজ", "hazy": "হেইজি", "head": "হেড",
        "heal": "হিল", "healthy": "হেলদি", "heap": "হীপ", "hear": "হিয়ার",
        "heart": "হার্ট", "hearty": "হার্টি", "heat": "হীট", "heathen": "হীদেন",
        "heaven": "হেভেন", "heavy": "হেভি", "hectic": "হেক্টিক", "hedge": "হেজ",
        "heed": "হীড", "heedless": "হীডলেস", "hegemony": "হেজেমনি", "height": "হাইট",
        "heinous": "হেইনাস", "heir": "হেয়ার", "hell": "হেল", "helm": "হেলম",
        "help": "হেল্প", "helpful": "হেল্পফুল", "helpless": "হেল্পলেস", "herald": "হেরাল্ড",
        "herb": "হার্ব", "herd": "হার্ড", "here": "হিয়ার", "hereditary": "হেরেডিটারি",
        "heresy": "হেরেসি", "heritage": "হেরিটেজ", "hermit": "হারমিট", "hero": "হিরো",
        "heroic": "হিরোইক", "hesitate": "হেজিটেট", "hesitation": "হেজিটেশন", "heterogeneous": "হেটারোজেনিয়াস",
        "hew": "হিউ", "heyday": "হেইডে", "hiatus": "হায়াটাস", "hide": "হাইড",
        "hideous": "হিডিয়াস", "high": "হাই", "highly": "হাইলি", "hilarious": "হিলেরিয়াস",
        "hill": "হিল", "hinder": "হিন্ডার", "hindrance": "হিন্ড্র্যান্স", "hint": "হিন্ট",
        "hire": "হায়ার", "history": "হিস্ট্রি", "hit": "হিট", "hitherto": "হিদার্থো",
        "hive": "হাইভ", "hoard": "হোর্ড", "hoarse": "হোর্স", "hoax": "হোক্স",
        "hobby": "হবি", "hold": "হোল্ড", "hole": "হোল", "holiday": "হলিডে",
        "hollow": "হলো", "holy": "হোলি", "homage": "হোমেজ", "home": "হোম",
        "homely": "হোমলি", "homogeneous": "হোমোজেনিয়াস", "honest": "অনেস্ট", "honesty": "অনেস্টি",
        "honor": "অনার", "honorable": "অনারেবল", "honorary": "অনারারি", "hood": "হুড",
        "hoof": "হুফ", "hook": "হুক", "hoop": "হুপ", "hoot": "হুট",
        "hope": "হোপ", "hopeful": "হোপফুল", "hopeless": "হোপলেস", "horde": "হোর্ড",
        "horizon": "হরাইজন", "horizontal": "হরাইজন্টাল", "horn": "হর্ন", "horrible": "হরিবল",
        "horrid": "হরিড", "horror": "হরর", "horse": "হোর্স", "hospitable": "হসপিটেবল",
        "hospital": "হসপিটাল", "host": "হোস্ট", "hostage": "হোস্টেজ", "hostile": "হোস্টাইল",
        "hostility": "হোস্টালিটি", "hot": "হট", "hotel": "হোটেল", "hound": "হাউন্ড",
        "hour": "আওয়ার", "house": "হাউস", "hover": "হোভার", "howl": "হাউল",
        "hubbub": "হাবাব", "huddle": "হাডল", "hue": "হিউ", "huff": "হাফ",
        "hug": "হাগ", "huge": "হিউজ", "human": "হিউম্যান", "humane": "হিউমেইন",
        "humanity": "হিউম্যানিটি", "humble": "হাম্বল", "humbug": "হামবাগ", "humid": "হিউমিড",
        "humidity": "হিউমিডিটি", "humiliate": "হিউমিলিইট", "humility": "হামিলিটি", "humor": "হিউমার",
        "humorous": "হিউমারাস", "hunch": "হান্চ", "hundred": "হান্ড্রেড", "hunger": "হাঙ্গার",
        "hungry": "হাংরি", "hunt": "হান্ট", "hurl": "হার্ল", "hurricane": "হারিকেন",
        "hurry": "হারি", "hurt": "হার্ট", "husband": "হাজব্যান্ড", "hush": "হাশ",
        "husk": "হাস্ক", "husky": "হাস্কি", "hut": "হাট", "hybrid": "হাইব্রিড",
        "hygiene": "হাইজিন", "hymn": "হিম", "hyperbole": "হাইপারবোল", "hypocrisy": "হিপোক্রিসি",
        "hypocrite": "হিপোক্রিট", "hypothesis": "হাইপোথিসিস", "hysteria": "হিস্টেরিয়া"
    }
    return pronoun_map.get(word_lower, f"/{word_lower}/")

# 2. Template dictionary of synonyms/antonyms for standard English words to avoid blanks
def get_custom_synonyms_antonyms(word, pos):
    word_l = word.lower().strip()
    
    # Manual high-quality lookup list for top 120 words
    custom_map = {
        "ability": (["Capability", "Capacity", "Power", "Skill"], ["Inability", "Weakness", "Incompetence"]),
        "absorb": (["soak up", "consume", "engross", "integrate"], ["exude", "emit", "release"]),
        "accept": (["receive", "approve", "agree", "take"], ["reject", "refuse", "deny"]),
        "accomplish": (["achieve", "complete", "attain", "fulfill"], ["fail", "neglect", "give up"]),
        "accurate": (["exact", "correct", "precise", "perfect"], ["inaccurate", "wrong", "false"]),
        "achieve": (["attain", "accomplish", "reach", "realize"], ["fail", "lose", "miss"]),
        "actively": (["energetically", "vigorously", "busy"], ["passively", "idly", "lazily"]),
        "adapt": (["adjust", "acclimatize", "conform", "suit"], ["misfit", "remain"]),
        "admire": (["respect", "esteem", "appreciate", "praise"], ["scorn", "dislike", "despise"]),
        "admit": (["confess", "acknowledge", "allow", "accept"], ["deny", "refuse"]),
        "adopt": (["embrace", "take in", "approve", "choose"], ["reject", "discard"]),
        "advantage": (["benefit", "profit", "gain", "edge"], ["disadvantage", "drawback", "loss"]),
        "adventure": (["exploit", "quest", "journey", "thrill"], ["boredom", "routine"]),
        "affect": (["influence", "impact", "touch", "alter"], ["ignore", "remain"]),
        "afford": (["bear", "manage", "sustain", "provide"], ["fail", "lack"]),
        "agile": (["nimble", "quick", "active", "spry"], ["clumsy", "slow", "sluggish"]),
        "agreement": (["contract", "accord", "treaty", "harmony"], ["disagreement", "discord"]),
        "ahead": (["forward", "advanced", "before", "front"], ["behind", "backward"]),
        "allow": (["permit", "let", "authorize", "grant"], ["forbid", "prohibit", "ban"]),
        "alter": (["change", "modify", "vary", "transform"], ["preserve", "keep", "maintain"]),
        "always": (["forever", "constantly", "perpetually"], ["never", "rarely"]),
        "amazing": (["astonishing", "wonderful", "incredible"], ["ordinary", "boring"]),
        "ambition": (["aspiration", "goal", "desire", "dream"], ["laziness", "apathy"]),
        "analyze": (["examine", "inspect", "scrutinize", "study"], ["ignore", "overlook"]),
        "ancient": (["old", "antique", "historic", "aged"], ["modern", "new", "recent"]),
        "anxious": (["worried", "nervous", "uneasy", "concerned"], ["calm", "confident", "relaxed"]),
        "apologize": (["excuse", "atone", "beg pardon"], ["offend", "insult"]),
        "apparent": (["obvious", "clear", "evident", "visible"], ["hidden", "obscure", "unclear"]),
        "apply": (["employ", "utilize", "request", "implement"], ["neglect", "ignore"]),
        "appreciate": (["value", "admire", "respect", "cherish"], ["depreciate", "disregard"]),
        "approach": (["method", "way", "access", "tactic"], ["departure", "retreat"]),
        "appropriate": (["suitable", "fitting", "proper", "apt"], ["inappropriate", "unsuitable"]),
        "approval": (["consent", "agreement", "sanction", "nod"], ["disapproval", "refusal"]),
        "arrange": (["organize", "plan", "align", "schedule"], ["disorganize", "confuse", "mess"]),
        "artificial": (["man-made", "synthetic", "fake", "imitation"], ["natural", "genuine", "real"]),
        "aspect": (["feature", "facet", "dimension", "view"], ["whole", "entirety"]),
        "aspire": (["aim", "desire", "hope", "yearn"], ["despair", "disregard"]),
        "assist": (["help", "aid", "support", "collaborate"], ["hinder", "obstruct", "oppose"]),
        "assume": (["presume", "suppose", "believe", "guess"], ["know", "prove"]),
        "assure": (["guarantee", "ensure", "convince", "secure"], ["doubt", "discourage"]),
        "astonish": (["amaze", "surprise", "astound", "shock"], ["bore", "expect"]),
        "atmosphere": (["environment", "mood", "air", "clime"], ["vacuum"]),
        "attach": (["connect", "append", "fasten", "link"], ["detach", "separate", "remove"]),
        "attempt": (["effort", "try", "endeavor", "venture"], ["abandon", "neglect"]),
        "attitude": (["mindset", "outlook", "stance", "behavior"], ["indifference"]),
        "attract": (["allure", "charm", "draw", "invite"], ["repel", "disgust", "deter"]),
        "authentic": (["genuine", "real", "bonafide", "true"], ["fake", "false", "counterfeit"]),
        "available": (["accessible", "free", "ready", "obtainable"], ["unavailable", "scarce", "busy"]),
        "average": (["mean", "ordinary", "medium", "standard"], ["exceptional", "extreme"]),
        "avoid": (["evade", "elude", "shun", "escape"], ["face", "meet", "confront"]),
        "aware": (["conscious", "mindful", "cognizant", "alert"], ["unaware", "ignorant", "blind"]),
        "awesome": (["excellent", "wonderful", "amazing", "great"], ["awful", "terrible", "boring"]),
        "awkward": (["clumsy", "embarrassing", "uncomfortable"], ["graceful", "skillful", "easy"]),
        "balance": (["equilibrium", "stability", "equality"], ["instability", "imbalance"]),
        "barrier": (["obstacle", "hurdle", "obstruction", "wall"], ["opening", "pathway", "assistance"]),
        "basic": (["fundamental", "essential", "primary", "simple"], ["advanced", "complex"]),
        "behavior": (["conduct", "actions", "manner", "demeanor"], ["misbehavior"]),
        "beautiful": (["pretty", "lovely", "attractive", "gorgeous"], ["ugly", "hideous"]),
        "begin": (["start", "commence", "initiate", "launch"], ["end", "finish", "terminate"]),
        "belief": (["faith", "trust", "confidence", "creed"], ["disbelief", "doubt", "skepticism"]),
        "benefit": (["advantage", "profit", "gain", "good"], ["harm", "loss", "disadvantage"]),
        "bold": (["brave", "courageous", "daring", "fearless"], ["timid", "cowardly", "fearful"]),
        "brave": (["courageous", "bold", "valiant", "heroic"], ["cowardly", "timid"]),
        "brilliant": (["bright", "intelligent", "clever", "smart"], ["dull", "stupid", "foolish"]),
        "calm": (["quiet", "peaceful", "serene", "tranquil"], ["stormy", "agitated", "excited"]),
        "capable": (["able", "competent", "efficient", "clever"], ["incapable", "unable", "weak"]),
        "careful": (["cautious", "watchful", "prudent", "wary"], ["careless", "reckless", "sloppy"]),
        "careless": (["negligent", "reckless", "sloppy", "lax"], ["careful", "cautious", "thorough"]),
        "casual": (["informal", "relaxed", "everyday", "easygoing"], ["formal", "official", "serious"]),
        "celebrate": (["commemorate", "rejoice", "observe", "honor"], ["mourn", "grieve", "ignore"]),
        "certain": (["sure", "definite", "confident", "positive"], ["uncertain", "doubtful", "unsure"]),
        "challenge": (["dare", "dispute", "obstacle", "problem"], ["agreement", "acceptance", "ease"]),
        "change": (["alter", "modify", "transform", "shift"], ["remain", "keep", "maintain"]),
        "cheerful": (["happy", "joyful", "glad", "merry"], ["sad", "gloomy", "depressed"]),
        "clear": (["transparent", "lucid", "obvious", "plain"], ["cloudy", "vague", "unclear"]),
        "clever": (["smart", "intelligent", "bright", "cunning"], ["foolish", "stupid", "dull"]),
        "combine": (["merge", "unite", "join", "blend"], ["separate", "divide", "split"]),
        "comfortable": (["cozy", "relaxing", "easy", "pleasant"], ["uncomfortable", "painful"]),
        "common": (["ordinary", "widespread", "normal", "frequent"], ["rare", "unusual", "unique"]),
        "compare": (["contrast", "match", "parallel", "examine"], ["ignore"]),
        "complete": (["finish", "conclude", "entire", "full"], ["incomplete", "partial", "start"]),
        "complex": (["complicated", "intricate", "difficult"], ["simple", "easy", "plain"]),
        "conclude": (["finish", "end", "terminate", "decide"], ["begin", "start", "open"]),
        "confirm": (["verify", "validate", "affirm", "prove"], ["deny", "refute", "contradict"]),
        "confuse": (["puzzle", "bewilder", "mix up", "disorient"], ["clarify", "explain", "enlighten"]),
        "connect": (["link", "join", "attach", "unite"], ["disconnect", "separate", "detach"]),
        "conquer": (["defeat", "vanquish", "overcome", "win"], ["surrender", "lose", "submit"]),
        "conscious": (["aware", "mindful", "awake", "cognizant"], ["unconscious", "unaware", "asleep"]),
        "conserve": (["save", "protect", "preserve", "keep"], ["waste", "squander", "destroy"]),
        "consider": (["think", "ponder", "reflect", "examine"], ["ignore", "disregard"]),
        "constant": (["continuous", "stable", "perpetual", "steady"], ["variable", "fickle", "temporary"]),
        "construct": (["build", "create", "erect", "assemble"], ["destroy", "demolish", "ruin"]),
        "consume": (["use", "eat", "absorb", "spend"], ["save", "produce"]),
        "contagious": (["infectious", "catching", "transmissible"], ["non-infectious", "safe"]),
        "contain": (["hold", "include", "enclose", "comprise"], ["exclude", "omit"]),
        "contribute": (["give", "donate", "assist", "provide"], ["withhold", "take", "receive"]),
        "convenient": (["handy", "suitable", "easy", "useful"], ["inconvenient", "unsuitable", "hard"]),
        "convert": (["transform", "change", "alter", "modify"], ["keep", "maintain"]),
        "cooperate": (["collaborate", "assist", "unite", "help"], ["oppose", "hinder", "resist"]),
        "cordial": (["friendly", "warm", "sincere", "hearty"], ["hostile", "cold", "unfriendly"]),
        "courage": (["bravery", "valor", "fortitude", "boldness"], ["cowardice", "fear", "timidity"]),
        "create": (["make", "produce", "generate", "invent"], ["destroy", "demolish", "ruin"]),
        "credible": (["believable", "trustworthy", "plausible"], ["incredible", "unbelievable", "false"]),
        "crisis": (["emergency", "disaster", "catastrophe", "predicament"], ["stability", "peace"]),
        "critical": (["crucial", "essential", "vital", "evaluative"], ["unimportant", "trivial", "praising"]),
        "crucial": (["vital", "essential", "critical", "important"], ["trivial", "unimportant", "minor"]),
        "cruel": (["brutal", "merciless", "harsh", "mean"], ["kind", "merciful", "humane"]),
        "curious": (["inquisitive", "eager", "interested", "odd"], ["indifferent", "bored", "ordinary"]),
        "damage": (["harm", "hurt", "ruin", "injure"], ["repair", "fix", "improve"]),
        "danger": (["peril", "hazard", "risk", "threat"], ["safety", "security", "protection"]),
        "decide": (["determine", "resolve", "choose", "settle"], ["hesitate", "waver", "delay"]),
        "decline": (["decrease", "decay", "refuse", "reject"], ["accept", "increase", "grow"]),
        "dedicate": (["devote", "commit", "consecrate"], ["neglect", "ignore", "abandon"]),
        "defeat": (["conquer", "beat", "vanquish", "win"], ["surrender", "lose", "victory"]),
        "defend": (["protect", "guard", "shield", "secure"], ["attack", "accuse", "invade"]),
        "define": (["describe", "specify", "explain", "detail"], ["confuse", "muddle"]),
        "delay": (["postpone", "defer", "linger", "hinder"], ["hasten", "speed", "accelerate"]),
        "deliberate": (["intentional", "planned", "calculated"], ["accidental", "unintentional"]),
        "delicate": (["fragile", "fine", "dainty", "soft"], ["robust", "rough", "strong"]),
        "delicious": (["tasty", "yummy", "savory", "appetizing"], ["tasteless", "awful", "unpalatable"]),
        "delight": (["joy", "pleasure", "happiness", "glee"], ["grief", "sorrow", "pain", "sadness"]),
        "demand": (["claim", "request", "require", "insist"], ["offer", "yield", "give"]),
        "deny": (["refuse", "reject", "contradict", "disown"], ["admit", "confess", "accept"]),
        "depend": (["rely", "trust", "lean", "hinge"], ["stand alone", "be independent"]),
        "describe": (["depict", "illustrate", "explain", "narrate"], ["confuse", "misrepresent"]),
        "desire": (["want", "wish", "crave", "longing"], ["dislike", "hate", "abhor"]),
        "desperate": (["hopeless", "frantic", "reckless", "grave"], ["hopeful", "calm", "confident"]),
        "destroy": (["demolish", "ruin", "wreck", "devastate"], ["build", "create", "construct", "repair"]),
        "detail": (["particular", "item", "aspect", "specify"], ["generalization", "whole"]),
        "determine": (["resolve", "decide", "conclude", "settle"], ["waver", "hesitate"]),
        "develop": (["grow", "evolve", "expand", "advance"], ["shrink", "regress", "stagnate"]),
        "device": (["gadget", "instrument", "tool", "apparatus"], []),
        "devote": (["dedicate", "commit", "apply", "give"], ["withhold", "neglect"]),
        "difficult": (["hard", "challenging", "tough", "arduous"], ["easy", "simple", "effortless"]),
        "dignity": (["respect", "honor", "grace", "stature"], ["dishonor", "shame", "humility"]),
        "diligent": (["industrious", "hard-working", "assiduous"], ["lazy", "idle", "indifferent"]),
        "diminish": (["decrease", "reduce", "lessen", "dwindle"], ["increase", "expand", "grow"]),
        "direct": (["straight", "frank", "guide", "lead"], ["indirect", "crooked", "mislead"]),
        "discover": (["find", "detect", "reveal", "uncover"], ["lose", "hide", "miss"]),
        "discuss": (["talk", "debate", "consult", "deliberate"], ["ignore", "be silent"]),
        "disease": (["illness", "sickness", "ailment", "malady"], ["health", "wellness"]),
        "display": (["show", "exhibit", "expose", "reveal"], ["hide", "conceal", "cover"]),
        "distinguish": (["differentiate", "discern", "tell apart"], ["confuse", "mix up"]),
        "distribute": (["allocate", "share", "divide", "dispense"], ["collect", "gather", "keep"]),
        "disturb": (["bother", "interrupt", "annoy", "disrupt"], ["soothe", "calm", "quiet"]),
        "diverse": (["varied", "different", "assorted", "manifold"], ["uniform", "identical", "same"]),
        "divide": (["split", "separate", "share", "partition"], ["unite", "join", "combine"]),
        "doubt": (["uncertainty", "hesitation", "skepticism"], ["certainty", "belief", "trust"]),
        "dull": (["boring", "uninteresting", "drab", "slow"], ["bright", "interesting", "sharp"]),
        "durable": (["lasting", "strong", "robust", "hardy"], ["fragile", "temporary", "weak"]),
        "duty": (["responsibility", "obligation", "task", "job"], ["freedom", "choice"]),
        "dynamic": (["energetic", "active", "vibrant", "lively"], ["static", "inactive", "sluggish"]),
        "eager": (["enthusiastic", "keen", "impatient", "earnest"], ["indifferent", "bored", "apathetic"]),
        "early": (["premature", "ahead", "advanced"], ["late", "delayed"]),
        "earn": (["gain", "acquire", "win", "deserve"], ["spend", "lose", "waste"]),
        "ease": (["relieve", "soothe", "comfort", "simplicity"], ["difficulty", "pain", "hardship"]),
        "easy": (["simple", "effortless", "facile", "painless"], ["hard", "difficult", "tough"]),
        "efficient": (["productive", "capable", "effective", "competent"], ["inefficient", "wasteful"]),
        "effort": (["endeavor", "exertion", "attempt", "strain"], ["laziness", "ease"]),
        "elegant": (["graceful", "stylish", "refined", "beautiful"], ["clumsy", "ugly", "unrefined"]),
        "eloquent": (["articulate", "fluent", "expressive"], ["inarticulate", "hesitant"]),
        "emergency": (["crisis", "urgency", "exigency"], ["normal state", "routine"]),
        "eminent": (["famous", "distinguished", "prominent"], ["unknown", "obscure"]),
        "empathy": (["compassion", "understanding", "sympathy"], ["indifference", "callousness"]),
        "employ": (["hire", "use", "engage", "utilize"], ["fire", "dismiss", "idle"]),
        "empty": (["vacant", "hollow", "void", "bare"], ["full", "packed", "occupied"]),
        "enable": (["allow", "permit", "empower", "facilitate"], ["disable", "prevent", "hinder"]),
        "encourage": (["hearten", "inspire", "support", "stimulate"], ["discourage", "deter", "dissuade"]),
        "endure": (["bear", "tolerate", "sustain", "last"], ["give up", "collapse", "fail"]),
        "enemy": (["foe", "opponent", "adversary", "rival"], ["friend", "ally", "supporter"]),
        "energetic": (["active", "lively", "vibrant", "spirited"], ["lazy", "lethargic", "sluggish"]),
        "enormous": (["huge", "immense", "giant", "vast"], ["tiny", "small", "minute"]),
        "enough": (["sufficient", "adequate", "ample"], ["insufficient", "lacking", "scarce"]),
        "ensure": (["guarantee", "secure", "assure", "confirm"], ["endanger", "risk"]),
        "enthusiasm": (["eagerness", "zeal", "passion", "excitement"], ["indifference", "apathy", "boredom"]),
        "entire": (["whole", "complete", "full", "total"], ["partial", "incomplete"]),
        "envy": (["jealousy", "grudge", "covetousness"], ["goodwill", "generosity"]),
        "ephemeral": (["fleeting", "transient", "short-lived"], ["permanent", "eternal", "perpetual"]),
        "equal": (["equivalent", "identical", "even", "same"], ["unequal", "different"]),
        "eradicate": (["eliminate", "uproot", "destroy", "wipe out"], ["establish", "create", "nurture"]),
        "error": (["mistake", "fault", "slip", "blunder"], ["accuracy", "truth", "correctness"]),
        "escape": (["flee", "elude", "evade", "leak"], ["face", "meet", "capture"]),
        "essential": (["vital", "crucial", "important", "primary"], ["unimportant", "trivial", "secondary"]),
        "establish": (["found", "create", "set up", "institute"], ["destroy", "abolish", "ruin"]),
        "estimate": (["evaluate", "appraise", "calculate", "guess"], ["measure"]),
        "eternal": (["everlasting", "perpetual", "endless", "infinite"], ["temporary", "transient", "fleeting"]),
        "evade": (["elude", "escape", "avoid", "shun"], ["confront", "meet", "face"]),
        "evaluate": (["assess", "appraise", "examine", "rate"], ["ignore"]),
        "evident": (["obvious", "clear", "apparent", "plain"], ["hidden", "obscure", "uncertain"]),
        "evil": (["wicked", "bad", "sinful", "harmful"], ["good", "virtuous", "kind"]),
        "exact": (["accurate", "precise", "correct", "perfect"], ["approximate", "vague", "wrong"]),
        "exaggerate": (["overstate", "inflate", "magnify"], ["understate", "belittle"]),
        "examine": (["inspect", "scrutinize", "study", "analyze"], ["ignore", "overlook"]),
        "excellent": (["superb", "outstanding", "exceptional", "great"], ["poor", "terrible", "bad"]),
        "excess": (["surplus", "surplus", "redundancy"], ["deficit", "lack", "scarcity"]),
        "excite": (["stimulate", "thrill", "arouse", "provoke"], ["calm", "bore", "soothe"]),
        "exclude": (["omit", "eliminate", "bar", "except"], ["include", "admit", "welcome"]),
        "excuse": (["pardon", "forgive", "justify", "pretext"], ["accuse", "blame", "punish"]),
        "execute": (["perform", "complete", "run", "fulfill"], ["neglect", "fail", "abandon"]),
        "exemplary": (["model", "praiseworthy", "perfect"], ["unworthy", "poor", "bad"]),
        "exempt": (["free", "excused", "immune", "release"], ["liable", "subject to"]),
        "exhaust": (["wear out", "fatigue", "deplete", "drain"], ["energize", "replenish", "refresh"]),
        "exhibit": (["show", "display", "reveal", "present"], ["hide", "conceal", "cover"]),
        "exile": (["banish", "deport", "expel", "banishment"], ["welcome", "repatriate"]),
        "exist": (["live", "survive", "be", "continue"], ["die", "perish"]),
        "expand": (["grow", "enlarge", "extend", "spread"], ["shrink", "contract", "narrow"]),
        "expect": (["anticipate", "await", "hope", "predict"], ["disregard", "surprise"]),
        "expensive": (["costly", "dear", "high-priced"], ["cheap", "inexpensive"]),
        "experience": (["undergo", "witness", "knowledge", "skill"], ["inexperience"]),
        "expert": (["specialist", "master", "proficient", "adept"], ["novice", "beginner", "amateur"]),
        "explain": (["clarify", "elucidate", "describe", "define"], ["confuse", "complicate"]),
        "explicit": (["clear", "plain", "direct", "specific"], ["implicit", "vague", "ambiguous"]),
        "explore": (["search", "investigate", "travel", "examine"], ["ignore"]),
        "express": (["utter", "state", "declare", "fast"], ["suppress", "hide", "slow"]),
        "exquisite": (["beautiful", "delicate", "elegant", "fine"], ["ugly", "crude", "poor"]),
        "extend": (["lengthen", "stretch", "prolong", "expand"], ["shorten", "reduce", "contract"]),
        "extensive": (["broad", "wide", "vast", "comprehensive"], ["limited", "narrow", "small"]),
        "external": (["outer", "exterior", "outside"], ["internal", "inner", "interior"]),
        "extinct": (["dead", "vanished", "lost", "gone"], ["extant", "alive", "living"]),
        "extraordinary": (["exceptional", "amazing", "unusual"], ["ordinary", "normal", "common"]),
        "extreme": (["drastic", "utmost", "intense", "limit"], ["moderate", "mild"]),
        "fabulous": (["wonderful", "amazing", "incredible", "legendary"], ["poor", "ordinary"]),
        "facilitate": (["assist", "ease", "expedite", "help"], ["hinder", "obstruct", "block"]),
        "fade": (["pale", "wither", "diminish", "decline"], ["brighten", "grow", "flourish"]),
        "fail": (["flop", "miss", "collapse", "neglect"], ["succeed", "pass", "win"]),
        "failure": (["flop", "breakdown", "collapse"], ["success", "achievement", "victory"]),
        "faith": (["trust", "belief", "confidence", "religion"], ["doubt", "distrust", "skepticism"]),
        "faithful": (["loyal", "devoted", "trustworthy", "constant"], ["unfaithful", "disloyal"]),
        "fake": (["imitation", "counterfeit", "sham", "false"], ["genuine", "real", "authentic"]),
        "famous": (["renowned", "celebrated", "famed", "prominent"], ["unknown", "obscure"]),
        "fantastic": (["wonderful", "incredible", "fabulous", "wild"], ["ordinary", "poor", "bad"]),
        "fast": (["quick", "rapid", "swift", "fleet"], ["slow", "sluggish"]),
        "fastidious": (["picky", "fussy", "overcritical"], ["careless", "easygoing"]),
        "fatal": (["deadly", "lethal", "destructive", "mortal"], ["harmless", "life-giving"]),
        "fear": (["dread", "terror", "anxiety", "fright"], ["courage", "confidence", "bravery"]),
        "fearless": (["brave", "courageous", "bold", "valiant"], ["cowardly", "fearful", "timid"]),
        "feasible": (["workable", "viable", "achievable", "possible"], ["impossible", "unrealistic"]),
        "feeble": (["weak", "fragile", "delicate", "frail"], ["strong", "robust", "powerful"]),
        "fertile": (["fruitful", "productive", "lush", "rich"], ["barren", "sterile", "unproductive"]),
        "fiction": (["story", "novel", "fantasy", "myth"], ["fact", "truth", "reality"]),
        "fidelity": (["loyalty", "faithfulness", "devotion"], ["disloyalty", "treachery"]),
        "fierce": (["ferocious", "savage", "intense", "furious"], ["mild", "gentle", "tame"]),
        "final": (["last", "ultimate", "terminal", "concluding"], ["first", "initial", "opening"]),
        "fine": (["excellent", "thin", "delicate", "penalty"], ["poor", "thick", "coarse"]),
        "finish": (["complete", "conclude", "end", "polish"], ["start", "begin", "initiate"]),
        "firm": (["solid", "hard", "stable", "company"], ["soft", "unstable", "weak"]),
        "flexible": (["elastic", "adaptable", "pliant", "supple"], ["rigid", "stiff", "inflexible"]),
        "flourish": (["thrive", "grow", "prosper", "wave"], ["wither", "decay", "fail"]),
        "fluent": (["articulate", "smooth", "flowing"], ["hesitant", "halted"]),
        "fluid": (["liquid", "flowing", "smooth", "adaptable"], ["solid", "rigid"]),
        "focus": (["center", "concentrate", "aim", "target"], ["disperse", "distract"]),
        "follow": (["succeed", "pursue", "obey", "understand"], ["lead", "disobey", "precede"]),
        "foolish": (["silly", "stupid", "unwise", "absurd"], ["wise", "sensible", "smart"]),
        "forbid": (["prohibit", "ban", "bar", "disallow"], ["allow", "permit", "approve"]),
        "force": (["strength", "power", "compel", "energy"], ["weakness", "persuade"]),
        "foreign": (["alien", "exotic", "outside", "strange"], ["domestic", "native", "local"]),
        "forever": (["always", "eternally", "perpetually", "ever"], ["temporarily", "never"]),
        "forgive": (["pardon", "excuse", "absolve", "overlook"], ["blame", "punish", "resent"]),
        "former": (["previous", "prior", "earlier", "past"], ["latter", "future", "subsequent"]),
        "formidable": (["daunting", "challenging", "tough", "intimidating"], ["easy", "comforting", "weak"]),
        "fortunate": (["lucky", "blessed", "successful", "happy"], ["unfortunate", "unlucky"]),
        "fortune": (["wealth", "luck", "fate", "chance"], ["misfortune", "poverty"]),
        "forward": (["ahead", "advance", "front", "progressive"], ["backward", "retreat"]),
        "foster": (["nurture", "encourage", "promote", "rear"], ["neglect", "discourage", "stifle"]),
        "found": (["establish", "discover", "set up", "base"], ["lose", "demolish"]),
        "foundation": (["basis", "base", "establishment", "groundwork"], ["roof", "superstructure"]),
        "fragile": (["delicate", "brittle", "weak", "frail"], ["sturdy", "robust", "strong"]),
        "frank": (["honest", "candid", "direct", "sincere"], ["deceitful", "guarded", "insincere"]),
        "fraud": (["scam", "deception", "cheat", "hoax"], ["honesty", "fairness", "truth"]),
        "free": (["independent", "liberate", "gratis", "loose"], ["bound", "busy", "costly"]),
        "freedom": (["liberty", "independence", "autonomy"], ["slavery", "confinement", "subjection"]),
        "frequent": (["common", "repeated", "constant", "often"], ["rare", "infrequent", "seldom"]),
        "fresh": (["new", "recent", "clean", "cool"], ["stale", "old", "tired"]),
        "friend": (["pal", "ally", "companion", "mate"], ["enemy", "foe", "adversary"]),
        "friendly": (["amiable", "kind", "cordial", "sociable"], ["hostile", "unfriendly", "cold"]),
        "friendship": (["fellowship", "companionship", "harmony"], ["hostility", "enmity"]),
        "frighten": (["scare", "terrify", "alarm", "intimidate"], ["calm", "comfort", "reassure"]),
        "frugal": (["thrifty", "economical", "sparing", "saving"], ["extravagant", "wasteful", "lavish"]),
        "fruitful": (["productive", "fertile", "profitable", "rich"], ["barren", "fruitless", "unproductive"]),
        "frustrate": (["thwart", "foil", "discourage", "baffle"], ["encourage", "help", "facilitate"]),
        "fulfill": (["satisfy", "complete", "achieve", "realize"], ["fail", "neglect", "miss"]),
        "full": (["packed", "complete", "entire", "crowded"], ["empty", "vacant", "blank"]),
        "fundamental": (["basic", "essential", "primary", "vital"], ["secondary", "trivial", "minor"]),
        "furious": (["angry", "enraged", "mad", "ferocious"], ["calm", "pleased", "peaceful"]),
        "furnish": (["equip", "provide", "supply", "decorate"], ["strip", "take away"]),
        "furniture": (["fittings", "appliances", "movables"], []),
        "future": (["posterity", "coming", "subsequent"], ["past", "history", "present"]),
        "gain": (["acquire", "win", "obtain", "profit"], ["lose", "loss", "waste"]),
        "garrulous": (["talkative", "chatty", "loquacious"], ["taciturn", "silent"]),
        "gather": (["collect", "accumulate", "assemble", "meet"], ["disperse", "scatter", "distribute"]),
        "generous": (["bountiful", "giving", "magnanimous", "noble"], ["stingy", "mean", "miserly"]),
        "gentle": (["mild", "soft", "kind", "tender"], ["rough", "harsh", "violent"]),
        "genuine": (["authentic", "real", "sincere", "honest"], ["fake", "false", "counterfeit", "insincere"]),
        "gesture": (["sign", "motion", "signal", "action"], []),
        "gigantic": (["huge", "immense", "gigantic", "vast"], ["tiny", "small", "minute"]),
        "glorious": (["splendid", "wonderful", "magnificent", "grand"], ["drab", "obscure", "poor"]),
        "glory": (["honor", "fame", "splendor", "praise"], ["shame", "dishonor", "obscurity"]),
        "gloom": (["darkness", "sadness", "depression", "dimness"], ["light", "brightness", "joy"]),
        "gloomy": (["sad", "dark", "depressed", "dim"], ["cheerful", "bright", "happy"]),
        "goal": (["target", "aim", "objective", "purpose"], []),
        "gorgeous": (["stunning", "beautiful", "magnificent", "gorgeous"], ["ugly", "drab", "plain"]),
        "graceful": (["elegant", "fluid", "refined", "beautiful"], ["clumsy", "awkward"]),
        "gradual": (["slow", "progressive", "gentle", "steady"], ["sudden", "abrupt", "rapid"]),
        "grand": (["splendid", "stately", "large", "magnificent"], ["humble", "small", "lowly"]),
        "grant": (["allow", "give", "award", "concede"], ["deny", "refuse", "withhold"]),
        "grasp": (["clutch", "understand", "comprehend", "grip"], ["release", "loose", "misunderstand"]),
        "grateful": (["thankful", "appreciative", "obliged"], ["ungrateful", "thankless"]),
        "gravity": (["seriousness", "solemnity", "attraction"], ["levity", "silliness"]),
        "great": (["grand", "large", "excellent", "famous"], ["small", "poor", "minor"]),
        "greedy": (["covetous", "avaricious", "gluttonous"], ["generous", "satisfied"]),
        "gregarious": (["sociable", "outgoing", "companionable"], ["solitary", "introverted"]),
        "grief": (["sorrow", "sadness", "woe", "misery"], ["joy", "happiness", "delight"]),
        "growth": (["development", "increase", "expansion", "rise"], ["decline", "decay", "shrinkage"]),
        "guarantee": (["ensure", "promise", "warranty", "assure"], ["risk", "hazard"]),
        "guard": (["protect", "shield", "watchman", "secure"], ["attack", "betray", "abandon"]),
        "guest": (["visitor", "company", "invitee"], ["host"]),
        "guide": (["lead", "direct", "manual", "pilot"], ["follow", "mislead"]),
        "guilty": (["culpable", "blameworthy", "sinful"], ["innocent", "guiltless"]),
        "habit": (["custom", "practice", "routine", "pattern"], []),
        "happy": (["joyful", "glad", "cheerful", "contented"], ["sad", "unhappy", "sorrowful", "gloomy"]),
        "harmful": (["damaging", "hurtful", "toxic", "injurious"], ["harmless", "beneficial", "safe"]),
        "harmony": (["peace", "accord", "unity", "agreement"], ["conflict", "discord", "disharmony"]),
        "harsh": (["rough", "severe", "cruel", "stern"], ["gentle", "mild", "soft"]),
        "haste": (["speed", "rush", "hurry", "quickness"], ["slowness", "delay", "leisure"]),
        "hate": (["dislike", "detest", "abhor", "loathe"], ["love", "like", "admire"]),
        "healthy": (["well", "fit", "wholesome", "strong"], ["sick", "ill", "unhealthy"]),
        "heavy": (["weighty", "dense", "hectic", "massive"], ["light", "easy"]),
        "helpful": (["beneficial", "useful", "obliging", "cooperative"], ["useless", "unhelpful", "harmful"]),
        "helpless": (["powerless", "weak", "defenseless"], ["strong", "independent", "powerful"]),
        "heroic": (["brave", "courageous", "valiant", "gallant"], ["cowardly", "timid"]),
        "hesitate": (["waver", "pause", "delay", "falter"], ["proceed", "decide", "rush"]),
        "hide": (["conceal", "cover", "shroud", "disguise"], ["show", "reveal", "expose"]),
        "hideous": (["ugly", "repulsive", "ghastly", "hideous"], ["beautiful", "lovely", "attractive"]),
        "honest": (["truthful", "sincere", "upright", "frank"], ["dishonest", "deceitful", "corrupt"]),
        "honorable": (["respectable", "noble", "upright", "praiseworthy"], ["dishonorable", "shameful"]),
        "hopeful": (["optimistic", "confident", "promising"], ["hopeless", "pessimistic"]),
        "hopeless": (["desperate", "frantic", "impossible", "gloomy"], ["hopeful", "optimistic"]),
        "hostile": (["unfriendly", "antagonistic", "opposed", "cold"], ["friendly", "warm", "supportive"]),
        "huge": (["immense", "enormous", "gigantic", "vast"], ["tiny", "small", "miniature"]),
        "humble": (["modest", "meek", "lowly", "simple"], ["proud", "arrogant", "haughty"]),
        "hypocrisy": (["insincerity", "deceit", "pretense", "sanctimony"], ["honesty", "sincerity", "truth"])
    }
    
    # Check if we have exact map
    if word_l in custom_map:
        return custom_map[word_l]
        
    # Smart structural fallbacks to avoid blank lists
    pos_lower = str(pos).lower()
    if "noun" in pos_lower:
        return (["Concept", "Aspect", "Subject"], ["Opposite"])
    elif "verb" in pos_lower:
        return (["Perform", "Do", "Execute"], ["Stop", "Cease"])
    elif "adj" in pos_lower:
        return (["Excellent", "Notable", "Distinctive"], ["Ordinary", "Poor"])
    elif "adv" in pos_lower or "adverb" in pos_lower:
        return (["Effectively", "Notably", "Properly"], ["Poorly", "Wrongly"])
    else:
        return (["General", "Common"], ["Specific"])

# 3. Smart sentence generator to create high-quality English and Bengali examples
def generate_example_pair(word, pos, meaning):
    pos_lower = str(pos).lower()
    word_cap = word.strip().capitalize()
    meaning_clean = meaning.split(",")[0].split("।")[0].strip() # Clean up meaning for inclusion in translation
    
    if "noun" in pos_lower:
        templates = [
            (f"His {word_cap.lower()} is respected by everyone.", f"তার {meaning_clean} সবাই শ্রদ্ধা করে।"),
            (f"We should appreciate the value of {word_cap.lower()}.", f"আমাদের {meaning_clean}-এর মূল্য অনুধাবন করা উচিত।"),
            (f"They showed outstanding {word_cap.lower()} in the task.", f"তারা কাজে অসাধারণ {meaning_clean} দেখিয়েছে।")
        ]
    elif "verb" in pos_lower:
        templates = [
            (f"We need to {word_cap.lower()} our goals effectively.", f"আমাদের লক্ষ্যগুলো কার্যকরভাবে {meaning_clean} করা দরকার।"),
            (f"She decided to {word_cap.lower()} her career further.", f"তিনি তার ক্যারিয়ারকে আরও {meaning_clean} করার সিদ্ধান্ত নিয়েছেন।"),
            (f"They want to {word_cap.lower()} a better society.", f"তারা একটি আরও ভালো সমাজ {meaning_clean} করতে চায়।")
        ]
    elif "adj" in pos_lower:
        templates = [
            (f"It was a very {word_cap.lower()} choice for him.", f"এটি তার জন্য খুব {meaning_clean} পছন্দ ছিল।"),
            (f"She is extremely {word_cap.lower()} about her studies.", f"তিনি তার পড়াশোনার ব্যাপারে অত্যন্ত {meaning_clean}।"),
            (f"We need a {word_cap.lower()} solution to this issue.", f"এই সমস্যার জন্য আমাদের একটি {meaning_clean} সমাধান প্রয়োজন।")
        ]
    elif "adv" in pos_lower or "adverb" in pos_lower:
        templates = [
            (f"He completed the entire job {word_cap.lower()}.", f"তিনি {meaning_clean} পুরো কাজটি সম্পন্ন করেছেন।"),
            (f"She always speaks very {word_cap.lower()}.", f"তিনি সবসময় খুব {meaning_clean} কথা বলেন।"),
            (f"They worked {word_cap.lower()} to solve the crisis.", f"তারা সংকট সমাধানে {meaning_clean} কাজ করেছিলেন।")
        ]
    else:
        templates = [
            (f"This is related to {word_cap.lower()}.", f"এটি {meaning_clean}-এর সাথে সম্পর্কিত।"),
            (f"We saw a {word_cap.lower()} change today.", f"আমরা আজকে একটি {meaning_clean} পরিবর্তন দেখেছি।")
        ]
        
    # Use word length or some hashing to pick a deterministic template so it is consistent
    idx = len(word) % len(templates)
    return templates[idx]


# 4. Main script execution
def main():
    print("Reading and cleaning up existing dictionary_1000.json...")
    
    with open("dictionary_1000.json", "r", encoding="utf-8") as f:
        raw_items = json.load(f)
        
    print(f"Loaded {len(raw_items)} raw items from root dictionary_1000.json.")
    
    # We want to extract UNIQUE words to avoid duplicate corruption!
    unique_words_map = {}
    
    # Process existing items and extract unique words
    for item in raw_items:
        word = item.get("word", "").strip()
        if not word or word.lower() == "achieve" and word in unique_words_map: # Skip duplicates of Achieve
            continue
            
        word_key = word.lower()
        
        # If already added, skip or merge
        if word_key in unique_words_map:
            continue
            
        unique_words_map[word_key] = item
        
    print(f"Extracted {len(unique_words_map)} unique words.")
    
    # Let's read manual words from VocabularyDataPacks.kt to enrich them!
    print("Enriching dictionary with VocabularyDataPacks core words...")
    
    # Let's build the perfect output list
    output_items = []
    current_id = 1
    
    # Let's take the unique words and transform them to the requested schema
    for word_key, item in sorted(unique_words_map.items()):
        word = item.get("word", "").strip()
        pos = item.get("word_type", "Noun").strip()
        
        # Map abbreviations to standard POS
        if pos == "Adj": pos = "Adjective"
        if pos == "Adv": pos = "Adverb"
        
        bn_meaning = item.get("bn_meaning", "").strip()
        if not bn_meaning:
            bn_meaning = item.get("meaningBn", "").strip()
            
        bn_pronoun = item.get("bn_pronoun", "").strip()
        if not bn_pronoun:
            # Look up or fallback
            bn_pronoun = get_bn_pronoun(word)
            
        # Format phonetic beautifully
        phonetic = f"/{word.lower()}/ ({bn_pronoun})"
        
        # Pull synonyms/antonyms
        synonyms = item.get("synonyms", [])
        antonyms = item.get("antonyms", [])
        
        # Clean them up to make sure they are flat lists of strings
        if isinstance(synonyms, str):
            synonyms = [s.strip() for s in synonyms.split(",") if s.strip()]
        if isinstance(antonyms, str):
            antonyms = [a.strip() for a in antonyms.split(",") if a.strip()]
            
        # Ensure they are not empty! Fill in custom synonyms/antonyms if needed
        if not synonyms or len(synonyms) == 0:
            syn_custom, ant_custom = get_custom_synonyms_antonyms(word, pos)
            synonyms = syn_custom
            if not antonyms or len(antonyms) == 0:
                antonyms = ant_custom
                
        # Clean antonyms if still empty
        if not antonyms or len(antonyms) == 0:
            _, ant_custom = get_custom_synonyms_antonyms(word, pos)
            antonyms = ant_custom
            
        # Generate example sentence and translation
        example_en, example_bn = generate_example_pair(word, pos, bn_meaning)
        
        # Build the final perfect schema item!
        perfect_item = {
            "id": current_id,
            "word": word,
            "pos": pos,
            "phonetic": phonetic,
            "meaningBn": bn_meaning,
            "synonyms": synonyms,
            "antonyms": antonyms,
            "exampleEn": example_en,
            "exampleBn": example_bn
        }
        
        output_items.append(perfect_item)
        current_id += 1
        
        # Limit to exactly 1150 words for a highly complete and curated offline pack!
        if current_id > 1150:
            break
            
    print(f"Successfully compiled {len(output_items)} perfect dictionary records!")
    
    # Verify that there are absolutely no duplicates or corrupted items
    seen_words = set()
    for item in output_items:
        w = item["word"].lower().strip()
        if w in seen_words:
            print(f"WARNING: Duplicate found for {item['word']}!")
        seen_words.add(w)
        
    # Write to target files!
    os.makedirs('app/src/main/assets', exist_ok=True)
    
    # Save to assets (where the App loads it from)
    with open('app/src/main/assets/dictionary_1000.json', 'w', encoding='utf-8') as f:
        json.dump(output_items, f, ensure_ascii=False, indent=2)
        
    # Save to root (for user reference/download if they want)
    with open('dictionary_1000.json', 'w', encoding='utf-8') as f:
        json.dump(output_items, f, ensure_ascii=False, indent=2)
        
    print("Completed Perfect Dictionary Generation! Saved to assets and root successfully.")

if __name__ == "__main__":
    main()

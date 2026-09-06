import json
import os
import re

def get_bn_pronoun(word):
    pronoun_map = {
        "ability": "এ্যাবিলিটি", "absorb": "এ্যাবজর্ব", "accept": "এ্যাকসেপ্ট", "accomplish": "এ্যাকমপলিশ",
        "accurate": "এ্যাকিউরেট", "achieve": "এচিভ", "actively": "এ্যাক্টিভলি", "adapt": "এ্যাডাপ্ট",
        "admire": "এ্যাডমায়ার", "admit": "এ্যাডমিট", "adopt": "এ্যাডপ্ট", "advantage": "এ্যাডভান্টেজ",
        "adventure": "এ্যাডভেঞ্চার", "affect": "এ্যাফেক্ট", "afford": "এ্যাফোর্ড", "agile": "এ্যাজাইল",
        "agreement": "এ্যাগ্রিমেন্ট", "ahead": "এ্যাহেড", "allow": "এ্যালাউ", "alter": "অল্টার",
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
        "complicate": "কমপ্লিকেট", "comply": "কমপ্লাই", "component": "কম্পোনেন্ট", "compose": "কমপোজ",
        "comprehend": "কমপ্রিহেন্ড", "comprehensive": "কমপ্রিহেনসিভ", "compromise": "কমপ্রোমাইজ", "compulsory": "কমপালসরি",
        "conceal": "কনসিল", "concede": "কনসিড", "concentrate": "কনসেনট্রেট", "concept": "কনসেপ্ট",
        "concern": "কনসার্ন", "conclude": "কনক্লুড", "concrete": "কনক্রিট", "concur": "কনকার",
        "condemn": "কনডেম", "condense": "কনডেন্স", "condition": "কন্ডিশন", "conduct": "কন্ডাক্ট",
        "confer": "কনফার", "confidence": "কনফিডেন্স", "confine": "কনফাইন", "confirm": "কনফার্ম",
        "conflict": "কনফ্লিক্ট", "conform": "কনফর্ম", "confuse": "কনফিউজ", "congratulate": "কনগ্র্যাচুলেট",
        "connect": "কানেক্ট", "conquer": "কনকার", "conscientious": "কনশিয়েনশাস", "conscious": "কনশাস",
        "consecutive": "কনসিকিউটিভ", "consensus": "কনসেনসাস", "consent": "কনসেন্ট", "consequence": "কনসিকোয়েন্স",
        "conserve": "কনসার্ভ", "consider": "কনসিডার", "consistent": "কনসিস্টেন্ট", "console": "কনসোল",
        "consolidate": "কনসোলিডেট", "conspicuous": "কনস্পিকুয়াস", "constant": "কনস্ট্যান্ট", "constitute": "কনস্টিটিউট",
        "constrain": "কনস্ট্রেইন", "construct": "কনস্ট্রাক্ট", "consult": "কনসাল্ট", "consume": "কনসিউম",
        "contact": "কনট্যাক্ট", "contain": "কনটেইন", "contemplate": "কনটেমপ্লেট", "contemporary": "কনটেম্পোরারি",
        "contend": "কনটেন্ড", "content": "কনটেন্ট", "contest": "কনটেস্ট", "context": "কনটেক্সট",
        "continual": "কনটিনিউয়াল", "continue": "কনটিনিউ", "contract": "কনট্র্যাক্ট", "contradict": "কনট্রাডিক্ট",
        "contrast": "কনট্রাস্ট", "contribute": "কনট্রিবিউট", "control": "কনট্রোল", "controversy": "কনট্রোভার্সি",
        "convene": "কনভিন", "convenient": "কনভিনিয়েন্ট", "conventional": "কনভেনশনাল", "converge": "কনভার্জ",
        "conversation": "কনভারসেশন", "convert": "কনভার্ট", "convey": "কনভে", "convict": "কনভিক্ট",
        "convince": "কনভিন্স", "cooperate": "কোঅপারেট", "coordinate": "কোঅর্ডিনেট", "cope": "কোপ",
        "cordial": "কর্ডিয়াল", "corporate": "কর্পোরেট", "correct": "কারেক্ট", "correlate": "করেলেট",
        "correspond": "করেসপন্ড", "corrupt": "করাপ্ট", "counsel": "কাউন্সেল", "countless": "কাউন্টলেস",
        "courage": "কারেজ", "courteous": "কার্টিয়াস", "create": "ক্রিয়েট", "credible": "ক্রেডিবল",
        "creditable": "ক্রেডিটেবল", "crisis": "ক্রাইসিস", "criterion": "ক্রাইটেরিয়ন", "critical": "ক্রিটিক্যাল",
        "criticize": "ক্রিটিসাইজ", "crucial": "ক্রুশিয়াল", "crude": "ক্রুড", "cruel": "ক্রুয়েল",
        "culpable": "কালপেবল", "cultivate": "কালটিভেট", "cumbersome": "কামবারসাম", "cumulative": "কিউমুলেটিভ",
        "cunning": "কানিং", "curious": "কিউরিয়াস", "current": "কারেন্ট", "curtail": "কারটেইল",
        "customary": "কাস্টমারি", "damage": "ড্যামেজ", "danger": "ডেঞ্জার", "daring": "ডেয়ারিং",
        "dazzling": "ড্যাজলিং", "deadly": "ডেডলি", "debate": "ডিবেট", "debris": "ডেব্রি",
        "decay": "ডিকে", "deceive": "ডিসাইভ", "decent": "ডিসেন্ট", "decide": "ডিসাইড",
        "decisive": "ডিসাইসিভ", "declare": "ডিক্লেয়ার", "decline": "ডিক্লাইন", "decorous": "ডেকোরাস",
        "decrease": "ডিক্রিজ", "dedicate": "ডেডিকেট", "deduce": "ডিডিউস", "deep": "ডিপ",
        "defeat": "ডিফিট", "defect": "ডিফেক্ট", "defend": "ডিফেন্ড", "defer": "ডিফার",
        "defiant": "ডিফায়েন্ট", "deficient": "ডিফিসিয়েন্ট", "define": "ডিফাইন", "definite": "ডেফিনিট",
        "degrade": "ডিগ্রেড", "delay": "ডিলে", "deliberate": "ডিলিবারেট", "delicate": "ডেলিকেট",
        "delicious": "ডেলিশাস", "delight": "ডিলাইট", "deliver": "ডেলিভার", "delusion": "ডিভিউশন",
        "demand": "ডিমান্ড", "demolish": "ডিমোলিশ", "demonstrate": "ডেমোনস্ট্রেট", "denounce": "ডিনাউন্স",
        "dense": "ডেন্স", "deny": "ডিনাই", "depart": "ডিপার্ট", "depend": "ডিপেন্ড",
        "depict": "ডিপিক্ট", "deplete": "ডিপ্লিট", "deplore": "ডিপ্লোর", "deposit": "ডিপোজিট",
        "deprive": "ডিপ্ৰাইভ", "deride": "ডিরাইড", "derive": "ডিরাইভ", "describe": "ডিসক্রাইব",
        "deserve": "ডিজার্ভ", "design": "ডিজাইন", "desire": "ডিজায়ার", "despair": "ডিসপেয়ার",
        "desperate": "ডিসপারেট", "despise": "ডিসপাইজ", "despite": "ডিসপাইট", "destiny": "ডেস্টিনি",
        "destroy": "ডিসট্রয়", "detach": "ডিটাচ", "detailed": "ডিটেইলড", "detain": "ডিটেইন",
        "detect": "ডিটেক্ট", "deter": "ডিটার", "deteriorate": "ডিটেরিওরেট", "determine": "ডিটারমিন",
        "detest": "ডিটেস্ট", "detrimental": "ডেট্রিমেন্টাল", "devastate": "ডেভাস্টেট", "develop": "ডেভেলপ",
        "deviate": "ডিভিয়েট", "device": "ডিভাইস", "devious": "ডিভিয়াস", "devote": "ডিভোট",
        "dexterous": "ডেক্সটেরাস", "diagnose": "ডায়াগনোজ", "dialogue": "ডায়ালগ", "dichotomy": "ডাইকোটমি",
        "dictate": "ডিক্টেট", "different": "ডিফারেন্ট", "difficult": "ডিফিকাল্ট", "diffuse": "ডিফিউজ",
        "dignity": "ডিগনিটি", "diligent": "ডিলিজেন্ট", "dilute": "ডিলিউট", "diminish": "ডিমিশ",
        "diplomatic": "ডিপ্লোমেটিক", "direct": "ডিরেক্ট", "disaster": "ডিজাস্টার", "disband": "ডিসব্যান্ড",
        "discern": "ডিসার্ন", "discharge": "ডিসচার্জ", "discipline": "ডিসিপ্লিন", "disclose": "ডিসক্লোজ",
        "discomfort": "ডিসকমফোর্ট", "disconnect": "ডিসকানেক্ট", "discontent": "ডিসকনটেন্ট", "discord": "ডিসকর্ড",
        "discount": "ডিসকাউন্ট", "discourage": "ডিসপারেজ", "discourse": "ডিসকোর্স", "discover": "ডিসকাভার",
        "discrepancy": "ডিসক্রেপ্যান্সি", "discrete": "ডিসক্রিট", "discretion": "ডিসক্রেশন", "discriminate": "ডিসক্রিমিনেট",
        "discuss": "ডিসকাস", "disdain": "ডিসডেইন", "disease": "ডিজিজ", "disguise": "ডিসগাইজ",
        "disgust": "ডিসগাস্ট", "dismal": "ডিজমাল", "dismantle": "ডিসম্যান্টল", "dismay": "ডিসমে",
        "dismiss": "ডিসমিস", "disobey": "ডিসওবে", "disparate": "ডিসপ্যারেট", "disparity": "ডিসপ্যারিটি",
        "dispassionate": "ডিসপ্যাশনেট", "dispatch": "ডিসপ্যাচ", "dispel": "ডিসপেল", "dispense": "ডিসপেন্স",
        "disperse": "ডিসপার্স", "displace": "ডিসপ্লেস", "display": "ডিসপ্লে", "displease": "ডিসপ্লিজ",
        "dispose": "ডিসপোজ", "dispute": "ডিসপিউট", "disregard": "ডিসরিগার্ড", "disrupt": "ডিসরাপ্ট",
        "dissect": "ডিসেক্ট", "disseminate": "ডিসেমিনেট", "dissent": "ডিসেন্ট", "dissolve": "ডিসলভ",
        "distant": "ডিসট্যান্ট", "distinct": "ডিসটিংক্ট", "distinguish": "ডিসটিংগুইশ", "distort": "ডিসটর্ট",
        "distract": "ডিসট্র্যাক্ট", "distress": "ডিসট্রেস", "distribute": "ডিসট্রিবিউট", "disturb": "ডিসটার্ব",
        "divergent": "ডাইভারজেন্ট", "diverse": "ডাইভার্স", "divert": "ডাইভার্ট", "divide": "ডিভাইড",
        "divine": "ডিভাইন", "divulge": "ডিভাল্জ", "docile": "ডোসাইল", "doctrine": "ডকট্রিন",
        "domain": "ডোমেইন", "dominant": "ডমিন্যান্ট", "dominate": "ডমিনেট", "donate": "ডোনেট",
        "dormant": "ডরমান্ট", "drastic": "ড্রাস্টিক", "dreadful": "ড্রেডফুল", "dubious": "ডিউবিয়াস",
        "durable": "ডিউরেবল", "dwindle": "ডউইন্ডল", "dynamic": "ডাইনামিক", "eager": "ইগার",
        "earnest": "আর্নেস্ট", "earthly": "আর্থলি", "easy": "ইজি", "eccentric": "একসেন্ট্রিক",
        "economical": "ইকোনমিক্যাল", "ecstasy": "একস্ট্যাসি", "edge": "এজ", "edify": "এডিফাই",
        "educate": "এডুকেশন", "eerie": "ইরি", "effective": "ইফেক্টিভ", "efficient": "ইফিসিয়েন্ট",
        "effort": "ইফোর্ট", "elaborate": "ইলাবোরেট", "elated": "ইলেটেড", "elegant": "এলিগ্যান্ট",
        "element": "এলিমেন্ট", "elevate": "এলিভেট", "eligible": "এলিজিবল", "eliminate": "এলিমিনেট",
        "eloquent": "ইলোকোয়েন্ট", "elucidate": "ইলিউসিডেট", "elusive": "ইলিউসিভ", "embargo": "এমবার্গো",
        "embark": "এমবার্ক", "embarrass": "এমব্যারাস", "embellish": "এমবেলিশ", "emblem": "এমব্লেম",
        "embrace": "এমব্রেস", "emerge": "ইমার্জ", "emergency": "ইমারজেন্সি", "eminent": "এমিনেন্ট",
        "emit": "ইমিট", "emotion": "ইমোশন", "empathy": "এমপ্যাথি", "emphasize": "এমফাসাইজ",
        "empirical": "এমপিরিক্যাল", "employ": "এমপ্লয়", "empower": "এমপাওয়ার", "empty": "এম্পটি",
        "emulate": "এমিউলেট", "enable": "এনেবল", "enact": "এন্যাক্ট", "enchant": "এনচ্যান্ট",
        "encircle": "এনসার্কেল", "enclose": "এনক্লোজ", "encounter": "এনকাউন্টার", "encourage": "এনপারেজ",
        "encroach": "এনক্রোচ", "endanger": "এনডেঞ্জার", "endeavor": "এনডেভার",
        "endless": "এন্ডলেস", "endorse": "এনডোর্স", "endure": "এনডিউর", "enemy": "এনিমি",
        "energetic": "এনার্জেটিক", "enforce": "এনফোর্স", "engage": "এনগেজ", "engender": "এনজেন্ডার",
        "enhance": "এনহ্যান্স", "enigma": "এনিগমা", "enjoy": "এনজয়", "enlarge": "এনলার্জ",
        "enlighten": "এনলাইটেন", "enormous": "ইনরমাস", "enough": "এনাফ", "enquire": "ইনকোয়ার",
        "enrage": "এনরেজ", "enrich": "এনরিচ", "enroll": "এনরোল", "ensue": "এনসিউ",
        "ensure": "এনসিওর", "entail": "এনটেইল", "enter": "এন্টার", "enterprise": "এন্টারপ্রাইজ",
        "entertain": "এন্টারটেইন", "enthusiasm": "এনথুসিয়াজম", "entice": "এনটাইস", "entire": "এন্টায়ার",
        "entitle": "এনটাইটেল", "entity": "এনটিটি", "entrance": "এন্ট্রান্স", "entrust": "এনট্রাস্ট",
        "enumerate": "ইনিউমারেট", "enunciate": "ইনানসিয়েট", "envision": "এনভিশন", "envy": "এনভি",
        "epidemic": "এপিডেমিক", "epitome": "এপিটোম", "equal": "ইকুয়াল", "equitable": "ইকুইটেবল",
        "equivalent": "ইকুইভ্যালেন্ট", "equivocal": "ইকুইভোক্যাল", "eradicate": "ইরাডিকেট", "erase": "ইরেজ",
        "erect": "ইরেক্ট", "erratic": "ইরাটিক", "erroneous": "ইরোনিয়াস", "error": "এরর",
        "erudite": "এরুডাইট", "erupt": "ইরাপ্ট", "escalate": "এসকেলেশন", "escape": "এসকেপ",
        "esoteric": "এসোটেরিক", "essential": "এসেনশিয়াল", "establish": "এস্টাবলিশ", "esteem": "এস্টিম",
        "estimate": "এস্টিমেট", "eternal": "ইটারনাল", "ethical": "ইথিক্যাল",
        "etiquette": "এটিকেট", "eulogy": "ইউলোজি", "euphemism": "ইউফেমিজম", "evacuate": "ইভাকুয়েট",
        "evade": "ইভেড", "evaluate": "ইভালুয়েট", "transient": "ট্রানজিয়েন্ট", "ultimate": "আলটিমেট",
        "unanimous": "ইউনানিমান্স", "uncertain": "আনসার্টেন", "undergo": "আন্ডারগো", "underlying": "আন্ডারলাইং",
        "undermine": "আন্ডারমাইন", "understand": "আন্ডারস্ট্যান্ড", "undertake": "আন্ডারটেক", "unique": "ইউনিক",
        "universal": "ইউনিভার্সাল", "urgent": "আর্জেন্ট", "utilize": "ইউটিলাইজ", "utmost": "আটমোস্ট",
        "vacant": "ভ্যাক্যান্ট", "vague": "ভেগ", "valid": "ভ্যালিড", "valuable": "ভ্যালুয়েবল",
        "vanish": "ভ্যানিশ", "variable": "ভ্যারিয়েবল", "vast": "ভাস্ট", "vehement": "ভিহিমেন্ট",
        "venerate": "ভেনারেট", "veracity": "ভেরাসিটি", "verdict": "ভারডিক্ট", "verify": "ভেরিফাই",
        "versatile": "ভারসাটাইল", "vertical": "ভার্টিক্যাল", "vessel": "ভেসেল", "veteran": "ভেটেরান",
        "vibrant": "ভাইব্রেন্ট", "vicious": "ভিশাস", "victory": "ভিক্টোরি", "vigilant": "ভিজিল্যান্ট",
        "vigorous": "ভিগোরাস", "violate": "ভায়োলেট", "virtue": "ভার্চু", "visible": "ভিজিবল",
        "vision": "ভিশন", "vital": "ভাইটাল", "vivid": "ভিভিড", "vocal": "ভোকাল",
        "vocation": "ভোকেশন", "volatile": "ভোলাটাইল", "voluntary": "ভলান্টারি", "vulnerable": "ভালনারেবল",
        "warrant": "ওয়ারেন্ট", "wealthy": "ওয়েলদি", "welfare": "ওয়েলফেয়ার", "wholesome": "হোলসাম",
        "wisdom": "উইজডম", "withdraw": "উইথড্র", "withstand": "উইথস্ট্যান্ড", "witness": "উইটনেস",
        "worthwhile": "ওয়ার্থহোয়াইল", "zeal": "জিল", "zenith": "জেনিত"
    }

    word_clean = word.lower().strip()
    if word_clean in pronoun_map:
        return pronoun_map[word_clean]
    
    return word.capitalize()

kotlin_file_1 = "app/src/main/java/com/example/ui/screens/tools/VocabularyHighFrequencyDataset.kt"
kotlin_file_2 = "app/src/main/java/com/example/ui/screens/tools/VocabularyDataPacks.kt"

collected_items = []
seen_words = set()

# Seed list
sample_seeds = [
    ("Achieve", "Verb", "এচিভ", "অর্জন করা", ["accomplish", "attain"], ["fail", "lose"], "High"),
    ("Abandon", "Verb", "এ্যাব্যান্ডন", "পরিত্যাগ করা", ["desert", "forsake"], ["retain", "keep"], "High"),
    ("Abate", "Verb", "এ্যাবেট", "হ্রাস পাওয়া", ["subside", "decrease"], ["increase", "intensify"], "High"),
    ("Abbreviate", "Verb", "এ্যাব্রিভিয়েট", "সংক্ষিপ্ত করা", ["shorten", "condense"], ["lengthen", "expand"], "Medium"),
    ("Abdicate", "Verb", "এ্যাবডিকেট", "পদত্যাগ করা", ["resign", "relinquish"], ["claim", "retain"], "Medium"),
    ("Aberration", "Noun", "এ্যাবারেশন", "বিচ্যুতি", ["deviation", "anomaly"], ["normality", "regularity"], "Medium"),
    ("Abhor", "Verb", "এ্যাবহর", "ঘৃণা করা", ["detest", "loathe"], ["love", "admire"], "High"),
    ("Abide", "Verb", "এ্যাবাইড", "মেনে চলা", ["comply", "endure"], ["disobey", "reject"], "High"),
    ("Ability", "Noun", "এ্যাবিলিটি", "সামর্থ্য", ["capability", "skill"], ["inability", "weakness"], "High"),
    ("Abolish", "Verb", "এ্যাবলিশ", "বাতিল করা", ["annul", "eliminate"], ["establish", "enact"], "High")
]

for w, pos, pron, m, syn, ant, freq in sample_seeds:
    seen_words.add(w.lower())
    collected_items.append({
        "word": w,
        "word_type": pos,
        "bn_pronoun": pron,
        "bn_meaning": m,
        "synonyms": syn,
        "antonyms": ant,
        "frequency_level": freq
    })

for file_path in [kotlin_file_1, kotlin_file_2]:
    if os.path.exists(file_path):
        with open(file_path, "r", encoding="utf-8") as f:
            text = f.read()

        triples = re.findall(r'Triple\s*\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*,\s*"([^"]+)"\s*\)', text)
        for w, p, m in triples:
            w_clean = w.strip().capitalize()
            if w_clean.lower() not in seen_words and len(collected_items) < 1000:
                seen_words.add(w_clean.lower())
                collected_items.append({
                    "word": w_clean,
                    "word_type": "Noun" if p == "Noun" else ("Verb" if p == "Verb" else ("Adjective" if p in ["Adj", "Adjective"] else "Adverb")),
                    "bn_pronoun": get_bn_pronoun(w_clean),
                    "bn_meaning": m.strip(),
                    "synonyms": [],
                    "antonyms": [],
                    "frequency_level": "High" if len(collected_items) < 500 else "Medium"
                })

        matches = re.findall(r'word\s*=\s*"([^"]+)"[^,]*,\s*phonetic\s*=\s*"([^"]+)"[^,]*,\s*partOfSpeech\s*=\s*"([^"]+)"[^,]*,\s*meaningBn\s*=\s*"([^"]+)"', text)
        for w, ph, p, m in matches:
            w_clean = w.strip().capitalize()
            if w_clean.lower() not in seen_words and len(collected_items) < 1000:
                seen_words.add(w_clean.lower())
                collected_items.append({
                    "word": w_clean,
                    "word_type": p,
                    "bn_pronoun": get_bn_pronoun(w_clean),
                    "bn_meaning": m.strip(),
                    "synonyms": [],
                    "antonyms": [],
                    "frequency_level": "High"
                })

full_dataset = []
for idx, item in enumerate(collected_items, start=1):
    full_dataset.append({
        "id": idx,
        "word": item["word"],
        "word_type": item["word_type"],
        "bn_pronoun": item["bn_pronoun"],
        "bn_meaning": item["bn_meaning"],
        "synonyms": item["synonyms"],
        "antonyms": item["antonyms"],
        "frequency_level": item["frequency_level"]
    })

assets_dir = "app/src/main/assets"
os.makedirs(assets_dir, exist_ok=True)

asset_path = os.path.join(assets_dir, "dictionary_1000.json")
root_path = "dictionary_1000.json"

with open(asset_path, "w", encoding="utf-8") as f:
    json.dump(full_dataset, f, ensure_ascii=False, indent=2)

with open(root_path, "w", encoding="utf-8") as f:
    json.dump(full_dataset, f, ensure_ascii=False, indent=2)

print(f"SUCCESS: Generated {len(full_dataset)} words JSON dataset!")
print(f"Saved to: {asset_path} and {root_path}")

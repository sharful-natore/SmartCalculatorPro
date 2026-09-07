# -*- coding: utf-8 -*-
import json

with open('app/src/main/assets/dictionary_1000.json') as f:
    existing_data = json.load(f)
existing_words = set(w['word'].strip().lower() for w in existing_data)

words_100_p1 = [
    # 1. Architecture, Structure & Classical Arts (Pack 67)
    {
        "word": "Pergola",
        "pos": "Noun",
        "phonetic": "/ˈpɜːr.ɡəl.ə/ (পারগোলা)",
        "meaningBn": "লতাগুল্মে আচ্ছাদিত বাগান বা পার্কের ছাদযুক্ত হাঁটার পথ",
        "synonyms": ["Arbor", "Trellis", "Bower", "Colonnade"],
        "antonyms": ["Enclosed corridor"],
        "exampleEn": "The stone pathway was shaded by a beautiful wooden pergola covered in blooming wisteria.",
        "exampleBn": "পাথুরে হাঁটার পথটি প্রস্ফুটিত উইস্টেরিয়া লতায় ছাওয়া একটি সুন্দর কাঠের পারগোলা দ্বারা ছায়াযুক্ত ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Minaret",
        "pos": "Noun",
        "phonetic": "/ˌmɪn.əˈrɛt/ (মিনার)",
        "meaningBn": "মসজিদের সুউচ্চ সরু মিনার যেখান থেকে আজান দেওয়া হয়",
        "synonyms": ["Tower", "Turret", "Steeple", "Spire"],
        "antonyms": ["Base", "Basement"],
        "exampleEn": "The ancient mosque featured four slender minarets piercing the desert sky.",
        "exampleBn": "প্রাচীন মসজিদটিতে চারটি সরু সুউচ্চ মিনার ছিল যা মরুভূমির আকাশ ছুঁয়ে ছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Cantilever",
        "pos": "Noun",
        "phonetic": "/ˈkæn.tɪˌliː.vər/ (ক্যান্টিলিভার)",
        "meaningBn": "এক প্রান্তে দৃঢ়ভাবে আবদ্ধ এবং অন্য প্রান্ত মুক্তভাবে ঝুলন্ত অনুভূমিক কাঠামো বা বীম",
        "synonyms": ["Bracket", "Projecting beam", "Overhang support"],
        "antonyms": ["Double-supported span"],
        "exampleEn": "Frank Lloyd Wright's Fallingwater is famous for its dramatic concrete cantilevers.",
        "exampleBn": "ফ্রাঙ্ক লয়েড রাইটের ফলিংওয়াটার বাড়িটি তার নাটকীয় ঝুলন্ত কংক্রিট ক্যান্টিলিভারের জন্য বিখ্যাত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Pediment",
        "pos": "Noun",
        "phonetic": "/ˈpɛd.ɪ.mənt/ (পেডিমেন্ট)",
        "meaningBn": "ধ্রুপদী গ্রিক ও রোমান ভবনের প্রবেশদ্বারের ওপর স্থাপিত ত্রিভুজাকার অলংকৃত চূড়া",
        "synonyms": ["Gable", "Tympanum", "Fronton"],
        "antonyms": ["Flat roof", "Arch"],
        "exampleEn": "The Parthenon's eastern pediment displayed sculpted figures depicting the birth of Athena.",
        "exampleBn": "পার্থেননের পূর্ব পেডিমেন্টে দেবী এথেনার জন্ম বর্ণনাকারী খোদাইকৃত ভাস্কর্য শোভা পেত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Pilaster",
        "pos": "Noun",
        "phonetic": "/pɪˈlæs.tər/ (পিলাস্টার)",
        "meaningBn": "দেয়ালে অর্ধ-প্রবিষ্ট বা খোদাই করা স্তম্ভ যা অলংকরণ ও ভার বহন উভয়ের কাজ করে",
        "synonyms": ["Wall column", "Pier", "Engaged column"],
        "antonyms": ["Free-standing column"],
        "exampleEn": "Fluted Corinthian pilasters framed the grandiose entryway of the neoclassical bank.",
        "exampleBn": "খাঁজকাটা করিন্থিয়ান পিলাস্টারগুলো নব্য-ধ্রুপদী ব্যাংক ভবনের রাজকীয় প্রবেশদ্বারকে অলংকৃত করেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Caryatid",
        "pos": "Noun",
        "phonetic": "/ˌkær.iˈæt.ɪd/ (ক্যারিয়াটিড)",
        "meaningBn": "ভাস্কর্যমণ্ডিত নারীমূর্তি যা স্তম্ভের মতো ভবনের ছাদ বা কার্নিশের ভার বহন করে",
        "synonyms": ["Sculptured female column", "Support figure"],
        "antonyms": ["Atlante (male figure column)"],
        "exampleEn": "Six draped caryatids gracefully support the porch roof of the Erechtheion in Athens.",
        "exampleBn": "এথেন্সের ইরেকথিয়ন মন্দিরের বারান্দার ছাদটি ছয়টি সুশ্রী নারীমূর্তি স্তম্ভ বা ক্যারিয়াটিড দ্বারা ভরপ্রাপ্ত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Entablature",
        "pos": "Noun",
        "phonetic": "/ɪnˈtæb.lə.tʃər/ (এনট্যাবল্যাচার)",
        "meaningBn": "ধ্রুপদী স্তম্ভশীর্ষের ওপর অনুভূমিকভাবে অবস্থিত কাঠামোগত অংশ (আর্কিট্রেভ, ফ্রিজ ও কার্নিশ সমন্বিত)",
        "synonyms": ["Superstructure", "Corniced lintel"],
        "antonyms": ["Column base", "Foundation"],
        "exampleEn": "The heavy marble entablature rested securely on rows of Ionic columns.",
        "exampleBn": "ভারী মার্বেল পাথরের এনট্যাবল্যাচারটি সারিবদ্ধ আয়োনিক স্তম্ভের ওপর দৃঢ়ভাবে স্থাপিত ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Soffit",
        "pos": "Noun",
        "phonetic": "/ˈsɒf.ɪt/ (সফিট)",
        "meaningBn": "কার্নিশ, খিলান বা বারান্দার ঝুলে থাকা অংশের নিচের সমতল তল",
        "synonyms": ["Undersurface", "Ceiling underside", "Intrados"],
        "antonyms": ["Rooftop", "Extrados"],
        "exampleEn": "Recessed spotlights were installed along the soffit to illuminate the patio below.",
        "exampleBn": "নিচের আঙিনাকে আলোকিত করার জন্য কার্নিশের নিচের তলে গোপন স্পটলাইট বসানো হয়েছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Lintel",
        "pos": "Noun",
        "phonetic": "/ˈlɪn.təl/ (লিন্টেল)",
        "meaningBn": "দরজা বা জানালার ওপর অনুভূমিকভাবে স্থাপিত কাঠ, পাথর বা কংক্রিটের ভারবাহী কড়িকাঠ",
        "synonyms": ["Crossbeam", "Transom bar", "Header", "Architrave"],
        "antonyms": ["Sill", "Threshold"],
        "exampleEn": "The ancient stone lintel over the castle doorway bore an inscription dating to 1420.",
        "exampleBn": "দুর্গের দরজার ওপরের প্রাচীন পাথরের লিন্টেলটিতে ১৪২০ খ্রিস্টাব্দের একটি শিলালিপি খোদাই করা ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Transom",
        "pos": "Noun",
        "phonetic": "/ˈtræn.səm/ (ট্র্যানসাম)",
        "meaningBn": "দরজার ওপরের অনুভূমিক পাটাতন বা তার ওপরের ছোট বাতাস চলাচলের জানালা (Transom window)",
        "synonyms": ["Fanlight", "Crossbar", "Overdoor window"],
        "antonyms": ["Doorstep", "Jamb"],
        "exampleEn": "She opened the glass transom above the door to allow cool evening air into the hallway.",
        "exampleBn": "তিনি বারান্দায় শীতল সান্ধ্য বাতাস আসার জন্য দরজার ওপরের ছোট কাঁচের ট্র্যানসাম জানালাটি খুলে দিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Spandrel",
        "pos": "Noun",
        "phonetic": "/ˈspæn.drəl/ (স্প্যান্ড্রেল)",
        "meaningBn": "খিলানের বাইরের বক্ররেখা এবং তার চারপাশের আয়তাকার কাঠামোর মধ্যবর্তী ত্রিভুজাকার স্থান",
        "synonyms": ["Arch corner triangle", "Infill space"],
        "antonyms": ["Keystone", "Crown"],
        "exampleEn": "The mosaic artist filled each spandrel of the cathedral arch with gilded angelic emblems.",
        "exampleBn": "মোজাইক শিল্পী ক্যাথেড্রাল খিলানের প্রতিটি কোণাকার ত্রিভুজ স্থানে সোনালি দেবদূতের প্রতীক আঁকেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Keystone",
        "pos": "Noun",
        "phonetic": "/ˈkiː.stoʊn/ (কিস্টোন)",
        "meaningBn": "খিলানের সর্বোচ্চ মধ্যভাগের পাথর যা সম্পূর্ণ খিলানকে স্বস্থানে দৃঢ়ভাবে আটকে রাখে; মূল ভিত্তিপ্রস্তর",
        "synonyms": ["Crown stone", "Central stone", "Linocut wedge", "Cornerstone"],
        "antonyms": ["Springing stone", "Impost"],
        "exampleEn": "Removing the keystone causes the entire stone arch to immediately collapse under gravity.",
        "exampleBn": "খিলানের মধ্যভাগের কিস্টোনটি সরিয়ে ফেললে পুরো পাথুরে খিলানটি মুহূর্তেই ভেঙে পড়ে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Oculus",
        "pos": "Noun",
        "phonetic": "/ˈɒk.jʊ.ləs/ (অকিউলাস)",
        "meaningBn": "গম্বুজ বা দেওয়ালের মধ্যস্থলে উন্মুক্ত বৃত্তাকার চোখসদৃশ জানালা বা রন্ধ্র",
        "synonyms": ["Eye of dome", "Circular skylight", "Round aperture"],
        "antonyms": ["Blind arch", "Solid dome"],
        "exampleEn": "A beam of sunlight streamed down through the open oculus at the summit of Rome's Pantheon.",
        "exampleBn": "রোমের প্যান্থিয়নের চূড়ায় উন্মুক্ত বৃত্তাকার অকিউলাসের মধ্য দিয়ে সূর্যের আলোকরশ্মি নিচে নামছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Cupola",
        "pos": "Noun",
        "phonetic": "/ˈkjuː.pə.lə/ (কিউপোলা)",
        "meaningBn": "ভবনের ছাদের ওপর নির্মিত ছোট গম্বুজ বা পর্যবেক্ষণ কক্ষ",
        "synonyms": ["Lantern dome", "Turret dome", "Belfry crown"],
        "antonyms": ["Basement", "Subterrane"],
        "exampleEn": "The historic town hall was crowned with a copper cupola containing the town clock.",
        "exampleBn": "ঐতিহাসিক টাউন হলটির শীর্ষে শহরের ঘড়ি সংবলিত একটি তামার তৈরি ছোট কিউপোলা গম্বুজ ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Campanile",
        "pos": "Noun",
        "phonetic": "/ˌkæm.pəˈniː.li/ (ক্যাম্পানিলি)",
        "meaningBn": "গির্জা থেকে আলাদাভাবে নির্মিত মুক্ত ও স্বতন্ত্র ঘণ্টাঘর বা মিনার",
        "synonyms": ["Bell tower", "Free-standing belfry", "Carillon tower"],
        "antonyms": ["Narthex", "Crypt"],
        "exampleEn": "Giotto's campanile rises majestically beside the Florence Cathedral.",
        "exampleBn": "ফ্লোরেন্স ক্যাথেড্রালের পাশে জিয়োটোর ঘণ্টাঘর মিনারটি রাজকীয়ভাবে মাথা তুলে দাঁড়িয়ে আছে।",
        "category": "History & Civilization"
    },
    {
        "word": "Grotesque",
        "pos": "Noun",
        "phonetic": "/ɡroʊˈtɛsk/ (গ্রোটেস্ক)",
        "meaningBn": "অলংকৃত বিকটাকার অবাস্তব পৌরাণিক মূর্তি (গার্গয়েলের মতো কিন্তু যাতে পানি নিষ্কাশনের ছিদ্র থাকে না)",
        "synonyms": ["Chimerical sculpture", "Gargoyle carving", "Bizarre ornament"],
        "antonyms": ["Classical beauty", "Realistic statuary"],
        "exampleEn": "Carved stone grotesques glared down from the ledges of the medieval monastery.",
        "exampleBn": "মধ্যযুগীয় মঠের দেওয়াল কার্নিশ থেকে খোদাই করা অদ্ভুত বিকটাকার পাথুরে মূর্তিগুলো নিচে তাকিয়ে ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Stanchion",
        "pos": "Noun",
        "phonetic": "/ˈstæn.tʃən/ (স্ট্যানশন)",
        "meaningBn": "ভিড় নিয়ন্ত্রণ বা নিরাপত্তা রেলিং ধরে রাখার জন্য ব্যবহৃত খাড়া ধাতব খুঁটি",
        "synonyms": ["Upright support", "Post", "Upright pillar", "Barrier pole"],
        "antonyms": ["Horizontal crossbar"],
        "exampleEn": "Security guards placed stanchions with velvet ropes to organize the museum queue.",
        "exampleBn": "জাদুঘরের লাইন সুশৃঙ্খল করতে নিরাপত্তা রক্ষীরা ভেলভেটের রশিযুক্ত ধাতব স্ট্যানশন খুঁটি বসিয়ে দিল।",
        "category": "Everyday & Social"
    },
    {
        "word": "Bollard",
        "pos": "Noun",
        "phonetic": "/ˈbɒl.ərd/ (বোলার্ড)",
        "meaningBn": "যানবাহন চলাচল রোধে ফুটপাতে বা জাহাজ বাঁধার জন্য জেটিতে পোঁতা শক্ত খাটো খুঁটি",
        "synonyms": ["Mooring post", "Traffic barrier post", "Pylon post"],
        "antonyms": ["Open driveway"],
        "exampleEn": "Heavy steel bollards were installed around the pedestrian plaza to protect walkers from cars.",
        "exampleBn": "পথচারীদের গাড়ি থেকে সুরক্ষিত রাখতে চত্বরের চারপাশে ভারী ইস্পাতের বোলার্ড খুঁটি পোঁতা হয়েছিল।",
        "category": "Everyday & Social"
    },
    {
        "word": "Baluster",
        "pos": "Noun",
        "phonetic": "/ˈbæl.ə.stər/ (ব্যালুস্টার)",
        "meaningBn": "সিঁড়ির রেলিং ধরে রাখার জন্য অলংকৃত ছোট খাড়া খুঁটি বা খাম্বা",
        "synonyms": ["Upright spindle", "Bannister post", "Rail support"],
        "antonyms": ["Handrail", "Tread"],
        "exampleEn": "The grand staircase had a carved mahogany handrail supported by ornate balusters.",
        "exampleBn": "বিশাল সিঁড়িটিতে কারুকাজ করা মেহগনি রেলিং ছিল যা অলংকৃত ব্যালুস্টার খুঁটির ওপর বসানো ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Colonnade",
        "pos": "Noun",
        "phonetic": "/ˌkɒl.əˈneɪd/ (কলোনেড)",
        "meaningBn": "নিয়মিত বিরতিতে সারিবদ্ধ সুউচ্চ স্তম্ভের সারি যা ছাদ বা কার্নিশকে ধারণ করে",
        "synonyms": ["Pillared portico", "Arcade of pillars", "Peristyle walk"],
        "antonyms": ["Single pier", "Uncolumned wall"],
        "exampleEn": "St. Peter's Square in Rome is embraced by Bernini's quadruple colonnade.",
        "exampleBn": "রোমের সেন্ট পিটার্স স্কয়ার বার্নিনির চার সারি বিশিষ্ট সুবৃহৎ কলোনেড বা স্তম্ভশ্রেণী দ্বারা বেষ্টিত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Peristyle",
        "pos": "Noun",
        "phonetic": "/ˈpɛr.ɪˌstaɪl/ (পেরিস্টাইল)",
        "meaningBn": "চারপাশে সারিবদ্ধ স্তম্ভ দ্বারা ঘেরা অভ্যন্তরীণ চত্বর, বাগান বা প্রাঙ্গণ",
        "synonyms": ["Pillared courtyard", "Cloistered court"],
        "antonyms": ["Open terrace"],
        "exampleEn": "Wealthy Roman villas centered around a lush peristyle garden with splashing fountains.",
        "exampleBn": "ধনাঢ্য রোমান বাড়িগুলোর কেন্দ্রে থাকত ফোয়ারা ও সবুজে ঘেরা স্তম্ভবেষ্টিত অভ্যন্তরীণ পেরিস্টাইল প্রাঙ্গণ।",
        "category": "History & Civilization"
    },
    {
        "word": "Atrium",
        "pos": "Noun",
        "phonetic": "/ˈeɪ.tri.əm/ (অ্যাট্রিয়াম)",
        "meaningBn": "ভবনের কেন্দ্রের সুউচ্চ আকাশমুখী খোলা চত্বর বা কাঁচের ছাদযুক্ত মূল হলঘর",
        "synonyms": ["Central court", "Sky-lit hall", "Vestibule"],
        "antonyms": ["Windowless basement"],
        "exampleEn": "Natural light flooded into the high-rise office through its soaring ten-story glass atrium.",
        "exampleBn": "দশ তলা বিশিষ্ট বিশাল কাঁচের অ্যাট্রিয়াম চত্বরের মধ্য দিয়ে অফিস ভবনে প্রাকৃতিক আলো প্লাবিত হচ্ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Hypostyle",
        "pos": "Noun",
        "phonetic": "/ˈhaɪ.pəˌstaɪl/ (হাইপোস্টাইল)",
        "meaningBn": "অসংখ্য স্তম্ভ দ্বারা ছাদ ধরে রাখা বিশাল মিলনায়তন বা মন্দির কক্ষ",
        "synonyms": ["Pillared hall", "Columned chamber"],
        "antonyms": ["Clear-span arena"],
        "exampleEn": "The Great Hypostyle Hall at Karnak contains 134 towering sandstone columns.",
        "exampleBn": "মিশরের কার্নাকের বিশাল হাইপোস্টাইল হলে ১৩৪টি সুউচ্চ বেলেপাথরের স্তম্ভ দাঁড়িয়ে আছে।",
        "category": "History & Civilization"
    },
    {
        "word": "Pylon",
        "pos": "Noun",
        "phonetic": "/ˈpaɪ.lɒn/ (পাইলন)",
        "meaningBn": "প্রাচীন মিশরীয় মন্দিরের বিশাল তোরণ অথবা উচ্চ ভোল্টেজের বিদ্যুৎবাহী সুউচ্চ ধাতব টাওয়ার",
        "synonyms": ["Gateway portal", "Transmission tower", "Monumental gateway"],
        "antonyms": ["Postern gate"],
        "exampleEn": "Carvings of pharaohs victorious in battle decorated the temple's colossal entrance pylon.",
        "exampleBn": "মন্দিরের বিশাল প্রবেশ তোরণ পাইলনে যুদ্ধে বিজয়ী ফারাওদের ভাস্কর্য খোদাই করা ছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Obelisk",
        "pos": "Noun",
        "phonetic": "/ˈɒb.əl.ɪsk/ (ওবেলিস্ক)",
        "meaningBn": "একখণ্ড পাথরে তৈরি চারকোনা ক্রমশ সরু হয়ে শীর্ষে পিরামিডাকৃতি ধারণকারী স্তম্ভ",
        "synonyms": ["Monolith pillar", "Needle monument", "Pyramidion pillar"],
        "antonyms": ["Cenotaph", "Flat slab"],
        "exampleEn": "Cleopatra's Needle is an authentic ancient Egyptian granite obelisk standing by the Thames.",
        "exampleBn": "টেমস নদীর তীরে দাঁড়িয়ে থাকা ক্লিওপেট্রাস নিডল হলো খাঁটি প্রাচীন মিশরীয় গ্রানাইটের তৈরি ওবেলিস্ক।",
        "category": "History & Civilization"
    },
    {
        "word": "Ziggurat",
        "pos": "Noun",
        "phonetic": "/ˈzɪɡ.əˌræt/ (জিগুরাত)",
        "meaningBn": "প্রাচীন মেসোপটেমিয়ার ধাপে ধাপে ওপরে ওঠা পিরামিডসদৃশ বিশাল মন্দির চূড়া",
        "synonyms": ["Terraced temple tower", "Stepped pyramid"],
        "antonyms": ["Underground catacomb"],
        "exampleEn": "The Great Ziggurat of Ur served as an administrative and religious hub in ancient Sumer.",
        "exampleBn": "উরের মহান জিগুরাত প্রাচীন সুমেরে প্রশাসনিক ও ধর্মীয় কেন্দ্রবিন্দু হিসেবে কাজ করত।",
        "category": "History & Civilization"
    },
    {
        "word": "Stupa",
        "pos": "Noun",
        "phonetic": "/ˈstuː.pə/ (স্তূপ)",
        "meaningBn": "বৌদ্ধ ধর্মীয় পবিত্র স্মৃতিচিহ্ন রক্ষিত গম্বুজাকৃতি সৌধ বা স্তূপ",
        "synonyms": ["Buddhist mound", "Reliquary dome", "Chorten"],
        "antonyms": ["Crypt"],
        "exampleEn": "Pilgrims walked clockwise around the Great Stupa of Sanchi in prayer and contemplation.",
        "exampleBn": "তীর্থযাত্রীরা প্রার্থনা ও ধ্যানের সাথে সাঁচীর মহান স্তূপের চারদিকে ঘড়ির কাঁটার অভিমুখে প্রদক্ষিণ করছিলেন।",
        "category": "History & Civilization"
    },
    {
        "word": "Pagoda",
        "pos": "Noun",
        "phonetic": "/pəˈɡoʊ.də/ (প্যাগোডা)",
        "meaningBn": "বহুস্তরবিশিষ্ট ছাদযুক্ত পূর্ব ও দক্ষিণ-পূর্ব এশিয়ার বৌদ্ধ বা তাও ধর্মীয় মন্দির",
        "synonyms": ["Tiered temple tower", "Sacred tower"],
        "antonyms": ["Single-story shrine"],
        "exampleEn": "The seven-storied wooden pagoda survived centuries of earthquakes thanks to its central pillar.",
        "exampleBn": "সাত তলা বিশিষ্ট কাঠের প্যাগোডাটি তার কেন্দ্রীয় স্থিতিস্থাপক কাঠামোর কারণে শত শত ভূমিকম্পে অক্ষত ছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Narthex",
        "pos": "Noun",
        "phonetic": "/ˈnɑːr.θɛks/ (নার্থেক্স)",
        "meaningBn": "প্রাচীন খ্রিস্টান ব্যাসিলিকা বা গির্জার পশ্চিম দিকের প্রবেশ বারান্দা বা অলিন্দ",
        "synonyms": ["Vestibule", "Entrance porch", "Antechamber"],
        "antonyms": ["Sanctuary", "Apse"],
        "exampleEn": "Before entering the nave, worshippers paused in the candlelit narthex to pray.",
        "exampleBn": "গির্জার মূল অংশে প্রবেশের আগে ভক্তরা মোমবাতির আলোয় আলোকিত নার্থেথ্স বারান্দায় দাঁড়িয়ে প্রার্থনা করতেন।",
        "category": "History & Civilization"
    },
    
    # 2. Medical, Physiological & Biological Sciences (Pack 67 continued)
    {
        "word": "Homeostasis",
        "pos": "Noun",
        "phonetic": "/ˌhoʊ.mi.oʊˈsteɪ.sɪs/ (হোমিওস্ট্যাসিস)",
        "meaningBn": "পরিবর্তনশীল পরিবেশে দেহের অভ্যন্তরীণ অবস্থা (যেমন তাপমাত্রা ও রক্তচাপ) স্বাভাবিক ও স্থিতিশীল রাখার প্রক্রিয়া",
        "synonyms": ["Equilibrium", "Internal balance", "Physiological stability"],
        "antonyms": ["Disequilibrium", "Instability", "Decompensation"],
        "exampleEn": "Sweating during intense exercise helps the body maintain temperature homeostasis.",
        "exampleBn": "তীব্র ব্যায়ামের সময় ঘাম বের হওয়া শরীরকে অভ্যন্তরীণ তাপমাত্রার ভারসাম্য বা হোমিওস্ট্যাসিস বজায় রাখতে সাহায্য করে।",
        "category": "Medical & Health"
    },
    {
        "word": "Vasodilation",
        "pos": "Noun",
        "phonetic": "/ˌveɪ.zoʊ.daɪˈleɪ.ʃən/ (ভাসোডিলেশন)",
        "meaningBn": "রক্তনালির প্রাচীরের পেশি শিথিল হয়ে রক্তনালির ব্যাস বৃদ্ধি পাওয়া এবং রক্তপ্রবাহ বৃদ্ধি পাওয়া",
        "synonyms": ["Blood vessel widening", "Vascular relaxation"],
        "antonyms": ["Vasoconstriction"],
        "exampleEn": "Nitroglycerin relieves chest pain by promoting vasodilation of the coronary arteries.",
        "exampleBn": "নাইট্রোগ্লিসারিন করোনারি ধমনির প্রসারণ ঘটিয়ে বা ভাসোডিলেশনের মাধ্যমে বুকের ব্যথা নিরাময় করে।",
        "category": "Medical & Health"
    },
    {
        "word": "Vasoconstriction",
        "pos": "Noun",
        "phonetic": "/ˌveɪ.zoʊ.kənˈstrɪk.ʃən/ (ভাসোকনস্ট্রিকশন)",
        "meaningBn": "রক্তনালির সংকোচন যার ফলে রক্তনালির ব্যাস ছোট হয় এবং রক্তচাপ বৃদ্ধি পায়",
        "synonyms": ["Vascular narrowing", "Blood vessel constriction"],
        "antonyms": ["Vasodilation"],
        "exampleEn": "In cold weather, cutaneous vasoconstriction prevents internal heat loss by shunting blood to vital organs.",
        "exampleBn": "শীতের আবহাওয়ায় চামড়ার রক্তনালির সংকোচন অভ্যন্তরীণ তাপ ক্ষয় রোধ করতে মূল অঙ্গে রক্তপ্রবাহ পাঠায়।",
        "category": "Medical & Health"
    },
    {
        "word": "Ischemia",
        "pos": "Noun",
        "phonetic": "/ɪˈskiː.mi.ə/ (ইসকেমিয়া)",
        "meaningBn": "রক্তনালি সংকুচিত বা অবরুদ্ধ হওয়ার কারণে কোনো অঙ্গে রক্ত ও অক্সিজেনের ঘাটতি",
        "synonyms": ["Deficient blood flow", "Tissue hypoxia"],
        "antonyms": ["Hyperemia (excess blood flow)"],
        "exampleEn": "Myocardial ischemia occurs when arterial plaques restrict blood flow to heart muscle.",
        "exampleBn": "হৃদপিণ্ডের পেশিতে রক্তপ্রবাহ ধমনির চর্বি জমার কারণে বাধাগ্রস্ত হলে মায়োকার্ডিয়াল ইসকেমিয়া সৃষ্টি হয়।",
        "category": "Medical & Health"
    },
    {
        "word": "Hypoxia",
        "pos": "Noun",
        "phonetic": "/haɪˈpɒk.si.ə/ (হাইপোক্সিয়া)",
        "meaningBn": "দেহের কোষ বা কলাসমূহে প্রয়োজনীয় অক্সিজেনের অভাবজনিত অস্বাভাবিক অবস্থা",
        "synonyms": ["Oxygen deficiency", "Tissue anoxia"],
        "antonyms": ["Hyperoxia", "Normoxia"],
        "exampleEn": "Mountaineers climbing Mount Everest use supplemental tanks to prevent high-altitude hypoxia.",
        "exampleBn": "মাউন্ট এভারেস্টে আরোহী পর্বতারোহীরা উচ্চতাজনিত অক্সিজেনের ঘাটতি বা হাইপোক্সিয়া রোধে অক্সিজেন সিলিন্ডার ব্যবহার করেন।",
        "category": "Medical & Health"
    },
    {
        "word": "Anoxia",
        "pos": "Noun",
        "phonetic": "/ænˈɒk.si.ə/ (অ্যানোক্সিয়া)",
        "meaningBn": "কলা বা অঙ্গে অক্সিজেনের সম্পূর্ণ অনুপস্থিতি বা চরম মারাত্মক ঘাটতি",
        "synonyms": ["Total oxygen absence", "Severe oxygen deprivation"],
        "antonyms": ["Oxygenation", "Normoxia"],
        "exampleEn": "Deprived of blood for five minutes, brain cells undergo irreversible damage due to anoxia.",
        "exampleBn": "পাঁচ মিনিট রক্ত না পেলে অক্সিজেনের সম্পূর্ণ শূন্যতা বা অ্যানোক্সিয়ার কারণে মস্তিষ্কের কোষের স্থায়ী ক্ষতি হয়।",
        "category": "Medical & Health"
    },
    {
        "word": "Edema",
        "pos": "Noun",
        "phonetic": "/ɪˈdiː.mə/ (ইডিমা)",
        "meaningBn": "দেহের কলায় বা ত্বকের নিচে অস্বাভাবিকভাবে তরল জমে ফুলে যাওয়া (শোথ রোগ)",
        "synonyms": ["Dropsy", "Fluid retention swelling", "Hydrops"],
        "antonyms": ["Dehydration", "Desiccation"],
        "exampleEn": "The physician checked the patient's ankles for signs of pitting edema caused by heart failure.",
        "exampleBn": "চিকিৎসক হার্ট ফেইলিওরের কারণে পায়ের গোড়ালিতে পানি জমে ফুলে যাওয়া বা ইডিমা পরীক্ষা করলেন।",
        "category": "Medical & Health"
    },
    {
        "word": "Aneurysm",
        "pos": "Noun",
        "phonetic": "/ˈæn.jʊ.rɪz.əm/ (অ্যানিউরিজম)",
        "meaningBn": "ধমনির প্রাচীর দুর্বল হয়ে অস্বাভাবিক ফুলে যাওয়া বা বেলুনের মতো স্ফীত হওয়া",
        "synonyms": ["Arterial dilation", "Vascular bulge"],
        "antonyms": ["Intact arterial wall"],
        "exampleEn": "Brain aneurysms require urgent neurosurgical repair before they rupture and cause hemorrhagic stroke.",
        "exampleBn": "মস্তিষ্কের অ্যানিউরিজম ফেটে গিয়ে রক্তক্ষরণজনিত স্ট্রোক হওয়ার আগেই জরুরি অস্ত্রোপচার প্রয়োজন।",
        "category": "Medical & Health"
    },
    {
        "word": "Thrombosis",
        "pos": "Noun",
        "phonetic": "/θrɒmˈboʊ.sɪs/ (থ্রম্বোসিস)",
        "meaningBn": "রক্তনালির ভেতরে রক্ত জমাট বেঁধে রক্তের স্বাভাবিক প্রবাহে বাধার সৃষ্টি হওয়া",
        "synonyms": ["Blood clot formation", "Intravascular coagulation"],
        "antonyms": ["Thrombolysis (clot dissolution)"],
        "exampleEn": "Long sedentary flights increase the risk of deep vein thrombosis in the lower legs.",
        "exampleBn": "দীর্ঘ সময় নড়াচড়া না করে বিমানে বসে থাকলে পায়ের গভীর শিরায় রক্ত জমাট বাঁধা বা থ্রম্বোসিসের ঝুঁকি বাড়ে।",
        "category": "Medical & Health"
    },
    {
        "word": "Embolism",
        "pos": "Noun",
        "phonetic": "/ˈɛm.bəˌlɪz.əm/ (এমবোলিজম)",
        "meaningBn": "রক্তস্রোতে ভাসমান রক্তপিণ্ড বা বায়ুর বুদবুদ কোনো সরু রক্তনালিকে হঠাৎ পুরোপুরি আটকে দেওয়া",
        "synonyms": ["Arterial blockage", "Vascular occlusion by clot"],
        "antonyms": ["Free circulation"],
        "exampleEn": "A pulmonary embolism occurs when a traveling clot lodges inside the pulmonary arteries of the lungs.",
        "exampleBn": "ফুসফুসীয় এমবোলিজম ঘটে যখন স্থানচ্যুত রক্তপিণ্ড ফুসফুসের ধমনিতে গিয়ে আটকে যায়।",
        "category": "Medical & Health"
    },
    {
        "word": "Hematoma",
        "pos": "Noun",
        "phonetic": "/ˌhiː.məˈtoʊ.mə/ (হেমাটোমা)",
        "meaningBn": "রক্তনালি ফেটে গিয়ে চামড়ার নিচে বা কোনো অঙ্গে রক্ত জমাট বাঁধা (রক্তস্ফীতি বা চাপড়া)",
        "synonyms": ["Blood extravasation", "Localized blood pool", "Contusion mass"],
        "antonyms": ["Intravascular blood"],
        "exampleEn": "The athlete developed a painful subdural hematoma following a direct collision on the football pitch.",
        "exampleBn": "ফুটবল মাঠে সরাসরি সংঘর্ষের পর খেলোয়াড়টির মাথার খুলির নিচে মারাত্মক রক্ত জমাট বাঁধা হেমাটোমা দেখা দেয়।",
        "category": "Medical & Health"
    },
    {
        "word": "Paresthesia",
        "pos": "Noun",
        "phonetic": "/ˌpær.əsˈθiː.ʒə/ (প্যারেসথেসিয়া)",
        "meaningBn": "ত্বকে ঝিঁঝি ধরা, অবশ হওয়া বা সুঁই ফোটার মতো অস্বাভাবিক সংবেদনশীল অনুভূতি",
        "synonyms": ["Pins and needles", "Tingling sensation", "Numbness"],
        "antonyms": ["Normal sensation", "Normoesthesia"],
        "exampleEn": "Compression of the median nerve at the wrist causes nocturnal paresthesia in the fingers.",
        "exampleBn": "কবজিতে মিডিয়ান স্নায়ু চেপে গেলে রাতে আঙুলগুলোতে ঝিঁঝি ধরা বা প্যারেসথেসিয়া অনুভূতি হয়।",
        "category": "Medical & Health"
    },
    {
        "word": "Proprioception",
        "pos": "Noun",
        "phonetic": "/ˌproʊ.pri.oʊˈsɛp.ʃən/ (প্রোপ্রিওসেপশন)",
        "meaningBn": "চোখ বন্ধ রেখেও নিজের শরীরের অঙ্গপ্রত্যঙ্গের সঠিক অবস্থান ও নড়াচড়া অনুভব করার স্নায়বিক ক্ষমতা",
        "synonyms": ["Kinesthesia", "Spatial body awareness"],
        "antonyms": ["Sensory ataxia"],
        "exampleEn": "Gymnasts rely on acute proprioception to execute mid-air flips and stick their landing cleanly.",
        "exampleBn": "জিমন্যাস্টরা শূন্যে ডিগবাজি দিয়ে নিখুঁতভাবে মাটিতে নামতে তাঁদের তীক্ষ্ণ শারীরিক অবস্থান অনুভূতির ওপর নির্ভর করেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Nociception",
        "pos": "Noun",
        "phonetic": "/ˌnoʊ.sɪˈsɛp.ʃən/ (নোসিসেপশন)",
        "meaningBn": "ক্ষতিকর বা পীড়াদায়ক উত্তেজনার প্রতি স্নায়ুতন্ত্রের সংকেত গ্রহণ ও ব্যথাবোধের অনুভূতি সঞ্চালন",
        "synonyms": ["Pain signaling", "Harm perception", "Noxious detection"],
        "antonyms": ["Analgesia", "Anesthesia"],
        "exampleEn": "Thermal nociception prompts you to jerk your finger away from a hot frying pan instantly.",
        "exampleBn": "উত্তাপজনিত ব্যথা সংকেত মুহূর্তের মধ্যেই গরম কড়াই থেকে আপনার আঙুল সরিয়ে নিতে সংকেত পাঠায়।",
        "category": "Science & Environment"
    },
    {
        "word": "Apoptosis",
        "pos": "Noun",
        "phonetic": "/ˌæp.əpˈtoʊ.sɪs/ (অ্যাপোপ্টোসিস)",
        "meaningBn": "পরিকল্পিত ও সুশৃঙ্খল কোষের স্বাভাবিক মৃত্যু (প্রোগ্রামড সেল ডেথ)",
        "synonyms": ["Programmed cell death", "Cellular self-destruction"],
        "antonyms": ["Necrosis (uncontrolled cell death)", "Cell survival"],
        "exampleEn": "During embryonic hand development, webbed fingers separate as intervening cells undergo apoptosis.",
        "exampleBn": "ভ্রূণের হাত তৈরির সময় আঙুলগুলোর মধ্যবর্তী কোষগুলো পরিকল্পিত মৃত্যুর মাধ্যমে বিলুপ্ত হয়ে আঙুলগুলো পৃথক হয়।",
        "category": "Science & Environment"
    },
    {
        "word": "Necrosis",
        "pos": "Noun",
        "phonetic": "/nɪˈkroʊ.sɪs/ (নেক্রোসিস)",
        "meaningBn": "আঘাত, সংক্রমণ বা বিষক্রিয়ার কারণে কোনো কলা বা অঙ্গের কোষের অপমৃত্যু ও পচন",
        "synonyms": ["Cell death", "Tissue gangrene", "Mortification"],
        "antonyms": ["Cellular regeneration", "Tissue viability"],
        "exampleEn": "Venom from the viper bite caused localized tissue necrosis around the wound.",
        "exampleBn": "চন্দ্রবোড়ার বিষের কারণে কামড়ের ক্ষতের চারপাশের কোষ বিনষ্ট হয়ে পচন বা নেক্রোসিস দেখা দেয়।",
        "category": "Medical & Health"
    },
    {
        "word": "Phagocytosis",
        "pos": "Noun",
        "phonetic": "/ˌfæɡ.ə.saɪˈtoʊ.sɪs/ (ফ্যাগোসাইটোসিস)",
        "meaningBn": "শ্বেত রক্তকণিকা কর্তৃক ক্ষতিকর জীবাণু বা বহিরাগত কণা গিলে ফেলে ধ্বংস করার জৈবনিক প্রক্রিয়া",
        "synonyms": ["Cellular engulfment", "Microbe ingestion"],
        "antonyms": ["Exocytosis"],
        "exampleEn": "Macrophage cells defend the bloodstream by destroying bacteria through rapid phagocytosis.",
        "exampleBn": "ম্যাক্রোফেজ কোষগুলো দ্রুত ফ্যাগোসাইটোসিস প্রক্রিয়ায় ব্যাকটেরিয়া গিলে ফেলে রক্তকে সুরক্ষিত রাখে।",
        "category": "Science & Environment"
    },
    {
        "word": "Cytokinesis",
        "pos": "Noun",
        "phonetic": "/ˌsaɪ.toʊ.kɪˈniː.sɪs/ (সাইটোকাইনেসিস)",
        "meaningBn": "কোষ বিভাজনের শেষ ধাপে সাইটোপ্লাজম বিভক্ত হয়ে দুটি নতুন অপত্য কোষ সৃষ্টির প্রক্রিয়া",
        "synonyms": ["Cytoplasm cleavage", "Cell division completion"],
        "antonyms": ["Karyokinesis (nuclear division)"],
        "exampleEn": "During plant cytokinesis, a cell plate forms along the center to build new cell walls.",
        "exampleBn": "উদ্ভিদের সাইটোকাইনেসিসের সময় কেন্দ্রের মধ্য দিয়ে সেল প্লেট গঠিত হয়ে নতুন কোষপ্রাচীর তৈরি হয়।",
        "category": "Science & Environment"
    },
    {
        "word": "Synapse",
        "pos": "Noun",
        "phonetic": "/ˈsɪn.æps/ (সিন্যাপস)",
        "meaningBn": "দুটি স্নায়ুকোষের সংযোগস্থল যার মধ্য দিয়ে স্নায়ু উদ্দীপনা এক নিউরন থেকে অন্য নিউরনে প্রবাহিত হয়",
        "synonyms": ["Neural junction", "Neuronal gap", "Intercellular contact"],
        "antonyms": ["Axon trunk"],
        "exampleEn": "Neurotransmitters diffuse across the microscopic synaptic cleft to bind with target receptors.",
        "exampleBn": "নিউরোট্রান্সমিটারগুলো সূক্ষ্ম সিন্যাপস ফাঁক দিয়ে প্রবাহিত হয়ে লক্ষ্য গ্রাহক কোষে আবদ্ধ হয়।",
        "category": "Science & Environment"
    },
    {
        "word": "Neurotransmitter",
        "pos": "Noun",
        "phonetic": "/ˌnjʊər.oʊ.trænzˈmɪt.ər/ (নিউরোট্রান্সমিটার)",
        "meaningBn": "স্নায়ু উদ্দীপনা বহনকারী রাসায়নিক বার্তাবাহক (যেমন ডোপামিন বা সেরোটোনিন)",
        "synonyms": ["Chemical messenger", "Neural transmitter molecule"],
        "antonyms": ["Inert molecule"],
        "exampleEn": "Dopamine is a critical neurotransmitter governing motor control and reward pathways in the brain.",
        "exampleBn": "ডোপামিন হলো একটি গুরুত্বপূর্ণ নিউরোট্রান্সমিটার যা মস্তিষ্কের অঙ্গসঞ্চালন ও ভালো লাগার অনুভূতি নিয়ন্ত্রণ করে।",
        "category": "Science & Environment"
    },
    {
        "word": "Myelin",
        "pos": "Noun",
        "phonetic": "/ˈmaɪ.ə.lɪn/ (মায়েলিন)",
        "meaningBn": "স্নায়ুতন্তুকে আবৃতকারী চর্বিজাতীয় অন্তরণী স্তর যা স্নায়ু সংকেতের দ্রুত প্রবাহ নিশ্চিত করে",
        "synonyms": ["Nerve sheath", "Medullary substance"],
        "antonyms": ["Unmyelinated fiber"],
        "exampleEn": "Multiple sclerosis damages the protective myelin sheath surrounding central nerve fibers.",
        "exampleBn": "মাল্টিপল স্ক্লেরোসিস রোগ কেন্দ্রীয় স্নায়ুতন্তুর চারপাশের সুরক্ষাকারী মায়েলিন স্তরকে ক্ষতিগ্রস্ত করে।",
        "category": "Medical & Health"
    },
    {
        "word": "Dendrite",
        "pos": "Noun",
        "phonetic": "/ˈdɛn.draɪt/ (ডেনড্রাইট)",
        "meaningBn": "নিউরনের শাখাপ্রশাখা যা অন্য স্নায়ুকোষ থেকে সংকেত বা উদ্দীপনা গ্রহণ করে",
        "synonyms": ["Neuronal branch", "Receptor extension"],
        "antonyms": ["Axon terminal"],
        "exampleEn": "Each cortical neuron receives signals through thousands of branching dendrites.",
        "exampleBn": "মস্তিষ্কের প্রতিটি নিউরন হাজার হাজার শাখাযুক্ত ডেনড্রাইটের মাধ্যমে সংকেত গ্রহণ করে।",
        "category": "Science & Environment"
    },
    {
        "word": "Glial",
        "pos": "Adjective",
        "phonetic": "/ˈɡlaɪ.əl/ (গ্লিয়াল)",
        "meaningBn": "স্নায়ুতন্ত্রের সহায়ক বা নিউরোগ্লিয়া কোষসংক্রান্ত (যা নিউরনকে পুষ্টি ও সুরক্ষা দেয়)",
        "synonyms": ["Neuroglial", "Supporting nerve tissue"],
        "antonyms": ["Neuronal"],
        "exampleEn": "Astrocytes are specialized glial cells that maintain the vital blood-brain barrier.",
        "exampleBn": "অ্যাস্ট্রোসাইট হলো বিশেষ গ্লিয়াল কোষ যা রক্তের ক্ষতিকর উপাদান থেকে মস্তিষ্ককে সুরক্ষিত রাখে।",
        "category": "Science & Environment"
    },
    {
        "word": "Epitope",
        "pos": "Noun",
        "phonetic": "/ˈɛp.ɪ.toʊp/ (এপিটোপ)",
        "meaningBn": "অ্যান্টিজেনের যে নির্দিষ্ট ক্ষুদ্র অংশকে অ্যান্টিবডি বা ইমিউন কোষ শনাক্ত করে আক্রমণ করে",
        "synonyms": ["Antigenic determinant", "Immune binding site"],
        "antonyms": ["Paratope (antibody binding cleft)"],
        "exampleEn": "Vaccine engineers designed synthetic molecules mimicking the spike protein's key epitope.",
        "exampleBn": "টিকা প্রস্তুতকারী প্রকৌশলীরা স্পাইক প্রোটিনের প্রধান এপিটোপ নকল করে কৃত্রিম অণু তৈরি করেছিলেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Leukocyte",
        "pos": "Noun",
        "phonetic": "/ˈluː.kəˌsaɪt/ (লিউকোসাইট)",
        "meaningBn": "শ্বেত রক্তকণিকা যা শরীরকে রোগজীবাণু ও সংক্রমণ থেকে রক্ষা করে",
        "synonyms": ["White blood cell", "Immunocyte"],
        "antonyms": ["Erythrocyte (red blood cell)"],
        "exampleEn": "An elevated leukocyte count on the laboratory report signaled an ongoing bacterial infection.",
        "exampleBn": "ল্যাবরেটরি রিপোর্টে শ্বেত রক্তকণিকা বা লিউকোসাইটের আধিক্য চলমান ব্যাকটেরিয়া সংক্রমণের ইঙ্গিত দিয়েছিল।",
        "category": "Medical & Health"
    },
    {
        "word": "Erythrocyte",
        "pos": "Noun",
        "phonetic": "/ɪˈrɪθ.rəˌsaɪt/ (এরিথ্রোসাইট)",
        "meaningBn": "লোহিত রক্তকণিকা যা ফুসফুস থেকে সারা দেহে অক্সিজেন পরিবহন করে",
        "synonyms": ["Red blood cell", "RBC"],
        "antonyms": ["Leukocyte"],
        "exampleEn": "Hemoglobin molecules packed inside each erythrocyte bind reversibly with oxygen atoms.",
        "exampleBn": "প্রতিটি লোহিত রক্তকণিকা বা এরিথ্রোসাইটের ভেতরের হিমোগ্লোবিন অণু অক্সিজেনের সাথে যুক্ত হয়ে তা পরিবহন করে।",
        "category": "Science & Environment"
    },
    {
        "word": "Thrombocyte",
        "pos": "Noun",
        "phonetic": "/ˈθrɒm.bəˌsaɪt/ (থ্রম্বোসাইট)",
        "meaningBn": "অনুচক্রিকা বা রক্ত জমাট বাঁধতে সাহায্যকারী রক্তকণিকা (Platelet)",
        "synonyms": ["Platelet", "Clotting cell"],
        "antonyms": ["Anticoagulant"],
        "exampleEn": "Patients with severe dengue require close monitoring to prevent dangerous drops in thrombocyte levels.",
        "exampleBn": "মারাত্মক ডেঙ্গু রোগীদের অনুচক্রিকা বা থ্রম্বোসাইটের মাত্রা বিপজ্জনকভাবে কমে যাওয়া রোধে সার্বক্ষণিক পর্যবেক্ষণ প্রয়োজন।",
        "category": "Medical & Health"
    },
    {
        "word": "Myoglobin",
        "pos": "Noun",
        "phonetic": "/ˈmaɪ.əˌɡloʊ.bɪn/ (মায়োগ্লোবিন)",
        "meaningBn": "পেশিকোষে অবস্থিত আয়রনযুক্ত প্রোটিন যা অক্সিজেন সঞ্চয় করে রাখে",
        "synonyms": ["Muscle oxygen protein", "Heme muscle pigment"],
        "antonyms": ["Hemoglobin (in blood)"],
        "exampleEn": "Diving mammals like seals possess abundant myoglobin allowing them to stay submerged for an hour.",
        "exampleBn": "সিল মাছের মতো ডুবুরি স্তন্যপায়ীদের পেশিতে প্রচুর মায়োগ্লোবিন থাকায় তারা এক ঘণ্টা পর্যন্ত পানিতে ডুব দিয়ে থাকতে পারে।",
        "category": "Science & Environment"
    },
    {
        "word": "Peristalsis",
        "pos": "Noun",
        "phonetic": "/ˌpɛr.ɪˈstæl.sɪs/ (পেরিস্টালসিস)",
        "meaningBn": "পরিপাকনালির বৃত্তাকার পেশির ছন্দোবদ্ধ সংকোচন-প্রসারণ যার মাধ্যমে খাদ্য নিচে নেমে যায়",
        "synonyms": ["Involuntary muscular wave", "Digestive motility wave"],
        "antonyms": ["Stasis", "Paralytic ileus"],
        "exampleEn": "Intestinal peristalsis propels digesting food along the gastrointestinal tract smoothly.",
        "exampleBn": "অন্ত্রের পেরিস্টালসিস ঢেউ হজম হতে থাকা খাদ্যবস্তুকে পৌষ্টিকনালি দিয়ে মসৃণভাবে সামনের দিকে ধাবিত করে।",
        "category": "Science & Environment"
    },
    {
        "word": "Gastrulation",
        "pos": "Noun",
        "phonetic": "/ˌɡæs.trʊˈleɪ.ʃən/ (গ্যাস্ট্রুলেশন)",
        "meaningBn": "ভ্রূণবিকাশের যে দশায় ব্লাস্টুলা থেকে একনালী একস্তর বিশিষ্ট ভ্রূণ তিনটি মৌলিক স্তরে (এক্টোডার্ম, মেসোডার্ম, এন্ডোডার্ম) রূপান্তরিত হয়",
        "synonyms": ["Germ layer formation", "Embryonic invagination"],
        "antonyms": ["Blastulation"],
        "exampleEn": "Gastrulation is the defining developmental event where the three primary germ layers arise.",
        "exampleBn": "গ্যাস্ট্রুলেশন হলো ভ্রূণের বিকাশের সেই প্রধান ধাপ যেখানে তিনটি মৌলিক প্রাথমিক ভ্রূণস্তর গঠিত হয়।",
        "category": "Science & Environment"
    },
    {
        "word": "Blastula",
        "pos": "Noun",
        "phonetic": "/ˈblæs.tʃʊ.lə/ (ব্লাস্টুলা)",
        "meaningBn": "জাইগোটের ক্লিভেজ বিভাজনের ফলে সৃষ্ট ফাঁপা তরলপূর্ণ গোলকাকৃতির প্রাথমিক ভ্রূণীয় দশা",
        "synonyms": ["Blastosphere", "Hollow embryonic sphere"],
        "antonyms": ["Morula (solid ball)", "Gastrula"],
        "exampleEn": "The fertilized egg divided rapidly until it became a fluid-filled blastula.",
        "exampleBn": "নিষিক্ত ডিম্বাণুটি দ্রুত বিভাজিত হয়ে তরলপূর্ণ ফাঁপা গোলাকার ব্লাস্টুলা দশায় পরিণত হয়েছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Zygote",
        "pos": "Noun",
        "phonetic": "/ˈzaɪ.ɡoʊt/ (জাইগোট)",
        "meaningBn": "ডিম্বাণু ও শুক্রাণুর মিলনে সৃষ্ট একক ডিপ্লয়েড কোষ বা নিষিক্ত ডিম্বাণু",
        "synonyms": ["Fertilized ovum", "Diploid conceptus"],
        "antonyms": ["Gamete (unfertilized sex cell)"],
        "exampleEn": "Life begins sexually when sperm fertilizes the ovum to produce a single-celled zygote.",
        "exampleBn": "যৌন জননে ডিম্বাণুর সাথে শুক্রাণুর মিলনে এককোষী জাইগোট সৃষ্টির মধ্য দিয়ে জীবনের সূচনা ঘটে।",
        "category": "Science & Environment"
    },
    {
        "word": "Haploid",
        "pos": "Adjective",
        "phonetic": "/ˈhæp.lɔɪd/ (হ্যাপ্লয়েড)",
        "meaningBn": "এক সেট ক্রোমোজোম বিশিষ্ট কোষ (যেমন শুক্রাণু বা ডিম্বাণু)",
        "synonyms": ["Single-set chromosome", "Monoploid"],
        "antonyms": ["Diploid", "Polyploid"],
        "exampleEn": "Human gametes are haploid cells containing twenty-three individual chromosomes.",
        "exampleBn": "মানুষের জননকোষ হলো হ্যাপ্লয়েড কোষ যাতে তেইশটি একক ক্রোমোজোম থাকে।",
        "category": "Science & Environment"
    },
    {
        "word": "Diploid",
        "pos": "Adjective",
        "phonetic": "/ˈdɪp.lɔɪd/ (ডিপ্লয়েড)",
        "meaningBn": "দুই সেট পূর্ণ ক্রোমোজোম বিশিষ্ট দেহকোষ (এক সেট পিতার ও এক সেট মাতার)",
        "synonyms": ["Double-set chromosome", "Paired-chromosome cell"],
        "antonyms": ["Haploid"],
        "exampleEn": "Somatic human cells are diploid, having forty-six chromosomes arranged in twenty-three pairs.",
        "exampleBn": "মানুষের দেহকোষগুলো ডিপ্লয়েড, যার মধ্যে তেইশ জোড়ায় বিভক্ত মোট ছেচল্লিশটি ক্রোমোজোম রয়েছে।",
        "category": "Science & Environment"
    },
    {
        "word": "Genotype",
        "pos": "Noun",
        "phonetic": "/ˈdʒiː.nəˌtaɪp/ (জিনোটাইপ)",
        "meaningBn": "জীবের জিনগত গঠন বা অভ্যন্তরীণ বংশগত বৈশিষ্ট্যসমূহ",
        "synonyms": ["Genetic constitution", "Allelic composition"],
        "antonyms": ["Phenotype (visible traits)"],
        "exampleEn": "Environmental factors interact with an organism's genotype to determine its observable traits.",
        "exampleBn": "পরিবেশগত প্রভাব জীবের জিনোটাইপের সাথে মিথস্ক্রিয়া করে তার বাহ্যিক বৈশিষ্ট্য নির্ধারণ করে।",
        "category": "Science & Environment"
    },
    {
        "word": "Phenotype",
        "pos": "Noun",
        "phonetic": "/ˈfiː.nəˌtaɪp/ (ফিনোটাইপ)",
        "meaningBn": "জীবের বাহ্যিকভাবে দৃশ্যমান শারীরিক বৈশিষ্ট্য (যেমন গায়ের রং, উচ্চতা)",
        "synonyms": ["Observable trait", "Physical manifestation"],
        "antonyms": ["Genotype"],
        "exampleEn": "Eye color and adult stature are distinct components of an individual's physical phenotype.",
        "exampleBn": "চোখের রঙ এবং প্রাপ্তবয়স্কদের উচ্চতা হলো ব্যক্তির বাহ্যিক ফিনোটাইপের অন্যতম দৃশ্যমান উপাদান।",
        "category": "Science & Environment"
    },
    {
        "word": "Allele",
        "pos": "Noun",
        "phonetic": "/əˈliːl/ (অ্যালিল)",
        "meaningBn": "হোমোলোগাস ক্রোমোজোমের নির্দিষ্ট লোকাসে অবস্থিত জিনের এক বা একাধিক বৈকল্পিক রূপ",
        "synonyms": ["Gene variant", "Alternative form of gene"],
        "antonyms": ["Locus (location)"],
        "exampleEn": "Mendel demonstrated how dominant and recessive alleles govern pea plant seed textures.",
        "exampleBn": "মেন্ডেল প্রমাণ করেছিলেন কীভাবে প্রকট ও প্রচ্ছন্ন অ্যালিলগুলো মটরশুঁটির বীজের গঠন নিয়ন্ত্রণ করে।",
        "category": "Science & Environment"
    },
    
    # 3. Earth Sciences, Geology & Topography (Pack 67 continued)
    {
        "word": "Permafrost",
        "pos": "Noun",
        "phonetic": "/ˈpɜːr.məˌfrɒst/ (পারমাফ্রস্ট)",
        "meaningBn": "তুন্দ্রা অঞ্চলের স্থায়ীভাবে জমে বরফ হয়ে থাকা মাটির স্তর যা দুই বা ততোধিক বছর অবিরাম বরফাবৃত থাকে",
        "synonyms": ["Permanently frozen ground", "Subsurface pergelisol"],
        "antonyms": ["Thawed soil", "Mollisol"],
        "exampleEn": "Global warming causes Arctic permafrost to thaw, releasing ancient trapped methane into the air.",
        "exampleBn": "বৈশ্বিক উষ্ণায়নের ফলে সুমেরুর স্থায়ী বরফমাটি বা পারমাফ্রস্ট গলে গিয়ে জমে থাকা মিথেন গ্যাস বাতাসে নির্গত হচ্ছে।",
        "category": "Science & Environment"
    },
    {
        "word": "Regolith",
        "pos": "Noun",
        "phonetic": "/ˈrɛɡ.əˌlɪθ/ (রেগোলিথ)",
        "meaningBn": "মূল শয্যাশিলার ওপর জমে থাকা শিথিল পাথরকুচি, ধূলিকণা ও মাটির আলগা আস্তরণ (যেমন চাঁদের ধূলিস্তর)",
        "synonyms": ["Blanket rock", "Loose surface mantle", "Unconsolidated sediment"],
        "antonyms": ["Bedrock"],
        "exampleEn": "Apollo astronauts left footprints preserved in the fine lunar regolith covering the Moon's surface.",
        "exampleBn": "অ্যাপোলো নভোচারীরা চাঁদের পৃষ্ঠের মিহি রেগোলিথ ধূলিস্তরে তাঁদের পদচিহ্ন রেখে এসেছিলেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Scree",
        "pos": "Noun",
        "phonetic": "/skriː/ (স্ক্রী)",
        "meaningBn": "পাহাড়ের ঢালে বা পর্বতের পাদদেশে জমা হওয়া খাড়া আলগা নুড়ি ও ভাঙা পাথরের স্তূপ (Talus)",
        "synonyms": ["Talus", "Rock slope debris", "Loose scree slope"],
        "antonyms": ["Solid cliff face"],
        "exampleEn": "Hikers slipped repeatedly while traversing the steep scree on the volcanic ridge.",
        "exampleBn": "আগ্নেয়গিরির ঢালে খাড়া পিচ্ছিল নুড়িপাথরের স্ক্রী পার হওয়ার সময় পর্বতারোহীরা বারবার পিছলে যাচ্ছিলেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Moraine",
        "pos": "Noun",
        "phonetic": "/məˈreɪn/ (মোরেইন / গ্রাবরেখা)",
        "meaningBn": "হিমবাহ নেমে আসার সময় সাথে বয়ে আনা এবং জমিয়ে রাখা পাথর, মাটি ও বালির স্তূপ",
        "synonyms": ["Glacial debris ridge", "Glacial drift"],
        "antonyms": ["Fluvial delta"],
        "exampleEn": "The terminal moraine marked the furthest point reached by the glacier before it retreated.",
        "exampleBn": "প্রান্তিক গ্রাবরেখা বা মোরেইন চিহ্নিত করে হিমবাহটি গলে যাওয়ার আগে সর্বোচ্চ কত দূর পর্যন্ত বিস্তৃত হয়েছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Drumlin",
        "pos": "Noun",
        "phonetic": "/ˈdrʌm.lɪn/ (ড্রামলিন)",
        "meaningBn": "হিমবাহের অবক্ষেপণের ফলে গঠিত উল্টানো নৌকার মতো ডিম্বাকৃতির মসৃণ টিলা",
        "synonyms": ["Glacial oval hill", "Elongated hillock"],
        "antonyms": ["Glacial kettle hole"],
        "exampleEn": "The undulating farm valley featured swarms of drumlins formed by Ice Age glaciers.",
        "exampleBn": "তরঙ্গায়িত ফসলি উপত্যকাটিতে বরফ যুগের হিমবাহ দ্বারা গঠিত ডিম্বাকৃতি ড্রামলিন টিলার সমাহার ছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Esker",
        "pos": "Noun",
        "phonetic": "/ˈɛs.kər/ (এসকার)",
        "meaningBn": "গলন্ত হিমবাহের নিচের জলস্রোতের মাধ্যমে সঞ্চিত বালি ও নুড়িপাথরের দীর্ঘ আঁকাবাঁকা শৈলশিরা",
        "synonyms": ["Glacial gravel ridge", "Osar"],
        "antonyms": ["Cirque"],
        "exampleEn": "Road builders often quarry gravel from ancient eskers winding through the boreal forest.",
        "exampleBn": "উত্তর বরফবৃত বনের মধ্য দিয়ে যাওয়া আঁকাবাঁকা প্রাচীন এসকার শৈলশিরা থেকে সড়ক নির্মাতারা প্রায়ই নুড়ি সংগ্রহ করেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Cirque",
        "pos": "Noun",
        "phonetic": "/sɜːrk/ (সার্ক / করি)",
        "meaningBn": "হিমবাহের ক্ষয়কাজের ফলে পর্বতের গায়ে সৃষ্ট অর্ধবৃত্তাকার খাড়া দেয়ালযুক্ত আরামকেদারাসদৃশ গহ্বর",
        "synonyms": ["Corrie", "Cwm", "Glacial amphitheater"],
        "antonyms": ["Mountain peak"],
        "exampleEn": "A crystal blue tarn lake rested peacefully at the bottom of the alpine cirque.",
        "exampleBn": "আলপাইন পর্বতের খাড়া সার্ক গহ্বরের তলদেশে স্ফটিক স্বচ্ছ নীল হ্রদ শান্তভাবে অবস্থান করছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Arete",
        "pos": "Noun",
        "phonetic": "/əˈreɪt/ (অ্যারেত)",
        "meaningBn": "পাশাপাশি দুটি হিমবাহ উপত্যকার মধ্যবর্তী ছুরির ধারের মতো ধারালো পাথুরে সংকীর্ণ শৈলশিরা",
        "synonyms": ["Knife-edge ridge", "Sharp glacial arête"],
        "antonyms": ["Broad plateau"],
        "exampleEn": "Climbers carefully straddled the icy arete with steep drop-offs on both sides.",
        "exampleBn": "আরোহীরা সাবধানে উভয় পাশে গভীর খাদ সংবলিত ছুরির ধারের মতো ধারালো পাথুরে অ্যারেত অতিক্রম করছিলেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Caldera",
        "pos": "Noun",
        "phonetic": "/kælˈdɛər.ə/ (ক্যালডেরা)",
        "meaningBn": "আগ্নেয়গিরির জ্বালামুখের বিধ্বংসী বিস্ফোরণ বা ধসের ফলে গঠিত বিশাল কড়াইসদৃশ গভীর গহ্বর",
        "synonyms": ["Volcanic depression", "Crater basin"],
        "antonyms": ["Volcanic cone"],
        "exampleEn": "Crater Lake in Oregon fills a massive collapsed volcanic caldera formed 7,700 years ago.",
        "exampleBn": "ওরেগনের ক্রেটার লেক ৭৭০০ বছর আগে গঠিত বিশাল ধ্বসে যাওয়া আগ্নেয় ক্যালডেরা গহ্বর পূর্ণ করে রয়েছে।",
        "category": "Science & Environment"
    },
    {
        "word": "Fumarole",
        "pos": "Noun",
        "phonetic": "/ˈfjuː.məˌroʊl/ (ফিউমারোল)",
        "meaningBn": "আগ্নেয়গিরির ভূত্বকে সৃষ্ট মুখ যেখান থেকে সালফারযুক্ত গরম ধোঁয়া ও বাষ্প বের হয় (বাষ্পমুখ)",
        "synonyms": ["Steam vent", "Volcanic gas vent", "Solfatara"],
        "antonyms": ["Geyser (liquid water eruption)"],
        "exampleEn": "Hissing fumaroles released pungent clouds of sulfurous steam across the geothermal field.",
        "exampleBn": "ভূ-তাপীয় অঞ্চল জুড়ে হিসহিস শব্দে বাষ্পমুখগুলো তীব্র গন্ধযুক্ত সালফার বাষ্প বাতাসে ছড়াচ্ছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Solfatara",
        "pos": "Noun",
        "phonetic": "/ˌsɒl.fəˈtɑː.rə/ (সলফাটারা)",
        "meaningBn": "যে বাষ্পমুখ থেকে প্রধানত সালফার ও গন্ধকযুক্ত ধোঁয়া ও গ্যাস নির্গত হয়",
        "synonyms": ["Sulfur vent", "Sulfuric fumarole"],
        "antonyms": ["Clean air crater"],
        "exampleEn": "Yellow sulfur crystals precipitated around the rim of the active solfatara.",
        "exampleBn": "সক্রিয় সলফাটারা বাষ্পমুখের কিনারায় হলুদ সালফার বা গন্ধকের স্ফটিক জমা হচ্ছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Cenote",
        "pos": "Noun",
        "phonetic": "/sɪˈnoʊ.ti/ (সিনোটি)",
        "meaningBn": "চুনাপাথরের ছাদ ধসে গিয়ে প্রাকৃতিক ভূগর্ভস্থ বিশুদ্ধ পানির জলাশয় (ইউকাটান উপদ্বীপে জনপ্রিয়)",
        "synonyms": ["Limestone sinkhole pool", "Natural groundwater pit"],
        "antonyms": ["Dry desert basin"],
        "exampleEn": "Ancient Mayans regarded sacred cenotes as mystical portals to the underworld.",
        "exampleBn": "প্রাচীন মায়ারা প্রাকৃতিক পাতাল সিনোটি জলাশয়কে পাতালপুরীর পবিত্র প্রবেশদ্বার মনে করত।",
        "category": "History & Civilization"
    },
    {
        "word": "Guyot",
        "pos": "Noun",
        "phonetic": "/ˈɡiː.oʊ/ (গিয়ট)",
        "meaningBn": "সমুদ্রের তলদেশে অবস্থিত সমতল শীর্ষবিশিষ্ট ডুবন্ত প্রাচীন আগ্নেয়গিরি পর্বত (টেবিলমাউন্ট)",
        "synonyms": ["Tablemount", "Flat-topped seamount"],
        "antonyms": ["Pointed seamount"],
        "exampleEn": "Oceanographers charted an isolated underwater guyot rising thousands of meters from the ocean floor.",
        "exampleBn": "সমুদ্রবিজ্ঞানীরা সমুদ্রের তলদেশ থেকে হাজার মিটার উঁচুতে ওঠা সমতল শীর্ষবিশিষ্ট নিমজ্জিত গিয়ট পাহাড়ের মানচিত্র আঁকেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Butte",
        "pos": "Noun",
        "phonetic": "/bjuːt/ (বিউট)",
        "meaningBn": "মরু অঞ্চলে অবস্থিত খাড়া ঢালযুক্ত কিন্তু ছোট সমতল শীর্ষবিশিষ্ট বিচ্ছিন্ন পাথুরে পাহাড়",
        "synonyms": ["Isolated steep hill", "Flat-topped mound"],
        "antonyms": ["Broad mesa", "Sprawling valley"],
        "exampleEn": "The lonely sandstone butte stood like a sentinel over the Arizona desert.",
        "exampleBn": "একাকী বেলেপাথরের খাড়া বিউট পাহাড়টি অ্যারিজোনার মরুভূমির ওপর প্রহরীর মতো দাঁড়িয়ে ছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Speleothem",
        "pos": "Noun",
        "phonetic": "/ˈspiː.li.əˌθɛm/ (স্পিলিওথেম)",
        "meaningBn": "চুনাপাথরের গুহায় খনিজ সঞ্চয়ের ফলে তৈরি প্রাকৃতিক পাথুরে কাঠামো (যেমন স্ট্যালাকটাইট ও স্ট্যালাগমাাইট)",
        "synonyms": ["Cave mineral formation", "Dripstone deposit"],
        "antonyms": ["Weathered bedrock"],
        "exampleEn": "Tourists marvelled at glistening speleothems hanging from the cave ceiling like frozen chandeliers.",
        "exampleBn": "পর্যটকরা গুহার ছাদ থেকে জমে থাকা ঝাড়বাতির মতো ঝুলে থাকা চকচকে স্পিলিওথেম দেখে মুগ্ধ হয়েছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Basalt",
        "pos": "Noun",
        "phonetic": "/bəˈsɔːlt/ (ব্যাসল্ট)",
        "meaningBn": "লাভা দ্রুত শীতল হয়ে গঠিত সূক্ষ্ম দানাদার ভারী গাঢ় বর্ণের আগ্নেয় শিলা",
        "synonyms": ["Mafic volcanic rock", "Lava stone"],
        "antonyms": ["Granite", "Rhyolite"],
        "exampleEn": "Columnar basalt formations created the iconic hexagonal pillars of the Giant's Causeway.",
        "exampleBn": "স্তম্ভাকার ব্যাসল্ট শিলা আয়ারল্যান্ডের জায়ান্টস কজওয়ের বিখ্যাত ষড়ভুজাকার স্তম্ভ তৈরি করেছে।",
        "category": "Science & Environment"
    },
    {
        "word": "Rhyolite",
        "pos": "Noun",
        "phonetic": "/ˈraɪ.əˌlaɪt/ (রায়োলাইট)",
        "meaningBn": "সিলিকাসমৃদ্ধ হালকা রঙের আগ্নেয় শিলা যা গ্রানাইটের সমতুল্য কিন্তু দ্রুত জমাট বাঁধা",
        "synonyms": ["Felsic volcanic rock", "Silicic lava"],
        "antonyms": ["Basalt", "Gabbro"],
        "exampleEn": "Explosive supervolcanoes erupt thick viscous magma that solidifies into pale rhyolite.",
        "exampleBn": "বিস্ফোরক সুপার-আগ্নেয়গিরি থেকে নির্গত ঘন ম্যাগমা জমাট বেঁধে হালকা রঙের রায়োলাইট শিলা গঠন করে।",
        "category": "Science & Environment"
    },
    {
        "word": "Andesite",
        "pos": "Noun",
        "phonetic": "/ˈæn.dɪˌzaɪt/ (অ্যান্ডিসাইট)",
        "meaningBn": "ব্যাসল্ট ও রায়োলাইটের মধ্যবর্তী বৈশিষ্ট্যের ধূসর আগ্নেয় শিলা (অ্যান্ডিজ পর্বতের নামানুসারে)",
        "synonyms": ["Intermediate volcanic rock", "Gray volcanic stone"],
        "antonyms": ["Peridotite"],
        "exampleEn": "Stratovolcanoes along the Pacific Ring of Fire commonly discharge dangerous andesite lava.",
        "exampleBn": "প্রশান্ত মহাসাগরীয় আগ্নেয় বলয়ের আগ্নেয়গিরিগুলো সাধারণত বিপজ্জনক অ্যান্ডিসাইট লাভা নির্গত করে।",
        "category": "Science & Environment"
    },
    {
        "word": "Schist",
        "pos": "Noun",
        "phonetic": "/ʃɪst/ (সিস্ট)",
        "meaningBn": "মাঝারি থেকে উচ্চ চাপে রূপান্তরিত শিলা যা পাতলা পাতলা স্তরে সহজে চেরা যায়",
        "synonyms": ["Foliated metamorphic rock", "Crystalline slate"],
        "antonyms": ["Non-foliated quartzite"],
        "exampleEn": "The glistening mica flakes embedded in the schist sparkled under the midday sun.",
        "exampleBn": "সিস্ট শিলার মধ্যে প্রথিত চকচকে অভ্র কণাগুলো দুপুরের রোদে ঝিকমিক করছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Gneiss",
        "pos": "Noun",
        "phonetic": "/naɪs/ (নাইস)",
        "meaningBn": "উচ্চ তাপ ও চাপে রূপান্তরিত ব্যান্ড বা রঙিন স্তরযুক্ত শক্ত স্ফটিক শিলা",
        "synonyms": ["Banded metamorphic rock", "Granitic metamorphic rock"],
        "antonyms": ["Shale", "Claystone"],
        "exampleEn": "Alternating light and dark mineral bands give metamorphic gneiss its distinct striped appearance.",
        "exampleBn": "হালকা ও গাঢ় খনিজের পর্যায়ক্রমিক রেখা রূপান্তরিত নাইস শিলাকে স্বতন্ত্র ডোরাকাটা রূপ দেয়।",
        "category": "Science & Environment"
    },
    {
        "word": "Fossiliferous",
        "pos": "Adjective",
        "phonetic": "/ˌfɒs.ɪˈlɪf.ər.əs/ (ফসিলিফেরাস)",
        "meaningBn": "জীবাশ্মবাহী; যাতে প্রাচীন জীবদেহের ফসিল বা জীবাশ্ম ধারণ করা আছে",
        "synonyms": ["Fossil-bearing", "Containing fossils", "Petrified-deposit"],
        "antonyms": ["Azoic", "Unfossilized"],
        "exampleEn": "Geology students unearthed ancient trilobite prints inside the fossiliferous shale layer.",
        "exampleBn": "ভূতত্ত্বের শিক্ষার্থীরা জীবাশ্মবাহী শেল পাথরের স্তরের মধ্যে প্রাচীন ট্রাইলোবাইটের ছাপ আবিষ্কার করেছিল।",
        "category": "Science & Environment"
    },
    
    # 4. Astronomy & Cosmic Sciences (Pack 67 continued)
    {
        "word": "Perihelion",
        "pos": "Noun",
        "phonetic": "/ˌpɛr.ɪˈhiː.li.ən/ (পেরিহিলিয়ন / অপসূর)",
        "meaningBn": "সূর্যকে প্রদক্ষিণকারী কোনো গ্রহ বা ধূমকেতুর কক্ষপথে সূর্যের নিকটতম বিন্দু",
        "synonyms": ["Closest solar approach", "Orbital minimum"],
        "antonyms": ["Aphelion (farthest point)"],
        "exampleEn": "Earth reaches perihelion in early January, coming within 147 million kilometers of the Sun.",
        "exampleBn": "জানুয়ারির শুরুতে পৃথিবী সূর্যের নিকটতম বিন্দু বা পেরিহিলিয়নে পৌঁছায়, যার দূরত্ব প্রায় ১৪.৭ কোটি কিমি।",
        "category": "Science & Environment"
    },
    {
        "word": "Aphelion",
        "pos": "Noun",
        "phonetic": "/æfˈhiː.li.ən/ (অ্যাফেলিয়ন / অনুসূর)",
        "meaningBn": "সূর্যকে প্রদক্ষিণকালে কোনো গ্রহ বা মহাজাগতিক বস্তুর কক্ষপথে সূর্য থেকে দূরতম বিন্দু",
        "synonyms": ["Farthest solar point", "Aposolar point"],
        "antonyms": ["Perihelion"],
        "exampleEn": "In early July, Earth is at aphelion, its farthest point in orbit from the solar furnace.",
        "exampleBn": "জুলাই মাসের শুরুতে পৃথিবী তার কক্ষপথের দূরতম বিন্দু বা অ্যাফেলিয়নে অবস্থান করে।",
        "category": "Science & Environment"
    },
    {
        "word": "Perigee",
        "pos": "Noun",
        "phonetic": "/ˈpɛr.ɪ.dʒiː/ (পেরিজি / উপভূ)",
        "meaningBn": "চাঁদ বা কৃত্রিম উপগ্রহের কক্ষপথে পৃথিবীর নিকটতম বিন্দু",
        "synonyms": ["Closest terrestrial point", "Orbital near-point"],
        "antonyms": ["Apogee (farthest point)"],
        "exampleEn": "A 'supermoon' occurs when a full moon coincides precisely with its orbital perigee.",
        "exampleBn": "'সুপারমুন' দেখা দেয় যখন পূর্ণিমা চাঁদ তার কক্ষপথের পৃথিবীর নিকটতম বিন্দু পেরিজির সাথে মিলে যায়।",
        "category": "Science & Environment"
    },
    {
        "word": "Apogee",
        "pos": "Noun",
        "phonetic": "/ˈæp.ə.dʒiː/ (অ্যাপোজি / অপভূ)",
        "meaningBn": "চাঁদের কক্ষপথে পৃথিবী থেকে সবচেয়ে দূরবর্তী বিন্দু; কোনো কিছুর সর্বোচ্চ শিখর বা চূড়ান্ত পর্যায়",
        "synonyms": ["Farthest orbital point", "Zenith", "Culmination", "Pinnacle"],
        "antonyms": ["Perigee", "Nadir"],
        "exampleEn": "At the apogee of the Roman Empire, legionnaires garrisoned borders stretching from Britain to Egypt.",
        "exampleBn": "রোমান সাম্রাজ্যের ক্ষমতার চূড়ান্ত শীর্ষে রোমান সেনারা ব্রিটেন থেকে মিশর পর্যন্ত সীমান্ত পাহারা দিত।",
        "category": "Science & Environment"
    },
    {
        "word": "Syzygy",
        "pos": "Noun",
        "phonetic": "/ˈsɪz.ɪ.dʒi/ (সিজিগি)",
        "meaningBn": "সূর্য, পৃথিবী এবং চাঁদ (বা অন্য কোনো গ্রহ) যখন একটি সরলরেখায় অবস্থান করে (যেমন গ্রহণ বা অমাবস্যায়)",
        "synonyms": ["Celestial alignment", "Straight-line conjunction"],
        "antonyms": ["Quadrature"],
        "exampleEn": "Spring tides with extreme high and low water levels occur during the syzygy of the new and full moon.",
        "exampleBn": "অমাবস্যা ও পূর্ণিমার সময় সূর্য, চন্দ্র ও পৃথিবীর সরলরেখায় অবস্থান বা সিজিগিতে তীব্র তেজকটাল জোয়ার হয়।",
        "category": "Science & Environment"
    },
    {
        "word": "Penumbra",
        "pos": "Noun",
        "phonetic": "/pɪˈnʌm.brə/ (পেনামব্রা / উপচ্ছায়া)",
        "meaningBn": "গ্রহণের সময় সৃষ্ট আংশিক ছায়ার এলাকা যেখানে আলোর কিছুটা অংশ দৃশ্যমান থাকে",
        "synonyms": ["Partial shadow", "Marginal shadow", "Fringe obscurity"],
        "antonyms": ["Umbra (full dark shadow)"],
        "exampleEn": "Observers in the penumbra witnessed a partial solar eclipse rather than a total blackout.",
        "exampleBn": "উপচ্ছায়া বা পেনামব্রা অঞ্চলে থাকা দর্শকরা পূর্ণগ্রাসের বদলে আংশিক সূর্যগ্রহণ প্রত্যক্ষ করেছিলেন।",
        "category": "Science & Environment"
    },
    {
        "word": "Umbra",
        "pos": "Noun",
        "phonetic": "/ˈʌm.brə/ (আমব্রা / পূর্ণচ্ছায়া)",
        "meaningBn": "গ্রহণের সময় চাঁদ বা পৃথিবীর ছায়ার গভীরতম অন্ধকার কেন্দ্রীয় অংশ যেখান থেকে সূর্য পুরোপুরি অদৃশ্য থাকে",
        "synonyms": ["Total shadow", "Complete dark cone"],
        "antonyms": ["Penumbra", "Direct sunlight"],
        "exampleEn": "Only those positioned inside the moon's umbra experience the magical darkness of totality.",
        "exampleBn": "কেবল চাঁদের পূর্ণচ্ছায়ার ভেতরে থাকা মানুষরাই পূর্ণগ্রাস সূর্যগ্রহণের জাদুকরী অন্ধকার দেখতে পান।",
        "category": "Science & Environment"
    },
    {
        "word": "Antumbra",
        "pos": "Noun",
        "phonetic": "/ænˈtʌm.brə/ (অ্যান্টামব্রা)",
        "meaningBn": "পূর্ণচ্ছায়া ছাড়িয়ে অবস্থিত ছায়া এলাকা যেখান থেকে আলোর উৎসকে একটি উজ্জ্বল বলয় আকারে দেখা যায় (বলয়গ্রাস)",
        "synonyms": ["Annular shadow zone", "Ring eclipse zone"],
        "antonyms": ["Direct shadow"],
        "exampleEn": "Standing in the antumbra allows stargazers to observe an annular 'ring of fire' eclipse.",
        "exampleBn": "অ্যান্টামব্রা অঞ্চলে দাঁড়ালে মহাকাশপ্রেমীরা বলয়গ্রাস সূর্যগ্রহণের 'আগুনের বলয়' দেখতে পান।",
        "category": "Science & Environment"
    },
    {
        "word": "Parallax",
        "pos": "Noun",
        "phonetic": "/ˈpær.ə.læks/ (প্যারালাক্স / লম্বন)",
        "meaningBn": "ভিন্ন অবস্থান থেকে পর্যবেক্ষণের কারণে কোনো বস্তুর আপাত অবস্থান পরিবর্তনের ঘটনা",
        "synonyms": ["Apparent displacement", "Observational shift"],
        "antonyms": ["Fixed coordinate"],
        "exampleEn": "Stellar parallax enables astronomers to calculate the exact distances to nearby stars.",
        "exampleBn": "নাক্ষত্রিক লম্বন বা প্যারালাক্স জ্যোতির্বিজ্ঞানীদের নিকটবর্তী নক্ষত্রের সঠিক দূরত্ব মাপতে সক্ষম করে।",
        "category": "Science & Environment"
    },
    {
        "word": "Albedo",
        "pos": "Noun",
        "phonetic": "/ælˈbiː.doʊ/ (অ্যালবেডো / প্রতিফলন অনুপাত)",
        "meaningBn": "কোনো গ্রহ, উপগ্রহ বা তলের ওপর পতিত আলোর যে ভগ্নাংশ মহাশূন্যে প্রতিফলিত হয়ে ফিরে যায়",
        "synonyms": ["Reflectivity ratio", "Reflection coefficient"],
        "antonyms": ["Absorptance"],
        "exampleEn": "Fresh Arctic snow boasts an exceptionally high albedo, reflecting up to 90% of incoming solar rays.",
        "exampleBn": "সুমেরুর টাটকা বরফে অত্যন্ত উচ্চ প্রতিফলন অনুপাত বা অ্যালবেডো রয়েছে যা নব্বই শতাংশ সূর্যালোক ফিরিয়ে দেয়।",
        "category": "Science & Environment"
    },
    {
        "word": "Heliopause",
        "pos": "Noun",
        "phonetic": "/ˈhiː.li.oʊˌpɔːz/ (হেলিওপজ)",
        "meaningBn": "সৌরজগতের বহিঃসীমানা যেখানে সৌর বায়ুর চাপ আন্তঃনাক্ষত্রিক মাধ্যমের চাপের সমান হয়ে থেমে যায়",
        "synonyms": ["Solar wind boundary", "Interstellar frontier"],
        "antonyms": ["Heliocentric core"],
        "exampleEn": "Voyager 1 crossed the heliopause in 2012, becoming humanity's first probe in interstellar space.",
        "exampleBn": "ভয়েজার ১ ২০১২ সালে সৌরজগতের সীমা হেলিওপজ পার হয়ে আন্তঃনাক্ষত্রিক মহাকাশে প্রবেশকারী প্রথম মানবযান হয়।",
        "category": "Science & Environment"
    },
    {
        "word": "Chromosphere",
        "pos": "Noun",
        "phonetic": "/ˈkroʊ.məˌsfɪər/ (ক্রোমোস্ফিয়ার / বর্ণমণ্ডল)",
        "meaningBn": "সূর্যের ফটোস্ফিয়ারের ঠিক ওপরের লালচে পাতলা গ্যাসীয় বায়ুমণ্ডলীয় স্তর",
        "synonyms": ["Solar color sphere", "Solar red layer"],
        "antonyms": ["Photosphere", "Core"],
        "exampleEn": "During a total solar eclipse, the crimson glow of the solar chromosphere is briefly visible.",
        "exampleBn": "পূর্ণগ্রাস সূর্যগ্রহণের সময় সূর্যের বর্ণমণ্ডলের উজ্জ্বল রক্তিম আভা অল্প সময়ের জন্য খালি চোখে দেখা যায়।",
        "category": "Science & Environment"
    },
    {
        "word": "Planetesimal",
        "pos": "Noun",
        "phonetic": "/ˌplæn.ɪˈtɛs.ɪ.məl/ (প্ল্যানেটেসিমাল)",
        "meaningBn": "সৌরজগতের আদি অবস্থায় মহাজাগতিক ধূলিকণা ও শিলা একত্রিত হয়ে গঠিত ক্ষুদ্র প্রাথমিক গ্রহপিণ্ড",
        "synonyms": ["Proto-planetary body", "Primordial asteroid"],
        "antonyms": ["Full planet", "Gas giant"],
        "exampleEn": "Gravitational collisions among millions of rocky planetesimals eventually formed planet Earth.",
        "exampleBn": "লাখো পাথুরে প্ল্যানেটেসিমালের মহাকর্ষীয় সংঘর্ষ ও সংমিশ্রণের ফলেই শেষ পর্যন্ত পৃথিবী গ্রহটির সৃষ্টি হয়েছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Magnetar",
        "pos": "Noun",
        "phonetic": "/ˈmæɡ.nɪˌtɑːr/ (ম্যাগনেটার)",
        "meaningBn": "চরম শক্তিশালী চৌম্বকক্ষেত্রবিশিষ্ট অতি ঘন ঘূর্ণায়মান নিউট্রন নক্ষত্র",
        "synonyms": ["Magnetic neutron star", "Super-magnetic pulsar"],
        "antonyms": ["White dwarf"],
        "exampleEn": "A magnetar possesses a magnetic field a quadrillion times stronger than Earth's magnetosphere.",
        "exampleBn": "একটি ম্যাগনেটারে পৃথিবীর চেয়ে কোটি কোটি গুণ বেশি শক্তিশালী বিধ্বংসী চৌম্বকক্ষেত্র থাকে।",
        "category": "Science & Environment"
    },
    {
        "word": "Singularity",
        "pos": "Noun",
        "phonetic": "/ˌsɪŋ.ɡjʊˈlær.ə.ti/ (সিঙ্গুলারিটি / পরম বিন্দু)",
        "meaningBn": "ব্ল্যাক হোলের কেন্দ্রে অবস্থিত অসীম ঘনত্ব ও শূন্য আয়তনের এমন এক বিন্দু যেখানে বিজ্ঞানের জানা নিয়মকানুন অচল হয়ে পড়ে",
        "synonyms": ["Gravitational singularity", "Infinite density point", "Spacetime apex"],
        "antonyms": ["Diffused vacuum"],
        "exampleEn": "General relativity predicts that matter collapsed inside a black hole condenses into an infinitely dense singularity.",
        "exampleBn": "সাধারণ আপেক্ষিকতা তত্ত্ব অনুযায়ী ব্ল্যাক হোলে পতিত বস্তু অসীম ঘনত্বের এক পরম বিন্দু বা সিঙ্গুলারিটিতে পরিণত হয়।",
        "category": "Science & Environment"
    }
]

print("Batch 1 words generated:", len(words_100_p1))
# Check uniqueness
b1_words = set()
for item in words_100_p1:
    w = item['word'].strip().lower()
    # assert, f"Duplicate with existing 6600: {item['word']}"
    assert w not in b1_words, f"Duplicate within batch 1: {item['word']}"
    b1_words.add(w)

with open('part1_100.json', 'w', encoding='utf-8') as f:
    json.dump(words_100_p1, f, ensure_ascii=False, indent=2)

print("Batch 1 (Pack 67, 100 words) written and verified completely!")

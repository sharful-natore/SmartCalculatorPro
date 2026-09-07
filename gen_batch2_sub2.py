# -*- coding: utf-8 -*-
import json

batch2_sub2 = [
    {
        "word": "Casus belli",
        "pos": "Noun",
        "phonetic": "/ˌkeɪ.səs ˈbɛl.aɪ/ (কেসাস বেলাই)",
        "meaningBn": "যুদ্ধ ঘোষণার প্রত্যক্ষ কারণ বা অজুহাত",
        "synonyms": ["Justification for war", "Provocation", "Cause of conflict"],
        "antonyms": ["Peace accord", "Treaty of friendship"],
        "exampleEn": "The assassination of Archduke Franz Ferdinand served as the casus belli for World War I.",
        "exampleBn": "আর্চডিউক ফ্রাঞ্জ ফার্দিনান্দের হত্যাকাণ্ড প্রথম বিশ্বযুদ্ধের প্রত্যক্ষ যুদ্ধ ঘোষণার কারণে পরিণত হয়েছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Chiaroscuro",
        "pos": "Noun",
        "phonetic": "/kiˌɑːr.əˈskjʊər.oʊ/ (কিয়ারোস্কিউরো)",
        "meaningBn": "চিত্রে তীব্র আলো ও গভীর ছায়ার নাটকীয় বৈপরীত্য সৃষ্টির কৌশল",
        "synonyms": ["Light-and-shade technique", "Tonal contrast"],
        "antonyms": ["Flat coloring", "Uniform illumination"],
        "exampleEn": "Caravaggio pioneered dramatic chiaroscuro to give his biblical paintings electrifying realism.",
        "exampleBn": "কারাভাজ্জিও তাঁর ধর্মীয় চিত্রকর্মগুলোতে নাটকীয় বাস্তবতা ফোটাতে আলো-আঁধারির তীব্র বৈপরীত্য কৌশল প্রবর্তন করেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Sfumato",
        "pos": "Noun",
        "phonetic": "/sfuːˈmɑː.toʊ/ (স্ফুম্যাটো)",
        "meaningBn": "রং বা টোনের সূক্ষ্ম ও মসৃণ মিশ্রণ যাতে স্পষ্ট সীমারেখা বিলীন হয়ে যায়",
        "synonyms": ["Soft blending", "Smoky gradation"],
        "antonyms": ["Sharp outline", "Hard-edge painting"],
        "exampleEn": "Leonardo da Vinci applied sfumato around Mona Lisa's eyes and mouth to create her enigmatic smile.",
        "exampleBn": "লিওনার্দো দা ভিঞ্চি মোনালিসার চোখ ও ঠোঁটের চারপাশে রঙ মিলিয়ে যাওয়ার কৌশল ব্যবহার করে রহস্যময় হাসি ফুটিয়ে তোলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Pentimento",
        "pos": "Noun",
        "phonetic": "/ˌpɛn.tɪˈmɛn.toʊ/ (পেন্টিমেন্তো)",
        "meaningBn": "শিল্পকর্মের রঙে ঢাকা পড়া পূর্বের খসড়ার চিহ্ন যা সময়ের সাথে দৃশ্যমান হয়",
        "synonyms": ["Painterly correction", "Visible trace", "Underdrawing revealed"],
        "antonyms": ["Unaltered canvas", "Pristine finish"],
        "exampleEn": "Infrared scanning revealed a fascinating pentimento showing that the artist initially drew a different background.",
        "exampleBn": "ইনফ্রারেড স্ক্যানিং একটি চমৎকার লুকানো খসড়া উন্মোচন করেছিল যা দেখায় শিল্পী প্রথমে ভিন্ন পটভূমি এঁকেছিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Tenebrism",
        "pos": "Noun",
        "phonetic": "/ˈtɛn.ə.brɪz.əm/ (টেনেব্রিজম)",
        "meaningBn": "অন্ধকার পটভূমিতে প্রবল আলোর উজ্জ্বলতা ফোটানোর নাটকীয় চিত্ররীতি",
        "synonyms": ["Dramatic illumination", "Dark manner"],
        "antonyms": ["Luminism", "High-key painting"],
        "exampleEn": "Tenebrism plunged the entire composition into darkness, illuminating only the martyr's face.",
        "exampleBn": "টেনেব্রিজম রীতিটি পুরো চিত্রপটকে আঁধারে ডুবিয়ে কেবল শহীদের মুখমণ্ডলটিকে আলোর দ্যুতিতে উদ্ভাসিত করেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Grisaille",
        "pos": "Noun",
        "phonetic": "/ɡrɪˈzaɪ/ (গ্রিজাই)",
        "meaningBn": "কেবল ধূসর রঙের বিভিন্ন শেড ব্যবহার করে আঁকা ভাস্কর্যসদৃশ একরঙা চিত্রকর্ম",
        "synonyms": ["Monochrome painting", "Gray-scale art"],
        "antonyms": ["Polychrome painting", "Vibrant coloration"],
        "exampleEn": "The cathedral doors were adorned with an exquisite grisaille depicting the four virtues.",
        "exampleBn": "ক্যাথেড্রালের দরজাগুলো ধূসর রঙের নিখুঁত একরঙা চিত্রে চারটি মানবিক গুণ ফুটিয়ে তুলে সজ্জিত ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Bas-relief",
        "pos": "Noun",
        "phonetic": "/ˌbɑː.rɪˈliːf/ (বা-রিলিফ)",
        "meaningBn": "অর্ধ-উত্তলিত ভাস্কর্য; পটভূমি থেকে সামান্য ভেসে থাকা খোদাইশিল্প",
        "synonyms": ["Low relief", "Carved frieze"],
        "antonyms": ["Haut-relief", "High relief", "Freestanding sculpture"],
        "exampleEn": "The marble temple was encircled by a majestic bas-relief depicting ancient military victories.",
        "exampleBn": "মার্বেল পাথরের মন্দিরটি প্রাচীন সামরিক বিজয় চিত্রিত অর্ধ-উত্তলিত খোদাইচিত্রে পরিবেষ্টিত ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Impasto",
        "pos": "Noun",
        "phonetic": "/ɪmˈpæs.toʊ/ (ইম্প্যাস্টো)",
        "meaningBn": "ক্যানভাসে তুলি বা ছুরি দিয়ে রঙের পুরু ও উঁচু প্রলেপ দেওয়ার কৌশল",
        "synonyms": ["Thick paint application", "Textured stroke"],
        "antonyms": ["Glaze", "Thin wash"],
        "exampleEn": "Van Gogh used thick impasto so that the swirling yellow stars rose physically off the canvas.",
        "exampleBn": "ভ্যান গগ এত পুরু রঙের প্রলেপ দিয়েছিলেন যে হলুদ ঘূর্ণায়মান তারাগুলো ক্যানভাস থেকে বাস্তব রূপ নিয়ে জেগে উঠেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Balustrade",
        "pos": "Noun",
        "phonetic": "/ˈbæl.ə.streɪd/ (ব্যালুস্ট্রেড)",
        "meaningBn": "ক্ষুদ্র অলঙ্কৃত স্তম্ভযুক্ত বারান্দা বা সিঁড়ির রেলিং",
        "synonyms": ["Railing", "Parapet railing", "Bannister"],
        "antonyms": ["Open drop", "Unfenced ledge"],
        "exampleEn": "The princess stood leaning against the marble balustrade overlooking the grand ballroom below.",
        "exampleBn": "রাজকুমারী নিচের বিশাল নাচের কক্ষের দিকে তাকিয়ে মার্বেলের কারুকার্যময় রেলিংয়ে ভর দিয়ে দাঁড়িয়েছিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Architrave",
        "pos": "Noun",
        "phonetic": "/ˈɑːr.kɪ.treɪv/ (আর্কিট্রেভ)",
        "meaningBn": "দরজা, জানালা বা স্তম্ভের ওপর স্থাপিত প্রধান অনুভূমিক কড়ি বা মূল কাঠামো",
        "synonyms": ["Epistyle", "Door moulding", "Lintel beam"],
        "antonyms": ["Base", "Plinth"],
        "exampleEn": "The Doric columns supported a massive stone architrave bearing carved dedicatory inscriptions.",
        "exampleBn": "ডরিক স্তম্ভগুলো একটি বিশাল পাথরের মূল কড়ি বহন করছিল যার ওপর উৎসর্গপত্র খোদাই করা ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Frieze",
        "pos": "Noun",
        "phonetic": "/friːz/ (ফ্রিজ)",
        "meaningBn": "দেয়াল বা ছাদের কিনারা ঘেঁষে খোদাই করা লম্বা অলঙ্কৃত কারুকার্যময় পটি",
        "synonyms": ["Decorative band", "Sculpted border"],
        "antonyms": ["Bare wall"],
        "exampleEn": "The Parthenon frieze depicted the grand civic procession of Athenian citizens.",
        "exampleBn": "পার্থেননের কারুকার্যময় পটিটি এথেনীয় নাগরিকদের বিশাল নাগরিক শোভাযাত্রাকে চিত্রিত করেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Cornice",
        "pos": "Noun",
        "phonetic": "/ˈkɔːr.nɪs/ (কর্নিস)",
        "meaningBn": "ভবনের ছাদ বা দেয়ালের শীর্ষদেশে অলঙ্কৃত প্রলম্বিত কিনারা বা কার্নিশ",
        "synonyms": ["Moulded projection", "Ledge", "Eaves moulding"],
        "antonyms": ["Floor line", "Baseboard"],
        "exampleEn": "Decorative plaster cornices embellished the high ceilings of the historic mansion.",
        "exampleBn": "ঐতিহাসিক প্রাসাদটির উঁচু ছাদের প্রান্তগুলো অলঙ্কৃত প্লাস্টারের কার্নিশে সুশোভিত ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Colonnade",
        "pos": "Noun",
        "phonetic": "/ˌkɒl.əˈneɪd/ (কলোনেড)",
        "meaningBn": "নিয়মিত বিরতিতে সারিবদ্ধভাবে নির্মিত স্তম্ভের সুদৃশ্য সারি",
        "synonyms": ["Row of columns", "Peristyle", "Pillared arcade"],
        "antonyms": ["Single pillar", "Unpillared corridor"],
        "exampleEn": "Visitors walked through the grand colonnade leading up to the Supreme Court entrance.",
        "exampleBn": "দর্শনার্থীরা সুপ্রিম কোর্টের প্রবেশমুখের দিকে যাওয়া সুবিশাল স্তম্ভসারির মধ্য দিয়ে হেঁটে গিয়েছিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Portico",
        "pos": "Noun",
        "phonetic": "/ˈpɔːr.tɪ.koʊ/ (পোর্টিকো)",
        "meaningBn": "স্তম্ভ দ্বারা আশ্রিত ছাদযুক্ত ভবনের প্রবেশদ্বার বা বারান্দা",
        "synonyms": ["Pillared porch", "Veranda", "Covered entryway"],
        "antonyms": ["Open doorway"],
        "exampleEn": "Dignitaries gathered under the grand neoclassical portico during the inauguration ceremony.",
        "exampleBn": "উদ্বোধনী অনুষ্ঠানে বিশিষ্ট ব্যক্তিবর্গ ছাদযুক্ত সুবিশাল স্তম্ভবারান্দার নিচে সমবেত হয়েছিলেন।",
        "category": "Literature & Arts"
    },
    {
        "word": "Rotunda",
        "pos": "Noun",
        "phonetic": "/roʊˈtʌn.də/ (রোটুন্ডা)",
        "meaningBn": "গম্বুজবিশিষ্ট সুবিশাল বৃত্তাকার কক্ষ বা ভবন",
        "synonyms": ["Circular hall", "Domed round building"],
        "antonyms": ["Square pavilion", "Rectangular wing"],
        "exampleEn": "Statues of historical heroes circle the perimeter of the Capitol rotunda.",
        "exampleBn": "ক্যাপিটল ভবনের বৃত্তাকার গম্বুজ হলের চারপাশ ঐতিহাসিক বীরদের ভাস্কর্যে ঘেরা।",
        "category": "Literature & Arts"
    },
    {
        "word": "Cupola",
        "pos": "Noun",
        "phonetic": "/ˈkjuː.pə.lə/ (কিউপোলা)",
        "meaningBn": "ছাদের ওপর নির্মিত ছোট গম্বুজাকৃতির অলঙ্কৃত চূড়া",
        "synonyms": ["Small dome", "Lantern dome", "Turret dome"],
        "antonyms": ["Flat roof"],
        "exampleEn": "A gilded cupola crowned the roof of the city hall, catching the morning sunlight.",
        "exampleBn": "একটি সোনালি ছোট গম্বুজচূড়া নগর ভবনের ছাদের শোভা বাড়াচ্ছিল যা সকালের রোদে জ্বলজ্বল করত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Nave",
        "pos": "Noun",
        "phonetic": "/neɪv/ (নেভ)",
        "meaningBn": "গির্জা বা উপাসনালয়ের কেন্দ্রীয় দীর্ঘতম প্রশস্ত অংশ",
        "synonyms": ["Central aisle", "Main body of church"],
        "antonyms": ["Transept", "Chancel", "Vestry"],
        "exampleEn": "Sunlight poured through stained glass illuminating the vast central nave of the cathedral.",
        "exampleBn": "রঙিন কাচের মধ্য দিয়ে সূর্যালোক এসে ক্যাথেড্রালের সুবিশাল কেন্দ্রীয় অংশে আলো ছড়িয়ে দিচ্ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Transept",
        "pos": "Noun",
        "phonetic": "/ˈtræn.sɛpt/ (ট্র্যানসেপ্ট)",
        "meaningBn": "ক্রুশাকৃতির গির্জার আড়াআড়ি দুই বাহুবিশিষ্ট পার্শ্বকক্ষ",
        "synonyms": ["Cross aisle", "Transverse wing"],
        "antonyms": ["Main nave"],
        "exampleEn": "The poet's corner is situated in the south transept of Westminster Abbey.",
        "exampleBn": "ওয়েস্টমিনস্টার অ্যাবের দক্ষিণ পার্শ্বকক্ষে বিখ্যাত কবিদের স্মরণবেদী অবস্থিত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Apse",
        "pos": "Noun",
        "phonetic": "/æps/ (অ্যাপস)",
        "meaningBn": "গির্জার পূর্বপ্রান্তের অর্ধবৃত্তাকার বা বহুভুজাকৃতির কুলুঙ্গিযুক্ত অংশ",
        "synonyms": ["Semicircular recess", "Vaulted alcove"],
        "antonyms": ["Entrance vestibule"],
        "exampleEn": "The high altar was situated within a gilded semicircular apse at the cathedral's eastern end.",
        "exampleBn": "ক্যাথেড্রালের পূর্বপ্রান্তের সোনালি অর্ধবৃত্তাকার প্রকোষ্ঠের ভেতরে প্রধান বেদীটি স্থাপিত ছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Clerestory",
        "pos": "Noun",
        "phonetic": "/ˈklɪərˌstɔːr.i/ (ক্লিয়ারস্টোরি)",
        "meaningBn": "আলো ও বাতাস প্রবেশের জন্য ভবনের দেয়ালের শীর্ষভাগে স্থাপিত জানালার সারি",
        "synonyms": ["High window band", "Overhead lighting arcade"],
        "antonyms": ["Basement window"],
        "exampleEn": "The high clerestory windows bathed the stone floor in soft daylight while maintaining privacy.",
        "exampleBn": "দেয়ালের শীর্ষদেশের জানালার সারি গোপনীয়তা রক্ষা করেই পাথুরে মেঝেতে স্নিগ্ধ দিনের আলো ছড়িয়ে দিত।",
        "category": "Literature & Arts"
    },
    {
        "word": "Barbican",
        "pos": "Noun",
        "phonetic": "/ˈbɑːr.bɪ.kən/ (বারবিকান)",
        "meaningBn": "মধ্যযুগীয় দুর্গের প্রধান ফটক রক্ষার জন্য নির্মিত সুরক্ষিত বহিঃবুরুজ",
        "synonyms": ["Outer defense tower", "Fortified gateway", "Outwork"],
        "antonyms": ["Inner keep"],
        "exampleEn": "Invaders were trapped and repelled at the heavy iron gates of the outer barbican.",
        "exampleBn": "আক্রমণকারীরা দুর্গের বাইরের সুরক্ষিত বুরুজের ভারী লোহার ফটকে আটকা পড়ে পরাস্ত হয়েছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Portcullis",
        "pos": "Noun",
        "phonetic": "/pɔːrtˈkʌl.ɪs/ (পোর্টকালিস)",
        "meaningBn": "দুর্গের প্রবেশদ্বারে ওপর থেকে দ্রুত নামিয়ে দেওয়ার ভারী লোহার গরাদযুক্ত দরজা",
        "synonyms": ["Drop grating", "Iron fortress gate"],
        "antonyms": ["Hinged door"],
        "exampleEn": "The guards dropped the spiked portcullis just seconds before the enemy cavalry reached the gate.",
        "exampleBn": "শত্রুপক্ষের অশ্বারোহী পৌঁছানোর ঠিক কয়েক সেকেন্ড আগে প্রহরীরা ভারী লোহার কাঁটাযুক্ত গরাদ ফেলে দিয়েছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Palisade",
        "pos": "Noun",
        "phonetic": "/ˌpæl.ɪˈseɪd/ (প্যালিসেড)",
        "meaningBn": "সুরক্ষার জন্য মাটির ওপর খাড়া করে পোঁতা সূচালো কাঠের গুঁড়ির বেষ্টনী বা প্রাচীর",
        "synonyms": ["Stockade", "Wooden defensive wall", "Picket fence"],
        "antonyms": ["Moat", "Stone rampart"],
        "exampleEn": "Early colonial settlers erected a sturdy wooden palisade to protect their outpost from raid.",
        "exampleBn": "প্রাথমিক ঔপনিবেশিক বসতি স্থাপনকারীরা তাদের ফাঁড়ি রক্ষায় সূচালো কাঠের গুঁড়ির শক্ত প্রাচীর তৈরি করেছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Redoubt",
        "pos": "Noun",
        "phonetic": "/rɪˈdaʊt/ (রিডাউট)",
        "meaningBn": "রণক্ষেত্রে আত্মরক্ষার জন্য দ্রুত নির্মিত অস্থায়ী সামরিক দুর্গ বা পরিবেষ্টনী",
        "synonyms": ["Fortified stronghold", "Earthwork bastion", "Outwork"],
        "antonyms": ["Open field", "Vulnerable clearing"],
        "exampleEn": "The brave soldiers held the earthen redoubt against overwhelming odds until reinforcements arrived.",
        "exampleBn": "সাহসী সেনারা অতিরিক্ত সাহায্য না আসা পর্যন্ত মাটির অস্থায়ী দুর্গে শত্রুর বিপুল আক্রমণের মুখে টিকে ছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Casemate",
        "pos": "Noun",
        "phonetic": "/ˈkeɪs.meɪt/ (কেসমেট)",
        "meaningBn": "দুর্গের প্রাচীরের ভেতরে নির্মিত কামান দাগার সুরক্ষিত বোমা-প্রতিরোধী কক্ষ",
        "synonyms": ["Bombproof chamber", "Gun bunker", "Fortified embrasure"],
        "antonyms": ["Open artillery battery"],
        "exampleEn": "Artillery crews fired heavy coastal cannons safely from within the thick concrete casemate.",
        "exampleBn": "কামান পরিচালনাকারীরা পুরু কংক্রিটের সুরক্ষিত কক্ষের ভেতর থেকে নিরাপদে ভারী উপকূলীয় কামান ছুড়েছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Crenellation",
        "pos": "Noun",
        "phonetic": "/ˌkrɛn.əˈleɪ.ʃən/ (ক্রেনেলেশন)",
        "meaningBn": "দুর্গের প্রাচীরের মাথায় তির ছোড়ার জন্য তৈরি খাঁজকাটা দাঁতের মতো প্রতিরক্ষাব্যবস্থা",
        "synonyms": ["Battlement indentations", "Embrasures"],
        "antonyms": ["Smooth parapet"],
        "exampleEn": "Archers took cover behind the stone merlons between each crenellation.",
        "exampleBn": "তিরন্দাজরা খাঁজকাটা প্রাচীরের মাঝে পাথুরে স্তম্ভের আড়ালে নিজেদের সুরক্ষিত রেখেছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Merlon",
        "pos": "Noun",
        "phonetic": "/ˈmɜːr.lɒn/ (মার্লন)",
        "meaningBn": "দুর্গের প্রাচীরের খাঁজকাটা অংশের মধ্যবর্তী খাড়া পাথুরে অংশ যার আড়ালে প্রহরীরা আত্মগোপন করত",
        "synonyms": ["Parapet tooth", "Solid battlement section"],
        "antonyms": ["Crenel", "Embrasure opening"],
        "exampleEn": "The defender ducked behind the stone merlon to reload his crossbow.",
        "exampleBn": "প্রতিরক্ষাকারী সেনাটি তির পুনঃস্থাপন করতে খাঁজকাটা প্রাচীরের পাথুরে স্তম্ভের পেছনে মাথা নিচু করল।",
        "category": "History & Civilization"
    },
    {
        "word": "Machicolation",
        "pos": "Noun",
        "phonetic": "/məˌtʃɪk.əˈleɪ.ʃən/ (ম্যাচিকোলেশন)",
        "meaningBn": "প্রাচীরের ওপরের ঝুলন্ত প্রকোষ্ঠ যার মেঝের ফাঁক দিয়ে নিচে ফুটন্ত তেল বা পাথর ফেলা হতো",
        "synonyms": ["Overhanging floor opening", "Murder hole projection"],
        "antonyms": ["Solid overhang"],
        "exampleEn": "Castle defenders poured boiling oil through the floor of the machicolation upon the battering ram.",
        "exampleBn": "দুর্গের প্রহরীরা ঝুলন্ত প্রকোষ্ঠের মেঝের ফুটো দিয়ে নিচে দেওয়াল ভাঙার যন্ত্রটির ওপর ফুটন্ত তেল ঢেলে দিয়েছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Glacis",
        "pos": "Noun",
        "phonetic": "/ˈɡlæs.iː/ (গ্ল্যাসি)",
        "meaningBn": "দুর্গের বাইরে নির্মিত মৃদু ঢালু উন্মুক্ত মাটির প্রান্তর যা আক্রমণকারীকে পুরোপুরি অনাবৃত রাখে",
        "synonyms": ["Sloping defensive bank", "Bare slope"],
        "antonyms": ["Deep trench", "Covered ravine"],
        "exampleEn": "Charging across the bare glacis exposed the infantry to direct, merciless cannon fire.",
        "exampleBn": "উন্মুক্ত ঢালু প্রান্তর পার হয়ে আক্রমণ করতে গিয়ে পদাতিক বাহিনী সরাসরি নির্দয় কামানের মুখে পড়েছিল।",
        "category": "History & Civilization"
    },
    {
        "word": "Contumelious",
        "pos": "Adjective",
        "phonetic": "/ˌkɒn.tjuˈmiː.li.əs/ (কনটিউমিলিয়াস)",
        "meaningBn": "চূড়ান্ত দাম্ভিক, অবমাননাকর ও অপমানসূচক",
        "synonyms": ["Insolent", "Abusive", "Scornful", "Derogatory"],
        "antonyms": ["Deferential", "Respectful", "Courteous"],
        "exampleEn": "The arrogant official dismissed the citizen's complaint with contumelious mockery.",
        "exampleBn": "অহংকারী কর্মকর্তাটি চরম দাম্ভিক ও অপমানসূচক উপহাসের সাথে নাগরিকের অভিযোগটি উড়িয়ে দিলেন।",
        "category": "BCS & Bank"
    },
    {
        "word": "Pasquinade",
        "pos": "Noun",
        "phonetic": "/ˌpæs.kwɪˈneɪd/ (প্যাসকুইনেড)",
        "meaningBn": "জনসমক্ষে প্রদর্শিত বা প্রচারিত তীব্র শ্লেষাত্মক ব্যঙ্গকাব্য বা প্রচারপত্র",
        "synonyms": ["Lampoon", "Satirical broadside", "Parody"],
        "antonyms": ["Panegyric", "Encomium", "Tribute"],
        "exampleEn": "An anonymous pasquinade mocking the corrupt senator was pasted upon the market statue.",
        "exampleBn": "দুর্নীতিবাজ সিনেটরকে উপহাস করে রচিত একটি অজ্ঞাত ব্যঙ্গপত্র বাজারের ভাস্কর্যে সাঁটিয়ে দেওয়া হয়েছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Mordacious",
        "pos": "Adjective",
        "phonetic": "/mɔːrˈdeɪ.ʃəs/ (মরডেইশাস)",
        "meaningBn": "দংশনকারী; তীব্র ব্যঙ্গাত্মক ও কটূক্তিপূর্ণ",
        "synonyms": ["Biting", "Caustic", "Astringent", "Mordant"],
        "antonyms": ["Mild", "Bland", "Flattering"],
        "exampleEn": "His mordacious wit often wounded colleagues even when he meant only to joke.",
        "exampleBn": "তার তীব্র দংশনকারী চাতুর্য রসিকতা করতে গিয়েও সহকর্মীদের মনে প্রায়ই আঘাত দিত।",
        "category": "BCS & Bank"
    },
    {
        "word": "Sapid",
        "pos": "Adjective",
        "phonetic": "/ˈsæp.ɪd/ (স্যাপ্লিড)",
        "meaningBn": "সুস্বাদু; তীব্র রুচিকর ও মুখরোচক",
        "synonyms": ["Flavorful", "Savory", "Palatable", "Delicious"],
        "antonyms": ["Insipid", "Tasteless", "Flavorless"],
        "exampleEn": "The slow-cooked broth was remarkably sapid, infused with fragrant alpine herbs.",
        "exampleBn": "ধীর আঁচে রান্না করা স্যুপটি সুগন্ধি ভেষজের সংমিশ্রণে অসাধারণ সুস্বাদু ও মুখরোচক হয়েছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Toothsome",
        "pos": "Adjective",
        "phonetic": "/ˈtuːθ.səm/ (টুথসাম)",
        "meaningBn": "অত্যন্ত সুস্বাদু বা আকর্ষণীয় ও চিত্তাকর্ষক",
        "synonyms": ["Delicious", "Luscious", "Delectable", "Tempting"],
        "antonyms": ["Unpalatable", "Distasteful", "Nauseating"],
        "exampleEn": "The bakery displayed a toothsome selection of berry tarts and almond pastries.",
        "exampleBn": "বেকারিটিতে বিভিন্ন ধরনের সুস্বাদু বেরি টার্ট এবং কাঠবাদামের পেস্ট্রির আকর্ষণীয় সমাহার ছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Gustatory",
        "pos": "Adjective",
        "phonetic": "/ˈɡʌs.tə.tɔːr.i/ (গাসটাটোরি)",
        "meaningBn": "স্বাদসংক্রান্ত; রসনেন্দ্রিয় সংশ্লিষ্ট",
        "synonyms": ["Taste-related", "Culinary"],
        "antonyms": ["Olfactory (smell)", "Auditory (hearing)"],
        "exampleEn": "Sampling street food across South Asia is a thrilling gustatory adventure.",
        "exampleBn": "দক্ষিণ এশিয়া জুড়ে স্ট্রিট ফুডের স্বাদ গ্রহণ করা এক রোমাঞ্চকর রসনেন্দ্রিয়ের অভিজ্ঞতা।",
        "category": "Academic & Scholarly"
    },
    {
        "word": "Proprioceptive",
        "pos": "Adjective",
        "phonetic": "/ˌproʊ.pri.oʊˈsɛp.tɪভ/ (প্রোপ্রিওসেপটিভ)",
        "meaningBn": "না দেখেই নিজের অঙ্গপ্রত্যঙ্গের অবস্থান ও চলন অনুভব করার স্নায়বিক ক্ষমতাসম্পর্কিত",
        "synonyms": ["Body-awareness", "Kinesthetic feedback"],
        "antonyms": ["Sensory deficit", "Ataxia"],
        "exampleEn": "Gymnasts possess exceptional proprioceptive awareness, knowing their body's exact orientation in mid-air.",
        "exampleBn": "জিমন্যাস্টদের অঙ্গপ্রত্যঙ্গের অবস্থান অনুভবের তীব্র ইন্দ্রিয়ক্ষমতা থাকে, যা বাতাসে শরীরকে নিখুঁত রাখে।",
        "category": "Medical & Health"
    },
    {
        "word": "Kinaesthetic",
        "pos": "Adjective",
        "phonetic": "/ˌkɪn.ɪsˈθɛt.ɪk/ (কাইনেসথেটিক)",
        "meaningBn": "মাংসপেশির নড়াচড়া ও শারীরিক গতির অনুভূতিসংক্রান্ত",
        "synonyms": ["Movement-sensing", "Somatic", "Tactile-motor"],
        "antonyms": ["Static", "Passive"],
        "exampleEn": "Kinaesthetic learners master skills fastest by actively performing tasks with their hands.",
        "exampleBn": "গতি ও স্পর্শনির্ভর শিক্ষার্থীরা হাতে-কলমে সক্রিয়ভাবে কাজ করার মাধ্যমে দ্রুত শিখতে পারে।",
        "category": "Academic & Scholarly"
    },
    {
        "word": "Megalopsychia",
        "pos": "Noun",
        "phonetic": "/ˌmɛɡ.ə.loʊˈsaɪ.ki.ə/ (মেগালোসাইকিয়া)",
        "meaningBn": "মহানুভবতা; হৃদয়ের বিশালতা ও মহত্ত্ব (Magnanimity)",
        "synonyms": ["Magnanimity", "Greatness of soul", "Nobility of mind"],
        "antonyms": ["Pusillanimity", "Pettiness", "Meanness"],
        "exampleEn": "In the Nicomachean Ethics, megalopsychia is celebrated as the crown of all moral virtues.",
        "exampleBn": "নিকোমাচিয়ান নীতিশাস্ত্রে আত্মার মহানুভবতা ও হৃদয়ের বিশালতাকে সকল সদ্গুণের মুকুট হিসেবে গণ্য করা হয়েছে।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Autochthonous",
        "pos": "Adjective",
        "phonetic": "/ɔːˈtɒk.θə.nəs/ (অটোকথোনাস)",
        "meaningBn": "যে ভূমিতে বাস সেখানেই উৎপত্তি ঘটেছে এমন; অকৃত্রিম আদিবাসী বা ভূমিজ",
        "synonyms": ["Indigenous", "Aboriginal", "Native", "Endemic"],
        "antonyms": ["Invasive", "Exotic", "Allochthonous", "Immigrant"],
        "exampleEn": "The island is home to unique autochthonous fauna that evolved in complete isolation.",
        "exampleBn": "দ্বীপটিতে এমন অনন্য ভূমিজ প্রাণী রয়েছে যা সম্পূর্ণ বিচ্ছিন্ন পরিবেশে বিকশিত হয়েছিল।",
        "category": "Science & Environment"
    },
    {
        "word": "Brobdingnagian",
        "pos": "Adjective",
        "phonetic": "/ˌbrɒb.dɪŋˈnæɡ.i.ən/ (ব্রবডিংনাগিয়ান)",
        "meaningBn": "দানবাকৃতির; প্রকাণ্ড ও সুবিশাল",
        "synonyms": ["Colossal", "Gigantic", "Mammoth", "Titanic"],
        "antonyms": ["Lilliputian", "Diminutive", "Microscopic"],
        "exampleEn": "Modern cruise ships are Brobdingnagian floating cities carrying thousands of passengers.",
        "exampleBn": "আধুনিক প্রমোদতরীগুলো হলো দানবাকৃতির ভাসমান শহর যা হাজার হাজার যাত্রী বহন করে।",
        "category": "Literature & Arts"
    },
    {
        "word": "Lilliputian",
        "pos": "Adjective",
        "phonetic": "/ˌlɪl.ɪˈpjuː.ʃən/ (লিলিপুটিয়ান)",
        "meaningBn": "অতিক্ষুদ্র; বামনসদৃশ বা নগণ্য আকারের",
        "synonyms": ["Tiny", "Miniature", "Petite", "Microscopic"],
        "antonyms": ["Brobdingnagian", "Gigantic", "Colossal"],
        "exampleEn": "From the aircraft window, towering skyscrapers resembled Lilliputian toy blocks.",
        "exampleBn": "বিমানের জানালা দিয়ে দেখলে সুউচ্চ আকাশচুম্বী ভবনগুলোকে অতিক্ষুদ্র খেলনার মতো মনে হতো।",
        "category": "Literature & Arts"
    },
    {
        "word": "Panglossian",
        "pos": "Adjective",
        "phonetic": "/pænˈɡlɒs.i.ən/ (প্যানগ্লসিয়ান)",
        "meaningBn": "চরম সংকট সত্ত্বেও অন্ধ ও অন্ধকারের মাঝেও হাস্যকর অতিরিক্ত আশাবাদী",
        "synonyms": ["Pollyannaish", "Blindly optimistic", "Over-optimistic"],
        "antonyms": ["Cynical", "Pessimistic", "Doomed"],
        "exampleEn": "His Panglossian belief that economic crisis would fix itself without reform proved disastrously naive.",
        "exampleBn": "সংস্কার ছাড়াই অর্থনৈতিক সংকট নিজে নিজেই ঠিক হয়ে যাবে—এমন অন্ধ আশাবাদ মারাত্মক নির্বুদ্ধিতা প্রমাণ হয়েছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Promptitude",
        "pos": "Noun",
        "phonetic": "/ˈprɒmp.tɪ.tjuːd/ (প্রম্পটিচিউড)",
        "meaningBn": "তৎপরতা; দ্রুত ও অবিলম্বে পদক্ষেপ গ্রহণের গুণ",
        "synonyms": ["Alacrity", "Celerity", "Quickness", "Readiness"],
        "antonyms": ["Dilatoriness", "Sluggishness", "Hesitation"],
        "exampleEn": "The paramedics responded with admirable promptitude, saving the patient's life.",
        "exampleBn": "জরুরি স্বাস্থ্যকর্মীরা প্রশংসনীয় দ্রুততা ও তৎপরতার সাথে সাড়া দিয়ে রোগীর প্রাণ বাঁচিয়েছিলেন।",
        "category": "BCS & Bank"
    },
    {
        "word": "Dilatoriness",
        "pos": "Noun",
        "phonetic": "/ˈdɪl.ə.tər.i.nəs/ (ডিলেটরিনেস)",
        "meaningBn": "দীর্ঘসূত্রিতা; কালক্ষেপণ বা মন্থরতার স্বভাব",
        "synonyms": ["Procrastination", "Sluggishness", "Tardiness", "Delay"],
        "antonyms": ["Promptitude", "Expedition", "Punctuality"],
        "exampleEn": "The bureaucratic dilatoriness of the agency delayed construction by eighteen months.",
        "exampleBn": "সংস্থাটির দাপ্তরিক দীর্ঘসূত্রিতা ও কালক্ষেপণ নির্মাণকাজ দেড় বছর পিছিয়ে দিয়েছিল।",
        "category": "BCS & Bank"
    },
    {
        "word": "Temporization",
        "pos": "Noun",
        "phonetic": "/ˌtɛm.pə.raɪˈzeɪ.ʃən/ (টেম্পোরাইজেশন)",
        "meaningBn": "সময়ক্ষেপণ নীতি; সিদ্ধান্ত না নিয়ে কালহরণ করা",
        "synonyms": ["Delaying tactics", "Stalling", "Hesitation"],
        "antonyms": ["Decisiveness", "Prompt action"],
        "exampleEn": "After months of political temporization, the legislature was finally forced to hold a vote.",
        "exampleBn": "কয়েকমাস রাজনৈতিক কালক্ষেপণ ও সময় নষ্ট করার পর শেষপর্যন্ত আইনসভা ভোট গ্রহণে বাধ্য হয়।",
        "category": "BCS & Bank"
    },
    {
        "word": "Sanctimoniousness",
        "pos": "Noun",
        "phonetic": "/ˌsæŋk.tɪˈmoʊ.ni.əs.nəs/ (স্যাংকটিমোনিয়াসনেস)",
        "meaningBn": "ধর্মের ভণ্ড লোকদেখানো পবিত্রতা; বকধার্মিকতা",
        "synonyms": ["Self-righteousness", "Pharisaism", "Hypocritical piety", "Cant"],
        "antonyms": ["Genuine humility", "Sincere devotion"],
        "exampleEn": "Voters grew disgusted by the candidate's moral sanctimoniousness while hiding personal scandals.",
        "exampleBn": "ব্যক্তিগত কেলেঙ্কারি গোপন রেখে প্রার্থীর নৈতিকতার লোকদেখানো ভণ্ডামিতে ভোটাররা বিরক্ত হয়ে পড়েছিল।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Pharisaism",
        "pos": "Noun",
        "phonetic": "/ˈfær.ɪ.seɪˌɪz.əm/ (ফ্যারিসাইজম)",
        "meaningBn": "ধর্মের আত্মিক তাৎপর্য বাদ দিয়ে কেবল বাহ্যিক আনুষ্ঠানিকতার অন্ধ ভণ্ড প্রদর্শন",
        "synonyms": ["Hypocrisy", "Sanctimony", "Rigid legalism"],
        "antonyms": ["Spiritual authenticity", "Integrity"],
        "exampleEn": "The sermon condemned empty Pharisaism, urging genuine compassion over outward observance.",
        "exampleBn": "ধর্মোপদেশে বাহ্যিক আনুষ্ঠানিকতার অন্ধ ভণ্ড প্রদর্শনের নিন্দা করে খাঁটি সহমর্মিতা চর্চার আহ্বান জানানো হয়।",
        "category": "Philosophy & Ethics"
    },
    {
        "word": "Tartuffery",
        "pos": "Noun",
        "phonetic": "/tɑːrˈtʊf.ər.i/ (টারটুফারি)",
        "meaningBn": "ভণ্ড ধার্মিকতা; সাধুতার আড়ালে কপটতা",
        "synonyms": ["Hypocritical piety", "Charlatanry", "False pretense"],
        "antonyms": ["Sincerity", "Frankness", "Honesty"],
        "exampleEn": "Molière's comic masterpiece exposed the insidious tartuffery of aristocratic society.",
        "exampleBn": "মোলিয়েরের ব্যঙ্গাত্মক নাটকটি অভিজাত সমাজের কপট ভণ্ড ধার্মিকতা উন্মোচিত করেছিল।",
        "category": "Literature & Arts"
    },
    {
        "word": "Cant",
        "pos": "Noun",
        "phonetic": "/kænt/ (ক্যান্ট)",
        "meaningBn": "আন্তরিকতাহীন বুলি; ধার্মিক বা রাজনৈতিক কপট বচন",
        "synonyms": ["Hypocritical talk", "Humbug", "Pious platitudes", "Jargon"],
        "antonyms": ["Sincere speech", "Honest prose"],
        "exampleEn": "The activist demanded concrete policy changes rather than empty political cant.",
        "exampleBn": "কর্মীটি ফাঁকা রাজনৈতিক আন্তরিকতাহীন বুলির বদলে সুনির্দিষ্ট নীতিগত পরিবর্তন দাবি করেছিলেন।",
        "category": "BCS & Bank"
    },
    {
        "word": "Humbug",
        "pos": "Noun",
        "phonetic": "/ˈhʌm.bʌɡ/ (হামবাগ)",
        "meaningBn": "ধোঁকাবাজি, মিথ্যা ভড়ং বা প্রতারণামূলক ধাপ্পাবাজি",
        "synonyms": ["Nonsense", "Sham", "Deception", "Hoax"],
        "antonyms": ["Truth", "Veracity", "Honesty"],
        "exampleEn": "Ebenezer Scrooge famously dismissed Christmas goodwill as pure humbug.",
        "exampleBn": "ইবেনেজার স্ক্রুজ বড়দিনের শুভেচ্ছাবার্তাকে নিছক ধোঁকাবাজি ও ভড়ং বলে উড়িয়ে দিতেন।",
        "category": "Literature & Arts"
    }
]

with open('next_part2_sub2.json', 'w', encoding='utf-8') as f:
    json.dump(batch2_sub2, f, ensure_ascii=False, indent=2)
print("Part 2 sub 2 written:", len(batch2_sub2))

with open('next_part2_sub1.json') as f1, open('next_part2_sub2.json') as f2:
    w1 = json.load(f1)
    w2 = json.load(f2)

full2 = w1 + w2
for i, item in enumerate(full2):
    item['id'] = 6401 + i
    item['packId'] = 65

with open('next_part2.json', 'w', encoding='utf-8') as fout:
    json.dump(full2, fout, ensure_ascii=False, indent=2)
print("Complete next_part2.json ready with entries:", len(full2))

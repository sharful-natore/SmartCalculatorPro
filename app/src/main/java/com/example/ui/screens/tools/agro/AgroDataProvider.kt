package com.example.ui.screens.tools.agro

object AgroDataProvider {

    // -------------------------------------------------------------
    // 1. CROP CALENDAR DATA (Comprehensive)
    // -------------------------------------------------------------
    val cropCalendarList: List<CropCalendarItem> = listOf(
        // === দানা ও খাদ্যশস্য ===
        CropCalendarItem(
            id = "crop_boro_rice",
            cropNameBn = "বোরো ধান",
            cropNameEn = "Boro Rice",
            categoryBn = "দানা ও খাদ্যশস্য",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "১৫ কার্তিক - ১৫ অগ্রহায়ণ (নভেম্বর)",
            transplantAgeBn = "৩৫ - ৪৫ দিন বয়সের সুস্থ চারা",
            harvestTimeBn = "বৈশাখ - জ্যৈষ্ঠ (মে - জুন)",
            seedRatePerDecimalBn = "১০০ - ১২০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "২৫ - ৩৫ কেজি (শতক প্রতি)",
            landPrepTipsBn = "জমি ৩-৪ বার আড়াআড়ি চাষ ও মই দিয়ে থকথকে কাদা তৈরি করতে হবে। শেষ চাষে টিএসপি, পটাশ, জিপসাম ও দস্তা প্রয়োগ করুন।",
            specialAdviceBn = "শৈত্যপ্রবাহের সময় বীজতলায় রাতে ২-৩ ইঞ্চি পানি ধরে রাখুন এবং সকালে বের করে দিন যাতে ঠান্ডায় চারা নষ্ট না হয়।"
        ),
        CropCalendarItem(
            id = "crop_aman_rice",
            cropNameBn = "রোপা আমন ধান",
            cropNameEn = "Transplanted Aman Rice",
            categoryBn = "দানা ও খাদ্যশস্য",
            seasonBn = "খরিপ-২ (বর্ষা)",
            sowingTimeBn = "১৫ আষাঢ় - ১৫ শ্রাবণ (জুলাই)",
            transplantAgeBn = "২৫ - ৩০ দিন বয়সের চারা",
            harvestTimeBn = "কার্তিক - অগ্রহায়ণ (নভেম্বর - ডিসেম্বর)",
            seedRatePerDecimalBn = "১২০ - ১৫০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "২০ - ২৮ কেজি (শতক প্রতি)",
            landPrepTipsBn = "বৃষ্টির পানিতে জমি ভালোভাবে কাদা করে ৩-৪ টি চাষ ও মই দিতে হবে। জমিতে কোনো বড় ঢিলা রাখা যাবে না।",
            specialAdviceBn = "দেরিতে রোপণ করলে ফলন মারাত্মক কমে যায়। শ্রাবণ মাসের মধ্যেই মূল জমিতে রোপণ শেষ করুন।"
        ),
        CropCalendarItem(
            id = "crop_aush_rice",
            cropNameBn = "উফশী আউশ ধান",
            cropNameEn = "Aus Rice",
            categoryBn = "দানা ও খাদ্যশস্য",
            seasonBn = "খরিপ-১ (গ্রীষ্ম)",
            sowingTimeBn = "১৫ চৈত্র - ১৫ বৈশাখ (মার্চ - এপ্রিল)",
            transplantAgeBn = "২০ - ২৫ দিন বয়সের চারা বা সরাসরি বপন",
            harvestTimeBn = "আষাঢ় - শ্রাবণ (জুলাই - আগস্ট)",
            seedRatePerDecimalBn = "১০০ - ১২৫ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "১৪ - ১৮ কেজি (শতক প্রতি)",
            landPrepTipsBn = "জমি ঝুরঝুরে বা হালকা কাদাময় করে তৈরি করুন। বৃষ্টির ওপর নির্ভরশীল এলাকায় সেচের বিকল্প রাখুন।",
            specialAdviceBn = "আউশ কাটার পরই রোপা আমন চাষের চমৎকার সুযোগ তৈরি হয় (শস্য নিবিড়তা বৃদ্ধি পায়)।"
        ),
        CropCalendarItem(
            id = "crop_wheat",
            cropNameBn = "গম",
            cropNameEn = "Wheat",
            categoryBn = "দানা ও খাদ্যশস্য",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "১৫ কার্তিক - ১৫ অগ্রহায়ণ (নভেম্বর ১৫ - ৩০)",
            transplantAgeBn = "সরাসরি লাইনে বীজ বপন",
            harvestTimeBn = "ফাল্গুন - চৈত্র (মার্চ - এপ্রিল)",
            seedRatePerDecimalBn = "৪৫০ - ৫০০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "১৪ - ১৮ কেজি (শতক প্রতি)",
            landPrepTipsBn = "মাটি রসযুক্ত অবস্থায় ৩-৪ বার চাষ ও মই দিয়ে তৈরি করুন। লাইনে বপন করলে সেচ ও আগাছা নিড়ানো সহজ হয়।",
            specialAdviceBn = "বপনের ১৭-২১ দিনের মাথায় (প্রথম পাতা গজানোর পর) প্রথম মুকুট শিকড় পর্যায়ে সেচ দেওয়া ফলনের জন্য অত্যন্ত জরুরি।"
        ),
        CropCalendarItem(
            id = "crop_maize",
            cropNameBn = "ভুট্টা (হাইব্রিড)",
            cropNameEn = "Maize / Corn",
            categoryBn = "দানা ও খাদ্যশস্য",
            seasonBn = "রবি ও খরিপ-১",
            sowingTimeBn = "অক্টোবর - ডিসেম্বর (রবি) অথবা ফেব্রুয়ারি - মার্চ (খরিপ)",
            transplantAgeBn = "সরাসরি নির্দিষ্ট দূরত্বে বপন",
            harvestTimeBn = "১৩০ - ১৪৫ দিন পর (মোচা শক্ত হলে)",
            seedRatePerDecimalBn = "৮০ - ১০০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৩৫ - ৪৫ কেজি (শতক প্রতি)",
            landPrepTipsBn = "সারি থেকে সারি ৬০ সেমি (২৪ ইঞ্চি) এবং গাছ থেকে গাছ ২৫ সেমি (১০ ইঞ্চি) দূরত্ব বজায় রাখতে হবে।",
            specialAdviceBn = "ভুট্টায় জলাবদ্ধতা একদম সহ্য হয় না। জমিতে সেচের পর দ্রুত অতিরিক্ত পানি নিষ্কাশনের নালা রাখুন।"
        ),
        CropCalendarItem(
            id = "crop_foxtail_millet",
            cropNameBn = "কাউন ও চিনা",
            cropNameEn = "Foxtail Millet / Proso Millet",
            categoryBn = "দানা ও খাদ্যশস্য",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "পৌষ - মাঘ (ডিসেম্বর - জানুয়ারি)",
            transplantAgeBn = "সরাসরি ছিটিয়ে বা লাইনে বপন",
            harvestTimeBn = "চৈত্র - বৈশাখ (মার্চ - এপ্রিল)",
            seedRatePerDecimalBn = "৪০ - ৫০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৭ - ৯ কেজি (শতক প্রতি)",
            landPrepTipsBn = "চরের বেলে-দোআঁশ মাটিতে ২-৩ বার চাষ দিলেই তৈরি হয়।",
            specialAdviceBn = "খরা সহনশীল ও কম খরচের স্বাস্থ্যকর পুষ্টিকর খাদ্যশস্য।"
        ),

        // === শাকসবজি (Vegetables & Greens) ===
        CropCalendarItem(
            id = "crop_potato",
            cropNameBn = "গোল আলু",
            cropNameEn = "Potato",
            categoryBn = "শাকসবজি",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "১ কার্তিক - ১৫ অগ্রহায়ণ (অক্টোবর শেষ - নভেম্বর)",
            transplantAgeBn = "সরাসরি অঙ্কুরিত বীজ আলু রোপণ",
            harvestTimeBn = "মাঘ - ফাল্গুন (৮০ - ৯০ দিন পর)",
            seedRatePerDecimalBn = "৬ - ৮ কেজি আস্ত বীজ (শতক প্রতি)",
            estimatedYieldBn = "৮০ - ১০০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "বেলে-দোআঁশ মাটি আলুর জন্য সর্বোত্তম। ৫-৬ বার গভীর চাষ দিয়ে মাটি ঝুরঝুরে করতে হবে।",
            specialAdviceBn = "গাছের গোড়ায় মাটি তুলে দেওয়া এবং কুয়াশাচ্ছন্ন আবহাওয়ায় নাবি ধসা রোগ রোধে আগাম ছত্রাকনাশক স্প্রে করা জরুরি।"
        ),
        CropCalendarItem(
            id = "crop_brinjal",
            cropNameBn = "বেগুন",
            cropNameEn = "Brinjal / Eggplant",
            categoryBn = "শাকসবজি",
            seasonBn = "সারা বছর",
            sowingTimeBn = "রবি: ভাদ্র-আশ্বিন | খরিপ: ফাল্গুন-চৈত্র",
            transplantAgeBn = "৩০ - ৩৫ দিনের সুস্থ চারা",
            harvestTimeBn = "চারা রোপণের ৬০-৭০ দিন পর থেকে শুরু",
            seedRatePerDecimalBn = "১.৫ - ২ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "১০০ - ১৪০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "উঁচু বেড তৈরি করে প্রতি বেডে দুই সারিতে ৭৫ সেমি x ৬০ সেমি দূরত্বে চারা লাগান।",
            specialAdviceBn = "ডগা ও ফল ছিদ্রকারী পোকা দমনে ফেরোমোন ফাঁদ ব্যবহার করুন এবং আক্রান্ত ডগা দেখামাত্র কেটে মাটিতে পুঁতে ফেলুন।"
        ),
        CropCalendarItem(
            id = "crop_tomato",
            cropNameBn = "টমেটো",
            cropNameEn = "Tomato",
            categoryBn = "শাকসবজি",
            seasonBn = "রবি ও গ্রীষ্মকালীন",
            sowingTimeBn = "ভাদ্র - কার্তিক (সেপ্টেম্বর - অক্টোবর)",
            transplantAgeBn = "২৫ - ৩০ দিন বয়সের চারা",
            harvestTimeBn = "রোপণের ৭৫ - ৯০ দিন পর",
            seedRatePerDecimalBn = "১ - ১.৫ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "১২০ - ১৫০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "জমি তৈরির সময় প্রচুর জৈব সার ও ট্রাইকোডার্মা দিন। চারা লাগানোর পর কাঠি দিয়ে খুঁটি বেঁধে দিন।",
            specialAdviceBn = "পাতা কোঁকড়ানো সাদা মাছি দমনে হলুদ আঠালো ফাঁদ ব্যবহার করুন এবং গাছ মাটি স্পর্শ করতে দেবেন না।"
        ),
        CropCalendarItem(
            id = "crop_cauliflower",
            cropNameBn = "ফুলকপি (আর্লি ও লেইট)",
            cropNameEn = "Cauliflower",
            categoryBn = "শাকসবজি",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "আর্লি: শ্রাবণ-ভাদ্র | লেইট: আশ্বিন-কার্তিক",
            transplantAgeBn = "২৫ - ৩০ দিন বয়সের সুস্থ চারা",
            harvestTimeBn = "রোপণের ৬০ - ৮০ দিন পর",
            seedRatePerDecimalBn = "১.৫ - ২ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৮০ - ১০০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "উঁচু ড্রেনযুক্ত বেড তৈরি করুন। সারি ৬০ সেমি ও চারা ৪৫ সেমি দূরত্বে লাগান।",
            specialAdviceBn = "ফুলকপিতে মলিবডেনাম ও বোরনের ঘাটতিতে হুইপটেইল ও ফুল বাদামি হয়, তাই বোরন স্প্রে নিশ্চিত করুন।"
        ),
        CropCalendarItem(
            id = "crop_cabbage",
            cropNameBn = "বাঁধাকপি",
            cropNameEn = "Cabbage",
            categoryBn = "শাকসবজি",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "ভাদ্র - কার্তিক (সেপ্টেম্বর - নভেম্বর)",
            transplantAgeBn = "২৫ - ৩০ দিন বয়সের চারা",
            harvestTimeBn = "রোপণের ৭০ - ৯০ দিন পর",
            seedRatePerDecimalBn = "১.৫ - ২ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "১০০ - ১৩০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "জৈব সার ও পটাশ বেশি প্রয়োজন। চারা লাগানোর পর গোড়ায় সেচ দিয়ে ছায়া প্রদান করুন।",
            specialAdviceBn = "মাথা শক্ত ও আঁটসাঁট হয়ে এলে তুলে ফেলুন, দেরিতে তুললে ফেটে যেতে পারে।"
        ),
        CropCalendarItem(
            id = "crop_bottle_gourd",
            cropNameBn = "লাউ / কদু",
            cropNameEn = "Bottle Gourd",
            categoryBn = "শাকসবজি",
            seasonBn = "সারা বছর",
            sowingTimeBn = "রবি: ভাদ্র-আশ্বিন | গ্রীষ্ম: মাঘ-ফাল্গুন",
            transplantAgeBn = "মাদায় সরাসরি বীজ অথবা পলিব্যাগ চারা",
            harvestTimeBn = "বপনের ৬০ - ৭৫ দিন পর",
            seedRatePerDecimalBn = "৩ - ৪ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "১৫০ - ২০০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "২ মিটার x ২ মিটার দূরত্বে মাদা (গর্ত) করে প্রচুর গোবর, টিএসপি ও পটাশ সার দিয়ে মাদা তৈরি করুন।",
            specialAdviceBn = "মাচায় তুলে দিন এবং বিকেলে হাত দিয়ে কৃত্রিম পরাগায়ন (Hand Pollination) করালে ফলন দ্বিগুণ বাড়ে।"
        ),
        CropCalendarItem(
            id = "crop_sweet_pumpkin",
            cropNameBn = "মিষ্টিকুমড়া",
            cropNameEn = "Sweet Pumpkin",
            categoryBn = "শাকসবজি",
            seasonBn = "রবি ও খরিপ-১",
            sowingTimeBn = "কার্তিক - অগ্রহায়ণ (শীত) অথবা মাঘ - চৈত্র (গ্রীষ্ম)",
            transplantAgeBn = "পলিব্যাগ চারা ১২-১৫ দিন",
            harvestTimeBn = "৯০ - ১২০ দিন পর",
            seedRatePerDecimalBn = "২.৫ - ৩ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "১০০ - ১৫০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "উঁচু মাচায় বা খড়ের ওপর লতা ছড়িয়ে দিন। ফল যাতে সরাসরি ভেজা মাটি স্পর্শ না করে।",
            specialAdviceBn = "ফল মাছি পোকা দমনে ফেরোমোন ও বিষটোপ ফাঁদ ব্যবহার করুন।"
        ),
        CropCalendarItem(
            id = "crop_country_bean",
            cropNameBn = "শিম (দেশি ও হাইব্রিড)",
            cropNameEn = "Hyacinth Bean / Country Bean",
            categoryBn = "শাকসবজি",
            seasonBn = "রবি (শীত) ও গ্রীষ্মকালীন",
            sowingTimeBn = "আষাঢ় - ভাদ্র (জুলাই - সেপ্টেম্বর)",
            transplantAgeBn = "মাদায় সরাসরি বীজ বপন",
            harvestTimeBn = "কার্তিক থেকে চৈত্র পর্যন্ত ফলন",
            seedRatePerDecimalBn = "৪০ - ৫০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৫০ - ৮০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "আইলে বা মাচায় গাছ তুলে দিন। মাদা প্রতি ৫ কেজি পচা গোবর সার দিন।",
            specialAdviceBn = "জাবপোকা (Aphid) দমনে সাবান পানি অথবা ইমিডাক্লোপ্রিড স্প্রে করুন।"
        ),
        CropCalendarItem(
            id = "crop_radish",
            cropNameBn = "মুলা",
            cropNameEn = "Radish",
            categoryBn = "শাকসবজি",
            seasonBn = "রবি (শীত) ও আগাম",
            sowingTimeBn = "ভাদ্র - অগ্রহায়ণ (আগস্ট - নভেম্বর)",
            transplantAgeBn = "সরাসরি লাইনে বীজ বপন",
            harvestTimeBn = "৪০ - ৫০ দিন পর",
            seedRatePerDecimalBn = "১৫ - ২০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৮০ - ১০০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "গভীর চাষ দিয়ে মাটি ঝুরঝুরে করুন যেন মুলার শিকড় সহজে নিচে নামে ও আঁকাবাঁকা না হয়।",
            specialAdviceBn = "অতিরিক্ত বয়সের আগেই কচি অবস্থায় মুলা তুলুন যাতে স্পঞ্জি না হয়ে যায়।"
        ),
        CropCalendarItem(
            id = "crop_carrot",
            cropNameBn = "গাজর",
            cropNameEn = "Carrot",
            categoryBn = "শাকসবজি",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "আশ্বিন - অগ্রহায়ণ (অক্টোবর - নভেম্বর)",
            transplantAgeBn = "সরাসরি সারিতে বপন",
            harvestTimeBn = "৮০ - ১০০ দিন পর",
            seedRatePerDecimalBn = "৮ - ১০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৭০ - ৯০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "বেলে-দোআঁশ মাটি গাজরের জন্য সবচেয়ে ভালো। মাটির বড় ঢিলা ভেঙে সমান করুন।",
            specialAdviceBn = "বীজ গজানোর পর চারা পাতলা করে দূরত্ব বজায় রাখুন যেন গাজর মোটা ও সুন্দর রঙের হয়।"
        ),
        CropCalendarItem(
            id = "crop_bitter_gourd",
            cropNameBn = "করলা ও উচ্ছে",
            cropNameEn = "Bitter Gourd",
            categoryBn = "শাকসবজি",
            seasonBn = "খরিপ-১ ও সারা বছর",
            sowingTimeBn = "মাঘ - বৈশাখ (ফেব্রুয়ারি - এপ্রিল)",
            transplantAgeBn = "পলিব্যাগ চারা বা সরাসরি বপন",
            harvestTimeBn = "৫৫ - ৬৫ দিন পর",
            seedRatePerDecimalBn = "১০ - ১২ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৪০ - ৬০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "মাচা তৈরি করে লতা তুলে দিন। ড্রেনেজ নালা তৈরি রাখুন।",
            specialAdviceBn = "মাছি পোকা থেকে ফল রক্ষায় প্লাস্টিক বা কাগজের ঠোঙা দিয়ে কচি করলা ঢেকে দেওয়া যায়।"
        ),
        CropCalendarItem(
            id = "crop_cucumber",
            cropNameBn = "শসা ও ক্ষীরা",
            cropNameEn = "Cucumber",
            categoryBn = "শাকসবজি",
            seasonBn = "সারা বছর",
            sowingTimeBn = "মাঘ - চৈত্র (গ্রীষ্ম) ও ভাদ্র - আশ্বিন (শীত)",
            transplantAgeBn = "সরাসরি মাদায় বপন",
            harvestTimeBn = "৪৫ - ৫০ দিন পর",
            seedRatePerDecimalBn = "৪ - ৫ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৬০ - ৮০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "উঁচু বেড তৈরি করে মালচিং পেপার ব্যবহার করলে শসার ফলন ও মান অনেক বৃদ্ধি পায়।",
            specialAdviceBn = "ডাউনি মিলডিউ রোগ প্রতিরোধে কপার বা ম্যানকোজেব স্প্রে করে রাখুন।"
        ),
        CropCalendarItem(
            id = "crop_pointed_gourd",
            cropNameBn = "পটল",
            cropNameEn = "Pointed Gourd",
            categoryBn = "শাকসবজি",
            seasonBn = "খরিপ ও রবি",
            sowingTimeBn = "আশ্বিন - কার্তিক (অক্টোবর - নভেম্বর)",
            transplantAgeBn = "লতার কাটিং (রুট কাটিং)",
            harvestTimeBn = "ফাল্গুন থেকে আশ্বিন পর্যন্ত দীর্ঘমেয়াদী",
            seedRatePerDecimalBn = "৪০ - ৫০ টি সুস্থ লতার কাটিং",
            estimatedYieldBn = "৮০ - ১২০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "উঁচু সুনিষ্কাশিত বেলে মাটি। জমিতে ১০:১ অনুপাতে স্ত্রী ও পুরুষ গাছের লতা রোপণ করতে হয়।",
            specialAdviceBn = "সকালে পুরুষ ফুল ছিঁড়ে স্ত্রী ফুলের গর্ভমুণ্ডে স্পর্শ করিয়ে পরাগায়ন করালে সর্বোচ্চ ফলন পাওয়া যায়।"
        ),
        CropCalendarItem(
            id = "crop_okra",
            cropNameBn = "ঢ্যাঁড়শ / ভেন্ডি",
            cropNameEn = "Okra / Lady's Finger",
            categoryBn = "শাকসবজি",
            seasonBn = "খরিপ-১ (গ্রীষ্ম)",
            sowingTimeBn = "ফাল্গুন - বৈশাখ (ফেব্রুয়ারি - এপ্রিল)",
            transplantAgeBn = "সরাসরি সারিতে বপন",
            harvestTimeBn = "৪৫ - ৫৫ দিন পর থেকে নিয়মিত সংগ্রহ",
            seedRatePerDecimalBn = "১৫ - ২০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৪০ - ৫০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "সারি থেকে সারি ৫০ সেমি ও গাছ থেকে গাছ ৩০ সেমি রাখুন।",
            specialAdviceBn = "হলুদ মোজাইক ভাইরাস প্রতিরোধী জাত (যেমন: অর্ক অনামিকা, বারি ঢ্যাঁড়শ-১) চাষ করুন।"
        ),
        CropCalendarItem(
            id = "crop_red_spinach",
            cropNameBn = "লালশাক",
            cropNameEn = "Red Amaranth",
            categoryBn = "শাকসবজি",
            seasonBn = "সারা বছর",
            sowingTimeBn = "সারা বছরই বপনযোগ্য (বিশেষত ফাল্গুন - কার্তিক)",
            transplantAgeBn = "সরাসরি ছিটিয়ে বা ঘন সারিতে বপন",
            harvestTimeBn = "বপনের ২৫ - ৩৫ দিন পর",
            seedRatePerDecimalBn = "৮ - ১০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৩০ - ৪০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "মাটি একদম মিহি ও ঝুরঝুরে করে বীজ ছিটান। বীজের সাথে ছাই বা বালু মিশিয়ে ছিটালে সমানভাবে পড়ে।",
            specialAdviceBn = "পাতা কাটার পর হালকা ইউরিয়া পানি স্প্রে করলে পুনরায় দ্রুত কুঁড়ি গজায়।"
        ),
        CropCalendarItem(
            id = "crop_spinach",
            cropNameBn = "পালংশাক",
            cropNameEn = "Spinach",
            categoryBn = "শাকসবজি",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "ভাদ্র - অগ্রহায়ণ (সেপ্টেম্বর - নভেম্বর)",
            transplantAgeBn = "সরাসরি লাইনে বপন",
            harvestTimeBn = "৩০ - ৪০ দিন পর",
            seedRatePerDecimalBn = "১৫ - ২০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৪০ - ৫০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "মাটিতে প্রচুর গোবর সার ও আর্দ্রতা থাকা দরকার।",
            specialAdviceBn = "গোড়ার পাতা ছেঁটে সংগ্রহ করলে গাছ থেকে ৩-৪ বার শাক কাটা যায়।"
        ),
        CropCalendarItem(
            id = "crop_pui_shak",
            cropNameBn = "পুঁইশাক",
            cropNameEn = "Indian Spinach / Malabar Spinach",
            categoryBn = "শাকসবজি",
            seasonBn = "খরিপ ও সারা বছর",
            sowingTimeBn = "ফাল্গুন - আষাঢ় (মার্চ - জুন)",
            transplantAgeBn = "সরাসরি বীজ অথবা লতার ডাল কাটিং",
            harvestTimeBn = "৪০ - ৫০ দিন পর থেকে শুরু করে শীতের আগ পর্যন্ত",
            seedRatePerDecimalBn = "১০ - ১২ গ্রাম বীজ",
            estimatedYieldBn = "৬০ - ৮০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "বেড়ার পাশে বা মাচায় তুলে দিন। প্রচুর নাইট্রোজেন ও পানি পছন্দ করে।",
            specialAdviceBn = "ডগা কেটে দিলে নতুন প্রচুর পার্শ্বডাল গজায় এবং দীর্ঘ সময় ফলন পাওয়া যায়।"
        ),
        CropCalendarItem(
            id = "crop_coriander",
            cropNameBn = "ধনেপাতা",
            cropNameEn = "Coriander",
            categoryBn = "শাকসবজি",
            seasonBn = "রবি ও সারা বছর",
            sowingTimeBn = "আশ্বিন - পৌষ (অক্টোবর - ডিসেম্বর)",
            transplantAgeBn = "বীজ ডলে দুই ফালি করে ২৪ ঘণ্টা ভিজিয়ে বপন",
            harvestTimeBn = "৩০ - ৪০ দিন পর",
            seedRatePerDecimalBn = "২০ - ২৫ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "১৫ - ২০ কেজি সুগন্ধি পাতা",
            landPrepTipsBn = "বেড উঁচু রাখুন এবং মাটি সর্বদা নরম ও আর্দ্র রাখুন।",
            specialAdviceBn = "গ্রীষ্মকালীন সময়ে ছায়াযুক্ত স্থানে শেড নেট দিয়ে চাষ করলে ভালো দাম পাওয়া যায়।"
        ),

        // === ফলমূল (Fruits) ===
        CropCalendarItem(
            id = "crop_mango",
            cropNameBn = "আম (আম্রপালি/হিমসাগর/কাটিমন)",
            cropNameEn = "Mango",
            categoryBn = "ফলমূল",
            seasonBn = "গ্রীষ্ম ও বারোমাসি",
            sowingTimeBn = "চারা রোপণ: আষাঢ় - ভাদ্র (জুন - আগস্ট)",
            transplantAgeBn = "কলমের ১-২ বছর বয়সের সুস্থ চারা",
            harvestTimeBn = "জ্যৈষ্ঠ - আষাঢ় (মে - জুলাই) | কাটিমন সারা বছর",
            seedRatePerDecimalBn = "শতকে ৪-৬ টি গাছ (হাই ডেনসিটি)",
            estimatedYieldBn = "গাছপ্রতি ৪০ - ৮০ কেজি",
            landPrepTipsBn = "৩ ফুট x ৩ ফুট গর্ত করে পচা গোবর, টিএসপি, পটাশ ও জিপসাম দিয়ে ১৫ দিন পর চারা রোপণ করুন।",
            specialAdviceBn = "মুকুল আসার আগে হপার পোকা ও অ্যানথ্রাকনোজ দমনে ইমিডাক্লোপ্রিড ও ম্যানকোজেব স্প্রে আবশ্যক।"
        ),
        CropCalendarItem(
            id = "crop_banana",
            cropNameBn = "কলা (সবরি / মেহেরসাগর / জি-৯)",
            cropNameEn = "Banana",
            categoryBn = "ফলমূল",
            seasonBn = "সারা বছর",
            sowingTimeBn = "আশ্বিন-কার্তিক (১ম), মাঘ-ফাল্গুন (২য়), চৈত্র-বৈশাখ (৩য়)",
            transplantAgeBn = "সুস্থ ও নিরোগ তরবারি তেউড় (Sword Sucker)",
            harvestTimeBn = "রোপণের ১১ - ১৩ মাস পর",
            seedRatePerDecimalBn = "শতকে ১৬ - ১৮ টি চারা (৬ ফুট x ৬ ফুট)",
            estimatedYieldBn = "শতকে ১৬ - ১৮ কাঁদি কলা",
            landPrepTipsBn = "উঁচু সুনিষ্কাশিত বেলে-দোআঁশ মাটি। জলাবদ্ধতা কলার প্রধান শত্রু।",
            specialAdviceBn = "মোজাইক ও পানামা রোগমুক্ত টিস্যুকালচার চারা ব্যবহার করলে সর্বোচ্চ লাভ হয়।"
        ),
        CropCalendarItem(
            id = "crop_papaya",
            cropNameBn = "পেঁপে (রেড লেডি / শাহী)",
            cropNameEn = "Papaya",
            categoryBn = "ফলমূল",
            seasonBn = "সারা বছর",
            sowingTimeBn = "আশ্বিন - কার্তিক অথবা ফাল্গুন - চৈত্র",
            transplantAgeBn = "পলিব্যাগে ৩৫ - ৪০ দিনের চারা",
            harvestTimeBn = "রোপণের ৭ - ৯ মাস পর থেকে পাকা ফল",
            seedRatePerDecimalBn = "শতকে ৮ - ১০ টি গাছ (৬ ফুট x ৬ ফুট)",
            estimatedYieldBn = "গাছপ্রতি ৩৫ - ৫০ কেজি",
            landPrepTipsBn = "উঁচু বেড ও দুই লাইনের মাঝে গভীর পানি নিষ্কাশন নালা বাধ্যতামূলক।",
            specialAdviceBn = "গোড়ায় যেন এক ফোঁটাও পানি না জমে, পানি জমলে শিকড় পচে গাছ মারা যায়।"
        ),
        CropCalendarItem(
            id = "crop_guava",
            cropNameBn = "পেয়ারা (থাই-৭ / কাজী পেয়ারা)",
            cropNameEn = "Guava",
            categoryBn = "ফলমূল",
            seasonBn = "বর্ষা ও শীতকালীন",
            sowingTimeBn = "জ্যৈষ্ঠ - ভাদ্র (মে - আগস্ট)",
            transplantAgeBn = "গ্রাফটিং বা গুটি কলমের চারা",
            harvestTimeBn = "রোপণের ১ম বছর থেকেই ফলন শুরু",
            seedRatePerDecimalBn = "শতকে ৬ - ৮ টি চারা",
            estimatedYieldBn = "গাছপ্রতি ২৫ - ৪৫ কেজি",
            landPrepTipsBn = "গাছের নিয়মিত প্রুনিং বা ডাল ছাঁটাই করলে নতুন ডালে প্রচুর পেয়ারা ধরে।",
            specialAdviceBn = "মার্বেল আকৃতির থাকতেই পলিথিন ব্যাগিং করলে মাছি পোকার আক্রমণ হয় না ও পেয়ারা চকচকে থাকে।"
        ),
        CropCalendarItem(
            id = "crop_watermelon",
            cropNameBn = "তরমুজ ও বাঙ্গি",
            cropNameEn = "Watermelon / Musk Melon",
            categoryBn = "ফলমূল",
            seasonBn = "রবি ও খরিপ-১ (গ্রীষ্ম)",
            sowingTimeBn = "পৌষ - মাঘ (ডিসেম্বর - জানুয়ারি)",
            transplantAgeBn = "পলিব্যাগ চারা বা সরাসরি মাদায় বীজ",
            harvestTimeBn = "বপনের ৭০ - ৮৫ দিন পর",
            seedRatePerDecimalBn = "১.৫ - ২ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "১০০ - ১৪০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "চরের বেলে ও উপকূলীয় দোআঁশ মাটিতে খড় বা মালচিং পেপার দিয়ে বেড তৈরি করুন।",
            specialAdviceBn = "ফল বড় হওয়ার সময় নিয়মিত পর্যাপ্ত সেচ দিন এবং পাকার ১০ দিন আগে সেচ কমান।"
        ),
        CropCalendarItem(
            id = "crop_dragon_fruit",
            cropNameBn = "ড্রাগন ফল (লাল ও সাদা)",
            cropNameEn = "Dragon Fruit",
            categoryBn = "ফলমূল",
            seasonBn = "গ্রীষ্ম ও বর্ষা (মে - নভেম্বর)",
            sowingTimeBn = "বৈশাখ - আশ্বিন (মে - অক্টোবর)",
            transplantAgeBn = "এক বছর বয়সের সুস্থ কাটিং ডাল",
            harvestTimeBn = "ফুল ফোটার ৩০ - ৩৫ দিন পর ফল পাকা শুরু",
            seedRatePerDecimalBn = "শতকে ৪টি আরসিসি পিলার (প্রতি পিলারে ৪টি করে ১৬টি চারা)",
            estimatedYieldBn = "শতকে ৮০ - ১২০ কেজি",
            landPrepTipsBn = "আরসিসি পিলারের মাথায় মোটরসাইকেলের পুরনো টায়ার বা রিং বসিয়ে লতা ঝুলিয়ে দিন।",
            specialAdviceBn = "অতিরিক্ত বৃষ্টিতে ছত্রাকনাশক স্প্রে করুন এবং রাতে আলোর ব্যবস্থা করলে ফলন বাড়ে।"
        ),
        CropCalendarItem(
            id = "crop_malta_lemon",
            cropNameBn = "বারি মাল্টা-১ ও কাগজি লেবু",
            cropNameEn = "Malta & Lemon",
            categoryBn = "ফলমূল",
            seasonBn = "সারা বছর ও শরৎকাল",
            sowingTimeBn = "আষাঢ় - ভাদ্র (জুন - সেপ্টেম্বর)",
            transplantAgeBn = "কলমের সুস্থ চারা",
            harvestTimeBn = "ভাদ্র - কার্তিক (মাল্টা) | লেবু সারা বছর",
            seedRatePerDecimalBn = "শতকে ৪ - ৬ টি গাছ (১০ ফুট x ১০ ফুট)",
            estimatedYieldBn = "গাছপ্রতি ২০০ - ৪০০ টি লেবু/মাল্টা",
            landPrepTipsBn = "মাটি অম্লীয় হলে ডলোমাইট চুন দিন এবং বছরে ৩ বার সুষম সার প্রয়োগ করুন।",
            specialAdviceBn = "লেবুর ক্যাঙ্কার রোগ প্রতিরোধে কপার অক্সিক্লোরাইড ও স্ট্রেপ্টোমাইসিন স্প্রে করুন।"
        ),
        CropCalendarItem(
            id = "crop_strawberry",
            cropNameBn = "স্ট্রবেরি",
            cropNameEn = "Strawberry",
            categoryBn = "ফলমূল",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "আশ্বিন - কার্তিক (অক্টোবর - নভেম্বর)",
            transplantAgeBn = "টিস্যুকালচার রানার চারা",
            harvestTimeBn = "পৌষ - চৈত্র (জানুয়ারি - মার্চ)",
            seedRatePerDecimalBn = "শতকে ১২০ - ১৫০ টি চারা",
            estimatedYieldBn = "২০ - ৩০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "কালো মালচিং পেপার দিয়ে বেড ঢেকে কাটিং করে চারা লাগান যাতে ফল মাটিতে না ঠেকে।",
            specialAdviceBn = "পাখি ও পোকামাকড় থেকে রক্ষায় নেট হাউস বা পলি টানেলে চাষ লাভজনক।"
        ),

        // === ফুল ও শোভাবর্ধক (Flowers & Floriculture) ===
        CropCalendarItem(
            id = "crop_marigold",
            cropNameBn = "গাঁদা ফুল (ইনকা / ফরাসি)",
            cropNameEn = "Marigold",
            categoryBn = "ফুল ও শোভাবর্ধক",
            seasonBn = "রবি (শীত) ও সারা বছর",
            sowingTimeBn = "ভাদ্র - কার্তিক (আগস্ট - অক্টোবর)",
            transplantAgeBn = "২০ - ২৫ দিন বয়সের চারা বা ডাল কাটিং",
            harvestTimeBn = "অগ্রহায়ণ - ফাল্গুন (নভেম্বর - ফেব্রুয়ারি)",
            seedRatePerDecimalBn = "শতকে ১৫০ - ১৮০ টি চারা (১৫ ইঞ্চি x ১২ ইঞ্চি)",
            estimatedYieldBn = "শতকে ২০,০০০ - ২৫,০০০ ফুল",
            landPrepTipsBn = "উঁচু বেড তৈরি করে প্রচুর পচা গোবর ও পটাশ সার দিন।",
            specialAdviceBn = "চারা রোপণের ২৫ দিন পর আগা ভেঙে দিলে প্রচুর শাখা বের হয় ও ফুলের সংখ্যা দ্বিগুণ হয়।"
        ),
        CropCalendarItem(
            id = "crop_rose",
            cropNameBn = "গোলাপ (কাট ফ্লাওয়ার)",
            cropNameEn = "Rose",
            categoryBn = "ফুল ও শোভাবর্ধক",
            seasonBn = "শীত ও সারা বছর",
            sowingTimeBn = "আশ্বিন - কার্তিক (অক্টোবর - নভেম্বর)",
            transplantAgeBn = "চোখ কলম বা বাডিং চারা",
            harvestTimeBn = "শীত ও বসন্তকালে সর্বোচ্চ বাণিজ্যিক সংগ্রহ",
            seedRatePerDecimalBn = "শতকে ১০০ - ১২০ টি চারা",
            estimatedYieldBn = "শতকে ৮,০০০ - ১২,০০০ স্টিক",
            landPrepTipsBn = "বছরে অন্তত একবার (আশ্বিন মাসে) হার্ড প্রুনিং করে ডাল ছাঁটাই ও বোর্দো মিশ্রণ লাগাতে হবে।",
            specialAdviceBn = "কালো দাগ ও ডাই ব্যাক রোগ দমনে ডাল কাটার অংশে কার্বেনডাজিম পেস্ট লাগিয়ে দিন।"
        ),
        CropCalendarItem(
            id = "crop_tuberose",
            cropNameBn = "রজনীগন্ধা (সিঙ্গেল ও ডাবল)",
            cropNameEn = "Tuberose",
            categoryBn = "ফুল ও শোভাবর্ধক",
            seasonBn = "সারা বছর (বসন্ত ও বর্ষা)",
            sowingTimeBn = "ফাল্গুন - বৈশাখ (ফেব্রুয়ারি - এপ্রিল)",
            transplantAgeBn = "মাঝারি আকারের কন্দ বা বাল্ব",
            harvestTimeBn = "রোপণের ৩-৪ মাস পর থেকে নিয়মিত স্টিক কাটা",
            seedRatePerDecimalBn = "শতকে ৪০০ - ৫০০ টি কন্দ (৩০ সেমি x ২০ সেমি)",
            estimatedYieldBn = "শতকে ১,২০০ - ১,৫০০ ফুল স্টিক",
            landPrepTipsBn = "দোআঁশ মাটিতে রোদযুক্ত উঁচু জমিতে কন্দ রোপণ করুন।",
            specialAdviceBn = "স্টিক কাটার সময় গোড়ায় ১ ইঞ্চি রেখে ধারালো ছুরি দিয়ে সকাল বা বিকেলে কাটুন।"
        ),
        CropCalendarItem(
            id = "crop_gladiolus",
            cropNameBn = "গ্ল্যাডিওলাস",
            cropNameEn = "Gladiolus",
            categoryBn = "ফুল ও শোভাবর্ধক",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "আশ্বিন - অগ্রহায়ণ (অক্টোবর - নভেম্বর)",
            transplantAgeBn = "পরিপক্ক করম (Corm)",
            harvestTimeBn = "বপনের ৭৫ - ৯০ দিন পর",
            seedRatePerDecimalBn = "শতকে ৩৫০ - ৪০০ টি করম",
            estimatedYieldBn = "শতকে ৩০০ - ৩৫০ টি স্টিক",
            landPrepTipsBn = "সারি থেকে সারি ৩০ সেমি ও করম থেকে করম ১৫ সেমি দূরত্বে ৫ সেমি গভীরে লাগান।",
            specialAdviceBn = "নিচের প্রথম ফুল ফোটার উপক্রম হলেই ফুল স্টিক কেটে পানিতে সংরক্ষণ করুন।"
        ),
        CropCalendarItem(
            id = "crop_sunflower",
            cropNameBn = "সূর্যমুখী (তেল ও ফুল)",
            cropNameEn = "Sunflower",
            categoryBn = "ফুল ও শোভাবর্ধক",
            seasonBn = "রবি ও খরিপ-১",
            sowingTimeBn = "কার্তিক - অগ্রহায়ণ (নভেম্বর - ডিসেম্বর)",
            transplantAgeBn = "সরাসরি নির্দিষ্ট দূরত্বে সারিতে বীজ",
            harvestTimeBn = "৯০ - ১০৫ দিন পর",
            seedRatePerDecimalBn = "৩৫ - ৪০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৮ - ১০ কেজি বীজ",
            landPrepTipsBn = "সারি ৫০ সেমি ও গাছ ২৫ সেমি দূরত্বে রাখুন। শেষ চাষে বোরন ও জিপসাম দিন।",
            specialAdviceBn = "ফুল ফোটার সময় জমিতে রস নিশ্চিত করুন যাতে পুষ্ট বড় বীজ গঠিত হয়।"
        ),

        // === মসলা ও অর্থকরী ফসল (Spices & Cash Crops) ===
        CropCalendarItem(
            id = "crop_onion",
            cropNameBn = "পেঁয়াজ (তাহেরপুরী/বারি-১)",
            cropNameEn = "Onion",
            categoryBn = "মসলা ও অর্থকরী",
            seasonBn = "রবি ও গ্রীষ্মকালীন",
            sowingTimeBn = "কার্তিক - অগ্রহায়ণ (অক্টোবর - নভেম্বর)",
            transplantAgeBn = "৪০ - ৪৫ দিনের কন্দ বা চারা",
            harvestTimeBn = "ফাল্গুন - চৈত্র (৯০ - ১০০ দিন পর)",
            seedRatePerDecimalBn = "৪০ - ৫০ গ্রাম বীজ অথবা ২০-২৫ কেজি কন্দ",
            estimatedYieldBn = "৪০ - ৬০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "উঁচু বেড তৈরি করে ১৫ সেমি x ১০ সেমি দূরত্বে চারা রোপণ করুন। গোড়ায় পানি জমতে দেবেন না।",
            specialAdviceBn = "ফসল তোলার ১৫ দিন আগে সেচ বন্ধ করে দিন যাতে পেঁয়াজের ত্বক শক্ত হয় এবং ঘরে বেশিদিন টেকে।"
        ),
        CropCalendarItem(
            id = "crop_garlic",
            cropNameBn = "রসুন (জিরো টিলেজ ও স্বাভাবিক)",
            cropNameEn = "Garlic",
            categoryBn = "মসলা ও অর্থকরী",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "১৫ আশ্বিন - কার্তিক (অক্টোবর - নভেম্বর)",
            transplantAgeBn = "সুস্থ রসুনের বড় কোয়া সরাসরি রোপণ",
            harvestTimeBn = "ফাল্গুন - চৈত্র (১১০ - ১২০ দিন পর)",
            seedRatePerDecimalBn = "২ - ২.৫ কেজি কোয়া (শতক প্রতি)",
            estimatedYieldBn = "২০ - ৩০ কেজি (শতক প্রতি)",
            landPrepTipsBn = "বিনা চাষে (Zero-tillage) ধান কাটার পর ভেজা মাটিতে খড় বিছিয়ে রসুন রোপণ অত্যন্ত লাভজনক।",
            specialAdviceBn = "পার্পল ব্লচ বা বেগুনি দাগ রোগ রোধে রোভরাল বা অটোস্টিন স্প্রে করুন।"
        ),
        CropCalendarItem(
            id = "crop_ginger",
            cropNameBn = "আদা",
            cropNameEn = "Ginger",
            categoryBn = "মসলা ও অর্থকরী",
            seasonBn = "খরিপ (দীর্ঘমেয়াদী)",
            sowingTimeBn = "চৈত্র - বৈশাখ (এপ্রিল - মে)",
            transplantAgeBn = "অঙ্কুরিত কন্দ বা রাইজোম কাটিং",
            harvestTimeBn = "পৌষ - মাঘ (ডিসেম্বর - জানুয়ারি)",
            seedRatePerDecimalBn = "৮ - ১০ কেজি রাইজোম (শতক প্রতি)",
            estimatedYieldBn = "৪০ - ৬০ কেজি কাঁচা আদা",
            landPrepTipsBn = "ছায়াযুক্ত বা ফলের বাগানের সাথী ফসল হিসেবে উঁচু বেডে চাষের জন্য আদর্শ।",
            specialAdviceBn = "আদার নরম পচা রোগ প্রতিরোধে কন্দ শোধন ও রিডোমিল গোল্ড স্প্রে নিশ্চিত করুন।"
        ),
        CropCalendarItem(
            id = "crop_turmeric",
            cropNameBn = "হলুদ (ডিমলা/বারি-১)",
            cropNameEn = "Turmeric",
            categoryBn = "মসলা ও অর্থকরী",
            seasonBn = "খরিপ (দীর্ঘমেয়াদী)",
            sowingTimeBn = "চৈত্র - বৈশাখ (মার্চ - মে)",
            transplantAgeBn = "সুস্থ কন্দ রোপণ",
            harvestTimeBn = "পৌষ - মাঘ (৯ - ১০ মাস পর)",
            seedRatePerDecimalBn = "১০ - ১২ কেজি কন্দ",
            estimatedYieldBn = "৫০ - ৭০ কেজি কাঁচা হলুদ",
            landPrepTipsBn = "উঁচু বেড তৈরি করে খড় দিয়ে মালচিং করে দিলে আর্দ্রতা বজায় থাকে ও ফলন বাড়ে।",
            specialAdviceBn = "গাছের গোড়ায় মাটি তুলে দেওয়া এবং পাতা পোড়া রোগ প্রতিরোধে ব্যবস্থা নিন।"
        ),
        CropCalendarItem(
            id = "crop_chilli",
            cropNameBn = "মরিচ (কাঁচা ও শুকনা)",
            cropNameEn = "Chilli",
            categoryBn = "মসলা ও অর্থকরী",
            seasonBn = "সারা বছর",
            sowingTimeBn = "রবি: ভাদ্র-আশ্বিন | খরিপ: ফাল্গুন-বৈশাখ",
            transplantAgeBn = "৩০ - ৩৫ দিনের সুস্থ চারা",
            harvestTimeBn = "৬০ দিন পর থেকে শুরু হয়ে ৫-৬ মাস ফলন",
            seedRatePerDecimalBn = "১ - ১.৫ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "২৫ - ৩৫ কেজি কাঁচা মরিচ",
            landPrepTipsBn = "জমি সুনিষ্কাশিত হতে হবে। মরিচ গাছ জলাবদ্ধতা একদমই সহ্য করতে পারে না।",
            specialAdviceBn = "থ্রিপস ও মাকড়ের আক্রমণে পাতা উল্টো নৌকার মতো কোঁকড়ালে মাকড়নাশক (যেমন ভার্টিমেক) স্প্রে করুন।"
        ),
        CropCalendarItem(
            id = "crop_mustard",
            cropNameBn = "সরিষা (উফশী বারি-১৪/১৮)",
            cropNameEn = "Mustard",
            categoryBn = "মসলা ও অর্থকরী",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "১৫ আশ্বিন - কার্তিক (অক্টোবর - নভেম্বর)",
            transplantAgeBn = "সরাসরি ছিটিয়ে বা সারিতে বপন",
            harvestTimeBn = "৭০ - ৮৫ দিন পর (গাছ হলুদ হলে)",
            seedRatePerDecimalBn = "৩০ - ৩৫ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৫ - ৭ কেজি (শতক প্রতি)",
            landPrepTipsBn = "জমির জো অবস্থায় মাটির গভীর চাষ দিন। সরিষার জমিতে বোরন ও জিপসাম দিলে দানার তেল ও ওজন বাড়ে।",
            specialAdviceBn = "গাছে ফুল আসার সময় কোনো কীটনাশক স্প্রে করবেন না, এতে পরাগায়নকারী মৌমাছি মারা গিয়ে ফলন কমে যায়।"
        ),
        CropCalendarItem(
            id = "crop_jute",
            cropNameBn = "পাট (তোষা ও দেশী)",
            cropNameEn = "Jute",
            categoryBn = "মসলা ও অর্থকরী",
            seasonBn = "খরিপ-১ (গ্রীষ্ম)",
            sowingTimeBn = "চৈত্র - বৈশাখ (মার্চ - এপ্রিল)",
            transplantAgeBn = "সরাসরি সারিতে বা ছিটিয়ে বপন",
            harvestTimeBn = "শ্রাবণ - ভাদ্র (১০০ - ১২০ দিন পর)",
            seedRatePerDecimalBn = "২০ - ২৫ গ্রাম (তোষা) | ৩০ গ্রাম (দেশী)",
            estimatedYieldBn = "১০ - ১৪ কেজি শুকনা পাট আঁশ",
            landPrepTipsBn = "মাটি মিহি করে চাষ দিন। গাছে ফুল আসার আগেই কেটে জাগ (জলাশয়ে পচানো) দিলে সোনালী আঁশ পাওয়া যায়।",
            specialAdviceBn = "রিবন রেটিং পদ্ধতিতে কম পানিতে উন্নত মানের পাট পচানো সম্ভব।"
        ),
        CropCalendarItem(
            id = "crop_groundnut",
            cropNameBn = "চীনাবাদাম (ঢাকা-১ / মাইচর)",
            cropNameEn = "Groundnut / Peanut",
            categoryBn = "মসলা ও অর্থকরী",
            seasonBn = "রবি ও খরিপ-২",
            sowingTimeBn = "কার্তিক - অগ্রহায়ণ (শীত) অথবা আষাঢ় - শ্রাবণ (বর্ষা)",
            transplantAgeBn = "খোসা ছাড়ানো দানা সরাসরি বপন",
            harvestTimeBn = "১২০ - ১৪০ দিন পর",
            seedRatePerDecimalBn = "৩০০ - ৩৫০ গ্রাম দানা",
            estimatedYieldBn = "৮ - ১০ কেজি বাদাম",
            landPrepTipsBn = "নদীর চরের বেলে-দোআঁশ মাটি বাদাম চাষের জন্য সবচেয়ে উপযুক্ত।",
            specialAdviceBn = "গাছে ফুল আসার পর মাটি আলগা করে দিন যাতে সুঁই (Peg) সহজে মাটিতে ঢুকতে পারে।"
        ),

        // === ডাল জাতীয় ফসল (Pulses & Legumes) ===
        CropCalendarItem(
            id = "crop_lentil",
            cropNameBn = "মসুর ডাল (উফশী বারি-৬/৭)",
            cropNameEn = "Lentil",
            categoryBn = "ডাল ও তৈলবীজ",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "১৫ কার্তিক - অগ্রহায়ণ (নভেম্বর)",
            transplantAgeBn = "সরাসরি ছিটিয়ে বা সারিতে বপন",
            harvestTimeBn = "মাঘ - ফাল্গুন (৯০ - ১০০ দিন পর)",
            seedRatePerDecimalBn = "১২০ - ১৫০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৫ - ৭ কেজি (শতক প্রতি)",
            landPrepTipsBn = "আমন ধান কাটার আগেই রিলে ফসল হিসেবে ভেজা মাটিতে ছিটিয়েও মসুর চাষ করা যায়।",
            specialAdviceBn = "স্টেমফিলিয়াম ব্লাইট রোগ দমনে রোভরাল বা প্রোভ্যাক্স দিয়ে বীজ শোধন করুন।"
        ),
        CropCalendarItem(
            id = "crop_mungbean",
            cropNameBn = "মুগ ডাল (বারি মুগ-৬)",
            cropNameEn = "Mungbean",
            categoryBn = "ডাল ও তৈলবীজ",
            seasonBn = "খরিপ-১ ও রবি",
            sowingTimeBn = "মাঘ - ফাল্গুন (ফেব্রুয়ারি - মার্চ) অথবা ভাদ্র (আগস্ট)",
            transplantAgeBn = "সরাসরি লাইনে বপন",
            harvestTimeBn = "৬৫ - ৭৫ দিন পর (২-৩ বারে তোলা)",
            seedRatePerDecimalBn = "১০০ - ১২০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৫ - ৬ কেজি (শতক প্রতি)",
            landPrepTipsBn = "মুগ ডাল চাষের পর গাছের অবশিষ্টাংশ মাটিতে মিশিয়ে দিলে জমির জৈব সার বৃদ্ধি পায়।",
            specialAdviceBn = "হলুদ মোজাইক রোগ দমনে সাদা মাছি বাহক পোকা নিয়ন্ত্রণ করুন।"
        ),
        CropCalendarItem(
            id = "crop_blackgram",
            cropNameBn = "মাষকলাই ডাল",
            cropNameEn = "Blackgram",
            categoryBn = "ডাল ও তৈলবীজ",
            seasonBn = "খরিপ-২ (নাবি বর্ষা)",
            sowingTimeBn = "ভাদ্র - আশ্বিন (আগস্ট - সেপ্টেম্বর)",
            transplantAgeBn = "সরাসরি ছিটিয়ে বপন",
            harvestTimeBn = "কার্তিক - অগ্রহায়ণ (৭০ - ৮০ দিন পর)",
            seedRatePerDecimalBn = "১৩০ - ১৫০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৪ - ৬ কেজি (শতক প্রতি)",
            landPrepTipsBn = "বন্যা বা বৃষ্টির পানি নেমে যাওয়ার পর পলি পড়া মাটিতে বিনা চাষেই চমৎকার ফলে।",
            specialAdviceBn = "কম খরচে ও কম পরিশ্রমে সর্বাধিক লাভজনক ডাল ফসল।"
        ),
        CropCalendarItem(
            id = "crop_grasspea",
            cropNameBn = "খেসারি ডাল",
            cropNameEn = "Grasspea",
            categoryBn = "ডাল ও তৈলবীজ",
            seasonBn = "রবি (শীত)",
            sowingTimeBn = "কার্তিক - অগ্রহায়ণ (অক্টোবর শেষ - নভেম্বর)",
            transplantAgeBn = "আমন ধানের দাঁড়ানো জমিতে রিলে হিসেবে ছিটানো",
            harvestTimeBn = "ফাল্গুন - চৈত্র (১১০ - ১২০ দিন পর)",
            seedRatePerDecimalBn = "১৬০ - ১৮০ গ্রাম (শতক প্রতি)",
            estimatedYieldBn = "৬ - ৮ কেজি (শতক প্রতি)",
            landPrepTipsBn = "আমন ধান কাটার ১৫-২০ দিন আগে মাটি নরম বা স্যাঁতসেঁতে থাকা অবস্থায় বীজ ছিটিয়ে দিতে হয়।",
            specialAdviceBn = "অতিরিক্ত কোনো সেচ বা সারের প্রয়োজন হয় না, গবাদিপশুর জন্য চমৎকার কাঁচা ঘাসও মেলে।"
        )
    )

    // -------------------------------------------------------------
    // 2. CROP PESTS & DISEASES DATA
    // -------------------------------------------------------------
    val cropDiseaseList: List<CropDiseaseItem> = listOf(
        CropDiseaseItem(
            id = "dis_rice_blast",
            cropNameBn = "ধান",
            cropNameEn = "Rice",
            diseaseNameBn = "ব্লাস্ট রোগ (পাতা, গিট ও শীষ ব্লাস্ট)",
            diseaseNameEn = "Rice Blast (Pyricularia oryzae)",
            pathogenTypeBn = "ছত্রাকঘটিত",
            severityLevelBn = "উচ্চ ঝুঁকিপূর্ণ",
            symptomsBn = "পাতায় চোখের মতো বা ডিম্বাকৃতি দাগ পড়ে যার কেন্দ্র ধূসর ও কিনারা বাদামি। শীষের গোড়ায় আক্রমণ হলে পুরো শীষ শুকিয়ে সাদা চিটা হয়ে যায়।",
            organicControlBn = "ট্রাইকোডার্মা দিয়ে বীজ শোধন করুন। অতিরিক্ত ইউরিয়া সার দেওয়া বন্ধ রাখুন এবং জমিতে পটাশ সারের মাত্রা বাড়ান।",
            chemicalRemedyBn = "ট্রাইসাইক্লাজল গ্রুপের ছত্রাকনাশক (যেমন: ট্রুপার/দিফা) প্রতি লিটার পানিতে ০.৭৫ গ্রাম অথবা এ্যাজোক্সিস্ট্রবিন + ডাইফেনোকোনাজল (যেমন: এ্যামিস্টার টপ) ১ মিলি/লিটার হারে বিকেলে স্প্রে করুন।",
            preventiveCareBn = "রোগমুক্ত বীজ ব্যবহার এবং কার্বেনডাজিম (অটোস্টিন) দিয়ে ২ গ্রাম প্রতি কেজি হারে বীজ শোধন করে বপন করুন।"
        ),
        CropDiseaseItem(
            id = "dis_rice_bph",
            cropNameBn = "ধান",
            cropNameEn = "Rice",
            diseaseNameBn = "বাদামি গাছফড়িং / কারেন্ট পোকা (BPH)",
            diseaseNameEn = "Brown Plant Hopper",
            pathogenTypeBn = "কীটপতঙ্গ / পোকা",
            severityLevelBn = "উচ্চ ঝুঁকিপূর্ণ",
            symptomsBn = "ধান গাছের গোড়ায় বসে রস চুষে খায়। আক্রান্ত ধানক্ষেতের নির্দিষ্ট বৃত্তাকার এলাকা হঠাৎ পুড়ে যাওয়ার মতো হলুদ ও খড়ের রঙ হয়ে বসে যায় (Hoper Burn)।",
            organicControlBn = "ধান রোপণের সময় ১০-১২ সারি পরপর ১টি ফাঁকা লাইন (আলোক পথ) রাখুন। ক্ষেতের পানি শুকিয়ে ২-৩ দিন শুকনা রাখুন।",
            chemicalRemedyBn = "পাইমেট্রোজিন গ্রুপের কীটনাশক (যেমন: প্লেনাম/চেস) প্রতি লিটার পানিতে ০.৫ গ্রাম অথবা পাইরিপ্রক্সিফেন + ডায়াফেনথিউরন গাছের গোড়া লক্ষ্য করে স্প্রে করুন।",
            preventiveCareBn = "ইউরিয়া সারের অতিরিক্ত ব্যবহার পরিহার করুন এবং জমিতে আলোর ফাঁদ স্থাপন করুন।"
        ),
        CropDiseaseItem(
            id = "dis_potato_late_blight",
            cropNameBn = "গোল আলু",
            cropNameEn = "Potato",
            diseaseNameBn = "আলুর নাবি ধসা / লেইট ব্লাইট",
            diseaseNameEn = "Late Blight of Potato",
            pathogenTypeBn = "ছত্রাকঘটিত",
            severityLevelBn = "উচ্চ ঝুঁকিপূর্ণ",
            symptomsBn = "মেঘলা ও কুয়াশাচ্ছন্ন আবহাওয়ায় পাতার কিনারায় বা আগায় ভেজা বাদামি-কালো দাগ পড়ে। ভোরে পাতার নিচে সাদা তুলার মতো ছত্রাকের স্তর দেখা যায় এবং দ্রুত পুরো গাছ পচে পচা গন্ধ বের হয়।",
            organicControlBn = "আক্রান্ত গাছ তুলে পুড়িয়ে ফেলুন। কুয়াশা ও মেঘলা আবহাওয়ায় জমিতে সেচ দেওয়া বন্ধ রাখুন।",
            chemicalRemedyBn = "লক্ষণ দেখা দিলে সঙ্গে সঙ্গে ডাইমেথোমর্ফ + ম্যানকোজেব (যেমন: এক্রোবেট এমজেড) ২ গ্রাম/লিটার অথবা সাইমোক্সানিল + ম্যানকোজেব (কার্জেট) ২ গ্রাম/লিটার হারে ৫-৭ দিন পরপর স্প্রে করুন।",
            preventiveCareBn = "আবহাওয়া মেঘলা হলে রোগ আসার আগেই ম্যানকোজেব (ডাইথেন এম-৪৫) প্রতি লিটার পানিতে ২ গ্রাম হারে প্রতিরক্ষামূলক স্প্রে করে রাখুন।"
        ),
        CropDiseaseItem(
            id = "dis_brinjal_borer",
            cropNameBn = "বেগুন",
            cropNameEn = "Brinjal",
            diseaseNameBn = "ডগা ও ফল ছিদ্রকারী পোকা",
            diseaseNameEn = "Shoot and Fruit Borer",
            pathogenTypeBn = "কীটপতঙ্গ / পোকা",
            severityLevelBn = "উচ্চ ঝুঁকিপূর্ণ",
            symptomsBn = "পোকার কীড়া কচি ডগার ভেতর ঢুকে কুড়ে কুড়ে খায়, ফলে ডগা ঢলে পড়ে শুকিয়ে যায়। ফলের গায়ে ফুটো করে মল বের করে রাখে এবং ফল খাওয়ার অনুপযোগী হয়।",
            organicControlBn = "ক্ষেতে সেক্স ফেরোমোন ফাঁদ পাতুন (শতকে ১টি)। শুকিয়ে যাওয়া আক্রান্ত ডগা ও ফল হাত দিয়ে কেটে ধ্বংস করুন।",
            chemicalRemedyBn = "এমামেকটিন বেনজোয়েট (যেমন: প্রোক্লেইম) ১ গ্রাম/লিটার অথবা স্পাইনোস্যাড (ট্রেসার) ০.৪ মিলি/লিটার পানিতে মিশিয়ে বিকেলে স্প্রে করুন।",
            preventiveCareBn = "পরপর জমিতে বেগুন চাষ না করে শস্য পর্যায় অবলম্বন করুন এবং নিমতেল প্রতি লিটার পানিতে ৫ মিলি হারে নিয়মিত স্প্রে করুন।"
        ),
        CropDiseaseItem(
            id = "dis_tomato_leaf_curl",
            cropNameBn = "টমেটো",
            cropNameEn = "Tomato",
            diseaseNameBn = "টমেটোর পাতা কোঁকড়ানো রোগ",
            diseaseNameEn = "Tomato Leaf Curl Virus",
            pathogenTypeBn = "ভাইরাসঘটিত (বাহক: সাদা মাছি)",
            severityLevelBn = "মাঝারি",
            symptomsBn = "গাছের কচি পাতাগুলো খর্বাকৃতি হয়ে ওপরের দিকে নৌকার মতো কুঁকড়ে যায়। গাছ খাটো হয়ে যায়, ফুল ঝরে পড়ে এবং ফল ধরে না।",
            organicControlBn = "হলুদ রঙের আঠালো ফাঁদ (Yellow Sticky Trap) ব্যবহার করে রোগ বিস্তারকারী সাদা মাছি ধ্বংস করুন। আক্রান্ত গাছ শুরুতেই উপড়ে মাটিতে পুঁতে ফেলুন।",
            chemicalRemedyBn = "ভাইরাসের কোনো সরাসরি ওষুধ নেই, বাহক সাদা মাছি দমনে ইমিডাক্লোপ্রিড (কনফিডোর/টিডো) ০.৫ মিলি/লিটার অথবা অ্যাসিটামিপ্রিড ১ গ্রাম/লিটার স্প্রে করুন।",
            preventiveCareBn = "নার্সারিতে বীজতলায় মশারির নেট ব্যবহার করে চারা উৎপাদন করুন যাতে সাদা মাছি চারায় বসতে না পারে।"
        ),
        CropDiseaseItem(
            id = "dis_chilli_anthracnose",
            cropNameBn = "মরিচ",
            cropNameEn = "Chilli",
            diseaseNameBn = "মরিচের ফল পচা ও ডাই ব্যাক রোগ",
            diseaseNameEn = "Anthracnose / Dieback",
            pathogenTypeBn = "ছত্রাকঘটিত",
            severityLevelBn = "মাঝারি",
            symptomsBn = "ডালপালা ওপর থেকে নিচের দিকে শুকিয়ে কাঠির মতো মারা যেতে থাকে (Die-back)। পাকা মরিচের গায়ে গোল গোল বসে যাওয়া দাগ পড়ে এবং ফল শুকিয়ে ঝরে যায়।",
            organicControlBn = "ট্রাইকোডার্মা দিয়ে বীজ শোধন করুন এবং শুকনো ডালপালা কেটে পরিষ্কার রাখুন।",
            chemicalRemedyBn = "কার্বেনডাজিম (অটোস্টিন) প্রতি লিটার পানিতে ২ গ্রাম অথবা অ্যাজোক্সিস্ট্রবিন + ডাইফেনোকোনাজল (এ্যামিস্টার টপ) ১ মিলি/লিটার হারে স্প্রে করুন।",
            preventiveCareBn = "সুস্থ ও পরিপক্ক ফল থেকে বীজ সংগ্রহ করুন এবং জমিতে অতিরিক্ত সেচ বা পানি জমা রোধ করুন।"
        )
    )

    // -------------------------------------------------------------
    // 3. FERTILIZERS & PESTICIDES HANDBOOK
    // -------------------------------------------------------------
    val fertilizerList: List<FertilizerItem> = listOf(
        FertilizerItem(
            id = "fert_urea",
            nameBn = "ইউরিয়া (Urea)",
            nameEn = "Urea Fertilizer",
            nutrientSymbol = "N (নাইট্রোজেন ৪৬%)",
            primaryFunctionBn = "গাছের পাতা ও ডালপালা দ্রুত বৃদ্ধি করে, গাছকে গাঢ় সবুজ রাখে এবং সার্বিক বাড়বাড়ন্তে শক্তি জোগায়।",
            deficiencySymptomsBn = "পুরনো পাতাগুলো প্রথমে ফ্যাকাশে হলুদ বর্ণ ধারণ করে, গাছের বৃদ্ধি স্থবির হয়ে যায় এবং ফলন কমে যায়।",
            excessHarmBn = "গাছ অতিরিক্ত লকলকে হয়ে হেলে পড়ে, রোগবালাই ও পোকামাকড়ের আক্রমণ মারাত্মক বৃদ্ধি পায় এবং ফলন আসতে দেরি হয়।",
            applicationMethodBn = "একবারে সব না দিয়ে ২ থেকে ৩ কিস্তিতে উপরিপ্রয়োগ করতে হয়। ভেজা নরম মাটিতে বিকেলে ছিটানো উত্তম।",
            dosePerDecimalBn = "ফসলের ধরনভেদে শতকে ৬০০ গ্রাম থেকে ১০০০ গ্রাম (৩ কিস্তিতে)"
        ),
        FertilizerItem(
            id = "fert_tsp_dap",
            nameBn = "টিএসপি / ডিএপি (TSP / DAP)",
            nameEn = "Triple Super Phosphate / DAP",
            nutrientSymbol = "P (ফসফরাস ৪৬%)",
            primaryFunctionBn = "গাছের শিকড় মজবুত ও বিস্তৃত করে, দ্রুত ফুল ও ফল আনতে সাহায্য করে এবং কাণ্ডকে শক্ত ও মজবুত রাখে।",
            deficiencySymptomsBn = "শিকড় দুর্বল হয়, পাতার নিচের দিকে বা বোঁটায় বেগুনি/নীলচে আভা দেখা যায় এবং ফুল-ফল আসতে অনেক দেরি হয়।",
            excessHarmBn = "মাটির দস্তা (Zinc) ও আয়রন গ্রহণ ক্ষমতা বাধাগ্রস্ত হয়।",
            applicationMethodBn = "জমি তৈরির শেষ চাষে পুরোটা মাটির সাথে মিশিয়ে দিতে হয় কারণ ফসফরাস মাটিতে সহজে চলাচল করতে পারে না।",
            dosePerDecimalBn = "শতকে ৪০০ গ্রাম থেকে ৬০০ গ্রাম (জমি তৈরিতে)"
        ),
        FertilizerItem(
            id = "fert_mop",
            nameBn = "মিউরেট অব পটাশ (MOP / পটাশ)",
            nameEn = "Muriate of Potash",
            nutrientSymbol = "K (পটাশিয়াম ৬০%)",
            primaryFunctionBn = "রোগবালাই ও খরা সহনশীলতা বাড়ায়, ফল ও দানার আকার পুষ্ট করে, ফলের মিষ্টি ও ওজন বাড়ায় এবং গাছ হেলে পড়া রোধ করে।",
            deficiencySymptomsBn = "পুরনো পাতার অগ্রভাগ ও কিনারা পুড়ে যাওয়ার মতো ঝলসে যায় এবং ফলের আকার ছোট ও অপুষ্ট হয়।",
            excessHarmBn = "গাছে ক্যালসিয়াম ও ম্যাগনেসিয়ামের ঘাটতি তৈরি হতে পারে।",
            applicationMethodBn = "অর্ধেক জমি তৈরিতে এবং বাকি অর্ধেক ফুল ও ফল আসার সময় উপরিপ্রয়োগ করা লাভজনক।",
            dosePerDecimalBn = "শতকে ৪০০ গ্রাম থেকে ৭০০ গ্রাম"
        ),
        FertilizerItem(
            id = "fert_gypsum",
            nameBn = "জিপসাম (Gypsum)",
            nameEn = "Gypsum",
            nutrientSymbol = "S (সালফার / গন্ধক ১৮%)",
            primaryFunctionBn = "তেলজাতীয় ফসলে তেলের পরিমাণ বাড়ায়, প্রোটিন তৈরিতে সাহায্য করে এবং মাটির অম্লতা-ক্ষারত্বের ভারসাম্য রক্ষা করে।",
            deficiencySymptomsBn = "কচি ও নতুন পাতাগুলো হলুদাভ হয়ে যায় (ইউরিয়ার উল্টো, ইউরিয়ায় পুরনো পাতা হলুদ হয়)।",
            excessHarmBn = "মাটি অতিরিক্ত অম্লীয় হতে পারে।",
            applicationMethodBn = "জমি তৈরির শেষ চাষে মাটির সাথে ভালোভাবে মিশিয়ে দিতে হবে।",
            dosePerDecimalBn = "শতকে ৩০০ গ্রাম থেকে ৪০০ গ্রাম"
        ),
        FertilizerItem(
            id = "fert_zinc",
            nameBn = "জিংক সালফেট (দস্তা)",
            nameEn = "Zinc Sulphate",
            nutrientSymbol = "Zn (দস্তা ৩৬% বা মনোহাইড্রেট)",
            primaryFunctionBn = "গাছের হরমোন তৈরি, ক্লোরোফিল গঠন এবং ধানের খয়রা রোগ প্রতিরোধে অপরিহার্য।",
            deficiencySymptomsBn = "ধানের পাতায় মরিচা পড়ার মতো লালচে-বাদামি ছোপ ছোপ দাগ পড়ে এবং গাছের বাড়ন্ত থমকে যায়।",
            excessHarmBn = "মাটির স্বাভাবিক উর্বরতা ও অন্যান্য ক্ষুদ্র উপাদান গ্রহণ বাধা পায়।",
            applicationMethodBn = "টিএসপি বা ডিএপির সাথে সরাসরি না মিশিয়ে আলাদাভাবে জমি তৈরিতে অথবা পাতায় চিলেটেড জিংক হিসেবে স্প্রে করা যায়।",
            dosePerDecimalBn = "শতকে ৫০ গ্রাম থেকে ১০০ গ্রাম"
        ),
        FertilizerItem(
            id = "fert_boron",
            nameBn = "বোরন / বোরিক এসিড (Boron)",
            nameEn = "Solubor Boron",
            nutrientSymbol = "B (বোরন ২০%)",
            primaryFunctionBn = "ফুল ও ফলের পরাগায়নে সাহায্য করে, ফুল ঝরে পড়া বন্ধ করে এবং ফল ফেটে যাওয়া রোধ করে।",
            deficiencySymptomsBn = "ফল ফেটে যায়, ফলের বিকৃতি ঘটে, ফুল শুকিয়ে ঝরে যায় এবং কুঁড়ির ডগা মরে যায়।",
            excessHarmBn = "পাতার কিনারা পুড়ে যায় এবং বিষাক্ততা দেখা দেয়।",
            applicationMethodBn = "ফুল আসার আগে ও পরে প্রতি লিটার পানিতে ১.৫ থেকে ২ গ্রাম সলুবোর বোরন স্প্রে করা সবচেয়ে কার্যকর।",
            dosePerDecimalBn = "শতকে ৩০ গ্রাম থেকে ৫০ গ্রাম (জমি তৈরিতে) অথবা স্প্রে"
        )
    )

    val pesticideGroupList: List<PesticideGroupItem> = listOf(
        PesticideGroupItem(
            id = "pest_mancozeb",
            groupName = "Mancozeb (ম্যানকোজেব)",
            categoryBn = "ছত্রাকনাশক",
            popularBrandsBn = "ডাইথেন এম-৪৫, ইন্ডোফিল, পেনকোজেব",
            targetPestBn = "আলুর আর্লি ও লেইট ব্লাইট, সবজির পাতার দাগ রোগ, ডাউনি মিলডিউ।",
            dilutionDoseBn = "প্রতি লিটার পানিতে ২ গ্রাম (১৬ লিটার ড্রামে ৩২ গ্রাম)",
            safetyWaitingPeriodBn = "৭ - ১০ দিন",
            precautionsBn = "এটি স্পর্শক ছত্রাকনাশক, তাই কুয়াশা বা বৃষ্টির আগে পাতার ওপর-নিচ ভালো করে ভিজিয়ে স্প্রে করতে হবে।"
        ),
        PesticideGroupItem(
            id = "pest_carbendazim",
            groupName = "Carbendazim (কার্বেনডাজিম)",
            categoryBn = "ছত্রাকনাশক",
            popularBrandsBn = "অটোস্টিন, নোইন, গোল্ডাজিম",
            targetPestBn = "বীজবাহিত ছত্রাক, ডাল শস্যের গোড়া পচা, অ্যানথ্রাকনোজ ও ডাই ব্যাক রোগ।",
            dilutionDoseBn = "প্রতি লিটার পানিতে ১.৫ - ২ গ্রাম",
            safetyWaitingPeriodBn = "১০ - ১৪ দিন",
            precautionsBn = "বীজ শোধনের জন্য কেজি প্রতি ২ গ্রাম কার্বেনডাজিম মিশিয়ে ১২ ঘণ্টা ছায়ায় শুকিয়ে নিলে চারা মৃত্যু রোধ হয়।"
        ),
        PesticideGroupItem(
            id = "pest_imidacloprid",
            groupName = "Imidacloprid (ইমিডাক্লোপ্রিড)",
            categoryBn = "কীটনাশক",
            popularBrandsBn = "কনফিডোর, টিডো, ইমিটাফ, অটোমিডা",
            targetPestBn = "জাবপোকা, সাদা মাছি, থ্রিপস, ঘাসফড়িং এবং অন্যান্য চোষক পোকা।",
            dilutionDoseBn = "প্রতি লিটার পানিতে ০.৫ মিলি (১৬ লিটার ড্রামে ৮ মিলি)",
            safetyWaitingPeriodBn = "১৪ - ২১ দিন",
            precautionsBn = "ফুল ফোটার সময় স্প্রে করবেন না। এটি অন্তর্বাহী বিষ, পাতার রস চুষে খাওয়া পোকায় খুব দ্রুত কাজ করে।"
        ),
        PesticideGroupItem(
            id = "pest_chlorpyrifos_cyper",
            groupName = "Chlorpyrifos + Cypermethrin",
            categoryBn = "কীটনাশক",
            popularBrandsBn = "নাইট্রো, রেলোথ্রিন, ক্লাসিক প্লাস",
            targetPestBn = "মাজরা পোকা, লেদা পোকা, ফল ছিদ্রকারী ও মাটির নিচের কাটুই পোকা।",
            dilutionDoseBn = "প্রতি লিটার পানিতে ১.৫ - ২ মিলি",
            safetyWaitingPeriodBn = "১৪ দিন",
            precautionsBn = "তীব্র গন্ধযুক্ত ও শক্তিশালী বিষ। স্প্রে করার সময় মুখে মাস্ক ও চোখে গগলস ব্যবহার করা বাধ্যতামূলক।"
        ),
        PesticideGroupItem(
            id = "pest_abamectin",
            groupName = "Abamectin (এবামেকটিন)",
            categoryBn = "মাকড়নাশক",
            popularBrandsBn = "ভার্টিমেক, বায়োম্যাক্স, মাকড়নাশ",
            targetPestBn = "মরিচ, বেগুন, পেঁপে ও চায়ের লাল মাকড় ও হলুদ মাকড়।",
            dilutionDoseBn = "প্রতি লিটার পানিতে ১.২ মিলি",
            safetyWaitingPeriodBn = "৭ দিন",
            precautionsBn = "মাকড় পাতার উল্টো পিঠে থাকে, তাই নোজল নিচের দিকে রেখে পাতার নিচের অংশে স্প্রে লাগাতে হবে।"
        )
    )

    // -------------------------------------------------------------
    // 4. LIVESTOCK DISEASES & PRIMARY CARE
    // -------------------------------------------------------------
    val livestockDiseaseList: List<LivestockDiseaseItem> = listOf(
        LivestockDiseaseItem(
            id = "dis_fmd",
            diseaseNameBn = "ক্ষুরারোগ (FMD / বাদল রোগ)",
            diseaseNameEn = "Foot and Mouth Disease",
            affectedAnimalBn = "গরু, মহিষ, ছাগল ও ভেড়া",
            causeBn = "ভাইরাসঘটিত (Picornaviridae)",
            severityBn = "অতি মারাত্মক / জরুরি",
            symptomsBn = "১০৪-১০৬ ডিগ্রি তীব্র জ্বর। মুখ থেকে দড়ির মতো ঘন লালা ঝরে। জিহ্বা, মাড়ি ও পায়ের ক্ষুরের মাঝে ফোসকা পড়ে ও ঘা হয়। পশুর খুঁড়িয়ে হাঁটা ও জাবর কাটা বন্ধ হয়ে যায়।",
            immediateCareBn = "আক্রান্ত পশুকে আলাদা রাখুন। মুখ ফিটকিরি বা পটাশ মেশানো কুসুম গরম পানি দিয়ে এবং ক্ষুর পটাশ মিশ্রিত পানি দিয়ে দিনে ২-৩ বার ধুয়ে দিন। নরম জাউ বা ভাতের মাড় খেতে দিন।",
            medicalTreatmentBn = "ভাইরাস প্রতিরোধে কোনো অ্যান্টিবায়োটিক নেই, তবে সেকেন্ডারি ব্যাকটেরিয়াল ইনফেকশন ঠেকাতে রেজিস্টার্ড চিকিৎসকের পরামর্শে ব্রড-স্পেকট্রাম অ্যান্টিবায়োটিক ও প্যারাসিটামল প্রয়োগ করতে হয়।",
            preventionBn = "৪ মাস বয়সের পর নিয়মিত ৬ মাস পর পর ক্ষুরারোগের এফএমডি (FMD) ভ্যাকসিন প্রয়োগ করুন।"
        ),
        LivestockDiseaseItem(
            id = "dis_anthrax",
            diseaseNameBn = "তড়কা রোগ (Anthrax)",
            diseaseNameEn = "Anthrax (Bacillus anthracis)",
            affectedAnimalBn = "সকল গবাদিপশু (মানুষেও ছড়ায় - জুরোটিক)",
            causeBn = "ব্যাকটেরিয়াঘটিত (স্পোর তৈরি করে)",
            severityBn = "অতি মারাত্মক / জরুরি",
            symptomsBn = "হঠাৎ তীব্র জ্বর (১০৬-১০৭ ডিগ্রি), পশুর কাঁপুনি ও মাটিতে পড়ে যাওয়া। নাক, মুখ, মলদ্বার ও যোনিপথ দিয়ে আলকাতরার মতো জমাট না বাঁধা কালো রক্ত ক্ষরণ এবং অল্প সময়ের মধ্যে মৃত্যু।",
            immediateCareBn = "সতর্কতা: মৃতদেহ কখনোই কাটবেন না বা চামড়া ছাড়াবেন না! এতে স্পোর বাতাসে ও মাটিতে ছড়িয়ে শত বছর বেঁচে থাকে। মৃত পশুকে চুনের গুঁড়া ছিটিয়ে মাটির অন্তত ৬ ফুট নিচে পুঁতে ফেলুন।",
            medicalTreatmentBn = "রোগের প্রাথমিক পর্যায়ে ধরা পড়লে উচ্চমাত্রার পেনিসিলিন গ্রুপের অ্যান্টিবায়োটিক জরুরি ভিত্তিতে চিকিৎসকের অধীনে দিতে হবে।",
            preventionBn = "প্রতি বছর বর্ষার আগে স্থানীয় প্রাণিসম্পদ দপ্তর থেকে তড়কা ভ্যাকসিন প্রয়োগ বাধ্যতামূলক।"
        ),
        LivestockDiseaseItem(
            id = "dis_black_quarter",
            diseaseNameBn = "বাদলা রোগ (Black Quarter / BQ)",
            diseaseNameEn = "Black Quarter (Clostridium chauvoei)",
            affectedAnimalBn = "প্রধানত ১-৩ বছর বয়সের পুষ্ট গরু ও ষাঁড়",
            causeBn = "ব্যাকটেরিয়াঘটিত",
            severityBn = "অতি মারাত্মক / জরুরি",
            symptomsBn = "পেছনের বা সামনের রানের মাংসল অংশে গরম ও বেদনাদায়ক ফোলা। ফোলার ওপর চাপ দিলে মটমট বা চড়চড় শব্দ (Gas crackling) করে। তীব্র জ্বর ও পশু মারাত্মক খোঁড়ায়।",
            immediateCareBn = "আক্রান্ত স্থানে বরফ বা ঠান্ডা সেক দেওয়া এবং দ্রুত রেজিস্টার্ড ডাক্তারের ব্যবস্থা করা। আক্রান্ত স্থানের চামড়া কেটে কালো রক্ত ও গ্যাস নির্গমনে সাহায্য করতে ডাক্তার পরামর্শ দিতে পারেন।",
            medicalTreatmentBn = "অক্সিট্রেট্রাসাইক্লিন বা পেনিসিলিন ইনজেকশন প্রাথমিক অবস্থায় প্রয়োগ করলে জীবন রক্ষা পেতে পারে।",
            preventionBn = "বছরে একবার বাদলা ভ্যাকসিন নিয়মিত দিন।"
        ),
        LivestockDiseaseItem(
            id = "dis_lsd",
            diseaseNameBn = "লাম্পি স্কিন ডিজিজ (LSD)",
            diseaseNameEn = "Lumpy Skin Disease",
            affectedAnimalBn = "গরু ও মহিষ (মশা-মাছির মাধ্যমে বিস্তার)",
            causeBn = "ক্যাপ্রিপক্স ভাইরাস (Capripoxvirus)",
            severityBn = "মাঝারি থেকে মারাত্মক",
            symptomsBn = "উচ্চ জ্বর, নাক-চোখ দিয়ে জল পড়া। সারা শরীরের চামড়ায় গোল গোল শক্ত গুটি বা ফোড়া ওঠা যা পরে ফেটে ঘা হয়ে পুঁজ পড়তে পারে। পা ও অণ্ডকোষ ফুলে যেতে পারে।",
            immediateCareBn = "মশা-মাছি প্রতিরোধে মশারি টানান। ক্ষতে আয়োডিন বা পভিডন লোশন লাগান। নিমপাতা সিদ্ধ পানি দিয়ে শরীর মুছে দিন। গ্লুকোজ ও স্যালাইন পানি পান করান।",
            medicalTreatmentBn = "অ্যান্টিহিস্টামিনিক, ব্যথানাশক এবং ক্ষত শুকানোর জন্য অ্যান্টিবায়োটিক চিকিৎসকের পরামর্শে ব্যবহার করতে হবে।",
            preventionBn = "সুস্থ গরুকে প্রতি বছর গোট পক্স বা এলএসডি ভ্যাকসিন প্রদান করুন এবং খামার মশা-মাছি মুক্ত রাখুন।"
        ),
        LivestockDiseaseItem(
            id = "dis_mastitis",
            diseaseNameBn = "ওলান ফোলা / ওলান প্রদাহ (Mastitis)",
            diseaseNameEn = "Bovine Mastitis",
            affectedAnimalBn = "দুগ্ধবতী গাভী",
            causeBn = "ব্যাকটেরিয়াল সংক্রমণ",
            severityBn = "মাঝারি",
            symptomsBn = "ওলান গরম হয়ে লালচে ফোলা এবং গাভী দুধ দোয়ানোর সময় স্পর্শ করতে দেয় না। দুধে ছানা কাটার মতো পুঁজ বা রক্ত মিশ্রিত পাতলা পানি বের হয়।",
            immediateCareBn = "ওলানের ভেতরের সম্পূর্ণ দুধ ও পুঁজ ঘন ঘন দুয়ে ফেলে পরিষ্কার করুন। ওলানে বরফের সেক দিন। দোয়ানোর পর প্রতিটি বাঁটে পভিডন আয়োডিন ডিপ করুন।",
            medicalTreatmentBn = "বাঁটের ভেতরে ইন্ট্রাম্যামারি অ্যান্টিবায়োটিক টিউব পুশ করা এবং মাংসপেশিতে ব্রড স্পেকট্রাম অ্যান্টিবায়োটিক ইনজেকশন দিতে হবে।",
            preventionBn = "গাভী দোয়ানোর অন্তত ৩০ মিনিট গাভীকে শুতে দেবেন না (এসময় খাবার দিন যেন দাঁড়িয়ে থাকে)। মেঝে শুকনো ও জীবাণুমুক্ত রাখুন।"
        ),
        LivestockDiseaseItem(
            id = "dis_bloat",
            diseaseNameBn = "পেট ফাঁপা (Tympany / Bloat)",
            diseaseNameEn = "Bloat / Ruminal Tympany",
            affectedAnimalBn = "গরু, ছাগল ও মহিষ",
            causeBn = "পরিপাকজনিত (অতিরিক্ত কাঁচা কচি ঘাস বা পচা খাবার খাওয়া)",
            severityBn = "জরুরি",
            symptomsBn = "পেটের বাম পাশ ঢোলের মতো ফুলে ওঠা, মুখে শ্বাসকষ্ট, অস্থিরতা ও অনবরত জিহ্বা বের করা। গুরুতর হলে পশু মাটিতে শুয়ে পড়ে।",
            immediateCareBn = "পশুর মুখের ভেতরে আড়াআড়ি কাঠ বা লাঠি বেঁধে মুখ খোলা রাখুন যাতে বাতাস বের হতে পারে। আদা, জিরা ও খাবার সোডা গুলে মুখে খাওয়ান।",
            medicalTreatmentBn = "অ্যান্টি-ব্লট ড্রপ (যেমন: সিম্প্রা/ব্লটোরিল) ১০০-২০০ মিলি দ্রুত খাওয়ান। অতি জরুরি ক্ষেত্রে ট্রোকার-ক্যানুলা দিয়ে বাম পেট পাংচার করে গ্যাস বের করতে হবে।",
            preventionBn = "শিশিরভেজা কচি ঘাস সরাসরি খেতে দেবেন না, রোদে কিছুটা শুকিয়ে খড়ের সাথে মিশিয়ে খেতে দিন।"
        )
    )

    // -------------------------------------------------------------
    // 5. LIVESTOCK FEED RECIPES & SILAGE GUIDE
    // -------------------------------------------------------------
    val feedRecipeList: List<LivestockFeedRecipe> = listOf(
        LivestockFeedRecipe(
            id = "feed_dairy_cow",
            titleBn = "দুগ্ধবতী গাভীর সুষম দানাদার খাদ্য (১০০ কেজি অনুপাত)",
            titleEn = "Milking Cow Concentrated Feed (100 kg)",
            targetAnimalBn = "দুগ্ধবতী গাভী",
            batchWeightBn = "১০০ কেজি সুষম মিশ্রণ",
            ingredientListBn = listOf(
                "গম বা গমের ভুসি" to "৩৫ কেজি",
                "ভুট্টা ভাঙা (দানাদার)" to "২৫ কেজি",
                "সরিষার খৈল / সয়াবিন মিল" to "২০ কেজি",
                "চালের কুঁড়া (অটো পালিশ)" to "১৬ কেজি",
                "ডাইক্যালসিয়াম ফসফেট (DCP)" to "১.৫ কেজি",
                "খাবার লবণ" to "১ কেজি",
                "ভিটামিন-মিনারেল প্রিমিক্স" to "০.৫ কেজি (৫০০ গ্রাম)"
            ),
            dailyFeedingRuleBn = "গাভীর নিজের শরীরের জন্য ২ থেকে ২.৫ কেজি এবং প্রতি আড়াই থেকে তিন লিটার দুধ উৎপাদনের জন্য অতিরিক্ত ১ কেজি দানাদার খাদ্য দিন। সাথে প্রচুর কাঁচা ঘাস ও খড় দিন।",
            preparationGuideBn = "সব শুকনো উপাদান শুকনো পরিষ্কার ফ্লোরে কোদাল দিয়ে ৩-৪ বার ভালো করে মিশিয়ে মুখবন্ধ প্লাস্টিক ড্রামে রাখুন। খাওয়ানোর আগে খৈল কিছুক্ষণ পানিতে ভিজিয়ে রেখে তারপর অন্যান্য উপাদান মেশালে হজম ভালো হয়।",
            specialAdviceBn = "গাভীকে দিনে অন্তত ৩০-৪০ লিটার সুপেয় বিশুদ্ধ পানি পান করাতে হবে, কারণ দুধের ৮৭% উপাদানই পানি।"
        ),
        LivestockFeedRecipe(
            id = "feed_beef_fattening",
            titleBn = "ষাঁড় মোটাতাজাকরণ খাদ্য মিশ্রণ (১০০ কেজি)",
            titleEn = "Beef Fattening Ration (100 kg)",
            targetAnimalBn = "মোটাতাজাকরণ ষাঁড়",
            batchWeightBn = "১০০ কেজি মোটাতাজাকরণ মিশ্রণ",
            ingredientListBn = listOf(
                "ভুট্টা ভাঙা" to "৩৫ কেজি",
                "গমের ভুসি" to "২৫ কেজি",
                "সয়াবিন মিল / খৈল" to "২২ কেজি",
                "চালের অটো কুঁড়া" to "১৪ কেজি",
                "ডাইক্যালসিয়াম ফসফেট (DCP)" to "১.৫ কেজি",
                "খাবার লবণ" to "১.৫ কেজি",
                "টক্সিন বাইন্ডার ও প্রিমিক্স" to "১ কেজি"
            ),
            dailyFeedingRuleBn = "পশুর দৈহিক ওজনের ১.৫% থেকে ২% হারে দানাদার খাদ্য দিন (যেমন: ২০০ কেজি ওজনের ষাঁড়কে দিনে ৩ থেকে ৪ কেজি দানাদার খাদ্য, দুই বেলা ভাগ করে)।",
            preparationGuideBn = "খাদ্যের সাথে পরিমিত মাত্রায় ইউরিয়া মোলাসেস স্ট্র (UMS) খাওয়ালে মোটাতাজাকরণ দ্রুত ও লাভজনক হয়।",
            specialAdviceBn = "মোটাতাজাকরণ শুরু করার প্রথম দিনেই নিয়ম মেনে কৃমিনাশক ও লিভার টনিক দিতে হবে।"
        ),
        LivestockFeedRecipe(
            id = "feed_ums_making",
            titleBn = "ইউরিয়া মোলাসেস স্ট্র (UMS) তৈরির সহজ পদ্ধতি",
            titleEn = "Urea Molasses Straw (UMS) Guide",
            targetAnimalBn = "গরু ও মহিষের খড় পুষ্টিবর্ধন",
            batchWeightBn = "১০০ কেজি শুকনা খড়ের জন্য অনুপাত",
            ingredientListBn = listOf(
                "শুকনো ছোট কাটা খড়" to "১০০ কেজি",
                "চিটাগুড় (মোলাসেস)" to "২০ - ২৫ কেজি",
                "ইউরিয়া সার" to "৩ কেজি",
                "পরিষ্কার পানি" to "৪০ - ৫০ লিটার"
            ),
            dailyFeedingRuleBn = "প্রাপ্তবয়স্ক গরুকে দৈনিক ৪ থেকে ৬ কেজি UMS খাওয়ানো যায়। এটি সাধারণ খড়ের চেয়ে তিনগুণ পুষ্টিকর।",
            preparationGuideBn = "৪০ লিটার পানিতে ৩ কেজি ইউরিয়া ও ২৫ কেজি চিটাগুড় ভালোভাবে গুলে দ্রবণ বানান। এবার খড়ের ওপর স্তরে স্তরে সেই দ্রবণ সমানভাবে ছিটিয়ে পা দিয়ে চাপুন। ৮-১০ দিন মাটির নিচে বা পলিথিনে বায়ুরোধী করে রেখে দিন।",
            specialAdviceBn = "সতর্কতা: ইউরিয়ার মাত্রা কোনোভাবেই ৩ কেজির বেশি করবেন না এবং ৬ মাসের কম বয়সী বাছুরকে UMS খেতে দেবেন না।"
        ),
        LivestockFeedRecipe(
            id = "feed_silage_making",
            titleBn = "কাঁচা ঘাসের সাইলেজ (Silage) তৈরি ও সংরক্ষণ",
            titleEn = "Green Fodder Silage Making Guide",
            targetAnimalBn = "সকল গবাদিপশু (শুষ্ক মৌসুমের খাদ্য)",
            batchWeightBn = "১০০ কেজি সাইলেজের অনুপাত",
            ingredientListBn = listOf(
                "নেপিয়ার / ভুট্টা কাঁচা ঘাস (কুচানো)" to "১০০ কেজি (৬০-৭০% আর্দ্রতা)",
                "চিটাগুড় (মোলাসেস)" to "৩ - ৪ কেজি",
                "পানি" to "৪ - ৫ লিটার (চিটাগুড় পাতলা করার জন্য)"
            ),
            dailyFeedingRuleBn = "প্রতিটি বড় গরুকে দিনে ১০ থেকে ১৫ কেজি সাইলেজ খাওয়ানো যায়। এতে শীতকালে কাঁচা ঘাসের ঘাটতি থাকে না।",
            preparationGuideBn = "ঘাস কেটে ১-২ ইঞ্চি সাইজে কুচিয়ে ২-৩ ঘণ্টা রোদে পানি কিছুটা শুকান। চিটাগুড়ের মিশ্রণ ছিটিয়ে মোটা পলিথিন ব্যাগে বা সাইলো পিটে স্তরে স্তরে খুব শক্ত করে চেপে বাতাস বের করে বায়ুরোধীভাবে বেঁধে রাখুন।",
            specialAdviceBn = "৪০-৪৫ দিন পর মিষ্টি-টক গন্ধ বের হলে সাইলেজ তৈরি হবে। একবার মুখ খুললে প্রতিদিনের প্রয়োজনীয় অংশ নিয়ে আবার মুখ শক্ত করে বন্ধ করুন।"
        )
    )

    // -------------------------------------------------------------
    // 6. VACCINATION & DEWORMING SCHEDULE
    // -------------------------------------------------------------
    val vaccinationList: List<VaccinationScheduleItem> = listOf(
        VaccinationScheduleItem(
            id = "vac_cattle_fmd",
            animalTypeBn = "গরু ও মহিষ",
            vaccineNameBn = "ক্ষুরারোগের ভ্যাকসিন (FMD Vaccine)",
            targetDiseaseBn = "ক্ষুরারোগ (Foot and Mouth Disease)",
            firstDoseAgeBn = "৪ মাস বয়সে প্রথম ডোজ",
            boosterIntervalBn = "প্রথম ডোজের ১ মাস পর বুস্টার, অতঃপর প্রতি ৬ মাস পর পর নিয়মিত",
            routeBn = "চামড়ার নিচে (Subcutaneous - SC)",
            doseAmountBn = "৩ মিলি (ডাক্তারের নির্দেশ মতো)",
            precautionsBn = "৪ থেকে ৮ ডিগ্রি সেলসিয়াস রেফ্রিজারেটরে সংরক্ষণ করুন। অসুস্থ পশু বা অন্তঃসত্ত্বা পশুর শেষ মাসে ভ্যাকসিন দেবেন না।"
        ),
        VaccinationScheduleItem(
            id = "vac_cattle_anthrax",
            animalTypeBn = "গরু ও মহিষ",
            vaccineNameBn = "তড়কা ভ্যাকসিন (Anthrax Vaccine)",
            targetDiseaseBn = "তড়কা রোগ (Anthrax)",
            firstDoseAgeBn = "৬ মাস বয়সে প্রথম প্রয়োগ",
            boosterIntervalBn = "প্রতি বছর একবার (বর্ষা শুরু হওয়ার আগে মে-জুন মাসে)",
            routeBn = "চামড়ার নিচে (ঘাড়ের চামড়া টেনে)",
            doseAmountBn = "১ মিলি",
            precautionsBn = "অঞ্চলভিত্তিক প্রাদুর্ভাব থাকলে পুরো এলাকার সুস্থ পশুকে একসাথে ভ্যাকসিন দিতে হয়।"
        ),
        VaccinationScheduleItem(
            id = "vac_cattle_bq",
            animalTypeBn = "গরু ও মহিষ",
            vaccineNameBn = "বাদলা ভ্যাকসিন (Black Quarter Vaccine)",
            targetDiseaseBn = "বাদলা রোগ (BQ)",
            firstDoseAgeBn = "৬ মাস বয়সে প্রথম প্রয়োগ",
            boosterIntervalBn = "প্রতি বছর একবার নিয়মিত",
            routeBn = "চামড়ার নিচে",
            doseAmountBn = "৫ মিলি",
            precautionsBn = "সুস্থ ও সবল পশুকে ভ্যাকসিন দিন। ভ্যাকসিন দেওয়ার পূর্বে পশুকে কৃমিমুক্ত করা আবশ্যক।"
        ),
        VaccinationScheduleItem(
            id = "vac_cattle_lsd",
            animalTypeBn = "গরু ও মহিষ",
            vaccineNameBn = "গোট পক্স / লাম্পি স্কিন ভ্যাকসিন (LSD)",
            targetDiseaseBn = "লাম্পি স্কিন ডিজিজ (LSD)",
            firstDoseAgeBn = "৩-৪ মাস বয়সে",
            boosterIntervalBn = "প্রতি বছর একবার গরম বা বর্ষার শুরুতে",
            routeBn = "চামড়ার নিচে",
            doseAmountBn = "গরুর জন্য গোট পক্সের ডাবল বা নির্ধারিত ডোজ",
            precautionsBn = "ভ্যাকসিন ভায়াল খোলার ২ ঘণ্টার মধ্যে ব্যবহার শেষ করতে হবে।"
        ),
        VaccinationScheduleItem(
            id = "vac_goat_ppr",
            animalTypeBn = "ছাগল ও ভেড়া",
            vaccineNameBn = "পিপিআর ভ্যাকসিন (PPR Vaccine)",
            targetDiseaseBn = "পিপিআর রোগ (Peste des Petits Ruminants)",
            firstDoseAgeBn = "৩ মাস বয়সে প্রথম প্রয়োগ",
            boosterIntervalBn = "প্রতি ১ বছর পর পর বুস্টার ডোজ",
            routeBn = "চামড়ার নিচে (ঘাড়ের চামড়া টেনে)",
            doseAmountBn = "১ মিলি",
            precautionsBn = "ভ্যাকসিন দেওয়ার আগে কৃমিনাশক খাইয়ে ছাগলকে সুস্থ করে নিতে হবে। গর্ভবতী ছাগলকে দেওয়া যাবে না।"
        ),
        VaccinationScheduleItem(
            id = "vac_poultry_bcrdv",
            animalTypeBn = "মুরগি / পোল্ট্রি",
            vaccineNameBn = "বেবি চিক রাণীক্ষেত (BCRDV / 'F' Strain)",
            targetDiseaseBn = "রাণীক্ষেত রোগ (Ranikhet / Newcastle)",
            firstDoseAgeBn = "বাচ্চার বয়স ৩ থেকে ৫ দিন",
            boosterIntervalBn = "২১ দিন বয়সে দ্বিতীয় ডোজ",
            routeBn = "চোখে বা নাকের ছিদ্রে ১ ফোঁটা",
            doseAmountBn = "১ ফোঁটা ড্রপার দিয়ে",
            precautionsBn = "বরফ পাত্রে বহন করুন এবং ড্রপ দেওয়ার পর চোখে ফোঁটা মিলানো পর্যন্ত অপেক্ষা করুন।"
        ),
        VaccinationScheduleItem(
            id = "vac_poultry_gumboro",
            animalTypeBn = "মুরগি / পোল্ট্রি",
            vaccineNameBn = "গামবোরো ভ্যাকসিন (IBD Vaccine)",
            targetDiseaseBn = "গামবোরো রোগ (Infectious Bursal Disease)",
            firstDoseAgeBn = "বাচ্চার বয়স ১০ থেকে ১২ দিন",
            boosterIntervalBn = "১৭ থেকে ২০ দিন বয়সে বুস্টার ডোজ",
            routeBn = "চোখে ১ ফোঁটা অথবা বিশুদ্ধ পানিতে মিশিয়ে",
            doseAmountBn = "১ ফোঁটা / নির্ধারিত পানির অনুপাত",
            precautionsBn = "ক্লোরিনমুক্ত সাধারণ নলকূপের পানিতে স্কিম মিল্ক পাউডার মিশিয়ে ভ্যাকসিন মেশাতে হবে।"
        ),
        VaccinationScheduleItem(
            id = "vac_cattle_deworming",
            animalTypeBn = "সকল গবাদিপশু",
            vaccineNameBn = "কৃমিনাশক শিডিউল (Deworming Chart)",
            targetDiseaseBn = "কলিজা কৃমি, ফিতা কৃমি ও গোল কৃমি দমন",
            firstDoseAgeBn = "বাছুরকে ১৫-২০ দিন বয়সে প্রথমবার",
            boosterIntervalBn = "প্রতি ৩ মাস পর পর নিয়মিত কৃমিনাশক পরিবর্তন করে খাওয়ান",
            routeBn = "মুখে খাওয়ানোর ট্যাবলেট বা বোলাস",
            doseAmountBn = "শরীরের ওজন অনুযায়ী (৫০ কেজিতে ১টি বোলাস)",
            precautionsBn = "সকালে খালি পেটে খাওয়াতে হবে। কৃমিনাশক দেওয়ার পর ৫-৭ দিন ভালো লিভার টনিক ও মিনারেল খাওয়ানো জরুরি।"
        )
    )

    // -------------------------------------------------------------
    // 7. AQUACULTURE & FISH FARMING GUIDE
    // -------------------------------------------------------------
    val aquacultureTopicList: List<AquacultureTopicItem> = listOf(
        AquacultureTopicItem(
            id = "aqua_pond_prep",
            titleBn = "পুকুর প্রস্তুতি ও চুন প্রয়োগের সঠিক নিয়ম",
            titleEn = "Pond Preparation & Liming",
            categoryBn = "পুকুর প্রস্তুতি",
            summaryBn = "পুকুর শুকানো, রাক্ষুসে মাছ দূরীকরণ, মাটির অম্লতা অনুযায়ী চুন ও গোবর প্রয়োগ করে প্লাঙ্কটন তৈরি।",
            detailedStepsBn = listOf(
                "১. পুকুর শুকানো ও তলা পরিষ্কার: সম্ভব হলে পুকুরের পানি সেচে তলার অতিরিক্ত কালো পচা কাদা তুলে ফেলে ৭-১০ দিন কড়া রোদে শুকিয়ে মাটি ফেটে যাওয়া পর্যন্ত রাখুন।",
                "২. রাক্ষুসে ও অচাষযোগ্য মাছ দমন: পুকুর শুকানো সম্ভব না হলে প্রতি শতকে প্রতি ফুট পানির গভীরতার জন্য ২০-২৫ গ্রাম রোটেনন পাউডার অথবা মহুয়া খৈল প্রয়োগ করুন।",
                "৩. চুন প্রয়োগের মাত্রা: প্রতি শতকে ১ থেকে ২ কেজি হারে পাথুরে চুন পানিতে গুলে ঠাণ্ডা করে পুরো পুকুরে ছিটিয়ে দিন। এটি পানির পিএইচ (pH) ৭.৫ থেকে ৮.৫ এ ধরে রাখে।",
                "৪. প্রাকৃতিক খাবার (প্লাঙ্কটন) তৈরি: চুন দেওয়ার ৫-৭ দিন পর শতক প্রতি ৫-৭ কেজি পচা গোবর, ১০০ গ্রাম ইউরিয়া ও ৫০ গ্রাম টিএসপি গুলে ছিটিয়ে দিন।",
                "৫. পানির রঙ পরীক্ষা: সার দেওয়ার ৪-৫ দিন পর পানির রঙ হালকা সবুজ বা বাদামি-সবুজ হলে বুঝতে হবে প্রাকৃতিক খাবার তৈরি হয়েছে এবং পুকুর পোনা ছাড়ার জন্য প্রস্তুত।"
            ),
            calculationTipBn = "শতকে চুন হিসাব: পুকুর যদি ২০ শতক হয় এবং গড়ে ৩ ফুট পানি থাকে, তবে ২০ থেকে ৩০ কেজি চুন প্রয়োজন হবে।"
        ),
        AquacultureTopicItem(
            id = "aqua_stocking",
            titleBn = "কার্পজাতীয় ও মিশ্র মাছের পোনা মজুদ অনুপাত",
            titleEn = "Species Combination & Stocking Density",
            categoryBn = "পোনা মজুদ",
            summaryBn = "পুকুরের ওপর, মধ্য ও নিচের স্তরের খাদ্যের সর্বোচ্চ ব্যবহারের জন্য রুই, কাতলা, মৃগেল ও সিলভার কার্পের আদর্শ অনুপাত।",
            detailedStepsBn = listOf(
                "১. শতকে আদর্শ মজুদ সংখ্যা: আধা-নিবিড় মিশ্র কার্প চাষে শতকে মোট ৪০ থেকে ৫০ টি সুস্থ ও সবল পোনা (আঙ্গুলিপোনা, ৪-৫ ইঞ্চি) মজুদ করা সর্বোত্তম।",
                "২. স্তরের অনুপাত (শতক প্রতি):",
                "• ওপরের স্তর (১০-১২ টি): কাতলা ৩-৪ টি, সিলভার কার্প ৬-৮ টি (প্ল্যাঙ্কটন ও শ্যাওলা খাবে)।",
                "• মধ্য স্তর (১৫-১৮ টি): রুই ১২-১৪ টি, গ্রাস কার্প ২ টি (জলজ ঘাস খাবে)।",
                "• নিচের স্তর (১২-১৫ টি): মৃগেল ৮-১০ টি, কার্পিও / মিরর কার্প ৩-৪ টি (পুকুর তলার খাদ্য খাবে)।",
                "৩. পোনা ছাড়ার সঠিক নিয়ম: সকালে বা বিকেলে রোদের তীব্রতা কম থাকলে পোনার প্যাকেট আধা ঘণ্টা পুকুরের পানিতে ভাসিয়ে রেখে তাপমাত্রা সমান করে ধীরে ধীরে পুকুরে ছাড়ুন।"
            ),
            calculationTipBn = "একটি ৫০ শতকের পুকুরে মোট ২০০০ - ২৫০০ টি বিভিন্ন স্তরের পোনা মজুত করলে সর্বোচ্চ বৃদ্ধি পাওয়া যায়।"
        ),
        AquacultureTopicItem(
            id = "aqua_daily_feed",
            titleBn = "মাছের দৈনিক সম্পূরক খাদ্য নির্ধারণ ও প্রয়োগ",
            titleEn = "Daily Fish Feeding Rate",
            categoryBn = "দৈনিক খাদ্য",
            summaryBn = "মাছের বয়স ও শারীরিক ওজনের ওপর ভিত্তি করে দৈনিক ৩% থেকে ৫% হারে ভাসমান বা ডুবন্ত খাবারের হিসাব।",
            detailedStepsBn = listOf(
                "১. নমুনায়ন (Sampling): প্রতি ১৫ দিন পর পর জাল টেনে মাছের গড় ওজন নির্ধারণ করুন এবং মোট মাছের বায়োমাস (Biomass) বের করুন।",
                "২. দৈনিক খাদ্যের হার (দেহের ওজনের অনুপাত):",
                "• পোনা অবস্থা (১০-৫০ গ্রাম ওজন): দেহের ওজনের ৮% থেকে ১০% হারে খাবার।",
                "• বাড়ন্ত অবস্থা (৫০-২৫০ গ্রাম ওজন): দেহের ওজনের ৪% থেকে ৫% হারে খাবার।",
                "• বড় মাছ (২৫০ গ্রাম এর বেশি): দেহের ওজনের ২.৫% থেকে ৩% হারে খাবার।",
                "৩. খাবার দেওয়ার সময়: দৈনিক খাবারকে দুই বেলা ভাগ করে সকাল ৯-১০ টায় একবার এবং বিকেল ৩-৪ টায় একবার পুকুরের নির্দিষ্ট ফিডিং প্লেসে দিন।",
                "৪. সতর্কতা: মেঘলা আবহাওয়ায় বা পানি অতিরিক্ত সবুজ হলে খাবারের পরিমাণ অর্ধেক কমিয়ে দিন বা সেদিনের খাবার বন্ধ রাখুন।"
            ),
            calculationTipBn = "ফর্মুলা: পুকুরের মোট মাছ সংখ্যা × গড় ওজন (কেজি) = মোট বায়োমাস। মোট বায়োমাস × ৩% = দৈনিক খাদ্য (কেজি)।"
        ),
        AquacultureTopicItem(
            id = "aqua_water_quality",
            titleBn = "পুকুরে অক্সিজেনের ঘাটতি ও গ্যাস দূরীকরণ",
            titleEn = "Water Quality, Oxygen & Toxic Gas Management",
            categoryBn = "পানির যত্ন",
            summaryBn = "ভোরবেলা মাছ ভেসে ওঠার কারণ, তাৎক্ষণিক প্রতিকার, পিএইচ পরিমাপ ও গ্যাস কমানোর কার্যকরী উপায়।",
            detailedStepsBn = listOf(
                "১. অক্সিজেন ঘাটতির লক্ষণ: শেষ রাতে বা ভোরে মাছ পানির ওপরে এসে খাবি খায় এবং বাতাস গেলার চেষ্টা করে।",
                "২. জরুরি করণীয়: সাথে সাথে পুকুরে পাম্প বা স্যালো ইঞ্জিন চালিয়ে উপর থেকে পানির ফোয়ারা দিন অথবা বাঁশ দিয়ে পানি পিটিয়ে ঢেউ তৈরি করুন। প্রতি শতকে ২৫০ গ্রাম অক্সিজেন ট্যাবলেট (যেমন: অক্সিফ্লো/অক্সিম্যাক্স) ছড়িয়ে দিন।",
                "৩. অ্যামোনিয়া ও বিষাক্ত গ্যাস দূরীকরণ: পুকুরের তলায় জমে থাকা গ্যাস দূর করতে তলায় হররা টানুন (দড়িতে ইট বেঁধে তলা দিয়ে টেনে নিয়ে যাওয়া)। জিওলাইট শতকে ১ কেজি হারে প্রয়োগ করুন।",
                "৪. সঠিক পিএইচ ধরে রাখা: পানির পিএইচ ৭.৫ থেকে ৮.৫ এর মধ্যে রাখা আদর্শ। পিএইচ কমে পানি অম্লীয় হলে শতকে ২৫০-৫০০ গ্রাম চুন দিন।"
            ),
            calculationTipBn = "পুকুরে সবসময় ৫ মিলিগ্রাম/লিটার এর বেশি দ্রবীভূত অক্সিজেন বজায় রাখলে মাছের বৃদ্ধি দ্বিগুণ হয়।"
        ),
        AquacultureTopicItem(
            id = "aqua_fish_diseases",
            titleBn = "মাছের সাধারণ রোগবালাই ও চিকিৎসা",
            titleEn = "Common Fish Diseases & Treatment",
            categoryBn = "মাছের রোগ",
            summaryBn = "শীতকালীন ক্ষত রোগ (EUS), পাখনা ও লেজ পচা, ফুলকা পচা এবং মাছের উকুন প্রতিকার।",
            detailedStepsBn = listOf(
                "১. শীতকালীন ক্ষত রোগ (EUS): শীতের শুরুতে মাছের গায়ে লাল লাল দাগ ও পরে গভীর ক্ষত হয়। প্রতিকার: আশ্বিন-কার্তিক মাসে শীতের আগেই শতকে ১ কেজি চুন ও ১ কেজি লবণ একত্রে পুকুরে প্রয়োগ করুন। আক্রান্ত হলে পটাসিয়াম পারম্যাঙ্গানেট প্রতি শতকে ২-৩ গ্রাম দিন।",
                "২. লেজ ও পাখনা পচা রোগ: ব্যাকটেরিয়াজনিত কারণে পাখনার প্রান্ত সাদা হয়ে পচে যায়। প্রতিকার: কপার সালফেট (তুঁতে) প্রতি শতকে ১.৫-২ গ্রাম অথবা জীবাণুনাশক স্প্রে করুন।",
                "৩. মাছের উকুন (আর্গুলোসিস): রুই ও কাতলা মাছের গায়ে ছোট ছোট উকুন লেগে রক্ত চুষে খায়। প্রতিকার: সাইপারমেথ্রিন গ্রুপের ওষুধ (যেমন: ডেল্টামেথ্রিন/সাইপারমেথ্রিন) প্রতি শতাংশে ১ ফুট পানির জন্য ১ মিলি হিসেবে প্রয়োগ করুন।"
            ),
            calculationTipBn = "শীত আসার আগে আশ্বিন মাসে চুন ও লবণ প্রয়োগ করলে ৯০% ক্ষত রোগ প্রতিরোধ করা সম্ভব।"
        )
    )
}

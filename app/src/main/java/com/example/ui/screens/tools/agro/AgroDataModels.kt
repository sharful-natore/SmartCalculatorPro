package com.example.ui.screens.tools.agro

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AgroSection(
    val titleBn: String,
    val titleEn: String,
    val icon: ImageVector,
    val subtitleBn: String,
    val subtitleEn: String
) {
    CROP_CALENDAR(
        titleBn = "ফসল ক্যালেন্ডার",
        titleEn = "Crop Calendar",
        icon = Icons.Default.CalendarMonth,
        subtitleBn = "মৌসুম ভিত্তিক রোপণ ও ফলন সময়সূচি",
        subtitleEn = "Season & Planting Timetable"
    ),
    CROP_DISEASES(
        titleBn = "রোগ ও প্রতিকার",
        titleEn = "Crop Doctor",
        icon = Icons.Default.PestControl,
        subtitleBn = "ফসলের রোগবালাই, লক্ষণ ও সঠিক চিকিৎসা",
        subtitleEn = "Pests, Diseases & Remedies"
    ),
    FERTILIZERS_PESTICIDES(
        titleBn = "সার ও বালাইনাশক",
        titleEn = "Fertilizer & Spray",
        icon = Icons.Default.Science,
        subtitleBn = "সারের কাজ, বালাইনাশক গ্রুপ ও স্প্রে মাপক",
        subtitleEn = "Nutrients, Pesticides & Dilution"
    ),
    LIVESTOCK_DISEASES(
        titleBn = "পশুর রোগবালাই",
        titleEn = "Animal Health",
        icon = Icons.Default.Pets,
        subtitleBn = "গরু-ছাগলের মারাত্মক রোগ ও প্রাথমিক সেবা",
        subtitleEn = "Livestock Diseases & Treatment"
    ),
    LIVESTOCK_FEED(
        titleBn = "খাদ্য তৈরি ও যত্ন",
        titleEn = "Feed & Care",
        icon = Icons.Default.Grass,
        subtitleBn = "১০০ কেজি দানাদার খাদ্য, UMS ও সাইলেজ",
        subtitleEn = "Feed Rations, UMS & Silage"
    ),
    VACCINATION_SCHEDULE(
        titleBn = "ভ্যাকসিন ও কৃমিনাশক",
        titleEn = "Vaccine & Worm",
        icon = Icons.Default.Vaccines,
        subtitleBn = "গরু, ছাগল ও মুরগির টিকাদান চার্ট",
        subtitleEn = "Vaccination & Deworming Schedule"
    ),
    AQUACULTURE_GUIDE(
        titleBn = "মাছ চাষ নির্দেশিকা",
        titleEn = "Aquaculture",
        icon = Icons.Default.WaterDrop,
        subtitleBn = "পুকুর প্রস্তুতি, পোনা মজুদ ও মাছের খাবার",
        subtitleEn = "Pond Prep, Feeding & Fish Care"
    )
}

// 1. Crop Calendar
data class CropCalendarItem(
    val id: String,
    val cropNameBn: String,
    val cropNameEn: String,
    val categoryBn: String, // "দানা জাতীয়", "সবজি", "ডাল ও তেল", "ফলমূল", "মসলা"
    val seasonBn: String, // "রবি (শীত)", "খরিপ-১ (গ্রীষ্ম)", "খরিপ-২ (বর্ষা)", "সারা বছর"
    val sowingTimeBn: String, // বীজ বপন সময়
    val transplantAgeBn: String, // চারার বয়স
    val harvestTimeBn: String, // ফসল সংগ্রহ
    val seedRatePerDecimalBn: String, // শতক প্রতি বীজ
    val estimatedYieldBn: String, // শতকে গড় ফলন
    val landPrepTipsBn: String, // জমি তৈরির নিয়ম
    val specialAdviceBn: String // বিশেষ পরামর্শ
)

// 2. Crop Disease & Remedy
data class CropDiseaseItem(
    val id: String,
    val cropNameBn: String,
    val cropNameEn: String,
    val diseaseNameBn: String,
    val diseaseNameEn: String,
    val pathogenTypeBn: String, // "ছত্রাকঘটিত", "ব্যাকটেরিয়াঘটিত", "ভাইরাসঘটিত", "কীটপতঙ্গ / পোকা"
    val severityLevelBn: String, // "উচ্চ ঝুঁকিপূর্ণ", "মাঝারি", "নিয়ন্ত্রণযোগ্য"
    val symptomsBn: String, // দৃশ্যমান লক্ষণ
    val organicControlBn: String, // জৈব বা সমন্বিত দমন (IPM)
    val chemicalRemedyBn: String, // রাসায়নিক বালাইনাশক ও মাত্রা
    val preventiveCareBn: String // পূর্বপ্রতিরোধ ব্যবস্থা
)

// 3. Fertilizer and Pesticide
data class FertilizerItem(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val nutrientSymbol: String, // N, P, K, S, Zn, B, Mg
    val primaryFunctionBn: String, // কোন সারের কী কাজ
    val deficiencySymptomsBn: String, // অভাবজনিত লক্ষণ
    val excessHarmBn: String, // মাত্রাতিরিক্ত ব্যবহারে ক্ষতি
    val applicationMethodBn: String, // প্রয়োগের নিয়ম ও সময়
    val dosePerDecimalBn: String // সাধারণ ফসলে শতকে আনুমানিক মাত্রা
)

data class PesticideGroupItem(
    val id: String,
    val groupName: String,
    val categoryBn: String, // "কীটনাশক", "ছত্রাকনাশক", "মাকড়নাশক", "আগাছানাশক"
    val popularBrandsBn: String, // যেমন: কনফিডোর, ডাইথেন এম-৪৫, নোইন, রিডোমিল গোল্ড
    val targetPestBn: String, // কোন পোকা বা রোগে কাজ করে
    val dilutionDoseBn: String, // প্রতি লিটার পানিতে কতটুকু
    val safetyWaitingPeriodBn: String, // স্প্রে করার পর ফসল তোলার নিরাপদ বিরতি (দিন)
    val precautionsBn: String // সতর্কতা
)

// 4. Livestock Disease
data class LivestockDiseaseItem(
    val id: String,
    val diseaseNameBn: String,
    val diseaseNameEn: String,
    val affectedAnimalBn: String, // "গরু ও মহিষ", "ছাগল ও ভেড়া", "সকল গবাদিপশু"
    val causeBn: String, // "ভাইরাসঘটিত", "ব্যাকটেরিয়াঘটিত", "মেটাবলিক / পরিপাকজনিত"
    val severityBn: String, // "অতি মারাত্মক / জরুরি", "মাঝারি", "সাধারণ"
    val symptomsBn: String, // রোগের মূল লক্ষণ
    val immediateCareBn: String, // প্রাথমিক ও ঘরোয়া শুশ্রূষা
    val medicalTreatmentBn: String, // পশুচিকিৎসকের চিকিৎসা সহায়িকা
    val preventionBn: String // প্রতিরোধ ও সুরক্ষামূলক ব্যবস্থা
)

// 5. Livestock Feed Formula
data class LivestockFeedRecipe(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val targetAnimalBn: String, // "দুগ্ধবতী গাভী", "মোটাতাজাকরণ ষাঁড়", "বাছুর", "ছাগল"
    val batchWeightBn: String, // যেমন "১০০ কেজি দানাদার মিশ্রণ"
    val ingredientListBn: List<Pair<String, String>>, // নাম ও ওজন
    val dailyFeedingRuleBn: String, // দৈহিক ওজনের কত শতাংশ খাওয়াতে হবে
    val preparationGuideBn: String, // তৈরি ও মেশানোর পদ্ধতি
    val specialAdviceBn: String // বিশেষ যত্ন
)

// 6. Vaccination Schedule
data class VaccinationScheduleItem(
    val id: String,
    val animalTypeBn: String, // "গরু ও মহিষ", "ছাগল ও ভেড়া", "মুরগি / পোল্ট্রি"
    val vaccineNameBn: String,
    val targetDiseaseBn: String,
    val firstDoseAgeBn: String, // প্রথম প্রয়োগের বয়স
    val boosterIntervalBn: String, // বুস্টার ডোজের সময়কাল
    val routeBn: String, // "চামড়ার নিচে (SC)", "মাংসে (IM)", "চোখে ফোঁটা", "পানিতে মিশিয়ে"
    val doseAmountBn: String, // ডোজের পরিমাণ
    val precautionsBn: String // বিশেষ সতর্কতা ও তাপমাত্রা
)

// 7. Aquaculture Topic
data class AquacultureTopicItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val categoryBn: String, // "পুকুর প্রস্তুতি", "পোনা মজুদ", "দৈনিক খাদ্য", "পানির যত্ন", "মাছের রোগ"
    val summaryBn: String,
    val detailedStepsBn: List<String>,
    val calculationTipBn: String? = null
)

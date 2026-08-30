package com.example.util

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

data class SpecialDayEvent(
    val month: Int, // 1-12
    val day: Int,   // 1-31
    val titleBn: String,
    val titleEn: String,
    val categoryBn: String,
    val categoryEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val searchQuery: String,
    val icon: ImageVector = Icons.Default.Celebration,
    val isBirthday: Boolean = false,
    val isFestival: Boolean = false,
    val isSearchFallback: Boolean = false
)

object SpecialDayManager {

    private const val PREFS_NAME = "special_days_prefs"
    private const val KEY_CACHED_EVENTS_JSON = "cached_special_days_json"

    // Online CDN endpoint URL for dynamic yearly special days update
    private const val CDN_EVENTS_URL = "https://raw.githubusercontent.com/aistudio-cdn/special-days/main/bangladesh_special_days.json"

    private val defaultSpecialEventsList = listOf(
        // === JANUARY ===
        SpecialDayEvent(
            month = 1, day = 1,
            titleBn = "ইংরেজি নববর্ষ (New Year's Day)",
            titleEn = "Happy New Year!",
            categoryBn = "উৎসব ও আনন্দ",
            categoryEn = "Festival & Greetings",
            descriptionBn = "নতুন বছরের শুভকামনা! সাফল্য ও সমৃদ্ধিময় জীবনের জন্য বছরটির শুরু হোক ইতিবাচক শক্তিতে।",
            descriptionEn = "Wishing you a happy, fruitful and prosperous New Year ahead!",
            searchQuery = "New Year's Day traditions and significance",
            icon = Icons.Default.Celebration,
            isFestival = true
        ),
        SpecialDayEvent(
            month = 1, day = 4,
            titleBn = "স্যার আইজ্যাক নিউটনের জন্মবার্ষিকী",
            titleEn = "Sir Isaac Newton's Birthday",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "মহাকর্ষ সূত্র ও গতির মহাজাগতিক তিন সূত্রের জনক পদার্থবিদ স্যার আইজ্যাক নিউটনের জন্মদিবস।",
            descriptionEn = "Celebrating the birth of Sir Isaac Newton, pioneer of classical mechanics & gravity.",
            searchQuery = "Sir Isaac Newton biography laws of motion",
            icon = Icons.Default.Science,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 1, day = 8,
            titleBn = "স্টিফেন হকিংয়ের জন্মবার্ষিকী",
            titleEn = "Stephen Hawking's Birthday",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "ব্ল্যাকহোল ও মহাবিশ্বের অজানা রহস্য উদঘাটক প্রখ্যাত তাত্ত্বিক পদার্থবিজ্ঞানী স্টিফেন হকিংয়ের জন্মবার্ষিকী।",
            descriptionEn = "Commemorating theoretical physicist Stephen Hawking and his discoveries on black holes.",
            searchQuery = "Stephen Hawking biography black holes",
            icon = Icons.Default.Psychology,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 1, day = 10,
            titleBn = "বঙ্গবন্ধুর স্বদেশ প্রত্যাবর্তন দিবস",
            titleEn = "Bangabandhu's Homecoming Day",
            categoryBn = "জাতীয় দিবস",
            categoryEn = "National Day",
            descriptionBn = "১৯৭২ সালের ১০ই জানুয়ারি পাকিস্তানি কারাগার থেকে মুক্ত হয়ে স্বাধীন বাংলাদেশে ফিরে আসেন জাতির পিতা শেখ মুজিবুর রহমান।",
            descriptionEn = "Commemorating Bangabandhu Sheikh Mujibur Rahman's historic return to independent Bangladesh in 1972.",
            searchQuery = "Bangabandhu Homecoming Day 10 January Bangladesh",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 1, day = 17,
            titleBn = "বেঞ্জামিন ফ্র্যাঙ্কলিনের জন্মবার্ষিকী",
            titleEn = "Benjamin Franklin's Birthday",
            categoryBn = "মনীষী জন্মবার্ষিকী",
            categoryEn = "Polymath Birthday",
            descriptionBn = "বিদ্যুৎ আবিষ্কার ও আধুনিক বিজ্ঞানের অন্যতম পথিকৃৎ বেঞ্জামিন ফ্র্যাঙ্কলিনের জন্মবার্ষিকী।",
            descriptionEn = "Celebrating polymath Benjamin Franklin and his contributions to science & electricity.",
            searchQuery = "Benjamin Franklin inventions electricity",
            icon = Icons.Default.AutoAwesome,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 1, day = 24,
            titleBn = "ঐতিহাসিক গণঅভ্যুত্থান দিবস (১৯৬৯)",
            titleEn = "Mass Uprising Day (1969)",
            categoryBn = "জাতীয় ঐতিহাসিক দিবস",
            categoryEn = "National Historic Day",
            descriptionBn = "১৯৬৯ সালের এই দিনে পাকিস্তানি স্বৈরাচারী শাসনবিরোধী ছাত্র-জনতার গণঅভ্যুত্থান নতুন ইতিহাস সৃষ্টি করে।",
            descriptionEn = "Honoring the martyrs of the historic 1969 Mass Uprising in East Pakistan.",
            searchQuery = "1969 Mass Uprising in East Pakistan history",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 1, day = 25,
            titleBn = "মাইকেল মধুসূদন দত্তের জন্মবার্ষিকী",
            titleEn = "Michael Madhusudan Dutt's Birthday",
            categoryBn = "সাহিত্যিক জন্মবার্ষিকী",
            categoryEn = "Poet Birthday",
            descriptionBn = "বাংলা আধুনিক কবিতার পথিকৃৎ ও সনেট রচয়িতা মহাকবি মাইকেল মধুসূদন দত্তের জন্মদিবস।",
            descriptionEn = "Celebrating the pioneer of Bengali sonnets & modern poetry, Michael Madhusudan Dutt.",
            searchQuery = "Michael Madhusudan Dutt biography sonnets",
            icon = Icons.Default.School,
            isBirthday = true
        ),

        // === FEBRUARY ===
        SpecialDayEvent(
            month = 2, day = 1,
            titleBn = "অমর একুশে বইমেলা উদ্বোধন",
            titleEn = "Amar Ekushey Book Fair Begins",
            categoryBn = "জাতীয় সংস্কৃতি ও সাহিত্য",
            categoryEn = "National Culture & Literature",
            descriptionBn = "বাঙালি সংস্কৃতির প্রাণের মেলা 'অমর একুশে বইমেলা' বাংলা একাডেমি প্রাঙ্গণে উদযাপন শুরু।",
            descriptionEn = "Celebrating the grand opening of Amar Ekushey Book Fair at Bangla Academy.",
            searchQuery = "Amar Ekushey Book Fair history Bangla Academy",
            icon = Icons.Default.School,
            isFestival = true
        ),
        SpecialDayEvent(
            month = 2, day = 11,
            titleBn = "থমাস আলভা এডিসনের জন্মবার্ষিকী",
            titleEn = "Thomas Edison's Birthday",
            categoryBn = "উদ্ভাবক জন্মবার্ষিকী",
            categoryEn = "Inventor Birthday",
            descriptionBn = "বৈদ্যুতিক বাতি, ফোনোগ্রাফ সহ ১০০০ এরও বেশি উদ্ভাবনের জনক থমাস এডিসনের জন্মবার্ষিকী।",
            descriptionEn = "Celebrating prolific inventor Thomas Alva Edison, creator of the electric light bulb.",
            searchQuery = "Thomas Alva Edison inventions biography",
            icon = Icons.Default.Science,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 2, day = 12,
            titleBn = "চার্লস ডারউইনের জন্মবার্ষিকী (ডারউইন দিবস)",
            titleEn = "Charles Darwin Day",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "বিবর্তনবাদের জনক জীববিজ্ঞানী চার্লস ডারউইনের স্মরণে আন্তর্জাতিক ডারউইন দিবস।",
            descriptionEn = "Honoring Charles Darwin and his ground-breaking theory of evolution and natural selection.",
            searchQuery = "Charles Darwin theory of evolution",
            icon = Icons.Default.Public,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 2, day = 14,
            titleBn = "সুন্দরবন দিবস / ভালোবাসা দিবস",
            titleEn = "Sundarbans Day / Valentine's Day",
            categoryBn = "পরিবেশ ও জাতীয় দিবস",
            categoryEn = "Environment & Day",
            descriptionBn = "বিশ্বের একক বৃহত্তম ম্যানগ্রোভ বন সুন্দরবন রক্ষা ও ভালোবাসার দিবস উদযাপন।",
            descriptionEn = "Promoting Sundarbans mangrove forest conservation and celebrating love & affection.",
            searchQuery = "Sundarbans Day Bangladesh conservation",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 2, day = 15,
            titleBn = "গ্যালিলিও গ্যালিলেইয়ের জন্মবার্ষিকী",
            titleEn = "Galileo Galilei's Birthday",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "আধুনিক জ্যোতির্বিজ্ঞান ও টেলিস্কোপের জনক বিজ্ঞানী গ্যালিলিও গ্যালিলেইয়ের জন্মবার্ষিকী।",
            descriptionEn = "Celebrating the father of modern observational astronomy, Galileo Galilei.",
            searchQuery = "Galileo Galilei telescope discovery",
            icon = Icons.Default.Science,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 2, day = 21,
            titleBn = "আন্তর্জাতিক মাতৃভাষা দিবস ও শহীদ দিবস",
            titleEn = "International Mother Language Day",
            categoryBn = "আন্তর্জাতিক ও জাতীয় দিবস",
            categoryEn = "National & Int'l Day",
            descriptionBn = "১৯৫২ সালের মহান ভাষা শহীদের আত্মত্যাগের স্মরণে বিশ্বজুড়ে পালিত মাতৃভাষা দিবস। মোদের গরব মোদের আশা, আমরি বাংলা ভাষা!",
            descriptionEn = "Honoring the martyrs of 1952 Language Movement & promoting linguistic diversity.",
            searchQuery = "International Mother Language Day history 1952",
            icon = Icons.Default.Flag,
            isFestival = true
        ),
        SpecialDayEvent(
            month = 2, day = 28,
            titleBn = "জাতীয় বিজ্ঞান দিবস (রমন প্রভাব আবিষ্কার)",
            titleEn = "National Science Day",
            categoryBn = "বিজ্ঞান ও গবেষণা",
            categoryEn = "Science & Discovery",
            descriptionBn = "নোবেলজয়ী বিজ্ঞানী সি ভি রমনের 'রমন প্রভাব' আবিষ্কারের স্মরণে উদযাপিত বিজ্ঞান দিবস।",
            descriptionEn = "Celebrating the discovery of the Raman Effect by Nobel laureate Sir C.V. Raman.",
            searchQuery = "Raman Effect CV Raman Nobel prize physics",
            icon = Icons.Default.Science
        ),

        // === MARCH ===
        SpecialDayEvent(
            month = 3, day = 3,
            titleBn = "বিশ্ব বন্যপ্রাণী দিবস (World Wildlife Day)",
            titleEn = "World Wildlife Day",
            categoryBn = "আন্তর্জাতিক পরিবেশ দিবস",
            categoryEn = "Environment Day",
            descriptionBn = "পৃথিবীর বৈচিত্র্যময় বন্যপ্রাণী ও উদ্ভিদ সংরক্ষণে সচেতনতা সৃষ্টির লক্ষ্যে জাতিসংঘ ঘোষিত আন্তর্জাতিক দিবস।",
            descriptionEn = "UN International day to celebrate and raise awareness of world's wild fauna & flora.",
            searchQuery = "World Wildlife Day UN theme",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 3, day = 7,
            titleBn = "ঐতিহাসিক ৭ই মার্চের ভাষণ দিবস",
            titleEn = "Historic 7th March Speech Day",
            categoryBn = "জাতীয় ঐতিহাসিক দিবস",
            categoryEn = "National Historic Day",
            descriptionBn = "১৯৭১ সালের ৭ই মার্চ রেসকোর্স ময়দানে বঙ্গবন্ধুর কালজয়ী ভাষণ: 'এবারের সংগ্রাম আমাদের মুক্তির সংগ্রাম, এবারের সংগ্রাম স্বাধীনতার সংগ্রাম!'",
            descriptionEn = "Commemorating Bangabandhu's historic speech inspiring 1971 Bangladesh Liberation War.",
            searchQuery = "7th March speech Bangabandhu UNESCO world heritage",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 3, day = 8,
            titleBn = "আন্তর্জাতিক নারী দিবস",
            titleEn = "International Women's Day",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "International Day",
            descriptionBn = "নারীর অধিকার, সমতা ও সমাজে নারীদের অসামান্য অবদানকে সম্মান জানাতে বিশ্ব নারী দিবস।",
            descriptionEn = "Celebrating social, economic, cultural and political achievements of women worldwide.",
            searchQuery = "International Women's Day significance history",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 3, day = 14,
            titleBn = "আলবার্ট আইনস্টাইনের জন্মবার্ষিকী ও বিশ্ব পাই দিবস",
            titleEn = "Albert Einstein's Birthday & Pi Day",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী & পাই দিবস",
            categoryEn = "Scientist Birthday & Pi Day",
            descriptionBn = "আপেক্ষিকতার সূত্রের জনক আইনস্টাইনের জন্ম ও গাণিতিক ধ্রুবক পাই (π = ৩.১৪) উদযাপনের বিশেষ দিন।",
            descriptionEn = "Celebrating Albert Einstein's birth & mathematical constant Pi (3.14).",
            searchQuery = "Albert Einstein theory of relativity Pi Day",
            icon = Icons.Default.Science,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 3, day = 17,
            titleBn = "জাতীয় শিশু দিবস ও বঙ্গবন্ধুর জন্মবার্ষিকী",
            titleEn = "National Children's Day Bangladesh",
            categoryBn = "জাতীয় দিবস",
            categoryEn = "National Day",
            descriptionBn = "জাতির পিতা শেখ মুজিবুর রহমানের জন্মবার্ষিকী উপলক্ষে বাংলাদেশে উদযাপিত জাতীয় শিশু দিবস।",
            descriptionEn = "Commemorating the birthday of Bangabandhu Sheikh Mujibur Rahman and celebrating children.",
            searchQuery = "National Children Day Bangladesh Sheikh Mujib birthday",
            icon = Icons.Default.Cake,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 3, day = 21,
            titleBn = "বিশ্ব বন দিবস ও বিশ্ব কবিতা দিবস",
            titleEn = "International Day of Forests & Poetry Day",
            categoryBn = "পরিবেশ ও সাহিত্য",
            categoryEn = "Environment & Literature",
            descriptionBn = "বনাঞ্চল রক্ষা ও কবিতার অনন্য সাহিত্যিক শক্তিকে বিশ্বব্যাপী স্মরণ করার দিন।",
            descriptionEn = "Raising awareness about forest conservation and celebrating poetic expression.",
            searchQuery = "International Day of Forests UNESCO World Poetry Day",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 3, day = 22,
            titleBn = "বিশ্ব পানি দিবস (World Water Day)",
            titleEn = "World Water Day",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "International Day",
            descriptionBn = "বিশুদ্ধ খাবার পানি সংরক্ষণ ও বৈশ্বিক পানি সংকট মোকাবিলার সচেতনতা বৃদ্ধির জাতিসংঘ দিবস।",
            descriptionEn = "Focusing attention on the importance of freshwater and advocating sustainable management.",
            searchQuery = "World Water Day UN freshwater conservation",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 3, day = 26,
            titleBn = "বাংলাদেশের স্বাধীনতা ও জাতীয় দিবস",
            titleEn = "Independence Day of Bangladesh",
            categoryBn = "মহান জাতীয় দিবস",
            categoryEn = "National Independence Day",
            descriptionBn = "১৯৭১ সালের ২৬শে মার্চ স্বাধীন বাংলাদেশ ঘোষণার ঐতিহাসিক গৌরবোজ্জ্বল দিন। রক্তে কেনা আমাদের প্রিয় লাল-সবুজের পতাকা!",
            descriptionEn = "Celebrating Bangladesh Independence Day & honoring our heroic freedom fighters.",
            searchQuery = "Independence Day of Bangladesh 26 March 1971 history",
            icon = Icons.Default.Flag,
            isFestival = true
        ),

        // === APRIL ===
        SpecialDayEvent(
            month = 4, day = 7,
            titleBn = "বিশ্ব স্বাস্থ্য দিবস (World Health Day)",
            titleEn = "World Health Day",
            categoryBn = "স্বাস্থ্য ও সচেতনতা",
            categoryEn = "Health & Awareness",
            descriptionBn = "বিশ্ব স্বাস্থ্য সংস্থা (WHO) ঘোষিত সবার জন্য সুস্বাস্থ্য নিশ্চিত করার বৈশ্বিক সচেতনতা দিবস।",
            descriptionEn = "Global health awareness day celebrated under the sponsorship of World Health Organization.",
            searchQuery = "World Health Day WHO theme",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 4, day = 14,
            titleBn = "পহেলা বৈশাখ (বাংলা নববর্ষ)",
            titleEn = "Pohela Boishakh (Bengali New Year)",
            categoryBn = "জাতীয় উৎসব",
            categoryEn = "National Cultural Festival",
            descriptionBn = "শুভ নববর্ষ! ১৪৩১ বাংলা সনের সূচনা ও মঙ্গল শোভাযাত্রার মধ্য দিয়ে ঐতিহ্যবাহী সর্বজনীন পহেলা বৈশাখ উদযাপন।",
            descriptionEn = "Wishing Happy Bengali New Year! Celebrating traditional Mangal Shobhajatra and cultural festivities.",
            searchQuery = "Pohela Boishakh Mangal Shobhajatra UNESCO cultural heritage",
            icon = Icons.Default.Celebration,
            isFestival = true
        ),
        SpecialDayEvent(
            month = 4, day = 15,
            titleBn = "লিওনার্দো দ্য ভিঞ্চির জন্মবার্ষিকী (বিশ্ব শিল্প দিবস)",
            titleEn = "Leonardo da Vinci Birthday / World Art Day",
            categoryBn = "শিল্পী জন্মবার্ষিকী",
            categoryEn = "Artist Birthday",
            descriptionBn = "মো নালিসা চিত্রশিল্পী, বিজ্ঞানী ও সর্বকালের সেরা উদ্ভাবক লিওনার্দো দ্য ভিঞ্চির জন্মবার্ষিকী।",
            descriptionEn = "Celebrating Renaissance genius Leonardo da Vinci on World Art Day.",
            searchQuery = "Leonardo da Vinci inventions Mona Lisa biography",
            icon = Icons.Default.AutoAwesome,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 4, day = 17,
            titleBn = "ঐতিহাসিক মুজিবনগর দিবস",
            titleEn = "Historic Mujibnagar Day",
            categoryBn = "জাতীয় ঐতিহাসিক দিবস",
            categoryEn = "National Historic Day",
            descriptionBn = "১৯৭১ সালের ১৭ই এপ্রিল মেহেরপুরের আম্রকাননে গণপ্রজাতন্ত্রী বাংলাদেশের প্রথম সরকারের আনুষ্ঠানিকভাবে শপথ গ্রহণ।",
            descriptionEn = "Commemorating the oath-taking of Bangladesh's first provisional government at Mujibnagar in 1971.",
            searchQuery = "Historic Mujibnagar Day 17 April 1971 Bangladesh",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 4, day = 22,
            titleBn = "বিশ্ব বসুন্ধরা/পৃথিবী দিবস (Earth Day)",
            titleEn = "International Earth Day",
            categoryBn = "আন্তর্জাতিক পরিবেশ দিবস",
            categoryEn = "Environment Day",
            descriptionBn = "জলবায়ু পরিবর্তন রোধ ও ধরিত্রী মাতাকে রক্ষা করতে বিশ্বজুড়ে পরিবেশ সচেতনতা দিবস।",
            descriptionEn = "Demonstrating support for environmental protection and planetary preservation.",
            searchQuery = "International Earth Day environmental protection",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 4, day = 23,
            titleBn = "বিশ্ব বই ও গ্রন্থস্বত্ব দিবস (World Book Day)",
            titleEn = "World Book & Copyright Day",
            categoryBn = "সাহিত্য ও সংস্কৃতি",
            categoryEn = "Literature & Books",
            descriptionBn = "বই পাঠের আনন্দ ছড়িয়ে দিতে এবং শেক্সপিয়ারের প্রয়াণ দিবস উপলক্ষে ইউনেস্কো বই দিবস।",
            descriptionEn = "Promoting reading, publishing and copyright protection worldwide by UNESCO.",
            searchQuery = "World Book Day UNESCO William Shakespeare",
            icon = Icons.Default.School
        ),

        // === MAY ===
        SpecialDayEvent(
            month = 5, day = 1,
            titleBn = "মহান মে দিবস (আন্তর্জাতিক শ্রমিক দিবস)",
            titleEn = "May Day / Labor Day",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "International Workers' Day",
            descriptionBn = "১৮৮৬ সালের শিকাগো শ্রমিক আন্দোলনের স্মরণে শ্রমিকদের ৮ ঘণ্টা কাজের অধিকারের ঐতিহাসিক দিবস।",
            descriptionEn = "Honoring the historic struggle and rights of working class people worldwide.",
            searchQuery = "May Day International Workers Day Chicago strike history",
            icon = Icons.Default.Flag,
            isFestival = true
        ),
        SpecialDayEvent(
            month = 5, day = 3,
            titleBn = "বিশ্ব মুক্ত গণমাধ্যম দিবস (Press Freedom Day)",
            titleEn = "World Press Freedom Day",
            categoryBn = "আন্তর্জাতিক গণমাধ্যম",
            categoryEn = "International Journalism",
            descriptionBn = "স্বাধীন সাংবাদিকতা ও গণমাধ্যমের বাক স্বাধীনতা রক্ষার জাতিসংঘ ঘোষিত আন্তর্জাতিক দিবস।",
            descriptionEn = "Raising awareness of the importance of press freedom and safety of journalists.",
            searchQuery = "World Press Freedom Day UNESCO",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 5, day = 8,
            titleBn = "বিশ্ব রেড ক্রস দিবস / অঁরি দ্যুনার জন্মবার্ষিকী",
            titleEn = "World Red Cross Day",
            categoryBn = "মানবসেবা দিবস",
            categoryEn = "Humanitarian Day",
            descriptionBn = "রেড ক্রস প্রতিষ্ঠাতা ও শান্তিতে প্রথম নোবেলজয়ী অঁরি দ্যুনার স্মরণে মানবসেবার বিশেষ দিন।",
            descriptionEn = "Celebrating the Red Cross founder Henry Dunant & selfless humanitarian work globally.",
            searchQuery = "World Red Cross Day Henry Dunant Nobel prize",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 5, day = 9,
            titleBn = "বিশ্বকবি রবীন্দ্রনাথ ঠাকুরের জন্মজয়ন্তী (২৫শে বৈশাখ)",
            titleEn = "Rabindranath Tagore's Birth Anniversary",
            categoryBn = "মহাকবি জন্মবার্ষিকী",
            categoryEn = "Nobel Poet Birthday",
            descriptionBn = "আমাদের জাতীয় সংগীতের রচয়িতা, সাহিত্য নোবেলজয়ী বিশ্বকবি রবীন্দ্রনাথ ঠাকুরের ২৫শে বৈশাখ জন্মজয়ন্তী।",
            descriptionEn = "Celebrating Nobel laureate poet Rabindranath Tagore, author of Bangladesh national anthem.",
            searchQuery = "Rabindranath Tagore biography Nobel Prize literature Gitanjali",
            icon = Icons.Default.School,
            isBirthday = true,
            isFestival = true
        ),
        SpecialDayEvent(
            month = 5, day = 17,
            titleBn = "বিশ্ব টেলিযোগাযোগ ও তথ্য সংঘ দিবস",
            titleEn = "World Telecommunication Day",
            categoryBn = "তথ্যপ্রযুক্তি দিবস",
            categoryEn = "ICT & Tech Day",
            descriptionBn = "ইন্টারনেট ও আধুনিক ডিজিটাল প্রযুক্তির সুবিধা সবার কাছে পৌঁছে দেওয়ার আন্তর্জাতিক সচেতনতা দিন।",
            descriptionEn = "Promoting awareness of the possibilities that internet and ICT bring to societies.",
            searchQuery = "World Telecommunication Information Society Day ITU",
            icon = Icons.Default.Science
        ),
        SpecialDayEvent(
            month = 5, day = 25,
            titleBn = "জাতীয় কবি কাজী নজরুল ইসলামের জন্মজয়ন্তী (১১ই জ্যৈষ্ঠ)",
            titleEn = "Kazi Nazrul Islam's Birth Anniversary",
            categoryBn = "জাতীয় কবি জন্মবার্ষিকী",
            categoryEn = "National Poet Birthday",
            descriptionBn = "বিদ্রোহী কবি ও আমাদের জাতীয় কবি কাজী নজরুল ইসলামের অসামান্য সাম্যবাদী সাহিত্য কর্মের জন্মদিবস।",
            descriptionEn = "Commemorating the birth anniversary of National Poet of Bangladesh, Kazi Nazrul Islam.",
            searchQuery = "Kazi Nazrul Islam Bidrohi poetry biography",
            icon = Icons.Default.School,
            isBirthday = true,
            isFestival = true
        ),

        // === JUNE ===
        SpecialDayEvent(
            month = 6, day = 5,
            titleBn = "বিশ্ব পরিবেশ দিবস (World Environment Day)",
            titleEn = "World Environment Day",
            categoryBn = "আন্তর্জাতিক পরিবেশ দিবস",
            categoryEn = "Environment Day",
            descriptionBn = "জাতিসংঘের পরিবেশ কর্মসূচির (UNEP) অধীনে প্রকৃতির সংরক্ষণ ও সবুজ পৃথিবী গড়ার বৈশ্বিক আহ্বান।",
            descriptionEn = "UN's principal vehicle for encouraging global awareness and action for environment.",
            searchQuery = "World Environment Day UNEP campaign theme",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 6, day = 7,
            titleBn = "ঐতিহাসিক ছয় দফা দিবস",
            titleEn = "Historic 6-Point Day",
            categoryBn = "জাতীয় ঐতিহাসিক দিবস",
            categoryEn = "National Historic Day",
            descriptionBn = "১৯৬৬ সালের ৭ই জুন বঙ্গবন্ধুর পেশকৃত 'বাঙালির সনদের দাবি' ৬ দফা আন্দোলনের ঐতিহাসিক সূচনা।",
            descriptionEn = "Honoring 1966 historic 6-point demand movement led by Bangabandhu Sheikh Mujibur Rahman.",
            searchQuery = "Historic 6 point movement East Pakistan Bangabandhu 1966",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 6, day = 8,
            titleBn = "বিশ্ব মহাসাগর দিবস (World Oceans Day)",
            titleEn = "World Oceans Day",
            categoryBn = "পরিবেশ দিবস",
            categoryEn = "Oceans Day",
            descriptionBn = "সাগরের বাস্তুতন্ত্র রক্ষা ও সামুদ্রিক সম্পদ সংরক্ষণে আন্তর্জাতিক সচেতনতা বৃদ্ধি।",
            descriptionEn = "Highlighting the critical role oceans play in sustaining life on Earth.",
            searchQuery = "World Oceans Day marine conservation UN",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 6, day = 14,
            titleBn = "বিশ্ব রক্তদাতা দিবস (World Blood Donor Day)",
            titleEn = "World Blood Donor Day",
            categoryBn = "মানবসেবা ও স্বাস্থ্য",
            categoryEn = "Health & Blood Donation",
            descriptionBn = "রক্তের এবিও গ্রুপ আবিষ্কারক কার্ল ল্যান্ডস্টাইনারের জন্মদিনে স্বেচ্ছায় রক্তদাতাদের প্রতি শ্রদ্ধাঞ্জলি।",
            descriptionEn = "Thanking voluntary, unpaid blood donors for their life-saving gifts of blood.",
            searchQuery = "World Blood Donor Day Karl Landsteiner WHO",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 6, day = 21,
            titleBn = "আন্তর্জাতিক যোগ দিবস & বছরের দীর্ঘতম দিন",
            titleEn = "International Yoga Day & Summer Solstice",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "Health & Astronomy",
            descriptionBn = "উত্তর গোলার্ধের দীর্ঘতম দিন এবং শারীরিক ও মানসিক সুস্থতায় আন্তর্জাতিক যোগ দিবস।",
            descriptionEn = "Celebrating holistic health through Yoga & marking the longest day of the year in Northern Hemisphere.",
            searchQuery = "International Yoga Day Summer Solstice astronomy",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 6, day = 23,
            titleBn = "ঐতিহাসিক পলাশী দিবস (১৭৫৭)",
            titleEn = "Historic Palashi Day (1757)",
            categoryBn = "ইতিহাস স্মরণ",
            categoryEn = "History Remembrance",
            descriptionBn = "১৭৫৭ সালের ২৩শে জুন পলাশীর আম্রকাননে নবাব সিরাজউদ্দৌলার পরাজয়ে বাংলা স্বাধীনতা হারায়।",
            descriptionEn = "Remembering Nawab Siraj ud-Daulah and the Battle of Plassey in 1757.",
            searchQuery = "Battle of Plassey 1757 Nawab Siraj ud-Daulah history",
            icon = Icons.Default.Flag
        ),

        // === JULY ===
        SpecialDayEvent(
            month = 7, day = 1,
            titleBn = "ঢাকা বিশ্ববিদ্যালয় দিবস (১৯২১)",
            titleEn = "Dhaka University Day",
            categoryBn = "শিক্ষা ও ইতিহাস",
            categoryEn = "Education & Heritage",
            descriptionBn = "১৯২১ সালের ১লা জুলাই প্রাচ্যের অক্সফোর্ড খ্যাত ঢাকা বিশ্ববিদ্যালয়ের গৌরবময় শিক্ষা যাত্রার শুভ সূচনা।",
            descriptionEn = "Celebrating the foundation day of University of Dhaka (est. 1921).",
            searchQuery = "Dhaka University foundation history 1921",
            icon = Icons.Default.School
        ),
        SpecialDayEvent(
            month = 7, day = 11,
            titleBn = "বিশ্ব জনসংখ্যা দিবস (World Population Day)",
            titleEn = "World Population Day",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "International Day",
            descriptionBn = "বিশ্বের ক্রমবর্ধমান জনসংখ্যা ও পরিকল্পিত পরিবার গঠনের সচেতনতায় জাতিসংঘ দিবস।",
            descriptionEn = "Focusing attention on urgency and importance of population issues.",
            searchQuery = "World Population Day UN family planning",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 7, day = 18,
            titleBn = "নেলসন ম্যান্ডেলা আন্তর্জাতিক দিবস",
            titleEn = "Nelson Mandela International Day",
            categoryBn = "শান্তি ও মানবতা",
            categoryEn = "Peace & Humanity",
            descriptionBn = "বর্ণবাদবিরোধী আন্দোলনের রূপকার ও নোবেল শান্তি বিজয়ী নেলসন ম্যান্ডেলার জীবনের স্মরণে দিবস।",
            descriptionEn = "Honoring anti-apartheid leader Nelson Mandela's values and dedication to human service.",
            searchQuery = "Nelson Mandela biography apartheid Nobel Peace Prize",
            icon = Icons.Default.Star,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 7, day = 20,
            titleBn = "আন্তর্জাতিক দাবা দিবস & প্রথম চাঁদে অবতরণ (১৯৬৯)",
            titleEn = "International Chess Day & Moon Landing",
            categoryBn = "বিজ্ঞান ও খেলাধুলো",
            categoryEn = "Science & Games",
            descriptionBn = "১৯৬৯ সালে অ্যাপোলো ১১ তে চড়ে নীল আর্মস্ট্রংয়ের চাঁদে প্রথম পদক্ষেপের ঐতিহাসিক সাফল্য।",
            descriptionEn = "Celebrating Apollo 11 moon landing in 1969 & International Chess Day.",
            searchQuery = "Apollo 11 Moon landing 1969 Neil Armstrong International Chess Day",
            icon = Icons.Default.Science
        ),
        SpecialDayEvent(
            month = 7, day = 28,
            titleBn = "বিশ্ব হেপাটাইটিস দিবস (World Hepatitis Day)",
            titleEn = "World Hepatitis Day",
            categoryBn = "স্বাস্থ্য সচেতনতা",
            categoryEn = "Health Awareness",
            descriptionBn = "হেপাটাইটিস ভাইরাস প্রতিরোধ ও লিভার সুস্থ রাখার চিকিৎসা সচেতনতায় বৈশ্বিক দিবস।",
            descriptionEn = "Raising awareness about viral hepatitis and global elimination efforts.",
            searchQuery = "World Hepatitis Day WHO liver health",
            icon = Icons.Default.Star
        ),

        // === AUGUST ===
        SpecialDayEvent(
            month = 8, day = 5,
            titleBn = "জাতীয় ছাত্র-জনতার অভ্যুত্থান ও স্বাধীনতা দিবস (২০২৪)",
            titleEn = "Mass Uprising Victory Day 2024",
            categoryBn = "ঐতিহাসিক জাতীয় দিবস",
            categoryEn = "National Historic Victory",
            descriptionBn = "২০২৪ সালের ঐতিহাসিক ৫ই আগস্ট ছাত্র-জনতার অভূতপূর্ব বীরত্বপূর্ণ গণঅভ্যুত্থানে অর্জিত নতুন বাংলাদেশ।",
            descriptionEn = "Commemorating the historic student-led mass uprising and victory of democracy in Bangladesh 2024.",
            searchQuery = "Bangladesh August 5 mass uprising 2024 history",
            icon = Icons.Default.Flag,
            isFestival = true
        ),
        SpecialDayEvent(
            month = 8, day = 6,
            titleBn = "হিরোশিমা দিবস (পারমাণবিক বিপর্যয় স্মরণ)",
            titleEn = "Hiroshima Day",
            categoryBn = "শান্তি ও পরমাণু বিরোধী",
            categoryEn = "Peace & Anti-Nuclear",
            descriptionBn = "১৯৪৫ সালের ২য় বিশ্বযুদ্ধে হিরোশিমায় পারমাণবিক বোমার ধ্বংসযজ্ঞের স্মরণে বিশ্ব শান্তি দিবস।",
            descriptionEn = "Promoting nuclear disarmament and remembering victims of atomic bomb on Hiroshima.",
            searchQuery = "Hiroshima Day 1945 atomic bomb history",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 8, day = 12,
            titleBn = "আন্তর্জাতিক যুব দিবস (International Youth Day)",
            titleEn = "International Youth Day",
            categoryBn = "আন্তর্জাতিক যুব শক্তি",
            categoryEn = "Youth Empowerment",
            descriptionBn = "তরুণ প্রজন্মের নেতৃত্ব, শিক্ষা ও সমাজ পরিবর্তনের ভূমিকাকে উৎসাহিত করার জাতিসংঘ দিন।",
            descriptionEn = "Celebrating youth as essential partners in global change and development.",
            searchQuery = "International Youth Day UN youth empowerment",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 8, day = 15,
            titleBn = "জাতীয় শোক দিবস (বঙ্গবন্ধুর প্রয়াণ দিবস)",
            titleEn = "National Mourning Day Bangladesh",
            categoryBn = "জাতীয় শোক দিবস",
            categoryEn = "National Mourning Day",
            descriptionBn = "১৯৭৫ সালের ১৫ই আগস্ট সপরিবারে নিহত জাতির পিতা শেখ মুজিবুর রহমান ও শহীদদের স্মরণে বিনম্র শ্রদ্ধাঞ্জলি।",
            descriptionEn = "Solemnly remembering Bangabandhu Sheikh Mujibur Rahman and family members martyred in 1975.",
            searchQuery = "National Mourning Day 15 August Bangladesh history",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 8, day = 19,
            titleBn = "বিশ্ব আলোকচিত্র দিবস & মানবিক দিবস",
            titleEn = "World Photography Day & Humanitarian Day",
            categoryBn = "শিল্প ও মানবিকতা",
            categoryEn = "Art & Humanitarian",
            descriptionBn = "ফটোগ্রাফির শিল্পকলা উদযাপন এবং দুঃসময়ে আত্মত্যাগী মানবিক কর্মীদের সম্মান জানানো।",
            descriptionEn = "Celebrating the art, craft and passion of photography & honoring humanitarian workers.",
            searchQuery = "World Photography Day Daguerreotype World Humanitarian Day",
            icon = Icons.Default.AutoAwesome
        ),
        SpecialDayEvent(
            month = 8, day = 27,
            titleBn = "জাতীয় কবি কাজী নজরুল ইসলামের প্রয়াণ দিবস",
            titleEn = "National Poet Kazi Nazrul Islam Death Anniversary",
            categoryBn = "জাতীয় শোক ও সাহিত্য",
            categoryEn = "Literature & Remembrance",
            descriptionBn = "১৯৭৬ সালের ২৭শে আগস্ট আমাদের জাতীয় কবি কাজী নজরুল ইসলাম ধানমন্ডিতে শেষ নিঃশ্বাস ত্যাগ করেন।",
            descriptionEn = "Commemorating the legacy and passing of National Poet Kazi Nazrul Islam.",
            searchQuery = "Kazi Nazrul Islam death anniversary Bangladesh",
            icon = Icons.Default.School
        ),

        // === SEPTEMBER ===
        SpecialDayEvent(
            month = 9, day = 8,
            titleBn = "আন্তর্জাতিক সাক্ষরতা দিবস (Literacy Day)",
            titleEn = "International Literacy Day",
            categoryBn = "আন্তর্জাতিক শিক্ষা দিবস",
            categoryEn = "Education & Literacy",
            descriptionBn = "ইউনেস্কো ঘোষিত নিরক্ষরতা দূরীকরণ ও সবার জন্য মানসম্পন্ন শিক্ষার গুরুত্ব বৃদ্ধির দিবস।",
            descriptionEn = "Reminding the public of the importance of literacy as a matter of dignity and human rights.",
            searchQuery = "International Literacy Day UNESCO",
            icon = Icons.Default.School
        ),
        SpecialDayEvent(
            month = 9, day = 15,
            titleBn = "আন্তর্জাতিক গণতন্ত্র দিবস (Democracy Day)",
            titleEn = "International Day of Democracy",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "International Day",
            descriptionBn = "বিশ্বজুড়ে গণতন্ত্রের মূলনীতি ও জনগণের অধিকার সুরক্ষার সচেতনতা বাড়াতে জাতিসংঘ দিবস।",
            descriptionEn = "Providing opportunity to review the state of democracy in the world.",
            searchQuery = "International Day of Democracy UN",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 9, day = 16,
            titleBn = "বিশ্ব ওজন স্তর রক্ষা দিবস (Ozone Layer Day)",
            titleEn = "International Day for Preservation of Ozone Layer",
            categoryBn = "পরিবেশ সংরক্ষণ",
            categoryEn = "Environment Conservation",
            descriptionBn = "মনট্রিঅল প্রোটোকল স্মরণে বায়ুমণ্ডলের অতিবেগুনি রশ্মির ক্ষতিকর প্রভাব থেকে সুরক্ষার আন্তর্জাতিক দিন।",
            descriptionEn = "Commemorating the signing of Montreal Protocol to protect Earth's ozone layer.",
            searchQuery = "International Day for the Preservation of the Ozone Layer Montreal Protocol",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 9, day = 21,
            titleBn = "আন্তর্জাতিক শান্তি দিবস (International Day of Peace)",
            titleEn = "International Day of Peace",
            categoryBn = "বিশ্ব শান্তি দিবস",
            categoryEn = "World Peace Day",
            descriptionBn = "বিশ্বজুড়ে সংঘাত নিরসন, যুদ্ধবিরতি ও শান্তি প্রতিষ্ঠার সচেতনতায় জাতিসংঘের আহ্বান।",
            descriptionEn = "Strengthening the ideals of peace among all nations and peoples.",
            searchQuery = "International Day of Peace UN ceasefire",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 9, day = 27,
            titleBn = "বিশ্ব পর্যটন দিবস (World Tourism Day)",
            titleEn = "World Tourism Day",
            categoryBn = "পর্যটন ও সংস্কৃতি",
            categoryEn = "Tourism & Travel",
            descriptionBn = "পর্যটন শিল্পের মাধ্যমে অর্থনীতি সমৃদ্ধি ও প্রাকৃতিক রূপ বৈচিত্র্য তুলে ধরার বৈশ্বিক দিন।",
            descriptionEn = "Fostering awareness among international community of the importance of tourism.",
            searchQuery = "World Tourism Day UNWTO",
            icon = Icons.Default.Public
        ),

        // === OCTOBER ===
        SpecialDayEvent(
            month = 10, day = 2,
            titleBn = "আন্তর্জাতিক অহিংসা দিবস & মহাত্মা গান্ধীর জন্মবার্ষিকী",
            titleEn = "International Day of Non-Violence & Gandhi Birthday",
            categoryBn = "আন্তর্জাতিক শান্তি",
            categoryEn = "Peace & Non-Violence",
            descriptionBn = "অহিংস আন্দোলনের রূপকার মহাত্মা গান্ধীর জন্মদিনে বিশ্বজুড়ে শান্তি ও অহিংসার বার্তা ছড়িয়ে দেওয়ার দিন।",
            descriptionEn = "Disseminating the message of non-violence through education and public awareness.",
            searchQuery = "International Day of Non-Violence Mahatma Gandhi",
            icon = Icons.Default.Star,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 10, day = 5,
            titleBn = "বিশ্ব শিক্ষক দিবস (World Teachers' Day)",
            titleEn = "World Teachers' Day",
            categoryBn = "শিক্ষা ও শ্রদ্ধা",
            categoryEn = "Education & Honor",
            descriptionBn = "মানুষ গড়ার কারিগর আমাদের সম্মানীয় শিক্ষকদের অসামান্য অবদানের প্রতি শ্রদ্ধা জানানোর দিবস।",
            descriptionEn = "Honoring teachers worldwide for their vital contribution to society & education.",
            searchQuery = "World Teachers Day UNESCO recommendation",
            icon = Icons.Default.School
        ),
        SpecialDayEvent(
            month = 10, day = 10,
            titleBn = "বিশ্ব মানসিক স্বাস্থ্য দিবস (Mental Health Day)",
            titleEn = "World Mental Health Day",
            categoryBn = "স্বাস্থ্য ও সচেতনতা",
            categoryEn = "Mental Health Awareness",
            descriptionBn = "মানসিক স্বাস্থ্যের যত্ন ও বিষণ্নতা দূরীকরণে বিশ্বজুড়ে সচেতনতা বৃদ্ধির আন্তর্জাতিক দিবস।",
            descriptionEn = "Raising awareness of mental health issues around the world & mobilizing support.",
            searchQuery = "World Mental Health Day WHO awareness",
            icon = Icons.Default.Psychology
        ),
        SpecialDayEvent(
            month = 10, day = 16,
            titleBn = "বিশ্ব খাদ্য দিবস (World Food Day)",
            titleEn = "World Food Day",
            categoryBn = "আন্তর্জাতিক খাদ্য নিরাপত্তা",
            categoryEn = "Food Security",
            descriptionBn = "জাতিসংঘের খাদ্য ও কৃষি সংস্থার (FAO) উদ্যোগে বিশ্ব ক্ষুধা মুক্ত করা ও পুষ্টিকর খাবারের সচেতনতা দিন।",
            descriptionEn = "Promoting worldwide awareness and action for those who suffer from hunger.",
            searchQuery = "World Food Day FAO hunger reduction",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 10, day = 24,
            titleBn = "জাতিসংঘ দিবস (United Nations Day)",
            titleEn = "United Nations Day",
            categoryBn = "আন্তর্জাতিক কূটনীতি",
            categoryEn = "UN & Global Peace",
            descriptionBn = "১৯৪৫ সালের ২৪শে অক্টোবর জাতিসংঘের সনদ কার্যকরের মাধ্যমে বিশ্ব শান্তি সংস্থার আত্মপ্রকাশ।",
            descriptionEn = "Marks the anniversary of the entry into force of the UN Charter in 1945.",
            searchQuery = "United Nations Day UN Charter 1945 history",
            icon = Icons.Default.Public
        ),

        // === NOVEMBER ===
        SpecialDayEvent(
            month = 11, day = 3,
            titleBn = "ঐতিহাসিক জেল হত্যা দিবস (১৯৭৫)",
            titleEn = "Jail Killing Day Bangladesh",
            categoryBn = "জাতীয় শোক দিবস",
            categoryEn = "National Historic Day",
            descriptionBn = "১৯৭৫ সালের ৩রা নভেম্বর ঢাকা কেন্দ্রীয় কারাগারে নিহত জাতীয় চার নেতার প্রতি গভীর শ্রদ্ধাঞ্জলি।",
            descriptionEn = "Commemorating four national leaders martyred in Dhaka Central Jail in 1975.",
            searchQuery = "Jail Killing Day 3 November 1975 Bangladesh history",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 11, day = 10,
            titleBn = "শহীদ নূর হোসেন দিবস (গণতন্ত্র পুনরুদ্ধার দিবস)",
            titleEn = "Shaheed Noor Hossain Day",
            categoryBn = "জাতীয় গণতন্ত্র দিবস",
            categoryEn = "Democracy Remembrance",
            descriptionBn = "১৯৮৭ সালের স্বৈরাচারবিরোধী আন্দোলনে পিঠে 'স্বৈরাচার নিপাত যাক, গণতন্ত্র মুক্তি পাক' লেখা শহীদ নূর হোসেন।",
            descriptionEn = "Honoring Noor Hossain who sacrificed his life for pro-democracy movement in 1987.",
            searchQuery = "Noor Hossain Shaheed Day democracy movement 1987",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 11, day = 14,
            titleBn = "বিশ্ব ডায়াবেটিস দিবস (World Diabetes Day)",
            titleEn = "World Diabetes Day",
            categoryBn = "স্বাস্থ্য সচেতনতা",
            categoryEn = "Health Awareness",
            descriptionBn = "ইনসুলিনের সহ-আবিষ্কারক ফ্রেডরিক বান্টিংয়ের জন্মদিনে ডায়াবেটিস নিয়ন্ত্রণে আন্তর্জাতিক প্রতিরোধ সচেতনতা।",
            descriptionEn = "Primary global awareness campaign focusing on diabetes mellitus prevention & care.",
            searchQuery = "World Diabetes Day Frederick Banting IDF",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 11, day = 21,
            titleBn = "বাংলাদেশের সশস্ত্র বাহিনী দিবস",
            titleEn = "Armed Forces Day Bangladesh",
            categoryBn = "জাতীয় বাহিনী দিবস",
            categoryEn = "National Armed Forces Day",
            descriptionBn = "১৯৭১ সালের স্বাধীনতা যুদ্ধে সেনা, নৌ ও বিমান বাহিনীর সমন্বিত পাকিস্তানি দখলদারদের বিরুদ্ধে সর্বাত্মক যৌথ আক্রমণ।",
            descriptionEn = "Commemorating the joint operations of Bangladesh Army, Navy & Air Force in 1971 war.",
            searchQuery = "Armed Forces Day Bangladesh 21 November 1971",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 11, day = 30,
            titleBn = "আচার্য জগদীশচন্দ্র বসুর জন্মবার্ষিকী",
            titleEn = "Acharya Jagadish Chandra Bose Birthday",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "উদ্ভিদের প্রাণ ও বেতার তরঙ্গের প্রথম গবেষক বাঙালি বৈজ্ঞানিক আচার্য জগদীশচন্দ্র বসুর জন্মদিবস।",
            descriptionEn = "Celebrating pioneer Bengali polymath & radio wave research scientist Sir J.C. Bose.",
            searchQuery = "Jagadish Chandra Bose plant response radio waves biography",
            icon = Icons.Default.Science,
            isBirthday = true
        ),

        // === DECEMBER ===
        SpecialDayEvent(
            month = 12, day = 1,
            titleBn = "বিশ্ব এইডস দিবস (World AIDS Day)",
            titleEn = "World AIDS Day",
            categoryBn = "স্বাস্থ্য সচেতনতা",
            categoryEn = "Health Awareness",
            descriptionBn = "এইচআইভি এইচআইভি সংক্রমণ প্রতিরোধ ও রোগীদের পাশে দাঁড়াতে আন্তর্জাতিক স্বাস্থ্য সচেতনতা দিবস।",
            descriptionEn = "Raising awareness of the AIDS pandemic caused by HIV infection.",
            searchQuery = "World AIDS Day WHO awareness",
            icon = Icons.Default.Star
        ),
        SpecialDayEvent(
            month = 12, day = 4,
            titleBn = "সত্যেন্দ্রনাথ বসুর জন্মবার্ষিকী (বোসন কণা আবিষ্কার)",
            titleEn = "Satyendra Nath Bose's Birthday",
            categoryBn = "পদার্থবিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Physicist Birthday",
            descriptionBn = "আইনস্টাইনের সাথে যৌথ বোস-আইনস্টাইন সংখ্যায়ন ও বোসন কণার জনক বিশ্ববরেণ্য পদার্থবিজ্ঞানী সত্যেন্দ্রনাথ বসু।",
            descriptionEn = "Celebrating Indian physicist Satyendra Nath Bose, pioneer of Bose-Einstein statistics & Boson particle.",
            searchQuery = "Satyendra Nath Bose Boson particle physics biography",
            icon = Icons.Default.Science,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 12, day = 9,
            titleBn = "বেগম রোকেয়া দিবস (নারী জাগরণের পথিকৃৎ)",
            titleEn = "Begum Rokeya Day",
            categoryBn = "জাতীয় দিবস ও নারী জাগরণ",
            categoryEn = "Pioneer of Women's Rights",
            descriptionBn = "উপমহাদেশের মুসলিম নারী শিক্ষার কান্ডারি ও সাহিত্যিক বেগম রোকেয়া সাখাওয়াত হোসেনের স্মরণে দিবস।",
            descriptionEn = "Commemorating Begum Rokeya Sakhawat Hossain, pioneer of Bengali women's education & rights.",
            searchQuery = "Begum Rokeya Day 9 December Sultana Dream biography",
            icon = Icons.Default.School,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 12, day = 10,
            titleBn = "আন্তর্জাতিক মানবাধিকার দিবস (Human Rights Day)",
            titleEn = "Human Rights Day",
            categoryBn = "আন্তর্জাতিক মানবাধিকার",
            categoryEn = "Human Rights",
            descriptionBn = "১৯৪৮ সালের এই দিনে জাতিসংঘের সর্বজনীন মানবাধিকার ঘোষণার বিশ্বজুড়ে অধিকার দিবস।",
            descriptionEn = "Commemorating the adoption of Universal Declaration of Human Rights by UN in 1948.",
            searchQuery = "Human Rights Day Universal Declaration UN",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 12, day = 14,
            titleBn = "শহীদ বুদ্ধিজীবী দিবস (১৯৭১)",
            titleEn = "Martyred Intellectuals Day Bangladesh",
            categoryBn = "জাতীয় শোক দিবস",
            categoryEn = "National Historic Day",
            descriptionBn = "১৯৭১ সালের ১৪ই ডিসেম্বর পাকিস্তানি হানাদার ও রাজাকার বাহিনী কর্তৃক বরেণ্য শিক্ষক, ডাক্তার ও সাংবাদিকদের নির্মম হত্যাকাণ্ড।",
            descriptionEn = "Solemnly remembering the nation's finest minds martyred by occupation forces in 1971 war.",
            searchQuery = "Martyred Intellectuals Day 14 December 1971 Bangladesh history",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 12, day = 16,
            titleBn = "বাংলাদেশের মহান বিজয় দিবস",
            titleEn = "Victory Day of Bangladesh",
            categoryBn = "মহান জাতীয় বিজয় দিবস",
            categoryEn = "Victory Day",
            descriptionBn = "১৬ই ডিসেম্বর আমাদের অহংকার! ৯ মাসের রক্তক্ষয়ী যুদ্ধে অর্জিত স্বপ্নের স্বাধীন বাংলাদেশ। জয় বাংলা!",
            descriptionEn = "Celebrating the glorious Victory Day of Bangladesh in 1971 Liberation War.",
            searchQuery = "Bangladesh Victory Day December 16 history",
            icon = Icons.Default.Flag,
            isFestival = true
        ),
        SpecialDayEvent(
            month = 12, day = 22,
            titleBn = "শ্রীনিবাস রামানুজনের জন্মবার্ষিকী (জাতীয় গণিত দিবস)",
            titleEn = "Srinivasa Ramanujan Birthday (Math Day)",
            categoryBn = "গণিতবিদ জন্মবার্ষিকী",
            categoryEn = "Mathematician Birthday",
            descriptionBn = "গণিতের বিস্ময়কর প্রতিভা শ্রীনিবাস রামানুজনের জন্মবার্ষিকীতে উদযাপিত গণিত দিবস।",
            descriptionEn = "Celebrating mathematical genius Srinivasa Ramanujan on National Mathematics Day.",
            searchQuery = "Srinivasa Ramanujan biography mathematics discoveries",
            icon = Icons.Default.Science,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 12, day = 25,
            titleBn = "শুভ বড়দিন (Christmas Day)",
            titleEn = "Merry Christmas!",
            categoryBn = "উৎসব ও শুভকামনা",
            categoryEn = "Festival & Greetings",
            descriptionBn = "খ্রিস্টধর্মাবলম্বীদের পবিত্র উৎসব শুভ বড়দিন উপলক্ষে সবাইকে আন্তরিক প্রীতি ও শুভেচ্ছা!",
            descriptionEn = "Wishing everyone joy, peace and happiness on Christmas Day!",
            searchQuery = "Christmas Day tradition and celebrations",
            icon = Icons.Default.Celebration,
            isFestival = true
        )
    )

    private var activeSpecialEventsList: List<SpecialDayEvent> = defaultSpecialEventsList
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        loadFromLocalCache(context)

        // Asynchronously check network & fetch updated database from CDN if online
        GlobalScope.launch(Dispatchers.IO) {
            syncFromOnlineIfAvailable(context)
        }
    }

    private fun loadFromLocalCache(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_CACHED_EVENTS_JSON, null)
            if (!jsonStr.isNullOrEmpty()) {
                val parsed = parseEventsJson(jsonStr)
                if (parsed.isNotEmpty()) {
                    activeSpecialEventsList = parsed
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncFromOnlineIfAvailable(context: Context) {
        if (!NetworkUtil.isOnline(context)) return

        withContext(Dispatchers.IO) {
            try {
                val url = URL(CDN_EVENTS_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.doInput = true

                if (connection.responseCode == 200) {
                    val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                    val parsedEvents = parseEventsJson(jsonStr)
                    if (parsedEvents.isNotEmpty()) {
                        activeSpecialEventsList = parsedEvents
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(KEY_CACHED_EVENTS_JSON, jsonStr)
                            .apply()
                    }
                }
            } catch (e: Exception) {
                // Network unavailable or server unreachable: fallback gracefully to current active list
                e.printStackTrace()
            }
        }
    }

    private fun parseEventsJson(jsonStr: String): List<SpecialDayEvent> {
        val list = mutableListOf<SpecialDayEvent>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val month = obj.getInt("month")
                val day = obj.getInt("day")
                val titleBn = obj.getString("titleBn")
                val titleEn = obj.getString("titleEn")
                val categoryBn = obj.optString("categoryBn", "দিবস ও উৎসব")
                val categoryEn = obj.optString("categoryEn", "Day & Festival")
                val descriptionBn = obj.optString("descriptionBn", "")
                val descriptionEn = obj.optString("descriptionEn", "")
                val searchQuery = obj.optString("searchQuery", "$titleEn history")
                val iconKey = obj.optString("iconKey", "celebration")
                val isBirthday = obj.optBoolean("isBirthday", false)
                val isFestival = obj.optBoolean("isFestival", false)

                list.add(
                    SpecialDayEvent(
                        month = month,
                        day = day,
                        titleBn = titleBn,
                        titleEn = titleEn,
                        categoryBn = categoryBn,
                        categoryEn = categoryEn,
                        descriptionBn = descriptionBn,
                        descriptionEn = descriptionEn,
                        searchQuery = searchQuery,
                        icon = mapIconKeyToVector(iconKey),
                        isBirthday = isBirthday,
                        isFestival = isFestival
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun mapIconKeyToVector(key: String): ImageVector {
        return when (key.lowercase()) {
            "science" -> Icons.Default.Science
            "birthday", "cake" -> Icons.Default.Cake
            "flag" -> Icons.Default.Flag
            "school" -> Icons.Default.School
            "star" -> Icons.Default.Star
            "public" -> Icons.Default.Public
            "psychology" -> Icons.Default.Psychology
            "awesome" -> Icons.Default.AutoAwesome
            "search" -> Icons.Default.Search
            else -> Icons.Default.Celebration
        }
    }

    fun getTodaySpecialEvents(calendar: Calendar = Calendar.getInstance()): List<SpecialDayEvent> {
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // 1-12
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val exactMatches = activeSpecialEventsList.filter { it.month == currentMonth && it.day == currentDay }
        if (exactMatches.isNotEmpty()) {
            return exactMatches
        }

        // Fallback search option when there are no specific hardcoded events for today
        val monthNamesBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
        val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val monthBn = monthNamesBn.getOrElse(currentMonth - 1) { "" }
        val monthEn = monthNamesEn.getOrElse(currentMonth - 1) { "" }

        val searchFallbackEvent = SpecialDayEvent(
            month = currentMonth,
            day = currentDay,
            titleBn = "$currentDay $monthBn: ইতিহাস ও বিশেষ ঘটনা অনুসন্ধান",
            titleEn = "Search History & Notable Events ($monthEn $currentDay)",
            categoryBn = "ইতিহাস ও দিবস অনুসন্ধান",
            categoryEn = "Search History",
            descriptionBn = "আজকের দিনে বিশ্বজুড়ে ঘটে যাওয়া উল্লেখযোগ্য ইতিহাস, আবিষ্কার বা বিজ্ঞানীদের দিবস গুগলে খুঁজুন।",
            descriptionEn = "Explore notable historical events, famous birthdays and discoveries on this day.",
            searchQuery = "What happened on $monthEn $currentDay in world history famous events",
            icon = Icons.Default.Search,
            isSearchFallback = true
        )

        return listOf(searchFallbackEvent)
    }

    fun getTodaySpecialEvent(calendar: Calendar = Calendar.getInstance()): SpecialDayEvent {
        return getTodaySpecialEvents(calendar).first()
    }

    fun getAllSpecialEvents(): List<SpecialDayEvent> {
        return activeSpecialEventsList
    }
}

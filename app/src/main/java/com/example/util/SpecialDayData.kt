package com.example.util

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

    private val specialEventsList = listOf(
        // January
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

        // February
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
            titleBn = "আন্তর্জাতিক মাতৃভাষা দিবস",
            titleEn = "International Mother Language Day",
            categoryBn = "আন্তর্জাতিক ও জাতীয় দিবস",
            categoryEn = "National & Int'l Day",
            descriptionBn = "১৯৫২ সালের মহান ভাষা শহীদের আত্মত্যাগের স্মরণে বিশ্বজুড়ে পালিত মাতৃভাষা দিবস। মোদের গরব মোদের আশা, আমরি বাংলা ভাষা!",
            descriptionEn = "Honoring the martyrs of 1952 Language Movement & promoting linguistic diversity.",
            searchQuery = "International Mother Language Day history 1952",
            icon = Icons.Default.Flag
        ),

        // March
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
            titleEn = "Albert Einstein Birthday & Pi Day",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "আপেক্ষিকতা তত্ত্বের জনক আলবার্ট আইনস্টাইনের জন্মবার্ষিকী এবং গণিতের ধ্রুবক পাই (3.14) দিবস।",
            descriptionEn = "Celebrating legendary physicist Albert Einstein's birthday & International Pi Day.",
            searchQuery = "Albert Einstein theory of relativity Pi Day",
            icon = Icons.Default.Psychology,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 3, day = 17,
            titleBn = "বঙ্গবন্ধু শেখ মুজিবুর রহমানের জন্মবার্ষিকী ও জাতীয় শিশু দিবস",
            titleEn = "Bangabandhu Birthday & National Children's Day",
            categoryBn = "জাতীয় দিবস",
            categoryEn = "National Day",
            descriptionBn = "স্বাধীন বাংলাদেশের স্থপতি জাতির পিতা বঙ্গবন্ধু শেখ মুজিবুর রহমানের জন্মবার্ষিকী ও শিশু দিবস।",
            descriptionEn = "Celebrating the birth anniversary of Bangabandhu Sheikh Mujibur Rahman & Children's Day.",
            searchQuery = "Bangabandhu Sheikh Mujibur Rahman biography Children's Day",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 3, day = 26,
            titleBn = "বাংলাদেশের স্বাধীনতা ও জাতীয় দিবস",
            titleEn = "Independence Day of Bangladesh",
            categoryBn = "মহান জাতীয় দিবস",
            categoryEn = "National Day",
            descriptionBn = "১৯৭১ সালের এই দিনে বীর বাঙ্গালি স্বাধীনতার ডাক দিয়েছিল। বীর শহীদদের বিনম্র শ্রদ্ধা!",
            descriptionEn = "Commemorating the independence declaration of Bangladesh in 1971.",
            searchQuery = "Bangladesh Independence Day 1971 history",
            icon = Icons.Default.Flag
        ),

        // April
        SpecialDayEvent(
            month = 4, day = 7,
            titleBn = "বিশ্ব স্বাস্থ্য দিবস",
            titleEn = "World Health Day",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "International Day",
            descriptionBn = "সবার জন্য সুস্বাস্থ্য নিশ্চিতকরণ ও স্বাস্থ্য সচেতনতা বৃদ্ধিতে বিশ্ব স্বাস্থ্য সংস্থা ঘোষিত দিবস।",
            descriptionEn = "Promoting global health awareness and equal healthcare access for all.",
            searchQuery = "World Health Day theme and facts",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 4, day = 14,
            titleBn = "পহেলা বৈশাখ (বাংলা নববর্ষ)",
            titleEn = "Pohela Boishakh (Bengali New Year)",
            categoryBn = "জাতীয় উৎসব",
            categoryEn = "National Festival",
            descriptionBn = "এসো হে বৈশাখ এসো এসো! নতুন বাংলা বছরের আনন্দ ও ঐতিহ্যে সবাইকে শুভ নববর্ষের প্রীতি ও শুভেচ্ছা!",
            descriptionEn = "Happy Bengali New Year! Celebrating rich Bengali culture, tradition and unity.",
            searchQuery = "Pohela Boishakh tradition history celebration",
            icon = Icons.Default.Celebration,
            isFestival = true
        ),
        SpecialDayEvent(
            month = 4, day = 15,
            titleBn = "লিওনার্দো দা ভিঞ্চির জন্মবার্ষিকী",
            titleEn = "Leonardo da Vinci's Birthday",
            categoryBn = "মনীষী ও চিত্রশিল্পী",
            categoryEn = "Polymath Birthday",
            descriptionBn = "মনালিসার চিত্রশিল্পী, গাণিতিক ও উদ্ভাবক রেনেসাঁসের মহান পুরুষ লিওনার্দো দা ভিঞ্চির জন্মবার্ষিকী।",
            descriptionEn = "Celebrating Renaissance genius Leonardo da Vinci, painter of Mona Lisa.",
            searchQuery = "Leonardo da Vinci paintings and inventions",
            icon = Icons.Default.AutoAwesome,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 4, day = 22,
            titleBn = "বিশ্ব ধরিত্রী দিবস (Earth Day)",
            titleEn = "International Earth Day",
            categoryBn = "পরিবেশ দিবস",
            categoryEn = "Environment Day",
            descriptionBn = "আমাদের প্রিয় পৃথিবী ও প্রকৃতিকে রক্ষা করতে পরিবেশ সচেতনতার অঙ্গীকার নেওয়ার দিন।",
            descriptionEn = "Demonstrating support for environmental protection and climate safety.",
            searchQuery = "International Earth Day climate action",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 4, day = 23,
            titleBn = "উইলিয়াম শেক্সপিয়রের জন্মবার্ষিকী",
            titleEn = "William Shakespeare's Birthday",
            categoryBn = "সাহিত্যিক জন্মবার্ষিকী",
            categoryEn = "Literary Personality",
            descriptionBn = "বিশ্বসাহিত্যের প্রখ্যাত নাট্যকার ও কবি উইলিয়াম শেক্সপিয়রের জন্ম ও প্রয়াণ স্মারক দিবস।",
            descriptionEn = "Celebrating the legendary English playwright and poet William Shakespeare.",
            searchQuery = "William Shakespeare plays biography",
            icon = Icons.Default.School,
            isBirthday = true
        ),

        // May
        SpecialDayEvent(
            month = 5, day = 1,
            titleBn = "আন্তর্জাতিক মে দিবস (শ্রমিক দিবস)",
            titleEn = "International Workers' Day",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "International Day",
            descriptionBn = "১৮৮৬ সালের শিকাগোর হে মার্কেটের শ্রমিকদের অধিকার আদায়ের লড়াই স্মরণে আন্তর্জাতিক মে দিবস।",
            descriptionEn = "Honoring the historic struggles and rights of labor and working classes worldwide.",
            searchQuery = "International Workers' Day May 1 history",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 5, day = 7,
            titleBn = "বিশ্বকবি রবীন্দ্রনাথ ঠাকুরের জন্মবার্ষিকী",
            titleEn = "Rabindranath Tagore's Birthday",
            categoryBn = "কবি জন্মবার্ষিকী",
            categoryEn = "Poet Birthday",
            descriptionBn = "নোবেলজয়ী বিশ্বকবি রবীন্দ্রনাথ ঠাকুরের ২৫শে বৈশাখ শুভ জন্মজয়ন্তী। চিত মম বিকশিত করো হে!",
            descriptionEn = "Celebrating Nobel laureate poet Rabindranath Tagore, author of National Anthems.",
            searchQuery = "Rabindranath Tagore Gitanjali Nobel Prize biography",
            icon = Icons.Default.School,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 5, day = 25,
            titleBn = "জাতীয় কবি কাজী নজরুল ইসলামের জন্মবার্ষিকী",
            titleEn = "Kazi Nazrul Islam's Birthday",
            categoryBn = "কবি জন্মবার্ষিকী",
            categoryEn = "Poet Birthday",
            descriptionBn = "বাংলাদেশের জাতীয় কবি সাম্য ও বিদ্রোহের রূপকার কাজী নজরুল ইসলামের ১১ই জ্যৈষ্ঠ শুভ জন্মজয়ন্তী।",
            descriptionEn = "Celebrating the birth of Rebel Poet & Bangladesh National Poet Kazi Nazrul Islam.",
            searchQuery = "Kazi Nazrul Islam poems rebel poet biography",
            icon = Icons.Default.School,
            isBirthday = true
        ),

        // June
        SpecialDayEvent(
            month = 6, day = 5,
            titleBn = "বিশ্ব পরিবেশ দিবস (World Environment Day)",
            titleEn = "World Environment Day",
            categoryBn = "পরিবেশ দিবস",
            categoryEn = "Environment Day",
            descriptionBn = "গাছ লাগান, পরিবেশ বাঁচান! সুন্দর ও সবুজ পৃথিবী গড়তে বিশ্বজুড়ে উদযাপিত দিবস।",
            descriptionEn = "Encouraging worldwide awareness and action to protect our natural environment.",
            searchQuery = "World Environment Day environmental protection",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 6, day = 19,
            titleBn = "ব্লেইজ পাসকালের জন্মবার্ষিকী",
            titleEn = "Blaise Pascal's Birthday",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "ফরাসি গণিতবিদ, পদার্থবিদ ও কম্পিউটার গণক যন্ত্রের প্রাথমিক উদ্ভাবক ব্লেইজ পাসকালের জন্মবার্ষিকী।",
            descriptionEn = "Celebrating mathematician & physicist Blaise Pascal, pioneer of probability & fluid pressure.",
            searchQuery = "Blaise Pascal triangle probability inventions",
            icon = Icons.Default.Science,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 6, day = 21,
            titleBn = "अंतर्राष्ट्रीय योग दिवस ও বিশ্ব সংগীত দিবস",
            titleEn = "International Yoga & Music Day",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "International Day",
            descriptionBn = "দৈহিক সুস্থতায় যোগব্যায়াম এবং আত্মার প্রশান্তিতে বিশ্ব সংগীত উদযাপনের বিশেষ দিন।",
            descriptionEn = "Celebrating the harmony of music & healthy mind-body benefits of yoga practice.",
            searchQuery = "International Yoga Day Music Day benefits",
            icon = Icons.Default.Celebration
        ),

        // July
        SpecialDayEvent(
            month = 7, day = 10,
            titleBn = "নিকোলা টেসলার জন্মবার্ষিকী",
            titleEn = "Nikola Tesla's Birthday",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "অল্টারনেটিং কারেন্ট (AC) বিদ্যুৎ ও ওয়ারলেস প্রযুক্তির মহান আবিষ্কারক নিকোলা টেসলার জন্মবার্ষিকী।",
            descriptionEn = "Commemorating visionary inventor Nikola Tesla, pioneer of AC electric power systems.",
            searchQuery = "Nikola Tesla inventions AC electricity biography",
            icon = Icons.Default.Science,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 7, day = 11,
            titleBn = "বিশ্ব জনসংখ্যা দিবস",
            titleEn = "World Population Day",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "International Day",
            descriptionBn = "বিশ্ব জনসংখ্যা বৃদ্ধি, জনমিতি ও টেকসই উন্নয়নের সচেতনতায় জাতিসংঘ ঘোষিত দিন।",
            descriptionEn = "Focusing attention on the urgency & importance of global population trends.",
            searchQuery = "World Population Day global demographics",
            icon = Icons.Default.Public
        ),

        // August
        SpecialDayEvent(
            month = 8, day = 15,
            titleBn = "জাতীয় শোক দিবস",
            titleEn = "National Mourning Day",
            categoryBn = "জাতীয় স্মরণ দিবস",
            categoryEn = "National Day",
            descriptionBn = "১৯৭৫ সালের ১৫ই আগস্টের নির্মম ট্র্যাজেডিতে জাতির পিতা ও তাঁর পরিবারের শহীদদের শ্রদ্ধাঞ্জলি।",
            descriptionEn = "Solemnly remembering the martyrs of August 15, 1975 in Bangladesh history.",
            searchQuery = "National Mourning Day August 15 Bangladesh history",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 8, day = 28,
            titleBn = "জাতীয় কবি কাজী নজরুল ইসলামের প্রয়াণ দিবস",
            titleEn = "Death Anniversary of Kazi Nazrul Islam",
            categoryBn = "জাতীয় স্মরণ দিবস",
            categoryEn = "National Remembrance",
            descriptionBn = "আজ ১২ই ভাদ্র, আমাদের জাতীয় কবি কাজী নজরুল ইসলামের প্রয়াণ দিবস। তাঁর কালজয়ী সৃষ্টি আমাদের চিরপ্রেরণা।",
            descriptionEn = "Commemorating the legacy of Rebel Poet Kazi Nazrul Islam on his death anniversary.",
            searchQuery = "Kazi Nazrul Islam death anniversary history biography",
            icon = Icons.Default.School
        ),
        SpecialDayEvent(
            month = 8, day = 28,
            titleBn = "মার্টিন লুথার কিং এর 'আই হ্যাভ আ ড্রীম' ভাষণের বার্ষিকী",
            titleEn = "Martin Luther King Jr. 'I Have a Dream' Speech",
            categoryBn = "ঐতিহাসিক স্মরণীয় ঘটনা",
            categoryEn = "Historic World Event",
            descriptionBn = "১৯৬৩ সালের ২৮শে আগস্ট ওয়াশিংটনে অনুষ্ঠিত মার্টিন লুথার কিং জুনিয়রের ঐতিহাসিক মানবাধিকার ভাষণ দিবস।",
            descriptionEn = "Commemorating Martin Luther King Jr.'s landmark 'I Have a Dream' speech in 1963.",
            searchQuery = "Martin Luther King Jr I Have a Dream speech August 28 1963 history",
            icon = Icons.Default.Public
        ),

        // September
        SpecialDayEvent(
            month = 9, day = 8,
            titleBn = "আন্তর্জাতিক সাক্ষরতা দিবস",
            titleEn = "International Literacy Day",
            categoryBn = "শিক্ষা দিবস",
            categoryEn = "Education Day",
            descriptionBn = "সবার জন্য মানসম্মত শিক্ষা ও শতভাগ সাক্ষরতা অর্জনের প্রত্যয়ে ইউনেস্কো ঘোষিত দিবস।",
            descriptionEn = "Highlighting the importance of literacy as a matter of dignity and human rights.",
            searchQuery = "International Literacy Day education importance",
            icon = Icons.Default.School
        ),
        SpecialDayEvent(
            month = 9, day = 27,
            titleBn = "বিশ্ব পর্যটন দিবস",
            titleEn = "World Tourism Day",
            categoryBn = "আন্তর্জাতিক দিবস",
            categoryEn = "International Day",
            descriptionBn = "দেশ বিদেশের বৈচিত্র্যময় সংস্কৃতি ও ভ্রমণ সাহসিকতায় পর্যটন শিল্পের অবদান উদযাপনের দিন।",
            descriptionEn = "Promoting global tourism to foster cultural understanding and economic growth.",
            searchQuery = "World Tourism Day highlights",
            icon = Icons.Default.Public
        ),
        SpecialDayEvent(
            month = 9, day = 28,
            titleBn = "লুই পাস্তুরের জন্মবার্ষিকী ও বিশ্ব জলাতঙ্ক দিবস",
            titleEn = "Louis Pasteur Birthday & World Rabies Day",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "জলাতঙ্ক ও গুটিবসন্তের টিকার উদ্ভাবক এবং পাস্তুরায়ন প্রক্রিয়ার জনক লুই পাস্তুরের স্মারক দিবস।",
            descriptionEn = "Honoring Louis Pasteur, pioneer of microbiology and life-saving vaccines.",
            searchQuery = "Louis Pasteur rabies vaccine biography",
            icon = Icons.Default.Science,
            isBirthday = true
        ),

        // October
        SpecialDayEvent(
            month = 10, day = 2,
            titleBn = "মহাত্মা গান্ধীর জন্মবার্ষিকী ও আন্তর্জাতিক অহিংসা দিবস",
            titleEn = "Mahatma Gandhi Birthday & Non-Violence Day",
            categoryBn = "মনীষী জন্মবার্ষিকী",
            categoryEn = "Leader Birthday",
            descriptionBn = "অহিংস আন্দোলনের প্রবক্তা মহাত্মা গান্ধীর জন্মদিবসে আন্তর্জাতিক অহিংসা দিবস।",
            descriptionEn = "Celebrating Mahatma Gandhi's birth and promoting peace through non-violence.",
            searchQuery = "Mahatma Gandhi non-violence movement history",
            icon = Icons.Default.Event,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 10, day = 5,
            titleBn = "বিশ্ব শিক্ষক দিবস",
            titleEn = "World Teachers' Day",
            categoryBn = "শিক্ষা ও সম্মাননা",
            categoryEn = "Teachers Day",
            descriptionBn = "মানুষ গড়ার কারিগর আমাদের প্রাণপ্রিয় শিক্ষকবৃন্দের প্রতি বিনম্র শ্রদ্ধা ও ভালোবাসা।",
            descriptionEn = "Celebrating teachers worldwide for their vital contribution to society.",
            searchQuery = "World Teachers' Day history and significance",
            icon = Icons.Default.School
        ),
        SpecialDayEvent(
            month = 10, day = 21,
            titleBn = "আলফ্রেড নোবেলের জন্মবার্ষিকী",
            titleEn = "Alfred Nobel's Birthday",
            categoryBn = "উদ্ভাবক ও নোবেল প্রবর্তক",
            categoryEn = "Inventor Birthday",
            descriptionBn = "ডাইনামাইটের উদ্ভাবক এবং বিশ্বখ্যাত নোবেল পুরষ্কারের প্রতিষ্ঠাতা আলফ্রেড নোবেলের জন্মবার্ষিকী।",
            descriptionEn = "Commemorating Swedish chemist Alfred Nobel, founder of the prestigious Nobel Prizes.",
            searchQuery = "Alfred Nobel biography dynamite Nobel Prize",
            icon = Icons.Default.Science,
            isBirthday = true
        ),

        // November
        SpecialDayEvent(
            month = 11, day = 7,
            titleBn = "মারি ক্যুরি ও স্যার সি ভি রমনের জন্মবার্ষিকী",
            titleEn = "Marie Curie & C. V. Raman Birthday",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "তেজস্ক্রিয়তার আবিষ্কারক দুইবার নোবেলজয়ী মারি ক্যুরি ও রমন ইফেক্টের আবিষ্কারক সি ভি রমনের জন্মদিন।",
            descriptionEn = "Celebrating Nobel physicists Marie Curie & Sir C. V. Raman for groundbreaking physics discoveries.",
            searchQuery = "Marie Curie radioactivity C V Raman effect",
            icon = Icons.Default.Science,
            isBirthday = true
        ),
        SpecialDayEvent(
            month = 11, day = 10,
            titleBn = "বিশ্ব বিজ্ঞান দিবস (World Science Day)",
            titleEn = "World Science Day for Peace & Development",
            categoryBn = "আন্তর্জাতিক বিজ্ঞান দিবস",
            categoryEn = "Science Day",
            descriptionBn = "শান্তি ও উন্নয়নের জন্য বিজ্ঞানের সুফল এবং বৈজ্ঞানিক চিন্তার প্রসার ঘটাতে উদযাপিত দিবস।",
            descriptionEn = "Highlighting the important role of science in society and peaceful development.",
            searchQuery = "World Science Day peace development UNESCO",
            icon = Icons.Default.Science
        ),
        SpecialDayEvent(
            month = 11, day = 30,
            titleBn = "স্যার জগদীশ চন্দ্র বসুর জন্মবার্ষিকী",
            titleEn = "Sir Jagadish Chandra Bose's Birthday",
            categoryBn = "বিজ্ঞানী জন্মবার্ষিকী",
            categoryEn = "Scientist Birthday",
            descriptionBn = "গাছের প্রাণ ও রেডিও মাইক্রোওয়েভের আবিষ্কারক বাঙালি বিজ্ঞানী স্যার জগদীশ চন্দ্র বসুর জন্মবার্ষিকী।",
            descriptionEn = "Honoring polymath Sir Jagadish Chandra Bose, pioneer of radio wave & plant physiology.",
            searchQuery = "Jagadish Chandra Bose wireless plant response biography",
            icon = Icons.Default.Science,
            isBirthday = true
        ),

        // December
        SpecialDayEvent(
            month = 12, day = 14,
            titleBn = "শহীদ বুদ্ধিজীবী দিবস",
            titleEn = "Martyred Intellectuals Day",
            categoryBn = "জাতীয় স্মরণ দিবস",
            categoryEn = "National Day",
            descriptionBn = "১৯৭১ সালে দেশের শ্রেষ্ঠ সন্তান শিক্ষক, চিকিৎসক ও সাংবাদিকদের বিনম্র শ্রদ্ধায় স্মরণ।",
            descriptionEn = "Solemnly remembering the martyred intellectuals of Bangladesh in 1971 war.",
            searchQuery = "Martyred Intellectuals Day December 14 Bangladesh",
            icon = Icons.Default.Flag
        ),
        SpecialDayEvent(
            month = 12, day = 16,
            titleBn = "বাংলাদেশের মহান বিজয় দিবস",
            titleEn = "Victory Day of Bangladesh",
            categoryBn = "মহান জাতীয় বিজয় দিবস",
            categoryEn = "Victory Day",
            descriptionBn = "১৬ই ডিসেম্বর আমাদের অহংকার! ৯ মাসের রক্তক্ষয়ী যুদ্ধে অর্জিত স্বপ্নের স্বাধীন বাংলাদেশ।",
            descriptionEn = "Celebrating the glorious Victory Day of Bangladesh in 1971 Liberation War.",
            searchQuery = "Bangladesh Victory Day December 16 history",
            icon = Icons.Default.Flag
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

    fun getTodaySpecialEvents(calendar: Calendar = Calendar.getInstance()): List<SpecialDayEvent> {
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // 1-12
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val exactMatches = specialEventsList.filter { it.month == currentMonth && it.day == currentDay }
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
        return specialEventsList
    }
}

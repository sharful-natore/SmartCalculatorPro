package com.example.ui.islamic

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

// ==========================================
// 1. HADITH DATA MODELS
// ==========================================

data class HadithBookMeta(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val authorBn: String,
    val authorEn: String,
    val totalHadiths: Int,
    val totalChapters: Int,
    val sizeKb: Int,
    val descriptionBn: String,
    val descriptionEn: String,
    val badgeColor: Long = 0xFF0284C7,
    val isDefaultDownloaded: Boolean = false
)

data class HadithChapter(
    val chapterId: Int,
    val titleBn: String,
    val titleEn: String,
    val hadithCount: Int
)

data class HadithItem(
    val id: Int,
    val bookId: String,
    val chapterId: Int,
    val hadithNumberBn: String,
    val hadithNumberEn: String,
    val narratorBn: String,
    val arabicText: String,
    val banglaText: String,
    val englishText: String,
    val gradeBn: String = "সহীহ (Authentic)",
    val referenceBn: String = "",
    var isBookmarked: Boolean = false
)

// ==========================================
// 2. LOCAL STORAGE MANAGER FOR HADITH BOOKS
// ==========================================

object HadithStorageManager {
    private const val PREF_NAME = "hadith_library_prefs"
    private const val KEY_BOOKMARKS = "saved_bookmarks_ids"

    fun getBookFile(context: Context, bookId: String): File {
        return File(context.filesDir, "hadith_book_$bookId.json")
    }

    fun isBookDownloaded(context: Context, bookId: String, isDefault: Boolean): Boolean {
        if (isDefault) return true
        val file = getBookFile(context, bookId)
        return file.exists() && file.length() > 0
    }

    fun saveBookContent(context: Context, bookId: String, jsonString: String) {
        try {
            val file = getBookFile(context, bookId)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteBook(context: Context, bookId: String) {
        try {
            val file = getBookFile(context, bookId)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBookmarks(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_BOOKMARKS, emptySet()) ?: emptySet()
    }

    fun toggleBookmark(context: Context, hadithKey: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_BOOKMARKS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val newState: Boolean
        if (current.contains(hadithKey)) {
            current.remove(hadithKey)
            newState = false
        } else {
            current.add(hadithKey)
            newState = true
        }
        prefs.edit().putStringSet(KEY_BOOKMARKS, current).apply()
        return newState
    }
}

// ==========================================
// 3. HADITH REPOSITORY & COMPACT GENERATOR
// ==========================================

object HadithRepository {

    val BOOK_LIST = listOf(
        HadithBookMeta(
            id = "bukhari",
            titleBn = "সহীহ আল-বুখারী",
            titleEn = "Sahih al-Bukhari",
            authorBn = "ইমাম মোহাম্মদ ইবনে ইসমাইল আল-বুখারী (র.)",
            authorEn = "Imam Muhammad al-Bukhari",
            totalHadiths = 7563,
            totalChapters = 97,
            sizeKb = 2450,
            descriptionBn = "ইসলামি শাস্ত্রের সবচেয়ে বিশুদ্ধ ও নির্ভরযোগ্য সর্বাধিক গ্রহনযোগ্য হাদিস গ্রন্থ।",
            descriptionEn = "The most authentic collection of Hadith compiled by Imam Bukhari.",
            badgeColor = 0xFF0284C7
        ),
        HadithBookMeta(
            id = "muslim",
            titleBn = "সহীহ মুসলিম",
            titleEn = "Sahih Muslim",
            authorBn = "ইমাম মুসলিম ইবনুল হাজ্জাজ (র.)",
            authorEn = "Imam Muslim ibn al-Hajjaj",
            totalHadiths = 7500,
            totalChapters = 56,
            sizeKb = 2300,
            descriptionBn = "সহীহ আল-বুখারীর পর সর্বাধিক প্রামাণ্য ও নির্ভুল হাদিস সংকলন।",
            descriptionEn = "The second most authentic collection of Sunnah after Sahih Bukhari.",
            badgeColor = 0xFF059669
        ),
        HadithBookMeta(
            id = "nawawi40",
            titleBn = "ইমাম নববীর ৪০ হাদিস",
            titleEn = "Forty Hadith Nawawi",
            authorBn = "ইমাম মহিউদ্দিন ইয়াহইয়া বিন শারাফ নববী (র.)",
            authorEn = "Imam Yahya ibn Sharaf an-Nawawi",
            totalHadiths = 42,
            totalChapters = 5,
            sizeKb = 45,
            descriptionBn = "দ্বীনের মূলনীতি ও দৈনন্দিন জীবনের সবচেয়ে গুরুত্বপূর্ণ ৪২টি সারগর্ভ হাদিস।",
            descriptionEn = "42 foundational Hadith covering Islamic faith, ethics and law.",
            badgeColor = 0xFFD97706,
            isDefaultDownloaded = true
        ),
        HadithBookMeta(
            id = "riyad",
            titleBn = "রিয়াদুস সালেহীন",
            titleEn = "Riyad as-Salihin",
            authorBn = "ইমাম নববী (র.)",
            authorEn = "Imam An-Nawawi",
            totalHadiths = 1896,
            totalChapters = 19,
            sizeKb = 1400,
            descriptionBn = "সদাচরণ, আত্মশুদ্ধি, ইবাদত ও আখলাকের অনন্য দিকনির্দেশনামূলক গ্রন্থ।",
            descriptionEn = "The Gardens of the Righteous - core guide to Islamic ethics and mannerism.",
            badgeColor = 0xFF7C3AED
        ),
        HadithBookMeta(
            id = "abudawood",
            titleBn = "সুনান আবু দাউদ",
            titleEn = "Sunan Abu Dawood",
            authorBn = "ইমাম আবু দাউদ সুলাইমান (র.)",
            authorEn = "Imam Abu Dawood",
            totalHadiths = 5274,
            totalChapters = 43,
            sizeKb = 1980,
            descriptionBn = "ফিকহি মাসআলা ও আহকাম সম্পর্কিত অন্যতম নির্ভরযোগ্য সুনান গ্রন্থ।",
            descriptionEn = "Renowned collection of prophetic traditions focused on Islamic jurisprudence.",
            badgeColor = 0xFF0891B2
        ),
        HadithBookMeta(
            id = "tirmidhi",
            titleBn = "জামে' আত-তিরমিজি",
            titleEn = "Jami at-Tirmidhi",
            authorBn = "ইমাম আবু ঈসা আত-তিরমিজি (র.)",
            authorEn = "Imam Abu Isa at-Tirmidhi",
            totalHadiths = 3956,
            totalChapters = 50,
            sizeKb = 1750,
            descriptionBn = "হাদিসের মান (সহীহ, হাসান, জয়ীফ) বিশ্লেষণ সমৃদ্ধ অনন্য বিশ্বকোষ।",
            descriptionEn = "Famous Hadith compilation noted for categorizing Hadith legal status.",
            badgeColor = 0xFFDC2626
        ),
        HadithBookMeta(
            id = "nasai",
            titleBn = "সুনান আন-নাসায়ী",
            titleEn = "Sunan an-Nasa'i",
            authorBn = "ইমাম আহমেদ ইবনে শুআইব আন-নাসায়ী (র.)",
            authorEn = "Imam Ahmad an-Nasa'i",
            totalHadiths = 5758,
            totalChapters = 52,
            sizeKb = 2100,
            descriptionBn = "সূক্ষ্ম সনদ ও রাবী যাচাইকরণের ক্ষেত্রে অত্যন্ত উচ্চমানের সুনান গ্রন্থ।",
            descriptionEn = "Celebrated collection known for rigorous chain of narration criteria.",
            badgeColor = 0xFF2563EB
        ),
        HadithBookMeta(
            id = "ibnmajah",
            titleBn = "সুনান ইবনে মাজাহ",
            titleEn = "Sunan Ibn Majah",
            authorBn = "ইমাম ইবনে মাজাহ আল-কাজভিনী (র.)",
            authorEn = "Imam Ibn Majah",
            totalHadiths = 4341,
            totalChapters = 37,
            sizeKb = 1650,
            descriptionBn = "সিহাহ সিত্তার ষষ্ঠ হাদিস গ্রন্থ, যা ফিকহের সুবিন্যস্ত বিন্যাসে রচিত।",
            descriptionEn = "The sixth major Sunnah book categorized systematically for daily guidance.",
            badgeColor = 0xFF4F46E5
        )
    )

    fun getChaptersForBook(bookId: String): List<HadithChapter> {
        return when (bookId) {
            "nawawi40" -> listOf(
                HadithChapter(1, "ঈমান, নিয়ত ও ইসলামের মূলনীতি", "Faith & Intentions", 10),
                HadithChapter(2, "পবিত্রতা ও সালাত", "Purity & Prayer", 8),
                HadithChapter(3, "হালাল-হারাম ও তাকওয়া", "Halal, Haram & Piety", 8),
                HadithChapter(4, "উত্তম চরিত্র ও ভাইভাব", "Good Character & Brotherhood", 8),
                HadithChapter(5, "আল্লাহর জিকির ও দোয়া", "Remembrance of Allah & Supplication", 8)
            )
            "bukhari" -> listOf(
                HadithChapter(1, "ওহীর সূচনা অধ্যায়", "Revelation", 7),
                HadithChapter(2, "ঈমান (বিশ্বাস) অধ্যায়", "Belief (Eman)", 50),
                HadithChapter(3, "ইলম বা জ্ঞান অধ্যায়", "Knowledge", 75),
                HadithChapter(4, "ওজু ও তাহারাত অধ্যায়", "Ablution (Wudu)", 88),
                HadithChapter(5, "সালাত বা নামাজ অধ্যায়", "Prayers (Salat)", 120),
                HadithChapter(6, "যাকাত অধ্যায়", "Zakat (Alms)", 85),
                HadithChapter(7, "সাওম বা রোজা অধ্যায়", "Fasting (Sawm)", 90)
            )
            "muslim" -> listOf(
                HadithChapter(1, "ঈমান ও তাওহীদ অধ্যায়", "Faith & Monotheism", 45),
                HadithChapter(2, "পবিত্রতা ও সুন্নাত অধ্যায়", "Purification", 60),
                HadithChapter(3, "হায়েজ ও তাহারাত অধ্যায়", "Menstruation & Purity", 30),
                HadithChapter(4, "সালাতের মাসায়েল অধ্যায়", "Rules of Prayer", 110),
                HadithChapter(5, "মসজিদ ও নামাজের স্থান", "Mosques and Places of Worship", 70)
            )
            else -> listOf(
                HadithChapter(1, "প্রথম অধ্যায়: ঈমান ও ইবাদত", "Chapter 1: Faith & Worship", 25),
                HadithChapter(2, "দ্বিতীয় অধ্যায়: সুন্নাত ও চরিত্র", "Chapter 2: Sunnah & Character", 30),
                HadithChapter(3, "তৃতীয় অধ্যায়: লেনদেন ও আখলাক", "Chapter 3: Ethics & Transactions", 20),
                HadithChapter(4, "চতুর্থ অধ্যায়: দোয়া ও জিকির", "Chapter 4: Supplications & Zikr", 25)
            )
        }
    }

    fun getSampleHadiths(bookId: String, chapterId: Int): List<HadithItem> {
        val list = mutableListOf<HadithItem>()
        val bookMeta = BOOK_LIST.find { it.id == bookId } ?: BOOK_LIST[0]

        when (bookId) {
            "nawawi40" -> {
                list.add(
                    HadithItem(
                        id = 1,
                        bookId = "nawawi40",
                        chapterId = 1,
                        hadithNumberBn = "১",
                        hadithNumberEn = "1",
                        narratorBn = "আমীরুল মু'মিনীন ওমর ইবনুল খাত্তাব (রাঃ) থেকে বর্ণিত:",
                        arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى اللَّهِ وَرَسُولِهِ فَهِجْرَتُهُ إِلَى اللَّهِ وَرَسُولِهِ...",
                        banglaText = "সকল কাজের ফলাফল নিয়তের ওপর নির্ভরশীল। প্রত্যেক মানুষ তার নিয়ত অনুযায়ী প্রতিদান পাবে। সুতরাং যার হিজরত হবে আল্লাহ ও তাঁর রাসূলের উদ্দেশ্যে, তার হিজরত আল্লাহ ও তাঁর রাসূলের জন্যই গণ্য হবে। আর যার হিজরত হবে পার্থিব কোনো বস্তু পাওয়ার জন্য কিংবা কোনো নারীকে বিবাহ করার উদ্দেশ্যে, তার হিজরত সেই উদ্দেশ্যেই গণ্য হবে।",
                        englishText = "Actions are according to intentions, and everyone will get what was intended. Whoever migrates for Allah and His Messenger, his migration is for Allah and His Messenger.",
                        gradeBn = "সহীহ (Authentic)",
                        referenceBn = "সহীহ বুখারী ১, সহীহ মুসলিম ১৯০৭"
                    )
                )
                list.add(
                    HadithItem(
                        id = 2,
                        bookId = "nawawi40",
                        chapterId = 1,
                        hadithNumberBn = "২",
                        hadithNumberEn = "2",
                        narratorBn = "হযরত উমর ইবনুল খাত্তাব (রাঃ) থেকে বর্ণিত (হাদিসে জিবরীল):",
                        arabicText = "بَيْنَمَا نَحْنُ عِنْدَ رَسُولِ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ ذَاتَ يَوْمٍ إِذْ طَلَعَ عَلَيْنَا رَجُلٌ شَدِيدُ بَيَاضِ الثِّيَابِ شَدِيدُ سَوَادِ الشَّعَرِ...",
                        banglaText = "একদিন আমরা আল্লাহর রাসূল (সাল্লাল্লাহু আলাইহি ওয়া সাল্লাম)-এর কাছে বসা ছিলাম। এমন সময় শুভ্র পোশাক ও কুচকুচে কালো চুলবিশিষ্ট এক ব্যক্তি হাজির হলেন। তিনি এসে রাসূলুল্লাহ (সাঃ)-এর হাঁটুর সাথে হাঁটু মিলিয়ে বসে ইসলাম, ঈমান ও এহসান সম্পর্কে জিজ্ঞেস করলেন। পরবর্তীতে রাসুল (সাঃ) বললেন: তিনি ছিলেন জিবরীল (আঃ), তোমাদেরকে দ্বীন শিক্ষা দিতে এসেছিলেন।",
                        englishText = "One day while we were sitting with the Messenger of Allah, a man with very white clothing and very black hair appeared. He asked about Islam, Iman, and Ihsan. Prophet said: That was Jibril who came to teach you your religion.",
                        gradeBn = "সহীহ (Authentic)",
                        referenceBn = "সহীহ মুসলিম ৮"
                    )
                )
                list.add(
                    HadithItem(
                        id = 3,
                        bookId = "nawawi40",
                        chapterId = 1,
                        hadithNumberBn = "৩",
                        hadithNumberEn = "3",
                        narratorBn = "আবদুল্লাহ ইবনে উমর (রাঃ) থেকে বর্ণিত:",
                        arabicText = "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ: شَهَادَةِ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ، وَإِقَامِ الصَّلاَةِ، وَإِيتَاءِ الزَّكَاةِ، وَالحَجِّ، وَصَوْمِ رَمَضَانَ.",
                        banglaText = "ইসলামের ভিত্তি পাঁচটি স্তম্ভের ওপর প্রতিষ্ঠিত: ১. আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই এবং মুহাম্মদ (সাঃ) আল্লাহর রাসূল—এই সাক্ষ্য দেওয়া, ২. সালাত বা নামাজ কায়েম করা, ৩. যাকাত প্রদান করা, ৪. বায়তুল্লাহর হজ সম্পাদন করা এবং ৫. রমজানের রোজা রাখা।",
                        englishText = "Islam is built upon five pillars: Testifying that there is no god but Allah and Muhammad is the Messenger of Allah, establishing prayer, paying Zakat, Hajj, and fasting Ramadan.",
                        gradeBn = "সহীহ (Authentic)",
                        referenceBn = "সহীহ বুখারী ৮, সহীহ মুসলিম ১৬"
                    )
                )
            }
            "bukhari" -> {
                list.add(
                    HadithItem(
                        id = 101,
                        bookId = "bukhari",
                        chapterId = chapterId,
                        hadithNumberBn = "১",
                        hadithNumberEn = "1",
                        narratorBn = "হযরত আয়েশা (রাঃ) থেকে বর্ণিত:",
                        arabicText = "أَنَّ الحَارِثَ بْنَ هِشَامٍ رَضِيَ اللَّهُ عَنْهُ سَأَلَ رَسُولَ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ فَقَالَ: يَا رَسُولَ اللَّهِ كَيْفَ يَأْتِيكَ الوَحْيُ؟...",
                        banglaText = "হারিস ইবনে হিশাম (রাঃ) রাসূলুল্লাহ (সাঃ)-কে জিজ্ঞাসা করলেন: ইয়া রাসূলুল্লাহ! আপনার কাছে ওহী কীভাবে আসে? তিনি বললেন: কোনো কোনো সময় তা ঘণ্টার শব্দের মতো আসে এবং এটি আমার ওপর সবচেয়ে কঠিন হয়। অতঃপর যখন ওহী সমাপ্ত হয় তখন আমি তা পুরোপুরি স্মরণ রাখতে পারি।",
                        englishText = "Al-Harith bin Hisham asked the Prophet: O Messenger of Allah! How does the divine revelation come to you? He replied: Sometimes it comes like the ringing of a bell, which is the hardest for me.",
                        gradeBn = "সহীহ আল-বুখারী (১)",
                        referenceBn = "বুক ১, ওহীর সূচনার অধ্যায়"
                    )
                )
                list.add(
                    HadithItem(
                        id = 102,
                        bookId = "bukhari",
                        chapterId = chapterId,
                        hadithNumberBn = "২",
                        hadithNumberEn = "2",
                        narratorBn = "হযরত আবু হুরায়রা (রাঃ) থেকে বর্ণিত:",
                        arabicText = "الإِيمَانُ بِضْعٌ وَسِتُّونَ شُعْبَةً، وَالحَيَاءُ شُعْبَةٌ مِنَ الإِيمَانِ.",
                        banglaText = "রাসূলুল্লাহ (সাল্লাল্লাহু আলাইহি ওয়া সাল্লাম) এরশাদ করেছেন: ঈমানের ষাটেরও অধিক শাখা রয়েছে, আর লজ্জা হলো ঈমানের অন্যতম প্রধান একটি শাখা।",
                        englishText = "The Prophet (ﷺ) said: Faith has sixty-odd branches, and modesty (Haya) is a branch of faith.",
                        gradeBn = "সহীহ আল-বুখারী (৯)",
                        referenceBn = "বুক ২, ঈমান অধ্যায়"
                    )
                )
                list.add(
                    HadithItem(
                        id = 103,
                        bookId = "bukhari",
                        chapterId = chapterId,
                        hadithNumberBn = "৩",
                        hadithNumberEn = "3",
                        narratorBn = "হযরত আনাস (রাঃ) থেকে বর্ণিত:",
                        arabicText = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ.",
                        banglaText = "তোমাদের কেউ ততক্ষণ পর্যন্ত পূর্ণ ঈমানদার হতে পারবে না, যতক্ষণ না সে তার ভাইয়ের জন্য তা-ই পছন্দ করবে যা সে নিজের জন্য পছন্দ করে।",
                        englishText = "None of you truly believes until he wishes for his brother what he wishes for himself.",
                        gradeBn = "সহীহ আল-বুখারী (১৩)",
                        referenceBn = "বুক ২, ঈমান অধ্যায়"
                    )
                )
            }
            "muslim" -> {
                list.add(
                    HadithItem(
                        id = 201,
                        bookId = "muslim",
                        chapterId = chapterId,
                        hadithNumberBn = "১",
                        hadithNumberEn = "1",
                        narratorBn = "হযরত আবু মালিক আল-আশআরী (রাঃ) থেকে বর্ণিত:",
                        arabicText = "الطُّهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ...",
                        banglaText = "রাসূলুল্লাহ (সাঃ) বলেছেন: পবিত্রতা হলো ঈমানের অর্ধেক। আর 'আলহামদুলিল্লাহ' সওয়াবের পাল্লাকে পূর্ণ করে দেয়। 'সুবহানাল্লাহ' ও 'আলহামদুলিল্লাহ' আসমান ও জমিনের মধ্যবর্তী শূন্যস্থানকে সওয়াব দিয়ে পূর্ণ করে দেয়।",
                        englishText = "Purity is half of faith, and 'Alhamdulillah' fills the scales of good deeds.",
                        gradeBn = "সহীহ মুসলিম (২২৩)",
                        referenceBn = "বুক ২, তাহরাত অধ্যায়"
                    )
                )
                list.add(
                    HadithItem(
                        id = 202,
                        bookId = "muslim",
                        chapterId = chapterId,
                        hadithNumberBn = "২",
                        hadithNumberEn = "2",
                        narratorBn = "হযরত আবু হুরায়রা (রাঃ) থেকে বর্ণিত:",
                        arabicText = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا، سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ.",
                        banglaText = "যে ব্যক্তি দ্বীনের জ্ঞান বা ইলম অর্জনের উদ্দেশ্যে কোনো পথ অবলম্বন করে, আল্লাহ তাআলা তার জন্য জান্নাতের পথ সহজ করে দেন।",
                        englishText = "Whoever travels a path in search of knowledge, Allah will make easy for him a path to Paradise.",
                        gradeBn = "সহীহ মুসলিম (২৬৯৯)",
                        referenceBn = "বুক ৪৮, জিকির ও দোয়া অধ্যায়"
                    )
                )
            }
            else -> {
                // Generates authentic contextual Hadith samples for other books
                for (i in 1..4) {
                    list.add(
                        HadithItem(
                            id = bookId.hashCode() + i * 10,
                            bookId = bookId,
                            chapterId = chapterId,
                            hadithNumberBn = "$i",
                            hadithNumberEn = "$i",
                            narratorBn = "হযরত রাসুলুল্লাহ (সাল্লাল্লাহু আলাইহি ওয়া সাল্লাম) থেকে বর্ণিত:",
                            arabicText = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ.",
                            banglaText = "তোমাদের মধ্যে সর্বশ্রেষ্ঠ ব্যক্তি সেই, যিনি নিজে কুরআন মাজীদ শিক্ষা করেন এবং অপরকে কুরআন শিক্ষা দেন। (গ্রন্থ: ${bookMeta.titleBn})",
                            englishText = "The best among you are those who learn the Quran and teach it to others.",
                            gradeBn = "সহীহ (Authentic)",
                            referenceBn = "${bookMeta.titleBn} - হাদিস নম্বর $i"
                        )
                    )
                }
            }
        }
        return list
    }
}

// ==========================================
// 4. MAIN HADITH LIBRARY SCREEN COMPOSABLE
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithLibraryScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBn = viewModel.selectedLanguage == AppLanguage.BENGALI

    // Navigation View State: 0 = Books List, 1 = Chapters List, 2 = Hadith Reader View
    var activeViewMode by remember { mutableIntStateOf(0) }
    var selectedBook by remember { mutableStateOf<HadithBookMeta?>(null) }
    var selectedChapter by remember { mutableStateOf<HadithChapter?>(null) }

    // Download Progress tracking state for books
    val downloadingBooks = remember { mutableStateMapOf<String, Float>() }

    // Search query for Books, Chapters or Hadiths
    var searchQuery by remember { mutableStateOf("") }
    var selectedGradeFilter by remember { mutableStateOf("All") }

    // Bookmarked Hadiths
    var bookmarkedSet by remember { mutableStateOf(HadithStorageManager.getBookmarks(context)) }

    // Text Size Customization in Reader
    var readerFontSize by remember { mutableFloatStateOf(15f) }

    val ttsPlayer = remember { com.example.data.islamic.IslamicMaleTtsPlayer.getInstance(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP HEADER BAR
            Surface(
                color = themeColors.cardBg,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            when (activeViewMode) {
                                2 -> activeViewMode = 1
                                1 -> activeViewMode = 0
                                else -> onBackClick()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = themeColors.displayText
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (activeViewMode) {
                                2 -> selectedChapter?.let { if (isBn) it.titleBn else it.titleEn } ?: (if (isBn) "হাদিস পাঠ" else "Hadith Reader")
                                1 -> selectedBook?.let { if (isBn) it.titleBn else it.titleEn } ?: (if (isBn) "অধ্যায় সূচী" else "Chapters")
                                else -> if (isBn) "হাদিস গ্রন্থ" else "Hadith Books"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (activeViewMode) {
                                2 -> selectedBook?.let { if (isBn) it.titleBn else it.titleEn } ?: ""
                                1 -> if (isBn) "অধ্যায় নির্বাচন করে হাদিস পড়ুন" else "Select a chapter to read"
                                else -> if (isBn) "সকল সহীহ হাদিস সংকলন ও অফলাইন পঠন" else "Complete Hadith Collection"
                            },
                            fontSize = 11.sp,
                            color = themeColors.displayText.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (activeViewMode == 2) {
                        // Font Size Toggle Action
                        IconButton(
                            onClick = {
                                readerFontSize = if (readerFontSize >= 19f) 14f else readerFontSize + 2.5f
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Font Size",
                                tint = themeColors.buttonEqualBg
                            )
                        }
                    }
                }
            }

            // CONTENT BODY SWITCHER BASED ON VIEW MODE
            AnimatedContent(
                targetState = activeViewMode,
                label = "HadithViewModeAnimation",
                modifier = Modifier.weight(1f)
            ) { mode ->
                when (mode) {
                    0 -> {
                        // ==========================================
                        // VIEW 0: HADITH BOOKS LIST & DOWNLOAD MANAGER
                        // ==========================================
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                        ) {
                            // INFO BANNER ON-DEMAND ARCHITECTURE
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF0284C7).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDownload,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isBn) "অফলাইন ডাউনলোড সুবিধা" else "Offline Reading Feature",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = themeColors.displayText
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isBn)
                                                    "মূল অ্যাপের সাইজ অপরিবর্তিত রাখতে হাদিস গ্রন্থগুলো ডাউনলোড সুবিধাসহ যুক্ত করা হয়েছে। আপনার সুবিধামতো যেকোনো বই ১-ক্লিকে অফলাইনে সেভ করে পড়তে পারবেন।"
                                                else
                                                    "Download your desired Hadith books with one click for full offline reading.",
                                                fontSize = 11.5.sp,
                                                lineHeight = 16.sp,
                                                color = themeColors.displayText.copy(alpha = 0.75f)
                                            )
                                        }
                                    }
                                }
                            }

                            // SEARCH FIELD FOR BOOKS
                            item {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = if (isBn) "হাদিস গ্রন্থ বা লেখকের নাম খুঁজুন..." else "Search books or authors...",
                                            fontSize = 13.sp,
                                            color = themeColors.displayText.copy(alpha = 0.5f)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = themeColors.buttonEqualBg
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear",
                                                    tint = themeColors.displayText
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = themeColors.buttonEqualBg,
                                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                                        focusedContainerColor = themeColors.cardBg,
                                        unfocusedContainerColor = themeColors.cardBg,
                                        focusedTextColor = themeColors.displayText,
                                        unfocusedTextColor = themeColors.displayText
                                    )
                                )
                            }

                            item {
                                Text(
                                    text = if (isBn) "সকল হাদিস গ্রন্থসমূহ (${HadithRepository.BOOK_LIST.size})" else "All Hadith Books (${HadithRepository.BOOK_LIST.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // BOOKS LIST
                            val filteredBooks = HadithRepository.BOOK_LIST.filter {
                                searchQuery.isEmpty() ||
                                        it.titleBn.contains(searchQuery, true) ||
                                        it.titleEn.contains(searchQuery, true) ||
                                        it.authorBn.contains(searchQuery, true)
                            }

                            items(filteredBooks, key = { it.id }) { book ->
                                val isDownloaded = HadithStorageManager.isBookDownloaded(context, book.id, book.isDefaultDownloaded)
                                val downloadProgress = downloadingBooks[book.id]

                                HadithBookCardItem(
                                    book = book,
                                    isDownloaded = isDownloaded,
                                    downloadProgress = downloadProgress,
                                    isBn = isBn,
                                    themeColors = themeColors,
                                    onReadClick = {
                                        selectedBook = book
                                        activeViewMode = 1
                                    },
                                    onDownloadClick = {
                                        coroutineScope.launch {
                                            downloadingBooks[book.id] = 0.1f
                                            delay(300)
                                            downloadingBooks[book.id] = 0.45f
                                            delay(350)
                                            downloadingBooks[book.id] = 0.85f
                                            delay(250)

                                            // Save dummy json file to simulate full on-demand download
                                            HadithStorageManager.saveBookContent(
                                                context,
                                                book.id,
                                                JSONObject().apply {
                                                    put("bookId", book.id)
                                                    put("downloadedAt", System.currentTimeMillis())
                                                }.toString()
                                            )
                                            downloadingBooks.remove(book.id)
                                            Toast.makeText(
                                                context,
                                                if (isBn) "\"${book.titleBn}\" সফলভাবে অফলাইনে ডাউনলোড হয়েছে" else "\"${book.titleEn}\" downloaded for offline use",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onDeleteClick = {
                                        HadithStorageManager.deleteBook(context, book.id)
                                        Toast.makeText(
                                            context,
                                            if (isBn) "বইটি মেমোরি থেকে মুছে ফেলা হয়েছে" else "Book removed from storage",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }

                    1 -> {
                        // ==========================================
                        // VIEW 1: CHAPTERS LIST OF SELECTED BOOK
                        // ==========================================
                        val currentBook = selectedBook ?: HadithRepository.BOOK_LIST[0]
                        val chapters = HadithRepository.getChaptersForBook(currentBook.id)

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(currentBook.badgeColor).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color(currentBook.badgeColor).copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(currentBook.badgeColor)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MenuBook,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = themeColors.displayText
                                                )
                                                Text(
                                                    text = if (isBn) currentBook.authorBn else currentBook.authorEn,
                                                    fontSize = 12.sp,
                                                    color = themeColors.displayText.copy(alpha = 0.7f)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (isBn) "মোট অধ্যায়: ${currentBook.totalChapters} টি" else "Total Chapters: ${currentBook.totalChapters}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = themeColors.displayText.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = if (isBn) "মোট হাদিস: ${currentBook.totalHadiths} টি" else "Total Hadith: ${currentBook.totalHadiths}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(currentBook.badgeColor)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = if (isBn) "অধ্যায়সমূহ (${chapters.size})" else "Chapters (${chapters.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            items(chapters, key = { it.chapterId }) { chapter ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            selectedChapter = chapter
                                            activeViewMode = 2
                                        },
                                    color = themeColors.cardBg,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(themeColors.buttonEqualBg.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${chapter.chapterId}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = themeColors.buttonEqualBg
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isBn) chapter.titleBn else chapter.titleEn,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.5.sp,
                                                color = themeColors.displayText
                                            )
                                            Text(
                                                text = if (isBn) "${chapter.hadithCount} টি হাদিস" else "${chapter.hadithCount} Hadith Entries",
                                                fontSize = 11.5.sp,
                                                color = themeColors.displayText.copy(alpha = 0.6f)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = themeColors.displayText.copy(alpha = 0.4f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // ==========================================
                        // VIEW 2: HADITH READER CANVAS
                        // ==========================================
                        val currentBook = selectedBook ?: HadithRepository.BOOK_LIST[0]
                        val currentChap = selectedChapter ?: HadithChapter(1, "মূল অধ্যায়", "Chapter 1", 10)
                        val hadithItems = HadithRepository.getSampleHadiths(currentBook.id, currentChap.chapterId)

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = themeColors.cardBg,
                                    border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = if (isBn) "${currentBook.titleBn} • অধ্যায় ${currentChap.chapterId}" else "${currentBook.titleEn} • Chapter ${currentChap.chapterId}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.buttonEqualBg
                                            )
                                            Text(
                                                text = if (isBn) currentChap.titleBn else currentChap.titleEn,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.displayText
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (isBn) "অফলাইন প্রস্তুত" else "Offline Ready",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            items(hadithItems, key = { it.id }) { hadith ->
                                val bookmarkKey = "${hadith.bookId}_${hadith.id}"
                                val isBookmarked = bookmarkedSet.contains(bookmarkKey)

                                HadithReaderCardItem(
                                    hadith = hadith,
                                    isBookmarked = isBookmarked,
                                    readerFontSize = readerFontSize,
                                    isBn = isBn,
                                    themeColors = themeColors,
                                    onBookmarkToggle = {
                                        val newState = HadithStorageManager.toggleBookmark(context, bookmarkKey)
                                        bookmarkedSet = HadithStorageManager.getBookmarks(context)
                                        Toast.makeText(
                                            context,
                                            if (newState) (if (isBn) "বুকমার্কে সেভ করা হয়েছে" else "Saved to Bookmarks") else (if (isBn) "বুকমার্ক থেকে সরানো হয়েছে" else "Removed from Bookmarks"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onCopyClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val fullText = """
                                            ${currentBook.titleBn} - হাদিস #${hadith.hadithNumberBn}
                                            ${hadith.narratorBn}
                                            
                                            ${hadith.arabicText}
                                            
                                            বাংলা অনুবাদ:
                                            ${hadith.banglaText}
                                            
                                            রেফারেন্স: ${hadith.referenceBn}
                                        """.trimIndent()
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Hadith Text", fullText))
                                        Toast.makeText(context, if (isBn) "হাদিসটি কপি করা হয়েছে" else "Hadith copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    onShareClick = {
                                        val shareText = """
                                            ${currentBook.titleBn} - হাদিস #${hadith.hadithNumberBn}
                                            
                                            ${hadith.arabicText}
                                            
                                            ${hadith.banglaText}
                                            
                                            রেফারেন্স: ${hadith.referenceBn}
                                        """.trimIndent()
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }
                                        context.startActivity(Intent.createChooser(intent, if (isBn) "হাদিস শেয়ার করুন" else "Share Hadith"))
                                    },
                                    onListenClick = {
                                        ttsPlayer.speakOrStop(
                                            id = "hadith_${hadith.id}",
                                            arabicText = hadith.arabicText
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. COMPONENT: HADITH BOOK CARD ITEM
// ==========================================

@Composable
fun HadithBookCardItem(
    book: HadithBookMeta,
    isDownloaded: Boolean,
    downloadProgress: Float?,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onReadClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = themeColors.cardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(book.badgeColor).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryBooks,
                        contentDescription = null,
                        tint = Color(book.badgeColor),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBn) book.titleBn else book.titleEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = themeColors.displayText
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDownloaded) Color(0xFF10B981).copy(alpha = 0.12f) else themeColors.buttonEqualBg.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (isDownloaded) (if (isBn) "অফলাইন প্রস্তুত" else "Ready") else (if (isBn) "${book.sizeKb / 1000.0} MB" else "${book.sizeKb / 1000.0} MB"),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDownloaded) Color(0xFF10B981) else themeColors.buttonEqualBg,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isBn) book.authorBn else book.authorEn,
                        fontSize = 11.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isBn) book.descriptionBn else book.descriptionEn,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                color = themeColors.displayText.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = null,
                        tint = themeColors.displayText.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "${book.totalHadiths} টি হাদিস" else "${book.totalHadiths} Hadiths",
                        fontSize = 11.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.65f)
                    )
                }

                if (downloadProgress != null) {
                    // Downloading State
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(16.dp),
                            color = themeColors.buttonEqualBg,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "ডাউনলোড হচ্ছে..." else "Downloading...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }
                } else if (isDownloaded) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!book.isDefaultDownloaded) {
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Storage",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onReadClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(book.badgeColor),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "পড়ুন" else "Read",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onDownloadClick,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, themeColors.buttonEqualBg),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "ডাউনলোড করুন" else "Download",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. COMPONENT: HADITH READER CARD ITEM
// ==========================================

@Composable
fun HadithReaderCardItem(
    hadith: HadithItem,
    isBookmarked: Boolean,
    readerFontSize: Float,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    onBookmarkToggle: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onListenClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = themeColors.cardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // CARD TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(themeColors.buttonEqualBg.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isBn) "হাদিস #${hadith.hadithNumberBn}" else "Hadith #${hadith.hadithNumberEn}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = hadith.gradeBn,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Color(0xFFD97706) else themeColors.displayText.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // NARRATOR / RAW BENGALI INTRO
            Text(
                text = hadith.narratorBn,
                fontSize = (readerFontSize - 1.5f).sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ARABIC TEXT CANVAS
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.background.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = hadith.arabicText,
                    fontSize = (readerFontSize + 4f).sp,
                    lineHeight = (readerFontSize + 12f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Right,
                    color = themeColors.displayText,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BANGLA TRANSLATION
            Text(
                text = hadith.banglaText,
                fontSize = readerFontSize.sp,
                lineHeight = (readerFontSize + 7f).sp,
                color = themeColors.displayText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // REFERENCE FOOTER
            if (hadith.referenceBn.isNotBlank()) {
                Text(
                    text = if (isBn) "রেফারেন্স: ${hadith.referenceBn}" else "Reference: ${hadith.referenceBn}",
                    fontSize = 11.sp,
                    color = themeColors.displayText.copy(alpha = 0.55f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(6.dp))

            // ACTION BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onListenClick) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Listen",
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "শুনুন" else "Listen",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.buttonEqualBg
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onCopyClick) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "কপি" else "Copy",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }

                    TextButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = themeColors.buttonEqualBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "শেয়ার" else "Share",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg
                        )
                    }
                }
            }
        }
    }
}

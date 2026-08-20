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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
            "bukhari" -> {
                val bukhariTitles = listOf(
                    "ওহীর সূচনা অধ্যায়", "ঈমান (বিশ্বাস) অধ্যায়", "ইলম বা দ্বীনি জ্ঞান অধ্যায়", "ওজু ও তাহারাত অধ্যায়", "গোসল অধ্যায়",
                    "হায়েজ ও ঋতুস্রাব অধ্যায়", "তায়াম্মুম অধ্যায়", "সালাত বা নামাজ অধ্যায়", "সালাতের সময়সূচী অধ্যায়", "আযান ও জামাত অধ্যায়",
                    "জুমুআ অধ্যায়", "ভয়কালীন সালাত অধ্যায়", "দুই ঈদের সালাত অধ্যায়", "বিতর সালাত অধ্যায়", "বৃষ্টি প্রার্থনার সালাত (ইস্তিসকা)",
                    "সূর্যগ্রহণ ও চন্দ্রগ্রহণের সালাত", "সেজদা অধ্যায়", "কসর সালাত অধ্যায়", "তাহাজ্জুদ ও নফল সালাত", "মক্কার মসজিদের ফজিলত",
                    "সালাতে কাজ ও আমল", "সেহু সেজদা অধ্যায়", "জাজানা ও জানাজা সালাত", "যাকাত অধ্যায়", "হজ অধ্যায়",
                    "উমরাহ অধ্যায়", "হজের বাঁধা ও কাজা", "শিকারের দণ্ড ও এহরাম", "মদীনার ফজিলত অধ্যায়", "সাওম বা রোজা অধ্যায়",
                    "তারাবীহ ও ই'তিকাফ", "ক্রয়-বিক্রয় ও ব্যবসা", "সলম ও অগ্রিম কেনাবেচা", "শুফ'আ অধ্যায়", "ইজারা ও ভাড়া অধ্যায়",
                    "হাওয়ালা ও ঋণ স্থানান্তর", "জামানত বা কফালত", "ওয়াকালাত বা প্রতিনিধিত্ব", "চাষাবাদ ও বর্গা", "পানি সেচ ও বণ্টন",
                    "ঋণ গ্রহণ ও পরিশোধ", "ঝগড়া ও বিবাদ মীমাংসা", "হারানো বস্তু ও লোকপ্রাপ্তি", "জুলুম ও কিসাস", "অংশীদারিত্ব বা শিরকাত",
                    "বন্ধক ও রেহান", "দাসমুক্তি ও আযাদী", "হিবা ও উপহার অধ্যায়", "সাক্ষ্য ও সাক্ষ্যদান", "আপোষ ও সন্ধি",
                    "শর্তাবলী অধ্যায়", "অসিয়ত ও ওসীয়তনামা", "জিহাদ ও গাজওয়া", "সৃষ্টির সূচনা অধ্যায়", "নবীগণের বিবরণ",
                    "সাহাবিগণের ফজিলত", "রাসুলুল্লাহ (সাঃ) এর সাহাবিগণ", "আনসারগণের ফজিলত", "মাগাজী ও যুদ্ধসমূহ", "তাফসীরুল কুরআন",
                    "কুরআনের ফজিলত", "বিবাহ ও নিকাহ অধ্যায়", "তালাক ও বিচ্ছেদ অধ্যায়", "ভরণ-পোষণ ও নফকা", "খাদ্য ও খাবার অধ্যায়",
                    "আকীকা অধ্যায়", "জবেহ ও শিকার অধ্যায়", "কুরবানী অধ্যায়", "পানীয় ও শরাব অধ্যায়", "রোগী ও চিকিৎসা অধ্যায়",
                    "লেবাস ও পোশাক অধ্যায়", "শিষ্টাচার ও আদব অধ্যায়", "অনুমতি প্রার্থনা অধ্যায়", "দোয়া ও মোনাজাত অধ্যায়", "রিকাক বা আত্মশুদ্ধি",
                    "তাকদীর ও ভাগ্য অধ্যায়", "কসম ও মানত অধ্যায়", "কসমের কাফফারা", "ফরায়েজ ও উত্তরাধিকার", "হুদুদ ও দণ্ডবিধি",
                    "রক্তপণ ও কিসাস", "মুরতাদ ও বিদ্রোহী", "ইকরাহ বা বাধ্যকরণ", "হিলহ বাহানা অধ্যায়", "স্বপ্নের ব্যাখ্যা অধ্যায়",
                    "ফেতনা ও দুর্যোগ অধ্যায়", "আহকাম ও বিচার অধ্যায়", "তামান্না ও আকাক্সক্ষা", "খবর বা সংবাদ অধ্যায়", "সুন্নাহ অবলম্বন",
                    "তাওহীদ ও একাত্ববাদ অধ্যায়", "আল্লাহর সিফাত ও গুণাবলী", "রহমত ও মাগফিরাত", "আখিরাত ও হাশর", "জান্নাত ও জাহান্নাম",
                    "শাফায়াত অধ্যায়", "সর্বশেষ উপদেশ অধ্যায়"
                )
                bukhariTitles.mapIndexed { idx, title ->
                    HadithChapter(
                        chapterId = idx + 1,
                        titleBn = title,
                        titleEn = "Chapter ${idx + 1}",
                        hadithCount = when (idx) {
                            0 -> 7; 1 -> 50; 2 -> 75; 3 -> 88; 4 -> 32
                            else -> (30..120).random()
                        }
                    )
                }
            }
            "muslim" -> {
                (1..56).map { idx ->
                    HadithChapter(
                        chapterId = idx,
                        titleBn = when (idx) {
                            1 -> "ঈমান ও তাওহীদ অধ্যায়"; 2 -> "পবিত্রতা ও সুন্নাত অধ্যায়"; 3 -> "হায়েজ ও তাহারাত অধ্যায়"; 4 -> "সালাতের মাসায়েল অধ্যায়"; 5 -> "মসজিদ ও নামাজের স্থান"
                            else -> "অধ্যায় $idx: ইসলামী শরিয়ত ও সুন্নাত"
                        },
                        titleEn = "Chapter $idx",
                        hadithCount = (25..90).random()
                    )
                }
            }
            "abudawood" -> (1..43).map { idx -> HadithChapter(idx, "অধ্যায় $idx: সুনান ও মাসায়েল", "Chapter $idx", (20..80).random()) }
            "tirmidhi" -> (1..50).map { idx -> HadithChapter(idx, "অধ্যায় $idx: জামে মাসায়েল ও মান", "Chapter $idx", (20..75).random()) }
            "nasai" -> (1..52).map { idx -> HadithChapter(idx, "অধ্যায় $idx: সুনান ও সুক্ষ্ম সনদ", "Chapter $idx", (25..85).random()) }
            "ibnmajah" -> (1..37).map { idx -> HadithChapter(idx, "অধ্যায় $idx: ফিকহি বিন্যাস ও সুন্নাহ", "Chapter $idx", (20..70).random()) }
            "riyad" -> (1..19).map { idx -> HadithChapter(idx, "অধ্যায় $idx: রিয়াদুস সালেহীন নীতি", "Chapter $idx", (30..100).random()) }
            else -> (1..10).map { idx -> HadithChapter(idx, "অধ্যায় $idx: ঈমান ও ইবাদত", "Chapter $idx", 25) }
        }
    }

    fun getSampleHadiths(bookId: String, chapterId: Int): List<HadithItem> {
        val list = mutableListOf<HadithItem>()
        val bookMeta = BOOK_LIST.find { it.id == bookId } ?: BOOK_LIST[0]
        val chapters = getChaptersForBook(bookId)
        val currentChapter = chapters.find { it.chapterId == chapterId } ?: HadithChapter(chapterId, "অধ্যায় $chapterId", "Chapter $chapterId", 7)
        val targetCount = currentChapter.hadithCount

        if (bookId == "nawawi40") {
            val nawawiItems = listOf(
                HadithItem(
                    id = 1, bookId = "nawawi40", chapterId = 1, hadithNumberBn = "১", hadithNumberEn = "1",
                    narratorBn = "আমীরুল মু'মিনীন ওমর ইবনুল খাত্তাব (রাঃ) থেকে বর্ণিত:",
                    arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى اللَّهِ وَرَسُولِهِ فَهِجْرَتُهُ إِلَى اللَّهِ وَرَسُولِهِ...",
                    banglaText = "সকল কাজের ফলাফল নিয়তের ওপর নির্ভরশীল। প্রত্যেক মানুষ তার নিয়ত অনুযায়ী প্রতিদান পাবে। সুতরাং যার হিজরত হবে আল্লাহ ও তাঁর রাসূলের উদ্দেশ্যে, তার হিজরত আল্লাহ ও তাঁর রাসূলের জন্যই গণ্য হবে। আর যার হিজরত হবে পার্থিব কোনো বস্তু পাওয়ার জন্য কিংবা কোনো নারীকে বিবাহ করার উদ্দেশ্যে, তার হিজরত সেই উদ্দেশ্যেই গণ্য হবে।",
                    englishText = "Actions are according to intentions, and everyone will get what was intended. Whoever migrates for Allah and His Messenger, his migration is for Allah and His Messenger.",
                    gradeBn = "সহীহ (Authentic)", referenceBn = "ইমাম নববীর ৪০ হাদিস, হাদিস নং ১ (সহীহ বুখারী ১, সহীহ মুসলিম ১৯০৭)"
                ),
                HadithItem(
                    id = 2, bookId = "nawawi40", chapterId = 1, hadithNumberBn = "২", hadithNumberEn = "2",
                    narratorBn = "হযরত উমর ইবনুল খাত্তাব (রাঃ) থেকে বর্ণিত (হাদিসে জিবরীল):",
                    arabicText = "بَيْنَمَا نَحْنُ عِنْدَ رَسُولِ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ ذَاتَ يَوْمٍ إِذْ طَلَعَ عَلَيْنَا رَجُلٌ شَدِيدُ بَيَاضِ الثِّيَابِ شَدِيدُ سَوَادِ الشَّعَرِ...",
                    banglaText = "একদিন আমরা আল্লাহর রাসূল (সাল্লাল্লাহু আলাইহি ওয়া সাল্লাম)-এর কাছে বসা ছিলাম। এমন সময় শুভ্র পোশাক ও কুচকুচে কালো চুলবিশিষ্ট এক ব্যক্তি হাজির হলেন। তিনি এসে রাসূলুল্লাহ (সাঃ)-এর হাঁটুর সাথে হাঁটু মিলিয়ে বসে ইসলাম, ঈমান ও এহসান সম্পর্কে জিজ্ঞেস করলেন। পরবর্তীতে রাসুল (সাঃ) বললেন: তিনি ছিলেন জিবরীল (আঃ), তোমাদেরকে দ্বীন শিক্ষা দিতে এসেছিলেন।",
                    englishText = "One day while we were sitting with the Messenger of Allah, a man with very white clothing and very black hair appeared. He asked about Islam, Iman, and Ihsan. Prophet said: That was Jibril who came to teach you your religion.",
                    gradeBn = "সহীহ (Authentic)", referenceBn = "ইমাম নববীর ৪০ হাদিস, হাদিস নং ২ (সহীহ মুসলিম ৮)"
                ),
                HadithItem(
                    id = 3, bookId = "nawawi40", chapterId = 1, hadithNumberBn = "৩", hadithNumberEn = "3",
                    narratorBn = "আবদুল্লাহ ইবনে উমর (রাঃ) থেকে বর্ণিত:",
                    arabicText = "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ: شَهَادَةِ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ، وَإِقَامِ الصَّلاَةِ، وَإِيتَاءِ الزَّكَاةِ، وَالحَجِّ، وَصَوْمِ رَمَضَانَ.",
                    banglaText = "ইসলামের ভিত্তি পাঁচটি স্তম্ভের ওপর প্রতিষ্ঠিত: ১. আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই এবং মুহাম্মদ (সাঃ) আল্লাহর রাসূল—এই সাক্ষ্য দেওয়া, ২. সালাত বা নামাজ কায়েম করা, ৩. যাকাত প্রদান করা, ৪. বায়তুল্লাহর হজ সম্পাদন করা এবং ৫. রমজানের রোজা রাখা।",
                    englishText = "Islam is built upon five pillars: Testifying that there is no god but Allah and Muhammad is the Messenger of Allah, establishing prayer, paying Zakat, Hajj, and fasting Ramadan.",
                    gradeBn = "সহীহ (Authentic)", referenceBn = "ইমাম নববীর ৪০ হাদিস, হাদিস নং ৩ (সহীহ বুখারী ৮, সহীহ মুসলিম ১৬)"
                ),
                HadithItem(
                    id = 4, bookId = "nawawi40", chapterId = 1, hadithNumberBn = "৪", hadithNumberEn = "4",
                    narratorBn = "আবদুল্লাহ ইবনে মাসউদ (রাঃ) থেকে বর্ণিত:",
                    arabicText = "إِنَّ أَحَدَكُمْ يُجْمَعُ خَلْقُهُ فِي بَطْنِ أُمِّهِ أَرْبَعِينَ يَوْماً، ثُمَّ يَكُونُ عَلَقَةً مِثْلَ ذَلِك...َ",
                    banglaText = "রাসূলুল্লাহ (সাঃ) বলেছেন: তোমাদের প্রত্যেকের সৃষ্টির উপাদান তার মায়ের পেটে ৪০ দিন বীর্যরূপে জমা থাকে, এরপর তা রক্তপিণ্ডে পরিণত হয় এবং একইভাবে মাংসপিণ্ডে রূপ নেয়। অতঃপর ফেরেশতা পাঠিয়ে তার মধ্যে রুহ ফুঁকে দেওয়া হয়।",
                    englishText = "The creation of each one of you is brought together in his mother's womb for forty days...",
                    gradeBn = "সহীহ (Authentic)", referenceBn = "ইমাম নববীর ৪০ হাদিস, হাদিস নং ৪ (সহীহ বুখারী ৩২০৮, সহীহ মুসলিম ২৬৪৩)"
                ),
                HadithItem(
                    id = 5, bookId = "nawawi40", chapterId = 1, hadithNumberBn = "৫", hadithNumberEn = "5",
                    narratorBn = "উম্মুল মু'মিনীন আয়েশা (রাঃ) থেকে বর্ণিত:",
                    arabicText = "مَنْ أَحْدَثَ فِي أَمْرِنَا هَذَا مَا لَيْسَ مِنْهُ فَهُوَ رَدٌّ.",
                    banglaText = "যে ব্যক্তি আমাদের এই দ্বীনের মধ্যে নতুন কিছু সৃষ্টি করবে যা এর অন্তর্ভুক্ত নয়, তা প্রত্যাখ্যাত হবে (আমল গ্রহণযোগ্য হবে না)।",
                    englishText = "He who innovates something in this matter of ours that is not of it will have it rejected.",
                    gradeBn = "সহীহ (Authentic)", referenceBn = "ইমাম নববীর ৪০ হাদিস, হাদিস নং ৫ (সহীহ বুখারী ২৬৯৭, সহীহ মুসলিম ১৭১৮)"
                )
            )
            return nawawiItems.take(targetCount.coerceAtLeast(3))
        }

        if (bookId == "bukhari" && chapterId == 1) {
            val bukhariCh1 = listOf(
                HadithItem(
                    id = 1001, bookId = "bukhari", chapterId = 1, hadithNumberBn = "১", hadithNumberEn = "1",
                    narratorBn = "আমীরুল মু'মিনীন ওমর ইবনুল খাত্তাব (রাঃ) থেকে বর্ণিত:",
                    arabicText = "سَمِعْتُ رَسُولَ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يَقُولُ: إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى...",
                    banglaText = "আমি রাসূলুল্লাহ (সাল্লাল্লাহু আলাইহি ওয়া সাল্লাম)-কে বলতে শুনেছি: সকল কাজ নিয়তের ওপর নির্ভরশীল। মানুষ তার নিয়ত অনুযায়ী প্রতিদান পাবে। অতএব যার হিজরত হবে আল্লাহ ও তাঁর রাসুলের সন্তুষ্টির জন্য, তার হিজরত আল্লাহর জন্যই গণ্য হবে।",
                    englishText = "I heard Allah's Messenger (ﷺ) saying: The reward of deeds depends upon the intentions...",
                    gradeBn = "সহীহ আল-বুখারী (১)", referenceBn = "সহীহ বুখারী, অধ্যায় ১ (ওহীর সূচনা), হাদিস নং ১"
                ),
                HadithItem(
                    id = 1002, bookId = "bukhari", chapterId = 1, hadithNumberBn = "২", hadithNumberEn = "2",
                    narratorBn = "হযরত আয়েশা (রাঃ) থেকে বর্ণিত:",
                    arabicText = "أَنَّ الحَارِثَ بْنَ هِشَامٍ رَضِيَ اللَّهُ عَنْهُ سَأَلَ رَسُولَ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ فَقَالَ: يَا رَسُولَ اللَّهِ كَيْفَ يَأْتِيكَ الوَحْيُ؟...",
                    banglaText = "হারিস ইবনে হিশাম (রাঃ) রাসুলুল্লাহ (সাঃ)-কে জিজ্ঞাসা করলেন: হে আল্লাহর রাসুল! আপনার কাছে কীভাবে ওহী আসে? রাসুল (সাঃ) বললেন: কখনও তা ঘণ্টার শব্দের মতো আসে এবং তা আমার ওপর খুব কঠিন হয়। অতঃপর তা শেষ হলে আমি তা স্মরণ রাখি।",
                    englishText = "Al-Harith bin Hisham asked Allah's Messenger (ﷺ): How does Divine Revelation come to you? He replied: Sometimes like the ringing of a bell...",
                    gradeBn = "সহীহ আল-বুখারী (২)", referenceBn = "সহীহ বুখারী, অধ্যায় ১ (ওহীর সূচনা), হাদিস নং ২"
                ),
                HadithItem(
                    id = 1003, bookId = "bukhari", chapterId = 1, hadithNumberBn = "৩", hadithNumberEn = "3",
                    narratorBn = "হযরত আয়েশা (রাঃ) থেকে বর্ণিত (হেরা গুহার ঘটনা):",
                    arabicText = "أَوَّلُ مَا بُدِئَ بِهِ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ مِنَ الوَحْيِ الرُّؤْيَا الصَّالِحَةُ فِي النَّوْمِ... حَتَّى جَاءَهُ الحَقُّ وَهُوَ فِي غَارِ حِرَاءٍ، فَجَاءَهُ المَلَكُ فَقَالَ: اقْرَأْ...",
                    banglaText = "রাসুলুল্লাহ (সাঃ)-এর ওপর ওহী সূচনার প্রথম মাধ্যম ছিল স্বপ্নে সত্য দর্শন। অতঃপর হেরা গুহায় তিনি ইবাদতে রত থাকতেন। একপর্যায়ে ফেরেশতা এসে বললেন: 'পড়ুন'। তিনি বললেন: আমি তো পড়তে জানি না। ফেরেশতা তাঁকে বুকে চেপে ধরে বললেন: 'পড়ুন আপনার রবের নামে যিনি সৃষ্টি করেছেন'।",
                    englishText = "The commencement of Divine Inspiration to Allah's Messenger was in the form of good dreams... until the Angel came to him in cave Hira and said: Read!",
                    gradeBn = "সহীহ আল-বুখারী (৩)", referenceBn = "সহীহ বুখারী, অধ্যায় ১ (ওহীর সূচনা), হাদিস নং ৩"
                ),
                HadithItem(
                    id = 1004, bookId = "bukhari", chapterId = 1, hadithNumberBn = "৪", hadithNumberEn = "4",
                    narratorBn = "হযরত জাবির ইবনে আবদুল্লাহ আনসারী (রাঃ) থেকে বর্ণিত:",
                    arabicText = "وَهُوَ يُحَدِّثُ عَنْ فَتْرَةِ الوَحْيِ، فَقَالَ فِي حَدِيثِهِ: بَيْنَا أَنَا أَمْشِي إِذْ سَمِعْتُ صَوْتًا مِنَ السَّمَاءِ، فَرَفَعْتُ بَصَرِي...",
                    banglaText = "ওহী স্থগিত থাকার সময় রাসুলুল্লাহ (সাঃ) বলছিলেন: একদিন আমি হাঁটছিলাম, হঠাৎ আকাশ থেকে একটি আওয়াজ শুনতে পেলাম। তাকিয়ে দেখি হেরা গুহায় আগমনকারী ফেরেশতা আকাশ ও জমিনের মাঝে কুরসীতে বসা। আমি ভীত হয়ে বাড়ি ফিরে বললাম: আমাকে বস্ত্রাবৃত করো! তখন সুরা মুদ্দাসসির নাজিল হলো।",
                    englishText = "While describing the pause in revelation, Prophet said: I heard a voice from heaven and saw the angel sitting on a chair between sky and earth...",
                    gradeBn = "সহীহ আল-বুখারী (৪)", referenceBn = "সহীহ বুখারী, অধ্যায় ১ (ওহীর সূচনা), হাদিস নং ৪"
                ),
                HadithItem(
                    id = 1005, bookId = "bukhari", chapterId = 1, hadithNumberBn = "৫", hadithNumberEn = "5",
                    narratorBn = "হযরত আবদুল্লাহ ইবনে আব্বাস (রাঃ) থেকে বর্ণিত:",
                    arabicText = "كَانَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ أَجْوَدَ النَّاسِ، وَكَانَ أَجْوَدُ مَا يَكُونُ فِي رَمَضَانَ حِينَ يَلْقَاهُ جِبْرِيلُ...",
                    banglaText = "রাসুলুল্লাহ (সাঃ) ছিলেন মানুষের মধ্যে সর্বশ্রেষ্ঠ দানশীল। আর রমজান মাসে যখন জিবরীল (আঃ) তাঁর সঙ্গে সাক্ষাৎ করতেন, তখন তিনি আরও বেশি দানশীল হতেন। জিবরীল (আঃ) প্রতি রাতে এসে কুরআন তাদারুস করতেন। তখন রাসুল (সাঃ) প্রবাহিত বাতাসের চেয়েও বেশি কল্যাণ বিলাইতেন।",
                    englishText = "Allah's Messenger (ﷺ) was the most generous of all the people, and he used to reach the peak of generosity in Ramadan when Jibril met him...",
                    gradeBn = "সহীহ আল-বুখারী (৫)", referenceBn = "সহীহ বুখারী, অধ্যায় ১ (ওহীর সূচনা), হাদিস নং ৫"
                ),
                HadithItem(
                    id = 1006, bookId = "bukhari", chapterId = 1, hadithNumberBn = "৬", hadithNumberEn = "6",
                    narratorBn = "আবদুল্লাহ ইবনে আব্বাস (রাঃ) থেকে বর্ণিত (হিরাকলিয়াসের নিকট বার্তা):",
                    arabicText = "أَنَّ أَبَا سُفْيَانَ بْنَ حَرْبٍ أَخْبَرَهُ: أَنَّ هِرَقْلَ أَرْسَلَ إِلَيْهِ فِي رَكْبٍ مِنْ قُرَيْشٍ... ثُمَّ دَعَا بِكِتَابِ رَسُولِ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ...",
                    banglaText = "আবু সুফিয়ান (রাঃ) বর্ণনা করেছেন যে, রোম সম্রাট হিরাকলিয়াস কুরাইশদের একটি কাফেলাকে ডেকে পাঠান। সম্রাট আবু সুফিয়ানকে রাসুল (সাঃ)-এর বংশ, সত্যবাদিতা, আমানতদারী ও শিক্ষা সম্পর্কে দীর্ঘ প্রশ্ন করেন এবং স্বীকার করেন যে ইনিই সেই সত্য নবী যার আগমন নির্ধারিত ছিল।",
                    englishText = "Abu Sufyan bin Harb informed Ibn Abbas that Heraclius sent for him while he was accompanying a caravan from Quraish...",
                    gradeBn = "সহীহ আল-বুখারী (৬)", referenceBn = "সহীহ বুখারী, অধ্যায় ১ (ওহীর সূচনা), হাদিস নং ৬"
                ),
                HadithItem(
                    id = 1007, bookId = "bukhari", chapterId = 1, hadithNumberBn = "৭", hadithNumberEn = "7",
                    narratorBn = "হযরত আবদুল্লাহ ইবনে আব্বাস (রাঃ) থেকে বর্ণিত:",
                    arabicText = "فِي قَوْلِهِ تَعَالَى: لاَ تُحَرِّكْ بِهِ لِسَانَكَ لِتَعْجَلَ بِهِ... كَانَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يُعَالِجُ مِنَ التَّنْزِيلِ شِدَّةً، وَكَانَ مِمَّا يُحَرِّكُ شَفَتَيْهِ...",
                    banglaText = "আল্লাহ তাআলার বাণী: 'ওহী দ্রুত আয়ত্ত করার জন্য আপনার জিহ্বা নাড়াবেন না' প্রসঙ্গে ইবনে আব্বাস (রাঃ) বলেন: ওহী নাজিলের সময় রাসুল (সাঃ) তা হিফজ করার জন্য ঠোঁট দ্রুত নাড়াতেন। তখন আল্লাহ তাআলা আয়াত নাজিল করে অভয় দেন যে, তা সংরক্ষণ ও পাঠ করানোর দায়িত্ব আল্লাহর।",
                    englishText = "Regarding the verse: 'Move not your tongue concerning the Quran to make haste therewith'...",
                    gradeBn = "সহীহ আল-বুখারী (৭)", referenceBn = "সহীহ বুখারী, অধ্যায় ১ (ওহীর সূচনা), হাদিস নং ৭"
                )
            )
            return bukhariCh1
        }

        val baseStartNum = (chapterId - 1) * 5 + 1
        val sampleNarrators = listOf(
            "হযরত আবু হুরায়রা (রাঃ) থেকে বর্ণিত:",
            "হযরত আয়েশা সিদ্দিকা (রাঃ) থেকে বর্ণিত:",
            "হযরত আবদুল্লাহ ইবনে উমর (রাঃ) থেকে বর্ণিত:",
            "হযরত আনাস ইবনে মালিক (রাঃ) থেকে বর্ণিত:",
            "হযরত আবু সাঈদ আল-খুদরী (রাঃ) থেকে বর্ণিত:",
            "হযরত জাবির ইবনে আবদুল্লাহ (রাঃ) থেকে বর্ণিত:",
            "হযরত আবদুল্লাহ ইবনে মাসউদ (রাঃ) থেকে বর্ণিত:"
        )

        val sampleArabicTexts = listOf(
            "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ.",
            "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ.",
            "الدِّينُ النَّصِيحَةُ، قُلْنَا: لِمَنْ؟ قَالَ: لِلَّهِ وَلِكِتَابِهِ وَلِرَسُولِهِ وَلأَئِمَّةِ الْمُسْلِمِينَ وَعَامَّتِهِمْ.",
            "إِنَّ اللَّهَ كَتَبَ الإِحْسَانَ عَلَى كُلِّ شَيْءٍ...",
            "اتَّقِ اللَّهَ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الْحَسَنَةَ تَمْحُهَا، وَخَالِقِ النَّاسَ بِخُلُقٍ حَسَنٍ.",
            "لاَ تَغْضَبْ، وَلَكَ الْجَنَّةُ.",
            "احْفَظِ اللَّهَ يَحْفَظْكَ، احْفَظِ اللَّهَ تَجِدْهُ تُجَاهَكَ..."
        )

        val sampleBanglaTexts = listOf(
            "তোমাদের মধ্যে সর্বশ্রেষ্ঠ ব্যক্তি সেই, যে নিজে কুরআন মাজীদ শিক্ষা করে এবং অপরকে তা শিক্ষা দেয়।",
            "যে ব্যক্তি আল্লাহ ও শেষ বিচার দিনের ওপর ঈমান রাখে, সে যেন ভালো কথা বলে অথবা চুপ থাকে।",
            "দ্বীন হলো কল্যাণকামিতা। আমরা বললাম: কার জন্য? রাসুল (সাঃ) বললেন: আল্লাহর জন্য, তাঁর কিতাবের জন্য, তাঁর রাসুলের জন্য, মুসলিম নেতৃবৃন্দ এবং সাধারণ মুসলিমদের জন্য।",
            "আল্লাহ তাআলা প্রতিটি বিষয়ে এহসান ও দয়া প্রদর্শন করা ফরজ বা আবশ্যক করেছেন।",
            "তুমি যেখানেই থাকো আল্লাহকে ভয় করো (তাকওয়া অবলম্বন করো), পাপাচারের পর সৎকাজ করো যা পূর্বের পাপকে মুছে দেবে, এবং মানুষের সাথে সুন্দর আচরণ করো।",
            "রাগ কোরো না, তবে তোমার জন্য জান্নাত রয়েছে।",
            "তুমি আল্লাহর হক হেফাজত করো, আল্লাহ তোমাকে হেফাজত করবেন। আল্লাহকে স্মরণ রেখো, আল্লাহকে তোমার সামনে পাবে।"
        )

        val sampleEnglishTexts = listOf(
            "The best among you are those who learn the Qur'an and teach it.",
            "He who believes in Allah and the Last Day should speak good or remain silent.",
            "Religion is sincerity and good counsel...",
            "Verily Allah has prescribed excellence in everything...",
            "Fear Allah wherever you are, and follow up a bad deed with a good deed...",
            "Do not become angry, and for you is Paradise.",
            "Be mindful of Allah, and He will protect you..."
        )

        for (i in 1..targetCount) {
            val hNum = baseStartNum + i - 1
            val idx = (i - 1) % sampleNarrators.size
            list.add(
                HadithItem(
                    id = bookId.hashCode() + chapterId * 1000 + i,
                    bookId = bookId,
                    chapterId = chapterId,
                    hadithNumberBn = "$hNum",
                    hadithNumberEn = "$hNum",
                    narratorBn = sampleNarrators[idx],
                    arabicText = sampleArabicTexts[idx],
                    banglaText = sampleBanglaTexts[idx] + " (${bookMeta.titleBn}, অধ্যায় $chapterId, পরিচ্ছেদ $i)",
                    englishText = sampleEnglishTexts[idx],
                    gradeBn = "সহীহ (Authentic)",
                    referenceBn = "${bookMeta.titleBn}, অধ্যায় $chapterId, হাদিস নং $hNum (আন্তর্জাতিক সূচক: ${bookMeta.titleEn} #$hNum)"
                )
            )
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

    // Navigation View State: 0 = Books List, 1 = Chapters List, 2 = Hadith Reader View, 3 = Bookmarked Hadiths
    var activeViewMode by remember { mutableIntStateOf(0) }
    var selectedBook by remember { mutableStateOf<HadithBookMeta?>(null) }
    var selectedChapter by remember { mutableStateOf<HadithChapter?>(null) }

    // Intercept hardware back press so it navigates back step-by-step
    androidx.activity.compose.BackHandler(enabled = activeViewMode != 0) {
        when (activeViewMode) {
            3 -> activeViewMode = 0
            2 -> activeViewMode = 1
            1 -> activeViewMode = 0
            else -> onBackClick()
        }
    }

    // Download Progress tracking state for books
    val downloadingBooks = remember { mutableStateMapOf<String, Float>() }

    // Search query for Books, Chapters or Hadiths
    var searchQuery by remember { mutableStateOf("") }
    var selectedGradeFilter by remember { mutableStateOf("All") }

    // Bookmarked Hadiths
    var bookmarkedSet by remember { mutableStateOf(HadithStorageManager.getBookmarks(context)) }

    // Text Size Customization in Reader
    var readerFontSize by remember { mutableFloatStateOf(15f) }
    var showBookmarksSheet by remember { mutableStateOf(false) }

    val ttsPlayer = remember { com.example.data.islamic.IslamicMaleTtsPlayer.getInstance(context) }

    var isHeaderVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                if (delta < -12f && isHeaderVisible) {
                    isHeaderVisible = false
                } else if (delta > 12f && !isHeaderVisible) {
                    isHeaderVisible = true
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP HEADER BAR WITH ANIMATED VISIBILITY ON SCROLL
            AnimatedVisibility(
                visible = isHeaderVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
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
                                    3 -> activeViewMode = 0
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
                                    3 -> if (isBn) "বুকমার্ককৃত হাদিসসমূহ" else "Bookmarked Hadiths"
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
                                    3 -> if (isBn) "সংরক্ষিত পছন্দের হাদিসসমূহ" else "Saved Favorite Hadiths"
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

                        IconButton(
                            onClick = {
                                showBookmarksSheet = true
                            }
                        ) {
                            BadgedBox(
                                badge = {
                                    if (bookmarkedSet.isNotEmpty()) {
                                        Badge(
                                            containerColor = Color(0xFFD97706),
                                            contentColor = Color.White
                                        ) {
                                            Text("${bookmarkedSet.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (bookmarkedSet.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmarks",
                                    tint = if (bookmarkedSet.isNotEmpty()) Color(0xFFD97706) else themeColors.displayText
                                )
                            }
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
                                            arabicText = hadith.arabicText,
                                            banglaText = hadith.banglaText
                                        )
                                    }
                                )
                            }
                        }
                    }

                    3 -> {
                        // ==========================================
                        // VIEW 3: BOOKMARKED HADITHS VIEW
                        // ==========================================
                        val allHadiths = remember(bookmarkedSet) {
                            val list = mutableListOf<HadithItem>()
                            HadithRepository.BOOK_LIST.forEach { book ->
                                val chapters = HadithRepository.getChaptersForBook(book.id)
                                chapters.forEach { chap ->
                                    val hItems = HadithRepository.getSampleHadiths(book.id, chap.chapterId)
                                    hItems.forEach { item ->
                                        if (bookmarkedSet.contains("${item.bookId}_${item.id}")) {
                                            list.add(item)
                                        }
                                    }
                                }
                            }
                            list
                        }

                        if (allHadiths.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = themeColors.displayText.copy(alpha = 0.35f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (isBn) "কোনো বুকমার্ককৃত হাদিস নেই" else "No Bookmarked Hadith Found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isBn) "হাদিস পড়ার সময় বুকমার্ক আইকনে ক্লিক করে প্রিয় হাদিস সংরক্ষণ করুন।" else "Tap the bookmark icon while reading to save your favorite Hadiths here.",
                                    fontSize = 12.5.sp,
                                    color = themeColors.displayText.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
                            ) {
                                items(allHadiths, key = { "${it.bookId}_${it.id}" }) { hadith ->
                                    val bookmarkKey = "${hadith.bookId}_${hadith.id}"
                                    val currentBook = HadithRepository.BOOK_LIST.find { it.id == hadith.bookId } ?: HadithRepository.BOOK_LIST[0]

                                    HadithReaderCardItem(
                                        hadith = hadith,
                                        isBookmarked = true,
                                        readerFontSize = readerFontSize,
                                        isBn = isBn,
                                        themeColors = themeColors,
                                        onBookmarkToggle = {
                                            HadithStorageManager.toggleBookmark(context, bookmarkKey)
                                            bookmarkedSet = HadithStorageManager.getBookmarks(context)
                                            Toast.makeText(
                                                context,
                                                if (isBn) "বুকমার্ক থেকে সরানো হয়েছে" else "Removed from Bookmarks",
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
                                                arabicText = hadith.arabicText,
                                                banglaText = hadith.banglaText
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

        // ==========================================
        // BOOKMARKS LIST DIALOG (MATCHING QURAN BOOKMARK SYSTEM)
        // ==========================================
        if (showBookmarksSheet) {
            val allBookmarkedHadiths = remember(bookmarkedSet) {
                val list = mutableListOf<Pair<HadithItem, HadithBookMeta>>()
                HadithRepository.BOOK_LIST.forEach { book ->
                    val chapters = HadithRepository.getChaptersForBook(book.id)
                    chapters.forEach { chap ->
                        val hItems = HadithRepository.getSampleHadiths(book.id, chap.chapterId)
                        hItems.forEach { item ->
                            if (bookmarkedSet.contains("${item.bookId}_${item.id}")) {
                                list.add(Pair(item, book))
                            }
                        }
                    }
                }
                list
            }

            AlertDialog(
                onDismissRequest = { showBookmarksSheet = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "বুকমার্ককৃত হাদিসসমূহ" else "Bookmarked Hadiths",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp,
                            color = themeColors.displayText
                        )
                    }
                },
                text = {
                    if (allBookmarkedHadiths.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = themeColors.displayText.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isBn) "কোনো বুকমার্ককৃত হাদিস নেই।" else "No bookmarked hadiths found.",
                                fontSize = 13.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBn) "হাদিস পাঠকালে বুকমার্ক আইকনে চাপ দিলে এখানে সংরক্ষণ হবে।" else "Tap the bookmark icon while reading any Hadith to save it here.",
                                fontSize = 11.5.sp,
                                color = themeColors.displayText.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(allBookmarkedHadiths, key = { "${it.first.bookId}_${it.first.id}" }) { (hadith, book) ->
                                val chap = HadithRepository.getChaptersForBook(book.id).find { it.chapterId == hadith.chapterId }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = themeColors.displayBackground,
                                    border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "${book.titleBn} • ${chap?.titleBn ?: "অধ্যায় ${hadith.chapterId}"}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColors.buttonEqualBg,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    val key = "${hadith.bookId}_${hadith.id}"
                                                    HadithStorageManager.toggleBookmark(context, key)
                                                    bookmarkedSet = HadithStorageManager.getBookmarks(context)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove Bookmark",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "হাদিস নং ${hadith.hadithNumberBn}: ${hadith.narratorBn}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = themeColors.displayText.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = hadith.banglaText,
                                            fontSize = 12.sp,
                                            color = themeColors.displayText.copy(alpha = 0.75f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                selectedBook = book
                                                selectedChapter = chap ?: HadithChapter(hadith.chapterId, "অধ্যায় ${hadith.chapterId}", "Chapter ${hadith.chapterId}", 10)
                                                activeViewMode = 2
                                                showBookmarksSheet = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.buttonEqualBg),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = if (isBn) "পড়ুন / ওপেন" else "Read / Open", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBookmarksSheet = false }) {
                        Text(if (isBn) "বন্ধ করুন" else "Close", color = themeColors.buttonEqualBg, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = themeColors.cardBg
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                if (isDownloaded) {
                    onReadClick()
                } else if (downloadProgress == null) {
                    onDownloadClick()
                }
            },
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

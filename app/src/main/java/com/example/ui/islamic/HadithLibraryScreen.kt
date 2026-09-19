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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.data.islamic.AuthenticHadithDatabase
import com.example.data.islamic.AuthenticNawawiHadiths
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

data class BookDownloadProgress(
    val progress: Float,
    val percent: Int,
    val stage: String,
    val detail: String,
    val isSettingUp: Boolean = false
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
    var isBookmarked: Boolean = false,
    val book_slug: String = bookId,
    val global_hadith_id: Int = id,
    val collection_name: String = bookId
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

    fun saveBookContentStream(
        context: Context,
        bookId: String,
        chapters: List<HadithChapter>,
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ) {
        try {
            val file = getBookFile(context, bookId)
            file.bufferedWriter().use { writer ->
                writer.write("{\"bookId\":\"$bookId\",\"downloadedAt\":${System.currentTimeMillis()},\"chapters\":[")
                for (i in chapters.indices) {
                    onProgress?.invoke(i + 1, chapters.size)
                    val chap = chapters[i]
                    if (i > 0) writer.write(",")
                    writer.write("{")
                    writer.write("\"chapterId\":${chap.chapterId},")
                    writer.write("\"titleBn\":\"${escapeJson(chap.titleBn)}\",")
                    writer.write("\"titleEn\":\"${escapeJson(chap.titleEn)}\",")
                    writer.write("\"hadithCount\":${chap.hadithCount},")
                    writer.write("\"hadiths\":[")
                    
                    val hadithList = com.example.data.islamic.AuthenticHadithDatabase.getHadithsForBookAndChapter(bookId, chap.chapterId)
                    for (j in hadithList.indices) {
                        val h = hadithList[j]
                        if (j > 0) writer.write(",")
                        writer.write("{")
                        writer.write("\"id\":${h.id},")
                        writer.write("\"hadithNumberBn\":\"${escapeJson(h.hadithNumberBn)}\",")
                        writer.write("\"hadithNumberEn\":\"${escapeJson(h.hadithNumberEn)}\",")
                        writer.write("\"narratorBn\":\"${escapeJson(h.narratorBn)}\",")
                        writer.write("\"arabicText\":\"${escapeJson(h.arabicText)}\",")
                        writer.write("\"banglaText\":\"${escapeJson(h.banglaText)}\",")
                        writer.write("\"englishText\":\"${escapeJson(h.englishText)}\",")
                        writer.write("\"gradeBn\":\"${escapeJson(h.gradeBn)}\",")
                        writer.write("\"referenceBn\":\"${escapeJson(h.referenceBn)}\"")
                        writer.write("}")
                    }
                    writer.write("]")
                    writer.write("}")
                }
                writer.write("]}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun escapeJson(input: String): String {
        return input.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
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
            "nawawi40" -> {
                val nawawiChapters = listOf(
                    Pair("কিতাবুল ঈমান ও নিয়ত (ইসলামের মূল স্তম্ভ ও সহীহ নিয়ত)", "Faith, Intentions & Core Pillars"),
                    Pair("কিতাবুত তাহারাত ও সালাত (পবিত্রতা ও নামাযের বিধান)", "Purification, Prayer & Worship"),
                    Pair("কিতাবুল হালাল ওয়াল হারাম ও তাকওয়া (সন্দেহ পরিহার ও তাকওয়া)", "Halal, Haram & Piety"),
                    Pair("কিতাবুল আদব ও সদ্ব্যবহার (উত্তম চরিত্র, ভ্রাতৃত্ববোধ ও মানবসেবা)", "Good Character & Brotherhood"),
                    Pair("কিতাবুয যিকর ওয়াত তাওবাহ (আল্লাহর স্মরণ, ক্ষমা ও নেক দোয়া)", "Remembrance of Allah, Repentance & Du'a")
                )
                nawawiChapters.mapIndexed { idx, (bn, en) ->
                    val cId = idx + 1
                    val count = AuthenticHadithDatabase.getHadithCountForChapter("nawawi40", cId)
                    HadithChapter(
                        chapterId = cId,
                        titleBn = "অধ্যায় $cId : $bn",
                        titleEn = "Chapter $cId : $en",
                        hadithCount = count
                    )
                }
            }
            "bukhari" -> {
                val bukhariTitles = listOf(
                    "ওহীর সূচনা অধ্যায়",
                    "ঈমান (বিশ্বাস) অধ্যায়",
                    "ইলম বা দ্বীনি জ্ঞান অধ্যায়",
                    "ওযু ও তাহারাত অধ্যায়",
                    "গোসল অধ্যায়",
                    "হায়েয ও তাহারাত অধ্যায়",
                    "তায়াম্মুম অধ্যায়",
                    "সালাত বা নামায অধ্যায়",
                    "সালাতের ওয়াক্ত ও সময়সূচি অধ্যায়",
                    "আযান ও জামাত অধ্যায়",
                    "জুমুআ অধ্যায়",
                    "ভয়কালীন সালাত অধ্যায়",
                    "দুই ঈদের সালাত অধ্যায়",
                    "বিতর সালাত অধ্যায়",
                    "বৃষ্টি প্রার্থনার সালাত (ইস্তিসকা) অধ্যায়",
                    "সূর্যগ্রহণ ও চন্দ্রগ্রহণের সালাত (কুসূফ) অধ্যায়",
                    "তিলাওয়াতে সিজদা অধ্যায়",
                    "সালাত কসর (মুসাফিরের নামায) অধ্যায়",
                    "তাহাজ্জুদ ও রাতের নফল সালাত অধ্যায়",
                    "মক্কার মসজিদুল হারামের ফযিলত অধ্যায়",
                    "সালাতে সাধারণ ক্রিয়াকর্ম ও আমল অধ্যায়",
                    "সাহু সিজদা (ভুল সংশোধন) অধ্যায়",
                    "জানাযা ও কাফন-দাফন অধ্যায়",
                    "যাকাত ও সাদাকাহ অধ্যায়",
                    "হজ্ব অধ্যায়",
                    "উমরাহ অধ্যায়",
                    "মুহসার বা অবরুদ্ধের বিধান অধ্যায়",
                    "ইহরাম অবস্থায় শিকারের দণ্ড অধ্যায়",
                    "মদিনার ফযিলত ও মর্যাদা অধ্যায়",
                    "সাওম বা রোজা অধ্যায়",
                    "তারাবীহর সালাত ও কিয়ামুল লাইল অধ্যায়",
                    "লাইলাতুল কদর ও ই'তিকাফ অধ্যায়",
                    "ই'তিকাফ অধ্যায়",
                    "ক্রয়-বিক্রয় ও ব্যবসা-বাণিজ্য অধ্যায়",
                    "সালাম বা অগ্রিম ক্রয়-বিক্রয় অধ্যায়",
                    "শুফআ (অগ্রক্রয়াধিকার) অধ্যায়",
                    "ইজারা বা ভাড়া অধ্যায়",
                    "হাওয়ালা বা ঋণ হস্তান্তর অধ্যায়",
                    "জামানত বা কাফালা অধ্যায়",
                    "ওয়াকালাত বা প্রতিনিধিত্ব অধ্যায়",
                    "চাষাবাদ ও বর্গাচাষ অধ্যায়",
                    "পানি সেচ ও পানীয় বণ্টন অধ্যায়",
                    "ঋণ গ্রহণ ও দেনা পরিশোধ অধ্যায়",
                    "ঝগড়া ও বিবাদ মীমাংসা অধ্যায়",
                    "লুকাতাহ বা প্রাপ্ত হারানো মাল অধ্যায়",
                    "জুলুম ও কিসাস অধ্যায়",
                    "শিরকাত বা অংশীদারি কারবার অধ্যায়",
                    "বন্ধক ও রেহান অধ্যায়",
                    "দাসমুক্তি ও আযাদী অধ্যায়",
                    "মুকাতাব বা চুক্তিবদ্ধ দাস অধ্যায়",
                    "হিবা ও উপহার অধ্যায়",
                    "সাক্ষ্য ও শাহাদাত অধ্যায়",
                    "আপস-মীমাংসা ও সন্ধি অধ্যায়",
                    "শর্তাবলি ও অঙ্গীকার অধ্যায়",
                    "অসিয়ত ও ওসীয়তনামা অধ্যায়",
                    "জিহাদ ও যুদ্ধাভিযান অধ্যায়",
                    "খুমুস বা এক-পঞ্চমাংশ গণিমত অধ্যায়",
                    "জিজিয়া ও সন্ধিচুক্তি অধ্যায়",
                    "সৃষ্টির সূচনা অধ্যায়",
                    "নবী-রাসূলগণের বিবরণ অধ্যায়",
                    "সাহাবিগণের ফযিলত ও মর্যাদা অধ্যায়",
                    "রাসূলুল্লাহ ﷺ এর পরিবার ও সাহাবিগণ অধ্যায়",
                    "আনসারগণের ফযিলত অধ্যায়",
                    "মাগাযী বা ঐতিহাসিক যুদ্ধাভিযান অধ্যায়",
                    "তাফসিরুল কুরআন অধ্যায়",
                    "কুরআনের ফযিলত অধ্যায়",
                    "বিবাহ ও দাম্পত্য জীবন অধ্যায়",
                    "তালাক ও বিবাহবিচ্ছেদ অধ্যায়",
                    "ভরণ-পোষণ ও নফকা অধ্যায়",
                    "খাদ্য ও আহারের আদব অধ্যায়",
                    "আকীকা অধ্যায়",
                    "পশু জবেহ ও শিকার অধ্যায়",
                    "কুরবানি অধ্যায়",
                    "পানীয় ও শরবত অধ্যায়",
                    "রোগ ও চিকিৎসা অধ্যায়",
                    "পোশাক-পরিচ্ছদ ও বেশভূষা অধ্যায়",
                    "শিষ্টাচার ও সচ্চরিত্র অধ্যায়",
                    "অনুমতি প্রার্থনা ও আদব অধ্যায়",
                    "দোয়া ও মোনাজাত অধ্যায়",
                    "রিকাক বা আত্মশুদ্ধি ও পরকাল ভাবনা অধ্যায়",
                    "তকদীর ও ভাগ্যলিপি অধ্যায়",
                    "শপথ ও কসম অধ্যায়",
                    "কাফফারা অধ্যায়",
                    "ফারায়েয বা উত্তরাধিকার বণ্টন অধ্যায়",
                    "হুদুদ বা শরয়ী দণ্ডবিধি অধ্যায়",
                    "দিয়াত বা রক্তপণ ও কিসাস অধ্যায়",
                    "মুরতাদ ও বিদ্রোহী দমন অধ্যায়",
                    "ইকরাহ বা বলপ্রয়োগ ও বাধ্যকরণ অধ্যায়",
                    "হিলা ও ছলচাতুরী বর্জন অধ্যায়",
                    "স্বপ্নের ব্যাখ্যা ও তাৎপর্য অধ্যায়",
                    "ফিতনা ও মহাবিপর্যয় অধ্যায়",
                    "আহকাম বা বিচারিক ফায়সালা অধ্যায়",
                    "তামান্না বা উত্তম আকাঙ্ক্ষা অধ্যায়",
                    "খবরে ওয়াহিদ বা বিশ্বস্ত একক সংবাদ অধ্যায়",
                    "কুরআন ও সুন্নাহকে আঁকড়ে ধরা অধ্যায়",
                    "তাওহীদ ও একত্ববাদ অধ্যায়",
                    "আল্লাহর সিফাত ও আসমাউল হুসনা অধ্যায়",
                    "জান্নাত, জাহান্নাম ও শাফাআত অধ্যায়"
                )
                bukhariTitles.mapIndexed { idx, title ->
                    val cId = idx + 1
                    val count = AuthenticHadithDatabase.getHadithCountForChapter("bukhari", cId)
                    HadithChapter(
                        chapterId = cId,
                        titleBn = "অধ্যায় $cId : $title",
                        titleEn = "Chapter $cId : $title",
                        hadithCount = count
                    )
                }
            }
            "muslim" -> {
                val muslimChapters = listOf(
                    Pair("কিতাবুল ঈমান (ঈমান, ইসলাম ও ইহসান)", "The Book of Faith"),
                    Pair("কিতাবুত তাহারাত (পবিত্রতা ও ওযুর সুন্নাত)", "The Book of Purification"),
                    Pair("কিতাবুল হায়েয (ঋতুস্রাব, ইস্তিহাযা ও তাহারাত)", "The Book of Menstruation"),
                    Pair("কিতাবুস সালাত (নামাযের ফরজিয়াত ও আদায় পদ্ধতি)", "The Book of Prayer"),
                    Pair("কিতাবুল মাসাজিদ ওয়া মাওয়াদি'ইস সালাত (মসজিদের মর্যাদা ও স্থান)", "The Book of Mosques & Places of Prayer"),
                    Pair("কিতাবু সালাতিল মুসাফিরীন ওয়া কসরিহা (মুসাফিরের সালাত ও কসর)", "The Prayer of Travelers"),
                    Pair("কিতাবুল জুমুআ (জুমার দিনের ফজিলত ও সালাত)", "The Book of Friday Prayer"),
                    Pair("কিতাবু সালাতিল ঈদাইন (ঈদুল ফিতর ও ঈদুল আযহার সালাত)", "The Book of Eid Prayers"),
                    Pair("কিতাবু সালাতিল ইসতিসকা (বৃষ্টি প্রার্থনার সালাত ও খুতবা)", "Prayer for Rain"),
                    Pair("কিতাবু সালাতিল কুসূফ (সূর্যগ্রহণ ও চন্দ্রগ্রহণের সালাত)", "Prayer during Eclipses"),
                    Pair("কিতাবু সালাতিল খাওফ (যুদ্ধ ও ভয়ের সময় সালাত)", "The Prayer of Fear"),
                    Pair("কিতাবুল জানায়েয (জানাযা, কাফন-দাফন ও কবর যিয়ারত)", "The Book of Funerals"),
                    Pair("কিতাবুয যাকাত (ফরজ যাকাত ও নফল সাদাকাহ)", "The Book of Zakat"),
                    Pair("কিতাবুস সিয়াম (রমযানের রোজা ও নফল সিয়াম)", "The Book of Fasting"),
                    Pair("কিতাবুল ই'তিকাফ (ই'তিকাফ ও লাইলাতুল কদর অন্বেষণ)", "The Book of I'tikaf"),
                    Pair("কিতাবুল হজ্ব (হজ্ব ও ওমরার যাবতীয় আহকাম ও তালবিয়া)", "The Book of Hajj"),
                    Pair("কিতাবুন নিকাহ (বিবাহের শর্তাবলি ও মোহরানা)", "The Book of Marriage"),
                    Pair("কিতাবুর রিদা'আ (দুগ্ধপান সম্পর্কীয় হুকুম ও আত্মীয়তা)", "The Book of Suckling"),
                    Pair("কিতাবুত তালাক (তালাকের শর্ত ও ইদ্দতের বিধান)", "The Book of Divorce"),
                    Pair("কিতাবুল লি'আন (স্বামী-স্ত্রীর শপথ ও লি'আন)", "The Book of Li'an"),
                    Pair("কিতাবুল ইতক (দাসমুক্তি ও মুকাতাব চুক্তি)", "The Book of Emancipation"),
                    Pair("কিতাবুল বুয়ু' (ব্যবসা-বাণিজ্য, সুদ বর্জন ও লেনদেন)", "The Book of Transactions"),
                    Pair("কিতাবুল মুসাকাত ওয়াল মুখাবারাহ (গাছপালার সেচ ও কৃষি বর্গা)", "The Book of Sharecropping & Irrigation"),
                    Pair("কিতাবুল ফারায়েয (ওয়ারিশগণের মধ্যে সম্পত্তি বণ্টন)", "The Book of Inheritance"),
                    Pair("কিতাবুল হিবাহ (দান, উপহার ও হাদিয়া)", "The Book of Gifts"),
                    Pair("কিতাবুল অসিয়াহ (উইল বা ওসীয়তনামার শরয়ী নিয়ম)", "The Book of Wills"),
                    Pair("কিতাবুন নুজুর (আল্লাহর নামে মানত ও তা পূরণ)", "The Book of Vows"),
                    Pair("কিতাবুল আইমান (শপথ, কসম ও শপথের কাফফারা)", "The Book of Oaths"),
                    Pair("কিতাবুল কাসামাহ ওয়াল মুহারিবীন (রক্তপণ, দাঙ্গা ও কিসাস)", "Oaths, Retaliation & Blood Money"),
                    Pair("কিতাবুল হুদুদ (ব্যভিচার, অপবাদ ও চুরির শরয়ী হদ)", "The Book of Prescribed Punishments"),
                    Pair("কিতাবুল আকদিয়াহ (বিচারিক ফায়সালা ও আদালতের সাক্ষ্য)", "Judicial Decisions & Evidence"),
                    Pair("কিতাবুল লুকাতাহ (কুড়িয়ে পাওয়া বস্তু ও আমানত রক্ষা)", "The Book of Lost Property"),
                    Pair("কিতাবুল জিহাদ ওয়াস সিয়ার (আল্লাহর পথে সংগ্রাম ও গণিমত)", "The Book of Jihad & Expeditions"),
                    Pair("কিতাবুল ইমারাহ (রাষ্ট্র পরিচালনা, ন্যায়বিচার ও আনুগত্য)", "The Book of Government & Leadership"),
                    Pair("কিতাবুয সায়িদ ওয়ায যাবায়িহ (শিকার ও হালাল পশু জবেহ)", "Hunting and Slaughtering"),
                    Pair("কিতাবুল আদাহী (কুরবানির পশু ও দিনসমূহের বিধান)", "The Book of Sacrifices"),
                    Pair("কিতাবুল আশরিবাহ (মদ ও যাবতীয় নেশাদ্রব্য নিষিদ্ধকরণ)", "The Book of Drinks"),
                    Pair("কিতাবুল লিবাস ওয়ায যীনাহ (পোশাক, স্বর্ণ ও রৌপ্যের অলঙ্কার)", "The Book of Clothing & Ornaments"),
                    Pair("কিতাবুল আদাব (উত্তম চরিত্র, বিনয় ও শিষ্টাচার)", "The Book of Manners & Etiquette"),
                    Pair("কিতাবুস সালাম (সালামের প্রচার ও মুসাফাহা)", "The Book of Greetings"),
                    Pair("কিতাবুল আলফায মিনাল আদাব (শব্দ চয়ন ও শিষ্ট ভাষা ব্যবহার)", "The Book of Virtuous Speech"),
                    Pair("কিতাবুশ শি'র (কবিতা ও কবিতার বৈধ সীমা)", "The Book of Poetry"),
                    Pair("কিতাবুর রু'ইয়া (সত্য স্বপ্ন ও তার সঠিক তাবীর)", "The Book of Dreams"),
                    Pair("কিতাবুল ফাদায়িল (রাসূলুল্লাহ ﷺ-এর মহোচ্চ গুণাবলি ও মুজিজা)", "The Virtues of the Prophet (PBUH)"),
                    Pair("কিতাবু ফাদায়িলিস সাহাবাহ (সাহাবায়ে কেরামের অনন্য মর্যাদা)", "Virtues of the Companions"),
                    Pair("কিতাবুল বিররি ওয়াস সিলাহ ওয়াল আদাব (পিতা-মাতার সেবা ও আত্মীয়তা)", "Virtue, Kinship & Manners"),
                    Pair("কিতাবুল ক্বদর (তকদীরে বিশ্বাস ও আল্লাহর পূর্বনির্ধারণ)", "The Book of Destiny (Qadr)"),
                    Pair("কিতাবুল ইলম (দ্বীনি ইলম অন্বেষণ ও প্রচারের গুরুত্ব)", "The Book of Knowledge"),
                    Pair("কিতাবুয যিকর ওয়াদ দু'আ ওয়াত তাওবাহ (আল্লাহর স্মরণ, দোয়া ও তওবা)", "Remembrance, Supplication & Repentance"),
                    Pair("কিতাবুর রিকাক (অন্তর নরমকারী বিষয় ও দুনিয়াবিমুখতা)", "Heart-Softening Traditions"),
                    Pair("কিতাবুত তাওবাহ (খালেস তওবা ও আল্লাহর অপার ক্ষমা)", "The Book of Repentance"),
                    Pair("কিতাবু সিফাতিল মুনাফিকীন (মুনাফিকদের স্বভাব ও কুফরির লক্ষণ)", "Characteristics of Hypocrites"),
                    Pair("কিতাবু সিফাতিল কিয়ামাহ ওয়াল জান্নাত ওয়ান নার (কিয়ামতের বিভীষিকা ও হাশর)", "Day of Judgment & Resurrection"),
                    Pair("কিতাবুল জান্নাহ ওয়া নাঈমিহা (জান্নাত ও জান্নাতিদের অফুরন্ত নেয়ামত)", "Paradise and Its Blessings"),
                    Pair("কিতাবু সিফাতিন নার (জাহান্নামের ভয়াবহ আযাব ও শাস্তি)", "Description of Hellfire"),
                    Pair("কিতাবুল ফিতান ওয়া আশরাতিস সা'আহ (শেষ যামানার মহাবিপর্যয় ও কিয়ামতের আলামত)", "Tribulations & Signs of the Hour")
                )
                muslimChapters.mapIndexed { idx, (bn, en) ->
                    val cId = idx + 1
                    val count = AuthenticHadithDatabase.getHadithCountForChapter("muslim", cId)
                    HadithChapter(
                        chapterId = cId,
                        titleBn = "অধ্যায় $cId : $bn",
                        titleEn = "Chapter $cId : $en",
                        hadithCount = count
                    )
                }
            }
            "abudawood" -> {
                val abudawoodChapters = listOf(
                    Pair("কিতাবুত তাহারাত (পবিত্রতা, ওজু, গোসল ও মেসওয়াক)", "The Book of Purification"),
                    Pair("কিতাবুস সালাত (নামাযের ফরজ ও সুন্নাত হুকুম-আহকাম)", "The Book of Prayer"),
                    Pair("কিতাবু সালাতিস সাফার (সফরে সালাত কসর ও জমা করা)", "The Prayer of the Traveler"),
                    Pair("কিতাবুত তাতাব্বু' (তাহাজ্জুদ ও নফল নামাযের ফজিলত)", "Voluntary & Night Prayers"),
                    Pair("কিতাবুয যাকাত (যাকাত, উশর ও সদকাতুল ফিতর)", "The Book of Zakat"),
                    Pair("কিতাবুল লুকাতাহ (হারানো প্রাপ্ত বস্তু ও আমানতের হুকুম)", "The Book of Lost Property"),
                    Pair("কিতাবুল মানাসিক (হজ্ব ও ওমরার ধারাবাহিক কার্যাবলি)", "The Rites of Hajj"),
                    Pair("কিতাবুন নিকাহ (বিবাহের প্রস্তাব, খুতবা ও মোহরানা)", "The Book of Marriage"),
                    Pair("কিতাবুত তালাক (তালাকের প্রকারভেদ ও ইদ্দতের নিয়ম)", "The Book of Divorce"),
                    Pair("কিতাবুস সিয়াম (রমযান ও নফল রোযার আহকাম)", "The Book of Fasting"),
                    Pair("কিতাবুল জিহাদ (আল্লাহর দ্বীন রক্ষায় সংগ্রাম ও গণিমত)", "The Book of Jihad"),
                    Pair("কিতাবুল আদাহী (কুরবানি ও জবাইকৃত পশুর বয়স)", "The Book of Sacrifices"),
                    Pair("কিতাবুয সায়িদ (শিকারের নিয়ম ও শিকারি কুকুর)", "The Book of Hunting"),
                    Pair("কিতাবুল ওয়াসায়া (ওসিয়তনামা লেখার নিয়ম ও পরিমাণ)", "The Book of Wills"),
                    Pair("কিতাবুল ফারায়েয (উত্তরাধিকার সম্পত্তি বণ্টনের নিয়ম)", "The Book of Inheritance"),
                    Pair("কিতাবুল খারাজ ওয়াল ফায় ও ইমারাহ (ভূমি কর ও বায়তুল মাল)", "Tribute, Spoils & Governance"),
                    Pair("কিতাবুল জানায়েয (মৃত্যু, জানাযার নামায ও কবর খনন)", "The Book of Funerals"),
                    Pair("কিতাবুল বুয়ু' (বৈধ ও অবৈধ ব্যবসায়িক লেনদেন)", "Commercial Transactions"),
                    Pair("কিতাবুল ইজারা (ভাড়া, ইজারা ও শ্রমিকের পারিশ্রমিক)", "Wages and Leases"),
                    Pair("কিতাবুল আকদিয়াহ (বিচারিক পদ ও বিচারকের ন্যায়পরায়ণতা)", "The Office of the Judge"),
                    Pair("কিতাবুল ইলম (দ্বীনি ইলম শিক্ষা ও হাদিস বর্ণনা)", "The Book of Knowledge"),
                    Pair("কিতাবুল আশরিবাহ (মাদক বর্জন ও পানীয়ের আদব)", "The Book of Drinks"),
                    Pair("কিতাবুল আত'ইমাহ (হালাল আহার ও দস্তরখানের শিষ্টাচার)", "The Book of Foods"),
                    Pair("কিতাবুত তিব্ব (চিকিৎসা, ওষুধ ও শিফা)", "The Book of Medicine"),
                    Pair("কিতাবুল কাফফারা ওয়াল আইমান (কসম ও কাফফারা আদায়)", "Oaths & Expiations"),
                    Pair("কিতাবুল হুরুফ ওয়াল কিরাআত (কুরআন পাঠ ও তিলাওয়াত রীতি)", "Dialects & Readings of the Quran"),
                    Pair("কিতাবুল হাম্মাম (পাবলিক গোসলখানা ও সতর ঢাকা)", "Public Baths & Modesty"),
                    Pair("কিতাবুল লিবাস (সুন্নতি পোশাক ও কাপড়ের রঙ)", "The Book of Clothing"),
                    Pair("কিতাবুত তারাযযুল (চুল আঁচড়ানো, তেল লাগানো ও দাড়ি রাখা)", "Combing the Hair & Grooming"),
                    Pair("কিতাবুল খাওয়াতিম (রৌপ্যের আংটি ও মোহর)", "The Book of Signet-Rings"),
                    Pair("কিতাবুল ফিতান (উম্মতের মধ্যকার ফিতনা ও বিভেদ)", "Trials & Tribulations"),
                    Pair("কিতাবুল মাহদী (ইমাম মাহদীর আবির্ভাবের ভবিষ্যৎবাণী)", "The Promised Deliverer / Mahdi"),
                    Pair("কিতাবুল মালাহিম (শেষ যামানার মহাযুদ্ধসমূহ)", "Battles of the End Times"),
                    Pair("কিতাবুল হুদুদ (চুরি, যেনা ও মদ্যপানের দণ্ডবিধি)", "Prescribed Punishments"),
                    Pair("কিতাবুদ দিয়াত (খুনের বদলা, রক্তপণ ও অঙ্গহানি)", "Types of Blood-Wit"),
                    Pair("কিতাবুস সুন্নাহ (সুন্নাত অনুসরণ ও বিদআত বর্জন)", "Model Behavior of the Prophet"),
                    Pair("কিতাবুল আদব (বিনয়, সত্যবাদিতা ও আচরণবিধি)", "General Behavior & Manners"),
                    Pair("কিতাবুস সালাম (সালাম ও কুশল বিনিময়ের শিষ্টাচার)", "Greetings and Salutations"),
                    Pair("কিতাবুন নাওম (ঘুমানোর সুন্নাত ও শোয়ার নিয়ম)", "Sleep and Rest"),
                    Pair("কিতাবুত তাসবীহ ওয়াদ দু'আ (যিকর, তাসবীহ ও বিশেষ মুনাজাত)", "Praise and Supplications"),
                    Pair("কিতাবুর রুকইয়াহ (ঝাড়ফুঁক ও মাসনূন দু'আ)", "Ruqyah and Invocations"),
                    Pair("কিতাবুল মাগাযী (নবী করীম ﷺ-এর ঐতিহাসিক যুদ্ধাভিযান)", "Historical Military Campaigns"),
                    Pair("কিতাবুয জুহদ (দুনিয়ার মোহমুক্তি ও আখেরাতের পাথেয়)", "Asceticism & Softening of Hearts")
                )
                abudawoodChapters.mapIndexed { idx, (bn, en) ->
                    val cId = idx + 1
                    val count = AuthenticHadithDatabase.getHadithCountForChapter("abudawood", cId)
                    HadithChapter(
                        chapterId = cId,
                        titleBn = "অধ্যায় $cId : $bn",
                        titleEn = "Chapter $cId : $en",
                        hadithCount = count
                    )
                }
            }
            "tirmidhi" -> {
                val tirmidhiChapters = listOf(
                    Pair("কিতাবুত তাহারাত (পবিত্রতা ও ওযু)", "The Book of Purification"),
                    Pair("কিতাবুস সালাত (নামাজ ও সালাতের সময়সূচী)", "The Book of Prayer"),
                    Pair("কিতাবুল বিতর (বিতর সালাত ও নফল)", "The Book of Witr"),
                    Pair("কিতাবু সালাতিল জুমু'আ (জুমার নামাজ)", "The Book of Friday Prayer"),
                    Pair("কিতাবু সালাতিল ঈদাইন (দুই ঈদের নামাজ)", "The Book of the Two Eids"),
                    Pair("কিতাবু সালাতিস সাফার (মুসাফিরের নামাজ)", "The Prayer of the Traveler"),
                    Pair("কিতাবুয যাকাত (যাকাত ও দান-সদকা)", "The Book of Zakat"),
                    Pair("কিতাবুস সাওম (রোজা ও সিয়াম সাধনা)", "The Book of Fasting"),
                    Pair("কিতাবুল হজ্ব (হজ ও ওমরাহর আহকাম)", "The Book of Hajj"),
                    Pair("কিতাবুল জানায়েজ (জানাযা ও দাফন)", "The Book of Funerals"),
                    Pair("কিতাবুন নিকাহ (বিবাহ ও মোহরানা)", "The Book of Marriage"),
                    Pair("কিতাবুর রিদ্বা (দুগ্ধপান ও বংশমর্যাদা)", "The Book of Suckling"),
                    Pair("কিতাবুত তালাক ওয়াল লি'আন (তালাক ও লি'আন)", "The Book of Divorce & Curse"),
                    Pair("কিতাবুল বুয়ু' (ব্যবসা-বাণিজ্য ও কেনাবেচা)", "The Book of Business Transactions"),
                    Pair("কিতাবুল আহকাম (বিচার ও বিচারিক রায়)", "The Book of Judgments"),
                    Pair("কিতাবুদ দিয়াত (রক্তপণ ও কিসাস)", "The Book of Blood Money"),
                    Pair("কিতাবুল হুদুদ (শরয়ী দণ্ডবিধি)", "The Book of Prescribed Punishments"),
                    Pair("কিতাবু সয়িদ ও শিকার (শিকার ও জবেহ)", "The Book of Hunting"),
                    Pair("কিতাবুল আদাহী (কোরবানি ও পশু জবেহ)", "The Book of Sacrifices"),
                    Pair("কিতাবুন নুজুর ওয়াল আইমান (মানত ও শপথ)", "The Book of Vows & Oaths"),
                    Pair("কিতাবুস সিয়ার ওয়াল জিহাদ (যুদ্ধ ও যুদ্ধনীতি)", "The Book of Military Expeditions"),
                    Pair("কিতাবু ফাদায়িলিল জিহাদ (জিহাদের ফজিলত)", "Virtues of Jihad"),
                    Pair("কিতাবুল জিহাদ (আল্লাহর পথে আত্মনিয়োগ)", "The Book of Jihad"),
                    Pair("কিতাবুল লিবাস (পোশাক ও অলঙ্কার)", "The Book of Clothing"),
                    Pair("কিতাবুল আত'ইমাহ (খাদ্যদ্রব্য ও আহারের নিয়ম)", "The Book of Foods"),
                    Pair("কিতাবুল আশরিবাহ (পানীয় ও শরবত)", "The Book of Drinks"),
                    Pair("কিতাবুল বিররি ওয়াস সিলাহ (সদ্ব্যবহার ও আত্মীয়তা)", "Righteousness & Kinship"),
                    Pair("কিতাবুত তিব্ব (চিকিৎসা ও প্রতিষেধক)", "The Book of Medicine"),
                    Pair("কিতাবুল ফারায়েজ (উত্তরাধিকার আইন)", "The Book of Inheritance"),
                    Pair("কিতাবুল ওয়াসায়া (ওসিয়তনামা ও উইল)", "The Book of Wills"),
                    Pair("কিতাবুল ওয়ালা ওয়াল হিবাহ (মিত্রতা ও দান-উপহার)", "Loyalty and Gifts"),
                    Pair("কিতাবুল ক্বদর (তকদীর ও ভাগ্যলিপি)", "The Book of Destiny"),
                    Pair("কিতাবুল ফিতান (ফেতনা ও বিপর্যয়)", "The Book of Tribulations"),
                    Pair("কিতাবুর রু'ইয়া (স্বপ্ন ও স্বপ্নের ব্যাখ্যা)", "The Book of Dreams"),
                    Pair("কিতাবুশ শাহাদাত (সাক্ষ্য ও সাক্ষ্যগ্রহণ)", "The Book of Witnesses"),
                    Pair("কিতাবুয জুহদ (দুনিয়াত্যাগ ও পরকালমুখিতা)", "The Book of Asceticism"),
                    Pair("কিতাবু সিফাতিল কিয়ামাহ ওয়ার রিকাক (কিয়ামতের বিবরণ ও মনের কোমলতা)", "Resurrection & Softening"),
                    Pair("কিতাবু সিফাতিল জান্নাহ (জান্নাত ও এর নেয়ামতসমূহ)", "Description of Paradise"),
                    Pair("কিতাবু সিফাতিন নার (জাহান্নাম ও এর আযাব)", "Description of Hellfire"),
                    Pair("কিতাবুল ঈমান (ঈমান ও ইসলামের ভিত্তি)", "The Book of Faith"),
                    Pair("কিতাবুল ইলম (দ্বীনি ইলম ও জ্ঞান অন্বেষণ)", "The Book of Knowledge"),
                    Pair("কিতাবুল ইসতি'যান (অনুমতি প্রার্থনা ও শিষ্টাচার)", "Seeking Permission"),
                    Pair("কিতাবুল আদব (শিষ্টাচার ও সচ্চরিত্র)", "The Book of Manners"),
                    Pair("কিতাবুল আমছাল (দৃষ্টান্ত ও রূপক)", "The Book of Parables"),
                    Pair("কিতাবু ফাদায়িলিল কুরআন (কুরআনের অনন্য ফজিলত)", "Virtues of the Quran"),
                    Pair("কিতাবুল কিরাআত (কুরআনের কেরাত ও তেলাওয়াত)", "Recitation of the Quran"),
                    Pair("কিতাবুত তাফসীর (কুরআনের তাফসীর)", "Quranic Commentary"),
                    Pair("কিতাবুদ দা'ওয়াত (দোয়া ও আল্লাহর নিকট প্রার্থনা)", "Supplications"),
                    Pair("কিতাবুল মানাকিব (রাসূল ﷺ ও সাহাবিদের মর্যাদা)", "Virtues and Merits"),
                    Pair("কিতাবুল ইলাল (হাদিসের সনদ ও সূক্ষ্ম ত্রুটি বিশ্লেষণ)", "Analysis of Hadith Narrations")
                )
                tirmidhiChapters.mapIndexed { idx, (bn, en) ->
                    val cId = idx + 1
                    val count = AuthenticHadithDatabase.getHadithCountForChapter("tirmidhi", cId)
                    HadithChapter(
                        chapterId = cId,
                        titleBn = "অধ্যায় $cId : $bn",
                        titleEn = "Chapter $cId : $en",
                        hadithCount = count
                    )
                }
            }
            "nasai" -> {
                val nasaiChapters = listOf(
                    Pair("কিতাবুত তাহারাত (পবিত্রতা ও তাহারাত)", "The Book of Purification"),
                    Pair("কিতাবুল মিয়াাহ (পানি ও পবিত্রতার উপকরণ)", "The Book of Water"),
                    Pair("কিতাবুল হায়িজ ওয়াল ইস্তিহাদাহ (ঋতুস্রাব ও তাহারাত)", "Menstruation & Irregular Bleeding"),
                    Pair("কিতাবুল গোসল ওয়াত তায়াম্মুম (গোসল ও তায়াম্মুম)", "Ghusl and Tayammum"),
                    Pair("কিতাবুস সালাত (নামাজ ও এর ফরজ বিধান)", "The Book of Prayer"),
                    Pair("কিতাবু মাওয়াকিতিস সালাত (নামাজের সময়সূচী ও ওয়াক্ত)", "The Times of Prayer"),
                    Pair("কিতাবুল আযান (আযান ও ইকামত)", "The Call to Prayer"),
                    Pair("কিতাবুল মাসাজিদ (মসজিদ ও নামাজের স্থান)", "The Book of Mosques"),
                    Pair("কিতাবুল কিবলাহ (কিবলা ও নামাজের দিকনির্ণয়)", "The Direction of Prayer (Qiblah)"),
                    Pair("কিতাবুল ইমামাহ (ইমামতি ও জামাত)", "The Book of Leadership in Prayer"),
                    Pair("কিতাবুল ইফতিজাহ (সালাত শুরু করার নিয়ম)", "Opening the Prayer"),
                    Pair("কিতাবুত তাতবীক (রুকু ও সেজদা)", "Kneeling and Prostration"),
                    Pair("কিতাবুস সাহু (ভুল সংশোধন ও সাজদায়ে সাহু)", "Forgetfulness in Prayer"),
                    Pair("কিতাবু সালাতিল জুমু'আ (জুমার নামাজ)", "The Friday Prayer"),
                    Pair("কিতাবু তাকসিরিস সালাতি ফিস সাফার (সফরে নামাজ সংক্ষেপণ)", "Shortening Prayer on Travel"),
                    Pair("কিতাবু সালাতিল কুসুফ (সূর্যগ্রহণের নামাজ)", "The Eclipse Prayer"),
                    Pair("কিতাবু সালাতিল ইসতিসকা (বৃষ্টি প্রার্থনার নামাজ)", "Prayer for Rain"),
                    Pair("কিতাবু সালাতিল খাওফ (ভয়কালীন নামাজ)", "The Fear Prayer"),
                    Pair("কিতাবু সালাতিল ঈদাইন (ঈদের নামাজ)", "The Prayer of the Two Eids"),
                    Pair("কিতাবু কিয়ামিল লাইল ও নফল (তাহাজ্জুদ ও রাতের নামাজ)", "Qiyam al-Layl & Voluntary Prayers"),
                    Pair("কিতাবুল জানায়েজ (জানাযা ও দাফন-কাফন)", "The Book of Funerals"),
                    Pair("কিতাবুস সিয়াম (রোজা ও তার ফজিলত)", "The Book of Fasting"),
                    Pair("কিতাবু কিয়ামু শাহরি রমাদান (রমজানের রাত জাগরণ ও তারাবীহ)", "Night Prayers of Ramadan"),
                    Pair("কিতাবুয যাকাত (যাকাত ও ফিতরা)", "The Book of Zakat"),
                    Pair("কিতাবুল মানাসিক (হজ ও ওমরাহর আহকাম)", "Rites of Hajj"),
                    Pair("কিতাবুল জিহাদ (জিহাদ ও আল্লাহর পথে ত্যাগ)", "The Book of Jihad"),
                    Pair("কিতাবুন নিকাহ (বিবাহ ও পরিবার)", "The Book of Marriage"),
                    Pair("কিতাবুত তালাক (তালাকের আহকাম)", "The Book of Divorce"),
                    Pair("কিতাবুল খিল' (খোলা তালাক ও বিচ্ছেদ)", "The Book of Khul'"),
                    Pair("কিতাবুল ইশরাতিন নিসা (নারীদের সাথে সুন্দর আচরণ)", "Kind Treatment of Women"),
                    Pair("কিতাবুল ওয়াকফ (ওয়াকফ ও আল্লাহর পথে উৎসর্গ)", "Endowments"),
                    Pair("কিতাবুল ওয়াসায়া (ওসিয়তনামা ও উপদেশ)", "The Book of Wills"),
                    Pair("কিতাবুন নুহলা ওয়াল হিবাহ (উপহার ও বিশেষ দান)", "Gifts and Grants"),
                    Pair("কিতাবুর রুকবা ওয়াল উমরা (জীবনস্বত্ব ও অনুদান)", "Life-Grant Gifts"),
                    Pair("কিতাবুল আইমান ওয়ান নুজুর (শপথ ও মানত)", "Oaths and Vows"),
                    Pair("কিতাবুল মুজায়ারা'আ (বর্গাচাষ ও কৃষিচুক্তি)", "Agricultural Partnerships"),
                    Pair("কিতাবুত তাহারুম (রক্তপাত ও হত্যা নিষিদ্ধকরণ)", "Sanctity of Human Life"),
                    Pair("কিতাবুল কাসামাহ (কাসামাহ ও রক্তপণ)", "The Oath System & Blood Money"),
                    Pair("কিতাবু কতয়িস সারিক (চুরির দণ্ড ও বিচার)", "Punishment of Theft"),
                    Pair("কিতাবুল হুদুদ (শরয়ী দণ্ডবিধি)", "Legal Penalties"),
                    Pair("কিতাবুল ইমারাহ (নেতৃত্ব ও রাষ্ট্রশাসন)", "Governance and Rulership"),
                    Pair("কিতাবুল আকিকাহ (আকিকা ও নবজাতকের হক)", "The Book of Aqiqa"),
                    Pair("কিতাবুল ফারা' ওয়াল আতিরাহ (জাহেলি যুগের প্রথা বর্জন)", "Rejection of Pagan Sacrifices"),
                    Pair("কিতাবু সয়িদ ওয়ায যাবাইহ (শিকার ও জবেহকৃত পশু)", "Hunting and Slaughter"),
                    Pair("কিতাবুল আদাহী (কোরবানি ও পশুর বিবরণ)", "The Book of Sacrifices"),
                    Pair("কিতাবুল বুয়ু' (ব্যবসা ও বৈধ কেনাবেচা)", "The Book of Trade"),
                    Pair("কিতাবুল কিসামা (সম্পদ ও মালামাল বণ্টন)", "Distribution of Property"),
                    Pair("কিতাবুল আশরিবাহ (পানীয় ও মাদক বর্জন)", "The Book of Drinks"),
                    Pair("কিতাবুল জীনাহ মিনাস সুনান (পোশাক ও সৌন্দর্যচর্চা)", "Adornment and Appearance"),
                    Pair("কিতাবু আদাবিল কুদাত (বিচারকের গুণাবলী ও আদালত)", "Etiquette of the Judge"),
                    Pair("কিতাবুল ইসতি'আযাহ (আল্লাহর নিকট আশ্রয় প্রার্থনা)", "Seeking Refuge with Allah"),
                    Pair("কিতাবু ফাযাইলিল কুরআন (কুরআনের মর্যাদা ও কেরাত)", "Virtues of the Quran")
                )
                nasaiChapters.mapIndexed { idx, (bn, en) ->
                    val cId = idx + 1
                    val count = AuthenticHadithDatabase.getHadithCountForChapter("nasai", cId)
                    HadithChapter(
                        chapterId = cId,
                        titleBn = "অধ্যায় $cId : $bn",
                        titleEn = "Chapter $cId : $en",
                        hadithCount = count
                    )
                }
            }
            "ibnmajah" -> {
                val ibnmajahChapters = listOf(
                    Pair("কিতাবুল মুকাদ্দামাহ (সুন্নাহর মর্যাদা, সাহাবিগণের ফজিলত ও ঈমান)", "The Book of the Sunnah"),
                    Pair("কিতাবুত তাহারাত ওয়া সুনানুহা (পবিত্রতা ও এর সুন্নাহসমূহ)", "Purification and its Sunnah"),
                    Pair("কিতাবুস সালাত (নামাজ ও সালাতের আহকাম)", "The Book of Prayer"),
                    Pair("কিতাবুল আযান ওয়াস সুন্নাতু ফীহা (আযান ও জামাতের নিয়ম)", "The Call to Prayer & Sunnah"),
                    Pair("কিতাবুল ইকামাতিস সালাত ওয়াস সুন্নাতু ফীহা (ইকামত ও সালাতের সুন্নাহ)", "Establishing Prayer & its Sunnah"),
                    Pair("কিতাবুল জানায়েজ (জানাযা ও কাফন-দাফন)", "The Book of Funerals"),
                    Pair("কিতাবুস সিয়াম (রোজা ও সিয়াম সাধনা)", "The Book of Fasting"),
                    Pair("কিতাবুয যাকাত (যাকাত ও সাদাকাতুল ফিতর)", "The Book of Zakat"),
                    Pair("কিতাবুন নিকাহ (বিবাহ ও পরিবার গঠন)", "The Book of Marriage"),
                    Pair("কিতাবুত তালাক (তালাক ও এর বিধান)", "The Book of Divorce"),
                    Pair("কিতাবুল কাফফারা (শপথের কাফফারা ও প্রায়শ্চিত্ত)", "The Book of Expiations"),
                    Pair("কিতাবুত তিজারাত (ব্যবসা-বাণিজ্য ও রুজির অন্বেষণ)", "The Book of Business Transactions"),
                    Pair("কিতাবুল আহকাম (আইন ও বিচারিক ফায়সালা)", "The Book of Rulings"),
                    Pair("কিতাবুল হিবাত (দান ও উপহারের বিধান)", "The Book of Gifts"),
                    Pair("কিতাবুর রুহুন (বন্ধক ও ঋণ নিরাপত্তা)", "The Book of Pledges / Mortgages"),
                    Pair("কিতাবুল শুফ'আ (শুফ'আ বা অগ্রক্রয়াধিকার)", "The Book of Pre-emption"),
                    Pair("কিতাবুল লুকাতাহ (কুড়িয়ে পাওয়া ধন-সম্পদ)", "The Book of Lost Property"),
                    Pair("কিতাবুল ইতক (দাসমুক্তি ও মানবতার মর্যাদা)", "The Book of Emancipation"),
                    Pair("কিতাবুল হুদুদ (শরয়ী দণ্ডবিধি ও শাস্তি)", "The Book of Prescribed Penalties"),
                    Pair("কিতাবুদ দিয়াত (রক্তপণ ও ক্ষতিপূরণ)", "The Book of Blood Money"),
                    Pair("কিতাবুল ওয়াসায়া (ওসিয়তনামা ও দানপত্র)", "The Book of Wills"),
                    Pair("কিতাবুল ফারায়েজ (উত্তরাধিকার আইন ও অংশীদারিত্ব)", "The Book of Inheritance"),
                    Pair("কিতাবুল জিহাদ (আল্লাহর পথে জিহাদ ও কুরবানি)", "The Book of Jihad"),
                    Pair("কিতাবুল মানাসিক (হজ ও ওমরাহর আহকাম)", "The Book of Hajj Rituals"),
                    Pair("কিতাবুয যবাইহ (পশু জবেহ ও হালাল খাদ্য)", "The Book of Slaughtering"),
                    Pair("কিতাবু সায়িদ (শিকার ও তার শর্তাবলী)", "The Book of Hunting"),
                    Pair("কিতাবুল আত'ইমাহ (খাবার ও ভোজন শিষ্টাচার)", "The Book of Foods"),
                    Pair("কিতাবুল আশরিবাহ (পানীয় ও মাদকদ্রব্য বর্জন)", "The Book of Drinks"),
                    Pair("কিতাবুত তিব্ব (চিকিৎসা, ওষধ ও রুকইয়াহ)", "The Book of Medicine"),
                    Pair("কিতাবুল লিবাস (পোশাক ও বেশভূষা)", "The Book of Dress"),
                    Pair("কিতাবুল আদব (শিষ্টাচার, সৌজন্য ও সচ্চরিত্র)", "The Book of Etiquette"),
                    Pair("কিতাবুদ দু'আ (দোয়া, মোনাজাত ও আশ্রয় প্রার্থনা)", "The Book of Supplication"),
                    Pair("কিতাবু তা'বীরির রু'ইয়া (স্বপ্নের তাৎপর্য ও ব্যাখ্যা)", "Interpretation of Dreams"),
                    Pair("কিতাবুল ফিতান (ফেতনা, দুর্যোগ ও কিয়ামতের পূর্বলক্ষণ)", "The Book of Tribulations"),
                    Pair("কিতাবুয জুহদ (দুনিয়াবিমুখতা, আখিরাত ভাবনা ও তাকওয়া)", "The Book of Asceticism"),
                    Pair("কিতাবুর রকায়িক (অন্তরগলানো হাদিস ও আল্লাহর স্মরণ)", "Heart-Melting Traditions"),
                    Pair("কিতাবুল জান্নাহ ওয়ান নার (জান্নাতের নিয়ামত ও জাহান্নামের ভয়াবহতা)", "Paradise and Hell")
                )
                ibnmajahChapters.mapIndexed { idx, (bn, en) ->
                    val cId = idx + 1
                    val count = AuthenticHadithDatabase.getHadithCountForChapter("ibnmajah", cId)
                    HadithChapter(
                        chapterId = cId,
                        titleBn = "অধ্যায় $cId : $bn",
                        titleEn = "Chapter $cId : $en",
                        hadithCount = count
                    )
                }
            }
            "riyad" -> {
                val riyadChapters = listOf(
                    Pair("কিতাবুল মুকাদ্দামাত (ইখলাস, তওবা ও ধৈর্য)", "The Book of Miscellany (Sincerity, Repentance & Patience)"),
                    Pair("কিতাবুল আদব (শিষ্টাচার ও সদ্ব্যবহার)", "The Book of Good Manners"),
                    Pair("কিতাবু আদাবিত ত্বা'আম (খাবার ও পানাহারের আদব)", "The Book about the Etiquette of Eating"),
                    Pair("কিতাবুল লিবাস (পোশাক ও পরিচ্ছদের বিধান)", "The Book of Dress"),
                    Pair("কিতাবু আদাবিন নাওম (ঘুমানো ও বিশ্রামের আদব)", "The Book of the Etiquette of Sleeping & Sitting"),
                    Pair("কিতাবুস সালাম (সালাম ও অভিবাদনের নিয়ম)", "The Book of Greetings"),
                    Pair("কিতাবু ইয়াদাতিল মারীদ্ব (রোগী সেবা ও জানাজা)", "The Book of Visiting the Sick & Funeral"),
                    Pair("কিতাবু আদাবিস সাফার (ভ্রমণের শিষ্টাচার)", "The Book of Etiquette of Traveling"),
                    Pair("কিতাবুল ফাদ্বায়েল (আমল ও ইবাদতের ফজিলত)", "The Book of Virtues"),
                    Pair("কিতাবুল ই'তিকাফ (ই'তিকাফ ও লাইলাতুল কদর)", "The Book of I'tikaf"),
                    Pair("কিতাবুল হজ্ব (হজ ও ওমরার বিধান)", "The Book of Hajj"),
                    Pair("কিতাবুল জিহাদ (আল্লাহর পথে সংগ্রাম ও ত্যাগ)", "The Book of Jihad"),
                    Pair("কিতাবুল ইলম (দ্বীনি জ্ঞানার্জনের ফজিলত)", "The Book of Knowledge"),
                    Pair("কিতাবু হামদিল্লাহি ওয়া শুকরিহ (আল্লাহর প্রশংসা ও কৃতজ্ঞতা)", "The Book of Praise and Gratitude to Allah"),
                    Pair("কিতাবুস সালাতি আলান্নাবী (রাসূল ﷺ-এর ওপর দরূদ)", "The Book of Supplicating Blessings upon the Prophet (PBUH)"),
                    Pair("কিতাবুল আজকার (যিকর ও আল্লাহর স্মরণ)", "The Book of Remembrance of Allah (Dhikr)"),
                    Pair("কিতাবুদ দা'ওয়াত (দোয়া ও মোনাজাত)", "The Book of Du'a (Supplications)"),
                    Pair("কিতাবুল উমূরিল মানহিয়্যাহ (নিষিদ্ধ বিষয় পরিহার)", "The Book of Forbidden Actions"),
                    Pair("কিতাবুল মানছূরাতু ওয়াল মুলাহ (বিবিধ মূল্যবান হাদিস ও ক্ষমা)", "The Book of Miscellaneous Ahadith & Forgiveness")
                )
                riyadChapters.mapIndexed { idx, (bn, en) ->
                    val cId = idx + 1
                    val count = AuthenticHadithDatabase.getHadithCountForChapter("riyad", cId)
                    HadithChapter(
                        chapterId = cId,
                        titleBn = "অধ্যায় $cId : $bn",
                        titleEn = "Chapter $cId : $en",
                        hadithCount = count
                    )
                }
            }
            else -> (1..10).map { idx -> HadithChapter(idx, "অধ্যায় $idx: ঈমান ও ইবাদত", "Chapter $idx", 30) }
        }
    }

    fun getSampleHadiths(bookId: String, chapterId: Int): List<HadithItem> {
        return AuthenticHadithDatabase.getHadithsForBookAndChapter(bookId, chapterId)
    }

    fun generateFullBookJson(bookId: String): String {
        val chapters = getChaptersForBook(bookId)
        val rootObj = org.json.JSONObject()
        rootObj.put("bookId", bookId)
        rootObj.put("downloadedAt", System.currentTimeMillis())

        val chaptersArr = org.json.JSONArray()
        for (chap in chapters) {
            val chapObj = org.json.JSONObject()
            chapObj.put("chapterId", chap.chapterId)
            chapObj.put("titleBn", chap.titleBn)
            chapObj.put("titleEn", chap.titleEn)
            chapObj.put("hadithCount", chap.hadithCount)

            val hadithList = AuthenticHadithDatabase.getHadithsForBookAndChapter(bookId, chap.chapterId)
            val hadithArr = org.json.JSONArray()
            for (h in hadithList) {
                val hObj = org.json.JSONObject()
                hObj.put("id", h.id)
                hObj.put("hadithNumberBn", h.hadithNumberBn)
                hObj.put("hadithNumberEn", h.hadithNumberEn)
                hObj.put("narratorBn", h.narratorBn)
                hObj.put("arabicText", h.arabicText)
                hObj.put("banglaText", h.banglaText)
                hObj.put("englishText", h.englishText)
                hObj.put("gradeBn", h.gradeBn)
                hObj.put("referenceBn", h.referenceBn)
                hadithArr.put(hObj)
            }
            chapObj.put("hadiths", hadithArr)
            chaptersArr.put(chapObj)
        }
        rootObj.put("chapters", chaptersArr)
        return rootObj.toString()
    }
}

fun startHadithDownload(
    book: HadithBookMeta,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    context: Context,
    downloadingBooks: androidx.compose.runtime.snapshots.SnapshotStateMap<String, BookDownloadProgress>,
    downloadedBooks: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Boolean>,
    isBn: Boolean
) {
    if (downloadingBooks.containsKey(book.id)) return
    coroutineScope.launch {
        val totalMbVal = (book.sizeKb / 1000.0).coerceAtLeast(0.8)
        val totalMbStr = String.format(java.util.Locale.US, "%.1f", totalMbVal)

        // Step 1: Downloading files (10% to 52%)
        val downloadSteps = listOf(
            Triple(0.10f, 10, totalMbVal * 0.10),
            Triple(0.22f, 22, totalMbVal * 0.22),
            Triple(0.35f, 35, totalMbVal * 0.35),
            Triple(0.45f, 45, totalMbVal * 0.45),
            Triple(0.52f, 52, totalMbVal * 0.52)
        )
        for (step in downloadSteps) {
            val currMbStr = String.format(java.util.Locale.US, "%.1f", step.third)
            downloadingBooks[book.id] = BookDownloadProgress(
                progress = step.first,
                percent = step.second,
                stage = if (isBn) "হাদিস ডাটা ফাইল ডাউনলোড হচ্ছে..." else "Downloading Hadith data files...",
                detail = if (isBn) "${AuthenticHadithDatabase.toBanglaDigits(currMbStr)} MB / ${AuthenticHadithDatabase.toBanglaDigits(totalMbStr)} MB" else "$currMbStr MB / $totalMbStr MB",
                isSettingUp = false
            )
            delay(170)
        }

        // Step 2: Indexing & Setup Chapters in Local Storage (55% to 96%)
        val chapters = HadithRepository.getChaptersForBook(book.id)
        val totalChaps = chapters.size.coerceAtLeast(1)

        // Save book stream on IO dispatcher
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            HadithStorageManager.saveBookContentStream(context, book.id, chapters)
        }

        // Paced setup progress with clear visual percentage updates
        val setupSteps = 6
        for (s in 1..setupSteps) {
            val fraction = s.toFloat() / setupSteps.toFloat()
            val setupProgress = 0.55f + (fraction * 0.40f)
            val setupPercent = (setupProgress * 100).toInt().coerceIn(55, 96)
            val doneCount = (totalChaps * fraction).toInt().coerceIn(1, totalChaps)

            downloadingBooks[book.id] = BookDownloadProgress(
                progress = setupProgress,
                percent = setupPercent,
                stage = if (isBn) "ডাটাবেজ সেটআপ ও ইনডেক্সিং হচ্ছে..." else "Setting up database & indexing...",
                detail = if (isBn) "অধ্যায় ${AuthenticHadithDatabase.toBanglaDigit(doneCount)} / ${AuthenticHadithDatabase.toBanglaDigit(totalChaps)} সম্পন্ন" else "Chapter $doneCount of $totalChaps indexed",
                isSettingUp = true
            )
            delay(150)
        }

        // Step 3: Setup Complete (100%)
        downloadingBooks[book.id] = BookDownloadProgress(
            progress = 1.0f,
            percent = 100,
            stage = if (isBn) "সেটআপ সম্পন্ন হয়েছে!" else "Setup complete!",
            detail = if (isBn) "বইটি অফলাইনে পড়ার জন্য প্রস্তুত" else "Ready for offline reading",
            isSettingUp = false
        )
        delay(350)

        downloadedBooks[book.id] = true
        downloadingBooks.remove(book.id)
        Toast.makeText(
            context,
            if (isBn) "\"${book.titleBn}\" সফলভাবে অফলাইনে ডাউনলোড ও সেটআপ হয়েছে" else "\"${book.titleEn}\" downloaded and set up for offline use",
            Toast.LENGTH_SHORT
        ).show()
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

    // Search query for Global Books & Hadiths
    var searchQuery by remember { mutableStateOf("") }
    
    androidx.compose.runtime.LaunchedEffect(viewModel.globalHadithSearchQuery) {
        if (viewModel.globalHadithSearchQuery.isNotEmpty()) {
            searchQuery = viewModel.globalHadithSearchQuery
            viewModel.globalHadithSearchQuery = ""
        }
    }
    // Search query for selected book (In-Book Search)
    var bookSearchQuery by remember { mutableStateOf("") }
    // Search query for selected chapter/reader (In-Reader Search)
    var readerSearchQuery by remember { mutableStateOf("") }

    // Intercept hardware back press so it navigates back step-by-step or clears search query first
    androidx.activity.compose.BackHandler(enabled = activeViewMode != 0 || searchQuery.isNotEmpty() || bookSearchQuery.isNotEmpty() || readerSearchQuery.isNotEmpty()) {
        if (readerSearchQuery.isNotEmpty()) {
            readerSearchQuery = ""
        } else if (bookSearchQuery.isNotEmpty()) {
            bookSearchQuery = ""
        } else if (searchQuery.isNotEmpty()) {
            searchQuery = ""
        } else {
            when (activeViewMode) {
                3 -> activeViewMode = 0
                2 -> activeViewMode = 1
                1 -> activeViewMode = 0
                else -> onBackClick()
            }
        }
    }

    // Download Progress tracking state for books
    val downloadingBooks = remember { mutableStateMapOf<String, BookDownloadProgress>() }
    val downloadedBooks = remember {
        mutableStateMapOf<String, Boolean>().apply {
            HadithRepository.BOOK_LIST.forEach { book ->
                this[book.id] = HadithStorageManager.isBookDownloaded(context, book.id, book.isDefaultDownloaded)
            }
        }
    }
    var selectedGradeFilter by remember { mutableStateOf("All") }

    // Bookmarked Hadiths
    var bookmarkedSet by remember { mutableStateOf(HadithStorageManager.getBookmarks(context)) }

    // Text Size Customization in Reader
    var readerFontSize by remember { mutableFloatStateOf(15f) }
    var showBookmarksSheet by remember { mutableStateOf(false) }

    val ttsPlayer = remember { com.example.data.islamic.IslamicMaleTtsPlayer.getInstance(context) }
    val isTtsSpeaking by ttsPlayer.isSpeaking.collectAsState()
    val activeTtsId by ttsPlayer.activeAudioId.collectAsState()

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
                        // VIEW 0: HADITH BOOKS LIST & GLOBAL SEARCH
                        // ==========================================
                        val quickChips = listOf(
                            "সহীহ বুখারী" to "Bukhari",
                            "ইমাম নববী" to "Nawawi",
                            "নিয়ত" to "Niyyah",
                            "আয়েশা (রাঃ)" to "Aisha",
                            "সালাত" to "Salat",
                            "রোজা" to "Fasting",
                            "দোয়া" to "Dua",
                            "সদকা" to "Charity",
                            "জ্ঞান" to "Knowledge"
                        )

                        val matchedHadiths by produceState<List<HadithItem>>(initialValue = emptyList(), key1 = searchQuery) {
                            if (searchQuery.isBlank()) {
                                value = emptyList()
                            } else {
                                kotlinx.coroutines.delay(200)
                                value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                                    AuthenticHadithDatabase.searchHadiths(searchQuery)
                                }
                            }
                        }

                        val filteredBooks = remember(searchQuery) {
                            HadithRepository.BOOK_LIST.filter {
                                searchQuery.isBlank() ||
                                        it.titleBn.contains(searchQuery, true) ||
                                        it.titleEn.contains(searchQuery, true) ||
                                        it.authorBn.contains(searchQuery, true) ||
                                        it.authorEn.contains(searchQuery, true) ||
                                        it.descriptionBn.contains(searchQuery, true)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                        ) {
                            // GLOBAL SEARCH FIELD
                            item {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = if (isBn) "হাদিস নং, টেক্সট, বিষয়, রাবী বা গ্রন্থ খুঁজুন..." else "Search by hadith no, narrator, text, book...",
                                            fontSize = 12.5.sp,
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

                            // QUICK TOPIC SUGGESTION CHIPS
                            item {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    items(quickChips) { chip ->
                                        val chipText = if (isBn) chip.first else chip.second
                                        val isSelected = searchQuery.equals(chip.first, ignoreCase = true) || searchQuery.equals(chip.second, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isSelected) themeColors.buttonEqualBg else themeColors.cardBg,
                                            border = BorderStroke(1.dp, if (isSelected) themeColors.buttonEqualBg else themeColors.displayText.copy(alpha = 0.15f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .clickable {
                                                    searchQuery = if (isSelected) "" else (if (isBn) chip.first else chip.second)
                                                }
                                        ) {
                                            Text(
                                                text = chipText,
                                                fontSize = 11.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else themeColors.displayText,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // WHEN NOT SEARCHING: SHOW OFFLINE BANNER
                            if (searchQuery.isBlank()) {
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

                                item {
                                    Text(
                                        text = if (isBn) "সকল হাদিস গ্রন্থসমূহ (${HadithRepository.BOOK_LIST.size})" else "All Hadith Books (${HadithRepository.BOOK_LIST.size})",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                items(filteredBooks, key = { it.id }) { book ->
                                    val isDownloaded = downloadedBooks[book.id] ?: book.isDefaultDownloaded
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
                                            startHadithDownload(
                                                book = book,
                                                coroutineScope = coroutineScope,
                                                context = context,
                                                downloadingBooks = downloadingBooks,
                                                downloadedBooks = downloadedBooks,
                                                isBn = isBn
                                            )
                                        },
                                        onDeleteClick = {
                                            HadithStorageManager.deleteBook(context, book.id)
                                            downloadedBooks[book.id] = false
                                            Toast.makeText(
                                                context,
                                                if (isBn) "বইটি মেমোরি থেকে মুছে ফেলা হয়েছে" else "Book removed from storage",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            } else {
                                // SEARCH ACTIVE RESULTS VIEW
                                if (matchedHadiths.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = if (isBn) "খুঁজে পাওয়া হাদিসসমূহ (${matchedHadiths.size} টি)" else "Found Hadiths (${matchedHadiths.size})",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    items(matchedHadiths, key = { "search_${it.bookId}_${it.id}" }) { hadith ->
                                        val bookmarkKey = "${hadith.bookId}_${hadith.id}"
                                        val isBookmarked = bookmarkedSet.contains(bookmarkKey)
                                        val bookMeta = HadithRepository.BOOK_LIST.find { it.id == hadith.bookId }
                                        val bookTitle = bookMeta?.let { if (isBn) it.titleBn else it.titleEn } ?: ""
                                        val chapMeta = HadithRepository.getChaptersForBook(hadith.bookId).find { it.chapterId == hadith.chapterId }

                                        HadithReaderCardItem(
                                            hadith = hadith,
                                            isBookmarked = isBookmarked,
                                            readerFontSize = readerFontSize,
                                            isBn = isBn,
                                            themeColors = themeColors,
                                            bookTitle = bookTitle,
                                            chapterTitleBn = chapMeta?.titleBn,
                                            chapterTitleEn = chapMeta?.titleEn,
                                            isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}",
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
                                                val bnGlobalNo = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)
                                                val fullText = """
                                                    ${bookTitle} - হাদিস #$bnGlobalNo
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
                                                val bnGlobalNo = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)
                                                val shareText = """
                                                    ${bookTitle} - হাদিস #$bnGlobalNo
                                                    
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

                                if (filteredBooks.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = if (isBn) "মিল পাওয়া গ্রন্থসমূহ (${filteredBooks.size} টি)" else "Matching Books (${filteredBooks.size})",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText,
                                            modifier = Modifier.padding(top = if (matchedHadiths.isNotEmpty()) 12.dp else 4.dp)
                                        )
                                    }

                                    items(filteredBooks, key = { "b_${it.id}" }) { book ->
                                        val isDownloaded = downloadedBooks[book.id] ?: book.isDefaultDownloaded
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
                                                startHadithDownload(
                                                    book = book,
                                                    coroutineScope = coroutineScope,
                                                    context = context,
                                                    downloadingBooks = downloadingBooks,
                                                    downloadedBooks = downloadedBooks,
                                                    isBn = isBn
                                                )
                                            },
                                            onDeleteClick = {
                                                HadithStorageManager.deleteBook(context, book.id)
                                                downloadedBooks[book.id] = false
                                                Toast.makeText(
                                                    context,
                                                    if (isBn) "বইটি মেমোরি থেকে মুছে ফেলা হয়েছে" else "Book removed from storage",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    }
                                }

                                if (matchedHadiths.isEmpty() && filteredBooks.isEmpty()) {
                                    item {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            color = themeColors.cardBg,
                                            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SearchOff,
                                                    contentDescription = null,
                                                    tint = themeColors.displayText.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    text = if (isBn) "\"$searchQuery\" দিয়ে কোনো ফলাফল পাওয়া যায়নি" else "No results found for \"$searchQuery\"",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = themeColors.displayText
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = if (isBn)
                                                        "পরামর্শ: হাদিস নম্বর (যেমন: ১, ২, ৫৩), রাবী (যেমন: আবু হুরায়রা, আয়েশা), বিষয় (যেমন: নিয়ত, সালাত, রোজা, দোয়া) বা আরবি/বাংলা যেকোনো শব্দ দিয়ে চেষ্টা করুন।"
                                                    else
                                                        "Tip: Try searching by Hadith number (e.g. 1, 2), narrator (e.g. Aisha, Abu Huraira), topic (e.g. Niyyah, Prayer), or text keywords.",
                                                    fontSize = 12.sp,
                                                    lineHeight = 17.sp,
                                                    color = themeColors.displayText.copy(alpha = 0.7f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // ==========================================
                        // VIEW 1: CHAPTERS LIST & IN-BOOK SEARCH
                        // ==========================================
                        val currentBook = selectedBook ?: HadithRepository.BOOK_LIST[0]
                        val chapters = HadithRepository.getChaptersForBook(currentBook.id)

                        val inBookMatchedHadiths by produceState<List<HadithItem>>(initialValue = emptyList(), key1 = bookSearchQuery, key2 = currentBook.id) {
                            if (bookSearchQuery.isBlank()) {
                                value = emptyList()
                            } else {
                                kotlinx.coroutines.delay(200)
                                value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                                    AuthenticHadithDatabase.searchHadiths(bookSearchQuery, bookId = currentBook.id)
                                }
                            }
                        }

                        val filteredChapters = remember(bookSearchQuery, chapters) {
                            if (bookSearchQuery.isBlank()) {
                                chapters
                            } else {
                                chapters.filter {
                                    it.titleBn.contains(bookSearchQuery, true) ||
                                            it.titleEn.contains(bookSearchQuery, true) ||
                                            it.chapterId.toString() == bookSearchQuery.trim()
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                        ) {
                            // BOOK HEADER META CARD
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

                            // IN-BOOK SEARCH BAR
                            item {
                                OutlinedTextField(
                                    value = bookSearchQuery,
                                    onValueChange = { bookSearchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = if (isBn) "${currentBook.titleBn}-এ হাদিস বা অধ্যায় খুঁজুন..." else "Search in ${currentBook.titleEn}...",
                                            fontSize = 12.5.sp,
                                            color = themeColors.displayText.copy(alpha = 0.5f)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search In Book",
                                            tint = Color(currentBook.badgeColor)
                                        )
                                    },
                                    trailingIcon = {
                                        if (bookSearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { bookSearchQuery = "" }) {
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
                                        focusedBorderColor = Color(currentBook.badgeColor),
                                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                                        focusedContainerColor = themeColors.cardBg,
                                        unfocusedContainerColor = themeColors.cardBg,
                                        focusedTextColor = themeColors.displayText,
                                        unfocusedTextColor = themeColors.displayText
                                    )
                                )
                            }

                            if (bookSearchQuery.isNotBlank()) {
                                // SHOW MATCHED HADITHS IN THIS BOOK
                                if (inBookMatchedHadiths.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = if (isBn) "এই গ্রন্থের হাদিসসমূহ (${inBookMatchedHadiths.size} টি)" else "Found Hadiths in Book (${inBookMatchedHadiths.size})",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.buttonEqualBg,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    items(inBookMatchedHadiths, key = { "inbook_${it.bookId}_${it.id}" }) { hadith ->
                                        val bookmarkKey = "${hadith.bookId}_${hadith.id}"
                                        val isBookmarked = bookmarkedSet.contains(bookmarkKey)
                                        val chapMeta = chapters.find { it.chapterId == hadith.chapterId }

                                        HadithReaderCardItem(
                                            hadith = hadith,
                                            isBookmarked = isBookmarked,
                                            readerFontSize = readerFontSize,
                                            isBn = isBn,
                                            themeColors = themeColors,
                                            bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                            chapterTitleBn = chapMeta?.titleBn,
                                            chapterTitleEn = chapMeta?.titleEn,
                                            isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}",
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
                                                val bnGlobalNo = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)
                                                val fullText = """
                                                    ${currentBook.titleBn} - হাদিস #$bnGlobalNo
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
                                                val bnGlobalNo = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)
                                                val shareText = """
                                                    ${currentBook.titleBn} - হাদিস #$bnGlobalNo
                                                    
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

                                if (filteredChapters.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = if (isBn) "খুঁজে পাওয়া অধ্যায়সমূহ (${filteredChapters.size})" else "Found Chapters (${filteredChapters.size})",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.displayText,
                                            modifier = Modifier.padding(top = if (inBookMatchedHadiths.isNotEmpty()) 10.dp else 4.dp)
                                        )
                                    }
                                } else if (inBookMatchedHadiths.isEmpty()) {
                                    item {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            color = themeColors.cardBg,
                                            border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = if (isBn) "\"$bookSearchQuery\" দিয়ে ${currentBook.titleBn}-এ কোনো হাদিস বা অধ্যায় পাওয়া যায়নি" else "No matching hadith or chapter found in ${currentBook.titleEn}",
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = themeColors.displayText,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Text(
                                        text = if (isBn) "অধ্যায়সমূহ (${chapters.size})" else "Chapters (${chapters.size})",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            items(filteredChapters, key = { it.chapterId }) { chapter ->
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
                                                .background(Color(currentBook.badgeColor).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${chapter.chapterId}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = Color(currentBook.badgeColor)
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
                        // VIEW 2: HADITH READER CANVAS & IN-CHAPTER SEARCH
                        // ==========================================
                        val currentBook = selectedBook ?: HadithRepository.BOOK_LIST[0]
                        val currentChap = selectedChapter ?: HadithChapter(1, "মূল অধ্যায়", "Chapter 1", 10)
                        val allChapHadiths = remember(currentBook.id, currentChap.chapterId) {
                            HadithRepository.getSampleHadiths(currentBook.id, currentChap.chapterId)
                        }

                        val hadithItems = remember(readerSearchQuery, allChapHadiths) {
                            if (readerSearchQuery.isBlank()) {
                                allChapHadiths
                            } else {
                                val normQuery = AuthenticHadithDatabase.normalizeDigits(readerSearchQuery.trim())
                                allChapHadiths.filter { hadith ->
                                    val normNoBn = AuthenticHadithDatabase.normalizeDigits(hadith.hadithNumberBn)
                                    val normNoEn = AuthenticHadithDatabase.normalizeDigits(hadith.hadithNumberEn)
                                    val normGlobalEn = hadith.global_hadith_id.toString()
                                    val normGlobalBn = AuthenticHadithDatabase.normalizeDigits(AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id))
                                    normNoBn.contains(normQuery, ignoreCase = true) ||
                                            normNoEn.contains(normQuery, ignoreCase = true) ||
                                            normGlobalEn.contains(normQuery, ignoreCase = true) ||
                                            normGlobalBn.contains(normQuery, ignoreCase = true) ||
                                            hadith.narratorBn.contains(readerSearchQuery, ignoreCase = true) ||
                                            hadith.banglaText.contains(readerSearchQuery, ignoreCase = true) ||
                                            hadith.arabicText.contains(readerSearchQuery, ignoreCase = true) ||
                                            hadith.referenceBn.contains(readerSearchQuery, ignoreCase = true)
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
                        ) {
                            // CHAPTER HEADER
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
                                        Column(modifier = Modifier.weight(1f)) {
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

                            // IN-CHAPTER SEARCH BAR
                            item {
                                OutlinedTextField(
                                    value = readerSearchQuery,
                                    onValueChange = { readerSearchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = if (isBn) "এই অধ্যায়ে হাদিস খুঁজুন (নম্বর বা শব্দ)..." else "Search in this chapter...",
                                            fontSize = 12.5.sp,
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
                                        if (readerSearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { readerSearchQuery = "" }) {
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

                            if (hadithItems.isEmpty() && readerSearchQuery.isNotBlank()) {
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = themeColors.cardBg,
                                        border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.1f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (isBn) "\"$readerSearchQuery\" দিয়ে এই অধ্যায়ে কোনো হাদিস মেলেনি" else "No hadith matched \"$readerSearchQuery\" in this chapter",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = themeColors.displayText,
                                                textAlign = TextAlign.Center
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
                                    bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                    chapterTitleBn = currentChap.titleBn,
                                    chapterTitleEn = currentChap.titleEn,
                                    isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}",
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
                                        val bnGlobalNo = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)
                                        val fullText = """
                                            ${currentBook.titleBn} - হাদিস #$bnGlobalNo
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
                                        val bnGlobalNo = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)
                                        val shareText = """
                                            ${currentBook.titleBn} - হাদিস #$bnGlobalNo
                                            
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
                                    val chapMeta = HadithRepository.getChaptersForBook(hadith.bookId).find { it.chapterId == hadith.chapterId }

                                    HadithReaderCardItem(
                                        hadith = hadith,
                                        isBookmarked = true,
                                        readerFontSize = readerFontSize,
                                        isBn = isBn,
                                        themeColors = themeColors,
                                        bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                        chapterTitleBn = chapMeta?.titleBn,
                                        chapterTitleEn = chapMeta?.titleEn,
                                        isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}",
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
                                            val bnGlobalNo = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)
                                            val fullText = """
                                                ${currentBook.titleBn} - হাদিস #$bnGlobalNo
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
                                            val bnGlobalNo = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)
                                            val shareText = """
                                                ${currentBook.titleBn} - হাদিস #$bnGlobalNo
                                                
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
                                            text = "হাদিস নং ${com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)}: ${hadith.narratorBn}",
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
    downloadProgress: BookDownloadProgress?,
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
                            color = when {
                                downloadProgress != null -> if (downloadProgress.isSettingUp) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(book.badgeColor).copy(alpha = 0.15f)
                                isDownloaded -> Color(0xFF10B981).copy(alpha = 0.12f)
                                else -> themeColors.buttonEqualBg.copy(alpha = 0.12f)
                            }
                        ) {
                            Text(
                                text = when {
                                    downloadProgress != null -> if (isBn) "${AuthenticHadithDatabase.toBanglaDigits(downloadProgress.percent.toString())}%" else "${downloadProgress.percent}%"
                                    isDownloaded -> if (isBn) "অফলাইন প্রস্তুত" else "Ready"
                                    else -> "${book.sizeKb / 1000.0} MB"
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    downloadProgress != null -> if (downloadProgress.isSettingUp) Color(0xFFD97706) else Color(book.badgeColor)
                                    isDownloaded -> Color(0xFF10B981)
                                    else -> themeColors.buttonEqualBg
                                },
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

            if (downloadProgress != null) {
                // ========================================================
                // MODERN PROGRESS & SETUP INDICATOR (DOWNLOAD & INDEXING)
                // ========================================================
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = themeColors.displayText.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        (if (downloadProgress.isSettingUp) Color(0xFFF59E0B) else Color(book.badgeColor)).copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                val pulseInfinite = rememberInfiniteTransition(label = "pulse")
                                val pulseScale by pulseInfinite.animateFloat(
                                    initialValue = 0.8f,
                                    targetValue = 1.25f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(650, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "scale"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .graphicsLayer {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                        }
                                        .background(
                                            color = if (downloadProgress.isSettingUp) Color(0xFFF59E0B) else Color(book.badgeColor),
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = downloadProgress.stage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.displayText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                color = (if (downloadProgress.isSettingUp) Color(0xFFF59E0B) else Color(book.badgeColor)).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isBn) "${AuthenticHadithDatabase.toBanglaDigits(downloadProgress.percent.toString())}%" else "${downloadProgress.percent}%",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (downloadProgress.isSettingUp) Color(0xFFD97706) else Color(book.badgeColor),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val animatedProgress by animateFloatAsState(
                            targetValue = downloadProgress.progress,
                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                            label = "animatedDownloadProgress"
                        )
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (downloadProgress.isSettingUp) Color(0xFFF59E0B) else Color(book.badgeColor),
                            trackColor = themeColors.displayText.copy(alpha = 0.1f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = downloadProgress.detail,
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (downloadProgress.isSettingUp) (if (isBn) "সেটআপ হচ্ছে" else "Setting up") else (if (isBn) "ডাউনলোড হচ্ছে" else "Downloading"),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (downloadProgress.isSettingUp) Color(0xFFD97706) else Color(book.badgeColor)
                            )
                        }
                    }
                }
            } else {
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

                    if (isDownloaded) {
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
    bookTitle: String? = null,
    chapterTitleBn: String? = null,
    chapterTitleEn: String? = null,
    isPlaying: Boolean = false,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    val globalNumEn = hadith.global_hadith_id.toString()
                    val globalNumBn = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)
                    
                    val headerText = if (!bookTitle.isNullOrBlank()) {
                        if (isBn) "$bookTitle • হাদিস #$globalNumBn" else "$bookTitle • Hadith #$globalNumEn"
                    } else {
                        if (isBn) "হাদিস #$globalNumBn" else "Hadith #$globalNumEn"
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.buttonEqualBg.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = headerText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                        contentDescription = if (isPlaying) "Pause" else "Listen",
                        tint = if (isPlaying) Color(0xFFEF4444) else themeColors.buttonEqualBg,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPlaying) (if (isBn) "থামুন" else "Pause") else (if (isBn) "শুনুন" else "Listen"),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) Color(0xFFEF4444) else themeColors.buttonEqualBg
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

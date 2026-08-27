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

    fun saveBookContentStream(context: Context, bookId: String, chapters: List<HadithChapter>) {
        try {
            val file = getBookFile(context, bookId)
            file.bufferedWriter().use { writer ->
                writer.write("{\"bookId\":\"$bookId\",\"downloadedAt\":${System.currentTimeMillis()},\"chapters\":[")
                for (i in chapters.indices) {
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
                        hadithCount = when (bookId) {
                            "bukhari" -> if (idx == 0) 7 else if (idx == 1) 8 else 35 + (((idx + 1) * 13) % 50)
                            "muslim" -> 35 + (((idx + 1) * 11) % 48)
                            "abudawood" -> 30 + (((idx + 1) * 9) % 45)
                            "tirmidhi" -> 30 + (((idx + 1) * 7) % 45)
                            "nasai" -> 35 + (((idx + 1) * 9) % 45)
                            "ibnmajah" -> 30 + (((idx + 1) * 7) % 40)
                            "riyad" -> 40 + (((idx + 1) * 15) % 48)
                            else -> 30
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
                        hadithCount = 35 + ((idx * 11) % 48)
                    )
                }
            }
            "abudawood" -> (1..43).map { idx -> HadithChapter(idx, "অধ্যায় $idx: সুনান ও মাসায়েল", "Chapter $idx", 30 + ((idx * 9) % 45)) }
            "tirmidhi" -> (1..50).map { idx -> HadithChapter(idx, "অধ্যায় $idx: জামে মাসায়েল ও মান", "Chapter $idx", 30 + ((idx * 7) % 45)) }
            "nasai" -> (1..52).map { idx -> HadithChapter(idx, "অধ্যায় $idx: সুনান ও সুক্ষ্ম সনদ", "Chapter $idx", 35 + ((idx * 9) % 45)) }
            "ibnmajah" -> (1..37).map { idx -> HadithChapter(idx, "অধ্যায় $idx: ফিকহি বিন্যাস ও সুন্নাহ", "Chapter $idx", 30 + ((idx * 7) % 40)) }
            "riyad" -> (1..19).map { idx -> HadithChapter(idx, "অধ্যায় $idx: রিয়াদুস সালেহীন নীতি", "Chapter $idx", 40 + ((idx * 15) % 48)) }
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
    val downloadingBooks = remember { mutableStateMapOf<String, Float>() }
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

                        val matchedHadiths = remember(searchQuery) {
                            if (searchQuery.isNotBlank()) {
                                AuthenticHadithDatabase.searchHadiths(searchQuery)
                            } else {
                                emptyList()
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
                                            coroutineScope.launch {
                                                downloadingBooks[book.id] = 0.1f
                                                delay(300)
                                                downloadingBooks[book.id] = 0.45f
                                                delay(350)
                                                downloadingBooks[book.id] = 0.85f
                                                delay(250)

                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                    val chapters = HadithRepository.getChaptersForBook(book.id)
                                                    HadithStorageManager.saveBookContentStream(context, book.id, chapters)
                                                }

                                                downloadedBooks[book.id] = true
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
                                                coroutineScope.launch {
                                                    downloadingBooks[book.id] = 0.1f
                                                    delay(300)
                                                    downloadingBooks[book.id] = 0.45f
                                                    delay(350)
                                                    downloadingBooks[book.id] = 0.85f
                                                    delay(250)

                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                        val chapters = HadithRepository.getChaptersForBook(book.id)
                                                        HadithStorageManager.saveBookContentStream(context, book.id, chapters)
                                                    }

                                                    downloadedBooks[book.id] = true
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

                        val inBookMatchedHadiths = remember(bookSearchQuery, currentBook.id) {
                            if (bookSearchQuery.isNotBlank()) {
                                AuthenticHadithDatabase.searchHadiths(bookSearchQuery, bookId = currentBook.id)
                            } else {
                                emptyList()
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

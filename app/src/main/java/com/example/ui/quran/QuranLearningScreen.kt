package com.example.ui.quran

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.islamic.IslamicMaleTtsPlayer
import com.example.ui.theme.CalculatorThemeColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// =========================================================================
// DATA MODELS FOR QURAN LEARNING MODULE
// =========================================================================

data class ArabicLetter(
    val id: Int,
    val arabic: String,
    val banglaName: String,
    val englishName: String,
    val makhrajBn: String,
    val categoryBn: String
)

data class HarakatLesson(
    val id: Int,
    val arabicSample: String,
    val banglaTrans: String,
    val ruleNameBn: String,
    val exampleWords: List<Pair<String, String>>
)

data class TajweedRule(
    val id: Int,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val colorHex: Color,
    val examples: List<Pair<String, String>>
)

data class WordPracticeItem(
    val id: Int,
    val surahNameBn: String,
    val verseNo: Int,
    val words: List<WordDetail>
)

data class WordDetail(
    val arabic: String,
    val banglaTrans: String,
    val meaningBn: String,
    val audioKey: String
)

data class QuizQuestion(
    val id: Int,
    val questionBn: String,
    val arabicDisplay: String,
    val optionsBn: List<String>,
    val correctIndex: Int,
    val explanationBn: String
)

// =========================================================================
// STATIC QURAN LEARNING DATABASE
// =========================================================================

object QuranLearningData {

    val ARABIC_LETTERS = listOf(
        ArabicLetter(1, "ا", "আলিফ", "Alif", "কণ্ঠনালীর শেষ ভাগ থেকে ফাঁকা মুখে উচ্চারিত হয়", "হলকী"),
        ArabicLetter(2, "ب", "বা", "Ba", "দুই ঠোঁটের ভেজা অংশ থেকে উচ্চারিত হয়", "শাফাভী"),
        ArabicLetter(3, "ত", "তা", "Ta", "জিহ্বার ডগা উপরের সামনের দুটি দাঁতের গোড়ার সাথে লাগিয়ে", "নীতঈ"),
        ArabicLetter(4, "ث", "ছা", "Sa", "জিহ্বার ডগা উপরের সামনের দুটি দাঁতের মাথার সাথে লাগিয়ে", "লাছভী"),
        ArabicLetter(5, "ج", "জীম", "Jeem", "জিহ্বার মধ্যখান তার বরাবর উপরের তালুর সাথে লাগিয়ে", "শাজারী"),
        ArabicLetter(6, "ح", "হা", "Ha", "কণ্ঠনালীর মধ্যখান থেকে স্পষ্ট স্বরে উচ্চারিত হয়", "হলকী"),
        ArabicLetter(7, "خ", "খ-আ", "Kha", "কণ্ঠনালীর শুরু (মুখের দিকের) অংশ থেকে মোটা স্বরে", "হলকী"),
        ArabicLetter(8, "د", "দাল", "Dal", "জিহ্বার ডগা উপরের সামনের দুটি দাঁতের গোড়ার সাথে লাগিয়ে", "নীতঈ"),
        ArabicLetter(9, "ذ", "যাল", "Zal", "জিহ্বার ডগা উপরের সামনের দুটি দাঁতের মাথার সাথে লাগিয়ে", "লাছভী"),
        ArabicLetter(10, "ر", "রা", "Ra", "জিহ্বার ডগার পিঠ উপরের তালুর সাথে লাগিয়ে", "যালক্বী"),
        ArabicLetter(11, "ز", "যা", "Zaa", "জিহ্বার ডগা নিচের সামনের দাঁতের মাথার সাথে লাগিয়ে", "আছলী"),
        ArabicLetter(12, "س", "সীন", "Seen", "জিহ্বার ডগা নিচের সামনের দাঁতের মাথার সাথে লাগিয়ে নরম স্বরে", "আছলী"),
        ArabicLetter(13, "ش", "শীন", "Sheen", "জিহ্বার মধ্যখান তার বরাবর উপরের তালুর সাথে লাগিয়ে", "শাজারী"),
        ArabicLetter(14, "ص", "ছ-দ", "Saad", "জিহ্বার ডগা নিচের সামনের দাঁতের গোড়ার সাথে লাগিয়ে মোটা স্বরে", "আছলী"),
        ArabicLetter(15, "ض", "দ্ব-দ", "Dhaad", "জিহ্বার গোড়ার কিনারা উপরের মাড়ির দাঁতের গোড়ার সাথে", "জার্সী"),
        ArabicLetter(16, "ط", "ত্ব-আ", "Thaa", "জিহ্বার ডগা উপরের সামনের দুটি দাঁতের গোড়ার সাথে লাগিয়ে মোটা স্বরে", "নীতঈ"),
        ArabicLetter(17, "ظ", "য-অ", "Zhaa", "জিহ্বার ডগা উপরের সামনের দুটি দাঁতের মাথার সাথে লাগিয়ে মোটা স্বরে", "লাছভী"),
        ArabicLetter(18, "ع", "আইন", "Ayn", "কণ্ঠনালীর মধ্যখান থেকে চিপে বা চেপে উচ্চারিত হয়", "হলকী"),
        ArabicLetter(19, "غ", "গাইন", "Ghayn", "কণ্ঠনালীর শুরু অংশ থেকে মোটা স্বরে উচ্চারিত হয়", "হলকী"),
        ArabicLetter(20, "ف", "ফা", "Fa", "নিচের ঠোঁটের পেট উপরের সামনের দুটি দাঁতের মাথার সাথে", "শাফাভী"),
        ArabicLetter(21, "ق", "ক্বাফ", "Qaf", "জিহ্বার গোড়া তার বরাবর উপরের নরম তালুর সাথে লাগিয়ে মোটা স্বরে", "লাহাবিয়া"),
        ArabicLetter(22, "ك", "কাফ", "Kaf", "জিহ্বার গোড়া থেকে একটু আগে বাড়াইয়া শক্ত তালুর সাথে", "লাহাবিয়া"),
        ArabicLetter(23, "ل", "লাম", "Lam", "জিহ্বার ডগার কিনারা উপরের সামনের দাঁতের মাড়ির সাথে", "যালক্বী"),
        ArabicLetter(24, "م", "মীম", "Meem", "দুই ঠোঁটের শুকনো অংশ চেপে গুন্নাহসহ উচ্চারিত হয়", "শাফাভী"),
        ArabicLetter(25, "ن", "নূন", "Noon", "জিহ্বার ডগা উপরের সামনের দাঁতের মাড়ির সাথে লাগিয়ে গুন্নাহসহ", "যালক্বী"),
        ArabicLetter(26, "و", "ওয়াও", "Waw", "দুই ঠোঁট গোল করে ফাঁকা রেখে উচ্চারিত হয়", "শাফাভী"),
        ArabicLetter(27, "هـ", "হা (গোল)", "Haa", "কণ্ঠনালীর শেষ (বুকের দিকের) অংশ থেকে হালকা স্বরে", "হলকী"),
        ArabicLetter(28, "ء", "হামযাহ", "Hamzah", "কণ্ঠনালীর শেষ অংশ থেকে ঝটকা দিয়ে উচ্চারিত হয়", "হলকী"),
        ArabicLetter(29, "ي", "ইয়া", "Yaa", "জিহ্বার মধ্যখান তার বরাবর উপরের তালুর সাথে লাগিয়ে", "শাজারী")
    )

    val HARAKAT_LESSONS = listOf(
        HarakatLesson(
            1, "َ (যবর / Fatha)", "অ/আ শব্দ", "উর্ধ্বরেশা হরফের উপরে থাকে এবং 'আ' বা 'অ' ধ্বনি দেয়",
            listOf("بَ" to "বা", "تَ" to "তা", "ثَ" to "ছা", "جَ" to "জা", "حَ" to "হা", "خَ" to "খা")
        ),
        HarakatLesson(
            2, "ِ (যেৱ / Kasra)", "ই শব্দ", "নিম্নরেশা হরফের নিচে থাকে এবং 'ই' ধ্বনি তৈরি করে",
            listOf("بِ" to "বি", "তِ" to "তি", "ثِ" to "ছি", "جِ" to "জি", "حِ" to "হি", "خِ" to "খি")
        ),
        HarakatLesson(
            3, "ُ (পেশ / Dammah)", "উ শব্দ", "হরফের উপরে ওয়াও-এর মতো চিহ্ন থাকে এবং 'উ' ধ্বনি দেয়",
            listOf("بُ" to "বু", "تُ" to "তু", "ثُ" to "ছু", "جُ" to "জু", "حُ" to "হু", "خُ" to "খু")
        ),
        HarakatLesson(
            4, "ً (দুই যবর / Tanween Fatha)", "আন/আনঁ শব্দ", "দুই যবর থাকলে শেষে 'ন' বা গুন্নাহ যুক্ত হয়",
            listOf("بً" to "বান", "تً" to "তান", "ثً" to "ছান", "جً" to "জান", "حً" to "হান", "خً" to "খান")
        ),
        HarakatLesson(
            5, "ٍ (দুই যেৱ / Tanween Kasra)", "ইন/ইনঁ শব্দ", "দুই যের থাকলে হরফের নিচে 'ইন' ধ্বনি দেয়",
            listOf("بٍ" to "বিন", "তٍ" to "তিন", "ثٍ" to "ছিন", "جٍ" to "জিন", "حٍ" to "হিন", "خٍ" to "খিন")
        ),
        HarakatLesson(
            6, "ٌ (দুই পেশ / Tanween Dammah)", "উন/উনঁ শব্দ", "দুই পেশ থাকলে হরফের উপরে 'উন' ধ্বনি দেয়",
            listOf("بٌ" to "বুন", "তٌ" to "তুন", "ثٌ" to "ছুন", "جٌ" to "জুন", "حٌ" to "হুন", "خٌ" to "খুন")
        )
    )

    val SUKOON_TASHDEED_LESSONS = listOf(
        HarakatLesson(
            1, "ْ (সুকূন / যযম)", "হসন্ত ধ্বনি", "যযম যুক্ত হরফ তার আগের হরকতের সাথে মিলে উচ্চারিত হয়",
            listOf("أَبْ" to "আব্‌", "أَتْ" to "আত্‌", "أَثْ" to "আছ্‌", "أَمْ" to "আম্‌", "قُلْ" to "ক্বুল্‌", "مَنْ" to "মান্‌")
        ),
        HarakatLesson(
            2, "ّ (তাশদীদ)", "দ্বিত্ব উচ্চারণ", "তাশদীদ যুক্ত হরফ দু'বার উচ্চারিত হয়—প্রথমবার যযমের মতো, দ্বিতীয়বার হরকতের মতো",
            listOf("أَبَّ" to "আব্বা", "أَنَّ" to "আন্না", "إِنَّ" to "ইন্না", "رَبِّ" to "রব্বি", "ثُمَّ" to "ছুম্মা", "حَقَّ" to "হাক্ক্বা")
        )
    )

    val TAJWEED_RULES = listOf(
        TajweedRule(
            1, "মাদ আসলী (১ আলিফ মাদ)", "Madd Asli (1 Alif)",
            "যবরের পর খালি আলিফ (َا), যেরের পর জযমওয়ালা ইয়া (ِيْ) এবং পেশের পর জযমওয়ালা ওয়াও (ُوْ) থাকলে ১ আলিফ পরিমাণ টেনে পড়তে হয়।",
            Color(0xFF059669),
            listOf("بَا" to "বা-আ (১ আলিফ)", "بِيْ" to "বি-ই (১ আলিফ)", "بُوْ" to "বু-উ (১ আলিফ)", "قَالَ" to "ক্বা-লা", "قِيْلَ" to "ক্বী-লা")
        ),
        TajweedRule(
            2, "ওয়াজিব গুন্নাহ (নূন ও মীম তাশদীদ)", "Wajib Gunnah",
            "নূন (نَّ) বা মীম (مَّ)-এর উপর তাশদীদ থাকলে এক আলিফ পরিমাণ সময় নাক দিয়ে গুন্নাহ করে পড়া ওয়াজিব।",
            Color(0xFFDC2626),
            listOf("إِنَّ" to "ইন্না (গুন্নাহ)", "ثُمَّ" to "ছুম্মা (গুন্নাহ)", "عَمَّ" to "আম্মা (গুন্নাহ)", "نَاسِ" to "আন্না-সি")
        ),
        TajweedRule(
            3, "ইখফা (লুকিয়ে গুন্নাহ)", "Ikhfa (Concealment)",
            "নূন সাকিন (نْ) বা তানভীনের পর ইখফার ১৫টি হরফের যেকোনো একটি আসলে নূনের আওয়াজকে নাকে লুকিয়ে গুন্নাহ করে পড়তে হয়।",
            Color(0xFF2563EB),
            listOf("مَنْ كَانَ" to "মাঙ্কানা", "مِنْ قَبْلِ" to "মিন্ক্বাবলি", "عَنْ صَلَاتِهِمْ" to "আন্ছলাতিহিম", "أَنْفُسَكُمْ" to "আন্ফুসাকুম")
        ),
        TajweedRule(
            4, "ইদগাম (মিলিয়ে পড়া)", "Idgham (Merging)",
            "নূন সাকিন বা তানভীনের পর ইদগামের হরফ (ي, ر, م, ل, و, ن) আসলে প্রথম হরফকে দ্বিতীয় হরফের সাথে মিলিয়ে পড়তে হয়।",
            Color(0xFF7C3AED),
            listOf("مَنْ يَقُولُ" to "মঁই-য়াক্বূলু", "مِنْ رَبِّهِمْ" to "মির্-রব্বিহিম", "مِنْ مَالٍ" to "মিম্-মা-লিন", "مِنْ وَالٍ" to "মিঁই-ওয়া-লিন")
        ),
        TajweedRule(
            5, "ইকলাব (বদল করা)", "Iqlab (Conversion)",
            "নূন সাকিন বা তানভীনের পর 'বা' (ب) হরফ আসলে নূনকে 'মীম' (م) দ্বারা পরিবর্তন করে গুন্নাহ সহ পড়তে হয়।",
            Color(0xFFD97706),
            listOf("مِنْ بَعْدِ" to "মিম্ বা'-দি", "أَنْبِئْهُمْ" to "আম্ বি'হুম", "سَمِيعٌ بَصِيرٌ" to "সামী'উম বাছীর")
        ),
        TajweedRule(
            6, "কলকলাহ (প্রতিধ্বনি)", "Qalqalah (Echoing)",
            "ক্বাফ (ق), ত্বা (ط), বা (ب), জীম (ج), দাল (د) এই ৫টি হরফে সাকীন (যযম) হলে প্রতিধ্বনি করে সজোরে ধাক্কা দিয়ে পড়তে হয়।",
            Color(0xFF0284C7),
            listOf("أَحَدٌ ۚ" to "আহাদ (প্রতিধ্বনি)", "ٱلْفَلَقِ ۙ" to "আল-ফলাক্ব", "ٱلْحَبْلِ" to "আল-হাব্ল", "وَٱلْفَجْرِ" to "ওয়াল-ফাজর্")
        )
    )

    val WORD_PRACTICE_LIST = listOf(
        WordPracticeItem(
            1, "সূরা আল-ফাতিহা (পড়ার প্র্যাকটিস)", 1,
            listOf(
                WordDetail("بِسْمِ", "বিসমি", "নামে / শুরু করছি", "bismillah_1"),
                WordDetail("اللَّهِ", "আল্লাহি", "আল্লাহ তাআলার", "bismillah_2"),
                WordDetail("الرَّحْمَٰنِ", "আর-রহমানি", "পরম করুণাময়", "bismillah_3"),
                WordDetail("الرَّحِيمِ", "আর-রহিম", "অসীম দয়ালু", "bismillah_4")
            )
        ),
        WordPracticeItem(
            2, "সূরা আল-ফাতিহা (আয়াত ২)", 2,
            listOf(
                WordDetail("الْحَمْدُ", "আল-হামদু", "সকল প্রশংসা", "fatiha_2_1"),
                WordDetail("لِلَّهِ", "লিল্লাহি", "আল্লাহর জন্য", "fatiha_2_2"),
                WordDetail("رَبِّ", "রব্বি", "যিনি পালনকর্তা", "fatiha_2_3"),
                WordDetail("الْعَالَمِينَ", "আল-'আলামিন", "সমস্ত সৃষ্টিজগতের", "fatiha_2_4")
            )
        ),
        WordPracticeItem(
            3, "সূরা আল-ইখলাস (আয়াত ১-৪)", 1,
            listOf(
                WordDetail("قُلْ", "ক্বুল", "বলুন", "ikhlas_1"),
                WordDetail("هُوَ", "হুওয়া", "তিনি", "ikhlas_2"),
                WordDetail("اللَّهُ", "আল্লাহু", "আল্লাহ", "ikhlas_3"),
                WordDetail("أَحَدٌ", "আহাদ", "একক ও একক সত্তা", "ikhlas_4")
            )
        ),
        WordPracticeItem(
            4, "সূরা আন-নাস (আয়াত ১)", 1,
            listOf(
                WordDetail("قُلْ", "ক্বুল", "বলুন", "nas_1"),
                WordDetail("أَعُوذُ", "আ'উজু", "আমি আশ্রয় চাই", "nas_2"),
                WordDetail("بِرَبِّ", "বি-রব্বি", "পালনকর্তার কাছে", "nas_3"),
                WordDetail("النَّاسِ", "আন-নাস", "মানুষের", "nas_4")
            )
        )
    )

    val QUIZ_QUESTIONS = listOf(
        QuizQuestion(
            1, "আরবি বর্ণমালায় মোট কতটি মূল হরফ রয়েছে?", "ا ب ت ...",
            listOf("২৮টি", "২৯টি", "৩০টি", "২৬টি"), 1, "আরবি বর্ণমালায় মোট ২৯টি হরফ রয়েছে।"
        ),
        QuizQuestion(
            2, "কোন হরফটি কণ্ঠনালীর মধ্যখান থেকে চিপে উচ্চারিত হয়?", "ع",
            listOf("হামযাহ (ء)", "আইন (ع)", "হা (هـ)", "গাইন (غ)"), 1, "আইন (ع) হরফটি কণ্ঠনালীর মধ্যখান থেকে সজোরে চিপে উচ্চারিত হয়।"
        ),
        QuizQuestion(
            3, "দুই যবর, দুই যের ও দুই পেশকে একসাথে কী বলা হয়?", "ً ٍ ٌ",
            listOf("হরকত", "তানভীন", "তাশদীদ", "সুকূন"), 1, "দুই যবর, দুই যের ও দুই পেশকে তানভীন বলা হয়, যার শেষে 'নূনে সাকিন' বা 'ন' ধ্বনি থাকে।"
        ),
        QuizQuestion(
            4, "নূন সাকিন বা তানভীনের পর 'ب' (বা) আসলে কোন তাজভীদ প্রয়োগ করতে হয়?", "مِنْ بَعْدِ",
            listOf("ইখফা", "ইদগাম", "ইকলাব (মীম দ্বারা পরিবর্তন)", "কলকলাহ"), 2, "ইকলাবের নিয়মানুযায়ী নূন সাকিনের পর 'বা' আসলে নূনকে 'মীম'-এ রূপান্তর করে গুন্নাহ সহ পড়তে হয়।"
        ),
        QuizQuestion(
            5, "কলকলাহের হরফ কয়টি এবং কী কী?", "ق ط ب ج د",
            listOf("৩টি", "৪টি", "৫টি (ক্বাফ, ত্বা, বা, জীম, দাল)", "৬টি"), 2, "কলকলাহের হরফ ৫টি: ক্বাফ, ত্বা, বা, জীম, দাল (قطب جد)।"
        )
    )
}

// =========================================================================
// MAIN COMPOSABLE: QURAN LEARNING SCREEN
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranLearningScreen(
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit = {},
    isBn: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val ttsPlayer = remember { IslamicMaleTtsPlayer.getInstance(context) }

    // Active Tab Index: 0=Letters, 1=Harakat, 2=Sukoon/Tashdeed, 3=Tajweed, 4=Word Practice, 5=Quiz
    var activeTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLetterCategory by remember { mutableStateOf("All") }

    // Quiz State
    var currentQuizIndex by remember { mutableIntStateOf(0) }
    var userScore by remember { mutableIntStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var isQuizAnswerSubmitted by remember { mutableStateOf(false) }

    // Overscroll Physics Bounce
    val bounceAnimatable = remember { Animatable(0f) }
    val bounceNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y != 0f && source == NestedScrollSource.UserInput) {
                    val newOffset = (bounceAnimatable.value + available.y * 0.35f).coerceIn(-140f, 140f)
                    coroutineScope.launch {
                        bounceAnimatable.snapTo(newOffset)
                    }
                }
                return Offset.Zero
            }

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (bounceAnimatable.value != 0f) {
                    val newOffset = if (bounceAnimatable.value > 0 && available.y < 0) {
                        (bounceAnimatable.value + available.y).coerceAtLeast(0f)
                    } else if (bounceAnimatable.value < 0 && available.y > 0) {
                        (bounceAnimatable.value + available.y).coerceAtMost(0f)
                    } else {
                        bounceAnimatable.value
                    }
                    coroutineScope.launch {
                        bounceAnimatable.snapTo(newOffset)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                coroutineScope.launch {
                    bounceAnimatable.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    // Intercept Back Button
    BackHandler(enabled = searchQuery.isNotEmpty() || activeTab != 0) {
        if (searchQuery.isNotEmpty()) {
            searchQuery = ""
        } else {
            activeTab = 0
        }
    }

    val tabTitles = if (isBn) {
        listOf("১. আরবি ২৯ হরফ", "২. হরকত ও তানভীন", "৩. যযম ও তাশদীদ", "৪. তাজভীদ রুলস", "৫. শব্দে শব্দে প্র্যাকটিস", "৬. কুইজ টেস্ট")
    } else {
        listOf("1. Alphabets", "2. Harakat", "3. Sukoon & Tashdeed", "4. Tajweed", "5. Word Practice", "6. Quiz")
    }

    val primaryGreen = Color(0xFF059669)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .nestedScroll(bounceNestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, bounceAnimatable.value.roundToInt()) }
        ) {
            // =========================================================================
            // SINGLE CLEAN TOP HEADER BAR (NO DUPLICATE HEADERS)
            // =========================================================================
            Surface(
                color = themeColors.cardBg,
                shadowElevation = 3.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = themeColors.displayText
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(primaryGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = primaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "কুরআন শিক্ষা (নূরানী কায়দা)" else "Learn Quran (Noorani Qaida)",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isBn) "শব্দে শব্দে অডিও, মাখরাজ ও তাজভীদ" else "Makhraj, Harakat & Audio Lessons",
                                fontSize = 11.sp,
                                color = primaryGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Search Toggle or Speaker Info
                        IconButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    if (isBn) "যেকোনো আরবি হরফ বা শব্দে ট্যাপ করে বিশুদ্ধ উচ্চারণ শুনুন" else "Tap any letter or word to hear audio",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Audio Info",
                                tint = primaryGreen
                            )
                        }
                    }

                    // TABS ROW
                    ScrollableTabRow(
                        selectedTabIndex = activeTab,
                        edgePadding = 12.dp,
                        containerColor = Color.Transparent,
                        contentColor = primaryGreen,
                        divider = {}
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = activeTab == index,
                                onClick = { activeTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (activeTab == index) primaryGreen else themeColors.displayText.copy(alpha = 0.7f)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // MAIN CONTENT BODY BASED ON TAB
            // =========================================================================
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { if (targetState > initialState) it else -it } togetherWith
                            fadeOut() + slideOutHorizontally { if (targetState > initialState) -it else it }
                },
                label = "quran_learning_tab_transition",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    0 -> LettersTab(themeColors, isBn, ttsPlayer, primaryGreen)
                    1 -> HarakatTab(themeColors, isBn, ttsPlayer, primaryGreen)
                    2 -> SukoonTashdeedTab(themeColors, isBn, ttsPlayer, primaryGreen)
                    3 -> TajweedTab(themeColors, isBn, ttsPlayer, primaryGreen)
                    4 -> WordPracticeTab(themeColors, isBn, ttsPlayer, primaryGreen)
                    5 -> QuizTab(
                        themeColors = themeColors,
                        isBn = isBn,
                        currentIndex = currentQuizIndex,
                        score = userScore,
                        selectedIndex = selectedAnswerIndex,
                        isSubmitted = isQuizAnswerSubmitted,
                        onSelectOption = { selectedAnswerIndex = it },
                        onSubmitAnswer = {
                            if (selectedAnswerIndex != null) {
                                isQuizAnswerSubmitted = true
                                val q = QuranLearningData.QUIZ_QUESTIONS[currentQuizIndex]
                                if (selectedAnswerIndex == q.correctIndex) {
                                    userScore += 10
                                }
                            }
                        },
                        onNextQuestion = {
                            if (currentQuizIndex < QuranLearningData.QUIZ_QUESTIONS.size - 1) {
                                currentQuizIndex += 1
                                selectedAnswerIndex = null
                                isQuizAnswerSubmitted = false
                            } else {
                                Toast.makeText(context, if (isBn) "অভিনন্দন! আপনি কুইজ সম্পন্ন করেছেন।" else "Congratulations! Quiz completed.", Toast.LENGTH_LONG).show()
                            }
                        },
                        onResetQuiz = {
                            currentQuizIndex = 0
                            userScore = 0
                            selectedAnswerIndex = null
                            isQuizAnswerSubmitted = false
                        }
                    )
                }
            }
        }
    }
}

// =========================================================================
// TAB 1: ARABIC 29 LETTERS & MAKHRAJ GRID
// =========================================================================
@Composable
fun LettersTab(
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    ttsPlayer: IslamicMaleTtsPlayer,
    primaryGreen: Color
) {
    var selectedCategory by remember { mutableStateOf("সব হরফ") }
    var selectedLetterDetail by remember { mutableStateOf<ArabicLetter?>(null) }

    val categories = listOf("সব হরফ", "হলকী (কণ্ঠ)", "শাফাভী (ঠোঁট)", "লাছভী (দাঁত)", "আছলী (জিহ্বা)")

    val filteredLetters = remember(selectedCategory) {
        if (selectedCategory == "সব হরফ") {
            QuranLearningData.ARABIC_LETTERS
        } else {
            val key = selectedCategory.takeWhile { it != ' ' }
            QuranLearningData.ARABIC_LETTERS.filter { it.categoryBn.contains(key, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP INSTRUCTION BANNER
        Surface(
            color = primaryGreen.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "যেকোনো আরবি বর্ণে স্পর্শ (Tap) করে তার মাখরাজ ও বিশুদ্ধ উচ্চারণ শুনুন" else "Tap any Arabic letter to hear audio and learn its Makhraj",
                    fontSize = 11.5.sp,
                    color = themeColors.displayText,
                    lineHeight = 16.sp
                )
            }
        }

        // CATEGORY CHIPS
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) primaryGreen else themeColors.cardBg,
                    border = BorderStroke(1.dp, if (isSelected) primaryGreen else themeColors.displayText.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else themeColors.displayText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // GRID OF 29 LETTERS
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredLetters, key = { it.id }) { letter ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = themeColors.cardBg,
                    border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.25f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            selectedLetterDetail = letter
                            ttsPlayer.speakOrStop("letter_${letter.id}", letter.arabic, letter.banglaName)
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter.arabic,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = letter.banglaName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "#${letter.id} • ${letter.categoryBn}",
                            fontSize = 10.sp,
                            color = themeColors.displayText.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    // MAKHRAJ DETAIL BOTTOM SHEET / DIALOG
    selectedLetterDetail?.let { letter ->
        AlertDialog(
            onDismissRequest = { selectedLetterDetail = null },
            confirmButton = {
                Button(
                    onClick = {
                        ttsPlayer.speakOrStop("letter_${letter.id}", letter.arabic, letter.banglaName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isBn) "পুনরায় শুনুন" else "Listen Audio", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLetterDetail = null }) {
                    Text(text = if (isBn) "বন্ধ করুন" else "Close", color = themeColors.displayText)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = letter.arabic,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "হরফ: ${letter.banglaName} (${letter.englishName})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "ক্যাটাগরি: ${letter.categoryBn}", fontSize = 11.sp, color = primaryGreen)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "মাখরাজ বা উচ্চারণের স্থান:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = primaryGreen.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = letter.makhrajBn,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = themeColors.displayText,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// =========================================================================
// TAB 2: HARAKAT & TANWEEN LESSONS
// =========================================================================
@Composable
fun HarakatTab(
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    ttsPlayer: IslamicMaleTtsPlayer,
    primaryGreen: Color
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = primaryGreen.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = primaryGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBn) "হরকত (যবর, যের, পেশ) ও তানভীন শেখার সহজ পাঠ। শব্দে ট্যাপ করে সঠিক উচ্চারণ ও ছন্দ শুনুন।" else "Learn Harakat and Tanween vowel signs with tap audio.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = themeColors.displayText
                    )
                }
            }
        }

        items(QuranLearningData.HARAKAT_LESSONS, key = { it.id }) { lesson ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.2f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = lesson.arabicSample,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = lesson.banglaTrans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lesson.ruleNameBn,
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.75f),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // EXAMPLES GRID
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        lesson.exampleWords.forEach { (ar, bn) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = primaryGreen.copy(alpha = 0.06f),
                                border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        ttsPlayer.speakOrStop("harakat_${ar}", ar, bn)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = ar,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = bn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = themeColors.displayText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 3: SUKOON & TASHDEED LESSONS
// =========================================================================
@Composable
fun SukoonTashdeedTab(
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    ttsPlayer: IslamicMaleTtsPlayer,
    primaryGreen: Color
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(QuranLearningData.SUKOON_TASHDEED_LESSONS, key = { it.id }) { lesson ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.25f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = lesson.arabicSample,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = lesson.banglaTrans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lesson.ruleNameBn,
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.75f),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(lesson.exampleWords) { (ar, bn) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = primaryGreen.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .width(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        ttsPlayer.speakOrStop("st_${ar}", ar, bn)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = ar,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = bn,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 4: TAJWEED RULES & COLOR-CODED LESSONS
// =========================================================================
@Composable
fun TajweedTab(
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    ttsPlayer: IslamicMaleTtsPlayer,
    primaryGreen: Color
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = primaryGreen.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = primaryGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBn) "তাজভীদ হলো কুরআন বিশুদ্ধভাবে তিলাওয়াতের সহজ নিয়মাবলী। উদাহরণের ওপর ট্যাপ করে অডিও শুনুন।" else "Tajweed rules for correct Quran recitation with color coding and audio examples.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = themeColors.displayText
                    )
                }
            }
        }

        items(QuranLearningData.TAJWEED_RULES, key = { it.id }) { rule ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, rule.colorHex.copy(alpha = 0.4f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(rule.colorHex)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rule.titleBn,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = rule.titleEn,
                            fontSize = 11.sp,
                            color = rule.colorHex,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rule.descriptionBn,
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.8f),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "উদাহরণসমূহ (ট্যাপ করুন):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(rule.examples) { (ar, bn) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = rule.colorHex.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, rule.colorHex.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        ttsPlayer.speakOrStop("tajweed_${ar}", ar, bn)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ar,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = rule.colorHex
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = bn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = themeColors.displayText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 5: WORD-BY-WORD PRACTICE & AUDIO LESSONS
// =========================================================================
@Composable
fun WordPracticeTab(
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    ttsPlayer: IslamicMaleTtsPlayer,
    primaryGreen: Color
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = primaryGreen.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = primaryGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBn) "শব্দে শব্দে তিলাওয়াত ও উচ্চারণ শিক্ষা। প্রতিটি আরবি শব্দের ওপর ট্যাপ করলে সেটির আলাদা উচ্চারণ ও বাংলা অর্থ শোনা যাবে।" else "Word-by-word Quran practice. Tap any individual word to hear exact audio and translation.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = themeColors.displayText
                    )
                }
            }
        }

        items(QuranLearningData.WORD_PRACTICE_LIST, key = { it.id }) { item ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = themeColors.cardBg,
                border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.25f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = item.surahNameBn,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // WORDS FLEX FLOW ROW
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(item.words) { word ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = primaryGreen.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        ttsPlayer.speakOrStop("word_${word.audioKey}", word.arabic, word.banglaTrans)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = word.arabic,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = word.banglaTrans,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.displayText
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = word.meaningBn,
                                        fontSize = 10.5.sp,
                                        color = themeColors.displayText.copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 6: QURAN LEARNING QUIZ TEST
// =========================================================================
@Composable
fun QuizTab(
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    currentIndex: Int,
    score: Int,
    selectedIndex: Int?,
    isSubmitted: Boolean,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onResetQuiz: () -> Unit
) {
    val questions = QuranLearningData.QUIZ_QUESTIONS
    val currentQ = questions.getOrNull(currentIndex) ?: questions[0]
    val primaryGreen = Color(0xFF059669)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // SCORE & PROGRESS BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "প্রশ্ন ${currentIndex + 1} / ${questions.size}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = primaryGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "স্কোর: $score পয়েন্ট",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = primaryGreen,
            trackColor = primaryGreen.copy(alpha = 0.15f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // QUESTION CARD
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = themeColors.cardBg,
            border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.3f)),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentQ.arabicDisplay,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentQ.questionBn,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // OPTIONS LIST
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            currentQ.optionsBn.forEachIndexed { optIdx, optionText ->
                val isSelected = selectedIndex == optIdx
                val isCorrect = optIdx == currentQ.correctIndex

                val cardBg = when {
                    isSubmitted && isCorrect -> Color(0xFF059669).copy(alpha = 0.2f)
                    isSubmitted && isSelected && !isCorrect -> Color(0xFFDC2626).copy(alpha = 0.2f)
                    isSelected -> primaryGreen.copy(alpha = 0.12f)
                    else -> themeColors.cardBg
                }

                val borderColor = when {
                    isSubmitted && isCorrect -> Color(0xFF059669)
                    isSubmitted && isSelected && !isCorrect -> Color(0xFFDC2626)
                    isSelected -> primaryGreen
                    else -> themeColors.displayText.copy(alpha = 0.15f)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !isSubmitted) {
                            onSelectOption(optIdx)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) primaryGreen else Color.Transparent)
                                .border(1.dp, if (isSelected) primaryGreen else themeColors.displayText.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = optionText,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = themeColors.displayText
                        )
                    }
                }
            }

            if (isSubmitted) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryGreen.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ব্যাখ্যা: ${currentQ.explanationBn}",
                        fontSize = 12.sp,
                        color = themeColors.displayText,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // BOTTOM ACTION BUTTON
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isSubmitted) {
                Button(
                    onClick = onSubmitAnswer,
                    enabled = selectedIndex != null,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "উত্তর নিশ্চিত করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                if (currentIndex < questions.size - 1) {
                    Button(
                        onClick = onNextQuestion,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "পরবর্তী প্রশ্ন →", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onResetQuiz,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "পুনরায় কুইজ শুরু করুন 🔄", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

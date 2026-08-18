package com.example.ui.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors

// Traditional Tajweed Colors used in standard Tajweed Quran Mushafs
object TajweedColors {
    val Ghunnah = Color(0xFFD946EF)  // Magenta / Pink (গুণ্নাহ)
    val Ikhfa = Color(0xFFEF4444)    // Crimson Red (ইখফা)
    val Idgham = Color(0xFF10B981)   // Emerald Green (ঈদগাম)
    val Qalqalah = Color(0xFF3B82F6) // Royal Blue (কলকলাহ)
    val Madd = Color(0xFFF59E0B)     // Gold / Amber (মাদ্দ / দীর্ঘ টান)
    val Iqlab = Color(0xFF06B6D4)    // Cyan / Teal (ইকলাব)
}

data class TajweedDetailRule(
    val titleBn: String,
    val titleAr: String,
    val color: Color,
    val ruleDescriptionBn: String,
    val lettersBn: String,
    val exampleAr: String,
    val exampleBn: String,
    val durationBn: String
)

val allTajweedDetailRules = listOf(
    TajweedDetailRule(
        titleBn = "গুণ্নাহ (Ghunnah)",
        titleAr = "غُنَّة",
        color = TajweedColors.Ghunnah,
        ruleDescriptionBn = "নূন (ن) বা মীম (م) হরফের ওপর তাশদীদ ( ّ ) থাকলে নাকের বাঁশিতে গুঞ্জন তৈরি করে ১ আলিফ (২ হরকত) সময় ধরে পড়া বাধ্যতামূলক।",
        lettersBn = "তাশদীদযুক্ত নূন (نّ) ও মীম (مّ)",
        exampleAr = "إِنَّ • ثُمَّ • أُمَّتُكُمْ",
        exampleBn = "ইন্না • ছুম্মা • উম্মাতুকুম",
        durationBn = "১ আলিফ (২ হরকত)"
    ),
    TajweedDetailRule(
        titleBn = "ইখফা (Ikhfa - গোপন করে পড়া)",
        titleAr = "إِخْفَاء",
        color = TajweedColors.Ikhfa,
        ruleDescriptionBn = "নূন সাকিন (نْ) বা তানবীন ( ً ٍ ٌ ) এর পর ইখফার ১৫টি অক্ষরের যেকোনো একটি আসলে নূনের আওয়াজকে নাকের বাঁশিতে লুকাইয়া অস্পষ্টভাবে পড়তে হয়।",
        lettersBn = "১৫টি হরফ: ত (ت), ছ (ث), জিম (ج), দাল (د), যাল (ذ), যা (ز), সীন (س), শীন (ش), সোয়াদ (ص), দোয়াদ (ض), তোয়া (ط), জোয়া (ظ), ফা (ف), ক্বাফ (ق), কাফ (ك)",
        exampleAr = "مَنْ كَانَ • مِنْ قَبْلِ • كِتَابٌ كَرِيمٌ",
        exampleBn = "মাঙ্ ক্যানা • মিঙ্ ক্বাবলি • কিতাবুঙ্ কারীম",
        durationBn = "১ আলিফ গুণ্নাহসহ"
    ),
    TajweedDetailRule(
        titleBn = "ঈদগাম (Idgham - মিলিয়ে পড়া)",
        titleAr = "إِدْغَام",
        color = TajweedColors.Idgham,
        ruleDescriptionBn = "নূন সাকিন বা তানবীনের পর ঈদগামের ৬টি অক্ষরের যেকোনো একটি আসলে প্রথম বর্ণকে দ্বিতীয় বর্ণের সাথে যুক্ত করে পড়া।",
        lettersBn = "৬টি হরফ (যালমুন - يرملون): য়া (ي), রা (ر), মীম (م), লাম (ل), ওয়াও (و), নূন (ن)\n• বা-গুণ্নাহ (গুণ্নাহসহ ৪টি): ي, ن, م, و\n• বেলা-গুণ্নাহ (গুণ্নাহ ছাড়া ২টি): ل, ر",
        exampleAr = "مَنْ يَقُولُ (বা-গুণ্নাহ) • مِنْ رَبِّهِمْ (বেলা-গুণ্নাহ)",
        exampleBn = "মাইঁ ইয়াকূলু • মির্ রাব্বিহিম",
        durationBn = "বা-গুণ্নাহ ১ আলিফ / বেলা-গুণ্নাহ স্বাভাবিক"
    ),
    TajweedDetailRule(
        titleBn = "কলকলাহ (Qalqalah - প্রতিধ্বনি বা ধাক্কা)",
        titleAr = "قَلْقَلَة",
        color = TajweedColors.Qalqalah,
        ruleDescriptionBn = "কলকলাহর ৫টি অক্ষরের ওপর জযম ( ْ ) থাকলে বা থামলে (ওয়াকফ) উচ্চারণের সময় কণ্ঠে ধাক্কা বা প্রতিধ্বনি সৃষ্টি করে পড়তে হয়।",
        lettersBn = "৫টি হরফ (কুতবু জাদ - قُطْبُ جَدٍّ): ক্বাফ (ق), তোয়া (ط), বা (ب), জিম (ج), দাল (د)",
        exampleAr = "قُلْ هُوَ اللَّهُ أَحَدٌ • الْفَلَقِ • وَتَبَّ",
        exampleBn = "আহাদ (ধাক্কা) • আল-ফালাক্ব • ওয়া ত্যাব্ব",
        durationBn = "প্রতিধ্বনি সৃষ্টি"
    ),
    TajweedDetailRule(
        titleBn = "মাদ্দ (Madd - দীর্ঘ টানা)",
        titleAr = "مَدّ",
        color = TajweedColors.Madd,
        ruleDescriptionBn = "মাদ্দের ৩টি মূল হরফ (আলিফ, ওয়াও, ইয়া) এর আগে যথাক্রমে জবর, পেশ, যের অথবা খাড়া জবর ( ٰ ), খাড়া যের ( ٖ ), উল্টা পেশ ( ٗ ), মাদ্দ চিহ্ন ( ۤ ) থাকলে স্বরধ্বনি টেনে পড়তে হয়।",
        lettersBn = "আলিফ (ا), ওয়াও (و), ইয়া (ي), খাড়া জবর/যের, মাদ্দ চিহ্ন ( ۤ )",
        exampleAr = "جَاءَ (৪ আলিফ) • الرَّحْمٰنِ (১ আলিফ) • السَّمَاءِ (৪ আলিফ)",
        exampleBn = "জাাাা-আ • আর-রাহমা-ন • আস-সামাাা-ই",
        durationBn = "১, ৩ বা ৪ আলিফ টান"
    ),
    TajweedDetailRule(
        titleBn = "ইকলাব (Iqlab - মীম দিয়ে পরিবর্তন)",
        titleAr = "إِقْلَاب",
        color = TajweedColors.Iqlab,
        ruleDescriptionBn = "নূন সাকিন বা তানবীনের পর 'বা' (ب) অক্ষর আসলে নূনকে মীম (م) দ্বারা পরিবর্তন করে গুণ্নাহর সাথে পড়তে হয়। ছোট মীম ( ۢ ) দিয়ে নির্দেশিত থাকে।",
        lettersBn = "১টি হরফ: বা (ب)",
        exampleAr = "مِنْ بَعْدِ • أَنْبِئْهُمْ",
        exampleBn = "মিম্ বা'দি • আম্বি-হুম",
        durationBn = "১ আলিফ গুণ্নাহ"
    ),
    TajweedDetailRule(
        titleBn = "ইযহার (Izhar - স্পষ্ট পড়া)",
        titleAr = "إِظْهَار",
        color = Color(0xFF6B7280),
        ruleDescriptionBn = "নূন সাকিন বা তানবীনের পর হলকী (কণ্ঠনালীর) ৬টি অক্ষরের যেকোনো একটি আসলে কোনো গুণ্নাহ না করে স্পষ্ট ও স্বাভাবিকভাবে পড়া।",
        lettersBn = "৬টি কণ্ঠনালীর হরফ: হামযাহ (ء), হা (هـ), 'আইন (ع), হা (ح), গাইন (غ), খা (خ)",
        exampleAr = "مَنْ آمَنَ • مِنْ حَكِيمٍ",
        exampleBn = "মান আমানা • মিন হাকীম",
        durationBn = "স্পষ্ট (গুণ্নাহ ছাড়া)"
    )
)

/**
 * Dynamically parses Arabic text with diacritics and builds an AnnotatedString
 * with Tajweed color styles according to classic Quranic recitation rules.
 */
@Composable
fun buildTajweedAnnotatedString(
    text: String,
    baseColor: Color
): AnnotatedString {
    return remember(text, baseColor) {
        buildAnnotatedString {
            val len = text.length
            var i = 0

            while (i < len) {
                val char = text[i]

                // Rule 1: Madd (Maddah symbol ۤ, dagger alif ٰ, Alif Maddah آ, etc.)
                val isMaddSymbol = char == 'ۤ' || char == 'ٰ' || char == 'آ' || char == 'ۦ' || char == 'ۧ'

                // Rule 2: Iqlab (Small Meem above ۢ or below ۣ)
                val isIqlabSymbol = char == 'ۢ' || char == 'ۣ'

                // Rule 3: Ghunnah (Nun ن or Mim م with Shaddah ّ)
                val isNunOrMim = char == 'ن' || char == 'م'
                val hasShaddahNext = (i + 1 < len && text[i + 1] == 'ّ') || (i + 2 < len && text[i + 2] == 'ّ')

                // Rule 4: Qalqalah (ق, ط, ب, ج, د - Qutb Jadd) with Sukun ْ
                val isQalqalahChar = char == 'ق' || char == 'ط' || char == 'ب' || char == 'ج' || char == 'د'
                val hasSukunNext = (i + 1 < len && text[i + 1] == 'ْ') || (i + 2 < len && text[i + 2] == 'ْ')

                when {
                    isMaddSymbol -> {
                        withStyle(SpanStyle(color = TajweedColors.Madd, fontWeight = FontWeight.Bold)) {
                            append(char)
                        }
                    }
                    isIqlabSymbol -> {
                        withStyle(SpanStyle(color = TajweedColors.Iqlab, fontWeight = FontWeight.Bold)) {
                            append(char)
                        }
                    }
                    isNunOrMim && hasShaddahNext -> {
                        withStyle(SpanStyle(color = TajweedColors.Ghunnah, fontWeight = FontWeight.Bold)) {
                            append(char)
                        }
                    }
                    isQalqalahChar && hasSukunNext -> {
                        withStyle(SpanStyle(color = TajweedColors.Qalqalah, fontWeight = FontWeight.Bold)) {
                            append(char)
                        }
                    }
                    // Tanween (ً, ٍ, ٌ) rules check next letters for Idgham or Ikhfa
                    char == 'ً' || char == 'ٍ' || char == 'ٌ' -> {
                        var nextIdx = i + 1
                        while (nextIdx < len && (text[nextIdx] == ' ' || text[nextIdx] == 'ۡ')) nextIdx++
                        if (nextIdx < len) {
                            val nextChar = text[nextIdx]
                            if ("يرملون".contains(nextChar)) { // Yarmaloon -> Idgham
                                withStyle(SpanStyle(color = TajweedColors.Idgham, fontWeight = FontWeight.Bold)) {
                                    append(char)
                                }
                            } else if ("تثجدذزسشصضطظفقك".contains(nextChar)) { // Ikhfa letters
                                withStyle(SpanStyle(color = TajweedColors.Ikhfa, fontWeight = FontWeight.Bold)) {
                                    append(char)
                                }
                            } else {
                                withStyle(SpanStyle(color = baseColor)) {
                                    append(char)
                                }
                            }
                        } else {
                            withStyle(SpanStyle(color = baseColor)) {
                                append(char)
                            }
                        }
                    }
                    else -> {
                        withStyle(SpanStyle(color = baseColor)) {
                            append(char)
                        }
                    }
                }
                i++
            }
        }
    }
}

/**
 * Tajweed Color Legend Bar component for explaining Tajweed colors to the user.
 */
@Composable
fun TajweedLegendBar(
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false,
    isBn: Boolean = true
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedRuleForDialog by remember { mutableStateOf<TajweedDetailRule?>(null) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (themeColors.isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (themeColors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) "তাজবীদ পড়ার কালার গাইড ও বিস্তারিত নিয়ম" else "Tajweed Color Guide & Detailed Rules",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "ট্যাপ করে রং ও হরফের নিয়ম সংক্ষেপ / বিস্তারিত দেখুন" else "Tap to view rules summary & details",
                        fontSize = 10.5.sp,
                        color = Color(0xFF10B981)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = themeColors.displayText.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = if (isBn) "কুরআন শরীফ সঠিক উচ্চারণে পড়ার জন্য রঙিন হরফের সহজ সংকেত (ট্যাপ করে বিস্তারিত পড়ুন):" else "Color-coded rules for correct Quranic pronunciation (Tap to read details):",
                        fontSize = 11.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TajweedRulePill(
                            name = if (isBn) "গুণ্নাহ" else "Ghunnah",
                            color = TajweedColors.Ghunnah,
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedRuleForDialog = allTajweedDetailRules[0]
                            showDetailDialog = true
                        }
                        TajweedRulePill(
                            name = if (isBn) "ইখফা" else "Ikhfa",
                            color = TajweedColors.Ikhfa,
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedRuleForDialog = allTajweedDetailRules[1]
                            showDetailDialog = true
                        }
                        TajweedRulePill(
                            name = if (isBn) "ঈদগাম" else "Idgham",
                            color = TajweedColors.Idgham,
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedRuleForDialog = allTajweedDetailRules[2]
                            showDetailDialog = true
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TajweedRulePill(
                            name = if (isBn) "কলকলাহ" else "Qalqalah",
                            color = TajweedColors.Qalqalah,
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedRuleForDialog = allTajweedDetailRules[3]
                            showDetailDialog = true
                        }
                        TajweedRulePill(
                            name = if (isBn) "মাদ্দ / টানা" else "Madd",
                            color = TajweedColors.Madd,
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedRuleForDialog = allTajweedDetailRules[4]
                            showDetailDialog = true
                        }
                        TajweedRulePill(
                            name = if (isBn) "ইকলাব" else "Iqlab",
                            color = TajweedColors.Iqlab,
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedRuleForDialog = allTajweedDetailRules[5]
                            showDetailDialog = true
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Button to open full detailed guide
                    Surface(
                        onClick = {
                            selectedRuleForDialog = null
                            showDetailDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "📖 সকল তাজবীদ নিয়ম ও মাসআলা বিস্তারিত দেখুন" else "📖 View All Tajweed Rules & Details",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDetailDialog) {
        TajweedRulesDetailDialog(
            themeColors = themeColors,
            initialSelectedRule = selectedRuleForDialog,
            onDismiss = { showDetailDialog = false },
            isBn = isBn
        )
    }
}

@Composable
private fun TajweedRulePill(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TajweedRulesDetailDialog(
    themeColors: CalculatorThemeColors,
    initialSelectedRule: TajweedDetailRule? = null,
    onDismiss: () -> Unit,
    isBn: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বন্ধ করুন" else "Close", fontWeight = FontWeight.Bold, color = themeColors.buttonEqualBg)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isBn) "তাজবীদ পড়ার পূর্ণাঙ্গ নিয়ম ও মাসআলা" else "Complete Tajweed Rules & Guide",
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Text(
                        text = if (isBn) "কুরআন মজিদ সহিহ-শুদ্ধ উচ্চারণে পড়ার গাইডলাইন" else "Guidelines for correct pronunciation of the Holy Quran",
                        fontSize = 11.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(allTajweedDetailRules.size) { index ->
                        val rule = allTajweedDetailRules[index]
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (themeColors.isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                            ),
                            border = BorderStroke(
                                width = if (initialSelectedRule?.titleBn == rule.titleBn) 2.dp else 1.dp,
                                color = rule.color.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(rule.color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val displayTitle = remember(rule.titleBn, isBn) {
                                            if (isBn) {
                                                rule.titleBn.substringBefore(" (")
                                            } else {
                                                when {
                                                    rule.titleBn.contains("Ghunnah") -> "Ghunnah"
                                                    rule.titleBn.contains("Ikhfa") -> "Ikhfa"
                                                    rule.titleBn.contains("Idgham") -> "Idgham"
                                                    rule.titleBn.contains("Qalqalah") -> "Qalqalah"
                                                    rule.titleBn.contains("Madd") -> "Madd"
                                                    rule.titleBn.contains("Iqlab") -> "Iqlab"
                                                    else -> rule.titleBn
                                                }
                                            }
                                        }
                                        Text(
                                            text = displayTitle,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = rule.color
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = rule.color.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = rule.titleAr,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = rule.color,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (isBn) "📌 নিয়ম: ${rule.ruleDescriptionBn}" else "📌 Rule: ${rule.ruleDescriptionBn}",
                                    fontSize = 12.5.sp,
                                    color = themeColors.displayText,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (isBn) "🔤 সংশ্লিষ্ট হরফ/চিহ্ন: ${rule.lettersBn}" else "🔤 Related Letters/Symbols: ${rule.lettersBn}",
                                    fontSize = 11.5.sp,
                                    color = themeColors.displayText.copy(alpha = 0.85f),
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = rule.color.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = if (isBn) "কুরআনের উদাহরণ (আরবি):" else "Quran Example (Arabic):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = rule.color
                                        )
                                        Text(
                                            text = rule.exampleAr,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = rule.color,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        )
                                        Text(
                                            text = if (isBn) "বাংলা উচ্চারণ: ${rule.exampleBn} (${rule.durationBn})" else "Pronunciation: ${rule.exampleBn} (${rule.durationBn})",
                                            fontSize = 11.5.sp,
                                            color = themeColors.displayText.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = themeColors.cardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

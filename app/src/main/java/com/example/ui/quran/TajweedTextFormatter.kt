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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (themeColors.isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (themeColors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
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
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "তাজবীদ পড়ার কালার গাইড (Tajweed Rules)",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    modifier = Modifier.weight(1f)
                )
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
                        text = "কুরআন শরীফ সঠিক উচ্চারণে পড়ার জন্য পড়ার নিয়ম অনুযায়ী রঙিন হরফ নির্দেশিকা:",
                        fontSize = 11.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.75f),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TajweedRulePill(name = "গুণ্নাহ (Ghunnah)", color = TajweedColors.Ghunnah)
                        TajweedRulePill(name = "ইখফা (Ikhfa)", color = TajweedColors.Ikhfa)
                        TajweedRulePill(name = "ঈদগাম (Idgham)", color = TajweedColors.Idgham)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TajweedRulePill(name = "কলকলাহ (Qalqalah)", color = TajweedColors.Qalqalah)
                        TajweedRulePill(name = "মাদ্দ / টানা (Madd)", color = TajweedColors.Madd)
                        TajweedRulePill(name = "ইকলাব (Iqlab)", color = TajweedColors.Iqlab)
                    }
                }
            }
        }
    }
}

@Composable
private fun TajweedRulePill(
    name: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
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
            color = color
        )
    }
}

package com.example.ui.screens.tools.kids

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CalculatorThemeColors

enum class SpellingTabMode(val titleBn: String, val icon: String) {
    WORDS("শব্দের বানান", "📖"),
    PHONICS("ইংরেজি বানানের নিয়ম ও ফনিক্স", "🗣️")
}

enum class PhonicsSubCategory(val titleBn: String, val icon: String) {
    ALPHABET_SOUNDS("A-Z ফনিক্স চার্ট", "🔤"),
    WORD_FAMILIES("শব্দ গঠন ও ব্লেন্ডিং (-at, -in, -ee...)", "🧩"),
    VOWEL_RULES("হ্রস্ব ও দীর্ঘ Vowel (Magic E)", "✨"),
    DIGRAPHS("ডাইগ্রাফ (SH, CH, TH...)", "👥"),
    BLENDS("যুক্তধ্বনি (Blends)", "🔗"),
    SILENT_LETTERS("অনুচ্চারিত বর্ণ (Silent Rules)", "🤫")
}

@Composable
fun KidsSpellingTab(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    mainMode: SpellingTabMode = SpellingTabMode.WORDS,
    onMainModeChange: (SpellingTabMode) -> Unit = {},
    selectedCategory: SpellingCategory = SpellingCategory.BANGLA_TWO_LETTER,
    onCategoryChange: (SpellingCategory) -> Unit = {},
    selectedPhonicsCategory: PhonicsSubCategory = PhonicsSubCategory.ALPHABET_SOUNDS,
    onPhonicsCategoryChange: (PhonicsSubCategory) -> Unit = {},
    onRewardStars: (Int) -> Unit
) {
    var puzzleTargetWord by remember { mutableStateOf<SpellingWordItem?>(null) }
    var highlightedStepIndex by remember { mutableStateOf(-1) }
    var currentlySpellingWord by remember { mutableStateOf<String?>(null) }

    val filteredWords = remember(selectedCategory) {
        KidsDataProvider.spellingWords.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Primary Mode Switcher: Words vs Phonics Rules
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpellingTabMode.values().forEach { mode ->
                val isSelected = mainMode == mode
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) themeColors.accent else themeColors.surface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.15f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clickable {
                            audioPlayer.playClickSound()
                            onMainModeChange(mode)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = mode.icon, fontSize = 17.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mode.titleBn,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.5.sp
                            ),
                            color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (mainMode == SpellingTabMode.WORDS) {
            // ========================================================
            // MODE 1: WORD SPELLING STUDIO (বানান করে পড়ার পাঠশালা)
            // ========================================================

            // Category Selector Chips (Horizontal Scrollable)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SpellingCategory.values()) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) themeColors.accent else themeColors.surface,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.15f)) else null,
                        modifier = Modifier
                            .height(40.dp)
                            .clickable {
                                audioPlayer.playClickSound()
                                onCategoryChange(cat)
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = cat.titleBn,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) themeColors.onAccent else themeColors.onSurface
                            )
                        }
                    }
                }
            }

            // Guidance & Word Count Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "বর্ণে স্পর্শ করে উচ্চারণ করো বা বানান শোনো 👇",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.onSurface.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (themeColors.isDark) Color(0xFF2E7D32).copy(alpha = 0.35f) else Color(0xFF4CAF50).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${filteredWords.size}টি শব্দ 📚",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (themeColors.isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // List of Word Cards with Interactive Letter Blocks
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
            ) {
                items(filteredWords) { wordItem ->
                    val isCurrentlySpellingThis = currentlySpellingWord == wordItem.word

                    SpellingCard(
                        item = wordItem,
                        isSpellingActive = isCurrentlySpellingThis,
                        highlightedIndex = if (isCurrentlySpellingThis) highlightedStepIndex else -1,
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onStartSpelling = {
                            audioPlayer.playClickSound()
                            currentlySpellingWord = wordItem.word
                            highlightedStepIndex = 0
                            val isBn = wordItem.category != SpellingCategory.ENGLISH_CVC &&
                                    wordItem.category != SpellingCategory.ENGLISH_WORDS

                            audioPlayer.spellWordStepByStep(
                                letterParts = wordItem.letterBlocks,
                                fullWord = wordItem.word,
                                isBn = isBn,
                                onStepHighlight = { step ->
                                    highlightedStepIndex = step
                                },
                                onComplete = {
                                    currentlySpellingWord = null
                                    highlightedStepIndex = -1
                                }
                            )
                        },
                        onOpenPuzzle = {
                            audioPlayer.playClickSound()
                            puzzleTargetWord = wordItem
                        }
                    )
                }
            }
        } else {
            // ========================================================
            // MODE 2: ENGLISH PHONICS & SPELLING RULES STUDIO
            // ========================================================

            // Phonics Sub-Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PhonicsSubCategory.values()) { subCat ->
                    val isSelected = selectedPhonicsCategory == subCat
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) themeColors.accent else themeColors.surface,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.15f)) else null,
                        modifier = Modifier
                            .height(40.dp)
                            .clickable {
                                audioPlayer.playClickSound()
                                onPhonicsCategoryChange(subCat)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = subCat.icon, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = subCat.titleBn,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) themeColors.onAccent else themeColors.onSurface
                            )
                        }
                    }
                }
            }

            when (selectedPhonicsCategory) {
                PhonicsSubCategory.ALPHABET_SOUNDS -> {
                    PhonicsAlphabetSection(
                        items = KidsDataProvider.phonicsLetterSounds,
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = onRewardStars
                    )
                }
                PhonicsSubCategory.WORD_FAMILIES -> {
                    PhonicsRulesListSection(
                        rules = KidsDataProvider.wordFamilyRules,
                        introTitle = "শব্দ তৈরির সহজ নিয়ম (Word Families & Blending)",
                        introDescription = "ভাওয়েল ও ব্যঞ্জনবর্ণ জোড়া লাগিয়ে কীভাবে হাজারো ইংরেজি শব্দ সহজে পড়া যায় তার দারুণ লেসন! নিচে ট্যাপ করে করে পড়ো ও শোনো।",
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = onRewardStars
                    )
                }
                PhonicsSubCategory.VOWEL_RULES -> {
                    PhonicsRulesListSection(
                        rules = KidsDataProvider.vowelRules,
                        introTitle = "ম্যাজিক 'E' ও ভাওয়েল নিয়ম (Magic 'E' Rules)",
                        introDescription = "শব্দের শেষে অনুচ্চারিত E বসলে আগের Vowel তার আসল পূর্ণ নাম (Alphabet Name) ধারণ করে! যেমন Cap (ক্যাপ) ➡️ Cape (কেইপ)।",
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = onRewardStars
                    )
                }
                PhonicsSubCategory.DIGRAPHS -> {
                    PhonicsRulesListSection(
                        rules = KidsDataProvider.digraphRules,
                        introTitle = "ডাইগ্রাফ (Digraphs) - দুই বর্ণ মিলে এক ধ্বনি",
                        introDescription = "দুটি ইংরেজি ব্যঞ্জনবর্ণ পাশাপাশি বসে যখন একেবারেই নতুন একটি আলাদা ধ্বনি সৃষ্টি করে, তাকে Digraph বলে (যেমন SH = /ʃ/ তালব্য 'শ', CH = /tʃ/ 'চ')।",
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = onRewardStars
                    )
                }
                PhonicsSubCategory.BLENDS -> {
                    PhonicsRulesListSection(
                        rules = KidsDataProvider.consonantBlendRules,
                        introTitle = "যুক্তধ্বনি (Consonant Blends)",
                        introDescription = "দুটি ব্যঞ্জনবর্ণ একসাথে উচ্চারিত হলেও প্রতিটি বর্ণের নিজস্ব ধ্বনি স্পষ্টভাবে শোনা যায় (যেমন BL = ব্ল, FR = ফ্র, ST = স্ট)।",
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = onRewardStars
                    )
                }
                PhonicsSubCategory.SILENT_LETTERS -> {
                    PhonicsRulesListSection(
                        rules = KidsDataProvider.silentLetterRules,
                        introTitle = "অনুচ্চারিত বর্ণ (Silent Letter Rules)",
                        introDescription = "ইংরেজি বানানে কিছু বর্ণ লেখা হলেও উচ্চারণ করতে হয় না! নিচের নিয়মগুলো শিখলে বানানের কোনো বিভ্রান্তি থাকবে না।",
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = onRewardStars
                    )
                }
            }
        }
    }

    // Interactive Spelling Puzzle Modal Dialog
    puzzleTargetWord?.let { target ->
        SpellingPuzzleDialog(
            target = target,
            themeColors = themeColors,
            audioPlayer = audioPlayer,
            onDismiss = { puzzleTargetWord = null },
            onSolved = {
                onRewardStars(5)
            }
        )
    }
}

/**
 * Word Spelling Card with Letter Blocks, Syllables, Pronunciation, and Puzzle Trigger
 */
@Composable
fun SpellingCard(
    item: SpellingWordItem,
    isSpellingActive: Boolean,
    highlightedIndex: Int,
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onStartSpelling: () -> Unit,
    onOpenPuzzle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSpellingActive) 2.dp else 1.dp,
                color = if (isSpellingActive) themeColors.accent else item.accentColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Word Header + Emoji + Meaning
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(item.accentColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.emoji, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = item.word,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = item.accentColor
                        )
                        Text(
                            text = item.meaning,
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Quick Puzzle Action Button
                FilledTonalIconButton(
                    onClick = onOpenPuzzle,
                    shape = RoundedCornerShape(14.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (themeColors.isDark) Color(0xFF2E7D32).copy(alpha = 0.35f) else Color(0xFF4CAF50).copy(alpha = 0.12f),
                        contentColor = if (themeColors.isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                    )
                ) {
                    Icon(Icons.Default.Extension, contentDescription = "Puzzle")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Building Blocks Row (ব + ই = বই)
            Text(
                text = "বানান সূত্র (Letter Blocks):",
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.letterBlocks.forEachIndexed { index, block ->
                    val isHighlighted = isSpellingActive && highlightedIndex == index
                    val isEnglish = item.category == SpellingCategory.ENGLISH_CVC ||
                            item.category == SpellingCategory.ENGLISH_WORDS

                    val blockBg = if (isHighlighted) {
                        themeColors.accent
                    } else {
                        item.accentColor.copy(alpha = 0.12f)
                    }

                    val blockTextColor = if (isHighlighted) {
                        themeColors.onAccent
                    } else {
                        item.accentColor
                    }

                    // Tappable block
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = blockBg,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isHighlighted) 2.dp else 1.dp,
                            color = if (isHighlighted) themeColors.accent else item.accentColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .height(44.dp)
                            .defaultMinSize(minWidth = 44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                audioPlayer.playClickSound()
                                audioPlayer.speak(block, isBn = !isEnglish)
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = block,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = blockTextColor
                            )
                        }
                    }

                    // Plus sign between blocks
                    if (index < item.letterBlocks.size - 1) {
                        Text(
                            text = "+",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                Text(
                    text = "=",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.onSurface.copy(alpha = 0.5f)
                )

                // Result Block
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = item.accentColor.copy(alpha = 0.20f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, item.accentColor)
                ) {
                    Text(
                        text = item.word,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = item.accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Spell Aloud & Puzzle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Spell Aloud Button
                Button(
                    onClick = onStartSpelling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpellingActive) Color(0xFFE65100) else themeColors.accent,
                        contentColor = themeColors.onAccent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = if (isSpellingActive) Icons.Default.GraphicEq else Icons.Default.VolumeUp,
                        contentDescription = "Spell Aloud",
                        tint = themeColors.onAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSpellingActive) "বানান হচ্ছে..." else "বানান শোনো",
                        style = MaterialTheme.typography.labelLarge,
                        color = themeColors.onAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Interactive Puzzle button
                val puzzleBorderColor = if (themeColors.isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                OutlinedButton(
                    onClick = onOpenPuzzle,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, puzzleBorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = puzzleBorderColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Extension,
                        contentDescription = "Assemble Puzzle",
                        tint = puzzleBorderColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "নিজে মেলাও",
                        color = puzzleBorderColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Phonics A-Z Alphabet Chart Section:
 * Displays all 26 letters with IPA, Bengali phonics sounds, spelling rules, and example words.
 */
@Composable
fun PhonicsAlphabetSection(
    items: List<PhonicsLetterSound>,
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onRewardStars: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "যেকোনো অক্ষরে ট্যাপ করে ফনিক্স ধ্বনি ও বানানের নিয়ম শোনো 👇",
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.onSurface.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
        ) {
            items(items) { letterItem ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, letterItem.accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Letter Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(letterItem.accentColor.copy(alpha = 0.15f), CircleShape)
                                    .border(2.dp, letterItem.accentColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letterItem.letter,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = letterItem.accentColor
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ধ্বনি: ${letterItem.soundBn}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.onSurface,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = letterItem.accentColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = letterItem.ipa,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = letterItem.accentColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = letterItem.ruleExplanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themeColors.onSurface.copy(alpha = 0.7f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Speaker Button for this Letter
                            FilledIconButton(
                                onClick = {
                                    audioPlayer.playClickSound()
                                    audioPlayer.speakPhonicsLetter(letterItem)
                                    onRewardStars(1)
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(42.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = letterItem.accentColor,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak letter sound",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Spelling Rule Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = letterItem.accentColor.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(text = "💡", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = letterItem.ruleExplanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themeColors.onSurface.copy(alpha = 0.85f),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Example Words Horizontal Chips
                        Text(
                            text = "উদাহরণ শব্দসমূহ (স্পর্শ করে শোনো):",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(letterItem.exampleWords) { example ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = themeColors.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, letterItem.accentColor.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            audioPlayer.playClickSound()
                                            audioPlayer.speak("${example.word}। ${example.pronunciationBn}। ${example.meaningBn}", isBn = true)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = example.emoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = example.word,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = letterItem.accentColor
                                            )
                                            Text(
                                                text = "${example.pronunciationBn} (${example.meaningBn})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = themeColors.onSurface.copy(alpha = 0.7f),
                                                fontSize = 10.5.sp
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
    }
}

/**
 * Generic Phonics Rules List Section:
 * Displays cards for Magic E, Digraphs, Consonant Blends, or Silent Letter rules.
 */
@Composable
fun PhonicsRulesListSection(
    rules: List<PhonicsRuleItem>,
    introTitle: String,
    introDescription: String,
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onRewardStars: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
    ) {
        // Educational Intro Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = themeColors.accent.copy(alpha = 0.12f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = introTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = introDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onSurface.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        items(rules) { ruleItem ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, ruleItem.accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header Row: Rule Title & Speaker Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ruleItem.ruleTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ruleItem.accentColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ruleItem.accentColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = ruleItem.formula,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ruleItem.accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        FilledIconButton(
                            onClick = {
                                audioPlayer.playClickSound()
                                audioPlayer.speakPhonicsRule(ruleItem)
                                onRewardStars(2)
                            },
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = ruleItem.accentColor,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Listen Rule", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Explanation Box
                    Text(
                        text = ruleItem.explanationBn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.onSurface.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Example Words Grid / Chips
                    Text(
                        text = "উদাহরণ শব্দসমূহ (ট্যাপ করে শোনো):",
                        style = MaterialTheme.typography.labelSmall,
                        color = themeColors.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ruleItem.examples) { ex ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = themeColors.surfaceVariant.copy(alpha = 0.55f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ruleItem.accentColor.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        audioPlayer.playClickSound()
                                        audioPlayer.speak("${ex.word}। বাংলা উচ্চারণ ${ex.pronunciationBn}। অর্থ ${ex.meaningBn}", isBn = true)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = ex.emoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = ex.word,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = ruleItem.accentColor
                                        )
                                        Text(
                                            text = "${ex.pronunciationBn} (${ex.meaningBn})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = themeColors.onSurface.copy(alpha = 0.7f),
                                            fontSize = 11.sp
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
}

/**
 * Interactive Spelling Puzzle Dialog:
 * Target word shown with emoji and blank slot spaces.
 * Jumbled letter tiles are presented at the bottom.
 * Kids tap letters in sequence. Correct completion plays fanfare and rewards stars!
 */
@Composable
fun SpellingPuzzleDialog(
    target: SpellingWordItem,
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onDismiss: () -> Unit,
    onSolved: () -> Unit
) {
    val totalLetters = target.letterBlocks
    val isEnglish = target.category == SpellingCategory.ENGLISH_CVC ||
            target.category == SpellingCategory.ENGLISH_WORDS

    // Current placed letters
    val placedLetters = remember { mutableStateListOf<String>() }

    // Shuffled pool of letters (including target letters and 1 or 2 extra distractor letters)
    val poolLetters = remember {
        val list = totalLetters.toMutableList()
        // Add random distractor if short
        if (list.size < 4) {
            val distractor = if (isEnglish) listOf("X", "Z", "M", "T").random() else listOf("ক", "ম", "র", "ন").random()
            list.add(distractor)
        }
        list.shuffled().toMutableStateList()
    }

    var isCompleted by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = themeColors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🧩", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "বানান মেলাও খেলা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = themeColors.onSurface.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Illustration
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(target.accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = target.emoji, fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = target.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Slots Row (Blank boxes being filled)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    totalLetters.forEachIndexed { index, _ ->
                        val filledLetter = placedLetters.getOrNull(index)
                        val isFilled = filledLetter != null

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFilled) target.accentColor.copy(alpha = 0.15f) else themeColors.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                color = if (isFilled) target.accentColor else themeColors.onSurface.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = isFilled && !isCompleted) {
                                    // Remove last placed letter
                                    if (placedLetters.isNotEmpty()) {
                                        val removed = placedLetters.removeAt(placedLetters.lastIndex)
                                        poolLetters.add(removed)
                                        audioPlayer.playClickSound()
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = filledLetter ?: "?",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFilled) target.accentColor else themeColors.onSurface.copy(alpha = 0.35f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Congratulatory banner if completed
                AnimatedVisibility(visible = isCompleted) {
                    val celebrationGreen = if (themeColors.isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = celebrationGreen.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🎉", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "চমৎকার হয়েছে! +৫ স্টার ⭐",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = celebrationGreen
                            )
                        }
                    }
                }

                if (!isCompleted) {
                    Text(
                        text = "নিচের বর্ণগুলো ক্রমানুসারে স্পর্শ করো:",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Letter Pool Tiles
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        poolLetters.forEachIndexed { pIndex, letter ->
                            Button(
                                onClick = {
                                    audioPlayer.playClickSound()
                                    audioPlayer.speak(letter, isBn = !isEnglish)

                                    // Check if this matches the expected next letter
                                    val nextIndex = placedLetters.size
                                    if (nextIndex < totalLetters.size && letter == totalLetters[nextIndex]) {
                                        placedLetters.add(letter)
                                        poolLetters.removeAt(pIndex)
                                        audioPlayer.playSuccessChime()

                                        // Check if solved
                                        if (placedLetters.size == totalLetters.size) {
                                            isCompleted = true
                                            audioPlayer.playCelebrationSound()
                                            audioPlayer.speak(target.word, isBn = !isEnglish)
                                            onSolved()
                                        }
                                    } else {
                                        // Wrong letter tapped
                                        audioPlayer.speak("আবার চেষ্টা করো", isBn = true)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themeColors.accent,
                                    contentColor = themeColors.onAccent
                                ),
                                modifier = Modifier.size(50.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = letter,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.onAccent
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Button
                if (isCompleted) {
                    val finishBtnColor = if (themeColors.isDark) Color(0xFF388E3C) else Color(0xFF2E7D32)
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = finishBtnColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "পরবর্তী শব্দে যাও ➡️", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    TextButton(
                        onClick = {
                            // Reset
                            placedLetters.clear()
                            poolLetters.clear()
                            val list = totalLetters.toMutableList()
                            if (list.size < 4) {
                                val distractor = if (isEnglish) listOf("X", "Z", "M", "T").random() else listOf("ক", "ম", "র", "ন").random()
                                list.add(distractor)
                            }
                            poolLetters.addAll(list.shuffled())
                            audioPlayer.playClickSound()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = themeColors.accent)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = themeColors.accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "পুনরায় চেষ্টা করো", fontWeight = FontWeight.SemiBold, color = themeColors.accent)
                    }
                }
            }
        }
    }
}

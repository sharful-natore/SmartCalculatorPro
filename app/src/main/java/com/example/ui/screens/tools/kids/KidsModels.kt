package com.example.ui.screens.tools.kids

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CalculatorThemeColors

val CalculatorThemeColors.surface: Color get() = this.cardBg
val CalculatorThemeColors.onSurface: Color get() = this.displayText
val CalculatorThemeColors.accent: Color get() = this.buttonEqualBg
val CalculatorThemeColors.onAccent: Color get() = this.buttonEqualText
val CalculatorThemeColors.surfaceVariant: Color get() = this.chipBg

enum class KidsSectionTab(val titleBn: String, val titleEn: String, val icon: ImageVector) {
    ALPHABET("বর্ণমালা", "Alphabets", Icons.Default.Spellcheck),
    SPELLING("বানান ও শব্দ", "Spelling", Icons.AutoMirrored.Filled.MenuBook),
    MATH("গণিত ও নামতা", "Math & Tables", Icons.Default.Calculate),
    RHYMES("ছড়া ও কবিতা", "Rhymes", Icons.Default.MusicNote),
    NATURE("প্রাণী ও প্রকৃতি", "Animals & Nature", Icons.Default.Pets),
    SLATE("ম্যাজিক স্লেট", "Magic Slate", Icons.Default.Draw),
    QUIZ("মজার কুইজ", "Brain Games", Icons.Default.Extension)
}

// -------------------------------------------------------------
// 1. ALPHABET MODELS
// -------------------------------------------------------------
enum class AlphabetCategory(val titleBn: String, val titleEn: String) {
    BANGLA_VOWEL("স্বরবর্ণ (১১টি)", "Vowels"),
    BANGLA_CONSONANT("ব্যঞ্জনবর্ণ (৩৯টি)", "Consonants"),
    BANGLA_KAR("কারচিহ্ন (১০টি)", "Kar Marks"),
    ENGLISH("ইংরেজি (A - Z)", "English A-Z")
}

data class LetterItem(
    val letter: String,
    val pronunciation: String,
    val wordBn: String,
    val wordEn: String,
    val emoji: String,
    val exampleSentence: String,
    val accentColor: Color
)

// -------------------------------------------------------------
// 2. SPELLING & PHONICS MODELS (বানান করে পড়া শেখা)
// -------------------------------------------------------------
enum class SpellingCategory(val titleBn: String, val titleEn: String) {
    BANGLA_TWO_LETTER("২ বর্ণের শব্দ", "2-Letter Words"),
    BANGLA_KAR_WORDS("কারচিহ্ন যুক্ত শব্দ", "Vowel Sign Words"),
    BANGLA_THREE_LETTER("৩ বর্ণের শব্দ", "3-Letter Words"),
    BANGLA_ADVANCED("৪-৫ বর্ণের শব্দ", "4-5 Letter Words"),
    ENGLISH_CVC("English CVC (৩ বর্ণ)", "CVC Phonics"),
    ENGLISH_WORDS("English শব্দ (৪-৫ বর্ণ)", "4-5 Letter Words")
}

data class SpellingWordItem(
    val word: String,
    val pronunciation: String,
    val meaning: String,
    val letterBlocks: List<String>, // e.g. ["ব", "ই"] or ["ব", "া", "ব", "া"] or ["C", "A", "T"]
    val spellingExplanation: String, // e.g. "ব + ই = বই" or "C - A - T = Cat"
    val emoji: String,
    val category: SpellingCategory,
    val accentColor: Color
)

// -------------------------------------------------------------
// PHONICS & SPELLING RULES MODELS (ইংরেজি বানানের নিয়ম ও ফনিমিক চার্ট)
// -------------------------------------------------------------
enum class PhonicsRuleCategory(val titleBn: String, val titleEn: String, val icon: String) {
    ALPHABET_SOUNDS("A-Z ফনিক্স চার্ট", "Phonics Chart", "🔤"),
    VOWEL_RULES("হ্রস্ব ও দীর্ঘ Vowel", "Short & Long Vowels", "🅰️"),
    DIGRAPHS("ডাইগ্রাফ (SH, CH...)", "Digraphs", "🔗"),
    BLENDS("যুক্তধ্বনি (Blends)", "Consonant Blends", "🤝"),
    SILENT_LETTERS("অনুচ্চারিত বর্ণ (Silent)", "Silent Letters", "🤫")
}

data class PhonicsLetterSound(
    val letter: String, // e.g. "A a"
    val soundBn: String, // e.g. "অ্যা / আ / এই"
    val ipa: String, // e.g. "/æ/ or /eɪ/"
    val ruleExplanation: String, // e.g. "সাধারণত অ্যা (Apple, Cat), শব্দের শেষে e থাকলে এই (Cake, Name)"
    val exampleWords: List<PhonicsWordExample>,
    val accentColor: Color
)

data class PhonicsWordExample(
    val word: String,
    val pronunciationBn: String,
    val meaningBn: String,
    val emoji: String
)

data class PhonicsRuleItem(
    val ruleTitle: String, // e.g. "Magic 'E' Rule" or "SH = 'শ' ধ্বনি" or "Silent K"
    val formula: String, // e.g. "Cap + e = Cape" or "S + H = /ʃ/ (শ)" or "K + N -> K অনুচ্চারিত"
    val explanationBn: String,
    val examples: List<PhonicsWordExample>,
    val accentColor: Color
)

// -------------------------------------------------------------
// 3. MATH & COUNTING MODELS
// -------------------------------------------------------------
data class NumberItem(
    val numberBn: String,
    val numberEn: String,
    val wordBn: String,
    val wordEn: String,
    val countEmoji: String,
    val count: Int,
    val accentColor: Color
)

data class MultiplicationTable(
    val number: Int,
    val numberBn: String,
    val items: List<MultiplicationItem>
)

data class MultiplicationItem(
    val multiplicand: Int,
    val multiplier: Int,
    val product: Int,
    val textBn: String,
    val speechBn: String,
    val textEn: String,
    val speechEn: String
)

// -------------------------------------------------------------
// 4. RHYMES & STORIES MODELS
// -------------------------------------------------------------
data class KidsRhyme(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val authorBn: String,
    val authorEn: String,
    val emoji: String,
    val linesBn: List<String>,
    val linesEn: List<String>,
    val themeColor: Color,
    val funFact: String
)

// -------------------------------------------------------------
// 5. NATURE & SURROUNDINGS MODELS
// -------------------------------------------------------------
enum class NatureCategory(val titleBn: String, val titleEn: String, val emoji: String) {
    ANIMALS("পশুপাখি", "Animals", "🦁"),
    FRUITS("ফলমূল", "Fruits", "🍎"),
    VEGETABLES("শাকসবজি", "Vegetables", "🥕"),
    FLOWERS("ফুল", "Flowers", "🌸"),
    VEHICLES("যানবাহন", "Vehicles", "🚗"),
    COLORS_SHAPES("রং ও আকার", "Colors & Shapes", "🔺")
}

data class NatureItem(
    val nameBn: String,
    val nameEn: String,
    val pronunciation: String,
    val soundText: String, // e.g. "মিউ মিউ / Meow!"
    val emoji: String,
    val category: NatureCategory,
    val funFact: String,
    val accentColor: Color
)

// -------------------------------------------------------------
// 6. QUIZ & BRAIN GAMES MODELS
// -------------------------------------------------------------
enum class QuizGameType {
    PICTURE_TO_WORD,
    MISSING_LETTER,
    ANIMAL_SOUND,
    COUNTING_PUZZLE
}

data class QuizQuestion(
    val type: QuizGameType,
    val questionBn: String,
    val questionEn: String,
    val visualPrompt: String, // Emoji or partial word
    val options: List<String>,
    val correctIndex: Int,
    val rewardPoints: Int = 10,
    val hintBn: String
)

data class MemoryCard(
    val id: Int,
    val emoji: String,
    val labelBn: String,
    val labelEn: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

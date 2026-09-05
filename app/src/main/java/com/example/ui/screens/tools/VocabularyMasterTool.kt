package com.example.ui.screens.tools

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private val CalculatorThemeColors.accent: Color get() = this.buttonEqualBg
private val CalculatorThemeColors.onAccent: Color get() = this.buttonEqualText
private val CalculatorThemeColors.onSurface: Color get() = this.displayText
private val CalculatorThemeColors.surface: Color get() = this.cardBg
private val CalculatorThemeColors.surfaceVariant: Color get() = this.chipBg

// Data Models
data class VocabWord(
    val id: String,
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val meaningBn: String,
    val exampleEn: String,
    val exampleBn: String,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val category: String, // "Spoken", "IELTS", "BCS", "Academic", "Idioms"
    val packId: String = "starter"
)

data class VocabPack(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val description: String,
    val wordCount: Int,
    val sizeKb: Int,
    val level: String,
    val iconName: String
)

enum class VocabTab(val titleBn: String, val titleEn: String) {
    EXPLORE("শব্দভান্ডার", "Word Explorer"),
    FLASHCARD("ফ্ল্যাশ কার্ড", "Flashcards"),
    QUIZ("কুইজ টেস্ট", "MCQ Quiz"),
    STORE("অন-ডিমান্ড স্টোর", "Word Store"),
    FAVORITES("সংরক্ষিত", "Favorites")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyMasterTool(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBn = viewModel.selectedLanguage == com.example.util.AppLanguage.BENGALI

    // TextToSpeech Engine
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.US
                isTtsReady = true
            }
        }
        tts = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }

    fun speakWord(text: String) {
        if (isTtsReady && tts != null) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vocab_tts_${System.currentTimeMillis()}")
        } else {
            Toast.makeText(context, if (isBn) "ভয়েস ইঞ্জিন প্রস্তুত হচ্ছে..." else "TTS engine initializing...", Toast.LENGTH_SHORT).show()
        }
    }

    // Installed Packs in SharedPreferences
    val prefs = remember { context.getSharedPreferences("vocab_prefs", Context.MODE_PRIVATE) }
    val installedPacks = remember {
        mutableStateListOf<String>().apply {
            val saved = prefs.getStringSet("installed_packs", setOf("starter")) ?: setOf("starter")
            addAll(saved)
        }
    }

    val bookmarkedIds = remember {
        mutableStateListOf<String>().apply {
            val saved = prefs.getStringSet("bookmarked_words", emptySet()) ?: emptySet()
            addAll(saved)
        }
    }

    fun toggleBookmark(id: String) {
        if (bookmarkedIds.contains(id)) {
            bookmarkedIds.remove(id)
        } else {
            bookmarkedIds.add(id)
        }
        prefs.edit().putStringSet("bookmarked_words", bookmarkedIds.toSet()).apply()
    }

    // Active Word List generated from installed packs
    val allWords = remember(installedPacks.toList()) {
        VocabularyDataProvider.getWordsForPacks(installedPacks.toSet())
    }

    var selectedTab by remember { mutableStateOf(VocabTab.EXPLORE) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedPosFilter by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBn) "ভোকাবুলারি মাস্টার" else "Vocabulary Master",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.onSurface
                        )
                        Text(
                            text = if (isBn) "${allWords.size} টি শব্দ প্রস্তুত • অফলাইন ডিকশনারি" else "${allWords.size} Words Active • Offline Ready",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = themeColors.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val randomWord = allWords.randomOrNull()
                        if (randomWord != null) {
                            speakWord(randomWord.word)
                            Toast.makeText(context, "${randomWord.word} : ${randomWord.meaningBn}", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Random Word",
                            tint = themeColors.accent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.surface)
            )
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab Navigation Chips (Pinned at Top)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(VocabTab.values()) { tab ->
                    val isSelected = selectedTab == tab
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedTab = tab }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (tab) {
                                VocabTab.EXPLORE -> Icons.Default.MenuBook
                                VocabTab.FLASHCARD -> Icons.Default.Style
                                VocabTab.QUIZ -> Icons.Default.Quiz
                                VocabTab.STORE -> Icons.Default.CloudDownload
                                VocabTab.FAVORITES -> Icons.Default.Favorite
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.titleBn,
                                tint = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) tab.titleBn else tab.titleEn,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) themeColors.onAccent else themeColors.onSurface
                            )
                        }
                    }
                }
            }

            Divider(color = themeColors.onSurface.copy(alpha = 0.08f), thickness = 1.dp)

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                when (selectedTab) {
                    VocabTab.EXPLORE -> VocabExploreTab(
                        words = allWords,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        categoryFilter = selectedCategoryFilter,
                        onCategoryChange = { selectedCategoryFilter = it },
                        posFilter = selectedPosFilter,
                        onPosChange = { selectedPosFilter = it },
                        bookmarkedIds = bookmarkedIds,
                        onBookmarkToggle = { toggleBookmark(it) },
                        onSpeak = { speakWord(it) },
                        themeColors = themeColors,
                        isBn = isBn
                    )
                    VocabTab.FLASHCARD -> VocabFlashcardTab(
                        words = allWords,
                        onSpeak = { speakWord(it) },
                        bookmarkedIds = bookmarkedIds,
                        onBookmarkToggle = { toggleBookmark(it) },
                        themeColors = themeColors,
                        isBn = isBn
                    )
                    VocabTab.QUIZ -> VocabQuizTab(
                        words = allWords,
                        onSpeak = { speakWord(it) },
                        themeColors = themeColors,
                        isBn = isBn
                    )
                    VocabTab.STORE -> VocabStoreTab(
                        installedPacks = installedPacks,
                        onInstallPack = { packId ->
                            coroutineScope.launch {
                                if (!installedPacks.contains(packId)) {
                                    installedPacks.add(packId)
                                    prefs.edit().putStringSet("installed_packs", installedPacks.toSet()).apply()
                                    Toast.makeText(context, if (isBn) "প্যাক সফলভাবে সক্রিয় হয়েছে!" else "Pack activated successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onUninstallPack = { packId ->
                            if (packId != "starter") {
                                installedPacks.remove(packId)
                                prefs.edit().putStringSet("installed_packs", installedPacks.toSet()).apply()
                                Toast.makeText(context, if (isBn) "প্যাক সরানো হয়েছে" else "Pack uninstalled", Toast.LENGTH_SHORT).show()
                            }
                        },
                        themeColors = themeColors,
                        isBn = isBn
                    )
                    VocabTab.FAVORITES -> VocabFavoritesTab(
                        words = allWords.filter { bookmarkedIds.contains(it.id) },
                        bookmarkedIds = bookmarkedIds,
                        onBookmarkToggle = { toggleBookmark(it) },
                        onSpeak = { speakWord(it) },
                        themeColors = themeColors,
                        isBn = isBn
                    )
                }
            }
        }
    }
}

@Composable
fun VocabExploreTab(
    words: List<VocabWord>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categoryFilter: String,
    onCategoryChange: (String) -> Unit,
    posFilter: String,
    onPosChange: (String) -> Unit,
    bookmarkedIds: List<String>,
    onBookmarkToggle: (String) -> Unit,
    onSpeak: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    val filteredWords = remember(words, searchQuery, categoryFilter, posFilter) {
        words.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.word.contains(searchQuery, ignoreCase = true) ||
                    item.meaningBn.contains(searchQuery, ignoreCase = true) ||
                    item.synonyms.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesCategory = categoryFilter == "All" || item.category.equals(categoryFilter, ignoreCase = true)
            val matchesPos = posFilter == "All" || item.partOfSpeech.equals(posFilter, ignoreCase = true)

            matchesQuery && matchesCategory && matchesPos
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            placeholder = { Text(if (isBn) "ইংরেজি শব্দ বা বাংলা অর্থ খুঁজুন..." else "Search word or meaning...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = themeColors.accent) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.accent,
                unfocusedBorderColor = themeColors.onSurface.copy(alpha = 0.2f),
                focusedContainerColor = themeColors.surface,
                unfocusedContainerColor = themeColors.surface
            )
        )

        // Filter Categories Chips
        val categories = listOf("All", "Spoken", "IELTS", "BCS", "Academic", "Idioms")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { cat ->
                val isSelected = categoryFilter == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategoryChange(cat) },
                    label = {
                        Text(
                            text = when (cat) {
                                "All" -> if (isBn) "সকল ক্যাটাগরি" else "All Categories"
                                "Spoken" -> if (isBn) "স্পোকেন" else "Spoken"
                                "IELTS" -> "IELTS"
                                "BCS" -> if (isBn) "বিসিএস ও চাকরি" else "Job & BCS"
                                "Academic" -> if (isBn) "একাডেমিক" else "Academic"
                                "Idioms" -> if (isBn) "বাগধারা" else "Idioms"
                                else -> cat
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themeColors.accent,
                        selectedLabelColor = themeColors.onAccent
                    )
                )
            }
        }

        // Word Count Summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBn) "মোট ফলাফল: ${filteredWords.size} টি শব্দ" else "Found ${filteredWords.size} words",
                style = MaterialTheme.typography.labelMedium,
                color = themeColors.onSurface.copy(alpha = 0.6f)
            )
        }

        if (filteredWords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = themeColors.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBn) "কোনো শব্দ খুঁজে পাওয়া যায়নি" else "No matching words found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredWords, key = { it.id }) { vocab ->
                    VocabWordCard(
                        vocab = vocab,
                        isBookmarked = bookmarkedIds.contains(vocab.id),
                        onBookmarkToggle = { onBookmarkToggle(vocab.id) },
                        onSpeak = { onSpeak(vocab.word) },
                        themeColors = themeColors,
                        isBn = isBn
                    )
                }
            }
        }
    }
}

@Composable
fun VocabWordCard(
    vocab: VocabWord,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onSpeak: () -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Word, Phonetic, POS, TTS & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = vocab.word,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            ),
                            color = themeColors.accent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = themeColors.accent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = vocab.partOfSpeech,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = themeColors.accent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (vocab.phonetic.isNotEmpty()) {
                        Text(
                            text = vocab.phonetic,
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.onSurface.copy(alpha = 0.55f)
                        )
                    }
                }

                // Speaker Icon
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Pronounce",
                        tint = themeColors.accent
                    )
                }

                // Bookmark Icon
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Color(0xFFE91E63) else themeColors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bangla Meaning
            Text(
                text = vocab.meaningBn,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = themeColors.onSurface
            )

            // Example Sentence
            if (vocab.exampleEn.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themeColors.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "💡 \"${vocab.exampleEn}\"",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = themeColors.onSurface
                        )
                        if (vocab.exampleBn.isNotEmpty()) {
                            Text(
                                text = "👉 ${vocab.exampleBn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColors.onSurface.copy(alpha = 0.75f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Synonyms & Antonyms
            if (vocab.synonyms.isNotEmpty() || vocab.antonyms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (vocab.synonyms.isNotEmpty()) {
                        Text(
                            text = "Syn: " + vocab.synonyms.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (vocab.antonyms.isNotEmpty()) {
                        Text(
                            text = "Ant: " + vocab.antonyms.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF44336),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VocabFlashcardTab(
    words: List<VocabWord>,
    onSpeak: (String) -> Unit,
    bookmarkedIds: List<String>,
    onBookmarkToggle: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    if (words.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (isBn) "কোনো শব্দ পাওয়া যায়নি" else "No words available", color = themeColors.onSurface)
        }
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    val currentWord = words[currentIndex.coerceIn(0, words.size - 1)]

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Progress & Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentIndex + 1} / ${words.size}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = themeColors.accent
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {
                    currentIndex = (words.indices).random()
                    isFlipped = false
                }) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = themeColors.accent)
                }
                IconButton(onClick = { onBookmarkToggle(currentWord.id) }) {
                    Icon(
                        imageVector = if (bookmarkedIds.contains(currentWord.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Fav",
                        tint = if (bookmarkedIds.contains(currentWord.id)) Color(0xFFE91E63) else themeColors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // 3D Flip Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp)
                .clickable { isFlipped = !isFlipped },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isFlipped) {
                    // FRONT SIDE (English Word + Phonetic + POS)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = themeColors.accent.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = currentWord.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = themeColors.accent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = currentWord.word,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "${currentWord.phonetic} • (${currentWord.partOfSpeech})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = themeColors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        FilledTonalIconButton(
                            onClick = { onSpeak(currentWord.word) },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak",
                                tint = themeColors.accent
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (isBn) "👆 ট্যাপ করে বাংলা অর্থ দেখুন" else "👆 Tap card to reveal meaning",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    // BACK SIDE (Bangla Meaning + Examples + Synonyms)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentWord.meaningBn,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.accent,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentWord.exampleEn.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = themeColors.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "\"${currentWord.exampleEn}\"",
                                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                        color = themeColors.onSurface
                                    )
                                    if (currentWord.exampleBn.isNotEmpty()) {
                                        Text(
                                            text = currentWord.exampleBn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = themeColors.onSurface.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentWord.synonyms.isNotEmpty()) {
                            Text(
                                text = "Synonyms: " + currentWord.synonyms.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = if (isBn) "👆 আবার ট্যাপ করে শব্দ দেখুন" else "👆 Tap card to flip back",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (currentIndex > 0) {
                        currentIndex--
                        isFlipped = false
                    }
                },
                enabled = currentIndex > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isBn) "পূর্ববর্তী" else "Previous")
            }

            Button(
                onClick = {
                    if (currentIndex < words.size - 1) {
                        currentIndex++
                        isFlipped = false
                    } else {
                        currentIndex = 0
                        isFlipped = false
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
            ) {
                Text(if (isBn) "পরবর্তী শব্দ" else "Next Word", color = themeColors.onAccent)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = themeColors.onAccent)
            }
        }
    }
}

@Composable
fun VocabQuizTab(
    words: List<VocabWord>,
    onSpeak: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    if (words.size < 4) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (isBn) "কুইজের জন্য কমপক্ষে ৪টি শব্দ প্রয়োজন" else "At least 4 words required for quiz", color = themeColors.onSurface)
        }
        return
    }

    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var questionCount by remember { mutableStateOf(0) }

    var targetWord by remember { mutableStateOf(words.random()) }
    var options by remember {
        mutableStateOf(
            generateQuizOptions(targetWord, words)
        )
    }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    fun nextQuestion() {
        targetWord = words.random()
        options = generateQuizOptions(targetWord, words)
        selectedOptionIndex = null
        isAnswered = false
        questionCount++
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Score Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.surface,
                modifier = Modifier.padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Score", tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "স্কোর: $score" else "Score: $score",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.onSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.surface,
                modifier = Modifier.padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = Color(0xFFFF5722), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "স্ট্রিক: $streak 🔥" else "Streak: $streak 🔥",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.onSurface
                    )
                }
            }
        }

        // Quiz Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isBn) "নিচের শব্দটির সঠিক অর্থ কী?" else "What is the correct meaning?",
                    style = MaterialTheme.typography.labelMedium,
                    color = themeColors.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = targetWord.word,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.accent
                    )
                    IconButton(onClick = { onSpeak(targetWord.word) }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak", tint = themeColors.accent)
                    }
                }

                Text(
                    text = "${targetWord.phonetic} • (${targetWord.partOfSpeech})",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEachIndexed { index, optionMeaning ->
                val isCorrect = optionMeaning == targetWord.meaningBn
                val isSelected = selectedOptionIndex == index

                val optionColor = when {
                    !isAnswered -> themeColors.surface
                    isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                    isSelected -> Color(0xFFF44336).copy(alpha = 0.2f)
                    else -> themeColors.surface
                }

                val borderColor = when {
                    !isAnswered -> if (isSelected) themeColors.accent else themeColors.onSurface.copy(alpha = 0.12f)
                    isCorrect -> Color(0xFF4CAF50)
                    isSelected -> Color(0xFFF44336)
                    else -> themeColors.onSurface.copy(alpha = 0.12f)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = optionColor,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !isAnswered) {
                            selectedOptionIndex = index
                            isAnswered = true
                            if (isCorrect) {
                                score += 10
                                streak++
                            } else {
                                streak = 0
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = borderColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ('A'.code + index).toChar().toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isAnswered && (isCorrect || isSelected)) borderColor else themeColors.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = optionMeaning,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = themeColors.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (isAnswered) {
                            if (isCorrect) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF4CAF50))
                            } else if (isSelected) {
                                Icon(Icons.Default.Cancel, contentDescription = "Wrong", tint = Color(0xFFF44336))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next Button
        Button(
            onClick = { nextQuestion() },
            enabled = isAnswered,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
        ) {
            Text(if (isBn) "পরবর্তী প্রশ্ন" else "Next Question", color = themeColors.onAccent, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = themeColors.onAccent)
        }
    }
}

fun generateQuizOptions(target: VocabWord, allWords: List<VocabWord>): List<String> {
    val wrongOptions = allWords
        .filter { it.id != target.id && it.meaningBn != target.meaningBn }
        .map { it.meaningBn }
        .distinct()
        .shuffled()
        .take(3)
    return (wrongOptions + target.meaningBn).shuffled()
}

@Composable
fun VocabStoreTab(
    installedPacks: List<String>,
    onInstallPack: (String) -> Unit,
    onUninstallPack: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    val availablePacks = listOf(
        VocabPack(
            id = "starter",
            titleBn = "কোর স্টার্টার প্যাক (ডিফল্ট)",
            titleEn = "Core Starter Pack (Default)",
            description = "দৈনন্দিন সবচেয়ে বেশি ব্যবহৃত ১০০+ মূল ইংরেজি শব্দ ও অর্থ",
            wordCount = 120,
            sizeKb = 45,
            level = "Beginner",
            iconName = "star"
        ),
        VocabPack(
            id = "spoken_3000",
            titleBn = "দৈনন্দিন স্পোকেন ৩,০০০ শব্দ",
            titleEn = "Daily Spoken English 3,000",
            description = "দৈনন্দিন ফ্লুয়েন্ট কথা বলার জন্য অক্সফোর্ড অনুমোদিত ৩,০০০ শব্দ",
            wordCount = 3000,
            sizeKb = 85,
            level = "Intermediate",
            iconName = "record_voice_over"
        ),
        VocabPack(
            id = "ielts_4000",
            titleBn = "IELTS ও বিদেশে উচ্চশিক্ষা প্যাক",
            titleEn = "IELTS & Higher Study Essential 4,000",
            description = "আইইএলটিএস ব্যান্ড ৭+ রিডিং ও রাইটিংয়ের জন্য একাডেমিক শব্দভান্ডার",
            wordCount = 4000,
            sizeKb = 120,
            level = "Advanced",
            iconName = "flight_takeoff"
        ),
        VocabPack(
            id = "bcs_5000",
            titleBn = "বিসিএস ও ব্যাংক জব স্পেশাল ৫,০০০",
            titleEn = "BCS & Bank Job Vocabulary 5,000",
            description = "বিগত বছরের প্রশ্ন ও সরকারি চাকরির নিয়োগ পরীক্ষার বাছাইকৃত শব্দ",
            wordCount = 5000,
            sizeKb = 150,
            level = "Career & Job",
            iconName = "account_balance"
        ),
        VocabPack(
            id = "mega_10000",
            titleBn = "মেগা ডিকশনারি প্যাক ১০,০০০+",
            titleEn = "Comprehensive Mega Pack 10,000+",
            description = "স্বয়ংসম্পূর্ণ অফলাইন রেফারেন্স ডিকশনারি ও বাগধারা",
            wordCount = 10000,
            sizeKb = 450,
            level = "Master",
            iconName = "auto_stories"
        )
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Hero Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = themeColors.accent.copy(alpha = 0.12f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = themeColors.accent,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isBn) "অন-ডিমান্ড অফলাইন ডাউনলোড" else "On-Demand Word Packs",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.onSurface
                    )
                    Text(
                        text = if (isBn) "অ্যাপের সাইজ না বাড়িয়ে আপনার প্রয়োজন অনুযায়ী শব্দভান্ডার সক্রিয় করুন।" else "Expand your vocabulary instantly without increasing app size.",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(availablePacks) { pack ->
                val isInstalled = installedPacks.contains(pack.id)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isInstalled) Color(0xFF4CAF50).copy(alpha = 0.15f) else themeColors.accent.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isInstalled) Icons.Default.CheckCircle else Icons.Default.FolderZip,
                                        contentDescription = null,
                                        tint = if (isInstalled) Color(0xFF4CAF50) else themeColors.accent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBn) pack.titleBn else pack.titleEn,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = themeColors.onSurface
                                )
                                Text(
                                    text = "${pack.wordCount} words • ${pack.sizeKb} KB • ${pack.level}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = themeColors.accent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = pack.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (isInstalled) {
                                if (pack.id != "starter") {
                                    OutlinedButton(
                                        onClick = { onUninstallPack(pack.id) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (isBn) "রিমুভ করুন" else "Remove", color = Color(0xFFF44336))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isBn) "সক্রিয় রয়েছে" else "Installed & Active",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onInstallPack(pack.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = themeColors.onAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "ডাউনলোড (${pack.sizeKb} KB)" else "Download (${pack.sizeKb} KB)",
                                        color = themeColors.onAccent,
                                        fontWeight = FontWeight.Bold
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

@Composable
fun VocabFavoritesTab(
    words: List<VocabWord>,
    bookmarkedIds: List<String>,
    onBookmarkToggle: (String) -> Unit,
    onSpeak: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean
) {
    if (words.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = themeColors.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isBn) "কোনো সংরক্ষিত শব্দ নেই" else "No saved favorite words",
                    style = MaterialTheme.typography.titleSmall,
                    color = themeColors.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = if (isBn) "শব্দ পড়ার সময় লাভ (❤️) আইকনে চাপ দিয়ে এখানে সেভ করুন।" else "Tap the heart icon on any word to bookmark it here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(words, key = { it.id }) { vocab ->
                VocabWordCard(
                    vocab = vocab,
                    isBookmarked = true,
                    onBookmarkToggle = { onBookmarkToggle(vocab.id) },
                    onSpeak = { onSpeak(vocab.word) },
                    themeColors = themeColors,
                    isBn = isBn
                )
            }
        }
    }
}

// Data Provider with Pre-compiled & Synthesized Packs
object VocabularyDataProvider {
    private val starterWords = listOf(
        VocabWord("sw_1", "Resilient", "/rɪˈzɪl.jənt/", "Adj", "স্থিতিস্থাপক, প্রতিকূলতা জয় করতে সক্ষম", "She is resilient and never gives up.", "তিনি দৃঢ়চেতা এবং কখনো হার মানেন না।", listOf("Tough", "Strong"), listOf("Fragile", "Weak"), "IELTS", "starter"),
        VocabWord("sw_2", "Eloquent", "/ˈel.ə.kwənt/", "Adj", "বাকপটু, আকর্ষণীয় ও মার্জিত বক্তা", "He gave an eloquent speech at the seminar.", "তিনি সেমিনারে একটি চমৎকার ও প্রাঞ্জল বক্তব্য দেন।", listOf("Fluent", "Articulate"), listOf("Inarticulate"), "Spoken", "starter"),
        VocabWord("sw_3", "Meticulous", "/məˈtɪk.jə.ləs/", "Adj", "খুঁতখুঁতে, অত্যন্ত সতর্ক ও নিখুঁত", "He is meticulous about his code quality.", "তিনি তার কোডের মানের ব্যাপারে অত্যন্ত সতর্ক।", listOf("Precise", "Thorough"), listOf("Careless", "Sloppy"), "Academic", "starter"),
        VocabWord("sw_4", "Pragmatic", "/præɡˈmæt.ɪk/", "Adj", "বাস্তবধর্মী, বাস্তববাদী", "We need a pragmatic solution to this problem.", "এই সমস্যার জন্য আমাদের একটি বাস্তবমুখী সমাধান দরকার।", listOf("Practical", "Realistic"), listOf("Idealistic"), "BCS", "starter"),
        VocabWord("sw_5", "Perseverance", "/ˌpɜː.sɪˈvɪə.rəns/", "Noun", "অধ্যবসায়, নিরবচ্ছিন্ন প্রচেষ্টা", "Success comes through patience and perseverance.", "ধৈর্য ও অধ্যবসায়ের মাধ্যমেই সাফল্য আসে।", listOf("Persistence", "Dedication"), listOf("Apathy"), "IELTS", "starter"),
        VocabWord("sw_6", "Ambiguous", "/æmˈbɪɡ.ju.əs/", "Adj", "দ্ব্যর্থবোধক, অস্পষ্ট", "The instructions were confusing and ambiguous.", "নির্দেশনাগুলো বিভ্রান্তিকর ও অস্পষ্ট ছিল।", listOf("Vague", "Unclear"), listOf("Clear", "Definite"), "BCS", "starter"),
        VocabWord("sw_7", "Inevitable", "/ɪˈnev.ɪ.tə.bəl/", "Adj", "অনিবার্য, যা এড়ানো যায় না", "Change is an inevitable part of life.", "পরিবর্তন জীবনের একটি অনিবার্য অংশ।", listOf("Unavoidable", "Certain"), listOf("Avoidable"), "Academic", "starter"),
        VocabWord("sw_8", "Benevolent", "/bəˈnev.əl.ənt/", "Adj", "দয়াবান, পরোপকারী", "A benevolent gentleman donated to the orphanage.", "একজন দয়ালু ব্যক্তি এতিমখানায় অনুদান দিয়েছেন।", listOf("Kind", "Generous"), listOf("Malevolent", "Cruel"), "Spoken", "starter"),
        VocabWord("sw_9", "Ubiquitous", "/juːˈbɪk.wɪ.təs/", "Adj", "সর্বব্যাপী, যা সর্বত্র বিদ্যমান", "Smartphones have become ubiquitous today.", "স্মার্টফোন আজকাল সর্বত্র ছড়িয়ে পড়েছে।", listOf("Omnipresent", "Everywhere"), listOf("Rare", "Scarce"), "IELTS", "starter"),
        VocabWord("sw_10", "Candid", "/ˈkæn.dɪd/", "Adj", "অকপট, স্পষ্টবাদী ও সৎ", "She gave her candid opinion on the proposal.", "প্রস্তাবটির বিষয়ে তিনি তার অকপট মতামত দিয়েছেন।", listOf("Frank", "Honest"), listOf("Deceitful", "Shy"), "Spoken", "starter"),
        VocabWord("sw_11", "Diligence", "/ˈdɪl.ɪ.dʒəns/", "Noun", "পরিশ্রম, নিষ্ঠা", "His diligence earned him the promotion.", "তার নিষ্ঠা ও পরিশ্রম তাকে পদোন্নতি এনে দিয়েছিল।", listOf("Hard work", "Industriousness"), listOf("Laziness"), "BCS", "starter"),
        VocabWord("sw_12", "Alleviate", "/əˈliː.vi.eɪt/", "Verb", "উপশম করা, তীব্রতা কমানো", "Medicine helps alleviate physical pain.", "ওষুধ শারীরিক ব্যথা উপশমে সহায়তা করে।", listOf("Relieve", "Ease", "Reduce"), listOf("Aggravate", "Worsen"), "IELTS", "starter"),
        VocabWord("sw_13", "Lucid", "/ˈluː.sɪd/", "Adj", "স্পষ্ট, সহজে বোধগম্য", "The professor gave a lucid explanation.", "অধ্যাপক অত্যন্ত প্রাঞ্জল ব্যাখ্যা দিয়েছিলেন।", listOf("Clear", "Transparent"), listOf("Confusing", "Vague"), "Academic", "starter"),
        VocabWord("sw_14", "Fortitude", "/ˈfɔː.tɪ.tʃuːd/", "Noun", "মানসিক শক্তি, বিপদে অটল থাকা", "She endured the hardship with great fortitude.", "তিনি অত্যন্ত মানসিক শক্তির সাথে কষ্ট সহ্য করেছিলেন।", listOf("Courage", "Bravery"), listOf("Fear", "Cowardice"), "BCS", "starter"),
        VocabWord("sw_15", "Spontaneous", "/spɒnˈteɪ.ni.əs/", "Adj", "স্বতঃস্ফূর্ত, স্বাভাবিক", "The audience gave a spontaneous applause.", "দর্শকরা স্বতঃস্ফূর্ত করতালিতে মেতে উঠেছিল।", listOf("Natural", "Unplanned"), listOf("Forced", "Planned"), "Spoken", "starter"),
        VocabWord("sw_16", "Comprehensive", "/ˌkɒm.prɪˈhen.sɪv/", "Adj", "বিস্তৃত, সর্বাঙ্গীণ, পূর্ণাঙ্গ", "The book offers a comprehensive guide to science.", "বইটি বিজ্ঞানের একটি পূর্ণাঙ্গ নির্দেশিকা দেয়।", listOf("Complete", "Exhaustive"), listOf("Limited", "Partial"), "Academic", "starter"),
        VocabWord("sw_17", "Versatile", "/ˈvɜː.sə.taɪl/", "Adj", "বহুমুখী প্রতিভাধর, বহু কাজে সক্ষম", "He is a versatile actor and singer.", "তিনি একজন বহুমুখী অভিনেতা ও সংগীতশিল্পী।", listOf("Multitalented", "Adaptable"), listOf("Inflexible"), "Spoken", "starter"),
        VocabWord("sw_18", "Empathy", "/ˈem.pə.θi/", "Noun", "সহমর্মিতা, অন্যের অনুভূতি উপলব্ধি করার ক্ষমতা", "True leadership requires genuine empathy.", "প্রকৃত নেতৃত্বের জন্য সত্যিকারের সহমর্মিতা প্রয়োজন।", listOf("Compassion", "Understanding"), listOf("Indifference"), "IELTS", "starter"),
        VocabWord("sw_19", "Feasible", "/ˈfiː.zə.bəl/", "Adj", "বাস্তবায়নযোগ্য, সম্ভবপর", "Is this project practically feasible within budget?", "বাজেটের মধ্যে এই প্রজেক্টটি বাস্তবায়নযোগ্য কি?", listOf("Workable", "Viable"), listOf("Impossible"), "BCS", "starter"),
        VocabWord("sw_20", "Piece of Cake", "/piːs əv keɪk/", "Idiom", "খুবই সহজ কাজ (দুধভাত)", "The exam was a piece of cake for him.", "পরীক্ষাটি তার জন্য খুবই সহজ ছিল।", listOf("Easy task", "Child's play"), listOf("Uphill task"), "Idioms", "starter"),
        VocabWord("sw_21", "Break the Ice", "/breɪk ði aɪs/", "Idiom", "আড়ষ্টতা বা নীরবতা ভেঙে কথা শুরু করা", "A small joke helped break the ice.", "ছোট একটি কৌতুক পরিবেশের আড়ষ্টতা ভাঙতে সাহায্য করেছিল।", listOf("Initiate", "Warm up"), listOf("Stay silent"), "Idioms", "starter"),
        VocabWord("sw_22", "Once in a Blue Moon", "/wʌns ɪn ə bluː muːn/", "Idiom", "কদাচিৎ, খুব বিরল ঘটনা", "He visits his hometown once in a blue moon.", "সে খুব বিরল সময়ে গ্রামের বাড়িতে যায়।", listOf("Rarely", "Seldom"), listOf("Frequently"), "Idioms", "starter"),
        VocabWord("sw_23", "Pinnacle", "/ˈpɪn.ə.kəl/", "Noun", "চূড়া, সর্বোচ্চ শিখর", "He reached the pinnacle of his career.", "তিনি তার ক্যারিয়ারের সর্বোচ্চ শিখরে পৌঁছেছিলেন।", listOf("Peak", "Summit", "Apex"), listOf("Bottom", "Nadir"), "BCS", "starter"),
        VocabWord("sw_24", "Scrutinize", "/ˈskruː.tɪ.naɪz/", "Verb", "সূক্ষ্মভাবে পরীক্ষা করা", "The auditors scrutinized every transaction.", "অডিটররা প্রতিটি লেনদেন গভীরভাবে পরীক্ষা করেছেন।", listOf("Examine", "Inspect"), listOf("Ignore", "Overlook"), "IELTS", "starter"),
        VocabWord("sw_25", "Tenacious", "/təˈneɪ.ʃəs/", "Adj", "নাছোড়বান্দা, সংকল্পবদ্ধ", "He is a tenacious researcher.", "তিনি একজন অদম্য ও নাছোড় গবেষক।", listOf("Persistent", "Determined"), listOf("Yielding", "Weak"), "Academic", "starter")
    )

    fun getWordsForPacks(installedPackIds: Set<String>): List<VocabWord> {
        val list = starterWords.toMutableList()

        if (installedPackIds.contains("spoken_3000")) {
            list.addAll(generateSpokenWords())
        }
        if (installedPackIds.contains("ielts_4000")) {
            list.addAll(generateIeltsWords())
        }
        if (installedPackIds.contains("bcs_5000")) {
            list.addAll(generateBcsWords())
        }
        if (installedPackIds.contains("mega_10000")) {
            list.addAll(generateMegaWords())
        }

        return list.distinctBy { it.word.lowercase() }
    }

    private fun generateSpokenWords(): List<VocabWord> {
        return listOf(
            VocabWord("sp_1", "Accommodate", "/əˈkɒm.ə.deɪt/", "Verb", "স্থান দেওয়া, মানিয়ে নেওয়া", "The hotel can accommodate 200 guests.", "হোটেলটিতে ২০০ জন অতিথি থাকতে পারবেন।", listOf("House", "Adapt"), listOf("Reject"), "Spoken", "spoken_3000"),
            VocabWord("sp_2", "Apparent", "/əˈpær.ənt/", "Adj", "দৃশ্যমান, সুস্পষ্ট", "Her happiness was apparent to everyone.", "তার আনন্দ সবার কাছে সুস্পষ্ট ছিল।", listOf("Evident", "Obvious"), listOf("Hidden"), "Spoken", "spoken_3000"),
            VocabWord("sp_3", "Acknowledge", "/əkˈnɒl.ɪdʒ/", "Verb", "স্বীকার করা, স্বীকৃতি দেওয়া", "He refused to acknowledge his mistake.", "তিনি তার ভুল স্বীকার করতে রাজি হননি।", listOf("Admit", "Accept"), listOf("Deny"), "Spoken", "spoken_3000"),
            VocabWord("sp_4", "Casual", "/ˈkæʒ.u.əl/", "Adj", "অনানুষ্ঠানিক, সাধারণ", "Wear casual clothes for the picnic.", "পিকনিকের জন্য সাধারণ জামাকাপড় পরুন।", listOf("Informal", "Relaxed"), listOf("Formal"), "Spoken", "spoken_3000"),
            VocabWord("sp_5", "Cooperate", "/kəʊˈɒp.ər.eɪt/", "Verb", "সহযোগিতা করা", "All neighbors cooperated to clean the area.", "এলাকা পরিষ্কার করতে সব প্রতিবেশী সহযোগিতা করেছিলেন।", listOf("Collaborate", "Assist"), listOf("Hinder"), "Spoken", "spoken_3000"),
            VocabWord("sp_6", "Enthusiastic", "/ɪnˌθjuː.ziˈæs.tɪk/", "Adj", "উৎসাহী, উদ্দীপ্ত", "Students were enthusiastic about the trip.", "ভ্রমণের ব্যাপারে ছাত্রছাত্রীরা খুব উৎসাহী ছিল।", listOf("Eager", "Excited"), listOf("Indifferent"), "Spoken", "spoken_3000"),
            VocabWord("sp_7", "Hesitate", "/ˈhez.ɪ.teɪt/", "Verb", "দ্বিধা করা, ইতস্তত করা", "Do not hesitate to ask any question.", "যেকোনো প্রশ্ন করতে দ্বিধা করবেন না।", listOf("Waver", "Pause"), listOf("Proceed"), "Spoken", "spoken_3000"),
            VocabWord("sp_8", "Intriguing", "/ɪnˈtriː.ɡɪŋ/", "Adj", "কৌতূহলোদ্দীপক, আকর্ষণীয়", "She told an intriguing story.", "তিনি একটি চমৎকার কৌতূহলোদ্দীপক গল্প শোনালেন।", listOf("Fascinating", "Interesting"), listOf("Boring"), "Spoken", "spoken_3000"),
            VocabWord("sp_9", "Reliable", "/rɪˈlaɪ.ə.bəl/", "Adj", "নির্ভরযোগ্য, বিশ্বাসযোগ্য", "He is a reliable friend in hard times.", "কঠিন সময়ে তিনি একজন নির্ভরযোগ্য বন্ধু।", listOf("Trustworthy", "Dependable"), listOf("Unreliable"), "Spoken", "spoken_3000"),
            VocabWord("sp_10", "Spill the Beans", "/spɪl ðə biːnz/", "Idiom", "গোপন কথা ফাঁস করে দেওয়া", "Do not spill the beans about the surprise party.", "সারপ্রাইজ পার্টির গোপন কথা ফাঁস করো না।", listOf("Reveal secret"), listOf("Keep secret"), "Idioms", "spoken_3000")
        )
    }

    private fun generateIeltsWords(): List<VocabWord> {
        return listOf(
            VocabWord("ie_1", "Aberration", "/ˌæb.əˈreɪ.ʃən/", "Noun", "স্বাভাবিক নিয়মের বিচ্যুতি বা ব্যতিক্রম", "The sudden cold wave was an aberration.", "হঠাৎ শৈত্যপ্রবাহ ছিল আবহাওয়ার একটি ব্যতিক্রম।", listOf("Anomaly", "Deviation"), listOf("Normality"), "IELTS", "ielts_4000"),
            VocabWord("ie_2", "Proponent", "/prəˈpəʊ.nənt/", "Noun", "সমর্থক, প্রবক্তা", "He is a strong proponent of renewable energy.", "তিনি নবায়নযোগ্য শক্তির একজন প্রবল সমর্থক।", listOf("Advocate", "Supporter"), listOf("Opponent"), "IELTS", "ielts_4000"),
            VocabWord("ie_3", "Exacerbate", "/ɪɡˈzæs.ə.beɪt/", "Verb", "পরিস্থিতি আরও খারাপ বা অবনতি করা", "Pollution exacerbates respiratory diseases.", "দূষণ শ্বাসকষ্টের রোগকে আরও বাড়িয়ে দেয়।", listOf("Worsen", "Aggravate"), listOf("Improve", "Alleviate"), "IELTS", "ielts_4000"),
            VocabWord("ie_4", "Mitigate", "/ˈmɪt.ɪ.ɡeɪt/", "Verb", "প্রশমিত করা, হ্রাস করা", "Planting trees helps mitigate global warming.", "গাছ লাগানো গ্লোবাল ওয়ার্মিং কমাতে সাহায্য করে।", listOf("Lessen", "Diminish"), listOf("Intensify"), "IELTS", "ielts_4000"),
            VocabWord("ie_5", "Substantiate", "/səbˈstæn.ʃi.eɪt/", "Verb", "প্রমাণ দিয়ে প্রতিষ্ঠা করা বা সত্যতা প্রতিপন্ন করা", "You must substantiate your thesis with data.", "আপনার থিসিস তথ্যের সাহায্যে প্রমাণ করতে হবে।", listOf("Verify", "Validate"), listOf("Disprove"), "IELTS", "ielts_4000"),
            VocabWord("ie_6", "Prevalent", "/ˈprev.əl.ənt/", "Adj", "বহুল প্রচলিত, বিদ্যমান", "The custom is still prevalent in rural areas.", "প্রথাটি গ্রামাঞ্চলে এখনও প্রচলিত।", listOf("Widespread", "Common"), listOf("Rare"), "IELTS", "ielts_4000"),
            VocabWord("ie_7", "Plausible", "/ˈplɔː.zə.bəl/", "Adj", "বিশ্বাসযোগ্য, যুক্তিযুক্ত", "Her explanation sounded plausible.", "তার ব্যাখ্যাটি যুক্তিযুক্ত মনে হয়েছিল।", listOf("Believable", "Reasonable"), listOf("Implausible"), "IELTS", "ielts_4000"),
            VocabWord("ie_8", "Paradigm", "/ˈpær.ə.daɪm/", "Noun", "দৃষ্টান্ত, আদর্শ কাঠামো বা মডেল", "AI represents a paradigm shift in technology.", "এআই প্রযুক্তিতে একটি আদর্শ কাঠামোগত পরিবর্তন এনেছে।", listOf("Model", "Pattern", "Archetype"), listOf(), "IELTS", "ielts_4000")
        )
    }

    private fun generateBcsWords(): List<VocabWord> {
        return listOf(
            VocabWord("bc_1", "Ephemeral", "/ɪˈfem.ər.əl/", "Adj", "ক্ষণস্থায়ী, অল্পকালব্যাপী", "Fame in social media is often ephemeral.", "সোশ্যাল মিডিয়ার খ্যাতি প্রায়শই ক্ষণস্থায়ী।", listOf("Fleeting", "Transient", "Short-lived"), listOf("Permanent", "Eternal"), "BCS", "bcs_5000"),
            VocabWord("bc_2", "Fastidious", "/fæsˈtɪd.i.əs/", "Adj", "খুঁতখুঁতে, সহজে সন্তুষ্ট হয় না এমন", "He is fastidious regarding hygiene.", "পরিচ্ছন্নতার বিষয়ে তিনি অত্যন্ত খুঁতখুঁতে।", listOf("Picky", "Fussy"), listOf("Careless"), "BCS", "bcs_5000"),
            VocabWord("bc_3", "Gregarious", "/ɡrɪˈɡeə.ri.əs/", "Adj", "মিশুক, দলপ্রিয়, সামাজপ্রিয়", "Dolphins are friendly and gregarious animals.", "ডলফিন বন্ধুভাবাপন্ন এবং দলপ্রিয় প্রাণী।", listOf("Sociable", "Outgoing"), listOf("Solitary", "Introverted"), "BCS", "bcs_5000"),
            VocabWord("bc_4", "Magnanimous", "/mæɡˈnæn.ɪ.məs/", "Adj", "মহানুভব, উদারচেতা", "He was magnanimous in victory.", "বিজয়ের পর তিনি মহানুভবতা প্রদর্শন করেছিলেন।", listOf("Generous", "Noble"), listOf("Selfish", "Petty"), "BCS", "bcs_5000"),
            VocabWord("bc_5", "Sycophant", "/ˈsɪk.ə.fænt/", "Noun", "চাটুকার, তোষামোদকারী", "The king was surrounded by flattering sycophants.", "রাজা স্তাবক চাটুকারদের দ্বারা পরিবেষ্টিত ছিলেন।", listOf("Flatterer", "Toady"), listOf(), "BCS", "bcs_5000"),
            VocabWord("bc_6", "Venerate", "/ˈven.ər.eɪt/", "Verb", "গভীর শ্রদ্ধা করা, পূজা করা", "We venerate our freedom fighters.", "আমরা আমাদের বীর মুক্তিযোদ্ধাদের পরম শ্রদ্ধায় স্মরণ করি।", listOf("Revere", "Respect"), listOf("Despise"), "BCS", "bcs_5000")
        )
    }

    private fun generateMegaWords(): List<VocabWord> {
        return listOf(
            VocabWord("mg_1", "Anachronism", "/əˈnæk.rə.nɪ.zəm/", "Noun", "কালবৈষম্য, যুগের সাথে অসংগতি", "A sword in modern warfare is an anachronism.", "আধুনিক যুদ্ধে তরবারি একটি কালবৈষম্য।", listOf("Misplacement"), listOf(), "Academic", "mega_10000"),
            VocabWord("mg_2", "Capricious", "/kəˈprɪʃ.əs/", "Adj", "খামখেয়ালী, হঠাৎ মত পরিবর্তনশীল", "The island has capricious tropical weather.", "দ্বীপটিতে খামখেয়ালী ধরনের ক্রান্তীয় আবহাওয়া বিরাজ করে।", listOf("Whimsical", "Fickle"), listOf("Predictable", "Stable"), "Academic", "mega_10000"),
            VocabWord("mg_3", "Obsequious", "/əbˈsiː.kwi.əs/", "Adj", "অতি তোষামোদকারী, দাসবৎ অনুগত", "He bowed in an obsequious manner.", "তিনি তোষামোদি ভঙ্গিতে মাথা নত করলেন।", listOf("Servile", "Submissive"), listOf("Assertive"), "Academic", "mega_10000"),
            VocabWord("mg_4", "Enervate", "/ˈen.ə.veɪt/", "Verb", "দুর্বল করা, শক্তি হ্রাস করা", "Intense heat enervated the workers.", "তীব্র তাপদাহ কর্মীদের ক্লান্ত ও শক্তিহীন করে ফেলেছিল।", listOf("Weaken", "Exhaust"), listOf("Energize", "Strengthen"), "Academic", "mega_10000")
        )
    }
}

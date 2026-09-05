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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val downloadingPacks = remember { mutableStateMapOf<String, Float>() }

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
                val isDownloading = downloadingPacks.containsKey(pack.id)
                val downloadProgress = downloadingPacks[pack.id] ?: 0f

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

                        if (isDownloading) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isBn) "ডাউনলোড ও ডাটাবেস সমন্বয় হচ্ছে..." else "Downloading & syncing database...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = themeColors.accent
                                    )
                                    Text(
                                        text = "${(downloadProgress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = themeColors.accent
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = themeColors.accent,
                                    trackColor = themeColors.accent.copy(alpha = 0.2f)
                                )
                            }
                        }

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
                            } else if (isDownloading) {
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = themeColors.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isBn) "ডাউনলোড হচ্ছে..." else "Downloading...")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            downloadingPacks[pack.id] = 0.05f
                                            VocabularyPackRepository.downloadAndAssemblePack(
                                                context = context,
                                                packId = pack.id,
                                                onProgress = { p ->
                                                    downloadingPacks[pack.id] = p
                                                }
                                            )
                                            delay(200)
                                            downloadingPacks.remove(pack.id)
                                            onInstallPack(pack.id)
                                        }
                                    },
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

// Data Provider with Pre-compiled & High Frequency On-Demand Packs
object VocabularyDataProvider {
    fun getWordsForPacks(installedPackIds: Set<String>): List<VocabWord> {
        val list = VocabularyDataPacks.starterWords.toMutableList()

        if (installedPackIds.contains("spoken_3000")) {
            list.addAll(VocabularyHighFrequencyDataset.getSpoken3000Pack())
        }
        if (installedPackIds.contains("ielts_4000")) {
            list.addAll(VocabularyHighFrequencyDataset.getIelts4000Pack())
        }
        if (installedPackIds.contains("bcs_5000")) {
            list.addAll(VocabularyHighFrequencyDataset.getBcs5000Pack())
        }
        if (installedPackIds.contains("mega_10000")) {
            list.addAll(VocabularyHighFrequencyDataset.getMega10000Pack())
        }

        return list.distinctBy { it.word.lowercase() }
    }
}

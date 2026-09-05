package com.example.ui.screens.tools

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
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
    val exampleEn: String = "",
    val exampleBn: String = "",
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val category: String, // "Spoken", "IELTS", "BCS", "Academic", "Idioms"
    val packId: String = "starter",
    val frequencyRank: Int = 9999
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

enum class VocabSortOption(val titleBn: String, val titleEn: String) {
    TOP_1000("শীর্ষ ১,০০০ ফ্রিকোয়েন্সি", "Top 1,000 High Frequency"),
    FREQUENCY("ফ্রিকোয়েন্সি রেঙ্ক", "Frequency Rank"),
    ALPHABETICAL_AZ("A থেকে Z", "A to Z"),
    ALPHABETICAL_ZA("Z থেকে A", "Z to A"),
    LENGTH("শব্দের দৈর্ঘ্য", "Word Length")
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

    // Active Word List generated from installed packs & disk database
    val allWords = remember(installedPacks.toList()) {
        VocabularyDataProvider.getWordsForPacks(context, installedPacks.toSet())
    }

    var selectedTab by remember { mutableStateOf(VocabTab.EXPLORE) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedPosFilter by remember { mutableStateOf("All") }
    var selectedSortOption by remember { mutableStateOf(VocabSortOption.FREQUENCY) }
    var selectedLetterFilter by remember { mutableStateOf<Char?>(null) }
    var top1000Only by remember { mutableStateOf(false) }

    var isHeaderVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -12f) {
                    isHeaderVisible = false
                } else if (delta > 12f) {
                    isHeaderVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isHeaderVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isBn) "ভোকাবুলারি মাস্টার" else "Vocabulary Master",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = themeColors.onSurface
                            )
                            Text(
                                text = if (isBn) "${allWords.size} টি শব্দ সক্রিয় • অফলাইন ডিকশনারি" else "${allWords.size} Words Active • Offline Ready",
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
            }
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(nestedScrollConnection)
        ) {
            // Main Tab Navigation Chips (Hides with header on scroll down)
            AnimatedVisibility(
                visible = isHeaderVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
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
                            border = if (isSelected) null else BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)),
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
            }

            // Compact 1-line chip indicator when header is hidden
            AnimatedVisibility(
                visible = !isHeaderVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = themeColors.surface,
                    border = BorderStroke(0.5.dp, themeColors.onSurface.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isHeaderVisible = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = themeColors.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "ভোকাবুলারি (${allWords.size})" else "Vocab (${allWords.size})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = themeColors.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = themeColors.accent.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) selectedTab.titleBn else selectedTab.titleEn,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = themeColors.accent
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Expand Header",
                                    tint = themeColors.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = themeColors.onSurface.copy(alpha = 0.08f), thickness = 1.dp)

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
                        sortOption = selectedSortOption,
                        onSortChange = { selectedSortOption = it },
                        letterFilter = selectedLetterFilter,
                        onLetterChange = { selectedLetterFilter = it },
                        top1000Only = top1000Only,
                        onTop1000Toggle = { top1000Only = it },
                        bookmarkedIds = bookmarkedIds,
                        onBookmarkToggle = { toggleBookmark(it) },
                        onSpeak = { speakWord(it) },
                        themeColors = themeColors,
                        isBn = isBn,
                        isHeaderVisible = isHeaderVisible
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
                                    Toast.makeText(context, if (isBn) "প্যাক সফলভাবে সক্রিয় হয়েছে!" else "Pack activated successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onUninstallPack = { packId ->
                            if (packId != "starter") {
                                installedPacks.remove(packId)
                                prefs.edit().putStringSet("installed_packs", installedPacks.toSet()).apply()
                                Toast.makeText(context, if (isBn) "প্যাক সরানো হয়েছে" else "Pack uninstalled", Toast.LENGTH_SHORT).show()
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
    sortOption: VocabSortOption,
    onSortChange: (VocabSortOption) -> Unit,
    letterFilter: Char?,
    onLetterChange: (Char?) -> Unit,
    top1000Only: Boolean,
    onTop1000Toggle: (Boolean) -> Unit,
    bookmarkedIds: List<String>,
    onBookmarkToggle: (String) -> Unit,
    onSpeak: (String) -> Unit,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    isHeaderVisible: Boolean = true
) {
    val listState = rememberLazyListState()

    val filteredAndSortedWords = remember(
        words, searchQuery, categoryFilter, posFilter, sortOption, letterFilter, top1000Only
    ) {
        var list = words.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.word.contains(searchQuery, ignoreCase = true) ||
                    item.meaningBn.contains(searchQuery, ignoreCase = true) ||
                    item.synonyms.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesCategory = when (categoryFilter) {
                "All" -> true
                "Top 1000" -> item.frequencyRank <= 1000
                else -> item.category.equals(categoryFilter, ignoreCase = true)
            }
            val matchesPos = posFilter == "All" || item.partOfSpeech.equals(posFilter, ignoreCase = true)
            val matchesLetter = letterFilter == null || item.word.startsWith(letterFilter, ignoreCase = true)
            val matchesTop1000 = !top1000Only || item.frequencyRank <= 1000

            matchesQuery && matchesCategory && matchesPos && matchesLetter && matchesTop1000
        }

        list = when (sortOption) {
            VocabSortOption.TOP_1000 -> list.sortedBy { it.frequencyRank }.take(1000)
            VocabSortOption.FREQUENCY -> list.sortedBy { it.frequencyRank }
            VocabSortOption.ALPHABETICAL_AZ -> list.sortedBy { it.word.lowercase() }
            VocabSortOption.ALPHABETICAL_ZA -> list.sortedByDescending { it.word.lowercase() }
            VocabSortOption.LENGTH -> list.sortedBy { it.word.length }
        }
        list
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input Bar (Always visible at top)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            placeholder = { Text(if (isBn) "শব্দ বা বাংলা অর্থ খুঁজুন..." else "Search word or meaning...") },
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

        // Collapsible Top Sub-Header Section (Hides A-Z scroller, Chips & Sort on Scroll Down)
        AnimatedVisibility(
            visible = isHeaderVisible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                // A-Z Quick Index Alphabet Scroller
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (letterFilter == null) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onLetterChange(null) }
                    ) {
                        Text(
                            text = if (isBn) "সকল" else "All",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (letterFilter == null) themeColors.onAccent else themeColors.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    ('A'..'Z').forEach { char ->
                        val isSelected = letterFilter == char
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onLetterChange(if (isSelected) null else char) }
                        ) {
                            Text(
                                text = char.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Filter Categories & Sorting Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category filter chips
                    val categories = listOf("All", "Top 1000", "Spoken", "IELTS", "BCS", "Academic", "Idioms")
                    LazyRow(
                        modifier = Modifier.weight(1f),
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
                                            "All" -> if (isBn) "সকল" else "All"
                                            "Top 1000" -> if (isBn) "টপ ১,০০০" else "Top 1000"
                                            "Spoken" -> if (isBn) "স্পোকেন" else "Spoken"
                                            "IELTS" -> "IELTS"
                                            "BCS" -> if (isBn) "বিসিএস" else "BCS"
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

                    // Sort Dropdown Menu Button
                    var showSortMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = themeColors.accent
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            VocabSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isBn) option.titleBn else option.titleEn,
                                            fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSortChange(option)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (sortOption == option) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = themeColors.accent)
                                        }
                                    }
                                )
                            }
                        }
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
                        text = if (isBn) "মোট প্রদর্শিত: ${filteredAndSortedWords.size} টি শব্দ" else "Showing ${filteredAndSortedWords.size} words",
                        style = MaterialTheme.typography.labelMedium,
                        color = themeColors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (isBn) "সর্ট: ${sortOption.titleBn}" else "Sort: ${sortOption.titleEn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = themeColors.accent
                    )
                }
            }
        }

        if (filteredAndSortedWords.isEmpty()) {
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
                        modifier = Modifier.size(56.dp),
                        tint = themeColors.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBn) "কোনো শব্দ খুঁজে পাওয়া যায়নি" else "No matching words found",
                        style = MaterialTheme.typography.titleMedium,
                        color = themeColors.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (isBn) "অনুগ্রহ করে ফিল্টার পরিবর্তন করে চেষ্টা করুন" else "Try clearing filters or search query",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredAndSortedWords, key = { it.id }) { vocab ->
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
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = vocab.word,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = themeColors.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = vocab.partOfSpeech,
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.accent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSpeak, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = themeColors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onBookmarkToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFFE91E63) else themeColors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = vocab.meaningBn,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = themeColors.accent
            )

            if (vocab.phonetic.isNotBlank()) {
                Text(
                    text = vocab.phonetic,
                    style = MaterialTheme.typography.labelSmall,
                    color = themeColors.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Expanded Details
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = themeColors.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (vocab.exampleEn.isNotBlank()) {
                        Text(
                            text = if (isBn) "উদাহরণ বাক্য:" else "Example Sentence:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "\"${vocab.exampleEn}\"",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = themeColors.onSurface
                        )
                        if (vocab.exampleBn.isNotBlank()) {
                            Text(
                                text = vocab.exampleBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColors.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    if (vocab.synonyms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) "সমার্থক শব্দ (Synonyms): " + vocab.synonyms.joinToString(", ") else "Synonyms: " + vocab.synonyms.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    if (vocab.antonyms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) "বিপরীত শব্দ (Antonyms): " + vocab.antonyms.joinToString(", ") else "Antonyms: " + vocab.antonyms.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF44336)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Category: ${vocab.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Rank #${vocab.frequencyRank}",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.onSurface.copy(alpha = 0.5f)
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
    if (words.isEmpty()) return

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }

    val filteredWords = remember(words, selectedCategory, selectedLetter) {
        words.filter { w ->
            val matchesCat = when (selectedCategory) {
                "All" -> true
                "Top 1000" -> w.frequencyRank <= 1000
                else -> w.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesLetter = selectedLetter == null || w.word.startsWith(selectedLetter!!, ignoreCase = true)
            matchesCat && matchesLetter
        }.ifEmpty { words }
    }

    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    if (currentIndex >= filteredWords.size) {
        currentIndex = 0
    }
    val currentWord = filteredWords[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf("All", "Top 1000", "Spoken", "IELTS", "BCS", "Academic", "Idioms")
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedCategory = cat
                        currentIndex = 0
                        isFlipped = false
                    },
                    label = {
                        Text(
                            text = when (cat) {
                                "All" -> if (isBn) "সকল" else "All"
                                "Top 1000" -> if (isBn) "টপ ১,০০০" else "Top 1000"
                                "Spoken" -> if (isBn) "স্পোকেন" else "Spoken"
                                "IELTS" -> "IELTS"
                                "BCS" -> if (isBn) "বিসিএস" else "BCS"
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

        // A-Z Quick Index Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (selectedLetter == null) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.clickable { selectedLetter = null }
            ) {
                Text(
                    text = if (isBn) "সকল" else "All",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedLetter == null) themeColors.onAccent else themeColors.onSurface,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            ('A'..'Z').forEach { c ->
                val isSel = selectedLetter == c
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSel) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.clickable { selectedLetter = if (isSel) null else c }
                ) {
                    Text(
                        text = c.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSel) themeColors.onAccent else themeColors.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Progress & Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentIndex + 1} / ${filteredWords.size}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = themeColors.accent
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {
                    currentIndex = (filteredWords.indices).random()
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

        // 3D Dynamic Auto-Sizing Flip Card (No Internal Card Scroll)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 340.dp)
                .padding(vertical = 12.dp)
                .clickable { isFlipped = !isFlipped },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isFlipped) {
                    // FRONT SIDE
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = themeColors.accent.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "${currentWord.category} • #${currentWord.frequencyRank}",
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

                        Spacer(modifier = Modifier.height(24.dp))

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

                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = if (isBn) "👆 ট্যাপ করে বাংলা অর্থ দেখুন" else "👆 Tap card to reveal meaning",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    // BACK SIDE
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

                        if (currentWord.exampleEn.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = themeColors.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "\"${currentWord.exampleEn}\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                        color = themeColors.onSurface
                                    )
                                    if (currentWord.exampleBn.isNotBlank()) {
                                        Text(
                                            text = currentWord.exampleBn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = themeColors.onSurface.copy(alpha = 0.75f),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (currentWord.synonyms.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Synonyms: " + currentWord.synonyms.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4CAF50),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
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
                .padding(vertical = 8.dp),
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
                    if (currentIndex < filteredWords.size - 1) {
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

    var selectedSource by remember { mutableStateOf("All") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var quizMode by remember { mutableStateOf("EN_TO_BN") } // "EN_TO_BN", "BN_TO_EN"

    val activeQuizPool = remember(words, selectedSource, selectedLetter) {
        val pool = words.filter { w ->
            val matchesSource = when (selectedSource) {
                "Top1000" -> w.frequencyRank <= 1000
                "BCS" -> w.category.equals("BCS", ignoreCase = true)
                "IELTS" -> w.category.equals("IELTS", ignoreCase = true)
                "Spoken" -> w.category.equals("Spoken", ignoreCase = true)
                else -> true
            }
            val matchesLetter = selectedLetter == null || w.word.startsWith(selectedLetter!!, ignoreCase = true)
            matchesSource && matchesLetter
        }
        if (pool.size >= 4) pool else words
    }

    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var questionCount by remember { mutableStateOf(0) }

    var targetWord by remember { mutableStateOf(activeQuizPool.random()) }
    var options by remember {
        mutableStateOf(
            generateQuizOptions(targetWord, activeQuizPool, quizMode == "BN_TO_EN")
        )
    }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    fun nextQuestion() {
        targetWord = activeQuizPool.random()
        options = generateQuizOptions(targetWord, activeQuizPool, quizMode == "BN_TO_EN")
        selectedOptionIndex = null
        isAnswered = false
        questionCount++
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        // Quiz Customization Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sources = listOf("All", "Top1000", "Spoken", "IELTS", "BCS")
            items(sources) { src ->
                val isSelected = selectedSource == src
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedSource = src
                        nextQuestion()
                    },
                    label = {
                        Text(
                            text = when (src) {
                                "All" -> if (isBn) "সকল শব্দ" else "All"
                                "Top1000" -> if (isBn) "টপ ১০০০" else "Top 1000"
                                "Spoken" -> if (isBn) "স্পোকেন" else "Spoken"
                                "IELTS" -> "IELTS"
                                "BCS" -> if (isBn) "বিসিএস ও জব" else "BCS & Job"
                                else -> src
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

        // Mode Selector: English to Bangla vs Bangla to English
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (quizMode == "EN_TO_BN") themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        quizMode = "EN_TO_BN"
                        nextQuestion()
                    }
            ) {
                Text(
                    text = if (isBn) "🇬🇧 ইংরেজি ➔ 🇧🇩 বাংলা" else "EN ➔ BN Meaning",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (quizMode == "EN_TO_BN") themeColors.onAccent else themeColors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (quizMode == "BN_TO_EN") themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        quizMode = "BN_TO_EN"
                        nextQuestion()
                    }
            ) {
                Text(
                    text = if (isBn) "🇧🇩 বাংলা ➔ 🇬🇧 ইংরেজি" else "BN ➔ EN Word",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (quizMode == "BN_TO_EN") themeColors.onAccent else themeColors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Score Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBn) "প্রশ্ন #${questionCount + 1}" else "Question #${questionCount + 1}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = themeColors.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "🔥 Streak: $streak",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFF9800),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themeColors.accent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isBn) "স্কোর: $score" else "Score: $score",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
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
                    text = if (quizMode == "EN_TO_BN") targetWord.word else targetWord.meaningBn,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = themeColors.onSurface,
                    textAlign = TextAlign.Center
                )

                if (quizMode == "EN_TO_BN" && targetWord.phonetic.isNotBlank()) {
                    Text(
                        text = targetWord.phonetic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (quizMode == "EN_TO_BN") {
                    IconButton(onClick = { onSpeak(targetWord.word) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = themeColors.accent
                        )
                    }
                }
            }
        }

        // Options
        val correctAnswer = if (quizMode == "EN_TO_BN") targetWord.meaningBn else targetWord.word
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEachIndexed { index, optionText ->
                val isCorrect = optionText == correctAnswer
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
                    border = BorderStroke(1.5.dp, borderColor),
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
                            text = optionText,
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

        Spacer(modifier = Modifier.height(14.dp))

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

fun generateQuizOptions(target: VocabWord, allWords: List<VocabWord>, isBanglaToEnglish: Boolean): List<String> {
    val correctAnswer = if (isBanglaToEnglish) target.word else target.meaningBn
    val wrongOptions = allWords
        .filter { it.id != target.id && (if (isBanglaToEnglish) it.word != target.word else it.meaningBn != target.meaningBn) }
        .map { if (isBanglaToEnglish) it.word else it.meaningBn }
        .distinct()
        .shuffled()
        .take(3)
    return (wrongOptions + correctAnswer).shuffled()
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
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadStatusText by remember { mutableStateOf("") }

    val masterPackId = "master_dictionary"
    val isInstalled = installedPacks.contains(masterPackId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // Hero Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = themeColors.accent.copy(alpha = 0.12f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = themeColors.accent,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isBn) "অফলাইন ডিকশনারি ডাটা ডাউনলোড" else "Offline Dictionary Download",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = themeColors.onSurface
                    )
                    Text(
                        text = if (isBn) "সম্পূর্ণ খাঁটি ইংলিশ-বাংলা ডাটাবেজ ডাউনলোড করে ১০০,০০০+ শব্দ অফলাইনে ব্যবহার করুন।" else "Download verified English-Bengali database with 100,000+ real words.",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Single Master Dictionary Download Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isInstalled) Color(0xFF4CAF50).copy(alpha = 0.15f) else themeColors.accent.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isInstalled) Icons.Default.CheckCircle else Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = if (isInstalled) Color(0xFF4CAF50) else themeColors.accent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "সম্পূর্ণ ১০৩,৬৫০+ ইংলিশ-বাংলা ডিকশনারি" else "Complete 103,650+ English-Bangla Dictionary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBn) "১০৩,৬৫০+ খাঁটি শব্দ • ৭.৮ MB • অফলাইন ডিকশনারি" else "103,650+ Real Words • 7.8 MB • Complete Database",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = themeColors.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isBn) "ইংরেজি শব্দ, বাংলা অর্থ, সঠিক উচ্চারণ, পদ প্রকরণ ও ক্যাটাগরিভিত্তিক শ্রেণীবিন্যাসসহ সম্পূর্ণ খাঁটি ডিকশনারি ডাটাবেজ। একবার ডাউলোডের পর আজীবনের জন্য অফলাইনে কাজ করবে।" else "Comprehensive English-Bengali dictionary containing authentic meanings, phonetics, parts of speech and categories. Works completely offline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.onSurface.copy(alpha = 0.75f)
                )

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = downloadStatusText.ifBlank { if (isBn) "ডাউনলোড ও ডাটাবেস ইনডেক্সিং হচ্ছে..." else "Downloading database..." },
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColors.accent
                            )
                            Text(
                                text = "${(downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = themeColors.accent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = themeColors.accent,
                            trackColor = themeColors.accent.copy(alpha = 0.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isInstalled) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    VocabularyPackRepository.deletePackFile(context, masterPackId)
                                    onUninstallPack(masterPackId)
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "ডাটা রিসেট / মুছুন" else "Remove Data", color = Color(0xFFF44336))
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "সক্রিয় রয়েছে" else "Installed & Active",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBn) "ডাউনলোড হচ্ছে..." else "Downloading...")
                        }
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isDownloading = true
                                    downloadProgress = 0.05f
                                    downloadStatusText = if (isBn) "ডাউনলোড প্রস্তুত করা হচ্ছে..." else "Preparing download..."

                                    val words = VocabularyPackRepository.downloadAndAssemblePack(
                                        context = context,
                                        packId = masterPackId,
                                        onProgress = { p, status ->
                                            downloadProgress = p
                                            downloadStatusText = status
                                        }
                                    )

                                    isDownloading = false
                                    if (words.isNotEmpty()) {
                                        onInstallPack(masterPackId)
                                        Toast.makeText(
                                            context,
                                            if (isBn) "ডিকশনারি ডাটাবেজ (${words.size} টি শব্দ) সফলভাবে ডাউনলোড ও সক্রিয় করা হয়েছে!" else "Downloaded ${words.size} words successfully!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            if (isBn) "ডাউনলোড ব্যর্থ হয়েছে, ইন্টারনেট সংযোগ পরীক্ষা করুন" else "Download failed, check connection",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = themeColors.onAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "ডাউনলোড ও সক্রিয় করুন (৭.৮ MB)" else "Download & Activate (7.8 MB)",
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
                    modifier = Modifier.size(56.dp),
                    tint = themeColors.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isBn) "কোনো সংরক্ষিত শব্দ নেই" else "No saved words yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = themeColors.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = if (isBn) "শব্দের পাশের হার্ট আইকনে ট্যাপ করে ফেভারিটে যোগ করুন" else "Tap heart icon on any word to bookmark",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.onSurface.copy(alpha = 0.5f)
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

// Data Provider with Pre-compiled & Real Downloaded Packs
object VocabularyDataProvider {
    fun getWordsForPacks(context: Context, installedPackIds: Set<String>): List<VocabWord> {
        val list = VocabularyDataPacks.starterWords.toMutableList()

        for (packId in installedPackIds) {
            val fileWords = VocabularyPackRepository.loadPackFromFileSync(context, packId)
            if (!fileWords.isNullOrEmpty()) {
                list.addAll(fileWords)
            }
        }

        return list.distinctBy { it.word.lowercase() }
    }
}

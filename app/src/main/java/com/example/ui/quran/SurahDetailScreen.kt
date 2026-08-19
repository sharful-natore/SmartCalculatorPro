package com.example.ui.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.quran.AyahEntity
import com.example.data.quran.SurahEntity
import com.example.ui.theme.CalculatorThemeColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    surah: SurahEntity,
    viewModel: QuranViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit,
    isBn: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ayahs by viewModel.currentAyahs.collectAsStateWithLifecycle()
    val isAyahsLoading by viewModel.isAyahsLoading.collectAsStateWithLifecycle()
    val isPlayerVisible by viewModel.isPlayerVisible.collectAsStateWithLifecycle()

    val currentSurahNum by viewModel.audioPlayer.currentSurahNumber.collectAsStateWithLifecycle()
    val currentAyahIndex by viewModel.audioPlayer.currentAyahIndex.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.audioPlayer.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.audioPlayer.durationMs.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.audioPlayer.playbackSpeed.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val ayahDownloadProgress by viewModel.ayahDownloadProgress.collectAsStateWithLifecycle()
    val downloadedAyahKeys by viewModel.downloadedAyahKeys.collectAsStateWithLifecycle()
    val downloadedSurahs by viewModel.downloadedSurahs.collectAsStateWithLifecycle()
    val isSurahDownloaded = surah.isAudioDownloaded || downloadedSurahs.any { it.number == surah.number }

    // Persistent User Preferences for Quran Reading
    val quranPrefs = remember { context.getSharedPreferences("quran_view_prefs", Context.MODE_PRIVATE) }
    var arabicFontSize by remember { mutableFloatStateOf(quranPrefs.getFloat("arabic_font_size", 25f)) }
    var banglaFontSize by remember { mutableFloatStateOf(quranPrefs.getFloat("bangla_font_size", 14.5f)) }
    var showPronunciation by remember { mutableStateOf(quranPrefs.getBoolean("show_pronunciation", true)) }
    var showTranslation by remember { mutableStateOf(quranPrefs.getBoolean("show_translation", true)) }
    var enableTajweed by remember { mutableStateOf(quranPrefs.getBoolean("enable_tajweed", true)) }

    // Bookmarked Ayahs
    val bookmarkPrefs = remember { context.getSharedPreferences("quran_bookmarks_prefs", Context.MODE_PRIVATE) }
    var bookmarkedAyahs by remember {
        mutableStateOf(bookmarkPrefs.getStringSet("bookmarked_ayah_keys", emptySet()) ?: emptySet())
    }

    // Modal Sheet & Dialog States
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }

    // Auto-collapsing TopBar on scroll for immersive reading
    var isTopBarVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                if (delta < -15f && isTopBarVisible && (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 80)) {
                    isTopBarVisible = false
                } else if (delta > 15f && !isTopBarVisible) {
                    isTopBarVisible = true
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 40) {
            isTopBarVisible = true
        }
    }

    // Palette
    val emeraldPrimary = Color(0xFF059669)
    val emeraldDark = Color(0xFF064E3B)
    val goldAmber = Color(0xFFD97706)
    val cyanPrimary = themeColors.buttonEqualBg

    // Intercept system back press to return to Surah List
    BackHandler {
        onBackClick()
    }

    // Load ayahs when screen opens
    LaunchedEffect(surah.number) {
        viewModel.selectSurah(surah)
    }

    // Inform audio player that we are on Surah Detail page
    DisposableEffect(Unit) {
        viewModel.audioPlayer.setIsDetailScreenOpen(true)
        onDispose {
            viewModel.audioPlayer.setIsDetailScreenOpen(false)
        }
    }

    // Auto-Scroll to currently playing Ayah
    LaunchedEffect(currentSurahNum, currentAyahIndex) {
        if (currentSurahNum == surah.number && currentAyahIndex in ayahs.indices) {
            // Index 0: Tajweed Guide, 1: Surah Banner, 2: Bismillah, so ayah index corresponds to item index + 3
            val offset = if (surah.number != 9) 3 else 2
            val scrollIndex = (currentAyahIndex + offset).coerceIn(0, ayahs.size + offset)
            scope.launch {
                listState.animateScrollToItem(scrollIndex)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        containerColor = themeColors.background,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AnimatedVisibility(
                visible = isTopBarVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = themeColors.cardBg,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (themeColors.isDark) {
                                        listOf(Color(0xFF0F172A), Color(0xFF064E3B))
                                    } else {
                                        listOf(Color(0xFF065F46), Color(0xFF047857))
                                    }
                                )
                            )
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${surah.number}. ${surah.nameBangla}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (surah.revelationType.equals("Meccan", ignoreCase = true)) "মাক্কী" else "মাদানী",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFEF08A),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${surah.nameEnglish} • ${surah.numberOfAyahs} টি আয়াত",
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1
                                )
                            }

                            // Jump to Ayah Icon Button
                            IconButton(onClick = { showJumpDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.PinDrop,
                                    contentDescription = "Jump to Ayah",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Text Settings Icon Button
                            IconButton(onClick = { showSettingsSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Reading Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Download Full Surah Icon Button
                            IconButton(
                                onClick = {
                                    viewModel.downloadSurahAudio(context, surah.number)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSurahDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                    contentDescription = if (isSurahDownloaded) "Downloaded Offline" else "Download Full Surah",
                                    tint = if (isSurahDownloaded) Color(0xFF6EE7B7) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (isPlayerVisible) {
                // Bottom Floating Audio Control Bar
                SurahAudioControlBar(
                    surah = surah,
                    currentAyahIndex = currentAyahIndex,
                    totalAyahs = ayahs.size,
                    isPlaying = isPlaying && currentSurahNum == surah.number,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    playbackSpeed = playbackSpeed,
                    cyanPrimary = cyanPrimary,
                    cyanDark = emeraldDark,
                    themeColors = themeColors,
                    onPlayPauseToggle = {
                        if (currentSurahNum == surah.number) {
                            viewModel.audioPlayer.togglePlayPause()
                        } else {
                            viewModel.playSurah(surah, 0)
                        }
                    },
                    onPrevious = { viewModel.audioPlayer.playPrevious() },
                    onNext = { viewModel.audioPlayer.playNext() },
                    onSeek = { viewModel.audioPlayer.seekTo(it) },
                    onSpeedChange = { viewModel.audioPlayer.setPlaybackSpeed(it) },
                    onClose = { viewModel.stopAndClosePlayer() }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(themeColors.displayBackground)
        ) {
            if (isAyahsLoading && ayahs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = emeraldPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "সূরা ${surah.nameBangla} লোড করা হচ্ছে...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    // 1. Tajweed Guide Legend Bar
                    item {
                        TajweedLegendBar(
                            themeColors = themeColors,
                            modifier = Modifier.padding(bottom = 10.dp),
                            initialExpanded = false,
                            isBn = isBn
                        )
                    }

                    // 2. Surah Hero Header Card
                    item {
                        SurahHeroBannerCard(
                            surah = surah,
                            isPlaying = isPlaying && currentSurahNum == surah.number,
                            themeColors = themeColors,
                            onPlayFullSurah = {
                                viewModel.playSurah(surah, 0)
                            },
                            onJumpToAyah = { showJumpDialog = true }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 3. Bismillah Header Card
                    item {
                        if (surah.number != 9) { // Surah At-Tawbah does not have Bismillah
                            BismillahOrnamentalCard(
                                themeColors = themeColors,
                                enableTajweed = enableTajweed
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // 4. Ayah Cards
                    itemsIndexed(
                        items = ayahs,
                        key = { _, ayah -> ayah.id }
                    ) { index, ayah ->
                        val isCurrentPlayingAyah = currentSurahNum == surah.number && currentAyahIndex == index
                        val ayahKey = "${surah.number}_${ayah.numberInSurah}"
                        val progress = ayahDownloadProgress[ayahKey]
                        val isAyahDownloaded = isSurahDownloaded || downloadedAyahKeys.contains(ayahKey)
                        val isBookmarked = bookmarkedAyahs.contains(ayahKey)

                        AyahCard(
                            ayah = ayah,
                            surah = surah,
                            index = index,
                            isCurrentPlaying = isCurrentPlayingAyah,
                            isAyahDownloaded = isAyahDownloaded,
                            isBookmarked = isBookmarked,
                            downloadProgress = progress,
                            arabicFontSize = arabicFontSize,
                            banglaFontSize = banglaFontSize,
                            showPronunciation = showPronunciation,
                            showTranslation = showTranslation,
                            enableTajweed = enableTajweed,
                            themeColors = themeColors,
                            onPlayAyah = { viewModel.playSurah(surah, index) },
                            onDownloadAyah = { viewModel.downloadAyahAudio(surah.number, ayah) },
                            onToggleBookmark = {
                                val newSet = if (isBookmarked) {
                                    bookmarkedAyahs - ayahKey
                                } else {
                                    bookmarkedAyahs + ayahKey
                                }
                                bookmarkedAyahs = newSet
                                bookmarkPrefs.edit().putStringSet("bookmarked_ayah_keys", newSet).apply()
                                val msg = if (!isBookmarked) "আয়াত ${ayah.numberInSurah} বুকমার্কে যুক্ত করা হয়েছে" else "বুকমার্ক সরানো হয়েছে"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onCopyAyah = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val formattedAyah = buildString {
                                    append(ayah.textArabic)
                                    append("\n\n")
                                    if (ayah.getBanglaPronunciation().isNotBlank()) {
                                        append("উচ্চারণ: ${ayah.getBanglaPronunciation()}\n\n")
                                    }
                                    append("অনুবাদ: ${ayah.textBangla}\n")
                                    append("— [সূরা ${surah.nameBangla} (${surah.nameEnglish}), আয়াত ${ayah.numberInSurah}]")
                                }
                                val clip = ClipData.newPlainText("Ayah ${surah.number}:${ayah.numberInSurah}", formattedAyah)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "আয়াত কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            onShareAyah = {
                                val formattedAyah = buildString {
                                    append(ayah.textArabic)
                                    append("\n\n")
                                    if (ayah.getBanglaPronunciation().isNotBlank()) {
                                        append("উচ্চারণ: ${ayah.getBanglaPronunciation()}\n\n")
                                    }
                                    append("অনুবাদ: ${ayah.textBangla}\n\n")
                                    append("— [সূরা ${surah.nameBangla} (${surah.nameEnglish}), আয়াত ${ayah.numberInSurah}]")
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, formattedAyah)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন আয়াত"))
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    // 5. Quran View & Font Customizer Bottom Sheet
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = emeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "কুরআন পড়ার কাস্টমাইজেশন ও সেটিংস",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Arabic Font Size Slider
                Text(
                    text = "আরবি হরফের সাইজ: ${arabicFontSize.toInt()} sp",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText
                )
                Slider(
                    value = arabicFontSize,
                    onValueChange = {
                        arabicFontSize = it
                        quranPrefs.edit().putFloat("arabic_font_size", it).apply()
                    },
                    valueRange = 18f..38f,
                    colors = SliderDefaults.colors(
                        thumbColor = emeraldPrimary,
                        activeTrackColor = emeraldPrimary
                    )
                )

                // Bangla Font Size Slider
                Text(
                    text = "বাংলা অনুবাদ ও উচ্চারণের সাইজ: ${banglaFontSize.toInt()} sp",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText
                )
                Slider(
                    value = banglaFontSize,
                    onValueChange = {
                        banglaFontSize = it
                        quranPrefs.edit().putFloat("bangla_font_size", it).apply()
                    },
                    valueRange = 12f..22f,
                    colors = SliderDefaults.colors(
                        thumbColor = emeraldPrimary,
                        activeTrackColor = emeraldPrimary
                    )
                )

                HorizontalDivider(
                    color = themeColors.displayText.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // Toggle Pronunciation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বাংলা উচ্চারণ দেখান",
                        fontSize = 14.sp,
                        color = themeColors.displayText
                    )
                    Switch(
                        checked = showPronunciation,
                        onCheckedChange = {
                            showPronunciation = it
                            quranPrefs.edit().putBoolean("show_pronunciation", it).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = emeraldPrimary
                        )
                    )
                }

                // Toggle Translation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বাংলা অনুবাদ ও অর্থ দেখান",
                        fontSize = 14.sp,
                        color = themeColors.displayText
                    )
                    Switch(
                        checked = showTranslation,
                        onCheckedChange = {
                            showTranslation = it
                            quranPrefs.edit().putBoolean("show_translation", it).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = emeraldPrimary
                        )
                    )
                }

                // Toggle Tajweed Coloring
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "তাজবীদ কালার কোডিং হাইলাইট",
                        fontSize = 14.sp,
                        color = themeColors.displayText
                    )
                    Switch(
                        checked = enableTajweed,
                        onCheckedChange = {
                            enableTajweed = it
                            quranPrefs.edit().putBoolean("enable_tajweed", it).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = emeraldPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showSettingsSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("সেভ করে বন্ধ করুন", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // 6. Jump to Ayah Dialog
    if (showJumpDialog) {
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PinDrop,
                        contentDescription = null,
                        tint = emeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সরাসরি আয়াতে যান (১ - ${surah.numberOfAyahs})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                }
            },
            text = {
                Box(modifier = Modifier.height(280.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 48.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(surah.numberOfAyahs) { i ->
                            val ayahNumber = i + 1
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (currentAyahIndex == i) emeraldPrimary else themeColors.displayBackground,
                                border = BorderStroke(1.dp, if (currentAyahIndex == i) emeraldPrimary else themeColors.displayText.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        showJumpDialog = false
                                        val offset = if (surah.number != 9) 3 else 2
                                        val target = (i + offset).coerceIn(0, ayahs.size + offset)
                                        scope.launch {
                                            listState.animateScrollToItem(target)
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$ayahNumber",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (currentAyahIndex == i) Color.White else themeColors.displayText
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text(text = "বন্ধ করুন", color = emeraldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = themeColors.cardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// --- SURAH HERO BANNER CARD ---
@Composable
fun SurahHeroBannerCard(
    surah: SurahEntity,
    isPlaying: Boolean,
    themeColors: CalculatorThemeColors,
    onPlayFullSurah: () -> Unit,
    onJumpToAyah: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (themeColors.isDark) {
                            listOf(Color(0xFF064E3B), Color(0xFF0F172A))
                        } else {
                            listOf(Color(0xFF047857), Color(0xFF065F46))
                        }
                    )
                )
                .border(
                    BorderStroke(1.dp, Color(0xFFFDE047).copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Arabic Calligraphy Surah Name
                Text(
                    text = "سُورَةُ ${surah.nameArabic}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFEF08A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bangla Surah Name & English Meaning
                Text(
                    text = "${surah.nameBangla} (${surah.nameEnglish})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                if (surah.nameTranslation.isNotBlank()) {
                    Text(
                        text = "অর্থঃ ${surah.nameTranslation}",
                        fontSize = 12.5.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Metadata Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Revelation Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = Color(0xFFFEF08A),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (surah.revelationType.equals("Meccan", ignoreCase = true)) "মাক্কী সূরা" else "মাদানী সূরা",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Total Ayahs Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF6EE7B7),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${surah.numberOfAyahs} টি আয়াত",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Play Full Surah & Jump
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPlayFullSurah,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEF08A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF064E3B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPlaying) "তেলাওয়াত চলছে" else "সম্পূর্ণ সূরা শুনুন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF064E3B)
                        )
                    }

                    OutlinedButton(
                        onClick = onJumpToAyah,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PinDrop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "আয়াতে যান",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- BISMILLAH ORNAMENTAL CARD ---
@Composable
fun BismillahOrnamentalCard(
    themeColors: CalculatorThemeColors,
    enableTajweed: Boolean
) {
    val emeraldColor = if (themeColors.isDark) Color(0xFF34D399) else Color(0xFF065F46)
    val bismillahArabic = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (themeColors.isDark) Color(0xFF0F172A) else Color(0xFFECFDF5)
        ),
        border = BorderStroke(1.dp, emeraldColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Arabic Calligraphy
            if (enableTajweed) {
                Text(
                    text = buildTajweedAnnotatedString(bismillahArabic, emeraldColor),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = bismillahArabic,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = emeraldColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bangla Translation
            Text(
                text = "পরম করুণাময়, অসীম দয়ালু আল্লাহর নামে শুরু করছি",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = themeColors.displayText.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- AYAH CARD ---
@Composable
fun AyahCard(
    ayah: AyahEntity,
    surah: SurahEntity,
    index: Int,
    isCurrentPlaying: Boolean,
    isAyahDownloaded: Boolean,
    isBookmarked: Boolean,
    downloadProgress: Int?,
    arabicFontSize: Float,
    banglaFontSize: Float,
    showPronunciation: Boolean,
    showTranslation: Boolean,
    enableTajweed: Boolean,
    themeColors: CalculatorThemeColors,
    onPlayAyah: () -> Unit,
    onDownloadAyah: () -> Unit,
    onToggleBookmark: () -> Unit,
    onCopyAyah: () -> Unit,
    onShareAyah: () -> Unit
) {
    val context = LocalContext.current
    val emeraldColor = if (themeColors.isDark) Color(0xFF34D399) else Color(0xFF047857)
    val amberColor = if (themeColors.isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
    val slateTextColor = if (themeColors.isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)

    val banglaPronunciation = remember(ayah) { ayah.getBanglaPronunciation() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isCurrentPlaying) 6.dp else 1.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlaying) {
                emeraldColor.copy(alpha = if (themeColors.isDark) 0.15f else 0.08f)
            } else {
                themeColors.cardBg
            }
        ),
        border = if (isCurrentPlaying) {
            BorderStroke(2.dp, emeraldColor)
        } else {
            BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Bar: Ayah Number Badge + Action Icon Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ornamental Ayah Number Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCurrentPlaying) emeraldColor else emeraldColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, emeraldColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${surah.number}:${ayah.numberInSurah}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentPlaying) Color.White else emeraldColor
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Actions: Bookmark, Download, Play, Copy, Share
                // 1. Bookmark Button
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) amberColor else themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(19.dp)
                    )
                }

                // 2. Download / Status Button
                when {
                    downloadProgress != null && downloadProgress in 1..99 -> {
                        CircularProgressIndicator(
                            progress = { downloadProgress / 100f },
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(18.dp),
                            strokeWidth = 2.dp,
                            color = emeraldColor
                        )
                    }
                    isAyahDownloaded -> {
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "আয়াতটি অফলাইনে সংরক্ষিত রয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Downloaded",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    else -> {
                        IconButton(
                            onClick = onDownloadAyah,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Ayah Audio",
                                tint = themeColors.displayText.copy(alpha = 0.6f),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                // 3. Play/Pause Ayah Audio
                IconButton(
                    onClick = onPlayAyah,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentPlaying) emeraldColor else Color.Transparent)
                ) {
                    Icon(
                        imageVector = if (isCurrentPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play Verse",
                        tint = if (isCurrentPlaying) Color.White else emeraldColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 4. Copy Ayah Button
                IconButton(
                    onClick = onCopyAyah,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Verse",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 5. Share Ayah Button
                IconButton(
                    onClick = onShareAyah,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Verse",
                        tint = themeColors.displayText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Arabic Ayah Text
            val formattedArabic = if (enableTajweed) {
                buildTajweedAnnotatedString(ayah.textArabic, emeraldColor)
            } else {
                androidx.compose.ui.text.AnnotatedString(ayah.textArabic)
            }

            Text(
                text = formattedArabic,
                fontSize = arabicFontSize.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                lineHeight = (arabicFontSize * 1.8f).sp,
                color = emeraldColor,
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Bangla Pronunciation (উচ্চারণ)
            if (showPronunciation && banglaPronunciation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = amberColor.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, amberColor.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = amberColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "উচ্চারণ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = amberColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = banglaPronunciation,
                            fontSize = banglaFontSize.sp,
                            fontWeight = FontWeight.Medium,
                            color = amberColor,
                            lineHeight = (banglaFontSize * 1.45f).sp
                        )
                    }
                }
            }

            // 3. Bangla Translation (অনুবাদ ও অর্থ)
            if (showTranslation && ayah.textBangla.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = slateTextColor.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, slateTextColor.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = emeraldColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "অনুবাদ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = emeraldColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ayah.textBangla,
                            fontSize = banglaFontSize.sp,
                            fontWeight = FontWeight.Normal,
                            color = slateTextColor,
                            lineHeight = (banglaFontSize * 1.5f).sp
                        )
                    }
                }
            }
        }
    }
}

// --- AUDIO PLAYER BAR ---
@Composable
fun SurahAudioControlBar(
    surah: SurahEntity,
    currentAyahIndex: Int,
    totalAyahs: Int,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    playbackSpeed: Float,
    cyanPrimary: Color,
    cyanDark: Color,
    themeColors: CalculatorThemeColors,
    onPlayPauseToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onClose: () -> Unit
) {
    var showSpeedMenu by remember { mutableStateOf(false) }
    val emeraldPrimary = Color(0xFF059669)

    Surface(
        color = themeColors.cardBg,
        shadowElevation = 10.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Track Info Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = emeraldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${surah.nameBangla} (${surah.nameArabic})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "আয়াত ${currentAyahIndex + 1} / $totalAyahs",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = emeraldPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Player",
                        tint = themeColors.displayText.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Progress Seekbar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatMs(currentPositionMs),
                    fontSize = 10.5.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )
                Slider(
                    value = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f,
                    onValueChange = { percent ->
                        onSeek((percent * durationMs).toLong())
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = emeraldPrimary,
                        activeTrackColor = emeraldPrimary,
                        inactiveTrackColor = emeraldPrimary.copy(alpha = 0.2f)
                    )
                )
                Text(
                    text = formatMs(durationMs),
                    fontSize = 10.5.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f)
                )
            }

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Speed Button
                Box {
                    TextButton(onClick = { showSpeedMenu = true }) {
                        Text(
                            text = "${playbackSpeed}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = emeraldPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showSpeedMenu,
                        onDismissRequest = { showSpeedMenu = false }
                    ) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${speed}x Speed") },
                                onClick = {
                                    onSpeedChange(speed)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(onClick = onPrevious) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = themeColors.displayText,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = onPlayPauseToggle,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(emeraldPrimary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = themeColors.displayText,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

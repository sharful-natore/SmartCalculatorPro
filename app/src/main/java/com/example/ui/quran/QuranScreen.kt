package com.example.ui.quran

import android.content.Context
import android.text.format.Formatter
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.quran.SurahEntity
import com.example.ui.theme.CalculatorThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit,
    onSurahClick: (SurahEntity) -> Unit,
    isBn: Boolean = true
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val surahs by viewModel.surahs.collectAsStateWithLifecycle()
    val currentPlayingSurah by viewModel.audioPlayer.currentSurahNumber.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsStateWithLifecycle()

    val aiDialogVisible by viewModel.aiDialogVisible.collectAsStateWithLifecycle()
    val storageDialogVisible by viewModel.storageDialogVisible.collectAsStateWithLifecycle()
    val downloadConfirmSurah by viewModel.downloadConfirmSurah.collectAsStateWithLifecycle()
    val showDownloadAllConfirmDialog by viewModel.showDownloadAllConfirmDialog.collectAsStateWithLifecycle()

    // Dynamic theme-derived palette matching main app theme
    val cyanPrimary = themeColors.buttonEqualBg
    val cyanLight = if (themeColors.isDark) themeColors.buttonNormalBg else themeColors.buttonEqualBg.copy(alpha = 0.12f)
    val cyanDark = if (themeColors.isDark) themeColors.background else themeColors.buttonEqualBg.copy(alpha = 0.85f)
    val cyanAccent = themeColors.buttonEqualBg

    // Auto-collapsing TopBar on scroll
    val listState = rememberLazyListState()
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
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    // Minimal & Theme-Matching Slim Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = themeColors.displayText
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "পবিত্র আল-কুরআন",
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.displayText
                            )
                            Text(
                                text = "Holy Quran (Audio & Translation)",
                                fontSize = 11.sp,
                                color = cyanAccent
                            )
                        }

                        var showBookmarksSheet by remember { mutableStateOf(false) }

                        IconButton(onClick = { showBookmarksSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Bookmarked Ayahs",
                                tint = Color(0xFFD97706)
                            )
                        }

                        if (showBookmarksSheet) {
                            val bookmarkPrefs = remember { context.getSharedPreferences("quran_bookmarks_prefs", Context.MODE_PRIVATE) }
                            var bookmarkedSet by remember {
                                mutableStateOf(bookmarkPrefs.getStringSet("bookmarked_ayah_keys", emptySet()) ?: emptySet())
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
                                            text = if (isBn) "বুকমার্ককৃত আয়াতসমূহ" else "Bookmarked Ayahs",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.5.sp,
                                            color = themeColors.displayText
                                        )
                                    }
                                },
                                text = {
                                    if (bookmarkedSet.isEmpty()) {
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
                                                text = if (isBn) "কোনো বুকমার্ককৃত আয়াত নেই।" else "No bookmarked ayahs found.",
                                                fontSize = 13.5.sp,
                                                color = themeColors.displayText.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (isBn) "সুরা পাঠকালে যেকোনো আয়াতের বুকমার্ক আইকনে চাপ দিলে এখানে সংরক্ষিত হবে।" else "Tap the bookmark icon on any Ayah while reading to save it here.",
                                                fontSize = 11.5.sp,
                                                color = themeColors.displayText.copy(alpha = 0.5f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 350.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            val keysList = bookmarkedSet.toList()
                                            items(keysList) { key ->
                                                val parts = key.split("_")
                                                val surahNum = parts.getOrNull(0)?.toIntOrNull() ?: 1
                                                val ayahNum = parts.getOrNull(1)?.toIntOrNull() ?: 1
                                                val surahObj = surahs.find { it.number == surahNum }

                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = themeColors.displayBackground,
                                                    border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.12f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "${surahObj?.nameBangla ?: "সূরা $surahNum"} • আয়াত $ayahNum",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.5.sp,
                                                                color = cyanPrimary
                                                            )
                                                            Text(
                                                                text = "${surahObj?.nameEnglish ?: "Surah $surahNum"} (Verse $ayahNum)",
                                                                fontSize = 11.sp,
                                                                color = themeColors.displayText.copy(alpha = 0.6f)
                                                            )
                                                        }

                                                        Row {
                                                            if (surahObj != null) {
                                                                IconButton(
                                                                    onClick = {
                                                                        showBookmarksSheet = false
                                                                        onSurahClick(surahObj)
                                                                    }
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.MenuBook,
                                                                        contentDescription = "Read",
                                                                        tint = cyanPrimary,
                                                                        modifier = Modifier.size(20.dp)
                                                                    )
                                                                }
                                                            }

                                                            IconButton(
                                                                onClick = {
                                                                    val updated = bookmarkedSet - key
                                                                    bookmarkedSet = updated
                                                                    bookmarkPrefs.edit().putStringSet("bookmarked_ayah_keys", updated).apply()
                                                                }
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Delete,
                                                                    contentDescription = "Delete",
                                                                    tint = Color(0xFFEF4444),
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showBookmarksSheet = false }) {
                                        Text(if (isBn) "বন্ধ করুন" else "Close", color = cyanPrimary, fontWeight = FontWeight.Bold)
                                    }
                                },
                                containerColor = themeColors.cardBg
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(themeColors.displayBackground)
        ) {
            if (surahs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = cyanPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "সূরা প্রস্তুত করা হচ্ছে...",
                            fontSize = 14.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                val isQuickShortcut = run {
                    var ctx = context
                    var found = false
                    while (ctx is android.content.ContextWrapper) {
                        if (ctx is android.app.Activity) {
                            val name = ctx::class.java.simpleName
                            if (name.contains("Quick") || name.contains("Shortcut")) {
                                found = true
                            }
                            break
                        }
                        ctx = ctx.baseContext
                    }
                    found || context::class.java.simpleName.contains("Quick") || context::class.java.simpleName.contains("Shortcut")
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = if (isQuickShortcut) 16.dp else 80.dp, top = 8.dp)
                ) {
                    // 1. Search Bar (Scrolls away smoothly)
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            placeholder = {
                                Text(
                                    text = "সূরা খুঁজুন (নাম বা নম্বর)...",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = themeColors.displayText.copy(alpha = 0.5f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = cyanPrimary
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = themeColors.displayText
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.displayText,
                                unfocusedTextColor = themeColors.displayText,
                                focusedBorderColor = cyanPrimary,
                                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                                focusedContainerColor = themeColors.cardBg,
                                unfocusedContainerColor = themeColors.cardBg
                            )
                        )
                    }

                    // 2. AI Assistant & Storage Action Buttons (Scrolls away smoothly)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                onClick = { viewModel.openAiAssistant() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = cyanPrimary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, cyanPrimary.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 9.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = cyanPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "এআই অ্যাসিস্ট্যান্ট",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cyanPrimary
                                    )
                                }
                            }

                            Surface(
                                onClick = { viewModel.openStorageManager(context) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = cyanPrimary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, cyanPrimary.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 9.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SdCard,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = cyanPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "অফলাইন স্টোরেজ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cyanPrimary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            onClick = { viewModel.downloadAllQuranAudio(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = cyanPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, cyanPrimary.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = cyanPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সম্পূর্ণ কুরআন অডিও একসাথে ডাউনলোড করুন (১১৪ সূরা)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cyanPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    // Active Downloading Banner (shows when any download is in progress)
                    val activeDownloadingSurahs = surahs.filter { it.downloadProgress in 1..99 }
                    if (activeDownloadingSurahs.isNotEmpty()) {
                        item {
                            val firstDownloading = activeDownloadingSurahs.first()
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cyanPrimary.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, cyanPrimary.copy(alpha = 0.35f))
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
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = cyanPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (activeDownloadingSurahs.size > 1) {
                                                    "ডাউনলোড হচ্ছে: সূরা ${firstDownloading.nameBangla} সহ ${activeDownloadingSurahs.size}টি সূরা (${firstDownloading.downloadProgress}%)"
                                                } else {
                                                    "ডাউনলোড হচ্ছে: সূরা ${firstDownloading.nameBangla} (${firstDownloading.downloadProgress}%)"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = cyanPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            onClick = { viewModel.cancelAllDownloads(context) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Cancel,
                                                    contentDescription = "Cancel",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "বাতিল",
                                                    color = Color(0xFFEF4444),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { firstDownloading.downloadProgress / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = cyanPrimary,
                                        trackColor = cyanPrimary.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }

                    items(
                        items = surahs,
                        key = { it.number }
                    ) { surah ->
                        SurahListItemCard(
                            surah = surah,
                            isPlaying = isPlaying && currentPlayingSurah == surah.number,
                            cyanPrimary = cyanPrimary,
                            cyanDark = cyanDark,
                            themeColors = themeColors,
                            onCardClick = { onSurahClick(surah) },
                            onPlayClick = { viewModel.playSurah(surah) },
                            onDownloadClick = { viewModel.requestDownloadSurah(surah) },
                            onCancelDownloadClick = { viewModel.cancelSurahDownload(context, surah.number) },
                            onDownloadCompleteClick = { viewModel.openStorageManager(context) }
                        )
                    }
                }
            }
        }
    }

    // Surah Download Confirmation Dialog
    downloadConfirmSurah?.let { surah ->
        SurahDownloadConfirmDialog(
            surah = surah,
            viewModel = viewModel,
            themeColors = themeColors,
            cyanPrimary = cyanPrimary,
            onConfirm = { viewModel.downloadSurahAudio(context, surah.number) },
            onDismiss = { viewModel.dismissDownloadSurahConfirm() }
        )
    }

    // Full Quran Download Confirmation Dialog
    if (showDownloadAllConfirmDialog) {
        FullQuranDownloadConfirmDialog(
            themeColors = themeColors,
            cyanPrimary = cyanPrimary,
            onConfirm = { viewModel.downloadAllQuranAudio(context) },
            onDismiss = { viewModel.dismissDownloadAllConfirm() }
        )
    }

    // Storage Manager Dialog
    if (storageDialogVisible) {
        StorageManagerDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            cyanPrimary = cyanPrimary,
            onDismiss = { viewModel.closeStorageManager() }
        )
    }

    // AI Quran Assistant Dialog
    if (aiDialogVisible) {
        AiQuranAssistantDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            cyanPrimary = cyanPrimary,
            cyanDark = cyanDark,
            onDismiss = { viewModel.closeAiAssistant() }
        )
    }
}

@Composable
fun SurahListItemCard(
    surah: SurahEntity,
    isPlaying: Boolean,
    cyanPrimary: Color,
    cyanDark: Color,
    themeColors: CalculatorThemeColors,
    onCardClick: () -> Unit,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onCancelDownloadClick: () -> Unit,
    onDownloadCompleteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = themeColors.cardBg
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) cyanPrimary else cyanPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${surah.number}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) Color.White else cyanPrimary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Surah Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.nameBangla,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.displayText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "(${surah.nameEnglish})",
                    fontSize = 11.5.sp,
                    color = themeColors.displayText.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val revTypeBn = if (surah.revelationType.lowercase().contains("meccan")) "মাক্কী" else "মাদানী"
                    Text(
                        text = "$revTypeBn • ${surah.numberOfAyahs} টি আয়াত",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                }

                if (surah.downloadProgress in 1..99) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { surah.downloadProgress / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = cyanPrimary,
                            trackColor = cyanPrimary.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "${surah.downloadProgress}%",
                            fontSize = 10.sp,
                            color = cyanPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Arabic Calligraphy Name & Action Buttons
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = buildTajweedAnnotatedString(surah.nameArabic, cyanDark),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Play Button
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Play Audio",
                            tint = cyanPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Download or Download Complete or Cancel Download Button
                    if (surah.isAudioDownloaded) {
                        // Downloaded Complete Icon Button -> Click opens downloaded list dialog
                        IconButton(
                            onClick = onDownloadCompleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DownloadDone,
                                contentDescription = "Downloaded List",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else if (surah.downloadProgress in 1..99) {
                        IconButton(
                            onClick = onCancelDownloadClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Cancel Download",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Audio",
                                tint = themeColors.displayText.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StorageManagerDialog(
    viewModel: QuranViewModel,
    themeColors: CalculatorThemeColors,
    cyanPrimary: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val downloadedSurahs by viewModel.downloadedSurahs.collectAsStateWithLifecycle()
    val storageBytes by viewModel.storageSizeBytes.collectAsStateWithLifecycle()

    var selectedSurahNumbers by remember { mutableStateOf(setOf<Int>()) }

    val formattedSize = Formatter.formatShortFileSize(context, storageBytes)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeColors.cardBg,
        titleContentColor = themeColors.displayText,
        textContentColor = themeColors.displayText,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SdCard, contentDescription = null, tint = cyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("অফলাইন স্টোরেজ ম্যানেজার", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cyanPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "মোট ডাউনলোডকৃত অডিও সাইজ:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = themeColors.displayText
                            )
                            Text(
                                text = formattedSize,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = cyanPrimary
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (downloadedSurahs.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.deleteAllSurahAudio(context)
                                    selectedSurahNumbers = emptySet()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("সব ডিলিট", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (downloadedSurahs.isEmpty()) {
                    Text(
                        text = "বর্তমানে কোনো অফলাইন অডিও ডিরেক্টরি ডাউনলোড করা নেই।",
                        fontSize = 12.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ডাউনলোডকৃত সূরাসমূহ (${downloadedSurahs.size} টি):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (selectedSurahNumbers.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.deleteSelectedSurahAudios(context, selectedSurahNumbers.toList())
                                    selectedSurahNumbers = emptySet()
                                }
                            ) {
                                Text("নির্বাচিত ডিলিট (${selectedSurahNumbers.size})", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "লং প্রেস করে সিলেক্ট করুন",
                                fontSize = 11.sp,
                                color = themeColors.displayText.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        downloadedSurahs.forEach { surah ->
                            val isSelected = selectedSurahNumbers.contains(surah.number)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) cyanPrimary.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .pointerInput(surah.number) {
                                        detectTapGestures(
                                            onTap = {
                                                if (selectedSurahNumbers.isNotEmpty()) {
                                                    if (isSelected) selectedSurahNumbers -= surah.number
                                                    else selectedSurahNumbers += surah.number
                                                } else {
                                                    viewModel.playSurah(surah)
                                                    onDismiss()
                                                }
                                            },
                                            onLongPress = {
                                                if (isSelected) selectedSurahNumbers -= surah.number
                                                else selectedSurahNumbers += surah.number
                                            }
                                        )
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedSurahNumbers += surah.number
                                        else selectedSurahNumbers -= surah.number
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${surah.number}. ${surah.nameBangla} (${surah.nameEnglish})",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = themeColors.displayText
                                    )
                                    Text(
                                        text = "${surah.numberOfAyahs} টি আয়াত • অফলাইন প্রস্তুত",
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.playSurah(surah)
                                        onDismiss()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Play",
                                        tint = cyanPrimary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteSurahAudio(context, surah.number) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            HorizontalDivider(color = themeColors.displayText.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = cyanPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AiQuranAssistantDialog(
    viewModel: QuranViewModel,
    themeColors: CalculatorThemeColors,
    cyanPrimary: Color,
    cyanDark: Color,
    onDismiss: () -> Unit
) {
    val aiQuestion by viewModel.aiQuestion.collectAsStateWithLifecycle()
    val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeColors.cardBg,
        titleContentColor = themeColors.displayText,
        textContentColor = themeColors.displayText,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = cyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("এআই কুরআন অ্যাসিস্ট্যান্ট", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.displayText)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                Text(
                    text = "কুরআনের যেকোনো বিষয়বস্তু, আয়াত, ধৈর্য বা রিযিক সংক্রান্ত প্রশ্ন জিজ্ঞাসা করুন:",
                    fontSize = 12.sp,
                    color = themeColors.displayText.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestion chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val chips = listOf("ধৈর্য ও সালাত", "রিজিক ও বরকত", "পিতা-মাতা")
                    chips.forEach { chipText ->
                        SuggestionChip(
                            onClick = {
                                viewModel.setAiQuestion(chipText)
                                viewModel.askQuranAi(chipText)
                            },
                            label = { Text(chipText, fontSize = 10.5.sp, color = themeColors.displayText) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = themeColors.displayBackground,
                                labelColor = themeColors.displayText
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Question Input
                OutlinedTextField(
                    value = aiQuestion,
                    onValueChange = { viewModel.setAiQuestion(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("যেমন: কুরআনে ধৈর্য নিয়ে কি বলা হয়েছে?", fontSize = 12.sp, color = themeColors.displayText.copy(alpha = 0.5f)) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.askQuranAi(aiQuestion)
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = cyanPrimary)
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        viewModel.askQuranAi(aiQuestion)
                    }),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themeColors.displayText,
                        unfocusedTextColor = themeColors.displayText,
                        focusedBorderColor = cyanPrimary,
                        unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                        focusedContainerColor = themeColors.displayBackground,
                        unfocusedContainerColor = themeColors.displayBackground
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Response Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(cyanPrimary.copy(alpha = 0.06f))
                        .padding(12.dp)
                ) {
                    if (isAiLoading) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = cyanPrimary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কুরআনের তফসির ও তথ্য খোঁজা হচ্ছে...",
                                fontSize = 12.sp,
                                color = themeColors.displayText.copy(alpha = 0.7f)
                            )
                        }
                    } else if (!aiResponse.isNullOrBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = aiResponse ?: "",
                                fontSize = 13.sp,
                                color = themeColors.displayText,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        Text(
                            text = "উপরে আপনার প্রশ্ন টাইপ করে পাঠান বা সাজেস্টেড বাটন ট্যাপ করুন।",
                            fontSize = 12.sp,
                            color = themeColors.displayText.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = cyanPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

package com.example.ui.quran

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onSurahClick: (SurahEntity) -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val surahs by viewModel.surahs.collectAsStateWithLifecycle()
    val currentPlayingSurah by viewModel.audioPlayer.currentSurahNumber.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsStateWithLifecycle()

    val aiDialogVisible by viewModel.aiDialogVisible.collectAsStateWithLifecycle()
    val storageDialogVisible by viewModel.storageDialogVisible.collectAsStateWithLifecycle()

    // Cyan/Teal Palette for Quran Module
    val cyanPrimary = Color(0xFF00838F)
    val cyanLight = Color(0xFFE0F7FA)
    val cyanDark = Color(0xFF004D40)
    val cyanAccent = Color(0xFF00ACC1)

    Scaffold(
        topBar = {
            Surface(
                color = themeColors.cardBg,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    // Header Banner with Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(cyanDark, cyanPrimary)
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 14.dp)
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
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "পবিত্র আল-কুরআন",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Holy Quran (Audio, Translation & AI)",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            // Quick Floating AI Button in Top Bar
                            IconButton(
                                onClick = { viewModel.openAiAssistant() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Assistant",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = {
                            Text(
                                text = "সূরা খুঁজুন (নাম বা নম্বর, যেমন: ১ বা ফাতিহা)...",
                                fontSize = 13.sp,
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
                            focusedBorderColor = cyanPrimary,
                            unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.2f),
                            focusedContainerColor = themeColors.cardBg,
                            unfocusedContainerColor = themeColors.cardBg
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Chips (AI Assistant & Storage Manager)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.openAiAssistant() },
                            label = {
                                Text("✨ এআই কুরআন অ্যাসিস্ট্যান্ট", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp), tint = cyanPrimary)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = cyanLight,
                                labelColor = cyanDark
                            )
                        )

                        FilterChip(
                            selected = false,
                            onClick = { viewModel.openStorageManager(context) },
                            label = {
                                Text("💾 অফলাইন স্টোরেজ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SdCard, contentDescription = null, modifier = Modifier.size(16.dp), tint = cyanPrimary)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = cyanLight,
                                labelColor = cyanDark
                            )
                        )
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                ) {
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
                            onDownloadClick = { viewModel.downloadSurahAudio(context, surah.number) }
                        )
                    }
                }
            }
        }
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
    onDownloadClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
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
                .padding(12.dp),
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

            Spacer(modifier = Modifier.width(12.dp))

            // Surah Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = surah.nameBangla,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${surah.nameEnglish})",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val revTypeBn = if (surah.revelationType.lowercase().contains("meccan")) "মাক্কী" else "মাদানী"
                    Text(
                        text = "$revTypeBn • ${surah.numberOfAyahs} টি আয়াত",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Download Status Badge
                if (surah.isAudioDownloaded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Downloaded",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "অডিও সংগৃহীত (অফলাইন)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                } else if (surah.downloadProgress in 1..99) {
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
                    text = surah.nameArabic,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = cyanDark
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

                    // Download Button
                    if (!surah.isAudioDownloaded && surah.downloadProgress == 0) {
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

    val formattedSize = Formatter.formatShortFileSize(context, storageBytes)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SdCard, contentDescription = null, tint = cyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("অফলাইন স্টোরেজ ম্যানেজার", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        Text(
                            text = "মোট ডাউনলোডকৃত অডিও সাইজ:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.displayText
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = formattedSize,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = cyanPrimary
                        )
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
                    Text(
                        text = "ডাউনলোডকৃত সূরাসমূহ (${downloadedSurahs.size} টি):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.displayText
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        downloadedSurahs.forEach { surah ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${surah.number}. ${surah.nameBangla} (${surah.nameEnglish})",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = themeColors.displayText
                                    )
                                    Text(
                                        text = "${surah.numberOfAyahs} টি আয়াত",
                                        fontSize = 11.sp,
                                        color = themeColors.displayText.copy(alpha = 0.6f)
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = cyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("এআই কুরআন অ্যাসিস্ট্যান্ট", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                            label = { Text(chipText, fontSize = 10.5.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Question Input
                OutlinedTextField(
                    value = aiQuestion,
                    onValueChange = { viewModel.setAiQuestion(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("যেমন: কুরআনে ধৈর্য নিয়ে কি বলা হয়েছে?", fontSize = 12.sp) },
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
                    shape = RoundedCornerShape(10.dp)
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

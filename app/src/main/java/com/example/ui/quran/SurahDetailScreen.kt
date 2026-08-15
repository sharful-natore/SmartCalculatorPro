package com.example.ui.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ayahs by viewModel.currentAyahs.collectAsStateWithLifecycle()
    val isAyahsLoading by viewModel.isAyahsLoading.collectAsStateWithLifecycle()
    val isWordByWord by viewModel.isWordByWord.collectAsStateWithLifecycle()

    val currentSurahNum by viewModel.audioPlayer.currentSurahNumber.collectAsStateWithLifecycle()
    val currentAyahIndex by viewModel.audioPlayer.currentAyahIndex.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.audioPlayer.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.audioPlayer.durationMs.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.audioPlayer.playbackSpeed.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    val cyanPrimary = Color(0xFF00838F)
    val cyanLight = Color(0xFFE0F7FA)
    val cyanDark = Color(0xFF004D40)

    // Load ayahs when screen opens
    LaunchedEffect(surah.number) {
        viewModel.selectSurah(surah)
    }

    // Auto-Scroll to currently playing Ayah
    LaunchedEffect(currentSurahNum, currentAyahIndex) {
        if (currentSurahNum == surah.number && currentAyahIndex in ayahs.indices) {
            // Index 0 is Bismillah header, so ayah index corresponds to item index + 1
            val scrollIndex = (currentAyahIndex + 1).coerceIn(0, ayahs.size)
            scope.launch {
                listState.animateScrollToItem(scrollIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = themeColors.cardBg,
                shadowElevation = 3.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(colors = listOf(cyanDark, cyanPrimary)))
                        .padding(horizontal = 12.dp, vertical = 12.dp)
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
                                text = "${surah.number}. ${surah.nameBangla}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${surah.nameEnglish} • ${surah.numberOfAyahs} টি আয়াত",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Text(
                            text = surah.nameArabic,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        bottomBar = {
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
                cyanDark = cyanDark,
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
                onSpeedChange = { viewModel.audioPlayer.setPlaybackSpeed(it) }
            )
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
                        CircularProgressIndicator(color = cyanPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "সূরা ${surah.nameBangla} লোড করা হচ্ছে...",
                            fontSize = 14.sp,
                            color = themeColors.displayText.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    // Header Item: Bismillah
                    item {
                        if (surah.number != 9) { // Bismillah is not present in Surah At-Tawbah
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = cyanLight
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cyanDark,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "পরম করুণাময় অসীম দয়ালু আল্লাহর নামে শুরু করছি",
                                        fontSize = 13.sp,
                                        color = cyanDark.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Ayah Items
                    itemsIndexed(
                        items = ayahs,
                        key = { _, ayah -> ayah.id }
                    ) { index, ayah ->
                        val isCurrentPlayingAyah = currentSurahNum == surah.number && currentAyahIndex == index

                        AyahCard(
                            ayah = ayah,
                            index = index,
                            isCurrentPlaying = isCurrentPlayingAyah,
                            isWordByWord = isWordByWord,
                            cyanPrimary = cyanPrimary,
                            cyanDark = cyanDark,
                            themeColors = themeColors,
                            onPlayAyah = { viewModel.playSurah(surah, index) },
                            onCopyAyah = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(
                                    "Ayah",
                                    "${ayah.textArabic}\n\n${ayah.textBangla}\n(সূরা ${surah.nameBangla}, আয়াত ${ayah.numberInSurah})"
                                )
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "আয়াত কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AyahCard(
    ayah: AyahEntity,
    index: Int,
    isCurrentPlaying: Boolean,
    isWordByWord: Boolean,
    cyanPrimary: Color,
    cyanDark: Color,
    themeColors: CalculatorThemeColors,
    onPlayAyah: () -> Unit,
    onCopyAyah: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlaying) cyanPrimary.copy(alpha = 0.08f) else themeColors.cardBg
        ),
        border = if (isCurrentPlaying) BorderStroke(2.dp, cyanPrimary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentPlaying) 4.dp else 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Ayah Top Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ayah Number Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(cyanPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${ayah.numberInSurah}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = cyanPrimary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Play Verse Icon Button
                IconButton(
                    onClick = onPlayAyah,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isCurrentPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                        contentDescription = "Play Verse",
                        tint = cyanPrimary
                    )
                }

                // Copy Verse Icon Button
                IconButton(
                    onClick = onCopyAyah,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Verse",
                        tint = themeColors.displayText.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Text
            Text(
                text = ayah.textArabic,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = cyanDark,
                textAlign = TextAlign.End,
                lineHeight = 40.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bangla Translation
            Text(
                text = ayah.textBangla,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = themeColors.displayText,
                lineHeight = 22.sp
            )
        }
    }
}

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
    onSpeedChange: (Float) -> Unit
) {
    var showSpeedMenu by remember { mutableStateOf(false) }

    Surface(
        color = themeColors.cardBg,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
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
                    tint = cyanPrimary,
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
                    color = cyanPrimary
                )
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
                        thumbColor = cyanPrimary,
                        activeTrackColor = cyanPrimary,
                        inactiveTrackColor = cyanPrimary.copy(alpha = 0.2f)
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
                            color = cyanPrimary
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
                        .background(cyanPrimary)
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

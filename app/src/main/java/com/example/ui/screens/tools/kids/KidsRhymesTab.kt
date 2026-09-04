package com.example.ui.screens.tools.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KidsRhymesTab(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    showEnglish: Boolean = false,
    onShowEnglishChange: (Boolean) -> Unit = {},
    onRewardStars: (Int) -> Unit
) {
    val rhymes = KidsDataProvider.rhymesList
    var selectedRhymeIndex by remember { mutableStateOf(0) }
    val currentRhyme = rhymes[selectedRhymeIndex]

    var isPlaying by remember { mutableStateOf(false) }
    var activeLineIndex by remember { mutableStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()
    var playbackJob by remember { mutableStateOf<Job?>(null) }

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        isPlaying = false
        activeLineIndex = -1
        audioPlayer.stop()
    }

    fun startPlayback() {
        stopPlayback()
        isPlaying = true
        playbackJob = coroutineScope.launch {
            val lines = if (showEnglish) currentRhyme.linesEn else currentRhyme.linesBn
            val isBn = !showEnglish

            for (i in lines.indices) {
                if (!isPlaying) break
                activeLineIndex = i
                val line = lines[i]
                audioPlayer.speak(line, isBn = isBn)
                delay(2600) // comfortable reading interval
            }
            activeLineIndex = -1
            isPlaying = false
            onRewardStars(5)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top Language Category Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val langOptions = listOf(
                false to "🎶 বাংলা ছড়া ও গান",
                true to "🇬🇧 English Rhymes"
            )
            langOptions.forEach { (isEn, label) ->
                val isSelected = showEnglish == isEn
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) themeColors.accent else themeColors.surface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            stopPlayback()
                            audioPlayer.playClickSound()
                            onShowEnglishChange(isEn)
                        }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp
                        ),
                        color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
        // Rhyme Selector Carousel
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rhymes.indices.toList()) { index ->
                val rhyme = rhymes[index]
                val isSelected = selectedRhymeIndex == index

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) themeColors.accent else themeColors.surface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.15f)) else null,
                    modifier = Modifier
                        .height(44.dp)
                        .clickable {
                            if (selectedRhymeIndex != index) {
                                stopPlayback()
                                audioPlayer.playClickSound()
                                selectedRhymeIndex = index
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = rhyme.emoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = rhyme.titleBn,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) themeColors.onAccent else themeColors.onSurface
                        )
                    }
                }
            }
        }

        // Active Rhyme Main Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.5.dp, currentRhyme.themeColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Rhyme Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(currentRhyme.themeColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentRhyme.emoji, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (showEnglish) currentRhyme.titleEn else currentRhyme.titleBn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = themeColors.onSurface
                            )
                            Text(
                                text = if (showEnglish) currentRhyme.authorEn else currentRhyme.authorBn,
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColors.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }

                    // Language Toggle (বাংলা / EN)
                    FilterChip(
                        selected = showEnglish,
                        onClick = {
                            stopPlayback()
                            onShowEnglishChange(!showEnglish)
                        },
                        label = {
                            Text(
                                text = if (showEnglish) "English" else "বাংলা",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rhyme Lines List (Interactive Read-Along)
                val lines = if (showEnglish) currentRhyme.linesEn else currentRhyme.linesBn
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(lines.indices.toList()) { idx ->
                        val line = lines[idx]
                        val isHighlighted = activeLineIndex == idx

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isHighlighted) currentRhyme.themeColor.copy(alpha = 0.18f) else Color.Transparent,
                            border = if (isHighlighted) androidx.compose.foundation.BorderStroke(1.5.dp, currentRhyme.themeColor) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    audioPlayer.playClickSound()
                                    audioPlayer.speak(line, isBn = !showEnglish)
                                    activeLineIndex = idx
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = line,
                                    fontSize = 18.sp,
                                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isHighlighted) currentRhyme.themeColor else themeColors.onSurface
                                )
                                if (isHighlighted) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Playing line",
                                        tint = currentRhyme.themeColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Fun Fact Note
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = themeColors.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 ${currentRhyme.funFact}",
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColors.onSurface.copy(alpha = 0.75f),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Playback Control Bar
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = themeColors.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isPlaying) "ছড়া আবৃত্তি হচ্ছে..." else "সম্পূর্ণ ছড়া শোনো",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.onSurface
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Previous Rhyme
                            IconButton(
                                onClick = {
                                    stopPlayback()
                                    if (selectedRhymeIndex > 0) selectedRhymeIndex--
                                },
                                enabled = selectedRhymeIndex > 0
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                            }

                            // Play / Pause Button
                            val playBtnBg = if (isPlaying) Color(0xFFD32F2F) else themeColors.accent
                            val playBtnContent = if (isPlaying) Color.White else themeColors.onAccent
                            FilledIconButton(
                                onClick = {
                                    if (isPlaying) {
                                        stopPlayback()
                                    } else {
                                        startPlayback()
                                    }
                                },
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = playBtnBg,
                                    contentColor = playBtnContent
                                )
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = playBtnContent
                                )
                            }

                            // Next Rhyme
                            IconButton(
                                onClick = {
                                    stopPlayback()
                                    if (selectedRhymeIndex < rhymes.size - 1) selectedRhymeIndex++
                                },
                                enabled = selectedRhymeIndex < rhymes.size - 1
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next")
                            }
                        }
                    }
                }
            }
        }
    }
}

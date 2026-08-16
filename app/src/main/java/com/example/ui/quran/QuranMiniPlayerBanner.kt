package com.example.ui.quran

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.quran.QuranAudioPlayer
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun QuranMiniPlayerBanner(
    audioPlayer: QuranAudioPlayer,
    themeColors: CalculatorThemeColors,
    modifier: Modifier = Modifier,
    onOpenSurah: (Int) -> Unit
) {
    val isPlayerActive by audioPlayer.isPlayerActive.collectAsStateWithLifecycle()
    val isPlaying by audioPlayer.isPlaying.collectAsStateWithLifecycle()
    val currentSurahNum by audioPlayer.currentSurahNumber.collectAsStateWithLifecycle()
    val currentSurahBangla by audioPlayer.currentSurahNameBangla.collectAsStateWithLifecycle()
    val currentSurahArabic by audioPlayer.currentSurahNameArabic.collectAsStateWithLifecycle()
    val currentAyahIndex by audioPlayer.currentAyahIndex.collectAsStateWithLifecycle()
    val totalAyahs by audioPlayer.totalAyahs.collectAsStateWithLifecycle()
    val currentPos by audioPlayer.currentPositionMs.collectAsStateWithLifecycle()
    val duration by audioPlayer.durationMs.collectAsStateWithLifecycle()

    val visible = isPlayerActive && currentSurahNum != null

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(250)),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        val primaryColor = themeColors.buttonEqualBg
        val progress = if (duration > 0) (currentPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

        val surahTitle = if (currentSurahBangla.isNotBlank()) {
            "$currentSurahBangla ${if (currentSurahArabic.isNotBlank()) "($currentSurahArabic)" else ""}"
        } else {
            "সূরা $currentSurahNum"
        }

        val ayahText = if (totalAyahs > 0) "আয়াত ${currentAyahIndex + 1} / $totalAyahs" else "তেলাওয়াত চলছে..."

        val bannerInteractionSource = remember { MutableInteractionSource() }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = bannerInteractionSource,
                    indication = ripple(bounded = true, color = primaryColor)
                ) {
                    currentSurahNum?.let { onOpenSurah(it) }
                },
            color = if (themeColors.isDark) Color(0xFF1E293B) else Color.White,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.5.dp,
                if (themeColors.isDark) primaryColor.copy(alpha = 0.4f) else primaryColor.copy(alpha = 0.25f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Main Info Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Equalizer / Book Icon badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        primaryColor,
                                        primaryColor.copy(alpha = 0.8f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPlaying) {
                            EqualizerAnimation(color = Color.White)
                        } else {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Quran",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Middle: Title & Ayah Progress
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = surahTitle,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ayahText,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• স্পর্শ করে বিস্তারিত দেখুন",
                                fontSize = 10.sp,
                                color = themeColors.displayText.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right: Play/Pause Button
                    IconButton(
                        onClick = { audioPlayer.togglePlayPause() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Right: Close Button
                    IconButton(
                        onClick = { audioPlayer.stopAndClose() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Player",
                            tint = themeColors.displayText.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom subtle playback progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = primaryColor,
                    trackColor = primaryColor.copy(alpha = 0.15f)
                )
            }
        }
    }
}

@Composable
private fun EqualizerAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "b3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar1)
                .background(color, RoundedCornerShape(1.5.dp))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar2)
                .background(color, RoundedCornerShape(1.5.dp))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar3)
                .background(color, RoundedCornerShape(1.5.dp))
        )
    }
}

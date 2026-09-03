package com.example.ui.screens.tools.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KidsPuzzleTab(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    selectedCategory: PuzzleCategory,
    onCategoryChange: (PuzzleCategory) -> Unit,
    onRewardStars: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val puzzles = remember(selectedCategory) { KidsDataProvider.getWordPuzzles(selectedCategory) }

    var currentPuzzleIndex by remember { mutableStateOf(0) }
    val currentPuzzle = puzzles.getOrElse(currentPuzzleIndex) { puzzles.firstOrNull() }

    // Placed tiles in target slots
    val placedTiles = remember(currentPuzzleIndex, selectedCategory) {
        mutableStateListOf<String>()
    }

    // Available tiles remaining to pick
    val availableTiles = remember(currentPuzzleIndex, selectedCategory) {
        mutableStateListOf<String>().apply {
            currentPuzzle?.let { addAll(it.letterTiles.shuffled()) }
        }
    }

    var isWordCompleted by remember { mutableStateOf(false) }
    var isWrongAnswer by remember { mutableStateOf(false) }

    // Auto-pronounce when puzzle loads
    LaunchedEffect(currentPuzzleIndex, selectedCategory) {
        isWordCompleted = false
        isWrongAnswer = false
        currentPuzzle?.let {
            delay(200)
            val isBn = selectedCategory == PuzzleCategory.EASY_BANGLA || selectedCategory == PuzzleCategory.MEDIUM_BANGLA
            audioPlayer.speak("ছবি দেখে শব্দ সাজাও: ${it.targetWord}", isBn = isBn)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Category Selector Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PuzzleCategory.values()) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) themeColors.accent else themeColors.surface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)) else null,
                    modifier = Modifier.clickable {
                        audioPlayer.playClickSound()
                        onCategoryChange(cat)
                        currentPuzzleIndex = 0
                    }
                ) {
                    Text(
                        text = cat.titleBn,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Header Progress & Star Level
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = themeColors.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Extension, contentDescription = "Puzzle", tint = themeColors.accent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ধাপ: ${currentPuzzleIndex + 1}/${puzzles.size}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.onSurface
                    )
                }

                Text(
                    text = "বর্ণ সাজিয়ে শব্দ বানাও 🧩",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Picture Card & Hint
        currentPuzzle?.let { puzzle ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = puzzle.accentColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, puzzle.accentColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Picture Emoji with Speaker Button
                    Box(contentAlignment = Alignment.TopEnd) {
                        Surface(
                            shape = CircleShape,
                            color = themeColors.surface,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(110.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = puzzle.imageEmoji,
                                    fontSize = 58.sp
                                )
                            }
                        }

                        // Voice Replay Speaker
                        IconButton(
                            onClick = {
                                audioPlayer.playClickSound()
                                val isBn = selectedCategory == PuzzleCategory.EASY_BANGLA || selectedCategory == PuzzleCategory.MEDIUM_BANGLA
                                audioPlayer.speak(puzzle.targetWord, isBn = isBn)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .offset(x = 6.dp, y = (-4).dp)
                                .background(puzzle.accentColor, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Pronounce",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hint Text
                    Text(
                        text = puzzle.hintBn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.onSurface.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Target Drop Slots (Placed Letters)
            Text(
                text = "নিচের ঘরে বর্ণ বসাও:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = themeColors.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                puzzle.correctOrder.forEachIndexed { index, _ ->
                    val placedTile = placedTiles.getOrNull(index)
                    val isSlotFilled = placedTile != null

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSlotFilled) {
                            if (isWordCompleted) Color(0xFF2E7D32) else themeColors.accent
                        } else {
                            themeColors.surfaceVariant.copy(alpha = 0.35f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = if (isWordCompleted) Color(0xFF43A047) else if (isSlotFilled) themeColors.accent else themeColors.onSurface.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(64.dp)
                            .clickable(enabled = isSlotFilled && !isWordCompleted) {
                                // Return tile back to available pool
                                placedTile?.let { tile ->
                                    audioPlayer.playClickSound()
                                    placedTiles.removeAt(index)
                                    availableTiles.add(tile)
                                    isWrongAnswer = false
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSlotFilled) {
                                Text(
                                    text = placedTile ?: "",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.onSurface.copy(alpha = 0.25f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Available Letter Tiles Pool (Tap to place)
            Text(
                text = "বর্ণে ট্যাপ করে ঘরে বসাও:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = themeColors.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (availableTiles.isEmpty() && !isWordCompleted) {
                    Text(
                        text = "সব বর্ণ বসানো হয়েছে!",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onSurface.copy(alpha = 0.5f)
                    )
                }

                availableTiles.forEachIndexed { index, tile ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = puzzle.accentColor,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(60.dp)
                            .clickable {
                                if (placedTiles.size < puzzle.correctOrder.size) {
                                    audioPlayer.playClickSound()
                                    placedTiles.add(tile)
                                    availableTiles.removeAt(index)

                                    // Check if full word entered
                                    if (placedTiles.size == puzzle.correctOrder.size) {
                                        val isCorrect = placedTiles.toList() == puzzle.correctOrder
                                        if (isCorrect) {
                                            isWordCompleted = true
                                            audioPlayer.playSuccessChime()
                                            audioPlayer.playCelebrationSound()
                                            val isBn = selectedCategory == PuzzleCategory.EASY_BANGLA || selectedCategory == PuzzleCategory.MEDIUM_BANGLA
                                            audioPlayer.speak("দারুণ! ${puzzle.targetWord}! সঠিক হয়েছে!", isBn = isBn)
                                            onRewardStars(3)

                                            coroutineScope.launch {
                                                delay(1400)
                                                if (currentPuzzleIndex < puzzles.size - 1) {
                                                    currentPuzzleIndex++
                                                } else {
                                                    audioPlayer.speak("অভিনন্দন! তুমি সব শব্দ সম্পূর্ণ করেছ!", isBn = true)
                                                    onRewardStars(5)
                                                    currentPuzzleIndex = 0
                                                }
                                            }
                                        } else {
                                            isWrongAnswer = true
                                            audioPlayer.playClickSound()
                                            audioPlayer.speak("শব্দ মেলেনি, আবার সাজাও!", isBn = true)
                                        }
                                    }
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tile,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Completed Message or Wrong notification
            if (isWordCompleted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF43A047)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF43A047), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "দারুণ! সঠিক শব্দ: ${puzzle.targetWord} (+৩ ⭐)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            } else if (isWrongAnswer) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE53935).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Wrong", tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "শব্দটি মেলেনি! ঘরে ট্যাপ করে বর্ণ ফিরিয়ে নাও।",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Puzzle
                IconButton(
                    onClick = {
                        if (currentPuzzleIndex > 0) {
                            audioPlayer.playClickSound()
                            currentPuzzleIndex--
                        }
                    },
                    enabled = currentPuzzleIndex > 0,
                    modifier = Modifier
                        .size(44.dp)
                        .background(themeColors.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Puzzle",
                        tint = if (currentPuzzleIndex > 0) themeColors.onSurface else themeColors.onSurface.copy(alpha = 0.3f)
                    )
                }

                // Reset & Shuffle Current Word
                OutlinedButton(
                    onClick = {
                        audioPlayer.playClickSound()
                        placedTiles.clear()
                        availableTiles.clear()
                        availableTiles.addAll(puzzle.letterTiles.shuffled())
                        isWrongAnswer = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp), tint = themeColors.onSurface)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("পুনরায় সাজাও", style = MaterialTheme.typography.labelMedium, color = themeColors.onSurface)
                }

                // Next Puzzle
                IconButton(
                    onClick = {
                        if (currentPuzzleIndex < puzzles.size - 1) {
                            audioPlayer.playClickSound()
                            currentPuzzleIndex++
                        } else {
                            currentPuzzleIndex = 0
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(themeColors.accent, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Puzzle",
                        tint = themeColors.onAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

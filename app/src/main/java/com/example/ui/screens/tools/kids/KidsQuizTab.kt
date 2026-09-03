package com.example.ui.screens.tools.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KidsQuizTab(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onRewardStars: (Int) -> Unit
) {
    var quizMode by remember { mutableStateOf(0) } // 0: Question Quiz, 1: Memory Match Game

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Mode Selector (Quiz vs Memory Match)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("মজার কুইজ প্রতিযোগিতা 🏆", "কার্ড মেমরি গেম 🃏").forEachIndexed { idx, title ->
                val isSelected = quizMode == idx
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) themeColors.accent else themeColors.surface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.15f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable {
                            audioPlayer.playClickSound()
                            quizMode = idx
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.5.sp
                            ),
                            color = if (isSelected) themeColors.onAccent else themeColors.onSurface
                        )
                    }
                }
            }
        }

        if (quizMode == 0) {
            QuestionQuizSection(
                themeColors = themeColors,
                audioPlayer = audioPlayer,
                onRewardStars = onRewardStars
            )
        } else {
            MemoryMatchGameSection(
                themeColors = themeColors,
                audioPlayer = audioPlayer,
                onRewardStars = onRewardStars
            )
        }
    }
}

@Composable
fun QuestionQuizSection(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onRewardStars: (Int) -> Unit
) {
    val questions = KidsDataProvider.quizQuestions
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val currentQuestion = questions[currentQuestionIndex]

    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    fun loadQuestion(index: Int) {
        currentQuestionIndex = index
        selectedOptionIndex = null
        isAnswered = false
        isCorrect = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Question Progress Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "প্রশ্ন: ${currentQuestionIndex + 1} / ${questions.size}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = themeColors.accent
            )
            Text(
                text = "+${currentQuestion.rewardPoints} স্টার ⭐",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
        }

        // Main Question Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, themeColors.accent.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Big Visual Prompt (Emoji / Partial Word)
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .background(themeColors.accent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = currentQuestion.visualPrompt, fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentQuestion.questionBn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = themeColors.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Audio Button to read question
                FilledTonalButton(
                    onClick = {
                        audioPlayer.speak(currentQuestion.questionBn, isBn = true)
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Read question", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "প্রশ্নটি শোনো", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Options List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            currentQuestion.options.forEachIndexed { optIndex, optText ->
                val isThisSelected = selectedOptionIndex == optIndex
                val isThisCorrect = optIndex == currentQuestion.correctIndex

                val correctGreen = if (themeColors.isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                val wrongRed = if (themeColors.isDark) Color(0xFFEF5350) else Color(0xFFC62828)

                val (bgColor, textColor, borderColor) = when {
                    !isAnswered -> Triple(themeColors.surface, themeColors.onSurface, themeColors.onSurface.copy(alpha = 0.15f))
                    isThisCorrect -> Triple(Color(0xFF4CAF50).copy(alpha = 0.18f), correctGreen, correctGreen)
                    isThisSelected && !isCorrect -> Triple(Color(0xFFE53935).copy(alpha = 0.18f), wrongRed, wrongRed)
                    else -> Triple(themeColors.surface, themeColors.onSurface.copy(alpha = 0.4f), themeColors.onSurface.copy(alpha = 0.1f))
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isAnswered) {
                            audioPlayer.playClickSound()
                            selectedOptionIndex = optIndex
                            isAnswered = true
                            if (optIndex == currentQuestion.correctIndex) {
                                isCorrect = true
                                audioPlayer.playCelebrationSound()
                                audioPlayer.speak("সঠিক উত্তর! চমৎকার!", isBn = true)
                                onRewardStars(currentQuestion.rewardPoints)
                            } else {
                                isCorrect = false
                                audioPlayer.speak("হলো না, সঠিক উত্তর ছিল ${currentQuestion.options[currentQuestion.correctIndex]}", isBn = true)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        if (isAnswered) {
                            if (isThisCorrect) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = correctGreen)
                            } else if (isThisSelected) {
                                Icon(Icons.Default.Cancel, contentDescription = "Wrong", tint = wrongRed)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action (Next Question Button)
        if (isAnswered) {
            Button(
                onClick = {
                    audioPlayer.playClickSound()
                    val nextIndex = (currentQuestionIndex + 1) % questions.size
                    loadQuestion(nextIndex)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.accent,
                    contentColor = themeColors.onAccent
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (currentQuestionIndex < questions.size - 1) "পরবর্তী প্রশ্ন ➡️" else "প্রথম থেকে আবার শুরু 🔄",
                    fontWeight = FontWeight.Bold,
                    color = themeColors.onAccent,
                    fontSize = 15.sp
                )
            }
        }
    }
}

/**
 * Memory Match Game Section:
 * 12 cards with 6 pairs. Flip cards to find matching pairs!
 */
@Composable
fun MemoryMatchGameSection(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onRewardStars: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var cards by remember {
        mutableStateOf(
            KidsDataProvider.memoryPairs.flatMapIndexed { index, pair ->
                listOf(
                    MemoryCard(index * 2, pair.first, pair.second, pair.second),
                    MemoryCard(index * 2 + 1, pair.first, pair.second, pair.second)
                )
            }.shuffled()
        )
    }

    var flippedIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    val isGameWon = cards.isNotEmpty() && cards.all { it.isMatched }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "একই রকম দুটি কার্ড মেলাও 👇",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "মেলেছে: ${cards.count { it.isMatched } / 2} / 6",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.accent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4x3 Grid of Memory Cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cards.indices.toList()) { index ->
                    val card = cards[index]
                    val isRevealed = card.isFaceUp || card.isMatched

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRevealed) themeColors.surface else themeColors.accent
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .aspectRatio(0.9f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.5.dp,
                                if (card.isMatched) Color(0xFF4CAF50) else themeColors.accent.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = !isRevealed && !isProcessing) {
                                audioPlayer.playClickSound()

                                // Flip this card
                                val updated = cards.toMutableList()
                                updated[index] = card.copy(isFaceUp = true)
                                cards = updated

                                val newFlipped = flippedIndices + index
                                flippedIndices = newFlipped

                                if (newFlipped.size == 2) {
                                    isProcessing = true
                                    val firstIdx = newFlipped[0]
                                    val secondIdx = newFlipped[1]
                                    val firstCard = cards[firstIdx]
                                    val secondCard = cards[secondIdx]

                                    if (firstCard.emoji == secondCard.emoji) {
                                        // MATCH!
                                        audioPlayer.playCelebrationSound()
                                        audioPlayer.speak(firstCard.labelBn, isBn = true)
                                        coroutineScope.launch {
                                            delay(300)
                                            val matchedList = cards.toMutableList()
                                            matchedList[firstIdx] = firstCard.copy(isMatched = true, isFaceUp = true)
                                            matchedList[secondIdx] = secondCard.copy(isMatched = true, isFaceUp = true)
                                            cards = matchedList
                                            flippedIndices = emptyList()
                                            isProcessing = false
                                            onRewardStars(5)
                                        }
                                    } else {
                                        // MISMATCH
                                        coroutineScope.launch {
                                            delay(900)
                                            val resetList = cards.toMutableList()
                                            resetList[firstIdx] = firstCard.copy(isFaceUp = false)
                                            resetList[secondIdx] = secondCard.copy(isFaceUp = false)
                                            cards = resetList
                                            flippedIndices = emptyList()
                                            isProcessing = false
                                        }
                                    }
                                }
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isRevealed) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(text = card.emoji, fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = card.labelBn.split(" ")[0],
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.onSurface
                                    )
                                }
                            } else {
                                Text(
                                    text = "❓",
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Win or Reset Banner
        if (isGameWon) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val winGreen = if (themeColors.isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                    Text(text = "🎉 অভিনন্দন! সব জোড়া মিলেছে! ⭐", fontWeight = FontWeight.Bold, color = winGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            cards = KidsDataProvider.memoryPairs.flatMapIndexed { index, pair ->
                                listOf(
                                    MemoryCard(index * 2, pair.first, pair.second, pair.second),
                                    MemoryCard(index * 2 + 1, pair.first, pair.second, pair.second)
                                )
                            }.shuffled()
                            flippedIndices = emptyList()
                            isProcessing = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "নতুন গেম খেলো 🔄", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        } else {
            TextButton(
                onClick = {
                    cards = KidsDataProvider.memoryPairs.flatMapIndexed { index, pair ->
                        listOf(
                            MemoryCard(index * 2, pair.first, pair.second, pair.second),
                            MemoryCard(index * 2 + 1, pair.first, pair.second, pair.second)
                        )
                    }.shuffled()
                    flippedIndices = emptyList()
                    isProcessing = false
                },
                colors = ButtonDefaults.textButtonColors(contentColor = themeColors.accent),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = themeColors.accent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "কার্ডগুলো এলোমেলো করো 🔄", fontWeight = FontWeight.Medium, color = themeColors.accent)
            }
        }
    }
}

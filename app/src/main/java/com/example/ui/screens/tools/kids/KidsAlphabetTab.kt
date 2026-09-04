package com.example.ui.screens.tools.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun KidsAlphabetTab(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    selectedCategory: AlphabetCategory = AlphabetCategory.BANGLA_VOWEL,
    onCategoryChange: (AlphabetCategory) -> Unit = {},
    isRandomOrder: Boolean = false,
    onToggleRandomOrder: () -> Unit = {},
    onReshuffle: () -> Unit = {},
    shuffleSeed: Int = 0,
    onTraceLetter: (String) -> Unit
) {
    var selectedLetterItem by remember { mutableStateOf<LetterItem?>(null) }

    val baseItems = when (selectedCategory) {
        AlphabetCategory.BANGLA_VOWEL -> KidsDataProvider.banglaVowels
        AlphabetCategory.BANGLA_CONSONANT -> KidsDataProvider.banglaConsonants
        AlphabetCategory.BANGLA_KAR -> KidsDataProvider.banglaKarMarks
        AlphabetCategory.ENGLISH -> KidsDataProvider.englishAlphabet
    }

    val currentItems = remember(selectedCategory, isRandomOrder, shuffleSeed) {
        if (isRandomOrder) {
            baseItems.shuffled(kotlin.random.Random(shuffleSeed + selectedCategory.ordinal * 100))
        } else {
            baseItems
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        // Top Category Selector Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(AlphabetCategory.values()) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) themeColors.accent else themeColors.surface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)) else null,
                    modifier = Modifier.clickable {
                        audioPlayer.playClickSound()
                        onCategoryChange(cat)
                    }
                ) {
                    Text(
                        text = cat.titleBn,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp
                        ),
                        color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Practice Mode Row (Sequential vs Random + Reshuffle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sequential / Random Toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColors.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clickable {
                    audioPlayer.playClickSound()
                    onToggleRandomOrder()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRandomOrder) "🔀 এলোমেলো ক্রম" else "📑 ধারাবাহিক ক্রম",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.onSurface
                    )
                }
            }

            // Reshuffle button if random
            if (isRandomOrder) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.accent.copy(alpha = 0.15f),
                    modifier = Modifier.clickable {
                        audioPlayer.playClickSound()
                        onReshuffle()
                    }
                ) {
                    Text(
                        text = "🎲 আবার এলোমেলো",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Letter Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 78.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 2.dp, bottom = 24.dp)
        ) {
            items(currentItems) { item ->
                val isEnglish = selectedCategory == AlphabetCategory.ENGLISH
                LetterCardTile(
                    item = item,
                    themeColors = themeColors,
                    onClick = {
                        audioPlayer.playClickSound()
                        val speech = KidsDataProvider.getAlphabetSpeechText(item, isEnglish = isEnglish, isDetailed = false)
                        audioPlayer.speak(speech, isBn = !isEnglish)
                        selectedLetterItem = item
                    }
                )
            }
        }
    }

    // Modal Details Dialog for Selected Letter
    selectedLetterItem?.let { item ->
        LetterDetailDialog(
            item = item,
            isEnglish = selectedCategory == AlphabetCategory.ENGLISH,
            themeColors = themeColors,
            audioPlayer = audioPlayer,
            onDismiss = { selectedLetterItem = null },
            onTrace = {
                selectedLetterItem = null
                onTraceLetter(item.letter)
            }
        )
    }
}

@Composable
fun LetterCardTile(
    item: LetterItem,
    themeColors: CalculatorThemeColors,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .aspectRatio(0.80f)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.5.dp,
                color = item.accentColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Top emoji
            Text(
                text = item.emoji,
                fontSize = 20.sp
            )

            // Letter
            Text(
                text = item.letter,
                fontSize = if (item.letter.length > 2) 18.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = item.accentColor,
                textAlign = TextAlign.Center
            )

            // Word Preview
            Text(
                text = item.wordBn,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.onSurface.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LetterDetailDialog(
    item: LetterItem,
    isEnglish: Boolean,
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onDismiss: () -> Unit,
    onTrace: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = themeColors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "English Alphabet" else "বাংলা বর্ণ পরিচয়",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.onSurface.copy(alpha = 0.7f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = themeColors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Big Avatar with Letter and Emoji
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(4.dp, CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    item.accentColor.copy(alpha = 0.25f),
                                    item.accentColor.copy(alpha = 0.10f)
                                )
                            ),
                            CircleShape
                        )
                        .border(3.dp, item.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = item.letter,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = item.accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Word & Emoji
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = item.accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = item.emoji, fontSize = 34.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.wordBn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.onSurface
                            )
                            Text(
                                text = item.wordEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = themeColors.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rhyme / Example sentence
                Text(
                    text = item.exampleSentence,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.accent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pronounce Button
                    Button(
                        onClick = {
                            audioPlayer.playSuccessChime()
                            val speech = KidsDataProvider.getAlphabetSpeechText(item, isEnglish = isEnglish, isDetailed = true)
                            audioPlayer.speak(speech, isBn = !isEnglish)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.accent,
                            contentColor = themeColors.onAccent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Pronounce", tint = themeColors.onAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "উচ্চারণ শোনো", fontWeight = FontWeight.Bold, color = themeColors.onAccent)
                    }

                    // Trace in Magic Slate Button
                    OutlinedButton(
                        onClick = onTrace,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, item.accentColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Draw, contentDescription = "Trace", tint = item.accentColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "হাতে লেখো", fontWeight = FontWeight.Bold, color = item.accentColor)
                    }
                }
            }
        }
    }
}

package com.example.ui.screens.tools.kids

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
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun KidsNatureTab(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    selectedCategory: NatureCategory = NatureCategory.ANIMALS,
    onCategoryChange: (NatureCategory) -> Unit = {},
    onRewardStars: (Int) -> Unit
) {
    var selectedItem by remember { mutableStateOf<NatureItem?>(null) }

    val currentItems = remember(selectedCategory) {
        KidsDataProvider.natureItems.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Subtitle Guidance
        Text(
            text = "ছবিতে স্পর্শ করে ডাক ও পরিচয় শোনো 👇",
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.onSurface.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Items Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
        ) {
            items(currentItems) { item ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.2.dp, item.accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clickable {
                            audioPlayer.playClickSound()
                            audioPlayer.speak(
                                "${item.nameBn}। English is ${item.nameEn}। ${item.soundText}। ${item.funFact}",
                                isBn = true
                            )
                            selectedItem = item
                            onRewardStars(2)
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Big Visual Circle with High Contrast Background and Accent Border
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .background(
                                    if (item.category == NatureCategory.COLORS_SHAPES && item.accentColor == Color(0xFFFFFFFF))
                                        Color(0xFFF0F0F0)
                                    else
                                        item.accentColor.copy(alpha = 0.22f),
                                    CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (item.accentColor == Color(0xFFFFFFFF)) Color(0xFF9E9E9E) else item.accentColor.copy(alpha = 0.7f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.category == NatureCategory.COLORS_SHAPES && item.emoji.isEmpty()) {
                                Surface(
                                    shape = CircleShape,
                                    color = item.accentColor,
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.Gray.copy(alpha = 0.4f)),
                                    modifier = Modifier.size(42.dp)
                                ) {}
                            } else {
                                Text(
                                    text = item.emoji,
                                    fontSize = 36.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.nameBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = item.nameEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.onSurface.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = item.accentColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = item.soundText,
                                style = MaterialTheme.typography.labelSmall,
                                color = item.accentColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Details Dialog for Nature Item
    selectedItem?.let { item ->
        Dialog(onDismissRequest = { selectedItem = null }) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = themeColors.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.category.titleBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent
                        )
                        IconButton(onClick = { selectedItem = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(item.accentColor.copy(alpha = 0.18f), CircleShape)
                            .border(3.dp, item.accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.emoji, fontSize = 54.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = item.nameBn,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = themeColors.onSurface
                    )

                    Text(
                        text = "${item.nameEn} (${item.pronunciation})",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = item.accentColor.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🗣️ ডাক / বৈশিষ্ট্য: ${item.soundText}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = item.accentColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.funFact,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = themeColors.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            audioPlayer.playSuccessChime()
                            audioPlayer.speak(
                                "${item.nameBn}। English is ${item.nameEn}। ${item.soundText}। ${item.funFact}",
                                isBn = true
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = item.accentColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "আবৃত্তি শোনো", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

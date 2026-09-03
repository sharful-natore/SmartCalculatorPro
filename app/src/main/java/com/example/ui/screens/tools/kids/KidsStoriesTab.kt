package com.example.ui.screens.tools.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun KidsStoriesTab(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onRewardStars: (Int) -> Unit
) {
    val stories = remember { KidsDataProvider.getMoralStories() }
    var selectedStory by remember { mutableStateOf<MoralStoryItem?>(null) }
    var currentSceneIndex by remember { mutableStateOf(0) }
    var isNarrating by remember { mutableStateOf(false) }

    // If a story is open in reader mode
    if (selectedStory != null) {
        val story = selectedStory!!
        val currentScene = story.scenes.getOrElse(currentSceneIndex) { story.scenes.first() }
        val isLastScene = currentSceneIndex == story.scenes.size - 1

        val readerScroll = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(readerScroll)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Story Reader Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back to Stories List
                IconButton(
                    onClick = {
                        audioPlayer.playClickSound()
                        selectedStory = null
                        currentSceneIndex = 0
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(themeColors.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Stories",
                        tint = themeColors.onSurface
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = story.titleBn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "দৃশ্য ${currentSceneIndex + 1} / ${story.scenes.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = story.themeColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Scene Audio Narrator
                IconButton(
                    onClick = {
                        audioPlayer.playClickSound()
                        audioPlayer.speak(currentScene.narrationBn, isBn = true)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(story.themeColor, CircleShape)
                ) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Read Aloud",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Illustrated Scene Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = story.themeColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, story.themeColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Scene Visual Emoji Illustration
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = themeColors.surface,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = currentScene.imageEmoji,
                                fontSize = 48.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scene Title
                    Text(
                        text = currentScene.headingBn,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = themeColors.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Story Scene Description
                    Text(
                        text = currentScene.descriptionBn,
                        style = MaterialTheme.typography.bodyLarge,
                        color = themeColors.onSurface.copy(alpha = 0.9f),
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Moral Box on Final Scene
            if (isLastScene) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFFD54F).copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Moral", tint = Color(0xFFFF8F00), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "গল্পের নীতিশিক্ষা (Moral):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = story.moralBn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.onSurface,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = story.moralEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Scene Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Scene
                OutlinedButton(
                    onClick = {
                        if (currentSceneIndex > 0) {
                            audioPlayer.playClickSound()
                            currentSceneIndex--
                        }
                    },
                    enabled = currentSceneIndex > 0,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", modifier = Modifier.size(16.dp), tint = themeColors.onSurface)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("পূর্ববর্তী দৃশ্য", style = MaterialTheme.typography.labelMedium, color = themeColors.onSurface)
                }

                // Next Scene or Finish Story
                if (!isLastScene) {
                    Button(
                        onClick = {
                            if (currentSceneIndex < story.scenes.size - 1) {
                                audioPlayer.playClickSound()
                                currentSceneIndex++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = story.themeColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("পরবর্তী দৃশ্য", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(16.dp), tint = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            audioPlayer.playCelebrationSound()
                            audioPlayer.speak("দারুণ! গল্প পড়া শেষ করেছ!", isBn = true)
                            onRewardStars(5)
                            selectedStory = null
                            currentSceneIndex = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = "Finish", modifier = Modifier.size(16.dp), tint = Color(0xFFFFD54F))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("গল্প শেষ (+৫ ⭐)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    } else {
        // Stories List View
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = themeColors.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Moral Stories", tint = themeColors.accent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ছোটদের সচিত্র নীতিগল্প 📖",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.onSurface
                            )
                            Text(
                                text = "সুন্দর ছবি দেখে গল্প পড়ো ও নীতিশিক্ষা শেখো",
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColors.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            items(stories, key = { it.id }) { story ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = themeColors.surface,
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, story.themeColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            audioPlayer.playClickSound()
                            selectedStory = story
                            currentSceneIndex = 0
                            audioPlayer.speak("গল্প: ${story.titleBn}", isBn = true)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Story Cover Art
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = story.themeColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = story.coverEmoji, fontSize = 34.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Story Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = story.titleBn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.onSurface
                            )
                            Text(
                                text = story.titleEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColors.onSurface.copy(alpha = 0.55f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "শিক্ষা: ${story.moralBn}",
                                style = MaterialTheme.typography.bodySmall,
                                color = story.themeColor,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Story",
                            tint = story.themeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

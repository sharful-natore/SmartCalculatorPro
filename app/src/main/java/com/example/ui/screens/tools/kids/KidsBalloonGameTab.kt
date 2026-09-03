package com.example.ui.screens.tools.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KidsBalloonGameTab(
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    selectedCategory: BalloonCategory,
    onCategoryChange: (BalloonCategory) -> Unit,
    onRewardStars: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val targets = remember(selectedCategory) { KidsDataProvider.getBalloonTargets(selectedCategory) }

    var currentTargetIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var poppedOptionId by remember { mutableStateOf<String?>(null) }
    var wrongOptionId by remember { mutableStateOf<String?>(null) }

    val currentTarget = targets.getOrElse(currentTargetIndex) { targets.firstOrNull() }

    // Speak prompt on target switch
    LaunchedEffect(currentTargetIndex, selectedCategory) {
        currentTarget?.let { target ->
            poppedOptionId = null
            wrongOptionId = null
            delay(200)
            audioPlayer.speak(target.targetSpeech, isBn = true)
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
            items(BalloonCategory.values()) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) themeColors.accent else themeColors.surface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)) else null,
                    modifier = Modifier.clickable {
                        audioPlayer.playClickSound()
                        onCategoryChange(cat)
                        currentTargetIndex = 0
                        poppedOptionId = null
                        wrongOptionId = null
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = cat.icon, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.titleBn,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) themeColors.onAccent else themeColors.onSurface
                        )
                    }
                }
            }
        }

        // Score & Streak Board Card
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
                // Score
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Score",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "পয়েন্ট: $score",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.onSurface
                    )
                }

                // Level indicator
                Text(
                    text = "ধাপ: ${currentTargetIndex + 1}/${targets.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.accent
                )

                // Streak combo
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFF9100).copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "কম্বো: 🔥 $streak",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Target Prompt Banner
        currentTarget?.let { target ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = themeColors.accent.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, themeColors.accent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "বেলুন খুঁজে ফোটাও 🎈",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = target.promptBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = themeColors.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Voice Replay Button
                    IconButton(
                        onClick = {
                            audioPlayer.playClickSound()
                            audioPlayer.speak(target.targetSpeech, isBn = true)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(themeColors.accent, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = "Speak Prompt",
                            tint = themeColors.onAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Balloons Floating Grid (2 x 2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                target.options.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowOptions.forEach { opt ->
                            val isCorrect = opt.id == target.correctId
                            val isPopped = poppedOptionId == opt.id
                            val isWrong = wrongOptionId == opt.id

                            Box(modifier = Modifier.weight(1f)) {
                                BalloonCard(
                                    option = opt,
                                    isPopped = isPopped,
                                    isWrong = isWrong,
                                    themeColors = themeColors,
                                    onPop = {
                                        if (poppedOptionId == null) {
                                            if (isCorrect) {
                                                poppedOptionId = opt.id
                                                audioPlayer.playSuccessChime()
                                                audioPlayer.speak("সাবাস! দারুণ!", isBn = true)
                                                score += 10
                                                streak += 1
                                                onRewardStars(2)

                                                coroutineScope.launch {
                                                    delay(1000)
                                                    if (currentTargetIndex < targets.size - 1) {
                                                        currentTargetIndex++
                                                    } else {
                                                        audioPlayer.playCelebrationSound()
                                                        audioPlayer.speak("অভিনন্দন! তুমি সব বেলুন সঠিক ফুটিয়েছ!", isBn = true)
                                                        onRewardStars(5)
                                                        currentTargetIndex = 0
                                                    }
                                                    poppedOptionId = null
                                                }
                                            } else {
                                                wrongOptionId = opt.id
                                                streak = 0
                                                audioPlayer.playClickSound()
                                                audioPlayer.speak("আরেকবার চেষ্টা করো!", isBn = true)
                                                coroutineScope.launch {
                                                    delay(700)
                                                    wrongOptionId = null
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bottom Controls / Skip Target
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Restart Level
            OutlinedButton(
                onClick = {
                    audioPlayer.playClickSound()
                    currentTargetIndex = 0
                    score = 0
                    streak = 0
                    poppedOptionId = null
                },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(16.dp), tint = themeColors.onSurface)
                Spacer(modifier = Modifier.width(4.dp))
                Text("পুনরায় শুরু", style = MaterialTheme.typography.labelMedium, color = themeColors.onSurface)
            }

            // Next Target
            Button(
                onClick = {
                    audioPlayer.playClickSound()
                    if (currentTargetIndex < targets.size - 1) {
                        currentTargetIndex++
                    } else {
                        currentTargetIndex = 0
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("পরবর্তী ধাপ ➡️", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = themeColors.onAccent)
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun BalloonCard(
    option: BalloonOption,
    isPopped: Boolean,
    isWrong: Boolean,
    themeColors: CalculatorThemeColors,
    onPop: () -> Unit
) {
    // Floating bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "balloon_float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "balloon_bounce"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPopped) 1.25f else if (isWrong) 0.9f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "balloon_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offsetY.dp)
            .scale(scale)
            .clickable { onPop() }
    ) {
        // Balloon Body
        Surface(
            shape = RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp, bottomStart = 35.dp, bottomEnd = 35.dp),
            color = option.color,
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(width = 135.dp, height = 150.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Highlight Reflection Arc
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.38f),
                        startAngle = 200f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(16.dp.toPx(), 14.dp.toPx()),
                        size = Size(35.dp.toPx(), 45.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Balloon Content (Letter, Number, or Emoji)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (option.displayEmoji.isNotEmpty() && option.displayEmoji != "🎈") {
                        Text(
                            text = option.displayEmoji,
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = option.displayText,
                        fontSize = if (option.displayText.length > 3) 18.sp else 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Balloon Knot & Thread
        Canvas(
            modifier = Modifier
                .width(20.dp)
                .height(24.dp)
        ) {
            // Knot Triangle
            val knotPath = Path().apply {
                moveTo(size.width / 2 - 6.dp.toPx(), 0f)
                lineTo(size.width / 2 + 6.dp.toPx(), 0f)
                lineTo(size.width / 2, 8.dp.toPx())
                close()
            }
            drawPath(path = knotPath, color = option.color)

            // Thread
            val threadPath = Path().apply {
                moveTo(size.width / 2, 8.dp.toPx())
                quadraticTo(size.width / 2 - 4.dp.toPx(), 16.dp.toPx(), size.width / 2, 24.dp.toPx())
            }
            drawPath(
                path = threadPath,
                color = Color.Gray.copy(alpha = 0.7f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

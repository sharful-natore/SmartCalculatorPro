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
import androidx.compose.ui.graphics.StrokeCap
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
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "balloon_bounce"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPopped) 0.05f else if (isWrong) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "balloon_scale"
    )

    // Particle Burst Animation when popped
    var burstProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(isPopped) {
        if (isPopped) {
            burstProgress = 0f
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(400, easing = LinearOutSlowInEasing)
            ) { value, _ ->
                burstProgress = value
            }
        } else {
            burstProgress = 0f
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .offset(y = offsetY.dp)
    ) {
        if (burstProgress > 0f) {
            // Particle Burst Explosion
            Canvas(modifier = Modifier.size(170.dp)) {
                val numParticles = 12
                val maxRadius = size.minDimension * 0.55f * burstProgress
                val alpha = (1f - burstProgress).coerceIn(0f, 1f)
                val particleColors = listOf(
                    option.color, Color.Yellow, Color.White, Color.Cyan, Color(0xFFFF4081)
                )

                for (i in 0 until numParticles) {
                    val angle = (i * (360f / numParticles)) * (Math.PI / 180f)
                    val px = center.x + maxRadius * kotlin.math.cos(angle).toFloat()
                    val py = center.y + maxRadius * kotlin.math.sin(angle).toFloat()
                    val pColor = particleColors[i % particleColors.size]

                    // Draw star/circle fragment
                    drawCircle(
                        color = pColor.copy(alpha = alpha),
                        radius = (8.dp.toPx() * (1f - burstProgress * 0.5f)),
                        center = Offset(px, py)
                    )
                }
            }
        }

        if (!isPopped || burstProgress < 0.3f) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(scale)
                    .clickable { onPop() }
            ) {
                // Realistic Teardrop Balloon Canvas
                Box(
                    modifier = Modifier.size(width = 130.dp, height = 150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val balloonBodyHeight = h * 0.88f

                        // Teardrop Balloon Path
                        val balloonPath = Path().apply {
                            moveTo(w * 0.5f, 0f)
                            // Top Right curve
                            cubicTo(
                                w * 1.05f, 0f,
                                w * 1.05f, balloonBodyHeight * 0.65f,
                                w * 0.54f, balloonBodyHeight
                            )
                            // Bottom Knot Point Right
                            lineTo(w * 0.54f, balloonBodyHeight + 6.dp.toPx())
                            lineTo(w * 0.46f, balloonBodyHeight + 6.dp.toPx())
                            lineTo(w * 0.46f, balloonBodyHeight)
                            // Top Left curve
                            cubicTo(
                                -w * 0.05f, balloonBodyHeight * 0.65f,
                                -w * 0.05f, 0f,
                                w * 0.5f, 0f
                            )
                            close()
                        }

                        // Draw Balloon Body Shadow & Fill
                        drawPath(path = balloonPath, color = option.color)

                        // Glossy Highlight reflection arc (Top-Left)
                        val highlightPath = Path().apply {
                            moveTo(w * 0.22f, h * 0.15f)
                            cubicTo(
                                w * 0.28f, h * 0.08f,
                                w * 0.45f, h * 0.08f,
                                w * 0.52f, h * 0.12f
                            )
                        }
                        drawPath(
                            path = highlightPath,
                            color = Color.White.copy(alpha = 0.45f),
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Small bottom highlight dot
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = 3.dp.toPx(),
                            center = Offset(w * 0.25f, h * 0.28f)
                        )

                        // Knot Triangle
                        val knotPath = Path().apply {
                            moveTo(w * 0.5f - 8.dp.toPx(), balloonBodyHeight + 4.dp.toPx())
                            lineTo(w * 0.5f + 8.dp.toPx(), balloonBodyHeight + 4.dp.toPx())
                            lineTo(w * 0.5f, balloonBodyHeight + 12.dp.toPx())
                            close()
                        }
                        drawPath(path = knotPath, color = option.color)

                        // String Thread Curve
                        val threadPath = Path().apply {
                            moveTo(w * 0.5f, balloonBodyHeight + 12.dp.toPx())
                            cubicTo(
                                w * 0.5f - 10.dp.toPx(), balloonBodyHeight + 20.dp.toPx(),
                                w * 0.5f + 10.dp.toPx(), balloonBodyHeight + 28.dp.toPx(),
                                w * 0.5f, h
                            )
                        }
                        drawPath(
                            path = threadPath,
                            color = Color.White.copy(alpha = 0.65f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Content inside Balloon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        if (option.displayEmoji.isNotEmpty() && option.displayEmoji != "🎈") {
                            Text(
                                text = option.displayEmoji,
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        Text(
                            text = option.displayText,
                            fontSize = if (option.displayText.length > 3) 16.sp else 34.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

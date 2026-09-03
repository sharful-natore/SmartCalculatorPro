package com.example.ui.screens.tools.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorThemeColors

data class DrawStroke(
    val path: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

enum class TracingCategory(val titleBn: String, val titleEn: String) {
    BANGLA_VOWEL("স্বরবর্ণ", "Vowels"),
    BANGLA_CONSONANT("ব্যঞ্জনবর্ণ", "Consonants"),
    BANGLA_NUMBERS("সংখ্যা ১-১০", "Numbers 1-10"),
    ENGLISH_LETTERS("English A-Z", "Letters A-Z"),
    ENGLISH_NUMBERS("Numbers 1-10", "1-10"),
    SHAPES("আকার ও শেপস", "Shapes"),
    FREE_SLATE("মুক্ত স্লেট", "Free Slate")
}

@Composable
fun KidsSlateTab(
    initialTraceLetter: String? = null,
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onRewardStars: (Int) -> Unit
) {
    val strokes = remember { mutableStateListOf<DrawStroke>() }
    val currentPathPoints = remember { mutableStateListOf<Offset>() }

    var activeCategory by remember { mutableStateOf(TracingCategory.BANGLA_VOWEL) }

    val banglaVowels = listOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ", "ও", "ঔ")
    val banglaConsonants = listOf(
        "ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ",
        "ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন",
        "প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ", "স", "হ",
        "ড়", "ঢ়", "য়", "ৎ", "ং", "ঃ", "ঁ"
    )
    val banglaNumbers = listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "১০")
    val englishLetters = ('A'..'Z').map { it.toString() }
    val englishNumbers = (1..10).map { it.toString() }
    val shapes = listOf("⭕ বৃত্ত", "🔺 ত্রিভুজ", "🟩 বর্গ", "⭐ তারা", "❤️ হার্ট", "🔷 রম্বস")

    val currentList: List<String> = remember(activeCategory) {
        when (activeCategory) {
            TracingCategory.BANGLA_VOWEL -> banglaVowels
            TracingCategory.BANGLA_CONSONANT -> banglaConsonants
            TracingCategory.BANGLA_NUMBERS -> banglaNumbers
            TracingCategory.ENGLISH_LETTERS -> englishLetters
            TracingCategory.ENGLISH_NUMBERS -> englishNumbers
            TracingCategory.SHAPES -> shapes
            TracingCategory.FREE_SLATE -> listOf("মুক্ত ড্রয়িং 🎨")
        }
    }

    var selectedItemIndex by remember { mutableStateOf(0) }
    val currentItem = currentList.getOrElse(selectedItemIndex) { currentList.firstOrNull() ?: "অ" }

    // Dotted display letter (e.g. without shape emoji)
    val displayGuideText = remember(currentItem, activeCategory) {
        if (activeCategory == TracingCategory.FREE_SLATE) {
            ""
        } else if (activeCategory == TracingCategory.SHAPES) {
            currentItem.split(" ").firstOrNull() ?: currentItem
        } else {
            currentItem
        }
    }

    val kidColors = listOf(
        Color(0xFFFF1744), // Bright Red
        Color(0xFFFF9100), // Orange
        Color(0xFFFFEA00), // Yellow
        Color(0xFF00E676), // Green
        Color(0xFF00E5FF), // Cyan
        Color(0xFF2979FF), // Blue
        Color(0xFFD500F9), // Purple
        Color(0xFFFFFFFF)  // Chalk White
    )

    var selectedColor by remember { mutableStateOf(kidColors[1]) }
    var strokeWidth by remember { mutableStateOf(18f) }
    var isEraser by remember { mutableStateOf(false) }

    LaunchedEffect(initialTraceLetter) {
        if (!initialTraceLetter.isNullOrBlank()) {
            val vIndex = banglaVowels.indexOf(initialTraceLetter)
            if (vIndex >= 0) {
                activeCategory = TracingCategory.BANGLA_VOWEL
                selectedItemIndex = vIndex
            } else {
                val cIndex = banglaConsonants.indexOf(initialTraceLetter)
                if (cIndex >= 0) {
                    activeCategory = TracingCategory.BANGLA_CONSONANT
                    selectedItemIndex = cIndex
                } else {
                    val eIndex = englishLetters.indexOf(initialTraceLetter.uppercase())
                    if (eIndex >= 0) {
                        activeCategory = TracingCategory.ENGLISH_LETTERS
                        selectedItemIndex = eIndex
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Tracing Category Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(TracingCategory.values()) { cat ->
                val isSelected = activeCategory == cat
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) themeColors.accent else themeColors.surface,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)) else null,
                    modifier = Modifier.clickable {
                        audioPlayer.playClickSound()
                        activeCategory = cat
                        selectedItemIndex = 0
                        strokes.clear()
                    }
                ) {
                    Text(
                        text = cat.titleBn,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Horizontal Letter Selector (Within selected category)
        if (activeCategory != TracingCategory.FREE_SLATE) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(currentList.size) { idx ->
                    val item = currentList[idx]
                    val isSelected = selectedItemIndex == idx
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) themeColors.accent.copy(alpha = 0.22f) else themeColors.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, themeColors.accent) else null,
                        modifier = Modifier
                            .size(if (activeCategory == TracingCategory.SHAPES) 64.dp else 40.dp, 40.dp)
                            .clickable {
                                audioPlayer.playClickSound()
                                selectedItemIndex = idx
                                strokes.clear()
                                val speech = if (activeCategory == TracingCategory.ENGLISH_LETTERS || activeCategory == TracingCategory.ENGLISH_NUMBERS) {
                                    item
                                } else {
                                    item.split(" ").firstOrNull() ?: item
                                }
                                audioPlayer.speak(speech, isBn = activeCategory != TracingCategory.ENGLISH_LETTERS && activeCategory != TracingCategory.ENGLISH_NUMBERS)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = if (activeCategory == TracingCategory.SHAPES) 12.sp else 16.sp
                                ),
                                color = if (isSelected) themeColors.accent else themeColors.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Canvas & Drawing Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF131D2A))
                .border(2.5.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
        ) {
            // Dotted Guide Character in Background
            if (displayGuideText.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayGuideText,
                        fontSize = if (displayGuideText.length > 2) 110.sp else 160.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.16f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Foreground Drawing Layer
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedColor, strokeWidth, isEraser) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPathPoints.clear()
                                currentPathPoints.add(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPathPoints.add(change.position)
                            },
                            onDragEnd = {
                                if (currentPathPoints.isNotEmpty()) {
                                    val finalColor = if (isEraser) Color(0xFF131D2A) else selectedColor
                                    strokes.add(DrawStroke(currentPathPoints.toList(), finalColor, strokeWidth))
                                    currentPathPoints.clear()
                                }
                            }
                        )
                    }
            ) {
                strokes.forEach { stroke ->
                    if (stroke.path.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.path.first().x, stroke.path.first().y)
                            for (i in 1 until stroke.path.size) {
                                lineTo(stroke.path[i].x, stroke.path[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = stroke.color,
                            style = Stroke(
                                width = stroke.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    } else if (stroke.path.size == 1) {
                        drawCircle(
                            color = stroke.color,
                            radius = stroke.strokeWidth / 2,
                            center = stroke.path.first()
                        )
                    }
                }

                if (currentPathPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                        for (i in 1 until currentPathPoints.size) {
                            lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                        }
                    }
                    val activeColor = if (isEraser) Color(0xFF131D2A) else selectedColor
                    drawPath(
                        path = path,
                        color = activeColor,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Top Floating Navigation (Prev / Next & Speaker)
            if (activeCategory != TracingCategory.FREE_SLATE && currentList.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Item
                    IconButton(
                        onClick = {
                            if (selectedItemIndex > 0) {
                                selectedItemIndex--
                                strokes.clear()
                                audioPlayer.playClickSound()
                                val prevItem = currentList[selectedItemIndex]
                                audioPlayer.speak(prevItem, isBn = activeCategory != TracingCategory.ENGLISH_LETTERS && activeCategory != TracingCategory.ENGLISH_NUMBERS)
                            }
                        },
                        enabled = selectedItemIndex > 0,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = if (selectedItemIndex > 0) 0.2f else 0.06f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            tint = if (selectedItemIndex > 0) Color.White else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Listen Audio Pronunciation
                    IconButton(
                        onClick = {
                            audioPlayer.playClickSound()
                            val sp = if (activeCategory == TracingCategory.SHAPES) currentItem.replace("⭕", "").replace("🔺", "").replace("🟩", "").replace("⭐", "").replace("❤️", "").replace("🔷", "").trim() else currentItem
                            audioPlayer.speak(sp, isBn = activeCategory != TracingCategory.ENGLISH_LETTERS && activeCategory != TracingCategory.ENGLISH_NUMBERS)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = "Pronounce",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Next Item
                    IconButton(
                        onClick = {
                            if (selectedItemIndex < currentList.size - 1) {
                                selectedItemIndex++
                                strokes.clear()
                                audioPlayer.playClickSound()
                                val nextItem = currentList[selectedItemIndex]
                                audioPlayer.speak(nextItem, isBn = activeCategory != TracingCategory.ENGLISH_LETTERS && activeCategory != TracingCategory.ENGLISH_NUMBERS)
                            }
                        },
                        enabled = selectedItemIndex < currentList.size - 1,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = if (selectedItemIndex < currentList.size - 1) 0.2f else 0.06f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = if (selectedItemIndex < currentList.size - 1) Color.White else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bottom Canvas Guide Hint
            if (strokes.isEmpty() && currentPathPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = if (activeCategory == TracingCategory.FREE_SLATE) "স্ক্রিনে আঙুল দিয়ে স্বাধীনভাবে আঁকাআঁকি করো 🖍️" else "ডটেড লাইনের ওপর হাত ঘুরিয়ে নিখুঁতভাবে বর্ণ লেখো ✍️",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tool Palette & Action Controls
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = themeColors.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Color circles row & Eraser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    kidColors.forEach { col ->
                        val isSelected = !isEraser && selectedColor == col
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) themeColors.accent else Color.Black.copy(alpha = 0.25f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    audioPlayer.playClickSound()
                                    selectedColor = col
                                    isEraser = false
                                }
                        )
                    }

                    // Eraser Tool
                    IconButton(
                        onClick = {
                            audioPlayer.playClickSound()
                            isEraser = !isEraser
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (isEraser) themeColors.accent else themeColors.surfaceVariant,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.AutoFixNormal,
                            contentDescription = "Eraser",
                            tint = if (isEraser) themeColors.onAccent else themeColors.onSurface,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Brush Thickness, Done button & Clear Slate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Brush Sizes
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(10f to "চিকন", 18f to "মাঝারি", 28f to "মোটা").forEach { (size, label) ->
                            val isSelected = strokeWidth == size
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) themeColors.accent.copy(alpha = 0.2f) else themeColors.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.accent) else null,
                                modifier = Modifier.clickable {
                                    audioPlayer.playClickSound()
                                    strokeWidth = size
                                }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) themeColors.accent else themeColors.onSurface,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Right Actions (Done Star / Clear)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (strokes.isNotEmpty()) {
                            Button(
                                onClick = {
                                    audioPlayer.playCelebrationSound()
                                    audioPlayer.speak("দারুণ হয়েছে! তুমি অসাধারণ লিখেছো!", isBn = true)
                                    onRewardStars(3)
                                    if (selectedItemIndex < currentList.size - 1) {
                                        selectedItemIndex++
                                    }
                                    strokes.clear()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "Done", tint = Color(0xFFFFD54F), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("লেখা শেষ ⭐", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Clear Button
                        IconButton(
                            onClick = {
                                if (strokes.isNotEmpty()) {
                                    audioPlayer.playClickSound()
                                    strokes.clear()
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFE53935).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color(0xFFE53935), modifier = Modifier.size(17.dp))
                        }
                    }
                }
            }
        }
    }
}

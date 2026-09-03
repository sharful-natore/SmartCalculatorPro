package com.example.ui.screens.tools.kids

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

@Composable
fun KidsSlateTab(
    initialTraceLetter: String? = null,
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    onRewardStars: (Int) -> Unit
) {
    val strokes = remember { mutableStateListOf<DrawStroke>() }
    var currentPathPoints = remember { mutableStateListOf<Offset>() }

    val kidColors = listOf(
        Color(0xFFFF1744), // Bright Red
        Color(0xFFFF9100), // Orange
        Color(0xFFFFEA00), // Yellow
        Color(0xFF00E676), // Green
        Color(0xFF00E5FF), // Cyan
        Color(0xFF2979FF), // Blue
        Color(0xFFD500F9), // Purple
        Color(0xFFFFFFFF)  // White chalk
    )

    var selectedColor by remember { mutableStateOf(kidColors[1]) }
    var strokeWidth by remember { mutableStateOf(16f) }
    var isEraser by remember { mutableStateOf(false) }

    // Dotted tracing guide letters
    val traceTemplates = listOf("মুক্ত স্লেট", "অ", "আ", "ই", "ক", "খ", "গ", "A", "B", "C", "১", "২", "1", "2")
    var selectedTemplate by remember { mutableStateOf(initialTraceLetter ?: "মুক্ত স্লেট") }

    LaunchedEffect(initialTraceLetter) {
        if (!initialTraceLetter.isNullOrBlank()) {
            selectedTemplate = initialTraceLetter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Tracing Templates Horizontal Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ট্রেসিং গাইড:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = themeColors.accent,
                modifier = Modifier.padding(end = 8.dp)
            )

            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(traceTemplates) { template ->
                    val isSelected = selectedTemplate == template
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) themeColors.accent else themeColors.surface,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.15f)) else null,
                        modifier = Modifier
                            .clickable {
                                audioPlayer.playClickSound()
                                selectedTemplate = template
                                strokes.clear()
                                if (template != "মুক্ত স্লেট") {
                                    audioPlayer.speak(template, isBn = true)
                                }
                            }
                    ) {
                        Text(
                            text = template,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // The Magic Canvas / Slate Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E293B)) // Deep dark chalkboard slate
                .border(3.dp, Color(0xFF475569), RoundedCornerShape(24.dp))
        ) {
            // Background Dotted Tracing Guide (if selected)
            if (selectedTemplate != "মুক্ত স্লেট") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedTemplate,
                        fontSize = 180.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.16f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Foreground User Drawing Canvas
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
                                    val finalColor = if (isEraser) Color(0xFF1E293B) else selectedColor
                                    strokes.add(DrawStroke(currentPathPoints.toList(), finalColor, strokeWidth))
                                    currentPathPoints.clear()
                                }
                            }
                        )
                    }
            ) {
                // Draw existing completed strokes
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

                // Draw current active in-progress stroke
                if (currentPathPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                        for (i in 1 until currentPathPoints.size) {
                            lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                        }
                    }
                    val activeColor = if (isEraser) Color(0xFF1E293B) else selectedColor
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

            // Slate watermark / helper
            if (strokes.isEmpty() && currentPathPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = if (selectedTemplate == "মুক্ত স্লেট") "আঙুল দিয়ে স্ক্রিনে ছবি বা লেখা আঁকো 🖍️" else "ডটেড লাইনের ওপর হাত ঘুরিয়ে বর্ণ লেখো ✍️",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Color Palette & Tool Controls
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = themeColors.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Color Circles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    kidColors.forEach { col ->
                        val isSelected = !isEraser && selectedColor == col
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) themeColors.accent else Color.Black.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    audioPlayer.playClickSound()
                                    selectedColor = col
                                    isEraser = false
                                }
                        )
                    }

                    // Eraser Button
                    IconButton(
                        onClick = {
                            audioPlayer.playClickSound()
                            isEraser = !isEraser
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isEraser) themeColors.accent else themeColors.surfaceVariant,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.AutoFixNormal,
                            contentDescription = "Eraser",
                            tint = if (isEraser) themeColors.onAccent else themeColors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Brush Size & Clear Slate Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Brush Sizes (চিকন / মাঝারি / মোটা)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ব্রাশ:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.onSurface.copy(alpha = 0.7f)
                        )

                        listOf(10f to "স্লিক", 18f to "মাঝারি", 28f to "মোটা").forEach { (size, label) ->
                            val isSelected = strokeWidth == size
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) themeColors.accent.copy(alpha = 0.2f) else themeColors.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.accent) else null,
                                modifier = Modifier.clickable {
                                    audioPlayer.playClickSound()
                                    strokeWidth = size
                                }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSelected) themeColors.accent else themeColors.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Clear Slate Button
                    Button(
                        onClick = {
                            if (strokes.isNotEmpty()) {
                                audioPlayer.playSuccessChime()
                                strokes.clear()
                                onRewardStars(2)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "মুছে ফেলো 🧹", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

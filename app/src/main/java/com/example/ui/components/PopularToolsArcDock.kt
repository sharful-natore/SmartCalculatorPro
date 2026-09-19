package com.example.ui.components

import android.graphics.Matrix
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.util.AppLanguage
import com.example.ui.screens.FeaturedDashboardItem
import com.example.ui.theme.CalculatorThemeColors

/**
 * 9-Tool Focal Crest Arc Dock:
 * - Center item (index 4) is the Last Used Tool, with the largest size and an animated AI Chat FAB rotating brush border.
 * - Flanking 4 tools on the left and 4 tools on the right gradually decrease in size.
 * - Non-center tools are sorted by usage frequency (most used adjacent to center, tapering to outer edges).
 * - Cascading stacked discs with crisp borders, drop shadows, and zero touch-collision.
 */
@Composable
fun PopularToolsArcDock(
    items: List<FeaturedDashboardItem>,
    themeColors: CalculatorThemeColors,
    language: AppLanguage,
    onItemClick: (FeaturedDashboardItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val isBn = language == AppLanguage.BENGALI
    val centerIndex = 4
    val centerItem = items.getOrNull(centerIndex) ?: items.first()

    // Highlighted / previewed tool (defaults to center / last used)
    var focusedItem by remember(centerItem) { mutableStateOf<FeaturedDashboardItem?>(null) }
    val displayItem = focusedItem ?: centerItem

    // AI Rotating Border Animation for Center Item
    val infiniteTransition = rememberInfiniteTransition(label = "dock_ai_border_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aiRotationAngle"
    )

    // AI Gemini Gradient Color Palette
    val geminiColors = remember {
        listOf(
            Color(0xFF4285F4), // Blue
            Color(0xFF9B51E0), // Purple
            Color(0xFFEA4335), // Red/Pink
            Color(0xFFFBBC05), // Yellow
            Color(0xFF34A853), // Green
            Color(0xFF4285F4)  // Close loop
        )
    }

    val animatedAiGradientBrush = remember(rotationAngle) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                val shader = SweepGradientShader(
                    center = Offset(size.width / 2f, size.height / 2f),
                    colors = geminiColors
                )
                val matrix = Matrix()
                matrix.postRotate(rotationAngle, size.width / 2f, size.height / 2f)
                shader.setLocalMatrix(matrix)
                return shader
            }
        }
    }

    // Color gradient palette for each slot in the dock
    val slotGradients = remember {
        listOf(
            Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF14B8A6))), // 0 (left edge)
            Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))), // 1
            Brush.linearGradient(listOf(Color(0xFF4C1D95), Color(0xFF8B5CF6))), // 2
            Brush.linearGradient(listOf(Color(0xFF831843), Color(0xFFEC4899))), // 3 (left flank)
            Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))), // 4 (center)
            Brush.linearGradient(listOf(Color(0xFF7C2D12), Color(0xFFF97316))), // 5 (right flank)
            Brush.linearGradient(listOf(Color(0xFF14532D), Color(0xFF22C55E))), // 6
            Brush.linearGradient(listOf(Color(0xFF0369A1), Color(0xFF06B6D4))), // 7
            Brush.linearGradient(listOf(Color(0xFF701A75), Color(0xFFD946EF)))  // 8 (right edge)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stacked Arc Layout Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            val centeredXOffsets = remember {
                listOf(
                    (-129).dp, // 0 (left edge)
                    (-105).dp, // 1
                    (-76).dp,  // 2
                    (-42).dp,  // 3 (left flank)
                    0.dp,      // 4 (center)
                    42.dp,     // 5 (right flank)
                    76.dp,     // 6
                    105.dp,    // 7
                    129.dp     // 8 (right edge)
                )
            }

            items.forEachIndexed { index, item ->
                val distFromCenter = kotlin.math.abs(index - centerIndex)
                val isCenter = distFromCenter == 0

                // Dynamic sizing tapering from center (58dp) down to edges (32dp)
                val circleSize = when (distFromCenter) {
                    0 -> 58.dp
                    1 -> 48.dp
                    2 -> 42.dp
                    3 -> 37.dp
                    else -> 32.dp
                }

                val iconSize = when (distFromCenter) {
                    0 -> 28.dp
                    1 -> 23.dp
                    2 -> 20.dp
                    3 -> 17.dp
                    else -> 15.dp
                }

                // Elevation and Z-Index ensure cascading overlap
                val zIndexVal = (5 - distFromCenter).toFloat()
                val elevationVal = when (distFromCenter) {
                    0 -> 8.dp
                    1 -> 5.dp
                    2 -> 4.dp
                    3 -> 3.dp
                    else -> 2.dp
                }

                val isFocused = focusedItem == item || (focusedItem == null && isCenter)
                val scaleFactor by animateFloatAsState(
                    targetValue = if (isFocused && !isCenter) 1.08f else 1.0f,
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    label = "iconScale"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .offset(x = centeredXOffsets.getOrElse(index) { 0.dp })
                        .zIndex(zIndexVal)
                        .size(circleSize)
                        .scale(scaleFactor)
                        .shadow(
                            elevation = elevationVal,
                            shape = CircleShape,
                            clip = false
                        )
                        .clip(CircleShape)
                        .then(
                            if (isCenter) {
                                Modifier
                                    .background(
                                        if (themeColors.isDark) Color(0xFF0F172A)
                                        else Color(0xFF1E293B)
                                    )
                                    .border(
                                        width = 2.8.dp,
                                        brush = animatedAiGradientBrush,
                                        shape = CircleShape
                                    )
                            } else {
                                Modifier
                                    .background(
                                        slotGradients.getOrElse(index) {
                                            Brush.linearGradient(listOf(themeColors.buttonEqualBg, themeColors.buttonEqualBg))
                                        }
                                    )
                                    .border(
                                        width = 1.6.dp,
                                        color = if (themeColors.isDark) Color(0xFF1E293B) else Color.White,
                                        shape = CircleShape
                                    )
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            focusedItem = item
                            onItemClick(item)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = if (isBn) item.titleBn else item.titleEn,
                        tint = if (isCenter) Color(0xFF67E8F9) else Color.White,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tool Title Capsule with Smooth Transition
        AnimatedContent(
            targetState = displayItem,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220))).togetherWith(fadeOut(animationSpec = tween(180)))
            },
            label = "dock_title_transition"
        ) { currentItem ->
            val isCurrentCenter = currentItem == centerItem
            Surface(
                onClick = { onItemClick(currentItem) },
                shape = RoundedCornerShape(20.dp),
                color = themeColors.cardBg,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrentCenter) Color(0xFF4285F4).copy(alpha = 0.45f)
                    else themeColors.displayText.copy(alpha = 0.12f)
                ),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isCurrentCenter) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(13.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = Color(0xFFFF6D00),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = if (isCurrentCenter) {
                            if (isBn) "সর্বশেষ: ${currentItem.titleBn}" else "Last used: ${currentItem.titleEn}"
                        } else {
                            if (isBn) currentItem.titleBn else currentItem.titleEn
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = themeColors.displayText.copy(alpha = 0.4f),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

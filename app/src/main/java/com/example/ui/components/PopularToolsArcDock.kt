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

import com.example.ui.theme.getToolIconGradient
import androidx.compose.foundation.layout.BoxWithConstraints

/**
 * 9-Tool Focal Crest Arc Dock:
 * - Center item (index 4) is the Last Used Tool, with the largest size and an animated AI Chat rotating brush border.
 * - Flanking tools gracefully span across the screen width with balanced, easily-tappable sizes (40dp minimum).
 * - Theme-harmonized gradients matching the active app theme.
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

    // Theme harmonized gradient brush for non-center items
    val themeGradientBrush = remember(themeColors.buttonEqualBg) {
        getToolIconGradient(themeColors.buttonEqualBg)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stacked Arc Layout Container using BoxWithConstraints for full-width responsive positioning
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            val totalWidth = maxWidth
            // Calculate step so the full 9 items neatly fill the width between edges
            val horizontalPadding = 20.dp
            val availableSpan = (totalWidth - horizontalPadding * 2).coerceAtLeast(280.dp)
            val halfSpan = availableSpan / 2

            items.forEachIndexed { index, item ->
                val distFromCenter = kotlin.math.abs(index - centerIndex)
                val isCenter = distFromCenter == 0

                // Balanced sizing: center is 56dp, tapering gently down to 40dp at the edges
                val circleSize = when (distFromCenter) {
                    0 -> 56.dp
                    1 -> 49.dp
                    2 -> 45.dp
                    3 -> 42.dp
                    else -> 40.dp
                }

                val iconSize = when (distFromCenter) {
                    0 -> 27.dp
                    1 -> 24.dp
                    2 -> 22.dp
                    3 -> 20.dp
                    else -> 19.dp
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

                // Non-linear horizontal distribution for harmonic curve across full width
                val sign = if (index < centerIndex) -1f else if (index > centerIndex) 1f else 0f
                val fraction = when (distFromCenter) {
                    1 -> 0.28f
                    2 -> 0.54f
                    3 -> 0.78f
                    4 -> 1.0f
                    else -> 0f
                }
                val xOffset = halfSpan * fraction * sign

                val isFocused = focusedItem == item || (focusedItem == null && isCenter)
                val scaleFactor by animateFloatAsState(
                    targetValue = if (isFocused && !isCenter) 1.08f else 1.0f,
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    label = "iconScale"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .offset(x = xOffset)
                        .zIndex(zIndexVal)
                        .size(circleSize)
                        .scale(scaleFactor)
                        .clip(CircleShape)
                        .then(
                            if (isCenter) {
                                Modifier
                                    .background(Color.White)
                                    .border(
                                        width = 2.8.dp,
                                        brush = animatedAiGradientBrush,
                                        shape = CircleShape
                                    )
                            } else {
                                Modifier
                                    .background(themeGradientBrush)
                                    .border(
                                        width = 1.6.dp,
                                        color = Color.White,
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
                        tint = if (isCenter) themeColors.buttonEqualBg else Color.White,
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
                    if (isCurrentCenter) themeColors.buttonEqualBg.copy(alpha = 0.45f)
                    else themeColors.displayText.copy(alpha = 0.12f)
                ),
                shadowElevation = 0.dp
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
                            tint = themeColors.buttonEqualBg,
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
                            if (isBn) "শীর্ষ ব্যবহৃত: ${currentItem.titleBn}" else "Top Used: ${currentItem.titleEn}"
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

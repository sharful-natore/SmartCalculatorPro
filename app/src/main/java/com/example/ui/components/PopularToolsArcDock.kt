package com.example.ui.components

import android.graphics.Matrix
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.FeaturedDashboardItem
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.getToolIconGradient
import com.example.util.AppLanguage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Seamless Speech Bubble Shape with integrated downward pointer arrow.
 * Creates a continuous outline so background, border, and elevation cast seamlessly.
 */
class SpeechBubbleShape(
    val cornerRadius: Dp = 14.dp,
    val arrowWidth: Dp = 11.dp,
    val arrowHeight: Dp = 5.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cr = with(density) { cornerRadius.toPx() }.coerceAtMost(size.height / 2f)
        val aw = with(density) { arrowWidth.toPx() }
        val ah = with(density) { arrowHeight.toPx() }
        val bubbleHeight = (size.height - ah).coerceAtLeast(0f)
        val path = Path().apply {
            moveTo(cr, 0f)
            lineTo(size.width - cr, 0f)
            arcTo(
                rect = Rect(size.width - 2 * cr, 0f, size.width, 2 * cr),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, bubbleHeight - cr)
            arcTo(
                rect = Rect(size.width - 2 * cr, bubbleHeight - 2 * cr, size.width, bubbleHeight),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            val arrowLeft = (size.width - aw) / 2f
            val arrowRight = (size.width + aw) / 2f
            val arrowTip = size.width / 2f
            lineTo(arrowRight, bubbleHeight)
            lineTo(arrowTip, size.height)
            lineTo(arrowLeft, bubbleHeight)
            lineTo(cr, bubbleHeight)
            arcTo(
                rect = Rect(0f, bubbleHeight - 2 * cr, 2 * cr, bubbleHeight),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(0f, cr)
            arcTo(
                rect = Rect(0f, 0f, 2 * cr, 2 * cr),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        return Outline.Generic(path)
    }
}

private fun getPositiveMod(value: Int, mod: Int): Int {
    if (mod <= 0) return 0
    val r = value % mod
    return if (r < 0) r + mod else r
}

/**
 * Scrollable Rotary Arc Dock with Speech Bubble Tooltip & Focal Layering:
 * - Persistent Speech Bubble Tooltip with Downward Pointer Arrow sitting directly atop the center tool.
 * - Center item is a true 1:1 circular disc, layered ON TOP of all adjacent discs.
 * - Flanking items decrease gently in size (54dp -> 50dp -> 47dp -> 44dp -> 42dp) with tight, gapless spacing.
 * - Carousel has ample top/bottom clearance so circles are never clipped or colliding with bottom pill.
 * - Bottom capsule permanently displays the user's actual Last Used Tool ("সর্বশেষ ব্যবহৃত").
 */
@Composable
fun PopularToolsArcDock(
    items: List<FeaturedDashboardItem>,
    lastUsedItem: FeaturedDashboardItem?,
    themeColors: CalculatorThemeColors,
    language: AppLanguage,
    onItemClick: (FeaturedDashboardItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val isBn = language == AppLanguage.BENGALI
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Slot spacing tuned to keep items closely nested without gaps
    val slotWidthDp = 40.dp
    val slotWidthPx = with(density) { slotWidthDp.toPx() }
    val carouselHeightDp = 58.dp

    // Continuous scroll offset in pixels
    val scrollOffset = remember { Animatable(0f) }

    // Gemini Rotating Gradient Animation for the center item
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

    val geminiColors = remember {
        listOf(
            Color(0xFF4285F4), // Blue
            Color(0xFF9B51E0), // Purple
            Color(0xFFEA4335), // Red/Pink
            Color(0xFFFBBC05), // Yellow
            Color(0xFF34A853), // Green
            Color(0xFF4285F4)  // Loop
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

    val themeGradientBrush = remember(themeColors.buttonEqualBg) {
        getToolIconGradient(themeColors.buttonEqualBg)
    }

    // Determine current center virtual slot index
    val currentCenterSlot = (-scrollOffset.value / slotWidthPx).roundToInt()

    // Identify active item currently in the center
    val activeCenterItem = remember(currentCenterSlot, items) {
        val actualIdx = getPositiveMod(currentCenterSlot, items.size)
        items.getOrNull(actualIdx) ?: items.first()
    }

    // Smoothly animate to lastUsedItem when it updates externally
    LaunchedEffect(lastUsedItem?.key) {
        if (lastUsedItem != null) {
            val targetLocalIdx = items.indexOfFirst { it.key == lastUsedItem.key }
            if (targetLocalIdx >= 0) {
                val currentSlot = (-scrollOffset.value / slotWidthPx).roundToInt()
                val currentMod = getPositiveMod(currentSlot, items.size)
                var diff = targetLocalIdx - currentMod
                if (diff > items.size / 2) diff -= items.size
                if (diff < -items.size / 2) diff += items.size
                val targetSlot = currentSlot + diff
                scrollOffset.animateTo(
                    targetValue = -targetSlot * slotWidthPx,
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Seamless Speech Bubble Tooltip with Integrated Pointer Arrow
        Surface(
            onClick = { onItemClick(activeCenterItem) },
            shape = SpeechBubbleShape(
                cornerRadius = 14.dp,
                arrowWidth = 11.dp,
                arrowHeight = 5.dp
            ),
            color = themeColors.cardBg,
            border = androidx.compose.foundation.BorderStroke(
                1.2.dp,
                themeColors.buttonEqualBg.copy(alpha = 0.70f)
            ),
            shadowElevation = 2.5.dp
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 3.5.dp, bottom = 8.5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(themeColors.buttonEqualBg)
                )
                AnimatedContent(
                    targetState = activeCenterItem,
                    transitionSpec = {
                        fadeIn(tween(140)) togetherWith fadeOut(tween(140))
                    },
                    label = "CenterTooltipContent"
                ) { targetItem ->
                    Text(
                        text = if (isBn) targetItem.titleBn else targetItem.titleEn,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        maxLines = 1
                    )
                }
            }
        }

        // Tiny 1dp gap between indicator tip and center circle top
        Spacer(modifier = Modifier.height(1.dp))

        // 2. Rotary Arc Carousel Container
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(carouselHeightDp)
                .clipToBounds()
                .draggable(
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            scrollOffset.snapTo(scrollOffset.value + delta)
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        coroutineScope.launch {
                            val current = scrollOffset.value
                            // Fling prediction with magnetic snap to nearest slot
                            val velocityOffset = (velocity / 1000f) * slotWidthPx * 0.35f
                            val projected = current + velocityOffset
                            val targetSlot = (-projected / slotWidthPx).roundToInt()
                            val targetOffset = -targetSlot * slotWidthPx
                            scrollOffset.animateTo(
                                targetValue = targetOffset,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            val screenWidth = maxWidth
            val halfScreenWidth = screenWidth / 2

            // Order slots so outer wings are drawn FIRST, and center slot is drawn LAST on top
            val slotsToRender = remember(currentCenterSlot) {
                val list = mutableListOf<Int>()
                for (dist in 6 downTo 1) {
                    list.add(currentCenterSlot - dist)
                    list.add(currentCenterSlot + dist)
                }
                list.add(currentCenterSlot) // Center slot is rendered last so it's always strictly on top!
                list
            }

            slotsToRender.forEach { slotIdx ->
                val distFromCenter = abs(slotIdx - currentCenterSlot)
                val isCenter = distFromCenter == 0

                val actualItemIdx = getPositiveMod(slotIdx, items.size)
                val item = items[actualItemIdx]

                // Gentle, gradual size gradation so flanking tools remain comfortably sized
                val circleSize = when (distFromCenter) {
                    0 -> 54.dp
                    1 -> 50.dp
                    2 -> 47.dp
                    3 -> 44.dp
                    4 -> 42.dp
                    else -> 40.dp
                }

                val iconSize = when (distFromCenter) {
                    0 -> 26.dp
                    1 -> 24.dp
                    2 -> 23.dp
                    3 -> 22.dp
                    4 -> 21.dp
                    else -> 20.dp
                }

                // Calculate center X coordinate of this slot
                val slotCenterFromMidScreenPx = (slotIdx * slotWidthPx) + scrollOffset.value
                val slotCenterXDp = halfScreenWidth + with(density) { slotCenterFromMidScreenPx.toDp() }
                val slotLeftDp = slotCenterXDp - (circleSize / 2)
                // Center vertically within the 58dp carousel height
                val slotTopDp = (carouselHeightDp - circleSize) / 2

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .offset(x = slotLeftDp, y = slotTopDp)
                        .size(circleSize)
                        .aspectRatio(1f)
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
                            if (isCenter) {
                                onItemClick(item)
                            } else {
                                coroutineScope.launch {
                                    val targetOffset = -slotIdx * slotWidthPx
                                    scrollOffset.animateTo(
                                        targetValue = targetOffset,
                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                    )
                                }
                            }
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

        // 3. Clear spacing before bottom capsule (prevents collision)
        Spacer(modifier = Modifier.height(7.dp))

        // 4. Bottom Capsule: Strictly displays the actual last used tool, fixed and independent of scrolling
        val actualLastUsed = lastUsedItem ?: items.firstOrNull()
        if (actualLastUsed != null) {
            Surface(
                onClick = { onItemClick(actualLastUsed) },
                shape = RoundedCornerShape(20.dp),
                color = themeColors.cardBg,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    themeColors.buttonEqualBg.copy(alpha = 0.45f)
                ),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = themeColors.buttonEqualBg,
                        modifier = Modifier.size(14.dp)
                    )

                    Text(
                        text = if (isBn) "সর্বশেষ ব্যবহৃত: ${actualLastUsed.titleBn}" else "Last used: ${actualLastUsed.titleEn}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = themeColors.displayText.copy(alpha = 0.5f),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

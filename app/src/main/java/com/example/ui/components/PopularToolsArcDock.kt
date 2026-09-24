package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.screens.FeaturedDashboardItem
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.theme.getToolIconGradient
import com.example.util.AppLanguage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private fun getPositiveMod(value: Int, mod: Int): Int {
    if (mod <= 0) return 0
    val r = value % mod
    return if (r < 0) r + mod else r
}

/**
 * Rotary Arc Carousel of Popular Tools with Seamless Speech Bubble Tooltip.
 * - Center slot is strictly on top (zIndex 10f) with a pulse animation that is fully visible and unclipped.
 * - Items are strictly ranked by usage count.
 * - Seamless infinite scroll loop via modular indexing.
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
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Slot spacing tuned to keep items closely nested without gaps
    val slotWidthDp = 40.dp
    val slotWidthPx = with(density) { slotWidthDp.toPx() }
    val carouselHeightDp = 58.dp

    // Continuous scroll offset in pixels
    val scrollOffset = remember { Animatable(0f) }

    // Smooth Theme Color Pulse Animation for the center item
    val infiniteTransition = rememberInfiniteTransition(label = "dock_center_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 1.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Compact & Seamless Speech Bubble Tooltip with Integrated Pointer Arrow
        val tooltipInteractionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .zIndex(20f)
                .clickable(
                    interactionSource = tooltipInteractionSource,
                    indication = null
                ) { onItemClick(activeCenterItem) }
                .drawBehind {
                    val arrowWidthPx = 9.dp.toPx()
                    val arrowHeightPx = 4.dp.toPx()
                    val bubbleHeight = (size.height - arrowHeightPx).coerceAtLeast(0f)
                    val r = (bubbleHeight / 2f).coerceAtLeast(0f)

                    val path = Path().apply {
                        // Top horizontal line
                        moveTo(r, 0f)
                        lineTo((size.width - r).coerceAtLeast(r), 0f)
                        // Right semicircle
                        arcTo(
                            rect = Rect(size.width - 2 * r, 0f, size.width, bubbleHeight),
                            startAngleDegrees = -90f,
                            sweepAngleDegrees = 180f,
                            forceMoveTo = false
                        )
                        // Bottom line right of pointer
                        val arrowRight = (size.width + arrowWidthPx) / 2f
                        val arrowLeft = (size.width - arrowWidthPx) / 2f
                        val arrowTip = size.width / 2f
                        lineTo(arrowRight, bubbleHeight)
                        // Pointer tip
                        lineTo(arrowTip, size.height)
                        // Pointer back to left
                        lineTo(arrowLeft, bubbleHeight)
                        lineTo(r, bubbleHeight)
                        // Left semicircle
                        arcTo(
                            rect = Rect(0f, 0f, 2 * r, bubbleHeight),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 180f,
                            forceMoveTo = false
                        )
                        close()
                    }

                    // Background fill
                    drawPath(
                        path = path,
                        color = themeColors.cardBg
                    )
                    // Border outline
                    drawPath(
                        path = path,
                        color = themeColors.buttonEqualBg.copy(alpha = 0.70f),
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
                .padding(start = 10.dp, end = 10.dp, top = 2.5.dp, bottom = 6.5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.displayText,
                        maxLines = 1
                    )
                }
            }
        }

        // 2. Rotary Arc Carousel Container (no clipping to let pulse wave breathe fully)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(carouselHeightDp)
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
                // Center vertically within the carousel height
                val slotTopDp = (carouselHeightDp - circleSize) / 2

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .offset(x = slotLeftDp, y = slotTopDp)
                        .size(circleSize)
                        .aspectRatio(1f)
                        .zIndex(if (isCenter) 10f else (5f - distFromCenter).coerceAtLeast(0f))
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
                        }
                        .drawBehind {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val baseRadius = size.minDimension / 2f

                            if (isCenter) {
                                // 1. Outer Pulse Ring expanding outwards with fading alpha (fully on top, never obscured)
                                if (pulseAlpha > 0.01f) {
                                    val pulseRadius = baseRadius * pulseScale
                                    drawCircle(
                                        color = themeColors.buttonEqualBg.copy(alpha = pulseAlpha),
                                        radius = pulseRadius,
                                        center = center,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }

                                // 2. Solid White Disc Background
                                drawCircle(
                                    color = Color.White,
                                    radius = baseRadius,
                                    center = center
                                )

                                // 3. Solid Theme Color Border (inward so it stays razor-sharp)
                                val strokeWidth = 2.5.dp.toPx()
                                drawCircle(
                                    color = themeColors.buttonEqualBg,
                                    radius = baseRadius - (strokeWidth / 2f),
                                    center = center,
                                    style = Stroke(width = strokeWidth)
                                )
                            } else {
                                // Side items:
                                // 1. Solid Theme Gradient Disc Fill
                                drawCircle(
                                    brush = themeGradientBrush,
                                    radius = baseRadius,
                                    center = center
                                )

                                // 2. Pure Solid White Border drawn cleanly on inner edge
                                // No clip, no grey fringe, 100% solid white
                                val strokeWidth = 1.6.dp.toPx()
                                drawCircle(
                                    color = Color.White,
                                    radius = baseRadius - (strokeWidth / 2f),
                                    center = center,
                                    style = Stroke(width = strokeWidth)
                                )
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
    }
}

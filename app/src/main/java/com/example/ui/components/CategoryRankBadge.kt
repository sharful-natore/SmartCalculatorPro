package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom Vector Rendered Medal Badge for Top 1, 2, and 3 items in each category.
 * Features shiny metallic gradients, ribbon tails, and crisp rank numbers.
 */
@Composable
fun CategoryRankBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    if (rank !in 1..3) return

    val (topColor, bottomColor, borderColor, textColor) = when (rank) {
        1 -> Quadruple(
            Color(0xFFFFD700), // Bright Gold
            Color(0xFFFF8F00), // Dark Gold
            Color(0xFFFFF59D), // Light Gold Highlight
            Color(0xFF3E2723)  // Dark contrast text
        )
        2 -> Quadruple(
            Color(0xFFF5F5F5), // Light Silver
            Color(0xFF9E9E9E), // Dark Silver
            Color(0xFFFFFFFF), // White Highlight
            Color(0xFF212121)  // Dark contrast text
        )
        else -> Quadruple(
            Color(0xFFFFB74D), // Light Bronze
            Color(0xFFA1887F), // Dark Bronze
            Color(0xFFFFE0B2), // Highlight
            Color(0xFF3E2723)  // Dark contrast text
        )
    }

    Box(
        modifier = modifier.size(17.dp, 21.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val circleRadius = width / 2f
            val circleCenter = Offset(width / 2f, circleRadius)

            // 1. Draw Ribbon Tails behind the medal
            val ribbonColorLeft = Color(0xFFD32F2F)
            val ribbonColorRight = Color(0xFFC62828)

            val leftRibbon = Path().apply {
                moveTo(circleCenter.x - circleRadius * 0.4f, circleCenter.y)
                lineTo(circleCenter.x - circleRadius * 0.7f, height)
                lineTo(circleCenter.x - circleRadius * 0.35f, height - 2.5.dp.toPx())
                lineTo(circleCenter.x, height)
                lineTo(circleCenter.x, circleCenter.y)
                close()
            }
            drawPath(leftRibbon, ribbonColorLeft)

            val rightRibbon = Path().apply {
                moveTo(circleCenter.x, circleCenter.y)
                lineTo(circleCenter.x, height)
                lineTo(circleCenter.x + circleRadius * 0.35f, height - 2.5.dp.toPx())
                lineTo(circleCenter.x + circleRadius * 0.7f, height)
                lineTo(circleCenter.x + circleRadius * 0.4f, circleCenter.y)
                close()
            }
            drawPath(rightRibbon, ribbonColorRight)

            // 2. Main Metallic Circle Gradient
            val metallicBrush = Brush.linearGradient(
                colors = listOf(topColor, bottomColor),
                start = Offset(0f, 0f),
                end = Offset(width, width)
            )
            drawCircle(
                brush = metallicBrush,
                radius = circleRadius - 0.75.dp.toPx(),
                center = circleCenter
            )

            // 3. Glossy Rim Border
            drawCircle(
                color = borderColor,
                radius = circleRadius - 0.75.dp.toPx(),
                center = circleCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 4. Medal Rank Number inside circle
        Box(
            modifier = Modifier.size(17.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                color = textColor,
                fontSize = 9.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)



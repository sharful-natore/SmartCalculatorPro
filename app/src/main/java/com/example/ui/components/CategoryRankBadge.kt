package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern, high-polish Gold, Silver, and Bronze Rank Badges
 * for the Top 1, 2, and 3 most used tools and converters in each category.
 */
@Composable
fun CategoryRankBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    if (rank !in 1..3) return

    val (gradientColors, borderColor, badgeEmoji, labelText) = when (rank) {
        1 -> Quadruple(
            listOf(Color(0xFFFFDF00), Color(0xFFFFA000)),
            Color(0xFFFFECB3),
            "🥇",
            "#1"
        )
        2 -> Quadruple(
            listOf(Color(0xFFEEEEEE), Color(0xFFA0A0A0)),
            Color(0xFFFFFFFF),
            "🥈",
            "#2"
        )
        else -> Quadruple(
            listOf(Color(0xFFE89B60), Color(0xFF9E5724)),
            Color(0xFFFFCC80),
            "🥉",
            "#3"
        )
    }

    Box(
        modifier = modifier
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(topStart = 14.dp, bottomEnd = 12.dp, topEnd = 4.dp, bottomStart = 4.dp))
            .clip(RoundedCornerShape(topStart = 14.dp, bottomEnd = 12.dp, topEnd = 4.dp, bottomStart = 4.dp))
            .background(Brush.linearGradient(gradientColors))
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.8f),
                shape = RoundedCornerShape(topStart = 14.dp, bottomEnd = 12.dp, topEnd = 4.dp, bottomStart = 4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = badgeEmoji,
                fontSize = 10.sp,
                lineHeight = 10.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = labelText,
                color = if (rank == 1) Color(0xFF4A2800) else if (rank == 2) Color(0xFF212121) else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.2.sp
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

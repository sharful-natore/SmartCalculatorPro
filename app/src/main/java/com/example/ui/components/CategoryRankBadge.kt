package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Clean Medal Badge for Top 1, 2, and 3 items in each category.
 * Rendered without background card container at top-right corner.
 */
@Composable
fun CategoryRankBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    if (rank !in 1..3) return

    val badgeEmoji = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        else -> "🥉"
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badgeEmoji,
            fontSize = 15.sp,
            lineHeight = 15.sp
        )
    }
}


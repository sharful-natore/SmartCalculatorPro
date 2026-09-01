package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AtsChecklistItem
import com.example.data.model.ChecklistStatus
import com.example.ui.theme.CalculatorThemeColors

/**
 * MODULE 3: Jetpack Compose Interactive Checklist Component
 * Renders real-time ATS / Circular Evaluation checklist with '✔' for PRESENT and 'Fix Now' for MISSING items.
 */
@Composable
fun JobMatchChecklistComponent(
    checklist: List<AtsChecklistItem>,
    themeColors: CalculatorThemeColors,
    isBn: Boolean = false,
    onFixNowClick: (AtsChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (checklist.isEmpty()) {
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val presentCount = checklist.count { it.status.equals(ChecklistStatus.PRESENT, ignoreCase = true) }
        val missingCount = checklist.size - presentCount

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBn) "ইন্টারেক্টিভ অপ্টিমাইজেশন চেকলিস্ট:" else "ATS Requirements Checklist:",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.displayText
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Present Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "✔ $presentCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Missing Badge
                if (missingCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "✕ $missingCount Missing",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        checklist.forEach { item ->
            AtsChecklistItemCard(
                item = item,
                themeColors = themeColors,
                isBn = isBn,
                onFixNowClick = { onFixNowClick(item) }
            )
        }
    }
}

@Composable
fun AtsChecklistItemCard(
    item: AtsChecklistItem,
    themeColors: CalculatorThemeColors,
    isBn: Boolean,
    onFixNowClick: () -> Unit
) {
    val isPresent = item.status.equals(ChecklistStatus.PRESENT, ignoreCase = true)
    val statusColor = if (isPresent) Color(0xFF10B981) else Color(0xFFEF4444)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = themeColors.cardBg,
        border = BorderStroke(1.dp, statusColor.copy(alpha = if (isPresent) 0.2f else 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status Icon Indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPresent) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = item.status,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Information Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.displayText,
                    lineHeight = 16.sp
                )

                val explanationText = item.explanation.ifBlank { item.detail.orEmpty() }
                if (explanationText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = explanationText,
                        fontSize = 10.5.sp,
                        color = themeColors.displayText.copy(alpha = 0.65f),
                        lineHeight = 14.sp
                    )
                }
            }

            // Fix Now Button for MISSING items
            if (!isPresent) {
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onFixNowClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = themeColors.buttonEqualBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "সমাধান" else "Fix Now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CalculatorButton(
    text: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false,
    aspectRatio: Float? = null,
    fontSize: Int = if (text.length > 3) 15 else 22,
    padding: androidx.compose.ui.unit.Dp = 4.dp,
    icon: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    repeatsOnLongPress: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    if (repeatsOnLongPress) {
        androidx.compose.runtime.LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(350)
                while (isPressed) {
                    onClick()
                    kotlinx.coroutines.delay(60)
                }
            }
        }
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.80f else 1.0f, // Deeper scale down to 0.80f
        animationSpec = spring(dampingRatio = 0.35f, stiffness = 4000f), // Even more reactive
        label = "btn_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) bgColor.copy(alpha = 0.85f) else bgColor,
        animationSpec = tween(durationMillis = 50),
        label = "btn_color"
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag("btn_${text.replace(" ", "_").replace("⁻¹", "_inv").lowercase()}")
            .padding(padding)
            .then(if (aspectRatio != null) Modifier.aspectRatio(aspectRatio) else Modifier.fillMaxHeight())
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        if (icon != null) {
            icon()
        } else {
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


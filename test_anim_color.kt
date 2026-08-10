import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun Test() {
    val glowingColors = listOf(
        Color(0xFFFF007F), Color(0xFFFF00FF), Color(0xFF8A2BE2), Color(0xFF4B0082),
        Color(0xFF0000FF), Color(0xFF1E90FF), Color(0xFF00BFFF), Color(0xFF00FFFF),
        Color(0xFF20B2AA), Color(0xFF00FF7F), Color(0xFF32CD32), Color(0xFFADFF2F),
        Color(0xFFFFFF00), Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFF4500),
        Color(0xFFFF0000), Color(0xFFDC143C), Color(0xFFFF1493), Color(0xFFFF69B4),
        Color(0xFFFF007F)
    )
    val infiniteTransition = rememberInfiniteTransition()
    val color1 by infiniteTransition.animateColor(
        initialValue = glowingColors[0],
        targetValue = glowingColors.last(),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 10000
                glowingColors.forEachIndexed { index, color ->
                    color at (10000 * index / (glowingColors.size - 1)) with LinearEasing
                }
            },
            repeatMode = RepeatMode.Restart
        )
    )
}

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun TestScreen(isExpanded: Boolean) {
    val displayWeight by animateFloatAsState(if (isExpanded) 0.85f else 1.15f)
    val keypadWeight by animateFloatAsState(if (isExpanded) 3.15f else 2.85f)
    Column {
        Box(Modifier.weight(displayWeight))
        Column(Modifier.weight(keypadWeight)) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.weight(4f)
            ) {
                Column { /* buttons */ }
            }
            Column(Modifier.weight(5f)) { /* basic buttons */ }
        }
    }
}

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun TestScreen(isExpanded: Boolean) {
    Column {
        AnimatedVisibility(
            visible = isExpanded,
            modifier = if (isExpanded) Modifier.weight(4f) else Modifier,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            Box(Modifier.fillMaxSize())
        }
        Box(Modifier.weight(5f))
    }
}

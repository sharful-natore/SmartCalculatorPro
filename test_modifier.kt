import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

fun Modifier.slideInExpand(expansionFraction: Float) = this.layout { measurable, constraints ->
    if (expansionFraction <= 0.01f) {
        val placeable = measurable.measure(constraints)
        return@layout layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(0, 0)
        }
    }
    
    val targetHeight = (constraints.maxHeight / expansionFraction).toInt()
    val unconstrained = constraints.copy(minHeight = targetHeight, maxHeight = targetHeight)
    val placeable = measurable.measure(unconstrained)
    
    layout(constraints.maxWidth, constraints.maxHeight) {
        // Slide in from top (anchored to bottom of the visible area? No, anchored to its bottom)
        // If we want it to slide down, its bottom edge should be at constraints.maxHeight
        val yOffset = constraints.maxHeight - placeable.height
        placeable.placeRelative(0, yOffset)
    }
}

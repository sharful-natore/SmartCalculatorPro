import re

with open('app/src/main/java/com/example/ui/screens/BasicScientificScreen.kt', 'r') as f:
    content = f.read()

# Replace the scientific column modifier
old_modifier = """
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(4f * expansionFraction)
                        .graphicsLayer {
                            alpha = expansionFraction
                        },
                    verticalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
"""

new_modifier = """
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(4f * expansionFraction)
                        .graphicsLayer {
                            alpha = expansionFraction
                            clip = true
                        }
                        .androidx.compose.ui.layout.layout { measurable, constraints ->
                            if (expansionFraction <= 0.01f) {
                                val p = measurable.measure(constraints)
                                layout(constraints.maxWidth, constraints.maxHeight) { p.placeRelative(0, 0) }
                            } else {
                                val targetHeight = (constraints.maxHeight / expansionFraction).toInt()
                                val unconstrained = constraints.copy(minHeight = targetHeight, maxHeight = targetHeight)
                                val p = measurable.measure(unconstrained)
                                layout(constraints.maxWidth, constraints.maxHeight) {
                                    val yOffset = constraints.maxHeight - p.height
                                    p.placeRelative(0, yOffset)
                                }
                            }
                        },
                    verticalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
"""

content = content.replace(old_modifier, new_modifier)

with open('app/src/main/java/com/example/ui/screens/BasicScientificScreen.kt', 'w') as f:
    f.write(content)

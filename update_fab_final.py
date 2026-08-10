import re

with open('app/src/main/java/com/example/ui/MainApp.kt', 'r') as f:
    content = f.read()

# Replace the FAB colors, duration and scale
pattern = re.compile(r'val glowingColors1 = remember \{.*?val iconScale by infiniteTransition\.animateFloat\(.*?label = "iconScale"\n                \)', re.DOTALL)

new_code = """val glowingColors1 = remember {
                    listOf(
                        Color(0xFFFF5E8E), Color(0xFFFF7EB3), Color(0xFFFF99CC), Color(0xFFE8B0FF),
                        Color(0xFFD0A1FF), Color(0xFFB591FF), Color(0xFF9EA3FF), Color(0xFF8AB5FF),
                        Color(0xFF7CC7FF), Color(0xFF6ED8FF), Color(0xFF6CF0FF), Color(0xFF75FFEC),
                        Color(0xFF82FFD2), Color(0xFF9CFFB3), Color(0xFFB3FF99), Color(0xFFCDFF82),
                        Color(0xFFE6FF6E), Color(0xFFFFF275), Color(0xFFFFDF70), Color(0xFFFFC66C),
                        Color(0xFFFFA568), Color(0xFFFF856B), Color(0xFFFF6D7A), Color(0xFFFF5E8E) // Loop back
                    )
                }
                val glowingColors2 = remember { glowingColors1.drop(12) + glowingColors1.take(12) + listOf(glowingColors1[12]) }

                val duration = 30000

                val color1 by infiniteTransition.animateColor(
                    initialValue = glowingColors1.first(),
                    targetValue = glowingColors1.last(),
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = duration
                            glowingColors1.forEachIndexed { index, color ->
                                color at (duration * index / (glowingColors1.size - 1)) with LinearEasing
                            }
                        },
                        repeatMode = RepeatMode.Restart
                    ), label = "color1"
                )
                
                val color2 by infiniteTransition.animateColor(
                    initialValue = glowingColors2.first(),
                    targetValue = glowingColors2.last(),
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = duration
                            glowingColors2.forEachIndexed { index, color ->
                                color at (duration * index / (glowingColors2.size - 1)) with LinearEasing
                            }
                        },
                        repeatMode = RepeatMode.Restart
                    ), label = "color2"
                )
                
                val iconScale by infiniteTransition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "iconScale"
                )"""

content = pattern.sub(new_code, content)

# Replace the FAB background and padding
content = content.replace(
    '.background(themeColors.navBarBg, androidx.compose.foundation.shape.CircleShape)',
    '.background(Color.White, androidx.compose.foundation.shape.CircleShape)'
)
content = content.replace(
    '.padding(3.dp) // Border thickness',
    '.padding(4.dp) // Border thickness'
)

# And add LinearOutSlowInEasing import if missing
if 'import androidx.compose.animation.core.LinearOutSlowInEasing' not in content:
    content = content.replace(
        'import androidx.compose.animation.core.LinearEasing',
        'import androidx.compose.animation.core.LinearEasing\nimport androidx.compose.animation.core.LinearOutSlowInEasing'
    )

with open('app/src/main/java/com/example/ui/MainApp.kt', 'w') as f:
    f.write(content)
print("Updated FAB")

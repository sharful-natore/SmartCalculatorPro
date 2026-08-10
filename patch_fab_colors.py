import re

with open('app/src/main/java/com/example/ui/MainApp.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'val glowingColors1 = remember \{.*?label = "iconScale"\n                \)', re.DOTALL)

new_code = """val glowingColors1 = remember {
                    listOf(
                        Color(0xFFFF0055), Color(0xFFFF0099), Color(0xFFFF00DD), Color(0xFFCC00FF),
                        Color(0xFF8800FF), Color(0xFF4400FF), Color(0xFF0022FF), Color(0xFF0066FF),
                        Color(0xFF00AAFF), Color(0xFF00EEFF), Color(0xFF00FFCC), Color(0xFF00FF88),
                        Color(0xFF00FF22), Color(0xFF66FF00), Color(0xFFBBFF00), Color(0xFFFFEE00),
                        Color(0xFFFFBB00), Color(0xFFFF8800), Color(0xFFFF4400), Color(0xFFFF0000),
                        Color(0xFFFF0055) // Loop back
                    )
                }
                val glowingColors2 = remember { glowingColors1.drop(10) + glowingColors1.take(10) + listOf(glowingColors1[10]) }

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

content = content.replace(
    '.offset(y = (-13).dp) // 72 (nav) + 5 (outside) - 64 (fab) = 13 (so 5dp sticks out)',
    '.offset(y = (-18).dp) // 72 (nav) + 10 (outside) - 64 (fab) = 18 (so 10dp sticks out)'
)

with open('app/src/main/java/com/example/ui/MainApp.kt', 'w') as f:
    f.write(content)
print("Updated FAB in MainApp")

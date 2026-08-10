import re

with open('app/src/main/java/com/example/ui/MainApp.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'val infiniteTransition = rememberInfiniteTransition\(label = "ai_gradient"\).*?\.clip\(androidx\.compose\.foundation\.shape\.CircleShape\)\s*\.drawBehind \{.*?\n                        }\s*\.clickable\(', re.DOTALL)

new_fab = """val infiniteTransition = rememberInfiniteTransition(label = "ai_gradient")
                
                val glowingColors1 = remember {
                    listOf(
                        Color(0xFFFF007F), Color(0xFFFF00FF), Color(0xFF8A2BE2), Color(0xFF4B0082),
                        Color(0xFF0000FF), Color(0xFF1E90FF), Color(0xFF00BFFF), Color(0xFF00FFFF),
                        Color(0xFF20B2AA), Color(0xFF00FF7F), Color(0xFF32CD32), Color(0xFFADFF2F),
                        Color(0xFFFFFF00), Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFF4500),
                        Color(0xFFFF0000), Color(0xFFDC143C), Color(0xFFFF1493), Color(0xFFFF69B4),
                        Color(0xFFFF007F) // Loop back
                    )
                }
                val glowingColors2 = remember { glowingColors1.drop(7) + glowingColors1.take(7) + listOf(glowingColors1[7]) }

                val color1 by infiniteTransition.animateColor(
                    initialValue = glowingColors1.first(),
                    targetValue = glowingColors1.last(),
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 10000
                            glowingColors1.forEachIndexed { index, color ->
                                color at (10000 * index / (glowingColors1.size - 1)) with LinearEasing
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
                            durationMillis = 10000
                            glowingColors2.forEachIndexed { index, color ->
                                color at (10000 * index / (glowingColors2.size - 1)) with LinearEasing
                            }
                        },
                        repeatMode = RepeatMode.Restart
                    ), label = "color2"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-13).dp) // 72 (nav) + 5 (outside) - 64 (fab) = 13 (so 5dp sticks out)
                        .size(64.dp)
                        .shadow(elevation = 6.dp, shape = androidx.compose.foundation.shape.CircleShape)
                        .background(themeColors.navBarBg, androidx.compose.foundation.shape.CircleShape)
                        .padding(3.dp) // Border thickness
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .drawBehind {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(color1, color2),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height)
                                ),
                                size = size
                            )
                            // Inner subtle glow/blur overlay to make it look smooth and blurry
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha=0.3f), Color.Transparent),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = size.width / 2f
                                )
                            )
                        }
                        .clickable("""

if pattern.search(content):
    content = pattern.sub(new_fab, content)
    with open('app/src/main/java/com/example/ui/MainApp.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Pattern not found!")

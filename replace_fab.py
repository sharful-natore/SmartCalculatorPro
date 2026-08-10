import re

with open('app/src/main/java/com/example/ui/MainApp.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'val infiniteTransition = rememberInfiniteTransition\(label = "ai_gradient"\).*?\.clip\(androidx\.compose\.foundation\.shape\.CircleShape\)\s*\.drawBehind \{.*?\n                        }\s*\.clickable\(', re.DOTALL)

new_fab = """val infiniteTransition = rememberInfiniteTransition(label = "ai_gradient")
                
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "angle"
                )
                
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
                val glowingColors3 = remember { glowingColors1.drop(14) + glowingColors1.take(14) + listOf(glowingColors1[14]) }

                val color1 by infiniteTransition.animateColor(
                    initialValue = glowingColors1.first(),
                    targetValue = glowingColors1.last(),
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 6000
                            glowingColors1.forEachIndexed { index, color ->
                                color at (6000 * index / (glowingColors1.size - 1)) with LinearEasing
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
                            durationMillis = 6000
                            glowingColors2.forEachIndexed { index, color ->
                                color at (6000 * index / (glowingColors2.size - 1)) with LinearEasing
                            }
                        },
                        repeatMode = RepeatMode.Restart
                    ), label = "color2"
                )
                
                val color3 by infiniteTransition.animateColor(
                    initialValue = glowingColors3.first(),
                    targetValue = glowingColors3.last(),
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 6000
                            glowingColors3.forEachIndexed { index, color ->
                                color at (6000 * index / (glowingColors3.size - 1)) with LinearEasing
                            }
                        },
                        repeatMode = RepeatMode.Restart
                    ), label = "color3"
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
                            val w = size.width
                            val h = size.height
                            val radius = w * 0.75f
                            
                            // Base color so screen blend doesn't just show white or transparent
                            drawRect(Color(0xFF1E1E2E))
                            
                            val cx1 = w / 2f + kotlin.math.cos(angle * Math.PI / 180f).toFloat() * (w * 0.25f)
                            val cy1 = h / 2f + kotlin.math.sin(angle * Math.PI / 180f).toFloat() * (h * 0.25f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(color1, color1.copy(alpha = 0f)),
                                    center = Offset(cx1, cy1),
                                    radius = radius
                                ),
                                blendMode = androidx.compose.ui.graphics.BlendMode.Screen
                            )
                            
                            val cx2 = w / 2f + kotlin.math.cos((angle + 120f) * Math.PI / 180f).toFloat() * (w * 0.25f)
                            val cy2 = h / 2f + kotlin.math.sin((angle + 120f) * Math.PI / 180f).toFloat() * (h * 0.25f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(color2, color2.copy(alpha = 0f)),
                                    center = Offset(cx2, cy2),
                                    radius = radius
                                ),
                                blendMode = androidx.compose.ui.graphics.BlendMode.Screen
                            )
                            
                            val cx3 = w / 2f + kotlin.math.cos((angle + 240f) * Math.PI / 180f).toFloat() * (w * 0.25f)
                            val cy3 = h / 2f + kotlin.math.sin((angle + 240f) * Math.PI / 180f).toFloat() * (h * 0.25f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(color3, color3.copy(alpha = 0f)),
                                    center = Offset(cx3, cy3),
                                    radius = radius
                                ),
                                blendMode = androidx.compose.ui.graphics.BlendMode.Screen
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


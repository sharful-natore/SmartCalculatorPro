import re

with open('app/src/main/java/com/example/ui/MainApp.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'val infiniteTransition = rememberInfiniteTransition\(label = "ai_gradient"\).*?modifier = Modifier\.size\(28\.dp\)\n                    \)', re.DOTALL)

new_fab = """val infiniteTransition = rememberInfiniteTransition(label = "ai_gradient")
                
                val glowingColors1 = remember {
                    listOf(
                        Color(0xFFFF3399), Color(0xFFFF33FF), Color(0xFFB983FF), Color(0xFF9933FF),
                        Color(0xFF3366FF), Color(0xFF3399FF), Color(0xFF33CCFF), Color(0xFF33FFFF),
                        Color(0xFF48D1CC), Color(0xFF33FF99), Color(0xFF66FF66), Color(0xFFC0FF3E),
                        Color(0xFFFFFF33), Color(0xFFFFE633), Color(0xFFFFB333), Color(0xFFFF6633),
                        Color(0xFFFF3333), Color(0xFFFF4D79), Color(0xFFFF3399), Color(0xFFFF80DF),
                        Color(0xFFFF3399) // Loop back
                    )
                }
                val glowingColors2 = remember { glowingColors1.drop(10) + glowingColors1.take(10) + listOf(glowingColors1[10]) }

                val duration = 24000

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
                    initialValue = 0.9f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "iconScale"
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
                        .clickable(
                            interactionSource = fabInteractionSource,
                            indication = ripple(bounded = false),
                            onClick = {
                                try {
                                    if (!viewModel.showAiChat) {
                                        viewModel.showAiChat = true
                                    }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                    viewModel.reportError("AI Chat FAB error: ${e.localizedMessage ?: e.javaClass.simpleName}")
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp).graphicsLayer(
                            scaleX = iconScale,
                            scaleY = iconScale
                        )
                    )"""

if pattern.search(content):
    content = pattern.sub(new_fab, content)
    with open('app/src/main/java/com/example/ui/MainApp.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Pattern not found!")

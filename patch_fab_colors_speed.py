import re

with open('app/src/main/java/com/example/ui/MainApp.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'val glowingColors1 = remember \{.*?val duration = 30000', re.DOTALL)

new_code = """val glowingColors1 = remember {
                    listOf(
                        Color(0xFFFF0000), Color(0xFFFF2200), Color(0xFFFF4400), Color(0xFFFF6600),
                        Color(0xFFFF8800), Color(0xFFFFAA00), Color(0xFFFFCC00), Color(0xFFFFEE00),
                        Color(0xFFDDFF00), Color(0xFFBBFF00), Color(0xFF99FF00), Color(0xFF77FF00),
                        Color(0xFF55FF00), Color(0xFF33FF00), Color(0xFF11FF00), Color(0xFF00FF22),
                        Color(0xFF00FF55), Color(0xFF00FF88), Color(0xFF00FFBB), Color(0xFF00FFEE),
                        Color(0xFF00DDFF), Color(0xFF00BBFF), Color(0xFF0099FF), Color(0xFF0077FF),
                        Color(0xFF0055FF), Color(0xFF0033FF), Color(0xFF0011FF), Color(0xFF2200FF),
                        Color(0xFF4400FF), Color(0xFF6600FF), Color(0xFF8800FF), Color(0xFFAA00FF),
                        Color(0xFFCC00FF), Color(0xFFEE00FF), Color(0xFFFF00DD), Color(0xFFFF00BB),
                        Color(0xFFFF0099), Color(0xFFFF0077), Color(0xFFFF0055), Color(0xFFFF0022),
                        Color(0xFFFF0000) // Loop back
                    )
                }
                val glowingColors2 = remember { glowingColors1.drop(20) + glowingColors1.take(20) + listOf(glowingColors1[20]) }

                val duration = 18000"""

content = pattern.sub(new_code, content)

with open('app/src/main/java/com/example/ui/MainApp.kt', 'w') as f:
    f.write(content)
print("Updated FAB in MainApp")

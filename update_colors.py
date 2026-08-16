import re

with open("app/src/main/java/com/example/ui/namaz/PrayerStepCard.kt", "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace(
    "color = if (themeColors.isDark) Color(0xFF6EE7B7) else Color(0xFF047857)",
    "color = if (themeColors.isDark) Color(0xFF34D399) else Color(0xFF059669)"
)

content = content.replace(
    "color = themeColors.displayText.copy(alpha = 0.85f)",
    "color = if (themeColors.isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)"
)

content = content.replace(
    "color = themeColors.displayText.copy(alpha = 0.75f)",
    "color = if (themeColors.isDark) Color(0xFFFBBF24) else Color(0xFFD97706)"
)

with open("app/src/main/java/com/example/ui/namaz/PrayerStepCard.kt", "w", encoding="utf-8") as f:
    f.write(content)


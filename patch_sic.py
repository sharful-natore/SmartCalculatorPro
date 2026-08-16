import re

with open("app/src/main/java/com/example/ui/screens/IslamicToolsComposables.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Sehri card side-by-side
old_sehri_card = """Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                )"""
new_sehri_card = """Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.background),
                    border = BorderStroke(1.dp, themeColors.displayText.copy(alpha = 0.08f))
                )"""
content = content.replace(old_sehri_card, new_sehri_card)

# Sehri text color
content = content.replace('color = Color.White.copy(alpha = 0.8f)', 'color = themeColors.displayText.copy(alpha = 0.8f)')
# The Iftar side-by-side is similar, wait, the replace above might replace all of them!
# Let's check how many times "color = Color.White.copy(alpha = 0.8f)" occurs.

with open("app/src/main/java/com/example/ui/screens/IslamicToolsComposables.kt", "w", encoding="utf-8") as f:
    f.write(content)

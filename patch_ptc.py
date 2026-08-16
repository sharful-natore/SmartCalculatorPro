import re

with open("app/src/main/java/com/example/ui/screens/IslamicToolsComposables.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Replace PrayerTimesCard's Live Banner Colors
old_banner_color = """colors = CardDefaults.cardColors(
                    containerColor = if (isForbidden) Color(0xFFEF4444) else themeColors.buttonEqualBg
                )"""
new_banner_color = """colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A)
                )"""
content = content.replace(old_banner_color, new_banner_color)

# Replace the Surface for Active/Forbidden tag in PrayerTimesCard to match Sehri style
old_tag_color = """color = Color.White.copy(alpha = 0.25f)"""
new_tag_color = """color = (if (isForbidden) Color(0xFFEF4444) else Color(0xFF38BDF8)).copy(alpha = 0.25f)"""
content = content.replace(old_tag_color, new_tag_color)

old_tag_text_color = """color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)"""
new_tag_text_color = """color = if (isForbidden) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)"""
content = content.replace(old_tag_text_color, new_tag_text_color)

# Replace timer icon tint
old_icon_tint = """tint = Color.White,
                                modifier = Modifier.size(20.dp)"""
new_icon_tint = """tint = if (isForbidden) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)"""
content = content.replace(old_icon_tint, new_icon_tint)

# In the timetable list, let's fix the layout of the forbidden times which look messy vertically.
# Currently the list item has a column with Name + Active tag, and then the time text on the right.
# Actually, the messy part in the screenshot is "সূর্যোদয় (নিষিদ্ধ সময়)" being multi-line because of small width? No, it's:
# "সূর্য ওঠার পর ১৬ মিনিট নামাজ পড়া নিষেধ" which is a long note.
# In the original:
# Text(text = if (isBn) item.noteBn else ..., fontSize = 10.5.sp ... padding(start = 25.dp, top = 2.dp))
# This is fine, but it seems cluttered.

with open("app/src/main/java/com/example/ui/screens/IslamicToolsComposables.kt", "w", encoding="utf-8") as f:
    f.write(content)

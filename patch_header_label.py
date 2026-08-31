import re

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

# Replace the static subtitle with the current profile label
new_subtitle = """
                    Text(
                        text = cvData.profileLabel.ifBlank { if (isBn) "স্মার্ট এআই এবং প্রফেশনাল টেমপ্লেট" else "Smart AI & Professional Templates" },
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = themeColors.displayText.copy(alpha = 0.6f)
                    )
"""

content = re.sub(r'Text\(\s*text = if \(isBn\) "স্মার্ট এআই এবং প্রফেশনাল টেমপ্লেট" else "Smart AI & Professional Templates",\s*fontSize = 11\.5\.sp,\s*color = themeColors\.displayText\.copy\(alpha = 0\.6f\)\s*\)', new_subtitle.strip(), content)

# Remove the CvCustomTextField for Profile Label
content = re.sub(r'CvCustomTextField\(\s*label = if \(isBn\) "প্রোফাইলের নাম \(লেবেল\)" else "Profile Label",\s*value = cvData\.profileLabel,\s*onValueChange = \{ onCvDataChange\(cvData\.copy\(profileLabel = it\)\) \},\s*themeColors = themeColors,\s*placeholderText = if \(isBn\) "যেমন: সফটওয়্যার ইঞ্জিনিয়ার প্রোফাইল" else "e.g., Software Engineer Profile"\s*\)', '', content)

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
    f.write(content)

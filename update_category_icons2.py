import re

def update_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Pattern for the Badge Box
    # We look for `.background(themeColors.buttonEqualBg)` followed by the Text color `Color.White`
    # Let's match from .background(themeColors.buttonEqualBg) to color = Color.White
    badge_box_pattern = r'(\.clip\(CircleShape\)\s*\n\s*\.background\()themeColors\.buttonEqualBg(\)\s*\n\s*\.padding\(horizontal\s*=\s*6\.dp,\s*vertical\s*=\s*2\.dp\),\s*\n\s*contentAlignment\s*=\s*Alignment\.Center\s*\n\s*\)\s*{\s*\n\s*Text\(.*?color\s*=\s*)Color\.White'
    
    content = re.sub(badge_box_pattern, r'\g<1>themeColors.buttonEqualBg.copy(alpha = 0.15f)\g<2>themeColors.buttonEqualBg', content, flags=re.DOTALL)

    with open(filepath, 'w') as f:
        f.write(content)

update_file("app/src/main/java/com/example/ui/screens/DashboardScreen.kt")
update_file("app/src/main/java/com/example/ui/screens/SmartConverterScreen.kt")


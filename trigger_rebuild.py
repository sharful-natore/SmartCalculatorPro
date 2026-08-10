import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

if '// Force rebuild' not in content:
    content = content.replace('class MainActivity', '// Force rebuild to refresh emulator\nclass MainActivity')
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(content)

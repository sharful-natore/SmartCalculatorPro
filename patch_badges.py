import re

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r") as f:
    content = f.read()

# Replace history badge
content = re.sub(
    r'\.size\(18\.dp\)\s*\.align\(Alignment\.TopEnd\)\s*\.offset\(x = 4\.dp, y = \(-4\)\.dp\)\s*\.clip\(CircleShape\)',
    '.size(20.dp)\\n                                    .align(Alignment.TopEnd)\\n                                    .offset(x = 2.dp, y = (-2).dp)\\n                                    .clip(CircleShape)',
    content
)

# Fix Line Height and sizing in badge Text to ensure centering
content = re.sub(
    r'fontSize = 10\.sp,\s*fontWeight = FontWeight\.Bold,\s*textAlign = TextAlign\.Center',
    'fontSize = 10.sp,\\n                                    fontWeight = FontWeight.Bold,\\n                                    textAlign = TextAlign.Center,\\n                                    lineHeight = 10.sp',
    content
)

with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w") as f:
    f.write(content)

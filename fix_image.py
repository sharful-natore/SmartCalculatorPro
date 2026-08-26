import re

with open("app/src/main/java/com/example/ui/MainApp.kt", "r") as f:
    content = f.read()

# Remove the LaunchedEffect and manual download
download_block_pattern = r'val context = LocalContext\.current\s*\n\s*val localPhotoFile = remember { java\.io\.File\(context\.filesDir, "developer_photo\.jpg"\) }\s*\n\s*var photoDownloaded by remember { mutableStateOf\(localPhotoFile\.exists\(\)\) }\s*\n\s*LaunchedEffect\(Unit\) \{.*?\s*\}\s*\n\s*\}\s*\n\s*\}\s*\n\s*Card\('

content = re.sub(download_block_pattern, r'val context = LocalContext.current\n                        Card(', content, flags=re.DOTALL)


# Fix the border width and ImageRequest
# .border(1.5.dp -> .border(2.5.dp
# .data(if (photoDownloaded)... ) -> .data("https://www.dropbox.com/scl/fi/io67lcl16o1wddcq4yx4m/Dev_photo.jpg?rlkey=erlthhlxwjhbgtd2w3tv9jbvv&st=djqdym2s&dl=1")

content = content.replace('.border(1.5.dp, themeColors.buttonEqualBg, CircleShape)', '.border(2.5.dp, themeColors.buttonEqualBg, CircleShape)')
content = content.replace('.data(if (photoDownloaded) localPhotoFile else "https://www.dropbox.com/scl/fi/io67lcl16o1wddcq4yx4m/Dev_photo.jpg?rlkey=erlthhlxwjhbgtd2w3tv9jbvv&st=djqdym2s&dl=1")', '.data("https://www.dropbox.com/scl/fi/io67lcl16o1wddcq4yx4m/Dev_photo.jpg?rlkey=erlthhlxwjhbgtd2w3tv9jbvv&st=djqdym2s&dl=1")')


with open("app/src/main/java/com/example/ui/MainApp.kt", "w") as f:
    f.write(content)

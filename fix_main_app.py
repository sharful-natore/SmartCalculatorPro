import re

with open("app/src/main/java/com/example/ui/MainApp.kt", "r") as f:
    content = f.read()

pattern = r'val painter = rememberAsyncImagePainter\([\s\S]*?\.build\(\)\s*\)'
replacement = """val painter = rememberAsyncImagePainter(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data("https://www.dropbox.com/scl/fi/io67lcl16o1wddcq4yx4m/Dev_photo.jpg?rlkey=erlthhlxwjhbgtd2w3tv9jbvv&st=djqdym2s&dl=1")
                                                .crossfade(false)
                                                .build()
                                        )"""
                                        
content = re.sub(pattern, replacement, content)

content = content.replace("painter.state is coil.compose.AsyncImagePainter.State.Success || photoDownloaded", "painter.state is coil.compose.AsyncImagePainter.State.Success")

with open("app/src/main/java/com/example/ui/MainApp.kt", "w") as f:
    f.write(content)

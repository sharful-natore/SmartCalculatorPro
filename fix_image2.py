import re

with open("app/src/main/java/com/example/ui/MainApp.kt", "r") as f:
    content = f.read()

target = """                                    // Circular Developer Photo with nice border and design
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.buttonEqualBg.copy(alpha = 0.12f))
                                            .border(2.5.dp, themeColors.buttonEqualBg, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val painter = rememberAsyncImagePainter(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data("https://www.dropbox.com/scl/fi/io67lcl16o1wddcq4yx4m/Dev_photo.jpg?rlkey=erlthhlxwjhbgtd2w3tv9jbvv&st=djqdym2s&dl=1")
                                                .crossfade(true)
                                                .listener(
                                                    onSuccess = { _, result ->
                                                        if (!photoDownloaded) {
                                                            val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                                                            if (bitmap != null) {
                                                                try {
                                                                    java.io.FileOutputStream(localPhotoFile).use { out ->
                                                                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                                                                    }
                                                                    photoDownloaded = true
                                                                } catch (e: Exception) {
                                                                    e.printStackTrace()
                                                                }
                                                            }
                                                        }
                                                    }
                                                )
                                                .build()
                                        )
                                        if (painter.state is coil.compose.AsyncImagePainter.State.Success || photoDownloaded) {
                                            Image(
                                                painter = painter,
                                                contentDescription = "Developer Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Avatar Placeholder",
                                                tint = themeColors.buttonEqualBg,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }"""

replacement = """                                    // Circular Developer Photo with nice border and design
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.buttonEqualBg.copy(alpha = 0.12f))
                                            .border(2.5.dp, themeColors.buttonEqualBg, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val painter = rememberAsyncImagePainter(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data("https://www.dropbox.com/scl/fi/io67lcl16o1wddcq4yx4m/Dev_photo.jpg?rlkey=erlthhlxwjhbgtd2w3tv9jbvv&st=djqdym2s&dl=1")
                                                .crossfade(false) // Disable crossfade to stop blinking
                                                .build()
                                        )
                                        if (painter.state is coil.compose.AsyncImagePainter.State.Loading || painter.state is coil.compose.AsyncImagePainter.State.Empty) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Avatar Placeholder",
                                                tint = themeColors.buttonEqualBg,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Image(
                                                painter = painter,
                                                contentDescription = "Developer Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/MainApp.kt", "w") as f:
    f.write(content)

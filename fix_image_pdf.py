import re
with open("app/src/main/java/com/example/ui/screens/tools/PdfTools.kt", "r") as f:
    content = f.read()

replacement = """                                        // Thumbnail
                                        coil.compose.AsyncImage(
                                            model = imgItem.uri,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        OutlinedTextField(
                                            value = imgItem.title,
                                            onValueChange = { newTitle ->
                                                selectedImages[idx] = imgItem.copy(title = newTitle)
                                            },
                                            placeholder = { Text(if (isBn) "শিরোনাম (ঐচ্ছিক)" else "Title (Optional)", fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = themeColors.buttonEqualBg,
                                                unfocusedBorderColor = themeColors.displayText.copy(alpha = 0.3f),
                                                focusedTextColor = themeColors.displayText,
                                                unfocusedTextColor = themeColors.displayText
                                            )
                                        )
                                        
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            if (idx > 0) {
                                                IconButton(
                                                    onClick = { 
                                                        val temp = selectedImages[idx]
                                                        selectedImages[idx] = selectedImages[idx - 1]
                                                        selectedImages[idx - 1] = temp
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = themeColors.buttonEqualBg)
                                                }
                                            }
                                            if (idx < selectedImages.size - 1) {
                                                IconButton(
                                                    onClick = { 
                                                        val temp = selectedImages[idx]
                                                        selectedImages[idx] = selectedImages[idx + 1]
                                                        selectedImages[idx + 1] = temp
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = themeColors.buttonEqualBg)
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = { selectedImages.removeAt(idx) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }"""

# We replace the content of the Row starting after the Text index part
# Let's write a regex that matches from `Surface(...) { Box(...) { Text(...) } } Spacer OutlinedTextField IconButton` 


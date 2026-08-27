import re

with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Replace the Header logic in HadithReaderCardItem
old_header = """                    if (!bookTitle.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = themeColors.buttonEqualBg.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = bookTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.buttonEqualBg,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    val globalNumEn = hadith.global_hadith_id.toString()
                    val globalNumBn = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(themeColors.displayText.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isBn) "হাদিস #$globalNumBn" else "Hadith #$globalNumEn",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.displayText
                        )
                    }"""

new_header = """                    val globalNumEn = hadith.global_hadith_id.toString()
                    val globalNumBn = com.example.data.islamic.AuthenticHadithDatabase.toBanglaDigit(hadith.global_hadith_id)
                    
                    val headerText = if (!bookTitle.isNullOrBlank()) {
                        if (isBn) "$bookTitle • হাদিস #$globalNumBn" else "$bookTitle • Hadith #$globalNumEn"
                    } else {
                        if (isBn) "হাদিস #$globalNumBn" else "Hadith #$globalNumEn"
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.buttonEqualBg.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = headerText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.buttonEqualBg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }"""

content = content.replace(old_header, new_header)

with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)


import re

with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Let's add chapterTitle parameter to HadithReaderCardItem
old_signature = """fun HadithReaderCardItem(
    hadith: HadithItem,
    isBookmarked: Boolean,
    readerFontSize: Float,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    bookTitle: String? = null,
    isPlaying: Boolean = false,
    onBookmarkToggle: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onListenClick: () -> Unit
) {"""

new_signature = """fun HadithReaderCardItem(
    hadith: HadithItem,
    isBookmarked: Boolean,
    readerFontSize: Float,
    isBn: Boolean,
    themeColors: CalculatorThemeColors,
    bookTitle: String? = null,
    chapterTitleBn: String? = null,
    chapterTitleEn: String? = null,
    isPlaying: Boolean = false,
    onBookmarkToggle: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onListenClick: () -> Unit
) {"""
content = content.replace(old_signature, new_signature)

# Fix hardcoded 'Chapter 3' inside HadithReaderCardItem
old_chapter_text = """                    Text(
                        text = if (isBn) "অধ্যায় ৩" else "Chapter 3",
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )"""

new_chapter_text = """                    val displayChapterBn = chapterTitleBn ?: "অধ্যায় ${hadith.chapterId}"
                    val displayChapterEn = chapterTitleEn ?: "Chapter ${hadith.chapterId}"
                    Text(
                        text = if (isBn) displayChapterBn else displayChapterEn,
                        fontSize = 12.sp,
                        color = themeColors.displayText.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )"""
content = content.replace(old_chapter_text, new_chapter_text)


with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)


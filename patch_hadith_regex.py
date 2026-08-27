import re

with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Replace the signature
content = re.sub(
    r"fun HadithReaderCardItem\((.*?)\s*bookTitle: String\? = null,\s*isPlaying: Boolean = false,",
    r"fun HadithReaderCardItem(\1 bookTitle: String? = null, chapterTitleBn: String? = null, chapterTitleEn: String? = null, isPlaying: Boolean = false,",
    content,
    flags=re.DOTALL
)

# Replace Chapter 3 text
content = re.sub(
    r'text = if \(isBn\) "অধ্যায় ৩" else "Chapter 3",',
    r'text = if (isBn) (chapterTitleBn ?: "অধ্যায় ${hadith.chapterId}") else (chapterTitleEn ?: "Chapter ${hadith.chapterId}"),',
    content
)

# Replace instances of bookTitle: bookTitle, isPlaying:
content = re.sub(
    r"bookTitle = bookTitle,\s*isPlaying =",
    r"bookTitle = bookTitle, chapterTitleBn = chapMeta?.titleBn, chapterTitleEn = chapMeta?.titleEn, isPlaying =",
    content
)

# Replace instances of bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn, isPlaying =
content = re.sub(
    r"bookTitle = if \(isBn\) currentBook\.titleBn else currentBook\.titleEn,\s*isPlaying =",
    r"bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn, chapterTitleBn = chapMeta?.titleBn, chapterTitleEn = chapMeta?.titleEn, isPlaying =",
    content
)

# Replace instances where chapMeta is not available (View 2)
# In View 2 it's chapterTitleBn = currentChap.titleBn
content = re.sub(
    r"bookTitle = if \(isBn\) currentBook\.titleBn else currentBook\.titleEn, chapterTitleBn = chapMeta\?\.titleBn, chapterTitleEn = chapMeta\?\.titleEn, isPlaying = isTtsSpeaking && activeTtsId == \"hadith_\$\{hadith\.id\}\",\n(.*?)onBookmarkToggle = \{\n(.*?)val bookmarkKey = \"\$\{hadith\.bookId\}_\$\{hadith\.id\}\"\n(.*?)val isBookmarked = bookmarkedSet\.contains\(bookmarkKey\)",
    # Wait, the above is getting too complex and risky. Let's do it safer.
    "",
    content
)


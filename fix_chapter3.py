import re

with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Fix the chapter 3 text
content = re.sub(
    r'text = if \(isBn\) "অধ্যায় ৩" else "Chapter 3",',
    r'text = if (isBn) (chapterTitleBn ?: "অধ্যায় ${hadith.chapterId}") else (chapterTitleEn ?: "Chapter ${hadith.chapterId}"),',
    content
)

with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)


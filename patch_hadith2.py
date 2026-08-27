import re

with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# 1. Update signature
if "chapterTitleBn: String? = null" not in content:
    content = re.sub(
        r"bookTitle: String\? = null,\s*isPlaying: Boolean = false,",
        r"bookTitle: String? = null,\n    chapterTitleBn: String? = null,\n    chapterTitleEn: String? = null,\n    isPlaying: Boolean = false,",
        content
    )
    print("Patched signature")

# 2. Update Chapter 3 hardcode
if '"অধ্যায় ৩"' in content:
    content = content.replace(
        'text = if (isBn) "অধ্যায় ৩" else "Chapter 3",',
        'text = if (isBn) (chapterTitleBn ?: "অধ্যায় ${hadith.chapterId}") else (chapterTitleEn ?: "Chapter ${hadith.chapterId}"),'
    )
    print("Patched Chapter 3")

# 3. Add chapMeta to View 0
if "val chapMeta = HadithRepository.getChaptersForBook(hadith.bookId).find" not in content.split("items(matchedHadiths")[1].split("HadithReaderCardItem")[0]:
    old_code_1 = """                                        val bookTitle = bookMeta?.let { if (isBn) it.titleBn else it.titleEn } ?: ""

                                        HadithReaderCardItem(
                                            hadith = hadith,
                                            isBookmarked = isBookmarked,
                                            readerFontSize = readerFontSize,
                                            isBn = isBn,
                                            themeColors = themeColors,
                                            bookTitle = bookTitle,
                                            isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
    new_code_1 = """                                        val bookTitle = bookMeta?.let { if (isBn) it.titleBn else it.titleEn } ?: ""
                                        val chapMeta = HadithRepository.getChaptersForBook(hadith.bookId).find { it.chapterId == hadith.chapterId }

                                        HadithReaderCardItem(
                                            hadith = hadith,
                                            isBookmarked = isBookmarked,
                                            readerFontSize = readerFontSize,
                                            isBn = isBn,
                                            themeColors = themeColors,
                                            bookTitle = bookTitle,
                                            chapterTitleBn = chapMeta?.titleBn,
                                            chapterTitleEn = chapMeta?.titleEn,
                                            isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
    if old_code_1 in content:
        content = content.replace(old_code_1, new_code_1)
        print("Patched View 0")
    else:
        print("View 0 not found")

# 4. Add chapMeta to View 1
old_code_2 = """                                        val isBookmarked = bookmarkedSet.contains(bookmarkKey)

                                        HadithReaderCardItem(
                                            hadith = hadith,
                                            isBookmarked = isBookmarked,
                                            readerFontSize = readerFontSize,
                                            isBn = isBn,
                                            themeColors = themeColors,
                                            bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                            isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
new_code_2 = """                                        val isBookmarked = bookmarkedSet.contains(bookmarkKey)
                                        val chapMeta = chapters.find { it.chapterId == hadith.chapterId }

                                        HadithReaderCardItem(
                                            hadith = hadith,
                                            isBookmarked = isBookmarked,
                                            readerFontSize = readerFontSize,
                                            isBn = isBn,
                                            themeColors = themeColors,
                                            bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                            chapterTitleBn = chapMeta?.titleBn,
                                            chapterTitleEn = chapMeta?.titleEn,
                                            isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
if old_code_2 in content:
    content = content.replace(old_code_2, new_code_2)
    print("Patched View 1")
else:
    print("View 1 not found")

# 5. Add currentChap to View 2
old_code_3 = """                                val isBookmarked = bookmarkedSet.contains(bookmarkKey)

                                HadithReaderCardItem(
                                    hadith = hadith,
                                    isBookmarked = isBookmarked,
                                    readerFontSize = readerFontSize,
                                    isBn = isBn,
                                    themeColors = themeColors,
                                    bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                    isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
new_code_3 = """                                val isBookmarked = bookmarkedSet.contains(bookmarkKey)

                                HadithReaderCardItem(
                                    hadith = hadith,
                                    isBookmarked = isBookmarked,
                                    readerFontSize = readerFontSize,
                                    isBn = isBn,
                                    themeColors = themeColors,
                                    bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                    chapterTitleBn = currentChap.titleBn,
                                    chapterTitleEn = currentChap.titleEn,
                                    isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
if old_code_3 in content:
    content = content.replace(old_code_3, new_code_3)
    print("Patched View 2")
else:
    print("View 2 not found")

# 6. Add chapMeta to Bookmarks
old_code_4 = """                                    val currentBook = HadithRepository.BOOK_LIST.find { it.id == hadith.bookId } ?: HadithRepository.BOOK_LIST[0]

                                    HadithReaderCardItem(
                                        hadith = hadith,
                                        isBookmarked = true,
                                        readerFontSize = readerFontSize,
                                        isBn = isBn,
                                        themeColors = themeColors,
                                        isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
new_code_4 = """                                    val currentBook = HadithRepository.BOOK_LIST.find { it.id == hadith.bookId } ?: HadithRepository.BOOK_LIST[0]
                                    val chapMeta = HadithRepository.getChaptersForBook(hadith.bookId).find { it.chapterId == hadith.chapterId }

                                    HadithReaderCardItem(
                                        hadith = hadith,
                                        isBookmarked = true,
                                        readerFontSize = readerFontSize,
                                        isBn = isBn,
                                        themeColors = themeColors,
                                        bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                        chapterTitleBn = chapMeta?.titleBn,
                                        chapterTitleEn = chapMeta?.titleEn,
                                        isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
if old_code_4 in content:
    content = content.replace(old_code_4, new_code_4)
    print("Patched View Bookmarks")
else:
    print("View Bookmarks not found")

with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)


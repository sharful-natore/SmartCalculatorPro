import re

with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# 1. View 0
old_1 = """                                        val bookMeta = HadithRepository.BOOK_LIST.find { it.id == hadith.bookId }
                                        val bookTitle = bookMeta?.let { if (isBn) it.titleBn else it.titleEn } ?: ""

                                        HadithReaderCardItem(
                                            hadith = hadith,
                                            isBookmarked = isBookmarked,
                                            readerFontSize = readerFontSize,
                                            isBn = isBn,
                                            themeColors = themeColors,
                                            bookTitle = bookTitle,
                                            isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
new_1 = """                                        val bookMeta = HadithRepository.BOOK_LIST.find { it.id == hadith.bookId }
                                        val bookTitle = bookMeta?.let { if (isBn) it.titleBn else it.titleEn } ?: ""
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
content = content.replace(old_1, new_1)

# 2. View 1
old_2 = """                                    items(inBookMatchedHadiths, key = { "inbook_${it.bookId}_${it.id}" }) { hadith ->
                                        val bookmarkKey = "${hadith.bookId}_${hadith.id}"
                                        val isBookmarked = bookmarkedSet.contains(bookmarkKey)

                                        HadithReaderCardItem(
                                            hadith = hadith,
                                            isBookmarked = isBookmarked,
                                            readerFontSize = readerFontSize,
                                            isBn = isBn,
                                            themeColors = themeColors,
                                            bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                            isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
new_2 = """                                    items(inBookMatchedHadiths, key = { "inbook_${it.bookId}_${it.id}" }) { hadith ->
                                        val bookmarkKey = "${hadith.bookId}_${hadith.id}"
                                        val isBookmarked = bookmarkedSet.contains(bookmarkKey)
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
content = content.replace(old_2, new_2)

# 3. View 2
old_3 = """                                HadithReaderCardItem(
                                    hadith = hadith,
                                    isBookmarked = isBookmarked,
                                    readerFontSize = readerFontSize,
                                    isBn = isBn,
                                    themeColors = themeColors,
                                    bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                    isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
new_3 = """                                HadithReaderCardItem(
                                    hadith = hadith,
                                    isBookmarked = isBookmarked,
                                    readerFontSize = readerFontSize,
                                    isBn = isBn,
                                    themeColors = themeColors,
                                    bookTitle = if (isBn) currentBook.titleBn else currentBook.titleEn,
                                    chapterTitleBn = currentChap.titleBn,
                                    chapterTitleEn = currentChap.titleEn,
                                    isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
content = content.replace(old_3, new_3)

# 4. View Bookmarks list
old_4 = """                                items(allHadiths, key = { "${it.bookId}_${it.id}" }) { hadith ->
                                    val bookmarkKey = "${hadith.bookId}_${hadith.id}"
                                    val currentBook = HadithRepository.BOOK_LIST.find { it.id == hadith.bookId } ?: HadithRepository.BOOK_LIST[0]

                                    HadithReaderCardItem(
                                        hadith = hadith,
                                        isBookmarked = true,
                                        readerFontSize = readerFontSize,
                                        isBn = isBn,
                                        themeColors = themeColors,
                                        isPlaying = isTtsSpeaking && activeTtsId == "hadith_${hadith.id}","""
new_4 = """                                items(allHadiths, key = { "${it.bookId}_${it.id}" }) { hadith ->
                                    val bookmarkKey = "${hadith.bookId}_${hadith.id}"
                                    val currentBook = HadithRepository.BOOK_LIST.find { it.id == hadith.bookId } ?: HadithRepository.BOOK_LIST[0]
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
content = content.replace(old_4, new_4)


with open("app/src/main/java/com/example/ui/islamic/HadithLibraryScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)


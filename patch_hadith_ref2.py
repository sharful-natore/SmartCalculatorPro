import re

with open("app/src/main/java/com/example/data/islamic/AuthenticHadithDatabase.kt", "r", encoding="utf-8") as f:
    content = f.read()

old_return = """        val newRefText = "$bookTitleBn, অধ্যায় ${hadith.chapterId}, হাদিস নং ${hadith.hadithNumberBn} [আন্তর্জাতিক সূচক: $collectionName $trueId]"

        return hadith.copy(
            global_hadith_id = trueId,
            collection_name = collectionName,
            book_slug = hadith.bookId,
            referenceBn = newRefText
        )"""

new_return = """        val newRefText = "$bookTitleBn (তাওহীদ: ${hadith.hadithNumberBn} | আন্তর্জাতিক সূচক: $collectionName $trueId)"

        return hadith.copy(
            global_hadith_id = trueId,
            collection_name = collectionName,
            book_slug = hadith.bookId,
            referenceBn = newRefText
        )"""

content = content.replace(old_return, new_return)

with open("app/src/main/java/com/example/data/islamic/AuthenticHadithDatabase.kt", "w", encoding="utf-8") as f:
    f.write(content)


import re

with open("app/src/main/java/com/example/data/islamic/AuthenticHadithDatabase.kt", "r", encoding="utf-8") as f:
    content = f.read()

# For dynamic references in mapHadithMetadata
old_return = """        val newRefText = "$bookTitleBn (তাওহীদ: ${hadith.hadithNumberBn} | আন্তর্জাতিক সূচক: $collectionName $trueId)"

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
if old_return != new_return:
    content = content.replace(old_return, new_return)

# Also need to fix the reference format where templates are injected manually
old_tmpl = """val refText = "$bookTitleBn: অধ্যায় $chapterId, হাদিস নং $bnNum [আন্তর্জাতিক সূচক: $bookPrefixEn $enNum]\""""
new_tmpl = """val refText = "$bookTitleBn (তাওহীদ: $bnNum | আন্তর্জাতিক সূচক: $bookPrefixEn $enNum)\""""
content = content.replace(old_tmpl, new_tmpl)

with open("app/src/main/java/com/example/data/islamic/AuthenticHadithDatabase.kt", "w", encoding="utf-8") as f:
    f.write(content)


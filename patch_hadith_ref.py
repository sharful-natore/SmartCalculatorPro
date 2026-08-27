import re

with open("app/src/main/java/com/example/data/islamic/AuthenticHadithDatabase.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Let's see mapHadithMetadata
old_return = """        return hadith.copy(
            global_hadith_id = trueId,
            collection_name = collectionName,
            book_slug = hadith.bookId
        )"""

new_return = """        val bookTitleBn = when (hadith.bookId) {
            "bukhari" -> "সহীহ আল-বুখারী"
            "muslim" -> "সহীহ মুসলিম"
            "abudawood" -> "সুনান আবু দাউদ"
            "tirmidhi" -> "জামে' আত-তিরমিজি"
            "nasai" -> "সুনান আন-নাসায়ী"
            "ibnmajah" -> "সুনান ইবনে মাজাহ"
            "riyad" -> "রিয়াদুস সালেহীন"
            else -> "সহীহ হাদিস"
        }
        val newRefText = "$bookTitleBn, অধ্যায় ${hadith.chapterId}, হাদিস নং ${hadith.hadithNumberBn} [আন্তর্জাতিক সূচক: $collectionName $trueId]"

        return hadith.copy(
            global_hadith_id = trueId,
            collection_name = collectionName,
            book_slug = hadith.bookId,
            referenceBn = newRefText
        )"""

content = content.replace(old_return, new_return)

with open("app/src/main/java/com/example/data/islamic/AuthenticHadithDatabase.kt", "w", encoding="utf-8") as f:
    f.write(content)


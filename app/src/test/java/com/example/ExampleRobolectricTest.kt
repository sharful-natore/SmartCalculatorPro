package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.islamic.HadithItem
import com.example.ui.islamic.HadithStorageManager
import com.example.ui.islamic.extractIfNumberFromText
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ToolsMate", appName)
  }

  @Test
  fun `verify hadith save, load and integrity`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testBookId = "test_book"

    // Prepare mock chapter data
    val hadiths = listOf(
      HadithItem(
        id = 1,
        bookId = testBookId,
        chapterId = 1,
        hadithNumberBn = "১",
        hadithNumberEn = "1",
        narratorBn = "উমর (রা.)",
        arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ",
        banglaText = "সকল কাজ নিয়তের উপর নির্ভরশীল। (ইসলামিক ফাউন্ডেশনঃ ১)",
        englishText = "Actions are according to intentions.",
        gradeBn = "সহীহ",
        referenceBn = "সহীহ বুখারী: ১",
        in_book_reference = 1
      ),
      HadithItem(
        id = 2,
        bookId = testBookId,
        chapterId = 1,
        hadithNumberBn = "২",
        hadithNumberEn = "2",
        narratorBn = "আয়িশা (রা.)",
        arabicText = "مَنْ أَحْدَثَ فِي أَمْرِنَا هَذَا مَا لَيْسَ فِيهِ فَهُوَ رَدٌّ",
        banglaText = "যে ব্যক্তি আমাদের দ্বীনে নতুন কিছু উদ্ভাবন করে...",
        englishText = "Whoever innovates something in this matter...",
        gradeBn = "সহীহ",
        referenceBn = "সহীহ বুখারী: ২",
        in_book_reference = 2
      )
    )

    val map = mapOf(1 to hadiths)
    val cdnSections = mapOf(1 to "কিতাবুল ঈমান")

    // Save full book data
    HadithStorageManager.saveFullBookData(context, testBookId, map, cdnSections)

    // Check downloaded state
    assertTrue("Book should be marked as downloaded", HadithStorageManager.isBookDownloaded(context, testBookId, false))

    // Check meta file exists
    val metaFile = HadithStorageManager.getMetaFile(context, testBookId)
    assertTrue("Meta file must exist", metaFile.exists())

    // Check chapters metadata
    val chapters = HadithStorageManager.getChaptersForBook(context, testBookId)
    assertTrue("Chapters must not be empty", chapters.isNotEmpty())
    assertEquals(1, chapters[0].chapterId)
    assertEquals(2, chapters[0].hadithCount)

    // Load hadiths for chapter 1
    val loadedHadiths = HadithStorageManager.loadHadithsForChapter(context, testBookId, 1)
    assertEquals(2, loadedHadiths.size)
    assertEquals("إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ", loadedHadiths[0].arabicText)
    assertEquals("সকল কাজ নিয়তের উপর নির্ভরশীল। (ইসলামিক ফাউন্ডেশনঃ ১)", loadedHadiths[0].banglaText)
    assertEquals("১", loadedHadiths[0].hadithNumberBn)
    assertEquals(1, loadedHadiths[0].in_book_reference)

    // Test IF number extraction
    val extractedIf = extractIfNumberFromText(loadedHadiths[0].banglaText)
    assertEquals("১", extractedIf)

    // Clean up
    HadithStorageManager.deleteBook(context, testBookId)
    assertFalse("Book should be deleted", HadithStorageManager.isBookDownloaded(context, testBookId, false))
  }
}

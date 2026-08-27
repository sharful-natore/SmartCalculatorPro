package com.example.data.quran

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameTranslation: String, // e.g., "The Opening" or Bangla title
    val nameBangla: String,      // e.g., "আল-ফাতেহা"
    val revelationType: String,  // "Meccan" or "Medinan"
    val numberOfAyahs: Int,
    val isAudioDownloaded: Boolean = false,
    val downloadProgress: Int = 0, // 0 - 100
    val lastDownloadError: String? = null,
    val downloadedType: String? = null // "ARABIC", "BANGLA", "BOTH"
)

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey val id: String, // format: "surahNum_ayahNum" e.g., "1_1"
    val surahNumber: Int,
    val numberInSurah: Int,
    val numberInQuran: Int,
    val textArabic: String,
    val textBangla: String,
    val textEnglish: String = "",
    val audioUrl: String,
    val wordsJson: String = "" // Optional word-by-word data as JSON string
) {
    fun getBanglaPronunciation(): String {
        return QuranBanglaPronunciation.getPronunciation(surahNumber, numberInSurah, textEnglish)
    }
}

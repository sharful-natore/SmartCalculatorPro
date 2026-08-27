package com.example.data.quran

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    @Query("SELECT * FROM surahs ORDER BY number ASC")
    fun getAllSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs WHERE number = :number LIMIT 1")
    fun getSurahByNumber(number: Int): Flow<SurahEntity?>

    @Query("SELECT * FROM surahs WHERE isAudioDownloaded = 1 ORDER BY number ASC")
    fun getDownloadedSurahs(): Flow<List<SurahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    @Query("UPDATE surahs SET isAudioDownloaded = :isDownloaded, downloadProgress = :progress, lastDownloadError = :error, downloadedType = :type WHERE number = :surahNumber")
    suspend fun updateDownloadStatus(
        surahNumber: Int, 
        isDownloaded: Boolean, 
        progress: Int, 
        error: String? = null, 
        type: String? = null
    )

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah ASC")
    fun getAyahsForSurah(surahNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah ASC")
    suspend fun getAyahsForSurahDirect(surahNumber: Int): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Query("DELETE FROM ayahs WHERE surahNumber = :surahNumber")
    suspend fun deleteAyahsForSurah(surahNumber: Int)

    @Query("SELECT * FROM ayahs WHERE textBangla LIKE '%' || :query || '%' OR textArabic LIKE '%' || :query || '%' LIMIT 100")
    fun searchAyahs(query: String): Flow<List<AyahEntity>>
}

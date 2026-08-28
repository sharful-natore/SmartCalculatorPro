package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM calculation_history WHERE type = :type ORDER BY timestamp DESC")
    fun getHistoryByType(type: String): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntry)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("UPDATE calculation_history SET customName = :customName WHERE id = :id")
    suspend fun updateCustomName(id: Long, customName: String?)

    @Query("DELETE FROM calculation_history")
    suspend fun clearHistory()
}

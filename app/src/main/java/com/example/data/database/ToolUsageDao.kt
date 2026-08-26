package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ToolUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolUsageDao {
    @Query("SELECT * FROM tool_usage")
    fun getAllUsage(): Flow<List<ToolUsage>>

    @Query("SELECT usageCount FROM tool_usage WHERE toolId = :toolId")
    suspend fun getUsageCount(toolId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUsage(usage: ToolUsage)

    @Query("UPDATE tool_usage SET usageCount = usageCount + 1 WHERE toolId = :toolId")
    suspend fun incrementUsage(toolId: String)

    @Query("INSERT OR IGNORE INTO tool_usage (toolId, usageCount) VALUES (:toolId, 1)")
    suspend fun insertInitialUsage(toolId: String)
    
    suspend fun recordUsage(toolId: String) {
        val count = getUsageCount(toolId)
        if (count == null) {
            insertOrUpdateUsage(ToolUsage(toolId, 1))
        } else {
            incrementUsage(toolId)
        }
    }
}

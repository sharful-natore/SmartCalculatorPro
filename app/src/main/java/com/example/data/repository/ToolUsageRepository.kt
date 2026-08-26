package com.example.data.repository

import com.example.data.database.ToolUsageDao
import com.example.data.model.ToolUsage
import kotlinx.coroutines.flow.Flow

class ToolUsageRepository(private val toolUsageDao: ToolUsageDao) {
    val allUsage: Flow<List<ToolUsage>> = toolUsageDao.getAllUsage()

    suspend fun recordUsage(toolId: String) {
        toolUsageDao.recordUsage(toolId)
    }

    suspend fun getAllUsageList(): List<ToolUsage> = toolUsageDao.getAllUsageList()

    suspend fun restoreUsageList(list: List<ToolUsage>) {
        list.forEach { usage ->
            toolUsageDao.insertOrUpdateUsage(usage)
        }
    }
}

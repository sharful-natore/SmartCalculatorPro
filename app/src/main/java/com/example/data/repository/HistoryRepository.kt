package com.example.data.repository

import com.example.data.database.HistoryDao
import com.example.data.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryEntry>> = historyDao.getAllHistory()

    fun getHistoryByType(type: String): Flow<List<HistoryEntry>> = historyDao.getHistoryByType(type)

    suspend fun insertHistory(entry: HistoryEntry) {
        historyDao.insertHistory(entry)
    }

    suspend fun deleteHistoryById(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun updateCustomName(id: Long, customName: String?) {
        historyDao.updateCustomName(id, customName)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }
}

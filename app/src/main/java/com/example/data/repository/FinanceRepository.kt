package com.example.data.repository

import com.example.data.database.FinanceDao
import com.example.data.model.FinanceTransaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {
    val allTransactions: Flow<List<FinanceTransaction>> = financeDao.getAllTransactions()

    suspend fun insert(transaction: FinanceTransaction) = financeDao.insertTransaction(transaction)

    suspend fun update(transaction: FinanceTransaction) = financeDao.updateTransaction(transaction)

    suspend fun deleteById(id: Long) = financeDao.deleteTransactionById(id)

    suspend fun clearAll() = financeDao.clearAllTransactions()
}

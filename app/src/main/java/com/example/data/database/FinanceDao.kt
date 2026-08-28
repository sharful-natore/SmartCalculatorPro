package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FinanceTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<FinanceTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinanceTransaction)

    @Update
    suspend fun updateTransaction(transaction: FinanceTransaction)

    @Query("DELETE FROM finance_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM finance_transactions")
    suspend fun clearAllTransactions()
}

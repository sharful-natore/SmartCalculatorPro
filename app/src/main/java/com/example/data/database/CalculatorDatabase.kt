package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FinanceTransaction
import com.example.data.model.HistoryEntry
import com.example.data.model.ToolUsage

@Database(entities = [HistoryEntry::class, ToolUsage::class, FinanceTransaction::class], version = 5, exportSchema = false)
abstract class CalculatorDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun toolUsageDao(): ToolUsageDao
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: CalculatorDatabase? = null

        fun getDatabase(context: Context): CalculatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalculatorDatabase::class.java,
                    "calculator_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

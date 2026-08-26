package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.HistoryEntry
import com.example.data.model.ToolUsage

@Database(entities = [HistoryEntry::class, ToolUsage::class], version = 3, exportSchema = false)
abstract class CalculatorDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun toolUsageDao(): ToolUsageDao

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

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finance_transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME", "EXPENSE", "DEBT", "SAVINGS"
    val subType: String = "", // "GIVEN" (পাওনা), "TAKEN" (দেনা), "DEPOSIT", "WITHDRAWAL"
    val category: String, // "বেতন", "ব্যবসা", "শপিং", "খাবার", "বিল", "চিকিৎসা", "ঋণ", "সঞ্চয়"
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSettled: Boolean = false
)

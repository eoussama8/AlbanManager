package com.example.albanmanage.HistoryScreen

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String,
    val fileName: String,
    val date: Long, // Stored as epoch milliseconds
    val totalBefore: Double,
    val totalAfter: Double,
    val productCount: Int
)
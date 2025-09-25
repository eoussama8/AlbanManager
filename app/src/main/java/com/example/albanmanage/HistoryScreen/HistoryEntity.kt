package com.example.albanmanage.HistoryScreen

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: Long = 0,
    val actionType: String,
    val fileName: String,
    val filePath: String = "", // Add this field
    val date: Long,
    val totalBefore: Double,
    val totalAfter: Double,
    val productCount: Int
)
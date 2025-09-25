package com.example.albanmanage.HistoryScreen

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {

    @Insert
    suspend fun insert(history: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY date DESC")
    suspend fun getAllHistory(): List<HistoryEntity>

    @Delete
    suspend fun delete(history: HistoryEntity)
}

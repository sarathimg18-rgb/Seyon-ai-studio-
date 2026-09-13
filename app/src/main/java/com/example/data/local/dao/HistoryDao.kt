package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

  @Query("SELECT * FROM generation_history ORDER BY timestamp DESC")
  fun getAllHistory(): Flow<List<HistoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: HistoryEntity)

  @Query("DELETE FROM generation_history WHERE id = :id")
  suspend fun deleteById(id: String)

  @Query("DELETE FROM generation_history")
  suspend fun clearAll()
}

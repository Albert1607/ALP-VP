package com.example.alp.data.local.dao

import androidx.room.*
import com.example.alp.data.local.entity.FridgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FridgeDao {
    @Query("SELECT * FROM fridge_items ORDER BY expiryDate ASC")
    fun getAllFridgeItems(): Flow<List<FridgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FridgeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FridgeEntity)

    @Query("DELETE FROM fridge_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM fridge_items")
    suspend fun clearAll()
}

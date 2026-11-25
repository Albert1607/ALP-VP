package com.example.alp.data.local.dao

import androidx.room.*
import com.example.alp.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_logs ORDER BY date DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<MealEntity>)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM meal_logs")
    suspend fun clearAll()
}

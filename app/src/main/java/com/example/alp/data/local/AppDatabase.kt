package com.example.alp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.alp.data.local.dao.FridgeDao
import com.example.alp.data.local.dao.MealDao
import com.example.alp.data.local.dao.ProfileDao
import com.example.alp.data.local.entity.FridgeEntity
import com.example.alp.data.local.entity.MealEntity
import com.example.alp.data.local.entity.ProfileEntity

@Database(
    entities = [FridgeEntity::class, MealEntity::class, ProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fridgeDao(): FridgeDao
    abstract fun mealDao(): MealDao
    abstract fun profileDao(): ProfileDao
}

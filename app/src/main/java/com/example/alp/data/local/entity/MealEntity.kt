package com.example.alp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealEntity(
    @PrimaryKey val id: String,
    val slot: String,
    val date: String,
    val totalKcal: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val itemsJson: String // Serialized items list for offline cache
)

package com.example.alp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val gender: String,
    val weightKg: Double,
    val heightCm: Double,
    val age: Int,
    val activityLevel: String,
    val calibratedActivityFactor: Double?,
    val goal: String,
    val bmr: Int,
    val tdee: Int,
    val targetKcal: Int,
    val targetProtein: Int
)

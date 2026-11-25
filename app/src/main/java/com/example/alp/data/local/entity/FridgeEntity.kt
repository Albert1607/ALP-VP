package com.example.alp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fridge_items")
data class FridgeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val weightType: String,
    val rawQuantity: Double,
    val quantityGramsOrMl: Double,
    val tareWeight: Double,
    val fillPercent: Double,
    val containerCapacity: Double,
    val densityGPerMl: Double,
    val expiryDate: String?
)

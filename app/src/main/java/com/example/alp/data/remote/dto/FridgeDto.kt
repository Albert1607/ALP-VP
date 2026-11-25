package com.example.alp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FridgeItemDto(
    val id: String,
    val userId: String,
    val name: String,
    val weightType: String = "NET",
    val rawQuantity: Double = 0.0,
    val quantityGramsOrMl: Double,
    val tareWeight: Double = 0.0,
    val fillPercent: Double = 100.0,
    val containerCapacity: Double = 0.0,
    val densityGPerMl: Double = 1.0,
    val expiryDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateFridgeItemRequest(
    val name: String? = null,
    val weightType: String? = null,
    val rawQuantity: Double? = null,
    val quantityGramsOrMl: Double? = null,
    val tareWeight: Double? = null,
    val fillPercent: Double? = null,
    val containerCapacity: Double? = null,
    val densityGPerMl: Double? = null,
    val expiryDate: String? = null,
    val textInput: String? = null,
    val imageBase64: String? = null
)

@Serializable
data class UpdateFridgeItemRequest(
    val name: String? = null,
    val weightType: String? = null,
    val rawQuantity: Double? = null,
    val quantityGramsOrMl: Double? = null,
    val tareWeight: Double? = null,
    val fillPercent: Double? = null,
    val containerCapacity: Double? = null,
    val densityGPerMl: Double? = null,
    val expiryDate: String? = null
)

@Serializable
data class FridgeListResponse(
    val items: List<FridgeItemDto>
)

@Serializable
data class FridgeSingleResponse(
    val message: String? = null,
    val item: FridgeItemDto
)

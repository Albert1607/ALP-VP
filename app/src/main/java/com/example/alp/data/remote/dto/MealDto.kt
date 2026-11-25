package com.example.alp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateMealItemInput(
    val foodName: String,
    val grams: Double,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbPer100g: Double,
    val fatPer100g: Double
)

@Serializable
data class CreateMealLogRequest(
    val slot: String, // BREAKFAST, LUNCH, DINNER
    val date: String? = null,
    val items: List<CreateMealItemInput>
)

@Serializable
data class MealItemFormattedDto(
    val id: String? = null,
    val foodId: String? = null,
    val name: String,
    val grams: Double,
    val kcalPer100g: Double = 0.0,
    val proteinPer100g: Double = 0.0,
    val carbPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

@Serializable
data class MealSummaryDto(
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

@Serializable
data class FormattedMealLogDto(
    val id: String,
    val slot: String,
    val date: String,
    val items: List<MealItemFormattedDto>,
    val summary: MealSummaryDto
)

@Serializable
data class DaySummaryDto(
    val totalKcal: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double
)

@Serializable
data class MealListResponse(
    val mealLogs: List<FormattedMealLogDto>,
    val daySummary: DaySummaryDto
)

@Serializable
data class SingleMealResponse(
    val message: String? = null,
    val meal: FormattedMealLogDto? = null
)

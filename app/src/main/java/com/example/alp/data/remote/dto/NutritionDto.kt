package com.example.alp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnalyzeNutritionRequest(
    val textInput: String? = null,
    val imageBase64: String? = null,
    val imageMimeType: String? = null
)

@Serializable
data class NutritionItemDto(
    val name: String,
    val grams: Double,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbPer100g: Double,
    val fatPer100g: Double,
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

@Serializable
data class NutritionAnalysisData(
    val items: List<NutritionItemDto>,
    val totalKcal: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double
)

@Serializable
data class AnalyzeNutritionResponse(
    val message: String? = null,
    val analysis: NutritionAnalysisData
)

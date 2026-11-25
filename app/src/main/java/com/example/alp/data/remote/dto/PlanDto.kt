package com.example.alp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeneratePlanRequest(
    val date: String? = null,
    val customTargetKcal: Double? = null,
    val customTargetProtein: Double? = null,
    val customMaxBudget: Double? = null
)

@Serializable
data class FoodDto(
    val id: String? = null,
    val name: String,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbPer100g: Double,
    val fatPer100g: Double
)

@Serializable
data class FoodPlanMealDto(
    val id: String? = null,
    val slot: String, // BREAKFAST, LUNCH, DINNER
    val foodId: String? = null,
    val food: FoodDto? = null,
    val grams: Double,
    val estimatedCostIdr: Double
)

@Serializable
data class FoodPlanDto(
    val id: String,
    val userId: String,
    val date: String,
    val targetKcal: Double,
    val targetProtein: Double,
    val totalCostIdr: Double,
    val status: String, // GENERATED, APPROVED, REJECTED, UNVERIFIED
    val meals: List<FoodPlanMealDto> = emptyList()
)

@Serializable
data class PlanValidationDto(
    val attemptsUsed: Int = 1,
    val validatedSuccessfully: Boolean = true,
    val status: String = "GENERATED",
    val targetKcal: Double = 0.0,
    val computedKcal: Double = 0.0,
    val targetProtein: Double = 0.0,
    val computedProtein: Double = 0.0,
    val maxBudgetDaily: Double = 0.0,
    val computedCost: Double = 0.0
)

@Serializable
data class GeneratePlanResponse(
    val message: String? = null,
    val plan: FoodPlanDto,
    val validation: PlanValidationDto? = null
)

@Serializable
data class PlanListResponse(
    val plans: List<FoodPlanDto>
)

@Serializable
data class UpdatePlanStatusRequest(
    val status: String // APPROVED, REJECTED
)

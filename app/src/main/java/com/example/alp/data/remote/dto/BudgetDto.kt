package com.example.alp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BudgetDto(
    val id: String? = null,
    val userId: String? = null,
    val amountIdr: Double,
    val period: String
)

@Serializable
data class SetBudgetRequest(
    val amountIdr: Double,
    val period: String // DAILY, WEEKLY, MONTHLY
)

@Serializable
data class BudgetResponse(
    val budget: BudgetDto? = null,
    val dailyLimitIdr: Double = 0.0,
    val message: String? = null
)

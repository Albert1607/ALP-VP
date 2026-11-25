package com.example.alp.data.remote.api

import com.example.alp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface BudgetApi {
    @GET("budget")
    suspend fun getBudget(): Response<BudgetResponse>

    @POST("budget")
    suspend fun setBudget(@Body request: SetBudgetRequest): Response<BudgetResponse>

    @PUT("budget")
    suspend fun updateBudget(@Body request: SetBudgetRequest): Response<BudgetResponse>
}

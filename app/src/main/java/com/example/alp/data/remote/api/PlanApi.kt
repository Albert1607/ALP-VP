package com.example.alp.data.remote.api

import com.example.alp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PlanApi {
    @POST("plan/generate")
    suspend fun generatePlan(@Body request: GeneratePlanRequest): Response<GeneratePlanResponse>

    @GET("plan")
    suspend fun getUserPlans(): Response<PlanListResponse>

    @PATCH("plan/{id}/status")
    suspend fun updatePlanStatus(
        @Path("id") id: String,
        @Body request: UpdatePlanStatusRequest
    ): Response<GeneratePlanResponse>
}

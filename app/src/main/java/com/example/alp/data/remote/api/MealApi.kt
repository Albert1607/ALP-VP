package com.example.alp.data.remote.api

import com.example.alp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MealApi {
    @POST("meals")
    suspend fun createMealLog(@Body request: CreateMealLogRequest): Response<SingleMealResponse>

    @GET("meals")
    suspend fun getMealLogs(@Query("date") date: String? = null): Response<MealListResponse>

    @GET("meals/{id}")
    suspend fun getMealLogById(@Path("id") id: String): Response<SingleMealResponse>

    @DELETE("meals/{id}")
    suspend fun deleteMealLog(@Path("id") id: String): Response<Unit>
}

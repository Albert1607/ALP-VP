package com.example.alp.data.remote.api

import com.example.alp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface FridgeApi {
    @GET("fridge")
    suspend fun getFridgeItems(): Response<FridgeListResponse>

    @POST("fridge")
    suspend fun createFridgeItem(@Body request: CreateFridgeItemRequest): Response<FridgeSingleResponse>

    @GET("fridge/{id}")
    suspend fun getFridgeItemById(@Path("id") id: String): Response<FridgeSingleResponse>

    @PUT("fridge/{id}")
    suspend fun updateFridgeItem(
        @Path("id") id: String,
        @Body request: UpdateFridgeItemRequest
    ): Response<FridgeSingleResponse>

    @DELETE("fridge/{id}")
    suspend fun deleteFridgeItem(@Path("id") id: String): Response<Unit>
}

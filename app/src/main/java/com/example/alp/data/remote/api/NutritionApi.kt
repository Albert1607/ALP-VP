package com.example.alp.data.remote.api

import com.example.alp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NutritionApi {
    @POST("nutrition/analyze")
    suspend fun analyzeNutrition(@Body request: AnalyzeNutritionRequest): Response<AnalyzeNutritionResponse>
}

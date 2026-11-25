package com.example.alp.data.repository

import com.example.alp.data.remote.api.NutritionApi
import com.example.alp.data.remote.dto.AnalyzeNutritionRequest
import com.example.alp.data.remote.dto.AnalyzeNutritionResponse
import com.example.alp.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionRepository @Inject constructor(
    private val nutritionApi: NutritionApi
) {
    suspend fun analyzeNutrition(
        textInput: String? = null,
        imageBase64: String? = null
    ): Resource<AnalyzeNutritionResponse> {
        return try {
            val response = nutritionApi.analyzeNutrition(
                AnalyzeNutritionRequest(
                    textInput = textInput,
                    imageBase64 = imageBase64
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to analyze nutrition")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error during nutrition analysis")
        }
    }
}

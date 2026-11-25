package com.example.alp.data.repository

import com.example.alp.data.remote.api.MealApi
import com.example.alp.data.remote.dto.*
import com.example.alp.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val mealApi: MealApi
) {
    suspend fun createMealLog(request: CreateMealLogRequest): Resource<SingleMealResponse> {
        return try {
            val response = mealApi.createMealLog(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to log meal")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error logging meal")
        }
    }

    suspend fun getMealLogs(date: String? = null): Resource<MealListResponse> {
        return try {
            val response = mealApi.getMealLogs(date)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to fetch meal logs")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error fetching meal logs")
        }
    }

    suspend fun deleteMealLog(id: String): Resource<Unit> {
        return try {
            val response = mealApi.deleteMealLog(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to delete meal log")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error deleting meal log")
        }
    }
}

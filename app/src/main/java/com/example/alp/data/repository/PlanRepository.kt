package com.example.alp.data.repository

import com.example.alp.data.remote.api.PlanApi
import com.example.alp.data.remote.dto.*
import com.example.alp.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepository @Inject constructor(
    private val planApi: PlanApi
) {
    suspend fun generatePlan(
        date: String? = null,
        customTargetKcal: Double? = null,
        customTargetProtein: Double? = null,
        customMaxBudget: Double? = null
    ): Resource<GeneratePlanResponse> {
        return try {
            val response = planApi.generatePlan(
                GeneratePlanRequest(
                    date = date,
                    customTargetKcal = customTargetKcal,
                    customTargetProtein = customTargetProtein,
                    customMaxBudget = customMaxBudget
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to generate plan")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error generating food plan")
        }
    }

    suspend fun getUserPlans(): Resource<PlanListResponse> {
        return try {
            val response = planApi.getUserPlans()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to fetch user plans")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error fetching plans")
        }
    }

    suspend fun updatePlanStatus(id: String, status: String): Resource<GeneratePlanResponse> {
        return try {
            val response = planApi.updatePlanStatus(id, UpdatePlanStatusRequest(status))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to update plan status")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error updating plan status")
        }
    }
}

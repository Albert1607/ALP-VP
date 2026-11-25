package com.example.alp.data.repository

import com.example.alp.data.remote.api.BudgetApi
import com.example.alp.data.remote.dto.BudgetResponse
import com.example.alp.data.remote.dto.SetBudgetRequest
import com.example.alp.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetApi: BudgetApi
) {
    suspend fun getBudget(): Resource<BudgetResponse> {
        return try {
            val response = budgetApi.getBudget()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to fetch budget")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error fetching budget")
        }
    }

    suspend fun setBudget(amountIdr: Double, period: String): Resource<BudgetResponse> {
        return try {
            val response = budgetApi.setBudget(SetBudgetRequest(amountIdr, period))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to save budget")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error saving budget")
        }
    }
}

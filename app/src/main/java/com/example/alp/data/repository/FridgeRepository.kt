package com.example.alp.data.repository

import com.example.alp.data.remote.api.FridgeApi
import com.example.alp.data.remote.dto.CreateFridgeItemRequest
import com.example.alp.data.remote.dto.FridgeItemDto
import com.example.alp.data.remote.dto.FridgeListResponse
import com.example.alp.data.remote.dto.UpdateFridgeItemRequest
import com.example.alp.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FridgeRepository @Inject constructor(
    private val fridgeApi: FridgeApi
) {
    suspend fun getFridgeItems(): Resource<FridgeListResponse> {
        return try {
            val response = fridgeApi.getFridgeItems()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to fetch fridge items")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error fetching fridge items")
        }
    }

    suspend fun createFridgeItem(request: CreateFridgeItemRequest): Resource<FridgeItemDto> {
        return try {
            val response = fridgeApi.createFridgeItem(request)
            if (response.isSuccessful && response.body()?.item != null) {
                Resource.Success(response.body()!!.item)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to create fridge item")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error creating fridge item")
        }
    }

    suspend fun updateFridgeItem(id: String, request: UpdateFridgeItemRequest): Resource<FridgeItemDto> {
        return try {
            val response = fridgeApi.updateFridgeItem(id, request)
            if (response.isSuccessful && response.body()?.item != null) {
                Resource.Success(response.body()!!.item)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to update fridge item")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error updating fridge item")
        }
    }

    suspend fun deleteFridgeItem(id: String): Resource<Unit> {
        return try {
            val response = fridgeApi.deleteFridgeItem(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to delete fridge item")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error deleting fridge item")
        }
    }
}

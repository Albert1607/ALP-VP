package com.example.alp.data.repository

import com.example.alp.data.local.TokenManager
import com.example.alp.data.remote.api.ProfileApi
import com.example.alp.data.remote.dto.*
import com.example.alp.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    private val tokenManager: TokenManager
) {
    suspend fun getProfile(): Resource<ProfileResponse> {
        return try {
            val response = profileApi.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to fetch profile")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error fetching profile")
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Resource<ProfileResponse> {
        return try {
            val response = profileApi.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error updating profile")
        }
    }

    suspend fun updateTimezone(timezone: String): Resource<AuthResponse> {
        return try {
            val response = profileApi.updateTimezone(UpdateTimezoneRequest(timezone))
            if (response.isSuccessful && response.body() != null) {
                val authRes = response.body()!!
                tokenManager.saveToken(authRes.token)
                tokenManager.saveUserInfo(authRes.user.email, authRes.user.timezone ?: timezone)
                Resource.Success(authRes)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to update timezone")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error updating timezone")
        }
    }

    suspend fun addWeightLog(weightKg: Double): Resource<WeightLogDto> {
        return try {
            val response = profileApi.addWeightLog(AddWeightLogRequest(weightKg))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.weightLog)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to record weight log")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error recording weight log")
        }
    }

    suspend fun calibrate(days: Int = 14): Resource<CalibrateResponse> {
        return try {
            val response = profileApi.calibrate(CalibrateRequest(days))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to calibrate activity factor")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error calibrating activity factor")
        }
    }

    suspend fun confirmCalibration(proposedFactor: Double): Resource<ProfileResponse> {
        return try {
            val response = profileApi.confirmCalibration(ConfirmCalibrateRequest(proposedFactor))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to confirm calibration")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error confirming calibration")
        }
    }
}

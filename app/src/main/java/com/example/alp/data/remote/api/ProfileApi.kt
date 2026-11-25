package com.example.alp.data.remote.api

import com.example.alp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ProfileResponse>

    @PUT("profile/timezone")
    suspend fun updateTimezone(@Body request: UpdateTimezoneRequest): Response<AuthResponse>

    @POST("profile/weight-log")
    suspend fun addWeightLog(@Body request: AddWeightLogRequest): Response<AddWeightLogResponse>

    @POST("profile/calibrate")
    suspend fun calibrate(@Body request: CalibrateRequest): Response<CalibrateResponse>

    @POST("profile/calibrate/confirm")
    suspend fun confirmCalibration(@Body request: ConfirmCalibrateRequest): Response<ProfileResponse>
}

package com.example.alp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BodyProfileDto(
    val id: String? = null,
    val userId: String? = null,
    val gender: String,
    val weightKg: Double,
    val heightCm: Double,
    val age: Int,
    val activityLevel: String,
    val calibratedActivityFactor: Double? = null,
    val goal: String
)

@Serializable
data class WeightLogDto(
    val id: String? = null,
    val userId: String? = null,
    val weightKg: Double,
    val date: String? = null
)

@Serializable
data class UpdateProfileRequest(
    val gender: String? = null,
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val age: Int? = null,
    val activityLevel: String? = null,
    val goal: String? = null
)

@Serializable
data class UpdateTimezoneRequest(
    val timezone: String
)

@Serializable
data class AddWeightLogRequest(
    val weightKg: Double,
    val date: String? = null
)

@Serializable
data class AddWeightLogResponse(
    val message: String? = null,
    val weightLog: WeightLogDto
)

@Serializable
data class CalibrateRequest(
    val days: Int? = null
)

@Serializable
data class ConfirmCalibrateRequest(
    val proposedFactor: Double
)

@Serializable
data class ProfileResponse(
    val profile: BodyProfileDto? = null,
    val bmr: Int = 0,
    val tdee: Int = 0,
    val targetKcal: Int = 0,
    val targetProtein: Int = 0,
    val latestWeightLog: WeightLogDto? = null
)

@Serializable
data class CalibrateResultDto(
    val periodDays: Int = 0,
    val distinctLoggedDaysCount: Int = 0,
    val initialSmoothedWeightKg: Double = 0.0,
    val finalSmoothedWeightKg: Double = 0.0,
    val weightChangeKg: Double = 0.0,
    val averageDailyLoggedKcal: Int = 0,
    val bmr: Int = 0,
    val previousActivityFactor: Double = 1.2,
    val proposedCalibratedActivityFactor: Double = 1.2,
    val proposedTargetKcal: Int = 0,
    val status: String? = null,
    val note: String? = null
)

@Serializable
data class CalibrateResponse(
    val message: String? = null,
    val calibrationResult: CalibrateResultDto
)

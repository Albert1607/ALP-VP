package com.example.alp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val timezone: String = "Asia/Jakarta",
    val gender: String? = null,
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val age: Int? = null,
    val activityLevel: String? = null,
    val goal: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val timezone: String? = "Asia/Jakarta"
)

@Serializable
data class AuthResponse(
    val message: String? = null,
    val user: UserDto,
    val token: String
)

@Serializable
data class ErrorDetailDto(
    val field: String? = null,
    val message: String? = null
)

@Serializable
data class ErrorResponse(
    val error: String? = null,
    val details: List<ErrorDetailDto>? = null
)

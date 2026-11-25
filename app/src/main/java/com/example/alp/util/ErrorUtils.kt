package com.example.alp.util

import com.example.alp.data.remote.dto.ErrorResponse
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody

fun parseError(errorBody: ResponseBody?, defaultMessage: String): String {
    val bodyStr = errorBody?.string()
    if (bodyStr.isNullOrEmpty()) return defaultMessage
    return try {
        val errorResponse = Json { ignoreUnknownKeys = true }.decodeFromString<ErrorResponse>(bodyStr)
        val firstDetail = errorResponse.details?.firstOrNull()?.message
        if (!firstDetail.isNullOrEmpty()) {
            firstDetail
        } else {
            errorResponse.error ?: defaultMessage
        }
    } catch (e: Exception) {
        bodyStr
    }
}

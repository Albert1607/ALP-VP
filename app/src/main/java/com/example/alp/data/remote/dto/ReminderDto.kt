package com.example.alp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReminderSettingDto(
    val id: String? = null,
    val userId: String? = null,
    val enabled: Boolean,
    val breakfastTime: String,
    val lunchTime: String,
    val dinnerTime: String
)

@Serializable
data class LoggedStatusDto(
    val breakfastLogged: Boolean = false,
    val lunchLogged: Boolean = false,
    val dinnerLogged: Boolean = false,
    val allLogged: Boolean = false
)

@Serializable
data class ReminderStatusResponse(
    val settings: ReminderSettingDto,
    val todayLoggedStatus: LoggedStatusDto
)

@Serializable
data class UpdateReminderRequest(
    val enabled: Boolean? = null,
    val breakfastTime: String? = null,
    val lunchTime: String? = null,
    val dinnerTime: String? = null
)

@Serializable
data class UpdateReminderResponse(
    val message: String? = null,
    val settings: ReminderSettingDto
)

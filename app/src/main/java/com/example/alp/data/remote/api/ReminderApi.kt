package com.example.alp.data.remote.api

import com.example.alp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ReminderApi {
    @GET("reminders/status")
    suspend fun getReminderStatus(): Response<ReminderStatusResponse>

    @PUT("reminders")
    suspend fun updateReminderSettings(@Body request: UpdateReminderRequest): Response<UpdateReminderResponse>
}

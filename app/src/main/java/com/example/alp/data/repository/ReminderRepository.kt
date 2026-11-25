package com.example.alp.data.repository

import com.example.alp.data.remote.api.ReminderApi
import com.example.alp.data.remote.dto.ReminderSettingDto
import com.example.alp.data.remote.dto.ReminderStatusResponse
import com.example.alp.data.remote.dto.UpdateReminderRequest
import com.example.alp.util.Resource
import com.example.alp.util.parseError
import com.example.alp.worker.ReminderScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderApi: ReminderApi,
    private val reminderScheduler: ReminderScheduler
) {
    suspend fun getReminderStatus(): Resource<ReminderStatusResponse> {
        return try {
            val response = reminderApi.getReminderStatus()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(parseError(response.errorBody(), "Failed to fetch reminder status"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error fetching reminders")
        }
    }

    suspend fun updateReminderSettings(request: UpdateReminderRequest): Resource<ReminderSettingDto> {
        return try {
            val response = reminderApi.updateReminderSettings(request)
            if (response.isSuccessful && response.body() != null) {
                reminderScheduler.scheduleReminders()
                Resource.Success(response.body()!!.settings)
            } else {
                Resource.Error(parseError(response.errorBody(), "Failed to update reminder settings"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error updating reminders")
        }
    }
}

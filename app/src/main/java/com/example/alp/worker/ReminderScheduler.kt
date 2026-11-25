package com.example.alp.worker

import android.content.Context
import androidx.work.*
import com.example.alp.data.local.TokenManager
import com.example.alp.data.remote.api.ReminderApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderApi: ReminderApi,
    private val tokenManager: TokenManager
) {
    companion object {
        const val WORK_NAME_BREAKFAST = "MealReminder_BREAKFAST"
        const val WORK_NAME_LUNCH = "MealReminder_LUNCH"
        const val WORK_NAME_DINNER = "MealReminder_DINNER"
    }

    suspend fun scheduleReminders() {
        if (!tokenManager.isLoggedIn()) return
        try {
            val response = reminderApi.getReminderStatus()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val settings = data.settings
                tokenManager.saveReminderSettings(
                    enabled = settings.enabled,
                    breakfast = settings.breakfastTime,
                    lunch = settings.lunchTime,
                    dinner = settings.dinnerTime
                )
                if (settings.enabled) {
                    scheduleStoredReminders()
                } else {
                    cancelReminders()
                }
            } else {
                scheduleStoredReminders()
            }
        } catch (e: Exception) {
            scheduleStoredReminders()
        }
    }

    fun scheduleStoredReminders() {
        if (!tokenManager.isLoggedIn()) return
        val enabled = tokenManager.getReminderEnabled()
        if (!enabled) {
            cancelReminders()
            return
        }
        val zoneId = ZoneId.of(tokenManager.getTimezone())
        scheduleSlot("BREAKFAST", WORK_NAME_BREAKFAST, tokenManager.getBreakfastTime(), zoneId)
        scheduleSlot("LUNCH", WORK_NAME_LUNCH, tokenManager.getLunchTime(), zoneId)
        scheduleSlot("DINNER", WORK_NAME_DINNER, tokenManager.getDinnerTime(), zoneId)
    }

    fun scheduleNextOccurrence(slot: String) {
        if (!tokenManager.isLoggedIn()) return
        if (!tokenManager.getReminderEnabled()) return
        val zoneId = ZoneId.of(tokenManager.getTimezone())
        val (workName, timeStr) = when (slot) {
            "BREAKFAST" -> WORK_NAME_BREAKFAST to tokenManager.getBreakfastTime()
            "LUNCH" -> WORK_NAME_LUNCH to tokenManager.getLunchTime()
            "DINNER" -> WORK_NAME_DINNER to tokenManager.getDinnerTime()
            else -> return
        }
        scheduleSlot(slot, workName, timeStr, zoneId)
    }

    private fun scheduleSlot(slot: String, workName: String, timeStr: String, zoneId: ZoneId) {
        try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val localTime = LocalTime.parse(timeStr, formatter)
            val now = ZonedDateTime.now(zoneId)
            var target = now.withHour(localTime.hour).withMinute(localTime.minute).withSecond(0).withNano(0)
            if (target.isBefore(now) || target.isEqual(now)) {
                target = target.plusDays(1)
            }
            val delayMillis = Duration.between(now, target).toMillis()

            val inputData = workDataOf("SLOT" to slot)
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelReminders() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_NAME_BREAKFAST)
        workManager.cancelUniqueWork(WORK_NAME_LUNCH)
        workManager.cancelUniqueWork(WORK_NAME_DINNER)
    }
}

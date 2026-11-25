package com.example.alp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.alp.MainActivity
import com.example.alp.R
import com.example.alp.data.repository.ReminderRepository
import com.example.alp.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val slot = inputData.getString("SLOT") ?: return Result.failure()

        // First, enqueue next occurrence from locally stored time (no network needed)
        withContext(Dispatchers.IO) {
            reminderScheduler.scheduleNextOccurrence(slot)
        }

        // Then, check logged flag
        try {
            val result = reminderRepository.getReminderStatus()
            if (result is Resource.Success && result.data != null) {
                val status = result.data.todayLoggedStatus
                val isLogged = when (slot) {
                    "BREAKFAST" -> status.breakfastLogged
                    "LUNCH" -> status.lunchLogged
                    "DINNER" -> status.dinnerLogged
                    else -> true
                }

                if (!isLogged) {
                    showNotification(slot)
                }
            } else {
                showNotification(slot, generic = true)
            }
        } catch (e: Exception) {
            showNotification(slot, generic = true)
        }

        return Result.success()
    }

    private fun showNotification(slot: String, generic: Boolean = false) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "meal_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Makan",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val (title, text) = when (slot) {
            "BREAKFAST" -> "Sarapan" to if (generic) "Waktunya mencatat makanan Anda!" else "Waktunya mencatat sarapan Anda!"
            "LUNCH" -> "Makan siang" to if (generic) "Waktunya mencatat makanan Anda!" else "Waktunya mencatat makan siang Anda!"
            "DINNER" -> "Makan malam" to if (generic) "Waktunya mencatat makanan Anda!" else "Waktunya mencatat makan malam Anda!"
            else -> "Pengingat" to "Waktunya mencatat makanan Anda!"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "nutrition")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(slotNotificationId(slot), notification)
    }

    private fun slotNotificationId(slot: String?): Int {
        return when (slot) {
            "BREAKFAST" -> 1001
            "LUNCH" -> 1002
            "DINNER" -> 1003
            else -> 1000
        }
    }
}

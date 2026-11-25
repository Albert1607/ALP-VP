package com.example.alp.data.remote

import android.content.Context
import androidx.work.WorkManager
import com.example.alp.data.local.AppDatabase
import com.example.alp.data.local.TokenManager
import com.example.alp.util.AuthEventBus
import com.example.alp.worker.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager,
    private val appDatabase: AppDatabase,
    private val authEventBus: AuthEventBus
) : Authenticator {

    private val isLoggingOut = AtomicBoolean(false)

    override fun authenticate(route: Route?, response: Response): Request? {
        val authHeader = response.request.header("Authorization")
        val path = response.request.url.encodedPath
        val isAuthPath = path.contains("auth/") || path.contains("auth")

        if (response.code == 401 && authHeader != null && !isAuthPath) {
            if (isLoggingOut.compareAndSet(false, true)) {
                tokenManager.clear()
                try {
                    WorkManager.getInstance(context).cancelUniqueWork(ReminderScheduler.WORK_NAME_BREAKFAST)
                    WorkManager.getInstance(context).cancelUniqueWork(ReminderScheduler.WORK_NAME_LUNCH)
                    WorkManager.getInstance(context).cancelUniqueWork(ReminderScheduler.WORK_NAME_DINNER)
                    WorkManager.getInstance(context).cancelUniqueWork("MealReminderWork")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        appDatabase.clearAllTables()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    authEventBus.triggerLogout()
                    isLoggingOut.set(false)
                }
            }
        }
        return null
    }
}

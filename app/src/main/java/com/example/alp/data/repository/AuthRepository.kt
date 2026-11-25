package com.example.alp.data.repository

import com.example.alp.BuildConfig
import com.example.alp.data.local.AppDatabase
import com.example.alp.data.local.TokenManager
import com.example.alp.data.remote.api.AuthApi
import com.example.alp.data.remote.dto.AuthResponse
import com.example.alp.data.remote.dto.LoginRequest
import com.example.alp.data.remote.dto.RegisterRequest
import com.example.alp.util.AuthEventBus
import com.example.alp.util.Resource
import com.example.alp.util.parseError
import com.example.alp.worker.ReminderScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val appDatabase: AppDatabase,
    private val reminderScheduler: ReminderScheduler,
    private val authEventBus: AuthEventBus
) {
    private val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun register(request: RegisterRequest): Resource<AuthResponse> {
        return try {
            val response = authApi.register(request)
            if (response.isSuccessful && response.body() != null) {
                val authRes = response.body()!!
                tokenManager.saveToken(authRes.token)
                tokenManager.saveUserInfo(authRes.user.email, authRes.user.timezone ?: "Asia/Jakarta")

                // Asynchronously schedule reminders on application scope (do not await)
                applicationScope.launch {
                    try {
                        reminderScheduler.scheduleReminders()
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) e.printStackTrace()
                    }
                }

                Resource.Success(authRes)
            } else {
                Resource.Error(parseError(response.errorBody(), "Pendaftaran gagal."))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (BuildConfig.DEBUG) e.printStackTrace()

            when (e) {
                is UnknownHostException, is ConnectException, is SocketTimeoutException, is IOException -> {
                    Resource.Error("Tidak dapat terhubung ke server. Periksa koneksi Anda.")
                }
                else -> {
                    Resource.Error("Terjadi kesalahan. Coba lagi.")
                }
            }
        }
    }

    suspend fun login(request: LoginRequest): Resource<AuthResponse> {
        return try {
            val response = authApi.login(request)
            if (response.isSuccessful && response.body() != null) {
                val authRes = response.body()!!
                tokenManager.saveToken(authRes.token)
                tokenManager.saveUserInfo(authRes.user.email, authRes.user.timezone ?: "Asia/Jakarta")

                // Asynchronously schedule reminders on application scope (do not await)
                applicationScope.launch {
                    try {
                        reminderScheduler.scheduleReminders()
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) e.printStackTrace()
                    }
                }

                Resource.Success(authRes)
            } else {
                Resource.Error(parseError(response.errorBody(), "Gagal masuk. Periksa kembali email dan kata sandi Anda."))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (BuildConfig.DEBUG) e.printStackTrace()

            when (e) {
                is UnknownHostException, is ConnectException, is SocketTimeoutException, is IOException -> {
                    Resource.Error("Tidak dapat terhubung ke server. Periksa koneksi Anda.")
                }
                else -> {
                    Resource.Error("Terjadi kesalahan. Coba lagi.")
                }
            }
        }
    }

    suspend fun logout() {
        tokenManager.clear()
        reminderScheduler.cancelReminders()
        try {
            appDatabase.clearAllTables()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        }
        authEventBus.triggerLogout()
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
}

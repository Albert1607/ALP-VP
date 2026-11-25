package com.example.alp.ui.screens.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alp.data.remote.dto.LoginRequest
import com.example.alp.data.remote.dto.RegisterRequest
import com.example.alp.data.repository.AuthRepository
import com.example.alp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val email: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun login(email: String, password: String) {
        if (_uiState.value is AuthUiState.Loading) return

        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            _uiState.value = AuthUiState.Error("Email tidak boleh kosong.")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = AuthUiState.Error("Format email tidak valid.")
            return
        }
        if (password.isEmpty()) {
            _uiState.value = AuthUiState.Error("Kata sandi tidak boleh kosong.")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = authRepository.login(
                LoginRequest(
                    email = trimmedEmail,
                    password = password
                )
            )

            _uiState.value = when (result) {
                is Resource.Success -> {
                    AuthUiState.Success(result.data?.user?.email ?: trimmedEmail)
                }
                is Resource.Error -> {
                    AuthUiState.Error(result.message ?: "Gagal masuk. Periksa kembali email dan kata sandi Anda.")
                }
                is Resource.Loading -> AuthUiState.Loading
            }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        timezone: String = try { ZoneId.systemDefault().id } catch (_: Exception) { "Asia/Jakarta" },
        gender: String? = null,
        weightKgStr: String? = null,
        heightCmStr: String? = null,
        ageStr: String? = null,
        activityLevel: String? = null,
        goal: String? = null
    ) {
        if (_uiState.value is AuthUiState.Loading) return

        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            _uiState.value = AuthUiState.Error("Email tidak boleh kosong.")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = AuthUiState.Error("Format email tidak valid.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Kata sandi minimal harus 6 karakter.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Konfirmasi kata sandi tidak cocok.")
            return
        }

        // Profile fields parsing (replace ',' with '.')
        val weightKg = weightKgStr?.replace(',', '.')?.trim()?.toDoubleOrNull()
        val heightCm = heightCmStr?.replace(',', '.')?.trim()?.toDoubleOrNull()
        val age = ageStr?.trim()?.toIntOrNull()

        val isAnyProfileFilled = !gender.isNullOrBlank() ||
                !weightKgStr.isNullOrBlank() ||
                !heightCmStr.isNullOrBlank() ||
                !ageStr.isNullOrBlank() ||
                !activityLevel.isNullOrBlank() ||
                !goal.isNullOrBlank()

        val isGenderValid = !gender.isNullOrBlank()
        val isWeightValid = weightKg != null && weightKg in 20.0..300.0
        val isHeightValid = heightCm != null && heightCm in 100.0..250.0
        val isAgeValid = age != null && age in 10..100
        val isActivityValid = !activityLevel.isNullOrBlank()
        val isGoalValid = !goal.isNullOrBlank()

        val allProfileValid = isGenderValid && isWeightValid && isHeightValid && isAgeValid && isActivityValid && isGoalValid

        if (isAnyProfileFilled && !allProfileValid) {
            _uiState.value = AuthUiState.Error("Lengkapi semua kolom profil atau kosongkan semuanya")
            return
        }

        val finalGender = if (allProfileValid) gender else null
        val finalWeight = if (allProfileValid) weightKg else null
        val finalHeight = if (allProfileValid) heightCm else null
        val finalAge = if (allProfileValid) age else null
        val finalActivity = if (allProfileValid) activityLevel else null
        val finalGoal = if (allProfileValid) goal else null

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = authRepository.register(
                RegisterRequest(
                    email = trimmedEmail,
                    password = password,
                    timezone = timezone.ifBlank { "Asia/Jakarta" },
                    gender = finalGender,
                    weightKg = finalWeight,
                    heightCm = finalHeight,
                    age = finalAge,
                    activityLevel = finalActivity,
                    goal = finalGoal
                )
            )

            _uiState.value = when (result) {
                is Resource.Success -> {
                    AuthUiState.Success(result.data?.user?.email ?: trimmedEmail)
                }
                is Resource.Error -> {
                    AuthUiState.Error(result.message ?: "Gagal mendaftar. Silakan coba lagi.")
                }
                is Resource.Loading -> AuthUiState.Loading
            }
        }
    }
}

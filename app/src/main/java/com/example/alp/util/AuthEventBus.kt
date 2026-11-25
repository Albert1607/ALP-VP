package com.example.alp.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthEventBus @Inject constructor() {
    private val _logoutFlow = MutableSharedFlow<Unit>(replay = 0)
    val logoutFlow: SharedFlow<Unit> = _logoutFlow.asSharedFlow()

    suspend fun triggerLogout() {
        _logoutFlow.emit(Unit)
    }
}

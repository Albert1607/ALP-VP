package com.example.alp.ui.screens.meals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.alp.data.local.TokenManagerEntryPoint
import com.example.alp.ui.common.RequestNotificationPermissionOnce
import dagger.hilt.android.EntryPointAccessors

@Composable
fun MealsScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext
    val tokenManager = remember(context) {
        EntryPointAccessors.fromApplication(context, TokenManagerEntryPoint::class.java).getTokenManager()
    }

    // Request notification permission once on first display after login
    RequestNotificationPermissionOnce(tokenManager = tokenManager)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Meals Screen Placeholder")
    }
}

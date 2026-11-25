package com.example.alp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.alp.data.local.TokenManager
import com.example.alp.ui.common.AppBottomBar
import com.example.alp.ui.navigation.NavGraph
import com.example.alp.ui.navigation.Screen
import com.example.alp.ui.theme.ALPTheme
import com.example.alp.util.AuthEventBus
import com.example.alp.worker.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var authEventBus: AuthEventBus

    private val navigateToState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigateToState.value = intent?.getStringExtra("navigate_to")

        if (tokenManager.isLoggedIn()) {
            CoroutineScope(Dispatchers.IO).launch {
                reminderScheduler.scheduleReminders()
            }
        }

        val startDestination = if (tokenManager.isLoggedIn()) {
            Screen.MealLog.route
        } else {
            Screen.Login.route
        }

        setContent {
            ALPTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    authEventBus.logoutFlow.collectLatest {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                // Handle navigate_to only when logged in
                LaunchedEffect(navigateToState.value, tokenManager.isLoggedIn()) {
                    val destination = navigateToState.value
                    if (tokenManager.isLoggedIn() && destination == "nutrition") {
                        navController.navigate(Screen.Nutrition.route) {
                            launchSingleTop = true
                        }
                        navigateToState.value = null // reset after navigating
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { AppBottomBar(navController = navController) }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navigateToState.value = intent.getStringExtra("navigate_to")
    }
}

package com.example.alp.ui.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.alp.ui.navigation.Screen

@Composable
fun AppBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomScreens = Screen.bottomBarScreens

    if (bottomScreens.any { it.route == currentRoute }) {
        NavigationBar {
            bottomScreens.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        screen.icon?.let {
                            Icon(imageVector = it, contentDescription = screen.title)
                        }
                    },
                    label = { Text(screen.title) },
                    selected = currentRoute == screen.route,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.MealLog.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

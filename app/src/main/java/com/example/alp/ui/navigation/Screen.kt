package com.example.alp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    
    // Bottom Bar Screens (5 items)
    object MealLog : Screen("meals", "Meals", Icons.Default.RestaurantMenu)
    object Fridge : Screen("fridge", "Fridge", Icons.Default.Kitchen)
    object Nutrition : Screen("nutrition", "Nutrition AI", Icons.Default.CameraAlt)
    object FoodPlan : Screen("plan", "Food Plan", Icons.Default.CalendarMonth)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    // Other screens
    object Budget : Screen("budget", "Budget", Icons.Default.AttachMoney)

    companion object {
        val bottomBarScreens = listOf(
            MealLog,
            Fridge,
            Nutrition,
            FoodPlan,
            Profile
        )
    }
}

package com.example.alp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.alp.ui.screens.auth.LoginScreen
import com.example.alp.ui.screens.auth.RegisterScreen
import com.example.alp.ui.screens.budget.BudgetScreen
import com.example.alp.ui.screens.fridge.FridgeScreen
import com.example.alp.ui.screens.meals.MealsScreen
import com.example.alp.ui.screens.nutrition.NutritionScreen
import com.example.alp.ui.screens.plan.PlanScreen
import com.example.alp.ui.screens.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.MealLog.route) {
            MealsScreen(navController = navController)
        }
        composable(Screen.Fridge.route) {
            FridgeScreen(navController = navController)
        }
        composable(Screen.Nutrition.route) {
            NutritionScreen(navController = navController)
        }
        composable(Screen.FoodPlan.route) {
            PlanScreen(navController = navController)
        }
        composable(Screen.Budget.route) {
            BudgetScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
}

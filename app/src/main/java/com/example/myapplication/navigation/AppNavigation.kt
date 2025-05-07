package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.features.habits.AddHabitScreen
import com.example.myapplication.features.habits.EditHabitScreen
import com.example.myapplication.features.habits.HabitListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavRoutes.HABIT_LIST) {
        composable(NavRoutes.HABIT_LIST) {
            HabitListScreen(navController = navController)
        }
        composable(NavRoutes.ADD_HABIT) {
            AddHabitScreen(navController = navController)
        }
        composable(
            route = NavRoutes.EDIT_HABIT, // Use the route with argument placeholder
            arguments = listOf(navArgument(NavRoutes.EDIT_HABIT_ARG_ID) { type = NavType.StringType })
        ) {
            val habitId = it.arguments?.getString(NavRoutes.EDIT_HABIT_ARG_ID)
            EditHabitScreen(navController = navController, habitId = habitId)
        }
    }
}

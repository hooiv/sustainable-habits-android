package com.example.myapplication.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
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
    
    // Add debug logging for navigation events
    navController.addOnDestinationChangedListener { controller, destination, arguments ->
        Log.d("Navigation", "Navigating to: ${destination.route}, arguments: $arguments")
    }
    
    AppNavigationGraph(navController = navController)
}

@Composable
fun AppNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController, 
        startDestination = NavRoutes.HABIT_LIST
    ) {
        composable(route = NavRoutes.HABIT_LIST) {
            Log.d("AppNavigation", "Setting up HabitListScreen")
            HabitListScreen(navController = navController)
        }
        
        composable(route = NavRoutes.ADD_HABIT) {
            Log.d("AppNavigation", "Setting up AddHabitScreen") 
            AddHabitScreen(navController = navController)
        }
        
        composable(
            route = NavRoutes.EDIT_HABIT,
            arguments = listOf(navArgument(NavRoutes.EDIT_HABIT_ARG_ID) { 
                type = NavType.StringType 
            })
        ) {
            val habitId = it.arguments?.getString(NavRoutes.EDIT_HABIT_ARG_ID)
            Log.d("AppNavigation", "Setting up EditHabitScreen with habitId: $habitId")
            EditHabitScreen(navController = navController, habitId = habitId)
        }
    }
}

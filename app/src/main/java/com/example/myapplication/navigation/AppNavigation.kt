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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.example.myapplication.features.stats.StatsScreen
import com.example.myapplication.features.calendar.HabitCalendarScreen
import com.example.myapplication.features.settings.SettingsScreen
import java.time.LocalDate

@OptIn(ExperimentalAnimationApi::class)
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

        composable(NavRoutes.STATS,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(500)
                )
            }
        ) {
            StatsScreen(navController)
        }

        composable(
            route = NavRoutes.CALENDAR,
            arguments = listOf(navArgument("habitName") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitName = backStackEntry.arguments?.getString("habitName") ?: "Unknown Habit"
            val completionHistory = listOf<LocalDate>() // Replace with actual data fetching logic
            HabitCalendarScreen(habitName = habitName, completionHistory = completionHistory)
        }

        composable(route = NavRoutes.SETTINGS) {
            SettingsScreen(context = navController.context)
        }
    }
}

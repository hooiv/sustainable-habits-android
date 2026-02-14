package com.example.myapplication.features.habits.navigation

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.features.habits.ui.AddHabitScreen
import com.example.myapplication.features.habits.ui.EditHabitScreen
import com.example.myapplication.features.habits.ui.HabitListScreen
import com.example.myapplication.features.habits.ui.HabitCompletionScreen

fun NavGraphBuilder.habitsGraph(navController: NavController) {
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
    ) { backStackEntry ->
        val habitId = backStackEntry.arguments?.getString(NavRoutes.EDIT_HABIT_ARG_ID)
        Log.d("AppNavigation", "Setting up EditHabitScreen with habitId: $habitId")
        EditHabitScreen(navController = navController, habitId = habitId)
    }

    composable(
        route = NavRoutes.HABIT_COMPLETION,
        arguments = listOf(
            navArgument(NavRoutes.HABIT_COMPLETION_ARG_ID) {
                type = NavType.StringType
            },
            navArgument(NavRoutes.HABIT_COMPLETION_ARG_NAME) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val habitId = backStackEntry.arguments?.getString(NavRoutes.HABIT_COMPLETION_ARG_ID) ?: ""
        val habitName = backStackEntry.arguments?.getString(NavRoutes.HABIT_COMPLETION_ARG_NAME) ?: ""

        HabitCompletionScreen(
            navController = navController,
            habitId = habitId,
            habitName = habitName
        )
    }
}

package com.hooiv.habitflow.features.habits.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hooiv.habitflow.core.ui.navigation.NavRoutes
import com.hooiv.habitflow.features.habits.ui.AddHabitScreen
import com.hooiv.habitflow.features.habits.ui.EditHabitScreen
import com.hooiv.habitflow.features.habits.ui.HabitListScreen
import com.hooiv.habitflow.features.habits.ui.HabitCompletionScreen
import com.hooiv.habitflow.features.habits.HabitDetailsScreen

fun NavGraphBuilder.habitsGraph(navController: NavController) {
    composable(route = NavRoutes.HABIT_LIST) {
        HabitListScreen(
            navController = navController,
            onNavigateToDetails = { habitId ->
                navController.navigate(NavRoutes.habitDetails(habitId))
            }
        )
    }

    composable(
        route = NavRoutes.HABIT_DETAILS,
        arguments = listOf(navArgument("habitId") { type = NavType.StringType })
    ) { backStackEntry ->
        val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
        HabitDetailsScreen(
            habitId = habitId,
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(route = NavRoutes.ADD_HABIT) {
        AddHabitScreen(navController = navController)
    }

    composable(
        route = NavRoutes.EDIT_HABIT,
        arguments = listOf(navArgument(NavRoutes.EDIT_HABIT_ARG_ID) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val habitId = backStackEntry.arguments?.getString(NavRoutes.EDIT_HABIT_ARG_ID)
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

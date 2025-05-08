package com.example.myapplication.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn // Added import for scaleIn
import androidx.compose.animation.scaleOut // Added import for scaleOut
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
import com.example.myapplication.features.stats.StatsScreen
import java.time.LocalDate
import kotlin.math.pow // Ensure pow is imported

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // Add debug logging for navigation events
    navController.addOnDestinationChangedListener { _, destination, arguments ->
        Log.d("Navigation", "Navigating to: ${destination.route}, arguments: $arguments")
    }
    
    AppNavigationGraph(navController = navController)
}

@Composable
fun AppNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController, 
        startDestination = NavRoutes.HABIT_LIST,
        // Apply enhanced enter/exit animations for the entire NavHost
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable(route = NavRoutes.HABIT_LIST) {
            Log.d("AppNavigation", "Setting up HabitListScreen")
            HabitListScreen(navController = navController)
        }
        
        composable(
            route = NavRoutes.ADD_HABIT,
            // Apply a unique animation for the add habit screen
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(
                    animationSpec = tween(300)
                )
            }
        ) {
            Log.d("AppNavigation", "Setting up AddHabitScreen") 
            AddHabitScreen(navController = navController)
        }
        
        composable(
            route = NavRoutes.EDIT_HABIT,
            arguments = listOf(navArgument(NavRoutes.EDIT_HABIT_ARG_ID) { 
                type = NavType.StringType 
            }),
            // Apply a 3D-like rotation for the edit screen
            enterTransition = {
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn()
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut()
            }
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString(NavRoutes.EDIT_HABIT_ARG_ID)
            Log.d("AppNavigation", "Setting up EditHabitScreen with habitId: $habitId")
            EditHabitScreen(navController = navController, habitId = habitId)
        }

        composable(
            route = NavRoutes.STATS,
            // Apply an impressive transition for the stats screen
            enterTransition = {
                fadeIn(
                    animationSpec = tween(700, easing = EaseOutQuint)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(700, easing = EaseInQuint)
                )
            }
        ) {
            StatsScreen(navController)
        }

        // For now, commenting out screens that aren't implemented yet
        // to avoid errors about missing composables
        /*
        composable(
            route = NavRoutes.CALENDAR
        ) {
            // Placeholder for calendar screen
        }

        composable(route = NavRoutes.SETTINGS) {
            // Placeholder for settings screen
        }
        */
    }
}

// Custom easing curves for more interesting animations
private val EaseOutQuint = Easing { fraction -> (1f - (1f - fraction).pow(5)).toFloat() }
private val EaseInQuint = Easing { fraction -> fraction.pow(5).toFloat() }

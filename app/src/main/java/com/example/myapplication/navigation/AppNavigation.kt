package com.example.myapplication.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.features.habits.AddHabitScreen
import com.example.myapplication.features.habits.EditHabitScreen
import com.example.myapplication.features.habits.HabitListScreen
import com.example.myapplication.features.stats.StatsScreen
import com.example.myapplication.features.settings.SettingsScreen
import com.example.myapplication.features.auth.SignInScreen
import com.example.myapplication.R
import java.time.LocalDate
import kotlin.math.pow

@Composable
fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        colorResource(R.color.brand_gradient_start),
                        colorResource(R.color.brand_gradient_end)
                    )
                )
            )
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = colorResource(R.color.brand_accent)
        ) {
            NavigationBarItem(
                selected = currentRoute == NavRoutes.HABIT_LIST,
                onClick = { onNavigate(NavRoutes.HABIT_LIST) },
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Habits") },
                label = { Text("Habits") }
            )
            NavigationBarItem(
                selected = currentRoute == NavRoutes.STATS,
                onClick = { onNavigate(NavRoutes.STATS) },
                icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                label = { Text("Stats") }
            )
            NavigationBarItem(
                selected = currentRoute == NavRoutes.SETTINGS,
                onClick = { onNavigate(NavRoutes.SETTINGS) },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") }
            )
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    navController.addOnDestinationChangedListener { _, destination, arguments ->
        Log.d("Navigation", "Navigating to: ${destination.route}, arguments: $arguments")
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(currentRoute = currentRoute) { route ->
                if (route != currentRoute) navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavigationGraph(navController = navController)
        }
    }
}

@Composable
fun AppNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController, 
        startDestination = NavRoutes.HABIT_LIST,
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

        composable(route = NavRoutes.STATS) {
            StatsScreen(navController)
        }

        composable(route = NavRoutes.SETTINGS) {
            val context = LocalContext.current
            SettingsScreen(navController = navController, context = context)
        }

        composable(route = NavRoutes.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(NavRoutes.SETTINGS) {
                        popUpTo(NavRoutes.SIGN_IN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private val EaseOutQuint = Easing { fraction -> (1f - (1f - fraction).pow(5)).toFloat() }
private val EaseInQuint = Easing { fraction -> fraction.pow(5).toFloat() }

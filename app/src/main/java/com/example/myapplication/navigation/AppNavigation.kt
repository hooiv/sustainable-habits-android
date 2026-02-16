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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import com.example.myapplication.core.ui.navigation.NavRoutes
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.R
import java.time.LocalDate
import kotlin.math.pow

// Feature Navigation Graphs
import com.example.myapplication.features.advanced.navigation.advancedGraph
import com.example.myapplication.features.animation.navigation.animationGraph
import com.example.myapplication.features.auth.navigation.authGraph
import com.example.myapplication.features.demo.navigation.demoGraph
import com.example.myapplication.features.habits.navigation.habitsGraph
import com.example.myapplication.features.settings.navigation.settingsGraph
import com.example.myapplication.features.stats.navigation.statsGraph

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

            // Add Animations tab
            NavigationBarItem(
                selected = currentRoute == NavRoutes.ANIMATIONS ||
                          currentRoute == NavRoutes.ANIMEJS_ANIMATION ||
                          currentRoute == NavRoutes.THREEJS_VISUALIZATION,
                onClick = { onNavigate(NavRoutes.ANIMATIONS) },
                icon = { Icon(Icons.Default.Star, contentDescription = "Animations") },
                label = { Text("Animations") }
            )

            NavigationBarItem(
                selected = currentRoute == NavRoutes.STATS,
                onClick = { onNavigate(NavRoutes.STATS) },
                icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                label = { Text("Stats") }
            )
            // Advanced Features tab
            NavigationBarItem(
                selected = currentRoute == NavRoutes.ADVANCED_FEATURES ||
                          currentRoute == NavRoutes.BIOMETRIC_INTEGRATION_GLOBAL ||
                          currentRoute == NavRoutes.AR_GLOBAL ||
                          currentRoute == NavRoutes.VOICE_INTEGRATION ||
                          currentRoute == NavRoutes.QUANTUM_VISUALIZATION_GLOBAL ||
                          currentRoute == NavRoutes.SPATIAL_COMPUTING ||
                          currentRoute == NavRoutes.THREEJS_VISUALIZATION ||
                          currentRoute == NavRoutes.ANIMEJS_ANIMATION ||
                          currentRoute == NavRoutes.MULTI_MODAL_LEARNING ||
                          currentRoute == NavRoutes.META_LEARNING ||
                          currentRoute == NavRoutes.NEURAL_NETWORK ||
                          currentRoute == NavRoutes.AI_ASSISTANT ||
                          currentRoute == NavRoutes.GESTURE_CONTROLS ||
                          currentRoute == NavRoutes.ADVANCED_ANALYTICS ||
                          currentRoute == NavRoutes.PREDICTIVE_ML,
                onClick = { onNavigate(NavRoutes.ADVANCED_FEATURES) },
                icon = { Icon(Icons.Default.Star, contentDescription = "Advanced") },
                label = { Text("Advanced") }
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
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(
                animationSpec = tween(400, easing = EaseOutQuint)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeOut(
                animationSpec = tween(400, easing = EaseInQuint)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(
                animationSpec = tween(400, easing = EaseOutQuint)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeOut(
                animationSpec = tween(400, easing = EaseInQuint)
            )
        }
    ) {
        habitsGraph(navController)
        statsGraph(navController)
        settingsGraph(navController)
        authGraph(navController)
        demoGraph(navController)
        animationGraph(navController)
        advancedGraph(navController)
    }
}

private val EaseOutQuint = Easing { fraction -> 1f - (1f - fraction).pow(5) }
private val EaseInQuint = Easing { fraction -> fraction.pow(5) }

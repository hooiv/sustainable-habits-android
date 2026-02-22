package com.example.myapplication.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import com.example.myapplication.core.ui.navigation.NavRoutes
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.math.pow

// Feature Navigation Graphs
import androidx.navigation.compose.composable
import com.example.myapplication.features.ai.ui.AIAssistantScreen
import com.example.myapplication.features.ai.ui.AIAssistantSettingsScreen
import com.example.myapplication.features.analytics.ui.AdvancedAnalyticsScreen
import com.example.myapplication.features.auth.navigation.authGraph
import com.example.myapplication.features.gamification.GamificationScreen
import com.example.myapplication.features.habits.navigation.habitsGraph
import com.example.myapplication.features.settings.navigation.settingsGraph
import com.example.myapplication.features.stats.navigation.statsGraph

@Composable
fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = currentRoute == NavRoutes.HABIT_LIST,
                onClick = { onNavigate(NavRoutes.HABIT_LIST) },
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Habits") },
                label = { Text("Habits") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )

            NavigationBarItem(
                selected = currentRoute == NavRoutes.STATS,
                onClick = { onNavigate(NavRoutes.STATS) },
                icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                label = { Text("Stats") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )

            NavigationBarItem(
                selected = currentRoute == NavRoutes.SETTINGS,
                onClick = { onNavigate(NavRoutes.SETTINGS) },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

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
        composable(route = NavRoutes.AI_ASSISTANT) {
            AIAssistantScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = NavRoutes.AI_ASSISTANT_SETTINGS) {
            AIAssistantSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = NavRoutes.ADVANCED_ANALYTICS) {
            AdvancedAnalyticsScreen(navController = navController)
        }
        composable(route = NavRoutes.GAMIFICATION) {
            GamificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

private val EaseOutQuint = Easing { fraction -> 1f - (1f - fraction).pow(5) }
private val EaseInQuint = Easing { fraction -> fraction.pow(5) }

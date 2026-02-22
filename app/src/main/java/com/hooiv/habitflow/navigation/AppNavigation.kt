package com.hooiv.habitflow.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.hooiv.habitflow.core.ui.navigation.NavRoutes
import com.hooiv.habitflow.core.ui.util.WindowWidthSizeClass
import com.hooiv.habitflow.core.ui.util.rememberWindowWidthSizeClass
import com.hooiv.habitflow.features.ai.ui.AIAssistantScreen
import com.hooiv.habitflow.features.ai.ui.AIAssistantSettingsScreen
import com.hooiv.habitflow.features.analytics.ui.AdvancedAnalyticsScreen
import com.hooiv.habitflow.features.auth.navigation.authGraph
import com.hooiv.habitflow.features.gamification.GamificationScreen
import com.hooiv.habitflow.features.habits.navigation.habitsGraph
import com.hooiv.habitflow.features.settings.navigation.settingsGraph
import com.hooiv.habitflow.features.stats.navigation.statsGraph
import kotlin.math.pow

// ---------------------------------------------------------------------------
// Navigation items shared between BottomBar and Rail
// ---------------------------------------------------------------------------

private data class TopLevelDestination(
    val route: String,
    val icon: @Composable () -> Unit,
    val label: String
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = NavRoutes.HABIT_LIST,
        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Habits") },
        label = "Habits"
    ),
    TopLevelDestination(
        route = NavRoutes.STATS,
        icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
        label = "Stats"
    ),
    TopLevelDestination(
        route = NavRoutes.SETTINGS,
        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
        label = "Settings"
    )
)

// ---------------------------------------------------------------------------
// Phone bottom bar
// ---------------------------------------------------------------------------

@Composable
fun MainBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
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
            topLevelDestinations.forEach { dest ->
                NavigationBarItem(
                    selected = currentRoute == dest.route,
                    onClick = { onNavigate(dest.route) },
                    icon = dest.icon,
                    label = { Text(dest.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tablet / foldable navigation rail
// ---------------------------------------------------------------------------

@Composable
fun MainNavigationRail(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxHeight()
    ) {
        // Centre items vertically in the rail
        Spacer(Modifier.weight(1f))
        topLevelDestinations.forEach { dest ->
            NavigationRailItem(
                selected = currentRoute == dest.route,
                onClick = { onNavigate(dest.route) },
                icon = dest.icon,
                label = { Text(dest.label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            Spacer(Modifier.height(4.dp))
        }
        Spacer(Modifier.weight(1f))
    }
}

// ---------------------------------------------------------------------------
// Root AppNavigation — chooses layout based on window width
// ---------------------------------------------------------------------------

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val widthSizeClass = rememberWindowWidthSizeClass()

    fun navigate(route: String) {
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    if (widthSizeClass == WindowWidthSizeClass.Compact) {
        // --- Phone: bottom bar ---
        Scaffold(
            bottomBar = {
                MainBottomBar(currentRoute = currentRoute, onNavigate = ::navigate)
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavigationGraph(navController = navController)
            }
        }
    } else {
        // --- Tablet / foldable: navigation rail on the left ---
        Row(modifier = Modifier.fillMaxSize()) {
            MainNavigationRail(currentRoute = currentRoute, onNavigate = ::navigate)
            VerticalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )
            Box(modifier = Modifier.weight(1f)) {
                AppNavigationGraph(navController = navController)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// NavHost graph (unchanged)
// ---------------------------------------------------------------------------

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
            ) + fadeIn(animationSpec = tween(400, easing = EaseOutQuint))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeOut(animationSpec = tween(400, easing = EaseInQuint))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(400, easing = EaseOutQuint))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeOut(animationSpec = tween(400, easing = EaseInQuint))
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
private val EaseInQuint  = Easing { fraction -> fraction.pow(5) }

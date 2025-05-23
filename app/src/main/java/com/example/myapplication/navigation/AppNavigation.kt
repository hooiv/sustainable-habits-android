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
import androidx.compose.material.icons.filled.Star
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
import com.example.myapplication.features.neural.NeuralInterfaceScreen
import com.example.myapplication.features.neural.NeuralInterfaceViewModel
import com.example.myapplication.features.stats.StatsScreen
import com.example.myapplication.features.settings.SettingsScreen
import com.example.myapplication.features.auth.SignInScreen
import com.example.myapplication.features.demo.AnimationDemoScreen
import com.example.myapplication.features.animation.AnimeJsAnimationScreen
import com.example.myapplication.features.advanced.AdvancedFeaturesScreen
import androidx.hilt.navigation.compose.hiltViewModel
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

        composable(route = NavRoutes.ANIMATION_DEMO) {
            AnimationDemoScreen(navController = navController)
        }

        composable(
            route = NavRoutes.NEURAL_INTERFACE,
            arguments = listOf(navArgument(NavRoutes.NEURAL_INTERFACE_ARG_ID) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString(NavRoutes.NEURAL_INTERFACE_ARG_ID)
            val viewModel: NeuralInterfaceViewModel = hiltViewModel()

            habitId?.let { id ->
                // Get the habit from the repository
                val database = com.example.myapplication.data.database.AppDatabase.getInstance(LocalContext.current)
                val habitRepository = com.example.myapplication.data.repository.HabitRepository(
                    database.habitDao(),
                    database.habitCompletionDao()
                )

                // For simplicity, we're creating a sample habit
                // In a real app, you would get this from the repository
                val habit = com.example.myapplication.data.model.Habit(
                    id = id,
                    name = "Sample Habit",
                    description = "This is a sample habit for the neural interface demo",
                    frequency = com.example.myapplication.data.model.HabitFrequency.DAILY,
                    streak = 5,
                    goal = 10,
                    goalProgress = 7
                )

                NeuralInterfaceScreen(
                    habit = habit,
                    onBackClick = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
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

            com.example.myapplication.features.habits.HabitCompletionScreen(
                navController = navController,
                habitId = habitId,
                habitName = habitName
            )
        }

        // AR screen with specific habit
        composable(
            route = NavRoutes.AR,
            arguments = listOf(
                navArgument(NavRoutes.AR_ARG_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString(NavRoutes.AR_ARG_ID) ?: ""

            // Get the habit from the repository
            val database = com.example.myapplication.data.database.AppDatabase.getInstance(LocalContext.current)
            val habitRepository = com.example.myapplication.data.repository.HabitRepository(
                database.habitDao(),
                database.habitCompletionDao()
            )

            // For simplicity, we're creating a sample habit
            // In a real app, you would get this from the repository
            val habit = com.example.myapplication.data.model.Habit(
                id = habitId,
                name = "Sample Habit",
                description = "This is a sample habit for AR visualization",
                frequency = com.example.myapplication.data.model.HabitFrequency.DAILY,
                streak = 5,
                goal = 10,
                goalProgress = 7
            )

            com.example.myapplication.features.ar.ARScreen(
                navController = navController,
                habit = habit,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Global AR screen without a specific habit
        composable(route = NavRoutes.AR_GLOBAL) {
            com.example.myapplication.features.ar.ARScreen(
                navController = navController,
                habit = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Quantum visualization with specific habit
        composable(
            route = NavRoutes.QUANTUM_VISUALIZATION,
            arguments = listOf(navArgument(NavRoutes.QUANTUM_VISUALIZATION_ARG_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString(NavRoutes.QUANTUM_VISUALIZATION_ARG_ID)

            com.example.myapplication.features.quantum.QuantumVisualizationScreen(
                navController = navController,
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Global quantum visualization without a specific habit
        composable(route = NavRoutes.QUANTUM_VISUALIZATION_GLOBAL) {
            com.example.myapplication.features.quantum.QuantumVisualizationScreen(
                navController = navController,
                habitId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Biometric integration with specific habit
        composable(
            route = NavRoutes.BIOMETRIC_INTEGRATION,
            arguments = listOf(navArgument(NavRoutes.BIOMETRIC_INTEGRATION_ARG_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString(NavRoutes.BIOMETRIC_INTEGRATION_ARG_ID)

            com.example.myapplication.features.biometric.BiometricIntegrationScreen(
                navController = navController,
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Global biometric integration without a specific habit
        composable(route = NavRoutes.BIOMETRIC_INTEGRATION_GLOBAL) {
            com.example.myapplication.features.biometric.BiometricIntegrationScreen(
                navController = navController,
                habitId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Voice integration screen
        composable(route = NavRoutes.VOICE_INTEGRATION) {
            com.example.myapplication.features.voice.VoiceIntegrationScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Spatial computing screen
        composable(route = NavRoutes.SPATIAL_COMPUTING) {
            com.example.myapplication.features.spatial.SpatialComputingScreen(
                navController = navController,
                habitId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Three.js visualization screen
        composable(route = NavRoutes.THREEJS_VISUALIZATION) {
            com.example.myapplication.features.threejs.ThreeJsVisualizationScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Anime.js animation screen
        composable(route = NavRoutes.ANIMEJS_ANIMATION) {
            AnimeJsAnimationScreen(
                navController = navController
            )
        }

        // Advanced features screen
        composable(route = NavRoutes.ADVANCED_FEATURES) {
            AdvancedFeaturesScreen(
                navController = navController
            )
        }

        // Multi-modal learning screen
        composable(route = NavRoutes.MULTI_MODAL_LEARNING) {
            com.example.myapplication.features.ml.MultiModalLearningScreen(
                navController = navController
            )
        }

        // Meta-learning screen
        composable(route = NavRoutes.META_LEARNING) {
            com.example.myapplication.features.ml.MetaLearningScreen(
                navController = navController
            )
        }

        // Neural network screen
        composable(route = NavRoutes.NEURAL_NETWORK) {
            com.example.myapplication.features.neural.NeuralNetworkScreen(
                navController = navController
            )
        }

        // AI Assistant screen
        composable(route = NavRoutes.AI_ASSISTANT) {
            com.example.myapplication.features.ai.AIAssistantScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Gesture Controls screen
        composable(route = NavRoutes.GESTURE_CONTROLS) {
            com.example.myapplication.features.gestures.GestureControlsScreen(
                navController = navController
            )
        }

        // Advanced Analytics screen
        composable(route = NavRoutes.ADVANCED_ANALYTICS) {
            com.example.myapplication.features.analytics.AdvancedAnalyticsScreen(
                navController = navController
            )
        }

        // Predictive ML screen
        composable(route = NavRoutes.PREDICTIVE_ML) {
            com.example.myapplication.features.ml.PredictiveMLScreen(
                navController = navController
            )
        }

        // AI Assistant Settings screen
        composable(route = NavRoutes.AI_ASSISTANT_SETTINGS) {
            com.example.myapplication.features.ai.AIAssistantSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }


    }
}

private val EaseOutQuint = Easing { fraction -> (1f - (1f - fraction).pow(5)).toFloat() }
private val EaseInQuint = Easing { fraction -> fraction.pow(5).toFloat() }

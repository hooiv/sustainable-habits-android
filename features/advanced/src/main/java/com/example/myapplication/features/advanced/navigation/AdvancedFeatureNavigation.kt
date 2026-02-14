package com.example.myapplication.features.advanced.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.features.advanced.AdvancedFeaturesScreen
import com.example.myapplication.features.ai.ui.AIAssistantScreen
import com.example.myapplication.features.ai.ui.AIAssistantSettingsScreen
import com.example.myapplication.features.analytics.ui.AdvancedAnalyticsScreen
import com.example.myapplication.features.ar.ARScreen
import com.example.myapplication.features.biometric.BiometricIntegrationScreen
import com.example.myapplication.features.gestures.GestureControlsScreen
import com.example.myapplication.features.ml.MetaLearningScreen
import com.example.myapplication.features.ml.MultiModalLearningScreen
import com.example.myapplication.features.ml.PredictiveMLScreen
import com.example.myapplication.features.neural.NeuralInterfaceScreen
import com.example.myapplication.features.neural.NeuralInterfaceViewModel
import com.example.myapplication.features.neural.NeuralNetworkScreen
import com.example.myapplication.features.quantum.QuantumVisualizationScreen
import com.example.myapplication.features.spatial.SpatialComputingScreen
import com.example.myapplication.features.threejs.ThreeJsVisualizationScreen
import com.example.myapplication.features.voice.VoiceIntegrationScreen
import com.example.myapplication.core.data.database.AppDatabase
import com.example.myapplication.core.data.repository.HabitRepository

fun NavGraphBuilder.advancedGraph(navController: NavController) {
    // Advanced features dashboard
    composable(route = NavRoutes.ADVANCED_FEATURES) {
        AdvancedFeaturesScreen(navController = navController)
    }

    // Neural Interface
    composable(
        route = NavRoutes.NEURAL_INTERFACE,
        arguments = listOf(navArgument(NavRoutes.NEURAL_INTERFACE_ARG_ID) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val habitId = backStackEntry.arguments?.getString(NavRoutes.NEURAL_INTERFACE_ARG_ID)
        val viewModel: NeuralInterfaceViewModel = hiltViewModel()

        habitId?.let { id ->
            // In a real app, you would get this from the repository via ViewModel
             // For simplicity duplicating the logic from AppNavigation.kt
            val database = AppDatabase.getInstance(LocalContext.current)
            val habitRepository = HabitRepository(
                database.habitDao(),
                database.habitCompletionDao()
            )

            // For simplicity, we're creating a sample habit
            val habit = com.example.myapplication.core.data.model.Habit(
                id = id,
                name = "Sample Habit",
                description = "This is a sample habit for the neural interface demo",
                frequency = com.example.myapplication.core.data.model.HabitFrequency.DAILY,
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

    composable(route = NavRoutes.NEURAL_NETWORK) {
        NeuralNetworkScreen(navController = navController)
    }

    // AR
    composable(
        route = NavRoutes.AR,
        arguments = listOf(navArgument(NavRoutes.AR_ARG_ID) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val habitId = backStackEntry.arguments?.getString(NavRoutes.AR_ARG_ID) ?: ""
        // Sample habit creation would ideally be in ViewModel
        val habit = com.example.myapplication.core.data.model.Habit(
            id = habitId,
            name = "Sample Habit",
            description = "This is a sample habit for AR visualization",
            frequency = com.example.myapplication.core.data.model.HabitFrequency.DAILY,
            streak = 5,
            goal = 10,
            goalProgress = 7
        )

        ARScreen(
            navController = navController,
            habit = habit,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(route = NavRoutes.AR_GLOBAL) {
        ARScreen(
            navController = navController,
            habit = null,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Quantum
    composable(
        route = NavRoutes.QUANTUM_VISUALIZATION,
        arguments = listOf(navArgument(NavRoutes.QUANTUM_VISUALIZATION_ARG_ID) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) { backStackEntry ->
        val habitId = backStackEntry.arguments?.getString(NavRoutes.QUANTUM_VISUALIZATION_ARG_ID)
        QuantumVisualizationScreen(
            navController = navController,
            habitId = habitId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(route = NavRoutes.QUANTUM_VISUALIZATION_GLOBAL) {
        QuantumVisualizationScreen(
            navController = navController,
            habitId = null,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Biometric
    composable(
        route = NavRoutes.BIOMETRIC_INTEGRATION,
        arguments = listOf(navArgument(NavRoutes.BIOMETRIC_INTEGRATION_ARG_ID) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) { backStackEntry ->
        val habitId = backStackEntry.arguments?.getString(NavRoutes.BIOMETRIC_INTEGRATION_ARG_ID)
        BiometricIntegrationScreen(
            navController = navController,
            habitId = habitId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(route = NavRoutes.BIOMETRIC_INTEGRATION_GLOBAL) {
        BiometricIntegrationScreen(
            navController = navController,
            habitId = null,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Voice
    composable(route = NavRoutes.VOICE_INTEGRATION) {
        VoiceIntegrationScreen(
            navController = navController,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Spatial
    composable(route = NavRoutes.SPATIAL_COMPUTING) {
        SpatialComputingScreen(
            navController = navController,
            habitId = null,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // ThreeJS
    composable(route = NavRoutes.THREEJS_VISUALIZATION) {
        ThreeJsVisualizationScreen(
            navController = navController,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // ML
    composable(route = NavRoutes.MULTI_MODAL_LEARNING) {
        MultiModalLearningScreen(navController = navController)
    }

    composable(route = NavRoutes.META_LEARNING) {
        MetaLearningScreen(navController = navController)
    }

    composable(route = NavRoutes.PREDICTIVE_ML) {
        PredictiveMLScreen(navController = navController)
    }

    // AI
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

    // Gestures
    composable(route = NavRoutes.GESTURE_CONTROLS) {
        GestureControlsScreen(navController = navController)
    }

    // Analytics
    composable(route = NavRoutes.ADVANCED_ANALYTICS) {
        AdvancedAnalyticsScreen(navController = navController)
    }
}

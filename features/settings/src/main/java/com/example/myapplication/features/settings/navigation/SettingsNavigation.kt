package com.example.myapplication.features.settings.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.features.settings.SettingsScreen

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    composable(route = NavRoutes.SETTINGS) {
        val context = LocalContext.current
        SettingsScreen(navController = navController, context = context)
    }
}

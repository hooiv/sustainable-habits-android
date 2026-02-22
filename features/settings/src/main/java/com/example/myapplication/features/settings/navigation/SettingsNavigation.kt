package com.hooiv.habitflow.features.settings.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.hooiv.habitflow.core.ui.navigation.NavRoutes
import com.hooiv.habitflow.features.settings.SettingsScreen

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    composable(route = NavRoutes.SETTINGS) {
        val context = LocalContext.current
        SettingsScreen(navController = navController, context = context)
    }
}

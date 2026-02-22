package com.hooiv.habitflow.features.stats.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.hooiv.habitflow.core.ui.navigation.NavRoutes
import com.hooiv.habitflow.features.stats.StatsScreen

fun NavGraphBuilder.statsGraph(navController: NavController) {
    composable(route = NavRoutes.STATS) {
        StatsScreen(navController)
    }
}

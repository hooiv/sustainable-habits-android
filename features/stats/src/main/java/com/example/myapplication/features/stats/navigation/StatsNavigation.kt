package com.example.myapplication.features.stats.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.features.stats.StatsScreen

fun NavGraphBuilder.statsGraph(navController: NavController) {
    composable(route = NavRoutes.STATS) {
        StatsScreen(navController)
    }
}

package com.example.myapplication.features.demo.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.features.demo.AnimationDemoScreen

fun NavGraphBuilder.demoGraph(navController: NavController) {
    composable(route = NavRoutes.ANIMATION_DEMO) {
        AnimationDemoScreen(navController = navController)
    }
}

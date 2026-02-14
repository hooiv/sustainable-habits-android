package com.example.myapplication.features.animation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.features.animation.AnimeJsAnimationScreen
import com.example.myapplication.features.animation.AnimationsScreen

fun NavGraphBuilder.animationGraph(navController: NavController) {
    composable(route = NavRoutes.ANIMATIONS) {
        AnimationsScreen(navController = navController)
    }

    composable(route = NavRoutes.ANIMEJS_ANIMATION) {
        AnimeJsAnimationScreen(navController = navController)
    }
}

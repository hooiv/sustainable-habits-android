package com.hooiv.habitflow.features.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.hooiv.habitflow.core.ui.navigation.NavRoutes
import com.hooiv.habitflow.features.auth.SignInScreen

fun NavGraphBuilder.authGraph(navController: NavController) {
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
}

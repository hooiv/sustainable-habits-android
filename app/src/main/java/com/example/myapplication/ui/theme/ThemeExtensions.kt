package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ElevationValues for the theme 
data class Elevation(
    val default: Dp = 0.dp,
    val small: Dp = 2.dp,
    val medium: Dp = 4.dp,
    val large: Dp = 8.dp,
    val extraLarge: Dp = 16.dp
)

/**
 * Provides access to elevation values from the current theme
 */
val MaterialTheme.customElevation: Elevation
    @Composable
    @ReadOnlyComposable
    get() {
        return Elevation() // Return default values to avoid conflicts
    }
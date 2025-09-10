package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

object AnimatedThemeExtension {
    data class GradientSet(
        val primary: List<Color>,
        val secondary: List<Color>,
        val tertiary: List<Color>
    )
}

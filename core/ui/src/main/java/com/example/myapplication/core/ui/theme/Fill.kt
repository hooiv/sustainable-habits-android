package com.example.myapplication.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill

/**
 * Enum class for fill types
 */
enum class FillType {
    SOLID,
    GRADIENT,
    PATTERN,
    NONE
}

/**
 * Extension function to get the Fill object for DrawScope
 */
fun DrawScope.getFill(): androidx.compose.ui.graphics.drawscope.Fill = Fill

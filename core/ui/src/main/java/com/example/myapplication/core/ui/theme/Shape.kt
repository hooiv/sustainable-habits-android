package com.example.myapplication.core.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = CutCornerShape(16.dp), // Cyberpunk style cut corners for cards
    extraLarge = CutCornerShape(24.dp)
)

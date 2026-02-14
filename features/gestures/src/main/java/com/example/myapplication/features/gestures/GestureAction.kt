package com.example.myapplication.features.gestures

import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

/**
 * Represents an action that can be triggered by a gesture (radial menu, swipe, etc.).
 *
 * Constructor is ordered so that you can create an action succinctly with:
 *   GestureAction("Complete Habit", "Complete the current habit", Icons.Default.Check) { ... }
 * or with explicit id first if needed via named argument:
 *   GestureAction(id = "add_habit", name = "Add Habit", description = "Create a new habit", icon = Icons.Default.Add) { }
 */
data class GestureAction(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val action: () -> Unit = {},
    val id: String = name.lowercase(Locale.getDefault()).replace("\\s+".toRegex(), "_")
)


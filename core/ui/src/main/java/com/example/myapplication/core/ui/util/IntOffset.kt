package com.example.myapplication.core.ui.util

import androidx.compose.ui.unit.IntOffset

/**
 * Extension function to create an IntOffset from x and y coordinates
 */
fun IntOffset(x: Int, y: Int): IntOffset = androidx.compose.ui.unit.IntOffset(x, y)

/**
 * Extension function to create an IntOffset from a float Offset
 */
fun IntOffset(offset: androidx.compose.ui.geometry.Offset): IntOffset = 
    androidx.compose.ui.unit.IntOffset(offset.x.toInt(), offset.y.toInt())

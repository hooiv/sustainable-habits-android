package com.example.myapplication.core.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * A composable that detects swipe gestures and triggers callbacks
 */
@Composable
fun SwipeGestureArea(
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    val swipeThreshold = 50f
                    when {
                        dragAmount < -swipeThreshold -> onSwipeLeft()
                        dragAmount > swipeThreshold -> onSwipeRight()
                    }
                }
            }
    ) {
        content()
    }
}

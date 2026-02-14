package com.example.myapplication.features.gestures

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * A composable that detects swipe gestures and triggers callbacks
 */
@Composable
fun SwipeGestureArea(
    modifier: Modifier = Modifier,
    swipeThreshold: Float = 100f,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var dragStartX by remember { mutableStateOf(0f) }
    var dragEndX by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragStartX = it.x },
                    onDragEnd = {
                        val dragDistance = dragEndX - dragStartX
                        if (abs(dragDistance) > swipeThreshold) {
                            if (dragDistance < 0) {
                                onSwipeLeft()
                            } else {
                                onSwipeRight()
                            }
                        }
                    },
                    onDragCancel = {},
                    onHorizontalDrag = { _, dragAmount ->
                        dragEndX = dragStartX + dragAmount
                    }
                )
            }
    ) {
        content()
    }
}

package com.example.myapplication.features.gestures

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Data class representing a gesture action
 */
data class GestureAction(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val action: () -> Unit
)

/**
 * A component that provides a radial gesture menu activated by long press
 */
@Composable
fun RadialGestureMenu(
    modifier: Modifier = Modifier,
    actions: List<GestureAction>,
    onActionSelected: (GestureAction) -> Unit = {}
) {
    var menuVisible by remember { mutableStateOf(false) }
    var menuPosition by remember { mutableStateOf(Offset.Zero) }
    var selectedActionIndex by remember { mutableStateOf<Int?>(null) }
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Animation for menu appearance
    val menuScale by animateFloatAsState(
        targetValue = if (menuVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "menuScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { position ->
                        menuPosition = position
                        menuVisible = true
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onTap = {
                        if (menuVisible) {
                            // Check if tap is outside the menu
                            val menuRadius = 150f // Same as in the Canvas
                            val distance = (it - menuPosition).getDistance()
                            if (distance > menuRadius) {
                                menuVisible = false
                                selectedActionIndex = null
                            }
                        }
                    }
                )
            }
            .pointerInput(menuVisible) {
                if (menuVisible) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val position = event.changes.firstOrNull()?.position ?: continue

                            // Calculate angle and distance from menu center
                            val dx = position.x - menuPosition.x
                            val dy = position.y - menuPosition.y
                            val distance = sqrt(dx * dx + dy * dy)

                            // Only process if pointer is within menu radius range
                            if (distance > 50f && distance < 150f) {
                                // Calculate angle in degrees (0 is right, going clockwise)
                                var angle = atan2(dy, dx) * 180 / PI.toFloat()
                                if (angle < 0) angle += 360

                                // Determine which action is selected based on angle
                                val actionCount = actions.size
                                val sectorAngle = 360f / actionCount
                                val newSelectedIndex = ((angle + sectorAngle / 2) % 360 / sectorAngle).toInt()

                                if (selectedActionIndex != newSelectedIndex) {
                                    selectedActionIndex = newSelectedIndex
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            } else {
                                selectedActionIndex = null
                            }

                            // Check for pointer up to execute action
                            if (event.changes.any { it.changedToUp() }) {
                                selectedActionIndex?.let { index ->
                                    if (index in actions.indices) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onActionSelected(actions[index])
                                        actions[index].action()
                                    }
                                }
                                menuVisible = false
                                selectedActionIndex = null
                                break
                            }
                        }
                    }
                }
            }
    ) {
        // Draw radial menu when visible
        if (menuVisible) {
            // Extract colors outside of Canvas
            val primaryColor = MaterialTheme.colorScheme.primary

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(menuScale)
            ) {
                val menuRadius = 150f
                val innerRadius = 50f
                val actionCount = actions.size

                // Draw semi-transparent background overlay
                drawRect(
                    color = Color.Black.copy(alpha = 0.3f),
                    size = size
                )

                // Draw center circle
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = innerRadius,
                    center = menuPosition
                )

                // Draw sectors for each action
                val sectorAngle = 360f / actionCount
                for (i in actions.indices) {
                    val startAngle = i * sectorAngle
                    val isSelected = selectedActionIndex == i

                    // Draw sector
                    drawArc(
                        color = if (isSelected) {
                            primaryColor
                        } else {
                            Color.White.copy(alpha = 0.7f)
                        },
                        startAngle = startAngle,
                        sweepAngle = sectorAngle,
                        useCenter = true,
                        topLeft = Offset(
                            menuPosition.x - menuRadius,
                            menuPosition.y - menuRadius
                        ),
                        size = Size(menuRadius * 2, menuRadius * 2),
                        alpha = if (isSelected) 0.9f else 0.7f
                    )

                    // Draw sector outline
                    drawArc(
                        color = Color.White,
                        startAngle = startAngle,
                        sweepAngle = sectorAngle,
                        useCenter = true,
                        topLeft = Offset(
                            menuPosition.x - menuRadius,
                            menuPosition.y - menuRadius
                        ),
                        size = Size(menuRadius * 2, menuRadius * 2),
                        style = Stroke(width = 2f),
                        alpha = 0.5f
                    )

                    // Calculate position for icon
                    val iconAngle = (startAngle + sectorAngle / 2) * PI.toFloat() / 180
                    val iconRadius = (innerRadius + menuRadius) / 2
                    val iconX = menuPosition.x + cos(iconAngle) * iconRadius
                    val iconY = menuPosition.y + sin(iconAngle) * iconRadius

                    // Draw icon placeholder (in a real implementation, you would draw the actual icon)
                    drawCircle(
                        color = if (isSelected) {
                            Color.White
                        } else {
                            Color.Black.copy(alpha = 0.7f)
                        },
                        radius = 20f,
                        center = Offset(iconX, iconY)
                    )
                }
            }

            // Draw action icons and labels
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                actions.forEachIndexed { index, action ->
                    val sectorAngle = 360f / actions.size
                    val iconAngle = (index * sectorAngle + sectorAngle / 2) * PI.toFloat() / 180
                    val iconRadius = 100f // Between innerRadius and menuRadius
                    val iconX = menuPosition.x + cos(iconAngle) * iconRadius
                    val iconY = menuPosition.y + sin(iconAngle) * iconRadius

                    val isSelected = selectedActionIndex == index

                    // Icon
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.name,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .offset(
                                x = with(LocalDensity.current) { (iconX - 20).toDp() },
                                y = with(LocalDensity.current) { (iconY - 20).toDp() }
                            )
                            .scale(if (isSelected) 1.2f else 1f)
                    )

                    // Label (only show for selected action)
                    if (isSelected) {
                        val labelAngle = iconAngle
                        val labelRadius = 180f
                        val labelX = menuPosition.x + cos(labelAngle) * labelRadius
                        val labelY = menuPosition.y + sin(labelAngle) * labelRadius

                        Text(
                            text = action.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .offset(
                                    x = with(LocalDensity.current) { (labelX - 50).toDp() },
                                    y = with(LocalDensity.current) { (labelY - 10).toDp() }
                                )
                                .width(100.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(4.dp)
                        )
                    }
                }

                // Center text
                Text(
                    text = "Menu",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(
                            x = with(LocalDensity.current) { (menuPosition.x - 25).toDp() },
                            y = with(LocalDensity.current) { (menuPosition.y - 10).toDp() }
                        )
                        .width(50.dp)
                )
            }
        }
    }
}

/**
 * A component that provides a swipe gesture area with visual feedback
 */
@Composable
fun SwipeGestureArea(
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onSwipeUp: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    swipeThreshold: Float = 100f,
    content: @Composable () -> Unit
) {
    var swipeDirection by remember { mutableStateOf<String?>(null) }
    var swipeProgress by remember { mutableStateOf(0f) }
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Animation for swipe indicator
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (swipeDirection != null) 0.7f else 0f,
        animationSpec = tween(200),
        label = "indicatorAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { swipeDirection = null },
                    onDragEnd = {
                        if (abs(swipeProgress) > swipeThreshold) {
                            if (swipeProgress > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }

                        // Reset after a short delay
                        coroutineScope.launch {
                            delay(200)
                            swipeDirection = null
                            swipeProgress = 0f
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        swipeProgress += dragAmount
                        swipeDirection = if (dragAmount > 0) "right" else "left"

                        // Provide haptic feedback at threshold
                        if (abs(swipeProgress) > swipeThreshold && abs(swipeProgress - dragAmount) <= swipeThreshold) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { swipeDirection = null },
                    onDragEnd = {
                        if (abs(swipeProgress) > swipeThreshold) {
                            if (swipeProgress > 0) {
                                onSwipeDown()
                            } else {
                                onSwipeUp()
                            }
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }

                        // Reset after a short delay
                        coroutineScope.launch {
                            delay(200)
                            swipeDirection = null
                            swipeProgress = 0f
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        swipeProgress += dragAmount
                        swipeDirection = if (dragAmount > 0) "down" else "up"

                        // Provide haptic feedback at threshold
                        if (abs(swipeProgress) > swipeThreshold && abs(swipeProgress - dragAmount) <= swipeThreshold) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                )
            }
    ) {
        // Main content
        content()

        // Extract colors outside of Box
        val primaryColor = MaterialTheme.colorScheme.primary

        // Swipe indicators
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(indicatorAlpha)
        ) {
            // Left indicator
            if (swipeDirection == "left") {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(50.dp)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    primaryColor,
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Swipe Left",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                    )
                }
            }

            // Right indicator
            if (swipeDirection == "right") {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(50.dp)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    primaryColor
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Swipe Right",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                    )
                }
            }

            // Up indicator
            if (swipeDirection == "up") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor,
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Swipe Up",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                }
            }

            // Down indicator
            if (swipeDirection == "down") {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    primaryColor
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Swipe Down",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

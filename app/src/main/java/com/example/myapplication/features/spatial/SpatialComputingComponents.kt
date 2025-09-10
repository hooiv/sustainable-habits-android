package com.example.myapplication.features.spatial

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.Offset3D
import com.example.myapplication.core.data.model.Rotation3D
import com.example.myapplication.core.data.model.SpatialObject
import com.example.myapplication.core.data.model.SpatialObjectType
import com.example.myapplication.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*



/**
 * A component that provides a 3D spatial environment for habit visualization
 */
@Composable
fun HabitSpatialEnvironment(
    modifier: Modifier = Modifier,
    spatialObjects: List<SpatialObject> = emptyList(),
    onObjectClick: (SpatialObject) -> Unit = {}
) {
    // Camera state
    var cameraRotationX by remember { mutableStateOf(20f) }
    var cameraRotationY by remember { mutableStateOf(20f) }
    var cameraScale by remember { mutableStateOf(1f) }
    var cameraPosition by remember { mutableStateOf(Offset(0f, 0f)) }

    // Selected object state
    var selectedObjectId by remember { mutableStateOf<String?>(null) }

    // Animation state
    val infiniteTransition = rememberInfiniteTransition(label = "spatialAnim")
    val floatAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floatAnim"
    )

    // Create text measurer for drawing text
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E).copy(alpha = 0.8f),
                        Color(0xFF000000)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    // Update camera
                    cameraScale *= zoom
                    cameraScale = cameraScale.coerceIn(0.5f, 3f)

                    cameraPosition += pan

                    // Limit camera position
                    cameraPosition = Offset(
                        cameraPosition.x.coerceIn(-500f, 500f),
                        cameraPosition.y.coerceIn(-500f, 500f)
                    )

                    // Update rotation
                    cameraRotationY += pan.x * 0.2f
                    cameraRotationX += pan.y * 0.2f

                    // Limit rotation
                    cameraRotationX = cameraRotationX.coerceIn(-60f, 60f)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    // Update camera rotation
                    cameraRotationY += dragAmount.x * 0.2f
                    cameraRotationX += dragAmount.y * 0.2f

                    // Limit rotation
                    cameraRotationX = cameraRotationX.coerceIn(-60f, 60f)
                }
            }
    ) {
        // Draw spatial environment
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2 + cameraPosition.x
            val centerY = size.height / 2 + cameraPosition.y

            // Draw grid
            val gridSize = 100f
            val gridExtent = 5
            val gridColor = Color.White.copy(alpha = 0.2f)

            translate(centerX, centerY) {
                rotate(degrees = cameraRotationX, pivot = Offset.Zero) {
                    rotate(degrees = cameraRotationY, pivot = Offset.Zero) {
                    scale(cameraScale) {
                        // Draw grid lines
                        for (i in -gridExtent..gridExtent) {
                            // X-axis lines
                            drawLine(
                                color = gridColor,
                                start = Offset(i * gridSize, -gridExtent * gridSize, 0f),
                                end = Offset(i * gridSize, gridExtent * gridSize, 0f),
                                strokeWidth = 1f
                            )

                            // Z-axis lines
                            drawLine(
                                color = gridColor,
                                start = Offset(-gridExtent * gridSize, i * gridSize, 0f),
                                end = Offset(gridExtent * gridSize, i * gridSize, 0f),
                                strokeWidth = 1f
                            )
                        }

                        // Draw axes
                        drawLine(
                            color = Color.Red.copy(alpha = 0.7f),
                            start = Offset(0f, 0f, 0f),
                            end = Offset(gridSize * 2, 0f, 0f),
                            strokeWidth = 2f
                        )

                        drawLine(
                            color = Color.Green.copy(alpha = 0.7f),
                            start = Offset(0f, 0f, 0f),
                            end = Offset(0f, gridSize * 2, 0f),
                            strokeWidth = 2f
                        )

                        drawLine(
                            color = Color.Blue.copy(alpha = 0.7f),
                            start = Offset(0f, 0f, 0f),
                            end = Offset(0f, 0f, gridSize * 2),
                            strokeWidth = 2f
                        )

                        // Draw spatial objects
                        // Sort objects by Z for proper depth rendering
                        val sortedObjects = spatialObjects.sortedBy {
                            // Apply camera rotation to get view-space Z
                            val rotatedZ = it.position.z * cos(Math.toRadians(cameraRotationY.toDouble())) -
                                          it.position.x * sin(Math.toRadians(cameraRotationY.toDouble()))
                            rotatedZ
                        }

                        for (obj in sortedObjects) {
                            val isSelected = obj.id == selectedObjectId

                            // Apply object position and rotation
                            translate(obj.position.x, obj.position.y) {
                                rotate(degrees = obj.rotation.x, pivot = Offset.Zero) {
                                    rotate(degrees = obj.rotation.y, pivot = Offset.Zero) {
                                        scale(obj.scale) {
                                        // Draw object based on type
                                        when (obj.type) {
                                            SpatialObjectType.HABIT_SPHERE -> {
                                                // Draw sphere
                                                drawCircle(
                                                    color = Color(obj.color).copy(alpha = 0.7f),
                                                    radius = 50f,
                                                    style = if (isSelected) Fill else Stroke(width = 2f)
                                                )

                                                // Draw glow effect
                                                drawCircle(
                                                    color = Color(obj.color).copy(alpha = 0.3f),
                                                    radius = 60f + 10f * sin(floatAnimation * 2 * PI.toFloat())
                                                )
                                            }
                                            SpatialObjectType.STREAK_TOWER -> {
                                                // Draw tower
                                                val towerHeight = 100f
                                                val towerWidth = 40f

                                                drawRect(
                                                    color = Color(obj.color).copy(alpha = 0.7f),
                                                    topLeft = Offset(-towerWidth / 2, -towerHeight),
                                                    size = Size(towerWidth, towerHeight),
                                                    style = if (isSelected) Fill else Stroke(width = 2f)
                                                )

                                                // Draw glow effect
                                                drawRect(
                                                    color = Color(obj.color).copy(alpha = 0.3f),
                                                    topLeft = Offset(
                                                        -towerWidth / 2 - 5f,
                                                        -towerHeight - 5f
                                                    ),
                                                    size = Size(
                                                        towerWidth + 10f,
                                                        towerHeight + 10f
                                                    )
                                                )
                                            }
                                            SpatialObjectType.ACHIEVEMENT_STAR -> {
                                                // Draw star
                                                val starPath = createStarPath(50f)

                                                drawPath(
                                                    path = starPath,
                                                    color = Color(obj.color).copy(alpha = 0.7f),
                                                    style = if (isSelected) Fill else Stroke(width = 2f)
                                                )

                                                // Draw glow effect
                                                drawPath(
                                                    path = createStarPath(60f + 10f * sin(floatAnimation * 2 * PI.toFloat())),
                                                    color = Color(obj.color).copy(alpha = 0.3f)
                                                )
                                            }
                                            SpatialObjectType.GOAL_PYRAMID -> {
                                                // Draw pyramid
                                                val pyramidPath = Path().apply {
                                                    // Base
                                                    moveTo(-50f, 50f)
                                                    lineTo(50f, 50f)
                                                    lineTo(50f, -50f)
                                                    lineTo(-50f, -50f)
                                                    close()

                                                    // Top
                                                    moveTo(0f, 0f)
                                                    lineTo(-50f, 50f)
                                                    moveTo(0f, 0f)
                                                    lineTo(50f, 50f)
                                                    moveTo(0f, 0f)
                                                    lineTo(50f, -50f)
                                                    moveTo(0f, 0f)
                                                    lineTo(-50f, -50f)
                                                }

                                                drawPath(
                                                    path = pyramidPath,
                                                    color = Color(obj.color).copy(alpha = 0.7f),
                                                    style = Stroke(width = if (isSelected) 3f else 2f)
                                                )
                                            }
                                            SpatialObjectType.CATEGORY_CUBE -> {
                                                // Draw cube
                                                val cubeSize = 50f

                                                // Front face
                                                drawRect(
                                                    color = Color(obj.color).copy(alpha = 0.7f),
                                                    topLeft = Offset(-cubeSize / 2, -cubeSize / 2),
                                                    size = Size(cubeSize, cubeSize),
                                                    style = if (isSelected) Fill else Stroke(width = 2f)
                                                )

                                                // Draw perspective lines
                                                val perspectiveOffset = 20f

                                                // Top-left corner
                                                drawLine(
                                                    color = Color(obj.color).copy(alpha = 0.5f),
                                                    start = Offset(-cubeSize / 2, -cubeSize / 2),
                                                    end = Offset(-cubeSize / 2 - perspectiveOffset, -cubeSize / 2 - perspectiveOffset),
                                                    strokeWidth = 2f
                                                )

                                                // Top-right corner
                                                drawLine(
                                                    color = Color(obj.color).copy(alpha = 0.5f),
                                                    start = Offset(cubeSize / 2, -cubeSize / 2),
                                                    end = Offset(cubeSize / 2 + perspectiveOffset, -cubeSize / 2 - perspectiveOffset),
                                                    strokeWidth = 2f
                                                )

                                                // Bottom-right corner
                                                drawLine(
                                                    color = Color(obj.color).copy(alpha = 0.5f),
                                                    start = Offset(cubeSize / 2, cubeSize / 2),
                                                    end = Offset(cubeSize / 2 + perspectiveOffset, cubeSize / 2 + perspectiveOffset),
                                                    strokeWidth = 2f
                                                )

                                                // Bottom-left corner
                                                drawLine(
                                                    color = Color(obj.color).copy(alpha = 0.5f),
                                                    start = Offset(-cubeSize / 2, cubeSize / 2),
                                                    end = Offset(-cubeSize / 2 - perspectiveOffset, cubeSize / 2 + perspectiveOffset),
                                                    strokeWidth = 2f
                                                )

                                                // Back face
                                                drawRect(
                                                    color = Color(obj.color).copy(alpha = 0.3f),
                                                    topLeft = Offset(-cubeSize / 2 - perspectiveOffset, -cubeSize / 2 - perspectiveOffset),
                                                    size = Size(cubeSize + perspectiveOffset * 2, cubeSize + perspectiveOffset * 2),
                                                    style = Stroke(width = 2f)
                                                )
                                            }
                                            SpatialObjectType.REMINDER_CLOCK -> {
                                                // Draw clock
                                                val clockRadius = 50f

                                                drawCircle(
                                                    color = Color(obj.color).copy(alpha = 0.7f),
                                                    radius = clockRadius,
                                                    style = if (isSelected) Fill else Stroke(width = 2f)
                                                )

                                                // Draw clock hands
                                                val hourAngle = floatAnimation * 2 * PI.toFloat()
                                                val minuteAngle = floatAnimation * 24 * PI.toFloat()

                                                // Hour hand
                                                drawLine(
                                                    color = Color.White,
                                                    start = Offset(0f, 0f),
                                                    end = Offset(
                                                        clockRadius * 0.5f * sin(hourAngle),
                                                        -clockRadius * 0.5f * cos(hourAngle)
                                                    ),
                                                    strokeWidth = 3f
                                                )

                                                // Minute hand
                                                drawLine(
                                                    color = Color.White,
                                                    start = Offset(0f, 0f),
                                                    end = Offset(
                                                        clockRadius * 0.7f * sin(minuteAngle),
                                                        -clockRadius * 0.7f * cos(minuteAngle)
                                                    ),
                                                    strokeWidth = 2f
                                                )
                                            }
                                        }

                                        // Draw label if provided
                                        obj.label?.let { label ->
                                            drawText(
                                                textMeasurer = textMeasurer,
                                                text = label,
                                                topLeft = Offset(-50f, 60f),
                                                style = TextStyle(
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }
        }

        // Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = {
                    // Reset camera
                    cameraRotationX = 20f
                    cameraRotationY = 20f
                    cameraScale = 1f
                    cameraPosition = Offset.Zero
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset View",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = {
                    // Zoom in
                    cameraScale *= 1.2f
                    cameraScale = cameraScale.coerceAtMost(3f)
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom In",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = {
                    // Zoom out
                    cameraScale /= 1.2f
                    cameraScale = cameraScale.coerceAtLeast(0.5f)
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "Zoom Out",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Instructions
        Text(
            text = "Drag to rotate • Pinch to zoom",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Helper function to create a star path
 */
private fun createStarPath(radius: Float): Path {
    val path = Path()
    val outerRadius = radius
    val innerRadius = radius * 0.4f
    val centerX = 0f
    val centerY = 0f

    for (i in 0 until 10) {
        val angle = (i * 36) * (PI / 180f)
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = centerX + r * cos(angle).toFloat()
        val y = centerY + r * sin(angle).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    path.close()
    return path
}

/**
 * Extension function to create a 3D offset
 */
private fun Offset(x: Float, y: Float, z: Float): Offset {
    // Project 3D point to 2D
    // This is a simple orthographic projection
    return Offset(x, y)
}

/**
 * A component that displays a spatial habit garden
 */
@Composable
fun SpatialHabitGarden(
    habits: List<Habit>,
    modifier: Modifier = Modifier,
    onHabitClick: (Habit) -> Unit = {}
) {
    // Convert habits to spatial objects
    val spatialObjects = remember(habits) {
        habits.mapIndexed { index, habit ->
            // Calculate position in a spiral pattern
            val angle = index * 30f * (PI / 180f)
            val radius = 100f + index * 20f
            val x = radius * cos(angle).toFloat()
            val y = radius * sin(angle).toFloat()

            // Determine object type based on habit properties
            val objectType = when {
                habit.streak > 10 -> SpatialObjectType.STREAK_TOWER
                habit.unlockedBadges.isNotEmpty() -> SpatialObjectType.ACHIEVEMENT_STAR
                habit.goal > 1 -> SpatialObjectType.GOAL_PYRAMID
                habit.category != null -> SpatialObjectType.CATEGORY_CUBE
                habit.reminderTime != null -> SpatialObjectType.REMINDER_CLOCK
                else -> SpatialObjectType.HABIT_SPHERE
            }

            // Determine color based on habit category
            val color = when (habit.category) {
                "Health" -> Color(0xFF4CAF50)
                "Fitness" -> Color(0xFFF44336)
                "Learning" -> Color(0xFF2196F3)
                "Productivity" -> Color(0xFFFF9800)
                "Mindfulness" -> Color(0xFF9C27B0)
                else -> Color(0xFF3F51B5)
            }

            SpatialObject(
                type = objectType,
                position = Offset3D(x, y, 0f),
                rotation = Rotation3D(0f, 0f, 0f),
                scale = 0.5f + (habit.streak.coerceAtMost(10) / 10f) * 0.5f,
                color = color.value.toLong(),
                label = habit.name,
                relatedHabitId = habit.id
            )
        }
    }

    HabitSpatialEnvironment(
        modifier = modifier,
        spatialObjects = spatialObjects,
        onObjectClick = { spatialObject: SpatialObject ->
            // Find the corresponding habit and trigger the click handler
            spatialObject.relatedHabitId?.let { habitId: String ->
                habits.find { it.id == habitId }?.let { habit: Habit ->
                    onHabitClick(habit)
                }
            }
        }
    )
}

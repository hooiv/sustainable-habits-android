package com.example.myapplication.features.ar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.Habit
import com.example.myapplication.ui.animation.*
import com.example.myapplication.ui.theme.Brown
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*

/**
 * Data class representing an AR object
 */
data class ARObject(
    val id: String = UUID.randomUUID().toString(),
    val type: ARObjectType,
    val position: Offset = Offset.Zero,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val color: Color = Color.White,
    val label: String? = null,
    val relatedHabitId: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Enum for different types of AR objects
 */
enum class ARObjectType {
    HABIT_TREE,
    STREAK_FLAME,
    ACHIEVEMENT_TROPHY,
    PROGRESS_CHART,
    HABIT_REMINDER,
    MOTIVATION_OBJECT,
    CUSTOM_OBJECT
}

/**
 * A component that simulates an AR view for habit visualization
 */
@Composable
fun ARHabitVisualization(
    modifier: Modifier = Modifier,
    arObjects: List<ARObject> = emptyList(),
    onObjectClick: (ARObject) -> Unit = {},
    onAddObject: (ARObjectType, Offset) -> Unit = { _, _ -> }
) {
    var cameraActive by remember { mutableStateOf(true) }
    var selectedObject by remember { mutableStateOf<ARObject?>(null) }
    var draggedObjectId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val coroutineScope = rememberCoroutineScope()

    // Mutable list of AR objects that can be modified
    val mutableARObjects = remember { arObjects.toMutableStateList() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.1f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // Check if we tapped on an object
                        val tappedObject = mutableARObjects.firstOrNull { arObject ->
                            val objectRadius = 50f * arObject.scale
                            val distance = (arObject.position - offset).getDistance()
                            distance < objectRadius
                        }

                        if (tappedObject != null) {
                            selectedObject = tappedObject
                            onObjectClick(tappedObject)
                        } else {
                            // Add new object at tap location if camera is active
                            if (cameraActive) {
                                coroutineScope.launch {
                                    // Show object selection dialog
                                    // For now, just add a random object
                                    val randomType = ARObjectType.values().random()
                                    onAddObject(randomType, offset)

                                    // Add the object to our local list
                                    mutableARObjects.add(
                                        ARObject(
                                            type = randomType,
                                            position = offset,
                                            color = Color.hsl(
                                                Random().nextFloat() * 360f,
                                                0.7f,
                                                0.6f
                                            )
                                        )
                                    )
                                }
                            }

                            selectedObject = null
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Find object being dragged
                        val draggedObject = mutableARObjects.firstOrNull { arObject ->
                            val objectRadius = 50f * arObject.scale
                            val distance = (arObject.position - offset).getDistance()
                            distance < objectRadius
                        }

                        draggedObjectId = draggedObject?.id
                        dragOffset = offset
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount

                        // Update position of dragged object
                        draggedObjectId?.let { id ->
                            val index = mutableARObjects.indexOfFirst { it.id == id }
                            if (index >= 0) {
                                mutableARObjects[index] = mutableARObjects[index].copy(
                                    position = mutableARObjects[index].position + dragAmount
                                )
                            }
                        }
                    },
                    onDragEnd = {
                        draggedObjectId = null
                    },
                    onDragCancel = {
                        draggedObjectId = null
                    }
                )
            }
    ) {
        // Simulated camera background (would be a real camera feed in a real AR app)
        if (cameraActive) {
            // Subtle grid pattern to simulate AR space
            Canvas(modifier = Modifier.matchParentSize()) {
                val gridSize = 50f
                val gridColor = Color.White.copy(alpha = 0.1f)

                // Draw horizontal grid lines
                for (y in 0..(size.height / gridSize).toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y * gridSize),
                        end = Offset(size.width, y * gridSize),
                        strokeWidth = 1f
                    )
                }

                // Draw vertical grid lines
                for (x in 0..(size.width / gridSize).toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x * gridSize, 0f),
                        end = Offset(x * gridSize, size.height),
                        strokeWidth = 1f
                    )
                }
            }

            // Particle effect to enhance AR feel
            ParticleSystem(
                modifier = Modifier.matchParentSize(),
                particleCount = 30,
                particleColor = Color.White,
                particleSize = 2.dp,
                maxSpeed = 0.2f,
                fadeDistance = 0.9f,
                particleShape = ParticleShape.CIRCLE,
                particleEffect = ParticleEffect.FLOAT,
                colorVariation = true,
                glowEffect = true
            )
        }

        // Render AR objects
        mutableARObjects.forEach { arObject ->
            val isSelected = selectedObject?.id == arObject.id
            val isDragged = draggedObjectId == arObject.id

            ARObjectRenderer(
                arObject = arObject,
                isSelected = isSelected,
                isDragged = isDragged,
                onClick = { onObjectClick(arObject) }
            )
        }

        // AR controls overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            // Object info card for selected object
            selectedObject?.let { selected ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = selected.label ?: selected.type.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (selected.relatedHabitId != null) {
                            Text(
                                text = "Related to habit ID: ${selected.relatedHabitId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        // Object controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = {
                                val index = mutableARObjects.indexOfFirst { it.id == selected.id }
                                if (index >= 0) {
                                    mutableARObjects[index] = mutableARObjects[index].copy(
                                        scale = mutableARObjects[index].scale * 1.2f
                                    )
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Scale Up"
                                )
                            }

                            IconButton(onClick = {
                                val index = mutableARObjects.indexOfFirst { it.id == selected.id }
                                if (index >= 0) {
                                    mutableARObjects[index] = mutableARObjects[index].copy(
                                        scale = mutableARObjects[index].scale * 0.8f
                                    )
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ZoomOut,
                                    contentDescription = "Scale Down"
                                )
                            }

                            IconButton(onClick = {
                                val index = mutableARObjects.indexOfFirst { it.id == selected.id }
                                if (index >= 0) {
                                    mutableARObjects[index] = mutableARObjects[index].copy(
                                        rotation = mutableARObjects[index].rotation + 45f
                                    )
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = "Rotate"
                                )
                            }

                            IconButton(onClick = {
                                mutableARObjects.removeAll { it.id == selected.id }
                                selectedObject = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // AR control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FloatingActionButton(
                    onClick = { cameraActive = !cameraActive },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = if (cameraActive) Icons.Outlined.CameraAlt else Icons.Default.CameraAlt,
                        contentDescription = if (cameraActive) "Camera On" else "Camera Off"
                    )
                }

                FloatingActionButton(
                    onClick = {
                        // Add a random object in the center
                        val center = Offset(
                            x = 500f, // This would be the actual center in a real implementation
                            y = 500f
                        )
                        val randomType = ARObjectType.values().random()
                        onAddObject(randomType, center)

                        // Add the object to our local list
                        mutableARObjects.add(
                            ARObject(
                                type = randomType,
                                position = center,
                                color = Color.hsl(
                                    Random().nextFloat() * 360f,
                                    0.7f,
                                    0.6f
                                )
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Object"
                    )
                }

                FloatingActionButton(
                    onClick = {
                        // Clear all objects
                        mutableARObjects.clear()
                        selectedObject = null
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear All"
                    )
                }
            }
        }
    }
}

/**
 * Renders an individual AR object
 */
@Composable
fun ARObjectRenderer(
    arObject: ARObject,
    isSelected: Boolean = false,
    isDragged: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arObjectAnimation")

    // Hover animation
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = AnimeEasing.EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hoverOffset"
    )

    // Rotation animation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    // Selection animation
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selectionScale"
    )

    // Drag animation
    val dragScale by animateFloatAsState(
        targetValue = if (isDragged) 1.1f else 1f,
        animationSpec = tween(200),
        label = "dragScale"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(x = arObject.position.x.toInt(), y = arObject.position.y.toInt()) }
            .size(100.dp * arObject.scale * selectionScale * dragScale)
            .offset(y = (hoverOffset * arObject.scale).dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Render different AR objects based on type
        when (arObject.type) {
            ARObjectType.HABIT_TREE -> {
                // Tree visualization
                Canvas(modifier = Modifier.fillMaxSize()) {
                    rotate(arObject.rotation + rotationAngle * 0.05f) {
                        // Draw tree trunk
                        drawRect(
                            color = Brown,
                            topLeft = Offset(size.width * 0.45f, size.height * 0.5f),
                            size = Size(size.width * 0.1f, size.height * 0.4f)
                        )

                        // Draw tree leaves
                        drawCircle(
                            color = arObject.color,
                            radius = size.width * 0.3f,
                            center = Offset(size.width * 0.5f, size.height * 0.3f)
                        )
                    }
                }
            }
            ARObjectType.STREAK_FLAME -> {
                // Flame visualization
                MorphingBlob(
                    modifier = Modifier.fillMaxSize(),
                    color = arObject.color,
                    pointCount = 12,
                    minRadius = 0.7f,
                    maxRadius = 0.9f,
                    animationDuration = 2000
                )
            }
            ARObjectType.ACHIEVEMENT_TROPHY -> {
                // Trophy visualization
                Canvas(modifier = Modifier.fillMaxSize()) {
                    rotate(arObject.rotation + rotationAngle * 0.1f) {
                        // Draw trophy cup
                        drawArc(
                            color = arObject.color,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(size.width * 0.2f, size.height * 0.2f),
                            size = Size(size.width * 0.6f, size.width * 0.6f)
                        )

                        // Draw trophy base
                        drawRect(
                            color = arObject.color,
                            topLeft = Offset(size.width * 0.4f, size.height * 0.6f),
                            size = Size(size.width * 0.2f, size.height * 0.3f)
                        )
                    }
                }
            }
            else -> {
                // Default visualization for other types
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(arObject.color.copy(alpha = 0.7f))
                        .border(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.8f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (arObject.type) {
                            ARObjectType.PROGRESS_CHART -> Icons.Default.ShowChart
                            ARObjectType.HABIT_REMINDER -> Icons.Default.Alarm
                            ARObjectType.MOTIVATION_OBJECT -> Icons.Default.EmojiEvents
                            ARObjectType.CUSTOM_OBJECT -> Icons.Default.Star
                            else -> Icons.Default.Visibility
                        },
                        contentDescription = arObject.type.name,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp * arObject.scale)
                    )
                }
            }
        }

        // Label if provided
        arObject.label?.let { label ->
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp * arObject.scale,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (10 * arObject.scale).dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 2.dp,
                        color = Color.Yellow,
                        shape = CircleShape
                    )
            )
        }
    }
}

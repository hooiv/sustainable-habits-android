package com.example.myapplication.features.spatial

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.data.repository.SpatialObject
import com.example.myapplication.data.repository.SpatialObjectType
import com.example.myapplication.data.repository.Offset3D
import kotlinx.coroutines.launch
import kotlin.math.*



/**
 * 3D Spatial Environment
 */
@Composable
fun SpatialEnvironment(
    spatialObjects: List<SpatialObject>,
    onObjectClick: (SpatialObject) -> Unit,
    modifier: Modifier = Modifier
) {
    // State for rotation
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }

    // State for zoom
    var scale by remember { mutableStateOf(1f) }

    // State for pan
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Animation for objects
    val infiniteTransition = rememberInfiniteTransition(label = "spatial")
    val animationValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spatial_animation"
    )

    Box(
        modifier = modifier
            .background(Color(0xFF121212))
            // Gesture handling for rotation, zoom, and pan
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    rotationY += pan.x * 0.5f
                    rotationX -= pan.y * 0.5f
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        // Grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationX = rotationX
                    rotationY = rotationY
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
        ) {
            // Draw grid
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val gridSize = 50f
                val gridColor = Color.Gray.copy(alpha = 0.3f)

                // Draw horizontal grid lines
                for (i in 0..(height / gridSize).toInt()) {
                    val y = i * gridSize
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                // Draw vertical grid lines
                for (i in 0..(width / gridSize).toInt()) {
                    val x = i * gridSize
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                }
            }

            // Draw spatial objects
            spatialObjects.forEach { obj ->
                // Calculate position based on 3D to 2D projection
                val x = obj.position.x + 100f // Center offset
                val y = obj.position.y + 100f // Center offset
                val z = obj.position.z

                // Apply animation
                val animatedScale = obj.scale * (1f + sin(animationValue * 2 * PI.toFloat()) * 0.1f)

                // Draw object based on type
                Box(
                    modifier = Modifier
                        .offset(x.dp, y.dp)
                        .size((30 * animatedScale).dp)
                        .clip(
                            when (obj.type) {
                                SpatialObjectType.CATEGORY_CUBE -> RoundedCornerShape(2.dp)
                                SpatialObjectType.HABIT_SPHERE -> CircleShape
                                SpatialObjectType.STREAK_TOWER -> RoundedCornerShape(50)
                                SpatialObjectType.GOAL_PYRAMID -> RoundedCornerShape(4.dp)
                                SpatialObjectType.ACHIEVEMENT_STAR -> RoundedCornerShape(16.dp)
                                SpatialObjectType.REMINDER_CLOCK -> CircleShape
                            }
                        )
                        .background(obj.color)
                        .clickable { onObjectClick(obj) },
                    contentAlignment = Alignment.Center
                ) {
                    if (obj.type == SpatialObjectType.REMINDER_CLOCK) {
                        Text(
                            text = obj.label.take(1), // First letter as label
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = (12 * animatedScale).sp
                        )
                    }
                }
            }
        }

        // Instructions
        Text(
            text = "Pinch to zoom, drag to rotate",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun SpatialComputingScreen(
    navController: NavController,
    habitId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: SpatialComputingViewModel = hiltViewModel()
) {
    val spatialObjects by viewModel.spatialObjects.collectAsState()
    val selectedObject by viewModel.selectedObject.collectAsState()
    val isPlacementMode by viewModel.isPlacementMode.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Initialize with specific habit if provided
    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.setCurrentHabitId(habitId)
        }
        viewModel.loadSpatialObjects()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spatial Computing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Error message
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Spatial environment
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 3D environment
                    SpatialEnvironment(
                        spatialObjects = spatialObjects,
                        onObjectClick = { obj -> viewModel.selectObject(obj.id) },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Controls overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.togglePlacementMode()
                                }
                            },
                            containerColor = if (isPlacementMode)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = if (isPlacementMode)
                                    Icons.Default.Close
                                else
                                    Icons.Default.Add,
                                contentDescription = if (isPlacementMode) "Cancel" else "Add Object"
                            )
                        }

                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.resetView()
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset View"
                            )
                        }
                    }
                }
            }

            // Selected object details
            selectedObject?.let { obj ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Selected Object: ${obj.label}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Type: ${obj.type.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = "Position: (${obj.position.x.toInt()}, ${obj.position.y.toInt()}, ${obj.position.z.toInt()})",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = "Scale: ${obj.scale}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.deleteObject(obj.id)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }

            // Object type selection (when in placement mode)
            if (isPlacementMode) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Select Object Type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        SpatialObjectTypeSelector(
                            onTypeSelected = { type ->
                                coroutineScope.launch {
                                    viewModel.setSelectedObjectType(type)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

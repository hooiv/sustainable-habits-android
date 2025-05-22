package com.example.myapplication.features.spatial

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.data.repository.SpatialObject
import com.example.myapplication.ui.components.AppScaffold
import kotlinx.coroutines.launch

/**
 * Spatial Computing Screen
 * Displays a 3D spatial environment for habit visualization
 */
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
                    // Placeholder for SpatialEnvironment
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3D Environment with ${spatialObjects.size} objects",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

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
                            }
                        )
                    }
                }
            }
        }
    }
}

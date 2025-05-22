package com.example.myapplication.features.ar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.data.model.Habit
import com.example.myapplication.ui.components.AppScaffold
import com.google.ar.core.TrackingState
import androidx.compose.runtime.collectAsState

/**
 * Screen for AR visualization of habits
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARScreen(
    navController: NavController,
    habit: Habit? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Create AR renderer
    val arRenderer = remember { ARRenderer(context) }

    // Collect AR state
    val arObjects by arRenderer.arObjects.collectAsState()
    val trackingState by arRenderer.trackingState.collectAsState()
    val isSessionResumed by arRenderer.isSessionResumed.collectAsState()
    val errorMessage by arRenderer.errorMessage.collectAsState()

    // Initialize AR session
    LaunchedEffect(Unit) {
        arRenderer.initializeSession()
        arRenderer.resumeSession()
    }

    // Clean up when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            arRenderer.pauseSession()
            arRenderer.cleanup()
        }
    }

    AppScaffold(
        title = habit?.name ?: "AR Visualization",
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // AR visualization
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f))
            ) {
                // AR objects visualization
                arObjects.forEach { arObject ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .size(120.dp)
                                .padding(8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = when (arObject.type) {
                                        ARObjectType.HABIT_TREE -> Icons.Default.Park
                                        ARObjectType.STREAK_FLAME -> Icons.Default.LocalFireDepartment
                                        ARObjectType.ACHIEVEMENT_TROPHY -> Icons.Default.EmojiEvents
                                        ARObjectType.PROGRESS_CHART -> Icons.Default.BarChart
                                        ARObjectType.HABIT_REMINDER -> Icons.Default.Notifications
                                        ARObjectType.MOTIVATION_OBJECT -> Icons.Default.Star
                                        ARObjectType.CUSTOM_OBJECT -> Icons.Default.Favorite
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = arObject.label ?: arObject.type.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // If no objects, show help text
                if (arObjects.isEmpty() && trackingState == TrackingState.TRACKING) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Tap the + button to place a habit visualization in AR",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Error message
            errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { arRenderer.resumeSession() }) {
                            Text("Retry")
                        }
                    }
                ) {
                    Text(error)
                }
            }

            // Controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                // Tracking state indicator
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (trackingState) {
                            TrackingState.TRACKING -> MaterialTheme.colorScheme.primaryContainer
                            TrackingState.PAUSED -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (trackingState) {
                                TrackingState.TRACKING -> Icons.Default.CheckCircle
                                TrackingState.PAUSED -> Icons.Default.Warning
                                else -> Icons.Default.Error
                            },
                            contentDescription = null,
                            tint = when (trackingState) {
                                TrackingState.TRACKING -> MaterialTheme.colorScheme.primary
                                TrackingState.PAUSED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = when (trackingState) {
                                TrackingState.TRACKING -> "Tracking"
                                TrackingState.PAUSED -> "Tracking Paused"
                                else -> "Tracking Lost"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Place habit button
                    FloatingActionButton(
                        onClick = {
                            if (isSessionResumed && trackingState == TrackingState.TRACKING) {
                                habit?.let { arRenderer.placeHabitVisualization(it) }
                                    ?: arRenderer.placeObjectInFront()
                            }
                        },
                        containerColor = if (isSessionResumed && trackingState == TrackingState.TRACKING)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Place Object",
                            tint = if (isSessionResumed && trackingState == TrackingState.TRACKING)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Clear all button
                    FloatingActionButton(
                        onClick = { arRenderer.clearAllObjects() },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear All",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

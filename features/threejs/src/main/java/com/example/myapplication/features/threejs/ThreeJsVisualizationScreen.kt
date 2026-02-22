package com.example.myapplication.features.threejs

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.core.ui.components.LoadingIndicator

/**
 * Screen for Three.js visualizations
 */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreeJsVisualizationScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    viewModel: ThreeJsVisualizationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val isSceneReady by viewModel.isSceneReady.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val visualizationType by viewModel.visualizationType.collectAsState()

    // Initialize WebView
    LaunchedEffect(Unit) {
        viewModel.loadHabits()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Three.js Visualization") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshVisualization() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }

                    IconButton(onClick = { viewModel.toggleVisualizationType() }) {
                        Icon(
                            when (visualizationType) {
                                ThreeJsVisualizationType.HABITS -> Icons.Default.Insights
                                ThreeJsVisualizationType.COMPLETIONS -> Icons.Default.CheckCircle
                                ThreeJsVisualizationType.STREAKS -> Icons.Default.Timeline
                            },
                            contentDescription = "Change Visualization"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Three.js WebView
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                        }

                        // Initialize Three.js integration
                        viewModel.initializeWebView(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Visualization type indicator
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = when (visualizationType) {
                            ThreeJsVisualizationType.HABITS -> Icons.Default.Insights
                            ThreeJsVisualizationType.COMPLETIONS -> Icons.Default.CheckCircle
                            ThreeJsVisualizationType.STREAKS -> Icons.Default.Timeline
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = when (visualizationType) {
                            ThreeJsVisualizationType.HABITS -> "Habits"
                            ThreeJsVisualizationType.COMPLETIONS -> "Completions"
                            ThreeJsVisualizationType.STREAKS -> "Streaks"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Visualization Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ControlButton(
                        icon = Icons.Default.ZoomIn,
                        label = "Zoom In",
                        onClick = { viewModel.zoomIn() }
                    )

                    ControlButton(
                        icon = Icons.Default.ZoomOut,
                        label = "Zoom Out",
                        onClick = { viewModel.zoomOut() }
                    )

                    ControlButton(
                        icon = Icons.Default.Refresh,
                        label = "Rotate",
                        onClick = { viewModel.rotate() }
                    )

                    ControlButton(
                        icon = Icons.Default.Animation,
                        label = "Animate",
                        onClick = { viewModel.animate() }
                    )
                }
            }

            // Loading indicator
            if (isLoading || !isSceneReady) {
                LoadingIndicator(message = "Loading visualization...")
            }

            // Error message
            errorMessage?.let { message ->
                if (message.isNotEmpty()) {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        action = {
                            TextButton(onClick = { viewModel.clearErrorMessage() }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(message)
                    }
                }
            }
        }
    }
}

/**
 * Control button component
 */
@Composable
fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Three.js visualization types
 */
enum class ThreeJsVisualizationType {
    HABITS,
    COMPLETIONS,
    STREAKS
}

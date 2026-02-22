package com.example.myapplication.features.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.core.ui.components.AppBar
import com.example.myapplication.core.ui.components.FeatureCard

/**
 * Advanced Features Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFeaturesScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            AppBar(
                title = "Advanced Features",
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Explore Advanced Features",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interaction Features
            SectionHeader(title = "Interaction Features")

            FeatureCard(
                title = "Gesture Controls",
                description = "Control the app with intuitive gestures",
                icon = Icons.Default.TouchApp,
                onClick = {
                    navController.navigate(NavRoutes.GESTURE_CONTROLS)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Analytics Features
            SectionHeader(title = "Analytics Features")

            FeatureCard(
                title = "Advanced Analytics",
                description = "Deep insights into your habit patterns",
                icon = Icons.Default.Insights,
                onClick = {
                    navController.navigate(NavRoutes.ADVANCED_ANALYTICS)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Visualization Features
            SectionHeader(title = "Visualization Features")

            FeatureCard(
                title = "Three.js Visualization",
                description = "Visualize your habits with Three.js",
                icon = Icons.Default.Animation,
                onClick = {
                    navController.navigate(NavRoutes.THREEJS_VISUALIZATION)
                }
            )

            FeatureCard(
                title = "Anime.js Animations",
                description = "Interactive animations with Anime.js",
                icon = Icons.Default.Star,
                onClick = {
                    navController.navigate(NavRoutes.ANIMEJS_ANIMATION)
                }
            )

            FeatureCard(
                title = "Spatial Computing",
                description = "Explore habits in 3D space",
                icon = Icons.Default.ViewInAr,
                onClick = {
                    navController.navigate(NavRoutes.SPATIAL_COMPUTING)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Biometric Features
            SectionHeader(title = "Biometric Features")

            FeatureCard(
                title = "Biometric Integration",
                description = "Connect your habits with biometric data",
                icon = Icons.Default.MonitorHeart,
                onClick = {
                    navController.navigate(NavRoutes.BIOMETRIC_INTEGRATION_GLOBAL)
                }
            )

            FeatureCard(
                title = "Biometric Visualization",
                description = "Visualize your biometric data",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                onClick = {
                    navController.navigate(NavRoutes.BIOMETRIC_INTEGRATION_GLOBAL)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // AI Features
            SectionHeader(title = "AI Features")

            FeatureCard(
                title = "AI Assistant",
                description = "Get personalized habit advice from AI",
                icon = Icons.Default.Assistant,
                onClick = {
                    navController.navigate(NavRoutes.AI_ASSISTANT)
                }
            )

            FeatureCard(
                title = "Neural Interface",
                description = "Connect with neural networks",
                icon = Icons.Default.Psychology,
                onClick = {
                    navController.navigate(NavRoutes.neuralInterface("global"))
                }
            )

            FeatureCard(
                title = "Voice Integration",
                description = "Control your habits with voice commands",
                icon = Icons.Default.KeyboardVoice,
                onClick = {
                    navController.navigate(NavRoutes.VOICE_INTEGRATION)
                }
            )

            FeatureCard(
                title = "Quantum Visualization",
                description = "Visualize your habits with quantum-inspired algorithms",
                icon = Icons.Default.Biotech,
                onClick = {
                    navController.navigate(NavRoutes.QUANTUM_VISUALIZATION)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Machine Learning Features
            SectionHeader(title = "Machine Learning Features")

            FeatureCard(
                title = "Predictive ML",
                description = "ML-powered predictions about your habits",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                onClick = {
                    navController.navigate(NavRoutes.PREDICTIVE_ML)
                }
            )

            FeatureCard(
                title = "Multi-Modal Learning",
                description = "Learn from images, text, and sensor data",
                icon = Icons.Default.PhotoCamera,
                onClick = {
                    navController.navigate(NavRoutes.MULTI_MODAL_LEARNING)
                }
            )

            FeatureCard(
                title = "Meta-Learning",
                description = "Learn how to learn better",
                icon = Icons.Default.Psychology,
                onClick = {
                    navController.navigate(NavRoutes.META_LEARNING)
                }
            )

            FeatureCard(
                title = "Neural Network",
                description = "Visualize and train neural networks",
                icon = Icons.Default.Memory,
                onClick = {
                    navController.navigate(NavRoutes.NEURAL_NETWORK)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // AR Features
            SectionHeader(title = "AR Features")

            FeatureCard(
                title = "AR Visualization",
                description = "View your habits in augmented reality",
                icon = Icons.Default.ViewInAr,
                onClick = {
                    navController.navigate(NavRoutes.AR_GLOBAL)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Section header component
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

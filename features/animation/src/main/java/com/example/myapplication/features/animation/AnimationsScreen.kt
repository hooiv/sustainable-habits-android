package com.example.myapplication.features.animation

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.core.ui.animation.*
import com.example.myapplication.core.ui.components.FeatureCard
import kotlinx.coroutines.delay

/**
 * A screen that serves as a hub for all animation features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationsScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    
    // Start animations
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    // Create a gradient background
    val gradientColors = listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surface
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Animations",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(colors = gradientColors)
                )
        ) {
            // Background particle effect
            if (showContent) {
                ParticleSystem(
                    modifier = Modifier.fillMaxSize(),
                    particleCount = 20,
                    particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    maxSpeed = 0.3f
                )
            }
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Preview animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Show a rotating 3D cube
                    ThreeJSScene(
                        modifier = Modifier.fillMaxSize(),
                        rotationEnabled = true,
                        backgroundColor = Color.Transparent
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Animation features section
                SectionTitle(title = "Animation Libraries", visible = showContent)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Anime.js card
                FeatureCard(
                    title = "Anime.js Animations",
                    description = "Interactive animations with Anime.js",
                    icon = Icons.Default.Animation,
                    onClick = {
                        navController.navigate(NavRoutes.ANIMEJS_ANIMATION)
                    },
                    modifier = Modifier.animeEntrance(
                        visible = showContent,
                        index = 0,
                        baseDelay = 100,
                        initialOffsetY = 50
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Three.js card
                FeatureCard(
                    title = "Three.js Visualizations",
                    description = "3D visualizations with Three.js",
                    icon = Icons.Default.ViewInAr,
                    onClick = {
                        navController.navigate(NavRoutes.THREEJS_VISUALIZATION)
                    },
                    modifier = Modifier.animeEntrance(
                        visible = showContent,
                        index = 1,
                        baseDelay = 100,
                        initialOffsetY = 50
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Advanced animations section
                SectionTitle(title = "Advanced Animations", visible = showContent)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Spatial computing card
                FeatureCard(
                    title = "Spatial Computing",
                    description = "Explore habits in 3D space",
                    icon = Icons.Default.Layers,
                    onClick = {
                        navController.navigate(NavRoutes.SPATIAL_COMPUTING)
                    },
                    modifier = Modifier.animeEntrance(
                        visible = showContent,
                        index = 2,
                        baseDelay = 100,
                        initialOffsetY = 50
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // AR visualization card
                FeatureCard(
                    title = "AR Visualization",
                    description = "View your habits in augmented reality",
                    icon = Icons.Default.ViewInAr,
                    onClick = {
                        navController.navigate(NavRoutes.AR_GLOBAL)
                    },
                    modifier = Modifier.animeEntrance(
                        visible = showContent,
                        index = 3,
                        baseDelay = 100,
                        initialOffsetY = 50
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quantum visualization card
                FeatureCard(
                    title = "Quantum Visualization",
                    description = "Visualize habits in quantum space",
                    icon = Icons.Default.Biotech,
                    onClick = {
                        navController.navigate(NavRoutes.QUANTUM_VISUALIZATION_GLOBAL)
                    },
                    modifier = Modifier.animeEntrance(
                        visible = showContent,
                        index = 4,
                        baseDelay = 100,
                        initialOffsetY = 50
                    )
                )
                
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    visible: Boolean
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .animeEntrance(
                visible = visible,
                initialOffsetY = 30,
                initialAlpha = 0f
            )
            .padding(vertical = 8.dp)
    )
}

package com.example.myapplication.features.demo

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ui.animation.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/**
 * A demo screen showcasing various anime.js and three.js inspired animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationDemoScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    
    // Animation states
    var showParticles by remember { mutableStateOf(false) }
    var showThreeJS by remember { mutableStateOf(false) }
    var showTimeline by remember { mutableStateOf(false) }
    var showWave by remember { mutableStateOf(false) }
    
    // Start animations sequentially
    LaunchedEffect(Unit) {
        delay(100)
        showParticles = true
        delay(300)
        showThreeJS = true
        delay(300)
        showTimeline = true
        delay(300)
        showWave = true
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
                        "Animation Demos",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
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
            if (showParticles) {
                ParticleSystem(
                    modifier = Modifier.fillMaxSize(),
                    particleCount = 30,
                    particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    maxSpeed = 0.5f
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
                // Section 1: Three.js Inspired 3D Cube
                SectionTitle(
                    title = "Three.js Inspired 3D",
                    visible = showThreeJS
                )
                
                if (showThreeJS) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ThreeJSScene(
                            modifier = Modifier.fillMaxSize(),
                            rotationEnabled = true
                        ) { sceneModifier ->
                            ThreeJSCube(
                                modifier = sceneModifier.size(150.dp),
                                frontColor = MaterialTheme.colorScheme.primary,
                                backColor = MaterialTheme.colorScheme.secondary,
                                leftColor = MaterialTheme.colorScheme.tertiary,
                                rightColor = MaterialTheme.colorScheme.primaryContainer,
                                topColor = MaterialTheme.colorScheme.secondaryContainer,
                                bottomColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Section 2: Anime.js Inspired Timeline Animation
                SectionTitle(
                    title = "Anime.js Timeline Effects",
                    visible = showTimeline
                )
                
                if (showTimeline) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Create 5 boxes with staggered animations
                        repeat(5) { index ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .animeEntrance(
                                        visible = true,
                                        index = index,
                                        baseDelay = 100,
                                        duration = 800,
                                        initialOffsetY = 100,
                                        easing = AnimeEasing.EaseOutElastic
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Morphing shape animation
                    var morphTarget by remember { mutableStateOf(0f) }
                    
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(2000)
                            morphTarget = if (morphTarget < 0.5f) 1f else 0f
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .animeMorph(
                                targetShape = morphTarget,
                                duration = 1000,
                                easing = AnimeEasing.EaseInOutElastic
                            )
                            .background(
                                color = MaterialTheme.colorScheme.secondary
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Morph",
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Section 3: Particle Effects
                SectionTitle(
                    title = "Particle Effects",
                    visible = showWave
                )
                
                if (showWave) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        ParticleWave(
                            modifier = Modifier.fillMaxSize(),
                            particleColor = MaterialTheme.colorScheme.primary,
                            particleCount = 100,
                            waveHeight = 50f,
                            speed = 0.5f
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        ParticleExplosion(
                            modifier = Modifier.fillMaxSize(),
                            particleColor = MaterialTheme.colorScheme.secondary,
                            particleCount = 100,
                            explosionRadius = 150f,
                            duration = 3000,
                            repeat = true
                        )
                    }
                }
                
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
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .graphicsLayer {
                alpha = if (visible) 1f else 0f
                translationY = if (visible) 0f else -50f
            }
    )
}

package com.example.myapplication.features.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    // Enhanced onboarding with advanced animations
    var page by rememberSaveable { mutableStateOf(0) }
    val pages = listOf(
        "Welcome to MyApp! Track your habits easily.",
        "Stay motivated with reminders and stats.",
        "Visualize your progress with beautiful charts.",
        "Connect with friends and share your journey."
    )

    // Track if content should be visible (for animations)
    var contentVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Start entrance animation
    LaunchedEffect(page) {
        contentVisible = false
        delay(100)
        contentVisible = true
    }

    // Create a gradient background
    val gradientColors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
    ) {
        // Add advanced particle effect in background based on current page
        when (page) {
            0 -> ParticleSystem(
                modifier = Modifier.fillMaxSize(),
                particleCount = 50,
                particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                particleShape = ParticleShape.CIRCLE,
                particleEffect = ParticleEffect.FLOAT,
                maxSpeed = 0.5f
            )
            1 -> ParticleSystem(
                modifier = Modifier.fillMaxSize(),
                particleCount = 50,
                particleColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                particleShape = ParticleShape.SQUARE,
                particleEffect = ParticleEffect.WAVE,
                maxSpeed = 0.3f
            )
            2 -> ParticleSystem(
                modifier = Modifier.fillMaxSize(),
                particleCount = 50,
                particleColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                particleShape = ParticleShape.TRIANGLE,
                particleEffect = ParticleEffect.VORTEX,
                maxSpeed = 0.4f
            )
            3 -> ParticleSystem(
                modifier = Modifier.fillMaxSize(),
                particleCount = 50,
                particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                particleShape = ParticleShape.STAR,
                particleEffect = ParticleEffect.PULSE,
                maxSpeed = 0.3f,
                interactionEnabled = true,
                colorVariation = true
            )
        }

        // Main content with animations
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicator with enhanced animation
            Row(
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .graphicsLayer {
                        alpha = if (contentVisible) 1f else 0f
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { i ->
                    val isSelected = i == page
                    val infiniteTransition = rememberInfiniteTransition(label = "indicator$i")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isSelected) 1.2f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = AnimeEasing.EaseInOutQuad),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale$i"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(if (isSelected) 12.dp else 8.dp)
                            .scale(if (isSelected) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                brush = if (isSelected) {
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            )
                    )
                }
            }

            // Animated text with glowing effect
            GlowingText(
                text = pages[page],
                color = MaterialTheme.colorScheme.onBackground,
                glowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                glowRadius = 5.dp,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                particlesEnabled = false,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = if (contentVisible) 1f else 0f
                        translationY = if (contentVisible) 0f else 50f
                    }
                    .padding(bottom = 32.dp)
            )

            // Enhanced 3D scene with interactive elements
            ThreeJSScene(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(bottom = 32.dp)
                    .graphicsLayer {
                        alpha = if (contentVisible) 1f else 0f
                        translationY = if (contentVisible) 0f else 100f
                    },
                rotationEnabled = true,
                initialRotationY = 10f,
                cameraDistance = 12f,
                enableParallax = true,
                enableShadows = true,
                enableZoom = true,
                enableTapInteraction = true,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                onTap = {
                    coroutineScope.launch {
                        // Show a visual feedback when tapped
                        if (page < pages.lastIndex) {
                            page++
                        } else {
                            onFinish()
                        }
                    }
                }
            ) { sceneModifier ->
                Box(
                    modifier = sceneModifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Different content for each page with enhanced visuals
                    when (page) {
                        0 -> {
                            // Habit tracking visualization
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Track Daily Habits",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Sample habit cards
                                repeat(3) { index ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Motivation and reminders visualization
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Stay Motivated",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Sample achievement badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    repeat(3) { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    brush = Brush.radialGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.primary,
                                                            MaterialTheme.colorScheme.tertiary
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Visualization demo
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Beautiful Visualizations",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Sample chart
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                ) {
                                    // Simple chart visualization
                                    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        val width = size.width
                                        val height = size.height
                                        val barWidth = width / 7

                                        // Draw sample bars
                                        for (i in 0 until 7) {
                                            val barHeight = (0.3f + (i % 3) * 0.2f) * height
                                            drawRect(
                                                color = primaryColor,
                                                topLeft = Offset(i * barWidth + 4, height - barHeight),
                                                size = Size(barWidth - 8, barHeight)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Social features visualization
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Connect with Friends",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Sample user avatars
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    repeat(4) { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    color = when (index) {
                                                        0 -> MaterialTheme.colorScheme.primary
                                                        1 -> MaterialTheme.colorScheme.secondary
                                                        2 -> MaterialTheme.colorScheme.tertiary
                                                        else -> MaterialTheme.colorScheme.error
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "U${index + 1}",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Sample challenge card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "30-Day Challenge",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Navigation buttons with liquid animation
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = if (contentVisible) 1f else 0f
                        translationY = if (contentVisible) 0f else 50f
                    },
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (page > 0) {
                    LiquidButton(
                        text = "Back",
                        onClick = { page-- },
                        color = MaterialTheme.colorScheme.secondary,
                        width = 120.dp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                if (page < pages.lastIndex) {
                    LiquidButton(
                        text = "Next",
                        onClick = { page++ },
                        color = MaterialTheme.colorScheme.primary,
                        width = 120.dp
                    )
                } else {
                    LiquidButton(
                        text = "Get Started",
                        onClick = onFinish,
                        color = MaterialTheme.colorScheme.primary,
                        width = 160.dp
                    )
                }
            }
        }
    }
}

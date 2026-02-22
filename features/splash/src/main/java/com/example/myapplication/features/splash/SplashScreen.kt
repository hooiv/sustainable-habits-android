package com.example.myapplication.features.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.features.gamification.AchievementBadge
import com.example.myapplication.features.gamification.ExperienceBar
import com.example.myapplication.core.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen() {
    // Remember animation states
    var showParticles by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showBadges by remember { mutableStateOf(false) }
    var showExperienceBar by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Start animations after a short delay
    LaunchedEffect(Unit) {
        delay(100)
        showParticles = true
        delay(300)
        showText = true
        delay(500)
        showBadges = true
        delay(300)
        showExperienceBar = true
    }

    // Create a gradient background
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondary
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Add advanced particle effects in the background
        if (showParticles) {
            ParticleSystem(
                modifier = Modifier.fillMaxSize(),
                particleCount = 100,
                particleColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                maxSpeed = 1f,
                fadeDistance = 0.9f,
                particleShape = ParticleShape.STAR,
                particleEffect = ParticleEffect.VORTEX,
                colorVariation = true,
                glowEffect = true
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Add animated text with 3D effect and morphing blob
            Box(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Add morphing blob behind the text
                if (showText) {
                    MorphingBlob(
                        modifier = Modifier
                            .size(280.dp)
                            .alpha(0.7f),
                        color = MaterialTheme.colorScheme.tertiary,
                        pointCount = 12,
                        minRadius = 0.7f,
                        maxRadius = 0.9f
                    )
                }

                // 3D text effect
                ThreeJSScene(
                    modifier = Modifier
                        .size(240.dp)
                        .graphicsLayer {
                            alpha = if (showText) 1f else 0f
                            scaleX = if (showText) 1f else 0.8f
                            scaleY = if (showText) 1f else 0.8f
                        },
                    rotationEnabled = true,
                    initialRotationY = 10f,
                    cameraDistance = 12f,
                    enableParallax = true,
                    enableShadows = true,
                    backgroundColor = Color.Transparent
                ) { sceneModifier ->
                    Box(
                        modifier = sceneModifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Use glowing text for enhanced effect
                        GlowingText(
                            text = "HabitFlow",
                            color = MaterialTheme.colorScheme.onPrimary,
                            glowColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            glowRadius = 15.dp,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            particlesEnabled = true
                        )
                    }
                }
            }

            // Add achievement badges with animation
            AnimatedVisibility(
                visible = showBadges,
                enter = fadeIn() + expandVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AchievementBadge(
                        title = "Starter",
                        description = "Begin your journey",
                        isUnlocked = true,
                        iconVector = Icons.Default.Star,
                        modifier = Modifier.padding(8.dp),
                        onBadgeClick = {}
                    )

                    AchievementBadge(
                        title = "Consistent",
                        description = "7 day streak",
                        isUnlocked = true,
                        iconVector = Icons.Default.Check,
                        modifier = Modifier.padding(8.dp),
                        onBadgeClick = {}
                    )

                    AchievementBadge(
                        title = "Champion",
                        description = "30 day streak",
                        isUnlocked = false,
                        iconVector = Icons.Default.EmojiEvents,
                        modifier = Modifier.padding(8.dp),
                        onBadgeClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Add experience bar with animation
            AnimatedVisibility(
                visible = showExperienceBar,
                enter = fadeIn() + expandVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 32.dp)
                ) {
                    var currentXp by remember { mutableStateOf(0) }
                    var level by remember { mutableStateOf(1) }

                    LaunchedEffect(showExperienceBar) {
                        delay(500)
                        // Animate XP increase directly in the LaunchedEffect scope
                        repeat(5) {
                            currentXp += 20
                            delay(300)
                        }
                    }

                    ExperienceBar(
                        currentXp = currentXp,
                        maxXp = 100,
                        level = level,
                        primaryColor = MaterialTheme.colorScheme.tertiary,
                        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        onLevelUp = {
                            coroutineScope.launch {
                                level++
                                currentXp = 0
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

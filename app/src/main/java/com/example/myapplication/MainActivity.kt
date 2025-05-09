package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.animation.AnimeEasing
import com.example.myapplication.ui.animation.ParticleSystem
import com.example.myapplication.ui.animation.ParticleWave
import com.example.myapplication.ui.animation.ThreeJSScene
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

sealed class StartDestination {
    object Splash : StartDestination()
    object Onboarding : StartDestination()
    object Main : StartDestination()

    companion object {
        val Saver: Saver<StartDestination, String> = Saver(
            save = {
                when (it) {
                    Splash -> "Splash"
                    Onboarding -> "Onboarding"
                    Main -> "Main"
                }
            },
            restore = {
                when (it) {
                    "Splash" -> Splash
                    "Onboarding" -> Onboarding
                    "Main" -> Main
                    else -> throw IllegalArgumentException("Unknown StartDestination: $it")
                }
            }
        )
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Attempt to programmatically set the theme before super.onCreate()
        // This ensures the theme is applied before any window decisions are made.
        setTheme(R.style.Theme_MyApplication) // Using the theme defined in themes.xml

        super.onCreate(savedInstanceState)

        // Ensure no default ActionBar is displayed when using Compose
        WindowCompat.setDecorFitsSystemWindows(window, false) // Optional: for edge-to-edge

        setContent {
            // The ThemePreferenceManager will be observed directly within MyApplicationTheme
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    var startDestination by rememberSaveable(stateSaver = StartDestination.Saver) { mutableStateOf<StartDestination>(StartDestination.Splash) }

                    LaunchedEffect(Unit) {
                        delay(1500) // Splash duration
                        // TODO: Replace with DataStore or SharedPreferences check for onboarding completion
                        val prefs = context.getSharedPreferences("onboarding", MODE_PRIVATE)
                        val completed = prefs.getBoolean("completed", false)
                        startDestination = if (completed) StartDestination.Main else StartDestination.Onboarding
                    }

                    when (startDestination) {
                        StartDestination.Splash -> SplashScreen()
                        StartDestination.Onboarding -> OnboardingScreen(
                            onFinish = {
                                context.getSharedPreferences("onboarding", MODE_PRIVATE)
                                    .edit().putBoolean("completed", true).apply()
                                startDestination = StartDestination.Main
                            }
                        )
                        StartDestination.Main -> AppNavigation()
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    // Remember animation states
    var showParticles by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    // Start animations after a short delay
    LaunchedEffect(Unit) {
        delay(100)
        showParticles = true
        delay(300)
        showText = true
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
        // Add particle effects in the background
        if (showParticles) {
            ParticleSystem(
                modifier = Modifier.fillMaxSize(),
                particleCount = 100,
                particleColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                maxSpeed = 1f,
                fadeDistance = 0.9f
            )
        }

        // Add animated text with 3D effect
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationX = if (showText) 0f else 30f
                    alpha = if (showText) 1f else 0f
                    scaleX = if (showText) 1f else 0.8f
                    scaleY = if (showText) 1f else 0.8f
                    shadowElevation = 20f
                }
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MyApp",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(32.dp)
                    .graphicsLayer {
                        shadowElevation = 12f
                    }
            )
        }

        // Add a pulsing circle behind the text
        if (showText) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = AnimeEasing.EaseInOutQuad),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .alpha(0.2f)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    // Enhanced onboarding with anime.js-like animations
    var page by rememberSaveable { mutableStateOf(0) }
    val pages = listOf(
        "Welcome to MyApp! Track your habits easily.",
        "Stay motivated with reminders and stats.",
        "Visualize your progress with beautiful charts."
    )

    // Track if content should be visible (for animations)
    var contentVisible by remember { mutableStateOf(false) }

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
        // Add subtle particle effect in background
        ParticleWave(
            modifier = Modifier.fillMaxSize(),
            particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            particleCount = 50,
            waveHeight = 50f,
            speed = 0.3f
        )

        // Main content with animations
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicator
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
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(if (isSelected) 12.dp else 8.dp)
                            .background(
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .graphicsLayer {
                                scaleX = if (isSelected) 1f else 0.8f
                                scaleY = if (isSelected) 1f else 0.8f
                            }
                    )
                }
            }

            // Animated text
            Text(
                text = pages[page],
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = if (contentVisible) 1f else 0f
                        translationY = if (contentVisible) 0f else 50f
                    }
                    .padding(bottom = 32.dp)
            )

            // 3D card with content
            ThreeJSScene(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 32.dp)
                    .graphicsLayer {
                        alpha = if (contentVisible) 1f else 0f
                        translationY = if (contentVisible) 0f else 100f
                    },
                rotationEnabled = true,
                initialRotationY = 10f,
                cameraDistance = 12f
            ) { sceneModifier ->
                Box(
                    modifier = sceneModifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Different content for each page
                    when (page) {
                        0 -> Text(
                            "Track Daily Habits",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        1 -> Text(
                            "View Progress Stats",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        2 -> Text(
                            "Beautiful Visualizations",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Navigation buttons with animation
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = if (contentVisible) 1f else 0f
                        translationY = if (contentVisible) 0f else 50f
                    }
            ) {
                if (page > 0) {
                    Button(
                        onClick = { page-- },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("Back")
                    }
                }

                if (page < pages.lastIndex) {
                    Button(
                        onClick = { page++ },
                        modifier = Modifier.padding(start = if (page > 0) 0.dp else 16.dp)
                    ) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.padding(start = if (page > 0) 0.dp else 16.dp)
                    ) {
                        Text("Get Started")
                    }
                }
            }
        }
    }
}
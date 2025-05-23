package com.example.myapplication

import android.os.Bundle
import android.util.Log
import com.google.ar.core.HitResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.myapplication.features.ai.AIAssistantCard
import com.example.myapplication.features.ai.AISuggestion
import com.example.myapplication.features.ai.SuggestionType
import com.example.myapplication.features.analytics.AnalyticsInsight
import com.example.myapplication.features.analytics.AnalyticsInsightsPanel
import com.example.myapplication.features.analytics.InsightType
import com.example.myapplication.features.ar.ARHabitVisualization
import com.example.myapplication.features.ar.ARObject
import com.example.myapplication.features.ar.ARObjectType
import com.example.myapplication.features.gamification.AchievementBadge
import com.example.myapplication.features.gamification.ExperienceBar
import com.example.myapplication.features.gestures.GestureAction
import com.example.myapplication.features.gestures.RadialGestureMenu
import com.example.myapplication.features.gestures.SwipeGestureArea
import com.example.myapplication.features.personalization.AnimationSpeedCustomizer
import com.example.myapplication.features.personalization.AppTheme
import com.example.myapplication.features.personalization.FontSizeCustomizer
import com.example.myapplication.features.personalization.ThemeSelector
import com.example.myapplication.features.UltraAdvancedFeaturesDemo
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.navigation.NavRoutes
import com.example.myapplication.ui.animation.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

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

                    Box(modifier = Modifier.fillMaxSize()) {
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
}

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
                            text = "MyApp",
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
                        // Animate XP increase
                        coroutineScope.launch {
                            repeat(5) {
                                currentXp += 20
                                delay(300)
                            }
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

/**
 * A demo screen showcasing all the advanced features we've added
 */
@Composable
fun AdvancedFeaturesDemo(onClose: () -> Unit) {
    // State for current demo section
    var currentSection by remember { mutableStateOf(0) }
    val sectionTitles = listOf(
        "AI Assistant",
        "Augmented Reality",
        "Advanced Analytics",
        "Gesture Controls",
        "Personalization"
    )

    // Create sample data for demos
    val aiSuggestions = remember {
        listOf(
            AISuggestion(
                title = "New Meditation Habit",
                description = "Based on your sleep patterns, adding a 10-minute meditation before bed could improve your sleep quality.",
                type = SuggestionType.NEW_HABIT,
                confidence = 0.85f
            ),
            AISuggestion(
                title = "Optimize Your Reading Schedule",
                description = "You're more likely to complete your reading habit in the morning based on your completion patterns.",
                type = SuggestionType.SCHEDULE_OPTIMIZATION,
                confidence = 0.92f
            ),
            AISuggestion(
                title = "Streak at Risk",
                description = "Your 'Daily Exercise' habit streak is at risk. Consider completing it in the next 3 hours.",
                type = SuggestionType.STREAK_PROTECTION,
                confidence = 0.78f
            )
        )
    }

    val analyticsInsights = remember {
        listOf(
            AnalyticsInsight(
                title = "Morning Exercise Boosts Productivity",
                description = "We've detected a strong correlation between completing your morning exercise habit and higher productivity in work-related habits later in the day.",
                type = InsightType.CORRELATION,
                score = 0.89f
            ),
            AnalyticsInsight(
                title = "Weekend Habit Completion Declining",
                description = "Your habit completion rate on weekends has decreased by 23% over the past month. Consider adjusting your weekend routine.",
                type = InsightType.TREND_DETECTION,
                score = 0.76f
            ),
            AnalyticsInsight(
                title = "Unusual Sleep Pattern Detected",
                description = "Your sleep tracking shows irregular patterns this week compared to your baseline. This might affect your habit performance.",
                type = InsightType.ANOMALY_DETECTION,
                score = 0.82f
            )
        )
    }

    val gestureActions = remember {
        listOf(
            GestureAction(
                id = "add_habit",
                name = "Add Habit",
                description = "Create a new habit",
                icon = Icons.Default.Add,
                action = {}
            ),
            GestureAction(
                id = "view_stats",
                name = "Statistics",
                description = "View your habit statistics",
                icon = Icons.Default.BarChart,
                action = {}
            ),
            GestureAction(
                id = "settings",
                name = "Settings",
                description = "Open settings menu",
                icon = Icons.Default.Settings,
                action = {}
            ),
            GestureAction(
                id = "profile",
                name = "Profile",
                description = "View your profile",
                icon = Icons.Default.Person,
                action = {}
            ),
            GestureAction(
                id = "social",
                name = "Friends",
                description = "View your friends",
                icon = Icons.Default.People,
                action = {}
            )
        )
    }

    val themes = remember {
        listOf(
            AppTheme(
                name = "Ocean Blue",
                primaryColor = Color(0xFF1976D2),
                secondaryColor = Color(0xFF26A69A),
                tertiaryColor = Color(0xFF7E57C2),
                backgroundColor = Color(0xFFF5F5F5),
                surfaceColor = Color(0xFFFFFFFF),
                isDark = false
            ),
            AppTheme(
                name = "Midnight",
                primaryColor = Color(0xFF6200EA),
                secondaryColor = Color(0xFF03DAC6),
                tertiaryColor = Color(0xFFCF6679),
                backgroundColor = Color(0xFF121212),
                surfaceColor = Color(0xFF1E1E1E),
                isDark = true
            ),
            AppTheme(
                name = "Sunset",
                primaryColor = Color(0xFFFF9800),
                secondaryColor = Color(0xFFE91E63),
                tertiaryColor = Color(0xFF8BC34A),
                backgroundColor = Color(0xFFFFF8E1),
                surfaceColor = Color(0xFFFFFFFF),
                isDark = false
            )
        )
    }

    // Create AR objects
    val arObjects = remember {
        List(5) { index ->
            val angle = index * (2 * Math.PI / 5)
            val radius = 300f
            val x = 500f + (radius * cos(angle)).toFloat()
            val y = 500f + (radius * sin(angle)).toFloat()

            ARObject(
                type = ARObjectType.entries[index % ARObjectType.entries.size],
                position = Offset(x, y),
                label = "AR Object ${index + 1}"
            )
        }
    }

    // Main content with SwipeGestureArea for navigation
    SwipeGestureArea(
        onSwipeLeft = {
            if (currentSection < sectionTitles.size - 1) {
                currentSection++
            }
        },
        onSwipeRight = {
            if (currentSection > 0) {
                currentSection--
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Advanced Features Demo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Section tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sectionTitles.size) { index ->
                    val isSelected = index == currentSection

                    Button(
                        onClick = { currentSection = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text(sectionTitles[index])
                    }
                }
            }

            // Section content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                when (currentSection) {
                    0 -> {
                        // AI Assistant Demo
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AI Assistant",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            AIAssistantCard(
                                suggestions = aiSuggestions,
                                onSuggestionClick = {},
                                onAskQuestion = {}
                            )
                        }
                    }
                    1 -> {
                        // AR Demo
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Augmented Reality Visualization",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Tap to add objects, drag to move them",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            ARHabitVisualization(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.1f)),
                                arObjects = arObjects,
                                onObjectClick = {},
                                onAddARObject = { hitResult, arObjectType ->
                                    Log.d("AR_DEMO", "Add object triggered: Type: $arObjectType, Hit: $hitResult")
                                }
                            )
                        }
                    }
                    2 -> {
                        // Advanced Analytics Demo
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Advanced Analytics",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            AnalyticsInsightsPanel(
                                insights = analyticsInsights,
                                onInsightClick = {}
                            )
                        }
                    }
                    3 -> {
                        // Gesture Controls Demo
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Gesture Controls",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            Text(
                                text = "Long press anywhere to open the radial menu",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Swipe left/right to navigate between sections",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )

                            // Overlay the RadialGestureMenu
                            Box(
                                modifier = Modifier
                                    .size(300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = "Long press here",
                                    modifier = Modifier.align(Alignment.Center)
                                )

                                RadialGestureMenu(
                                    actions = gestureActions,
                                    onActionSelected = {}
                                )
                            }
                        }
                    }
                    4 -> {
                        // Personalization Demo
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Personalization",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Theme selector
                            ThemeSelector(
                                themes = themes,
                                currentThemeId = themes[0].id,
                                onThemeSelected = {},
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Animation speed customizer
                            AnimationSpeedCustomizer(
                                currentSpeed = 1f,
                                onSpeedChanged = {},
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Font size customizer
                            FontSizeCustomizer(
                                currentScale = 1f,
                                onScaleChanged = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

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
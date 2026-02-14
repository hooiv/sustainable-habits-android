package com.example.myapplication.features.stats

import android.graphics.drawable.GradientDrawable
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitFrequency
// import com.example.myapplication.features.habits.HabitViewModel
import com.example.myapplication.core.ui.animation.*
import com.example.myapplication.core.ui.animation.ThreeJSScene
import com.example.myapplication.core.ui.animation.ParticleWave
import com.example.myapplication.core.ui.animation.ParticleExplosion
import com.example.myapplication.core.ui.animation.AnimeEasing
import com.example.myapplication.features.stats.HabitCategoryBarChart
import com.example.myapplication.features.stats.HabitTrendsLineChart
import com.example.myapplication.core.ui.components.*
import com.example.myapplication.core.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.util.FirebaseUtil
import com.google.firebase.Timestamp
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Helper function to parse Firebase data map to a Habit domain object
private fun parseHabitMapToDomain(id: String, data: Map<String, Any>): Habit? {
    return try {
        val name = data["name"] as? String ?: run {
            Log.e("HabitParsing", "Habit name is null or not a String for ID $id. Skipping.")
            return null
        }
        val description = data["description"] as? String
        val frequencyString = data["frequency"] as? String ?: HabitFrequency.DAILY.name
        val frequency = try { HabitFrequency.valueOf(frequencyString) } catch (e: IllegalArgumentException) {
            Log.w("HabitParsing", "Invalid frequency string '$frequencyString' for ID $id. Defaulting to DAILY.")
            HabitFrequency.DAILY
        }

        val goal = (data["goal"] as? Long)?.toInt() ?: 1
        val goalProgress = (data["goalProgress"] as? Long)?.toInt() ?: 0
        val streak = (data["streak"] as? Long)?.toInt() ?: 0

        val createdTimestamp = data["createdDate"] as? Timestamp
        val createdDate = createdTimestamp?.toDate() ?: run {
            Log.w("HabitParsing", "createdDate is null or not a Timestamp for ID $id. Using current date as fallback.")
            Date() // Fallback, ideally this should always come from Firebase and be valid
        }

        val lastUpdatedTimestampFirebase = data["lastUpdatedTimestamp"] as? Timestamp
        val lastUpdatedTimestamp = lastUpdatedTimestampFirebase?.toDate() ?: createdDate


        val lastCompletedTimestamp = data["lastCompletedDate"] as? Timestamp
        val lastCompletedDate = lastCompletedTimestamp?.toDate()

        val completionHistoryFirebase = data["completionHistory"] as? List<*>
        val completionHistory = completionHistoryFirebase?.mapNotNull {
            (it as? Timestamp)?.toDate()
        }?.toMutableList() ?: mutableListOf()

        val isEnabled = data["isEnabled"] as? Boolean ?: true
        val reminderTime = data["reminderTime"] as? String

        val unlockedBadgesFirebase = data["unlockedBadges"] as? List<*>
        val unlockedBadges = unlockedBadgesFirebase?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList()

        val category = data["category"] as? String

        Habit(
            id = id, // Use the ID from the map key
            name = name,
            description = description,
            frequency = frequency,
            goal = goal,
            goalProgress = goalProgress,
            streak = streak,
            createdDate = createdDate,
            lastUpdatedTimestamp = lastUpdatedTimestamp,
            lastCompletedDate = lastCompletedDate,
            completionHistory = completionHistory,
            isEnabled = isEnabled,
            reminderTime = reminderTime,
            unlockedBadges = unlockedBadges,
            category = category
        )
    } catch (e: Exception) {
        Log.e("HabitParsing", "Failed to parse habit with id $id: ${e.message}", e)
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    // TODO: Fix ViewModel dependency issue - need proper multi-module setup
    // val viewModel: HabitViewModel = hiltViewModel()
    // val habits: List<Habit> by viewModel.habits.collectAsState(emptyList())
    val habits: List<Habit> = emptyList() // Temporary placeholder
    val completedCount = habits.count { it.goalProgress >= it.goal }
    val totalCount = habits.size
    val completionRate = if (totalCount > 0) completedCount * 100f / totalCount else 0f

    var selectedTab by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }

    val animatedCompletionRate by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "completionRate"
    )

    LaunchedEffect(true) {
        delay(1000)
        isLoading = false
    }

    val chartScale by animateFloatAsState(
        targetValue = if (isLoading) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "chartScale"
    )

    val chartAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(1000),
        label = "chartAlpha"
    )

    val tabIndicatorOffset by animateFloatAsState(
        targetValue = selectedTab.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tabOffset"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(R.color.brand_gradient_start),
                                colorResource(R.color.brand_gradient_end)
                            )
                        )
                    ),
                title = {
                    Text(
                        text = "Statistics Dashboard",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colorResource(R.color.brand_accent),
                        modifier = Modifier.alpha(chartAlpha)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.scale(if (isLoading) 0.8f else 1f)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(R.color.brand_accent)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            // Add subtle particle wave effect in the background
            if (!isLoading) {
                ParticleWave(
                    modifier = Modifier.fillMaxSize(),
                    particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    particleCount = 30,
                    waveHeight = 30f,
                    waveWidth = 1000f,
                    speed = 0.2f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ThreeDCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(120.dp)
                            .animeEntrance(
                                visible = !isLoading,
                                initialOffsetY = -100,
                                delayMillis = 200
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            colorResource(R.color.brand_gradient_start).copy(alpha = 0.7f),
                                            colorResource(R.color.brand_gradient_end).copy(alpha = 0.9f)
                                        )
                                    )
                                )
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "Total Habits",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$totalCount",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Total Habits",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    ThreeDCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .height(120.dp)
                            .animeEntrance(
                                visible = !isLoading,
                                initialOffsetY = 100,
                                delayMillis = 400
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            colorResource(R.color.brand_gradient_start).copy(alpha = 0.7f),
                                            colorResource(R.color.brand_gradient_end).copy(alpha = 0.9f)
                                        )
                                    )
                                )
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed Habits",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$completedCount",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                AnimatedCircularProgress(
                    currentValue = animatedCompletionRate.roundToInt(),
                    maxValue = 100,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(vertical = 16.dp)
                        .animeEntrance(
                            visible = !isLoading,
                            initialScale = 0.5f,
                            delayMillis = 600
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .animeEntrance(
                            visible = !isLoading,
                            initialOffsetY = 50,
                            delayMillis = 800
                        ),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabs = listOf(
                        "Pie Chart" to Icons.Default.PieChart,
                        "Bar Chart" to Icons.Default.BarChart,
                        "Trends" to Icons.AutoMirrored.Filled.ShowChart
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width((LocalConfiguration.current.screenWidthDp / 3).dp)
                                .offset(x = (tabIndicatorOffset * (LocalConfiguration.current.screenWidthDp / 3)).dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEachIndexed { index, (title, icon) ->
                                val isSelected = selectedTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            selectedTab = index
                                            coroutineScope.launch {
                                                scrollState.animateScrollTo(scrollState.maxValue)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = icon,
                                            contentDescription = title,
                                            tint = if (isSelected)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Enhanced 3D chart display
                ThreeJSScene(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = 16.dp)
                        .alpha(chartAlpha),
                    rotationEnabled = !isLoading,
                    initialRotationY = 5f,
                    cameraDistance = 12f
                ) { sceneModifier ->
                    Box(
                        modifier = sceneModifier
                            .fillMaxSize()
                            .scale(chartScale)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    ) {
                        when (selectedTab) {
                            0 -> CompletionPieChart(
                                completed = completedCount,
                                total = totalCount,
                                modifier = Modifier.fillMaxSize()
                            )
                            1 -> HabitCategoryBarChart(
                                habits = habits,
                                modifier = Modifier.fillMaxSize()
                            )
                            2 -> HabitTrendsLineChart(
                                habits = habits,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Add particle effect when chart changes
                        if (!isLoading) {
                            ParticleExplosion(
                                modifier = Modifier.fillMaxSize(),
                                particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                particleCount = 30,
                                explosionRadius = 150f,
                                duration = 1500,
                                repeat = false
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !isLoading,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "Habit Streak Leaderboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val topStreakHabits = habits
                            .sortedByDescending { it.streak }
                            .take(5)

                        for ((index, habit) in topStreakHabits.withIndex()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (index) {
                                            0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                            1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            2 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (index < 3) {
                                    androidx.compose.material3.Icon(
                                        imageVector = when (index) {
                                            0 -> Icons.Default.EmojiEvents
                                            1 -> Icons.Default.EmojiEvents
                                            else -> Icons.Default.EmojiEvents
                                        },
                                        contentDescription = "Rank ${index + 1}",
                                        tint = when (index) {
                                            0 -> Color(0xFFFFD700)
                                            1 -> Color(0xFFC0C0C0)
                                            else -> Color(0xFFCD7F32)
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(end = 8.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(24.dp)
                                    )
                                }

                                Text(
                                    text = habit.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = when {
                                            habit.streak > 20 -> Color(0xFFFF1744)
                                            habit.streak > 10 -> Color(0xFFFF9100)
                                            else -> Color(0xFFFF9800)
                                        },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .pulseEffect(pulseEnabled = habit.streak > 7)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${habit.streak}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (topStreakHabits.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "No streak data yet. Keep completing your habits!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // Firebase Backup and Restore Data sections removed.
                // These functionalities are available in the SettingsScreen.

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
@Composable
fun AnimatedCircularProgress(
    currentValue: Int,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    val sweepAngleAnimation by animateFloatAsState(
        targetValue = (currentValue.toFloat() / maxValue.toFloat()) * 360f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "sweepAngle"
    )

    val rotationAnimation by rememberInfiniteTransition(label = "rotate").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scheme = MaterialTheme.colorScheme
    val primaryColor = scheme.primary
    val tertiaryColor = scheme.tertiary
    val primaryContainerAlpha03Color = scheme.primaryContainer.copy(alpha = 0.3f)
    val surfaceVariantAlpha01Color = scheme.surfaceVariant.copy(alpha = 0.1f)
    val surfaceVariantAlpha03Color = scheme.surfaceVariant.copy(alpha = 0.3f)
    val onBackgroundColor = scheme.onBackground
    val onBackgroundAlpha07Color = scheme.onBackground.copy(alpha = 0.7f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier
            .matchParentSize()
            .rotate(rotationAnimation)
        ) {
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryContainerAlpha03Color,
                        surfaceVariantAlpha01Color
                    ),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height)
                ),
                radius = size.minDimension / 2.2f
            )
        }

        Canvas(modifier = Modifier.matchParentSize()) {
            drawArc(
                color = surfaceVariantAlpha03Color,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round),
                size = Size(size.width * 0.8f, size.height * 0.8f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.1f)
            )

            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor,
                        tertiaryColor
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                startAngle = -90f,
                sweepAngle = sweepAngleAnimation,
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round),
                size = Size(size.width * 0.8f, size.height * 0.8f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.1f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$currentValue%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = onBackgroundColor
            )
            Text(
                text = "Completion Rate",
                style = MaterialTheme.typography.bodyMedium,
                color = onBackgroundAlpha07Color
            )
        }

        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = size.minDimension / 2f
            val particleCount = 8
            val particleRadius = 4f

            for (i in 0 until particleCount) {
                val angle = (i * (360f / particleCount) + rotationAnimation) % 360f
                val particleProgress = (angle + 90) / 360f

                if (particleProgress * 360f <= sweepAngleAnimation ||
                    (particleProgress - 1f) * 360f <= sweepAngleAnimation) {

                    val x = center.x + cos(angle * PI.toFloat() / 180f) * radius
                    val y = center.y + sin(angle * PI.toFloat() / 180f) * radius

                    drawCircle(
                        color = primaryColor,
                        radius = particleRadius,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

@Composable
fun CompletionPieChart(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val onSurfaceComposeColor = scheme.onSurface
    val primaryComposeColor = scheme.primary
    val surfaceVariantComposeColor = scheme.surfaceVariant
    val transparentComposeColor = Color.Transparent
    val surfaceVariantAlpha02ComposeColor = scheme.surfaceVariant.copy(alpha = 0.2f)
    val onSurfaceVariantAlpha05ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.5f)
    val onSurfaceVariantAlpha07ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.7f)

    val onSurfaceArgb = onSurfaceComposeColor.toArgb()
    val primaryArgb = primaryComposeColor.toArgb()
    val surfaceVariantArgb = surfaceVariantComposeColor.toArgb()
    val transparentArgb = transparentComposeColor.toArgb()

    if (total == 0) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    surfaceVariantAlpha02ComposeColor,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = "No Data",
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(0.5f),
                    tint = onSurfaceVariantAlpha05ComposeColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No habit data to display",
                    textAlign = TextAlign.Center,
                    color = onSurfaceVariantAlpha07ComposeColor
                )
            }
        }
    } else {
        AndroidView(
            factory = { PieChart(context) },
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(surfaceVariantAlpha02ComposeColor)
                .padding(8.dp)
        ) { pieChart ->
            pieChart.apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(transparentArgb)
                holeRadius = 58f
                transparentCircleRadius = 61f
                setDrawCenterText(true)
                centerText = "${(completed * 100f / total).roundToInt()}%\\nCompleted"
                setCenterTextSize(14f)
                setCenterTextColor(onSurfaceArgb)
                legend.isEnabled = true
                legend.textSize = 12f
                legend.textColor = onSurfaceArgb
                setDrawEntryLabels(false)
                animateY(1400, Easing.EaseInOutQuad)
            }

            val entries = listOf(
                PieEntry(completed.toFloat(), "Completed"),
                PieEntry((total - completed).toFloat(), "Remaining")
            )

            val dataSet = PieDataSet(entries, "")
            dataSet.apply {
                colors = listOf(
                    primaryArgb,
                    surfaceVariantArgb
                )
                valueTextSize = 14f
                valueTextColor = onSurfaceArgb
                valueFormatter = PercentFormatter(pieChart)
                setDrawValues(true)
            }

            pieChart.data = PieData(dataSet)
            pieChart.invalidate()
        }
    }
}

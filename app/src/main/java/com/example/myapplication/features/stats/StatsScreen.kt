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
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import com.example.myapplication.features.habits.HabitViewModel
import com.example.myapplication.ui.animation.*
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.util.FirebaseUtil
import com.google.firebase.Timestamp
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
            createdDate = createdDate, // Use the parsed createdDate
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
    val viewModel: HabitViewModel = hiltViewModel()
    val habits by viewModel.habits.collectAsState(emptyList())
    val completedCount = habits.count { it.goalProgress >= it.goal }
    val totalCount = habits.size
    val completionRate = if (totalCount > 0) completedCount * 100f / totalCount else 0f
    
    // Animation states
    var selectedTab by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Loading state for animations
    var isLoading by remember { mutableStateOf(true) }

    // Completion rate animation
    // Completion rate animation
    val animatedCompletionRate by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "completionRate"
    )
    
    // Simulate loading
    LaunchedEffect(true) {
        delay(1000)
        isLoading = false
    }
    
    // Chart appearance animation
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
    
    // Tab selection animation
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
                        Icon(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Summary cards with animation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Card 1: Total Habits
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
                        Icon(
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
                
                // Card 2: Completed Habits
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
                        Icon(
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
            
            // Completion rate circular progress
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
            
            // Tab selection for different chart types
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
                    // Animated selector
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
                    
                    // Tab options
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
                                    Icon(
                                        icon,
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
            
            // Chart content that changes based on tab selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(vertical = 16.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .alpha(chartAlpha)
                    .scale(chartScale)
            ) {
                // Multiple chart types with crossfade animation
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
            }
            
            // Habit streak leaderboard
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
                    
                    // Sort habits by streak and show top 5
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
                                        0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) // Gold
                                        1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) // Silver
                                        2 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f) // Bronze
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Trophy icon for top 3
                            if (index < 3) {
                                Icon(
                                    when (index) {
                                        0 -> Icons.Default.EmojiEvents
                                        1 -> Icons.Default.EmojiEvents
                                        else -> Icons.Default.EmojiEvents
                                    },
                                    contentDescription = "Rank ${index + 1}",
                                    tint = when (index) {
                                        0 -> Color(0xFFFFD700) // Gold
                                        1 -> Color(0xFFC0C0C0) // Silver
                                        else -> Color(0xFFCD7F32) // Bronze
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
                            
                            // Streak count with fire icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
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
                    
                    // If there are no habits with streaks
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
            
            // Firebase Backup and Restore Section
            Text(
                "Cloud Backup & Restore",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JupiterGradientButton(
                    text = "Backup Data",
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            FirebaseUtil.backupHabitData(userId, habits,
                                onSuccess = {
                                    coroutineScope.launch {
                                        Toast.makeText(context, "Data backed up successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onFailure = { exception: Exception ->
                                     coroutineScope.launch {
                                        Toast.makeText(context, "Backup failed: ${exception.message}", Toast.LENGTH_LONG).show()
                                        Log.e("StatsScreenBackup", "Backup failed", exception)
                                     }
                                }
                            )
                        } else {
                            coroutineScope.launch {
                                Toast.makeText(context, "User not logged in for backup.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                JupiterGradientButton(
                    text = "Restore Data",
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            FirebaseUtil.fetchHabitData(userId,
                                onSuccess = { habitDataMap ->
                                    coroutineScope.launch {
                                        if (habitDataMap.isEmpty()) {
                                            Toast.makeText(context, "No data found in backup to restore.", Toast.LENGTH_LONG).show()
                                            Log.i("StatsScreenRestore", "No data found in backup for user $userId")
                                            return@launch
                                        }

                                        val restoredHabits = mutableListOf<Habit>()
                                        Log.d("StatsScreenRestore", "Fetched ${habitDataMap.size} items from Firebase for user $userId.")
                                        for ((habitId, habitDetails) in habitDataMap.entries) {
                                            if (habitDetails is Map<*, *>) {
                                                @Suppress("UNCHECKED_CAST")
                                                val detailsMap = habitDetails as? Map<String, Any>
                                                if (detailsMap != null) {
                                                    Log.d("StatsScreenRestore", "Parsing habit with ID: $habitId")
                                                    parseHabitMapToDomain(habitId, detailsMap)?.let { habit ->
                                                        restoredHabits.add(habit)
                                                    }
                                                } else {
                                                    Log.w("StatsScreenRestore", "Skipping habit ID $habitId: details not a Map<String, Any>: $habitDetails")
                                                }
                                            } else {
                                                Log.w("StatsScreenRestore", "Skipping habit ID $habitId: details not a Map: $habitDetails")
                                            }
                                        }

                                        if (restoredHabits.isNotEmpty()) {
                                            Log.d("StatsScreenRestore", "Attempting to restore ${restoredHabits.size} habits.")
                                            // Ensure HabitViewModel has a method like restoreHabits(List<Habit>)
                                            // which uses a DAO with OnConflictStrategy.REPLACE
                                            viewModel.restoreHabits(restoredHabits)
                                            Toast.makeText(context, "Data restored: ${restoredHabits.size} habits processed.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "No valid habit data parsed from backup.", Toast.LENGTH_LONG).show()
                                            Log.i("StatsScreenRestore", "No valid habits parsed from fetched data for user $userId.")
                                        }
                                    }
                                },
                                onFailure = { exception ->
                                    coroutineScope.launch {
                                        Log.e("StatsScreenRestore", "Restore failed for user $userId", exception)
                                        Toast.makeText(context, "Restore failed: ${exception.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        } else {
                             coroutineScope.launch {
                                Toast.makeText(context, "User not logged in. Please log in to restore data.", Toast.LENGTH_LONG).show()
                             }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Extra padding at bottom for scrolling
            Spacer(modifier = Modifier.height(40.dp))
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
    
    // Hoist color definitions
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
        // Background gradient circle
        Canvas(modifier = Modifier
            .matchParentSize()
            .rotate(rotationAnimation)
        ) {
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryContainerAlpha03Color, // Use hoisted color
                        surfaceVariantAlpha01Color    // Use hoisted color
                    ),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height)
                ),
                radius = size.minDimension / 2.2f
            )
        }
        
        // Progress arc
        Canvas(modifier = Modifier.matchParentSize()) {
            // Draw empty background circle
            drawArc(
                color = surfaceVariantAlpha03Color, // Use hoisted color
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round),
                size = Size(size.width * 0.8f, size.height * 0.8f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.1f)
            )
            
            // Draw progress arc
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor,  // Use hoisted color
                        tertiaryColor  // Use hoisted color
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
        
        // Text in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$currentValue%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = onBackgroundColor // Use hoisted color
            )
            Text(
                text = "Completion Rate",
                style = MaterialTheme.typography.bodyMedium,
                color = onBackgroundAlpha07Color // Use hoisted color
            )
        }
        
        // Add particles around the circle for completed progress
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = size.minDimension / 2f
            val particleCount = 8
            val particleRadius = 4f
            
            for (i in 0 until particleCount) {
                val angle = (i * (360f / particleCount) + rotationAnimation) % 360f
                val particleProgress = (angle + 90) / 360f
                
                // Only show particles for completed progress
                if (particleProgress * 360f <= sweepAngleAnimation || 
                    (particleProgress - 1f) * 360f <= sweepAngleAnimation) {
                    
                    val x = center.x + cos(angle * PI.toFloat() / 180f) * radius
                    val y = center.y + sin(angle * PI.toFloat() / 180f) * radius
                    
                    drawCircle(
                        color = primaryColor, // Use hoisted color
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
    // Explicitly resolve Compose Colors first
    val scheme = MaterialTheme.colorScheme
    val onSurfaceComposeColor = scheme.onSurface
    val primaryComposeColor = scheme.primary
    val surfaceVariantComposeColor = scheme.surfaceVariant
    val transparentComposeColor = Color.Transparent // Constant
    val surfaceVariantAlpha02ComposeColor = scheme.surfaceVariant.copy(alpha = 0.2f)
    val onSurfaceVariantAlpha05ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.5f)
    val onSurfaceVariantAlpha07ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.7f)

    // Convert to ARGB Ints for MPAndroidChart
    val onSurfaceArgb = onSurfaceComposeColor.toArgb()
    val primaryArgb = primaryComposeColor.toArgb()
    val surfaceVariantArgb = surfaceVariantComposeColor.toArgb()
    val transparentArgb = transparentComposeColor.toArgb()

    if (total == 0) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    surfaceVariantAlpha02ComposeColor, // Use Compose Color for Compose Modifier
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.PieChart,
                    contentDescription = "No Data",
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(0.5f),
                    tint = onSurfaceVariantAlpha05ComposeColor // Use Compose Color
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No habit data to display",
                    textAlign = TextAlign.Center,
                    color = onSurfaceVariantAlpha07ComposeColor // Use Compose Color
                )
            }
        }
    } else {
        AndroidView(
            factory = { PieChart(context) },
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(surfaceVariantAlpha02ComposeColor) // Use Compose Color for Compose Modifier
                .padding(8.dp)
        ) { pieChart ->
            pieChart.apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(transparentArgb) // Use ARGB Int
                holeRadius = 58f
                transparentCircleRadius = 61f
                setDrawCenterText(true)
                centerText = "${(completed * 100f / total).roundToInt()}%\nCompleted"
                setCenterTextSize(14f)
                setCenterTextColor(onSurfaceArgb) // Use ARGB Int
                legend.isEnabled = true
                legend.textSize = 12f
                legend.textColor = onSurfaceArgb // Use ARGB Int
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
                    primaryArgb,         // Use ARGB Int
                    surfaceVariantArgb   // Use ARGB Int
                )
                valueTextSize = 14f
                valueTextColor = onSurfaceArgb // Use ARGB Int
                valueFormatter = com.github.mikephil.charting.formatter.PercentFormatter(pieChart)
                setDrawValues(true)
            }
            
            pieChart.data = PieData(dataSet)
            pieChart.invalidate()
        }
    }
}

@Composable
fun HabitCategoryBarChart(
    habits: List<Habit>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Explicitly resolve Compose Colors first
    val scheme = MaterialTheme.colorScheme
    val onSurfaceComposeColor = scheme.onSurface
    val primaryComposeColor = scheme.primary
    val surfaceVariantComposeColor = scheme.surfaceVariant
    val surfaceVariantAlpha02ComposeColor = scheme.surfaceVariant.copy(alpha = 0.2f)
    val onSurfaceVariantAlpha05ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.5f)
    val onSurfaceVariantAlpha07ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    val gridLinesComposeColor = scheme.onSurface.copy(alpha = 0.1f)

    // Convert to ARGB Ints for MPAndroidChart
    val onSurfaceArgb = onSurfaceComposeColor.toArgb()
    val primaryArgb = primaryComposeColor.toArgb()
    val surfaceVariantArgb = surfaceVariantComposeColor.toArgb()
    val gridLinesArgb = gridLinesComposeColor.toArgb()

    val categoryCounts = habits
        .filter { !it.category.isNullOrEmpty() }
        .groupBy { it.category ?: "Uncategorized" }
        .mapValues { (_, habitList) ->
            habitList.count { it.isEnabled } to habitList.count { !it.isEnabled }
        }
    
    if (categoryCounts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    surfaceVariantAlpha02ComposeColor, // Use Compose Color
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "No Data",
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(0.5f),
                    tint = onSurfaceVariantAlpha05ComposeColor // Use Compose Color
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No category data available",
                    textAlign = TextAlign.Center,
                    color = onSurfaceVariantAlpha07ComposeColor // Use Compose Color
                )
            }
        }
    } else {
        AndroidView(
            factory = { BarChart(context) },
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(surfaceVariantAlpha02ComposeColor) // Use Compose Color
                .padding(8.dp)
        ) { barChart ->
            barChart.apply {
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    textColor = onSurfaceArgb // Use ARGB Int
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                }
                
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    textColor = onSurfaceArgb // Use ARGB Int
                    textSize = 10f
                    valueFormatter = IndexAxisValueFormatter(categoryCounts.keys.toList())
                    setDrawGridLines(false)
                }
                
                axisLeft.apply {
                    textColor = onSurfaceArgb // Use ARGB Int
                    textSize = 10f
                    axisMinimum = 0f
                    granularity = 1f
                    setDrawGridLines(true)
                    gridColor = gridLinesArgb // Use ARGB Int
                }
                axisRight.isEnabled = false
                setScaleEnabled(true)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled = true
            }
            
            val enabledEntries = mutableListOf<BarEntry>()
            val disabledEntries = mutableListOf<BarEntry>()
            
            categoryCounts.values.forEachIndexed { index, (enabled, disabled) ->
                enabledEntries.add(BarEntry(index.toFloat(), enabled.toFloat()))
                disabledEntries.add(BarEntry(index.toFloat(), disabled.toFloat()))
            }
            
            val enabledDataSet = BarDataSet(enabledEntries, "Active").apply {
                color = primaryArgb // Use ARGB Int
                valueTextColor = onSurfaceArgb // Use ARGB Int
                valueTextSize = 10f
                setDrawValues(true)
            }
            
            val disabledDataSet = BarDataSet(disabledEntries, "Paused").apply {
                color = surfaceVariantArgb // Use ARGB Int
                valueTextColor = onSurfaceArgb // Use ARGB Int
                valueTextSize = 10f
                setDrawValues(true)
            }
            
            val groupSpace = 0.3f
            val barSpace = 0.05f
            val barWidth = 0.3f
            val start = 0f
            
            val barData = BarData(enabledDataSet, disabledDataSet)
            barData.barWidth = barWidth
            
            barChart.data = barData
            barChart.groupBars(start, groupSpace, barSpace)
            barChart.setVisibleXRangeMaximum(5f)
            barChart.setFitBars(true)
            
            barChart.animateY(1400)
            barChart.invalidate()
        }
    }
}

@Composable
fun HabitTrendsLineChart(
    habits: List<Habit>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Explicitly resolve Compose Colors first
    val scheme = MaterialTheme.colorScheme
    val onSurfaceComposeColor = scheme.onSurface
    val primaryComposeColor = scheme.primary
    val tertiaryComposeColor = scheme.tertiary
    val surfaceVariantAlpha02ComposeColor = scheme.surfaceVariant.copy(alpha = 0.2f)
    val onSurfaceVariantAlpha05ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.5f)
    val onSurfaceVariantAlpha07ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    val gridLinesComposeColor = scheme.onSurface.copy(alpha = 0.1f)
    val primaryAlpha05ComposeColor = scheme.primary.copy(alpha = 0.5f)
    val primaryAlpha01ComposeColor = scheme.primary.copy(alpha = 0.1f)
    val tertiaryAlpha05ComposeColor = scheme.tertiary.copy(alpha = 0.5f)
    val tertiaryAlpha01ComposeColor = scheme.tertiary.copy(alpha = 0.1f)

    // Convert to ARGB Ints for MPAndroidChart
    val onSurfaceArgb = onSurfaceComposeColor.toArgb()
    val primaryArgb = primaryComposeColor.toArgb()
    val tertiaryArgb = tertiaryComposeColor.toArgb()
    val gridLinesArgb = gridLinesComposeColor.toArgb()
    val primaryAlpha05Argb = primaryAlpha05ComposeColor.toArgb()
    val primaryAlpha01Argb = primaryAlpha01ComposeColor.toArgb()
    val tertiaryAlpha05Argb = tertiaryAlpha05ComposeColor.toArgb()
    val tertiaryAlpha01Argb = tertiaryAlpha01ComposeColor.toArgb()

    if (habits.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    surfaceVariantAlpha02ComposeColor, // Use Compose Color
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = "No Data",
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(0.5f),
                    tint = onSurfaceVariantAlpha05ComposeColor // Use Compose Color
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Not enough data to show trends",
                    textAlign = TextAlign.Center,
                    color = onSurfaceVariantAlpha07ComposeColor // Use Compose Color
                )
            }
        }
    } else {
        AndroidView(
            factory = { LineChart(context) },
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(surfaceVariantAlpha02ComposeColor) // Use Compose Color
                .padding(8.dp)
        ) { lineChart ->
            lineChart.apply {
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    textColor = onSurfaceArgb // Use ARGB Int
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                }
                
                setDrawGridBackground(false)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = onSurfaceArgb // Use ARGB Int
                    textSize = 10f
                    valueFormatter = IndexAxisValueFormatter(
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    )
                    setDrawGridLines(false)
                }
                
                axisLeft.apply {
                    textColor = onSurfaceArgb // Use ARGB Int
                    textSize = 10f
                    axisMinimum = 0f
                    granularity = 1f
                    setDrawGridLines(true)
                    gridColor = gridLinesArgb // Use ARGB Int
                }
                axisRight.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
            }
            
            val completionEntries = (0..6).map { day ->
                val value = (habits.size * (0.3f + (day % 3) * 0.2f)).toFloat()
                Entry(day.toFloat(), value)
            }
            
            val streakEntries = (0..6).map { day ->
                val value = habits.sumOf { it.streak }.toFloat() / habits.size.coerceAtLeast(1)
                Entry(day.toFloat(), value * (1 + day % 2) * 0.2f)
            }
            
            val completionDataSet = LineDataSet(completionEntries, "Daily Completions").apply {
                color = primaryArgb // Use ARGB Int
                lineWidth = 2f
                setDrawValues(false)
                setDrawCircles(true)
                setCircleColor(primaryArgb) // Use ARGB Int
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleRadius = 2f
                setDrawFilled(true)
                fillDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        primaryAlpha05Argb, // Use ARGB Int
                        primaryAlpha01Argb  // Use ARGB Int
                    )
                )
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            val streakDataSet = LineDataSet(streakEntries, "Avg. Streak Length").apply {
                color = tertiaryArgb // Use ARGB Int
                lineWidth = 2f
                setDrawValues(false)
                setDrawCircles(true)
                setCircleColor(tertiaryArgb) // Use ARGB Int
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleRadius = 2f
                setDrawFilled(true)
                fillDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        tertiaryAlpha05Argb, // Use ARGB Int
                        tertiaryAlpha01Argb  // Use ARGB Int
                    )
                )
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            lineChart.data = LineData(completionDataSet, streakDataSet)
            lineChart.animateX(1500)
            lineChart.invalidate()
        }
    }
}
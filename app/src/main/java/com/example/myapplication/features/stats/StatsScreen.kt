package com.example.myapplication.features.stats

import android.graphics.drawable.GradientDrawable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.Habit
import com.example.myapplication.features.habits.HabitViewModel
import com.example.myapplication.ui.animation.*
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
    var isLoading by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
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
            TopAppBar(
                title = { 
                    Text(
                        "Statistics Dashboard",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(chartAlpha)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.scale(if (isLoading) 0.8f else 1f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                            initialOffsetX = -100,
                            delayMillis = 200
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
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
                            initialOffsetX = 100,
                            delayMillis = 400
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
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
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
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
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Completion Rate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
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
                        color = MaterialTheme.colorScheme.primary,
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
    
    if (total == 0) {
        // No data to display
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No habit data to display",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        AndroidView(
            factory = { PieChart(context) },
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .padding(8.dp)
        ) { pieChart ->
            // Configure chart appearance
            pieChart.apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(Color.Transparent.toArgb())
                holeRadius = 58f
                transparentCircleRadius = 61f
                setDrawCenterText(true)
                centerText = "${(completed * 100f / total).roundToInt()}%\nCompleted"
                setCenterTextSize(14f)
                setCenterTextColor(MaterialTheme.colorScheme.onSurface.toArgb())
                legend.isEnabled = true
                legend.textSize = 12f
                legend.textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                setDrawEntryLabels(false)
                animateY(1400, Easing.EaseInOutQuad)
            }
            
            // Create data entries
            val entries = listOf(
                PieEntry(completed.toFloat(), "Completed"),
                PieEntry((total - completed).toFloat(), "Remaining")
            )
            
            // Create dataset and style it
            val dataSet = PieDataSet(entries, "")
            dataSet.apply {
                colors = listOf(
                    MaterialTheme.colorScheme.primary.toArgb(),
                    MaterialTheme.colorScheme.surfaceVariant.toArgb()
                )
                valueTextSize = 14f
                valueTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
                valueFormatter = com.github.mikephil.charting.formatter.PercentFormatter(pieChart)
                setDrawValues(true)
            }
            
            // Apply data to chart
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
    
    // Group habits by category
    val categoryCounts = habits
        .filter { !it.category.isNullOrEmpty() }
        .groupBy { it.category ?: "Uncategorized" }
        .mapValues { (_, habitList) ->
            habitList.count { it.isEnabled } to habitList.count { !it.isEnabled }
        }
    
    // Check if there's data to display
    if (categoryCounts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No category data available",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        AndroidView(
            factory = { BarChart(context) },
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .padding(8.dp)
        ) { barChart ->
            // Setup chart appearance
            barChart.apply {
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                }
                
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                
                // X-axis styling
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                    textSize = 10f
                    valueFormatter = IndexAxisValueFormatter(categoryCounts.keys.toList())
                    setDrawGridLines(false)
                }
                
                // Left Y-axis styling
                axisLeft.apply {
                    textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                    textSize = 10f
                    axisMinimum = 0f
                    granularity = 1f
                    setDrawGridLines(true)
                    gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f).toArgb()
                }
                
                // Right Y-axis styling (disabled)
                axisRight.isEnabled = false
                
                // Enable zooming and scrolling
                setScaleEnabled(true)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled = true
            }
            
            // Group data by enabled/disabled
            val enabledEntries = mutableListOf<BarEntry>()
            val disabledEntries = mutableListOf<BarEntry>()
            
            categoryCounts.values.forEachIndexed { index, (enabled, disabled) ->
                enabledEntries.add(BarEntry(index.toFloat(), enabled.toFloat()))
                disabledEntries.add(BarEntry(index.toFloat(), disabled.toFloat()))
            }
            
            // Create datasets
            val enabledDataSet = BarDataSet(enabledEntries, "Active").apply {
                color = MaterialTheme.colorScheme.primary.toArgb()
                valueTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
                valueTextSize = 10f
                setDrawValues(true)
            }
            
            val disabledDataSet = BarDataSet(disabledEntries, "Paused").apply {
                color = MaterialTheme.colorScheme.surfaceVariant.toArgb()
                valueTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
                valueTextSize = 10f
                setDrawValues(true)
            }
            
            // Group the data
            val groupSpace = 0.3f
            val barSpace = 0.05f
            val barWidth = 0.3f
            
            // Create and apply the data
            val start = 0f
            
            val barData = BarData(enabledDataSet, disabledDataSet)
            barData.barWidth = barWidth
            
            barChart.data = barData
            barChart.groupBars(start, groupSpace, barSpace)
            barChart.setVisibleXRangeMaximum(5f) // Show max 5 groups at a time
            barChart.setFitBars(true)
            
            // Animate the chart
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
    
    // Check if there's enough data to display
    if (habits.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Not enough data to show trends",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        AndroidView(
            factory = { LineChart(context) },
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .padding(8.dp)
        ) { lineChart ->
            // Setup chart appearance
            lineChart.apply {
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                }
                
                setDrawGridBackground(false)
                
                // X-axis styling
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                    textSize = 10f
                    valueFormatter = IndexAxisValueFormatter(
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    )
                    setDrawGridLines(false)
                }
                
                // Left Y-axis styling
                axisLeft.apply {
                    textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                    textSize = 10f
                    axisMinimum = 0f
                    granularity = 1f
                    setDrawGridLines(true)
                    gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f).toArgb()
                }
                
                // Right Y-axis styling (disabled)
                axisRight.isEnabled = false
                
                // Enable zooming and scrolling
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
            }
            
            // Generate some example trend data (in a real app this would come from actual user data)
            val completionEntries = (0..6).map { day ->
                // Generate random completion data for demo purposes
                // In a real app, this would come from actual habit completion data
                val value = (habits.size * (0.3f + (day % 3) * 0.2f)).toFloat()
                Entry(day.toFloat(), value)
            }
            
            val streakEntries = (0..6).map { day ->
                // Generate random streak data for demo
                val value = habits.sumOf { it.streak }.toFloat() / habits.size.coerceAtLeast(1)
                Entry(day.toFloat(), value * (1 + day % 2) * 0.2f)
            }
            
            // Create datasets
            val completionDataSet = LineDataSet(completionEntries, "Daily Completions").apply {
                color = MaterialTheme.colorScheme.primary.toArgb()
                lineWidth = 2f
                setDrawValues(false)
                setDrawCircles(true)
                setCircleColor(MaterialTheme.colorScheme.primary.toArgb())
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleRadius = 2f
                setDrawFilled(true)
                fillDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f).toArgb(),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f).toArgb()
                    )
                )
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            val streakDataSet = LineDataSet(streakEntries, "Avg. Streak Length").apply {
                color = MaterialTheme.colorScheme.tertiary.toArgb()
                lineWidth = 2f
                setDrawValues(false)
                setDrawCircles(true)
                setCircleColor(MaterialTheme.colorScheme.tertiary.toArgb())
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleRadius = 2f
                setDrawFilled(true)
                fillDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f).toArgb(),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f).toArgb()
                    )
                )
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            // Apply data
            lineChart.data = LineData(completionDataSet, streakDataSet)
            
            // Animate the chart
            lineChart.animateX(1500)
            lineChart.invalidate()
        }
    }
}
package com.example.myapplication.features.analytics.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import com.example.myapplication.features.analytics.AdvancedAnalyticsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Screen for advanced analytics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedAnalyticsScreen(
    navController: NavController,
    viewModel: AdvancedAnalyticsViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    // State
    val habits by viewModel.habits.collectAsState()
    val completions by viewModel.completions.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    // Sample correlation data
    val correlationData = remember(habits) {
        if (habits.size < 2) {
            emptyMap()
        } else {
            val result = mutableMapOf<Pair<String, String>, Float>()
            for (i in habits.indices) {
                for (j in i + 1 until habits.size) {
                    // In a real app, this would be calculated based on actual data
                    result[Pair(habits[i].id, habits[j].id)] = Random.nextDouble(-0.8, 0.8).toFloat()
                }
            }
            result
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Analytics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "Insights") },
                    label = { Text("Insights") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = { Icon(Icons.Default.Timeline, contentDescription = "Trends") },
                    label = { Text("Trends") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = { Icon(Icons.Default.BubbleChart, contentDescription = "Correlations") },
                    label = { Text("Correlations") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Patterns") },
                    label = { Text("Patterns") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Content based on selected tab
                when (selectedTab) {
                    0 -> InsightsTab(insights, viewModel)
                    1 -> TrendsTab(habits, completions)
                    2 -> CorrelationsTab(habits, correlationData)
                    3 -> PatternsTab(habits, completions)
                }
            }
        }
    }
}

/**
 * Tab for displaying AI-generated insights
 */
@Composable
fun InsightsTab(
    insights: List<AnalyticsInsight>,
    viewModel: AdvancedAnalyticsViewModel
) {
    if (insights.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No insights available yet",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Complete more habits to generate insights",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { viewModel.generateInsights() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Generate Insights")
                }
            }
        }
    } else {
        AnalyticsInsightsPanel(
            insights = insights,
            onInsightClick = { insight ->
                // Handle insight click
                viewModel.showToast("Insight: ${insight.title}")
            }
        )
    }
}

/**
 * Tab for displaying trend analysis
 */
@Composable
fun TrendsTab(
    habits: List<Habit>,
    completions: List<HabitCompletion>
) {
    if (habits.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Timeline,
            title = "No habits to analyze",
            message = "Add habits to see trend analysis"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Habit Trends",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Analyze how your habits have evolved over time",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            items(habits) { habit ->
                TrendCard(habit, completions.filter { it.habitId == habit.id })
            }
        }
    }
}

/**
 * Card displaying trend analysis for a habit
 */
@Composable
fun TrendCard(
    habit: Habit,
    completions: List<HabitCompletion>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Show less" else "Show more"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Trend visualization
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (completions.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Not enough data to show trends",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Trend visualization would go here
                            // For now, we'll just show a placeholder
                            TrendVisualization(completions)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Trend analysis
                    Text(
                        text = "Analysis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (completions.isEmpty()) {
                            "Complete this habit more to see trend analysis"
                        } else {
                            "Your consistency with this habit has ${
                                listOf("improved", "remained stable", "declined").random()
                            } over the past ${(2..8).random()} weeks."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Visualization of habit completion trends
 */
@Composable
fun TrendVisualization(completions: List<HabitCompletion>) {
    // This would contain actual trend visualization
    // For now, we'll just show a placeholder
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Drawing code would go here
    }
}

/**
 * Tab for displaying habit correlations
 */
@Composable
fun CorrelationsTab(
    habits: List<Habit>,
    correlationData: Map<Pair<String, String>, Float>
) {
    if (habits.size < 2) {
        EmptyStateMessage(
            icon = Icons.Default.BubbleChart,
            title = "Not enough habits",
            message = "Add at least 2 habits to see correlations"
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Habit Correlations",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Discover how your habits influence each other",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Correlation matrix
            HabitCorrelationMatrix(
                habits = habits,
                correlationData = correlationData,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onCellClick = { habit1, habit2, correlation ->
                    // Handle cell click
                }
            )
        }
    }
}

/**
 * Tab for displaying habit patterns
 */
@Composable
fun PatternsTab(
    habits: List<Habit>,
    completions: List<HabitCompletion>
) {
    if (habits.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.BarChart,
            title = "No habits to analyze",
            message = "Add habits to see pattern analysis"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Habit Patterns",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Discover patterns in your habit completion",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            items(habits) { habit ->
                PatternCard(habit, completions.filter { it.habitId == habit.id })
            }
        }
    }
}

/**
 * Card displaying pattern analysis for a habit
 */
@Composable
fun PatternCard(
    habit: Habit,
    completions: List<HabitCompletion>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (completions.isEmpty()) {
                Text(
                    text = "Not enough data to analyze patterns",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                // Pattern visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Pattern visualization would go here
                    // For now, we'll just show a placeholder
                    PatternVisualization(completions)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pattern insights
                Text(
                    text = "You tend to complete this habit most often on ${
                        listOf("Mondays", "Tuesdays", "Wednesdays", "Thursdays", "Fridays", "weekends").random()
                    } and during the ${
                        listOf("morning", "afternoon", "evening").random()
                    }.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Visualization of habit completion patterns
 */
@Composable
fun PatternVisualization(completions: List<HabitCompletion>) {
    // This would contain actual pattern visualization
    // For now, we'll just show a placeholder
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Drawing code would go here
    }
}

/**
 * Empty state message
 */
@Composable
fun EmptyStateMessage(
    icon: ImageVector,
    title: String,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

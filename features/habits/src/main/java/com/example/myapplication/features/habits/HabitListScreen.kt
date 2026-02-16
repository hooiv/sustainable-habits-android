package com.example.myapplication.features.habits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import com.example.myapplication.features.habits.HabitViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.features.habits.ui.HabitItem
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.ui.R
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.core.ui.animation.AnimeEasing
import com.example.myapplication.core.ui.animation.ParticleWave
import com.example.myapplication.core.ui.animation.animeEntrance
import com.example.myapplication.core.ui.components.JupiterGradientButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    navController: NavController,
    viewModel: HabitViewModel = hiltViewModel(),
    onNavigateToDetails: (String) -> Unit = {}
) {
    val habits by viewModel.habits.collectAsState(initial = emptyList())

    // List state for scroll animations
    val listState = rememberLazyListState()
    // Animation states
    var isLoading by remember { mutableStateOf(true) }

    // Simulate loading state briefly
    LaunchedEffect(true) {
        delay(1500)
        isLoading = false
    }

    // Category filtering
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All") + habits.mapNotNull { it.category }.distinct().sorted()

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
                        "My Habits",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colorResource(R.color.brand_accent)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            JupiterGradientButton(
                text = "Add Habit",
                onClick = { navController.navigate(NavRoutes.ADD_HABIT) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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

            Column(modifier = Modifier.fillMaxSize()) {
                // Category filter card with animation
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .graphicsLayer {
                            // Add subtle floating animation
                            translationY = if (!isLoading) 0f else -50f
                            alpha = if (!isLoading) 1f else 0f
                        }
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Filter by Category",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontal category chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEachIndexed { index, category ->
                                val isSelected = category == selectedCategory

                                // Add staggered entrance animation for each chip
                                Box(
                                    modifier = Modifier.animeEntrance(
                                        visible = !isLoading,
                                        index = index,
                                        baseDelay = 50,
                                        duration = 600,
                                        initialOffsetY = 20,
                                        easing = AnimeEasing.EaseOutBack
                                    )
                                ) {
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategory = category },
                                        label = { Text(category) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.scale(if (isSelected) 1.05f else 1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter habits
                val filteredHabits = if (selectedCategory == "All")
                    habits
                else
                    habits.filter { habit -> habit.category == selectedCategory }

                if (isLoading) {
                    // Loading state with pulsing animation
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Create pulsing effect for the loader
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = AnimeEasing.EaseInOutQuad),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseScale"
                        )

                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scale)
                                .alpha(0.1f)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )

                        CircularProgressIndicator()
                    }
                } else if (filteredHabits.isEmpty()) {
                    // Empty state with animations
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Add floating particles in the background
                        ParticleWave(
                            modifier = Modifier.fillMaxSize(),
                            particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            particleCount = 50,
                            waveHeight = 80f,
                            waveWidth = 1000f,
                            speed = 0.3f
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            // Animated icon with bounce effect
                            val infiniteTransition = rememberInfiniteTransition(label = "iconBounce")
                            val iconScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = AnimeEasing.EaseInOutBack),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "iconScale"
                            )

                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add habit",
                                modifier = Modifier
                                    .size(120.dp)
                                    .scale(iconScale)
                                    .padding(bottom = 16.dp)
                                    .alpha(0.6f),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                "No habits yet",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .graphicsLayer {
                                        alpha = iconScale * 0.8f
                                    }
                            )

                            Text(
                                "Tap the + button to start tracking your first habit",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .graphicsLayer {
                                        alpha = iconScale * 0.7f
                                    }
                            )

                            JupiterGradientButton(
                                text = "Create First Habit",
                                onClick = { navController.navigate(NavRoutes.ADD_HABIT) },
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale * 0.9f
                                        scaleY = iconScale * 0.9f
                                    }
                            )
                        }
                    }
                } else {
                    // Habits list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("habit_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(
                            items = filteredHabits,
                            key = { _, habit -> habit.id }
                        ) { index, habit ->
                            Box(
                                modifier = Modifier.animeEntrance(
                                    visible = !isLoading,
                                    index = index,
                                    baseDelay = 100, // Slightly longer delay than chips
                                    duration = 600,
                                    initialOffsetY = 50,
                                    easing = AnimeEasing.EaseOutQuint
                                )
                            ) {
                                HabitItem(
                                    habit = habit,
                                    onItemClick = { navController.navigate(NavRoutes.habitDetails(habit.id)) },
                                    onCompletedClick = { viewModel.markHabitCompleted(habit.id) },
                                    onDeleteClick = { viewModel.deleteHabit(habit) },
                                    onToggleEnabled = { viewModel.toggleHabitEnabled(habit) },
                                    onNeuralInterfaceClick = { navController.navigate(NavRoutes.neuralInterface(habit.id)) },
                                    onCompletionHistoryClick = {
                                        navController.navigate(NavRoutes.habitCompletion(habit.id, habit.name))
                                    },
                                    onARVisualizationClick = {
                                        navController.navigate(NavRoutes.ar(habit.id))
                                    },
                                    onBiometricIntegrationClick = {
                                        navController.navigate(NavRoutes.biometricIntegration(habit.id))
                                    },
                                    onQuantumVisualizationClick = {
                                        navController.navigate(NavRoutes.quantumVisualization(habit.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

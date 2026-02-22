package com.example.myapplication.features.habits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.core.ui.animation.AnimeEasing
import com.example.myapplication.core.ui.animation.ParticleWave
import com.example.myapplication.core.ui.animation.animeEntrance
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
    val listState = rememberLazyStaggeredGridState()
    // Data loads reactively — no artificial delay needed
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(habits) {
        isLoading = false
    }

    // Also handle the initial case where habits is empty on first load
    LaunchedEffect(Unit) {
        delay(600) // Short grace period for initial DB query
        isLoading = false
    }

    // Category filtering
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All") + habits.mapNotNull { it.category }.distinct().sorted()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Habits",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (habits.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(NavRoutes.ADD_HABIT) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("Add Habit", style = MaterialTheme.typography.labelLarge) }
                )
            }
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
                // Category filter card — only shown when there are habits to filter
                if (habits.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .graphicsLayer {
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
                        // Horizontal category chips (no header label needed — chips are self-explanatory)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEachIndexed { index, category ->
                                val isSelected = category == selectedCategory

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
                            )

                            Text(
                                "Tap the + button to start tracking your first habit",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                            )

                            Button(
                                onClick = { navController.navigate(NavRoutes.ADD_HABIT) },
                                modifier = Modifier
                                    .padding(top = 16.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Create First Habit", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                } else {
                    val configuration = LocalConfiguration.current
                    val columns = when {
                        configuration.screenWidthDp < 600 -> 1 // Phone
                        configuration.screenWidthDp < 840 -> 2 // Foldable
                        else -> 3 // Tablet Landscape
                    }
                    
                    // Habits list array adaptive to screen width
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(columns),
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("habit_list"),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalItemSpacing = 16.dp
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

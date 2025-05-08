package com.example.myapplication.features.habits

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.Habit
import com.example.myapplication.navigation.NavRoutes
import com.example.myapplication.ui.animation.*
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.animatedTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    navController: NavController,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val habits by viewModel.habits.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    
    // List state for scroll animations
    val listState = rememberLazyListState()
    // Animation states
    var isLoading by remember { mutableStateOf(true) }
    var showBackgroundAnimation by remember { mutableStateOf(true) }
    
    // Track scroll position for parallax effects
    val firstVisibleItemScrollOffset = remember { mutableStateOf(0) }
    val firstVisibleItemIndex = remember { mutableStateOf(0) }
    
    // Update scroll position when list scrolls
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }.collect { offset ->
            firstVisibleItemScrollOffset.value = offset
        }
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            firstVisibleItemIndex.value = index
        }
    }
    
    // Simulate loading state briefly
    LaunchedEffect(true) {
        delay(1500)
        isLoading = false
    }
    
    // Add a dropdown for category filtering
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All") + habits.mapNotNull { it.category }.distinct().sorted()
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    // Title animation
    val titleAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "titleAlpha"
    )
    
    // Floating action button animation
    val fabScale by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fabScale"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "My Habits",
                        modifier = Modifier.alpha(titleAlpha),
                        style = MaterialTheme.typography.headlineMedium
                    ) 
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(NavRoutes.STATS) },
                        modifier = Modifier
                            .scale(titleAlpha)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.BarChart, 
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = { 
            FloatingActionButton(
                onClick = {
                    Log.d("HabitListScreen", "FAB clicked, navigating to ADD_HABIT route")
                    navController.navigate(NavRoutes.ADD_HABIT)
                },
                modifier = Modifier
                    .scale(fabScale)
                    .pulseEffect(pulseEnabled = !isLoading),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, "Add new habit")
            }
        }
    ) { innerPadding ->
        // Determine background colors based on theme and habit counts
        val bgColors = MaterialTheme.animatedTheme.gradients.primary
        val particleColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        
        // Create a box with the parallax background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Only show parallax background if enabled and not loading
            if (showBackgroundAnimation && !isLoading) {
                EnhancedParallaxBackground(
                    backgroundColors = bgColors,
                    particleColor = particleColor
                ) {
                    // This content placeholder is needed but will be overlaid
                }
            }
            
            // Main content
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Category filter card with 3D effect
                ThreeDCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .animeEntrance(
                            visible = !isLoading,
                            initialOffsetY = -100,
                            delayMillis = 300
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Filter by Category",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
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
                                val chipColor by animateColorAsState(
                                    targetValue = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    label = "chipColor"
                                )
                                val textColor by animateColorAsState(
                                    targetValue = if (isSelected) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    label = "textColor"
                                )
                                val scale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.05f else 1f,
                                    label = "chipScale"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(chipColor)
                                        .clickable { 
                                            selectedCategory = category
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .scale(scale)
                                ) {
                                    Text(
                                        category,
                                        color = textColor,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter habits based on the selected category
                val filteredHabits = if (selectedCategory == "All") 
                    habits 
                else 
                    habits.filter { it.category == selectedCategory }

                if (isLoading) {
                    // Show shimmer loading effect
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Create shimmer loading placeholders
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(16.dp)
                        ) {
                            repeat(3) { index ->
                                ShimmerItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .padding(vertical = 8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }
                        }
                    }
                } else if (filteredHabits.isEmpty()) {
                    // Empty state with animation
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Empty state illustration
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .floatingEffect(enabled = true, amplitude = 10f)
                                .background(
                                    brush = animatedGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add habits",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "No habits yet",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Tap the + button to start tracking your first habit",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        GradientButton(
                            text = "Create First Habit",
                            onClick = { navController.navigate(NavRoutes.ADD_HABIT) },
                            modifier = Modifier.padding(horizontal = 32.dp),
                            gradientColors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                } else {
                    // Habits list with staggered animation
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = filteredHabits,
                            key = { it.id }
                        ) { habit ->
                            val index = filteredHabits.indexOf(habit)
                            HabitItem(
                                habit = habit,
                                onItemClick = { navController.navigate(NavRoutes.editHabit(habit.id)) },
                                onCompletedClick = { viewModel.markHabitCompleted(habit.id) },
                                onDeleteClick = { viewModel.deleteHabit(habit) },
                                onToggleEnabled = { viewModel.toggleHabitEnabled(habit) },
                                index = index // Pass index for staggered animation
                            )
                        }
                    }
                }
            }
            
            // Background toggle button at the bottom corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .alpha(if (isLoading) 0f else 0.7f)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showBackgroundAnimation = !showBackgroundAnimation }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Toggle background animation",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
package com.example.myapplication.features.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.data.model.Habit
import com.example.myapplication.navigation.NavRoutes
import com.example.myapplication.ui.components.JupiterGradientButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    navController: NavController,
    viewModel: HabitViewModel = hiltViewModel()
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Category filter card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                            categories.forEach { category ->
                                val isSelected = category == selectedCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Filter habits
                val filteredHabits = if (selectedCategory == "All") 
                    habits 
                else 
                    habits.filter { it.category == selectedCategory }

                if (isLoading) {
                    // Simple loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filteredHabits.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add habit",
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(bottom = 16.dp)
                                    .alpha(0.6f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "No habits yet",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                "Tap the + button to start tracking your first habit",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            JupiterGradientButton(
                                text = "Create First Habit",
                                onClick = { navController.navigate(NavRoutes.ADD_HABIT) },
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                } else {
                    // Habits list
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
                            HabitItem(
                                habit = habit,
                                onItemClick = { navController.navigate(NavRoutes.editHabit(habit.id)) },
                                onCompletedClick = { viewModel.markHabitCompleted(habit.id) },
                                onDeleteClick = { viewModel.deleteHabit(habit) },
                                onToggleEnabled = { viewModel.toggleHabitEnabled(habit) }
                            )
                        }
                    }
                }
            }
        }
    }
}
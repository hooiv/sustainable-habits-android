package com.example.myapplication.features.habits

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.Habit
import com.example.myapplication.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    navController: NavController,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val habits by viewModel.habits.collectAsState(initial = emptyList())

    // Add a dropdown for category filtering
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All") + habits.mapNotNull { it.category }.distinct()
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sustainable Habits") },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.STATS) }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Stats")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = { 
            FloatingActionButton(onClick = {
                Log.d("HabitListScreen", "FAB clicked, navigating to ADD_HABIT route")
                navController.navigate(NavRoutes.ADD_HABIT)
            }) {
                Icon(Icons.Filled.Add, "Add new habit")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category filter dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter by Category:", style = MaterialTheme.typography.bodyLarge)

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Filter habits based on the selected category
            val filteredHabits = if (selectedCategory == "All") habits else habits.filter { it.category == selectedCategory }

            if (filteredHabits.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No habits yet. Tap the + button to add one!")
                }
            } else {
                LazyColumn(
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
package com.example.myapplication.features.habits.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.features.habits.HabitViewModel
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.features.habits.ui.HabitItem
import com.example.myapplication.navigation.Screen
import com.example.myapplication.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    navController: NavController,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val habits by viewModel.habits.collectAsState()
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    // Show confirmation dialog when deleting a habit
    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Delete Habit") },
            text = { Text("Are you sure you want to delete \"${habit.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHabit(habit)
                        habitToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Habits") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddHabit.route) },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Habit"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (habits.isEmpty()) {
                // Show empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No habits yet",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to add your first habit",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Show habit list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(
                        items = habits,
                        key = { habit -> habit.id } // Use stable ID for better animations
                    ) { habit ->
                        HabitItem(
                            habit = habit,
                            onItemClick = {
                                navController.navigate("${Screen.EditHabit.route}/${habit.id}")
                            },
                            onCompletedClick = {
                                viewModel.markHabitCompleted(habit.id)
                            },
                            onDeleteClick = {
                                habitToDelete = habit
                            },
                            onToggleEnabled = {
                                viewModel.toggleHabitEnabled(habit)
                            },
                            onNeuralInterfaceClick = {
                                navController.navigate(NavRoutes.neuralInterface(habit.id))
                            },
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

package com.example.myapplication.features.habits

import android.util.Log // Import Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.model.Habit
import com.example.myapplication.navigation.NavRoutes
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    navController: NavController,
    viewModel: HabitViewModel = hiltViewModel() // Inject ViewModel
) {
    val habits by viewModel.habits.collectAsState() // Collect habits as state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sustainable Habits") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = { 
            FloatingActionButton(onClick = {
                Log.d("HabitListScreen", "FAB clicked, navigating to ADD_HABIT route: ${NavRoutes.ADD_HABIT}")
                try {
                    // Use navigate(route) instead of navigate(route_string) to avoid deep link interpretation
                    navController.navigate(NavRoutes.ADD_HABIT) {
                        // Add navigation options to ensure proper navigation behavior
                        launchSingleTop = true
                    }
                    Log.d("HabitListScreen", "Navigation executed successfully")
                } catch (e: Exception) {
                    Log.e("HabitListScreen", "Navigation error: ${e.message}", e)
                }
            }) { // Navigate to AddHabitScreen
                Icon(Icons.Filled.Add, "Add new habit")
            }
        }
    ) { innerPadding ->
        if (habits.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No habits yet. Tap the + button to add one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = habits,
                    key = { it.id }
                ) { habit ->
                    // Animated visibility for item add/delete
                    AnimatedVisibility(
                        visible = true, // Always true for now, but can be used for delete animation
                        enter = fadeIn(tween(500)) + scaleIn(tween(500)),
                        exit = fadeOut(tween(500)) + scaleOut(tween(500))
                    ) {
                        HabitItem(
                            habit = habit,
                            onDeleteClicked = { viewModel.deleteHabit(habit) },
                            onEditClicked = { navController.navigate(NavRoutes.editHabit(habit.id)) },
                            onCompleteClicked = { viewModel.markHabitCompleted(habit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit, 
    onDeleteClicked: () -> Unit, 
    onEditClicked: () -> Unit,
    onCompleteClicked: () -> Unit = {},
    viewModel: HabitViewModel = hiltViewModel()
) {
    // Simple date formatter, consider moving to a utility class or injecting for testability
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Animate progress bar
    val animatedProgress by animateFloatAsState(
        targetValue = habit.goalProgress.toFloat() / habit.goal.toFloat(),
        animationSpec = tween(durationMillis = 700), label = "progressAnim"
    )

    // Animate streak chip scale and color
    val streakScale by animateFloatAsState(
        targetValue = if (habit.streak > 0) 1.15f else 1f,
        animationSpec = tween(durationMillis = 500), label = "streakScale"
    )
    val streakColor = if (habit.streak > 0) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top section with name and complete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.name, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                
                // Complete button
                FilledTonalButton(
                    onClick = onCompleteClicked,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text("Complete")
                }
            }
            
            // Description (if any)
            habit.description?.let {
                Text(
                    text = it, 
                    fontSize = 14.sp, 
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress indicator
            val progressText = "Progress: ${habit.goalProgress}/${habit.goal} ${habit.frequency.name.lowercase()}"
            Text(
                text = progressText,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LinearProgressIndicator(
                progress = animatedProgress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (habit.streak > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
            
            // Status section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Streak chip
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = streakColor,
                    modifier = Modifier.scale(streakScale)
                ) {
                    Text(
                        text = "🔥 ${habit.streak} streak",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Frequency chip
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = habit.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Last completed date
            habit.lastCompletedDate?.let {
                Text(
                    text = "Last completed: ${dateFormat.format(it)}",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEditClicked,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = onDeleteClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitListScreenPreview() {
    MyApplicationTheme {
        // In previews, we can't use hiltViewModel easily.
        // For preview purposes, we'll just pass an empty list instead of a real viewModel
        HabitListScreen(
            navController = rememberNavController(),
            // Remove the direct viewModel instantiation which would need a repository
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HabitItemPreview() {
    MyApplicationTheme {
        HabitItem(
            habit = Habit(name = "Preview Habit", description = "This is a test habit for preview."), 
            onDeleteClicked = {}, 
            onEditClicked = {} // Added empty lambda for preview
        )
    }
}
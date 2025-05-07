package com.example.myapplication.features.habits

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
            FloatingActionButton(onClick = { /* TODO: Navigate to add habit screen */ }) {
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
                items(habits) { habit ->
                    HabitItem(habit = habit)
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit) {
    // Simple date formatter, consider moving to a utility class or injecting for testability
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = habit.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            habit.description?.let {
                Text(text = it, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Frequency: ${habit.frequency}", fontSize = 12.sp)
            Text(text = "Goal: ${habit.goal} per ${habit.frequency.name.lowercase()}", fontSize = 12.sp)
            Text(text = "Streak: ${habit.streak}", fontSize = 12.sp)
            habit.lastCompletedDate?.let {
                Text(text = "Last completed: ${dateFormat.format(it)}", fontSize = 12.sp)
            }
            Text(text = "Created: ${dateFormat.format(habit.createdDate)}", fontSize = 12.sp)
            // TODO: Add buttons for completing a habit, editing, deleting etc.
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitListScreenPreview() {
    MyApplicationTheme {
        // In preview, we can't use hiltViewModel directly easily without more setup.
        // So, we create a dummy ViewModel or pass dummy data for preview.
        val previewViewModel = HabitViewModel() // This will show initial dummy data
        HabitListScreen(navController = rememberNavController(), viewModel = previewViewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun HabitItemPreview() {
    MyApplicationTheme {
        HabitItem(habit = Habit(name = "Preview Habit", description = "This is a test habit for preview."))
    }
}
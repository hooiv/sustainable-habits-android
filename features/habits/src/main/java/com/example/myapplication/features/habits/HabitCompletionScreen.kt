package com.example.myapplication.features.habits.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.features.habits.HabitCompletionViewModel
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.ui.components.AppScaffold
import java.text.SimpleDateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen for displaying and managing habit completions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCompletionScreen(
    navController: NavController,
    habitId: String,
    habitName: String,
    viewModel: HabitCompletionViewModel = hiltViewModel()
) {
    // Load completions for the habit
    LaunchedEffect(habitId) {
        viewModel.loadCompletionsForHabit(habitId)
    }

    // State
    val completions by viewModel.completions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Dialog state
    var showAddCompletionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedCompletion by remember { mutableStateOf<HabitCompletion?>(null) }
    
    // Add completion dialog state
    var completionNote by remember { mutableStateOf("") }
    var completionMood by remember { mutableStateOf<Int?>(null) }
    
    AppScaffold(
        title = "$habitName Completions",
        onNavigateBack = { navController.popBackStack() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Completion History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = { showAddCompletionDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Completion",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
                
                // Completions list
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (completions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No completions yet",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(completions) { completion ->
                            CompletionItem(
                                completion = completion,
                                onDelete = {
                                    selectedCompletion = completion
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            }
            
            // Error message
            if (error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error ?: "An error occurred")
                }
            }
            
            // Add completion dialog
            if (showAddCompletionDialog) {
                AlertDialog(
                    onDismissRequest = { showAddCompletionDialog = false },
                    title = { Text("Add Completion") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = completionNote,
                                onValueChange = { completionNote = it },
                                label = { Text("Note") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Mood", style = MaterialTheme.typography.bodyLarge)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (mood in 1..5) {
                                    IconButton(
                                        onClick = { completionMood = mood },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = if (completionMood == mood) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    Color.Transparent,
                                                shape = RoundedCornerShape(24.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = when (mood) {
                                                1 -> Icons.Default.SentimentVeryDissatisfied
                                                2 -> Icons.Default.SentimentDissatisfied
                                                3 -> Icons.Default.SentimentNeutral
                                                4 -> Icons.Default.SentimentSatisfied
                                                5 -> Icons.Default.SentimentVerySatisfied
                                                else -> Icons.Default.SentimentNeutral
                                            },
                                            contentDescription = "Mood $mood",
                                            tint = if (completionMood == mood) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.addCompletion(
                                    habitId = habitId,
                                    note = completionNote.takeIf { it.isNotBlank() },
                                    mood = completionMood
                                )
                                completionNote = ""
                                completionMood = null
                                showAddCompletionDialog = false
                            }
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showAddCompletionDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            
            // Delete confirmation dialog
            if (showDeleteConfirmDialog && selectedCompletion != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showDeleteConfirmDialog = false
                        selectedCompletion = null
                    },
                    title = { Text("Delete Completion") },
                    text = { Text("Are you sure you want to delete this completion?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedCompletion?.let { viewModel.deleteCompletion(it) }
                                showDeleteConfirmDialog = false
                                selectedCompletion = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showDeleteConfirmDialog = false
                                selectedCompletion = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Composable for displaying a single completion item
 */
@Composable
fun CompletionItem(
    completion: HabitCompletion,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(completion.completionDate))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            completion.note?.let { note ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (completion.mood != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mood: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = when (completion.mood) {
                            1 -> Icons.Default.SentimentVeryDissatisfied
                            2 -> Icons.Default.SentimentDissatisfied
                            3 -> Icons.Default.SentimentNeutral
                            4 -> Icons.Default.SentimentSatisfied
                            5 -> Icons.Default.SentimentVerySatisfied
                            else -> Icons.Default.SentimentNeutral
                        },
                        contentDescription = "Mood ${completion.mood}",
                        tint = when (completion.mood) {
                            1, 2 -> MaterialTheme.colorScheme.error
                            3 -> MaterialTheme.colorScheme.onSurface
                            4, 5 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

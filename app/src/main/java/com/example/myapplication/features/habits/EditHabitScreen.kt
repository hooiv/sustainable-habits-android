package com.example.myapplication.features.habits

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    navController: NavController,
    habitId: String?, // Habit ID to edit
    viewModel: HabitViewModel = hiltViewModel()
) {
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }
    var originalHabit by remember { mutableStateOf<Habit?>(null) }
    var goal by remember { mutableStateOf("1") }
    var reminderTime by remember { mutableStateOf<LocalTime?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Fetch the habit details when the screen is launched
    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.getHabitById(habitId).collectLatest { habit ->
                if (habit != null) {
                    originalHabit = habit
                    habitName = habit.name
                    habitDescription = habit.description ?: ""
                    selectedFrequency = habit.frequency
                    goal = habit.goal.toString()
                    
                    // Parse reminder time if it exists
                    habit.reminderTime?.let {
                        try {
                            val parts = it.split(":")
                            if (parts.size == 2) {
                                reminderTime = LocalTime.of(parts[0].toInt(), parts[1].toInt())
                            }
                        } catch (e: Exception) {
                            // Handle parse error gracefully
                            reminderTime = null
                        }
                    }
                }
            }
        }
    }
    
    // Show time picker dialog when requested
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderTime?.hour ?: 8,
            initialMinute = reminderTime?.minute ?: 0
        )
        
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                reminderTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            },
            timePickerState = timePickerState
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Habit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("Habit Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = habitDescription,
                onValueChange = { habitDescription = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Frequency Dropdown
            ExposedDropdownMenuBox(
                expanded = isFrequencyDropdownExpanded,
                onExpandedChange = { isFrequencyDropdownExpanded = !isFrequencyDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedFrequency.name,
                    onValueChange = {}, // Not directly editable
                    readOnly = true,
                    label = { Text("Frequency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFrequencyDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isFrequencyDropdownExpanded,
                    onDismissRequest = { isFrequencyDropdownExpanded = false }
                ) {
                    HabitFrequency.values().forEach { frequency ->
                        DropdownMenuItem(
                            text = { Text(frequency.name) },
                            onClick = {
                                selectedFrequency = frequency
                                isFrequencyDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Goal input
            OutlinedTextField(
                value = goal,
                onValueChange = { 
                    // Only allow numbers
                    if (it.isBlank() || it.toIntOrNull() != null) {
                        goal = it
                    }
                },
                label = { Text("Goal (times per ${selectedFrequency.name.lowercase()})") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
            
            // Reminder time picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Set reminder",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = "Set reminder time",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        reminderTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Set Time"
                    )
                }
            }
            
            if (reminderTime != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("You'll be reminded at ${reminderTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { reminderTime = null }) {
                        Text("Clear")
                    }
                }
            }
            
            // Show streak and completion statistics if available
            originalHabit?.let { habit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Current streak: ${habit.streak}")
                        if (habit.streak > 0) {
                            Text("🔥 Keep it up! You're on a roll!")
                        }
                        Text("Completed ${habit.completionHistory.size} times in total")
                        habit.lastCompletedDate?.let { lastDate ->
                            Text("Last completed: ${java.text.SimpleDateFormat("MMM dd, yyyy").format(lastDate)}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes button to the bottom

            Button(
                onClick = {
                    if (habitName.isNotBlank() && originalHabit != null) {
                        val goalValue = goal.toIntOrNull() ?: 1
                        val reminderTimeString = reminderTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
                        
                        val updatedHabit = originalHabit!!.copy(
                            name = habitName,
                            description = habitDescription.takeIf { it.isNotBlank() },
                            frequency = selectedFrequency,
                            goal = goalValue,
                            reminderTime = reminderTimeString
                        )
                        viewModel.updateHabit(updatedHabit)
                        navController.popBackStack() // Go back after saving
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = habitName.isNotBlank() && originalHabit != null // Enable button only if name is not blank and habit is loaded
            ) {
                Text("Save Changes")
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: TimePickerState
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

@Preview(showBackground = true)
@Composable
fun EditHabitScreenPreview() {
    MyApplicationTheme {
        EditHabitScreen(navController = rememberNavController(), habitId = null)
    }
}


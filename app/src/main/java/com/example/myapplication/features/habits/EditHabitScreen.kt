package com.example.myapplication.features.habits

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

    // Fetch the habit details when the screen is launched
    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.getHabitById(habitId).collectLatest { habit -> // Assuming getHabitById returns Flow<Habit?>
                if (habit != null) {
                    originalHabit = habit
                    habitName = habit.name
                    habitDescription = habit.description ?: ""
                    selectedFrequency = habit.frequency
                }
            }
        }
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

            Spacer(modifier = Modifier.weight(1f)) // Pushes button to the bottom

            Button(
                onClick = {
                    if (habitName.isNotBlank() && originalHabit != null) {
                        val updatedHabit = originalHabit!!.copy( // Create a copy to update
                            name = habitName,
                            description = habitDescription.takeIf { it.isNotBlank() },
                            frequency = selectedFrequency
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

@Preview(showBackground = true)
@Composable
fun EditHabitScreenPreview() {
    MyApplicationTheme {
        // Previewing with a dummy habitId is tricky as it involves ViewModel logic.
        // For a simple preview, we can pass null or a mock ViewModel.
        EditHabitScreen(navController = rememberNavController(), habitId = null)
    }
}


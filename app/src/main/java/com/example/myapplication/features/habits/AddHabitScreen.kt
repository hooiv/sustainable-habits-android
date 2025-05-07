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
import com.example.myapplication.data.model.HabitFrequency
import com.example.myapplication.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    navController: NavController,
    viewModel: HabitViewModel = hiltViewModel()
) {
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Habit") },
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
                    if (habitName.isNotBlank()) {
                        viewModel.addHabit(
                            name = habitName,
                            description = habitDescription,
                            frequency = selectedFrequency
                        )
                        navController.popBackStack() // Go back after saving
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = habitName.isNotBlank() // Enable button only if name is not blank
            ) {
                Text("Save Habit")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddHabitScreenPreview() {
    MyApplicationTheme {
        AddHabitScreen(navController = rememberNavController())
    }
}

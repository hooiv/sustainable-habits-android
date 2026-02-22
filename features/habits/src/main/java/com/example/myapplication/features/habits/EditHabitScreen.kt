package com.example.myapplication.features.habits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitFrequency
import com.example.myapplication.core.ui.theme.MyApplicationTheme
import com.example.myapplication.features.habits.HabitViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    navController: NavController,
    habitId: String?,
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
    var habitCategory by remember { mutableStateOf("") }

    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.getHabitById(habitId).collectLatest { habit ->
                if (habit != null) {
                    originalHabit = habit
                    habitName = habit.name
                    habitDescription = habit.description ?: ""
                    selectedFrequency = habit.frequency
                    goal = habit.goal.toString()
                    habitCategory = habit.category ?: ""
                    habit.reminderTime?.let {
                        try {
                            val parts = it.split(":")
                            if (parts.size == 2) reminderTime = LocalTime.of(parts[0].toInt(), parts[1].toInt())
                        } catch (_: Exception) { reminderTime = null }
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderTime?.hour ?: 8,
            initialMinute = reminderTime?.minute ?: 0
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    reminderTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Edit Habit",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─── Form Fields ──────────────────────────────────────────
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit Name") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = habitDescription,
                    onValueChange = { habitDescription = it },
                    label = { Text("Description (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = habitCategory,
                    onValueChange = { habitCategory = it },
                    label = { Text("Category") },
                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                // Frequency dropdown
                ExposedDropdownMenuBox(
                    expanded = isFrequencyDropdownExpanded,
                    onExpandedChange = { isFrequencyDropdownExpanded = !isFrequencyDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedFrequency.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFrequencyDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isFrequencyDropdownExpanded,
                        onDismissRequest = { isFrequencyDropdownExpanded = false }
                    ) {
                        HabitFrequency.values().forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = { selectedFrequency = freq; isFrequencyDropdownExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = goal,
                    onValueChange = { if (it.isBlank() || it.toIntOrNull() != null) goal = it },
                    label = { Text("Goal (times per ${selectedFrequency.name.lowercase()})") },
                    leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp)
                )

                // Reminder row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Daily Reminder", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(
                                reminderTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Not set",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (reminderTime != null) {
                            TextButton(onClick = { reminderTime = null }) { Text("Clear") }
                        }
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (reminderTime != null) "Change" else "Set Time")
                        }
                    }
                }

                // ─── Stats card ───────────────────────────────────────────
                originalHabit?.let { habit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Statistics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                StatChip(label = "Streak", value = "🔥 ${habit.streak}", modifier = Modifier.weight(1f))
                                StatChip(label = "Completions", value = "✅ ${habit.completionHistory.size}", modifier = Modifier.weight(1f))
                            }
                            habit.lastCompletedDate?.let { date ->
                                Text(
                                    "Last completed: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ─── Save Button ──────────────────────────────────────────
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (habitName.isNotBlank()) {
                            originalHabit?.let { original ->
                                val updatedHabit = original.copy(
                                    name = habitName,
                                    description = habitDescription.takeIf { it.isNotBlank() },
                                    frequency = selectedFrequency,
                                    goal = goal.toIntOrNull() ?: 1,
                                    reminderTime = reminderTime?.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    category = habitCategory.takeIf { it.isNotBlank() }
                                )
                                viewModel.updateHabit(updatedHabit)
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = habitName.isNotBlank()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        "Save Changes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun EditHabitScreenPreview() {
    MyApplicationTheme {
        EditHabitScreen(navController = rememberNavController(), habitId = null)
    }
}

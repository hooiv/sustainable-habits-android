package com.example.myapplication.features.habits.ui

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import com.example.myapplication.features.habits.HabitViewModel
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.core.data.model.HabitFrequency
import com.example.myapplication.core.ui.animation.AnimeEasing
import com.example.myapplication.core.ui.animation.ParticleWave
import com.example.myapplication.core.ui.animation.animeEntrance
import com.example.myapplication.core.ui.theme.MyApplicationTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    navController: NavController,
    viewModel: HabitViewModel = hiltViewModel()
) {
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }
    var habitCategory by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf<LocalTime?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    var goal by remember { mutableStateOf("1") }

    val context = LocalContext.current

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!matches.isNullOrEmpty()) {
            habitName = matches[0] // Pre-fill the habit name with the first result
        } else {
            Toast.makeText(context, "No speech input detected", Toast.LENGTH_SHORT).show()
        }
    }

    // Show time picker dialog when requested
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderTime?.hour ?: 8,
            initialMinute = reminderTime?.minute ?: 0
        )

        AddHabitTimePickerDialog(
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Add New Habit",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        // Add animated background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            // Add subtle particle wave effect in the background
            ParticleWave(
                modifier = Modifier.fillMaxSize(),
                particleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                particleCount = 20,
                waveHeight = 20f,
                waveWidth = 1000f,
                speed = 0.2f
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Subtle pulse used for mic button (when empty) and reminder time button
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = AnimeEasing.EaseInOutQuad),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                // Animated form fields with staggered entrance
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animeEntrance(
                            visible = true,
                            index = 0,
                            baseDelay = 100,
                            duration = 800,
                            initialOffsetY = 50,
                            easing = AnimeEasing.EaseOutBack
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text("Habit Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Animated mic button
                    IconButton(
                        onClick = { launcher.launch(intent) },
                        modifier = Modifier
                            .scale(if (habitName.isEmpty()) scale else 1f)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = habitDescription,
                    onValueChange = { habitDescription = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animeEntrance(
                            visible = true,
                            index = 1,
                            baseDelay = 100,
                            duration = 800,
                            initialOffsetY = 50,
                            easing = AnimeEasing.EaseOutBack
                        ),
                    minLines = 3
                )

                // Category chips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animeEntrance(
                            visible = true,
                            index = 2,
                            baseDelay = 100,
                            duration = 800,
                            initialOffsetY = 50,
                            easing = AnimeEasing.EaseOutBack
                        )
                ) {
                    Text(
                        "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val presetCategories = listOf(
                        "Health", "Fitness", "Work", "Mindfulness",
                        "Learning", "Social", "Creativity", "Finance"
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetCategories) { cat ->
                            FilterChip(
                                selected = habitCategory == cat,
                                onClick = {
                                    habitCategory = if (habitCategory == cat) "" else cat
                                },
                                label = { Text(cat) },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                            )
                        }
                    }
                    // Custom category fallback
                    if (habitCategory.isNotEmpty() && presetCategories.none { it == habitCategory }) {
                        Text(
                            "Custom: $habitCategory",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    OutlinedTextField(
                        value = if (presetCategories.contains(habitCategory)) "" else habitCategory,
                        onValueChange = { habitCategory = it },
                        label = { Text("Custom category (optional)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }

                // Frequency Dropdown with animation
                ExposedDropdownMenuBox(
                    expanded = isFrequencyDropdownExpanded,
                    onExpandedChange = { isFrequencyDropdownExpanded = !isFrequencyDropdownExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animeEntrance(
                            visible = true,
                            index = 3,
                            baseDelay = 100,
                            duration = 800,
                            initialOffsetY = 50,
                            easing = AnimeEasing.EaseOutBack
                        )
                ) {
                    OutlinedTextField(
                        value = selectedFrequency.name.lowercase().replaceFirstChar { it.uppercase() },
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
                                text = { Text(frequency.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedFrequency = frequency
                                    isFrequencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Goal input with animation
                OutlinedTextField(
                    value = goal,
                    onValueChange = {
                        // Only allow numbers
                        if (it.isBlank() || it.toIntOrNull() != null) {
                            goal = it
                        }
                    },
                    label = { Text("Goal (times per ${selectedFrequency.name.lowercase()})") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animeEntrance(
                            visible = true,
                            index = 4,
                            baseDelay = 100,
                            duration = 800,
                            initialOffsetY = 50,
                            easing = AnimeEasing.EaseOutBack
                        ),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                // Reminder time picker with animation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animeEntrance(
                            visible = true,
                            index = 5,
                            baseDelay = 100,
                            duration = 800,
                            initialOffsetY = 50,
                            easing = AnimeEasing.EaseOutBack
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set reminder",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
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

                Spacer(modifier = Modifier.weight(1f)) // Pushes button to the bottom

                Button(
                    onClick = {
                        if (habitName.isNotBlank()) {
                            val goalValue = goal.toIntOrNull() ?: 1
                            val reminderTimeString = reminderTime?.format(DateTimeFormatter.ofPattern("HH:mm"))

                            viewModel.addHabit(
                                name = habitName,
                                description = habitDescription,
                                category = habitCategory.takeIf { it.isNotBlank() },
                                frequency = selectedFrequency,
                                goal = goalValue,
                                reminderTime = reminderTimeString
                            )
                            navController.popBackStack()
                        }
                    },
                    enabled = habitName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .animeEntrance(
                            visible = true,
                            index = 6,
                            baseDelay = 100,
                            duration = 800,
                            initialOffsetY = 50,
                            easing = AnimeEasing.EaseOutElastic
                        ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Save Habit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitTimePickerDialog(
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
fun AddHabitScreenPreview() {
    MyApplicationTheme {
        AddHabitScreen(navController = rememberNavController())
    }
}

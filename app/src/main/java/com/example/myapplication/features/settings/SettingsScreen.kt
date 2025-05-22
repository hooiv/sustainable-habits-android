@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.features.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.util.FirebaseUtil
import com.example.myapplication.util.ThemePreferenceManager
import com.example.myapplication.util.NotificationUtil
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import kotlinx.coroutines.launch
import com.example.myapplication.R
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.features.habits.HabitViewModel
import com.google.firebase.Timestamp
import java.util.Date
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.navigation.NavController
import com.example.myapplication.features.auth.AuthViewModel
import com.example.myapplication.navigation.NavRoutes

// Helper function to parse Firebase data map to a Habit domain object
// Adapted from HabitSyncWorker.kt
private fun parseHabitMapToDomain(id: String, data: Map<String, Any>): Habit? {
    return try {
        val name = data["name"] as? String ?: run {
            Log.e("SettingsScreenParser", "Habit name is null or not a String for ID $id. Skipping.")
            return null
        }
        val description = data["description"] as? String
        val frequencyString = data["frequency"] as? String ?: HabitFrequency.DAILY.name
        val frequency = try { HabitFrequency.valueOf(frequencyString) } catch (e: IllegalArgumentException) {
            Log.w("SettingsScreenParser", "Invalid frequency string '$frequencyString' for ID $id. Defaulting to DAILY.")
            HabitFrequency.DAILY
        }

        val goal = (data["goal"] as? Long)?.toInt() ?: 1
        val goalProgress = (data["goalProgress"] as? Long)?.toInt() ?: 0
        val streak = (data["streak"] as? Long)?.toInt() ?: 0

        val createdTimestamp = data["createdDate"] as? Timestamp
        val createdDate = createdTimestamp?.toDate() ?: run {
            Log.w("SettingsScreenParser", "createdDate is null or not a Timestamp for ID $id. Using current date as fallback.")
            Date()
        }

        val lastUpdatedTimestampFirebase = data["lastUpdatedTimestamp"] as? Timestamp
        val lastUpdatedTimestamp = lastUpdatedTimestampFirebase?.toDate() ?: createdDate

        val lastCompletedTimestamp = data["lastCompletedDate"] as? Timestamp
        val lastCompletedDate = lastCompletedTimestamp?.toDate()

        val completionHistoryFirebase = data["completionHistory"] as? List<*>
        val completionHistory = completionHistoryFirebase?.mapNotNull {
            (it as? Timestamp)?.toDate()
        }?.toMutableList() ?: mutableListOf()

        val isEnabled = data["isEnabled"] as? Boolean ?: true
        val reminderTime = data["reminderTime"] as? String

        val unlockedBadgesFirebase = data["unlockedBadges"] as? List<*>
        val unlockedBadges = unlockedBadgesFirebase?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList()

        val category = data["category"] as? String

        Habit(
            id = id,
            name = name,
            description = description,
            frequency = frequency,
            goal = goal,
            goalProgress = goalProgress,
            streak = streak,
            createdDate = createdDate,
            lastUpdatedTimestamp = lastUpdatedTimestamp,
            lastCompletedDate = lastCompletedDate,
            completionHistory = completionHistory,
            isEnabled = isEnabled,
            reminderTime = reminderTime,
            unlockedBadges = unlockedBadges,
            category = category
        )
    } catch (e: Exception) {
        Log.e("SettingsScreenParser", "Failed to parse habit with id $id: ${e.message}", e)
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    context: Context,
    habitViewModel: HabitViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(R.color.brand_gradient_start),
                                colorResource(R.color.brand_gradient_end)
                            )
                        )
                    ),
                title = {
                    Text(
                        "Settings",
                        color = colorResource(R.color.brand_accent),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isDarkModeEnabled by ThemePreferenceManager.isDarkModeEnabled(context).collectAsState(initial = false)
            val coroutineScope = rememberCoroutineScope()
            val authState by authViewModel.authState.collectAsState()
            val userId = authState.userId
            var notificationsEnabled by remember { mutableStateOf(true) }
            var selectedLanguage by remember { mutableStateOf("English") }
            val languages = listOf("English", "Hindi", "Spanish", "French")
            var isLanguageDropdownExpanded by remember { mutableStateOf(false) }
            var notificationHour by remember { mutableStateOf(8) }
            var notificationMinute by remember { mutableStateOf(0) }
            var notificationText by remember { mutableStateOf("Don't forget to complete your habit today!") }
            var notificationSoundEnabled by remember { mutableStateOf(true) }
            var notificationCustomSoundUri by remember { mutableStateOf<String?>(null) }

            val soundPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
                onResult = { uri: Uri? ->
                    notificationCustomSoundUri = uri?.toString()
                    // Re-schedule notification with the new sound
                    if (notificationsEnabled) {
                        NotificationUtil.scheduleDailyNotification(
                            context,
                            notificationHour,
                            notificationMinute,
                            notificationText,
                            notificationSoundEnabled,
                            notificationCustomSoundUri
                        )
                        Toast.makeText(context, "Notification sound updated.", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            HorizontalDivider()
            // Dark Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(text = "Dark Mode", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(
                    checked = isDarkModeEnabled,
                    onCheckedChange = {
                        coroutineScope.launch {
                            ThemePreferenceManager.setDarkModeEnabled(context, it)
                        }
                    }
                )
            }
            // Notifications Toggle (real settings)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(text = "Notifications", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        notificationsEnabled = enabled
                        if (enabled) {
                            NotificationUtil.scheduleDailyNotification(context, notificationHour, notificationMinute, notificationText, notificationSoundEnabled, notificationCustomSoundUri)
                            Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
                        } else {
                            NotificationUtil.cancelDailyNotification(context)
                            Toast.makeText(context, "Notifications disabled", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            if (notificationsEnabled) {
                // Notification time selection
                var showTimePicker by remember { mutableStateOf(false) }
                val formattedTime = String.format("%02d:%02d", notificationHour, notificationMinute)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Notification Time: $formattedTime", modifier = Modifier.weight(1f))
                    Button(onClick = { showTimePicker = true }) {
                        Text("Set Time")
                    }
                }
                if (showTimePicker) {
                    TimePickerDialog(
                        initialHour = notificationHour,
                        initialMinute = notificationMinute,
                        onTimeSelected = { hour, minute ->
                            notificationHour = hour
                            notificationMinute = minute
                            NotificationUtil.scheduleDailyNotification(context, hour, minute, notificationText, notificationSoundEnabled, notificationCustomSoundUri)
                            showTimePicker = false
                            Toast.makeText(context, "Notification time set to $hour:$minute", Toast.LENGTH_SHORT).show()
                        },
                        onDismiss = { showTimePicker = false }
                    )
                }
                // Custom notification text
                OutlinedTextField(
                    value = notificationText,
                    onValueChange = {
                        notificationText = it
                        NotificationUtil.scheduleDailyNotification(context, notificationHour, notificationMinute, notificationText, notificationSoundEnabled, notificationCustomSoundUri)
                    },
                    label = { Text("Notification Text") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                // Notification sound toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sound", modifier = Modifier.weight(1f))
                    Switch(
                        checked = notificationSoundEnabled,
                        onCheckedChange = {
                            notificationSoundEnabled = it
                            NotificationUtil.scheduleDailyNotification(context, notificationHour, notificationMinute, notificationText, notificationSoundEnabled, notificationCustomSoundUri)
                        }
                    )
                }
                // Custom sound picker (file picker intent)
                Button(onClick = {
                    if (notificationSoundEnabled) {
                        soundPickerLauncher.launch("audio/*")
                    } else {
                        Toast.makeText(context, "Enable notification sound first.", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text("Pick Custom Sound")
                }
                notificationCustomSoundUri?.let {
                    Text("Selected sound: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            // Language Selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(text = "Language", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Box {
                    Button(onClick = { isLanguageDropdownExpanded = true }) {
                        Text(selectedLanguage)
                    }
                    DropdownMenu(
                        expanded = isLanguageDropdownExpanded,
                        onDismissRequest = { isLanguageDropdownExpanded = false }
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    selectedLanguage = lang
                                    isLanguageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            HorizontalDivider()
            // Backup/Restore
            Button(onClick = {
                if (userId == null) {
                    navController.navigate(NavRoutes.SIGN_IN)
                    return@Button
                }
                val exampleHabitMap = mapOf(
                    "name" to "Example Habit",
                    "description" to "This is a sample habit",
                    "category" to "Health",
                    "frequency" to "DAILY",
                    "goal" to 1
                )
                // Construct a Habit object from the map
                val habitToBackup = Habit(
                    id = "settings_example_habit_" + System.currentTimeMillis(), // Generate a unique ID for this test
                    name = exampleHabitMap["name"] as String,
                    description = exampleHabitMap["description"] as? String,
                    category = exampleHabitMap["category"] as? String,
                    frequency = HabitFrequency.valueOf(exampleHabitMap["frequency"] as String),
                    goal = (exampleHabitMap["goal"] as Number).toInt()
                    // Other Habit fields like createdDate, lastUpdatedTimestamp will use defaults from the constructor
                )

                // Call backupHabitData with a list containing the single Habit
                FirebaseUtil.backupHabitData(userId, listOf(habitToBackup),
                    onSuccess = { Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show() },
                    onFailure = { exception -> // Explicitly named lambda parameter
                        Toast.makeText(context, "Backup failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }) {
                Text("Backup Data")
            }
            Button(onClick = {
                if (userId == null) {
                    navController.navigate(NavRoutes.SIGN_IN)
                    return@Button
                }
                coroutineScope.launch {
                    try {
                        val firebaseDataMap = FirebaseUtil.fetchHabitDataSuspend(userId)
                        if (firebaseDataMap.isEmpty()) {
                            Toast.makeText(context, "No data found in Firebase to restore.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val habitsToRestore = firebaseDataMap.mapNotNull { (id, dataMap) ->
                            parseHabitMapToDomain(id, dataMap)
                        }

                        if (habitsToRestore.isNotEmpty()) {
                            habitViewModel.restoreHabits(habitsToRestore)
                            Toast.makeText(context, "Restored ${habitsToRestore.size} habits successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No valid habits found to restore after parsing.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("SettingsScreenRestore", "Error restoring data: ${e.message}", e)
                        Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Restore Data")
            }

            // Sign Out Button
            if (authState.isSignedIn) {
                Button(onClick = {
                    authViewModel.signOut()
                    Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Sign Out")
                }
            } else {
                Button(onClick = { navController.navigate(NavRoutes.SIGN_IN) }) {
                    Text("Sign In")
                }
            }

            HorizontalDivider()

            // Animation Demo Button
            Button(
                onClick = { navController.navigate(NavRoutes.ANIMATION_DEMO) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Animation Demo")
            }

            // Quantum Visualization Button
            Button(
                onClick = { navController.navigate(NavRoutes.QUANTUM_VISUALIZATION) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quantum Visualization")
            }

            // AR Visualization Button
            Button(
                onClick = { navController.navigate(NavRoutes.AR_GLOBAL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("AR Visualization")
            }

            // Three.js Visualization Button
            Button(
                onClick = { navController.navigate(NavRoutes.THREEJS_VISUALIZATION) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Three.js Visualization")
            }

            // Biometric Integration Button
            Button(
                onClick = { navController.navigate(NavRoutes.BIOMETRIC_INTEGRATION_GLOBAL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Biometric Integration")
            }

            // Voice Integration Button
            Button(
                onClick = { navController.navigate(NavRoutes.VOICE_INTEGRATION) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voice Commands")
            }

            // Spatial Computing Button
            Button(
                onClick = { navController.navigate(NavRoutes.SPATIAL_COMPUTING) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Spatial Computing")
            }

            HorizontalDivider()
            // About Section
            Text(
                text = "App Version: 1.0.0\nDeveloped by YourName\n© 2025",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

// Compose TimePickerDialog implementation
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = remember { mutableStateOf(Pair(initialHour, initialMinute)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(timePickerState.value.first, timePickerState.value.second)
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Select Time") },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Hour
                OutlinedTextField(
                    value = timePickerState.value.first.toString().padStart(2, '0'),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let { h ->
                            if (h in 0..23) timePickerState.value = timePickerState.value.copy(first = h)
                        }
                    },
                    label = { Text("Hour") },
                    modifier = Modifier.width(80.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(":")
                Spacer(Modifier.width(8.dp))
                // Minute
                OutlinedTextField(
                    value = timePickerState.value.second.toString().padStart(2, '0'),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let { m ->
                            if (m in 0..59) timePickerState.value = timePickerState.value.copy(second = m)
                        }
                    },
                    label = { Text("Minute") },
                    modifier = Modifier.width(80.dp)
                )
            }
        }
    )
}
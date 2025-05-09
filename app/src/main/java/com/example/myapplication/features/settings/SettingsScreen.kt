@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.features.settings

import android.content.Context
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
import kotlinx.coroutines.launch
import com.example.myapplication.R
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(context: Context) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isDarkModeEnabled by ThemePreferenceManager.isDarkModeEnabled(context).collectAsState(initial = false)
            val coroutineScope = rememberCoroutineScope()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
            var notificationsEnabled by remember { mutableStateOf(true) }
            var selectedLanguage by remember { mutableStateOf("English") }
            val languages = listOf("English", "Hindi", "Spanish", "French")
            var isLanguageDropdownExpanded by remember { mutableStateOf(false) }
            var notificationHour by remember { mutableStateOf(8) }
            var notificationMinute by remember { mutableStateOf(0) }
            var notificationText by remember { mutableStateOf("Don't forget to complete your habit today!") }
            var notificationSoundEnabled by remember { mutableStateOf(true) }
            var notificationCustomSoundUri by remember { mutableStateOf<String?>(null) }

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
                    // TODO: Implement file picker for custom sound URI
                    Toast.makeText(context, "Custom sound picker not implemented in this demo.", Toast.LENGTH_SHORT).show()
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
                val habitData = mapOf(
                    "name" to "Example Habit",
                    "description" to "This is a sample habit",
                    "category" to "Health",
                    "frequency" to "DAILY",
                    "goal" to 1
                )
                FirebaseUtil.saveHabitData(userId, habitData,
                    onSuccess = { Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show() },
                    onFailure = { Toast.makeText(context, "Backup failed: ${it.message}", Toast.LENGTH_SHORT).show() }
                )
            }) {
                Text("Backup Data")
            }
            Button(onClick = {
                FirebaseUtil.fetchHabitData(userId,
                    onSuccess = { habits ->
                        Toast.makeText(context, "Fetched ${habits.size} habits", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { Toast.makeText(context, "Fetch failed: ${it.message}", Toast.LENGTH_SHORT).show() }
                )
            }) {
                Text("Restore Data")
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

// --- NotificationUtil update required for custom text and sound ---
// Add these parameters to NotificationUtil.scheduleDailyNotification and update NotificationWorker accordingly.
// --- End NotificationUtil update note ---

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
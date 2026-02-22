@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.features.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitFrequency
import com.example.myapplication.core.data.util.FirebaseUtil
import com.example.myapplication.core.data.util.ThemePreferenceManager
import com.example.myapplication.core.ui.navigation.NavRoutes
import com.example.myapplication.core.ui.util.NotificationUtil
import com.example.myapplication.features.auth.AuthViewModel
import com.example.myapplication.features.habits.HabitViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.Date

// Helper function to parse Firebase data map to a Habit domain object
private fun parseHabitMapToDomain(id: String, data: Map<String, Any>): Habit? {
    return try {
        val name = data["name"] as? String ?: return null
        val description = data["description"] as? String
        val frequencyString = data["frequency"] as? String ?: HabitFrequency.DAILY.name
        val frequency = try { HabitFrequency.valueOf(frequencyString) } catch (e: IllegalArgumentException) { HabitFrequency.DAILY }
        val goal = (data["goal"] as? Long)?.toInt() ?: 1
        val goalProgress = (data["goalProgress"] as? Long)?.toInt() ?: 0
        val streak = (data["streak"] as? Long)?.toInt() ?: 0
        val createdTimestamp = data["createdDate"] as? Timestamp
        val createdDate = createdTimestamp?.toDate() ?: Date()
        val lastUpdatedTimestamp = (data["lastUpdatedTimestamp"] as? Timestamp)?.toDate() ?: createdDate
        val lastCompletedDate = (data["lastCompletedDate"] as? Timestamp)?.toDate()
        val completionHistory = (data["completionHistory"] as? List<*>)?.mapNotNull { (it as? Timestamp)?.toDate() }?.toMutableList() ?: mutableListOf()
        val isEnabled = data["isEnabled"] as? Boolean ?: true
        val reminderTime = data["reminderTime"] as? String
        val unlockedBadges = (data["unlockedBadges"] as? List<*>)?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList()
        val category = data["category"] as? String
        Habit(id = id, name = name, description = description, frequency = frequency, goal = goal, goalProgress = goalProgress, streak = streak, createdDate = createdDate, lastUpdatedTimestamp = lastUpdatedTimestamp, lastCompletedDate = lastCompletedDate, completionHistory = completionHistory, isEnabled = isEnabled, reminderTime = reminderTime, unlockedBadges = unlockedBadges, category = category)
    } catch (e: Exception) {
        Log.e("SettingsScreenParser", "Failed to parse habit $id: ${e.message}", e)
        null
    }
}

@Composable
fun SettingsScreen(
    navController: NavController,
    context: Context,
    habitViewModel: HabitViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isDarkModeEnabled by remember(context) { ThemePreferenceManager.isDarkModeEnabled(context) }.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    val authState by authViewModel.authState.collectAsState()
    val userId = authState.userId
    val habits by habitViewModel.habits.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var notificationHour by remember { mutableStateOf(8) }
    var notificationMinute by remember { mutableStateOf(0) }
    var notificationText by remember { mutableStateOf("Don't forget to complete your habit today!") }
    var notificationSoundEnabled by remember { mutableStateOf(true) }
    var notificationCustomSoundUri by remember { mutableStateOf<String?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            notificationCustomSoundUri = uri?.toString()
            if (notificationsEnabled) {
                NotificationUtil.scheduleDailyNotification(context, notificationHour, notificationMinute, notificationText, notificationSoundEnabled, notificationCustomSoundUri)
                Toast.makeText(context, "Notification sound updated.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Appearance ───────────────────────────────────────────────
            SettingsSectionCard(title = "Appearance", icon = Icons.Default.Palette) {
                SettingsToggleRow(
                    icon = Icons.Default.DarkMode,
                    label = "Dark Mode",
                    checked = isDarkModeEnabled,
                    onCheckedChange = {
                        coroutineScope.launch { ThemePreferenceManager.setDarkModeEnabled(context, it) }
                    }
                )
            }

            // ─── Notifications ────────────────────────────────────────────
            SettingsSectionCard(title = "Notifications", icon = Icons.Default.Notifications) {
                SettingsToggleRow(
                    icon = Icons.Default.NotificationsActive,
                    label = "Daily Reminders",
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
                if (notificationsEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsActionRow(
                        icon = Icons.Default.Schedule,
                        label = "Reminder Time",
                        value = String.format("%02d:%02d", notificationHour, notificationMinute),
                        onClick = { showTimePicker = true }
                    )
                    SettingsToggleRow(
                        icon = Icons.Default.VolumeUp,
                        label = "Sound",
                        checked = notificationSoundEnabled,
                        onCheckedChange = {
                            notificationSoundEnabled = it
                            NotificationUtil.scheduleDailyNotification(context, notificationHour, notificationMinute, notificationText, notificationSoundEnabled, notificationCustomSoundUri)
                        }
                    )
                }
            }

            // ─── Data & Backup ────────────────────────────────────────────
            SettingsSectionCard(title = "Data & Backup", icon = Icons.Default.CloudSync) {
                SettingsActionRow(
                    icon = Icons.Default.CloudUpload,
                    label = "Backup to Cloud",
                    value = if (authState.isSignedIn) "Signed in" else "Sign in required",
                    onClick = {
                        if (userId == null) { navController.navigate(NavRoutes.SIGN_IN); return@SettingsActionRow }
                        if (habits.isEmpty()) {
                            Toast.makeText(context, "No habits to back up.", Toast.LENGTH_SHORT).show()
                            return@SettingsActionRow
                        }
                        FirebaseUtil.backupHabitData(userId, habits,
                            onSuccess = { Toast.makeText(context, "Backed up ${habits.size} habit(s) ✓", Toast.LENGTH_SHORT).show() },
                            onFailure = { Toast.makeText(context, "Backup failed: ${it.message}", Toast.LENGTH_SHORT).show() }
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsActionRow(
                    icon = Icons.Default.CloudDownload,
                    label = "Restore from Cloud",
                    value = if (authState.isSignedIn) "Signed in" else "Sign in required",
                    onClick = {
                        if (userId == null) { navController.navigate(NavRoutes.SIGN_IN); return@SettingsActionRow }
                        coroutineScope.launch {
                            try {
                                val dataMap = FirebaseUtil.fetchHabitDataSuspend(userId)
                                if (dataMap.isEmpty()) {
                                    Toast.makeText(context, "No backup found.", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val habits = dataMap.mapNotNull { (id, m) -> parseHabitMapToDomain(id, m) }
                                habitViewModel.restoreHabits(habits)
                                Toast.makeText(context, "Restored ${habits.size} habits ✓", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            // ─── Account ──────────────────────────────────────────────────
            SettingsSectionCard(title = "Account", icon = Icons.Default.AccountCircle) {
                if (authState.isSignedIn) {
                    SettingsActionRow(
                        icon = Icons.Default.Logout,
                        label = "Sign Out",
                        onClick = {
                            authViewModel.signOut()
                            Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                        },
                        tintDanger = true
                    )
                } else {
                    SettingsActionRow(
                        icon = Icons.Default.Login,
                        label = "Sign In",
                        onClick = { navController.navigate(NavRoutes.SIGN_IN) }
                    )
                }
            }

            // ─── Advanced ──────────────────────────────────────────────────
            SettingsSectionCard(title = "Advanced Features", icon = Icons.Default.AutoAwesome) {
                SettingsNavRow(icon = Icons.Default.Face, label = "AI Assistant", onClick = { navController.navigate(NavRoutes.AI_ASSISTANT) })
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsNavRow(icon = Icons.Default.EmojiEvents, label = "Achievements & Badges", onClick = { navController.navigate(NavRoutes.GAMIFICATION) })
            }

            // ─── About ────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🌱", fontSize = 32.sp)
                    Text("Sustainable Habits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Build great habits, one day at a time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = notificationHour, initialMinute = notificationMinute)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    notificationHour = timePickerState.hour
                    notificationMinute = timePickerState.minute
                    NotificationUtil.scheduleDailyNotification(context, notificationHour, notificationMinute, notificationText, notificationSoundEnabled, notificationCustomSoundUri)
                    showTimePicker = false
                    Toast.makeText(context, "Reminder set for ${String.format("%02d:%02d", notificationHour, notificationMinute)}", Toast.LENGTH_SHORT).show()
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

// ─── Reusable Setting Card ──────────────────────────────────────────────────

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    label: String,
    value: String = "",
    onClick: () -> Unit,
    tintDanger: Boolean = false
) {
    val color = if (tintDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = if (tintDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            if (value.isNotEmpty()) {
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    SettingsActionRow(icon = icon, label = label, onClick = onClick)
}

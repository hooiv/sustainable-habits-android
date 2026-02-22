package com.hooiv.habitflow.features.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hooiv.habitflow.core.data.model.AIAssistantPersonalization

/**
 * AI Assistant Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantSettingsScreen(
    onNavigateBack: () -> Unit
) {
    var settings by remember { mutableStateOf(AIAssistantPersonalization()) }
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { settings = AIAssistantPersonalization() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset to defaults")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // General Settings
            SettingsSection(title = "General Settings") {
                SwitchSetting(
                    title = "Streaming Responses",
                    description = "Show AI responses as they are generated",
                    icon = Icons.Default.Stream,
                    checked = settings.useStreaming,
                    onCheckedChange = { settings = settings.copy(useStreaming = it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy Settings
            SettingsSection(title = "Privacy Settings") {
                SwitchSetting(
                    title = "Save Conversation History",
                    description = "Store conversations for context",
                    icon = Icons.Default.History,
                    checked = settings.saveConversationHistory,
                    onCheckedChange = { settings = settings.copy(saveConversationHistory = it) }
                )

                SwitchSetting(
                    title = "Share Habit Data",
                    description = "Use habit data for personalized advice",
                    icon = Icons.Default.Share,
                    checked = settings.shareHabitData,
                    onCheckedChange = { settings = settings.copy(shareHabitData = it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = { onNavigateBack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Save Settings")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Settings section with title
 */
@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Switch setting item
 */
@Composable
fun SwitchSetting(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

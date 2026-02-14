package com.example.myapplication.features.ai.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.data.model.AIAssistantPersonalization
import com.example.myapplication.features.ai.viewmodel.AIAssistantViewModel

/**
 * AI Assistant Settings Screen
 */
@Composable
fun AIAssistantSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AIAssistantViewModel = hiltViewModel()
) {
    var settings by remember { mutableStateOf(AIAssistantPersonalization()) }
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Reset to defaults
                        settings = AIAssistantPersonalization()
                    }) {
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
                
                SwitchSetting(
                    title = "Voice Interaction",
                    description = "Enable voice input and output",
                    icon = Icons.Default.RecordVoiceOver,
                    checked = settings.useVoice,
                    onCheckedChange = { settings = settings.copy(useVoice = it) }
                )
                
                if (settings.useVoice) {
                    SliderSetting(
                        title = "Voice Speed",
                        value = settings.voiceSpeed,
                        valueRange = 0.5f..2.0f,
                        steps = 6,
                        onValueChange = { settings = settings.copy(voiceSpeed = it) }
                    )
                    
                    SliderSetting(
                        title = "Voice Pitch",
                        value = settings.voicePitch,
                        valueRange = 0.5f..2.0f,
                        steps = 6,
                        onValueChange = { settings = settings.copy(voicePitch = it) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Voice Recognition Settings
            SettingsSection(title = "Voice Recognition") {
                SwitchSetting(
                    title = "Continuous Listening",
                    description = "Listen continuously for commands",
                    icon = Icons.Default.Hearing,
                    checked = settings.continuousListening,
                    onCheckedChange = { settings = settings.copy(continuousListening = it) }
                )
                
                SwitchSetting(
                    title = "Wake Word Detection",
                    description = "Activate with phrases like 'Hey Assistant'",
                    icon = Icons.Default.KeyboardVoice,
                    checked = settings.useWakeWord,
                    onCheckedChange = { settings = settings.copy(useWakeWord = it) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Context Settings
            SettingsSection(title = "Context Settings") {
                SwitchSetting(
                    title = "Include Mood Data",
                    description = "Use mood data to personalize responses",
                    icon = Icons.Default.Mood,
                    checked = settings.includeMoodData,
                    onCheckedChange = { settings = settings.copy(includeMoodData = it) }
                )
                
                SwitchSetting(
                    title = "Include Location Data",
                    description = "Use location for contextual recommendations",
                    icon = Icons.Default.LocationOn,
                    checked = settings.includeLocationData,
                    onCheckedChange = { settings = settings.copy(includeLocationData = it) }
                )
                
                SwitchSetting(
                    title = "Include Time Patterns",
                    description = "Analyze time patterns for better suggestions",
                    icon = Icons.Default.Schedule,
                    checked = settings.includeTimePatterns,
                    onCheckedChange = { settings = settings.copy(includeTimePatterns = it) }
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
                onClick = {
                    // Save settings and navigate back
                    // viewModel.savePersonalizationSettings(settings)
                    onNavigateBack()
                },
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

/**
 * Slider setting item
 */
@Composable
fun SliderSetting(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Slow",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            
            Text(
                text = "Fast",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = String.format("%.1fx", value),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

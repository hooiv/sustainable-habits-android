package com.example.myapplication.features.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.data.model.EntityType
import com.example.myapplication.core.data.model.VoiceCommand
import com.example.myapplication.core.data.model.VoiceEntity
import com.example.myapplication.core.data.model.VoiceIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

/**
 * Object containing voice components for easier access
 */
object VoiceComponents {

    /**
     * A voice recognition interface
     */
    @Composable
    fun VoiceRecognitionInterface(
        modifier: Modifier = Modifier,
        onVoiceCommand: (VoiceCommand) -> Unit = {}
    ) {
        // State for recording
        var isRecording by remember { mutableStateOf(false) }
        var recordingText by remember { mutableStateOf("") }

        // Animation for voice waves
        val infiniteTransition = rememberInfiniteTransition()
        val waveScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        val coroutineScope = rememberCoroutineScope()

        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Voice Recognition",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Voice input display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRecording) {
                            recordingText.ifEmpty { "Listening..." }
                        } else {
                            "Press the microphone button to speak"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Microphone button with voice recognition functionality
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Voice waves (only visible when recording)
                    if (isRecording) {
                        repeat(3) { index ->
                            val scale = 1f + (index + 1) * 0.5f * waveScale
                            Box(
                                modifier = Modifier
                                    .size(80.dp * scale)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.1f / scale
                                        )
                                    )
                            )
                        }
                    }

                    // Microphone button
                    Button(
                        onClick = {
                            isRecording = !isRecording

                            if (isRecording) {
                                // Start recording simulation
                                recordingText = ""
                                coroutineScope.launch {
                                    // Simulate typing
                                    val sampleCommands = listOf(
                                        "Create a new habit to drink water every day",
                                        "Complete my meditation habit for today",
                                        "Show me my exercise statistics",
                                        "Set a reminder for my reading habit at 9 PM"
                                    )

                                    val selectedCommand = sampleCommands.random()

                                    for (i in selectedCommand.indices) {
                                        recordingText = selectedCommand.substring(0, i + 1)
                                        delay(50)
                                    }

                                    delay(500)

                                    // Generate command
                                    val command = generateVoiceCommand(recordingText)
                                    onVoiceCommand(command)

                                    isRecording = false
                                }
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Icon(
                            imageVector = if (isRecording) {
                                Icons.Default.Stop
                            } else {
                                Icons.Default.Mic
                            },
                            contentDescription = if (isRecording) {
                                "Stop Recording"
                            } else {
                                "Start Recording"
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = if (isRecording) {
                        "Tap to stop"
                    } else {
                        "Tap to speak"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }

    /**
     * Display NLP results
     */
    @Composable
    fun NLPResultDisplay(
        command: VoiceCommand,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Natural Language Processing Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Command text
                Text(
                    text = "\"${command.text}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Intent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Intent:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(100.dp)
                    )

                    Text(
                        text = command.intent.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "${(command.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Entities
                Text(
                    text = "Entities:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                if (command.entities.isEmpty()) {
                    Text(
                        text = "No entities detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    command.entities.forEach { entity ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Entity type
                            Text(
                                text = entity.type.name.replace("_", " "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.width(100.dp)
                            )

                            // Entity value
                            Text(
                                text = "\"${entity.value}\"",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Confidence
                            Text(
                                text = "${(entity.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A voice recognition interface
 */
@Composable
fun VoiceRecognitionInterface(
    modifier: Modifier = Modifier,
    onVoiceCommand: (VoiceCommand) -> Unit = {}
) {
    // State for recording
    var isRecording by remember { mutableStateOf(false) }
    var recordingText by remember { mutableStateOf("") }

    // Animation for voice waves
    val infiniteTransition = rememberInfiniteTransition()
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Voice Recognition",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Voice input display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRecording) {
                        recordingText.ifEmpty { "Listening..." }
                    } else {
                        "Press the microphone button to speak"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone button
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Voice waves (only visible when recording)
                if (isRecording) {
                    repeat(3) { index ->
                        val scale = 1f + (index + 1) * 0.5f * waveScale
                        Box(
                            modifier = Modifier
                                .size(80.dp * scale)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.1f / scale
                                    )
                                )
                        )
                    }
                }

                // Microphone button
                Button(
                    onClick = {
                        isRecording = !isRecording

                        if (isRecording) {
                            // Start recording simulation
                            recordingText = ""
                            coroutineScope.launch {
                                // Simulate typing
                                val sampleCommands = listOf(
                                    "Create a new habit to drink water every day",
                                    "Complete my meditation habit for today",
                                    "Show me my exercise statistics",
                                    "Set a reminder for my reading habit at 9 PM"
                                )

                                val selectedCommand = sampleCommands.random()

                                for (i in selectedCommand.indices) {
                                    recordingText = selectedCommand.substring(0, i + 1)
                                    delay(50)
                                }

                                delay(500)

                                // Generate command
                                val command = generateVoiceCommand(recordingText)
                                onVoiceCommand(command)

                                isRecording = false
                            }
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    Icon(
                        imageVector = if (isRecording) {
                            Icons.Default.Stop
                        } else {
                            Icons.Default.Mic
                        },
                        contentDescription = if (isRecording) {
                            "Stop Recording"
                        } else {
                            "Start Recording"
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = if (isRecording) {
                    "Tap to stop"
                } else {
                    "Tap to speak"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Generate a voice command from text
 */
private fun generateVoiceCommand(text: String): VoiceCommand {
    // Determine intent
    val intent = when {
        text.contains("create", ignoreCase = true) -> VoiceIntent.CREATE_HABIT
        text.contains("complete", ignoreCase = true) -> VoiceIntent.COMPLETE_HABIT
        text.contains("show", ignoreCase = true) || text.contains("view", ignoreCase = true) -> {
            if (text.contains("statistics", ignoreCase = true) || text.contains("stats", ignoreCase = true)) {
                VoiceIntent.VIEW_STATS
            } else {
                VoiceIntent.VIEW_HABIT
            }
        }
        text.contains("reminder", ignoreCase = true) || text.contains("set", ignoreCase = true) -> VoiceIntent.SET_REMINDER
        else -> VoiceIntent.UNKNOWN
    }

    // Extract entities
    val entities = mutableListOf<VoiceEntity>()

    // Extract habit name
    val habitNameRegex = when (intent) {
        VoiceIntent.CREATE_HABIT -> "create a new habit to (.*?)(every|daily|weekly|monthly|at|$)".toRegex(RegexOption.IGNORE_CASE)
        VoiceIntent.COMPLETE_HABIT -> "complete my (.*?) (habit|for|$)".toRegex(RegexOption.IGNORE_CASE)
        VoiceIntent.VIEW_HABIT -> "show me my (.*?) (habit|stats|$)".toRegex(RegexOption.IGNORE_CASE)
        VoiceIntent.SET_REMINDER -> "set a reminder for my (.*?) (habit|at|$)".toRegex(RegexOption.IGNORE_CASE)
        else -> "(.*?)".toRegex()
    }

    habitNameRegex.find(text)?.let { matchResult ->
        val habitName = matchResult.groupValues[1].trim()
        if (habitName.isNotEmpty()) {
            entities.add(
                VoiceEntity(
                    type = EntityType.HABIT_NAME,
                    value = habitName,
                    confidence = 0.9f + Random.nextFloat() * 0.1f
                )
            )
        }
    }

    // Extract frequency
    val frequencyRegex = "(every day|daily|weekly|monthly)".toRegex(RegexOption.IGNORE_CASE)
    frequencyRegex.find(text)?.let { matchResult ->
        entities.add(
            VoiceEntity(
                type = EntityType.FREQUENCY,
                value = matchResult.value,
                confidence = 0.9f + Random.nextFloat() * 0.1f
            )
        )
    }

    // Extract time
    val timeRegex = "at (\\d+)(:\\d+)? (am|pm)".toRegex(RegexOption.IGNORE_CASE)
    timeRegex.find(text)?.let { matchResult ->
        entities.add(
            VoiceEntity(
                type = EntityType.TIME,
                value = matchResult.value.substring(3),
                confidence = 0.9f + Random.nextFloat() * 0.1f
            )
        )
    }

    return VoiceCommand(
        text = text,
        intent = intent,
        confidence = 0.8f + Random.nextFloat() * 0.2f,
        entities = entities
    )
}

/**
 * Display NLP results
 */
@Composable
fun NLPResultDisplay(
    command: VoiceCommand,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Natural Language Processing Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Command text
            Text(
                text = "\"${command.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Intent
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intent:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(100.dp)
                )

                Text(
                    text = command.intent.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${(command.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Entities
            Text(
                text = "Entities:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            if (command.entities.isEmpty()) {
                Text(
                    text = "No entities detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                command.entities.forEach { entity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Entity type
                        Text(
                            text = entity.type.name.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.width(100.dp)
                        )

                        // Entity value
                        Text(
                            text = "\"${entity.value}\"",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Confidence
                        Text(
                            text = "${(entity.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

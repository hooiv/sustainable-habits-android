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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset

import com.example.myapplication.core.data.model.EntityType
import com.example.myapplication.core.data.model.VoiceCommand
import com.example.myapplication.core.data.model.VoiceEntity
import com.example.myapplication.core.data.model.VoiceIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*
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

/**
 * Voice and NLP component
 */
@Composable
fun VoiceAndNlpCard(
    recognizedText: String,
    isListening: Boolean,
    isSpeaking: Boolean,
    nlpIntent: NlpIntent?,
    confidence: Float,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )

                Text(
                    text = "Voice & Natural Language",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Status indicators
                Row {
                    StatusIndicator(
                        isActive = isListening,
                        label = "Listening",
                        activeColor = Color.Green
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    StatusIndicator(
                        isActive = isSpeaking,
                        label = "Speaking",
                        activeColor = Color.Blue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice visualization
            VoiceVisualization(
                isListening = isListening,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recognized text
            Text(
                text = "Recognized Text:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp)
            ) {
                Text(
                    text = recognizedText.ifEmpty { "Say something..." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (recognizedText.isEmpty())
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // NLP intent
            if (nlpIntent != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Intent: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = nlpIntent.action.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    LinearProgressIndicator(
                        progress = confidence,
                        modifier = Modifier
                            .width(60.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Entities
                if (nlpIntent.entities.isNotEmpty()) {
                    Text(
                        text = "Entities:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    nlpIntent.entities.forEach { (key, value) ->
                        Row {
                            Text(
                                text = "$key: ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Response
                val response = generateResponse(nlpIntent)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(12.dp)
                        .clickable { onSpeak(response) }
                ) {
                    Text(
                        text = response,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = if (isListening) onStopListening else onStartListening,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(if (isListening) "Stop Listening" else "Start Listening")
                }
            }
        }
    }
}

/**
 * Status indicator
 */
@Composable
fun StatusIndicator(
    isActive: Boolean,
    label: String,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color.Gray)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Voice visualization
 */
@Composable
fun VoiceVisualization(
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Get colors outside of Canvas
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(surfaceVariantColor.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2

            // Draw baseline
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 1.dp.value * density
            )

            if (isListening) {
                // Draw voice waveform
                val numBars = 30
                val barWidth = width / numBars
                val maxBarHeight = height * 0.8f

                for (i in 0 until numBars) {
                    val x = i * barWidth
                    val seed = (i / numBars.toFloat() + animationProgress) * 10f
                    val barHeight = if (isListening) {
                        maxBarHeight * (0.2f + 0.8f * abs(sin(seed)))
                    } else {
                        maxBarHeight * 0.1f
                    }

                    val barColor = primaryColor.copy(
                        alpha = if (isListening) 0.7f else 0.3f
                    )

                    drawLine(
                        color = barColor,
                        start = Offset(x + barWidth / 2, centerY - barHeight / 2),
                        end = Offset(x + barWidth / 2, centerY + barHeight / 2),
                        strokeWidth = barWidth * 0.8f
                    )
                }
            } else {
                // Draw flat line with small ripples
                val path = Path()
                path.moveTo(0f, centerY)

                for (x in 0 until width.toInt() step 4) {
                    val progress = (x.toFloat() / width + animationProgress) % 1f
                    val y = centerY + height * 0.05f * sin(progress * PI.toFloat() * 10)
                    path.lineTo(x.toFloat(), y)
                }

                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = 0.3f),
                    style = Stroke(width = 2.dp.value * density)
                )
            }
        }

        if (!isListening) {
            Text(
                text = "Tap to speak",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * Generate response based on NLP intent
 */
private fun generateResponse(nlpIntent: NlpIntent): String {
    return when (nlpIntent.action) {
        "create" -> {
            val habitName = nlpIntent.entities["habit_name"] ?: "new habit"
            val time = nlpIntent.entities["time"]
            val frequency = nlpIntent.entities["frequency"]

            if (time != null && frequency != null) {
                "I'll create a $habitName habit for you, scheduled for $time, $frequency."
            } else if (time != null) {
                "I'll create a $habitName habit for you, scheduled for $time."
            } else if (frequency != null) {
                "I'll create a $habitName habit for you, with $frequency frequency."
            } else {
                "I'll create a $habitName habit for you."
            }
        }
        "complete" -> {
            val habitName = nlpIntent.entities["habit_name"] ?: "the habit"
            "I'll mark $habitName as completed."
        }
        "delete" -> {
            val habitName = nlpIntent.entities["habit_name"] ?: "the habit"
            "Are you sure you want to delete $habitName? Please confirm."
        }
        "update" -> {
            val habitName = nlpIntent.entities["habit_name"] ?: "the habit"
            "I'll update $habitName for you."
        }
        "view" -> {
            val habitName = nlpIntent.entities["habit_name"]
            if (habitName != null) {
                "Here's the information for $habitName."
            } else {
                "Here are your habits."
            }
        }
        else -> "I'm not sure what you want to do. Could you rephrase that?"
    }
}


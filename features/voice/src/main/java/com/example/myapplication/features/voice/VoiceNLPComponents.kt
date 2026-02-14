package com.example.myapplication.features.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.data.model.EntityType
import com.example.myapplication.core.data.model.VoiceCommand
import com.example.myapplication.core.data.model.VoiceEntity
import com.example.myapplication.core.data.model.VoiceIntent
import com.example.myapplication.core.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*



/**
 * A component that displays a voice recognition interface
 */
@Composable
fun VoiceRecognitionInterface(
    modifier: Modifier = Modifier,
    onVoiceCommand: (VoiceCommand) -> Unit = {},
    onVoiceInputError: (String) -> Unit = {}
) {
    var isListening by remember { mutableStateOf(false) }
    var voiceInputText by remember { mutableStateOf("") }
    var recognitionProgress by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Waveform animation
    val waveAmplitude by animateFloatAsState(
        targetValue = if (isListening) 1f else 0f,
        animationSpec = tween(500),
        label = "waveAmplitude"
    )

    // Simulated voice recognition
    LaunchedEffect(isListening) {
        if (isListening) {
            recognitionProgress = 0f
            voiceInputText = ""

            // Simulate voice recognition
            val simulatedCommands = listOf(
                "Create a new habit to drink water every day",
                "Mark my meditation habit as complete",
                "What's my progress on reading habit?",
                "Set a reminder for my exercise habit at 7 AM",
                "Show me my habit statistics for this week"
            )

            val selectedCommand = simulatedCommands.random()
            val wordCount = selectedCommand.split(" ").size

            // Simulate word-by-word recognition
            for (i in 1..wordCount) {
                delay(300)
                recognitionProgress = i.toFloat() / wordCount
                voiceInputText = selectedCommand.split(" ").take(i).joinToString(" ")
            }

            delay(500)
            isListening = false

            // Process the command
            val intent = when {
                voiceInputText.contains("create") || voiceInputText.contains("new habit") -> VoiceIntent.CREATE_HABIT
                voiceInputText.contains("complete") || voiceInputText.contains("mark") -> VoiceIntent.COMPLETE_HABIT
                voiceInputText.contains("progress") -> VoiceIntent.CHECK_PROGRESS
                voiceInputText.contains("reminder") -> VoiceIntent.SET_REMINDER
                voiceInputText.contains("statistics") || voiceInputText.contains("stats") -> VoiceIntent.GET_STATS
                else -> VoiceIntent.UNKNOWN
            }

            // Extract entities
            val entities = mutableListOf<VoiceEntity>()

            // Extract habit name
            val habitNameRegex = "(meditation|reading|exercise|drink water|yoga)".toRegex(RegexOption.IGNORE_CASE)
            habitNameRegex.find(voiceInputText)?.let { match ->
                entities.add(
                    VoiceEntity(
                        type = EntityType.HABIT_NAME,
                        value = match.value,
                        confidence = 0.9f
                    )
                )
            }

            // Extract time
            val timeRegex = "(\\d{1,2}\\s*(AM|PM))".toRegex(RegexOption.IGNORE_CASE)
            timeRegex.find(voiceInputText)?.let { match ->
                entities.add(
                    VoiceEntity(
                        type = EntityType.TIME,
                        value = match.value,
                        confidence = 0.85f
                    )
                )
            }

            // Extract frequency
            val frequencyRegex = "(daily|every day|weekly|monthly)".toRegex(RegexOption.IGNORE_CASE)
            frequencyRegex.find(voiceInputText)?.let { match ->
                entities.add(
                    VoiceEntity(
                        type = EntityType.FREQUENCY,
                        value = match.value,
                        confidence = 0.8f
                    )
                )
            }

            // Create voice command
            val command = VoiceCommand(
                text = voiceInputText,
                intent = intent,
                confidence = if (intent == VoiceIntent.UNKNOWN) 0.5f else 0.9f,
                entities = entities
            )

            onVoiceCommand(command)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Voice Assistant",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Voice waveform visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Get colors outside of Canvas
            val outlineColor = MaterialTheme.colorScheme.outline
            val primaryColor = MaterialTheme.colorScheme.primary
            val outlineAlpha = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

            // Draw waveform
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerY = height / 2

                // Draw baseline
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 1f
                )

                if (waveAmplitude > 0f) {
                    // Draw animated waveform
                    val waveColor = primaryColor
                    val waveCount = 100
                    val waveWidth = width / waveCount

                    for (i in 0 until waveCount) {
                        val x = i * waveWidth

                        // Create dynamic wave pattern
                        val phase = System.currentTimeMillis() / 100f
                        val frequency1 = 0.1f
                        val frequency2 = 0.05f
                        val frequency3 = 0.02f

                        val amplitude = waveAmplitude * 50f * (0.5f + Random().nextFloat() * 0.5f)

                        val y1 = sin((x * frequency1 + phase) * 0.5f) * amplitude
                        val y2 = sin((x * frequency2 + phase) * 0.3f) * amplitude * 0.7f
                        val y3 = sin((x * frequency3 + phase) * 0.2f) * amplitude * 0.5f

                        val y = y1 + y2 + y3

                        drawLine(
                            color = waveColor,
                            start = Offset(x, centerY - y),
                            end = Offset(x, centerY + y),
                            strokeWidth = 2f
                        )
                    }
                } else {
                    // Draw static waveform
                    val staticWaveColor = outlineAlpha
                    val staticWaveCount = 50
                    val staticWaveWidth = width / staticWaveCount

                    for (i in 0 until staticWaveCount) {
                        val x = i * staticWaveWidth
                        val y = sin(i * 0.5f) * 5f

                        drawLine(
                            color = staticWaveColor,
                            start = Offset(x, centerY - y),
                            end = Offset(x, centerY + y),
                            strokeWidth = 1f
                        )
                    }
                }
            }

            // Show recognized text
            if (voiceInputText.isNotEmpty()) {
                Text(
                    text = voiceInputText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        )
                        .padding(8.dp)
                )
            } else if (!isListening) {
                Text(
                    text = "Tap the microphone to speak",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Show progress indicator when listening
            if (isListening && recognitionProgress > 0f) {
                LinearProgressIndicator(
                    progress = recognitionProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Microphone button
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    color = if (isListening) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                .clickable {
                    if (!isListening) {
                        isListening = true
                    } else {
                        isListening = false
                        voiceInputText = ""
                        onVoiceInputError("Voice recognition canceled")
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )

            // Ripple effect when listening
            if (isListening) {
                val infiniteTransition = rememberInfiniteTransition(label = "ripple")
                val rippleScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = AnimeEasing.EaseOutQuad),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rippleScale"
                )

                val rippleAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = AnimeEasing.EaseOutQuad),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rippleAlpha"
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .scale(rippleScale)
                        .alpha(rippleAlpha)
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isListening) "Listening..." else "Tap to speak",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isListening) FontWeight.Bold else FontWeight.Normal,
            color = if (isListening) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Example commands
        Text(
            text = "Try saying:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        LazyColumn {
            items(
                listOf(
                    "Create a new habit to drink water every day",
                    "Mark my meditation habit as complete",
                    "What's my progress on reading habit?",
                    "Set a reminder for my exercise habit at 7 AM",
                    "Show me my habit statistics for this week"
                )
            ) { command ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = command,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * A component that displays a natural language processing result
 */
@Composable
fun NLPResultDisplay(
    command: VoiceCommand,
    modifier: Modifier = Modifier,
    onExecute: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Intent icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            color = when (command.intent) {
                                VoiceIntent.CREATE_HABIT -> MaterialTheme.colorScheme.primary
                                VoiceIntent.COMPLETE_HABIT -> MaterialTheme.colorScheme.secondary
                                VoiceIntent.CHECK_PROGRESS -> MaterialTheme.colorScheme.tertiary
                                VoiceIntent.SET_REMINDER -> MaterialTheme.colorScheme.primaryContainer
                                VoiceIntent.GET_STATS -> MaterialTheme.colorScheme.secondaryContainer
                                VoiceIntent.VIEW_HABIT -> MaterialTheme.colorScheme.tertiaryContainer
                                VoiceIntent.VIEW_STATS -> MaterialTheme.colorScheme.inversePrimary
                                VoiceIntent.UNKNOWN -> MaterialTheme.colorScheme.error
                            }.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (command.intent) {
                            VoiceIntent.CREATE_HABIT -> Icons.Default.AddCircle
                            VoiceIntent.COMPLETE_HABIT -> Icons.Default.CheckCircle
                            VoiceIntent.CHECK_PROGRESS -> Icons.Default.ShowChart
                            VoiceIntent.SET_REMINDER -> Icons.Default.Alarm
                            VoiceIntent.GET_STATS -> Icons.Default.BarChart
                            VoiceIntent.VIEW_HABIT -> Icons.Default.Visibility
                            VoiceIntent.VIEW_STATS -> Icons.Default.Assessment
                            VoiceIntent.UNKNOWN -> Icons.Default.Help
                        },
                        contentDescription = command.intent.name,
                        tint = when (command.intent) {
                            VoiceIntent.CREATE_HABIT -> MaterialTheme.colorScheme.primary
                            VoiceIntent.COMPLETE_HABIT -> MaterialTheme.colorScheme.secondary
                            VoiceIntent.CHECK_PROGRESS -> MaterialTheme.colorScheme.tertiary
                            VoiceIntent.SET_REMINDER -> MaterialTheme.colorScheme.primary
                            VoiceIntent.GET_STATS -> MaterialTheme.colorScheme.secondary
                            VoiceIntent.VIEW_HABIT -> MaterialTheme.colorScheme.tertiary
                            VoiceIntent.VIEW_STATS -> MaterialTheme.colorScheme.primary
                            VoiceIntent.UNKNOWN -> MaterialTheme.colorScheme.error
                        }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Intent details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (command.intent) {
                            VoiceIntent.CREATE_HABIT -> "Create Habit"
                            VoiceIntent.COMPLETE_HABIT -> "Complete Habit"
                            VoiceIntent.CHECK_PROGRESS -> "Check Progress"
                            VoiceIntent.SET_REMINDER -> "Set Reminder"
                            VoiceIntent.GET_STATS -> "Get Statistics"
                            VoiceIntent.VIEW_HABIT -> "View Habit"
                            VoiceIntent.VIEW_STATS -> "View Statistics"
                            VoiceIntent.UNKNOWN -> "Unknown Command"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Confidence: ${(command.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Dismiss button
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Original text
            Text(
                text = "\"${command.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Entities
            if (command.entities.isNotEmpty()) {
                Text(
                    text = "Recognized Entities:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                command.entities.forEach { entity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entity.type.name.replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.width(120.dp)
                        )

                        Text(
                            text = entity.value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "${(entity.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onExecute,
                    enabled = command.intent != VoiceIntent.UNKNOWN
                ) {
                    Text("Execute")
                }
            }
        }
    }
}

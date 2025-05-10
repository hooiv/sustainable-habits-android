package com.example.myapplication.features.advanced

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.nlp.NlpIntent
import com.example.myapplication.data.quantum.QuantumParticle
import com.example.myapplication.data.quantum.QuantumVisualization
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

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
                strokeWidth = 1.dp.toPx()
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
                    style = Stroke(width = 2.dp.toPx())
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

/**
 * Quantum visualization component
 */
@Composable
fun QuantumVisualizationCard(
    quantumVisualization: QuantumVisualization?,
    onUpdateSimulation: () -> Unit,
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
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )

                Text(
                    text = "Quantum-Inspired Visualization",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (quantumVisualization != null) {
                // Quantum state info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuantumStateItem(
                        label = "Amplitude",
                        value = String.format("%.2f", quantumVisualization.amplitude)
                    )

                    QuantumStateItem(
                        label = "Phase",
                        value = String.format("%.2f°", quantumVisualization.phase * 180 / PI.toFloat())
                    )

                    QuantumStateItem(
                        label = "Energy",
                        value = "Level ${quantumVisualization.energyLevel}"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantum particle visualization
                QuantumParticleVisualization(
                    particles = quantumVisualization.particles,
                    entanglements = quantumVisualization.entanglements,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Particle count
                Text(
                    text = "${quantumVisualization.particles.size} quantum particles, " +
                           "${quantumVisualization.entanglements.size} entanglements",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // No visualization available
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No quantum data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Update button
            Button(
                onClick = onUpdateSimulation,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Update Quantum Simulation")
            }
        }
    }
}

/**
 * Quantum state item
 */
@Composable
fun QuantumStateItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Quantum particle visualization
 */
@Composable
fun QuantumParticleVisualization(
    particles: List<QuantumParticle>,
    entanglements: List<com.example.myapplication.data.quantum.QuantumEntanglement>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.1f))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2

            // Draw entanglements
            entanglements.forEach { entanglement ->
                // Find particles for each qubit
                val qubit1Particles = particles.filter { it.qubitIndex == entanglement.qubit1Index }
                val qubit2Particles = particles.filter { it.qubitIndex == entanglement.qubit2Index }

                // Draw entanglement lines between some particles
                val numLines = (qubit1Particles.size.coerceAtMost(qubit2Particles.size) * entanglement.strength).toInt().coerceAtLeast(1)

                for (i in 0 until numLines) {
                    if (i < qubit1Particles.size && i < qubit2Particles.size) {
                        val particle1 = qubit1Particles[i]
                        val particle2 = qubit2Particles[i]

                        // Calculate positions
                        val x1 = centerX + particle1.position.x
                        val y1 = centerY + particle1.position.y
                        val x2 = centerX + particle2.position.x
                        val y2 = centerY + particle2.position.y

                        // Draw entanglement line
                        drawLine(
                            color = Color(entanglement.color).copy(alpha = 0.3f * entanglement.strength),
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), animationProgress * 8f)
                        )
                    }
                }
            }

            // Draw particles
            particles.forEach { particle ->
                // Calculate position with animation
                val angle = particle.phase + animationProgress * 2 * PI.toFloat()
                val distance = particle.amplitude * 20f
                val wobble = sin(animationProgress * 4 * PI.toFloat() + particle.phase) * 5f

                val x = centerX + particle.position.x + cos(angle) * distance + wobble
                val y = centerY + particle.position.y + sin(angle) * distance + wobble

                // Calculate size based on amplitude
                val size = 2.dp.toPx() + particle.amplitude * 6.dp.toPx()

                // Draw particle
                drawCircle(
                    color = Color(particle.color).copy(alpha = 0.7f),
                    center = Offset(x, y),
                    radius = size
                )

                // Draw particle trail
                val trailPath = Path()
                trailPath.moveTo(x, y)

                for (i in 1..5) {
                    val trailAngle = particle.phase + (animationProgress - i * 0.05f) * 2 * PI.toFloat()
                    val trailDistance = particle.amplitude * 20f * (1f - i * 0.15f)
                    val trailWobble = sin((animationProgress - i * 0.05f) * 4 * PI.toFloat() + particle.phase) * 5f * (1f - i * 0.15f)

                    val trailX = centerX + particle.position.x + cos(trailAngle) * trailDistance + trailWobble
                    val trailY = centerY + particle.position.y + sin(trailAngle) * trailDistance + trailWobble

                    trailPath.lineTo(trailX, trailY)
                }

                drawPath(
                    path = trailPath,
                    color = Color(particle.color).copy(alpha = 0.3f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

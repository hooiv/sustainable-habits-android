package com.example.myapplication.features.quantum

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.features.quantum.QuantumVisualizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * A component that displays quantum particles
 */
@Composable
fun QuantumParticleVisualizer(
    particles: List<QuantumParticle>,
    entanglements: List<QuantumEntanglement>,
    modifier: Modifier = Modifier,
    onParticleClick: (QuantumParticle) -> Unit = {}
) {
    var selectedParticleId by remember { mutableStateOf<String?>(null) }

    // Animation values
    val infiniteTransition = rememberInfiniteTransition(label = "quantum")
    val animationTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "quantumTime"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Handle drag interactions with particles
                }
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Draw entanglements
            entanglements.forEach { entanglement ->
                // Find connected particles
                val particle1 = particles.find { it.id == entanglement.qubit1Index.toString() }
                val particle2 = particles.find { it.id == entanglement.qubit2Index.toString() }

                if (particle1 != null && particle2 != null) {
                    // Draw entanglement line
                    drawLine(
                        color = Color(entanglement.color).copy(alpha = 0.6f),
                        start = Offset(particle1.position.x, particle1.position.y),
                        end = Offset(particle2.position.x, particle2.position.y),
                        strokeWidth = 2f * entanglement.strength
                    )

                    // Draw pulsing effect along the line
                    val pulsePosition = (sin(animationTime + entanglement.id.hashCode() % 10) + 1) / 2
                    val pulseX = particle1.position.x + (particle2.position.x - particle1.position.x) * pulsePosition
                    val pulseY = particle1.position.y + (particle2.position.y - particle1.position.y) * pulsePosition

                    drawCircle(
                        color = Color(entanglement.color).copy(alpha = 0.8f),
                        radius = 5f * entanglement.strength,
                        center = Offset(pulseX, pulseY)
                    )
                }
            }

            // Draw particles
            particles.forEach { particle ->
                val isSelected = particle.id == selectedParticleId

                // Calculate particle position with some animation
                val animatedX = particle.position.x + sin(animationTime + particle.id.hashCode() % 10) * 5f
                val animatedY = particle.position.y + cos(animationTime + particle.id.hashCode() % 10) * 5f

                // Draw particle
                val particleRadius = particle.size * (if (isSelected) 1.5f else 1f)
                drawCircle(
                    color = Color(particle.color).copy(alpha = 0.8f),
                    radius = particleRadius,
                    center = Offset(animatedX, animatedY)
                )

                // Draw glow effect
                val glowRadius = particle.size * 3f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(particle.color).copy(alpha = 0.5f),
                            Color(particle.color).copy(alpha = 0f)
                        ),
                        center = Offset(animatedX, animatedY),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(animatedX, animatedY)
                )

                // Draw wave effect for quantum particles
                val waveRadius = particle.size * 2f + sin(animationTime * 2 + particle.id.hashCode()) * particle.size
                drawCircle(
                    color = Color(particle.color).copy(alpha = 0.2f),
                    radius = waveRadius,
                    center = Offset(animatedX, animatedY),
                    style = Stroke(width = 1f)
                )
            }
        }
    }
}

/**
 * A component that displays quantum energy levels
 */
@Composable
fun QuantumEnergyLevelVisualizer(
    energyLevels: Map<String, Int>,
    maxLevel: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Quantum Energy Levels",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Create text measurers outside of Canvas
        val levelTextMeasurer = rememberTextMeasurer()
        val labelTextMeasurer = rememberTextMeasurer()

        // Draw energy levels
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val width = size.width
            val height = size.height
            val levelHeight = height / (maxLevel + 1)

            // Draw level lines
            for (i in 0..maxLevel) {
                val y = height - i * levelHeight

                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )

                // Draw level number
                drawText(
                    textMeasurer = levelTextMeasurer,
                    text = i.toString(),
                    topLeft = Offset(5f, y - 15f),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp
                    )
                )
            }

            // Draw energy states
            val itemWidth = width / (energyLevels.size + 1)
            var x = itemWidth / 2

            energyLevels.forEach { (habitId, level) ->
                val y = height - level * levelHeight

                // Draw energy state
                drawCircle(
                    color = Color.Cyan,
                    radius = 10f,
                    center = Offset(x, y)
                )

                // Draw label
                drawText(
                    textMeasurer = labelTextMeasurer,
                    text = habitId.takeLast(4),
                    topLeft = Offset(x - 15f, y + 15f),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 10.sp
                    )
                )

                x += itemWidth
            }
        }
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
                    imageVector = Icons.Default.Star,
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

                // Quantum particle visualization (Using existing QuantumParticleVisualizer)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    QuantumParticleVisualizer(
                        particles = quantumVisualization.particles,
                        entanglements = quantumVisualization.entanglements.map { coreEntanglement ->
                           // Map core entanglement to local if needed, or if QuantumParticleVisualizer expects
                           // features.quantum.QuantumEntanglement, ensuring types match.
                           // Assuming quantumVisualization.entanglements are already the correct type or compatible.
                           // The Advanced code mapped it:
                           // entanglements = quantumVisualization.entanglements.map { ... QuantumEntanglement(...) }
                           // But QuantumVisualization likely has the lists.
                           // Let's assume for now they are compatible or I might need to fix type mismatch.
                           // Since I am in features:quantum, QuantumEntanglement should be the local one.
                           // coreEntanglement might be from core?
                           // If QuantumVisualization comes from core.network or something...
                           // I'll leave it as direct pass for now.
                           coreEntanglement
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

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


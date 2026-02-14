package com.example.myapplication.features.quantum

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.features.quantum.QuantumEntanglement
import com.example.myapplication.features.quantum.QuantumParticle
import com.example.myapplication.core.ui.components.AppScaffold
import com.example.myapplication.core.ui.theme.Purple40
import com.example.myapplication.core.ui.theme.Purple80
import com.example.myapplication.core.ui.theme.PurpleGrey40
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Quantum Visualization Screen
 * Displays quantum-inspired visualizations of habit data
 */
@Composable
fun QuantumVisualizationScreen(
    navController: NavController,
    habitId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: QuantumVisualizationViewModel = hiltViewModel()
) {
    val particles by viewModel.particles.collectAsState()
    val entanglements by viewModel.entanglements.collectAsState()
    val energyLevels by viewModel.energyLevels.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val optimalSchedule by viewModel.optimalSchedule.collectAsState()
    val isSimulationRunning by viewModel.isSimulationRunning.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Initialize with specific habit if provided
    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.setCurrentHabitId(habitId)
        }
        viewModel.initializeQuantumState()
    }

    // Animation state
    var animationTime by remember { mutableStateOf(0f) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isSimulationRunning) 1f else 0.8f,
        label = "scale"
    )

    // Update animation time
    LaunchedEffect(isSimulationRunning) {
        while (isSimulationRunning) {
            animationTime += 0.01f
            delay(16) // ~60fps
        }
    }

    AppScaffold(
        title = "Quantum Visualization",
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(16.dp)
        ) {
            // Error message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Quantum visualization
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                ) {
                    // Quantum particles visualization
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Draw entanglements
                        entanglements.forEach { entanglement ->
                            drawEntanglement(entanglement, particles, animationTime)
                        }

                        // Draw particles
                        particles.forEach { particle ->
                            drawParticle(particle, animatedScale, animationTime)
                        }
                    }

                    // Controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                if (isSimulationRunning) {
                                    viewModel.stopSimulation()
                                } else {
                                    viewModel.startSimulation()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSimulationRunning)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (isSimulationRunning) "Stop" else "Start")
                        }

                        Button(
                            onClick = { viewModel.resetSimulation() }
                        ) {
                            Text("Reset")
                        }

                        Button(
                            onClick = { viewModel.applyQuantumEffect() }
                        ) {
                            Text("Apply Effect")
                        }
                    }
                }
            }

            // Optimal habit schedule
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Quantum-Optimized Habit Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (optimalSchedule.isEmpty()) {
                        Text(
                            text = "No optimal schedule available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        optimalSchedule.take(5).forEach { (habit, priority) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Priority indicator
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = getPriorityColor(priority),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Habit name
                                Text(
                                    text = habit.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )

                                // Priority value
                                Text(
                                    text = String.format("%.2f", priority),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Energy levels
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Quantum Energy Levels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (energyLevels.isEmpty()) {
                        Text(
                            text = "No energy levels available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Display energy levels for each habit
                        habits.forEach { habit ->
                            val energyLevel = energyLevels[habit.id] ?: 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = habit.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(120.dp)
                                )

                                LinearProgressIndicator(
                                    progress = energyLevel / 5f,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp),
                                    color = getEnergyLevelColor(energyLevel),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Level $energyLevel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draw a quantum particle
 */
private fun DrawScope.drawParticle(
    particle: QuantumParticle,
    scale: Float,
    animationTime: Float
) {
    val center = Offset(size.width / 2, size.height / 2)
    val position = Offset(
        center.x + particle.position.x * scale,
        center.y + particle.position.y * scale
    )

    // Particle size based on amplitude
    val particleSize = 4f + particle.amplitude * 8f

    // Particle color with alpha based on amplitude
    val particleColor = Color(
        android.graphics.Color.red(particle.color) / 255f,
        android.graphics.Color.green(particle.color) / 255f,
        android.graphics.Color.blue(particle.color) / 255f,
        0.3f + particle.amplitude * 0.7f
    )

    // Draw particle with pulsating effect
    val pulseFactor = 1f + 0.2f * kotlin.math.sin(animationTime * 5f + particle.phase)

    drawCircle(
        color = particleColor,
        radius = particleSize * pulseFactor,
        center = position
    )

    // Draw particle trail
    drawLine(
        color = particleColor.copy(alpha = 0.3f),
        start = position,
        end = Offset(
            position.x - particle.velocity.x * 3f,
            position.y - particle.velocity.y * 3f
        ),
        strokeWidth = particleSize / 2f,
        cap = StrokeCap.Round
    )
}

/**
 * Draw a quantum entanglement
 */
private fun DrawScope.drawEntanglement(
    entanglement: QuantumEntanglement,
    particles: List<QuantumParticle>,
    animationTime: Float
) {
    val center = Offset(size.width / 2, size.height / 2)

    // Find particles for the entangled qubits
    val qubit1Particles = particles.filter { it.qubitIndex == entanglement.qubit1Index }
    val qubit2Particles = particles.filter { it.qubitIndex == entanglement.qubit2Index }

    if (qubit1Particles.isEmpty() || qubit2Particles.isEmpty()) return

    // Draw entanglement lines between particles
    for (i in 0 until kotlin.math.min(3, kotlin.math.min(qubit1Particles.size, qubit2Particles.size))) {
        val particle1 = qubit1Particles[i]
        val particle2 = qubit2Particles[i]

        val position1 = Offset(
            center.x + particle1.position.x,
            center.y + particle1.position.y
        )

        val position2 = Offset(
            center.x + particle2.position.x,
            center.y + particle2.position.y
        )

        // Entanglement color with alpha based on strength
        val entanglementColor = Color(
            android.graphics.Color.red(entanglement.color) / 255f,
            android.graphics.Color.green(entanglement.color) / 255f,
            android.graphics.Color.blue(entanglement.color) / 255f,
            0.2f + entanglement.strength * 0.3f
        )

        // Draw entanglement with pulsating effect
        val pulseFactor = 1f + 0.5f * kotlin.math.sin(animationTime * 3f)
        val strokeWidth = 1f + entanglement.strength * 2f * pulseFactor

        drawLine(
            color = entanglementColor,
            start = position1,
            end = position2,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Get color based on priority
 */
private fun getPriorityColor(priority: Double): Color {
    return when {
        priority > 0.8 -> Color(0xFF4CAF50) // Green
        priority > 0.6 -> Color(0xFF8BC34A) // Light Green
        priority > 0.4 -> Color(0xFFFFC107) // Amber
        priority > 0.2 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFFF5722) // Deep Orange
    }
}

/**
 * Get color based on energy level
 */
private fun getEnergyLevelColor(level: Int): Color {
    return when (level) {
        0 -> Color.Gray
        1 -> Purple40
        2 -> PurpleGrey40
        3 -> Color(0xFF3F51B5) // Indigo
        4 -> Color(0xFF2196F3) // Blue
        5 -> Color(0xFF00BCD4) // Cyan
        else -> Purple80
    }
}

package com.example.myapplication.ui.quantum

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.network.quantum.*
import com.example.myapplication.ui.components.LoadingIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Screen for quantum visualization
 */
@Composable
fun QuantumVisualizationScreen(
    viewModel: QuantumVisualizationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val quantumVisualization by viewModel.quantumVisualization.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Animation state
    var animationTime by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "quantum")
    val animationValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "quantum_animation"
    )

    // Update animation time
    LaunchedEffect(animationValue) {
        animationTime = animationValue * 2 * PI.toFloat()
    }

    // Zoom and pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Update quantum simulation
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateQuantumSimulation()
            delay(16) // ~60fps
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quantum Visualization") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetVisualization() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF121212))
        ) {
            // Quantum visualization
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                            offset += pan
                        }
                    }
            ) {
                // Draw quantum particles and entanglements
                quantumVisualization?.let { visualization ->
                    QuantumVisualizationCanvas(
                        visualization = visualization,
                        animationTime = animationTime,
                        scale = scale,
                        offset = offset
                    )
                }
            }

            // Quantum stats
            quantumVisualization?.let { visualization ->
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .width(180.dp)
                        .background(
                            Color(0xFF1E1E1E).copy(alpha = 0.8f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Quantum Stats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuantumStatItem(
                        label = "Amplitude",
                        value = visualization.amplitude,
                        maxValue = 1f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuantumStatItem(
                        label = "Phase",
                        value = visualization.phase / (2 * PI.toFloat()),
                        maxValue = 1f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuantumStatItem(
                        label = "Energy",
                        value = visualization.energyLevel.toFloat(),
                        maxValue = 100f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Particles: ${visualization.particles.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Text(
                        text = "Entanglements: ${visualization.entanglements.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                LoadingIndicator()
            }

            // Error message
            errorMessage?.let { message ->
                if (message.isNotEmpty()) {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        action = {
                            TextButton(onClick = { /* Dismiss */ }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(message)
                    }
                }
            }
        }
    }
}

/**
 * Canvas for quantum visualization
 */
@Composable
fun QuantumVisualizationCanvas(
    visualization: QuantumVisualization,
    animationTime: Float,
    scale: Float,
    offset: Offset
) {
    val particles = visualization.particles
    val entanglements = visualization.entanglements

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val scaleFactor = min(size.width, size.height) / 2 * scale

        // Draw background grid
        drawGrid(center, scaleFactor, animationTime)

        // Draw entanglements
        entanglements.forEach { entanglement ->
            // Find particles
            val particle1 = particles.find { it.id == entanglement.id }
            val particle2 = particles.find { it.id == entanglement.id }

            if (particle1 != null && particle2 != null) {
                val pos1 = center + (particle1.position * scaleFactor) + offset
                val pos2 = center + (particle2.position * scaleFactor) + offset

                // Draw entanglement line
                drawLine(
                    color = Color(entanglement.color).copy(alpha = entanglement.strength),
                    start = pos1,
                    end = pos2,
                    strokeWidth = 2f * entanglement.strength,
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw particles
        particles.forEach { particle ->
            val pos = center + (particle.position * scaleFactor) + offset
            val size = particle.size * scale * (0.8f + 0.2f * sin(animationTime + particle.phase))

            // Draw particle glow
            drawCircle(
                color = Color(particle.color).copy(alpha = 0.3f),
                radius = size * 2,
                center = pos
            )

            // Draw particle
            drawCircle(
                color = Color(particle.color).copy(alpha = particle.amplitude),
                radius = size,
                center = pos
            )

            // Draw phase indicator
            val phaseIndicatorLength = size * 1.5f
            val phaseX = cos(particle.phase) * phaseIndicatorLength
            val phaseY = sin(particle.phase) * phaseIndicatorLength

            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = pos,
                end = pos + Offset(phaseX, phaseY),
                strokeWidth = 1f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Draw background grid
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid(
    center: Offset,
    scaleFactor: Float,
    animationTime: Float
) {
    // Draw circular grid
    for (i in 1..5) {
        val radius = i * scaleFactor / 5
        drawCircle(
            color = Color.White.copy(alpha = 0.1f),
            radius = radius,
            center = center,
            style = Stroke(width = 1f)
        )
    }

    // Draw radial lines
    for (i in 0 until 12) {
        val angle = i * 30f
        val x = cos(Math.toRadians(angle.toDouble())).toFloat() * scaleFactor
        val y = sin(Math.toRadians(angle.toDouble())).toFloat() * scaleFactor

        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = center,
            end = center + Offset(x, y),
            strokeWidth = 1f
        )
    }

    // Draw animated wave
    val wavePath = Path()
    val waveRadius = scaleFactor * 0.8f
    val waveAmplitude = scaleFactor * 0.05f
    val waveFrequency = 8

    for (i in 0..360 step 5) {
        val angle = Math.toRadians(i.toDouble())
        val waveOffset = sin(angle * waveFrequency + animationTime.toDouble()).toFloat() * waveAmplitude
        val radius = waveRadius + waveOffset
        val x = center.x + cos(angle).toFloat() * radius
        val y = center.y + sin(angle).toFloat() * radius

        if (i == 0) {
            wavePath.moveTo(x, y)
        } else {
            wavePath.lineTo(x, y)
        }
    }

    wavePath.close()

    drawPath(
        path = wavePath,
        color = Color(0xFF00FFFF).copy(alpha = 0.2f),
        style = Stroke(width = 2f)
    )
}

/**
 * Quantum stat item
 */
@Composable
fun QuantumStatItem(
    label: String,
    value: Float,
    maxValue: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )

            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value / maxValue).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Color(0xFF00FFFF), RoundedCornerShape(2.dp))
            )
        }
    }
}

/**
 * Scheduled habit for quantum optimization
 */
data class ScheduledHabit(
    val habitId: String,
    val habitName: String,
    val probability: Float,
    val recommendedTime: String
)

package com.example.myapplication.features.quantum

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*

/**
 * Data class representing a quantum state
 */
data class QuantumState(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val probability: Float, // 0.0 to 1.0
    val phase: Float, // 0.0 to 2π
    val entangledStates: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * A component that displays a quantum circuit visualization for habit states
 */
@Composable
fun QuantumCircuitVisualizer(
    quantumStates: List<QuantumState>,
    modifier: Modifier = Modifier,
    onStateClick: (QuantumState) -> Unit = {},
    onCircuitRun: (List<QuantumState>) -> Unit = {}
) {
    var isRunning by remember { mutableStateOf(false) }
    var selectedStateId by remember { mutableStateOf<String?>(null) }
    var circuitProgress by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Text measurer for drawing text
    val textMeasurer = rememberTextMeasurer()

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "quantum")
    val phaseAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Quantum Habit States",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Quantum circuit visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF000020))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // Check if we tapped on a quantum state
                            val stateRadius = 30f
                            val wireSpacing = size.height / (quantumStates.size + 1)

                            for ((index, state) in quantumStates.withIndex()) {
                                val stateY = wireSpacing * (index + 1)

                                // Check gates
                                val gatePositions = listOf(
                                    size.width * 0.25f,
                                    size.width * 0.5f,
                                    size.width * 0.75f
                                )

                                for (gateX in gatePositions) {
                                    val distance = sqrt(
                                        (offset.x - gateX).pow(2) +
                                        (offset.y - stateY).pow(2)
                                    )

                                    if (distance < stateRadius) {
                                        selectedStateId = state.id
                                        onStateClick(state)
                                        break
                                    }
                                }
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                // Draw quantum wires
                val wireSpacing = height / (quantumStates.size + 1)
                val wireColor = Color.Cyan.copy(alpha = 0.5f)

                for ((index, state) in quantumStates.withIndex()) {
                    val wireY = wireSpacing * (index + 1)

                    // Draw wire
                    drawLine(
                        color = wireColor,
                        start = Offset(0f, wireY),
                        end = Offset(width, wireY),
                        strokeWidth = 1f
                    )

                    // Draw state label
                    drawText(
                        textMeasurer = textMeasurer,
                        text = state.name,
                        topLeft = Offset(10f, wireY - 15f),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Draw probability
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${(state.probability * 100).toInt()}%",
                        topLeft = Offset(width - 50f, wireY - 15f),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )

                    // Draw quantum gates
                    val gatePositions = listOf(
                        width * 0.25f,
                        width * 0.5f,
                        width * 0.75f
                    )

                    val gateTypes = listOf(
                        "H", // Hadamard gate
                        "X", // Pauli-X gate
                        "Z"  // Pauli-Z gate
                    )

                    for ((gateIndex, gateX) in gatePositions.withIndex()) {
                        val isSelected = state.id == selectedStateId
                        val gateColor = when (gateIndex) {
                            0 -> Color(0xFF00BCD4)
                            1 -> Color(0xFFFF5722)
                            else -> Color(0xFF4CAF50)
                        }

                        // Draw gate
                        drawRect(
                            color = gateColor.copy(alpha = if (isSelected) 0.8f else 0.5f),
                            topLeft = Offset(gateX - 15f, wireY - 15f),
                            size = Size(30f, 30f)
                        )

                        // Draw gate label
                        drawText(
                            textMeasurer = textMeasurer,
                            text = gateTypes[gateIndex],
                            topLeft = Offset(gateX - 5f, wireY - 8f),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        // Draw animation when circuit is running
                        if (isRunning && circuitProgress > gateIndex / 3f) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.8f),
                                radius = 5f,
                                center = Offset(gateX, wireY)
                            )
                        }
                    }
                }

                // Draw entanglement lines
                for (state in quantumStates) {
                    val stateIndex = quantumStates.indexOfFirst { it.id == state.id }
                    val stateY = wireSpacing * (stateIndex + 1)

                    for (entangledStateId in state.entangledStates) {
                        val entangledIndex = quantumStates.indexOfFirst { it.id == entangledStateId }
                        if (entangledIndex >= 0) {
                            val entangledY = wireSpacing * (entangledIndex + 1)
                            val entanglementX = width * 0.5f

                            // Draw entanglement line
                            drawLine(
                                color = Color.Magenta.copy(alpha = 0.7f),
                                start = Offset(entanglementX, stateY),
                                end = Offset(entanglementX, entangledY),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                            )

                            // Draw entanglement symbol
                            drawCircle(
                                color = Color.Magenta.copy(alpha = 0.7f),
                                radius = 5f,
                                center = Offset(entanglementX, stateY)
                            )

                            drawCircle(
                                color = Color.Magenta.copy(alpha = 0.7f),
                                radius = 5f,
                                center = Offset(entanglementX, entangledY)
                            )
                        }
                    }
                }

                // Draw measurement at the end
                if (isRunning && circuitProgress > 0.9f) {
                    for ((index, state) in quantumStates.withIndex()) {
                        val wireY = wireSpacing * (index + 1)
                        val measureX = width - 30f

                        // Draw measurement symbol
                        drawRect(
                            color = Color.Yellow.copy(alpha = 0.7f),
                            topLeft = Offset(measureX - 15f, wireY - 15f),
                            size = Size(30f, 30f)
                        )

                        drawLine(
                            color = Color.Yellow,
                            start = Offset(measureX - 10f, wireY - 10f),
                            end = Offset(measureX + 10f, wireY + 10f),
                            strokeWidth = 2f
                        )

                        drawLine(
                            color = Color.Yellow,
                            start = Offset(measureX - 10f, wireY + 10f),
                            end = Offset(measureX + 10f, wireY - 10f),
                            strokeWidth = 2f
                        )
                    }
                }
            }

            // Progress indicator when running
            if (isRunning) {
                LinearProgressIndicator(
                    progress = circuitProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter),
                    color = Color.Cyan,
                    trackColor = Color.Transparent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bloch sphere visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF000020))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerX = width / 2
                val centerY = height / 2
                val radius = min(centerX, centerY) * 0.8f

                // Draw Bloch sphere
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.2f),
                    radius = radius,
                    center = Offset(centerX, centerY)
                )

                // Draw X, Y, Z axes
                drawLine(
                    color = Color.Red.copy(alpha = 0.7f),
                    start = Offset(centerX - radius, centerY),
                    end = Offset(centerX + radius, centerY),
                    strokeWidth = 1f
                )

                drawLine(
                    color = Color.Green.copy(alpha = 0.7f),
                    start = Offset(centerX, centerY - radius),
                    end = Offset(centerX, centerY + radius),
                    strokeWidth = 1f
                )

                // Z-axis with perspective
                drawLine(
                    color = Color.Blue.copy(alpha = 0.7f),
                    start = Offset(centerX - radius * 0.3f, centerY - radius * 0.3f),
                    end = Offset(centerX + radius * 0.3f, centerY + radius * 0.3f),
                    strokeWidth = 1f
                )

                // Draw axis labels
                drawText(
                    textMeasurer = textMeasurer,
                    text = "X",
                    topLeft = Offset(centerX + radius + 5f, centerY),
                    style = TextStyle(
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = "Y",
                    topLeft = Offset(centerX, centerY - radius - 20f),
                    style = TextStyle(
                        color = Color.Green,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = "Z",
                    topLeft = Offset(centerX + radius * 0.3f + 5f, centerY + radius * 0.3f + 5f),
                    style = TextStyle(
                        color = Color.Blue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                // Draw quantum states on the Bloch sphere
                for (state in quantumStates) {
                    val isSelected = state.id == selectedStateId

                    // Calculate position on Bloch sphere
                    val theta = state.probability * PI.toFloat() // 0 to π
                    val phi = state.phase + phaseAnimation // 0 to 2π

                    val x = radius * sin(theta) * cos(phi)
                    val y = radius * sin(theta) * sin(phi)
                    val z = radius * cos(theta)

                    // Apply perspective projection
                    val projectedX = centerX + x
                    val projectedY = centerY - y - z * 0.3f

                    // Draw state
                    drawCircle(
                        color = when {
                            isSelected -> Color.Yellow
                            state.entangledStates.isNotEmpty() -> Color.Magenta
                            else -> Color.Cyan
                        },
                        radius = if (isSelected) 8f else 6f,
                        center = Offset(projectedX, projectedY)
                    )

                    // Draw state vector
                    drawLine(
                        color = when {
                            isSelected -> Color.Yellow.copy(alpha = 0.7f)
                            state.entangledStates.isNotEmpty() -> Color.Magenta.copy(alpha = 0.7f)
                            else -> Color.Cyan.copy(alpha = 0.7f)
                        },
                        start = Offset(centerX, centerY),
                        end = Offset(projectedX, projectedY),
                        strokeWidth = if (isSelected) 2f else 1f
                    )

                    // Draw state label
                    if (isSelected) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = state.name,
                            topLeft = Offset(projectedX + 10f, projectedY - 10f),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Text(
                text = "Bloch Sphere Representation",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    selectedStateId = null
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Reset Selection")
            }

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        circuitProgress = 0f

                        coroutineScope.launch {
                            // Simulate circuit execution
                            while (circuitProgress < 1f) {
                                delay(50)
                                circuitProgress += 0.02f
                            }

                            // Complete execution
                            delay(500)
                            isRunning = false

                            // Generate results
                            val results = quantumStates.map { state ->
                                // Apply quantum transformations
                                val newProbability = when {
                                    state.entangledStates.isNotEmpty() -> {
                                        // Entangled states have correlated probabilities
                                        val entangledState = quantumStates.find { it.id == state.entangledStates.first() }
                                        if (entangledState != null) {
                                            1f - entangledState.probability
                                        } else {
                                            state.probability
                                        }
                                    }
                                    else -> {
                                        // Apply quantum gate effects
                                        val hadamardEffect = sin(state.probability * PI.toFloat()).pow(2)
                                        val pauliXEffect = 1f - state.probability
                                        val pauliZEffect = cos(state.phase).pow(2)

                                        (hadamardEffect + pauliXEffect + pauliZEffect) / 3f
                                    }
                                }

                                state.copy(probability = newProbability)
                            }

                            onCircuitRun(results)
                        }
                    }
                },
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isRunning) "Running..." else "Run Circuit")
            }
        }
    }
}

/**
 * A component that displays a quantum superposition of habit states
 */
@Composable
fun QuantumSuperpositionVisualizer(
    habits: List<Habit>,
    modifier: Modifier = Modifier,
    onHabitClick: (Habit) -> Unit = {}
) {
    // Text measurer for drawing text
    val textMeasurer = rememberTextMeasurer()

    // Convert habits to quantum states
    val quantumStates = remember(habits) {
        habits.map { habit ->
            // Calculate probability based on habit completion rate
            val probability = if (habit.goal > 0) {
                habit.goalProgress.toFloat() / habit.goal
            } else {
                0.5f
            }

            // Calculate phase based on streak
            val phase = (habit.streak % 100) / 100f * 2 * PI.toFloat()

            // Determine entangled states
            val entangledStates = habits
                .filter { other ->
                    other.id != habit.id &&
                    other.category == habit.category &&
                    other.frequency == habit.frequency
                }
                .take(1)
                .map { it.id }

            QuantumState(
                id = habit.id,
                name = habit.name,
                probability = probability,
                phase = phase,
                entangledStates = entangledStates
            )
        }
    }

    QuantumCircuitVisualizer(
        quantumStates = quantumStates,
        modifier = modifier,
        onStateClick = { state ->
            // Find the corresponding habit and trigger the click handler
            habits.find { it.id == state.id }?.let { habit ->
                onHabitClick(habit)
            }
        },
        onCircuitRun = { results ->
            // Process quantum circuit results
            // In a real app, this would update the habits based on the quantum simulation
        }
    )
}

/**
 * A component that displays quantum interference patterns for habit interactions
 */
@Composable
fun QuantumInterferencePattern(
    modifier: Modifier = Modifier,
    waveCount: Int = 2,
    amplitude: Float = 1f,
    frequency: Float = 10f,
    phase: Float = 0f
) {
    // Text measurer for drawing text
    val textMeasurer = rememberTextMeasurer()

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val animatedPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF000020))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
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
                strokeWidth = 1f
            )

            // Draw individual waves
            val waveColors = listOf(
                Color(0xFF4CAF50),
                Color(0xFF2196F3),
                Color(0xFFFF9800),
                Color(0xFFE91E63)
            )

            for (i in 0 until waveCount) {
                val wavePhase = phase + animatedPhase + (i * PI.toFloat() / waveCount)
                val waveAmplitude = amplitude * 50f
                val waveFrequency = frequency * (1f + i * 0.5f)
                val waveColor = waveColors[i % waveColors.size]

                val path = Path()
                var firstPoint = true

                for (x in 0 until width.toInt() step 2) {
                    val xFloat = x.toFloat()
                    val y = centerY - waveAmplitude * sin(xFloat * waveFrequency / width + wavePhase)

                    if (firstPoint) {
                        path.moveTo(xFloat, y)
                        firstPoint = false
                    } else {
                        path.lineTo(xFloat, y)
                    }
                }

                drawPath(
                    path = path,
                    color = waveColor.copy(alpha = 0.7f),
                    style = Stroke(width = 2f)
                )
            }

            // Draw interference pattern
            val interferencePath = Path()
            var firstPoint = true

            for (x in 0 until width.toInt() step 2) {
                val xFloat = x.toFloat()
                var ySum = 0f

                for (i in 0 until waveCount) {
                    val wavePhase = phase + animatedPhase + (i * PI.toFloat() / waveCount)
                    val waveAmplitude = amplitude * 50f
                    val waveFrequency = frequency * (1f + i * 0.5f)

                    ySum += waveAmplitude * sin(xFloat * waveFrequency / width + wavePhase)
                }

                val y = centerY - ySum / waveCount

                if (firstPoint) {
                    interferencePath.moveTo(xFloat, y)
                    firstPoint = false
                } else {
                    interferencePath.lineTo(xFloat, y)
                }
            }

            drawPath(
                path = interferencePath,
                color = Color.White,
                style = Stroke(width = 3f)
            )
        }

        Text(
            text = "Quantum Interference Pattern",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
    }
}

package com.example.myapplication.core.network.quantum

import android.graphics.Color
import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*
import kotlin.random.Random

/**
 * Implements quantum-inspired visualizations for habit data
 */
@Singleton
class QuantumVisualizer @Inject constructor() {
    companion object {
        private const val TAG = "QuantumVisualizer"

        // Quantum simulation parameters
        private const val NUM_QUBITS = 8
        private const val NUM_PARTICLES = 100
        private const val ENTANGLEMENT_THRESHOLD = 0.7f
        private const val SUPERPOSITION_FACTOR = 0.5f
        private const val INTERFERENCE_FACTOR = 0.3f

        // Visualization parameters
        private const val VISUALIZATION_SCALE = 100f
        private const val TIME_STEP = 0.05f
        private const val MAX_ENERGY_LEVEL = 5
    }

    // Quantum state
    private val _quantumState = MutableStateFlow<QuantumState?>(null)
    val quantumState: StateFlow<QuantumState?> = _quantumState.asStateFlow()

    // Particle system
    private val _particles = MutableStateFlow<List<QuantumParticle>>(emptyList())
    val particles: StateFlow<List<QuantumParticle>> = _particles.asStateFlow()

    // Entanglement connections
    private val _entanglements = MutableStateFlow<List<QuantumEntanglement>>(emptyList())
    val entanglements: StateFlow<List<QuantumEntanglement>> = _entanglements.asStateFlow()

    // Energy levels
    private val _energyLevels = MutableStateFlow<Map<String, Int>>(emptyMap())
    val energyLevels: StateFlow<Map<String, Int>> = _energyLevels.asStateFlow()

    // Simulation time
    private var simulationTime = 0f

    // Random number generator
    private val random = java.util.Random()

    /**
     * Initialize quantum state from habits
     */
    fun initializeQuantumState(habits: List<Habit>, completions: Map<String, List<HabitCompletion>>) {
        // Create quantum state
        val qubits = Array(NUM_QUBITS) { ComplexNumber(0.0, 0.0) }

        // Initialize qubits based on habits
        habits.forEachIndexed { index, habit ->
            if (index < NUM_QUBITS) {
                // Calculate amplitude based on habit streak and completion rate
                val streak = habit.streak.toDouble()
                val completionRate = calculateCompletionRate(habit, completions[habit.id] ?: emptyList())

                // Convert to quantum amplitude (normalized between 0 and 1)
                val amplitude = sqrt(completionRate)
                val phase = streak * PI / 10.0 // Phase based on streak

                // Set qubit state using amplitude and phase
                qubits[index] = ComplexNumber(
                    amplitude * cos(phase),
                    amplitude * sin(phase)
                )
            }
        }

        // Normalize quantum state
        normalizeQubits(qubits)

        // Create quantum state
        val state = QuantumState(
            qubits = qubits,
            habitIds = habits.take(NUM_QUBITS).map { it.id }
        )

        _quantumState.value = state

        // Initialize particles
        initializeParticles(state)

        // Initialize entanglements
        initializeEntanglements(habits, completions)

        // Initialize energy levels
        initializeEnergyLevels(habits)

        Log.d(TAG, "Initialized quantum state with ${habits.size} habits")
    }

    /**
     * Initialize quantum particles
     */
    private fun initializeParticles(state: QuantumState) {
        val particles = mutableListOf<QuantumParticle>()

        // Create particles for each qubit
        for (i in 0 until NUM_QUBITS) {
            val qubit = state.qubits[i]
            val habitId = if (i < state.habitIds.size) state.habitIds[i] else null

            // Calculate number of particles based on qubit amplitude
            val amplitude = qubit.magnitude()
            val numParticles = (amplitude * amplitude * NUM_PARTICLES / NUM_QUBITS).toInt()

            // Create particles
            for (j in 0 until numParticles) {
                // Calculate position based on qubit state
                val angle = random.nextDouble() * 2 * PI
                val radius = random.nextDouble() * VISUALIZATION_SCALE * amplitude

                val position = Offset(
                    (cos(angle) * radius).toFloat(),
                    (sin(angle) * radius).toFloat()
                )

                // Calculate velocity based on qubit phase
                val phase = qubit.phase()
                val speed = (1.0 + random.nextDouble()) * 10.0

                val velocity = Offset(
                    (cos(phase + angle) * speed).toFloat(),
                    (sin(phase + angle) * speed).toFloat()
                )

                // Create particle
                val particle = QuantumParticle(
                    id = UUID.randomUUID().toString(),
                    position = position,
                    velocity = velocity,
                    amplitude = amplitude.toFloat(),
                    phase = phase.toFloat(),
                    qubitIndex = i,
                    habitId = habitId,
                    color = generateColorForQubit(i)
                )

                particles.add(particle)
            }
        }

        _particles.value = particles
    }

    /**
     * Initialize quantum entanglements
     */
    private fun initializeEntanglements(
        habits: List<Habit>,
        completions: Map<String, List<HabitCompletion>>
    ) {
        val entanglements = mutableListOf<QuantumEntanglement>()
        val state = _quantumState.value ?: return

        // Create entanglements between related habits
        for (i in 0 until min(habits.size, NUM_QUBITS)) {
            for (j in i + 1 until min(habits.size, NUM_QUBITS)) {
                val habit1 = habits[i]
                val habit2 = habits[j]

                // Calculate correlation between habits
                val correlation = calculateHabitCorrelation(
                    habit1, habit2,
                    completions[habit1.id] ?: emptyList(),
                    completions[habit2.id] ?: emptyList()
                )

                // Create entanglement if correlation is above threshold
                if (correlation > ENTANGLEMENT_THRESHOLD) {
                    val entanglement = QuantumEntanglement(
                        id = UUID.randomUUID().toString(),
                        qubit1Index = i,
                        qubit2Index = j,
                        habit1Id = habit1.id,
                        habit2Id = habit2.id,
                        strength = correlation,
                        color = blendColors(
                            generateColorForQubit(i),
                            generateColorForQubit(j)
                        )
                    )

                    entanglements.add(entanglement)
                }
            }
        }

        _entanglements.value = entanglements
    }

    /**
     * Initialize energy levels
     */
    private fun initializeEnergyLevels(habits: List<Habit>) {
        val energyLevels = mutableMapOf<String, Int>()

        // Calculate energy level for each habit
        for (habit in habits) {
            // Energy level based on streak and difficulty
            val energyLevel = min(
                (habit.streak / 5 + habit.difficulty.ordinal / 2).toInt(),
                MAX_ENERGY_LEVEL
            )

            energyLevels[habit.id] = energyLevel
        }

        _energyLevels.value = energyLevels
    }

    /**
     * Update quantum simulation
     */
    fun updateSimulation() {
        val state = _quantumState.value ?: return
        val currentParticles = _particles.value

        // Update simulation time
        simulationTime += TIME_STEP

        // Apply quantum gates
        applyHadamardGate(0) // Apply Hadamard gate to first qubit
        applyPhaseGate(1, simulationTime) // Apply phase gate to second qubit

        // Apply entanglement effects
        applyEntanglementEffects()

        // Update particles
        val updatedParticles = currentParticles.map { particle ->
            // Update position
            val newPosition = Offset(
                particle.position.x + particle.velocity.x * TIME_STEP,
                particle.position.y + particle.velocity.y * TIME_STEP
            )

            // Apply quantum effects
            val qubitIndex = particle.qubitIndex
            val qubit = state.qubits[qubitIndex]

            // Update amplitude and phase based on qubit state
            val newAmplitude = (particle.amplitude + (qubit.magnitude().toFloat() - particle.amplitude) * 0.1f)
                .coerceIn(0.1f, 1.0f)

            val newPhase = particle.phase + 0.1f * sin(simulationTime)

            // Apply superposition effect
            val superpositionEffect = sin(simulationTime * 2) * SUPERPOSITION_FACTOR

            // Apply interference effect
            val interferenceEffect = cos(particle.position.x / 20 + simulationTime) * INTERFERENCE_FACTOR

            // Calculate new velocity
            val speed = particle.velocity.x * particle.velocity.x + particle.velocity.y * particle.velocity.y
            val direction = atan2(particle.velocity.y, particle.velocity.x) +
                            superpositionEffect + interferenceEffect

            val newVelocity = Offset(
                (sqrt(speed) * cos(direction)).toFloat(),
                (sqrt(speed) * sin(direction)).toFloat()
            )

            // Create updated particle
            particle.copy(
                position = newPosition,
                velocity = newVelocity,
                amplitude = newAmplitude,
                phase = newPhase
            )
        }

        _particles.value = updatedParticles
    }

    /**
     * Apply Hadamard gate to a qubit
     */
    private fun applyHadamardGate(qubitIndex: Int) {
        val state = _quantumState.value ?: return
        if (qubitIndex >= state.qubits.size) return

        val qubit = state.qubits[qubitIndex]

        // Hadamard transform: |0⟩ -> (|0⟩ + |1⟩)/√2, |1⟩ -> (|0⟩ - |1⟩)/√2
        val newQubit = ComplexNumber(
            (qubit.real + qubit.imaginary) / sqrt(2.0),
            (qubit.real - qubit.imaginary) / sqrt(2.0)
        )

        // Update qubit
        state.qubits[qubitIndex] = newQubit

        // Normalize quantum state
        normalizeQubits(state.qubits)
    }

    /**
     * Apply phase gate to a qubit
     */
    private fun applyPhaseGate(qubitIndex: Int, phase: Float) {
        val state = _quantumState.value ?: return
        if (qubitIndex >= state.qubits.size) return

        val qubit = state.qubits[qubitIndex]

        // Phase gate: |0⟩ -> |0⟩, |1⟩ -> e^(iθ)|1⟩
        val newQubit = ComplexNumber(
            qubit.real * cos(phase.toDouble()) - qubit.imaginary * sin(phase.toDouble()),
            qubit.real * sin(phase.toDouble()) + qubit.imaginary * cos(phase.toDouble())
        )

        // Update qubit
        state.qubits[qubitIndex] = newQubit

        // Normalize quantum state
        normalizeQubits(state.qubits)
    }

    /**
     * Apply entanglement effects
     */
    private fun applyEntanglementEffects() {
        val state = _quantumState.value ?: return
        val entanglements = _entanglements.value

        for (entanglement in entanglements) {
            val qubit1Index = entanglement.qubit1Index
            val qubit2Index = entanglement.qubit2Index

            if (qubit1Index < state.qubits.size && qubit2Index < state.qubits.size) {
                val qubit1 = state.qubits[qubit1Index]
                val qubit2 = state.qubits[qubit2Index]

                // Apply CNOT gate (simplified)
                if (qubit1.magnitude() > 0.5) {
                    // Flip qubit2 if qubit1 is in |1⟩ state
                    state.qubits[qubit2Index] = ComplexNumber(qubit2.imaginary, qubit2.real)
                }
            }
        }

        // Normalize quantum state
        normalizeQubits(state.qubits)
    }

    /**
     * Normalize qubits to ensure valid quantum state
     */
    private fun normalizeQubits(qubits: Array<ComplexNumber>) {
        // Calculate total probability
        var totalProbability = 0.0
        for (qubit in qubits) {
            val magnitude = qubit.magnitude()
            totalProbability += magnitude * magnitude
        }

        // Normalize if needed
        if (totalProbability > 0) {
            val normalizationFactor = 1.0 / sqrt(totalProbability)
            for (i in qubits.indices) {
                qubits[i] = ComplexNumber(
                    qubits[i].real * normalizationFactor,
                    qubits[i].imaginary * normalizationFactor
                )
            }
        }
    }

    /**
     * Calculate completion rate for a habit
     */
    private fun calculateCompletionRate(habit: Habit, completions: List<HabitCompletion>): Double {
        if (completions.isEmpty()) return 0.0

        // Calculate days since habit creation
        val now = System.currentTimeMillis()
        val creationDate = habit.createdDate.time
        val daysSinceCreation = (now - creationDate) / (1000.0 * 60 * 60 * 24)

        if (daysSinceCreation < 1.0) return 0.0

        // Calculate expected completions based on frequency
        val expectedCompletions = when (habit.frequency) {
            com.example.myapplication.core.data.model.HabitFrequency.DAILY -> daysSinceCreation
            com.example.myapplication.core.data.model.HabitFrequency.WEEKLY -> daysSinceCreation / 7
            com.example.myapplication.core.data.model.HabitFrequency.MONTHLY -> daysSinceCreation / 30
            else -> daysSinceCreation
        }

        // Calculate completion rate
        return if (expectedCompletions > 0) {
            (completions.size.toDouble() / expectedCompletions).coerceIn(0.0, 1.0)
        } else {
            0.0
        }
    }

    /**
     * Apply quantum effect to habit data
     * This simulates how quantum computing could optimize habit scheduling
     */
    fun applyQuantumEffect(habits: List<Habit>, completions: Map<String, List<HabitCompletion>>): Map<String, Double> {
        val state = _quantumState.value ?: return emptyMap()
        val results = mutableMapOf<String, Double>()

        // Apply quantum algorithm (simulated)
        for (i in 0 until min(habits.size, NUM_QUBITS)) {
            val habitId = state.habitIds.getOrNull(i) ?: continue
            val habit = habits.find { it.id == habitId } ?: continue

            // Get qubit state
            val qubit = state.qubits[i]
            val amplitude = qubit.magnitude()
            val phase = qubit.phase()

            // Calculate completion rate
            val completionRate = calculateCompletionRate(habit, completions[habitId] ?: emptyList())

            // Calculate quantum-enhanced success probability
            // This simulates how a quantum algorithm might optimize habit scheduling
            val enhancedProbability = (completionRate + amplitude * 0.3 + sin(phase) * 0.1)
                .coerceIn(0.0, 1.0)

            results[habitId] = enhancedProbability
        }

        return results
    }

    /**
     * Get optimal habit schedule based on quantum state
     * This simulates how quantum computing could find optimal habit combinations
     */
    fun getOptimalHabitSchedule(habits: List<Habit>): List<Pair<Habit, Double>> {
        val state = _quantumState.value ?: return emptyList()
        val entanglements = _entanglements.value
        val results = mutableListOf<Pair<Habit, Double>>()

        // Apply quantum scheduling algorithm (simulated)
        for (habit in habits) {
            val habitIndex = state.habitIds.indexOf(habit.id)
            if (habitIndex < 0) continue

            // Get qubit state
            val qubit = state.qubits[habitIndex]
            val amplitude = qubit.magnitude()

            // Calculate base priority
            var priority = amplitude

            // Adjust priority based on entanglements
            for (entanglement in entanglements) {
                if (entanglement.habit1Id == habit.id || entanglement.habit2Id == habit.id) {
                    // Increase priority for strongly entangled habits
                    priority += entanglement.strength * 0.2
                }
            }

            // Add to results
            results.add(Pair(habit, priority))
        }

        // Sort by priority (descending)
        return results.sortedByDescending { it.second }
    }

    /**
     * Calculate correlation between two habits
     */
    private fun calculateHabitCorrelation(
        habit1: Habit,
        habit2: Habit,
        completions1: List<HabitCompletion>,
        completions2: List<HabitCompletion>
    ): Float {
        // Simple correlation based on completion dates
        if (completions1.isEmpty() || completions2.isEmpty()) return 0f

        // Get completion dates
        val dates1 = completions1.map { it.completionDate }.toList()
        val dates2 = completions2.map { it.completionDate }.toList()

        // Count completions on the same day
        var sameDay = 0
        var i = 0
        var j = 0

        while (i < dates1.size && j < dates2.size) {
            val day1 = dates1[i] / (1000 * 60 * 60 * 24)
            val day2 = dates2[j] / (1000 * 60 * 60 * 24)

            when {
                day1 == day2 -> {
                    sameDay++
                    i++
                    j++
                }
                day1 < day2 -> i++
                else -> j++
            }
        }

        // Calculate correlation
        val totalCompletions = dates1.size + dates2.size
        return if (totalCompletions > 0) {
            (2 * sameDay.toFloat() / totalCompletions).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    /**
     * Predict habit success probability
     */
    fun predictHabitSuccess(habit: Habit, completions: List<HabitCompletion>): Double {
        val state = _quantumState.value ?: return 0.0
        val habitIndex = state.habitIds.indexOf(habit.id)
        if (habitIndex < 0) return 0.0

        // Get qubit state
        val qubit = state.qubits[habitIndex]
        val amplitude = qubit.magnitude()

        // Calculate completion rate
        val completionRate = calculateCompletionRate(habit, completions)

        // Calculate streak factor (higher streak = higher probability)
        val streakFactor = min(habit.streak / 10.0, 1.0)

        // Calculate quantum-enhanced success probability
        return (0.3 * completionRate + 0.4 * amplitude + 0.3 * streakFactor).coerceIn(0.0, 1.0)
    }

    /**
     * Update quantum visualization
     */
    fun updateQuantumVisualization() {
        val state = _quantumState.value ?: return

        // Update particles
        val updatedParticles = _particles.value.map { particle ->
            // Apply quantum effects to particle position
            val position = particle.position
            val phase = particle.phase

            // Calculate new position with quantum effects
            val newX = position.x + sin(phase + TIME_STEP) * 0.05f
            val newY = position.y + cos(phase + TIME_STEP * 2) * 0.05f

            // Create updated particle
            particle.copy(
                position = Offset(newX, newY),
                phase = (phase + TIME_STEP) % (2 * PI.toFloat())
            )
        }

        _particles.value = updatedParticles

        // Update entanglements
        val updatedEntanglements = _entanglements.value.map { entanglement ->
            // Fluctuate entanglement strength
            val newStrength = (entanglement.strength + sin(TIME_STEP * 3) * 0.1f).coerceIn(0.1f, 1.0f)

            entanglement.copy(strength = newStrength)
        }

        _entanglements.value = updatedEntanglements

        // Update energy levels
        val updatedEnergyLevels = _energyLevels.value.toMutableMap()
        state.habitIds.forEachIndexed { index, habitId ->
            val qubit = state.qubits[index]
            val amplitude = qubit.magnitude()
            val phase = qubit.phase()

            // Calculate energy level based on amplitude and phase
            val energyLevel = (amplitude * 100 + sin(phase) * 20).toInt().coerceIn(0, 100)
            updatedEnergyLevels[habitId] = energyLevel
        }
        _energyLevels.value = updatedEnergyLevels

        // Trigger quantum fluctuations periodically
        if (random.nextFloat() < 0.05f) { // 5% chance each update
            triggerQuantumFluctuation()
        }
    }

    /**
     * Normalize a list of qubits
     */
    private fun normalizeQubits(qubits: MutableList<ComplexNumber>) {
        // Calculate total probability
        var totalProbability = 0.0
        for (qubit in qubits) {
            totalProbability += qubit.magnitudeSquared()
        }

        // Normalize if total probability is not 1
        if (abs(totalProbability - 1.0) > 0.000001) {
            val normalizationFactor = 1.0 / sqrt(totalProbability)
            for (i in qubits.indices) {
                qubits[i] = ComplexNumber(
                    qubits[i].real * normalizationFactor,
                    qubits[i].imaginary * normalizationFactor
                )
            }
        }
    }

    /**
     * Trigger a quantum fluctuation
     */
    private fun triggerQuantumFluctuation() {
        val state = _quantumState.value ?: return

        // Choose a random qubit to fluctuate
        val qubitIndex = random.nextInt(state.qubits.size)
        val qubit = state.qubits[qubitIndex]

        // Apply a small random rotation to the qubit
        val angle = random.nextFloat() * PI.toFloat() * 0.1f
        val rotatedQubit = ComplexNumber(
            qubit.real * cos(angle) - qubit.imaginary * sin(angle),
            qubit.real * sin(angle) + qubit.imaginary * cos(angle)
        )

        // Update the quantum state
        val updatedQubits = state.qubits.toMutableList()
        updatedQubits[qubitIndex] = rotatedQubit

        // Normalize the state
        normalizeQubits(updatedQubits)

        // Update the state
        _quantumState.value = state.copy(qubits = updatedQubits)

        // Log the fluctuation
        Log.d(TAG, "Quantum fluctuation applied to qubit $qubitIndex")
    }

    /**
     * Create quantum visualization for all habits
     */
    fun createQuantumVisualization(): QuantumVisualization {
        val state = _quantumState.value

        if (state == null) {
            // Return empty visualization
            return QuantumVisualization(
                habitId = "",
                amplitude = 0f,
                phase = 0f,
                particles = emptyList(),
                entanglements = emptyList(),
                energyLevel = 0
            )
        }

        // Get all particles
        val allParticles = _particles.value

        // Get all entanglements
        val allEntanglements = _entanglements.value

        // Calculate average amplitude
        val avgAmplitude = state.qubits.map { it.magnitude() }.average().toFloat()

        // Calculate average phase
        val avgPhase = state.qubits.map { it.phase() }.average().toFloat()

        // Create visualization
        return QuantumVisualization(
            habitId = "all",
            amplitude = avgAmplitude,
            phase = avgPhase,
            particles = allParticles,
            entanglements = allEntanglements,
            energyLevel = _energyLevels.value.values.average().toInt()
        )
    }

    /**
     * Generate color for a qubit
     */
    private fun generateColorForQubit(index: Int): Int {
        // Generate colors based on qubit index
        val hue = (index * 360f / NUM_QUBITS) % 360f
        return Color.HSVToColor(floatArrayOf(hue, 0.8f, 0.9f))
    }

    /**
     * Blend two colors
     */
    private fun blendColors(color1: Int, color2: Int): Int {
        val r = (Color.red(color1) + Color.red(color2)) / 2
        val g = (Color.green(color1) + Color.green(color2)) / 2
        val b = (Color.blue(color1) + Color.blue(color2)) / 2
        return Color.rgb(r, g, b)
    }

    /**
     * Get quantum visualization data for a habit
     */
    fun getQuantumVisualizationForHabit(habitId: String): QuantumVisualization? {
        val state = _quantumState.value ?: return null
        val habitIndex = state.habitIds.indexOf(habitId)
        if (habitIndex < 0) return null

        // Get qubit state
        val qubit = state.qubits[habitIndex]

        // Get particles for this habit
        val habitParticles = _particles.value.filter { it.habitId == habitId }

        // Get entanglements for this habit
        val habitEntanglements = _entanglements.value.filter {
            it.habit1Id == habitId || it.habit2Id == habitId
        }

        // Get energy level
        val energyLevel = _energyLevels.value[habitId] ?: 0

        return QuantumVisualization(
            habitId = habitId,
            amplitude = qubit.magnitude().toFloat(),
            phase = qubit.phase().toFloat(),
            particles = habitParticles,
            entanglements = habitEntanglements,
            energyLevel = energyLevel
        )
    }
}

/**
 * Complex number for quantum state representation
 */
data class ComplexNumber(
    val real: Double,
    val imaginary: Double
) {
    fun magnitude(): Double = sqrt(real * real + imaginary * imaginary)

    fun magnitudeSquared(): Double = real * real + imaginary * imaginary

    fun phase(): Double = atan2(imaginary, real)
}

/**
 * Quantum state
 */
data class QuantumState(
    val qubits: MutableList<ComplexNumber>,
    val habitIds: List<String>
) {
    constructor(qubits: Array<ComplexNumber>, habitIds: List<String>) : this(qubits.toMutableList(), habitIds)
}

/**
 * Quantum particle for visualization
 */
data class QuantumParticle(
    val id: String,
    val position: Offset,
    val velocity: Offset,
    val amplitude: Float,
    val phase: Float,
    val qubitIndex: Int,
    val habitId: String?,
    val color: Int,
    val size: Float = 5f
)

/**
 * Quantum entanglement between qubits
 */
data class QuantumEntanglement(
    val id: String,
    val qubit1Index: Int,
    val qubit2Index: Int,
    val habit1Id: String,
    val habit2Id: String,
    val strength: Float,
    val color: Int
)

/**
 * Quantum visualization for a habit
 */
data class QuantumVisualization(
    val habitId: String,
    val amplitude: Float,
    val phase: Float,
    val particles: List<QuantumParticle>,
    val entanglements: List<QuantumEntanglement>,
    val energyLevel: Int
)

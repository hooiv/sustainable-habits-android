package com.example.myapplication.features.habits

import android.util.Log // Import Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.model.Habit
import com.example.myapplication.navigation.NavRoutes
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    navController: NavController,
    viewModel: HabitViewModel = hiltViewModel() // Inject ViewModel
) {
    val habits by viewModel.habits.collectAsState() // Collect habits as state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sustainable Habits") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = { 
            FloatingActionButton(onClick = {
                Log.d("HabitListScreen", "FAB clicked, navigating to ADD_HABIT route: ${NavRoutes.ADD_HABIT}")
                try {
                    // Use navigate(route) instead of navigate(route_string) to avoid deep link interpretation
                    navController.navigate(NavRoutes.ADD_HABIT) {
                        // Add navigation options to ensure proper navigation behavior
                        launchSingleTop = true
                    }
                    Log.d("HabitListScreen", "Navigation executed successfully")
                } catch (e: Exception) {
                    Log.e("HabitListScreen", "Navigation error: ${e.message}", e)
                }
            }) { // Navigate to AddHabitScreen
                Icon(Icons.Filled.Add, "Add new habit")
            }
        }
    ) { innerPadding ->
        if (habits.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No habits yet. Tap the + button to add one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = habits,
                    key = { it.id }
                ) { habit ->
                    // Animated visibility for item add/delete
                    AnimatedVisibility(
                        visible = true, // Always true for now, but can be used for delete animation
                        enter = fadeIn(tween(500)) + scaleIn(tween(500)),
                        exit = fadeOut(tween(500)) + scaleOut(tween(500))
                    ) {
                        HabitItem(
                            habit = habit,
                            onDeleteClicked = { viewModel.deleteHabit(habit) },
                            onEditClicked = { navController.navigate(NavRoutes.editHabit(habit.id)) },
                            onCompleteClicked = { viewModel.markHabitCompleted(habit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit, 
    onDeleteClicked: () -> Unit, 
    onEditClicked: () -> Unit,
    onCompleteClicked: () -> Unit = {},
    viewModel: HabitViewModel = hiltViewModel()
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Animate progress bar
    val animatedProgress by animateFloatAsState(
        targetValue = habit.goalProgress.toFloat() / habit.goal.toFloat(),
        animationSpec = tween(durationMillis = 700), label = "progressAnim"
    )

    // Animate streak chip scale and color (spring for bouncy effect)
    val streakScale by animateFloatAsState(
        targetValue = if (habit.streak > 0) 1.2f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow), label = "streakScale"
    )
    val streakColor by animateColorAsState(
        targetValue = if (habit.streak > 0) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 600), label = "streakColor"
    )

    // Animate card background color
    val cardBgColor by animateColorAsState(
        targetValue = if (habit.streak > 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.background,
        animationSpec = tween(durationMillis = 800), label = "cardBgColor"
    )

    // Ripple/glow effect state
    val showGlow = remember { mutableStateOf(false) }
    val glowAlpha by animateFloatAsState(
        targetValue = if (showGlow.value) 0.6f else 0f,
        animationSpec = tween(durationMillis = 400), label = "glowAlpha"
    )

    // Confetti/particle state
    val showConfetti = remember { mutableStateOf(false) }
    LaunchedEffect(habit.streak) {
        if (habit.streak > 0) {
            showConfetti.value = true
            delay(900)
            showConfetti.value = false
        }
    }

    // 3D tilt effect on card hover/tap (simulate with infinite animation for demo)
    val infiniteTransition = rememberInfiniteTransition(label = "tilt")
    val tilt by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "tiltAnim"
    )

    // Animated border color pulse when streak is high
    val borderPulseColor by animateColorAsState(
        targetValue = if (habit.streak >= 7) Color(0xFFFFD700) else Color.Transparent,
        animationSpec = tween(durationMillis = 800), label = "borderPulse"
    )
    val borderPulseWidth by animateFloatAsState(
        targetValue = if (habit.streak >= 7) 4f else 0f,
        animationSpec = tween(durationMillis = 800), label = "borderPulseWidth"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(cardBgColor)
            .graphicsLayer {
                rotationY = tilt
                if (glowAlpha > 0f) {
                    shadowElevation = 24f * glowAlpha
                    shape = MaterialTheme.shapes.medium
                    clip = true
                }
            }
            .then(
                if (borderPulseWidth > 0f) Modifier.border(
                    width = borderPulseWidth.dp,
                    color = borderPulseColor,
                    shape = MaterialTheme.shapes.medium
                ) else Modifier
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Glow effect
            if (glowAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.Yellow.copy(alpha = glowAlpha), Color.Transparent),
                                center = Offset.Unspecified,
                                radius = 600f
                            )
                        )
                )
            }
            // Confetti/particle effect
            if (showConfetti.value) {
                ConfettiEffect()
            }
            // Add a floating sparkle effect when streak is high
            if (habit.streak >= 7) {
                SparkleEffect()
            }
            Column(modifier = Modifier.padding(16.dp)) {
                // Top section with name and complete button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = habit.name, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Complete button
                    FilledTonalButton(
                        onClick = {
                            showGlow.value = true
                            onCompleteClicked()
                            LaunchedEffect(Unit) {
                                delay(400)
                                showGlow.value = false
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false, color = Color.Yellow)
                    ) {
                        Text("Complete")
                    }
                }
                
                // Description (if any)
                habit.description?.let {
                    Text(
                        text = it, 
                        fontSize = 14.sp, 
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress indicator
                val progressText = "Progress: ${habit.goalProgress}/${habit.goal} ${habit.frequency.name.lowercase()}"
                Text(
                    text = progressText,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                ShimmerLinearProgressIndicator(
                    progress = animatedProgress.coerceIn(0f, 1f),
                    color = if (habit.streak > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
                
                // Status section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Streak chip
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = streakColor,
                        modifier = Modifier.scale(streakScale)
                    ) {
                        Text(
                            text = "🔥 ${habit.streak} streak",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    // Frequency chip
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = habit.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Last completed date
                habit.lastCompletedDate?.let {
                    Text(
                        text = "Last completed: ${dateFormat.format(it)}",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onEditClicked,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Edit")
                    }
                    Button(
                        onClick = onDeleteClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfettiEffect(particleCount: Int = 24) {
    val particles = remember { List(particleCount) { ParticleState() } }
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(
                    x = size.width * particle.x,
                    y = size.height * particle.y
                )
            )
        }
    }
}

class ParticleState {
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    val size = Random.nextFloat() * 12f + 6f
    val color = Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat(),
        alpha = 0.8f
    )
}

@Composable
fun SparkleEffect(sparkleCount: Int = 8) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    for (i in 0 until sparkleCount) {
        val angle = 360f / sparkleCount * i
        val sparkleAlpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0.2f at 0
                    1f at 600
                    0.2f at 1200
                },
                repeatMode = RepeatMode.Restart
            ), label = "sparkleAlpha$i"
        )
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                rotationZ = angle,
                alpha = sparkleAlpha
            )
        ) {
            drawCircle(
                color = Color.White.copy(alpha = sparkleAlpha),
                radius = 8f,
                center = Offset(
                    x = size.width / 2 + 60 * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
                    y = size.height / 2 + 60 * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                )
            )
        }
    }
}

@Composable
fun ShimmerLinearProgressIndicator(progress: Float, color: Color) {
    val shimmerAlpha by animateFloatAsState(
        targetValue = if (progress in 0.01f..0.99f) 0.5f else 0f,
        animationSpec = tween(durationMillis = 1200), label = "shimmerAlpha"
    )
    Box(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color
        )
        if (shimmerAlpha > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0f),
                                Color.White.copy(alpha = shimmerAlpha),
                                Color.White.copy(alpha = 0f)
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitListScreenPreview() {
    MyApplicationTheme {
        // In previews, we can't use hiltViewModel easily.
        // For preview purposes, we'll just pass an empty list instead of a real viewModel
        HabitListScreen(
            navController = rememberNavController(),
            // Remove the direct viewModel instantiation which would need a repository
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HabitItemPreview() {
    MyApplicationTheme {
        HabitItem(
            habit = Habit(name = "Preview Habit", description = "This is a test habit for preview."), 
            onDeleteClicked = {}, 
            onEditClicked = {} // Added empty lambda for preview
        )
    }
}
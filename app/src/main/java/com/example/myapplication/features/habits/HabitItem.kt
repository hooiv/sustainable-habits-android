package com.example.myapplication.features.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import com.example.myapplication.ui.animation.*
import com.example.myapplication.ui.components.GradientButton
import com.example.myapplication.ui.components.ThreeDCard
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.pow

@Composable
fun SimpleParallaxBackground(
    scrollOffset: Float,
    modifier: Modifier = Modifier
) {
    // Simulate 3 parallax layers with different speeds
    val colors = listOf(
        listOf(Color(0xFFB3E5FC), Color(0xFFE1F5FE)), // Layer 1
        listOf(Color(0xFF81D4FA), Color(0xFFB3E5FC)), // Layer 2
        listOf(Color(0xFF0288D1), Color(0xFF81D4FA))  // Layer 3
    )
    val speeds = listOf(0.2f, 0.5f, 1.0f)
    Box(modifier = modifier) {
        colors.forEachIndexed { i, colorList ->
            Canvas(modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationY = scrollOffset * speeds[i]
                }
            ) {
                drawRect(
                    brush = Brush.verticalGradient(colorList),
                    size = size
                )
            }
        }
    }
}

@Composable
fun FloatingIconsAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "floatingIcons")
    val yOffsets = List(3) { i ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000 + i * 500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "iconY$i"
        )
    }
    val xOffsets = listOf(30.dp, 120.dp, 220.dp)
    val iconColors = listOf(Color(0xFFFFF176), Color(0xFF80DEEA), Color(0xFFFF8A65))
    Box(modifier = modifier.fillMaxSize()) {
        xOffsets.forEachIndexed { i, x ->
            Box(
                modifier = Modifier
                    .offset(x, 10.dp + yOffsets[i].value.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(iconColors[i].copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onItemClick: () -> Unit,
    onCompletedClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleEnabled: () -> Unit,
    onNeuralInterfaceClick: () -> Unit = {},
    onCompletionHistoryClick: () -> Unit = {},
    onARVisualizationClick: () -> Unit = {},
    onBiometricIntegrationClick: () -> Unit = {},
    onQuantumVisualizationClick: () -> Unit = {},
    index: Int = 0 // Added index parameter for staggered animations
) {
    // Get current date for comparison and formatting
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val isCompletedToday = remember(habit) {
        habit.lastCompletedDate?.let {
            dateFormat.format(it) == dateFormat.format(Date())
        } ?: false
    }

    // Card states
    var isExpanded by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    var cardSize by remember { mutableStateOf(IntSize.Zero) }

    // Animation states
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Calculate progress percentage
    val progress = if (habit.goal > 0) {
        habit.goalProgress.toFloat() / habit.goal.toFloat()
    } else {
        0f
    }

    // Animated values
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isCompletedToday)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        else
            MaterialTheme.colorScheme.surface,
        label = "backgroundColor"
    )

    // Use only one MaterialTheme.elevation property - we'll define this in Theme.kt later
    val cardElevation by animateFloatAsState(
        targetValue = if (habit.isEnabled) 8f else 2f,
        label = "elevation"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (habit.isEnabled) 1f else 0.7f,
        label = "contentAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Entry animation
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(true) {
        delay(staggeredDelay(index, 80, 800).toLong())
        visible.value = true
    }

    // Animated particle effects for completed habits
    val particleAlpha by animateFloatAsState(
        targetValue = if (isCompletedToday) 1f else 0f,
        animationSpec = tween(500),
        label = "particleAlpha"
    )

    // Rotation with smooth animation
    val animatedRotationX by animateFloatAsState(
        targetValue = rotationX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotationX"
    )

    val animatedRotationY by animateFloatAsState(
        targetValue = rotationY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotationY"
    )

    // Create color gradient based on streak and completion status - avoid color ambiguities
    val gradientColors = when {
        isCompletedToday -> listOf(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        )
        habit.streak > 9 -> listOf(
            Color(0xFF39FF14).copy(alpha = 0.4f), // Use direct color value instead of NeonGreen
            Color(0xFF00BFFF).copy(alpha = 0.2f)  // Use direct color value instead of NeonBlue
        )
        habit.streak > 4 -> listOf(
            Color(0xFF00BFFF).copy(alpha = 0.3f), // Use direct color value instead of NeonBlue
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        else -> listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface
        )
    }

    // Main card wrapper with entrance animation
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animeEntrance(
                visible = visible.value,
                initialOffsetY = 100,
                initialAlpha = 0f,
                initialScale = 0.9f
            )
    ) {
        // Particle effects when habit is completed (visible only when completed)
        if (particleAlpha > 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(particleAlpha)
            ) {
                // Add subtle star particles when habit is completed
                repeat(10) { i ->
                    val particleDelay = i * 100
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(
                                x = ((i * 20) % (cardSize.width / density.density)).dp,
                                y = ((i * 15) % (cardSize.height / density.density)).dp
                            )
                            .alpha(0.7f)
                            .background(
                                color = Color(0xFFFFC107),
                                shape = CircleShape
                            )
                            .loadingBounceEffect(enabled = true)
                    )
                }
            }
        }

        // Main card with 3D effect
        ThreeDCard(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { cardSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // Reset rotation when touch ends
                            rotationX = 0f
                            rotationY = 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()

                        // Calculate rotation based on drag position
                        val touchX = change.position.x
                        val touchY = change.position.y

                        // Apply rotation (limited range)
                        rotationX = (touchY / cardSize.height.coerceAtLeast(1)) * 10f
                        rotationY = -(touchX / cardSize.width.coerceAtLeast(1)) * 10f
                    }
                }
                .clickable { onItemClick() },
            onClick = {
                isExpanded = !isExpanded
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        ) {
            // Card content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(colors = gradientColors),
                        shape = MaterialTheme.shapes.medium
                    )
                    .animateContentSize()
                    .padding(16.dp)
            ) {
                // Title row with frequency and indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Show streak fire icon with animation for streaks
                        if (habit.streak > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .pulseEffect(pulseEnabled = habit.streak > 3)
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak: ${habit.streak}",
                                    modifier = Modifier.size(24.dp),
                                    tint = when {
                                        habit.streak > 9 -> Color(0xFFFF00E4) // Use direct color value instead of NeonPink
                                        habit.streak > 4 -> Color(0xFFFF9800)
                                        else -> Color(0xFFF57C00)
                                    }
                                )

                                // Add streak count inside the fire icon for higher streaks
                                if (habit.streak > 3) {
                                    Text(
                                        text = "${habit.streak}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .offset(y = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isCompletedToday) 0.7f else 1f)
                        )
                    }

                    // Right side indicators
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Frequency chip with icon
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (habit.frequency) {
                                        HabitFrequency.DAILY -> Icons.Default.Today
                                        HabitFrequency.WEEKLY -> Icons.Default.DateRange
                                        HabitFrequency.MONTHLY -> Icons.Default.CalendarMonth
                                        HabitFrequency.CUSTOM -> Icons.Default.Schedule
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (habit.frequency) {
                                        HabitFrequency.DAILY -> "Daily"
                                        HabitFrequency.WEEKLY -> "Weekly"
                                        HabitFrequency.MONTHLY -> "Monthly"
                                        HabitFrequency.CUSTOM -> "Custom"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Reminder indicator with animation
                        if (habit.reminderTime != null) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .pulseEffect(pulseEnabled = true, pulseMagnitude = 0.05f)
                            ) {
                                Icon(
                                    Icons.Default.Alarm,
                                    contentDescription = "Reminder set",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                // Progress indicator
                if (habit.goal > 0) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress: ${habit.goalProgress}/${habit.goal}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            // Completion percentage
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = when {
                                    animatedProgress >= 1f -> MaterialTheme.colorScheme.primary
                                    animatedProgress >= 0.7f -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                }
                            )
                        }

                        // Animated progress bar with gradient colors
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedProgress)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = when {
                                                animatedProgress >= 1f -> listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.tertiary
                                                )
                                                animatedProgress >= 0.7f -> listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                                else -> listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        )
                                    )
                            )
                        }
                    }
                }

                // Description if available - shown when expanded or no goal
                if (!habit.description.isNullOrEmpty() && (isExpanded || habit.goal <= 0)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Action buttons with improved layout and animations
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Toggle enabled button with animation
                    IconButton(
                        onClick = {
                            onToggleEnabled()
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (habit.isEnabled) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (habit.isEnabled) "Pause habit" else "Resume habit",
                            tint = if (habit.isEnabled)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .scale(if (habit.isEnabled) 1f else 0.9f)
                        )
                    }

                    // More options menu
                    Box {
                        var expanded by remember { mutableStateOf(false) }

                        IconButton(
                            onClick = {
                                expanded = true
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.scale(0.9f)
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            // Delete option
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    expanded = false
                                    onDeleteClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )

                            // Neural Interface option
                            DropdownMenuItem(
                                text = { Text("Neural Interface") },
                                onClick = {
                                    expanded = false
                                    onNeuralInterfaceClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = "Neural Interface",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            )

                            // AR Visualization option
                            DropdownMenuItem(
                                text = { Text("AR Visualization") },
                                onClick = {
                                    expanded = false
                                    onARVisualizationClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ViewInAr,
                                        contentDescription = "AR Visualization",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            )

                            // Biometric Integration option
                            DropdownMenuItem(
                                text = { Text("Biometric Data") },
                                onClick = {
                                    expanded = false
                                    onBiometricIntegrationClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.MonitorHeart,
                                        contentDescription = "Biometric Data",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )

                            // Quantum Visualization option
                            DropdownMenuItem(
                                text = { Text("Quantum View") },
                                onClick = {
                                    expanded = false
                                    onQuantumVisualizationClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Biotech,
                                        contentDescription = "Quantum Visualization",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            )

                            // Completion History option
                            DropdownMenuItem(
                                text = { Text("Completion History") },
                                onClick = {
                                    expanded = false
                                    onCompletionHistoryClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Completion History",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )

                            // AR Visualization option
                            DropdownMenuItem(
                                text = { Text("AR Visualization") },
                                onClick = {
                                    expanded = false
                                    onARVisualizationClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ViewInAr,
                                        contentDescription = "AR Visualization",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            )
                        }
                    }

                    // Complete button with gradient and animation
                    GradientButton(
                        text = if (isCompletedToday) "Completed" else "Complete",
                        onClick = {
                            if (!isCompletedToday && habit.isEnabled) {
                                onCompletedClick()
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        gradientColors = when {
                            isCompletedToday -> listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                            habit.isEnabled -> listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                            else -> listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        },
                        contentColor = if (isCompletedToday || !habit.isEnabled)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
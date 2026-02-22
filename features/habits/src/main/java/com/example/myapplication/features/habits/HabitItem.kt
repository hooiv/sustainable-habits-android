package com.example.myapplication.features.habits.ui

import android.content.Context
import android.os.PowerManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitFrequency

import com.example.myapplication.core.ui.animation.*
import com.example.myapplication.core.ui.components.ThreeDCard
import com.example.myapplication.core.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable for native Compose animations within habit items replacing Anime.js
 */
@Composable
fun HabitItemNativeAnimation(
    modifier: Modifier = Modifier,
    animationType: String = "pulse"
) {
    val context = LocalContext.current
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    val isPowerSaveMode = powerManager.isPowerSaveMode

    if (isPowerSaveMode) return

    val infiniteTransition = rememberInfiniteTransition(label = "habit_anim")
    
    val scale = if (animationType == "pulse") {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        ).value
    } else 1f

    val rotation = if (animationType == "rotate") {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotate"
        ).value
    } else 0f

    val alpha = if (animationType == "fade") {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fade"
        ).value
    } else 1f

    Box(
        modifier = modifier.height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                    this.rotationZ = rotation
                    this.alpha = alpha
                }
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onItemClick: () -> Unit,
    onCompletedClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleEnabled: () -> Unit,
    onCompletionHistoryClick: () -> Unit = {},
    index: Int = 0
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

    // Use Native Compose animations for advanced effects
    var useAdvancedAnimations by remember { mutableStateOf(true) }

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
        // Native animation when habit is completed
        if (isCompletedToday && useAdvancedAnimations) {
            HabitItemNativeAnimation(
                modifier = Modifier.matchParentSize(),
                animationType = if (habit.streak > 5) "rotate" else "pulse"
            )
        }

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
                .semantics(mergeDescendants = true) {
                    stateDescription = if (isCompletedToday) "Completed today" else "Not completed"
                    customActions = listOf(
                        CustomAccessibilityAction(if (habit.isEnabled) "Pause habit" else "Resume habit") {
                            onToggleEnabled()
                            true
                        },
                        CustomAccessibilityAction("Delete") {
                            onDeleteClick()
                            true
                        },
                        CustomAccessibilityAction("Completion History") {
                            onCompletionHistoryClick()
                            true
                        }
                    )
                }
                .clickable(onClickLabel = "Toggle expand ${habit.name}") { onItemClick() },
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
                            text = habit.description ?: "",
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
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
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

                            // Toggle advanced animations
                            DropdownMenuItem(
                                text = { Text(if (useAdvancedAnimations) "Disable Animations" else "Enable Animations") },
                                onClick = {
                                    expanded = false
                                    useAdvancedAnimations = !useAdvancedAnimations
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (useAdvancedAnimations) Icons.Default.Animation else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    }

                    // Complete button
                    if (isCompletedToday) {
                        FilledTonalButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )
                            Text("Completed")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (habit.isEnabled) {
                                    onCompletedClick()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            enabled = habit.isEnabled,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )
                            Text("Complete")
                        }
                    }
                }
            }
        }
    }
}

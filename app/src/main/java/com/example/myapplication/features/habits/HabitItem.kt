package com.example.myapplication.features.habits

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitItem(
    habit: Habit,
    onItemClick: () -> Unit,
    onCompletedClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    val today = Calendar.getInstance().timeInMillis
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val isCompletedToday = remember(habit) {
        habit.lastCompletedDate?.let {
            dateFormat.format(Date(it)) == dateFormat.format(Date(today))
        } ?: false
    }
    
    // Calculate progress percentage
    val progress = if (habit.goalCount > 0) {
        habit.goalProgress.toFloat() / habit.goalCount.toFloat()
    } else 0f
    
    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "progress"
    )
    
    // Card elevation based on enabled state
    val cardElevation = if (habit.isEnabled) 2.dp else 0.dp
    val contentAlpha = if (habit.isEnabled) 1f else 0.4f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .alpha(contentAlpha),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title row with frequency and indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                    )
                    
                    // Show habit frequency
                    Text(
                        text = when (habit.frequency) {
                            HabitFrequency.DAILY -> " • Daily"
                            HabitFrequency.WEEKLY -> " • Weekly"
                            HabitFrequency.MONTHLY -> " • Monthly"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Show reminder and streak indicator
                Row {
                    if (habit.reminderTime != null) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = "Reminder set",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    if (habit.streak > 0) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFF57C00) // Orange
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${habit.streak}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFF57C00)
                        )
                    }
                }
            }
            
            // Description if available
            if (!habit.description.isNullOrEmpty()) {
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Progress indicator if there's a goal
            if (habit.goalCount > 0) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${habit.goalProgress}/${habit.goalCount}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Toggle enabled button
                IconButton(onClick = onToggleEnabled) {
                    Icon(
                        imageVector = if (habit.isEnabled) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = if (habit.isEnabled) "Pause habit" else "Resume habit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Delete button
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete habit",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                // Complete button
                Button(
                    onClick = onCompletedClick,
                    enabled = !isCompletedToday && habit.isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompletedToday) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isCompletedToday) Icons.Default.CheckCircle else Icons.Default.Done,
                        contentDescription = "Mark as completed"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isCompletedToday) "Completed" else "Complete")
                }
            }
        }
    }
}
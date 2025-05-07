package com.example.myapplication.features.habits

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitItem(
    habit: Habit,
    onItemClick: () -> Unit,
    onCompletedClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val lastCompletedText = habit.lastCompletedDate?.let { "Last: ${dateFormat.format(it)}" } ?: "Not started yet"
    
    val progressText = when (habit.frequency) {
        HabitFrequency.DAILY -> "${habit.goalProgress}/${habit.goal} today"
        HabitFrequency.WEEKLY -> "${habit.goalProgress}/${habit.goal} this week"
        HabitFrequency.MONTHLY -> "${habit.goalProgress}/${habit.goal} this month"
    }
    
    val streakEmoji = when {
        habit.streak >= 10 -> "🔥🔥🔥"
        habit.streak >= 5 -> "🔥🔥"
        habit.streak >= 1 -> "🔥"
        else -> ""
    }
    
    val itemAlpha = if (habit.isEnabled) 1f else 0.6f
    
    Card(
        onClick = onItemClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(itemAlpha),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Progress indicator
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { habit.goalProgress.toFloat() / habit.goal.coerceAtLeast(1) },
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                Text(
                    text = habit.goalProgress.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Middle: Habit details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (streakEmoji.isNotEmpty()) {
                        Text(
                            text = " $streakEmoji",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (habit.reminderTime != null) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = "Has reminder",
                            modifier = Modifier
                                .size(16.dp)
                                .padding(start = 4.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Text(
                    text = "$progressText • ${habit.frequency.name.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (habit.streak > 0) {
                    Text(
                        text = "Streak: ${habit.streak} ${if (habit.frequency == HabitFrequency.DAILY) "days" else habit.frequency.name.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = lastCompletedText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Right side: Action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Check button
                IconButton(
                    onClick = onCompletedClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Mark as completed",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // More options menu
                var expanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (habit.isEnabled) "Disable" else "Enable") },
                            onClick = {
                                onToggleEnabled()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (habit.isEnabled) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDeleteClick()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
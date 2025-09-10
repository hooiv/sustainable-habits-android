package com.example.myapplication.features.spatial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.data.model.SpatialObjectType

/**
 * A component for selecting spatial object types
 */
@Composable
fun SpatialObjectTypeSelector(
    onTypeSelected: (SpatialObjectType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(SpatialObjectType.HABIT_SPHERE) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SpatialObjectType.values()) { type ->
                SpatialObjectTypeItem(
                    type = type,
                    isSelected = type == selectedType,
                    onClick = {
                        selectedType = type
                        onTypeSelected(type)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description of selected type
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = getObjectTypeDescription(selectedType),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * A component for displaying a single spatial object type
 */
@Composable
fun SpatialObjectTypeItem(
    type: SpatialObjectType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(80.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(getObjectTypeColor(type).copy(alpha = 0.2f))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getObjectTypeIcon(type),
                contentDescription = type.name,
                tint = getObjectTypeColor(type),
                modifier = Modifier.size(32.dp)
            )
        }

        // Label
        Text(
            text = getObjectTypeName(type),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Get the icon for a spatial object type
 */
fun getObjectTypeIcon(type: SpatialObjectType) = when (type) {
    SpatialObjectType.HABIT_SPHERE -> Icons.Default.Circle
    SpatialObjectType.STREAK_TOWER -> Icons.Default.Architecture
    SpatialObjectType.GOAL_PYRAMID -> Icons.Default.ChangeHistory
    SpatialObjectType.ACHIEVEMENT_STAR -> Icons.Default.Star
    SpatialObjectType.CATEGORY_CUBE -> Icons.Default.Widgets
    SpatialObjectType.REMINDER_CLOCK -> Icons.Default.Schedule
}

/**
 * Get the color for a spatial object type
 */
fun getObjectTypeColor(type: SpatialObjectType) = when (type) {
    SpatialObjectType.HABIT_SPHERE -> Color(0xFF2196F3) // Blue
    SpatialObjectType.STREAK_TOWER -> Color(0xFFF44336) // Red
    SpatialObjectType.GOAL_PYRAMID -> Color(0xFF4CAF50) // Green
    SpatialObjectType.ACHIEVEMENT_STAR -> Color(0xFFFFEB3B) // Yellow
    SpatialObjectType.CATEGORY_CUBE -> Color(0xFF9C27B0) // Purple
    SpatialObjectType.REMINDER_CLOCK -> Color(0xFFFF9800) // Orange
}

/**
 * Get the name for a spatial object type
 */
fun getObjectTypeName(type: SpatialObjectType) = when (type) {
    SpatialObjectType.HABIT_SPHERE -> "Habit"
    SpatialObjectType.STREAK_TOWER -> "Streak"
    SpatialObjectType.GOAL_PYRAMID -> "Goal"
    SpatialObjectType.ACHIEVEMENT_STAR -> "Achievement"
    SpatialObjectType.CATEGORY_CUBE -> "Category"
    SpatialObjectType.REMINDER_CLOCK -> "Reminder"
}

/**
 * Get the description for a spatial object type
 */
fun getObjectTypeDescription(type: SpatialObjectType) = when (type) {
    SpatialObjectType.HABIT_SPHERE -> "A basic habit representation. Spheres represent individual habits in your spatial environment."
    SpatialObjectType.STREAK_TOWER -> "Represents your habit streaks. The height of the tower corresponds to your streak length."
    SpatialObjectType.GOAL_PYRAMID -> "Visualizes your habit goals. The pyramid grows as you approach your goal."
    SpatialObjectType.ACHIEVEMENT_STAR -> "Represents achievements and badges you've earned through consistent habit completion."
    SpatialObjectType.CATEGORY_CUBE -> "Organizes habits by category. Cubes can contain multiple related habits."
    SpatialObjectType.REMINDER_CLOCK -> "Represents habit reminders. The clock shows when you should perform your habit."
}

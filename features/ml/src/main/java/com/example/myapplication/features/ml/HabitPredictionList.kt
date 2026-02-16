package com.example.myapplication.features.ml

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.features.ml.HabitPrediction
import com.example.myapplication.features.ml.PredictionType

/**
 * A list of habit predictions
 */
@Composable
fun HabitPredictionList(
    predictions: List<HabitPrediction>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(predictions) { prediction ->
            HabitPredictionCard(prediction = prediction)
        }
    }
}

/**
 * A card displaying a habit prediction
 */
@Composable
fun HabitPredictionCard(
    prediction: HabitPrediction,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prediction type icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            color = when (prediction.predictionType) {
                                PredictionType.COMPLETION_LIKELIHOOD -> MaterialTheme.colorScheme.primary
                                PredictionType.STREAK_CONTINUATION -> MaterialTheme.colorScheme.secondary
                                PredictionType.HABIT_FORMATION -> MaterialTheme.colorScheme.tertiary
                                PredictionType.HABIT_ABANDONMENT -> MaterialTheme.colorScheme.error
                                PredictionType.OPTIMAL_TIME -> MaterialTheme.colorScheme.primaryContainer
                                PredictionType.DIFFICULTY_CHANGE -> MaterialTheme.colorScheme.secondaryContainer
                            }.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (prediction.predictionType) {
                            PredictionType.COMPLETION_LIKELIHOOD -> Icons.Default.CheckCircle
                            PredictionType.STREAK_CONTINUATION -> Icons.Default.Timeline
                            PredictionType.HABIT_FORMATION -> Icons.Default.AddCircle
                            PredictionType.HABIT_ABANDONMENT -> Icons.Default.RemoveCircle
                            PredictionType.OPTIMAL_TIME -> Icons.Default.Schedule
                            PredictionType.DIFFICULTY_CHANGE -> Icons.Default.TrendingUp
                        },
                        contentDescription = prediction.predictionType.name,
                        tint = when (prediction.predictionType) {
                            PredictionType.COMPLETION_LIKELIHOOD -> MaterialTheme.colorScheme.primary
                            PredictionType.STREAK_CONTINUATION -> MaterialTheme.colorScheme.secondary
                            PredictionType.HABIT_FORMATION -> MaterialTheme.colorScheme.tertiary
                            PredictionType.HABIT_ABANDONMENT -> MaterialTheme.colorScheme.error
                            PredictionType.OPTIMAL_TIME -> MaterialTheme.colorScheme.primary
                            PredictionType.DIFFICULTY_CHANGE -> MaterialTheme.colorScheme.secondary
                        }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Prediction details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = prediction.habitName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = when (prediction.predictionType) {
                            PredictionType.COMPLETION_LIKELIHOOD -> "Completion Likelihood"
                            PredictionType.STREAK_CONTINUATION -> "Streak Continuation"
                            PredictionType.HABIT_FORMATION -> "Habit Formation"
                            PredictionType.HABIT_ABANDONMENT -> "Habit Abandonment Risk"
                            PredictionType.OPTIMAL_TIME -> "Optimal Time"
                            PredictionType.DIFFICULTY_CHANGE -> "Difficulty Change"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Probability indicator
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${(prediction.probability * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = prediction.timeframe,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Expand/collapse button
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = if (expanded) "Less" else "More")
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Show less" else "Show more"
                )
            }

            // Expanded content
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Confidence Interval: ${(prediction.confidenceInterval.first * 100).toInt()}% - ${(prediction.confidenceInterval.second * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Contributing Factors:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                // Factors list
                prediction.factors.forEach { factor ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Impact indicator
                        Icon(
                            imageVector = when {
                                factor.impact > 0.3f -> Icons.AutoMirrored.Filled.TrendingUp
                                factor.impact < -0.3f -> Icons.AutoMirrored.Filled.TrendingDown
                                else -> Icons.AutoMirrored.Filled.TrendingFlat
                            },
                            contentDescription = "Factor impact",
                            tint = when {
                                factor.impact > 0.3f -> MaterialTheme.colorScheme.primary
                                factor.impact < -0.3f -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Factor name
                        Text(
                            text = factor.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        // Impact value
                        Text(
                            text = when {
                                factor.impact > 0 -> "+${(factor.impact * 100).toInt()}%"
                                else -> "${(factor.impact * 100).toInt()}%"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                factor.impact > 0.3f -> MaterialTheme.colorScheme.primary
                                factor.impact < -0.3f -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
            }
        }
    }
}

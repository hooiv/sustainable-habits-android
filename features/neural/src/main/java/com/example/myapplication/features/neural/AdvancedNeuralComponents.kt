package com.example.myapplication.features.neural

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.network.ml.TestResult
import com.example.myapplication.core.data.model.HabitRecommendation
import com.example.myapplication.core.data.model.NeuralPrediction
import com.example.myapplication.core.data.model.PredictionType
import com.example.myapplication.core.data.model.SimpleReinforcementAction
import java.text.SimpleDateFormat
import java.util.*

/**
 * Displays personalized recommendations
 */
@Composable
fun RecommendationsCard(
    recommendations: List<HabitRecommendation>,
    onRecommendationAction: (HabitRecommendation, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Personalized Recommendations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (recommendations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recommendations available yet. Complete this habit a few times to get personalized suggestions.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recommendations) { recommendation ->
                        RecommendationItem(
                            recommendation = recommendation,
                            onAction = onRecommendationAction
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual recommendation item
 */
@Composable
fun RecommendationItem(
    recommendation: HabitRecommendation,
    onAction: (HabitRecommendation, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Recommendation icon and title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on recommendation type
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getRecommendationColor(recommendation.type))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getRecommendationIcon(recommendation.type),
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recommendation description
        Text(
            text = recommendation.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 56.dp),
            horizontalArrangement = Arrangement.End
        ) {
            // Only show action buttons if recommendation hasn't been acted upon
            if (recommendation.isFollowed == null) {
                OutlinedButton(
                    onClick = { onAction(recommendation, false) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Dismiss")
                }

                Button(
                    onClick = { onAction(recommendation, true) }
                ) {
                    Text("Apply")
                }
            } else {
                // Show status if already acted upon
                Text(
                    text = if (recommendation.isFollowed == true) "Applied" else "Dismissed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Get icon for recommendation type
 */
@Composable
fun getRecommendationIcon(type: Int) = when (type) {
    0 -> Icons.Default.Schedule // Timing
    1 -> Icons.Default.Bolt // Streak
    2 -> Icons.Default.Edit // Modification
    3 -> Icons.Default.EmojiEvents // Motivation
    4 -> Icons.Default.Place // Context
    else -> Icons.Default.Lightbulb
}

/**
 * Get color for recommendation type
 */
@Composable
fun getRecommendationColor(type: Int) = when (type) {
    0 -> MaterialTheme.colorScheme.primary
    1 -> MaterialTheme.colorScheme.secondary
    2 -> MaterialTheme.colorScheme.tertiary
    3 -> Color(0xFFFFA000) // Amber
    4 -> Color(0xFF43A047) // Green
    else -> MaterialTheme.colorScheme.primary
}

/**
 * Displays feature importance for explainable AI
 */
@Composable
fun FeatureImportanceCard(
    featureImportance: Map<String, Float>,
    explanation: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Prediction Explanation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation text
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Feature Importance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Feature importance bars
            featureImportance.forEach { (feature, importance) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(120.dp)
                    )

                    LinearProgressIndicator(
                        progress = importance,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            importance > 0.25f -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )

                    Text(
                        text = "${(importance * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

/**
 * Displays reinforcement learning action
 */
@Composable
fun ReinforcementActionCard(
    action: SimpleReinforcementAction?,
    actionDescription: String,
    onFeedback: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "AI Recommendation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (action == null) {
                Text(
                    text = "No recommendation available yet. Complete this habit a few times to get personalized timing suggestions.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                // Action icon and description
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = actionDescription,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feedback section
                Text(
                    text = "Was this recommendation helpful?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Feedback buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeedbackButton(
                        icon = Icons.Default.ThumbDown,
                        label = "Not Helpful",
                        onClick = { onFeedback(-10f) }
                    )

                    FeedbackButton(
                        icon = Icons.Default.ThumbsUpDown,
                        label = "Somewhat",
                        onClick = { onFeedback(5f) }
                    )

                    FeedbackButton(
                        icon = Icons.Default.ThumbUp,
                        label = "Very Helpful",
                        onClick = { onFeedback(10f) }
                    )
                }
            }
        }
    }
}

/**
 * Feedback button for reinforcement learning
 */
@Composable
fun FeedbackButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Displays A/B testing results
 */
@Composable
fun ABTestingResultsCard(
    testResults: Map<String, TestResult>,
    currentVariant: String,
    onSwitchVariant: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Neural Network Optimization",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Current model: $currentVariant",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (testResults.isEmpty()) {
                Text(
                    text = "No test results available yet. Train the neural network to see performance metrics.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                // Results table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    // Table header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Variant",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Accuracy",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Loss",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Actions",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    // Table rows
                    testResults.forEach { (variant, result) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = variant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "${(result.accuracy * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = String.format("%.4f", result.loss),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { onSwitchVariant(variant) },
                                modifier = Modifier.weight(0.5f)
                            ) {
                                Icon(
                                    imageVector = if (variant == currentVariant)
                                        Icons.Default.CheckCircle else Icons.Default.SwapHoriz,
                                    contentDescription = "Switch to this variant",
                                    tint = if (variant == currentVariant)
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Divider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

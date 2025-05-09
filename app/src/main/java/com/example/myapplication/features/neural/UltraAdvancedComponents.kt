package com.example.myapplication.features.neural

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.myapplication.data.ml.CompressionStats
import com.example.myapplication.data.ml.Hyperparameters
import com.example.myapplication.data.ml.TrialResult
import com.example.myapplication.data.ml.HabitAnomaly
import java.text.SimpleDateFormat
import java.util.*

/**
 * Displays model compression statistics
 */
@Composable
fun ModelCompressionCard(
    compressionStats: CompressionStats,
    onRecompress: () -> Unit,
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
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Compress,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
                
                Text(
                    text = "Model Compression",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Compression statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Original Size",
                    value = formatFileSize(compressionStats.originalSize),
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    label = "Compressed Size",
                    value = formatFileSize(compressionStats.compressedSize),
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    label = "Ratio",
                    value = String.format("%.1fx", compressionStats.compressionRatio),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Space saved indicator
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Space Saved: ${formatFileSize(compressionStats.spaceSaved)} (${compressionStats.percentSaved.toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = compressionStats.percentSaved / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recompress button
            Button(
                onClick = onRecompress,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Compress,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Recompress Model")
            }
        }
    }
}

/**
 * Displays hyperparameter optimization results
 */
@Composable
fun HyperparameterOptimizationCard(
    trialResults: List<TrialResult>,
    bestHyperparameters: Hyperparameters?,
    hyperparameterImportance: Map<String, Float>,
    onOptimize: () -> Unit,
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
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
                
                Text(
                    text = "Hyperparameter Optimization",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Best hyperparameters
            if (bestHyperparameters != null) {
                Text(
                    text = "Best Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    HyperparameterRow(
                        name = "Learning Rate",
                        value = bestHyperparameters.learningRate.toString(),
                        importance = hyperparameterImportance["learningRate"] ?: 0f
                    )
                    
                    HyperparameterRow(
                        name = "Hidden Layers",
                        value = bestHyperparameters.hiddenLayerSizes.joinToString(", "),
                        importance = hyperparameterImportance["hiddenLayerSizes"] ?: 0f
                    )
                    
                    HyperparameterRow(
                        name = "Batch Size",
                        value = bestHyperparameters.batchSize.toString(),
                        importance = hyperparameterImportance["batchSize"] ?: 0f
                    )
                    
                    HyperparameterRow(
                        name = "Dropout Rate",
                        value = bestHyperparameters.dropoutRate.toString(),
                        importance = hyperparameterImportance["dropoutRate"] ?: 0f
                    )
                }
            } else {
                Text(
                    text = "No optimization results available yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Trial results
            if (trialResults.isNotEmpty()) {
                Text(
                    text = "Optimization Trials",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Trial results chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    // Simple bar chart of trial scores
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxScore = trialResults.maxOfOrNull { it.score } ?: 1f
                        
                        trialResults.forEach { result ->
                            val normalizedScore = result.score / maxScore
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = String.format("%.2f", result.score),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height((normalizedScore * 100).dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                                
                                Text(
                                    text = result.trial.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Optimize button
            Button(
                onClick = onOptimize,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Optimize Hyperparameters")
            }
        }
    }
}

/**
 * Displays anomaly detection results
 */
@Composable
fun AnomalyDetectionCard(
    anomalies: List<HabitAnomaly>,
    onExplainAnomaly: (HabitAnomaly) -> Unit,
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
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
                
                Text(
                    text = "Anomaly Detection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (anomalies.isEmpty()) {
                Text(
                    text = "No anomalies detected in your habit patterns",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "Detected Anomalies",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Anomaly list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(anomalies) { anomaly ->
                        AnomalyItem(
                            anomaly = anomaly,
                            onExplain = { onExplainAnomaly(anomaly) }
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
 * Displays multi-modal learning features
 */
@Composable
fun MultiModalLearningCard(
    hasImageFeatures: Boolean,
    hasTextFeatures: Boolean,
    hasSensorFeatures: Boolean,
    onCaptureImage: () -> Unit,
    onAddNotes: () -> Unit,
    onProcessFeatures: () -> Unit,
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
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
                
                Text(
                    text = "Multi-Modal Learning",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Enhance your habit tracking with images, notes, and sensor data",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Feature status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureStatusItem(
                    icon = Icons.Default.Image,
                    label = "Image",
                    isActive = hasImageFeatures
                )
                
                FeatureStatusItem(
                    icon = Icons.Default.Notes,
                    label = "Text",
                    isActive = hasTextFeatures
                )
                
                FeatureStatusItem(
                    icon = Icons.Default.Sensors,
                    label = "Sensor",
                    isActive = hasSensorFeatures
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onCaptureImage,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasImageFeatures) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Capture")
                }
                
                Button(
                    onClick = onAddNotes,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasTextFeatures) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Add Notes")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onProcessFeatures,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = hasImageFeatures || hasTextFeatures || hasSensorFeatures
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Process Features")
            }
        }
    }
}

/**
 * Displays meta-learning status
 */
@Composable
fun MetaLearningCard(
    metaLearningProgress: Float,
    adaptationProgress: Float,
    onStartMetaLearning: () -> Unit,
    onAdaptToHabit: () -> Unit,
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
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
                
                Text(
                    text = "Meta-Learning",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Learn from all your habits to quickly adapt to new ones",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress indicators
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Meta-Learning Progress",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = metaLearningProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Adaptation Progress",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = adaptationProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onStartMetaLearning,
                    enabled = metaLearningProgress == 0f || metaLearningProgress == 1f
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Meta-Learn")
                }
                
                Button(
                    onClick = onAdaptToHabit,
                    enabled = metaLearningProgress == 1f && adaptationProgress == 0f
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Adapt to Habit")
                }
            }
        }
    }
}

/**
 * Helper components
 */

@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HyperparameterRow(
    name: String,
    value: String,
    importance: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(120.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        LinearProgressIndicator(
            progress = importance,
            modifier = Modifier
                .width(60.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = when {
                importance > 0.5f -> MaterialTheme.colorScheme.primary
                importance > 0.25f -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.tertiary
            }
        )
    }
}

@Composable
fun AnomalyItem(
    anomaly: HabitAnomaly,
    onExplain: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Anomaly type icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when (anomaly.type) {
                        com.example.myapplication.data.ml.AnomalyType.TIME -> MaterialTheme.colorScheme.primary
                        com.example.myapplication.data.ml.AnomalyType.FREQUENCY -> MaterialTheme.colorScheme.secondary
                        com.example.myapplication.data.ml.AnomalyType.PATTERN -> MaterialTheme.colorScheme.tertiary
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (anomaly.type) {
                    com.example.myapplication.data.ml.AnomalyType.TIME -> Icons.Default.Schedule
                    com.example.myapplication.data.ml.AnomalyType.FREQUENCY -> Icons.Default.CalendarToday
                    com.example.myapplication.data.ml.AnomalyType.PATTERN -> Icons.Default.Insights
                },
                contentDescription = null,
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Anomaly description
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = anomaly.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
                    .format(Date(anomaly.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Explain button
        IconButton(
            onClick = onExplain
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Explain anomaly",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FeatureStatusItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) Color.White
                      else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Utility functions
 */
private fun formatFileSize(bytes: Int): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

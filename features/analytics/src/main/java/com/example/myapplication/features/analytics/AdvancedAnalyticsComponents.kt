package com.example.myapplication.features.analytics.ui

import androidx.compose.animation.core.*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.myapplication.core.ui.theme.Orange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.ui.animation.AnimeEasing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * Data class representing an analytics insight
 */
data class AnalyticsInsight(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val type: InsightType,
    val confidence: Float, // 0.0 to 1.0 (confidence level)
    val relatedHabitIds: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Enum for different types of analytics insights
 */
enum class InsightType {
    TREND_DETECTION,
    PATTERN_RECOGNITION,
    CORRELATION,
    ANOMALY_DETECTION,
    PREDICTION,
    RECOMMENDATION,
    ACHIEVEMENT
}

/**
 * A component that displays a 3D interactive habit correlation matrix
 */
@Composable
fun HabitCorrelationMatrix(
    habits: List<Habit>,
    correlationData: Map<Pair<String, String>, Float>, // Habit ID pairs to correlation value (-1.0 to 1.0)
    modifier: Modifier = Modifier,
    onCellClick: (Habit, Habit, Float) -> Unit = { _, _, _ -> }
) {
    var rotationX by remember { mutableStateOf(20f) }
    var rotationY by remember { mutableStateOf(20f) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Animation for cell selection
    val selectedCellScale by animateFloatAsState(
        targetValue = if (selectedCell != null) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cellScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Convert tap position to matrix cell
                    val size = this.size
                    val cellSize = size.width / habits.size
                    val row = (offset.y / cellSize).toInt().coerceIn(0, habits.size - 1)
                    val col = (offset.x / cellSize).toInt().coerceIn(0, habits.size - 1)

                    if (row != col) { // Ignore diagonal (self-correlation)
                        val habitA = habits[row]
                        val habitB = habits[col]
                        val correlation = correlationData[Pair(habitA.id, habitB.id)]
                            ?: correlationData[Pair(habitB.id, habitA.id)]
                            ?: 0f

                        selectedCell = Pair(row, col)
                        onCellClick(habitA, habitB, correlation)

                        // Auto-clear selection after a delay
                        coroutineScope.launch {
                            delay(3000)
                            selectedCell = null
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Create text measurer outside of Canvas
    val textMeasurer = rememberTextMeasurer()

    // 3D Matrix visualization
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                this.cameraDistance = 12f * density
            }
    ) {
            val cellSize = size.width / habits.size

            // Draw matrix cells
            for (i in habits.indices) {
                for (j in habits.indices) {
                    if (i == j) {
                        // Diagonal cells (self-correlation)
                        drawRect(
                            color = Color.Gray.copy(alpha = 0.3f),
                            topLeft = Offset(j * cellSize, i * cellSize),
                            size = Size(cellSize, cellSize),
                            style = Stroke(width = 1f)
                        )
                    } else {
                        // Get correlation value
                        val correlation = correlationData[Pair(habits[i].id, habits[j].id)]
                            ?: correlationData[Pair(habits[j].id, habits[i].id)]
                            ?: 0f

                        // Determine cell color based on correlation
                        val cellColor = when {
                            correlation > 0.7f -> Color.Green.copy(alpha = 0.7f)
                            correlation > 0.3f -> Color.Yellow.copy(alpha = 0.7f)
                            correlation > -0.3f -> Color.Gray.copy(alpha = 0.3f)
                            correlation > -0.7f -> Orange.copy(alpha = 0.7f)
                            else -> Color.Red.copy(alpha = 0.7f)
                        }

                        // Check if this is the selected cell
                        val isSelected = selectedCell?.let { it.first == i && it.second == j } ?: false
                        val cellScale = if (isSelected) selectedCellScale else 1f

                        // Calculate cell position with potential scaling for selected cell
                        val cellOffset = Offset(
                            j * cellSize + (cellSize * (1f - cellScale) / 2f),
                            i * cellSize + (cellSize * (1f - cellScale) / 2f)
                        )

                        // Draw cell
                        drawRect(
                            color = cellColor,
                            topLeft = cellOffset,
                            size = Size(cellSize * cellScale, cellSize * cellScale)
                        )

                        // Draw correlation value - using a pre-created text measurer
                        drawText(
                            textMeasurer = textMeasurer,
                            text = String.format("%.2f", correlation),
                            topLeft = Offset(
                                j * cellSize + cellSize / 4f,
                                i * cellSize + cellSize / 2f
                            ),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }

            // Draw habit labels on axes
            for (i in habits.indices) {
                // Y-axis labels (rows)
                drawText(
                    textMeasurer = textMeasurer,
                    text = habits[i].name.take(10),
                    topLeft = Offset(0f, i * cellSize + cellSize / 3f),
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                // X-axis labels (columns)
                rotate(90f) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = habits[i].name.take(10),
                        topLeft = Offset(0f, -((i + 1) * cellSize) + cellSize / 3f),
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Matrix controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { rotationX = (rotationX + 10) % 360 },
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Rotate X+"
                )
            }

            IconButton(
                onClick = { rotationX = (rotationX - 10 + 360) % 360 },
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Rotate X-"
                )
            }

            IconButton(
                onClick = { rotationY = (rotationY + 10) % 360 },
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Rotate Y+"
                )
            }

            IconButton(
                onClick = { rotationY = (rotationY - 10 + 360) % 360 },
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Rotate Y-"
                )
            }

            IconButton(
                onClick = {
                    rotationX = 20f
                    rotationY = 20f
                },
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset View"
                )
            }
        }

        // Legend
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                text = "Correlation Legend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Green.copy(alpha = 0.7f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Strong Positive (>0.7)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Yellow.copy(alpha = 0.7f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Moderate Positive (>0.3)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Weak/No Correlation",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Orange.copy(alpha = 0.7f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Moderate Negative (>-0.7)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Red.copy(alpha = 0.7f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Strong Negative (<-0.7)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * A component that displays analytics insights with interactive elements
 */
@Composable
fun AnalyticsInsightsPanel(
    insights: List<AnalyticsInsight>,
    modifier: Modifier = Modifier,
    onInsightClick: (AnalyticsInsight) -> Unit = {}
) {
    var expandedInsightId by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Analytics Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(insights.sortedByDescending { it.confidence }) { insight ->
                    val isExpanded = expandedInsightId == insight.id

                    InsightCard(
                        insight = insight,
                        isExpanded = isExpanded,
                        onClick = {
                            expandedInsightId = if (isExpanded) null else insight.id
                            onInsightClick(insight)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual insight card with animation
 */
@Composable
fun InsightCard(
    insight: AnalyticsInsight,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when (insight.type) {
        InsightType.TREND_DETECTION -> MaterialTheme.colorScheme.primary
        InsightType.PATTERN_RECOGNITION -> MaterialTheme.colorScheme.secondary
        InsightType.CORRELATION -> MaterialTheme.colorScheme.tertiary
        InsightType.ANOMALY_DETECTION -> MaterialTheme.colorScheme.error
        InsightType.PREDICTION -> MaterialTheme.colorScheme.primaryContainer
        InsightType.RECOMMENDATION -> MaterialTheme.colorScheme.secondaryContainer
        InsightType.ACHIEVEMENT -> MaterialTheme.colorScheme.tertiaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Insight icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (insight.type) {
                            InsightType.TREND_DETECTION -> Icons.Default.TrendingUp
                            InsightType.PATTERN_RECOGNITION -> Icons.Default.Timeline
                            InsightType.CORRELATION -> Icons.Default.Compare
                            InsightType.ANOMALY_DETECTION -> Icons.Default.Warning
                            InsightType.PREDICTION -> Icons.Default.Visibility
                            InsightType.RECOMMENDATION -> Icons.Default.Lightbulb
                            InsightType.ACHIEVEMENT -> Icons.Default.EmojiEvents
                        },
                        contentDescription = insight.type.name,
                        tint = backgroundColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Insight title and score
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = insight.type.name.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Score indicator
                        LinearProgressIndicator(
                            progress = insight.confidence,
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp),
                            color = backgroundColor,
                            trackColor = backgroundColor.copy(alpha = 0.2f)
                        )
                    }
                }

                // Expand/collapse icon
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Expanded content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (insight.relatedHabitIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Related Habits: ${insight.relatedHabitIds.size}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Timestamp
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Generated: ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(insight.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

package com.example.myapplication.features.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.*

/**
 * Displays a calendar heatmap visualization of habit completion
 */
@Composable
fun CalendarHeatmap(
    completionDates: List<Date>,
    startDate: Date = Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000), // Last 30 days
    endDate: Date = Date(),
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    onDateSelected: (Date) -> Unit = {}
) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Calculate the number of days to display
    calendar.time = startDate
    val startMillis = calendar.timeInMillis
    calendar.time = endDate
    val endMillis = calendar.timeInMillis
    val dayCount = ((endMillis - startMillis) / (24 * 60 * 60 * 1000)).toInt() + 1

    // Create a map of dates to completion counts
    val dateCountMap = completionDates
        .map { dateFormat.format(it) }
        .groupingBy { it }
        .eachCount()

    // Animation for cell appearance
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(completionDates) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = androidx.compose.animation.core.EaseOutQuart
            )
        )
    }

    // Calculate the number of weeks to display
    val weeksCount = ceil(dayCount / 7f).toInt()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height((weeksCount * 20 + 40).dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Day labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            dayLabels.forEach { day ->
                Text(
                    text = day,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        }

        // Heatmap grid
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Calculate which cell was tapped
                        val cellWidth = size.width / 7
                        val cellHeight = size.height / weeksCount

                        val col = (offset.x / cellWidth).toInt()
                        val row = (offset.y / cellHeight).toInt()

                        // Calculate the date for the tapped cell
                        calendar.time = startDate
                        calendar.add(Calendar.DAY_OF_YEAR, row * 7 + col)

                        if (calendar.time.before(endDate) || calendar.time == endDate) {
                            onDateSelected(calendar.time)
                        }
                    }
                }
        ) {
            val cellWidth = size.width / 7
            val cellHeight = size.height / weeksCount

            // Draw the heatmap cells
            for (week in 0 until weeksCount) {
                for (day in 0 until 7) {
                    // Calculate the date for this cell
                    calendar.time = startDate
                    calendar.add(Calendar.DAY_OF_YEAR, week * 7 + day)

                    // Skip if the date is after the end date
                    if (calendar.time.after(endDate)) continue

                    val dateStr = dateFormat.format(calendar.time)
                    val completionCount = dateCountMap[dateStr] ?: 0

                    // Calculate color intensity based on completion count
                    val colorIntensity = when {
                        completionCount == 0 -> 0.1f
                        completionCount == 1 -> 0.4f
                        completionCount == 2 -> 0.7f
                        else -> 1.0f
                    }

                    val cellColor = primaryColor.copy(
                        alpha = colorIntensity * animatedProgress.value
                    )

                    // Draw the cell
                    drawRect(
                        color = cellColor,
                        topLeft = Offset(day * cellWidth, week * cellHeight),
                        size = Size(cellWidth * 0.9f, cellHeight * 0.9f),
                        style = Fill
                    )
                }
            }
        }
    }
}

/**
 * Displays a radial progress chart with animations
 */
@Composable
fun RadialProgressChart(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    strokeWidth: Float = 20f,
    animationDuration: Int = 1000,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showPercentage: Boolean = true
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = androidx.compose.animation.core.EaseOutQuart
            )
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = (canvasWidth.coerceAtMost(canvasHeight) - strokeWidth) / 2
            val center = Offset(canvasWidth / 2, canvasHeight / 2)

            // Draw background circle
            drawCircle(
                color = secondaryColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Draw progress arc
            val sweepAngle = animatedProgress.value * 360f

            drawArc(
                color = primaryColor,
                startAngle = -90f, // Start from top
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        if (showPercentage) {
            Text(
                text = "${(animatedProgress.value * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Displays a streak timeline visualization
 */
@Composable
fun StreakTimeline(
    streakData: List<Pair<Date, Boolean>>, // Date and whether the habit was completed
    modifier: Modifier = Modifier,
    completedColor: Color = MaterialTheme.colorScheme.primary,
    missedColor: Color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
) {
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    // Text measurer for drawing text
    val textMeasurer = rememberTextMeasurer()

    // Extract theme colors before Canvas
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    // Animation for timeline appearance
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(streakData) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = androidx.compose.animation.core.EaseOutQuart
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            if (streakData.isEmpty()) return@Canvas

            val canvasWidth = size.width
            val canvasHeight = size.height
            val lineY = canvasHeight / 2

            // Draw the timeline line
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(0f, lineY),
                end = Offset(canvasWidth, lineY),
                strokeWidth = 2f
            )

            // Calculate spacing between points
            val pointSpacing = canvasWidth / (streakData.size - 1).coerceAtLeast(1)

            // Draw the streak points and connections
            var currentStreak = 0
            var longestStreak = 0

            streakData.forEachIndexed { index, (date, completed) ->
                val x = index * pointSpacing

                // Update streak count
                if (completed) {
                    currentStreak++
                    longestStreak = maxOf(longestStreak, currentStreak)
                } else {
                    currentStreak = 0
                }

                // Draw connection line to previous point if both are completed
                if (index > 0 && completed && streakData[index - 1].second) {
                    drawLine(
                        color = completedColor.copy(alpha = 0.7f * animatedProgress.value),
                        start = Offset((index - 1) * pointSpacing, lineY),
                        end = Offset(x, lineY),
                        strokeWidth = 4f
                    )
                }

                // Draw the point
                val pointColor = if (completed) completedColor else missedColor
                val pointRadius = if (completed) 8f else 6f

                drawCircle(
                    color = pointColor.copy(alpha = animatedProgress.value),
                    radius = pointRadius,
                    center = Offset(x, lineY)
                )

                // Draw date label for some points (e.g., every 5th point)
                if (index % 5 == 0 || index == streakData.size - 1) {
                    val dateText = dateFormat.format(date)

                    // Draw date below the line
                    drawText(
                        textMeasurer = textMeasurer,
                        text = dateText,
                        topLeft = Offset(x - 20, lineY + 15),
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            // Draw the longest streak label
            drawText(
                textMeasurer = textMeasurer,
                text = "Longest Streak: $longestStreak",
                topLeft = Offset(10f, 10f),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = onBackgroundColor
                )
            )
        }
    }
}

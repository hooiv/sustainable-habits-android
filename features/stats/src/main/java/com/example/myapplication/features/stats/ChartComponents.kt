package com.example.myapplication.features.stats

import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.core.data.model.Habit
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun HabitCategoryBarChart(
    habits: List<Habit>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val onSurfaceComposeColor = scheme.onSurface
    val primaryComposeColor = scheme.primary
    val surfaceVariantComposeColor = scheme.surfaceVariant
    val surfaceVariantAlpha02ComposeColor = scheme.surfaceVariant.copy(alpha = 0.2f)
    val onSurfaceVariantAlpha05ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.5f)
    val onSurfaceVariantAlpha07ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    val gridLinesComposeColor = scheme.onSurface.copy(alpha = 0.1f)

    val onSurfaceArgb = onSurfaceComposeColor.toArgb()
    val primaryArgb = primaryComposeColor.toArgb()
    val surfaceVariantArgb = surfaceVariantComposeColor.toArgb()
    val gridLinesArgb = gridLinesComposeColor.toArgb()

    val categoryCounts = habits
        .filter { !it.category.isNullOrEmpty() }
        .groupBy { it.category ?: "Uncategorized" }
        .mapValues { (_, habitList) ->
            habitList.count { it.isEnabled } to habitList.count { !it.isEnabled }
        }

    if (categoryCounts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    surfaceVariantAlpha02ComposeColor,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "No Data",
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(0.5f),
                    tint = onSurfaceVariantAlpha05ComposeColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No category data available",
                    textAlign = TextAlign.Center,
                    color = onSurfaceVariantAlpha07ComposeColor
                )
            }
        }
    } else {
        AndroidView(
            factory = { BarChart(context) },
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(surfaceVariantAlpha02ComposeColor)
                .padding(8.dp)
        ) { barChart ->
            barChart.apply {
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    textColor = onSurfaceArgb
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                }

                setDrawGridBackground(false)
                setDrawBarShadow(false)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    textColor = onSurfaceArgb
                    textSize = 10f
                    valueFormatter = IndexAxisValueFormatter(categoryCounts.keys.toList())
                    setDrawGridLines(false)
                }

                axisLeft.apply {
                    textColor = onSurfaceArgb
                    textSize = 10f
                    axisMinimum = 0f
                    granularity = 1f
                    setDrawGridLines(true)
                    gridColor = gridLinesArgb
                }
                axisRight.isEnabled = false
                setScaleEnabled(true)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled = true
            }

            val enabledEntries = mutableListOf<BarEntry>()
            val disabledEntries = mutableListOf<BarEntry>()

            categoryCounts.values.forEachIndexed { index, (enabled, disabled) ->
                enabledEntries.add(BarEntry(index.toFloat(), enabled.toFloat()))
                disabledEntries.add(BarEntry(index.toFloat(), disabled.toFloat()))
            }

            val enabledDataSet = BarDataSet(enabledEntries, "Active").apply {
                color = primaryArgb
                valueTextColor = onSurfaceArgb
                valueTextSize = 10f
                setDrawValues(true)
            }

            val disabledDataSet = BarDataSet(disabledEntries, "Paused").apply {
                color = surfaceVariantArgb
                valueTextColor = onSurfaceArgb
                valueTextSize = 10f
                setDrawValues(true)
            }

            val groupSpace = 0.3f
            val barSpace = 0.05f
            val barWidth = 0.3f
            val start = 0f

            val barData = BarData(enabledDataSet, disabledDataSet)
            barData.barWidth = barWidth

            barChart.data = barData
            barChart.groupBars(start, groupSpace, barSpace)
            barChart.setVisibleXRangeMaximum(5f)
            barChart.setFitBars(true)

            barChart.animateY(1400)
            barChart.invalidate()
        }
    }
}

@Composable
fun HabitTrendsLineChart(
    habits: List<Habit>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val onSurfaceComposeColor = scheme.onSurface
    val primaryComposeColor = scheme.primary
    val tertiaryComposeColor = scheme.tertiary
    val surfaceVariantAlpha02ComposeColor = scheme.surfaceVariant.copy(alpha = 0.2f)
    val onSurfaceVariantAlpha05ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.5f)
    val onSurfaceVariantAlpha07ComposeColor = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    val gridLinesComposeColor = scheme.onSurface.copy(alpha = 0.1f)
    val primaryAlpha05ComposeColor = scheme.primary.copy(alpha = 0.5f)
    val primaryAlpha01ComposeColor = scheme.primary.copy(alpha = 0.1f)
    val tertiaryAlpha05ComposeColor = scheme.tertiary.copy(alpha = 0.5f)
    val tertiaryAlpha01ComposeColor = scheme.tertiary.copy(alpha = 0.1f)

    val onSurfaceArgb = onSurfaceComposeColor.toArgb()
    val primaryArgb = primaryComposeColor.toArgb()
    val tertiaryArgb = tertiaryComposeColor.toArgb()
    val gridLinesArgb = gridLinesComposeColor.toArgb()
    val primaryAlpha05Argb = primaryAlpha05ComposeColor.toArgb()
    val primaryAlpha01Argb = primaryAlpha01ComposeColor.toArgb()
    val tertiaryAlpha05Argb = tertiaryAlpha05ComposeColor.toArgb()
    val tertiaryAlpha01Argb = tertiaryAlpha01ComposeColor.toArgb()

    if (habits.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    surfaceVariantAlpha02ComposeColor,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = "No Data",
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(0.5f),
                    tint = onSurfaceVariantAlpha05ComposeColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Not enough data to show trends",
                    textAlign = TextAlign.Center,
                    color = onSurfaceVariantAlpha07ComposeColor
                )
            }
        }
    } else {
        AndroidView(
            factory = { LineChart(context) },
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(surfaceVariantAlpha02ComposeColor)
                .padding(8.dp)
        ) { lineChart ->
            lineChart.apply {
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    textColor = onSurfaceArgb
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                }

                setDrawGridBackground(false)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = onSurfaceArgb
                    textSize = 10f
                    valueFormatter = IndexAxisValueFormatter(
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    )
                    setDrawGridLines(false)
                }

                axisLeft.apply {
                    textColor = onSurfaceArgb
                    textSize = 10f
                    axisMinimum = 0f
                    granularity = 1f
                    setDrawGridLines(true)
                    gridColor = gridLinesArgb
                }
                axisRight.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
            }

            val completionEntries = (0..6).map { day ->
                val value = (habits.size * (0.3f + (day % 3) * 0.2f)).toFloat() // Dummy data
                Entry(day.toFloat(), value)
            }

            val streakEntries = (0..6).map { day ->
                val value = habits.sumOf { it.streak }.toFloat() / habits.size.coerceAtLeast(1) // Dummy data
                Entry(day.toFloat(), value * (1 + day % 2) * 0.2f)
            }

            val completionDataSet = LineDataSet(completionEntries, "Daily Completions").apply {
                color = primaryArgb
                lineWidth = 2f
                setDrawValues(false)
                setDrawCircles(true)
                setCircleColor(primaryArgb)
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleRadius = 2f
                setDrawFilled(true)
                fillDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        primaryAlpha05Argb,
                        primaryAlpha01Argb
                    )
                )
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            val streakDataSet = LineDataSet(streakEntries, "Avg. Streak Length").apply {
                color = tertiaryArgb
                lineWidth = 2f
                setDrawValues(false)
                setDrawCircles(true)
                setCircleColor(tertiaryArgb)
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleRadius = 2f
                setDrawFilled(true)
                fillDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        tertiaryAlpha05Argb,
                        tertiaryAlpha01Argb
                    )
                )
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            lineChart.data = LineData(completionDataSet, streakDataSet)
            lineChart.animateX(1500)
            lineChart.invalidate()
        }
    }
}

package com.example.myapplication.features.stats

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.navigation.NavRoutes
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.features.habits.HabitViewModel
import com.example.myapplication.data.model.Habit
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    val viewModel: HabitViewModel = hiltViewModel()
    val habits by viewModel.habits.collectAsState(emptyList())
    val completedCount = habits.count { it.goalProgress >= it.goal }
    val totalCount = habits.size
    val completionRate = if (totalCount > 0) completedCount * 100f / totalCount else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Habits Completed: $completedCount/$totalCount",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Completion Rate: ${completionRate.roundToInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF388E3C),
                modifier = Modifier.padding(bottom = 24.dp)
            )
            // Simple animated chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CompletionPieChart(completedCount, totalCount)
            }
        }
    }
}

@Composable
fun CompletionPieChart(completed: Int, total: Int) {
    AndroidView(
        factory = { context -> PieChart(context) },
        modifier = Modifier.size(200.dp)
    ) { pieChart ->
        val entries = listOf(
            PieEntry(completed.toFloat(), "Completed"),
            PieEntry((total - completed).toFloat(), "Remaining")
        )
        val dataSet = PieDataSet(entries, "Completion Rate")
        dataSet.colors = listOf(Color(0xFF388E3C).toArgb(), Color.LightGray.toArgb())
        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }
}

@Composable
fun HabitStatsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Habit Statistics",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Pie Chart for Completion Rate
        Text(
            text = "Completion Rate",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        CompletionPieChart(completed = 7, total = 10)

        Spacer(modifier = Modifier.height(16.dp))

        // Bar Chart for Weekly Completion Trends
        val weeklyData = listOf(
            BarEntry(1f, 3f),
            BarEntry(2f, 5f),
            BarEntry(3f, 2f),
            BarEntry(4f, 4f),
            BarEntry(5f, 6f),
            BarEntry(6f, 1f),
            BarEntry(7f, 3f)
        )

        Text(
            text = "Weekly Completion Trends",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AndroidView(
            factory = { context -> BarChart(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { barChart ->
            val dataSet = BarDataSet(weeklyData, "Weekly Trends")
            dataSet.colors = listOf(Color(0xFF388E3C).toArgb())
            barChart.data = BarData(dataSet)
            barChart.invalidate()
        }
    }
}
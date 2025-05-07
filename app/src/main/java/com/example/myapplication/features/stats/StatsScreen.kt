package com.example.myapplication.features.stats

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.features.habits.HabitViewModel
import com.example.myapplication.data.model.Habit
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.roundToInt
import com.tehras.charts.piechart.PieChart
import com.tehras.charts.linechart.LineChart

@Composable
fun StatsScreen(navController: NavController, viewModel: HabitViewModel = viewModel()) {
    val habits by viewModel.habits.observeAsState(emptyList())
    val completedCount = habits.count { it.isCompleted }
    val totalCount = habits.size
    val completionRate = if (totalCount > 0) completedCount * 100f / totalCount else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
    val sweepAngle = if (total > 0) 360f * completed / total else 0f
    Canvas(modifier = Modifier.size(160.dp)) {
        // Background circle
        drawArc(
            color = Color.LightGray,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = true,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height)
        )
        // Completed arc
        drawArc(
            color = Color(0xFF388E3C),
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height)
        )
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
        PieChart(
            pieChartData = listOf(0.7f, 0.3f), // Example data
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Line Chart for Streak History
        Text(
            text = "Streak History",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LineChart(
            lineChartData = listOf(1f, 2f, 3f, 4f, 5f), // Example data
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}
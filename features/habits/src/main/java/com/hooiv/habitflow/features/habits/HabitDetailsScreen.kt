package com.hooiv.habitflow.features.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hooiv.habitflow.core.ui.components.HabitStrengthGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailsScreen(
    habitId: String,
    onBackClick: () -> Unit,
    viewModel: HabitCompletionViewModel = hiltViewModel()
) {
    LaunchedEffect(habitId) {
        viewModel.loadCompletionsForHabit(habitId)
    }

    val completions by viewModel.completions.collectAsState()

    // Derive normalised data points (0..1) from completion timestamps.
    // Each completion date maps to a fraction within the observed date range.
    val dataPoints: List<Float> = run {
        val timestamps = completions.map { it.completionDate }.sorted()
        when {
            // No history yet — render a single mid-height placeholder point
            timestamps.isEmpty() -> listOf(0.5f)
            timestamps.size == 1 -> listOf(1.0f)
            else -> {
                val minTs = timestamps.first()
                val maxTs = timestamps.last()
                if (maxTs == minTs) {
                    // All completions at the exact same timestamp
                    List(timestamps.size) { 1.0f }
                } else {
                    val range = (maxTs - minTs).toFloat()
                    timestamps.map { ((it - minTs) / range).toFloat() }
                }
            }
        }
    }

    val completionWord = if (completions.size == 1) "completion" else "completions"
    val analysisText = if (completions.isEmpty())
        "Complete this habit to start building your consistency graph."
    else
        "Based on ${completions.size} recorded $completionWord. The curve shows how your completion frequency has evolved over time."

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Consistency Trend",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // The Custom Graph Component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                HabitStrengthGraph(
                    dataPoints = dataPoints,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Analysis",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = analysisText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@androidx.compose.ui.tooling.preview.Preview(
    name = "HabitDetails — Phone Light",
    showBackground = true, widthDp = 360, heightDp = 800
)
@androidx.compose.runtime.Composable
private fun HabitDetailsScreenPreview() {
    com.hooiv.habitflow.core.ui.theme.MyApplicationTheme(darkTheme = false) {
        HabitDetailsScreen(habitId = "preview", onBackClick = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "HabitDetails — Phone Dark",
    showBackground = true, widthDp = 360, heightDp = 800,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@androidx.compose.runtime.Composable
private fun HabitDetailsScreenDarkPreview() {
    com.hooiv.habitflow.core.ui.theme.MyApplicationTheme(darkTheme = true) {
        HabitDetailsScreen(habitId = "preview", onBackClick = {})
    }
}

package com.example.myapplication.features.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.time.LocalDate

@Composable
fun HabitCalendarScreen(
    habitName: String,
    completionHistory: List<LocalDate>,
    onDayMarked: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Calendar functionality is under construction.")
        // Placeholder for future calendar implementation
    }
}

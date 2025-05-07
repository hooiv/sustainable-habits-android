package com.example.myapplication.features.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.CalendarView
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HabitCalendarScreen(
    habitName: String,
    completionHistory: List<LocalDate>,
    onDayMarked: (LocalDate) -> Unit
) {
    val currentMonth = YearMonth.now()
    val startMonth = currentMonth.minusMonths(12)
    val endMonth = currentMonth.plusMonths(12)
    val daysOfWeek = daysOfWeek(firstDayOfWeek = java.time.DayOfWeek.SUNDAY)
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth.atStartOfMonth()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Habit Calendar: $habitName",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CalendarView(
            state = calendarState,
            dayContent = { day ->
                val isCompleted = completionHistory.contains(day.date)
                val isMissed = day.date.isBefore(LocalDate.now()) && !isCompleted

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                        .clickable {
                            if (!isCompleted) {
                                onDayMarked(day.date) // Callback to mark the day as complete
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isMissed -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onBackground
                        }
                    )
                }
            },
            monthHeader = { month ->
                Text(
                    text = month.yearMonth.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        )
    }
}
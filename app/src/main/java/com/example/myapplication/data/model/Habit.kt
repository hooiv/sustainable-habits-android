package com.example.myapplication.data.model

import java.util.UUID

// Using java.util.Date for simplicity, can be replaced with kotlinx-datetime later if needed
import java.util.Date

data class Habit(
    val id: String = UUID.randomUUID().toString(), // Unique ID for each habit
    val name: String, // Name of the habit, e.g., "Used reusable coffee cup"
    val description: String? = null, // Optional longer description
    val frequency: HabitFrequency = HabitFrequency.DAILY, // How often the habit is tracked
    val goal: Int = 1, // Target number of times per frequency period (e.g., 1 time per day)
    val streak: Int = 0, // Current streak of completing the habit
    val lastCompletedDate: Date? = null, // Last date the habit was marked as completed
    val createdDate: Date = Date() // Date the habit was created
)

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

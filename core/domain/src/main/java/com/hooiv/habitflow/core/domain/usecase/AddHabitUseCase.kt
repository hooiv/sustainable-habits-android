package com.hooiv.habitflow.core.domain.usecase

import com.hooiv.habitflow.core.data.model.Habit
import com.hooiv.habitflow.core.data.model.HabitFrequency
import com.hooiv.habitflow.core.data.repository.HabitRepository
import javax.inject.Inject

/**
 * Creates a new habit with sensible defaults and persists it via the repository.
 */
class AddHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String?,
        category: String?,
        frequency: HabitFrequency,
        goal: Int = 1,
        reminderTime: String? = null
    ) {
        repository.insertHabit(
            Habit(
                name = name,
                description = description?.takeIf { it.isNotBlank() },
                category = category,
                frequency = frequency,
                goal = goal,
                reminderTime = reminderTime
                // All other fields use default values defined in Habit data class
            )
        )
    }
}

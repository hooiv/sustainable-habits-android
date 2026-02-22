package com.hooiv.habitflow.core.domain.usecase

import com.hooiv.habitflow.core.data.model.Habit
import com.hooiv.habitflow.core.data.model.HabitFrequency
import com.hooiv.habitflow.core.data.repository.HabitRepository
import java.util.Date
import javax.inject.Inject

/**
 * Use case for creating a new habit.
 * Encapsulates the creation logic including setting sensible defaults.
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
        val newHabit = Habit(
            name = name,
            description = description?.takeIf { it.isNotBlank() },
            category = category,
            frequency = frequency,
            createdDate = Date(),
            goal = goal,
            goalProgress = 0,
            streak = 0,
            completionHistory = mutableListOf(),
            isEnabled = true,
            reminderTime = reminderTime
        )
        repository.insertHabit(newHabit)
    }
}

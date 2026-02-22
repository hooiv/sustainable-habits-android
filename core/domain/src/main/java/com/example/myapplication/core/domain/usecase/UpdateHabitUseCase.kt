package com.hooiv.habitflow.core.domain.usecase

import com.hooiv.habitflow.core.data.model.Habit
import com.hooiv.habitflow.core.data.repository.HabitRepository
import javax.inject.Inject

/**
 * Use case for updating an existing habit.
 */
class UpdateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit) {
        repository.updateHabit(habit)
    }
}

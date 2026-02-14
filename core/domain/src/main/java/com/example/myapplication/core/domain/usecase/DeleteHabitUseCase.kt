package com.example.myapplication.core.domain.usecase

import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.repository.HabitRepository
import javax.inject.Inject

/**
 * Use case for deleting a habit.
 */
class DeleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit) {
        repository.deleteHabit(habit)
    }
}

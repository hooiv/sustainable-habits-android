package com.hooiv.habitflow.core.domain.usecase

import com.hooiv.habitflow.core.data.repository.HabitRepository
import javax.inject.Inject

/**
 * Use case for marking a habit as completed for the current day.
 * Delegates to the repository which handles streak and progress calculations.
 */
class MarkHabitCompletedUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String) {
        repository.markHabitCompleted(habitId)
    }
}

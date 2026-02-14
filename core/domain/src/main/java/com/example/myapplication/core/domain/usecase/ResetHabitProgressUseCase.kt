package com.example.myapplication.core.domain.usecase

import com.example.myapplication.core.data.repository.HabitRepository
import java.util.Date
import javax.inject.Inject

/**
 * Use case for resetting habit progress when a new period begins.
 * Checks if the habit's goal period has elapsed and resets progress if needed.
 */
class ResetHabitProgressUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String, currentDate: Date = Date()) {
        repository.resetHabitProgressIfNeeded(habitId, currentDate)
    }
}

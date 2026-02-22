package com.hooiv.habitflow.core.domain.usecase

import com.hooiv.habitflow.core.data.model.Habit
import com.hooiv.habitflow.core.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve all habits as a reactive Flow.
 * This is the single source of truth for listing habits.
 */
class GetHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<List<Habit>> {
        return repository.getAllHabits()
    }
}

package com.hooiv.habitflow.core.domain.usecase

import com.hooiv.habitflow.core.data.model.Habit
import com.hooiv.habitflow.core.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a single habit by its ID.
 */
class GetHabitByIdUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(habitId: String): Flow<Habit?> {
        return repository.getHabitById(habitId)
    }
}

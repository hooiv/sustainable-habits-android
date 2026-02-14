package com.example.myapplication.core.domain.usecase

import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.repository.HabitRepository
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

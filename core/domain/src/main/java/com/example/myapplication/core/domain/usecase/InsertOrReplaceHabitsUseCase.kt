package com.example.myapplication.core.domain.usecase

import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.repository.HabitRepository
import javax.inject.Inject

/**
 * Use case for bulk inserting or replacing habits.
 * Used when restoring habits from cloud backup — preserves original IDs.
 */
class InsertOrReplaceHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habits: List<Habit>) {
        repository.insertOrReplaceHabits(habits)
    }
}

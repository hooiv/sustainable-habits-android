package com.example.myapplication.core.domain.usecase

import com.example.myapplication.core.data.repository.HabitRepository
import javax.inject.Inject

/**
 * Use case for toggling a habit's enabled/disabled status.
 */
class ToggleHabitEnabledUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val getHabitByIdUseCase: GetHabitByIdUseCase
) {
    suspend operator fun invoke(habitId: String) {
        getHabitByIdUseCase(habitId).collect { habit ->
            habit?.let {
                repository.updateHabit(it.copy(isEnabled = !it.isEnabled))
            }
        }
    }
}

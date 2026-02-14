package com.example.myapplication.features.habits

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitFrequency
import com.example.myapplication.core.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for habit management screens.
 * Delegates all business logic to domain-layer use cases.
 */
@HiltViewModel
class HabitViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val addHabitUseCase: AddHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val getHabitByIdUseCase: GetHabitByIdUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val markHabitCompletedUseCase: MarkHabitCompletedUseCase,
    private val resetHabitProgressUseCase: ResetHabitProgressUseCase
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = getHabitsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addHabit(
        name: String,
        description: String?,
        category: String?,
        frequency: HabitFrequency,
        goal: Int = 1,
        reminderTime: String? = null
    ) {
        Log.d("HabitViewModel", "addHabit called. Name: $name, Freq: $frequency, Goal: $goal")
        viewModelScope.launch {
            addHabitUseCase(name, description, category, frequency, goal, reminderTime)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            deleteHabitUseCase(habit)
        }
    }

    fun getHabitById(habitId: String) = getHabitByIdUseCase(habitId)

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            updateHabitUseCase(habit)
        }
    }

    fun restoreHabits(habitsToRestore: List<Habit>) {
        viewModelScope.launch {
            Log.d("HabitViewModel", "Restoring ${habitsToRestore.size} habits.")
            habitsToRestore.forEach { updateHabitUseCase(it) }
        }
    }

    fun markHabitCompleted(habitId: String) {
        Log.d("HabitViewModel", "Marking habit as completed: $habitId")
        viewModelScope.launch {
            markHabitCompletedUseCase(habitId)
        }
    }

    fun checkAndResetHabitProgress(habitId: String) {
        viewModelScope.launch {
            resetHabitProgressUseCase(habitId)
        }
    }

    fun toggleHabitEnabled(habit: Habit) {
        viewModelScope.launch {
            updateHabitUseCase(habit.copy(isEnabled = !habit.isEnabled))
        }
    }

    fun updateHabitReminder(habitId: String, reminderTime: String?) {
        viewModelScope.launch {
            getHabitByIdUseCase(habitId).collect { habit ->
                habit?.let {
                    updateHabitUseCase(it.copy(reminderTime = reminderTime))
                }
            }
        }
    }
}

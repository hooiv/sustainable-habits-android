package com.example.myapplication.features.habits

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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import com.example.myapplication.core.di.IoDispatcher

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
    private val insertOrReplaceHabitsUseCase: InsertOrReplaceHabitsUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = getHabitsUseCase()
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Drop upstream after 5s background
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
            insertOrReplaceHabitsUseCase(habitsToRestore)
        }
    }

    fun markHabitCompleted(habitId: String) {
        viewModelScope.launch {
            markHabitCompletedUseCase(habitId)
        }
    }

    fun toggleHabitEnabled(habit: Habit) {
        viewModelScope.launch {
            updateHabitUseCase(habit.copy(isEnabled = !habit.isEnabled))
        }
    }
}

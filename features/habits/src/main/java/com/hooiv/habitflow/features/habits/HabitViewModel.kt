package com.hooiv.habitflow.features.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooiv.habitflow.core.data.model.Habit
import com.hooiv.habitflow.core.data.model.HabitFrequency
import com.hooiv.habitflow.core.di.IoDispatcher
import com.hooiv.habitflow.core.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One-shot events emitted to the UI (e.g. snackbar messages on failure). */
sealed interface HabitUiEvent {
    data class Error(val message: String) : HabitUiEvent
    data object HabitAdded    : HabitUiEvent
    data object HabitDeleted  : HabitUiEvent
    data object HabitUpdated  : HabitUiEvent
    data object HabitCompleted: HabitUiEvent
}

/**
 * ViewModel for habit management screens.
 *
 * Exposes:
 *  - [habits]  — a stable [StateFlow] backed by Room's reactive query.
 *  - [uiEvents] — a one-shot [Channel]-backed Flow for snackbar / toast messages.
 *
 * All mutations are fire-and-forget from the UI's perspective; errors surface via [uiEvents].
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
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiEvents = Channel<HabitUiEvent>(Channel.BUFFERED)
    /** Collect this in the UI to handle one-shot events (snackbars, navigation, etc.). */
    val uiEvents = _uiEvents.receiveAsFlow()

    fun addHabit(
        name: String,
        description: String?,
        category: String?,
        frequency: HabitFrequency,
        goal: Int = 1,
        reminderTime: String? = null
    ) {
        viewModelScope.launch {
            runCatching { addHabitUseCase(name, description, category, frequency, goal, reminderTime) }
                .onSuccess { _uiEvents.send(HabitUiEvent.HabitAdded) }
                .onFailure { _uiEvents.send(HabitUiEvent.Error("Could not add habit: ${it.localizedMessage}")) }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            runCatching { deleteHabitUseCase(habit) }
                .onSuccess { _uiEvents.send(HabitUiEvent.HabitDeleted) }
                .onFailure { _uiEvents.send(HabitUiEvent.Error("Could not delete habit: ${it.localizedMessage}")) }
        }
    }

    fun getHabitById(habitId: String) = getHabitByIdUseCase(habitId)

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            runCatching { updateHabitUseCase(habit) }
                .onSuccess { _uiEvents.send(HabitUiEvent.HabitUpdated) }
                .onFailure { _uiEvents.send(HabitUiEvent.Error("Could not update habit: ${it.localizedMessage}")) }
        }
    }

    fun restoreHabits(habitsToRestore: List<Habit>) {
        viewModelScope.launch {
            runCatching { insertOrReplaceHabitsUseCase(habitsToRestore) }
                .onFailure { _uiEvents.send(HabitUiEvent.Error("Could not restore habits: ${it.localizedMessage}")) }
        }
    }

    fun markHabitCompleted(habitId: String) {
        viewModelScope.launch {
            runCatching { markHabitCompletedUseCase(habitId) }
                .onSuccess { _uiEvents.send(HabitUiEvent.HabitCompleted) }
                .onFailure { _uiEvents.send(HabitUiEvent.Error("Could not mark habit complete: ${it.localizedMessage}")) }
        }
    }

    fun toggleHabitEnabled(habit: Habit) {
        viewModelScope.launch {
            runCatching { updateHabitUseCase(habit.copy(isEnabled = !habit.isEnabled)) }
                .onFailure { _uiEvents.send(HabitUiEvent.Error("Could not update habit: ${it.localizedMessage}")) }
        }
    }
}

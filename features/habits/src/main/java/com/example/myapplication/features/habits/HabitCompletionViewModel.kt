package com.example.myapplication.features.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for handling habit completion operations
 */
@HiltViewModel
class HabitCompletionViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _completions = MutableStateFlow<List<HabitCompletion>>(emptyList())
    val completions: StateFlow<List<HabitCompletion>> = _completions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** The currently active completions-loading job; cancelled before a new one starts. */
    private var loadJob: Job? = null

    /**
     * Load completions for a specific habit.
     * Room's Flow pushes every subsequent DB change automatically, so this only
     * needs to be called once per screen session (e.g. from LaunchedEffect).
     * Cancels any previously-running collection to avoid duplicate subscribers.
     */
    fun loadCompletionsForHabit(habitId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                habitRepository.getHabitCompletions(habitId).collect { completionList ->
                    _completions.value = completionList
                    // Clear the loading indicator on the first (and every) emission.
                    // The finally block is NOT used here because collect() on a Room
                    // Flow never completes — finally would only run on cancellation.
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load completions: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a completion for a habit.
     * Room's reactive Flow emits the update automatically; no manual reload needed.
     */
    fun addCompletion(
        habitId: String,
        note: String? = null,
        mood: Int? = null,
        location: String? = null,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            try {
                habitRepository.markHabitCompleted(
                    habitId = habitId,
                    completionDate = Date(),
                    note = note,
                    mood = mood,
                    location = location,
                    photoUri = photoUri
                )
            } catch (e: Exception) {
                _error.value = "Failed to add completion: ${e.message}"
            }
        }
    }

    /**
     * Delete a completion.
     * Room's reactive Flow emits the update automatically; no manual reload needed.
     */
    fun deleteCompletion(completion: HabitCompletion) {
        viewModelScope.launch {
            try {
                habitRepository.deleteHabitCompletion(completion)
            } catch (e: Exception) {
                _error.value = "Failed to delete completion: ${e.message}"
            }
        }
    }

    /**
     * Update a completion.
     * Room's reactive Flow emits the update automatically; no manual reload needed.
     */
    fun updateCompletion(completion: HabitCompletion) {
        viewModelScope.launch {
            try {
                habitRepository.updateHabitCompletion(completion)
            } catch (e: Exception) {
                _error.value = "Failed to update completion: ${e.message}"
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}

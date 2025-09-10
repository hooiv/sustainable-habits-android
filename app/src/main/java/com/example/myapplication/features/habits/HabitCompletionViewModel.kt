package com.example.myapplication.features.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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

    /**
     * Load completions for a specific habit
     */
    fun loadCompletionsForHabit(habitId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                habitRepository.getHabitCompletions(habitId).collect { completionList ->
                    _completions.value = completionList
                }
            } catch (e: Exception) {
                _error.value = "Failed to load completions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load all completions
     */
    fun loadAllCompletions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                habitRepository.getAllCompletions().collect { completionList ->
                    _completions.value = completionList
                }
            } catch (e: Exception) {
                _error.value = "Failed to load completions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load completions in a date range
     */
    fun loadCompletionsInDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                habitRepository.getCompletionsInDateRange(startDate, endDate).collect { completionList ->
                    _completions.value = completionList
                }
            } catch (e: Exception) {
                _error.value = "Failed to load completions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a completion for a habit
     */
    fun addCompletion(
        habitId: String,
        note: String? = null,
        mood: Int? = null,
        location: String? = null,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                habitRepository.markHabitCompleted(
                    habitId = habitId,
                    completionDate = Date(),
                    note = note,
                    mood = mood,
                    location = location,
                    photoUri = photoUri
                )
                // Reload completions after adding a new one
                loadCompletionsForHabit(habitId)
            } catch (e: Exception) {
                _error.value = "Failed to add completion: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a completion
     */
    fun deleteCompletion(completion: HabitCompletion) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                habitRepository.deleteHabitCompletion(completion)
                // Reload completions after deleting one
                loadCompletionsForHabit(completion.habitId)
            } catch (e: Exception) {
                _error.value = "Failed to delete completion: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update a completion
     */
    fun updateCompletion(completion: HabitCompletion) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                habitRepository.updateHabitCompletion(completion)
                // Reload completions after updating one
                loadCompletionsForHabit(completion.habitId)
            } catch (e: Exception) {
                _error.value = "Failed to update completion: ${e.message}"
            } finally {
                _isLoading.value = false
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

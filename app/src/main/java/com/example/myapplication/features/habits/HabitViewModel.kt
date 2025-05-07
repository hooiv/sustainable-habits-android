package com.example.myapplication.features.habits

import android.util.Log // Import Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import com.example.myapplication.data.repository.HabitRepository // Import HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository // Inject HabitRepository
) : ViewModel() {

    // Expose habits from the repository, converting the Flow to a StateFlow
    val habits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep collecting for 5s after last subscriber
            initialValue = emptyList() // Initial value while the flow is loading
        )

    fun addHabit(name: String, description: String?, frequency: HabitFrequency) {
        Log.d("HabitViewModel", "addHabit called. Name: $name, Desc: $description, Freq: $frequency") // Add log statement
        viewModelScope.launch {
            val newHabit = Habit(
                name = name,
                description = description?.takeIf { it.isNotBlank() },
                frequency = frequency,
                createdDate = Date()
            )
            Log.d("HabitViewModel", "Inserting new habit: $newHabit") // Add log statement
            repository.insertHabit(newHabit) // Use repository to insert habit
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun getHabitById(habitId: String): kotlinx.coroutines.flow.Flow<Habit?> {
        return repository.getHabitById(habitId)
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }
}

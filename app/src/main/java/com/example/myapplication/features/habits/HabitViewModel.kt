package com.example.myapplication.features.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor() : ViewModel() {

    // Private MutableStateFlow to hold the list of habits
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    // Public StateFlow to expose the habits to the UI
    val habits: StateFlow<List<Habit>> = _habits

    init {
        // Load initial dummy habits
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            // Replace with actual data loading logic later (e.g., from a repository)
            _habits.value = listOf(
                Habit(name = "Use Reusable Water Bottle", frequency = HabitFrequency.DAILY, lastCompletedDate = Date()),
                Habit(name = "Cycle to Work", description = "Cycle at least 3 times a week", frequency = HabitFrequency.WEEKLY, goal = 3),
                Habit(name = "Meatless Monday", frequency = HabitFrequency.WEEKLY),
                Habit(name = "Reduce Plastic Usage", frequency = HabitFrequency.DAILY, streak = 5)
            )
        }
    }

    // TODO: Add functions to add, update, delete, and complete habits
}

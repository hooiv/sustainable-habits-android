package com.example.myapplication.navigation

object NavRoutes {
    const val HABIT_LIST = "habit_list"
    const val ADD_HABIT = "add_habit"
    const val EDIT_HABIT = "edit_habit/{habitId}"
    
    // Helper function for parameterized routes
    fun editHabit(habitId: String) = "edit_habit/$habitId"
}
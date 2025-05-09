package com.example.myapplication.navigation

object NavRoutes {
    const val HABIT_LIST = "habit_list"
    const val ADD_HABIT = "add_habit"
    const val EDIT_HABIT_ROUTE = "edit_habit" // Base route name
    const val EDIT_HABIT_ARG_ID = "habitId" // Argument name
    const val EDIT_HABIT = "$EDIT_HABIT_ROUTE/{$EDIT_HABIT_ARG_ID}" // Full route with argument placeholder
    const val STATS = "stats"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val SIGN_IN = "sign_in" // Added sign_in route
    const val ANIMATION_DEMO = "animation_demo" // Added animation demo route
    const val NEURAL_INTERFACE_ROUTE = "neural_interface" // Base route name
    const val NEURAL_INTERFACE_ARG_ID = "habitId" // Argument name
    const val NEURAL_INTERFACE = "$NEURAL_INTERFACE_ROUTE/{$NEURAL_INTERFACE_ARG_ID}" // Full route with argument placeholder

    // Helper function for parameterized routes
    fun editHabit(habitId: String) = "$EDIT_HABIT_ROUTE/$habitId"
    fun neuralInterface(habitId: String) = "$NEURAL_INTERFACE_ROUTE/$habitId"
}
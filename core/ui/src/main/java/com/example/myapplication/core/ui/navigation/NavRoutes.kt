package com.example.myapplication.core.ui.navigation

object NavRoutes {
    const val HABIT_LIST = "habit_list"
    const val HABIT_DETAILS = "habit_details/{habitId}"
    const val ADD_HABIT = "add_habit"
    const val EDIT_HABIT_ROUTE = "edit_habit"
    const val EDIT_HABIT_ARG_ID = "habitId"
    const val EDIT_HABIT = "$EDIT_HABIT_ROUTE/{$EDIT_HABIT_ARG_ID}"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val SIGN_IN = "sign_in"

    // AI Assistant routes
    const val AI_ASSISTANT = "ai_assistant"
    const val AI_ASSISTANT_SETTINGS = "ai_assistant_settings"

    // Advanced Analytics route
    const val ADVANCED_ANALYTICS = "advanced_analytics"

    // Habit completion routes
    const val HABIT_COMPLETION_ROUTE = "habit_completion"
    const val HABIT_COMPLETION_ARG_ID = "habitId"
    const val HABIT_COMPLETION_ARG_NAME = "habitName"
    const val HABIT_COMPLETION = "$HABIT_COMPLETION_ROUTE/{$HABIT_COMPLETION_ARG_ID}/{$HABIT_COMPLETION_ARG_NAME}"

    // Gamification route
    const val GAMIFICATION = "gamification"

    // Splash / Onboarding
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"

    // Helper functions for parameterized routes
    fun editHabit(habitId: String) = "$EDIT_HABIT_ROUTE/$habitId"
    fun habitDetails(habitId: String) = HABIT_DETAILS.replace("{habitId}", habitId)
    fun habitCompletion(habitId: String, habitName: String) = "$HABIT_COMPLETION_ROUTE/$habitId/$habitName"
}

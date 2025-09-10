package com.example.myapplication.navigation

sealed class Screen(val route: String) {
    object AddHabit : Screen("add_habit")
    object EditHabit : Screen("edit_habit")
    object HabitsList : Screen("habits_list")
    object Stats : Screen("stats")
}

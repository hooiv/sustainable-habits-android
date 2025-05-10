package com.example.myapplication.data.model

/**
 * Enum for habit categories
 */
enum class HabitCategory(val displayName: String) {
    HEALTH("Health"),
    FITNESS("Fitness"),
    EXERCISE("Exercise"),
    PRODUCTIVITY("Productivity"),
    WORK("Work"),
    STUDY("Study"),
    MINDFULNESS("Mindfulness"),
    MEDITATION("Meditation"),
    MENTAL_HEALTH("Mental Health"),
    SOCIAL("Social"),
    RELATIONSHIPS("Relationships"),
    CREATIVITY("Creativity"),
    HOBBY("Hobby"),
    NUTRITION("Nutrition"),
    DIET("Diet"),
    SLEEP("Sleep"),
    WELLNESS("Wellness"),
    READING("Reading"),
    LEARNING("Learning"),
    CAREER("Career"),
    OTHER("Other");

    companion object {
        /**
         * Get a HabitCategory from a string name (case-insensitive)
         */
        fun fromString(name: String?): HabitCategory {
            if (name == null) return OTHER
            
            return values().find { 
                it.name.equals(name, ignoreCase = true) || 
                it.displayName.equals(name, ignoreCase = true) 
            } ?: OTHER
        }
    }
}

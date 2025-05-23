package com.example.myapplication.data.model

import java.util.Date

/**
 * Data class for mood tracking
 */
data class MoodEntry(
    val id: String,
    val userId: String,
    val mood: MoodType,
    val intensity: Float, // 0.0 to 1.0
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val associatedHabitIds: List<String> = emptyList()
)

/**
 * Enum for mood types
 */
enum class MoodType {
    HAPPY,
    ENERGETIC,
    CALM,
    TIRED,
    STRESSED,
    ANXIOUS,
    SAD,
    NEUTRAL
}

/**
 * Data class for location context
 */
data class LocationContext(
    val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String = "",
    val locationType: LocationType = LocationType.OTHER,
    val timestamp: Long = System.currentTimeMillis(),
    val associatedHabitIds: List<String> = emptyList()
)

/**
 * Enum for location types
 */
enum class LocationType {
    HOME,
    WORK,
    GYM,
    OUTDOORS,
    TRANSIT,
    RESTAURANT,
    SHOPPING,
    OTHER
}

/**
 * Data class for time pattern analysis
 */
data class TimePattern(
    val habitId: String,
    val userId: String,
    val dayOfWeekFrequency: Map<Int, Int>, // Day of week (1-7) to count
    val hourOfDayFrequency: Map<Int, Int>, // Hour of day (0-23) to count
    val averageCompletionTime: Long, // Average time taken to complete
    val streakPatterns: List<StreakPeriod>,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Data class for streak periods
 */
data class StreakPeriod(
    val startDate: Long,
    val endDate: Long,
    val length: Int
)

/**
 * Data class for AI context that combines all context sources
 */
data class AIContext(
    val habits: List<Habit> = emptyList(),
    val completions: List<HabitCompletion> = emptyList(),
    val moods: List<MoodEntry> = emptyList(),
    val locations: List<LocationContext> = emptyList(),
    val timePatterns: List<TimePattern> = emptyList(),
    val personalization: AIAssistantPersonalization = AIAssistantPersonalization()
)

/**
 * Data class for AI Assistant personalization settings
 */
data class AIAssistantPersonalization(
    // General settings
    val useStreaming: Boolean = true,
    val useVoice: Boolean = false,
    val voiceSpeed: Float = 1.0f,
    val voicePitch: Float = 1.0f,
    
    // Voice recognition settings
    val continuousListening: Boolean = false,
    val useWakeWord: Boolean = false,
    val customWakeWords: List<String> = emptyList(),
    
    // Context settings
    val includeMoodData: Boolean = true,
    val includeLocationData: Boolean = true,
    val includeTimePatterns: Boolean = true,
    
    // Appearance settings
    val showAnimations: Boolean = true,
    val darkTheme: Boolean = false,
    
    // Privacy settings
    val saveConversationHistory: Boolean = true,
    val shareHabitData: Boolean = true
)

/**
 * Helper class to format context data for AI prompts
 */
object AIContextFormatter {
    /**
     * Format mood data for AI prompt
     */
    fun formatMoodData(moods: List<MoodEntry>): String {
        if (moods.isEmpty()) return ""
        
        val recentMoods = moods.sortedByDescending { it.timestamp }.take(5)
        val builder = StringBuilder("Recent mood data:\n")
        
        recentMoods.forEach { mood ->
            val date = Date(mood.timestamp)
            builder.append("- ${mood.mood} (intensity: ${(mood.intensity * 10).toInt()}/10) on $date")
            if (mood.notes.isNotEmpty()) {
                builder.append(", notes: \"${mood.notes}\"")
            }
            builder.append("\n")
        }
        
        // Add mood summary
        val moodCounts = moods.groupBy { it.mood }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
        
        builder.append("\nMost common moods: ")
        builder.append(moodCounts.joinToString(", ") { "${it.first} (${it.second})" })
        
        return builder.toString()
    }
    
    /**
     * Format location data for AI prompt
     */
    fun formatLocationData(locations: List<LocationContext>): String {
        if (locations.isEmpty()) return ""
        
        val recentLocations = locations.sortedByDescending { it.timestamp }.take(3)
        val builder = StringBuilder("Recent location data:\n")
        
        recentLocations.forEach { location ->
            val date = Date(location.timestamp)
            builder.append("- ${location.locationName} (${location.locationType}) on $date\n")
        }
        
        // Add location summary
        val locationCounts = locations.groupBy { it.locationType }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
        
        builder.append("\nMost common locations: ")
        builder.append(locationCounts.joinToString(", ") { "${it.first} (${it.second})" })
        
        return builder.toString()
    }
    
    /**
     * Format time pattern data for AI prompt
     */
    fun formatTimePatternData(timePatterns: List<TimePattern>): String {
        if (timePatterns.isEmpty()) return ""
        
        val builder = StringBuilder("Time pattern analysis:\n")
        
        timePatterns.forEach { pattern ->
            builder.append("- Habit ID ${pattern.habitId}:\n")
            
            // Most common days
            val topDays = pattern.dayOfWeekFrequency.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { entry ->
                    val day = when (entry.key) {
                        1 -> "Monday"
                        2 -> "Tuesday"
                        3 -> "Wednesday"
                        4 -> "Thursday"
                        5 -> "Friday"
                        6 -> "Saturday"
                        7 -> "Sunday"
                        else -> "Unknown"
                    }
                    "$day (${entry.value})"
                }
            
            builder.append("  Most common days: ${topDays.joinToString(", ")}\n")
            
            // Most common hours
            val topHours = pattern.hourOfDayFrequency.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { entry -> "${entry.key}:00 (${entry.value})" }
            
            builder.append("  Most common hours: ${topHours.joinToString(", ")}\n")
            
            // Streak info
            if (pattern.streakPatterns.isNotEmpty()) {
                val longestStreak = pattern.streakPatterns.maxByOrNull { it.length }
                longestStreak?.let {
                    val startDate = Date(it.startDate)
                    val endDate = Date(it.endDate)
                    builder.append("  Longest streak: ${it.length} days ($startDate to $endDate)\n")
                }
            }
        }
        
        return builder.toString()
    }
}

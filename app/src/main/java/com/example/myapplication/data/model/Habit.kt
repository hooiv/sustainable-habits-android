package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myapplication.data.database.Converters // Import Converters
import java.util.UUID
import java.util.Date

@Entity(tableName = "habits") // Annotate as an entity with table name
@TypeConverters(Converters::class) // Specify TypeConverters for this entity
data class Habit(
    @PrimaryKey // Mark id as the primary key
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val goal: Int = 1, // e.g., complete 1 time for daily, 3 times for weekly
    var goalProgress: Int = 0, // How many times completed in the current frequency period
    var streak: Int = 0,
    var lastCompletedDate: Date? = null,
    val createdDate: Date = Date(),
    var completionHistory: List<Date> = emptyList(), // History of all completion dates
    var isEnabled: Boolean = true, // To allow pausing a habit
    var reminderTime: String? = null // For notifications, e.g., "09:00"
)

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

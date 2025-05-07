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
    val goal: Int = 1,
    val streak: Int = 0,
    val lastCompletedDate: Date? = null,
    val createdDate: Date = Date()
)

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

package com.example.myapplication.data.database

import androidx.room.TypeConverter
import com.example.myapplication.data.model.HabitFrequency
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromHabitFrequency(value: String?): HabitFrequency? {
        return value?.let { HabitFrequency.valueOf(it) }
    }

    @TypeConverter
    fun habitFrequencyToString(frequency: HabitFrequency?): String? {
        return frequency?.name
    }
}

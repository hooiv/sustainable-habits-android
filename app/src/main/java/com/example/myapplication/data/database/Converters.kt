package com.example.myapplication.data.database

import androidx.room.TypeConverter
import com.example.myapplication.data.model.HabitFrequency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    
    // Convert List<Date> to String for storage
    @TypeConverter
    fun fromDateList(dateList: List<Date>?): String? {
        if (dateList == null) {
            return null
        }
        // Convert the list of dates to a list of timestamps
        val dateTimestamps = dateList.map { it.time }
        return Gson().toJson(dateTimestamps)
    }
    
    // Convert String back to List<Date>
    @TypeConverter
    fun toDateList(dateListString: String?): List<Date> {
        if (dateListString.isNullOrEmpty()) {
            return emptyList()
        }
        val listType = object : TypeToken<List<Long>>() {}.type
        val timestamps: List<Long> = Gson().fromJson(dateListString, listType)
        return timestamps.map { Date(it) }
    }
}

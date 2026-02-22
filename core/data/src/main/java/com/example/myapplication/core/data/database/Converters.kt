package com.hooiv.habitflow.core.data.database

import androidx.room.TypeConverter
import com.hooiv.habitflow.core.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

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

    @TypeConverter
    fun fromDateList(value: List<Date>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDateList(value: String): List<Date> {
        val listType = object : TypeToken<List<Date>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromMutableDateList(value: MutableList<Date>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMutableDateList(value: String): MutableList<Date> {
        val listType = object : TypeToken<MutableList<Date>>() {}.type
        return gson.fromJson(value, listType) ?: mutableListOf()
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // HabitNote converters
    @TypeConverter
    fun fromHabitNoteList(value: List<HabitNote>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toHabitNoteList(value: String): List<HabitNote> {
        val listType = object : TypeToken<List<HabitNote>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // Enum converters for HabitDifficulty, HabitPriority
    @TypeConverter
    fun fromHabitDifficulty(value: String?): HabitDifficulty? {
        return value?.let { HabitDifficulty.valueOf(it) }
    }

    @TypeConverter
    fun habitDifficultyToString(difficulty: HabitDifficulty?): String? {
        return difficulty?.name
    }

    @TypeConverter
    fun fromHabitPriority(value: String?): HabitPriority? {
        return value?.let { HabitPriority.valueOf(it) }
    }

    @TypeConverter
    fun habitPriorityToString(priority: HabitPriority?): String? {
        return priority?.name
    }
}

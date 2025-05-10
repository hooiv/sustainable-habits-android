package com.example.myapplication.data.database

import androidx.room.TypeConverter
import com.example.myapplication.data.model.*
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

    // LocationReminder converters
    @TypeConverter
    fun fromLocationReminder(value: LocationReminder?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toLocationReminder(value: String?): LocationReminder? {
        return value?.let {
            gson.fromJson(it, LocationReminder::class.java)
        }
    }

    // Map converters
    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringIntMap(value: String): Map<String, Int> {
        val mapType = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(value, mapType) ?: emptyMap()
    }

    // HabitReward converters
    @TypeConverter
    fun fromHabitRewardList(value: List<HabitReward>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toHabitRewardList(value: String): List<HabitReward> {
        val listType = object : TypeToken<List<HabitReward>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // Enum converters for HabitDifficulty, HabitPriority, VisualizationType
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

    @TypeConverter
    fun fromVisualizationType(value: String?): VisualizationType? {
        return value?.let { VisualizationType.valueOf(it) }
    }

    @TypeConverter
    fun visualizationTypeToString(type: VisualizationType?): String? {
        return type?.name
    }

    // Neural network type converters
    @TypeConverter
    fun fromNeuralNodeType(value: String?): NeuralNodeType? {
        return value?.let { NeuralNodeType.valueOf(it) }
    }

    @TypeConverter
    fun neuralNodeTypeToString(type: NeuralNodeType?): String? {
        return type?.name
    }

    @TypeConverter
    fun fromTrainingStatus(value: String?): TrainingStatus? {
        return value?.let { TrainingStatus.valueOf(it) }
    }

    @TypeConverter
    fun trainingStatusToString(status: TrainingStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun fromPredictionType(value: String?): PredictionType? {
        return value?.let { PredictionType.valueOf(it) }
    }

    @TypeConverter
    fun predictionTypeToString(type: PredictionType?): String? {
        return type?.name
    }
}

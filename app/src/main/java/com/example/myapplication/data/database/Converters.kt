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

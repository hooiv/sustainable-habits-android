package com.hooiv.habitflow.core.data.database

import androidx.room.TypeConverter
import com.hooiv.habitflow.core.data.model.HabitDifficulty
import com.hooiv.habitflow.core.data.model.HabitFrequency
import com.hooiv.habitflow.core.data.model.HabitNote
import com.hooiv.habitflow.core.data.model.HabitPriority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Room TypeConverters for the HabitFlow database.
 *
 * Guiding principles:
 *  - Primitive / enum types use simple string/Long representations — no Gson overhead.
 *  - `List<Long>` (completionHistory) uses '|'-delimited encoding for O(n) read without
 *    reflection. Falls back to legacy JSON bracket format for existing rows.
 *  - `List<Int>` (unlockedBadges) uses the same '|' encoding.
 *  - `List<String>` (tags) uses Gson because strings may contain '|'.
 *  - `List<HabitNote>` uses Gson because it is a complex object.
 *  - `Date` ↔ `Long` kept for any remaining Date fields used in legacy migrations.
 */
class Converters {

    companion object {
        private val gson: Gson by lazy { Gson() }
    }

    // ── Date ──────────────────────────────────────────────────────────────────
    @TypeConverter fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }
    @TypeConverter fun dateToTimestamp(date: Date?): Long? = date?.time

    // ── Enums ─────────────────────────────────────────────────────────────────
    @TypeConverter fun fromHabitFrequency(value: String?): HabitFrequency? =
        value?.let { HabitFrequency.valueOf(it) }
    @TypeConverter fun habitFrequencyToString(v: HabitFrequency?): String? = v?.name

    @TypeConverter fun fromHabitDifficulty(value: String?): HabitDifficulty? =
        value?.let { HabitDifficulty.valueOf(it) }
    @TypeConverter fun habitDifficultyToString(v: HabitDifficulty?): String? = v?.name

    @TypeConverter fun fromHabitPriority(value: String?): HabitPriority? =
        value?.let { HabitPriority.valueOf(it) }
    @TypeConverter fun habitPriorityToString(v: HabitPriority?): String? = v?.name

    // ── List<Long> — epoch-millis completion history ──────────────────────────
    // Storage format: "1704067200000|1704153600000|..."
    // Legacy format (Gson):  "[1704067200000,1704153600000,...]"
    @TypeConverter
    fun fromLongList(value: List<Long>?): String {
        if (value.isNullOrEmpty()) return ""
        return value.joinToString("|")
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        if (value.isBlank()) return emptyList()
        return if (value.startsWith("[")) {
            // Legacy Gson JSON — parse once and store in new format on next write
            gson.fromJson<List<Long>>(value, object : TypeToken<List<Long>>() {}.type)
                ?: emptyList()
        } else {
            value.split("|").mapNotNull { it.toLongOrNull() }
        }
    }

    // ── List<Int> — unlocked badge milestone IDs ──────────────────────────────
    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        if (value.isNullOrEmpty()) return ""
        return value.joinToString("|")
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isBlank()) return emptyList()
        return if (value.startsWith("[")) {
            gson.fromJson<List<Int>>(value, object : TypeToken<List<Int>>() {}.type)
                ?: emptyList()
        } else {
            value.split("|").mapNotNull { it.toIntOrNull() }
        }
    }

    // ── List<String> — tags (may contain '|', so keep Gson) ───────────────────
    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank() || value == "null") return emptyList()
        return gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
            ?: emptyList()
    }

    // ── List<HabitNote> — complex objects, Gson required ──────────────────────
    @TypeConverter
    fun fromHabitNoteList(value: List<HabitNote>?): String =
        gson.toJson(value ?: emptyList<HabitNote>())

    @TypeConverter
    fun toHabitNoteList(value: String): List<HabitNote> {
        if (value.isBlank() || value == "null") return emptyList()
        return gson.fromJson(value, object : TypeToken<List<HabitNote>>() {}.type)
            ?: emptyList()
    }
}

package com.example.myapplication.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.model.Habit

@Database(entities = [Habit::class], version = 1, exportSchema = false) // Added exportSchema = false for simplicity
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    // You can add a companion object for a singleton instance if not using Hilt for DB provider
    // For Hilt, we will provide this via a Hilt module later.
}

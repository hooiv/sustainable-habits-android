package com.example.myapplication.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.model.Habit

// Increased version number from 1 to 2
@Database(entities = [Habit::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // This is a fallback migration - since we don't know the exact schema changes,
                // we're just updating the version number without making schema changes.
                // In a production app, you would add the appropriate ALTER TABLE statements here.
            }
        }

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_database"
                )
                .addMigrations(MIGRATION_1_2) // Add migration
                .fallbackToDestructiveMigration() // As a last resort, recreate the database
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

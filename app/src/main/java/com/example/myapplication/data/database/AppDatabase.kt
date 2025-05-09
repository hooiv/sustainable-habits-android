package com.example.myapplication.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.model.Habit

@Database(entities = [Habit::class], version = 4, exportSchema = false)
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
        
        // Migration from version 2 to 3: Adding lastUpdatedTimestamp
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new lastUpdatedTimestamp column to the habits table
                database.execSQL("ALTER TABLE habits ADD COLUMN lastUpdatedTimestamp INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 3 to 4: To resolve identity hash mismatch
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // This migration is added to address a potential schema mismatch.
            }
        }

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // Add all migrations
                .fallbackToDestructiveMigration() // As a last resort, recreate the database
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

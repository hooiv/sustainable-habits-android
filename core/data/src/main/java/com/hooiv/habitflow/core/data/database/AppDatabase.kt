package com.hooiv.habitflow.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hooiv.habitflow.core.data.model.Habit
import com.hooiv.habitflow.core.data.model.HabitCompletion

/**
 * HabitFlow Room database.
 *
 * Schema export is enabled: files are written to `schemas/` and should be
 * checked into version control so migration correctness can be verified in CI.
 *
 * Version history:
 *  1→2  Initial migration placeholder
 *  2→3  Added `lastUpdatedTimestamp`
 *  3→4  Identity-hash bump after minor schema annotation change
 *  4→5  Neural-network tables (feature since removed; tables remain for safety)
 *  5→6  Added `habit_completions` table
 *  6→7  Identity-hash bump after WorkManager / modular refactoring
 *  7→8  Converted timestamp fields from Date/Long TypeConverter to native Long;
 *       converted completionHistory from JSON-Date blob to JSON-Long blob.
 *       No column-type changes — all were already stored as INTEGER / TEXT.
 *       Added indices on `isDeleted`, `lastCompletedDate`, `category`.
 */
@Database(
    entities = [Habit::class, HabitCompletion::class],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) { /* placeholder */ }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE habits ADD COLUMN lastUpdatedTimestamp INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) { /* identity-hash bump */ }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Neural-network feature tables — kept as no-ops on upgrade; never used.
                db.execSQL("""CREATE TABLE IF NOT EXISTS habit_neural_networks (
                    id TEXT NOT NULL PRIMARY KEY, habitId TEXT NOT NULL,
                    name TEXT NOT NULL, description TEXT,
                    createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_nn_habitId ON habit_neural_networks(habitId)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS habit_completions")
                db.execSQL("""CREATE TABLE IF NOT EXISTS habit_completions (
                    id TEXT NOT NULL PRIMARY KEY,
                    habitId TEXT NOT NULL,
                    completionDate INTEGER NOT NULL,
                    note TEXT, mood INTEGER, location TEXT, photoUri TEXT,
                    FOREIGN KEY (habitId) REFERENCES habits(id) ON DELETE CASCADE)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_hc_habitId ON habit_completions(habitId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_hc_date ON habit_completions(completionDate)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) { /* identity-hash bump */ }
        }

        /**
         * Version 7 → 8: Add performance indices on habits table.
         * No column changes — timestamp fields were already INTEGER, list fields were TEXT.
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_habits_isDeleted ON habits(isDeleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_habits_lastCompleted ON habits(lastCompletedDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_habits_category ON habits(category)")
            }
        }
    }
}

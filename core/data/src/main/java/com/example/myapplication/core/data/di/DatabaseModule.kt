package com.hooiv.habitflow.core.data.di

import android.content.Context
import androidx.room.Room
import com.hooiv.habitflow.core.data.database.AppDatabase
import com.hooiv.habitflow.core.data.database.HabitCompletionDao
import com.hooiv.habitflow.core.data.database.HabitDao
import com.hooiv.habitflow.core.data.repository.HabitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "habit_database")
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8
            )
            // No fallbackToDestructiveMigration — forces explicit migrations to preserve user data
            .build()

    @Provides @Singleton
    fun provideHabitDao(db: AppDatabase): HabitDao = db.habitDao()

    @Provides @Singleton
    fun provideHabitCompletionDao(db: AppDatabase): HabitCompletionDao = db.habitCompletionDao()

    @Provides @Singleton
    fun provideHabitRepository(
        db: AppDatabase,
        habitDao: HabitDao,
        habitCompletionDao: HabitCompletionDao
    ): HabitRepository = HabitRepository(db, habitDao, habitCompletionDao)
}

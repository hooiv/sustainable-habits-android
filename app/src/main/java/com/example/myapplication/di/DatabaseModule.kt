package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.database.HabitDao
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
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "habit_database" // Changed to match the name used in AppDatabase.getInstance()
        )
        .addMigrations(
            AppDatabase.MIGRATION_1_2, 
            AppDatabase.MIGRATION_2_3, 
            AppDatabase.MIGRATION_3_4, 
            AppDatabase.MIGRATION_4_5
        ) // Add all migrations
        .fallbackToDestructiveMigration() // Allow destructive migrations as a last resort
        .build()
    }

    @Provides
    fun provideHabitDao(appDatabase: AppDatabase): HabitDao {
        return appDatabase.habitDao()
    }

    @Provides
    fun provideNeuralNetworkDao(appDatabase: AppDatabase): NeuralNetworkDao {
        return appDatabase.neuralNetworkDao()
    }
}

package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.database.HabitDao
import com.example.myapplication.data.database.HabitCompletionDao
import com.example.myapplication.data.database.NeuralNetworkDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "habit_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(appDatabase: AppDatabase): HabitDao {
        return appDatabase.habitDao()
    }

    @Provides
    @Singleton
    fun provideHabitCompletionDao(appDatabase: AppDatabase): HabitCompletionDao {
        return appDatabase.habitCompletionDao()
    }

    @Provides
    @Singleton
    fun provideNeuralNetworkDao(appDatabase: AppDatabase): NeuralNetworkDao {
        return appDatabase.neuralNetworkDao()
    }
}

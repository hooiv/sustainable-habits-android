package com.example.myapplication.core.ui.receivers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.core.data.database.AppDatabase
import com.example.myapplication.core.data.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker for handling habit completion in the background
 * Uses proper dependency injection with Hilt
 */
@HiltWorker
class HabitCompletionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HabitRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getString("habit_id") ?: return Result.failure()

        try {
            Log.d("HabitCompletionWorker", "Processing habit completion for ID: $habitId")
            repository.markHabitCompleted(habitId)
            return Result.success()
        } catch (e: Exception) {
            Log.e("HabitCompletionWorker", "Error completing habit: ${e.message}")
            return Result.retry()
        }
    }
}

/**
 * Non-Hilt fallback worker for when dependency injection is not available
 * (e.g., when launched from a BroadcastReceiver)
 */
class HabitCompletionFallbackWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getString("habit_id") ?: return Result.failure()

        try {
            Log.d("HabitCompletionWorker", "Processing habit completion for ID: $habitId")

            // Create repository manually since we don't have injection
            val database = AppDatabase.getInstance(applicationContext)
            val habitDao = database.habitDao()
            val habitCompletionDao = database.habitCompletionDao()
            val workManager = androidx.work.WorkManager.getInstance(applicationContext)
            val repository = HabitRepository(habitDao, habitCompletionDao, workManager)

            repository.markHabitCompleted(habitId)
            return Result.success()
        } catch (e: Exception) {
            Log.e("HabitCompletionWorker", "Error completing habit: ${e.message}")
            return Result.retry()
        }
    }
}

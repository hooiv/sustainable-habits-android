package com.example.myapplication.core.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.core.data.database.HabitDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class HabitSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitDao: HabitDao,
    private val remoteDataSource: MockRemoteDataSource
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Push Phase: Upload local changes
            val unsyncedHabits = habitDao.getUnsyncedHabits()
            if (unsyncedHabits.isNotEmpty()) {
                remoteDataSource.pushHabits(unsyncedHabits)
                habitDao.markSynced(unsyncedHabits.map { it.id })
            }

            // 2. Pull Phase: Download remote changes
            // For simplicity, we assume we want everything after the last sync
            // In a real app, we'd store the last successful sync timestamp in DataStore
            val lastSyncTimestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // Mock: Sync last 24h
            val remoteUpdates = remoteDataSource.fetchMainHabitsBefore(lastSyncTimestamp)

            // 3. Conflict Resolution (Last-Write-Wins) is handled by Insert(onConflict = REPLACE)
            // paired with the fact that we just fetched the latest from server.
            // However, a true LWW needs to compare timestamps.
            
            // For this simulation, we'll just insert/replace whatever we got from remote
            if (remoteUpdates.isNotEmpty()) {
                habitDao.insertOrReplaceHabits(remoteUpdates)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
             if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}

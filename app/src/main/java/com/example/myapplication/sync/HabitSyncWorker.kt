package com.example.myapplication.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.util.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HabitSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext Result.failure()

        try {
            // Fetch local and remote data
            val localHabits = fetchLocalHabits()
            val remoteHabits = fetchRemoteHabits(userId)

            // Merge data and resolve conflicts
            val mergedHabits = mergeHabits(localHabits, remoteHabits)

            // Save merged data back to Firestore and locally
            saveToFirestore(userId, mergedHabits)
            saveToLocalDatabase(mergedHabits)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun fetchLocalHabits(): List<Map<String, Any>> {
        // Fetch habits from local database (placeholder logic)
        return emptyList()
    }

    private suspend fun fetchRemoteHabits(userId: String): List<Map<String, Any>> {
        val remoteHabits = mutableListOf<Map<String, Any>>()
        FirebaseUtil.fetchHabitData(userId, onSuccess = {
            remoteHabits.addAll(it)
        }, onFailure = {
            throw it
        })
        return remoteHabits
    }

    private fun mergeHabits(local: List<Map<String, Any>>, remote: List<Map<String, Any>>): List<Map<String, Any>> {
        // Placeholder logic for merging habits
        return local + remote
    }

    private fun saveToFirestore(userId: String, habits: List<Map<String, Any>>) {
        habits.forEach { habit ->
            FirebaseUtil.saveHabitData(userId, habit, onSuccess = {}, onFailure = { throw it })
        }
    }

    private fun saveToLocalDatabase(habits: List<Map<String, Any>>) {
        // Save habits to local database (placeholder logic)
    }
}
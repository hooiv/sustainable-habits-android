package com.example.myapplication.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import com.example.myapplication.data.repository.HabitRepository
import com.example.myapplication.util.FirebaseUtil
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.Date

// Helper function to parse Firebase data map to a Habit domain object (similar to StatsScreen)
private fun parseHabitMapToDomain(id: String, data: Map<String, Any>): Habit? {
    return try {
        val name = data["name"] as? String ?: run {
            Log.e("HabitParsingWorker", "Habit name is null or not a String for ID $id. Skipping.")
            return null
        }
        val description = data["description"] as? String
        val frequencyString = data["frequency"] as? String ?: HabitFrequency.DAILY.name
        val frequency = try { HabitFrequency.valueOf(frequencyString) } catch (e: IllegalArgumentException) {
            Log.w("HabitParsingWorker", "Invalid frequency string \'$frequencyString\' for ID $id. Defaulting to DAILY.")
            HabitFrequency.DAILY
        }

        val goal = (data["goal"] as? Long)?.toInt() ?: 1
        val goalProgress = (data["goalProgress"] as? Long)?.toInt() ?: 0
        val streak = (data["streak"] as? Long)?.toInt() ?: 0

        val createdTimestamp = data["createdDate"] as? Timestamp
        val createdDate = createdTimestamp?.toDate() ?: run {
            Log.w("HabitParsingWorker", "createdDate is null or not a Timestamp for ID $id. Using current date as fallback.")
            Date()
        }

        val lastUpdatedTimestampFirebase = data["lastUpdatedTimestamp"] as? Timestamp
        val lastUpdatedTimestamp = lastUpdatedTimestampFirebase?.toDate() ?: createdDate

        val lastCompletedTimestamp = data["lastCompletedDate"] as? Timestamp
        val lastCompletedDate = lastCompletedTimestamp?.toDate()

        val completionHistoryFirebase = data["completionHistory"] as? List<*>
        val completionHistory = completionHistoryFirebase?.mapNotNull {
            (it as? Timestamp)?.toDate()
        }?.toMutableList() ?: mutableListOf()

        val isEnabled = data["isEnabled"] as? Boolean ?: true
        val reminderTime = data["reminderTime"] as? String

        val unlockedBadgesFirebase = data["unlockedBadges"] as? List<*>
        val unlockedBadges = unlockedBadgesFirebase?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList()

        val category = data["category"] as? String

        Habit(
            id = id,
            name = name,
            description = description,
            frequency = frequency,
            goal = goal,
            goalProgress = goalProgress,
            streak = streak,
            createdDate = createdDate,
            lastUpdatedTimestamp = lastUpdatedTimestamp,
            lastCompletedDate = lastCompletedDate,
            completionHistory = completionHistory,
            isEnabled = isEnabled,
            reminderTime = reminderTime,
            unlockedBadges = unlockedBadges,
            category = category
        )
    } catch (e: Exception) {
        Log.e("HabitParsingWorker", "Failed to parse habit with id $id: ${e.message}", e)
        null
    }
}

@HiltWorker
class HabitSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val habitRepository: HabitRepository // Injected HabitRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext Result.failure()
        Log.d("HabitSyncWorker", "Starting sync for user $userId")

        try {
            // Fetch local and remote data
            val localHabits = fetchLocalHabits()
            Log.d("HabitSyncWorker", "Fetched ${localHabits.size} local habits.")

            val remoteHabitsMap = FirebaseUtil.fetchHabitDataSuspend(userId)
            Log.d("HabitSyncWorker", "Fetched ${remoteHabitsMap.size} remote habits from Firebase.")

            val remoteHabits = remoteHabitsMap.mapNotNull { (id, data) ->
                parseHabitMapToDomain(id, data)
            }
            Log.d("HabitSyncWorker", "Parsed ${remoteHabits.size} remote habits into domain objects.")

            // Merge data and resolve conflicts
            val mergedHabits = mergeHabits(localHabits, remoteHabits)
            Log.d("HabitSyncWorker", "Merged into ${mergedHabits.size} habits.")

            // Save merged data back to Firestore and locally
            if (mergedHabits.isNotEmpty()) {
                FirebaseUtil.backupHabitDataSuspend(userId, mergedHabits)
                Log.d("HabitSyncWorker", "Saved ${mergedHabits.size} habits to Firestore.")
                saveToLocalDatabase(mergedHabits)
                Log.d("HabitSyncWorker", "Saved ${mergedHabits.size} habits to local database.")
            } else {
                Log.d("HabitSyncWorker", "No habits to save after merge.")
            }

            Log.d("HabitSyncWorker", "Sync completed successfully for user $userId.")
            Result.success()
        } catch (e: Exception) {
            Log.e("HabitSyncWorker", "Sync failed for user $userId", e)
            Result.failure()
        }
    }

    private suspend fun fetchLocalHabits(): List<Habit> {
        Log.d("HabitSyncWorker", "fetchLocalHabits: Fetching from repository.")
        // Use HabitRepository to get all habits
        // The flow is collected once to get the current list of habits.
        return habitRepository.getAllHabits().firstOrNull() ?: emptyList()
    }

    private fun mergeHabits(localHabits: List<Habit>, remoteHabits: List<Habit>): List<Habit> {
        Log.d("HabitSyncWorker", "Merging ${localHabits.size} local and ${remoteHabits.size} remote habits.")
        val mergedHabits = mutableMapOf<String, Habit>()

        localHabits.forEach { mergedHabits[it.id] = it }

        remoteHabits.forEach { remoteHabit ->
            val localHabit = mergedHabits[remoteHabit.id]
            if (localHabit == null) {
                mergedHabits[remoteHabit.id] = remoteHabit
            } else {
                if (remoteHabit.lastUpdatedTimestamp.after(localHabit.lastUpdatedTimestamp)) {
                    mergedHabits[remoteHabit.id] = remoteHabit
                }
            }
        }
        Log.d("HabitSyncWorker", "Merge result: ${mergedHabits.values.size} habits.")
        return mergedHabits.values.toList()
    }

    private suspend fun saveToFirestore(userId: String, habits: List<Habit>) {
        if (habits.isEmpty()) {
            Log.d("HabitSyncWorker", "saveToFirestore: No habits to save.")
            return
        }
        Log.d("HabitSyncWorker", "saveToFirestore: Saving ${habits.size} habits for user $userId.")
        FirebaseUtil.backupHabitDataSuspend(userId, habits)
        Log.d("HabitSyncWorker", "Successfully saved ${habits.size} habits to Firestore.")
    }

    private suspend fun saveToLocalDatabase(habits: List<Habit>) {
        if (habits.isEmpty()) {
            Log.d("HabitSyncWorker", "saveToLocalDatabase: No habits to save.")
            return
        }
        Log.d("HabitSyncWorker", "saveToLocalDatabase: Saving ${habits.size} habits.")
        // Use HabitRepository to insert or replace habits
        habitRepository.insertOrReplaceHabits(habits)
        Log.d("HabitSyncWorker", "Successfully saved ${habits.size} habits to local database.")
    }
}
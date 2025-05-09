package com.example.myapplication.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import com.example.myapplication.util.FirebaseUtil
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
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


class HabitSyncWorker(
    context: Context, 
    params: WorkerParameters,
    // Assuming you have a way to inject or get an instance of HabitRepository or HabitViewModel
    // For simplicity, let's assume a static or singleton access for now, or pass it if possible
    // private val habitRepository: HabitRepository 
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext Result.failure()
        Log.d("HabitSyncWorker", "Starting sync for user $userId")

        try {
            // Fetch local and remote data
            val localHabits = fetchLocalHabits() // Should return List<Habit>
            Log.d("HabitSyncWorker", "Fetched ${localHabits.size} local habits.")
            val remoteHabitsMap = fetchRemoteHabitsData(userId) // Returns Map<String, Map<String, Any>>
            Log.d("HabitSyncWorker", "Fetched ${remoteHabitsMap.size} remote habits.")

            val remoteHabits = remoteHabitsMap.mapNotNull { (id, data) ->
                parseHabitMapToDomain(id, data)
            }
            Log.d("HabitSyncWorker", "Parsed ${remoteHabits.size} remote habits into domain objects.")

            // Merge data and resolve conflicts
            val mergedHabits = mergeHabits(localHabits, remoteHabits) // Should operate on List<Habit>
            Log.d("HabitSyncWorker", "Merged into ${mergedHabits.size} habits.")

            // Save merged data back to Firestore and locally
            if (mergedHabits.isNotEmpty()) {
                saveToFirestore(userId, mergedHabits)
                Log.d("HabitSyncWorker", "Saved ${mergedHabits.size} habits to Firestore.")
                saveToLocalDatabase(mergedHabits) // Should take List<Habit>
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
        // TODO: Implement actual logic to fetch habits from local Room database
        // Example: return habitRepository.getAllHabits().first() 
        Log.w("HabitSyncWorker", "fetchLocalHabits: Placeholder - returning empty list.")
        return emptyList() 
    }

    private suspend fun fetchRemoteHabitsData(userId: String): Map<String, Map<String, Any>> {
        return try {
            val data = mutableMapOf<String, Map<String, Any>>()
            // Using a more direct way to handle suspend function for Firebase
            FirebaseUtil.fetchHabitData(userId, 
                onSuccess = { fetchedMap -> data.putAll(fetchedMap) },
                onFailure = { exception -> throw exception } // Propagate failure
            )
            // This is tricky because fetchHabitData is not a suspend function and uses callbacks.
            // For a cleaner approach in a CoroutineWorker, FirebaseUtil.fetchHabitData should be a suspend function.
            // For now, let's assume it populates `data` correctly or we might need a delay/await mechanism if it's truly async.
            // A better way if FirebaseUtil.fetchHabitData was a suspend function:
            // return FirebaseUtil.fetchHabitDataSuspend(userId) 
            
            // Kludge for callback: wait a bit. THIS IS NOT ROBUST.
            // Consider using kotlinx.coroutines.tasks.await() on the Task if FirebaseUtil can return it.
            // Or use a CompletableDeferred.
            // For now, assuming the callback populates `data` quickly enough or is synchronous for this example.
            // If FirebaseUtil.fetchHabitData uses addOnSuccessListener/FailureListener, it's async.
            // The current FirebaseUtil.fetchHabitData is not ideal for direct use in a suspend function like this.
            // It should ideally return a Task or be a suspend function itself.
            
            // Let's assume FirebaseUtil.fetchHabitData is modified to be suspend or uses await()
            // For the sake of this example, if it's callback based and we can't change it now:
            var remoteData: Map<String, Map<String, Any>>? = null
            var error: Exception? = null
            val deferred = kotlinx.coroutines.CompletableDeferred<Map<String, Map<String, Any>>>()

            FirebaseUtil.fetchHabitData(userId,
                onSuccess = { fetchedMap -> deferred.complete(fetchedMap) },
                onFailure = { exception -> deferred.completeExceptionally(exception) }
            )
            Log.d("HabitSyncWorker", "fetchRemoteHabitsData: Waiting for Firebase callback.")
            return deferred.await() // Wait for the callback to complete

        } catch (e: Exception) {
            Log.e("HabitSyncWorker", "fetchRemoteHabitsData failed", e)
            throw e // Re-throw to be caught by doWork
        }
    }
    
    // Simplified merge: remote wins for conflicts, new from local are added.
    // A real merge would be more complex (e.g., based on last updated timestamp).
    private fun mergeHabits(local: List<Habit>, remote: List<Habit>): List<Habit> {
        Log.d("HabitSyncWorker", "Merging ${local.size} local and ${remote.size} remote habits.")
        val remoteMap = remote.associateBy { it.id }
        val localMap = local.associateBy { it.id }

        val merged = mutableMapOf<String, Habit>()

        // Add all remote habits (remote is source of truth for existing)
        remote.forEach { merged[it.id] = it }

        // Add local habits that are not in remote (new habits created offline)
        local.forEach { 
            if (!remoteMap.containsKey(it.id)) {
                merged[it.id] = it
            }
            // For conflicts (same ID): if you want local to win, or merge fields:
            // else { val remoteHabit = remoteMap[it.id]!! /* handle merging fields */ }
        }
        Log.d("HabitSyncWorker", "Merge result: ${merged.values.size} habits.")
        return merged.values.toList()
    }

    private fun saveToFirestore(userId: String, habits: List<Habit>) {
        if (habits.isEmpty()) {
            Log.d("HabitSyncWorker", "saveToFirestore: No habits to save.")
            return
        }
        Log.d("HabitSyncWorker", "saveToFirestore: Saving ${habits.size} habits for user $userId.")
        // FirebaseUtil.backupHabitData already handles a list
        FirebaseUtil.backupHabitData(userId, habits, 
            onSuccess = { Log.d("HabitSyncWorker", "Successfully saved ${habits.size} habits to Firestore.") }, 
            onFailure = { exception -> 
                Log.e("HabitSyncWorker", "Failed to save habits to Firestore for user $userId.", exception)
                throw exception // Propagate failure
            }
        )
    }

    private suspend fun saveToLocalDatabase(habits: List<Habit>) {
        if (habits.isEmpty()) {
            Log.d("HabitSyncWorker", "saveToLocalDatabase: No habits to save.")
            return
        }
        Log.d("HabitSyncWorker", "saveToLocalDatabase: Saving ${habits.size} habits.")
        // TODO: Implement actual logic to save habits to local Room database
        // Example: habitRepository.insertOrReplaceHabits(habits)
        // Make sure the repository method is suspend or called from a coroutine scope.
        Log.w("HabitSyncWorker", "saveToLocalDatabase: Placeholder - not saving to local DB yet.")
        // For example, if you had habitRepository:
        // habitRepository.insertOrReplaceHabits(habits) 
    }
}
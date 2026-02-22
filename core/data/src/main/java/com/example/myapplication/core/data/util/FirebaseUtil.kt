package com.hooiv.habitflow.core.data.util

import com.hooiv.habitflow.core.data.model.Habit
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

object FirebaseUtil {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Helper function to convert Habit object to Map for Firestore
    private fun habitToMap(habit: Habit): Map<String, Any?> {
        return mapOf(
            "id" to habit.id,
            "name" to habit.name,
            "description" to habit.description,
            "frequency" to habit.frequency.name,
            "goal" to habit.goal,
            "goalProgress" to habit.goalProgress,
            "streak" to habit.streak,
            "lastCompletedDate" to habit.lastCompletedDate?.let { Timestamp(it) },
            "createdDate" to Timestamp(habit.createdDate),
            "completionHistory" to habit.completionHistory.map { Timestamp(it) },
            "isEnabled" to habit.isEnabled,
            "reminderTime" to habit.reminderTime,
            "unlockedBadges" to habit.unlockedBadges,
            "category" to habit.category,
            "lastUpdatedTimestamp" to Timestamp(habit.lastUpdatedTimestamp)
        )
    }

    suspend fun backupHabitDataSuspend(userId: String, habits: List<Habit>) {
        val userHabitsCollection = firestore.collection("users").document(userId).collection("habits")
        val batch = firestore.batch()

        for (habit in habits) {
        val habitMap = habitToMap(habit.copy(lastUpdatedTimestamp = Date()))
            val docRef = userHabitsCollection.document(habit.id)
            batch.set(docRef, habitMap)
        }
        batch.commit().await() // Use await()
    }

    suspend fun fetchHabitDataSuspend(userId: String): Map<String, Map<String, Any>> {
        val result = firestore.collection("users").document(userId).collection("habits")
            .get()
            .await() // Use await()
        
        val habitsMap = mutableMapOf<String, Map<String, Any>>()
        for (document in result.documents) {
            document.data?.let {
                habitsMap[document.id] = it
            }
        }
        return habitsMap
    }
}

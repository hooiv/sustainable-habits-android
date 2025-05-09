package com.example.myapplication.util

import com.example.myapplication.data.model.Habit
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseUtil {
    private val firestore = Firebase.firestore

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
            "category" to habit.category
        )
    }

    fun backupHabitData(userId: String, habits: List<Habit>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userHabitsCollection = firestore.collection("users").document(userId).collection("habits")
        val batch = firestore.batch()

        for (habit in habits) {
            val habitMap = habitToMap(habit)
            val docRef = userHabitsCollection.document(habit.id)
            batch.set(docRef, habitMap)
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun fetchHabitData(userId: String, onSuccess: (Map<String, Map<String, Any>>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId).collection("habits")
            .get()
            .addOnSuccessListener { result ->
                val habitsMap = mutableMapOf<String, Map<String, Any>>()
                for (document in result.documents) {
                    document.data?.let {
                        habitsMap[document.id] = it
                    }
                }
                onSuccess(habitsMap)
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}
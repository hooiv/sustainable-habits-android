package com.example.myapplication.util

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtil {
    private val firestore = FirebaseFirestore.getInstance()

    fun saveHabitData(userId: String, habitData: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId).collection("habits")
            .add(habitData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun fetchHabitData(userId: String, onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId).collection("habits")
            .get()
            .addOnSuccessListener { result ->
                val habits = result.map { it.data }
                onSuccess(habits)
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}
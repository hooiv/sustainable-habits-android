package com.example.myapplication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repository.HabitRepository

/**
 * Broadcast receiver for handling habit-related actions from notifications
 */
class HabitActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("HabitActionReceiver", "Received action: ${intent.action}")
        
        if (intent.action == "MARK_HABIT_COMPLETE") {
            val habitId = intent.getStringExtra("habit_id")
            if (habitId != null) {
                Log.d("HabitActionReceiver", "Marking habit as complete: $habitId")
                
                // Instead of using GlobalScope directly, schedule a WorkManager task
                // This is a safer approach for background work in a BroadcastReceiver
                val inputData = Data.Builder()
                    .putString("habit_id", habitId)
                    .build()
                
                // Use the fallback worker which doesn't require Hilt
                val workRequest = OneTimeWorkRequestBuilder<HabitCompletionFallbackWorker>()
                    .setInputData(inputData)
                    .build()
                
                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}
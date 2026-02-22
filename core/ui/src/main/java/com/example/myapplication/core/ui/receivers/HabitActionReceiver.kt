package com.example.myapplication.core.ui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data

/**
 * Broadcast receiver for handling habit-related actions from notifications
 */
class HabitActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "MARK_HABIT_COMPLETE") {
            val habitId = intent.getStringExtra("habit_id")
            if (habitId != null) {
                val inputData = Data.Builder()
                    .putString("habit_id", habitId)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<HabitCompletionFallbackWorker>()
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}

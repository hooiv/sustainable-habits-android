package com.example.myapplication.notifications

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    fun scheduleHabitReminder(context: Context, habitName: String, reminderTime: LocalTime, notificationId: Int) {
        val now = LocalTime.now()
        val delay = if (reminderTime.isAfter(now)) {
            Duration.between(now, reminderTime).toMillis()
        } else {
            Duration.between(now, reminderTime.plusHours(24)).toMillis()
        }

        val inputData = workDataOf(
            "habitName" to habitName,
            "notificationId" to notificationId
        )

        val reminderRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(reminderRequest)
    }
}
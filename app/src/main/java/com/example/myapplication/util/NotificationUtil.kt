package com.example.myapplication.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

object NotificationUtil {
    private const val CHANNEL_ID = "habit_reminder_channel"
    private const val NOTIFICATION_ID = 1001

    fun scheduleDailyNotification(
        context: Context,
        hour: Int,
        minute: Int,
        text: String = "Don't forget to complete your habit today!",
        soundEnabled: Boolean = true,
        customSoundUri: String? = null // URI as string for custom sound
    ) {
        createNotificationChannel(context, customSoundUri)
        val workManager = WorkManager.getInstance(context)
        val data = Data.Builder()
            .putString("title", "Habit Reminder")
            .putString("text", text)
            .putBoolean("soundEnabled", soundEnabled)
            .putString("customSoundUri", customSoundUri)
            .build()
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (before(now)) add(java.util.Calendar.DATE, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "habit_reminder_work",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelDailyNotification(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("habit_reminder_work")
    }

    private fun createNotificationChannel(context: Context, customSoundUri: String? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Daily habit reminder notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                if (!customSoundUri.isNullOrBlank()) {
                    val soundUri = android.net.Uri.parse(customSoundUri)
                    val audioAttributes = android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                }
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {
        override fun doWork(): Result {
            val title = inputData.getString("title") ?: "Habit Reminder"
            val text = inputData.getString("text") ?: "Don't forget to complete your habit today!"
            val soundEnabled = inputData.getBoolean("soundEnabled", true)
            val customSoundUri = inputData.getString("customSoundUri")
            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
            if (customSoundUri != null && soundEnabled) {
                val soundUri = android.net.Uri.parse(customSoundUri)
                builder.setSound(soundUri)
            } else if (soundEnabled) {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
            } else {
                builder.setSound(null)
            }
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(NOTIFICATION_ID, builder.build())
            }
            return Result.success()
        }
    }
}

package com.example.myapplication.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.model.Habit
import com.example.myapplication.receivers.HabitActionReceiver
import java.time.LocalTime
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "habit_reminders_channel"
        private const val REQUEST_CODE_PREFIX = 1000 // Base request code for PendingIntents
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Notifications for habit reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = ContextCompat.getSystemService(
                context, 
                NotificationManager::class.java
            ) as NotificationManager
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleHabitReminder(habit: Habit) {
        // Only schedule if the habit has a reminder time and is enabled
        if (habit.reminderTime == null || !habit.isEnabled) {
            cancelHabitReminder(habit.id)
            return
        }
        
        try {
            // Parse the reminder time (format: "HH:mm")
            val reminderTimeString = habit.reminderTime ?: return
            val timeParts = reminderTimeString.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            // Create a Calendar for the reminder time today
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            
            // If the time has already passed today, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            // Create the notification intent that opens the app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("habit_id", habit.id)
            }
            
            val uniqueRequestCode = REQUEST_CODE_PREFIX + habit.id.hashCode()
            val pendingIntent = PendingIntent.getActivity(
                context,
                uniqueRequestCode, 
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            // Get the AlarmManager and schedule the alarm
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Use the appropriate method based on API level
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // On API 31+ without SCHEDULE_EXACT_ALARM permission, use inexact alarms
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY, // Repeat daily
                    pendingIntent
                )
            } else {
                // Schedule exact alarm if possible
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        } catch (e: Exception) {
            // Log error, but don't crash app if parsing fails
            android.util.Log.e("NotificationHelper", "Failed to schedule reminder: ${e.message}")
        }
    }
    
    fun cancelHabitReminder(habitId: String) {
        val intent = Intent(context, MainActivity::class.java)
        val uniqueRequestCode = REQUEST_CODE_PREFIX + habitId.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    
    fun showHabitReminderNotification(habit: Habit) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habit_id", habit.id)
        }
        
        val uniqueRequestCode = REQUEST_CODE_PREFIX + habit.id.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Make sure this icon exists
            .setContentTitle("Time for your habit")
            .setContentText("Reminder: ${habit.name}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Add 'Done' action button to mark habit complete
        val actionIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = "MARK_HABIT_COMPLETE"
            putExtra("habit_id", habit.id)
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueRequestCode + 1,
            actionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.addAction(R.drawable.ic_done, "Done", actionPendingIntent)

        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager
        
        notificationManager.notify(uniqueRequestCode, builder.build())
    }
}
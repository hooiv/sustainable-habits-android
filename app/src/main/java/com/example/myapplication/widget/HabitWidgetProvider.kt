package com.example.myapplication.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.myapplication.R
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.database.AppDatabase
import com.example.myapplication.core.data.repository.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val database = AppDatabase.getInstance(context)
        val repository = HabitRepository(
            database.habitDao(),
            database.habitCompletionDao()
        )

        // Use runBlocking to get the list synchronously for the widget
        val todayHabits = runBlocking {
            try {
                repository.getTodayHabits().first()
            } catch (e: Exception) {
                emptyList<Habit>()
            }
        }

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_habit).apply {
                setTextViewText(R.id.widget_title, "Today's Habits")

                // Handle empty list case
                if (todayHabits.isEmpty()) {
                    setTextViewText(R.id.widget_placeholder, "No habits for today")
                } else {
                    // Use the first habit for simplicity (in a real app, you'd use RemoteViewsService/RemoteViewsFactory for a list)
                    val habit = todayHabits[0]
                    setTextViewText(R.id.widget_placeholder, habit.name)

                    // Create intent for marking habit complete
                    val habitIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                        action = "MARK_HABIT_COMPLETE"
                        putExtra("habitId", habit.id)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        habit.id.hashCode(),
                        habitIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_placeholder, pendingIntent)
                }
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "MARK_HABIT_COMPLETE") {
            val habitId = intent.getStringExtra("habitId")
            if (habitId != null) {
                val database = AppDatabase.getInstance(context)
                val repository = HabitRepository(
                    database.habitDao(),
                    database.habitCompletionDao()
                )

                // Use runBlocking for simplicity in widget provider
                runBlocking {
                    repository.markHabitCompleted(habitId)
                }

                // Update all widgets
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    intent.component
                )
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }
}

package com.example.myapplication.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.myapplication.R
import com.example.myapplication.features.habits.HabitsScreen
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.repository.HabitRepository

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repository = HabitRepository(context) // Assuming a repository exists
        val todayHabits = repository.getTodayHabits() // Fetch today's habits

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_habit).apply {
                setTextViewText(R.id.widget_title, "Today's Habits")

                // Dynamically add habits to the widget
                todayHabits.forEachIndexed { index, habit ->
                    val habitIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                        action = "MARK_HABIT_COMPLETE"
                        putExtra("habitId", habit.id)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(context, index, habitIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                    setTextViewText(R.id.widget_placeholder, habit.name)
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
            val repository = HabitRepository(context)
            repository.markHabitComplete(habitId)
        }
    }
}
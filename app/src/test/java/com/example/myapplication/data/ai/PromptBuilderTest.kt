package com.example.myapplication.data.ai

import com.example.myapplication.core.data.model.*
import com.google.gson.Gson
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit4.MockKRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PromptBuilderTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @InjectMockKs
    private lateinit var promptBuilder: PromptBuilder

    @Test
    fun `buildMessages includes all context`() {
        val messages = promptBuilder.buildMessages(
            question = "How can I improve?",
            userHabits = listOf(Habit(id = "1", name = "Read", goalValue = 10, goalUnit = "pages", frequency = "daily")),
            habitCompletions = listOf(HabitCompletion(habitId = "1", date = "2025-09-08", value = 10)),
            moodData = listOf(MoodEntry(date = "2025-09-08", mood = 4, energyLevel = 3, stressLevel = 2)),
            locationData = listOf(LocationContext(date = "2025-09-08", locationType = "Home")),
            timePatterns = listOf(TimePattern(patternName = "Morning Routine", startTime = "08:00", endTime = "09:00")),
            personalization = AIAssistantPersonalization(includeMoodData = true, includeLocationData = true, includeTimePatterns = true)
        )

        val content = messages.joinToString { it.content }
        assertTrue(content.contains("Read"))
        assertTrue(content.contains("pages"))
        assertTrue(content.contains("Mood: 4"))
        assertTrue(content.contains("Home"))
        assertTrue(content.contains("Morning Routine"))
    }
}

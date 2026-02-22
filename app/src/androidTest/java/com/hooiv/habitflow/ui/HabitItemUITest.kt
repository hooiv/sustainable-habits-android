package com.hooiv.habitflow.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hooiv.habitflow.core.data.model.Habit
import com.hooiv.habitflow.features.habits.ui.HabitItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitItemUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun habitItem_displaysHabitName() {
        // Given
        val testHabit = Habit(name = "Test Habit", description = "Test Description")

        // When
        composeTestRule.setContent {
            HabitItem(
                habit = testHabit,
                onComplete = {},
                onEdit = {},
                onDelete = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test Habit").assertIsDisplayed()
    }

    @Test
    fun habitItem_clickComplete_triggersCallback() {
        // Given
        val testHabit = Habit(name = "Test Habit")
        var completeCalled = false

        // When
        composeTestRule.setContent {
            HabitItem(
                habit = testHabit,
                onComplete = { completeCalled = true },
                onEdit = {},
                onDelete = {}
            )
        }

        // Click the complete button (assuming it has a checkmark icon)
        composeTestRule.onNodeWithContentDescription("Complete habit").performClick()

        // Then
        assert(completeCalled)
    }
}

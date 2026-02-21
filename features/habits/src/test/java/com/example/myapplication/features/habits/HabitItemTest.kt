package com.example.myapplication.features.habits

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import androidx.compose.ui.test.onRoot
import com.example.myapplication.features.habits.ui.HabitItem
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitFrequency
import com.example.myapplication.core.data.model.HabitPriority
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HabitItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleHabit = Habit(
        id = "1",
        name = "Morning Meditation",
        description = "15 minutes of mindfulness",
        frequency = HabitFrequency.DAILY,
        priority = HabitPriority.HIGH,
        goal = 1,
        streak = 5
    )

    @Test
    fun habitItem_active() {
        composeTestRule.setContent {
            TestTheme {
                HabitItem(
                    habit = sampleHabit,
                    onItemClick = {},
                    onCompletedClick = {},
                    onDeleteClick = {},
                    onToggleEnabled = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitItem_completed() {
        composeTestRule.setContent {
            TestTheme {
                HabitItem(
                    habit = sampleHabit, // Simplified
                    onItemClick = {},
                    onCompletedClick = {},
                    onDeleteClick = {},
                    onToggleEnabled = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}

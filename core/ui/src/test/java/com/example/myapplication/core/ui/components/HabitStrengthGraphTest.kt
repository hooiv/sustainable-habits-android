package com.example.myapplication.core.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import androidx.compose.ui.test.onRoot
import com.example.myapplication.core.ui.theme.TestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HabitStrengthGraphTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun habitStrengthGraph_empty() {
        composeTestRule.setContent {
            TestTheme {
                HabitStrengthGraph(
                    dataPoints = emptyList()
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitStrengthGraph_rising() {
        composeTestRule.setContent {
            TestTheme {
                HabitStrengthGraph(
                    dataPoints = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitStrengthGraph_fluctuating() {
        composeTestRule.setContent {
            TestTheme {
                HabitStrengthGraph(
                    dataPoints = listOf(0.8f, 0.2f, 0.9f, 0.3f, 0.7f)
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}

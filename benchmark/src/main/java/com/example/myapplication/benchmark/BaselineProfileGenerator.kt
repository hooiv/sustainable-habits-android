package com.example.myapplication.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = "com.example.myapplication",
            includeInStartupProfile = true
        ) {
            // Start the default activity
            pressHome()
            startActivityAndWait()

            // Scroll the habit list to capture JIT for the list
            val habitList = device.findObject(By.res("habit_list"))
            if (habitList != null) {
                habitList.setGestureMargin(device.displayWidth / 5)
                habitList.fling(Direction.DOWN)
                device.waitForIdle()
                habitList.fling(Direction.UP)
                device.waitForIdle()
            }
            
            // Navigate to Add Habit to capture more compilation routes
            val addHabitBtn = device.findObject(By.text("Add Habit"))
            if (addHabitBtn != null) {
                addHabitBtn.click()
                device.waitForIdle()
                device.pressBack()
                device.waitForIdle()
            }
        }
    }
}

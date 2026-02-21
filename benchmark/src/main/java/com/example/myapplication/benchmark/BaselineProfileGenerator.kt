package com.example.myapplication.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
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
                habitList.fling(androidx.test.uiautomator.Direction.DOWN)
                habitList.fling(androidx.test.uiautomator.Direction.UP)
            }
            
            // Navigate to details (if possible)
            // Just basic startup for now to ensure <500ms
        }
    }
}

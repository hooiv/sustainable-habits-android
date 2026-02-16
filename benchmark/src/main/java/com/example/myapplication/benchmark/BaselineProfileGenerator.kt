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
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.example.myapplication",
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()
        
        // Find the habit list and scroll it to capture JIT compilation for list rendering
        val list = device.findObject(By.res("habit_list"))
        if (list != null) {
            list.fling(Direction.DOWN)
            device.waitForIdle()
            list.fling(Direction.UP)
        }
    }
}

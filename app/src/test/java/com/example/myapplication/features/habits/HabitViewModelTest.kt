package com.example.myapplication.features.habits.viewmodel

import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitFrequency
import com.example.myapplication.core.data.repository.HabitRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelTest {

    @MockK
    private lateinit var repository: HabitRepository

    private lateinit var viewModel: HabitViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = HabitViewModel(repository)
    }

    @Test
    fun `addHabit calls repository insertHabit`() = runTest {
        // When
        viewModel.addHabit(
            name = "Test Habit",
            description = "Test Description",
            category = "Health",
            frequency = HabitFrequency.DAILY,
            goal = 1
        )

        // Then
        coVerify { repository.insertHabit(any()) }
    }

    @Test
    fun `deleteHabit calls repository deleteHabit`() = runTest {
        // Given
        val habit = Habit(name = "Test Habit")

        // When
        viewModel.deleteHabit(habit)

        // Then
        coVerify { repository.deleteHabit(habit) }
    }

    @Test
    fun `habits flow is properly exposed from repository`() = runTest {
        // Given
        val testHabits = listOf(
            Habit(name = "Habit 1"),
            Habit(name = "Habit 2")
        )
        coEvery { repository.getAllHabits() } returns flowOf(testHabits)

        // When
        val result = viewModel.habits.value

        // Then
        assertEquals(testHabits, result)
    }
}

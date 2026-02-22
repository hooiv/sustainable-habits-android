package com.example.myapplication.features.habits

import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitFrequency
import com.example.myapplication.core.domain.usecase.AddHabitUseCase
import com.example.myapplication.core.domain.usecase.DeleteHabitUseCase
import com.example.myapplication.core.domain.usecase.GetHabitByIdUseCase
import com.example.myapplication.core.domain.usecase.GetHabitsUseCase
import com.example.myapplication.core.domain.usecase.InsertOrReplaceHabitsUseCase
import com.example.myapplication.core.domain.usecase.MarkHabitCompletedUseCase
import com.example.myapplication.core.domain.usecase.UpdateHabitUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK private lateinit var getHabitsUseCase: GetHabitsUseCase
    @MockK private lateinit var addHabitUseCase: AddHabitUseCase
    @MockK private lateinit var deleteHabitUseCase: DeleteHabitUseCase
    @MockK private lateinit var getHabitByIdUseCase: GetHabitByIdUseCase
    @MockK private lateinit var updateHabitUseCase: UpdateHabitUseCase
    @MockK private lateinit var markHabitCompletedUseCase: MarkHabitCompletedUseCase
    @MockK private lateinit var insertOrReplaceHabitsUseCase: InsertOrReplaceHabitsUseCase

    private lateinit var viewModel: HabitViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        // GetHabitsUseCase is called in init — stub it before constructing the ViewModel
        every { getHabitsUseCase() } returns flowOf(emptyList())
        viewModel = HabitViewModel(
            getHabitsUseCase,
            addHabitUseCase,
            deleteHabitUseCase,
            getHabitByIdUseCase,
            updateHabitUseCase,
            markHabitCompletedUseCase,
            insertOrReplaceHabitsUseCase,
            testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addHabit delegates to AddHabitUseCase`() = runTest {
        coEvery {
            addHabitUseCase(any(), any(), any(), any(), any(), any())
        } returns Unit

        viewModel.addHabit(
            name = "Test Habit",
            description = "Test Description",
            category = "Health",
            frequency = HabitFrequency.DAILY,
            goal = 1
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { addHabitUseCase("Test Habit", "Test Description", "Health", HabitFrequency.DAILY, 1, null) }
    }

    @Test
    fun `deleteHabit delegates to DeleteHabitUseCase`() = runTest {
        val habit = Habit(name = "Test Habit")
        coEvery { deleteHabitUseCase(habit) } returns Unit

        viewModel.deleteHabit(habit)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteHabitUseCase(habit) }
    }

    @Test
    fun `habits StateFlow reflects GetHabitsUseCase output`() = runTest {
        val testHabits = listOf(Habit(name = "Habit 1"), Habit(name = "Habit 2"))
        every { getHabitsUseCase() } returns flowOf(testHabits)

        val freshViewModel = HabitViewModel(
            getHabitsUseCase,
            addHabitUseCase,
            deleteHabitUseCase,
            getHabitByIdUseCase,
            updateHabitUseCase,
            markHabitCompletedUseCase,
            insertOrReplaceHabitsUseCase,
            testDispatcher
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(testHabits, freshViewModel.habits.value)
    }
}

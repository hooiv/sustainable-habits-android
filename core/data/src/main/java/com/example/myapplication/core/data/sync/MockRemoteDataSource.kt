package com.example.myapplication.core.data.sync

import com.example.myapplication.core.data.model.Habit
import kotlinx.coroutines.delay
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Simulates a remote backend API for synchronization testing.
 * Introduce artificial latency and random errors to test resilience.
 */
@Singleton
class MockRemoteDataSource @Inject constructor() {

    private val remoteDb = mutableMapOf<String, Habit>()

    // Simulate network latency (500ms - 2000ms)
    private suspend fun simulateNetwork() {
        delay(Random.nextLong(500, 2000))
        // Simulate 10% failure rate
        if (Random.nextFloat() < 0.1f) {
            throw Exception("Network Timeout (Simulated)")
        }
    }

    suspend fun pushHabits(habits: List<Habit>) {
        simulateNetwork()
        habits.forEach { habit ->
            remoteDb[habit.id] = habit.copy(isSynced = true)
        }
    }

    suspend fun fetchMainHabitsBefore(timestamp: Long): List<Habit> {
        simulateNetwork()
        // Return habits updated on "server" after the given timestamp
        return remoteDb.values.filter { 
            it.lastUpdatedTimestamp.time > timestamp 
        }
    }

    // Identify conflicts where server has a newer version than client
    suspend fun fetchConflicts(clientHabits: List<Habit>): List<Habit> {
        simulateNetwork()
        return clientHabits.mapNotNull { clientHabit ->
            val serverHabit = remoteDb[clientHabit.id]
            if (serverHabit != null && serverHabit.lastUpdatedTimestamp.time > clientHabit.lastUpdatedTimestamp.time) {
                serverHabit // Server wins
            } else {
                null
            }
        }
    }
}

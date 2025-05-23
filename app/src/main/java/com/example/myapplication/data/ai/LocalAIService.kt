package com.example.myapplication.data.ai

import android.util.Log
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitCompletion
import com.example.myapplication.data.model.HabitFrequency
import com.example.myapplication.features.ai.AISuggestion
import com.example.myapplication.features.ai.SuggestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local implementation of AIService that doesn't require external API calls
 * This is used as a fallback when the API is not available or when the user doesn't want to use an external API
 */
@Singleton
class LocalAIService @Inject constructor() : AIService {
    companion object {
        private const val TAG = "LocalAIService"

        // Simulate processing delay for more realistic experience
        private const val PROCESSING_DELAY_MS = 1000L
    }

    /**
     * Generate a response to a user question about habits
     */
    override suspend fun generateResponse(
        question: String,
        userHabits: List<Habit>?,
        habitCompletions: List<HabitCompletion>?
    ): String = withContext(Dispatchers.Default) {
        // Simulate processing time
        delay(PROCESSING_DELAY_MS)

        Log.d(TAG, "Generating response for question: $question")

        // Include completion data in log
        if (habitCompletions != null && habitCompletions.isNotEmpty()) {
            Log.d(TAG, "With ${habitCompletions.size} habit completions")
        }

        val lowercaseQuestion = question.lowercase()

        // Generate response based on question content
        val baseResponse = when {
            lowercaseQuestion.contains("new habit") || lowercaseQuestion.contains("suggest") -> {
                generateNewHabitSuggestion(userHabits ?: emptyList(), habitCompletions)
            }
            lowercaseQuestion.contains("schedule") || lowercaseQuestion.contains("time") -> {
                generateScheduleOptimization(userHabits ?: emptyList(), habitCompletions)
            }
            lowercaseQuestion.contains("motivat") || lowercaseQuestion.contains("stuck") -> {
                generateMotivationTips(userHabits ?: emptyList(), habitCompletions)
            }
            lowercaseQuestion.contains("improve") || lowercaseQuestion.contains("better") -> {
                generateHabitImprovementTips(userHabits ?: emptyList(), habitCompletions)
            }
            lowercaseQuestion.contains("streak") || lowercaseQuestion.contains("chain") -> {
                generateStreakProtectionTips(userHabits ?: emptyList(), habitCompletions)
            }
            lowercaseQuestion.contains("routine") || lowercaseQuestion.contains("combine") -> {
                generateHabitChainSuggestions(userHabits ?: emptyList(), habitCompletions)
            }
            lowercaseQuestion.contains("analyze") || lowercaseQuestion.contains("insight") -> {
                generateInsightAnalysis(userHabits ?: emptyList(), habitCompletions)
            }
            lowercaseQuestion.contains("progress") || lowercaseQuestion.contains("history") -> {
                generateCompletionAnalysis(userHabits, habitCompletions)
            }
            else -> {
                "That's an interesting question about habits. While I don't have a specific answer prepared, I can tell you that the most effective habits are small, consistent, and tied to existing routines. Would you like me to suggest a new habit or help you optimize your current ones?"
            }
        }

        return@withContext baseResponse
    }

    /**
     * Generate a streaming response to a user question about habits
     */
    override fun generateStreamingResponse(
        question: String,
        userHabits: List<Habit>?,
        habitCompletions: List<HabitCompletion>?
    ): Flow<String> = flow {
        // Get the full response
        val fullResponse = generateResponse(question, userHabits, habitCompletions)

        // Split the response into words
        val words = fullResponse.split(" ")

        // Emit words with a small delay to simulate streaming
        for (i in words.indices) {
            val chunk = if (i == 0) words[i] else " ${words[i]}"
            emit(chunk)
            delay(50) // 50ms delay between words
        }
    }.flowOn(Dispatchers.Default)

    /**
     * Generate completion analysis based on habit history
     */
    private fun generateCompletionAnalysis(
        userHabits: List<Habit>?,
        habitCompletions: List<HabitCompletion>?
    ): String {
        if (habitCompletions.isNullOrEmpty()) {
            return "I don't see any habit completion history yet. Start completing your habits regularly, and I'll be able to provide insights on your progress patterns."
        }

        // Group completions by habit
        val completionsByHabit = habitCompletions.groupBy { it.habitId }

        // Create a map of habit IDs to names
        val habitNames = userHabits?.associateBy({ it.id }, { it.name }) ?: emptyMap()

        // Build analysis
        val analysis = StringBuilder("Based on your habit completion history:\n\n")

        // Most consistent habit
        val mostConsistentHabitId = completionsByHabit.maxByOrNull { it.value.size }?.key
        if (mostConsistentHabitId != null) {
            val habitName = habitNames[mostConsistentHabitId] ?: "Unknown habit"
            val completionCount = completionsByHabit[mostConsistentHabitId]?.size ?: 0
            analysis.append("• Your most consistent habit is **$habitName** with $completionCount completions\n\n")
        }

        // Time patterns
        val timePatterns = analyzeTimePatterns(habitCompletions)
        if (timePatterns.isNotEmpty()) {
            analysis.append("• $timePatterns\n\n")
        }

        // Mood analysis if available
        val moodAnalysis = analyzeMoods(habitCompletions)
        if (moodAnalysis.isNotEmpty()) {
            analysis.append("• $moodAnalysis\n\n")
        }

        // Recent progress
        val recentProgress = analyzeRecentProgress(habitCompletions)
        analysis.append("• $recentProgress\n\n")

        // Recommendation
        analysis.append("Recommendation: ${generateRecommendationFromCompletions(habitCompletions, userHabits)}")

        return analysis.toString()
    }

    /**
     * Analyze time patterns in habit completions
     */
    private fun analyzeTimePatterns(completions: List<HabitCompletion>): String {
        // Extract hour of day for each completion
        val hours = completions.map {
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = it.completionDate
            calendar.get(java.util.Calendar.HOUR_OF_DAY)
        }

        // Count completions by time of day
        val morning = hours.count { it in 5..11 }
        val afternoon = hours.count { it in 12..17 }
        val evening = hours.count { it in 18..22 }
        val night = hours.count { it in 23..23 || it in 0..4 }

        // Determine peak time
        val timeMap = mapOf(
            "morning" to morning,
            "afternoon" to afternoon,
            "evening" to evening,
            "night" to night
        )

        val peakTime = timeMap.maxByOrNull { it.value }?.key ?: "no specific time"

        return "You tend to complete habits most often in the $peakTime"
    }

    /**
     * Analyze mood patterns in habit completions
     */
    private fun analyzeMoods(completions: List<HabitCompletion>): String {
        // Filter completions with mood data
        val completionsWithMood = completions.filter { it.mood != null }

        if (completionsWithMood.isEmpty()) {
            return ""
        }

        // Calculate average mood
        val avgMood = completionsWithMood.map { it.mood!! }.average()

        return when {
            avgMood >= 4.5 -> "Your average mood during habit completions is excellent (${String.format("%.1f", avgMood)}/5)"
            avgMood >= 3.5 -> "Your average mood during habit completions is good (${String.format("%.1f", avgMood)}/5)"
            avgMood >= 2.5 -> "Your average mood during habit completions is moderate (${String.format("%.1f", avgMood)}/5)"
            else -> "Your average mood during habit completions is low (${String.format("%.1f", avgMood)}/5)"
        }
    }

    /**
     * Analyze recent progress in habit completions
     */
    private fun analyzeRecentProgress(completions: List<HabitCompletion>): String {
        // Sort by date
        val sortedCompletions = completions.sortedBy { it.completionDate }

        // If we have less than 7 completions, just report the count
        if (sortedCompletions.size < 7) {
            return "You have ${sortedCompletions.size} habit completions recorded so far"
        }

        // Compare recent week to previous week
        val calendar = java.util.Calendar.getInstance()
        val currentTime = calendar.timeInMillis

        calendar.timeInMillis = currentTime
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
        val oneWeekAgo = calendar.timeInMillis

        calendar.timeInMillis = currentTime
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -14)
        val twoWeeksAgo = calendar.timeInMillis

        val lastWeekCompletions = completions.count { it.completionDate >= oneWeekAgo && it.completionDate < currentTime }
        val previousWeekCompletions = completions.count { it.completionDate >= twoWeeksAgo && it.completionDate < oneWeekAgo }

        return when {
            lastWeekCompletions > previousWeekCompletions * 1.2 ->
                "Your habit completion rate has improved in the last week ($lastWeekCompletions vs $previousWeekCompletions completions)"
            lastWeekCompletions < previousWeekCompletions * 0.8 ->
                "Your habit completion rate has decreased in the last week ($lastWeekCompletions vs $previousWeekCompletions completions)"
            else ->
                "Your habit completion rate has been consistent over the last two weeks"
        }
    }

    /**
     * Generate a recommendation based on completion history
     */
    private fun generateRecommendationFromCompletions(
        completions: List<HabitCompletion>,
        habits: List<Habit>?
    ): String {
        if (completions.isEmpty()) {
            return "Start tracking your habit completions to get personalized recommendations."
        }

        // Group completions by habit
        val completionsByHabit = completions.groupBy { it.habitId }

        // Find habits with few or no completions
        val habitIds = habits?.map { it.id } ?: emptyList()
        val neglectedHabitIds = habitIds.filter { id ->
            completionsByHabit[id]?.size ?: 0 < 3
        }

        if (neglectedHabitIds.isNotEmpty() && habits != null) {
            val neglectedHabit = habits.find { it.id == neglectedHabitIds.first() }
            if (neglectedHabit != null) {
                return "Focus on improving consistency with your '${neglectedHabit.name}' habit, which has fewer completions than your other habits."
            }
        }

        // Analyze time patterns
        val hours = completions.map {
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = it.completionDate
            calendar.get(java.util.Calendar.HOUR_OF_DAY)
        }

        val earlyMorning = hours.count { it in 5..8 }
        val lateMorning = hours.count { it in 9..11 }
        val earlyAfternoon = hours.count { it in 12..14 }
        val lateAfternoon = hours.count { it in 15..17 }
        val evening = hours.count { it in 18..22 }

        val timeMap = mapOf(
            "early morning (5-8 AM)" to earlyMorning,
            "late morning (9-11 AM)" to lateMorning,
            "early afternoon (12-2 PM)" to earlyAfternoon,
            "late afternoon (3-5 PM)" to lateAfternoon,
            "evening (6-10 PM)" to evening
        )

        val leastUsedTime = timeMap.filter { it.value < (completions.size / 10) }.keys.firstOrNull()

        return if (leastUsedTime != null) {
            "Try scheduling some of your habits during $leastUsedTime to create a more balanced daily routine."
        } else {
            "Continue your current habit schedule, which shows good distribution throughout the day."
        }
    }

    /**
     * Generate personalized habit suggestions
     */
    override suspend fun generateSuggestions(
        userHabits: List<Habit>,
        previousSuggestion: AISuggestion?,
        habitCompletions: List<HabitCompletion>?
    ): List<AISuggestion> = withContext(Dispatchers.Default) {
        // Simulate processing time
        delay(PROCESSING_DELAY_MS)

        Log.d(TAG, "Generating suggestions based on previous suggestion: ${previousSuggestion?.title}")

        // Generate suggestions based on previous suggestion type
        val suggestions = when (previousSuggestion?.type) {
            SuggestionType.NEW_HABIT -> listOf(
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Make this habit stick",
                    description = "Strategies for habit formation",
                    type = SuggestionType.MOTIVATION,
                    confidence = 0.89f
                ),
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Optimal timing",
                    description = "Find the best time for this habit",
                    type = SuggestionType.SCHEDULE_OPTIMIZATION,
                    confidence = 0.85f
                ),
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Create a habit chain",
                    description = "Connect this habit with others",
                    type = SuggestionType.HABIT_CHAIN,
                    confidence = 0.82f
                )
            )
            SuggestionType.SCHEDULE_OPTIMIZATION -> listOf(
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Track consistency",
                    description = "Methods to monitor your progress",
                    type = SuggestionType.INSIGHT,
                    confidence = 0.82f
                ),
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Complementary habit",
                    description = "Add a habit that works well with your current ones",
                    type = SuggestionType.NEW_HABIT,
                    confidence = 0.78f
                ),
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Protect your streak",
                    description = "Strategies for maintaining consistency",
                    type = SuggestionType.STREAK_PROTECTION,
                    confidence = 0.75f
                )
            )
            SuggestionType.MOTIVATION -> listOf(
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Visualize benefits",
                    description = "Create a mental image of success",
                    type = SuggestionType.MOTIVATION,
                    confidence = 0.91f
                ),
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Track your progress",
                    description = "Use visual cues to see your improvement",
                    type = SuggestionType.INSIGHT,
                    confidence = 0.87f
                ),
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Join a community",
                    description = "Connect with others with similar goals",
                    type = SuggestionType.HABIT_IMPROVEMENT,
                    confidence = 0.83f
                )
            )
            else -> listOf(
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Start a daily meditation practice",
                    description = "Begin with just 2 minutes each morning",
                    type = SuggestionType.NEW_HABIT,
                    confidence = 0.95f
                ),
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Stack habits for better consistency",
                    description = "Connect new habits to existing routines",
                    type = SuggestionType.HABIT_IMPROVEMENT,
                    confidence = 0.88f
                ),
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Create a habit emergency plan",
                    description = "Have a minimal version of each habit for busy days",
                    type = SuggestionType.STREAK_PROTECTION,
                    confidence = 0.85f
                ),
                AISuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Analyze your habit patterns",
                    description = "Identify trends in your habit completion",
                    type = SuggestionType.INSIGHT,
                    confidence = 0.82f
                )
            )
        }

        return@withContext suggestions
    }

    /**
     * Generate a new habit suggestion
     */
    override suspend fun generateNewHabitSuggestion(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?
    ): String = withContext(Dispatchers.Default) {
        // Simulate processing time
        delay(PROCESSING_DELAY_MS)

        // Base habits to suggest
        val baseHabits = listOf(
            "Drink a glass of water first thing in the morning",
            "Take a 10-minute walk after lunch",
            "Practice deep breathing for 2 minutes before bed",
            "Write down three things you're grateful for each day",
            "Read for 15 minutes before bed instead of using your phone",
            "Stretch for 5 minutes when you wake up",
            "Take a moment to tidy one small area of your home each day",
            "Eat one extra serving of vegetables each day",
            "Spend 10 minutes planning your day each morning",
            "Practice mindfulness during your daily commute"
        )

        // If we have completion data, use it to make more personalized suggestions
        val suggestion = if (!habitCompletions.isNullOrEmpty() && habitCompletions.size > 5) {
            // Analyze completion times to suggest habits at optimal times
            val hours = habitCompletions.map {
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = it.completionDate
                calendar.get(java.util.Calendar.HOUR_OF_DAY)
            }

            // Determine when user is most active with habits
            val morningActive = hours.count { it in 5..11 } > hours.count { it in 12..23 }

            if (morningActive) {
                // Suggest morning habits
                listOf(
                    "Start your day with a 2-minute meditation right after waking up",
                    "Drink a glass of water with lemon first thing in the morning",
                    "Do 5 minutes of stretching before getting out of bed",
                    "Write down your top 3 priorities for the day while having breakfast"
                ).random()
            } else {
                // Suggest evening habits
                listOf(
                    "Take 5 minutes to tidy up your space before bed",
                    "Do a quick 2-minute reflection on your day before sleeping",
                    "Prepare your clothes for tomorrow evening",
                    "Read one page of a book before bed instead of using your phone"
                ).random()
            }
        } else {
            // Without completion data, use the base suggestions
            baseHabits.random()
        }

        return@withContext "Based on sustainable habit formation principles, here's a suggestion: $suggestion\n\n" +
               "This habit is effective because it's small, specific, and can be easily integrated into your existing routine. " +
               "Start with just 2 minutes per day to build consistency, then gradually increase as it becomes automatic."
    }

    /**
     * Generate schedule optimization advice
     */
    override suspend fun generateScheduleOptimization(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?
    ): String = withContext(Dispatchers.Default) {
        // Simulate processing time
        delay(PROCESSING_DELAY_MS)

        // Base advice
        val baseAdvice = "For optimal habit scheduling, consider these principles:\n\n" +
               "1. **Habit stacking**: Attach new habits to existing ones (e.g., meditate after brushing teeth)\n\n" +
               "2. **Energy matching**: Schedule demanding habits when your energy naturally peaks\n\n" +
               "3. **Implementation intentions**: Use specific time/place triggers (e.g., \"After I pour my morning coffee, I'll write in my journal\")\n\n" +
               "4. **Buffer time**: Allow transition time between habits to reduce stress\n\n" +
               "5. **Consistency**: Same time each day builds stronger habit cues"

        // If we have completion data, add personalized advice
        if (!habitCompletions.isNullOrEmpty() && habitCompletions.size > 5) {
            // Analyze completion times
            val hours = habitCompletions.map {
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = it.completionDate
                calendar.get(java.util.Calendar.HOUR_OF_DAY)
            }

            // Count completions by time of day
            val morning = hours.count { it in 5..11 }
            val afternoon = hours.count { it in 12..17 }
            val evening = hours.count { it in 18..22 }

            // Determine peak time
            val timeMap = mapOf(
                "morning (5-11 AM)" to morning,
                "afternoon (12-5 PM)" to afternoon,
                "evening (6-10 PM)" to evening
            )

            val peakTime = timeMap.maxByOrNull { it.value }?.key ?: "no specific time"
            val personalizedAdvice = "\n\nBased on your habit completion history, you're most consistent in the $peakTime. " +
                "Consider scheduling your most challenging habits during this time when you've demonstrated the most consistency."

            return@withContext baseAdvice + personalizedAdvice
        }

        return@withContext baseAdvice
    }

    /**
     * Generate motivation tips
     */
    override suspend fun generateMotivationTips(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?
    ): String = withContext(Dispatchers.Default) {
        // Simulate processing time
        delay(PROCESSING_DELAY_MS)

        // Base motivation tips
        val baseTips = "To maintain motivation with your habits:\n\n" +
               "1. **Focus on identity**: See yourself as the type of person who does this habit\n\n" +
               "2. **Celebrate small wins**: Acknowledge each completion, no matter how small\n\n" +
               "3. **Make it enjoyable**: Pair habits with something you love (like favorite music)\n\n" +
               "4. **Visualize benefits**: Regularly imagine the positive outcomes\n\n" +
               "5. **Join a community**: Connect with others pursuing similar habits\n\n" +
               "6. **Track visibly**: Use a method that shows your progress streak\n\n" +
               "Remember that motivation follows action, not the other way around. Often, just starting the habit will generate the motivation to continue."

        // If we have completion data, add personalized motivation
        if (!habitCompletions.isNullOrEmpty() && habitCompletions.size > 3) {
            // Find the habit with the most completions
            val habitIdToCompletionCount = habitCompletions.groupBy { it.habitId }
                .mapValues { it.value.size }

            val mostCompletedHabitId = habitIdToCompletionCount.maxByOrNull { it.value }?.key

            if (mostCompletedHabitId != null) {
                val mostCompletedHabit = userHabits.find { it.id == mostCompletedHabitId }

                if (mostCompletedHabit != null) {
                    val personalizedTip = "\n\nPersonalized tip: You've been most consistent with your \"${mostCompletedHabit.name}\" habit. " +
                        "Use this success as motivation for your other habits by applying the same techniques that worked for this habit."

                    return@withContext baseTips + personalizedTip
                }
            }
        }

        return@withContext baseTips
    }

    /**
     * Generate habit improvement tips
     */
    override suspend fun generateHabitImprovementTips(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?
    ): String = withContext(Dispatchers.Default) {
        // Simulate processing time
        delay(PROCESSING_DELAY_MS)

        // Base improvement tips
        val baseTips = "Here are strategies to improve your existing habits:\n\n" +
               "1. **Habit stacking**: Connect your habit to an existing strong habit\n\n" +
               "2. **Environment design**: Modify your environment to make the habit easier\n\n" +
               "3. **Implementation intentions**: Create specific if-then plans\n\n" +
               "4. **Minimum viable effort**: Reduce the initial effort required\n\n" +
               "5. **Progress tracking**: Use visual cues to track your progress\n\n" +
               "6. **Social accountability**: Share your habit goals with others\n\n" +
               "The most effective improvement is often making the habit easier to start rather than focusing on motivation or willpower."

        // If we have habit and completion data, add personalized improvement tips
        if (userHabits.isNotEmpty() && !habitCompletions.isNullOrEmpty()) {
            // Group completions by habit
            val completionsByHabit = habitCompletions.groupBy { it.habitId }

            // Find habits with few completions
            val habitsWithFewCompletions = userHabits.filter { habit ->
                val completionCount = completionsByHabit[habit.id]?.size ?: 0
                completionCount < 3 // Just check completion count
            }

            if (habitsWithFewCompletions.isNotEmpty()) {
                val habitToImprove = habitsWithFewCompletions.first()
                val personalizedTip = "\n\nPersonalized tip for \"${habitToImprove.name}\": " +
                    "This habit could benefit from being made easier. Consider reducing the initial effort required - " +
                    "for example, if it's a 10-minute meditation, try starting with just 2 minutes. " +
                    "Also, try placing a visual reminder in your environment where you'll see it at the right time."

                return@withContext baseTips + personalizedTip
            }
        }

        return@withContext baseTips
    }

    /**
     * Generate streak protection tips
     */
    override suspend fun generateStreakProtectionTips(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?
    ): String = withContext(Dispatchers.Default) {
        // Simulate processing time
        delay(PROCESSING_DELAY_MS)

        // Base streak protection tips
        val baseTips = "To protect your habit streaks and avoid breaking the chain:\n\n" +
               "1. **Never miss twice rule**: If you miss once, make it a priority to not miss the next day\n\n" +
               "2. **Emergency plan**: Create a minimal version of your habit for busy days\n\n" +
               "3. **Buffer days**: Build in occasional planned rest days for sustainable habits\n\n" +
               "4. **Habit tracking**: Use a visual system to make your streak tangible\n\n" +
               "5. **Recovery ritual**: Have a specific plan for getting back on track after a miss\n\n" +
               "Remember that consistency, not perfection, is the goal. A single miss doesn't erase your progress."

        // If we have habits with streaks, add personalized tips
        if (userHabits.isNotEmpty()) {
            val habitsWithStreaks = userHabits.filter { it.streak > 5 }

            if (habitsWithStreaks.isNotEmpty()) {
                val habitWithLongestStreak = habitsWithStreaks.maxByOrNull { it.streak }

                if (habitWithLongestStreak != null) {
                    val personalizedTip = "\n\nPersonalized tip: You have a ${habitWithLongestStreak.streak}-day streak with \"${habitWithLongestStreak.name}\". " +
                        "To protect this streak, create a minimal emergency version that takes just 2 minutes. " +
                        "For example, if it's a workout habit, your emergency version could be doing just 5 push-ups or a 2-minute stretch."

                    return@withContext baseTips + personalizedTip
                }
            }
        }

        return@withContext baseTips
    }

    /**
     * Generate habit chain suggestions
     */
    override suspend fun generateHabitChainSuggestions(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?
    ): String = withContext(Dispatchers.Default) {
        // Simulate processing time
        delay(PROCESSING_DELAY_MS)

        // Base habit chain suggestions
        val baseSuggestions = "Creating effective habit chains can multiply your results:\n\n" +
               "1. **Morning routine chain**: [Wake up → Drink water → Stretch → Meditate → Plan day]\n\n" +
               "2. **Evening wind-down**: [Set out clothes → Journal → Read → Sleep routine]\n\n" +
               "3. **Work productivity**: [Focus block → Short break → Hydrate → Focus block]\n\n" +
               "4. **Exercise progression**: [Warm up → Main workout → Cool down → Protein intake]\n\n" +
               "The key to effective habit chains is ensuring each habit naturally leads to the next one with minimal friction between steps."

        // If we have enough habits, suggest a personalized chain
        if (userHabits.size >= 3) {
            // Group habits by frequency
            val dailyHabits = userHabits.filter { habit ->
                habit.frequency == HabitFrequency.DAILY
            }

            if (dailyHabits.size >= 2) {
                val habitNames = dailyHabits.take(3).map { it.name }

                val personalizedChain = "\n\nPersonalized habit chain suggestion based on your existing habits:\n\n" +
                    "**Your custom chain**: [${habitNames.joinToString(" → ")}]\n\n" +
                    "Try performing these habits in sequence at the same time each day to build a powerful routine."

                return@withContext baseSuggestions + personalizedChain
            }
        }

        return@withContext baseSuggestions
    }

    /**
     * Generate insight analysis
     */
    override suspend fun generateInsightAnalysis(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?
    ): String = withContext(Dispatchers.Default) {
        // Simulate processing time
        delay(PROCESSING_DELAY_MS)

        val habitCount = userHabits.size
        val streakInfo = if (habitCount > 0) {
            val avgStreak = userHabits.map { it.streak }.average().toInt()
            val maxStreak = userHabits.maxOfOrNull { it.streak } ?: 0
            "Your average streak is $avgStreak days, with your longest streak being $maxStreak days."
        } else {
            "You haven't established any habit streaks yet."
        }

        // Base analysis
        var analysis = "Based on your habit data, here's an analysis of your progress:\n\n" +
               "• You currently have $habitCount active habits\n\n" +
               "• $streakInfo\n\n" +
               "• Your habits are focused on ${if (habitCount > 0) getHabitFocus(userHabits) else "no specific area yet"}\n\n" +
               "• ${if (habitCount > 0) "Your most consistent habit is ${getMostConsistentHabit(userHabits)}" else "Start with one simple daily habit to build momentum"}"

        // Add completion insights if available
        if (!habitCompletions.isNullOrEmpty() && habitCompletions.size > 5) {
            // Analyze completion times
            val completionTimes = habitCompletions.map {
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = it.completionDate
                calendar.get(java.util.Calendar.HOUR_OF_DAY)
            }

            // Determine peak completion time
            val morning = completionTimes.count { it in 5..11 }
            val afternoon = completionTimes.count { it in 12..17 }
            val evening = completionTimes.count { it in 18..22 }
            val night = completionTimes.count { it in 23..23 || it in 0..4 }

            val timeMap = mapOf(
                "morning" to morning,
                "afternoon" to afternoon,
                "evening" to evening,
                "night" to night
            )

            val peakTime = timeMap.maxByOrNull { it.value }?.key ?: "no specific time"

            // Add completion time insight
            analysis += "\n\n• You tend to complete habits most often in the $peakTime"

            // Analyze day of week patterns
            val dayOfWeekCounts = habitCompletions.map {
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = it.completionDate
                calendar.get(java.util.Calendar.DAY_OF_WEEK)
            }.groupBy { it }.mapValues { it.value.size }

            val weekdayCount = dayOfWeekCounts.filter { it.key in 2..6 }.values.sum()
            val weekendCount = dayOfWeekCounts.filter { it.key == 1 || it.key == 7 }.values.sum()

            // Normalize by number of days (5 weekdays vs 2 weekend days)
            val weekdayAvg = if (weekdayCount > 0) weekdayCount / 5.0 else 0.0
            val weekendAvg = if (weekendCount > 0) weekendCount / 2.0 else 0.0

            // Add weekday/weekend insight
            if (weekdayAvg > 0 || weekendAvg > 0) {
                val weekdayWeekendRatio = if (weekendAvg > 0) weekdayAvg / weekendAvg else Double.POSITIVE_INFINITY

                analysis += if (weekdayWeekendRatio > 1.5) {
                    "\n\n• You're significantly more consistent on weekdays than weekends"
                } else if (weekdayWeekendRatio < 0.67) {
                    "\n\n• You're more consistent on weekends than weekdays"
                } else {
                    "\n\n• You maintain similar consistency on both weekdays and weekends"
                }
            }
        }

        // Add recommendation
        analysis += "\n\nRecommendation: ${getRecommendation(userHabits)}"

        return@withContext analysis
    }

    /**
     * Get the focus area of the user's habits
     */
    private fun getHabitFocus(habits: List<Habit>): String {
        if (habits.isEmpty()) return "no specific area"

        // Simple categorization based on habit names and descriptions
        val healthCount = habits.count { habit ->
            habit.name.contains("health", ignoreCase = true) ||
            habit.name.contains("exercise", ignoreCase = true) ||
            habit.name.contains("workout", ignoreCase = true) ||
            habit.description?.contains("health", ignoreCase = true) == true
        }

        val productivityCount = habits.count { habit ->
            habit.name.contains("work", ignoreCase = true) ||
            habit.name.contains("study", ignoreCase = true) ||
            habit.name.contains("productivity", ignoreCase = true) ||
            habit.description?.contains("productivity", ignoreCase = true) == true
        }

        val wellbeingCount = habits.count { habit ->
            habit.name.contains("meditation", ignoreCase = true) ||
            habit.name.contains("mindfulness", ignoreCase = true) ||
            habit.name.contains("journal", ignoreCase = true) ||
            habit.description?.contains("wellbeing", ignoreCase = true) == true
        }

        return when {
            healthCount >= productivityCount && healthCount >= wellbeingCount -> "health and fitness"
            productivityCount >= healthCount && productivityCount >= wellbeingCount -> "productivity and work"
            else -> "wellbeing and mindfulness"
        }
    }

    /**
     * Get the most consistent habit
     */
    private fun getMostConsistentHabit(habits: List<Habit>): String {
        if (habits.isEmpty()) return "none"

        val mostConsistent = habits.maxByOrNull { it.streak }
        return mostConsistent?.name ?: "none"
    }

    /**
     * Get a recommendation based on the user's habits
     */
    private fun getRecommendation(habits: List<Habit>): String {
        return when {
            habits.isEmpty() -> "Start with one simple daily habit to build momentum."
            habits.size < 3 -> "Consider adding one more habit that complements your current ones."
            habits.all { it.frequency == habits.first().frequency } ->
                "Try varying your habit frequencies to include a mix of daily, weekly, and monthly habits."
            habits.none { it.name.contains("morning", ignoreCase = true) } ->
                "Consider adding a morning habit to start your day with a win."
            else -> "Focus on maintaining consistency with your current habits before adding new ones."
        }
    }
}

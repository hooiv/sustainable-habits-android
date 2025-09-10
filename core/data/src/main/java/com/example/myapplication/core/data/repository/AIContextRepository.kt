package com.example.myapplication.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.core.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing AI context data (mood, location, time patterns, personalization)
 */
@Singleton
class AIContextRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_assistant_settings")
    
    // Preference keys
    private object PreferenceKeys {
        val MOOD_ENTRIES = stringPreferencesKey("mood_entries")
        val LOCATION_CONTEXTS = stringPreferencesKey("location_contexts")
        val TIME_PATTERNS = stringPreferencesKey("time_patterns")
        
        // Personalization settings
        val USE_STREAMING = booleanPreferencesKey("use_streaming")
        val USE_VOICE = booleanPreferencesKey("use_voice")
        val VOICE_SPEED = floatPreferencesKey("voice_speed")
        val VOICE_PITCH = floatPreferencesKey("voice_pitch")
        val CONTINUOUS_LISTENING = booleanPreferencesKey("continuous_listening")
        val USE_WAKE_WORD = booleanPreferencesKey("use_wake_word")
        val CUSTOM_WAKE_WORDS = stringPreferencesKey("custom_wake_words")
        val INCLUDE_MOOD_DATA = booleanPreferencesKey("include_mood_data")
        val INCLUDE_LOCATION_DATA = booleanPreferencesKey("include_location_data")
        val INCLUDE_TIME_PATTERNS = booleanPreferencesKey("include_time_patterns")
        val SHOW_ANIMATIONS = booleanPreferencesKey("show_animations")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SAVE_CONVERSATION_HISTORY = booleanPreferencesKey("save_conversation_history")
        val SHARE_HABIT_DATA = booleanPreferencesKey("share_habit_data")
    }
    
    /**
     * Save a mood entry
     */
    suspend fun saveMoodEntry(mood: MoodEntry) {
        val moodEntries = getMoodEntries().toMutableList()
        moodEntries.add(mood)
        
        // Save to shared preferences
        val json = gson.toJson(moodEntries)
        sharedPreferences.edit().putString(PreferenceKeys.MOOD_ENTRIES.name, json).apply()
    }
    
    /**
     * Get all mood entries
     */
    suspend fun getMoodEntries(): List<MoodEntry> {
        val json = sharedPreferences.getString(PreferenceKeys.MOOD_ENTRIES.name, null) ?: return emptyList()
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    /**
     * Get mood entries as a flow
     */
    fun getMoodEntriesFlow(): Flow<List<MoodEntry>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[PreferenceKeys.MOOD_ENTRIES] ?: return@map emptyList<MoodEntry>()
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }
    
    /**
     * Save a location context
     */
    suspend fun saveLocationContext(location: LocationContext) {
        val locations = getLocationContexts().toMutableList()
        locations.add(location)
        
        // Save to shared preferences
        val json = gson.toJson(locations)
        sharedPreferences.edit().putString(PreferenceKeys.LOCATION_CONTEXTS.name, json).apply()
    }
    
    /**
     * Get all location contexts
     */
    suspend fun getLocationContexts(): List<LocationContext> {
        val json = sharedPreferences.getString(PreferenceKeys.LOCATION_CONTEXTS.name, null) ?: return emptyList()
        val type = object : TypeToken<List<LocationContext>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    /**
     * Get location contexts as a flow
     */
    fun getLocationContextsFlow(): Flow<List<LocationContext>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[PreferenceKeys.LOCATION_CONTEXTS] ?: return@map emptyList<LocationContext>()
            val type = object : TypeToken<List<LocationContext>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }
    
    /**
     * Save a time pattern
     */
    suspend fun saveTimePattern(timePattern: TimePattern) {
        val patterns = getTimePatterns().toMutableList()
        
        // Replace existing pattern for the same habit if it exists
        val existingIndex = patterns.indexOfFirst { it.habitId == timePattern.habitId }
        if (existingIndex >= 0) {
            patterns[existingIndex] = timePattern
        } else {
            patterns.add(timePattern)
        }
        
        // Save to shared preferences
        val json = gson.toJson(patterns)
        sharedPreferences.edit().putString(PreferenceKeys.TIME_PATTERNS.name, json).apply()
    }
    
    /**
     * Get all time patterns
     */
    suspend fun getTimePatterns(): List<TimePattern> {
        val json = sharedPreferences.getString(PreferenceKeys.TIME_PATTERNS.name, null) ?: return emptyList()
        val type = object : TypeToken<List<TimePattern>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    /**
     * Get time patterns as a flow
     */
    fun getTimePatternsFlow(): Flow<List<TimePattern>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[PreferenceKeys.TIME_PATTERNS] ?: return@map emptyList<TimePattern>()
            val type = object : TypeToken<List<TimePattern>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }
    
    /**
     * Save personalization settings
     */
    suspend fun savePersonalizationSettings(settings: AIAssistantPersonalization) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.USE_STREAMING] = settings.useStreaming
            preferences[PreferenceKeys.USE_VOICE] = settings.useVoice
            preferences[PreferenceKeys.VOICE_SPEED] = settings.voiceSpeed
            preferences[PreferenceKeys.VOICE_PITCH] = settings.voicePitch
            preferences[PreferenceKeys.CONTINUOUS_LISTENING] = settings.continuousListening
            preferences[PreferenceKeys.USE_WAKE_WORD] = settings.useWakeWord
            preferences[PreferenceKeys.CUSTOM_WAKE_WORDS] = gson.toJson(settings.customWakeWords)
            preferences[PreferenceKeys.INCLUDE_MOOD_DATA] = settings.includeMoodData
            preferences[PreferenceKeys.INCLUDE_LOCATION_DATA] = settings.includeLocationData
            preferences[PreferenceKeys.INCLUDE_TIME_PATTERNS] = settings.includeTimePatterns
            preferences[PreferenceKeys.SHOW_ANIMATIONS] = settings.showAnimations
            preferences[PreferenceKeys.DARK_THEME] = settings.darkTheme
            preferences[PreferenceKeys.SAVE_CONVERSATION_HISTORY] = settings.saveConversationHistory
            preferences[PreferenceKeys.SHARE_HABIT_DATA] = settings.shareHabitData
        }
    }
    
    /**
     * Get personalization settings as a flow
     */
    fun getPersonalizationSettingsFlow(): Flow<AIAssistantPersonalization> {
        return context.dataStore.data.map { preferences ->
            AIAssistantPersonalization(
                useStreaming = preferences[PreferenceKeys.USE_STREAMING] ?: true,
                useVoice = preferences[PreferenceKeys.USE_VOICE] ?: false,
                voiceSpeed = preferences[PreferenceKeys.VOICE_SPEED] ?: 1.0f,
                voicePitch = preferences[PreferenceKeys.VOICE_PITCH] ?: 1.0f,
                continuousListening = preferences[PreferenceKeys.CONTINUOUS_LISTENING] ?: false,
                useWakeWord = preferences[PreferenceKeys.USE_WAKE_WORD] ?: false,
                customWakeWords = preferences[PreferenceKeys.CUSTOM_WAKE_WORDS]?.let {
                    val type = object : TypeToken<List<String>>() {}.type
                    gson.fromJson(it, type) ?: emptyList()
                } ?: emptyList(),
                includeMoodData = preferences[PreferenceKeys.INCLUDE_MOOD_DATA] ?: true,
                includeLocationData = preferences[PreferenceKeys.INCLUDE_LOCATION_DATA] ?: true,
                includeTimePatterns = preferences[PreferenceKeys.INCLUDE_TIME_PATTERNS] ?: true,
                showAnimations = preferences[PreferenceKeys.SHOW_ANIMATIONS] ?: true,
                darkTheme = preferences[PreferenceKeys.DARK_THEME] ?: false,
                saveConversationHistory = preferences[PreferenceKeys.SAVE_CONVERSATION_HISTORY] ?: true,
                shareHabitData = preferences[PreferenceKeys.SHARE_HABIT_DATA] ?: true
            )
        }
    }
    
    /**
     * Get personalization settings
     */
    suspend fun getPersonalizationSettings(): AIAssistantPersonalization {
        // Default settings
        return AIAssistantPersonalization()
    }
    
    /**
     * Create a new mood entry
     */
    fun createMoodEntry(
        userId: String,
        mood: MoodType,
        intensity: Float,
        notes: String = "",
        associatedHabitIds: List<String> = emptyList()
    ): MoodEntry {
        return MoodEntry(
            id = UUID.randomUUID().toString(),
            userId = userId,
            mood = mood,
            intensity = intensity,
            notes = notes,
            timestamp = System.currentTimeMillis(),
            associatedHabitIds = associatedHabitIds
        )
    }
    
    /**
     * Create a new location context
     */
    fun createLocationContext(
        userId: String,
        latitude: Double,
        longitude: Double,
        locationName: String = "",
        locationType: LocationType = LocationType.OTHER,
        associatedHabitIds: List<String> = emptyList()
    ): LocationContext {
        return LocationContext(
            id = UUID.randomUUID().toString(),
            userId = userId,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            locationType = locationType,
            timestamp = System.currentTimeMillis(),
            associatedHabitIds = associatedHabitIds
        )
    }
}

package com.example.myapplication.core.ui.navigation

object NavRoutes {
    const val HABIT_LIST = "habit_list"
    const val ADD_HABIT = "add_habit"
    const val EDIT_HABIT_ROUTE = "edit_habit" // Base route name
    const val EDIT_HABIT_ARG_ID = "habitId" // Argument name
    const val EDIT_HABIT = "$EDIT_HABIT_ROUTE/{$EDIT_HABIT_ARG_ID}" // Full route with argument placeholder
    const val STATS = "stats"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val SIGN_IN = "sign_in" // Added sign_in route
    const val ANIMATION_DEMO = "animation_demo" // Added animation demo route
    const val NEURAL_INTERFACE_ROUTE = "neural_interface" // Base route name
    const val NEURAL_INTERFACE_ARG_ID = "habitId" // Argument name
    const val NEURAL_INTERFACE = "$NEURAL_INTERFACE_ROUTE/{$NEURAL_INTERFACE_ARG_ID}" // Full route with argument placeholder
    const val QUANTUM_VISUALIZATION_ROUTE = "quantum_visualization" // Base route name
    const val QUANTUM_VISUALIZATION_ARG_ID = "habitId" // Argument name (optional)
    const val QUANTUM_VISUALIZATION = "$QUANTUM_VISUALIZATION_ROUTE/{$QUANTUM_VISUALIZATION_ARG_ID}?{$QUANTUM_VISUALIZATION_ARG_ID}" // Full route with optional argument
    const val QUANTUM_VISUALIZATION_GLOBAL = "$QUANTUM_VISUALIZATION_ROUTE" // Global quantum view without a specific habit

    // Biometric integration routes
    const val BIOMETRIC_INTEGRATION_ROUTE = "biometric_integration" // Base route name
    const val BIOMETRIC_INTEGRATION_ARG_ID = "habitId" // Argument name (optional)
    const val BIOMETRIC_INTEGRATION = "$BIOMETRIC_INTEGRATION_ROUTE/{$BIOMETRIC_INTEGRATION_ARG_ID}?{$BIOMETRIC_INTEGRATION_ARG_ID}" // Full route with optional argument
    const val BIOMETRIC_INTEGRATION_GLOBAL = "$BIOMETRIC_INTEGRATION_ROUTE" // Global biometric view without a specific habit

    // Voice integration route
    const val VOICE_INTEGRATION = "voice_integration" // Voice integration route

    // Spatial computing route
    const val SPATIAL_COMPUTING = "spatial_computing" // Spatial computing route

    // Three.js visualization route
    const val THREEJS_VISUALIZATION = "threejs_visualization" // Three.js visualization route

    // Animation routes
    const val ANIMATIONS = "animations" // Main animations hub
    const val ANIMEJS_ANIMATION = "animejs_animation" // Anime.js animation route

    // Advanced features route
    const val ADVANCED_FEATURES = "advanced_features" // Advanced features route

    // Multi-modal learning route
    const val MULTI_MODAL_LEARNING = "multi_modal_learning" // Multi-modal learning route

    // Meta-learning route
    const val META_LEARNING = "meta_learning" // Meta-learning route

    // Neural network route
    const val NEURAL_NETWORK = "neural_network" // Neural network route

    // AI Assistant routes
    const val AI_ASSISTANT = "ai_assistant" // AI Assistant route
    const val AI_ASSISTANT_SETTINGS = "ai_assistant_settings" // AI Assistant settings route

    // New advanced feature routes
    const val GESTURE_CONTROLS = "gesture_controls" // Gesture Controls route
    const val ADVANCED_ANALYTICS = "advanced_analytics" // Advanced Analytics route
    const val PREDICTIVE_ML = "predictive_ml" // Predictive ML route

    // Habit completion routes
    const val HABIT_COMPLETION_ROUTE = "habit_completion" // Base route name
    const val HABIT_COMPLETION_ARG_ID = "habitId" // Argument name
    const val HABIT_COMPLETION_ARG_NAME = "habitName" // Argument name
    const val HABIT_COMPLETION = "$HABIT_COMPLETION_ROUTE/{$HABIT_COMPLETION_ARG_ID}/{$HABIT_COMPLETION_ARG_NAME}" // Full route with argument placeholder

    // AR routes
    const val AR_ROUTE = "ar" // Base route name
    const val AR_ARG_ID = "habitId" // Argument name
    const val AR = "$AR_ROUTE/{$AR_ARG_ID}" // Full route with argument placeholder
    const val AR_GLOBAL = "ar_global" // Global AR view without a specific habit

    // Helper function for parameterized routes
    fun editHabit(habitId: String) = "$EDIT_HABIT_ROUTE/$habitId"
    fun neuralInterface(habitId: String) = "$NEURAL_INTERFACE_ROUTE/$habitId"
    fun habitCompletion(habitId: String, habitName: String) = "$HABIT_COMPLETION_ROUTE/$habitId/$habitName"
    fun ar(habitId: String) = "$AR_ROUTE/$habitId"
    fun quantumVisualization(habitId: String? = null) = if (habitId != null) "$QUANTUM_VISUALIZATION_ROUTE/$habitId" else QUANTUM_VISUALIZATION_GLOBAL
    fun biometricIntegration(habitId: String? = null) = if (habitId != null) "$BIOMETRIC_INTEGRATION_ROUTE/$habitId" else BIOMETRIC_INTEGRATION_GLOBAL
}

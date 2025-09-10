package com.example.myapplication.core.data.model

import java.util.UUID

/**
 * Data class for reinforcement learning actions
 */
data class ReinforcementAction(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val actionType: ActionType,
    val parameters: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val actionId: Int = 0
)

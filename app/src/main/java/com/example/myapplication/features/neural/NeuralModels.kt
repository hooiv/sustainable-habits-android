package com.example.myapplication.features.neural

import androidx.compose.ui.geometry.Offset
import com.example.myapplication.data.model.NeuralNodeType
import java.util.UUID

/**
 * Data class representing a neural node
 */
data class NeuralNode(
    val id: String = UUID.randomUUID().toString(),
    val type: NeuralNodeType,
    var position: Offset,
    var connections: MutableList<String> = mutableListOf(),
    var activationLevel: Float = 0f,
    var label: String? = null,
    var metadata: Map<String, Any> = emptyMap()
)

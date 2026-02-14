package com.example.myapplication.features.spatial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.SpatialObject
import com.example.myapplication.core.data.model.SpatialObjectType
import com.example.myapplication.core.data.model.Offset3D
import com.example.myapplication.core.data.model.Rotation3D
import com.example.myapplication.core.data.repository.HabitRepository
import com.example.myapplication.core.data.repository.SpatialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.*

/**
 * ViewModel for the Spatial Computing screen
 */
@HiltViewModel
class SpatialComputingViewModel @Inject constructor(
    private val spatialRepository: SpatialRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    // Current habit ID
    private val _currentHabitId = MutableStateFlow<String?>(null)
    val currentHabitId: StateFlow<String?> = _currentHabitId.asStateFlow()

    // Spatial objects
    private val _spatialObjects = MutableStateFlow<List<SpatialObject>>(emptyList())
    val spatialObjects: StateFlow<List<SpatialObject>> = _spatialObjects.asStateFlow()

    // Selected object
    private val _selectedObject = MutableStateFlow<SpatialObject?>(null)
    val selectedObject: StateFlow<SpatialObject?> = _selectedObject.asStateFlow()

    // Selected object type for placement
    private val _selectedObjectType = MutableStateFlow(SpatialObjectType.HABIT_SPHERE)
    val selectedObjectType: StateFlow<SpatialObjectType> = _selectedObjectType.asStateFlow()

    // Placement mode
    private val _isPlacementMode = MutableStateFlow(false)
    val isPlacementMode: StateFlow<Boolean> = _isPlacementMode.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadSpatialObjects()
    }

    /**
     * Set current habit ID
     */
    fun setCurrentHabitId(habitId: String) {
        _currentHabitId.value = habitId
    }

    /**
     * Load spatial objects
     */
    fun loadSpatialObjects() {
        viewModelScope.launch {
            try {
                _spatialObjects.value = spatialRepository.getSpatialObjects()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load spatial objects: ${e.message}"
            }
        }
    }

    /**
     * Select an object
     */
    fun selectObject(objectId: String) {
        _selectedObject.value = _spatialObjects.value.find { it.id == objectId }
    }

    /**
     * Move an object
     */
    fun moveObject(objectId: String, newPosition: Offset3D) {
        viewModelScope.launch {
            try {
                // Find the object
                val objectToMove = _spatialObjects.value.find { it.id == objectId } ?: return@launch

                // Create updated object
                val updatedObject = objectToMove.copy(position = newPosition)

                // Update in repository
                spatialRepository.updateSpatialObject(updatedObject)

                // Update in view model
                _spatialObjects.value = _spatialObjects.value.map {
                    if (it.id == objectId) updatedObject else it
                }

                // Update selected object if needed
                if (_selectedObject.value?.id == objectId) {
                    _selectedObject.value = updatedObject
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to move object: ${e.message}"
            }
        }
    }

    /**
     * Delete an object
     */
    fun deleteObject(objectId: String) {
        viewModelScope.launch {
            try {
                // Delete from repository
                spatialRepository.deleteSpatialObject(objectId)

                // Remove from view model
                _spatialObjects.value = _spatialObjects.value.filter { it.id != objectId }

                // Clear selected object if needed
                if (_selectedObject.value?.id == objectId) {
                    _selectedObject.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete object: ${e.message}"
            }
        }
    }

    /**
     * Toggle placement mode
     */
    fun togglePlacementMode() {
        _isPlacementMode.value = !_isPlacementMode.value

        // Clear selected object when entering placement mode
        if (_isPlacementMode.value) {
            _selectedObject.value = null
        }
    }

    /**
     * Set selected object type
     */
    fun setSelectedObjectType(type: SpatialObjectType) {
        _selectedObjectType.value = type
    }

    /**
     * Create a new object
     */
    fun createObject(position: Offset3D) {
        viewModelScope.launch {
            try {
                // Get habits to associate with the object
                val habits = habitRepository.getAllHabits().first()
                val habitToAssociate = _currentHabitId.value?.let { habitId ->
                    habits.find { habit -> habit.id == habitId }
                } ?: habits.randomOrNull()

                // Create new object
                val newObject = SpatialObject(
                    id = UUID.randomUUID().toString(),
                    type = _selectedObjectType.value,
                    position = position,
                    rotation = Rotation3D(0f, 0f, 0f),
                    scale = 1.0f,
                    color = getColorForObjectType(_selectedObjectType.value).value.toLong(),
                    label = habitToAssociate?.name ?: "New Object",
                    relatedHabitId = habitToAssociate?.id
                )

                // Add to repository
                spatialRepository.addSpatialObject(newObject)

                // Add to view model
                _spatialObjects.value = _spatialObjects.value + newObject

                // Exit placement mode
                _isPlacementMode.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create object: ${e.message}"
            }
        }
    }

    /**
     * Reset view
     */
    fun resetView() {
        _selectedObject.value = null
        _isPlacementMode.value = false
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Get color for object type
     */
    private fun getColorForObjectType(type: SpatialObjectType): androidx.compose.ui.graphics.Color {
        return when (type) {
            SpatialObjectType.HABIT_SPHERE -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
            SpatialObjectType.STREAK_TOWER -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
            SpatialObjectType.GOAL_PYRAMID -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            SpatialObjectType.ACHIEVEMENT_STAR -> androidx.compose.ui.graphics.Color(0xFFFFEB3B) // Yellow
            SpatialObjectType.CATEGORY_CUBE -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
            SpatialObjectType.REMINDER_CLOCK -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
        }
    }
}

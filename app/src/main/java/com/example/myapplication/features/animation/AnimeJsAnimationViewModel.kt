package com.example.myapplication.features.animation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Anime.js animations
 */
@HiltViewModel
class AnimeJsAnimationViewModel @Inject constructor(
    val animeJsIntegration: AnimeJsIntegration
) : ViewModel() {
    
    // State
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _currentAnimation = MutableStateFlow(AnimationType.DEFAULT)
    val currentAnimation: StateFlow<AnimationType> = _currentAnimation.asStateFlow()
    
    init {
        // Observe integration state
        viewModelScope.launch {
            animeJsIntegration.isReady.collectLatest { ready ->
                _isReady.value = ready
            }
        }
        
        viewModelScope.launch {
            animeJsIntegration.error.collectLatest { error ->
                _error.value = error
            }
        }
    }
    
    /**
     * Called when WebView is ready
     */
    fun onWebViewReady() {
        // Default animation will be executed automatically
    }
    
    /**
     * Set animation type
     */
    fun setAnimationType(type: AnimationType) {
        _currentAnimation.value = type
        animeJsIntegration.executeAnimation(type)
    }
    
    /**
     * Animate habits
     */
    fun animateHabits() {
        animeJsIntegration.animateHabits()
    }
    
    /**
     * Animate progress
     */
    fun animateProgress() {
        animeJsIntegration.animateProgress()
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
}

package com.example.myapplication.features.threejs

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Three.js visualizations
 */
@HiltViewModel
class ThreeJsVisualizationViewModel @Inject constructor(
    private val threeJsIntegration: ThreeJsIntegration,
    private val habitRepository: HabitRepository
) : ViewModel() {
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Scene ready state
    private val _isSceneReady = MutableStateFlow(false)
    val isSceneReady: StateFlow<Boolean> = _isSceneReady.asStateFlow()
    
    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Visualization type
    private val _visualizationType = MutableStateFlow(ThreeJsVisualizationType.HABITS)
    val visualizationType: StateFlow<ThreeJsVisualizationType> = _visualizationType.asStateFlow()
    
    // WebView instance
    private var webView: WebView? = null
    
    // Habits data
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    
    // Completions data
    private val _completions = MutableStateFlow<Map<String, List<HabitCompletion>>>(emptyMap())
    
    init {
        // Observe Three.js integration state
        viewModelScope.launch {
            threeJsIntegration.isSceneReady.collect { isReady ->
                _isSceneReady.value = isReady
                
                if (isReady) {
                    visualizeData()
                }
            }
        }
        
        viewModelScope.launch {
            threeJsIntegration.error.collect { error ->
                if (error != null) {
                    _errorMessage.value = error
                }
            }
        }
    }
    
    /**
     * Initialize WebView
     */
    fun initializeWebView(webView: WebView) {
        this.webView = webView
        threeJsIntegration.initialize(webView)
    }
    
    /**
     * Load habits data
     */
    fun loadHabits() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Load habits
                val habits = habitRepository.getAllHabits().first()
                _habits.value = habits
                
                // Load completions for each habit
                val completionsMap = mutableMapOf<String, List<HabitCompletion>>()
                habits.forEach { habit ->
                    val completions = habitRepository.getHabitCompletions(habit.id).first()
                    completionsMap[habit.id] = completions
                }
                _completions.value = completionsMap
                
                // Visualize data
                visualizeData()
            } catch (e: Exception) {
                _errorMessage.value = "Error loading habits: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh visualization
     */
    fun refreshVisualization() {
        loadHabits()
    }
    
    /**
     * Toggle visualization type
     */
    fun toggleVisualizationType() {
        _visualizationType.value = when (_visualizationType.value) {
            ThreeJsVisualizationType.HABITS -> ThreeJsVisualizationType.COMPLETIONS
            ThreeJsVisualizationType.COMPLETIONS -> ThreeJsVisualizationType.STREAKS
            ThreeJsVisualizationType.STREAKS -> ThreeJsVisualizationType.HABITS
        }
        
        visualizeData()
    }
    
    /**
     * Visualize data based on current visualization type
     */
    private fun visualizeData() {
        if (!_isSceneReady.value) return
        
        when (_visualizationType.value) {
            ThreeJsVisualizationType.HABITS -> visualizeHabits()
            ThreeJsVisualizationType.COMPLETIONS -> visualizeCompletions()
            ThreeJsVisualizationType.STREAKS -> visualizeStreaks()
        }
    }
    
    /**
     * Visualize habits
     */
    private fun visualizeHabits() {
        viewModelScope.launch {
            try {
                threeJsIntegration.visualizeHabits()
            } catch (e: Exception) {
                _errorMessage.value = "Error visualizing habits: ${e.message}"
            }
        }
    }
    
    /**
     * Visualize completions
     */
    private fun visualizeCompletions() {
        viewModelScope.launch {
            try {
                // Execute JavaScript to visualize completions
                webView?.evaluateJavascript(
                    """
                    (function() {
                        // Clear existing objects
                        clearScene();
                        
                        // Create visualization based on completions
                        const habits = ${com.google.gson.Gson().toJson(_habits.value)};
                        const completions = ${com.google.gson.Gson().toJson(_completions.value)};
                        
                        // Calculate positions in a circle
                        const radius = 10;
                        const angleStep = (2 * Math.PI) / habits.length;
                        
                        // Create objects for each habit
                        habits.forEach((habit, index) => {
                            const angle = index * angleStep;
                            const x = radius * Math.cos(angle);
                            const z = radius * Math.sin(angle);
                            
                            // Get completions for this habit
                            const habitCompletions = completions[habit.id] || [];
                            
                            // Scale based on number of completions
                            const scale = Math.max(0.5, Math.min(3, habitCompletions.length * 0.2 + 0.5));
                            
                            // Convert habit color to hex
                            const colorMap = {
                                RED: 0xff5252,
                                GREEN: 0x4caf50,
                                BLUE: 0x2196f3,
                                YELLOW: 0xffeb3b,
                                PURPLE: 0x9c27b0,
                                ORANGE: 0xff9800,
                                TEAL: 0x009688,
                                PINK: 0xe91e63
                            };
                            
                            const color = colorMap[habit.color] || 0xffffff;
                            
                            // Create sphere with size based on completions
                            const objectId = createSphere(habit.id, x, 0, z, scale, color);
                            
                            // Create text label with completion count
                            createText(
                                habit.id + "_label", 
                                habitCompletions.length.toString(), 
                                x, 2, z, 
                                1, 
                                color
                            );
                            
                            // Animate based on recent completions
                            if (habitCompletions.length > 0) {
                                // Sort by timestamp (newest first)
                                const sortedCompletions = [...habitCompletions].sort(
                                    (a, b) => b.timestamp - a.timestamp
                                );
                                
                                // Check if there's a recent completion (last 24 hours)
                                const mostRecent = sortedCompletions[0];
                                const now = Date.now();
                                const oneDayMs = 24 * 60 * 60 * 1000;
                                
                                if (mostRecent && (now - mostRecent.timestamp) < oneDayMs) {
                                    animateObject(habit.id, "rotation", { y: Math.PI * 2 }, 2);
                                }
                            }
                        });
                    })();
                    """,
                    null
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error visualizing completions: ${e.message}"
            }
        }
    }
    
    /**
     * Visualize streaks
     */
    private fun visualizeStreaks() {
        viewModelScope.launch {
            try {
                // Execute JavaScript to visualize streaks
                webView?.evaluateJavascript(
                    """
                    (function() {
                        // Clear existing objects
                        clearScene();
                        
                        // Create visualization based on streaks
                        const habits = ${com.google.gson.Gson().toJson(_habits.value)};
                        
                        // Calculate positions in a circle
                        const radius = 10;
                        const angleStep = (2 * Math.PI) / habits.length;
                        
                        // Create objects for each habit
                        habits.forEach((habit, index) => {
                            const angle = index * angleStep;
                            const x = radius * Math.cos(angle);
                            const z = radius * Math.sin(angle);
                            
                            // Scale based on streak
                            const scale = Math.max(0.5, Math.min(3, habit.streak * 0.3 + 0.5));
                            
                            // Convert habit color to hex
                            const colorMap = {
                                RED: 0xff5252,
                                GREEN: 0x4caf50,
                                BLUE: 0x2196f3,
                                YELLOW: 0xffeb3b,
                                PURPLE: 0x9c27b0,
                                ORANGE: 0xff9800,
                                TEAL: 0x009688,
                                PINK: 0xe91e63
                            };
                            
                            const color = colorMap[habit.color] || 0xffffff;
                            
                            // Create cylinder with height based on streak
                            const objectId = createCylinder(
                                habit.id, 
                                x, 
                                0, 
                                z, 
                                0.8, 
                                Math.max(0.5, habit.streak), 
                                color
                            );
                            
                            // Create text label with streak count
                            createText(
                                habit.id + "_label", 
                                habit.streak.toString(), 
                                x, 2, z, 
                                1, 
                                color
                            );
                            
                            // Animate based on streak
                            if (habit.streak > 0) {
                                animateObject(habit.id, "rotation", { y: Math.PI * 2 }, 2);
                            }
                        });
                    })();
                    """,
                    null
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error visualizing streaks: ${e.message}"
            }
        }
    }
    
    /**
     * Zoom in
     */
    fun zoomIn() {
        webView?.evaluateJavascript(
            """
            (function() {
                camera.position.z -= 2;
                camera.updateProjectionMatrix();
            })();
            """,
            null
        )
    }
    
    /**
     * Zoom out
     */
    fun zoomOut() {
        webView?.evaluateJavascript(
            """
            (function() {
                camera.position.z += 2;
                camera.updateProjectionMatrix();
            })();
            """,
            null
        )
    }
    
    /**
     * Rotate scene
     */
    fun rotate() {
        webView?.evaluateJavascript(
            """
            (function() {
                gsap.to(scene.rotation, {
                    y: scene.rotation.y + Math.PI * 2,
                    duration: 2,
                    ease: "power2.inOut"
                });
            })();
            """,
            null
        )
    }
    
    /**
     * Animate objects
     */
    fun animate() {
        webView?.evaluateJavascript(
            """
            (function() {
                // Animate all objects
                for (const id in objects) {
                    if (objects[id].type === "Mesh") {
                        gsap.to(objects[id].position, {
                            y: objects[id].position.y + 1,
                            duration: 1,
                            ease: "power2.out",
                            yoyo: true,
                            repeat: 1
                        });
                    }
                }
            })();
            """,
            null
        )
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        threeJsIntegration.cleanup()
    }
}

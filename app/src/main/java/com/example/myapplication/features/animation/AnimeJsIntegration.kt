package com.example.myapplication.features.animation

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.repository.HabitRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Integration with Anime.js for animations
 */
@Singleton
class AnimeJsIntegration @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitRepository: HabitRepository,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "AnimeJsIntegration"
    }

    // WebView
    private var webView: WebView? = null

    // Coroutine scope
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // State
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Initialize WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun initialize(webView: WebView) {
        this.webView = webView

        // Configure WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
        }

        // Add JavaScript interface
        webView.addJavascriptInterface(AnimeJsInterface(), "AndroidAnimeBridge")

        // Set WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "WebView page loaded")
            }
        }

        // Load HTML
        loadHtml()
    }

    /**
     * Load HTML template
     */
    private fun loadHtml() {
        try {
            // Load HTML from assets
            val inputStream = context.assets.open("html/animation_template.html")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val html = String(buffer, Charsets.UTF_8)

            webView?.loadDataWithBaseURL(
                "https://animejs.com/",
                html,
                "text/html",
                "UTF-8",
                null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading HTML template", e)
            _error.value = "Error loading HTML template: ${e.message}"
        }
    }

    /**
     * Execute animation
     */
    fun executeAnimation(type: AnimationType) {
        webView?.evaluateJavascript(
            "executeAnimation('${type.name.lowercase()}');",
            null
        )
    }

    /**
     * Animate habits
     */
    fun animateHabits() {
        coroutineScope.launch {
            try {
                // Get habits
                val habits = habitRepository.getAllHabits().first()

                // Convert to JSON
                val habitsJson = gson.toJson(habits)

                // Send to WebView
                withContext(Dispatchers.Main) {
                    webView?.evaluateJavascript(
                        """
                        (function() {
                            if (window.AnimeBridge) {
                                const habits = $habitsJson;
                                const container = document.getElementById('container');

                                // Clear container
                                container.innerHTML = '';

                                // Create habit grid
                                const habitGrid = document.createElement('div');
                                habitGrid.className = 'habit-grid';

                                // Create habit cards
                                habits.forEach(habit => {
                                    const card = document.createElement('div');
                                    card.className = 'habit-card';
                                    card.textContent = habit.name;

                                    // Set color based on habit color
                                    const colorMap = {
                                        RED: '#F44336',
                                        GREEN: '#4CAF50',
                                        BLUE: '#2196F3',
                                        YELLOW: '#FFEB3B',
                                        PURPLE: '#9C27B0',
                                        ORANGE: '#FF9800',
                                        TEAL: '#009688',
                                        PINK: '#E91E63'
                                    };

                                    card.style.backgroundColor = colorMap[habit.color] || '#6200EE';
                                    habitGrid.appendChild(card);
                                });

                                container.appendChild(habitGrid);

                                // Animate habit cards
                                anime({
                                    targets: '.habit-card',
                                    translateY: [100, 0],
                                    opacity: [0, 1],
                                    delay: anime.stagger(100),
                                    easing: 'easeOutElastic(1, .6)',
                                    complete: function() {
                                        // Continuous subtle animation
                                        anime({
                                            targets: '.habit-card',
                                            translateY: function() { return anime.random(-5, 5); },
                                            duration: 2000,
                                            loop: true,
                                            direction: 'alternate',
                                            easing: 'easeInOutQuad',
                                            delay: anime.stagger(100)
                                        });
                                    }
                                });
                            }
                        })();
                        """,
                        null
                    )
                }

                Log.d(TAG, "Animating ${habits.size} habits")
            } catch (e: Exception) {
                _error.value = "Error animating habits: ${e.message}"
                Log.e(TAG, "Error animating habits", e)
            }
        }
    }

    /**
     * Animate progress
     */
    fun animateProgress() {
        coroutineScope.launch {
            try {
                // Get habits
                val habits = habitRepository.getAllHabits().first()

                // Convert to JSON
                val habitsJson = gson.toJson(habits)

                // Send to WebView
                withContext(Dispatchers.Main) {
                    webView?.evaluateJavascript(
                        """
                        (function() {
                            if (window.AnimeBridge) {
                                const habits = $habitsJson;
                                const container = document.getElementById('container');

                                // Clear container
                                container.innerHTML = '';

                                // Create progress circles
                                habits.forEach(habit => {
                                    const circle = document.createElement('div');
                                    circle.className = 'progress-circle';

                                    const fill = document.createElement('div');
                                    fill.className = 'progress-circle-fill';

                                    // Calculate progress percentage
                                    const progressValue = Math.min(100, habit.streak * 10);
                                    fill.textContent = progressValue + '%';
                                    fill.style.transform = 'scale(0)';

                                    // Set color based on habit color
                                    const colorMap = {
                                        RED: '#F44336',
                                        GREEN: '#4CAF50',
                                        BLUE: '#2196F3',
                                        YELLOW: '#FFEB3B',
                                        PURPLE: '#9C27B0',
                                        ORANGE: '#FF9800',
                                        TEAL: '#009688',
                                        PINK: '#E91E63'
                                    };

                                    fill.style.backgroundColor = colorMap[habit.color] || '#6200EE';

                                    circle.appendChild(fill);
                                    container.appendChild(circle);
                                });

                                // Animate progress circles
                                anime({
                                    targets: '.progress-circle-fill',
                                    scale: [0, 1],
                                    delay: anime.stagger(200),
                                    easing: 'easeOutElastic(1, .6)',
                                    duration: 1500
                                });
                            }
                        })();
                        """,
                        null
                    )
                }

                Log.d(TAG, "Animating progress for ${habits.size} habits")
            } catch (e: Exception) {
                _error.value = "Error animating progress: ${e.message}"
                Log.e(TAG, "Error animating progress", e)
            }
        }
    }

    /**
     * JavaScript interface for communication with Anime.js
     */
    inner class AnimeJsInterface {

        @JavascriptInterface
        fun onBridgeReady() {
            coroutineScope.launch {
                _isReady.value = true
            }
        }

        @JavascriptInterface
        fun onError(message: String) {
            coroutineScope.launch {
                _error.value = message
            }
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        webView = null
    }
}

/**
 * Animation types
 */
enum class AnimationType {
    DEFAULT,
    STAGGER,
    TEXT,
    HABITS,
    PROGRESS,
    PULSE,
    ROTATE,
    FADE
}

/**
 * Composable for Anime.js WebView
 */
@Composable
fun AnimeJsWebView(
    integration: AnimeJsIntegration,
    onReady: () -> Unit = {}
) {
    val context = LocalContext.current

    // Create WebView
    val webView = remember {
        WebView(context).apply {
            integration.initialize(this)
        }
    }

    // Dispose effect
    DisposableEffect(Unit) {
        onDispose {
            integration.cleanup()
        }
    }

    // Android view
    AndroidView(
        factory = { webView },
        update = { /* No updates needed */ }
    )
}

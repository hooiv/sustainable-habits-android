package com.example.myapplication.ui.animation

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Interface for Anime.js bridge callbacks
 */
interface AnimeBridgeCallback {
    fun onBridgeReady()
    fun onAnimationComplete(animationId: String)
}

/**
 * Wrapper for Anime.js animations
 */
class AnimeJSWrapper(private val context: Context) {
    private var webView: WebView? = null
    private var bridgeCallback: AnimeBridgeCallback? = null
    
    /**
     * Set bridge callback
     */
    fun setBridgeCallback(callback: AnimeBridgeCallback) {
        bridgeCallback = callback
    }
    
    /**
     * Initialize WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun initWebView(webView: WebView) {
        this.webView = webView
        
        // Configure WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        
        // Add JavaScript interface
        webView.addJavascriptInterface(AnimeJSInterface(), "AndroidAnimeBridge")
        
        // Set WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Page loaded
            }
        }
        
        // Load animation template
        webView.loadUrl("file:///android_asset/html/animation_template.html")
    }
    
    /**
     * Execute animation
     */
    fun executeAnimation(animationType: String) {
        webView?.evaluateJavascript(
            "window.executeAnimation('$animationType');",
            null
        )
    }
    
    /**
     * Create animation
     */
    fun createAnimation(options: String): String? {
        val script = "AnimeBridge.createAnimation($options);"
        var result: String? = null
        
        webView?.evaluateJavascript(script) { value ->
            result = value?.trim('"')
        }
        
        return result
    }
    
    /**
     * Play animation
     */
    fun playAnimation(animationId: String) {
        webView?.evaluateJavascript(
            "AnimeBridge.playAnimation('$animationId');",
            null
        )
    }
    
    /**
     * Pause animation
     */
    fun pauseAnimation(animationId: String) {
        webView?.evaluateJavascript(
            "AnimeBridge.pauseAnimation('$animationId');",
            null
        )
    }
    
    /**
     * Restart animation
     */
    fun restartAnimation(animationId: String) {
        webView?.evaluateJavascript(
            "AnimeBridge.restartAnimation('$animationId');",
            null
        )
    }
    
    /**
     * Create timeline
     */
    fun createTimeline(options: String): String? {
        val script = "AnimeBridge.createTimeline($options);"
        var result: String? = null
        
        webView?.evaluateJavascript(script) { value ->
            result = value?.trim('"')
        }
        
        return result
    }
    
    /**
     * Add to timeline
     */
    fun addToTimeline(timelineId: String, animationOptions: String) {
        webView?.evaluateJavascript(
            "AnimeBridge.addToTimeline('$timelineId', $animationOptions);",
            null
        )
    }
    
    /**
     * Play timeline
     */
    fun playTimeline(timelineId: String) {
        webView?.evaluateJavascript(
            "AnimeBridge.playTimeline('$timelineId');",
            null
        )
    }
    
    /**
     * JavaScript interface for Anime.js bridge
     */
    inner class AnimeJSInterface {
        @JavascriptInterface
        fun onBridgeReady() {
            bridgeCallback?.onBridgeReady()
        }
        
        @JavascriptInterface
        fun onAnimationComplete(animationId: String) {
            bridgeCallback?.onAnimationComplete(animationId)
        }
    }
}

/**
 * Composable for Anime.js animations
 */
@Composable
fun AnimeJSAnimation(
    animationType: String = "default",
    modifier: Modifier = Modifier,
    onBridgeReady: () -> Unit = {}
) {
    val context = LocalContext.current
    val animeJSWrapper = remember { AnimeJSWrapper(context) }
    
    // Set bridge callback
    DisposableEffect(animeJSWrapper) {
        animeJSWrapper.setBridgeCallback(object : AnimeBridgeCallback {
            override fun onBridgeReady() {
                onBridgeReady()
                animeJSWrapper.executeAnimation(animationType)
            }
            
            override fun onAnimationComplete(animationId: String) {
                // Handle animation complete
            }
        })
        
        onDispose {
            // Clean up
        }
    }
    
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    animeJSWrapper.initWebView(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

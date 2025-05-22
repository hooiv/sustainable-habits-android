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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

/**
 * Interface for Three.js bridge callbacks
 */
interface ThreeBridgeCallback {
    fun onBridgeReady()
    fun onObjectClick(sceneId: String, objectId: String)
}

/**
 * Wrapper for Three.js visualizations
 */
class ThreeJSWrapper(private val context: Context) {
    private var webView: WebView? = null
    private var bridgeCallback: ThreeBridgeCallback? = null

    /**
     * Set bridge callback
     */
    fun setBridgeCallback(callback: ThreeBridgeCallback) {
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
        webView.addJavascriptInterface(ThreeJSInterface(), "AndroidThreeBridge")

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
     * Initialize scene
     */
    fun initScene(containerId: String, options: SceneOptions): String? {
        val optionsJson = JSONObject().apply {
            put("backgroundColor", options.backgroundColor)
            put("ambientLight", options.ambientLight)
            put("directionalLight", options.directionalLight)
            put("shadows", options.shadows)
            put("controls", options.controls)
            put("enableZoom", options.enableZoom)
            put("enablePan", options.enablePan)
            put("enableRotate", options.enableRotate)
            put("autoRotate", options.autoRotate)
            put("autoRotateSpeed", options.autoRotateSpeed)
            put("fov", options.fov)
            put("near", options.near)
            put("far", options.far)
            put("cameraDistance", options.cameraDistance)
        }

        val script = "ThreeBridge.initScene('$containerId', $optionsJson);"
        var result: String? = null

        webView?.evaluateJavascript(script) { value ->
            result = value?.trim('"')
        }

        return result
    }

    /**
     * Add cube to scene
     */
    fun addCube(sceneId: String, options: CubeOptions): String? {
        val optionsJson = JSONObject().apply {
            put("width", options.width)
            put("height", options.height)
            put("depth", options.depth)
            put("color", options.color)
            put("x", options.x)
            put("y", options.y)
            put("z", options.z)
            put("castShadow", options.castShadow)
            put("receiveShadow", options.receiveShadow)
            put("metalness", options.metalness)
            put("roughness", options.roughness)
        }

        val script = "ThreeBridge.addCube('$sceneId', $optionsJson);"
        var result: String? = null

        webView?.evaluateJavascript(script) { value ->
            result = value?.trim('"')
        }

        return result
    }

    /**
     * Add sphere to scene
     */
    fun addSphere(sceneId: String, options: SphereOptions): String? {
        val optionsJson = JSONObject().apply {
            put("radius", options.radius)
            put("color", options.color)
            put("x", options.x)
            put("y", options.y)
            put("z", options.z)
            put("castShadow", options.castShadow)
            put("receiveShadow", options.receiveShadow)
            put("metalness", options.metalness)
            put("roughness", options.roughness)
            put("opacity", options.opacity)
        }

        val script = "ThreeBridge.addSphere('$sceneId', $optionsJson);"
        var result: String? = null

        webView?.evaluateJavascript(script) { value ->
            result = value?.trim('"')
        }

        return result
    }

    /**
     * Add cylinder to scene
     */
    fun addCylinder(sceneId: String, options: CylinderOptions): String? {
        val optionsJson = JSONObject().apply {
            put("radius", options.radius)
            put("height", options.height)
            put("color", options.color)
            put("x", options.x)
            put("y", options.y)
            put("z", options.z)
            put("castShadow", options.castShadow)
            put("receiveShadow", options.receiveShadow)
            put("metalness", options.metalness)
            put("roughness", options.roughness)
        }

        val script = "ThreeBridge.addCylinder('$sceneId', $optionsJson);"
        var result: String? = null

        webView?.evaluateJavascript(script) { value ->
            result = value?.trim('"')
        }

        return result
    }

    /**
     * Add text to scene
     */
    fun addText(sceneId: String, options: TextOptions): String? {
        val optionsJson = JSONObject().apply {
            put("text", options.text)
            put("size", options.size)
            put("color", options.color)
            put("x", options.x)
            put("y", options.y)
            put("z", options.z)
            put("rotationX", options.rotationX)
            put("rotationY", options.rotationY)
            put("rotationZ", options.rotationZ)
        }

        val script = "ThreeBridge.addText('$sceneId', $optionsJson);"
        var result: String? = null

        webView?.evaluateJavascript(script) { value ->
            result = value?.trim('"')
        }

        return result
    }

    /**
     * Animate object
     */
    fun animateObject(objectId: String, options: AnimationOptions): String? {
        val optionsJson = JSONObject().apply {
            // Position animation
            if (options.position != null) {
                val positionJson = JSONObject().apply {
                    options.position.x?.let { put("x", it) }
                    options.position.y?.let { put("y", it) }
                    options.position.z?.let { put("z", it) }
                }
                put("position", positionJson)
            }

            // Rotation animation
            if (options.rotation != null) {
                val rotationJson = JSONObject().apply {
                    options.rotation.x?.let { put("x", it) }
                    options.rotation.y?.let { put("y", it) }
                    options.rotation.z?.let { put("z", it) }
                }
                put("rotation", rotationJson)
            }

            // Scale animation
            if (options.scale != null) {
                val scaleJson = JSONObject().apply {
                    options.scale.x?.let { put("x", it) }
                    options.scale.y?.let { put("y", it) }
                    options.scale.z?.let { put("z", it) }
                }
                put("scale", scaleJson)
            }

            // Other animation properties
            put("duration", options.duration)
            put("delay", options.delay)
            put("easing", options.easing)
            put("loop", options.loop)

            // Opacity animation
            options.opacity?.let { put("opacity", it) }
        }

        val script = "ThreeBridge.animateObject('$objectId', $optionsJson);"
        var result: String? = null

        webView?.evaluateJavascript(script) { value ->
            result = value?.trim('"')
        }

        return result
    }

    /**
     * JavaScript interface for Three.js bridge
     */
    inner class ThreeJSInterface {
        @JavascriptInterface
        fun onBridgeReady() {
            bridgeCallback?.onBridgeReady()
        }

        @JavascriptInterface
        fun onObjectClick(sceneId: String, objectId: String) {
            bridgeCallback?.onObjectClick(sceneId, objectId)
        }
    }
}

/**
 * Scene options for Three.js
 */
data class SceneOptions(
    val backgroundColor: String = "#121212",
    val ambientLight: Boolean = true,
    val directionalLight: Boolean = true,
    val shadows: Boolean = true,
    val controls: Boolean = true,
    val enableZoom: Boolean = true,
    val enablePan: Boolean = true,
    val enableRotate: Boolean = true,
    val autoRotate: Boolean = false,
    val autoRotateSpeed: Float = 1.0f,
    val fov: Int = 75,
    val near: Float = 0.1f,
    val far: Float = 1000f,
    val cameraDistance: Float = 5f
)

/**
 * Cube options for Three.js
 */
data class CubeOptions(
    val width: Float = 1f,
    val height: Float = 1f,
    val depth: Float = 1f,
    val color: Int = 0x00ff00,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val castShadow: Boolean = true,
    val receiveShadow: Boolean = true,
    val metalness: Float = 0.2f,
    val roughness: Float = 0.7f
)

/**
 * Sphere options for Three.js
 */
data class SphereOptions(
    val radius: Float = 1f,
    val color: Int = 0x00ff00,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val castShadow: Boolean = true,
    val receiveShadow: Boolean = true,
    val metalness: Float = 0.2f,
    val roughness: Float = 0.7f,
    val opacity: Float = 1.0f
)

/**
 * Cylinder options for Three.js
 */
data class CylinderOptions(
    val radius: Float = 1f,
    val height: Float = 1f,
    val color: Int = 0x00ff00,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val castShadow: Boolean = true,
    val receiveShadow: Boolean = true,
    val metalness: Float = 0.2f,
    val roughness: Float = 0.7f
)

/**
 * Text options for Three.js
 */
data class TextOptions(
    val text: String,
    val size: Float = 0.5f,
    val color: Int = 0xffffff,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f
)

/**
 * Animation options for Three.js
 */
data class AnimationOptions(
    val position: Position? = null,
    val rotation: Rotation? = null,
    val scale: Scale? = null,
    val opacity: Float? = null,
    val duration: Int = 1000,
    val delay: Int = 0,
    val easing: String = "easeInOutQuad",
    val loop: Boolean = false
)

/**
 * Position for animation
 */
data class Position(
    val x: Any? = null,
    val y: Any? = null,
    val z: Any? = null
)

/**
 * Rotation for animation
 */
data class Rotation(
    val x: Any? = null,
    val y: Any? = null,
    val z: Any? = null
)

/**
 * Scale for animation
 */
data class Scale(
    val x: Any? = null,
    val y: Any? = null,
    val z: Any? = null
)

/**
 * Composable for Three.js visualizations
 */
@Composable
fun ThreeJSScene(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF121212),
    rotationEnabled: Boolean = true,
    initialRotationY: Float = 0f,
    cameraDistance: Float = 5f,
    enableParallax: Boolean = false,
    enableShadows: Boolean = true,
    enableZoom: Boolean = true,
    enableTapInteraction: Boolean = true,
    onTap: () -> Unit = {},
    onBridgeReady: () -> Unit = {}
) {
    val context = LocalContext.current
    val threeJSWrapper = remember { ThreeJSWrapper(context) }

    // Convert backgroundColor to hex string
    val backgroundColorHex = String.format("#%06X", 0xFFFFFF and backgroundColor.toArgb())

    // Set bridge callback
    DisposableEffect(threeJSWrapper) {
        threeJSWrapper.setBridgeCallback(object : ThreeBridgeCallback {
            override fun onBridgeReady() {
                onBridgeReady()

                // Initialize scene
                val sceneId = threeJSWrapper.initScene("three-container", SceneOptions(
                    backgroundColor = backgroundColorHex,
                    ambientLight = true,
                    directionalLight = true,
                    shadows = enableShadows,
                    controls = rotationEnabled,
                    enableZoom = enableZoom,
                    enablePan = enableParallax,
                    enableRotate = rotationEnabled,
                    autoRotate = rotationEnabled,
                    cameraDistance = cameraDistance
                ))

                // Add cube
                sceneId?.let {
                    threeJSWrapper.addCube(it, CubeOptions(
                        width = 1f,
                        height = 1f,
                        depth = 1f,
                        color = 0x6200EE,
                        y = initialRotationY
                    ))
                }
            }

            override fun onObjectClick(sceneId: String, objectId: String) {
                if (enableTapInteraction) {
                    onTap()
                }
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
                    threeJSWrapper.initWebView(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

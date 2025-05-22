package com.example.myapplication.features.ar

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplication.data.model.Habit
import com.example.myapplication.ui.animation.*
import com.example.myapplication.ui.theme.Brown
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

// ARObject and ARObjectType are defined in ARObject.kt

@Composable
fun ARHabitVisualization(
    modifier: Modifier = Modifier,
    arObjects: List<ARObject> = emptyList(),
    onObjectClick: (ARObject) -> Unit = {},
    onAddARObject: (HitResult, ARObjectType) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var arCoreAvailability by remember { mutableStateOf(ArCoreApk.Availability.UNKNOWN_CHECKING) }
    var cameraPermissionGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var userRequestedInstall by remember { mutableStateOf(true) }
    var arInstallErrorText by remember { mutableStateOf<String?>(null) }

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    LaunchedEffect(Unit) {
        val availability = ArCoreApk.getInstance().checkAvailability(context)
        if (availability.isTransient) {
            delay(200)
            arCoreAvailability = ArCoreApk.getInstance().checkAvailability(context)
            return@LaunchedEffect
        }
        arCoreAvailability = availability

        if (availability == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE ||
            availability == ArCoreApk.Availability.UNKNOWN_ERROR ||
            availability == ArCoreApk.Availability.UNKNOWN_TIMED_OUT) {
            return@LaunchedEffect
        }

        if (availability.isSupported) {
            cameraPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionGranted = isGranted
    }

    val requestArCoreInstallLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        userRequestedInstall = true
        ArCoreApk.getInstance().checkAvailability(context).also {
            arCoreAvailability = it
            if (it == ArCoreApk.Availability.SUPPORTED_INSTALLED) {
                cameraPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                if (!cameraPermissionGranted) {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    when (arCoreAvailability) {
        ArCoreApk.Availability.UNKNOWN_CHECKING, ArCoreApk.Availability.UNKNOWN_ERROR, ArCoreApk.Availability.UNKNOWN_TIMED_OUT -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Checking ARCore availability...")
            }
            return
        }
        ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("This device does not support AR.")
            }
            return
        }
        ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED, ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AR features require ARCore. Please install or update it.")
                    arInstallErrorText?.let { error ->
                        Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            try {
                                arInstallErrorText = null
                                when (ArCoreApk.getInstance().requestInstall(activity, userRequestedInstall, ArCoreApk.InstallBehavior.REQUIRED, ArCoreApk.UserMessageType.USER_ALREADY_INFORMED)) {
                                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> { /* Handled by launcher */ }
                                    ArCoreApk.InstallStatus.INSTALLED -> {
                                        arCoreAvailability = ArCoreApk.Availability.SUPPORTED_INSTALLED
                                        cameraPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                        if (!cameraPermissionGranted) {
                                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                                    else -> { /* No-op */ }
                                }
                            } catch (e: UnavailableUserDeclinedInstallationException) {
                                arInstallErrorText = "ARCore installation was declined. AR features cannot be used."
                            } catch (e: Exception) {
                                arInstallErrorText = "Failed to request ARCore installation: ${e.message}"
                            }
                        } else {
                            arInstallErrorText = "Could not find Activity to request ARCore installation."
                        }
                    }) {
                        Text(if (arCoreAvailability == ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD) "Update ARCore" else "Install ARCore")
                    }
                }
            }
            return
        }
        ArCoreApk.Availability.SUPPORTED_INSTALLED -> {
            if (!cameraPermissionGranted) {
                LaunchedEffect(cameraPermissionGranted) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    } else {
                        cameraPermissionGranted = true
                    }
                }

                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission is required for AR features.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Camera Permission")
                        }
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            SideEffect {
                                if (!cameraPermissionGranted) cameraPermissionGranted = true
                            }
                        }
                    }
                }
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            ARScene(
                modifier = modifier,
                arObjects = arObjects,
                onObjectClick = onObjectClick,
                onAddARObject = onAddARObject
            )
        }
    }
}

@Composable
fun ARScene(
    modifier: Modifier = Modifier,
    arObjects: List<ARObject> = emptyList(),
    onObjectClick: (ARObject) -> Unit = {},
    onAddARObject: (HitResult, ARObjectType) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var arRenderer by remember { mutableStateOf<ARRenderer?>(null) }
    var glSurfaceView by remember { mutableStateOf<GLSurfaceView?>(null) }
    var sessionInitializationError by remember { mutableStateOf<String?>(null) }

    // Use the findActivity extension function defined earlier in the file
    val activity = when (context) {
        is Activity -> context
        is ContextWrapper -> context.baseContext.let {
            when (it) {
                is Activity -> it
                else -> null
            }
        }
        else -> null
    }

    LaunchedEffect(activity) {
        if (activity == null) {
            sessionInitializationError = "Could not find Activity to initialize AR session."
            return@LaunchedEffect
        }
        if (arRenderer != null) return@LaunchedEffect

        try {
            when (ArCoreApk.getInstance().requestInstall(activity, true)) {
                ArCoreApk.InstallStatus.INSTALLED -> {
                    // Create AR renderer
                    val renderer = ARRenderer(context)
                    renderer.initializeSession()
                    arRenderer = renderer
                    sessionInitializationError = null
                }
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    sessionInitializationError = "ARCore installation is required. Please complete it."
                }
                else -> {
                    sessionInitializationError = "ARCore is not supported or installation failed."
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            sessionInitializationError = "ARCore installation was declined by the user."
        } catch (e: Exception) {
            sessionInitializationError = "Failed to initialize AR session: ${e.message}"
            Log.e("ARScene", "Session Init Error", e)
            arRenderer = null
        }
    }

    // Handle session lifecycle
    DisposableEffect(Unit) {
        // Resume session when composable enters composition
        try {
            arRenderer?.resumeSession()
            Log.d("ARScene", "ARCore session resumed")
        } catch (e: Exception) {
            Log.e("ARScene", "Error resuming ARCore session", e)
            sessionInitializationError = "Error resuming ARCore: ${e.message}"
        }

        onDispose {
            // Pause session when composable leaves composition
            try {
                arRenderer?.pauseSession()
                Log.d("ARScene", "ARCore session paused")
            } catch (e: Exception) {
                Log.e("ARScene", "Error pausing ARCore session", e)
            }

            // Clean up resources
            glSurfaceView?.onPause()
            arRenderer?.cleanup()
            arRenderer = null
            glSurfaceView = null
        }
    }

    if (sessionInitializationError != null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(sessionInitializationError!!)
        }
        return
    }

    if (arRenderer == null || activity == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            Text("Initializing AR session...", modifier = Modifier.padding(top = 8.dp))
        }
        return
    }

    var cameraActive by remember { mutableStateOf(true) }
    var selectedObject by remember { mutableStateOf<ARObject?>(null) }
    var draggedObjectId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val coroutineScope = rememberCoroutineScope()
    val mutableARObjects = remember { arObjects.toMutableStateList() }

    // Track session state for UI feedback
    val isSessionPausedState = remember { derivedStateOf { arRenderer?.isSessionResumed?.value?.not() ?: false } }
    val isSessionPaused = isSessionPausedState.value

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val tappedObject = mutableARObjects.firstOrNull { arObject ->
                            val objectRadius = 50f * arObject.scale
                            val distance = (arObject.position - offset).getDistance()
                            distance < objectRadius
                        }

                        if (tappedObject != null) {
                            selectedObject = tappedObject
                            onObjectClick(tappedObject)
                        } else {
                            if (cameraActive) {
                                coroutineScope.launch {
                                    val randomType = ARObjectType.entries.random()
                                    // Add object directly to the list instead of calling a separate function
                                    mutableARObjects.add(
                                        ARObject(
                                            type = randomType,
                                            position = offset
                                        )
                                    )
                                }
                            }
                            selectedObject = null
                        }
                    }
                )
            }
            // ... (rest of ARScene, including pointerInput for drag, Canvas, ParticleSystem, object rendering, controls) ...
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val draggedObject = mutableARObjects.firstOrNull { arObject ->
                            val objectRadius = 50f * arObject.scale
                            val distance = (arObject.position - offset).getDistance()
                            distance < objectRadius
                        }
                        // Use a unique identifier for dragging
                        draggedObjectId = mutableARObjects.indexOf(draggedObject).takeIf { it >= 0 }?.toString()
                        dragOffset = offset
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                        draggedObjectId?.let { id ->
                            val index = id.toIntOrNull() ?: -1
                            if (index >= 0 && index < mutableARObjects.size) {
                                mutableARObjects[index] = mutableARObjects[index].copy(
                                    position = mutableARObjects[index].position + dragAmount
                                )
                            }
                        }
                    },
                    onDragEnd = { draggedObjectId = null },
                    onDragCancel = { draggedObjectId = null }
                )
            }
    ) {
        if (cameraActive) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val gridSize = 50f
                val gridColor = Color.Black.copy(alpha = 0.1f)
                for (y in 0..(size.height / gridSize).toInt()) {
                    drawLine(color = gridColor, start = Offset(0f, y * gridSize), end = Offset(size.width, y * gridSize), strokeWidth = 1f)
                }
                for (x in 0..(size.width / gridSize).toInt()) {
                    drawLine(color = gridColor, start = Offset(x * gridSize, 0f), end = Offset(x * gridSize, size.height), strokeWidth = 1f)
                }
            }
            ParticleSystem(
                modifier = Modifier.matchParentSize(),
                particleCount = 30, particleColor = Color.Black,
                particleSize = 2.dp, maxSpeed = 0.2f, fadeDistance = 0.9f,
                particleShape = ParticleShape.CIRCLE, particleEffect = ParticleEffect.FLOAT,
                colorVariation = false, glowEffect = false
            )
        }

        mutableARObjects.forEach { arObject ->
            val isSelected = selectedObject == arObject
            val isDragged = draggedObjectId == mutableARObjects.indexOf(arObject).toString()

            // Render AR object directly instead of using a separate composable
            Box(
                modifier = Modifier
                    .offset { IntOffset(arObject.position.x.toInt(), arObject.position.y.toInt()) }
                    .size((100 * arObject.scale).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = if (isSelected) 0.8f else 0.6f))
                    .border(
                        width = if (isSelected || isDragged) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                        shape = CircleShape
                    )
                    .clickable { onObjectClick(arObject) },
                contentAlignment = Alignment.Center
            ) {
                // Icon based on object type
                Icon(
                    imageVector = when (arObject.type) {
                        ARObjectType.HABIT_TREE -> Icons.Default.Park
                        ARObjectType.STREAK_FLAME -> Icons.Default.LocalFireDepartment
                        ARObjectType.ACHIEVEMENT_TROPHY -> Icons.Default.EmojiEvents
                        ARObjectType.PROGRESS_CHART -> Icons.AutoMirrored.Filled.ShowChart
                        ARObjectType.HABIT_REMINDER -> Icons.Default.Notifications
                        ARObjectType.MOTIVATION_OBJECT -> Icons.Default.Star
                        ARObjectType.CUSTOM_OBJECT -> Icons.Default.Favorite
                        ARObjectType.HABIT_CUBE -> Icons.Default.ViewInAr
                        ARObjectType.HABIT_VISUALIZATION -> Icons.Default.Visibility
                    },
                    contentDescription = arObject.type.name,
                    tint = Color.White,
                    modifier = Modifier.size((50 * arObject.scale).dp)
                )

                // Label if available
                arObject.label?.let { label ->
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    )
                }
            }
        }
        // Session paused overlay
        if (isSessionPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .width(300.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AR Session Paused",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The AR session is currently paused. This may happen when the app goes to the background or when the camera is not available.",
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                // Try to resume the session
                                try {
                                    // Resume the session using the renderer
                                    arRenderer?.resumeSession()
                                } catch (e: Exception) {
                                    Log.e("ARScene", "Error resuming session", e)
                                }
                            }
                        ) {
                            Text("Try to Resume")
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            arRenderer?.getSelectedObject()?.let { selectedArObj ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(text = selectedArObj.label ?: selectedArObj.type.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (selectedArObj.relatedHabitId != null) {
                            Text(text = "Related to habit ID: ${selectedArObj.relatedHabitId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FloatingActionButton(
                    onClick = { /* TODO: Toggle AR info/debug view? */ },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(imageVector = Icons.Outlined.CameraAlt, contentDescription = "AR Info")
                }
                FloatingActionButton(
                    onClick = {
                        // Only allow placing objects if session is not paused
                        if (isSessionPaused.not()) {
                            arRenderer?.placeObjectInFront()
                        }
                    },
                    containerColor = if (isSessionPaused)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.Add,
                        "Add Test Object",
                        tint = if (isSessionPaused) Color.Gray else LocalContentColor.current
                    )
                }
                FloatingActionButton(
                    onClick = { arRenderer?.clearAllObjects() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) { Icon(Icons.Default.Clear, "Clear All") }
            }
        }
    }
}

class ARCoreRenderer(
    private val context: Context,
    private val session: Session,
    initialARObjects: List<ARObject>,
    private val onScreenTapCallback: (HitResult) -> Unit
) : GLSurfaceView.Renderer {

    private val TAG = "ARCoreRenderer"
    private val backgroundRenderer = BackgroundRenderer()
    private val objectRenderer = ARObjectRenderer()
    private val arObjects = initialARObjects.toMutableList()
    private val anchors = mutableListOf<com.google.ar.core.Anchor>()
    private val anchorToObjectMap = mutableMapOf<com.google.ar.core.Anchor, ARObject>()

    // Track session state manually since isResumed is not available in this ARCore version
    private var _isSessionResumed = true // Assume initially resumed
    val isSessionResumed: Boolean
        get() = _isSessionResumed

    // Method to manually set session state
    fun setSessionResumed(resumed: Boolean) {
        _isSessionResumed = resumed
    }

    private val tapQueue = java.util.concurrent.LinkedBlockingQueue<MotionEvent>()

    fun updateARObjects(newObjects: List<ARObject>) {
        arObjects.clear()
        arObjects.addAll(newObjects)
    }

    fun handleTap(motionEvent: MotionEvent) {
        tapQueue.offer(motionEvent)
    }

    fun placeObjectInFront() {
        try {
            // Check if session is paused using our custom state tracking
            if (_isSessionResumed) {
                try {
                    val frame = session.update()
                    frame?.camera?.takeIf { it.trackingState == TrackingState.TRACKING }?.let {
                        val pose = it.pose.compose(Pose.makeTranslation(0f, 0f, -1f)).extractTranslation()
                        val newAnchor = session.createAnchor(pose)
                        anchors.add(newAnchor)

                        // Create a new AR object
                        val newObject = ARObject(
                            id = UUID.randomUUID().toString(),
                            type = ARObjectType.HABIT_CUBE,
                            position = Offset.Zero, // Position will be determined by the anchor
                            scale = 0.15f,
                            color = 0xFF6200EE.toInt()
                        )

                        // Add to our collections
                        arObjects.add(newObject)
                        anchorToObjectMap[newAnchor] = newObject

                        Log.d(TAG, "Object placed in front via FAB, new anchor: ${newAnchor.pose}")
                    }
                } catch (e: com.google.ar.core.exceptions.SessionPausedException) {
                    // Session is paused, update our state tracker
                    _isSessionResumed = false
                    Log.w(TAG, "Session is paused, cannot update frame", e)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating session", e)
                }
            } else {
                Log.w(TAG, "Cannot place object - ARCore session is paused")
                // Could show a Toast or other UI feedback here
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error placing object in AR scene", e)
            // Could show a Toast or other UI feedback here
        }
    }

    fun clearAllObjects() {
        try {
            // Safe detach of anchors
            anchors.forEach {
                try {
                    it.detach()
                } catch (e: Exception) {
                    Log.e(TAG, "Error detaching anchor", e)
                }
            }
            anchors.clear()
            arObjects.clear()
            anchorToObjectMap.clear()
            Log.d(TAG, "All objects and anchors cleared.")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing AR objects", e)
        }
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        // Initialize renderers
        backgroundRenderer.createOnGlThread(context)
        objectRenderer.createOnGlThread(context)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        displayRotationHelper?.updateSessionIfNeeded(session)
        GLES20.glViewport(0, 0, width, height)
        session.setDisplayGeometry(displayRotationHelper?.getRotation() ?: Surface.ROTATION_0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Skip rendering if session is not resumed (using our custom state tracking)
        if (!_isSessionResumed) {
            // Just render a blank frame
            return
        }

        displayRotationHelper?.updateSessionIfNeeded(session)

        try {
            session.setCameraTextureName(backgroundRenderer.textureId)

            // Try to update the session and get a new frame
            val frame = try {
                session.update()
            } catch (e: com.google.ar.core.exceptions.SessionPausedException) {
                // Session is paused, update our state tracker
                _isSessionResumed = false
                Log.d(TAG, "Cannot update frame, session is paused")
                return
            } catch (e: Exception) {
                Log.e(TAG, "Exception updating ARCore session", e)
                return
            } ?: return

            val camera = frame.camera

            // Draw the camera background
            try {
                backgroundRenderer.draw(frame)
            } catch (e: Exception) {
                Log.e(TAG, "Exception drawing background", e)
            }

            // Only handle interactions when tracking is good
            if (camera.trackingState == TrackingState.TRACKING) {
                try {
                    handleQueuedTap(frame)

                    val projmtx = FloatArray(16)
                    camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f)
                    val viewmtx = FloatArray(16)
                    camera.getViewMatrix(viewmtx, 0)

                    // Render all anchors with their associated AR objects
                    GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                    GLES20.glDepthMask(true)

                    // Iterate through all anchors and render their associated objects
                    for (anchor in anchors) {
                        // Skip anchors that are not tracking
                        if (anchor.trackingState != TrackingState.TRACKING) continue

                        // Get the associated AR object
                        val arObject = anchorToObjectMap[anchor] ?: continue

                        // Draw the object
                        objectRenderer.draw(viewmtx, projmtx, anchor, arObject.scale)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception handling AR interactions", e)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Exception on ARCore draw frame", t)
        }
    }

    private fun handleQueuedTap(frame: Frame) {
        // Skip if session is not in a good state (using our custom state tracking)
        if (!_isSessionResumed) {
            tapQueue.clear() // Clear any pending taps
            return
        }

        tapQueue.poll()?.let { motionEvent ->
            try {
                frame.hitTest(motionEvent).firstOrNull { hit ->
                    val trackable = hit.trackable
                    (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose) &&
                     PlaneRenderer.isPlaneHitInExtents(trackable, hit.hitPose)) || // Example from ARCore samples
                    (trackable is Point && trackable.orientationMode == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)
                }?.also { hitResult ->
                    try {
                        val newAnchor = session.createAnchor(hitResult.hitPose)
                        anchors.add(newAnchor)

                        // Create a new AR object
                        val newObject = ARObject(
                            id = UUID.randomUUID().toString(),
                            type = ARObjectType.HABIT_VISUALIZATION,
                            position = Offset.Zero, // Position will be determined by the anchor
                            scale = 0.2f,
                            color = 0xFF03DAC5.toInt()
                        )

                        // Add to our collections
                        arObjects.add(newObject)
                        anchorToObjectMap[newAnchor] = newObject

                        onScreenTapCallback(hitResult)
                        Log.d(TAG, "Hit test successful, new anchor created: ${newAnchor.pose}")
                    } catch (e: com.google.ar.core.exceptions.SessionPausedException) {
                        // Session is paused, update our state tracker
                        _isSessionResumed = false
                        Log.w(TAG, "Session is paused, cannot create anchor", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating anchor from hit test", e)
                    }
                }
            } catch (e: com.google.ar.core.exceptions.SessionPausedException) {
                // Session is paused, update our state tracker
                _isSessionResumed = false
                Log.w(TAG, "Session is paused, cannot perform hit test", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error performing hit test", e)
            }
        }
    }

    var displayRotationHelper: DisplayRotationHelper? = null
    fun getSelectedObject(): ARObject? {
        return null
    }
}

class BackgroundRenderer {
    private val TAG = "BackgroundRenderer"
    companion object {
        private const val COORDS_PER_VERTEX = 2
        private const val TEXCOORDS_PER_VERTEX = 2
        private const val FLOAT_SIZE = 4
        private const val GL_TEXTURE_EXTERNAL_OES = 0x8D65 // OpenGL ES extension constant
    }

    private var quadVertices: FloatBuffer? = null
    private var quadTexCoord: FloatBuffer? = null
    private var quadTexCoordTransformed: FloatBuffer? = null

    private var program = 0
    var textureId = -1
        private set

    private var positionHandle = 0
    private var texCoordHandle = 0

    fun createOnGlThread(context: Context?) {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

        val vertexShaderCode = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_TexCoord;
            void main() {
               gl_Position = a_Position;
               v_TexCoord = a_TexCoord;
            }
        """
        val fragmentShaderCode = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 v_TexCoord;
            uniform samplerExternalOES sTexture;
            void main() {
              gl_FragColor = texture2D(sTexture, v_TexCoord);
            }
        """

        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).also {
            GLES20.glShaderSource(it, vertexShaderCode)
            GLES20.glCompileShader(it)
        }
        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).also {
            GLES20.glShaderSource(it, fragmentShaderCode)
            GLES20.glCompileShader(it)
        }

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
            GLES20.glUseProgram(it)
        }

        positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        texCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoord")

        val bbVertices = ByteBuffer.allocateDirect(4 * COORDS_PER_VERTEX * FLOAT_SIZE)
        bbVertices.order(ByteOrder.nativeOrder())
        quadVertices = bbVertices.asFloatBuffer()
        quadVertices?.put(floatArrayOf(-1.0f, -1.0f, +1.0f, -1.0f, -1.0f, +1.0f, +1.0f, +1.0f))
        quadVertices?.position(0)

        val bbTexCoords = ByteBuffer.allocateDirect(4 * TEXCOORDS_PER_VERTEX * FLOAT_SIZE)
        bbTexCoords.order(ByteOrder.nativeOrder())
        quadTexCoord = bbTexCoords.asFloatBuffer()
        quadTexCoord?.put(floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f))
        quadTexCoord?.position(0)

        val bbTexCoordsTransformed = ByteBuffer.allocateDirect(4 * TEXCOORDS_PER_VERTEX * FLOAT_SIZE)
        bbTexCoordsTransformed.order(ByteOrder.nativeOrder())
        quadTexCoordTransformed = bbTexCoordsTransformed.asFloatBuffer()
    }

    fun draw(frame: Frame) {
        if (frame.hasDisplayGeometryChanged()) {
            // Define constants for Coordinates2d if they're not available
            val TEXTURE_NORMALIZE_FROM_VIEW_NORMALIZE = 1
            val TEXTURE_NORMALIZE_TO_SCREEN_NORMALIZE = 2

            // Skip coordinate transformation in this simplified implementation
            // Just copy the original coordinates
            quadTexCoord?.position(0)
            quadTexCoordTransformed?.position(0)
            quadTexCoord?.let { src ->
                quadTexCoordTransformed?.let { dst ->
                    src.position(0)
                    dst.position(0)
                    // Copy the coordinates
                    for (i in 0 until src.capacity()) {
                        dst.put(i, src.get(i))
                    }
                }
            }
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUseProgram(program)

        quadVertices?.position(0)
        // Explicitly cast to java.nio.Buffer to resolve ambiguity
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadVertices as java.nio.Buffer)

        quadTexCoordTransformed?.position(0)
        // Explicitly cast to java.nio.Buffer to resolve ambiguity
        GLES20.glVertexAttribPointer(texCoordHandle, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadTexCoordTransformed as java.nio.Buffer)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)

        GLES20.glDepthMask(true)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }
}

object PlaneRenderer {
    @Suppress("unused")
    fun isPlaneHitInExtents(plane: Plane, hitPose: Pose): Boolean {
        val planeCenter = plane.centerPose
        val distance = kotlin.math.sqrt(
            (hitPose.tx() - planeCenter.tx()).pow(2) +
            (hitPose.ty() - planeCenter.ty()).pow(2) +
            (hitPose.tz() - planeCenter.tz()).pow(2)
        )
        return distance < 2.0f
    }
}

class DisplayRotationHelper(private val context: Context) : SensorEventListener {
    private var displayRotation = context.display?.rotation ?: Surface.ROTATION_0
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    fun onResume() {
        rotationSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun onPause() {
        sensorManager.unregisterListener(this)
    }

    @Suppress("unused")
    fun onDisplayRotationChanged() {
        displayRotation = context.display?.rotation ?: Surface.ROTATION_0
    }

    @Suppress("unused")
    fun getRotation(): Int = displayRotation

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @Suppress("unused")
    override fun onSensorChanged(event: SensorEvent?) {}

    @Suppress("unused")
    fun updateSessionIfNeeded(session: Session?) {
        session ?: return
        val currentRotation = context.display?.rotation ?: Surface.ROTATION_0
        if (currentRotation != displayRotation) {
            displayRotation = currentRotation
            session.setDisplayGeometry(displayRotation, context.resources.displayMetrics.widthPixels, context.resources.displayMetrics.heightPixels)
        }
    }
}

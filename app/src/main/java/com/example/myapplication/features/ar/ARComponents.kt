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
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import java.util.*
import kotlin.math.*

data class ARObject(
    val id: String = UUID.randomUUID().toString(),
    val type: ARObjectType,
    val position: Offset = Offset.Zero,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val color: Color = Color.White,
    val label: String? = null,
    val relatedHabitId: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

enum class ARObjectType {
    HABIT_TREE,
    STREAK_FLAME,
    ACHIEVEMENT_TROPHY,
    PROGRESS_CHART,
    HABIT_REMINDER,
    MOTIVATION_OBJECT,
    CUSTOM_OBJECT
}

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
    var arSession by remember { mutableStateOf<Session?>(null) }
    var glSurfaceView by remember { mutableStateOf<GLSurfaceView?>(null) }
    var arRenderer by remember { mutableStateOf<ARCoreRenderer?>(null) }
    var displayRotationHelper by remember { mutableStateOf<DisplayRotationHelper?>(null) }
    var sessionInitializationError by remember { mutableStateOf<String?>(null) }

    val activity = context.findActivity()

    LaunchedEffect(activity) {
        if (activity == null) {
            sessionInitializationError = "Could not find Activity to initialize AR session."
            return@LaunchedEffect
        }
        if (arSession != null) return@LaunchedEffect

        try {
            when (ArCoreApk.getInstance().requestInstall(activity, true)) {
                ArCoreApk.InstallStatus.INSTALLED -> {
                    val session = Session(activity)
                    val config = Config(session)
                    config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    session.configure(config)
                    arSession = session

                    val renderer = ARCoreRenderer(activity, session, arObjects) { hitResult ->
                        onAddARObject(hitResult, ARObjectType.entries.random())
                    }
                    arRenderer = renderer
                    displayRotationHelper = DisplayRotationHelper(activity)
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
            arSession = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            glSurfaceView?.onPause()
            arSession?.close()
            displayRotationHelper?.onPause()
            arSession = null
            arRenderer = null
            glSurfaceView = null
            displayRotationHelper = null
        }
    }

    if (sessionInitializationError != null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(sessionInitializationError!!)
        }
        return
    }

    if (arSession == null || arRenderer == null || activity == null) {
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
                                    onAddObject(randomType, offset) 
                                    mutableARObjects.add(
                                        ARObject(
                                            type = randomType,
                                            position = offset, 
                                            color = Color.hsl(
                                                Random().nextFloat() * 360f,
                                                0.7f,
                                                0.6f
                                            )
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
                        draggedObjectId = draggedObject?.id
                        dragOffset = offset 
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                        draggedObjectId?.let { id ->
                            val index = mutableARObjects.indexOfFirst { it.id == id }
                            if (index >= 0) {
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
            val isSelected = selectedObject?.id == arObject.id
            val isDragged = draggedObjectId == arObject.id
            ARObjectRenderer( 
                arObject = arObject,
                isSelected = isSelected,
                isDragged = isDragged,
                onClick = { onObjectClick(arObject) }
            )
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
                    onClick = { arRenderer?.placeObjectInFront() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) { Icon(Icons.Default.Add, "Add Test Object") }
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
    private val arObjects = initialARObjects.toMutableList()
    private val anchors = mutableListOf<com.google.ar.core.Anchor>()

    private val tapQueue = java.util.concurrent.LinkedBlockingQueue<MotionEvent>()

    fun updateARObjects(newObjects: List<ARObject>) {
        arObjects.clear()
        arObjects.addAll(newObjects)
    }

    fun handleTap(motionEvent: MotionEvent) {
        tapQueue.offer(motionEvent)
    }

    fun placeObjectInFront() {
        session.update()?.camera?.takeIf { it.trackingState == TrackingState.TRACKING }?.let {
            val pose = it.pose.compose(Pose.makeTranslation(0f, 0f, -1f)).extractTranslation()
            val newAnchor = session.createAnchor(pose)
            anchors.add(newAnchor)
            Log.d(TAG, "Object placed in front via FAB, new anchor: ${newAnchor.pose}")
        }
    }

    fun clearAllObjects() {
        anchors.forEach { it.detach() }
        anchors.clear()
        arObjects.clear()
        Log.d(TAG, "All objects and anchors cleared.")
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        backgroundRenderer.createOnGlThread(context)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        displayRotationHelper?.updateSessionIfNeeded(session)
        GLES20.glViewport(0, 0, width, height)
        session.setDisplayGeometry(displayRotationHelper?.getRotation() ?: Surface.ROTATION_0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        displayRotationHelper?.updateSessionIfNeeded(session)

        try {
            session.setCameraTextureName(backgroundRenderer.textureId)
            val frame = session.update() ?: return
            val camera = frame.camera

            backgroundRenderer.draw(frame)

            if (camera.trackingState == TrackingState.TRACKING) {
                handleQueuedTap(frame)

                val projmtx = FloatArray(16)
                camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f)
                val viewmtx = FloatArray(16)
                camera.getViewMatrix(viewmtx, 0)

                // TODO: Render all `anchors` with their associated 3D models/ARObjects
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Exception on ARCore draw frame", t)
        }
    }

    private fun handleQueuedTap(frame: Frame) {
        tapQueue.poll()?.let { motionEvent ->
            frame.hitTest(motionEvent).firstOrNull { hit ->
                val trackable = hit.trackable
                (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose) &&
                 PlaneRenderer.isPlaneHitInExtents(trackable, hit.hitPose)) || // Example from ARCore samples
                (trackable is Point && trackable.orientationMode == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)
            }?.also { hitResult ->
                val newAnchor = session.createAnchor(hitResult.hitPose)
                anchors.add(newAnchor)
                onScreenTapCallback(hitResult)
                Log.d(TAG, "Hit test successful, new anchor created: ${newAnchor.pose}")
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

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
            frame.transformCoordinates2d(
                com.google.ar.core.Coordinates2d.TEXTURE_NORMALIZE_FROM_VIEW_NORMALIZE,
                quadTexCoord,
                com.google.ar.core.Coordinates2d.TEXTURE_NORMALIZE_TO_SCREEN_NORMALIZE,
                quadTexCoordTransformed
            )
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUseProgram(program)

        quadVertices?.position(0)
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadVertices)

        quadTexCoordTransformed?.position(0)
        GLES20.glVertexAttribPointer(texCoordHandle, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadTexCoordTransformed)

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

package com.example.myapplication.features.ar

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import com.example.myapplication.core.data.model.Habit
import com.google.ar.core.*
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs

/**
 * Handles AR rendering and interactions
 */
class ARRenderer(private val context: Context) {
    companion object {
        private const val TAG = "ARRenderer"
    }

    // AR session
    private var session: Session? = null
    private val anchors = CopyOnWriteArrayList<Anchor>()
    private val _arObjectsMap = mutableMapOf<String, ARObject>()

    // State
    private val _isSessionResumed = MutableStateFlow(false)
    val isSessionResumed: StateFlow<Boolean> = _isSessionResumed.asStateFlow()

    private val _trackingState = MutableStateFlow(TrackingState.STOPPED)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _arObjects = MutableStateFlow<List<ARObject>>(emptyList())
    val arObjects: StateFlow<List<ARObject>> = _arObjects.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Queue for tap events
    private val tapQueue = ArrayDeque<MotionEvent>()

    /**
     * Initialize AR session
     */
    fun initializeSession() {
        try {
            if (session == null) {
                val sessionConfig = Config(Session(context))
                sessionConfig.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                sessionConfig.lightEstimationMode = Config.LightEstimationMode.AMBIENT_INTENSITY
                sessionConfig.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE

                session = Session(context).apply {
                    configure(sessionConfig)
                }

                Log.d(TAG, "AR session initialized")
            }
        } catch (e: UnavailableException) {
            _errorMessage.value = "AR not available: ${e.message}"
            Log.e(TAG, "Failed to initialize AR session", e)
        } catch (e: Exception) {
            _errorMessage.value = "Error initializing AR: ${e.message}"
            Log.e(TAG, "Failed to initialize AR session", e)
        }
    }

    /**
     * Resume AR session
     */
    fun resumeSession() {
        try {
            session?.let { arSession ->
                arSession.resume()
                _isSessionResumed.value = true
                Log.d(TAG, "AR session resumed")
            }
        } catch (e: CameraNotAvailableException) {
            _errorMessage.value = "Camera not available: ${e.message}"
            Log.e(TAG, "Failed to resume AR session", e)
        } catch (e: Exception) {
            _errorMessage.value = "Error resuming AR: ${e.message}"
            Log.e(TAG, "Failed to resume AR session", e)
        }
    }

    /**
     * Pause AR session
     */
    fun pauseSession() {
        try {
            session?.pause()
            _isSessionResumed.value = false
            Log.d(TAG, "AR session paused")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause AR session", e)
        }
    }

    /**
     * Handle frame update
     */
    fun onDrawFrame() {
        val session = this.session ?: return

        try {
            if (!_isSessionResumed.value) return

            // Update session
            val frame = session.update()
            val camera = frame.camera

            // Update tracking state
            _trackingState.value = camera.trackingState

            // Only handle interactions when tracking is good
            if (camera.trackingState == TrackingState.TRACKING) {
                try {
                    handleQueuedTap(frame)

                    // Render all anchors with their associated 3D models/ARObjects
                    updateARObjects()
                } catch (e: Exception) {
                    Log.e(TAG, "Exception handling AR interactions", e)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Exception on ARCore draw frame", t)
        }
    }

    /**
     * Handle tap events
     */
    fun handleTap(motionEvent: MotionEvent) {
        if (_isSessionResumed.value && _trackingState.value == TrackingState.TRACKING) {
            tapQueue.add(motionEvent)
        }
    }

    /**
     * Process queued tap events
     */
    private fun handleQueuedTap(frame: Frame) {
        // Skip if session is not in a good state
        if (!_isSessionResumed.value) {
            tapQueue.clear() // Clear any pending taps
            return
        }

        // Process all queued taps
        while (tapQueue.isNotEmpty()) {
            val tap = tapQueue.removeFirst()

            // Perform hit test
            val hitResult = frame.hitTest(tap)
            val trackableHit = hitResult.firstOrNull { hit ->
                (hit.trackable is Plane && (hit.trackable as Plane).isPoseInPolygon(hit.hitPose)) ||
                hit.trackable is Point
            }

            // Create anchor at hit point
            trackableHit?.let { hit ->
                val anchor = hit.trackable.createAnchor(hit.hitPose)
                anchors.add(anchor)

                // Create AR object
                val arObject = ARObject(
                    type = ARObjectType.HABIT_TREE,
                    position = Offset(hit.hitPose.tx(), hit.hitPose.tz())
                )

                _arObjectsMap[anchor.cloudAnchorId] = arObject
                _arObjects.value = _arObjectsMap.values.toList()

                Log.d(TAG, "Created anchor and AR object at ${hit.hitPose.tx()}, ${hit.hitPose.tz()}")
            }
        }
    }

    /**
     * Place an object in front of the camera
     */
    fun placeObjectInFront() {
        val session = this.session ?: return

        try {
            val frame = session.update()
            val camera = frame.camera

            if (camera.trackingState != TrackingState.TRACKING) return

            // Get camera pose
            val pose = camera.pose

            // Create pose 1 meter in front of camera
            val forward = pose.getZAxis()
            val position = pose.getTranslation()
            val forwardPose = Pose.makeTranslation(
                position[0] - forward[0],
                position[1] - forward[1],
                position[2] - forward[2]
            )

            // Create anchor
            val anchor = session.createAnchor(forwardPose)
            anchors.add(anchor)

            // Create AR object
            val arObject = ARObject(
                type = ARObjectType.HABIT_TREE,
                position = Offset(forwardPose.tx(), forwardPose.tz())
            )

            _arObjectsMap[anchor.cloudAnchorId] = arObject
            _arObjects.value = _arObjectsMap.values.toList()

            Log.d(TAG, "Placed object in front of camera")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to place object in front", e)
        }
    }

    /**
     * Place a habit visualization in AR
     */
    fun placeHabitVisualization(habit: Habit) {
        val session = this.session ?: return

        try {
            val frame = session.update()
            val camera = frame.camera

            if (camera.trackingState != TrackingState.TRACKING) return

            // Get camera pose
            val pose = camera.pose

            // Create pose 1 meter in front of camera
            val forward = pose.getZAxis()
            val position = pose.getTranslation()
            val forwardPose = Pose.makeTranslation(
                position[0] - forward[0],
                position[1] - forward[1],
                position[2] - forward[2]
            )

            // Create anchor
            val anchor = session.createAnchor(forwardPose)
            anchors.add(anchor)

            // Create AR object based on habit
            val arObject = ARObject(
                type = when {
                    habit.streak > 10 -> ARObjectType.ACHIEVEMENT_TROPHY
                    habit.streak > 5 -> ARObjectType.STREAK_FLAME
                    else -> ARObjectType.HABIT_TREE
                },
                position = Offset(forwardPose.tx(), forwardPose.tz()),
                scale = 1.0f + (habit.streak * 0.1f).coerceAtMost(2.0f),
                label = habit.name,
                relatedHabitId = habit.id
            )

            _arObjectsMap[anchor.cloudAnchorId] = arObject
            _arObjects.value = _arObjectsMap.values.toList()

            Log.d(TAG, "Placed habit visualization for ${habit.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to place habit visualization", e)
        }
    }

    /**
     * Update AR objects
     */
    private fun updateARObjects() {
        // Update positions and states of AR objects
        anchors.forEach { anchor ->
            _arObjectsMap[anchor.cloudAnchorId]?.let { arObject ->
                // Update position based on anchor pose
                val pose = anchor.pose
                val updatedObject = arObject.copy(
                    position = Offset(pose.tx(), pose.tz())
                )
                _arObjectsMap[anchor.cloudAnchorId] = updatedObject
            }
        }

        // Remove detached anchors
        val iterator = anchors.iterator()
        while (iterator.hasNext()) {
            val anchor = iterator.next()
            if (anchor.trackingState == TrackingState.STOPPED) {
                _arObjectsMap.remove(anchor.cloudAnchorId)
                iterator.remove()
            }
        }

        // Update state
        _arObjects.value = _arObjectsMap.values.toList()
    }

    /**
     * Clear all AR objects
     */
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
            _arObjectsMap.clear()
            _arObjects.value = emptyList()
            Log.d(TAG, "All objects and anchors cleared.")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing AR objects", e)
        }
    }

    /**
     * Get selected object
     */
    fun getSelectedObject(): ARObject? {
        return _arObjects.value.firstOrNull()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            session?.pause()
            session = null
            anchors.clear()
            _arObjectsMap.clear()
            _arObjects.value = emptyList()
            _isSessionResumed.value = false
            Log.d(TAG, "AR resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up AR resources", e)
        }
    }
}

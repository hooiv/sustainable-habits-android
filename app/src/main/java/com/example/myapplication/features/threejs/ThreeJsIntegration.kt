package com.example.myapplication.features.threejs

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.data.repository.HabitRepository
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
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Integration with Three.js for 3D visualizations
 */
@Singleton
class ThreeJsIntegration @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitRepository: HabitRepository
) {
    companion object {
        private const val TAG = "ThreeJsIntegration"

        // HTML template for Three.js scene
        private const val HTML_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                <title>Three.js Visualization</title>
                <style>
                    body { margin: 0; overflow: hidden; }
                    canvas { display: block; }
                </style>
                <script src="https://cdn.jsdelivr.net/npm/three@0.132.2/build/three.min.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/three@0.132.2/examples/js/controls/OrbitControls.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/three@0.132.2/examples/js/loaders/GLTFLoader.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/gsap@3.7.1/dist/gsap.min.js"></script>
            </head>
            <body>
                <script>
                    // Scene setup
                    const scene = new THREE.Scene();
                    scene.background = new THREE.Color(0x121212);

                    // Camera setup
                    const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
                    camera.position.z = 15;

                    // Renderer setup
                    const renderer = new THREE.WebGLRenderer({ antialias: true });
                    renderer.setSize(window.innerWidth, window.innerHeight);
                    renderer.setPixelRatio(window.devicePixelRatio);
                    document.body.appendChild(renderer.domElement);

                    // Controls setup
                    const controls = new THREE.OrbitControls(camera, renderer.domElement);
                    controls.enableDamping = true;
                    controls.dampingFactor = 0.05;

                    // Lights setup
                    const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
                    scene.add(ambientLight);

                    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
                    directionalLight.position.set(1, 1, 1);
                    scene.add(directionalLight);

                    // Objects container
                    const objects = {};

                    // Animation mixer
                    const mixer = new THREE.AnimationMixer(scene);

                    // Clock for animations
                    const clock = new THREE.Clock();

                    // Resize handler
                    window.addEventListener('resize', () => {
                        camera.aspect = window.innerWidth / window.innerHeight;
                        camera.updateProjectionMatrix();
                        renderer.setSize(window.innerWidth, window.innerHeight);
                    });

                    // Animation loop
                    function animate() {
                        requestAnimationFrame(animate);

                        const delta = clock.getDelta();
                        mixer.update(delta);
                        controls.update();

                        renderer.render(scene, camera);
                    }

                    // Start animation loop
                    animate();

                    // Create a cube
                    function createCube(id, x, y, z, size, color) {
                        const geometry = new THREE.BoxGeometry(size, size, size);
                        const material = new THREE.MeshStandardMaterial({
                            color: color,
                            roughness: 0.7,
                            metalness: 0.3
                        });
                        const cube = new THREE.Mesh(geometry, material);
                        cube.position.set(x, y, z);
                        scene.add(cube);

                        // Store reference
                        objects[id] = cube;

                        // Animate creation
                        gsap.from(cube.scale, {
                            x: 0,
                            y: 0,
                            z: 0,
                            duration: 1,
                            ease: "elastic.out(1, 0.3)"
                        });

                        return id;
                    }

                    // Create a sphere
                    function createSphere(id, x, y, z, radius, color) {
                        const geometry = new THREE.SphereGeometry(radius, 32, 32);
                        const material = new THREE.MeshStandardMaterial({
                            color: color,
                            roughness: 0.7,
                            metalness: 0.3
                        });
                        const sphere = new THREE.Mesh(geometry, material);
                        sphere.position.set(x, y, z);
                        scene.add(sphere);

                        // Store reference
                        objects[id] = sphere;

                        // Animate creation
                        gsap.from(sphere.scale, {
                            x: 0,
                            y: 0,
                            z: 0,
                            duration: 1,
                            ease: "elastic.out(1, 0.3)"
                        });

                        return id;
                    }

                    // Create a cylinder
                    function createCylinder(id, x, y, z, radius, height, color) {
                        const geometry = new THREE.CylinderGeometry(radius, radius, height, 32);
                        const material = new THREE.MeshStandardMaterial({
                            color: color,
                            roughness: 0.7,
                            metalness: 0.3
                        });
                        const cylinder = new THREE.Mesh(geometry, material);
                        cylinder.position.set(x, y, z);
                        scene.add(cylinder);

                        // Store reference
                        objects[id] = cylinder;

                        // Animate creation
                        gsap.from(cylinder.scale, {
                            x: 0,
                            y: 0,
                            z: 0,
                            duration: 1,
                            ease: "elastic.out(1, 0.3)"
                        });

                        return id;
                    }

                    // Create text
                    function createText(id, text, x, y, z, size, color) {
                        // Create a canvas for the text
                        const canvas = document.createElement('canvas');
                        const context = canvas.getContext('2d');
                        canvas.width = 256;
                        canvas.height = 256;

                        // Draw text on canvas
                        context.fillStyle = '#ffffff';
                        context.fillRect(0, 0, canvas.width, canvas.height);
                        context.font = '80px Arial';
                        context.fillStyle = color;
                        context.textAlign = 'center';
                        context.textBaseline = 'middle';
                        context.fillText(text, canvas.width / 2, canvas.height / 2);

                        // Create texture from canvas
                        const texture = new THREE.CanvasTexture(canvas);

                        // Create a plane with the texture
                        const geometry = new THREE.PlaneGeometry(size, size);
                        const material = new THREE.MeshBasicMaterial({
                            map: texture,
                            transparent: true
                        });
                        const textPlane = new THREE.Mesh(geometry, material);
                        textPlane.position.set(x, y, z);
                        scene.add(textPlane);

                        // Store reference
                        objects[id] = textPlane;

                        // Animate creation
                        gsap.from(textPlane.scale, {
                            x: 0,
                            y: 0,
                            z: 0,
                            duration: 1,
                            ease: "elastic.out(1, 0.3)"
                        });

                        return id;
                    }

                    // Create a connection between objects
                    function createConnection(id, fromId, toId, color) {
                        const fromObj = objects[fromId];
                        const toObj = objects[toId];

                        if (!fromObj || !toObj) return null;

                        // Create a line between the objects
                        const points = [];
                        points.push(fromObj.position);
                        points.push(toObj.position);

                        const geometry = new THREE.BufferGeometry().setFromPoints(points);
                        const material = new THREE.LineBasicMaterial({ color: color });
                        const line = new THREE.Line(geometry, material);
                        scene.add(line);

                        // Store reference
                        objects[id] = line;

                        // Update line when objects move
                        const updateLine = () => {
                            const positions = line.geometry.attributes.position.array;
                            positions[0] = fromObj.position.x;
                            positions[1] = fromObj.position.y;
                            positions[2] = fromObj.position.z;
                            positions[3] = toObj.position.x;
                            positions[4] = toObj.position.y;
                            positions[5] = toObj.position.z;
                            line.geometry.attributes.position.needsUpdate = true;
                        };

                        // Initial update
                        updateLine();

                        return id;
                    }

                    // Animate object
                    function animateObject(id, property, value, duration) {
                        const obj = objects[id];
                        if (!obj) return;

                        const animProps = {};
                        animProps[property] = value;

                        gsap.to(obj[property], {
                            ...animProps,
                            duration: duration,
                            ease: "power2.out"
                        });
                    }

                    // Remove object
                    function removeObject(id) {
                        const obj = objects[id];
                        if (!obj) return;

                        // Animate removal
                        gsap.to(obj.scale, {
                            x: 0,
                            y: 0,
                            z: 0,
                            duration: 0.5,
                            ease: "power2.in",
                            onComplete: () => {
                                scene.remove(obj);
                                delete objects[id];
                            }
                        });
                    }

                    // Clear all objects
                    function clearScene() {
                        for (const id in objects) {
                            scene.remove(objects[id]);
                        }
                        objects = {};
                    }

                    // Android interface
                    window.Android = {
                        onHabitsLoaded: function(habitsJson) {
                            try {
                                const habits = JSON.parse(habitsJson);
                                visualizeHabits(habits);
                            } catch (e) {
                                console.error("Error parsing habits JSON:", e);
                            }
                        }
                    };

                    // Visualize habits
                    function visualizeHabits(habits) {
                        // Clear existing objects
                        clearScene();

                        // Calculate positions in a circle
                        const radius = 10;
                        const angleStep = (2 * Math.PI) / habits.length;

                        // Create objects for each habit
                        habits.forEach((habit, index) => {
                            const angle = index * angleStep;
                            const x = radius * Math.cos(angle);
                            const z = radius * Math.sin(angle);

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

                            // Create object based on category
                            let objectId;
                            switch (habit.category) {
                                case "HEALTH":
                                    objectId = createSphere(habit.id, x, 0, z, 1, color);
                                    break;
                                case "PRODUCTIVITY":
                                    objectId = createCube(habit.id, x, 0, z, 1.5, color);
                                    break;
                                case "EDUCATION":
                                    objectId = createCylinder(habit.id, x, 0, z, 0.8, 2, color);
                                    break;
                                default:
                                    objectId = createSphere(habit.id, x, 0, z, 1, color);
                            }

                            // Create text label
                            createText(habit.id + "_label", habit.title.charAt(0), x, 2, z, 1, color);

                            // Animate based on streak
                            if (habit.streak > 0) {
                                animateObject(habit.id, "rotation", { y: Math.PI * 2 }, 2);
                            }
                        });

                        // Create connections between related habits
                        if (habits.length > 1) {
                            for (let i = 0; i < habits.length - 1; i++) {
                                createConnection(
                                    "connection_" + i,
                                    habits[i].id,
                                    habits[i + 1].id,
                                    0x444444
                                );
                            }
                        }
                    }
                </script>
            </body>
            </html>
        """
    }

    // WebView instance
    private var webView: WebView? = null

    // Scene state
    private val _isSceneReady = MutableStateFlow(false)
    val isSceneReady: StateFlow<Boolean> = _isSceneReady.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Coroutine scope
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // Gson for JSON serialization
    private val gson = Gson()

    /**
     * Initialize WebView
     */
    fun initialize(webView: WebView) {
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
        webView.addJavascriptInterface(ThreeJsInterface(), "AndroidBridge")

        // Set WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                _isSceneReady.value = true
                Log.d(TAG, "Three.js scene loaded")
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
            val inputStream = context.assets.open("html/threejs_template.html")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val html = String(buffer, Charsets.UTF_8)

            webView?.loadDataWithBaseURL(
                "https://threejs.org/",
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
     * Visualize habits
     */
    fun visualizeHabits() {
        coroutineScope.launch {
            try {
                // Get habits
                val habits = habitRepository.getAllHabits().first()

                // Convert to JSON
                val habitsJson = gson.toJson(habits)

                // Send to WebView
                withContext(Dispatchers.Main) {
                    webView?.evaluateJavascript(
                        "window.Android.onHabitsLoaded('$habitsJson')",
                        null
                    )
                }

                Log.d(TAG, "Visualizing ${habits.size} habits")
            } catch (e: Exception) {
                _error.value = "Error visualizing habits: ${e.message}"
                Log.e(TAG, "Error visualizing habits", e)
            }
        }
    }

    /**
     * Visualize completions
     */
    fun visualizeCompletions() {
        coroutineScope.launch {
            try {
                // Get habits and completions
                val habits = habitRepository.getAllHabits().first()
                val completionsMap = mutableMapOf<String, List<HabitCompletion>>()

                // Get completions for each habit
                habits.forEach { habit ->
                    val completions = habitRepository.getHabitCompletions(habit.id).first()
                    completionsMap[habit.id] = completions
                }

                // Convert to JSON
                val habitsJson = gson.toJson(habits)
                val completionsJson = gson.toJson(completionsMap)

                // Send to WebView
                withContext(Dispatchers.Main) {
                    webView?.evaluateJavascript(
                        """
                        (function() {
                            // Clear existing objects
                            clearScene();

                            // Parse data
                            const habits = $habitsJson;
                            const completionsMap = $completionsJson;

                            // Calculate positions in a circle
                            const radius = 15;
                            const angleStep = (2 * Math.PI) / habits.length;

                            // Create objects for each habit
                            habits.forEach((habit, index) => {
                                const angle = index * angleStep;
                                const x = radius * Math.cos(angle);
                                const z = radius * Math.sin(angle);

                                // Get completions for this habit
                                const completions = completionsMap[habit.id] || [];

                                // Create cylinder for habit
                                createCylinder(
                                    habit.id,
                                    x,
                                    0,
                                    z,
                                    0.8,
                                    Math.max(0.5, completions.length * 0.2),
                                    0x2196f3
                                );

                                // Create text label with completion count
                                createText(
                                    habit.id + "_label",
                                    habit.name + " (" + completions.length + ")",
                                    x,
                                    Math.max(0.5, completions.length * 0.2) + 1,
                                    z,
                                    1,
                                    0xffffff
                                );

                                // Create small spheres for each completion
                                completions.forEach((completion, cIndex) => {
                                    const cAngle = cIndex * (Math.PI * 2 / completions.length);
                                    const cRadius = 3;
                                    const cx = x + cRadius * Math.cos(cAngle);
                                    const cz = z + cRadius * Math.sin(cAngle);

                                    // Create small sphere for completion
                                    createSphere(
                                        completion.id,
                                        cx,
                                        0.5,
                                        cz,
                                        0.3,
                                        0x4caf50
                                    );
                                });
                            });
                        })();
                        """,
                        null
                    )
                }

                Log.d(TAG, "Visualizing completions for ${habits.size} habits")
            } catch (e: Exception) {
                _error.value = "Error visualizing completions: ${e.message}"
                Log.e(TAG, "Error visualizing completions", e)
            }
        }
    }

    /**
     * Visualize streaks
     */
    fun visualizeStreaks() {
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
                            // Clear existing objects
                            clearScene();

                            // Parse data
                            const habits = $habitsJson;

                            // Calculate positions in a circle
                            const radius = 15;
                            const angleStep = (2 * Math.PI) / habits.length;

                            // Create objects for each habit
                            habits.forEach((habit, index) => {
                                const angle = index * angleStep;
                                const x = radius * Math.cos(angle);
                                const z = radius * Math.sin(angle);

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
                                createCylinder(
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
                                    habit.name + " (" + habit.streak + ")",
                                    x,
                                    Math.max(0.5, habit.streak) + 1,
                                    z,
                                    1,
                                    0xffffff
                                );
                            });
                        })();
                        """,
                        null
                    )
                }

                Log.d(TAG, "Visualizing streaks for ${habits.size} habits")
            } catch (e: Exception) {
                _error.value = "Error visualizing streaks: ${e.message}"
                Log.e(TAG, "Error visualizing streaks", e)
            }
        }
    }

    /**
     * JavaScript interface for communication with Three.js
     */
    inner class ThreeJsInterface {

        @JavascriptInterface
        fun onSceneReady() {
            coroutineScope.launch {
                _isSceneReady.value = true
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

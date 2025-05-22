/**
 * Three.js Bridge for Android WebView
 * This file provides a bridge between Android WebView and Three.js
 */

// Import Three.js
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

// Global object to expose to Android WebView
window.ThreeBridge = {
    // Store scenes, cameras, renderers, etc.
    _scenes: {},
    _cameras: {},
    _renderers: {},
    _controls: {},
    _objects: {},
    _animations: {},
    _raycaster: new THREE.Raycaster(),
    _mouse: new THREE.Vector2(),
    
    /**
     * Initialize a Three.js scene
     * @param {String} containerId Container element ID
     * @param {Object} options Scene options
     * @returns {String} Scene ID
     */
    initScene: function(containerId, options = {}) {
        const container = document.getElementById(containerId);
        if (!container) {
            console.error('Container not found:', containerId);
            return null;
        }
        
        // Generate a unique ID for this scene
        const sceneId = 'scene_' + Math.random().toString(36).substr(2, 9);
        
        // Create scene
        const scene = new THREE.Scene();
        if (options.backgroundColor) {
            scene.background = new THREE.Color(options.backgroundColor);
        }
        
        // Create camera
        const width = container.clientWidth;
        const height = container.clientHeight;
        const camera = new THREE.PerspectiveCamera(
            options.fov || 75,
            width / height,
            options.near || 0.1,
            options.far || 1000
        );
        camera.position.z = options.cameraDistance || 5;
        
        // Create renderer
        const renderer = new THREE.WebGLRenderer({ 
            antialias: options.antialias !== false,
            alpha: options.alpha !== false
        });
        renderer.setSize(width, height);
        renderer.setPixelRatio(window.devicePixelRatio);
        if (options.shadows) {
            renderer.shadowMap.enabled = true;
            renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        }
        container.appendChild(renderer.domElement);
        
        // Add controls if enabled
        let controls = null;
        if (options.controls !== false) {
            controls = new OrbitControls(camera, renderer.domElement);
            controls.enableDamping = true;
            controls.dampingFactor = 0.05;
            controls.enableZoom = options.enableZoom !== false;
            controls.enablePan = options.enablePan !== false;
            controls.enableRotate = options.enableRotate !== false;
            
            if (options.autoRotate) {
                controls.autoRotate = true;
                controls.autoRotateSpeed = options.autoRotateSpeed || 1.0;
            }
            
            this._controls[sceneId] = controls;
        }
        
        // Add ambient light
        if (options.ambientLight !== false) {
            const ambientLight = new THREE.AmbientLight(
                options.ambientLightColor || 0xffffff,
                options.ambientLightIntensity || 0.5
            );
            scene.add(ambientLight);
        }
        
        // Add directional light
        if (options.directionalLight !== false) {
            const directionalLight = new THREE.DirectionalLight(
                options.directionalLightColor || 0xffffff,
                options.directionalLightIntensity || 0.8
            );
            directionalLight.position.set(
                options.directionalLightX || 5,
                options.directionalLightY || 5,
                options.directionalLightZ || 5
            );
            if (options.shadows) {
                directionalLight.castShadow = true;
                directionalLight.shadow.mapSize.width = 1024;
                directionalLight.shadow.mapSize.height = 1024;
            }
            scene.add(directionalLight);
        }
        
        // Store scene, camera, and renderer
        this._scenes[sceneId] = scene;
        this._cameras[sceneId] = camera;
        this._renderers[sceneId] = renderer;
        this._objects[sceneId] = {};
        
        // Set up animation loop
        const animate = () => {
            requestAnimationFrame(animate);
            
            // Update controls
            if (controls) {
                controls.update();
            }
            
            // Run custom animations
            if (this._animations[sceneId]) {
                for (const animFn of this._animations[sceneId]) {
                    animFn();
                }
            }
            
            // Render scene
            renderer.render(scene, camera);
        };
        
        // Start animation loop
        animate();
        
        // Add resize handler
        window.addEventListener('resize', () => {
            const width = container.clientWidth;
            const height = container.clientHeight;
            
            camera.aspect = width / height;
            camera.updateProjectionMatrix();
            
            renderer.setSize(width, height);
        });
        
        return sceneId;
    },
    
    /**
     * Add a cube to the scene
     * @param {String} sceneId Scene ID
     * @param {Object} options Cube options
     * @returns {String} Object ID
     */
    addCube: function(sceneId, options = {}) {
        const scene = this._scenes[sceneId];
        if (!scene) return null;
        
        const geometry = new THREE.BoxGeometry(
            options.width || 1,
            options.height || 1,
            options.depth || 1
        );
        
        const material = new THREE.MeshStandardMaterial({
            color: options.color || 0x00ff00,
            metalness: options.metalness || 0.2,
            roughness: options.roughness || 0.7
        });
        
        const cube = new THREE.Mesh(geometry, material);
        cube.position.set(
            options.x || 0,
            options.y || 0,
            options.z || 0
        );
        
        if (options.castShadow) {
            cube.castShadow = true;
        }
        
        if (options.receiveShadow) {
            cube.receiveShadow = true;
        }
        
        scene.add(cube);
        
        // Generate object ID and store reference
        const objectId = 'obj_' + Math.random().toString(36).substr(2, 9);
        this._objects[sceneId][objectId] = cube;
        
        return objectId;
    }
};

// Notify Android that the bridge is ready
if (window.AndroidThreeBridge) {
    window.AndroidThreeBridge.onBridgeReady();
}

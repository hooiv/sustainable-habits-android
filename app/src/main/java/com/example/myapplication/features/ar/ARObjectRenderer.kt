package com.example.myapplication.features.ar

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.sqrt

/**
 * Renders objects in AR space
 */
class ARObjectRenderer {
    private val TAG = "ARObjectRenderer"
    
    // Shader code
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec4 vColor;
        varying vec4 fragmentColor;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            fragmentColor = vColor;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec4 fragmentColor;
        void main() {
            gl_FragColor = fragmentColor;
        }
    """
    
    // OpenGL program
    private var program = 0
    
    // Handles
    private var positionHandle = 0
    private var colorHandle = 0
    private var mvpMatrixHandle = 0
    
    // Buffers for cube
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null
    
    // Model matrix
    private val modelMatrix = FloatArray(16)
    
    // MVP matrix
    private val mvpMatrix = FloatArray(16)
    
    // Temporary matrices
    private val tempMatrix = FloatArray(16)
    
    // Cube vertices
    private val cubeCoords = floatArrayOf(
        // Front face
        -0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,
        
        // Back face
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        -0.5f, 0.5f, -0.5f
    )
    
    // Cube colors (RGBA)
    private val cubeColors = floatArrayOf(
        // Front face (purple)
        0.6f, 0.0f, 0.9f, 1.0f,
        0.6f, 0.0f, 0.9f, 1.0f,
        0.6f, 0.0f, 0.9f, 1.0f,
        0.6f, 0.0f, 0.9f, 1.0f,
        
        // Back face (blue)
        0.0f, 0.3f, 0.8f, 1.0f,
        0.0f, 0.3f, 0.8f, 1.0f,
        0.0f, 0.3f, 0.8f, 1.0f,
        0.0f, 0.3f, 0.8f, 1.0f
    )
    
    // Cube indices
    private val cubeIndices = shortArrayOf(
        0, 1, 2, 0, 2, 3, // Front face
        4, 5, 6, 4, 6, 7, // Back face
        0, 4, 7, 0, 7, 3, // Left face
        1, 5, 6, 1, 6, 2, // Right face
        3, 2, 6, 3, 6, 7, // Top face
        0, 1, 5, 0, 5, 4  // Bottom face
    )
    
    /**
     * Initialize the renderer
     */
    fun createOnGlThread(context: Context) {
        try {
            // Prepare buffers
            prepareBuffers()
            
            // Create program
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
            
            program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
            
            // Get handles
            positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
            colorHandle = GLES20.glGetAttribLocation(program, "vColor")
            mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
            
            // Set model matrix to identity
            Matrix.setIdentityM(modelMatrix, 0)
            
            Log.d(TAG, "Created OpenGL program successfully")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to create OpenGL program", e)
        }
    }
    
    /**
     * Prepare vertex, color, and index buffers
     */
    private fun prepareBuffers() {
        // Vertex buffer
        val byteBuf = ByteBuffer.allocateDirect(cubeCoords.size * 4)
        byteBuf.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuf.asFloatBuffer()
        vertexBuffer?.put(cubeCoords)
        vertexBuffer?.position(0)
        
        // Color buffer
        val colorBuf = ByteBuffer.allocateDirect(cubeColors.size * 4)
        colorBuf.order(ByteOrder.nativeOrder())
        colorBuffer = colorBuf.asFloatBuffer()
        colorBuffer?.put(cubeColors)
        colorBuffer?.position(0)
        
        // Index buffer
        val indexBuf = ByteBuffer.allocateDirect(cubeIndices.size * 2)
        indexBuf.order(ByteOrder.nativeOrder())
        indexBuffer = indexBuf.asShortBuffer()
        indexBuffer?.put(cubeIndices)
        indexBuffer?.position(0)
    }
    
    /**
     * Load shader
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
    
    /**
     * Draw an object at the given anchor
     */
    fun draw(viewMatrix: FloatArray, projectionMatrix: FloatArray, anchor: Anchor, scale: Float = 0.1f) {
        // Get anchor pose
        val anchorPose = anchor.pose
        
        // Set model matrix based on anchor pose
        anchorPose.toMatrix(modelMatrix, 0)
        
        // Scale the model
        scaleMatrix(modelMatrix, scale, scale, scale)
        
        // Calculate MVP matrix
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        
        // Draw the object
        drawObject()
    }
    
    /**
     * Draw the object using current MVP matrix
     */
    private fun drawObject() {
        GLES20.glUseProgram(program)
        
        // Set vertex attributes
        vertexBuffer?.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)
        
        colorBuffer?.position(0)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
        GLES20.glEnableVertexAttribArray(colorHandle)
        
        // Set MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        
        // Draw
        indexBuffer?.position(0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cubeIndices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
    
    /**
     * Scale a matrix
     */
    private fun scaleMatrix(matrix: FloatArray, x: Float, y: Float, z: Float) {
        matrix[0] *= x
        matrix[1] *= x
        matrix[2] *= x
        matrix[4] *= y
        matrix[5] *= y
        matrix[6] *= y
        matrix[8] *= z
        matrix[9] *= z
        matrix[10] *= z
    }
}
